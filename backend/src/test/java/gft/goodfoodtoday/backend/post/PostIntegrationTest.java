package gft.goodfoodtoday.backend.post;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PostIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void authenticatedUsersCanCreateBrowseUpdateAndDeleteOwnPosts() throws Exception {
        String ownerToken = registerAndLogin("post-owner@example.com", "Post Owner");
        String otherToken = registerAndLogin("post-other@example.com", "Post Other");

        String postResponse = mockMvc.perform(post("/api/posts")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\":\"Paella\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.author.email").value("post-owner@example.com"))
                .andReturn().getResponse().getContentAsString();
        long postId = objectMapper.readTree(postResponse).get("id").asLong();

        mockMvc.perform(get("/api/posts?page=0&size=10")
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].text").value("Paella"))
                .andExpect(jsonPath("$.pageable.pageSize").value(10));

        mockMvc.perform(put("/api/posts/" + postId)
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\":\"Updated paella\",\"photoUrl\":\"https://example.com/paella.jpg\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("Updated paella"));

        mockMvc.perform(put("/api/posts/" + postId)
                        .header("Authorization", "Bearer " + otherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\":\"Hacked\"}"))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/api/posts/" + postId)
                        .header("Authorization", "Bearer " + otherToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/api/posts/" + postId)
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isNoContent());
    }

    @Test
    void postEndpointsRejectMissingTokenAndEmptyContent() throws Exception {
        mockMvc.perform(get("/api/posts"))
                .andExpect(status().isUnauthorized());

        String token = registerAndLogin("empty-post-integration@example.com", "Empty Post User");
        mockMvc.perform(post("/api/posts")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\":\" \",\"photoUrl\":null}"))
                .andExpect(status().isBadRequest());
    }

    private String registerAndLogin(String email, String name) throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"name\":\"" + name
                                + "\",\"password\":\"secret\"}"))
                .andExpect(status().isCreated());

        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"secret\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode token = objectMapper.readTree(response).get("accessToken");
        return token.asText();
    }
}
