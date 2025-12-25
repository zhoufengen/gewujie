-- Fill UUIDs for existing users
UPDATE users SET uuid = gen_random_uuid()::text WHERE uuid IS NULL;

-- Verify all users have UUIDs
SELECT COUNT(*) as total, COUNT(uuid) as with_uuid FROM users;

-- Check for any null UUIDs
SELECT id, nickname, uuid FROM users WHERE uuid IS NULL;
