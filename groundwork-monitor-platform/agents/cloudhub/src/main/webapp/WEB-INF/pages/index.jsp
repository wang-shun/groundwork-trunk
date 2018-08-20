<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<HTML>
<HEAD>
    <title>Groundwork Cloud Hub</title>
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
        <p align="center" class="agent_title">Cloud Hub Configuration Wizard</p>
        <div class="sidebox">
            <div class="boxhead">
                <h2>Configuration Wizard</h2>
            </div>
            <div class="boxbody">
                <div id="result" class="message"> ${result} </div>
                <p>Welcome to Cloud Hub configuration wizard.
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
                            <th>Details</th>
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
                                    <c:choose>
                                    <c:when test='${configuration.common.virtualSystem.name() == "CLOUDERA"}'>
                                        <c:url value="/app/#/configuration/cloudera" var="updateConfigURL">
                                            <c:param name="name" value="${configuration.common.configurationFile}"/>
                                        </c:url>
                                    </c:when>
                                    <c:when test='${configuration.common.virtualSystem.name() == "AZURE"}'>
                                        <c:url value="/app/#/configuration/azure" var="updateConfigURL">
                                            <c:param name="name" value="${configuration.common.configurationFile}"/>
                                        </c:url>
                                    </c:when>
                                    <c:when test='${configuration.common.virtualSystem.name() == "NEDI"}'>
                                        <c:url value="/app/#/configuration/nedi" var="updateConfigURL">
                                            <c:param name="name" value="${configuration.common.configurationFile}"/>
                                        </c:url>
                                    </c:when>
                                    <c:when test='${configuration.common.virtualSystem.name() == "VMWARE"}'>
                                        <c:url value="/app/#/configuration/vmware" var="updateConfigURL">
                                            <c:param name="name" value="${configuration.common.configurationFile}"/>
                                        </c:url>
                                    </c:when>
                                    <c:when test='${configuration.common.virtualSystem.name() == "DOCKER"}'>
                                        <c:url value="/app/#/configuration/docker" var="updateConfigURL">
                                            <c:param name="name" value="${configuration.common.configurationFile}"/>
                                        </c:url>
                                    </c:when>
                                    <c:when test='${configuration.common.virtualSystem.name() == "AMAZON"}'>
                                        <c:url value="/app/#/configuration/aws" var="updateConfigURL">
                                            <c:param name="name" value="${configuration.common.configurationFile}"/>
                                        </c:url>
                                    </c:when>
                                    <c:otherwise>
                                        <c:url value="/mvc/updateConfiguration" var="updateConfigURL">
                                            <c:param name="filePath" value="${configuration.common.pathToConfigurationFile}"/>
                                            <c:param name="fileName" value="${configuration.common.configurationFile}"/>
                                        </c:url>
                                    </c:otherwise>
                                    </c:choose>
                                    <input type="button" value="  Modify  " onclick="location.href='<c:out value="${updateConfigURL}"/>'" class="button" />
                                </td>
                                <td class="center">
                                    <c:url value="/app/#/status" var="statusURL">
                                        <c:param name="name" value="${configuration.common.configurationFile}"/>
                                    </c:url>
                                    <input type="button" value="  Status  " onclick="location.href='<c:out value="${statusURL}"/>'" class="button" />
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
              <td class="col-1"><div class="logo-holder"><img class="logo" src="/cloudhub/resources/images/docker.png" alt=""></div>
                  Docker </td>
              <td class="col-2"><a class="button-l" href="/cloudhub/app/#/configuration/docker">+ Add</a></td>
          </tr>
        </tbody>
      </table>
    </div>
    <%--<div class="left_1-3">--%>
      <%--<table border="0" cellpadding="0" cellspacing="0" >--%>
        <%--<tbody>--%>
          <%--<tr>--%>
              <%--<td class="col-1"><div class="logo-holder"><img class="logo" src="/cloudhub/resources/images/openshift.png" alt=""></div>--%>
                  <%--OpenShift </td>--%>
              <%--<td class="col-2"><a class="button-l" href="/cloudhub/mvc/openshift/navigateCreateConnection">+ Add</a></td>--%>
          <%--</tr>--%>
        <%--</tbody>--%>
      <%--</table>--%>
    <%--</div>--%>
    <div class="left_1-3">
      <table border="0" cellpadding="0" cellspacing="0" >
        <tbody>
          <tr>
              <td class="col-1"><div class="logo-holder"><img class="logo" src="/cloudhub/resources/images/openstack.png" alt=""></div>
                  OpenStack </td>
              <td class="col-2"><a class="button-l" href="/cloudhub/mvc/openstack/navigateCreateConnection">+ Add</a></td>
          </tr>
        </tbody>
      </table>
    </div>
    <div class="left_1-3">
      <table border="0" cellpadding="0" cellspacing="0" >
        <tbody>
          <tr>
              <td class="col-1"><div class="logo-holder"><img class="logo" src="/cloudhub/resources/images/redhat.png" alt=""></div>
                  Red Hat Enterprise<br>
                  Virtualization</td>
              <td class="col-2"><a class="button-l" href="/cloudhub/mvc/rhev/navigateCreateConnection">+ Add</a></td>
          </tr>
        </tbody>
      </table>
    </div>
    <div class="left_1-3">
      <table border="0" cellpadding="0" cellspacing="0" >
        <tbody>
          <tr>
              <td class="col-1"><div class="logo-holder"><img class="logo" src="/cloudhub/resources/images/vmware.png" alt=""></div>
                  VMware vSphere </td>
              <%--<td class="col-2"><a class="button-l" href="/cloudhub/mvc/vmware2/navigateCreateConnection">+ Add</a></td>--%>
              <td class="col-2"><a class="button-l" href="/cloudhub/app/#/configuration/vmware">+ Add</a></td>
          </tr>
        </tbody>
      </table>
    </div>
      <div class="left_1-3">
          <table border="0" cellpadding="0" cellspacing="0" >
              <tbody>
              <tr>
                  <td class="col-1"><div class="logo-holder"><img class="logo" src="/cloudhub/resources/images/amazon-aws.png" alt=""></div>
                      Amazon AWS </td>
                  <td class="col-2"><a class="button-l" href="/cloudhub/app/#/configuration/aws">+ Add</a></td>
              </tr>
              </tbody>
          </table>
      </div>
      <div class="left_1-3">
          <table border="0" cellpadding="0" cellspacing="0" >
              <tbody>
              <tr>
                  <td class="col-1"><div class="logo-holder"><img class="logo" src="/cloudhub/resources/images/netapp.png" alt=""></div>
                      NetApp </td>
                  <td class="col-2"><a class="button-l" href="/cloudhub/mvc/netapp/navigateCreateConnection">+ Add</a></td>
              </tr>
              </tbody>
          </table>
      </div>
      <div class="left_1-3">
          <table border="0" cellpadding="0" cellspacing="0" >
              <tbody>
              <tr>
                  <td class="col-1"><div class="logo-holder"><img class="logo" src="/cloudhub/resources/images/cloudera.png" alt=""></div>
                      Cloudera </td>
                  <td class="col-2"><a class="button-l" href="/cloudhub/app/#/configuration/cloudera">+ Add</a></td>
              </tr>
              </tbody>
          </table>
      </div>
      <div class="left_1-3">
          <table border="0" cellpadding="0" cellspacing="0" >
              <tbody>
              <tr>
                  <td class="col-1"><div class="logo-holder"><img class="logo" src="/cloudhub/resources/images/azure.png" alt=""></div>
                      Azure </td>
                  <td class="col-2"><a class="button-l" href="/cloudhub/app/#/configuration/azure">+ Add</a></td>
              </tr>
              </tbody>
          </table>
      </div>
      <div class="left_1-3">
          <table border="0" cellpadding="0" cellspacing="0" >
              <tbody>
              <tr>
                  <td class="col-1"><div class="logo-holder"><img class="logo" src="/cloudhub/resources/images/nedi-dgray-320.jpg" alt=""></div>
                      NeDi </td>
                  <td class="col-2"><a class="button-l" href="/cloudhub/app/#/configuration/nedi">+ Add</a></td>
              </tr>
              </tbody>
          </table>
      </div>
      <div class="left_1-3">
          <table border="0" cellpadding="0" cellspacing="0" >
              <tbody>
              <tr>
                  <td class="col-1"><div class="logo-holder"><img class="logo" src="/cloudhub/resources/images/icinga.png" alt=""></div>
                      Icinga 2 Monitoring</td>
                  <td class="col-2"><a class="button-l" href="/cloudhub/mvc/icinga2/navigateCreateConnection">+ Add</a></td>
              </tr>
              </tbody>
          </table>
      </div>
      <c:if test="${loadTestConnectorVisible == true}">
          <div class="left_1-3">
              <table border="0" cellpadding="0" cellspacing="0" >
                  <tbody>
                  <tr>
                      <td class="col-1"><div class="logo-holder"><img class="logo" src="/cloudhub/resources/images/cloudhub.png" alt=""></div>
                          CloudHub Load Test </td>
                      <td class="col-2"><a class="button-l" href="/cloudhub/mvc/loadtest/navigateCreateConnection">+ Add</a></td>
                  </tr>
                  </tbody>
              </table>
          </div>
      </c:if>
  </div>
</section>
</BODY>
</HTML>
