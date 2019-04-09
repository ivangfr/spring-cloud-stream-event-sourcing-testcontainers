# `springboot-kafka-mysql-cassandra`

The goal of this project is to create a service that handles _users_ using
[`Event Sourcing`](https://martinfowler.com/eaaDev/EventSourcing.html). So, besides the traditional create/update/delete,
whenever an user is created/updated/deleted, an event (informing this change) is sent to [`Kafka`](https://kafka.apache.org).
Furthermore, we will implement a service that will listen for those events and save them in [`Cassandra`](http://cassandra.apache.org).

# Microservices

![project-diagram](images/project-diagram.png)

## user-service

Spring-boot application responsible for handling users (create/update/delete). The users information
will be stored in [`MySQL`](https://www.mysql.com). Once an user is created/updated/deleted, one event is sent
to `Kafka`.

### Serialization format 

`user-service` can use [`JSON`](https://www.json.org) or [`Avro`](https://avro.apache.org) format to serialize
data to the `binary` format used by Kafka. If `Avro` format is chosen, both services will benefit by the
[`Schema Registry`](https://docs.confluent.io/current/schema-registry/docs/index.html) that is running as Docker
container. The serialization format used is defined by value set to the environment variable `SPRING_PROFILES_ACTIVE`,
present in the `user-service` section in `docker-compose.yml`.

| Configuration                    | Format |
| -------------------------------- | ------ |
| `SPRING_PROFILES_ACTIVE=default` | `JSON` |
| `SPRING_PROFILES_ACTIVE=avro`    | `Avro` |

## event-service

Spring-boot application responsible for listening events from `Kafka` and saving those events in `Cassandra`.

### Deserialization

Differently from `user-service`, `event-service` has no specific spring profile to select the deserialization format.
Spring Cloud Stream provides a stack of `MessageConverters` that handle the conversion of many different types of
content-types, including `application/json`. Besides, as `event-service` has `SchemaRegistryClient` bean registered,
Spring Cloud Stream auto configures an Apache Avro message converter for schema management.

In order to handle different content-types, Spring Cloud Stream  has a "content-type negotiation and transformation"
strategy (https://docs.spring.io/spring-cloud-stream/docs/current/reference/htmlsingle/#content-type-management). The
precedence orders are: 1st, content-type present in the message header; 2nd content-type defined in the binding;
and finally, content-type is `application/json` (default).

The producer (in the case `user-service`) always sets the content-type in the message header. The content-type can be
`application/json` or `application/*+avro`, depending on with which `SPRING_PROFILES_ACTIVE` `user-service` is started.

### Java classes from Avro Schema

The following command in `springboot-kafka-mysql-cassandra` root folder. It will re-generate the Java classes from the
Avro schema present at `src/main/resources/avro`.
```
./gradlew event-service:generateAvro
```

# Start Environment

- Open a terminal and go to `springboot-kafka-mysql-cassandra` root folder

- Build `user-service` docker image
```
./gradlew user-service:docker -x test
```

- Build `event-service` docker image
```
./gradlew event-service:docker -x test
```

- In order to run `user-service` with `Avro` format serialization, export the following environment variable.
If `JSON` is preferred, skip this step.
```
export USER_SERVICE_SPRING_PROFILES_ACTIVE=avro
```

- Start docker-compose
```
docker-compose up -d
```
> To stop and remove containers, networks and volumes type:
> ```
> docker-compose down -v
> ```

- Wait a little bit until all containers are `Up (healthy)`. You can check their status running
```
docker-compose ps
```
The output will be something like
```
Name              Command                          State          Ports
---------------------------------------------------------------------------------------------------------------
cassandra                  docker-entrypoint.sh cassa ...   Up (healthy)   7000/tcp, 7001/tcp, 0.0.0.0:7199->7199/tcp...
event-service              java -jar /event-service.jar     Up (healthy)   0.0.0.0:9081->9081/tcp
kafka                      /etc/confluent/docker/run        Up (healthy)   0.0.0.0:29092->29092/tcp, 9092/tcp
kafka-manager              /kafka-manager/bin/kafka-m ...   Up             0.0.0.0:9000->9000/tcp
kafka-rest-proxy           /etc/confluent/docker/run        Up (healthy)   0.0.0.0:8082->8082/tcp
kafka-schema-registry-ui   /run.sh                          Up (healthy)   0.0.0.0:8001->8000/tcp
kafka-topics-ui            /run.sh                          Up (healthy)   0.0.0.0:8085->8000/tcp
mysql                      docker-entrypoint.sh mysqld      Up (healthy)   0.0.0.0:3306->3306/tcp, 33060/tcp
schema-registry            /etc/confluent/docker/run        Up (healthy)   0.0.0.0:8081->8081/tcp
user-service               java -jar /user-service.jar      Up (healthy)   0.0.0.0:9080->9080/tcp
zipkin                     /bin/bash -c test -n "$STO ...   Up (healthy)   9410/tcp, 0.0.0.0:9411->9411/tcp
zookeeper                  /etc/confluent/docker/run        Up (healthy)   0.0.0.0:2181->2181/tcp, 2888/tcp, 3888/tcp
```

# Running microservices with Gradle

During development, it is better to just run the microservices with Gradle instead of always build the docker images and
run it. In order to do that, comment the `user-service` and/or `event-service` in `docker-compose.yml` file and run the
microservice(s) with Gradle Wrapper.

### user-service
```
export KAFKA_PORT=29092
./gradlew user-service:bootRun -Dserver.port=9080
```

### event-service
```
export KAFKA_PORT=29092
./gradlew event-service:bootRun -Dserver.port=9081
```

# Playing around with the microservices

- Open `user-service` swagger link: http://localhost:9080/swagger-ui.html

![user-service](images/user-service.png)

- Create a new user, `POST /api/users`

- Open `event-service` swagger link: http://localhost:9081/swagger-ui.html

![event-service](images/event-service.png)

- Get all events related to the user created, informing the user id `GET /api/events/users/{id}`

- You can also check how the event was sent by `user-service` and listened by `event-service` (as shown on the image
below) using [`Zipkin`](https://zipkin.io): http://localhost:9411

![zipkin](images/zipkin.png)

- Create new users and update/delete existing ones in order to see how the application works.

# Running tests

- To run `event-service` test cases
```
./gradlew event-service:test
```

- To run `user-service` test cases
```
./gradlew user-service:test
```
> Note: We are using [`Testcontainers`](https://www.testcontainers.org/) to run `user-service` integration tests.  It
starts automatically some Docker containers before the tests begin and shuts the containers down when the tests finish.

# Useful Commands & Links

### MySQL Database
```
docker exec -it mysql bash -c 'mysql -uroot -psecret'
use userdb;
select * from users;
```

### Cassandra Database
```
docker exec -it cassandra cqlsh
USE mycompany;
SELECT * FROM user_events;
```

### Kafka Topics UI

Kafka Topics UI can be accessed at http://localhost:8085

![kafka-topics-ui](images/kafka-topics-ui.png)

### Schema Registry UI

Schema Registry UI can be accessed at http://localhost:8001

![schema-registry-ui](images/schema-registry-ui.png)

### Kafka Manager

- Kafka Manager can be accessed at http://localhost:9000
- First, you must create a new cluster. Click on `Cluster` (dropdown on the header) and then on `Add Cluster`
- Type on `Cluster Name` field the name of your cluster, for example: `MyZooCluster`
- On `Cluster Zookeeper Hosts` field type: `zookeeper:2181`
- Click on `Save` button on the bottom of the page. Done!
- The image below shows the topics present on Kafka, including the topic `com.mycompany.userservice.user` with `2`
partitions, that is used by the microservices of this project.

![kafka-manager](images/kafka-manager.png)

# References

- https://docs.spring.io/spring-cloud-stream/docs/current/reference/htmlsingle/
- https://docs.docker.com/reference/
- https://docs.docker.com/compose/compose-file/compose-versioning/
