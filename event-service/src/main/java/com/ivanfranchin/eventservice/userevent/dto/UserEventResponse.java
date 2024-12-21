package com.ivanfranchin.eventservice.userevent.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ivanfranchin.eventservice.userevent.model.UserEvent;
import com.ivanfranchin.eventservice.userevent.model.UserEventKey;

import java.util.Date;

public record UserEventResponse(Long userId,
                                @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ", timezone = "UTC") Date datetime,
                                String type, String data) {

    public static UserEventResponse from(UserEvent userEvent) {
        Long userId = userEventKeyUserId(userEvent);
        Date datetime = userEventKeyDatetime(userEvent);
        String type = userEvent.getType();
        String data = userEvent.getData();
        return new UserEventResponse(userId, datetime, type, data);
    }

    private static Long userEventKeyUserId(UserEvent userEvent) {
        UserEventKey key = userEvent.getKey();
        return key == null ? null : key.getUserId();
    }

    private static Date userEventKeyDatetime(UserEvent userEvent) {
        UserEventKey key = userEvent.getKey();
        return key == null ? null : key.getDatetime();
    }
}
