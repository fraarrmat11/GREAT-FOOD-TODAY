package gft.goodfoodtoday.backend.user;

import gft.goodfoodtoday.backend.user.dto.UserCreateRequest;
import gft.goodfoodtoday.backend.user.dto.UserMapper;
import gft.goodfoodtoday.backend.user.dto.UserResponse;
import gft.goodfoodtoday.backend.user.dto.UserUpdateRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final CurrentUserResolver currentUserResolver;

    public UserController(UserService userService, CurrentUserResolver currentUserResolver) {
        this.userService = userService;
        this.currentUserResolver = currentUserResolver;
    }

    @PostMapping
    public ResponseEntity<UserResponse> create(@Valid @RequestBody UserCreateRequest request) {
        User created = userService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(UserMapper.toResponse(created));
    }

    @GetMapping("/{id}")
    public UserResponse getById(@PathVariable Long id) {
        return UserMapper.toResponse(userService.getById(id));
    }

    @GetMapping("/me")
    public UserResponse getCurrentUser(HttpServletRequest request) {
        User currentUser = currentUserResolver.resolve(request).orElseThrow(NoCurrentUserException::new);
        return UserMapper.toResponse(currentUser);
    }

    @PutMapping("/{id}")
    public UserResponse update(@PathVariable Long id, @Valid @RequestBody UserUpdateRequest request,
            HttpServletRequest servletRequest) {
        User currentUser = currentUserResolver.resolve(servletRequest).orElseThrow(NoCurrentUserException::new);
        return UserMapper.toResponse(userService.update(id, currentUser, request));
    }
}
