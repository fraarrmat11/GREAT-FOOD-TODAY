package gft.goodfoodtoday.backend.user.dto;

import gft.goodfoodtoday.backend.user.User;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;

class UserMapperTest {

    @Test
    void mapsAllFieldsToResponse() throws Exception {
        User user = new User("ana@example.com", "Ana", "https://example.com/ana.png", "Engineering");
        Field idField = User.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(user, 7L);

        UserResponse response = UserMapper.toResponse(user);

        assertThat(response.id()).isEqualTo(7L);
        assertThat(response.email()).isEqualTo("ana@example.com");
        assertThat(response.name()).isEqualTo("Ana");
        assertThat(response.avatarUrl()).isEqualTo("https://example.com/ana.png");
        assertThat(response.department()).isEqualTo("Engineering");
    }
}
