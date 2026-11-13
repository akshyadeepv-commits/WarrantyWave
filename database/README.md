# WarrantyWave - Oracle Database Setup Guide

This directory contains the database definitions and seed records for the **WarrantyWave** Enterprise Automotive Warranty & Financing Platform.

## Files
- `schema-oracle.sql`: Full DDL schema with sequences, identity columns, foreign keys, check constraints, and performance indexes.
- `seed-data-oracle.sql`: Production-grade sample data with demo users, vehicles, customers, warranty plans, active contracts, claims, and financing applications.

---

## 1. Quick Local Setup using Oracle Database Free (Docker)

If you don't already have an Oracle Database instance running locally, you can start one in seconds using Oracle's official container image:

```bash
docker run -d \
  --name warrantywave-oracle \
  -p 1521:1521 \
  -e ORACLE_PASSWORD=YourStrongPassword123! \
  gvenzl/oracle-free:latest
```

Wait until the container is healthy:
```bash
docker logs -f warrantywave-oracle
# Once you see "DATABASE IS READY TO USE!", proceed.
```

---

## 2. Execute Schema & Seed Scripts

### Using SQLcl or SQL*Plus:
```bash
sqlplus SYSTEM/YourStrongPassword123!@localhost:1521/FREEPDB1

-- Create dedicated app user
CREATE USER WARRANTYWAVE IDENTIFIED BY "SecureAppPass2026!" DEFAULT TABLESPACE USERS QUOTA UNLIMITED ON USERS;
GRANT CONNECT, RESOURCE, CREATE VIEW, CREATE SEQUENCE TO WARRANTYWAVE;

-- Connect as the app user
CONNECT WARRANTYWAVE/"SecureAppPass2026!"@localhost:1521/FREEPDB1;

-- Run schema and seed scripts
@schema-oracle.sql
@seed-data-oracle.sql
```

---

## 3. Configure Spring Boot Application

Set your environment variables (or update `.env` / `application-oracle.properties`):

| Variable | Default Example | Description |
|---|---|---|
| `SPRING_PROFILES_ACTIVE` | `oracle` | Activates Oracle profile |
| `DB_URL` | `jdbc:oracle:thin:@localhost:1521/FREEPDB1` | Oracle JDBC connection URL |
| `DB_USERNAME` | `WARRANTYWAVE` | Database username |
| `DB_PASSWORD` | `SecureAppPass2026!` | Database password |
| `DB_DRIVER` | `oracle.jdbc.OracleDriver` | Oracle JDBC Driver |
| `JPA_DIALECT` | `org.hibernate.dialect.OracleDialect` | Hibernate Oracle Dialect |

---

## 4. Default Demo Accounts
All seed accounts use the default password: **`Password@123`**

| Username | Role | Purpose |
|---|---|---|
| `admin` | `ROLE_ADMIN` | Full administrative control, system metrics, users |
| `claims_officer` | `ROLE_CLAIMS_OFFICER` | Reviews, validates, approves/rejects warranty claims |
| `finance_officer` | `ROLE_FINANCE_OFFICER` | Assesses credit risk, calculates EMI, reviews financing |
