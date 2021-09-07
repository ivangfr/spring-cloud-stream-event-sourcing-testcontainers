package com.mycompany.eventservice.kafka;

import com.mycompany.eventservice.EventServiceApplication;
import com.mycompany.eventservice.model.UserEvent;
import com.mycompany.eventservice.repository.UserEventRepository;
import com.mycompany.userservice.messages.EventType;
import com.mycompany.userservice.messages.UserEventMessage;
import org.junit.jupiter.api.Test;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.stream.binder.test.InputDestination;
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.messaging.support.MessageBuilder;
import org.testcontainers.containers.CassandraContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * This class has the same test as {@link UserStreamTest} using a different style
 */
@Testcontainers
class UserStream2Test {

    @Container
    private static final CassandraContainer<?> cassandraContainer = new CassandraContainer<>("cassandra:3.11.11");

    @Test
    void testUsers() {
        String contractPoints = String.format("%s:%s",
                cassandraContainer.getHost(),
                cassandraContainer.getMappedPort(9042));

        try (ConfigurableApplicationContext context = new SpringApplicationBuilder(
                TestChannelBinderConfiguration
                        .getCompleteConfiguration(EventServiceApplication.class))
                .web(WebApplicationType.NONE)
                .run("--spring.zipkin.enabled=false",
                        "--spring.data.cassandra.contact-points=" + contractPoints)) {

            String eventId = UUID.randomUUID().toString();
            Date datetime = new Date();
            EventType eventType = EventType.CREATED;
            Long userId = 1L;
            String userJson = "{\"email\":\"email\",\"fullName\":\"fullName\",\"active\":true}";

            UserEventMessage userEventMessage = UserEventMessage.newBuilder()
                    .setEventId(eventId)
                    .setEventTimestamp(datetime.getTime())
                    .setEventType(eventType)
                    .setUserId(userId)
                    .setUserJson(userJson)
                    .build();

            InputDestination inputDestination = context.getBean(InputDestination.class);
            inputDestination.send(MessageBuilder.withPayload(userEventMessage).build(), "com.mycompany.userservice.user");

            UserEventRepository userEventRepository = context.getBean(UserEventRepository.class);
            List<UserEvent> userEvents = userEventRepository.findByKeyUserId(userId);

            assertThat(userEvents).isNotNull();
            assertThat(userEvents.size()).isEqualTo(1);
            assertThat(userEvents.get(0).getKey().getUserId()).isEqualTo(userId);
            assertThat(userEvents.get(0).getKey().getDatetime()).isEqualTo(datetime);
            assertThat(userEvents.get(0).getData()).isEqualTo(userJson);
            assertThat(userEvents.get(0).getType()).isEqualTo(eventType.name());
        }
    }
}