-- Banking Database - Comprehensive Data Insertion (Fixed)
-- Inserts realistic sample data in the correct order

SET SERVEROUTPUT ON SIZE 1000000
SET FEEDBACK ON

BEGIN
    DBMS_OUTPUT.PUT_LINE('Starting banking database population...');
    DBMS_OUTPUT.PUT_LINE('Timestamp: ' || TO_CHAR(SYSDATE, 'YYYY-MM-DD HH24:MI:SS'));
END;
/

-- 1. Insert Branches first
INSERT INTO BRANCHES (branch_id, branch_name, branch_code, address, city, state, zip_code, phone, manager_name, opened_date, total_employees) VALUES (seq_branches.nextval, 'Downtown Manhattan', 'NY001', '100 Wall Street', 'New York', 'NY', '10005', '212-555-0001', 'Michael Anderson', DATE '2010-01-15', 45);
INSERT INTO BRANCHES (branch_id, branch_name, branch_code, address, city, state, zip_code, phone, manager_name, opened_date, total_employees) VALUES (seq_branches.nextval, 'Beverly Hills Branch', 'CA001', '9000 Sunset Boulevard', 'Beverly Hills', 'CA', '90210', '310-555-0002', 'Sarah Johnson', DATE '2011-03-20', 38);
INSERT INTO BRANCHES (branch_id, branch_name, branch_code, address, city, state, zip_code, phone, manager_name, opened_date, total_employees) VALUES (seq_branches.nextval, 'Chicago Loop', 'IL001', '200 LaSalle Street', 'Chicago', 'IL', '60601', '312-555-0003', 'Robert Chen', DATE '2012-06-10', 42);
INSERT INTO BRANCHES (branch_id, branch_name, branch_code, address, city, state, zip_code, phone, manager_name, opened_date, total_employees) VALUES (seq_branches.nextval, 'Miami Beach', 'FL001', '1500 Ocean Drive', 'Miami Beach', 'FL', '33139', '305-555-0004', 'Maria Rodriguez', DATE '2013-09-05', 35);
INSERT INTO BRANCHES (branch_id, branch_name, branch_code, address, city, state, zip_code, phone, manager_name, opened_date, total_employees) VALUES (seq_branches.nextval, 'Dallas Downtown', 'TX001', '1800 Main Street', 'Dallas', 'TX', '75201', '214-555-0005', 'James Wilson', DATE '2014-02-14', 40);

COMMIT;
DBMS_OUTPUT.PUT_LINE('✓ Inserted 5 branches');

-- 2. Insert Branch Managers first (referenced by branches)
INSERT INTO EMPLOYEES (employee_id, branch_id, first_name, last_name, email, phone, position, department, manager_id, salary, hire_date, commission_rate, status) VALUES (seq_employees.nextval, 100, 'Michael', 'Anderson', 'manderson@bank.com', '555-1001', 'Branch Manager', 'Management', NULL, 95000, DATE '2010-01-15', 0, 'ACTIVE');
INSERT INTO EMPLOYEES (employee_id, branch_id, first_name, last_name, email, phone, position, department, manager_id, salary, hire_date, commission_rate, status) VALUES (seq_employees.nextval, 101, 'Sarah', 'Johnson', 'sjohnson@bank.com', '555-1002', 'Branch Manager', 'Management', NULL, 97000, DATE '2011-03-20', 0, 'ACTIVE');
INSERT INTO EMPLOYEES (employee_id, branch_id, first_name, last_name, email, phone, position, department, manager_id, salary, hire_date, commission_rate, status) VALUES (seq_employees.nextval, 102, 'Robert', 'Chen', 'rchen@bank.com', '555-1003', 'Branch Manager', 'Management', NULL, 99000, DATE '2012-06-10', 0, 'ACTIVE');
INSERT INTO EMPLOYEES (employee_id, branch_id, first_name, last_name, email, phone, position, department, manager_id, salary, hire_date, commission_rate, status) VALUES (seq_employees.nextval, 103, 'Maria', 'Rodriguez', 'mrodriguez@bank.com', '555-1004', 'Branch Manager', 'Management', NULL, 101000, DATE '2013-09-05', 0, 'ACTIVE');
INSERT INTO EMPLOYEES (employee_id, branch_id, first_name, last_name, email, phone, position, department, manager_id, salary, hire_date, commission_rate, status) VALUES (seq_employees.nextval, 104, 'James', 'Wilson', 'jwilson@bank.com', '555-1005', 'Branch Manager', 'Management', NULL, 103000, DATE '2014-02-14', 0, 'ACTIVE');

-- Add more employees under each manager
INSERT INTO EMPLOYEES (employee_id, branch_id, first_name, last_name, email, phone, position, department, manager_id, salary, hire_date, commission_rate, status) VALUES (seq_employees.nextval, 100, 'Alice', 'Brown', 'abrown@bank.com', '555-2001', 'Loan Officer', 'Lending', 2001, 65000, DATE '2015-03-10', 0.02, 'ACTIVE');
INSERT INTO EMPLOYEES (employee_id, branch_id, first_name, last_name, email, phone, position, department, manager_id, salary, hire_date, commission_rate, status) VALUES (seq_employees.nextval, 100, 'Bob', 'Davis', 'bdavis@bank.com', '555-2002', 'Teller', 'Operations', 2001, 45000, DATE '2016-06-15', 0, 'ACTIVE');
INSERT INTO EMPLOYEES (employee_id, branch_id, first_name, last_name, email, phone, position, department, manager_id, salary, hire_date, commission_rate, status) VALUES (seq_employees.nextval, 101, 'Carol', 'Miller', 'cmiller@bank.com', '555-2003', 'Loan Officer', 'Lending', 2002, 67000, DATE '2017-01-20', 0.02, 'ACTIVE');
INSERT INTO EMPLOYEES (employee_id, branch_id, first_name, last_name, email, phone, position, department, manager_id, salary, hire_date, commission_rate, status) VALUES (seq_employees.nextval, 101, 'David', 'Wilson', 'dwilson@bank.com', '555-2004', 'Customer Service Rep', 'Customer Service', 2002, 48000, DATE '2018-08-12', 0, 'ACTIVE');
INSERT INTO EMPLOYEES (employee_id, branch_id, first_name, last_name, email, phone, position, department, manager_id, salary, hire_date, commission_rate, status) VALUES (seq_employees.nextval, 102, 'Emma', 'Garcia', 'egarcia@bank.com', '555-2005', 'Loan Officer', 'Lending', 2003, 69000, DATE '2019-04-05', 0.02, 'ACTIVE');

COMMIT;
DBMS_OUTPUT.PUT_LINE('✓ Inserted 10 employees');

-- 3. Insert Customers (100 customers for faster processing)
DECLARE
    v_counter NUMBER := 0;
    v_first_names SYS.ODCIVARCHAR2LIST := SYS.ODCIVARCHAR2LIST(
        'James', 'Mary', 'John', 'Patricia', 'Robert', 'Jennifer', 'Michael', 'Linda', 'William', 'Elizabeth',
        'David', 'Barbara', 'Richard', 'Susan', 'Joseph', 'Jessica', 'Thomas', 'Sarah', 'Christopher', 'Karen'
    );
    v_last_names SYS.ODCIVARCHAR2LIST := SYS.ODCIVARCHAR2LIST(
        'Smith', 'Johnson', 'Williams', 'Brown', 'Jones', 'Garcia', 'Miller', 'Davis', 'Rodriguez', 'Martinez',
        'Hernandez', 'Lopez', 'Gonzalez', 'Wilson', 'Anderson', 'Thomas', 'Taylor', 'Moore', 'Jackson', 'Martin'
    );
    v_states SYS.ODCIVARCHAR2LIST := SYS.ODCIVARCHAR2LIST('NY', 'CA', 'IL', 'FL', 'TX');
    v_cities SYS.ODCIVARCHAR2LIST := SYS.ODCIVARCHAR2LIST('New York', 'Los Angeles', 'Chicago', 'Miami', 'Dallas');
BEGIN
    FOR i IN 1..100 LOOP
        INSERT INTO CUSTOMERS (
            customer_id, first_name, last_name, email, phone, date_of_birth, ssn, 
            address, city, state, zip_code, annual_income, employment_status, 
            credit_score, customer_since, status
        ) VALUES (
            seq_customers.nextval,
            v_first_names(MOD(i-1, 20) + 1),
            v_last_names(MOD(i-1, 20) + 1),
            LOWER(v_first_names(MOD(i-1, 20) + 1)) || '.' || LOWER(v_last_names(MOD(i-1, 20) + 1)) || i || '@email.com',
            '555-' || LPAD(TO_CHAR(1000 + i), 4, '0'),
            DATE '1970-01-01' + FLOOR(DBMS_RANDOM.VALUE(0, 18000)),
            LPAD(TO_CHAR(FLOOR(DBMS_RANDOM.VALUE(100000000, 999999999))), 9, '0'),
            TO_CHAR(FLOOR(DBMS_RANDOM.VALUE(100, 9999))) || ' Main St',
            v_cities(MOD(i-1, 5) + 1),
            v_states(MOD(i-1, 5) + 1),
            LPAD(TO_CHAR(FLOOR(DBMS_RANDOM.VALUE(10000, 99999))), 5, '0'),
            FLOOR(DBMS_RANDOM.VALUE(30000, 120000)),
            CASE MOD(i, 4) WHEN 0 THEN 'EMPLOYED' WHEN 1 THEN 'SELF_EMPLOYED' WHEN 2 THEN 'RETIRED' ELSE 'UNEMPLOYED' END,
            FLOOR(DBMS_RANDOM.VALUE(600, 800)),
            DATE '2020-01-01' + FLOOR(DBMS_RANDOM.VALUE(0, 1500)),
            'ACTIVE'
        );
        v_counter := v_counter + 1;
    END LOOP;
    
    DBMS_OUTPUT.PUT_LINE('✓ Inserted ' || v_counter || ' customers');
END;
/
COMMIT;

-- 4. Insert Accounts (200+ accounts)
DECLARE
    v_counter NUMBER := 0;
BEGIN
    FOR i IN 1..100 LOOP
        FOR j IN 1..FLOOR(DBMS_RANDOM.VALUE(1, 4)) LOOP
            INSERT INTO ACCOUNTS (
                account_id, customer_id, account_number, account_type, balance, 
                opened_date, status, interest_rate
            ) VALUES (
                seq_accounts.nextval,
                i,
                CASE MOD(j, 4)
                    WHEN 1 THEN 'CHK-' || LPAD(TO_CHAR(1000000 + v_counter), 7, '0')
                    WHEN 2 THEN 'SAV-' || LPAD(TO_CHAR(1000000 + v_counter), 7, '0')
                    WHEN 3 THEN 'BUS-' || LPAD(TO_CHAR(1000000 + v_counter), 7, '0')
                    ELSE 'INV-' || LPAD(TO_CHAR(1000000 + v_counter), 7, '0')
                END,
                CASE MOD(j, 4)
                    WHEN 1 THEN 'CHECKING'
                    WHEN 2 THEN 'SAVINGS'
                    WHEN 3 THEN 'BUSINESS'
                    ELSE 'INVESTMENT'
                END,
                ROUND(DBMS_RANDOM.VALUE(1000, 50000), 2),
                DATE '2020-01-01' + FLOOR(DBMS_RANDOM.VALUE(0, 1000)),
                'ACTIVE',
                CASE MOD(j, 4)
                    WHEN 1 THEN 0.005
                    WHEN 2 THEN 0.015
                    WHEN 3 THEN 0.008
                    ELSE 0.025
                END
            );
            v_counter := v_counter + 1;
        END LOOP;
    END LOOP;
    
    DBMS_OUTPUT.PUT_LINE('✓ Inserted ' || v_counter || ' accounts');
END;
/
COMMIT;

-- 5. Insert Loan Types
INSERT INTO LOAN_TYPES (loan_type_id, loan_type_name, description, base_interest_rate, max_amount, min_amount, max_term_months, min_term_months, processing_fee_percent, requires_collateral, min_credit_score, status) VALUES (seq_loan_types.nextval, 'Personal Loan', 'Unsecured personal loan', 0.0899, 50000, 1000, 84, 12, 0.01, 'N', 650, 'ACTIVE');
INSERT INTO LOAN_TYPES (loan_type_id, loan_type_name, description, base_interest_rate, max_amount, min_amount, max_term_months, min_term_months, processing_fee_percent, requires_collateral, min_credit_score, status) VALUES (seq_loan_types.nextval, 'Auto Loan', 'Secured auto loan', 0.0549, 100000, 5000, 96, 24, 0.005, 'Y', 600, 'ACTIVE');
INSERT INTO LOAN_TYPES (loan_type_id, loan_type_name, description, base_interest_rate, max_amount, min_amount, max_term_months, min_term_months, processing_fee_percent, requires_collateral, min_credit_score, status) VALUES (seq_loan_types.nextval, 'Home Mortgage', 'Fixed rate mortgage', 0.0325, 1000000, 50000, 360, 120, 0.002, 'Y', 620, 'ACTIVE');
INSERT INTO LOAN_TYPES (loan_type_id, loan_type_name, description, base_interest_rate, max_amount, min_amount, max_term_months, min_term_months, processing_fee_percent, requires_collateral, min_credit_score, status) VALUES (seq_loan_types.nextval, 'Business Loan', 'Small business loan', 0.0675, 500000, 10000, 120, 12, 0.015, 'Y', 680, 'ACTIVE');
INSERT INTO LOAN_TYPES (loan_type_id, loan_type_name, description, base_interest_rate, max_amount, min_amount, max_term_months, min_term_months, processing_fee_percent, requires_collateral, min_credit_score, status) VALUES (seq_loan_types.nextval, 'Student Loan', 'Education financing', 0.0450, 150000, 2000, 240, 60, 0.005, 'N', 600, 'ACTIVE');

COMMIT;
DBMS_OUTPUT.PUT_LINE('✓ Inserted 5 loan types');

-- 6. Insert Loan Applications (50 applications)
DECLARE
    v_counter NUMBER := 0;
BEGIN
    FOR i IN 1..50 LOOP
        INSERT INTO LOAN_APPLICATIONS (
            application_id, customer_id, loan_type_id, requested_amount, requested_term_months,
            purpose, application_date, processed_by, processed_date, status, notes,
            debt_to_income_ratio, collateral_value
        ) VALUES (
            seq_loan_applications.nextval,
            MOD(i-1, 100) + 1,
            MOD(i-1, 5) + 1,
            CASE MOD(i-1, 5) + 1
                WHEN 1 THEN FLOOR(DBMS_RANDOM.VALUE(5000, 30000))
                WHEN 2 THEN FLOOR(DBMS_RANDOM.VALUE(15000, 50000))
                WHEN 3 THEN FLOOR(DBMS_RANDOM.VALUE(200000, 500000))
                WHEN 4 THEN FLOOR(DBMS_RANDOM.VALUE(25000, 200000))
                ELSE FLOOR(DBMS_RANDOM.VALUE(10000, 80000))
            END,
            FLOOR(DBMS_RANDOM.VALUE(12, 60)),
            CASE MOD(i, 5)
                WHEN 0 THEN 'Home Purchase' WHEN 1 THEN 'Vehicle Purchase' 
                WHEN 2 THEN 'Business Expansion' WHEN 3 THEN 'Debt Consolidation'
                ELSE 'Education'
            END,
            DATE '2023-01-01' + FLOOR(DBMS_RANDOM.VALUE(0, 365)),
            2006, -- Loan Officer Alice Brown
            DATE '2023-01-01' + FLOOR(DBMS_RANDOM.VALUE(0, 365)) + FLOOR(DBMS_RANDOM.VALUE(1, 14)),
            CASE MOD(i, 4)
                WHEN 0 THEN 'APPROVED' WHEN 1 THEN 'APPROVED' 
                WHEN 2 THEN 'PENDING' ELSE 'REJECTED'
            END,
            'Standard application processing',
            ROUND(DBMS_RANDOM.VALUE(0.2, 0.4), 4),
            CASE WHEN MOD(i-1, 5) + 1 IN (2, 3, 4) THEN FLOOR(DBMS_RANDOM.VALUE(50000, 500000)) ELSE NULL END
        );
        v_counter := v_counter + 1;
    END LOOP;
    
    DBMS_OUTPUT.PUT_LINE('✓ Inserted ' || v_counter || ' loan applications');
END;
/
COMMIT;

-- 7. Insert Loans (from approved applications)
DECLARE
    v_counter NUMBER := 0;
BEGIN
    FOR rec IN (SELECT application_id, customer_id, loan_type_id, requested_amount, requested_term_months, processed_by
                FROM loan_applications WHERE status = 'APPROVED') LOOP
        INSERT INTO LOANS (
            loan_id, application_id, customer_id, loan_type_id, principal_amount,
            interest_rate, term_months, monthly_payment, disbursement_date, maturity_date,
            current_balance, payments_made, last_payment_date, next_payment_due,
            status, created_by
        ) VALUES (
            seq_loans.nextval,
            rec.application_id,
            rec.customer_id,
            rec.loan_type_id,
            rec.requested_amount,
            0.06 + ROUND(DBMS_RANDOM.VALUE(-0.01, 0.02), 4),
            rec.requested_term_months,
            ROUND(rec.requested_amount * 0.06 / 12 * 1.2, 2),
            DATE '2023-06-01' + FLOOR(DBMS_RANDOM.VALUE(0, 180)),
            ADD_MONTHS(DATE '2023-06-01' + FLOOR(DBMS_RANDOM.VALUE(0, 180)), rec.requested_term_months),
            rec.requested_amount - FLOOR(DBMS_RANDOM.VALUE(0, rec.requested_amount * 0.3)),
            FLOOR(DBMS_RANDOM.VALUE(0, 12)),
            DATE '2024-01-01' + FLOOR(DBMS_RANDOM.VALUE(0, 60)),
            ADD_MONTHS(DATE '2024-01-01' + FLOOR(DBMS_RANDOM.VALUE(0, 60)), 1),
            'ACTIVE',
            rec.processed_by
        );
        v_counter := v_counter + 1;
    END LOOP;
    
    DBMS_OUTPUT.PUT_LINE('✓ Inserted ' || v_counter || ' loans');
END;
/
COMMIT;

-- 8. Insert Transactions (500 transactions)
DECLARE
    v_counter NUMBER := 0;
    v_transaction_types SYS.ODCIVARCHAR2LIST := SYS.ODCIVARCHAR2LIST(
        'DEPOSIT', 'WITHDRAWAL', 'TRANSFER', 'PAYMENT', 'ATM_WITHDRAWAL', 'DIRECT_DEPOSIT'
    );
BEGIN
    FOR i IN 1..500 LOOP
        INSERT INTO TRANSACTIONS (
            transaction_id, account_id, transaction_date, transaction_type, amount,
            balance_after, description, reference_number, processed_by, branch_id,
            status, fee_amount
        ) VALUES (
            seq_transactions.nextval,
            1001 + MOD(i-1, 200),
            DATE '2024-01-01' + FLOOR(DBMS_RANDOM.VALUE(0, 365)),
            v_transaction_types(MOD(i-1, 6) + 1),
            CASE WHEN v_transaction_types(MOD(i-1, 6) + 1) IN ('WITHDRAWAL', 'PAYMENT', 'ATM_WITHDRAWAL') 
                 THEN -ROUND(DBMS_RANDOM.VALUE(50, 2000), 2) 
                 ELSE ROUND(DBMS_RANDOM.VALUE(100, 5000), 2) END,
            ROUND(DBMS_RANDOM.VALUE(1000, 25000), 2),
            'Transaction processed',
            'TXN-' || TO_CHAR(SYSDATE, 'YYYYMMDD') || '-' || LPAD(TO_CHAR(i), 5, '0'),
            2007, -- Teller Bob Davis
            100 + MOD(i-1, 5),
            'COMPLETED',
            CASE WHEN v_transaction_types(MOD(i-1, 6) + 1) = 'ATM_WITHDRAWAL' THEN 2.50 ELSE 0 END
        );
        v_counter := v_counter + 1;
    END LOOP;
    
    DBMS_OUTPUT.PUT_LINE('✓ Inserted ' || v_counter || ' transactions');
END;
/
COMMIT;

-- 9. Insert Payments (for active loans)
DECLARE
    v_counter NUMBER := 0;
BEGIN
    FOR rec IN (SELECT loan_id, monthly_payment FROM loans WHERE status = 'ACTIVE') LOOP
        FOR i IN 1..FLOOR(DBMS_RANDOM.VALUE(1, 12)) LOOP
            INSERT INTO PAYMENTS (
                payment_id, loan_id, payment_date, scheduled_amount, actual_amount,
                principal_portion, interest_portion, late_fee, payment_method,
                transaction_id, processed_by, status, notes
            ) VALUES (
                seq_payments.nextval,
                rec.loan_id,
                DATE '2024-01-01' + (i * 30) + FLOOR(DBMS_RANDOM.VALUE(-3, 4)),
                rec.monthly_payment,
                rec.monthly_payment + ROUND(DBMS_RANDOM.VALUE(-20, 21), 2),
                rec.monthly_payment * 0.7,
                rec.monthly_payment * 0.3,
                CASE WHEN MOD(i, 8) = 7 THEN 25.00 ELSE 0 END,
                CASE MOD(i, 3) WHEN 0 THEN 'AUTO_PAY' WHEN 1 THEN 'ONLINE' ELSE 'CHECK' END,
                'PAY-' || TO_CHAR(SYSDATE, 'YYYYMMDD') || '-' || LPAD(TO_CHAR(v_counter + 1), 5, '0'),
                2009, -- Customer Service Rep
                'COMPLETED',
                'Regular payment processed'
            );
            v_counter := v_counter + 1;
        END LOOP;
    END LOOP;
    
    DBMS_OUTPUT.PUT_LINE('✓ Inserted ' || v_counter || ' payments');
END;
/
COMMIT;

-- 10. Insert Credit Reports (80 reports)
DECLARE
    v_counter NUMBER := 0;
    v_providers SYS.ODCIVARCHAR2LIST := SYS.ODCIVARCHAR2LIST('Experian', 'Equifax', 'TransUnion');
BEGIN
    FOR i IN 1..80 LOOP
        INSERT INTO CREDIT_REPORTS (
            report_id, customer_id, report_date, credit_score, report_provider,
            tradelines_count, total_debt, available_credit, payment_history_score,
            credit_utilization, length_of_history_months, new_credit_accounts,
            credit_mix_score, report_data
        ) VALUES (
            seq_credit_reports.nextval,
            MOD(i-1, 100) + 1,
            DATE '2024-01-01' + FLOOR(DBMS_RANDOM.VALUE(0, 365)),
            FLOOR(DBMS_RANDOM.VALUE(600, 800)),
            v_providers(MOD(i-1, 3) + 1),
            FLOOR(DBMS_RANDOM.VALUE(5, 15)),
            ROUND(DBMS_RANDOM.VALUE(10000, 100000), 2),
            ROUND(DBMS_RANDOM.VALUE(20000, 150000), 2),
            FLOOR(DBMS_RANDOM.VALUE(700, 800)),
            ROUND(DBMS_RANDOM.VALUE(0.1, 0.7), 4),
            FLOOR(DBMS_RANDOM.VALUE(24, 240)),
            FLOOR(DBMS_RANDOM.VALUE(0, 4)),
            FLOOR(DBMS_RANDOM.VALUE(700, 800)),
            '{"creditScore":' || FLOOR(DBMS_RANDOM.VALUE(600, 800)) || 
            ',"accounts":[{"type":"creditCard","balance":' || FLOOR(DBMS_RANDOM.VALUE(2000, 10000)) || '}]}'
        );
        v_counter := v_counter + 1;
    END LOOP;
    
    DBMS_OUTPUT.PUT_LINE('✓ Inserted ' || v_counter || ' credit reports');
END;
/
COMMIT;

-- Final Summary
BEGIN
    DBMS_OUTPUT.PUT_LINE('');
    DBMS_OUTPUT.PUT_LINE('🎉 Banking Database Population Complete!');
    DBMS_OUTPUT.PUT_LINE('=====================================');
    
    FOR rec IN (
        SELECT 'BRANCHES' as table_name, COUNT(*) as record_count FROM branches
        UNION ALL
        SELECT 'EMPLOYEES' as table_name, COUNT(*) as record_count FROM employees
        UNION ALL
        SELECT 'CUSTOMERS' as table_name, COUNT(*) as record_count FROM customers
        UNION ALL
        SELECT 'ACCOUNTS' as table_name, COUNT(*) as record_count FROM accounts
        UNION ALL
        SELECT 'LOAN_TYPES' as table_name, COUNT(*) as record_count FROM loan_types
        UNION ALL
        SELECT 'LOAN_APPLICATIONS' as table_name, COUNT(*) as record_count FROM loan_applications
        UNION ALL
        SELECT 'LOANS' as table_name, COUNT(*) as record_count FROM loans
        UNION ALL
        SELECT 'TRANSACTIONS' as table_name, COUNT(*) as record_count FROM transactions
        UNION ALL
        SELECT 'PAYMENTS' as table_name, COUNT(*) as record_count FROM payments
        UNION ALL
        SELECT 'CREDIT_REPORTS' as table_name, COUNT(*) as record_count FROM credit_reports
    ) LOOP
        DBMS_OUTPUT.PUT_LINE('✓ ' || RPAD(rec.table_name, 20) || ': ' || LPAD(TO_CHAR(rec.record_count, '999'), 4) || ' records');
    END LOOP;
    
    DBMS_OUTPUT.PUT_LINE('');
    DBMS_OUTPUT.PUT_LINE('Ready for comprehensive banking analytics testing!');
    DBMS_OUTPUT.PUT_LINE('Completed at: ' || TO_CHAR(SYSDATE, 'YYYY-MM-DD HH24:MI:SS'));
END;
/
