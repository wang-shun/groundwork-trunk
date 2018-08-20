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
	String newHostGroup = request.getParameter("new-hostgroup");
	String applicationType = request.getParameter("listExistingAppTypes");
	
	// If a hostGroup was selected refresh the hosts associated with the Selected HostGroup
	String selectedHG = request.getParameter("listHGremove");
	if (selectedHG != null && selectedHG.length()> 0 )
	{
		adminBean.updateHostSelection(selectedHG);
	}
	
	if (newHostGroup != null && newHostGroup.length() > 0 )
	{
		// Call Admin function
		System.out.println("Add new hostgroup [" + newHostGroup + "]");
		adminBean.addHostGroup(applicationType,newHostGroup);
	}
	else
	{
		String hostGroup		=	request.getParameter("listHGAdd");
		String host	=	request.getParameter("listHostsAdd");
		
		if ( hostGroup != null && host != null )
		{
			System.out.println("Add Host ["+ host+"] to HostGroup [" + hostGroup + "] ");

			// Assign property
			adminBean.addHostToHostgroup("NAGIOS",hostGroup, host);
		}
		else
		{
			hostGroup		=	request.getParameter("listHGremove");
			host	=	request.getParameter("listHostsRemove");
			
			if ( hostGroup != null && host != null )
			{
				System.out.println("Remove Host ["+ host+"] From HostGroup [" + hostGroup + "] ");
	
				// Assign property
				adminBean.removeHostFromHostGroup(hostGroup, host);
			}
		}
	}
%>

<head>
 <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
 <link rel="stylesheet" type="text/css" media="all" href="../styles/groundwork.css" />
 <title>Manage Hostgroups</title>
 
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
Add new Host Groups to the Foundation datamodel</p></h1>

<table border="0" cellspacing="1" cellpadding="5">
<tr>
<td valign="top">
<p> Existing Host Groups:</p>
	<%
	int hostGoupArrayLength =adminBean.getHostGroupCount()  ;
	int comboSize = 15;
	if (hostGoupArrayLength < 15)
	{
		comboSize = hostGoupArrayLength;
	}%>
	<p>
	<select name="textExistingHG" size="<%= comboSize %>" >
	<% for(int iii=0;iii<hostGoupArrayLength;iii++)
	{ %>
	<option value="<%= adminBean.getHostGroupByIndex(iii) %>"><%= adminBean.getHostGroupByIndex(iii) %></option> 
	<% } %>
	</select>
	</p>
</td>
<form METHOD=POST ACTION="/foundation-webapp/admin/manage-hostgroups.jsp">
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

   <p>Enter new HostGroup to add (case sensitive):</p>
   <P>
          <input TYPE="text" NAME="new-hostgroup" SIZE="40" MAXLENGTH="40">
  </p>
  <p>
          <input TYPE="submit" NAME="submit-hostgroup-add" VALUE="Add Host Group">
          </p>
</form>

</td>
</tr>
</table>
<hr>
<h1>Add Hosts to an existing Host Group</h1>
<table border="0" cellspacing="1" cellpadding="5">
<tr>
<form METHOD=POST ACTION="/foundation-webapp/admin/manage-hostgroups.jsp">
<td valign="top">
<p>Existing Host Groups:</p>
<p>
	<select name="listHGAdd" size="<%= comboSize %>" >
	<% for(int iii=0;iii<hostGoupArrayLength;iii++)
	{ %>
	<option value="<%= adminBean.getHostGroupByIndex(iii) %>"><%= adminBean.getHostGroupByIndex(iii) %></option> 
	<% } %>
	</select>
	</p>
</td>

<td valign="top">
   <p>Existing Hosts:</p>
   <%
	int hostArrayLength =adminBean.getHostCount()  ;
	int hostComboSize = 15;
	if (hostArrayLength < 15)
	{
		hostComboSize = hostArrayLength;
	}%>
	<p>
	<select name="listHostsAdd" size="<%= hostComboSize %>" >
	<% for(int iii=0;iii<hostArrayLength;iii++)
	{ %>
	<option value="<%= adminBean.getHostByIndex(iii) %>"><%= adminBean.getHostByIndex(iii) %></option> 
	<% } %>
	</select>
	</p>
	
          <center><input TYPE="submit" NAME="submit-hostgroup-host-add" VALUE="Add Host to HostGroup"></center>

</td>
</tr>
</form>
</table>
<hr>
<h1>Remove Hosts from an existing Host Group</h1>
<table border="0" cellspacing="1" cellpadding="5">
<tr>
<form METHOD=POST ACTION="/foundation-webapp/admin/manage-hostgroups.jsp">
<td valign="top">
<p> Existing Host Groups:</p>
<p>
	<select name="listHGremove" size="<%= comboSize %>" onFocus="parent.location='/foundation-webapp/admin/manage-hostgroups.jsp?'+listHGremove.name+'='+listHGremove.value" >
	<% for(int iii=0;iii<hostGoupArrayLength;iii++)
	{ %>
	<option <% if (selectedHG != null && adminBean.getHostGroupByIndex(iii).compareTo(selectedHG) == 0 ) {%>selected<%} %> value="<%= adminBean.getHostGroupByIndex(iii) %>"><%= adminBean.getHostGroupByIndex(iii) %></option> 
	<% } %>
	</select>
	</p>
</td>
<td></td>
<td valign="top">
<p> Existing Hosts in HostGroup:</p>
<%
	int selHostArrayLength =adminBean.getselectedHostCount();
	int selHostComboSize = 15;
	if (selHostArrayLength < 15)
	{
		selHostComboSize = selHostArrayLength;
	}%>
<p>
	<select name="listHostsRemove" size="<%= selHostComboSize %>" >
	<% for(int iii=0;iii<selHostArrayLength;iii++)
	{ %>
	<option value="<%= adminBean.getSelectedHostByIndex(iii) %>"><%= adminBean.getSelectedHostByIndex(iii) %></option> 
	<% } %>
	</select>
	</p>
	
          <center><input TYPE="submit" NAME="submit-hostgroup-host-remove" VALUE="remove Host from HostGroup"></center>

</td>
</tr>
</form>
</table>


</body>
</html>
