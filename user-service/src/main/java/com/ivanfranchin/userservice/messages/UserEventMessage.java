package com.ivanfranchin.userservice.messages;

import lombok.Value;

@Value
public class UserEventMessage {
    // It does not work if we change this class to Java Record

    String eventId;
    Long eventTimestamp;
    EventType eventType;
    Long userId;
    String userJson;
}
