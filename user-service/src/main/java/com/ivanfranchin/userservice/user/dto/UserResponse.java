package com.ivanfranchin.userservice.user.dto;

import com.ivanfranchin.userservice.user.model.User;

public record UserResponse(Long id, String email, String fullName, Boolean active) {

    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getActive()
        );
    }
}
