#!/usr/local/groundwork/perl/bin/perl -w --
#
# MonArch - Groundwork Monitor Architect
# pg_migrate_dashboard.pl
#
############################################################################
# Release 4.5
# November 2016
############################################################################
#
# Copyright 2011-2016 GroundWork, Inc. ("GroundWork")
# All rights reserved.
#

# This script is responsible for all systemic changes to the "dashboard" database
# from release to release, be they schema or content.

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

# GWMEE 7.1.1:  simple schema extension

# ----------------------------------------------------------------
# GWMEE 7.1.1 conversions
# ----------------------------------------------------------------
#
# This upgrade implements only a few minor schema changes.
#
# * GWMON-12780:  In the fresh-install setup, the measurements.component field
#   has been modified to be a maximum of 255 characters instead of 100 characters,
#   to accommodate long data seen in the field.  This same change must be made
#   during an upgrade.

##############################################################################
# Perl setup
##############################################################################

use strict;

use DBI;

##############################################################################
# Script parameters
##############################################################################

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

if ( -e "/usr/local/groundwork/config/db.properties" ) {
    open( FILE, '<', '/usr/local/groundwork/config/db.properties' )
      or die "\nCannot open the db.properties file ($!); aborting!\n";
    while ( my $line = <FILE> ) {
	if ( $line =~ /^\s*insightreports\.dbhost\s*=\s*(\S+)/ )   { $dbhost = $1 }
	if ( $line =~ /^\s*insightreports\.database\s*=\s*(\S+)/ ) { $dbname = $1 }
	if ( $line =~ /^\s*insightreports\.username\s*=\s*(\S+)/ ) { $dbuser = $1 }
	if ( $line =~ /^\s*insightreports\.password\s*=\s*(\S+)/ ) { $dbpass = $1 }
    }
    close(FILE);
}

if ( !defined($dbhost) or !defined($dbname) or !defined($dbuser) or !defined($dbpass) ) {
    my $database_name = defined($dbname) ? $dbname : 'dashboard';
    print "ERROR:  Cannot read the \"$database_name\" database configuration.\n";
    exit (1);
}

##############################################################################
# Connect to the database
##############################################################################

print "\ndashboard database update\n";
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
# Schema changes for the GWMEE 7.1.0 => 7.1.1 transition
##############################################################################

#-----------------------------------------------------------------------------
# Extend the lengths of certain fields, to address database problems
# seen at customer sites.
#-----------------------------------------------------------------------------

# * GWMON-12780:  In the fresh-install setup, the measurements.component field
#   has been modified to be a maximum of 255 characters instead of 100 characters,
#   to accommodate long data seen in the field.  This same change must be made
#   during an upgrade.

print "\nExtending columns in certain tables, if needed ...\n";

# While the initial conversions we want to run here are all just extensions of the
# max length of character varying fields, we define this conversion as generally
# as we can currently imagine, so this table of column conversions can be extended
# in the future with other transformations.
#
# That said, we don't currently handle any changes to the NOT NULL modifier for a column,
# since we have not yet seen the need for such a change to any column.  That property is
# recorded in the PostgreSQL information_schema.columns table as the separate is_nullable
# field, and if needed, would be handled in the following code by an additional optional
# "ALTER [ COLUMN ] column_name { SET | DROP } NOT NULL" clause in the ALTER TABLE command.

# There's nothing to convert if we simply give a varying field a larger maximum size.
my $extend_character_field = undef;

#                                                                                      new
#     table           column       old type, old max len     new type, new max len     default  conversion
#     --------------  -----------  ------------------------  ------------------------  -------  -----------------------
my @convert_to_711_column_types = (
    [ 'measurements', 'component', 'character varying', 100, 'character varying', 255, "''",    $extend_character_field ]
);

foreach my $column_to_convert (@convert_to_711_column_types) {
    my $table       = $column_to_convert->[0];
    my $column      = $column_to_convert->[1];
    my $old_type    = $column_to_convert->[2];
    my $old_max_len = $column_to_convert->[3];
    my $new_type    = $column_to_convert->[4];
    my $new_max_len = $column_to_convert->[5];
    my $new_default = $column_to_convert->[6];
    my $conversion  = $column_to_convert->[7];

    $sqlstmt = "
	select data_type, character_maximum_length, column_default
	from information_schema.columns
	where table_name = '$table'
	and column_name = '$column'
    ";
    my ( $data_type, $character_maximum_length, $column_default ) = $dbh->selectrow_array($sqlstmt);

    if ( $data_type eq $old_type && ( !defined($character_maximum_length) || $character_maximum_length == $old_max_len ) ) {
	## Note that because we will drop any old default value here, if we want to just
	## preserve an existing default value, we'll need to specify it in the table above.
	if ( defined $column_default ) {
	    $sqlstmt = "alter table \"$table\" alter column \"$column\" drop default";
	    $dbh->do($sqlstmt);
	}

	$sqlstmt = "alter table \"$table\" alter column \"$column\" type $new_type" . ( defined($new_max_len) ? "($new_max_len)" : '' );
	if ( defined $conversion ) {
	    $conversion =~ s/{COLUMN}/"$column"/g;
	    $sqlstmt .= " $conversion";
	}
	$dbh->do($sqlstmt);
    }

    if ( defined $new_default ) {
	$sqlstmt = "alter table \"$table\" alter column \"$column\" set default $new_default";
	$dbh->do($sqlstmt);
    }
}

##############################################################################
# Committing Changes
##############################################################################

print "\nCommitting all changes ...\n";

# Commit all previous changes.  Note that some earlier commands may have performed
# implicit commit operations, which is why the very first change we might make above
# could be to modify a marker at the start of the script to something that would
# show that we were only partially done migrating the database schema and content.
# There is not much of anything we can do about those implicit commits; there is no
# good way to roll back automatically if some part of the operations that perform
# such implicit commits should fail.  If we find an "incompletely converted" marker
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
	## commit) it may leave the database in an incompletely converted state.  A
	## future version of this script might use a marker in a temporary table to
	## record state in the same way that we use the monarch_version value in the
	## pg_migrate_monarch.pl script to tell whether the conversion is complete,
	## so we can report here whether there is any confusion as to whether the
	## database is in a usable state.
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
	    print "WARNING:  The dashboard database has probably been left in an inconsistent, unusable state.\n";
	}
	$dbh->disconnect();
    }
    if (!$all_is_done) {
	print "\n";
	print "====================================================================\n";
	print "    WARNING:  dashboard database migration did not fully complete!\n";
	print "====================================================================\n";
	print "\n";
	exit (1);
    }
}

print "\nUpdate of the dashboard database is complete.\n\n";

