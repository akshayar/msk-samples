#!/bin/bash
maven_dep() {
    local REPO="$1"
    local GROUP="$2"
    local PACKAGE="$3"
    local VERSION="$4"
    local FILE="$5"

    DOWNLOAD_FILE="$DOWNLOAD_FILE_TMP_PATH/$FILE"
    test -d $DOWNLOAD_FILE_TMP_PATH || mkdir -p $DOWNLOAD_FILE_TMP_PATH

    curl -sfSL -o "$DOWNLOAD_FILE" "$REPO/$GROUP/$PACKAGE/$VERSION/$FILE"
    test -f $DOWNLOAD_FILE || echo "Not found at $REPO/$GROUP/$PACKAGE/$VERSION/$FILE"

}

download_jar() {
    maven_dep $MAVEN_REPO_CONFLUENT $1 $2 $3 "$2-$3.jar"
    echo $DOWNLOAD_FILE
    test -f $DOWNLOAD_FILE || maven_dep $MAVEN_REPO_CENTRAL $1 $2 $3 "$2-$3.jar"
    test -f $DOWNLOAD_FILE || exit 1
}

debezium_zip() {
    maven_dep $MAVEN_REPO_CENTRAL $1 $2 $3 "$2-$3-plugin.zip"
    echo $DOWNLOAD_FILE
    test -f $DOWNLOAD_FILE || echo "Error: $DOWNLOAD_FILE not found"
    test -f $DOWNLOAD_FILE || exit 1
}
msk_config_providers_zip() {
  DOWNLOAD_FILE="$DOWNLOAD_FILE_TMP_PATH/$2"
  curl -sfSL -o $DOWNLOAD_FILE ${CONFIG_PROVIDER_BASE_URL}/$1/$2
  echo $DOWNLOAD_FILE
  test -f $DOWNLOAD_FILE || echo "Error: $DOWNLOAD_FILE not found"
  test -f $DOWNLOAD_FILE || exit 1
}

download_unzip_base_files(){
  debezium_zip io/debezium debezium-connector-mysql 2.2.0.Final
  unzip -q -j $DOWNLOAD_FILE -d $DOWNLOAD_FILE_TMP_PATH
  echo "Removing $DOWNLOAD_FILE"
  mkdir -p .archive
  mv $DOWNLOAD_FILE .archive/

  msk_config_providers_zip r0.2.0 msk-config-providers-0.2.0-with-dependencies.zip
  unzip -q -j $DOWNLOAD_FILE -d $DOWNLOAD_FILE_TMP_PATH
  echo "Removing $DOWNLOAD_FILE"
  mkdir -p .archive
  mv $DOWNLOAD_FILE .archive/
}

export CONFIG_PROVIDER_BASE_URL=https://github.com/aws-samples/msk-config-providers/releases/download
export MAVEN_REPO_CENTRAL=https://repo1.maven.org/maven2
export MAVEN_REPO_CONFLUENT=https://packages.confluent.io/maven
export DOWNLOAD_FILE_TMP_PATH="./debezium-connector-mysql"
rm -rf $DOWNLOAD_FILE_TMP_PATH


download_unzip_base_files
download_jar io/confluent kafka-connect-avro-converter 6.1.9
download_jar io/confluent kafka-connect-avro-data 6.1.9
download_jar io/confluent kafka-avro-serializer 6.1.9
download_jar io/confluent kafka-schema-serializer 6.1.9
download_jar io/confluent kafka-schema-registry-client 6.1.9
download_jar io/confluent common-config 6.1.9
download_jar io/confluent common-utils 6.1.9
download_jar org/apache/avro avro 1.11.0
download_jar com/google/guava guava 31.1-jre
download_jar com/google/protobuf protobuf-java 3.22.0
download_jar com/google/guava failureaccess 1.0.1

FINAL_PACKAGE_NAME=$1

if [ -z "$FINAL_PACKAGE_NAME" ]; then
  FINAL_PACKAGE_NAME="debezium-mysql-secret-manager-avro-confluent-2.2.0.Final-plugin.zip"
fi

zip -q -r ${FINAL_PACKAGE_NAME} ${DOWNLOAD_FILE_TMP_PATH}
#zip -Tvf ${FINAL_PACKAGE_NAME}
echo ${FINAL_PACKAGE_NAME}
