-- Banking Database Schema for Loan Processing System
-- Complete schema with 15 tables and sample data for analytics testing
-- Created for Oracle MCP Server testing

-- Connect as C##DEEPAI user
-- ALTER SESSION SET CURRENT_SCHEMA = C##DEEPAI;

-- Drop tables if they exist (in reverse order to handle foreign keys)
BEGIN
    FOR cur_rec IN (SELECT object_name FROM user_objects WHERE object_type = 'TABLE' ORDER BY object_name DESC)
    LOOP
        EXECUTE IMMEDIATE 'DROP TABLE ' || cur_rec.object_name || ' CASCADE CONSTRAINTS';
    END LOOP;
END;
/

-- 1. CUSTOMERS table - Core customer information
CREATE TABLE CUSTOMERS (
    customer_id NUMBER PRIMARY KEY,
    first_name VARCHAR2(50) NOT NULL,
    last_name VARCHAR2(50) NOT NULL,
    email VARCHAR2(100) UNIQUE,
    phone VARCHAR2(20),
    date_of_birth DATE,
    ssn VARCHAR2(11) UNIQUE,
    address VARCHAR2(200),
    city VARCHAR2(50),
    state VARCHAR2(50),
    zip_code VARCHAR2(10),
    annual_income NUMBER(12,2),
    employment_status VARCHAR2(20),
    credit_score NUMBER(3),
    customer_since DATE DEFAULT SYSDATE,
    status VARCHAR2(10) DEFAULT 'ACTIVE'
);

-- 2. ACCOUNTS table - Bank accounts
CREATE TABLE ACCOUNTS (
    account_id NUMBER PRIMARY KEY,
    customer_id NUMBER REFERENCES CUSTOMERS(customer_id),
    account_number VARCHAR2(20) UNIQUE,
    account_type VARCHAR2(20) CHECK (account_type IN ('CHECKING', 'SAVINGS', 'BUSINESS', 'INVESTMENT')),
    balance NUMBER(15,2) DEFAULT 0,
    opened_date DATE DEFAULT SYSDATE,
    status VARCHAR2(10) DEFAULT 'ACTIVE',
    interest_rate NUMBER(5,4) DEFAULT 0
);

-- 3. BRANCHES table - Bank branch information
CREATE TABLE BRANCHES (
    branch_id NUMBER PRIMARY KEY,
    branch_name VARCHAR2(100),
    branch_code VARCHAR2(10) UNIQUE,
    address VARCHAR2(200),
    city VARCHAR2(50),
    state VARCHAR2(50),
    zip_code VARCHAR2(10),
    phone VARCHAR2(20),
    manager_name VARCHAR2(100),
    opened_date DATE,
    total_employees NUMBER DEFAULT 0
);

-- 4. EMPLOYEES table - Bank employees
CREATE TABLE EMPLOYEES (
    employee_id NUMBER PRIMARY KEY,
    branch_id NUMBER REFERENCES BRANCHES(branch_id),
    first_name VARCHAR2(50),
    last_name VARCHAR2(50),
    email VARCHAR2(100) UNIQUE,
    phone VARCHAR2(20),
    position VARCHAR2(50),
    department VARCHAR2(50),
    manager_id NUMBER REFERENCES EMPLOYEES(employee_id),
    salary NUMBER(10,2),
    hire_date DATE,
    commission_rate NUMBER(5,4) DEFAULT 0,
    status VARCHAR2(10) DEFAULT 'ACTIVE'
);

-- 5. LOAN_TYPES table - Different types of loans
CREATE TABLE LOAN_TYPES (
    loan_type_id NUMBER PRIMARY KEY,
    loan_type_name VARCHAR2(50) UNIQUE,
    description VARCHAR2(500),
    base_interest_rate NUMBER(5,4),
    max_amount NUMBER(15,2),
    min_amount NUMBER(15,2),
    max_term_months NUMBER(3),
    min_term_months NUMBER(3),
    processing_fee_percent NUMBER(5,4),
    requires_collateral CHAR(1) CHECK (requires_collateral IN ('Y', 'N')),
    min_credit_score NUMBER(3),
    status VARCHAR2(10) DEFAULT 'ACTIVE'
);

-- 6. LOAN_APPLICATIONS table - Loan applications from customers
CREATE TABLE LOAN_APPLICATIONS (
    application_id NUMBER PRIMARY KEY,
    customer_id NUMBER REFERENCES CUSTOMERS(customer_id),
    loan_type_id NUMBER REFERENCES LOAN_TYPES(loan_type_id),
    requested_amount NUMBER(15,2),
    requested_term_months NUMBER(3),
    purpose VARCHAR2(200),
    application_date DATE DEFAULT SYSDATE,
    processed_by NUMBER REFERENCES EMPLOYEES(employee_id),
    processed_date DATE,
    status VARCHAR2(20) DEFAULT 'PENDING',
    notes VARCHAR2(1000),
    debt_to_income_ratio NUMBER(5,4),
    collateral_value NUMBER(15,2)
);

-- 7. LOANS table - Approved loans
CREATE TABLE LOANS (
    loan_id NUMBER PRIMARY KEY,
    application_id NUMBER REFERENCES LOAN_APPLICATIONS(application_id),
    customer_id NUMBER REFERENCES CUSTOMERS(customer_id),
    loan_type_id NUMBER REFERENCES LOAN_TYPES(loan_type_id),
    principal_amount NUMBER(15,2),
    interest_rate NUMBER(5,4),
    term_months NUMBER(3),
    monthly_payment NUMBER(10,2),
    disbursement_date DATE,
    maturity_date DATE,
    current_balance NUMBER(15,2),
    payments_made NUMBER(3) DEFAULT 0,
    last_payment_date DATE,
    next_payment_due DATE,
    status VARCHAR2(20) DEFAULT 'ACTIVE',
    created_by NUMBER REFERENCES EMPLOYEES(employee_id)
);

-- 8. PAYMENTS table - Loan payment history
CREATE TABLE PAYMENTS (
    payment_id NUMBER PRIMARY KEY,
    loan_id NUMBER REFERENCES LOANS(loan_id),
    payment_date DATE DEFAULT SYSDATE,
    scheduled_amount NUMBER(10,2),
    actual_amount NUMBER(10,2),
    principal_portion NUMBER(10,2),
    interest_portion NUMBER(10,2),
    late_fee NUMBER(8,2) DEFAULT 0,
    payment_method VARCHAR2(20),
    transaction_id VARCHAR2(50),
    processed_by NUMBER REFERENCES EMPLOYEES(employee_id),
    status VARCHAR2(15) DEFAULT 'COMPLETED',
    notes VARCHAR2(500)
);

-- 9. CREDIT_REPORTS table - Customer credit information
CREATE TABLE CREDIT_REPORTS (
    report_id NUMBER PRIMARY KEY,
    customer_id NUMBER REFERENCES CUSTOMERS(customer_id),
    report_date DATE DEFAULT SYSDATE,
    credit_score NUMBER(3),
    report_provider VARCHAR2(50),
    tradelines_count NUMBER(3),
    total_debt NUMBER(12,2),
    available_credit NUMBER(12,2),
    payment_history_score NUMBER(3),
    credit_utilization NUMBER(5,4),
    length_of_history_months NUMBER(4),
    new_credit_accounts NUMBER(3),
    credit_mix_score NUMBER(3),
    report_data CLOB
);

-- 10. COLLATERAL table - Loan collateral information
CREATE TABLE COLLATERAL (
    collateral_id NUMBER PRIMARY KEY,
    loan_id NUMBER REFERENCES LOANS(loan_id),
    collateral_type VARCHAR2(50),
    description VARCHAR2(500),
    estimated_value NUMBER(15,2),
    appraised_value NUMBER(15,2),
    appraiser_name VARCHAR2(100),
    appraisal_date DATE,
    condition_rating VARCHAR2(20),
    insurance_policy VARCHAR2(100),
    location VARCHAR2(200),
    status VARCHAR2(20) DEFAULT 'ACTIVE'
);

-- 11. TRANSACTIONS table - All account transactions
CREATE TABLE TRANSACTIONS (
    transaction_id NUMBER PRIMARY KEY,
    account_id NUMBER REFERENCES ACCOUNTS(account_id),
    transaction_date DATE DEFAULT SYSDATE,
    transaction_type VARCHAR2(20),
    amount NUMBER(15,2),
    balance_after NUMBER(15,2),
    description VARCHAR2(200),
    reference_number VARCHAR2(50),
    processed_by NUMBER REFERENCES EMPLOYEES(employee_id),
    branch_id NUMBER REFERENCES BRANCHES(branch_id),
    status VARCHAR2(15) DEFAULT 'COMPLETED',
    fee_amount NUMBER(8,2) DEFAULT 0
);

-- 12. RISK_ASSESSMENTS table - Loan risk evaluation
CREATE TABLE RISK_ASSESSMENTS (
    assessment_id NUMBER PRIMARY KEY,
    application_id NUMBER REFERENCES LOAN_APPLICATIONS(application_id),
    assessed_by NUMBER REFERENCES EMPLOYEES(employee_id),
    assessment_date DATE DEFAULT SYSDATE,
    risk_score NUMBER(5,2),
    risk_category VARCHAR2(20),
    income_verification CHAR(1) CHECK (income_verification IN ('Y', 'N')),
    employment_verification CHAR(1) CHECK (employment_verification IN ('Y', 'N')),
    debt_to_income_ratio NUMBER(5,4),
    loan_to_value_ratio NUMBER(5,4),
    probability_of_default NUMBER(5,4),
    recommended_action VARCHAR2(50),
    notes VARCHAR2(1000)
);

-- 13. AUDIT_LOG table - System audit trail
CREATE TABLE AUDIT_LOG (
    log_id NUMBER PRIMARY KEY,
    table_name VARCHAR2(50),
    record_id NUMBER,
    action_type VARCHAR2(10) CHECK (action_type IN ('INSERT', 'UPDATE', 'DELETE')),
    old_values CLOB,
    new_values CLOB,
    changed_by NUMBER REFERENCES EMPLOYEES(employee_id),
    change_date DATE DEFAULT SYSDATE,
    ip_address VARCHAR2(45),
    user_agent VARCHAR2(500),
    session_id VARCHAR2(100)
);

-- 14. LOAN_DOCUMENTS table - Document management
CREATE TABLE LOAN_DOCUMENTS (
    document_id NUMBER PRIMARY KEY,
    loan_id NUMBER REFERENCES LOANS(loan_id),
    application_id NUMBER REFERENCES LOAN_APPLICATIONS(application_id),
    document_type VARCHAR2(50),
    document_name VARCHAR2(200),
    file_path VARCHAR2(500),
    file_size NUMBER(12),
    mime_type VARCHAR2(100),
    uploaded_by NUMBER REFERENCES EMPLOYEES(employee_id),
    uploaded_date DATE DEFAULT SYSDATE,
    document_status VARCHAR2(20) DEFAULT 'ACTIVE',
    expiry_date DATE,
    notes VARCHAR2(500)
);

-- 15. INTEREST_RATES table - Historical interest rate tracking
CREATE TABLE INTEREST_RATES (
    rate_id NUMBER PRIMARY KEY,
    loan_type_id NUMBER REFERENCES LOAN_TYPES(loan_type_id),
    effective_date DATE,
    base_rate NUMBER(5,4),
    prime_rate NUMBER(5,4),
    fed_rate NUMBER(5,4),
    risk_premium NUMBER(5,4),
    final_rate NUMBER(5,4),
    term_months NUMBER(3),
    created_by NUMBER REFERENCES EMPLOYEES(employee_id),
    created_date DATE DEFAULT SYSDATE,
    status VARCHAR2(10) DEFAULT 'ACTIVE'
);

-- Create sequences for primary keys
CREATE SEQUENCE seq_customers START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE seq_accounts START WITH 1001 INCREMENT BY 1;
CREATE SEQUENCE seq_branches START WITH 100 INCREMENT BY 1;
CREATE SEQUENCE seq_employees START WITH 2001 INCREMENT BY 1;
CREATE SEQUENCE seq_loan_types START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE seq_loan_applications START WITH 10001 INCREMENT BY 1;
CREATE SEQUENCE seq_loans START WITH 20001 INCREMENT BY 1;
CREATE SEQUENCE seq_payments START WITH 30001 INCREMENT BY 1;
CREATE SEQUENCE seq_credit_reports START WITH 40001 INCREMENT BY 1;
CREATE SEQUENCE seq_collateral START WITH 50001 INCREMENT BY 1;
CREATE SEQUENCE seq_transactions START WITH 60001 INCREMENT BY 1;
CREATE SEQUENCE seq_risk_assessments START WITH 70001 INCREMENT BY 1;
CREATE SEQUENCE seq_audit_log START WITH 80001 INCREMENT BY 1;
CREATE SEQUENCE seq_loan_documents START WITH 90001 INCREMENT BY 1;
CREATE SEQUENCE seq_interest_rates START WITH 1 INCREMENT BY 1;

-- Create indexes for performance
CREATE INDEX idx_customers_email ON CUSTOMERS(email);
CREATE INDEX idx_customers_ssn ON CUSTOMERS(ssn);
CREATE INDEX idx_customers_credit_score ON CUSTOMERS(credit_score);
CREATE INDEX idx_accounts_customer_id ON ACCOUNTS(customer_id);
CREATE INDEX idx_accounts_account_number ON ACCOUNTS(account_number);
CREATE INDEX idx_employees_branch_id ON EMPLOYEES(branch_id);
CREATE INDEX idx_employees_manager_id ON EMPLOYEES(manager_id);
CREATE INDEX idx_loan_applications_customer_id ON LOAN_APPLICATIONS(customer_id);
CREATE INDEX idx_loan_applications_status ON LOAN_APPLICATIONS(status);
CREATE INDEX idx_loans_customer_id ON LOANS(customer_id);
CREATE INDEX idx_loans_status ON LOANS(status);
CREATE INDEX idx_payments_loan_id ON PAYMENTS(loan_id);
CREATE INDEX idx_payments_payment_date ON PAYMENTS(payment_date);
CREATE INDEX idx_transactions_account_id ON TRANSACTIONS(account_id);
CREATE INDEX idx_transactions_date ON TRANSACTIONS(transaction_date);

COMMIT;

PROMPT 'Banking database schema created successfully!'
PROMPT 'Tables created: 15'
PROMPT 'Sequences created: 15' 
PROMPT 'Indexes created: 14'
PROMPT 'Ready for data population...'
