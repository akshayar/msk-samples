package org.example;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.FileReader;
import java.util.*;

/**
 *
 */
public class Main {
    public static void main(String[] args) throws Exception{
        Map<String,Long> sourceOffsetMap=new TreeMap<>();
        Map<String,Long> destinationOffsetMap=new TreeMap<>();
        Map<String,Long> offsetDifference=new HashMap<>();

        String topicOffsetSourceFile=args[0];
        String topicOffsetDestinationFile=args[1];
        String offsetSyncTopicFile=args[2];

        Set<String> missingTopicsAtDestination=compareSourceAndDestination("source_topic_list.txt","destination_topic_list.txt");
        System.out.println("Topics not found at destination: "+missingTopicsAtDestination);
        Set<String> missingGroupsAtDestination=compareSourceAndDestination("source_group_list.txt","destination_group_list.txt");
        System.out.println("Groups not found at destination: "+missingGroupsAtDestination);

        IOUtils.readLines(new FileReader(topicOffsetSourceFile)).stream().forEach(str->{
            String[] arr=str.split(":");
            sourceOffsetMap.put(arr[0]+"-"+arr[1],Long.parseLong(arr[2]));
        });

        IOUtils.readLines(new FileReader(topicOffsetDestinationFile)).stream().forEach(str->{
            String[] arr=str.split(":");
            destinationOffsetMap.put(arr[0]+"-"+arr[1],Long.parseLong(arr[2]));
        });
        System.out.println(sourceOffsetMap);
        System.out.println(destinationOffsetMap);
        IOUtils.readLines(new FileReader(offsetSyncTopicFile)).stream().forEach(str->{
            String output = getStringBetweenTwoChars(str, "{", "}");
            String arr[]=output.split(",");
            String topicPartition=arr[0].split("=")[1];
            String sourceOffset=arr[1].split("=")[1];
            String destinationOffset=arr[2].split("=")[1];
            offsetDifference.put(topicPartition,Long.parseLong(destinationOffset)-Long.parseLong(sourceOffset));
        });
        System.out.println(offsetDifference);
        sourceOffsetMap.keySet().stream().forEach(k->{
            Long sourceOffset=sourceOffsetMap.get(k);
            Long destinationOffset= Optional.ofNullable(destinationOffsetMap.get(k)).orElse(0l);
            Long offsetDiff=Optional.ofNullable(offsetDifference.get(k)).orElse(0l);
            Long offsetLag=destinationOffset-(sourceOffset+offsetDiff);
            System.out.println("Offset lag for "+k+" is "+offsetLag);
        });

    }

    private static Set<String> compareSourceAndDestination(String sourceFile, String destinationFiles) {
        Set<String> source=new HashSet<>();
        Set<String> destination=new HashSet<>();
        try{
            IOUtils.readLines(new FileReader(sourceFile)).stream().forEach(str->{
                String[] arr=str.split(" ");
                source.addAll(Arrays.asList(arr));
            });
            IOUtils.readLines(new FileReader(destinationFiles)).stream().forEach(str->{
                String[] arr=str.split(" ");
                destination.addAll(Arrays.asList(arr));
            });
        }catch (Exception e){
            e.printStackTrace();
        }
        source.removeAll(destination);
        return source;
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