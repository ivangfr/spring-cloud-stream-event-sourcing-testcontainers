package com.mycompany.userservice.bus;

import com.google.gson.Gson;
import com.mycompany.userservice.dto.CreateUserDto;
import com.mycompany.userservice.dto.UpdateUserDto;
import com.mycompany.userservice.messages.EventType;
import com.mycompany.userservice.messages.UserEventMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Component
@EnableBinding(Source.class)
public class UserStream {

    private static final long SEND_BUS_TIMEOUT = 3000;

    private final Source source;
    private final Gson gson;

    public void userCreated(Long id, CreateUserDto createUserDto) {
        UserEventMessage userEventMessage = UserEventMessage.builder()
                .eventId(UUID.randomUUID().toString())
                .eventTimestamp(System.currentTimeMillis())
                .eventType(EventType.CREATED)
                .userId(id)
                .userJson(gson.toJson(createUserDto))
                .build();

        sendToBus(id, userEventMessage);
    }

    public void userUpdated(Long id, UpdateUserDto updateUserDto) {
        UserEventMessage userEventMessage = UserEventMessage.builder()
                .eventId(UUID.randomUUID().toString())
                .eventTimestamp(System.currentTimeMillis())
                .eventType(EventType.UPDATED)
                .userId(id)
                .userJson(gson.toJson(updateUserDto))
                .build();

        sendToBus(id, userEventMessage);
    }

    public void userDeleted(Long id) {
        UserEventMessage userEventMessage = UserEventMessage.builder()
                .eventId(UUID.randomUUID().toString())
                .eventTimestamp(System.currentTimeMillis())
                .eventType(EventType.DELETED)
                .userId(id)
                .build();

        sendToBus(id, userEventMessage);
    }

    private void sendToBus(Long partitionKey, UserEventMessage userEventMessage) {
        Message<UserEventMessage> message = MessageBuilder.withPayload(userEventMessage)
                .setHeader("partitionKey", partitionKey)
                .build();

        source.output().send(message, SEND_BUS_TIMEOUT);
        log.info("\n---\nHeaders: {}\n\nPayload: {}\n---", message.getHeaders(), message.getPayload());
    }

}
