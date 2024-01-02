#!/bin/bash
export KAFKA_BIN_PATH=/home/ec2-user/environment/kafka_2.13-2.7.1/bin
SOURCE_KAFKA_URL=$1
DESTINATION_KAFKA_URL=$2
OFFSET_TOPIC=$3
IF_PROBE_KAFKA_CLUSTER=$4
export PATH=$PATH:$KAFKA_BIN_PATH

mkdir -p archive
mv source_offset.txt destination_offset.txt offset_sync.txt archive
echo "Probing kafka cluster"
./check-kafka-sync.sh ${SOURCE_KAFKA_URL} ${DESTINATION_KAFKA_URL} ${OFFSET_TOPIC}

echo "Compiling and running the program"
mvn clean compile exec:java -Dexec.mainClass="org.example.Main" -Dexec.args="source_offset.txt destination_offset.txt offset_sync.txt"