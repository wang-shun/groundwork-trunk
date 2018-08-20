#!/usr/bin/perl
#
#Copyright 2007 GroundWork Open Source, Inc. ("GroundWork")
#All rights reserved. Use is subject to GroundWork commercial license terms. 
#
package TestLogFileType;
use lib qw(/usr/local/groundwork/log-reporting/lib);
use LogFileType;
use GWTest;
use GWUtils;
@ISA = qw(GWTest);


sub new{
	my ($invocant,$testName) = @_;
	my $class = ref($invocant) || $invocant;
	my $self = GWTest->new("LogFileType");
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
	"test_addFilter",
	"test_addType",
	"test_convertToLogfileTypeID",
	"test_deleteType",
	"test_delFilter",
	"test_edit",
	"test_getFilenameFilter",
	"test_getLogfileTypeID",
	"test_getLogfileTypeList",
	"test_getTypeList",  
	);
	foreach $t(@tests){
		$testHash{$t} = 0;
	}
 	return %testHash;
}
sub test_addFilter{
	$sth = LogFileType::addFilter("UnitTestFilter");
	if($sth->isa(DBI::st)){
		return 1;
	}
	else{
		return 0;
	}
}

sub test_addType{
	$retVal = 0;
	$sth = LogFileType::addType("UnitTestType");
	if($sth->isa(DBI::st)){
		return 1;
	}
	else{
		return 0;
	}
}
sub test_convertToLogfileTypeID{
	$typeName = "Windows";
	$id = LogFileType::convertToLogfileTypeID($typeName);
	if(GWUtils::isInt($id)){
		return 1;
	}
	else{
		return 0;
	}
}
sub test_deleteType{
	$sth = LogFileType::deleteType("UnitTestType");
	if($sth->isa(DBI::st)){
		return 1;
	}
	else{
		return 0;
	}

}
sub test_delFilter{
	$sth = LogFileType::delFilter("UnitTestFilter");
	if($sth->isa(DBI::st)){
		return 1;
	}
	else{
		return 0;
	}
}
sub test_edit{
	$retVal = 2;
	return $retVal;
}
sub test_getFilenameFilter{
	@regexen = LogFileType::getFilenameFilter("Windows");
	$rSize = @regexen;
	if(($rSize > 0) && ($regexen[0] =~ /\w+/)){
		return 1;
	}
	else{
		return 0;
	}
}
sub test_getLogfileTypeID{
	$typeName = "Windows";
	$id = LogFileType::getLogfileTypeID($typeName);
	if(GWUtils::isInt($id)){
		return 1;
	}
	else{
		return 0;
	}
}
sub test_getLogfileTypeList{
	@list = LogFileType::getLogfileTypeList();
	$listSize = @list;
	($id,$name) = split(/ZZZ/,$list[0]);
	if($listSize > 0  && ($id =~ /\d+/) && ($name =~ /\w+/)){
		return 1;
	}
	else{
		print "size: $listSize";
		return 0;
	}

}

sub test_getTypeList{
	@list = LogFileType::getTypeList();
	$listSize = @list;
	($id,$name) = split(/ZZZ/,$list[0]);
	if($listSize > 0  && ($id =~ /\d+/) && ($name =~ /\w+/)){
		return 1;
	}
	else{
		print "size: $listSize";
		return 0;
	}
}
 
 
1;