package com.mycompany.userservice.messages;

public record UserEventMessage(String eventId, Long eventTimestamp, EventType eventType, Long userId, String userJson) {
}
