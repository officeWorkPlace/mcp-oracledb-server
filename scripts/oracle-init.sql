-- Oracle Database Initialization Script for MCP Server
-- Creates necessary users, tablespaces, and initial setup

-- Create MCP user with appropriate privileges
CREATE USER mcpuser IDENTIFIED BY mcppassword
DEFAULT TABLESPACE USERS
TEMPORARY TABLESPACE TEMP
QUOTA UNLIMITED ON USERS;

-- Grant necessary privileges
GRANT CONNECT, RESOURCE TO mcpuser;
GRANT CREATE SESSION TO mcpuser;
GRANT CREATE TABLE TO mcpuser;
GRANT CREATE VIEW TO mcpuser;
GRANT CREATE PROCEDURE TO mcpuser;
GRANT CREATE SEQUENCE TO mcpuser;
GRANT CREATE TRIGGER TO mcpuser;

-- Grant additional privileges for MCP operations
GRANT SELECT ANY DICTIONARY TO mcpuser;
GRANT SELECT ON V_$DATABASE TO mcpuser;
GRANT SELECT ON V_$INSTANCE TO mcpuser;
GRANT SELECT ON V_$SESSION TO mcpuser;
GRANT SELECT ON DBA_TABLES TO mcpuser;
GRANT SELECT ON DBA_USERS TO mcpuser;
GRANT SELECT ON DBA_TABLESPACES TO mcpuser;
GRANT SELECT ON DBA_DATA_FILES TO mcpuser;

-- Create sample table for testing
CREATE TABLE mcpuser.sample_data (
    id NUMBER PRIMARY KEY,
    name VARCHAR2(100) NOT NULL,
    description CLOB,
    created_date DATE DEFAULT SYSDATE,
    updated_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Insert sample data
INSERT INTO mcpuser.sample_data (id, name, description) VALUES 
(1, 'Oracle MCP Server', 'Production-ready Oracle MCP server implementation');
INSERT INTO mcpuser.sample_data (id, name, description) VALUES 
(2, 'Enhanced Edition', 'Includes 55+ Oracle-specific tools');
INSERT INTO mcpuser.sample_data (id, name, description) VALUES 
(3, 'Enterprise Edition', 'Includes 75+ tools with enterprise features');

COMMIT;

-- Display completion message
SELECT 'Oracle MCP Server database initialization completed successfully' AS message FROM dual;
