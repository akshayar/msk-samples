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
1. The deployment works with both IAM and plaintext auth. You can either deploy UI for two clusters on single deployment and for single cluster on a deployment. They can either be MSK or self-hosted EC2.
2. The ECS task will have SGs of cluster attached along with another SG which get created by the CFT. 
3. Clusters SGs have self referencing rules on required port. 
4. Update parameters in `admin-ui/deploy.sh`.  
5. Run following command to deploy AKHQ using ECS service  -
```shell
cd $SOURCE_ROOT/admin-ui
./deploy.sh 
```
6. Once deployed, refer the output of the stack `akhq-ecs-deploy` and follow AKHQUrl to access the UI. 