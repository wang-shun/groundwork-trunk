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
<%@ page
	import="com.groundworkopensource.portal.statusviewer.handler.ReferenceTreeMetaModel"%>
<jsp:useBean id="referenceTree" scope="application"
	class="com.groundworkopensource.portal.statusviewer.handler.ReferenceTreeMetaModel" />

<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>
<portlet:defineObjects />
<form method="post" action="<portlet:actionURL/>">
<table border="0">
	<tr>
		<td>Enter Host Name :</td>
		<td><input type="text" name='serviceHostPref'
			id='serviceHostPref'
			value='<%=renderRequest.getAttribute("serviceHostPref")%>' /></td>
	</tr>

	<tr>
		<td>Enter Service Name :</td>
		<td><input type="text" name='servicePref' id='servicePref'
			value='<%=renderRequest.getAttribute("servicePref")%>'
			onfocus="populateServices();" /></td>
	</tr>

	<tr>
		<td>Enter Custom Portlet Title :</td>
		<td><input type="text" name='customPortletTitle'
			value='<%=renderRequest.getAttribute("customPortletTitle")%>' /></td>
	</tr>
	<tr>
		<td>Time :</td>
		<td><select size="1" id="perfMeasurementPortlet_menuTimeSelector"
			name='timepref' onchange="hideShowCustomDates();"  >
			<option value="1" selected="selected">Today</option>
			<option value="24">Last 24 Hours</option>
			<option value="48">Last 48 Hours</option>
			<option value="120">Last 5 Days</option>
			<option value="168">Last 7 Days</option>
			<option value="30">Last 30 Days</option>
			<option value="90">Last 90 Days</option>
			<option value="-1">Custom Date-Time</option>
		</select></td>
		<td>
		<div id="customDatesDiv" style="visibility:hidden;">
		<div style="float: left;">Start Date: <input type="text"
			 id="custStartDatePref" name="custStartDatePref" value='<%=renderRequest.getAttribute("custStartDatePref")%>' /><img
			src="/portal-statusviewer/images/date-time.png" alt="Start Date"
			title="Start Date" onclick="datePicker(this);"
			onLoad="datePicker(this);" /></div>

		<div style="float: left;">End Date:<input type="text" 
			id="custEndDatePref" name="custEndDatePref" value='<%=renderRequest.getAttribute("custEndDatePref")%>'  /><img
			src="/portal-statusviewer/images/date-time.png" alt="Start Date"
			title="Start Date" onclick="datePicker(this);"
			onLoad="datePicker(this);" /></div>
		</div>
		</td>
	</tr>
	<tr>
		<td colspan="2"><input type="submit" value="Save Preferences"></td>
	</tr>

</table>
</form>

<script type="text/javascript">
// jQuery.noConflict();
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

window.onload=function(){
	var timepref = "<%=renderRequest.getAttribute("timepref")%>";
	//alert(timepref);
	var myselect=document.getElementById('perfMeasurementPortlet_menuTimeSelector');
	for (var i=0; i<myselect.options.length; i++){
		if(timepref == myselect.options[i].value){
		myselect.options[i].selected='true';
		if(timepref == "-1")
		{
			document.getElementById('customDatesDiv').style.visibility = 'visible';
			
		}
		 break;
		}
	 
	
}
}
</script>
