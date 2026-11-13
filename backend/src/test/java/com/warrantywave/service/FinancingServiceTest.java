package com.warrantywave.service;

import com.warrantywave.model.dto.EmiCalculationRequest;
import com.warrantywave.model.dto.EmiCalculationResponse;
import com.warrantywave.model.enums.CreditTier;
import com.warrantywave.repository.FinancingApplicationRepository;
import com.warrantywave.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class FinancingServiceTest {

    @Mock
    private FinancingApplicationRepository financingRepository;

    @Mock
    private CustomerService customerService;

    @Mock
    private VehicleService vehicleService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private FinancingService financingService;

    @Test
    @DisplayName("Should accurately categorize credit score tiers")
    void testCreditTierEvaluation() {
        assertEquals(CreditTier.TIER_1_EXCELLENT, financingService.evaluateCreditTier(800));
        assertEquals(CreditTier.TIER_1_EXCELLENT, financingService.evaluateCreditTier(750));
        assertEquals(CreditTier.TIER_2_GOOD, financingService.evaluateCreditTier(720));
        assertEquals(CreditTier.TIER_2_GOOD, financingService.evaluateCreditTier(670));
        assertEquals(CreditTier.TIER_3_FAIR, financingService.evaluateCreditTier(640));
        assertEquals(CreditTier.TIER_3_FAIR, financingService.evaluateCreditTier(580));
        assertEquals(CreditTier.HIGH_RISK, financingService.evaluateCreditTier(550));
    }

    @Test
    @DisplayName("Should accurately calculate amortized monthly EMI and total interest")
    void testEmiCalculation() {
        // Vehicle price: $30,000, Down payment: $5,000, Loan: $25,000, 60 months, Credit score: 780 (Tier 1 -> 5.25%)
        EmiCalculationRequest request = new EmiCalculationRequest(
            new BigDecimal("30000.00"),
            new BigDecimal("5000.00"),
            60,
            780
        );

        EmiCalculationResponse response = financingService.calculateEmi(request);

        assertNotNull(response);
        assertEquals(new BigDecimal("25000.00"), response.loanAmount());
        assertEquals(CreditTier.TIER_1_EXCELLENT, response.creditTier());
        assertEquals(new BigDecimal("5.25"), response.estimatedInterestRate());
        
        // At 5.25% APR on 25,000 for 60 months, standard EMI is approx $474.50
        assertTrue(response.monthlyEmi().doubleValue() > 470.0 && response.monthlyEmi().doubleValue() < 480.0);
        assertTrue(response.totalInterest().doubleValue() > 3000.0);
        assertEquals(response.loanAmount().add(response.totalInterest()), response.totalPayment());
    }
}
