package gft.goodfoodtoday.backend.post.dto;

import gft.goodfoodtoday.backend.place.dto.PlaceResponse;
import gft.goodfoodtoday.backend.user.dto.UserResponse;

import java.time.LocalDateTime;

public record PostResponse(
        Long id,
        UserResponse author,
        String text,
        String photoUrl,
        PlaceResponse place,
        LocalDateTime createdAt) {
}
