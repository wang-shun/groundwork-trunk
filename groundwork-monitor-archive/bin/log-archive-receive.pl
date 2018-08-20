#!/usr/local/groundwork/perl/bin/perl -w --

# log-archive-receive.pl

# Copyright (c) 2013-2016 GroundWork, Inc. (www.gwos.com).  All rights reserved.
# Use of this software is subject to commercial license terms.

# This script takes a directory containing a set of runtime-database table-dump
# files, and injects them into an archive database.  Injection consists of
# updating existing rows, and inserting new rows.  No existing rows are ever
# deleted from the archive database (which can be somewhat problematic for
# generating reports from the archive database, for certain object-association
# tables like hostgroupcollection, where the set of valid associations might
# change over time).  This script also manages the eventual deletion of the
# files it reads (though they are generally kept around for some period of time
# after they are used for injection, to potentially allow manual recovery in
# exceptional conditions).
#
# The config file for this script contains extensive comments on the settings
# for adjusting the actions of this script at a customer site.
#
# Normally, this script is not run in a standalone fashion.  Rather, this
# receiving script is usually invoked directly by the companion sending script,
# immediately after the sending script has captured a set of data from the
# runtime database into files, and made those files available to the receiving
# script.  Just that one set of files will be processed in a single run of the
# receiving script.

# Note that we must implement exceptional condition handling in these scripts
# (pun intended).  ALL exception conditions MUST be detected and dealt with
# in a clean way, so we are guaranteed that the data we are moving around is
# safely migrated before we go destroying it in either the original runtime
# database or in the source or target files.  THERE MUST BE NO EXCEPTIONS TO
# THIS RULE!  Any place we can possibly sense a problem, we have to check.

# TO DO:
#
# (*) FIX MAJOR:  Perhaps use ACCESS EXCLUSIVE locking instead of just EXCLUSIVE locking;
#     but for that to make sense from the standpoint of some other concurrently executing
#     script, we would need to lock all the tables we will modify before we lock any of
#     them, not just lock them one at a time as we inject data into that single permanent
#     table.
# (*) FIX MINOR:  If we do use an ACCESS EXCLUSIVE lock, then we should probably advise
#     all consumers of the archive-database data to use a LOCK NOWAIT to test to see
#     whether they can proceed with reading the database before trying to export or
#     otherwise process the data.  Of course, that would only make sense in the context
#     of a transaction that includes all the rest of the reading operations the other
#     process will want to perform, since this process might otherwise lock the tables
#     right after your LOCK NOWAIT says you have the green light.  Also note that some
#     high-level reporting contexts won't give you that level of locking control, so
#     there may be no choice for the reader than to wait for this writer to complete.
# (*) FIX MAJOR:  Revisit the decision to run a COMMIT on every individual table as its
#     data is copied from a temporary table to the permanent table, as opposed to using
#     just one large COMMIT or ROLLBACK for data changes across all tables.
# (*) FIX MAJOR:  Think about whether there are any conditions that might arise that
#     could disable archiving entirely, if some archiving cycle was interrupted partway
#     through for any reason, and data from only a partial set of files got committed.
#     Might that cause any problems when processing future copies of later files in the
#     set, that certain rows might no longer be present but might need to be?  Or am I
#     just being paranoid, and this scenario can never occur?  If it could, that would
#     be a powerful argument for a single global COMMIT per archiving cycle.
# (*) FIX LATER:  Consider refactoring the send and receive scripts, since some
#     of their code is shared by simple copying.  Generalize routines as necessary,
#     and stuff them into a package that can be referenced from either script.
# (*) FIX MINOR:  We should probably lock the receive state file while this script is
#     operating, to prevent multiple copies from operating concurrently.  A failure
#     to obtain the lock would cause the script to abort.

use strict;

use DBI;
use Fcntl;
use Getopt::Std;
use Config;
use Time::Local qw(timelocal timelocal_nocheck);
use Time::HiRes;
use Socket;
use POSIX qw();

use Data::Dumper;
$Data::Dumper::Indent   = 1;
$Data::Dumper::Sortkeys = 1;

use TypedConfig;

use GW::Logger;
use GW::Foundation;

# ================================
# Package Parameters
# ================================

my $PROGNAME       = "log-archive-receive.pl";
my $VERSION        = "0.0.11";
my $COPYRIGHT_YEAR = "2016";

my $default_config_file        = '/usr/local/groundwork/config/log-archive-receive.conf';
my $log_archive_bin            = '/usr/local/groundwork/core/archive/bin';
my $gwservices_control_program = "$log_archive_bin/control_archive_gwservices";

# ================================
# Command-Line Parameters
# ================================

# In theory, these parameter settings could be overridden by command-line arguments.
# In practice, we don't currently support any such arguments; this script uses only
# a fixed set of arguments.

my $config_file            = $default_config_file;
my $debug_config           = 0;                      # if set, spill out certain data about config-file processing to STDOUT
my $show_help              = 0;
my $show_version           = 0;
my $run_interactively      = 0;
my $reflect_log_to_stdout  = 0;
my $target_extraction_path = undef;

# ================================
# Configuration Parameters
# ================================

# Parameters in the config file.

my $enable_processing = undef;

# Possible $debug_level values:
# 0 = no info of any kind printed, except for startup/shutdown messages and major errors
# 1 = print just error info and summary statistical data
# 2 = also print basic debug info
# 3 = print detailed debug info
# Initial level, to be overwritten by a value from the config file:
my $debug_level = 1;

my $logfile                = undef;
my $max_logfile_size       = undef;    # log rotate is handled externally, not here
my $max_logfiles_to_retain = undef;    # log rotate is handled externally, not here

my $archive_dbtype = undef;
my $archive_dbhost = undef;
my $archive_dbport = undef;
my $archive_dbname = undef;
my $archive_dbuser = undef;
my $archive_dbpass = undef;

my $target_script_machine    = undef;
my $target_script_ip_address = undef;

my $manage_archive_gwservices            = undef;
my $archive_gwservices_machine           = undef;
my $archive_gwservices_machine_is_remote = undef;

my $table_locking_timeout_seconds = undef;

my @primary_table_attributes   = ();
my @secondary_table_attributes = ();
my @tertiary_table_attributes  = ();
my @primary_tables             = ();
my @secondary_tables           = ();
my @tertiary_tables            = ();
my %all_table_row_type         = ();
my %all_table_key_fields       = ();
my %is_a_secondary_table       = ();
my @message_data_tables        = ();
my @performance_data_tables    = ();

my $dumpfile_format                   = undef;
my $target_dumpfile_retention_days    = undef;
my $log_archive_target_data_directory = undef;
my $log_archive_target_state_file     = undef;

my $foundation_host = '';
my $foundation_port = 4913;

# Parameters derived from the config-file parameters.

# These values will be replaced once $debug_level is itself replaced by a value from
# the config file.  But we want these values to be operational even before the config
# file is read, in case we need to debug early operation of this script.
my $debug_minimal = ( $debug_level >= 1 );
my $debug_basic   = ( $debug_level >= 2 );
my $debug_maximal = ( $debug_level >= 3 );

my $monitor_server_hostname   = undef;
my $monitor_server_ip_address = undef;

my $table_locking_timeout_ms = undef;

# Locally defined parameters, that might someday move to the config file.

# Socket timeout (in seconds), to address GWMON-7407.  Typical value
# is 60.  Set to 0 to disable.
#
# This timeout is here only for use in emergencies, when Foundation
# has completely frozen up and is no longer reading (will never read)
# a socket we have open.  We don't want to set this value so low that
# it will interfere with normal communication, even given the fact that
# Foundation may wait a rather long time between sips from this straw
# as it processes a large bundle of messages that we sent it, or is
# otherwise busy and just cannot get back around to reading the socket
# in a reasonably short time.
my $socket_send_timeout = 60;

# This is the actual SO_SNDBUF value, as set by setsockopt().  This is
# therefore the actual size of the data buffer available for writing,
# irrespective of additional kernel bookkeeping overhead.  This will
# have no effect without the companion as-yet-undocumented patch to
# IO::Socket::INET.  Set this to 0 to use the system default socket send
# buffer size.  A typical value to set here is 262144.  (Note that the
# value specified here is likely to be limited to something like 131071
# by the sysctl net.core.wmem_max parameter.)
#
# This value is not currently used by the GW::Foundation package we call,
# so we just let it default for future compatibility.
my $send_buffer_size = 0;

# ================================
# State-File Parameters
# ================================

my $last_previous_successful_run_timestamp = undef;

# ================================
# Working Variables
# ================================

my $dbh   = undef;
my $sth   = undef;
my $query = undef;

my $script_start_time            = undef;
my $script_init_end_time         = undef;
my $script_injection_end_time    = undef;
my $script_delete_files_end_time = undef;

my $run_start_time      = undef;
my $run_start_timestamp = undef;

my $archive_run_time      = undef;
my $archive_run_timestamp = undef;
my $archive_end_timestamp = undef;

my $earliest_file_retention_time      = undef;
my $earliest_file_retention_timestamp = undef;

my %matched_file           = ();
my %matched_dump_timestamp = ();

my $total_tables_injected   = 0;
my $total_rows_injected     = 0;
my $total_rows_copied       = 0;
my $total_rows_obsoleted    = 0;
my $total_rows_updated      = 0;
my $total_rows_inserted     = 0;
my $total_rows_reincarnated = 0;
my $total_files_deleted     = 0;

my $total_copy_time        = 0;
my $total_lock_time        = 0;
my $total_obsolete_time    = 0;
my $total_update_time      = 0;
my $total_insert_time      = 0;
my $total_reincarnate_time = 0;

my %rows_injected     = ();
my %rows_obsoleted    = ();
my %rows_updated      = ();
my %rows_inserted     = ();
my %rows_reincarnated = ();

my $message_rows_injected  = undef;
my $message_rows_inserted  = undef;
my $perfdata_rows_injected = undef;
my $perfdata_rows_inserted = undef;

my $cycle_outcome = undef;

# These variables really ought to just be local to the log_archiving_statistics() routine,
# except that we want a few of them to be accessible to the send_outcome_to_foundation()
# routine so the message it sends is more informative.
my $init_time              = undef;
my $injection_time         = undef;
my $delete_files_time      = undef;
my $total_time             = undef;
my $init_timestamp         = undef;
my $injection_timestamp    = undef;
my $copy_timestamp         = undef;
my $lock_timestamp         = undef;
my $obsolete_timestamp     = undef;
my $update_timestamp       = undef;
my $insert_timestamp       = undef;
my $reincarnate_timestamp  = undef;
my $delete_files_timestamp = undef;
my $total_timestamp        = undef;
my $row_injection_speed    = undef;
my $row_copy_speed         = undef;
my $row_obsolete_speed     = undef;
my $row_update_speed       = undef;
my $row_insert_speed       = undef;
my $row_reincarnate_speed  = undef;

use constant HOURS_PER_DAY      => 24;
use constant MINUTES_PER_HOUR   => 60;
use constant SECONDS_PER_MINUTE => 60;
use constant MINUTES_PER_DAY    => MINUTES_PER_HOUR * HOURS_PER_DAY;
use constant SECONDS_PER_DAY    => SECONDS_PER_MINUTE * MINUTES_PER_DAY;
use constant SECONDS_PER_HOUR   => SECONDS_PER_MINUTE * MINUTES_PER_HOUR;

use constant ERROR_STATUS    => 0;
use constant STOP_STATUS     => 1;
use constant RESTART_STATUS  => 2;
use constant CONTINUE_STATUS => 3;

# ================================================================
# Program.
# ================================================================

exit ((main() == ERROR_STATUS) ? 1 : 0);

# ================================================================
# Supporting subroutines.
# ================================================================

sub main {
    capture_timing(\$script_start_time);

    $run_start_time = POSIX::floor($script_start_time);
    my ( $second, $minute, $hour, $month_day, $month, $year, $week_day, $year_day, $is_dst ) = localtime($run_start_time);
    $run_start_timestamp = sprintf "%04d-%02d-%02d %02d:%02d:%02d", $year + 1900, $month + 1, $month_day, $hour, $minute, $second;

    # If this script fails, and we have successfully made it past reading the config file (so we know how to send
    # messages to Foundation), the $status_message will be sent to Foundation, and show up in the Event Console.
    # Thus there is no point in defining $status_message in the code below until we have made it past that point.
    my $status_message = '';
    $cycle_outcome = 1;

    # Safety first, since we fork some other processes as we execute.
    $ENV{PATH} = '/bin:/sbin:/usr/bin:/usr/sbin';

    if (open (STDERR, '>>&STDOUT')) {
	## Apparently, appending STDERR to the STDOUT stream isn't by itself enough
	## to get the line disciplines of STDOUT and STDERR synchronized and their
	## respective messages appearing in order as produced.  The combination is
	## apparently happening at the file-descriptor level, not at the level of
	## Perl's i/o buffering.  So it's still possible to have their respective
	## output streams inappropriately interleaved, brought on by buffering of
	## STDOUT messages.  To prevent that, we need to have STDOUT use the same
	## buffering as STDERR, namely to flush every line as soon as it is produced.
	## This is certainly a less-efficient use of system resources, but we don't
	## expect this program to write much to the STDOUT stream anyway.
	STDOUT->autoflush(1);
    }
    else {
	print "ERROR:  STDERR cannot be redirected to STDOUT!\n";
	$cycle_outcome = 0;
    }

    if ($cycle_outcome) {
	my $command_line_status = parse_command_line();
	if ( !$command_line_status ) {
	    spill_message "FATAL:  $PROGNAME either cannot understand its command-line parameters or cannot find its config file";
	    exit 1;
	}

	if ($show_version) {
	    print_version();
	}

	if ($show_help) {
	    print_usage();
	}

	if ($show_version || $show_help) {
	    exit 0;
	}

	if (not read_config_file($config_file, $debug_config)) {
	    spill_message "FATAL:  $PROGNAME cannot load configuration from $config_file";
	    return ERROR_STATUS;
	}

	# Stop if this is just a debugging run.
	return STOP_STATUS if $debug_config;

	# We need to prohibit executing as root (say, for a manual debugging run), so we
	# don't create files and directories that won't be modifiable later on when this
	# script is run in its usual mode as an ordinary user ("nagios").  We purposely
	# delay this test until after simple actions of the script, so we can at least
	# show the version and command-usage messages without difficulty.
	if ($> == 0) {
	    (my $program = $0) =~ s<.*/><>;
	    print "ERROR:  You cannot run $program as root.\n";
	    return ERROR_STATUS;
	}

	# We use a message prefix because multiple concurrent copies of this script may be writing to
	# the log file (not in normal operation via daily cron job, but potentially if manual executions
	# are also run occasionally), and we need a means to disambiguate where each message comes from.
	GW::Logger->new( $logfile, $run_interactively, $reflect_log_to_stdout, $max_logfile_size, $max_logfiles_to_retain, "($$)\t" );

	if ( !open_logfile() ) {
	    ## The routine will print an error message if it fails, so we don't do so ourselves.
	    $status_message = 'cannot open log file';
	    $cycle_outcome = 0;
	}
    }

    if ($cycle_outcome) {
	## We precede the startup message with a blank line, simply so the startup message is more visible.
	log_message '';
	log_timed_message "=== Log archive receiving script (version $VERSION) starting up (process $$). ===";

	if ( !$enable_processing ) {
	    print "FATAL:  log-archive receiving is not enabled in its config file.\n";
	    log_timed_message "FATAL:  Stopping log-archive receiving (process $$) because processing is not enabled in the config file ($config_file).";
	    $status_message = 'processing is disabled in the config file';
	    $cycle_outcome = 0;
	}
    }

    if ($cycle_outcome) {
	log_timed_message "NOTICE:  Reading state information from previous cycles.";
	$cycle_outcome = read_state_info();
	$status_message = 'cannot read state file' if not $cycle_outcome;
    }

    # Validate the time period that will be archived from file(s) into the database in this pass.
    if ($cycle_outcome) {
	log_timed_message "NOTICE:  Validating the archiving time period.";
	$cycle_outcome = validate_archiving_period();
	$status_message = 'cannot validate the archiving time period' if not $cycle_outcome;
    }

    # Compute the time period that will be used to delete file(s) in this pass, if the archiving is successful.
    if ($cycle_outcome) {
	log_timed_message "NOTICE:  Computing the file deletion time period.";
	$cycle_outcome = compute_file_deletion_period();
	$status_message = 'cannot compute the file deletion time period' if not $cycle_outcome;
    }

    # Compute the set of tables that will be archived.
    if ($cycle_outcome) {
	log_timed_message "NOTICE:  Computing the tables to archive.";
	$cycle_outcome = compute_tables_to_archive($target_extraction_path);
	$status_message = 'cannot compute the tables to archive' if not $cycle_outcome;
    }

    # Compute the order in which the tables will be archived, to ensure that all foreign key references
    # are satisfied in the resulting files.
    if ($cycle_outcome) {
	log_timed_message "NOTICE:  Computing the table archive order.";
	$cycle_outcome = compute_table_archive_order();
	$status_message = 'cannot compute the table archive order' if not $cycle_outcome;
    }

    capture_timing(\$script_init_end_time);

    # Open a connection to the archive database.
    if ($cycle_outcome) {
	log_timed_message "NOTICE:  Opening a connection to the archive database.";
	$cycle_outcome = open_database_connection();
	$status_message = 'cannot connect to the archive database' if not $cycle_outcome;
    }

    # Inject the archive data from file(s) into the archive database;
    # initiate data-insert/update operations on the target script machine.
    if ($cycle_outcome) {
	log_timed_message "NOTICE:  Injecting all data from files into the archive database.";
	$cycle_outcome = inject_into_all_tables($target_extraction_path);
	$status_message = 'cannot inject all configured tables' if not $cycle_outcome;
    }

    capture_timing(\$script_injection_end_time);

    if ($cycle_outcome) {
	log_timed_message "NOTICE:  Writing state information for use in future cycles.";
	$cycle_outcome = write_state_info();
	$status_message = 'cannot write state file' if not $cycle_outcome;
    }

    # If all of the above succeeded, delete data from the target file(s), per calculation above.
    if ($cycle_outcome) {
	log_timed_message "NOTICE:  Deleting old data files on the target machine.";
	$cycle_outcome = delete_old_target_files();
	$status_message = 'cannot delete target files' if not $cycle_outcome;
    }

    # Close the connection to the runtime database.  This is done unconditionally, since we should close
    # the connection even if errors occurred after it was opened.  The routine can internally handle the
    # case where the connection was never opened in the first place because of prior errors.  However,
    # it gets confusing if we log the occurrence of this call under circumstances when it won't actually
    # do anything, so we do qualify the logging here.
    log_timed_message "NOTICE:  Closing the connection to the archive database." if $dbh and log_is_open();
    close_database_connection();

    capture_timing(\$script_delete_files_end_time);

    log_archiving_statistics($status_message) if log_is_open();

    send_outcome_to_foundation( $status_message, $cycle_outcome );

    close_logfile();

    # Now return the overall cycle success or failure as the status of this routine.
    # This will be turned into a corresponding exit code for the receiving script,
    # and that will be the programmatic way that the calling sending script will
    # know whether this receiving cycle worked as intended.  No further detail is
    # either needed or directly provided.  (Note, though, that if the sending script
    # invokes the -o option of the receiving script, then all log messages in the
    # receiving script will be copied to STDOUT of the receiving script, not just
    # placed in the receiving-script's log file.  The sending script can capture
    # all that information and include it in its own log file.  But that level of
    # detail is more for human consumption and ease of access than anything else.
    # The success or failure of the receiving script is still just determined by
    # the exit code of this script.

    return $cycle_outcome ? STOP_STATUS : ERROR_STATUS;
}

sub print_version {
    print "$PROGNAME Version:  $VERSION\n";
    print "Copyright 2013-$COPYRIGHT_YEAR GroundWork, Inc. (www.gwos.com).\n";
    print "All rights reserved.\n";
}

# FIX LATER:  Perhaps support a -r option to control removal of old files from the
# target filesystem, and set that flag in the calling (send) script.
# FIX MAJOR:  Take a closer look at that idea:  what config-file parameters do we
# already have that control the deletion of such files?  Do we need yet another
# means of controlling the retention of such files?
sub print_usage {
    print "usage:  $PROGNAME -h\n";
    print "        $PROGNAME -v\n";
    print "        $PROGNAME -d [-c config_file]\n";
    print "        $PROGNAME -x extraction_path [-c config_file] [-i] [-o]\n";
    print "where:  -h:  print this help message\n";
    print "        -v:  print the version number\n";
    print "        -d:  debug config file\n";
    print "        -x extraction_path\n";
    print "             specifies the absolute pathname of the directory to search to\n";
    print "             find files to inject into the database; the final directory-name\n";
    print "             component must be specified in the form \"YYYY-MM-DD_hh.mm.ss\".\n";
    print "        -c config_file\n";
    print "             specifies an alternate config file; the default config file is:\n";
    print "             $default_config_file\n";
    print "        -i:  run interactively, not as a background process\n";
    print "        -o:  write log messages also to standard output\n";
    print "The -o option is illegal unless -i is also specified.\n";
}

# See the Config(3pm) man page for details of this magic formulation.
sub system_signal_name {
    my $signal_number = shift;
    local $_;

    my %sig_num;
    my @sig_name;

    unless ( $Config{sig_name} && $Config{sig_num} ) {
	return undef;
    }

    my @names = split ' ', $Config{sig_name};
    @sig_num{@names} = split ' ', $Config{sig_num};
    foreach (@names) {
	$sig_name[ $sig_num{$_} ] ||= $_;
    }

    return $sig_name[$signal_number] || undef;
}

sub wait_status_message {
    my $wait_status   = shift;
    my $exit_status   = $wait_status >> 8;
    my $signal_number = $wait_status & 0x7F;
    my $dumped_core   = $wait_status & 0x80;
    my $signal_name   = system_signal_name($signal_number) || "$signal_number is unknown";
    my $message = "exit status $exit_status" . ( $signal_number ? " (signal $signal_name)" : '' ) . ( $dumped_core ? ' (with core dump)' : '' );
    return $message;
}

sub log_to_foundation {
    my $severity   = shift;
    my $message    = shift;
    my $foundation = undef;
    local $_;

    # If $foundation_host is defined as an empty string in the config file,
    # we intentionally drop this message on the floor.
    if ( $foundation_host ne '' ) {
	eval {
	    $foundation = GW::Foundation->new( $foundation_host, $foundation_port, $monitor_server_hostname, $monitor_server_ip_address,
		$socket_send_timeout, $send_buffer_size, $debug_basic );
	};
	if ($@) {
	    chomp $@;
	    log_timed_message $@ if log_is_open();
	}

	# We adapt automatically to the absence (GWMEE 6.7.0) or presence (in later releases)
	# of the GW::Foundation::APP_ARCHIVE definition.
	my $app_archive;
	eval { $app_archive = GW::Foundation::APP_ARCHIVE(); };
	if ($@) {
	    if ( $@ =~ /^Undefined subroutine &GW::Foundation::APP_ARCHIVE called/ ) {
		## This possibility is allowed for, as APP_ARCHIVE was not defined in GW::Foundation
		## until the GWMEE 7.0.0 release.  We are assuming that this value was added to the
		## gwcollagedb.applicationtype table when this scripting was installed, though.
		$app_archive = 'ARCHIVE';
	    }
	    else {
		## Something more fundamental is afoot.  We have to punt.
		log_timed_message "ERROR:  Cannot call GW::Foundation::APP_ARCHIVE().\n" if log_is_open();
		$app_archive = GW::Foundation::APP_SYSTEM();
	    }
	}

	if ( defined $foundation ) {
	    my $errors = $foundation->send_log_message( $severity, $app_archive, $message );
	    map { log_timed_message $_ } @$errors if defined($errors) and log_is_open();
	}
    }
}

sub send_outcome_to_foundation {
    my $status_message = shift;
    my $outcome        = shift;

    # The speed numbers are slightly misleading, in that they also account for time taken for rows in other tables.
    # But this is good enough for reporting purposes.
    my $statistics =
	"injected $message_rows_injected events, $perfdata_rows_injected perf rows at $row_injection_speed total rows/sec;"
      . " inserted $message_rows_inserted new events, $perfdata_rows_inserted perf rows at $row_insert_speed total rows/sec;"
      . " $total_timestamp total run time";

    # We generate a Foundation log message to bring the success or failure of regular archiving
    # to the attention of the system operators.  We make this SEVERITY_WARNING in case of failure,
    # not SEVERITY_CRITICAL, because there should be no data loss involved; the same data will be
    # processed on the next run, generally along with additional new data.
    if ($outcome) {
	## We don't bother cluttering the message with a reference to the log file in the case of
	## success, as it shouldn't ordinarily be necessary to look there if everything went okay.
	log_to_foundation( SEVERITY_OK, $status_message eq '' ? "\u$statistics." : "\u$status_message; $statistics." );
    }
    else {
	## For this reference to be valid, we assume that our standard symlink is in place.
	## FIX LATER:  We could check that assumption here, and spill the full pathname if not.
	(my $log_file_only = $logfile) =~ s{.*/}{};
	my $log_details = " See logs/$log_file_only for details.";
	log_to_foundation( SEVERITY_WARNING, $status_message eq '' ? "\u$statistics." : "\u$status_message; $statistics.$log_details" );
    }
}

# See http://en.wikipedia.org/wiki/Hostname#Restrictions_on_valid_host_names for the tests we run here.
# FIX LATER:  We should probably go further, and run a name-service lookup here, to validate that $hostname
# will actually be useable later on.
sub is_valid_hostname {
    my $hostname = shift;
    my $label = '(?:[a-zA-Z0-9](?:[-a-zA-Z0-9]{0,61}[a-zA-Z0-9])?)';
    return (defined($hostname) and $hostname ne '' and length($hostname) <= 255 and $hostname =~ /^$label(?:\.$label)*$/o);
}

sub read_config_file {
    my $config_file  = shift;
    my $config_debug = shift;
    local $_;

    # All the config-file processing is wrapped in an eval{}; because TypedConfig
    # throws exceptions when it cannot open the config file or finds bad config data.
    eval {
	my $config = TypedConfig->secure_new( $config_file, $config_debug );

	# Whether to process anything.  Turn this off if you want to disable
	# this process completely, so log-archiving is prohibited.
	$enable_processing = $config->get_boolean('enable_processing');

	$debug_level = $config->get_number('debug_level');

	$debug_minimal = ( $debug_level >= 1 );
	$debug_basic   = ( $debug_level >= 2 );
	$debug_maximal = ( $debug_level >= 3 );

	# Where to log debug messages.
	$logfile = $config->get_scalar ('logfile');

	$archive_dbtype = $config->get_scalar('archive_dbtype');
	$archive_dbhost = $config->get_scalar('archive_dbhost');
	$archive_dbport = $config->get_number('archive_dbport');
	$archive_dbname = $config->get_scalar('archive_dbname');
	$archive_dbuser = $config->get_scalar('archive_dbuser');
	$archive_dbpass = $config->get_scalar('archive_dbpass');

	$target_script_machine    = $config->get_scalar('target_script_machine');
	$target_script_ip_address = $config->get_scalar('target_script_ip_address');

	if ( !is_valid_hostname($target_script_machine) ) {
	    die "ERROR:  configured value for target_script_machine must be a valid hostname\n";
	}

	$monitor_server_hostname = $target_script_machine;
	if ( $monitor_server_hostname eq '' ) {
	    die "ERROR:  configured value for target_script_machine cannot be an empty string\n";
	}
	elsif ( $monitor_server_hostname eq 'localhost' ) {
	    $monitor_server_ip_address = $target_script_ip_address ne '' ? $target_script_ip_address : '127.0.0.1';
	}
	elsif ( $target_script_ip_address ne '' ) {
	    $monitor_server_ip_address = $target_script_ip_address;
	}
	else {
	    ## FIX LATER:  This code is not yet IPv6-compliant.
	    ##
	    ## Note that gethostbyname() can return undef if passed an argument which is not actually found in DNS.
	    ## This can happen, for instance, if the specified host is not an actual hostname on the network.  So we
	    ## need to check the gethostbyname() return value to ensure it is defined.
	    ##
	    ## Also note that gethostbyname() is capable of returning all IP addresses on the machine, but we
	    ## don't have any way to figure out which is the "best" (except perhaps to reject any that look like
	    ## localhost, unless that's the only choice), so we just go with whatever standard algorithm is already
	    ## embedded within gethostbyname() when it is called in scalar context.  The administrator can avoid
	    ## this ambiguity by not defaulting the target_script_ip_address in the config file.
	    my $packed_ip_address = gethostbyname($monitor_server_hostname);
	    ## Check if we could resolve the hostname to an IP address.
	    if ( defined $packed_ip_address ) {
		$monitor_server_ip_address = inet_ntoa($packed_ip_address);
	    }
	    else {
		die "ERROR:  cannot resolve the IP address for target_script_machine \"$monitor_server_hostname\";"
		  . " you can avoid this by specifying a non-empty value for target_script_ip_address\n";
	    }
	}

	$manage_archive_gwservices            = $config->get_boolean('manage_archive_gwservices');
	$archive_gwservices_machine           = $config->get_scalar('archive_gwservices_machine');
	$archive_gwservices_machine_is_remote = $config->get_boolean('archive_gwservices_machine_is_remote');

	if ( !is_valid_hostname($archive_gwservices_machine) ) {
	    die "ERROR:  configured value for archive_gwservices_machine must be a valid hostname\n";
	}

	$table_locking_timeout_seconds = $config->get_number('table_locking_timeout_seconds');

	if ( $table_locking_timeout_seconds < 1 ) {
	    die "ERROR:  configured value for table_locking_timeout_seconds must be at least 1\n";
	}

	# Convert seconds to milliseconds for later use in an SQL statement.
	$table_locking_timeout_ms = $table_locking_timeout_seconds * 1000;

	# FIX MINOR:  These arrays are just an initial attempt at how to configure these parameters.  We still
	# need to ensure that we guarantee a particular fixed ordering of the specified values, in most cases.
	# Currently, that has been done by manual analysis of the database followed by corresponding sequencing of
	# the specified table names in the config file, and by experiment showing that said orderings are preserved
	# when the config file is read into these arrays.  In the future, we will take all the lists, analyze all
	# the cross-table references based on associations extracted from the database, and run a topological sort
	# to transform all the partial orderings we extract that way into a consistent total ordering.

	@primary_table_attributes = $config->get_array ('primary_table_attributes');
	print Data::Dumper->Dump([\@primary_table_attributes], [qw(\@primary_table_attributes)]) if $config_debug;

	@secondary_table_attributes = $config->get_array ('secondary_table_attributes');
	print Data::Dumper->Dump([\@secondary_table_attributes], [qw(\@secondary_table_attributes)]) if $config_debug;

	@tertiary_table_attributes = $config->get_array ('tertiary_table_attributes');
	print Data::Dumper->Dump([\@tertiary_table_attributes], [qw(\@tertiary_table_attributes)]) if $config_debug;

	foreach my $field_spec (@primary_table_attributes) {
	    ## This is really only crude validation.  Full validation will wait until we try to use
	    ## these values when accessing the database.
	    if ( $field_spec =~ /^\s*([a-z_]+)\s+([a-z_]+)\s+([a-z_]+(?:,[a-z_]+)*)\s*$/ ) {
		my $table         = $1;
		my $row_type      = $2;
		my $key_fields    = $3;
		if ( $table =~ /^schemainfo$/i ) {
		    die "ERROR:  primary_table_attributes cannot specify the \"$table\" table\n";
		}
		push @primary_tables, $table;
		## timed_association is not used for any primary tables in the present setup, so we may as well
		## disallow it here to better validate the configuration.  We can put it back in this validation
		## test in some future release, if it is ever actually needed for a primary table.
		if ( $row_type =~ /^(?:timed_object|untimed_detail)$/ ) {
		    $all_table_row_type{$table} = $row_type;
		}
		else {
		    die "ERROR:  configured value for primary_table_attributes (table \"$table\") has an invalid row_type (\"$row_type\")\n";
		}
		@{ $all_table_key_fields{$table} } = split( ',', $key_fields );
	    }
	    else {
		die "ERROR:  configured value for primary_table_attributes (\"$field_spec\") is invalid\n";
	    }
	}

	foreach my $field_spec (@secondary_table_attributes) {
	    ## This is really only crude validation.  Full validation will wait until we try to use
	    ## these values when accessing the database.
	    if ( $field_spec =~ /^\s*([a-z_]+)\s+([a-z_]+)\s+([a-z_]+(?:,[a-z_]+)*)\s*$/ ) {
		my $table         = $1;
		my $row_type      = $2;
		my $key_fields    = $3;
		if ( $table =~ /^schemainfo$/i ) {
		    die "ERROR:  secondary_table_attributes cannot specify the \"$table\" table\n";
		}
		push @secondary_tables, $table;
		if ( $row_type =~ /^(?:untimed_object)$/ ) {
		    $all_table_row_type{$table} = $row_type;
		}
		else {
		    die "ERROR:  configured value for secondary_table_attributes (table \"$table\") has an invalid row_type (\"$row_type\")\n";
		}
		@{ $all_table_key_fields{$table} } = split( ',', $key_fields );
	    }
	    else {
		die "ERROR:  configured value for secondary_table_attributes (\"$field_spec\") is invalid\n";
	    }
	}

	foreach my $field_spec (@tertiary_table_attributes) {
	    ## This is really only crude validation.  Full validation will wait until we try to use
	    ## these values when accessing the database.
	    if ( $field_spec =~ /^\s*([a-z_]+)\s+([a-z_]+)\s+([a-z_]+(?:,[a-z_]+)*)\s*$/ ) {
		my $table         = $1;
		my $row_type      = $2;
		my $key_fields    = $3;
		if ( $table =~ /^schemainfo$/i ) {
		    die "ERROR:  tertiary_table_attributes cannot specify the \"$table\" table\n";
		}
		push @tertiary_tables, $table;
		if ( $row_type =~ /^(?:timed_object|timed_association|untimed_detail)$/ ) {
		    $all_table_row_type{$table} = $row_type;
		}
		else {
		    die "ERROR:  configured value for tertiary_table_attributes (table \"$table\") has an invalid row_type (\"$row_type\")\n";
		}
		@{ $all_table_key_fields{$table} } = split( ',', $key_fields );
	    }
	    else {
		die "ERROR:  configured value for tertiary_table_attributes (\"$field_spec\") is invalid\n";
	    }
	}

	# Here we reverse the order of the specified tables, because by design we specify these tables
	# in the receiving-script config file in the same order as we do in the sending-script config file
	# (that is, in the required dump order), to make it trivial to compare the two sets of lists.
	# The receiving script, of course, has to populate the tables in the opposite order that they were
	# dumped in, so all the referred-to rows are in place before the references to them are created.
	@primary_tables   = reverse @primary_tables;
	@secondary_tables = reverse @secondary_tables;
	@tertiary_tables  = reverse @tertiary_tables;

	# We construct a hash both so we can trivially verify the content of the @message_data_tables
	# and @performance_data_tables arrays below.
	%is_a_secondary_table = map { $_ => 1 } @secondary_tables;

	@message_data_tables = $config->get_array ('message_data_table');
	print Data::Dumper->Dump([\@message_data_tables], [qw(\@message_data_tables)]) if $config_debug;

	foreach my $table (@message_data_tables) {
	    if ( not $is_a_secondary_table{$table} ) {
		die "ERROR:  message_data_table must specify one of the configured secondary tables\n";
	    }
	}

	@performance_data_tables = $config->get_array ('performance_data_table');
	print Data::Dumper->Dump([\@performance_data_tables], [qw(\@performance_data_tables)]) if $config_debug;

	foreach my $table (@performance_data_tables) {
	    if ( not $is_a_secondary_table{$table} ) {
		die "ERROR:  performance_data_table must specify one of the configured secondary tables\n";
	    }
	}

	# FIX LATER:  We ought to validate that there is no duplication within, nor commonality between,
	# @message_data_tables and @performance_data_tables.  For that matter, we should probably also
	# validate no duplication within, nor commonality between, the @primary_tables, @secondary_tables,
	# and @tertiary_tables as well.

	# FIX LATER:  We ought to validate that @message_data_tables union @performance_data_tables yields
	# exactly @secondary_tables.

	$dumpfile_format = $config->get_scalar('dumpfile_format');
	## We allow for easy evolution of this code in the overly-general formulation of the match.
	if ( $dumpfile_format !~ /^(copy)$/ ) {
	    die "ERROR:  configured value for dumpfile_format is not supported\n";
	}

	$target_dumpfile_retention_days    = $config->get_number('target_dumpfile_retention_days');
	$log_archive_target_data_directory = $config->get_scalar('log_archive_target_data_directory');
	$log_archive_target_state_file     = $config->get_scalar('log_archive_target_state_file');

	if ( $target_dumpfile_retention_days < 1 ) {
	    die "ERROR:  configured value for target_dumpfile_retention_days must be at least 1\n";
	}

	if ( $log_archive_target_data_directory !~ m{^/} ) {
	    die "ERROR:  configured value for log_archive_target_data_directory must be an absolute pathname\n";
	}

	if ( $log_archive_target_state_file !~ m{^/.*\.state$} ) {
	    ## We insist on an absolute pathname ending with ".state" to ensure that we never overwrite
	    ## any critical existing file elsewhere in the system because this setup was misconfigured.
	    die "ERROR:  configured value for log_archive_target_state_file must be an absolute pathname ending with \".state\"\n";
	}

	if ( -l $log_archive_target_state_file ) {
	    die "ERROR:  configured value for log_archive_target_state_file cannot be the path to a symlink\n";
	}

	$foundation_host = $config->get_scalar('foundation_host');
	$foundation_port = $config->get_number('foundation_port');

	if ( $foundation_host ne '' and !is_valid_hostname($foundation_host) ) {
	    die "ERROR:  configured value for foundation_host must be an empty string or a valid hostname\n";
	}
    };
    if ($@) {
	chomp $@;
	$@ =~ s/^ERROR:\s+//i;
	print "ERROR:  Cannot read config file $config_file\n  ($@).\n";
	return 0;
    }

    return 1;
}

sub parse_command_line {
    # First, clean up the $default_config_file value in case we print usage.
    # (This is disabled because of potential working-directory issues with realpath().)
    # my $real_path = realpath ($default_config_file);
    # $default_config_file = $real_path if $real_path;

    my %opts;
    if (not getopts('hvc:diox:', \%opts)) {
	print_usage();
	return 0;
    }

    $show_help              = $opts{h};
    $show_version           = $opts{v};
    $config_file            = ( defined $opts{c} && $opts{c} ne '' ) ? $opts{c} : $default_config_file;
    $debug_config           = $opts{d};
    $run_interactively      = $opts{i};
    $reflect_log_to_stdout  = $opts{o};
    $target_extraction_path = ( defined $opts{x} && $opts{x} ne '' ) ? $opts{x} : undef;

    # This test is not a full enforcement of intended exclusivity of the major
    # mode options, but it at least requires that you specify either -x or -d,
    # if neither -h nor -v is specified.
    if ( !$show_version && !$show_help && !$debug_config && !$target_extraction_path ) {
	print_usage();
	return 0;
    }

    # FIX MAJOR
    # Adjust the config file specification to be an absolute pathname,
    # partly so we could be a bit more cavalier when we specified it, and
    # partly because a relative pathname would be misleading considering
    # that our working directory will generally be different from where
    # we started, even if we don't run this script as a daemon.
    if (0) {
	# FIX MAJOR:  This next line is commented out because $Bin is not yet defined here.
	# $config_file = "$Bin/../config/$config_file" if $config_file !~ m{^/};
	my $real_path = realpath($config_file);
	if (!$real_path) {
	    spill_message "FATAL:  The path to the $PROGNAME config file $config_file either does not exist or is inaccessible to this script running as ", (scalar getpwuid $>), '.';
	    return 0;
	}
	$config_file = $real_path;
    }

    if (!$run_interactively && $reflect_log_to_stdout) {
	print_usage();
	return 0;
    }

    # We cannot match the $target_extraction_path against the $log_archive_target_data_directory
    # at this point, because we haven't read the config file yet.  We will carry out that extra
    # validation before we use the path.
    if (    !$show_help
	and !$show_version
	and !$debug_config
	and ( !defined($target_extraction_path) or $target_extraction_path !~ m{^/.+/\d{4}-\d{2}-\d{2}_\d{2}\.\d{2}\.\d{2}$} ) )
    {
	print "ERROR:  You have not specified a valid -x option.\n";
	print "        It must be an absolute pathname ending with a\n";
	print "        directory name of the form \"YYYY-MM-DD_hh.mm.ss\".\n";
	print "\n";
	print_usage();
	return 0;
    }

    return 1;
}

sub read_state_info {
    my $config_file  = shift;
    my $config_debug = shift;
    my $outcome      = 1;

    # All the state-file processing is wrapped in an eval{}; because TypedConfig
    # throws exceptions when it cannot open the config file or finds bad config data.
    eval {
	## We use secure_new() instead of a plain new() not because the data is
	## terribly secretive, but because we want to ensure that nobody but the
	## file owner has write permissions to this file.  This is a bit different
	## from the usual case, where for security reasons we want to ensure that
	## nobody but the owner has read permissions to the file.  Perhaps this
	## circumstance shows that we could use a new variant of the constructor.
	my $config = TypedConfig->secure_new($log_archive_target_state_file);

	# Where to begin capturing data in this archiving cycle.
	$last_previous_successful_run_timestamp = $config->get_scalar('last_previous_successful_run_timestamp');
	if ( $last_previous_successful_run_timestamp !~ /^\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}$/ ) {
	    log_timed_message "ERROR:  State-file saved value for last_previous_successful_run_timestamp is invalid.";
	    log_timed_message "        State-file saved value is: \"$last_previous_successful_run_timestamp\".";
	    $outcome = 0;
	}
    };
    if ($@) {
	chomp $@;
	$@ =~ s/^ERROR:\s+//i;
	log_timed_message "ERROR:  Cannot read state file $log_archive_target_state_file\n  ($@).";
	$outcome = 0;
    }

    return $outcome;
}

sub write_state_info {
    my $outcome = 1;

    # In this routine, we write the updated state info into a new file, so it
    # can be swapped into place as the standard state file as an atomic operation.

    ## For now, we will unconditionally write the state file if we get this far.
    ## We might want to revisit that decision, and suppress writing the state file
    ## if this run represented data older than the most recent data we have archived.
    if ( 1 or $archive_run_timestamp gt $last_previous_successful_run_timestamp ) {
	## By dint of the validation we performed when we read in the config file,
	## we know that $log_archive_target_state_file will be an absolute pathname.
	my $temporary_state_file = "$log_archive_target_state_file.temp";
	if ( not sysopen( STATE, $temporary_state_file, O_WRONLY | O_APPEND | O_CREAT | O_EXCL, 0600 ) ) {
	    log_timed_message "ERROR:  Could not open the temporary state file $temporary_state_file ($!).";
	    $outcome = 0;
	}
	else {
	    if (not print STATE <<EOF) {
# This file contains the persistent-state information that must be carried
# across invocations of the log-archive-receive.pl script, to efficiently
# compare the time periods used in successive runs.  Since the data to be
# injected into the database is determined external to this script, this
# only serves to validate monotonicity of successive injection runs, and
# not to define or constrain the injected data.

# The data timestamp of the last successful run of log-archive-receive.pl,
# expressed in the local timezone of the target script machine.  It must be
# specified as a string in the form "YYYY-MM-DD hh:mm:ss".  This value is
# the timestamp of the target directory that contained all the files whose
# contents were stuffed into the archive database in the last successful run.
last_previous_successful_run_timestamp = "$archive_run_timestamp"
EOF
		log_timed_message "ERROR:  Could not write to the temporary state file $temporary_state_file ($!).";
		$outcome = 0;
	    }

	    if (not close STATE) {
		log_timed_message "ERROR:  Could not close the temporary state file $temporary_state_file ($!).";
		$outcome = 0;
	    }

	    if ($outcome) {
		if (not rename $temporary_state_file, $log_archive_target_state_file) {
		    log_timed_message "ERROR:  Could not rename the temporary state file $temporary_state_file ($!).";
		    $outcome = 0;
		}
	    }
	}
    }

    return $outcome;
}

# We stop and start archive-related gwservices regardless of whether the archive machine is
# different from the runtime machine, because the copy of gwservices that we control here is
# a special version that manages only archive-related services, not production-monitoring
# services.  So we don't fear disabling gwservices in a production monitoring environment.
# On the other hand, explicit control of whether we manage archive gwservices is provided in
# the config file, so we depend on that setting to decide whether to take any action here.

sub start_archive_gwservices {
    my $outcome     = 1;
    my $wait_status = undef;
    my $results;

    if ($manage_archive_gwservices) {
	## The control script needs to be run on the archive server, whether or not this receive script
	## is running on the archive server.  So we need an independent determination of where the
	## archive server is; we cannot simply depend on the name of the target_script_server.
	log_timed_message "INFO:  Starting archive gwservices on the $archive_gwservices_machine machine:";
	if ($archive_gwservices_machine_is_remote) {
	    ## run remotely, via ssh
	    $results = `ssh nagios\@$archive_gwservices_machine $gwservices_control_program start 2>&1`;
	    $wait_status = $?;
	}
	else {
	    ## run locally
	    $results = `$gwservices_control_program start 2>&1`;
	    $wait_status = $?;
	}
	chomp $results if defined $results;
	$results = '(No output was received from starting archive gwservices.)' if !defined($results) or $results eq '';
	log_message $results;

	if ( $wait_status == 0 ) {
	    log_timed_message 'NOTICE:  Archive gwservices were successfully started.';
	}
	else {
	    my $status_message = wait_status_message($wait_status);
	    log_message "ERROR:  Starting archive gwservices failed with $status_message.";
	    $outcome = 0;
	}
    }
    else {
	log_timed_message 'NOTICE:  Archive gwservices are not being started, because they are not being managed by the log archiving.';
    }

    return $outcome;
}

sub stop_archive_gwservices {
    my $outcome     = 1;
    my $wait_status = undef;
    my $results;

    if ($manage_archive_gwservices) {
	## The control script needs to be run on the archive server, whether or not this receive script
	## is running on the archive server.  So we need an independent determination of where the
	## archive server is; we cannot simply depend on the name of the target_script_server.
	log_timed_message "INFO:  Stopping archive gwservices on the $archive_gwservices_machine machine:";
	if ($archive_gwservices_machine_is_remote) {
	    ## run remotely, via ssh
	    $results = `ssh nagios\@$archive_gwservices_machine $gwservices_control_program stop 2>&1`;
	    $wait_status = $?;
	}
	else {
	    ## run locally
	    $results = `$gwservices_control_program stop 2>&1`;
	    $wait_status = $?;
	}
	chomp $results if defined $results;
	$results = '(No output was received from stopping archive gwservices.)' if !defined($results) or $results eq '';
	log_message $results;

	if ( $wait_status == 0 ) {
	    log_timed_message 'NOTICE:  Archive gwservices were successfully stopped.';
	}
	else {
	    my $status_message = wait_status_message($wait_status);
	    log_message "ERROR:  Stopping archive gwservices failed with $status_message.";
	    $outcome = 0;
	}
    }
    else {
	log_timed_message 'NOTICE:  Archive gwservices are not being stopped, because they are not being managed by the log archiving.';
    }

    return $outcome;
}

sub validate_archiving_period {
    my $outcome = 1;

    if ($target_extraction_path !~ m{^\Q$log_archive_target_data_directory\E/(\d{4})-(\d{2})-(\d{2})_(\d{2})\.(\d{2})\.(\d{2})$} ) {
	log_timed_message "ERROR:  Invalid target extraction path specified:  \"$target_extraction_path\".";
	log_timed_message "        The configured log_archive_target_data_directory value must be";
	log_timed_message "        the first part of this path, and the remainder must be a direct";
	log_timed_message "        subdirectory of that directory of the form \"YYYY-MM-DD_hh.mm.ss\".";
	$outcome = 0;
    }
    else {
	## By convention strictly observed by the sending script, the $archive_end_timestamp can be unambiguously
	## derived from the name of the directory in which the dump files are stored.  This allows the receiving
	## script to get this extra piece of information without requiring either an extra command-line argument
	## or the use of a separate transient-state file mixed in amongst the dump files.
	$archive_end_timestamp = "$1-$2-$3 00:00:00";
	$archive_run_timestamp = "$1-$2-$3 $4:$5:$6";

	my ( $year, $month, $month_day, $hour, $minute, $second ) = ( $1, $2, $3, $4, $5, $6 );
	eval {
	    ## We always use a 4-digit year when calling timelocal(), to avoid any confusion about how it is treating this value,
	    ## since it makes a mess of interpreting this time component.  See "perldoc Time::Local" for details.
	    $archive_run_time = timelocal( $second, $minute, $hour, $month_day, $month - 1, $year );
	};
	if ($@) {
	    log_timed_message "ERROR:  target extraction path $target_extraction_path has an invalid time value; it will not be processed.";
	    $outcome = 0;
	}
	else {
	    log_timed_message "STATS:  Archiving capture time was:  $archive_run_timestamp" if $debug_minimal;
	    log_timed_message "STATS:  Archiving period   ends at:  $archive_end_timestamp" if $debug_minimal;
	    if ( $archive_run_timestamp le $last_previous_successful_run_timestamp ) {
		my $compared_to = ( $archive_run_timestamp eq $last_previous_successful_run_timestamp ) ? "the same time as" : "earlier than";
		log_timed_message "WARNING:  The last injection used data captured at $last_previous_successful_run_timestamp,";
		log_timed_message "          while this run will use data captured at $archive_run_timestamp,";
		log_timed_message "          which is $compared_to the previous run.";
		## In this first version, we don't stop the script from re-running old data.
		## It should be safe to so so, because we support updates as well as inserts.
		## Still, we provide the warning above that you're probably doing something
		## out of the standard order, so the odd circumstance is noted in the log.
		## $outcome = 0;
	    }
	}
    }

    return $outcome;
}

# We only want to delete files whose containing-directory timestamp is older than both $target_dumpfile_retention_days
# (relative to now) and $last_previous_successful_run_timestamp (as an absolute point in time).  This gives us some
# assurance that we can play with recent data and re-insert it if necessary.  In particular, these constraints provide
# a simple hedge against data loss between full backups of the database, assuming you back up the full archive database
# to some other location (tape, perhaps?) more often than $target_dumpfile_retention_days.
sub compute_file_deletion_period {
    my ( $second, $minute, $hour, $month_day, $month, $year, $week_day, $year_day, $is_dst );
    my $outcome = 1;

    # For safety purposes, we retain data in files for at least as long as the locally configured
    # $target_dumpfile_retention_days, and also for at least one previous successful run.
    if ( $last_previous_successful_run_timestamp =~ /^(\d{4})-(\d{2})-(\d{2}) (\d{2}):(\d{2}):(\d{2})$/ ) {
	( $year, $month, $month_day, $hour, $minute, $second ) = ( $1, $2, $3, $4, $5, $6 );
	## We always use a 4-digit year when calling timelocal(), to avoid any confusion about how it is treating this value,
	## since it makes a mess of interpreting this time component.  See "perldoc Time::Local" for details.
	my $last_previous_successful_run_time = timelocal( $second, $minute, $hour, $month_day, $month - 1, $year );

	if ( $run_start_timestamp =~ /^(\d{4})-(\d{2})-(\d{2}) (\d{2}):(\d{2}):(\d{2})$/ ) {
	    ( $year, $month, $month_day, $hour, $minute, $second ) = ( $1, $2, $3, $4, $5, $6 );

	    # $target_dumpfile_retention_days may be expressed in fractional days.  As such, we need to convert
	    # any fractional part to corresponding smaller units, and them subtrace them from our time base.
	    #
	    # We could have made this calculation easier by just computing:
	    #
	    #     $earliest_file_retention_time = $run_start_time - $target_dumpfile_retention_days * SECONDS_PER_DAY;
	    #
	    # but then the integral "day" component of $target_dumpfile_retention_days might not get us back
	    # the corresponding number of exact calendar days, due to Daylight Savings Time adjustments.

	    my $remaining_dumpfile_retention_days = $target_dumpfile_retention_days;
	    my $dumpfile_retention_days = POSIX::floor($remaining_dumpfile_retention_days);
	    $remaining_dumpfile_retention_days -= $dumpfile_retention_days;
	    my $dumpfile_retention_hours = POSIX::floor($remaining_dumpfile_retention_days * HOURS_PER_DAY);
	    $remaining_dumpfile_retention_days -= $dumpfile_retention_hours / HOURS_PER_DAY;
	    my $dumpfile_retention_minutes = POSIX::floor($remaining_dumpfile_retention_days * MINUTES_PER_DAY);
	    $remaining_dumpfile_retention_days -= $dumpfile_retention_minutes / MINUTES_PER_DAY;
	    my $dumpfile_retention_seconds = POSIX::floor($remaining_dumpfile_retention_days * SECONDS_PER_DAY);

	    ## We always use a 4-digit year when calling timelocal(), to avoid any confusion about how it is treating this value,
	    ## since it makes a mess of interpreting this time component.  See "perldoc Time::Local" for details.
	    $earliest_file_retention_time = timelocal_nocheck(
		int($second    - $dumpfile_retention_seconds),
		int($minute    - $dumpfile_retention_minutes),
		int($hour      - $dumpfile_retention_hours),
		int($month_day - $dumpfile_retention_days),
		$month - 1,
		$year
	    );

	    # Beyond just taking $target_dumpfile_retention_days into account when deleting files, we never immediately
	    # delete the files we just processed in this run, nor do we delete the files from the last previous successful
	    # run during this run.  These additional constraints provide an extra level of protection against deleting
	    # data too quickly.  Some day, this ultra-conservative approach will save us from disaster.
	    if ( $earliest_file_retention_time >= $archive_run_time ) {
		$earliest_file_retention_time = $archive_run_time - 1;
	    }
	    if ( $earliest_file_retention_time >= $last_previous_successful_run_time ) {
		$earliest_file_retention_time = $last_previous_successful_run_time - 1;
	    }

	    ( $second, $minute, $hour, $month_day, $month, $year, $week_day, $year_day, $is_dst ) = localtime($earliest_file_retention_time);
	    $earliest_file_retention_timestamp =
	      sprintf "%04d-%02d-%02d %02d:%02d:%02d", $year + 1900, $month + 1, $month_day, $hour, $minute, $second;
	    log_timed_message "STATS:  File deletion period ends at:  $earliest_file_retention_timestamp" if $debug_minimal;
	}
	else {
	    log_timed_message "ERROR:  Bad internal operation; \$run_start_timestamp has an unexpected format!";
	    $outcome = 0;
	}
    }
    else {
	## This value was supposed to have been checked for correctness in the read_state_file() routine.
	log_timed_message "ERROR:  Internal failure (invalid value for \$last_previous_successful_run_timestamp).";
	$outcome = 0;
    }

    return $outcome;
}

# In this initial implementation, this routine validates the available files in the $target_directory,
# but does not take the preceding step of analyzing the database to determine the full set of tables to
# archive.  That's because we are specifying all the tables to archive directly in the config file.  A
# future release might allow us to limit the specified tables to just the secondary tables, and allow the
# rest of the table names to be automatically derived from the secondary-table names using the database's
# information_schema associations.
sub compute_tables_to_archive {
    my $target_directory = shift;
    my $outcome          = 1;
    local $_;

    # First, validate the full $target_directory path, now that we have all information available to do that.
    if ( $target_directory !~ m{^\Q$log_archive_target_data_directory\E/\d{4}-\d{2}-\d{2}_\d{2}\.\d{2}\.\d{2}$} ) {
	log_timed_message "ERROR:  Invalid target extraction path specified:  $target_directory";
	log_timed_message "        The configured log_archive_target_data_directory value must be";
	log_timed_message "        the first part of this path, and the remainder must be a direct";
	log_timed_message "        subdirectory of that directory of the form \"YYYY-MM-DD_hh.mm.ss\".";
	$outcome = 0;
    }
    elsif ( -l $target_directory or !-d _ ) {
	## We're outlawing symlinks simply for security reasons.  Logically, they would work, but we
	## don't want to allow such symlinks to point to some random location in the filesystem and
	## have this script then be responsible for loading some sort of garbage data into the archive.
	log_timed_message "ERROR:  Invalid target extraction path specified:  $target_directory";
	log_timed_message "        The configured log_archive_target_data_directory value must be";
	log_timed_message "        a directory, and cannot be a symlink to such a directory.";
	$outcome = 0;
    }
    else {
	my %primary_files   = ();
	my %secondary_files = ();
	my %tertiary_files  = ();
	if (opendir TARGET, $target_directory) {
	    ## There doesn't seem to be any direct way of sensing a failure when reading the directory.
	    ## Oh, well, we'll probably find out if that happened in the following qualification of what
	    ## we did manage to read.
	    my @allfiles = readdir TARGET;
	    if (closedir TARGET) {
		foreach my $file (@allfiles) {
		    ## Skip the usual suspects.
		    next if $file eq '.' or $file eq '..';

		    # Validate the form of each filename, and extract the relevant subfields.
		    # Logically, we ought to ensure that the timestamp on the file is no older
		    # than the timestamp in the directory name.  But we'll skip that for now.
		    if ($file =~ m{^([a-z_]+)\.dump\.(\d{4})-(\d{2})-(\d{2})_(\d{2})\.(\d{2})\.(\d{2})$} ) {
			## Accumulate the table names, and check for duplicates.  Build a table => filename
			## hash for later use in sequencing the filenames for injection purposes.
			my $table = $1;
			if ( not exists $matched_file{$table} ) {
			    $matched_file{$table}           = $file;
			    $matched_dump_timestamp{$table} = "$2-$3-$4 $5:$6:$7";
			}
			else {
			    log_timed_message "ERROR:  Directory \"$target_directory\" contains more than one file for the $table table:";
			    log_timed_message "        \"$matched_file{$table}\" and \"$file\".";
			    log_timed_message "        This dataset looks corrupt; archiving will be aborted.";
			    $outcome = 0;
			    last;
			}
		    }
		    else {
			log_timed_message "ERROR:  Directory \"$target_directory\" contains unknown file \"$file\".";
			$outcome = 0;
		    }
		}
		if ($outcome) {
		    ## By assigning each file to only one category (primary, secondary, and tertiary),
		    ## we effectively also test here that these sets of tables are disjoint.
		    ##
		    ## Accumulate separated lists of primary, secondary, and tertiary files.
		    my %primary_tablenames   = map { $_ => 1 } @primary_tables;
		    my %secondary_tablenames = map { $_ => 1 } @secondary_tables;
		    my %tertiary_tablenames  = map { $_ => 1 } @tertiary_tables;
		    foreach my $table (keys %matched_file) {
			if (exists $primary_tablenames{$table}) {
			    $primary_files{$table} = $matched_file{$table};
			}
			elsif (exists $secondary_tablenames{$table}) {
			    $secondary_files{$table} = $matched_file{$table};
			}
			elsif (exists $tertiary_tablenames{$table}) {
			    $tertiary_files{$table} = $matched_file{$table};
			}
			else {
			    log_timed_message "ERROR:  Directory \"$target_directory\" contains file \"$matched_file{$table}\" for unknown table \"$table\".";
			    $outcome = 0;
			    ## We don't abort out of this loop early, because there is no danger in continuing
			    ## to the end, and we might usefully catch and log other unknown files as well.
			}
		    }
		}
		if ($outcome) {
		    ## Verify that we have all the tables we expect, and no missing tables, in each category.
		    foreach my $table (@primary_tables) {
			if (not exists $primary_files{$table}) {
			    log_timed_message "ERROR:  Directory \"$target_directory\" contains no file for primary table \"$table\".";
			    $outcome = 0;
			}
		    }
		    foreach my $table (@secondary_tables) {
			if (not exists $secondary_files{$table}) {
			    log_timed_message "ERROR:  Directory \"$target_directory\" contains no file for secondary table \"$table\".";
			    $outcome = 0;
			}
		    }
		    foreach my $table (@tertiary_tables) {
			if (not exists $tertiary_files{$table}) {
			    log_timed_message "ERROR:  Directory \"$target_directory\" contains no file for tertiary table \"$table\".";
			    $outcome = 0;
			}
		    }
		}
	    }
	    else {
		log_timed_message "ERROR:  Could not close directory \"$target_directory\" after reading ($!).";
		$outcome = 0;
	    }
	}
	else {
	    log_timed_message "ERROR:  Could not open directory \"$target_directory\" for reading ($!).";
	    $outcome = 0;
	}
    }

    return $outcome;
}

# In this initial implementation, this routine is just a placeholder, because we are specifying the
# exact order in which the archived tables must be processed directly in the config file.  A future
# release might calculate that information for us, by relying on table associations listed in the
# database's information_schema tables.  We would have done so right away in this initial release,
# save for a bug in the PostgreSQL 9.1.1 information_schema.referential_constraints view (fixed in
# the PostgreSQL 9.1.2 release; see the release notes).  We haven't yet had time to investigate and
# work around that bug if it were to impact our analysis here.
sub compute_table_archive_order {
    my $outcome = 1;
    return $outcome;
}

sub delete_data_subdirectory {
    my $outcome = 1;
    my $subdirectory = shift;
    if (opendir SUBDIR, $subdirectory) {
	my @allfiles = readdir SUBDIR;
	closedir SUBDIR;
	foreach my $file (@allfiles) {
	    ## $file eq "$table.dump.$dump_timestamp"
	    next if $file eq '.' or $file eq '..';
	    if ( -l "$subdirectory/$file" ) {
		log_timed_message "ERROR:  File $subdirectory/$file is a symlink, so it will not be deleted.";
	    }
	    elsif ( !-f _ ) {
		log_timed_message "ERROR:  File $subdirectory/$file is not an ordinary file, so it will not be deleted.";
	    }
	    elsif ( $file !~ /^.+?\.dump\.(\d{4})-(\d{2})-(\d{2})_(\d{2})\.(\d{2})\.(\d{2})$/ ) {
		log_timed_message "ERROR:  File $subdirectory/$file does not match the expected filename pattern, so it will not be deleted.";
	    }
	    else {
		my ($year, $month, $month_day, $hour, $minute, $second) = ($1, $2, $3, $4, $5, $6);
		my $file_time = undef;
		eval {
		    ## We always use a 4-digit year when calling timelocal(), to avoid any confusion about how it is treating this value,
		    ## since it makes a mess of interpreting this time component.  See "perldoc Time::Local" for details.
		    $file_time = timelocal( $second, $minute, $hour, $month_day, $month - 1, $year );
		};
		if ($@) {
		    ## This circumstance is not serious enough to warrant a full-on error,
		    ## but we do need to report its unexpected occurrence.
		    log_timed_message "WARNING:  File $subdirectory/$file has an invalid time value; it will not be deleted.";
		}
		else {
		    if ($file_time < $earliest_file_retention_time) {
			if (unlink ("$subdirectory/$file")) {
			    log_timed_message "NOTICE:  File $subdirectory/$file was removed as part of normal cleanup.";
			    ++$total_files_deleted;
			}
			else {
			    log_timed_message "WARNING:  File $subdirectory/$file could not be removed ($!).";
			}
		    }
		    else {
			log_timed_message "NOTICE:  File $subdirectory/$file time is not before earliest file retention time, so it will not be deleted.";
		    }
		}
	    }
	}
	# We remove the directory itself if it is now empty.
	if (not rmdir $subdirectory) {
	    ## This is not necessarily a bad condition.  Typically, it will be resolved in the run
	    ## of this script on the following day, when all the files in the subdirectory will have
	    ## completely aged out.  Still, for the sake of completeness about what is happening on
	    ## the customer system, we note this circumstance in the log, in case other objects have
	    ## found their way into the subdirectory and it will never be removed.
	    log_timed_message "NOTICE:  $subdirectory is not removed because it is not empty.";
	}
    }
    else {
	log_timed_message "ERROR:  Could not open directory $subdirectory for reading ($!).";
	## We don't reflect this condition in $outcome to stop the rest of the script, because
	## by the time we get here, we've already done the critical work in this script.  So
	## there's no point in stopping now.
    }
    return $outcome;
}

sub delete_old_target_files {
    my $outcome = 1;
    ## Our strategy for deleting files is simple:  the file reader always has ownership and is in charge of deleting
    ## them when it decides they are no longer useful.  The determination of "owner" is a bit subtle, though, because
    ## we support several different setups.  Specifically, the target script machine may or may not be the same machine
    ## as the source script machine, and (in theory, at least) the $log_archive_source_data_directory may or may not be
    ## the same as the $log_archive_target_data_directory.  So we need to carefully determine who is the reader of the
    ## files in the $log_archive_target_data_directory, which are the only files that this log-archive-receive.pl script
    ## will have authority to delete.  However, the decision is simple on the receiving side:  the receiver is always
    ## the reader of what it sees as the $log_archive_target_data_directory, even if the sender also has access to that
    ## file tree, so it always has authority to manage the removal of its subdirectories.
    if (opendir BASEDIR, $log_archive_target_data_directory) {
	my @allfiles = readdir BASEDIR;
	closedir BASEDIR;
	foreach my $file (@allfiles) {
	    if ($file =~ /^(\d{4})-(\d{2})-(\d{2})_(\d{2})\.(\d{2})\.(\d{2})$/ and !-l "$log_archive_target_data_directory/$file" and -d _) {
		my ($year, $month, $month_day, $hour, $minute, $second) = ($1, $2, $3, $4, $5, $6);
		my $directory_time = undef;
		eval {
		    ## We always use a 4-digit year when calling timelocal(), to avoid any confusion about how it is treating this value,
		    ## since it makes a mess of interpreting this time component.  See "perldoc Time::Local" for details.
		    $directory_time = timelocal( $second, $minute, $hour, $month_day, $month - 1, $year );
		};
		if ($@) {
		    ## This circumstance is not serious enough to warrant a full-on error,
		    ## but we do need to report its unexpected occurrence.
		    log_timed_message "WARNING:  Directory $log_archive_target_data_directory/$file has an invalid time value; it will not be deleted.";
		}
		else {
		    if ($directory_time < $earliest_file_retention_time) {
			log_timed_message "NOTICE:  Cleaning up the $log_archive_target_data_directory/$file directory.";
			delete_data_subdirectory ("$log_archive_target_data_directory/$file");
		    }
		}
	    }
	}
    }
    else {
	log_timed_message "ERROR:  Could not open directory $log_archive_target_data_directory for reading ($!).";
	## We don't reflect this condition in $outcome to stop the rest of the script, because
	## by the time we get here, we've already done the critical work in this script.  So
	## there's no point in stopping now.
    }
    return $outcome;
}

# Inject rows from a designated file into a single named database table.
sub inject_into_database_table {
    my $target_path    = shift;
    my $dump_timestamp = shift;
    my $table          = shift;
    my $outcome        = 1;
    local $_;

    # This implementation only supports PostgreSQL, because it depends on specific capabilities of the PostgreSQL DBD::Pg driver.
    if ( $archive_dbtype eq 'postgresql' ) {
	if ( $dumpfile_format eq 'copy' ) {
	    ## Here we use the client connection as a transport from the database to the file, both because we're not
	    ## assuming database superuser privileges (needed for the server to write directly to a file), and because
	    ## the database might be located on a remote server, which would not have access to our local filesystem.

	    # Dump files can potentially be huge, especially for the first archive run when all historical data
	    # for a given table is gathered together in a single large file.  Therefore, we have to anticipate
	    # the possibility that a dump file might be larger than 2GB.
	    #
	    # We don't bother specifying O_LARGEFILE here as well, because GroundWork Perl is compiled with
	    # "-D_LARGEFILE_SOURCE -D_FILE_OFFSET_BITS=64" (you can check this with "/usr/local/groundwork/perl/bin/perl -V").
	    # So we should have no problem accessing very large dump files here.
	    if ( sysopen( DUMP, $target_path, O_RDONLY | O_NOFOLLOW ) ) {
		my $injection_is_good = 1;
		my $injected_rows     = 0;
		my $obsoleted_rows    = 0;
		my $updated_rows      = 0;
		my $inserted_rows     = 0;
		my $reincarnated_rows = 0;

		# We capture timing statistics for the data-loading activity.  Inasmuch as we are interleaving
		# file reading and database writing, can cannot reasonably be more granular about the collected
		# data (i.e., separating reading and writing timing).  So we just lump in everything having to
		# do with database operations during this activity, including the temporary table creation.
		my $copy_start_time;
		my $copy_end_time;
		capture_timing(\$copy_start_time);

		## Create the temporary table first, before copying into it.  PostgreSQL temporary tables
		## are automatically unlogged; you cannot specify the UNLOGGED keyword in this command.
		my $temp_table = "temp_$table";
		log_timed_message "NOTICE:  Creating temporary table $temp_table ...";
		## If we have startvalidtime and endvalidtime fields in $table, it is not acceptable to create
		## $temp_table directly in its image.  That's because we don't want those fields in the temporary table,
		## which is supposed to mirror the table structure in the runtime database, not in the archive database.
		## We want the restricted structure partly because we need to use a COPY command to feed data into the
		## temporary table from a dump taken on the runtime database, and partly because we reference "tt.*" in
		## one of the SQL statements below, without meaning to have it reference startvalidtime and endvalidtime
		## fields.  So we need to either not use those columns in the temporary table definition, or drop those
		## two columns from the temporary table definition (for a timed $table) before trying to stuff data into
		## the temporary table.  The latter course is by far the simpler, because it means we don't need to have
		## detailed knowledge of the column structure when we first create the temporary table.
		if ( not defined $dbh->do("CREATE TEMPORARY TABLE \"$temp_table\" (LIKE \"$table\")") ) {
		    my $errstr = $dbh->errstr;
		    chomp $errstr if defined $errstr;
		    $errstr = 'unknown condition' if not defined $errstr;
		    log_timed_message "ERROR:  Cannot create temporary table \"$temp_table\" ($errstr); aborting injection into table $table!";
		    $injection_is_good = 0;
		}
		elsif (
		    $all_table_row_type{$table} =~ /^timed_/
		    and (  !defined( $dbh->do("ALTER TABLE \"$temp_table\" DROP COLUMN startvalidtime") )
			or !defined( $dbh->do("ALTER TABLE \"$temp_table\" DROP COLUMN endvalidtime") ) )
		  )
		{
		    my $errstr = $dbh->errstr;
		    chomp $errstr if defined $errstr;
		    $errstr = 'unknown condition' if not defined $errstr;
		    log_timed_message "ERROR:  Cannot alter temporary table \"$temp_table\" ($errstr); aborting injection into table $table!";
		    $injection_is_good = 0;
		}

		if ($injection_is_good) {
		    log_timed_message "NOTICE:  Copying data into temporary table $temp_table ...";
		    if ( not defined $dbh->do("COPY \"$temp_table\" FROM STDIN") ) {
			my $errstr = $dbh->errstr;
			chomp $errstr if defined $errstr;
			$errstr = 'unknown condition' if not defined $errstr;
			log_timed_message
			  "ERROR:  Cannot put the database connection into COPY IN mode ($errstr); aborting injection into table $table!";
			$injection_is_good = 0;
		    }
		    else {
			## A future version of this code might rework this logic to use a separate COPY ... FROM STDIN command
			## for each data block of a certain configured number of rows from the file, if we decide that we want
			## to commit this data insertion at the block level instead of at the entire-insert level.  It's not
			## clear whether that would be either required or advantageous, except perhaps so we could independently
			## measure timing for the read-from-file and send-to-database actions.
			##
			## We don't slurp in the entire file here, because it could be ginormous and chew up all available
			## memory.  So we read it as individual rows, and inject those rows directly into the database as we
			## go.  Perl doesn't have an operator that would allow us to "read n lines into this array, and let
			## me test explicitly for errors" in one operation, so there doesn't seem to be any way to make this
			## file-reading process more efficient; we are reliant on whatever input buffer size is supplied by the
			## standard Perl I/O layer.  And the PostgreSQL DBI::Pg package doesn't seem to have any block-copy
			## support, either, so we are constrained to use per-row calls to pg_putcopydata().  So in that sense,
			## our reading the file a row at a time is a good match to the available database facilities.

			# Oddly, Perl doesn't seem to have any way for me to detect file-reading errors at this stage.
			# I guess we'll only discover that later, when the file is closed.
			while (my $data = <DUMP>) {
			    ## FIX LATER:  The documentation says nothing about the return value of the pg_putcopydata()
			    ## call, but it should.  Report that upstream.  Here we just assume it must be true on success,
			    ## false on failure.
			    if ( not $dbh->pg_putcopydata($data) ) {
				my $errstr = $dbh->errstr;
				chomp $errstr if defined $errstr;
				$errstr = 'unknown condition' if not defined $errstr;
				log_timed_message "ERROR:  Cannot copy data into table ($errstr); aborting injection into table $table!";
				$injection_is_good = 0;
				## There's no sense in going further once we see an error.  Better to abort quickly and stop this nonsense.
				last;
			    }
			    else {
				## Remember, at this point we're only loading the data into a staging area,
				## so we don't count injected rows just yet.  But we do count copied rows,
				## to allow us to compute the overall speed of copying.
				++$total_rows_copied;
			    }
			}

			# The documentation is not clear whether we need to call pg_putcopyend() if the copy failed
			# at an earlier stage, but it can't hurt, so we do it unconditionally here.
			#
			# We are assuming that testing the pg_putcopyend() return value here is sufficient to tell us
			# whether the entire insert succeeded or failed.  Presumably, the fact that we are running the
			# database connection in auto-commit mode will cause the data to be committed at this time.
			# Actually, as long as the insertion worked, we don't care whether the data is committed, since
			# this is just a temporary table that will disappear at the end of this session.  It only needs
			# to last as long as the upsert/indate actions that will happen later in this session.
			if ( not $dbh->pg_putcopyend() ) {
			    my $errstr = $dbh->errstr;
			    chomp $errstr if defined $errstr;
			    $errstr = 'unknown condition' if not defined $errstr;
			    log_timed_message "ERROR:  Cannot copy data into table ($errstr); aborting injection into table $table!";
			    $injection_is_good = 0;
			}
		    }
		}

		capture_timing(\$copy_end_time);
		$total_copy_time += $copy_end_time - $copy_start_time;

		if ( not close DUMP ) {
		    log_timed_message "ERROR:  Cannot close file \"$target_path\" ($!); aborting injection into table $table!";
		    $injection_is_good = 0;
		}

		if ($injection_is_good) {
		    # Originally, we thought we would need to define some PostgreSQL functions here, to accomplish the data
		    # merging.  If we did need such functions here, we would create them in the pg_temp schema (pg_temp is
		    # an alias for whatever pg_temp_NNN schema is actually in use for your session for creating temporary
		    # tables or other objects, once you create such an object).  We would do so both so they are essentially
		    # invisible to other database users (although they can go searching through all the pg_temp_NNN schemas,
		    # find yours, and use such a function), and because the function definition would automatically disappear
		    # once your session ended.  (We should perhaps test to see what happens if PostgreSQL has anything like
		    # MySQL's automatic reconnection attempts, if a connection drops -- that might disturb our usage if we
		    # cannot then see the function.  But the damage is probably slight -- the affected data would simply be
		    # processed in the next archiving run instead.)
		    #
		    # A big advantage of using the temporary schema is that we never have to worry about replacing any existing
		    # function of the same name.  This allows us to fix these function definitions in future versions of this
		    # script without needing to deal with cleanup of old function versions.

		    # It turns out we really want a MERGE, not an UPSERT.  In its classical incarnation, an UPSERT seems
		    # to be oriented around sticking one row at a time into the target table.  But what we want is a bulk
		    # UPSERT, and not from externally specified data, but from data already existing in some other table.
		    # This is what MERGE is for.  But PostgreSQL 9.X (up through 9.2, at least) has neither MERGE nor
		    # UPSERT native support.  So we have to build what we need out of more primitive tools.  Here is the
		    # basic approach we need, in dumb-as-rocks fashion.
		    #
		    #    BEGIN WORK;
		    #    SET TRANSACTION ISOLATION LEVEL SERIALIZABLE;
		    #    LOCK TABLE $table;
		    #
		    #    -- The UPDATE should skip the $index_field, but capture all other fields in $table.
		    #    UPDATE $table SET col1 = t.col1, col2 = t.col2, ... FROM $temp_table t WHERE $temp_table.$index_field = $table.index_field;
		    #
		    #    -- The INSERT must be expressed in a form which is very efficient to execute; this satisfies that goal.
		    #    INSERT INTO "$table" (
		    #        SELECT tt.* FROM "$temp_table" AS tt
		    #        LEFT JOIN "$table" AS t USING ({their common primary-key columns})
		    #        WHERE t."primary-key-column-1" IS NULL [AND t."primary-key-column-2" IS NULL] ...
		    #    )
		    #
		    #    -- A COMMIT releases the lock, too; it also implicitly does a ROLLBACK instead, if some previous
		    #    -- command in the transaction failed.  But we are more careful than that; we check the success of
		    #    -- each of the commands in the transaction, and if we sense a failure, we don't try to execute any
		    #    -- more commands in the transaction, and perform an explicit ROLLBACK here instead of a COMMIT.
		    #    COMMIT;
		    #    --check the exit code of the commit to see if the whole transaction worked
		    #
		    # Now, I could easily be wrong about the details here -- what transaction isolation level is required,
		    # what locking mode is required (which may depend on the transaction isolation level), exactly what
		    # INSERT and UPDATE queries to use, etc.  We shouldn't need to lock the $temp_table, because it is in
		    # our own private temp-table space -- though it might not hurt, and we should verify that no other
		    # session can see our table.  We should probably always acquire the locks in $temp_table, $table order,
		    # for consistency.

		    # A given table can have more than one field in its primary key; we take that into account when forming
		    # the commands below.
		    my @unique_key_fields = @{ $all_table_key_fields{$table} };
		    my %is_key_field      = map { $_ => 1 } @unique_key_fields;

		    # First, we need a list of all the column names in the table, so we can form the complete UPDATE
		    # command below.
		    #
		    # In MySQL, this would be the following (and then you would need to extract the "Field" column from
		    # this more-extensive data):
		    #    show columns from `$table`
		    # In PostgreSQL, you can get just the field names without any extra data from the query:
		    #    select column_name from information_schema.columns
		    #    where table_catalog = current_catalog and table_schema = current_schema and table_name='$table'
		    #    order by ordinal_position
		    # Here we don't necessarily care about the ordering of the column names, but if we do, that last clause
		    # puts them in order.
		    my @non_key_column_names = ();
		    ## We need to exclude the startvalidtime and endvalidtime columns from the list of @non_key_column_names
		    ## we are constructing here, because that list will be used to match columns in $table and $temp_table,
		    ## and the latter never contains these columns (for good reason).  We could perform this exclusion in
		    ## any of several ways:  (a) probe the structure of $temp_table instead of $table, (b) ignore such names
		    ## when we qualify the returned values from this query to decide whether to push them onto the list, or
		    ## (c) rig the query to recognize and ignore these column names in the first place.  For no particular
		    ## reason, we choose the last approach.
		    $query = "
			select column_name from information_schema.columns
			where
			    table_catalog = current_catalog  and
			    table_schema  = current_schema   and
			    table_name    = '$table'         and
			    column_name  != 'startvalidtime' and
			    column_name  != 'endvalidtime'
			order by ordinal_position
		    ";
		    log_timed_message "NOTICE:  Finding column names in table $table ...";
		    if (not ($sth = $dbh->prepare($query))) {
			my $errstr = $dbh->errstr;
			chomp $errstr if defined $errstr;
			$errstr = 'unknown condition' if not defined $errstr;
			log_timed_message "ERROR:  Cannot fetch column names for table \"$table\" ($errstr).";
			$injection_is_good = 0;
		    }
		    else {
			if ( not defined $sth->execute ) {
			    my $errstr = $sth->errstr;
			    chomp $errstr if defined $errstr;
			    $errstr = 'unknown condition' if not defined $errstr;
			    log_timed_message "ERROR:  Cannot fetch column names for table \"$table\" ($errstr).";
			    $injection_is_good = 0;
			}
			else {
			    while ( my @values = $sth->fetchrow_array() ) {
				push @non_key_column_names, $values[0] if not exists $is_key_field{ $values[0] };
			    }
			    ## Testing of $sth->err is the approved mechanism for checking for errors here, but the
			    ## particular values it returns are database-specific, per the DBI specification.  See
			    ## DBD::Pg for the interpretation of specific values for PostgreSQL.  However, you must
			    ## also recognize that the current (DBD::Pg v2.19.3) documentation is simply wrong about
			    ## the values returned by the err() and errstr() routines.  They are generally both undef
			    ## when no error has occurred; the DBD::Pg module does *not* override this initial value
			    ## set by the DBI module before each command, when the command has succeeded.
			    my $err = $sth->err;
			    if ( defined($err) and $err != 2 ) {
				my $errstr = $sth->errstr;
				chomp $errstr if defined $errstr;
				$errstr = 'unknown condition' if not defined $errstr;
				log_timed_message "ERROR:  Cannot fetch column names for table \"$table\" ($errstr).";
				$injection_is_good = 0;
			    }
			}
			if (not $sth->finish) {
			    my $errstr = $sth->errstr;
			    chomp $errstr if defined $errstr;
			    $errstr = 'unknown condition' if not defined $errstr;
			    log_timed_message "ERROR:  Cannot fetch column names for table \"$table\" ($errstr).";
			    $injection_is_good = 0;
			}
		    }

		    my @column_assignments = map { "\"$_\"=\"$temp_table\".\"$_\"" } @non_key_column_names;
		    my @null_key_comparisons = map { "\"$table\".\"$_\"=\"null_$table\".\"$_\""} @unique_key_fields;
		    my @temp_key_comparisons = map { "\"$table\".\"$_\"=\"$temp_table\".\"$_\""} @unique_key_fields;

		    # The LOCK statement here will, by itself, wait forever to obtain a lock if it is not immediately
		    # available.  There is apparently no way to specify a timeout directly in the SQL language.  But
		    # of course, that's really dumb -- you want that kind of control at the application level.  We
		    # have tried to use our usual alarm() and POSIX::RT::Timer constructions around this statement,
		    # to no avail.  Fortunately, PostgreSQL provides a transaction-level statement_timeout parameter
		    # that we can tweak before the lock and reset immediately afterward, to see the timeout behavior
		    # we desire.  See the code below for details.
		    #
		    my $lock_tables_statement = "LOCK TABLE $temp_table, $table IN EXCLUSIVE MODE";

		    # For tables in the archive table with timed row types, we must find rows where $table.endvalidtime
		    # IS NULL but no corresponding row appears in the incoming data.  In those rows, we must now mark
		    # the $table.endvalidtime by changing it to some recent timestamp, to indicate the end of the valid
		    # time interval for that row.
		    #
		    # We will only obsolete rows in tables with timed row_types, which will have startvalidtime and
		    # endvalidtime fields attached.  For those tables, the row-obsolete statement is supposed to check
		    # to see if a given row in the archive table with a NULL endvalidtime also exists in the temporary
		    # archive table, and if not, the endvalidtime timestamp should be set to $dump_timestamp.  For
		    # untimed tables, which will not have these extra startvalidtime and endvalidtime fields attached,
		    # we will perform no such maintenance, so all we need there is a simple innocuous statement.
		    #
		    # We need something like this, except using an efficient join, not an inefficient subselect:
		    #
		    #     UPDATE \"$table\" SET endvalidtime = '$dump_timestamp'
		    #     WHERE $primarykeyfields NOT IN (select $primarykeyfields from $temp_table)
		    #     AND \"$table\".endvalidtime IS NULL
		    #
		    # The example commands in this comment below obviously need to be modified to use some variants of
		    # join(',',@unique_key_fields) and join(' AND ',@null_key_comparisons) in several places.  The somewhat
		    # frustrating part of these statements is that the PostgreSQL 9.1 manual says that in the join implied
		    # by the FROM keyword in the UPDATE command, the $table will be effectively joined to the null_$table,
		    # but it doesn't specify anything about exactly how that join is supposed to happen (what fields are
		    # implicitly matched, and such, except insofar as the type and details of the join are effectively
		    # specified by the explicit conditions in the WHERE clause).
		    #
		    # We update the endvalidtime with $dump_timestamp (the data-capture time for this specific table on the
		    # sending side) instead of $archive_end_timestamp.  We do this in the same spirit that we invoke when
		    # the $dump_timestamp is used to populate startvalidtime values for newly inserted rows.  That choice
		    # is intentional, because the $archive_end_timestamp actually applies to the secondary tables, not the
		    # timed tables.  (One can argue the point a bit, since the timed tables will be joined to the secondary
		    # tables for useful reports, and we're looking for a consistent snapshot of all the data.  Perhaps we
		    # will revisit this decision.  The present code does have both timestamps available for use if need be.)
		    #
		    # In PostgreSQL, we could use:
		    #
		    #     "WITH \"null_$table\" AS (
		    #         SELECT t.@unique_key_fields
		    #         FROM \"$table\" AS t LEFT JOIN \"$temp_table\" AS tt USING (@unique_key_fields)
		    #         WHERE tt.@unique_key_fields IS NULL and t.endvalidtime IS NULL
		    #     )
		    #     UPDATE \"$table\" SET endvalidtime = '$dump_timestamp'
		    #     FROM \"null_$table\"
		    #     WHERE @null_key_comparisons AND \"$table\".endvalidtime IS NULL"
		    #
		    # or alternatively:
		    #
		    #     "UPDATE \"$table\" SET endvalidtime = '$dump_timestamp'
		    #     FROM (
		    #         SELECT t.@unique_key_fields
		    #         FROM \"$table\" AS t LEFT JOIN \"$temp_table\" AS tt USING (@unique_key_fields)
		    #         WHERE tt.@unique_key_fields IS NULL and t.endvalidtime IS NULL
		    #     ) AS \"null_$table\"
		    #     WHERE @null_key_comparisons AND \"$table\".endvalidtime IS NULL"
		    #
		    # We use a "$table.endvalidtime IS NULL" condition at the end of the outer WHERE clause, not just an
		    # equivalent condition in the inner WHERE clause, to prevent already-expired rows from also having
		    # their endvalidtime values updated.
		    #
		    my $obsolete_statement = $all_table_row_type{$table} =~ /^timed_/
		      ? "UPDATE \"$table\" SET endvalidtime = '$dump_timestamp'
			FROM (
			    SELECT t.\"" . join( '", t."', @unique_key_fields ) . "\"
			    FROM \"$table\" AS t LEFT JOIN \"$temp_table\" AS tt USING (" . join( ', ', @unique_key_fields ) . ")
			    WHERE tt.\"". join('" IS NULL AND tt."', @unique_key_fields) . "\" IS NULL AND t.endvalidtime IS NULL
			) AS \"null_$table\"
			WHERE " . join( ' AND ', @null_key_comparisons ) . " AND \"$table\".endvalidtime IS NULL"
		      : 'select null where 1=0';

		    # If the table consists only of the primary key (now including the startvalidtime), plus the
		    # endvalidtime field, there is nothing left to update (i.e., @column_assignments == 0), and
		    # the table row type must be "timed_association".  In that case, we simply skip any updates
		    # in this table, by executing a dummy command that invariably yields no rows affected.
		    #
		    # If the row type is "timed_object", then we update just like we do for untimed row types.
		    # In this case, there is no need to include a condition that $table.endvalidtime IS NULL,
		    # because the fact that this same row (based on primary key values) still appears in the
		    # $temp_table (which has no startvalidtime and endvalidtime fields) demonstrates that the
		    # row should not yet be marked as expired in the archive database.  And thus there is no
		    # need to touch either of the existing $table.startvalidtime or $table.endvalidtime fields.
		    #
		    my $update_statement =
		      $all_table_row_type{$table} eq 'timed_association'
		      ? 'select null where 1=0'
		      : "UPDATE \"$table\" SET "
		      . join( ', ', @column_assignments )
		      . " FROM \"$temp_table\" WHERE "
		      . join( ' AND ', @temp_key_comparisons );

		    # We set the startvalidtime field to the earliest time we can definitively say this row was
		    # present in the runtime database, and we allow the endvalidtime field to default to NULL.
		    # The latter is our flag that says this row is still valid in the runtime database.
		    #
		    my $startvalidtime = $all_table_row_type{$table} =~ /^timed_/ ? ", '$dump_timestamp'" : '';

		    # This insertion only handles new rows for which the unique key fields have never before
		    # appeared in the archive database.
		    #
		    # This statement does a sensible join on the two tables even without any indexes on $temp_table
		    # because all it has to do is a single full-table walk of the $temp_table, joining each row (or
		    # not) with a corresponding row in $table.  We seem to get quite good performance this way.
		    #
		    my $insert_statement =
			"INSERT INTO \"$table\" (SELECT tt.* $startvalidtime FROM \"$temp_table\" AS tt LEFT JOIN \"$table\" AS t USING ("
		      . join( ', ', @unique_key_fields )
		      . ") WHERE t.\"" . join( '" IS NULL AND t."', @unique_key_fields ) . "\" IS NULL)";

		    # This insertion only handles new rows for which the unique key fields have appeared before
		    # in the archive database, but for which all endvalidtime fields in the existing rows are now
		    # non-NULL.  We might have tried to fold this into the $insert_statement, but:
		    #
		    # (*) This statement should only be run for timed_association tables, not for timed_object
		    #     tables (see below).
		    # (*) Trying to combine the statements might result in a rather large and messy single INSERT
		    #     statement, whose overall execution would be that much harder to understand.
		    # (*) We would like to track the statistics for reincarnated rows separate from the statistics
		    #     for newly inserted rows.  This is not a high-priority desire, especially given that we
		    #     don't expect reincarnation to happen very often, but it helps us understand how these
		    #     queries are operating if we separate their actions.
		    # (*) I think it makes sense to have separate reincarnation processing as the dual of the
		    #     obsoletion processing above.  This complementarity makes the code easier to understand.
		    #
		    # The thing is, for any $table that uses unique-id fields to identify objects that specifically
		    # reside in that table (that is, any $table with a row_type of "timed_object"), the reincarnation
		    # query should do exactly nothing.  That's because any reincarnated object should appear with
		    # a new set of unique-id key fields that should specifically identify this object as never having
		    # appeared before in the database.  But if that is indeed the case, then such a row should have
		    # already been added by the $insert_statement, and the $reincarnate_statement should have nothing
		    # to do.  So the only case where this should have any effect is that of a $table with a row_type
		    # of "timed_association", where the row represents an association of other objects, not an object
		    # unto itself.  In this case, the preceding INSERT will not have added the revived association,
		    # because it only handles cases where the association itself (i.e., that set of unique-key values)
		    # has never before been present in the table.

		    # A correctly working $reincarnate_statement is strikingly hard to compose without direct reference
		    # to all of the columns in the $table we are inserting into.  What makes it possible is the fact
		    # that we only need reincarnation for a timed_association $table, and for such a table, we are
		    # guaranteed that @non_key_column_names == 0, so we can construct the column names that need to be
		    # inserted from the @unique_key_fields plus the startvalidtime and endvalidtime columns.  But just
		    # to be safe against future modifications, let's check that condition here.
		    if ( $all_table_row_type{$table} eq 'timed_association' and @non_key_column_names != 0) {
			log_timed_message "ERROR:  Table \"$table\" is marked as timed_association but has non-key columns.";
			$injection_is_good = 0;
		    }

		    # We're very careful here to name the columns we're inserting into $table, to prevent any mistakes
		    # if the @unique_key_fields are somehow not specified in the same order they appear in the $table.
		    # The key point is that we do not specify an explicit endvalidtime value in the insertion, so it is
		    # left to default to NULL, making this an active row.
		    #
		    # The inner SELECT joins all the rows in $table to $temp_table, but then qualifies that join (via
		    # the ORDER BY and DISTINCT ON clauses) by taking only the latest copy in each logical group of rows
		    # (identified by @unique_key_fields) in $table.  The outer SELECT then further qualifies the rows we
		    # get back from the inner SELECT, by choosing only those latest rows that represent an inactive object
		    # (endvalidtime IS NOT NULL).  If that is so, this logical row has previously been obsoleted, and we
		    # know there is no active copy still around.  In that case, we retain the row from $temp_table for
		    # insertion into $table (with a NULL endvalidtime, thereby making the new row an active copy).
		    #
		    my $reincarnate_statement =
		      $all_table_row_type{$table} eq 'timed_association'
		      ? "INSERT INTO \"$table\" (\"" . join( '", "', @unique_key_fields ) . "\", startvalidtime)"
		      . "  SELECT \"" . join( '", "', @unique_key_fields ) . "\" $startvalidtime AS startvalidtime FROM ("
		      . "    SELECT DISTINCT ON (t.\"" . join( '", t."', @unique_key_fields ) . "\") tt.*, t.endvalidtime"
		      . "    FROM \"$temp_table\" AS tt INNER JOIN \"$table\" AS t USING (\"" . join( '", "', @unique_key_fields ) . "\")"
		      . "    ORDER BY t.\"" . join( '", t."', @unique_key_fields ) . "\", t.endvalidtime DESC NULLS FIRST"
		      . "  ) AS latest_object_row"
		      . "  WHERE latest_object_row.endvalidtime IS NOT NULL"
		      : 'select null where 1=0';

		    # We run the updates before the inserts, because that way there will be fewer rows to search
		    # through in the target table when we look for rows to update.  As the target table grows large,
		    # the improvement will grow relatively small, but we may as well take advantage of it.

		    my $transaction_started = 0;
		    if ($injection_is_good) {
			## We count the transaction as having been started even if the begin_work() call fails; this
			## flag is mostly a record that we got to this point where we tried to begin the transaction.
			## This simply provides the condition for controlling whether we later execute a rollback()
			## if some earlier or later database access has failed.  If we get to this point and the
			## begin_work() call fails, we won't do any more work until the rollback, which will also
			## fail, but that secondary failure is okay.  It's just an insurance policy so we close out
			## the transaction in case any funny condition happened during the begin_work() where a
			## transaction was really started but it was reported here as a failure.  Stranger things
			## have happened.
			$transaction_started = 1;
			log_timed_message "NOTICE:  Starting transaction for table $table ...";
			if ( not $dbh->begin_work() ) {
			    my $errstr = $dbh->errstr;
			    chomp $errstr if defined $errstr;
			    $errstr = 'unknown condition' if not defined $errstr;
			    log_timed_message "ERROR:  Cannot start a transaction for table \"$table\" ($errstr).";
			    $injection_is_good = 0;
			}
		    }
		    if ($injection_is_good) {
			## In testing, we have sometimes had trouble getting the locks in short order, because
			## we had some old orphaned copy of this script (or more precisely, an orphaned postgres
			## process on the database server) still processing the tables we want to edit now, from
			## some previously-initiated long-running transaction.  So we capture timing statistics
			## here for the locking as well, to see how long we had to spend just waiting.
			my $lock_start_time;
			my $lock_end_time;

			# This detailed level of logging, here and in related code segments below, is inherently
			# uninteresting.  We only emit these messages because they give an indication in the log
			# file as to where the code was hanging, if one of these operations takes a long time and
			# the script gets interrupted while in this process of injecting data.
			log_timed_message "NOTICE:  Locking tables before processing table $table ...";
			log_timed_message "DEBUG:  Lock statement is:\n$lock_tables_statement" if $debug_basic;
			capture_timing(\$lock_start_time);

			# By coding experiment, we find that neither wrapping the lock statement in an alarm()
			# construction nor wrapping it in a POSIX::RT::Timer construction suffices to kick
			# a hung locking attempt out of its slumber.  But the PostgreSQL client-connection
			# statement_timeout parameter will do that for us.  We make this setting local to the
			# transaction, then further constrain it to just the lock statement by resetting it
			# after we have fully processed the lock statement.  (We wait to perform the reset
			# until after we have captured the error status from the lock statement.)

			my $timed_out = 0;
			if (not defined $dbh->do("SET LOCAL statement_timeout = $table_locking_timeout_ms")) {
			    my $errstr = $dbh->errstr;
			    chomp $errstr if defined $errstr;
			    $errstr = 'unknown condition' if not defined $errstr;
			    log_timed_message "ERROR:  Cannot set statement_timeout before locking table \"$table\" ($errstr).";
			    $injection_is_good = 0;
			}
			elsif ( not defined $dbh->do($lock_tables_statement) ) {
			    my $errstr = $dbh->errstr;
			    chomp $errstr if defined $errstr;
			    $errstr = 'unknown condition' if not defined $errstr;
			    log_timed_message "ERROR:  Cannot lock tables (\"$temp_table\" or \"$table\") ($errstr).";
			    log_timed_message "        Lock statement is:\n$lock_tables_statement" if not $debug_basic;
			    $timed_out = ($errstr =~ /canceling statement due to statement timeout/);
			    $injection_is_good = 0;
			}
			elsif (not defined $dbh->do("SET LOCAL statement_timeout = DEFAULT")) {
			    my $errstr = $dbh->errstr;
			    chomp $errstr if defined $errstr;
			    $errstr = 'unknown condition' if not defined $errstr;
			    log_timed_message "ERROR:  Cannot reset statement_timeout before modifying data in table \"$table\" ($errstr).";
			    $injection_is_good = 0;
			}

			# If we do get a lock timeout, we run an additional query to try to identify what activity is blocking
			# us by holding a lock.  It will be up to the administrator to use the process ID information we spill
			# out here to advantage, by further relating it to whatever else is going on in the system, and taking
			# whatever action is deemed necessary to correct the problem.
			if ($timed_out) {
			    ## We perform a rollback on the current transaction so we can once again get access
			    ## to the database for the following diagnostic queries.  We are assured by our having
			    ## set the $injection_is_good flag above, and the checking of that flag below, that
			    ## this won't inadvertently cause any SQL commands below to alter the database outside
			    ## of a locked transaction.  Note also that the rollback will automatically disable the
			    ## non-default LOCAL statement_timeout we put in place before the lock attempt, so we
			    ## don't need to take that action explicitly here.  (Logically, I suppose we might have
			    ## to if the rollback failed, but if we're in this situation, the entire archiving cycle
			    ## will be aborted, so it wouldn't matter much then.)
			    my $warnstr  = undef;
			    my $rollback = undef;
			    do {
				## We get a "rollback ineffective with AutoCommit enabled" warning along with a failed rollback()
				## call, but not an error, when that condition arises.  We have to go to extraordinary measures
				## to capture the warning message in this situation so we can log it, because no error condition
				## or message is reported via the usual $dbh->err() or $dbh->errstr() routines, and there is no
				## corresponding $dbh->warn() routine and no $dbh->warnstr() routine to allow the application to
				## directly sense the warning state and retrieve the warning message.
				local $SIG{__WARN__} = sub { $warnstr = $_[0]; };
				$rollback = $dbh->rollback();
			    };
			    if ( not $rollback ) {
				my $message = $dbh->errstr;
				$message = $warnstr            if not defined $message;
				$message = 'unknown condition' if not defined $message;
				chomp $message;
				log_timed_message "ERROR:  Cannot roll back transaction on table \"$table\" ($message).";
			    }

			    # Avoid running another rollback for the original transaction later on.
			    $transaction_started = 0;

			    # FIX LATER:  Assuming that the database itself runs on the $archive_gwservices_machine (i.e., that
			    # is where we will find the "postgres" server processes), we could use that variable along with
			    # $archive_gwservices_machine_is_remote to reach out and perform a "ps" listing on each process
			    # we find that has a lock on the $table, to reveal some details about the client that process is
			    # connected to, and what it is doing.  For instance, "ps -efl" on such a process reveals:
			    #
			    #   1 R postgres 9500 14233 90 80 0 - 104018 - 12:00 ? 05:05:44 postgres: collage archive_gwcollagedb 192.168.117.202(43982) INSERT
			    #
			    # This tells us the client address and process, and what type of query is being run.  It could
			    # be valuable to collect this type of information at the time of failure, because typically this
			    # scripting will be run in the dead of night and the evidence will have disappeared by the time
			    # anyone looks at the problem during the daylight hours.  So such detail should be logged for
			    # later inspection.
			    $query = "
				select l.pid, l.mode from pg_locks l, pg_database d, pg_class c
				where d.datname='$archive_dbname' and l.database = d.oid
				and c.relname = '$table' and l.relation = c.oid
				and l.granted=true
			    ";
			    log_timed_message "NOTICE:  Finding processes holding locks on table $table ...";
			    if (not ($sth = $dbh->prepare($query))) {
				my $errstr = $dbh->errstr;
				chomp $errstr if defined $errstr;
				$errstr = 'unknown condition' if not defined $errstr;
				log_timed_message "ERROR:  Cannot find processes holding locks on table \"$table\" ($errstr).";
			    }
			    else {
				if ( not defined $sth->execute ) {
				    my $errstr = $sth->errstr;
				    chomp $errstr if defined $errstr;
				    $errstr = 'unknown condition' if not defined $errstr;
				    log_timed_message "ERROR:  Cannot find processes holding locks on table \"$table\" ($errstr).";
				}
				else {
				    while ( my @values = $sth->fetchrow_array() ) {
					log_message "INFO:  On the archive database machine, process $values[0] holds "
					  . ($values[1] =~ /^[aeiou]/i ? 'an' : 'a') . " $values[1] on the $table table.";
				    }
				    ## Testing of $sth->err is the approved mechanism for checking for errors here, but the
				    ## particular values it returns are database-specific, per the DBI specification.  See
				    ## DBD::Pg for the interpretation of specific values for PostgreSQL.  However, you must
				    ## also recognize that the current (DBD::Pg v2.19.3) documentation is simply wrong about
				    ## the values returned by the err() and errstr() routines.  They are generally both undef
				    ## when no error has occurred; the DBD::Pg module does *not* override this initial value
				    ## set by the DBI module before each command, when the command has succeeded.
				    my $err = $sth->err;
				    if ( defined($err) and $err != 2 ) {
					my $errstr = $sth->errstr;
					chomp $errstr if defined $errstr;
					$errstr = 'unknown condition' if not defined $errstr;
					log_timed_message "ERROR:  Cannot find processes holding locks on table \"$table\" ($errstr).";
				    }
				}
				if (not $sth->finish) {
				    my $errstr = $sth->errstr;
				    chomp $errstr if defined $errstr;
				    $errstr = 'unknown condition' if not defined $errstr;
				    log_timed_message "ERROR:  Cannot find processes holding locks on table \"$table\" ($errstr).";
				}
			    }
			}

			# No rows can be affected by a simple table lock, so we don't count any here.

			capture_timing(\$lock_end_time);
			## We don't currently save the lock time on a per-table basis, but we could do that
			## easily enough here if it seemed important.
			$total_lock_time += $lock_end_time - $lock_start_time;
		    }

		    if ($injection_is_good) {
			my $obsolete_start_time;
			my $obsolete_end_time;
			log_timed_message "NOTICE:  Obsoleting old rows in table $table ...";
			log_timed_message "DEBUG:  Obsolete statement is:\n$obsolete_statement" if $debug_basic;
			capture_timing(\$obsolete_start_time);
			my $rows_affected = $dbh->do($obsolete_statement);
			if ( not defined $rows_affected ) {
			    my $errstr = $dbh->errstr;
			    chomp $errstr if defined $errstr;
			    $errstr = 'unknown condition' if not defined $errstr;
			    log_timed_message "ERROR:  Cannot obsolete rows in table \"$table\" ($errstr).";
			    log_timed_message "        Obsolete statement is:\n$obsolete_statement" if not $debug_basic;
			    $injection_is_good = 0;
			}
			elsif ( $rows_affected > 0 ) {
			    $injected_rows += $rows_affected;
			    $obsoleted_rows = $rows_affected;
			}
			capture_timing(\$obsolete_end_time);
			## We don't currently save the obsolete time on a per-table basis,
			## but we could do that easily enough here if it seemed important.
			$total_obsolete_time += $obsolete_end_time - $obsolete_start_time;
		    }

		    if ($injection_is_good) {
			my $update_start_time;
			my $update_end_time;
			log_timed_message "NOTICE:  Updating old rows in table $table ...";
			log_timed_message "DEBUG:  Update statement is:\n$update_statement" if $debug_basic;
			capture_timing(\$update_start_time);
			my $rows_affected = $dbh->do($update_statement);
			if ( not defined $rows_affected ) {
			    my $errstr = $dbh->errstr;
			    chomp $errstr if defined $errstr;
			    $errstr = 'unknown condition' if not defined $errstr;
			    log_timed_message "ERROR:  Cannot update rows in table \"$table\" ($errstr).";
			    log_timed_message "        Update statement is:\n$update_statement" if not $debug_basic;
			    $injection_is_good = 0;
			}
			elsif ( $rows_affected > 0 ) {
			    $injected_rows += $rows_affected;
			    $updated_rows = $rows_affected;
			}
			capture_timing(\$update_end_time);
			## We don't currently save the update time on a per-table basis,
			## but we could do that easily enough here if it seemed important.
			$total_update_time += $update_end_time - $update_start_time;
		    }
		    if ($injection_is_good) {
			my $insert_start_time;
			my $insert_end_time;
			capture_timing(\$insert_start_time);
			log_timed_message "NOTICE:  Inserting new rows into table $table ...";
			log_timed_message "DEBUG:  Insert statement is:\n$insert_statement" if $debug_basic;
			my $rows_affected = $dbh->do($insert_statement);
			if ( not defined $rows_affected ) {
			    ## FIX LATER:  If we get errors like the following, we should attempt to locate and log the
			    ## respective primary-key values in the two tables, to make it easier to diagnose the problem.
			    ## Such problems are particularly difficult to diagnose because the temporary table is, well,
			    ## temporary.  And by the time you go to investigate the root of the problem, it has evaporated
			    ## along with all of the obvious evidence.  Although such evidence can still be found in the
			    ## file that was used to populate the temporary table, it's not obvious that you should look
			    ## there, and not immediately obvious what to look for in either the file or the still-extant
			    ## permanent table.
			    ##     DBD::Pg::db do failed: ERROR:  duplicate key value violates unique constraint "severity_name_key"
			    ##     DETAIL:  Key (name)=(ACKNOWLEDGEMENT (WARNING)) already exists. at /usr/local/groundwork/core/archive/bin/log-archive-receive.pl line 1257.
			    my $errstr = $dbh->errstr;
			    chomp $errstr if defined $errstr;
			    $errstr = 'unknown condition' if not defined $errstr;
			    log_timed_message "ERROR:  Cannot insert rows into table \"$table\" ($errstr).";
			    log_timed_message "        Insert statement is:\n$insert_statement" if not $debug_basic;
			    $injection_is_good = 0;
			}
			elsif ( $rows_affected > 0 ) {
			    $injected_rows += $rows_affected;
			    $inserted_rows = $rows_affected;
			}
			capture_timing(\$insert_end_time);
			## We don't currently save the insert time on a per-table basis,
			## but we could do that easily enough here if it seemed important.
			$total_insert_time += $insert_end_time - $insert_start_time;
		    }
		    if ($injection_is_good) {
			my $reincarnate_start_time;
			my $reincarnate_end_time;
			capture_timing(\$reincarnate_start_time);
			log_timed_message "NOTICE:  Reincarnating new rows in table $table ...";
			log_timed_message "DEBUG:  Reincarnate statement is:\n$reincarnate_statement" if $debug_basic;
			my $rows_affected = $dbh->do($reincarnate_statement);
			if ( not defined $rows_affected ) {
			    ## FIX MAJOR:  Does this next comment have anything to do with the reincarnation processing?
			    ## FIX LATER:  If we get errors like the following, we should attempt to locate and log the
			    ## respective primary-key values in the two tables, to make it easier to diagnose the problem.
			    ## Such problems are particularly difficult to diagnose because the temporary table is, well,
			    ## temporary.  And by the time you go to investigate the root of the problem, it has evaporated
			    ## along with all of the obvious evidence.  Although such evidence can still be found in the
			    ## file that was used to populate the temporary table, it's not obvious that you should look
			    ## there, and not immediately obvious what to look for in either the file or the still-extant
			    ## permanent table.
			    ##     DBD::Pg::db do failed: ERROR:  duplicate key value violates unique constraint "severity_name_key"
			    ##     DETAIL:  Key (name)=(ACKNOWLEDGEMENT (WARNING)) already exists. at /usr/local/groundwork/core/archive/bin/log-archive-receive.pl line 1257.
			    my $errstr = $dbh->errstr;
			    chomp $errstr if defined $errstr;
			    $errstr = 'unknown condition' if not defined $errstr;
			    log_timed_message "ERROR:  Cannot reincarnate rows into table \"$table\" ($errstr).";
			    log_timed_message "        Reincarnate statement is:\n$reincarnate_statement" if not $debug_basic;
			    $injection_is_good = 0;
			}
			elsif ( $rows_affected > 0 ) {
			    $injected_rows    += $rows_affected;
			    $reincarnated_rows = $rows_affected;
			}
			capture_timing(\$reincarnate_end_time);
			## We don't currently save the reincarnate time on a per-table basis,
			## but we could do that easily enough here if it seemed important.
			$total_reincarnate_time += $reincarnate_end_time - $reincarnate_start_time;
		    }

		    if ($injection_is_good) {
			log_timed_message "NOTICE:  Committing changes to table $table ...";
			if ( not $dbh->commit() ) {
			    my $errstr = $dbh->errstr;
			    chomp $errstr if defined $errstr;
			    $errstr = 'unknown condition' if not defined $errstr;
			    log_timed_message "ERROR:  Cannot commit changes to table \"$table\" ($errstr).";
			    $injection_is_good = 0;
			}
		    }
		    elsif ($transaction_started) {
			log_timed_message "NOTICE:  Rolling back changes to table $table ...";
			## Note that in the case of failure, we do not currently back out any increments we
			## made above to counts of injected, updated, or inserted rows.  In that sense, those
			## statistics might be a bit misleading in the case of a failed run.  One can argue
			## the point; we did take those actions, even though we subsequently roll them back.
			## So does that count, for statistical purposes?  It depends on your point of view --
			## whether you're trying to count for overall work and speed calculations, or whether
			## you're trying to count only the successful changes to the database.
			##
			## Note that this treatment differs from how we are currently collecting equivalent
			## data on the sending side.  There, we only count captured rows for a given table
			## if a complete dump was successfully generated.  Perhaps in some future release,
			## this discrepancy will be resolved.

			my $warnstr  = undef;
			my $rollback = undef;
			do {
			    ## We get a "rollback ineffective with AutoCommit enabled" warning along with a failed rollback()
			    ## call, but not an error, when that condition arises.  We have to go to extraordinary measures
			    ## to capture the warning message in this situation so we can log it, because no error condition
			    ## or message is reported via the usual $dbh->err() or $dbh->errstr() routines, and there is no
			    ## corresponding $dbh->warn() routine and no $dbh->warnstr() routine to allow the application to
			    ## directly sense the warning state and retrieve the warning message.
			    local $SIG{__WARN__} = sub { $warnstr = $_[0]; };
			    $rollback = $dbh->rollback();
			};
			if ( not $rollback ) {
			    my $message = $dbh->errstr;
			    $message = $warnstr            if not defined $message;
			    $message = 'unknown condition' if not defined $message;
			    chomp $message;
			    log_timed_message "ERROR:  Cannot roll back changes to table \"$table\" ($message).";
			    $injection_is_good = 0;
			}
		    }

		    # We need to understand the appropriate discipline to follow with regard to the PostgreSQL sequences
		    # which are used to implement what MySQL calls auto-increment columns.
		    #
		    # If we were to update any PostgreSQL sequences associated with $table, we would first need to
		    # locate all such sequences (by poking around in information_schema), then find their respective
		    # proper new values, then update those values in the database.  Note that it makes no sense to try
		    # to capture the current values of the sequences in the runtime database and inject them into the
		    # archive database, because each time we run the sending script, we will in general not be capturing
		    # the very latest table data in the runtime database, but the current values of the sequences will
		    # have already been updated to reflect later rows in the runtime table.  So the best we could do
		    # would be to calculate max($column_name) on the archive table after this cycle of new data is
		    # injected, and use that to set the sequence value:
		    #
		    #     select setval('$sequence_name', max("$column_name")) from $table;
		    #
		    # where $sequence_name is drawn from the modifier for the $table.$column_name (e.g., "not null
		    # default nextval('logmessage_logmessageid_seq'::regclass)" yields logmessage_logmessageid_seq as
		    # the $sequence_name).  This would, of course, require a full-table walk to find the maximum value,
		    # unless a clever PostgreSQL implementation performs this calculation far faster by just looking
		    # at the last value in the PRIMARY KEY index which is very likely defined on $column_name.  (One
		    # would hope the database would take the latter approach, because a full-table walk on one of our
		    # ever-growing tables would be an ever-more-expensive calculation over time.)
		    #
		    # However, before we get to that stage, we need to step back and examine the rationale for wanting
		    # to maintain such sequences relatively up-to-date in the archive database in the first place.  The
		    # fact is, such sequences are there in the runtime database to be used when inserting new rows for
		    # which a unique ID is not already established.  In such insert statements, the ID column is either
		    # not specified at all or is specified as the special keyword DEFAULT.  Either formulation causes
		    # the database to run nextval('$sequence_name') to find the appropriate value to stuff into the new
		    # row.  This not only provides an identifier for the row, it also prevents duplicate-key collisions.
		    # But over here in the archive database, we never want any source other than this receiving script
		    # to insert new rows.  If such insertions from other sources do happen, the archive database will
		    # contain local data unrelated to the runtime database, and there will eventually be a collision
		    # of sorts when new rows from the runtime database are supposed to be inserted into the archive
		    # database, but those ID values are already used.  (In practice, we would just update the rest of
		    # the data in such rows, so the database would heal itself, but we don't want such alien data there
		    # in the first place, as it would contaminate any reporting done on the archive database.)
		    #
		    # So, what are we to do to prevent such rogue insertions?  At the moment, we don't have any special
		    # active blocking in place; we just depend on a gentleman's agreement that shuts down the usual
		    # sources of new rows.  Then, since we won't really have a local use for the sequences in the
		    # archive database, we don't bother to try to update them here.  This provides a certain level of
		    # protection against rogue inserts, in that, if some data originator does try to insert a new row
		    # that implicitly uses nextval('$sequence_name'), the sequence will likely still have a low value
		    # that will conflict with an existing row already copied over from the runtime database ("ERROR:
		    # duplicate key value violates unique constraint"), and the rogue insert will fail.  It's true that
		    # if the rogue client repeats this often enough, they may exceed the range of previously-inserted
		    # data (since the failed insert will still leave $sequence_name updated with the next value, and
		    # it will climb with subsequent insert attempts), so this is not an absolute protection.  But
		    # presumably, before then, the failure of the rogue client will have been flagged and noticed by
		    # humans, and they will have disabled it.
		    #
		    # So the upshot is, this receiving script will NOT try to update sequences in the database.
		    #
		    # We could try to resolve potential conflicts with local insertions in a slightly different, clever
		    # way:  by making the sequences in the archive database be decreasing, starting with -1.  Then
		    # if any rogue insertions ever did happen, they would occupy a domain never seen in the runtime
		    # database, and there would be no direct conflict.  Of course, the data-contamination problem
		    # would still remain, so it's still not a good idea to have other clients adding data to this
		    # database.  At the present time, we have not taken any steps to change the sequences to have them
		    # be decreasing.

		    # We don't bother to capture independent timing statistics for the table-drop portion of
		    # this phase, because it is ordinarily not expected to take a significant amount of time.
		    # It's just bundled in with the total $injection_time accounting.  The only reason to
		    # look deeper would be if that statistic appeared to be out of kilter when compared with
		    # $total_copy_time + $total_lock_time + $total_update_time + $total_insert_time timing.
		    log_timed_message "NOTICE:  Dropping temporary table $temp_table ...";
		    if (not defined $dbh->do("DROP TABLE \"$temp_table\"")) {
			## In this case, we already succeeded or failed while injecting the data into the permanent table,
			## so the failure to drop the temporary table (which will be dropped automatically anyway, at the
			## end of this session) is of little consequence.  So we don't abort the entire injection of this
			## table just because of this one issue.  We do log the occurrence, though, for later forensics.
			my $errstr = $dbh->errstr;
			chomp $errstr if defined $errstr;
			$errstr = 'unknown condition' if not defined $errstr;
			log_timed_message "WARNING:  Cannot drop temporary table \"$temp_table\" ($errstr).";
		    }
		}

		if ($injection_is_good) {
		    ## We only save the statistics if the injection is good.  That means that if this table injection
		    ## failed, the end-of-run statistics won't reflect the time spent on the table up to the point of
		    ## failure, which could be a bit misleading with regard to the $row_injection_speed.  We might
		    ## address that in a future release, when we might perhaps track the injection times on a per-table
		    ## basis.  Until then, this decision seems acceptable.
		    $rows_injected{$table}     = $injected_rows;
		    $rows_obsoleted{$table}    = $obsoleted_rows;
		    $rows_updated{$table}      = $updated_rows;
		    $rows_inserted{$table}     = $inserted_rows;
		    $rows_reincarnated{$table} = $reincarnated_rows;
		    $total_rows_injected     += $injected_rows;
		    $total_rows_obsoleted    += $obsoleted_rows;
		    $total_rows_updated      += $updated_rows;
		    $total_rows_inserted     += $inserted_rows;
		    $total_rows_reincarnated += $reincarnated_rows;
		}
		else {
		    $outcome = 0;
		}

		# We were able to open the file, so we presumably have permission to clean up by removing it (after all,
		# our rule is always that the reader deletes its own incoming data [when it knows it's safely done with
		# it], never the writer).  However, at this point only one table has been processed, and we might not
		# want to delete individual files that were processed to completion until we see whether the entire set
		# of files is also processed without error.  And if we had trouble processing this file, we might want to
		# leave it around as forensic evidence to help in diagnosing the problem.  In that regard, the downside
		# of removing the file now is that it would disallow inspecting the file and potentially running the
		# receiving script by hand on this set of files.  In any case, the file should eventually be purged as
		# part of ordinary cleanup of old target-machine data files.
	    }
	    else {
		log_timed_message "ERROR:  Cannot open file \"$target_path\" ($!); aborting injection into table $table!";
		$outcome = 0;
	    }
	}
	else {
	    log_timed_message "ERROR:  Unknown dumpfile_format \"$dumpfile_format\"; aborting injection into table $table!";
	    $outcome = 0;
	}
    }
    else {
	log_timed_message "ERROR:  archive_dbtype \"$archive_dbtype\" is not supported; aborting injection into table $table!";
	$outcome = 0;
    }

    return $outcome;
}

sub inject_into_all_tables {
    my $target_directory = shift;
    my $outcome          = 1;

    # FIX LATER:  Here we are about to stop gwservices on the archive server.  We might want to
    # protect against any signals coming in and asynchronously aborting this script before we
    # get a chance to start gwservices, even if all the other database manipulations managed
    # by this routine should be aborted.
    $outcome = 0 if not stop_archive_gwservices();

    if ($outcome) {
	# We must always inject referred-to rows before we inject corresponding referencing rows, so we know
	# that we will never attempt to inject any dangling references (which ought to fail due to foreign-key
	# constraint violation).  That rule establishes the ordering in which we must process the tables.  So we
	# intentionally inject the tables in this order:  primary, secondary, tertiary.  Because the foreign-key
	# constraints will be immediately enforced, this requirement cannot be relaxed on the receiving side, even
	# if we implement some kind of snapshot-freeze in the database on the sending side before we capture data
	# from any of the tables.  So we depend not just on the array ordering given in the following foreach
	# loop, but also on the ordering given in the config file for each of the individual arrays listed in the
	# loop control, since there are some cross-table references even between tables listed in the same array.
	# If we don't want to force the config file to be in the required ordering, we would need to dive into
	# the database (that is, look at the information_schema in detail) in the compute_table_archive_order()
	# routine, find all the foreign-key references between tables, compute the correct ordering for injecting
	# the full configured set of tables, and pass the resulting all-tables sequence to here.

	foreach my $table ( @primary_tables, @secondary_tables, @tertiary_tables ) {
	    my $target_file = $matched_file{$table};
	    my $status = inject_into_database_table( "$target_directory/$target_file", $matched_dump_timestamp{$table}, $table );
	    if ($status) {
		++$total_tables_injected;
	    }
	    else {
		$outcome = 0;
		## Once we see a failure with one table, there's no point in continuing with other tables,
		## because any references to the failed table in the later tables might not be correctly
		## satisfied.
		##
		## FIX MINOR:  Should we perhaps have not committed the changes to earlier tables, and roll
		## them all back as well when we find a table that fails, along with the one failing table?
		## For now, we leave the changes in place, believing that they won't be damaging.  But this
		## decision needs to be thoroughly vetted.  If we do make just one large commit or rollback,
		## then we also need to lock all tables at once before any of the new data is injected into
		## the permanent tables, instead of locking them one-by-one as we inject data into them.
		## If we do that, we probably want to also separate the copying of data from all files into
		## temporary tables as a separate sub-phase, before any copying of data from temporary tables
		## into permanent tables.
		last;
	    }
	}
    }

    # No matter what happened earlier, we attempt to start gwservices on the archive server.
    # FIX LATER:  I suppose this might be considered potentially problematic, if you had
    # gwservices intentionally disabled and you don't want this effectively rogue process
    # coming in asynchronously and starting them back up again.  Perhaps we need to sense
    # earlier on if there was anything to stop, and only start them up again if they were
    # already running when we began this routine.
    $outcome = 0 if not start_archive_gwservices();

    return $outcome;
}

# The only changes we make to the database in this log-archive-receive.pl script are
# deletes, so there is no interesting transaction behavior we need to control explicitly.
# Therefore, we enable auto-commit on this connection, to keep our application code
# simple.  Note that if a PostgreSQL command fails under auto-commit mode, it will be
# automatically rolled back; the application does not need to take any action to make
# this happen.  (Under PostgreSQL, all changes made so far in the transaction are rolled
# back, any additional commands in the transaction are aborted as soon as the command is
# run, before they have a chance to make any changes, and the COMMIT or END that ends
# the transaction is automatically turned into a ROLLBACK; the application has no choice
# about this.  That behavior is not necessarily the case with other commercial databases,
# so this issue would need to be investigated if we ever wanted to port this code to some
# other database.)  So any failed deletes in this script will be turned into no-ops, which
# is fine; a future run of this script ought to successfully perform the same deletions,
# if the underlying problem gets resolved.
#
# If we did turn on auto-commit, then we might perhaps generate table snapshots which are
# consistent across all tables, potentially avoiding some of the out-of-sync issues that
# we are handling instead by careful ordering of the dump operations.  But for now, that
# possibility doesn't seem important enough to force us to move in that direction.
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

    my $dsn = '';
    if ( defined($archive_dbtype) && $archive_dbtype eq 'postgresql' ) {
	$dsn = "DBI:Pg:dbname=$archive_dbname;host=$archive_dbhost";
    }
    else {
	$dsn = "DBI:mysql:database=$archive_dbname;host=$archive_dbhost";
    }
    $dbh = DBI->connect( $dsn, $archive_dbuser, $archive_dbpass, { 'AutoCommit' => 1 } );
    if (!$dbh) {
	log_timed_message "ERROR:  Cannot connect to database $archive_dbname: ", $DBI::errstr;
	return 0;
    }

    return 1;
}

sub close_database_connection {
    my $outcome = 1;
    $dbh->disconnect() if $dbh;
    $dbh = undef;
    return $outcome;
}

sub capture_timing {
    my $timestamp = shift;
    $$timestamp = Time::HiRes::time();
}

sub format_hhmmss_timestamp {
    ## Taking the floor() of the value understates the actual time interval, but it looks
    ## too strange taking the ceil() instead, which overstates the time interval.  In either
    ## case, you are likely to end up with a bunch of values that don't add up to the total
    ## interval, but use of floor() seems more natural.
    my $interval = int(POSIX::floor(shift));
    my $seconds  =     $interval % SECONDS_PER_MINUTE;
    my $minutes  = int($interval / SECONDS_PER_MINUTE) % MINUTES_PER_HOUR;
    my $hours    = int($interval / SECONDS_PER_HOUR);
    return sprintf "%02d:%02d:%02d", $hours, $minutes, $seconds;
}

sub log_archiving_statistics {
    my $status_message = shift;
    my $suffix         = $status_message eq '' ? '' : " ($status_message)";
    my $cycle_status   = $cycle_outcome ? 'SUCCEEDED' : 'FAILED';
    my ( $second, $minute, $hour, $month_day, $month, $year, $week_day, $year_day, $is_dst );

    # Sleep for one full second, to move past the $script_end_timestamp computed below by
    # rounding up a high-resolution time via POSIX::ceil().  This will guarantee that any
    # subsequent log_timed_message() output that appears after our "script ended at" message
    # will have a timestamp no earlier than the timestamp specified in that message.
    select undef, undef, undef, 1.0;

    ( $second, $minute, $hour, $month_day, $month, $year, $week_day, $year_day, $is_dst ) = localtime( POSIX::floor($script_start_time) );
    my $script_start_timestamp = sprintf "%04d-%02d-%02d %02d:%02d:%02d", $year + 1900, $month + 1, $month_day, $hour, $minute, $second;
    ( $second, $minute, $hour, $month_day, $month, $year, $week_day, $year_day, $is_dst ) = localtime( POSIX::ceil($script_delete_files_end_time) );
    my $script_end_timestamp = sprintf "%04d-%02d-%02d %02d:%02d:%02d", $year + 1900, $month + 1, $month_day, $hour, $minute, $second;

    $init_time         = $script_init_end_time - $script_start_time;
    $injection_time    = $script_injection_end_time - $script_init_end_time;
    $delete_files_time = $script_delete_files_end_time - $script_injection_end_time;
    $total_time        = POSIX::ceil($script_delete_files_end_time - $script_start_time);

    $init_timestamp         = format_hhmmss_timestamp($init_time);
    $injection_timestamp    = format_hhmmss_timestamp($injection_time);
    $copy_timestamp         = format_hhmmss_timestamp($total_copy_time);
    $lock_timestamp         = format_hhmmss_timestamp($total_lock_time);
    $obsolete_timestamp     = format_hhmmss_timestamp($total_obsolete_time);
    $update_timestamp       = format_hhmmss_timestamp($total_update_time);
    $insert_timestamp       = format_hhmmss_timestamp($total_insert_time);
    $reincarnate_timestamp  = format_hhmmss_timestamp($total_reincarnate_time);
    $delete_files_timestamp = format_hhmmss_timestamp($delete_files_time);
    $total_timestamp        = format_hhmmss_timestamp($total_time);

    # All speed measurements are "per second".
    $row_injection_speed   = $injection_time         > 0 ? sprintf( "%12.3f", $total_rows_injected     / $injection_time         ) : 'indeterminate';
    $row_copy_speed        = $total_copy_time        > 0 ? sprintf( "%12.3f", $total_rows_copied       / $total_copy_time        ) : 'indeterminate';
    $row_obsolete_speed    = $total_obsolete_time    > 0 ? sprintf( "%12.3f", $total_rows_obsoleted    / $total_obsolete_time    ) : 'indeterminate';
    $row_update_speed      = $total_update_time      > 0 ? sprintf( "%12.3f", $total_rows_updated      / $total_update_time      ) : 'indeterminate';
    $row_insert_speed      = $total_insert_time      > 0 ? sprintf( "%12.3f", $total_rows_inserted     / $total_insert_time      ) : 'indeterminate';
    $row_reincarnate_speed = $total_reincarnate_time > 0 ? sprintf( "%12.3f", $total_rows_reincarnated / $total_reincarnate_time ) : 'indeterminate';

    log_timed_message "STATS:  Log archive receiving statistics:";
    log_message "Receiving script started at:  $script_start_timestamp";
    log_message "Receiving script   ended at:  $script_end_timestamp";
    log_message         "$init_timestamp taken to initialize the receiving script";
    log_message    "$injection_timestamp taken to run the injection phase on the archive database";
    log_message         "$copy_timestamp taken to        copy rows into the archive database";
    log_message         "$lock_timestamp taken to        lock tables in the archive database";
    log_message     "$obsolete_timestamp taken to    obsolete rows   in the archive database";
    log_message       "$update_timestamp taken to      update rows   in the archive database";
    log_message       "$insert_timestamp taken to      insert rows into the archive database";
    log_message  "$reincarnate_timestamp taken to reincarnate rows   in the archive database";
    log_message "$delete_files_timestamp taken to      delete old files from the filesystem";
    log_message        "$total_timestamp taken to run the entire receiving side of the archiving cycle";
    log_message "In the statistics below, \"injected\" means \"obsoleted\", \"updated\", \"inserted\", or \"reincarnated\".";

    log_message sprintf( "%8d tables into which data was injected",                      $total_tables_injected   );
    log_message sprintf( "%8d rows of data were injected     into the archive database", $total_rows_injected     );
    log_message sprintf( "%8d rows of data were obsoleted    in   the archive database", $total_rows_obsoleted    );
    log_message sprintf( "%8d rows of data were updated      in   the archive database", $total_rows_updated      );
    log_message sprintf( "%8d rows of data were inserted     into the archive database", $total_rows_inserted     );
    log_message sprintf( "%8d rows of data were reincarnated in   the archive database", $total_rows_reincarnated );
    log_message sprintf( "%8d old files were deleted",                                   $total_files_deleted     );

    foreach my $table ( sort keys %rows_injected ) {
	log_message sprintf( "%8d rows of data were injected     into the $table table", $rows_injected{$table} );
    }
    foreach my $table ( sort keys %rows_obsoleted ) {
	log_message sprintf( "%8d rows of data were obsoleted    in   the $table table", $rows_obsoleted{$table} );
    }
    foreach my $table ( sort keys %rows_updated ) {
	log_message sprintf( "%8d rows of data were updated      in   the $table table", $rows_updated{$table} );
    }
    foreach my $table ( sort keys %rows_inserted ) {
	log_message sprintf( "%8d rows of data were inserted     into the $table table", $rows_inserted{$table} );
    }
    foreach my $table ( sort keys %rows_reincarnated ) {
	log_message sprintf( "%8d rows of data were reincarnated in   the $table table", $rows_reincarnated{$table} );
    }

    log_message   "$row_injection_speed rows injected     per second, over all tables";
    log_message        "$row_copy_speed rows copied       per second, over all tables";
    log_message    "$row_obsolete_speed rows obsoleted    per second, over all tables";
    log_message      "$row_update_speed rows updated      per second, over all tables";
    log_message      "$row_insert_speed rows inserted     per second, over all tables";
    log_message "$row_reincarnate_speed rows reincarnated per second, over all tables";

    log_timed_message "STATS:  This pass of log archiving $cycle_status on the receiving side$suffix.";

    # FIX MAJOR:  Do we need any reincarnation statistics in the status message sent to Foundation?

    # Reformat certain speed measurements for later use in a message sent to Foundation.
    $row_injection_speed = $injection_time    > 0 ? sprintf( "%.1f", $total_rows_injected / $injection_time )    : 'indeterminate';
    $row_insert_speed    = $total_insert_time > 0 ? sprintf( "%.1f", $total_rows_inserted / $total_insert_time ) : 'indeterminate';

    # Calculate some other statistics for later use in a status message sent to Foundation.
    $message_rows_injected = 0;
    $message_rows_inserted = 0;
    foreach my $table (@message_data_tables) {
	$message_rows_injected += $rows_injected{$table} || 0;
	$message_rows_inserted += $rows_inserted{$table} || 0;
    }
    $perfdata_rows_injected = 0;
    $perfdata_rows_inserted = 0;
    foreach my $table (@performance_data_tables) {
	$perfdata_rows_injected += $rows_injected{$table} || 0;
	$perfdata_rows_inserted += $rows_inserted{$table} || 0;
    }
}
