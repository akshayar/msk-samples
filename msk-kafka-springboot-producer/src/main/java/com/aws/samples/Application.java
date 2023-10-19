package com.aws.samples;

import org.apache.kafka.clients.admin.NewTopic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.io.FileReader;
import java.util.TimerTask;

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
    private final Logger logger = LoggerFactory.getLogger(Application.class);
    private static String topicData;
    public static void main(String[] args) throws  Exception{
        SpringApplication.run(Application.class, args);
        //read data from msk-kafka-springboot-producer/src/main/resources/sample.json file and convert to json
        topicData=FileCopyUtils.copyToString(new FileReader(new File("src/main/resources/sample.json")));

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
    public ApplicationRunner runner(KafkaTemplate<String, String> template) {

        return args -> {
            // create timer task to send message
            logger.info("Starting timer task");
            java.util.Timer timer=new java.util.Timer();

            TimerTask task=new TimerTask() {
                private int counter=1;
                @Override
                public void run() {
                    template.send(topic, topicData.replaceAll("COUNT",""+counter++));
                }
            };
            timer.schedule(task, 1000, 1000);
        };
    }
}
