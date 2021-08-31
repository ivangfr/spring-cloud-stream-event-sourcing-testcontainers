package com.mycompany.userservice.mapper;

import com.mycompany.userservice.model.User;
import com.mycompany.userservice.rest.dto.CreateUserDto;
import com.mycompany.userservice.rest.dto.UpdateUserDto;
import com.mycompany.userservice.rest.dto.UserDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@Import(UserMapperImpl.class)
class UserMapperTest {

    @Autowired
    private UserMapper userMapper;

    @Test
    void testToUser() {
        CreateUserDto createUserDto = new CreateUserDto("email@test", "fullName", true);

        User user = userMapper.toUser(createUserDto);

        assertThat(user.getId()).isNull();
        assertThat(user.getEmail()).isEqualTo(createUserDto.getEmail());
        assertThat(user.getFullName()).isEqualTo(createUserDto.getFullName());
        assertThat(user.getActive()).isEqualTo(createUserDto.getActive());
        assertThat(user.getCreatedAt()).isNull();
        assertThat(user.getUpdatedAt()).isNull();
    }

    @Test
    void testToUserDto() {
        User user = new User("email@test", "fullName", true);

        UserDto userDto = userMapper.toUserDto(user);

        assertThat(userDto.getId()).isNull();
        assertThat(userDto.getEmail()).isEqualTo(user.getEmail());
        assertThat(userDto.getFullName()).isEqualTo(user.getFullName());
        assertThat(userDto.getActive()).isEqualTo(user.getActive());
        assertThat(user.getCreatedAt()).isNull();
        assertThat(user.getUpdatedAt()).isNull();
    }

    @ParameterizedTest
    @MethodSource("provideUpdateUserFromDto")
    void testUpdateUserFromDto(String newEmail, String newFullName, Boolean newActive, User expectedUser) {
        User user = new User("email@test", "fullName", true);

        UpdateUserDto updateUserDto = new UpdateUserDto();
        updateUserDto.setEmail(newEmail);
        updateUserDto.setFullName(newFullName);
        updateUserDto.setActive(newActive);

        userMapper.updateUserFromDto(updateUserDto, user);
        assertThat(user).isEqualTo(expectedUser);
    }

    private static Stream<Arguments> provideUpdateUserFromDto() {
        return Stream.of(
                Arguments.of("email2@test.com", "fullName2", false, new User("email2@test.com", "fullName2", false)),
                Arguments.of("email2@test.com", null, null, new User("email2@test.com", "fullName", true)),
                Arguments.of(null, "fullName2", null, new User("email@test", "fullName2", true)),
                Arguments.of(null, null, false, new User("email@test", "fullName", false)),
                Arguments.of(null, null, null, new User("email@test", "fullName", true))
        );
    }
}