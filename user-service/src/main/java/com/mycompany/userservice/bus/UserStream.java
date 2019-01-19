package com.mycompany.userservice.bus;

import com.google.gson.Gson;
import com.mycompany.userservice.commons.events.EventType;
import com.mycompany.userservice.commons.events.UserEventBus;
import com.mycompany.userservice.dto.CreateUserDto;
import com.mycompany.userservice.dto.UpdateUserDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@EnableBinding(Source.class)
public class UserStream {

    private static final long SEND_BUS_TIMEOUT = 3000;

    private final Source source;
    private final Gson gson;

    public UserStream(Source source, Gson gson) {
        this.source = source;
        this.gson = gson;
    }

    public void userCreated(Long id, CreateUserDto createUserDto) {
        UserEventBus userEventBus = UserEventBus.builder()
                .eventId(UUID.randomUUID().toString())
                .eventTimestamp(System.currentTimeMillis())
                .eventType(EventType.CREATED)
                .userId(id)
                .userJson(gson.toJson(createUserDto))
                .build();

        sendToBus(id, userEventBus);
    }

    public void userUpdated(Long id, UpdateUserDto updateUserDto) {
        UserEventBus userEventBus = UserEventBus.builder()
                .eventId(UUID.randomUUID().toString())
                .eventTimestamp(System.currentTimeMillis())
                .eventType(EventType.UPDATED)
                .userId(id)
                .userJson(gson.toJson(updateUserDto))
                .build();

        sendToBus(id, userEventBus);
    }

    public void userDeleted(Long id) {
        UserEventBus userEventBus = UserEventBus.builder()
                .eventId(UUID.randomUUID().toString())
                .eventTimestamp(System.currentTimeMillis())
                .eventType(EventType.DELETED)
                .userId(id)
                .build();

        sendToBus(id, userEventBus);
    }

    private void sendToBus(Long partitionKey, UserEventBus userEventBus) {
        Message<UserEventBus> message = MessageBuilder.withPayload(userEventBus)
                .setHeader("partitionKey", partitionKey)
                .build();

        source.output().send(message, SEND_BUS_TIMEOUT);
        log.info("\n---\nHeaders: {}\n\nPayload: {}\n---", message.getHeaders(), message.getPayload());
    }

}
