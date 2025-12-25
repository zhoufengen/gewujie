-- Fix existing users without UUID
UPDATE users 
SET user_uuid = gen_random_uuid()::text 
WHERE user_uuid IS NULL;

-- Verify
SELECT id, nickname, phone, 
       CASE WHEN user_uuid IS NULL THEN 'NULL' ELSE substring(user_uuid, 1, 8) || '...' END as uuid_preview
FROM users
ORDER BY id;

-- Count
SELECT 
    COUNT(*) as total_users,
    COUNT(user_uuid) as users_with_uuid,
    COUNT(*) - COUNT(user_uuid) as users_without_uuid
FROM users;
