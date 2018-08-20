#!/usr/local/groundwork/perl/bin/perl -w --
# MonArch - Groundwork Monitor Architect
# alter_graph_id_columns.pl
#
############################################################################
# Release 3.1
# November 2009
############################################################################
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
my $version        = '3.1';
my $is_portal      = 0;
my $monarch_home   = 0;
my $sqlstmt        = '';
my $sth            = undef;

my ( $dbhost, $database, $user, $passwd ) = undef;
if ( -e "/usr/local/groundwork/config/db.properties" ) {
    open( FILE, '<', '/usr/local/groundwork/config/db.properties' );
    while ( my $line = <FILE> ) {
	if ( $line =~ /\s*monarch\.dbhost\s*=\s*(\S+)/ )   { $dbhost   = $1 }
	if ( $line =~ /\s*monarch\.database\s*=\s*(\S+)/ ) { $database = $1 }
	if ( $line =~ /\s*monarch\.username\s*=\s*(\S+)/ ) { $user     = $1 }
	if ( $line =~ /\s*monarch\.password\s*=\s*(\S+)/ ) { $passwd   = $1 }
    }
    close(FILE);
    $is_portal      = 1;
    $monarch_home   = '/usr/local/groundwork/core/monarch';
}
else {
    print "\n\tMonarch $version Update";
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
	    print "\n\tPlease enter the Monarch installation path : ";
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
    open( FILE, '<', "$monarch_home/lib/MonarchConf.pm" );
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

print "\n\tConnecting to $database with user $user ...\n" unless $is_portal;

my $dsn = "DBI:mysql:$database:$dbhost";
my $dbh = undef;
# We turn PrintError off because RaiseError is on and we don't want duplicate messages printed.
eval { $dbh = DBI->connect( $dsn, $user, $passwd, { 'RaiseError' => 1, 'PrintError' => 0 } ) };
if ($@) {
    print "\nError: connect failed ($@)\n";
    die;
}

##############################################################################
# Modify Existing Tables
##############################################################################

print "\n\tModifying tables ...\n\n";

#-----------------------------------------------------------------------------
# Convert to int
#-----------------------------------------------------------------------------

# GWMON-8036

my %table_int = (
    'datatype'          => { 'datatype_id'          => 'AUTO_INCREMENT' },
    'host_service'      => { 'host_service_id'      => 'AUTO_INCREMENT',
			     'datatype_id'          => "default '0'"    },
    'performanceconfig' => { 'performanceconfig_id' => 'AUTO_INCREMENT' }
);
foreach my $table ( keys %table_int ) {
    $sqlstmt = "describe $table";
    $sth = $dbh->prepare($sqlstmt);
    $sth->execute();
    my %fields = ();
    while ( my @values = $sth->fetchrow_array() ) {
	$fields{ $values[0] } = $values[1];
    }
    $sth->finish;
    foreach my $column ( keys %{ $table_int{$table} } ) {
	unless ( $fields{$column} =~ /^int/i ) {
	    $dbh->do( "ALTER TABLE $table MODIFY $column INT(8) UNSIGNED $table_int{$table}{$column}" );
	}
    }
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

print "\n\tModifications complete.\n\n";

