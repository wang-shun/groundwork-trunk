<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<HTML>
<HEAD>
    <title>Groundwork Net Hub</title>
    <link type="text/css" href="/cloudhub/resources/css/jquery.dataTables.css" rel="Stylesheet" />
    <script type="text/javascript" src="/cloudhub/resources/js/java-agent.js"></script>
    <script type="text/javascript" src="/cloudhub/resources/js/jquery.js"></script>
    <script type="text/javascript" src="/cloudhub/resources/js/jquery.validate.min.js"></script>
    <script type="text/javascript" src="/cloudhub/resources/js/datatable/jquery.dataTables.js"></script>
    <script type="text/javascript" src="/cloudhub/resources/js/index.js"></script>

    <link type="text/css" href="/cloudhub/resources/css/java-agent.css" rel="Stylesheet" />
</HEAD>
<BODY id="dt_example">
<form:form method="post">
    <div id="container">
        <p align="center" class="agent_title">Net Hub Configuration Wizard</p>
        <div class="sidebox">
            <div class="boxhead">
                <h2>Configuration Wizard</h2>
            </div>
            <div class="boxbody">
                <div id="result" class="message"> ${result} </div>
                <p>Welcome to Net Hub configuration wizard.
                    This wizard helps you to configure the Cloud Hub.
                    Please have your GroundWork Server
                    and Virtual Environment connection parameters handy.
                </p>

                <div id="demo">
                    <table cellpadding="0" cellspacing="0" border="0" class="display" id="example" width="100%">
                        <thead>
                        <tr>
                            <th>Display Name</th>
                            <th>Type</th>
                            <th>Status</th>
                            <th>Modify</th>
                            <th>Delete</th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:forEach var="configuration" items="${configurations}" varStatus="loop">
                            <tr class="odd gradeX">
                                <td>${configuration.common.displayName}</td>
                                <td class="center">${configuration.common.virtualSystem}</td>
                                <td class="serverStatusCol">
                                    <c:if test="${configuration.common.serverSuspended == false && configuration.monitorExceptionCount > 0}">
                                        <div id="startStopServerImg" class="yellowcircle"> </div>
                                        &nbsp;
                                        <input id="startStopServerbtn" type="button" value="Stop" onclick="toggle(this, '<c:out value="${configuration.common.configurationFile}"/>', '<c:out value="${configuration.common.pathToConfigurationFile}"/>')" class="button"/>
                                    </c:if>
                                    <c:if test="${configuration.common.serverSuspended == false && configuration.monitorExceptionCount == 0}">
                                        <div id="startStopServerImg" class="greencircle"> </div>
                                        &nbsp;
                                        <input id="startStopServerbtn" type="button" value="Stop" onclick="toggle(this, '<c:out value="${configuration.common.configurationFile}"/>', '<c:out value="${configuration.common.pathToConfigurationFile}"/>')" class="button"/>
                                    </c:if>
                                    <c:if test="${configuration.common.serverSuspended == true}">
                                        <div id="startStopServerImg" class="redcircle"> </div>
                                        &nbsp;
                                        <input id="startStopServerbtn" type="button" value="Start" onclick="toggle(this, '<c:out value="${configuration.common.configurationFile}"/>', '<c:out value="${configuration.common.pathToConfigurationFile}"/>')" class="button"/>
                                    </c:if>
                                </td>
                                <td class="center">
                                    <c:url value="/mvc/updateConfiguration" var="updateConfigURL">
                                        <c:param name="filePath" value="${configuration.common.pathToConfigurationFile}"/>
                                        <c:param name="fileName" value="${configuration.common.configurationFile}"/>
                                    </c:url>

                                    <input type="button" value="  Modify  " onclick="location.href='<c:out value="${updateConfigURL}"/>'" class="button" />
                                </td>
                                <td class="center">
                                    <c:url value="/mvc/deleteConfiguration" var="deleteConfigURL">
                                        <c:param name="filePath" value="${configuration.common.pathToConfigurationFile}"/>
                                        <c:param name="fileName" value="${configuration.common.configurationFile}"/>
                                    </c:url>
                                    <input type="button" value="  Delete  " onclick="return confirmDelete(this);" class="button"/>
                                    <input id="deleteHiddenBtn" type="button" value="  Delete  " onclick="location.href='<c:out value="${deleteConfigURL}"/>'" class="button" style="display:none"/>
                                </td>
                            </tr>
                        </c:forEach>
                        </tbody>
                        <tfoot>
                        <tr>
                            <th>Display Name</th>
                            <th>Type</th>
                            <th>Status</th>
                            <th>Modify</th>
                            <th>Delete</th>
                        </tr>
                        </tfoot>
                    </table>
                </div>
                <br/>
            </div>
        </div>
        <input type="hidden" name="configObgIndex" value="" />
    </div>
</form:form>
<section class="alt-bg">
    <div class="add-connection">
        <h3>Add a Connection</h3>
        <div class="left_1-3">
            <table border="0" cellpadding="0" cellspacing="0" >
                <tbody>
                <tr>
                    <td class="col-1"><div class="logo-holder"><img class="logo" src="/cloudhub/resources/images/opendaylight.png" alt=""></div>
                        Open Daylight </td>
                    <td class="col-2"><a class="button-l" href="/cloudhub/mvc/opendaylight/navigateCreateConnection">+ Add</a></td>
                </tr>
                </tbody>
            </table>
        </div>
        <%--<div class="left_1-3">--%>
            <%--<table border="0" cellpadding="0" cellspacing="0" >--%>
                <%--<tbody>--%>
                <%--<tr>--%>
                    <%--<td class="col-1"><div class="logo-holder"><img class="logo" src="/cloudhub/resources/images/cisco.png" alt=""></div>--%>
                        <%--CISCO ACI </td>--%>
                    <%--<td class="col-2"><a class="button-l" href="/cloudhub/mvc/cisco/navigateCreateConnection">+ Add</a></td>--%>
                <%--</tr>--%>
                <%--</tbody>--%>
            <%--</table>--%>
        <%--</div>--%>
        <%--<div class="left_1-3">--%>
            <%--<table border="0" cellpadding="0" cellspacing="0" >--%>
                <%--<tbody>--%>
                <%--<tr>--%>
                    <%--<td class="col-1"><div class="logo-holder"><img class="logo" src="/cloudhub/resources/images/vmware.png" alt=""></div>--%>
                        <%--VMware NSX </td>--%>
                    <%--<td class="col-2"><a class="button-l" href="/cloudhub/mvc/nsx/navigateCreateConnection">+ Add</a></td>--%>
                <%--</tr>--%>
                <%--</tbody>--%>
            <%--</table>--%>
        <%--</div>--%>
    </div>
</section>
</BODY>
</HTML>
