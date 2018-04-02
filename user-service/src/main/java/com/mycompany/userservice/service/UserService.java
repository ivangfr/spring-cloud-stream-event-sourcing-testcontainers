package com.mycompany.userservice.service;

import com.mycompany.userservice.exception.UserEmailDuplicatedException;
import com.mycompany.userservice.exception.UserNotFoundException;
import com.mycompany.userservice.model.User;

import java.util.List;

public interface UserService {

    List<User> getAllUsers();

    User getUserById(Long id);

    User saveUser(User user);

    void deleteUser(User user);

    User validateAndGetUserById(Long id) throws UserNotFoundException;

    void validateUserExistsByEmail(String email) throws UserEmailDuplicatedException;

}
