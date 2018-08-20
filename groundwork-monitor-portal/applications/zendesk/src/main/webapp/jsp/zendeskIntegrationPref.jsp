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
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %> 
<portlet:defineObjects/>

<div style="background-color: #FFFF99; font-weight:11px;border-style:solid;border-width:1px;border-color:#98bf21;">Please enter below the credential for your Zendesk account. If you don't have an account <a href="http://www.zendesk.com/signup/?cpao=gwos&cpca=banner&utm_medium=referral" target="_blank">click here</a> to sign up for a zendesk account.</div>
<br/>

<form method="post" action="<portlet:actionURL/>">
<table border="0">
	<tr>
		<td>User Id :</td>
		<td><input type="text" size="50" name='useridPref' id='useridPref'
			value='<%=renderRequest.getAttribute("useridPref")%>' /></td>
	</tr>
    <tr>
        <td>Password :</td>
        <td><input type="password" size="50" name='pwdPref' id='pwdPref'
            value='<%=renderRequest.getAttribute("pwdPref")%>' /></td>
    </tr>
	<tr>
		<td>Token :</td>
		<td><input type="text" size="50" name='zendeskToken' id='zendeskToken'
			value='<%=renderRequest.getAttribute("zendeskToken")%>' /></td>
	</tr>
	<tr>
		<td>Zendesk-Url :</td>
		<td><input type="text" size="50" name='zendeskUrlPref'
			value='<%=renderRequest.getAttribute("zendeskUrlPref")%>' /></td>
	</tr>
	
	<tr>
		<td colspan="2"><input type="submit" value="Save Preferences"></td>
	</tr>
</table>

</form>

