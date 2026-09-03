package gft.goodfoodtoday.backend.user;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class CurrentUserResolverTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final CurrentUserResolver resolver = new CurrentUserResolver(userRepository);

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void resolvesUserFromAuthenticatedSecurityContext() {
        User user = new User("ana@example.com", "Ana", null, null);
        when(userRepository.findById(42L)).thenReturn(Optional.of(user));

        HttpServletRequest request = mock(HttpServletRequest.class);
        setAuthentication("42");

        assertThat(resolver.resolve(request)).contains(user);
    }

    @Test
    void returnsEmptyWhenNoAuthenticationExists() {
        HttpServletRequest request = mock(HttpServletRequest.class);

        assertThat(resolver.resolve(request)).isEmpty();
        verifyNoInteractions(userRepository);
    }

    @Test
    void returnsEmptyWhenAuthenticatedSubjectIsNotANumber() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        setAuthentication("not-a-number");

        assertThat(resolver.resolve(request)).isEmpty();
    }

    private void setAuthentication(String subject) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(UsernamePasswordAuthenticationToken.authenticated(subject, null, List.of()));
        SecurityContextHolder.setContext(context);
    }
}
