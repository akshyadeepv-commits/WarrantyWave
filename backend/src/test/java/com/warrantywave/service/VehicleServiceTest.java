package com.warrantywave.service;

import com.warrantywave.exception.BadRequestException;
import com.warrantywave.exception.ResourceNotFoundException;
import com.warrantywave.model.dto.VehicleDto;
import com.warrantywave.model.entity.Customer;
import com.warrantywave.model.entity.Vehicle;
import com.warrantywave.model.enums.VehicleCondition;
import com.warrantywave.repository.VehicleRepository;
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
class VehicleServiceTest {

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private CustomerService customerService;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private VehicleService vehicleService;

    private Customer owner;
    private Vehicle sampleVehicle;
    private VehicleDto sampleDto;

    @BeforeEach
    void setUp() {
        owner = new Customer();
        owner.setId(1L);
        owner.setFirstName("Alice");
        owner.setLastName("Cooper");

        sampleVehicle = new Vehicle();
        sampleVehicle.setId(100L);
        sampleVehicle.setVin("1FA6P8CF5H5123456");
        sampleVehicle.setMake("Ford");
        sampleVehicle.setModel("Mustang GT");
        sampleVehicle.setModelYear(2023);
        sampleVehicle.setMileage(12500);
        sampleVehicle.setPurchasePrice(new BigDecimal("42000.00"));
        sampleVehicle.setPurchaseDate(LocalDate.of(2023, 6, 15));
        sampleVehicle.setVehicleCondition(VehicleCondition.CERTIFIED_PRE_OWNED);
        sampleVehicle.setStatus("ACTIVE");
        sampleVehicle.setCustomer(owner);

        sampleDto = new VehicleDto(
            null,
            "1FA6P8CF5H5123456",
            "Ford",
            "Mustang GT",
            2023,
            12500,
            new BigDecimal("42000.00"),
            LocalDate.of(2023, 6, 15),
            VehicleCondition.CERTIFIED_PRE_OWNED,
            "ACTIVE",
            1L,
            null
        );
    }

    @Test
    @DisplayName("Should successfully register vehicle with valid 17-char ISO 3779 VIN")
    void testRegisterVehicleSuccess() {
        when(vehicleRepository.existsByVin("1FA6P8CF5H5123456")).thenReturn(false);
        when(customerService.getCustomerEntity(1L)).thenReturn(owner);
        when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(inv -> {
            Vehicle v = inv.getArgument(0);
            v.setId(100L);
            return v;
        });

        VehicleDto result = vehicleService.createVehicle(sampleDto, "ADMIN");

        assertNotNull(result);
        assertEquals("1FA6P8CF5H5123456", result.vin());
        assertEquals("Ford", result.make());
        assertEquals("Alice Cooper", result.customerName());
        verify(auditService).log(eq("VEHICLES"), eq(100L), eq("CREATE"), eq("ADMIN"), anyString());
    }

    @Test
    @DisplayName("Should reject vehicle when VIN length is not exactly 17 characters")
    void testRegisterVehicleInvalidVinLength() {
        VehicleDto invalidVinDto = new VehicleDto(
            null,
            "SHORT_VIN",
            "Ford",
            "Mustang",
            2023,
            12500,
            new BigDecimal("42000.00"),
            LocalDate.now(),
            VehicleCondition.USED,
            "ACTIVE",
            1L,
            null
        );

        BadRequestException ex = assertThrows(BadRequestException.class, () ->
            vehicleService.createVehicle(invalidVinDto, "ADMIN")
        );

        assertTrue(ex.getMessage().contains("17 characters"));
        verify(vehicleRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should reject vehicle when VIN is already registered")
    void testRegisterVehicleDuplicateVin() {
        when(vehicleRepository.existsByVin("1FA6P8CF5H5123456")).thenReturn(true);

        BadRequestException ex = assertThrows(BadRequestException.class, () ->
            vehicleService.createVehicle(sampleDto, "ADMIN")
        );

        assertTrue(ex.getMessage().contains("already registered"));
        verify(vehicleRepository, never()).save(any());
    }
}
