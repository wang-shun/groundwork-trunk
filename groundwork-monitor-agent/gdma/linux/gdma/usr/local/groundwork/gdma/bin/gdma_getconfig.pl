#!/usr/bin/perl -w --

##
##	gdma_getconfig.pl
##
##	Get all configuration file(s) for this gdma host.
##	(C) 2007, Groundwork Open Source, Inc.  All Rights Reserved.
##

use strict;
use Sys::Hostname;
use Getopt::Std;

##
##	Help/Usage String
##

my $helpstring = "
This script will copy the GroundWork monitoring configuration file
from the central server to this server.

  Options:
  --------
    -H <hostname>      Hostname of GroundWork Server. [REQUIRED]
    -c <config file>   Config file containing monitoring parameters.
    -d                 Debug mode. Will log additional messages to the log file.
    -h or -help        Displays help message.\n\n";

##
##	Options
##

my $os = `uname -s`;
chomp $os;
my $solaris = ($os eq 'SunOS');

my %opt = ();
my $remote_dir    = "/usr/local/groundwork/gdma/config";
my $gw_gdma_home;
my $id_dsa_file;
if ($solaris) {
    $gw_gdma_home = "/opt/groundwork/home/gdma/config";
    $id_dsa_file  = "/opt/groundwork/home/gdma/.ssh/id_dsa";
} else {
    # For Linux, and for all other platforms until we extend this code.
    $gw_gdma_home = "/usr/local/groundwork/gdma/config";
    $id_dsa_file  = "/usr/local/groundwork/gdma/.ssh/id_dsa";
}
my $fqhn          = hostname();
my @host_parseout = split(/\./, $fqhn);
my $hostname      = $host_parseout[0];
my $gwserver      = "";
my $debug         = 0;
my $configfile;
my $scpstmt;

##
##	Say Hello
##

print "================================================\n";
print "gdma_getconfig.pl\n";
print "Copyright 2003-2007 Groundwork Open Source, Inc.\n";
print "================================================\n";

##
##	Collect user-supplied options.
##

getopts("dH:hc:",\%opt);
if ($opt{h} or $opt{help}) {
    print $helpstring;
    exit;
}
if ($opt{d}) {
    $debug = 1;
}

if ($opt{H}) {
    $gwserver = $opt{H};
} else {
    print $helpstring;
    exit;
}

if ($opt{c}) {
    $configfile = $opt{c};
} else {
    $configfile = "gwmon_$hostname.cfg";
}

print "GroundWork Server:  [$gwserver]\n";
print "Configuration File: [$configfile]\n";

##
##	Create scp command line and execute the copy operation.
##

$scpstmt = "scp -B -p -i $id_dsa_file gdma\@$gwserver:$remote_dir/$configfile $gw_gdma_home";
print "Transferring configuration [$configfile] from Groundwork Server [$gwserver]\n";
print "Command: $scpstmt"."\n" if $debug;
my $res = qx/$scpstmt 2>&1/;

##
##	Report on result of transfer.
##

if (!$res)
{
    print "Transfer OK.\n";
}
else
{
    print "Transfer FAILED. Error detail follows:\n";
    print "--------------------------------------\n";
    print $res;
    print "--------------------------------------\n";
}

##
##	... And exit.
##

exit;

