#!/bin/bash
echo "Compiling and running the program"
mvn -q clean compile exec:java -Dexec.mainClass="org.example.Main" -Dexec.args="source_topic_offset.txt destination_topic_offset.txt source_offset_sync.txt"