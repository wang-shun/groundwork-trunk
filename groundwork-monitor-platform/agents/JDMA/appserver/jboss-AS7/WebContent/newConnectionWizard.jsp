<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ page import="java.util.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Groundwork JBoss AS7 JDMA</title>
<script type="text/javascript" src="js/java-agent.js"></script>
<link type="text/css" href="css/java-agent.css" rel="Stylesheet" />
<style type="text/css" title="currentStyle">
@import "css/jdma_page.css";
@import "css/jdma_table.css";
</style>
<script type="text/javascript" src="js/jquery.js"></script>
<script type="text/javascript" src="js/jquery.validate.min.js"></script>
<script>
$(document).ready(function(){
    $("#connForm").validate();
  });
</script>
</head>
<body>
<form name="connForm" id = "connForm" method="POST"
	action="/gwos-jbossas7-monitoringAgent/GWOSJBossAS7Servlet">
<p align="center"><img align="top" src="images/gwlogo.png" width="264px"></p>
<p align="center">JDMA for JBoss AS7</p>
<div id="controlbg" class="controlbg">
<div class="controltop">
<div class="cornerul"></div>
<div class="cornerur"></div>
</div>
<div class="controlheader">JBoss Connection Settings (all fields required)</div>
<div class="controlcontent">
<p>
<div id="container">
<%
	 String styleClass = "";
     Object styleClassObj = request.getAttribute("styleClass");
     if (styleClassObj != null)
        styleClass = (String) styleClassObj;
	String message = "";
	Object messageObj = request.getAttribute("message");
	if (messageObj != null) {
		message = (String) messageObj;


%> <font class="<%=styleClass%>"><%=message%> </font>
<%} %>
<jsp:useBean id="connectorBean" class="com.groundwork.agents.appservers.collector.beans.ConnectorBean" scope="session"/>
<jsp:setProperty name="connectorBean" property="*"/> 

<table cellpadding="0" cellspacing="0" border="0" class="display" id="example" style="width:auto;">

	<tr>
		<td>JBoss HostName :</td>
		<td><input type="text" id="hostName" name="hostName" size="20" class="required"
				   value="${connectorBean.hostName}"/>(this is how nagios identifies this agent/host)
		</td>
	</tr>
	<tr>
		<td>JBoss Instance Id :</td>
		<td><input type="text" id="instanceId" name="instanceId" size="20" value="${connectorBean.instanceId}"/>(required
			if you are running multiple instance of JBoss on single host. For ex, say :JB01)
		</td>
	</tr>
	<tr>
		<td>JBoss remoting port :</td>
		<td><input type="text" id="port" name="port" size="10" class="required" value="${connectorBean.port}"/>
			4447 default. 9999 for authentication over management Realm</td>
	</tr>

	<tr>
		<td>JBoss User Name :</td>
		<td><input type="text" id="jmx_username" name="jmx_username" size="50" value="${connectorBean.username}"/></td>
	</tr>
	<tr>
		<td>JBoss Password :</td>
		<td><input type="password" id="jmx_password" name="jmx_password" size="50" value="${connectorBean.password}"/></td>
	</tr>
	<tr>
	</tr>
	<tr>
		<td>Nagios HostName :</td>
		<td><input type="text" id="nagiosHostname" name="nagiosHostname" size="30" class="required" value="${connectorBean.nagiosHostname}"/></td>
	</tr>
	<tr>
		<td>Nagios Port :</td>
		<td><input type="text" id="nagiosPort" name="nagiosPort" size="5" class="required"value="${connectorBean.nagiosPort}"/></td>
	</tr>
	<tr>
		<td>Nagios Encryption :</td>
		<td>
			<select id="nagiosEncryption" name="nagiosEncryption" class="text">
			<option value="0">0-No Encryption</option>
			<option value="1" selected="selected">1-XOR(Just obfuscated)</option>
			<option value="3">3-Triple DES</option>
			</select>
			
		</td>
	</tr>
	<tr>
		<td>Nagios Password : (Optional)</td>
		<td><input type="text" id="nagiosPassword" name="nagiosPassword" size="20"  value="${connectorBean.nagiosPassword}"/></td>
	</tr>
	<tr>
		<td>Passive Check Interval(in secs) :</td>
		<td><input type="text" id="passiveCheckInterval" name="passiveCheckInterval" size="6" class="required" value="${connectorBean.passiveCheckInterval}"/></td>
	</tr>	
	<tr>
		<td>Object Name Filter :</td>
		<td><input type="text" id="object.name.filter" name="object.name.filter" size="24" class="required" value="${connectorBean.objectNameFilter}"/></td>
	</tr>	
</table>
</div>
</p>
</div>
<div class="controlbottom">
<div class="cornerll"></div>
<div class="cornerlr"></div>
</div>
</div>
<p align="center">
<input type="button" value="Home"
	onclick="location.href='index.html'" class="button"/>
	<input type="submit"
	name="test" value="Test Connection" class="button"/>	
	<input type="submit"
	name="start" value="Start Discovery" class="button"/>
</p>

<input type="hidden" name="action" value="create_from_ui_conn_page" />
</form>
</body>
</html>
