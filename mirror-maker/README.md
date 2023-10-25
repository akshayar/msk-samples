## Build Kafka connect image [If required]
1. The kafka connect image is built with default properties specified at `mirror-maker/docker-image-build-mm2/connect-distributed.properties`. 
2. You need to build the image if you want to modify default properties. If not skip the steps below. 
2. Update `mirror-maker/docker-image-build-mm2/connect-distributed.properties` with additional parameters if required. Refer to the defaults which are used. 
2. Execute following commands to build and push docker image. 
```shell
cd $SOURCE_ROOT
cd ./mirror-maker/docker-image-build-mm2
./build.sh <image_tag[arawa3/kafka-connect-mm2]> <push_to_dh[true|false]>
# Ex  ./build.sh arawa3/kafka-connect-mm2 true
```
## Deploy using Docker on EC2 single node
1. Update `mirror-maker/ec2-docker-mm2/docker-compose-kafka-connect.yaml`  and update value of DESTINATION_BOOTSTRAP_SERVER. 
2. If you built a new image, update the image referred in `mirror-maker/ec2-docker-mm2/docker-compose-kafka-connect.yaml` .
3. Execute following commands to run the Kafka connect. 
```shell
cd $SOURCE_ROOT/mirror-maker/ec2-docker-mm2
docker-compose -f docker-compose-kafka-connect.yaml up 
```
4. Got to `Deploy MM2 connectors` section and execute command to run the 2 connectors required for MM2. 

## Deploy using Docker on ECS
1. The deployment assumes that both source and destination clusters are on AWS. They can either be MSK or self-hosted EC2.
2. The ECS task will have 3 SGs assigned - cluster SGs and an SG that gets created by the CFT.
3. Clusters SGs have self referencing rules on required port.
4. Update parameters in `mirror-maker/ecs-mm2/deploy.sh`.
5. Run following command to deploy the Kafka connect using ECS services -
```shell
cd $SOURCE_ROOT/mirror-maker/ecs-mm2
./deploy.sh
```
6. Once deployed, refer the output of the stack and follow MM2Url to access the Kafka connect REST commands.
```shell
curl -s ${MM2Url}/connectors
```
7. Got to `Deploy MM2 connectors` section and execute command to run the 2 connectors required for MM2.
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