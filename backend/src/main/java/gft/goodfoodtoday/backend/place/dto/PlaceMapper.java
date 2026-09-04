package gft.goodfoodtoday.backend.place.dto;

import gft.goodfoodtoday.backend.place.Place;
import gft.goodfoodtoday.backend.user.dto.UserMapper;

public final class PlaceMapper {

    private PlaceMapper() {
    }

    public static PlaceResponse toResponse(Place place) {
        return new PlaceResponse(
                place.getId(),
                place.getName(),
                place.getAddress(),
                place.getCuisineType(),
                place.getPriceRange(),
                UserMapper.toResponse(place.getCreatedBy()),
                place.getCreatedAt());
    }
}
