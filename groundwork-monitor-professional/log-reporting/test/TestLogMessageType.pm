package TestLogMessageType;
#
#Copyright 2007 GroundWork Open Source, Inc. ("GroundWork")
#All rights reserved. Use is subject to GroundWork commercial license terms. 
#
use lib qw(/usr/local/groundwork/log-reporting/lib);
use LogMessageType;
use GWTest;
@ISA = qw(GWTest);

sub new{
	my ($invocant,$testName) = @_;
	my $class = ref($invocant) || $invocant;
	my $self = GWTest->new("LogMessageType");
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
	"test_addType",
	"test_deleteType", 
	"test_getTypeList",
	"test_getTypeListNEW",
	"test_saveType"
	);
	foreach $t(@tests){
		$testHash{$t} = 0;
	}
 	return %testHash;
} 
sub test_addType{
	return LogMessageType::addType("UnitTestType");
}
sub test_deleteType{
	return LogMessageType::deleteType("UnitTestType");
	}
sub test_saveType{
	return LogMessageType::saveType("UnitTestType",999,"Day")
}

 

sub test_getTypeList{
	@list = LogMessageType::getTypeList();
	$listSize = @list;
	if($listSize > 0 && ($list[0] =~ /\w+/)){
		return 1;
	}
	else{
		return 0;
	}
}
sub test_getTypeListNEW{
	@list = LogMessageType::getTypeListNEW();
	$listSize = @list;
	$firstItem = $list[0];
	($id,$name) = split(/ZZZ/,$firstItem);
	if($listSize > 0 && ($id =~ /\w+/) && ($name =~ /\w+/)){
		return 1;
	}	
	else{
		 
		return 0;
	}	 
}
1;