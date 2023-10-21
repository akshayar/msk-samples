#!/bin/bash
set -eo pipefail
ARTIFACT_BUCKET=$1
stack_name=$2
IS_SERVERLESS=$3
export PeerVPCCidr="172.16.0.0/16"
export PeerAccountId="ACCOUNT1"
export VPCCidr="10.0.0.0/16"
export PeerVPCId="vpc-023cf350d1e316b1d"
export peeringAcceptorRoleArn="arn:aws:iam::ACCOUNT1:role/msk-serverless-vpcPeeringAcceptorRole-1MA9BABFKTHG5"
export MSKSourceClusterArn="arn:aws:kafka:ap-south-1:ACCOUNT1:cluster/MSKSrc-msk-serverless/3fff2f1d-9d81-4b3b-a0ee-d0d90d5a0b6c-s3"
export mskActIamRoleArn="arn:aws:iam::ACCOUNT1:policy/msk-serverless-redshiftStreamingManagedPolicy-1D96FIRGGIC7Q"

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