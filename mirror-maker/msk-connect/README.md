```shell
aws iam create-role --role-name mm2-execution-role --assume-role-policy-document '{
    "Version": "2012-10-17",
    "Statement": [
      {
        "Effect": "Allow",
        "Principal": {
          "Service": "kafkaconnect.amazonaws.com"
        },
        "Action": "sts:AssumeRole"
      }
    ]
  }'
```
```shell
aws iam put-role-policy --role-name mm2-execution-role --policy-name msk-connect --policy-document '{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Sid": "mskconnect",
            "Effect": "Allow",
            "Action": [
                "kafkaconnect:*",
                "s3:*",
                "iam:PassRole",
                "kafka:*",
                "logs:*",
                "ec2:*"
            ],
            "Resource": "*"
        }
    ]
}'

```

```shell
mkdir -p .tmp
```
```shell
mkdir mm2 
zip mm2.zip mm2 
export bucket_name=aksh-code-binaries-2
export plugin_name=mm2-custom-replication-policy
aws s3 cp mm2.zip s3://${bucket_name}/msk-connect-plugin/
```
```shell
cat << EOF > .tmp/create-custom-plugin.json
{
    "name": "${plugin_name}",
    "contentType": "ZIP",
    "location": {
        "s3Location": {
            "bucketArn": "arn:aws:s3:::${bucket_name}",
            "fileKey": "msk-connect-plugin/mm2-custom-replication-policy.zip"
        }
    }
}
EOF
```
```shell
cat .tmp/create-custom-plugin.json
export custom_plugin_arn=$(aws kafkaconnect create-custom-plugin --cli-input-json file://.tmp/create-custom-plugin.json --query customPluginArn --output text)
echo $custom_plugin_arn
rm create-custom-plugin.json
```
```shell
worker_config_name=mm2-worker-configuration
worker_properties_file=mm2-worker-configuration.properties
./create-worker-config.sh mm2-worker-configuration mm2-worker-configuration.properties

```
```shell
./deploy-cli.sh

```
