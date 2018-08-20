#!/usr/local/groundwork/perl/bin/perl -w --

# log-archive-send.pl

# Copyright (c) 2013-2016 GroundWork, Inc. (www.gwos.com).  All rights reserved.
# Use of this software is subject to commercial license terms.

# This script extracts data from a runtime database into files, transfers
# those files to where a companion receiving script can see them, runs the
# receiving script (to inject the data into an archive database), and manages
# the eventual deletion of the files it creates (if those files are not
# instead managed by the receiving script).  If all goes well and the data is
# successfully transferred to the archive database, the corresponding rows from
# selected tables are eventually deleted from the runtime database.  Every
# effort is made along the way to ensure that data is always preserved and
# never lost.  The files created by this script are not deleted immediately
# after use; they are kept around for some period to ensure that any necessary
# manual recovery actions could be taken in extreme situations.
#
# The set of data to transfer this way is established in the companion config
# file for this script.  That config file contains extensive comments on the
# settings for adjusting the actions of this script at a customer site.
#
# Normally, this script is run in an automated fashion via a cron job shortly
# after midnight each day.  This scheduling allows for a brief period after
# midnight during which data is gradually flushed from Foundation into
# the runtime database, before this script runs and tries to archive it.
# Currently, there is no function available to force Foundation to flush its
# data to the database; we rely strictly on delaying the run of this script
# long enough after midnight for the data to likely appear in the runtime
# database before we try to pick up the previous day's data.

# Note that we must implement exceptional condition handling in these scripts
# (pun intended).  ALL exception conditions MUST be detected and dealt with
# in a clean way, so we are guaranteed that the data we are moving around is
# safely migrated before we go destroying it in either the original runtime
# database or in the source or target files.  THERE MUST BE NO EXCEPTIONS TO
# THIS RULE!  Any place we can possibly sense a problem, we have to check.

# TO DO:
#
# (*) FIX MINOR:  We should have a command-line option to suppress row and file
#     deletion, for development purposes.
# (*) FIX LATER:  We should support command-line arguments to specify what day(s)
#     to archive, so we can run data archiving manually for test or recovery purposes.
# (*) FIX LATER:  Think about how this script might need to be run in situations
#     other than just the daily archiving run.  To that end, we should perhaps
#     consider providing some kind of forced-time-period mode, where the
#     archiving-time parameters must be specified on the command line instead
#     of being drawn from the state files.  Such a design would allow the script
#     to be run manually both to test its capabilities and to recover from any
#     failure scenarios.
# (*) FIX MINOR:  We should probably lock the send state file while this script is
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

my $PROGNAME       = "log-archive-send.pl";
my $VERSION        = "0.0.11";
my $COPYRIGHT_YEAR = "2016";

my $default_config_file = '/usr/local/groundwork/config/log-archive-send.conf';
my $log_archive_bin     = '/usr/local/groundwork/core/archive/bin';

# These specifications of executables can be changed to absolute pathnames if there is
# any concern about misconfigured PATH settings, Trojan horses, or other security issues.
# This shouldn't be an issue for local commands (scp and ssh), since we set the PATH here
# to a safe value very early in our execution.  It's a possible (though not likely) concern
# on the remote side, for remote commands (mkdir).
my $mkdir = 'mkdir';
my $scp   = 'scp';
my $ssh   = 'ssh';

# ================================
# Command-Line Parameters
# ================================

# In theory, these parameter settings could be overridden by command-line arguments.
# In practice, we don't currently support any such arguments; this script uses only
# a fixed set of arguments.

my $config_file           = $default_config_file;
my $debug_config          = 0;                      # if set, spill out certain data about config-file processing to STDOUT
my $show_help             = 0;
my $show_version          = 0;
my $run_automatically     = 0;
my $run_interactively     = 0;
my $reflect_log_to_stdout = 0;
my $run_in_test_mode      = 0;
my $unified_logging       = 0;

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
my $max_logfile_size       = undef;                 # log rotate is handled externally, not here
my $max_logfiles_to_retain = undef;                 # log rotate is handled externally, not here

my $send_data_to_archive_database = undef;

my $runtime_dbtype = undef;
my $runtime_dbhost = undef;
my $runtime_dbport = undef;
my $runtime_dbname = undef;
my $runtime_dbuser = undef;
my $runtime_dbpass = undef;

my $source_script_machine    = undef;
my $source_script_ip_address = undef;

my $target_script_machine = undef;

my @primary_tables              = ();
my @secondary_tables_and_fields = ();
my @secondary_tables            = ();
my @tertiary_tables_and_joins   = ();
my @tertiary_tables             = ();
my @message_data_tables         = ();
my @performance_data_tables     = ();
my %time_field                  = ();
my %join_condition              = ();

my $dumpfile_format                                    = undef;
my $dump_copy_block_rows                               = undef;
my $minimum_additional_hours_to_archive                = undef;
my $dump_days_minimum                                  = undef;
my $dump_days_maximum                                  = undef;
my $operationally_useful_days_for_messages             = undef;
my $operationally_useful_days_for_performance_data     = undef;
my $post_archiving_retention_days_for_messages         = undef;
my $post_archiving_retention_days_for_performance_data = undef;
my $source_dumpfile_retention_days                     = undef;
my $log_archive_source_data_directory                  = undef;
my $log_archive_target_data_directory                  = undef;
my $log_archive_source_state_file                      = undef;

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

# Parameters derived from the state-file parameters.

my %data_start_time      = ();
my %data_start_timestamp = ();
my %data_end_time        = ();
my %data_end_timestamp   = ();

# ================================
# Working Variables
# ================================

my $dbh   = undef;
# my $sth   = undef;
# my $query = undef;

my $script_start_time            = undef;
my $script_init_end_time         = undef;
my $script_capture_end_time      = undef;
my $script_transfer_end_time     = undef;
my $script_injection_end_time    = undef;
my $script_delete_files_end_time = undef;
my $script_delete_data_end_time  = undef;

my $run_start_time      = undef;
my $run_start_timestamp = undef;

my $script_run_time      = undef;
my $script_run_timestamp = undef;

my $archive_start_time      = undef;
my $archive_start_timestamp = undef;
my $archive_end_time        = undef;
my $archive_end_timestamp   = undef;

my $earliest_file_retention_time      = undef;
my $earliest_file_retention_timestamp = undef;

my $message_data_delete_start_time          = undef;
my $message_data_delete_start_timestamp     = undef;
my $message_data_delete_end_time            = 0;
my $message_data_delete_end_timestamp       = '2000-01-01 00:00:00';    # Defaulted to infinitely far back as a safety measure.
my $performance_data_delete_start_time      = undef;
my $performance_data_delete_start_timestamp = undef;
my $performance_data_delete_end_time        = 0;
my $performance_data_delete_end_timestamp   = '2000-01-01 00:00:00';    # Defaulted to infinitely far back as a safety measure.

my $total_tables_captured        = 0;
my $total_rows_captured          = 0;
my $total_transferred_file_bytes = 0;
my $total_files_deleted          = 0;
my $total_rows_deleted           = 0;

my %rows_captured = ();
my %rows_deleted  = ();

my $message_rows_captured  = undef;
my $message_rows_deleted   = undef;
my $perfdata_rows_captured = undef;
my $perfdata_rows_deleted  = undef;

my $unsupported_options_in_use = 0;
my $some_data_not_archived     = 0;
my $cycle_outcome              = undef;

# These variables really ought to just be local to the log_archiving_statistics() routine,
# except that we want a few of them to be accessible to the send_outcome_to_foundation()
# routine so the message it sends is more informative.
my $init_time              = undef;
my $capture_time           = undef;
my $transfer_time          = undef;
my $injection_time         = undef;
my $delete_files_time      = undef;
my $delete_data_time       = undef;
my $total_time             = undef;
my $init_timestamp         = undef;
my $capture_timestamp      = undef;
my $transfer_timestamp     = undef;
my $injection_timestamp    = undef;
my $delete_files_timestamp = undef;
my $delete_data_timestamp  = undef;
my $total_timestamp        = undef;
my $row_capture_speed      = undef;
my $byte_transfer_speed    = undef;
my $row_deletion_speed     = undef;

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
	log_timed_message "=== Log archive sending script (version $VERSION) starting up (process $$). ===";

	if ( !$enable_processing ) {
	    print "FATAL:  log-archive sending is not enabled in its config file.\n";
	    log_timed_message "FATAL:  Stopping log-archive sending (process $$) because processing is not enabled in the config file ($config_file).";
	    $status_message = 'processing is disabled in the config file';
	    $cycle_outcome = 0;
	}
    }

    if ($cycle_outcome) {
	log_timed_message "NOTICE:  Reading state information from previous cycles.";
	$cycle_outcome = read_state_info();
	$status_message = 'cannot read state file' if not $cycle_outcome;
    }

    # Compute the time period that will be archived from the database into file(s) in this pass.
    if ($cycle_outcome) {
	log_timed_message "NOTICE:  Computing the archiving time period.";
	$cycle_outcome = compute_archiving_period();
	$status_message = 'cannot compute the archiving time period' if not $cycle_outcome;
    }

    # Compute the time periods that will be deleted from the secondary tables in the runtime database in this pass,
    # if the archiving is successful.
    if ($cycle_outcome) {
	log_timed_message "NOTICE:  Computing the database deletion time periods.";
	$cycle_outcome = compute_database_deletion_periods();
	$status_message = 'cannot compute the database deletion time periods' if not $cycle_outcome;
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
	$cycle_outcome = compute_tables_to_archive();
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

    # Open a connection to the runtime database.
    if ($cycle_outcome) {
	log_timed_message "NOTICE:  Opening a connection to the runtime database.";
	$cycle_outcome = open_database_connection();
	$status_message = 'cannot connect to the runtime database' if not $cycle_outcome;
    }

    # Pull the archive data from the runtime database, into source file(s).
    my $source_files;
    if ($cycle_outcome) {
	log_timed_message "NOTICE:  Capturing all data from the runtime database into files.";
	( $cycle_outcome, $source_files ) = capture_all_tables("$log_archive_source_data_directory/$script_run_timestamp");
	$status_message = 'cannot capture all configured tables' if not $cycle_outcome;
    }

    capture_timing(\$script_capture_end_time);

    # Send the file(s) to the target script machine.
    if ($cycle_outcome) {
	log_timed_message "NOTICE:  Transferring all files to the target script machine.";
	$cycle_outcome = transfer_files_to_target( $source_files, "$log_archive_target_data_directory/$script_run_timestamp" );
	$status_message = 'cannot transfer files' if not $cycle_outcome;
    }

    capture_timing(\$script_transfer_end_time);

    # Initiate data-insert/update operations on the target script machine.
    if ($cycle_outcome) {
	log_timed_message "NOTICE:  Injecting data into the archive database.";
	$cycle_outcome = inject_data_into_archive("$log_archive_target_data_directory/$script_run_timestamp");
	$status_message = 'cannot inject data into the archive database' if not $cycle_outcome;
    }

    capture_timing(\$script_injection_end_time);

    if ($cycle_outcome) {
	log_timed_message "NOTICE:  Writing state information for use in future cycles.";
	$cycle_outcome = write_state_info();
	$status_message = 'cannot write state file' if not $cycle_outcome;
    }

    # If all of the above succeeded, delete data from the source file(s), per calculation above.
    if ($cycle_outcome) {
	log_timed_message "NOTICE:  Deleting old data files on the source machine.";
	$cycle_outcome = delete_old_source_files();
	$status_message = 'cannot delete source files' if not $cycle_outcome;
    }

    capture_timing(\$script_delete_files_end_time);

    # If all of the above succeeded, delete data from the secondary tables in the runtime database,
    # per calculation of time period above.
    if ($cycle_outcome) {
	log_timed_message "NOTICE:  Deleting old data in the runtime database.";
	$cycle_outcome = delete_old_database_rows();
	$status_message = 'cannot delete old runtime-database rows' if not $cycle_outcome;
    }

    # Close the connection to the runtime database.  This is done unconditionally, since we should close
    # the connection even if errors occurred after it was opened.  The routine can internally handle the
    # case where the connection was never opened in the first place because of prior errors.  However,
    # it gets confusing if we log the occurrence of this call under circumstances when it won't actually
    # do anything, so we do qualify the logging here.
    log_timed_message "NOTICE:  Closing the connection to the runtime database." if $dbh and log_is_open();
    close_database_connection();

    capture_timing(\$script_delete_data_end_time);

    log_archiving_statistics($status_message) if log_is_open();

    send_outcome_to_foundation( $status_message, $cycle_outcome );

    close_logfile();

    return $cycle_outcome ? STOP_STATUS : ERROR_STATUS;
}

sub print_version {
    print "$PROGNAME Version:  $VERSION\n";
    print "Copyright 2013-$COPYRIGHT_YEAR GroundWork, Inc. (www.gwos.com).\n";
    print "All rights reserved.\n";
}

# This script is designed to run both in automatic mode (the -a option) or in debug-config mode (the -d option,
# which just spills out what the script sees in the config file, for human verification that the script is seeing
# what it ought to see).  One or the other of those flags must be given on each invocation, to prevent the stupid
# mistake made by the crontab designers long ago, where running the crontab command without any arguments can
# damage the setup by emptying the content of your crontab file without telling you that this is probably something
# you don't want.  In automatic mode, the determination of the data to be archived, and related data-retention
# calculations, will all be computed from saved state information.
sub print_usage {
    print "usage:  $PROGNAME -h\n";
    print "        $PROGNAME -v\n";
    print "        $PROGNAME -d [-c config_file]\n";
    print "        $PROGNAME -a [-c config_file] [-i] [-o] [-u] [-t]\n";
    print "where:  -h:  print this help message\n";
    print "        -v:  print the version number\n";
    print "        -d:  debug config file\n";
    print "        -a:  run in automatic mode (normal production use)\n";
    print "        -c config_file\n";
    print "             specifies an alternate config file; the default config file is:\n";
    print "             $default_config_file\n";
    print "        -i:  run interactively, not as a background process\n";
    print "        -o:  write log messages also to standard output\n";
    print "        -u:  write a unified log file on the sending side, combining\n";
    print "             messages from both send and receive actions\n";
    print "        -t:  run in test mode (NOT FOR PRODUCTION USE)\n";
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
	"captured $message_rows_captured events, $perfdata_rows_captured perf rows at $row_capture_speed total rows/sec;"
      . " deleted $message_rows_deleted old events, $perfdata_rows_deleted perf rows at $row_deletion_speed total rows/sec;"
      . " $total_timestamp total run time";

    if ($unsupported_options_in_use) {
	## Prepend a short message about using a non-standard setup, and force the status to WARNING.
	my $unsupported_message = 'unsupported options in use';
	$status_message = $status_message eq '' ? $unsupported_message : "$unsupported_message; $status_message";
	$outcome = 0;
    }

    if ($some_data_not_archived) {
	## Prepend a short message about the potential loss of archived data, and force the status to WARNING.
	my $missing_data_message = 'some data is not being archived';
	$status_message = $status_message eq '' ? $missing_data_message : "$missing_data_message; $status_message";
	$outcome = 0;
    }

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

	$send_data_to_archive_database = $config->get_boolean ('send_data_to_archive_database');

	# FIX LATER:  We have the send_data_to_archive_database flag in the config file but don't
	# currently pay attention to it in this script.  We will extend the script to allow not
	# sending to an archive database, so this script can be run at some sites purely for its
	# automated data-purging capabilities.  But we don't want any actively archiving sites to
	# break when we do allow that mode of operation, so for now we insist that the parameter
	# be set as enabled.
	if ( not $send_data_to_archive_database ) {
	    die "ERROR:  configured value for send_data_to_archive_database must be \"yes\"\n";
	}

	$runtime_dbtype = $config->get_scalar('runtime_dbtype');
	$runtime_dbhost = $config->get_scalar('runtime_dbhost');
	$runtime_dbport = $config->get_number('runtime_dbport');
	$runtime_dbname = $config->get_scalar('runtime_dbname');
	$runtime_dbuser = $config->get_scalar('runtime_dbuser');
	$runtime_dbpass = $config->get_scalar('runtime_dbpass');

	$source_script_machine    = $config->get_scalar('source_script_machine');
	$source_script_ip_address = $config->get_scalar('source_script_ip_address');

	if ( !is_valid_hostname($source_script_machine) ) {
	    die "ERROR:  configured value for source_script_machine must be a valid hostname\n";
	}

	$monitor_server_hostname = $source_script_machine;
	if ( $monitor_server_hostname eq '' ) {
	    die "ERROR:  configured value for source_script_machine cannot be an empty string\n";
	}
	elsif ( $monitor_server_hostname eq 'localhost' ) {
	    $monitor_server_ip_address = $source_script_ip_address ne '' ? $source_script_ip_address : '127.0.0.1';
	}
	elsif ( $source_script_ip_address ne '' ) {
	    $monitor_server_ip_address = $source_script_ip_address;
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
	    ## this ambiguity by not defaulting the source_script_ip_address in the config file.
	    my $packed_ip_address = gethostbyname($monitor_server_hostname);
	    ## Check if we could resolve the hostname to an IP address.
	    if ( defined $packed_ip_address ) {
		$monitor_server_ip_address = inet_ntoa($packed_ip_address);
	    }
	    else {
		die "ERROR:  cannot resolve the IP address for source_script_machine \"$monitor_server_hostname\";"
		  . " you can avoid this by specifying a non-empty value for source_script_ip_address\n";
	    }
	}

	$target_script_machine = $config->get_scalar('target_script_machine');

	if ( !is_valid_hostname($target_script_machine) ) {
	    die "ERROR:  configured value for target_script_machine must be a valid hostname\n";
	}

	# FIX MINOR:  These arrays are just an initial attempt at how to configure these parameters.  We still
	# need to ensure that we guarantee a particular fixed ordering of the specified values, in most cases.
	# Currently, that has been done by manual analysis of the database followed by corresponding sequencing of
	# the specified table names in the config file, and by experiment showing that said orderings are preserved
	# when the config file is read into these arrays.  In the future, we will take all the lists, analyze all
	# the cross-table references based on associations extracted from the database, and run a topological sort
	# to transform all the partial orderings we extract that way into a consistent total ordering.

	@primary_tables = $config->get_array ('primary_table');
	print Data::Dumper->Dump([\@primary_tables], [qw(\@primary_tables)]) if $config_debug;

	@secondary_tables_and_fields = $config->get_array ('secondary_table_and_field');
	print Data::Dumper->Dump([\@secondary_tables_and_fields], [qw(\@secondary_tables_and_fields)]) if $config_debug;

	@tertiary_tables_and_joins = $config->get_array ('tertiary_table_and_join');
	print Data::Dumper->Dump([\@tertiary_tables_and_joins], [qw(\@tertiary_tables_and_joins)]) if $config_debug;

	foreach my $table (@primary_tables) {
	    if ( $table =~ /^schemainfo$/i ) {
		die "ERROR:  primary_table cannot specify the \"$table\" table\n";
	    }
	}

	foreach my $field_spec (@secondary_tables_and_fields) {
	    ## This is really only crude validation.  Actual validation will wait until we try to use
	    ## these values when accessing the database.
	    if ( $field_spec =~ /^([a-z_]+)\.([a-z_]+)$/ ) {
		my $table = $1;
		my $field = $2;
		if ( $table =~ /^schemainfo$/i ) {
		    die "ERROR:  secondary_table_and_field cannot specify the \"$table\" table\n";
		}
		push @secondary_tables, $table;
		$time_field{$table} = $field;
	    }
	    else {
		die "ERROR:  configured value for secondary_table_and_field (\"$field_spec\") is invalid\n";
	    }
	}

	foreach my $join_spec (@tertiary_tables_and_joins) {
	    ## This is really only crude validation.  Actual validation will wait until we try to use
	    ## these values when accessing the database.
	    if ( $join_spec =~ /^([a-z_]+)(?:;([a-z_=.]+))?$/ ) {
		my $table           = $1;
		my $join_expression = $2;
		if ( $table =~ /^schemainfo$/i ) {
		    die "ERROR:  tertiary_table_and_join cannot specify the \"$table\" table\n";
		}
		push @tertiary_tables, $table;

		if ( defined($join_expression) ) {
		    ## Here, we only process the join condition.  Downstream processing when we capture the data
		    ## will construct a corresponding WHERE clause and attach that to the SQL statement that fetches
		    ## data from $table.
		    ##
		    ## In the initial case, we need to take the "logmessageid=logmessage.logmessageid" join condition
		    ## for the logmessageproperty table, and add "logmessage.reportdate < $capture_end_timestamp"
		    ## or equivalent, to make the full filter operation we will apply.  Since we haven't computed
		    ## $capture_end_timestamp at this point in the script, computing that part of the WHERE clause
		    ## must be deferred until the point of use.
		    ##
		    ## First, qualify all unqualified names in the join, by assuming they refer to the base table
		    ## and prepending the table name.  This action not only gets us the valid SQL we desire, it also
		    ## provides some level of protection against SQL injection attacks via the config file, by causing
		    ## inappropriate joins to produce malformed SQL.
		    $join_expression =~ s/(^\s*|[^a-z_.]+)([a-z_]+)(?=\s*[^a-z_.]|$)/$1$table.$2/g;
		    $join_condition{$table} = $join_expression;
		}
	    }
	    else {
		die "ERROR:  configured value for tertiary_table_and_join (\"$join_spec\") is invalid\n";
	    }
	}

	# In the validation of @message_data_tables and @performance_data_tables below, %time_field serves as a proxy
	# for a %is_a_secondary_table hash, to quickly tell if the table is defined in the @secondary_tables array.

	@message_data_tables = $config->get_array ('message_data_table');
	print Data::Dumper->Dump([\@message_data_tables], [qw(\@message_data_tables)]) if $config_debug;

	foreach my $table (@message_data_tables) {
	    if ( not exists $time_field{$table} ) {
		die "ERROR:  message_data_table must specify one of the configured secondary tables\n";
	    }
	}

	@performance_data_tables = $config->get_array ('performance_data_table');
	print Data::Dumper->Dump([\@performance_data_tables], [qw(\@performance_data_tables)]) if $config_debug;

	foreach my $table (@performance_data_tables) {
	    if ( not exists $time_field{$table} ) {
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

	$dump_copy_block_rows                               = $config->get_number('dump_copy_block_rows');
	$minimum_additional_hours_to_archive                = $config->get_number('minimum_additional_hours_to_archive');
	$dump_days_minimum                                  = $config->get_number('dump_days_minimum');
	$dump_days_maximum                                  = $config->get_number('dump_days_maximum');
	$operationally_useful_days_for_messages             = $config->get_number('operationally_useful_days_for_messages');
	$operationally_useful_days_for_performance_data     = $config->get_number('operationally_useful_days_for_performance_data');
	$post_archiving_retention_days_for_messages         = $config->get_number('post_archiving_retention_days_for_messages');
	$post_archiving_retention_days_for_performance_data = $config->get_number('post_archiving_retention_days_for_performance_data');
	$source_dumpfile_retention_days                     = $config->get_number('source_dumpfile_retention_days');
	$log_archive_source_data_directory                  = $config->get_scalar('log_archive_source_data_directory');
	$log_archive_target_data_directory                  = $config->get_scalar('log_archive_target_data_directory');
	$log_archive_source_state_file                      = $config->get_scalar('log_archive_source_state_file');

	# We insist on a certain minimum level of performance tuning.
	if ( $dump_copy_block_rows < 100 ) {
	    die "ERROR:  configured value for dump_copy_block_rows must be at least 100\n";
	}

	if ( $minimum_additional_hours_to_archive < 1 ) {
	    die "ERROR:  configured value for minimum_additional_hours_to_archive must be at least 1\n";
	}

	if ( $dump_days_minimum < 1 ) {
	    die "ERROR:  configured value for dump_days_minimum must be at least 1\n";
	}
	if ( $dump_days_maximum < $dump_days_minimum ) {
	    die "ERROR:  configured value for dump_days_maximum must be at least as large as dump_days_minimum\n";
	}

	# Frankly, I'd like to set a higher minimum value for operationally_useful_days_for_messages.
	# One day won't retain stuff that happens over a normal weekend, let alone a long weekend.
	# FIX LATER:  Decide if this minimum should be raised.
	if ( $operationally_useful_days_for_messages < 1 ) {
	    die "ERROR:  configured value for operationally_useful_days_for_messages must be at least 1\n";
	}

	# In contrast to messages, the copy of performance data within Foundation has no operational significance;
	# it is saved there only for reporting purposes.  So it's quite reasonable to declare that it can be removed
	# from the runtime database as soon as it has reached the archive database, from an operational perspective.
	# Because of that, we just ensure that the value is non-negative.
	if ( $operationally_useful_days_for_performance_data < 0 ) {
	    die "ERROR:  configured value for operationally_useful_days_for_performance_data must be at least 0\n";
	}

	# We allow a zero value for post_archiving_retention_days_for_messages, but ONLY if $run_in_test_mode is
	# true, and that should only be set when the software is under development or QA testing.  And if we do
	# find such an otherwise unsupported value in play, a WARNING message should be emitted in the log file.
	if ( $post_archiving_retention_days_for_messages < ( $run_in_test_mode ? 0 : 1 ) ) {
	    die "ERROR:  configured value for post_archiving_retention_days_for_messages must be at least "
	      . ( $run_in_test_mode ? "0" : "1" ) . "\n";
	}
	if ( $run_in_test_mode and $post_archiving_retention_days_for_messages < 1 ) {
	    ## FIX MINOR:  I'd like to get this message out into the log file, but the log file is not yet open.
	    ## Perhaps we need some kind of separate config-revalidation routine to run after the log file is opened.
	    print "WARNING:  A post_archiving_retention_days_for_messages value less than 1 is only allowed in test mode.\n";
	    print "          USE OF THIS SETTING IS STRONGLY DISCOURAGED, as it destroys short-term data.\n";
	    $unsupported_options_in_use = 1;
	}

	# We allow a zero value for post_archiving_retention_days_for_performance_data, but ONLY if $run_in_test_mode
	# is true, and that should only be set when the software is under development or QA testing.  And if we do
	# find such an otherwise unsupported value in play, a WARNING message should be emitted in the log file.
	if ( $post_archiving_retention_days_for_performance_data < ( $run_in_test_mode ? 0 : 1 ) ) {
	    die "ERROR:  configured value for post_archiving_retention_days_for_performance_data must be at least "
	      . ( $run_in_test_mode ? "0" : "1" ) . "\n";
	}
	if ( $run_in_test_mode and $post_archiving_retention_days_for_performance_data < 1 ) {
	    ## FIX MINOR:  I'd like to get this message out into the log file, but the log file is not yet open.
	    ## Perhaps we need some kind of separate config-revalidation routine to run after the log file is opened.
	    print "WARNING:  A post_archiving_retention_days_for_performance_data value less than 1 is only allowed in test mode.\n";
	    print "          USE OF THIS SETTING IS STRONGLY DISCOURAGED, as it destroys short-term data.\n";
	    $unsupported_options_in_use = 1;
	}

	if ( $source_dumpfile_retention_days < 1 ) {
	    die "ERROR:  configured value for source_dumpfile_retention_days must be at least 1\n";
	}

	if ( $log_archive_source_data_directory !~ m{^/} ) {
	    die "ERROR:  configured value for log_archive_source_data_directory must be an absolute pathname\n";
	}
	if ( $log_archive_target_data_directory !~ m{^/} ) {
	    die "ERROR:  configured value for log_archive_target_data_directory must be an absolute pathname\n";
	}

	if ( $log_archive_source_state_file !~ m{^/.*\.state$} ) {
	    ## We insist on an absolute pathname ending with ".state" to ensure that we never overwrite
	    ## any critical existing file elsewhere in the system because this setup was misconfigured.
	    die "ERROR:  configured value for log_archive_source_state_file must be an absolute pathname ending with \".state\"\n";
	}

	if ( -l $log_archive_source_state_file ) {
	    die "ERROR:  configured value for log_archive_source_state_file cannot be the path to a symlink\n";
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
    if (not getopts('hvc:daiotu', \%opts)) {
	print_usage();
	return 0;
    }

    $show_help             = $opts{h};
    $show_version          = $opts{v};
    $config_file           = ( defined $opts{c} && $opts{c} ne '' ) ? $opts{c} : $default_config_file;
    $debug_config          = $opts{d};
    $run_automatically     = $opts{a};
    $run_interactively     = $opts{i};
    $reflect_log_to_stdout = $opts{o};
    $run_in_test_mode      = $opts{t};
    $unified_logging       = $opts{u};

    # This test is not a full enforcement of intended exclusivity of the major
    # mode options, but it at least requires that you specify either -a or -d,
    # if neither -h nor -v is specified.
    if (!$show_version && !$show_help && !$debug_config && !$run_automatically) {
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
	my $config = TypedConfig->secure_new($log_archive_source_state_file);

	my @successful_archiving_run = $config->get_array ('successful_archiving_run');
	if ($debug_basic) {
	    log_timed_message "DEBUG:  Previous state info read from the state file:";
	    log_message (Data::Dumper->Dump([\@successful_archiving_run], [qw(\@successful_archiving_run)]));
	}

	# @successful_archiving_run values should be like:  "2000-01-02 00:30:00 => 2000-01-01 00:00:00 .. 2000-01-02 00:00:00"
	my ( $second, $minute, $hour, $month_day, $month, $year );
	foreach my $previous_run (@successful_archiving_run) {
	    if ( $previous_run =~ /^(\d{4})-(\d{2})-(\d{2})\s+(\d{2}):(\d{2}):(\d{2}) \s+ => \s+
	      (\d{4})-(\d{2})-(\d{2})\s+(\d{2}):(\d{2}):(\d{2}) \s+ \.\. \s+ (\d{4})-(\d{2})-(\d{2})\s+(\d{2}):(\d{2}):(\d{2})$/x )
	    {
		my $run_timestamp = "$1-$2-$3 $4:$5:$6";
		if ( exists( $data_start_timestamp{$run_timestamp} ) or exists( $data_end_timestamp{$run_timestamp} ) ) {
		    log_timed_message "ERROR:  State-file saved run timestamp for successful_archiving_run is duplicated.";
		    log_timed_message "        State-file saved run timestamp is: \"$run_timestamp\".";
		    $outcome = 0;
		}
		else {
		    ## We always use a 4-digit year when calling timelocal(), to avoid any confusion about how it is treating this value,
		    ## since it makes a mess of interpreting this time component.  See "perldoc Time::Local" for details.

		    ( $year, $month, $month_day, $hour, $minute, $second ) = ( $7, $8, $9, $10, $11, $12 );
		    $data_start_time{$run_timestamp} = timelocal( $second, $minute, $hour, $month_day, $month - 1, $year );
		    ( $year, $month, $month_day, $hour, $minute, $second ) = ( $13, $14, $15, $16, $17, $18 );
		    $data_end_time{$run_timestamp} = timelocal( $second, $minute, $hour, $month_day, $month - 1, $year );

		    $data_start_timestamp{$run_timestamp} = "$7-$8-$9 $10:$11:$12";
		    $data_end_timestamp{$run_timestamp}   = "$13-$14-$15 $16:$17:$18";
		}
	    }
	    else {
		log_timed_message "ERROR:  State-file saved value for successful_archiving_run is invalid.";
		log_timed_message "        State-file saved value is: \"$previous_run\".";
		$outcome = 0;
	    }
	}
    };
    if ($@) {
	chomp $@;
	$@ =~ s/^ERROR:\s+//i;
	log_timed_message "ERROR:  Cannot read state file $log_archive_source_state_file\n  ($@).";
	$outcome = 0;
    }

    if (!%data_start_timestamp) {
	log_timed_message "ERROR:  State file contains no previous successful_archiving_run values.";
	$outcome = 0;
    }

    return $outcome;
}

sub write_state_info {
    my $outcome = 1;

    # In this routine, we write the updated state info into a new file, so it
    # can be swapped into place as the standard state file as an atomic operation.

    # By dint of the validation we performed when we read in the config file,
    # we know that $log_archive_source_state_file will be an absolute pathname.
    my $temporary_state_file = "$log_archive_source_state_file.temp";
    if ( not sysopen( STATE, $temporary_state_file, O_WRONLY | O_APPEND | O_CREAT | O_EXCL, 0600 ) ) {
	log_timed_message "ERROR:  Could not open the temporary state file $temporary_state_file ($!).";
	$outcome = 0;
    }
    else {
	if (not print STATE <<EOF) {
# This file contains the persistent-state information that must be carried
# across invocations of the log-archive-send.pl script, to efficiently
# determine the time periods to use for the next run.

# Each value listed here represents one of the most recent successful runs
# of the archiving scripts.  The value must be of the form:
#
#     "run_start_timestamp => data_start_timestamp .. data_end_timestamp"
#
# where each timestamp must be expressed in the local timezone of the
# source script machine, in the form "YYYY-mm-DD hh:mm:ss".
#
# By convention, the rows are sorted by increasing run_start_timestamp,
# to make it easier to read this file, but that is not strictly necessary.
#
# The specific values listed in those lines are:
#
# run_start_timestamp:
#     A timestamp captured when this cycle of the send script began operation.
#
# data_start_timestamp, data_end_timestamp:
#     The data-capture limits used during this archiving run.  By convention,
#     the start and end time-of-day is always 00:00:00 (midnight), but the
#     stored format allows for more-general timestamp specification.  The
#     selected data archived in this run obeys the mathematical relationship:
#
#         data_start_timestamp <= data_timestamp < data_end_timestamp
#
EOF
	    log_timed_message "ERROR:  Could not write to the temporary state file $temporary_state_file ($!).";
	    $outcome = 0;
	}

	# A successful_archiving_run line for the current run will be stored in the send-state file
	# at the end of each archiving run, so the archiving interval can be correctly calculated
	# in the next run (based on the $archive_end_time of this run).
	#
	# Some number of additional previous successful_archiving_run lines will be saved back in
	# the send-state file at the end of each archiving run, so the retention intervals can be
	# correctly calculated in future runs.  It is desired that older copies of such lines be
	# dropped as the system operates, to maintain a steady-state, relatively small state-file
	# size.  That is done by having the send script remove a row once enough time has passed
	# that all "new_run_time <= old_run_time + post_archiving_retention_days..." conditions
	# for the row will never be satisfied again.

	my ( $second, $minute, $hour, $month_day, $month, $year );
	foreach my $run_timestamp ( sort keys %data_end_timestamp ) {
	    ## We intentionally don't check the value corresponding to $data_end_timestamp{$run_start_timestamp}
	    ## against any configured post-archiving-retention-days parameters, because if we have got this far,
	    ## we never want to prevent writing a line for the current run into the state file.
	    if ( $run_timestamp ne $run_start_timestamp ) {
		## Drop obsolete successful_archiving_run lines that will never be applicable again.
		## We could have based this analysis on the slightly earlier $data_end_timestamp{$run_timestamp} instead of $run_timestamp,
		## but we're being extremely conservative here when we interpret the $post_archiving_retention_days_for_messages and
		## $post_archiving_retention_days_for_performance_data parameters.
		if ( $run_timestamp =~ /^(\d{4})-(\d{2})-(\d{2}) (\d{2}):(\d{2}):(\d{2})$/ ) {
		    ( $year, $month, $month_day, $hour, $minute, $second ) = ( $1, $2, $3, $4, $5, $6 );
		    ## We always use a 4-digit year when calling timelocal(), to avoid any confusion about how it is treating this value,
		    ## since it makes a mess of interpreting this time component.  See "perldoc Time::Local" for details.
		    my $message_retention_end_time =
		      timelocal_nocheck( $second, $minute, $hour, $month_day + $post_archiving_retention_days_for_messages, $month - 1, $year );
		    my $performance_data_retention_end_time =
		      timelocal_nocheck( $second, $minute, $hour, $month_day + $post_archiving_retention_days_for_performance_data,
			$month - 1, $year );
		    ## FIX MAJOR:  Use $run_start_time here, or $archive_end_time instead?  Also refer to the
		    ## formula in the comment above, and change other documentation as well if necessary.
		    next if $run_start_time > $message_retention_end_time and $run_start_time > $performance_data_retention_end_time;
		    ## next if $archive_end_time > $message_retention_end_time and $archive_end_time > $performance_data_retention_end_time;
		}
		else {
		    log_timed_message "ERROR:  Bad internal operation; \$run_timestamp from \%data_end_timestamp has an unexpected format!";
		    $outcome = 0;
		    ## Once we see a failure with one output line, there's no point in continuing with other lines,
		    ## because we're just going to discard the temporary state file files we are writing in this run.
		    last;
		}
	    }
	    print STATE
	      "successful_archiving_run = \"$run_timestamp => $data_start_timestamp{$run_timestamp} .. $data_end_timestamp{$run_timestamp}\"\n";
	}

	if (not close STATE) {
	    log_timed_message "ERROR:  Could not close the temporary state file $temporary_state_file ($!).";
	    $outcome = 0;
	}

	if ($outcome) {
	    if (not rename $temporary_state_file, $log_archive_source_state_file) {
		log_timed_message "ERROR:  Could not rename the temporary state file $temporary_state_file ($!).";
		$outcome = 0;
	    }
	}
	else {
	    ## Let's clean up so we don't impede writing the state file during the next archiving cycle,
	    ## where the code above will once again insist that the temporary file not exist before opening it.
	    if ( unlink($temporary_state_file) != 1 ) {
		log_timed_message "ERROR:  Could not unlink the temporary state file $temporary_state_file ($!).";
	    }
	}
    }

    return $outcome;
}

sub compute_archiving_period {
    my ( $second, $minute, $hour, $month_day, $month, $year, $week_day, $year_day, $is_dst );
    my $outcome = 1;

    my @run_timestamps = (sort keys %data_end_timestamp);
    my $last_run_timestamp = $run_timestamps[$#run_timestamps];
    my $last_previous_successful_end_timestamp = $data_end_timestamp{$last_run_timestamp};

    # The $archive_end_time must be set to 00:00:00 today in the local timezone, and it must be at least
    # $minimum_additional_hours_to_archive hours later than $archive_start_time (if that value is defined).
    # $minimum_additional_hours_to_archive is usually set to a bit less than a day, like 18 hours, instead
    # of 24 hours to allow for shortened days at Daylight Savings Time transitions.
    #
    # On the other side of this overall archiving operation, the receiving script needs to be able to derive
    # $archive_end_timestamp for this run from easily available information, without requiring either another
    # command-line argument (which would complexify calling the receiving script during recovery operations)
    # or some additional transient-state file passed along with the set of dumpfiles.  The simplest way to
    # do this is to ensure that the data used to construct $script_run_timestamp, which is used to name the
    # directory containing the set of dumpfiles, is always used to compute $archive_end_timestamp as well.
    # That way, the receiving script can just drop the time-of-day information from the directory name, and
    # that will yield the $archive_end_timestamp value needed for its operations.  That being the case, we
    # make that correspondence a strictly enforced convention here on the sending side.
    #
    # FIX MINOR:  Should $script_run_time and $script_run_timestamp just be copies of $run_start_time
    # and $run_start_timestamp?  That would mean as well that $archive_end_time would be derived from
    # $run_start_time, to ensure that $archive_end_time remains 00:00:00 of the same day as
    # $script_run_timestamp.  Compare all the uses of $run_start_time, $run_start_timestamp, and
    # $script_run_timestamp in this script, to see if we ought to simplify the calculations.

    $script_run_time = time();
    ( $second, $minute, $hour, $month_day, $month, $year, $week_day, $year_day, $is_dst ) = localtime($script_run_time);
    $script_run_timestamp = sprintf "%04d-%02d-%02d_%02d.%02d.%02d", $year + 1900, $month + 1, $month_day, $hour, $minute, $second;

    ## We always use a 4-digit year when calling timelocal(), to avoid any confusion about how it is treating this value,
    ## since it makes a mess of interpreting this time component.  See "perldoc Time::Local" for details.
    $archive_end_time = timelocal( 0, 0, 0, $month_day, $month, $year + 1900 );
    if ( defined($archive_start_time) and $archive_end_time - $archive_start_time < $minimum_additional_hours_to_archive * SECONDS_PER_HOUR ) {
	log_timed_message "ERROR:  The archive end time must be at least $minimum_additional_hours_to_archive hours past the archive start time,";
	log_timed_message "        which is typically set to the end of the last previous successful cycle.)";
	log_timed_message "        (The last previous successful end time was $last_previous_successful_end_timestamp.)";
	log_timed_message "NOTICE:  No archiving will occur during this cycle.";
	$outcome = 0;
    }
    # We need the special timelocal_nocheck() function here because we are generating month-day values which are often
    # outside the range of what would be valid for the specified month.  This routine will automatically normalize the
    # data for us, which is exactly what we're looking for.  This allows us to calculate using "whole days" for the
    # dump days limits, even though some days may be 23 or 25 hours long at Daylight Savings Time transitions.
    my $latest_archive_start_time   = timelocal_nocheck( 0, 0, 0, $month_day - $dump_days_minimum, $month, $year + 1900 );
    my $earliest_archive_start_time = timelocal_nocheck( 0, 0, 0, $month_day - $dump_days_maximum, $month, $year + 1900 );

    if ( defined($last_previous_successful_end_timestamp)
	and $last_previous_successful_end_timestamp =~ /^(\d{4})-(\d{2})-(\d{2}) (\d{2}):(\d{2}):(\d{2})$/ )
    {
	( $year, $month, $month_day, $hour, $minute, $second ) = ( $1, $2, $3, $4, $5, $6 );
	## We always use a 4-digit year when calling timelocal(), to avoid any confusion about how it is treating this value,
	## since it makes a mess of interpreting this time component.  See "perldoc Time::Local" for details.
	$archive_start_time = timelocal( $second, $minute, $hour, $month_day, $month - 1, $year );

	# Apply the configured limits on the time period over which we will archive.
	if ($archive_start_time > $latest_archive_start_time) {
	    $archive_start_time = $latest_archive_start_time;
	}
	if ( $archive_start_time < $earliest_archive_start_time ) {
	    ## We're in a potentially dangerous situation here.  Putting a warning in the log file is the least we can do.
	    ## So we also set a flag that will affect the event message sent to Foundation about the status of this cycle.
	    $some_data_not_archived = 1;
	    log_timed_message "WARNING:  The archive start time is being limited by the configuration";
	    log_timed_message "          setting for the dump_days_maximum option ($dump_days_maximum days).";
	    log_timed_message "          DATA BETWEEN "
	      . ( scalar localtime $archive_start_time ) . " AND "
	      . ( scalar localtime $earliest_archive_start_time )
	      . " WILL NOT BE ARCHIVED.";
	    $archive_start_time = $earliest_archive_start_time;
	}
    }
    else {
	log_timed_message "ERROR:  Cannot determine the archive start time.";
	$outcome = 0;
    }

    # The following timestamps are used in database accesses, not in filename construction,
    # so we use standard ISO 8601 punctuation (except with a space instead of a T separator
    # between date and time, as the database expects).
    if ( defined $archive_start_time ) {
	( $second, $minute, $hour, $month_day, $month, $year, $week_day, $year_day, $is_dst ) = localtime($archive_start_time);
	$archive_start_timestamp = sprintf "%04d-%02d-%02d %02d:%02d:%02d", $year + 1900, $month + 1, $month_day, $hour, $minute, $second;
	log_timed_message "STATS:  Archiving period starts at:  $archive_start_timestamp" if $debug_minimal;
    }
    else {
	# $archive_start_time must always be defined, so we can store a proper start timestamp for this run in the state file
	# when this run is finished.  Otherwise, the rest of the code would adapt automatically to a missing start time.
	log_timed_message "ERROR:  Cannot determine the archive start timestamp.";
	$outcome = 0;
    }
    if ( defined $archive_end_time ) {
	( $second, $minute, $hour, $month_day, $month, $year, $week_day, $year_day, $is_dst ) = localtime($archive_end_time);
	$archive_end_timestamp = sprintf "%04d-%02d-%02d %02d:%02d:%02d", $year + 1900, $month + 1, $month_day, $hour, $minute, $second;
	log_timed_message "STATS:  Archiving period   ends at:  $archive_end_timestamp" if $debug_minimal;

	# We issue a same-day warning, and a failure outcome, if this script is re-run on the same day
	# as the last successful run (since there will be no further data archived, since the end of the
	# archive period will still be midnight last night).  We could perhaps just test the largest
	# key in the hash, since that should be the last successful run, but it's easier just to run
	# the whole loop.
	foreach my $run_timestamp ( sort keys %data_end_timestamp ) {
	    if ( $data_end_timestamp{$run_timestamp} eq $archive_end_timestamp ) {
		## We allow processing to proceed only in test mode, where you might want
		## to play with data deletion as part of development or QA testing.  You
		## still won't get anything more archived in this cycle, though.
		(my $archive_day = $archive_end_timestamp) =~ s/ .*//;
		if ($run_in_test_mode) {
		    log_timed_message "WARNING:  Archiving has already been run successfully today ($archive_day),";
		    log_timed_message "          as noted in the log-archive-send.state file.";
		}
		else {
		    log_timed_message "ERROR:  Archiving has already been run successfully today ($archive_day),";
		    log_timed_message "        as noted in the log-archive-send.state file.";
		    log_timed_message "NOTICE:  This cycle of archiving will be skipped, since";
		    log_timed_message "         no additional data would be archived.";
		    $outcome = 0;
		}
		last;
	    }
	}
    }
    else {
	# $archive_end_time must always be defined, both to limit the data archived in this run and
	# to provide a proper end timestamp for this run in the state file when this run is finished.
	log_timed_message "ERROR:  Cannot determine the archive end timestamp.";
	$outcome = 0;
    }

    if ($outcome) {
	## We only update the state file with data for this run if we actually archive stuff.
	$data_start_timestamp{$run_start_timestamp} = $archive_start_timestamp;
	$data_end_timestamp{$run_start_timestamp}   = $archive_end_timestamp;
    }

    return $outcome;
}

sub compute_database_deletion_periods {
    my ( $second, $minute, $hour, $month_day, $month, $year, $week_day, $year_day, $is_dst );
    my $outcome = 1;

    # We don't bother to calculate values for $message_data_delete_start_time or $performance_data_delete_start_time
    # because there's really not much point in limiting the start of the deletion interval.  The downstream code is
    # equipped to cope with undefined values for these parameters.

    # We base our deletion-time calculations on walking back from $archive_end_time instead of on walking back from
    # $run_start_time, because it makes more sense to keep synchronized with midnight at the end of the archive period and
    # not to care much about any additional time that has transpired today between $archive_end_time and $run_start_time.
    ( $second, $minute, $hour, $month_day, $month, $year, $week_day, $year_day, $is_dst ) = localtime($archive_end_time);

    # We need the special timelocal_nocheck() function here because we are generating month-day values which are often
    # outside the range of what would be valid for the specified month.  This routine will automatically normalize the
    # data for us, which is exactly what we're looking for.  This allows us to calculate using "whole days" for the
    # operationally useful days, even though some days may be 23 or 25 hours long at Daylight Savings Time transitions.
    $message_data_delete_end_time =
      timelocal_nocheck( $second, $minute, $hour, $month_day - $operationally_useful_days_for_messages, $month, $year + 1900 );
    $performance_data_delete_end_time =
      timelocal_nocheck( $second, $minute, $hour, $month_day - $operationally_useful_days_for_performance_data, $month, $year + 1900 );

    # FIX MAJOR:  are these calculations correct?  and if they are, are we always assured
    # that we have a defined $archive_start_time to reference here?
    #
    # Before we go looking to see if we need to keep data around for some post_archiving_retention_days after some
    # previous archiving runs, we may as well recognize that we will need to keep around all the data we are about
    # to archive for at least that long.  So we make the same comparisons we're about to make for prior runs, but
    # applied first to the current run.
    if ($message_data_delete_end_time > $archive_start_time) {
	$message_data_delete_end_time = $archive_start_time;
    }
    if ($performance_data_delete_end_time > $archive_start_time) {
	$performance_data_delete_end_time = $archive_start_time;
    }

    # FIX MAJOR:  use the data_end_time for the post_archiving calculations, not the run time?
    # FIX MAJOR:  compare to the calculations we make in write_state_info()
    foreach my $run_timestamp ( keys %data_start_time ) {
	if ( defined($run_timestamp) and $run_timestamp =~ /^(\d{4})-(\d{2})-(\d{2}) (\d{2}):(\d{2}):(\d{2})$/ ) {
	    ( $year, $month, $month_day, $hour, $minute, $second ) = ( $1, $2, $3, $4, $5, $6 );
	    ## We always use a 4-digit year when calling timelocal(), to avoid any confusion about how it is treating this value,
	    ## since it makes a mess of interpreting this time component.  See "perldoc Time::Local" for details.
	    my $message_retention_end_time =
	      timelocal_nocheck( $second, $minute, $hour, $month_day + $post_archiving_retention_days_for_messages, $month - 1, $year );
	    my $performance_data_retention_end_time =
	      timelocal_nocheck( $second, $minute, $hour, $month_day + $post_archiving_retention_days_for_performance_data, $month - 1, $year );

	    if ($message_retention_end_time >= $archive_end_time) {
		## We're still in the post-archive retention period for messages, for this run.
		## Make sure we extend the delete end time farther back to accommodate (not delete)
		## all data archived during this run, if necessary.
		if ($message_data_delete_end_time > $data_start_time{$run_timestamp}) {
		    $message_data_delete_end_time = $data_start_time{$run_timestamp};
		}
	    }
	    if ($performance_data_retention_end_time >= $archive_end_time) {
		## We're still in the post-archive retention period for performance data, for this run.
		## Make sure we extend the delete end time farther back to accommodate (not delete)
		## all data archived during this run, if necessary.
		if ($performance_data_delete_end_time > $data_start_time{$run_timestamp}) {
		    $performance_data_delete_end_time = $data_start_time{$run_timestamp};
		}
	    }
	}
	else {
	    log_timed_message "ERROR:  Bad internal operation; \$run_timestamp from \%data_start_time has an unexpected format!";
	    $outcome = 0;
	}
    }

    # The following timestamps are used in database accesses, not in filename construction,
    # so we use standard ISO 8601 punctuation (except with a space instead of a T separator
    # between date and time, as the database expects).
    if ( defined $message_data_delete_start_time ) {
	( $second, $minute, $hour, $month_day, $month, $year, $week_day, $year_day, $is_dst ) = localtime($message_data_delete_start_time);
	$message_data_delete_start_timestamp =
	  sprintf "%04d-%02d-%02d %02d:%02d:%02d", $year + 1900, $month + 1, $month_day, $hour, $minute, $second;
    }
    if ( defined $message_data_delete_end_time ) {
	( $second, $minute, $hour, $month_day, $month, $year, $week_day, $year_day, $is_dst ) = localtime($message_data_delete_end_time);
	$message_data_delete_end_timestamp =
	  sprintf "%04d-%02d-%02d %02d:%02d:%02d", $year + 1900, $month + 1, $month_day, $hour, $minute, $second;
	log_timed_message "STATS:  Message          deletion period ends at:  $message_data_delete_end_timestamp" if $debug_minimal;
    }
    if ( defined $performance_data_delete_start_time ) {
	( $second, $minute, $hour, $month_day, $month, $year, $week_day, $year_day, $is_dst ) = localtime($performance_data_delete_start_time);
	$performance_data_delete_start_timestamp =
	  sprintf "%04d-%02d-%02d %02d:%02d:%02d", $year + 1900, $month + 1, $month_day, $hour, $minute, $second;
    }
    if ( defined $performance_data_delete_end_time ) {
	( $second, $minute, $hour, $month_day, $month, $year, $week_day, $year_day, $is_dst ) = localtime($performance_data_delete_end_time);
	$performance_data_delete_end_timestamp =
	  sprintf "%04d-%02d-%02d %02d:%02d:%02d", $year + 1900, $month + 1, $month_day, $hour, $minute, $second;
	log_timed_message "STATS:  Performance data deletion period ends at:  $performance_data_delete_end_timestamp" if $debug_minimal;
    }

    return $outcome;
}

sub compute_file_deletion_period {
    my ( $second, $minute, $hour, $month_day, $month, $year, $week_day, $year_day, $is_dst );
    my $outcome = 1;

    if ( $run_start_timestamp =~ /^(\d{4})-(\d{2})-(\d{2}) (\d{2}):(\d{2}):(\d{2})$/ ) {
	my ( $year, $month, $month_day, $hour, $minute, $second ) = ( $1, $2, $3, $4, $5, $6 );

	# $source_dumpfile_retention_days may be expressed in fractional days.  As such, we need to convert
	# any fractional part to corresponding smaller units, and them subtrace them from our time base.
	#
	# We could have made this calculation easier by just computing:
	#
	#     $earliest_file_retention_time = $run_start_time - $source_dumpfile_retention_days * SECONDS_PER_DAY;
	#
	# but then the integral "day" component of $source_dumpfile_retention_days might not get us back
	# the corresponding number of exact calendar days, due to Daylight Savings Time adjustments.

	my $remaining_dumpfile_retention_days = $source_dumpfile_retention_days;
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

	# Beyond just taking $source_dumpfile_retention_days into account when deleting files, we never immediately
	# delete the files we just processed in this run.  This additional constraint logically provides an extra
	# level of protection against deleting data too quickly (although with $source_dumpfile_retention_days
	# constrained to be at least 1, and $script_run_time reflecting the time when we run this script, this test
	# should never be true).  Still, some day, this ultra-conservative approach will save us from disaster.
	#
	# FIX MAJOR:  Perhaps we should impose a further constraint, processing old state-file information so we
	# also never delete the files from the last previous successful run during this run.  Are we satisfied
	# that our retention model is sufficiently robust that we don't need such extra hardcoded protection?
	if ( $earliest_file_retention_time >= $script_run_time ) {
	    $earliest_file_retention_time = $script_run_time - 1;
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

    return $outcome;
}

# This routine is just a placeholder left over from early development, because we are specifying
# all the tables to archive directly in the config file.  We cannot limit the specified tables to
# just the secondary tables, and allow the rest of table names to be automatically derived from
# the secondary-table names using the database's information_schema associations, for two reasons.
#
# (*) We also want to archive a number of tables which are not directly connected to the secondary tables.
#     So those tables must be explicitly listed, apart from the secondary tables.
#
# (*) We need to specify a lot of ancillary information about the archived tables in the receiving-side
#     config file, and we want to keep the lists of tables on the sending and receiving sides synchronized
#     so all parties are known to agree on what is to take place.
#
# Perhaps this routine will have some useful function in a future release, such as validating the lists
# of tables.
sub compute_tables_to_archive {
    my $outcome = 1;
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

sub delete_old_source_files {
    my $outcome = 1;
    ## Our strategy for deleting files is simple:  the file reader always has ownership and is in charge of deleting
    ## them when it decides they are no longer useful.  The determination of "owner" is a bit subtle, though, because
    ## we support several different setups.  Specifically, the target script machine may or may not be the same machine
    ## as the source script machine, and (in theory, at least) the $log_archive_source_data_directory may or may not be
    ## the same as the $log_archive_target_data_directory.  So we need to carefully determine who is the reader of the
    ## files in the $log_archive_source_data_directory, which are the only files that this log-archive-send.pl script
    ## will have authority to delete.
    if ( $target_script_machine ne 'localhost' or $log_archive_source_data_directory ne $log_archive_target_data_directory) {
	if (opendir BASEDIR, $log_archive_source_data_directory) {
	    my @allfiles = readdir BASEDIR;
	    closedir BASEDIR;
	    foreach my $file (@allfiles) {
		if ($file =~ /^(\d{4})-(\d{2})-(\d{2})_(\d{2})\.(\d{2})\.(\d{2})$/ and !-l "$log_archive_source_data_directory/$file" and -d _) {
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
			log_timed_message "WARNING:  Directory $log_archive_source_data_directory/$file has an invalid time value; it will not be deleted.";
		    }
		    else {
			if ($directory_time < $earliest_file_retention_time) {
			    log_timed_message "NOTICE:  Cleaning up the $log_archive_source_data_directory/$file directory.";
			    delete_data_subdirectory ("$log_archive_source_data_directory/$file");
			}
		    }
		}
	    }
	}
	else {
	    log_timed_message "ERROR:  Could not open directory $log_archive_source_data_directory for reading ($!).";
	    ## We don't reflect this condition in $outcome to stop the rest of the script, because
	    ## by the time we get here, we've already done the critical work in this script.  So
	    ## there's no point in stopping now.
	}
    }
    return $outcome;
}

sub delete_old_rows_from_table {
    my $table                  = shift;
    my $delete_start_timestamp = shift;
    my $delete_end_timestamp   = shift;

    my $outcome      = 1;
    my $deleted_rows = 0;

    my $where_clause = '';
    my $time_column  = $time_field{$table};
    if ( defined($time_column) ) {
	## We always require the $delete_end_timestamp to be defined, because we're never interested
	## in completely deleting all content of a given table.  But generally, the $delete_start_timestamp
	## won't be defined, because there's no point in creating a hole in the data and leaving behind some
	## data from before that hole.  Deletion of rows in the logmessage table is special-cased in order
	## to leave behind the last status marker for each host or service as it was known just before the
	## $delete_end_timestamp.  This is necessary to provide initial status for availability graphing.
	if ( defined($delete_end_timestamp) ) {
	    $where_clause = "where";
	    $where_clause .= " \"$time_column\" >= '$delete_start_timestamp' and" if defined $delete_start_timestamp;
	    $where_clause .= " \"$time_column\" < '$delete_end_timestamp'";
	    $where_clause .= "
		and logmessageid not in
		    (
			(
			select distinct on (l.hoststatusid)
			    l.logmessageid
			from
			    \"$table\" l,
			    applicationtype a
			where
			    a.applicationtypeid = l.applicationtypeid
			and a.name != 'SYSTEM'
			and l.hoststatusid is not null
			and l.servicestatusid is null
			and l.\"$time_column\" < '$delete_end_timestamp'
			order by l.hoststatusid, l.\"$time_column\" desc
			)
		    UNION ALL
			(
			select distinct on (l.hoststatusid, l.servicestatusid)
			    l.logmessageid
			from
			    \"$table\" l,
			    applicationtype a
			where
			    a.applicationtypeid = l.applicationtypeid
			and a.name != 'SYSTEM'
			and l.hoststatusid is not null
			and l.servicestatusid is not null
			and l.\"$time_column\" < '$delete_end_timestamp'
			order by l.hoststatusid, l.servicestatusid, l.\"$time_column\" desc
			)
		    )
		" if $table eq 'logmessage';
	}
	else {
	    log_timed_message "ERROR:  Bad internal operation; \$delete_end_timestamp is not defined!";
	    $outcome = 0;
	}
    }
    else {
	log_timed_message "ERROR:  Improper configuration discovered during row deletion:";
	log_timed_message "        table \"$table\" has no associated time-based field.";
	log_timed_message "        Should this table even be listed as a secondary table?";
	$outcome = 0;
    }

    if ($outcome) {
	$deleted_rows = $dbh->do("delete from \"$table\" $where_clause");
	if ( defined $deleted_rows ) {
	    if ( $deleted_rows >= 0 ) {
		$rows_deleted{$table} = $deleted_rows;
		$total_rows_deleted += $deleted_rows;
	    }
	    else {
		## There is no apparent error, but for some reason the number of deleted rows is unknown or not available.
		## I don't know under what circumstances this could ever occur, but if it does, we at least want to
		## know that it's happening, so we understand why this table is omitted from the end-of-run statistics.
		log_timed_message "NOTICE:  Cannot determine the number of rows deleted from table \"$table\".";
	    }
	}
	else {
	    log_timed_message "ERROR:  Delete of rows in table \"$table\" failed.";
	    log_timed_message "        Database error is: ", $dbh->errstr;
	    $outcome = 0;
	}
    }

    return $outcome;
}

sub delete_old_database_rows {
    my $outcome = 1;

    # Everything we've done with the database up to this point had no business modifying it,
    # so we disabled the ability to do so as a precaution against development slip-ups.
    # Now, though, we need to give ourselves full power.
    if (not defined $dbh->do("set session characteristics as transaction read write")) {
	log_timed_message "ERROR:  Cannot set session transaction access mode to read/write; no database deletions are possible.";
	log_timed_message "        Database error is: ", $dbh->errstr;
	$outcome = 0;
    }
    else {
	## We ONLY delete rows from the @secondary_tables, NEVER from the @primary_tables or @tertiary_tables,
	## because the only data we want to be deleting from the runtime database is log-type data.  Unless it
	## is cascade-deleted using already-established foreign-key-reference clauses, any data in the other
	## tables must persist in the database except as it is managed by ordinary runtime operations.
	foreach my $table (@message_data_tables) {
	    $outcome &= delete_old_rows_from_table($table, $message_data_delete_start_timestamp, $message_data_delete_end_timestamp);
	    ## By the time we get to running delete_old_database_rows(), all the other work in this script
	    ## has already been done (successfully enough to allow us to discard data now).  For that reason,
	    ## if we have trouble deleting data from any individual table, there is no reason to abort the
	    ## deletions and not attempt deleting data from any remaining tables.  So we don't exit this
	    ## loop upon error.
	}
	foreach my $table (@performance_data_tables) {
	    $outcome &= delete_old_rows_from_table($table, $performance_data_delete_start_timestamp, $performance_data_delete_end_timestamp);
	    ## By the time we get to running delete_old_database_rows(), all the other work in this script
	    ## has already been done (successfully enough to allow us to discard data now).  For that reason,
	    ## if we have trouble deleting data from any individual table, there is no reason to abort the
	    ## deletions and not attempt deleting data from any remaining tables.  So we don't exit this
	    ## loop upon error.
	}
    }
    return $outcome;
}

sub capture_database_table {
    my $table                   = shift;
    my $time_column             = shift;
    my $join_table              = shift;
    my $join_expression         = shift;
    my $source_directory        = shift;
    my $capture_start_timestamp = shift;
    my $capture_end_timestamp   = shift;
    my $source_file             = shift;
    my $outcome                 = 1;
    my ( $second, $minute, $hour, $month_day, $month, $year, $week_day, $year_day, $is_dst ) = localtime;
    my $dump_timestamp = sprintf "%04d-%02d-%02d_%02d.%02d.%02d", $year + 1900, $month + 1, $month_day, $hour, $minute, $second;

    $$source_file = "$source_directory/$table.dump.$dump_timestamp";

    # This implementation only supports PostgreSQL, because it depends on specific capabilities of the PostgreSQL DBD::Pg driver.
    if ( $runtime_dbtype eq 'postgresql' ) {
	if ( $dumpfile_format eq 'copy' ) {
	    ## Here we use the client connection as a transport from the database to the file, both because we're not
	    ## assuming database superuser privileges (needed for the server to write directly to a file), and because
	    ## the database might be located on a remote server, which would not have access to our local filesystem.
	    ##
	    ## Note that we depend on the caller to have already created any ancestor directories needed here.
	    if ( sysopen( DUMP, $$source_file, O_WRONLY | O_APPEND | O_CREAT | O_EXCL, 0600 ) ) {
		my $dump_is_good  = 1;
		my $captured_rows = 0;
		my $from_clause   = "from \"$table\"";
		my $where_clause  = undef;
		if ( defined($time_column) ) {
		    ## FIX MAJOR:  We need to understand issues of concurrency with regard to the $time_column and $capture_end_timestamp;
		    ## if we try to capture data too quickly after it is reported to the monitoring system, there could be some transactions
		    ## still in flight within Nagios or Foundation that we do not capture because they're not yet in the database when we
		    ## probe here, even though their reportdate or lastchecktime field will lie within the start..end filter we apply here
		    ## once the data eventually is deposited in the database.  Such data will be rejected in the next daily pass of data
		    ## capture, because the start..end filter applied then will have moved on; so such data will never be archived.  How can
		    ## we ensure that all in-flight data that we need to capture here has been flushed out of Foundation to the database?
		    ## Currently, the only way is to wait long enough to allow the data to filter all the way through to the database.
		    ## That argues for running this sending script not immediately after midnight, but perhaps 30 or 45 minutes thereafter.
		    ##
		    ## For some tables, that issue is resolved by joining to a related table and using that table's timestamp filtering,
		    ## to guarantee consistency of the captured data snapshot across tables.
		    if ( defined($capture_start_timestamp) or defined($capture_end_timestamp) ) {
			## Joining by itself doesn't make sense unless we also have a WHERE clause to limit the retrieved data, since we're
			## only pulling columns from $table.  So we only extend $from_clause with a join clause if the join will actually
			## be useful.  That's why this next line is located here, as opposed to when $from_clause is initially defined.
			$from_clause .= " INNER JOIN $join_table ON $join_expression" if defined($join_table) and defined($join_expression);

			(my $quoted_time_column = $time_column) =~ s/([a-z_]+)/"$1"/g;
			$where_clause = "where";
			$where_clause .= " $quoted_time_column >= '$capture_start_timestamp'" if defined($capture_start_timestamp);
			$where_clause .= " and" if defined($capture_start_timestamp) and defined($capture_end_timestamp);
			$where_clause .= " $quoted_time_column < '$capture_end_timestamp'" if defined($capture_end_timestamp);
		    }
		    else {
			log_timed_message "ERROR:  Internal failure (no capture start or end time provided for a table that requires a constraint); aborting capture of table $table!";
			$dump_is_good = 0;
		    }
		}
		if ($dump_is_good) {
		    # Just to be clear, $runtime_data will generally end up taking one of 3 forms:
		    #
		    #     -- the table name, for copying the entire table
		    #     "$table"
		    #
		    #     -- restricting the table content to a given time range listed in that table
		    #     (
		    #     select "logmessage".*
		    #     from "logmessage"
		    #     where "reportdate" >= '2000-02-08 00:00:00' and "reportdate" < '2013-02-08 00:00:00'
		    #     )
		    #
		    #     -- restricting the table content to a given time range listed in a related table
		    #     (
		    #     select "logmessageproperty".*
		    #     from "logmessageproperty" INNER JOIN logmessage ON logmessageproperty.logmessageid=logmessage.logmessageid
		    #     where "logmessage"."reportdate" >= '2000-02-08 00:00:00' and "logmessage"."reportdate" < '2013-02-08 00:00:00'
		    #     )
		    #
		    my $runtime_data = defined($where_clause) ? "(select \"$table\".* $from_clause $where_clause)" : "\"$table\"";
		    log_timed_message "DEBUG:  COPY selection for table $table is:\n$runtime_data" if $debug_basic;
		    if (not defined $dbh->do("COPY $runtime_data TO STDOUT")) {
			my $errstr = $dbh->errstr;
			chomp $errstr if defined $errstr;
			log_timed_message "ERROR:  Cannot put the database connection into COPY OUT mode ($errstr); aborting capture of table $table!";
			$dump_is_good = 0;
		    }
		    else {
			my @data = ();
			$#data = $dump_copy_block_rows;    # pre-extend the array, for efficiency
			$#data = -1;                       # truncate the array, since we don't have any data yet
			my $x    = 0;
			my $size = 0;
			while (1) {
			    ## NOTE:  The DBD::Pg 2.19.3 documentation (http://search.cpan.org/~turnstep/DBD-Pg/Pg.pm#pg_getcopydata)
			    ## example of how to fill @data is wrong; it violates the condition expressed just above the example, that
			    ## the variable cannot be undefined.  This means that you have to leave the array pre-extended, with defined
			    ## values, and then you cannot use the array size to figure out how much data you have; you must use the
			    ## incremented index to extract an appropriate range of values from the array.  Or you can do what we do
			    ## here, pass a reference to the variable to be populated; testing shows that this works.  This should be
			    ## reported to the upstream maintainers.
			    if ( ( $size = $dbh->pg_getcopydata( \$data[ $x++ ] ) ) >= 0 ) {
				++$captured_rows;
			    }
			    if ( $size < 0 || $x >= $dump_copy_block_rows ) {
				## The DBD::Pg documentation says that, once the server is in COPY TO mode, no other SQL
				## commands are allowed until the final pg_getcopydata() has been called to retrieve the
				## last of the data.  This provides no way to abort the copying over the client/server
				## connection in the middle, in a situation like this where we get a file-writing error.
				## We ought to report this problem upstream, and see if we can get the package fixed to
				## provide some sort of data-transfer interrupt capability.  In the meantime, our error
				## handling here has to allow the data fetching to continue; we just turn off writing to
				## the file, to speed up getting out of this mess.
				if ( @data and $dump_is_good and not print DUMP @data ) {
				    log_timed_message "ERROR:  Cannot write to file \"$$source_file\" ($!); aborting capture of table $table!";
				    $dump_is_good = 0;
				}
				last if $size < 0;
				@data = ();
				$x    = 0;
			    }
			}
		    }
		}
		if ( not close DUMP ) {
		    log_timed_message "ERROR:  Cannot close file \"$$source_file\" ($!); aborting capture of table $table!";
		    $dump_is_good = 0;
		}

		# FIX MAJOR:  Perhaps delete the dump file if the dump is not good, to prevent anyone
		# confusing it with a complete and useable file.  Are we already doing that below?
		# Perhaps we should only keep the dump file if $debug_maximal is true.

		if ($dump_is_good) {
		    ## We only save the statistics if the dump is good.  That means that if this table capture failed,
		    ## the end-of-run statistics won't reflect the time spent on the table up to the point of failure,
		    ## which could be a bit misleading with regard to the $row_capture_speed.  We might address that in
		    ## a future release, when we might perhaps track the dump times on a per-table basis.  Until then,
		    ## this decision seems acceptable.
		    $rows_captured{$table} = $captured_rows;
		    $total_rows_captured += $captured_rows;
		}
		else {
		    ## We were able to open the file, so we're going to presume here that we have permission
		    ## to clean up by removing it.  A downside is that we're removing the forensic evidence.
		    ## If we run into problems like this in the field, we might want to comment out the unlink
		    ## to help in diagnosis of the problem; in that case, the file should eventually be purged
		    ## as part of ordinary cleanup of old source-machine data files.
		    unlink $$source_file;
		    $$source_file = undef;
		    $outcome = 0;
		}
	    }
	    else {
		log_timed_message "ERROR:  Cannot open file \"$$source_file\" ($!); aborting capture of table $table!";
		$$source_file   = undef;
		$outcome = 0;
	    }
	}
	else {
	    log_timed_message "ERROR:  Unknown dumpfile_format \"$dumpfile_format\"; aborting capture of table $table!";
	    $$source_file   = undef;
	    $outcome = 0;
	}
    }
    else {
	log_timed_message "ERROR:  runtime_dbtype \"$runtime_dbtype\" is not supported; aborting capture of table $table!";
	$$source_file   = undef;
	$outcome = 0;
    }

    return $outcome;
}

sub capture_all_tables {
    my $source_directory = shift;
    my $outcome      = 1;
    my @source_files = ();

    # We must always capture references to rows before we capture the referred-to rows, so we know that we
    # have not captured any dangling references that might show up if we captured referred-to rows and then
    # references to such rows (which might then include some new references, to rows not just captured).
    # So we intentionally save the tables in this order:  tertiary, secondary, primary.  This requirement
    # could perhaps be relaxed if we implemented some kind of snapshot-freeze (perhaps a savepoint?) in the
    # database before we capture data from any of the tables, and perhaps in the future we might do that
    # instead of insisting on a particular ordering.  In the meantime, though, we depend not just on the
    # ordering given in the following foreach loop, but also on the ordering given in the config file for
    # each of the arrays listed in the loop control, since there are some cross-table references even between
    # tables listed in the same array.  If we don't want a snapshot-freeze but we also don't want to force the
    # config file to be in the required ordering, we would need to dive into the database (that is, look at
    # the information_schema in detail) in the compute_table_archive_order() routine, find all the foreign-key
    # references between tables, compute the correct ordering for cleanly capturing the full configured set of
    # tables, and pass the resulting all-tables sequence to here.

    ## The $source_directory must be absolutely new.  Not that it's likely, but we don't want an
    ## unexpected collision with a pre-existing directory causing confusion when we later go to
    ## transfer source files out of that directory to the target script machine.
    if (not mkdir $source_directory, 0700) {
	log_timed_message "ERROR:  Could not create source directory \"$source_directory\" ($!).";
	$outcome = 0;
    }
    else {
	## FIX MAJOR:  We have carefully sequenced the order in which tables are captured, to ensure that
	## references to rows are always captured before the referenced rows are captured.  This is supposed
	## to guarantee the ability to restore the tables (in the opposite order) from these snapshots, by
	## ensuring that the referenced rows are in place on the receiving side before we try to establish
	## references to them.  But what about the opposite scenario?  What happens if we capture references
	## to some rows, and then some of the referenced rows (along with the references in the database,
	## via cascading deletes) are deleted from the runtime database before we can capture all the
	## referenced rows we just captured references to?  Do we need to use transactions here, and/or
	## lock sets of tables, to ensure the necessary consistency across tables?  What guarantees do we
	## have that there will never be any offending deletions during the time we try to capture data?
	foreach my $table ( @tertiary_tables, @secondary_tables, @primary_tables ) {
	    my $time_column     = $time_field{$table};
	    my $join_table      = undef;
	    my $join_expression = $join_condition{$table};
	    ## If we have a join expression, we must determine here how the data will be time-filtered
	    ## to correspond to just rows that will be extracted from the joined-to table.  We assume
	    ## that at most only one other table will be joined to, and thus only at most one other
	    ## table will need to be handled in this fashion.
	    if ( defined $join_expression ) {
		foreach my $constrained_table ( keys %time_field ) {
		    if ( $join_expression =~ /(^|[^a-z_])$constrained_table([^a-z_]|$)/ ) {
			$join_table  = $constrained_table;
			$time_column = "$constrained_table.$time_field{$constrained_table}";
			last;
		    }
		}
	    }
	    my $capture_start_timestamp = defined($time_column) ? $archive_start_timestamp : undef;
	    my $capture_end_timestamp   = defined($time_column) ? $archive_end_timestamp   : undef;
	    my $source_file             = undef;
	    my $status = capture_database_table( $table, $time_column, $join_table, $join_expression, $source_directory,
		$capture_start_timestamp, $capture_end_timestamp, \$source_file );
	    push @source_files, $source_file if defined $source_file;
	    if ($status) {
		++$total_tables_captured;
	    }
	    else {
		$outcome = 0;
		## Once we see a failure with one table, there's no point in continuing with other tables,
		## because we're just going to discard all the source files we write in this run.
		last;
	    }
	}
	if ( not $outcome ) {
	    ## We delete all the source files we just created, since without the full set, we
	    ## won't be sending any of this data to the archive database.  You might want to
	    ## disable this deletion for development and debugging purposes, but we don't
	    ## presently have a command-line or config-file control for doing so.
	    log_timed_message "NOTICE:  Removing all source files just created, due to errors noted above.";
	    foreach my $source_file (@source_files) {
		## Not that we think there is anything significant to worry about, but for general
		## security purposes, we unlink only if it's not a symlink, and if it's a true file,
		## not a directory or some other filesystem object.
		unlink $source_file if !-l $source_file and -f _;
	    }
	    @source_files = ();
	    if (rmdir $source_directory) {
		log_timed_message "NOTICE:  Source directory \"$source_directory\" has been deleted to clean up after previous errors.";
	    }
	    else {
		log_timed_message "ERROR:  Could not delete source directory \"$source_directory\" ($!).";
		## $outcome is already false at this point, so we don't bother setting it again.  (And
		## even if it weren't, the simple fact that immediate cleanup failed wouldn't necessarily
		## by itself be serious enough to prevent the rest of the script from running, although
		## it would indicate a significant failure that we ought to be concerned about with
		## respect to operation of the rest of the archiving.)
	    }
	}
    }
    return $outcome, \@source_files;
}

# The only changes we make to the database in this log-archive-send.pl script are
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
    if ( defined($runtime_dbtype) && $runtime_dbtype eq 'postgresql' ) {
	$dsn = "DBI:Pg:dbname=$runtime_dbname;host=$runtime_dbhost";
    }
    else {
	$dsn = "DBI:mysql:database=$runtime_dbname;host=$runtime_dbhost";
    }
    $dbh = DBI->connect( $dsn, $runtime_dbuser, $runtime_dbpass, { 'AutoCommit' => 1 } );
    if (!$dbh) {
	log_timed_message "ERROR:  Cannot connect to database $runtime_dbname: ", $DBI::errstr;
	return 0;
    }

    # All of the data-capture actions in this script don't need the ability to modify the
    # database, so we globally disable that capability so we are forcibly notified if we
    # violate our own rule.  This will be reverted at the end of the script, when we get
    # to where we need to delete old rows in certain tables.
    if (not defined $dbh->do("set session characteristics as transaction read only")) {
	log_timed_message "ERROR:  Cannot set session transaction access mode to read-only.";
	log_timed_message "        Database error is: ", $dbh->errstr;
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

sub transfer_files_to_target {
    my $files            = shift;
    my $target_directory = shift;
    my $outcome          = 1;

    # We will run the data injection processing on the target script machine, instead of using the
    # source script machine as a client and running all that activity through a remote database
    # connection.  To make that happen, we need to transfer the captured data to the target script
    # machine.  But in the degenerate case where we are simply archiving to a separate database still
    # on the source script machine where we stored all the files, there is no file transfer to be done.
    if ( $target_script_machine ne 'localhost' ) {
	## We transfer files individually mostly so we can determine the status of each separate transfer
	## and count the number of bytes successfully copied.  This strategy might slow down the operation
	## slightly, as multiple scp sessions will therefore have to be initiated, but any such delay is
	## considered to be of secondary importance to getting good statistics.
	if (@$files) {
	    ## We need to reach across to the $target_script_machine and create the new $target_directory on that
	    ## machine, before we go trying to scp files into it.
	    ##
	    ## We intentionally DO NOT use the mkdir -p option, for two reasons.  One, it will make parent directories
	    ## as needed, but we don't want to equip this code to create long chains of directories in potentially
	    ## arbitrary places in the system.  And two, we don't want to suppress an error if somehow the directory
	    ## already exists.  The name of the target directory is supposed to be timestamped, and therefore should
	    ## be unique to this run and not already exist.  If we find that it does exist, then we don't want to
	    ## trust that it doesn't contain any unexpected files.  So we just fail in that case.  The upshot is that
	    ## it is up to administrator performing the initial configuration steps at install time to create the
	    ## parent directory of the $target_directory we will create here.
	    my $make_directory_command = "$ssh -o NumberOfPasswordPrompts=0 \"nagios\@$target_script_machine\" $mkdir -m 700 \"$target_directory\" 2>&1";
	    my $mkdir_messages = `$make_directory_command`;
	    my $wait_status = $?;
	    if ( $wait_status != 0 ) {
		my $status_message = wait_status_message($wait_status);
		if (defined $mkdir_messages) {
		    chomp $mkdir_messages;
		}
		else {
		    $mkdir_messages = '';
		}
		$mkdir_messages = ($mkdir_messages eq '') ? '.' : ":\n$mkdir_messages";
		log_timed_message "ERROR:  Could not create target directory \"$target_script_machine:$target_directory\".";
		log_timed_message "ERROR:  Making the target directory failed with $status_message$mkdir_messages";
		$outcome = 0;
	    }
	    else {
		## We log any unexpected messages we get from the target side even in the case of success.
		if ( defined $mkdir_messages ) {
		    chomp $mkdir_messages;
		    if ( $mkdir_messages ne '') {
			log_timed_message "NOTICE:  The \"$target_script_machine:$target_directory\" directory has been successfully created.";
			log_timed_message "INFO:  Target-side messages follow:\n$mkdir_messages";
		    }
		}
		foreach my $file (@$files) {
		    ## For extra security protection, since part of the file pathname is derived from an
		    ## external source, we ensure that the filename is properly quoted, no matter what
		    ## characters it contains.  This is to prevent possible shell escaping.
		    (my $safe_filename = $file) =~ s/(.)/\\$1/g;

		    # FIX LATER:  Is there some Perl-only way to perform the scp operation?

		    # When this script is run in non-interactive mode (its usual operating condition), STDERR
		    # will be redirected to the log file by the GW::Logger package, so in that sense we don't
		    # need 2>&1 to be embedded in the $transfer_command.  But then any error messages emitted
		    # from the $transfer_command would be immediately inserted directly into the log file before
		    # we got a chance here to explain their context with a preceding comment.  So we use an
		    # explicit redirection of STDERR in this command, even though it means we are forcing the
		    # overhead of an extra intermediate shell process, which we are otherwise loath to do.
		    my $transfer_command  = "$scp -o NumberOfPasswordPrompts=0 -p $safe_filename '$target_script_machine:$target_directory' 2>&1";
		    my $transfer_messages = `$transfer_command`;
		    my $wait_status       = $?;
		    if ($wait_status == 0) {
			my $size = -s $file;
			if (defined $size) {
			    $total_transferred_file_bytes += $size;
			    log_timed_message "NOTICE:  File $file ($size bytes) was successfully transferred.";
			}
			else {
			    log_timed_message "WARNING:  File $file was successfully transferred, but the file size is unknown.";
			}
		    }
		    else {
			my $status_message = wait_status_message($wait_status);
			chomp $transfer_messages if defined $transfer_messages;
			log_timed_message "ERROR:  Transfer of $file failed with $status_message:\n$transfer_messages" if $debug_minimal;
			$outcome = 0;
			## Once we see a failure with one file, there's no point in continuing with other files,
			## because we're not going to use any of the files we are transferring in this run.
			last;
		    }
		}
	    }
	}
	else {
	    log_timed_message "WARNING:  No files to transfer to the target script machine.";
	}
    }
    elsif ( $log_archive_source_data_directory ne $log_archive_target_data_directory ) {
	if (@$files) {
	    if (-d $target_directory) {
		## The name of the target directory is supposed to be timestamped, and therefore should be
		## unique to this run and not already exist.  If we find that it does exist, then we don't
		## want to trust that it doesn't contain any unexpected files.  So we just fail in that case.
		log_timed_message "ERROR:  Target directory \"$target_directory\" already exists.";
		$outcome = 0;
	    }
	    elsif (not mkdir $target_directory, 0700) {
		log_timed_message "ERROR:  Could not create target directory \"$target_directory\" ($!).";
		$outcome = 0;
	    }
	    else {
		foreach my $file (@$files) {
		    ## For extra security protection, since part of the file pathname is derived from an
		    ## external source, we ensure that the filename is properly quoted, no matter what
		    ## characters it contains.  This is to prevent possible shell escaping.
		    (my $safe_filename = $file) =~ s/(.)/\\$1/g;

		    # FIX LATER:  We could avoid forking child and grandchild processes, and use only Perl i/o,
		    # either by local read/write operations implemented here or by File::Copy or some similar
		    # package, to do the copying instead of delegating the work to a separate process.

		    # When this script is run in non-interactive mode (its usual operating condition), STDERR
		    # will be redirected to the log file by the GW::Logger package, so in that sense we don't
		    # need 2>&1 to be embedded in the $transfer_command.  But then any error messages emitted
		    # from the $transfer_command would be immediately inserted directly into the log file before
		    # we got a chance here to explain their context with a preceding comment.  So we use an
		    # explicit redirection of STDERR in this command, even though it means we are forcing the
		    # overhead of an extra intermediate shell process, which we are otherwise loath to do.
		    my $transfer_command  = "cp -p $safe_filename '$target_directory' 2>&1";
		    my $transfer_messages = `$transfer_command`;
		    my $wait_status       = $?;
		    if ($wait_status == 0) {
			my $size = -s $file;
			if (defined $size) {
			    $total_transferred_file_bytes += $size;
			    log_timed_message "NOTICE:  File $file ($size bytes) was successfully transferred.";
			}
			else {
			    log_timed_message "WARNING:  File $file was successfully transferred, but the file size is unknown.";
			}
		    }
		    else {
			my $status_message = wait_status_message($wait_status);
			chomp $transfer_messages if defined $transfer_messages;
			log_timed_message "ERROR:  Transfer of $file failed with $status_message:\n$transfer_messages" if $debug_minimal;
			$outcome = 0;
			## Once we see a failure with one file, there's no point in continuing with other files,
			## because we're not going to use any of the files we are transferring in this run.
			last;
		    }
		}
	    }
	}
	else {
	    log_timed_message "WARNING:  No files to transfer to the target directory.";
	}
    }
    else {
	log_timed_message "NOTICE:  The target_script_machine is localhost, and the source and";
	log_timed_message "         target directories match, so files are not being transferred.";
    }
    return $outcome;
}

sub inject_data_into_archive {
    my $target_directory = shift;
    my $outcome          = 1;

    # First check to see the name of the target script machine.  If it is localhost,
    # alter the command executed to inject the data so it runs locally.

    # FIX MAJOR:  We have used the -i option here, as it is logically useful to have it in place, and it is
    # required if we also want the -o option in play.  But this disables having the called script automatically
    # have its logging module redirect STDERR to its log file.  Is that appropriate?  It seems not, as then the
    # receiving log file no longer captures Perl errors.  More testing, and probably some program adjustment,
    # is needed.  Not having timelocal_nocheck() defined in the receiving script serves as a good test that will
    # generate a Perl error and cause the receiving script to bomb out.
    #
    # It might not matter, since we redirect STDERR using 2>&1 right here in the command, in order to capture
    # any stray unexpected messages from ssh or Perl or the DBI module.  There might be a downside in that
    # perhaps we will lose the clean message interleaving we desire, but I suppose we can live with that.

    # When this script is run in non-interactive mode (its usual operating condition), STDERR
    # will be redirected to the log file by the GW::Logger package, so in that sense we don't need
    # 2>&1 to be embedded in the $data_injection_command.  But then any error messages emitted
    # from the $data_injection_command would be immediately inserted directly into the log file
    # before we got a chance here to explain their context with a preceding comment.  So we use
    # an explicit redirection of STDERR in this command, even though it means we are forcing the
    # overhead of an extra intermediate shell process, which we are otherwise loath to do.
    my $data_injection_command = ($target_script_machine eq 'localhost')
	? "$log_archive_bin/log-archive-receive.pl -i -x $target_directory 2>&1"
	: "$ssh -o NumberOfPasswordPrompts=0 \"nagios\@$target_script_machine\" $log_archive_bin/log-archive-receive.pl -i -x $target_directory 2>&1";
    ## If the caller asked for unified logging, we ask the receiving side to print all of its log messages also
    ## to STDOUT, from which we will capture them here and save them in the sending-side log file as well.
    $data_injection_command .= ' -o' if $unified_logging;
    my $injection_messages = `$data_injection_command`;
    my $wait_status = $?;
    if ( $wait_status == 0 ) {
	## We log any messages we get from the receiving side even in the case of success, so we can see
	## in one place (the sending-side log file) a complete picture of the entire archiving cycle.
	if (defined($injection_messages) and $injection_messages ne '') {
	    chomp $injection_messages;
	    log_timed_message "INFO:  Receiving-side messages follow:\n$injection_messages";
	    log_timed_message "INFO:  End of receiving-side messages.";
	}
	log_timed_message "NOTICE:  All data in "
	  . ( $target_script_machine eq 'localhost' ? '' : "$target_script_machine:" )
	  . "$target_directory was successfully injected into the archive database.";
    }
    else {
	my $status_message = wait_status_message($wait_status);
	chomp $injection_messages if defined $injection_messages;
	$injection_messages = 'See the log archive receive log file for details.' if !defined($injection_messages) or $injection_messages eq '';
	## After spilling out any messages from the receiving side, we repeat the basic failure message, because
	## without the extra copy of this notification, when you're looking at the end of a long series of the
	## receiver's log messages in a unified log file, the fact that it didn't work properly gets lost.
	log_timed_message "ERROR:  Data injection failed with $status_message:\n$injection_messages";
	log_timed_message "ERROR:  Data injection failed with $status_message, as noted above.";
	$outcome = 0;
    }
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

    ( $second, $minute, $hour, $month_day, $month, $year, $week_day, $year_day, $is_dst ) = localtime(POSIX::floor($script_start_time));
    my $script_start_timestamp = sprintf "%04d-%02d-%02d %02d:%02d:%02d", $year + 1900, $month + 1, $month_day, $hour, $minute, $second;
    ( $second, $minute, $hour, $month_day, $month, $year, $week_day, $year_day, $is_dst ) = localtime(POSIX::ceil($script_delete_data_end_time));
    my $script_end_timestamp = sprintf "%04d-%02d-%02d %02d:%02d:%02d", $year + 1900, $month + 1, $month_day, $hour, $minute, $second;

    $init_time         = $script_init_end_time - $script_start_time;
    $capture_time      = $script_capture_end_time - $script_init_end_time;
    $transfer_time     = $script_transfer_end_time - $script_capture_end_time;
    $injection_time    = $script_injection_end_time - $script_transfer_end_time;
    $delete_files_time = $script_delete_files_end_time - $script_injection_end_time;
    $delete_data_time  = $script_delete_data_end_time - $script_delete_files_end_time;
    $total_time        = POSIX::ceil($script_delete_data_end_time - $script_start_time);

    $init_timestamp         = format_hhmmss_timestamp($init_time);
    $capture_timestamp      = format_hhmmss_timestamp($capture_time);
    $transfer_timestamp     = format_hhmmss_timestamp($transfer_time);
    $injection_timestamp    = format_hhmmss_timestamp($injection_time);
    $delete_files_timestamp = format_hhmmss_timestamp($delete_files_time);
    $delete_data_timestamp  = format_hhmmss_timestamp($delete_data_time);
    $total_timestamp        = format_hhmmss_timestamp($total_time);

    # All speed measurements are "per second".
    $row_capture_speed   = $capture_time > 0     ? sprintf( "%12.3f", $total_rows_captured / $capture_time )           : 'indeterminate';
    $byte_transfer_speed = $transfer_time > 0    ? sprintf( "%12.3f", $total_transferred_file_bytes / $transfer_time ) : 'indeterminate';
    $row_deletion_speed  = $delete_data_time > 0 ? sprintf( "%12.3f", $total_rows_deleted / $delete_data_time )        : 'indeterminate';

    log_timed_message "STATS:  Log archive sending statistics:";
    log_message "Sending script started at:  $script_start_timestamp";
    log_message "Sending script   ended at:  $script_end_timestamp";
    log_message         "$init_timestamp taken to initialize the sending script";
    log_message      "$capture_timestamp taken to run the capture phase on the runtime database";
    log_message     "$transfer_timestamp taken to transfer the data to the target script machine";
    log_message    "$injection_timestamp taken to run the injection phase on the archive database";
    log_message "$delete_files_timestamp taken to delete old files from the filesystem";
    log_message  "$delete_data_timestamp taken to delete old data from the database";
    log_message        "$total_timestamp taken to run the entire sending and receiving sides of the archiving cycle";

    log_message sprintf("%8d tables from which data was captured", $total_tables_captured);
    log_message sprintf("%8d rows of data were captured from the runtime database", $total_rows_captured);
    log_message sprintf("%8d bytes of data (%.3f MB) were transferred", $total_transferred_file_bytes, $total_transferred_file_bytes / 1_000_000);
    log_message sprintf("%8d old files were deleted", $total_files_deleted);
    log_message sprintf("%8d old rows of data were deleted", $total_rows_deleted);

    foreach my $table (sort keys %rows_captured) {
	log_message sprintf("%8d rows of data were captured from the $table table", $rows_captured{$table});
    }

    foreach my $table (sort keys %rows_deleted) {
	log_message sprintf("%8d old rows of data were deleted from the $table table", $rows_deleted{$table});
    }

    log_message   "$row_capture_speed rows  captured    per second, over all tables";
    log_message "$byte_transfer_speed bytes transferred per second, over all files";
    log_message  "$row_deletion_speed rows  deleted     per second, over all tables";

    log_timed_message "STATS:  This pass of log archiving $cycle_status on the sending side$suffix.";

    # Reformat certain speed measurements for later use in a status message sent to Foundation.
    $row_capture_speed  = $capture_time > 0     ? sprintf( "%.1f", $total_rows_captured / $capture_time )    : 'indeterminate';
    $row_deletion_speed = $delete_data_time > 0 ? sprintf( "%.1f", $total_rows_deleted / $delete_data_time ) : 'indeterminate';

    # Calculate some other statistics for later use in a status message sent to Foundation.
    $message_rows_captured = 0;
    $message_rows_deleted  = 0;
    foreach my $table (@message_data_tables) {
	$message_rows_captured += $rows_captured{$table} || 0;
	$message_rows_deleted  +=  $rows_deleted{$table} || 0;
    }
    $perfdata_rows_captured = 0;
    $perfdata_rows_deleted  = 0;
    foreach my $table (@performance_data_tables) {
	$perfdata_rows_captured += $rows_captured{$table} || 0;
	$perfdata_rows_deleted  +=  $rows_deleted{$table} || 0;
    }
}
