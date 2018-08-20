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
# disable_service_checks.pl
#
# Script that can be run regularly via cron, or manually, to import hosts into
# Monarch using the Configuration API available in GroundWork Monitor 6.0 and higher.
#
# Change log:
#
# 2010-10-18	v0.1
# 2012-04-17	v0.2	run perltidy on the code

BEGIN {
    unshift @INC, "/usr/local/groundwork/core/monarch/lib";
}
use strict;

my $version  = "0.2";
my $PROGNAME = "disable_service_checks.pl";
my $debug    = 0;

use Getopt::Long;
use Time::HiRes;
use vars qw($opt_d $opt_f $opt_v);

my $start_time = Time::HiRes::time();

Getopt::Long::Configure('bundling');
my $status = GetOptions( "d=s" => \$opt_d, "debug=s" => \$opt_d );

my $input = $ARGV[0];

if ($opt_d) {
    $debug = $opt_d;
}

# Read the input file
open( DATA, '<', $input. "ALLGROUPS") or die("Error: Unable to read file" .$input. "ALLGROUPS\n");

my $host_hashref = {};

if ( $debug > 1 ) {
    print "debug: processing file $opt_f\n";
}

# Use the dassmonarch API
use dassmonarch;

# Construct an instance of class dassmonarch
my $monarchapi = dassmonarch->new();

# Set this to error, in order to get minimal debug messages, verbose creates a lot of output
if ( $debug > 2 ) {
    print "debug: submitting hosts to Monarch\n";
    $monarchapi->set_debuglevel('verbose');
}
else {
    $monarchapi->set_debuglevel('none');
}

while ( my $line = <DATA> ) {
    ## Format of file is
    ## host group \t hostgroup alias 
    chomp $line;
    my ( $hostgroup, $hostalias ) = split( /\t/, $line );

    if ( $debug > 1 ) {
	print "debug:   file entry - $line\n";
    }
    my $result = $monarchapi->disable_all_active_service_checks_on_hostgroup ( $hostgroup );
    if ( !$result && $debug > 1 ) {
	print "debug:   disable_ all_active_service_checks_on_hostgroup exception for $hostgroup\n";
    }
}
close(DATA);

exit 0;
