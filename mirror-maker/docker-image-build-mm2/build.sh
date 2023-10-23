#!/bin/bash
IMAGE_TAG=$1
PUSH_TO_DOCKERHUB=$2
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

docker build -t $IMAGE_TAG .

if [ $PUSH_TO_DOCKERHUB == "true" ]; then
    docker push $IMAGE_TAG
fi
