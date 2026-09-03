package gft.goodfoodtoday.backend.auth;

import java.time.Instant;

public record TokenResponse(String accessToken, Instant expiresAt) {
}