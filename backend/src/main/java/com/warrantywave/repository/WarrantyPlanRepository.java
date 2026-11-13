package com.warrantywave.repository;

import com.warrantywave.model.entity.WarrantyPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WarrantyPlanRepository extends JpaRepository<WarrantyPlan, Long> {
    Optional<WarrantyPlan> findByPlanCode(String planCode);
    List<WarrantyPlan> findByActiveTrue();
    boolean existsByPlanCode(String planCode);
}
