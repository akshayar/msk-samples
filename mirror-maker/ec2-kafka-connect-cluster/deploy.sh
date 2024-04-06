#!/bin/bash
## Parameters
export REGION=ap-south-1
export SOURCE_KAFKA_CLUSTER_BOOTSTRAP="b-2.msksourcecluster.7wuq46.c4.kafka.ap-south-1.amazonaws.com:9092,b-1.msksourcecluster.7wuq46.c4.kafka.ap-south-1.amazonaws.com:9092"
export DESTINATION_KAFKA_CLUSTER_BOOTSTRAP="b-2.mskdestinationcluster.dp3o1i.c4.kafka.ap-south-1.amazonaws.com:9092,b-1.mskdestinationcluster.dp3o1i.c4.kafka.ap-south-1.amazonaws.com:9092"
export MSKCluster1SG=sg-06ac17937c4e3a96d
export MSKCluster2SG=sg-0d2c5800ca5ad2d51
export ECS_ALB_VPC=vpc-01be4940d7ee23da5
export Subnet1=subnet-0d85538e799c33c9c
export Subnet2=subnet-03dfb01c582465083
## Uncomment and change the image name if you built an image.
export REPLICATION_FACTOR=2
export KEY_NAME=redshift-mum


echo "Deploying ECS stack"
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