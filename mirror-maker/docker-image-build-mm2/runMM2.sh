#!/bin/bash

set -e
echo "Starting Kafka Connect"
echo "DESTINATION_BOOTSTRAP_SERVER=${DESTINATION_BOOTSTRAP_SERVER}"
echo "SOURCE_BOOTSTRAP_SERVER=${SOURCE_BOOTSTRAP_SERVER}"
echo "REPLICATION_FACTOR=${REPLICATION_FACTOR}"
echo "OFFSET_STORAGE_TOPIC=${OFFSET_STORAGE_TOPIC}"
echo "CONFIG_STORAGE_TOPIC=${CONFIG_STORAGE_TOPIC}"
echo "STATUS_STORAGE_TOPIC=${STATUS_STORAGE_TOPIC}"
envsubst < /opt/kafka-connect/connect-mirror-maker.properties > /var/run/kafka-connect/connect-mirror-maker.properties
cat /var/run/kafka-connect/connect-mirror-maker.properties
cp /tmp/kafka/*.jar /opt/bitnami/kafka/libs
ls -ltr /opt/bitnami/kafka/libs
/opt/bitnami/kafka/bin/connect-mirror-maker.sh /var/run/kafka-connect/connect-mirror-maker.properties

