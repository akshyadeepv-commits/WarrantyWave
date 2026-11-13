package com.warrantywave.config;

import com.warrantywave.model.entity.*;
import com.warrantywave.model.enums.*;
import com.warrantywave.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final VehicleRepository vehicleRepository;
    private final WarrantyPlanRepository warrantyPlanRepository;
    private final WarrantyContractRepository warrantyContractRepository;
    private final ClaimRepository claimRepository;
    private final FinancingApplicationRepository financingRepository;
    private final AuditLogRepository auditLogRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(
        UserRepository userRepository,
        CustomerRepository customerRepository,
        VehicleRepository vehicleRepository,
        WarrantyPlanRepository warrantyPlanRepository,
        WarrantyContractRepository warrantyContractRepository,
        ClaimRepository claimRepository,
        FinancingApplicationRepository financingRepository,
        AuditLogRepository auditLogRepository,
        PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.customerRepository = customerRepository;
        this.vehicleRepository = vehicleRepository;
        this.warrantyPlanRepository = warrantyPlanRepository;
        this.warrantyContractRepository = warrantyContractRepository;
        this.claimRepository = claimRepository;
        this.financingRepository = financingRepository;
        this.auditLogRepository = auditLogRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("Database already populated with initial seed data. Skipping initialization.");
            return;
        }

        log.info("Populating database with realistic automotive enterprise seed data...");

        // 1. Seed Users
        String encodedPass = passwordEncoder.encode("Password@123");

        User admin = new User("admin", encodedPass, "Alexander Wright", "admin@warrantywave.com", UserRole.ROLE_ADMIN);
        admin.setPhoneNumber("+1-555-0199");
        userRepository.save(admin);

        User claimsOfficer = new User("claims_officer", encodedPass, "Samantha Reed", "claims@warrantywave.com", UserRole.ROLE_CLAIMS_OFFICER);
        claimsOfficer.setPhoneNumber("+1-555-0142");
        userRepository.save(claimsOfficer);

        User financeOfficer = new User("finance_officer", encodedPass, "Marcus Chen", "finance@warrantywave.com", UserRole.ROLE_FINANCE_OFFICER);
        financeOfficer.setPhoneNumber("+1-555-0188");
        userRepository.save(financeOfficer);

        // 2. Seed Customers
        Customer c1 = new Customer();
        c1.setFirstName("David");
        c1.setLastName("Miller");
        c1.setEmail("david.miller@example.com");
        c1.setPhone("+1-555-234-5678");
        c1.setStreetAddress("742 Evergreen Terrace");
        c1.setCity("Austin");
        c1.setStateProvince("TX");
        c1.setPostalCode("78701");
        c1.setCreditScore(785);
        c1.setAnnualIncome(new BigDecimal("125000.00"));
        c1.setEmploymentStatus("EMPLOYED");
        customerRepository.save(c1);

        Customer c2 = new Customer();
        c2.setFirstName("Elena");
        c2.setLastName("Rostova");
        c2.setEmail("elena.rostova@example.com");
        c2.setPhone("+1-555-345-6789");
        c2.setStreetAddress("120 Ocean Drive");
        c2.setCity("Miami");
        c2.setStateProvince("FL");
        c2.setPostalCode("33139");
        c2.setCreditScore(720);
        c2.setAnnualIncome(new BigDecimal("98000.00"));
        c2.setEmploymentStatus("EMPLOYED");
        customerRepository.save(c2);

        Customer c3 = new Customer();
        c3.setFirstName("Robert");
        c3.setLastName("Johnson");
        c3.setEmail("robert.j@example.com");
        c3.setPhone("+1-555-456-7890");
        c3.setStreetAddress("458 Pine Street");
        c3.setCity("Seattle");
        c3.setStateProvince("WA");
        c3.setPostalCode("98101");
        c3.setCreditScore(645);
        c3.setAnnualIncome(new BigDecimal("62000.00"));
        c3.setEmploymentStatus("SELF_EMPLOYED");
        customerRepository.save(c3);

        Customer c4 = new Customer();
        c4.setFirstName("Sarah");
        c4.setLastName("Connor");
        c4.setEmail("sarah.connor@example.com");
        c4.setPhone("+1-555-567-8901");
        c4.setStreetAddress("89 Valley Road");
        c4.setCity("Denver");
        c4.setStateProvince("CO");
        c4.setPostalCode("80202");
        c4.setCreditScore(810);
        c4.setAnnualIncome(new BigDecimal("160000.00"));
        c4.setEmploymentStatus("EMPLOYED");
        customerRepository.save(c4);

        Customer c5 = new Customer();
        c5.setFirstName("Michael");
        c5.setLastName("Chang");
        c5.setEmail("mchang@example.com");
        c5.setPhone("+1-555-678-9012");
        c5.setStreetAddress("310 Michigan Ave");
        c5.setCity("Chicago");
        c5.setStateProvince("IL");
        c5.setPostalCode("60601");
        c5.setCreditScore(590);
        c5.setAnnualIncome(new BigDecimal("54000.00"));
        c5.setEmploymentStatus("EMPLOYED");
        customerRepository.save(c5);

        // 3. Seed Vehicles
        Vehicle v1 = new Vehicle();
        v1.setVin("1FTFW1ED5NFA12345");
        v1.setMake("Ford");
        v1.setModel("F-150 Lariat");
        v1.setModelYear(2022);
        v1.setMileage(28500);
        v1.setPurchasePrice(new BigDecimal("52000.00"));
        v1.setPurchaseDate(LocalDate.now().minusMonths(24));
        v1.setVehicleCondition(VehicleCondition.NEW);
        v1.setCustomer(c1);
        vehicleRepository.save(v1);

        Vehicle v2 = new Vehicle();
        v2.setVin("5YJ3E1EB7NF987654");
        v2.setMake("Tesla");
        v2.setModel("Model 3 Long Range");
        v2.setModelYear(2023);
        v2.setMileage(14200);
        v2.setPurchasePrice(new BigDecimal("47500.00"));
        v2.setPurchaseDate(LocalDate.now().minusMonths(14));
        v2.setVehicleCondition(VehicleCondition.NEW);
        v2.setCustomer(c2);
        vehicleRepository.save(v2);

        Vehicle v3 = new Vehicle();
        v3.setVin("2T3C1RFV8NW456789");
        v3.setMake("Toyota");
        v3.setModel("RAV4 Hybrid XSE");
        v3.setModelYear(2021);
        v3.setMileage(48900);
        v3.setPurchasePrice(new BigDecimal("34200.00"));
        v3.setPurchaseDate(LocalDate.now().minusMonths(36));
        v3.setVehicleCondition(VehicleCondition.CERTIFIED_PRE_OWNED);
        v3.setCustomer(c3);
        vehicleRepository.save(v3);

        Vehicle v4 = new Vehicle();
        v4.setVin("WBA5R1C50NFP34567");
        v4.setMake("BMW");
        v4.setModel("330i xDrive");
        v4.setModelYear(2023);
        v4.setMileage(19800);
        v4.setPurchasePrice(new BigDecimal("46000.00"));
        v4.setPurchaseDate(LocalDate.now().minusMonths(10));
        v4.setVehicleCondition(VehicleCondition.NEW);
        v4.setCustomer(c4);
        vehicleRepository.save(v4);

        Vehicle v5 = new Vehicle();
        v5.setVin("1G1ZE5ST4NF654321");
        v5.setMake("Chevrolet");
        v5.setModel("Malibu LT");
        v5.setModelYear(2019);
        v5.setMileage(78500);
        v5.setPurchasePrice(new BigDecimal("18500.00"));
        v5.setPurchaseDate(LocalDate.now().minusMonths(48));
        v5.setVehicleCondition(VehicleCondition.USED);
        v5.setCustomer(c5);
        vehicleRepository.save(v5);

        // 4. Seed Warranty Plans
        WarrantyPlan p1 = new WarrantyPlan();
        p1.setPlanCode("PLAN-PWR-01");
        p1.setPlanName("Powertrain Plus Shield");
        p1.setPlanType(PlanType.POWERTRAIN);
        p1.setDescription("Covers core engine assembly, transmission casing and internal parts, drive axles, and transfer case.");
        p1.setDurationMonths(36);
        p1.setMaxMileageCoverage(60000);
        p1.setDeductibleAmount(new BigDecimal("100.00"));
        p1.setBasePremium(new BigDecimal("899.00"));
        p1.setMaxClaimLimit(new BigDecimal("12000.00"));
        p1.setActive(true);
        warrantyPlanRepository.save(p1);

        WarrantyPlan p2 = new WarrantyPlan();
        p2.setPlanCode("PLAN-CMP-02");
        p2.setPlanName("Comprehensive Gold Guard");
        p2.setPlanType(PlanType.COMPREHENSIVE);
        p2.setDescription("Covers powertrain, AC & heating, steering, high-tech sensors, braking system, and fuel injection.");
        p2.setDurationMonths(48);
        p2.setMaxMileageCoverage(75000);
        p2.setDeductibleAmount(new BigDecimal("150.00"));
        p2.setBasePremium(new BigDecimal("1499.00"));
        p2.setMaxClaimLimit(new BigDecimal("20000.00"));
        p2.setActive(true);
        warrantyPlanRepository.save(p2);

        WarrantyPlan p3 = new WarrantyPlan();
        p3.setPlanCode("PLAN-B2B-03");
        p3.setPlanName("Bumper-to-Bumper Platinum");
        p3.setPlanType(PlanType.BUMPER_TO_BUMPER);
        p3.setDescription("All-inclusive coverage matching factory warranty including infotainment, electrical, and suspension.");
        p3.setDurationMonths(60);
        p3.setMaxMileageCoverage(100000);
        p3.setDeductibleAmount(new BigDecimal("50.00"));
        p3.setBasePremium(new BigDecimal("2299.00"));
        p3.setMaxClaimLimit(new BigDecimal("35000.00"));
        p3.setActive(true);
        warrantyPlanRepository.save(p3);

        WarrantyPlan p4 = new WarrantyPlan();
        p4.setPlanCode("PLAN-EV-04");
        p4.setPlanName("EV Volt & Drivetrain Shield");
        p4.setPlanType(PlanType.EV_DRIVETRAIN);
        p4.setDescription("Dedicated coverage for electric vehicle battery pack, drive motors, inverter, and thermal cooling loops.");
        p4.setDurationMonths(60);
        p4.setMaxMileageCoverage(120000);
        p4.setDeductibleAmount(new BigDecimal("100.00"));
        p4.setBasePremium(new BigDecimal("1899.00"));
        p4.setMaxClaimLimit(new BigDecimal("25000.00"));
        p4.setActive(true);
        warrantyPlanRepository.save(p4);

        // 5. Seed Warranty Contracts
        WarrantyContract cnt1 = new WarrantyContract();
        cnt1.setContractNumber("WC-2024-001");
        cnt1.setCustomer(c1);
        cnt1.setVehicle(v1);
        cnt1.setPlan(p2);
        cnt1.setStartDate(LocalDate.now().minusMonths(6));
        cnt1.setEndDate(LocalDate.now().plusMonths(42));
        cnt1.setStartMileage(20000);
        cnt1.setMaxMileage(95000);
        cnt1.setTotalPremium(new BigDecimal("1499.00"));
        cnt1.setMonthlyPremium(new BigDecimal("31.22"));
        cnt1.setStatus(ContractStatus.ACTIVE);
        cnt1.setRenewalCount(0);
        cnt1.setNotes("Standard Gold Plan on Ford F-150");
        warrantyContractRepository.save(cnt1);

        WarrantyContract cnt2 = new WarrantyContract();
        cnt2.setContractNumber("WC-2024-002");
        cnt2.setCustomer(c2);
        cnt2.setVehicle(v2);
        cnt2.setPlan(p4);
        cnt2.setStartDate(LocalDate.now().minusMonths(4));
        cnt2.setEndDate(LocalDate.now().plusMonths(56));
        cnt2.setStartMileage(10000);
        cnt2.setMaxMileage(130000);
        cnt2.setTotalPremium(new BigDecimal("1899.00"));
        cnt2.setMonthlyPremium(new BigDecimal("31.65"));
        cnt2.setStatus(ContractStatus.ACTIVE);
        cnt2.setRenewalCount(0);
        cnt2.setNotes("EV Volt Warranty on Tesla Model 3");
        warrantyContractRepository.save(cnt2);

        WarrantyContract cnt3 = new WarrantyContract();
        cnt3.setContractNumber("WC-2023-003");
        cnt3.setCustomer(c3);
        cnt3.setVehicle(v3);
        cnt3.setPlan(p1);
        cnt3.setStartDate(LocalDate.now().minusMonths(15));
        cnt3.setEndDate(LocalDate.now().plusMonths(21));
        cnt3.setStartMileage(35000);
        cnt3.setMaxMileage(95000);
        cnt3.setTotalPremium(new BigDecimal("899.00"));
        cnt3.setMonthlyPremium(new BigDecimal("24.97"));
        cnt3.setStatus(ContractStatus.ACTIVE);
        cnt3.setRenewalCount(1);
        cnt3.setNotes("Renewed Powertrain plan for Toyota RAV4");
        warrantyContractRepository.save(cnt3);

        WarrantyContract cnt4 = new WarrantyContract();
        cnt4.setContractNumber("WC-2024-004");
        cnt4.setCustomer(c4);
        cnt4.setVehicle(v4);
        cnt4.setPlan(p3);
        cnt4.setStartDate(LocalDate.now().minusMonths(2));
        cnt4.setEndDate(LocalDate.now().plusMonths(58));
        cnt4.setStartMileage(15000);
        cnt4.setMaxMileage(115000);
        cnt4.setTotalPremium(new BigDecimal("2299.00"));
        cnt4.setMonthlyPremium(new BigDecimal("38.31"));
        cnt4.setStatus(ContractStatus.ACTIVE);
        cnt4.setRenewalCount(0);
        cnt4.setNotes("Platinum Plan on BMW 330i");
        warrantyContractRepository.save(cnt4);

        // 6. Seed Claims
        Claim clm1 = new Claim();
        clm1.setClaimNumber("CLM-2024-101");
        clm1.setContract(cnt1);
        clm1.setCustomer(c1);
        clm1.setVehicle(v1);
        clm1.setIncidentDate(LocalDate.now().minusDays(45));
        clm1.setCurrentMileage(28400);
        clm1.setDescription("Alternator failure causing battery drain and engine stall on highway.");
        clm1.setRepairShopName("Lone Star Ford Certified Service");
        clm1.setRepairEstimate(new BigDecimal("1250.00"));
        clm1.setApprovedAmount(new BigDecimal("1100.00"));
        clm1.setDeductiblePaid(new BigDecimal("150.00"));
        clm1.setStatus(ClaimStatus.APPROVED);
        clm1.setOfficerNotes("Diagnostic verified by master technician. Covered under Gold electrical section.");
        clm1.setReviewer(claimsOfficer);
        claimRepository.save(clm1);

        Claim clm2 = new Claim();
        clm2.setClaimNumber("CLM-2024-102");
        clm2.setContract(cnt2);
        clm2.setCustomer(c2);
        clm2.setVehicle(v2);
        clm2.setIncidentDate(LocalDate.now().minusDays(20));
        clm2.setCurrentMileage(14100);
        clm2.setDescription("HVAC heat pump refrigerant valve stuck open causing loss of cabin climate control.");
        clm2.setRepairShopName("Tesla Service Center Miami South");
        clm2.setRepairEstimate(new BigDecimal("880.00"));
        clm2.setApprovedAmount(new BigDecimal("780.00"));
        clm2.setDeductiblePaid(new BigDecimal("100.00"));
        clm2.setStatus(ClaimStatus.PAID);
        clm2.setOfficerNotes("Thermal valve replacement covered under EV Drivetrain auxiliary systems.");
        clm2.setReviewer(claimsOfficer);
        claimRepository.save(clm2);

        Claim clm3 = new Claim();
        clm3.setClaimNumber("CLM-2024-103");
        clm3.setContract(cnt3);
        clm3.setCustomer(c3);
        clm3.setVehicle(v3);
        clm3.setIncidentDate(LocalDate.now().minusDays(5));
        clm3.setCurrentMileage(48500);
        clm3.setDescription("Transmission shudder during gear shift between 2nd and 3rd gear.");
        clm3.setRepairShopName("Toyota Precision Motors Seattle");
        clm3.setRepairEstimate(new BigDecimal("2400.00"));
        clm3.setStatus(ClaimStatus.UNDER_REVIEW);
        clm3.setOfficerNotes("Awaiting torque converter fluid pressure diagnostic telemetry.");
        clm3.setReviewer(claimsOfficer);
        claimRepository.save(clm3);

        Claim clm4 = new Claim();
        clm4.setClaimNumber("CLM-2024-104");
        clm4.setContract(cnt1);
        clm4.setCustomer(c1);
        clm4.setVehicle(v1);
        clm4.setIncidentDate(LocalDate.now().minusDays(90));
        clm4.setCurrentMileage(24100);
        clm4.setDescription("Brake pads worn down below 2mm with rotor scoring.");
        clm4.setRepairShopName("QuickFit Brake & Tire Austin");
        clm4.setRepairEstimate(new BigDecimal("620.00"));
        clm4.setStatus(ClaimStatus.REJECTED);
        clm4.setRejectionReason("Normal wear and tear consumable components (brake pads/rotors) are excluded under Section 4.2.");
        clm4.setOfficerNotes("Wear item exclusion applies.");
        clm4.setReviewer(claimsOfficer);
        claimRepository.save(clm4);

        // 7. Seed Financing Applications
        FinancingApplication f1 = new FinancingApplication();
        f1.setApplicationNumber("FIN-2024-501");
        f1.setCustomer(c1);
        f1.setVehicle(v1);
        f1.setVehiclePrice(new BigDecimal("52000.00"));
        f1.setDownPayment(new BigDecimal("10000.00"));
        f1.setLoanAmount(new BigDecimal("42000.00"));
        f1.setLoanTermMonths(60);
        f1.setInterestRate(new BigDecimal("5.25"));
        f1.setCalculatedEmi(new BigDecimal("797.16"));
        f1.setStatus(FinancingStatus.APPROVED);
        f1.setMonthlyIncome(new BigDecimal("10416.67"));
        f1.setDebtToIncomeRatio(new BigDecimal("18.5"));
        f1.setCreditTier(CreditTier.TIER_1_EXCELLENT);
        f1.setOfficerNotes("Low DTI, Tier 1 credit score of 785. Approved at prime rate.");
        f1.setReviewer(financeOfficer);
        financingRepository.save(f1);

        FinancingApplication f2 = new FinancingApplication();
        f2.setApplicationNumber("FIN-2024-502");
        f2.setCustomer(c2);
        f2.setVehicle(v2);
        f2.setVehiclePrice(new BigDecimal("47500.00"));
        f2.setDownPayment(new BigDecimal("7500.00"));
        f2.setLoanAmount(new BigDecimal("40000.00"));
        f2.setLoanTermMonths(48);
        f2.setInterestRate(new BigDecimal("5.75"));
        f2.setCalculatedEmi(new BigDecimal("934.82"));
        f2.setStatus(FinancingStatus.APPROVED);
        f2.setMonthlyIncome(new BigDecimal("8166.67"));
        f2.setDebtToIncomeRatio(new BigDecimal("24.2"));
        f2.setCreditTier(CreditTier.TIER_2_GOOD);
        f2.setOfficerNotes("Consistent salary, solid credit score 720. Approved.");
        f2.setReviewer(financeOfficer);
        financingRepository.save(f2);

        FinancingApplication f3 = new FinancingApplication();
        f3.setApplicationNumber("FIN-2024-503");
        f3.setCustomer(c3);
        f3.setVehicle(v3);
        f3.setVehiclePrice(new BigDecimal("34200.00"));
        f3.setDownPayment(new BigDecimal("4200.00"));
        f3.setLoanAmount(new BigDecimal("30000.00"));
        f3.setLoanTermMonths(60);
        f3.setInterestRate(new BigDecimal("8.49"));
        f3.setCalculatedEmi(new BigDecimal("615.34"));
        f3.setStatus(FinancingStatus.UNDER_REVIEW);
        f3.setMonthlyIncome(new BigDecimal("5166.67"));
        f3.setDebtToIncomeRatio(new BigDecimal("36.8"));
        f3.setCreditTier(CreditTier.TIER_3_FAIR);
        f3.setOfficerNotes("DTI is 36.8%, requires tax return verification for self-employed applicant.");
        f3.setReviewer(financeOfficer);
        financingRepository.save(f3);

        FinancingApplication f4 = new FinancingApplication();
        f4.setApplicationNumber("FIN-2024-504");
        f4.setCustomer(c5);
        f4.setVehicle(v5);
        f4.setVehiclePrice(new BigDecimal("18500.00"));
        f4.setDownPayment(new BigDecimal("1500.00"));
        f4.setLoanAmount(new BigDecimal("17000.00"));
        f4.setLoanTermMonths(36);
        f4.setInterestRate(new BigDecimal("14.50"));
        f4.setCalculatedEmi(new BigDecimal("584.85"));
        f4.setStatus(FinancingStatus.REJECTED);
        f4.setMonthlyIncome(new BigDecimal("4500.00"));
        f4.setDebtToIncomeRatio(new BigDecimal("48.5"));
        f4.setCreditTier(CreditTier.HIGH_RISK);
        f4.setOfficerNotes("Credit score 590, high DTI above allowable risk limits without cosigner.");
        f4.setReviewer(financeOfficer);
        financingRepository.save(f4);

        // 8. Seed Audit Logs
        auditLogRepository.save(new AuditLog("FINANCING_APPLICATIONS", f1.getId(), "APPROVE", "finance_officer", "Approved 60-month loan at 5.25% APR for customer David Miller"));
        auditLogRepository.save(new AuditLog("CLAIMS", clm1.getId(), "APPROVE", "claims_officer", "Approved alternator repair claim CLM-2024-101 for $1,100.00"));
        auditLogRepository.save(new AuditLog("CLAIMS", clm4.getId(), "REJECT", "claims_officer", "Rejected brake pads claim CLM-2024-104 due to wear item exclusion clause"));

        log.info("WarrantyWave database seed data successfully initialized.");
    }
}
