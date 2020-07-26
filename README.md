# spring-cloud-stream-event-sourcing-testcontainers

The goal of this project is to create a [`Spring Boot`](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/) application that handles `users` using [`Event Sourcing`](https://martinfowler.com/eaaDev/EventSourcing.html). So, besides the traditional create/update/delete, whenever a user is created, updated or deleted, an event informing this change is sent to [`Kafka`](https://kafka.apache.org). Furthermore, we will implement another `Spring Boot` application that listens to those events and saves them in [`Cassandra`](http://cassandra.apache.org). Finally, we will use [`Testcontainers`](https://www.testcontainers.org/) to run the integration tests of all project.

> **Note:** In [`kubernetes-environment`](https://github.com/ivangfr/kubernetes-environment/tree/master/user-event-sourcing-monitoring) repository, it is shown how to deploy this project in `Kubernetes` (`Minikube`)

## Project Architecture

![project-diagram](images/project-diagram.png)

## Applications

- ### user-service

  `Spring Boot` Web Java application responsible for handling users. The user information is stored in [`MySQL`](https://www.mysql.com). Once a user is created, updated or deleted, an event is sent to `Kafka`.

  - **Serialization format**

    `user-service` can use [`JSON`](https://www.json.org) or [`Avro`](https://avro.apache.org) format to serialize data to the `binary` format used by Kafka. If `Avro` format is chosen, both services will benefit by the [`Schema Registry`](https://docs.confluent.io/current/schema-registry/docs/index.html) that is running as Docker container. The serialization format to be used is defined by the value set to the environment variable `SPRING_PROFILES_ACTIVE`.
  
    | Configuration                    | Format |
    | -------------------------------- | ------ |
    | `SPRING_PROFILES_ACTIVE=default` | `JSON` |
    | `SPRING_PROFILES_ACTIVE=avro`    | `Avro` |

- ### event-service

  `Spring Boot` Web Java application responsible for listening events from `Kafka` and saving those events in `Cassandra`.

  - **Deserialization**
  
    Differently from `user-service`, `event-service` has no specific Spring profile to select the deserialization format. [`Spring Cloud Stream`](https://docs.spring.io/spring-cloud-stream/docs/current/reference/htmlsingle) provides a stack of `MessageConverters` that handle the conversion of many types of content-types, including `application/json`. Besides, as `event-service` has `SchemaRegistryClient` bean registered, `Spring Cloud Stream` auto configures an Apache Avro message converter for schema management.
    
    In order to handle different content-types, `Spring Cloud Stream` has a _"content-type negotiation and transformation"_ strategy (more [here](https://docs.spring.io/spring-cloud-stream/docs/current/reference/htmlsingle/#content-type-management)). The precedence orders are: first, content-type present in the message header; second, content-type defined in the binding; and finally, content-type is `application/json` (default).
    
    The producer (in the case `user-service`) always sets the content-type in the message header. The content-type can be `application/json` or `application/*+avro`, depending on with which `SPRING_PROFILES_ACTIVE` the `user-service` is started.
  
  - **Java classes from Avro Schema**
  
    Run the following command in `spring-cloud-stream-event-sourcing-testcontainers` root folder. It will re-generate the Java classes from the Avro schema present at `event-service/src/main/resources/avro`.
    ```
    ./gradlew event-service:generateAvro
    ```
  
## Prerequisites

- [`Java 11+`](https://www.oracle.com/java/technologies/javase-jdk11-downloads.html)
- [`Docker`](https://www.docker.com/)
- [`Docker-Compose`](https://docs.docker.com/compose/install/)

## Start Environment

- In a terminal and inside `spring-cloud-stream-event-sourcing-testcontainers` root folder run
  ```
  docker-compose up -d
  ```

- Wait a bit until all containers are `Up (healthy)`. You can check by running the following command
  ```
  docker-compose ps
  ```

## Running Applications with Gradle

Inside `spring-cloud-stream-event-sourcing-testcontainers` root folder, run the following `Gradle` commands in different terminals.

> **Note:** start `user-service` first, so it created the `com.mycompany.userservice.user` partitioned.

- **user-service**
  
  - Using `JSON`
    ```
    ./gradlew user-service:clean user-service:bootRun --args='--server.port=9080'
    ```
  
  - Using `Avro`
    ```
    ./gradlew user-service:clean user-service:bootRun --args='--server.port=9080 --spring.profiles.active=avro'
    ```

- **event-service**
  ```
  ./gradlew event-service:clean event-service:bootRun --args='--server.port=9081'
  ```

## Running Applications as Docker containers

### Build Application's Docker Image

- In a terminal, make sure you are inside `spring-cloud-stream-event-sourcing-testcontainers` root folder

- Run the following script to build the application's docker images 
  ```
  ./build-apps.sh
  ```

### Application's Environment Variables
   
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

### Start Application's Docker Container

- In a terminal, make sure you are inside `spring-cloud-stream-event-sourcing-testcontainers` root folder

- In order to run the application's docker containers, you can pick between `JSON` or `Avro`

  - Using `JSON`
    ```
    ./start-apps.sh
    ```
    
  - Using `Avro`
    ```
    ./start-apps.sh avro
    ```

## Applications URLs

| Application   | URL                                   |
| ------------- | ------------------------------------- |
| user-service  | http://localhost:9080/swagger-ui.html |
| event-service | http://localhost:9081/swagger-ui.html |

## Playing around

1. Access `user-service` Swagger website http://localhost:9080/swagger-ui.html

   ![user-service](images/user-service-swagger.png)

1. Create a new user, `POST /api/users`

1. Access `event-service` Swagger website http://localhost:9081/swagger-ui.html

   ![event-service](images/event-service-swagger.png)

1. Get all events related to the user created, informing the user id `GET /api/events/users/{id}`

1. You can also check how the event was sent by `user-service` and listened by `event-service` (as shown on the image below) using [`Zipkin`](https://zipkin.io) http://localhost:9411

   ![zipkin](images/zipkin.png)

1. Create new users and update/delete existing ones in order to see how the application works.

## Useful Commands & Links

- **MySQL**
  ```
  docker exec -it mysql mysql -uroot -psecret --database userdb
  select * from users;
  ```

- **Cassandra**
  ```
  docker exec -it cassandra cqlsh
  USE mycompany;
  SELECT * FROM user_events;
  ```

- **Zipkin**

  `Zipkin` can be accessed at http://localhost:9411

- **Kafka Topics UI**

  `Kafka Topics UI` can be accessed at http://localhost:8085

  ![kafka-topics-ui](images/kafka-topics-ui.png)

- **Schema Registry UI**

  `Schema Registry UI` can be accessed at http://localhost:8001

  ![schema-registry-ui](images/schema-registry-ui.png)

- **Kafka Manager**

  `Kafka Manager` can be accessed at http://localhost:9000

  _Configuration_

  - First, you must create a new cluster. Click on `Cluster` (dropdown button on the header) and then on `Add Cluster`
  - Type the name of your cluster in `Cluster Name` field, for example: `MyCluster`
  - Type `zookeeper:2181` in `Cluster Zookeeper Hosts` field
  - Enable checkbox `Poll consumer information (Not recommended for large # of consumers if ZK is used for offsets tracking on older Kafka versions)`
  - Click on `Save` button at the bottom of the page.

  The image below shows the topics present in Kafka, including the topic `com.mycompany.userservice.user` with `2`
partitions.

  ![kafka-manager](images/kafka-manager.png)

## Shutdown

- Stop applications
  - If they were started with `Gradle`, go to the terminals where they are running and press `Ctrl+C`
  - If they were started as a Docker container, run the script below
    ```
    ./stop-apps.sh
    ```

- To stop and remove docker-compose containers, networks and volumes, make sure you are inside `spring-cloud-stream-event-sourcing-testcontainers` root folder and run
  ```
  docker-compose down -v
  ```

## Running tests

- **event-service**

  - Run the command below to start the tests
    ```
    ./gradlew event-service:clean event-service:cleanTest event-service:test
    ```

- **user-service**

  - During integration tests, [`Testcontainers`](https://www.testcontainers.org/) will start automatically `Zookeeper`, `Kafka`, `MySQL`, `Cassandra` and `event-service` containers before the tests begin and shuts them down when the tests finish.
    > **Note:** Make sure you have an updated `event-service` docker image

  - Run the command below to start the tests
    - Using `JSON`
      ```
      ./gradlew user-service:clean user-service:cleanTest user-service:test
      ```
    - Using `Avro`
      > **Warning:** app starts with correct profile. However, messages are sent in json format.
      ```
      SPRING_PROFILES_ACTIVE=avro ./gradlew user-service:clean user-service:cleanTest user-service:test
      ```

## Issues

- Disable some `UserEventRepositoryTest` test case because I was not able to make `org.cassandraunit:cassandra-unit-spring` to work in `event-service` since I updated to `springboot` version `2.3.1`
    
## References

- https://docs.spring.io/spring-cloud-stream/docs/current/reference/htmlsingle/
