package com.mycompany.userservice.controller;

import com.mycompany.userservice.bus.UserStream;
import com.mycompany.userservice.dto.CreateUserDto;
import com.mycompany.userservice.dto.UpdateUserDto;
import com.mycompany.userservice.dto.UserDto;
import com.mycompany.userservice.mapper.UserMapper;
import com.mycompany.userservice.model.User;
import com.mycompany.userservice.service.UserService;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
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

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final UserStream userStream;
    private final UserMapper userMapper;

    public UserController(UserService userService, UserStream userStream, UserMapper userMapper) {
        this.userService = userService;
        this.userStream = userStream;
        this.userMapper = userMapper;
    }

    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 500, message = "Internal Server Error")
    })
    @GetMapping
    public List<UserDto> getAllUsers() {
        return userService.getAllUsers()
                .stream()
                .map(userMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 404, message = "Not Found"),
            @ApiResponse(code = 500, message = "Internal Server Error")
    })
    @GetMapping("/{id}")
    public UserDto getUserById(@PathVariable Long id) {
        User user = userService.validateAndGetUserById(id);
        return userMapper.toUserDto(user);
    }

    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Created"),
            @ApiResponse(code = 400, message = "Bad Request"),
            @ApiResponse(code = 409, message = "Conflict"),
            @ApiResponse(code = 500, message = "Internal Server Error")
    })
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

    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 400, message = "Bad Request"),
            @ApiResponse(code = 404, message = "Not Found"),
            @ApiResponse(code = 409, message = "Conflict"),
            @ApiResponse(code = 500, message = "Internal Server Error")
    })
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

    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 404, message = "Not Found"),
            @ApiResponse(code = 500, message = "Internal Server Error")
    })
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
