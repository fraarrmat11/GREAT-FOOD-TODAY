package gft.goodfoodtoday.backend.user;

import tools.jackson.databind.ObjectMapper;
import gft.goodfoodtoday.backend.user.dto.UserCreateRequest;
import gft.goodfoodtoday.backend.user.dto.UserUpdateRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.lang.reflect.Field;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private CurrentUserResolver currentUserResolver;

    private static User userWithId(Long id, String email, String name) throws Exception {
        User user = new User(email, name, null, null);
        Field idField = User.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(user, id);
        return user;
    }

    @Test
    void createReturns201ForValidRequest() throws Exception {
        when(userService.create(any())).thenReturn(userWithId(1L, "ana@example.com", "Ana"));

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new UserCreateRequest("ana@example.com", "Ana", null, null))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("ana@example.com"));
    }

    @Test
    void createReturns400ForInvalidEmail() throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new UserCreateRequest("not-an-email", "Ana", null, null))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createReturns400ForBlankName() throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new UserCreateRequest("ana@example.com", " ", null, null))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createReturns409ForDuplicateEmail() throws Exception {
        when(userService.create(any())).thenThrow(new DuplicateEmailException("ana@example.com"));

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new UserCreateRequest("ana@example.com", "Ana", null, null))))
                .andExpect(status().isConflict());
    }

    @Test
    void getByIdReturns200ForExistingUser() throws Exception {
        when(userService.getById(1L)).thenReturn(userWithId(1L, "ana@example.com", "Ana"));

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("ana@example.com"));
    }

    @Test
    void getByIdReturns404ForUnknownUser() throws Exception {
        when(userService.getById(99L)).thenThrow(new UserNotFoundException(99L));

        mockMvc.perform(get("/api/users/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getMeReturns200WhenCurrentUserResolved() throws Exception {
        when(currentUserResolver.resolve(any())).thenReturn(Optional.of(userWithId(1L, "ana@example.com", "Ana")));

        mockMvc.perform(get("/api/users/me").header("X-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("ana@example.com"));
    }

    @Test
    void getMeReturns401WhenNoCurrentUser() throws Exception {
        when(currentUserResolver.resolve(any())).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void updateReturns200ForOwner() throws Exception {
        User owner = userWithId(1L, "ana@example.com", "Ana");
        when(currentUserResolver.resolve(any())).thenReturn(Optional.of(owner));
        when(userService.update(eq(1L), eq(owner), any()))
                .thenReturn(userWithId(1L, "ana@example.com", "Ana Updated"));

        mockMvc.perform(put("/api/users/1")
                        .header("X-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new UserUpdateRequest("Ana Updated", null, null))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Ana Updated"))
                .andExpect(jsonPath("$.email").value("ana@example.com"));
    }

    @Test
    void updateReturns400ForBlankName() throws Exception {
        User owner = userWithId(1L, "ana@example.com", "Ana");
        when(currentUserResolver.resolve(any())).thenReturn(Optional.of(owner));

        mockMvc.perform(put("/api/users/1")
                        .header("X-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new UserUpdateRequest(" ", null, null))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateReturns403ForNonOwner() throws Exception {
        User currentUser = userWithId(2L, "bob@example.com", "Bob");
        when(currentUserResolver.resolve(any())).thenReturn(Optional.of(currentUser));
        when(userService.update(eq(1L), eq(currentUser), any())).thenThrow(new NotProfileOwnerException());

        mockMvc.perform(put("/api/users/1")
                        .header("X-User-Id", "2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new UserUpdateRequest("Hacked", null, null))))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateReturns404ForUnknownUser() throws Exception {
        User currentUser = userWithId(2L, "bob@example.com", "Bob");
        when(currentUserResolver.resolve(any())).thenReturn(Optional.of(currentUser));
        when(userService.update(eq(99L), eq(currentUser), any())).thenThrow(new UserNotFoundException(99L));

        mockMvc.perform(put("/api/users/99")
                        .header("X-User-Id", "2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new UserUpdateRequest("Name", null, null))))
                .andExpect(status().isNotFound());
    }
}
