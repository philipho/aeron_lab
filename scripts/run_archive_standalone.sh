#! /bin/bash
set -x

export LIB_DIR=../lib

java --add-opens=java.base/jdk.internal.misc=ALL-UNNAMED -Daeron.debug=true -classpath $LIB_DIR/aeron_lab-1.0-SNAPSHOT-all.jar org.mec.aeronlab.archive.AeronArchiveLauncher

