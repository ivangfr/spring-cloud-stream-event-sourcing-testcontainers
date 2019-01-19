package com.mycompany.userservice.commons.events;

import lombok.Data;

@Data
public class UserEventBus {

    private String eventId;
    private Long eventTimestamp;
    private EventType eventType;
    private Long userId;
    private String userJson;

}
