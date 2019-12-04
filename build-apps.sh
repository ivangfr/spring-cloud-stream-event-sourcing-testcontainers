#!/usr/bin/env bash

./gradlew user-service:clean user-service:docker -x test
./gradlew event-service:clean event-service:docker -x test
