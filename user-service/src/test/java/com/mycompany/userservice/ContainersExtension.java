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
    private static MySQLContainer<?> mySQLContainer;

    @Container
    private static GenericContainer<?> zookeeperContainer;

    @Container
    private static KafkaContainer kafkaContainer;

//    @Container
//    private static GenericContainer<?> schemaRegistryContainer;

    @Container
    private static CassandraContainer<?> cassandraContainer;

    @Container
    private static GenericContainer<?> eventServiceContainer;

    @Override
    public void beforeAll(ExtensionContext extensionContext) {

        Network network = Network.SHARED;

        // MySQL
        mySQLContainer = new MySQLContainer<>("mysql:8.0.20") // using default database, username and password
                .withNetwork(network);
        mySQLContainer.setNetworkAliases(Collections.singletonList("mysql"));
        mySQLContainer.setPortBindings(Collections.singletonList("3306:3306"));
        mySQLContainer.start();

        // Zookeeper
        zookeeperContainer = new GenericContainer<>("confluentinc/cp-zookeeper:5.4.1")
                .withNetwork(network)
                .withNetworkAliases("zookeeper")
                .withEnv("ZOOKEEPER_CLIENT_PORT", "2181");
        zookeeperContainer.setPortBindings(Collections.singletonList("2181:2181"));
        zookeeperContainer.setWaitStrategy(Wait.forListeningPort().withStartupTimeout(Duration.ofMinutes(2)));
        zookeeperContainer.start();

        // Kafka
        kafkaContainer = new KafkaContainer("5.4.1")
                .withNetwork(network)
                .withExternalZookeeper("zookeeper:2181")
                .withExposedPorts(9092, 9093);
        kafkaContainer.setNetworkAliases(Collections.singletonList("kafka"));
        kafkaContainer.setPortBindings(Collections.singletonList("9092:9092"));
        kafkaContainer.start();

        // Schema Registry
        /*
        schemaRegistryContainer = new GenericContainer<>("confluentinc/cp-schema-registry:5.4.1")
                .withNetwork(network)
                .withNetworkAliases("schema-registry")
                .withEnv("SCHEMA_REGISTRY_KAFKASTORE_CONNECTION_URL", "zookeeper:2181")
                .withEnv("SCHEMA_REGISTRY_HOST_NAME", "schema-registry")
                .withEnv("SCHEMA_REGISTRY_LISTENERS", "http://0.0.0.0:8081")
                .withExposedPorts(8081);
        schemaRegistryContainer.setPortBindings(Collections.singletonList("8081:8081"));
        schemaRegistryContainer.setWaitStrategy(Wait.forListeningPort().withStartupTimeout(Duration.ofMinutes(2)));
        schemaRegistryContainer.start();
         */

        // Cassandra
        cassandraContainer = new CassandraContainer<>("cassandra:3.11.6")
                .withNetwork(network);
        cassandraContainer.setNetworkAliases(Collections.singletonList("cassandra"));
        cassandraContainer.setPortBindings(Collections.singletonList("9042:9042"));
        cassandraContainer.start();

        // event-service
        eventServiceContainer = new GenericContainer<>("docker.mycompany.com/event-service:1.0.0")
                .withNetwork(network)
                .withNetworkAliases("event-service")
                .withEnv("KAFKA_HOST", "kafka")
                .withEnv("KAFKA_PORT", "9092")
//                .withEnv("SCHEMA_REGISTRY_HOST", "schema-registry")
                .withEnv("CASSANDRA_HOST", "cassandra");
        eventServiceContainer.setPortBindings(Collections.singletonList("9081:8080"));
        eventServiceContainer.setWaitStrategy(Wait.forHttp("/actuator/health").forPort(8080).forStatusCode(200).withStartupTimeout(Duration.ofMinutes(2)));
        eventServiceContainer.start();
    }

    @Override
    public void afterAll(ExtensionContext extensionContext) {
        mySQLContainer.stop();
        zookeeperContainer.stop();
        kafkaContainer.stop();
//        schemaRegistryContainer.stop();
        cassandraContainer.stop();
        eventServiceContainer.stop();
    }

}
