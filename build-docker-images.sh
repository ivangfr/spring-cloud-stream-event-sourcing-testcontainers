#!/usr/bin/env bash

DOCKER_IMAGE_PREFIX="ivanfranchin"
APP_VERSION="1.0.0"

USER_SERVICE_APP_NAME="user-service"
EVENT_SERVICE_APP_NAME="event-service"

USER_SERVICE_DOCKER_IMAGE_NAME="${DOCKER_IMAGE_PREFIX}/${USER_SERVICE_APP_NAME}:${APP_VERSION}"
EVENT_SERVICE_DOCKER_IMAGE_NAME="${DOCKER_IMAGE_PREFIX}/${EVENT_SERVICE_APP_NAME}:${APP_VERSION}"

SKIP_TESTS="true"

./mvnw clean spring-boot:build-image \
  --projects "$USER_SERVICE_APP_NAME" \
  -DskipTests="$SKIP_TESTS" \
  -Dspring-boot.build-image.imageName="$USER_SERVICE_DOCKER_IMAGE_NAME"

./mvnw clean spring-boot:build-image \
  --projects "$EVENT_SERVICE_APP_NAME" \
  -DskipTests="$SKIP_TESTS" \
  -Dspring-boot.build-image.imageName="$EVENT_SERVICE_DOCKER_IMAGE_NAME"
