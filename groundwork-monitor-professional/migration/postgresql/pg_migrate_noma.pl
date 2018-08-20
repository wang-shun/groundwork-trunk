#!/usr/local/groundwork/perl/bin/perl -w --
#
# pg_migrate_noma.pl
#
# Copyright 2017 GroundWork, Inc. ("GroundWork")
# All rights reserved.
#

# This script is responsible for all systemic changes to the "noma" database
# from release to release, be they schema or content, as well as changes to
# some files outside of the database.  It only handles changes starting from
# the GWMEE 7.1.0 release.

# IMPORTANT NOTES TO DEVELOPERS:
# (*) All actions taken by this script MUST BE IDEMPOTENT!  This script may be
#     run multiple times on a system, and it is important that each action
#     senses the current state of the database and filesystemm and only performs
#     the incremental transformations if they are actually needed.  That is
#     because the script may be run in several different contexts, and may be
#     run multiple times on the same database and filesystem, during patching,
#     during the same release, or on successive upgrades to later releases.
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
# (*) Currently, we perform certain operations using external scripting.  This
#     may make it rather difficult to roll back such changes if some later part
#     of a NoMa upgrade fails.  This is as yet an unresolved problem.

# TO DO:
# (*) Validate all exception handling in this script, to make sure it comports
#     with our setting of RaiseError.

##############################################################################
# SUMMARY OF VERSION CHANGES
##############################################################################

# GWMEE 7.1.0 => 7.1.1 (no changes are implemented by this script)
# GWMEE 7.1.1 => 7.2.0 (add missing "noma" database indexes;
#                      convert unique-id values in the "noma" database;
#                      extend and edit the NoMa.yaml options;
#                      tighten the NoMa.yaml file permissions;
#                      allow very long values in some "noma" database fields)
# GWMEE 7.2.0 => 7.2.1 (notes on anticipated future changes, documented
#                      here for safekeeping but not yet implemented)

# ----------------------------------------------------------------
# GWMEE 7.2.0 conversions
# ----------------------------------------------------------------
#
# * Add important indexes to certain noma tables.
# * Convert unique-ID values in the noma database to prepare it for operation
#   using the new notifier:generate_IDs option.
# * Add new options to the NoMa.yaml file.
# * Edit existing options in the NoMa.yaml file.
# * Restrict permissions on the NoMa.yaml file, because it contains sensitive data.
# * Convert database fields containing lists of hosts, services, hostgroups,
#   or servicegroups from type "character varying(255)" to type "text".

# ----------------------------------------------------------------
# GWMEE 7.2.1 conversions
# ----------------------------------------------------------------
#
# This section describes changes that might come in a future release.
#
# Given the limitations we have found in NoMa, it is possible that the action in
# some future release might be to attempt to transfer NoMa setup data to some
# other package that handles notifications.

##############################################################################
# Perl setup
##############################################################################

use strict;
use warnings;

use DBI;
use IO::Handle;
use YAML::Syck;

use constant FAILURE_STATUS => 0;
use constant SUCCESS_STATUS => 1;

##############################################################################
# Script parameters
##############################################################################

my $groundwork     = '/usr/local/groundwork';
my $noma_yaml_conf = "$groundwork/noma/etc/NoMa.yaml";

##############################################################################
# Global variables
##############################################################################

my $all_is_done  = 0;
my $noma_version = '2.0.3.2';

my $wait_status;
my $status = SUCCESS_STATUS;

my ( $dbtype, $dbhost, $dbport, $dbname, $dbuser, $dbpass );
my $dbh = undef;
my $sqlstmt;

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

sub rewrite_file_atomically {
    my $filepath    = shift;
    my $content_ref = shift;
    my $error_ref   = shift;

    # Set this to true to leave around debris for later forensic investigation.
    my $debug_failure = 1;

    # Get the metadata of the original file, which will be used later for the new file.
    my ( $dev, $ino, $mode, $nlink, $uid, $gid ) = stat $filepath;
    unless ( defined $mode ) {
	$$error_ref = "ERROR:  Could not get permissions of the file '$filepath':  $!\n";
	return FAILURE_STATUS;
    }

    # Kill any filetype info, and further restrict the permissions to disallow any pointless
    # set-id/sticky or execute permissions and any group-write or other-write permissions.
    $mode &= 0644;

    # Open a temporary file for writing.  We do this so as not to utterly destroy the
    # old copy if we get interrupted before we're done constructing the updated copy.
    my $temp_filepath = "$filepath.new.$$";
    unless ( open( FILE_OUT, '>', $temp_filepath ) ) {
	$$error_ref = "ERROR:  Could not open the file '$temp_filepath' for writing:  $!\n";
	return FAILURE_STATUS;
    }

    # Attempt to write the full content out to the temp file.  After this print(), there may
    # still be some data left in Perl's I/O buffers that is not yet flushed out to the file.
    # FIX LATER:  Perhaps use IO::File instead, if that confers some significant advantages.
    unless ( print FILE_OUT $$content_ref ) {
	$$error_ref = "ERROR:  Could not write to the file '$temp_filepath':  $!\n";
	unlink $temp_filepath unless $debug_failure;
	return FAILURE_STATUS;
    }

    # A close() should flush Perl's I/O buffers if there is data left there, so writing can
    # occur here, too, and the success of this operation must be checked as well.
    unless ( close FILE_OUT ) {
	$$error_ref = "ERROR:  Could not close the file '$temp_filepath':  $!\n";
	unlink $temp_filepath unless $debug_failure;
	return FAILURE_STATUS;
    }

    # Set the ownership of the new file to that of the old file.  This should either effectively
    # be a no-op, because we are running as the owner of the old file, or force the ownership to
    # match the old file, because we are running as root.  If this fails, then we know we cannot
    # put the updated file into place without a change of ownership, so we abort.
    unless ( chown $uid, $gid, $temp_filepath ) {
	$$error_ref = "ERROR:  Could not set the ownership of the file '$temp_filepath':  $!\n";
	unlink $temp_filepath unless $debug_failure;
	return FAILURE_STATUS;
    }

    # Set the permissions of the new file to those of the old file, perhaps sensibly restricted.
    unless ( chmod( $mode, $temp_filepath ) ) {
	$$error_ref = "ERROR:  Could not set the permissions of the file '$temp_filepath':  $!\n";
	unlink $temp_filepath unless $debug_failure;
	return FAILURE_STATUS;
    }

    # Perform an atomic rename of the new file.  By standard UNIX rename semantics, the end
    # result is that you either get the entire new file or the entire old file at the name of
    # the old file, depending on whether or not the rename succeeded.  But you never can get
    # any partial file as a result.  This provides essential safety.
    unless ( rename( $temp_filepath, $filepath ) ) {
	$$error_ref = "ERROR:  Could not rename the updated file '$temp_filepath':  $!\n";
	unlink $temp_filepath unless $debug_failure;
	return FAILURE_STATUS;
    }

    return SUCCESS_STATUS;
}

sub edit_file {
    my $filepath    = shift;
    my $edit_sub    = shift;
    my $edit_status = SUCCESS_STATUS;
    local $_;

    # Our strategy for creating and populating a sibling temporary file and then
    # atomically renaming it once it's ready won't work correctly if the original
    # file is a symlink instead of just a regular file.  If you need to edit such
    # a file, specify the full path to the file without any symlinks in the path.
    if ( -l $filepath ) {
	print "ERROR:  Cannot edit a symlink:  $filepath";
	$edit_status = FAILURE_STATUS;
    }
    ## FIX LATER:  Try :mmap mode, to see if that might simplify this i/o.
    elsif ( open FILE_IN, '<', $filepath ) {
	## Read in the existing file content.
	do {
	    local $/;    # slurp mode
	    $_ = <FILE_IN>;
	};
	close FILE_IN;
	## Apply externally-specified content edits.
	eval { $edit_sub->(); };
	if ($@) {
	    chomp $@;
	    print "ERROR:  Could not edit $filepath ($@).\n";
	    $edit_status = FAILURE_STATUS;
	}
	else {
	    ## Put the updated content in place of the original file.
	    my $error = undef;
	    $edit_status = rewrite_file_atomically( $filepath, \$_, \$error );
	    print $error if defined $error;
	}
    }
    else {
	print "ERROR:  Could not open $filepath ($!).";
	$edit_status = FAILURE_STATUS;
    }
    return $edit_status;
}

# The master copy of the convert_column_types() function is found in the sibling
# pg_migrate_monarch.pl script.  For more information on its usage, and if any
# changes need to be made to this function, that master copy should be consulted
# and kept current.
#
# This routine may be called from multiple places in this script.  If we need to
# extend it in any way, make sure that the specifications of the columns to convert
# are appropriately extended, for all calls.
#
sub convert_column_types {
    my $column_type_conversions = shift;

    foreach my $column_to_convert (@$column_type_conversions) {
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
}

##############################################################################
# Pick up database-access location and credentials
##############################################################################

# We prefer the NoMa.yaml file for access info, because being what NoMa itself uses, it is definitive.
if (1) {
    my $noma_config = LoadFile($noma_yaml_conf);
    $dbtype = $noma_config->{db}{type};
    if ( $dbtype eq 'postgresql' ) {
	$dbhost = $noma_config->{db}{postgresql}{host};
	## $dbport = $noma_config->{db}{postgresql}{port};  # Not actually in the file.
	$dbname = $noma_config->{db}{postgresql}{database};
	$dbuser = $noma_config->{db}{postgresql}{user};
	$dbpass = $noma_config->{db}{postgresql}{password};
	$dbport = 5432 if not defined $dbport;
    }
}
else {
    ## Filtering for the "noma" database is presumptive, which is part
    ## of why we prefer to use the NoMa.yaml file for this information.
    my @noma_pgpass_lines = qx(egrep '^[^#:][^:]*:[0-9]+:noma:' ~nagios/.pgpass);
    chomp @noma_pgpass_lines;
    if ( @noma_pgpass_lines == 1 ) {
	( $dbhost, $dbport, $dbname, $dbuser, $dbpass ) = split /:/, $noma_pgpass_lines[0];
    }
    else {
	## We either found no info for the "noma" database, or too much info.
	print "ERROR:  The \"~nagios/.pgpass\" file looks to be corrupted.\n";
    }
}

if ( !defined($dbhost) or !defined($dbname) or !defined($dbuser) or !defined($dbpass) ) {
    my $database_name = defined($dbname) ? $dbname : 'noma';
    print "ERROR:  Cannot read the \"$database_name\" database configuration.\n";
    exit(1);
}

##############################################################################
# Connect to the database
##############################################################################

print "\nNoMa Update\n";
print "=============================================================\n";

# We deliberately only allow connection to a PostgreSQL database, no longer supporting
# MySQL or SQLite3 as an alternative, because we are now free to use (and in some cases
# may require the use of) various PostgreSQL capabilities such as savepoints, in our
# scripting.  We are assuming that a conversion to use PostgreSQL has already happened
# before this script is run, using facilities outside of this conversion script.  See
# the sibling dump_sqlite_data_for_postgresql script for more informaion on that kind
# of NoMa migration.
my $dsn = "DBI:Pg:dbname=$dbname;host=$dbhost;port=$dbport";

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
    exit(1);
}

print "\nEncapsulating the changes in a transaction ...\n";
$dbh->do("set session transaction isolation level serializable");

##############################################################################
# Global initialization, to prepare for later stages
##############################################################################

print "\nInitializing ...\n";

##############################################################################
# GWMEE 7.2.0 conversions
##############################################################################

#-----------------------------------------------------------------------------
# Stop NoMa to make sure it is down before we perform database operations.
# We'll leave it up to other code to ensure that NoMa is restarted sometime
# after all changes are in place.
#-----------------------------------------------------------------------------

print "\nStopping NoMa ...\n";

# Before we try to stop NoMa, we first check whether we can even run the "service groundwork"
# command at this time.  If we're running in some context where we can do so, and we find that the
# "noma" service is currently running, stop it.  If the initial status command cannot even be run
# (say because we're running in the middle of an upgrade, where perhaps "service groundwork" is not
# even runnable), we can presumably just safely skip trying to stop Noma, since the "noma" service
# should already be down in that case.  The point here is, we will assume that any failure of the
# "service groundwork status noma" command to actually run is evidence that it should not be run in
# the current context, not that perhaps it should be runnable and that we have just experienced a
# failure that should block us from continuing with the rest of this migration script.
#
my $running_status = qx{service groundwork status noma};
if ( $running_status =~ /noma is already running/) {
    $wait_status = system('/etc/init.d/groundwork stop noma');
    if ($wait_status) {
	print "ERROR:  Cannot stop NoMa.\n";
	exit(1);
    }
}

#-----------------------------------------------------------------------------
# * Add important indexes to certain noma tables.  We do this before any of
#   the unique ID values are converted, to ensure that these indexes can be
#   used to make the queries in those conversions efficient.
#-----------------------------------------------------------------------------

# Run the add_indexes_to_noma_database.sql script.  Do this as the nagios user
# so we automatically use the "noma" database password.

print "\nAdding missing indexes to the \"noma\" database ...\n";

$wait_status = system(
  'su - nagios -c "/usr/local/groundwork/postgresql/bin/psql -U noma -d noma -f /usr/local/groundwork/core/migration/postgresql/add_indexes_to_noma_database.sql"'
);
if ($wait_status) {
    print "ERROR:  Cannot add indexes to the \"noma\" database.\n";
    exit(1);
}

#-----------------------------------------------------------------------------
# * Convert unique-ID values in the noma database to prepare it for operation
#   using the new notifier:generate_IDs option.
#-----------------------------------------------------------------------------

# Run the switch_noma_to_generated_ids.sql script.  Do this as the nagios user
# so we automatically use the "noma" database password.

print "\nConverting the \"noma\" database unique-ID values ...\n";

# We intentionally use i/o redirection here instead of the -f option, because
# the -f option will print a script name and line number for every line of RAISE
# output, which we are using for timing data.  That long extra prefix clutters up
# the useful information tremendously.
$wait_status = system(
  'su - nagios -c "/usr/local/groundwork/postgresql/bin/psql -U noma -d noma < /usr/local/groundwork/core/migration/postgresql/switch_noma_to_generated_ids.sql"'
);
if ($wait_status) {
    print "ERROR:  Cannot convert the \"noma\" database to use generated IDs.\n";
    exit(1);
}

#-----------------------------------------------------------------------------
# * Add new options to the NoMa.yaml file.
#   - Add the nap_time option before the sleep_time option.
#   - Add the generate_IDs option after the timezone option.
#-----------------------------------------------------------------------------

print "\nAdding new options to the NoMa.yaml file ...\n";

$status = edit_file(
    $noma_yaml_conf,
    sub {
	!m{^\s*nap_time:}m     && s{^([ \t]*)(sleep_time:[ \t]*\d+)}{${1}nap_time: 0.01\n${1}${2}}m;
	!m{^\s*generate_IDs:}m && s{^([ \t]*)(timezone:[ \t]*.*\n)}{${1}${2}${1}generate_IDs: 1\n}m;
    }
);
if ( $status != SUCCESS_STATUS ) {
    print "ERROR:  Cannot add new options to the $noma_yaml_conf file.\n";
    exit(1);
}

#-----------------------------------------------------------------------------
# * Edit existing options in the NoMa.yaml file.
#   - Reverse the bumping up of the sleep_time value from 60 (seconds),
#     which is now considered to be too long, back to 30 (seconds).
#   - Set the timezone option appropriately (though I'm not sure if it is
#     ever used in the GroundWork context).
#-----------------------------------------------------------------------------

print "\nEditing existing options in the NoMa.yaml file ...\n";

$status = edit_file(
    $noma_yaml_conf,
    sub {
	## The NoMa.yaml config-file setting of the notifier:timezone parameter is
	## not currently used anywhere in the NoMa code, so there's no particular
	## reason for us to get an empty value corrected to be the local timezone.
	# my $timezone = 'America/New_York';
	# s{^([ \t]*timezone:[ \t]*$)}{${1}$timezone}m;

	s{^([ \t]*sleep_time:[ \t]*)(1|60)[ \t]*$}{${1}30}m;
    }
);
if ( $status != SUCCESS_STATUS ) {
    print "ERROR:  Cannot edit existing options in the $noma_yaml_conf file.\n";
    exit(1);
}

#-----------------------------------------------------------------------------
# * GWMON-13006:  Restrict permissions on the NoMa.yaml file, because it
#   contains sensitive data.
#-----------------------------------------------------------------------------

print "\nRestricting access to the NoMa.yaml file ...\n";

# We want to know if this restriction fails, but it's not critical enough to
# abort the entire migration if that happens.
if ( chmod( 0600, $noma_yaml_conf ) != 1 ) {
    print "WARNING:  Could not restrict the permissions of the $noma_yaml_conf file ($!).\n";
}

#-----------------------------------------------------------------------------
# * Convert database fields containing lists of hosts, services, hostgroups,
#   or servicegroups from type "character varying(255)" to type "text".
#-----------------------------------------------------------------------------

# We purposely left this modification for last, because:
#
# (*) Some of the other modifications will not fall under our database-transaction
#     control for direct database modifications in this script, and we don't want
#     any related changes between other steps to be possibly interrupted in the
#     middle and leaving an inconsistent mismatch between database content and
#     config-file settings.
# (*) The action taken in this step is relatively optional, and will not be
#     critical for most customer sites.

# The code for this kind of column-conversion operation originates in
# the sibling pg_migrate_monarch.pl script.  See there for both general
# commentary and important details.

print "\nExtending columns in certain tables, if needed ...\n";

# There's nothing to convert if we simply give a varying field a larger maximum size.
my $extend_character_field = undef;

#                                                                            new type,        new
#     table               column                   old type, old max len     new max len    default  conversion
#     ------------------  -----------------------  ------------------------  -------------  -------  -----------------------
my @convert_to_text_column_types = (
    [ 'escalation_stati', 'hostgroups',            'character varying', 255, 'text', undef, undef,   $extend_character_field ],
    [ 'escalation_stati', 'servicegroups',         'character varying', 255, 'text', undef, undef,   $extend_character_field ],
    [ 'notifications',    'hosts_include',         'character varying', 255, 'text', undef, undef,   $extend_character_field ],
    [ 'notifications',    'hosts_exclude',         'character varying', 255, 'text', undef, undef,   $extend_character_field ],
    [ 'notifications',    'hostgroups_include',    'character varying', 255, 'text', undef, undef,   $extend_character_field ],
    [ 'notifications',    'hostgroups_exclude',    'character varying', 255, 'text', undef, undef,   $extend_character_field ],
    [ 'notifications',    'services_include',      'character varying', 255, 'text', undef, undef,   $extend_character_field ],
    [ 'notifications',    'services_exclude',      'character varying', 255, 'text', undef, undef,   $extend_character_field ],
    [ 'notifications',    'servicegroups_include', 'character varying', 255, 'text', undef, undef,   $extend_character_field ],
    [ 'notifications',    'servicegroups_exclude', 'character varying', 255, 'text', undef, undef,   $extend_character_field ],
    [ 'tmp_commands',     'hostgroups',            'character varying', 255, 'text', undef, undef,   $extend_character_field ],
    [ 'tmp_commands',     'servicegroups',         'character varying', 255, 'text', undef, undef,   $extend_character_field ]
);

convert_column_types( \@convert_to_text_column_types );

##############################################################################
# GWMEE 7.2.1 conversions
##############################################################################

# FIX LATER:  Fill in this section with all known expected changes for the
# GWMEE 7.2.1 release.

##############################################################################
# Committing Changes
##############################################################################

# In some future version of this script, we might insert or modify some row
# in the "information" table to tell the state of its conversion.  But for
# now, we just commit the changes made above, as best we can.

print "\nCommitting all changes ...\n";

# Commit all previous changes.  Note that some earlier actions may have performed
# implicit commit operations, so the fact that we are finalizing the most recent
# actions is not a definitive way to treat the entire operation of this script
# as a single transaction.  There is not much of anything we can do about those
# earlier commits; there is no good way to roll back automatically if some part
# of the operations that perform such implicit commits should fail.
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
	## will roll back to the state of the database before this script was run
	## (ignoring the extent to which other scripts that we have called in this
	## script have made their own changes to the database).
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
	    print "WARNING:  The NoMa database has probably been left in an inconsistent, unusable state.\n";
	}
	$dbh->disconnect();
    }
    if ( !$all_is_done ) {
	print "\n";
	print "====================================================================\n";
	print "    WARNING:  The NoMa migration did not fully complete!\n";
	print "====================================================================\n";
	print "\n";
	exit(1);
    }
}

print "\nUpdate of NoMa is complete.\n\n";

