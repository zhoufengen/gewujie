# Design Document

## Overview

This design document outlines the implementation approach for enhancing the phone login system by adding UUID and phone number fields to the users table and related application components. The enhancement involves database schema changes, Java model updates, service layer modifications, and frontend adjustments to properly handle the new fields.

## Architecture

The system follows a layered architecture:

1. **Database Layer**: PostgreSQL database with users and user_auths tables
2. **Persistence Layer**: JPA repositories for data access
3. **Service Layer**: Business logic for user management and authentication
4. **Controller Layer**: REST API endpoints for authentication
5. **Frontend Layer**: Vue.js application with Pinia state management

The enhancement will touch all layers:
- Database: Add columns to users table
- Model: Update User entity with new fields
- Service: Modify user creation and login logic to handle new fields
- Controller: Ensure API responses include new fields
- Frontend: Update UserStore to handle UUID and phone from API response

## Components and Interfaces

### Database Schema Changes

**users table modifications:**
```sql
ALTER TABLE users ADD COLUMN uuid VARCHAR(36) UNIQUE NOT NULL;
ALTER TABLE users ADD COLUMN phone VARCHAR(20);
```

**Migration strategy:**
- Add columns as nullable first
- Generate UUIDs for existing records
- Populate phone numbers from user_auths table
- Add NOT NULL constraint to uuid
- Add UNIQUE constraint to uuid

### User Entity (Java Model)

**New fields:**
```java
@Column(unique = true, nullable = false, length = 36)
private String uuid;

@Column(length = 20)
private String phone;
```

**UUID generation:**
- Use `@PrePersist` callback to generate UUID before insertion
- Use `java.util.UUID.randomUUID().toString()`

### UserService Modifications

**registerUser method:**
- Generate UUID automatically via @PrePersist
- Extract phone number from identifier when identityType is "PHONE"
- Set phone field on User entity

**loginByPhone method:**
- Update user's phone field if not set or changed
- Return complete User entity with all fields

### API Response

**Login endpoint response:**
```json
{
  "id": 1,
  "uuid": "550e8400-e29b-41d4-a716-446655440000",
  "nickname": "User_1234",
  "phone": "13800138000",
  "avatar": null,
  "isVip": false,
  "createdAt": "2024-01-01T00:00:00",
  "updatedAt": "2024-01-01T00:00:00"
}
```

### Frontend UserStore

**Updated state:**
```typescript
const uuid = ref<string | null>(null)
const phone = ref<string>('')
```

**Login method updates:**
- Store uuid from API response
- Store phone from API response (not from input)
- Persist uuid in localStorage via Pinia persist plugin

## Data Models

### User Entity (Complete)

```java
@Entity
@Table(name = "users")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false, length = 36)
    private String uuid;
    
    private String nickname;
    
    @Column(length = 20)
    private String phone;
    
    private String avatar;
    
    private Boolean isVip = false;
    
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    @PrePersist
    protected void onCreate() {
        if (uuid == null) {
            uuid = UUID.randomUUID().toString();
        }
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
```

### Database Migration Script

```sql
-- Add new columns
ALTER TABLE users ADD COLUMN uuid VARCHAR(36);
ALTER TABLE users ADD COLUMN phone VARCHAR(20);

-- Generate UUIDs for existing users
UPDATE users SET uuid = gen_random_uuid()::text WHERE uuid IS NULL;

-- Populate phone numbers from user_auths
UPDATE users u
SET phone = ua.identifier
FROM user_auths ua
WHERE u.id = ua.user_id 
  AND ua.identity_type = 'PHONE'
  AND u.phone IS NULL;

-- Add constraints
ALTER TABLE users ALTER COLUMN uuid SET NOT NULL;
ALTER TABLE users ADD CONSTRAINT users_uuid_unique UNIQUE (uuid);
```

## Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system-essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*


### Property 1: UUID generation uniqueness
*For any* set of newly created users, each user should have a unique UUID that is non-null and follows the standard UUID format (8-4-4-4-12 hexadecimal pattern)
**Validates: Requirements 1.1, 1.4**

### Property 2: UUID persistence round-trip
*For any* user created and saved to the database, retrieving that user should return the same UUID value
**Validates: Requirements 1.2, 1.3**

### Property 3: Phone number storage on registration
*For any* valid phone number used during registration, the resulting user record should have that phone number stored in the users table
**Validates: Requirements 2.1**

### Property 4: Phone number update on login
*For any* existing user, if they login with a different phone number than currently stored, the stored phone number should be updated to the new value
**Validates: Requirements 2.2**

### Property 5: Phone number retrieval
*For any* user with a phone number, retrieving that user should include the phone number in the User Entity
**Validates: Requirements 2.3**

### Property 6: Null phone for third-party auth
*For any* user registered via third-party authentication (WECHAT, ALIPAY), the phone field should be allowed to be null
**Validates: Requirements 2.4**

### Property 7: Login response completeness
*For any* successful login, the API response should contain all user profile fields including id, uuid, nickname, phone (if available), avatar, isVip, createdAt, and updatedAt
**Validates: Requirements 3.1, 3.2, 3.3**

### Property 8: JSON serialization completeness
*For any* User entity, serializing it to JSON should include all fields without omission
**Validates: Requirements 3.4**

### Property 9: Automatic UUID generation
*For any* new User entity being persisted, if it lacks a UUID, the system should automatically generate one before database insertion
**Validates: Requirements 5.1, 5.2, 5.4**

### Property 10: UUID format compliance
*For any* generated UUID, it should match the standard UUID format specification
**Validates: Requirements 5.3**

## Error Handling

### UUID Generation Failures
- If UUID generation fails (extremely rare), log the error and retry
- If retry fails, throw a runtime exception to prevent user creation without UUID
- Never allow a user to be persisted without a valid UUID

### Database Constraint Violations
- If UUID uniqueness constraint is violated, catch the exception and regenerate UUID
- If phone number format is invalid, reject the registration with clear error message
- Return appropriate HTTP status codes (400 for validation errors, 500 for server errors)

### Migration Errors
- Wrap migration in transaction to allow rollback on failure
- Validate that all existing users have UUIDs after migration
- Log any users that couldn't have phone numbers populated
- Provide rollback script in case migration needs to be reverted

### Null Handling
- Handle null phone numbers gracefully in API responses
- Ensure frontend can handle missing phone field
- Don't attempt to update phone to null on login

## Testing Strategy

### Unit Testing

We will use JUnit 5 for unit testing the Java backend components.

**User Entity Tests:**
- Test UUID generation on entity creation
- Test @PrePersist callback execution
- Test @PreUpdate callback for updatedAt field
- Test that UUID format is valid
- Test that phone field accepts null values

**UserService Tests:**
- Test registerUser creates user with UUID
- Test registerUser sets phone for PHONE identity type
- Test loginByPhone updates phone if changed
- Test loginByThirdParty allows null phone
- Test that duplicate UUID handling works correctly

**API Integration Tests:**
- Test login endpoint returns complete user object
- Test response includes UUID and phone fields
- Test JSON serialization includes all fields

### Property-Based Testing

We will use JUnit-Quickcheck for property-based testing in Java.

**Configuration:**
- Each property test should run a minimum of 100 iterations
- Use appropriate generators for phone numbers, UUIDs, and user data
- Tag each test with the property number and description

**Property Test Requirements:**
- Each property-based test MUST include a comment with format: `**Feature: phone-login-enhancement, Property {number}: {property_text}**`
- Each property MUST be implemented by a SINGLE property-based test
- Tests should use realistic data generators (valid phone formats, proper UUID formats)

**Generators Needed:**
- Phone number generator (valid Chinese mobile format: 13-19 prefix, 11 digits)
- User data generator (random nicknames, avatars, VIP status)
- Identity type generator (PHONE, WECHAT, ALIPAY)

### Migration Testing

**Migration Test Approach:**
- Create test database with sample data (users without UUID/phone)
- Run migration script
- Verify all users have UUIDs
- Verify phone numbers populated from user_auths
- Verify constraints are in place
- Test rollback script

### Frontend Testing

**UserStore Tests:**
- Test that login stores UUID from response
- Test that login stores phone from response
- Test that UUID persists in localStorage
- Test that logout clears UUID

## Implementation Notes

### Database Migration Execution

The migration should be executed in the following order:
1. Add columns as nullable
2. Populate data
3. Add constraints

This approach minimizes downtime and allows for validation between steps.

### UUID Library

Use `java.util.UUID` which is part of the Java standard library. No additional dependencies needed.

### Phone Number Validation

While not part of this enhancement, consider adding phone number format validation in a future iteration. For now, we store whatever phone number is provided by the authentication system.

### Backward Compatibility

The API response will include new fields, but this is backward compatible as clients will simply ignore unknown fields. Ensure frontend is updated to use the new fields.

### Performance Considerations

- UUID generation is fast (microseconds)
- Adding indexed UUID column may slightly slow down inserts but improves lookup performance
- Phone column is not indexed initially; add index if phone-based lookups become common
