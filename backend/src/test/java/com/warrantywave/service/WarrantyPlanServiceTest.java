package com.warrantywave.service;

import com.warrantywave.exception.BadRequestException;
import com.warrantywave.exception.ResourceNotFoundException;
import com.warrantywave.model.dto.WarrantyPlanDto;
import com.warrantywave.model.entity.WarrantyPlan;
import com.warrantywave.model.enums.PlanType;
import com.warrantywave.repository.WarrantyPlanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WarrantyPlanServiceTest {

    @Mock
    private WarrantyPlanRepository planRepository;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private WarrantyPlanService planService;

    private WarrantyPlan samplePlan;
    private WarrantyPlanDto sampleDto;

    @BeforeEach
    void setUp() {
        samplePlan = new WarrantyPlan();
        samplePlan.setId(50L);
        samplePlan.setPlanCode("PLATINUM-MAX");
        samplePlan.setPlanName("Platinum Maximum Protection");
        samplePlan.setPlanType(PlanType.BUMPER_TO_BUMPER);
        samplePlan.setDescription("Comprehensive exclusionary coverage.");
        samplePlan.setDurationMonths(60);
        samplePlan.setMaxMileageCoverage(100000);
        samplePlan.setDeductibleAmount(new BigDecimal("100.00"));
        samplePlan.setBasePremium(new BigDecimal("3200.00"));
        samplePlan.setMaxClaimLimit(new BigDecimal("25000.00"));
        samplePlan.setActive(true);

        sampleDto = new WarrantyPlanDto(
            null,
            "PLATINUM-MAX",
            "Platinum Maximum Protection",
            PlanType.BUMPER_TO_BUMPER,
            "Comprehensive exclusionary coverage.",
            60,
            100000,
            new BigDecimal("100.00"),
            new BigDecimal("3200.00"),
            new BigDecimal("25000.00"),
            true
        );
    }

    @Test
    @DisplayName("Should successfully create a new warranty plan")
    void testCreatePlanSuccess() {
        when(planRepository.existsByPlanCode("PLATINUM-MAX")).thenReturn(false);
        when(planRepository.save(any(WarrantyPlan.class))).thenAnswer(inv -> {
            WarrantyPlan p = inv.getArgument(0);
            p.setId(50L);
            return p;
        });

        WarrantyPlanDto created = planService.createPlan(sampleDto, "ADMIN");

        assertNotNull(created);
        assertEquals("PLATINUM-MAX", created.planCode());
        assertEquals("Platinum Maximum Protection", created.planName());
        verify(auditService).log(eq("WARRANTY_PLANS"), eq(50L), eq("CREATE"), eq("ADMIN"), anyString());
    }

    @Test
    @DisplayName("Should reject warranty plan when plan code already exists")
    void testCreatePlanDuplicateCode() {
        when(planRepository.existsByPlanCode("PLATINUM-MAX")).thenReturn(true);

        BadRequestException ex = assertThrows(BadRequestException.class, () ->
            planService.createPlan(sampleDto, "ADMIN")
        );

        assertTrue(ex.getMessage().contains("already exists"));
        verify(planRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should retrieve active plans list")
    void testGetActivePlans() {
        when(planRepository.findByActiveTrue()).thenReturn(List.of(samplePlan));

        List<WarrantyPlanDto> active = planService.getActivePlans();

        assertEquals(1, active.size());
        assertEquals("PLATINUM-MAX", active.get(0).planCode());
    }
}
