## Why

Issue #1 introduced a temporary `X-User-Id` header so user-profile behavior could be developed before authentication existed. That header is spoofable and cannot establish a trusted caller identity, so the application now needs a real JWT-based authentication boundary before dependent features are exposed.

## What Changes

- Add a local registration flow that accepts an email, name, and password.
- Add a login flow that accepts an email and password and returns a signed JWT access token.
- Add JWT validation for protected API requests, including expiration and signature checks.
- Replace the temporary `X-User-Id` identity source with the authenticated user identity from the security context.
- Return `401 Unauthorized` for missing, malformed, expired, or invalid credentials on protected endpoints.
- Keep user profiles and credentials synchronized by creating them during registration and resolving them during login.
- Preserve the existing `/api/users/me` and profile-ownership behavior while making its caller identity trusted.

## Capabilities

### New Capabilities

- `authentication`: Credential-based login, signed JWT issuance, request authentication, and trusted current-user resolution.

### Modified Capabilities

- None.

## Impact

- Backend Spring Security configuration, authentication controllers/services, JWT token handling, and user identity resolution.
- User endpoints that currently depend on the temporary `X-User-Id` header.
- Backend dependencies and application configuration for password hashing, token signing, and authentication settings.
- API clients will register through `POST /api/auth/register`, log in through `POST /api/auth/login`, and send `Authorization: Bearer <token>` for protected requests; direct use of `X-User-Id` will no longer establish identity.