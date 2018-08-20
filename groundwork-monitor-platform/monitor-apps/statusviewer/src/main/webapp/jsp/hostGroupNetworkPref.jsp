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
		<td>Select Entity :</td>
		<td>
			<input type="radio" name="nodeType" id="radioEntireNetwork" class="radioEntireNetwork" value="Network" checked="checked"  onclick="document.getElementById('hostGroupPref').disabled=true;" /> Entire Network 
			<input type="radio"	name="nodeType" id="radioHostGroup" class="radioHostGroup" value="Host Group" onclick="doAutoComplete();" /> Host Group
		</td>
	</tr>
	<tr>
		<td>Enter HostGroup Name :</td>
		<td><input type="text" name='hostGroupPref' id='hostGroupPref' class="hostGroupPref" 
			value='<%=renderRequest.getAttribute("hostGroupPref")%>' /></td>
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
var data = "<%= referenceTree.getAllHostGroupNameList() %>".split(",");
var extendedRoleHostGroupList = "<%= referenceTree.getExtendedRoleHostGroupList()%>";

function hostGroupNetworkInit(){
	
	if(extendedRoleHostGroupList == "[R#STR!CT#D]") {
		//document.getElementById("radioHostGroup").disabled=true;
		//document.getElementById("hostGroupPref").disabled=true;
		jQuery(".radioHostGroup").attr('disabled', 'disabled');
		jQuery(".hostGroupPref").attr('disabled', 'disabled');
	}
	// restore the node type
	var radios = document.getElementsByName('nodeType');
	var nodeType = "<%=renderRequest.getAttribute("nodeType")%>";

	// if empty, list.toString() returns empty parantheses "[]"
	if(extendedRoleHostGroupList != "[]" && data=="") {
		nodeType = "Host Group";
		//document.getElementById("radioEntireNetwork").disabled=true;
		//jQuery(".radioEntireNetwork").attr('disabled', 'disabled');
	}
	
	 //alert(nodeType);
	if(nodeType == 'Network') {		
		radios[0].checked='true';
		document.getElementById('hostGroupPref').disabled=true;
		
	}else if(nodeType == 'Host Group') {
		radios[1].checked='true';
		jQuery(document).ready(function() {
	    	jQuery("#hostGroupPref").autocomplete(data);
		});
	}

	
}
addWindowLoadEvent(hostGroupNetworkInit);

function doAutoComplete() {
	// jQuery.noConflict();
	document.getElementById('hostGroupPref').disabled=false;
	var radios = document.getElementsByName('nodeType');
	if(radios[1].checked == true) {
		jQuery(document).ready(function() {
			jQuery("#hostGroupPref").autocomplete(data);
		});
	 
	}  

	}



 </script>
