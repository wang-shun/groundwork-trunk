#!/usr/local/groundwork/bin/perl
#
#Copyright 2007 GroundWork Open Source, Inc. ("GroundWork")
#All rights reserved. Use is subject to GroundWork commercial license terms. 
#
package TestParsingRule;
use lib qw(/usr/local/groundwork/log-reporting/lib);
use ParsingRule;
use GWTest;
use GWUtils;
use DBLib;
 
@ISA = qw(GWTest);


sub new{
	my ($invocant,$testName) = @_;
	my $class = ref($invocant) || $invocant;
	my $self = GWTest->new("ParsingRule");
	bless($self,$class);
	###
 	$hashRef = $self->{subTests};
	my %tests = %$hashRef;
	%tests = _getSubTestList();
	$self->{subTests} = \%tests;
 	###
	return $self;
}

sub setup{
	### Test ParsingRule ###########################
 
  
 
 
sub test_init{
	DBLib::initDB();
$initType = "Linux";
$setType = "Windows";

$initRegex = "^Linux.*log";
$setRegex = "Linlog.*";


$prTest = ParsingRule->new($initType,$initRegex);
 ($prTest != null)?(return 1):(return 0);


}
 
sub _getSubTestList{
	my @tests = (
	"test_init",
	"test_getRulesbyLogType",
	"test_getComponentTypes",
	"test_getLogMessageTypeID"
 );
	foreach $t(@tests){
		$testHash{$t} = 0;
	}
 	return %testHash;
}
}
sub test_getLogMessageTypeID{
	test_init();
	$pRID = "999";
	$lmtID = ParsingRule::getLogMessageTypeID($pRID);
	if(GWUtils::isInt($lmtID)){
	return 1;
	}
	else{
	return 0;
	}
}
sub test_getRulesbyLogType{
	test_init();
 	@rules = ParsingRule::getRulesByLogType("Linux");
 	$size = @rules;
 	 
 	($size > 0 && $rules[0]->isa("ParsingRule"))?(return 1):(return 0);	
}

sub test_getComponentTypes{
	test_init();
	@cTypes = $prTest->getComponentTypes();
	$size = @cTypes;
 	if(($size > 0 && $cTypes[0]->isa("ComponentType")) || ($size == 0)){
 		return 1;
 	}
 	elsif($size == 0){
 		return 1;
 	}
 	else{
 		return 0;	
 	}
}

 
1;