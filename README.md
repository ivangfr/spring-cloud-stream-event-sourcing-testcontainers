# springboot-kafka-mysql-cassandra

## Goal

The goals of this project are to implement three modules:

*user-service*: responsible for handling users (create/update/delete). User information is stored in `MySQL` database. Once one user is created/updated/deleted, one event is sent on `Kafka` bus;
*event-service*: responsible for listening events on Kafka bus and saving those events on `Cassandra` database;
*commons*: responsible for generating the common `Avro` user-event that is used by `user-service` and `event-service`. 

## Start Environment

### Docker compose

Export your machine ip address to `HOST_IP_ADDR` environment variable
```
export HOST_IP_ADDR=[machine ip address]
```

Inside `/springboot-kafka-mysql-cassandra/dev` folder run
```
docker-compose up
```

### Build services

Inside `/springboot-kafka-mysql-cassandra/dev` folder run
```
gradle build
```

## Start user-service

Inside `/springboot-kafka-mysql-cassandra/user-service` folder run
```
java -jar build/libs/user-service-0.0.1-SNAPSHOT.jar
```
OR
```
gradle bootRun
```

## Start event-service

Inside `/springboot-kafka-mysql-cassandra/event-service` folder run
```
java -jar build/libs/event-service-0.0.1-SNAPSHOT.jar
```
OR
```
gradle bootRun
```

## How to use

1. Open `user-service` swagger link
```
http://localhost:8081/swagger-ui.html
```

2. Create a new user (`POST /api/users`)

3. Open `event-service` swagger link
```
http://localhost:8082/swagger-ui.html
```

4. Get user-events related to the used created (`GET /api/events/users/{id}`) informing the user id;

5. You can also check how the event was sent by `user-service` and listened by `event-service` using `Zipkin` (`http://localhost:9411/`)

6. Create new users and update/delete existing ones in order to see how the application works. 
