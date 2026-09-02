## Context

See proposal.md — Why. The backend is a single-module Spring Boot 4.1 / Java 21 app with JPA, Web MVC, H2 and PostgreSQL drivers, and Lombok already on the classpath (see [backend/pom.xml](backend/pom.xml)). There is no authentication yet — Auth is issue #2 — and there is no validation starter on the classpath. Requirements for this change are in [specs/user-management/spec.md](openspec/changes/add-backend-user/specs/user-management/spec.md).

## Goals / Non-Goals

**Goals:**
- Persist and expose user profiles with the identity invariants other capabilities depend on.
- Implement `/api/users/me` and the "edit only your own profile" rule in a way that is testable now and cleanly replaced by Auth later.
- Structurally prevent mutation of `email`, `id`, and `createdAt` through the update endpoint.

**Non-Goals:**
- Real authentication / JWT (issue #2).
- Roles, admin overrides, account deletion, avatar file upload.
- Database migration tooling.

## Decisions

### Current-user resolution behind a single seam
Introduce one small component (a "current user" resolver) that all endpoints use to determine the caller. Until Auth exists it reads a temporary `X-User-Id` request header; when Auth lands (issue #2) only this component changes to read the security context. It returns "no current user" when the header is absent, which drives the 401 on `/api/users/me`.
- **Why:** Keeps `/api/users/me` and the ownership rule implemented and testable in this change, with a single isolated point to swap for Auth.
- **Alternatives:** Return 501 for `/me` and skip ownership enforcement — rejected because the ownership acceptance criterion becomes untestable and the seam still has to be built later. Hardcode a user — rejected as not meaningful or testable.

### Dedicated request/response DTOs (no entity binding)
The update endpoint binds to a request object exposing only `name`, `avatarUrl`, and `department`. Responses use a separate response object. The JPA entity is never bound directly to the HTTP body.
- **Why:** Makes `email`/`id`/`createdAt` immutability structural (the fields cannot be set from the body) and avoids a mass-assignment vulnerability (OWASP API6/A08). It also decouples the wire contract from the schema.
- **Alternative:** Bind the entity and ignore disallowed fields — rejected as error-prone and insecure.

### Email uniqueness enforced at the database and reported by the service
The `email` column carries a unique constraint; the service checks for an existing email before insert and returns a 409 Conflict with a clear message. Format validity is enforced by validation (below).
- **Why:** The database constraint is the source of truth against races; the service check yields a friendly error instead of a raw constraint violation.

### Validation via the Bean Validation starter
Add `spring-boot-starter-validation` and annotate the request DTOs (`@Email`, `@NotBlank`); controllers validate with `@Valid`. Constraint violations map to HTTP 400 through a controller-advice handler that also maps not-found → 404, ownership → 403, missing current user → 401, and duplicate email → 409.
- **Why:** Standard, declarative, and gives consistent error responses aligned with the spec's status codes.

### Persistence and schema
Map the entity to a `users` table via JPA. Use H2 (in-memory) for local dev and tests, PostgreSQL for deployment — both drivers are already present. Schema is managed by JPA `ddl-auto` for this MVP; no migration tool yet.
- **Why:** Matches the existing dependency set and keeps the change additive and simple.

## Risks / Trade-offs

- **`X-User-Id` header is spoofable** → Acceptable for a pre-Auth MVP; it is not presented as a security boundary and is isolated behind the resolver, replaced by the JWT security context in issue #2.
- **`ddl-auto` schema management** → Fine for early development; introduce Flyway/Liquibase before real data exists.
- **Email immutability enforced at the API boundary** → The repository layer could still change email in code; mitigated by the DTO seam plus the unique constraint, with the invariant documented in the spec.

## Migration Plan

Additive only: add the `spring-boot-starter-validation` dependency and a new `users` table created on startup. No data migration or rollback concerns; reverting means removing the new classes, endpoints, and dependency.
