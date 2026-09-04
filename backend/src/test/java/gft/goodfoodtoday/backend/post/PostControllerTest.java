package gft.goodfoodtoday.backend.post;

import gft.goodfoodtoday.backend.post.dto.PostRequest;
import gft.goodfoodtoday.backend.user.CurrentUserResolver;
import gft.goodfoodtoday.backend.user.User;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PostController.class)
class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PostService postService;

    @MockitoBean
    private CurrentUserResolver currentUserResolver;

    @Test
    void createReturns201WithAuthenticatedAuthor() throws Exception {
        User author = userWithId(1L);
        Post post = new Post(author, "Paella", null, null);
        setField(Post.class, post, "id", 2L);
        when(currentUserResolver.resolve(any())).thenReturn(Optional.of(author));
        when(postService.create(any(PostRequest.class), eq(author))).thenReturn(post);

        mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new PostRequest("Paella", null, null))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.author.id").value(1));
    }

    @Test
    void listReturnsPaginatedPosts() throws Exception {
        User author = userWithId(1L);
        Post post = new Post(author, "Paella", null, null);
        when(currentUserResolver.resolve(any())).thenReturn(Optional.of(author));
        when(postService.findAll(PageRequest.of(0, 20)))
                .thenReturn(new PageImpl<>(List.of(post), PageRequest.of(0, 20), 1));

        mockMvc.perform(get("/api/posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].text").value("Paella"));
    }

    @Test
    void createReturns401WhenCurrentUserIsMissing() throws Exception {
        when(currentUserResolver.resolve(any())).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new PostRequest("Paella", null, null))))
                .andExpect(status().isUnauthorized());
    }

    private static User userWithId(Long id) throws Exception {
        User user = new User("controller-post@example.com", "Ana", null, null);
        setField(User.class, user, "id", id);
        return user;
    }

    private static void setField(Class<?> type, Object target, String name, Object value) throws Exception {
        Field field = type.getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }
}
