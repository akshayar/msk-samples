package com.aws.sample.main;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;

public class PublisherMain{

  
    public String handleRequest(Context context)   {
        try {
            LogHelper.logger=context.getLogger();
            new KafkaPublisherJsonData().publish();
            return "SUCCESS";
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }


	/*
	 * public static void main(String[] args) throws Exception {
	 * 
	 * PublisherMain main=new PublisherMain(); main.handleRequest(); }
	 */
}
