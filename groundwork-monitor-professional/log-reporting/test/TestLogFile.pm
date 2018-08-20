#!/usr/bin/perl
#
#Copyright 2007 GroundWork Open Source, Inc. ("GroundWork")
#All rights reserved. Use is subject to GroundWork commercial license terms. 
#
package TestLogFile;
use lib qw(/usr/local/groundwork/log-reporting/lib);
use LogFile;
use GWTest;
@ISA = qw(GWTest);

sub new{
	my ($invocant,$testName) = @_;
	my $class = ref($invocant) || $invocant;
	my $self = GWTest->new("LogFile");
	bless($self,$class);
	###
 	$hashRef = $self->{subTests};
	my %tests = %$hashRef;
	%tests = _getSubTestList();
	$self->{subTests} = \%tests;
 	###
	return $self;
}

### Test LogFile##########################
sub setup{
	DBLib::initDB();
	$initInode=3000;
	$initSeekPos = 0;
	$initDir = "/var/log/";
	$initTypeName = "Linux";
	#print BOLD "LogFile:",RESET,"\n";
  my ($invocant,
      $logFileID,
      $logFileName,
      $logFileTypeID,
      $logFileTypeName,
      $LogDirectoryID,
      $logDirectoryName,
      $isProcessed,
      $seekPos,
      $inode);
			
 
 
 	
 	$setInode = 1000;
 
	$setSeekPos = 500;
 
}

sub test_init{
	 setup();
	$lfTest =  LogFile->new(111,"Linux.linux",222,$initTypeName,333,$initDir,$initSeekPos,$initInode);
	 
	($lfTest->isa("LogFile"))?(return 1):(return 0);
}

sub _getSubTestList{
	my @tests = (
	"test_init",
	"test_getType",
 	"test_getName",
	"test_setName",
 	"test_setInode",
 	"test_getInode",
	"test_setSeekPos",
	"test_getSeekPos",
	"test_isProcessed",
	"test_setIsProcessed",
	"test_getPath"
	);
	foreach $t(@tests){
		$testHash{$t} = 0;
	}
 	return %testHash;
}
sub test_getPath{
	 	 test_init();
	($lfTest->getPath() eq $initDir)?(return 1):(return 0);
 }
sub test_getName{
	test_init();
	if($lfTest->getName() eq "Linux.linux"){
		return 1;
	}
	else{ 
		return 0;
	}
	
}
sub test_getType{
	 	 test_init();
	if($lfTest->getType eq "orphan"){
		return 1;
	}
	else{
	 print "TYPE:" . $lfTest->getType;
		return 0;
	}	
}
sub test_setName{
	 	 test_init();
	if($lfTest->setName("Linux.linux")){
		return 1;
	}
	else{
		return 0;
	}	
}
sub test_setInode{
	 test_init();
	 my $setInode = 1111;
	($lfTest->setInode($setInode) == $setInode)?(return 1):(return 0);
	
}
sub test_getInode{
	 	 test_init();
 	 my $setInode = 1111;
	$lfTest->setInode($setInode);
	($lfTest->getInode() == $setInode)?(return 1):(return 0);	
}
sub test_setSeekPos{
		 test_init();
    my $setSeekPos = 500;
 
	($lfTest->setSeekPos($setSeekPos) == $setSeekPos)?(return 1):(return 0);
}
sub test_getSeekPos{
		 test_init();
	     my $setSeekPos = 500;
	     $lfTest->setSeekPos($setSeekPos);
	     
	($lfTest->getSeekPos() == $setSeekPos)?(return 1):(return 0);
}

sub test_isProcessed{
	 	 test_init();
	$state = $lfTest->isProcessed();
	($state == 1 || $state == 0)?(return 1):(return 0);
}

sub test_setIsProcessed{
	 	 test_init();
	$state = $lfTest->setIsProcessed(1);
	($state == 1 || $state == 0)?(return 1):(return 0);
	 
}
1; 