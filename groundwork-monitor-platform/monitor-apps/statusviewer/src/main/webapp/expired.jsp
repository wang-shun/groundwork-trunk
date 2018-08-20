<%@page contentType="text/html" pageEncoding="utf-8" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
    <title>Session Expired</title>
</head>
<body>
<%
    if (session != null) {
        try {
            session.invalidate();
        }
        catch (Exception e) {
        }
    }
    StringBuffer redirect = new StringBuffer();
    redirect.append(request.getScheme());
    redirect.append("://");
    redirect.append(request.getServerName());
    if (request.getServerPort() != 443 && request.getServerPort() != 80) {
        redirect.append(":");
        redirect.append(request.getServerPort());
    }
    response.sendRedirect(redirect.toString() + "/portal/classic/?portal:componentId=UIPortal&portal:action=Logout");
%>
</body>
</html>