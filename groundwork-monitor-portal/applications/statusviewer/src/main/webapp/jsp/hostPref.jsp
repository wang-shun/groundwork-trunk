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
		<td><input type="text" name='hostPref' id='hostPref' class="hostPref_Class"
			value='<%=renderRequest.getAttribute("hostPref")%>' /></td>
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

<script>
//var $j = jQuery.noConflict();

// Use jQuery via $j(...)
jQuery(document).ready(function() {
	var data = "<%= referenceTree.getAllHostNameList() %>".split(",");
	jQuery(".hostPref_Class").autocomplete(data);
}); 
	
</script>

