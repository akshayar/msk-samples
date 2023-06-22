package com.aws.sample.main;

import java.io.IOException;
import java.util.Properties;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import com.aws.sample.faker.JSRandomDataGenerator;


public class KafkaPublisherJsonData {

    int intervalMs=100;

    private JSRandomDataGenerator fakeRandomGenerator;
    private int maxMessages=100;
    String topicName;
    private KafkaConfigLoader kafkaConfigLoader=new KafkaConfigLoader();



    private Producer<String,String> createProducer() throws IOException {
        Properties kafkaConfig=kafkaConfigLoader.loadKafkaProperties();

        Producer<String,String> producer = new KafkaProducer<>(kafkaConfig);

        topicName=kafkaConfigLoader.loadLambdaProperty(KafkaConfigLoader.LAMBDA_PROPERTY_TOPIC,"testtopic");
        String numberOfPartitions=kafkaConfigLoader.loadLambdaProperty(KafkaConfigLoader.LAMBDA_PROPERTY_PARTITIONS,"3");
        String replicationFactor=kafkaConfigLoader.loadLambdaProperty(KafkaConfigLoader.LAMBDA_PROPERTY_REPLICATION_FACTOR,"3");
        kafkaConfigLoader.createTopic(topicName,kafkaConfig,Integer.parseInt(numberOfPartitions),Short.parseShort(replicationFactor));

        return producer;
    }



    void publish() throws Exception {
        Producer<String,String> producer = createProducer();
        fakeRandomGenerator=new JSRandomDataGenerator();
        publishData(producer);
        producer.flush();
        LogHelper.logger.log("Shutting down");
        producer.close();
    }




    private void publishData(Producer<String,String> producer) {

        int i = 0;
        while (maxMessages == -1 || maxMessages > i) {
            sleep(intervalMs);
            publishJson(producer,i);
            i++;
        }
    }


    private void publishJson(Producer<String,String> producer, int i) {
        String bucket=kafkaConfigLoader.loadLambdaProperty(KafkaConfigLoader.LAMBDA_PROPERTY_TEMPLATE_BUCKET,null);
        String filePath=kafkaConfigLoader.loadLambdaProperty(KafkaConfigLoader.LAMBDA_PROPERTY_TEMPLATE_PATH,null);
        String tradeData= fakeRandomGenerator.createPayload(bucket,filePath);
        LogHelper.logger.log("Kafka JSON Push TradeData: " + tradeData);
        producer.send(new ProducerRecord<>(topicName, tradeData), (m, e) -> {
            if (e != null) {
                e.printStackTrace();
            } else {
                System.out.printf("Produced record to topic %s partition [%d] @ offset %d%n", m.topic(), m.partition(), m.offset());
            }
        });
    }

    private void sleep(long intervalMs) {
        try {
            Thread.sleep(intervalMs);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }



}
