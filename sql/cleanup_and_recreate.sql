-- Clean up old uuid column if exists
ALTER TABLE users DROP COLUMN IF EXISTS uuid;

-- The application will create user_uuid column on restart
-- If you want to manually create it:
-- ALTER TABLE users ADD COLUMN user_uuid VARCHAR(36);
-- UPDATE users SET user_uuid = gen_random_uuid()::text WHERE user_uuid IS NULL;
-- ALTER TABLE users ALTER COLUMN user_uuid SET NOT NULL;
-- ALTER TABLE users ADD CONSTRAINT users_user_uuid_unique UNIQUE (user_uuid);
