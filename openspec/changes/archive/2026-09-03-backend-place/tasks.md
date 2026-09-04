## 1. Domain Model And Persistence

- [x] 1.1 Add the `PriceRange` enum and `Place` JPA entity with generated id, required name/address/creator, optional cuisine type and price range, and creation timestamp; verify the entity compiles and the schema starts with the expected `places` table.
- [x] 1.2 Add `PlaceRepository` with the lookup operations needed by the directory service; verify a repository test saves a place and retrieves the same fields and creator relationship.

## 2. API Contract And Validation

- [x] 2.1 Add separate create, update, and response DTOs so clients can submit only mutable place data and responses include the documented place fields; verify DTO mapping does not accept or overwrite id, creator, or createdAt.
- [x] 2.2 Add request validation for non-blank name and address and enum parsing for `LOW`, `MEDIUM`, and `HIGH`; verify invalid create and update payloads return `400 Bad Request` without persistence or mutation.

## 3. Application Service And Ownership

- [x] 3.1 Implement place creation and browsing through the authenticated user context; verify valid list, detail, and create flows return the expected data and assign the JWT user as creator.
- [x] 3.2 Implement update and delete operations with resource lookup and creator ownership checks; verify the creator can mutate a place, another authenticated user receives `403 Forbidden`, and unknown ids receive `404 Not Found`.
- [x] 3.3 Map missing authentication and domain errors to the established API error responses; verify protected operations return `401 Unauthorized` without changing data when no valid JWT is supplied.

## 4. HTTP Endpoints And Security Integration

- [x] 4.1 Add the `/api/places` and `/api/places/{id}` controller endpoints for list, detail, create, update, and delete; verify the endpoint contract with focused Web MVC tests.
- [x] 4.2 Integrate place routes with the existing JWT security configuration and current-user resolver, rejecting request-supplied identity overrides; verify authenticated requests use the token subject and unauthenticated requests are rejected.

## 5. Verification

- [x] 5.1 Run the backend test suite with the Java 21 Maven wrapper and verify repository, validation, authentication, ownership, and endpoint tests pass.
- [x] 5.2 Run strict OpenSpec validation for `backend-place` and verify all required planning artifacts are valid and complete.
