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
		<td>Select Entity :</td>
		<td>
			<input type="radio" name="nodeType" id="radioEntireNetwork_SLP" class="radioEntireNetwork_SLP" value="Network" onclick="document.getElementById('gw_nodeName').disabled=true;" /> Entire Network
			<input type="radio" name="nodeType" id="radioHost_SLP" class="radioHost_SLP" value="Host" checked="checked" onclick="doAutoComplete();" /> Host 
			<input type="radio" name="nodeType" id="radioServiceGroup_SLP" class="radioServiceGroup_SLP" value="Service Group" onclick="doAutoComplete();" /> Service Group 
			<input type="radio"	name="nodeType" id="radioHostGroup_SLP" class="radioHostGroup_SLP" value="Host Group" onclick="doAutoComplete();" /> Host Group
		</td>
	</tr>

	<tr>
		<td>Enter Name :</td>
		<td>
			<input type="text" name="gw_nodeName" id="gw_nodeName" class="nodeName_SLP" value="<%=renderRequest.getAttribute("gw_nodeName")%>" />
		</td>
	</tr>
	<tr>
		<td>
       Do not show services in state:
		</td>
		<td><input type="checkbox" name="serviceFilterOK" value="true" checked /> OK</td>
	</tr>
  <tr>
    <td>
    </td>
    <td><input type="checkbox" name="serviceFilterWARNING" value="true" checked /> WARNING</td>
  </tr>
  <tr>
    <td>
    </td>
    <td><input type="checkbox" name="serviceFilterCRITICAL" value="true" checked /> CRITICAL</td>
  </tr>
  <tr>
    <td>
    </td>
    <td><input type="checkbox" name="serviceFilterCRITICALscheduled" value="true" checked /> CRITICAL (scheduled)</td>
  </tr>
  <tr>
    <td>
    </td>
    <td><input type="checkbox" name="serviceFilterCRITICALunscheduled" value="true" checked /> CRITICAL (unscheduled)</td>
  </tr>
  <tr>
    <td>
    </td>
    <td><input type="checkbox" name="serviceFilterUNKNOWN" value="true" checked /> UNKNOWN</td>
  </tr>
  <tr>
    <td>
    </td>
    <td><input type="checkbox" name="serviceFilterPENDING" value="true" checked /> PENDING</td>
  </tr>
  <tr>
    <td>Do not show acknowledged services:
    </td>
    <td><input type="checkbox" name="serviceFilterACKNOWLEDGED" value="true" checked /></td>
  </tr>

	<tr>
		<td>Services per page :</td>
		<td>
			<input type="text" name='servicesPerPage' value='<%=renderRequest.getAttribute("servicesPerPage")%>' />
		</td>
	</tr>
	
	<tr>
		<td>Enter Custom Portlet Title :</td>
		<td><input type="text" name='customPortletTitle'
			value='<%=renderRequest.getAttribute("customPortletTitle")%>' /></td>
	</tr>
	
	<tr>
		<td colspan="2">
			<input type="submit" value="Save Preferences">
		</td>
	</tr>

</table>

</form>

<script type="text/javascript">
var hostListData = "<%= referenceTree.getAllHostNameList() %>".split(",");
var serviceGroupData = "<%= referenceTree.getAllServiceGroupNameList() %>".split(",");
var hostGroupData = "<%= referenceTree.getAllHostGroupNameList() %>".split(",");
var extendedRoleHostGroupList = "<%= referenceTree.getExtendedRoleHostGroupList()%>";
var extendedRoleServiceGroupList = "<%= referenceTree.getExtendedRoleServiceGroupList()%>";



function serviceListInit(){
		
	// if empty, list.toString() returns empty parantheses "[]"
	if(extendedRoleHostGroupList != "[]" || extendedRoleServiceGroupList != "[]") {		
		//document.getElementById("radioEntireNetwork_SLP").disabled=true;
		//jQuery(".radioEntireNetwork_SLP").attr('disabled', 'disabled');
	}
	// check if service groups are restricted
	if(extendedRoleServiceGroupList == "[R#STR!CT#D]") {
		//document.getElementById("radioServiceGroup_SLP").disabled=true;
		jQuery(".radioServiceGroup_SLP").attr('disabled', 'disabled');
	}
	// check if host groups are restricted
	if(extendedRoleHostGroupList == "[R#STR!CT#D]") {
		//document.getElementById("radioHostGroup_SLP").disabled=true;
		//document.getElementById("radioHost_SLP").disabled=true;
		jQuery(".radioHostGroup_SLP").attr('disabled', 'disabled');
		jQuery(".radioHost_SLP").attr('disabled', 'disabled');
	}

	document.getElementById('gw_nodeName').disabled=false;
	// restore the node type
	var radios = document.getElementsByName('nodeType');
	var nodeType = "<%=renderRequest.getAttribute("nodeType")%>";

	if(nodeType == 'Network') {
		radios[0].checked='true';
		document.getElementById('gw_nodeName').disabled=true;
		
	} else if(nodeType == 'Host') {
		radios[1].checked='true';
		jQuery(document).ready(function() {
	    	jQuery("#gw_nodeName").autocomplete(hostListData);
		});
	} else if(nodeType == 'Service Group') {
		radios[2].checked='true';
		jQuery(document).ready(function() {
	    	jQuery("#gw_nodeName").autocomplete(serviceGroupData);
		});
	} else if(nodeType == 'Host Group') {
		radios[3].checked='true';
		jQuery(document).ready(function() {
	    	jQuery("#gw_nodeName").autocomplete(hostGroupData);
		});
	}

	  // restore checkbox value for 'show non OK services' filter
	  var filterOkValue = "<%=renderRequest.getAttribute("serviceFilterOK")%>";
	  var filterOkCheckbox = document.getElementsByName('serviceFilterOK');
	  if(filterOkValue == 'true') {   
	    filterOkCheckbox[0].checked=true;
	  } else {
		  filterOkCheckbox[0].checked=false;
	  }
	  // restore checkbox value for 'show non WARNING services' filter
	  var filterWarningValue = "<%=renderRequest.getAttribute("serviceFilterWARNING")%>";
	  var filterWarningCheckbox = document.getElementsByName('serviceFilterWARNING');
	  if(filterWarningValue == 'true') {   
		  filterWarningCheckbox[0].checked=true;
	  } else {
		  filterWarningCheckbox[0].checked=false;
	  }
	  // restore checkbox value for 'show non CRITICAL services' filter
	  var filterCriticalValue = "<%=renderRequest.getAttribute("serviceFilterCRITICAL")%>";
	  var filterCriticalCheckbox = document.getElementsByName('serviceFilterCRITICAL');
	  if(filterCriticalValue == 'true') {   
		  filterCriticalCheckbox[0].checked=true;
	  } else {
		  filterCriticalCheckbox[0].checked=false;
	  }
	  // restore checkbox value for 'show non CRITICAL scheduled services' filter
	  var filterCriticalScheduledValue = "<%=renderRequest.getAttribute("serviceFilterCRITICALscheduled")%>";
	  var filterCriticalScheduledCheckbox = document.getElementsByName('serviceFilterCRITICALscheduled');
	  if(filterCriticalScheduledValue == 'true') {   
		  filterCriticalScheduledCheckbox[0].checked=true;
	  } else {
		  filterCriticalScheduledCheckbox[0].checked=false;
	  }
	  // restore checkbox value for 'show non CRITICAL unscheduled services' filter
	  var filterCriticalUnscheduledValue = "<%=renderRequest.getAttribute("serviceFilterCRITICALunscheduled")%>";
	  var filterCriticalUnscheduledCheckbox = document.getElementsByName('serviceFilterCRITICALunscheduled');
	  if(filterCriticalUnscheduledValue == 'true') {   
		  filterCriticalUnscheduledCheckbox[0].checked=true;
	  } else {
		  filterCriticalUnscheduledCheckbox[0].checked=false;
	  }
    // restore checkbox value for 'show non UNKNOWN services' filter
    var filterUnknownValue = "<%=renderRequest.getAttribute("serviceFilterUNKNOWN")%>";
    var filterUnknownCheckbox = document.getElementsByName('serviceFilterUNKNOWN');
    if(filterUnknownValue == 'true') {   
      filterUnknownCheckbox[0].checked=true;
    } else {
      filterUnknownCheckbox[0].checked=false;
    }
	  // restore checkbox value for 'show non PENDING services' filter
    var filterPendingValue = "<%=renderRequest.getAttribute("serviceFilterPENDING")%>";
    var filterPendingCheckbox = document.getElementsByName('serviceFilterPENDING');
    if(filterPendingValue == 'true') {   
      filterPendingCheckbox[0].checked=true;
    } else {
      filterPendingCheckbox[0].checked=false;
    }
    // restore checkbox value for 'Do not show acknowledged services' filter
    var filterAcknowledgedValue = "<%=renderRequest.getAttribute("serviceFilterACKNOWLEDGED")%>";
    var filterAcknowledgedCheckbox = document.getElementsByName('serviceFilterACKNOWLEDGED');
    if(filterAcknowledgedValue == 'true') {   
      filterAcknowledgedCheckbox[0].checked=true;
    } else {
      filterAcknowledgedCheckbox[0].checked=false;
    }
}

addWindowLoadEvent(serviceListInit);

function doAutoComplete() {

	document.getElementById('gw_nodeName').disabled=false;

	var radios = document.getElementsByName('nodeType');
	if(radios[1].checked == true) {
		jQuery(document).ready(function() {
	    	jQuery("#gw_nodeName").unautocomplete().autocomplete(hostListData);
		});
	 
	} else if(radios[2].checked == true) {
		jQuery(document).ready(function() {
	    	jQuery("#gw_nodeName").unautocomplete().autocomplete(serviceGroupData);
		});  
	
	} else if(radios[3].checked == true) {
		jQuery(document).ready(function() {
	    	jQuery("#gw_nodeName").unautocomplete().autocomplete(hostGroupData);
		});  
	}
}

</script>