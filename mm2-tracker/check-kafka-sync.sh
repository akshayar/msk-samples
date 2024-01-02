#!/bin/bash
SOURCE_KAFKA_URL=$1
DESTINATION_KAFKA_URL=$2
offset_topic=$3
#offset_topic=mm2-offset-syncs..internal
KAFKA_URL=$SOURCE_KAFKA_URL
echo "Iterating over all topics"
TOPIC_LIST=$(${KAFKA_BIN_PATH}/kafka-topics.sh --bootstrap-server $KAFKA_URL --list)
echo "List of topics $TOPIC_LIST"

for topic in $TOPIC_LIST
do
  if [[ $topic == _* ]]; then
         echo "Internal topic ignoring $topic"
         continue
  else
    echo "Getting offset information from source cluster for topic $topic"
    ${KAFKA_BIN_PATH}/kafka-run-class.sh kafka.tools.GetOffsetShell \
    --broker-list $SOURCE_KAFKA_URL --topic $topic --time -1 >> source_offset.txt
    echo "Getting offset information from destination cluster for topic $topic"
    ${KAFKA_BIN_PATH}/kafka-run-class.sh kafka.tools.GetOffsetShell \
    --broker-list $DESTINATION_KAFKA_URL --topic $topic --time -1 >> destination_offset.txt
  fi
done

echo "Comparing offset information"

message_count_in_offset=$(${KAFKA_BIN_PATH}/kafka-run-class.sh kafka.tools.GetOffsetShell --broker-list $SOURCE_KAFKA_URL --topic   ${offset_topic} --time -1 --offsets 1 | awk -F ":" '{sum += $3} END {print sum}')
echo "Total messages in offset topic $message_count_in_offset"
if [ $message_count_in_offset -gt 0 ]
then
    echo "Consuming offset topic"
    ${KAFKA_BIN_PATH}/kafka-console-consumer.sh --bootstrap-server $SOURCE_KAFKA_URL \
    --topic ${offset_topic} --formatter org.apache.kafka.connect.mirror.formatters.OffsetSyncFormatter \
    --from-beginning --max-messages ${message_count_in_offset} >> offset_sync.txt
    echo "Consumed offset topic"
fi