## Deploy on EC2
1. The deployment assumes that both source and destination clusters are on AWS. They can either be MSK or self-hosted EC2.
2. The EC2 task will have 3 SGs assigned - cluster SGs and an SG that gets created by the CFT.
3. Clusters SGs have self referencing rules on required port.
4. Update parameters in `mirror-maker/ec2-kafka-connect-cluster/deploy.sh`.
6. Run following command to deploy the Kafka connect using ECS services -
```shell
cd $SOURCE_ROOT/mirror-maker/ec2-kafka-connect-cluster
./deploy.sh
```
6. Once deployed, login to the EC2 and run following command to start the Kafka connect in cluster mode.
```shell
sudo systemctl start kafka-connect.service
sudo systemctl status kafka-connect.service
```
7. Got to `Deploy MM2 connectors` section and execute command to run the 3 connectors required for MM2.

## Deploy MM2 connectors
1. SSH to the EC2 and run set the Kafka connect url. 
```shell
#export KAFKA_CONNECT_URL="http://kafka-connect-mm2-2035114508.ap-south-1.elb.amazonaws.com"
export KAFKA_CONNECT_URL="http://localhost:8083"
```
2. View the configurations of connectors. 
```shell
cd /home/ec2-user/kafka-connect/mm2/
cat mm2-msc-cust-repl-policy.json
cat mm2-cpc-cust-repl-policy.json
cat mm2-hbc-no-auth.json
```
### Delete Connectors
```shell
curl -X DELETE ${KAFKA_CONNECT_URL}/connectors/mm2-msc  
curl -X DELETE ${KAFKA_CONNECT_URL}/connectors/mm2-cpc  
curl -X DELETE ${KAFKA_CONNECT_URL}/connectors/mm2-hbc  
```
### Create Connectors
```shell
cd /home/ec2-user/kafka-connect/mm2/
curl -X PUT -H "Content-Type: application/json" --data @mm2-msc-cust-repl-policy.json ${KAFKA_CONNECT_URL}/connectors/mm2-msc/config 
curl -s ${KAFKA_CONNECT_URL}/connectors/mm2-msc/status 

curl -X PUT -H "Content-Type: application/json" --data @mm2-cpc-cust-repl-policy.json ${KAFKA_CONNECT_URL}/connectors/mm2-cpc/config  
curl -s ${KAFKA_CONNECT_URL}/connectors/mm2-cpc/status 

curl -X PUT -H "Content-Type: application/json" --data @mm2-hbc-no-auth.json ${KAFKA_CONNECT_URL}/connectors/mm2-hbc/config 
curl -s ${KAFKA_CONNECT_URL}/connectors/mm2-hbc/status 
```