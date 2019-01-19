package com.mycompany.eventservice.bus;

import com.mycompany.userservice.commons.events.UserEventBus;
import com.mycompany.eventservice.model.UserEvent;
import com.mycompany.eventservice.model.UserEventKey;
import com.mycompany.eventservice.service.UserEventService;
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
    public void process(Message<UserEventBus> message) {
        log.info("\n---\nHeaders: {}\n\nPayload: {}\n---", message.getHeaders(), message.getPayload());

        UserEventBus userEventBus = message.getPayload();
        UserEvent userEvent = new UserEvent();
        UserEventKey key = new UserEventKey(userEventBus.getUserId(), new Date());
        userEvent.setKey(key);
        userEvent.setType(userEventBus.getEventType().toString());
        userEvent.setData(userEventBus.getUserJson());

        userEventService.saveUserEvent(userEvent);
    }

}
