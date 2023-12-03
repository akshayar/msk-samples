#/bin/bash
# Create and wait for connector to fail or succeed
#
export template_file=$1

if  [ -z "$template_file" ] ; then
    echo "Usage: $0 <template_file>  [polling_interval_sec]"
    exit 1
fi
polling_interval_sec=$3
if  [ -z "$polling_interval_sec" ]; then
    polling_interval_sec=30
    echo "Using default polling interval of $polling_interval_sec seconds"
    echo $polling_interval_sec
fi

echo "Using template file $template_file"
echo "Polling interval: $polling_interval_sec"

cat $template_file
date_create=$(date +%s)
export connector_arn=$(aws kafkaconnect create-connector --cli-input-json file://${template_file} --query 'connectorArn' --output text)
if  [ -z "$connector_arn" ]; then
    echo "Failed to create connector"
    exit 1
else
  echo "Connector ARN: $connector_arn"
  echo "Waiting for connector to be created"
  while true; do
      sleep $polling_interval_sec
      export connectorState=$(aws kafkaconnect describe-connector --connector-arn $connector_arn --query 'connectorState' --output text)
      echo "Connector state: $connectorState"
      if [ "$connectorState" == "RUNNING" ] || [ "$connectorState" == "FAILED" ]; then
          break
      fi
  done
  date_create_done=$(date +%s)
  echo "Created in $((date_create_done - date_create)) seconds, Connector $connector_arn"
  exit 0
fi

