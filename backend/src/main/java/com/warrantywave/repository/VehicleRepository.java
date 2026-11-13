package com.warrantywave.repository;

import com.warrantywave.model.entity.Vehicle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    Optional<Vehicle> findByVin(String vin);
    boolean existsByVin(String vin);
    List<Vehicle> findByCustomerId(Long customerId);
    long countByCustomerId(Long customerId);

    @Query("SELECT v FROM Vehicle v WHERE " +
           "LOWER(v.vin) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(v.make) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(v.model) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<Vehicle> searchVehicles(@Param("query") String query, Pageable pageable);
}
