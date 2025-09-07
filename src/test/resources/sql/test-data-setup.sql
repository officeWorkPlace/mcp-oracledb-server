-- ============================================================================
-- Oracle MCP Server - Bank Loan Department Test Data Setup Script
-- ============================================================================
-- This script creates comprehensive bank loan department data for all Oracle MCP tools
-- Covers: Loan management, Customer data, Risk analytics, Performance metrics, Security
-- Banking Domain: Personal loans, mortgages, business loans, credit scoring
-- ============================================================================

-- Clean up any existing bank test objects
BEGIN
    FOR c IN (SELECT table_name FROM user_tables WHERE table_name LIKE 'BANK_%') LOOP
        EXECUTE IMMEDIATE 'DROP TABLE ' || c.table_name || ' CASCADE CONSTRAINTS';
    END LOOP;
END;
/

-- ============================================================================
-- 1. CORE BANKING TABLES
-- ============================================================================

-- Bank branches table
CREATE TABLE BANK_BRANCHES (
    branch_id NUMBER(10) PRIMARY KEY,
    branch_code VARCHAR2(20) UNIQUE NOT NULL,
    branch_name VARCHAR2(100) NOT NULL,
    address VARCHAR2(200),
    city VARCHAR2(50),
    state VARCHAR2(50),
    postal_code VARCHAR2(20),
    phone VARCHAR2(20),
    manager_name VARCHAR2(100),
    region VARCHAR2(50),
    established_date DATE,
    total_loans_ytd NUMBER(15,2) DEFAULT 0,
    total_deposits NUMBER(15,2) DEFAULT 0,
    status VARCHAR2(20) DEFAULT 'ACTIVE'
);

-- Bank employees table (loan officers, managers, analysts)
CREATE TABLE BANK_EMPLOYEES (
    employee_id NUMBER(10) PRIMARY KEY,
    employee_code VARCHAR2(20) UNIQUE NOT NULL,
    first_name VARCHAR2(50) NOT NULL,
    last_name VARCHAR2(50) NOT NULL,
    email VARCHAR2(100) UNIQUE,
    phone VARCHAR2(20),
    job_title VARCHAR2(100),
    department VARCHAR2(50),
    branch_id NUMBER(10),
    manager_id NUMBER(10),
    hire_date DATE NOT NULL,
    salary NUMBER(10,2),
    commission_rate NUMBER(5,4),
    performance_rating VARCHAR2(20),
    loan_approval_limit NUMBER(15,2),
    employee_status VARCHAR2(20) DEFAULT 'ACTIVE',
    security_clearance VARCHAR2(20),
    ssn VARCHAR2(11), -- For security/redaction testing
    CONSTRAINT fk_emp_branch FOREIGN KEY (branch_id) REFERENCES BANK_BRANCHES(branch_id),
    CONSTRAINT fk_emp_manager FOREIGN KEY (manager_id) REFERENCES BANK_EMPLOYEES(employee_id)
);

-- Bank customers table
CREATE TABLE BANK_CUSTOMERS (
    customer_id NUMBER(10) PRIMARY KEY,
    customer_number VARCHAR2(20) UNIQUE NOT NULL,
    customer_type VARCHAR2(20) DEFAULT 'INDIVIDUAL', -- INDIVIDUAL, BUSINESS
    first_name VARCHAR2(50),
    last_name VARCHAR2(50),
    business_name VARCHAR2(100),
    date_of_birth DATE,
    ssn VARCHAR2(11), -- For redaction testing
    tax_id VARCHAR2(20),
    email VARCHAR2(100),
    phone VARCHAR2(20),
    address VARCHAR2(200),
    city VARCHAR2(50),
    state VARCHAR2(50),
    postal_code VARCHAR2(20),
    annual_income NUMBER(15,2),
    employment_status VARCHAR2(30),
    employer_name VARCHAR2(100),
    employment_years NUMBER(3),
    credit_score NUMBER(3),
    bank_relationship_start DATE,
    primary_branch_id NUMBER(10),
    customer_status VARCHAR2(20) DEFAULT 'ACTIVE',
    risk_category VARCHAR2(20),
    total_deposits NUMBER(15,2) DEFAULT 0,
    CONSTRAINT fk_cust_branch FOREIGN KEY (primary_branch_id) REFERENCES BANK_BRANCHES(branch_id)
);

-- Loan products table
CREATE TABLE BANK_LOAN_PRODUCTS (
    product_id NUMBER(10) PRIMARY KEY,
    product_code VARCHAR2(20) UNIQUE NOT NULL,
    product_name VARCHAR2(100) NOT NULL,
    loan_type VARCHAR2(50), -- PERSONAL, MORTGAGE, BUSINESS, AUTO, EDUCATION
    description CLOB,
    min_amount NUMBER(15,2),
    max_amount NUMBER(15,2),
    min_term_months NUMBER(3),
    max_term_months NUMBER(3),
    base_interest_rate NUMBER(5,4),
    min_credit_score NUMBER(3),
    max_ltv_ratio NUMBER(5,4), -- Loan to Value ratio
    processing_fee_rate NUMBER(5,4),
    prepayment_penalty_rate NUMBER(5,4),
    product_status VARCHAR2(20) DEFAULT 'ACTIVE',
    launch_date DATE,
    discontinued_date DATE
);

-- Loan applications table
CREATE TABLE BANK_LOAN_APPLICATIONS (
    application_id NUMBER(10) PRIMARY KEY,
    application_number VARCHAR2(30) UNIQUE NOT NULL,
    customer_id NUMBER(10) NOT NULL,
    product_id NUMBER(10) NOT NULL,
    loan_officer_id NUMBER(10),
    branch_id NUMBER(10),
    application_date DATE DEFAULT SYSDATE,
    requested_amount NUMBER(15,2),
    requested_term_months NUMBER(3),
    purpose VARCHAR2(100),
    collateral_type VARCHAR2(50),
    collateral_value NUMBER(15,2),
    application_status VARCHAR2(30) DEFAULT 'PENDING',
    credit_score_at_application NUMBER(3),
    debt_to_income_ratio NUMBER(5,4),
    ltv_ratio NUMBER(5,4),
    processing_notes CLOB,
    decision_date DATE,
    approved_amount NUMBER(15,2),
    approved_rate NUMBER(5,4),
    approved_term_months NUMBER(3),
    rejection_reason VARCHAR2(200),
    CONSTRAINT fk_app_customer FOREIGN KEY (customer_id) REFERENCES BANK_CUSTOMERS(customer_id),
    CONSTRAINT fk_app_product FOREIGN KEY (product_id) REFERENCES BANK_LOAN_PRODUCTS(product_id),
    CONSTRAINT fk_app_officer FOREIGN KEY (loan_officer_id) REFERENCES BANK_EMPLOYEES(employee_id),
    CONSTRAINT fk_app_branch FOREIGN KEY (branch_id) REFERENCES BANK_BRANCHES(branch_id)
);

-- Active loans table
CREATE TABLE BANK_LOANS (
    loan_id NUMBER(10) PRIMARY KEY,
    loan_number VARCHAR2(30) UNIQUE NOT NULL,
    application_id NUMBER(10) NOT NULL,
    customer_id NUMBER(10) NOT NULL,
    product_id NUMBER(10) NOT NULL,
    loan_officer_id NUMBER(10),
    branch_id NUMBER(10),
    origination_date DATE DEFAULT SYSDATE,
    principal_amount NUMBER(15,2),
    interest_rate NUMBER(5,4),
    term_months NUMBER(3),
    monthly_payment NUMBER(10,2),
    current_balance NUMBER(15,2),
    payments_made NUMBER(3) DEFAULT 0,
    last_payment_date DATE,
    last_payment_amount NUMBER(10,2),
    next_due_date DATE,
    loan_status VARCHAR2(30) DEFAULT 'ACTIVE',
    maturity_date DATE,
    collateral_description VARCHAR2(200),
    insurance_required VARCHAR2(1) DEFAULT 'N',
    escrow_required VARCHAR2(1) DEFAULT 'N',
    delinquency_status VARCHAR2(30),
    days_past_due NUMBER(3) DEFAULT 0,
    total_interest_paid NUMBER(15,2) DEFAULT 0,
    CONSTRAINT fk_loan_application FOREIGN KEY (application_id) REFERENCES BANK_LOAN_APPLICATIONS(application_id),
    CONSTRAINT fk_loan_customer FOREIGN KEY (customer_id) REFERENCES BANK_CUSTOMERS(customer_id),
    CONSTRAINT fk_loan_product FOREIGN KEY (product_id) REFERENCES BANK_LOAN_PRODUCTS(product_id),
    CONSTRAINT fk_loan_officer FOREIGN KEY (loan_officer_id) REFERENCES BANK_EMPLOYEES(employee_id),
    CONSTRAINT fk_loan_branch FOREIGN KEY (branch_id) REFERENCES BANK_BRANCHES(branch_id)
);

-- Loan payments table
CREATE TABLE BANK_LOAN_PAYMENTS (
    payment_id NUMBER(10) PRIMARY KEY,
    loan_id NUMBER(10) NOT NULL,
    payment_date DATE DEFAULT SYSDATE,
    payment_amount NUMBER(10,2),
    principal_amount NUMBER(10,2),
    interest_amount NUMBER(10,2),
    escrow_amount NUMBER(10,2) DEFAULT 0,
    late_fee NUMBER(8,2) DEFAULT 0,
    payment_method VARCHAR2(30),
    transaction_reference VARCHAR2(50),
    processed_by_employee_id NUMBER(10),
    payment_status VARCHAR2(20) DEFAULT 'COMPLETED',
    remaining_balance NUMBER(15,2),
    CONSTRAINT fk_payment_loan FOREIGN KEY (loan_id) REFERENCES BANK_LOANS(loan_id),
    CONSTRAINT fk_payment_employee FOREIGN KEY (processed_by_employee_id) REFERENCES BANK_EMPLOYEES(employee_id)
);

-- ============================================================================
-- 2. ANALYTICS AND REPORTING TABLES
-- ============================================================================

-- Credit scoring factors table
CREATE TABLE BANK_CREDIT_FACTORS (
    factor_id NUMBER(10) PRIMARY KEY,
    customer_id NUMBER(10) NOT NULL,
    evaluation_date DATE DEFAULT SYSDATE,
    payment_history_score NUMBER(3),
    credit_utilization_ratio NUMBER(5,4),
    length_of_credit_history NUMBER(4), -- in months
    types_of_credit_used NUMBER(2),
    new_credit_inquiries NUMBER(3),
    total_debt NUMBER(15,2),
    debt_to_income_ratio NUMBER(5,4),
    employment_stability_score NUMBER(3),
    collateral_value NUMBER(15,2),
    final_credit_score NUMBER(3),
    risk_grade VARCHAR2(5),
    CONSTRAINT fk_credit_customer FOREIGN KEY (customer_id) REFERENCES BANK_CUSTOMERS(customer_id)
);

-- Loan performance metrics table
CREATE TABLE BANK_LOAN_METRICS (
    metric_id NUMBER(10) PRIMARY KEY,
    reporting_date DATE NOT NULL,
    branch_id NUMBER(10),
    product_id NUMBER(10),
    total_applications NUMBER(8),
    approved_applications NUMBER(8),
    rejected_applications NUMBER(8),
    approval_rate NUMBER(5,4),
    average_loan_amount NUMBER(15,2),
    total_loans_originated NUMBER(15,2),
    current_portfolio_balance NUMBER(15,2),
    delinquent_loans_count NUMBER(8),
    delinquency_rate NUMBER(5,4),
    charge_offs_amount NUMBER(15,2),
    net_charge_off_rate NUMBER(5,4),
    average_credit_score NUMBER(5,2),
    CONSTRAINT fk_metrics_branch FOREIGN KEY (branch_id) REFERENCES BANK_BRANCHES(branch_id),
    CONSTRAINT fk_metrics_product FOREIGN KEY (product_id) REFERENCES BANK_LOAN_PRODUCTS(product_id)
);

-- Market interest rates for time series analysis
CREATE TABLE BANK_MARKET_RATES (
    rate_id NUMBER(10) PRIMARY KEY,
    rate_date DATE NOT NULL,
    rate_type VARCHAR2(50), -- PRIME, LIBOR, TREASURY_10Y, MORTGAGE_30Y, etc.
    rate_value NUMBER(5,4),
    basis_points_change NUMBER(6,2),
    source VARCHAR2(50),
    region VARCHAR2(50) DEFAULT 'US'
);

-- ============================================================================
-- 3. POPULATE BANK TEST DATA
-- ============================================================================

-- Insert bank branches
INSERT ALL
  INTO BANK_BRANCHES VALUES (1, 'BR001', 'Downtown Main Branch', '123 Main Street', 'New York', 'NY', '10001', '555-0100', 'Sarah Johnson', 'Northeast', DATE '2010-01-15', 45000000, 125000000, 'ACTIVE')
  INTO BANK_BRANCHES VALUES (2, 'BR002', 'Midtown Business Center', '456 Business Ave', 'New York', 'NY', '10016', '555-0200', 'Michael Chen', 'Northeast', DATE '2012-06-20', 38000000, 98000000, 'ACTIVE')
  INTO BANK_BRANCHES VALUES (3, 'BR003', 'Silicon Valley Branch', '789 Tech Drive', 'San Jose', 'CA', '95110', '555-0300', 'Jennifer Lopez', 'West', DATE '2015-03-10', 52000000, 145000000, 'ACTIVE')
  INTO BANK_BRANCHES VALUES (4, 'BR004', 'Chicago Loop Branch', '321 LaSalle Street', 'Chicago', 'IL', '60601', '555-0400', 'Robert Williams', 'Midwest', DATE '2008-09-01', 41000000, 112000000, 'ACTIVE')
  INTO BANK_BRANCHES VALUES (5, 'BR005', 'Austin Growth Branch', '654 Congress Ave', 'Austin', 'TX', '73301', '555-0500', 'Maria Garcia', 'South', DATE '2018-11-15', 29000000, 78000000, 'ACTIVE')
SELECT * FROM dual;

-- Insert bank employees
INSERT ALL
  INTO BANK_EMPLOYEES VALUES (1, 'EMP001', 'Sarah', 'Johnson', 'sarah.johnson@bank.com', '555-1001', 'Branch Manager', 'Management', 1, NULL, DATE '2010-02-01', 125000, 0.02, 'EXCELLENT', 2000000, 'ACTIVE', 'MANAGER', '123-45-6789')
  INTO BANK_EMPLOYEES VALUES (2, 'EMP002', 'Michael', 'Chen', 'michael.chen@bank.com', '555-1002', 'Senior Loan Officer', 'Lending', 1, 1, DATE '2012-03-15', 85000, 0.015, 'EXCELLENT', 1000000, 'ACTIVE', 'SENIOR', '234-56-7890')
  INTO BANK_EMPLOYEES VALUES (3, 'EMP003', 'Jennifer', 'Lopez', 'jennifer.lopez@bank.com', '555-1003', 'Branch Manager', 'Management', 3, NULL, DATE '2015-04-01', 130000, 0.025, 'EXCELLENT', 2500000, 'ACTIVE', 'MANAGER', '345-67-8901')
  INTO BANK_EMPLOYEES VALUES (4, 'EMP004', 'Robert', 'Williams', 'robert.williams@bank.com', '555-1004', 'Loan Officer', 'Lending', 1, 2, DATE '2016-07-10', 75000, 0.01, 'GOOD', 750000, 'ACTIVE', 'OFFICER', '456-78-9012')
  INTO BANK_EMPLOYEES VALUES (5, 'EMP005', 'Maria', 'Garcia', 'maria.garcia@bank.com', '555-1005', 'Credit Analyst', 'Risk Management', 2, NULL, DATE '2017-01-20', 68000, 0.005, 'GOOD', 500000, 'ACTIVE', 'ANALYST', '567-89-0123')
  INTO BANK_EMPLOYEES VALUES (6, 'EMP006', 'David', 'Kim', 'david.kim@bank.com', '555-1006', 'Business Loan Specialist', 'Commercial Lending', 3, 3, DATE '2018-05-15', 95000, 0.02, 'EXCELLENT', 5000000, 'ACTIVE', 'SPECIALIST', '678-90-1234')
  INTO BANK_EMPLOYEES VALUES (7, 'EMP007', 'Lisa', 'Anderson', 'lisa.anderson@bank.com', '555-1007', 'Mortgage Specialist', 'Mortgage Lending', 4, NULL, DATE '2019-02-28', 78000, 0.012, 'GOOD', 1200000, 'ACTIVE', 'SPECIALIST', '789-01-2345')
  INTO BANK_EMPLOYEES VALUES (8, 'EMP008', 'James', 'Taylor', 'james.taylor@bank.com', '555-1008', 'Risk Manager', 'Risk Management', 1, NULL, DATE '2013-08-12', 110000, 0, 'EXCELLENT', 0, 'ACTIVE', 'MANAGER', '890-12-3456')
SELECT * FROM dual;

-- Insert loan products
INSERT ALL
  INTO BANK_LOAN_PRODUCTS VALUES (1, 'PL001', 'Personal Loan - Standard', 'PERSONAL', 'Standard personal loan for various purposes', 5000, 50000, 12, 84, 0.0899, 650, NULL, 0.02, 0.05, 'ACTIVE', DATE '2020-01-01', NULL)
  INTO BANK_LOAN_PRODUCTS VALUES (2, 'PL002', 'Personal Loan - Premium', 'PERSONAL', 'Premium personal loan with lower rates', 10000, 100000, 12, 84, 0.0699, 720, NULL, 0.015, 0.03, 'ACTIVE', DATE '2020-01-01', NULL)
  INTO BANK_LOAN_PRODUCTS VALUES (3, 'MTG001', 'Home Mortgage - Fixed 30Y', 'MORTGAGE', '30-year fixed rate mortgage', 100000, 2000000, 360, 360, 0.0425, 620, 0.8, 0.01, 0.02, 'ACTIVE', DATE '2020-01-01', NULL)
  INTO BANK_LOAN_PRODUCTS VALUES (4, 'MTG002', 'Home Mortgage - ARM 5/1', 'MORTGAGE', '5/1 Adjustable rate mortgage', 100000, 1500000, 360, 360, 0.0375, 640, 0.8, 0.01, 0.02, 'ACTIVE', DATE '2020-01-01', NULL)
  INTO BANK_LOAN_PRODUCTS VALUES (5, 'BL001', 'Business Term Loan', 'BUSINESS', 'Term loan for business expansion', 50000, 5000000, 12, 120, 0.0575, 680, NULL, 0.025, 0.03, 'ACTIVE', DATE '2020-01-01', NULL)
  INTO BANK_LOAN_PRODUCTS VALUES (6, 'AL001', 'Auto Loan - New', 'AUTO', 'Auto loan for new vehicles', 15000, 150000, 24, 84, 0.0449, 600, 0.9, 0.005, 0, 'ACTIVE', DATE '2020-01-01', NULL)
  INTO BANK_LOAN_PRODUCTS VALUES (7, 'AL002', 'Auto Loan - Used', 'AUTO', 'Auto loan for used vehicles', 10000, 75000, 24, 72, 0.0549, 600, 0.8, 0.005, 0, 'ACTIVE', DATE '2020-01-01', NULL)
  INTO BANK_LOAN_PRODUCTS VALUES (8, 'EL001', 'Education Loan', 'EDUCATION', 'Student loan for education expenses', 5000, 200000, 60, 180, 0.0399, 580, NULL, 0.01, 0, 'ACTIVE', DATE '2020-01-01', NULL)
SELECT * FROM dual;

-- Insert customers
INSERT ALL
  INTO BANK_CUSTOMERS VALUES (1, 'C000001', 'INDIVIDUAL', 'John', 'Smith', NULL, DATE '1985-03-15', '123-45-6789', NULL, 'john.smith@email.com', '555-2001', '123 Oak Street', 'New York', 'NY', '10001', 85000, 'EMPLOYED', 'Tech Solutions Inc', 8, 750, DATE '2018-05-10', 1, 'ACTIVE', 'LOW', 25000)
  INTO BANK_CUSTOMERS VALUES (2, 'C000002', 'INDIVIDUAL', 'Emily', 'Johnson', NULL, DATE '1990-07-22', '234-56-7890', NULL, 'emily.johnson@email.com', '555-2002', '456 Pine Avenue', 'San Jose', 'CA', '95110', 95000, 'EMPLOYED', 'Google Inc', 6, 780, DATE '2019-02-15', 3, 'ACTIVE', 'LOW', 35000)
  INTO BANK_CUSTOMERS VALUES (3, 'C000003', 'BUSINESS', NULL, NULL, 'ABC Manufacturing LLC', NULL, NULL, '12-3456789', 'contact@abcmfg.com', '555-2003', '789 Industrial Blvd', 'Chicago', 'IL', '60601', 2500000, 'BUSINESS', 'ABC Manufacturing LLC', 15, 720, DATE '2015-08-20', 4, 'ACTIVE', 'MEDIUM', 450000)
  INTO BANK_CUSTOMERS VALUES (4, 'C000004', 'INDIVIDUAL', 'Michael', 'Brown', NULL, DATE '1982-11-08', '345-67-8901', NULL, 'michael.brown@email.com', '555-2004', '321 Elm Street', 'Austin', 'TX', '73301', 75000, 'EMPLOYED', 'Dell Technologies', 10, 680, DATE '2017-11-30', 5, 'ACTIVE', 'MEDIUM', 18000)
  INTO BANK_CUSTOMERS VALUES (5, 'C000005', 'INDIVIDUAL', 'Sarah', 'Davis', NULL, DATE '1988-06-14', '456-78-9012', NULL, 'sarah.davis@email.com', '555-2005', '654 Maple Drive', 'New York', 'NY', '10016', 120000, 'EMPLOYED', 'Morgan Stanley', 7, 800, DATE '2016-03-25', 2, 'ACTIVE', 'LOW', 65000)
  INTO BANK_CUSTOMERS VALUES (6, 'C000006', 'INDIVIDUAL', 'Robert', 'Wilson', NULL, DATE '1975-12-03', '567-89-0123', NULL, 'robert.wilson@email.com', '555-2006', '987 Cedar Lane', 'San Jose', 'CA', '95112', 65000, 'EMPLOYED', 'Apple Inc', 12, 720, DATE '2014-07-18', 3, 'ACTIVE', 'LOW', 28000)
  INTO BANK_CUSTOMERS VALUES (7, 'C000007', 'BUSINESS', NULL, NULL, 'Green Energy Solutions', NULL, NULL, '23-4567890', 'info@greenenergy.com', '555-2007', '147 Solar Street', 'Austin', 'TX', '73302', 1800000, 'BUSINESS', 'Green Energy Solutions', 8, 740, DATE '2020-01-12', 5, 'ACTIVE', 'LOW', 180000)
  INTO BANK_CUSTOMERS VALUES (8, 'C000008', 'INDIVIDUAL', 'Jessica', 'Martinez', NULL, DATE '1993-04-28', '678-90-1234', NULL, 'jessica.martinez@email.com', '555-2008', '258 Birch Road', 'Chicago', 'IL', '60602', 82000, 'EMPLOYED', 'JPMorgan Chase', 4, 710, DATE '2021-09-05', 4, 'ACTIVE', 'LOW', 22000)
  INTO BANK_CUSTOMERS VALUES (9, 'C000009', 'INDIVIDUAL', 'David', 'Lee', NULL, DATE '1980-01-20', '789-01-2345', NULL, 'david.lee@email.com', '555-2009', '369 Spruce Court', 'New York', 'NY', '10003', 105000, 'EMPLOYED', 'Goldman Sachs', 9, 760, DATE '2013-12-08', 1, 'ACTIVE', 'LOW', 42000)
  INTO BANK_CUSTOMERS VALUES (10, 'C000010', 'INDIVIDUAL', 'Lisa', 'Garcia', NULL, DATE '1987-09-17', '890-12-3456', NULL, 'lisa.garcia@email.com', '555-2010', '741 Willow Way', 'San Jose', 'CA', '95113', 88000, 'SELF_EMPLOYED', 'Garcia Consulting', 5, 690, DATE '2019-06-22', 3, 'ACTIVE', 'MEDIUM', 31000)
SELECT * FROM dual;

-- Insert loan applications
INSERT ALL
  INTO BANK_LOAN_APPLICATIONS VALUES (1, 'LA2024001', 1, 3, 2, 1, DATE '2024-01-15', 350000, 360, 'Home Purchase', 'REAL_ESTATE', 400000, 'APPROVED', 750, 0.28, 0.875, 'Excellent credit profile', DATE '2024-01-22', 350000, 0.0425, 360, NULL)
  INTO BANK_LOAN_APPLICATIONS VALUES (2, 'LA2024002', 2, 5, 6, 3, DATE '2024-01-20', 250000, 60, 'Business Expansion', 'EQUIPMENT', 300000, 'APPROVED', 780, 0.32, 0.833, 'Strong business financials', DATE '2024-01-25', 250000, 0.0575, 60, NULL)
  INTO BANK_LOAN_APPLICATIONS VALUES (3, 'LA2024003', 4, 1, 4, 5, DATE '2024-02-01', 25000, 60, 'Debt Consolidation', 'UNSECURED', NULL, 'APPROVED', 680, 0.42, NULL, 'Standard approval', DATE '2024-02-05', 25000, 0.0899, 60, NULL)
  INTO BANK_LOAN_APPLICATIONS VALUES (4, 'LA2024004', 3, 5, 6, 3, DATE '2024-02-10', 500000, 84, 'Equipment Purchase', 'EQUIPMENT', 600000, 'APPROVED', 720, 0.25, 0.833, 'Business loan approved', DATE '2024-02-15', 500000, 0.0575, 84, NULL)
  INTO BANK_LOAN_APPLICATIONS VALUES (5, 'LA2024005', 5, 4, 7, 4, DATE '2024-02-20', 450000, 360, 'Home Purchase', 'REAL_ESTATE', 550000, 'APPROVED', 800, 0.22, 0.818, 'Premium customer', DATE '2024-02-25', 450000, 0.0375, 360, NULL)
  INTO BANK_LOAN_APPLICATIONS VALUES (6, 'LA2024006', 6, 6, 2, 1, DATE '2024-03-01', 45000, 72, 'Vehicle Purchase', 'VEHICLE', 50000, 'APPROVED', 720, 0.35, 0.9, 'Auto loan approved', DATE '2024-03-05', 45000, 0.0449, 72, NULL)
  INTO BANK_LOAN_APPLICATIONS VALUES (7, 'LA2024007', 8, 2, 4, 4, DATE '2024-03-10', 35000, 72, 'Home Improvement', 'UNSECURED', NULL, 'APPROVED', 710, 0.38, NULL, 'Premium personal loan', DATE '2024-03-12', 35000, 0.0699, 72, NULL)
  INTO BANK_LOAN_APPLICATIONS VALUES (8, 'LA2024008', 9, 3, 2, 1, DATE '2024-03-15', 750000, 360, 'Home Purchase', 'REAL_ESTATE', 900000, 'PENDING', 760, 0.25, 0.833, 'Under review', NULL, NULL, NULL, NULL, NULL)
  INTO BANK_LOAN_APPLICATIONS VALUES (9, 'LA2024009', 10, 1, 4, 3, DATE '2024-03-20', 15000, 48, 'Business Equipment', 'EQUIPMENT', 18000, 'REJECTED', 690, 0.45, 0.833, 'DTI too high', DATE '2024-03-22', NULL, NULL, NULL, 'Debt-to-income ratio exceeds policy limits')
  INTO BANK_LOAN_APPLICATIONS VALUES (10, 'LA2024010', 7, 5, 6, 5, DATE '2024-03-25', 1200000, 120, 'Business Expansion', 'REAL_ESTATE', 1500000, 'APPROVED', 740, 0.28, 0.8, 'Commercial loan approved', DATE '2024-03-30', 1200000, 0.0575, 120, NULL)
SELECT * FROM dual;

-- Insert active loans (for approved applications)
INSERT ALL
  INTO BANK_LOANS VALUES (1, 'LN2024001', 1, 1, 3, 2, 1, DATE '2024-02-01', 350000, 0.0425, 360, 1854.02, 346789.45, 3, DATE '2024-04-01', 1854.02, DATE '2024-05-01', 'CURRENT', DATE '2054-02-01', '123 Oak Street Property', 'Y', 'Y', 'CURRENT', 0, 1351.12)
  INTO BANK_LOANS VALUES (2, 'LN2024002', 2, 2, 5, 6, 3, DATE '2024-02-01', 250000, 0.0575, 60, 4788.49, 235677.88, 3, DATE '2024-04-01', 4788.49, DATE '2024-05-01', 'CURRENT', DATE '2029-02-01', 'Manufacturing Equipment', 'N', 'N', 'CURRENT', 0, 4544.59)
  INTO BANK_LOANS VALUES (3, 'LN2024003', 3, 4, 1, 4, 5, DATE '2024-02-15', 25000, 0.0899, 60, 518.74, 23456.78, 2, DATE '2024-04-15', 518.74, DATE '2024-05-15', 'CURRENT', DATE '2029-02-15', 'Unsecured Personal Loan', 'N', 'N', 'CURRENT', 0, 337.48)
  INTO BANK_LOANS VALUES (4, 'LN2024004', 4, 3, 5, 6, 3, DATE '2024-03-01', 500000, 0.0575, 84, 7290.37, 478932.15, 2, DATE '2024-04-01', 7290.37, DATE '2024-05-01', 'CURRENT', DATE '2031-03-01', 'Commercial Equipment', 'Y', 'N', 'CURRENT', 0, 4580.74)
  INTO BANK_LOANS VALUES (5, 'LN2024005', 5, 5, 4, 7, 4, DATE '2024-03-15', 450000, 0.0375, 360, 2085.18, 447234.56, 1, DATE '2024-04-15', 2085.18, DATE '2024-05-15', 'CURRENT', DATE '2054-03-15', '654 Maple Drive Property', 'Y', 'Y', 'CURRENT', 0, 640.82)
  INTO BANK_LOANS VALUES (6, 'LN2024006', 6, 6, 6, 2, 1, DATE '2024-03-15', 45000, 0.0449, 72, 704.55, 43567.89, 2, DATE '2024-05-15', 704.55, DATE '2024-06-15', 'CURRENT', DATE '2030-03-15', '2022 Honda Civic', 'Y', 'N', 'CURRENT', 0, 409.10)
  INTO BANK_LOANS VALUES (7, 'LN2024007', 7, 8, 2, 4, 4, DATE '2024-03-20', 35000, 0.0699, 72, 576.89, 33876.54, 1, DATE '2024-04-20', 576.89, DATE '2024-05-20', 'CURRENT', DATE '2030-03-20', 'Home Improvement Loan', 'N', 'N', 'CURRENT', 0, 203.35)
  INTO BANK_LOANS VALUES (8, 'LN2024008', 10, 7, 5, 6, 5, DATE '2024-04-05', 1200000, 0.0575, 120, 13492.78, 1185643.22, 1, DATE '2024-05-05', 13492.78, DATE '2024-06-05', 'CURRENT', DATE '2034-04-05', 'Commercial Real Estate', 'Y', 'N', 'CURRENT', 0, 5750.00)
SELECT * FROM dual;

-- Insert loan payments (payment history for active loans)
-- Loan 1 payments
INSERT INTO BANK_LOAN_PAYMENTS VALUES (1, 1, DATE '2024-02-01', 1854.02, 606.02, 1248.00, 0, 0, 'AUTO_DEBIT', 'TXN001', 2, 'COMPLETED', 349393.98);
INSERT INTO BANK_LOAN_PAYMENTS VALUES (2, 1, DATE '2024-03-01', 1854.02, 608.17, 1245.85, 0, 0, 'AUTO_DEBIT', 'TXN002', 2, 'COMPLETED', 348785.81);
INSERT INTO BANK_LOAN_PAYMENTS VALUES (3, 1, DATE '2024-04-01', 1854.02, 610.33, 1243.69, 0, 0, 'AUTO_DEBIT', 'TXN003', 2, 'COMPLETED', 348175.48);

-- Loan 2 payments
INSERT INTO BANK_LOAN_PAYMENTS VALUES (4, 2, DATE '2024-02-01', 4788.49, 3595.82, 1192.67, 0, 0, 'WIRE_TRANSFER', 'TXN004', 6, 'COMPLETED', 246404.18);
INSERT INTO BANK_LOAN_PAYMENTS VALUES (5, 2, DATE '2024-03-01', 4788.49, 3613.12, 1175.37, 0, 0, 'WIRE_TRANSFER', 'TXN005', 6, 'COMPLETED', 242791.06);
INSERT INTO BANK_LOAN_PAYMENTS VALUES (6, 2, DATE '2024-04-01', 4788.49, 3630.50, 1157.99, 0, 0, 'WIRE_TRANSFER', 'TXN006', 6, 'COMPLETED', 239160.56);

-- Generate market rates data for time series analysis
INSERT INTO BANK_MARKET_RATES
SELECT 
    ROWNUM as rate_id,
    DATE '2023-01-01' + (LEVEL - 1) as rate_date,
    'PRIME' as rate_type,
    0.0525 + (DBMS_RANDOM.VALUE(-50, 50) / 10000) as rate_value,
    (DBMS_RANDOM.VALUE(-25, 25)) as basis_points_change,
    'Federal Reserve' as source,
    'US' as region
FROM dual
CONNECT BY LEVEL <= 365;

-- Insert credit factors
INSERT ALL
  INTO BANK_CREDIT_FACTORS VALUES (1, 1, DATE '2024-01-10', 780, 0.25, 96, 4, 2, 35000, 0.28, 850, 350000, 750, 'A')
  INTO BANK_CREDIT_FACTORS VALUES (2, 2, DATE '2024-01-15', 800, 0.15, 72, 5, 1, 28000, 0.32, 900, 300000, 780, 'A+')
  INTO BANK_CREDIT_FACTORS VALUES (3, 3, DATE '2024-01-20', 720, 0.35, 180, 3, 0, 125000, 0.25, 950, 1500000, 720, 'A')
  INTO BANK_CREDIT_FACTORS VALUES (4, 4, DATE '2024-02-01', 680, 0.42, 84, 3, 3, 42000, 0.42, 750, NULL, 680, 'B')
  INTO BANK_CREDIT_FACTORS VALUES (5, 5, DATE '2024-02-15', 820, 0.18, 108, 6, 1, 15000, 0.22, 900, 550000, 800, 'A+')
  INTO BANK_CREDIT_FACTORS VALUES (6, 6, DATE '2024-03-01', 720, 0.28, 144, 4, 2, 38000, 0.35, 820, 50000, 720, 'A')
  INTO BANK_CREDIT_FACTORS VALUES (7, 7, DATE '2024-03-10', 740, 0.22, 96, 5, 1, 85000, 0.28, 880, 1200000, 740, 'A')
  INTO BANK_CREDIT_FACTORS VALUES (8, 8, DATE '2024-03-15', 710, 0.38, 48, 3, 2, 28000, 0.38, 720, NULL, 710, 'A-')
  INTO BANK_CREDIT_FACTORS VALUES (9, 9, DATE '2024-03-20', 760, 0.20, 120, 5, 1, 25000, 0.25, 890, 900000, 760, 'A+')
  INTO BANK_CREDIT_FACTORS VALUES (10, 10, DATE '2024-03-25', 690, 0.45, 60, 2, 4, 45000, 0.45, 650, NULL, 690, 'B+')
SELECT * FROM dual;

-- Insert loan performance metrics
INSERT INTO BANK_LOAN_METRICS
SELECT 
    ROWNUM as metric_id,
    DATE '2024-01-01' + (LEVEL - 1) * 7 as reporting_date,
    MOD(LEVEL, 5) + 1 as branch_id,
    MOD(LEVEL, 8) + 1 as product_id,
    50 + ROUND(DBMS_RANDOM.VALUE(10, 30)) as total_applications,
    ROUND((50 + ROUND(DBMS_RANDOM.VALUE(10, 30))) * 0.75) as approved_applications,
    ROUND((50 + ROUND(DBMS_RANDOM.VALUE(10, 30))) * 0.25) as rejected_applications,
    0.75 + (DBMS_RANDOM.VALUE(-5, 5) / 100) as approval_rate,
    150000 + ROUND(DBMS_RANDOM.VALUE(-50000, 100000)) as average_loan_amount,
    5000000 + ROUND(DBMS_RANDOM.VALUE(-1000000, 2000000)) as total_loans_originated,
    25000000 + ROUND(DBMS_RANDOM.VALUE(-5000000, 10000000)) as current_portfolio_balance,
    ROUND(DBMS_RANDOM.VALUE(5, 25)) as delinquent_loans_count,
    0.02 + (DBMS_RANDOM.VALUE(-5, 10) / 1000) as delinquency_rate,
    ROUND(DBMS_RANDOM.VALUE(10000, 100000)) as charge_offs_amount,
    0.005 + (DBMS_RANDOM.VALUE(-2, 5) / 1000) as net_charge_off_rate,
    720 + ROUND(DBMS_RANDOM.VALUE(-40, 60)) as average_credit_score
FROM dual
CONNECT BY LEVEL <= 52; -- Weekly data for a year

-- ============================================================================
-- 4. CREATE INDEXES FOR PERFORMANCE TESTING
-- ============================================================================

-- Customer indexes
CREATE INDEX idx_customer_number ON BANK_CUSTOMERS(customer_number);
CREATE INDEX idx_customer_credit_score ON BANK_CUSTOMERS(credit_score);
CREATE INDEX idx_customer_branch ON BANK_CUSTOMERS(primary_branch_id);
CREATE INDEX idx_customer_type ON BANK_CUSTOMERS(customer_type);

-- Loan indexes
CREATE INDEX idx_loan_number ON BANK_LOANS(loan_number);
CREATE INDEX idx_loan_customer ON BANK_LOANS(customer_id);
CREATE INDEX idx_loan_status ON BANK_LOANS(loan_status);
CREATE INDEX idx_loan_origination_date ON BANK_LOANS(origination_date);
CREATE INDEX idx_loan_branch ON BANK_LOANS(branch_id);
CREATE INDEX idx_loan_officer ON BANK_LOANS(loan_officer_id);

-- Application indexes
CREATE INDEX idx_app_number ON BANK_LOAN_APPLICATIONS(application_number);
CREATE INDEX idx_app_customer ON BANK_LOAN_APPLICATIONS(customer_id);
CREATE INDEX idx_app_status ON BANK_LOAN_APPLICATIONS(application_status);
CREATE INDEX idx_app_date ON BANK_LOAN_APPLICATIONS(application_date);
CREATE INDEX idx_app_officer ON BANK_LOAN_APPLICATIONS(loan_officer_id);

-- Payment indexes
CREATE INDEX idx_payment_loan ON BANK_LOAN_PAYMENTS(loan_id);
CREATE INDEX idx_payment_date ON BANK_LOAN_PAYMENTS(payment_date);
CREATE INDEX idx_payment_status ON BANK_LOAN_PAYMENTS(payment_status);

-- Metrics indexes
CREATE INDEX idx_metrics_date ON BANK_LOAN_METRICS(reporting_date);
CREATE INDEX idx_metrics_branch ON BANK_LOAN_METRICS(branch_id);
CREATE INDEX idx_metrics_product ON BANK_LOAN_METRICS(product_id);

-- Market rates indexes
CREATE INDEX idx_rates_date ON BANK_MARKET_RATES(rate_date);
CREATE INDEX idx_rates_type ON BANK_MARKET_RATES(rate_type);

-- ============================================================================
-- 5. CREATE VIEWS FOR ANALYTICS TESTING
-- ============================================================================

CREATE OR REPLACE VIEW BANK_LOAN_PORTFOLIO_SUMMARY AS
SELECT 
    b.branch_name,
    p.product_name,
    p.loan_type,
    COUNT(l.loan_id) as active_loans,
    SUM(l.principal_amount) as total_principal,
    SUM(l.current_balance) as current_balance,
    AVG(l.interest_rate) as avg_interest_rate,
    SUM(CASE WHEN l.delinquency_status = 'CURRENT' THEN 1 ELSE 0 END) as current_loans,
    SUM(CASE WHEN l.days_past_due > 30 THEN 1 ELSE 0 END) as past_due_loans,
    ROUND(SUM(CASE WHEN l.days_past_due > 30 THEN 1 ELSE 0 END) / COUNT(l.loan_id) * 100, 2) as delinquency_rate
FROM BANK_LOANS l
JOIN BANK_BRANCHES b ON l.branch_id = b.branch_id
JOIN BANK_LOAN_PRODUCTS p ON l.product_id = p.product_id
WHERE l.loan_status = 'ACTIVE'
GROUP BY b.branch_name, p.product_name, p.loan_type;

CREATE OR REPLACE VIEW BANK_CUSTOMER_RISK_PROFILE AS
SELECT 
    c.customer_id,
    c.customer_number,
    CASE WHEN c.customer_type = 'INDIVIDUAL' THEN c.first_name || ' ' || c.last_name 
         ELSE c.business_name END as customer_name,
    c.credit_score,
    c.annual_income,
    c.risk_category,
    COUNT(l.loan_id) as total_loans,
    SUM(l.current_balance) as total_debt,
    ROUND(SUM(l.current_balance) / NULLIF(c.annual_income, 0) * 100, 2) as debt_to_income_ratio,
    MAX(l.days_past_due) as max_days_past_due,
    CASE 
        WHEN MAX(l.days_past_due) = 0 THEN 'EXCELLENT'
        WHEN MAX(l.days_past_due) <= 30 THEN 'GOOD'
        WHEN MAX(l.days_past_due) <= 60 THEN 'FAIR'
        ELSE 'POOR'
    END as payment_behavior
FROM BANK_CUSTOMERS c
LEFT JOIN BANK_LOANS l ON c.customer_id = l.customer_id AND l.loan_status = 'ACTIVE'
GROUP BY c.customer_id, c.customer_number, c.first_name, c.last_name, c.business_name, 
         c.customer_type, c.credit_score, c.annual_income, c.risk_category;

CREATE OR REPLACE VIEW BANK_MONTHLY_LOAN_TRENDS AS
SELECT 
    TO_CHAR(l.origination_date, 'YYYY-MM') as loan_month,
    p.loan_type,
    COUNT(*) as loans_originated,
    SUM(l.principal_amount) as total_amount,
    AVG(l.principal_amount) as avg_loan_amount,
    AVG(l.interest_rate) as avg_interest_rate,
    AVG(cf.final_credit_score) as avg_credit_score
FROM BANK_LOANS l
JOIN BANK_LOAN_PRODUCTS p ON l.product_id = p.product_id
LEFT JOIN BANK_CREDIT_FACTORS cf ON l.customer_id = cf.customer_id
GROUP BY TO_CHAR(l.origination_date, 'YYYY-MM'), p.loan_type
ORDER BY loan_month, p.loan_type;

-- ============================================================================
-- 6. COMMIT AND GATHER STATISTICS
-- ============================================================================

COMMIT;

-- Gather statistics for cost-based optimization testing
BEGIN
    DBMS_STATS.GATHER_SCHEMA_STATS(USER);
END;
/

-- ============================================================================
-- 7. VALIDATION QUERIES
-- ============================================================================

-- Verify bank data was inserted correctly
SELECT 'Bank Branches' as table_name, COUNT(*) as row_count FROM BANK_BRANCHES
UNION ALL
SELECT 'Bank Employees', COUNT(*) FROM BANK_EMPLOYEES  
UNION ALL
SELECT 'Bank Customers', COUNT(*) FROM BANK_CUSTOMERS
UNION ALL
SELECT 'Loan Products', COUNT(*) FROM BANK_LOAN_PRODUCTS
UNION ALL
SELECT 'Loan Applications', COUNT(*) FROM BANK_LOAN_APPLICATIONS
UNION ALL
SELECT 'Active Loans', COUNT(*) FROM BANK_LOANS
UNION ALL
SELECT 'Loan Payments', COUNT(*) FROM BANK_LOAN_PAYMENTS
UNION ALL
SELECT 'Credit Factors', COUNT(*) FROM BANK_CREDIT_FACTORS
UNION ALL
SELECT 'Loan Metrics', COUNT(*) FROM BANK_LOAN_METRICS
UNION ALL
SELECT 'Market Rates', COUNT(*) FROM BANK_MARKET_RATES;

-- Show sample bank data
SELECT 'Sample Customer Data:' as info FROM dual;
SELECT customer_number, first_name, last_name, credit_score, annual_income, risk_category
FROM BANK_CUSTOMERS WHERE customer_type = 'INDIVIDUAL' AND ROWNUM <= 5;

SELECT 'Sample Loan Portfolio:' as info FROM dual;
SELECT loan_number, principal_amount, current_balance, interest_rate, loan_status
FROM BANK_LOANS WHERE ROWNUM <= 5;

SELECT 'Sample Credit Analysis:' as info FROM dual;
SELECT customer_id, final_credit_score, debt_to_income_ratio, risk_grade
FROM BANK_CREDIT_FACTORS WHERE ROWNUM <= 5;

PROMPT
PROMPT ============================================================================
PROMPT Bank Loan Department test data setup completed successfully!
PROMPT ============================================================================
PROMPT Tables created: 10
PROMPT Views created: 3
PROMPT Indexes created: 20+
PROMPT Customers: 10 (8 Individual, 2 Business)
PROMPT Loan Products: 8 (Personal, Mortgage, Business, Auto, Education)
PROMPT Active Loans: 8 (Total Portfolio: $2.5M+)
PROMPT Market Rates: 365 daily observations
PROMPT Ready for comprehensive Oracle MCP Server banking tests
PROMPT ============================================================================
