package com.warrantywave.service;

import com.warrantywave.exception.BadRequestException;
import com.warrantywave.model.dto.ClaimRequestDto;
import com.warrantywave.model.dto.ClaimResponseDto;
import com.warrantywave.model.dto.ClaimReviewDto;
import com.warrantywave.model.entity.*;
import com.warrantywave.model.enums.ClaimStatus;
import com.warrantywave.model.enums.ContractStatus;
import com.warrantywave.model.enums.PlanType;
import com.warrantywave.model.enums.UserRole;
import com.warrantywave.repository.ClaimRepository;
import com.warrantywave.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClaimServiceTest {

    @Mock
    private ClaimRepository claimRepository;

    @Mock
    private WarrantyContractService contractService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private ClaimService claimService;

    private WarrantyContract contract;
    private WarrantyPlan plan;
    private Customer customer;
    private Vehicle vehicle;
    private User reviewer;

    @BeforeEach
    void setUp() {
        customer = new Customer();
        customer.setId(1L);
        customer.setFirstName("Mark");
        customer.setLastName("Spencer");

        vehicle = new Vehicle();
        vehicle.setId(5L);
        vehicle.setVin("1ABC9999999999999");
        vehicle.setMake("Ford");
        vehicle.setModel("Explorer");
        vehicle.setModelYear(2022);
        vehicle.setMileage(35000);

        plan = new WarrantyPlan();
        plan.setId(20L);
        plan.setPlanName("Comprehensive Gold");
        plan.setPlanType(PlanType.COMPREHENSIVE);
        plan.setDeductibleAmount(new BigDecimal("150.00"));
        plan.setMaxClaimLimit(new BigDecimal("10000.00"));

        contract = new WarrantyContract();
        contract.setId(100L);
        contract.setContractNumber("WC-2024-TEST");
        contract.setCustomer(customer);
        contract.setVehicle(vehicle);
        contract.setPlan(plan);
        contract.setStatus(ContractStatus.ACTIVE);
        contract.setStartDate(LocalDate.now().minusMonths(6));
        contract.setEndDate(LocalDate.now().plusMonths(30));
        contract.setMaxMileage(80000);

        reviewer = new User("claims_officer", "hash", "Claims Officer", "claims@test.com", UserRole.ROLE_CLAIMS_OFFICER);
    }

    @Test
    @DisplayName("Should successfully submit claim when within coverage terms")
    void testSubmitClaimSuccess() {
        ClaimRequestDto request = new ClaimRequestDto(
            100L,
            LocalDate.now().minusDays(5),
            42000,
            "Engine water pump coolant leak",
            "Metro Auto Repair",
            new BigDecimal("1200.00")
        );

        when(contractService.getContractEntity(100L)).thenReturn(contract);
        when(claimRepository.save(any(Claim.class))).thenAnswer(invocation -> {
            Claim saved = invocation.getArgument(0);
            saved.setId(500L);
            return saved;
        });

        ClaimResponseDto response = claimService.submitClaim(request, "claims_officer");

        assertNotNull(response);
        assertEquals(500L, response.id());
        assertEquals(ClaimStatus.SUBMITTED, response.status());
        assertEquals(new BigDecimal("1200.00"), response.repairEstimate());
        verify(auditService).log(eq("CLAIMS"), eq(500L), eq("SUBMIT"), any(), any());
    }

    @Test
    @DisplayName("Should reject claim submission if vehicle mileage exceeds contract maximum")
    void testSubmitClaimExceedsMileage() {
        ClaimRequestDto request = new ClaimRequestDto(
            100L,
            LocalDate.now().minusDays(2),
            95000, // exceeds 80000
            "Transmission failure",
            "Precision Auto",
            new BigDecimal("3500.00")
        );

        when(contractService.getContractEntity(100L)).thenReturn(contract);

        assertThrows(BadRequestException.class, () -> claimService.submitClaim(request, "tester"));
        verify(claimRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should accurately calculate deductible deduction upon claim approval")
    void testReviewClaimApproval() {
        Claim claim = new Claim();
        claim.setId(301L);
        claim.setClaimNumber("CLM-TEST-01");
        claim.setContract(contract);
        claim.setCustomer(customer);
        claim.setVehicle(vehicle);
        claim.setRepairEstimate(new BigDecimal("1500.00"));
        claim.setStatus(ClaimStatus.SUBMITTED);

        when(claimRepository.findById(301L)).thenReturn(Optional.of(claim));
        when(userRepository.findByUsername("claims_officer")).thenReturn(Optional.of(reviewer));
        when(claimRepository.save(any(Claim.class))).thenAnswer(inv -> inv.getArgument(0));

        ClaimReviewDto reviewDto = new ClaimReviewDto(
            true,
            null, // auto-calculate based on estimate minus deductible
            new BigDecimal("150.00"),
            "Repair estimate verified with shop invoice",
            null
        );

        ClaimResponseDto result = claimService.reviewClaim(301L, reviewDto, "claims_officer");

        assertEquals(ClaimStatus.APPROVED, result.status());
        assertEquals(new BigDecimal("150.00"), result.deductiblePaid());
        // 1500 - 150 = 1350 approved amount
        assertEquals(new BigDecimal("1350.00"), result.approvedAmount());
    }

    @Test
    @DisplayName("Should reject claim and require a rejection reason")
    void testReviewClaimRejection() {
        Claim claim = new Claim();
        claim.setId(302L);
        claim.setClaimNumber("CLM-TEST-02");
        claim.setContract(contract);
        claim.setCustomer(customer);
        claim.setVehicle(vehicle);
        claim.setRepairEstimate(new BigDecimal("800.00"));
        claim.setStatus(ClaimStatus.SUBMITTED);

        when(claimRepository.findById(302L)).thenReturn(Optional.of(claim));
        when(userRepository.findByUsername("claims_officer")).thenReturn(Optional.of(reviewer));
        when(claimRepository.save(any(Claim.class))).thenAnswer(inv -> inv.getArgument(0));

        ClaimReviewDto reviewDto = new ClaimReviewDto(
            false,
            null,
            null,
            "Component wear not covered under policy",
            "Exclusion clause 3.1: normal wear and tear"
        );

        ClaimResponseDto result = claimService.reviewClaim(302L, reviewDto, "claims_officer");

        assertEquals(ClaimStatus.REJECTED, result.status());
        assertEquals(BigDecimal.ZERO, result.approvedAmount());
        assertEquals("Exclusion clause 3.1: normal wear and tear", result.rejectionReason());
    }
}
