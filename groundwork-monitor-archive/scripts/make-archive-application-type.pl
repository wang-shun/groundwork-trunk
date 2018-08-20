#!/usr/local/groundwork/perl/bin/perl -w --

#
# make-archive-application-type.pl
#

# Script to install the ARCHIVE application type in a runtime database.

# Copyright (c) 2013 GroundWork, Inc. (www.gwos.com).  All rights reserved.
# Use is subject to GroundWork commercial license terms.

use strict;
use DBI;

my $VERSION = "0.0.6";

my $sth   = undef;
my $query = undef;

my $outcome = 1;

sub print_usage {
    print "usage:  make-archive-application-type.pl -m\n";
    print "or:     make-archive-application-type.pl -v\n";
    print "or:     make-archive-application-type.pl -h\n";
    print "where:  -m makes the application type\n";
    print "        -v prints the version of this script\n";
    print "        -h prints this help message\n";
    exit 1;
}

if ( scalar @ARGV != 1 or $ARGV[0] eq '-h' ) {
    print_usage();
    exit 1;
}

if ( $ARGV[0] eq '-v' ) {
    print "Version: $VERSION\n";
    exit 0;
}

if ( $ARGV[0] ne '-m' ) {
    print_usage();
    exit 1;
}

my $properties_file = '/usr/local/groundwork/config/db.properties';
my ( $dbtype, $dbhost, $dbname, $dbuser, $dbpass ) = undef;
my $db = 'collage';
if ( -e '/usr/local/groundwork/config/db.properties' ) {
    open( FILE, '<', $properties_file ) or die "ERROR:  Cannot open $properties_file\n";
    while ( my $line = <FILE> ) {
	if ( $line =~ /^\s*global\.db\.type\s*=\s*(\S+)/ ) { $dbtype = $1 }
	if ( $line =~ /^\s*$db\.dbhost\s*=\s*(\S+)/ )      { $dbhost = $1 }
	if ( $line =~ /^\s*$db\.database\s*=\s*(\S+)/ )    { $dbname = $1 }
	if ( $line =~ /^\s*$db\.username\s*=\s*(\S+)/ )    { $dbuser = $1 }
	if ( $line =~ /^\s*$db\.password\s*=\s*(\S+)/ )    { $dbpass = $1 }
    }
    close(FILE);
}

die "FATAL:  Cannot find the \"$db\" database in $properties_file\n" if not defined $dbname;

##############################################################################
# Connect to DB
##############################################################################

if ( $dbtype eq 'postgresql' ) {
    delete $ENV{PGCLIENTENCODING};
    delete $ENV{PGDATABASE};
    delete $ENV{PGDATESTYLE};
    delete $ENV{PGGEQO};
    delete $ENV{PGHOSTADDR};
    delete $ENV{PGHOST};
    delete $ENV{PGLOCALEDIR};
    delete $ENV{PGOPTIONS};
    delete $ENV{PGPASSFILE};
    delete $ENV{PGPASSWORD};
    delete $ENV{PGPORT};
    delete $ENV{PGSERVICEFILE};
    delete $ENV{PGSERVICE};
    delete $ENV{PGSYSCONFDIR};
    delete $ENV{PGTZ};
    delete $ENV{PGUSER};
    $ENV{PGCONNECT_TIMEOUT} = 20;
    $ENV{PGREQUIREPEER}     = 'postgres';
    $ENV{SHELL}             = '/bin/false';
    $ENV{PATH}              = '/bin:/sbin:/usr/bin:/usr/sbin';
}

my $dsn = '';
if ( defined($dbtype) && $dbtype eq 'postgresql' ) {
    $dsn = "DBI:Pg:dbname=$dbname;host=$dbhost";
}
else {
    $dsn = "DBI:mysql:database=$dbname;host=$dbhost";
}
my $dbh = undef;

# We turn PrintError off because RaiseError is on and we don't want duplicate messages printed.
eval { $dbh = DBI->connect( $dsn, $dbuser, $dbpass, { 'RaiseError' => 1, 'PrintError' => 0 } ) };
if ($@) {
    my $errstr = $@;
    chomp $errstr;
    print "\nError: connect failed ($errstr)\n";
    die;
}

##############################################################################
# Modify the database
##############################################################################

# The insert for application type is simple because no dynamic properties need to be specified.
# However, we make sure that the code below can be executed idempotently.

my $name                    = 'ARCHIVE';
my $description             = 'Archiving related messages';
my $statetransitioncriteria = 'Device;Host';

$query = "INSERT INTO ApplicationType(name, description, statetransitioncriteria) VALUES ('$name', '$description', '$statetransitioncriteria')";

eval {
    $sth = $dbh->prepare($query);
    $sth->execute;
    $sth->finish;
};
if ($@) {
    my $errstr = $@;
    chomp $errstr;
    if ( $errstr !~ / duplicate key value violates unique constraint / ) {
	print "Insertion of application type failed ($errstr).\n";
	$outcome = 0;
    }
    else {
	$query = "SELECT description, statetransitioncriteria FROM ApplicationType where name = '$name'";
	eval {
	    my $hashref = $dbh->selectrow_hashref($query);
	    if ( $hashref->{description} eq $description and $hashref->{statetransitioncriteria} eq $statetransitioncriteria ) {
		print "NOTICE:  The ARCHIVE application type already exists in the database.\n";
	    }
	    else {
		print "ERROR:  The ARCHIVE application type already exists in the database,\n";
		print "        but with unexpected description/statetransitioncriteria values.\n";
		$outcome = 0;
	    }
	};
	if ($@) {
	    my $errstr = $@;
	    chomp $errstr;
	    print "ERROR:  The ARCHIVE application type already exists in the database,\n";
	    print "        but we cannot check its associated values ($errstr).\n";
	    $outcome = 0;
	}
    }
}

if ( $outcome == 1 ) {
    print "Done establishing the ARCHIVE application type.\n";
    exit 0;
}

# If we didn't confirm a successful outcome above, then emit a failure status.
exit 1;

