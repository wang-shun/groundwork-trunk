<%@page contentType="text/html; charset=iso-8859-1" language="java" session="true" %>
<%
/* retrive the server name from the URL */
 StringBuffer requestURL = request.getRequestURL();
 String requestURI = request.getRequestURI();
 String serverURL = requestURL.substring(requestURL.indexOf("//")+2,requestURL.indexOf(requestURI));
%>
ServerURL: <%=serverURL %>
<%
 /*Set an attribute on the session that will be read by the IFrameportlets */
 request.getSession().setAttribute("serverOfRequestURL", serverURL);
 response.sendRedirect(request.getContextPath() + "/portal/josso_login/");
%>
