#!/bin/bash
echo "Compiling and running the program"
mvn clean compile exec:java -Dexec.mainClass="org.example.Main" -Dexec.args="source_offset.txt destination_offset.txt offset_sync.txt"