package com.warrantywave.service;

import com.warrantywave.model.dto.EligibilityCheckRequest;
import com.warrantywave.model.dto.EligibilityCheckResponse;
import com.warrantywave.model.entity.Customer;
import com.warrantywave.model.entity.Vehicle;
import com.warrantywave.model.entity.WarrantyPlan;
import com.warrantywave.model.enums.PlanType;
import com.warrantywave.repository.WarrantyContractRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Year;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WarrantyContractServiceTest {

    @Mock
    private WarrantyContractRepository contractRepository;

    @Mock
    private CustomerService customerService;

    @Mock
    private VehicleService vehicleService;

    @Mock
    private WarrantyPlanService planService;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private WarrantyContractService contractService;

    private Vehicle sampleVehicle;
    private WarrantyPlan samplePlan;

    @BeforeEach
    void setUp() {
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setFirstName("Jane");
        customer.setLastName("Doe");

        sampleVehicle = new Vehicle();
        sampleVehicle.setId(10L);
        sampleVehicle.setVin("1ABC2345678901234");
        sampleVehicle.setMake("Toyota");
        sampleVehicle.setModel("Camry");
        sampleVehicle.setModelYear(Year.now().getValue() - 2);
        sampleVehicle.setMileage(30000);
        sampleVehicle.setCustomer(customer);

        samplePlan = new WarrantyPlan();
        samplePlan.setId(100L);
        samplePlan.setPlanCode("PLAN-COMP-01");
        samplePlan.setPlanName("Comprehensive Guard");
        samplePlan.setPlanType(PlanType.COMPREHENSIVE);
        samplePlan.setDurationMonths(36);
        samplePlan.setMaxMileageCoverage(75000);
        samplePlan.setDeductibleAmount(new BigDecimal("100.00"));
        samplePlan.setBasePremium(new BigDecimal("1200.00"));
        samplePlan.setMaxClaimLimit(new BigDecimal("15000.00"));
    }

    @Test
    @DisplayName("Should pass eligibility check for vehicle within mileage and age limits")
    void testEligibilityPass() {
        when(vehicleService.getVehicleEntity(10L)).thenReturn(sampleVehicle);
        when(planService.getPlanEntity(100L)).thenReturn(samplePlan);

        EligibilityCheckRequest req = new EligibilityCheckRequest(10L, 100L, 30000);
        EligibilityCheckResponse response = contractService.validateEligibility(req);

        assertTrue(response.eligible());
        assertTrue(response.message().contains("qualifies"));
        assertEquals(30000, response.vehicleMileage());
    }

    @Test
    @DisplayName("Should fail eligibility check when vehicle mileage exceeds plan maximum")
    void testEligibilityFailHighMileage() {
        sampleVehicle.setMileage(95000);
        when(vehicleService.getVehicleEntity(10L)).thenReturn(sampleVehicle);
        when(planService.getPlanEntity(100L)).thenReturn(samplePlan);

        EligibilityCheckRequest req = new EligibilityCheckRequest(10L, 100L, 95000);
        EligibilityCheckResponse response = contractService.validateEligibility(req);

        assertFalse(response.eligible());
        assertTrue(response.message().contains("exceeds the plan's maximum threshold"));
    }

    @Test
    @DisplayName("Should fail eligibility check when vehicle age exceeds maximum allowable years")
    void testEligibilityFailOldVehicle() {
        sampleVehicle.setModelYear(Year.now().getValue() - 15);
        when(vehicleService.getVehicleEntity(10L)).thenReturn(sampleVehicle);
        when(planService.getPlanEntity(100L)).thenReturn(samplePlan);

        EligibilityCheckRequest req = new EligibilityCheckRequest(10L, 100L, 30000);
        EligibilityCheckResponse response = contractService.validateEligibility(req);

        assertFalse(response.eligible());
        assertTrue(response.message().contains("exceeds the maximum allowed age"));
    }
}
