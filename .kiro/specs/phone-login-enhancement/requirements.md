# Requirements Document

## Introduction

This document specifies the requirements for enhancing the phone login system in the Zibian application. Currently, the users table lacks essential fields such as UUID and phone number, which are needed for proper user identification and management. This enhancement will add these missing fields to both the database schema and the application models, ensuring complete user profile information is available throughout the system.

## Glossary

- **System**: The Zibian backend application and database
- **User**: A person who uses the Zibian application
- **UUID**: Universally Unique Identifier, a 36-character string used for unique user identification
- **Phone Number**: A mobile phone number used for authentication
- **User Entity**: The Java model class representing a user in the application
- **Users Table**: The database table storing user profile information
- **Frontend**: The Vue.js web application that users interact with
- **UserStore**: The Pinia store managing user state in the frontend

## Requirements

### Requirement 1

**User Story:** As a system administrator, I want each user to have a unique UUID, so that users can be reliably identified across different systems and integrations.

#### Acceptance Criteria

1. WHEN a new user is created THEN the System SHALL generate a unique UUID for that user
2. WHEN storing a user record THEN the System SHALL persist the UUID to the users table
3. WHEN retrieving a user THEN the System SHALL include the UUID in the User Entity
4. THE System SHALL ensure the UUID field is non-null and unique in the users table

### Requirement 2

**User Story:** As a developer, I want the phone number stored in the users table, so that I can easily access user contact information without joining multiple tables.

#### Acceptance Criteria

1. WHEN a user registers via phone THEN the System SHALL store the phone number in the users table
2. WHEN a user logs in via phone THEN the System SHALL update the phone number in the users table if it differs
3. WHEN retrieving a user THEN the System SHALL include the phone number in the User Entity
4. THE System SHALL ensure the phone number field allows null values for users registered via third-party authentication

### Requirement 3

**User Story:** As a frontend developer, I want the login API to return complete user information including phone number and UUID, so that the frontend can display and manage user data properly.

#### Acceptance Criteria

1. WHEN a user successfully logs in THEN the System SHALL return a response containing the user's UUID
2. WHEN a user successfully logs in THEN the System SHALL return a response containing the user's phone number if available
3. WHEN the frontend receives login response THEN the System SHALL include all user profile fields in the response
4. THE System SHALL serialize the User Entity to JSON format including all fields

### Requirement 4

**User Story:** As a database administrator, I want the database schema to be updated with migration scripts, so that existing data is preserved and new fields are added correctly.

#### Acceptance Criteria

1. WHEN applying database migrations THEN the System SHALL add the UUID column to the users table
2. WHEN applying database migrations THEN the System SHALL add the phone column to the users table
3. WHEN applying database migrations THEN the System SHALL generate UUIDs for existing users
4. WHEN applying database migrations THEN the System SHALL populate phone numbers for existing users from user_auths table where identity_type is 'PHONE'

### Requirement 5

**User Story:** As a backend developer, I want the User model to automatically generate UUIDs, so that I don't need to manually create them for each new user.

#### Acceptance Criteria

1. WHEN a new User Entity is instantiated THEN the System SHALL automatically generate a UUID
2. WHEN saving a User Entity without a UUID THEN the System SHALL generate one before persistence
3. THE System SHALL use the standard UUID generation algorithm
4. THE System SHALL ensure UUID generation happens before database insertion
