package com.aws.samples.avro;

import com.amazonaws.services.schemaregistry.utils.AWSSchemaRegistryConstants;
import com.aws.samples.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.glue.model.DataFormat;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

@Component
public class ConsumerAvro extends Consumer
{
    private static final Logger logger = LoggerFactory.getLogger(ConsumerAvro.class);

    @Value("${spring.kafka.avro.mainTopic}")
    private String topic;
    @Value("${spring.kafka.avro.fallbackTopic}")
    private String fallbackTopic;
    @Autowired
    private Environment env;

    @Override
    protected Environment env(){
        return  env;
    }
    @Override
    protected Map<String, Object> senderProps() throws IOException {
        Map<String, Object> props=super.senderProps();
        props.put(AWSSchemaRegistryConstants.SCHEMA_NAME, env().getProperty("spring.kafka.avro.schemaName"));
        props.put(AWSSchemaRegistryConstants.DATA_FORMAT, DataFormat.AVRO);
        return  props;
    }

    @Override
    protected Collection<String> getTopics() {
        return Arrays.asList(topic,fallbackTopic);
    }
}
