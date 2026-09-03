package gft.goodfoodtoday.backend.user.dto;

public record UserResponse(
        Long id,
        String email,
        String name,
        String avatarUrl,
        String department) {
}
