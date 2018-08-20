<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.*"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Groundwork Cloud Hub for RHEV-M</title>
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
		postTestConnection(form, action, "RHEV");
	}
	function profileForm(form, action) {
		successFormAction(form, action);
	}
</script>
</head>
	<form:form id="createRhevConnectionForm" commandName="configBean" method="POST">
<body id="dt_example">
		<div id="container">
			<!-- THIS is not in use at this level.  Standalone support is tomcat-only -->
			<!-- <p align="center">
				<img align="top" src="images/logo.png">
			</p> -->
			<p align="center" class="agent_title">Cloud Hub Configuration Wizard for RHEV-M</p>

            <div id="testConWaitMsg" class='message' style="display:none">
                <p><img src="/cloudhub/resources/images/loader.gif"/>Testing connection to Groundwork and RHEV-M servers!</p>
            </div>
            <div id='testConResultMsg'>
                <c:if test="${result == 'rheverror'}">
                    <div id="messages" class='redMessage'>
                        <p>RHEV-M server connection failed!</p>
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
                        <p class='message'>RHEV-M server connection saved successfully. (Remember to test connection to ensure connection validity)</p>
                    </div>
                </c:if>
                <c:if test="${result == 'savefailure'}">
                    <div id="messages" class='redMessage'>
                        <p class='message'>Sorry some problem occurred in saving RHEV-M server connection.!</p>
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
				<div class="controlheader">RHEV-M / Groundwork Connection. You need to have a valid connection to proceed.
                </div>
				<div class="controlcontent">

					<table cellpadding="0" cellspacing="0" border="0" class="display" id="example">
                        <!--
                        <thead>
                            <tr>
                                <th align="left"></th>
                                <th align="left"></th>
                                <th align="left"></th>
                                <th align="left"></th>
                            </tr>
                        </thead>
                        -->

						<c:if test="${message != null}">
							<tr>
								<td colspan="2" align="center"><label class="error">${message}</label></td>
							</tr>
						</c:if>
	
						<tr>
							<td class="group1"><i>GroundWork</i> Server Version :</td>
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
							<td class="group1"><form:input type="text" class="text required" name="groundwork.common.displayName"
								maxlength="40" path="common.displayName" /></td>
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
							<td class="group1">Is SSL enabled on <i>GroundWork</i> Server?:</td>
							<td class="group1"><form:checkbox path="gwos.gwosSSLEnabled" id="gwosSSLEnabled.sslEnabled" name="gwos.gwosSSLEnabled"/></td>
							<td class="group1"></td>
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
							<td class="group2">Is SSL enabled on RHEV-M Server?:</td>
							<td class="group2"><form:checkbox path="connection.sslEnabled" id="rhevSslEnabled" name="rhevSslEnabled"/></td>
							<td class="group2"><font color='red'><form:errors path='connection.sslEnabled' /></font></td>
                            <td class="group2"></td>
						</tr>

						<tr>
							<td class="group2">RHEV-M Server:</td>
							<td class="group2"><form:input type="text" class="text required"
								id="virtualEnv.serverName" name="virtualEnv.serverName"
								maxlength="256" path="connection.server" /></td>
							<td class="group2"><font color='red'><form:errors path='connection.server' /></font></td>
                            <td class="group2">RHEV-M-server.yourdomain.com</td>
						</tr>

						<tr>
							<td class="group2">RHEV-M entry-point:</td>
							<td class="group2"><form:input type="text" class="text required"
								id="virtualEnv.uri" name="virtualEnv.uri" maxlength="256"
								path="connection.url" />
							</td>
							<td class="group2"><font color='red'><form:errors path='connection.url' /></font></td>
                            <c:if test="${'rhev' eq 'rhev'}"><td class="group2">api</td></c:if>
                            <c:if test="${'rhev' eq 'vmware'}"><td class="group2">sdk</td></c:if>
						</tr>

						<tr>
							<td class="group2">RHEV-M Login Name:</td>
							<td class="group2"><form:input type="text" class="text required"
								id="username" name="username" maxlength="30"
								path="connection.username" /></td>
							<td class="group2"><font color='red'><form:errors path='connection.username' /></font></td>
                            <td class="group2">admin</td>
						</tr>

						<tr>
							<td class="group2">RHEV-M Password:</td>
							<td class="group2"><form:input type="password" class="text required"
								id="password" name="password" maxlength="128"
								path="connection.password" /></td>
							<td class="group2"><font color='red'><form:errors path='connection.password' /></font></td>
                            <td class="group2">&#8226;&#8226;&#8226;&#8226;&#8226;&#8226;</td>
						</tr>

						<%--tr>
							<td>RHEV-M Type:</td>
							<td><input type="text" class="text required"
								id="virtualEnv.type" name="virtualEnv.type" size="6"
								value="${configBean.virtualEnvType}" disabled/></td>
                            <td>rhev</td>
						</tr--%>

						<tr>
							<td class="group2">RHEV-M Realm:</td>
							
							<td class="group2"><form:input type="text" class="text required"
								id="virtualEnv.realm" name="virtualEnv.realm" maxlength="30"
								path="connection.realm"/></td>
							<td class="group2"><font color='red'><form:errors path='connection.realm' /></font></td>
                            <td class="group2">internal</td>
						</tr>

						<tr>
							<td class="group2">Certificate Store Path:</td>
							<td class="group2"><form:input type="text" class="text required"
								id="certificate.store" name="certificate.store" maxlength="256"
								path="connection.certificateStore"/></td>
							<td class="group2"><font color='red'><form:errors path='connection.certificateStore' /></font></td>
                            <td class="group2">/usr/java/latest/jre/lib/security/cacerts</td>
						</tr>

						<tr>
							<td class="group2">Certificate Store Passcode:</td>
							<td class="group2"><form:input type="password" class="text required"
								id="certificate.password" name="certificate.password" maxlength="30"
								path="connection.certificatePassword"/></td>
							<td class="group2"><font color='red'><form:errors path='connection.certificatePassword' /></font></td>
                            <td class="group2">&#8226;&#8226;&#8226;&#8226;&#8226;&#8226;&#8226;</td>
						</tr>

						<tr>
							<td class="group2">Check Interval (in mins):</td>
							<td class="group2"><form:input type="text" class="text required number"
								id="check.interval" name="check.interval" maxlength="9"
								path="common.uiCheckIntervalMinutes" /></td>
							<td class="group2"><font color='red'><form:errors path='common.uiCheckIntervalMinutes' /></font></td>
                            <td class="group2">5</td>
						</tr>

						<tr>
							<td class="group2">Sync Interval (in mins):</td>
							<td class="group2"><form:input type="text" class="text required number"
								id="sync.interval" name="sync.interval" maxlength="4"
								path="common.uiSyncIntervalMinutes" /></td>
							<td class="group2"><font color='red'><form:errors path='common.uiSyncIntervalMinutes' /></font></td>
                            <td class="group2">2</td>
						</tr>

                        <tr>
                            <td class="group2">Connection Retries (-1 infinite) :</td>
                            <td class="group2"><form:input type="text" class="text required number"
                                                           id="connectionRetries" name="connectionRetries" maxlength="3"
                                                           path="common.uiConnectionRetries" /></td>
                            <td class="group2"><font color='red'><form:errors path='common.uiConnectionRetries' /></font></td>
                            <td class="group2"></td>
                        </tr>

                        <tr>
                            <td class="group1" colspan="4">
                                <table cellpadding="0" cellspacing="0" border="0" class="display">
                                    <tr>
                                        <td class="group1">
                                            <form:checkbox disabled='true' path="common.hypervisorView" id="hypervisorView" name="hypervisorView" label=" Hypervisor View" />
                                            <br />
                                            <font color='red'><form:errors path='common.hypervisorView' /></font>
                                        </td>
                                        <td class="group1">
                                            <form:checkbox path="common.storageView" id="storageView" name="storageView" label=" Storage View"/>
                                            <br />
                                            <font color='red'><form:errors path='common.storageView' /></font>
                                        </td>
                                        <td class="group1">
                                            <form:checkbox path="common.networkView" id="networkView" name="networkView" label=" Network View"/>
                                            <br />
                                            <font color='red'><form:errors path='common.networkView' /></font>
                                        </td>
                                        <td class="group1">
                                            <form:checkbox path="common.resourcePoolView" id="resourcePoolView" name="resourcePoolView" label=" Resource Pool View"/>
                                            <br />
                                            <font color='red'><form:errors path='common.resourcePoolView' /></font>
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>

					</table>
                    <br />
					<p align="center">
						<input type="button" value="Home" onclick="location.href='/cloudhub/mvc/home/listAllConfigurations'" class="button-l" />
                        <c:if test="${configBean.common.testConnectionDisabled == false}">
                            <input id="testConnectionBtn" value="Test Connection" class="button-l" type="button" onclick="testForm('createRhevConnectionForm', '/cloudhub/mvc/rhev/testConnection')"/>
                        </c:if>
                        <c:if test="${configBean.common.testConnectionDisabled == true}">
                            <input id="testConnectionBtn" alt="This is a valid connection." name="test" value="Test Connection" class="button-l" type="button" disabled="disabled"/>
                        </c:if>

						<input name="save" id='saveButton' value="Save" class="button-l" type="button" onclick="saveForm('createRhevConnectionForm', '/cloudhub/mvc/rhev/saveConnectionConfiguration')"/>

						<c:if test="${configBean.common.createProfileDisabled == false}">
							<input name="next" value="Next" class="button-l" type="button" onclick="profileForm('createRhevConnectionForm', '/cloudhub/mvc/rhev/navigateToProfile')"/>
						</c:if>
						<c:if test="${configBean.common.createProfileDisabled == true}">
							<input alt="You need to have a valid connection to proceed to the next page." name="next" value="Next" class="button-l" type="button" disabled="disabled"
								   onclick="profileForm('createRhevConnectionForm', '/cloudhub/mvc/rhev/navigateToProfile')" />
						</c:if>
						
					</p>
					
					<form:hidden path="common.configurationFile" />
					<form:hidden path="common.pathToConfigurationFile" />
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
