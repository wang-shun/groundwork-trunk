#!/usr/local/groundwork/perl/bin/perl -w --
#
# migrate-dashboard.pl
#
############################################################################
# Release 1.1
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

my $all_is_done = 0;
my $db_properties_file = '/usr/local/groundwork/config/db.properties';

my ( $dbhost, $database, $user, $passwd ) = undef;
$database = 'dashboard';

if ( ! -e $db_properties_file ) {
    print "\n\tERROR: The $db_properties_file file does not exist!\n";
    exit 1;
}
if ( ! open( FILE, '<', $db_properties_file ) ) {
    print "\n\tERROR: Cannot open the $db_properties_file file!\n";
    exit 1;
}
while ( my $line = <FILE> ) {
    if ( $line =~ /\s*insightreports\.dbhost\s*=\s*(\S+)/ )   { $dbhost   = $1 }
    if ( $line =~ /\s*insightreports\.database\s*=\s*(\S+)/ ) { $database = $1 }
    if ( $line =~ /\s*insightreports\.username\s*=\s*(\S+)/ ) { $user     = $1 }
    if ( $line =~ /\s*insightreports\.password\s*=\s*(\S+)/ ) { $passwd   = $1 }
}
close(FILE);

##############################################################################
# Connect to DB
##############################################################################

my $dsn = "DBI:mysql:$database:$dbhost";
my $dbh = undef;
eval { $dbh = DBI->connect( $dsn, $user, $passwd, { 'RaiseError' => 1 } ) };
if ($@) {
    print "\n\tError: database connect failed ($@)\n";
    exit 1;
}

##############################################################################
# Prepare for database transformations
##############################################################################

print "\n\tUpdating $database database tables ...\n";

##############################################################################
# Add indexes to the dashboard database tables.
##############################################################################

sub table_has_primary_key {
    my $table_name = shift;
    my $sqlstmt = "select constraint_name from information_schema.table_constraints " .
	"where table_schema='$database' and table_name='$table_name' and constraint_name='PRIMARY'";
    my $sth = $dbh->prepare($sqlstmt);
    unless ( $sth->execute ) {
	print "\n\tERROR: $sqlstmt ($@)";
	exit 1;
    }
    my %fields = ();
    while ( my @values = $sth->fetchrow_array() ) {
	$fields{ $values[0] } = 1;
    }
    $sth->finish;
    return defined( $fields{'PRIMARY'} );
}

if ( table_has_primary_key ('host_availability') ) {
    print "\t\tThe host_availability table already has a primary key index.\n";
}
else {
    print "\t\tAdding index to host_availability ...\n";
    $dbh->do( 'alter table host_availability              add primary key (HOST_NAME,      DATESTAMP,               TIME_INTERVAL)' );
}
if ( table_has_primary_key ('service_availability') ) {
    print "\t\tThe service_availability table already has a primary key index.\n";
}
else {
    print "\t\tAdding index to service_availability ...\n";
    $dbh->do( 'alter table service_availability           add primary key (HOST_NAME,      DATESTAMP, SERVICE_NAME, TIME_INTERVAL)' );
}
if ( table_has_primary_key ('hostgroup_host_availability') ) {
    print "\t\tThe hostgroup_host_availability table already has a primary key index.\n";
}
else {
    print "\t\tAdding index to hostgroup_host_availability ...\n";
    $dbh->do( 'alter table hostgroup_host_availability    add primary key (HOSTGROUP_NAME, DATESTAMP,               TIME_INTERVAL)' );
}
if ( table_has_primary_key ('hostgroup_service_availability') ) {
    print "\t\tThe hostgroup_service_availability table already has a primary key index.\n";
}
else {
    print "\t\tAdding index to hostgroup_service_availability ...\n";
    $dbh->do( 'alter table hostgroup_service_availability add primary key (HOSTGROUP_NAME, DATESTAMP,               TIME_INTERVAL)' );
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
	print "\t    WARNING:  $database database migration did not fully complete!\n";
	print "\t====================================================================\n";
	print "\n";
	exit 1;
    }
}

print "\n\tUpdate of $database database tables is complete.\n\n";

