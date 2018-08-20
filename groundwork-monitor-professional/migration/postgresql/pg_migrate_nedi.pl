#!/usr/local/groundwork/perl/bin/perl -w --
#
# Copyright 2015-2018 GroundWork, Inc. ("GroundWork")
# All rights reserved.
#

# This script invokes NeDi with the right options to update an existing
# database to the current desired schema and content.

use strict;
use warnings;

use Getopt::Std;

sub print_help {
    print <<EOF;
usage:  pg_migrate_nedi.pl [-U /path/to/nedi.conf]
EOF
}

my %opt;

getopts( 'hU:', \%opt ) or do {
    print_help();
    exit(1);
};

if ( $opt{h} ) {
    print_help();
    exit(0);
}

# We will need to revise this script for future releases, generalizing the logic
# to make it less hardcoded for a particular NeDi version and more inclusive of
# historical transitions.
my $target_nedi_version = '1.8.100';

# In the NeDi 1.8.100 release, the exit status from an attempted database-version
# update should tell us whether the update succeeded.  However, the fact that the
# NeDi 1.8.100 release only supports an update from a 1.7.090 or 1.7.090p1 database
# means that an upgrade from the NeDi 1.6.100 release in a much earlier GWMEE release
# will fail.  And that circumstance will abort the GWMEE installer's post-upgrade
# processing and potentially cause later errors during a GWMEE release upgrade.  We
# check the database version here because the error message we produce now will be
# much more informative than the terse message printed by nedi.pl if it does not
# support an upgrade from the database version we have in hand.

my $psql         = '/usr/local/groundwork/postgresql/bin/psql';
my $pg_user      = defined( $ENV{PGPASSWORD} ) ? '' : '-U nedi';
my $psql_command = qq($psql $pg_user -d nedi -t -c "select value from system where name = 'version';");
my $shell_command;
if ( $> == 0 ) {
    $psql_command =~ s/"/\\"/g;
    $shell_command = qq(su nagios -c "$psql_command");
}
else {
    $shell_command = $psql_command;
}
my $existing_nedi_version = qx(cd /tmp; $shell_command);
if ( $? != 0 ) {
    print "ERROR:  Cannot determine the version of the existing nedi database!\n";
    exit 1;
}
$existing_nedi_version =~ s/^\s+|\s+$//g;
## We match both 1.7.090 and 1.7.090p1 versions.
if ( $existing_nedi_version !~ /1.7.090/ ) {
    print <<EOF;
ERROR:  Cannot update the schema of the existing \"nedi\" database.
        Only upgrades from 1.7.090 or 1.7.090p1 databases are supported.
        Your current database version is:  $existing_nedi_version
        The intended database version is:  $target_nedi_version
EOF

    if ( $existing_nedi_version =~ /\Q$target_nedi_version\E/ ) {
	print <<EOF;
        It looks like your database is already up to date.
EOF
    }
    else {
	print <<EOF;

WARNING:  The NeDi database is NOT being upgraded at this time, because
the current NeDi code is not equipped to perform the upgrade.  If you
wish to use NeDi after an upgrade of GWMEE, you can only re-initialize
the "nedi" database from scratch, thereby losing all previous data in the
database.  To do that, run the following command and answer the prompts:

    /usr/local/groundwork/nedi/nedi.pl -i

EOF
    }

    ## FIX MINOR:  If we wish to allow a GWMEE upgrade to complete without
    ## generating any warnings visible in the terminal window after this
    ## failure, change this to "exit 0;".
    exit 1;
}

# NeDi 1.8.100 provides the ability to upgrade from a 1.7.090 or 1.7.090p1
# database.  No other upgrade paths are currently supported.  Having checked
# the version above, we should be able to rely on the exit code now to tell
# whether or not the version update succeeded, and reflect that in the exit
# code from this script so the GWMEE installer can act accordingly.
#
# Execution of this database update depends critically on the nedi/nedi.conf
# file in place at this moment containing the current pw for the nedi database.
# That may be problematic if the customer has changed the default pw and we
# have an updated nedi/nedi.conf file, containing a default pw, in place
# during an upgrade from an earlier release.  The -U option can be used to
# specify an alternative nedi.conf file, such as the one that existed before
# the upgrade.
#
die "ERROR:  Invalid value of the -U option ($opt{U}).\n" if defined( $opt{U} ) && $opt{U} =~ /'/;
my $conf_file_option = $opt{U} ? "-U '$opt{U}'" : '';
my $wait_status = system("/usr/local/groundwork/nedi/nedi.pl $conf_file_option -i updatedb");

exit( $wait_status ? 1 : 0 );

