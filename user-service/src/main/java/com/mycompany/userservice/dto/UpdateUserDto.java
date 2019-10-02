package com.mycompany.userservice.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Email;

@Data
public class UpdateUserDto {

    @ApiModelProperty(value = "User's email", example = "ivan.franchin.jr@test2.com")
    @Email
    private String email;

    @ApiModelProperty(position = 1, value = "User's full name", example = "Ivan Franchin Jr")
    private String fullName;

    @ApiModelProperty(position = 2, value = "User's status", example = "false")
    private Boolean active;

}
