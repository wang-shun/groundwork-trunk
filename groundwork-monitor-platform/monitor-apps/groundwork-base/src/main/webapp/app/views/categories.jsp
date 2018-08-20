<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<portlet:defineObjects />
<portlet:resourceURL var="readPrefs" id="readPrefs" escapeXml="false" />

<div class="panel panel-default no-border relative" ng-controller="CategoriesController" ng-init="init('<%=renderResponse.encodeURL(readPrefs.toString())%>')">
    <div class="legend">
        <a href="#" id="add-cg-root" class="add-root pull-right" ng-click="addRoot('CUSTOM_GROUP')">CG+</a>

        <a href="#" id="add-hg-root" class="add-root" ng-click="showHostsView()">HG+</a>
        <input type="checkbox" id="show-hg-root" class="add-root" ng-click="showRoots('HOSTGROUP')"></a>

        <a href="#" id="add-sg-root" class="add-root pull-right" ng-click="showServiceGroupView()">SG+</a>
        <input type="checkbox" id="show-sg-root" class="add-root" ng-click="showRoots('SERVICE_GROUP')"></a>

        <a href="#" id="scale-up" class="add-root pull-right" ng-click="scaleUp()">+</a>
        <a href="#" id="scale-down" class="add-root pull-right" ng-click="scaleDown()">-</a>
    </div>

    <div id="canvas-container">
        <div id="canvas-inner"></div>
        <div id="loading-container" ng-show="loadingMessage">Loading <span>{{loadingMessage}}</span></div>
    </div>
</div>
