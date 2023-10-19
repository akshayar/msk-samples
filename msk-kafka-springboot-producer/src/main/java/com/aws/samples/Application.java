package com.aws.samples;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.concurrent.FailureCallback;
import org.springframework.util.concurrent.SuccessCallback;

import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;
import java.util.TimerTask;

/**
 * Hello world!
 *
 */
@SpringBootApplication
public class Application
{
    private final Logger logger = LoggerFactory.getLogger(Application.class);

    @Value("${spring.kafka.releaseTopic}")
    private String topic;
    @Value("${spring.kafka.fallbackTopic}")
    private String fallbackTopic;
    @Value("${spring.kafka.rf}")
    private int replicationFactor;
    @Value("${spring.kafka.partitions}")
    private int partitionCount;
    @Value("${spring.kafka.minIsr}")
    private String minIsr;
    @Autowired
    private Environment env;
    @Autowired
    KafkaTemplate<String, String> template;

    KafkaTemplate<String,String> template2;


    private static String topicData;
    public static void main(String[] args) throws  Exception{
        SpringApplication.run(Application.class, args);
        //read data from msk-kafka-springboot-producer/src/main/resources/sample.json file and convert to json
        topicData=FileCopyUtils.copyToString(new FileReader("src/main/resources/sample.json"));

    }
    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        return new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(senderProps()));
    }

    private Map<String, Object> senderProps() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, env.getProperty("spring.kafka.bootstrapAddress"));
        props.put(ProducerConfig.ACKS_CONFIG, env.getProperty("spring.kafka.acknowledgment"));
        props.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, env.getProperty("spring.kafka.maxBlockTime"));
        props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, env.getProperty("spring.kafka.bufferMemory"));
        props.put(ProducerConfig.SEND_BUFFER_CONFIG, env.getProperty("spring.kafka.sendBufferBytes"));
        props.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, env.getProperty("spring.kafka.maxInFlightReqPerConnInParallel"));
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, env.getProperty("spring.kafka.deliveryTimeoutMs"));
        props.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, env.getProperty("spring.kafka.producerRetryTimeOutMilliSeconds"));
        return props;
    }
    @Bean
    public ApplicationRunner runner() {

        return args -> {
            // create timer task to send message
            logger.info("Starting timer task");
            java.util.Timer timer=new java.util.Timer();

            TimerTask task=new TimerTask() {
                private int counter=1;
                @Override
                public void run() {
                    String message=topicData.replaceAll("COUNT",""+counter++);
                    String messageKey=counter+"";
                    sendMessageToMain(message, messageKey);
                }
            };
            timer.schedule(task, Integer.parseInt(env.getProperty("timer.initialDelay")), Integer.parseInt(env.getProperty("timer.interval")));
        };
    }

    private void sendMessageToMain(String message, String messageKey) {
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

    private void sendMessageToFallbackTopic(String message, String messageKey) {
        template2=new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(senderProps()));
        template2.send(fallbackTopic, messageKey, message)
                .addCallback(new SuccessCallback<SendResult<String, String>>() {
                                 @Override
                                 public void onSuccess(SendResult<String, String> stringStringSendResult) {
                                     logger.info("Message no {} sent to topic {} , partition {} successfully", messageKey,stringStringSendResult.getRecordMetadata().topic(),stringStringSendResult.getRecordMetadata().partition());
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
