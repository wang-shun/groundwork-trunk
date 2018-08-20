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
# get_hostgroup.pl
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
my $PROGNAME = "get_hostgroup.pl";
my $debug    = 0;

use Getopt::Long;
use Time::HiRes;
use vars qw($opt_d $opt_g $opt_v);

my $start_time = Time::HiRes::time();

Getopt::Long::Configure('bundling');
my $status = GetOptions( "d=s" => \$opt_d, "debug=s" => \$opt_d, "v" => \$opt_v, "version" => \$opt_v, "g=s" => \$opt_g, "group=s" => \$opt_g );

if ( !$opt_g && !$opt_v ) {
    print "Usage: $PROGNAME [-d|--debug <#>] [-v|--version] -g|--group <hostgroup name>
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
my $group = $opt_g;

# Use the dassmonarch API
use dassmonarch;

# Construct an instance of class dassmonarch
my $monarchapi = dassmonarch->new();
my @hosts      = ();

if ( !$monarchapi->hostgroup_exists($group) ) {
    print "$group does not exist.\n";
    exit;
}

@hosts = $monarchapi->get_hosts_in_hostgroup($group);
foreach (@hosts) { print; print "\n"; }
exit;

