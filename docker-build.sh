#!/usr/bin/env bash

if [ "$1" = "native" ];
then
  ./mvnw clean spring-boot:build-image --projects user-service -DskipTests
  ./mvnw clean spring-boot:build-image --projects event-service -DskipTests
else
  ./mvnw clean package jib:dockerBuild --projects user-service -DskipTests
  ./mvnw clean package jib:dockerBuild --projects event-service -DskipTests
fi
