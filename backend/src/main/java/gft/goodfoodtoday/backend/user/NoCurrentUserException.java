package gft.goodfoodtoday.backend.user;

/** Thrown when GET /api/users/me is requested but no current user can be determined. */
public class NoCurrentUserException extends RuntimeException {
    public NoCurrentUserException() {
        super("No current user");
    }
}
