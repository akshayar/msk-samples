## Delete Connectors
```shell
curl -X DELETE http://localhost:8083/connectors/mm2-msc  
curl -X DELETE http://localhost:8083/connectors/mm2-cpc  
curl -X DELETE http://localhost:8083/connectors/mm2-hbc  
```
## Create Connectors
```shell
cd mirror-maker/connectors/no-auth/
curl -X PUT -H "Content-Type: application/json" --data @mm2-msc-cust-repl-policy.json http://localhost:8083/connectors/mm2-msc/config 
curl -s localhost:8083/connectors/mm2-msc/status 

curl -X PUT -H "Content-Type: application/json" --data @mm2-cpc-cust-repl-policy.json http://localhost:8083/connectors/mm2-cpc/config  
curl -s localhost:8083/connectors/mm2-cpc/status 

curl -X PUT -H "Content-Type: application/json" --data @mm2-hbc-no-auth.json http://localhost:8083/connectors/mm2-hbc/config 
curl -s localhost:8083/connectors/mm2-hbc/status 
```