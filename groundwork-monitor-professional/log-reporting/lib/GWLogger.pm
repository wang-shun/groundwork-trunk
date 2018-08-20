#!/usr/local/groundwork/bin/perl
#
#Copyright 2007 GroundWork Open Source, Inc. ("GroundWork")
#All rights reserved. Use is subject to GroundWork commercial license terms. 
#

package GWLogger;
 

sub log{
	$line = shift;
	open(LOGFILE,">>/usr/local/groundwork/log-reporting/logs/log-reporting.log") || die "Couldn't open logfile $!";
	print LOGFILE $line . "\n";
	close(LOGFILE);
 
}
1;