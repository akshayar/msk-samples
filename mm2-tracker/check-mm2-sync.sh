#!/bin/bash
export KAFKA_BIN_PATH=/home/ec2-user/environment/kafka_2.13-2.7.1/bin
SOURCE_KAFKA_URL=$1
DESTINATION_KAFKA_URL=$2
offset_topic=$3
#offset_topic=mm2-offset-syncs..internal
./probe-kafka.sh source ${SOURCE_KAFKA_URL} ${offset_topic} &
./probe-kafka.sh destination ${DESTINATION_KAFKA_URL} &
wait