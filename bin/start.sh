#!/bin/bash
JAR="../dist/RPGTable.jar"
CONF="../conf/vertx-conf.json"

java -jar "${JAR}" -conf "${CONF}" "${1}"
