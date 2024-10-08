## Glue Schema Registry and Kafka Message Producer 
1. Refer to the sample code https://github.com/awslabs/aws-glue-schema-registry/tree/master 
2. The example in this repository does a demo of Glue Schema Registry ( GSR) integration with Kafka Producer code
3. Required dependencies -
```xml
    <dependency>
      <groupId>software.amazon.glue</groupId>
      <artifactId>schema-registry-serde</artifactId>
      <version>${glue.schema.reg.version}</version>
    </dependency>
```
4. Required Kafka Configurations -
```properties
# Serializer/Deserializer Configuration
key.serializer=org.apache.kafka.common.serialization.StringSerializer
value.serializer=com.amazonaws.services.schemaregistry.serializers.GlueSchemaRegistryKafkaSerializer
key.deserializer=org.apache.kafka.common.serialization.StringDeserializer
value.deserializer=com.amazonaws.services.schemaregistry.deserializers.GlueSchemaRegistryKafkaDeserializer

## AWS Glue Schema Registry config
region=ap-south-1
registry.name=<registry-name>
timeToLiveMillis=86400000
cacheSize=10
compatibility=FULL
compression=ZLIB
schemaAutoRegistrationEnabled=true
schemaName=<schema-name>
```
## Pre-requisite to run the example
1. Create Glue Schema registry
2. Update [kafka.properties](src/main/resources/kafka.properties) / [kafka_iam.properties](src/main/resources/kafka_iam.properties) / [kafka_sasl.properties](src/main/resources/kafka_sasl.properties) . 
   Update "registry.name" 

   Update other Kafka properties
3. Update [application.yaml](src/main/resources/application.yaml)
   #JSON,JSON_WITH_SCHEMA,AVRO, PROTOBUF

   messageFormat=AVRO 
4. Ensure right permission on the EC2/Client machine to access GSR. 
5. Install and confiure Maven and Java. 

## Execute the code
Execute 'run.sh' 
   

