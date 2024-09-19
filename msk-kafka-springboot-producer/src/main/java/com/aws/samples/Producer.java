package com.aws.samples;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public abstract class Producer {
    private final Logger logger = LoggerFactory.getLogger(Producer.class);

    public abstract void generateAndSendMessage() throws Exception;

    protected abstract Environment env();

    protected Map<String, Object> senderProps() throws IOException {
        final Properties cfg = new Properties();
        String configFile=env().getProperty("spring.kafka.propertiesFilePath");
        try (InputStream inputStream = new FileInputStream(configFile)) {
            cfg.load(inputStream);
        }
        Map<String, Object> props = new HashMap<>();
        cfg.stringPropertyNames().forEach(s-> props.put(s,cfg.getProperty(s)));
        return props;
    }

}
