# Azure Fork 

We initially forked Azure SDK at version 1.9.0, to support multiple queries being submitted per resource. Unfortunately, 
the backend REST API throttles the metric requests per resource to only 5. This performs almost 3x faster than submitting
one HTTP request per metric.

## Files changes

	modified:   azure-mgmt-monitor/src/main/java/com/microsoft/azure/management/monitor/MetricDefinition.java
	modified:   azure-mgmt-monitor/src/main/java/com/microsoft/azure/management/monitor/implementation/MetricDefinitionImpl.java
	modified:   azure-samples/src/main/java/com/microsoft/azure/management/monitor/samples/QueryMetricsAndActivityLogs.java

## Branch

https://github.com/bluesunrise/azure-libraries-for-java/commits/gwos

See: 

https://github.com/bluesunrise/azure-libraries-for-java/commit/5946f7aa343915788848e00093c10b89ddfec9ee

Added new API to support passing in an aggregation list (comma-separated):

 @Method
 MetricsQueryDefinitionStages.WithMetricStartTimeFilter defineQuery(String metricsList, String aggregationList);

see:

New Revision: 29710

Log:
CLOUDHUB-357: initial implementation of batched metric queries. Unfortunately we can only support 5 queries batched,
 as Azure has a hard limit throttle for all subscriptions. Updated the poms changing 1.9.0 to 1.9.0-GW as this fix required 
 changes to Azure Java SDK and a pull request. 1.9.0-GW jars are currently available in geneva/nexus/ in repo groundwork-ee-m2-repo
