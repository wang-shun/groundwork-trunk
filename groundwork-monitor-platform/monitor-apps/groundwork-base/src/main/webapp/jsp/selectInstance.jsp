<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core"%>


<!--
/**
 * Modified By: Arul Shanmugam
 * Date: 03/11/2010
 * Time: 11:50
 * This JSP displays iframe in the portlet. The height of the iframe from the user pref is considered only for the webpage portlet due to javascript limitation.
 * But for all internal portlets we use jQuery's autoheight plugin.
 */
-->
<portlet:defineObjects />
<form method="post" action="<portlet:actionURL/>">
    <h4 style="
    margin: 30px;
">More than one instance of cacti found. Please select the instance you see(Note : Your selection is valid for this session only. To select a different server instance, please logout and login again.) : </h4>
<table width="20%" style="
    margin: 30px;
">
	<tr>
		<td style="font-weight: bold;">Server Instance :</td>
		<td><select name="selectedServerInstance">
			<c:forEach items="${serverList}" var="server">
				<option value='<c:out value="${server}" />'><c:out
					value="${server}" /></option>
			</c:forEach>
		</select></td>
		<td align="right"><input type="submit" name="op" value="Go" style="background-color: #e66a1f;" /></td>
	</tr>
</table>
</form>