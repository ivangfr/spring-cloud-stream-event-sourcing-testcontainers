package com.ivanfranchin.eventservice.model;

import com.ivanfranchin.userservice.messages.UserEventMessage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;
import org.springframework.messaging.Message;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table("user_events")
public class UserEvent {

    @PrimaryKey
    private UserEventKey key;

    private String type;
    private String data;

    public static UserEvent from(Message<UserEventMessage> message) {
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
