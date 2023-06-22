#!/bin/bash
set -eo pipefail
ARTIFACT_BUCKET=$1
mvn clean package -DskipTests
aws s3 cp target/msk-producer-lambda-1.0-SNAPSHOT.jar  s3://${ARTIFACT_BUCKET}/

aws cloudformation deploy --template-file template-publish-mvn.yml --stack-name kafka-pub-fun --capabilities CAPABILITY_NAMED_IAM \
--parameter-overrides mskArn=arn:aws:kafka:ap-south-1:229369268201:cluster/sink-cluster/b03ca683-a052-481d-940c-38eed89be7ee-3 \
authType=NONE topicName=testtopic securityGroupId=sg-28d5f054 privateSubnetId=subnet-08710af059f886114 BucketName=${ARTIFACT_BUCKET} CodeKey=msk-producer-lambda-1.0-SNAPSHOT.jar

aws cloudformation deploy --template-file template-topic-list-mvn.yml --stack-name kafka-list-topic-fun --capabilities CAPABILITY_NAMED_IAM \
--parameter-overrides mskArn=arn:aws:kafka:ap-south-1:229369268201:cluster/sink-cluster/b03ca683-a052-481d-940c-38eed89be7ee-3 \
authType=NONE topicName=testtopic securityGroupId=sg-28d5f054 privateSubnetId=subnet-08710af059f886114 BucketName=${ARTIFACT_BUCKET} CodeKey=msk-producer-lambda-1.0-SNAPSHOT.jar
