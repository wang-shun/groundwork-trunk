#!/usr/local/groundwork/perl/bin/perl
#
# Copyright 2009, 2012 GroundWork Open Source, Inc. (GroundWork)
# All rights reserved. This program is free software; you can redistribute
# it and/or modify it under the terms of the GNU General Public License
# version 2 as published by the Free Software Foundation.
#
# This program is distributed in the hope that it will be useful, but
# WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
# General Public License for more details.
#
# You should have received a copy of the GNU General Public License along
# with this program; if not, write to the Free Software Foundation, Inc.,
# 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
#
# assign_servicegroup.pl
#
# Assign Hosts to Hostgroups reads entries from a text file
# Monarch using the Configuration API available in GroundWork Monitor 6.0 and higher.
#
# Change log:
#
# 2010-10-18	v0.1	Initial version.  Tested against GroundWork Monitor Enterprise 6.2
# 2012-04-17	v0.2	run perltidy on the code

BEGIN {
    unshift @INC, "/usr/local/groundwork/core/monarch/lib";
}
use strict;

my $version  = "0.2";
my $PROGNAME = "assign_servicegroup.pl";
my $debug    = 0;

use Getopt::Long;
use Time::HiRes;
use vars qw($opt_d $opt_f $opt_v);

my $start_time = Time::HiRes::time();

Getopt::Long::Configure('bundling');
my $status = GetOptions( "d=s" => \$opt_d, "debug=s" => \$opt_d, "v" => \$opt_v, "version" => \$opt_v, "f=s" => \$opt_f, "file=s" => \$opt_f );

if ( !$opt_f && !$opt_v ) {
    print "Usage: $PROGNAME [-d|--debug <#>] [-v|--version] -f|--file <FILENAME>
    where <#> is 0 or higher.  Set <#> to 2 or higher for file processing and
    Configuration API messages.\n";
    exit 0;
}

if ($opt_v) {
    print "Version: $PROGNAME $version\n";
    exit 0;
}

if ($opt_d) {
    $debug = $opt_d;
}

# Read the input file
open( DATA, '<', $opt_f ) or die("Error: Unable to read file $opt_f ($!)\n");

my $host_hashref = {};

if ( $debug > 1 ) {
    print "debug: processing file $opt_f\n";
}
while ( my $line = <DATA> ) {
    ## Format of file is
    ## host name \t hostgroup
    chomp $line;
    my ( $hostname, $servicename, $servicegroup ) = split( /\t/, $line );
    $host_hashref->{$hostname}->{servicename}  = $servicename;
    $host_hashref->{$hostname}->{servicegroup} = $servicegroup;

    if ( $debug > 1 ) {
	print "debug:   file entry - $line\n";
	print "debug: Hostname $hostname Servicename $servicename Servicegroup $servicegroup\n";
    }
}
close(DATA);

# Use the dassmonarch API
use dassmonarch;

# Construct an instance of class dassmonarch
my $monarchapi = dassmonarch->new();

if ( $debug > 1 ) { print "submitting hosts to Monarch\n"; }

foreach my $hostname ( keys %{$host_hashref} ) {

    #if (!$monarchapi->servicegroup_exists($host_hashref->{$hostname}->{servicegroup}))
    #	{
    #	print "skipping $host_hashref->{$hostname}->{hostgroup} does not exist.\n";
    #	next;
    #	}
    if ( !$monarchapi->host_exists($hostname) ) {
	print "skipping $hostname does not exist.\n";
	next;
    }

    if ( $debug > 1 ) { print "assigning $hostname,$host_hashref->{$hostname}\n"; }
    if ( !$monarchapi->assign_servicegroup( $hostname, $host_hashref->{$hostname}->{servicename}, $host_hashref->{$hostname}->{servicegroup} ) )
    {
	print
"assign_hostgroup  Failed to add host $hostname:$host_hashref->{$hostname}->{servicename} to group $host_hashref->{$hostname}->{servicegroup}\n";
    }
    sleep 1;
}

# Finish with summary statistics.
my $hosts = keys %{$host_hashref};
print "debug: completed processing of " 
  . $hosts
  . " group assignments in "
  . sprintf( '%0.3f', ( Time::HiRes::time() - $start_time ) )
  . " seconds.\n";
exit 0;

