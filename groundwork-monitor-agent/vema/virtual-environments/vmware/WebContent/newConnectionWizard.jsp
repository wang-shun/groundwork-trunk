<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ page import="java.util.*"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Groundwork VEMA for VMWare</title>
<script type="text/javascript" src="js/java-agent.js"></script>
<link type="text/css" href="css/java-agent.css" rel="Stylesheet" />
<style type="text/css" title="currentStyle">
@import "css/jdma_page.css";

@import "css/jdma_table.css";
</style>
<script type="text/javascript" src="js/jquery.js"></script>
<script type="text/javascript" src="js/jquery.validate.min.js"></script>
<script>
	$(document).ready(function() {
		$("#connForm").validate();
	});
</script>
</head>
<body id="dt_example">
	<form name="connForm" id="connForm" method="POST"
		action="/gwos-vema-vmware/VMWareServlet">
		<div id="container">
			<!-- <p align="center">
				<img align="top" src="images/logo.png">
			</p> -->
			<p align="center" class="agent_title">VEMA Configuration wizard
				for VMWare</p>

			<div id="controlbg">
				<div class="controltop">
					<div class="cornerul"></div>
					<div class="cornerur"></div>
				</div>
				<div class="controlheader">VMWare / Groundwork Connection
					Settings (all fields required)</div>
				<div class="controlcontent">


					<jsp:useBean id="configBean"
						class="com.groundwork.agents.vema.configuration.VEMAGwosConfiguration"
						scope="session" />
					<jsp:setProperty name="configBean" property="*" />
					<table cellpadding="0" cellspacing="0" border="0" class="display"
						id="example">
						<c:if test="${message != null}">
							<tr>
								<td colspan="2" align="center"><label class="error">${message}</label></td>
							</tr>
						</c:if>
						<tr>
							<td>Groundwork Server Name :</td>
							<td><input type="text" class="text required"
								id="groundwork.server.name" name="groundwork.server.name"
								size="30" value="${configBean.gwosServer}" /></td>
						</tr>
						<tr>
							<td>Groundwork Server Port :</td>
							<td><input type="text" class="text required number"
								id="groundwork.server.port" name="groundwork.server.port"
								size="30" value="${configBean.gwosPort}" /></td>
						</tr>
						<tr>
							<td>Is SSL enabled on Groundwork Server ? :</td>
							<td><input type="checkbox" id="groundwork.gwosSSLEnabled"
								name="groundwork.gwosSSLEnabled" /></td>
						</tr>
						<tr>
							<td>Groundwork WebServices Endpoint :</td>
							<td><input type="text" class="text required"
								id="groundwork.webservices.endpoint"
								name="groundwork.webservices.endpoint" size="30"
								value="${configBean.wsEndpoint}" /></td>
						</tr>
						<tr>
							<td>Groundwork WebServices Username :</td>
							<td><input type="text" class="text required"
								id="groundwork.webservices.username"
								name="groundwork.webservices.username" size="30"
								value="${configBean.wsUser}" /></td>
						</tr>
						<tr>
							<td>Groundwork WebServices Password :</td>
							<td><input type="password" class="text required"
								id="groundwork.webservices.password"
								name="groundwork.webservices.password" size="30"
								value="${configBean.wsPassword}" /></td>
						</tr>

						<tr>
							<td>Is SSL enabled on ESX Server ? :</td>
							<c:if test="${configBean.virtualEnvSSLEnabled == true}">
								<td><input type="checkbox" id="virtualEnv.sslEnabled"
									name="virtualEnv.sslEnabled" checked="checked" /></td>
							</c:if>
							<c:if test="${configBean.virtualEnvSSLEnabled == false}">
								<td><input type="checkbox" id="virtualEnv.sslEnabled"
									name="virtualEnv.sslEnabled" /></td>
							</c:if>
						</tr>

						<tr>
							<td>ESX Server Name :</td>
							<td><input type="text" class="text required"
								id="virtualEnv.serverName" name="virtualEnv.serverName"
								size="30" value="${configBean.virtualEnvServer}" /></td>
						</tr>

						<tr>
							<td>ESX Server URI :</td>
							<td><input type="text" class="text required"
								id="virtualEnv.uri" name="virtualEnv.uri" size="30"
								value="${configBean.virtualEnvURI}" /></td>
						</tr>

						<tr>
							<td>ESX Server Username :</td>
							<td><input type="text" class="text required"
								id="virtualEnv.username" name="virtualEnv.username" size="30"
								value="${configBean.virtualEnvUser}" /></td>
						</tr>

						<tr>
							<td>ESX Server Password :</td>
							<td><input type="password" class="text required"
								id="virtualEnv.password" name="virtualEnv.password" size="30"
								value="${configBean.virtualEnvPassword}" /></td>
						</tr>

						<tr>
							<td>Check Interval (in mins) :</td>
							<td><input type="text" class="text required number"
								id="check.interval" name="check.interval" size="30"
								value="${configBean.checkInterval}" /></td>
						</tr>

					</table>
					<p align="center">
						<input type="button" value="Home"
							onclick="location.href='index.html'" class="button" /> <input
							type="submit" name="test" value="Test Connection" class="button" />
						<input type="submit" name="next" value="Next" class="button" />
					</p>

					<input type="hidden" name="action" value="create_from_ui_conn_page" />
				</div>
				<div class="controlbottom">
					<div class="cornerll"></div>
					<div class="cornerlr"></div>
				</div>
			</div>
		</div>
	</form>
</body>
</html>