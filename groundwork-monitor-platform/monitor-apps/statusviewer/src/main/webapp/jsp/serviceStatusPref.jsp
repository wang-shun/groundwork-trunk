<!--
    Coopyright (C) 2009 GroundWork Open Source, Inc. (GroundWork)
    All rights reserved. This program is free software; you can redistribute
    it and/or modify it under the terms of the GNU General Public License
    version 2 as published by the Free Software Foundation.
   
    This program is distributed in the hope that it will be useful, but
    WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
    General Public License for more details.
  
    You should have received a copy of the GNU General Public License along
    with this program; if not, write to the Free Software Foundation, Inc.,
    51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
-->
<%@ page language="java"%>
<%@ page import="javax.portlet.RenderRequest" %>
<%@ page import="javax.portlet.RenderResponse" %>
<%@ page import="com.groundworkopensource.portal.statusviewer.handler.ReferenceTreeMetaModel"%>
<jsp:useBean id="referenceTree" scope="application"
	class="com.groundworkopensource.portal.statusviewer.handler.ReferenceTreeMetaModel" />
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %> 
<portlet:defineObjects/>
<form method="post" action="<portlet:actionURL/>">
<table border="0">
	<tr>
		<td>Enter Host Name :</td>
		<td>
			<input type="text" name='serviceHostPref' id='serviceHostPref'
				value='<%=renderRequest.getAttribute("serviceHostPref")%>' 
				onblur="populateServices();" />
		</td>
	</tr>
	
	<tr>
		<td>Enter Service Name :</td>
		<td>
			<input type="text" name='servicePref' id='servicePref'
				value='<%=renderRequest.getAttribute("servicePref")%>'/>
		</td>
	</tr>
	
	<tr>
		<td>Enter Custom Portlet Title :</td>
		<td><input type="text" id='customPortletTitle' name='customPortletTitle'
			value='<%=renderRequest.getAttribute("customPortletTitle")%>' /></td>
	</tr>
	
	<tr>
		<td colspan="2"><input type="submit" id='savePrefs' value="Save Preferences"></td>
	</tr>
</table>
</form>

<script type="text/javascript">

var hostServiceDataArray = "<%= referenceTree.getAllServicesWithHostName() %>".split(",");

jQuery(document).ready(function() {
	var data = "<%= referenceTree.getAllHostNameList() %>".split(",");
	jQuery("#serviceHostPref").autocomplete(data);

	populateServices();
});

function populateServices() {	
	var hostName =  document.getElementById('serviceHostPref').value;
	if(hostName == null || hostName == "") {
		return;
	}

	jQuery("#servicePref").flushCache();
	var serviceData = new Array();
	var count=0;
	for(var i = 0; i < hostServiceDataArray.length; i++){
		var hostServiceData = hostServiceDataArray[i];
		var hostService = hostServiceData.split("^");		
		if (hostName==hostService[0]) {
			serviceData[count] = hostService[1];
			count=count+1;
		}
	}
	
	jQuery(document).ready(function() {
		jQuery("#servicePref").unautocomplete().autocomplete(serviceData);
	});
}
</script>