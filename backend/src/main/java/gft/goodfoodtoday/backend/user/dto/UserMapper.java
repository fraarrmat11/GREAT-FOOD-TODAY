package gft.goodfoodtoday.backend.user.dto;

import gft.goodfoodtoday.backend.user.User;

public final class UserMapper {

    private UserMapper() {
    }

    public static UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getAvatarUrl(),
                user.getDepartment());
    }
}
