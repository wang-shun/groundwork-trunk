package org.groundwork.cloudhub.connectors.amazon;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClient;
import com.amazonaws.services.cloudwatch.model.DescribeAlarmsRequest;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancingClient;
import com.amazonaws.services.elasticloadbalancing.model.DescribeLoadBalancersResult;
import com.amazonaws.services.elasticloadbalancing.model.LoadBalancerDescription;
import com.amazonaws.services.rds.AmazonRDSClient;
import com.amazonaws.services.rds.model.DescribeDBEngineVersionsRequest;
import com.microsoft.azure.management.network.LoadBalancer;

public class AWSConnection {

    private static final String DOMAIN_PREFIX_EC2 = "ec2.";
    private static final String DOMAIN_PREFIX_RDS = "rds.";
    private static final String DOMAIN_PREFIX_CLOUDWATCH = "monitoring.";
    private static final String DOMAIN_PREFIX_ELB = "elasticloadbalancing.";

    private AmazonCloudWatchClient metricsClient = null;
    private AmazonEC2Client ec2Client = null;
    private AmazonRDSClient rdsClient = null;
    private AmazonElasticLoadBalancingClient elbClient = null;

    // IAM Roles only work when running on an EC2 Instance
    // SEE: https://docs.aws.amazon.com/sdk-for-java/v1/developer-guide/java-dg-roles.html
    public AWSConnection(String accessKey, String secretKey, String endpointDomain, boolean useSSL, boolean enableIAMRoles) {

        AWSCredentials awsCreds = (enableIAMRoles) ?
                new InstanceProfileCredentialsProvider(false).getCredentials() :
                new BasicAWSCredentials(accessKey, secretKey);

        ec2Client = new AmazonEC2Client(awsCreds);
        rdsClient = new AmazonRDSClient(awsCreds);
        metricsClient = new AmazonCloudWatchClient(awsCreds);
        elbClient = new AmazonElasticLoadBalancingClient(awsCreds);

        final String HTTP = "http://";
        final String HTTPS = "https://";
        String protocol = (useSSL ? HTTPS : HTTP);
        ec2Client.setEndpoint(protocol + DOMAIN_PREFIX_EC2 + endpointDomain);
        metricsClient.setEndpoint(protocol + DOMAIN_PREFIX_CLOUDWATCH + endpointDomain);

        // RDS service only supports SSL
        rdsClient.setEndpoint(HTTPS + DOMAIN_PREFIX_RDS + endpointDomain);
        elbClient.setEndpoint(protocol + DOMAIN_PREFIX_ELB + endpointDomain );
    }
    
    public void testConnection() throws AmazonClientException {
        // We want to test each service since we'll need them all, and the ability to
        // access one service does not mean you can access them all.
        // I tried to pick requests that seem light-weight, but that's not so easy.
        // The requests will throw an exception if there are problems using the service.
        ec2Client.describeAccountAttributes();
        
        DescribeDBEngineVersionsRequest describeDBEngineVersionsRequest = new DescribeDBEngineVersionsRequest();
        describeDBEngineVersionsRequest.setMaxRecords(20); // 20 is minimum
        rdsClient.describeDBEngineVersions(describeDBEngineVersionsRequest);
        
        DescribeAlarmsRequest describeAlarmsRequest = new DescribeAlarmsRequest();
        describeAlarmsRequest.setMaxRecords(20); // 20 is minimum
        metricsClient.describeAlarms(describeAlarmsRequest);
    }
    
    public AmazonEC2Client getEC2Client() {
        return ec2Client;
    }
    
    public AmazonRDSClient getRDSClient() {
        return rdsClient;
    }
    
    public AmazonCloudWatchClient getMetricsClient() {
        return metricsClient;
    }

    public AmazonElasticLoadBalancingClient getElbClient() {
        return elbClient;
    }
}
