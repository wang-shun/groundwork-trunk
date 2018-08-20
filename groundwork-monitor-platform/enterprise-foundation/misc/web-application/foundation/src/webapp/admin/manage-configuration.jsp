<%@ page import="org.itgroundwork.foundation.pagebeans.*, java.util.*, java.io.*, java.text.*" %>
<%@ page contentType="text/html; charset=utf-8" %>
<%
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
 %>

<% 
	  AdminBean adminBean = new AdminBean(); 
	  // HARDWIRED-PATH
	  final String CONFIG_DIRECTORY = "/usr/local/groundwork/config";

	  String save = request.getParameter("save");
	  String fileName = request.getParameter("fn");
	  
	  if (save != null && save.length() > 0 && fileName != null && fileName.length() > 0)
	  {
		  adminBean.saveConfigurationProperties(CONFIG_DIRECTORY + "/" + fileName, request);
	  }
	  
	  File[] propertyFiles = adminBean.getPropertyFiles(CONFIG_DIRECTORY);
	  
	  String errorMsg = null;
	  SortedMap props = null;
	 
	  if (fileName != null && fileName.length() > 0)
	  {
		  try {
			  props = adminBean.getConfigurationProperties(CONFIG_DIRECTORY + "/" + fileName);
		  }
		  catch (Exception e)
		  {
			  errorMsg = "Unable to load properties file - " + fileName;
		  }
	  }
%>

<html>
<head>
 <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
 <link rel="stylesheet" type="text/css" media="all" href="../styles/groundwork.css" />
 <title>Manage Foundation Configuration</title>
 <script>
 function addProperty ()
 {
 	var propInput = document.all['newProperty'];
 	var valueInput = document.all['newValue'];

	if (propInput.value == null || propInput.value == "")
	{
		alert("Unable to add property.  A property name must be provided.");
		return;
	}
		
	if (valueInput.value == null || valueInput.value == "")
	{
		alert("Unable to add property.  A property value must be provided.");
		return;
	}
			
	var propTable = document.all['propertyTable'];		
    var tbody = propTable.getElementsByTagName("TBODY")[0];
    
    // Check to make sure the property does not already exist by checking
    // for a row with the same name.       
    if (tbody.rows != null && tbody.rows.length > 0 && tbody.rows[propInput.value] != null)
    {
   		alert("Error:  Property already exists - " + propInput.value);
		return;
    }
    
	var row = document.createElement("TR");
	var cell1 = document.createElement("TD");
	var cell2 = document.createElement("TD");
	var inp1 =  document.createElement("INPUT");
	
	inp1.setAttribute("name", "prop_" + propInput.value);	
	inp1.setAttribute("type","text");
	inp1.setAttribute("value", valueInput.value);

	cell1.innerHTML = propInput.value;	
	cell2.appendChild(inp1);	
	
	row.setAttribute("name", propInput.value);
	row.setAttribute("id", propInput.value);
	row.appendChild(cell1);
	row.appendChild(cell2);
	
	// Insert into beginning of list
	tbody.insertBefore(row, tbody.childNodes[0]);	 	
	
	propInput.value = null;
	valueInput.value = null;
 }
 
 function deleteProperty (propertyTableRowName)
 {
 	var tableRow = document.all[propertyTableRowName];
 	
 	if (tableRow == null)
 		return; 	
 					
	var propTable = document.all['propertyTable'];	
    var tbody = propTable.getElementsByTagName("TBODY")[0];
	tbody.removeChild(tableRow);
 }
 </script>
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
<h1><center>Foundation Configuration Files</center>
<p>
Choose a Foundation configuration file to view or edit.</p></h1>
<hr>
<table border="0" cellspacing="1" cellpadding="5">
<%
if (errorMsg != null && errorMsg.length() > 0) {
%>
<tr>
<td valign="top">
<span style="color:red;font-weight:bold;"><%= errorMsg %></span>
</td>
</tr>
<% } %>
<tr>
<td valign="top">
<table border="0" cellspacing="1" cellpadding="5" width="100%">
<tr><td><span style="color:#fa840f; font-size:9pt; font-weight: bold;">Configuration Directory:</span>&nbsp;&nbsp;<%= CONFIG_DIRECTORY %></td></tr>
<tr><td><div>
<table border="0" cellspacing="0" cellpadding="0">
<%
	if (propertyFiles != null && propertyFiles.length > 0) 
	{
%>
		<tr><td><H1>File</H1></td><td>&nbsp;&nbsp;&nbsp;</td><td><H1>Last Modified</H1></td></tr>
<%
		SimpleDateFormat format = new SimpleDateFormat("MM-dd-yyyy 'at' hh:mm aaa");
		File file = null;
		Date modifiedDate = null;
		for (int i = 0; i < propertyFiles.length; i++)
		{
			file = propertyFiles[i];
			modifiedDate = new Date(file.lastModified());
%>
			<tr>
			<td><a href="/foundation-webapp/admin/manage-configuration.jsp?fn=<%=file.getName()%>"><%= file.getName() %></a></td>
			<td>&nbsp;&nbsp;&nbsp;</td>
			<td align="left"><%= format.format(modifiedDate) %></td>
			</tr>
<%
		}
	}
	else {
%>
		<tr><td>No property files in the configuration directory - <%= CONFIG_DIRECTORY %></td></tr>
<%
	}
%>
</table>
</div></td></tr>
</table>
</td></tr>
<%
	if (fileName != null && fileName.length() > 0) {
%>
		<tr><td><span style="color:#fa840f; font-size:9pt; font-weight: bold;">Properties File:</span>&nbsp;&nbsp;<%= fileName %></td></tr>
<%
	} else {
%>
		<tr><td><span style="color:#fa840f; font-size:9pt; font-weight: bold;">Properties File:</span>&nbsp;&nbsp;<B>Choose a configuration file...</B></td></tr>
<%
	}
%>
<tr><td class="login_footer"><b>Note:  Please click Save to commit your changes.
<br/>Any changes made to a properties file will not take affect until Foundation is restarted.
<br/>A backup of the original file is created each time it is saved.</td></tr>
<% if (fileName != null && fileName.length() > 0) { %>
<tr><td>
	<table border="0" cellspacing="1" cellpadding="5" width="100%">
		<tr>
			<td>New Property:&nbsp;<input name="newProperty" type="text" size="25"/></td>
			<td>Value:&nbsp;<input name="newValue" type="text" size="25"/></td>
			<td><input type="button" value="Add" onclick="addProperty()" /></td>
		</tr>
	</table>
</td></tr>
<% } %>
<td valign="top">
<form action="/foundation-webapp/admin/manage-configuration.jsp?save=1&fn=<%= fileName %>" method="post">
	<table border="0" cellspacing="1" cellpadding="5" width="100%">
	<tr><td width="320px"><H1>Property Name</H1></td><td><H1>Value</H1></td></tr>
	<tr><td colspan=2"><div style="height : 375px; overflow : auto; ">
	<table id="propertyTable" name="propertyTable" border="0" cellspacing="1" cellpadding="5" width="100%">
	<tbody>
<%
	if (props != null) 
	{
		String key = null;
		String value = null;
		Set keys = props.keySet();
		Iterator it = keys.iterator();
		while (it.hasNext())
		{
			key = (String)it.next();
			value = (String)props.get(key);						
%>
			<tr id="<%= key %>" name="<%= key %>">
			<td width="320px"><%= key %></td>
			<td><input id="prop_<%= key %>" name="prop_<%= key %>" type="text" size="50" value="<%= value %>"/></td>
			<td><input type="button" value="Delete" onclick="deleteProperty('<%= key %>')" /></td>
			</tr>
<%
		}
	}
%>
	</tbody>
	</table>
	</div></td></tr>
	<tr><td colspan="2" align="right"><input type="submit" value="Save"/>&nbsp;<input type="button" value="Cancel" onclick="document.location.href='/foundation-webapp/admin/manage-configuration.jsp?fn=<%= fileName%>'" /></td></tr>
	</table>
</form>
</td></tr>

</table>


</body>
</html>
