#!/usr/local/groundwork/perl/bin/perl -w --
#
# pg_migrate_postgresql.pl
#
############################################################################
# GWMEE Release 7.2.0
# September 2017
############################################################################
#
# Copyright 2017 GroundWork, Inc. ("GroundWork")
# All rights reserved.
#

# This script is responsible for all systemic changes to PostgreSQL itself
# from release to release, be they schema or content, that are not handled
# by the standard pg_dumpall/pg_restore programs or pg_upgrade or equivalent
# processing, or by our migration scripts for individual databases.  Only
# changes starting with an upgrade to the GWMEE 7.2.0 release are handled.

# Note:  pg_dumpall / pg_restore processing may be preferred over pg_upgrade.
# See the upgrade conversion details below.

# IMPORTANT NOTES TO DEVELOPERS:
# (*) All actions taken by this script MUST BE IDEMPOTENT!  This script may be
#     run multiple times on a database, and it is important that each action
#     senses the current state of the database and only performs the incremental
#     transformations if they are actually needed.  That is because the script
#     may be run in several different contexts, and may be run multiple times on
#     the same database, either during the same release or on successive upgrades
#     to later releases.
# (*) All error handling in this script must follow consistent conventions for
#     how potential errors are trapped, sensed, and handled.  To begin with,
#     whenever the code performs some database action, it must be aware of the
#     possibility of error.  Also, simply checking returned status is not a
#     sufficient paradigm, because in fact we use the RaiseError capability of
#     the DBI package, so exceptions must be explicitly planned for and caught,
#     if any special action should be taken in response to them.  In general,
#     given that RaiseError is in effect, we simply allow it to function, print
#     error messages, and cause the script to die.  The END block at the end of
#     the script will be executed after that, to attempt to roll back changes
#     made so far and to inform the user that the run did not execute to full
#     completion.
# (*) We attempt to carry out all operations in this script within a database
#     transaction that can be rolled back if the migration fails for any reason.
#     However, there are some types of database alterations, particularly schema
#     changes, that the database engine might not allow inside a transaction.
#     Executing such an alteration will generally end the previous explicitly
#     started transaction, using an implicit commit, before the non-transaction
#     action occurs.  This will obviously interfere with our ability to roll
#     back to the original state of the database should some unrelated error
#     occur later in the script's processing.  To avoid confusing ourselves as
#     to what is and is not contained within a controlled transaction, if at all
#     possible, any non-transactional (implicit-commit) operations should be
#     moved to the front of the script, before all the transactional operations
#     begin.  This will allow the best chance of our maintaining the ability to
#     have rollbacks make sense.  This issue must be re-examined for every new
#     release, according to the changes planned for that release.

# TO DO:
# (*) Validate all exception handling in this script, to make sure it
#     comports with our setting of RaiseError.
# (*) Figure out what kinds of table locking and other transaction and
#     serialization control we ought to be implementing here, and do
#     something about it.

##############################################################################
# SUMMARY OF VERSION CHANGES
##############################################################################

# GWMEE 7.2.0 => Implement changes needed to upgrade from Pg 9.4.1 to Pg 9.6.5.

# ----------------------------------------------------------------
# GWMEE 7.2.0 conversions (Pg 9.4.1 to Pg 9.6.5)
# ----------------------------------------------------------------
#
# This upgrade requires that certain fixes be applied to complete the work.  Here we call out the
# significant items listed in the release notes for all the relevant intermediate releases, and
# how we will deal with them in the GroundWork context.  In many cases the upstream documentation
# is not clear on exactly how one would go about detecting whether or not one needs a certain fix,
# so we will use as broad a determination as possible in deciding how to proceed.
#
# * Conversion to Pg 9.5.0:
#
# * Conversion to Pg 9.6.0:
#
#   (*) Update extension functions to be marked parallel-safe where appropriate (Andreas Karlsson)
#
#       Many of the standard extensions have been updated to allow their functions to be executed
#       within parallel query worker processes. These changes will not take effect in databases
#       pg_upgrade'd from prior versions unless you apply ALTER EXTENSION UPDATE to each such
#       extension (in each database of a cluster).
#
#       (We believe we will not be affected by this, because we are using pg_dumpall / pg_restore
#       instead of pg_upgrade during our upgrade processing.)
#
# * Conversion to Pg 9.6.1:
#
#   (*) https://wiki.postgresql.org/wiki/Free_Space_Map_Problems
#
#       (This is probably probably cleaned up for us automatically by our practice of running
#       initdb after the existing database content has been saved, to start fresh.)
#
#   (*) https://wiki.postgresql.org/wiki/Visibility_Map_Problems
#
#       (This is probably probably cleaned up for us automatically by our practice of running
#       initdb after the existing database content has been saved, to start fresh.)
#
# * Conversion to Pg 9.6.2:
#
#   (*) Fix a race condition that could cause indexes built with CREATE INDEX CONCURRENTLY to be
#       corrupt (Pavan Deolasee, Tom Lane)
#
#       If CREATE INDEX CONCURRENTLY was used to build an index that depends on a column not
#       previously indexed, then rows updated by transactions that ran concurrently with the
#       CREATE INDEX command could have received incorrect index entries. If you suspect this
#       may have happened, the most reliable solution is to rebuild affected indexes after
#       installing this update.
#
#       (We believe we will not be affected by this, because we are using pg_dumpall / pg_restore
#       during our upgrade processing, and that should rebuild the indexes from scratch.)
#
# * Conversion to Pg 9.6.3:
#
#   (*) There are no special steps to take that are not already covered for other releases.
#
# * Conversion to Pg 9.6.4:
#
#   (*) Further restrict visibility of pg_user_mappings.umoptions, to protect passwords stored
#       as user mapping options (Noah Misch)
#
#       See:  https://www.postgresql.org/about/news/1772/
#             https://www.postgresql.org/docs/9.6/static/release-9-6-4.html
#
#       (We are evidently not affected by this, because it is handled already for us during
#       an upgrade.  The BitRock installer runs /usr/local/groundwork/postgresql/bin/initdb
#       after capturing the old data but before doing much of anything else with the database,
#       and that takes care of the pg_user_mappings view definition in all databases.)
#
# * Conversion to Pg 9.6.5:
#
#   (*) Show foreign tables in information_schema.table_privileges view (Peter Eisentraut)
#
#       See:  https://www.postgresql.org/docs/9.6/static/release-9-6-5.html
#
#       (We are evidently not affected by this, because it is handled already for us during
#       an upgrade.  The BitRock installer runs /usr/local/groundwork/postgresql/bin/initdb
#       after capturing the old data but before doing much of anything else with the database,
#       and that takes care of the information_schema.table_privileges view definition in all
#       databases.)

##############################################################################
# Perl setup
##############################################################################

use strict;

use DBI;

use MonarchStorProc;

use TypedConfig;

##############################################################################
# Script parameters
##############################################################################

my $auto_registration_config_file = '/usr/local/groundwork/config/register_agent.properties';

my $debug_config = 0;    # if set, spill out certain data about config-file processing to STDOUT

##############################################################################
# Global variables
##############################################################################

my $all_is_done = 0;

my ( $dbhost, $dbname, $dbuser, $dbpass );
my $dbh = undef;
my $sth = undef;
my $sqlstmt;
my $outcome;

##############################################################################
# Perl context initialization
##############################################################################

# Autoflush the standard output on every single write, to avoid problems
# with block i/o and badly interleaved output lines on STDOUT and STDERR.
# This we do by having STDOUT use the same buffering discipline as STDERR,
# namely to flush every line as soon as it is produced.  This is certainly
# a less-efficient use of system resources, but we don't expect this program
# to write much to the STDOUT stream anyway, and this program will not be
# run very often.
STDOUT->autoflush(1);

##############################################################################
# Supporting subroutines
##############################################################################

# If a row insertion here fails because that row already exists, that's okay.
# But if it fails for some other reason, that's not okay.

sub idempotent_insert {
    my $table     = shift;
    my $row_label = shift;
    my $values    = shift;

    eval {
	$dbh->do( "SAVEPOINT before_insert" );
	$dbh->do( "INSERT INTO $table VALUES $values" );
	$dbh->do( "RELEASE SAVEPOINT before_insert" );
    };
    if ($@) {
	## To check:
	## if (not a duplicate row)
	## we look for:  "duplicate key value violates unique constraint" or equivalent
	## (I'm not sure how many different types of similar messages might exist).
	if ( $@ !~ /duplicate key value/i ) {
	    die "ERROR:  insert of $row_label into $table failed:\n    $@\n";
	}
	else {
	    ## This print is here just for initial debugging.  In production use, we don't want
	    ## to emit this message, because the stated condition is not considered a failure.
	    #  print "WARNING:  insert of $row_label into $table failed:\n    $@\n";

	    # Under PostgreSQL (at least), this condition (duplicate key value found) normally
	    # aborts the entire current transaction, meaning that all further commands until the
	    # end of the transaction block will be ignored.  That is obviously not the desired
	    # outcome.  So we either need some way to run a nested transaction, or some way to
	    # disable the usual aborting of the overall transaction.  An explicit savepoint around
	    # the insertion above is sufficient for our purposes.  Otherwise, we would need to
	    # implement some kind of subtransaction (which in effect, the savepoint is).  Or
	    # we would need to use some kind of trigger to check the insertion before actually
	    # executing it; but the difficulty there is that the trigger code would need to find
	    # out all the constraints on a given insertion, since we would not want to hardcode
	    # such associations.  Fortunately, savepoint management here is trivial.
	    $dbh->do( "ROLLBACK TO SAVEPOINT before_insert" );
	}
    }
}

##############################################################################
# Pick up database-access location and credentials
##############################################################################

# FIX MAJOR:  Credentials for a database connection will probably need to change,
# inasmuch as we're likely to be making system-level changes that will require
# PostgreSQL super-user privileges.

if ( -e "/usr/local/groundwork/config/db.properties" ) {
    open( FILE, '<', '/usr/local/groundwork/config/db.properties' )
      or die "\nCannot open the db.properties file ($!); aborting!\n";
    while ( my $line = <FILE> ) {
	if ( $line =~ /^\s*monarch\.dbhost\s*=\s*(\S+)/ )   { $dbhost = $1 }
	if ( $line =~ /^\s*monarch\.database\s*=\s*(\S+)/ ) { $dbname = $1 }
	if ( $line =~ /^\s*monarch\.username\s*=\s*(\S+)/ ) { $dbuser = $1 }
	if ( $line =~ /^\s*monarch\.password\s*=\s*(\S+)/ ) { $dbpass = $1 }
    }
    close(FILE);
}

if ( !defined($dbhost) or !defined($dbname) or !defined($dbuser) or !defined($dbpass) ) {
    my $database_name = defined($dbname) ? $dbname : 'monarch';
    print "ERROR:  Cannot read the \"$database_name\" database configuration.\n";
    exit (1);
}

##############################################################################
# Connect to the database
##############################################################################

print "\nPostgreSQL Update\n";
print "=============================================================\n";

# We deliberately only allow connection to a PostgreSQL database, no longer supporting
# MySQL as an alternative, because we are now free to use (and in some cases require
# the use of) various PostgreSQL capabilities such as savepoints, in our scripting.
my $dsn = "DBI:Pg:dbname=$dbname;host=$dbhost";

# We turn AutoCommit off because we want to make changes roll back automatically as much as
# possible if we don't get successfully through the entire script.  This is not perfect (i.e.,
# we don't necessarily have all the changes made in a single huge transaction) because some of
# the transformations may implicitly commit previous changes, and there is nothing we can do
# about that.  Still, we do the best we can.
#
# We turn PrintError off because RaiseError is on and we don't want duplicate messages printed.

print "\nConnecting to the $dbname database with user $dbuser ...\n";
eval { $dbh = DBI->connect_cached( $dsn, $dbuser, $dbpass, { 'AutoCommit' => 0, 'RaiseError' => 1, 'PrintError' => 0 } ) };
if ($@) {
    chomp $@;
    print "ERROR:  database connect failed ($@)\n";
    exit (1);
}

print "\nEncapsulating the changes in a transaction ...\n";
$dbh->do("set session transaction isolation level serializable");

##############################################################################
# Prepare for changes, in a manner that we can use to tell if the subsequent
# processing got aborted before it was finished.
##############################################################################


##############################################################################
# Global initialization, to prepare for later stages
##############################################################################

print "\nInitializing ...\n";

##############################################################################
# GWMEE 7.2.0 conversions
##############################################################################


##############################################################################
# Committing Changes
##############################################################################

print "\nCommitting all changes ...\n";

# FIX MINOR:  Make this true.
#
# Commit all previous changes.  Note that some earlier commands may have performed
# implicit commit operations, which is why the very first change we made above was
# to flag the state of the conversions at the start of the script to something that
# would show that we were only partially done migrating the database schema and content.
# There is not much of anything we can do about those implicit commits; there is no
# good way to roll back automatically if some part of the operations that perform
# such implicit commits should fail.  If we find the conversion flag is still set
# after running this script, we know the migration is not completely done.
$dbh->commit();

# Disconnect from the database, and undefine our database handle, so we don't get
# our "Rolling back ..." message from the trailing END block if we really did just
# successfully run the commit.
do {
    ## Localize and turn off RaiseError for this block, because once we have
    ## successfully committed all changes just above, we really don't care if
    ## we somehow get an error during the disconnect operation.
    local $dbh->{RaiseError};

    $dbh->disconnect();
    $dbh = undef;
};

##############################################################################
# Done.
##############################################################################

$all_is_done = 1;

END {
    if ($dbh) {
	## Roll back any uncommitted transaction.  If the $dbh->commit() above did
	## not execute (which should generally be the only way we get here), this
	## will either roll back to the state of the database before this script was
	## run, or (if our enclosing transaction was broken by some earlier implicit
	## commit) it should leave PostgreSQL in a state where we can later see that
	## the full migration did not complete, so there is no confusion as to whether
	## the database is in a usable state.
	##
	## FIX MINOR:  What flag will we use to tell whether the database conversion is complete?
	print "\nRolling back changes ...\n";
	eval {
	    $dbh->rollback();
	};
	if ($@) {
	    ## For some reason, $dbh->errstr here returns a value from far earlier in the script,
	    ## not reflecting what just failed within this eval{};.  So we need to look instead
	    # at $@ instead for clues as to what just happened.
	    my $errstr = $@;
	    print "\nERROR:  rollback failed", (defined($errstr) ? (":\n" . $errstr) : '; no error detail is available.'), "\n";
	    print "WARNING:  PostgreSQL has probably been left in an inconsistent, unusable state.\n";
	}
	$dbh->disconnect();
    }
    if (!$all_is_done) {
	print "\n";
	print "====================================================================\n";
	print "    WARNING:  PostgreSQL migration did not fully complete!\n";
	print "====================================================================\n";
	print "\n";
	exit (1);
    }
}

print "\nUpdate of PostgreSQL is complete.\n\n";

