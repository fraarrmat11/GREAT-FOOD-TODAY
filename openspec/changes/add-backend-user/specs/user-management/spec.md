## Purpose

The user-management capability owns the application's user profiles: how a user is stored, retrieved, and updated, and the identity invariants (unique, immutable email; non-empty name) that every other capability in Good Food Today depends on.

## ADDED Requirements

### Requirement: User profile persistence

The system SHALL persist a user profile consisting of a system-assigned identifier, a unique email address, a display name, an optional avatar URL, an optional department, and a creation timestamp assigned when the profile is first stored.

#### Scenario: Store and retrieve a user
- **WHEN** a user profile with a valid email and name is saved
- **THEN** it is assigned an identifier and a creation timestamp
- **AND** retrieving it by that identifier returns the same email, name, avatar URL, and department

#### Scenario: Optional fields absent
- **WHEN** a user profile is saved without an avatar URL or department
- **THEN** the profile is stored successfully with those fields empty

### Requirement: Retrieve a user profile by identifier

The system SHALL expose `GET /api/users/{id}` that returns the profile for the given identifier.

#### Scenario: Existing user
- **WHEN** a client requests `GET /api/users/{id}` for an existing user
- **THEN** the response status is 200 and the body contains the user's identifier, email, name, avatar URL, and department

#### Scenario: Unknown user
- **WHEN** a client requests `GET /api/users/{id}` for an identifier that does not exist
- **THEN** the response status is 404

### Requirement: Retrieve the current user's profile

The system SHALL expose `GET /api/users/me` that returns the profile of the currently identified user. Until the authentication capability exists, the current user is resolved through a temporary stand-in; the externally visible contract of returning the current user's profile does not change when authentication replaces the stand-in.

#### Scenario: Current user resolved
- **WHEN** a client requests `GET /api/users/me` and a current user can be determined
- **THEN** the response status is 200 and the body contains that user's profile

#### Scenario: No current user
- **WHEN** a client requests `GET /api/users/me` and no current user can be determined
- **THEN** the response status is 401

### Requirement: Update an own profile

The system SHALL expose `PUT /api/users/{id}` that updates only the name, avatar URL, and department of the target profile. A user SHALL be able to update only their own profile.

#### Scenario: Owner updates mutable fields
- **WHEN** the current user submits `PUT /api/users/{id}` for their own profile with a new name, avatar URL, or department
- **THEN** the response status is 200 and the returned profile reflects the updated values

#### Scenario: Updating another user's profile is forbidden
- **WHEN** the current user submits `PUT /api/users/{id}` for a profile that is not their own
- **THEN** the response status is 403 and the target profile is unchanged

#### Scenario: Updating an unknown profile
- **WHEN** a client submits `PUT /api/users/{id}` for an identifier that does not exist
- **THEN** the response status is 404

### Requirement: Email uniqueness and immutability

The system SHALL enforce that each user's email is unique across all users and cannot be changed after the profile is created.

#### Scenario: Duplicate email rejected
- **WHEN** a user profile is created with an email that already belongs to another user
- **THEN** the profile is not stored and the operation is rejected

#### Scenario: Email cannot be changed on update
- **WHEN** the current user submits `PUT /api/users/{id}` including a different email value
- **THEN** the stored email remains the original value

### Requirement: Profile input validation

The system SHALL reject profile creation or update requests whose email is not a syntactically valid address or whose name is empty or blank.

#### Scenario: Invalid email format
- **WHEN** a profile is submitted with an email that is not a valid address
- **THEN** the response status is 400 and the profile is not stored

#### Scenario: Blank name
- **WHEN** a profile is submitted with an empty or whitespace-only name
- **THEN** the response status is 400 and the profile is not stored
