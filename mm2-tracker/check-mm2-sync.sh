#!/bin/bash
SOURCE_KAFKA_URL=$1
DESTINATION_KAFKA_URL=$2
offset_topic=$3
SOURCE_OFFSET_FILE=source_offset.txt
DESTINATION_OFFSET_FILE=destination_offset.txt
OFFSET_SYNC_FILE=offset_sync.txt
#offset_topic=mm2-offset-syncs..internal
./probe-kafka.sh source ${SOURCE_KAFKA_URL} ${offset_topic} &
./probe-kafka.sh destination ${DESTINATION_KAFKA_URL} &
wait