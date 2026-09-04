package gft.goodfoodtoday.backend.post;

public class EmptyPostException extends RuntimeException {

    public EmptyPostException() {
        super("A post must contain text or a photo");
    }
}
