#!/usr/bin/env bash

echo
echo "Starting user-service..."

docker run -d --rm --name user-service \
  -p 9080:8080 --network=springboot-kafka-mysql-cassandra_default \
  -e SPRING_PROFILES_ACTIVE=${1:-default} -e MYSQL_HOST=mysql -e KAFKA_HOST=kafka -e KAFKA_PORT=9092\
  -e SCHEMA_REGISTRY_HOST=schema-registry -e ZIPKIN_HOST=zipkin \
  --health-cmd="curl -f http://localhost:8080/actuator/health || exit 1" --health-start-period=1m \
  docker.mycompany.com/user-service:1.0.0

sleep 5

echo
echo "Starting event-service..."

docker run -d --rm --name event-service \
  -p 9081:8080 --network=springboot-kafka-mysql-cassandra_default \
  -e CASSANDRA_HOST=cassandra -e KAFKA_HOST=kafka -e KAFKA_PORT=9092 \
  -e SCHEMA_REGISTRY_HOST=schema-registry -e ZIPKIN_HOST=zipkin \
  --health-cmd="curl -f http://localhost:8080/actuator/health || exit 1" --health-start-period=1m \
  docker.mycompany.com/event-service:1.0.0

sleep 5

printf "\n"
printf "%14s | %37s |\n" "Application" "URL"
printf "%14s + %37s |\n" "--------------" "-------------------------------------"
printf "%14s | %37s |\n" "user-service" "http://localhost:9080/swagger-ui.html"
printf "%14s | %37s |\n" "event-service" "http://localhost:9081/swagger-ui.html"
printf "\n"
