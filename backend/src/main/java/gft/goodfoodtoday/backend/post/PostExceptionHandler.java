package gft.goodfoodtoday.backend.post;

import gft.goodfoodtoday.backend.place.PlaceNotFoundException;
import gft.goodfoodtoday.backend.user.NoCurrentUserException;
import gft.goodfoodtoday.backend.user.NotProfileOwnerException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice(basePackageClasses = PostController.class)
public class PostExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getField() + " " + error.getDefaultMessage())
                .orElse("Validation failed");
        return ResponseEntity.badRequest().body(Map.of("error", message));
    }

    @ExceptionHandler(EmptyPostException.class)
    public ResponseEntity<Map<String, String>> handleEmptyPost(EmptyPostException exception) {
        return ResponseEntity.badRequest().body(Map.of("error", exception.getMessage()));
    }

    @ExceptionHandler({PostNotFoundException.class, PlaceNotFoundException.class})
    public ResponseEntity<Map<String, String>> handleNotFound(RuntimeException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", exception.getMessage()));
    }

    @ExceptionHandler(NotProfileOwnerException.class)
    public ResponseEntity<Map<String, String>> handleForbidden(NotProfileOwnerException exception) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", exception.getMessage()));
    }

    @ExceptionHandler(NoCurrentUserException.class)
    public ResponseEntity<Map<String, String>> handleUnauthorized(NoCurrentUserException exception) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", exception.getMessage()));
    }
}
