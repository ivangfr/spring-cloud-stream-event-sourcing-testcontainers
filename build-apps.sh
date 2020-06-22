#!/usr/bin/env bash

./gradlew user-service:clean user-service:jibDockerBuild -x test
./gradlew event-service:clean event-service:jibDockerBuild -x test
