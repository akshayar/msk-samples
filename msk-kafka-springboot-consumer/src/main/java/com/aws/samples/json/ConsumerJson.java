package com.aws.samples.json;

import com.amazonaws.services.schemaregistry.utils.AWSSchemaRegistryConstants;
import com.aws.samples.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

@Component
public class ConsumerJson extends Consumer
{
    private static final Logger logger = LoggerFactory.getLogger(ConsumerJson.class);

    @Value("${spring.kafka.json.mainTopic}")
    private String topic;
    @Value("${spring.kafka.json.fallbackTopic}")
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
        props.put(AWSSchemaRegistryConstants.SCHEMA_NAME, env().getProperty("spring.kafka.json.schemaName"));
        return  props;
    }

    @Override
    protected Collection<String> getTopics() {
        return Arrays.asList(topic,fallbackTopic);
    }

}
