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
  echo "Deploying MSK Cluster ${stack_name} with version ${cluster_version}"
  aws cloudformation deploy --template-file msk-provisioned-cluster.yml \
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
export REGION=ap-south-1
export MSKSourceKafkaVersion=3.3.2
export MSKTargetKafkaVersion=3.3.2
export VPCId=vpc-01be4940d7ee23da5
export Subnet1=subnet-03dfb01c582465083
export Subnet2=subnet-0d85538e799c33c9c
export Subnet3=subnet-069dc87ebf9750664
export SourceInstanceType=kafka.m5.xlarge
export TargetInstanceType=kafka.m7g.xlarge
export VolumeSizeGB=512
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
SOURCE_CLUSTER_STACK_NAME=msk-source-cluster
TARGET_CLUSTER_STACK_NAME=msk-destination-cluster
create_cluster ${SOURCE_CLUSTER_STACK_NAME} ${MSKSourceKafkaVersion} ${SourceInstanceType} &
create_cluster ${TARGET_CLUSTER_STACK_NAME} ${MSKTargetKafkaVersion} ${TargetInstanceType}

## Check for cloudformation stack status and wait
#aws cloudformation wait stack-create-complete --stack-name msk-source-cluster --region ${REGION} --output text --no-cli-pager
#aws cloudformation wait stack-create-complete --stack-name msk-destination-cluster --region ${REGION} --output text --no-cli-pager

## Query cloudformation stack output using cli
getClusterInformation ${SOURCE_CLUSTER_STACK_NAME}
getClusterInformation ${TARGET_CLUSTER_STACK_NAME}

