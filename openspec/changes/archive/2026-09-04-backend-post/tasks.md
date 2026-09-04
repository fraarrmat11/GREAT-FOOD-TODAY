## 1. Domain And Persistence

- [x] 1.1 Add the `Post` entity with generated `Long` id, required `User` author, optional text/photo URL/place relationships, and creation timestamp; verify a JPA persistence round trip preserves all fields and relationships
- [x] 1.2 Add the post repository with chronological pagination ordered by `createdAt DESC, id DESC`; verify repository tests return the requested page size and newest posts first, including equal-timestamp tie-breaking
- [x] 1.3 Add post request/response DTOs and explicit mapping for editable fields and response relationships; verify mapping tests never accept or overwrite author and creation timestamp from client input

## 2. Service And Validation

- [x] 2.1 Implement post creation using the authenticated current user and optional place lookup; verify service tests persist valid text-only, photo-only, and place-referencing posts
- [x] 2.2 Enforce the cross-field rule that text or photo URL must be present on create and update; verify empty and blank-only payloads return validation failures without persistence or mutation
- [x] 2.3 Implement paginated feed retrieval with stable newest-first ordering and documented default page/size values; verify service tests return pagination metadata and the expected page contents
- [x] 2.4 Implement author-only update and delete operations with `404` for unknown posts and `403` for non-authors; verify service tests preserve author/id/creation timestamp on update and leave data unchanged after denied operations

## 3. HTTP API And Security

- [x] 3.1 Add protected `POST /api/posts` and `GET /api/posts` endpoints with request validation and page/size binding; verify controller tests cover successful creation, default pagination, explicit pagination, empty-post `400`, and unknown-place `404`
- [x] 3.2 Add protected `PUT /api/posts/{id}` and `DELETE /api/posts/{id}` endpoints with consistent success and exception responses; verify controller tests cover owner success, non-owner `403`, unknown post `404`, and invalid update `400`
- [x] 3.3 Integrate every post endpoint with JWT-derived current-user resolution; verify security/integration tests return `401` for missing, invalid, or expired bearer tokens and never use an identity supplied in request data or headers

## 4. Integration Verification

- [x] 4.1 Add end-to-end backend coverage for create, feed pagination, update, and delete across two authenticated users; verify author ownership, optional place serialization, chronological ordering, and persistence behavior together
- [x] 4.2 Run the backend test suite with Java 21 using `backend\\mvnw.cmd test`; verify all existing tests and new post tests pass without modifying authentication, user-management, or place-directory behavior
