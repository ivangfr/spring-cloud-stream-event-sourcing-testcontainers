package com.ivanfranchin.eventservice.userevent;

import com.ivanfranchin.eventservice.CassandraTestcontainers;
import com.ivanfranchin.eventservice.config.CassandraConfig;
import com.ivanfranchin.eventservice.userevent.model.UserEvent;
import com.ivanfranchin.eventservice.userevent.model.UserEventKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.cassandra.test.autoconfigure.DataCassandraTest;
import org.springframework.boot.testcontainers.context.ImportTestcontainers;
import org.springframework.context.annotation.Import;

import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Disabled("Does not work with @DataCassandraTest slice on SB 4.x; see UserEventListener2Test for working pattern")
@DataCassandraTest
@Import(CassandraConfig.class)
@ImportTestcontainers(CassandraTestcontainers.class)
class UserEventRepositoryTest {

    @Autowired
    private UserEventRepository userEventRepository;

    @BeforeEach
    void setUp() {
        userEventRepository.deleteAll();
    }

    @Test
    void testFindByKeyUserIdWhenThereIsNone() {
        List<UserEvent> userEvents = userEventRepository.findByKeyUserId(1L);
        assertThat(userEvents).isEmpty();
    }

    // Sometimes this test fails due to: Caused by: com.datastax.oss.driver.api.core.DriverTimeoutException: Query timed out after PT2S
    @Test
    void testFindByKeyUserIdWhenThereIsOne() {
        UserEvent userEvent = createUserEvent(1L, new Date(), "type", "data");
        userEventRepository.save(userEvent);

        List<UserEvent> userEvents = userEventRepository.findByKeyUserId(1L);

        assertThat(userEvents).hasSize(1);
        assertThat(userEvents.getFirst()).isEqualTo(userEvent);
    }

    @Test
    void testFindByKeyUserIdWhenThereAreTwo() {
        Date now = new Date();
        UserEvent userEvent1 = createUserEvent(1L, new Date(now.getTime() - 5000), "type1", "data1");
        userEventRepository.save(userEvent1);

        UserEvent userEvent2 = createUserEvent(1L, now, "type2", "data2");
        userEventRepository.save(userEvent2);

        List<UserEvent> userEvents = userEventRepository.findByKeyUserId(1L);

        assertThat(userEvents).hasSize(2);

        assertThat(userEvents.get(0)).isEqualTo(userEvent1);
        assertThat(userEvents.get(1)).isEqualTo(userEvent2);
    }

    private UserEvent createUserEvent(Long userId, Date datetime, String type, String data) {
        return new UserEvent(new UserEventKey(userId, datetime), type, data);
    }
}