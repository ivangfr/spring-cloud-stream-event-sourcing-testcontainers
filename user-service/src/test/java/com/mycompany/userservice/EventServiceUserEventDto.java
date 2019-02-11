package com.mycompany.userservice;

import lombok.Data;

@Data
public class EventServiceUserEventDto {

    private Long userId;
    private String datetime;
    private String type;
    private String data;

}
