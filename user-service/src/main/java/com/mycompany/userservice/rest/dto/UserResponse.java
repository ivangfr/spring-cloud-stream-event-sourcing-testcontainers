package com.mycompany.userservice.rest.dto;

import lombok.Value;

@Value
public class UserResponse {

    Long id;
    String email;
    String fullName;
    Boolean active;
}
