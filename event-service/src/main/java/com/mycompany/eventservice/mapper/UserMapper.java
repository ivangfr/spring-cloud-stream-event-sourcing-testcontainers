package com.mycompany.eventservice.mapper;

import com.mycompany.eventservice.model.UserEvent;
import com.mycompany.eventservice.model.UserEventKey;
import com.mycompany.eventservice.rest.dto.UserEventDto;
import com.mycompany.userservice.messages.UserEventMessage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;

import java.util.Date;

@Configuration
@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(source = "key.userId", target = "userId")
    @Mapping(source = "key.datetime", target = "datetime")
    UserEventDto toUserEventDto(UserEvent userEvent);

    default UserEvent createUserEvent(Message<UserEventMessage> message) {
        UserEventMessage userEventMessage = message.getPayload();
        UserEvent userEvent = new UserEvent();
        userEvent.setKey(
                new UserEventKey(
                        userEventMessage.getUserId(),
                        new Date(userEventMessage.getEventTimestamp())
                ));
        userEvent.setType(userEventMessage.getEventType().toString());
        CharSequence userJson = userEventMessage.getUserJson();
        if (userJson != null) {
            userEvent.setData(userJson.toString());
        }
        return userEvent;
    }

}
