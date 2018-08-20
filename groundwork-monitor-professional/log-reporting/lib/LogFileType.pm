#!/usr/local/groundwork/bin/perl
#
#Copyright 2007 GroundWork Open Source, Inc. ("GroundWork")
#All rights reserved. Use is subject to GroundWork commercial license terms. 
#

package LogFileType;
use DBLib; 
 
sub new {
  my ($invocant,
      $id,
      $name
      ) = @_;
      
	my $class = ref($invocant) || $invocant;
	$arrayRef = \@filterList;
	my $self = {
			name => $logFileName,
			id => $logFileID,
			};
 	bless($self,$class);
	return $self;
}

 

# 
 
 # LogfileType
##############
sub addType{	 
	my $logfileType = shift;
	my $logfilt = shift;
	my $query = qq{
				insert into LogfileType(typeName) 
				values('$logfileType')
				};
		#	log("ADD LFT QUERY: " . $query);
	
   if(($logfileType ne '') && ($logfileType !~ /^\s+$/)){
	 
	 
    	$sth = DBLib::executeQuery($query);	  	
	}	
 	return $sth;
 	
}

sub deleteType{
	my $logfileTypeName = shift;
	my $query = qq{
				delete from LogfileType 
				where typeName = '$logfileTypeName';
				};
				 
	$sth = DBLib::executeQuery($query);
	return $sth;	
}

 


sub getTypeList{
    @myList = LogFileType::getLogfileTypeList();	
	return @myList;
}
sub getLogfileTypeList{
	#use DBLib;
	#DBLib::initDB();
	my $typeName;
	my $logfileTypeID;
	my @logfileTypeList;
	 
	my $query = qq{
					select logfileTypeID,typeName from LogfileType
					order by typeName;					
					};
	$sth = DBLib::executeQuery($query);
	$sth->bind_col(1,\$logfileTypeID);
	$sth->bind_col(2,\$typeName);
	while($sth->fetch()){
		 $send = "${logfileTypeID}ZZZ${typeName}";
		push(@logfileTypeList,$send);
	}
	$sth->finish();
	return @logfileTypeList;
	
}
sub convertToLogfileTypeID{
	$logType = shift;
	#select logtypeID 	
	$query= qq{
				select logfileTypeID
				from LogfileType 
				where typeName='$logType'
			};
	 
	$sth = DBLib::executeQuery($query);	  
	$logfileTypeID = 0;
 	$sth->bind_col(1,\$logfileTypeID);
	while($sth->fetch()){ ;}#reportError("$query logfileTypeID = $logfileTypeID");} #|| reportError("Couldnt fetch $query");
	$sth->finish();
	return($logfileTypeID);
}

sub getLogfileTypeID{
 	GWLogger::log("getLogfileTypeID()");
	my $typeID;
	my $typeName = shift;
	$query = qq{ select logfileTypeID 
				from LogfileType
				where typeName = '$typeName'
	};
 
	$sth = DBLib::executeQuery($query);
 	$sth->bind_col(1,\$typeID);
 	while($sth->fetch){ 
        ; 	 
 		}
		$sth->finish();
		return $typeID
}

sub addFilter{
	my $typeName = shift;
	my $filter = shift;
	$query = qq{ 
		INSERT INTO LogFilenameFilter(regex,logfileTypeID)
		VALUES('$filter',(select logfileTypeID from LogfileType where typeName = '$typeName'))
		};
	$sth = DBLib::executeQuery($query);
	return $sth;
	
}
sub delFilter{
	my $typeName = shift;
	my $filter = shift;
	$query = qq{
		DELETE FROM LogFilenameFilter
		WHERE regex='$filter' AND 
				logfileTypeID = (select logfileTypeID from LogfileType where typeName = '$typeName')
	};
	
	$sth = DBLib::executeQuery($query);
	return $sth;
}
sub getFilenameFilter{
 
	$fileType = shift;

	$query = qq{
			select filt.regex   
				from LogFilenameFilter filt,LogfileType t
				where t.typeName = '$fileType' and
					filt.logfileTypeID = t.logfileTypeID;
				};
	$sth = DBLib::executeQuery($query);
    $size = $sth->rows;
    if($size < 1){return "";}
	$sth->bind_col( 1, \$regex) || return null; 

	while($sth->fetch()){
	 	push(@RegexList,$regex);
		 
	}
	$sth->finish();
		return @RegexList;
}



1;