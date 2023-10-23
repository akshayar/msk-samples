## Create embedded source and destination clusters
1. Update private IP in `docker-compose-two-kafka-clusters.yaml` with private IP of the EC2 node. Replace `ip-10-0-0-21.ap-south-1.compute.internal`. 
2. Run following command to run source and destination cluster.
```shell
docker-compose -f docker-compose-two-kafka-clusters.yaml up
```
## Create Kafka connect
1. Update `docker-compose-kafka-connect.yaml` . Update DESTINATION_BOOTSTRAP_SERVER to privateid:9093 and REPLICATION_FACTOR=1. 
2. Run following command to run Kafka connect.
```shell
docker-compose -f docker-compose-kafka-connect.yaml up
```
## Create MM2 connectors
