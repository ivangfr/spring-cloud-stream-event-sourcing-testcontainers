package com.mycompany.userservice.exception;

public class UserEmailDuplicatedException extends Exception {

    public UserEmailDuplicatedException(String message) {
        super(message);
    }
}
