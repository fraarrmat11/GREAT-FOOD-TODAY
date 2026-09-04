package gft.goodfoodtoday.backend.place;

import gft.goodfoodtoday.backend.place.dto.PlaceCreateRequest;
import gft.goodfoodtoday.backend.place.dto.PlaceUpdateRequest;
import gft.goodfoodtoday.backend.user.NotProfileOwnerException;
import gft.goodfoodtoday.backend.user.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlaceServiceTest {

    @Mock
    private PlaceRepository placeRepository;

    @InjectMocks
    private PlaceService placeService;

    @Test
    void creatorCanUpdatePlace() throws Exception {
        User creator = userWithId(1L, "ana@example.com", "Ana");
        Place place = placeWithId(5L, creator);
        when(placeRepository.findById(5L)).thenReturn(Optional.of(place));
        when(placeRepository.save(place)).thenReturn(place);

        placeService.update(5L, creator,
                new PlaceUpdateRequest("Updated", "New address", "Italian", PriceRange.HIGH));

        verify(placeRepository).save(place);
    }

    @Test
    void nonCreatorCannotUpdatePlace() throws Exception {
        User creator = userWithId(1L, "ana@example.com", "Ana");
        User otherUser = userWithId(2L, "bob@example.com", "Bob");
        when(placeRepository.findById(5L)).thenReturn(Optional.of(placeWithId(5L, creator)));

        assertThatThrownBy(() -> placeService.update(5L, otherUser,
                new PlaceUpdateRequest("Hacked", "New address", null, null)))
                .isInstanceOf(NotProfileOwnerException.class);
    }

    private static Place placeWithId(Long id, User creator) throws Exception {
        Place place = new Place("La Huerta", "Calle Mayor 1", null, null, creator);
        Field field = Place.class.getDeclaredField("id");
        field.setAccessible(true);
        field.set(place, id);
        return place;
    }

    private static User userWithId(Long id, String email, String name) throws Exception {
        User user = new User(email, name, null, null);
        Field field = User.class.getDeclaredField("id");
        field.setAccessible(true);
        field.set(user, id);
        return user;
    }
}
