package TestLogMessageClass;
#
#Copyright 2007 GroundWork Open Source, Inc. ("GroundWork")
#All rights reserved. Use is subject to GroundWork commercial license terms. 
#
use lib qw(/usr/local/groundwork/log-reporting/lib);
use LogMessageClass;
use GWTest;
@ISA = qw(GWTest);
sub new{
	my ($invocant,$testName) = @_;
	my $class = ref($invocant) || $invocant;
	my $self = GWTest->new("LogMessageClass");
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
	"test_addClass",
	"test_addSubType",
	"test_deleteClass",
	"test_delSubType", 
	"test_getClassList", 
	"test_getLMTIDFromTypeName",
	"test_getLogMessageClassID",
	"test_getSubTypes",
	"test_printControl",
	"test_printEditControl",
	"test_printLMCEditPersistence",
	"test_saveClass"
	);
	foreach $t(@tests){
		$testHash{$t} = 0;
	}
 	return %testHash;
}
sub test_addClass{
	$sth = LogMessageClass::addClass("UnitTestClass");
	if($sth->isa(DBI::st)){
		return 1;
	}
	else{
		return 0;
	}
}
sub test_addSubType{
	return 1;
	#TODO need way to supress prints;
	$sth = LogMessageClass::addSubType("UnitTestType");
	if($sth->isa(DBI::st)){
		return 1;
	}
	else{
		return 0;
	}
}
sub test_deleteClass{
	$sth = LogMessageClass::deleteClass("UnitTestClass");
	if($sth->isa(DBI::st)){
		return 1;
	}
	else{
		return 0;
	}
}
sub test_delSubType{
	
	return 1;
	#TODO need a way to supress prints.
	$sth = LogMessageClass::delSubType("UnitTestType");
	if($sth->isa(DBI::st)){
		return 1;
	}
	else{
		return 0;
	}
} 
sub test_getClassList{
	@list = LogMessageClass::getClassList();
	$listSize = @list;
	if($listSize > 0 && ($list[0] =~ /\w+/)){
		return 1;
	}
	else{
		return 0;
	}
}
 
sub test_getLMTIDFromTypeName{
	$id = LogMessageClass::getLMTIDFromTypeName("SSH");
	if($id =~ /\d+/){
		return 1;
	}	
	else{
		 
		return 0;
	}
}

sub test_getLogMessageClassID{
	$id = LogMessageClass::getLogMessageClassID("Security");
	if($id =~ /\d+/){
		return 1;
	}	
	else{
		 
		return 0;
	}

}

sub test_getSubTypes{
	@list = LogMessageClass::getSubTypes("Security");
	$listSize = @list;
	if($listSize > 0 && ($list[0] =~ /\w+/)){
		return 1;
	}	
	else{
		 
		return 0;
	}
}

#TODO need way to store prints in a var for testing
sub test_printControl{
	return 1;
}
#TODO need way to store prints in a var for testing
sub test_printEditControl{
	return 1;
}
#TODO need way to store prints in a var for testing
sub test_printLMCEditPersistence{
	return 1;
}
sub test_saveClass{
	$sth = LogMessageClass::saveClass("UnitTestClass");
	if($sth->isa(DBI::st)){
		return 1;
	}
	else{
		return 0;
	}

}
1;