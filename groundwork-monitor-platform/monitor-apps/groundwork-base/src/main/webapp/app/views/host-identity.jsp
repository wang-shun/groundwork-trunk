<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<portlet:defineObjects />
<portlet:resourceURL var="readPrefs" id="readPrefs" escapeXml="false" />

<div class="panel panel-default no-border " ng-controller="HostIdentitiesController" ng-init="init('<%=renderResponse.encodeURL(readPrefs.toString())%>')">
    <div class="panel-body row no-margin">

        <div class="col-sm-10 col-md-10 col-lg-10 no-padding">
            <div class="form-group">
                <div class="col-sm-6 col-md-6 col-lg-6 no-padding">
                    <input type="text" class="form-control" id="host-identities-search" placeholder="Filter Hosts" ng-model="filters.hostname" ng-keyup="search()" />
                </div>
                &nbsp;&nbsp;
                <button type="button" class="btn btn-default" ng-click="addHost()">
                    <span class="glyphicon glyphicon-plus" aria-hidden="true"></span>
                </button>
                &nbsp;&nbsp;
                <button type="button" class="btn btn-default" ng-disabled="!options.selectedItems.length" ng-click="deleteHosts()">
                    <span class="glyphicon glyphicon-trash" aria-hidden="true"></span>
                </button>
                &nbsp;&nbsp;
                <button type="button" class="btn btn-default" ng-disabled="!options.selectedItems.length">
                    <span class="glyphicon glyphicon-download-alt" aria-hidden="true"></span>
                </button>
            </div>
        </div>

        <div class="col-sm-2 col-md-2 col-lg-2 text-right no-padding">
            <button id="audit-next" type="button" class="btn btn-default" ng-click="getNextPage()" ng-disabled="!page">
                <span class="glyphicon glyphicon-chevron-left" aria-hidden="true"></span>
            </button>
            &nbsp;&nbsp;
            <button type="button" class="btn btn-default" ng-click="getPrevPage()" ng-disabled="hosts.length < perPage">
                <span class="glyphicon glyphicon-chevron-right" aria-hidden="true"></span>
            </button>
        </div>
    </div>

    <div class="panel-body panel-grid no-padding">
        <div id="grid" ng-grid="options"></div>
    </div>
</div>
