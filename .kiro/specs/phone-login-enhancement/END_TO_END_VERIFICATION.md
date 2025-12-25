# End-to-End Verification Guide

## Overview
This document provides a checklist for verifying the phone login enhancement implementation.

## Database Verification

### 1. Run Database Migration
```bash
# Connect to PostgreSQL database
psql -U <username> -d <database_name>

# Run migration script
\i sql/migration_add_uuid_phone.sql

# Verify columns were added
\d users
```

**Expected Result:**
- `uuid` column exists (VARCHAR(36), NOT NULL, UNIQUE)
- `phone` column exists (VARCHAR(20), nullable)

### 2. Verify Existing Data Migration
```sql
-- Check that all users have UUIDs
SELECT COUNT(*) as total, COUNT(uuid) as with_uuid FROM users;

-- Check UUID format
SELECT id, uuid, length(uuid) as uuid_length 
FROM users 
LIMIT 5;

-- Check phone population from user_auths
SELECT u.id, u.phone, ua.identifier 
FROM users u 
LEFT JOIN user_auths ua ON u.id = ua.user_id AND ua.identity_type = 'PHONE'
LIMIT 5;
```

**Expected Result:**
- All users have UUIDs (total = with_uuid)
- All UUIDs are 36 characters long
- Phone numbers match identifiers from user_auths where applicable

## Backend Verification

### 1. Build Backend
```bash
cd zibian-backend
mvn clean install
```

**Expected Result:**
- Build succeeds without errors
- All tests pass

### 2. Run Backend Tests
```bash
mvn test
```

**Expected Result:**
- All unit tests pass
- All property-based tests pass
- All integration tests pass

### 3. Start Backend Server
```bash
mvn spring-boot:run
```

**Expected Result:**
- Server starts on configured port
- No startup errors
- Database connection successful

### 4. Test API Endpoints

#### Test Login Endpoint
```bash
# Send verification code
curl -X POST "http://localhost:8080/api/auth/send-code?phone=13800138000"

# Login with code
curl -X POST "http://localhost:8080/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"phone":"13800138000","code":"123456"}'
```

**Expected Response:**
```json
{
  "id": 1,
  "uuid": "550e8400-e29b-41d4-a716-446655440000",
  "nickname": "User_8000",
  "phone": "13800138000",
  "avatar": null,
  "isVip": false,
  "createdAt": "2024-12-25T10:00:00",
  "updatedAt": "2024-12-25T10:00:00"
}
```

**Verify:**
- ✅ Response includes `uuid` field
- ✅ Response includes `phone` field
- ✅ UUID is in correct format (36 chars, with dashes)
- ✅ Phone matches the login phone
- ✅ All other fields are present

#### Test Third-Party Login
```bash
curl -X GET "http://localhost:8080/api/auth/callback/wechat?code=test123"
```

**Expected Response:**
```json
{
  "id": 2,
  "uuid": "550e8400-e29b-41d4-a716-446655440001",
  "nickname": "WECHAT_User",
  "phone": null,
  "avatar": null,
  "isVip": false,
  "createdAt": "2024-12-25T10:00:00",
  "updatedAt": "2024-12-25T10:00:00"
}
```

**Verify:**
- ✅ Response includes `uuid` field
- ✅ `phone` field is null (third-party auth)
- ✅ UUID is generated correctly

## Frontend Verification

### 1. Install Dependencies
```bash
cd zibian
npm install
```

**Expected Result:**
- All dependencies install successfully
- No peer dependency warnings

### 2. Run Frontend Tests
```bash
npm test
```

**Expected Result:**
- All UserStore tests pass
- UUID storage tests pass
- Phone storage tests pass
- Logout tests pass

### 3. Start Frontend Development Server
```bash
npm run dev
```

**Expected Result:**
- Development server starts
- No compilation errors
- Application accessible at configured URL

### 4. Manual UI Testing

#### Test Login Flow
1. Navigate to login page
2. Enter phone number: `13800138000`
3. Click "获取" to send verification code
4. Enter verification code
5. Click "一键登录"

**Expected Behavior:**
- ✅ Verification code sent successfully
- ✅ Login succeeds
- ✅ Redirects to home page
- ✅ User data stored in localStorage
- ✅ UUID stored in localStorage
- ✅ Phone stored in localStorage

#### Verify LocalStorage
Open browser DevTools → Application → Local Storage

**Expected Data:**
```json
{
  "user": {
    "isLoggedIn": true,
    "userId": 1,
    "uuid": "550e8400-e29b-41d4-a716-446655440000",
    "username": "User_8000",
    "phone": "13800138000",
    "isVip": false
  }
}
```

**Verify:**
- ✅ `uuid` field is present and populated
- ✅ `phone` field is present and populated
- ✅ Data persists after page refresh

#### Test Logout Flow
1. Click logout button
2. Check localStorage

**Expected Behavior:**
- ✅ User logged out
- ✅ UUID cleared from store
- ✅ Phone cleared from store
- ✅ Redirects to login page

## Integration Testing

### Test Complete User Journey

1. **New User Registration via Phone**
   - Send code to new phone number
   - Login with code
   - Verify user created in database with UUID and phone
   - Verify frontend stores UUID and phone

2. **Existing User Login**
   - Login with existing phone number
   - Verify UUID returned in response
   - Verify phone returned in response
   - Verify frontend updates stored data

3. **Phone Number Update**
   - User logs in with different phone (same user_auth)
   - Verify phone updated in users table
   - Verify frontend reflects new phone

4. **Third-Party User**
   - Login via WeChat callback
   - Verify user created with UUID but no phone
   - Verify frontend handles null phone gracefully

## Rollback Testing

### Test Rollback Script
```bash
# Connect to database
psql -U <username> -d <database_name>

# Run rollback
\i sql/rollback_uuid_phone.sql

# Verify columns removed
\d users
```

**Expected Result:**
- `uuid` column removed
- `phone` column removed
- Other columns intact

### Re-run Migration
```bash
# Run migration again
\i sql/migration_add_uuid_phone.sql

# Verify columns added back
\d users
```

**Expected Result:**
- Columns added successfully
- Data integrity maintained

## Performance Testing

### Test UUID Generation Performance
```sql
-- Create 1000 test users
DO $$
BEGIN
  FOR i IN 1..1000 LOOP
    INSERT INTO users (nickname, created_at, updated_at)
    VALUES ('PerfTest' || i, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
  END LOOP;
END $$;

-- Verify all have UUIDs
SELECT COUNT(*) as total, COUNT(uuid) as with_uuid 
FROM users 
WHERE nickname LIKE 'PerfTest%';

-- Check for duplicates
SELECT uuid, COUNT(*) 
FROM users 
WHERE nickname LIKE 'PerfTest%'
GROUP BY uuid 
HAVING COUNT(*) > 1;

-- Cleanup
DELETE FROM users WHERE nickname LIKE 'PerfTest%';
```

**Expected Result:**
- All users have UUIDs
- No duplicate UUIDs
- Generation completes quickly (< 1 second)

## Security Testing

### Test UUID Uniqueness Constraint
```sql
-- Try to insert duplicate UUID (should fail)
INSERT INTO users (uuid, nickname) 
VALUES ('550e8400-e29b-41d4-a716-446655440000', 'DuplicateTest');
```

**Expected Result:**
- Insert fails with unique constraint violation

### Test NULL UUID Constraint
```sql
-- Try to insert user without UUID (should fail after constraint added)
INSERT INTO users (nickname) VALUES ('NullUUIDTest');
```

**Expected Result:**
- Insert fails with NOT NULL constraint violation (after migration)
- OR UUID is auto-generated by @PrePersist (before constraint)

## Checklist Summary

### Database
- [ ] Migration script runs successfully
- [ ] UUID column added with correct constraints
- [ ] Phone column added
- [ ] Existing users have UUIDs generated
- [ ] Phone numbers populated from user_auths
- [ ] Rollback script works correctly

### Backend
- [ ] User entity updated with uuid and phone fields
- [ ] @PrePersist generates UUIDs automatically
- [ ] UserService sets phone on registration
- [ ] UserService updates phone on login
- [ ] All unit tests pass
- [ ] All property-based tests pass
- [ ] All integration tests pass
- [ ] API returns uuid in login response
- [ ] API returns phone in login response
- [ ] Third-party auth allows null phone

### Frontend
- [ ] UserStore updated with uuid field
- [ ] Login stores uuid from API response
- [ ] Login stores phone from API response
- [ ] UUID persists in localStorage
- [ ] Logout clears uuid
- [ ] All tests pass
- [ ] Login page works correctly
- [ ] No console errors

### End-to-End
- [ ] New user registration creates UUID and phone
- [ ] Existing user login returns UUID and phone
- [ ] Phone update works correctly
- [ ] Third-party login works with null phone
- [ ] Data persists across page refreshes
- [ ] Logout clears all data correctly

## Troubleshooting

### Issue: Migration fails with "column already exists"
**Solution:** Run rollback script first, then re-run migration

### Issue: Tests fail with "UUID is null"
**Solution:** Ensure @PrePersist is being called. Check JPA configuration.

### Issue: Frontend doesn't store UUID
**Solution:** Check API response format. Verify UserStore login method.

### Issue: Phone not populated for existing users
**Solution:** Check user_auths table has correct data. Re-run migration step 3.

## Conclusion

Once all items in the checklist are verified, the phone login enhancement is complete and ready for production deployment.
