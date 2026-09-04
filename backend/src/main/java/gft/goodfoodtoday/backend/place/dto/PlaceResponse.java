package gft.goodfoodtoday.backend.place.dto;

import gft.goodfoodtoday.backend.place.PriceRange;
import gft.goodfoodtoday.backend.user.dto.UserResponse;

import java.time.LocalDateTime;

public record PlaceResponse(
        Long id,
        String name,
        String address,
        String cuisineType,
        PriceRange priceRange,
        UserResponse createdBy,
        LocalDateTime createdAt) {
}
