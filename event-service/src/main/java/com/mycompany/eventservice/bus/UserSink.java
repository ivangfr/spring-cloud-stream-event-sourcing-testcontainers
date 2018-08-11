package com.mycompany.eventservice.bus;

import org.springframework.cloud.stream.annotation.Input;
import org.springframework.messaging.SubscribableChannel;

public interface UserSink {

    String USER_INPUT = "user-input";

    @Input(UserSink.USER_INPUT)
    SubscribableChannel input();

}
