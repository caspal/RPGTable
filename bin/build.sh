#!/usr/bin/env bash

echo "Install npm modules"
(cd ../browser && npm install)
(cd ../browser && npm run grunt build)

echo "Build fat jar"
(cd ../server && ./gradlew shadowJar)

echo "Generate dist folder"
mkdir -p ../dist
cp ../server/build/libs/server-all.jar ../dist/RPGTable.jar
cp start.sh ../dist/start.sh
cp -n -R ../conf ../dist

