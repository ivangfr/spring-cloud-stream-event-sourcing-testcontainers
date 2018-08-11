package com.mycompany.userservice.bus;

import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;

public interface UserSource {

    String USER_OUTPUT = "user-output";

    @Output(UserSource.USER_OUTPUT)
    MessageChannel output();

}
