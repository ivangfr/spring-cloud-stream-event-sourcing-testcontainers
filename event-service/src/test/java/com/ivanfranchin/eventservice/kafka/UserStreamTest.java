package com.ivanfranchin.eventservice.kafka;

import com.ivanfranchin.eventservice.CassandraTestcontainers;
import com.ivanfranchin.eventservice.model.UserEvent;
import com.ivanfranchin.eventservice.repository.UserEventRepository;
import com.ivanfranchin.userservice.messages.EventType;
import com.ivanfranchin.userservice.messages.UserEventMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.context.ImportTestcontainers;
import org.springframework.cloud.stream.binder.test.InputDestination;
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.context.ActiveProfiles;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * This class has the same test as {@link UserStream2Test} using a different style
 */
@Disabled("It was disabled because the @ImportTestcontainers stop working since upgrade to Spring Boot 3.2.x")
@ActiveProfiles("test")
@SpringBootTest
@Import(TestChannelBinderConfiguration.class)
@ImportTestcontainers(CassandraTestcontainers.class)
class UserStreamTest {

    @Autowired
    private InputDestination inputDestination;

    @Autowired
    private UserEventRepository userEventRepository;

    @BeforeEach
    void setUp() {
        userEventRepository.deleteAll();
    }

    @Test
    void testUsers() {
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

        inputDestination.send(MessageBuilder.withPayload(userEventMessage).build(), "com.ivanfranchin.userservice.user");
        List<UserEvent> userEvents = userEventRepository.findByKeyUserId(userId);

        assertThat(userEvents).isNotNull();
        assertThat(userEvents.size()).isEqualTo(1);
        assertThat(userEvents.get(0).getKey().getUserId()).isEqualTo(userId);
        assertThat(userEvents.get(0).getKey().getDatetime()).isEqualTo(datetime);
        assertThat(userEvents.get(0).getData()).isEqualTo(userJson);
        assertThat(userEvents.get(0).getType()).isEqualTo(eventType.name());
    }
}