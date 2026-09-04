package gft.goodfoodtoday.backend.post.dto;

import gft.goodfoodtoday.backend.post.Post;
import gft.goodfoodtoday.backend.user.User;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class PostMapperTest {

    @Test
    void mapsPostWithoutCredentialData() throws Exception {
        User author = new User("mapper-post@example.com", "Ana", null, null);
        author.setPasswordHash("secret-hash");
        setField(User.class, author, "id", 1L);
        Post post = new Post(author, "Salad", null, null);
        setField(Post.class, post, "id", 2L);
        LocalDateTime createdAt = LocalDateTime.now();
        setField(Post.class, post, "createdAt", createdAt);

        PostResponse response = PostMapper.toResponse(post);

        assertThat(response.id()).isEqualTo(2L);
        assertThat(response.author().id()).isEqualTo(1L);
        assertThat(response.author().email()).isEqualTo("mapper-post@example.com");
        assertThat(response.createdAt()).isEqualTo(createdAt);
    }

    private static void setField(Class<?> type, Object target, String name, Object value) throws Exception {
        Field field = type.getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }
}
