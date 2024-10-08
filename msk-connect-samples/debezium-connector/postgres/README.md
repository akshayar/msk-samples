# Create ZIP file package for MSK Connect Plugin
1. Use Maven version 3.8.8 or higher
2. Use Java 15.0.2 or higher. 
```
cd <source-root>/debezium-connector/postgres/package-plugin
./checkout-build-dependent.sh

### To build package for connect with AWS config, and JSON output
mvn  install -DskipTests -f pom-debezium-connector-postgres-aws-config.xml

### To build package for connector with AWS config, Glue Schema registry and AVRO 
mvn  install -DskipTests -f pom-debezium-connector-postgres-gsr-avro.xml

### To build package for connector with AWS config, Glue Schema registry and Protobuff 
mvn  install -DskipTests -f pom-debezium-connector-postgres-gsr-protobuf.xml

### To build package for connector with AWS config, Confluent Schema registry and Protobuff 
mvn  install -DskipTests -f pom-debezium-connector-postgres-confluent.xml

## The package file will be created in the target folder. 
```
# Create Worker Configuration
1. Refer the worker configuraiton template at [worker configuraiton template](templates/worker-configuration-secret-manager.properties) which showcases secret manager config provider. Apart form that it uses default schema registery and AVRO converte which could be overwritten by connectors.
2. The worker configuration template at [SSM and secret manager configuration template](templates/worker-configuration.properties) shows how SSM can be used to read properties.
3. Use the sample below to create the worker configuration and use the ARN to create connectors subsequently.
```shell
export worker_config_name=<<worker_config_name>>
export worker_properties_file=$source_root/debezium-connector/postgres/templates/worker-configuration-secret-manager.properties
```
```shell
cd $source_root
./create-worker-config.sh $worker_config_name $worker_properties_file 
export worker_config_arn=`cat worker_config_arn.txt`
echo "worker_config_arn=$worker_config_arn"
```

# PostgreSQL Debezium Plugin with Secrets Manager Integration , JSON Output
## Create custom plugin
For this example use debezium-connector-postgres-aws-config-2.7.2.Final-plugin.zip
```shell
export custom_plugin_name=<<<custom_plugin_name>>>
export bucket_name=<<<bucket_name>>>
aws s3 cp target/<zip-file-package> s3://${bucket_name}/msk-connect-plugin/
```
```shell
cat << EOF > create-custom-plugin.json
{
    "name": "${custom_plugin_name}",
    "contentType": "ZIP",
    "location": {
        "s3Location": {
            "bucketArn": "arn:aws:s3:::${bucket_name}",
            "fileKey": "msk-connect-plugin/<zip-file-package>"
        }
    }
}
EOF
export custom_plugin_arn=$(aws kafkaconnect create-custom-plugin --cli-input-json file://create-custom-plugin.json --query customPluginArn --output text)
echo "Custom plugin ARN: ${custom_plugin_arn}"
```

## Check connectivity 
```shell
## Ensure that there is ingress from self on these ports  
# 9092(if plain, no auth), 9094(if TLS,no auth), 9098(if TLS,IAM)
## Ensure that thers is Egress on all ports, all ip , or
## to self on 9092(if plain, no auth), 9094(if TLS,no auth), 9098(if TLS,IAM)
aws ec2 describe-security-groups  --group-ids $msk_security_group \
--query 'SecurityGroups[0].IpPermissions' --output table

aws ec2 describe-security-groups  --group-ids $msk_security_group \
--query 'SecurityGroups[0].IpPermissionsEgress' --output table

```
## Create the connector JSON output, TLS No Auth
1. Refer to the connector template [JSON no-auth connector tempalte](templates/debezium-postgres-secret-manager-json-noauth-plaintext.json) which generates JSON output and connects on MSK on TLS without authentication.
2. Update the configuration and use the config to create the connector. 
3. The sample below uses the sample at [sample config](samples/debezium-postgres-secret-manager-json-noauth-plaintext.json) to generate the connector. 
```shell
cd $source_root
export connector_config_file=$source_root/debezium-connector/postgres/samples/debezium-mysql-secret-manager-json-noauth-plaintext.json
./create-connector.sh  ${connector_config_file} 

```

## Create the connector JSON output, IAM Auth
1. Refer to the connector template [JSON IAM auth connector template](templates/debezium-postgres-secret-manager-json-iam.json) which generates JSON output and connects on MSK on TLS without authentication.
2. Update the configuration and use the config to create the connector.
3. The sample below uses the sample at [sample config](samples/debezium-postgres-secret-manager-json-iam.json) to generate the connector.
```shell
cd $source_root
export connector_config_file=$source_root/debezium-connector/postgres/samples/debezium-mysql-secret-manager-json-iam.json
./create-connector.sh  ${connector_config_file} 

```


# PostgreSQL Debezium Plugin with Secrets Manager Integration , AVRO Output and Glue Schema Registry
## Create custom plugin
For this example use debezium-connector-postgres-gsr-avro-2.7.2.Final-plugin.zip
```shell
export custom_plugin_name=<<<custom_plugin_name>>>
export bucket_name=<<<bucket_name>>>
aws s3 cp target/<zip-file-package> s3://${bucket_name}/msk-connect-plugin/
```
```shell
cat << EOF > create-custom-plugin.json
{
    "name": "${custom_plugin_name}",
    "contentType": "ZIP",
    "location": {
        "s3Location": {
            "bucketArn": "arn:aws:s3:::${bucket_name}",
            "fileKey": "msk-connect-plugin/<zip-file-package>"
        }
    }
}
EOF
export custom_plugin_arn=$(aws kafkaconnect create-custom-plugin --cli-input-json file://create-custom-plugin.json --query customPluginArn --output text)
echo "Custom plugin ARN: ${custom_plugin_arn}"
```
## Create the connector AVRO output, Glue Schema Registry, IAM Auth

# To DO
1. Check the topic prefix is unique. If not warn customers. The script can look at all the MSK connector and warn. 
2. Based on SG config check if MSK and MSK connect can talk to each other. 
3. 