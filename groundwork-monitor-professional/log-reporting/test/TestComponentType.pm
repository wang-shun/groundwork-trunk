#!/usr/bin/perl
#
#Copyright 2007 GroundWork Open Source, Inc. ("GroundWork")
#All rights reserved. Use is subject to GroundWork commercial license terms. 
#
package TestComponentType;
use lib qw(/usr/local/groundwork/log-reporting/lib);
use ComponentType;
use GWTest;
@ISA = qw(GWTest);

sub new{
	my ($invocant,$testName) = @_;
	my $class = ref($invocant) || $invocant;
	my $self = GWTest->new("ComponentType");
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
	"test_getID",
	"test_getName"
	);
	foreach $t(@tests){
		$testHash{$t} = 0;
	}
 	return %testHash;
}
 

sub test_init{
$initID = 1;
$initName = "rhost";
$ctTest = ComponentType->new($initID,$initName);
($ctTest->isa("ComponentType"))?(return 1):(return 0);
}
sub test_getID{
	test_init();
	 
	($ctTest->getID == $initID)?(return 1):(return 0);
}

sub test_getName{
	test_init();
	($ctTest->getName eq $initName)?(return 1):(return 0);
}

1; 
 