# place-directory Specification

## Purpose

Provide an authenticated directory of places that users can discover, reference, and maintain while preserving creator ownership and valid place data.

## Requirements

### Requirement: Users can browse places

The system SHALL expose `GET /api/places` to return all persisted places and SHALL expose `GET /api/places/{id}` to return one place by identifier. Both operations SHALL require a valid JWT.

#### Scenario: List places
- **WHEN** an authenticated user requests `GET /api/places`
- **THEN** the system returns a successful response containing the available places

#### Scenario: Get an existing place
- **WHEN** an authenticated user requests `GET /api/places/{id}` for an existing place
- **THEN** the system returns that place, including its name, address, optional cuisine type and price range, creator, and creation timestamp

#### Scenario: Get an unknown place
- **WHEN** an authenticated user requests `GET /api/places/{id}` for an identifier that does not exist
- **THEN** the system returns `404 Not Found`

#### Scenario: Browse without authentication
- **WHEN** a client requests either browse endpoint without a valid JWT
- **THEN** the system returns `401 Unauthorized`

### Requirement: Authenticated users can create places

The system SHALL expose `POST /api/places` for authenticated users. The request SHALL require a non-blank `name` and `address`, MAY include `cuisineType` and `priceRange`, and SHALL assign the authenticated user as creator.

#### Scenario: Create a valid place
- **WHEN** an authenticated user submits a place with a name and address
- **THEN** the system persists one place owned by that user and returns a successful response containing its identifier and stored fields

#### Scenario: Create with missing required data
- **WHEN** an authenticated user submits a place with a blank or missing name or address
- **THEN** the system returns `400 Bad Request` and does not persist the place

#### Scenario: Create without authentication
- **WHEN** a client submits `POST /api/places` without a valid JWT
- **THEN** the system returns `401 Unauthorized` and does not persist the place

### Requirement: Place creators can update or delete their places

The system SHALL expose `PUT /api/places/{id}` and `DELETE /api/places/{id}`. Only the place creator identified by the valid JWT SHALL be allowed to perform these operations.

#### Scenario: Creator updates a place
- **WHEN** the creator submits valid replacement data for an existing place
- **THEN** the system updates the place and returns a successful response

#### Scenario: Creator deletes a place
- **WHEN** the creator requests deletion of an existing place
- **THEN** the system deletes the place and returns a successful deletion response

#### Scenario: Another user attempts to mutate a place
- **WHEN** an authenticated user who is not the creator submits an update or delete request
- **THEN** the system returns `403 Forbidden` and leaves the place unchanged

#### Scenario: Mutate an unknown place
- **WHEN** an authenticated user submits an update or delete request for an identifier that does not exist
- **THEN** the system returns `404 Not Found`

#### Scenario: Update with missing required data
- **WHEN** the creator submits an update with a blank or missing name or address
- **THEN** the system returns `400 Bad Request` and leaves the existing place unchanged

#### Scenario: Mutation without authentication
- **WHEN** a client submits an update or delete request without a valid JWT
- **THEN** the system returns `401 Unauthorized` and does not mutate data

### Requirement: Place data preserves the directory contract

Each place SHALL have a generated identifier, a non-blank name, a non-blank address, a required creator, and a creation timestamp. `cuisineType` and `priceRange` SHALL be optional, and `priceRange` SHALL accept only `LOW`, `MEDIUM`, or `HIGH`.

#### Scenario: Optional place fields are absent
- **WHEN** a valid place is created without cuisine type or price range
- **THEN** the system stores and returns the place with those fields absent

#### Scenario: Invalid price range
- **WHEN** a client submits a price range outside `LOW`, `MEDIUM`, or `HIGH`
- **THEN** the system returns `400 Bad Request` and does not persist the place

#### Scenario: Repository persistence round trip
- **WHEN** a valid place is saved and then retrieved by its identifier
- **THEN** the retrieved place contains the persisted identity, required fields, creator relationship, and creation timestamp
