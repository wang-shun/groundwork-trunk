<%@ page import="org.groundwork.foundation.reportserver.pagebeans.*" %>
<%
PageBean pageBean = new PageBean(pageContext.getServletContext());
String userAgent = request.getHeader("User-Agent");

// We don't allow access to Firefox 1.5.x user agents because of missing referer headers from request generated from dojo and BIRT
if (pageBean.isFirefox15Supported() == false && userAgent.indexOf("Firefox/1.5.") > 0) 
{
%>
	<html>
	<head>
		<link rel="stylesheet" type="text/css" media="all" href="styles/groundwork.css" />
	</head>
	<body>
		<br/>
		<H1>Sorry we don't currently support Firefox version 1.5.x because of security issues.<br /><br />
				Please check with your system administrator for supported browsers or if support for Firefox 1.5.x  is required.</H1>
	</body>
	</html>
<%
} else {
%>
<html>
<head>
	<link rel="stylesheet" type="text/css" media="all" href="styles/groundwork.css" />
</head>
<frameset cols="20%, *">
	<frame id="treeFrame" name="treeFrame" src="tree.jsp">
	<frame id="reportFrame" name="reportFrame" src="prompt.html">
	<NOFRAMES>
	<H1>Sorry your browser must support framesets</H1>
	</NOFRAMES>
</frameset>
</html>
<%
}
%>
