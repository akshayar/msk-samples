# mirror-maker

A mirror-maker container based on [bitnami/kafka](https://hub.docker.com/r/bitnami/kafka/) and
 [srotya/docker-kafka-mirror-maker](https://github.com/srotya/docker-kafka-mirror-maker). Now uses
 [MirrorMaker 2](https://cwiki.apache.org/confluence/display/KAFKA/KIP-382%3A+MirrorMaker+2.0)

### Build
This image is available from Docker hub however, if you would like to build it yourself here are the steps:

```
git clone https://github.com/srotya/docker-kafka-mirror-maker.git
cd docker-kafka-mirror-maker
docker build -t mirror-maker:latest .
```

**Note: Docker is expected to be installed where you run the build**

### Environment Variables
|    Variable Name    |                   Description                |   Default |
|---------------------|----------------------------------------------|------------|
|      SOURCE         | bootstrap.servers for the source kafka       |source-cluster:9092|
|    DESTINATION      | bootstrap.servers for the destination kafka  |localhost:9092|
|     TOPICS          | Topics to mirror     | .* |

#### Docker usage
```
docker run -it -e SOURCE=from.example.com:9092 -e DESTINATION=to.example.com:9092 -e TOPICS=<TOPIC NAME> mirror-maker:latest
```

#### Docker-compose usage

```
version: '2'

services:
  zookeeper:
    image: 'bitnami/zookeeper:3'
    ports:
      - '2181:2181'
    volumes:
      - 'zookeeper_data:/bitnami'
    environment:
      - ALLOW_ANONYMOUS_LOGIN=yes
  kafka:
    image: 'bitnami/kafka:2'
    ports:
      - '9092:9092'
    volumes:
      - 'kafka_data:/bitnami'
    environment:
      - KAFKA_CFG_ZOOKEEPER_CONNECT=zookeeper:2181
      - ALLOW_PLAINTEXT_LISTENER=yes
      - KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://kafka:9092
    depends_on:
      - zookeeper
  mirrormaker:
    image: 'wpietri/mirror-maker:2'
    depends_on:
      - kafka
    environment:
      - SOURCE=mysourcekafka.example.com:9092
      - DESTINATION=kafka:9092
      - TOPICS=Topic1,Topic2


volumes:
  zookeeper_data:
    driver: local
  kafka_data:
    driver: local

```

### License

Apache 2.0
