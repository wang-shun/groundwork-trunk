#!/usr/local/groundwork/perl/bin/perl

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
#
# Script that can be run regularly via cron, or manually, to import hosts into
# Monarch using the Configuration API available in GroundWork Monitor 6.0 and higher.
#
# Change log:
#
# 2012-04-17	v0.2	run perltidy on the code

BEGIN {
    unshift @INC, "/usr/local/groundwork/core/monarch/lib";
}

use strict;

my $version  = "0.2";
my $PROGNAME = "add_hostgroup.pl";
my $debug    = 0;

use Getopt::Long;
use Time::HiRes;
use vars qw($opt_d $opt_f $opt_v);
use dassmonarch;
my $monarchapi = dassmonarch->new();

my $start_time = Time::HiRes::time();

Getopt::Long::Configure('bundling');
my $status = GetOptions( "d=s" => \$opt_d, "debug=s" => \$opt_d, "v" => \$opt_v, "version" => \$opt_v, "f=s" => \$opt_f, "file=s" => \$opt_f );

if ( !$opt_f && !$opt_v ) {
    print "Usage: $PROGNAME [-d|--debug <#>] [-v|--version] -f|--file <FILENAME>
    where <#> is 0 or higher.  Set <#> to 2 or higher for file processing and
    Configuration API messages.";
    exit 0;
}

if ($opt_v) {
    print "Version: $PROGNAME $version\n";
    exit 0;
}

if ($opt_d) {
    $debug = $opt_d;
}

if ( $debug > 1 ) {
    print "debug: submitting hostgroups to Monarch\n";
    $monarchapi->set_debuglevel('verbose');
}
else {
    $monarchapi->set_debuglevel('none');
}
my $count = 0;

# Read the input file
open( DATA, '<', $opt_f ) or die("Error: Unable to read file $opt_f ($!)\n");

if ( $debug > 1 ) {
    print "debug: processing file $opt_f\n";
}
while ( my $line = <DATA> ) {
    ## Format of file is
    ## host group \t group alias
    chomp $line;
    my ( $groupname, $groupalias ) = split( /\t/, $line );
    $count++;

    if ( $groupname =~ /^#/ ) { next; }
    my $size_of_groupname = length ($groupname);   
    if ( $size_of_groupname ) {
        my $result = $monarchapi->create_hostgroup( $groupname, $groupalias );
        if ( !$result ) {
	    print "debug:   import_group exception for group $groupname\n";
        }
    }
}
close(DATA);

# Finish with summary statistics.
print "debug: completed processing of " 
  . $count
  . " group records in "
  . sprintf( '%0.3f', ( Time::HiRes::time() - $start_time ) )
  . " seconds.\n";
exit 0;

