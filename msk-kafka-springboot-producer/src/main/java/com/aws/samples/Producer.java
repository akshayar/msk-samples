package com.aws.samples;

import com.amazonaws.services.schemaregistry.serializers.GlueSchemaRegistryKafkaSerializer;
import com.amazonaws.services.schemaregistry.utils.AWSSchemaRegistryConstants;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;

import java.util.HashMap;
import java.util.Map;

public abstract class Producer {
    private final Logger logger = LoggerFactory.getLogger(Producer.class);

    public abstract void generateAndSendMessage() throws Exception;

    protected abstract Environment env();

    protected Map<String, Object> senderProps() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, env().getProperty("spring.kafka.bootstrapAddress"));
        props.put(ProducerConfig.ACKS_CONFIG, env().getProperty("spring.kafka.acknowledgment"));
        props.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, env().getProperty("spring.kafka.maxBlockTime"));
        props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, env().getProperty("spring.kafka.bufferMemory"));
        props.put(ProducerConfig.SEND_BUFFER_CONFIG, env().getProperty("spring.kafka.sendBufferBytes"));
        props.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, env().getProperty("spring.kafka.maxInFlightReqPerConnInParallel"));
        props.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, env().getProperty("spring.kafka.deliveryTimeoutMs"));
        props.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, env().getProperty("spring.kafka.producerRetryTimeOutMilliSeconds"));

        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, GlueSchemaRegistryKafkaSerializer.class);

        props.put(AWSSchemaRegistryConstants.AWS_REGION, env().getProperty("aws.gsr.region"));
        props.put(AWSSchemaRegistryConstants.DATA_FORMAT, env().getProperty("spring.kafka.messageFormat"));

        props.put(AWSSchemaRegistryConstants.SCHEMA_AUTO_REGISTRATION_SETTING, env().getProperty("aws.gsr.autoRegistration")); // If not passed, uses "false"
        props.put(AWSSchemaRegistryConstants.SCHEMA_NAME, env().getProperty("aws.gsr.schemaName")); // If not passed, uses transport name (topic name in case of Kafka, or stream name in case of Kinesis Data Streams)
        props.put(AWSSchemaRegistryConstants.REGISTRY_NAME, env().getProperty("aws.gsr.registryName")); // If not passed, uses "default-registry"
        props.put(AWSSchemaRegistryConstants.CACHE_TIME_TO_LIVE_MILLIS, env().getProperty("aws.gsr.ttl")); // If not passed, uses 86400000 (24 Hours)
        props.put(AWSSchemaRegistryConstants.CACHE_SIZE, env().getProperty("aws.gsr.cacheSize")); // default value is 200
        props.put(AWSSchemaRegistryConstants.COMPATIBILITY_SETTING, env().getProperty("aws.gsr.compatibility")); // Pass a compatibility mode. If not passed, uses Compatibility.BACKWARD
        props.put(AWSSchemaRegistryConstants.DESCRIPTION, "This registry is used for several purposes."); // If not passed, constructs a description
        props.put(AWSSchemaRegistryConstants.COMPRESSION_TYPE, "ZLIB"); // If not passed, records are sent uncompressed


        return props;
    }

}
