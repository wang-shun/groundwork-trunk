#!/usr/local/groundwork/perl/bin/perl

# Copyright 2009 GroundWork Open Source, Inc. (GroundWork)
# All rights reserved. This program is free software; you can redistribute it and/or
# modify it under the terms of the GNU General Public License version 2 as published
# by the Free Software Foundation.
#
# This program is distributed in the hope that it will be useful, but WITHOUT ANY
# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
# PARTICULAR PURPOSE.  See the GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License along with this
# program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street,
# Fifth Floor, Boston, MA 02110-1301, USA.

# Author Dr. Dave Blunt at GroundWork Open Source, Inc. (dblunt at gwos dot com)
# Updated 1-14-2009 Kevin Stone (kstone at gwos dot com)

use strict;
use lib qw(/usr/local/groundwork/nagios/libexec);
use Getopt::Long;
use Sys::Load qw/getload uptime/;
use vars qw($opt_h $opt_v $opt_w $opt_c);
use vars qw($PROGNAME);
use utils qw($TIMEOUT %ERRORS &print_revision &support &usage);

sub print_usage () {
	print "Usage: $PROGNAME -w|--warning <warning> -c|--critical <critical> [ -h|--help -v|--version ]\n";
}

sub print_help () {
	print_revision($PROGNAME,'$Revision: 1.1 $');
	print "Copyright (c) 2009 GroundWork Open Source, Inc.\n";
	print_usage();
	print "The required arguments for this plugin are warning and critical.  These are
used to compare against the number of seconds reported for system up time.  If system up time
is lower than the specified thresholds, the threshold is considered to have been exceeded.


  -h, --help     Print this help
  -v, --version  Print version of this plugin
  -w, --warning  Warning threshold for system up time in seconds
  -c, --critical Critical threshold for system up time in seconds
";
};

$PROGNAME = "check_system_uptime";

Getopt::Long::Configure('bundling');

my $status = GetOptions
       ("v"	=> \$opt_v, "version"		=> \$opt_v,
	"h"	=> \$opt_h, "help"		=> \$opt_h,
	"w=s"	=> \$opt_w, "warning=s"		=> \$opt_w,
	"c=s"	=> \$opt_c, "critical=s"	=> \$opt_c,
);

if ($opt_v) {
	print revision($PROGNAME,'$Revision: 1.0 $');
	exit $ERRORS{'OK'};
}

if ($opt_h) {
	print_help();
	exit $ERRORS{'OK'};
}

if (! $opt_w || ! $opt_c) {
	print_usage();
	exit $ERRORS{'UNKNOWN'};
}

my $uname=`uname -a`;
my $localtime= time();
my $uptime_elapsed_seconds = int uptime();
chomp $uname;
chomp $localtime;

my $uptime_epoch = $localtime - $uptime_elapsed_seconds;

my ($seconds, $minutes, $hours, $day_of_month, $month, $year,$wday, $yday, $isdst) = localtime($uptime_epoch);
my $uptime_localtime = sprintf "%04d-%02d-%02d %02d:%02d:%02d",$year+1900,$month+1,$day_of_month,$hours,$minutes,$seconds;

my $pluginOut = "Uptime (s) = " . $uptime_elapsed_seconds . ".  " . $uname . ", ";
$pluginOut   .= "Last boot time (local clock) = " . $uptime_localtime; 

if ($uptime_elapsed_seconds <= $opt_c) {
  print "CRITICAL: " . $pluginOut;
  exit $ERRORS{'CRITICAL'};
} elsif ($uptime_elapsed_seconds <= $opt_w) {
  print "WARNING: " . $pluginOut;
  exit $ERRORS{'WARNING'};
} else {
  print "OK: " . $pluginOut;
  exit $ERRORS{'OK'};
}

exit $ERRORS{'OK'}
