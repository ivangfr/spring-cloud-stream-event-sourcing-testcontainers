package com.mycompany.userservice.messages;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserEventMessage {

    private String eventId;
    private Long eventTimestamp;
    private EventType eventType;
    private Long userId;
    private String userJson;

}
