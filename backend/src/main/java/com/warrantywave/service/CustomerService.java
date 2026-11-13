package com.warrantywave.service;

import com.warrantywave.exception.BadRequestException;
import com.warrantywave.exception.ResourceNotFoundException;
import com.warrantywave.model.dto.CustomerDto;
import com.warrantywave.model.entity.Customer;
import com.warrantywave.repository.CustomerRepository;
import com.warrantywave.repository.VehicleRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final VehicleRepository vehicleRepository;
    private final AuditService auditService;

    public CustomerService(CustomerRepository customerRepository, VehicleRepository vehicleRepository, AuditService auditService) {
        this.customerRepository = customerRepository;
        this.vehicleRepository = vehicleRepository;
        this.auditService = auditService;
    }

    public Page<CustomerDto> getAllCustomers(String query, Pageable pageable) {
        Page<Customer> page = (query != null && !query.trim().isEmpty())
            ? customerRepository.searchCustomers(query.trim(), pageable)
            : customerRepository.findAll(pageable);

        return page.map(this::toDto);
    }

    public CustomerDto getCustomerById(Long id) {
        return customerRepository.findById(id)
            .map(this::toDto)
            .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));
    }

    public Customer getCustomerEntity(Long id) {
        return customerRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));
    }

    @Transactional
    public CustomerDto createCustomer(CustomerDto dto, String performedBy) {
        if (customerRepository.existsByEmail(dto.email())) {
            throw new BadRequestException("A customer with email " + dto.email() + " already exists.");
        }

        Customer customer = new Customer();
        updateCustomerFields(customer, dto);
        Customer saved = customerRepository.save(customer);

        auditService.log("CUSTOMERS", saved.getId(), "CREATE", performedBy != null ? performedBy : "SYSTEM",
            "Created customer: " + saved.getFullName() + " (" + saved.getEmail() + ")");

        return toDto(saved);
    }

    @Transactional
    public CustomerDto updateCustomer(Long id, CustomerDto dto, String performedBy) {
        Customer customer = getCustomerEntity(id);

        if (!customer.getEmail().equalsIgnoreCase(dto.email()) && customerRepository.existsByEmail(dto.email())) {
            throw new BadRequestException("Email " + dto.email() + " is already in use by another customer.");
        }

        updateCustomerFields(customer, dto);
        Customer updated = customerRepository.save(customer);

        auditService.log("CUSTOMERS", updated.getId(), "UPDATE", performedBy != null ? performedBy : "SYSTEM",
            "Updated customer profile for: " + updated.getFullName());

        return toDto(updated);
    }

    @Transactional
    public void deleteCustomer(Long id, String performedBy) {
        Customer customer = getCustomerEntity(id);
        customerRepository.delete(customer);
        auditService.log("CUSTOMERS", id, "DELETE", performedBy != null ? performedBy : "SYSTEM",
            "Deleted customer: " + customer.getFullName());
    }

    private void updateCustomerFields(Customer customer, CustomerDto dto) {
        customer.setFirstName(dto.firstName());
        customer.setLastName(dto.lastName());
        customer.setEmail(dto.email());
        customer.setPhone(dto.phone());
        customer.setStreetAddress(dto.streetAddress());
        customer.setCity(dto.city());
        customer.setStateProvince(dto.stateProvince());
        customer.setPostalCode(dto.postalCode());
        customer.setCreditScore(dto.creditScore());
        customer.setAnnualIncome(dto.annualIncome());
        customer.setEmploymentStatus(dto.employmentStatus());
    }

    public CustomerDto toDto(Customer customer) {
        long vehicleCount = customer.getId() != null ? vehicleRepository.countByCustomerId(customer.getId()) : 0;
        return new CustomerDto(
            customer.getId(),
            customer.getFirstName(),
            customer.getLastName(),
            customer.getFullName(),
            customer.getEmail(),
            customer.getPhone(),
            customer.getStreetAddress(),
            customer.getCity(),
            customer.getStateProvince(),
            customer.getPostalCode(),
            customer.getCreditScore(),
            customer.getAnnualIncome(),
            customer.getEmploymentStatus(),
            (int) vehicleCount
        );
    }
}
