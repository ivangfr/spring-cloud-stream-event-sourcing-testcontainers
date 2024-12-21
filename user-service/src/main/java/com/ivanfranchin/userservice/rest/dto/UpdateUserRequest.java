package com.ivanfranchin.userservice.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;

public record UpdateUserRequest(
        @Schema(example = "ivan.franchin.2@test.com") @Email String email,
        @Schema(example = "Ivan Franchin 2") String fullName,
        @Schema(example = "false") Boolean active) {
}
