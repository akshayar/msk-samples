#!/bin/bash
## Parameters
export ECS_CLUSTER_NAME=mm2-fargate-cluster
export REGION=ap-south-1
export SOURCE_KAFKA_CLUSTER_BOOTSTRAP=""
export DESTINATION_KAFKA_CLUSTER_BOOTSTRAP=""
export MSKCluster1SG=sg-0bc9fa1111111
export MSKCluster2SG=sg-0b909a2222222
export ECS_ALB_VPC=vpc-0111111111
export ECS_ALB_PublicSubnet1=subnet-0d1111111111
export ECS_ALB_PublicSubnet2=subnet-032222222222
## Uncomment and change the image name if you built an image.
#export KafkaConnectImage=arawa3/kafka-connect-mm2
export REPLICATION_FACTOR=2


echo "Fetching ECS cluster ARN"
ECS_CLUSTER_ARN=`aws ecs describe-clusters --cluster ${ECS_CLUSTER_NAME} --region ${REGION} --query 'clusters[].clusterArn' --output text`

AWS_ECS_SERVICE_ROLE_NAME=AWSServiceRoleForECS
echo "Fetching ECS service role ARN"
AWS_ECS_SERVICE_ROLE_ARN=`aws iam get-role --role-name ${AWS_ECS_SERVICE_ROLE_NAME} --query 'Role.Arn' --output text`

echo "Deploying ECS stack"
aws cloudformation deploy --template-file kafka-connect-mm2-ecs-deploy.yml \
   --capabilities CAPABILITY_NAMED_IAM CAPABILITY_AUTO_EXPAND \
   --stack-name kafka-connect-ecs-deploy  \
   --parameter-overrides \
   SourceKafkaClusterBootstrap=${SOURCE_KAFKA_CLUSTER_BOOTSTRAP} \
   DestinationKafkaClusterBootstrap=${DESTINATION_KAFKA_CLUSTER_BOOTSTRAP} \
   ECSClusterArn=${ECS_CLUSTER_ARN} \
   ECSServiceRoleArn=${AWS_ECS_SERVICE_ROLE_ARN} \
   VPC=${ECS_ALB_VPC} \
   PublicSubnet1=${ECS_ALB_PublicSubnet1} \
   PublicSubnet2=${ECS_ALB_PublicSubnet2} \
   MSKCluster1SG=${MSKCluster1SG} \
   MSKCluster2SG=${MSKCluster2SG} \
   DesiredCount=3 \
   ReplicationFactor=${REPLICATION_FACTOR}