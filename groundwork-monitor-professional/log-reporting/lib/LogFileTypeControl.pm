#!/usr/local/groundwork/bin/perl
#
#Copyright 2007 GroundWork Open Source, Inc. ("GroundWork")
#All rights reserved. Use is subject to GroundWork commercial license terms. 
#

package LogFileTypeControl;
use lib qq(/usr/local/groundwork/log-reporting/lib);

use LogFileType;
sub new {
  	my ($invocant) = @_;   
	my $class = ref($invocant) || $invocant;
	my $self = {
		name => "LFTControl"
	};
 	bless($self,$class);
	return $self;
}
 
sub draw{
	@ltypes = LogFileType::getLogfileTypeList();
	
	print qq{
	 <reportMessage>
	 <htmlData id='htmlData'>
	 <div id='LogfileType'>
	 <!--Log Directory Control -->
	 <table   id="logReporting" border="0" cellpadding="0" cellspacing="0">
	 	 
			<tr  class='windowHeader'>
			<td   class='windowHeader' colspan='2'>Log File Types
			</td>
			</tr>
			
			<tr>
			<td>
	
	 
	<!- content part ->
	<table border=0>
	<tr>
	<td valign='top'>
	 
	<!-- form name='LogfileTypeForm' -->
	<table border=0>
	<tr>
	<td>Name</td>
	  
	</tr>
	
	<tr>
	<td>
	
	 
	
	 <input type='text' onkeypress= check_submit(event,this.form,'LogfileType','add',document.getElementById('newLogfileType').value) id='newLogfileType' name='newLogfileType' size='20'></input>
	</td>
	 
	</tr>
	
	<tr>
	<td>
	 
	  
	 
	<input value='Add' type='button' onclick="javascript:sendDataReq('LogfileType','add',document.getElementById('newLogfileType').value + '::' + '');"></input> 
	 
	 </td>
	</tr>
	</table>
	<!-- /form -->
	</td>
	</tr>
	
	<tr>
	<!- left ->
	<td   valign=top>
	
	<table border=0>
	<tr>
	<td>
	Types:
	</td>
	</tr>
	
	<tr>
	<td>
	<form name='LogfileTypeDel'  method='post'>
	
	 <select  onclick=editLFT(); style='width: 125px;' size='5' name='selection'>
	 };
	 
	foreach $type (@ltypes){
	my ($id,$tname) = split(/ZZZ/,$type);
		print "<option value='$tname'>$tname</option>";	
		
	
	}
	 print qq{
	</select>
	<BR>
	<input value='Delete' type='button' onclick="javascript:sendDataReq('LogfileType','del',document.forms.LogfileTypeDel.selection.value);"></input>
	</form>
	
	</td>
	</tr>
	</table>
	
	</td>
	<!- end left ->
	
	<!- right ->
	 
	</tr>
	</table>
	<!- content part end->
	
	  
	
	</td>
	<td valign=top>
		 	<div id=lftFilter>
	<!- end right ->
	</div> 
	</td>
	</tr>
	</table> <!- window ->
	</div>
	</htmlData>
	</reportMessage>
	 }; 
} 

#
# LOG FILE TYPE: EDIT
# this should be in LogFileTypeControl
sub edit{
	DBLib::initDB();
	my $logfileType = shift;
	@list = LogFileType::getFilenameFilter($logfileType);
	print qq{
	 <form name="lftFilter">
	<table border=0>
	 
	<tr>
	<td>
	 <h3>$logfileType</h3>
	 <input type=hidden name='newLogfileType' value='$logfileType'>
 
	 	Associated Hostnames
	</td>
	</tr>
	
	<tr>
	<td  >
 
	</td>
	</tr>
 
 
 
	<tr>
	
	<td>
	
	<table border=0>
	<tr>
<td valign=top rowspan=2>
		 
	<select  style="width: 150px" name='delFilter' size='10'> 
	};
	
	print " value='";
	
	foreach $val(@list){
		chomp($val);
		print "<option value='$val'>$val</option>";
	}
	print "</select>";
	print qq{
	
	</input>
  	</td>

	<td align=center >
 	<input type='button' value='<< Associate Host <<' onclick="javascript:sendDataReq('lftFilter','addFilter',document.forms.lftFilter.newFilter.value + '::' + document.forms.lftFilter.newLogfileType.value);"/> 
 
	</td>
	<td>
		<input type='text' name='newFilter' size='20'/>
   </td>
 </tr>
 
	<tr> 
	 
	};
	 
print qq{
 
<td align=center valign=top>
<input type='button' value='>> Remove Host >>' onclick="javascript:sendDataReq('lftFilter','delFilter',document.forms.lftFilter.delFilter.value + '::' + document.forms.lftFilter.newLogfileType.value);"/>
 </td>
<td valign=top>
 	};
DBLib::initCollageDB();
$query = "SELECT HostName from Host order by HostName";
$Csth = DBLib::executeCollageQuery($query);
$Csth->bind_col(1,\$hostName);
print qq{

<select onChange="document.forms.lftFilter.newFilter.value = document.forms.lftFilter.newFilterList.value;" size='10' name='newFilterList'>

};
while($Csth->fetch()){
  print "<option value=$hostName>$hostName</option>\n";

}


print qq{
 </select>
 </td>
 </tr>
 </table>
   	};
	
	
print qq{
 
	</td>
	</tr>
	
	<tr>
	<td>
	 
	</form>
	 </td>
	</tr>
	</table>
	<!- end right ->
	 
	};
	
}



1;