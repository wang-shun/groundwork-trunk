<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<portlet:defineObjects />

<portlet:resourceURL var="readPrefs" id="readPrefs" escapeXml="false" />
<portlet:resourceURL var="writePrefs" id="writePrefs" escapeXml="false" />
<portlet:renderURL var="renderURL" escapeXml="false" windowState="normal" portletMode="view" />

<div class="panel panel-primary">
    <div class="panel-heading">Environment Map Preferences</div>
    <div ng-controller="MonitorEditController" class='panel-body'
         ng-init="init('<%=renderResponse.encodeURL(readPrefs.toString())%>',
                       '<%=renderResponse.encodeURL(writePrefs.toString())%>', '<%=renderResponse.encodeURL(renderURL.toString())%>')">
        <form name="topForm" class="form-horizontal app-form" novalidate>

            <div class="form-group">
                <label for="sortOrders" class="col-sm-2 control-label">Sort Order</label>
                <div class="col-sm-10">
                    <select id='sortOrders' class='form-control' ng-model='prefs.sortOrder' ng-options="name as name for name in sortOrders"></select>
                </div>
            </div>

            <div class="form-group">
                <label for="refreshSeconds" class="col-sm-2 control-label">Refresh Rate (seconds)</label>
                <div class="col-sm-10">
                    <input required  type="number" class="form-control" id="refreshSeconds" name='refreshSeconds' ng-model='prefs.refreshSeconds' placeholder="Enter refresh rate (seconds):"  tabindex='1' ng-pattern="/^[0-9]{1,5}$/">
                    <div class='errorMessage' ng-show="topForm.refreshSeconds.$dirty && topForm.refreshSeconds.$error.required">Enter Refresh Rate in seconds</div>
                </div>
            </div>

            <button  class="btn btn-primary" ng-click="update(prefs)" ng-disabled="topForm.$invalid || isUnchanged(prefs)" tabindex='6'>Submit</button>
            <span ng-show="topForm.refreshSeconds.$error.pattern">Not a valid number for  Refresh Rate in seconds</span>
            <alert ng-repeat="alert in alerts" type="alert.type" close="closeAlert($index)">{{alert.msg}}</alert>
            <pre ng-bind =" contact | json" ng-hide="!debug"> </pre>

        </form>
    </div>
</div>
