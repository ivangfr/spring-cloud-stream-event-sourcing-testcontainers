package com.mycompany.userservice.rest.dto;

import lombok.Value;

@Value
public class UserDto {

    Long id;
    String email;
    String fullName;
    Boolean active;
}
