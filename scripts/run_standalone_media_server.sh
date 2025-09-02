#! /bin/bash
set -x

export LIB_DIR=lib

java --add-opens=java.base/jdk.internal.misc=ALL-UNNAMED -Daeron.debug=true -jar $LIB_DIR/aeron_lab-1.0-SNAPSHOT-all.jar
