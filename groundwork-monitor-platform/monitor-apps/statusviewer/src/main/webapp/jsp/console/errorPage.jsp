<!--
   Copyright (C) 2009 GroundWork Open Source, Inc. (GroundWork)
   All rights reserved. Use is subject to GroundWork commercial license terms.
--> 

<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@page import="com.groundworkopensource.webapp.console.*"%>    
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Groundwork Error Page</title>
	<link href="resources/global.css" rel="styleSheet" type="text/css"
		media="screen" />
<style type="text/css">
.sidebox {
	margin: 0 auto; /* center for now */
	width: 35%; /* ems so it will grow */
	background: url(<%=request.getContextPath()%>/images/sbbody-r.gif) no-repeat bottom right;
	font-size: 100%;
}
.boxhead {
	background: url(<%=request.getContextPath()%>/images/sbhead-r.gif) no-repeat top right;
	margin: 0;
	padding: 0;
	text-align: center;
}
.boxhead h2 {
	background: url(<%=request.getContextPath()%>/images/sbhead-l.gif) no-repeat top left;
	margin: 0;
	padding: 22px 30px 5px;
	color: white; 
	font-weight: bold; 
	font-size: 1.2em; 
	line-height: 1em;
	text-shadow: rgba(0,0,0,.4) 0px 2px 5px; /* Safari-only, but cool */
}
.boxbody {
	background: url(<%=request.getContextPath()%>/images/sbbody-l.gif) no-repeat bottom left;
	margin: 0;
	padding: 5px 30px 31px;
}
.errorbody
{
background-color:#FFFFFF;
}
</style>		
</head>
<body class="errorbody"> 
<%String logoPath=request.getContextPath() + "/images/gwlogo.gif";
 String error= (String)request.getSession().getAttribute("error");   %>

<br>
<div class="sidebox">
	<div class="boxhead"><h2>Error</h2></div>
	<div class="boxbody">
		<p><%=DateUtils.getDateForHeader() %></p>
		<p><%=error%></p>
		<p><%=PropertyUtils.getProperty(ConsoleConstants.I18N_GLOBAL_ERROR_MESSAGE1) %></p>
<p><%=PropertyUtils.getProperty(ConsoleConstants.I18N_GLOBAL_ERROR_MESSAGE2) %></p>
<p><%=PropertyUtils.getProperty(ConsoleConstants.I18N_GLOBAL_ERROR_MESSAGE3) %></p>
	</div>
</div>
</body>
</html>