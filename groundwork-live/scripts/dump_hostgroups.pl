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
# dump_hostgroups.pl
#
# Assign Hosts to Hostgroups reads entries from a text file
# Monarch using the Configuration API available in GroundWork Monitor 6.0 and higher.
#
# Change log:
#
# 2010-10-18	v0.1	Initial version.  Tested against GroundWork Monitor Enterprise 6.2
# 2012-04-17	v0.2	port to PostgreSQL, by using StorProc routines; run perltidy on the code

BEGIN {
    unshift @INC, "/usr/local/groundwork/core/monarch/lib";
}
use strict;
use MonarchStorProc;

my $version  = "0.2";
my $PROGNAME = "get_hostgroup.pl";
my $debug    = 0;

use Getopt::Long;
use Time::HiRes;
use vars qw($opt_d $opt_v $opt_h);

Getopt::Long::Configure('bundling');
my $status = GetOptions( "d=s" => \$opt_d, "debug=s" => \$opt_d, "v" => \$opt_v, "version" => \$opt_v, "h" => \$opt_h, "help" => \$opt_h );

if ( $opt_h && !$opt_v ) {
    print "Usage: $PROGNAME [-d|--debug <#>] [-v|--version] 
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

my $auth = StorProc->dbconnect();

my %hostgroups = StorProc->get_hostgroups('');

foreach my $group ( sort keys %hostgroups ) {
    foreach my $member ( @{ $hostgroups{$group}{'members'} } ) {
	print "$member \t $group\n";
    }
}

my $result = StorProc->dbdisconnect();

