# WarrantyWave — Automotive Warranty & Financing Platform

**WarrantyWave** is an enterprise-grade, full-stack platform engineered for automotive vehicle warranty policy management, claims adjudication, auto loan/lease financing, and credit risk assessment.

---

## 🏗️ Architecture & Technology Stack

- **Backend**: Java 21, Spring Boot 3.3.4, Maven, Spring Data JPA, Hibernate, Spring Security (Stateless JWT), OpenAPI/Swagger UI.
- **Frontend**: Angular 22+, TypeScript, Reactive Signals, Standalone Components, Custom Responsive Automotive Enterprise CSS.
- **Database**: Oracle Database (19c, 21c, and 23c/23ai compatible). Includes explicit DDL schemas, constraints, sequences, and seed data.
- **Testing**: JUnit 5, Mockito, Spring Security Test, Angular Testing framework.

---

## 🚗 Core Functional Modules

### 1. Role-Based Access & Authentication
- **Roles**:
  - `ROLE_ADMIN`: Global management, audit compliance log inspection, policy catalog editing.
  - `ROLE_CLAIMS_OFFICER`: Technical adjudication of mechanical repair estimates, deductible calculation, and payout disbursement.
  - `ROLE_FINANCE_OFFICER`: Credit underwriting, debt-to-income (DTI) evaluation, APR adjustment, and auto loan authorization.
- Stateless authentication using HMAC-SHA256 JWT tokens.

### 2. Customer & Fleet Management
- Comprehensive customer profile tracking including FICO credit score, employment status, and annual income.
- Fleet vehicle registry with 17-character ISO 3779 VIN validation, odometer tracking, purchase pricing, and condition tags (`NEW`, `CERTIFIED_PRE_OWNED`, `USED`).

### 3. Warranty Plans & Telematics Eligibility Engine
- Protection tiers: **Powertrain Plus Shield**, **Comprehensive Gold Guard**, **Bumper-to-Bumper Platinum**, and **EV Volt & Drivetrain Shield**.
- Configurable deductibles, duration months, mileage limits, and maximum payout caps.
- **Interactive Eligibility Verification**: Pre-validates vehicle age against policy requirements (e.g. 7-12 years) and current odometer mileage against plan thresholds before issuing policies.

### 4. Financing & Leasing Underwriting
- Auto loan underwriting engine computing monthly Debt-to-Income (DTI) ratios.
- Risk-adjusted credit tier categorizations:
  - **Tier 1 (Prime 750+)**: 5.25% APR
  - **Tier 2 (Good 670–749)**: 6.49% APR
  - **Tier 3 (Fair 580–669)**: 9.99% APR
  - **High Risk (<580)**: 15.49% APR
- Amortized monthly payment formula:
  $$EMI = \frac{P \times r \times (1 + r)^n}{(1 + r)^n - 1}$$
- Interactive EMI Calculator page with dynamic sliders for purchase price, down payment, term, and credit score.

### 5. Contract Lifecycle Servicing & Renewals
- Automated policy contract issuance linked to customer and vehicle VIN.
- In-flight policy servicing and contract renewal workflows extending coverage periods and mileage thresholds with automated premium recalculation.

### 6. Executive Operations Dashboard & Compliance Audit
- Real-time business intelligence metrics: Active Contracts, Financing Volume, Claims Paid, Monthly Premium ARR, and **Claims Loss Ratio %** with automated underwriting health indicators.
- Live warranty product portfolio distribution breakdown.
- Audit compliance journal capturing all critical administrative transactions and state mutations.

---

## 🚀 Step-by-Step Setup and Execution

### Prerequisites
- **JDK 21** (`java -version`)
- **Maven 3.9+** or included Maven Wrapper (`mvnw` / `mvnw.cmd`)
- **Node.js 20+** and **npm** (`node -v`, `npm -v`)
- **Oracle Database** (Docker container or local/cloud instance)

---

### Step 1: Database Setup (Oracle Database)

If running Oracle Database Free via Docker:
```bash
docker run -d \
  --name warrantywave-oracle \
  -p 1521:1521 \
  -e ORACLE_PASSWORD=SecureAppPass2026! \
  gvenzl/oracle-free:latest
```

Execute the Oracle DDL schema and realistic seed data:
```bash
cd database
sqlplus SYSTEM/SecureAppPass2026!@localhost:1521/FREEPDB1 @schema-oracle.sql
sqlplus SYSTEM/SecureAppPass2026!@localhost:1521/FREEPDB1 @seed-data-oracle.sql
```

> **Note on Local Dev Profile**: If an Oracle Database instance is not immediately available, the backend automatically falls back to the `dev` profile using in-memory H2 with Oracle compatibility mode and automatically loads the full sample dataset via `DataInitializer`.

---

### Step 2: Configure and Run Backend (Spring Boot)

1. Open a terminal in `backend/`:
```bash
cd backend
```

2. (Optional) Set environment variables for Oracle:
```bash
# Windows PowerShell
$env:SPRING_PROFILES_ACTIVE = "oracle"
$env:DB_URL = "jdbc:oracle:thin:@localhost:1521/FREEPDB1"
$env:DB_USERNAME = "WARRANTYWAVE"
$env:DB_PASSWORD = "SecureAppPass2026!"

# Linux / macOS
export SPRING_PROFILES_ACTIVE=oracle
export DB_URL=jdbc:oracle:thin:@localhost:1521/FREEPDB1
export DB_USERNAME=WARRANTYWAVE
export DB_PASSWORD=SecureAppPass2026!
```

3. Run the automated test suite:
```bash
./mvnw clean test
# Or on Windows:
.\mvnw.cmd clean test
```

4. Launch the Spring Boot application:
```bash
./mvnw spring-boot:run
# Or on Windows:
.\mvnw.cmd spring-boot:run
```

The backend starts at: **`http://localhost:8080`**  
OpenAPI / Swagger UI is available at: **`http://localhost:8080/swagger-ui.html`**

---

### Step 3: Configure and Run Frontend (Angular)

1. Open a terminal in `frontend/`:
```bash
cd frontend
npm install
```

2. Run frontend unit tests:
```bash
npm test
```

3. Build production bundle:
```bash
npm run build
```

4. Start the Angular development server:
```bash
npm start
```

The frontend application runs at: **`http://localhost:4200`**

---

## 🔑 Default Demo Accounts

All pre-seeded demo accounts share the password: **`Password@123`**

| Role | Username | Password | Full Name | Primary Actions |
|---|---|---|---|---|
| **System Admin** | `admin` | `Password@123` | Alexander Wright | Audit logs, warranty plan definitions, full access |
| **Claims Officer** | `claims_officer` | `Password@123` | Samantha Reed | Review repair estimates, apply deductibles, authorize payouts |
| **Finance Officer** | `finance_officer` | `Password@123` | Marcus Chen | Underwrite auto loans, evaluate DTI & credit risk, adjust APR |

---

## 🧪 Automated Testing Summary

- **Backend**:
  - `AuthServiceTest`: JWT token generation and credential validation.
  - `WarrantyContractServiceTest`: Telematics eligibility engine, age/mileage validations.
  - `FinancingServiceTest`: Credit score tier evaluation and amortized EMI math accuracy.
  - `ClaimServiceTest`: Adjudication, deductible calculation, and claim coverage limit capping.
- **Frontend**:
  - Component lifecycle and routing unit tests.
  - `AuthService`: Token persistence and reactive session signal tests.
