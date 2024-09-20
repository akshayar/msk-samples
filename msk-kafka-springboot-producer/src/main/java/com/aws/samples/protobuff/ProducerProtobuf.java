package com.aws.samples.protobuff;

import com.amazonaws.services.schemaregistry.utils.AWSSchemaRegistryConstants;
import com.aws.samples.Producer;
import com.google.protobuf.DynamicMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.glue.model.DataFormat;

import java.io.IOException;
import java.util.Map;

@Component
public class ProducerProtobuf extends Producer {
    private static final Logger logger = LoggerFactory.getLogger(ProducerProtobuf.class);
    @Autowired
    MessageGeneratorProtobuf messageGenerator = new MessageGeneratorProtobuf();
    @Value("${spring.kafka.protobuf.mainTopic}")
    protected String topic;
    @Value("${spring.kafka.protobuf.fallbackTopic}")
    protected String fallbackTopic;
    @Autowired
    private Environment env;

    protected KafkaTemplate<String, DynamicMessage> template;
    protected KafkaTemplate<String, DynamicMessage> template2;

    @Override
    protected Environment env(){
        return  env;
    }
    @Override
    protected Map<String, Object> senderProps() throws IOException {
        Map<String, Object> props=super.senderProps();
        props.put(AWSSchemaRegistryConstants.SCHEMA_NAME, env().getProperty("spring.kafka.protobuf.schemaName"));
        props.put(AWSSchemaRegistryConstants.DATA_FORMAT, DataFormat.PROTOBUF);
        return  props;
    }

    @Override
    public void generateAndSendMessage() throws Exception {
        MessageGeneratorProtobuf.MessageContent messageContent = messageGenerator.generateMessage();
        sendMessageToMain(messageContent.message, messageContent.key);
    }

    protected void sendMessageToMain(DynamicMessage message, String messageKey) throws IOException {
        logger.info("Message content {}", message);
        if (template == null) {
            template = new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(senderProps()));
        }
        template.send(topic, messageKey, message)
                .completable()
                .whenComplete((result, error) -> {
                    if (error == null) {
                        logger.info("Message no {} sent to topic {} , partition {} successfully", messageKey, result.getRecordMetadata().topic(), result.getRecordMetadata().partition());
                    } else {
                        logger.info("Failed to send message key :{} , content:{}", messageKey, message);
                        logger.info("Error occurred while sending message ", error);
                        logger.info("Sending message to fallback topic");
                        sendMessageToFallbackTopic(message, messageKey);
                    }
                });
    }

    protected void sendMessageToFallbackTopic(DynamicMessage message, String messageKey) {
        try{
            if (template2 == null) {
                template2 = new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(senderProps()));
            }
            template2.send(fallbackTopic, messageKey, message)
                    .addCallback(stringStringSendResult -> logger.info("Message no {} sent to topic {} , partition {} successfully", messageKey, stringStringSendResult.getRecordMetadata().topic(), stringStringSendResult.getRecordMetadata().partition()),
                            throwable -> {
                                logger.info("Failed to send message key :{} , content:{}", messageKey, message);
                                logger.info("Error occurred while sending message ", throwable);
                            });
        }catch (Exception e){
            logger.error("Error",e);
        }

    }

}
