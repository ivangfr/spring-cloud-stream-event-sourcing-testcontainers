package com.ivanfranchin.userservice.service;

import com.ivanfranchin.userservice.exception.UserNotFoundException;
import com.ivanfranchin.userservice.model.User;
import com.ivanfranchin.userservice.exception.UserEmailDuplicatedException;
import com.ivanfranchin.userservice.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;

@ExtendWith(SpringExtension.class)
@Import(UserServiceImpl.class)
class UserServiceImplTest {

    @Autowired
    private UserService userService;

    @MockBean
    private UserRepository userRepository;

    @Test
    void testGetUsersWhenThereIsNone() {
        given(userRepository.findAll()).willReturn(Collections.emptyList());

        List<User> users = userService.getUsers();

        assertThat(users).isNotNull();
        assertThat(users).isEmpty();
    }

    @Test
    void testGetUsersWhenThereIsOne() {
        User user = getDefaultUser();
        given(userRepository.findAll()).willReturn(Collections.singletonList(user));

        List<User> users = userService.getUsers();

        assertThat(users).isNotNull();
        assertThat(users.size()).isEqualTo(1);
        assertThat(users.get(0)).isEqualTo(user);
    }

    @Test
    void testSaveUser() {
        User user = getDefaultUser();
        given(userRepository.save(any(User.class))).willReturn(user);

        User userSaved = userService.saveUser(user);

        assertThat(userSaved).isNotNull();
        assertThat(userSaved).isEqualTo(user);
    }

    @Test
    void testDeleteUser() {
        User user = getDefaultUser();
        willDoNothing().given(userRepository).delete(any(User.class));

        userService.deleteUser(user);

        then(userRepository).should().delete(any(User.class));
    }

    @Test
    void testValidateAndGetUserByIdWhenFound() {
        User user = getDefaultUser();
        given(userRepository.findById(anyLong())).willReturn(Optional.of(user));

        User userFound = userService.validateAndGetUserById(1L);

        assertThat(userFound).isNotNull();
        assertThat(userFound).isEqualTo(user);
    }

    @Test
    void testValidateAndGetUserByIdWhenNotFound() {
        given(userRepository.findById(anyLong())).willReturn(Optional.empty());

        assertThatExceptionOfType(UserNotFoundException.class)
                .isThrownBy(() -> userService.validateAndGetUserById(1L))
                .withMessage("User with id '1' doesn't exist.");
    }

    @Test
    void testValidateUserExistsByEmailWhenExistent() {
        User user = getDefaultUser();
        given(userRepository.findUserByEmail(anyString())).willReturn(Optional.of(user));

        assertThatExceptionOfType(UserEmailDuplicatedException.class)
                .isThrownBy(() -> userService.validateUserExistsByEmail("email@test"))
                .withMessage("User with email 'email@test' already exist.");
    }

    @Test
    void testValidateUserExistsByEmailWhenNonExistent() {
        given(userRepository.findUserByEmail(anyString())).willReturn(Optional.empty());

        userService.validateUserExistsByEmail("email@test");

        then(userRepository).should().findUserByEmail(anyString());
    }

    private User getDefaultUser() {
        return new User("email@test", "fullName", true);
    }
}