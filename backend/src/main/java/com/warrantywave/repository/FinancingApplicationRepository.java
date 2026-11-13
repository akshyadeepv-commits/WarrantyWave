package com.warrantywave.repository;

import com.warrantywave.model.entity.FinancingApplication;
import com.warrantywave.model.enums.FinancingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface FinancingApplicationRepository extends JpaRepository<FinancingApplication, Long> {
    Optional<FinancingApplication> findByApplicationNumber(String applicationNumber);
    boolean existsByApplicationNumber(String applicationNumber);
    List<FinancingApplication> findByCustomerId(Long customerId);
    List<FinancingApplication> findByStatus(FinancingStatus status);
    long countByStatus(FinancingStatus status);

    @Query("SELECT f FROM FinancingApplication f WHERE " +
           "(:status IS NULL OR f.status = :status) AND " +
           "(:query IS NULL OR " +
           "LOWER(f.applicationNumber) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(f.customer.firstName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(f.customer.lastName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(f.vehicle.vin) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<FinancingApplication> findFiltered(@Param("status") FinancingStatus status, @Param("query") String query, Pageable pageable);

    @Query("SELECT COALESCE(SUM(f.loanAmount), 0) FROM FinancingApplication f WHERE f.status = 'APPROVED'")
    BigDecimal sumApprovedLoanAmount();

    List<FinancingApplication> findTop5ByOrderByCreatedAtDesc();
}
