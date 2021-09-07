package com.mycompany.userservice.kafka;

import com.google.gson.Gson;
import com.mycompany.userservice.messages.EventType;
import com.mycompany.userservice.messages.UserEventMessage;
import com.mycompany.userservice.rest.dto.CreateUserDto;
import com.mycompany.userservice.rest.dto.UpdateUserDto;
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
    private final Gson gson;

    @Value("${spring.cloud.stream.bindings.users-out-0.content-type}")
    private String streamOutMimeType;

    public Message<UserEventMessage> userCreated(Long id, CreateUserDto createUserDto) {
        UserEventMessage userEventMessage = UserEventMessage.of(
                getId(),System.currentTimeMillis(), EventType.CREATED, id, gson.toJson(createUserDto));
        return sendToBus(id, userEventMessage);
    }

    public Message<UserEventMessage> userUpdated(Long id, UpdateUserDto updateUserDto) {
        UserEventMessage userEventMessage = UserEventMessage.of(
                getId(), System.currentTimeMillis(), EventType.UPDATED, id, gson.toJson(updateUserDto));
        return sendToBus(id, userEventMessage);
    }

    public Message<UserEventMessage> userDeleted(Long id) {
        UserEventMessage userEventMessage = UserEventMessage.of(
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
}
