#!/usr/bin/env bash
# Get dir of current script
bindir="$( cd "$(dirname "$0")" ; pwd -P )"
projectDir=$(dirname ${bindir})
webrootDir="${projectDir}/server/src/main/resources/webroot"
echo "Install npm modules"
(cd "${projectDir}/browser" && ng build)
rm -rf "${webrootDir}/"*
cp "${projectDir}/browser/dist/"* "${webrootDir}/"

echo "Build fat jar"
(cd "${projectDir}/server" && ./gradlew shadowJar)

echo "Generate dist folder"
mkdir -p "${projectDir}/dist"
cp "${projectDir}/server/build/libs/server-all.jar" "${projectDir}/dist/RPGTable.jar"
cp -n -R "${projectDir}/conf" "${projectDir}/dist"
# Build start script

cat > "${projectDir}/dist/start.sh" << EOF
#!/bin/bash
# Get dir of current script
distDir=\$( cd "\$(dirname "\${0}")" ; pwd -P )

JAR="\${distDir}/RPGTable.jar"
CONF="\${distDir}/conf/vertx-conf.json"

java -jar "\${JAR}" -conf "\${CONF}" -Dworkspace.dir="\${distDir}"
EOF
chmod +x "${projectDir}/dist/start.sh"
