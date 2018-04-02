package com.mycompany.userservice.dto;

import lombok.Data;

import java.util.Date;

@Data
public class UserDto {

    private Long id;

    private String email;

    private String fullName;

    private Boolean active;

}
