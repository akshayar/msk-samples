#!/bin/bash
## This script will deploy the AKHQ stack on ECS
## Parameters
export ECS_CLUSTER_NAME=ecs-fargate
export REGION=ap-south-1
export SOURCE_KAFKA_CLUSTER_BOOTSTRAP="b-2:9098,b-1:9098,b-3:9098"
export DESTINATION_KAFKA_CLUSTER_BOOTSTRAP=""
export AUTH_TYPE=IAM
export NUMBER_OF_CLUSTERS=1

export MSKCluster1SG=sg-11
export MSKCluster2SG=
export ECS_ALB_VPC=vpc-111
export ECS_ALB_PublicSubnet1=subnet-111
export ECS_ALB_PublicSubnet2=subnet-11


## Functions
function single_cluster(){
    if [ "${AUTH_TYPE}" == "IAM" ]; then
        export template_path=akhq-ecs-deploy-single-cluster-iam.yml
    else
        export template_path=akhq-ecs-deploy-single-cluster.yml
    fi
}

function two_clusters(){
   if [ "${AUTH_TYPE}" == "IAM" ]; then
        export template_path=akhq-ecs-deploy-iam.yml
    else
        export template_path=akhq-ecs-deploy.yml
  fi
}

## Start processing
if  [ ${NUMBER_OF_CLUSTERS} -eq 2 ]; then
    two_clusters
else
    single_cluster
fi


echo "Fetching ECS cluster ARN"
ECS_CLUSTER_ARN=`aws ecs describe-clusters --cluster ${ECS_CLUSTER_NAME} --region ${REGION} --query 'clusters[].clusterArn' --output text`

AWS_ECS_SERVICE_ROLE_NAME=AWSServiceRoleForECS
echo "Fetching ECS service role ARN"
AWS_ECS_SERVICE_ROLE_ARN=`aws iam get-role --role-name ${AWS_ECS_SERVICE_ROLE_NAME} --query 'Role.Arn' --output text`

echo "Deploying ECS stack"
aws cloudformation deploy --template-file ${template_path} \
   --capabilities CAPABILITY_NAMED_IAM CAPABILITY_AUTO_EXPAND \
   --stack-name akhq-ecs-deploy  \
   --parameter-overrides \
   SourceKafkaClusterBootstrap=${SOURCE_KAFKA_CLUSTER_BOOTSTRAP} \
   DestinationKafkaClusterBootstrap=${DESTINATION_KAFKA_CLUSTER_BOOTSTRAP} \
   ECSClusterArn=${ECS_CLUSTER_ARN} \
   ECSServiceRoleArn=${AWS_ECS_SERVICE_ROLE_ARN} \
   VPC=${ECS_ALB_VPC} \
   PublicSubnet1=${ECS_ALB_PublicSubnet1} \
   PublicSubnet2=${ECS_ALB_PublicSubnet2} \
   MSKCluster1SG=${MSKCluster1SG} \
   MSKCluster2SG=${MSKCluster2SG} \
   DesiredCount=1