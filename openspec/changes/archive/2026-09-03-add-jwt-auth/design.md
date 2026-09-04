## Context

The backend is a single Spring Boot 4.1 / Java 21 application. User profile endpoints currently resolve identity through the temporary `X-User-Id` header, which is intentionally isolated for replacement. The authentication boundary must cover HTTP request filtering, credential verification, JWT creation and validation, and user lookup without changing the established profile response contracts.

## Goals / Non-Goals

**Goals:**

- Establish a stateless bearer-token boundary for protected API endpoints.
- Make the JWT subject the single source of truth for current-user and ownership decisions.
- Use a password-hashing component and externalized signing configuration.
- Keep authentication failures uniform and avoid credential disclosure.
- Provide focused tests for token issuance, validation failures, protected endpoint access, and header spoofing.

**Non-Goals:**

- Social login, refresh-token rotation, logout revocation lists, or multi-factor authentication.
- Role and permission administration beyond identifying the authenticated user.
- Reworking unrelated user-profile fields or endpoint response shapes.

## Decisions

- **Use stateless JWT bearer authentication.** Tokens allow the API to authenticate requests without server-side sessions and match the issue's intended JWT contract. Server sessions were rejected because they add shared state and do not fit the backend's API-first boundary.
- **Use a standard `Authorization: Bearer` transport.** This is interoperable with clients, proxies, and security tooling. Continuing with `X-User-Id` was rejected because the caller can freely spoof it.
- **Use an asymmetric signing key configured through deployment secrets.** A private key signs tokens and a public key validates them, limiting verification components' ability to mint credentials. A single shared HMAC secret was rejected because every verifier would also possess the signing capability.
- **Keep the token subject as the stable user identifier and validate issuer and expiration.** This avoids trusting mutable profile data in claims and prevents tokens issued for another environment from being accepted. Unknown subjects fail authentication rather than falling back to a request header.
- **Separate registration from login.** `POST /api/auth/register` creates the profile and hashed credential; `POST /api/auth/login` only verifies an existing credential and issues a token. This avoids incomplete profiles and ambiguous first-login behavior. Automatic profile creation during login was rejected because local credentials do not provide a trustworthy display name.
- **Authenticate credentials against hashed passwords and normalize email lookup.** Passwords will be verified through a dedicated password encoder, while email matching uses the existing repository's uniqueness contract. Plain-text comparison and password persistence are prohibited by the security requirement.
- **Keep the current-user resolver as the application-facing seam.** Its input changes from the servlet header to the authenticated security context, minimizing changes to `/api/users/me` and ownership checks while removing the insecure identity source.
- **Permit only the authentication endpoint and explicitly public infrastructure routes without a token.** User profile operations, including `/api/users/me`, remain protected. CSRF is disabled for the stateless JSON API, while unauthenticated access is handled consistently by the authentication entry point.

## Risks / Trade-offs

- **[Risk]** A stolen access token can be used until it expires. **Mitigation:** use a short configured lifetime, HTTPS in deployment, and keep refresh-token and revocation concerns out of this change for explicit follow-up.
- **[Risk]** Key rotation can invalidate tokens or create a validation gap. **Mitigation:** support external key configuration and document a deployment rotation procedure that keeps the previous public key available during the overlap window.
- **[Risk]** Auto-creating profiles from authentication data may receive incomplete names. **Mitigation:** enforce the existing profile validation rules and define the identity-to-profile mapping at the authentication boundary; reject incomplete data rather than creating an invalid profile.
- **[Risk]** Existing tests and clients may still send `X-User-Id`. **Mitigation:** update the affected tests and client contract to use bearer tokens; explicitly verify that the legacy header cannot override a token identity.

## Migration Plan

1. Add signing-key, issuer, and token-lifetime configuration through environment-backed application properties.
2. Deploy authentication and update API clients/tests to obtain tokens and send bearer authorization.
3. Verify `/api/users/me` and ownership checks with valid, missing, expired, invalid, and conflicting-header requests.
4. Remove all remaining reliance on `X-User-Id`; rollback consists of reverting the authentication deployment and client configuration together, with no database migration required.
