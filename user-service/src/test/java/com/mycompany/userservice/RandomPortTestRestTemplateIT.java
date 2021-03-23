package com.mycompany.userservice;

import com.mycompany.userservice.model.User;
import com.mycompany.userservice.repository.UserRepository;
import com.mycompany.userservice.rest.dto.CreateUserDto;
import com.mycompany.userservice.rest.dto.UpdateUserDto;
import com.mycompany.userservice.rest.dto.UserDto;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
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

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@Slf4j
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
class RandomPortTestRestTemplateIT extends AbstractTestcontainers {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestRestTemplate testRestTemplate;

    /*
     * GET /api/users
     * ============== */

    @Test
    void givenNoUsersWhenGetAllUsersThenReturnEmptyArray() {
        ResponseEntity<UserDto[]> responseEntity = testRestTemplate.getForEntity(API_USERS_URL, UserDto[].class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody()).isEmpty();
    }

    @Test
    void givenOneUserWhenGetAllUsersThenReturnArrayWithUser() {
        User user = userRepository.save(getDefaultUser());

        ResponseEntity<UserDto[]> responseEntity = testRestTemplate.getForEntity(API_USERS_URL, UserDto[].class);

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
        String url = String.format(API_USERS_USER_ID_URL, id);
        ResponseEntity<MessageError> responseEntity = testRestTemplate.getForEntity(url, MessageError.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().getTimestamp()).isNotEmpty();
        assertThat(responseEntity.getBody().getStatus()).isEqualTo(404);
        assertThat(responseEntity.getBody().getError()).isEqualTo("Not Found");
        assertThat(responseEntity.getBody().getMessage()).isEqualTo("User with id '1' doesn't exist.");
        assertThat(responseEntity.getBody().getPath()).isEqualTo(url);
        assertThat(responseEntity.getBody().getErrors()).isNull();
    }

    @Test
    void givenOneUserWhenGetUserByIdThenReturnUserJson() {
        User user = userRepository.save(getDefaultUser());

        String url = String.format(API_USERS_USER_ID_URL, user.getId());
        ResponseEntity<UserDto> responseEntity = testRestTemplate.getForEntity(url, UserDto.class);

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
        ResponseEntity<UserDto> responseEntity = testRestTemplate.postForEntity(API_USERS_URL, createUserDto, UserDto.class);

        log.info("{}", responseEntity);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().getId()).isPositive();
        assertThat(responseEntity.getBody().getEmail()).isEqualTo(createUserDto.getEmail());
        assertThat(responseEntity.getBody().getFullName()).isEqualTo(createUserDto.getFullName());
        assertThat(responseEntity.getBody().getActive()).isEqualTo(createUserDto.getActive());

        final Long userId = responseEntity.getBody().getId();
        Optional<User> userFound = userRepository.findById(userId);
        assertThat(userFound).isPresent();

        await().atMost(Duration.ofSeconds(10)).pollInterval(Duration.ofSeconds(1)).untilAsserted(() -> {
            log.info("Waiting for event-service to receive the message and process ...");
            String eventServiceUrl = String.format("%s/events/users/%s", EVENT_SERVICE_API_URL, userId);
            ResponseEntity<EventServiceUserEventDto[]> eventServiceResponseEntity = testRestTemplate.getForEntity(eventServiceUrl, EventServiceUserEventDto[].class);
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
        User user = userRepository.save(getDefaultUser());
        final long userId = user.getId();

        UpdateUserDto updateUserDto = new UpdateUserDto();
        updateUserDto.setActive(false);

        HttpEntity<UpdateUserDto> requestUpdate = new HttpEntity<>(updateUserDto);
        String url = String.format(API_USERS_USER_ID_URL, userId);
        ResponseEntity<UserDto> responseEntity = testRestTemplate.exchange(url, HttpMethod.PUT, requestUpdate, UserDto.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().getId()).isEqualTo(userId);
        assertThat(responseEntity.getBody().getEmail()).isEqualTo(user.getEmail());
        assertThat(responseEntity.getBody().getFullName()).isEqualTo(user.getFullName());
        assertThat(responseEntity.getBody().getActive()).isEqualTo(updateUserDto.getActive());

        await().atMost(Duration.ofSeconds(10)).pollInterval(Duration.ofSeconds(1)).untilAsserted(() -> {
            log.info("Waiting for event-service to receive the message and process ...");
            String eventServiceUrl = String.format("%s/events/users/%s", EVENT_SERVICE_API_URL, userId);
            ResponseEntity<EventServiceUserEventDto[]> eventServiceResponseEntity = testRestTemplate.getForEntity(eventServiceUrl, EventServiceUserEventDto[].class);
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
        User user = userRepository.save(getDefaultUser());
        final long userId = user.getId();

        String url = String.format(API_USERS_USER_ID_URL, userId);
        ResponseEntity<UserDto> responseEntity = testRestTemplate.exchange(url, HttpMethod.DELETE, null, UserDto.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().getId()).isEqualTo(userId);
        assertThat(responseEntity.getBody().getEmail()).isEqualTo(user.getEmail());
        assertThat(responseEntity.getBody().getFullName()).isEqualTo(user.getFullName());
        assertThat(responseEntity.getBody().getActive()).isEqualTo(user.getActive());

        Optional<User> userNotFound = userRepository.findById(userId);
        assertThat(userNotFound).isNotPresent();

        await().atMost(Duration.ofSeconds(10)).pollInterval(Duration.ofSeconds(1)).untilAsserted(() -> {
            log.info("Waiting for event-service to receive the message and process ...");
            String eventServiceUrl = String.format("%s/events/users/%s", EVENT_SERVICE_API_URL, userId);
            ResponseEntity<EventServiceUserEventDto[]> eventServiceResponseEntity = testRestTemplate.getForEntity(eventServiceUrl, EventServiceUserEventDto[].class);
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

    private static final String API_USERS_URL = "/api/users";
    private static final String API_USERS_USER_ID_URL = "/api/users/%s";

}
