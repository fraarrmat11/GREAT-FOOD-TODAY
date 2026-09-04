# Tasks — add-backend-user

All backend commands run from the [backend/](backend/) directory using the Maven wrapper (`.\mvnw.cmd` on Windows). See [design.md](openspec/changes/add-backend-user/design.md) for the approach and [specs/user-management/spec.md](openspec/changes/add-backend-user/specs/user-management/spec.md) for the behavior contract.

## 1. Dependencies & configuration

- [x] 1.1 Add `spring-boot-starter-validation` to [backend/pom.xml](backend/pom.xml); verify `.\mvnw.cmd dependency:resolve` succeeds and the starter is on the classpath.
- [x] 1.2 Configure the local datasource in [backend/src/main/resources/application.yaml](backend/src/main/resources/application.yaml) (H2 in-memory + JPA `ddl-auto`); verify `.\mvnw.cmd spring-boot:run` starts and JPA initializes with no errors, then stop it.

## 2. Persistence layer

- [x] 2.1 Create the `User` JPA entity (`id`, `email`, `name`, `avatarUrl`, `department`, `createdAt`) mapped to a `users` table with `email` not-null and a unique constraint, and `createdAt` set on persist; verify `.\mvnw.cmd compile` succeeds.
- [x] 2.2 Create the `UserRepository` (Spring Data JPA) with `findByEmail` / `existsByEmail`; verify `.\mvnw.cmd compile` succeeds.
- [x] 2.3 Add a `@DataJpaTest` that saves a user then retrieves it by id (fields match) and asserts a second save with a duplicate email fails; verify the test passes (`.\mvnw.cmd test`). Covers the "repository test" acceptance criterion and the email-uniqueness spec scenario.

## 3. Current-user resolution seam

- [x] 3.1 Implement a single current-user resolver component that reads the temporary `X-User-Id` header and returns the matching user or "none" (the seam that Auth in issue #2 will replace); verify a unit test covers header-present and header-absent cases.

## 4. DTOs & mapping

- [x] 4.1 Create a `UserCreateRequest` DTO (`email`, `name`, `avatarUrl`, `department`) with `@Email` and `@NotBlank` on email/name, a `UserUpdateRequest` DTO exposing only `name`, `avatarUrl`, `department` with `@NotBlank` on name, and a `UserResponse` DTO; verify `.\mvnw.cmd compile` succeeds and the update DTO has no `email`/`id`/`createdAt` fields.
- [x] 4.2 Add entity↔DTO mapping (never binding the entity to the HTTP body); verify a unit test that a mapped `UserResponse` carries id, email, name, avatarUrl, department.

## 5. REST endpoints

- [x] 5.1 Implement `POST /api/users` creating a profile from `UserCreateRequest`; verify web-layer tests return 201 for a valid request, 400 for an invalid email or blank name, and 409 for a duplicate email.
- [x] 5.2 Implement `GET /api/users/{id}`; verify a web-layer test returns 200 with the profile for an existing user and 404 for an unknown id.
- [x] 5.3 Implement `GET /api/users/me` using the resolver; verify a web-layer test returns 200 with the current user's profile when `X-User-Id` resolves and 401 when it does not.
- [x] 5.4 Implement `PUT /api/users/{id}` updating only `name`/`avatarUrl`/`department` for the owner; verify web-layer tests return 200 for the owner (values updated, email unchanged when supplied), 400 for a blank name, 403 for a non-owner, and 404 for an unknown id.

## 6. Validation & error handling

- [x] 6.1 Add a `@RestControllerAdvice` mapping validation → 400, not-found → 404, ownership → 403, missing current user → 401, duplicate email → 409; verify tests that an invalid email and a blank name each return 400.

## 7. End-to-end verification

- [x] 7.1 Run `.\mvnw.cmd test` and confirm the full suite passes.
- [x] 7.2 Seed one user via `POST /api/users` (or the H2 console), then manually exercise `GET /api/users/{id}`, `GET /api/users/me` (with `X-User-Id`), and `PUT /api/users/{id}` with curl/Postman per the issue's acceptance criteria; record the commands and observed responses.

  Manual verification (2026-09-02, app run locally with H2, `X-User-Id` header for the current-user seam):
  - `POST /api/users` `{"email":"ana@example.com","name":"Ana","department":"Engineering"}` -> 201, body `{"id":1,"email":"ana@example.com","name":"Ana","avatarUrl":null,"department":"Engineering"}`
  - `GET /api/users/1` -> 200, same profile
  - `GET /api/users/999` -> 404
  - `GET /api/users/me` (no header) -> 401
  - `GET /api/users/me` (`X-User-Id: 1`) -> 200, Ana's profile
  - `PUT /api/users/1` (`X-User-Id: 1`, `{"name":"Ana Updated","department":"Sales"}`) -> 200, `{"id":1,"email":"ana@example.com","name":"Ana Updated","avatarUrl":null,"department":"Sales"}` (email unchanged)
  - `POST /api/users` `{"email":"bob@example.com","name":"Bob"}` -> 201 (second user, id 2)
  - `PUT /api/users/1` (`X-User-Id: 2`, non-owner) -> 403
  - `PUT /api/users/1` (`X-User-Id: 1`, `{"name":" "}`) -> 400 (blank name)
  - `POST /api/users` `{"email":"ana@example.com", ...}` (duplicate) -> 409
  - `POST /api/users` `{"email":"not-an-email", ...}` -> 400 (invalid email)
