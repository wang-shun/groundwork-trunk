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
	contentType="text/html; charset=utf-8"%>

<%!AdminBean adminBean = new AdminBean();%>

<%
   request.setAttribute("error", "");
	/* Check if new Consolidation Criteria needs to be added */
	String cmd = request.getParameter("cmd");
	if (cmd != null && cmd.equals("Update Data")) {
		// Call Admin function
		adminBean.updateConsolidationCriteria(request);
	}
	
	String cmdAdd = request.getParameter("submit-criteria-add");
	if (cmdAdd != null && cmdAdd.equals("Add Consolidation Criteria")) {
		// Call Admin function
		adminBean.addConsolidationCriteria(request);
	}
	
	String cmdRestoreDefault= request.getParameter("restore-criteria");
	if (cmdRestoreDefault != null && cmdRestoreDefault.equals("Restore Default Consolidation Criteria")) {
		// Call Admin function
		adminBean.restoreDefaultConsolidationCriteria(request);
	}
%>

<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<link rel="stylesheet" type="text/css" media="all"
	href="../styles/groundwork.css" />
<title>Manage Foundation Consolidation Criteria Entries</title>
</head>

<body bgcolor="#FFF888">



<table border="0" cellspacing="1" cellpadding="5">
	<tr>
		<td width="50"></td>
		<td width="300" valign="top" align="left"><br />
		<a href="/foundation-webapp/index.jsp">Main page</a><br />
		<br />

		Foundation Administration pages:<br />
		<a href="/foundation-webapp/admin/manage-configuration.jsp">Manage
		Configuration</a><br />
		<a href="/foundation-webapp/admin/manage-apptypes.jsp">Manage
		Application Types</a><br />
		<a href="/foundation-webapp/admin/manage-properties.jsp">Manage
		Properties</a><br />
		<a href="/foundation-webapp/admin/manage-hostgroups.jsp">Manage
		Host Groups</a><br />
		<a href="/foundation-webapp/admin/manage-performanceDataLabel.jsp">Manage
		Performance Data</a><br />
		<a href="/foundation-webapp/admin/manage-consolidationCriteria.jsp">Manage
		Consolidation Criteria</a><br />
		<td width="*">
		<h1>
		<center>Foundation Framework</center>
		<p>Update Consolidation Criteria Entries to the Consolidation
		Criteria Table.</p>
		</h1>
		<p>Existing Consolidation Criterias:</p>
		<p>
		<center>
		<div class="scroll">
		<form method="POST"
			action="/foundation-webapp/admin/manage-consolidationCriteria.jsp">
		<%=adminBean.getConsolidationCriterias()%></form>
		</div>
		</center>
		</p>
		</td>

	</tr>
	
	<tr>
		<td align="right" colspan="3">
		
		<form method="POST"
			action="/foundation-webapp/admin/manage-consolidationCriteria.jsp">
		<table align="right" BORDERCOLOR="blue">
                <th colspan="3"><h1>
                Enter new Consolidation criteria to add (case sensitive):<p><font color="red"><%=request.getAttribute("error")%></font></p>
                </h1></th>
		<tr>
		<td>Name: </td>
		<td><input TYPE="text" NAME="new-name" SIZE="50" MAXLENGTH="50"></td>
		</tr>
		<tr>
		<td>Criteria: </td>
		<td><input TYPE="text" NAME="new-criteria" SIZE="60" MAXLENGTH="100"></td>
		</tr>
		<tr>
		<td colspan="1"><input TYPE="submit" NAME="submit-criteria-add"
			VALUE="Add Consolidation Criteria"></td>
			  <td colspan="1"><input TYPE="submit" NAME="restore-criteria"
                        VALUE="Restore Default Consolidation Criteria"></td>
		</tr>
		</table>
		</form>
		
		</td>
	</tr>
	
	
</table>
</body>
</html>
