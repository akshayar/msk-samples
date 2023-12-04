#!/bin/bash
CONNECTOR_PREFIX=mm2
CONNECTORS=$(aws kafkaconnect list-connectors --connector-name-prefix ${CONNECTOR_PREFIX} --query "connectors[].connectorArn" --output text)
for connector in ${CONNECTORS}
do
    echo "Deleting ${connector}"
    ./delete-connector.sh ${connector} &
done
wait
