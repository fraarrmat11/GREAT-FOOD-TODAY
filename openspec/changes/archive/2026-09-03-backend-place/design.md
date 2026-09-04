## Context

See proposal.md - Why. The backend is a single-module Spring Boot 4.1 / Java 21 application. User persistence and JWT authentication are established by the preceding capabilities, and place operations must consume the authenticated user rather than request-supplied identity.

## Goals / Non-Goals

**Goals:**

- Add a place aggregate that can be referenced by later posts and reviews.
- Keep the HTTP contract separate from persistence so ownership and immutable fields cannot be mass-assigned.
- Centralize creator authorization for update and delete operations.
- Provide focused repository and web-layer coverage for persistence, validation, authentication, and ownership.

**Non-Goals:**

- Search, geographic distance/radius filtering, pagination, ratings, reviews, comments, likes, or photo uploads.
- Admin roles or moderation overrides.
- Database migration tooling or historical data migration.

## Decisions

### Use a `Place` entity with an explicit creator relationship

Map the required fields to a `places` table and associate each row with the existing `User` entity through a non-null many-to-one relationship. Represent `priceRange` as an enum with `LOW`, `MEDIUM`, and `HIGH`, and generate `createdAt` when the entity is created.

A separate creator relationship is preferred over storing only a user identifier because it preserves the domain association for later posts and reviews and lets repository queries and response mapping use the existing user model.

### Use separate create, update, and response DTOs

The create DTO exposes `name`, `address`, `cuisineType`, and `priceRange`; the update DTO exposes the mutable place fields; the response DTO exposes the public place representation, including identifier, creator, and creation timestamp. Controllers and services will never bind request JSON directly to the JPA entity.

This prevents callers from changing `id`, `createdAt`, or `createdBy`, and keeps ownership determined by the authenticated context. Binding entities directly was rejected because it makes accidental mass assignment and invariant violations easier.

### Put ownership checks in the application service

The service will resolve the current authenticated user, load the target place, compare its creator with that user, and only then update or delete it. Missing resources map to `404`, missing authentication to `401`, and an authenticated non-creator to `403` through the established API error handling pattern.

A controller-only check was rejected because it can be bypassed by another caller of the service and duplicates authorization logic across mutation paths.

### Require JWT for every place endpoint

Security configuration will protect the place collection and item routes. The service and controller assume that the current-user resolver is backed by the validated JWT subject from the authentication capability; no `X-User-Id` or body field is accepted as a substitute.

Allowing read access anonymously was rejected because the issue defines the directory as part of the authenticated application and consistent protection avoids exposing creator information before the client has established identity.

### Validate at the request boundary and preserve database constraints

Apply non-blank validation to `name` and `address` on both create and update requests. Reject invalid enum values as a client error before persistence. Keep the database relationship and not-null constraints as a second line of defense.

The design does not add a new dependency: it uses the validation, JPA, Web MVC, H2, PostgreSQL, and Lombok setup already selected by the backend.

## Risks / Trade-offs

- **[Risk]** Deleting a place later referenced by posts or reviews can create referential-integrity problems. **Mitigation:** this change defines place CRUD only; dependent content should establish the deletion policy before those capabilities are implemented.
- **[Risk]** JPA fetch behavior could expose too much user data or cause extra queries when mapping responses. **Mitigation:** map only the documented creator representation and keep response DTOs explicit; verify the web-layer contract with focused tests.
- **[Risk]** The current schema is managed by JPA during the early MVP. **Mitigation:** use the existing H2 setup for tests and document migration tooling as a later deployment concern.
- **[Risk]** Ownership tests can accidentally pass by relying on a mocked identity instead of the JWT boundary. **Mitigation:** cover both service/controller ownership decisions and at least one integration path using authenticated requests.

## Migration Plan

Add the entity and endpoints as an additive deployment. JPA creates the new `places` table with its required user relationship in the existing development/test schema. Deploy clients that obtain JWTs first, then exercise list, detail, create, update, and delete flows. Rollback consists of reverting the backend change; no existing records or endpoints need to be transformed.
