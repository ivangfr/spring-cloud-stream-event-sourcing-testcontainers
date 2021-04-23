#!/usr/bin/env bash

if [ "$1" = "native" ];
then
  ./mvnw clean spring-boot:build-image --projects user-service -Pnative -DskipTests
  ./mvnw clean spring-boot:build-image --projects event-service -Pnative -DskipTests
else
  ./mvnw clean compile jib:dockerBuild --projects user-service
  ./mvnw clean compile jib:dockerBuild --projects event-service
fi
