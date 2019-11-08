#!/usr/bin/env bash

./gradlew clean
./gradlew user-service:docker -x test
./gradlew event-service:docker -x test
