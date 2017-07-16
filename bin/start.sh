#!/bin/bash
# Get dir of current script
bindir="$( cd "$(dirname "$0")" ; pwd -P )"
projectDir=$(dirname ${bindir})

JAR="${projectDir}/dist/RPGTable.jar"
CONF="${projectDir}/conf/vertx-conf.json"

java -jar "${JAR}" -conf "${CONF}" -Dworkspace.dir="${projectDir}"
