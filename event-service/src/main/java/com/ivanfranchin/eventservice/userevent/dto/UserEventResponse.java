package com.ivanfranchin.eventservice.userevent.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ivanfranchin.eventservice.userevent.model.UserEvent;
import com.ivanfranchin.eventservice.userevent.model.UserEventKey;

import java.util.Date;

public record UserEventResponse(Long userId,
                                @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ", timezone = "UTC") Date datetime,
                                String type, String data) {

    public static UserEventResponse from(UserEvent userEvent) {
        UserEventKey key = userEvent.getKey();
        return new UserEventResponse(key.getUserId(), key.getDatetime(), userEvent.getType(), userEvent.getData());
    }
}
