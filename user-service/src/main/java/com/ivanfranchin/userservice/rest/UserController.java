package com.ivanfranchin.userservice.rest;

import com.ivanfranchin.userservice.kafka.UserStream;
import com.ivanfranchin.userservice.mapper.UserMapper;
import com.ivanfranchin.userservice.model.User;
import com.ivanfranchin.userservice.rest.dto.CreateUserRequest;
import com.ivanfranchin.userservice.rest.dto.UpdateUserRequest;
import com.ivanfranchin.userservice.rest.dto.UserResponse;
import com.ivanfranchin.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final UserStream userStream;
    private final UserMapper userMapper;

    @GetMapping
    public List<UserResponse> getUsers() {
        return userService.getUsers()
                .stream()
                .map(userMapper::toUserResponse)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public UserResponse getUserById(@PathVariable Long id) {
        User user = userService.validateAndGetUserById(id);
        return userMapper.toUserResponse(user);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public UserResponse createUser(@Valid @RequestBody CreateUserRequest createUserRequest) {
        userService.validateUserExistsByEmail(createUserRequest.getEmail());
        User user = userMapper.toUser(createUserRequest);

        //-- Saving to MySQL and sending event to Kafka is not an atomic transaction!
        user = userService.saveUser(user);
        userStream.userCreated(user.getId(), createUserRequest);
        //--

        return userMapper.toUserResponse(user);
    }

    @PutMapping("/{id}")
    public UserResponse updateUser(@PathVariable Long id, @Valid @RequestBody UpdateUserRequest updateUserRequest) {
        User user = userService.validateAndGetUserById(id);

        String userEmail = user.getEmail();
        String updateUserRequestEmail = updateUserRequest.getEmail();
        if (StringUtils.hasText(updateUserRequestEmail) && !updateUserRequestEmail.equals(userEmail)) {
            userService.validateUserExistsByEmail(updateUserRequestEmail);
        }

        userMapper.updateUserFromRequest(updateUserRequest, user);

        //-- Saving to MySQL and sending event to Kafka is not an atomic transaction!
        user = userService.saveUser(user);
        userStream.userUpdated(user.getId(), updateUserRequest);
        //--

        return userMapper.toUserResponse(user);
    }

    @DeleteMapping("/{id}")
    public UserResponse deleteUser(@PathVariable Long id) {
        User user = userService.validateAndGetUserById(id);

        //-- Deleting from MySQL and sending event to Kafka is not an atomic transaction!
        userService.deleteUser(user);
        userStream.userDeleted(user.getId());
        //--

        return userMapper.toUserResponse(user);
    }
}
