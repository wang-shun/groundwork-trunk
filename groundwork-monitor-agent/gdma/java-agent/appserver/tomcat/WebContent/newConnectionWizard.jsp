<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ page import="java.util.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Groundwork Tomcat JDMA</title>
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
	action="/gwos-tomcat-monitoringAgent/GWOSTomcatServlet">
<p align="center"><img align="top" src="images/gwlogo.gif"></p>
<p align="center">JDMA for Tomcat</p>
<div id="controlbg">
<div class="controltop">
<div class="cornerul"></div>
<div class="cornerur"></div>
</div>
<div class="controlheader">Tomcat Connection Settings (all fields required)</div>
<div class="controlcontent">
<p>
<div id="container">
<%
	String message = "";
	Object messageObj = request.getAttribute("message");
	if (messageObj != null) {
		message = (String) messageObj;
%> <font class="error"><%=message%> </font>
<%} %>
<jsp:useBean id="connectorBean" class="com.groundwork.agents.appservers.collector.beans.ConnectorBean" scope="session"/>
<jsp:setProperty name="connectorBean" property="*"/> 

<table cellpadding="0" cellspacing="0" border="0" class="display" id="example" style="width:auto;">
	
	
	<tr>
		<td>Tomcat HostName :</td>
		<td><input type="text" id="hostName" name="hostName" size="20" class="required" value="${connectorBean.hostName}"/></td>
	</tr>	
	<tr>
		<td>Tomcat Instance Id :</td>
		<td><input type="text" id="instanceId" name="instanceId" size="20"  value="${connectorBean.instanceId}"/>(required if you are running multiple instance of tomcat on single host. For ex, say :TC01)</td>
	</tr>	
	<tr>
		<td>Tomcat JMX port :</td>
		<td><input type="text" id="port" name="port" size="10" class="required" value="${connectorBean.port}"/></td>
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
<p align="center">
<input type="button" value="Home"
	onclick="location.href='index.html'" class="button"/>
	<input type="submit"
	name="test" value="Test Connection" class="button"/>	
	<input type="submit"
	name="start" value="Start Discovery" class="button"/>
</p>
</div>
<input type="hidden" name="action" value="create_from_ui_conn_page" />
</div>
</div>
</form>
</body>
</html>