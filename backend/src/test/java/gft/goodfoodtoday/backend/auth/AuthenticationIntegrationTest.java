package gft.goodfoodtoday.backend.auth;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.test.web.servlet.MockMvc;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;

import java.security.KeyPairGenerator;
import java.time.Instant;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthenticationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

        @Autowired
        private JwtEncoder jwtEncoder;

        @Autowired
        private SecurityProperties securityProperties;

    @Test
    void registrationLoginAndCurrentUserFlowUsesJwtIdentity() throws Exception {
        String email = "jwt-flow@example.com";
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","name":"JWT User","password":"secret"}
                                """.formatted(email)))
                .andExpect(status().isCreated());

        String loginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"secret"}
                                """.formatted(email)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isString())
                .andReturn()
                .getResponse()
                .getContentAsString();
        JsonNode token = objectMapper.readTree(loginResponse).get("accessToken");

        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer " + token.asText())
                        .header("X-User-Id", "999999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email));
    }

    @Test
    void protectedEndpointRejectsMissingToken() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void placeEndpointsRejectMissingToken() throws Exception {
        mockMvc.perform(get("/api/places"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/places")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"La Huerta\",\"address\":\"Calle Mayor 1\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void placeCreationUsesJwtIdentityInsteadOfHeader() throws Exception {
        String email = "jwt-place@example.com";
        String registrationResponse = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"%s\",\"name\":\"Place User\",\"password\":\"secret\"}"
                                .formatted(email)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long userId = objectMapper.readTree(registrationResponse).get("id").asLong();
        String loginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"%s\",\"password\":\"secret\"}".formatted(email)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String token = objectMapper.readTree(loginResponse).get("accessToken").asText();

        mockMvc.perform(post("/api/places")
                        .header("Authorization", "Bearer " + token)
                        .header("X-User-Id", "999999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"La Huerta\",\"address\":\"Calle Mayor 1\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.createdBy.id").value(userId));
    }

        @Test
        void protectedEndpointRejectsMalformedToken() throws Exception {
                mockMvc.perform(get("/api/users/me")
                                                .header("Authorization", "Bearer not-a-jwt"))
                                .andExpect(status().isUnauthorized());
        }

                    @Test
                    void protectedEndpointRejectsExpiredToken() throws Exception {
                        String token = encode(jwtClaims("999999", Instant.now().minusSeconds(60), securityProperties.issuer()), jwtEncoder);

                        mockMvc.perform(get("/api/users/me").header("Authorization", "Bearer " + token))
                                .andExpect(status().isUnauthorized());
                    }

                    @Test
                    void protectedEndpointRejectsWrongIssuer() throws Exception {
                        String token = encode(jwtClaims("999999", Instant.now().plusSeconds(60), "https://wrong.example"), jwtEncoder);

                        mockMvc.perform(get("/api/users/me").header("Authorization", "Bearer " + token))
                                .andExpect(status().isUnauthorized());
                    }

                    @Test
                    void protectedEndpointRejectsInvalidSignature() throws Exception {
                        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
                        generator.initialize(2048);
                        var keyPair = generator.generateKeyPair();
                        JwtEncoder otherEncoder = new NimbusJwtEncoder(new ImmutableJWKSet<>(new JWKSet(
                                new RSAKey.Builder((java.security.interfaces.RSAPublicKey) keyPair.getPublic())
                                        .privateKey(keyPair.getPrivate()).build())));
                        String token = encode(jwtClaims("999999", Instant.now().plusSeconds(60), securityProperties.issuer()), otherEncoder);

                        mockMvc.perform(get("/api/users/me").header("Authorization", "Bearer " + token))
                                .andExpect(status().isUnauthorized());
                    }

                    @Test
                    void protectedEndpointRejectsUnknownSubject() throws Exception {
                        String token = encode(jwtClaims("999999", Instant.now().plusSeconds(60), securityProperties.issuer()), jwtEncoder);

                        mockMvc.perform(get("/api/users/me").header("Authorization", "Bearer " + token))
                                .andExpect(status().isUnauthorized());
                    }

    @Test
    void loginRejectsIncorrectPassword() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"missing@example.com","password":"secret"}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Invalid credentials"));
    }

    @Test
    void bearerTokenCanUpdateItsOwnProfile() throws Exception {
        String email = "jwt-update@example.com";
        String registrationResponse = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","name":"Before","password":"secret"}
                                """.formatted(email)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long userId = objectMapper.readTree(registrationResponse).get("id").asLong();
        String loginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"secret"}
                                """.formatted(email)))
                .andReturn().getResponse().getContentAsString();
        String token = objectMapper.readTree(loginResponse).get("accessToken").asText();

        mockMvc.perform(put("/api/users/" + userId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"After","avatarUrl":null,"department":null}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("After"));
    }

        private JwtClaimsSet jwtClaims(String subject, Instant expiresAt, String issuer) {
                Instant issuedAt = expiresAt.isBefore(Instant.now()) ? expiresAt.minusSeconds(60) : Instant.now();
                return JwtClaimsSet.builder()
                                .issuer(issuer)
                                .subject(subject)
                        .issuedAt(issuedAt)
                                .expiresAt(expiresAt)
                                .build();
        }

        private String encode(JwtClaimsSet claims, JwtEncoder encoder) {
                return encoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
        }
}