package com.mycompany.userservice.bus;

import com.google.gson.Gson;
import com.mycompany.commons.avro.UserEventBus;
import com.mycompany.commons.avro.event_type;
import com.mycompany.userservice.dto.CreateUserDto;
import com.mycompany.userservice.dto.UpdateUserDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@EnableBinding(UserSource.class)
public class UserStream {

    private final static Logger LOGGER = LoggerFactory.getLogger(UserStream.class);

    private static final long SEND_BUS_TIMEOUT = 3000;

    private Gson gson;

    private UserSource userSource;

    public UserStream(Gson gson, UserSource userSource) {
        this.gson = gson;
        this.userSource = userSource;
    }

    public void userCreated(Long id, CreateUserDto createUserDto) {
        UserEventBus userEventBus = UserEventBus.newBuilder()
                .setEventId(UUID.randomUUID().toString())
                .setEventTimestamp(System.currentTimeMillis())
                .setEventType(event_type.CREATED)
                .setUserId(id)
                .setUserJson(gson.toJson(createUserDto))
                .build();

        sendToBus(userEventBus);
    }

    public void userUpdated(Long id, UpdateUserDto updateUserDto) {
        UserEventBus userEventBus = UserEventBus.newBuilder()
                .setEventId(UUID.randomUUID().toString())
                .setEventTimestamp(System.currentTimeMillis())
                .setEventType(event_type.UPDATED)
                .setUserId(id)
                .setUserJson(gson.toJson(updateUserDto))
                .build();

        sendToBus(userEventBus);
    }

    public void userDeleted(Long id) {
        UserEventBus userEventBus = UserEventBus.newBuilder()
                .setEventId(UUID.randomUUID().toString())
                .setEventTimestamp(System.currentTimeMillis())
                .setEventType(event_type.DELETED)
                .setUserId(id)
                .build();

        sendToBus(userEventBus);
    }

    private void sendToBus(UserEventBus userEventBus) {
        Message<UserEventBus> userEventBusMessage = MessageBuilder.withPayload(userEventBus).build();

        boolean send = userSource.output().send(userEventBusMessage, SEND_BUS_TIMEOUT);
        if (send) {
            LOGGER.info("Event sent to bus. {}", userEventBus);
        } else {
            LOGGER.error("An error occurred while sending event {} to bus.", userEventBus);
        }
    }

}
