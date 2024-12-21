package com.ivanfranchin.userservice.rest.dto;

import com.ivanfranchin.userservice.model.User;

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
