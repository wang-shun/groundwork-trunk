#!/usr/bin/perl
#
#Copyright 2008 GroundWork Open Source, Inc. ("GroundWork")
#All rights reserved. Use is subject to GroundWork commercial license terms. 
#
package TestLib;
use Term::ANSIColor;
use Term::ANSIColor qw(:constants);

 
sub ok {
	print     BLACK ON_GREEN "   OK   ",RESET,"\n";
}

sub fail{
	print    BLACK ON_RED " FAILED ",RESET,"\n";	
}

sub unimplemented{
	print 	BOLD WHITE ON_BLUE " UNIMPL ", RESET , "\n";
}
1;