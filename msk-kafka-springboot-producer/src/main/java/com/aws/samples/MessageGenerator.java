package com.aws.samples;

import com.github.javafaker.Faker;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

import java.io.FileReader;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

@Component
public class MessageGenerator {
    public static class MessageContent{
        String key;
        String message;

    }
    Faker faker = new Faker();
    @Value("${template:src/main/resources/message-template.json}")
    String templateFile;
    private static long counter=0;

    public MessageContent generateMessage() throws Exception {
        String message= FileCopyUtils.copyToString(new FileReader(templateFile));
        Properties fakeValues=generateFakeProperties();
        for (String key:fakeValues.stringPropertyNames()){
            message=message.replace("${"+key+"}",fakeValues.getProperty(key));
        }
        MessageContent messageContent=new MessageContent();
        messageContent.key=fakeValues.getProperty("counter");
        messageContent.message=message;
        return messageContent;
    }

    private Properties generateFakeProperties(){
        Properties properties = new Properties();
        Double buyPrice = faker.number().randomDouble(2,10,100) ;
        Double sellPrice = faker.number().randomDouble(2,10,100) ;
        String symbol = faker.options().option("AAPL","INFY","AMZN","GOOG","IBM") ;
        String tradeId=faker.numerify("##########") ;
        Date dateTimeRandom=faker.date().past(2, TimeUnit.HOURS);
        long epochMilliseconds = dateTimeRandom.getTime();
        long epochSeconds= epochMilliseconds/1000;

        properties.put("counter", String.valueOf(counter++));
        properties.put("id", faker.idNumber().valid());
        properties.put("name", faker.name().fullName());
        properties.put("email", faker.internet().emailAddress());
        properties.put("phone", faker.phoneNumber().cellPhone());
        properties.put("tradeId",tradeId);
        properties.put("orderId","order"+tradeId);
        properties.put("portfolioId","port"+tradeId);
        properties.put("customerId","cust"+tradeId);
        properties.put("symbol",symbol);
        properties.put("timestamp", String.valueOf(epochSeconds));
        properties.put( "orderTimestamp", String.valueOf(epochSeconds));
        properties.put("description",symbol+" Description of trade");
        properties.put("traderName",symbol+" Trader");
        properties.put("traderFirm",symbol+" Trader Firm");
        properties.put( "buy",faker.bool().bool());
        properties.put( "currentPosition",faker.number().digits(4));
        properties.put("quantity",faker.number().randomDouble(2,10,100));
        properties.put("price",faker.number().randomDouble(2,10,100));
        properties.put( "buyPrice",buyPrice) ;
        properties.put( "sellPrice",sellPrice);
        properties.put( "profit",sellPrice-buyPrice);
        return properties;
    }

}
