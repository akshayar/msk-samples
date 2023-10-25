#!/bin/bash
export REGION=ap-south-1
export MSKSourceKafkaVersion=2.8.0
export VPCId=vpc-01be4940d7ee23da5
export Subnet1=subnet-03dfb01c582465083
export Subnet2=subnet-0d85538e799c33c9c
export Subnet3=subnet-069dc87ebf9750664
export InstanceType=kafka.m5.large
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
echo "Deploying MSK Source Cluster "
aws cloudformation deploy --template-file msk-provisioned-cluster.yml \
   --capabilities CAPABILITY_NAMED_IAM CAPABILITY_AUTO_EXPAND \
   --stack-name msk-source-cluster   \
   --region ${REGION} \
   --parameter-overrides MSKSourceKafkaVersion=${MSKSourceKafkaVersion} \
   VPCId=${VPCId} \
   Subnet1=${Subnet1} \
   Subnet2=${Subnet2} \
   Subnet3=${Subnet3} \
   InstanceType=${InstanceType} \
   MSKPublisherSG=${MSKPublisherSG} \
   &


echo "Deploying MSK Destination Cluster "
aws cloudformation deploy --template-file msk-provisioned-cluster.yml \
   --capabilities CAPABILITY_NAMED_IAM CAPABILITY_AUTO_EXPAND \
   --stack-name msk-destination-cluster   \
   --region ${REGION} \
   --parameter-overrides MSKSourceKafkaVersion=${MSKSourceKafkaVersion} \
   VPCId=${VPCId} \
   Subnet1=${Subnet1} \
   Subnet2=${Subnet2} \
   Subnet3=${Subnet3} \
   InstanceType=${InstanceType} \
   MSKPublisherSG=${MSKPublisherSG} \
   &
