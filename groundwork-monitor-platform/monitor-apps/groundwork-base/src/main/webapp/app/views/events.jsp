<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<portlet:defineObjects />
<portlet:resourceURL var="readPrefs" id="readPrefs" escapeXml="false" />

<div class="panel panel-default" ng-controller="EventsController" ng-init="init('<%=renderResponse.encodeURL(readPrefs.toString())%>')">
    <alert ng-repeat="alert in alerts" type="{{alert.type}}" close="closeAlert($index)">{{alert.msg}}</alert>

    <div class="panel-body no-padding">
        <div class="event-container">
            <ul class="event-list">
                <li ng-repeat="event in events" class="event" ng-class="'status' + getStatusColor(event.monitorStatus, true)" gwp-event-hover ng-click="viewDetails(event.event.host, event.service)">
                    {{event.service || '-'}}
                    <div class="count">{{event.count}}<div class="status" ng-class="'status' + getStatusColor(event.monitorStatus, true)" gwp-status-icon></div></div>
                </li>
            </ul>
        </div>
    </div>
</div>

