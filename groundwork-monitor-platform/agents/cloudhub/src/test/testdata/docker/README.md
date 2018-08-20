# Docker Development and Testing Usage

This directory contains JSON output from CAdvsior APIs run via CURL 

## Version 2.0 of CAdvisor

October 11, 2017, we added support for CAdvisor 2.0 'Stats' API.
Note we can add a count field to limit the samples retrieved.
The JSON output has changed from v1, so there is special handling in MetricClient2.java for backward compatibility.
Eventually we will want to only support 2.0 JSON format.

### 2.0-stats-engine.json

Generate 2.0 stats sample for the entire Docker Engine in 2.0-stats-engine.json

``
curl http://localhost:9292/api/v2.0/stats/?count=4  | jq '.' > 2.0-stats-engine.json 
``

### 2.0-stats-docker.json 

Generate 2.0 stats sample for Docker Container in 2.0-stats-docker.json for a docker container named 'bash'

``
http://localhost:9292/api/v2.0/stats/bash?count=4&type=docker | jq '.' > 2.0-stats-docker.json 
``

### busy.sh

This script can be used in a bash container to simulate load to test the cpu.usage.total metric
Recommend varying sleep values with 0. 0.05, 0.1, 1, 2

``
docker run -it --rm --name bash bash
``

Then paste in and run this busy.sh script  (chmod +x)

To see usage 'docker stats' or run CloudHub

