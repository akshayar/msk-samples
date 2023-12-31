FROM bitnami/kafka:3.5.1
USER root
RUN install_packages gettext git maven

RUN mkdir -p /tmp/kafka
RUN git -C /tmp/kafka clone https://github.com/aws-samples/sasl-scram-secrets-manager-client-for-msk.git
RUN cd /tmp/kafka/sasl-scram-secrets-manager-client-for-msk/ && \
    mvn clean install -q -f pom.xml && cp target/SaslScramSecretsManagerClient-1.0-SNAPSHOT.jar /tmp/kafka
RUN cd /tmp/kafka && rm -rf sasl-scram-secrets-manager-client-for-msk
 
RUN mvn -q org.apache.maven.plugins:maven-dependency-plugin:2.4:get  \
    -DremoteRepositories=http://download.java.net/maven/2 \
    -Dartifact=software.amazon.msk:aws-msk-iam-auth:1.1.9  \
    -Ddest=/tmp/kafka/aws-msk-iam-auth-1.1.9.jar

RUN mvn -q org.apache.maven.plugins:maven-dependency-plugin:2.4:get  \
    -DremoteRepositories=http://download.java.net/maven/2 \
    -Dartifact=io.prometheus.jmx:jmx_prometheus_javaagent:0.17.0  \
    -Ddest=/tmp/kafka/jmx_prometheus_javaagent-0.17.0.jar

ADD ./kafka-connect.yml /opt/kafka-connect/kafka-connect.yml

RUN cp /tmp/kafka/* /opt/bitnami/kafka/libs && ls /opt/bitnami/kafka/libs

ADD ./connect-distributed.properties /opt/kafka-connect/connect-distributed.properties
ADD ./runConnect.sh /opt/kafka-connect/runConnect.sh
RUN chmod +x /opt/kafka-connect/runConnect.sh

RUN mkdir -p /var/run/kafka-connect
RUN chown 1234 /var/run/kafka-connect

ENV DESTINATION_BOOTSTRAP_SERVER "destination-cluster:9092"
ENV SOURCE_BOOTSTRAP_SERVER "source-cluster:9092"
ENV GROUP_ID 1
ENV REPLICATION_FACTOR 3
ENV OFFSET_STORAGE_TOPIC "connect-offsets"
ENV CONFIG_STORAGE_TOPIC "connect-config"
ENV STATUS_STORAGE_TOPIC "connect-status"
ENV ALLOW_PLAINTEXT_LISTENER "yes"
ENV KAFKA_OPTS="-javaagent:/opt/bitnami/kafka/libs/jmx_prometheus_javaagent-0.17.0.jar=9901:/opt/kafka-connect/kafka-connect.yml"

USER 1234
#CMD /opt/kafka-connect/runMM2.sh
CMD /opt/kafka-connect/runConnect.sh