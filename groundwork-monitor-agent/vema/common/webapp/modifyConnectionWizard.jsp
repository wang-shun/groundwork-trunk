<%@page import="org.itgroundwork.foundation.joxbeans.Metric"%>
<%@page import="org.itgroundwork.foundation.joxbeans.VM"%>
<%@page import="org.itgroundwork.foundation.joxbeans.Hypervisor"%>
<%@page import="org.itgroundwork.foundation.joxbeans.VemaMonitoring"%>
<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@page import="java.util.*"%>
<%@page import="com.groundwork.agents.vema.configuration.*;"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>CloudHub Configuration wizard for @virt_target_label@</title>
<script type="text/javascript" src="js/java-agent.js"></script>
<script type="text/javascript" src="js/jquery.js"></script>
<script type="text/javascript" src="js/jquery.validate.min.js"></script>
<script>
	$(document).ready(function() {
		$("#modForm").validate();
	});
</script>

<link type="text/css" href="css/java-agent.css" rel="Stylesheet" />
<style type="text/css" title="currentStyle">
@import "css/jdma_page.css";

@import "css/jdma_table.css";
</style>
<script type="text/javascript">
function hypOnChangeCheckbox (checkbox) {
	var id = checkbox.id;
 	var index = id.substring(id.lastIndexOf("_")+1,id.length);
	document.getElementById('hyp_graphed_'+index).checked = 
		checkbox.checked ? 'checked' : false;
    }
}
function vmOnChangeCheckbox (checkbox) {
	var id = checkbox.id;
 	var index = id.substring(id.lastIndexOf("_")+1,id.length);
	document.getElementById('vm_graphed_'+index).checked = 
		checkbox.checked ? 'checked' : false;
    }
}
</script>
</head>
<body id="dt_example">
	<form method="POST" action="/@virt_agent_name@/@virt_target@Servlet"
		id="modForm">
		<div id="container">
			@virt_header@
			<p align="center" class="agent_title">CloudHub Configuration wizard	for @virt_target_label@</p>
			<div id="controlbg">
				<div class="controltop">
					<div class="cornerul"></div>
					<div class="cornerur"></div>
				</div>
				<div class="controlheader">Current @virt_target_label@ CloudHub Configuration</div>
				<div class="controlcontent">

					<jsp:useBean id="configBean"
						class="com.groundwork.agents.vema.configuration.VEMAGwosConfiguration"
						scope="session" />
					<jsp:setProperty name="configBean" property="*" />
					<jsp:setProperty name="configBean" property="virtualEnvType" value="@virt_target_lowercase@" />

					<table cellpadding="0" cellspacing="0" border="0" class="display"
						id="example" style="width: auto;">

                        <thead>
                            <tr>
                                <th align="left"></th>
                                <th align="left"></th>
                                <th align="left">Examples</th>
                            </tr>
                        </thead>

						<c:if test="${message != null}">
							<tr>
								<td colspan="2" align="center"><label class="error">${message}</label></td>
							</tr>
						</c:if>
	
						<tr>
							<td><i>GroundWork</i> Server Name:</td>
							<td><input type="text" class="text required"
								id="groundwork.server.name" name="groundwork.server.name"
								size="30" value="${configBean.gwosServer}" /></td>
                            <td>localhost</td>
						</tr>
						
						<tr>
							<td><i>GroundWork</i> Server Port:</td>
							<td><input type="text" class="text required number"
								id="groundwork.server.port" name="groundwork.server.port"
								size="6" value="${configBean.gwosPort}" /></td>
                            <td>4913</td>
						</tr>
						
						<tr>
							<td>Is SSL enabled on <i>GroundWork</i> Server?:</td>
							<td><input type="checkbox" id="groundwork.server.sslEnabled"
								name="groundwork.server.sslEnabled" /></td>
                            <td></td>
						</tr>
						
						<tr>
							<td><i>GroundWork</i> WebServices Endpoint:</td>
							<td><input type="text" class="text required"
								id="groundwork.webservices.endpoint"
								name="groundwork.webservices.endpoint" size="30"
								value="${configBean.wsEndpoint}" /></td>
                            <td>/foundation-webapp/services</td>
						</tr>
						
						<tr>
							<td><i>GroundWork</i> WebServices Username:</td>
							<td><input type="text" class="text required"
								id="groundwork.webservices.username"
								name="groundwork.webservices.username" size="12"
								value="${configBean.wsUser}" /></td>
                            <td>wsuser</td>
						</tr>
						
						<tr>
							<td><i>GroundWork</i> WebServices Password:</td>
							<td><input type="password" class="text required"
								id="groundwork.webservices.password"
								name="groundwork.webservices.password" size="12"
								value="${configBean.wsPassword}" /></td>
                            <td>&#8226;&#8226;&#8226;&#8226;&#8226;&#8226;</td>
						</tr>

						<tr>
							<td>Is SSL enabled on @virt_target_label@ Server?:</td>
							<c:if test="${configBean.virtualEnvSSLEnabled == true}">
								<td><input type="checkbox" id="virtualEnv.sslEnabled"
									name="virtualEnv.sslEnabled" checked="checked" /></td>
							</c:if>
							<c:if test="${configBean.virtualEnvSSLEnabled == false}">
								<td><input type="checkbox" id="virtualEnv.sslEnabled"
									name="virtualEnv.sslEnabled" /></td>
							</c:if>
                            <td></td>
						</tr>

						<tr>
							<td>@virt_target_label@ Server:</td>
							<td><input type="text" class="text required"
								id="virtualEnv.serverName" name="virtualEnv.serverName"
								size="30" value="${configBean.virtualEnvServer}" /></td>
                            <td>@virt_target_label@-server.yourdomain.com</td>
						</tr>

						<tr>
							<td>@virt_target_label@ entry-point:</td>
							<td><input type="text" class="text required"
								id="virtualEnv.uri" name="virtualEnv.uri" size="30"
								value="${configBean.virtualEnvURI}" />
							</td>
                            <c:if test="${'@virt_target_lowercase@' eq 'rhev'}"><td>api</td></c:if>
                            <c:if test="${'@virt_target_lowercase@' eq 'vmware'}"><td>sdk</td></c:if>
						</tr>

						<tr>
							<td>@virt_target_label@ Login Name:</td>
							<td><input type="text" class="text required"
								id="virtualEnv.username" name="virtualEnv.username" size="12"
								value="${configBean.virtualEnvUser}" /></td>
                            <td>admin</td>
						</tr>

						<tr>
							<td>@virt_target_label@ Password:</td>
							<td><input type="password" class="text required"
								id="virtualEnv.password" name="virtualEnv.password" size="12"
								value="${configBean.virtualEnvPassword}" /></td>
                            <td>&#8226;&#8226;&#8226;&#8226;&#8226;&#8226;</td>
						</tr>

						<tr>
							<td>@virt_target_label@ Type:</td>
							<td><input type="text" class="text required"
								id="virtualEnv.type" name="virtualEnv.type" size="6"
								value="${configBean.virtualEnvType}" disabled/></td>
                            <td>@virt_target_lowercase@</td>
						</tr>

						<c:if test="${'@virt_target_lowercase@' eq 'rhev'}">
						<tr>
							<td>@virt_target_label@ Realm:</td>
							<td><input type="text" class="text required"
								id="virtualEnv.realm" name="virtualEnv.realm" size="12"
								value="${configBean.virtualEnvRealm}"/></td>
                            <td>internal</td>
						</tr>
						</c:if>

						<c:if test="${'@virt_target_lowercase@' eq 'rhev'}">
						<tr>
							<td>Certificate Store Path:</td>
							<td><input type="text" class="text required"
								id="certificate.store" name="certificate.store" size="30"
								value="${configBean.certificateStore}"/></td>
                            <td>/usr/java/latest/jre/lib/security/cacerts</td>
						</tr>
						</c:if>

						<c:if test="${'@virt_target_lowercase@' eq 'rhev'}">
						<tr>
							<td>Certificate Store Passcode:</td>
							<td><input type="password" class="text required"
								id="certificate.password" name="certificate.password" size="12"
								value="${configBean.certificatePassword}"/></td>
                            <td>&#8226;&#8226;&#8226;&#8226;&#8226;&#8226;&#8226;</td>
						</tr>
						</c:if>

						<c:if test="${'@virt_target_lowercase@' eq 'rhev'}">
						<tr>
							<td>COMA Timeout (in mins):</td>
							<td><input type="text" class="text required number"
								id="coma.interval" name="coma.interval" size="3"
								value="${configBean.comaInterval}" /></td>
                            <td>15</td>
						</tr>
						</c:if>

						<tr>
							<td>Check Interval (in mins):</td>
							<td><input type="text" class="text required number"
								id="check.interval" name="check.interval" size="3"
								value="${configBean.checkInterval}" /></td>
                            <td>5</td>
						</tr>

						<tr>
							<td>Sync Interval (in mins):</td>
							<td><input type="text" class="text required number"
								id="sync.interval" name="sync.interval" size="3"
								value="${configBean.syncInterval}" /></td>
                            <td>2</td>
						</tr>

						<tr>
							<td colspan="2">Hypervisor Metrics</td>
						</tr>

						<tr>
							<td colspan="3">
								<table cellpadding="0" cellspacing="0" border="0">
									<thead>
										<tr>
											<th align="left">Metric Name</th>
											<th align="left">Is Monitored</th>
											<th align="left">Is Graphed</th>
											<th align="left">Warning Threshold</th>
											<th align="left">Critical Threshold</th>
										</tr>
									</thead>
									<c:forEach items="${vemaBean.hypervisor.metric}" var="metric"
										varStatus="rowCounter">
										<tr>
											<td>${metric.name} <input type="hidden" 
												name="hyp_alias"
												value="${metric.name}" /></td>
											<c:if test="${metric.monitored == true}">
												<td><input type="checkbox"
													id="hyp_monitored_${rowCounter.count}" 
													name="hyp_monitored"
													checked="checked" value="${metric.name}" 
                                                    onchange="hypOnChangeCheckbox(this)"/></td>
											</c:if>
											<c:if test="${metric.monitored == false}">
												<td><input type="checkbox"
													id="hyp_monitored_${rowCounter.count}" 
													name="hyp_monitored"
													value="${metric.name}" 
                                                    onchange="hypOnChangeCheckbox(this)"/></td>
											</c:if>
											<c:if test="${metric.graphed == false}">
												<td><input type="checkbox"
													id="hyp_graphed_${rowCounter.count}" 
													name="hyp_graphed"
													value="${metric.name}" /></td>
											</c:if>
											<c:if test="${metric.graphed == true}">
												<td><input type="checkbox"
													id="hyp_graphed_${rowCounter.count}" 
													name="hyp_graphed"
													checked="checked" value="${metric.name}" /></td>
											</c:if>
											<td><input type="text"
												id="hyp_warningThreshold_${rowCounter.count}"
												name="hyp_warningThreshold_${rowCounter.count}" 
												size="6"
												maxlength="10" class="text required number"
												value="${metric.warningThreshold}" /></td>
											<td><input type="text"
												id="hyp_criticalThreshold_${rowCounter.count}"
												name="hyp_criticalThreshold_${rowCounter.count}" 
												size="6"
												maxlength="10" class="text required number"
												value="${metric.criticalThreshold}" /></td>
										</tr>
									</c:forEach>

								</table>
							</td>
						</tr>
						<tr>
							<td colspan="2">VM Metrics</td>
						</tr>
						<tr>
							<td colspan="3">
								<table cellpadding="0" cellspacing="0" border="0">
									<thead>
										<tr>
											<th align="left">Metric Name</th>
											<th align="left">Is Monitored</th>
											<th align="left">Is Graphed</th>
											<th align="left">Warning Threshold</th>
											<th align="left">Critical Threshold</th>
										</tr>
									</thead>
									<c:forEach items="${vemaBean.vm.metric}" var="metric"
										varStatus="rowCounter">
										<tr>
											<td>${metric.name} <input type="hidden"
											    name="vm_alias"
												value="${metric.name}" />
											</td>
											
											<c:if test="${metric.monitored == true}">
												<td><input type="checkbox"
													id="vm_monitored_${rowCounter.count}"
													name="vm_monitored"
													checked="checked" value="${metric.name}" 
                                                    onchange="vmOnChangeCheckbox(this)"/></td>
											</c:if>
											
											<c:if test="${metric.monitored == false}">
												<td><input type="checkbox"
													id="vm_monitored_${rowCounter.count}" 
													name="vm_monitored"
                                                    value="${metric.name}" 
                                                    onchange="vmOnChangeCheckbox(this)"/></td>
											</c:if>
											
											<c:if test="${metric.graphed == false}">
												<td><input type="checkbox"
													id="vm_graphed_${rowCounter.count}" 
													name="vm_graphed"
													value="${metric.name}" /></td>
											</c:if>
											
											<c:if test="${metric.graphed == true}">
												<td><input type="checkbox"
													id="vm_graphed_${rowCounter.count}" 
													name="vm_graphed"
													checked="checked" 
													value="${metric.name}" /></td>
											</c:if>
											
											<td><input type="text"
												id="vm_warningThreshold_${rowCounter.count}"
												name="vm_warningThreshold_${rowCounter.count}" 
												size="6"
												maxlength="10" 
												class="text required number"
												value="${metric.warningThreshold}" />
											</td>
											
											<td><input type="text"
												id="vm_criticalThreshold_${rowCounter.count}"
												name="vm_criticalThreshold_${rowCounter.count}" 
												size="6"
												maxlength="10" 
												class="text required number"
												value="${metric.criticalThreshold}" />
											</td>
										</tr>
									</c:forEach>
								</table>
								<table cellpadding="0" cellspacing="0" border="0"
									class="display">
									<tr>
										<td align="right">
											<input type="button" 
											value="Home"
											onclick="location.href='index.html'" 
											class="button" />
										</td>
										<td align="left">
											<c:if test="${vemaBean.hypervisor.metric != null}">
												<INPUT VALUE="Save" TYPE="SUBMIT" class="button" />
											</c:if>
										</td>
									</tr>
								</table>
							</td>
						</tr>
					</table>
				</div>
			</div>
			<input type="hidden" name="action" value="create_from_ui_assign_page" />
			<input type="hidden" name="flow" value="modify" />
			<div class="controlbottom">
				<div class="cornerll"></div>
				<div class="cornerlr"></div>
			</div>
		</div>
	</form>
</body>
</html>