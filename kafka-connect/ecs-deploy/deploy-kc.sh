#!/bin/bash
## Parameters
export ECS_CLUSTER_NAME=ecs-fargate
export REGION=ap-south-1
#export DESTINATION_KAFKA_CLUSTER_BOOTSTRAP=""
export DESTINATION_KAFKA_CLUSTER_BOOTSTRAP=""
export AUTH_TYPE=IAM
export MSKCluster1SG=sg-111
export MSKCluster2SG=sg-11
export ECS_ALB_VPC=vpc-11
export ECS_ALB_PublicSubnet1=subnet-11
export ECS_ALB_PublicSubnet2=subnet-11
export KafkaConnectImage=arawa3/k-connect
export REPLICATION_FACTOR=3
export STACK_NAME=kafka-connect-ecs
export TEMPLATE_FILE=kafka-connect-ecs-deploy.yml
export PLUGIN_ZIP_S3_PATH=




echo "Fetching ECS cluster ARN"
ECS_CLUSTER_ARN=`aws ecs describe-clusters --cluster ${ECS_CLUSTER_NAME} --region ${REGION} --query 'clusters[].clusterArn' --output text`

AWS_ECS_SERVICE_ROLE_NAME=AWSServiceRoleForECS
echo "Fetching ECS service role ARN"
AWS_ECS_SERVICE_ROLE_ARN=`aws iam get-role --role-name ${AWS_ECS_SERVICE_ROLE_NAME} --query 'Role.Arn' --output text`

echo "Deploying ECS stack"
aws cloudformation deploy --template-file ${TEMPLATE_FILE} \
   --capabilities CAPABILITY_NAMED_IAM CAPABILITY_AUTO_EXPAND \
   --stack-name ${STACK_NAME}  \
   --parameter-overrides \
   DestinationKafkaClusterBootstrap=${DESTINATION_KAFKA_CLUSTER_BOOTSTRAP} \
   ECSClusterArn=${ECS_CLUSTER_ARN} \
   ECSServiceRoleArn=${AWS_ECS_SERVICE_ROLE_ARN} \
   VPC=${ECS_ALB_VPC} \
   PublicSubnet1=${ECS_ALB_PublicSubnet1} \
   PublicSubnet2=${ECS_ALB_PublicSubnet2} \
   MSKCluster1SG=${MSKCluster1SG} \
   MSKCluster2SG=${MSKCluster2SG} \
   DesiredCount=1 \
   ReplicationFactor=${REPLICATION_FACTOR} \
   ContainerImage=${KafkaConnectImage} \
   AuthType=${AUTH_TYPE} \
   PluginZipS3Path=${PLUGIN_ZIP_S3_PATH}
