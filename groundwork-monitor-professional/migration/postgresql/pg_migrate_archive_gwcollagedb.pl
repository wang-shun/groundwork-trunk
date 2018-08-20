#!/usr/local/groundwork/perl/bin/perl -w --
#
# pg_migrate_archive_gwcollagedb.pl
#
############################################################################
# GWMEE Release 7.2.1
# May 2018
############################################################################
#
# Copyright 2013-2018 GroundWork, Inc. ("GroundWork")
# All rights reserved.
#

# This script is responsible for all systemic changes to the "archive_gwcollagedb"
# database from release to release, by they schema or content.  Since the content
# of this database is simply supposed to mirror the content of the "gwcollagedb"
# database (by automated copying, though with some organized schema changes for
# some tables), it is unlikely that any content changes will need to be made here.

# IMPORTANT NOTES TO DEVELOPERS:
#
# (*) All actions taken by this script MUST BE IDEMPOTENT!  This script may be
#     run multiple times on a database, and it is important that each action
#     senses the current state of the database and only performs the incremental
#     transformations if they are actually needed.  That is because the script
#     may be run multiple times over the lifetime of a single archive_gwcollagedb
#     database, generally but not necessarily on successive upgrades to later
#     releases.
#
# (*) All error handling in this script must follow consistent conventions for
#     how potential errors are trapped, sensed, and handled.  To begin with,
#     whenever the code performs some database action, it must be aware of the
#     possibility of error.  Since this script is largely for schema changes,
#     and PostgreSQL largely disallows having schema changes be embedded as
#     part of a transaction, we cannot simply try to fold all the changes in
#     this script into a single large transaction and roll back at the end if
#     anything failed in the middle.  This issue is not yet completely solved.
#
# (*) Executing a schema alteration will generally end the previous explicitly
#     started transaction, using an implicit commit, before the non-transaction
#     action occurs.  To avoid confusing ourselves as to what is and is not
#     contained within a controlled transaction, if at all possible, any
#     non-transactional (implicit-commit) operations should be moved to the
#     front of the script, before all the transactional operations begin.
#     This will allow the best chance of our maintaining the ability to have
#     rollbacks make sense.  This issue must be re-examined for every new
#     release, according to the changes planned for that release.
#
# (*) The archive_gwcollagedb database schema IS NOT an exact mirror of the
#     schema of the gwcollagedb database!!  In some cases, there might be
#     extra indexes or constraints on tables.  Or there may be more-relaxed
#     constraints, such as not insisting on case-insensitive uniqueness of
#     hostnames.  And in many cases, archive_gwcollagedb tables are extended
#     to include startvalidtime and endvalidtime fields.  See the full
#     core/databases/postgresql/Archive_GWCollageDB_extensions.sql script for
#     details of schema differences.  Both that script and this script must be
#     modified in tandem.  Note in particular that if a gwcollagedb table has
#     a new column added to the end of the table, most often you CANNOT simply
#     do the same thing in the archive_gwcollagedb.  If the archive_gwcollagedb
#     table contains startvalidtime and endvalidtime fields, those must be kept
#     as the last two columns in the table, while retaining their existing
#     content.  PostgreSQL does not permit addition of new columns other than at
#     the end of a table definition, so extra work is required in this script
#     to modify the table structure while at the same time preserving the final
#     position and contents of those two existing fields.

# FIX LATER:  Why does the BitRock installer log on a fresh 7.1.1 install specify these lines,
# which are exact duplicates of each other?  Why are such files not unpacked just once?  See
# BitRock case #45423, under which the mystery will ultimately be resolved.
#
#     Unpacking /usr/local/groundwork/core/migration/postgresql/pg_migrate_archive_gwcollagedb.pl
#     Unpacking /usr/local/groundwork/core/migration/postgresql/pg_migrate_archive_gwcollagedb.pl
#
#     Unpacking /usr/local/groundwork/core/databases/postgresql/Archive_GWCollageDB_extensions.sql
#     Unpacking /usr/local/groundwork/core/databases/postgresql/Archive_GWCollageDB_extensions.sql

# FIX LATER:  The failure of any item in the related Archive_GWCollageDB_extensions.sql
# script ought to cause that script to exit with a non-zero exit code.  To be more
# specific, that script ought to be called with the psql "--variable=ON_ERROR_STOP="
# option, or have an equivalent psql directive (\set ON_ERROR_STOP) embedded in it.

##############################################################################
# OVERVIEW OF DATABASE STRUCTURE
##############################################################################

# A fresh install makes and populates the archive_gwcollagedb database this way,
# with shell variables used here to shorten the presentation:
#
#      psql=/usr/local/groundwork/postgresql/bin/psql
# scriptdir=/usr/local/groundwork/core/databases/postgresql
# $psql -U postgres -d postgres            -f $scriptdir/create-fresh-archive-databases.sql
# $psql -U postgres -d archive_gwcollagedb -f $scriptdir/GWCollageDB.sql
# $psql -U postgres -d archive_gwcollagedb -f $scriptdir/postgres-xtra-functions.sql
# $psql -U postgres -d archive_gwcollagedb -f $scriptdir/Archive_GWCollageDB_extensions.sql
# $psql -U postgres -d archive_gwcollagedb -f $scriptdir/GWCollage-Version.sql
#
# This means that a fresh-install archive_gwcollagedb database exactly mirrors every
# individual schema element of the gwcollagedb database, except for specific extensions
# and adjustments having to do with adding startvalidtime and endvalidtime fields to
# a number of the tables, and the relaxation of certain constraints that appear in
# the gwcollagedb database (to better support object lifetimes and legacy data).  In
# particular, the associated sequences, indexes, and constraints are all present in
# either their original forms or modified as needed to accommodate the extended fields in
# those tables.  A small amount of the initial gwcollagedb database content is natively
# present as well in the archive_gwcollagedb database.  Anything else only ends up in the
# archive_gwcollagedb database because it is copied there during daily archiving runs.
# Not all tables are archived, so some remain permanently empty in the archive_gwcollagedb
# database.
#
# Note that as of GWMEE 7.2.0, after GWMON-13224, the $scriptdir/postgres-xtra-functions.sql
# additions are being applied to the archive_gwcollagedb, to match the application of these
# functions as specified in the set-up-archive-database.sh script.  This happens both during
# a fresh install and during an upgrade, so there is no need to apply those functions here
# to an archive_gwcollagedb database which does not already have the extra functions.

# Changes to the archive_gwcollagedb database in each subsequent (upgraded-to)
# GWMEE release are supposed to mirror the precise structure of changes to the
# gwcollagedb database in that same release, except for some special alterations for
# the archive_gwcollagedb database to accommodate the startvalidtime and endvalidtime
# fields where present, and the occasional relaxation of certain constraints.  This is
# supposed to happen for all schema changes to tables and other objects in the gwcollage
# database, even if they are not presently configured to be part of the daily archiving.
# That way, if we ever do decide to archive some other table, it should already have all
# the required structure in place (if it does not also require the addition of the extra
# startvalidtime and endvalidtime fields).  On the other hand, content changes to the
# gwcollagedb database need to be considered individually.  If they might affect some
# existing row in the archive_gwcollagedb database, we need to understand the row type of
# the affected table (timed_association, timed_object, untimed_detail, or untimed_object;
# for explanation, see the comments in the log-archive-receive.conf configuration file).
# And comments should appear here in the migration script as to why this script makes the
# choices that it does, whether or not to mirror specific content changes made to the
# gwcollagedb database tables during an upgrade.

##############################################################################
# SUMMARY OF VERSION CHANGES
##############################################################################

# GWMEE 6.7.0 => GWMEE 7.0.1	a few schema changes for this release
# GWMEE 7.0.0 => GWMEE 7.0.1	no changes for this release
# GWMEE 7.0.1 => GWMEE 7.0.2	no changes for this release
# GWMEE 7.0.2 => GWMEE 7.1.0	a long list of schema changes for this release;
#				see the detailed notes below
# GWMEE 7.1.0 => GWMEE 7.1.1	no schema changes for this release
# GWMEE 7.1.1 => GWMEE 7.2.0	schema changes for this release (adding timezones
#				to timestamp fields); a few data changes
# GWMEE 7.2.0 => GWMEE 7.2.1	schema changes for this release (adding a table
#				and associated objects)

# Note that there is some special consideration for what really went on in the 7.1.0
# release, as opposed to what you would have expected.  Unfortunately, the 7.1.0 release
# declared itself to be using "CurrentSchemaVersion = 7.1.1" in the schemainfo table.
# This confuses matters because the database version number is close to but does not
# match the GWMEE release number.  The pg_migrate_gwcollagedb.sql script in the 7.1.0
# release went on past the point where it did all the things that were supposedly to be
# done for the 7.1.0 release, and also executed several actions that were supposedly not
# to be done until the 7.1.1 release.  Why this got so sloppy is unknown.  For the 7.1.1
# release, we have rejiggered the pg_migrate_gwcollagedb.sql script so all of the schema
# and content changes that were actually performed in the 7.1.0 release are done in the
# section assigned to the upgrade to the 7.1.0 release, to clean things up.  In point
# of fact, no customer systems actually declare themselves with "CurrentSchemaVersion
# = 7.1.0", because that was never a valid combination for an actual installed release
# (other than perhaps some early 7.1.0 development builds).  So the rejiggering of the
# script to clean it up should have no adverse impact on any customer upgrades.  It does
# clean things up so we can better understand what actual changes are being made for the
# 7.1.1 release (there is at least a content fixup for the 7.1.1 release), and to document
# them as such.  The SUMMARY OF VERSION CHANGES above reflects our rejiggered scripting,
# which is the view of the world that we need to have here to produce a corresponding
# archive_gwcollagedb database for both of the 7.1.0 and 7.1.1 releases (even though both
# of them claim to be 7.1.1 in their internal database CurrentSchemaVersion value).

# ----------------------------------------------------------------
# Conversion to GWMEE 7.0.1
# ----------------------------------------------------------------

# (*) The pg_migrate_gwcollagedb.sql script contains these lines for the
#     GWMEE 6.7-to-7.0.X transition:
#
#         ALTER TABLE hostgroup     ADD COLUMN agentid character varying(128);
#         ALTER TABLE host          ADD COLUMN agentid character varying(128);
#         ALTER TABLE servicestatus ADD COLUMN agentid character varying(128);
#
#     We need to mirror equivalents of those lines here, for idempotent changes
#     to the corresponding tables in the archive_gwcollagedb database.  However,
#     because PostgreSQL does not allow inserting a column into the middle of an
#     existing table, we have to make these changes in a roundabout way.

# ----------------------------------------------------------------
# Conversion to GWMEE 7.1.0
# ----------------------------------------------------------------

# The following items represent what was done in the gwcollagedb database, with some
# special adjustments for equivalent actions in the archive_gwcollagedb database.
#
# (*) Add the applicationtype.displayname field in the middle of that table.  Take
#     care to handle all fields after that correctly, including any associated
#     constraints.  Also, populate this new field appropriately.
#
# (*) Add category.applicationtypeid, category.agentid, and category.root fields to
#     the end of that table, in that order.
#
# (*) Add the "auditlog" table with proper ownership, and corresponding unique-ID
#     sequence and its ownership, and the association of the sequence with the
#     unique-identifier column in the table.
#
#     The 7.1.0 release adds the auditlog table to the gwcollagedb database, with
#     a PRIMARY KEY but no other indexes.  The archive_gwcollagedb database should
#     mirror the basic table structure, but since this copy of the data may be used
#     for reporting of long-term data, it might make sense to apply additional indexes
#     to this copy of the table.  We do not presently do so, however.
#
# (*) Add the "devicetemplateprofile" table with proper ownership, and corresponding
#     unique-ID sequence and its ownership, and the association of the sequence with
#     the unique-identifier column in the table.
#
# (*) Add the "hostblacklist" table with proper ownership, and corresponding unique-ID
#     sequence and its ownership, and the association of the sequence with the
#     unique-identifier column in the table.
#
# (*) Add the "hostidentity" table with proper ownership.
#
# (*) Add the "hostname" table with proper ownership.
#
# (*) Add constraints of various kinds (PRIMARY KEY, UNIQUE, FOREIGN KEY) to the auditlog,
#     category, devicetemplateprofile, hostblacklist, hostidentity, and hostname tables.
#     In the archive_gwcollagedb database, the hostname-table index on its own hostname
#     field (in this database, a constraint rather than an index) differs from the setup
#     in the gwcollagedb database, because it does not use the lower() function.  Imposing
#     that extra restriction might make sense for operational reasons in the upstream
#     runtime database, but it is of little value in the archive database and would
#     seriously complicate the archiving process.
#
# (*) Add ordinary and UNIQUE indexes to the category, hostidentity, and hostname tables.
#
# (*) Make content insertions into the ApplicationType, ApplicationEntityProperty, and
#     MonitorStatus tables, which generally ought to be handled during daily archiving by
#     automatic row mirroring rather than through changes during release migration.
#
# (*) Add the "categoryancestry" table with proper ownership.
#
# (*) Drop the category_name_key constraint from the category table.
#
# (*) Add the category_name_entitytypeid_key constraint to the category table.
#
# (*) Add a PRIMARY KEY constraint to the categoryancestry table.
#
# (*) Add indexes to the categoryancestry and categoryhierarchy tables.
#
# (*) Add FOREIGN KEY constraints to the category and categoryancestry tables.
#
# (*) Make content insertions into the EntityType table, which generally ought to be
#     handled during daily archiving by automatic row mirroring rather than through
#     changes during release migration.
#
# (*) The following describes what happens in the gwcollagedb database, but is not needed
#     in the archive_gwcollagedb database because we are not collapsing hostnames using the
#     lower() function in the archive database as is done in the runtime database.  Also,
#     most or all of the cleanup done by the pg_migrate_gwcollagedb.sql script should now be
#     sidestepped by running the show-similar-hosts.sql and merge-similar-hosts.sql scripts
#     before any upgrade in which the pg_migrate_gwcollagedb.sql script actions listed here
#     would have any effect.  Those scripts are much more thorough than the migration script
#     has ever been.
#
#     The pg_migrate_gwcollagedb.sql script does this:
#
#         Merge "duplicate" hosts, preserving the most-uppercase-looking host (that is, in the
#         "host" table, delete similar rows differing only by hostname lettercase).  This choice
#         of preferring uppercase, if present, is made because we consider it to be likely that
#         the most-uppercase version is the "real" (in some sense) host name introduced into the
#         Collage database from feeders or agents that preserve case (e.g., CloudHub).  Never
#         mind that CloudHub often cannot see the actual real hostname, and can only see an
#         associated display name provided by the VM management software.
#
#         In a situation where you have multiple lettercase patterns used for the "same"
#         hostname, the lowercase version of the host was probably created by Monarch.  While
#         that sufficed and is definitely more readable, for some reason we have chosen not to
#         go with it as the standard formulation, perhaps because that would be the obvious
#         choice, and if a site has chosen not to use that form, they probably prefer not to.
#
#         Update the "hostgroupcollection" table accordingly by adding the most-uppercase-looking
#         host to any hostgroup to which a similar [to-be-deleted] host belonged.  At least in
#         the gwcollagedb database, also adjust all historical rows in the "logmessage" table to
#         reflect the collapsing of multiple forms of hostname into a single canonical form, and
#         do the same in the "servicestatus" table.
#
#     That set of steps misses some stuff that needs to be done.  But the merge-similar-hosts.sql
#     does a thorough job, and if it's run before the migration, the pg_migrate_gwcollagedb.sql
#     script will encounter nothing to adjust.
#
#     The one area that does affect the archive_gwcollagedb database is that some of the
#     actions taken by the merge-similar-hosts.sql script can cause the gwcollagedb database
#     to be out of sync with the archive_gwcollagedb database.  Those residual issues are
#     handled by the conflicting_archive_service_rows.pl script, which is to be run after an
#     upgrade where the show-similar-hosts.sql and merge-similar-hosts.sql scripts needed to
#     be run before the upgrade.  The conflicting_archive_service_rows.pl script will make a
#     few adjustments in the archive_gwcollagedb database to bring the two databases back into
#     synchrony so daily archiving won't run into any index collisions.
#
# (*) Drop the old unique-hostname constraint on the "host" table that required case-sensitive
#     uniqueness.  (This is not actually required or even necessarily useful in the archive
#     database, because we are not enforcing case-insensitivity here as is now done in the
#     runtime database.  In the archive_gwcollagedb database, we might want to keep an explicit
#     index on the host.hostname field, which was formerly implicit because of the unique
#     constraint, to keep the usual queries on that table efficient.)
#
# (*) Add a unique index for the "host" table to prevent case-insensitive duplicate hosts from
#     appearing in the future.  (Again, we are not enforcing case-insensitivity in the archive
#     database, so there is no reason to exactly mirror here the setup in the runtime database.)
#
# (*) Add an index to the servicestatus table to make lookups on the servicedescription column
#     more efficient.
#
# (*) Add the servicestatus.applicationhostname field to the end of that table.  This change
#     requires the usual careful adjustments, because this is one of the tables that contains
#     the extra startvalidtime and endvalidtime columns in the archive_gwcollagedb database.
#     But worse, an upgrade to the 7.1.0 release damaged this table by putting the new column
#     at the end of the table, after the startvalidtime and endvalidtime columns.  So we first
#     need to undo that damage if we find it.
#
# (*) Make more content insertions into the ApplicationType table, which generally ought to be
#     handled during daily archiving by automatic row mirroring rather than through changes
#     during release migration.

# ----------------------------------------------------------------
# Conversion to GWMEE 7.1.1
# ----------------------------------------------------------------

# (*) The 7.1.1 release does not modify any schema elements.  It does potentially modify the
#     ApplicationType table content slightly, implementing a mistakenly-omitted correction
#     that ought to have been made in either the 7.0.2 fresh-install data or the 7.1.0 upgrade
#     actions.  Since it's too late to carry out the changes there, we must do so now in the
#     7.1.1 release for the gwcollagedb database.  See GWMON-12687 and GWMON-12689 for more
#     information.  This change will be reflected in the archive_gwcollagedb database by
#     automatic row mirroring, so there is nothing to do here in the migration script.
#
# (*) There are some other content changes to the ApplicationType and ConsolidationCriteria
#     tables in the gwcollagedb database.  The ApplicationType changes will then be reflected
#     in the archive_gwcollagedb database via ordinary daily archiving.  We do not currently
#     archive the ConsolidationCriteria table, so there is no issue there.

# ----------------------------------------------------------------
# Conversion to GWMEE 7.2.0
# ----------------------------------------------------------------

# (*) "timestamp without time zone" fields are converted to "timestamp with time zone"
#     fields, throughout the database.  We make these changes even to tables that do not
#     get archived, just to keep the schema in the archive_gwcollagedb database in sync
#     with the schema in the gwcollagedb database.
#
# (*) There are some minor content changes to the ApplicationType table.  It is not
#     necessary to handle those changes in this migration script, because they will
#     be handled automatically during ordinary regular archiving.

# ----------------------------------------------------------------
# Conversion to GWMEE 7.2.1
# ----------------------------------------------------------------

# (*) Add the "comment" table and associated objects (sequence, indexes, constraints).
#
# (*) There are some minor content changes to the ApplicationType table.  It is not
#     necessary to handle those changes in this migration script, because they will
#     be handled automatically during ordinary regular archiving.

# ----------------------------------------------------------------
# Conversion to GWMEE 7.2.2 (future)
# ----------------------------------------------------------------

# (*) In this timeframe, we will consider altering the startvalidtime and endvalidtime
#     fields in the archive_gwcollagedb database from "timestamp without time zone" to
#     "timestamp with time zone".  The companion Archive_GWCollageDB_extensions.sql
#     script would be modified at the same time for the same purpose.

##############################################################################
# Perl setup
##############################################################################

use strict;

use IO::Handle;
use DBI;

use TypedConfig;

##############################################################################
# Script parameters
##############################################################################

my $PROGNAME = "pg_migrate_archive_gwcollagedb.pl";
my $VERSION  = "7.2.1";

my $default_config_file = '/usr/local/groundwork/config/log-archive-receive.conf';

##############################################################################
# Command-line parameters
##############################################################################

# In theory, these parameter settings could be overridden by command-line arguments.
# In practice, we don't currently support any such arguments; this script uses only
# a fixed set of arguments.

my $config_file  = $default_config_file;
my $debug_config = 0;                      # if set, spill out certain data about config-file processing to STDOUT

##############################################################################
# Global variables
##############################################################################

my $all_is_done = 0;

# In the schemainfo table, '7.0.0' is used for both GWMEE 7.0.0 and GWMEE 7.0.1
# releases.  Unfortunately, the literal string '${groundwork.version}' is used
# for the GWMEE 7.0.2 release, so we need to understand that during an upgrade.
# And to top it off, the GWMEE 7.1.0 release declared itself to be 7.1.1 in this
# field.  What a mess!  Hopefully, we'll do better moving forward.
my $CurrentSchemaVersion = '7.2.1';

# Not used in this script.
my $enable_processing = undef;

my $archive_dbtype = undef;
my $archive_dbhost = undef;
my $archive_dbport = undef;
my $archive_dbname = undef;
my $archive_dbuser = undef;
my $archive_dbpass = undef;

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

sub read_config_file {
    my $config_file  = shift;
    my $config_debug = shift;

    # All the config-file processing is wrapped in an eval{}; because TypedConfig
    # throws exceptions when it cannot open the config file or finds bad config data.
    eval {
	my $config = TypedConfig->secure_new( $config_file, $config_debug );

	# Whether to process anything.  Turn this off if you want to disable
	# this process (that is, log archiving, not this migration script)
	# completely, so log-archiving is prohibited.
	$enable_processing = $config->get_boolean('enable_processing');

	$archive_dbtype = $config->get_scalar('archive_dbtype');
	$archive_dbhost = $config->get_scalar('archive_dbhost');
	$archive_dbport = $config->get_number('archive_dbport');
	$archive_dbname = $config->get_scalar('archive_dbname');
	$archive_dbuser = $config->get_scalar('archive_dbuser');
	$archive_dbpass = $config->get_scalar('archive_dbpass');
    };
    if ($@) {
	chomp $@;
	$@ =~ s/^ERROR:\s+//i;
	print "ERROR:  Cannot read config file $config_file\n  ($@).\n";
	return 0;
    }

    return 1;
}

sub open_database_connection {
    local %ENV = %ENV;
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
    $ENV{PGREQUIREPEER} = 'postgres';
    $ENV{PATH} = '/bin:/sbin:/usr/bin:/usr/sbin';

    # We deliberately only allow connection to a PostgreSQL database, no longer supporting
    # MySQL as an alternative, because we are now free to use (and in some cases require
    # the use of) various PostgreSQL capabilities such as savepoints, in our scripting.
    my $dsn = '';
    # if ( defined($archive_dbtype) && $archive_dbtype eq 'postgresql' ) {
	$dsn = "DBI:Pg:dbname=$archive_dbname;host=$archive_dbhost";
    # }
    # else {
    #     $dsn = "DBI:mysql:database=$archive_dbname;host=$archive_dbhost";
    # }

    # We turn AutoCommit off because we want to make changes roll back automatically as much as
    # possible if we don't get successfully through the entire script.  This is not perfect (i.e.,
    # we don't necessarily have all the changes made in a single huge transaction) because some of
    # the transformations may implicitly commit previous changes, and there is nothing we can do
    # about that.  Still, we do the best we can.
    #
    # We turn PrintError off because RaiseError is on and we don't want duplicate messages printed.
    print "\nConnecting to the $archive_dbname database with user $archive_dbuser ...\n";
    eval {
	$dbh = DBI->connect_cached( $dsn, $archive_dbuser, $archive_dbpass, { 'AutoCommit' => 0, 'RaiseError' => 1, 'PrintError' => 0 } )
    };
    if ($@) {
	chomp $@;
	print "ERROR:  Cannot connect to database $archive_dbname:\n$@\n";
	return 0;
    }

    return 1;
}

=pod

-- -------------------------------------------------------------------
-- Here are some queries we found useful while developing this script.
-- -------------------------------------------------------------------

-- To obtain a list of the ordinal positions of the columns used in all primary keys:
select   tc.table_catalog,
         tc.table_schema,
         tc.table_name,
	 tc.constraint_type,
         kcu.constraint_name,
         kcu.column_name,
         kcu.ordinal_position
from     information_schema.table_constraints tc
         left join information_schema.key_column_usage kcu
                 on  kcu.table_catalog   = tc.table_catalog
                 and kcu.table_schema    = tc.table_schema
                 and kcu.table_name      = tc.table_name
                 and kcu.constraint_name = tc.constraint_name
where        tc.constraint_type = 'PRIMARY KEY'
	 and tc.table_catalog   = current_catalog
	 and tc.table_schema    = current_schema
order by tc.table_catalog,
         tc.table_schema,
         tc.table_name,
         kcu.constraint_name,
         kcu.ordinal_position;

-- To collapse all the rows listed for each primary key, to obtain a list of all the
-- column names used in the primary key, in order by ordinal position within the key:
select   tc.table_catalog,
         tc.table_schema,
         tc.table_name,
         tc.constraint_type,
         kcu.constraint_name,
         string_agg(kcu.column_name, ', ' order by kcu.ordinal_position asc) as primary_key_columns
from     information_schema.table_constraints tc
         left join information_schema.key_column_usage kcu
                 on  kcu.table_catalog   = tc.table_catalog
                 and kcu.table_schema    = tc.table_schema
                 and kcu.table_name      = tc.table_name
                 and kcu.constraint_name = tc.constraint_name
where        tc.constraint_type = 'PRIMARY KEY'
	 and tc.table_catalog   = current_catalog
	 and tc.table_schema    = current_schema
group by tc.table_catalog,
         tc.table_schema,
         tc.table_name,
         tc.constraint_type,
         kcu.constraint_name
order by 1, 2, 3, 5;

-- To obtain a list of the ordinal positions of the columns used in all unique constraints:
select   tc.table_catalog,
         tc.table_schema,
         tc.table_name,
	 tc.constraint_type,
         kcu.constraint_name,
         kcu.column_name,
         kcu.ordinal_position
from     information_schema.table_constraints tc
         left join information_schema.key_column_usage kcu
                 on  kcu.table_catalog   = tc.table_catalog
                 and kcu.table_schema    = tc.table_schema
                 and kcu.table_name      = tc.table_name
                 and kcu.constraint_name = tc.constraint_name
where        tc.constraint_type = 'UNIQUE'
	 and tc.table_catalog   = current_catalog
	 and tc.table_schema    = current_schema
order by tc.table_catalog,
         tc.table_schema,
         tc.table_name,
         kcu.constraint_name,
         kcu.ordinal_position;

-- To collapse all the rows listed for each unique constraint, to obtain a list of all the
-- column names used in the unique constraint, in order by ordinal position within the constraint:
select   tc.table_catalog,
         tc.table_schema,
         tc.table_name,
         tc.constraint_type,
         kcu.constraint_name,
         string_agg(kcu.column_name, ', ' order by kcu.ordinal_position asc) as unique_columns
from     information_schema.table_constraints tc
         left join information_schema.key_column_usage kcu
                 on  kcu.table_catalog   = tc.table_catalog
                 and kcu.table_schema    = tc.table_schema
                 and kcu.table_name      = tc.table_name
                 and kcu.constraint_name = tc.constraint_name
where        tc.constraint_type = 'UNIQUE'
	 and tc.table_catalog   = current_catalog
	 and tc.table_schema    = current_schema
group by tc.table_catalog,
         tc.table_schema,
         tc.table_name,
         tc.constraint_type,
         kcu.constraint_name
order by 1, 2, 3, 5;

-- To obtain a list of the ordinal positions of the columns used in all foreign key constraints:
select   tc.table_catalog,
         tc.table_schema,
         tc.table_name,
	 tc.constraint_type,
         kcu.constraint_name,
         kcu.column_name,
         kcu.ordinal_position
from     information_schema.table_constraints tc
         left join information_schema.key_column_usage kcu
                 on  kcu.table_catalog   = tc.table_catalog
                 and kcu.table_schema    = tc.table_schema
                 and kcu.table_name      = tc.table_name
                 and kcu.constraint_name = tc.constraint_name
where        tc.constraint_type = 'FOREIGN KEY'
	 and tc.table_catalog   = current_catalog
	 and tc.table_schema    = current_schema
order by tc.table_catalog,
         tc.table_schema,
         tc.table_name,
         kcu.constraint_name,
         kcu.ordinal_position;

-- To collapse all the rows listed for each foreign key constraint, to obtain a list of all the
-- column names used in the foreign key constraint, in order by ordinal position within the constraint:
select   tc.table_catalog,
         tc.table_schema,
         tc.table_name,
	 tc.constraint_type,
         kcu.constraint_name,
         string_agg(kcu.column_name, ', ' order by kcu.ordinal_position asc) as foreign_key_columns
from     information_schema.table_constraints tc
         left join information_schema.key_column_usage kcu
                 on  kcu.table_catalog   = tc.table_catalog
                 and kcu.table_schema    = tc.table_schema
                 and kcu.table_name      = tc.table_name
                 and kcu.constraint_name = tc.constraint_name
where        tc.constraint_type  = 'FOREIGN KEY'
	 and tc.table_catalog   = current_catalog
	 and tc.table_schema    = current_schema
group by tc.table_catalog,
         tc.table_schema,
         tc.table_name,
         tc.constraint_type,
         kcu.constraint_name
order by 1, 2, 3, 5;

-- -------------------------------------------------------------------
-- End of sample queries we found useful while developing this script.
-- -------------------------------------------------------------------

=cut

sub table_exists {
    my $table_name      = shift;
    my $table_array_ref = $dbh->selectrow_arrayref( "
	select table_name
	from information_schema.tables
	where
	    table_catalog = current_catalog
	and table_schema  = current_schema
	and table_name    = '$table_name'
    " );
    return defined $table_array_ref;
}

sub table_column_exists {
    my $table_name  = shift;
    my $column_name = shift;

    # If RaiseError is not set, and selectrow_arrayref() fails, it will return undef.
    # selectrow_arrayref() will also return undef if there are no more rows; this can
    # be distinguished from an error by checking $dhh->err afterwards or using RaiseError.
    # But we're running with RaiseError, so that should not be a problem.
    my $table_column_array_ref = $dbh->selectrow_arrayref( "
	select column_name
	from information_schema.columns
	where
	    table_catalog = current_catalog
	and table_schema  = current_schema
	and table_name    = '$table_name'
	and column_name   = '$column_name'
    " );
    return defined $table_column_array_ref;
}

# table_column_position() returns undef if the column does not exist; otherwise, it
# returns the column's official numeric "ordinal_position" in the table.  Bear in
# mind that this is only a relative value, not an absolute value; column drops may
# leave holes in the column position numbering for the table.
sub table_column_position {
    my $table_name  = shift;
    my $column_name = shift;

    # If RaiseError is not set, and selectrow_arrayref() fails, it will return undef.
    # selectrow_arrayref() will also return undef if there are no more rows; this can
    # be distinguished from an error by checking $dhh->err afterwards or using RaiseError.
    # But we're running with RaiseError, so that should not be a problem.
    my $table_column_position_array_ref = $dbh->selectrow_arrayref( "
	select ordinal_position
	from information_schema.columns
	where
	    table_catalog = current_catalog
	and table_schema  = current_schema
	and table_name    = '$table_name'
	and column_name   = '$column_name'
    " );
    return defined($table_column_position_array_ref) ? $table_column_position_array_ref->[0] : undef;
}

sub sequence_exists {
    my $sequence_name      = shift;
    my $sequence_array_ref = $dbh->selectrow_arrayref( "
	select sequence_name
	from information_schema.sequences
	where
	    sequence_catalog = current_catalog
	and sequence_schema  = current_schema
	and sequence_name    = '$sequence_name'
    " );
    return defined $sequence_array_ref;
}

sub index_exists {
    my $table_name      = shift;
    my $index_name      = shift;
    my $index_array_ref = $dbh->selectrow_arrayref( "
	select indexname
	from pg_catalog.pg_indexes
	where
	    schemaname  = current_schema
	and tablename   = '$table_name'
	and indexname   = '$index_name'
    " );
    return defined $index_array_ref;
}

sub primary_key_exists {
    my $table_name   = shift;
    my $pkey_name    = shift;
    my $pkey_columns = shift;

    # FIX LATER:  use $pkey_columns as well, to be thoroughly sure

    my $primary_key_array_ref = $dbh->selectrow_arrayref( "
	select constraint_name
	from information_schema.table_constraints
	where
	    table_catalog      = current_catalog
	and table_schema       = current_schema
	and table_name         = '$table_name'
	and constraint_catalog = current_catalog
	and constraint_schema  = current_schema
	and constraint_name    = '$pkey_name'
	and constraint_type    = 'PRIMARY KEY'
    " );
    return defined $primary_key_array_ref;
}

sub unique_constraint_exists {
    my $table_name      = shift;
    my $constraint_name = shift;
    my $unique_columns  = shift;

    # FIX LATER:  use $unique_columns as well, to be thoroughly sure

    my $unique_constraint_array_ref = $dbh->selectrow_arrayref( "
	select constraint_name
	from information_schema.table_constraints
	where
	    table_catalog      = current_catalog
	and table_schema       = current_schema
	and table_name         = '$table_name'
	and constraint_catalog = current_catalog
	and constraint_schema  = current_schema
	and constraint_name    = '$constraint_name'
	and constraint_type    = 'UNIQUE'
    " );
    return defined $unique_constraint_array_ref;
}

sub foreign_key_constraint_exists {
    my $table_name          = shift;
    my $constraint_name     = shift;
    my $constrained_columns = shift;

    # FIX LATER:  use $constrained_columns as well, to be thoroughly sure

    my $foreign_key_constraint_array_ref = $dbh->selectrow_arrayref( "
	select constraint_name
	from information_schema.table_constraints
	where
	    table_catalog      = current_catalog
	and table_schema       = current_schema
	and table_name         = '$table_name'
	and constraint_catalog = current_catalog
	and constraint_schema  = current_schema
	and constraint_name    = '$constraint_name'
	and constraint_type    = 'FOREIGN KEY'
    " );
    return defined $foreign_key_constraint_array_ref;
}

sub constraint_exists {
    my $table_name          = shift;
    my $constraint_name     = shift;
    my $constrained_columns = shift;

    # FIX LATER:  use $constrained_columns as well, if defined, to be thoroughly sure

    my $constraint_array_ref = $dbh->selectrow_arrayref( "
	select constraint_name
	from information_schema.table_constraints
	where
	    table_catalog      = current_catalog
	and table_schema       = current_schema
	and table_name         = '$table_name'
	and constraint_catalog = current_catalog
	and constraint_schema  = current_schema
	and constraint_name    = '$constraint_name'
    " );
    return defined $constraint_array_ref;
}

sub idempotently_create_table {
    my $table_name           = shift;
    my $table_owner          = shift;
    my $table_create_command = shift;

    # If the $table_name is not in this database, add it now.
    if ( not table_exists($table_name) ) {
	$dbh->do($table_create_command);
	$dbh->do("ALTER TABLE public.$table_name OWNER TO $table_owner");
    }
}

sub idempotently_create_sequence {
    my $sequence_name     = shift;
    my $sequence_owner    = shift;
    my $sequence_owned_by = shift;

    # If the $sequence_name is not in this database, add it now.
    if ( not sequence_exists($sequence_name) ) {
	$dbh->do( "
	    CREATE SEQUENCE $sequence_name
		START WITH 1
		INCREMENT BY 1
		NO MINVALUE
		NO MAXVALUE
		CACHE 1
	" );
	$dbh->do("ALTER TABLE public.$sequence_name OWNER TO $sequence_owner");
	$dbh->do("ALTER SEQUENCE $sequence_name OWNED BY $sequence_owned_by");
    }
}

# Constants to use for the $is_unique argument to idempotently_create_index().
use constant NON_UNIQUE => 0;
use constant UNIQUE     => 1;

sub idempotently_create_index {
    my $table_name    = shift;
    my $index_name    = shift;
    my $is_unique     = shift;
    my $index_columns = shift;

    # If the $index_name is not in this database, add it now.
    if ( not index_exists( $table_name, $index_name ) ) {
	$dbh->do( "CREATE "
	      . ( $is_unique ? 'UNIQUE' : '' )
	      . " INDEX $index_name ON $table_name USING btree ("
	      . join( ', ', @$index_columns )
	      . ")" );
    }
}

sub idempotently_add_primary_key {
    my $table_name   = shift;
    my $pkey_name    = shift;
    my $pkey_columns = shift;

    # If the $pkey_name is not in this database, add it now.
    if ( not primary_key_exists( $table_name, $pkey_name, $pkey_columns ) ) {
	$dbh->do( "ALTER TABLE ONLY $table_name ADD CONSTRAINT $pkey_name PRIMARY KEY (" . join( ', ', @$pkey_columns ) . ")" );
    }
}

sub idempotently_add_unique_constraint {
    my $table_name      = shift;
    my $constraint_name = shift;
    my $unique_columns  = shift;

    # If the $constraint_name is not in this database, add it now.
    if ( not unique_constraint_exists( $table_name, $constraint_name, $unique_columns ) ) {
	$dbh->do( "ALTER TABLE ONLY $table_name ADD CONSTRAINT $constraint_name UNIQUE (" . join( ', ', @$unique_columns ) . ")" );
    }
}

# Constants to use for the $on_update and $on_delete arguments to
# idempotently_add_foreign_key_reference().

use constant ON_UPDATE_NO_ACTION   => 'NO ACTION';
use constant ON_UPDATE_RESTRICT    => 'RESTRICT';
use constant ON_UPDATE_CASCADE     => 'CASCADE';
use constant ON_UPDATE_SET_NULL    => 'SET NULL';
use constant ON_UPDATE_SET_DEFAULT => 'SET DEFAULT';

use constant ON_DELETE_NO_ACTION   => 'NO ACTION';
use constant ON_DELETE_RESTRICT    => 'RESTRICT';
use constant ON_DELETE_CASCADE     => 'CASCADE';
use constant ON_DELETE_SET_NULL    => 'SET NULL';
use constant ON_DELETE_SET_DEFAULT => 'SET DEFAULT';

sub idempotently_add_foreign_key_constraint {
    my $table_name            = shift;
    my $constraint_name       = shift;
    my $constrained_columns   = shift;
    my $foreign_table_name    = shift;
    my $foreign_table_columns = shift;
    my $update_action         = shift;
    my $delete_action         = shift;

    # If the $constraint_name is not in this database, add it now.
    if ( not foreign_key_constraint_exists( $table_name, $constraint_name, $constrained_columns ) ) {
	$dbh->do( "ALTER TABLE ONLY $table_name ADD CONSTRAINT $constraint_name FOREIGN KEY ("
	      . join( ', ', @$constrained_columns ) . ")"
	      . " REFERENCES $foreign_table_name("
	      . join( ', ', @$foreign_table_columns )
	      . ") ON UPDATE $update_action ON DELETE $delete_action" );
    }
}

sub idempotently_drop_index {
    my $table_name = shift;
    my $index_name = shift;

    # If the $index_name is in this database, drop it now.
    if ( index_exists( $table_name, $index_name ) ) {
	$dbh->do( "DROP INDEX IF EXISTS $index_name" );
    }
}

sub idempotently_drop_constraint {
    my $table_name      = shift;
    my $constraint_name = shift;

    # If the $constraint_name is in this database, drop it now.
    if ( constraint_exists( $table_name, $constraint_name ) ) {
	$dbh->do( "ALTER TABLE ONLY $table_name DROP CONSTRAINT IF EXISTS $constraint_name" );
    }
}

# I like table-driven changes, because editing a table is easy as long as you
# understand its structure, and it makes us less forgetful about modification
# details if we don't have to write the change code from scratch every time we
# need to make a similar change to a table.

# We want to conditionally add certain columns to certain tables.  However, this is
# really a complicated problem.  The new column might be being added as the last
# column of a table in gwcollagedb, but it must have a corresponding position in
# the corresponding table in archive_gwcollagedb.  Which is to say, in such a case
# it must appear after all the other columns except for the startvalidtime and
# endvalidtime columns in such a table.  Since PostgreSQL 9.1.X through at least
# 9.6.X does not allow inserting a new column in the middle of a table, we have a few
# choices.  See https://wiki.postgresql.org/wiki/Alter_column_position for details.
#
# (*) Create a new table which is a copy of the old table (except for the desired
#     extensions), then copy over all the data from the old table to the new table,
#     then drop the old table and rename the new table to have the old table
#     name.  Trouble is, we then need to go reconstruct any foreign key references
#     that referenced the old table.  And hope that any rows in other tables that
#     referenced the old table didn't get deleted or have foreign key references
#     nulled out when the old table was dropped.  And re-create all constraints that
#     were on the original table.  All around, this process seems a bit risky, which
#     is to say rather readily subject to errors of omission in the implementation.
#
# (*) Edit the existing table:
#     (+) Add the new column to the end of the existing table, followed by new
#         copies of any other columns that should follow the new column.  Don't
#         allow NOT NULL constraints in the definitions of the new columns at
#         this stage, at least if there is not also a DEFAULT value for the
#         column, because the new columns can't be created under that condition.
#     (+) Copy the data from the old columns to the new columns.
#     (+) Establish any NOT NULL constraints needed on the new columns.
#     (+) Drop the old columns.  (What happens to ON UPDATE and ON DELETE clauses
#         in foreign key constraints in other tables at this stage?)
#     (+) Rename the new columns to have the old column names.
#     (+) Re-create any constraints that referenced the old columns.
#
# In this second case, had there been any foreign-key references to the re-created
# columns, we would have had to deal with them in the same way as in the first case,
# which underscores the need for careful analysis no matter which choice we make.
# Perhaps if such foreign-key references are in play, the last few steps (that is,
# after establishing any NOT NULL constraints needed on the new columns) should be:
#
#     (+) Re-work any foreign-key constraints in other tables that reference the
#         old columns to refer to the new column names instead, presuming that when
#         the new columns are renamed to the old column names, the column names in
#         these foreign-key constraints will be automatically updated as well.
#     (+) Drop the old columns.
#     (+) Rename the new columns to have the old column names.
#     (+) Re-create any same-table constraints that referenced the old columns.
#
# We adopt the second approach, with the understanding that in PostgreSQL, dropping
# a column doesn't really remove it from the table; it just makes it inaccessible
# from SQL (see the Notes at the end of the ALTER TABLE command doc in PostgreSQL).
# This is the cost of reducing the complexity by not choosing the first case in our
# implementation choices.

# Here's the structure of the complex data structure that drives safe automated
# column insertions.  Unquoted fields in this structure are to be specified
# as-is.  Quoted fields are variable, to be set as appropriate for the particular
# transformations at issue.
#
#     my %column_changes = (
#         'table_name' => {
#             columns => [
#                 ['column_name', 'column_type', 'column_qualifiers'],
#                 ...
#             ],
#             unique_constraints => {
#                 'constraint_name' => ['constraint_field_1', 'constraint_field_2', ...],
#                 ...
#             }
#         },
#         ...
#     );
#
# The structure can be given any name you wish; a reference to the structure will be
# passed as a parameter to the routine that makes all the table alterations.  In this
# structure, the first 'column_name' (that is, columns[0][0]) must be the one that you
# wish to insert into the table, and the remaining elements of the columns array must
# describe the following fields in the existing table structure, with their respective
# column types (e.g., 'character varying(128)') and column qualifiers (generally
# just either 'not null' or undef).  If there is a DEFAULT value for the column, it
# must be specified as part of the column type, even though strictly speaking you
# would think of it as a column qualifier.  Each 'constraint_name' must be the name
# of some UNIQUE constraint that must be created after the other adjustments are
# complete, usually because it existed before but got dropped by the processing for
# those adjustments, but also possibly in case some new UNIQUE constraint is desired.
# The 'constraint_field_#' values name the fields of the constraint, in order as they
# are to appear in the constraint.

# FIX LATER:  That data structure contains no mention of foreign key constraints, either
# to or from the altered/moved columns.  The make_idempotent_column_changes() routine
# which swallows that data structure currently does not handle such adjustments, for
# either foreign tables or the local table under reconstruction.  If we encounter a table
# modification that involves foreign key constraints either referring to or attached to
# an altered/moved column, we'll need to make such an extension to both the data structure
# and the routine.
#
# In practice, we are probably saved from accidentally mishandling foreign key constraints
# from other tables pointing to the modified table, by the following facts.
#
# (*) For foreign key constraints from some other table to the to-be-altered table, it is
#     unlikely that the alterations will cause any hiccup.  That's because such a foreign-key
#     constraint has to refer to some set of columns that are collectively unique in the
#     referred-to table.  Such columns are usually placed at the beginning of the target
#     table, and it is unlikely that they would be subject to repositioning when the upstream
#     table is modified.
#
# (*) If we do attempt to drop a column that has some foreign key reference to it:
#
#         alter table target_table drop column target_column;
#
#     we will get a PostgreSQL error similar to this:
#
#         ERROR:  cannot drop table target_table column target_column because other objects depend on it
#         DETAIL:  constraint ref_table_ibfk_1 on table ref_table depends on table target_table column target_column
#         HINT:  Use DROP ... CASCADE to drop the dependent objects too.
#
#     That will notify us that we really do have a problem and need to deal with it.
#
# (*) If we do add the CASCADE keyword to the statement to drop a column:
#
#         alter table target_table drop column target_column cascade;
#
#     then the entire foreign-key connection gets dropped when the target_column is dropped.
#     There is a NOTICE about that:
#
#         NOTICE:  drop cascades to constraint ref_table_ibfk_1 on table ref_table
#
#     so it's not silent; we have a way of seeing if that ever happens.
#
# On the other hand, with respect to foreign key constraints attached to columns in the
# modified table that need to remain attached when those columns are moved:
#
# (*) For foreign key constraints to some other table from the to-be-altered table, when a
#     column with a foreign-key constraint attached is dropped, the foreign-key constraint
#     is silently dropped along with the column.  There is no visible notice of this change.
#     So we need to be vigilant in development about such a possibility, examining the table
#     columns to be altered and moved, noticing if any of them are involved in foreign-key
#     constraints, and extending the code here to handle that situation if it ever arises.
#
# (*) To have a foreign-key constraint on a new (copied from old) column, you need to create
#     it explicitly.  You can create it before the old column is dropped, using the new column
#     name, and then the column name within that foreign-key reference will be automatically
#     modified when the new column is renamed to the old column name after the old column is
#     dropped.  That's the good news.  The bad news is that if you want to retain the same
#     foreign-key constraint name as you had before you started messing with the table, you'll
#     need to either explicitly drop the old foreign-key constraint before you create the new
#     foreign-key constraint, so you don't have a constraint-name collision, or you'll need
#     to wait until after the old column is dropped (which automatically removes its attached
#     foreign-key constraint) before creating the new foreign-key constraint.  Which basically
#     means you may as well not depend on the automatic renaming of the column name in the
#     foreign-key constraint, and just wait until both the old column is dropped and the new
#     column is renamed to the old name before using SQL identical to that which previously
#     created the old foreign-key constraint, with the same constraint name and column name as
#     were originally used.

sub make_idempotent_column_changes {
    my $changes = shift;

    foreach my $table ( keys %$changes ) {
	## FIX LATER:  Currently, we assume that each element of the top-level hash has
	## a "columns" hash key.  But in the future, we might extend this to allow this
	## routine to only establish UNIQUE constraints without having to also process some
	## column changes.  In that case, the "columns" hash key might be missing.  If
	## we allow that, then we need to ensure idempotency fof adding UNIQUE constraints,
	## because in the present code, we are pretty much assuming that any constraints we
	## handle here don't already exist (because they either never existed or because
	## they got dropped when the column processing occurred).
	##
	my $column_to_insert = $changes->{$table}{columns}[0][0];

	# Find out whether the first declared column already exists.
	# If the $column_to_insert is not in this $table, add it now.
	#
	if ( not table_column_exists( $table, $column_to_insert ) ) {
	    my @add_column_clauses       = ();
	    my @drop_column_clauses      = ();
	    my @rename_column_clauses    = ();
	    my @update_column_clauses    = ();
	    my @column_qualifier_clauses = ();
	    foreach my $column_definition ( @{ $changes->{$table}{columns} } ) {
		my $column_name = $column_definition->[0];
		if ( $column_name ne $column_to_insert ) {
		    push @update_column_clauses, "new_$column_name = $column_name";
		    push @drop_column_clauses,   "drop column $column_name";
		    push @rename_column_clauses, "rename column new_$column_name to $column_name";
		    $column_name = "new_$column_name";
		}
		my $column_type = $column_definition->[1];
		push @add_column_clauses, "add column $column_name $column_type";
		my $column_qualifiers = $column_definition->[2];
		if ( defined $column_qualifiers ) {
		    push @column_qualifier_clauses, "alter column $column_name set $column_qualifiers";
		}
	    }
	    $dbh->do( "alter table $table " . join( ', ', @add_column_clauses ) );
	    $dbh->do( "update $table set "  . join( ', ', @update_column_clauses ) ) if @update_column_clauses;
	    $dbh->do( "alter table $table " . join( ', ', @column_qualifier_clauses ) ) if @column_qualifier_clauses;
	    $dbh->do( "alter table $table " . join( ', ', @drop_column_clauses ) ) if @drop_column_clauses;
	    $dbh->do( "alter table $table $_" ) for @rename_column_clauses;

	    # FIX LATER:  Check the ownership of added UNIQUE constraints; set here if necessary (but probably not).

	    # After making the column adjustments, restore the UNIQUE constraint(s) that got
	    # dropped, if any, when the original columns were dropped.  And while we're at it,
	    # we can include the new column in the replaced constraints, or add new UNIQUE
	    # constraints for the new column, since those operations are convenient here.
	    if ( exists $changes->{$table}{unique_constraints} ) {
		foreach my $constraint ( keys %{ $changes->{$table}{unique_constraints} } ) {
		    $dbh->do( "alter table $table add constraint \"$constraint\" UNIQUE ("
			  . join( ', ', @{ $changes->{$table}{unique_constraints}{$constraint} } )
			  . ")" );
		}
	    }
	}
    }
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
# Connect to the database
##############################################################################

print "\narchive_collagedb update starting ...\n";
print "=============================================================\n";

if ( not read_config_file( $config_file, $debug_config ) ) {
    die "FATAL:  Cannot read config file!\n";
}

if ( not open_database_connection() ) {
    die "FATAL:  Cannot open a database connection!\n";
}

print "\nEncapsulating the changes in a transaction ...\n";
$dbh->do("set session transaction isolation level serializable");

##############################################################################
# Prepare for changes, in a manner that we can use to tell if the subsequent
# processing got aborted before it was finished.
##############################################################################

print "\nAltering the CurrentSchemaVersion value ...\n";

# Our first act of modifying the database is to update the archive_gwcollagedb version
# number, so it reflects the fact that the schema and content are in transition.

$sqlstmt = "select value from schemainfo where name = 'CurrentSchemaVersion'";
my ($old_CurrentSchemaVersion) = $dbh->selectrow_array($sqlstmt);

# Create an artificial archive_gwcollagedb version number which we will use to flag the fact that a migration is in progress.
# If the migration completes successfully, this setting will be updated to be the target archive_gwcollagedb version.
# If not, it will remain as an indicator to later users of the database that the schema is in bad shape.
my $transient_CurrentSchemaVersion = defined($old_CurrentSchemaVersion) && length($old_CurrentSchemaVersion) ? $old_CurrentSchemaVersion : '0.0.1';
$transient_CurrentSchemaVersion = '7.0.2' if $transient_CurrentSchemaVersion eq '${groundwork.version}';
$transient_CurrentSchemaVersion = '-' . $transient_CurrentSchemaVersion if $transient_CurrentSchemaVersion !~ /^-/;

# We delete/insert instead of just updating, in case somehow the database is so corrupted
# that it entirely lacks a current CurrentSchemaVersion setting.
$sqlstmt = "delete from schemainfo where name = 'CurrentSchemaVersion'";
$dbh->do($sqlstmt);

# For now, we stuff in a value for the archive_gwcollagedb version that will flag the fact that migration
# is in progress.  This will be replaced at the very end if we got through the entire script unscathed.
do {
    ## Localize and turn off RaiseError for this block, so we can test explicitly for
    ## certain conditions and emit a more informative error message in some cases.
    local $dbh->{RaiseError};

    $sqlstmt = "INSERT INTO schemainfo VALUES('CurrentSchemaVersion',?)";
    $outcome = $dbh->do( $sqlstmt, {}, $transient_CurrentSchemaVersion );
    if ( not defined $outcome ) {
	print $dbh->errstr . "\n";
	exit (1);
    }
    if ( $outcome != 1 ) {
	## It's highly unlikely that we would get here if the insert worked,
	## as I don't see any circumstances where the insert would succeed but
	## produce a non-unit outcome.  Nevertheless, if something weird like
	## that happens, it would be more helpful to know about the failure
	## in terms of what it means to this application script.
	print "ERROR:  Could not update the CurrentSchemaVersion setting before migration!\n";
	exit (1);
    }
};

##############################################################################
# Schema and data changes for the transition to GWMEE 7.0.1
##############################################################################

#-----------------------------------------------------------------------------
# Add "agentid" columns to certain tables, if needed.
#-----------------------------------------------------------------------------

print "\nAdding agentid columns, if needed ...\n";

my %add_agentid_columns = (
    'host' => {
	columns => [
	    [ 'agentid',        'character varying(128)',      undef ],
	    [ 'startvalidtime', 'timestamp without time zone', 'not null' ],
	    [ 'endvalidtime',   'timestamp without time zone', undef ]
	],
	unique_constraints => { 'host_hostname_startvalidtime_key' => [qw (hostname startvalidtime)] }
    },
    'hostgroup' => {
	columns => [
	    [ 'agentid',        'character varying(128)',      undef ],
	    [ 'startvalidtime', 'timestamp without time zone', 'not null' ],
	    [ 'endvalidtime',   'timestamp without time zone', undef ]
	],
	unique_constraints => { 'hostgroup_name_startvalidtime_key' => [qw (name startvalidtime)] }
    },
    'servicestatus' => {
	columns => [
	    [ 'agentid',        'character varying(128)',      undef ],
	    [ 'startvalidtime', 'timestamp without time zone', 'not null' ],
	    [ 'endvalidtime',   'timestamp without time zone', undef ]
	],
	unique_constraints =>
	  { 'servicestatus_hostid_servicedescription_startvalidtime_key' => [qw (hostid servicedescription startvalidtime)] }
    }
);

make_idempotent_column_changes(\%add_agentid_columns);

##############################################################################
# Schema and data changes for the transition to GWMEE 7.1.0
##############################################################################

#-----------------------------------------------------------------------------
# (*) Add the applicationtype.displayname field in the middle of that table.
#     Take care to handle all fields after that correctly, including any
#     associated constraints.  Also, populate this new field appropriately.
#-----------------------------------------------------------------------------

# This schema update suffices.  Populating the new field will be handled by the
# next pass of daily archiving, which will mirror the entire runtime table to
# the archive table, thereby filling in the new field for all rows that have it
# populated upstream.

print "\nAdding applicationtype.displayname column, if needed ...\n";

my %add_applicationtype_displayname_column = (
    'applicationtype' => {
	columns => [
	    [ 'displayname',             'character varying(128)',      undef ],
	    [ 'description',             'character varying(254)',      undef ],
	    [ 'statetransitioncriteria', 'character varying(512)',      undef ],
	    [ 'startvalidtime',          'timestamp without time zone', 'not null' ],
	    [ 'endvalidtime',            'timestamp without time zone', undef ]
	],
	unique_constraints => { 'applicationtype_name_startvalidtime_key' => [qw (name startvalidtime)] }
    }
);

make_idempotent_column_changes(\%add_applicationtype_displayname_column);

#- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

# SPECIAL-CASE FIX:  The pg_migrate_archive_gwcollagedb-702sp3.pl script used in an
# upgrade from 7.0.2-SP02 to 7.0.2-SP03 did not properly apply a "NOT NULL" modifier to
# the startvalidtime field when it re-created that field while adding the displayname
# field in the middle of the table.  If the displayname field is already present in the
# applicationtype table, the column changes in the item just above won't fix that.  We
# therefore need an idempotent means here to fix that possible damage.  Fortunately,
# we can just add the modifier without any prior testing; that action is idempotently
# interpreted by PostgreSQL without error if it is already present on the table column.

$dbh->do( "alter table applicationtype alter column startvalidtime set not null" );

#- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

# SPECIAL-CASE FIX:  The pg_migrate_archive_gwcollagedb-702sp3.pl script used in an upgrade from
# 7.0.2-SP02 to 7.0.2-SP03, and the pg_migrate_archive_gwcollagedb.pl script used in an upgrade
# to 7.1.0, both added the displayname column to the applicationtype table, but did so using a
# mechanism which failed to notice that any constraints involving the startvalidtime column would
# need to be re-established after columns got dropped and re-added while they were being moved
# around.  So effectively, the applicationtype_name_startvalidtime_key constraint gets dropped
# in those upgrades.  Since our code above takes those two actions (adding a new column and
# applying any new or prior unique constraints involving the affected columns) only in concert
# (as it should, because the actions are related), it will see that the displayname field is now
# present, but it won't then notice that the applicationtype_name_startvalidtime_key constraint
# is missing.  We repair that damage here.

idempotently_add_unique_constraint( 'applicationtype', 'applicationtype_name_startvalidtime_key', [qw(name startvalidtime)] );

#-----------------------------------------------------------------------------
# (*) Add category.applicationtypeid, category.agentid, and category.root
#     fields to the end of that table, in that order.
#-----------------------------------------------------------------------------

print "\nAdding category table columns, if needed ...\n";

# Because make_idempotent_column_changes() only checks the first column of the set
# we specify in the hash for existence, and assumes that all the other columns do
# already exist, we must perform the addition of multiple new columns to a single
# table in separate stages, so as not to try to assign from or drop columns that
# don't yet exist.  If that weren't already reason enough, this also allows us to
# recover from a partial table update in the 7.1.0 release.
#
# Also, neither the pg_migrate_archive_gwcollagedb-702sp3.pl script used in an
# upgrade from 7.0.2-SP02 to 7.0.2-SP03 nor the pg_migrate_archive_gwcollagedb.pl
# script in the 7.1.0 release correctly extends the category table with the "root"
# field to match the structure of this table in the 7.0.2-SP03 or 7.1.0 gwcollagedb
# database, respectively.  We therefore need an idempotent means here to fix that
# possible damage, and the single-column idempotent column change here does that.
# We are operationally saved in releases 7.0.2 SP03 through at least 7.1.1 by
# the fact that the category table is not listed as one that we archive, so this
# mismatch does not disrupt archiving in such releases.

my %add_category_applicationtypeid_column = ( 'category' => { columns => [ [ 'applicationtypeid', 'integer',                undef ] ] } );
my %add_category_agentid_column           = ( 'category' => { columns => [ [ 'agentid',           'character varying(128)', undef ] ] } );
my %add_category_root_column              = ( 'category' => { columns => [ [ 'root',              'boolean default true',   'not null' ] ] } );

make_idempotent_column_changes( \%add_category_applicationtypeid_column );
make_idempotent_column_changes( \%add_category_agentid_column );
make_idempotent_column_changes( \%add_category_root_column );

#-----------------------------------------------------------------------------
# (*) Add the "auditlog" table with proper ownership, and corresponding
#     unique-ID sequence and its ownership, and the association of the
#     sequence with the unique-identifier column in the table.
#
#     The 7.1.0 release adds the auditlog table to the gwcollagedb database,
#     with a PRIMARY KEY but no other indexes.  The archive_gwcollagedb
#     database should mirror the basic table structure, but since this copy
#     of the data may be used for reporting of long-term data, it might make
#     sense to apply additional indexes to this copy of the table.  We do not
#     presently do so, however.
#-----------------------------------------------------------------------------

print "\nAdding the auditlog table, if needed ...\n";

idempotently_create_table(
    'auditlog', 'collage',
    'CREATE TABLE IF NOT EXISTS auditlog (
	auditlogid         integer                     NOT NULL,
	subsystem          character varying(254)      NOT NULL,
	action             character varying(32)       NOT NULL,
	description        character varying(4096)     NOT NULL,
	username           character varying(254)      NOT NULL,
	logtimestamp       timestamp without time zone NOT NULL,
	hostname           character varying(254),
	servicedescription character varying(254),
	hostgroupname      character varying(254),
	servicegroupname   character varying(254)
    )'
);

idempotently_create_sequence( 'auditlog_auditlogid_seq', 'collage', 'auditlog.auditlogid' );

#- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

# FORENSIC DATA FOR SPECIAL-CASE FIX (below):  Releases prior to 7.1.1 left the
# archive_gwcollagedb copy of the auditlog table in a damaged condition.  Here we
# present accumulated observational data in service of discovering the exact nature
# of damage from earlier releases, so we can fix it accurately and thoroughly.
#
# What matters to us now (for use of this migration script in GWMEE 7.1.1 and later)
# is what this script will encounter when it is executed in 7.1.1 and later releases.
# So mostly, we want to know the condition of the databases after various paths used
# to get to the 7.1.0 release and 7.1.1 releases.  There may be some additional data
# presented here to document the situation in certain earlier releases.

# After 7.0.2 fresh install:
# After 7.0.2 fresh install + SP01:
# After 7.0.2 fresh install + SP02:
# After 7.0.2 fresh install + SP01 + SP02:
# in all of those scenarios, in         gwcollagedb:  no auditlog table exists
# in all of those scenarios, in archive_gwcollagedb:  no auditlog table exists

# After 7.0.2 fresh install + upgrade to 7.1.0:
#     Error: There has been an error.
#     Upgrades from GWMEE 7.0.2 SP02 or newer are supported. Please update your
#     install to GWMEE 7.0.2 SP02 before running the GWMEE 7.1.0-br389-gw2833
#     installer again.

# After 7.0.2 fresh install + SP01 + upgrade to 7.1.0:
#     Error: There has been an error.
#     Upgrades from GWMEE 7.0.2 SP02 or newer are supported. Please update your
#     install to GWMEE 7.0.2 SP02 before running the GWMEE 7.1.0-br389-gw2833
#     installer again.

# After 7.0.2 fresh install + SP02 + upgrade to 7.1.0:
#
# in gwcollagedb:  auditlog columns are:
#
#            Column       |            Type             | Modifiers
#     --------------------+-----------------------------+-----------
#      auditlogid         | integer                     | not null
#      subsystem          | character varying(254)      | not null
#      action             | character varying(32)       | not null
#      description        | character varying(4096)     | not null
#      username           | character varying(254)      | not null
#      logtimestamp       | timestamp without time zone | not null
#      hostname           | character varying(254)      |
#      servicedescription | character varying(254)      |
#      hostgroupname      | character varying(254)      |
#      servicegroupname   | character varying(254)      |
#
# in archive_gwcollagedb:  auditlog columns are:
#
#            Column       |            Type             | Modifiers
#     --------------------+-----------------------------+-----------
#      auditlogid         | integer                     | not null
#      subsystem          | character varying(254)      | not null
#      hostname           | character varying(254)      | not null
#      action             | character varying(32)       | not null
#      description        | character varying(4096)     | not null
#      username           | character varying(254)      | not null
#      logtimestamp       | timestamp without time zone | not null
#      servicedescription | character varying(254)      |
#
# which means that much more than just the last few columns are messed up;
# the hostname column is in the wrong place and has a "not null" modifier
# applied, and the hostgroupname and servicegroupname columns are missing.
#
# In the 7.1.0 pg_migrate_gwcollagedb.sql script, we find:
#
#     CREATE TABLE IF NOT EXISTS auditlog (
#         auditlogid         integer NOT NULL,
#         subsystem          character varying(254)      NOT NULL,
#         action             character varying(32)       NOT NULL,
#         description        character varying(4096)     NOT NULL,
#         username           character varying(254)      NOT NULL,
#         logtimestamp       timestamp without time zone NOT NULL,
#         hostname           character varying(254),
#         servicedescription character varying(254),
#         hostgroupname      character varying(254),
#         servicegroupname   character varying(254)
#     );
#
# In the 7.1.0 pg_migrate_archive_gwcollagedb.pl script, we find the following,
# probably a holdover from an earlier iteration of the script before the table
# definition was revised for the final 7.1.0 release:
#
#     CREATE TABLE auditlog (
#         auditlogid         integer                     NOT NULL,
#         subsystem          character varying(254)      NOT NULL,
#         hostname           character varying(254)      NOT NULL,
#         action             character varying(32)       NOT NULL,
#         description        character varying(4096)     NOT NULL,
#         username           character varying(254)      NOT NULL,
#         logtimestamp       timestamp without time zone NOT NULL,
#         servicedescription character varying(254)
#     )
#
# After 7.0.2 fresh install + SP02 + upgrade to 7.1.0 + upgrade to 7.1.1-br413-gw3073:
#
# in gwcollagedb:  auditlog columns are:
#
#            Column       |            Type             | Modifiers
#     --------------------+-----------------------------+-----------
#      auditlogid         | integer                     | not null
#      subsystem          | character varying(254)      | not null
#      action             | character varying(32)       | not null
#      description        | character varying(4096)     | not null
#      username           | character varying(254)      | not null
#      logtimestamp       | timestamp without time zone | not null
#      hostname           | character varying(254)      |
#      servicedescription | character varying(254)      |
#      hostgroupname      | character varying(254)      |
#      servicegroupname   | character varying(254)      |
#
# in archive_gwcollagedb:  auditlog columns are:
#
#            Column       |            Type             | Modifiers
#     --------------------+-----------------------------+-----------
#      auditlogid         | integer                     | not null
#      subsystem          | character varying(254)      | not null
#      action             | character varying(32)       | not null
#      description        | character varying(4096)     | not null
#      username           | character varying(254)      | not null
#      logtimestamp       | timestamp without time zone | not null
#      hostname           | character varying(254)      |
#      servicedescription | character varying(254)      |
#      hostgroupname      | character varying(254)      |
#      servicegroupname   | character varying(254)      |
#
# which means that the 7.1.1-br413-gw3073 dev build (including our special-case
# fixes below) fixed the table-schema problem for this upgrade path.

# After 7.0.2 fresh install + SP02 + SP03:
#
# in gwcollagedb:  auditlog columns are:
#
#            Column       |            Type             | Modifiers
#     --------------------+-----------------------------+-----------
#      auditlogid         | integer                     | not null
#      subsystem          | character varying(254)      | not null
#      action             | character varying(32)       | not null
#      description        | character varying(4096)     | not null
#      username           | character varying(254)      | not null
#      logtimestamp       | timestamp without time zone | not null
#      hostname           | character varying(254)      |
#      servicedescription | character varying(254)      |
#      hostgroupname      | character varying(254)      |
#      servicegroupname   | character varying(254)      |
#
# in archive_gwcollagedb:  auditlog columns are:
#
#            Column       |            Type             | Modifiers
#     --------------------+-----------------------------+-----------
#      auditlogid         | integer                     | not null
#      subsystem          | character varying(254)      | not null
#      hostname           | character varying(254)      | not null
#      action             | character varying(32)       | not null
#      description        | character varying(4096)     | not null
#      username           | character varying(254)      | not null
#      logtimestamp       | timestamp without time zone | not null
#      servicedescription | character varying(254)      |
#
# After 7.0.2 fresh install + SP02 + SP03 + upgrade to 7.1.0:
#
# in gwcollagedb:  auditlog columns are:
#
#            Column       |            Type             | Modifiers
#     --------------------+-----------------------------+-----------
#      auditlogid         | integer                     | not null
#      subsystem          | character varying(254)      | not null
#      action             | character varying(32)       | not null
#      description        | character varying(4096)     | not null
#      username           | character varying(254)      | not null
#      logtimestamp       | timestamp without time zone | not null
#      hostname           | character varying(254)      |
#      servicedescription | character varying(254)      |
#      hostgroupname      | character varying(254)      |
#      servicegroupname   | character varying(254)      |
#
# in archive_gwcollagedb:  auditlog columns are:
#
#            Column       |            Type             | Modifiers
#     --------------------+-----------------------------+-----------
#      auditlogid         | integer                     | not null
#      subsystem          | character varying(254)      | not null
#      hostname           | character varying(254)      | not null
#      action             | character varying(32)       | not null
#      description        | character varying(4096)     | not null
#      username           | character varying(254)      | not null
#      logtimestamp       | timestamp without time zone | not null
#      servicedescription | character varying(254)      |
#
# which is to say, the 7.1.0 migration script did absolutely nothing to
# properly repair the table in this scenario.
#
# After 7.0.2 fresh install + SP02 + SP03 + upgrade to 7.1.0 + upgrade to 7.1.1-br413-gw3073:
#
# in gwcollagedb:  auditlog columns are:
#
#            Column       |            Type             | Modifiers
#     --------------------+-----------------------------+-----------
#      auditlogid         | integer                     | not null
#      subsystem          | character varying(254)      | not null
#      action             | character varying(32)       | not null
#      description        | character varying(4096)     | not null
#      username           | character varying(254)      | not null
#      logtimestamp       | timestamp without time zone | not null
#      hostname           | character varying(254)      |
#      servicedescription | character varying(254)      |
#      hostgroupname      | character varying(254)      |
#      servicegroupname   | character varying(254)      |
#
# in archive_gwcollagedb:  auditlog columns are:
#
#            Column       |            Type             | Modifiers
#     --------------------+-----------------------------+-----------
#      auditlogid         | integer                     | not null
#      subsystem          | character varying(254)      | not null
#      action             | character varying(32)       | not null
#      description        | character varying(4096)     | not null
#      username           | character varying(254)      | not null
#      logtimestamp       | timestamp without time zone | not null
#      hostname           | character varying(254)      |
#      servicedescription | character varying(254)      |
#      hostgroupname      | character varying(254)      |
#      servicegroupname   | character varying(254)      |
#
# which means that the 7.1.1-br413-gw3073 dev build (including our special-case
# fixes below) fixed the table-schema problem for this upgrade path.

# After 7.1.0 fresh install:
#
# in gwcollagedb:  auditlog columns are:
#
#            Column       |            Type             | Modifiers
#     --------------------+-----------------------------+-----------
#      auditlogid         | integer                     | not null
#      subsystem          | character varying(254)      | not null
#      action             | character varying(32)       | not null
#      description        | character varying(4096)     | not null
#      username           | character varying(254)      | not null
#      logtimestamp       | timestamp without time zone | not null
#      hostname           | character varying(254)      |
#      servicedescription | character varying(254)      |
#      hostgroupname      | character varying(254)      |
#      servicegroupname   | character varying(254)      |
#
# in archive_gwcollagedb:  auditlog columns are:
#
#            Column       |            Type             | Modifiers
#     --------------------+-----------------------------+-----------
#      auditlogid         | integer                     | not null
#      subsystem          | character varying(254)      | not null
#      action             | character varying(32)       | not null
#      description        | character varying(4096)     | not null
#      username           | character varying(254)      | not null
#      logtimestamp       | timestamp without time zone | not null
#      hostname           | character varying(254)      |
#      servicedescription | character varying(254)      |
#      hostgroupname      | character varying(254)      |
#      servicegroupname   | character varying(254)      |
#
# which is to say, unsurprisingly, the archive table exactly mirrors the runtime table
# after a fresh 7.1.0 install.
#
# After 7.1.0 fresh install + upgrade to 7.1.1-br413-gw3073:
#
# in gwcollagedb:  auditlog columns are:
#
#            Column       |            Type             | Modifiers
#     --------------------+-----------------------------+-----------
#      auditlogid         | integer                     | not null
#      subsystem          | character varying(254)      | not null
#      action             | character varying(32)       | not null
#      description        | character varying(4096)     | not null
#      username           | character varying(254)      | not null
#      logtimestamp       | timestamp without time zone | not null
#      hostname           | character varying(254)      |
#      servicedescription | character varying(254)      |
#      hostgroupname      | character varying(254)      |
#      servicegroupname   | character varying(254)      |
#
# in archive_gwcollagedb:  auditlog columns are:
#
#            Column       |            Type             | Modifiers
#     --------------------+-----------------------------+-----------
#      auditlogid         | integer                     | not null
#      subsystem          | character varying(254)      | not null
#      action             | character varying(32)       | not null
#      description        | character varying(4096)     | not null
#      username           | character varying(254)      | not null
#      logtimestamp       | timestamp without time zone | not null
#      hostname           | character varying(254)      |
#      servicedescription | character varying(254)      |
#      hostgroupname      | character varying(254)      |
#      servicegroupname   | character varying(254)      |
#
# which is to say, unsurprisingly, the archive table exactly mirrors the runtime table
# after this upgrade, since it did so already before the upgrade.

# After a 7.1.1-br413-gw3073 fresh install:
#
# in gwcollagedb:  auditlog columns are:
#
#            Column       |            Type             | Modifiers
#     --------------------+-----------------------------+-----------
#      auditlogid         | integer                     | not null
#      subsystem          | character varying(254)      | not null
#      action             | character varying(32)       | not null
#      description        | character varying(4096)     | not null
#      username           | character varying(254)      | not null
#      logtimestamp       | timestamp without time zone | not null
#      hostname           | character varying(254)      |
#      servicedescription | character varying(254)      |
#      hostgroupname      | character varying(254)      |
#      servicegroupname   | character varying(254)      |
#
# in archive_gwcollagedb:  auditlog columns are:
#
#            Column       |            Type             | Modifiers
#     --------------------+-----------------------------+-----------
#      auditlogid         | integer                     | not null
#      subsystem          | character varying(254)      | not null
#      action             | character varying(32)       | not null
#      description        | character varying(4096)     | not null
#      username           | character varying(254)      | not null
#      logtimestamp       | timestamp without time zone | not null
#      hostname           | character varying(254)      |
#      servicedescription | character varying(254)      |
#      hostgroupname      | character varying(254)      |
#      servicegroupname   | character varying(254)      |

#- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

# SPECIAL-CASE FIX:  As noted above, an upgrade to the 7.1.0 release from some previous
# release creates the archive_gwcollagedb database copy of the auditlog table with an
# improper schema.  If we find that is the case at the present time, we will now need to
# make corrections, even though these separate adjustments do not appear explicitly in the
# pg_migrate_gwcollagedb.sql script from which most of the other actions in this script
# are derived.

# Per all the forensic data shown above, after an upgrade to 7.1.0 from either 7.0.2-SP02
# or 7.0.2-SP03, we find this to be the case:
#
# In gwcollagedb, the auditlog columns are:
#
#            Column       |            Type             | Modifiers
#     --------------------+-----------------------------+-----------
#      auditlogid         | integer                     | not null
#      subsystem          | character varying(254)      | not null
#      action             | character varying(32)       | not null
#      description        | character varying(4096)     | not null
#      username           | character varying(254)      | not null
#      logtimestamp       | timestamp without time zone | not null
#      hostname           | character varying(254)      |
#      servicedescription | character varying(254)      |
#      hostgroupname      | character varying(254)      |
#      servicegroupname   | character varying(254)      |
#
# But in archive_gwcollagedb, the auditlog columns are:
#
#            Column       |            Type             | Modifiers
#     --------------------+-----------------------------+-----------
#      auditlogid         | integer                     | not null
#      subsystem          | character varying(254)      | not null
#      hostname           | character varying(254)      | not null
#      action             | character varying(32)       | not null
#      description        | character varying(4096)     | not null
#      username           | character varying(254)      | not null
#      logtimestamp       | timestamp without time zone | not null
#      servicedescription | character varying(254)      |
#
# which means that much more than just the last few columns are messed up; the hostname
# column is in the wrong place and has a "not null" modifier, and the hostgroupname and
# servicegroupname columns are missing.  We need to repair such damage if exists, while
# not damaging anything else if we find the table schema already in good shape.

my $auditlog_hostname_position     = table_column_position( 'auditlog', 'hostname' );
my $auditlog_logtimestamp_position = table_column_position( 'auditlog', 'logtimestamp' );

if (    defined($auditlog_hostname_position)
    and defined($auditlog_logtimestamp_position)
    and $auditlog_hostname_position < $auditlog_logtimestamp_position )
{
    ## If this condition is true, it means that the archive_gwcollagedb database copy of the
    ## auditlog table was established during an upgrade from a pre-7.1.0 release, where the auditlog
    ## table did not exist, to the 7.1.0 release, using a version of this migration script in the
    ## 7.1.0 release that implemented an early-development schema for the auditlog table instead
    ## of the final schema that was delivered in the 7.1.0 release.  That early schema just won't
    ## work properly during archiving.  It has the hostname field in the wrong position (that is,
    ## it is currently positioned early in the table, when it should be closer to the end), the
    ## hostname field has an undesired "not null" modifier attached, and the table has both of
    ## the hostgroupname and servicegroupname columns missing at the end of the table.  Now that
    ## we know that, we must make corresponding corrections.  The hostname field must be moved to
    ## immediately after the logtimestamp field, at the same time removing the "not null" modifier
    ## from the hostname field, and the hostgroupname and servicegroupname fields must be added to
    ## the end of the table.
    ##
    ## There won't be any data loss in simply deleting the existing hostname column, because
    ## archiving would have failed once it encountered this table, so no data would have ever
    ## been populated into this column in the archive_gwcollagedb-database table.  Data from the
    ## gwcollagedb-database table will be mirrored into the archive_gwcollagedb-database table once
    ## archiving is working again, so there should be no loss of information overall, except for any
    ## auditlog rows that got deleted from the gwcollagedb database while archiving was inoperative,
    ## if any (though that's unlikely).  But that, if it has occurred, we can't do anything about.
    ##
    print "\nModifying the auditlog table schema ...\n";

    # There's nothing idempotent about this first action, because we know it is absolutely necessary
    # under the conditions just tested.
    $dbh->do('ALTER TABLE auditlog DROP COLUMN hostname');

    # Next, process both hostname and servicedescription columns in one coordinated action.  The
    # hostname field will be added to the end of the table, and then the existing servicedescription
    # field will effectively be moved back to the end of the table.

    my %add_auditlog_hostname_column = (
	'auditlog' => {
	    columns => [
		[ 'hostname',           'character varying(254)', undef ],
		[ 'servicedescription', 'character varying(254)', undef ]
	    ]
	}
    );

    make_idempotent_column_changes( \%add_auditlog_hostname_column );

    # Next, we must add the hostgroupname column and the servicegroupname column in separate
    # actions, to avoid the attempt to copy data in later columns that don't yet exist that would
    # occur if we tried to do this in a single action.

    my %add_auditlog_hostgroupname_column    = ( 'auditlog' => { columns => [ [ 'hostgroupname',    'character varying(254)', undef ] ] } );
    my %add_auditlog_servicegroupname_column = ( 'auditlog' => { columns => [ [ 'servicegroupname', 'character varying(254)', undef ] ] } );

    make_idempotent_column_changes( \%add_auditlog_hostgroupname_column );
    make_idempotent_column_changes( \%add_auditlog_servicegroupname_column );
}

#-----------------------------------------------------------------------------
# (*) Add the "devicetemplateprofile" table with proper ownership, and
#     corresponding unique-ID sequence and its ownership, and the association
#     of the sequence with the unique-identifier column in the table.
#-----------------------------------------------------------------------------

print "\nAdding the devicetemplateprofile table, if needed ...\n";

idempotently_create_table(
    'devicetemplateprofile', 'collage',
    'CREATE TABLE IF NOT EXISTS devicetemplateprofile (
	devicetemplateprofileid integer                     NOT NULL,
	deviceidentification    character varying(128)      NOT NULL,
	devicedescription       character varying(254),
	cactihosttemplate       character varying(254),
	monarchhostprofile      character varying(254),
	changedtimestamp        timestamp without time zone NOT NULL
    )'
);

idempotently_create_sequence( 'devicetemplateprofile_devicetemplateprofileid_seq', 'collage', 'devicetemplateprofile.devicetemplateprofileid' );

#-----------------------------------------------------------------------------
# (*) Add the "hostblacklist" table with proper ownership, and corresponding
#     unique-ID sequence and its ownership, and the association of the
#     sequence with the unique-identifier column in the table.
#-----------------------------------------------------------------------------

print "\nAdding the hostblacklist table, if needed ...\n";

# For simplicity of comparison, we start by defining the table using the same
# structure as in the gwcollagedb database.  Then we will add other fields
# separately for use in the archive_gwcollagedb database.

idempotently_create_table(
    'hostblacklist', 'collage',
    'CREATE TABLE IF NOT EXISTS hostblacklist (
	hostblacklistid    integer      NOT NULL,
	hostname character varying(254) NOT NULL
    )'
);

idempotently_create_sequence( 'hostblacklist_hostblacklistid_seq', 'collage', 'hostblacklist.hostblacklistid' );

my %add_hostblacklist_startvalidtime_column = ( 'hostblacklist' => { columns => [ [ 'startvalidtime', 'timestamp without time zone', 'not null' ] ] } );
my %add_hostblacklist_endvalidtime_column   = ( 'hostblacklist' => { columns => [ [ 'endvalidtime',   'timestamp without time zone', undef ] ] } );

make_idempotent_column_changes( \%add_hostblacklist_startvalidtime_column );
make_idempotent_column_changes( \%add_hostblacklist_endvalidtime_column );

#-----------------------------------------------------------------------------
# (*) Add the "hostidentity" table with proper ownership.
#-----------------------------------------------------------------------------

print "\nAdding the hostidentity table, if needed ...\n";

# For simplicity of comparison, we start by defining the table using the same
# structure as in the gwcollagedb database.  Then we will add other fields
# separately for use in the archive_gwcollagedb database.

idempotently_create_table(
    'hostidentity', 'collage',
    'CREATE TABLE IF NOT EXISTS hostidentity (
	hostidentityid uuid                   NOT NULL,
	hostname       character varying(254) NOT NULL,
	hostid         integer                NULL
    )'
);

my %add_hostidentity_startvalidtime_column = ( 'hostidentity' => { columns => [ [ 'startvalidtime', 'timestamp without time zone', 'not null' ] ] } );
my %add_hostidentity_endvalidtime_column   = ( 'hostidentity' => { columns => [ [ 'endvalidtime',   'timestamp without time zone', undef ] ] } );

make_idempotent_column_changes( \%add_hostidentity_startvalidtime_column );
make_idempotent_column_changes( \%add_hostidentity_endvalidtime_column );

#-----------------------------------------------------------------------------
# (*) Add the "hostname" table with proper ownership.
#-----------------------------------------------------------------------------

print "\nAdding the hostname table, if needed ...\n";

# For simplicity of comparison, we start by defining the table using the same
# structure as in the gwcollagedb database.  Then we will add other fields
# separately for use in the archive_gwcollagedb database.

idempotently_create_table(
    'hostname', 'collage',
    'CREATE TABLE IF NOT EXISTS hostname (
	hostidentityid uuid                   NOT NULL,
	hostname       character varying(254) NOT NULL
    )'
);

my %add_hostname_startvalidtime_column = ( 'hostname' => { columns => [ [ 'startvalidtime', 'timestamp without time zone', 'not null' ] ] } );
my %add_hostname_endvalidtime_column   = ( 'hostname' => { columns => [ [ 'endvalidtime',   'timestamp without time zone', undef ] ] } );

make_idempotent_column_changes( \%add_hostname_startvalidtime_column );
make_idempotent_column_changes( \%add_hostname_endvalidtime_column );

#-----------------------------------------------------------------------------
# (*) Add constraints of various kinds (PRIMARY KEY, UNIQUE, FOREIGN KEY)
#     to the auditlog, category, devicetemplateprofile, hostblacklist,
#     hostidentity, and hostname tables.  The hostname-table index on its own
#     hostname field (in this database, a constraint rather than an index)
#     differs from the setup in the gwcollagedb database, because it does not
#     use the lower() function.  Imposing that extra restriction might make
#     sense in the upstream runtime database, but it is of little value in the
#     archive database and would complicate the archiving process.
#-----------------------------------------------------------------------------

print "\nAdding various constraints, if needed ...\n";

idempotently_add_primary_key( 'auditlog', 'auditlog_pkey', [qw(auditlogid)] );

idempotently_add_foreign_key_constraint(
    'category', 'category_ibfk_2', [qw(applicationtypeid)], 'applicationtype',
    [qw(applicationtypeid)], ON_UPDATE_RESTRICT, ON_DELETE_CASCADE
);

idempotently_add_unique_constraint( 'devicetemplateprofile', 'devicetemplateprofile_deviceidentification_key', [qw(deviceidentification)] );
idempotently_add_primary_key( 'devicetemplateprofile', 'devicetemplateprofile_pkey', [qw(devicetemplateprofileid)] );

idempotently_drop_constraint( 'hostblacklist', 'hostblacklist_hostname_key' );
idempotently_add_unique_constraint( 'hostblacklist', 'hostblacklist_hostname_startvalidtime_key', [qw(hostname startvalidtime)] );
idempotently_add_primary_key( 'hostblacklist', 'hostblacklist_pkey', [qw(hostblacklistid)] );

idempotently_drop_constraint( 'hostidentity', 'hostidentity_hostname_key' );
idempotently_add_unique_constraint( 'hostidentity', 'hostidentity_hostname_startvalidtime_key', [qw(hostname startvalidtime)] );
idempotently_add_primary_key( 'hostidentity', 'hostidentity_pkey', [qw(hostidentityid)] );
idempotently_add_foreign_key_constraint( 'hostidentity', 'hostidentity_ibfk_1', [qw(hostid)], 'host', [qw(hostid)], ON_UPDATE_RESTRICT,
    ON_DELETE_SET_NULL );

idempotently_add_unique_constraint( 'hostname', 'hostname_hostname_startvalidtime_key', [qw(hostname startvalidtime)] );
idempotently_add_foreign_key_constraint( 'hostname', 'hostname_ibfk_1', [qw(hostidentityid)], 'hostidentity', [qw(hostidentityid)],
    ON_UPDATE_RESTRICT, ON_DELETE_CASCADE );

#-----------------------------------------------------------------------------
# (*) Add ordinary and UNIQUE indexes to the category, hostidentity, and
#     hostname tables.
#-----------------------------------------------------------------------------

print "\nAdding various indexes, if needed ...\n";

idempotently_create_index( 'category', 'category_applicationtypeid', NON_UNIQUE, [qw (applicationtypeid)] );

idempotently_drop_index( 'hostidentity', 'hostidentity_hostid' );
idempotently_create_index( 'hostidentity', 'hostidentity_hostid_startvalidtime_idx', UNIQUE, [qw (hostid startvalidtime)] );

idempotently_drop_index( 'hostname', 'hostname_hostname' );
idempotently_create_index( 'hostname', 'hostname_hostidentityid', NON_UNIQUE, [qw (hostidentityid)] );

#-----------------------------------------------------------------------------
# (*) Make content insertions into the ApplicationType,
#     ApplicationEntityProperty, and MonitorStatus tables, which generally
#     ought to be handled during daily archiving by automatic row mirroring
#     rather than through changes during release migration.
#-----------------------------------------------------------------------------

# The applicationtype, applicationentityproperty, and monitorstatus tables are
# captured in the gwcollagedb database and mirrored to the archive_gwcollagedb
# database on a daily basis.  So there is no reason to make any direct content
# changes to these tables in this migration script.

#-----------------------------------------------------------------------------
# (*) Add the "categoryancestry" table with proper ownership.
#-----------------------------------------------------------------------------

print "\nAdding the categoryancestry table, if needed ...\n";

idempotently_create_table(
    'categoryancestry', 'collage',
    'CREATE TABLE IF NOT EXISTS categoryancestry (
	categoryid integer DEFAULT 0 NOT NULL,
	ancestorid integer DEFAULT 0 NOT NULL
    )'
);

#-----------------------------------------------------------------------------
# (*) Drop the category_name_key constraint from the category table.
#-----------------------------------------------------------------------------

print "\nDropping a constraint on the category table, if needed ...\n";

idempotently_drop_constraint( 'category', 'category_name_key' );

#-----------------------------------------------------------------------------
# (*) Add the category_name_entitytypeid_key constraint to the category table.
#-----------------------------------------------------------------------------

print "\nAdding a unique constraint to the category table, if needed ...\n";

idempotently_add_unique_constraint( 'category', 'category_name_entitytypeid_key', [qw(name entitytypeid)] );

#-----------------------------------------------------------------------------
# (*) Add a PRIMARY KEY constraint to the categoryancestry table.
#-----------------------------------------------------------------------------

print "\nAdding a primary key to the categoryancestry table, if needed ...\n";

idempotently_add_primary_key( 'categoryancestry', 'categoryancestry_pkey', [qw(categoryid ancestorid)] );

#-----------------------------------------------------------------------------
# (*) Add indexes to the categoryancestry and categoryhierarchy tables.
#-----------------------------------------------------------------------------

print "\nAdding category-related indexes, if needed ...\n";

idempotently_create_index( 'categoryancestry',  'categoryancestry_categoryid',  NON_UNIQUE, [qw (categoryid)] );
idempotently_create_index( 'categoryancestry',  'categoryancestry_ancestorid',  NON_UNIQUE, [qw (ancestorid)] );
idempotently_create_index( 'categoryhierarchy', 'categoryhierarchy_categoryid', NON_UNIQUE, [qw (categoryid)] );

#-----------------------------------------------------------------------------
# (*) Add FOREIGN KEY constraints to the category and categoryancestry tables.
#-----------------------------------------------------------------------------

print "\nAdding several foreign-key constraints, if needed ...\n";

idempotently_add_foreign_key_constraint( 'category', 'category_ibfk_1', [qw(entitytypeid)], 'entitytype', [qw(entitytypeid)],
    ON_UPDATE_RESTRICT, ON_DELETE_CASCADE );
idempotently_add_foreign_key_constraint( 'categoryancestry', 'categoryancestry_ibfk_1', [qw(ancestorid)], 'category', [qw(categoryid)],
    ON_UPDATE_RESTRICT, ON_DELETE_CASCADE );
idempotently_add_foreign_key_constraint( 'categoryancestry', 'categoryancestry_ibfk_2', [qw(categoryid)], 'category', [qw(categoryid)],
    ON_UPDATE_RESTRICT, ON_DELETE_CASCADE );

#-----------------------------------------------------------------------------
# (*) Make content insertions into the EntityType table, which generally ought
#     to be handled during daily archiving by automatic row mirroring rather
#     than through changes during release migration.
#-----------------------------------------------------------------------------

# The entitytype table is captured in the gwcollagedb database and mirrored to
# the archive_gwcollagedb database on a daily basis.  So there is no reason to
# make any direct content changes to this table in this migration script.

#-----------------------------------------------------------------------------
# (*) The following describes what happens in the gwcollagedb database, but is not needed
#     in the archive_gwcollagedb database because we are not collapsing hostnames using the
#     lower() function in the archive database as is done in the runtime database.  Also,
#     most or all of the cleanup done by the pg_migrate_gwcollagedb.sql script should now be
#     sidestepped by running the show-similar-hosts.sql and merge-similar-hosts.sql scripts
#     before any upgrade in which the pg_migrate_gwcollagedb.sql script actions listed here
#     would have any effect.  Those scripts are much more thorough than the migration script
#     has ever been.
#
#     The pg_migrate_gwcollagedb.sql script does this:
#
#         Merge "duplicate" hosts, preserving the most-uppercase-looking host (that is, in the
#         "host" table, delete similar rows differing only by hostname lettercase).  This choice
#         of preferring uppercase, if present, is made because we consider it to be likely that
#         the most-uppercase version is the "real" (in some sense) host name introduced into the
#         Collage database from feeders or agents that preserve case (e.g., CloudHub).  Never
#         mind that CloudHub often cannot see the actual real hostname, and can only see an
#         associated display name provided by the VM management software.
#
#         In a situation where you have multiple lettercase patterns used for the "same"
#         hostname, the lowercase version of the host was probably created by Monarch.  While
#         that sufficed and is definitely more readable, for some reason we have chosen not to
#         go with it as the standard formulation, perhaps because that would be the obvious
#         choice, and if a site has chosen not to use that form, they probably prefer not to.
#
#         Update the "hostgroupcollection" table accordingly by adding the most-uppercase-looking
#         host to any hostgroup to which a similar [to-be-deleted] host belonged.  At least in
#         the gwcollagedb database, also adjust all historical rows in the "logmessage" table to
#         reflect the collapsing of multiple forms of hostname into a single canonical form, and
#         do the same in the "servicestatus" table.
#
#     That set of steps misses some stuff that needs to be done.  But the merge-similar-hosts.sql
#     does a thorough job, and if it's run before the migration, the pg_migrate_gwcollagedb.sql
#     script will encounter nothing to adjust.
#
#     The one area that does affect the archive_gwcollagedb database is that some of the
#     actions taken by the merge-similar-hosts.sql script can cause the gwcollagedb database
#     to be out of sync with the archive_gwcollagedb database.  Those residual issues are
#     handled by the conflicting_archive_service_rows.pl script, which is to be run after an
#     upgrade where the show-similar-hosts.sql and merge-similar-hosts.sql scripts needed to
#     be run before the upgrade.  The conflicting_archive_service_rows.pl script will make a
#     few adjustments in the archive_gwcollagedb database to bring the two databases back into
#     synchrony so daily archiving won't run into any index collisions.
#-----------------------------------------------------------------------------

# Because we don't apply case-insensitivity restrictions on the archive_gwcollagedb tables,
# there is no reason to uniqueify any legacy data here.  In fact, that would just disturb
# the historical record for no particularly good reason.  Instead, it will be up to any
# program that reports out of the archive_gwcollagedb database to recognize that on a
# historical basis, different lettercase constructions may have been used for the "same"
# host, and to collapse the discovered data if desired for such hosts.

#-----------------------------------------------------------------------------
# (*) Drop the old unique-hostname constraint on the "host" table that
#     required case-sensitive uniqueness.  (This is not actually required
#     or even necessarily useful in the archive database, because we are
#     not enforcing case-insensitivity here as is now done in the runtime
#     database.)
#-----------------------------------------------------------------------------

print "\nAdjusting the schema of the host table, if needed ...\n";

# A fresh install of 7.1.0 fails to attach the startvalidtime and endvalidtime fields to the
# archive_gwcollagedb copy of the host table, because trouble dropping the host_hostname_key
# constraint causes the Archive_GWCollageDB_extensions.sql script to fail on that point.  (The
# script does not die there; it apparently goes on to execute all the other intended actions,
# because we see those changes already in the archive_gwcollagedb database.)  So we need to
# re-do all parts of that one failing statement here, if necessary, not just the changes
# documented in the pg_migrate_gwcollagedb.sql script.
#
# Here are the lines from the 7.1.0 copy of the Archive_GWCollageDB_extensions.sql script:
#
#     ALTER TABLE "public"."host"
#         ADD COLUMN startvalidtime timestamp without time zone NOT NULL,
#         ADD COLUMN endvalidtime   timestamp without time zone,
#         DROP CONSTRAINT "host_hostname_key",
#         ADD UNIQUE (hostname, startvalidtime);
#
# The UNIQUE constraint will be handled in the next item, not here.

my %add_host_startvalidtime_column = ( 'host' => { columns => [ [ 'startvalidtime', 'timestamp without time zone', 'not null' ] ] } );
my %add_host_endvalidtime_column   = ( 'host' => { columns => [ [ 'endvalidtime',   'timestamp without time zone', undef ] ] } );

make_idempotent_column_changes( \%add_host_startvalidtime_column );
make_idempotent_column_changes( \%add_host_endvalidtime_column );

idempotently_drop_constraint( 'host', 'host_hostname_key' );

#-----------------------------------------------------------------------------
# (*) Add a unique index for the "host" table to prevent case-insensitive
#     duplicate hosts from appearing in the future.  (Again, we are not
#     enforcing case-insensitivity in the archive database, so there is no
#     reason to exactly mirror here the setup in the runtime database.)
#-----------------------------------------------------------------------------

print "\nAdding a hostname constraint, if needed ...\n";

idempotently_add_unique_constraint( 'host', 'host_hostname_startvalidtime_key', [qw(hostname startvalidtime)] );

# Proactively drop an index we might have inherited from gwcollagedb.  This should not be
# present in the 7.1.1 release, but there might be some sort of upgrade path that would
# result in our having such an index that depended on the use of lower(hostname).  And as
# such, it provides cheap insurance to have this executed here as well in future upgrades.
idempotently_drop_index( 'host', 'host_hostname' );

#-----------------------------------------------------------------------------
# (*) Add an index to the servicestatus table to make lookups on the
#     servicedescription column more efficient.
#-----------------------------------------------------------------------------

# It suffices to just add a simple non-unique index on the one field.  We don't bother
# with an index on (servicedescription, startvalidtime) instead because that would also
# not be unique and anyone querying this table would probably not have a startvalidtime
# value in hand to distinguish that level of query, anyway.  We do already have a unique
# index on the full set of (hostid, servicedescription, startvalidtime) fields.

print "\nAdding a service-name index, if needed ...\n";

idempotently_create_index( 'servicestatus', 'servicestatus_servicedescription', NON_UNIQUE, [qw (servicedescription)] );

#-----------------------------------------------------------------------------
# (*) Add the servicestatus.applicationhostname field to the end of that
#     table.  This change requires the usual careful adjustments, because
#     this is one of the tables that contains the extra startvalidtime
#     and endvalidtime columns in the archive_gwcollagedb database.  But
#     worse, an upgrade to the 7.1.0 release damaged this table by putting
#     the new column at the end of the table, after the startvalidtime and
#     endvalidtime columns.  So we first need to undo that damage if we
#     find it.
#-----------------------------------------------------------------------------

# FORENSIC DATA FOR SPECIAL-CASE FIX (below):  Some upgrade paths prior to 7.1.1 left the
# servicestatus table in the archive_gwcollagedb database with an incorrect schema.  We
# need accurate information on what went wrong in order to devise a robust fix.

# After 7.0.2 fresh install:
# in         gwcollagedb:  last servicestatus column is agentid
# in archive_gwcollagedb:  last servicestatus columns are agentid, startvalidtime, endvalidtime

# After 7.0.2 fresh install + upgrade to 7.1.0:
#     Error: There has been an error.
#     Upgrades from GWMEE 7.0.2 SP02 or newer are supported. Please update your
#     install to GWMEE 7.0.2 SP02 before running the GWMEE 7.1.0-br389-gw2833
#     installer again.

# After 7.0.2 fresh install + SP01 + upgrade to 7.1.0:
#     Error: There has been an error.
#     Upgrades from GWMEE 7.0.2 SP02 or newer are supported. Please update your
#     install to GWMEE 7.0.2 SP02 before running the GWMEE 7.1.0-br389-gw2833
#     installer again.

# After 7.0.2 fresh install + SP02:
# in         gwcollagedb:  last servicestatus column is agentid
# in archive_gwcollagedb:  last servicestatus columns are agentid, startvalidtime, endvalidtime
#
# After 7.0.2 fresh install + SP02 + upgrade to 7.1.0:
# in         gwcollagedb:  last servicestatus columns are agentid, applicationhostname
#
#     Indexes:
#         "servicestatus_hostid_servicedescription_key" UNIQUE CONSTRAINT, btree (hostid, servicedescription)
#         "servicestatus_servicedescription" btree (servicedescription)
#
# in archive_gwcollagedb:  last servicestatus columns are agentid, startvalidtime, endvalidtime, applicationhostname
#
#     Indexes:
#         "servicestatus_hostid_servicedescription_startvalidtime_key" UNIQUE CONSTRAINT, btree (hostid, servicedescription, startvalidtime)
#
# index differences after further upgrade to 7.1.1:
# gwcollagedb:          "servicestatus_hostid_servicedescription_key" UNIQUE CONSTRAINT, btree (hostid, servicedescription)
# archive_gwcollagedb:  "servicestatus_hostid_servicedescription_startvalidtime_key" UNIQUE CONSTRAINT, btree (hostid, servicedescription, startvalidtime)

# After 7.0.2 fresh install + SP02 + SP03:
# in         gwcollagedb:  last servicestatus column is agentid
# in archive_gwcollagedb:  last servicestatus columns are agentid, startvalidtime, endvalidtime
#
# After 7.0.2 fresh install + SP02 + SP03 + upgrade to 7.1.0:
# in         gwcollagedb:  last servicestatus columns are agentid, applicationhostname
# in archive_gwcollagedb:  last servicestatus columns are agentid, startvalidtime, endvalidtime, applicationhostname
# Note that the gwcollagedb database has no servicestatus_servicedescription index on the servicestatus
# table at this point.  That will need to be repaired in the pg_migrate_gwcollagedb.sql script, not here.
#
# After 7.0.2 fresh install + SP02 + SP03 + upgrade to 7.1.0 + upgrade to 7.1.1-br413-gw3073
# (including executing the fixes below):
# in         gwcollagedb:  last servicestatus columns are agentid, applicationhostname
# in archive_gwcollagedb:  last servicestatus columns are agentid, applicationhostname, startvalidtime, endvalidtime

# After fresh 7.1.0 install:
# in         gwcollagedb:  last servicestatus columns are agentid, applicationhostname
# in archive_gwcollagedb:  last servicestatus columns are agentid, applicationhostname, startvalidtime, endvalidtime
#
# After fresh 7.1.0 install + upgrade to 7.1.1-br413-gw3073 (including executing the fixes below):
# in         gwcollagedb:  last servicestatus columns are agentid, applicationhostname
# in archive_gwcollagedb:  last servicestatus columns are agentid, applicationhostname, startvalidtime, endvalidtime

#- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

# SPECIAL-CASE FIX:  As noted above, an upgrade to the 7.1.0 release from some previous
# release corrupts the schema of the servicestatus table in the archive_gwcollagedb
# database.  If we find that is the case at the present time, we will now need to make
# corrections, even though these separate adjustments do not appear explicitly in the
# pg_migrate_gwcollagedb.sql script from which most of the other actions in this script
# are derived.

print "\nDropping servicestatus.applicationhostname column, if needed ...\n";

my $servicestatus_applicationhostname_position = table_column_position( 'servicestatus', 'applicationhostname' );
my $servicestatus_startvalidtime_position      = table_column_position( 'servicestatus', 'startvalidtime' );

if (    defined($servicestatus_applicationhostname_position)
    and defined($servicestatus_startvalidtime_position)
    and $servicestatus_applicationhostname_position > $servicestatus_startvalidtime_position )
{
    ## Unlike most everything else in this script, there's nothing idempotent about this column
    ## deletion; it fixes a damaged table due to incorrect processing during a previous upgrade.
    ## We need to forcibly change this situation if it exists, by removing the badly-placed column
    ## and then allowing the following code to add it back in the right place.  There won't be any
    ## data loss in simply deleting the existing column, because archiving would have failed once
    ## it encountered this table, so no data would have ever been populated into this column in
    ## the archive_gwcollagedb-database table.  Data from the gwcollagedb-database table will be
    ## mirrored into the archive_gwcollagedb-database table once archiving is working again, so
    ## there should be no loss of information overall, except for any servicestatus rows that got
    ## deleted from the gwcollagedb database while archiving was inoperative.  That, we can't do
    ## anything about.
    ##
    print "... dropping an existing mispositioned servicestatus.applicationhostname column.\n";
    $dbh->do('ALTER TABLE servicestatus DROP COLUMN applicationhostname');
}

#- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

print "\nAdding servicestatus.applicationhostname column, if needed ...\n";

my %add_servicestatus_applicationhostname_column = (
    'servicestatus' => {
	columns => [
	    [ 'applicationhostname', 'character varying(254)',      undef ],
	    [ 'startvalidtime',      'timestamp without time zone', 'not null' ],
	    [ 'endvalidtime',        'timestamp without time zone', undef ]
	],
	unique_constraints =>
	  { 'servicestatus_hostid_servicedescription_startvalidtime_key' => [qw (hostid servicedescription startvalidtime)] }
    }
);

make_idempotent_column_changes(\%add_servicestatus_applicationhostname_column);

#-----------------------------------------------------------------------------
# (*) Make more content insertions into the ApplicationType table, which
#     generally ought to be handled during daily archiving by automatic row
#     mirroring rather than through changes during release migration.
#-----------------------------------------------------------------------------

# The applicationtype table is captured in the gwcollagedb database and mirrored
# to the archive_gwcollagedb database on a daily basis.  So there is no reason
# to make any direct content changes to this table in this migration script.

##############################################################################
# Schema and data changes for the transition to GWMEE 7.1.1
##############################################################################

# (*) The 7.1.1 release does not modify any schema elements.  It does potentially modify the
#     ApplicationType table content slightly, implementing a mistakenly-omitted correction
#     that ought to have been made in either the 7.0.2 fresh-install data or the 7.1.0 upgrade
#     actions.  Since it's too late to carry out the changes there, we must do so now in the
#     7.1.1 release for the gwcollagedb database.  See GWMON-12687 and GWMON-12689 for more
#     information.  This change will be reflected in the archive_gwcollagedb database by
#     automatic row mirroring, so there is nothing to do here in the migration script.
#
# (*) There are some other content changes to the ApplicationType and ConsolidationCriteria
#     tables in the gwcollagedb database.  The ApplicationType changes will then be reflected
#     in the archive_gwcollagedb database via ordinary daily archiving.  We do not currently
#     archive the ConsolidationCriteria table, so there is no issue there.

##############################################################################
# Schema and data changes for the transition to GWMEE 7.2.0
##############################################################################

# (*) "timestamp without time zone" fields are converted to "timestamp with time zone"
#     fields, throughout the database.  We make these changes even to tables that do not
#     get archived, just to keep the schema in the archive_gwcollagedb database in sync
#     with the schema in the gwcollagedb database.

print "\nConverting timestamp columns to support timezones, if needed ...\n";

# There's nothing to explicitly declare as a data conversion if we simply add a
# timezone to an existing timestamp field.
my $add_timezone_to_timestamp = undef;

#                                                                                                                                new
#     table                    column                 old type, old max len                 new type, new max len              default  conversion
#     -----------------------  ---------------------  ------------------------------------  ---------------------------------  -------  --------------------------
my @convert_to_7_2_0_column_types = (
    [ 'logmessage',            'firstinsertdate',     'timestamp without time zone', undef, 'timestamp with time zone', undef, undef,   $add_timezone_to_timestamp ],
    [ 'logmessage',            'lastinsertdate',      'timestamp without time zone', undef, 'timestamp with time zone', undef, undef,   $add_timezone_to_timestamp ],
    [ 'logmessage',            'reportdate',          'timestamp without time zone', undef, 'timestamp with time zone', undef, undef,   $add_timezone_to_timestamp ],
    [ 'auditlog',              'logtimestamp',        'timestamp without time zone', undef, 'timestamp with time zone', undef, undef,   $add_timezone_to_timestamp ],
    [ 'devicetemplateprofile', 'changedtimestamp',    'timestamp without time zone', undef, 'timestamp with time zone', undef, undef,   $add_timezone_to_timestamp ],
    [ 'entityproperty',        'valuedate',           'timestamp without time zone', undef, 'timestamp with time zone', undef, undef,   $add_timezone_to_timestamp ],
    [ 'entityproperty',        'lasteditedon',        'timestamp without time zone', undef, 'timestamp with time zone', undef, 'now()', $add_timezone_to_timestamp ],
    [ 'entityproperty',        'createdon',           'timestamp without time zone', undef, 'timestamp with time zone', undef, undef,   $add_timezone_to_timestamp ],
    [ 'hoststatus',            'lastchecktime',       'timestamp without time zone', undef, 'timestamp with time zone', undef, undef,   $add_timezone_to_timestamp ],
    [ 'hoststatus',            'nextchecktime',       'timestamp without time zone', undef, 'timestamp with time zone', undef, undef,   $add_timezone_to_timestamp ],
    [ 'hoststatusproperty',    'valuedate',           'timestamp without time zone', undef, 'timestamp with time zone', undef, undef,   $add_timezone_to_timestamp ],
    [ 'hoststatusproperty',    'lasteditedon',        'timestamp without time zone', undef, 'timestamp with time zone', undef, 'now()', $add_timezone_to_timestamp ],
    [ 'hoststatusproperty',    'createdon',           'timestamp without time zone', undef, 'timestamp with time zone', undef, undef,   $add_timezone_to_timestamp ],
    [ 'logmessageproperty',    'valuedate',           'timestamp without time zone', undef, 'timestamp with time zone', undef, undef,   $add_timezone_to_timestamp ],
    [ 'logmessageproperty',    'lasteditedon',        'timestamp without time zone', undef, 'timestamp with time zone', undef, 'now()', $add_timezone_to_timestamp ],
    [ 'logmessageproperty',    'createdon',           'timestamp without time zone', undef, 'timestamp with time zone', undef, undef,   $add_timezone_to_timestamp ],
    [ 'logperformancedata',    'lastchecktime',       'timestamp without time zone', undef, 'timestamp with time zone', undef, undef,   $add_timezone_to_timestamp ],
    [ 'plugin',                'lastupdatetimestamp', 'timestamp without time zone', undef, 'timestamp with time zone', undef, 'now()', $add_timezone_to_timestamp ],
    [ 'servicestatus',         'lastchecktime',       'timestamp without time zone', undef, 'timestamp with time zone', undef, undef,   $add_timezone_to_timestamp ],
    [ 'servicestatus',         'nextchecktime',       'timestamp without time zone', undef, 'timestamp with time zone', undef, undef,   $add_timezone_to_timestamp ],
    [ 'servicestatus',         'laststatechange',     'timestamp without time zone', undef, 'timestamp with time zone', undef, undef,   $add_timezone_to_timestamp ],
    [ 'servicestatusproperty', 'valuedate',           'timestamp without time zone', undef, 'timestamp with time zone', undef, undef,   $add_timezone_to_timestamp ],
    [ 'servicestatusproperty', 'lasteditedon',        'timestamp without time zone', undef, 'timestamp with time zone', undef, 'now()', $add_timezone_to_timestamp ],
    [ 'servicestatusproperty', 'createdon',           'timestamp without time zone', undef, 'timestamp with time zone', undef, undef,   $add_timezone_to_timestamp ],
);

convert_column_types( \@convert_to_7_2_0_column_types );

##############################################################################
# Schema and data changes for the transition to GWMEE 7.2.1
##############################################################################

# (*) Add the "comment" table and associated objects (sequence, indexes, constraints).

print "\nAdding the comment table, if needed ...\n";

idempotently_create_table(
    'comment', 'collage',
    'CREATE TABLE IF NOT EXISTS comment (
	commentid       integer                  NOT NULL,
	notes           text                     NOT NULL,
	author          character varying(254)   NOT NULL,
	createdon       timestamp with time zone NOT NULL,
	hostid          integer,
	servicestatusid integer 
    )'
);

idempotently_create_sequence( 'comment_commentid_seq', 'collage', 'comment.commentid' );

print "\nAdding a primary key to the comment table, if needed ...\n";

idempotently_add_primary_key( 'comment', 'comment_pkey', [qw(commentid)] );

print "\nAdding indexes, if needed ...\n";

idempotently_create_index( 'comment', 'comment_hostid',          NON_UNIQUE, [qw(hostid)] );
idempotently_create_index( 'comment', 'comment_servicestatusid', NON_UNIQUE, [qw(servicestatusid)] );

print "\nAdding foreign-key constraints, if needed ...\n";

idempotently_add_foreign_key_constraint( 'comment', 'comment_hostid_fkey', [qw(hostid)], 'host', [qw(hostid)], ON_UPDATE_NO_ACTION,
    ON_DELETE_CASCADE );
idempotently_add_foreign_key_constraint( 'comment', 'comment_servicestatusid_fkey',
    [qw(servicestatusid)], 'servicestatus', [qw(servicestatusid)], ON_UPDATE_NO_ACTION, ON_DELETE_CASCADE );

##############################################################################
# Committing Changes
##############################################################################

print "\nUpdating the CurrentSchemaVersion value to the current release level ...\n";

# After everything else is done, update our proxy flag for all the other changes made above.
# This field is not automatically transferred from gwcollagedb to archive_gwcollagedb during
# daily log-archive operations, so we must update it here ourselves.  Also update the field
# that records the timestamp at which the schema has been updated.
$dbh->do( "UPDATE schemainfo SET value = '$CurrentSchemaVersion' WHERE name = 'CurrentSchemaVersion'" );
my $schema_updated_array_ref = $dbh->selectrow_arrayref( "select value from schemainfo where name = 'SchemaUpdated'" );
if (not defined $schema_updated_array_ref) {
    $dbh->do( "INSERT INTO schemainfo (name, value) VALUES ('SchemaUpdated', date_trunc('second',now()))" );
}
else {
    $dbh->do( "UPDATE schemainfo SET value = date_trunc('second',now()) WHERE name = 'SchemaUpdated'" );
}

print "\nCommitting all changes ...\n";

# Commit all previous changes.  Note that some earlier commands may have performed
# implicit commit operations, which is why the very first change we made above was
# to modify the archive_gwcollagedb CurrentSchemaVersion at the start of the script
# to something that would show that we were only partially done migrating the database
# schema and content.  There is not much of anything we can do about those implicit
# commits; there is no good way to roll back automatically if some part of the
# operations that perform such implicit commits should fail.  If we find a negative
# CurrentSchemaVersion version after running this script, we know the migration is
# not completely done.
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
	## commit) it should leave the CurrentSchemaVersion in a state (that is, having
	## a negative value) where we can later see that the full migration did not
	## complete, so there is no confusion as to whether the database is in a
	## usable state.
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
	    print "WARNING:  The archive_gwcollagedb database has probably been left in an inconsistent, unusable state.\n";
	}
	else {
	    eval {
		my $sqlstmt = "select value from schemainfo where name = 'CurrentSchemaVersion'";
		my ($final_CurrentSchemaVersion) = $dbh->selectrow_array($sqlstmt);

		# If the migration had completed successfully, the CurrentSchemaVersion value
		# would have been updated to be the target archive_gwcollagedb version.
		# Conversely, if the rollback got us all the way back to where we were when we
		# started, we ought to have a standard useable copy of the CurrentSchemaVersion
		# value, even if it is not the current target release.  If not, which is to
		# say either that we didn't start with a fully usable database, or that some
		# implicit commit along the way destroyed our ability to roll back to where we
		# were when we started, the transient value will remain as an indicator to later
		# users of the database that the schema and/or content is in bad shape.  We may
		# as well report that condition now to the user, to avoid confusion.
		if ( !defined($final_CurrentSchemaVersion) or length($final_CurrentSchemaVersion) == 0 or $final_CurrentSchemaVersion =~ /^-/ ) {
		    print "FATAL:  The archive_gwcollagedb database has been left in an inconsistent, unusable state.\n";
		}
	    };
	    if ($@) {
		my $errstr = $@;
		print "\nERROR:  Cannot verify the final archive_gwcollagedb version",
		  ( defined($errstr) ? ( ":\n" . $errstr ) : '; no error detail is available.' ), "\n";
	    }
	}
	$dbh->disconnect();
    }
    if (!$all_is_done) {
	print "\n";
	print "================================================================================\n";
	print "    WARNING:  archive_gwcollagedb database migration did not fully complete!\n";
	print "================================================================================\n";
	print "\n";
	exit (1);
    }
}

print "\nUpdate of the archive_gwcollagedb database is complete.\n\n";

