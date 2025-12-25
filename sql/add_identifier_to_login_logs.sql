-- Add identifier column to login_logs table
-- This will store the login identifier (phone number, openId, etc.)

ALTER TABLE login_logs 
ADD COLUMN IF NOT EXISTS identifier VARCHAR(100);

-- Add index for faster queries
CREATE INDEX IF NOT EXISTS idx_login_logs_identifier 
ON login_logs(identifier);

-- Verify the change
SELECT column_name, data_type, character_maximum_length 
FROM information_schema.columns 
WHERE table_name = 'login_logs' 
ORDER BY ordinal_position;

-- Show sample data
SELECT id, user_id, login_type, identifier, terminal, ip, login_time 
FROM login_logs 
ORDER BY login_time DESC 
LIMIT 10;
