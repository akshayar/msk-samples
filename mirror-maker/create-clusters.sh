#!/bin/bash
function getClusterInformation() {
  stack_name=$1
  aws cloudformation describe-stacks --stack-name ${stack_name} --region ${REGION} \
   --query "Stacks[0].Outputs" --output text --no-cli-pager

  MSK_CLUSTER=$(aws kafka list-clusters --region ${REGION} --cluster-name-filter ${stack_name} \
    --region ${REGION} --query "ClusterInfoList[0].{ClusterArn:ClusterArn}" --output text --no-cli-pager)

  MSK_CLUSTER_SG=$(aws kafka describe-cluster --cluster-arn ${MSK_CLUSTER} --region ${REGION} \
    --region ${REGION} --query "ClusterInfo.BrokerNodeGroupInfo.SecurityGroups" --output text --no-cli-pager )

  export MSK_CLUSTER_BOOTSTRAP=$(aws kafka get-bootstrap-brokers --cluster-arn ${MSK_CLUSTER} \
    --region ${REGION} --query "BootstrapBrokerString" --output text --no-cli-pager)

    #echo "${MSK_CLUSTER}"
    echo "${MSK_CLUSTER_SG}"
    echo "${MSK_CLUSTER_BOOTSTRAP}"
    echo "#############"
}

function create_cluster() {
  stack_name=$1
  cluster_version=$2
  instance_type=$3
  echo "Deploying MSK Cluster ${stack_name} with version ${cluster_version} and template file ${TEMPLATE_FILE}"
  aws cloudformation deploy --template-file ${TEMPLATE_FILE}  \
     --capabilities CAPABILITY_NAMED_IAM CAPABILITY_AUTO_EXPAND \
     --stack-name ${stack_name}   \
     --region ${REGION} \
     --parameter-overrides MSKKafkaVersion=${cluster_version} \
     VPCId=${VPCId} \
     Subnet1=${Subnet1} \
     Subnet2=${Subnet2} \
     Subnet3=${Subnet3} \
     InstanceType=${instance_type} \
     MSKPublisherSG=${MSKPublisherSG} \
     VolumeSizeGB=${VolumeSizeGB}
}

function create_source_sink_clusters_in_parallel(){
  export MSKSourceKafkaVersion=2.7.0
  export MSKTargetKafkaVersion=3.6.0
  export SourceInstanceType=kafka.m5.large
  export TargetInstanceType=kafka.m7g.large
  export VolumeSizeGB=256

  echo "MSK Publisher SG created with ID: ${MSKPublisherSG}"
  SOURCE_CLUSTER_STACK_NAME=msk-source-cluster
  TARGET_CLUSTER_STACK_NAME=msk-destination-cluster
  create_cluster ${SOURCE_CLUSTER_STACK_NAME} ${MSKSourceKafkaVersion} ${SourceInstanceType} &
  create_cluster ${TARGET_CLUSTER_STACK_NAME} ${MSKTargetKafkaVersion} ${TargetInstanceType}

  ## Check for cloudformation stack status and wait
  aws cloudformation wait stack-create-complete --stack-name msk-source-cluster --region ${REGION} --output text --no-cli-pager
  aws cloudformation wait stack-create-complete --stack-name msk-destination-cluster --region ${REGION} --output text --no-cli-pager

  ## Query cloudformation stack output using cli
  getClusterInformation ${SOURCE_CLUSTER_STACK_NAME}
  getClusterInformation ${TARGET_CLUSTER_STACK_NAME}
}

function create_single_cluster(){
  export MSKSourceKafkaVersion=3.6.0
  export SourceInstanceType=kafka.m5.large
  export VolumeSizeGB=256

  SOURCE_CLUSTER_STACK_NAME=msk-source-cluster
  TARGET_CLUSTER_STACK_NAME=msk-destination-cluster
  create_cluster ${SOURCE_CLUSTER_STACK_NAME} ${MSKSourceKafkaVersion} ${SourceInstanceType}

  ## Check for cloudformation stack status and wait
  aws cloudformation wait stack-create-complete --stack-name msk-source-cluster --region ${REGION} --output text --no-cli-pager

  ## Query cloudformation stack output using cli
  getClusterInformation ${SOURCE_CLUSTER_STACK_NAME}
}
export REGION=ap-south-1
export VPCId=vpc-111
export Subnet1=subnet-11
export Subnet2=subnet-11
export Subnet3=subnet-11
export NUMBER_OF_AZ=3
export NUMBER_OF_CLUSTERS=2

echo "Creating MSK client SG"
aws ec2 create-security-group \
          --vpc-id ${VPCId} --region ${REGION} \
          --group-name MSKPublisherSG \
          --description "MSKPublisherSG"
export MSKPublisherSG=$(aws ec2 describe-security-groups --region ${REGION}\
          --filters "Name=vpc-id,Values=${VPCId}" \
          --query "SecurityGroups[?GroupName=='MSKPublisherSG'].GroupId" \
          --output text)

echo "MSK Publisher SG created with ID: ${MSKPublisherSG}"
if  [ ${NUMBER_OF_AZ} -eq 2 ]; then
    export TEMPLATE_FILE=msk-provisioned-cluster-2az.yml
else
    export TEMPLATE_FILE=msk-provisioned-cluster.yml
fi
if  [ ${NUMBER_OF_CLUSTERS} -eq 2 ]; then
    create_source_sink_clusters_in_parallel
else
    create_single_cluster
fi
#create_single_cluster
#create_source_sink_clusters_in_parallel
