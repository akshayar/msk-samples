# Create custom plugin and worker configuration
https://debezium.io/documentation/reference/stable/transformations/partition-routing.html
## Build the package for MSK Connect Plugin - MySQL Debezium with Secrets Manager Integration
The package created bellow will work for both JSON and AVRO output. 
1. For AVRO output you need additional jar for AVRO connector.
2. Use the instructions below to create the package.
3. Once the package is created , use subsequent instructions to upload to S3 and create custom connector.
```shell
cd $source_root/debezium-connector/mysql
export zip_file=debezium-connector-mysql-secret-manager-avro-confluent-2.2.0.Final-plugin.zip
./build-custom-plugin-package.sh $zip_file
```
4. The steps above will create debezium-connector-mysql-secret-manager-avro-confluent-2.2.0.Final-plugin.zip file. 
## Create custom plugin
```shell
export custom_plugin_name=debezium-mysql-secret-manager-avro-confluent
export bucket_name=<<<bucket_name>>>
```
```shell
aws s3 cp ${zip_file} s3://${bucket_name}/msk-connect-plugin/
cat << EOF > create-custom-plugin.json
{
    "name": "${custom_plugin_name}",
    "contentType": "ZIP",
    "location": {
        "s3Location": {
            "bucketArn": "arn:aws:s3:::${bucket_name}",
            "fileKey": "msk-connect-plugin/${zip_file}"
        }
    }
}
EOF
export custom_plugin_arn=$(aws kafkaconnect create-custom-plugin --cli-input-json file://create-custom-plugin.json --query customPluginArn --output text)
echo "Custom plugin ARN: ${custom_plugin_arn}"
```
## Create Worker Configuration
1. Refer the worker configuraiton template at [worker configuraiton template](templates/archive/worker-configuration-secret-manager.properties) which showcases secret manager config provider. Apart form that it uses default schema registery and AVRO converte which could be overwritten by connectors.
2. The worker configuration template at [SSM and secret manager configuration template](templates/archive/worker-configuration.properties) shows how SSM can be used to read properties.
3. Use the sample below to create the worker configuration and use the ARN to create connectors subsequently.
```shell
export worker_config_name=debezium-connector-mysql
export worker_properties_file=$source_root/debezium-connector/mysql/templates/worker-configuration-secret-manager.properties
```
```shell
cd $source_root
./create-worker-config.sh $worker_config_name $worker_properties_file 
export worker_config_arn=`cat worker_config_arn.txt`
echo "worker_config_arn=$worker_config_arn"
```
# Check connectivity
```shell
## Ensure that there is ingress from self on these ports  
# 9092(if plain, no auth), 9094(if TLS,no auth), 9098(if TLS,IAM)
## Ensure that thers is Egress on all ports, all ip , or
## to self on 9092(if plain, no auth), 9094(if TLS,no auth), 9098(if TLS,IAM)
## Ensure DB SG has ingress for MSK connect security group. 
aws ec2 describe-security-groups  --group-ids $msk_security_group \
--query 'SecurityGroups[0].IpPermissions' --output table

aws ec2 describe-security-groups  --group-ids $msk_security_group \
--query 'SecurityGroups[0].IpPermissionsEgress' --output table

```
# Create connectors
## Create the connector JSON output, Confluent Schema Registry, IAM Auth , Secrets Manager Integration
1. Refer to the connector cloudformation template [JSON IAM Template](./mysql/templates/debezium-mysql-secret-manager-json-iam.yml) which generates connector with JSON output and connects on MSK with IAM auth.
2. Update the [script](../archive/deploy-connector-json.sh) which invokes this cloudformation template. 
3. Execute the script.
```shell
cd $source_root/debezium-connector/mysql
./deploy-connector-json.sh
```
## Create the connector AVRO output, Confluent Schema Registry, No Auth, plaintext , Secrets Manager Integration
1. Refer to the connector cloudformation template [AVRO No auth plaintext template](templates/debezium-mysql-secret-manager-avro-plaintext.yml) which generates connector with AVRO output and connects on MSK on plaintext , no auth.
2. Update the [script](../archive/deploy-connector-avro.sh) which invokes this cloudformation template.
3. Execute the script.
```shell
cd $source_root/debezium-connector/mysql
./deploy-connector-json.sh
```
## Create the connector AVRO output, Confluent Schema Registry, No Auth, TLS , Secrets Manager Integration
1. Refer to the connector cloudformation template [AVRO No auth TLS template](templates/debezium-mysql-secret-manager-avro-tls.yml) which generates connector with AVRO output and connects on MSK on TLS , no auth.
2. Update the [script](../archive/deploy-connector-avro.sh) which invokes this cloudformation template.
3. Execute the script.
```shell
cd $source_root/debezium-connector/mysql
./deploy-connector-json.sh
```
## Create the connector AVRO output, Glue Schema Registry, IAM Auth

# To DO
1. Check the topic prefix is unique. If not warn customers. The script can look at all the MSK connector and warn. 
2. Based on SG config check if MSK and MSK connect can talk to each other. 
3. 