package com.warrantywave.repository;

import com.warrantywave.model.entity.WarrantyContract;
import com.warrantywave.model.enums.ContractStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WarrantyContractRepository extends JpaRepository<WarrantyContract, Long> {
    Optional<WarrantyContract> findByContractNumber(String contractNumber);
    boolean existsByContractNumber(String contractNumber);
    List<WarrantyContract> findByCustomerId(Long customerId);
    List<WarrantyContract> findByVehicleId(Long vehicleId);
    long countByStatus(ContractStatus status);

    @Query("SELECT c FROM WarrantyContract c WHERE " +
           "(:status IS NULL OR c.status = :status) AND " +
           "(:query IS NULL OR " +
           "LOWER(c.contractNumber) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(c.customer.firstName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(c.customer.lastName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(c.vehicle.vin) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<WarrantyContract> findFiltered(@Param("status") ContractStatus status, @Param("query") String query, Pageable pageable);
}
