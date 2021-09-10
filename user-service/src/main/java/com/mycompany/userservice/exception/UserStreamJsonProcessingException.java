package com.mycompany.userservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class UserStreamJsonProcessingException extends RuntimeException {

    public UserStreamJsonProcessingException(Throwable cause) {
        super(cause);
    }
}
