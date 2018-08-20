#!/usr/local/groundwork/perl/bin/perl --
#
# Copyright 2007-2011 GroundWork Open Source, Inc. ("GroundWork")
# All rights reserved. Use is subject to GroundWork commercial license terms. 
#

use DBI;

package LogMessageType;
use GWLogger;

sub new{
	my ($invocant,$id,$name,$persistence,$groupBy) = @_;
	my $class = ref($invocant) || $invocant;
	my $self = {
		id=>$id,
		name=> $name,
		persistence=> $persistence,
		groupBy => $groupBy
	};
	bless($self,$class);
	DBLib::initDB();
	return $self;
}

sub addType{
	#columbia change table name
	my $logMessageType = shift;
	my $query = qq{ insert into LogMessageType(logMessageTypeName) values('$logMessageType') };
	$sth = null;			 
	if(($logMessageType ne '') && ($logMessageType !~ /^\s+$/)){
	    $sth = DBLib::executeQuery($query);	  	
	    if($sth->isa(DBI::st)){
		    $sth->finish();	
		    $retVal= 1;
	    }
	    else{
		    $retVal=0;
	    }
	}	
	
	return $retVal;
}

sub saveType{
	my $LMT = shift;
	my $persistence = shift;
	my $grouping = shift;
	GWLogger::log("saving $LMT,$persistence,$grouping");
	$query = qq{ UPDATE LogMessageType SET persistenceInDays = '$persistence', groupBy = '$grouping' WHERE logMessageTypeName = '$LMT' };	
	$sth = DBLib::executeQuery($query);  			
	($sth->isa(DBI::st)?($retVal= 1):($retVal=0));
	$sth->finish();		
	return $retVal;
}

sub deleteType{
	#columbia change table name
	my $logMessageTypeName = shift;
	my $query = qq{ delete from LogMessageType where logMessageTypeName = '$logMessageTypeName'; };
	DBLib::executeQuery($query);
	$sth = DBLib::executeQuery($query);  			
	($sth->isa(DBI::st)?($retVal= 1):($retVal=0));
	$sth->finish();
	return $retVal;		
}

sub getTypeList{
	my @LogMessageTypeList;
	my $typeName;
	my $query = qq{ select logMessageTypeName from LogMessageType ORDER BY logMessageTypeName };
	$sth = DBLib::executeQuery($query);
	$sth->bind_col( 1, \$typeName )    || reportError("Couldn't bind column");
	while($sth->fetch()){
		push(@LogMessageTypeList,$typeName);
#		   print "typeName: $typeName";	
	}
	$sth->finish();
	return @LogMessageTypeList;
}

sub getTypeListNEW{
	my @LogMessageTypeList;
	my $typeName;
	my $query = qq{ select logMessageTypeID,logMessageTypeName from LogMessageType ORDER BY logMessageTypeName };
	$sth = DBLib::executeQuery($query);
	$sth->bind_col( 1, \$typeID )    || reportError("Couldn't bind column");
	$sth->bind_col( 2, \$typeName )    || reportError("Couldn't bind column");
	while($sth->fetch()){
		$logMsg= "${typeID}ZZZ${typeName}";
		push(@LogMessageTypeList,$logMsg);
	}
	$sth->finish();
	return @LogMessageTypeList;
 }
 
 sub getTypeListCollection{
  my $query = "select * from LogMessageType";
  $sth = DBLib::executeQuery($query);
  $sth->bind_col(1,\$logMessageTypeID);
  $sth->bind_col(2,\$logMessageTypeName);
  $sth->bind_col(3,\$persistenceInDays);
  $sth->bind_col(4,\$groupBy);
  
  while($sth->fetch()){
   $typeObj = LogMessageType->new($logMessageTypeID,$logMessageTypeName,$persistenceInDays,$groupBy);
   push(@typeCollection,$typeObj);
  }
  $sth->finish();
  return @typeCollection;
 }

1;
