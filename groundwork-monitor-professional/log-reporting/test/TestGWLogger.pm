#!/usr/local/groundwork/bin/perl
#
#Copyright 2007 GroundWork Open Source, Inc. ("GroundWork")
#All rights reserved. Use is subject to GroundWork commercial license terms. 
#
package TestGWLogger;
use lib qw(/usr/local/groundwork/log-reporting/lib);
use GWLogger;
use GWTest;
@ISA = qw(GWTest);

sub new{
	my ($invocant,$testName) = @_;
	my $class = ref($invocant) || $invocant;
	my $self = GWTest->new("GWLogger");
	bless($self,$class);
	###
 	$hashRef = $self->{subTests};
	my %tests = %$hashRef;
	%tests = _getSubTestList();
	$self->{subTests} = \%tests;
 	###
	return $self;
}


