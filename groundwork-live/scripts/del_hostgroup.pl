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
# del_hostgroup.pl
#
# Script that can be run to delete hostgroups that you added.
# Monarch using the Configuration API available in GroundWork Monitor 6.0 and higher.
#
# Change log:
#
# 2010-10-19	v0.1
# 2012-04-17	v0.2	run perltidy on the code

BEGIN {
    unshift @INC, "/usr/local/groundwork/core/monarch/lib";
}
use strict;

my $version  = "0.2";
my $PROGNAME = "del_hostgroup.pl";
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
my @hostgroupnames;
if ( $debug > 1 ) {
    print "debug: processing file $opt_f\n";
}
while ( my $line = <DATA> ) {
    ## Format of file is
    ## hostgroup name \t other data
    chomp $line;
    my ($hostgroupname) = split( /\t/, $line );
    push( @hostgroupnames, $hostgroupname );
    if ( $debug > 1 ) {
	print "debug:   file entry - $line\n";
	print "debug:   hostgroup entry - $hostgroupname\n";
    }
}
close(DATA);
my $count;

# Use the dassmonarch API
use dassmonarch;

# Construct an instance of class dassmonarch
my $monarchapi = dassmonarch->new();

# Set this to error, in order to get minimal debug messages, verbose creates a lot of output
if ( $debug > 2 ) {
    print "debug: deleting hostgroups from Monarch\n";
    $monarchapi->set_debuglevel('verbose');
}
else {
    $monarchapi->set_debuglevel('none');
}

foreach (@hostgroupnames) {
    my $result = $monarchapi->delete_hostgroup($_);
    $count++;
    if ( !$result ) {
	print "delete_hostgroup exception for hostgroup $_\n";
    }
}

# Finish with summary statistics.
print "Completed processing of " . $count . " hostgroup records in " . sprintf( '%0.3f', ( Time::HiRes::time() - $start_time ) ) . " seconds.\n";

exit 0;
