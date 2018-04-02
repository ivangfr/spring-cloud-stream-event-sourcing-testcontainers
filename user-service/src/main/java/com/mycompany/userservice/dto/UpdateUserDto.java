package com.mycompany.userservice.dto;

import lombok.Data;

import javax.validation.constraints.Email;

@Data
public class UpdateUserDto {

    @Email
    private String email;

    private String fullName;

    private Boolean active;

}
