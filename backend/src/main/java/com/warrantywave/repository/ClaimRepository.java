package com.warrantywave.repository;

import com.warrantywave.model.entity.Claim;
import com.warrantywave.model.enums.ClaimStatus;
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
public interface ClaimRepository extends JpaRepository<Claim, Long> {
    Optional<Claim> findByClaimNumber(String claimNumber);
    boolean existsByClaimNumber(String claimNumber);
    List<Claim> findByContractId(Long contractId);
    List<Claim> findByCustomerId(Long customerId);
    List<Claim> findByVehicleId(Long vehicleId);
    List<Claim> findByStatus(ClaimStatus status);
    long countByStatus(ClaimStatus status);

    @Query("SELECT c FROM Claim c WHERE " +
           "(:status IS NULL OR c.status = :status) AND " +
           "(:query IS NULL OR " +
           "LOWER(c.claimNumber) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(c.customer.firstName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(c.customer.lastName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(c.repairShopName) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<Claim> findFiltered(@Param("status") ClaimStatus status, @Param("query") String query, Pageable pageable);

    @Query("SELECT COALESCE(SUM(c.approvedAmount), 0) FROM Claim c WHERE c.status IN ('APPROVED', 'PAID')")
    BigDecimal sumApprovedClaimsAmount();

    List<Claim> findTop5ByOrderByCreatedAtDesc();
}
