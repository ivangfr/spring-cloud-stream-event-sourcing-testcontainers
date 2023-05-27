package com.ivanfranchin.endtoendtest.dto;

public record CreateUserRequest(String email, String fullName, Boolean active) {
}
