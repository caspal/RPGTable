#!/usr/bin/env bash
# Get dir of current script
bindir="$( cd "$(dirname "$0")" ; pwd -P )"
projectDir=$(dirname ${bindir})

echo "Install npm modules"
(cd "${projectDir}/browser" && npm install)
(cd "${projectDir}/browser" && npm run grunt build)

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

java -jar "\${JAR}" -conf "\${CONF}" "\${1}"
EOF
chmod +x "${projectDir}/dist/start.sh"
