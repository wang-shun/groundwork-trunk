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
# add_host.pl
#
# Script that can be run regularly via cron, or manually, to import hosts into
# Monarch using the Configuration API available in GroundWork Monitor 6.0 and higher.
#
# Change log:
#
# 2010-10-18	v0.1
# 2012-04-17	v0.2	run perltidy on the code
# 2012-06-10	v0.3	final version phase 1

BEGIN {
    unshift @INC, "/usr/local/groundwork/core/monarch/lib";
}
use strict;

my $version  = "0.3";
my $PROGNAME = "add_host.pl";
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
    ## host name \t host address \t host alias \t host profile name
    chomp $line;
    if ( $line =~ /^#/ ) { next; }
    my ( $hostname, $hostaddress, $hostalias, $hostprofile, $parent ) = split( /\t/, $line );
    my $size_of_hostname = length ($hostname);   
    my $size_of_hostaddress = length ($hostaddress);   
    my $size_of_hostprofile = length ($hostprofile);   
# only use entries that are meaningful, ie include a name, address and profile and are not a comment
    if ( $size_of_hostname && $size_of_hostaddress && $size_of_hostprofile ) {
        $host_hashref->{$hostname}->{hostaddress} = $hostaddress;
        $host_hashref->{$hostname}->{hostalias}   = $hostalias;
        $host_hashref->{$hostname}->{hostprofile} = $hostprofile;
        $host_hashref->{$hostname}->{parent} = $parent;

        if ( $debug > 1 ) {
	    print "debug:   file entry - $line\n";
        }
    }
}
close(DATA);

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

foreach my $hostname ( keys %{$host_hashref} ) {
    my $result = $monarchapi->import_host_api(
	$hostname,
	$host_hashref->{$hostname}->{hostalias},
	$host_hashref->{$hostname}->{hostaddress},
	$host_hashref->{$hostname}->{hostprofile}, 1
    );
    if ( !$result && $debug > 1 ) {
	print "debug:   import_host exception for host $hostname\n";
    }
}

# Finish with summary statistics.
my $hosts = keys %{$host_hashref};
print "debug: completed processing of " 
  . $hosts
  . " host records in "
  . sprintf( '%0.3f', ( Time::HiRes::time() - $start_time ) )
  . " seconds.\n";
exit 0;
