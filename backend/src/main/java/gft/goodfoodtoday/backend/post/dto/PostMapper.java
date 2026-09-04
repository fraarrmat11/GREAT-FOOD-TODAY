package gft.goodfoodtoday.backend.post.dto;

import gft.goodfoodtoday.backend.place.dto.PlaceMapper;
import gft.goodfoodtoday.backend.post.Post;
import gft.goodfoodtoday.backend.user.dto.UserMapper;

public final class PostMapper {

    private PostMapper() {
    }

    public static PostResponse toResponse(Post post) {
        return new PostResponse(
                post.getId(),
                UserMapper.toResponse(post.getAuthor()),
                post.getText(),
                post.getPhotoUrl(),
                post.getPlace() == null ? null : PlaceMapper.toResponse(post.getPlace()),
                post.getCreatedAt());
    }
}
