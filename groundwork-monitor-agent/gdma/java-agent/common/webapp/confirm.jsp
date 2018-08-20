<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<HTML>
<HEAD>
<title>Groundwork @appserver_camelcase@ Monitoring Agent</title>
<script type="text/javascript" src="js/java-agent.js"></script>
<link type="text/css" href="css/java-agent.css" rel="Stylesheet" />
</HEAD>
<BODY>
<p align="center">
	<img align="top" src="images/gwlogo.gif">
</p>
<p align="center">
	JDMA for @appserver_camelcase@
</p>
<div class="sidebox">
<div class="boxhead">
<h2>Confirmation</h2>
</div>
<div class="boxbody">
<p>
<%
	Object messageObj = request.getAttribute("message");
	String message = (String) messageObj;
%> <%=message%>. Please restart/reload your gwos_@appserver_shortname@_monitoringAgent application.</p>
<p align="center"><input type="button" value="Home"
	onclick="location.href='index.html'" class="button"/></p>
</div>
</div>
</BODY>
</HTML>
