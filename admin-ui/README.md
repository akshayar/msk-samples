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
1. The deployment assumes that both source and destination clusters are on AWS. They can either be MSK or self-hosted EC2. 
2. The ECS task will have 3 SGs assigned - cluster SGs and an SG that gets created by the CFT. 
3. Clusters SGs have self referencing rules on required port. 
4. Update parameters in `admin-ui/deploy.sh`.
5. Run following comand to deploy AKHQ using ECS service  -
```shell
cd $SOURCE_ROOT/admin-ui
./deploy.sh 
```
6. Once deployed, refer the output of the stack and follow AKHQUrl to access the UI. 