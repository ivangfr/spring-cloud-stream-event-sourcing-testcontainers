# springboot-kafka-mysql-cassandra

## Goal

In this project, we are going to implement three modules:
- `user-service`: service responsible for handling users (create/update/delete). The users information will be stored in [`MySQL`](https://www.mysql.com) database.
Once one user is created/updated/deleted, one event is sent on a [`Kafka`](https://kafka.apache.org) bus;
- `event-service`: service responsible for listening events on `Kafka` bus and saving those events on [`Cassandra`](http://cassandra.apache.org) database;
- `commons`: module responsible for generating the common [`Avro`](https://avro.apache.org) user-event that is used by `user-service` and `event-service`. 

## Start Environment

### Docker compose

- Open a terminal

- Inside `/springboot-kafka-mysql-cassandra` root folder run
```
docker-compose up -d
```
> To stop and remove containers, networks, images, and volumes type:
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
event-cassandra   docker-entrypoint.sh cassa ...   Up (healthy)   7000/tcp, 7001/tcp, 0.0.0.0:7199->7199/tcp...
kafka             /etc/confluent/docker/run        Up (healthy)   0.0.0.0:9092->9092/tcp
user-mysql        docker-entrypoint.sh mysqld      Up (healthy)   0.0.0.0:3306->3306/tcp
zipkin            /bin/sh -c test -n "$STORA ...   Up (healthy)   9410/tcp, 0.0.0.0:9411->9411/tcp
zookeeper         /etc/confluent/docker/run        Up (healthy)   0.0.0.0:2181->2181/tcp, 2888/tcp, 3888/tcp
```

## Generate UserEventBus

Inside `/springboot-kafka-mysql-cassandra` root folder run
```
gradle clean commons:install
```
It will install `commons-0.0.1-SNAPSHOT.jar` in you local maven repository, so that it can be visible by user-service and event-service.

## Start user-service

- Open a new terminal

- Inside `/springboot-kafka-mysql-cassandra` root folder run
```
gradle clean user-service:bootRun
```

## Start event-service

- Open a new terminal

- Inside `/springboot-kafka-mysql-cassandra` root folder run
```
gradle clean event-service:bootRun
```

## How to test

1. Open `user-service` swagger link: http://localhost:8081/swagger-ui.html

2. Create a new user, `POST /api/users`

3. Open `event-service` swagger link: http://localhost:8082/swagger-ui.html

4. Get user-events related to the used created `GET /api/events/users/{id}` informing the user id;

5. You can also check how the event was sent by `user-service` and listened by `event-service` using [`Zipkin`](https://zipkin.io): http://localhost:9411

6. Create new users and update/delete existing ones in order to see how the application works. 
