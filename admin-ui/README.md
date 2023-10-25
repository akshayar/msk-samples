## Deploy AKHQ on EC2/Cloud9
1. Update `admin-ui/application.yml` with cluster configuration. You can configure multiple clusters. 
2. Update SG of clusters to allow ingress from AKHQ EC2. 
3. Update SG of AKHQ EC2 to allow ingress on port 8080 from the source desktop. 
4. Run following commands to run AKHQ -
```shell
docker run     -p 8080:8080 \
  -v <local EC2 path to application.yml>:/app/application.yml  \
  tchiotludo/akhq   
```
## Deploy AKHQ on ECS
### Pre-requisite
1. Create an ECS cluster with Fargate as the "Infrastructure" option. 
### Deployment Steps
1. The deployment assumes that you have two clusters to administer, both allow plaintext auth. Both source and destination clusters are on AWS. They can either be MSK or self-hosted EC2.
2. The ECS task will have 3 SGs assigned - cluster SGs and an SG that gets created by the CFT. 
3. Clusters SGs have self referencing rules on required port. 
4. Update parameters in `admin-ui/deploy.sh`. If you have just one cluster to monitor make SOURCE_KAFKA_CLUSTER_BOOTSTRAP and DESTINATION_KAFKA_CLUSTER_BOOTSTRAP same. 
5. Run following command to deploy AKHQ using ECS service  -
```shell
cd $SOURCE_ROOT/admin-ui
./deploy.sh 
```
6. Once deployed, refer the output of the stack `akhq-ecs-deploy` and follow AKHQUrl to access the UI. 