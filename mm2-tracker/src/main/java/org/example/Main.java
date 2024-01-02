package org.example;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.FileReader;
import java.util.*;

// Press Shift twice to open the Search Everywhere dialog and type `show whitespaces`,
// then press Enter. You can now see whitespace characters in your code.
public class Main {
    public static void main(String[] args) throws Exception{
        Map<String,Integer> sourceOffsetMap=new TreeMap<>();
        Map<String,Integer> destinationOffsetMap=new TreeMap<>();
        Map<String,Integer> offsetDifference=new HashMap<>();

        System.out.printf("Hello and welcome!");
        String topicOffsetSourceFile=args[0];
        String topicOffsetDestinationFile=args[1];
        String offsetSyncTopicFile=args[2];

        IOUtils.readLines(new FileReader(topicOffsetSourceFile)).stream().forEach(str->{
            String[] arr=str.split(":");
            sourceOffsetMap.put(arr[0]+"-"+arr[1],Integer.parseInt(arr[2]));
        });

        IOUtils.readLines(new FileReader(topicOffsetDestinationFile)).stream().forEach(str->{
            String[] arr=str.split(":");
            destinationOffsetMap.put(arr[0]+"-"+arr[1],Integer.parseInt(arr[2]));
        });
        System.out.println(sourceOffsetMap);
        System.out.println(destinationOffsetMap);
        IOUtils.readLines(new FileReader(offsetSyncTopicFile)).stream().forEach(str->{
            String output = getStringBetweenTwoChars(str, "{", "}");
            String arr[]=output.split(",");
            String topicPartition=arr[0].split("=")[1];
            String sourceOffset=arr[1].split("=")[1];
            String destinationOffset=arr[2].split("=")[1];
            offsetDifference.put(topicPartition,Integer.parseInt(destinationOffset)-Integer.parseInt(sourceOffset));
        });
        System.out.println(offsetDifference);
        sourceOffsetMap.keySet().stream().forEach(k->{
            Integer sourceOffset=sourceOffsetMap.get(k);
            Integer destinationOffset= Optional.ofNullable(destinationOffsetMap.get(k)).orElse(0);
            Integer offsetDiff=Optional.ofNullable(offsetDifference.get(k)).orElse(0);
            Integer offsetLag=destinationOffset-(sourceOffset+offsetDiff);
            System.out.println("Offset lag for "+k+" is "+offsetLag);
        });


    }

    public static String getStringBetweenTwoChars(String input, String startChar, String endChar) {
        try {
            int start = input.indexOf(startChar);
            if (start != -1) {
                int end = input.indexOf(endChar, start + startChar.length());
                if (end != -1) {
                    return input.substring(start + startChar.length(), end);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return input; // return null; || return "" ;
    }

}