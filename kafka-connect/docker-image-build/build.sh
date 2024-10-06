#!/bin/bash
IMAGE_TAG=$1
PUSH_TO_DOCKERHUB=$2
PLUGIN_S3_PATH=$3

if [ -z $IMAGE_TAG ]; then
    IMAGE_TAG="arawa3/kafka-connect-mm2"
    echo "IMAGE_TAG not specified, defaulting to arawa3/kafka-connect-mm2"
    echo "Usage: build.sh <image_tag> [true|false]"
    if [ -z $PUSH_TO_DOCKERHUB ]; then
        PUSH_TO_DOCKERHUB=true
        echo "PUSH_TO_DOCKERHUB not specified, defaulting to true"
        echo "Usage: build.sh <image_tag> [true|false]"
    fi
fi

echo "IMAGE_TAG: $IMAGE_TAG"
echo "PUSH_TO_DOCKERHUB: $PUSH_TO_DOCKERHUB"
echo "PLUGIN_S3_PATH: $PLUGIN_S3_PATH"

mkdir -p ./plugins/
if [ -z "$PLUGIN_S3_PATH" ]; then
  echo "No plugin to add"
else
  echo "Copying plugin from $PLUGIN_S3_PATH"
  aws s3 cp "$PLUGIN_S3_PATH" ./plugins/
  ## iterate through zip files and extract
  for file in ./plugins/*.zip; do
    echo "Extracting $file"
    unzip -o "$file" -d ./plugins/
  done
fi

docker build -t $IMAGE_TAG .


if [ "$PUSH_TO_DOCKERHUB" == "true" ]; then
    docker push $IMAGE_TAG
fi
