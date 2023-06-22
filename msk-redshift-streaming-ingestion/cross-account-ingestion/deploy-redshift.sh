#!/bin/bash
set -eo pipefail
ARTIFACT_BUCKET=$1
stack_name=$2
IS_SERVERLESS=$3
export PeerVPCCidr="172.16.0.0/16"
export PeerAccountId="229369268201"
export VPCCidr="10.0.0.0/16"
export PeerVPCId="vpc-0698cb6744a727a84"
export peeringAcceptorRoleArn="arn:aws:iam::229369268201:role/msk-serverless-cluster-vpcPeeringAcceptorRole-1TV1KR41O92E0"
export MSKSourceClusterArn="arn:aws:kafka:ap-south-1:229369268201:cluster/MSKSrc-msk-serverless-cluster/55f94cf5-80eb-499e-825c-e777d5f71510-s3"
export mskActIamRoleArn="arn:aws:iam::229369268201:role/service-role/msk-serverless-cluster-redshiftStrIngestCrossActRo-RZ4CHCMSHVR3"

echo "Artifact bucket is ${ARTIFACT_BUCKET} , Serverless is ${IS_SERVERLESS}"
echo "VPC is ${VPCCidr} , Peer VPC is ${PeerVPCId} , Peer Account is ${PeerAccountId} , Peer VPC CIDR is ${PeerVPCCidr} , Peering acceptor role is ${peeringAcceptorRoleArn}"
echo "Source cluster is ${MSKSourceClusterArn} , MSK IAM Role is ${mskActIamRoleArn}"

if [ -z "$ARTIFACT_BUCKET" ]
then
    echo "Usage: $0 <artifact-bucket> <stack_name> <serverless>"
    exit 1
fi


echo "Serverless is ${IS_SERVERLESS}"
if [ -z "$IS_SERVERLESS" ]
then
    echo "Usage: $0 <artifact-bucket> <stack_name> <serverless>"
    IS_SERVERLESS="N"
fi

aws s3 cp . s3://${ARTIFACT_BUCKET}/ --recursive --exclude "*" --include "*.yml"




if [ "$IS_SERVERLESS" == "N" ]
then
  echo "Deploying Provisioned"
else
  echo "Deploying Serverless"
  aws cloudformation deploy --template-file redshift-serverless-cluster.yml --capabilities CAPABILITY_NAMED_IAM CAPABILITY_AUTO_EXPAND \
   --stack-name ${stack_name}  \
   --parameter-overrides VPCStackTemplate="https://${ARTIFACT_BUCKET}.s3.amazonaws.com/MSKPrivateVPCOnly.yml" \
   VPCCidr=${VPCCidr} \
   peerVpcId=${PeerVPCId} \
   peerAccountId=${PeerAccountId} \
   peerVPCCidr=${PeerVPCCidr} \
   peeringAcceptorRoleArn=${peeringAcceptorRoleArn} \
   mskSourceClusterArn=${MSKSourceClusterArn} \
   mskActIamRoleArn=${mskActIamRoleArn}
fi

aws cloudformation describe-stacks --stack-name ${stack_name} --query 'Stacks[0].Outputs[].[OutputKey,OutputValue,ExportName]' --output json