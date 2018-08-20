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
			<input type="radio" name="nodeType" class="radioEntireNetwork_HLP" id="radioEntireNetwork_HLP" value="Network" checked="checked"  onclick="document.getElementById('hostGroupPref').disabled=true;" /> Entire Network 
			<input type="radio"	name="nodeType" id="radioHostGroup_HLP" class="radioHostGroup_HLP" value="Host Group" onclick="doAutoComplete();" /> Host Group
		</td>
	</tr>
	<tr>
		<td>Enter HostGroup Name :</td>
		<td><input type="text" name='hostGroupPref' id='hostGroupPref' class="hostGroupPref_HLP"
			value='<%=renderRequest.getAttribute("hostGroupPref")%>' /></td>
	</tr>
	<tr>
		<td>
			Do not show Hosts in state:
		</td>
		<td> <input type="checkbox" name="hostFilterUP" value="true" checked/> UP</td>
	</tr>
	
	<tr>
    <td></td>
    <td>
      <input type="checkbox" name="hostFilterDOWNUNSCHEDULED" value="true" checked/> DOWN (unscheduled)
    </td>
  </tr>
  
  <tr>
    <td></td>
    <td>
      <input type="checkbox" name="hostFilterDOWNSCHEDULED" value="true" checked/> DOWN (scheduled)
    </td>
  </tr>
  
  <tr>
    <td></td>
    <td>
      <input type="checkbox" name="hostFilterUNREACHABLE" value="true" checked/> UNREACHABLE
    </td>
  </tr>
  
  <tr>
    <td></td>
    <td>
      <input type="checkbox" name="hostFilterPENDING" value="true" checked/> PENDING
    </td>
  </tr>
  
  <tr>
    <td>Do not show acknowledged hosts:</td>
    <td>
      <input type="checkbox" name="hostFilterACKNOWLEDGED" value="true" checked/>
    </td>
  </tr>
  
	<tr>
		<td>Hosts per page :</td>
		<td>
			<input type="text" name='hostsPerPage' value='<%=renderRequest.getAttribute("hostsPerPage")%>' />
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
var data = "<%= referenceTree.getAllHostGroupNameList() %>".split(",");
var extendedRoleHostGroupList = "<%= referenceTree.getExtendedRoleHostGroupList()%>";


function hostListInit(){
	// jQuery.noConflict();
		// restore the node type
		//alert("host list");
		if(extendedRoleHostGroupList == "[R#STR!CT#D]") {
		//document.getElementById("radioHostGroup_HLP").disabled=true;
		jQuery(".radioHostGroup_HLP").attr('disabled', 'disabled');
		//document.getElementById("hostGroupPref").disabled=true;
		jQuery(".hostGroupPref_HLP").attr('disabled', 'disabled');
	}
	// restore the node type
	var radios = document.getElementsByName('nodeType');
	var nodeType = "<%=renderRequest.getAttribute("nodeType")%>";

	// if empty, list.toString() returns empty parantheses "[]"
	if(extendedRoleHostGroupList != "[]" && data=="") {
		nodeType = "Host Group";
		//document.getElementById("radioEntireNetwork_HLP").disabled=true;
		//jQuery(".radioEntireNetwork_HLP").attr('disabled', 'disabled');
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
	// restore checkbox value for 'show UP hosts' filter
  var filterSelectedUPValue = "<%=renderRequest.getAttribute("hostFilterUP")%>";
  var filterCheckboxUP = document.getElementsByName('hostFilterUP');
  if(filterSelectedUPValue == 'true') {   
    filterCheckboxUP[0].checked=true;   
  } else {
    filterCheckboxUP[0].checked=false;    
  }
	
	// restore checkbox value for 'show DOWN (unscheduled) hosts' filter
	var filterSelectedDOWNUNSCHEDULEDValue = "<%=renderRequest.getAttribute("hostFilterDOWNUNSCHEDULED")%>";
  var filterCheckboxDOWNUNSCHEDULED = document.getElementsByName('hostFilterDOWNUNSCHEDULED');
  if(filterSelectedDOWNUNSCHEDULEDValue == 'true') {   
	  filterCheckboxDOWNUNSCHEDULED[0].checked=true;   
  } else {
	  filterCheckboxDOWNUNSCHEDULED[0].checked=false;    
  }
  
  // restore checkbox value for 'show DOWN (scheduled) hosts' filter
  var filterSelectedDOWNSCHEDULEDValue = "<%=renderRequest.getAttribute("hostFilterDOWNSCHEDULED")%>";
  var filterCheckboxDOWNSCHEDULED = document.getElementsByName('hostFilterDOWNSCHEDULED');
  if(filterSelectedDOWNSCHEDULEDValue == 'true') {   
    filterCheckboxDOWNSCHEDULED[0].checked=true;   
  } else {
    filterCheckboxDOWNSCHEDULED[0].checked=false;    
  }

  // restore checkbox value for 'show UNREACHABLE hosts' filter
  var filterSelectedUNREACHABLEValue = "<%=renderRequest.getAttribute("hostFilterUNREACHABLE")%>";
  var filterCheckboxUNREACHABLE = document.getElementsByName('hostFilterUNREACHABLE');
  if(filterSelectedUNREACHABLEValue == 'true') {   
    filterCheckboxUNREACHABLE[0].checked=true;   
  } else {
    filterCheckboxUNREACHABLE[0].checked=false;    
  }
  
  // restore checkbox value for 'show PENDING hosts' filter
  var filterSelectedPENDINGValue = "<%=renderRequest.getAttribute("hostFilterPENDING")%>";
  var filterCheckboxPENDING = document.getElementsByName('hostFilterPENDING');
  if(filterSelectedPENDINGValue == 'true') {   
    filterCheckboxPENDING[0].checked=true;   
  } else {
    filterCheckboxPENDING[0].checked=false;    
  }

  // restore checkbox value for 'show acknowledged hosts' filter
  var filterSelectedACKNOWLEDGEDValue = "<%=renderRequest.getAttribute("hostFilterACKNOWLEDGED")%>";
  var filterCheckboxACKNOWLEDGED = document.getElementsByName('hostFilterACKNOWLEDGED');
  if(filterSelectedACKNOWLEDGEDValue == 'true') {   
    filterCheckboxACKNOWLEDGED[0].checked=true;   
  } else {
    filterCheckboxACKNOWLEDGED[0].checked=false;    
  }

}

addWindowLoadEvent(hostListInit);

function doAutoComplete() {
	// jQuery.noConflict();
	document.getElementById('hostGroupPref').disabled=false;
	var radios = document.getElementsByName('nodeType');
	if(radios[1].checked == true) {
		jQuery(document).ready(function() {
			jQuery(".hostGroupPref_HLP").autocomplete(data);
		});
	 
	}  

	}



 </script>
