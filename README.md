# `springboot-kafka-mysql-cassandra`

The goal of this project is to create a [`Spring Boot`](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/) application that handles `users` using [`Event Sourcing`](https://martinfowler.com/eaaDev/EventSourcing.html). So, besides the traditional create/update/delete, whenever a user is created, updated or deleted, an event informing this change is sent to [`Kafka`](https://kafka.apache.org). Furthermore, we will implement another `Spring Boot` application that listens to those events and saves them in [`Cassandra`](http://cassandra.apache.org).

> **Note:** In [`kubernetes-environment`](https://github.com/ivangfr/kubernetes-environment/tree/master/user-event-sourcing-monitoring) repository, it is shown how to deploy this project in `Kubernetes` (`Minikube`)

## Project Architecture

![project-diagram](images/project-diagram.png)

## Applications

- ### user-service

  `Spring Boot` Web Java application responsible for handling users. The user information is stored in [`MySQL`](https://www.mysql.com). Once a user is created, updated or deleted, an event is sent to `Kafka`.

  #### Serialization format

  `user-service` can use [`JSON`](https://www.json.org) or [`Avro`](https://avro.apache.org) format to serialize data to the `binary` format used by Kafka. If `Avro` format is chosen, both services will benefit by the [`Schema Registry`](https://docs.confluent.io/current/schema-registry/docs/index.html) that is running as Docker container. The serialization format to be used is defined by the value set to the environment variable `SPRING_PROFILES_ACTIVE`.
  
  | Configuration                    | Format |
  | -------------------------------- | ------ |
  | `SPRING_PROFILES_ACTIVE=default` | `JSON` |
  | `SPRING_PROFILES_ACTIVE=avro`    | `Avro` |

- ### event-service

  `Spring Boot` Web Java application responsible for listening events from `Kafka` and saving those events in `Cassandra`.

  #### Deserialization
  
  Differently from `user-service`, `event-service` has no specific Spring profile to select the deserialization format. [`Spring Cloud Stream`](https://docs.spring.io/spring-cloud-stream/docs/current/reference/htmlsingle) provides a stack of `MessageConverters` that handle the conversion of many different types of content-types, including `application/json`. Besides, as `event-service` has `SchemaRegistryClient` bean registered, `Spring Cloud Stream` auto configures an Apache Avro message converter for schema management.
    
  In order to handle different content-types, `Spring Cloud Stream` has a _"content-type negotiation and transformation"_ strategy (more [here](https://docs.spring.io/spring-cloud-stream/docs/current/reference/htmlsingle/#content-type-management)). The precedence orders are: first, content-type present in the message header; second, content-type defined in the binding; and finally, content-type is `application/json` (default).
    
  The producer (in the case `user-service`) always sets the content-type in the message header. The content-type can be `application/json` or `application/*+avro`, depending on with which `SPRING_PROFILES_ACTIVE` the `user-service` is started.
  
  #### Java classes from Avro Schema
  
  Run the following command in `springboot-kafka-mysql-cassandra` root folder. It will re-generate the Java classes from the Avro schema present at `event-service/src/main/resources/avro`.
  ```
  ./gradlew event-service:generateAvro
  ```

## Start Environment

- In a terminal and inside `springboot-kafka-mysql-cassandra` root folder run
  ```
  docker-compose up -d
  ```

- Wait a little bit until all containers are `Up (healthy)`. You can check by running the following command
  ```
  docker-compose ps
  ```

## Running Applications with Gradle

Inside `springboot-kafka-mysql-cassandra` root folder, run the following `Gradle` commands in different terminals

- **user-service**
  ```
  ./gradlew user-service:bootRun --args='--server.port=9080'
  ```
  > **Note:** In order to run `user-service` with `Avro` use
  > ```
  > ./gradlew user-service:bootRun --args='--server.port=9080 --spring.profiles.active=avro'
  > ```

- **event-service**
  ```
  ./gradlew event-service:bootRun --args='--server.port=9081'
  ```

## Running Applications as Docker containers

- Build Application's Docker Image

  In a terminal and inside `springboot-kafka-mysql-cassandra` root folder, run the following script to build the applications docker images 
  ```
  ./build-apps.sh
  ```

- Application's Environment Variables
   
  - **user-service**

    | Environment Variable   | Description                                                                          |
    | ---------------------- | ------------------------------------------------------------------------------------ |
    | `MYSQL_HOST`           | Specify host of the `MySQL` database to use (default `localhost`)                    |
    | `MYSQL_PORT`           | Specify port of the `MySQL` database to use (default `3306`)                         |
    | `KAFKA_HOST`           | Specify host of the `Kafka` message broker to use (default `localhost`)              |
    | `KAFKA_PORT`           | Specify port of the `Kafka` message broker to use (default `29092`)                  |
    | `SCHEMA_REGISTRY_HOST` | Specify host of the `Schema Registry` to use (default `localhost`)                   |
    | `SCHEMA_REGISTRY_PORT` | Specify port of the `Schema Registry` to use (default `8081`)                        |
    | `ZIPKIN_HOST`          | Specify host of the `Zipkin` distributed tracing system to use (default `localhost`) |
    | `ZIPKIN_PORT`          | Specify port of the `Zipkin` distributed tracing system to use (default `9411`)      |

  - **event-service**

    | Environment Variable   | Description                                                                          |
    | ---------------------- | ------------------------------------------------------------------------------------ |
    | `CASSANDRA_HOST`       | Specify host of the `Cassandra` database to use (default `localhost`)                |
    | `CASSANDRA_PORT`       | Specify port of the `Cassandra` database to use (default `9042`)                     |
    | `KAFKA_HOST`           | Specify host of the `Kafka` message broker to use (default `localhost`)              |
    | `KAFKA_PORT`           | Specify port of the `Kafka` message broker to use (default `29092`)                  |
    | `SCHEMA_REGISTRY_HOST` | Specify host of the `Schema Registry` to use (default `localhost`)                   |
    | `SCHEMA_REGISTRY_PORT` | Specify port of the `Schema Registry` to use (default `8081`)                        |
    | `ZIPKIN_HOST`          | Specify host of the `Zipkin` distributed tracing system to use (default `localhost`) |
    | `ZIPKIN_PORT`          | Specify port of the `Zipkin` distributed tracing system to use (default `9411`)      |

- Start Application's Docker Container

  In a terminal and inside `springboot-kafka-mysql-cassandra` root folder, run the following script to start the applications docker containers
  ```
  ./start-apps.sh
  ```
  > **Note:** In order to run `user-service` with `Avro` use
  > ```
  > ./start-apps.sh avro
  > ```

## Applications URLs

| Application   | URL                                   |
| ------------- | ------------------------------------- |
| user-service  | http://localhost:9080/swagger-ui.html |
| event-service | http://localhost:9081/swagger-ui.html |

## Playing around

1. Access `user-service` Swagger website http://localhost:9080/swagger-ui.html

   ![user-service](images/user-service.png)

1. Create a new user, `POST /api/users`

1. Access `event-service` Swagger website http://localhost:9081/swagger-ui.html

   ![event-service](images/event-service.png)

1. Get all events related to the user created, informing the user id `GET /api/events/users/{id}`

1. You can also check how the event was sent by `user-service` and listened by `event-service` (as shown on the image below) using [`Zipkin`](https://zipkin.io) http://localhost:9411

   ![zipkin](images/zipkin.png)

1. Create new users and update/delete existing ones in order to see how the application works.

## Shutdown

1. Stop applications
   - If they were started with `Gradle`, go to the terminals where they are running and press `Ctrl+C`
   - If they were started as a Docker container, run the script below
     ```
     ./stop-apps.sh
     ```

1. Stop and remove docker-compose containers, networks and volumes
   ```
   docker-compose down -v
   ```

## Running tests

- **event-service**
  ```
  ./gradlew event-service:cleanTest event-service:test
  ```

- **user-service**
  ```
  ./gradlew user-service:cleanTest user-service:test
  ```
  > **Note:** We are using [`Testcontainers`](https://www.testcontainers.org/) to run `user-service` integration tests. It starts automatically some Docker containers before the tests begin and shuts the containers down when the tests finish.

## Useful Commands & Links

- ### MySQL Database
  ```
  docker exec -it mysql mysql -uroot -psecret --database userdb
  select * from users;
  ```

- ### Cassandra Database
  ```
  docker exec -it cassandra cqlsh
  USE mycompany;
  SELECT * FROM user_events;
  ```

- ### Zipkin

  `Zipkin` can be accessed at http://localhost:9411

- ### Kafka Topics UI

  `Kafka Topics UI` can be accessed at http://localhost:8085

  ![kafka-topics-ui](images/kafka-topics-ui.png)

- ### Schema Registry UI

  `Schema Registry UI` can be accessed at http://localhost:8001

  ![schema-registry-ui](images/schema-registry-ui.png)

- ### Kafka Manager

  `Kafka Manager` can be accessed at http://localhost:9000

  **Configuration**

  - First, you must create a new cluster. Click on `Cluster` (dropdown button on the header) and then on `Add Cluster`
  - Type the name of your cluster in `Cluster Name` field, for example: `MyZooCluster`
  - Type `zookeeper:2181` in `Cluster Zookeeper Hosts` field
  - Enable checkbox `Poll consumer information (Not recommended for large # of consumers if ZK is used for offsets tracking on older Kafka versions)`
  - Click on `Save` button at the bottom of the page.

  The image below shows the topics present on Kafka, including the topic `com.mycompany.userservice.user` with `2`
partitions.

  ![kafka-manager](images/kafka-manager.png)

## Issues

Unable to upgrade to `Spring Boot` version `2.2.X`.

`Spring Cloud Stream` has changed the `Schema Registry` and the documentation is very poor so far.

The `user-service` was ok to change. However, `event-service` cannot deserialize the event. 

Next time to try, those are the changes to be done:

- FROM
  ```
  implementation 'org.springframework.cloud:spring-cloud-stream-schema'
  ```
  TO
  ```
  implementation 'org.springframework.cloud:spring-cloud-schema-registry-client'
  ```
  
- FROM
  ```
  cloud:
    stream:
      schema-registry-client:
        endpoint: http://${SCHEMA_REGISTRY_HOST:localhost}:${SCHEMA_REGISTRY_PORT:8081}
  ```
  TO
  ```
  cloud:
    schema-registry-client:
      enabled: true
      endpoint: http://${SCHEMA_REGISTRY_HOST:localhost}:${SCHEMA_REGISTRY_PORT:8081}
    stream:
  ```

- FROM
  ```
  cloud:
    stream:
      schema:
        avro:
          schema-locations:
            - classpath:avro/userevent-message.avsc
  ```
  TO
  ```
  cloud:
    schema:
      avro:
        schema-locations:
          - classpath:avro/userevent-message.avsc
    stream:
  ```
  
- FROM
  ```
  SchemaRegistryClient schemaRegistryClient(@Value("${spring.cloud.stream.schema-registry-client.endpoint}") String endpoint) {
  ```
  TO
  ```
  SchemaRegistryClient schemaRegistryClient(@Value("${spring.cloud.schema-registry-client.endpoint}") String endpoint) {
  ```

## References

- https://docs.spring.io/spring-cloud-stream/docs/current/reference/htmlsingle/
- https://docs.docker.com/reference/
- https://docs.docker.com/compose/compose-file/compose-versioning/
