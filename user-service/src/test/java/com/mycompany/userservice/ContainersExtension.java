package com.mycompany.userservice;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testcontainers.containers.CassandraContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;
import java.util.Collections;

@Testcontainers
public class ContainersExtension implements BeforeAllCallback, AfterAllCallback {

    @Container
    private static MySQLContainer mySQLContainer;

    @Container
    private static KafkaContainer kafkaContainer;

    @Container
    private static CassandraContainer cassandraContainer;

    @Container
    private static GenericContainer eventServiceContainer;

    @Override
    public void beforeAll(ExtensionContext extensionContext) {

        Network network = Network.SHARED;

        // MySQL
        mySQLContainer = new MySQLContainer("mysql:5.7.28")
                .withDatabaseName("userdb-test")
                .withUsername("root-test")
                .withPassword("secret-test");
        mySQLContainer.setNetwork(network);
        mySQLContainer.setNetworkAliases(Collections.singletonList("mysql"));
        mySQLContainer.setPortBindings(Collections.singletonList("3306:3306"));
        mySQLContainer.start();

        // Kafka
        kafkaContainer = new KafkaContainer("5.3.1").withEmbeddedZookeeper();
        kafkaContainer.setNetwork(network);
        kafkaContainer.setNetworkAliases(Collections.singletonList("kafka"));
        kafkaContainer.setPortBindings(Collections.singletonList("9092:9092"));
        kafkaContainer.start();

        // Cassandra
        cassandraContainer = new CassandraContainer("cassandra:3.11.5");
        cassandraContainer.setNetwork(network);
        cassandraContainer.setNetworkAliases(Collections.singletonList("cassandra"));
        cassandraContainer.setPortBindings(Collections.singletonList("9042:9042"));
        cassandraContainer.start();

        // event-service
        eventServiceContainer = new GenericContainer("docker.mycompany.com/event-service:1.0.0")
                .withNetwork(network)
                .withNetworkAliases("event-service")
                .withEnv("CASSANDRA_HOST", "cassandra")
                .withEnv("KAFKA_HOST", "kafka")
                .withEnv("KAFKA_PORT", "9092");
        eventServiceContainer.setPortBindings(Collections.singletonList("9081:8080"));
        eventServiceContainer.setWaitStrategy(Wait.forHttp("/actuator/health").forPort(8080).forStatusCode(200).withStartupTimeout(Duration.ofMinutes(2)));
        eventServiceContainer.start();
    }

    @Override
    public void afterAll(ExtensionContext extensionContext) {
        mySQLContainer.stop();
        kafkaContainer.stop();
        cassandraContainer.stop();
        eventServiceContainer.stop();
    }

}
