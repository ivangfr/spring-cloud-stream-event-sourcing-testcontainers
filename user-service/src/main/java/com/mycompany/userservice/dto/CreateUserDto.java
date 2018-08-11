package com.mycompany.userservice.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserDto {

    @ApiModelProperty(value = "User's full name", example = "Peter Blair")
    @NotNull
    private String fullName;

    @ApiModelProperty(position = 1, value = "User's email", example = "peter.blair@test.com")
    @NotNull
    @Email
    private String email;

    @ApiModelProperty(position = 2, value = "User's status", example = "true")
    @NotNull
    private Boolean active;

}
