#/bin/bash
#
#
connector_arn=$1
polling_interval_sec=$2
if  [ -z "$polling_interval_sec" ]; then
    polling_interval_sec=30
    echo "Using default polling interval of $polling_interval_sec seconds"
    echo $polling_interval_sec
fi
date_delete=$(date +%s)
echo "Connector ARN: $connector_arn"
aws kafkaconnect delete-connector --connector-arn $connector_arn
while true; do
    sleep $polling_interval_sec
    export connectorState=$(aws kafkaconnect describe-connector --connector-arn $connector_arn --query 'connectorState' --output text)
    echo "Connector state: $connectorState"
    if [ "$connectorState" == "" ] || [ "$connectorState" == "RUNNING" ] || [ "$connectorState" == "DELETING" ]; then
        break
    fi
done
date_delete_done=$(date +%s)
echo "Deleted in $((date_delete_done - date_delete)) seconds $connector_arn "
exit 0