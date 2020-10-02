package com.mycompany.userservice.rest;

import com.mycompany.userservice.bus.UserStream;
import com.mycompany.userservice.mapper.UserMapper;
import com.mycompany.userservice.model.User;
import com.mycompany.userservice.rest.dto.CreateUserDto;
import com.mycompany.userservice.rest.dto.UpdateUserDto;
import com.mycompany.userservice.rest.dto.UserDto;
import com.mycompany.userservice.service.UserService;
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
    public List<UserDto> getAllUsers() {
        return userService.getAllUsers()
                .stream()
                .map(userMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public UserDto getUserById(@PathVariable Long id) {
        User user = userService.validateAndGetUserById(id);
        return userMapper.toUserDto(user);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public UserDto createUser(@Valid @RequestBody CreateUserDto createUserDto) {
        userService.validateUserExistsByEmail(createUserDto.getEmail());
        User user = userMapper.toUser(createUserDto);

        //-- Saving to MySQL and sending event to Kafka is not an atomic transaction!
        user = userService.saveUser(user);
        userStream.userCreated(user.getId(), createUserDto);
        //--

        return userMapper.toUserDto(user);
    }

    @PutMapping("/{id}")
    public UserDto updateUser(@PathVariable Long id, @Valid @RequestBody UpdateUserDto updateUserDto) {
        User user = userService.validateAndGetUserById(id);

        String userEmail = user.getEmail();
        String updateUserDtoEmail = updateUserDto.getEmail();
        if (!StringUtils.isEmpty(updateUserDtoEmail) && !updateUserDtoEmail.equals(userEmail)) {
            userService.validateUserExistsByEmail(updateUserDtoEmail);
        }

        userMapper.updateUserFromDto(updateUserDto, user);

        //-- Saving to MySQL and sending event to Kafka is not an atomic transaction!
        user = userService.saveUser(user);
        userStream.userUpdated(user.getId(), updateUserDto);
        //--

        return userMapper.toUserDto(user);
    }

    @DeleteMapping("/{id}")
    public UserDto deleteUser(@PathVariable Long id) {
        User user = userService.validateAndGetUserById(id);

        //-- Deleting from MySQL and sending event to Kafka is not an atomic transaction!
        userService.deleteUser(user);
        userStream.userDeleted(user.getId());
        //--

        return userMapper.toUserDto(user);
    }

}
