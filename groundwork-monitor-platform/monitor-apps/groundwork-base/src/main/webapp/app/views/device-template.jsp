<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<portlet:defineObjects />
<portlet:resourceURL var="readPrefs" id="readPrefs" escapeXml="false" />

<div class="panel panel-default no-border " ng-controller="DeviceTemplatesController" ng-init="init('<%=renderResponse.encodeURL(readPrefs.toString())%>')">
    <div class="panel-body row no-margin no-padding">

        <div class="col-sm-12 col-md-12 col-lg-12 no-padding">
            <table class="filter-table-2 col-sm-12 col-md-12 col-lg-12 hidden-xs">
                <tr>
                    <td class="col-sm-4 col-md-4 col-lg-4">
                        <div class="form-group">
                            <label for="audit-subsystem">Device Identification:</label>
                            <input id="audit-subsystem" class="form-control col-sm-12 col-md-12 col-lg-12" type="text" placeholder="Enter the identification of the device to filter on" ng-model="filters.id" ng-keyup="search()" />
                        </div>
                    </td>
                    <td class="col-sm-4 col-md-4 col-lg-4">
                        <div class="form-group">
                            <label for="audit-hostname">Cacti Template:</label>
                            <input id="audit-hostname" class="form-control col-sm-12 col-md-12 col-lg-12" type="text" placeholder="Enter the name of the Cacti template to filter on" ng-model="filters.cacti" ng-keyup="search()" />
                        </div>
                    </td>
                    <td class="col-sm-4 col-md-4 col-lg-4">
                        <div class="form-group">
                            <label for="audit-hostname">Monarch Template:</label>
                            <input id="audit-hostname" class="form-control col-sm-12 col-md-12 col-lg-12" type="text" placeholder="Enter the name of the Monarch template to filter on" ng-model="filters.monarch" ng-keyup="search()" />
                        </div>
                    </td>
                </tr>
            </table>
        </div>

        <div class="col-sm-12 col-md-12 col-lg-12 no-padding">
            <div class="col-sm-10 col-md-10 col-lg-10">
                <div class="form-group">
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
            <div class="col-sm-2 col-md-2 col-lg-2 text-right">
                <button id="audit-next" type="button" class="btn btn-default" ng-click="getNextPage()" ng-disabled="!page">
                    <span class="glyphicon glyphicon-chevron-left" aria-hidden="true"></span>
                </button>
                &nbsp;&nbsp;
                <button type="button" class="btn btn-default" ng-click="getPrevPage()" ng-disabled="deviceTemplates.length < perPage">
                    <span class="glyphicon glyphicon-chevron-right" aria-hidden="true"></span>
                </button>
            </div>
        </div>
    </div>

    <div class="panel-body panel-grid no-padding">
        <div id="grid" ng-grid="options"></div>
    </div>
</div>
