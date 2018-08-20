#!/usr/local/groundwork/bin/perl
#
#Copyright 2007 GroundWork Open Source, Inc. ("GroundWork")
#All rights reserved. Use is subject to GroundWork commercial license terms. 
#

package LogMessageClassControl;
use LogMessageClass;
use LogMessageType;
use GWLogger;

sub draw{
 	 
@ltypes = LogMessageClass::getClassList();
print qq{
 <reportMessage>
 <htmlData id='htmlData'>
 <div id='LogMessageClass'>
 <!--Log Directory Control -->
 	<table table id="logReporting" border="0" cellpadding="0" cellspacing="0"><tr><td colspan=3 class=windowHeader>Log Message Class Control</td></tr>
 	<tr><td class=windowContent valign=top>
	<table border=0  >
	<tr   class='windowCapsule'><td class='windowHeader' colspan='2'>Message Classes</td></tr>
<tr>
<td>
 <!-- form name='LogMessageClassForm'  METHOD='post' -->
 <input type='hidden' name='struct' value='LogMessageClass'></input>
 <input type='hidden' name='action' value='add'></input>
 <table>
 <tr>
 <td><input type='text' onkeypress=check_submit(event,this.form,'LogMessageClass','add',document.getElementById('newLogMessageClass').value) id='newLogMessageClass' name='newLogMessageClass' size='20'></input>
 <input value='Add' type='button' onclick="javascript:sendDataReq('LogMessageClass','add',document.getElementById('newLogMessageClass').value);"></input></td></tr>
 <tr>
 <td>
   </td></tr></table> <!--- why? -->
 <!-- /form -->
 <form name='LogMessageClassDel'  method='post'>
<select  onChange="javascript:editLMC();" name='selection' size='5'>
 
 };
 
foreach $type (@ltypes){
	print "<option value='$type'>$type</option>";	
	
}
print "</select>";
 
 print qq{
</select>
<input value='Delete' type='button' onclick="javascript:sendDataReq('LogMessageClass','del',document.forms.LogMessageClassDel.selection.value);"></input></form></td></tr>
</table>
 
</td>
 <td>
 };
 @messageTypeList = LogMessageType::getTypeList();
 
### ASSOCIATED TYPES
 print "</div><div id='LogMessageClassEdit'></div></td></tr></table>";
 
 

 }
 
sub _associatedTypesCtl{
	my $logMessageClass = shift;
 	GWLogger::log("associatedTypes:$logMessageClass");
	@messageTypeList = LogMessageClass::getSubTypes($logMessageClass);
 

 
print qq{
		 
		<form name=subTypes>
		<table class=windowContent>
		<tr><td  align='center' >Associated<br/>Message Types</td></tr>
 		<tr><td>
 		<select size=10 name=subTypes>
 };
foreach $type (@messageTypeList){
	 print "<option value=$type>$type</option>";	

}
print qq{
	</select>
	</td>
	</tr>
	</table>
	</form>
};
}
	
sub _messageTypeSubPanel{
	my @messageTypeList = LogMessageType::getTypeList();
	$listOpt = 
	print qq{
		<form name='addSubForm'>
		<table border=0> <tr><td><td valign=top align='center'>ALL <br/>Message Types</td></tr>
		<tr><td  valign='top' align='center'>
		 
		<input type=button value='>> REMOVE' onclick="javascript:sendDataReq('LogMessageClassEdit','remSub',document.forms.subTypes.subTypes.value + 'ZZ' + document.forms.LogMessageClassDel.selection.value);">
		<br>
		<input type=button value='<< ADD' onclick="javascript:sendDataReq('LogMessageClassEdit','addSub',document.forms.addSubForm.typeSubPanel.value + 'ZZ' + document.forms.LogMessageClassDel.selection.value);">
		</td><td>
		<select size=10 name=typeSubPanel>
	};
	
	foreach $type (@messageTypeList){
		print "<option value=$type>$type</option>";
	}
	print "</select> </td></tr></table></form>";
		
	
}
  
sub getLogMessageClassList{
	my @LogMessageClassList;
	my $query;
	$query = qq{ select logMessageClassName 
				 from LogMessageClass 
				 ORDER BY logMessageClassName
				};
 
	$sth = executeQuery($query);
	$sth->bind_col( 1, \$className )    || reportError("Couldn't bind column");
	 	
	while($sth->fetch()){
		push(@LogMessageClassList,$className);
		  #print "Logtype: $LogType";	
	}
	return @LogMessageClassList;
}

  

 

 

sub refreshMessageTypes{
	my $logMessageClass = shift;
	
	@messageSubTypes = LogMessageClass::getSubTypes($logMessageClass);
	print " <div id='popTypes'><table><tr><td  class=windowHeader>Associated Message Types</td></tr>
	<tr><td><select width=100% name=subTypes size=10> ";
	foreach $subType(@messageSubTypes){
		print "<option value=$subType>$subType</option>";	
	}


print qq{ 
	</select></td></tr></table></div> 
	};
	
}
 

sub edit{

my $class = shift;
my $persistence = shift;
my $grouping = shift;
GWLogger::log("edit class with $class and $persistence");
#print "edit class with $class and $persistence >> /tmp/logme";
if($persistence ne ""){
	LogMessageClass::saveClass($class,$persistence,$grouping);	
	} 

#GET PERSISTENCE 
#TODO: move to LogMessageClass::getPersistence()
	$query = qq{
			SELECT persistenceInDays,groupBy
			FROM	LogMessageClass
			WHERE	logMessageClassName = '$class'
	};
	
	$sth = DBLib::executeQuery($query);
	$sth->bind_col(1,\$persistence);
	$sth->bind_col(2,\$grouping);
	$sth->fetch();
	$sth->finish();
print "Content-type: text/html\n\n";	
print "<b><font size=3>$class</font></b>";
 print "<table  class=windowContent><tr><td valign=top>";
 _associatedTypesCtl($class);
 
 print qq{
	</td>
	<td valign=top>
};
_messageTypeSubPanel();

print qq{
		 
		 </td>
		 <td valign=top>
	 
};
printLMCEditPersistence($class,$persistence,$grouping);
#printLMCEditPersistence();
print qq{
	</td>
		 </tr>
  		 </table>
};
}

#TODO: tease out persistence logic.
sub printLMCEditPersistence{
my $class = shift;
my $persistence = shift;
my $grouping = shift;

GWLogger::log("edit Persistence with $class and $persistence and $grouping");

if($persistence ne ""){
	LogMessageClass::saveClass($class,$persistence,$grouping);	
	}
	$query = qq{
			SELECT persistenceInDays,groupBy
			FROM	LogMessageClass
			WHERE	logMessageClassName = '$class'
			};
	
	$sth = DBLib::executeQuery($query);
	$sth->bind_col(1,\$persistence);
	$sth->bind_col(2,\$grouping);
	$sth->fetch();
    $sth->finish();
if($grouping eq "Day"){ $daySelected = "SELECTED";}
if($grouping eq "Week"){ $weekSelected = "SELECTED";}
if($grouping eq "Month"){ $monthSelected = "SELECTED";}

$vav =  qq{ 
<form name=LMCEditForm>
<table class=windowContent>

<tr>
<td>
Consolidate After:

<b>
  </b>
</td>
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
 <option value="Day" $daySelected>Day</option>
 <option value="Week" $weekSelected>Week</option>
 <option value="Month" $monthSelected>Month</option>
 </select>
 </td>
 </tr>
<tr>
<td colspan='3' align='right'>
 <input type='button' value='Save' onclick="javascript:saveLMC();"></input>
 </td>
 </tr>
 
 </table>
 
</form>
};

print $vav;
GWLogger::log($vav);
}

 
1;
 
