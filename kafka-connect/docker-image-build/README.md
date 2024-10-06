# Kafka Connect / mirror-maker

A Kafka Connect container based on [bitnami/kafka](https://hub.docker.com/r/bitnami/kafka/) and
 [srotya/docker-kafka-mirror-maker](https://github.com/srotya/docker-kafka-mirror-maker). Now uses
 [MirrorMaker 2](https://cwiki.apache.org/confluence/display/KAFKA/KIP-382%3A+MirrorMaker+2.0)

### Build
This image is available from Docker hub (arawa3/kafka-connect), if you would like to build it yourself here are the steps:

```
git clone https://github.com/akshayar/msk-samples.git
cd msk-samples/kafka-connect
./build.sh <image_tag> [PUSH_TO_DOCKERHUB true|false] [PLUGIN_S3_PATH]
-- PLUGIN_S3_PATH - Zip file path of the plugin which you want to add.
```

#### Docker usage
Following are the parameters and default values -
1. BOOTSTRAP_SERVER "target-cluster:9092"
2. GROUP_ID 1
3. REPLICATION_FACTOR 3
4. OFFSET_STORAGE_TOPIC "connect-offsets"
5. CONFIG_STORAGE_TOPIC "connect-config"
6. STATUS_STORAGE_TOPIC "connect-status"
7. AWS_REGION "ap-south-1"
8. AUTH_TYPE "IAM"
9. PLUGIN_ZIP_S3_PATH ""
```
docker run -it -e BOOTSTRAP_SERVER=target:9092  arawa3/kafka-connect
```

#### Docker-compose usage

### License

Apache 2.0
