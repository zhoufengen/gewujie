-- Rollback: Remove UUID and Phone fields from users table
-- Date: 2024-12-25
-- Description: Removes uuid and phone columns from users table

-- Remove constraints first
ALTER TABLE users DROP CONSTRAINT IF EXISTS users_uuid_unique;

-- Remove columns
ALTER TABLE users DROP COLUMN IF EXISTS uuid;
ALTER TABLE users DROP COLUMN IF EXISTS phone;

-- Verify rollback
SELECT column_name 
FROM information_schema.columns 
WHERE table_name = 'users';
