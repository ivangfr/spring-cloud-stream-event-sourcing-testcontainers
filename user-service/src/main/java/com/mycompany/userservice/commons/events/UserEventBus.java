package com.mycompany.userservice.commons.events;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserEventBus {

    private String eventId;
    private Long eventTimestamp;
    private EventType eventType;
    private Long userId;
    private String userJson;

}
