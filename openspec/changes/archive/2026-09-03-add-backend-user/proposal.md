## Why

Almost every domain in Good Food Today (Auth, Post, Place, Review, Comment, Like) references a user. Issue #1 establishes that foundation: a persisted `User` entity with basic profile CRUD, so the dependent features can be built on a stable base.

## What Changes

- Add a `User` JPA entity: `id`, `email` (unique, not null, immutable), `name` (not null), `avatarUrl` (nullable), `department` (nullable), `createdAt`.
- Add a Spring Data JPA `UserRepository`.
- Expose REST endpoints:
  - `POST /api/users` — create a user profile (internal use only; issue #1 defines no client-facing signup flow, but a real HTTP path is needed to enforce and test the email/name validation rules until Auth (issue #2) creates users on first login).
  - `GET /api/users/{id}` — get a user profile.
  - `GET /api/users/me` — profile of the authenticated user. Placeholder until Auth (issue #2) exists; the "current user" is resolved from a temporary stand-in until the security context is available.
  - `PUT /api/users/{id}` — edit an own profile (`name`, `avatarUrl`, `department` only).
- Add request/response DTOs so `email`, `id`, and `createdAt` cannot be mutated through the update endpoint, and so creation only accepts `email`, `name`, `avatarUrl`, `department`.
- Add bean-validation rules: valid email format, non-empty `name` on creation; non-empty `name` on update; `email` is unique on creation and rejected on change.
- Add the `spring-boot-starter-validation` dependency (not yet on the classpath).
- Add a basic repository test (save then retrieve a user).

## Capabilities

### New Capabilities
- `user-management`: Persistence, retrieval, and profile update of application users, including the identity invariants (unique, immutable email; non-empty name) that dependent capabilities rely on.

### Modified Capabilities
<!-- None. This is the first capability in the project. -->

## Impact

- **Code**: New classes under `gft.goodfoodtoday.backend` (entity, repository, controller, DTOs). Follows the existing single-module Spring Boot layout.
- **Dependencies**: Adds `spring-boot-starter-validation`. JPA, Web MVC, H2, PostgreSQL, and Lombok are already present.
- **Data**: New `users` table (H2 for local dev, PostgreSQL for deployment).
- **Assumptions**: Auth (issue #2) does not exist yet, so `GET /api/users/me` and the "edit only your own profile" rule are implemented as placeholders (temporary current-user stand-in) and will be wired to the real security context when Auth lands. No breaking changes — this is net-new functionality.
