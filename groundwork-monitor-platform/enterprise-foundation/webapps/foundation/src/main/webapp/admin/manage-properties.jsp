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
	/* Check if new property needs to be added */
	String newProperty = request.getParameter("new-property");
	if (newProperty != null && newProperty.length() > 0 )
	{
		
		// Call Admin function
		System.out.println("Call add property for  Property [" + newProperty + "]");
		adminBean.addProperty(newProperty, "STRING");
	}
	else
	{
		// Extract the entity type and the application type
		String appType		=	request.getParameter("listExistingAppTypes");
		String entityType	=	request.getParameter("listExistingEntityTypes");
		String assignProperty = request.getParameter("listExistingPropertyTypes");
		
		if ( appType != null && entityType != null && assignProperty != null)
		{
			System.out.println("Call assign property for AppType ["+ appType+"] entityType [" + entityType + "] Property [" + assignProperty + "]");

			// Assign property
			adminBean.assignProperty(appType,entityType, assignProperty);
		}
	}
%>

<head>
 <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
 <link rel="stylesheet" type="text/css" media="all" href="../styles/groundwork.css" />
 <title>Manage Foundation Properties</title>
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
Add properties to the Foundation Data model or assign existing Properties to an Application Type/Entity Type</p></h1>


<table border="0" cellspacing="1" cellpadding="5">
<tr>
<td>
<form METHOD=POST ACTION="/foundation-webapp/admin/manage-properties.jsp">
<p> Existing Properties:</p>
<p>
	<%
	int propArrayLength =adminBean.getPropertiesCount()  ;
	int comboSize = 15;
	if (propArrayLength < 15)
	{
		comboSize = propArrayLength;
	}%>
	<select name="textExistingProperties" size="<%= comboSize %>" >
	<% for(int iii=0;iii<propArrayLength;iii++)
	{ %>
	<option value="<%= adminBean.getPropertyByIndex(iii) %>"><%= adminBean.getPropertyByIndex(iii) %></option> 
	<% } %>
	</select>
</td>
<td valign="top">	
</p>	
   <p>Enter new Property to add (case sensitive):</p>
          <input TYPE="text" NAME="new-property" SIZE="40" MAXLENGTH="40">
      
   <br>
	<p>
          <center><input TYPE="submit" NAME="submit-property" VALUE="Add Property"></center>
   </p>
</form>
</td>
</tr>
</table>
<%-- <hr>
<table border="0" cellspacing="1" cellpadding="5">
<tr>
<form METHOD=POST ACTION="/foundation-webapp/admin/manage-properties.jsp">
<td valign="top">
<p>Existing Application Types:</p>
	<p>
	
	<% 
	   int arrayLength =adminBean.getAppTypeCount(); %>
	<select name="listExistingAppTypes" size="<%= arrayLength %>">
	<% for(int i=0;i<arrayLength;i++)
		{ %>
	  <option value="<%= adminBean.getApplicationTypesByIndex(i) %>"><%= adminBean.getApplicationTypesByIndex(i) %></option>
	  <% } %>
	</select>
	
	</p>
</td>
<td valign="top">
<p>Existing Entity Types:</p>
	<p>
	
	<% 
	   int entArrayLength =adminBean.getEntityTypeCount(); %>
	<select name="listExistingEntityTypes" size="<%= entArrayLength %>">
	<% for(int ii=0;ii<entArrayLength;ii++)
		{ %>
	  <option value="<%= adminBean.getEntityTypesByIndex(ii) %>"><%= adminBean.getEntityTypesByIndex(ii) %></option>
	  <% } %>
	</select>
	
	</p>
</td>
<td valign="top">
<p>Existing Properties:</p>
	<p>
	<% 
		propArrayLength =adminBean.getPropertiesCount()  ; %>
	<select name="listExistingPropertyTypes" size="<%= comboSize %>">
	<% for(int ii=0;ii<propArrayLength;ii++)
		{ %>
	  <option value="<%= adminBean.getPropertyByIndex(ii) %>"><%= adminBean.getPropertyByIndex(ii) %></option>
	  <% } %>
	</select>
	</p>
</td>
</tr>
<tr>
<td></td>
<td><p>
          <center><input TYPE="submit" NAME="assign-property" VALUE="Assign Property"></center>
   </p></td>
<td></td>
</form>
</table> --%>

</td>

</tr>

</body>
</html>
