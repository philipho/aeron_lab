#! /bin/bash
set -x

AERON_DIR=./my-aeron-dir
JVM_OPTS="--add-opens=java.base/jdk.internal.misc=ALL-UNNAMED"
MAIN="io.aeron.samples.AeronStat"
SAMPLES_JAR=$HOME/dev/aeron_lab/scripts/aeron-samples-1.49.0-SNAPSHOT.jar
ALL_JAR=$HOME/dev/aeron_lab/scripts/aeron-all-1.48.5.jar

java $JVM_OPTS -Daeron.dir=$AERON_DIR -cp "$SAMPLES_JAR:$ALL_JAR" $MAIN

