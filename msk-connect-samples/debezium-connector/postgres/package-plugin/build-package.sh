#!/bin/bash
./checkout-build-dependent.sh
mvn  install -DskipTests -f pom-debezium-connector-postgres-aws-config.xml
mvn  install -DskipTests -f pom-debezium-connector-postgres-gsr-avro.xml
mvn  install -DskipTests -f pom-debezium-connector-postgres-gsr-protobuf.xml
mvn  install -DskipTests -f pom-debezium-connector-postgres-confluent.xml
