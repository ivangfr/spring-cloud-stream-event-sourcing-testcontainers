package com.mycompany.userservice;

import com.mycompany.userservice.dto.CreateUserDto;
import com.mycompany.userservice.dto.UpdateUserDto;
import com.mycompany.userservice.dto.UserDto;
import com.mycompany.userservice.model.User;
import com.mycompany.userservice.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
public class RandomPortTestRestTemplateTests {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestRestTemplate testRestTemplate;

    /*
     * GET /api/users
     * ============== */

    @Test
    void given_noUsers_when_getAllUsers_then_returnEmptyArray() {
        ResponseEntity<UserDto[]> responseEntity = testRestTemplate.getForEntity("/api/users", UserDto[].class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).hasSize(0);
    }

    @Test
    void given_oneUser_when_getAllUsers_then_returnArrayWithUser() {
        User user = getDefaultUser();
        user = userRepository.save(user);

        ResponseEntity<UserDto[]> responseEntity = testRestTemplate.getForEntity("/api/users", UserDto[].class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody()).hasSize(1);
        assertThat(responseEntity.getBody()[0].getId()).isEqualTo(user.getId());
        assertThat(responseEntity.getBody()[0].getEmail()).isEqualTo(user.getEmail());
        assertThat(responseEntity.getBody()[0].getActive()).isEqualTo(user.getActive());
    }

    /*
     * GET /api/users/{id}
     * =================== */

    @Test
    void given_noUsers_when_getUserById_then_returnNotFound() {
        long id = 1L;
        ResponseEntity<MessageError> responseEntity = testRestTemplate.getForEntity("/api/users/" + id, MessageError.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().getTimestamp()).isNotEmpty();
        assertThat(responseEntity.getBody().getStatus()).isEqualTo(404);
        assertThat(responseEntity.getBody().getError()).isEqualTo("Not Found");
        assertThat(responseEntity.getBody().getMessage()).isEqualTo("User with id '" + id + "' doesn't exist.");
        assertThat(responseEntity.getBody().getPath()).isEqualTo("/api/users/" + id);
        assertThat(responseEntity.getBody().getErrors()).isNull();
    }

    @Test
    void given_oneUser_when_getUserById_then_returnUserJson() {
        User user = getDefaultUser();
        user = userRepository.save(user);

        ResponseEntity<UserDto> responseEntity = testRestTemplate.getForEntity("/api/users/" + user.getId(), UserDto.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().getId()).isEqualTo(user.getId());
        assertThat(responseEntity.getBody().getEmail()).isEqualTo(user.getEmail());
        assertThat(responseEntity.getBody().getFullName()).isEqualTo(user.getFullName());
        assertThat(responseEntity.getBody().getActive()).isEqualTo(user.getActive());
    }

    /*
     * POST /api/users
     * =============== */

    @Test
    void given_noUsers_when_createUser_then_returnUserJson() {
        CreateUserDto createUserDto = getDefaultCreateUserDto();
        ResponseEntity<UserDto> responseEntity = testRestTemplate.postForEntity("/api/users", createUserDto, UserDto.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().getId()).isGreaterThan(0);
        assertThat(responseEntity.getBody().getEmail()).isEqualTo(createUserDto.getEmail());
        assertThat(responseEntity.getBody().getFullName()).isEqualTo(createUserDto.getFullName());
        assertThat(responseEntity.getBody().getActive()).isEqualTo(createUserDto.getActive());

        Optional<User> userFound = userRepository.findById(responseEntity.getBody().getId());
        assertThat(userFound.isPresent()).isTrue();
    }

    // TODO add more test cases

    /*
     * PUT /api/users/{id}
     * =================== */

    @Test
    void given_oneUser_when_updateUser_then_returnUserJson() {
        User user = getDefaultUser();
        user = userRepository.save(user);

        UpdateUserDto updateUserDto = new UpdateUserDto();
        updateUserDto.setActive(false);

        HttpEntity<UpdateUserDto> requestUpdate = new HttpEntity<>(updateUserDto);
        ResponseEntity<UserDto> responseEntity = testRestTemplate.exchange("/api/users/" + user.getId(), HttpMethod.PUT, requestUpdate, UserDto.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().getId()).isEqualTo(user.getId());
        assertThat(responseEntity.getBody().getEmail()).isEqualTo(user.getEmail());
        assertThat(responseEntity.getBody().getFullName()).isEqualTo(user.getFullName());
        assertThat(responseEntity.getBody().getActive()).isEqualTo(updateUserDto.getActive());
    }

    // TODO add more test cases

    /*
     * DELETE /api/users/{id}
     * ====================== */

    @Test
    void given_oneUser_when_deleteUser_then_returnUserJson() {
        User user = getDefaultUser();
        user = userRepository.save(user);

        ResponseEntity<UserDto> responseEntity = testRestTemplate.exchange("/api/users/" + user.getId(), HttpMethod.DELETE, null, UserDto.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().getId()).isEqualTo(user.getId());
        assertThat(responseEntity.getBody().getEmail()).isEqualTo(user.getEmail());
        assertThat(responseEntity.getBody().getFullName()).isEqualTo(user.getFullName());
        assertThat(responseEntity.getBody().getActive()).isEqualTo(user.getActive());

        Optional<User> userNotFound = userRepository.findById(user.getId());
        assertThat(userNotFound.isPresent()).isFalse();
    }

    // TODO add more test cases

    /*
     * Util Methods
     * ============ */

    private User getDefaultUser() {
        User user = new User();
        user.setEmail("ivan.franchin@test.com");
        user.setFullName("Ivan Franchin");
        user.setActive(true);
        return user;
    }

    private CreateUserDto getDefaultCreateUserDto() {
        return new CreateUserDto("ivan.franchin@test.com", "Ivan Franchin", true);
    }
}
