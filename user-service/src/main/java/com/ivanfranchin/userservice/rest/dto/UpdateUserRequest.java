package com.ivanfranchin.userservice.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.Email;

@Data
public class UpdateUserRequest {

    @Schema(example = "ivan.franchin.2@test.com")
    @Email
    private String email;

    @Schema(example = "Ivan Franchin 2")
    private String fullName;

    @Schema(example = "false")
    private Boolean active;
}
