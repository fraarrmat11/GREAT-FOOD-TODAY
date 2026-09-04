package gft.goodfoodtoday.backend.place.dto;

import gft.goodfoodtoday.backend.place.Place;
import gft.goodfoodtoday.backend.place.PriceRange;
import gft.goodfoodtoday.backend.user.User;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class PlaceMapperTest {

    @Test
    void mapsPlaceWithoutExposingCredentialData() throws Exception {
        User creator = new User("ana@example.com", "Ana", null, "Engineering");
        creator.setPasswordHash("secret-hash");
        setField(User.class, creator, "id", 1L);

        Place place = new Place("La Huerta", "Calle Mayor 1", "Mediterranean", PriceRange.MEDIUM, creator);
        setField(Place.class, place, "id", 2L);
        setField(Place.class, place, "createdAt", LocalDateTime.now());

        PlaceResponse response = PlaceMapper.toResponse(place);

        assertThat(response.id()).isEqualTo(2L);
        assertThat(response.createdBy()).isEqualTo(
            new gft.goodfoodtoday.backend.user.dto.UserResponse(1L, "ana@example.com", "Ana", null, "Engineering"));
    }

    private static void setField(Class<?> type, Object target, String name, Object value) throws Exception {
        Field field = type.getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }
}
