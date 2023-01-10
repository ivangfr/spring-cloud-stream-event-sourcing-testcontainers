package com.ivanfranchin.userservice.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserRequest {

    @Schema(example = "ivan.franchin@test.com")
    @NotBlank
    @Email
    private String email;

    @Schema(example = "Ivan Franchin")
    @NotBlank
    private String fullName;

    @Schema(example = "true")
    @NotNull
    private Boolean active;
}
