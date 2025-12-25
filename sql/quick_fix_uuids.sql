-- Quick fix: Generate UUIDs for all existing users without one
-- Run this script to immediately fix all existing users in the database

UPDATE users 
SET user_uuid = gen_random_uuid()::text 
WHERE user_uuid IS NULL;

-- Show results
SELECT 
    id, 
    nickname, 
    phone,
    CASE 
        WHEN user_uuid IS NULL THEN 'NULL' 
        ELSE substring(user_uuid, 1, 13) || '...' 
    END as uuid_preview
FROM users
ORDER BY id DESC
LIMIT 10;

-- Summary
SELECT 
    COUNT(*) as total_users,
    COUNT(user_uuid) as users_with_uuid,
    COUNT(*) - COUNT(user_uuid) as users_without_uuid
FROM users;
