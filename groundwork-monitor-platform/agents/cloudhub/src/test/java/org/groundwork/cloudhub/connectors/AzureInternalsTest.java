/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package org.groundwork.cloudhub.connectors;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.credentials.ApplicationTokenCredentials;
import com.microsoft.azure.management.AccessManagement;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.compute.AvailabilitySet;
import com.microsoft.azure.management.compute.ComputeUsage;
import com.microsoft.azure.management.compute.VirtualMachine;
import com.microsoft.azure.management.dns.DnsZone;
import com.microsoft.azure.management.monitor.Metric;
import com.microsoft.azure.management.monitor.MetricCollection;
import com.microsoft.azure.management.monitor.MetricDefinition;
import com.microsoft.azure.management.monitor.MetricDefinitions;
import com.microsoft.azure.management.monitor.ResultType;
import com.microsoft.azure.management.msi.Identity;
import com.microsoft.azure.management.network.Network;
import com.microsoft.azure.management.network.PublicIPAddress;
import com.microsoft.azure.management.network.PublicIPAddresses;
import com.microsoft.azure.management.resources.*;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.rest.LogLevel;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class AzureInternalsTest {

    public Azure authenticate() throws IOException {
        final File credFile = new File("/usr/local/groundwork/config/cloudhub/azure/cloudhub.azureauth"); //System.getenv("AZURE_AUTH_LOCATION"));
        //DateTime recordDateTime = DateTime.parse("2018-01-24T00:07:40.350Z");

        return Azure.configure().withLogLevel(LogLevel.BASIC).authenticate(credFile).withSubscription("bfb47a94-d30e-42be-8625-1219690d6c5a"); //"groundwork-dev");
        //.withDefaultSubscription(); // 090e1923-c971-4b70-a880-ec363d7f5012 withSubscription("2d788296-3e5a-4e5d-801b-70560c676ed6" );
    }

    @Test
    public void testMetricsDefinitions() throws Exception {
        Azure azure = authenticate();
        PagedList<VirtualMachine> vms = azure.virtualMachines().list();
        long start = System.currentTimeMillis();
        VirtualMachine vm = azure.virtualMachines().list().get(0);
        MetricDefinitions definitions = azure.metricDefinitions();
        for (MetricDefinition definition : definitions.listByResource(vm.id())) {
            System.out.println(definition.name().value());
            System.out.println(definition.namespace());
        }
    }

    @Test
    public void testMetrics() throws Exception {
        Azure azure = authenticate();
        MetricDefinitions definitions = azure.metricDefinitions();
        Subscription subscription = azure.getCurrentSubscription();
        System.out.println("sub state: " + subscription.state().toString());
        System.out.println(subscription.displayName());
        System.out.println(subscription.subscriptionId());

        PagedList<VirtualMachine> vms = azure.virtualMachines().list();
        long start = System.currentTimeMillis();
        for (VirtualMachine vm : vms) {
            listMetrics(definitions, vm.id());
            //listEvents(azure, vm.id());
        }
        System.out.println(System.currentTimeMillis() - start);

        start = System.currentTimeMillis();
        for (VirtualMachine vm : vms) {
            listMetricsMultiple(definitions, vm.id());
        }
        System.out.println(System.currentTimeMillis() - start);

        start = System.currentTimeMillis();
        for (VirtualMachine vm : vms) {
            System.out.println("vm id = " + vm.id());
            listMetricsMultiple(definitions, vm.id());
        }
        System.out.println(System.currentTimeMillis() - start);

        start = System.currentTimeMillis();
        for (VirtualMachine vm : vms) {
            listMetrics(definitions, vm.id());
        }
        System.out.println(System.currentTimeMillis() - start);
    }

    private static void listMetricsMultiple(MetricDefinitions definitions, String id) {
        DateTime recordDateTime = DateTime.now();
        List<MetricDefinition> md = definitions.listByResource(id);
        if (md != null) {
            List<String> names = new LinkedList<>();
            List<String> aggTypes = new LinkedList<>();
            MetricDefinition definition = md.get(0);
            int count = 0;
            for (MetricDefinition def : md) {
                names.add(def.name().value());
                aggTypes.add(def.primaryAggregationType().toString());
                count++;
                if (count >= 5) break;
            }
            String multiQuery = StringUtils.join(names, ",");
            String multiAggs = StringUtils.join(aggTypes, ",");
            MetricCollection metrics = definition.defineQuery(multiQuery, multiAggs)
                    .startingFrom(recordDateTime.minusMinutes(2))
                    .endsBefore(recordDateTime)
                    .withResultType(ResultType.DATA)
                    .execute();
            if (metrics.inner() != null) {
                List<Metric> ms = metrics.metrics();
                for (Metric m : ms) {
                    //switch (m.unit())
                    System.out.println("metric = " + m.name().value() + ", " + m.timeseries().get(0).data().get(0).timeStamp());
                }
            }
            //definition.defineQuery().startingFrom(recordDateTime.minusMinutes(2))
        }
    }

    private static void listMetrics(MetricDefinitions definitions, String id) {
        DateTime recordDateTime = DateTime.now();
        List<MetricDefinition> md = definitions.listByResource(id);
        int count = 0;
        for (MetricDefinition definition : md) {
            MetricCollection metrics = definition.defineQuery()
                    .startingFrom(recordDateTime.minusMinutes(2))
                    .endsBefore(recordDateTime)
                    .withResultType(ResultType.DATA)
                    .execute();
            if (metrics.inner() != null) {
                List<Metric> ms = metrics.metrics();
                for (Metric m : ms) {
                    //switch (m.unit())
                    System.out.println("metric = " + m.name() + ", " + m.timeseries().get(0).data().get(0).timeStamp());
                }
            }
            count++;
            if (count >= 5) break;
        }
    }

    @Test
    public void testAccessManagement() throws Exception {
        Azure azure = authenticate();
        System.out.println(azure.getCurrentSubscription().displayName());

        AccessManagement am = azure.accessManagement();
        //am.activeDirectoryUsers().list();
        //am.activeDirectoryGroups().list();
        //am.servicePrincipals().list();
        // TODO: gets an error: Status code 403, {"odata.error":{"code":"Authorization_RequestDenied","message":{"lang":"en","value":"Insufficient privileges to complete the operation."}}}
//        for (ActiveDirectoryApplication ada : am.activeDirectoryApplications().list()) {
//            System.out.println(ada.name());
//        }

        // TODO:                azure.deployments()

        for (DnsZone zone : azure.dnsZones().list()) {
            zone.aRecordSets();
        }
        for (Network network : azure.networks().list()) {
            System.out.println("net: " + network.name());
            System.out.println("net: " + network.id());
            List<String> ips = network.dnsServerIPs();
        }
        for (AvailabilitySet set : azure.availabilitySets().list()) {
            System.out.println(set.name());
            System.out.println(set.id());
        }

        for (Feature feature : azure.features().list()) {
            System.out.println("feature " + feature.name());

        }
        PublicIPAddresses pias = azure.publicIPAddresses();
        for (PublicIPAddress pub : pias.list()) {
            System.out.println("" + pub.fqdn());
            System.out.println("" + pub.ipAddress());
            System.out.println("" + pub.id());
            System.out.println("" + pub.name());
        }
        // get Subscriptions
        Subscriptions subscriptions = azure.subscriptions();
        for (Subscription subscription : subscriptions.list()) {
            System.out.println("Subscription Display Name: " + subscription.displayName());
            System.out.println("Subscription Id: " + subscription.subscriptionId());
            System.out.println("Subscription State: " + subscription.state());
        }

        // get resource groups
        ResourceGroups resourceGroups = azure.resourceGroups();
        for (ResourceGroup resourceGroup : resourceGroups.list()) {
            System.out.println("ResourceGroup Id: " + resourceGroup.id());
            System.out.println("ResourceGroup Name: " + resourceGroup.name());

//			ResourceGroupExportTemplateOptions options = ResourceGroupExportTemplateOptions.INCLUDE_BOTH;
//			ResourceGroupExportResult rgExpResult = resourceGroup.exportTemplate(options);
//			System.out.println("ResourceGroup Export Result Json: " + rgExpResult.templateJson());

        }

        Providers providers = azure.providers();
        for (Provider provider : providers.list()) {
            System.out.println("Provider Key: " + provider.key());
            System.out.println("Provider Namespace: " + provider.namespace());
            System.out.println("Registration State: " + provider.registrationState());
            for (ProviderResourceType pResType : provider.resourceTypes()) {
                System.out.println("\tResource Type: " + pResType.resourceType());
                Map<String, String> resProperties = pResType.properties();
                System.out.println(resProperties);
            }
        }

        for (Identity id : azure.identities().list()) {
            System.out.println(id.tenantId());
            System.out.println(id.principalId());
            System.out.println(id.clientId());
            System.out.println(id.name());
        }

    }

    @Test
    public void testResourceGroups() throws Exception {
        Azure azure = authenticate();
        PagedList<GenericResource> subscriptResources = azure.genericResources().list();
        for (GenericResource azureResource : subscriptResources) {
            System.out.println("resource: " + azureResource.name() + ", type: " + azureResource.type() + ", group: " + azureResource.resourceGroupName());
        }
//
//            for (ResourceGroup resourceGroup : azure.resourceGroups().list()) {
//            System.out.println("ResourceGroup Id: " + resourceGroup.id());
//            System.out.println("ResourceGroup Name: " + resourceGroup.name());
//            ResourceGroupExportTemplateOptions options = ResourceGroupExportTemplateOptions.INCLUDE_BOTH;
//            ResourceGroupExportResult rgExpResult = resourceGroup.exportTemplate(options);
//            System.out.println("ResourceGroup Export Result Json: " + rgExpResult.templateJson());
//        }
    }

    @Test
    public void testUsage() throws Exception {
        Azure azure = authenticate();
        //azure.getCurrentSubscription().subscriptionPoli
        PagedList<ComputeUsage> usages = azure.computeUsages().listByRegion(Region.US_WEST2);
        for (ComputeUsage usage : usages) {
            System.out.println("usage: " + usage.name().value() + usage.name().localizedValue() + ", " + usage.currentValue() + ", " + usage.unit());
        }
    }

    @Test
    public void testBilling() throws Exception {
        final File credFile = new File("/usr/local/groundwork/config/cloudhub/azure/cloudhub.azureauth"); //System.getenv("AZURE_AUTH_LOCATION"));
        ApplicationTokenCredentials credentials = ApplicationTokenCredentials.fromFile(credFile);

// TODO: billing manager is beta and not included in the public API of Azure 1.10
// we can explicitly include, but the APIs are not finalized, you have to access inner classes to use

//         <dependency>
//            <groupId>com.microsoft.azure</groupId>
//            <artifactId>azure-mgmt-billing</artifactId>
//            <version>1.10.1-beta-SNAPSHOT</version>
//        </dependency>

//        BillingManager bm = BillingManager.authenticate(credentials, credentials.defaultSubscriptionId());
//        for (InvoiceInner inner : bm.inner().invoices().list()) {
//            System.out.println(inner.downloadUrl());
//        }

    }

}
