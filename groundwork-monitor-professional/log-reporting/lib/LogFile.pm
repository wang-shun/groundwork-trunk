#!/usr/bin/perl
#
#Copyright 2007 GroundWork Open Source, Inc. ("GroundWork")
#All rights reserved. Use is subject to GroundWork commercial license terms. 
#

package LogFile;
use LogFilenameFilter;
use DBLib;
sub new {
  my ($invocant,
      $logFileID,
      $logFileName,
      $logFileTypeID,
      $logFileTypeName,
      $LogDirectoryID,
      $logDirectoryName,
      $isProcessed,
      $seekPos,
      $inode) = @_;
      
#	my ($invocant,$name,$id,$typeName,$inode,$seekPos,$dir) = @_;
	my $class = ref($invocant) || $invocant;
	my $self = {
			name => $logFileName,
			id => $logFileID,
			typeID => $logFileTypeID,
			typeName => $logFileTypeName,
			inode => $inode,
			seekPos => $seekPos,
			path => $logDirectoryName,
			dirID =>$LogDirectoryID
	};
 	bless($self,$class);
 	#print "SLFID: $logFileID," . $self->getID() . "\n";

	return $self;
				
}

sub getID{
  $self = shift;
  return $self->{id};
}

sub getPath{
	my $self = shift;
	return $self->{path};	
}

sub getInode{
	$self = shift;
	return $self->{inode};
}

#does this need to exist#
sub setInode{
	($self,$newInode) = @_;
	$self->{inode} = $newInode;
	return $self->{inode};	
}
sub getSeekPos{
	$self = shift;
	return $self->{seekPos};
}
sub setSeekPos{
	#set instance variable
	($self,$newSeekPos) = @_;
	#$self->setSeekPos($newSeekPos);
	$self->{seekPos} = $newSeekPos;
	my $thisID = $self->getID();
	my $seekP = $self->{seekPos};
	#set value in DB
	$query = qq{
			UPDATE LogFile
			SET seekPos = $seekP
			WHERE logFileID = $thisID
	};
	DBLib::executeQuery($query);
	
	return $self->getSeekPos();
	
}

sub getName{
	my $self = shift;
	return $self->{name};
}

sub setName{
	my($self,$newName) = @_;
	
	#set Instance Variable
	$self->{name} = $newName;
	
	#set value in DB
	$query = qq{
		UPDATE LogFile
		SET logFileName = '$newName'
		where logFileID = $self->{id}
	};
	$sth=DBLib::executeQuery($query);
	$sth->finish();
	return 1;
}

sub getType{
	
	$self = shift;
	if ($self->{typeName} ne "") {
		return $self->{typeName};
	}
	
	# ID Type grep -l MSWinEventLog *.log
	
	DBLib::initDB();

	my $filename = $self->{name};
	my $typeID;
	my $regex;
	my $typeName;
	#debug("getLogfileType(): for $filename");
	
	@LogfileTypeFilterList = getLogFilenameFilterList(); #make persistant 
	
	foreach $filter (@LogfileTypeFilterList) {
	    $regex = $filter->{regex};
	    $typeName = $filter->{typeName};
		#print "********REGEX $typeName = $regex\n";
	    if($filename =~ /$regex/){
	     #   pLog("$filename is of type $typeName , ID" . $self->{typeID});
	        $self->{type} = $typeName;
	        $self->{typeID} = LogFileType::convertToLogfileTypeID($typeName);
	    	return $typeName;
	    }#if
	}#foreach
 	print "$filename is type Orphan" if($debug);
	return "orphan";#"orphan";
}
 
#this needs to include the LogfileType.
sub getLogFilenameFilterList{
	@retArray = ();
    my %logTypeRegexList;
	$query = qq{
			select filt.regex, t.typeName 
				from LogFilenameFilter filt,LogfileType t
				where filt.logfileTypeID = t.logfileTypeID;
				};
	$sth = DBLib::executeQuery($query);

	$sth->bind_col( 1, \$regex)    || reportError("Couldn't bind column");
	$sth->bind_col(2,\$typeName);
	while($sth->fetch()){
         $filterObj = LogFilenameFilter->new($typeName,$regex);
         push(@retArray,$filterObj);	 
	}
        return @retArray;	
}

sub isProcessed{
	$self = shift;
	my $pState = 0;
	my $query = qq{
					select isProcessed
							 
					from	LogFile
					where logfileName = '$self->{name}';
					};
	$sth = DBLib::executeQuery($query);
	$sth->bind_col(1,\$pState);
	 
	while($sth->fetch()){
		 ;
		 
	} 		
	
	return $pState;
}


sub setIsProcessed{
	($self,$state) = @_;
 
	$query = qq(
				update LogFile
				set isProcessed = $state
				where logFileID = '$self->{id}';
				);
				
	DBLib::executeQuery($query);
	return $state;
}


#
# insertFile(): adds file to LogFile table
#
# param: filename,directory,logfileTypeName
sub insertOld{  ########### columbia missing logtype
	my $filename = shift;
	my $dir = shift;
	my $logfileTypeName = getLogfileType($filename);
	$logfileTypeID = convertToLogfileTypeID($logfileTypeName);
	#debug("Inserting $filename of type $logfileTypeName from $dir");
	convertToDirID($dir);
	
	#Add file to LogFile Table
		$query = qq{
					insert into LogFile(logFileName,
										logDirectoryID,
										logfileTypeID)
							values( '$filename',
									'$dirID',
									'$logfileTypeID');
								
		};
		#executeQuery($query);
		$sth = executeQuery($query);
		$logFileID = $sth->{mysql_insertid};
		#debug("insertFile:() logFileID = $logFileID");
		return $logFileID;
}

sub existsinDB{
	my $existance = 0;
	my $mFile = shift;
	
	my $query = qq{
					select *
							 
					from	LogFile
					where logfileName = '$mFile';
					};
	$sth = executeQuery($query);
	$sth->bind_col(1,\$id);
	 
	while($sth->fetch()){
		$existance = 1;
		 
	} 		
	
	return $existance;
}

sub getDir{
	my $self = shift;
	my $id = self->{dirID};
	my $p = $self->getPath();
	
	print "GETDIR: $id + $p\n";
	my $dir = LogDirectory->new($p,$id);
	return $dir;
}

sub insert{
	my $self = shift;
	my $dir = shift;
	$self->getType();
	my $name = $self->getName();
	my $typeID = $self->{typeID};
	#my $dir = $self->getDir();
	my $dirID = $dir->getID();
 	 
	my $inode = $self->getInode();
	$query = qq{
		INSERT INTO LogFile (logFileName,logFileTypeID,logDirectoryID,inode)
		VALUES('$name','$typeID','$dirID',$inode);
	};
	#print "QUERY: $query\n";
	DBLib::executeQuery($query);
}

# not sure why we want this
sub getAllFiles{
	$query = qq{
				select 	logFileID,
						logFileName,
						logfileTypeID,
						logDirectoryID
				from	LogFile
			};
	$sth = executeQuery($query);	
	$sth->bind_col(1,\$logFileID);
	$sth->bind_col(2,\$logFileName);
	$sth->bind_col(3,\$logfileTypeID);
	$sth->bind_col(4,\$logDirectoryID);
	
	while($sth->fetch()){
		$returnMe{$logFileName} = $logFileID;
	}
	return %returnMe;
}
 sub pLog{
 	my $txt = shift;
 	print $txt . "\n";
 }
1;