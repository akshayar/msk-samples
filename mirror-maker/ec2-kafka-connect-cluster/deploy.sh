#!/bin/bash
## Parameters
export REGION=ap-south-1
export SOURCE_KAFKA_CLUSTER_BOOTSTRAP=""
export DESTINATION_KAFKA_CLUSTER_BOOTSTRAP=""
export MSKCluster1SG=sg-11
export MSKCluster2SG=sg-11
export ECS_ALB_VPC=vpc-11
export Subnet1=subnet-11
export Subnet2=subnet-11
## Uncomment and change the image name if you built an image.
export REPLICATION_FACTOR=2
export KEY_NAME=redshift-mum


echo "Deploying Kafka Connect on EC2 stack"
aws cloudformation deploy --template-file kafka-connect-ec2-deploy.yml \
   --capabilities CAPABILITY_NAMED_IAM CAPABILITY_AUTO_EXPAND \
   --stack-name kafka-connect-ec2-deploy  \
   --parameter-overrides \
   SourceKafkaClusterBootstrap=${SOURCE_KAFKA_CLUSTER_BOOTSTRAP} \
   DestinationKafkaClusterBootstrap=${DESTINATION_KAFKA_CLUSTER_BOOTSTRAP} \
   VPC=${ECS_ALB_VPC} \
   Subnet1=${Subnet1} \
   Subnet2=${Subnet1} \
   MSKCluster1SG=${MSKCluster1SG} \
   MSKCluster2SG=${MSKCluster2SG} \
   ReplicationFactor=${REPLICATION_FACTOR} \
   KeyName=redshift-mum