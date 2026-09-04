package gft.goodfoodtoday.backend.place;

import gft.goodfoodtoday.backend.place.dto.PlaceCreateRequest;
import gft.goodfoodtoday.backend.place.dto.PlaceUpdateRequest;
import gft.goodfoodtoday.backend.user.CurrentUserResolver;
import gft.goodfoodtoday.backend.user.User;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PlaceController.class)
class PlaceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PlaceService placeService;

    @MockitoBean
    private CurrentUserResolver currentUserResolver;

    @Test
    void listReturnsPlaces() throws Exception {
        User creator = userWithId(1L, "ana@example.com", "Ana");
        when(placeService.findAll()).thenReturn(List.of(placeWithId(2L, creator)));

        mockMvc.perform(get("/api/places"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("La Huerta"));
    }

    @Test
    void createReturns201AndUsesCurrentUser() throws Exception {
        User creator = userWithId(1L, "ana@example.com", "Ana");
        Place created = placeWithId(2L, creator);
        when(currentUserResolver.resolve(any())).thenReturn(Optional.of(creator));
        when(placeService.create(any(PlaceCreateRequest.class), eq(creator))).thenReturn(created);

        mockMvc.perform(post("/api/places")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new PlaceCreateRequest("La Huerta", "Calle Mayor 1", null, PriceRange.MEDIUM))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.createdBy.id").value(1));
    }

    @Test
    void createReturns400ForBlankRequiredField() throws Exception {
        mockMvc.perform(post("/api/places")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new PlaceCreateRequest(" ", "Calle Mayor 1", null, null))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createReturns400ForInvalidPriceRange() throws Exception {
        mockMvc.perform(post("/api/places")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"La Huerta\",\"address\":\"Calle Mayor 1\",\"priceRange\":\"INVALID\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteReturns204ForAuthenticatedCreator() throws Exception {
        User creator = userWithId(1L, "ana@example.com", "Ana");
        when(currentUserResolver.resolve(any())).thenReturn(Optional.of(creator));

        mockMvc.perform(delete("/api/places/2"))
                .andExpect(status().isNoContent());
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
