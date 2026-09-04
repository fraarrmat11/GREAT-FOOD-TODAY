package gft.goodfoodtoday.backend.place;

import gft.goodfoodtoday.backend.user.User;
import gft.goodfoodtoday.backend.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class PlaceRepositoryTest {

    @Autowired
    private PlaceRepository placeRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void savesAndRetrievesAPlace() {
        User creator = userRepository.saveAndFlush(new User("ana@example.com", "Ana", null, null));
        Place saved = placeRepository.saveAndFlush(
                new Place("La Huerta", "Calle Mayor 1", "Mediterranean", PriceRange.MEDIUM, creator));

        Optional<Place> found = placeRepository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("La Huerta");
        assertThat(found.get().getAddress()).isEqualTo("Calle Mayor 1");
        assertThat(found.get().getCuisineType()).isEqualTo("Mediterranean");
        assertThat(found.get().getPriceRange()).isEqualTo(PriceRange.MEDIUM);
        assertThat(found.get().getCreatedBy().getId()).isEqualTo(creator.getId());
        assertThat(found.get().getCreatedAt()).isNotNull();
    }
}
