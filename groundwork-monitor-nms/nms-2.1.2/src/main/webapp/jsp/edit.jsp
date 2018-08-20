<%@ page language="java" extends="org.jboss.portal.core.servlet.jsp.PortalJsp" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet" %>

<!--
/**
 * User: Chris Mills (millsy@jboss.com)
 * Date: 27-Feb-2006
 * Time: 22:02:11
 */
-->
<portlet:defineObjects/>

<form method="post" action="<portlet:actionURL/>">
	<table>
        <tr class="portlet-msg-alert">
            <td colspan="2"><%= request.getParameter("message") != null ?  request.getParameter("message") : ""%></td>
        </tr>
        <tr class="portlet-section-body">
			<td>Non IFrame Browser Message</td>
			<td><input type="text" name="noiframemessage" value="<%= request.getAttribute("iframemessage") %>" size="50"/></td>
		</tr>
        <tr class="portlet-section-body">
			<td>Source URL</td>
			<td><input type="text" name="url" value="<%= request.getAttribute("iframeurl") %>" size="50"/></td>
		</tr>
        <tr class="portlet-section-body">
			<td>Height (px)</td>
			<td><input type="text" name="height" value="<%= request.getAttribute("iframeheight") %>"/></td>
		</tr>
        <tr class="portlet-section-body">
			<td>Width (px or %)</td>
			<td><input type="text" name="width" value="<%= request.getAttribute("iframewidth") %>"/></td>
		</tr>
        <tr class="portlet-section-body">
			<td align="right"><input type="submit" name="op" value="Update"/></td>
            <td align="left"><input type="submit" name="op" value="Cancel"/></td>
        </tr>
    </table>
</form>