<%@ page language="java"%>
<%@ page import="javax.portlet.RenderRequest"%>
<%@ page import="javax.portlet.RenderResponse"%>

<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>
<portlet:defineObjects />
<iframe WIDTH="100%" HEIGHT="2000" name="zendeskviewer"	id="zendeskviewer" frameborder="no" scrolling="auto"
	src="<%=renderRequest.getAttribute("zenURL")%>"></iframe>
