package com.ivanfranchin.userservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class UserEmailDuplicatedException extends RuntimeException {

    public UserEmailDuplicatedException(String message) {
        super(message);
    }
}
