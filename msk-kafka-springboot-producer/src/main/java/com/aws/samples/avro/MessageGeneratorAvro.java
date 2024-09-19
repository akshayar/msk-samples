package com.aws.samples.avro;

import com.github.javafaker.Faker;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

@Component("avro")
public class MessageGeneratorAvro {
    private final Logger logger = LoggerFactory.getLogger(MessageGeneratorAvro.class);

    public static class MessageContent{
        public String key;
        public GenericRecord genericRecord;

    }
    Faker faker = new Faker();
    @Value("${template:src/main/resources/message-template.json}")
    String templateFile;
    @Value("${spring.kafka.avro.schemaFile:src/main/resources/Trade.avsc}")
    String avroSchemaPath;
    Schema avroSchema ;
    private static long counter=0;

    private Schema getAvroSchema(){
        if(avroSchema==null){
            try {
                Schema.Parser parser= new Schema.Parser();
                avroSchema = parser.parse(new File(avroSchemaPath));
                return avroSchema;
            } catch (IOException e) {
                logger.error("Error in reading schema",e);
                throw new RuntimeException(e);
            }
        }
        return avroSchema;
    }

    public MessageContent generateMessage() throws Exception {
        String message= FileCopyUtils.copyToString(new FileReader(templateFile));
        Properties fakeValues=generateFakeProperties();
        for (String key:fakeValues.stringPropertyNames()){
            message=message.replace("${"+key+"}",fakeValues.getProperty(key));
        }

        Map<String,Object> map= JsonParserFactory.getJsonParser().parseMap(message);

        GenericRecord musical = new GenericData.Record(getAvroSchema());
        map.keySet().forEach(s-> musical.put(s,map.get(s)));

        MessageContent messageContent=new MessageContent();
        messageContent.key=fakeValues.getProperty("counter");
        messageContent.genericRecord=musical;
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
