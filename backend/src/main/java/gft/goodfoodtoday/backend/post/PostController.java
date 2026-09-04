package gft.goodfoodtoday.backend.post;

import gft.goodfoodtoday.backend.post.dto.PostMapper;
import gft.goodfoodtoday.backend.post.dto.PostRequest;
import gft.goodfoodtoday.backend.post.dto.PostResponse;
import gft.goodfoodtoday.backend.user.CurrentUserResolver;
import gft.goodfoodtoday.backend.user.NoCurrentUserException;
import gft.goodfoodtoday.backend.user.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;
    private final CurrentUserResolver currentUserResolver;

    public PostController(PostService postService, CurrentUserResolver currentUserResolver) {
        this.postService = postService;
        this.currentUserResolver = currentUserResolver;
    }

    @GetMapping
    public Page<PostResponse> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest request) {
        currentUser(request);
        return postService.findAll(PageRequest.of(page, size)).map(PostMapper::toResponse);
    }

    @PostMapping
    public ResponseEntity<PostResponse> create(@Valid @RequestBody PostRequest request,
            HttpServletRequest servletRequest) {
        Post created = postService.create(request, currentUser(servletRequest));
        return ResponseEntity.status(HttpStatus.CREATED).body(PostMapper.toResponse(created));
    }

    @PutMapping("/{id}")
    public PostResponse update(@PathVariable Long id, @Valid @RequestBody PostRequest request,
            HttpServletRequest servletRequest) {
        return PostMapper.toResponse(postService.update(id, currentUser(servletRequest), request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, HttpServletRequest servletRequest) {
        postService.delete(id, currentUser(servletRequest));
        return ResponseEntity.noContent().build();
    }

    private User currentUser(HttpServletRequest request) {
        return currentUserResolver.resolve(request).orElseThrow(NoCurrentUserException::new);
    }
}
