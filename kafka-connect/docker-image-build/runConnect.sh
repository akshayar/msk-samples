#!/bin/bash

set -e
echo "Starting Kafka Connect"
echo "BOOTSTRAP_SERVER=${BOOTSTRAP_SERVER}"
echo "REPLICATION_FACTOR=${REPLICATION_FACTOR}"
echo "OFFSET_STORAGE_TOPIC=${OFFSET_STORAGE_TOPIC}"
echo "CONFIG_STORAGE_TOPIC=${CONFIG_STORAGE_TOPIC}"
echo "STATUS_STORAGE_TOPIC=${STATUS_STORAGE_TOPIC}"
echo "GROUP_ID=${GROUP_ID}"
echo "ALLOW_PLAINTEXT_LISTENER=${ALLOW_PLAINTEXT_LISTENER}"
echo "PLUGIN_FILE_PATH=${PLUGIN_FILE_PATH}"
echo "KAFKA_OPTS=${KAFKA_OPTS}"
echo "AWS_REGION = ${AWS_REGION}"
echo "AUTH_TYPE = ${AUTH_TYPE}"

## Check if AUTH_TYPE is IAM then using IAM Auth property file , else use plaintext
if [ "${AUTH_TYPE}" = "IAM" ]; then
  echo "Using IAM Auth property file"
  envsubst < /opt/kafka-connect/connect-distributed-iam.properties > /var/run/kafka-connect/connect-distributed.properties
else
  echo "Using plaintext Auth property file"
  envsubst < /opt/kafka-connect/connect-distributed.properties > /var/run/kafka-connect/connect-distributed.properties
fi

## If PLUGIN_ZIP_S3_PATH is not null or empty add, additional plugin
if [ -z "${PLUGIN_ZIP_S3_PATH}" ]; then
  echo "No plugin to download"
else
  echo "Downloading plugin from S3 ${PLUGIN_ZIP_S3_PATH}"
  ## Split by comma and download each file
  IFS=',' read -ra ADDR <<< "${PLUGIN_ZIP_S3_PATH}"
  for i in "${ADDR[@]}"; do
    echo "Downloading plugin from S3 ${i}"
    ## Get file name from S3 path
    FILENAME=$(basename "${i}")
    FILE_NAME=$(basename "${FILENAME}" .zip)
    echo "File name is ${FILENAME} ${FILE_NAME}"
    aws s3 cp "${i}" /opt/bitnami/kafka/plugins/
    echo "Unzipping plugin /opt/bitnami/kafka/plugins/${FILE_NAME}.zip"
    unzip -o -j /opt/bitnami/kafka/plugins/${FILE_NAME}.zip -d /opt/bitnami/kafka/plugins/${FILE_NAME}
    rm /opt/bitnami/kafka/plugins/${FILE_NAME}.zip
  done
fi

#cat /var/run/kafka-connect/connect-distributed.properties
ls -ltr /opt/bitnami/kafka/libs
/opt/bitnami/kafka/bin/connect-distributed.sh /var/run/kafka-connect/connect-distributed.properties

