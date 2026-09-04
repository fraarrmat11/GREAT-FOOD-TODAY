package gft.goodfoodtoday.backend.post.dto;

public record PostRequest(
        String text,
        String photoUrl,
        Long placeId) {
}
