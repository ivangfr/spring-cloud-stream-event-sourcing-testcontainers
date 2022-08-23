package com.ivanfranchin.eventservice.mapper;

import com.ivanfranchin.eventservice.model.UserEvent;
import com.ivanfranchin.eventservice.model.UserEventKey;
import com.ivanfranchin.eventservice.rest.dto.UserEventResponse;
import com.ivanfranchin.userservice.messages.EventType;
import com.ivanfranchin.userservice.messages.UserEventMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Date;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@Import(UserMapperImpl.class)
class UserMapperTest {

    @Autowired
    private UserMapper userMapper;

    @Test
    void testToUserEventResponse() {
        Long userId = 1L;
        Date datetime = new Date();
        String data = "data";
        String type = "type";
        UserEvent userEvent = new UserEvent(new UserEventKey(userId, datetime), type, data);

        UserEventResponse userEventResponse = userMapper.toUserEventResponse(userEvent);

        assertThat(userEventResponse).isNotNull();
        assertThat(userEventResponse.userId()).isEqualTo(userId);
        assertThat(userEventResponse.datetime()).isEqualTo(datetime);
        assertThat(userEventResponse.data()).isEqualTo(data);
        assertThat(userEventResponse.type()).isEqualTo(type);
    }

    @Test
    void testCreateUserEvent() {
        String eventId = UUID.randomUUID().toString();
        Date datetime = new Date();
        EventType eventType = EventType.CREATED;
        Long userId = 1L;
        String userJson = "{\"email\":\"email\",\"fullName\":\"fullName\",\"active\":true}";

        UserEventMessage userEventMessage = UserEventMessage.newBuilder()
                .setEventId(eventId)
                .setEventTimestamp(datetime.getTime())
                .setEventType(eventType)
                .setUserId(userId)
                .setUserJson(userJson)
                .build();

        UserEvent userEvent = userMapper.createUserEvent(MessageBuilder.withPayload(userEventMessage).build());

        assertThat(userEvent).isNotNull();
        assertThat(userEvent.getKey().getUserId()).isEqualTo(userId);
        assertThat(userEvent.getKey().getDatetime()).isEqualTo(datetime);
        assertThat(userEvent.getData()).isEqualTo(userJson);
        assertThat(userEvent.getType()).isEqualTo(eventType.name());
    }
}