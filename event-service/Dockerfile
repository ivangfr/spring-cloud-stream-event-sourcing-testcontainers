FROM openjdk:8-jdk-alpine

ARG JAR_FILE

ENV CASSANDRA_ADDR cassandra
ENV KAFKA_ADDR=kafka KAFKA_PORT=9092
ENV SCHEMA_REGISTRY_ADDR schema-registry
ENV ZIPKIN_ADDR zipkin

LABEL maintainer="ivangfr@yahoo.com.br"

RUN apk update && apk add ca-certificates wget && update-ca-certificates

COPY ${JAR_FILE} /event-service.jar

CMD ["java", "-jar", "/event-service.jar"]

EXPOSE 9081