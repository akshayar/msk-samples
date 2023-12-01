```shell
mkdir mm2-custom-replication-policy  
git clone https://github.com/aws-samples/mirrormaker2-msk-migration.git
cd mirrormaker2-msk-migration/ && mvn clean install -q -f pom.xml && cp CustomMM2ReplicationPolicy/target/CustomMM2ReplicationPolicy-1.0-SNAPSHOT.jar ../mm2-custom-replication-policy
cd ..
zip -r mm2-custom-replication-policy.zip mm2-custom-replication-policy
zip -Tvf mm2-custom-replication-policy.zip
rm -rf mm2-custom-replication-policy  

```
```shell
export bucket_name=aksh-code-binaries-2
export plugin_name=mm2-custom-replication-policy
```
```shell
aws s3 cp mm2-custom-replication-policy.zip s3://${bucket_name}/msk-connect-plugin/

cat << EOF > create-custom-plugin.json
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
cat create-custom-plugin.json
export custom_plugin_arn=$(aws kafkaconnect create-custom-plugin --cli-input-json file://create-custom-plugin.json --query customPluginArn --output text)
echo $custom_plugin_arn
rm create-custom-plugin.json
```