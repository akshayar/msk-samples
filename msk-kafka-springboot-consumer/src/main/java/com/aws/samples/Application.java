package com.aws.samples;

import com.aws.samples.avro.ConsumerAvro;
import com.aws.samples.json.ConsumerJson;
import com.aws.samples.json.ConsumerJsonWithSchema;
import com.aws.samples.protobuff.ConsumerProtobuf;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Hello world!
 *
 */
@SpringBootApplication
public class Application implements ApplicationContextAware {
    private static final Logger logger = LoggerFactory.getLogger(Application.class);

    @Autowired
    private Environment env;

    Consumer consumer;
    private ApplicationContext applicationContext;

    public static void main(String[] args) throws  Exception{
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public ApplicationRunner runner() {

        return args -> {
            // create timer task to send message
            logger.info("Starting timer task");
            java.util.Timer timer=new java.util.Timer();

            String messageFormat=env.getProperty("spring.kafka.messageFormat");
            logger.info(messageFormat);
            consume(messageFormat,timer);

        };
    }

    private void consume(String messageFormat, Timer timer){
        if("avro".equalsIgnoreCase(messageFormat)){
            consumer=applicationContext.getBean(ConsumerAvro.class);
        } else if ("json".equalsIgnoreCase(messageFormat)) {
            consumer=applicationContext.getBean(ConsumerJson.class);
        }else if("JSON_WITH_SCHEMA".equalsIgnoreCase(messageFormat)){
            consumer=applicationContext.getBean(ConsumerJsonWithSchema.class);
        }else if("PROTOBUF".equalsIgnoreCase(messageFormat)){
            consumer=applicationContext.getBean(ConsumerProtobuf.class);
        }

        TimerTask task=new TimerTask() {
            @Override
            public void run() {
                try {
                    consumer.consumeAndPrintMessages();
                }catch (Exception e){
                    logger.info("Exception",e);
                    throw new RuntimeException(e);
                }

            }
        };
        timer.schedule(task, Integer.parseInt(Objects.requireNonNull(env.getProperty("timer.initialDelay"))), Integer.parseInt(Objects.requireNonNull(env.getProperty("timer.interval"))));
    }


    @Override
    public void setApplicationContext(@NotNull ApplicationContext applicationContext) throws BeansException {
        this.applicationContext=applicationContext;
    }
}
