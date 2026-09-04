## Why

The application needs a shared, authenticated feed where users can record what they have eaten today and associate it with an optional place. Issue 4 adds the first post workflow, including ownership controls and basic pagination, so the frontend and later capabilities can consume a stable post API.

## What Changes

- Add a persisted `Post` resource with an identifier, authenticated author, optional text, optional photo URL, optional place, and creation timestamp.
- Expose a chronologically ordered, paginated `GET /api/posts` feed with `page` and `size` parameters.
- Expose authenticated endpoints to create, edit, and delete posts at `POST /api/posts`, `PUT /api/posts/{id}`, and `DELETE /api/posts/{id}`.
- Require a valid JWT for every post endpoint and derive the author from the authenticated identity.
- Enforce that a post contains text or a photo URL and that only its author can edit or delete it.
- Allow a post to reference an existing place without making the place mandatory.

## Capabilities

### New Capabilities

- `post-management`: Persist, validate, list, create, update, and delete user-authored posts in a protected chronological feed.

### Modified Capabilities

- None.

## Impact

- Backend post domain entity, repository, service, DTO/mapping, controller, validation, and authorization tests.
- API surface under `/api/posts`, including paginated response behavior and HTTP error responses for invalid or unauthorized operations.
- Integration with existing JWT current-user resolution, `User` author relationships, and optional `Place` references.
- No changes to existing authentication, user-management, or place-directory requirements are expected.
