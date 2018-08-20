<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"   
    import="java.io.File,
    		java.lang.Object,
    		java.net.URL,
    		java.util.Properties,
    		java.io.InputStream,
    		java.io.FileInputStream,
    		org.groundwork.foundation.profiling.*"
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Profiler Tool</title>
<link rel="stylesheet" type="text/css" media="screen" href="css/style.css" />
<%
	String cmd = request.getParameter("cmd");
	if (cmd == null) 
	{
		response.sendRedirect("index.html");
	}
	
	if (cmd.equals("simplesa"))
	{
%>
<script type="text/javascript">
<!--
	function chgeValH ()
	{
		
		document.getElementById("ms.1.1.numHosts").value = document.getElementById("ms.1.1.numDevices").value;
	}

//-->
</script>		
<%	
	}
%>
</head>
<jsp:useBean id="oconfig" scope="session" class="org.groundwork.foundation.profiling.DOMUtil" />
<body>
<% 
    	//System.out.println("-----------1");
        //System.out.println("======text======"+textFromFile(this, "log4j.properties"));
        //System.out.println("-----------2");
	out.println(oconfig.getOutput(request, this));

%>
</body>
</html>

