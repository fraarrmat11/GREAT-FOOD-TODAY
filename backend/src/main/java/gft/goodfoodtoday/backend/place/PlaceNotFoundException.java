package gft.goodfoodtoday.backend.place;

public class PlaceNotFoundException extends RuntimeException {

    public PlaceNotFoundException(Long id) {
        super("Place not found: " + id);
    }
}
