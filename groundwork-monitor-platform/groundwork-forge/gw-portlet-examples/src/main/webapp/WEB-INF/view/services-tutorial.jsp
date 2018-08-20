<%@ page contentType="text/html" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>

<style>
    #servicesTable {
        font-family: "Trebuchet MS", Arial, Helvetica, sans-serif;
        width: 100%;
        border-collapse: collapse;
    }

    #servicesTable td, #servicesTable th {
        font-size: 1em;
        border: 1px solid #98bf21;
        padding: 3px 7px 2px 7px;
    }

    #servicesTable th {
        font-size: 1.1em;
        text-align: left;
        padding-top: 5px;
        padding-bottom: 4px;
        background-color: #A7C942;
        color: #ffffff;
    }

    #servicesTable tr.alt td {
        color: #000000;
        background-color: #EAF2D3;
    }

    #servicesInfo {
        margin-left: 5px;
        font-size: 2em;
    }

    #returnToList {
        margin-left: 5px;
        font-size: 1.25em;
        text-decoration: underline;
    }

</style>

<portlet:defineObjects/>

<br/>
<div id="servicesInfo"> Services for Host: ${hostName}</div>
<div id="returnToList"><a href="<portlet:renderURL/>"> Return to List Hosts</a></div>
<br/>

<table id="servicesTable">
    <tr>
        <th>Service Name</th>
        <th>Service Status</th>
        <th>Last Check Time</th>
        <th>Last Service Output</th>
    </tr>
    <c:forEach items="${services}" var="service"  varStatus="loopStatus">
        <tr class="${loopStatus.index % 2 == 0 ? '' : 'alt'}">
            <td>
                    ${service.description}
            </td>
            <td>
                    ${service.monitorStatus}
            </td>
            <td>
                    ${service.lastCheckTime}
            </td>
            <td>
                    ${service.getProperty("LastPluginOutput")}
            </td>
        </tr>
    </c:forEach>
</table>

