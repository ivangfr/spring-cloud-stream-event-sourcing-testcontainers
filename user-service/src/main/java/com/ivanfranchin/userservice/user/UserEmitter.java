package com.ivanfranchin.userservice.user;

import com.ivanfranchin.userservice.user.dto.CreateUserRequest;
import com.ivanfranchin.userservice.user.dto.UpdateUserRequest;
import com.ivanfranchin.userservice.user.event.EventType;
import com.ivanfranchin.userservice.user.event.UserEventMessage;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeType;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@RequiredArgsConstructor
@Component
public class UserEmitter {

  private final StreamBridge streamBridge;
  private final ObjectMapper objectMapper;

  @Value("${spring.cloud.stream.bindings.users-out-0.content-type}")
  private String streamOutMimeType;

  public Message<UserEventMessage> userCreated(Long id, CreateUserRequest createUserRequest) {
    UserEventMessage userEventMessage =
        new UserEventMessage(
            getId(),
            System.currentTimeMillis(),
            EventType.CREATED,
            id,
            objectMapper.writeValueAsString(createUserRequest));
    return sendToBus(id, userEventMessage);
  }

  public Message<UserEventMessage> userUpdated(Long id, UpdateUserRequest updateUserRequest) {
    UserEventMessage userEventMessage =
        new UserEventMessage(
            getId(),
            System.currentTimeMillis(),
            EventType.UPDATED,
            id,
            objectMapper.writeValueAsString(updateUserRequest));
    return sendToBus(id, userEventMessage);
  }

  public Message<UserEventMessage> userDeleted(Long id) {
    UserEventMessage userEventMessage =
        new UserEventMessage(getId(), System.currentTimeMillis(), EventType.DELETED, id, null);
    return sendToBus(id, userEventMessage);
  }

  private Message<UserEventMessage> sendToBus(
      Long partitionKey, UserEventMessage userEventMessage) {
    Message<UserEventMessage> message =
        MessageBuilder.withPayload(userEventMessage)
            .setHeader("partitionKey", partitionKey)
            .build();

    streamBridge.send("users-out-0", message, MimeType.valueOf(streamOutMimeType));
    log.info("\n---\nHeaders: {}\n\nPayload: {}\n---", message.getHeaders(), message.getPayload());
    return message;
  }

  private String getId() {
    return UUID.randomUUID().toString();
  }
}
