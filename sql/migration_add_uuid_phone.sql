-- Migration: Add UUID and Phone fields to users table
-- Date: 2024-12-25
-- Description: Adds uuid and phone columns to users table, generates UUIDs for existing users,
--              and populates phone numbers from user_auths table

-- Step 1: Add new columns as nullable
ALTER TABLE users ADD COLUMN uuid VARCHAR(36);
ALTER TABLE users ADD COLUMN phone VARCHAR(20);

-- Step 2: Generate UUIDs for existing users
UPDATE users SET uuid = gen_random_uuid()::text WHERE uuid IS NULL;

-- Step 3: Populate phone numbers from user_auths table
UPDATE users u
SET phone = ua.identifier
FROM user_auths ua
WHERE u.id = ua.user_id 
  AND ua.identity_type = 'PHONE'
  AND u.phone IS NULL;

-- Step 4: Add constraints
ALTER TABLE users ALTER COLUMN uuid SET NOT NULL;
ALTER TABLE users ADD CONSTRAINT users_uuid_unique UNIQUE (uuid);

-- Verify migration
SELECT COUNT(*) as total_users, 
       COUNT(uuid) as users_with_uuid,
       COUNT(phone) as users_with_phone
FROM users;
