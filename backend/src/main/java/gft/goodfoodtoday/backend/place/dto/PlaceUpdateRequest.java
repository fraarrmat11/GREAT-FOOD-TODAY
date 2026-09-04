package gft.goodfoodtoday.backend.place.dto;

import gft.goodfoodtoday.backend.place.PriceRange;
import jakarta.validation.constraints.NotBlank;

public record PlaceUpdateRequest(
        @NotBlank String name,
        @NotBlank String address,
        String cuisineType,
        PriceRange priceRange) {
}
