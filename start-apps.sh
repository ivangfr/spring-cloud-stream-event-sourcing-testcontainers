#!/usr/bin/env bash

source scripts/my-functions.sh

echo
echo "Starting user-service..."

docker run -d --rm --name user-service -p 9080:9080 \
  -e SPRING_PROFILES_ACTIVE=${1:-default} -e MYSQL_HOST=mysql -e KAFKA_HOST=kafka -e KAFKA_PORT=9092 \
  -e SCHEMA_REGISTRY_HOST=schema-registry -e ZIPKIN_HOST=zipkin \
  --network=spring-cloud-stream-event-sourcing-testcontainers_default \
  --health-cmd="curl -f http://localhost:9080/actuator/health || exit 1" --health-start-period=1m \
  ivanfranchin/user-service:1.0.0

wait_for_container_log "user-service" "Started"

echo
echo "Starting event-service..."

docker run -d --rm --name event-service -p 9081:9081 \
  -e CASSANDRA_HOST=cassandra -e KAFKA_HOST=kafka -e KAFKA_PORT=9092 \
  -e SCHEMA_REGISTRY_HOST=schema-registry -e ZIPKIN_HOST=zipkin \
  --network=spring-cloud-stream-event-sourcing-testcontainers_default \
  --health-cmd="curl -f http://localhost:9081/actuator/health || exit 1" --health-start-period=1m \
  ivanfranchin/event-service:1.0.0

wait_for_container_log "event-service" "Started"

printf "\n"
printf "%14s | %37s |\n" "Application" "URL"
printf "%14s + %37s |\n" "--------------" "-------------------------------------"
printf "%14s | %37s |\n" "user-service" "http://localhost:9080/swagger-ui.html"
printf "%14s | %37s |\n" "event-service" "http://localhost:9081/swagger-ui.html"
printf "\n"
