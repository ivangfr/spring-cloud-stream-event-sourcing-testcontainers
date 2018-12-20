package com.mycompany.eventservice.bus;

import com.mycompany.commons.avro.UserEventBus;
import com.mycompany.eventservice.model.UserEvent;
import com.mycompany.eventservice.model.UserEventKey;
import com.mycompany.eventservice.service.UserEventService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import java.util.Date;

@Slf4j
@Component
@EnableBinding(UserSink.class)
public class UserStream {

    private UserEventService userEventService;

    public UserStream(UserEventService userEventService) {
        this.userEventService = userEventService;
    }

    @StreamListener(UserSink.USER_INPUT)
    public void userEventInput(Message<UserEventBus> message/*,
                               @Payload UserEventBus userEventBus,
                               @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                               @Header(KafkaHeaders.RECEIVED_PARTITION_ID) Integer partition,
                               @Header(KafkaHeaders.OFFSET) Long offset*/) {

        // Getting payload and headers from message because an exception is thrown when the annotation @Header is used.
        // The annotation @Payload works fine.
        UserEventBus userEventBus = message.getPayload();
        String topic = message.getHeaders().get(KafkaHeaders.RECEIVED_TOPIC, String.class);
        Integer partition = message.getHeaders().get(KafkaHeaders.RECEIVED_PARTITION_ID, Integer.class);
        Long offset = message.getHeaders().get(KafkaHeaders.OFFSET, Long.class);

        log.info("New user event bus: {}. topic: {}, partition: {}, offset: {}", userEventBus, topic, partition, offset);

        UserEvent userEvent = new UserEvent();
        UserEventKey key = new UserEventKey(userEventBus.getUserId(), new Date());
        userEvent.setKey(key);
        userEvent.setType(userEventBus.getEventType().toString());
        userEvent.setData(userEventBus.getUserJson());

        userEventService.saveUserEvent(userEvent);
    }

}
