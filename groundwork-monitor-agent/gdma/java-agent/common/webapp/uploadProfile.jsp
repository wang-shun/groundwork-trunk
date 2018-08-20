<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ page import="java.util.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Groundwork @appserver_camelcase@ JDMA</title>
<script type="text/javascript" src="js/java-agent.js"></script>
<link type="text/css" href="css/java-agent.css" rel="Stylesheet" />
<script type="text/javascript">
function submitUpload() {	
	valid = true;
	if ( document.uploadForm.serverName.value == "" )
    {
        alert ( "Please enter a valid Groundwork Server name!" );
        
        valid = false;
    }
	if ( document.uploadForm.userName.value == "" )
    {
        alert ( "Please enter a valid Groundwork User name!" );
     
        valid = false;
    }
	if ( document.uploadForm.password.value == "" )
    {
        alert ( "Please enter a valid Groundwork password!" );
       
        valid = false;
    }
	if ( document.uploadForm.propFile.value == "" )
    {
        alert ( "Please enter a valid Properties File Path!" );
       
        valid = false;
    }
    return valid;
}
</script>
</head>
<body>

<form name="uploadForm"
	ACTION="/gwos-@appserver_shortname@-monitoringAgent/GWOS@appserver_camelcase@Servlet"
	method="post" onsubmit="return submitUpload();">
	<p align="center">
	<img align="top" src="images/gwlogo.gif">
</p>
<p align="center">
	JDMA for @appserver_camelcase@
</p>
<div class="sidebox">
<div class="boxhead">
<h2>Export Profile</h2>
</div>
<div class="boxbody">
<p>
<%
	String message = "";
	Object messageObj = request.getAttribute("message");
	if (messageObj != null) {
		message = (String) messageObj;
%> <font class="error"><%=message%> </font></p>
<%} %>
<table>
	<tr>
		<td>GroundWork Server :</td>
		<td><input type="text" name="serverName" /></td>
	</tr>
	<tr>
		<td>Is SSL enabled on your GroundWork Server ? :</td>
		<td><input type="checkbox" name="sslEnabled" /></td>
	</tr>
	<tr>
		<td>UserName :</td>
		<td><input type="text" name="userName" /></td>
	</tr>
	<tr>
		<td>Password :</td>
		<td><input type="password" name="password" /></td>
	</tr>
	<tr>
		<td>Properties File Path :</td>
		<td><input type="text" name="propFile" value="/tmp/"/>(Path for the gwos_@appserver_lowercase@.xml file on your @appserver_camelcase@ server)</td>
	</tr>
</table>
<p align="center">
<input type="button" value="Home"
	onclick="location.href='index.html'" class="button"/><input type="submit"
	name="export" value="Export" class="button"/>
</p>
</div>
</div>
<input type="hidden" name="action" value="create_from_ui_export_page" />
</form>
</body>
</html>