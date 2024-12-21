package com.ivanfranchin.eventservice.userevent;

import com.ivanfranchin.eventservice.userevent.model.UserEvent;
import com.ivanfranchin.userservice.messages.UserEventMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Slf4j
@RequiredArgsConstructor
@Component
public class UserEventListener {

    private final UserEventService userEventService;

    @Bean
    Consumer<Message<UserEventMessage>> users() {
        return message -> {
            log.info("\n---\nHeaders: {}\n\nPayload: {}\n---", message.getHeaders(), message.getPayload());
            try {
                userEventService.saveUserEvent(UserEvent.from(message));
            } catch (Exception e) {
                log.error("An error occurred while saving userEvent {}", message, e);
            }
        };
    }
}
