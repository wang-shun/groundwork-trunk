<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ page import="java.util.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Groundwork Weblogic JDMA</title>
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
	action="/gwos-wls-monitoringAgent/GWOSWeblogicServlet">
<p align="center"><img align="top" src="images/gwlogo.gif"></p>
<p align="center">JDMA for Weblogic</p>
<div id="controlbg">
<div class="controltop">
<div class="cornerul"></div>
<div class="cornerur"></div>
</div>
<div class="controlheader">Weblogic Connection Settings (all fields required)</div>
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
		<td>Weblogic UserName :</td>
		<td><input type="text" id = "userName" name="userName" class="required" value="${connectorBean.userName}"/></td>
	</tr>
	<tr>
		<td>Weblogic Password :</td>
		<td><input type="password" id = "password" name="password" class="required" value="${connectorBean.password}"/></td>
	</tr>
	
	<tr>
		<td>Protocol :</td>
		<td><input type="text" id="protocol" name="protocol" size="10" class="required"value="${connectorBean.protocol}"/></td>
	</tr>
	<tr>
		<td>Weblogic HostName :</td>
		<td><input type="text" id="hostName" name="hostName" size="20" class="required"value="${connectorBean.hostName}"/></td>
	</tr>	
	<tr>
		<td>Weblogic port :</td>
		<td><input type="text" id="port" name="port" size="10" class="required" value="${connectorBean.port}"/>(For stand-alone mode, enter application server soap port. For ND mode, enter node-agent soap port)</td>
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
		<td>Object Filter :</td>
		<td><input type="text" id="object.name.filter" name="object.name.filter" size="50" class="required" value="${connectorBean.objectFilter}"/></td>
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