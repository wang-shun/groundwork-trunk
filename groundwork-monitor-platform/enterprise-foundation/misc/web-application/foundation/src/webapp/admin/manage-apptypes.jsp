<%@ page import="org.itgroundwork.foundation.pagebeans.*" %>
<% AdminBean adminBean = new AdminBean(); %>
<html>
<%@ page contentType="text/html; charset=utf-8" %>
<%
/*
 * Collage - The ultimate data integration framework.
 * Copyright (C) 2004-2006  GroundWork Open Source Solutions info@groundworkopensource.com
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
 %>

<%
	/* Check if new App Type needs to be added */
	String appType = request.getParameter("new-apptype");
	if (appType != null && appType.length() > 0 )
	{
		// Call Admin function
		adminBean.addApplicationType(appType);
	}
%>

<head>
 <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
 <link rel="stylesheet" type="text/css" media="all" href="../styles/groundwork.css" />
 <title>Manage Foundation Application Types</title>
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
<a href="/foundation-webapp/admin/manage-configuration.jsp">Manage Configuration</a><br />
<a href="/foundation-webapp/admin/manage-apptypes.jsp">Manage Application Types</a><br />
<a href="/foundation-webapp/admin/manage-properties.jsp">Manage Properties</a><br />
<a href="/foundation-webapp/admin/manage-hostgroups.jsp">Manage Host Groups</a><br />
<a href="/foundation-webapp/admin/manage-performanceDataLabel.jsp">Manage Performance Data</a><br/>
<a href="/foundation-webapp/admin/manage-consolidationCriteria.jsp">Manage Consolidation Criteria</a><br/>

<td width="*">
<h1><center>Foundation Framework</center>
<p>
Add new Application Types to the Foundation Data model.</p></h1>
<form METHOD=POST ACTION="/foundation-webapp/admin/manage-apptypes.jsp">
      
	<p>Existing Application Types:</p>
	<p>
	<center>
	<% 
	   int arrayLength =adminBean.getAppTypeCount(); %>
	<select name="listExistingAppTypes" size="<%= arrayLength %>">
	<% for(int i=0;i<arrayLength;i++)
		{ %>
	  <option value="<%= adminBean.getApplicationTypesByIndex(i) %>"><%= adminBean.getApplicationTypesByIndex(i) %></option>
	  <% } %>
	</select>
	</center>
	</p>
	<br>
   <p>New Application Type (case sensitive)<p>
      <center>    
          <input TYPE="text" NAME="new-apptype" SIZE="40" MAXLENGTH="40">
      </center>
   <br>
	<p>
          <center><input TYPE="submit" NAME="submit-apptype" VALUE="Add Application Type"></center>
   </p>
</form>

</td>

</tr>
</table>

 

</body>
</html>
