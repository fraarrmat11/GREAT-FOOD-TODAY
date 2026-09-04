## Why

The application needs a shared directory of places where users can eat near the office. Without a persisted, authenticated place resource, posts and reviews cannot reliably reference a place or enforce who may maintain its information.

## What Changes

- Add a persisted `Place` resource with `id`, `name`, `address`, optional `cuisineType` and `priceRange`, the authenticated creator, and `createdAt`.
- Add repository support and REST endpoints to list places, retrieve a place, create a place, and update or delete a place.
- Require authentication for all place endpoints that create, update, or delete data, using the trusted JWT identity.
- Allow any authenticated user to create a place, while restricting updates and deletes to the place creator.
- Validate that `name` and `address` are present on create and update requests.
- Add a repository test covering basic place persistence and retrieval.

## Capabilities

### New Capabilities

- `place-directory`: Authenticated CRUD and persistence for places, including creator ownership and required-field validation.

### Modified Capabilities

<!-- None. This is a new capability. -->

## Impact

- **Code:** New backend entity, repository, DTOs, service, controller, and focused tests under `gft.goodfoodtoday.backend`.
- **API:** Adds `/api/places` and `/api/places/{id}` endpoints; write operations depend on JWT authentication and creator ownership.
- **Data:** Adds a `places` table with a required relationship to `User`; `priceRange` is represented by the supported `LOW`, `MEDIUM`, and `HIGH` values.
- **Dependencies:** Uses the existing JPA, Web MVC, validation, JWT authentication, H2, PostgreSQL, and Lombok setup; no new dependency is expected.
- **Consumers:** Posts and reviews can later reference the stable place identifier and directory endpoints.
