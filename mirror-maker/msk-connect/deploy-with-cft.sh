#!/bin/bash
## Parameters
export REGION=ap-south-1
export SOURCE_KAFKA_CLUSTER_BOOTSTRAP="b-1.msksourcecluster.lonhae.c4.kafka.ap-south-1.amazonaws.com:9092,b-3.msksourcecluster.lonhae.c4.kafka.ap-south-1.amazonaws.com:9092,b-2.msksourcecluster.lonhae.c4.kafka.ap-south-1.amazonaws.com:9092"
export DESTINATION_KAFKA_CLUSTER_BOOTSTRAP="b-3.mskdestinationcluster.juamie.c4.kafka.ap-south-1.amazonaws.com:9092,b-1.mskdestinationcluster.juamie.c4.kafka.ap-south-1.amazonaws.com:9092,b-2.mskdestinationcluster.juamie.c4.kafka.ap-south-1.amazonaws.com:9092"
export SOURCE_CLUSTER_SG=sg-0c62f6233be250c5a
export DESTINATION_CLUSTER_SG=sg-0b72dc485fb5c4e79
export SubnetAId=subnet-03dfb01c582465083
export SubnetBId=subnet-0d85538e799c33c9c
export MM2CustomPluginArn=

export TASK_MAX=2
echo "Deploying ECS stack"
aws cloudformation deploy --template-file mm2-msk-connect.yml \
   --capabilities CAPABILITY_NAMED_IAM CAPABILITY_AUTO_EXPAND \
   --stack-name mm2-msk-connect  \
   --disable-rollback \
   --parameter-overrides \
   SubnetAId=${SubnetAId} \
   SubnetBId=${SubnetBId} \
   DestinationClusterBootstrap=${DESTINATION_KAFKA_CLUSTER_BOOTSTRAP} \
   SourceClusterBootstrap=${SOURCE_KAFKA_CLUSTER_BOOTSTRAP} \
   DestinationClusterSG=${DESTINATION_CLUSTER_SG} \
   SourceClusterSG=${SOURCE_CLUSTER_SG} \
   SourceTopic=.* \
   MM2CustomPluginArn=${MM2CustomPluginArn} \
   TasksMax=${TASK_MAX}