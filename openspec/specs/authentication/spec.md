# authentication Specification

## Purpose
Provide a trusted, stateless authentication boundary for the API so callers can prove their identity with signed JWT access tokens instead of spoofable request headers.

## Requirements

### Requirement: User can register

The system SHALL expose `POST /api/auth/register`, accepting a unique email, name, and password, and SHALL create a user profile with a one-way password hash.

#### Scenario: Valid registration
- **WHEN** a client submits a valid unused email, name, and password
- **THEN** the system creates one user profile and returns a success response without the password or password hash

#### Scenario: Duplicate registration email
- **WHEN** a client submits an email already associated with a user profile
- **THEN** the system rejects the registration with a conflict response and does not create another profile

### Requirement: Registered user can authenticate

The system SHALL expose `POST /api/auth/login`, accepting an email and password, and SHALL return a signed JWT access token only when the credentials belong to an existing registered user.

#### Scenario: Valid registered credentials
- **WHEN** a client submits an existing user's email and correct password
- **THEN** the system returns a success response containing a signed JWT access token and its expiration information

#### Scenario: Unknown email or incorrect password
- **WHEN** a client submits credentials that do not match an existing registered user
- **THEN** the system returns `401 Unauthorized` without revealing whether the email or password was incorrect and without creating a user

### Requirement: Protected requests require a valid JWT

The system SHALL require a bearer JWT for protected API endpoints and SHALL validate its signature, issuer, subject, and expiration before treating the request as authenticated.

#### Scenario: Valid bearer token
- **WHEN** a client sends `Authorization: Bearer <valid-token>` to a protected endpoint
- **THEN** the request proceeds with the user identified by the token subject

#### Scenario: Missing bearer token
- **WHEN** a client sends a request to a protected endpoint without bearer credentials
- **THEN** the system returns `401 Unauthorized`

#### Scenario: Invalid or expired bearer token
- **WHEN** a client sends a malformed, incorrectly signed, wrong-issuer, unknown-user, or expired token
- **THEN** the system returns `401 Unauthorized` and does not execute the protected operation

### Requirement: Current-user and ownership checks use authenticated identity

The system SHALL resolve the current user for `/api/users/me` and profile mutation authorization from the validated JWT identity, and SHALL NOT accept `X-User-Id` as an identity source.

#### Scenario: Authenticated user reads own profile
- **WHEN** an authenticated user requests `GET /api/users/me`
- **THEN** the system returns that token's user's profile

#### Scenario: Header attempts to override token identity
- **WHEN** an authenticated request includes an `X-User-Id` value for a different user
- **THEN** the system continues to use the JWT identity for current-user and ownership decisions

#### Scenario: Unauthenticated profile request
- **WHEN** a client requests `GET /api/users/me` without a valid JWT
- **THEN** the system returns `401 Unauthorized`

### Requirement: Authentication secrets and credentials are protected

The system SHALL store passwords only as one-way password hashes, SHALL keep JWT signing configuration outside source-controlled code, and SHALL avoid returning passwords or raw credentials in API responses or logs.

#### Scenario: Password is persisted
- **WHEN** a user credential is created or changed
- **THEN** the persisted credential is a salted one-way hash and is not the submitted password

#### Scenario: Authentication failure response
- **WHEN** authentication fails
- **THEN** the response and application logs contain no password, token secret, or submitted credential
