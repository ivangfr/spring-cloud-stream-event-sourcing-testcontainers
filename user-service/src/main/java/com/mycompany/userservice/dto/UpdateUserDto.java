package com.mycompany.userservice.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Email;

@Data
public class UpdateUserDto {

    @ApiModelProperty(value = "User's full name", example = "Peter Blair Jr")
    private String fullName;

    @ApiModelProperty(position = 1, value = "User's email", example = "peter.blair.jr@test.com")
    @Email
    private String email;

    @ApiModelProperty(position = 2, value = "User's status", example = "true")
    private Boolean active;

}
