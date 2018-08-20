<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ page import="java.util.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Groundwork @appserver_camelcase@ JDMA</title>
<script type="text/javascript" src="js/java-agent.js"></script>
<link type="text/css" href="css/java-agent.css" rel="Stylesheet" />
<style type="text/css" title="currentStyle">
@import "css/jdma_page.css";

@import "css/jdma_table.css";
</style>
</head>
<body>
<form method="POST"
	action="/gwos-@appserver_shortname@-monitoringAgent/GWOS@appserver_camelcase@Servlet">
<p align="center"><img align="top" src="images/gwlogo.gif"></p>
<p align="center">JDMA for @appserver_camelcase@</p>
<div id="controlbg">
<div class="controltop">
<div class="cornerul"></div>
<div class="cornerur"></div>
</div>
<div class="controlheader">Current @appserver_camelcase@ JDMA Configuration</div>
<div class="controlcontent">
<p>
<div id="container">
<table cellpadding="0" cellspacing="0" border="0" class="display"
	id="example" style="width: auto;">
	<thead>
		<th colspan='2'>Connection Settings</th>
	</thead>

	<%
		Object propObj = request.getSession().getAttribute(
				"currentProperties");
		if (propObj != null) {
			Properties props = (Properties) propObj;
			Enumeration e = props.propertyNames();
			ArrayList<String> statProps = new ArrayList<String>();
			ArrayList<String> dynProps = new ArrayList<String>();
			while (e.hasMoreElements()) {
				String key = (String) e.nextElement();
				if (key.indexOf(".") == -1)
					statProps.add(key);
				else
					dynProps.add(key);

			} // end while

			Collections.sort(statProps);
			for (int i = 0; i < statProps.size(); i++) {
				String key = statProps.get(i);
				String value = props.getProperty(key);
	%>
	<tr>
		<td><%=key%> :</td>
		<td><%=value%></td>
	</tr>
	<%
		} // end for
	%>
	<thead>
		<th colspan='2'>MBeans</th>
	</thead>
	<%
		Collections.sort(dynProps);
			for (int i = 0; i < dynProps.size(); i++) {
				String key = dynProps.get(i);
				String value = props.getProperty(key);
	%>
	<tr>
		<td><%=key%> :</td>
		<td><%=value%></td>
	</tr>
	<%
		}// end for
		} else {
			String message = "No configuration found!";
	%>
	<font class="error"><%=message%> </font>
	</p>
	<%
		}
	%>
</table>
<p align="center"><input type="button" value="Home"
	onclick="location.href='index.html'" class="button" /></p>
</div>
<input type="hidden" name="action" value="create_from_ui_conn_page" />
</form>
</body>
</html>
