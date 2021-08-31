package com.mycompany.userservice.kafka;

import com.google.gson.Gson;
import com.mycompany.userservice.messages.UserEventMessage;
import com.mycompany.userservice.rest.dto.CreateUserDto;
import com.mycompany.userservice.rest.dto.UpdateUserDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.binder.test.OutputDestination;
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.test.context.junit.jupiter.DisabledIf;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

@DisabledIf("#{environment.acceptsProfiles('avro')}")
@SpringBootTest(properties = {
        "spring.zipkin.enabled=false",
        "spring.datasource.url=jdbc:h2:mem:db;DB_CLOSE_DELAY=-1",
        "spring.datasource.username: sa",
        "spring.datasource.password: sa",
        "spring.datasource.driver-class-name: org.h2.Driver"
})
@Import(TestChannelBinderConfiguration.class)
class UserStreamTest {

    @Autowired
    private OutputDestination outputDestination;

    @Autowired
    private UserStream userStream;

    @Autowired
    private Gson gson;

    @Test
    void testUserCreated() {
        CreateUserDto createUserDto = new CreateUserDto("email@test", "fullName", true);

        Message<UserEventMessage> message = userStream.userCreated(1L, createUserDto);

        Message<byte[]> outputMessage = outputDestination.receive(0, BINDING_NAME);
        assertThat(outputMessage).isNotNull();
        assertThat(outputMessage.getHeaders().get(MessageHeaders.CONTENT_TYPE))
                .isEqualTo(MediaType.APPLICATION_JSON_VALUE);
        assertThat(gson.fromJson(new String(outputMessage.getPayload(), StandardCharsets.UTF_8), UserEventMessage.class))
                .isEqualTo(message.getPayload());
    }

    @Test
    void testUserUpdated() {
        UpdateUserDto updateUserDto = new UpdateUserDto();
        updateUserDto.setEmail("email@test");
        updateUserDto.setActive(false);

        Message<UserEventMessage> message = userStream.userUpdated(1L, updateUserDto);

        Message<byte[]> outputMessage = outputDestination.receive(0, BINDING_NAME);
        assertThat(outputMessage).isNotNull();
        assertThat(outputMessage.getHeaders().get(MessageHeaders.CONTENT_TYPE))
                .isEqualTo(MediaType.APPLICATION_JSON_VALUE);
        assertThat(gson.fromJson(new String(outputMessage.getPayload(), StandardCharsets.UTF_8), UserEventMessage.class))
                .isEqualTo(message.getPayload());
    }

    @Test
    void testUserDeleted() {
        Message<UserEventMessage> message = userStream.userDeleted(1L);

        Message<byte[]> outputMessage = outputDestination.receive(0, BINDING_NAME);

        assertThat(outputMessage).isNotNull();
        assertThat(outputMessage.getHeaders().get(MessageHeaders.CONTENT_TYPE))
                .isEqualTo(MediaType.APPLICATION_JSON_VALUE);
        assertThat(gson.fromJson(new String(outputMessage.getPayload(), StandardCharsets.UTF_8), UserEventMessage.class))
                .isEqualTo(message.getPayload());
    }

    private final static String BINDING_NAME = "com.mycompany.userservice.user";
}