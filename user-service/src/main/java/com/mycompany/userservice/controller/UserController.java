package com.mycompany.userservice.controller;

import com.mycompany.userservice.bus.UserStream;
import com.mycompany.userservice.dto.CreateUserDto;
import com.mycompany.userservice.dto.UpdateUserDto;
import com.mycompany.userservice.dto.UserDto;
import com.mycompany.userservice.exception.UserEmailDuplicatedException;
import com.mycompany.userservice.exception.UserNotFoundException;
import com.mycompany.userservice.model.User;
import com.mycompany.userservice.service.UserService;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    private final ModelMapper modelMapper;

    private UserStream userStream;

    public UserController(UserService userService, ModelMapper modelMapper, UserStream userStream) {
        this.userService = userService;
        this.modelMapper = modelMapper;
        this.userStream = userStream;
    }

    @GetMapping
    public ResponseEntity<List<UserDto>> getAllUsers() {
        List<UserDto> userDtos = new ArrayList<>();
        for (User user : userService.getAllUsers()) {
            userDtos.add(modelMapper.map(user, UserDto.class));
        }

        return ResponseEntity.ok(userDtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) throws UserNotFoundException {
        User user = userService.validateAndGetUserById(id);

        return ResponseEntity.ok(modelMapper.map(user, UserDto.class));
    }

    @PostMapping
    public ResponseEntity<UserDto> createUser(@Valid @RequestBody CreateUserDto createUserDto) throws UserEmailDuplicatedException {
        userService.validateUserExistsByEmail(createUserDto.getEmail());

        User user = modelMapper.map(createUserDto, User.class);
        user = userService.saveUser(user);

        userStream.userCreated(user.getId(), createUserDto);

        return new ResponseEntity<>(modelMapper.map(user, UserDto.class), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserDto> updateUser(@PathVariable Long id, @Valid @RequestBody UpdateUserDto updateUserDto) throws UserNotFoundException, UserEmailDuplicatedException {
        User user = userService.validateAndGetUserById(id);

        String userEmail = user.getEmail();
        String updateUserDtoEmail = updateUserDto.getEmail();
        if (!StringUtils.isEmpty(updateUserDtoEmail) && !updateUserDtoEmail.equals(userEmail)) {
            userService.validateUserExistsByEmail(updateUserDtoEmail);
        }

        modelMapper.map(updateUserDto, user);
        user = userService.saveUser(user);

        userStream.userUpdated(user.getId(), updateUserDto);

        return ResponseEntity.ok(modelMapper.map(user, UserDto.class));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<UserDto> deleteUser(@PathVariable Long id) throws UserNotFoundException {
        User user = userService.validateAndGetUserById(id);

        userService.deleteUser(user);

        userStream.userDeleted(user.getId());

        return ResponseEntity.ok(modelMapper.map(user, UserDto.class));
    }

    @ExceptionHandler({UserNotFoundException.class})
    public void handleNotFoundException(Exception e, HttpServletResponse response) throws IOException {
        response.sendError(HttpStatus.NOT_FOUND.value(), e.getMessage());
    }

    @ExceptionHandler(UserEmailDuplicatedException.class)
    public void handleBadRequestException(Exception e, HttpServletResponse response) throws IOException {
        response.sendError(HttpStatus.BAD_REQUEST.value(), e.getMessage());
    }

}
