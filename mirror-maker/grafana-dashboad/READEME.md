## Setup Prometheus
Refer https://catalog.us-east-1.prod.workshops.aws/workshops/c2b72b6f-666b-4596-b8bc-bafa5dcca741/en-US/openmonitoring/installwithdocker
```shell
mkdir ~/prometheus
touch ~/prometheus/prometheus.yml
touch ~/prometheus/targets.json
```
Sample Prometheus
```shell
# file: prometheus.yml
# my global config
global:
  scrape_interval:     10s

# A scrape configuration containing exactly one endpoint to scrape:
# Here it's Prometheus itself.
scrape_configs:
# The job name is added as a label `job=<job_name>` to any timeseries scraped from this config.
- job_name: 'prometheus'
  static_configs:
  # 9090 is the prometheus server port
  - targets: ['localhost:9090']
- job_name: 'broker'
  file_sd_configs:
  - files:
    - 'targets.json'

```
Sample targets.json
```shell
[
  {
    "labels": {
      "job": "jmx"
    },
    "targets": [
      "broker_dns_1:11001",
      "broker_dns_2:11001",
      "broker_dns_N:11001"
    ]
  },
  {
    "labels": {
      "job": "node"
    },
    "targets": [
      "broker_dns_1:11002",
      "broker_dns_2:11002",
      "broker_dns_N:11002"
    ]
  }
]

```
## Setup Grafana
Refer https://catalog.us-east-1.prod.workshops.aws/workshops/c2b72b6f-666b-4596-b8bc-bafa5dcca741/en-US/openmonitoring/rungrafana
```shell
docker run -d -p 3000:3000 --name=grafana -e "GF_INSTALL_PLUGINS=grafana-clock-panel" grafana/grafana

```
## Dashboards to import
1. s3://aws-streaming-artifacts/msk-lab-resources/MM2-dashboard-1.json
2. https://grafana.com/grafana/dashboards/16808-aws-kafka-cluster/
3. https://github.com/aws-samples/amazonmsk-managed-observability/blob/main/AWS-MSK_KafkaCluster-Metrics-Dashboard.json
4. https://github.com/aws-samples/amazonmsk-managed-observability/blob/main/AWS-MSK_KafkaCluster-Overview-Dashboard.json