#!/bin/bash
CLUSTER_TYPE=$1
KAFKA_URL=$2
OFFSET_TOPIC=$3
TOPIC_LIST_FILE=${CLUSTER_TYPE}_topic_list.txt
GROUP_LIST_FILE=${CLUSTER_TYPE}_group_list.txt
OFFSET_FILE=${CLUSTER_TYPE}_offset.txt
OFFSET_SYNC_FILE=${CLUSTER_TYPE}_offset_sync.txt
#OFFSET_TOPIC=mm2-offset-syncs..internal
echo "Listing all topics in ${CLUSTER_TYPE}"
TOPIC_LIST=$(${KAFKA_BIN_PATH}/kafka-topics.sh --bootstrap-server $KAFKA_URL --list)
echo "$TOPIC_LIST" > ${TOPIC_LIST_FILE}

echo "Listing consumer groups in ${CLUSTER_TYPE}"
GROUP_LIST=$(${KAFKA_BIN_PATH}/kafka-consumer-groups.sh --bootstrap-server $KAFKA_URL --list)
echo "$GROUP_LIST" > ${GROUP_LIST_FILE}


for topic in $TOPIC_LIST
do
  if [[ $topic == _* ]]; then
         echo "Internal topic ignoring $topic"
         continue
  else
    echo "Getting offset information from source cluster for topic $topic"
    ${KAFKA_BIN_PATH}/kafka-run-class.sh kafka.tools.GetOffsetShell \
    --broker-list $KAFKA_URL --topic $topic --time -1 >> ${OFFSET_FILE}
  fi
done

## If OFFSET_TOPIC is supplied execute rest of the steps
if [ -z "$OFFSET_TOPIC" ]
then
    echo "OFFSET_TOPIC not supplied"
else
    echo "OFFSET_TOPIC supplied"
    echo "Comparing offset information"

    message_count_in_offset=$(${KAFKA_BIN_PATH}/kafka-run-class.sh kafka.tools.GetOffsetShell --broker-list $KAFKA_URL --topic   ${OFFSET_TOPIC} --time -1 --offsets 1 | awk -F ":" '{sum += $3} END {print sum}')
    echo "Total messages in offset topic $message_count_in_offset"

    if [ $message_count_in_offset -gt 0 ]
    then
        echo "Consuming offset topic"

        ${KAFKA_BIN_PATH}/kafka-console-consumer.sh --bootstrap-server $KAFKA_URL \
        --topic ${OFFSET_TOPIC} --formatter org.apache.kafka.connect.mirror.formatters.OffsetSyncFormatter \
        --from-beginning --max-messages ${message_count_in_offset} >> ${OFFSET_SYNC_FILE}

        echo "Consumed offset topic"
    fi
fi
