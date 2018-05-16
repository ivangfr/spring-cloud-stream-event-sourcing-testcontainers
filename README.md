# springboot-kafka-mysql-cassandra

## Goal

The goals of this project are to implement three modules:
- `user-service`: responsible for handling users (create/update/delete). User information is stored in `MySQL` database. Once one user is created/updated/deleted, one event is sent on `Kafka` bus;
- `event-service`: responsible for listening events on Kafka bus and saving those events on `Cassandra` database;
- `commons`: responsible for generating the common `Avro` user-event that is used by `user-service` and `event-service`. 

## Start Environment

### Docker compose

- Open a terminal
- Inside `/springboot-kafka-mysql-cassandra/dev` folder run
```
docker-compose up -d
```
> To stop and remove containers, networks, images, and volumes type:
> ```
> docker-compose down
> ```

## Start user-service

- Open a new terminal
- Inside `/springboot-kafka-mysql-cassandra` root folder run
```
gradle user-service:bootRun
```

## Start event-service

- Open a new terminal
- Inside `/springboot-kafka-mysql-cassandra` root folder run
```
gradle event-service:bootRun
```

## How to use

1. Open `user-service` swagger link: [http://localhost:8081/swagger-ui.html](http://localhost:8081/swagger-ui.html)

2. Create a new user (`POST /api/users`)

3. Open `event-service` swagger link: [http://localhost:8082/swagger-ui.html](http://localhost:8082/swagger-ui.html)

4. Get user-events related to the used created (`GET /api/events/users/{id}`) informing the user id;

5. You can also check how the event was sent by `user-service` and listened by `event-service` using `Zipkin` [http://localhost:9411](http://localhost:9411)

6. Create new users and update/delete existing ones in order to see how the application works. 
