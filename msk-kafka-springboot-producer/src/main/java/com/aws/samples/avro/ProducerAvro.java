package com.aws.samples.avro;

import com.amazonaws.services.schemaregistry.utils.AWSSchemaRegistryConstants;
import com.aws.samples.Producer;
import org.apache.avro.generic.GenericRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.FailureCallback;
import org.springframework.util.concurrent.SuccessCallback;
import software.amazon.awssdk.services.glue.model.DataFormat;

import java.util.Map;

@Component
public class ProducerAvro extends Producer
{
    private final Logger logger = LoggerFactory.getLogger(ProducerAvro.class);

    @Autowired
    MessageGeneratorAvro messageGenerator=new MessageGeneratorAvro();
    @Value("${spring.kafka.avro.mainTopic}")
    private String topic;
    @Value("${spring.kafka.avro.fallbackTopic}")
    private String fallbackTopic;
    @Autowired
    private Environment env;

    protected KafkaTemplate<String, GenericRecord> template;
    protected KafkaTemplate<String, GenericRecord> template2;


    @Override
    protected Environment env(){
        return  env;
    }
    @Override
    protected Map<String, Object> senderProps() {
        Map<String, Object> props=super.senderProps();
        props.put(AWSSchemaRegistryConstants.SCHEMA_NAME, env().getProperty("spring.kafka.avro.schemaName"));
        props.put(AWSSchemaRegistryConstants.DATA_FORMAT, DataFormat.AVRO);
        return  props;
    }
    @Override
    public void generateAndSendMessage() throws Exception {
        MessageGeneratorAvro.MessageContent messageContent=messageGenerator.generateMessage();
        sendMessageToMain(messageContent.genericRecord, messageContent.key);
    }


    private void sendMessageToMain(GenericRecord message, String messageKey) {
        logger.info("Message content {}",message);
        if (template == null) {
            template = new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(senderProps()));
        }
        template.send(topic, messageKey, message)
                .completable()
                .whenComplete((result, error) -> {
                    if (error == null) {
                        logger.info("Message no {} sent to topic {} , partition {} successfully", messageKey,result.getRecordMetadata().topic(),result.getRecordMetadata().partition());
                    }else {
                        logger.info("Failed to send message key :{} , content:{}", messageKey, message);
                        logger.info("Error occurred while sending message ",error);
                        logger.info("Sending message to fallback topic");
                        sendMessageToFallbackTopic(message, messageKey);
                    }
                });
    }

    private void sendMessageToFallbackTopic(GenericRecord message, String messageKey) {
        if(template2==null){
            template2=new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(senderProps()));
        }
        template2.send(fallbackTopic, messageKey, message)
                .addCallback(new SuccessCallback<>() {
                                 @Override
                                 public void onSuccess(SendResult<String, GenericRecord> stringStringSendResult) {
                                     logger.info("Message no {} sent to topic {} , partition {} successfully", messageKey, stringStringSendResult.getRecordMetadata().topic(), stringStringSendResult.getRecordMetadata().partition());
                                 }
                             },
                        new FailureCallback() {
                            @Override
                            public void onFailure(Throwable throwable) {
                                logger.info("Failed to send message key :{} , content:{}", messageKey, message);
                                logger.info("Error occurred while sending message ",throwable);
                            }
                        });
    }
}
