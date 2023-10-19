package com.aws.samples;

import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
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
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;

/**
 * Hello world!
 *
 */
@SpringBootApplication
public class Application
{
    @Value("${spring.kafka.releaseTopic}")
    private String topic;
    @Value("${spring.kafka.rf}")
    private int replicationFactor;
    @Value("${spring.kafka.partitions}")
    private int partitionCount;
    @Value("${spring.kafka.minIsr}")
    private String minIsr;

    @Autowired
    private Environment env;
    @Autowired
    private ProducerFactory producerFactory;
    @Autowired
    KafkaTemplate<String, String> template;
    private final Logger logger = LoggerFactory.getLogger(Application.class);
    private static String topicData;
    public static void main(String[] args) throws  Exception{
        SpringApplication.run(Application.class, args);
        //read data from msk-kafka-springboot-producer/src/main/resources/sample.json file and convert to json
        topicData=FileCopyUtils.copyToString(new FileReader("src/main/resources/sample.json"));

    }



    @Bean
    public NewTopic topic() {
        return TopicBuilder.name(topic)
                .partitions(partitionCount)
                .replicas(replicationFactor)
                .config("min.insync.replicas", minIsr)
                .build();
    }

    @Bean
    public ProducerFactory<String, String> producerFactory() {
        return new DefaultKafkaProducerFactory<>(senderProps());
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    @Bean
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, env.getProperty("spring.kafka.bootstrapAddress"));
        return new KafkaAdmin(configs);
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
                    template.send(topic, messageKey,message)
                            .completable()
                            .whenComplete((result, error) -> {
                                if (error == null) {
                                    logger.info("Message no {} sent to topic {} , partition {} successfully",messageKey,result.getRecordMetadata().topic(),result.getRecordMetadata().partition());
                                }else {
                                    logger.info("Failed to send message key :{} , content:{}",messageKey,message);
                                    logger.info("Error occurred while sending message ",error);
                                }
                            });
                }
            };
            timer.schedule(task, 1000, 1000);
        };
    }
}
