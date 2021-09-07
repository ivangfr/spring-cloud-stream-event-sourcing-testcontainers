package com.mycompany.eventservice.rest.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Value;

import java.util.Date;

@Value
public class UserEventDto {

    Long userId;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ", timezone = "UTC")
    Date datetime;

    String type;
    String data;
}
