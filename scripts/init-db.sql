-- Database initialization script
-- This script runs when the database is first created

-- Create extensions if needed
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Create indexes for better performance
-- These will be created after the tables exist (via JPA)

-- Grant permissions
GRANT ALL PRIVILEGES ON DATABASE mydatabase TO myuser;

-- You can add more initialization SQL here
-- For example: creating additional users, schemas, etc.
