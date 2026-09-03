package gft.goodfoodtoday.backend.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void savesAndRetrievesAUser() {
        User saved = userRepository.saveAndFlush(
                new User("ana@example.com", "Ana", "https://example.com/ana.png", "Engineering"));

        Optional<User> found = userRepository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("ana@example.com");
        assertThat(found.get().getName()).isEqualTo("Ana");
        assertThat(found.get().getAvatarUrl()).isEqualTo("https://example.com/ana.png");
        assertThat(found.get().getDepartment()).isEqualTo("Engineering");
        assertThat(found.get().getCreatedAt()).isNotNull();
    }

    @Test
    void rejectsDuplicateEmail() {
        userRepository.saveAndFlush(new User("dup@example.com", "First", null, null));

        assertThatThrownBy(() ->
                userRepository.saveAndFlush(new User("dup@example.com", "Second", null, null)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
