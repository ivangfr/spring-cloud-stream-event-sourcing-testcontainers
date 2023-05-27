package com.ivanfranchin.endtoendtest;

import com.ivanfranchin.endtoendtest.dto.CreateUserRequest;
import com.ivanfranchin.endtoendtest.dto.MessageError;
import com.ivanfranchin.endtoendtest.dto.UpdateUserRequest;
import com.ivanfranchin.endtoendtest.dto.UserEventResponse;
import com.ivanfranchin.endtoendtest.dto.UserResponse;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class EndToEndTestApplicationTests extends AbstractTestcontainers {

    @Autowired
    private TestRestTemplate testRestTemplate;

    /*
     * GET /api/users
     * ============== */

    @Test
    @Order(1)
    void testGetUsersWhenThereIsNone() {
        String userServiceUrl = String.format("%s/users", USER_SERVICE_API_URL);
        ResponseEntity<UserResponse[]> responseEntity = testRestTemplate.getForEntity(userServiceUrl, UserResponse[].class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody()).isEmpty();
    }

    /*
     * GET /api/users/{id}
     * =================== */

    @Test
    @Order(2)
    void testGetUserWhenNonExistent() {
        String userServiceUrl = String.format("%s/users/%s", USER_SERVICE_API_URL, 1L);
        ResponseEntity<MessageError> responseEntity = testRestTemplate.getForEntity(userServiceUrl, MessageError.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().timestamp()).isNotEmpty();
        assertThat(responseEntity.getBody().status()).isEqualTo(404);
        assertThat(responseEntity.getBody().error()).isEqualTo("Not Found");
        assertThat(responseEntity.getBody().message()).isEqualTo("User with id '1' doesn't exist.");
        assertThat(responseEntity.getBody().path()).isEqualTo("/api/users/1");
        assertThat(responseEntity.getBody().errors()).isNull();
    }

    /*
     * POST /api/users
     * =============== */

    @Test
    @Order(3)
    void testCreateUser() {
        CreateUserRequest createUserRequest = new CreateUserRequest("email@test", "fullName", true);
        String userServiceUrl = String.format("%s/users", USER_SERVICE_API_URL);
        ResponseEntity<UserResponse> responseEntity = testRestTemplate.postForEntity(userServiceUrl, createUserRequest, UserResponse.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().id()).isEqualTo(1L);
        assertThat(responseEntity.getBody().email()).isEqualTo(createUserRequest.email());
        assertThat(responseEntity.getBody().fullName()).isEqualTo(createUserRequest.fullName());
        assertThat(responseEntity.getBody().active()).isEqualTo(createUserRequest.active());

        await().atMost(AT_MOST_DURATION)
                .pollInterval(POLL_INTERVAL_DURATION)
                .untilAsserted(() -> {
                    log.info("Waiting for event-service to receive the message and process ...");
                    String eventServiceUrl = String.format("%s/events?userId=%s", EVENT_SERVICE_API_URL, 1L);
                    ResponseEntity<UserEventResponse[]> eventServiceResponseEntity =
                            testRestTemplate.getForEntity(eventServiceUrl, UserEventResponse[].class);
                    assertThat(eventServiceResponseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
                    assertThat(eventServiceResponseEntity.getBody()).isNotNull();
                    UserEventResponse[] userEventResponses = eventServiceResponseEntity.getBody();
                    assertThat(userEventResponses.length).isEqualTo(1);
                    assertThat(userEventResponses[0].type()).isEqualTo("CREATED");
                });
    }

    /*
     * PUT /api/users/{id}
     * =================== */

    @Test
    @Order(4)
    void testUpdateUser() {
        Long userId = 1L;
        UpdateUserRequest updateUserRequest = new UpdateUserRequest(null, null, false);
        HttpEntity<UpdateUserRequest> requestUpdate = new HttpEntity<>(updateUserRequest);
        String userServiceUrl = String.format("%s/users/%s", USER_SERVICE_API_URL, userId);
        ResponseEntity<UserResponse> responseEntity = testRestTemplate.exchange(userServiceUrl, HttpMethod.PUT, requestUpdate, UserResponse.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().id()).isEqualTo(userId);
        assertThat(responseEntity.getBody().email()).isEqualTo("email@test");
        assertThat(responseEntity.getBody().fullName()).isEqualTo("fullName");
        assertThat(responseEntity.getBody().active()).isEqualTo(updateUserRequest.active());

        await().atMost(AT_MOST_DURATION)
                .pollInterval(POLL_INTERVAL_DURATION)
                .untilAsserted(() -> {
                    log.info("Waiting for event-service to receive the message and process ...");
                    String eventServiceUrl = String.format("%s/events?userId=%s", EVENT_SERVICE_API_URL, userId);
                    ResponseEntity<UserEventResponse[]> eventServiceResponseEntity =
                            testRestTemplate.getForEntity(eventServiceUrl, UserEventResponse[].class);
                    assertThat(eventServiceResponseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
                    assertThat(eventServiceResponseEntity.getBody()).isNotNull();
                    UserEventResponse[] userEventResponses = eventServiceResponseEntity.getBody();
                    assertThat(userEventResponses.length).isEqualTo(2);
                    assertThat(userEventResponses[1].type()).isEqualTo("UPDATED");
                });
    }

    @Test
    @Order(5)
    void testUpdateUserWhenNonExistent() {
        UpdateUserRequest updateUserRequest = new UpdateUserRequest("email@test", "fullName", true);

        HttpEntity<UpdateUserRequest> requestUpdate = new HttpEntity<>(updateUserRequest);
        String userServiceUrl = String.format("%s/users/%s", USER_SERVICE_API_URL, 2L);
        ResponseEntity<MessageError> responseEntity = testRestTemplate.exchange(userServiceUrl, HttpMethod.PUT, requestUpdate, MessageError.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().timestamp()).isNotEmpty();
        assertThat(responseEntity.getBody().status()).isEqualTo(404);
        assertThat(responseEntity.getBody().error()).isEqualTo("Not Found");
        assertThat(responseEntity.getBody().message()).isEqualTo("User with id '2' doesn't exist.");
        assertThat(responseEntity.getBody().path()).isEqualTo("/api/users/2");
        assertThat(responseEntity.getBody().errors()).isNull();
    }

    /*
     * DELETE /api/users/{id}
     * ====================== */

    @Test
    @Order(6)
    void testDeleteUser() {
        Long userId = 1L;
        String userServiceUrl = String.format("%s/users/%s", USER_SERVICE_API_URL, userId);
        ResponseEntity<UserResponse> responseEntity = testRestTemplate.exchange(userServiceUrl, HttpMethod.DELETE, null, UserResponse.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().id()).isEqualTo(userId);
        assertThat(responseEntity.getBody().email()).isEqualTo("email@test");
        assertThat(responseEntity.getBody().fullName()).isEqualTo("fullName");
        assertThat(responseEntity.getBody().active()).isEqualTo(false);

        await().atMost(AT_MOST_DURATION)
                .pollInterval(POLL_INTERVAL_DURATION)
                .untilAsserted(() -> {
                    log.info("Waiting for event-service to receive the message and process ...");
                    String eventServiceUrl = String.format("%s/events?userId=%s", EVENT_SERVICE_API_URL, userId);
                    ResponseEntity<UserEventResponse[]> eventServiceResponseEntity =
                            testRestTemplate.getForEntity(eventServiceUrl, UserEventResponse[].class);
                    assertThat(eventServiceResponseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
                    assertThat(eventServiceResponseEntity.getBody()).isNotNull();
                    UserEventResponse[] userEventResponses = eventServiceResponseEntity.getBody();
                    assertThat(userEventResponses.length).isEqualTo(3);
                    assertThat(userEventResponses[2].type()).isEqualTo("DELETED");
                });
    }

    @Test
    @Order(7)
    void testDeleteUserWhenNonExistent() {
        String userServiceUrl = String.format("%s/users/%s", USER_SERVICE_API_URL, 2L);
        ResponseEntity<MessageError> responseEntity = testRestTemplate.exchange(userServiceUrl, HttpMethod.DELETE, null, MessageError.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().timestamp()).isNotEmpty();
        assertThat(responseEntity.getBody().status()).isEqualTo(404);
        assertThat(responseEntity.getBody().error()).isEqualTo("Not Found");
        assertThat(responseEntity.getBody().message()).isEqualTo("User with id '2' doesn't exist.");
        assertThat(responseEntity.getBody().path()).isEqualTo("/api/users/2");
        assertThat(responseEntity.getBody().errors()).isNull();
    }

    public static final Duration AT_MOST_DURATION = Duration.ofSeconds(10);
    public static final Duration POLL_INTERVAL_DURATION = Duration.ofSeconds(1);
}
