package com.aws.sample.faker;

import java.util.Date;

import com.github.javafaker.Faker;

public class FakerHelper {
    public static Faker faker=new Faker();

    public static long getEpochInSeconds(Date date){
        return date.getTime()/1000;
    }
}
