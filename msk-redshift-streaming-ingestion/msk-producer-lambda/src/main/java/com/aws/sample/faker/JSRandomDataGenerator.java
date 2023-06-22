package com.aws.sample.faker;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import com.aws.sample.main.LogHelper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.util.IOUtils;
import com.google.gson.Gson;
public class JSRandomDataGenerator  {
	  // create a script engine manager
    ScriptEngineManager factory = new ScriptEngineManager();
    // create a Nashorn script engine
    ScriptEngine engine = factory.getEngineByName("nashorn");
    static String FILE_PATH = "generate-data.js";


    final AmazonS3 s3 = AmazonS3ClientBuilder.standard().withRegion(Regions.DEFAULT_REGION).build();


    Gson gson=new Gson();

    public Properties evaluateFromJS(String bucket, String filePath) {
        try {
        	String templateContent=readFile(bucket,filePath);
            LogHelper.logger.log("Temlate Content"+templateContent);
            engine.eval(templateContent);
            Object javaVar = engine.get("outValue");
            LogHelper.logger.log("Content after evaluation"+javaVar);
            return (Properties) javaVar;
        } catch (final Exception se) {
            LogHelper.logger.log("Exception"+se);
        }
        return new Properties();
    }

    private String readFile(String bucket, String filePath) throws IOException {
        String path=Optional.ofNullable(filePath).orElse(FILE_PATH);
        InputStream defaultTemplate=getClass().getClassLoader().getResourceAsStream(FILE_PATH);
        LogHelper.logger.log(defaultTemplate+"");
        //Optional.ofNullable(file).map
        InputStream in=Optional.ofNullable(bucket).filter(s->!StringUtils.isEmpty(s)).
                map(b->(InputStream)s3.getObject(bucket, filePath).getObjectContent()).
                orElse(defaultTemplate);
        ByteArrayOutputStream stream=new ByteArrayOutputStream();
        IOUtils.copy(Objects.requireNonNull(in),stream);
        return stream.toString();
    }

    public String createPayload(String bucket, String filePath) {
        Properties data=evaluateFromJS(bucket,filePath);
        return gson.toJson(data);
    }


    public Properties createPayloadObject(String bucket, String filePath) {
        return  evaluateFromJS(bucket, filePath);
    }
}
