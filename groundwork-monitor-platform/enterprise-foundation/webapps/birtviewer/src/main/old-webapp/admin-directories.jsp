<%@ page contentType="text/json;charset=UTF-8" language="java" %>
<%@ page import="org.groundwork.foundation.reportserver.pagebeans.*, org.json.*" %>
<%
String action = request.getParameter("action");
String data = request.getParameter("data");

if (action != null && action.equalsIgnoreCase("getChildren"))
{
	String directory = null;
	if (data != null && data.length() > 0)
	{
		JSONObject json = new JSONObject(data);
		JSONObject node = json.getJSONObject("node");

		if (node != null) {
			boolean isFolder = node.getBoolean("isFolder");
			
			if (isFolder == true)
			{
				directory = node.getString("widgetId");
				
				// Note:  We expect root node's widgetId to be reportRoot (index.jsp)
				if (directory.equalsIgnoreCase("reportRoot") == true)
				{
					directory = null;
				}
						
				ReportPB pageBean = new ReportPB(pageContext.getServletContext());
								
				String output = pageBean.getDirectoryChildren(directory, true);

				out.write(output);
			}
		}
	}
}
%>
