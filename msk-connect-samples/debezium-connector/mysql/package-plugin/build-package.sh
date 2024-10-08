#!/bin/bash
./checkout-build-dependent.sh
mvn  install -DskipTests -f pom-debezium-connector-mysql-aws-config.xml
mvn  install -DskipTests -f pom-debezium-connector-mysql-confluent.xml
mvn  install -DskipTests -f pom-debezium-connector-mysql-gsr-avro.xml
