#/bin/bash
# Create worker configuration
#
export worker_config_name=$1
export worker_properties_file=$2

echo "Worker properties file: $worker_properties_file Worker configuration name: $worker_config_name"
if [ -z "$worker_properties_file" ] || [ -z "$worker_config_name" ] ; then
  echo "Usage: $0 <worker_config_name> <worker_properties_file>"
  exit 1
fi
echo "Worker properties file: $worker_properties_file"
echo "Worker configuration name: $worker_config_name"


cat $worker_properties_file
export properties_content=$(cat $worker_properties_file | base64)

worker_config_arn=$(aws kafkaconnect create-worker-configuration --name $worker_config_name  --properties-file-content $properties_content --query workerConfigurationArn --output text)
if  [ -z "$worker_config_arn" ] ; then
    echo "Worker configuration creation failed"
    exit 1
else
    echo "Worker configuration creation succeeded ARN: $worker_config_arn"
    echo "Worker configuration ARN: $worker_config_arn"
    aws kafkaconnect describe-worker-configuration --worker-configuration-arn  $worker_config_arn
    echo $worker_config_arn
fi