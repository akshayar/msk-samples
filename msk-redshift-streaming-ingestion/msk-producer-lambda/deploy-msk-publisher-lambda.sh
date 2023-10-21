#!/bin/bash
set -eo pipefail
ARTIFACT_BUCKET=$1
IS_SERVERLESS=$2
if [ -z "$ARTIFACT_BUCKET" ]
then
    echo "Usage: $0 <artifact-bucket> <serverless>"
    exit 1
fi

echo "Serverless is ${IS_SERVERLESS}"
if [ -z "$IS_SERVERLESS" ]
then
    echo "Usage: $0 <artifact-bucket> <serverless>"
    IS_SERVERLESS="N"
fi

mvn clean package -DskipTests
aws s3 cp . s3://${ARTIFACT_BUCKET}/ --recursive --exclude "*" --include "*.jar"
aws s3 cp . s3://${ARTIFACT_BUCKET}/ --recursive --exclude "*" --include "*.yml"

export msk_stack_name="msk-serverless"
export stack_name="msk-publisher-lambda"
echo "Deploying Lambda"

if [ "$IS_SERVERLESS" == "N" ]
then
  export auth_type="NONE"
else
  export auth_type="IAM"
  export msk_stack_name="msk-serverless"
fi

aws cloudformation deploy --template-file deploy-msk-publisher-lambda.yml --capabilities CAPABILITY_NAMED_IAM CAPABILITY_AUTO_EXPAND \
   --stack-name ${stack_name}  \
   --parameter-overrides MSKMainStackName=${msk_stack_name} \
    LambdaTemplatePath="https://${ARTIFACT_BUCKET}.s3.amazonaws.com/template-publish-mvn.yml" \
    LambdaBucketName="${ARTIFACT_BUCKET}" \
    LambdaCodeFileKey=target/msk-producer-lambda-1.0-SNAPSHOT.jar \
    AuthType=${auth_type}