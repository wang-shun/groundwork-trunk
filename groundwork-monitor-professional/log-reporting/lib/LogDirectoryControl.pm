#
#Copyright 2007 GroundWork Open Source, Inc. ("GroundWork")
#All rights reserved. Use is subject to GroundWork commercial license terms. 
#
package LogDirectoryControl;
use LogDirectory;
 # 
 # printLogDirectoryCtl(): print Log Directory Control Panel
 #
 
sub draw{
 	$isValid = shift;
 print qq{

 <!--Log Directory Control -->
 };
 unless($isValid){
 	print "<div id=msgArea style='padding-left:30pt'><font color=ff0000><b><P/>ERROR: The Directory does not exist!</b></font></div>";	
	 
 }
 
 print qq{
 	<h1>Warning: If you re-add a directory after deleting it, you will need to perform a log rotate on the files in that directory to a
void duplicating log messages</h1>
 	
 	<table id="logReporting" border="0" cellpadding="0" cellspacing="0"> 
 
	<tr   class='windowHeader'><td  class='windowHeader' colspan='2'>Log Directories</td></tr>
<tr>
<td>
 <!-- form onsubmit=nop(); name='LogDirectoryForm' -->
 <input type='hidden' name='struct' value='LogDirectory'></input>
 <input type='hidden' name='action' value='add'></input>
 <table>
 <tr>
 <td><input type='text' onkeypress="return check_submit(event,this.form,'LogDirectory','add',document.getElementById('newLogDirectory').value);" id='newLogDirectory' name='newLogDirectory' size='40'></input>
 <input value='Add' type='button' onclick="javascript:sendDataReq('LogDirectory','add',document.getElementById('newLogDirectory').value);"></input></td></tr>
 <tr>
 <td>
   </td></tr></table> <!--- why? -->
 <!-- /form -->
 <form name='LogDirectoryDel'   >
 <select  style="width: 250px;"  size='5' name='selection'>
 };
@dirs = ();
@dirs = LogDirectory::getLogDirectories();
$dirCount = @dirs;
if($dirCount == 0){
	print "<option>&lt;&lt;Please specify at least one directory&gt;&gt;</option>\n";
}
else{
 	foreach $directory(@dirs){
 	print "<option value='$directory->{id}'> $directory->{dirname} </option>\n";
 	}
}
 print qq{
 </select>

<input value='Delete' type='button' onclick="javascript:sendDataReq('LogDirectory','del',document.forms.LogDirectoryDel.selection.value);"></input></form></td></tr>
 
   
   
 </table>
  
 
 	
  
 };
 }
1;