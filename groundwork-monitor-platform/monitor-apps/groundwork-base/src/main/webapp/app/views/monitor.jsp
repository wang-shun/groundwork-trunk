<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<portlet:defineObjects />
<portlet:resourceURL var="readPrefs" id="readPrefs" escapeXml="false" />

<div class="row">
    <div class="col-lg-12 col-md-12 col-sm-12">
        <div class="panel panel-default" ng-controller="InfrastructureController" ng-cloak ng-init="init('<%=renderResponse.encodeURL(readPrefs.toString())%>')">
            <!-- <div class="panel-heading"><h3 class="panel-title">Environment Map</h3></div> -->
            <alert ng-repeat="alert in alerts" type="{{alert.type}}" close="closeAlert($index)">{{alert.msg}}</alert>
            <div class="panel-body" no-padding>
                <tabset justified="true">
                    <tab select="switchTo('vm')">
                        <tab-heading><img src="/portal-groundwork-base/app/images/vm.png" class="tab-icon" /><span class="label label-info">{{vmCount}}</span><div class="tab-name">VMs</div></tab-heading>
                        <div class="nothing" ng-if="!items.length">No items here yet.</div>
                        <ul ng-if="items.length" class="icon-list vms">
                            <li ng-repeat="item in items" class="icon-list-item vm" gwp-status-hover>
                                <div class="status" ng-class="'status' + getStatusColor(item.monitorStatus, true)" gwp-status-icon></div>
                                <div class="img-container" ng-click="viewDetails(item.hostName)" ><img ng-src="/portal-groundwork-base/app/images/vm{{getStatusColor(item.monitorStatus)}}.png" /></div>
                                <p class="name">{{item.hostName}}</p>
                            </li>
                        </ul>
                    </tab>
                    <tab select="switchTo('hypervisor')">
                        <tab-heading><img src="/portal-groundwork-base/app/images/hypervisor.png" class="tab-icon" /><span class="label label-info">{{hypervisorCount}}</span><div class="tab-name">Hypervisors</div></tab-heading>
                        <div class="nothing" ng-if="!items.length">No items here yet.</div>
                        <ul ng-if="items.length" class="icon-list hypervisors">
                            <li ng-repeat="item in items" class="icon-list-item hypervisor" gwp-status-hover>
                                <div class="status" ng-class="'status' + getStatusColor(item.monitorStatus, true)" gwp-status-icon></div>
                                <div class="img-container" ng-click="viewDetails(item.hostName)" ><img ng-src="/portal-groundwork-base/app/images/hypervisor{{getStatusColor(item.monitorStatus)}}.png" /></div>
                                <p class="name">{{item.hostName}}</p>
                            </li>
                        </ul>
                    </tab>
                    <tab select="switchTo('datastore')">
                        <tab-heading><img src="/portal-groundwork-base/app/images/storage.png" class="tab-icon" /><span class="label label-info">{{datastoreCount}}</span><div class="tab-name">Datastores</div></tab-heading>
                        <div class="nothing" ng-if="!items.length">No items here yet.</div>
                        <ul ng-if="items.length" class="icon-list datastores">
                            <li ng-repeat="item in items" class="icon-list-item datastore" gwp-status-hover>
                                <div class="status" ng-class="'status' + getStatusColor(item.monitorStatus, true)" gwp-status-icon></div>
                                <div class="img-container" ng-click="viewDetails(item.hostName)" ><img ng-src="/portal-groundwork-base/app/images/storage{{getStatusColor(item.monitorStatus)}}.png" /></div>
                                <p class="name">{{item.hostName}}</p>
                            </li>
                        </ul>
                    </tab>
                    <tab select="switchTo('network')">
                        <tab-heading><img src="/portal-groundwork-base/app/images/network.png" class="tab-icon" /><span class="label label-info">{{networkCount}}</span><div class="tab-name">Networks</div></tab-heading>
                        <div class="nothing" ng-if="!items.length">No items here yet.</div>
                        <ul ng-if="items.length" class="icon-list hypervisors">
                            <li ng-repeat="item in items" class="icon-list-item hypervisor" gwp-status-hover>
                                <div class="status" ng-class="'status' + getStatusColor(item.monitorStatus, true)" gwp-status-icon></div>
                                <div class="img-container" ng-click="viewDetails(item.hostName)" ><img ng-src="/portal-groundwork-base/app/images/network{{getStatusColor(item.monitorStatus)}}.png" /></div>
                                <p class="name">{{item.hostName}}</p>
                            </li>
                        </ul>
                    </tab>
                </tabset>
            </div>
        </div>
    </div>
</div>

