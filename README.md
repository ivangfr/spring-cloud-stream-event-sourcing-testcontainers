# spring-cloud-stream-event-sourcing-testcontainers

The goal of this project is to create a [`Spring Boot`](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/) application that handles `users` using [`Event Sourcing`](https://martinfowler.com/eaaDev/EventSourcing.html). So, besides the traditional create/update/delete, whenever a user is created, updated or deleted, an event informing this change is sent to [`Kafka`](https://kafka.apache.org). Furthermore, we will implement another `Spring Boot` application that listens to those events and saves them in [`Cassandra`](https://cassandra.apache.org). Finally, we will use [`Testcontainers`](https://www.testcontainers.org) to run the integration tests.

> **Note:** In [`kubernetes-minikube-environment`](https://github.com/ivangfr/kubernetes-minikube-environment/tree/master/user-event-sourcing-kafka) repository, it's shown how to deploy this project in `Kubernetes` (`Minikube`)

## Project Architecture

![project-diagram](images/project-diagram.png)

## Applications

- ### user-service

  `Spring Boot` Web Java application responsible for handling users. The user information is stored in [`MySQL`](https://www.mysql.com). Once a user is created, updated or deleted, an event is sent to `Kafka`.
  
  ![user-service](images/user-service-swagger.png)

  - **Serialization format**

    `user-service` can use [`JSON`](https://www.json.org) or [`Avro`](https://avro.apache.org) format to serialize data to the `binary` format used by `Kafka`. If we choose `Avro`, both services will benefit by the [`Schema Registry`](https://docs.confluent.io/current/schema-registry/docs/index.html) that is running as Docker container. The serialization format to be used is defined by the value set to the environment variable `SPRING_PROFILES_ACTIVE`.
  
    | Configuration                    | Format |
    | -------------------------------- | ------ |
    | `SPRING_PROFILES_ACTIVE=default` | `JSON` |
    | `SPRING_PROFILES_ACTIVE=avro`    | `Avro` |

- ### event-service

  `Spring Boot` Web Java application responsible for listening events from `Kafka` and saving them in `Cassandra`.

  ![event-service](images/event-service-swagger.png)

  - **Deserialization**
  
    Differently from `user-service`, `event-service` has no specific Spring profile to select the deserialization format. [`Spring Cloud Stream`](https://docs.spring.io/spring-cloud-stream/docs/current/reference/htmlsingle) provides a stack of `MessageConverters` that handle the conversion of many types of content-types, including `application/json`. Besides, as `event-service` has `SchemaRegistryClient` bean registered, `Spring Cloud Stream` auto configures an Apache Avro message converter for schema management.
    
    In order to handle different content-types, `Spring Cloud Stream` has a _"content-type negotiation and transformation"_ strategy (more [here](https://docs.spring.io/spring-cloud-stream/docs/current/reference/htmlsingle/#content-type-management)). The precedence orders are: first, content-type present in the message header; second, content-type defined in the binding; and finally, content-type is `application/json` (default).
    
    The producer (in the case `user-service`) always sets the content-type in the message header. The content-type can be `application/json` or `application/*+avro`, depending on with which `SPRING_PROFILES_ACTIVE` the `user-service` is started.
  
  - **Java classes from Avro Schema**
  
    Run the following command in `spring-cloud-stream-event-sourcing-testcontainers` root folder. It will re-generate the Java classes from the Avro schema present at `event-service/src/main/resources/avro`.
    ```
    ./mvnw compile --projects event-service
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

- Wait until all containers are `Up (healthy)`. You can check by running the following command
  ```
  docker-compose ps
  ```

## Running Applications with Maven

- **user-service**

  - In a terminal, make sure you are inside `spring-cloud-stream-event-sourcing-testcontainers` root folder
  
  - In order to run the application, you can pick between `JSON` or `Avro`
    - Using `JSON`
      ```
      ./mvnw clean spring-boot:run --projects user-service
      ```
    - Using `Avro`
      ```
      ./mvnw clean spring-boot:run --projects user-service -Dspring-boot.run.profiles=avro
      ```

- **event-service**

  - In a new terminal, make sure you are inside `spring-cloud-stream-event-sourcing-testcontainers` root folder
  
  - Run the following command
    ```
    ./mvnw clean spring-boot:run --projects event-service
    ```

## Running Applications as Docker containers

- ### Build Docker Images

  - In a terminal, make sure you are inside `spring-cloud-stream-event-sourcing-testcontainers` root folder

  - Run the following script to build the Docker images
    - JVM 
      ```
      ./docker-build.sh
      ```
    - Native (it's not working, see [Issues](#issues))
      ```
      ./docker-build.sh native
      ```

- ### Environment Variables
   
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

- ### Run Docker Containers

  - In a terminal, make sure you are inside `spring-cloud-stream-event-sourcing-testcontainers` root folder

  - In order to run the application's Docker container, you can pick between `JSON` or `Avro`
    > **Warning:**  Native is not working yet, [see Issues](#issues))
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

1. Create a user
   ```
   curl -i -X POST localhost:9080/api/users \
     -H  "Content-Type: application/json" \
     -d '{"email":"ivan.franchin@test.com","fullName":"Ivan Franchin","active":true}'
   ```

1. Check whether the event related to the user creation was received by `event-service`
   ```
   curl -i "localhost:9081/api/events?userId=1"
   ```

1. You can check me message trace using [`Zipkin`](https://zipkin.io) http://localhost:9411. The picture below shows an example 

   ![zipkin](images/zipkin.png)

1. Access `user-service` and create new users and/or update/delete existing ones. Then, access `event-service` Swagger website to validate if the events were sent correctly

## Useful Commands & Links

- **MySQL**
  ```
  docker exec -it mysql mysql -uroot -psecret --database userdb
  select * from users;
  ```
  > Type `exit` to leave `MySQL Monitor`

- **Cassandra**
  ```
  docker exec -it cassandra cqlsh
  USE mycompany;
  SELECT * FROM user_events;
  ```
  > Type `exit` to leave `CQL shell`

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

  The image below shows the topics present in Kafka, including the topic `com.mycompany.userservice.user` with `3`
partitions.

  ![kafka-manager](images/kafka-manager.png)

## Shutdown

- Stop applications
  - If they were started with `Maven`, go to the terminals where they are running and press `Ctrl+C`
  - If they were started as a Docker container, run the script below
    ```
    ./stop-apps.sh
    ```

- To stop and remove docker-compose containers, networks and volumes, make sure you are inside `spring-cloud-stream-event-sourcing-testcontainers` root folder and run
  ```
  docker-compose down -v
  ```

## Cleanup

To remove the Docker images created by this project, go to a terminal and, inside `spring-cloud-stream-event-sourcing-testcontainers` root folder, run the following script
```
./remove-docker-images.sh
```

## Running tests

- **event-service**

  - Run the command below to start the **Unit Tests**
    > **Note:** `Testcontainers` will start automatically `Cassandra` Docker container before some tests begin and will shut it down when the tests finish.
    ```
    ./mvnw clean test --projects event-service
    ```

- **user-service**

  - Run the command below to start the **Unit Tests**
    > **Note:** `Testcontainers` will start automatically `MySQL` Docker container before some tests begin and will shut it down when the tests finish.
    ```
    ./mvnw clean test --projects user-service
    ```

  - Run the command below to start the **Integration Tests**
    > **Important:** Make sure you have an updated `event-service` Docker image
    
    > **Note:** `Testcontainers` will start automatically `Zookeeper`, `Kafka`, `MySQL`, `Cassandra` and `event-service` Docker containers before the tests begin and will shut them down when the tests finish.
 
    - Using `JSON`
      ```
      ./mvnw clean verify --projects user-service -DargLine="-Dspring.profiles.active=test"
      ```
    - Using `Avro`
      ```
      ./mvnw clean verify --projects user-service -DargLine="-Dspring.profiles.active=test,avro"
      ```

## Using Tracing Agent to generate the missing configuration for native image

> **Important**: The environment variable `JAVA_HOME` must be set to a `GraalVM` installation directory ([Install GraalVM](https://www.graalvm.org/docs/getting-started/#install-graalvm)), and the `native-image` tool must be installed ([Install Native Image](https://www.graalvm.org/reference-manual/native-image/#install-native-image)).

> **Tip**: For more information `Tracing Agent` see [Spring Native documentation](https://docs.spring.io/spring-native/docs/current/reference/htmlsingle/#tracing-agent)

- **user-service**
  
  - Run the following steps in a terminal and inside `spring-cloud-stream-event-sourcing-testcontainers` root folder
    ```
    ./mvnw clean package --projects user-service -DskipTests
    cd user-service
    java -jar -agentlib:native-image-agent=config-merge-dir=src/main/resources/META-INF/native-image target/user-service-1.0.0.jar
    ```
  - Once the application is running, exercise it by calling its endpoints using `curl` and `Swagger` so that `Tracing Agent` observes the behavior of the application running on Java HotSpot VM and writes configuration files for reflection, JNI, resource, and proxy usage to automatically configure the native image generator.
  - It should generate `JSON` files in `user-service/src/main/resources/META-INF/native-image` such as: `jni-config.json`, `proxy-config.json`, `reflect-config.json`, `resource-config.json` and `serialization-config.json`.

- **event-service**
    
  - Run the following steps in a terminal and inside `spring-cloud-stream-event-sourcing-testcontainers` root folder
    ```
    ./mvnw clean package --projects user-service -DskipTests
    cd event-service
    java -jar -agentlib:native-image-agent=config-merge-dir=src/main/resources/META-INF/native-image target/event-service-1.0.0.jar
    ```
  - Once the application is running, exercise it by calling its endpoints using `curl` and `Swagger` so that `Tracing Agent` observes the behavior of the application running on Java HotSpot VM and writes configuration files for reflection, JNI, resource, and proxy usage to automatically configure the native image generator.
  - It should generate `JSON` files in `event-service/src/main/resources/META-INF/native-image` such as: `jni-config.json`, `proxy-config.json`, `reflect-config.json`, `resource-config.json` and `serialization-config.json`.

## References

- https://docs.spring.io/spring-cloud-stream/docs/current/reference/html/spring-cloud-stream.html

## Issues

- After building `user-service` Docker native image, the application starts and runs fine when using `default` profile, i.e, `JSON` serialization format.

  However, a warning is logged at startup when using `avro` profile
  ```
  WARN [user-service,,] 1 --- [           main] o.s.c.s.binder.DefaultBinderFactory      : Failed to add additional Message Converters from child context
  
  java.lang.NullPointerException: null
  	at org.springframework.cloud.stream.binder.DefaultBinderFactory.getBinderInstance(DefaultBinderFactory.java:277) ~[na:na]
  	at org.springframework.cloud.stream.binder.DefaultBinderFactory.doGetBinder(DefaultBinderFactory.java:224) ~[na:na]
  	at org.springframework.cloud.stream.binder.DefaultBinderFactory.getBinder(DefaultBinderFactory.java:152) ~[na:na]
  	at org.springframework.cloud.stream.binding.BindingService.getBinder(BindingService.java:386) ~[com.mycompany.userservice.UserServiceApplication:3.1.3]
  	at org.springframework.cloud.stream.binding.BindingService.bindProducer(BindingService.java:270) ~[com.mycompany.userservice.UserServiceApplication:3.1.3]
  	at org.springframework.cloud.stream.binding.BindingService.bindProducer(BindingService.java:294) ~[com.mycompany.userservice.UserServiceApplication:3.1.3]
  	at org.springframework.cloud.stream.binding.BindingService.bindProducer(BindingService.java:298) ~[com.mycompany.userservice.UserServiceApplication:3.1.3]
  	at org.springframework.cloud.stream.binding.AbstractBindableProxyFactory.createAndBindOutputs(AbstractBindableProxyFactory.java:142) ~[na:na]
  	at org.springframework.cloud.stream.binding.OutputBindingLifecycle.doStartWithBindable(OutputBindingLifecycle.java:58) ~[com.mycompany.userservice.UserServiceApplication:3.1.3]
  	at java.util.LinkedHashMap$LinkedValues.forEach(LinkedHashMap.java:608) ~[na:na]
  	at org.springframework.cloud.stream.binding.AbstractBindingLifecycle.start(AbstractBindingLifecycle.java:57) ~[na:na]
  	at org.springframework.cloud.stream.binding.OutputBindingLifecycle.start(OutputBindingLifecycle.java:34) ~[com.mycompany.userservice.UserServiceApplication:3.1.3]
  	at org.springframework.context.support.DefaultLifecycleProcessor.doStart(DefaultLifecycleProcessor.java:178) ~[com.mycompany.userservice.UserServiceApplication:5.3.9]
  	at org.springframework.context.support.DefaultLifecycleProcessor.access$200(DefaultLifecycleProcessor.java:54) ~[com.mycompany.userservice.UserServiceApplication:5.3.9]
  	at org.springframework.context.support.DefaultLifecycleProcessor$LifecycleGroup.start(DefaultLifecycleProcessor.java:356) ~[na:na]
  	at java.lang.Iterable.forEach(Iterable.java:75) ~[na:na]
  	at org.springframework.context.support.DefaultLifecycleProcessor.startBeans(DefaultLifecycleProcessor.java:155) ~[com.mycompany.userservice.UserServiceApplication:5.3.9]
  	at org.springframework.context.support.DefaultLifecycleProcessor.onRefresh(DefaultLifecycleProcessor.java:123) ~[com.mycompany.userservice.UserServiceApplication:5.3.9]
  	at org.springframework.context.support.AbstractApplicationContext.finishRefresh(AbstractApplicationContext.java:935) ~[na:na]
  	at org.springframework.context.support.AbstractApplicationContext.refresh(AbstractApplicationContext.java:586) ~[na:na]
  	at org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext.refresh(ServletWebServerApplicationContext.java:145) ~[na:na]
  	at org.springframework.boot.SpringApplication.refresh(SpringApplication.java:754) ~[com.mycompany.userservice.UserServiceApplication:2.5.4]
  	at org.springframework.boot.SpringApplication.refreshContext(SpringApplication.java:434) ~[com.mycompany.userservice.UserServiceApplication:2.5.4]
  	at org.springframework.boot.SpringApplication.run(SpringApplication.java:338) ~[com.mycompany.userservice.UserServiceApplication:2.5.4]
  	at org.springframework.boot.SpringApplication.run(SpringApplication.java:1343) ~[com.mycompany.userservice.UserServiceApplication:2.5.4]
  	at org.springframework.boot.SpringApplication.run(SpringApplication.java:1332) ~[com.mycompany.userservice.UserServiceApplication:2.5.4]
  	at com.mycompany.userservice.UserServiceApplication.main(UserServiceApplication.java:19) ~[com.mycompany.userservice.UserServiceApplication:na]
  ```

  Once the application is up and running using `avro` profile, the following exception is thrown when submitting a POST request to create a user
  ```
   WARN [user-service,9f16e9c3858269ff,9f16e9c3858269ff] 1 --- [nio-9080-exec-1] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Error: 0, SQLState: S1009
  2021-09-01 09:43:12.799 ERROR [user-service,9f16e9c3858269ff,9f16e9c3858269ff] 1 --- [nio-9080-exec-1] o.h.engine.jdbc.spi.SqlExceptionHelper   : Unsupported character encoding 'CP1252'
  2021-09-01 09:43:12.814 ERROR [user-service,,] 1 --- [nio-9080-exec-1] o.a.c.c.C.[.[.[/].[dispatcherServlet]    : Servlet.service() for servlet [dispatcherServlet] in context with path [] threw exception [Request processing failed; nested exception is org.springframework.orm.jpa.JpaSystemException: could not execute query; nested exception is org.hibernate.exception.GenericJDBCException: could not execute query] with root cause
  
  java.io.UnsupportedEncodingException: CP1252
  	at java.lang.StringCoding.decode(StringCoding.java:243) ~[na:na]
  	at java.lang.String.<init>(String.java:467) ~[com.mycompany.userservice.UserServiceApplication:na]
  	at com.mysql.cj.util.StringUtils.toString(StringUtils.java:1720) ~[na:na]
  	at com.mysql.cj.result.StringValueFactory.createFromBytes(StringValueFactory.java:139) ~[na:na]
  	at com.mysql.cj.result.StringValueFactory.createFromBytes(StringValueFactory.java:47) ~[na:na]
  	at com.mysql.cj.protocol.a.MysqlTextValueDecoder.decodeByteArray(MysqlTextValueDecoder.java:143) ~[na:na]
  	at com.mysql.cj.protocol.result.AbstractResultsetRow.decodeAndCreateReturnValue(AbstractResultsetRow.java:135) ~[na:na]
  	at com.mysql.cj.protocol.result.AbstractResultsetRow.getValueFromBytes(AbstractResultsetRow.java:243) ~[na:na]
  	at com.mysql.cj.protocol.a.result.ByteArrayRow.getValue(ByteArrayRow.java:91) ~[na:na]
  	at com.mysql.cj.jdbc.result.ResultSetImpl.getString(ResultSetImpl.java:875) ~[na:na]
  	at com.mysql.cj.jdbc.result.ResultSetImpl.getString(ResultSetImpl.java:888) ~[na:na]
  	at com.zaxxer.hikari.pool.HikariProxyResultSet.getString(HikariProxyResultSet.java) ~[na:na]
  	at org.hibernate.type.descriptor.sql.VarcharTypeDescriptor$2.doExtract(VarcharTypeDescriptor.java:62) ~[na:na]
  	at org.hibernate.type.descriptor.sql.BasicExtractor.extract(BasicExtractor.java:47) ~[na:na]
  	at org.hibernate.type.AbstractStandardBasicType.nullSafeGet(AbstractStandardBasicType.java:257) ~[na:na]
  	at org.hibernate.type.AbstractStandardBasicType.nullSafeGet(AbstractStandardBasicType.java:253) ~[na:na]
  	at org.hibernate.type.AbstractStandardBasicType.nullSafeGet(AbstractStandardBasicType.java:243) ~[na:na]
  	at org.hibernate.type.AbstractStandardBasicType.hydrate(AbstractStandardBasicType.java:329) ~[na:na]
  	at org.hibernate.persister.entity.AbstractEntityPersister.hydrate(AbstractEntityPersister.java:3131) ~[na:na]
  	at org.hibernate.loader.Loader.loadFromResultSet(Loader.java:1863) ~[na:na]
  	at org.hibernate.loader.Loader.hydrateEntityState(Loader.java:1791) ~[na:na]
  	at org.hibernate.loader.Loader.instanceNotYetLoaded(Loader.java:1764) ~[na:na]
  	at org.hibernate.loader.Loader.getRow(Loader.java:1616) ~[na:na]
  	at org.hibernate.loader.Loader.getRowFromResultSet(Loader.java:740) ~[na:na]
  	at org.hibernate.loader.Loader.getRowsFromResultSet(Loader.java:1039) ~[na:na]
  	at org.hibernate.loader.Loader.processResultSet(Loader.java:990) ~[na:na]
  	at org.hibernate.loader.Loader.doQuery(Loader.java:959) ~[na:na]
  	at org.hibernate.loader.Loader.doQueryAndInitializeNonLazyCollections(Loader.java:349) ~[na:na]
  	at org.hibernate.loader.Loader.doList(Loader.java:2843) ~[na:na]
  	at org.hibernate.loader.Loader.doList(Loader.java:2825) ~[na:na]
  	at org.hibernate.loader.Loader.listIgnoreQueryCache(Loader.java:2657) ~[na:na]
  	at org.hibernate.loader.Loader.list(Loader.java:2652) ~[na:na]
  	at org.hibernate.loader.hql.QueryLoader.list(QueryLoader.java:506) ~[na:na]
  	at org.hibernate.hql.internal.ast.QueryTranslatorImpl.list(QueryTranslatorImpl.java:400) ~[na:na]
  	at org.hibernate.engine.query.spi.HQLQueryPlan.performList(HQLQueryPlan.java:219) ~[na:na]
  	at org.hibernate.internal.SessionImpl.list(SessionImpl.java:1414) ~[com.mycompany.userservice.UserServiceApplication:5.4.32.Final]
  	at org.hibernate.query.internal.AbstractProducedQuery.doList(AbstractProducedQuery.java:1636) ~[na:na]
  	at org.hibernate.query.internal.AbstractProducedQuery.list(AbstractProducedQuery.java:1604) ~[na:na]
  	at org.hibernate.query.internal.AbstractProducedQuery.getSingleResult(AbstractProducedQuery.java:1652) ~[na:na]
  	at org.hibernate.query.criteria.internal.compile.CriteriaQueryTypeQueryAdapter.getSingleResult(CriteriaQueryTypeQueryAdapter.java:111) ~[na:na]
  	at org.springframework.data.jpa.repository.query.JpaQueryExecution$SingleEntityExecution.doExecute(JpaQueryExecution.java:196) ~[na:na]
  	at org.springframework.data.jpa.repository.query.JpaQueryExecution.execute(JpaQueryExecution.java:88) ~[na:na]
  	at org.springframework.data.jpa.repository.query.AbstractJpaQuery.doExecute(AbstractJpaQuery.java:155) ~[na:na]
  	at org.springframework.data.jpa.repository.query.AbstractJpaQuery.execute(AbstractJpaQuery.java:143) ~[na:na]
  	at org.springframework.data.repository.core.support.RepositoryMethodInvoker.doInvoke(RepositoryMethodInvoker.java:137) ~[na:na]
  	at org.springframework.data.repository.core.support.RepositoryMethodInvoker.invoke(RepositoryMethodInvoker.java:121) ~[na:na]
  	at org.springframework.data.repository.core.support.QueryExecutorMethodInterceptor.doInvoke(QueryExecutorMethodInterceptor.java:159) ~[na:na]
  	at org.springframework.data.repository.core.support.QueryExecutorMethodInterceptor.invoke(QueryExecutorMethodInterceptor.java:138) ~[na:na]
  	at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:186) ~[na:na]
  	at org.springframework.data.projection.DefaultMethodInvokingMethodInterceptor.invoke(DefaultMethodInvokingMethodInterceptor.java:80) ~[na:na]
  	at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:186) ~[na:na]
  	at org.springframework.transaction.interceptor.TransactionInterceptor$1.proceedWithInvocation(TransactionInterceptor.java:123) ~[na:na]
  	at org.springframework.transaction.interceptor.TransactionAspectSupport.invokeWithinTransaction(TransactionAspectSupport.java:388) ~[na:na]
  	at org.springframework.transaction.interceptor.TransactionInterceptor.invoke(TransactionInterceptor.java:119) ~[com.mycompany.userservice.UserServiceApplication:5.3.9]
  	at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:186) ~[na:na]
  	at org.springframework.dao.support.PersistenceExceptionTranslationInterceptor.invoke(PersistenceExceptionTranslationInterceptor.java:137) ~[na:na]
  	at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:186) ~[na:na]
  	at org.springframework.data.jpa.repository.support.CrudMethodMetadataPostProcessor$CrudMethodMetadataPopulatingMethodInterceptor.invoke(CrudMethodMetadataPostProcessor.java:145) ~[na:na]
  	at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:186) ~[na:na]
  	at org.springframework.aop.interceptor.ExposeInvocationInterceptor.invoke(ExposeInvocationInterceptor.java:97) ~[na:na]
  	at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:186) ~[na:na]
  	at org.springframework.aop.framework.JdkDynamicAopProxy.invoke(JdkDynamicAopProxy.java:215) ~[na:na]
  	at com.sun.proxy.$Proxy448.findUserByEmail(Unknown Source) ~[na:na]
  	at com.mycompany.userservice.service.UserServiceImpl.validateUserExistsByEmail(UserServiceImpl.java:41) ~[com.mycompany.userservice.UserServiceApplication:na]
  	at com.mycompany.userservice.rest.UserController.createUser(UserController.java:53) ~[com.mycompany.userservice.UserServiceApplication:na]
  	at java.lang.reflect.Method.invoke(Method.java:566) ~[na:na]
  	at org.springframework.web.method.support.InvocableHandlerMethod.doInvoke(InvocableHandlerMethod.java:197) ~[na:na]
  	at org.springframework.web.method.support.InvocableHandlerMethod.invokeForRequest(InvocableHandlerMethod.java:141) ~[na:na]
  	at org.springframework.web.servlet.mvc.method.annotation.ServletInvocableHandlerMethod.invokeAndHandle(ServletInvocableHandlerMethod.java:106) ~[na:na]
  	at org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter.invokeHandlerMethod(RequestMappingHandlerAdapter.java:895) ~[com.mycompany.userservice.UserServiceApplication:5.3.9]
  	at org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter.handleInternal(RequestMappingHandlerAdapter.java:808) ~[com.mycompany.userservice.UserServiceApplication:5.3.9]
  	at org.springframework.web.servlet.mvc.method.AbstractHandlerMethodAdapter.handle(AbstractHandlerMethodAdapter.java:87) ~[na:na]
  	at org.springframework.web.servlet.DispatcherServlet.doDispatch(DispatcherServlet.java:1064) ~[com.mycompany.userservice.UserServiceApplication:5.3.9]
  	at org.springframework.web.servlet.DispatcherServlet.doService(DispatcherServlet.java:963) ~[com.mycompany.userservice.UserServiceApplication:5.3.9]
  	at org.springframework.web.servlet.FrameworkServlet.processRequest(FrameworkServlet.java:1006) ~[na:na]
  	at org.springframework.web.servlet.FrameworkServlet.doPost(FrameworkServlet.java:909) ~[na:na]
  	at javax.servlet.http.HttpServlet.service(HttpServlet.java:681) ~[com.mycompany.userservice.UserServiceApplication:4.0.FR]
  	at org.springframework.web.servlet.FrameworkServlet.service(FrameworkServlet.java:883) ~[na:na]
  	at javax.servlet.http.HttpServlet.service(HttpServlet.java:764) ~[com.mycompany.userservice.UserServiceApplication:4.0.FR]
  	at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:227) ~[na:na]
  	at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:162) ~[na:na]
  	at org.apache.tomcat.websocket.server.WsFilter.doFilter(WsFilter.java:53) ~[com.mycompany.userservice.UserServiceApplication:9.0.52]
  	at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:189) ~[na:na]
  	at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:162) ~[na:na]
  	at org.springframework.web.filter.RequestContextFilter.doFilterInternal(RequestContextFilter.java:100) ~[com.mycompany.userservice.UserServiceApplication:5.3.9]
  	at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:119) ~[na:na]
  	at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:189) ~[na:na]
  	at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:162) ~[na:na]
  	at org.springframework.web.filter.FormContentFilter.doFilterInternal(FormContentFilter.java:93) ~[com.mycompany.userservice.UserServiceApplication:5.3.9]
  	at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:119) ~[na:na]
  	at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:189) ~[na:na]
  	at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:162) ~[na:na]
  	at org.springframework.cloud.sleuth.instrument.web.servlet.TracingFilter.doFilter(TracingFilter.java:89) ~[na:na]
  	at org.springframework.cloud.sleuth.autoconfig.instrument.web.LazyTracingFilter.doFilter(TraceWebServletConfiguration.java:114) ~[na:na]
  	at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:189) ~[na:na]
  	at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:162) ~[na:na]
  	at org.springframework.boot.actuate.metrics.web.servlet.WebMvcMetricsFilter.doFilterInternal(WebMvcMetricsFilter.java:96) ~[na:na]
  	at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:119) ~[na:na]
  	at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:189) ~[na:na]
  	at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:162) ~[na:na]
  	at org.springframework.web.filter.CharacterEncodingFilter.doFilterInternal(CharacterEncodingFilter.java:201) ~[com.mycompany.userservice.UserServiceApplication:5.3.9]
  	at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:119) ~[na:na]
  	at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:189) ~[na:na]
  	at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:162) ~[na:na]
  	at org.apache.catalina.core.StandardWrapperValve.invoke(StandardWrapperValve.java:197) ~[na:na]
  	at org.apache.catalina.core.StandardContextValve.invoke(StandardContextValve.java:97) ~[na:na]
  	at org.apache.catalina.authenticator.AuthenticatorBase.invoke(AuthenticatorBase.java:542) ~[na:na]
  	at org.apache.catalina.core.StandardHostValve.invoke(StandardHostValve.java:135) ~[na:na]
  	at org.apache.catalina.valves.ErrorReportValve.invoke(ErrorReportValve.java:92) ~[com.mycompany.userservice.UserServiceApplication:9.0.52]
  	at org.apache.catalina.core.StandardEngineValve.invoke(StandardEngineValve.java:78) ~[na:na]
  	at org.apache.catalina.connector.CoyoteAdapter.service(CoyoteAdapter.java:357) ~[na:na]
  	at org.apache.coyote.http11.Http11Processor.service(Http11Processor.java:382) ~[na:na]
  	at org.apache.coyote.AbstractProcessorLight.process(AbstractProcessorLight.java:65) ~[na:na]
  	at org.apache.coyote.AbstractProtocol$ConnectionHandler.process(AbstractProtocol.java:893) ~[na:na]
  	at org.apache.tomcat.util.net.NioEndpoint$SocketProcessor.doRun(NioEndpoint.java:1726) ~[na:na]
  	at org.apache.tomcat.util.net.SocketProcessorBase.run(SocketProcessorBase.java:49) ~[na:na]
  	at org.apache.tomcat.util.threads.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1191) ~[na:na]
  	at org.apache.tomcat.util.threads.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:659) ~[na:na]
  	at org.apache.tomcat.util.threads.TaskThread$WrappingRunnable.run(TaskThread.java:61) ~[na:na]
  	at java.lang.Thread.run(Thread.java:829) ~[na:na]
  	at com.oracle.svm.core.thread.JavaThreads.threadStartRoutine(JavaThreads.java:567) ~[na:na]
  	at com.oracle.svm.core.posix.thread.PosixJavaThreads.pthreadStartRoutine(PosixJavaThreads.java:192) ~[na:na]
  ```

- After building successfully the `event-service` Docker native image, the following exception is thrown at startup
  ```
  ERROR [event-service,,] 1 --- [           main] o.a.c.c.C.[Tomcat].[localhost].[/]       : Exception starting filter [lazyTracingFilter]
  
  org.springframework.beans.factory.UnsatisfiedDependencyException: Error creating bean with name 'braveHttpServerHandler' defined in class path resource [org/springframework/cloud/sleuth/autoconfig/brave/instrument/web/BraveHttpBridgeConfiguration.class]: Unsatisfied dependency expressed through method 'braveHttpServerHandler' parameter 0; nested exception is org.springframework.beans.factory.UnsatisfiedDependencyException: Error creating bean with name 'httpTracing' defined in class path resource [org/springframework/cloud/sleuth/autoconfig/brave/instrument/web/BraveHttpConfiguration.class]: Unsatisfied dependency expressed through method 'httpTracing' parameter 1; nested exception is org.springframework.beans.factory.BeanCreationException: Error creating bean with name 'sleuthSkipPatternProvider' defined in class path resource [org/springframework/cloud/sleuth/autoconfig/instrument/web/SkipPatternConfiguration.class]: Bean instantiation via factory method failed; nested exception is org.springframework.beans.BeanInstantiationException: Failed to instantiate [org.springframework.cloud.sleuth.instrument.web.SkipPatternProvider]: Factory method 'sleuthSkipPatternProvider' threw exception; nested exception is org.springframework.beans.factory.UnsatisfiedDependencyException: Error creating bean with name 'healthEndpoint' defined in class path resource [org/springframework/boot/actuate/autoconfigure/health/HealthEndpointConfiguration.class]: Unsatisfied dependency expressed through method 'healthEndpoint' parameter 0; nested exception is org.springframework.beans.factory.BeanCreationException: Error creating bean with name 'healthContributorRegistry' defined in class path resource [org/springframework/boot/actuate/autoconfigure/health/HealthEndpointConfiguration.class]: Bean instantiation via factory method failed; nested exception is org.springframework.beans.BeanInstantiationException: Failed to instantiate [org.springframework.boot.actuate.health.HealthContributorRegistry]: Factory method 'healthContributorRegistry' threw exception; nested exception is org.springframework.beans.factory.UnsatisfiedDependencyException: Error creating bean with name 'cassandraHealthContributor' defined in class path resource [org/springframework/boot/actuate/autoconfigure/cassandra/CassandraHealthContributorConfigurations$CassandraReactiveDriverConfiguration.class]: Unsatisfied dependency expressed through method 'cassandraHealthContributor' parameter 0; nested exception is org.springframework.beans.factory.BeanCreationException: Error creating bean with name 'session' defined in class path resource [com/mycompany/eventservice/config/CassandraConfig.class]: Invocation of init method failed; nested exception is com.typesafe.config.ConfigException$Missing: system properties: No configuration setting found for key 'datastax-java-driver'
  	at org.springframework.beans.factory.support.ConstructorResolver.createArgumentArray(ConstructorResolver.java:800) ~[na:na]
  	at org.springframework.beans.factory.support.ConstructorResolver.instantiateUsingFactoryMethod(ConstructorResolver.java:541) ~[na:na]
  	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.instantiateUsingFactoryMethod(AbstractAutowireCapableBeanFactory.java:1334) ~[na:na]
  	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.createBeanInstance(AbstractAutowireCapableBeanFactory.java:1177) ~[na:na]
  	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.doCreateBean(AbstractAutowireCapableBeanFactory.java:564) ~[na:na]
  	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.createBean(AbstractAutowireCapableBeanFactory.java:524) ~[na:na]
  	at org.springframework.beans.factory.support.AbstractBeanFactory.lambda$doGetBean$0(AbstractBeanFactory.java:335) ~[na:na]
  	at org.springframework.beans.factory.support.DefaultSingletonBeanRegistry.getSingleton(DefaultSingletonBeanRegistry.java:234) ~[na:na]
  	at org.springframework.beans.factory.support.AbstractBeanFactory.doGetBean(AbstractBeanFactory.java:333) ~[na:na]
  	at org.springframework.beans.factory.support.AbstractBeanFactory.getBean(AbstractBeanFactory.java:233) ~[na:na]
  	at org.springframework.beans.factory.support.DefaultListableBeanFactory.resolveNamedBean(DefaultListableBeanFactory.java:1273) ~[na:na]
  	at org.springframework.beans.factory.support.DefaultListableBeanFactory.resolveNamedBean(DefaultListableBeanFactory.java:1234) ~[na:na]
  	at org.springframework.beans.factory.support.DefaultListableBeanFactory.resolveBean(DefaultListableBeanFactory.java:494) ~[na:na]
  	at org.springframework.beans.factory.support.DefaultListableBeanFactory.getBean(DefaultListableBeanFactory.java:349) ~[na:na]
  	at org.springframework.beans.factory.support.DefaultListableBeanFactory.getBean(DefaultListableBeanFactory.java:342) ~[na:na]
  	at org.springframework.cloud.sleuth.autoconfig.instrument.web.LazyTracingFilter.tracingFilter(TraceWebServletConfiguration.java:125) ~[na:na]
  	at org.springframework.cloud.sleuth.autoconfig.instrument.web.LazyTracingFilter.init(TraceWebServletConfiguration.java:108) ~[na:na]
  	at org.apache.catalina.core.ApplicationFilterConfig.initFilter(ApplicationFilterConfig.java:270) ~[na:na]
  	at org.apache.catalina.core.ApplicationFilterConfig.<init>(ApplicationFilterConfig.java:105) ~[na:na]
  	at org.apache.catalina.core.StandardContext.filterStart(StandardContext.java:4613) ~[com.mycompany.eventservice.EventServiceApplication:9.0.52]
  	at org.apache.catalina.core.StandardContext.startInternal(StandardContext.java:5256) ~[com.mycompany.eventservice.EventServiceApplication:9.0.52]
  	at org.apache.catalina.util.LifecycleBase.start(LifecycleBase.java:183) ~[na:na]
  	at org.apache.catalina.core.ContainerBase$StartChild.call(ContainerBase.java:1396) ~[na:na]
  	at org.apache.catalina.core.ContainerBase$StartChild.call(ContainerBase.java:1386) ~[na:na]
  	at java.util.concurrent.FutureTask.run(FutureTask.java:264) ~[na:na]
  	at org.apache.tomcat.util.threads.InlineExecutorService.execute(InlineExecutorService.java:75) ~[na:na]
  	at java.util.concurrent.AbstractExecutorService.submit(AbstractExecutorService.java:140) ~[na:na]
  	at org.apache.catalina.core.ContainerBase.startInternal(ContainerBase.java:919) ~[na:na]
  	at org.apache.catalina.core.StandardHost.startInternal(StandardHost.java:835) ~[na:na]
  	at org.apache.catalina.util.LifecycleBase.start(LifecycleBase.java:183) ~[na:na]
  	at org.apache.catalina.core.ContainerBase$StartChild.call(ContainerBase.java:1396) ~[na:na]
  	at org.apache.catalina.core.ContainerBase$StartChild.call(ContainerBase.java:1386) ~[na:na]
  	at java.util.concurrent.FutureTask.run(FutureTask.java:264) ~[na:na]
  	at org.apache.tomcat.util.threads.InlineExecutorService.execute(InlineExecutorService.java:75) ~[na:na]
  	at java.util.concurrent.AbstractExecutorService.submit(AbstractExecutorService.java:140) ~[na:na]
  	at org.apache.catalina.core.ContainerBase.startInternal(ContainerBase.java:919) ~[na:na]
  	at org.apache.catalina.core.StandardEngine.startInternal(StandardEngine.java:263) ~[na:na]
  	at org.apache.catalina.util.LifecycleBase.start(LifecycleBase.java:183) ~[na:na]
  	at org.apache.catalina.core.StandardService.startInternal(StandardService.java:432) ~[na:na]
  	at org.apache.catalina.util.LifecycleBase.start(LifecycleBase.java:183) ~[na:na]
  	at org.apache.catalina.core.StandardServer.startInternal(StandardServer.java:927) ~[na:na]
  	at org.apache.catalina.util.LifecycleBase.start(LifecycleBase.java:183) ~[na:na]
  	at org.apache.catalina.startup.Tomcat.start(Tomcat.java:486) ~[com.mycompany.eventservice.EventServiceApplication:9.0.52]
  	at org.springframework.boot.web.embedded.tomcat.TomcatWebServer.initialize(TomcatWebServer.java:123) ~[na:na]
  	at org.springframework.boot.web.embedded.tomcat.TomcatWebServer.<init>(TomcatWebServer.java:104) ~[na:na]
  	at org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory.getTomcatWebServer(TomcatServletWebServerFactory.java:450) ~[com.mycompany.eventservice.EventServiceApplication:2.5.4]
  	at org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory.getWebServer(TomcatServletWebServerFactory.java:199) ~[com.mycompany.eventservice.EventServiceApplication:2.5.4]
  	at org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext.createWebServer(ServletWebServerApplicationContext.java:182) ~[na:na]
  	at org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext.onRefresh(ServletWebServerApplicationContext.java:160) ~[na:na]
  	at org.springframework.context.support.AbstractApplicationContext.refresh(AbstractApplicationContext.java:577) ~[na:na]
  	at org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext.refresh(ServletWebServerApplicationContext.java:145) ~[na:na]
  	at org.springframework.boot.SpringApplication.refresh(SpringApplication.java:754) ~[com.mycompany.eventservice.EventServiceApplication:2.5.4]
  	at org.springframework.boot.SpringApplication.refreshContext(SpringApplication.java:434) ~[com.mycompany.eventservice.EventServiceApplication:2.5.4]
  	at org.springframework.boot.SpringApplication.run(SpringApplication.java:338) ~[com.mycompany.eventservice.EventServiceApplication:2.5.4]
  	at org.springframework.boot.SpringApplication.run(SpringApplication.java:1343) ~[com.mycompany.eventservice.EventServiceApplication:2.5.4]
  	at org.springframework.boot.SpringApplication.run(SpringApplication.java:1332) ~[com.mycompany.eventservice.EventServiceApplication:2.5.4]
  	at com.mycompany.eventservice.EventServiceApplication.main(EventServiceApplication.java:19) ~[com.mycompany.eventservice.EventServiceApplication:na]
  Caused by: org.springframework.beans.factory.UnsatisfiedDependencyException: Error creating bean with name 'httpTracing' defined in class path resource [org/springframework/cloud/sleuth/autoconfig/brave/instrument/web/BraveHttpConfiguration.class]: Unsatisfied dependency expressed through method 'httpTracing' parameter 1; nested exception is org.springframework.beans.factory.BeanCreationException: Error creating bean with name 'sleuthSkipPatternProvider' defined in class path resource [org/springframework/cloud/sleuth/autoconfig/instrument/web/SkipPatternConfiguration.class]: Bean instantiation via factory method failed; nested exception is org.springframework.beans.BeanInstantiationException: Failed to instantiate [org.springframework.cloud.sleuth.instrument.web.SkipPatternProvider]: Factory method 'sleuthSkipPatternProvider' threw exception; nested exception is org.springframework.beans.factory.UnsatisfiedDependencyException: Error creating bean with name 'healthEndpoint' defined in class path resource [org/springframework/boot/actuate/autoconfigure/health/HealthEndpointConfiguration.class]: Unsatisfied dependency expressed through method 'healthEndpoint' parameter 0; nested exception is org.springframework.beans.factory.BeanCreationException: Error creating bean with name 'healthContributorRegistry' defined in class path resource [org/springframework/boot/actuate/autoconfigure/health/HealthEndpointConfiguration.class]: Bean instantiation via factory method failed; nested exception is org.springframework.beans.BeanInstantiationException: Failed to instantiate [org.springframework.boot.actuate.health.HealthContributorRegistry]: Factory method 'healthContributorRegistry' threw exception; nested exception is org.springframework.beans.factory.UnsatisfiedDependencyException: Error creating bean with name 'cassandraHealthContributor' defined in class path resource [org/springframework/boot/actuate/autoconfigure/cassandra/CassandraHealthContributorConfigurations$CassandraReactiveDriverConfiguration.class]: Unsatisfied dependency expressed through method 'cassandraHealthContributor' parameter 0; nested exception is org.springframework.beans.factory.BeanCreationException: Error creating bean with name 'session' defined in class path resource [com/mycompany/eventservice/config/CassandraConfig.class]: Invocation of init method failed; nested exception is com.typesafe.config.ConfigException$Missing: system properties: No configuration setting found for key 'datastax-java-driver'
  	at org.springframework.beans.factory.support.ConstructorResolver.createArgumentArray(ConstructorResolver.java:800) ~[na:na]
  	at org.springframework.beans.factory.support.ConstructorResolver.instantiateUsingFactoryMethod(ConstructorResolver.java:541) ~[na:na]
  	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.instantiateUsingFactoryMethod(AbstractAutowireCapableBeanFactory.java:1334) ~[na:na]
  	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.createBeanInstance(AbstractAutowireCapableBeanFactory.java:1177) ~[na:na]
  	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.doCreateBean(AbstractAutowireCapableBeanFactory.java:564) ~[na:na]
  	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.createBean(AbstractAutowireCapableBeanFactory.java:524) ~[na:na]
  	at org.springframework.beans.factory.support.AbstractBeanFactory.lambda$doGetBean$0(AbstractBeanFactory.java:335) ~[na:na]
  	at org.springframework.beans.factory.support.DefaultSingletonBeanRegistry.getSingleton(DefaultSingletonBeanRegistry.java:234) ~[na:na]
  	at org.springframework.beans.factory.support.AbstractBeanFactory.doGetBean(AbstractBeanFactory.java:333) ~[na:na]
  	at org.springframework.beans.factory.support.AbstractBeanFactory.getBean(AbstractBeanFactory.java:208) ~[na:na]
  	at org.springframework.beans.factory.config.DependencyDescriptor.resolveCandidate(DependencyDescriptor.java:276) ~[na:na]
  	at org.springframework.beans.factory.support.DefaultListableBeanFactory.doResolveDependency(DefaultListableBeanFactory.java:1380) ~[na:na]
  	at org.springframework.beans.factory.support.DefaultListableBeanFactory.resolveDependency(DefaultListableBeanFactory.java:1300) ~[na:na]
  	at org.springframework.beans.factory.support.ConstructorResolver.resolveAutowiredArgument(ConstructorResolver.java:887) ~[na:na]
  	at org.springframework.beans.factory.support.ConstructorResolver.createArgumentArray(ConstructorResolver.java:791) ~[na:na]
  	... 56 common frames omitted
  Caused by: org.springframework.beans.factory.BeanCreationException: Error creating bean with name 'sleuthSkipPatternProvider' defined in class path resource [org/springframework/cloud/sleuth/autoconfig/instrument/web/SkipPatternConfiguration.class]: Bean instantiation via factory method failed; nested exception is org.springframework.beans.BeanInstantiationException: Failed to instantiate [org.springframework.cloud.sleuth.instrument.web.SkipPatternProvider]: Factory method 'sleuthSkipPatternProvider' threw exception; nested exception is org.springframework.beans.factory.UnsatisfiedDependencyException: Error creating bean with name 'healthEndpoint' defined in class path resource [org/springframework/boot/actuate/autoconfigure/health/HealthEndpointConfiguration.class]: Unsatisfied dependency expressed through method 'healthEndpoint' parameter 0; nested exception is org.springframework.beans.factory.BeanCreationException: Error creating bean with name 'healthContributorRegistry' defined in class path resource [org/springframework/boot/actuate/autoconfigure/health/HealthEndpointConfiguration.class]: Bean instantiation via factory method failed; nested exception is org.springframework.beans.BeanInstantiationException: Failed to instantiate [org.springframework.boot.actuate.health.HealthContributorRegistry]: Factory method 'healthContributorRegistry' threw exception; nested exception is org.springframework.beans.factory.UnsatisfiedDependencyException: Error creating bean with name 'cassandraHealthContributor' defined in class path resource [org/springframework/boot/actuate/autoconfigure/cassandra/CassandraHealthContributorConfigurations$CassandraReactiveDriverConfiguration.class]: Unsatisfied dependency expressed through method 'cassandraHealthContributor' parameter 0; nested exception is org.springframework.beans.factory.BeanCreationException: Error creating bean with name 'session' defined in class path resource [com/mycompany/eventservice/config/CassandraConfig.class]: Invocation of init method failed; nested exception is com.typesafe.config.ConfigException$Missing: system properties: No configuration setting found for key 'datastax-java-driver'
  	at org.springframework.beans.factory.support.ConstructorResolver.instantiate(ConstructorResolver.java:658) ~[na:na]
  	at org.springframework.beans.factory.support.ConstructorResolver.instantiateUsingFactoryMethod(ConstructorResolver.java:638) ~[na:na]
  	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.instantiateUsingFactoryMethod(AbstractAutowireCapableBeanFactory.java:1334) ~[na:na]
  	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.createBeanInstance(AbstractAutowireCapableBeanFactory.java:1177) ~[na:na]
  	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.doCreateBean(AbstractAutowireCapableBeanFactory.java:564) ~[na:na]
  	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.createBean(AbstractAutowireCapableBeanFactory.java:524) ~[na:na]
  	at org.springframework.beans.factory.support.AbstractBeanFactory.lambda$doGetBean$0(AbstractBeanFactory.java:335) ~[na:na]
  	at org.springframework.beans.factory.support.DefaultSingletonBeanRegistry.getSingleton(DefaultSingletonBeanRegistry.java:234) ~[na:na]
  	at org.springframework.beans.factory.support.AbstractBeanFactory.doGetBean(AbstractBeanFactory.java:333) ~[na:na]
  	at org.springframework.beans.factory.support.AbstractBeanFactory.getBean(AbstractBeanFactory.java:208) ~[na:na]
  	at org.springframework.beans.factory.config.DependencyDescriptor.resolveCandidate(DependencyDescriptor.java:276) ~[na:na]
  	at org.springframework.beans.factory.support.DefaultListableBeanFactory.doResolveDependency(DefaultListableBeanFactory.java:1380) ~[na:na]
  	at org.springframework.beans.factory.support.DefaultListableBeanFactory.resolveDependency(DefaultListableBeanFactory.java:1300) ~[na:na]
  	at org.springframework.beans.factory.support.ConstructorResolver.resolveAutowiredArgument(ConstructorResolver.java:887) ~[na:na]
  	at org.springframework.beans.factory.support.ConstructorResolver.createArgumentArray(ConstructorResolver.java:791) ~[na:na]
  	... 70 common frames omitted
  Caused by: org.springframework.beans.BeanInstantiationException: Failed to instantiate [org.springframework.cloud.sleuth.instrument.web.SkipPatternProvider]: Factory method 'sleuthSkipPatternProvider' threw exception; nested exception is org.springframework.beans.factory.UnsatisfiedDependencyException: Error creating bean with name 'healthEndpoint' defined in class path resource [org/springframework/boot/actuate/autoconfigure/health/HealthEndpointConfiguration.class]: Unsatisfied dependency expressed through method 'healthEndpoint' parameter 0; nested exception is org.springframework.beans.factory.BeanCreationException: Error creating bean with name 'healthContributorRegistry' defined in class path resource [org/springframework/boot/actuate/autoconfigure/health/HealthEndpointConfiguration.class]: Bean instantiation via factory method failed; nested exception is org.springframework.beans.BeanInstantiationException: Failed to instantiate [org.springframework.boot.actuate.health.HealthContributorRegistry]: Factory method 'healthContributorRegistry' threw exception; nested exception is org.springframework.beans.factory.UnsatisfiedDependencyException: Error creating bean with name 'cassandraHealthContributor' defined in class path resource [org/springframework/boot/actuate/autoconfigure/cassandra/CassandraHealthContributorConfigurations$CassandraReactiveDriverConfiguration.class]: Unsatisfied dependency expressed through method 'cassandraHealthContributor' parameter 0; nested exception is org.springframework.beans.factory.BeanCreationException: Error creating bean with name 'session' defined in class path resource [com/mycompany/eventservice/config/CassandraConfig.class]: Invocation of init method failed; nested exception is com.typesafe.config.ConfigException$Missing: system properties: No configuration setting found for key 'datastax-java-driver'
  	at org.springframework.beans.factory.support.SimpleInstantiationStrategy.instantiate(SimpleInstantiationStrategy.java:185) ~[na:na]
  	at org.springframework.beans.factory.support.ConstructorResolver.instantiate(ConstructorResolver.java:653) ~[na:na]
  	... 84 common frames omitted
  Caused by: org.springframework.beans.factory.UnsatisfiedDependencyException: Error creating bean with name 'healthEndpoint' defined in class path resource [org/springframework/boot/actuate/autoconfigure/health/HealthEndpointConfiguration.class]: Unsatisfied dependency expressed through method 'healthEndpoint' parameter 0; nested exception is org.springframework.beans.factory.BeanCreationException: Error creating bean with name 'healthContributorRegistry' defined in class path resource [org/springframework/boot/actuate/autoconfigure/health/HealthEndpointConfiguration.class]: Bean instantiation via factory method failed; nested exception is org.springframework.beans.BeanInstantiationException: Failed to instantiate [org.springframework.boot.actuate.health.HealthContributorRegistry]: Factory method 'healthContributorRegistry' threw exception; nested exception is org.springframework.beans.factory.UnsatisfiedDependencyException: Error creating bean with name 'cassandraHealthContributor' defined in class path resource [org/springframework/boot/actuate/autoconfigure/cassandra/CassandraHealthContributorConfigurations$CassandraReactiveDriverConfiguration.class]: Unsatisfied dependency expressed through method 'cassandraHealthContributor' parameter 0; nested exception is org.springframework.beans.factory.BeanCreationException: Error creating bean with name 'session' defined in class path resource [com/mycompany/eventservice/config/CassandraConfig.class]: Invocation of init method failed; nested exception is com.typesafe.config.ConfigException$Missing: system properties: No configuration setting found for key 'datastax-java-driver'
  	at org.springframework.beans.factory.support.ConstructorResolver.createArgumentArray(ConstructorResolver.java:800) ~[na:na]
  	at org.springframework.beans.factory.support.ConstructorResolver.instantiateUsingFactoryMethod(ConstructorResolver.java:541) ~[na:na]
  	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.instantiateUsingFactoryMethod(AbstractAutowireCapableBeanFactory.java:1334) ~[na:na]
  	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.createBeanInstance(AbstractAutowireCapableBeanFactory.java:1177) ~[na:na]
  	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.doCreateBean(AbstractAutowireCapableBeanFactory.java:564) ~[na:na]
  	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.createBean(AbstractAutowireCapableBeanFactory.java:524) ~[na:na]
  	at org.springframework.beans.factory.support.AbstractBeanFactory.lambda$doGetBean$0(AbstractBeanFactory.java:335) ~[na:na]
  	at org.springframework.beans.factory.support.DefaultSingletonBeanRegistry.getSingleton(DefaultSingletonBeanRegistry.java:234) ~[na:na]
  	at org.springframework.beans.factory.support.AbstractBeanFactory.doGetBean(AbstractBeanFactory.java:333) ~[na:na]
  	at org.springframework.beans.factory.support.AbstractBeanFactory.getBean(AbstractBeanFactory.java:208) ~[na:na]
  	at org.springframework.context.support.AbstractApplicationContext.getBean(AbstractApplicationContext.java:1154) ~[na:na]
  	at org.springframework.boot.actuate.endpoint.annotation.EndpointDiscoverer.lambda$createEndpointBean$1(EndpointDiscoverer.java:145) ~[na:na]
  	at org.springframework.boot.actuate.endpoint.annotation.EndpointDiscoverer$EndpointBean.getBean(EndpointDiscoverer.java:447) ~[na:na]
  	at org.springframework.boot.actuate.endpoint.annotation.EndpointDiscoverer.getFilterEndpoint(EndpointDiscoverer.java:307) ~[na:na]
  	at org.springframework.boot.actuate.endpoint.annotation.EndpointDiscoverer.isFilterMatch(EndpointDiscoverer.java:285) ~[na:na]
  	at org.springframework.boot.actuate.endpoint.annotation.EndpointDiscoverer.isExtensionExposed(EndpointDiscoverer.java:239) ~[na:na]
  	at org.springframework.boot.actuate.endpoint.annotation.EndpointDiscoverer.addExtensionBean(EndpointDiscoverer.java:170) ~[na:na]
  	at org.springframework.boot.actuate.endpoint.annotation.EndpointDiscoverer.addExtensionBeans(EndpointDiscoverer.java:159) ~[na:na]
  	at org.springframework.boot.actuate.endpoint.annotation.EndpointDiscoverer.discoverEndpoints(EndpointDiscoverer.java:124) ~[na:na]
  	at org.springframework.boot.actuate.endpoint.annotation.EndpointDiscoverer.getEndpoints(EndpointDiscoverer.java:117) ~[na:na]
  	at org.springframework.cloud.sleuth.autoconfig.instrument.web.SkipPatternConfiguration$ActuatorSkipPatternProviderConfig.getEndpointsPatterns(SkipPatternConfiguration.java:152) ~[com.mycompany.eventservice.EventServiceApplication:3.0.3]
  	at org.springframework.cloud.sleuth.autoconfig.instrument.web.SkipPatternConfiguration$ActuatorSkipPatternProviderConfig.lambda$skipPatternForActuatorEndpointsSamePort$0(SkipPatternConfiguration.java:207) ~[com.mycompany.eventservice.EventServiceApplication:3.0.3]
  	at java.util.stream.ReferencePipeline$3$1.accept(ReferencePipeline.java:195) ~[na:na]
  	at java.util.ArrayList$ArrayListSpliterator.forEachRemaining(ArrayList.java:1655) ~[na:na]
  	at java.util.stream.AbstractPipeline.copyInto(AbstractPipeline.java:484) ~[na:na]
  	at java.util.stream.AbstractPipeline.wrapAndCopyInto(AbstractPipeline.java:474) ~[na:na]
  	at java.util.stream.ReduceOps$ReduceOp.evaluateSequential(ReduceOps.java:913) ~[na:na]
  	at java.util.stream.AbstractPipeline.evaluate(AbstractPipeline.java:234) ~[na:na]
  	at java.util.stream.ReferencePipeline.collect(ReferencePipeline.java:578) ~[na:na]
  	at org.springframework.cloud.sleuth.autoconfig.instrument.web.SkipPatternConfiguration.consolidateSkipPatterns(SkipPatternConfiguration.java:96) ~[com.mycompany.eventservice.EventServiceApplication:3.0.3]
  	at org.springframework.cloud.sleuth.autoconfig.instrument.web.SkipPatternConfiguration.sleuthSkipPatternProvider(SkipPatternConfiguration.java:80) ~[com.mycompany.eventservice.EventServiceApplication:3.0.3]
  	at java.lang.reflect.Method.invoke(Method.java:566) ~[na:na]
  	at org.springframework.beans.factory.support.SimpleInstantiationStrategy.instantiate(SimpleInstantiationStrategy.java:154) ~[na:na]
  	... 85 common frames omitted
  Caused by: org.springframework.beans.factory.BeanCreationException: Error creating bean with name 'healthContributorRegistry' defined in class path resource [org/springframework/boot/actuate/autoconfigure/health/HealthEndpointConfiguration.class]: Bean instantiation via factory method failed; nested exception is org.springframework.beans.BeanInstantiationException: Failed to instantiate [org.springframework.boot.actuate.health.HealthContributorRegistry]: Factory method 'healthContributorRegistry' threw exception; nested exception is org.springframework.beans.factory.UnsatisfiedDependencyException: Error creating bean with name 'cassandraHealthContributor' defined in class path resource [org/springframework/boot/actuate/autoconfigure/cassandra/CassandraHealthContributorConfigurations$CassandraReactiveDriverConfiguration.class]: Unsatisfied dependency expressed through method 'cassandraHealthContributor' parameter 0; nested exception is org.springframework.beans.factory.BeanCreationException: Error creating bean with name 'session' defined in class path resource [com/mycompany/eventservice/config/CassandraConfig.class]: Invocation of init method failed; nested exception is com.typesafe.config.ConfigException$Missing: system properties: No configuration setting found for key 'datastax-java-driver'
  	at org.springframework.beans.factory.support.ConstructorResolver.instantiate(ConstructorResolver.java:658) ~[na:na]
  	at org.springframework.beans.factory.support.ConstructorResolver.instantiateUsingFactoryMethod(ConstructorResolver.java:638) ~[na:na]
  	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.instantiateUsingFactoryMethod(AbstractAutowireCapableBeanFactory.java:1334) ~[na:na]
  	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.createBeanInstance(AbstractAutowireCapableBeanFactory.java:1177) ~[na:na]
  	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.doCreateBean(AbstractAutowireCapableBeanFactory.java:564) ~[na:na]
  	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.createBean(AbstractAutowireCapableBeanFactory.java:524) ~[na:na]
  	at org.springframework.beans.factory.support.AbstractBeanFactory.lambda$doGetBean$0(AbstractBeanFactory.java:335) ~[na:na]
  	at org.springframework.beans.factory.support.DefaultSingletonBeanRegistry.getSingleton(DefaultSingletonBeanRegistry.java:234) ~[na:na]
  	at org.springframework.beans.factory.support.AbstractBeanFactory.doGetBean(AbstractBeanFactory.java:333) ~[na:na]
  	at org.springframework.beans.factory.support.AbstractBeanFactory.getBean(AbstractBeanFactory.java:208) ~[na:na]
  	at org.springframework.beans.factory.config.DependencyDescriptor.resolveCandidate(DependencyDescriptor.java:276) ~[na:na]
  	at org.springframework.beans.factory.support.DefaultListableBeanFactory.doResolveDependency(DefaultListableBeanFactory.java:1380) ~[na:na]
  	at org.springframework.beans.factory.support.DefaultListableBeanFactory.resolveDependency(DefaultListableBeanFactory.java:1300) ~[na:na]
  	at org.springframework.beans.factory.support.ConstructorResolver.resolveAutowiredArgument(ConstructorResolver.java:887) ~[na:na]
  	at org.springframework.beans.factory.support.ConstructorResolver.createArgumentArray(ConstructorResolver.java:791) ~[na:na]
  	... 117 common frames omitted
  Caused by: org.springframework.beans.BeanInstantiationException: Failed to instantiate [org.springframework.boot.actuate.health.HealthContributorRegistry]: Factory method 'healthContributorRegistry' threw exception; nested exception is org.springframework.beans.factory.UnsatisfiedDependencyException: Error creating bean with name 'cassandraHealthContributor' defined in class path resource [org/springframework/boot/actuate/autoconfigure/cassandra/CassandraHealthContributorConfigurations$CassandraReactiveDriverConfiguration.class]: Unsatisfied dependency expressed through method 'cassandraHealthContributor' parameter 0; nested exception is org.springframework.beans.factory.BeanCreationException: Error creating bean with name 'session' defined in class path resource [com/mycompany/eventservice/config/CassandraConfig.class]: Invocation of init method failed; nested exception is com.typesafe.config.ConfigException$Missing: system properties: No configuration setting found for key 'datastax-java-driver'
  	at org.springframework.beans.factory.support.SimpleInstantiationStrategy.instantiate(SimpleInstantiationStrategy.java:185) ~[na:na]
  	at org.springframework.beans.factory.support.ConstructorResolver.instantiate(ConstructorResolver.java:653) ~[na:na]
  	... 131 common frames omitted
  Caused by: org.springframework.beans.factory.UnsatisfiedDependencyException: Error creating bean with name 'cassandraHealthContributor' defined in class path resource [org/springframework/boot/actuate/autoconfigure/cassandra/CassandraHealthContributorConfigurations$CassandraReactiveDriverConfiguration.class]: Unsatisfied dependency expressed through method 'cassandraHealthContributor' parameter 0; nested exception is org.springframework.beans.factory.BeanCreationException: Error creating bean with name 'session' defined in class path resource [com/mycompany/eventservice/config/CassandraConfig.class]: Invocation of init method failed; nested exception is com.typesafe.config.ConfigException$Missing: system properties: No configuration setting found for key 'datastax-java-driver'
  	at org.springframework.beans.factory.support.ConstructorResolver.createArgumentArray(ConstructorResolver.java:800) ~[na:na]
  	at org.springframework.beans.factory.support.ConstructorResolver.instantiateUsingFactoryMethod(ConstructorResolver.java:541) ~[na:na]
  	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.instantiateUsingFactoryMethod(AbstractAutowireCapableBeanFactory.java:1334) ~[na:na]
  	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.createBeanInstance(AbstractAutowireCapableBeanFactory.java:1177) ~[na:na]
  	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.doCreateBean(AbstractAutowireCapableBeanFactory.java:564) ~[na:na]
  	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.createBean(AbstractAutowireCapableBeanFactory.java:524) ~[na:na]
  	at org.springframework.beans.factory.support.AbstractBeanFactory.lambda$doGetBean$0(AbstractBeanFactory.java:335) ~[na:na]
  	at org.springframework.beans.factory.support.DefaultSingletonBeanRegistry.getSingleton(DefaultSingletonBeanRegistry.java:234) ~[na:na]
  	at org.springframework.beans.factory.support.AbstractBeanFactory.doGetBean(AbstractBeanFactory.java:333) ~[na:na]
  	at org.springframework.beans.factory.support.AbstractBeanFactory.getBean(AbstractBeanFactory.java:208) ~[na:na]
  	at org.springframework.beans.factory.support.DefaultListableBeanFactory.getBeansOfType(DefaultListableBeanFactory.java:671) ~[na:na]
  	at org.springframework.beans.factory.support.DefaultListableBeanFactory.getBeansOfType(DefaultListableBeanFactory.java:659) ~[na:na]
  	at org.springframework.context.support.AbstractApplicationContext.getBeansOfType(AbstractApplicationContext.java:1300) ~[na:na]
  	at org.springframework.boot.actuate.autoconfigure.health.HealthEndpointConfiguration$AdaptedReactiveHealthContributors.<init>(HealthEndpointConfiguration.java:141) ~[na:na]
  	at org.springframework.boot.actuate.autoconfigure.health.HealthEndpointConfiguration.healthContributorRegistry(HealthEndpointConfiguration.java:84) ~[com.mycompany.eventservice.EventServiceApplication:2.5.4]
  	at java.lang.reflect.Method.invoke(Method.java:566) ~[na:na]
  	at org.springframework.beans.factory.support.SimpleInstantiationStrategy.instantiate(SimpleInstantiationStrategy.java:154) ~[na:na]
  	... 132 common frames omitted
  Caused by: org.springframework.beans.factory.BeanCreationException: Error creating bean with name 'session' defined in class path resource [com/mycompany/eventservice/config/CassandraConfig.class]: Invocation of init method failed; nested exception is com.typesafe.config.ConfigException$Missing: system properties: No configuration setting found for key 'datastax-java-driver'
  	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.initializeBean(AbstractAutowireCapableBeanFactory.java:1786) ~[na:na]
  	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.doCreateBean(AbstractAutowireCapableBeanFactory.java:602) ~[na:na]
  	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.createBean(AbstractAutowireCapableBeanFactory.java:524) ~[na:na]
  	at org.springframework.beans.factory.support.AbstractBeanFactory.lambda$doGetBean$0(AbstractBeanFactory.java:335) ~[na:na]
  	at org.springframework.beans.factory.support.DefaultSingletonBeanRegistry.getSingleton(DefaultSingletonBeanRegistry.java:234) ~[na:na]
  	at org.springframework.beans.factory.support.AbstractBeanFactory.doGetBean(AbstractBeanFactory.java:333) ~[na:na]
  	at org.springframework.beans.factory.support.AbstractBeanFactory.getBean(AbstractBeanFactory.java:208) ~[na:na]
  	at org.springframework.beans.factory.config.DependencyDescriptor.resolveCandidate(DependencyDescriptor.java:276) ~[na:na]
  	at org.springframework.beans.factory.support.DefaultListableBeanFactory.addCandidateEntry(DefaultListableBeanFactory.java:1598) ~[na:na]
  	at org.springframework.beans.factory.support.DefaultListableBeanFactory.findAutowireCandidates(DefaultListableBeanFactory.java:1562) ~[na:na]
  	at org.springframework.beans.factory.support.DefaultListableBeanFactory.resolveMultipleBeans(DefaultListableBeanFactory.java:1481) ~[na:na]
  	at org.springframework.beans.factory.support.DefaultListableBeanFactory.doResolveDependency(DefaultListableBeanFactory.java:1338) ~[na:na]
  	at org.springframework.beans.factory.support.DefaultListableBeanFactory.resolveDependency(DefaultListableBeanFactory.java:1300) ~[na:na]
  	at org.springframework.beans.factory.support.ConstructorResolver.resolveAutowiredArgument(ConstructorResolver.java:887) ~[na:na]
  	at org.springframework.beans.factory.support.ConstructorResolver.createArgumentArray(ConstructorResolver.java:791) ~[na:na]
  	... 148 common frames omitted
  Caused by: com.typesafe.config.ConfigException$Missing: system properties: No configuration setting found for key 'datastax-java-driver'
  	at com.typesafe.config.impl.SimpleConfig.findKeyOrNull(SimpleConfig.java:157) ~[na:na]
  	at com.typesafe.config.impl.SimpleConfig.findOrNull(SimpleConfig.java:175) ~[na:na]
  	at com.typesafe.config.impl.SimpleConfig.find(SimpleConfig.java:189) ~[na:na]
  	at com.typesafe.config.impl.SimpleConfig.find(SimpleConfig.java:194) ~[na:na]
  	at com.typesafe.config.impl.SimpleConfig.getObject(SimpleConfig.java:269) ~[na:na]
  	at com.typesafe.config.impl.SimpleConfig.getConfig(SimpleConfig.java:275) ~[na:na]
  	at com.typesafe.config.impl.SimpleConfig.getConfig(SimpleConfig.java:42) ~[na:na]
  	at com.datastax.oss.driver.internal.core.config.typesafe.DefaultDriverConfigLoader.lambda$static$0(DefaultDriverConfigLoader.java:69) ~[na:na]
  	at com.datastax.oss.driver.internal.core.config.typesafe.DefaultDriverConfigLoader.<init>(DefaultDriverConfigLoader.java:196) ~[na:na]
  	at com.datastax.oss.driver.internal.core.config.typesafe.DefaultDriverConfigLoader.<init>(DefaultDriverConfigLoader.java:182) ~[na:na]
  	at com.datastax.oss.driver.internal.core.config.typesafe.DefaultDriverConfigLoader.<init>(DefaultDriverConfigLoader.java:150) ~[na:na]
  	at com.datastax.oss.driver.api.core.session.SessionBuilder.defaultConfigLoader(SessionBuilder.java:139) ~[na:na]
  	at com.datastax.oss.driver.api.core.session.SessionBuilder.buildDefaultSessionAsync(SessionBuilder.java:787) ~[na:na]
  	at com.datastax.oss.driver.api.core.session.SessionBuilder.buildAsync(SessionBuilder.java:755) ~[na:na]
  	at com.datastax.oss.driver.api.core.session.SessionBuilder.build(SessionBuilder.java:773) ~[na:na]
  	at org.springframework.data.cassandra.config.CqlSessionFactoryBean.buildSystemSession(CqlSessionFactoryBean.java:498) ~[com.mycompany.eventservice.EventServiceApplication:3.2.4]
  	at org.springframework.data.cassandra.config.CqlSessionFactoryBean.afterPropertiesSet(CqlSessionFactoryBean.java:451) ~[com.mycompany.eventservice.EventServiceApplication:3.2.4]
  	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.invokeInitMethods(AbstractAutowireCapableBeanFactory.java:1845) ~[na:na]
  	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.initializeBean(AbstractAutowireCapableBeanFactory.java:1782) ~[na:na]
  	... 162 common frames omitted
  
  2021-09-01 12:34:43.360 ERROR [event-service,,] 1 --- [           main] o.apache.catalina.core.StandardContext   : One or more Filters failed to start. Full details will be found in the appropriate container log file
  2021-09-01 12:34:43.364 ERROR [event-service,,] 1 --- [           main] o.apache.catalina.core.StandardContext   : Context [] startup failed due to previous errors
  2021-09-01 12:34:43.374  INFO [event-service,,] 1 --- [           main] o.apache.catalina.core.StandardService   : Stopping service [Tomcat]
  2021-09-01 12:34:43.376  WARN [event-service,,] 1 --- [           main] ConfigServletWebServerApplicationContext : Exception encountered during context initialization - cancelling refresh attempt: org.springframework.context.ApplicationContextException: Unable to start web server; nested exception is org.springframework.boot.web.server.WebServerException: Unable to start embedded Tomcat
  2021-09-01 12:34:43.384  INFO [event-service,,] 1 --- [           main] ConditionEvaluationReportLoggingListener :
  
  Error starting ApplicationContext. To display the conditions report re-run your application with 'debug' enabled.
  2021-09-01 12:34:43.388 ERROR [event-service,,] 1 --- [           main] o.s.boot.SpringApplication               : Application run failed
  
  org.springframework.context.ApplicationContextException: Unable to start web server; nested exception is org.springframework.boot.web.server.WebServerException: Unable to start embedded Tomcat
  	at org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext.onRefresh(ServletWebServerApplicationContext.java:163) ~[na:na]
  	at org.springframework.context.support.AbstractApplicationContext.refresh(AbstractApplicationContext.java:577) ~[na:na]
  	at org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext.refresh(ServletWebServerApplicationContext.java:145) ~[na:na]
  	at org.springframework.boot.SpringApplication.refresh(SpringApplication.java:754) ~[com.mycompany.eventservice.EventServiceApplication:2.5.4]
  	at org.springframework.boot.SpringApplication.refreshContext(SpringApplication.java:434) ~[com.mycompany.eventservice.EventServiceApplication:2.5.4]
  	at org.springframework.boot.SpringApplication.run(SpringApplication.java:338) ~[com.mycompany.eventservice.EventServiceApplication:2.5.4]
  	at org.springframework.boot.SpringApplication.run(SpringApplication.java:1343) ~[com.mycompany.eventservice.EventServiceApplication:2.5.4]
  	at org.springframework.boot.SpringApplication.run(SpringApplication.java:1332) ~[com.mycompany.eventservice.EventServiceApplication:2.5.4]
  	at com.mycompany.eventservice.EventServiceApplication.main(EventServiceApplication.java:19) ~[com.mycompany.eventservice.EventServiceApplication:na]
  Caused by: org.springframework.boot.web.server.WebServerException: Unable to start embedded Tomcat
  	at org.springframework.boot.web.embedded.tomcat.TomcatWebServer.initialize(TomcatWebServer.java:142) ~[na:na]
  	at org.springframework.boot.web.embedded.tomcat.TomcatWebServer.<init>(TomcatWebServer.java:104) ~[na:na]
  	at org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory.getTomcatWebServer(TomcatServletWebServerFactory.java:450) ~[com.mycompany.eventservice.EventServiceApplication:2.5.4]
  	at org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory.getWebServer(TomcatServletWebServerFactory.java:199) ~[com.mycompany.eventservice.EventServiceApplication:2.5.4]
  	at org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext.createWebServer(ServletWebServerApplicationContext.java:182) ~[na:na]
  	at org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext.onRefresh(ServletWebServerApplicationContext.java:160) ~[na:na]
  	... 8 common frames omitted
  Caused by: java.lang.IllegalStateException: StandardEngine[Tomcat].StandardHost[localhost].TomcatEmbeddedContext[] failed to start
  	at org.springframework.boot.web.embedded.tomcat.TomcatWebServer.rethrowDeferredStartupExceptions(TomcatWebServer.java:187) ~[na:na]
  	at org.springframework.boot.web.embedded.tomcat.TomcatWebServer.initialize(TomcatWebServer.java:126) ~[na:na]
  	... 13 common frames omitted
  ```

  In order to overcome the previous exception, I've set to `false` the property `management.health.cassandra.enabled`. After that, the following exception is thrown at startup
  ```
  ERROR [event-service,,] 1 --- [           main] o.s.boot.SpringApplication               : Application run failed
  
  org.springframework.beans.factory.UnsatisfiedDependencyException: Error creating bean with name 'userEventServiceImpl' defined in class path resource [com/mycompany/eventservice/service/UserEventServiceImpl.class]: Unsatisfied dependency expressed through constructor parameter 0; nested exception is org.springframework.beans.factory.NoSuchBeanDefinitionException: No qualifying bean of type 'com.mycompany.eventservice.repository.UserEventRepository' available: expected at least 1 bean which qualifies as autowire candidate. Dependency annotations: {}
  	at org.springframework.beans.factory.support.ConstructorResolver.createArgumentArray(ConstructorResolver.java:800) ~[na:na]
  	at org.springframework.beans.factory.support.ConstructorResolver.autowireConstructor(ConstructorResolver.java:229) ~[na:na]
  	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.autowireConstructor(AbstractAutowireCapableBeanFactory.java:1354) ~[na:na]
  	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.createBeanInstance(AbstractAutowireCapableBeanFactory.java:1204) ~[na:na]
  	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.doCreateBean(AbstractAutowireCapableBeanFactory.java:564) ~[na:na]
  	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.createBean(AbstractAutowireCapableBeanFactory.java:524) ~[na:na]
  	at org.springframework.beans.factory.support.AbstractBeanFactory.lambda$doGetBean$0(AbstractBeanFactory.java:335) ~[na:na]
  	at org.springframework.beans.factory.support.DefaultSingletonBeanRegistry.getSingleton(DefaultSingletonBeanRegistry.java:234) ~[na:na]
  	at org.springframework.beans.factory.support.AbstractBeanFactory.doGetBean(AbstractBeanFactory.java:333) ~[na:na]
  	at org.springframework.beans.factory.support.AbstractBeanFactory.getBean(AbstractBeanFactory.java:208) ~[na:na]
  	at org.springframework.beans.factory.support.DefaultListableBeanFactory.preInstantiateSingletons(DefaultListableBeanFactory.java:944) ~[na:na]
  	at org.springframework.context.support.AbstractApplicationContext.finishBeanFactoryInitialization(AbstractApplicationContext.java:918) ~[na:na]
  	at org.springframework.context.support.AbstractApplicationContext.refresh(AbstractApplicationContext.java:583) ~[na:na]
  	at org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext.refresh(ServletWebServerApplicationContext.java:145) ~[na:na]
  	at org.springframework.boot.SpringApplication.refresh(SpringApplication.java:754) ~[com.mycompany.eventservice.EventServiceApplication:2.5.4]
  	at org.springframework.boot.SpringApplication.refreshContext(SpringApplication.java:434) ~[com.mycompany.eventservice.EventServiceApplication:2.5.4]
  	at org.springframework.boot.SpringApplication.run(SpringApplication.java:338) ~[com.mycompany.eventservice.EventServiceApplication:2.5.4]
  	at org.springframework.boot.SpringApplication.run(SpringApplication.java:1343) ~[com.mycompany.eventservice.EventServiceApplication:2.5.4]
  	at org.springframework.boot.SpringApplication.run(SpringApplication.java:1332) ~[com.mycompany.eventservice.EventServiceApplication:2.5.4]
  	at com.mycompany.eventservice.EventServiceApplication.main(EventServiceApplication.java:19) ~[com.mycompany.eventservice.EventServiceApplication:na]
  Caused by: org.springframework.beans.factory.NoSuchBeanDefinitionException: No qualifying bean of type 'com.mycompany.eventservice.repository.UserEventRepository' available: expected at least 1 bean which qualifies as autowire candidate. Dependency annotations: {}
  	at org.springframework.beans.factory.support.DefaultListableBeanFactory.raiseNoMatchingBeanFound(DefaultListableBeanFactory.java:1790) ~[na:na]
  	at org.springframework.beans.factory.support.DefaultListableBeanFactory.doResolveDependency(DefaultListableBeanFactory.java:1346) ~[na:na]
  	at org.springframework.beans.factory.support.DefaultListableBeanFactory.resolveDependency(DefaultListableBeanFactory.java:1300) ~[na:na]
  	at org.springframework.beans.factory.support.ConstructorResolver.resolveAutowiredArgument(ConstructorResolver.java:887) ~[na:na]
  	at org.springframework.beans.factory.support.ConstructorResolver.createArgumentArray(ConstructorResolver.java:791) ~[na:na]
  	... 19 common frames omitted
  ```