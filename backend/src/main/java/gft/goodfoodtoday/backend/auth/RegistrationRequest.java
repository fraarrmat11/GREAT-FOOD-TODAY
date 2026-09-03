package gft.goodfoodtoday.backend.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RegistrationRequest(
        @NotBlank @Email String email,
        @NotBlank String name,
        @NotBlank String password) {
}