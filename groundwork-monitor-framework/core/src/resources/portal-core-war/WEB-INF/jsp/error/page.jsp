<%@ page import="java.lang.Throwable" %>
<%@ page import="org.jboss.portal.common.util.Exceptions" %>

<h2 class="portlet-msg-error"><%= request.getAttribute("org.jboss.portal.control.ERROR_TYPE") %>
</h2>

<div class="portlet-font">Cause: <%= request.getAttribute("org.jboss.portal.control.CAUSE") %>
</div>
<%
   if (request.getAttribute("org.jboss.portal.control.MESSAGE") != null)
   {
%>
<div class="portlet-font">Message: <%= request.getAttribute("org.jboss.portal.control.MESSAGE") %>
</div>
<%
   }
%>
<div class="portlet-font">
   StackTrace: <%= Exceptions.toHTML((Throwable)request.getAttribute("org.jboss.portal.control.CAUSE")) %>
</div>