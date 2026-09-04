package gft.goodfoodtoday.backend.place;

import gft.goodfoodtoday.backend.user.NotProfileOwnerException;
import gft.goodfoodtoday.backend.user.User;
import gft.goodfoodtoday.backend.place.dto.PlaceCreateRequest;
import gft.goodfoodtoday.backend.place.dto.PlaceUpdateRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PlaceService {

    private final PlaceRepository placeRepository;

    public PlaceService(PlaceRepository placeRepository) {
        this.placeRepository = placeRepository;
    }

    public List<Place> findAll() {
        return placeRepository.findAll();
    }

    public Place getById(Long id) {
        return placeRepository.findById(id).orElseThrow(() -> new PlaceNotFoundException(id));
    }

    public Place create(PlaceCreateRequest request, User currentUser) {
        return placeRepository.save(new Place(
                request.name(), request.address(), request.cuisineType(), request.priceRange(), currentUser));
    }

    public Place update(Long id, User currentUser, PlaceUpdateRequest request) {
        Place place = getById(id);
        assertOwner(place, currentUser);
        place.setName(request.name());
        place.setAddress(request.address());
        place.setCuisineType(request.cuisineType());
        place.setPriceRange(request.priceRange());
        return placeRepository.save(place);
    }

    public void delete(Long id, User currentUser) {
        Place place = getById(id);
        assertOwner(place, currentUser);
        placeRepository.delete(place);
    }

    private void assertOwner(Place place, User currentUser) {
        if (!place.getCreatedBy().getId().equals(currentUser.getId())) {
            throw new NotProfileOwnerException();
        }
    }
}
