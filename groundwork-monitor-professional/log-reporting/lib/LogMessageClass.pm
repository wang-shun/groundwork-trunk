#!/usr/local/groundwork/bin/perl
#
#Copyright 2007 GroundWork Open Source, Inc. ("GroundWork")
#All rights reserved. Use is subject to GroundWork commercial license terms. 
#

package LogMessageClass;
use LogMessageClassControl;
use GWLogger;

sub new{
	my ($invocant,$classID,$className,$persistence,$groupBy) = @_;
	my $class = ref($invocant) || $invocant;
	my $self = {
		classID=> $classID,
		className=>$className,
		persistence=>$persistence,
		groupBy=>$groupBy
	};
	bless($self,$class);
	DBLib::initDB();
	return $self;
}


sub printControl{
 	 
@ltypes = getLogMessageClassList();
print qq{
 <reportMessage>
 <htmlData id='htmlData'>
 <div id='LogMessageClass'>
 <!--Log Directory Control -->
 	<  table id="logReporting" border="0" cellpadding="0" cellspacing="0"><tr><td colspan=3 class=windowHeader>Log Message Class Control</td></tr>
 	<tr><td class=windowContent valign=top>
	<table border=0  >
	<tr   class='windowCapsule'><td class='windowHeader' colspan='2'>Message Classes</td></tr>
<tr>
<td>
 <form name='LogMessageClassForm'  METHOD='post'>
 <input type='hidden' name='struct' value='LogMessageClass'></input>
 <input type='hidden' name='action' value='add'></input>
 <table>
 <tr>
 <td><input type='text' name='newLogMessageClass' size='20'></input>
 <input value='Add' type='button' onclick="javascript:sendDataReq('LogMessageClass','add',document.forms.LogMessageClassForm.newLogMessageClass.value);"></input></td></tr>
 <tr>
 <td>
   </td></tr></table> <!--- why? -->
 </form>
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
</div>
</td>
 <td>
 };
 @messageTypeList = getLogMessageTypeList();
 
### ASSOCIATED TYPES
 print "<div id=LogMessageClassEdit> </div></td></tr></table>";
 
 

 }
 

sub getLogMessageClassID{
	

$className = shift;
$query = qq{select logMessageClassID
			from LogMessageClass
			where logMessageClassName = '$className'
			};
	$sth = DBLib::executeQuery($query);
	$sth->bind_col(1,\$logMessageClassID);
	while($sth->fetch()){;}
	$sth->finish();
#	debug("Logmessage typeid for rule $parsingRuleID = $logMessageTypeID");
	return $logMessageClassID;	
}

sub addSubType{

my $subType = shift;
my $messageClass = shift;
GWLogger::log("subType = $subType  and messageClass = $messageClass");
$typeID = getLMTIDFromTypeName($subType);
$classID = getLogMessageClassID($messageClass);
$query = qq{
		insert into LogMessageClass_LogMessageType(logMessageClassID,logMessageTypeID)
		values ('$classID','$typeID')
		};
$sth = DBLib::executeQuery($query);
GWLogger::log($query);
LogMessageClassControl::edit($messageClass);
return $sth;
}

sub delSubType{

my $subType = shift;
my $messageClass = shift;
GWLogger::log("subType = $subType  and messageClass = $messageClass");
$typeID = getLMTIDFromTypeName($subType);
$classID = getLogMessageClassID($messageClass);
$query = qq{
		delete from LogMessageClass_LogMessageType
		where logMessageClassID = '$classID' and
		logMessageTypeID = '$typeID'
		};
$sth = DBLib::executeQuery($query);
GWLogger::log($query);
LogMessageClassControl::edit($messageClass);
return $sth;
}


sub getLMTIDFromTypeName{
	

$className = shift;
$query = qq{select logMessageTypeID
			from LogMessageType
			where logMessageTypeName = '$className'
};
	$sth = DBLib::executeQuery($query);
	$sth->bind_col(1,\$logMessageTypeID);
	while($sth->fetch()){;}
	$sth->finish();
	return $logMessageTypeID;	
}
 
 
sub _associatedTypesCtl{
	my $logMessageClass = shift;
	GWLogger::log("associatedTypes:$logMessageClass");
	@messageTypeList = getLMCSubTypes($logMessageClass);
 

 
print qq{
		<form name=subTypes>
		<table class=windowContent>
		<tr><td   >Associated Message Types</td></tr>
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
	my @messageTypeList = getLogMessageTypeList();
	$listOpt = 
	print qq{
		<form name='addSubForm'>
		<table border=0> <tr><td valign=top colspan=2 >ALL Message Types</td></tr><tr><td>
		 
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
 
#TODO Possibly obsolete  
sub getClassCollection{

	my $query = qq{
			SELECT 	logMessageClassID,
					logMessageClassName,
					persistenceInDays,
					groupBy 
			FROM 	LogMessageClass;
	};
	$sth = DBLib::executeQuery($query);
	$sth->bind_col( 1, \$classID )    || reportError("Couldn't bind column");
	$sth->bind_col( 2, \$className )    || reportError("Couldn't bind column");
	$sth->bind_col( 3, \$persistence )    || reportError("Couldn't bind column");
	$sth->bind_col( 4, \$groupBy )    || reportError("Couldn't bind column");
	 	
	while($sth->fetch()){
	   $classObj = LogMessageClass->new($classID,$className,$persistence,$groupBy);
		push(@classCollection,$classObj);
	}
	$sth->finish();
	return @classCollection;

}  

sub getClassList{
	my @LogMessageClassList;
	my $query;
	$query = qq{ select logMessageClassName 
				 from LogMessageClass 
				 ORDER BY logMessageClassName
				};
 
	$sth = DBLib::executeQuery($query);
	$sth->bind_col( 1, \$className )    || reportError("Couldn't bind column");
	 	
	while($sth->fetch()){
		push(@LogMessageClassList,$className);
		  #print "Logtype: $LogType";	
	}
	$sth->finish();
	return @LogMessageClassList;
}

 # LogMessageClass
##################
sub addClass{
	#check for duplicates
	#error message 
	my $logMessageClass = $_[0];
	my $logfileTypeID = $_[1];

	my $query = qq{
					insert into LogMessageClass(logMessageClassName,logfileTypeID) 
					values('$logMessageClass','$logfileTypeID')
				};
   if(($logMessageClass ne '') && ($logMessageClass !~ /^\s+$/)){
	$sth = DBLib::executeQuery($query);
   }
	return $sth;	
}

sub deleteClass{
	my $logMessageClassName = shift;
	my $query = qq{
					delete from LogMessageClass 
					where logMessageClassName = '$logMessageClassName'
				};
	$sth = DBLib::executeQuery($query);
	return $sth;
}

sub getSubTypes{
	my $messageClass = shift;
	my $query = qq{
		SELECT 	lmc.logMessageClassName,lmt.logMessageTypeName
 		FROM 	LogMessageClass lmc,
				LogMessageType lmt,
				LogMessageClass_LogMessageType combo
		WHERE 	combo.logMessageClassID = lmc.logMessageClassID and
      			combo.logMessageTypeID = lmt.logMessageTypeID and 
       			lmc.logMessageClassName = '$messageClass'
		};

	$sth = DBLib::executeQuery($query);
	$sth->bind_col(2,\$result);
	while($sth->fetch){
		push(@retList,$result);
		}
	$sth->finish();
	return @retList;
}

 
#TODO Possibly obsolete
sub getID{
	

$className = shift;
$query = qq{select logMessageClassID
			from LogMessageClass
			where logMessageClassName = '$className'
			};
	$sth = executeQuery($query);
	$sth->bind_col(1,\$logMessageClassID);
	while($sth->fetch()){;}
#	debug("Logmessage typeid for rule $parsingRuleID = $logMessageTypeID");
	return $logMessageClassID;	
}

sub printEditControl{

my $class = shift;
my $persistence = shift;

GWLogger::log("edit class with $class and $persistence");

if($persistence ne ""){
	saveLMC($class,$persistence);	
	} 

#GET PERSISTENCE
	$query = qq{
			SELECT persistenceInDays
			FROM	LogMessageClass
			WHERE	logMessageClassName = '$class'
	};
	
	$sth = executeQuery($query);
	$sth->bind_col(1,\$persistence);
	$sth->fetch();
	
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
printLMCEditPersistence($class,$persistence);
#printLMCEditPersistence();
print qq{
	</td>
		 </tr>
  		 </table>
};
}

sub printLMCEditPersistence{
my $class = shift;
my $persistence = shift;


GWLogger::log("edit Persistence with $class and $persistence");

if($persistence ne ""){
	saveLMC($class,$persistence);	
	}
	$query = qq{
			SELECT persistenceInDays
			FROM	LogMessageClass
			WHERE	logMessageClassName = '$class'
			};
	
	$sth = executeQuery($query);
	$sth->bind_col(1,\$persistence);
	$sth->fetch();

print qq{ 
<form name=LMCEditForm>
Consolidate After:
<table class=windowContent>

<tr>
<td colspan=2>
<b>
  </b>
</td>
</tr>
 
<tr>
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
<td>
 <input type='button' value='Save' onclick="javascript:saveLMC();"></input>
 </td>
 </tr>
 
 </table>
 
</form>
};

}

sub saveClass{
my $LMC = shift;
my $persistence = shift;
my $grouping = shift;
GWLogger::log("saving $LMC,$persistence");
$query = qq{
		UPDATE LogMessageClass
		SET	persistenceInDays = '$persistence',
			groupBy = '$grouping'
		WHERE logMessageClassName = '$LMC'
};	
	
	$sth = DBLib::executeQuery($query);
	return $sth;
}
1;
 
