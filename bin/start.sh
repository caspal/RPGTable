#!/bin/bash
JAR="RPGTable.jar"
CONF="conf/vertx-conf.json"
WORK_DIR=$(pwd)
java -jar "${JAR}" -conf "${CONF}" -Dworkspace.dir="${WORK_DIR}"
