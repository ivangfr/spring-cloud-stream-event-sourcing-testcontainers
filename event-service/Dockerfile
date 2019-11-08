FROM openjdk:8-jre-alpine

LABEL maintainer="ivangfr@yahoo.com.br"

RUN apk --no-cache add curl

ARG JAR_FILE
COPY ${JAR_FILE} /event-service.jar

CMD ["java", "-jar", "/event-service.jar"]

EXPOSE 8080