package gft.goodfoodtoday.backend.post;

import gft.goodfoodtoday.backend.place.Place;
import gft.goodfoodtoday.backend.place.PlaceNotFoundException;
import gft.goodfoodtoday.backend.place.PlaceRepository;
import gft.goodfoodtoday.backend.post.dto.PostRequest;
import gft.goodfoodtoday.backend.user.NotProfileOwnerException;
import gft.goodfoodtoday.backend.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class PostService {

    public static final int DEFAULT_PAGE = 0;
    public static final int DEFAULT_SIZE = 20;

    private final PostRepository postRepository;
    private final PlaceRepository placeRepository;

    public PostService(PostRepository postRepository, PlaceRepository placeRepository) {
        this.postRepository = postRepository;
        this.placeRepository = placeRepository;
    }

    public Post create(PostRequest request, User currentUser) {
        validateContent(request);
        Place place = resolvePlace(request.placeId());
        return postRepository.save(new Post(currentUser, request.text(), request.photoUrl(), place));
    }

    public Page<Post> findAll(Pageable pageable) {
        return postRepository.findAllByOrderByCreatedAtDescIdDesc(pageable);
    }

    public Post update(Long id, User currentUser, PostRequest request) {
        Post post = getById(id);
        assertOwner(post, currentUser);
        validateContent(request);
        Place place = resolvePlace(request.placeId());
        post.setText(request.text());
        post.setPhotoUrl(request.photoUrl());
        post.setPlace(place);
        return postRepository.save(post);
    }

    public void delete(Long id, User currentUser) {
        Post post = getById(id);
        assertOwner(post, currentUser);
        postRepository.delete(post);
    }

    public Pageable defaultPageable() {
        return PageRequest.of(DEFAULT_PAGE, DEFAULT_SIZE);
    }

    private Post getById(Long id) {
        return postRepository.findById(id).orElseThrow(() -> new PostNotFoundException(id));
    }

    private Place resolvePlace(Long placeId) {
        if (placeId == null) {
            return null;
        }
        return placeRepository.findById(placeId).orElseThrow(() -> new PlaceNotFoundException(placeId));
    }

    private void validateContent(PostRequest request) {
        if (isBlank(request.text()) && isBlank(request.photoUrl())) {
            throw new EmptyPostException();
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private void assertOwner(Post post, User currentUser) {
        if (!post.getAuthor().getId().equals(currentUser.getId())) {
            throw new NotProfileOwnerException();
        }
    }
}
