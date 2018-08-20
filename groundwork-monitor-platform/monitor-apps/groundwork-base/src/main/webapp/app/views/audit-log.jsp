<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<portlet:defineObjects />
<portlet:resourceURL var="readPrefs" id="readPrefs" escapeXml="false" />

<div class="panel panel-default no-border " ng-controller="AuditLogController" ng-init="init('<%=renderResponse.encodeURL(readPrefs.toString())%>')">
    <div class="panel-body no-padding audit-nav-container row no-margin">
        <form role="form" class="col-xs-9 col-sm-10 col-md-10 col-lg-10">
            <table class="filter-table col-sm-12 col-md-12 col-lg-12 hidden-xs">
                <tr>
                    <td class="col-sm-5 col-md-5 col-lg-5">
                        <div class="form-group">
                            <label for="audit-subsystem">Sub System:</label>
                            <input id="audit-subsystem" class="form-control col-sm-12 col-md-12 col-lg-12" type="text" placeholder="Enter the name of a subsystem to filter on" ng-model="filters.subsystem" />
                        </div>
                    </td>
                    <td class="col-sm-5 col-md-5 col-lg-5">
                        <div class="form-group">
                            <label for="audit-hostname">Host or Group Name:</label>
                            <input id="audit-hostname" class="form-control col-sm-12 col-md-12 col-lg-12" type="text" placeholder="Enter the name of host to filter on" ng-model="filters.hostname" />
                        </div>
                    </td>
                    <td class="col-sm-2 col-md-2 col-lg-2">&nbsp;</td>
                </tr>
                <tr>
                    <td class="col-sm-5 col-md-5 col-lg-5">
                        <div class="form-group">
                            <label for="audit-service">Service Description:</label>
                            <input id="audit-service" class="form-control col-sm-12 col-md-12 col-lg-12" type="text" placeholder="Enter the name of a service to filter on" ng-model="filters.servicename" />
                        </div>
                    </td>
                    <td class="col-sm-5 col-md-5 col-lg-5">
                        <div class="form-group">
                            <label for="audit-username">User Name:</label>
                            <input id="audit-username" class="form-control col-sm-12 col-md-12 col-lg-12" type="text" placeholder="Enter the user name to filter on" ng-model="filters.username" />
                        </div>
                    </td>
                    <td class="col-sm-2 col-md-2 col-lg-2">&nbsp;</td>
                </tr>
                <tr>
                    <td class="col-sm-5 col-md-5 col-lg-5">
                        <div class="form-group">
                            <label for="audit-timestamp">Date-Time Range:</label>
                            <div class="col-sm-12 col-md-12 col-lg-12 no-padding">
                                <div class="col-sm-5 col-md-5 col-lg-5 no-padding">
                                    <div class="form-group">
                                        <div class='input-group date'>
                                            <input type='text' class="form-control" id="audit-timestampmin" placeholder="Y-m-d" gwp-datepicker="filters.timestampmin" />
                                            <span class="input-group-addon">
                                                <span class="glyphicon glyphicon-calendar"></span>
                                            </span>
                                        </div>
                                    </div>
                                </div>
                                <div class="col-sm-2 col-md-2 col-lg-2 text-center">-</div>
                                <div class="col-sm-5 col-md-5 col-lg-5 no-padding">
                                    <div class="form-group">
                                        <div class='input-group date'>
                                            <input type='text' class="form-control" id="audit-timestampmax" placeholder="Y-m-d" gwp-datepicker="filters.timestampmax" />
                                            <span class="input-group-addon">
                                                <span class="glyphicon glyphicon-calendar"></span>
                                            </span>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </td>
                    <td class="col-sm-5 col-md-5 col-lg-5">
                        <div class="form-group">
                            <label for="audit-message">Message:</label>
                            <input id="audit-message" class="form-control col-sm-12 col-md-12 col-lg-12" type="text" placeholder="Enter a part of activity pessage to filter on" ng-model="filters.message" />
                        </div>
                    </td>
                    <td class="col-sm-2 col-md-2 col-lg-2">
                        <div class="form-group">
                            <label>&nbsp;</label><br />
                            <button type="button" class="btn btn-default" ng-click="getPage(0)">Filter</button>&nbsp;&nbsp;<button type="button" class="btn btn-danger" ng-click="reset()">Reset</button>
                        </div>
                    </td>
                </tr>
            </table>

            <table class="filter-table col-xs-12 hidden-sm hidden-md hidden-lg">
                <tr>
                    <td class="col-xs-12">
                        <div class="form-group">
                            <label for="audit-subsystem">Sub System:</label>
                            <input id="audit-subsystem" class="form-control col-xs-12" type="text" placeholder="Enter the name of a subsystem to filter on" ng-model="filters.subsystem" />
                        </div>
                    </td>
                </tr>
                <tr>
                    <td class="col-xs-12">
                        <div class="form-group">
                            <label for="audit-hostname">Host Name:</label>
                            <input id="audit-hostname" class="form-control col-xs-12" type="text" placeholder="Enter the name of host to filter on" ng-model="filters.hostname" />
                        </div>
                    </td>
                </tr>
                <tr>
                    <td class="col-xs-12">
                        <div class="form-group">
                            <label for="audit-service">Service Name:</label>
                            <input id="audit-service" class="form-control col-xs-12" type="text" placeholder="Enter the name of a service to filter on" ng-model="filters.servicename" />
                        </div>
                    </td>
                </tr>
                <tr>
                    <td class="col-xs-12">
                        <div class="form-group">
                            <label for="audit-username">User Name:</label>
                            <input id="audit-username" class="form-control col-xs-12" type="text" placeholder="Enter the user name to filter on" ng-model="filters.username" />
                        </div>
                    </td>
                </tr>
                <tr>
                    <td class="col-xs-12">
                        <div class="form-group">
                            <label for="audit-timestamp">Time Stamp:</label>
                            <input id="audit-timestamp" class="form-control col-xs-12" type="text" placeholder="Y-m-d" gwp-datepicker="filters.timestamp" />
                        </div>
                    </td>
                </tr>
                <tr>
                    <td class="col-xs-12">
                        <div class="form-group">
                            <label for="audit-message">Message:</label>
                            <input id="audit-message" class="form-control col-xs-12" type="text" placeholder="Enter a part of activity pessage to filter on" ng-model="filters.message" />
                        </div>
                    </td>
                </tr>
                <tr>
                    <td class="col-xs-12">
                        <div class="form-group">
                            <button type="button" class="btn btn-default" ng-click="getPage(0)">Filter</button>&nbsp;&nbsp;<button type="button" class="btn btn-danger" ng-click="reset()">Reset</button>
                        </div>
                    </td>
                </tr>
            </table>

        </form>

        <div class="audit-nav-top col-xs-3 col-sm-2 col-md-2 col-lg-2 text-right">
            <div class="form-group">
                <button id="audit-next" type="button" class="btn btn-default" ng-click="getNextPage()" ng-disabled="!page">
                    <span class="glyphicon glyphicon-chevron-left" aria-hidden="true"></span>
                </button>
                &nbsp;&nbsp;
                <button type="button" class="btn btn-default" ng-click="getPrevPage()" ng-disabled="auditLogs.length < perPage">
                    <span class="glyphicon glyphicon-chevron-right" aria-hidden="true"></span>
                </button>
            </div>
        </div>
    </div>
    
    <div class="panel-body panel-grid no-padding">
        <div id="grid" ng-grid="options"></div>
    </div>
</div>
