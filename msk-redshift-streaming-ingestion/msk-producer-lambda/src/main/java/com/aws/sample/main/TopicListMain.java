package com.aws.sample.main;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.google.gson.Gson;

public class TopicListMain  {
    private Gson gson=new Gson();
    KafkaConfigLoader kafkaConfigLoader=new KafkaConfigLoader();
    public String handleRequest(Context context) {
        try {
            LambdaLogger logger=context.getLogger();
            LogHelper.logger=logger;
            logger.log("Starting topic list ,  context="+context);
            kafkaConfigLoader=new KafkaConfigLoader();
            kafkaConfigLoader.checkIfKafkaApiCanBeCalled();
            Set<String> topics= kafkaConfigLoader.listTopic();
            logger.log("Topic list"+topics);
            return Optional.ofNullable(topics).orElse(Collections.emptySet())+"";
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }
	/*
	 * public static void main(String[] args) throws Exception {
	 * System.setProperty("BOOTSTRAP_SERVER","localhost:9092");
	 * System.setProperty("REPLICATION_FACTOR","1"); TopicListMain main=new
	 * TopicListMain(); main.handleRequest(); }
	 */
}
