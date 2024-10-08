#!/bin/bash
git clone https://github.com/aws-samples/msk-config-providers.git
cd msk-config-providers || exit
git checkout tags/r0.2.0 -b r0.2.0
mvn clean install -DskipTests
