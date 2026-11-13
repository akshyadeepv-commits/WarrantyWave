package com.warrantywave.service;

import com.warrantywave.exception.BadRequestException;
import com.warrantywave.exception.ResourceNotFoundException;
import com.warrantywave.model.dto.CustomerDto;
import com.warrantywave.model.entity.Customer;
import com.warrantywave.repository.CustomerRepository;
import com.warrantywave.repository.VehicleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private CustomerService customerService;

    private Customer sampleCustomer;
    private CustomerDto sampleDto;

    @BeforeEach
    void setUp() {
        sampleCustomer = new Customer();
        sampleCustomer.setId(10L);
        sampleCustomer.setFirstName("Sarah");
        sampleCustomer.setLastName("Connor");
        sampleCustomer.setEmail("sarah.connor@sky.net");
        sampleCustomer.setPhone("+1-555-0100");
        sampleCustomer.setStreetAddress("12 Cyber Way");
        sampleCustomer.setCity("Los Angeles");
        sampleCustomer.setStateProvince("CA");
        sampleCustomer.setPostalCode("90001");
        sampleCustomer.setCreditScore(750);
        sampleCustomer.setAnnualIncome(new BigDecimal("110000.00"));
        sampleCustomer.setEmploymentStatus("EMPLOYED");

        sampleDto = new CustomerDto(
            null,
            "Sarah",
            "Connor",
            null,
            "sarah.connor@sky.net",
            "+1-555-0100",
            "12 Cyber Way",
            "Los Angeles",
            "CA",
            "90001",
            750,
            new BigDecimal("110000.00"),
            "EMPLOYED",
            0
        );
    }

    @Test
    @DisplayName("Should successfully create a new customer when email is unique")
    void testCreateCustomerSuccess() {
        when(customerRepository.existsByEmail("sarah.connor@sky.net")).thenReturn(false);
        when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> {
            Customer c = invocation.getArgument(0);
            c.setId(10L);
            return c;
        });
        when(vehicleRepository.countByCustomerId(10L)).thenReturn(0L);

        CustomerDto created = customerService.createCustomer(sampleDto, "ADMIN");

        assertNotNull(created);
        assertEquals("Sarah Connor", created.fullName());
        assertEquals("sarah.connor@sky.net", created.email());
        assertEquals(750, created.creditScore());
        verify(auditService).log(eq("CUSTOMERS"), eq(10L), eq("CREATE"), eq("ADMIN"), anyString());
    }

    @Test
    @DisplayName("Should reject customer creation when email already exists")
    void testCreateCustomerDuplicateEmail() {
        when(customerRepository.existsByEmail("sarah.connor@sky.net")).thenReturn(true);

        BadRequestException ex = assertThrows(BadRequestException.class, () ->
            customerService.createCustomer(sampleDto, "ADMIN")
        );

        assertTrue(ex.getMessage().contains("already exists"));
        verify(customerRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should retrieve customer by ID with accurate vehicle count")
    void testGetCustomerById() {
        when(customerRepository.findById(10L)).thenReturn(Optional.of(sampleCustomer));
        when(vehicleRepository.countByCustomerId(10L)).thenReturn(2L);

        CustomerDto found = customerService.getCustomerById(10L);

        assertNotNull(found);
        assertEquals(10L, found.id());
        assertEquals(2, found.vehicleCount());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException for non-existent customer ID")
    void testGetCustomerNotFound() {
        when(customerRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> customerService.getCustomerById(999L));
    }
}
