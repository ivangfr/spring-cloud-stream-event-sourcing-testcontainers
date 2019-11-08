package com.mycompany.eventservice.bus;

import com.mycompany.eventservice.model.UserEvent;
import com.mycompany.eventservice.model.UserEventKey;
import com.mycompany.eventservice.service.UserEventService;
import com.mycompany.userservice.messages.UserEventMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import java.util.Date;

@Slf4j
@Component
@EnableBinding(Sink.class)
public class UserStream {

    private final UserEventService userEventService;

    public UserStream(UserEventService userEventService) {
        this.userEventService = userEventService;
    }

    @StreamListener(Sink.INPUT)
    public void process(Message<UserEventMessage> message) {
        log.info("\n---\nHeaders: {}\n\nPayload: {}\n---", message.getHeaders(), message.getPayload());

        userEventService.saveUserEvent(createUserEvent(message));
    }

    private UserEvent createUserEvent(Message<UserEventMessage> message) {
        UserEventMessage userEventMessage = message.getPayload();
        UserEvent userEvent = new UserEvent();
        UserEventKey key = new UserEventKey(userEventMessage.getUserId(), new Date());
        userEvent.setKey(key);
        userEvent.setType(userEventMessage.getEventType().toString());
        userEvent.setData(userEventMessage.getUserJson());
        return userEvent;
    }

}
