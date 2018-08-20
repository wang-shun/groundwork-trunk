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
		<td colspan="2"><input type="text" name='hostPref' id="hostPref"
			value='<%=renderRequest.getAttribute("hostPref")%>' /></td>
	</tr>
	<tr>
		<td>Enter Custom Link 1 :</td>
		<td><input type="text" name='hostCustLink1'
			value='<%=renderRequest.getAttribute("hostCustLink1")%>' /></td>
		<td><img src="/portal-statusviewer/images/icon_help.gif"
			alt="Help"
			title="URL should be specified in this format: http://www.google.com. Formats other than this are invalid. User can use $HOST$ token in the URL like - http://$HOST$/sample_page.html" /></td>
	</tr>
	<tr>
		<td>Enter Custom Link 2 :</td>
		<td><input type="text" name='hostCustLink2'
			value='<%=renderRequest.getAttribute("hostCustLink2")%>' /></td>
		<td><img src="/portal-statusviewer/images/icon_help.gif"
			alt="Help"
			title="URL should be specified in this format: http://www.google.com. Formats other than this are invalid. User can use $HOST$ token in the URL like - http://$HOST$/sample_page.html" /></td>
	</tr>
	<tr>
		<td>Enter Custom Link 3 :</td>
		<td><input type="text" name='hostCustLink3'
			value='<%=renderRequest.getAttribute("hostCustLink3")%>' /></td>
		<td><img src="/portal-statusviewer/images/icon_help.gif"
			alt="Help"
			title="URL should be specified in this format: http://www.google.com. Formats other than this are invalid. User can use $HOST$ token in the URL like - http://$HOST$/sample_page.html" /></td>
	</tr>
	<tr>
		<td>Enter Custom Link 4 :</td>
		<td><input type="text" name='hostCustLink4'
			value='<%=renderRequest.getAttribute("hostCustLink4")%>' /></td>
		<td><img src="/portal-statusviewer/images/icon_help.gif"
			alt="Help"
			title="URL should be specified in this format: http://www.google.com. Formats other than this are invalid. User can use $HOST$ token in the URL like - http://$HOST$/sample_page.html" /></td>
	</tr>
	<tr>
		<td>Enter Custom Link 5 :</td>
		<td><input type="text" name='hostCustLink5'
			value='<%=renderRequest.getAttribute("hostCustLink5")%>' /></td>
		<td><img src="/portal-statusviewer/images/icon_help.gif"
			alt="Help"
			title="URL should be specified in this format: http://www.google.com. Formats other than this are invalid. User can use $HOST$ token in the URL like - http://$HOST$/sample_page.html" /></td>
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
	jQuery("#hostPref").autocomplete(data);
  });
 </script>