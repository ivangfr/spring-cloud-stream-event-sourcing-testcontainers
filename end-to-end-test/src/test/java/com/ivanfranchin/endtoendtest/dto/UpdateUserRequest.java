package com.ivanfranchin.endtoendtest.dto;

public record UpdateUserRequest(String email, String fullName, Boolean active) {
}
