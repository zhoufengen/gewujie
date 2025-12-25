-- Complete UUID migration for existing data
-- Run this after restarting the application with nullable user_uuid

-- Step 1: Fill UUIDs for all existing users
UPDATE users SET user_uuid = gen_random_uuid()::text WHERE user_uuid IS NULL;

-- Step 2: Verify all users have UUIDs
SELECT 
    COUNT(*) as total_users,
    COUNT(user_uuid) as users_with_uuid,
    COUNT(*) - COUNT(user_uuid) as users_without_uuid
FROM users;

-- Step 3: Add NOT NULL constraint (only if all users have UUIDs)
ALTER TABLE users ALTER COLUMN user_uuid SET NOT NULL;

-- Step 4: Verify constraint
SELECT 
    column_name, 
    is_nullable, 
    data_type 
FROM information_schema.columns 
WHERE table_name = 'users' 
  AND column_name IN ('user_uuid', 'phone');
