package com.mycompany.userservice.service;

import com.mycompany.userservice.exception.UserEmailDuplicatedException;
import com.mycompany.userservice.exception.UserNotFoundException;
import com.mycompany.userservice.model.User;
import com.mycompany.userservice.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public User getUserById(Long id) {
        return userRepository.findUserById(id);
    }

    @Override
    public User saveUser(User user) {
        return userRepository.save(user);
    }

    @Override
    public void deleteUser(User user) {
        userRepository.delete(user);
    }

    @Override
    public User validateAndGetUserById(Long id) throws UserNotFoundException {
        User user = getUserById(id);
        if (user == null) {
            String message = String.format("User with id '%d' doesn't exist.", id);
            throw new UserNotFoundException(message);
        }
        return user;
    }

    @Override
    public void validateUserExistsByEmail(String email) throws UserEmailDuplicatedException {
        User user = userRepository.findUserByEmail(email);
        if (user != null) {
            String message = String.format("User with email '%s' already exist.", email);
            throw new UserEmailDuplicatedException(message);
        }
    }
}
