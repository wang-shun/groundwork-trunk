<%@ page import="org.groundwork.foundation.reportserver.pagebeans.*" %>
<% PageBean pageBean = new PageBean(pageContext.getServletContext()); %>
<html>
<head>
<link rel="stylesheet" type="text/css" media="all" href="styles/groundwork.css" />
<script type="text/javascript" src="scripts/dojo/dojo.js"></script>
<script type="text/javascript">
dojo.require("dojo.widget.*");
dojo.require("dojo.event.*");
dojo.require("dojo.io.*");
dojo.require("dojo.widget.Tree");
dojo.require("dojo.widget.TreeNode");
dojo.require("dojo.widget.TreeSelector");
dojo.require("dojo.widget.TreeLoadingController");


function treeSelectFired() 
{
   <!-- get a reference to the treeSelector and get the selected node -->
    var treeSelector = dojo.widget.manager.getWidgetById('treeSelector');
    var treeNode = treeSelector.selectedNode;

    var isFolder = treeNode['isFolder'];
    var widgetId = treeNode['widgetId'];
	var directory = (widgetId == "reportRoot") ? "" : widgetId;

	// Refresh report list frame   		
	parent.listFrame.location = "admin-reports.jsp?dir=" + directory;
}

function init() { 

    <!-- get a reference to the treeSelector -->
    var treeSelector = dojo.widget.manager.getWidgetById('treeSelector');
    var rootNode = dojo.widget.manager.getWidgetById('reportRoot');

	// Select the root node so it is highlighted before connecting the event
	// so we avoid a double refresh
	treeSelector.doSelect(rootNode);

    <!-- connect the select event to the function treeSelectFired() -->
    dojo.event.connect(treeSelector,'select','treeSelectFired'); 
}

dojo.addOnLoad(init);

</script>
</head>
<body>
<table width="100%"><tr><td>
<div dojoType="TreeLoadingController" RPCUrl="admin-directories.jsp" widgetId="treeController" DNDController="create"></div>
<div dojoType="TreeSelector" widgetId="treeSelector"></div>
<div dojoType="Tree" DNDMode="between" selector="treeSelector" widgetId="bandTree" controller="treeController">
<div dojoType="TreeNode" title="Reports" widgetId="reportRoot" objectId="root" isFolder="true" expandlevel="1" ></div>
</td></tr>
</table>
</br></br></br>

</body></html>
