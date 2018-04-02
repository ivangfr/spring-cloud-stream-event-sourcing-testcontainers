package com.mycompany.eventservice.bus;

import org.springframework.cloud.stream.annotation.Input;
import org.springframework.messaging.SubscribableChannel;

public interface UserSink {

    String INPUT = "user";

    @Input(UserSink.INPUT)
    SubscribableChannel input();

}
