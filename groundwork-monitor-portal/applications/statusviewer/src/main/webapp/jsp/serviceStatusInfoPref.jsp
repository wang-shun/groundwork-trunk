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
<%@ page import="javax.portlet.RenderRequest"%>
<%@ page import="javax.portlet.RenderResponse"%>
<%@ page import="com.groundworkopensource.portal.statusviewer.handler.ReferenceTreeMetaModel"%>
<jsp:useBean id="referenceTree" scope="application"
	class="com.groundworkopensource.portal.statusviewer.handler.ReferenceTreeMetaModel" />
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>
<portlet:defineObjects />
<form method="post" action="<portlet:actionURL/>">
<table border="0">
	<tr>
		<td>Enter Host Name :</td>
		<td colspan="2"><input type="text" name='serviceHostPref'
			id='serviceHostPref'
			value='<%=renderRequest.getAttribute("serviceHostPref")%>' /></td>
	</tr>
	<tr>
		<td>Enter Service Name :</td>
		<td colspan="2"><input type="text" name='servicePref' id='servicePref'
			value='<%=renderRequest.getAttribute("servicePref")%>' onfocus="populateServices();" /></td>
	</tr>
	<tr>
		<td>Enter Custom Link 1 :</td>
		<td><input type="text" name='serviceCustLink1'
			value='<%=renderRequest.getAttribute("serviceCustLink1")%>' /></td>
		<td><img src="/portal-statusviewer/images/icon_help.gif"
			alt="Help"
			title="URL should be specified in this format: http://www.google.com. Formats other than this are invalid. User can use $HOST$ and/or $SERVICE$ tokens in the URL like - http://$HOST$/index.html?servie_name=$SERVICE$" /></td>
	</tr>
	<tr>
		<td>Enter Custom Link 2 :</td>
		<td><input type="text" name='serviceCustLink2'
			value='<%=renderRequest.getAttribute("serviceCustLink2")%>' /></td>
		<td><img src="/portal-statusviewer/images/icon_help.gif"
			alt="Help"
			title="URL should be specified in this format: http://www.google.com. Formats other than this are invalid. User can use $HOST$ and/or $SERVICE$ tokens in the URL like - http://$HOST$/index.html?servie_name=$SERVICE$" /></td>
	</tr>
	<tr>
		<td>Enter Custom Link 3 :</td>
		<td><input type="text" name='serviceCustLink3'
			value='<%=renderRequest.getAttribute("serviceCustLink3")%>' /></td>
		<td><img src="/portal-statusviewer/images/icon_help.gif"
			alt="Help"
			title="URL should be specified in this format: http://www.google.com. Formats other than this are invalid. User can use $HOST$ and/or $SERVICE$ tokens in the URL like - http://$HOST$/index.html?servie_name=$SERVICE$" /></td>
	</tr>
	<tr>
		<td>Enter Custom Link 4 :</td>
		<td><input type="text" name='serviceCustLink4'
			value='<%=renderRequest.getAttribute("serviceCustLink4")%>' /></td>
		<td><img src="/portal-statusviewer/images/icon_help.gif"
			alt="Help"
			title="URL should be specified in this format: http://www.google.com. Formats other than this are invalid. User can use $HOST$ and/or $SERVICE$ tokens in the URL like - http://$HOST$/index.html?servie_name=$SERVICE$" /></td>
	</tr>
	<tr>
		<td>Enter Custom Link 5 :</td>
		<td><input type="text" name='serviceCustLink5'
			value='<%=renderRequest.getAttribute("serviceCustLink5")%>' /></td>
		<td><img src="/portal-statusviewer/images/icon_help.gif"
			alt="Help"
			title="URL should be specified in this format: http://www.google.com. Formats other than this are invalid. User can use $HOST$ and/or $SERVICE$ tokens in the URL like - http://$HOST$/index.html?servie_name=$SERVICE$" /></td>
	</tr>
	
	<tr>
		<td>Enter Custom Portlet Title :</td>
		<td colspan="2"><input type="text" name='customPortletTitle'
			value='<%=renderRequest.getAttribute("customPortletTitle")%>' /></td>
	</tr>

	<tr>
		<td colspan="2"><input type="submit" value="Save Preferences"></td>
	</tr>
</table>
</form>
<script type="text/javascript">
//jQuery.noConflict();
jQuery(document).ready(function() {
	var data = "<%= referenceTree.getAllHostNameList() %>".split(",");
	jQuery("#serviceHostPref").autocomplete(data);

	
  });

function populateServices() {
	// jQuery.noConflict();
	var hostServiceDataArray = "<%= referenceTree.getAllServicesWithHostName() %>".split(",");
	jQuery("#servicePref").flushCache();
	var serviceData = new Array();
	var hostName =  document.getElementById('serviceHostPref').value;
	//alert("Host name--" +  hostName);
	var count=0;
	for(var i = 0; i < hostServiceDataArray.length; i++){
		var hostServiceData = hostServiceDataArray[i];
		var hostService = hostServiceData.split("^");
		//alert(hostService[0] +"--" +  hostName);
		if (hostName==hostService[0])
		{
			serviceData[count] = hostService[1];
			count=count+1;
		} // endif
	}
	jQuery(document).ready(function() {
	jQuery("#servicePref").autocomplete(serviceData);
	});
}
 </script>