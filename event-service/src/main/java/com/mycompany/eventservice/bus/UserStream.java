package com.mycompany.eventservice.bus;

import com.mycompany.commons.avro.UserEventBus;
import com.mycompany.eventservice.model.UserEvent;
import com.mycompany.eventservice.model.UserEventKey;
import com.mycompany.eventservice.service.UserEventService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.stereotype.Component;

import java.util.Date;

@Slf4j
@Component
@EnableBinding(UserSink.class)
public class UserStream {

    private UserEventService userEventService;

    public UserStream(UserEventService userEventService) {
        this.userEventService = userEventService;
    }

    @StreamListener(UserSink.INPUT)
    public void userEventInput(UserEventBus userEventBus) {
        log.info("New user event bus: {}", userEventBus);

        UserEvent userEvent = new UserEvent();
        UserEventKey key = new UserEventKey(userEventBus.getUserId(), new Date());
        userEvent.setKey(key);
        userEvent.setType(userEventBus.getEventType().toString());
        userEvent.setData(userEventBus.getUserJson());

        userEventService.saveUserEvent(userEvent);
    }

}
