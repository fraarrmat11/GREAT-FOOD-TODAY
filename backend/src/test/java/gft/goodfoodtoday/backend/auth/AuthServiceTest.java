package gft.goodfoodtoday.backend.auth;

import gft.goodfoodtoday.backend.user.User;
import gft.goodfoodtoday.backend.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthServiceTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final TokenService tokenService = mock(TokenService.class);
    private final AuthService authService = new AuthService(userRepository, passwordEncoder, tokenService);

    @Test
    void registerStoresAHashAndNormalizesEmail() {
        when(userRepository.existsByEmail("ana@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User registered = authService.register(new RegistrationRequest(" ANA@EXAMPLE.COM ", " Ana ", "secret"));

        assertThat(registered.getEmail()).isEqualTo("ana@example.com");
        assertThat(registered.getName()).isEqualTo("Ana");
        assertThat(registered.getPasswordHash()).isNotEqualTo("secret");
        assertThat(passwordEncoder.matches("secret", registered.getPasswordHash())).isTrue();
    }

    @Test
    void loginReturnsTokenForCorrectCredentials() {
        User user = new User("ana@example.com", "Ana", null, null);
        user.setPasswordHash(passwordEncoder.encode("secret"));
        TokenResponse token = new TokenResponse("signed-token", null);
        when(userRepository.findByEmail("ana@example.com")).thenReturn(Optional.of(user));
        when(tokenService.issue(user)).thenReturn(token);

        assertThat(authService.login(new AuthRequest("ana@example.com", "secret"))).isSameAs(token);
    }

    @Test
    void loginRejectsUnknownOrIncorrectCredentials() {
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(new AuthRequest("unknown@example.com", "secret")))
                .isInstanceOf(InvalidCredentialsException.class);
        verify(tokenService, org.mockito.Mockito.never()).issue(any());
    }

    @Test
    void registerRejectsDuplicateEmail() {
        when(userRepository.existsByEmail("ana@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(
                new RegistrationRequest("ana@example.com", "Ana", "secret")))
                .isInstanceOf(gft.goodfoodtoday.backend.user.DuplicateEmailException.class);
        verify(userRepository, org.mockito.Mockito.never()).save(any());
    }

    @Test
    void invalidLoginDoesNotLogSubmittedPassword() {
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());
        Logger logger = (Logger) LoggerFactory.getLogger(AuthService.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);

        try {
            assertThatThrownBy(() -> authService.login(new AuthRequest("unknown@example.com", "super-secret")))
                    .isInstanceOf(InvalidCredentialsException.class);
            assertThat(appender.list)
                    .allMatch(event -> !event.getFormattedMessage().contains("super-secret"));
        } finally {
            logger.detachAppender(appender);
        }
    }
}