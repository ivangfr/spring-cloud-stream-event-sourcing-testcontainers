package com.mycompany.eventservice.bus;

import com.mycompany.eventservice.model.UserEvent;
import com.mycompany.eventservice.model.UserEventKey;
import com.mycompany.eventservice.service.UserEventService;
import com.mycompany.userservice.messages.UserEventMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.function.Consumer;

@Slf4j
@RequiredArgsConstructor
@Component
public class UserStream {

    private final UserEventService userEventService;

    @Bean
    public Consumer<Message<UserEventMessage>> users() {
        return message -> {
            log.info("\n---\nHeaders: {}\n\nPayload: {}\n---", message.getHeaders(), message.getPayload());
            userEventService.saveUserEvent(createUserEvent(message));
        };
    }

    private UserEvent createUserEvent(Message<UserEventMessage> message) {
        UserEventMessage userEventMessage = message.getPayload();
        UserEvent userEvent = new UserEvent();
        UserEventKey key = new UserEventKey(userEventMessage.getUserId(), new Date());
        userEvent.setKey(key);
        userEvent.setType(userEventMessage.getEventType().toString());
        CharSequence userJson = userEventMessage.getUserJson();
        if (userJson != null) {
            userEvent.setData(userJson.toString());
        }
        return userEvent;
    }

}
