package com.aws.sample.main;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ExecutionException;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.kafka.AWSKafka;
import com.amazonaws.services.kafka.AWSKafkaClientBuilder;
import com.amazonaws.services.kafka.model.GetBootstrapBrokersRequest;
import com.amazonaws.services.kafka.model.ListClustersRequest;
import com.amazonaws.services.kafka.model.ListClustersResult;
import com.amazonaws.services.securitytoken.AWSSecurityTokenService;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClientBuilder;
import com.amazonaws.services.securitytoken.model.AssumeRoleRequest;
import com.amazonaws.services.securitytoken.model.AssumeRoleResult;
import com.amazonaws.services.securitytoken.model.Credentials;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.errors.TopicExistsException;

public class KafkaConfigLoader {
    public static final String LAMBDA_PROPERTY_AUTH_TYPE = "AUTH_TYPE";
    public static final String LAMBDA_PROPERTY_BOOTSTRAP_SERVER = "BOOTSTRAP_SERVER";
    public static final String LAMBDA_PROPERTY_MSK_ARN = "MSK_ARN";
    public static final String LAMBDA_PROPERTY_SCHEMA_REGISTRY = "SCHEMA_REGISTRY";
    public static final String LAMBDA_PROPERTY_TOPIC = "TOPIC";
    public static final String LAMBDA_PROPERTY_REPLICATION_FACTOR = "REPLICATION_FACTOR";
    public static final String LAMBDA_PROPERTY_PARTITIONS = "PARTITIONS";
    public static final String LAMBDA_PROPERTY_TEMPLATE_BUCKET = "BUCKET";
    public static final String LAMBDA_PROPERTY_TEMPLATE_PATH = "TEMPLATE_PATH";
    public static final String LAMBDA_PROPERTY_MSK_ACT_ROLE = "MSK_ACT_ROLE";
    private Map<String, String> kafkaProperties;

    public KafkaConfigLoader(Map<String, String> kafkaProperties) {
        this();
        Map<String,String> in=Optional.ofNullable(kafkaProperties).orElse(Collections.emptyMap());
        in.forEach((k,v)-> Optional.ofNullable(kafkaProperties).ifPresent(m->m.put(k+"",v+"")));
    }

    public KafkaConfigLoader() {
        this.kafkaProperties = System.getenv();
    }


    private Properties loadConfig(final String configFile) throws IOException {
        if (!Files.exists(Paths.get(configFile))) {
            throw new IOException(configFile + " not found.");
        }
        final Properties cfg = new Properties();
        try (InputStream inputStream = new FileInputStream(configFile)) {
            cfg.load(inputStream);
        }
        return cfg;
    }

    public Properties loadConfig(InputStream in) throws IOException {
        final Properties cfg = new Properties();
        cfg.load(in);
        return cfg;
    }

    public String loadLambdaProperty(String name, String defaultValue) {
        return Optional.ofNullable(kafkaProperties.get(name)).filter(s -> !StringUtils.isEmpty(s)).orElse(defaultValue);
    }


    public Properties loadKafkaProperties() throws IOException {
        String authType = loadLambdaProperty(LAMBDA_PROPERTY_AUTH_TYPE, "NONE");
        Properties kafkaConfig = new Properties();
        if ("IAM".equalsIgnoreCase(authType)) {
            kafkaConfig = loadConfig(getClass().getClassLoader().getResourceAsStream("kafka-iam.properties"));
            String saslJaasConfig=kafkaConfig.get("sasl.jaas.config")+"";
            String region=getRegions().getName();
            String roleToCallMsk = loadLambdaProperty(LAMBDA_PROPERTY_MSK_ACT_ROLE, null);
            if(roleToCallMsk==null || roleToCallMsk.isEmpty()){
                kafkaConfig.put("sasl.jaas.config","software.amazon.msk.auth.iam.IAMLoginModule required;");
            }else{
                String saslNewConfig=saslJaasConfig.replaceAll("IAM_ROLE_TO_ASSUME",roleToCallMsk).replaceAll("REGION_FOR_ASSUME_ROLE",region);
                kafkaConfig.put("sasl.jaas.config",saslNewConfig);
            }

        }
        String bootstrapBrokerProperty = loadLambdaProperty(LAMBDA_PROPERTY_BOOTSTRAP_SERVER, null);
        String bootstrap = Optional.ofNullable(bootstrapBrokerProperty).or(() -> {
            LogHelper.logger.log("Null Bootstrap broker, getting from arn");
            String mskArn = loadLambdaProperty(LAMBDA_PROPERTY_MSK_ARN, null);
            LogHelper.logger.log("MSk Arn" + mskArn);
            Optional<String> bootstrapTemp = Optional.ofNullable(mskArn).map(s -> kafkaBrokers(s, authType));
            LogHelper.logger.log("Bootstrap " + bootstrapTemp);
            return bootstrapTemp;
        }).orElseThrow(() -> new RuntimeException("Can't get bootstrap brokers"));

        kafkaConfig.put("bootstrap.servers", bootstrap);

        String schemaRegistry = loadLambdaProperty(LAMBDA_PROPERTY_SCHEMA_REGISTRY, null);
        if (Optional.ofNullable(schemaRegistry).isPresent()) {
            kafkaConfig.put("schema.registry.url", schemaRegistry);
        }
        // Add additional properties.
        kafkaConfig.put(ProducerConfig.ACKS_CONFIG, "all");

        kafkaConfig.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                "org.apache.kafka.common.serialization.StringSerializer");
        kafkaConfig.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                "org.apache.kafka.common.serialization.StringSerializer");

        return kafkaConfig;
    }

    public void checkIfKafkaApiCanBeCalled(){
        AWSKafka awsKafkaClient = AWSKafkaClientBuilder.defaultClient();
        ListClustersResult result=awsKafkaClient.listClusters(new ListClustersRequest());
        LogHelper.logger.log(result+"");
    }
    public String kafkaBrokers(String clusterArn, String type) {
        String roleToCallMsk = loadLambdaProperty(LAMBDA_PROPERTY_MSK_ACT_ROLE, null);
        Regions thisRegion = getRegions();
        LogHelper.logger.log(thisRegion + "");
        AWSKafka awsKafkaClient = AWSKafkaClientBuilder.defaultClient();
        if (Optional.ofNullable(roleToCallMsk).isPresent()) {
            AWSSecurityTokenService stsClient = AWSSecurityTokenServiceClientBuilder.standard()
                    .withRegion(thisRegion)
                    .build();
            AssumeRoleRequest roleRequest = new AssumeRoleRequest()
                    .withRoleArn(roleToCallMsk)
                    .withRoleSessionName("msk-cross-act");
            AssumeRoleResult roleResponse = stsClient.assumeRole(roleRequest);
            Credentials sessionCredentials = roleResponse.getCredentials();
            BasicSessionCredentials awsCredentials = new BasicSessionCredentials(
                    sessionCredentials.getAccessKeyId(),
                    sessionCredentials.getSecretAccessKey(),
                    sessionCredentials.getSessionToken());
            awsKafkaClient = AWSKafkaClientBuilder.standard()
                    .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                    .withRegion(thisRegion)
                    .build();

        }

        GetBootstrapBrokersRequest request = new GetBootstrapBrokersRequest();
        request.setClusterArn(clusterArn);
        LogHelper.logger.log(request + "");
        String out;
        if ("IAM".equalsIgnoreCase(type)) {
            out = awsKafkaClient.getBootstrapBrokers(request).getBootstrapBrokerStringSaslIam();
        } else if ("SASL".equalsIgnoreCase(type)) {
            out = awsKafkaClient.getBootstrapBrokers(request).getBootstrapBrokerStringSaslScram();
        } else if ("MTLS".equalsIgnoreCase(type)) {
            out = awsKafkaClient.getBootstrapBrokers(request).getBootstrapBrokerStringTls();
        } else {
            out = awsKafkaClient.getBootstrapBrokers(request).getBootstrapBrokerString();
        }
        return out;
    }

    private Regions getRegions() {
        String region = loadLambdaProperty("AWS_REGION", Regions.DEFAULT_REGION.getName());
        Regions thisRegion = Regions.fromName(region);
        return thisRegion;
    }

    AdminClient getAdminClient() throws IOException {
        Properties cloudConfig = loadKafkaProperties();
        LogHelper.logger.log(cloudConfig+"");
        return AdminClient.create(cloudConfig);
    }
    public void createTopic(final String topic,
                            final Properties cloudConfig, int numberOfPartitions, short replicationFactor) {
        final NewTopic newTopic = new NewTopic(topic, numberOfPartitions,replicationFactor);
        try (final AdminClient adminClient = AdminClient.create(cloudConfig)) {
            adminClient.createTopics(Collections.singletonList(newTopic)).all().get();
        } catch (final InterruptedException | ExecutionException e) {
            // Ignore if TopicExistsException, which may be valid if topic exists
            if (!(e.getCause() instanceof TopicExistsException)) {
                throw new RuntimeException(e);
            }
        }
    }

    public Set<String> listTopic() throws  Exception{
        return getAdminClient().listTopics().names().get();
    }

}

