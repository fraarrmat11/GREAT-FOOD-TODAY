package gft.goodfoodtoday.backend.post;

import gft.goodfoodtoday.backend.place.PlaceRepository;
import gft.goodfoodtoday.backend.post.dto.PostRequest;
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
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private PlaceRepository placeRepository;

    @InjectMocks
    private PostService postService;

    @Test
    void createsTextOnlyPostForCurrentUser() throws Exception {
        User author = userWithId(1L, "ana-post-service@example.com", "Ana");
        Post saved = new Post(author, "Tortilla", null, null);
        when(postRepository.save(any(Post.class))).thenReturn(saved);

        postService.create(new PostRequest("Tortilla", null, null), author);

        verify(postRepository).save(any(Post.class));
    }

    @Test
    void rejectsEmptyPostBeforePersistence() throws Exception {
        User author = userWithId(1L, "empty-post@example.com", "Ana");

        assertThatThrownBy(() -> postService.create(new PostRequest(" ", null, null), author))
                .isInstanceOf(EmptyPostException.class);
    }

    @Test
    void nonAuthorCannotUpdatePost() throws Exception {
        User author = userWithId(1L, "owner-post@example.com", "Ana");
        User otherUser = userWithId(2L, "other-post@example.com", "Bob");
        Post post = new Post(author, "Original", null, null);
        setField(Post.class, post, "id", 5L);
        when(postRepository.findById(5L)).thenReturn(Optional.of(post));

        assertThatThrownBy(() -> postService.update(5L, otherUser,
                new PostRequest("Changed", null, null)))
                .isInstanceOf(NotProfileOwnerException.class);
    }

    @Test
    void authorCanDeletePost() throws Exception {
        User author = userWithId(1L, "delete-post@example.com", "Ana");
        Post post = new Post(author, "Delete me", null, null);
        setField(Post.class, post, "id", 6L);
        when(postRepository.findById(6L)).thenReturn(Optional.of(post));

        postService.delete(6L, author);

        verify(postRepository).delete(post);
    }

    private static User userWithId(Long id, String email, String name) throws Exception {
        User user = new User(email, name, null, null);
        setField(User.class, user, "id", id);
        return user;
    }

    private static void setField(Class<?> type, Object target, String name, Object value) throws Exception {
        Field field = type.getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }
}
