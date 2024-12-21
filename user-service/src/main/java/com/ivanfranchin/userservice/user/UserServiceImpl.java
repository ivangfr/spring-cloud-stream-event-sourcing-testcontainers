package com.ivanfranchin.userservice.user;

import com.ivanfranchin.userservice.user.exception.UserNotFoundException;
import com.ivanfranchin.userservice.user.model.User;
import com.ivanfranchin.userservice.user.exception.UserEmailDuplicatedException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public List<User> getUsers() {
        return userRepository.findAll();
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
    public User validateAndGetUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(String.format("User with id '%d' doesn't exist.", id)));
    }

    @Override
    public void validateUserExistsByEmail(String email) {
        userRepository.findUserByEmail(email).ifPresent(user -> {
            throw new UserEmailDuplicatedException(String.format("User with email '%s' already exist.", email));
        });
    }
}
