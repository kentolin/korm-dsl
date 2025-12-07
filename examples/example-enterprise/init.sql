-- korm-dsl/examples/example-enterprise/init.sql

-- Enable extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_stat_statements";

-- Create read-only user
CREATE USER korm_readonly WITH PASSWORD 'readonly';
GRANT CONNECT ON DATABASE korm_enterprise TO korm_readonly;
GRANT USAGE ON SCHEMA public TO korm_readonly;
GRANT SELECT ON ALL TABLES IN SCHEMA public TO korm_readonly;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT ON TABLES TO korm_readonly;
