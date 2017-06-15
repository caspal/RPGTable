#!/usr/bin/env bash
# Get dir of current script
bindir="$( cd "$(dirname "$0")" ; pwd -P )"
projectDir=$(dirname ${bindir})

echo "Run server tests"
(cd "${projectDir}/server" && ./gradlew test)
