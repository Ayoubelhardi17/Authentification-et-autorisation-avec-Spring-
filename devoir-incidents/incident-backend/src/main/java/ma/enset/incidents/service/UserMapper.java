package ma.enset.incidents.service;

import ma.enset.incidents.dto.Dto.UserResponse;
import ma.enset.incidents.model.User;

public final class UserMapper {
    private UserMapper() {}

    public static UserResponse toResponse(User u) {
        return new UserResponse(u.getId(), u.getFullName(), u.getEmail(), u.getRole(), u.isEnabled(), u.getCreatedAt());
    }
}
