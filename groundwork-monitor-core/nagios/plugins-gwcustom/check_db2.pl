#!/usr/local/groundwork/perl/bin/perl -w --
#
# This is a plugin for monitoring IBM DB2 databases.
#
# Copyright 2005-2016 GroundWork Open Source, Inc.
#
# This program is free software; you can redistribute it and/or
# modify it under the terms of the GNU General Public License
# as published by the Free Software Foundation; either version 2
# of the License, or (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# Revision History
# 1.0.0 25-Oct-2005 Harper Mann
#	Initial Revision
# 1.1.0 15-Dec-2005 Harper Mann
#	Added low thresholds
# 2.0.0 22-Mar-2011 Glenn Herteg
#	Fixed obvious bugs.
# 2.0.1 16-Sep-2016 Glenn Herteg
#	Emit a sane message and exit code if DBD::DB2 is not installed.
#	Allow a usage message to print under --help even if DBD::DB2 is not installed.
#	Provide references to info on setting up the DBD::DB2 package.

use strict;

require 5.003;

use Getopt::Long;
use Scalar::Util qw(looks_like_number);

use utils qw($TIMEOUT %ERRORS &print_revision &support);

use vars qw($VERSION $PROGNAME $logfile $debug $state);
use vars qw($dbh $database $username $password $message);
use vars qw($sql $sth);
use vars qw($privsok $opt_warn $opt_crit);
use vars qw($low_warn $high_warn $low_crit $high_crit);
use vars qw($opt_select $newline $perfdata);

$VERSION = '2.0.1';
$0 =~ m!^.*/([^/]+)$!;
$PROGNAME = $1;

sub usage();

# Read cmdline opts:
Getopt::Long::Configure('bundling');

my $options = GetOptions(
    'V|version'            => \&print_version,
    'h|help'               => \&print_help,
    'v|verbose'            => \$debug,
    'b|database=s'         => \$database,
    'u|user=s'             => \$username,
    'p|passwd=s'           => \$password,
    's|select-statement=s' => \$opt_select,
    'n|newline'            => \$newline,
    'P|perfdata'           => \$perfdata,
    'w|warn=s'             => \$opt_warn,
    'c|crit=s'             => \$opt_crit
);
if ( not $options ) {
    usage();
    exit $ERRORS{'UNKNOWN'};
}

# DBI and DBD::DB2 Perl modules.  Importing these modules is delayed until now
# to allow the script's -h or --help option to spill out usage detail without
# requiring that DBD::DB2 be already installed.
#
# Note that GroundWork does not currently bundle the DBD::DB2 package, because
# of dependencies on the IBM Data Server Driver package (DS Driver), which IBM
# maintains for many different platforms and occasionally updates.  See these
# resources for more information on obtaining and installing those packages:
#
# http://www-01.ibm.com/support/docview.wss?rs=71&uid=swg21297335
# https://www.ibm.com/support/knowledgecenter/en/SSEPGG_11.1.0/com.ibm.swg.im.dbclient.perl.doc/doc/c0006687.html
# http://search.cpan.org/dist/DBD-DB2/DB2.pod
# http://search.cpan.org/dist/DBD-DB2/lib/Bundle/DBD/DB2.pm
# http://search.cpan.org/~ibmtordb2/DBD-DB2-1.85/
#
# To install DBD::DB2 for use with this script, you would need to use the
# /usr/local/groundwork/perl/bin/cpan copy of "cpan", not the system copy of
# "cpan", to get DBD::DB2 accessible by the GroundWork Perl distribution.
#
# An alternative approach to monitoring a DB2 database is the check_db2_health plugin:
# https://labs.consol.de/nagios/check_db2_health/
#
use DBI;
eval {
    require DBD::DB2;
};
if ($@) {
    print "ERROR:  The Perl DBD::DB2 package is not installed, so check_db2.pl cannot run.  See the plugin source code.\n";
    exit $ERRORS{'UNKNOWN'};
}
import DBD::DB2;

# This is the list of built-in error values.  These values are illustrative only
# and do not reflect what you would probably want to test against in production.
# low_warn:high_warn,low_crit:high_crit
my %error_vals = (
    AGENTS_STOLEN             => '10:20,5:25',
    SORT_OVERFLOWS            => '1000:1500,500:3000',
    HASH_JOIN_SMALL_OVERFLOWS => '10:20,5:35',
    HASH_JOIN_OVERFLOWS       => '10:20,5:35',
    LOCK_ESCALS               => '10:20,5:35',
    X_LOCK_ESCALS             => '10:20,5:35',
    DEADLOCKS                 => '5:10,0:25',
    FILES_CLOSED              => '10:20,5:35',
    AGENTS_WAITING            => '5:10,0:25',
    AGENTS_REGISTERED         => '40:50,30:70',
    POOLS                     => '80000000:100000000,70000000:120000000',
    PAGES                     => '4000000:5000000,3000000:6000000',
    TOTAL_CONS                => '40000:60000,30000:70000',
    WRITES                    => '400000000:500000000,300000000:600000000'
);

# List of SQL statements to get monitoring values.  Note the keys match the error vals above.

# NOTE:  These are intended as sample queries, primarily to stimulate your imagination.  You
# should figure out what statistics matter most to you, and write similar queries for them,
# passing such queries on the command line.

# We have fancified these sample queries and adapted the code that processes them to provide
# user-readable labels in the output, so the many numbers produced have some useful context.
# The processing of user specified queries is less sophisticated, and is designed only to
# handle queries of the form "SELECT column FROM ...", where the space-delimited "column" will
# be used as the name of the returned metric.  A sample user query that returns a numeric
# result would be "select julian_day (current date) from syspublic.dual", although this
# particular example is not terribly useful.

# FIX LATER:  Some of these table functions have been deprecated and are replaced by various
# views.  We should substitute more modern equivalents for these queries, though that might
# restrict us to more-recent versions of DB2.  See the DB2 doc for details.  In particular,
# SNAPSHOT_DBM(dbpartitionnum) does not return the partition number as one of its fields, so
# when using a wildcard for the partition number, you cannot tell which row is which!  The
# replacement SNAPDBM administrative view does have a DBPARTITIONNUM field for this purpose.

my %select_list = (

    AGENTS_STOLEN =>
      'SELECT concat(\'Agent ID \',AGENT_ID),\'Agents Stolen\',AGENTS_STOLEN FROM TABLE(SNAPSHOT_APPL(\'\',-1)) AS SNAPSHOT order by AGENT_ID',

    SORT_OVERFLOWS =>
'SELECT  CONCAT(RTRIM(DB_NAME),CONCAT(\' Partition \',CATALOG_PARTITION)),\'Sort Overflows\',SORT_OVERFLOWS FROM TABLE(SNAPSHOT_DATABASE(\'\',-1)) AS SNAPSHOT order by 1',

    HASH_JOIN_SMALL_OVERFLOWS =>
'SELECT concat(\'Agent ID \',AGENT_ID),\'Hash Join Small Overflows\',HASH_JOIN_SMALL_OVERFLOWS FROM TABLE(SNAPSHOT_APPL(\'\',-1)) AS SNAPSHOT order by AGENT_ID',

    HASH_JOIN_OVERFLOWS =>
'SELECT concat(\'Agent ID \',AGENT_ID),\'Hash Join Overflows\',HASH_JOIN_OVERFLOWS FROM TABLE(SNAPSHOT_APPL(\'\',-1)) AS SNAPSHOT order by AGENT_ID',

    LOCK_ESCALS =>
      'SELECT concat(\'Agent ID \',AGENT_ID),\'Lock Escalations\',LOCK_ESCALS FROM TABLE(SNAPSHOT_APPL(\'\',-1)) AS SNAPSHOT order by AGENT_ID',

    X_LOCK_ESCALS =>
'SELECT concat(\'Agent ID \',AGENT_ID),\'Exclusive Lock Escalations\',X_LOCK_ESCALS FROM TABLE(SNAPSHOT_APPL(\'\',-1)) AS SNAPSHOT order by AGENT_ID',

    DEADLOCKS =>
      'SELECT concat(\'Agent ID \',AGENT_ID),\'Deadlocks Detected\',DEADLOCKS FROM TABLE(SNAPSHOT_APPL(\'\',-1)) AS SNAPSHOT order by AGENT_ID',

    FILES_CLOSED => 'SELECT BP_NAME,\'Database Files Closed\',FILES_CLOSED FROM TABLE(SNAPSHOT_BP(\'\',-1)) AS SNAPSHOT order by 1',

    AGENTS_WAITING =>
'SELECT \'Current Partition\',\'Agents Waiting for a Token\',DECODE(AGENTS_WAITING_ON_TOKEN,(CAST(NULL AS BIGINT)),0,AGENTS_WAITING_ON_TOKEN) FROM TABLE(SNAPSHOT_DBM(-1)) AS SNAPSHOT order by 1',

    AGENTS_REGISTERED =>
      'SELECT \'Current Partition\',\'Agents Registered\',AGENTS_REGISTERED FROM TABLE(SNAPSHOT_DBM(-1)) AS SNAPSHOT order by 1',

    POOLS =>
'SELECT BP_NAME,\'Buffer Pool Data Physical Reads\',POOL_DATA_P_READS,\'Buffer Pool Index Physical Reads\',POOL_INDEX_P_READS,\'Buffer Pool Data Logical Reads\',POOL_DATA_L_READS,\'Buffer Pool Index Logical Reads\',POOL_INDEX_L_READS FROM TABLE(SNAPSHOT_BP(\'\',-1)) AS SNAPSHOT order by 1',

    PAGES =>
'SELECT TABLESPACE_NAME,\'Total Pages\',TOTAL_PAGES,\'Used Pages\',USED_PAGES FROM TABLE(SNAPSHOT_TBS_CFG(\'\',-1)) AS SNAPSHOT order by 1',

    TOTAL_CONS =>
'SELECT CONCAT(RTRIM(DB_NAME),CONCAT(\' Partition \',CATALOG_PARTITION)),\'Total Connects Since Database Activation\',TOTAL_CONS FROM TABLE(SNAPSHOT_DATABASE(\'\',-1)) AS SNAPSHOT order by 1',

    WRITES =>
'SELECT CONCAT(RTRIM(DB_NAME),CONCAT(\' Partition \',CATALOG_PARTITION)),\'Total Buffer Pool Physical Write Time\',POOL_WRITE_TIME,\'Buffer Pool Asynchronous Write Time\',POOL_ASYNC_WRITE_TIME FROM TABLE(SNAPSHOT_DATABASE(\'\',-1)) AS SNAPSHOT order by 1'

);

# Check args.  It's okay not to supply user/pass, because the environment
# (or local DB2 configuration files) may have these credentials.
if ( !$database ) {
    print "ERROR:  Database not specified\n";
    usage();
    exit $ERRORS{'UNKNOWN'};
}

my $errmsg = undef;
if ($opt_warn) {
    ( $low_warn, $high_warn ) = split( /:/, $opt_warn );
    if ( not defined $low_warn or not defined $high_warn or not looks_like_number($low_warn) or not looks_like_number($high_warn) ) {
	$errmsg = 'Warning low and high thresholds are not both numeric.';
    }
    elsif ( $low_warn > $high_warn ) {
	$errmsg = 'Warning low thresholds cannot be greater than high thresholds.';
    }
}
if ($opt_crit) {
    ( $low_crit, $high_crit ) = split( /:/, $opt_crit );
    if ( not defined $low_crit or not defined $high_crit or not looks_like_number($low_crit) or not looks_like_number($high_crit) ) {
	$errmsg = 'Critical low and high thresholds are not both numeric.';
    }
    elsif ( $low_crit > $high_crit ) {
	$errmsg = 'Critical low thresholds cannot be greater than high thresholds.';
    }
}
if ($errmsg) {
    print "ERROR:  $errmsg\n";
    usage();
    exit $ERRORS{'UNKNOWN'};
}

# Connect to the instance ...
$dbh = dbConnect( $database, $username, $password );

$state = check_select($opt_select) if ($privsok);

if ($debug) {
    $message = "$database: ";
    $message = $message . 'ok. ' . getDbVersion($dbh) unless ($state);
    print "$message\n";
}

exit $state;

#############################################################################

# We need to always call perish() instead of die(), to get the output
# posted to STDOUT instead of STDERR, and to provide a well-controlled
# exit code so Nagios will correctly understand it.
sub perish {
    my $exit_status = shift;
    print 'ERROR: ', @_;
    exit $exit_status;
}

sub usage () {
    print 'Usage:  $PROGNAME -v (verbose) -b <database> -u <user> -p <passwd>
	    [-s <select value>] [-w <low:high>] [-c <low:high>] -P (perfdata)
	$PROGNAME [-V|--version]
	$PROGNAME [-h|--help]
';
}

sub print_help {
    print_revision( $PROGNAME, $VERSION );

    # Normally, you will choose your own metric to probe, using the -s option.  You can run
    # without the -s option to spill out the results of a bunch of basic sample queries, but:
    # (*) The sample query thresholds have not been tuned for production use; they are present
    #     simply to provide examples of the types of queries you might want to run, and to
    #     provide a means to test portions of this script (e.g., database connectivity).
    # (*) The complete set of sample queries can produce a gargantuan amount of output.
    # (*) The sample queries are probably best just run interactively for testing, outside the
    #     Nagios context, using the -n option.
    print qq(
This plugin checks DB2 database health.  It requires the Perl DBD::DB2
package to be installed within the GroundWork Perl distribution, and
the IBM Data Server Driver package (DS Driver) to be installed as well.
See the plugin source code for more information on these packages.

Options:
 -v, --verbose
     More verbose output
 -b, --database=STRING
     db2 database name or alias
 -u, --user=STRING
     db2 user
 -p, --passwd=STRING
     db2 password
 -s, --select-statement=SQL_SELECT_STATEMENT
     A full SELECT statement of the form "SELECT column FROM ...", to
     retrieve a single numeric column from the database.  The complete
     statement will need to be quoted so it appears as a single argument
     on the commmand line, even though it will contain spaces.  All the
     rows returned will be checked against the warning (-w) and critical
     (-c) thresholds.  If you are using -P, the statement should be
     constructed to return just a single row, so its value can be
     unambiguously used as the performance metric value.
 -n, --newline
     Output newlines after values so the output is readable.  Also shows
     OK values.  (For test purposes; don't use this option with Nagios.)
 -P, --perfdata
     Output performance data as part of the plugin results.
 -w, --warn=WARNING_LOW:WARNING_HIGH
     WARNING(range): low:high range specification for warning thresholds.
     Any detected value outside this range constitutes a warning condition.
 -c, --crit=CRITICAL_LOW:CRITICAL_HIGH
     CRITICAL(range): low:high range specification for critical thresholds.
     Any detected value outside this range constitutes a critical condition.
\n);

    exit $ERRORS{'UNKNOWN'};
}

sub print_version {
    print_revision( $PROGNAME, $VERSION );
    exit $ERRORS{'OK'};
}

sub dbConnect {
    my $db   = shift;
    my $user = shift;
    my $pass = shift;

    # Make connection to the database.
    if ($debug) {
	my $u = defined($user) ? $user : '';
	my $p = defined($pass) ? $pass : '';
	print "Attempting connection to:  dbi:db2:$db $u $p\n";
    }
    my $source = "dbi:DB2:$db";
    if ($user) {
	$dbh = DBI->connect( $source, $user, $pass, { PrintError => 0, LongReadLen => 102400 } );
    }
    else {
	## Environment has access?  Try it ...
	$dbh = DBI->connect( $source, '', '', { PrintError => 0, LongReadLen => 102400 } );
    }
    if ( !defined($dbh) ) {
	print "db connect failed\n" if $debug;
	my $errstr = $DBI::errstr;
	chomp $errstr;
	print "CRITICAL - cannot connect to the \"$db\" database ($errstr)\n";
	$state = $ERRORS{'CRITICAL'};
    }
    ## check to be sure this user has "SELECT" privilege.
    elsif ( checkPriv('SELECT') < 1 ) {
	$message = 'user' . ( defined($user) ? " \"$user\"" : '' ) . ' needs "SELECT" privilege.';
	$state = $ERRORS{'UNKNOWN'};
    }
    else {
	$privsok = 1;
	$state   = $ERRORS{'OK'};
    }
    return ($dbh);
}

sub getDbVersion {
    my $dbh = shift;
    my $db2version;

    $sql = 'select * from SYSIBM.SYSVERSIONS';
    $sth = $dbh->prepare($sql) or perish( $ERRORS{'CRITICAL'}, 'DB version check preparation failed: ', $dbh->errstr );
    $sth->execute() or perish( $ERRORS{'CRITICAL'}, 'DB version check failed: ', $sth->errstr );
    ($db2version) = $sth->fetchrow_array();
    $sth->finish();

    return $db2version;
}

sub checkPriv {
    my ( $privilege, $yesno );
    $privilege = shift;

    $sql = "SELECT COUNT(*) FROM SYSIBM.SQLTABLEPRIVILEGES WHERE PRIVILEGE = '$privilege'";
    $sth = $dbh->prepare($sql) or perish( $ERRORS{'CRITICAL'}, 'Privilege check preparation failed: ', $dbh->errstr );
    $sth->execute() or perish( $ERRORS{'CRITICAL'}, 'Privilege check failed: ', $sth->errstr );
    $yesno = $sth->fetchrow_array();
    $sth->finish();

    return ($yesno);
}

my $perf;
my $ok;

sub check_select {
    my $svalue    = shift;
    my $retvalsum = 0;
    my ( $retval, @row, $result, $p_result, $status, $res, $str );

    $result = '';
    if ($svalue) {
	## Just run the one query requested.
	$sql = $svalue;
	print "$sql\n" if $debug;

	if ( not defined $low_crit or not defined $high_crit ) {
	    perish( $ERRORS{'UNKNOWN'}, "Critical low and high thresholds for your chosen metric are not defined.\n" );
	}
	if ( not defined $low_warn or not defined $high_warn ) {
	    perish( $ERRORS{'UNKNOWN'}, "Warning low and high thresholds for your chosen metric are not defined.\n" );
	}
	$sth = $dbh->prepare($sql) or perish( $ERRORS{'CRITICAL'}, 'DB probe check preparation failed: ', $dbh->errstr );
	$sth->execute() or perish( $ERRORS{'CRITICAL'}, 'DB probe check failed: ', $sth->errstr );

	while ( @row = $sth->fetchrow() ) {
	    print "@row\n" if $debug;
	    $result .= check_one_val( $sql, @row );
	}
	$sth->finish();
    }
    else {
	## Run the built-in list of queries from %select_list.
	while ( my ( $key, $value ) = each(%select_list) ) {
	    print "$value\n" if $debug;

	    $sth = $dbh->prepare($value) or perish( $ERRORS{'CRITICAL'}, 'List preparation failed: ', $dbh->errstr );
	    $sth->execute() or perish( $ERRORS{'CRITICAL'}, 'List select failed: ', $sth->errstr );

	    while ( @row = $sth->fetchrow() ) {
		print "@row\n" if $debug;
		$str = check_vals( $key, @row );
		$result .= $str if $str;
	    }
	    $sth->finish();
	}
    }

    if ( $result && $result =~ /Critical/i ) {
	print 'DB2 status CRITICAL - ';
	$status = $ERRORS{'CRITICAL'};
    }
    elsif ( $result && $result =~ /Warning/i ) {
	print 'DB2 status WARNING - ';
	$status = $ERRORS{'WARNING'};
    }
    else {
	print 'DB2 status OK - ';
	$status = $ERRORS{'OK'};
	if   ($newline) { $result = $ok }
	else            { $result = 'All indicators are within thresholds.' }
    }
    if ($newline) { print "\n" }
    print $result;

    if ($perfdata) { print "| $perf" }
    print "\n";

    return $status;
}

# Didn't get a single arg, so roll through the default list.
sub check_vals {
    my $key   = shift;
    my $label = shift;
    my @row   = @_;
    my ( $result, $val, $value, $crit, $warn );

    ( $warn,     $crit )      = split( /,/, $error_vals{$key} );
    ( $low_warn, $high_warn ) = split( /:/, $warn );
    ( $low_crit, $high_crit ) = split( /:/, $crit );

    if ( $low_warn > $high_warn ) {
	print "ERROR:  Warning low thresholds cannot be greater than high thresholds.\n";
	usage();
	exit $ERRORS{'UNKNOWN'};
    }
    if ( $low_crit > $high_crit ) {
	print "ERROR:  Critical low thresholds cannot be greater than high thresholds.\n";
	usage();
	exit $ERRORS{'UNKNOWN'};
    }

    my $item;
    $result = '';
    while ( ( $item, $val ) = splice @row, 0, 2 ) {
	$value = $val;    # capture last value for use outside the loop
	if ( $val > $high_crit ) {
	    $result .= "$key: $label: " if not defined $result;
	    $result .= "$val $item ($crit Critical High) ";
	}
	elsif ( $val < $low_crit ) {
	    $result .= "$key: $label: " if not defined $result;
	    $result .= "$val $item ($crit Critical Low) ";
	}
	elsif ( $val > $high_warn ) {
	    $result .= "$key: $label: " if not defined $result;
	    $result .= "$val $item ($warn Warning High) ";
	}
	elsif ( $val < $low_warn ) {
	    $result .= "$key: $label: " if not defined $result;
	    $result .= "$val $item ($warn Warning Low) ";
	}
	$ok .= "$key: $val (Ok) ";
    }

    if ( !$value ) { $value = '0' }
    $perf .= "$key=$value;$warn;$crit;; ";

    $result = $result . "\n" if $newline && $result;
    $ok     = $ok . "\n"     if $newline && $ok;

    return $result;
}

# Got an arg that is wanted so just do that one
sub check_one_val {
    my $statement = shift;
    my @row       = shift;
    my ( $result, $val, $value, $keyword, $column );

    # This construction presumes your SQL statement is of the form:
    #     select foo from ...
    # and extracts "foo" as the column name to be used in subsequent output.
    # FIX LATER:  Generalize this to allow for more complex naming of the
    # returned metric name.
    ( $keyword, $column ) = split( ' ', $statement );

    $result = '';
    foreach $val (@row) {
	$value = $val;    # capture last value for use outside the loop
	if ( $val > $high_crit ) {
	    $result .= "Critical High: $column: $val ($opt_crit) ";
	}
	elsif ( $val < $low_crit ) {
	    $result .= "Critical Low: $column: $val ($opt_crit) ";
	}
	elsif ( $val > $high_warn ) {
	    $result .= "Warning High: $column: $val ($opt_warn) ";
	}
	elsif ( $val < $low_warn ) {
	    $result .= "Warning Low: $column: $val ($opt_warn) ";
	}
	$ok .= "$column: $val (Ok) ";
    }

    if ( !$value ) { $value = '0' }

    # FIX LATER:  Should we be quoting the $column value?
    # FIX MINOR:  Ensure that the returned metric name always fits within
    # the 19-character RRD DS name and character set restrictions.
    $perf .= "$column=$value;$opt_warn;$opt_crit;; ";

    $result = $result . "\n" if $newline && $result;
    $ok     = $ok . "\n"     if $newline && $ok;
    return $result;
}
