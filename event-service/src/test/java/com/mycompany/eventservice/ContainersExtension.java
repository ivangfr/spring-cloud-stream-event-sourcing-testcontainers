package com.mycompany.eventservice;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testcontainers.containers.CassandraContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Collections;

@Testcontainers
public class ContainersExtension implements BeforeAllCallback, AfterAllCallback {

    @Container
    private static CassandraContainer cassandraContainer;

    @Container
    private static KafkaContainer kafkaContainer;

    @Override
    public void beforeAll(ExtensionContext extensionContext) {

        // Cassandra
        cassandraContainer = new CassandraContainer("cassandra:3.11.3");
        cassandraContainer.setPortBindings(Collections.singletonList("9042:9042"));
        cassandraContainer.start();

        // Kafka
        kafkaContainer = new KafkaContainer("5.1.0").withEmbeddedZookeeper();
        kafkaContainer.setNetworkAliases(Collections.singletonList("kafka"));
        kafkaContainer.setPortBindings(Collections.singletonList("9092:9092"));
        kafkaContainer.start();
    }

    @Override
    public void afterAll(ExtensionContext extensionContext) {
        cassandraContainer.stop();
        kafkaContainer.stop();
    }

}
