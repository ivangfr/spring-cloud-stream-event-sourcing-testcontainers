package com.mycompany.userservice.rest.dto;

import lombok.Data;

@Data
public class UserDto {

    private Long id;
    private String email;
    private String fullName;
    private Boolean active;

}
