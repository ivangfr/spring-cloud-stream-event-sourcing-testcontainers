#!/usr/bin/env bash

if [ "$1" = "native" ];
then
  ./gradlew user-service:clean user-service:bootBuildImage -x test
  ./gradlew event-service:clean event-service:bootBuildImage -x test
else
  ./gradlew user-service:clean user-service:jibDockerBuild -x test
  ./gradlew event-service:clean event-service:jibDockerBuild -x test
fi
