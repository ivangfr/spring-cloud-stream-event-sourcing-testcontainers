package com.mycompany.userservice.messages;

import lombok.Value;

@Value(staticConstructor = "of")
public class UserEventMessage {

    String eventId;
    Long eventTimestamp;
    EventType eventType;
    Long userId;
    String userJson;
}
