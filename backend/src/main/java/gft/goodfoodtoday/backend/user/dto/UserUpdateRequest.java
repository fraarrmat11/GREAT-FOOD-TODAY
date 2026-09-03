package gft.goodfoodtoday.backend.user.dto;

import jakarta.validation.constraints.NotBlank;

public record UserUpdateRequest(
        @NotBlank String name,
        String avatarUrl,
        String department) {
}
