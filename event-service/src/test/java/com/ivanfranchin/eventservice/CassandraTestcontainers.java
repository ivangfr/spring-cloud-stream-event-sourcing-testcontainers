package com.ivanfranchin.eventservice;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.CassandraContainer;
import org.testcontainers.junit.jupiter.Container;

public class CassandraTestcontainers {

    @Container
    //@ServiceConnection // It is not setting correctly the spring.cassandra.contact-points property.
    private static final CassandraContainer<?> cassandraContainer = new CassandraContainer<>("cassandra:4.1.1");

    @DynamicPropertySource
    private static void dynamicProperties(DynamicPropertyRegistry registry) {
        String contractPoints = String.format("%s:%s", cassandraContainer.getHost(), cassandraContainer.getMappedPort(9042));
        registry.add("spring.cassandra.contact-points", () -> contractPoints);
    }
}
