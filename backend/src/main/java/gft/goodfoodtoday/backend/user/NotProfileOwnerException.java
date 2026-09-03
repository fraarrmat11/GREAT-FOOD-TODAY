package gft.goodfoodtoday.backend.user;

/** Thrown when the current user tries to modify a profile that is not their own. */
public class NotProfileOwnerException extends RuntimeException {
    public NotProfileOwnerException() {
        super("Cannot modify another user's profile");
    }
}
