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
			<input type="radio" name="nodeType" id="radioEntireNetwork_ELP" class="radioEntireNetwork_ELP" value="Network" onclick="radioEntireNetworkSelected();" /> Entire Network 
			<input type="radio"	name="nodeType" id="radioHostGroup_ELP" class="radioHostGroup_ELP" value="Host Group" checked="checked" onclick="doAutoComplete();" /> Host Group
			<input type="radio" name="nodeType" id="radioServiceGroup_ELP" class="radioServiceGroup_ELP" value="Service Group" onclick="doAutoComplete();" /> Service Group 
			<input type="radio" name="nodeType" id="radioHost_ELP" class="radioHost_ELP" value="Host" onclick="doAutoComplete();" /> Host 
			<input type="radio" name="nodeType" id="radioService_ELP" class="radioService_ELP" value="Service" onclick="radioServiceSelected();" /> Service
		</td>
	</tr>

	<tr id="nodeNameRow" style="display:table-row">
		<td>Enter Name :</td>
		<td>
			<input type="text" name="nodeName" id="nodeName" class="nodeName_ELP" value="<%=renderRequest.getAttribute("nodeName")%>" />
		</td>
	</tr>
	
	<tr id="serviceEventsHostNameRow" style="display:none">
		<td>Enter Host Name :</td>
		<td>
			<input type="text" name="serviceHostPref" id="serviceHostPref" class="serviceEventsHostName_ELP" 
				value="<%=renderRequest.getAttribute("serviceHostPref")%>" 
				onblur="populateServices();"/>
		</td>
	</tr>


	<tr id="serviceEventsServiceNameRow" style="display:none">
		<td>Enter Service Name :</td>
		<td>
			<input type="text" name="servicePref" id="servicePref" class="serviceEventsServiceName_ELP" 
				value="<%=renderRequest.getAttribute("servicePref")%>" />
		</td>
	</tr>
	
	<tr>
		<td>Events per page :</td>
		<td><input type="text" name='eventsPerPage'
			value='<%=renderRequest.getAttribute("eventsPerPage")%>' /></td>
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

var hostListData = "<%= referenceTree.getAllHostNameList() %>".split(",");
var serviceGroupData = "<%= referenceTree.getAllServiceGroupNameList() %>".split(",");
var hostGroupData = "<%= referenceTree.getAllHostGroupNameList() %>".split(",");
var extendedRoleHostGroupList = "<%= referenceTree.getExtendedRoleHostGroupList()%>";
var extendedRoleServiceGroupList = "<%= referenceTree.getExtendedRoleServiceGroupList()%>";
var hostServiceDataArray = "<%= referenceTree.getAllServicesWithHostName() %>".split(",");

function eventPortletInit(){
	
	// if empty, list.toString() returns empty parantheses "[]"
	/* if(extendedRoleHostGroupList != "[]" || extendedRoleServiceGroupList != "[]") {		
		jQuery(".radioEntireNetwork_ELP").attr('disabled', 'disabled');
	} */
	// check if service groups are restricted
	if(extendedRoleServiceGroupList == "[R#STR!CT#D]") {
		jQuery(".radioServiceGroup_ELP").attr('disabled', 'disabled');
	}
	// check if host groups are restricted
	if(extendedRoleHostGroupList == "[R#STR!CT#D]") {
		jQuery(".radioHostGroup_ELP").attr('disabled', 'disabled');
		jQuery(".radioHost_ELP").attr('disabled', 'disabled');
	}

	document.getElementById('nodeName').disabled=false;
	// restore the node type
	var radios = document.getElementsByName('nodeType');
	var nodeType = "<%=renderRequest.getAttribute("nodeType")%>";

	if(nodeType == 'Network') {
		radios[0].checked='true';
		document.getElementById('nodeName').disabled=true;
		
	} else if(nodeType == 'Host Group') {
		radios[1].checked='true';
		jQuery(document).ready(function() {
	    	jQuery("#nodeName").autocomplete(hostGroupData);
		});
	} else if(nodeType == 'Service Group') {
		radios[2].checked='true';
		jQuery(document).ready(function() {
	    	jQuery("#nodeName").autocomplete(serviceGroupData);
		});
	} else if(nodeType == 'Host') {
		radios[3].checked='true';
		jQuery(document).ready(function() {
	    	jQuery("#nodeName").autocomplete(hostListData);
		});
	} else if(nodeType == 'Service') {
		radios[4].checked='true';
		radioServiceSelected();
		populateServices();
	} 
	
}

addWindowLoadEvent(eventPortletInit);

function doAutoComplete() {
	
	radioServiceUnselected();

	document.getElementById('nodeName').disabled=false;

	var radios = document.getElementsByName('nodeType');
	if(radios[1].checked == true) {
		jQuery(document).ready(function() {
	    	jQuery("#nodeName").unautocomplete().autocomplete(hostGroupData);
		});
	 
	} else if(radios[2].checked == true) {
		jQuery(document).ready(function() {
	    	jQuery("#nodeName").unautocomplete().autocomplete(serviceGroupData);
		});  
	
	} else if(radios[3].checked == true) {
		jQuery(document).ready(function() {
	    	jQuery("#nodeName").unautocomplete().autocomplete(hostListData);
		});  
	}
}

function radioServiceSelected() {
	var nodeNameRow = document.getElementById("nodeNameRow");
	nodeNameRow.style.display = 'none';	

	var serviceEventsHostNameRow= document.getElementById("serviceEventsHostNameRow");
	serviceEventsHostNameRow.style.display = 'table-row';

	var serviceEventsServiceNameRow= document.getElementById("serviceEventsServiceNameRow");
	serviceEventsServiceNameRow.style.display = 'table-row';

	// populate auto-complete data for host
	jQuery(document).ready(function() {
    	jQuery("#serviceHostPref").unautocomplete().autocomplete(hostListData);
	});
}

function radioServiceUnselected() {
	var nodeNameRow = document.getElementById("nodeNameRow");
	nodeNameRow.style.display = 'table-row';	

	var serviceEventsHostNameRow= document.getElementById("serviceEventsHostNameRow");
	serviceEventsHostNameRow.style.display = 'none';

	var serviceEventsServiceNameRow= document.getElementById("serviceEventsServiceNameRow");
	serviceEventsServiceNameRow.style.display = 'none';
}

function radioEntireNetworkSelected() {
	document.getElementById('nodeName').disabled=true; 
	radioServiceUnselected();
}

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
