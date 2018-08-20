<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ page import="java.util.*"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Groundwork Cloud Hub for Icinga 2</title>
<script type="text/javascript" src="/cloudhub/resources/js/java-agent.js"></script>
<link type="text/css" href="/cloudhub/resources/css/java-agent.css" rel="Stylesheet" />
<style type="text/css" title="currentStyle">
@import "/cloudhub/resources/css/jdma_page.css";
@import "/cloudhub/resources/css/jdma_table.css";
</style>
<script type="text/javascript" src="/cloudhub/resources/js/jquery.js"></script>
<script type="text/javascript" src="/cloudhub/resources/js/jquery.validate.min.js"></script>
<script type="text/javascript" src="/cloudhub/resources/js/common.js"></script>
<script>
	var formModified=false;
	$(document).ready(function() {
		$("form").bind('change', function(){
			formModified = true;
		});
		$(window).bind('beforeunload', function(e){
			if (formModified) {
				$("form :input").attr( "disabled", false );
				$("body").css("cursor", "default");
				return 'Modifications have been made to the form without saving.';
			}
			return;
		});
		$("#connForm").validate();
	});
	function saveForm(form, action) {
		formModified = false;
		setFormAction(form, action, successFormAction);
	}
	function testForm(form, action) {
		postTestConnection(form, action, "Icinga2");
	}
	function profileForm(form, action) {
		successFormAction(form, action);
	}
</script>
</head>
<body id="dt_example">
	<form:form id="createIcinga2ConnectionForm" commandName="configBean" method="POST" action="/cloudhub/mvc/icinga2/saveConnectionConfiguration">
		<div id="container">
			<!-- <p align="center">
				<img align="top" src="images/logo.png">
			</p> -->
			<p align="center" class="agent_title">Cloud Hub Configuration Wizard for Icinga 2</p>

            <div id="testConWaitMsg" class='message' style="display:none">
                <p><img src="/cloudhub/resources/images/loader.gif"/>Testing connection to Groundwork and Icinga 2 servers!</p>
            </div>
            <div id='testConResultMsg'>
                <c:if test="${result == 'icinga2error'}">
                    <div id="messages" class='redMessage'>
                        <p>Icinga 2 server connection failed!</p>
                        <p>${errorMessage}</p>
                    </div>
                </c:if>
                <c:if test="${result == 'gwoserror'}">
                    <div id="messages" class='redMessage'>
                        <p>GWOS connection failed!</p>
                        <p>${errorMessage}</p>
                    </div>
                </c:if>
                <c:if test="${result == 'success'}">
                    <div id="messages" class='message'>
                        <p class='message'>Connection successful!</p>
                    </div>
                </c:if>
                <c:if test="${result == 'savesuccess'}">
                    <div id="messages" class='message'>
                        <p class='message'>Icinga 2 server connection saved successfully. (Remember to test connection to ensure connection validity)</p>
                    </div>
                </c:if>
                <c:if test="${result == 'savefailure'}">
                    <div id="messages" class='redMessage'>
                        <p class='message'>Sorry some problem occurred in saving Icinga 2 server connection.!</p>
                        <p>${errorMessage}</p>
                    </div>
                </c:if>
                <c:if test="${result == 'profilesDoesNotExists'}">
                    <div id="messages" class='redMessage'>
                        <p>Local Groundwork profile and remote Groundwork profile both could not be accessed. Please check if you have proper rights and credentials.</p>
                    </div>
                </c:if>
            </div>

			<div id="controlbg">
				<div class="controltop">
					<div class="cornerul"></div>
					<div class="cornerur"></div>
				</div>
				<div class="controlheader">Icinga 2 / Groundwork Connection
					You need to have a valid connection to proceed.</div>
				<div class="controlcontent">


					<table cellpadding="0" cellspacing="0" border="0" class="display"
						id="example">

						<tr>
							<td class="group1"><i>GroundWork</i> Server Version</td>
							<td class="group1">
								<c:if test="${configBean.common.canAccessMultipleVersions == true}">
									<form:select path="gwos.gwosVersion" >
										<form:option value="7.1" label="7.1"/>
										<form:option value="7.0" label="7.0"/>
									</form:select>
								</c:if>
								<c:if test="${configBean.common.canAccessMultipleVersions == false}">
									<form:select path="gwos.gwosVersion" disabled="true">
                                        <form:option value="7.1" label="7.1"/>
                                        <form:option value="7.0" label="7.0"/>
									</form:select>
									<form:hidden path="gwos.gwosVersion"/>
								</c:if>
							</td>
							<td class="group1"></td>
							<td class="group1"></td>
						</tr>
						<tr>
							<td class="group1">Display Name:</td>
							<td class="group1"><form:input type="text" class="text required"
								maxlength="40" path="common.displayName" /> </td>
							<td class="group1"><font color='red'><form:errors path='common.displayName' /></font></td>
							<td class="group1">Configuration Server Display Name</td>
						</tr>
						<tr>
							<td class="group1"><i>GroundWork</i> Server Name:</td>
							<td class="group1"><form:input type="text" class="text required"
								id="groundwork.server.name" name="groundwork.common.gwosServer"
								maxlength="256" path="gwos.gwosServer" /></td>
							<td class="group1"><font color='red'><form:errors path='gwos.gwosServer' /></font></td>
							<td class="group1">localhost</td>
						</tr>

						<c:if test="${configBean.gwos.gwosVersion=='6.7'}">
							<tr id="rowGwosPort">
								<td class="group1"><i>GroundWork</i> Server Port:</td>
								<td class="group1"><form:input type="text" class="text required number"
									id="groundwork.server.port" name="groundwork.server.port"
									maxlength="6" path="gwos.gwosPort" /></td>
								<td class="group1"><font color='red'><form:errors path='gwos.gwosPort' /></font></td>
								<td class="group1">4913</td>
							</tr>
						</c:if>

						<tr>
							<td class="group1">Is SSL enabled on <i>GroundWork</i> Server?</td>
							<td class="group1"><form:checkbox path="gwos.gwosSSLEnabled" id="isGwosSSLEnabled" name="gwosSSLEnabled"/></td>
							<td class="group1"><font color='red'><form:errors path='gwos.gwosSSLEnabled' /></font></td>
							<td class="group1"></td>
						</tr>

						<tr>
							<td class="group1"><i>GroundWork</i> WebServices Username:</td>
							<td class="group1"><form:input type="text" class="text required"
								id="groundwork.webservices.username"
								name="groundwork.webservices.username" maxlength="30"
								path="gwos.wsUsername" /></td>
							<td class="group1"><font color='red'><form:errors path='gwos.wsUsername' /></font></td>
                            <td class="group1">${defaultUsername}</td>
						</tr>
						<tr>
							<td class="group1"><i>GroundWork</i> WebServices Password:</td>
							<td class="group1"><form:input type="password" class="text required"
								id="groundwork.webservices.password"
								name="groundwork.webservices.password" maxlength="128"
								path="gwos.wsPassword" /></td>
							<td class="group1"><font color='red'><form:errors path='gwos.wsPassword' /></font></td>
							<td class="group1">&#8226;&#8226;&#8226;&#8226;&#8226;&#8226;</td>
						</tr>

                        <c:if test="${configBean.gwos.gwosVersion=='7.1'}">
                            <tr id="rowMergeHost">
                                <td class="group1">Merge hosts on <i>GroundWork</i> Server?</td>
                                <td class="group1"><form:checkbox path="gwos.mergeHosts" id="isMergeHosts" name="isMergeHosts"/></td>
                                <td class="group1"><font color='red'><form:errors path='gwos.mergeHosts' /></font></td>
                                <td class="group1"></td>
                            </tr>
                        </c:if>
                        <c:if test="${configBean.gwos.gwosVersion!='7.1'}">
                            <form:hidden path="gwos.mergeHosts"/>
                        </c:if>

                        <tr>
							<td class="spacer"></td>
							<td class="spacer"></td>
							<td class="spacer"></td>
							<td class="spacer"></td>
						</tr>

						<tr>
							<td class="group2"><i>Icinga 2</i> Server</td>
							<td class="group2">
                                <form:input type="text" class="text required"
                                            id="virtualEnv.server" name="virtualEnv.server" maxlength="256"
                                            path="connection.server"/></td>
							</td>
							<td class="group2"><font color='red'><form:errors path='connection.server' /></font></td>
							<td class="group2">icinga2.mydomain.com</td>
						</tr>
						<tr>
							<td class="group2"><i>Icinga 2</i> API Port</td>
							<td class="group2">
                                <form:input type="text" class="text required number"
                                            id="virtualEnv.port" name="virtualEnv.port" maxlength="4"
                                            path="connection.port"/></td>
							</td>
							<td class="group2"><font color='red'><form:errors path='connection.port' /></font></td>
							<td class="group2">5665</td>
						</tr>
						<tr>
							<td class="group2"><i>Icinga 2</i> API Username</td>
							<td class="group2">
                                <form:input type="text" class="text required"
                                            id="virtualEnv.username" name="virtualEnv.username" maxlength="30"
                                            path="connection.username"/></td>
							</td>
							<td class="group2"><font color='red'><form:errors path='connection.username' /></font></td>
							<td class="group2">root</td>
						</tr>
						<tr>
							<td class="group2"><i>Icinga 2</i> API Password</td>
							<td class="group2">
                                <form:input type="password" class="text required"
                                            id="virtualEnv.password" name="virtualEnv.password" maxlength="128"
                                            path="connection.password"/></td>
							</td>
							<td class="group2"><font color='red'><form:errors path='connection.password' /></font></td>
							<td class="group2">&#8226;&#8226;&#8226;&#8226;&#8226;&#8226;</td>
						</tr>
						<tr>
							<td class="group2">Trust SSL Certificate on <i>Icinga 2</i> Server?</td>
							<td class="group2"><form:checkbox path="connection.trustAllSSL" id="virtualEnv.trustAllSSL"
                                                name="virtualEnv.trustAllSSL"/></td>
							<td class="group2"><font color='red'><form:errors path='connection.trustAllSSL' /></font></td>
							<td class="group2"></td>
						</tr>
						<tr>
							<td class="group2"><i>Icinga 2</i> Server SSL CA Certificate</td>
							<td class="group2">
                                <form:input type="text" class="text required"
                                            id="virtualEnv.trustSSLCACertificate" name="virtualEnv.trustSSLCACertificate"
                                            maxlength="256" path="connection.trustSSLCACertificate"/></td>
							</td>
							<td class="group2"><font color='red'><form:errors path='connection.trustSSLCACertificate' /></font></td>
							<td class="group2">/usr/local/groundwork/config/cloudhub/icinga2/ca.crt</td>
						</tr>
						<tr>
							<td class="group2"><i>Icinga 2</i> Server SSL CA Certificate Keystore</td>
							<td class="group2">
                                <form:input type="text" class="text required"
                                            id="virtualEnv.trustSSLCACertificateKeystore" name="virtualEnv.trustSSLCACertificateKeystore"
                                            maxlength="256" path="connection.trustSSLCACertificateKeystore"/></td>
							</td>
							<td class="group2"><font color='red'><form:errors path='connection.trustSSLCACertificateKeystore' /></font></td>
							<td class="group2">/usr/local/groundwork/config/cloudhub/icinga2/icinga2-keystore.jks</td>
						</tr>
						<tr>
							<td class="group2"><i>Icinga 2</i> Server SSL CA Certificate Keystore Password</td>
							<td class="group2">
                                <form:input type="password" class="text required"
                                            id="virtualEnv.trustSSLCACertificateKeystorePassword"
                                            name="virtualEnv.trustSSLCACertificateKeystorePassword" maxlength="128"
                                            path="connection.trustSSLCACertificateKeystorePassword"/></td>
							</td>
							<td class="group2"><font color='red'><form:errors path='connection.trustSSLCACertificateKeystorePassword' /></font></td>
							<td class="group2">&#8226;&#8226;&#8226;&#8226;&#8226;&#8226;</td>
						</tr>
						<tr>
							<td class="group2">Graph <i>Icinga 2</i> Service Metrics?</td>
							<td class="group2"><form:checkbox path="connection.metricsGraphed" id="virtualEnv.metricsGraphed"
                                                name="virtualEnv.metricsGraphed"/></td>
							<td class="group2"><font color='red'><form:errors path='connection.metricsGraphed' /></font></td>
							<td class="group2"></td>
						</tr>

						<tr>
							<td class="group2">Sync Interval (in mins):</td>
							<td class="group2"><form:input type="text" class="text required number"
								id="sync.interval" name="sync.interval" maxlength="4"
								path="common.uiSyncIntervalMinutes" /></td>
							<td class="group2"><font color='red'><form:errors path='common.uiSyncIntervalMinutes' /></font></td>
							<td class="group2"></td>
						</tr>

                        <tr>
                            <td class="group2">Connection Retries (-1 infinite):</td>
                            <td class="group2"><form:input type="text" class="text required number"
                                                           id="connectionRetries" name="connectionRetries" maxlength="3"
                                                           path="common.uiConnectionRetries" /></td>
                            <td class="group2"><font color='red'><form:errors path='common.uiConnectionRetries' /></font></td>
                            <td class="group2"></td>
                        </tr>
					</table>
                    <br />
					<p align="center">
						<input type="button" value="Home" onclick="location.href='/cloudhub/mvc/home/listAllConfigurations'" class="button-l" />
						<c:if test="${configBean.common.testConnectionDisabled == false}">
							<input id="testConnectionBtn" value="Test Connection" class="button-l" type="button" onclick="testForm('createIcinga2ConnectionForm', '/cloudhub/mvc/icinga2/testConnection')"/>
						</c:if>
						<c:if test="${configBean.common.testConnectionDisabled == true}">
							<input id="testConnectionBtn" alt="This is a valid connection." name="test" value="Test Connection" class="button-l" type="button" disabled="disabled"/>
						</c:if>
						
						<input name="save" id='saveButton' value="Save" class="button-l" type="button" onclick="saveForm('createIcinga2ConnectionForm', '/cloudhub/mvc/icinga2/saveConnectionConfiguration')"/>
					</p>
					<form:hidden path="common.configurationFile"/>
					<form:hidden path="common.pathToConfigurationFile"/>
					<form:hidden path="common.testConnectionDisabled" />
					<form:hidden path="common.createProfileDisabled" />
                    <form:hidden path="common.agentId" />
                    <form:hidden path="common.serverSuspended" />
                    <form:hidden path="common.canAccessMultipleVersions" />

                </div>
				<div class="controlbottom">
					<div class="cornerll"></div>
					<div class="cornerlr"></div>
				</div>
			</div>
		</div>
	</form:form>
</body>
</html>
