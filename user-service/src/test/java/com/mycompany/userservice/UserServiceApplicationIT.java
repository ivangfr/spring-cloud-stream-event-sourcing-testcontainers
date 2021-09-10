package com.mycompany.userservice;

import com.mycompany.userservice.model.User;
import com.mycompany.userservice.repository.UserRepository;
import com.mycompany.userservice.rest.dto.CreateUserRequest;
import com.mycompany.userservice.rest.dto.UpdateUserRequest;
import com.mycompany.userservice.rest.dto.UserResponse;
import lombok.Value;
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
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@Slf4j
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class UserServiceApplicationIT extends AbstractTestcontainers {

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private UserRepository userRepository;

    /*
     * GET /api/users
     * ============== */

    @Test
    void testGetUsersWhenThereIsNone() {
        ResponseEntity<UserResponse[]> responseEntity = testRestTemplate.getForEntity(API_USERS_URL, UserResponse[].class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody()).isEmpty();
    }

    @Test
    void testGetUsersWhenThereIsOne() {
        User user = userRepository.save(getDefaultUser());

        ResponseEntity<UserResponse[]> responseEntity = testRestTemplate.getForEntity(API_USERS_URL, UserResponse[].class);

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
    void testGetUserWhenNonExistent() {
        Long id = 1L;
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
    void testGetUserWhenExistent() {
        User user = userRepository.save(getDefaultUser());

        String url = String.format(API_USERS_USER_ID_URL, user.getId());
        ResponseEntity<UserResponse> responseEntity = testRestTemplate.getForEntity(url, UserResponse.class);

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
    void testCreateUser() {
        CreateUserRequest createUserRequest = getDefaultCreateUserRequest();
        ResponseEntity<UserResponse> responseEntity = testRestTemplate.postForEntity(API_USERS_URL, createUserRequest, UserResponse.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().getId()).isPositive();
        assertThat(responseEntity.getBody().getEmail()).isEqualTo(createUserRequest.getEmail());
        assertThat(responseEntity.getBody().getFullName()).isEqualTo(createUserRequest.getFullName());
        assertThat(responseEntity.getBody().getActive()).isEqualTo(createUserRequest.getActive());

        Long userId = responseEntity.getBody().getId();
        Optional<User> userFound = userRepository.findById(userId);
        assertThat(userFound).isPresent();

        await().atMost(AT_MOST_DURATION).pollInterval(POLL_INTERVAL_DURATION).untilAsserted(() -> {
            log.info("Waiting for event-service to receive the message and process ...");
            String eventServiceUrl = String.format("%s/events?userId=%s", EVENT_SERVICE_API_URL, userId);
            ResponseEntity<EventServiceUserEventResponse[]> eventServiceResponseEntity =
                    testRestTemplate.getForEntity(eventServiceUrl, EventServiceUserEventResponse[].class);
            assertThat(eventServiceResponseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(eventServiceResponseEntity.getBody()).isNotNull();
            assertThat(Arrays.stream(eventServiceResponseEntity.getBody())
                    .anyMatch(userEventResponse -> userEventResponse.getType().equals("CREATED"))).isTrue();
        });
    }

    // TODO add more test cases

    /*
     * PUT /api/users/{id}
     * =================== */

    @Test
    void testUpdateUser() {
        User user = userRepository.save(getDefaultUser());
        Long userId = user.getId();

        UpdateUserRequest updateUserRequest = new UpdateUserRequest();
        updateUserRequest.setActive(false);

        HttpEntity<UpdateUserRequest> requestUpdate = new HttpEntity<>(updateUserRequest);
        String url = String.format(API_USERS_USER_ID_URL, userId);
        ResponseEntity<UserResponse> responseEntity = testRestTemplate.exchange(url, HttpMethod.PUT, requestUpdate, UserResponse.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().getId()).isEqualTo(userId);
        assertThat(responseEntity.getBody().getEmail()).isEqualTo(user.getEmail());
        assertThat(responseEntity.getBody().getFullName()).isEqualTo(user.getFullName());
        assertThat(responseEntity.getBody().getActive()).isEqualTo(updateUserRequest.getActive());

        await().atMost(AT_MOST_DURATION).pollInterval(POLL_INTERVAL_DURATION).untilAsserted(() -> {
            log.info("Waiting for event-service to receive the message and process ...");
            String eventServiceUrl = String.format("%s/events?userId=%s", EVENT_SERVICE_API_URL, userId);
            ResponseEntity<EventServiceUserEventResponse[]> eventServiceResponseEntity =
                    testRestTemplate.getForEntity(eventServiceUrl, EventServiceUserEventResponse[].class);
            assertThat(eventServiceResponseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(eventServiceResponseEntity.getBody()).isNotNull();
            assertThat(Arrays.stream(eventServiceResponseEntity.getBody())
                    .anyMatch(userEventResponse -> userEventResponse.getType().equals("UPDATED"))).isTrue();
        });
    }

    // TODO add more test cases

    /*
     * DELETE /api/users/{id}
     * ====================== */

    @Test
    void testDeleteUser() {
        User user = userRepository.save(getDefaultUser());
        Long userId = user.getId();

        String url = String.format(API_USERS_USER_ID_URL, userId);
        ResponseEntity<UserResponse> responseEntity = testRestTemplate.exchange(url, HttpMethod.DELETE, null, UserResponse.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().getId()).isEqualTo(userId);
        assertThat(responseEntity.getBody().getEmail()).isEqualTo(user.getEmail());
        assertThat(responseEntity.getBody().getFullName()).isEqualTo(user.getFullName());
        assertThat(responseEntity.getBody().getActive()).isEqualTo(user.getActive());

        Optional<User> userNotFound = userRepository.findById(userId);
        assertThat(userNotFound).isNotPresent();

        await().atMost(AT_MOST_DURATION).pollInterval(POLL_INTERVAL_DURATION).untilAsserted(() -> {
            log.info("Waiting for event-service to receive the message and process ...");
            String eventServiceUrl = String.format("%s/events?userId=%s", EVENT_SERVICE_API_URL, userId);
            ResponseEntity<EventServiceUserEventResponse[]> eventServiceResponseEntity =
                    testRestTemplate.getForEntity(eventServiceUrl, EventServiceUserEventResponse[].class);
            assertThat(eventServiceResponseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(eventServiceResponseEntity.getBody()).isNotNull();
            assertThat(Arrays.stream(eventServiceResponseEntity.getBody())
                    .anyMatch(userEventResponse -> userEventResponse.getType().equals("DELETED"))).isTrue();
        });
    }

    // TODO add more test cases

    /*
     * Util Methods
     * ============ */

    private User getDefaultUser() {
        return new User("email@test", "fullName", true);
    }

    private CreateUserRequest getDefaultCreateUserRequest() {
        return new CreateUserRequest("email@test", "fullName", true);
    }

    @Value
    private static class EventServiceUserEventResponse {
        Long userId;
        String datetime;
        String type;
        String data;
    }

    @Value
    private static class MessageError {
        String timestamp;
        int status;
        String error;
        String message;
        String path;
        List<ErrorDetail> errors;

        @Value
        static class ErrorDetail {
            List<String> codes;
            String defaultMessage;
            String objectName;
            String field;
            String rejectedValue;
            boolean bindingFailure;
            String code;
        }
    }

    private static final String API_USERS_URL = "/api/users";
    private static final String API_USERS_USER_ID_URL = "/api/users/%s";

    public static final Duration AT_MOST_DURATION = Duration.ofSeconds(10);
    public static final Duration POLL_INTERVAL_DURATION = Duration.ofSeconds(1);
}
