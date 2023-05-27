package com.ivanfranchin.endtoendtest;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
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
import java.util.List;

@Testcontainers
public abstract class AbstractTestcontainers {

    private static final MySQLContainer<?> mySQLContainer = new MySQLContainer<>("mysql:5.7.42");
    private static final CassandraContainer<?> cassandraContainer = new CassandraContainer<>("cassandra:4.1.1");
    private static final KafkaContainer kafkaContainer = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.3.1"));
    private static final GenericContainer<?> schemaRegistryContainer = new GenericContainer<>("confluentinc/cp-schema-registry:7.3.1");
    private static final GenericContainer<?> userServiceContainer = new GenericContainer<>("ivanfranchin/user-service:1.0.0");
    private static final GenericContainer<?> eventServiceContainer = new GenericContainer<>("ivanfranchin/event-service:1.0.0");

    protected static String USER_SERVICE_API_URL;
    private static final int USER_SERVICE_EXPOSED_PORT = 9080;

    protected static String EVENT_SERVICE_API_URL;
    private static final int EVENT_SERVICE_EXPOSED_PORT = 9081;

    public static final Duration STARTUP_TIMEOUT = Duration.ofMinutes(2);

    @DynamicPropertySource
    private static void dynamicProperties(DynamicPropertyRegistry registry) {
        Network network = Network.SHARED;

        // MySQL
        mySQLContainer.withNetwork(network)
                .withNetworkAliases("mysql")
                .withUrlParam("characterEncoding", "UTF-8")
                .withUrlParam("serverTimezone", "UTC")
                .withPassword("secret")
                .withDatabaseName("userdb")
                .start();

        // Kafka
        kafkaContainer.withNetwork(network)
                .withNetworkAliases("kafka")
                .withExposedPorts(9092, 9093)
                .start();

        // Schema Registry
        schemaRegistryContainer.withNetwork(network)
                .withNetworkAliases("schema-registry")
                .withEnv("SCHEMA_REGISTRY_KAFKASTORE_BOOTSTRAP_SERVERS", "kafka:9092")
                .withEnv("SCHEMA_REGISTRY_HOST_NAME", "schema-registry")
                .withEnv("SCHEMA_REGISTRY_LISTENERS", "http://0.0.0.0:8081")
                .withExposedPorts(8081)
                .waitingFor(Wait.forListeningPort().withStartupTimeout(STARTUP_TIMEOUT))
                .start();

        // Cassandra
        cassandraContainer.withNetwork(network)
                .withNetworkAliases("cassandra")
                .start();

        // user-service
        userServiceContainer.withNetwork(network)
                .withNetworkAliases("user-service")
                .withEnv("SPRING_PROFILES_ACTIVE", hasAvroAsProfilesActive() ? "test,avro" : "test")
                .withEnv("KAFKA_HOST", "kafka")
                .withEnv("KAFKA_PORT", "9092")
                .withEnv("SCHEMA_REGISTRY_HOST", "schema-registry")
                .withEnv("MYSQL_HOST", "mysql")
                .withEnv("MANAGEMENT_TRACING_ENABLED", "false")
                .withExposedPorts(USER_SERVICE_EXPOSED_PORT)
                .waitingFor(Wait.forHttp("/actuator/health")
                        .forPort(USER_SERVICE_EXPOSED_PORT).forStatusCode(200).withStartupTimeout(STARTUP_TIMEOUT))
                .start();

        // event-service
        eventServiceContainer.withNetwork(network)
                .withNetworkAliases("event-service")
                .withEnv("KAFKA_HOST", "kafka")
                .withEnv("KAFKA_PORT", "9092")
                .withEnv("SCHEMA_REGISTRY_HOST", "schema-registry")
                .withEnv("CASSANDRA_HOST", "cassandra")
                .withEnv("MANAGEMENT_TRACING_ENABLED", "false")
                .withExposedPorts(EVENT_SERVICE_EXPOSED_PORT)
                .waitingFor(Wait.forHttp("/actuator/health")
                        .forPort(EVENT_SERVICE_EXPOSED_PORT).forStatusCode(200).withStartupTimeout(STARTUP_TIMEOUT))
                .start();

        USER_SERVICE_API_URL = String.format("http://localhost:%s/api", userServiceContainer.getMappedPort(USER_SERVICE_EXPOSED_PORT));
        EVENT_SERVICE_API_URL = String.format("http://localhost:%s/api", eventServiceContainer.getMappedPort(EVENT_SERVICE_EXPOSED_PORT));
    }

    private static boolean hasAvroAsProfilesActive() {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        return List.of(context.getEnvironment().getActiveProfiles()).contains("avro");
    }
}
