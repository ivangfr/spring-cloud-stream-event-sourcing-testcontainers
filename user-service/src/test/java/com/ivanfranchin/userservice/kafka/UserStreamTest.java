package com.ivanfranchin.userservice.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ivanfranchin.userservice.MySQLTestcontainers;
import com.ivanfranchin.userservice.rest.dto.CreateUserRequest;
import com.ivanfranchin.userservice.rest.dto.UpdateUserRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.context.ImportTestcontainers;
import org.springframework.cloud.stream.binder.test.OutputDestination;
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.test.context.junit.jupiter.DisabledIf;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@DisabledIf("#{environment.acceptsProfiles('avro')}")
@SpringBootTest
@ImportTestcontainers(MySQLTestcontainers.class)
@Import(TestChannelBinderConfiguration.class)
class UserStreamTest {

    @Autowired
    private OutputDestination outputDestination;

    @Autowired
    private UserStream userStream;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testUserCreated() throws IOException {
        CreateUserRequest createUserRequest = new CreateUserRequest("email@test", "fullName", true);

        Message<UserEventMessage> message = userStream.userCreated(1L, createUserRequest);

        Message<byte[]> outputMessage = outputDestination.receive(0, BINDING_NAME);
        assertThat(outputMessage).isNotNull();
        assertThat(outputMessage.getHeaders().get(MessageHeaders.CONTENT_TYPE))
                .isEqualTo(MediaType.APPLICATION_JSON_VALUE);
        UserEventMessage userEventMessage = objectMapper.readValue(outputMessage.getPayload(), UserEventMessage.class);
        assertThat(userEventMessage).isEqualTo(message.getPayload());
    }

    @Test
    void testUserUpdated() throws IOException {
        UpdateUserRequest updateUserRequest = new UpdateUserRequest(null, "email@test", false);

        Message<UserEventMessage> message = userStream.userUpdated(1L, updateUserRequest);

        Message<byte[]> outputMessage = outputDestination.receive(0, BINDING_NAME);
        assertThat(outputMessage).isNotNull();
        assertThat(outputMessage.getHeaders().get(MessageHeaders.CONTENT_TYPE))
                .isEqualTo(MediaType.APPLICATION_JSON_VALUE);
        UserEventMessage userEventMessage = objectMapper.readValue(outputMessage.getPayload(), UserEventMessage.class);
        assertThat(userEventMessage).isEqualTo(message.getPayload());
    }

    @Test
    void testUserDeleted() throws IOException {
        Message<UserEventMessage> message = userStream.userDeleted(1L);

        Message<byte[]> outputMessage = outputDestination.receive(0, BINDING_NAME);

        assertThat(outputMessage).isNotNull();
        assertThat(outputMessage.getHeaders().get(MessageHeaders.CONTENT_TYPE))
                .isEqualTo(MediaType.APPLICATION_JSON_VALUE);
        UserEventMessage userEventMessage = objectMapper.readValue(outputMessage.getPayload(), UserEventMessage.class);
        assertThat(userEventMessage).isEqualTo(message.getPayload());
    }

    private final static String BINDING_NAME = "com.ivanfranchin.userservice.user";
}