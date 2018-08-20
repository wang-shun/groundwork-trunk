<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<portlet:defineObjects />
<portlet:resourceURL var="readPrefs" id="readPrefs" escapeXml="false" />

<div class="panel panel-default" ng-controller="TopController" gwp-fit-grid ng-init="init('<%=renderResponse.encodeURL(readPrefs.toString())%>')">
    <div class="panel-body panel-grid no-padding">
        <div id="grid" ng-grid="options"></div>
    </div>
</div>
