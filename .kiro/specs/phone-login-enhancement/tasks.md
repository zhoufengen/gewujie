# Implementation Plan

- [x] 1. Update database schema
  - Create migration SQL script to add uuid and phone columns to users table
  - Generate UUIDs for existing users
  - Populate phone numbers from user_auths table
  - Add constraints (NOT NULL for uuid, UNIQUE for uuid)
  - _Requirements: 4.1, 4.2, 4.3, 4.4_

- [x] 1.1 Test database migration
  - Create test database with sample data
  - Run migration script and verify results
  - Test rollback script
  - _Requirements: 4.1, 4.2, 4.3, 4.4_

- [x] 2. Update User entity model
  - Add uuid field with @Column annotations (unique, nullable=false, length=36)
  - Add phone field with @Column annotation (length=20)
  - Implement @PrePersist method to generate UUID automatically
  - Implement @PreUpdate method to update updatedAt timestamp
  - _Requirements: 1.1, 1.2, 2.1, 2.3, 5.1, 5.2, 5.4_

- [x] 2.1 Write property test for UUID generation uniqueness
  - **Property 1: UUID generation uniqueness**
  - **Validates: Requirements 1.1, 1.4**
  - Generate multiple users and verify each has unique, valid UUID
  - _Requirements: 1.1, 1.4_

- [x] 2.2 Write property test for UUID persistence
  - **Property 2: UUID persistence round-trip**
  - **Validates: Requirements 1.2, 1.3**
  - Create user, save, retrieve, and verify UUID matches
  - _Requirements: 1.2, 1.3_

- [x] 2.3 Write property test for UUID format compliance
  - **Property 10: UUID format compliance**
  - **Validates: Requirements 5.3**
  - Verify generated UUIDs match standard format
  - _Requirements: 5.3_

- [x] 2.4 Write unit tests for User entity
  - Test @PrePersist callback execution
  - Test @PreUpdate callback execution
  - Test phone field accepts null values
  - _Requirements: 1.1, 2.4, 5.1_

- [x] 3. Update UserService for phone number handling
  - Modify registerUser method to extract and set phone number when identityType is "PHONE"
  - Modify loginByPhone method to update user's phone field if not set or changed
  - Ensure third-party registration allows null phone
  - _Requirements: 2.1, 2.2, 2.4_

- [x] 3.1 Write property test for phone storage on registration
  - **Property 3: Phone number storage on registration**
  - **Validates: Requirements 2.1**
  - Register users with various phone numbers and verify storage
  - _Requirements: 2.1_

- [x] 3.2 Write property test for phone update on login
  - **Property 4: Phone number update on login**
  - **Validates: Requirements 2.2**
  - Login with different phone and verify update
  - _Requirements: 2.2_

- [x] 3.3 Write property test for null phone with third-party auth
  - **Property 6: Null phone for third-party auth**
  - **Validates: Requirements 2.4**
  - Register via third-party and verify phone can be null
  - _Requirements: 2.4_

- [x] 3.4 Write unit tests for UserService
  - Test registerUser creates user with UUID
  - Test registerUser sets phone for PHONE identity
  - Test loginByPhone updates phone correctly
  - Test loginByThirdParty allows null phone
  - _Requirements: 2.1, 2.2, 2.4_

- [x] 4. Verify API response includes new fields
  - Ensure AuthController login endpoint returns complete User entity
  - Verify JSON serialization includes uuid and phone fields
  - Test that all user profile fields are included in response
  - _Requirements: 3.1, 3.2, 3.3, 3.4_

- [x] 4.1 Write property test for login response completeness
  - **Property 7: Login response completeness**
  - **Validates: Requirements 3.1, 3.2, 3.3**
  - Test login API returns all expected fields
  - _Requirements: 3.1, 3.2, 3.3_

- [x] 4.2 Write property test for JSON serialization
  - **Property 8: JSON serialization completeness**
  - **Validates: Requirements 3.4**
  - Serialize User entities and verify all fields present
  - _Requirements: 3.4_

- [x] 4.3 Write integration tests for login endpoint
  - Test login endpoint returns uuid
  - Test login endpoint returns phone when available
  - Test response format matches expected structure
  - _Requirements: 3.1, 3.2, 3.3_

- [x] 5. Checkpoint - Ensure all backend tests pass
  - Ensure all tests pass, ask the user if questions arise.

- [x] 6. Update frontend UserStore
  - Add uuid field to UserStore state
  - Update login method to store uuid from API response
  - Update login method to store phone from API response (not from input)
  - Ensure uuid is persisted via Pinia persist plugin
  - Update logout method to clear uuid
  - _Requirements: 3.1, 3.2_

- [x] 6.1 Write unit tests for UserStore
  - Test login stores uuid from response
  - Test login stores phone from response
  - Test uuid persists in localStorage
  - Test logout clears uuid
  - _Requirements: 3.1, 3.2_

- [x] 7. Update frontend Login page if needed
  - Verify login flow works with updated API response
  - Ensure error handling works correctly
  - Test that user data displays properly after login
  - _Requirements: 3.1, 3.2_

- [x] 8. Final checkpoint - Verify end-to-end flow
  - Ensure all tests pass, ask the user if questions arise.
