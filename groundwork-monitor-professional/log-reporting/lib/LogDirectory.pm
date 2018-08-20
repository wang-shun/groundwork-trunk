#!/usr/bin/perl
#
#Copyright 2007 GroundWork Open Source, Inc. ("GroundWork")
#All rights reserved. Use is subject to GroundWork commercial license terms. 
#

package LogDirectory;
use GWLogger;

sub new{
	my ($invocant,$dirName,$id) = @_;
	my $class = ref($invocant) || $invocant;
	$dirName =~ s/^\s+//; #remove leading spaces
	$dirName =~ s/\s+$//; #remove trailing spaces
	my $self = {
		dirname=> $dirName,
		id=>$id
	};
	bless($self,$class);
	DBLib::initDB();
	return $self;
}

sub getName{
	my $self = shift;
	return $self->{dirname};
}

sub getID{
	my $self = shift;
	return $self->{id};	
}


# LogDirectory
###############
sub add 
{
	
	#columbia check for duplicates
	my $logDir = shift;
	$logDir =~ s/\s+$//;
	$logDir =~ s/^\s+//;
	
	if(-e $logDir){
		unless($logDir =~ /\/$/){
	 		$logDir .= "/";
	 		}
		my $query = qq{	insert into LogDirectory(logDirectory) 
					values('$logDir');
					};
	GWLogger::log("LogDirectory::add(): query=" . $query);
	$sth=DBLib::executeQuery($query);
    $sth->finish();
	$retVal = 1;
	}
	else{
	$retVal = 0;
	}
	return $retVal;
}

sub delete 
{
	$valid = 1;
	my $logDirID = $_[0];

	#delete the log directory
	my $query = qq{
				delete from LogDirectory 
				where logDirectoryID = '$logDirID'
				};
	DBLib::executeQuery($query);	  	
	
	#delete files associated with this directory
	my $query = qq{
				delete from LogFile 
				where LogDirectoryID = '$logDirID'
				};
	DBLib::executeQuery($query);
	
	return $valid; 
}


sub getFileList{
	my $self = shift;
		
	#BEGIN Reconcile
	@fsFiles = $self->_getFilesFromFS();
	
	$fsCnt = @fsFiles;
	 
	foreach $file (@fsFiles){
			#Build a list of inodes of valid files still in fs
			$inodeList .= $file->{inode} . ",";
		 	#Add to DB if only in HD
			$file->insert($self);  # unique constraint on LogFile(inode) prevents duplicates 
			
			
			#Update name in SQL by (inode) 
			my $updateName = $file->getName();
			my $updateInode = $file->getInode();
			$updateQuery = qq{
				UPDATE LogFile
				SET logFileName = '$updateName'
				WHERE inode = $updateInode	
			};
			DBLib::executeQuery($updateQuery);
			
	}	
	chop($inodeList);
	
	
	#Delete Files in DB if no longer in HD (by inode)
	$sqlQuery = qq	{	
		DELETE FROM LogFile 
		WHERE inode NOT IN ($inodeList)
	};
	if($inodeList ne ""){
		DBLib::executeQuery($sqlQuery);
	}
 	@dbFiles = $self->_getFilesFromDB();
	return @dbFiles;	 	
}

#get the log directories from DB: /var/log
sub getLogDirectories
{
	 
	#columbia - set index on isProcessed
	my %direc;
	#get unprocessed logs
	my @director = (); 
	my $logDirectory = undef;
	my $query = qq(
				select 	logDirectoryID,
						logDirectory 
				from 	LogDirectory  
				);
	$sth = DBLib::executeQuery($query);
	$sth->bind_col(1,\$logDirectoryID);
	$sth->bind_col(2,\$logDirectory);
	
	while($sth->fetch()){
 	 	$logDirObj = LogDirectory->new($logDirectory,$logDirectoryID);
		push(@director,$logDirObj);
		GWLogger::log("Found: " . $logDirObj->getName()  . "," . $logDirObj->getID());
 		
	}#while
	$dSize = @director;
	GWLogger::log("Returning ". $dSize . " directories");
 	return @director;
	
}#sub


####################
# "PRIVATE" METHODS #
####################

#
# _getFilesFromDB():
# Returns Array of File Objects for files currently stored in the Database
#

sub _getFilesFromDB{
	my $self = shift;
    $myID = $self->{id};
    print "getFilesFromDB:MYID:$myID" .  $self->getName() . "\n" if($debug);
	my $query = qq{
		SELECT 		lf.logFileName,
				lf.logfileTypeID,
				lf.LogDirectoryID,
				lf.isProcessed,
				lf.seekPos,
				lf.inode,
				lf.logFileID,
				ld.logDirectory,
				lft.typeName
		FROM	LogFile lf,
			LogDirectory ld,
			LogfileType lft
		WHERE 	lf.LogDirectoryID = $myID	AND
			ld.logDirectoryID = lf.LogDirectoryID AND
 			lft.logfileTypeID = lf.logfileTypeID
	};
	
	$sth = DBLib::executeQuery($query);
	$sth->bind_col(1,\$logFileName);
	$sth->bind_col(2,\$logFileTypeID);
	$sth->bind_col(3,\$LogDirectoryID);
	$sth->bind_col(4,\$isProcessed);
	$sth->bind_col(5,\$seekPos);
	$sth->bind_col(6,\$inode);
	$sth->bind_col(7,\$logFileID);
	$sth->bind_col(8,\$logDirectoryName);
	$sth->bind_col(9,\$logfileTypeName);

	while($sth->fetch()){
	  
		$fileObj = LogFile->new($logFileID,$logFileName,$logFileTypeID,$logfileTypeName,$LogDirectoryID,$logDirectoryName,$isProcessed,$seekPos,$inode);	
		push(@dbFiles,$fileObj);
	}
	return @dbFiles;
}

#  _getFilesFromFS:
# 	Scans a directory in the filesystem and
#	returns an Array of LogFile objects populated with FileName,Inode,DirectoryName
#
sub _getFilesFromFS {

	$self = shift;
	
	$cmd = "ls -1i " . $self->getName();

	@listing = `$cmd`;
	foreach $line (@listing){
		chomp($line);
		$line =~  /\s*(\d+)\s+(\w+.*)/;
		$inode = $1;
		$fileName = $2;
		$dirID = $self->getID();
		#print "getFilesFromFS(): DIRID: $dirID  ****\n";
		unless(-d "/var/log/$fileName"){ # Ignore Directories for now
			$dir = $self->getName();
			$fObj = LogFile->new(undef,$fileName,undef,undef,$dirID,$dir,undef,undef,$inode);
			push(@files,$fObj);
		}	
	} 
	return @files;		
}

#
# exists(): May not be necessary
#
sub exists{
	my $self = shift;
	my $path = $self->getName();
	if( -e $path){
		$retVal = 1;
	}
	else{
		$retVal = 0;
	}
	return $retVal;
}

1;