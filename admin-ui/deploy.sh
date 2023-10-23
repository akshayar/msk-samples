#!/bin/bash
## Parameters
export ECS_CLUSTER_NAME=ForMSK
export REGION=ap-south-1
export SOURCE_KAFKA_CLUSTER_BOOTSTRAP="b-1.source.yjtfqf.c4.kafka.ap-south-1.amazonaws.com:9092,b-2.source.yjtfqf.c4.kafka.ap-south-1.amazonaws.com:9092"
export DESTINATION_KAFKA_CLUSTER_BOOTSTRAP="b-1.target.oumzco.c4.kafka.ap-south-1.amazonaws.com:9092,b-2.target.oumzco.c4.kafka.ap-south-1.amazon:9092"
export MSKCluster1SG=sg-09642b7a0ad7e5bf9
export MSKCluster2SG=sg-09642b7a0ad7e5bf9
export ECS_ALB_VPC=vpc-01be4940d7ee23da5
export ECS_ALB_PublicSubnet1=subnet-0d85538e799c33c9c
export ECS_ALB_PublicSubnet2=subnet-03dfb01c582465083


echo "Fetching ECS cluster ARN"
ECS_CLUSTER_ARN=`aws ecs describe-clusters --cluster ${ECS_CLUSTER_NAME} --region ${REGION} --query 'clusters[].clusterArn' --output text`

AWS_ECS_SERVICE_ROLE_NAME=AWSServiceRoleForECS
echo "Fetching ECS service role ARN"
AWS_ECS_SERVICE_ROLE_ARN=`aws iam get-role --role-name ${AWS_ECS_SERVICE_ROLE_NAME} --query 'Role.Arn' --output text`

echo "Deploying ECS stack"
aws cloudformation deploy --template-file akhq-ecs-deploy.yml \
   --capabilities CAPABILITY_NAMED_IAM CAPABILITY_AUTO_EXPAND \
   --stack-name akhq-ecs-deploy  \
   --parameter-overrides \
   SourceKafkaClusterBootstrap=${SOURCE_KAFKA_CLUSTER_BOOTSTRAP} \
   DestinationKafkaClusterBootstrap=${DESTINATION_KAFKA_CLUSTER_BOOTSTRAP} \
   ECSClusterArn=${ECS_CLUSTER_ARN} \
   ECSServiceRoleArn=${AWS_ECS_SERVICE_ROLE_ARN} \
   VPC=${ECS_ALB_VPC} \
   PublicSubnet1=${ECS_ALB_PublicSubnet1} \
   PublicSubnet2=${ECS_ALB_PublicSubnet2} \
   MSKCluster1SG=${MSKCluster1SG} \
   MSKCluster2SG=${MSKCluster2SG}