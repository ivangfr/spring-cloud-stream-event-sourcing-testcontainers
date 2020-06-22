package com.mycompany.userservice;

import com.mycompany.userservice.dto.CreateUserDto;
import com.mycompany.userservice.dto.UpdateUserDto;
import com.mycompany.userservice.dto.UserDto;
import com.mycompany.userservice.model.User;
import com.mycompany.userservice.repository.UserRepository;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
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

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@Slf4j
@ActiveProfiles("test")
@ExtendWith(ContainersExtension.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
public class RandomPortTestRestTemplateTests {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestRestTemplate testRestTemplate;

    /*
     * GET /api/users
     * ============== */

    @Test
    void givenNoUsersWhenGetAllUsersThenReturnEmptyArray() {
        ResponseEntity<UserDto[]> responseEntity = testRestTemplate.getForEntity("/api/users", UserDto[].class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody()).hasSize(0);
    }

    @Test
    void givenOneUserWhenGetAllUsersThenReturnArrayWithUser() {
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
    void givenNoUsersWhenGetUserByIdThenReturnNotFound() {
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
    void givenOneUserWhenGetUserByIdThenReturnUserJson() {
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
    void givenNoUsersWhenCreateUserThenReturnUserJson() {
        CreateUserDto createUserDto = getDefaultCreateUserDto();
        ResponseEntity<UserDto> responseEntity = testRestTemplate.postForEntity("/api/users", createUserDto, UserDto.class);

        log.info("{}", responseEntity);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().getId()).isGreaterThan(0);
        assertThat(responseEntity.getBody().getEmail()).isEqualTo(createUserDto.getEmail());
        assertThat(responseEntity.getBody().getFullName()).isEqualTo(createUserDto.getFullName());
        assertThat(responseEntity.getBody().getActive()).isEqualTo(createUserDto.getActive());

        final Long userId = responseEntity.getBody().getId();
        Optional<User> userFound = userRepository.findById(userId);
        assertThat(userFound.isPresent()).isTrue();

        await().atMost(Duration.ofSeconds(10)).pollInterval(Duration.ofSeconds(1)).untilAsserted(() -> {
            log.info("Waiting for event-service to receive the message and process ...");
            ResponseEntity<EventServiceUserEventDto[]> eventServiceResponseEntity = testRestTemplate.getForEntity("http://localhost:9081/api/events/users/" + userId, EventServiceUserEventDto[].class);
            assertThat(eventServiceResponseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(eventServiceResponseEntity.getBody()).isNotNull();
            assertThat(Arrays.stream(eventServiceResponseEntity.getBody()).anyMatch(userEventDto -> userEventDto.getType().equals("CREATED"))).isTrue();
        });
    }

    // TODO add more test cases

    /*
     * PUT /api/users/{id}
     * =================== */

    @Test
    void givenOneUserWhenUpdateUserThenReturnUserJson() {
        User user = getDefaultUser();
        user = userRepository.save(user);
        final Long userId = user.getId();

        UpdateUserDto updateUserDto = new UpdateUserDto();
        updateUserDto.setActive(false);

        HttpEntity<UpdateUserDto> requestUpdate = new HttpEntity<>(updateUserDto);
        ResponseEntity<UserDto> responseEntity = testRestTemplate.exchange("/api/users/" + userId, HttpMethod.PUT, requestUpdate, UserDto.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().getId()).isEqualTo(userId);
        assertThat(responseEntity.getBody().getEmail()).isEqualTo(user.getEmail());
        assertThat(responseEntity.getBody().getFullName()).isEqualTo(user.getFullName());
        assertThat(responseEntity.getBody().getActive()).isEqualTo(updateUserDto.getActive());

        await().atMost(Duration.ofSeconds(10)).pollInterval(Duration.ofSeconds(1)).untilAsserted(() -> {
            log.info("Waiting for event-service to receive the message and process ...");
            ResponseEntity<EventServiceUserEventDto[]> eventServiceResponseEntity = testRestTemplate.getForEntity("http://localhost:9081/api/events/users/" + userId, EventServiceUserEventDto[].class);
            assertThat(eventServiceResponseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(eventServiceResponseEntity.getBody()).isNotNull();
            assertThat(Arrays.stream(eventServiceResponseEntity.getBody()).anyMatch(userEventDto -> userEventDto.getType().equals("UPDATED"))).isTrue();
        });
    }

    // TODO add more test cases

    /*
     * DELETE /api/users/{id}
     * ====================== */

    @Test
    void givenOneUserWhenDeleteUserThenReturnUserJson() {
        User user = getDefaultUser();
        user = userRepository.save(user);
        final Long userId = user.getId();

        ResponseEntity<UserDto> responseEntity = testRestTemplate.exchange("/api/users/" + userId, HttpMethod.DELETE, null, UserDto.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().getId()).isEqualTo(userId);
        assertThat(responseEntity.getBody().getEmail()).isEqualTo(user.getEmail());
        assertThat(responseEntity.getBody().getFullName()).isEqualTo(user.getFullName());
        assertThat(responseEntity.getBody().getActive()).isEqualTo(user.getActive());

        Optional<User> userNotFound = userRepository.findById(userId);
        assertThat(userNotFound.isPresent()).isFalse();

        await().atMost(Duration.ofSeconds(10)).pollInterval(Duration.ofSeconds(1)).untilAsserted(() -> {
            log.info("Waiting for event-service to receive the message and process ...");
            ResponseEntity<EventServiceUserEventDto[]> eventServiceResponseEntity = testRestTemplate.getForEntity("http://localhost:9081/api/events/users/" + userId, EventServiceUserEventDto[].class);
            assertThat(eventServiceResponseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(eventServiceResponseEntity.getBody()).isNotNull();
            assertThat(Arrays.stream(eventServiceResponseEntity.getBody()).anyMatch(userEventDto -> userEventDto.getType().equals("DELETED"))).isTrue();
        });
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

    @Data
    private static class EventServiceUserEventDto {
        private Long userId;
        private String datetime;
        private String type;
        private String data;
    }

    @Data
    private static class MessageError {
        private String timestamp;
        private int status;
        private String error;
        private String message;
        private String path;
        private List<ErrorDetail> errors;

        @Data
        static class ErrorDetail {
            private List<String> codes;
            private String defaultMessage;
            private String objectName;
            private String field;
            private String rejectedValue;
            private boolean bindingFailure;
            private String code;
        }
    }

}
