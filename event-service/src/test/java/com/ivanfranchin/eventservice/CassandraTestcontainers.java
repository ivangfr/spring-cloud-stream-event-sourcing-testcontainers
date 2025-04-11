package com.ivanfranchin.eventservice;

import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.CassandraContainer;
import org.testcontainers.junit.jupiter.Container;

public class CassandraTestcontainers {

    @Container
    @ServiceConnection
    private static final CassandraContainer<?> cassandraContainer = new CassandraContainer<>("cassandra:5.0.3");
}
