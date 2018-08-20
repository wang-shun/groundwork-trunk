#!/usr/local/groundwork/bin/perl --
#
# $Id: process_service_perfdata,v 1.3 2004/06/22 16:53:13 hmann Exp $
#
# Process Service Performance Data
#
# Copyright 2009 GroundWork Open Source, Inc. ("GroundWork")
# All rights reserved. This program is free software; you can redistribute it
# and/or modify it under the terms of the GNU General Public License version 2
# as published by the Free Software Foundation.
#
# This program is distributed in the hope that it will be useful, but WITHOUT
# ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
# FOR A PARTICULAR PURPOSE.  See the GNU General Public License for 
# more details.
#
# You should have received a copy of the GNU General Public License along with
# this program; if not, write to the Free Software Foundation, Inc., 51 Franklin
# Street, Fifth Floor, Boston, MA 02110-1301, USA.
#
# Revision History
# 27-May-2004 Harper Mann
#	Initial Revision
# 14-June-2004 Harper Mann
#	Works for check_cpu
# 16-June-2004 Harper Mann
#	Added check_iostat
# 11-Jan-2005 Peter Loh
#	Modified for Yodlee. File based perf data. Added Yodlee reports
# 13-Jul-2009 Glenn Herteg
#	Changed path to debug log file to be consistent with similar scripts.
#
use strict;
use Time::Local;
my $start_time = time;
my $rrddir = "/usr/local/groundwork/rrd";
my $debug = 0;
my $debuglog = ">> /usr/local/groundwork/nagios/eventhandlers/process_service_perf.log";
my $Rrdtool = "/usr/local/groundwork/bin/rrdtool";
my %ERRORS = ('UNKNOWN' , '-1',
              'OK' , '0',
              'WARNING', '1',
              'CRITICAL', '2');
my ($lastcheck,$host,$svcdesc,$statustext,$perfdata);
$lastcheck = $ARGV[0];
$host = $ARGV[1];
$svcdesc = $ARGV[2];
$statustext = $ARGV[3];
$perfdata = $ARGV[4];

my $rrdname;
sub UNIX_disk_ssh;
sub UNIX_memory_ssh;
sub UNIX_load_ssh;
sub UNIX_swap_ssh;
sub SNMP_if;
sub SNMP_if_bandwidth;
sub NRPE_percent;
sub NRPE_local_disk;
sub NRPE_local_memory;
#sub NRPE_local_cpu;
#sub NRPE_local_pagefile;
# If we are debug, open a log file
if ($debug) {
	open(FP, $debuglog) ;
	print FP "---------------------------------------------------------------------\n " ;
	print FP `date` ;
	print FP "Host: $host\n Svcdesc: $svcdesc\n Lastcheck: $lastcheck\n Statustext: $statustext\n Perfdata:$perfdata\n " ;
}

$rrdname = $host . "_" . $svcdesc . "\.rrd";
# check to see if RRD exists. If not then create
if (!stat("$rrddir/$rrdname")) {
	if (create_rrd($host,$svcdesc,"$rrddir/$rrdname")){
		print FP "Creating $rrddir/$rrdname\n";
	}
}
# Pick off what type service check it is and call approprate handling
if ($svcdesc =~ /UNIX_disk_ssh$/) {
	UNIX_disk_ssh;		# uses check_sar nsca to send this data
} elsif ($svcdesc =~ /UNIX_memory_ssh$/) {
	UNIX_memory_ssh;		# uses check_sar nsca to send this data
} elsif ($svcdesc =~ /UNIX_load_ssh$/) {
	UNIX_load_ssh;		# uses check_sar nsca to send this data
} elsif ($svcdesc =~ /UNIX_swap_ssh$/) {
	UNIX_swap_ssh;		# uses check_sar nsca to send this data
} elsif ($svcdesc =~ /SNMP_if$/) {
	SNMP_if;		# uses check_sar nsca to send this data
} elsif ($svcdesc =~ /SNMP_if_bandwidth$/) {
	SNMP_if_bandwidth;		# uses check_sar nsca to send this data
} elsif ($svcdesc =~ /NRPE_local_cpu$/) {
	NRPE_percent;		# uses check_sar nsca to send this data
} elsif ($svcdesc =~ /NRPE_local_disk$/) {
	NRPE_local_disk;		# uses check_sar nsca to send this data
} elsif ($svcdesc =~ /NRPE_local_memory$/) {
	NRPE_local_memory;		# uses check_sar nsca to send this data
} elsif ($svcdesc =~ /NRPE_local_pagefile$/) {
	NRPE_percent;		# uses check_sar nsca to send this data
}

if ($debug) {
	$start_time = time - $start_time;
	my $datestring = `date`;
	chomp $datestring;
	print FP "$datestring	Execution time = $start_time seconds\n" ;
	close FP;
}
exit 0;

####################################################################
# Sub routine for usage information
#
sub usage {
   print "Required arguments not given!\n\n";
   print "Performance Data handler plugin for Nagios, V1.2\n";
   print "Copyright (c) 2004 Groundwork Open Source Solutions, All Rights Reserved \n\n";
   print "Usage: process_service_perfdata <host> <svc description> <perfdata>\n"   ; 
   exit $ERRORS{"UNKNOWN"};
}
####################################################################

sub UNIX_disk_ssh {
	my @lines; 	my $l; 	my $percent;
	if ($statustext) {
		print FP "Parsing statustext string: $statustext\n" if $debug;
		# parse status text: DISK OK - free space: / 42041 MB (89%)
		if ($statustext =~ /\(([\d\.]+)%\)/i) {
			$percent = $1;
		} else {
			print FP "Invalid status text.\n" if $debug;
			return;
		}
	}
	print FP "Values: percent used=$percent\n" if $debug;
	my $rrdcommand = "$Rrdtool update $rrddir/$rrdname $lastcheck:$percent 2>&1";
	print FP qq($rrdcommand) if $debug;
	@lines = qx($rrdcommand);
	print FP "\nReturn: " . "@lines" . "\n" if $debug;
}
####################################################################

sub UNIX_load_ssh {
	my @lines; 	my $l; 	my $number;
	if ($perfdata) {
		print FP "Parsing perfdata string: $perfdata\n" if $debug;
		# parse perfdata: load=0.06
		if ($perfdata =~ /load=([\d\.]+)/i) {
			$number = $1;
		} else {
			print FP "Invalid perf text. \n" if $debug;
			return;
		}
	}
	print FP "Values: number used=$number\n" if $debug;
	my $rrdcommand = "$Rrdtool update $rrddir/$rrdname $lastcheck:$number 2>&1";
	print FP qq($rrdcommand) if $debug;
	@lines = qx($rrdcommand);
	print FP "\nReturn: " . "@lines" . "\n" if $debug;
}
####################################################################

sub UNIX_memory_ssh {
	my @lines; 	my $l; 	my $percent;
	if ($perfdata) {
		print FP "Parsing perfdata string: $perfdata\n" if $debug;
		# parse perfdata: pct: 48.3
		if ($perfdata =~ /pct:\s+([\d\.]+)/i) {
			$percent = $1;
		} else {
			print FP "Invalid perf text. \n" if $debug;
			return;
		}
	}
	print FP "Values: percent used=$percent\n" if $debug;
	my $rrdcommand = "$Rrdtool update $rrddir/$rrdname $lastcheck:$percent 2>&1";
	print FP qq($rrdcommand) if $debug;
	@lines = qx($rrdcommand);
	print FP "\nReturn: " . "@lines" . "\n" if $debug;
}
####################################################################

sub UNIX_swap_ssh {
	my @lines; 	my $l; 	my $percent;
	if ($statustext) {
		print FP "Parsing statustext string: $statustext\n" if $debug;
		# parse status text: SWAP WARNING: 88% free (897 MB out of 1028 MB)
		if ($statustext =~ /([\d\.]+)% free/i) {
			$percent = $1;
		} else {
			print FP "Invalid status text.\n" if $debug;
			return;
		}
	}
	print FP "Values: percent used=$percent\n" if $debug;
	my $rrdcommand = "$Rrdtool update $rrddir/$rrdname $lastcheck:$percent 2>&1";
	print FP qq($rrdcommand) if $debug;
	@lines = qx($rrdcommand);
	print FP "\nReturn: " . "@lines" . "\n" if $debug;
}

####################################################################
# Sub routine for check_if processing
# There are 6 args: in,out,indiscards,outdiscards,inerrs,outerrs
#
sub SNMP_if {
	my @rtn;
	my $discards;
	my $errors;
	my ($in,$out,$indis,$outdis,$inerr,$outerr) = 0;
	if ($perfdata) { 
		print FP "Parsing perfdata string: $perfdata\n" if $debug;
		$perfdata =~ s/^\s+//;
		($in,$out,$indis,$outdis,$inerr,$outerr) = split (/\s/, $perfdata);
	} elsif ($statustext =~ /\s(\d+)\s(\d+)\s(\d+)\s(\d+)\s(\d+)\s(\d+)$/i) {
		print FP "Parsing statustext string: $statustext\n" if $debug;
		$in = $1;
		$out = $2;
		$indis = $3;
		$outdis = $4;
		$inerr = $5;
		$outerr = $6;
	} else {
		print FP "Invalid perfdata.\n" if $debug;
		return;
	}
	$discards = $indis + $outdis;
	$errors = $inerr + $outerr;
	print FP qq($Rrdtool update $rrddir/$rrdname $lastcheck:$in,$out,$discards,$errors) if $debug;
	@rtn = qx($Rrdtool update $rrddir/$rrdname $lastcheck:$in:$out:$discards:$errors);
	print FP "\nReturn: " . "@rtn" . "\n" if $debug;
}

####################################################################
# Sub routine for check_if_bandwidth processing
# There are 3 args: in,out,interface speed
#		Sample statustext: SNMP OK - 906279911 166817618 10000000 
sub SNMP_if_bandwidth {
	my @rtn;
	my ($in,$out,$if_speed) = 0;
	if ($perfdata) { 
		print FP "Parsing perfdata string: $perfdata\n" if $debug;
		$perfdata =~ s/^\s+//;
		($in,$out,$if_speed) = split (/\s/, $perfdata);
	} elsif ($statustext =~ /\s(\d+)\s(\d+)\s(\d+)$/i) {
		print FP "Parsing statustext string: $statustext\n" if $debug;
		$in = $1;
		$out = $2;
		$if_speed = $3;
	} else {
		print FP "Invalid perfdata.\n" if $debug;
		return;
	}
	print FP qq($Rrdtool update $rrddir/$rrdname $lastcheck:$in:$out:$if_speed) if $debug;
	@rtn = qx($Rrdtool update $rrddir/$rrdname $lastcheck:$in:$out:$if_speed);
	print FP "\nReturn: " . "@rtn" . "\n" if $debug;
}
####################################################################

sub NRPE_percent {
	my @lines; 	my $l; 	my $percent;
	if ($statustext) {
		print FP "Parsing statustext string: $statustext\n" if $debug;
		# parse status text:match first occurrence of xx%
		if ($statustext =~ / ([\d\.]+)%/i) {
			$percent = $1;
		} else {
			print FP "Invalid status text.\n" if $debug;
			return;
		}
	}
	print FP "Values: percent used=$percent\n" if $debug;
	my $rrdcommand = "$Rrdtool update $rrddir/$rrdname $lastcheck:$percent 2>&1";
	print FP qq($rrdcommand) if $debug;
	@lines = qx($rrdcommand);
	print FP "\nReturn: " . "@lines" . "\n" if $debug;
}
sub NRPE_local_disk {
	my @lines; 	my $l; 	my $percent;
	if ($statustext) {
		print FP "Parsing statustext string: $statustext\n" if $debug;
		# parse status text:match first occurrence of xx%
		if ($statustext =~ / Used:.*?\(([\d\.]+)%\)/i) {
			$percent = $1;
		} else {
			print FP "Invalid status text.\n" if $debug;
			return;
		}
	}
	print FP "Values: percent used=$percent\n" if $debug;
	my $rrdcommand = "$Rrdtool update $rrddir/$rrdname $lastcheck:$percent 2>&1";
	print FP qq($rrdcommand) if $debug;
	@lines = qx($rrdcommand);
	print FP "\nReturn: " . "@lines" . "\n" if $debug;
}
sub NRPE_local_memory {
	my @lines; 	my $l; 	my $number;
	if ($statustext) {
		print FP "Parsing statustext string: $statustext\n" if $debug;
		# parse status text:match first occurrence of xx%
		if ($statustext =~ /([\d\.]+) available bytes of memory/i) {
			$number = $1;
		} else {
			print FP "Invalid status text.\n" if $debug;
			return;
		}
	}
	print FP "Values: number used=$number\n" if $debug;
	my $rrdcommand = "$Rrdtool update $rrddir/$rrdname $lastcheck:$number 2>&1";
	print FP qq($rrdcommand) if $debug;
	@lines = qx($rrdcommand);
	print FP "\nReturn: " . "@lines" . "\n" if $debug;
}
####################################################################
# Sub routine to create all RRD types
#
####################################################################
sub create_rrd {
	my $host=shift;
	my $service=shift;
	my @lines;
	my $newrrd = shift;
	my $create_rrd_cmd;
	my $cmd;
	if ($service =~ /(UNIX_disk_ssh|UNIX_memory_ssh|UNIX_swap_ssh)$/) {
		$create_rrd_cmd = 
			"$Rrdtool create $newrrd ".
			"--step 300 --start n-1yr ".
			"DS:percent:GAUGE:900:U:U ".
			"RRA:AVERAGE:0.5:1:2880 ".
			"RRA:AVERAGE:0.5:5:4032 ".
			"RRA:AVERAGE:0.5:15:5760 ".
			"RRA:AVERAGE:0.5:60:8640 ";
	} elsif ($service =~ /UNIX_load_ssh$/) {
		$create_rrd_cmd = 
			"$Rrdtool create $newrrd ".
			"--step 300 --start n-1yr ".
			"DS:number:GAUGE:900:U:U ".
			"RRA:AVERAGE:0.5:1:2880 ".
			"RRA:AVERAGE:0.5:5:4032 ".
			"RRA:AVERAGE:0.5:15:5760 ".
			"RRA:AVERAGE:0.5:60:8640 ";
	} elsif ($service =~ /SNMP_if$/) {
		$create_rrd_cmd = 
			"$Rrdtool create $newrrd ".
			"--step 300 --start n-1yr ".
			 "DS:in:COUNTER:900:U:U ".
			 "DS:out:COUNTER:900:U:U ".
			 "DS:discards:COUNTER:900:U:U ".
			 "DS:errors:COUNTER:900:U:U ".
			 "RRA:AVERAGE:0.5:1:2880 ".
			 "RRA:AVERAGE:0.5:5:4032 ".
			 "RRA:AVERAGE:0.5:15:5760 ".
			 "RRA:AVERAGE:0.5:60:8640 ";
	} elsif ($service =~ /SNMP_if_bandwidth$/) {
		$create_rrd_cmd = 
			"$Rrdtool create $newrrd ".
			"--step 300 --start n-1yr ".
			"DS:in:COUNTER:900:U:U ".
			"DS:out:COUNTER:900:U:U ".
			"DS:ifspeed:GAUGE:900:U:U ".
			"RRA:AVERAGE:0.5:1:2880 ".
			"RRA:AVERAGE:0.5:5:4032 ".
			"RRA:AVERAGE:0.5:15:5760 ".
			"RRA:AVERAGE:0.5:60:8640 ";
	} elsif ($service =~ /(NRPE_local_cpu|NRPE_local_disk|NRPE_local_pagefile)$/) {
		$create_rrd_cmd = 
			"$Rrdtool create $newrrd ".
			"--step 300 --start n-1yr ".
			"DS:percent:GAUGE:900:U:U ".
			"RRA:AVERAGE:0.5:1:2880 ".
			"RRA:AVERAGE:0.5:5:4032 ".
			"RRA:AVERAGE:0.5:15:5760 ".
			"RRA:AVERAGE:0.5:60:8640 ";
	} elsif ($service =~ /NRPE_local_memory$/) {
		$create_rrd_cmd = 
			"$Rrdtool create $newrrd ".
			"--step 300 --start n-1yr ".
			"DS:number:GAUGE:900:U:U ".
			"RRA:AVERAGE:0.5:1:2880 ".
			"RRA:AVERAGE:0.5:5:4032 ".
			"RRA:AVERAGE:0.5:15:5760 ".
			"RRA:AVERAGE:0.5:60:8640 ";
	} else {
		return 0;
	}
	@lines = qx($create_rrd_cmd);
	$cmd = "chown nagios.nagios $newrrd";
	@lines = qx($cmd);
	$cmd = "chmod g+w $newrrd";
	@lines = qx($cmd);
	return 1;
}

__END__

