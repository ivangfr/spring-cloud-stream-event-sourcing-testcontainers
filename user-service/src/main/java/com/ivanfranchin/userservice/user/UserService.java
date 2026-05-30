package com.ivanfranchin.userservice.user;

import com.ivanfranchin.userservice.user.exception.UserEmailDuplicatedException;
import com.ivanfranchin.userservice.user.exception.UserNotFoundException;
import com.ivanfranchin.userservice.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;

    public List<User> getUsers() {
        return userRepository.findAll();
    }

    public User saveUser(User user) {
        return userRepository.save(user);
    }

    public void deleteUser(User user) {
        userRepository.delete(user);
    }

    public User validateAndGetUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(String.format("User with id '%d' doesn't exist.", id)));
    }

    public void validateUserExistsByEmail(String email) {
        userRepository.findUserByEmail(email).ifPresent(user -> {
            throw new UserEmailDuplicatedException(String.format("User with email '%s' already exist.", email));
        });
    }
}
