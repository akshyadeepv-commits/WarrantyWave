package com.warrantywave.service;

import com.warrantywave.exception.BadRequestException;
import com.warrantywave.exception.ResourceNotFoundException;
import com.warrantywave.model.dto.WarrantyPlanDto;
import com.warrantywave.model.entity.WarrantyPlan;
import com.warrantywave.repository.WarrantyPlanRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class WarrantyPlanService {

    private final WarrantyPlanRepository planRepository;
    private final AuditService auditService;

    public WarrantyPlanService(WarrantyPlanRepository planRepository, AuditService auditService) {
        this.planRepository = planRepository;
        this.auditService = auditService;
    }

    public List<WarrantyPlanDto> getAllPlans() {
        return planRepository.findAll().stream()
            .map(this::toDto)
            .toList();
    }

    public List<WarrantyPlanDto> getActivePlans() {
        return planRepository.findByActiveTrue().stream()
            .map(this::toDto)
            .toList();
    }

    public WarrantyPlanDto getPlanById(Long id) {
        return planRepository.findById(id)
            .map(this::toDto)
            .orElseThrow(() -> new ResourceNotFoundException("Warranty plan not found with id: " + id));
    }

    public WarrantyPlan getPlanEntity(Long id) {
        return planRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Warranty plan not found with id: " + id));
    }

    @Transactional
    public WarrantyPlanDto createPlan(WarrantyPlanDto dto, String performedBy) {
        if (planRepository.existsByPlanCode(dto.planCode().trim().toUpperCase())) {
            throw new BadRequestException("Plan with code " + dto.planCode() + " already exists.");
        }

        WarrantyPlan plan = new WarrantyPlan();
        plan.setPlanCode(dto.planCode().trim().toUpperCase());
        plan.setPlanName(dto.planName().trim());
        plan.setPlanType(dto.planType());
        plan.setDescription(dto.description().trim());
        plan.setDurationMonths(dto.durationMonths());
        plan.setMaxMileageCoverage(dto.maxMileageCoverage());
        plan.setDeductibleAmount(dto.deductibleAmount());
        plan.setBasePremium(dto.basePremium());
        plan.setMaxClaimLimit(dto.maxClaimLimit());
        plan.setActive(dto.active());

        WarrantyPlan saved = planRepository.save(plan);

        auditService.log("WARRANTY_PLANS", saved.getId(), "CREATE", performedBy != null ? performedBy : "SYSTEM",
            "Created warranty plan: " + saved.getPlanName() + " (" + saved.getPlanCode() + ")");

        return toDto(saved);
    }

    @Transactional
    public WarrantyPlanDto updatePlan(Long id, WarrantyPlanDto dto, String performedBy) {
        WarrantyPlan plan = getPlanEntity(id);

        plan.setPlanName(dto.planName().trim());
        plan.setPlanType(dto.planType());
        plan.setDescription(dto.description().trim());
        plan.setDurationMonths(dto.durationMonths());
        plan.setMaxMileageCoverage(dto.maxMileageCoverage());
        plan.setDeductibleAmount(dto.deductibleAmount());
        plan.setBasePremium(dto.basePremium());
        plan.setMaxClaimLimit(dto.maxClaimLimit());
        plan.setActive(dto.active());

        WarrantyPlan updated = planRepository.save(plan);

        auditService.log("WARRANTY_PLANS", updated.getId(), "UPDATE", performedBy != null ? performedBy : "SYSTEM",
            "Updated warranty plan: " + updated.getPlanName());

        return toDto(updated);
    }

    public WarrantyPlanDto toDto(WarrantyPlan p) {
        return new WarrantyPlanDto(
            p.getId(),
            p.getPlanCode(),
            p.getPlanName(),
            p.getPlanType(),
            p.getDescription(),
            p.getDurationMonths(),
            p.getMaxMileageCoverage(),
            p.getDeductibleAmount(),
            p.getBasePremium(),
            p.getMaxClaimLimit(),
            p.isActive()
        );
    }
}
