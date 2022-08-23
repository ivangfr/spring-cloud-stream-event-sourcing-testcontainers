package com.ivanfranchin.userservice.rest.dto;

public record UserResponse(Long id, String email, String fullName, Boolean active) {
}
