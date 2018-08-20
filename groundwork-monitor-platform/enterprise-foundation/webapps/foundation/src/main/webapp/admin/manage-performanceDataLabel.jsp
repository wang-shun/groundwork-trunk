<!--
/*
 * Collage - The ultimate data integration framework.
 * Copyright (C) 2004-2009  GroundWork Open Source Solutions info@groundworkopensource.com
 *     This program is free software; you can redistribute it and/or modify
 *     it under the terms of version 2 of the GNU General Public License
 *     as published by the Free Software Foundation.
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
-->
<%@ page import="org.itgroundwork.foundation.pagebeans.*"
		contentType="text/html; charset=utf-8"
%>

<%!
	AdminBean adminBean = new AdminBean(); 
%>

<%
	/* Check if new App Type needs to be added */
	String cmd = request.getParameter("cmd");
	if (cmd != null && cmd.equals("Update Data"))
	{
		// Call Admin function
		adminBean.updatePerformanceData(request);
	}
%>

<head>
 <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
 <link rel="stylesheet" type="text/css" media="all" href="../styles/groundwork.css" />
 <title>Manage Foundation PerformanceDataLabel Entries</title>
</head>

<body bgcolor="#FFF888">

<table border="0" cellspacing="1" cellpadding="5">
<tr>
<td width="50">
</td>
<td width="300" valign="top" align="left">
<br />
<a href="/foundation-webapp/index.jsp">Main page</a><br />
<br />

Foundation Administration pages:<br />
<a href="/foundation-webapp/admin/manage-apptypes.jsp">Manage Application Types</a><br />
<a href="/foundation-webapp/admin/manage-properties.jsp">Manage Properties</a><br />
<a href="/foundation-webapp/admin/manage-hostgroups.jsp">Manage Host Groups</a><br />
<a href="/foundation-webapp/admin/manage-performanceDataLabel.jsp">Manage Performance Data</a><br/>
<a href="/foundation-webapp/admin/manage-consolidationCriteria.jsp">Manage Consolidation Criteria</a><br/>
<td width="*">
<h1><center>Foundation Framework</center>
<p>
Update Performance Data Entries to the PerformanceDataLabel Table.</p></h1>
	<p>Existing Performance Data Labels:</p>
	<p>
	<center>
	<!-- 
	<iframe  src="/foundation-webapp/performanceDataPost?cmd=getperformancedata" width="645" height="360" FRAMEBORDER="0"></iframe>
	-->
	<style type="text/css">
	<!--  div.scroll{border:0px;padding:8px; -->
	</style>
	
	<div class="scroll">
	<form method="POST" action="/foundation-webapp/admin/manage-performanceDataLabel.jsp">
		<%=adminBean.getPerformanceData()%>
	</form>
	</div>
	</center>
	</p>
</td>

</tr>
</table>

 
</body>
</html>
