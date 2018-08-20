#!/usr/local/groundwork/bin/perl

print "Content-Type: text/html\n\n";

print qq{ 

 
<body>
<script>
function updateURL(newURL)
{
	
document.getElementById('theControlArea').src=newURL;
}
</script>
<span style="font-size:15px;font-weight:bold">Log Reporting Configuration</span>
 <table cellpadding='3'>
<tr>
 
<td bgcolor='d5d5d5'><a href="javascript:updateURL('/log-reporting/bin/ControlPanelSrv.pl?control=Database');">Database</a></td>
<td bgcolor='d5d5d5'><a href="javascript:updateURL('/log-reporting/bin/ControlPanelSrv.pl?control=LogDirectory');">Log Directories</a></td>
<td bgcolor='d5d5d5'><a href="javascript:updateURL('/log-reporting/bin/ControlPanelSrv.pl?control=LogFileType');">Log File Type</a></td>
<td bgcolor='d5d5d5'><a href="javascript:updateURL('/log-reporting/bin/ControlPanelSrv.pl?control=MessageType');">Message Type</a></td>
<td bgcolor='d5d5d5'><a href="javascript:updateURL('/log-reporting/bin/ControlPanelSrv.pl?control=MessageClass');">Message Class</a></td>
<td bgcolor='d5d5d5'><a href="javascript:updateURL('/log-reporting/bin/ControlPanelSrv.pl?control=MessageFilters');">Message Filters</a></td>
<td bgcolor='d5d5d5'><a href="javascript:updateURL('/log-reporting/bin/ControlPanelSrv.pl?control=Components');">Components</a></td>
</tr>
</table>

 
<iframe border='0' width='100%' height='100%' id='theControlArea' src='/log-reporting/bin/ControlPanelSrv.pl?control=Database'>test</iframe>
<div id='ControlDiv' src='/log-reporting/bin/ControlPanelSrv.pl?control=LogDirectory'></div>
</body>

};