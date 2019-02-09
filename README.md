# springboot-kafka-mysql-cassandra

## Goal

The goal of this project is to create a service that handles _users_ using
[`Event Sourcing`](https://martinfowler.com/eaaDev/EventSourcing.html). So, besides the traditional create/update/delete,
whenever an user is created/updated/deleted an event (informing this change) is sent to [`Kafka`](https://kafka.apache.org).
Furthermore, we will implement a service that will listen for those events and save them in
[`Cassandra`](http://cassandra.apache.org).

![project-diagram](images/project-diagram.png)

## Microservices

- `user-service`: spring-boot application responsible for handling users (create/update/delete). The users information
will be stored in [`MySQL`](https://www.mysql.com). Once an user is created/updated/deleted, one event is sent
to `Kafka`;

- `event-service`: spring-boot application responsible for listening events from `Kafka` and saving those events in
`Cassandra`.

## Start Environment

### Docker Compose

1. Open a terminal

2. Inside `/springboot-kafka-mysql-cassandra` root folder run
```
docker-compose up -d
```
> To stop and remove containers, networks and volumes type:
> ```
> docker-compose down -v
> ```

3. Wait a little bit until all containers are `Up (healthy)`. You can check their status running
```
docker-compose ps
```
The output will be something like
```
Name              Command                          State          Ports
---------------------------------------------------------------------------------------------------------------
event-cassandra            docker-entrypoint.sh cassa ...   Up (healthy)   7000/tcp, 7001/tcp, 0.0.0.0:7199->7199/tcp, 0.0.0.0:9042->9042/tcp, 0.0.0.0:9160->9160/tcp
kafka                      /etc/confluent/docker/run        Up (healthy)   0.0.0.0:29092->29092/tcp, 9092/tcp
kafka-manager              /kafka-manager/bin/kafka-m ...   Up             0.0.0.0:9000->9000/tcp
kafka-rest-proxy           /etc/confluent/docker/run        Up (healthy)   0.0.0.0:8082->8082/tcp
kafka-schema-registry-ui   /run.sh                          Up (healthy)   0.0.0.0:8001->8000/tcp
kafka-topics-ui            /run.sh                          Up (healthy)   0.0.0.0:8085->8000/tcp
schema-registry            /etc/confluent/docker/run        Up (healthy)   0.0.0.0:8081->8081/tcp
user-mysql                 docker-entrypoint.sh mysqld      Up (healthy)   0.0.0.0:3306->3306/tcp, 33060/tcp
zipkin                     /bin/bash -c test -n "$STO ...   Up (healthy)   9410/tcp, 0.0.0.0:9411->9411/tcp
zookeeper                  /etc/confluent/docker/run        Up (healthy)   0.0.0.0:2181->2181/tcp, 2888/tcp, 3888/tcp
```

## user-service

### Serialization format 

We can use [`JSON`](https://www.json.org) (default) or [`Avro`](https://avro.apache.org) format to serialize
data to the `binary` format used by Kafka. If `Avro` format is chosen, both services will benefit by the
[`Schema Registry`](https://docs.confluent.io/current/schema-registry/docs/index.html) that is running as Docker container.

### Start service

1. Open a new terminal

2. Inside `/springboot-kafka-mysql-cassandra` root folder run

- **For `JSON` serialization**
```
./gradlew user-service:bootRun
```

- **For `Avro` serialization**
```
./gradlew user-service:bootRun -Dspring.profiles.active=avro
```

## event-service

### Deserialization

Differently from `user-service`, `event-service` has no specific profile to select the deserialization format.
Spring Cloud Stream provides a stack of `MessageConverters` that handle the conversion of many different types of
content-types, including `application/json`. Besides, as `event-service` has `SchemaRegistryClient` bean registered,
Spring Cloud Stream auto configures an Apache Avro message converter for schema management.

In order to handle different content-types, Spring Cloud Stream  has a "content-type negotiation and transformation"
strategy (https://docs.spring.io/spring-cloud-stream/docs/current/reference/htmlsingle/#content-type-management). The
precedence orders are: 1st, content-type present in the message header; 2nd content-type defined in the binding;
and finally, content-type is `application/json` (default).

The producer (in the case `user-service`) always sets the content-type in the message header. The content-type can be
`application/json` or `application/*+avro`, depending on with which profile `user-service` is started.

### Start service

1. Open a new terminal

2. Inside `/springboot-kafka-mysql-cassandra` root folder run
```
./gradlew event-service:bootRun
``` 

## How to test

1. Open `user-service` swagger link: http://localhost:9080/swagger-ui.html

![user-service](images/user-service.png)

2. Create a new user, `POST /api/users`

3. Open `event-service` swagger link: http://localhost:9081/swagger-ui.html

![event-service](images/event-service.png)

4. Get all events related to the user created, informing the user id `GET /api/events/users/{id}`

5. You can also check how the event was sent by `user-service` and listened by `event-service` (as shown on the image
below) using [`Zipkin`](https://zipkin.io): http://localhost:9411

![zipkin](images/zipkin.png)

6. Create new users and update/delete existing ones in order to see how the application works.

## Useful Commands & Links

### MySQL Database
```
docker exec -it user-mysql bash -c 'mysql -uroot -psecret'
use userdb;
select * from users;
```

### Cassandra Database
```
docker exec -it event-cassandra cqlsh
USE mycompany;
SELECT * FROM user_events; 
```

### Kafka Topics UI

- Kafka Topics UI can be accessed at http://localhost:8085

![kafka-topics-ui](images/kafka-topics-ui.png)

### Schema Registry UI

- Schema Registry UI can be accessed at http://localhost:8001

![schema-registry-ui](images/schema-registry-ui.png)

### Kafka Manager

1. Kafka Manager can be accessed at http://localhost:9000

2. First, you must create a new cluster. Click on `Cluster` (dropdown on the header) and then on `Add Cluster`

3. Type on `Cluster Name` field the name of your cluster, for example: `MyZooCluster`

4. On `Cluster Zookeeper Hosts` field type: `zookeeper:2181`

5. Click on `Save` button on the bottom of the page. Done!

6. The image below shows the topics present on Kafka, including the topic `com.mycompany.userservice.user` with `2`
partitions, that is used by the microservices of this project.

![kafka-manager](images/kafka-manager.png)

## References

- https://docs.spring.io/spring-cloud-stream/docs/current/reference/htmlsingle/

## Issues

- even though `userJson` is defined in `user-event.avsc` to allow having `null` values, when a delete event arrives to
`event-service` with `userJson=null`, it throws the exception

```
2019-01-22 20:51:51.404 ERROR [event-service,1a9df23f321b6ff7,340692a2a2efd4a2,true] 2044 --- [container-0-C-1] o.s.integration.handler.LoggingHandler   : org.springframework.messaging.MessagingException: Exception thrown while invoking com.mycompany.eventservice.bus.UserStream#process[1 args]; nested exception is org.apache.avro.AvroTypeException: Found null, expecting string, failedMessage=GenericMessage [payload=byte[46], headers={deliveryAttempt=3, X-B3-ParentSpanId=a3c407ce0e55799d, kafka_timestampType=CREATE_TIME, scst_partition=1, kafka_receivedTopic=com.mycompany.userservice.user, spanTraceId=1a9df23f321b6ff7, spanId=8ea1d31d9891b97f, spanParentSpanId=a3c407ce0e55799d, nativeHeaders={spanTraceId=[1a9df23f321b6ff7], spanId=[340692a2a2efd4a2], spanParentSpanId=[d854c8b1d2ac5608], spanSampled=[1], X-B3-TraceId=[1a9df23f321b6ff7], X-B3-SpanId=[340692a2a2efd4a2], X-B3-ParentSpanId=[d854c8b1d2ac5608], X-B3-Sampled=[1]}, kafka_offset=2, partitionKey=1, X-B3-SpanId=8ea1d31d9891b97f, scst_nativeHeadersPresent=true, kafka_consumer=org.apache.kafka.clients.consumer.KafkaConsumer@50858d0b, X-B3-Sampled=1, X-B3-TraceId=1a9df23f321b6ff7, id=577d9318-7453-c482-392f-91529fb6e80b, spanSampled=1, kafka_receivedPartitionId=1, contentType=application/vnd.usereventbus.v1+avro, kafka_receivedTimestamp=1548186708364, timestamp=1548186711394}]
        at org.springframework.cloud.stream.binding.StreamListenerMessageHandler.handleRequestMessage(StreamListenerMessageHandler.java:63)
        at org.springframework.integration.handler.AbstractReplyProducingMessageHandler.handleMessageInternal(AbstractReplyProducingMessageHandler.java:123)
        at org.springframework.integration.handler.AbstractMessageHandler.handleMessage(AbstractMessageHandler.java:162)
        at org.springframework.integration.dispatcher.AbstractDispatcher.tryOptimizedDispatch(AbstractDispatcher.java:115)
        at org.springframework.integration.dispatcher.UnicastingDispatcher.doDispatch(UnicastingDispatcher.java:132)
        at org.springframework.integration.dispatcher.UnicastingDispatcher.dispatch(UnicastingDispatcher.java:105)
        at org.springframework.integration.channel.AbstractSubscribableChannel.doSend(AbstractSubscribableChannel.java:73)
        at org.springframework.integration.channel.AbstractMessageChannel.send(AbstractMessageChannel.java:453)
        at org.springframework.integration.channel.AbstractMessageChannel.send(AbstractMessageChannel.java:401)
        at org.springframework.messaging.core.GenericMessagingTemplate.doSend(GenericMessagingTemplate.java:187)
        at org.springframework.messaging.core.GenericMessagingTemplate.doSend(GenericMessagingTemplate.java:166)
        at org.springframework.messaging.core.GenericMessagingTemplate.doSend(GenericMessagingTemplate.java:47)
        at org.springframework.messaging.core.AbstractMessageSendingTemplate.send(AbstractMessageSendingTemplate.java:109)
        at org.springframework.integration.endpoint.MessageProducerSupport.sendMessage(MessageProducerSupport.java:205)
        at org.springframework.integration.kafka.inbound.KafkaMessageDrivenChannelAdapter.sendMessageIfAny(KafkaMessageDrivenChannelAdapter.java:369)
        at org.springframework.integration.kafka.inbound.KafkaMessageDrivenChannelAdapter.access$400(KafkaMessageDrivenChannelAdapter.java:74)
        at org.springframework.integration.kafka.inbound.KafkaMessageDrivenChannelAdapter$IntegrationRecordMessageListener.onMessage(KafkaMessageDrivenChannelAdapter.java:431)
        at org.springframework.integration.kafka.inbound.KafkaMessageDrivenChannelAdapter$IntegrationRecordMessageListener.onMessage(KafkaMessageDrivenChannelAdapter.java:402)
        at org.springframework.kafka.listener.adapter.RetryingMessageListenerAdapter.lambda$onMessage$0(RetryingMessageListenerAdapter.java:120)
        at org.springframework.retry.support.RetryTemplate.doExecute(RetryTemplate.java:287)
        at org.springframework.retry.support.RetryTemplate.execute(RetryTemplate.java:211)
        at org.springframework.kafka.listener.adapter.RetryingMessageListenerAdapter.onMessage(RetryingMessageListenerAdapter.java:114)
        at org.springframework.kafka.listener.adapter.RetryingMessageListenerAdapter.onMessage(RetryingMessageListenerAdapter.java:40)
        at org.springframework.kafka.listener.KafkaMessageListenerContainer$ListenerConsumer.doInvokeOnMessage(KafkaMessageListenerContainer.java:1220)
        at org.springframework.kafka.listener.KafkaMessageListenerContainer$ListenerConsumer.invokeOnMessage(KafkaMessageListenerContainer.java:1213)
        at org.springframework.kafka.listener.KafkaMessageListenerContainer$ListenerConsumer.doInvokeRecordListener(KafkaMessageListenerContainer.java:1174)
        at org.springframework.kafka.listener.KafkaMessageListenerContainer$ListenerConsumer.doInvokeWithRecords(KafkaMessageListenerContainer.java:1155)
        at org.springframework.kafka.listener.KafkaMessageListenerContainer$ListenerConsumer.invokeRecordListener(KafkaMessageListenerContainer.java:1096)
        at org.springframework.kafka.listener.KafkaMessageListenerContainer$ListenerConsumer.invokeListener(KafkaMessageListenerContainer.java:924)
        at org.springframework.kafka.listener.KafkaMessageListenerContainer$ListenerConsumer.pollAndInvoke(KafkaMessageListenerContainer.java:740)
        at org.springframework.kafka.listener.KafkaMessageListenerContainer$ListenerConsumer.run(KafkaMessageListenerContainer.java:689)
        at java.util.concurrent.Executors$RunnableAdapter.call(Executors.java:511)
        at java.util.concurrent.FutureTask.run(FutureTask.java:266)
        at java.lang.Thread.run(Thread.java:745)
Caused by: org.apache.avro.AvroTypeException: Found null, expecting string
        at org.apache.avro.io.ResolvingDecoder.doAction(ResolvingDecoder.java:292)
        at org.apache.avro.io.parsing.Parser.advance(Parser.java:88)
        at org.apache.avro.io.ResolvingDecoder.readString(ResolvingDecoder.java:196)
        at org.apache.avro.generic.GenericDatumReader.readString(GenericDatumReader.java:422)
        at org.apache.avro.reflect.ReflectDatumReader.readString(ReflectDatumReader.java:229)
        at org.apache.avro.generic.GenericDatumReader.readString(GenericDatumReader.java:414)
        at org.apache.avro.generic.GenericDatumReader.readWithoutConversion(GenericDatumReader.java:181)
        at org.apache.avro.reflect.ReflectDatumReader.readField(ReflectDatumReader.java:302)
        at org.apache.avro.generic.GenericDatumReader.readRecord(GenericDatumReader.java:222)
        at org.apache.avro.generic.GenericDatumReader.readWithoutConversion(GenericDatumReader.java:175)
        at org.apache.avro.generic.GenericDatumReader.read(GenericDatumReader.java:153)
        at org.apache.avro.generic.GenericDatumReader.read(GenericDatumReader.java:145)
        at org.springframework.cloud.stream.schema.avro.AbstractAvroMessageConverter.convertFromInternal(AbstractAvroMessageConverter.java:101)
        at org.springframework.messaging.converter.AbstractMessageConverter.fromMessage(AbstractMessageConverter.java:181)
        at org.springframework.messaging.converter.CompositeMessageConverter.fromMessage(CompositeMessageConverter.java:70)
        at org.springframework.cloud.stream.config.SmartMessageMethodArgumentResolver.convertPayload(SmartMessageMethodArgumentResolver.java:110)
        at org.springframework.cloud.stream.config.SmartMessageMethodArgumentResolver.resolveArgument(SmartMessageMethodArgumentResolver.java:81)
        at org.springframework.messaging.handler.invocation.HandlerMethodArgumentResolverComposite.resolveArgument(HandlerMethodArgumentResolverComposite.java:117)
        at org.springframework.messaging.handler.invocation.InvocableHandlerMethod.getMethodArgumentValues(InvocableHandlerMethod.java:147)
        at org.springframework.messaging.handler.invocation.InvocableHandlerMethod.invoke(InvocableHandlerMethod.java:116)
        at org.springframework.cloud.stream.binding.StreamListenerMessageHandler.handleRequestMessage(StreamListenerMessageHandler.java:55)
        ... 33 more

2019-01-22 20:51:51.436 ERROR [event-service,,,] 2044 --- [container-0-C-1] o.s.kafka.listener.LoggingErrorHandler   : Error while processing: ConsumerRecord(topic = com.mycompany.userservice.user, partition = 1, offset = 2, CreateTime = 1548186708364, serialized key size = -1, serialized value size = 46, headers = RecordHeaders(headers = [RecordHeader(key = X-B3-ParentSpanId, value = [34, 49, 97, 57, 100, 102, 50, 51, 102, 51, 50, 49, 98, 54, 102, 102, 55, 34]), RecordHeader(key = scst_partition, value = [49]), RecordHeader(key = spanTraceId, value = [34, 49, 97, 57, 100, 102, 50, 51, 102, 51, 50, 49, 98, 54, 102, 102, 55, 34]), RecordHeader(key = spanId, value = [34, 97, 51, 99, 52, 48, 55, 99, 101, 48, 101, 53, 53, 55, 57, 57, 100, 34]), RecordHeader(key = spanParentSpanId, value = [34, 49, 97, 57, 100, 102, 50, 51, 102, 51, 50, 49, 98, 54, 102, 102, 55, 34]), RecordHeader(key = nativeHeaders, value = [123, 34, 115, 112, 97, 110, 84, 114, 97, 99, 101, 73, 100, 34, 58, 91, 34, 49, 97, 57, 100, 102, 50, 51, 102, 51, 50, 49, 98, 54, 102, 102, 55, 34, 93, 44, 34, 115, 112, 97, 110, 73, 100, 34, 58, 91, 34, 97, 51, 99, 52, 48, 55, 99, 101, 48, 101, 53, 53, 55, 57, 57, 100, 34, 93, 44, 34, 115, 112, 97, 110, 80, 97, 114, 101, 110, 116, 83, 112, 97, 110, 73, 100, 34, 58, 91, 34, 49, 97, 57, 100, 102, 50, 51, 102, 51, 50, 49, 98, 54, 102, 102, 55, 34, 93, 44, 34, 115, 112, 97, 110, 83, 97, 109, 112, 108, 101, 100, 34, 58, 91, 34, 49, 34, 93, 125]), RecordHeader(key = partitionKey, value = [49]), RecordHeader(key = X-B3-SpanId, value = [34, 97, 51, 99, 52, 48, 55, 99, 101, 48, 101, 53, 53, 55, 57, 57, 100, 34]), RecordHeader(key = X-B3-Sampled, value = [34, 49, 34]), RecordHeader(key = X-B3-TraceId, value = [34, 49, 97, 57, 100, 102, 50, 51, 102, 51, 50, 49, 98, 54, 102, 102, 55, 34]), RecordHeader(key = spanSampled, value = [34, 49, 34]), RecordHeader(key = contentType, value = [34, 97, 112, 112, 108, 105, 99, 97, 116, 105, 111, 110, 47, 118, 110, 100, 46, 117, 115, 101, 114, 101, 118, 101, 110, 116, 98, 117, 115, 46, 118, 49, 43, 97, 118, 114, 111, 34]), RecordHeader(key = spring_json_header_types, value = [123, 34, 115, 112, 97, 110, 84, 114, 97, 99, 101, 73, 100, 34, 58, 34, 106, 97, 118, 97, 46, 108, 97, 110, 103, 46, 83, 116, 114, 105, 110, 103, 34, 44, 34, 115, 112, 97, 110, 73, 100, 34, 58, 34, 106, 97, 118, 97, 46, 108, 97, 110, 103, 46, 83, 116, 114, 105, 110, 103, 34, 44, 34, 115, 112, 97, 110, 80, 97, 114, 101, 110, 116, 83, 112, 97, 110, 73, 100, 34, 58, 34, 106, 97, 118, 97, 46, 108, 97, 110, 103, 46, 83, 116, 114, 105, 110, 103, 34, 44, 34, 110, 97, 116, 105, 118, 101, 72, 101, 97, 100, 101, 114, 115, 34, 58, 34, 111, 114, 103, 46, 115, 112, 114, 105, 110, 103, 102, 114, 97, 109, 101, 119, 111, 114, 107, 46, 117, 116, 105, 108, 46, 76, 105, 110, 107, 101, 100, 77, 117, 108, 116, 105, 86, 97, 108, 117, 101, 77, 97, 112, 34, 44, 34, 112, 97, 114, 116, 105, 116, 105, 111, 110, 75, 101, 121, 34, 58, 34, 106, 97, 118, 97, 46, 108, 97, 110, 103, 46, 76, 111, 110, 103, 34, 44, 34, 88, 45, 66, 51, 45, 83, 112, 97, 110, 73, 100, 34, 58, 34, 106, 97, 118, 97, 46, 108, 97, 110, 103, 46, 83, 116, 114, 105, 110, 103, 34, 44, 34, 88, 45, 66, 51, 45, 80, 97, 114, 101, 110, 116, 83, 112, 97, 110, 73, 100, 34, 58, 34, 106, 97, 118, 97, 46, 108, 97, 110, 103, 46, 83, 116, 114, 105, 110, 103, 34, 44, 34, 115, 99, 115, 116, 95, 112, 97, 114, 116, 105, 116, 105, 111, 110, 34, 58, 34, 106, 97, 118, 97, 46, 108, 97, 110, 103, 46, 73, 110, 116, 101, 103, 101, 114, 34, 44, 34, 88, 45, 66, 51, 45, 83, 97, 109, 112, 108, 101, 100, 34, 58, 34, 106, 97, 118, 97, 46, 108, 97, 110, 103, 46, 83, 116, 114, 105, 110, 103, 34, 44, 34, 88, 45, 66, 51, 45, 84, 114, 97, 99, 101, 73, 100, 34, 58, 34, 106, 97, 118, 97, 46, 108, 97, 110, 103, 46, 83, 116, 114, 105, 110, 103, 34, 44, 34, 115, 112, 97, 110, 83, 97, 109, 112, 108, 101, 100, 34, 58, 34, 106, 97, 118, 97, 46, 108, 97, 110, 103, 46, 83, 116, 114, 105, 110, 103, 34, 44, 34, 99, 111, 110, 116, 101, 110, 116, 84, 121, 112, 101, 34, 58, 34, 106, 97, 118, 97, 46, 108, 97, 110, 103, 46, 83, 116, 114, 105, 110, 103, 34, 125])], isReadOnly = false), key = null, value = [B@67e89877)

org.springframework.messaging.MessagingException: Exception thrown while invoking com.mycompany.eventservice.bus.UserStream#process[1 args]; nested exception is org.apache.avro.AvroTypeException: Found null, expecting string
        at org.springframework.cloud.stream.binding.StreamListenerMessageHandler.handleRequestMessage(StreamListenerMessageHandler.java:63) ~[spring-cloud-stream-2.1.0.RC4.jar:2.1.0.RC4]
        at org.springframework.integration.handler.AbstractReplyProducingMessageHandler.handleMessageInternal(AbstractReplyProducingMessageHandler.java:123) ~[spring-integration-core-5.1.2.RELEASE.jar:5.1.2.RELEASE]
        at org.springframework.integration.handler.AbstractMessageHandler.handleMessage(AbstractMessageHandler.java:162) ~[spring-integration-core-5.1.2.RELEASE.jar:5.1.2.RELEASE]
        at org.springframework.integration.dispatcher.AbstractDispatcher.tryOptimizedDispatch(AbstractDispatcher.java:115) ~[spring-integration-core-5.1.2.RELEASE.jar:5.1.2.RELEASE]
        at org.springframework.integration.dispatcher.UnicastingDispatcher.doDispatch(UnicastingDispatcher.java:132) ~[spring-integration-core-5.1.2.RELEASE.jar:5.1.2.RELEASE]
        at org.springframework.integration.dispatcher.UnicastingDispatcher.dispatch(UnicastingDispatcher.java:105) ~[spring-integration-core-5.1.2.RELEASE.jar:5.1.2.RELEASE]
        at org.springframework.integration.channel.AbstractSubscribableChannel.doSend(AbstractSubscribableChannel.java:73) ~[spring-integration-core-5.1.2.RELEASE.jar:5.1.2.RELEASE]
        at org.springframework.integration.channel.AbstractMessageChannel.send(AbstractMessageChannel.java:453) ~[spring-integration-core-5.1.2.RELEASE.jar:5.1.2.RELEASE]
        at org.springframework.integration.channel.AbstractMessageChannel.send(AbstractMessageChannel.java:401) ~[spring-integration-core-5.1.2.RELEASE.jar:5.1.2.RELEASE]
        at org.springframework.messaging.core.GenericMessagingTemplate.doSend(GenericMessagingTemplate.java:187) ~[spring-messaging-5.1.4.RELEASE.jar:5.1.4.RELEASE]
        at org.springframework.messaging.core.GenericMessagingTemplate.doSend(GenericMessagingTemplate.java:166) ~[spring-messaging-5.1.4.RELEASE.jar:5.1.4.RELEASE]
        at org.springframework.messaging.core.GenericMessagingTemplate.doSend(GenericMessagingTemplate.java:47) ~[spring-messaging-5.1.4.RELEASE.jar:5.1.4.RELEASE]
        at org.springframework.messaging.core.AbstractMessageSendingTemplate.send(AbstractMessageSendingTemplate.java:109) ~[spring-messaging-5.1.4.RELEASE.jar:5.1.4.RELEASE]
        at org.springframework.integration.endpoint.MessageProducerSupport.sendMessage(MessageProducerSupport.java:205) ~[spring-integration-core-5.1.2.RELEASE.jar:5.1.2.RELEASE]
        at org.springframework.integration.kafka.inbound.KafkaMessageDrivenChannelAdapter.sendMessageIfAny(KafkaMessageDrivenChannelAdapter.java:369) ~[spring-integration-kafka-3.1.0.RELEASE.jar:3.1.0.RELEASE]
        at org.springframework.integration.kafka.inbound.KafkaMessageDrivenChannelAdapter.access$400(KafkaMessageDrivenChannelAdapter.java:74) ~[spring-integration-kafka-3.1.0.RELEASE.jar:3.1.0.RELEASE]
        at org.springframework.integration.kafka.inbound.KafkaMessageDrivenChannelAdapter$IntegrationRecordMessageListener.onMessage(KafkaMessageDrivenChannelAdapter.java:431) ~[spring-integration-kafka-3.1.0.RELEASE.jar:3.1.0.RELEASE]
        at org.springframework.integration.kafka.inbound.KafkaMessageDrivenChannelAdapter$IntegrationRecordMessageListener.onMessage(KafkaMessageDrivenChannelAdapter.java:402) ~[spring-integration-kafka-3.1.0.RELEASE.jar:3.1.0.RELEASE]
        at org.springframework.kafka.listener.adapter.RetryingMessageListenerAdapter.lambda$onMessage$0(RetryingMessageListenerAdapter.java:120) ~[spring-kafka-2.2.3.RELEASE.jar:2.2.3.RELEASE]
        at org.springframework.retry.support.RetryTemplate.doExecute(RetryTemplate.java:287) ~[spring-retry-1.2.3.RELEASE.jar:na]
        at org.springframework.retry.support.RetryTemplate.execute(RetryTemplate.java:211) ~[spring-retry-1.2.3.RELEASE.jar:na]
        at org.springframework.kafka.listener.adapter.RetryingMessageListenerAdapter.onMessage(RetryingMessageListenerAdapter.java:114) ~[spring-kafka-2.2.3.RELEASE.jar:2.2.3.RELEASE]
        at org.springframework.kafka.listener.adapter.RetryingMessageListenerAdapter.onMessage(RetryingMessageListenerAdapter.java:40) ~[spring-kafka-2.2.3.RELEASE.jar:2.2.3.RELEASE]
        at org.springframework.kafka.listener.KafkaMessageListenerContainer$ListenerConsumer.doInvokeOnMessage(KafkaMessageListenerContainer.java:1220) [spring-kafka-2.2.3.RELEASE.jar:2.2.3.RELEASE]
        at org.springframework.kafka.listener.KafkaMessageListenerContainer$ListenerConsumer.invokeOnMessage(KafkaMessageListenerContainer.java:1213) [spring-kafka-2.2.3.RELEASE.jar:2.2.3.RELEASE]
        at org.springframework.kafka.listener.KafkaMessageListenerContainer$ListenerConsumer.doInvokeRecordListener(KafkaMessageListenerContainer.java:1174) [spring-kafka-2.2.3.RELEASE.jar:2.2.3.RELEASE]
        at org.springframework.kafka.listener.KafkaMessageListenerContainer$ListenerConsumer.doInvokeWithRecords(KafkaMessageListenerContainer.java:1155) [spring-kafka-2.2.3.RELEASE.jar:2.2.3.RELEASE]
        at org.springframework.kafka.listener.KafkaMessageListenerContainer$ListenerConsumer.invokeRecordListener(KafkaMessageListenerContainer.java:1096) [spring-kafka-2.2.3.RELEASE.jar:2.2.3.RELEASE]
        at org.springframework.kafka.listener.KafkaMessageListenerContainer$ListenerConsumer.invokeListener(KafkaMessageListenerContainer.java:924) [spring-kafka-2.2.3.RELEASE.jar:2.2.3.RELEASE]
        at org.springframework.kafka.listener.KafkaMessageListenerContainer$ListenerConsumer.pollAndInvoke(KafkaMessageListenerContainer.java:740) [spring-kafka-2.2.3.RELEASE.jar:2.2.3.RELEASE]
        at org.springframework.kafka.listener.KafkaMessageListenerContainer$ListenerConsumer.run(KafkaMessageListenerContainer.java:689) [spring-kafka-2.2.3.RELEASE.jar:2.2.3.RELEASE]
        at java.util.concurrent.Executors$RunnableAdapter.call(Executors.java:511) [na:1.8.0_102]
        at java.util.concurrent.FutureTask.run(FutureTask.java:266) [na:1.8.0_102]
        at java.lang.Thread.run(Thread.java:745) [na:1.8.0_102]
Caused by: org.apache.avro.AvroTypeException: Found null, expecting string
        at org.apache.avro.io.ResolvingDecoder.doAction(ResolvingDecoder.java:292) ~[avro-1.8.2.jar:1.8.2]
        at org.apache.avro.io.parsing.Parser.advance(Parser.java:88) ~[avro-1.8.2.jar:1.8.2]
        at org.apache.avro.io.ResolvingDecoder.readString(ResolvingDecoder.java:196) ~[avro-1.8.2.jar:1.8.2]
        at org.apache.avro.generic.GenericDatumReader.readString(GenericDatumReader.java:422) ~[avro-1.8.2.jar:1.8.2]
        at org.apache.avro.reflect.ReflectDatumReader.readString(ReflectDatumReader.java:229) ~[avro-1.8.2.jar:1.8.2]
        at org.apache.avro.generic.GenericDatumReader.readString(GenericDatumReader.java:414) ~[avro-1.8.2.jar:1.8.2]
        at org.apache.avro.generic.GenericDatumReader.readWithoutConversion(GenericDatumReader.java:181) ~[avro-1.8.2.jar:1.8.2]
        at org.apache.avro.reflect.ReflectDatumReader.readField(ReflectDatumReader.java:302) ~[avro-1.8.2.jar:1.8.2]
        at org.apache.avro.generic.GenericDatumReader.readRecord(GenericDatumReader.java:222) ~[avro-1.8.2.jar:1.8.2]
        at org.apache.avro.generic.GenericDatumReader.readWithoutConversion(GenericDatumReader.java:175) ~[avro-1.8.2.jar:1.8.2]
        at org.apache.avro.generic.GenericDatumReader.read(GenericDatumReader.java:153) ~[avro-1.8.2.jar:1.8.2]
        at org.apache.avro.generic.GenericDatumReader.read(GenericDatumReader.java:145) ~[avro-1.8.2.jar:1.8.2]
        at org.springframework.cloud.stream.schema.avro.AbstractAvroMessageConverter.convertFromInternal(AbstractAvroMessageConverter.java:101) ~[spring-cloud-stream-schema-2.1.0.RC4.jar:2.1.0.RC4]
        at org.springframework.messaging.converter.AbstractMessageConverter.fromMessage(AbstractMessageConverter.java:181) ~[spring-messaging-5.1.4.RELEASE.jar:5.1.4.RELEASE]
        at org.springframework.messaging.converter.CompositeMessageConverter.fromMessage(CompositeMessageConverter.java:70) ~[spring-messaging-5.1.4.RELEASE.jar:5.1.4.RELEASE]
        at org.springframework.cloud.stream.config.SmartMessageMethodArgumentResolver.convertPayload(SmartMessageMethodArgumentResolver.java:110) ~[spring-cloud-stream-2.1.0.RC4.jar:2.1.0.RC4]
        at org.springframework.cloud.stream.config.SmartMessageMethodArgumentResolver.resolveArgument(SmartMessageMethodArgumentResolver.java:81) ~[spring-cloud-stream-2.1.0.RC4.jar:2.1.0.RC4]
        at org.springframework.messaging.handler.invocation.HandlerMethodArgumentResolverComposite.resolveArgument(HandlerMethodArgumentResolverComposite.java:117) ~[spring-messaging-5.1.4.RELEASE.jar:5.1.4.RELEASE]
        at org.springframework.messaging.handler.invocation.InvocableHandlerMethod.getMethodArgumentValues(InvocableHandlerMethod.java:147) ~[spring-messaging-5.1.4.RELEASE.jar:5.1.4.RELEASE]
        at org.springframework.messaging.handler.invocation.InvocableHandlerMethod.invoke(InvocableHandlerMethod.java:116) ~[spring-messaging-5.1.4.RELEASE.jar:5.1.4.RELEASE]
        at org.springframework.cloud.stream.binding.StreamListenerMessageHandler.handleRequestMessage(StreamListenerMessageHandler.java:55) ~[spring-cloud-stream-2.1.0.RC4.jar:2.1.0.RC4]
        ... 33 common frames omitted
```