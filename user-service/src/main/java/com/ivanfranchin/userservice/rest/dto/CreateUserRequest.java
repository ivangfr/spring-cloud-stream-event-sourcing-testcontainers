package com.ivanfranchin.userservice.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateUserRequest(
        @Schema(example = "ivan.franchin@test.com") @NotBlank @Email String email,
        @Schema(example = "Ivan Franchin") @NotBlank String fullName,
        @Schema(example = "true") @NotNull Boolean active) {
}
