<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<portlet:defineObjects />
<portlet:resourceURL var="hitlist" id="hitlist" escapeXml="false" />
<div class="panel panel-default" ng-controller="HitListController" gwp-fit-grid ng-init="init('<%=renderResponse.encodeURL(hitlist.toString())%>')">
    <div class="form-group">
        <div>
            <alert ng-repeat="alert in alerts" type="alert.type" close="closeAlert($index)"><strong>{{alert.msg}}</strong></alert>
        </div>
        <br />
    </div>
    <div class="panel-body panel-grid no-padding">
        <div class="problemSection">
            <p class="problemTypeName">Host Problems</p>
            <div class="typeContainer">
                <div class="problemType" ng-class="{selected: current['hosts'] == 'hostsDownUnacknowledged'}"><div class="typeIndicator downUnacknowledgedType" tooltip-placement="top" tooltip="Down and Unacknowledged" ng-click="setCurrent('hosts', 'hostsDownUnacknowledged')" ng-bind="data.counts.hostsDownUnacknowledged">0</div><div class="pointer"></div></div>
                <div class="problemType" ng-class="{selected: current['hosts'] == 'hostsDownAcknowledged'}"><div class="typeIndicator warningUnacknowledgedType" tooltip-placement="top" tooltip="Down and Acknowledged" ng-click="setCurrent('hosts', 'hostsDownAcknowledged')" ng-bind="data.counts.hostsDownAcknowledged">0</div><div class="pointer"></div></div>
                <div class="problemType" ng-class="{selected: current['hosts'] == 'hostsScheduledDown'}"><div class="typeIndicator criticalAcknowledgedType" tooltip-placement="top" tooltip="Hosts Scheduled Down" ng-click="setCurrent('hosts', 'hostsScheduledDown')" ng-bind="data.counts.hostsScheduledDown">0</div><div class="pointer"></div></div>
                <div class="problemType" ng-class="{selected: current['hosts'] == 'hostsUnreachable'}"><div class="typeIndicator warningAcknowledgedType" tooltip-placement="top" tooltip="Hosts Unreachable" ng-click="setCurrent('hosts', 'hostsUnreachable')" ng-bind="data.counts.hostsUnreachable">0</div><div class="pointer"></div></div>
            </div>
            <p class="problemTypeName" ng-bind="getCurrentTypeName('hosts')">&nbsp;</p>
            <div id="grid1" ng-grid="optionsHosts" gwp-adjust-grid></div>
        </div>
        <div class="problemSection">
            <p class="problemTypeName">Service Problems (Hosts Up)</p>
            <div class="typeContainer">
                <div class="problemType" ng-class="{selected: current['serviceUp'] == 'servicesCriticalUnacknowledged'}"><div class="typeIndicator downUnacknowledgedType" tooltip-placement="top" tooltip="Critical and Unacknowledged" ng-click="setCurrent('serviceUp', 'servicesCriticalUnacknowledged')" ng-bind="data.counts.servicesCriticalUnacknowledged">0</div><div class="pointer"></div></div>
                <div class="problemType" ng-class="{selected: current['serviceUp'] == 'servicesWarningUnacknowledged'}"><div class="typeIndicator warningUnacknowledgedType" tooltip-placement="top" tooltip="Warning and Unacknowledged" ng-click="setCurrent('serviceUp', 'servicesWarningUnacknowledged')" ng-bind="data.counts.servicesWarningUnacknowledged">0</div><div class="pointer"></div></div>
                <div class="problemType" ng-class="{selected: current['serviceUp'] == 'servicesCriticalAcknowledged'}"><div class="typeIndicator criticalAcknowledgedType" tooltip-placement="top" tooltip="Critical and Acknowledged" ng-click="setCurrent('serviceUp', 'servicesCriticalAcknowledged')" ng-bind="data.counts.servicesCriticalAcknowledged">0</div><div class="pointer"></div></div>
                <div class="problemType" ng-class="{selected: current['serviceUp'] == 'servicesWarningAcknowledged'}"><div class="typeIndicator warningAcknowledgedType" tooltip-placement="top" tooltip="Warning and Acknowledged" ng-click="setCurrent('serviceUp', 'servicesWarningAcknowledged')" ng-bind="data.counts.servicesWarningAcknowledged">0</div><div class="pointer"></div></div>
            </div>
            <p class="problemTypeName" ng-bind="getCurrentTypeName('serviceUp')">&nbsp;</p>
            <div id="grid2" ng-grid="optionsServiceUp" gwp-adjust-grid></div>
        </div>
        <div class="problemSection problemSectionSmall">
            <p class="problemTypeName">Service Problems (Hosts Down)</p>
            <div class="typeContainer">
                <div class="problemType" ng-class="{selected: current['serviceDown'] == 'servicesCriticalDown'}"><div class="typeIndicator downUnacknowledgedType" tooltip-placement="top" tooltip="Critical on Down Hosts" ng-click="setCurrent('serviceDown', 'servicesCriticalDown')" ng-bind="data.counts.servicesCriticalDown">0</div><div class="pointer"></div></div>
                <div class="problemType" ng-class="{selected: current['serviceDown'] == 'servicesWarningDown'}"><div class="typeIndicator warningUnacknowledgedType" tooltip-placement="top" tooltip="Warning on Down Hosts" ng-click="setCurrent('serviceDown', 'servicesWarningDown')" ng-bind="data.counts.servicesWarningDown">0</div><div class="pointer"></div></div>
            </div>
            <p class="problemTypeName" ng-bind="getCurrentTypeName('serviceDown')">&nbsp;</p>
            <div id="grid3" ng-grid="optionsServiceDown" gwp-adjust-grid></div>
        </div>
        <br class="clearfix" />
    </div>
</div>
