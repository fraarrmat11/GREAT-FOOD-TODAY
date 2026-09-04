## 1. Authentication Foundation

- [x] 1.1 Add the backend security and JWT dependencies, configure an environment-backed issuer, asymmetric signing keys, and short access-token lifetime, and verify the application starts with valid configuration
- [x] 1.2 Extend the user credential model and repository contract for normalized email lookup and a one-way password hash, and verify persistence tests never store or expose the submitted password
- [x] 1.3 Define authentication request and token response DTOs with validation and verify serialization contains only the accepted credential inputs and token metadata

## 2. Registration And Credential Authentication

- [x] 2.1 Implement the registration service and `POST /api/auth/register`, including duplicate-email and validation handling, and verify valid registration creates one profile with a hashed password
- [x] 2.2 Implement credential verification with uniform failure behavior for unknown emails and incorrect passwords, and verify both cases return `401 Unauthorized` without credential disclosure or user creation
- [x] 2.3 Implement `POST /api/auth/login` and signed JWT issuance with subject, issuer, and expiration claims, and verify a valid login response contains a token accepted by the validator

## 3. Request Security

- [x] 3.1 Configure stateless bearer-token request authentication, public-route exceptions, and a uniform unauthorized response, and verify missing, malformed, invalid-signature, wrong-issuer, expired, and unknown-subject tokens are rejected
- [x] 3.2 Replace `X-User-Id` resolution with the validated security identity and verify `/api/users/me` returns the token subject's profile
- [x] 3.3 Verify ownership checks use the authenticated identity even when a conflicting `X-User-Id` header is present, and verify the legacy header cannot authenticate an otherwise unauthenticated request

## 4. Integration And Hardening

- [x] 4.1 Update user-controller and authentication tests for bearer tokens and verify protected profile operations preserve their existing response contracts
- [x] 4.2 Add security regression tests proving passwords, signing secrets, and submitted credentials are absent from responses and application logs
- [x] 4.3 Run the complete backend test suite and verify the configured OpenSpec scenarios are covered by passing tests