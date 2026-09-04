## Context

Issue 4 introduces the first post resource in a Spring Boot 4.1.1 backend that already uses Spring Data JPA, Jakarta validation, JWT resource-server authentication, and authenticated-user resolution. The existing `User` and `Place` domains are the relationship targets; the post feature must preserve their ownership and authentication contracts. See `proposal.md` for motivation and `specs/post-management/spec.md` for the externally visible behavior.

## Goals / Non-Goals

**Goals:**

- Add a persistence and HTTP boundary for posts with explicit author, optional place, text/photo validation, chronological paging, and author-only mutation.
- Reuse the existing JWT identity and current-user resolution path so author assignment and authorization cannot be selected by request payloads.
- Keep response and error behavior consistent with the existing user and place controllers, including `401`, `403`, `404`, and validation `400` responses.
- Make the feed ordering deterministic when posts share a creation timestamp.

**Non-Goals:**

- Uploading, storing, resizing, or validating image binaries; `photoUrl` is treated as an optional reference supplied by the client.
- Likes, comments, moderation, search, filtering, user-specific feeds, or editing the author and creation timestamp.
- Changing authentication, user-management, or place-directory requirements.

## Decisions

- **Use a dedicated post domain package with entity, repository, service, DTO/mapper, controller, and exception handling.** This matches the existing place and user module organization and keeps persistence, mapping, authorization, and HTTP concerns separated. A controller-only implementation was rejected because it would duplicate ownership and validation logic and make service-level tests weaker.

- **Model `author` as a required relationship to `User` and `place` as an optional relationship to `Place`.** The author is always resolved from the authenticated request and is never accepted as a writable client field. The place is looked up when supplied so an unknown reference fails before the post is stored. Embedding duplicate author/place fields was rejected because it would weaken referential integrity and diverge from existing domain relationships.

- **Enforce the text-or-photo invariant at the service boundary, with request validation for field-level constraints.** Bean validation can reject malformed individual inputs, while a service-level invariant handles the allowed combination of two optional values for both create and update. Relying only on database nullability was rejected because it cannot express the cross-field rule reliably or produce the intended `400` response.

- **Use Spring Data pagination with an explicit descending creation-time order and identifier tie-breaker.** The repository query should return a `Page<Post>` for `page` and `size`, ordered by `createdAt DESC, id DESC`. Loading all posts and slicing in memory was rejected for unbounded feed growth and inconsistent pagination.

- **Perform ownership checks in the service using the authenticated current user before mutation.** Missing posts resolve to `404`; an existing post owned by another authenticated user resolves to `403`. Comparing the JWT-derived user identifier rather than request headers preserves the authentication specification's identity rule.

- **Keep mapping explicit and avoid exposing mutable persistence relationships as request fields.** Create/update DTOs contain only editable post data and an optional place identifier; response DTOs contain the post identity, author representation, text, photo URL, place representation, and creation timestamp. This prevents clients from changing the author or timestamp and aligns with existing mapper-based modules.

## Risks / Trade-offs

- **[Risk]** `photoUrl` may point to an unavailable or unsafe external resource. **Mitigation:** this change stores only the URL and leaves binary hosting and URL policy to a later capability; do not fetch the resource during post creation.
- **[Risk]** Offset pagination can show duplicates or skips when new posts arrive between requests. **Mitigation:** provide stable descending ordering with the identifier tie-breaker; cursor pagination remains a future option if feed usage requires it.
- **[Risk]** Lazy relationship access during response mapping can cause incomplete author/place data outside a transaction. **Mitigation:** map within the service transaction or use a repository fetch strategy that loads the response relationships deliberately, then cover the behavior with controller/integration tests.
- **[Risk]** Existing temporary current-user behavior may differ across test and runtime profiles. **Mitigation:** route all post operations through the established resolver and add JWT-backed integration coverage for unauthenticated and cross-user mutation cases.

## Migration Plan

Add the post entity and repository so normal JPA schema generation creates the table in the current development database. Deploy the backend with the new endpoints; existing users and places remain unchanged. Roll back by deploying the prior backend version and removing the post table through the environment's normal database migration process if post data must also be removed.

## Open Questions

None. The issue specifies the required fields, endpoints, validation rule, pagination parameters, and ownership behavior sufficiently for implementation.
