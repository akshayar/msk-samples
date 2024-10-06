FROM bitnami/kafka:3.5.1
USER root
RUN install_packages gettext git maven unzip

RUN install_packages python3 python3-pip && pip3 install awscli

RUN mkdir -p /tmp/kafka

RUN git -C /tmp/kafka clone https://github.com/aws-samples/sasl-scram-secrets-manager-client-for-msk.git
RUN cd /tmp/kafka/sasl-scram-secrets-manager-client-for-msk/ && \
    mvn clean install -q -f pom.xml && cp target/SaslScramSecretsManagerClient-1.0-SNAPSHOT.jar /tmp/kafka
RUN cd /tmp/kafka && rm -rf sasl-scram-secrets-manager-client-for-msk

RUN mvn -q org.apache.maven.plugins:maven-dependency-plugin:2.4:get  \
    -DremoteRepositories=http://download.java.net/maven/2 \
    -DgroupId=software.amazon.msk -DartifactId=aws-msk-iam-auth -Dversion=2.0.3 -Dclassifier=all \
    -Ddest=/tmp/kafka/aws-msk-iam-auth-2.0.3-all.jar

RUN mvn -q org.apache.maven.plugins:maven-dependency-plugin:2.4:get  \
    -DremoteRepositories=http://download.java.net/maven/2 \
    -Dartifact=io.prometheus.jmx:jmx_prometheus_javaagent:0.17.0  \
    -Ddest=/tmp/kafka/jmx_prometheus_javaagent-0.17.0.jar

RUN cp /tmp/kafka/* /opt/bitnami/kafka/libs && ls /opt/bitnami/kafka/libs
ADD ./prometheus-kafka-connect.yml /opt/kafka-connect/prometheus-kafka-connect.yml

RUN mkdir -p /opt/bitnami/kafka/plugins && chown 1234 /opt/bitnami/kafka/plugins
COPY ./plugins/ /opt/bitnami/kafka/plugins/


ADD ./connect-distributed.properties /opt/kafka-connect/connect-distributed.properties
ADD ./connect-distributed-iam.properties /opt/kafka-connect/connect-distributed-iam.properties
ADD ./runConnect.sh /opt/kafka-connect/runConnect.sh
RUN chmod +x /opt/kafka-connect/runConnect.sh

RUN mkdir -p /var/run/kafka-connect
RUN chown 1234 /var/run/kafka-connect

ENV BOOTSTRAP_SERVER "target-cluster:9092"
ENV GROUP_ID 1
ENV REPLICATION_FACTOR 3
ENV OFFSET_STORAGE_TOPIC "connect-offsets"
ENV CONFIG_STORAGE_TOPIC "connect-config"
ENV STATUS_STORAGE_TOPIC "connect-status"
ENV ALLOW_PLAINTEXT_LISTENER "yes"
ENV KAFKA_OPTS="-javaagent:/opt/bitnami/kafka/libs/jmx_prometheus_javaagent-0.17.0.jar=9901:/opt/kafka-connect/prometheus-kafka-connect.yml"
ENV PLUGIN_ZIP_S3_PATH ""
ENV AWS_REGION "ap-south-1"
## IAM or PLAINTEXT
ENV AUTH_TYPE "IAM"

USER 1234
#CMD /opt/kafka-connect/runMM2.sh
CMD /opt/kafka-connect/runConnect.sh