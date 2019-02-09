FROM openjdk:8-jdk-alpine

LABEL maintainer="ivangfr@yahoo.com.br"

ARG JAR_FILE

ENV MYSQL_ADDR mysql
ENV KAFKA_ADDR=kafka KAFKA_PORT=9092
ENV SCHEMA_REGISTRY_ADDR schema-registry
ENV ZIPKIN_ADDR zipkin

RUN apk update && apk add ca-certificates wget && update-ca-certificates

ADD ${JAR_FILE} /user-service.jar

CMD ["java", "-jar", "/user-service.jar"]

EXPOSE 9080