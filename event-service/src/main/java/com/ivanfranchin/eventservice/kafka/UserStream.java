package com.ivanfranchin.eventservice.kafka;

import com.ivanfranchin.eventservice.mapper.UserMapper;
import com.ivanfranchin.eventservice.service.UserEventService;
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
public class UserStream {

    private final UserEventService userEventService;
    private final UserMapper userMapper;

    @Bean
    public Consumer<Message<UserEventMessage>> users() {
        return message -> {
            log.info("\n---\nHeaders: {}\n\nPayload: {}\n---", message.getHeaders(), message.getPayload());
            userEventService.saveUserEvent(userMapper.createUserEvent(message));
        };
    }
}
