#!/usr/bin/perl
#
#Copyright 2007 GroundWork Open Source, Inc. ("GroundWork")
#All rights reserved. Use is subject to GroundWork commercial license terms. 
#
package TestLogDirectory;
use lib qw(/usr/local/groundwork/log-reporting/lib);
use LogDirectory;
use GWTest;
@ISA = qw(GWTest);
sub new{
	my ($invocant,$testName) = @_;
	my $class = ref($invocant) || $invocant;
	my $self = GWTest->new("LogDirectory");
	bless($self,$class);
	###
 	$hashRef = $self->{subTests};
	my %tests = %$hashRef;
	%tests = _getSubTestList();
	$self->{subTests} = \%tests;
 	###
	return $self;
}


sub _getSubTestList{
	my @tests = (
	"test_init",
	"test_exists",
	"test_getFileList",
	"test_getLogDirectories"
	);
	foreach $t(@tests){
		$testHash{$t} = 0;
	}
 	return %testHash;
}

sub setup{
 ### Test LogDirectory######################
 print BOLD "LogDirectory:", RESET,"\n";
 $ldTest = LogDirectory->new("/logs",100);
 #print "  init...";
($ldTest != null)?(return 1):(return 0);
 
 }

sub test_init{
 $ldTest = LogDirectory->new("/logs",100);
# print "  init...";
($ldTest != null)?(return 1):(return 0);


}
sub test_exists{
	test_init();
 
 $existance = $ldTest->exists();
 if($existance == 0 || $existance == 1){
 	#print "Directory $ldTest->{name}" . ($existance?"exists...":"doesn't exist...");
 	return 1;;
 }
 else {
 	return 0;
 }
}

sub test_getFileList{
	test_init();
 @fileList = $ldTest->getFileList();
 $listSize = @fileList;
 if($listSize > 0 && $fileList[0]->isa("LogFile")){
 	return 1;;
 }
 else{
 	print "ListSize $listSize\n";
 	return 0;
 }
}

sub test_getLogDirectories{
	test_init();
 @dirList = $ldTest->getLogDirectories();
 $listSize = @dirList;
 ($listSize > 0 && $dirList[0]->isa("LogDirectory"))?(return 1):(return 0);
}


 1;