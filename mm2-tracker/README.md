## Collect data from clusters
1. Edit `check-mm2-sync.sh` to modify `KAFKA_BIN_PATH`. 
2. Execute command
```shell
./check-mm2-sync.sh <SOURCE cluster bootstrap> <Destination cluster bootstrap> <offset sync topic>
```
```shell
./check-mm2-sync.sh b-2.mskcluster.kha9l7.c4.kafka.ap-south-1.amazonaws.com:9092 b-3.mskdestinationcluster.c0jtgj.c4.kafka.ap-south-1.amazonaws.com:9092 mm2-offset-syncs..internal
```
## Analyze data
1. Execute java program. Ensure that you have Maven installed and Java version is 11.  
```shell
./analyze.sh
```