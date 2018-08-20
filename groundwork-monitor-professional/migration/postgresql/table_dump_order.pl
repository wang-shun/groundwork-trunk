#!/usr/local/groundwork/perl/bin/perl -w --

#
# table_dump_order
#

# Copyright 2011 GroundWork Open Source, Inc. (GroundWork)
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

# To do:
# (*) Port this script so it operates with PostgreSQL databases as well.

use strict;
use DBI;

my $VERSION = '2.0.0';

if ( scalar @ARGV != 5 ) {
    print "usage:  table_dump_order {host} {port} {database} {username} {password}\n";
    exit 1;
}


my ( $dbhost, $dbport, $dbname, $dbuser, $dbpass ) = undef;

$dbhost=$ARGV[0];
$dbport=$ARGV[1];
$dbname=$ARGV[2];
$dbuser=$ARGV[3];
$dbpass=$ARGV[4];


##############################################################################
# Connect to DB
##############################################################################

my $dsn = "DBI:mysql:database=$dbname;host=$dbhost";
my $dbh = undef;
# We turn PrintError off because RaiseError is on and we don't want duplicate messages printed.
eval { $dbh = DBI->connect( $dsn, $dbuser, $dbpass, { 'RaiseError' => 1, 'PrintError' => 0 } ) };
if ($@) {
    print "\nError: connect failed ($@)\n";
    die;
}

##############################################################################
# Analyze database table relationships, and sequence the table names in an
# order which is safe to dump them in so a restore will never try to create
# a foreign key reference to a row that doesn't yet exist in the other table.
##############################################################################

my $sth = $dbh->prepare('show tables');
$sth->execute;
my %tables = ();
while ( my @values = $sth->fetchrow_array() ) {
    $tables{ $values[0] } = 1;
}
$sth->finish;

my @orderings = ();

foreach my $table (sort keys %tables) {
    my @values  = ();   
    my @lines   = ();
    my $sqlstmt = "show create table $table";
    my $sth     = $dbh->prepare($sqlstmt);
    $sth->execute;
    while ( @values = $sth->fetchrow_array() ) {
	# Because a given table can contain multiple foreign key references,
	# we have to split the data we get back into individual lines so we
	# can easily process multiple foreign key references for that table.
        push @lines, split(/\n/, $values[1]);
    }
    $sth->finish;
    my $base_table = undef;
    for (@lines) {
	# The lines we care about look like this:
	#   CREATE TABLE `contact_group` (
	if (/CREATE TABLE `(.*)`/) {
	    $base_table = $1;
	    # Make sure every table is mentioned in the final result, regardless of whether
	    # it has any foreign key references itself or is referenced by any other tables.
	    push @orderings, "$base_table $base_table\n";
	}
	# The lines we care about look like this:
	#   CONSTRAINT `contact_group_ibfk_1` FOREIGN KEY (`group_id`) REFERENCES `monarch_groups` (`group_id`) ON DELETE CASCADE,
	# (though perhaps there might not be an ON DELETE clause in a foreign key reference definition).
	if (/CONSTRAINT (.*) FOREIGN KEY (.*) REFERENCES (.+) (\([^)]+\))/) {
	    (my $foreign_table = $3) =~ s/`//g;
	    # Pairs of table names represent ordering relationships.
	    # The first table must come before the second table in the final results, so all
	    # foreign-table rows are populated before the rows in other tables that refer to them.
	    push @orderings, "$foreign_table $base_table\n";
	}
    }
}
$dbh->disconnect();

open (TSORT, '|-', 'tsort');
print TSORT @orderings;
close TSORT;

exit 0;

__END__
