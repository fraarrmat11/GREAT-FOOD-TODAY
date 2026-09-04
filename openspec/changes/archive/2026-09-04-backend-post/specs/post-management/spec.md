## Purpose

Provide an authenticated social food log where users can publish what they ate, optionally attach a photo or place, and manage only their own posts in a chronological feed.

## ADDED Requirements

### Requirement: Post persistence

The system SHALL persist each post with a system-assigned `Long` identifier, a required author user, optional text, optional photo URL, an optional place reference, and a creation timestamp assigned when the post is first stored. A post SHALL contain at least text or a photo URL.

#### Scenario: Store a post with text
- **WHEN** an authenticated user creates a post with text and no photo URL or place
- **THEN** the system persists the post with that user as author, assigns an identifier and creation timestamp, and returns the stored text

#### Scenario: Store a post with a photo
- **WHEN** an authenticated user creates a post with a photo URL and no text
- **THEN** the system persists the post successfully with the photo URL and the authenticated user as author

#### Scenario: Store a post with an optional place
- **WHEN** an authenticated user creates a valid post referencing an existing place
- **THEN** the system persists the place relationship and returns the referenced place in the post representation

### Requirement: Authenticated users can create posts

The system SHALL expose `POST /api/posts`. The endpoint SHALL require a valid JWT, derive the author from the authenticated identity, accept optional text, photo URL, and place identifier, and return the created post without exposing credential data.

#### Scenario: Create a valid post
- **WHEN** an authenticated user submits a post containing non-blank text or a photo URL
- **THEN** the system returns a success response containing the new post and its authenticated author

#### Scenario: Create an empty post
- **WHEN** an authenticated user submits a post with neither text nor photo URL, or with both values blank
- **THEN** the system returns `400 Bad Request` and does not persist a post

#### Scenario: Create without authentication
- **WHEN** a client submits `POST /api/posts` without a valid JWT
- **THEN** the system returns `401 Unauthorized` and does not persist a post

#### Scenario: Reference an unknown place
- **WHEN** an authenticated user submits a valid post with a place identifier that does not exist
- **THEN** the system returns `404 Not Found` and does not persist the post

### Requirement: Users can browse a chronological post feed

The system SHALL expose `GET /api/posts` with `page` and `size` query parameters. The endpoint SHALL require a valid JWT and return posts ordered from newest to oldest in a paginated response.

#### Scenario: First page of posts
- **WHEN** an authenticated user requests `GET /api/posts?page=0&size=10`
- **THEN** the system returns at most 10 posts ordered by descending creation timestamp and includes pagination metadata sufficient to request subsequent pages

#### Scenario: Default pagination
- **WHEN** an authenticated user requests `GET /api/posts` without pagination parameters
- **THEN** the system returns the first page using the endpoint's documented default page and size values

#### Scenario: Browse without authentication
- **WHEN** a client requests `GET /api/posts` without a valid JWT
- **THEN** the system returns `401 Unauthorized`

### Requirement: Authors can update their own posts

The system SHALL expose `PUT /api/posts/{id}`. The endpoint SHALL require a valid JWT and SHALL allow only the post author to replace the editable text, photo URL, and place values. The updated post SHALL still contain text or a photo URL.

#### Scenario: Author updates a post
- **WHEN** the authenticated author submits valid replacement values for an existing post
- **THEN** the system updates the post and returns the updated representation while preserving its author, identifier, and original creation timestamp

#### Scenario: Non-author updates a post
- **WHEN** an authenticated user who is not the post author submits an update
- **THEN** the system returns `403 Forbidden` and leaves the post unchanged

#### Scenario: Update makes a post empty
- **WHEN** the author submits an update with neither text nor photo URL
- **THEN** the system returns `400 Bad Request` and leaves the existing post unchanged

#### Scenario: Update an unknown post
- **WHEN** an authenticated user submits an update for a post identifier that does not exist
- **THEN** the system returns `404 Not Found`

#### Scenario: Update without authentication
- **WHEN** a client submits `PUT /api/posts/{id}` without a valid JWT
- **THEN** the system returns `401 Unauthorized` and does not mutate a post

### Requirement: Authors can delete their own posts

The system SHALL expose `DELETE /api/posts/{id}`. The endpoint SHALL require a valid JWT and SHALL allow only the post author to delete the post.

#### Scenario: Author deletes a post
- **WHEN** the authenticated author deletes an existing post
- **THEN** the system removes the post and returns a successful deletion response

#### Scenario: Non-author deletes a post
- **WHEN** an authenticated user who is not the post author requests deletion
- **THEN** the system returns `403 Forbidden` and the post remains available

#### Scenario: Delete an unknown post
- **WHEN** an authenticated user requests deletion for a post identifier that does not exist
- **THEN** the system returns `404 Not Found`

#### Scenario: Delete without authentication
- **WHEN** a client submits `DELETE /api/posts/{id}` without a valid JWT
- **THEN** the system returns `401 Unauthorized` and does not remove a post
