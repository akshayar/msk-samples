## Build Kafka connect image [If required]
1. The kafka connect image is built with default properties specified at `kafka-connect/docker-image-build/connect-distributed.properties` or `kafka-connect/docker-image-build/connect-distributed-iam.properties`. 
2. You need to build the image if you want to modify default properties. If not skip the steps below. 
2. Update `kafka-connect/docker-image-build/connect-distributed.properties` with additional parameters if required. Refer to the defaults which are used. 
2. Execute following commands to build and push docker image. 
```shell
cd $SOURCE_ROOT
cd ./kafka-connect/docker-image-build
./build.sh <image_tag[arawa3/kafka-connect-mm2]> <push_to_dh[true|false]> <plugin zip file s3 path>
# Ex  ./build.sh arawa3/kafka-connect-mm2 true
```

## Deploy on ECS
1. The deployment assumes that both source and destination clusters are on AWS. They can either be MSK or self-hosted EC2.
2. The ECS task will have 3 SGs assigned - cluster SGs and an SG that gets created by the CFT.
3. Clusters SGs have self referencing rules on required port.
4. Create ECS cluster using https://docs.aws.amazon.com/AmazonECS/latest/developerguide/ECS_AWSCLI_Fargate.html. 
```shell
aws ecs create-cluster --cluster-name mm2-fargate-cluster
```
5. Update parameters in `kafka-connect/ecs-deploy/deploy.sh`.
6. Run following command to deploy the Kafka connect using ECS services -
```shell
cd $SOURCE_ROOT/kafka-connect/ecs-deploy
./deploy.sh
```
6. Once deployed, refer the output of the stack and follow KCUrl to access the Kafka connect REST commands.
```shell
curl -s ${MM2Url}/connectors
```
7. Got to `Deploy MM2 connectors` section for MM2 connector deploy instructions. 


## Deploy MM2 connectors
```shell
#export KAFKA_CONNECT_URL="http://kafka-connect-mm2-2035114508.ap-south-1.elb.amazonaws.com"
export KAFKA_CONNECT_URL="http://localhost:8083"
```
### Delete Connectors
```shell
curl -X DELETE ${KAFKA_CONNECT_URL}/connectors/mm2-msc  
curl -X DELETE ${KAFKA_CONNECT_URL}/connectors/mm2-cpc  
curl -X DELETE ${KAFKA_CONNECT_URL}/connectors/mm2-hbc  
```
### Create Connectors
```shell
cd mirror-maker/connectors/no-auth/
mkdir .tmp
export MSK_SOURCE_BOOTSTRAP=
export MSK_DESTINATION_BOOTSTRAP=
export REPLICATION_FACTOR=3 ## Use 1 if Kafka cluster is running on single node on docker.  

envsubst < mm2-msc-cust-repl-policy.json > .tmp/mm2-msc-cust-repl-policy.json
envsubst < mm2-cpc-cust-repl-policy.json > .tmp/mm2-cpc-cust-repl-policy.json
envsubst < mm2-hbc-no-auth.json > .tmp/mm2-hbc-no-auth.json

curl -X PUT -H "Content-Type: application/json" --data @.tmp/mm2-msc-cust-repl-policy.json ${KAFKA_CONNECT_URL}/connectors/mm2-msc/config 
curl -s ${KAFKA_CONNECT_URL}/connectors/mm2-msc/status 

curl -X PUT -H "Content-Type: application/json" --data @.tmp/mm2-cpc-cust-repl-policy.json ${KAFKA_CONNECT_URL}/connectors/mm2-cpc/config  
curl -s ${KAFKA_CONNECT_URL}/connectors/mm2-cpc/status 

curl -X PUT -H "Content-Type: application/json" --data @.tmp/mm2-hbc-no-auth.json ${KAFKA_CONNECT_URL}/connectors/mm2-hbc/config 
curl -s ${KAFKA_CONNECT_URL}/connectors/mm2-hbc/status 
```