package com.ivanfranchin.eventservice.userevent;

import com.ivanfranchin.userservice.messages.UserEventMessage;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class UserEventListener {

  private final UserEventService userEventService;

  @Bean
  Consumer<Message<UserEventMessage>> users() {
    return message -> {
      log.info(
          "\n---\nHeaders: {}\n\nPayload: {}\n---", message.getHeaders(), message.getPayload());
      try {
        userEventService.saveUserEvent(message);
      } catch (Exception e) {
        log.error("An error occurred while saving userEvent {}", message, e);
      }
    };
  }
}
