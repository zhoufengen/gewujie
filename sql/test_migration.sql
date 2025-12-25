-- Test Migration Script
-- This script creates test data, runs the migration, and verifies results

-- Create test data (users without uuid/phone)
BEGIN;

-- Insert test users
INSERT INTO users (nickname, avatar, is_vip, created_at, updated_at) 
VALUES 
  ('TestUser1', NULL, FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('TestUser2', NULL, FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('TestUser3', NULL, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Get the IDs of inserted users
DO $$
DECLARE
    user1_id BIGINT;
    user2_id BIGINT;
    user3_id BIGINT;
BEGIN
    SELECT id INTO user1_id FROM users WHERE nickname = 'TestUser1';
    SELECT id INTO user2_id FROM users WHERE nickname = 'TestUser2';
    SELECT id INTO user3_id FROM users WHERE nickname = 'TestUser3';
    
    -- Insert user_auths for phone users
    INSERT INTO user_auths (user_id, identity_type, identifier, created_at)
    VALUES 
      (user1_id, 'PHONE', '13800138001', CURRENT_TIMESTAMP),
      (user2_id, 'PHONE', '13800138002', CURRENT_TIMESTAMP),
      (user3_id, 'WECHAT', 'wx_test123', CURRENT_TIMESTAMP);
END $$;

COMMIT;

-- Display before migration
SELECT 'BEFORE MIGRATION:' as status;
SELECT id, nickname, 
       CASE WHEN uuid IS NULL THEN 'NULL' ELSE uuid END as uuid,
       CASE WHEN phone IS NULL THEN 'NULL' ELSE phone END as phone
FROM users 
WHERE nickname LIKE 'TestUser%'
ORDER BY id;

-- Run migration (inline for testing)
-- Add columns if they don't exist
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'users' AND column_name = 'uuid') THEN
        ALTER TABLE users ADD COLUMN uuid VARCHAR(36);
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'users' AND column_name = 'phone') THEN
        ALTER TABLE users ADD COLUMN phone VARCHAR(20);
    END IF;
END $$;

-- Generate UUIDs
UPDATE users SET uuid = gen_random_uuid()::text WHERE uuid IS NULL;

-- Populate phone numbers
UPDATE users u
SET phone = ua.identifier
FROM user_auths ua
WHERE u.id = ua.user_id 
  AND ua.identity_type = 'PHONE'
  AND u.phone IS NULL;

-- Add constraints if they don't exist
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints 
                   WHERE constraint_name = 'users_uuid_unique') THEN
        ALTER TABLE users ADD CONSTRAINT users_uuid_unique UNIQUE (uuid);
    END IF;
END $$;

-- Note: NOT NULL constraint will be added after verification in production

-- Display after migration
SELECT 'AFTER MIGRATION:' as status;
SELECT id, nickname, 
       CASE WHEN uuid IS NULL THEN 'NULL' ELSE 'UUID-' || substring(uuid, 1, 8) END as uuid_preview,
       CASE WHEN phone IS NULL THEN 'NULL' ELSE phone END as phone
FROM users 
WHERE nickname LIKE 'TestUser%'
ORDER BY id;

-- Verification checks
SELECT 'VERIFICATION:' as status;
SELECT 
    COUNT(*) as total_test_users,
    COUNT(uuid) as users_with_uuid,
    COUNT(DISTINCT uuid) as unique_uuids,
    COUNT(phone) as users_with_phone
FROM users 
WHERE nickname LIKE 'TestUser%';

-- Check UUID format (should be 36 characters with dashes)
SELECT 'UUID FORMAT CHECK:' as status;
SELECT id, nickname,
       length(uuid) as uuid_length,
       CASE 
           WHEN uuid ~ '^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$' 
           THEN 'VALID' 
           ELSE 'INVALID' 
       END as uuid_format
FROM users 
WHERE nickname LIKE 'TestUser%'
ORDER BY id;

-- Cleanup test data
DELETE FROM user_auths WHERE identifier LIKE '1380013800%' OR identifier = 'wx_test123';
DELETE FROM users WHERE nickname LIKE 'TestUser%';

SELECT 'TEST COMPLETED - Test data cleaned up' as status;
