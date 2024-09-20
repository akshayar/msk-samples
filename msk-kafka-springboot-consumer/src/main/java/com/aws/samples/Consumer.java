package com.aws.samples;

import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.*;

public abstract class Consumer {
    private static final Logger logger = LoggerFactory.getLogger(Consumer.class);

    protected abstract Environment env();

    protected KafkaConsumer<String, String> kafkaConsumer;

    protected Map<String, Object> senderProps() throws IOException {
        final Properties cfg = new Properties();
        String configFile=env().getProperty("spring.kafka.propertiesFilePath");
        Optional.ofNullable(configFile).ifPresent(file-> {
            try {
                cfg.load(new FileInputStream(file));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        Map<String, Object> props = new HashMap<>();
        cfg.stringPropertyNames().forEach(s-> props.put(s,cfg.getProperty(s)));
        return props;
    }
    protected abstract Collection<String> getTopics();

    public void consumeAndPrintMessages() throws Exception {
        if (kafkaConsumer == null) {
            kafkaConsumer = new KafkaConsumer<>(senderProps());
        }
        kafkaConsumer.subscribe(getTopics());
        kafkaConsumer.poll(Duration.ofSeconds(5)).forEach(con-> {
            logger.info("Topic {}, Partition {}, offset {}, message {}",con.topic(),con.partition(),con.offset(),con.value());
        });;
    }

}
