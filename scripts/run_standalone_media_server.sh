#! /bin/bash
set -x

java --add-opens=java.base/jdk.internal.misc=ALL-UNNAMED -Daeron.debug=true -jar ./aeron_lab-1.0-SNAPSHOT-all.jar
