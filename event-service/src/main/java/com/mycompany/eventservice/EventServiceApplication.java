package com.mycompany.eventservice;

import com.mycompany.userservice.messages.UserEventMessage;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.schema.registry.avro.DefaultSubjectNamingStrategy;
import org.springframework.nativex.hint.TypeHint;

@TypeHint(types = {DefaultSubjectNamingStrategy.class, UserEventMessage.class},
        typeNames = {
                "org.springframework.cloud.sleuth.autoconfig.zipkin2.ZipkinKafkaSenderConfiguration",
                "brave.kafka.clients.TracingProducer",
                "brave.kafka.clients.TracingConsumer"
        })
@SpringBootApplication
public class EventServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(EventServiceApplication.class, args);
    }

}
