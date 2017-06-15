#!/usr/bin/env bash

echo "Run server tests"
(cd ../server && ./gradlew test)
