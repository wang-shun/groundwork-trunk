<html>
<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="org.itgroundwork.foundation.pagebeans.*, java.util.*" %>
<% 
AdminBean adminBean = new AdminBean(); 
SortedMap<String, String> map = adminBean.getConfigurationProperties("/usr/local/groundwork/config/foundation.properties");
String isGDMAPluginEnabled = map.get("gdma.plugin.upload.enable");
%>
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

<%@ include file="i18nLib.jsp" %>


<%
    // initialize a private HttpServletRequest
    setRequest(request);

%>

<head>
 <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
 <link rel="stylesheet" type="text/css" media="all" href="../styles/groundwork.css" />
 <title>Foundation Administartion</title>
</head>

<body bgcolor="#FFF888">

<table border="0" cellspacing="1" cellpadding="5">
<tr>
<td width="50">
</td>
<td width="300" valign="top" align="left">

<td width="*">
<h1>Foundation Framework</h1>
<p>
Foundation Framework is Data integration framework. The backend data model allows the integration of any state, event, and performance data, independent of the Monitoring Application that produces it.
The datastore can be accessed through a Web Service interface.
</p>

<h1>Administration:</h1>
<ul>
<li><a href="/foundation-webapp/admin/manage-configuration.jsp">Manage Configuration</a></li>
<li><a href="/foundation-webapp/admin/manage-apptypes.jsp">Manage Application Types</a></li>
<li><a href="/foundation-webapp/admin/manage-properties.jsp">Manage Properties</a></li>
<li><a href="/foundation-webapp/admin/manage-hostgroups.jsp">Manage Host Groups</a></li>
<li><a href="/foundation-webapp/admin/manage-performanceDataLabel.jsp">Manage Performance Data</a></li>
<li><a href="/foundation-webapp/admin/manage-consolidationCriteria.jsp">Manage Consolidation Criteria</a></li>
<%if (isGDMAPluginEnabled != null && isGDMAPluginEnabled.equalsIgnoreCase("true")) {
%>
<li><a href="/foundation-webapp/admin/manage-plugins.iface">Manage Plugins</a></li>
<%
}
%>
</ul>
</td>

</tr>
</table>


</body>
</html>
