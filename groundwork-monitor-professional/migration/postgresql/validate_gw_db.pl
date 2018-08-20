#!/usr/local/groundwork/perl/bin/perl -w --

# validate_gw_db.pl
#
# Script to encapsulate MySQL database-content validation checking
# needed before migration to a PostgreSQL-based installation.

# Copyright 2011 GroundWork, Inc. ("GroundWork").  All rights reserved.
# Use is subject to GroundWork commercial license terms.

use strict;

sub print_usage {
    print "usage:  validate_gw_db.pl [dbhost:dbname:dbuser:dbpass ...]\n";
    print "where:  dbhost is the hostname running the dbname database\n";
    print "        dbname is the database you wish to check\n";
    print "        dbuser is the username that has access to dbname\n";
    print "        dbpass is the password for username\n";
    print "        Checks on some databases (monarch, GWCollageDB, dashboard)\n";
    print "        are performed even without any arguments on the command\n";
    print "        line.  Any arguments are used to specify checking of\n";
    print "        the additional databases.  This is particularly useful\n";
    print "        for checking the jbossportal database.\n";
}

foreach my $arg (@ARGV) {
    if ($arg !~ /^[^:]+:[^:]+:[^:]+:./) {
        print_usage();
	exit 1;
    }
}

my %datasource = (
    monarch     => 'monarch',
    GWCollageDB => 'collage',
    dashboard   => 'insightreports'
);

foreach my $dbname (sort keys %datasource) {
    print "Checking $dbname for void pointers ...\n";
    my $void_pointers_outcome = system("/usr/local/groundwork/core/migration/find_void_pointers -c $datasource{$dbname}");
    if ($void_pointers_outcome) {
	print "FATAL:  Error in running find_void_pointers for the $dbname database.\n";
	print "FATAL:  Database validation has failed!\n";
	exit 1;
    }
}

# Each command-line argument is expected to be of the form:
# dbhost:dbname:dbuser:dbpass
foreach my $arg (@ARGV) {
    ## We leave $dbpass containing any extra colon characters.
    my ($dbhost, $dbname, $dbuser, $dbpass) = split /:/, $arg, 4;
    print "Checking $dbname for void pointers ...\n";
    my $void_pointers_outcome = system("/usr/local/groundwork/core/migration/find_void_pointers -c -h '$dbhost' -d '$dbname' -u '$dbuser' -p '$dbpass'");
    if ($void_pointers_outcome) {
	print "FATAL:  Error in running find_void_pointers for the $dbname database.\n";
	print "FATAL:  Database validation has failed!\n";
	exit 1;
    }
}

print "Checking monarch for duplicate rows ...\n";
my $duplicate_rows_outcome = system('/usr/local/groundwork/core/migration/find_duplicate_rows');
if ($duplicate_rows_outcome) {
    print "FATAL:  Error in running find_duplicate_rows.\n";
    print "FATAL:  Database validation has failed!\n";
    exit 1;
}
