package gft.goodfoodtoday.backend.place;

import gft.goodfoodtoday.backend.place.dto.PlaceCreateRequest;
import gft.goodfoodtoday.backend.place.dto.PlaceMapper;
import gft.goodfoodtoday.backend.place.dto.PlaceResponse;
import gft.goodfoodtoday.backend.place.dto.PlaceUpdateRequest;
import gft.goodfoodtoday.backend.user.CurrentUserResolver;
import gft.goodfoodtoday.backend.user.NoCurrentUserException;
import gft.goodfoodtoday.backend.user.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/places")
public class PlaceController {

    private final PlaceService placeService;
    private final CurrentUserResolver currentUserResolver;

    public PlaceController(PlaceService placeService, CurrentUserResolver currentUserResolver) {
        this.placeService = placeService;
        this.currentUserResolver = currentUserResolver;
    }

    @GetMapping
    public List<PlaceResponse> findAll() {
        return placeService.findAll().stream().map(PlaceMapper::toResponse).toList();
    }

    @GetMapping("/{id}")
    public PlaceResponse getById(@PathVariable Long id) {
        return PlaceMapper.toResponse(placeService.getById(id));
    }

    @PostMapping
    public ResponseEntity<PlaceResponse> create(@Valid @RequestBody PlaceCreateRequest request,
            HttpServletRequest servletRequest) {
        User currentUser = currentUser(servletRequest);
        Place created = placeService.create(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(PlaceMapper.toResponse(created));
    }

    @PutMapping("/{id}")
    public PlaceResponse update(@PathVariable Long id, @Valid @RequestBody PlaceUpdateRequest request,
            HttpServletRequest servletRequest) {
        return PlaceMapper.toResponse(placeService.update(id, currentUser(servletRequest), request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, HttpServletRequest servletRequest) {
        placeService.delete(id, currentUser(servletRequest));
        return ResponseEntity.noContent().build();
    }

    private User currentUser(HttpServletRequest request) {
        return currentUserResolver.resolve(request).orElseThrow(NoCurrentUserException::new);
    }
}
