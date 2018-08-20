<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<portlet:defineObjects />
<portlet:resourceURL var="listConfigs" id="listConfigs" escapeXml="false" />
<portlet:resourceURL var="lookupConfig" id="lookupConfig" escapeXml="false" />
<portlet:resourceURL var="saveConfig" id="saveConfig" escapeXml="false" />
<portlet:resourceURL var="removeConfig" id="removeConfig" escapeXml="false" />
<portlet:resourceURL var="configExists" id="configExists" escapeXml="false" />

<div class="panel panel-default relative" style="margin-bottom: 0px" ng-controller="NocBoardConfigController as noc" gwp-fit-grid
     ng-init="init('<%=renderResponse.encodeURL(listConfigs.toString())%>',
                   '<%=renderResponse.encodeURL(lookupConfig.toString())%>', '<%=renderResponse.encodeURL(saveConfig.toString())%>',
                   '<%=renderResponse.encodeURL(removeConfig.toString())%>', '<%=renderResponse.encodeURL(configExists.toString())%>', '<%=renderRequest.getAttribute("jboss")%>')">

    <!-- <div class="loading-overlay" ng-show="isLoading">Loading...</div> -->
    <div class="form-group" style="margin-bottom: 0px">
        <div>
            <alert ng-repeat="alert in alerts" type="alert.type" close="closeAlert($index)"><strong>{{alert.msg}}</strong></alert>
        </div>
    </div>
    <accordion close-others="true" class="panel-group panel-group-noc" aria-multiselectable="true">
        <accordion-group class="panel panel-noc" is-open="isOpen" id="noc-config-group" gwp-accordion-state>
            <accordion-heading class="panel-heading">
              <span class="panel-heading-cell panel-heading-cell-expander" ng-style="{'background': getBackgroundColor()}">
                <i class="fa fa-plus"></i>&nbsp;NOC Board Configurations
                <button type="button" class="btn btn-default btn-sm pull-right" ng-click="newBoard()">New</button>
              </span>
            </accordion-heading>
            <div class="panel-collapse collapse in">
                <div class="panel-body">
                    <div class="col-sm-6" ng-hide="!boards.length">
                        <select class="form-control" ng-model="selectedBoardName">
                            <option ng-repeat="board in boards" value="{{board.name}}" ng-selected="board.name === selectedBoardName">{{board.name}}<span ng-show="!!board.title">&nbsp;&mdash;&nbsp;</span>{{board.title}}</option>
                        </select>
                    </div>
                    <button type="button" class="btn btn-default btn-sm btn-near-form" ng-hide="!boards.length" ng-click="editBoard(selectedBoardName)" ng-disabled="!selectedBoardName">Edit</button>
                </div>
            </div>
        </accordion-group>
    </accordion>
</div>
