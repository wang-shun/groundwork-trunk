<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<portlet:defineObjects />
<c:set var="rows" value="${renderRequest.getPreferences().getValue('rows', '15')}"/>

<portlet:resourceURL var="readPrefs" id="readPrefs" escapeXml="false" />
<portlet:resourceURL var="writePrefs" id="writePrefs" escapeXml="false" />
<portlet:renderURL var="renderURL" escapeXml="false" windowState="normal" portletMode="view" />

<div class="panel panel-primary">
    <div class="panel-heading">Unhandled Hosts and Services</div>
    <div ng-controller="HitListEditController" class='panel-body'
         ng-init="init('<%=renderResponse.encodeURL(readPrefs.toString())%>',
                       '<%=renderResponse.encodeURL(writePrefs.toString())%>', '<%=renderResponse.encodeURL(renderURL.toString())%>')">
        <form name="hitlistForm" class="form-horizontal app-form" novalidate>

            <div class="form-group">
                <label for="rows" class="col-sm-2 control-label">Rows</label>
                <div class="col-sm-10">
                    <input required  type="number" min="1" max="30" class="form-control" id="rows" name='rows' ng-model='prefs.rows' placeholder="Enter rows range: 1..30"  tabindex='1' ng-pattern="/^[0-9]{1,2}$/">
                    <div class="errorMessage" ng-show="hitlistForm.rows.$dirty && hitlistForm.rows.$invalid">Enter number of rows 1..30</div>
                </div>
            </div>

            <div class="form-group">
                <label for="refreshSeconds" class="col-sm-2 control-label">Refresh Rate (seconds)</label>
                <div class="col-sm-10">
                    <input required  type="number" class="form-control" id="refreshSeconds" name='refreshSeconds' ng-model='prefs.refreshSeconds' placeholder="Enter refresh rate (seconds): 0..3600"  tabindex='2' ng-pattern="/^[0-9]{1,5}$/" min="0" max="3600">
                    <div class="errorMessage" ng-show="hitlistForm.refreshSeconds.$dirty && hitlistForm.refreshSeconds.$invalid">Enter Refresh Rate in seconds 0..3600</div>
                </div>
            </div>

            <div class="form-group">
                <label for="refreshSeconds" class="col-sm-2 control-label">Tracked Host Groups</label>
                <div class="col-sm-4">
                    <select class="form-control" ng-model="prefs.hostGroup">
                        <option value="--ALL--" ng-selected="!prefs.hostGroup">All Host Groups</option>
                        <option disabled="true">---------------</option>
                        <option ng-repeat="hostgroup in prefs.hostGroups" ng-value="hostgroup" ng-checked="prefs.hostGroup == hostgroup" ng-selected="prefs.hostGroup == hostgroup">{{hostgroup}}</option>
                    </select>
                </div>
            </div>

            <button  class="btn btn-primary" ng-click="update(prefs)" ng-disabled="hitlistForm.$invalid || isUnchanged(prefs)" tabindex='6'>Submit</button>
            <span class="errorMessage" ng-show="hitlistForm.rows.$error.pattern">Not a valid number. Range is 1..20</span>
            <span class="errorMessage" ng-show="hitlistForm.refreshSeconds.$error.pattern">Not a valid number for  Refresh Rate in seconds</span>
            <alert ng-repeat="alert in alerts" type="alert.type" close="closeAlert($index)">{{alert.msg}}</alert>
            <pre ng-bind =" contact | json" ng-hide="!debug"> </pre>

        </form>
    </div>
</div>
