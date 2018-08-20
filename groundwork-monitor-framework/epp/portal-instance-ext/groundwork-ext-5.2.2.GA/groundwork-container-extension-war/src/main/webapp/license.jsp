<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Insert title here</title>
</head>
<script language="javascript">
redirTime = "1000";
redirURL = '<%= request.getAttribute("frompage") %>';
self.setTimeout("self.location.href = redirURL;",redirTime);
</script>
<body>
Your session license has been validated for user, '<%= request.getUserPrincipal().getName() %>'!
<%
request.getSession().setAttribute("hasbeenvalidated","true");
%>
<br/>
</body>
</html>