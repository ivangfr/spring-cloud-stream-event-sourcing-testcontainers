package com.mycompany.userservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.schema.registry.avro.DefaultSubjectNamingStrategy;
import org.springframework.nativex.hint.TypeHint;

@TypeHint(types = DefaultSubjectNamingStrategy.class,
        typeNames = {
                "org.springframework.cloud.sleuth.autoconfig.zipkin2.ZipkinKafkaSenderConfiguration",
                "brave.kafka.clients.TracingProducer",
                "brave.kafka.clients.TracingConsumer"
        })
@SpringBootApplication
public class UserServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }

}
