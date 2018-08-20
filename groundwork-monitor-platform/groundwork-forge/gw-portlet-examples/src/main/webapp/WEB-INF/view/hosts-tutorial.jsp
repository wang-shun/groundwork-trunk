<%@ page contentType="text/html" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>

<style>
    #hostsTable {
        font-family: "Trebuchet MS", Arial, Helvetica, sans-serif;
        width: 100%;
        border-collapse: collapse;
    }

    #hostsTable td, #hostsTable th {
        font-size: 1em;
        border: 1px solid #98bf21;
        padding: 3px 7px 2px 7px;
    }

    #hostsTable th {
        font-size: 1.1em;
        text-align: left;
        padding-top: 5px;
        padding-bottom: 4px;
        background-color: #A7C942;
        color: #ffffff;
    }

    #hostsTable tr.alt td {
        color: #000000;
        background-color: #EAF2D3;
    }
</style>

<portlet:defineObjects/>

<table id="hostsTable">
    <tr>
        <th>Host Name</th>
        <th>Monitor Status</th>
        <th>App Type</th>
        <th>Last Plugin Output</th>
    </tr>
    <c:forEach items="${hosts}" var="host"  varStatus="loopStatus">
        <tr class="${loopStatus.index % 2 == 0 ? '' : 'alt'}">
            <td>
                <a href='<portlet:actionURL><portlet:param name='hostName' value='${host.hostName}'/></portlet:actionURL>'>${host.hostName}</a>
            </td>
            <td>
                    ${host.monitorStatus}
            </td>
            <td>
                    ${host.appType}
            </td>
            <td>
                    ${host.getProperty("LastPluginOutput")}
            </td>
        </tr>
    </c:forEach>
</table>

