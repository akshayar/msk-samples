#!/bin/bash
## Parameters
export REGION=ap-south-1
export SOURCE_KAFKA_CLUSTER_BOOTSTRAP="b-3.msksourcecluster.n6yhmp.c4.kafka.ap-south-1.amazonaws.com:9092,b-2.msksourcecluster.n6yhmp.c4.kafka.ap-south-1.amazonaws.com:9092,b-1.msksourcecluster.n6yhmp.c4.kafka.ap-south-1.amazonaws.com:9092"
export DESTINATION_KAFKA_CLUSTER_BOOTSTRAP="b-1.mskdestinationcluster.01l0zn.c4.kafka.ap-south-1.amazonaws.com:9092,b-3.mskdestinationcluster.01l0zn.c4.kafka.ap-south-1.amazonaws.com:9092,b-2.mskdestinationcluster.01l0zn.c4.kafka.ap-south-1.amazonaws.com:9092"
export SOURCE_CLUSTER_SG=sg-054b423ef3b89cf5a
export DESTINATION_CLUSTER_SG=sg-0878c71b5594a9235
export SUBNET_1=subnet-03dfb01c582465083
export SUBNET_2=subnet-0d85538e799c33c9c
export MM2_CUSTOM_PLUGIN_ARN=
export MM2_WORKER_CONFIG_ARN=
export TASK_MAX=2
export LOG_GROUP_NAME=/aws/msk-connect-mm2

export TASK_MAX=2
echo "Deploying MM2 MSK Connect Stack"
aws cloudformation deploy --template-file cloudformation/mm2-msk-connect.yml \
   --capabilities CAPABILITY_NAMED_IAM CAPABILITY_AUTO_EXPAND \
   --stack-name mm2-msk-connect  \
   --region ${REGION} \
   --disable-rollback \
   --parameter-overrides \
   SubnetAId=${SUBNET_1} \
   SubnetBId=${SUBNET_2} \
   DestinationClusterBootstrap=${DESTINATION_KAFKA_CLUSTER_BOOTSTRAP} \
   SourceClusterBootstrap=${SOURCE_KAFKA_CLUSTER_BOOTSTRAP} \
   DestinationClusterSG=${DESTINATION_CLUSTER_SG} \
   SourceClusterSG=${SOURCE_CLUSTER_SG} \
   SourceTopic=.* \
   MM2CustomPluginArn=${MM2_CUSTOM_PLUGIN_ARN} \
   TasksMax=${TASK_MAX} \
   MSKConnectConnectorLogGroup=${LOG_GROUP_NAME} \
   WorkerConfigurationArn=${MM2_WORKER_CONFIG_ARN}