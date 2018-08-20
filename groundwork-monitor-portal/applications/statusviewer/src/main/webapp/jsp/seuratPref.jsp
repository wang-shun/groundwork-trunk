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
<script type="text/javascript">
var hostGroupData = "<%=renderRequest.getAttribute("hostGroupPref")%>".split(",");
var entireNetworkPreference = "<%=renderRequest.getAttribute("seuratEntNetPref")%>".split(",");
var extendedRoleHostGroupList = "<%= referenceTree.getExtendedRoleHostGroupList()%>";

function seuratInit(){
	document.getElementById("hostGroupPref").disabled=false;
	
	// if empty, list.toString() returns empty parantheses "[]"
	if(extendedRoleHostGroupList != "[]" && hostGroupData=="") {
		entireNetworkPreference = "true";
		//document.getElementById("seuratEntNetPref").disabled=true;
		//jQuery(".seuratEntNetPref").attr('disabled', 'disabled');
	}
	if(extendedRoleHostGroupList == "[R#STR!CT#D]") {
		//document.getElementById("radioHostGroupName").disabled=true;
		//document.getElementById("hostGroupPref").disabled=true;
		jQuery(".radioHostGroupName").attr('disabled', 'disabled');
		jQuery(".hostGroupPref").attr('disabled', 'disabled');
	}
	if(entireNetworkPreference=="true"){
		document.getElementById("hostGroupPref").value="";
		document.getElementById("hostGroupPref").disabled=true;
		document.getElementById("seuratEntNetPref").disabled=false;
		document.getElementById("seuratEntNetPref").checked=true;
	} else {
		document.getElementById("hostGroupPref").value=hostGroupData;		
		document.getElementById("radioHostGroupName").checked=true;
	}
		
}
addWindowLoadEvent(seuratInit);
</script>
<form method="post" action="<portlet:actionURL/>">
<table border="0">
	<tr>
		<td style="width: 200px; padding-bottom:10px;">Select "Entire Network" to view all Hosts or enter HostGroup Name :</td>
		<td style="padding-bottom:10px;">
		<input type="radio" name="seuratEntNetPref" id="seuratEntNetPref" class="seuratEntNetPref" value="entireNetwork" checked="checked" onclick="getElementById('hostGroupPref').disabled=true;"/>Entire Network
		<br>
		<input type="radio" name="seuratEntNetPref" id="radioHostGroupName" class="radioHostGroupName" value="other" onclick="getElementById('hostGroupPref').disabled=false;">
		<input type="text" name='hostGroupPref' id='hostGroupPref' class="hostGroupPref"/>
		</td>
	</tr>
	<tr>
		<td>Enter Custom Portlet Title :</td>
		<td><input type="text" name='customPortletTitle'
			value='<%=renderRequest.getAttribute("customPortletTitle")%>' /></td>
	</tr>
	
	<tr>
		<td colspan="2"><input type="submit" value="Save Preferences"></td>
	</tr>
</table>

</form>
<script type="text/javascript">
// jQuery.noConflict();
jQuery(document).ready(function() {
	var data = "<%= referenceTree.getAllHostGroupNameList() %>".split(",");
	jQuery("#hostGroupPref").autocomplete(data);
  });
 </script>
