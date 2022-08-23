package com.ivanfranchin.userservice.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ivanfranchin.userservice.exception.UserStreamJsonProcessingException;
import com.ivanfranchin.userservice.messages.EventType;
import com.ivanfranchin.userservice.messages.UserEventMessage;
import com.ivanfranchin.userservice.rest.dto.CreateUserRequest;
import com.ivanfranchin.userservice.rest.dto.UpdateUserRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeType;

import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Component
public class UserStream {

    private final StreamBridge streamBridge;
    private final ObjectMapper objectMapper;

    @Value("${spring.cloud.stream.bindings.users-out-0.content-type}")
    private String streamOutMimeType;

    public Message<UserEventMessage> userCreated(Long id, CreateUserRequest createUserRequest) {
        UserEventMessage userEventMessage = new UserEventMessage(
                getId(), System.currentTimeMillis(), EventType.CREATED, id, writeValueAsString(createUserRequest));
        return sendToBus(id, userEventMessage);
    }

    public Message<UserEventMessage> userUpdated(Long id, UpdateUserRequest updateUserRequest) {
        UserEventMessage userEventMessage = new UserEventMessage(
                getId(), System.currentTimeMillis(), EventType.UPDATED, id, writeValueAsString(updateUserRequest));
        return sendToBus(id, userEventMessage);
    }

    public Message<UserEventMessage> userDeleted(Long id) {
        UserEventMessage userEventMessage = new UserEventMessage(
                getId(), System.currentTimeMillis(), EventType.DELETED, id, null);
        return sendToBus(id, userEventMessage);
    }

    private Message<UserEventMessage> sendToBus(Long partitionKey, UserEventMessage userEventMessage) {
        Message<UserEventMessage> message = MessageBuilder.withPayload(userEventMessage)
                .setHeader("partitionKey", partitionKey)
                .build();

        streamBridge.send("users-out-0", message, MimeType.valueOf(streamOutMimeType));
        log.info("\n---\nHeaders: {}\n\nPayload: {}\n---", message.getHeaders(), message.getPayload());
        return message;
    }

    private String getId() {
        return UUID.randomUUID().toString();
    }

    private String writeValueAsString(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new UserStreamJsonProcessingException(e);
        }
    }
}
