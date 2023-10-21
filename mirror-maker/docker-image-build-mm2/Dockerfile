FROM bitnami/kafka:3.5.1
USER root
RUN install_packages gettext git maven

RUN mkdir -p /tmp/kafka
RUN git -C /tmp/kafka clone https://github.com/aws-samples/sasl-scram-secrets-manager-client-for-msk.git
RUN cd /tmp/kafka/sasl-scram-secrets-manager-client-for-msk/ && \
    mvn clean install -q -f pom.xml && cp target/SaslScramSecretsManagerClient-1.0-SNAPSHOT.jar /tmp/kafka
RUN cd /tmp/kafka && rm -rf sasl-scram-secrets-manager-client-for-msk
 
RUN git -C /tmp/kafka clone https://github.com/aws-samples/mirrormaker2-msk-migration.git
RUN cd /tmp/kafka/mirrormaker2-msk-migration/ \
    && mvn clean install -q -f pom.xml && cp CustomMM2ReplicationPolicy/target/CustomMM2ReplicationPolicy-1.0-SNAPSHOT.jar /tmp/kafka
RUN cd /tmp/kafka && rm -rf mirrormaker2-msk-migration

RUN mvn -q org.apache.maven.plugins:maven-dependency-plugin:2.4:get  \
    -DremoteRepositories=http://download.java.net/maven/2 \
    -Dartifact=software.amazon.msk:aws-msk-iam-auth:1.1.9  \
    -Ddest=/tmp/kafka/aws-msk-iam-auth-1.1.9.jar

RUN cp /tmp/kafka/* /opt/bitnami/kafka/libs && ls /opt/bitnami/kafka/libs

ADD ./connect-distributed.properties /opt/kafka-connect/connect-distributed.properties
ADD ./runConnect.sh /opt/kafka-connect/runConnect.sh
RUN chmod +x /opt/kafka-connect/runConnect.sh


ADD ./connect-mirror-maker.properties /opt/kafka-connect/connect-mirror-maker.properties
ADD ./runMM2.sh /opt/kafka-connect/runMM2.sh
RUN chmod +x /opt/kafka-connect/runMM2.sh

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

USER 1234
#CMD /opt/kafka-connect/runMM2.sh
CMD /opt/kafka-connect/runConnect.sh