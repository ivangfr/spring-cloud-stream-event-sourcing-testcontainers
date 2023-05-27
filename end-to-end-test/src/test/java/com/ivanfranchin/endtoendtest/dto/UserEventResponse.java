package com.ivanfranchin.endtoendtest.dto;

public record UserEventResponse(Long userId, String datetime, String type, String data) {
}
