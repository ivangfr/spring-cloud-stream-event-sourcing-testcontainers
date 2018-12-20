package com.mycompany.userservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class UserEmailDuplicatedException extends Exception {

    public UserEmailDuplicatedException(String message) {
        super(message);
    }
}
