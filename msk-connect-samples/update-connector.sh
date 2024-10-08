#!/bin/bash
#
#
connector_arn=$1
template_file=$2
if  [ -z "$connector_arn" ] || [ -z "$template_file" ]; then
    echo "Usage: $0 <connector_arn> <template_file> <polling_interval_sec default 30>"
    exit 1
fi
echo "Updating connector $connector_arn with template $template_file"
polling_interval_sec=$2
if  [ -z "$polling_interval_sec" ]; then
    polling_interval_sec=30
    echo "Using default polling interval of $polling_interval_sec seconds"
    echo $polling_interval_sec
fi
./delete-connector.sh $connector_arn
./create-connector.sh $template_file $polling_interval_sec
exit 0

