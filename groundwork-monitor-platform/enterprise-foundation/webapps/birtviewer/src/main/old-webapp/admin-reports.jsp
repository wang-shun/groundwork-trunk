<%@ page import="org.groundwork.foundation.reportserver.pagebeans.*, java.util.*, java.io.*, java.text.*" %>
<%
String directory = request.getParameter("dir");

ReportPB pageBean = new ReportPB(pageContext.getServletContext());
List reportList = pageBean.getDirectoryFiles(directory, false);
%>

<html>
<head>
	<link rel="stylesheet" type="text/css" media="all" href="styles/groundwork.css" />
</head>
<body>
<form method="post" action="fileupload" name="submit" enctype="multipart/form-data">
	<input type="hidden" name="redirectURL" value="admin-reports.jsp?t=<%= System.currentTimeMillis() %>&dir=<%= (directory == null) ? "" : directory %>">
	<input type="hidden" name="relativeDir" value="<%= (directory == null) ? "" : directory %>">

<table border="0">
<tr><td colspan="2" style="font-size : 9pt; font-weight : bold">In order to publish a report, please click the browse button and select a report file to publish.</br>
			Once you have selected the report file you would like to publish click the "Publish" button to upload the report to the currently selected directory in the tree.</br>
			The report published will immediately be available for viewing.</td></tr>
<tr><td colspan="2">
	<div id="divPublish" style="border : solid 2px #ffffff; visibility : visible">
		<table border="0">
			<tr><td><strong>Report file to publish (*.rptdesign):</strong></td>
				<td><input type="file" name="reportfile" size="70"></td>
				<td><input type="submit" name="submit" value="Publish"></td>
			</tr>
		</table>
	</div>
	</td>
</tr></table>
</form>
<div style="height : 500px; overflow : auto; ">

	<table border="0" cellspacing="1" cellpadding="5" width="100%">
	<tr><td><H1>Report</H1></td><td><H1>Date Published</H1></td></tr>
<%
	if (reportList != null && reportList.size() > 0) {
		Iterator it = reportList.iterator();
		
		SimpleDateFormat format = new SimpleDateFormat("MM-dd-yyyy 'at' hh:mm aaa");
		
		while (it.hasNext())
		{
			File file = (File)it.next();
			
			Date publishDate = new Date(file.lastModified());
			
%>
		<tr><td><%= file.getName() %></td><td><%= format.format(publishDate) %></td></tr>
<%
		}
	}
	else {
%>
		<tr><td>No reports in the current directory</td></tr>
<%
	}
%>
</table></div>
</body></html>