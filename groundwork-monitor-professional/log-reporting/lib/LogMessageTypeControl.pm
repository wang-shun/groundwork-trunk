#!/usr/local/groundwork/bin/perl
#
#Copyright 2007 GroundWork Open Source, Inc. ("GroundWork")
#All rights reserved. Use is subject to GroundWork commercial license terms. 
#

package LogMessageTypeControl; 
use LogMessageType;
use GWLogger;

sub draw{
	 
@ltypes = LogMessageType::getTypeList();
 
print qq{
 <reportMessage>
 <htmlData id='htmlData'>
 
<div id='LogMessageType'>
 <!--Log Directory Control -->
<table   id="logReporting" border="1" cellpadding="0" cellspacing="0"> 
 
<tr   class='windowHeader'><td  class='windowHeader' colspan='2'>Message Types

};
 
print qq{</td></tr>
<tr>
<td>
 
 <!-- form name='LogMessageTypeForm'  METHOD='post' -->
 <input type='hidden' name='struct' value='LogMessageType'></input>
 <input type='hidden' name='action' value='add'></input>
 <table>
 <tr>
 <td><input type='text' onkeypress=check_submit(event,this.form,'LogMessageType','add',document.getElementById('newLogMessageType').value) id='newLogMessageType' name='newLogMessageType' size='20'></input>
 <input value='Add' type='button' onclick="javascript:sendDataReq('LogMessageType','add',document.getElementById('newLogMessageType').value);"></input>
 </td>
 </tr>
</table>  
 <!-- /form -->
 
 <form name='LogMessageTypeDel'  method='post'>
  
 <select onclick=editLMT();   style='width: 125px;' size='5' name='selection'>
 };
#print "<select name='selection' size='5'>";
foreach $type (@ltypes){
	print "<option value='$type'>$type</option>";	
	
}
print "</select>";
  
 print qq{
</select>
<input value='Delete' type='button' onclick="javascript:sendDataReq('LogMessageType','del',document.forms.LogMessageTypeDel.selection.value);"></input>
</form>
</td>
<td><div id='LogMessageTypeEdit'></div></td>
</tr>
</table>
</div>

</htmlData>
</reportMessage>
 };
}

#
# LogMessageType EDIT
#
sub edit{
my $type = shift;
my $persistence = shift;
my $grouping = shift;
print "Content-type: text/html\n\n";
GWLogger::log("edit with $type and $persistence and $grouping");

if($persistence ne ""){
LogMessageType::saveType($type,$persistence,$grouping);	
}

	#Get Persistence Value
	$query = qq{
			SELECT persistenceInDays,groupBy
			FROM	LogMessageType
			WHERE	logMessageTypeName = '$type'
	};
	
	$sth = DBLib::executeQuery($query);
	$sth->bind_col(1,\$persistence);
	$sth->bind_col(2,\$grouping);
	$sth->fetch();
    $sth->finish();
	#Get Grouping Value
	if($grouping eq "Day"){$daySelected = "SELECTED";}
	if($grouping eq "Week"){$weekSelected = "SELECTED";}
	if($grouping eq "Month"){$monthSelected = "SELECTED";}
		
	
print qq{ 
<form name=LMTEditForm>
 
<table class=windowContent>
<tr>
<td colspan=2>
 
</td>
</tr>
<tr>
<td>
Consolidate After:</td>
<td>
<input size=2 name=persistence type=text value=$persistence> </input>
</td>
<td>
<select name=timeClass>
<option>Never</option>
<option selected>Days</option>
<option>Weeks</option>
<option>Months</option>
</select>
</td>

 </tr>
 <tr>
 <td colspan='2'>
 Group By:
 </td>
 <td>
 <select name=grouping>
 <option value='Day' $daySelected>Day</option>
 <option value='Week' $weekSelected>Week</option>
 <option value='Month' $monthSelected>Month</option>
 </select>
 </td>
 </tr>
<tr>
<td colspan='3' align='right'>
 <input type='button' value='Save' onclick="javascript:saveLMT();"></input>
</td>
</tr>
 </table>
</form>
};
}
 
1;