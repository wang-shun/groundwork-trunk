<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<portlet:defineObjects />
<c:set var="rows" value="${renderRequest.getPreferences().getValue('rows', '5')}"/>
<c:set var="service" value="${renderRequest.getPreferences().getValue('service', '')}"/>
<c:set var="serviceNames" value="${renderRequest.getPreferences().getValue('serviceNames', '')}"/>
<c:set var="serviceLabels" value="${renderRequest.getPreferences().getValue('serviceLabels', '')}"/>

<portlet:resourceURL var="readPrefs" id="readPrefs" escapeXml="false" />
<portlet:resourceURL var="writePrefs" id="writePrefs" escapeXml="false" />
<portlet:renderURL var="renderURL" escapeXml="false" windowState="normal" portletMode="view" />

<div class="panel panel-primary">
    <div class="panel-heading">Audit Log</div>
    <div ng-controller="AuditLogEditController" class='panel-body'
         ng-init="init('<%=renderResponse.encodeURL(readPrefs.toString())%>',
                       '<%=renderResponse.encodeURL(writePrefs.toString())%>', '<%=renderResponse.encodeURL(renderURL.toString())%>')">
        <form name="auditForm" class="form-horizontal app-form" novalidate>

            <div class="form-group">
                <label for="rows" class="col-sm-2 control-label">Rows</label>
                <div class="col-sm-10">
                    <input required  type="number" min="5" max="200" class="form-control" id="rows" name='rows' ng-model='prefs.rows' placeholder="Enter rows range: 5..200"  tabindex='1' ng-pattern="/^[0-9]{1,2,3}$/">
                    <div class='errorMessage' ng-show="auditForm.rows.$dirty && auditForm.rows.$error.required">Enter number of rows 5..200</div>
                </div>
            </div>

            <div class="form-group">
                <label for="refreshSeconds" class="col-sm-2 control-label">Refresh Rate (seconds)</label>
                <div class="col-sm-10">
                    <input required  type="number" class="form-control" id="refreshSeconds" name='refreshSeconds' ng-model='prefs.refreshSeconds' placeholder="Enter refresh rate (seconds):"  tabindex='2' ng-pattern="/^[0-9]{1,5}$/">
                    <div class='errorMessage' ng-show="auditForm.refreshSeconds.$dirty && auditForm.refreshSeconds.$error.required">Enter Refresh Rate in seconds</div>
                </div>
            </div>

            <button  class="btn btn-primary" ng-click="update(prefs)" ng-disabled="auditForm.$invalid || isUnchanged(prefs)" tabindex='6'>Submit</button>
            <span ng-show="auditForm.rows.$error.pattern">Not a valid number. Range is 1..20</span>
            <span ng-show="auditForm.refreshSeconds.$error.pattern">Not a valid number for  Refresh Rate in seconds</span>
            <alert ng-repeat="alert in alerts" type="alert.type" close="closeAlert($index)">{{alert.msg}}</alert>
            <pre ng-bind =" contact | json" ng-hide="!debug"> </pre>

        </form>
    </div>
</div>
