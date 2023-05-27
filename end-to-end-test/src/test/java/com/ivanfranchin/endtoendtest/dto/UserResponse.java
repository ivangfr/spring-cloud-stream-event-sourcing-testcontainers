package com.ivanfranchin.endtoendtest.dto;

public record UserResponse(Long id, String email, String fullName, Boolean active) {
}
