# Tasks — add-backend-user

All backend commands run from the [backend/](backend/) directory using the Maven wrapper (`.\mvnw.cmd` on Windows). See [design.md](openspec/changes/add-backend-user/design.md) for the approach and [specs/user-management/spec.md](openspec/changes/add-backend-user/specs/user-management/spec.md) for the behavior contract.

## 1. Dependencies & configuration

- [ ] 1.1 Add `spring-boot-starter-validation` to [backend/pom.xml](backend/pom.xml); verify `.\mvnw.cmd dependency:resolve` succeeds and the starter is on the classpath.
- [ ] 1.2 Configure the local datasource in [backend/src/main/resources/application.yaml](backend/src/main/resources/application.yaml) (H2 in-memory + JPA `ddl-auto`); verify `.\mvnw.cmd spring-boot:run` starts and JPA initializes with no errors, then stop it.

## 2. Persistence layer

- [ ] 2.1 Create the `User` JPA entity (`id`, `email`, `name`, `avatarUrl`, `department`, `createdAt`) mapped to a `users` table with `email` not-null and a unique constraint, and `createdAt` set on persist; verify `.\mvnw.cmd compile` succeeds.
- [ ] 2.2 Create the `UserRepository` (Spring Data JPA) with `findByEmail` / `existsByEmail`; verify `.\mvnw.cmd compile` succeeds.
- [ ] 2.3 Add a `@DataJpaTest` that saves a user then retrieves it by id (fields match) and asserts a second save with a duplicate email fails; verify the test passes (`.\mvnw.cmd test`). Covers the "repository test" acceptance criterion and the email-uniqueness spec scenario.

## 3. Current-user resolution seam

- [ ] 3.1 Implement a single current-user resolver component that reads the temporary `X-User-Id` header and returns the matching user or "none" (the seam that Auth in issue #2 will replace); verify a unit test covers header-present and header-absent cases.

## 4. DTOs & mapping

- [ ] 4.1 Create a `UserResponse` DTO and a `UserUpdateRequest` DTO exposing only `name`, `avatarUrl`, `department`, with `@NotBlank` on name and `@Email` where an email is accepted; verify `.\mvnw.cmd compile` succeeds and the update DTO has no `email`/`id`/`createdAt` fields.
- [ ] 4.2 Add entity↔DTO mapping (never binding the entity to the HTTP body); verify a unit test that a mapped `UserResponse` carries id, email, name, avatarUrl, department.

## 5. REST endpoints

- [ ] 5.1 Implement `GET /api/users/{id}`; verify a web-layer test returns 200 with the profile for an existing user and 404 for an unknown id.
- [ ] 5.2 Implement `GET /api/users/me` using the resolver; verify a web-layer test returns 200 with the current user's profile when `X-User-Id` resolves and 401 when it does not.
- [ ] 5.3 Implement `PUT /api/users/{id}` updating only `name`/`avatarUrl`/`department` for the owner; verify web-layer tests return 200 for the owner (values updated, email unchanged when supplied), 403 for a non-owner, and 404 for an unknown id.

## 6. Validation & error handling

- [ ] 6.1 Add a `@RestControllerAdvice` mapping validation → 400, not-found → 404, ownership → 403, missing current user → 401, duplicate email → 409; verify tests that an invalid email and a blank name each return 400.

## 7. End-to-end verification

- [ ] 7.1 Run `.\mvnw.cmd test` and confirm the full suite passes.
- [ ] 7.2 Seed one user (H2 console or a dev-only loader), then manually exercise `GET /api/users/{id}`, `GET /api/users/me` (with `X-User-Id`), and `PUT /api/users/{id}` with curl/Postman per the issue's acceptance criteria; record the commands and observed responses.
