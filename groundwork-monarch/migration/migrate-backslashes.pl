#!/usr/local/groundwork/perl/bin/perl -w --
# MonArch - Groundwork Monitor Architect
# migrate-backslashes.pl
#
# This script is used to transform the backslashes in command lines which
# are interpreted by Nagios 3.x and later releases, to accommodate the fact
# that Nagios now uses backslashes to escape bang characters (!) which are
# not to be interpreted as command-line argument separators.
#
# The tricky part here is not the transformation itself, but the fact that
# we need to protect against running this script more than once, so we don't
# redouble the backslashes and break things in the opposite sense (too many
# instead of too few backslashes).
#
# This script should ONLY be run in consultation with GroundWork Support.
# See Support TECH-NOTE-GW-5.3.0-M02 (available on GW Connect) for details.
#
############################################################################
# Version 1.1
# March 2009
############################################################################
#
# Author: Glenn Herteg
#
# Copyright 2009 GroundWork Open Source, Inc. (GroundWork)
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

use DBI;
use strict;

my $all_is_done    = 0;
my $monarch_home   = 0;
my $nagios_version = 0;
my $db_properties  = '/usr/local/groundwork/config/db.properties';

my ( $dbhost, $database, $user, $passwd ) = undef;
if ( -e $db_properties ) {
    open( FILE, '<', $db_properties ) ||
	die "\n\tCannot open $db_properties for reading; aborting!\n";
    while ( my $line = <FILE> ) {
	if ( $line =~ /\s*monarch\.dbhost\s*=\s*(\S+)/ )   { $dbhost   = $1 }
	if ( $line =~ /\s*monarch\.database\s*=\s*(\S+)/ ) { $database = $1 }
	if ( $line =~ /\s*monarch\.username\s*=\s*(\S+)/ ) { $user     = $1 }
	if ( $line =~ /\s*monarch\.password\s*=\s*(\S+)/ ) { $passwd   = $1 }
    }
    close(FILE);
    $monarch_home   = '/usr/local/groundwork/core/monarch';
}
else {
    print "\n\tMonarch Update";
    print "\n=============================================================\n";
    print "\n\tReading configuration file ...\n";

    until ($monarch_home) {
	if ( -e "/usr/local/groundwork/core/monarch/lib/MonarchConf.pm" ) {
	    $monarch_home = "/usr/local/groundwork/core/monarch";
	    print "\n\tPlease enter the Monarch installation path [ $monarch_home ] : ";
	    my $input = <STDIN>;
	    chomp $input;
	    if ($input) { $monarch_home = $input }
	    my $monarch_test = $monarch_home . '/lib/MonarchConf.pm';
	    unless ( -e $monarch_test ) {
		print "\n\tError: Cannot locate MonarchConf.pm in path $monarch_home [/lib] ...\n";
		$monarch_home = 0;
	    }
	}
	else {
	    print "\n\tPlease enter the Monarch installation path: ";
	    my $input = <STDIN>;
	    chomp $input;
	    if ($input) { $monarch_home = $input }
	    my $monarch_test = $monarch_home . '/lib/MonarchConf.pm';
	    unless ( -e $monarch_test ) {
		print "\n\tError: Cannot locate MonarchConf.pm in path $monarch_home [/lib] ...\n";
		$monarch_home = 0;
	    }
	}
    }
    open( FILE, '<', "$monarch_home/lib/MonarchConf.pm" ) ||
	die "\n\tCannot open $monarch_home/lib/MonarchConf.pm for reading; aborting!\n";
    while ( my $line = <FILE> ) {
	$line =~ s/\'|\"|;//g;
	if ( $line =~ /\s*\$dbhost\s*=\s*(\S+)/ )   { $dbhost   = $1 }
	if ( $line =~ /\s*\$database\s*=\s*(\S+)/ ) { $database = $1 }
	if ( $line =~ /\s*\$dbuser\s*=\s*(\S+)/ )   { $user     = $1 }
	if ( $line =~ /\s*\$dbpass\s*=\s*(\S+)/ )   { $passwd   = $1 }
    }
    close(FILE);
}

##############################################################################
# Connect to DB
##############################################################################

print "\n\tConnecting to $database with user $user ...\n";

my $dsn = "DBI:mysql:$database:$dbhost";
my $dbh = undef;
# We turn off AutoCommit so we can implement transactions, which are critical for the work in this script.
eval { $dbh = DBI->connect( $dsn, $user, $passwd, { 'RaiseError' => 1, PrintError => 0, AutoCommit => 0 } ) };
if ($@) {
    print "\nError: connect failed ($@)\n";
    die;
}

##############################################################################
# Nagios 3.0: Modify Data in Existing Tables
##############################################################################

my $sqlstmt = '';
my $sth     = undef;

$sqlstmt = "select value from setup where name = 'nagios_version'";
$nagios_version = $dbh->selectrow_array($sqlstmt);

if ( $nagios_version !~ /^3\.?/ ) {
    print "\n\tWARNING:  You are not operating with Nagios version 3,\n\tso this script will not modify anything in the database.\n";
    exit 1;
}

print "\n\tUpdating tables ...\n";

# The doubled_backslashes value will be used to determine what transformations have been run on the databse.
# doubled_backslashes == undefined   => no transformations have been run; but this does not necessarily mean
#					that they should be run, as it may simply be the case that the user
#					has made equivalent changes manually, or there were never any such
#					issues in the database to begin with, and all subsequent database
#					edits have followed the new doubled-backslash conventions
# doubled_backslashes == -1          => no transformations have been run
# doubled_backslashes == 0           => no transformations have been run
# doubled_backslashes == 1           => the service_names, service_templates, services, and service_instance
#					tables have been modified to follow the new doubled-backslash convention
# doubled_backslashes == 2           => reserved for future definition, in case we later discover that some
#					other tables also need the same kind of substitutions

my $doubled_backslashes = -1;
my $db_doubled_backslashes;

$sqlstmt = "select value from setup where name = 'doubled_backslashes' and type = 'migration'";
($db_doubled_backslashes) = $dbh->selectrow_array($sqlstmt);

if (defined ($db_doubled_backslashes)) {
    $doubled_backslashes = $db_doubled_backslashes;
}

if ( $doubled_backslashes < 1 ) {

    # place the transaction in an eval{} block so that errors can be trapped for rollback
    eval {
	# The import_services table appears to not be used anywhere within Monarch, or we would
	# similarly convert its command_line and possibly also command_line_trans columns.
	# Note that we need to double the desired backslashes once here to accommodate Perl's
	# interpretation of them, then again to accommodate MySQL's interpretation of them.
	$dbh->do( "update service_names     set command_line = replace(command_line, '\\\\', '\\\\\\\\')" );
	$dbh->do( "update service_templates set command_line = replace(command_line, '\\\\', '\\\\\\\\')" );
	$dbh->do( "update services          set command_line = replace(command_line, '\\\\', '\\\\\\\\')" );
	$dbh->do( "update service_instance  set arguments    = replace(arguments,    '\\\\', '\\\\\\\\')" );
	# Get the proper flag value into the database, in the same transaction.
	if ( $doubled_backslashes < 0 ) {
	    $sqlstmt = "insert into setup values('doubled_backslashes','migration','1')";
	}
	else {
	    $sqlstmt = "update setup set value = '1' where name = 'doubled_backslashes' and type = 'migration'";
	}
	$dbh->do( $sqlstmt );

	# If we got this far, no errors occurred, so it's safe to commit the changes.
	$dbh->commit();
    };

    # If any errors occurred, roll back all the changes instead.
    if ($@) {
	print "\n\tERROR:  Doubling of backslashes aborted:\n\t$@";
	eval { $dbh->rollback };
	exit 1;
    }
}
else {
    print "\tDoubling of backslashes was previously done;\n\tno changes were made during this run.\n";
}

##############################################################################
# Done.
##############################################################################

$all_is_done = 1;

END {
    $dbh->disconnect() if $dbh;
    if (!$all_is_done) {
	print "\n";
	print "\t====================================================================\n";
	print "\t    WARNING:  monarch database migration did not fully complete!\n";
	print "\t====================================================================\n";
	print "\n";
	exit 1;
    }
}

print "\n\tUpdate complete.\n\n";

