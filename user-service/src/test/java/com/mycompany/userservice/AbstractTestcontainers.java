package com.mycompany.userservice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.CassandraContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;

@Slf4j
@Testcontainers
public abstract class AbstractTestcontainers {

    private static final MySQLContainer<?> mySQLContainer = new MySQLContainer<>("mysql:5.7.35");
    private static final GenericContainer<?> zookeeperContainer = new GenericContainer<>("confluentinc/cp-zookeeper:6.1.1");
    private static final KafkaContainer kafkaContainer = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:6.1.1"));
    private static final GenericContainer<?> schemaRegistryContainer = new GenericContainer<>("confluentinc/cp-schema-registry:6.1.1");
    private static final CassandraContainer<?> cassandraContainer = new CassandraContainer<>("cassandra:3.11.11");
    private static final GenericContainer<?> eventServiceContainer = new GenericContainer<>("ivanfranchin/event-service:1.0.0");

    protected static String EVENT_SERVICE_API_URL;
    private static final int EVENT_SERVICE_EXPOSED_PORT = 9081;

    public static final Duration STARTUP_TIMEOUT = Duration.ofMinutes(2);

    @DynamicPropertySource
    private static void dynamicProperties(DynamicPropertyRegistry registry) {
        Network network = Network.SHARED;

        // MySQL
        mySQLContainer.withNetwork(network).withNetworkAliases("mysql")
                .withUrlParam("characterEncoding", "UTF-8")
                .withUrlParam("serverTimezone", "UTC");
        mySQLContainer.start();

        // Zookeeper
        zookeeperContainer.withNetwork(network).withNetworkAliases("zookeeper")
                .withEnv("ZOOKEEPER_CLIENT_PORT", "2181")
                .withExposedPorts(2181);
        zookeeperContainer.setWaitStrategy(Wait.forListeningPort().withStartupTimeout(STARTUP_TIMEOUT));
        zookeeperContainer.start();

        // Kafka
        kafkaContainer.withNetwork(network).withNetworkAliases("kafka")
                .withExternalZookeeper("zookeeper:2181")
                .withExposedPorts(9092, 9093);
        kafkaContainer.start();

        // Schema Registry
        schemaRegistryContainer.withNetwork(network).withNetworkAliases("schema-registry")
                .withEnv("SCHEMA_REGISTRY_KAFKASTORE_BOOTSTRAP_SERVERS", "kafka:9092")
                .withEnv("SCHEMA_REGISTRY_HOST_NAME", "schema-registry")
                .withEnv("SCHEMA_REGISTRY_LISTENERS", "http://0.0.0.0:8081")
                .withExposedPorts(8081);
        schemaRegistryContainer.setWaitStrategy(Wait.forListeningPort().withStartupTimeout(STARTUP_TIMEOUT));
        schemaRegistryContainer.start();

        // Cassandra
        cassandraContainer.withNetwork(network).withNetworkAliases("cassandra");
        cassandraContainer.start();

        // event-service
        eventServiceContainer.withNetwork(network).withNetworkAliases("event-service")
                .withEnv("KAFKA_HOST", "kafka")
                .withEnv("KAFKA_PORT", "9092")
                .withEnv("SCHEMA_REGISTRY_HOST", "schema-registry")
                .withEnv("CASSANDRA_HOST", "cassandra")
                .withEnv("SPRING_ZIPKIN_ENABLED", "false")
                .withExposedPorts(EVENT_SERVICE_EXPOSED_PORT);
        eventServiceContainer.setWaitStrategy(Wait.forHttp("/actuator/health")
                .forPort(EVENT_SERVICE_EXPOSED_PORT).forStatusCode(200).withStartupTimeout(STARTUP_TIMEOUT));
        eventServiceContainer.start();

        registry.add("spring.datasource.url", mySQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", mySQLContainer::getUsername);
        registry.add("spring.datasource.password", mySQLContainer::getPassword);
        registry.add("spring.jpa.properties.hibernate.dialect.storage_engine", () -> "innodb");

        String schemaRegistryUrl = String.format("http://localhost:%s", schemaRegistryContainer.getMappedPort(8081));
        registry.add("spring.cloud.schema-registry-client.endpoint", () -> schemaRegistryUrl);
        registry.add("spring.cloud.stream.kafka.binder.brokers", kafkaContainer::getBootstrapServers);

        EVENT_SERVICE_API_URL = String.format("http://localhost:%s/api", eventServiceContainer.getMappedPort(EVENT_SERVICE_EXPOSED_PORT));
    }
}
