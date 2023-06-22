#!/bin/bash
set -eo pipefail
ARTIFACT_BUCKET=$1
stack_name=$2
IS_SERVERLESS=$3
export VPCCidr="172.16.0.0/16"
export PeerAccountId="967781231549"
export PeerVPCCidr="10.0.0.0/16"
export MSKSourceKafkaVersion="2.8.1"

echo Artifact bucket is ${ARTIFACT_BUCKET} , VPC CIDR is ${VPCCidr} , Peer Account ID is ${PeerAccountId} , Peer VPC CIDR is ${PeerVPCCidr} , MSK Source Kafka Version is ${MSKSourceKafkaVersion}
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

aws s3 cp ../common s3://${ARTIFACT_BUCKET}/ --recursive --exclude "*" --include "*.yml"
aws s3 cp . s3://${ARTIFACT_BUCKET}/ --recursive --exclude "*" --include "*.yml"

if [ "$IS_SERVERLESS" == "N" ]
then
    echo "Deploying Provisioned"
    aws cloudformation deploy --template-file msk-provisioned-cluster.yml --capabilities CAPABILITY_NAMED_IAM CAPABILITY_AUTO_EXPAND \
     --stack-name ${stack_name}  \
     --parameter-overrides VPCStackTemplate=https://${ARTIFACT_BUCKET}.s3.amazonaws.com/MSKPrivateVPCOnly.yml \
     VPCCidr=${VPCCidr} \
     PeerAccountId=${PeerAccountId} \
     PeerVPCCidr=${PeerVPCCidr} \
     LambdaTemplatePath=https://${ARTIFACT_BUCKET}.s3.amazonaws.com/template-publish-mvn.yml \
     LambdaBucketName=${ARTIFACT_BUCKET} \
     LambdaCodeFileKey=msk-producer-lambda-1.0-SNAPSHOT.jar \
     MSKSourceKafkaVersion=${MSKSourceKafkaVersion}
else

    echo "Deploying Serverless"
    aws cloudformation deploy --template-file msk-serverless-cluster.yml --capabilities CAPABILITY_NAMED_IAM CAPABILITY_AUTO_EXPAND \
     --stack-name ${stack_name}  \
     --parameter-overrides VPCStackTemplate=https://${ARTIFACT_BUCKET}.s3.amazonaws.com/MSKPrivateVPCOnly.yml \
     VPCCidr=${VPCCidr} \
     PeerAccountId=${PeerAccountId} \
     PeerVPCCidr=${PeerVPCCidr} \
     LambdaTemplatePath=https://${ARTIFACT_BUCKET}.s3.amazonaws.com/template-publish-mvn.yml \
     LambdaBucketName=${ARTIFACT_BUCKET} \
     LambdaCodeFileKey=target/msk-producer-lambda-1.0-SNAPSHOT.jar

fi

aws cloudformation describe-stacks --stack-name ${stack_name} --query 'Stacks[0].Outputs[].[OutputKey,OutputValue,ExportName]' --output json
