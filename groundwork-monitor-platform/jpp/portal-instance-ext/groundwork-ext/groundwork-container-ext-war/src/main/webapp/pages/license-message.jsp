<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>
<%@ page
	import="org.exoplatform.portal.webui.util.Util,javax.servlet.http.HttpServletRequest,javax.servlet.http.HttpSession,org.exoplatform.portal.application.PortalRequestContext"%>
<portlet:defineObjects />
<%
	PortalRequestContext prContext = Util.getPortalRequestContext();
	HttpServletRequest servletRequest = prContext.getRequest(); 
	HttpSession servletSession = servletRequest.getSession();
	Object objFlag = servletSession.getAttribute("softlimitexceeded");
	if (objFlag != null) {
		boolean softlimitexceeded = Boolean.parseBoolean((String)objFlag);
		if (softlimitexceeded) {
%>
		<div class="GroundworkLicenseMessagePortlet UIHorizontalTabs"
			style="background-color: <%= servletSession.getAttribute("softlimitbgcolor") %>; color: <%= servletSession.getAttribute("softlimittxtcolor") %>;">
			&nbsp;Your GroundWork License
			<%= servletSession.getAttribute("softlimitmessage") %> Please call
			sales at +1 866-899-4342&nbsp;
		</div>
<% 		}
	}%>

