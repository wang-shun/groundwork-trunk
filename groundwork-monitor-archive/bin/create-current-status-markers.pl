#!/usr/local/groundwork/perl/bin/perl -w --

# create-current-status-markers.pl

# Copyright (c) 2016 GroundWork, Inc. (www.gwos.com).  All rights reserved.
# Use of this software is subject to commercial license terms.

# This script creates logmessage rows that reflect the current status of
# hosts and services.  It is useful for repairing a damaged gwcollagedb
# which no longer has at least one such row for every host and service.
# Some caveats:
#
# (*) This script must be run on the machine where the log-archive-send.pl
#     script is normally run.
#
# (*) Typically, one runs this script with the -m option, to only capture
#     the current status for current hosts and/or services which are
#     completely missing from the logmessage table.
#
# (*) This script is normally only run on a one-time basis, to repair a
#     previously damaged gwcollagedb database.
#
# (*) This script is normally only run after the capture-old-status-markers.pl
#     and restore-old-status-markers.pl scripts have been run, to copy back
#     into the gwcollagedb database such markers as might still be available
#     in the archive_gwcollagedb database, reflecting data prior to some
#     data-deletion cutoff.  Then this create-current-status-markers.pl script,
#     under control of the -m option (to create only "missing" data), will just
#     handle the residual cases where no historical data at all is available.
#     See the full documentation for those other two scripts.

use strict;
use warnings;

use DBI;
use Getopt::Std;
use Time::HiRes;
use Socket;
use POSIX qw();

use TypedConfig;

use GW::Logger;
use GW::Foundation;

# ================================
# Package Parameters
# ================================

my $PROGNAME       = "create-current-status-markers.pl";
my $VERSION        = "0.0.7";
my $COPYRIGHT_YEAR = "2016";

my $default_config_file = '/usr/local/groundwork/config/log-archive-send.conf';
my $default_log_file    = '/usr/local/groundwork/foundation/container/logs/status-markers.log';

# ================================
# Command-Line Parameters
# ================================

# In theory, these parameter settings could be overridden by command-line arguments.
# In practice, we don't currently support any such arguments; this script uses only
# a fixed set of arguments.

my $config_file                = $default_config_file;
my $debug_config               = 0;                      # if set, spill out certain data about config-file processing to STDOUT
my $show_help                  = 0;
my $show_version               = 0;
my $run_interactively          = 0;
my $reflect_log_to_stdout      = 0;
my $roll_back_all_changes      = undef;
my $save_all_object_status     = undef;
my $save_missing_object_status = undef;
my $save_status_for_hosts      = undef;
my $save_status_for_services   = undef;

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

# We use the default log file which is hardcoded above, essentially just
# because all other configuration values we can draw from the config file
# which is shared with the log-archive-send.pl program, but it doesn't make
# sense to dump log output from this script into that script's log file.
my $logfile                = $default_log_file;
my $max_logfile_size       = undef;    # log rotate is handled externally, not here
my $max_logfiles_to_retain = undef;    # log rotate is handled externally, not here

my $runtime_dbtype = undef;
my $runtime_dbhost = undef;
my $runtime_dbport = undef;
my $runtime_dbname = undef;
my $runtime_dbuser = undef;
my $runtime_dbpass = undef;

my $source_script_machine    = undef;
my $source_script_ip_address = undef;

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
# Working Variables
# ================================

my $dbh = undef;

my $script_start_time                  = undef;
my $script_init_end_time               = undef;
my $script_host_processing_end_time    = undef;
my $script_service_processing_end_time = undef;

my $host_rows_created    = 0;
my $service_rows_created = 0;
my $total_rows_inserted  = 0;

my $process_outcome = undef;

# These variables really ought to just be local to the log_action_statistics() routine,
# except that we want a few of them to be accessible to the send_outcome_to_foundation()
# routine so the message it sends is more informative.
my $init_time           = undef;
my $host_time           = undef;
my $service_time        = undef;
my $insert_data_time    = undef;
my $total_time          = undef;
my $init_timestamp      = undef;
my $host_timestamp      = undef;
my $service_timestamp   = undef;
my $total_timestamp     = undef;
my $row_insertion_speed = undef;

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
    my @SAVED_ARGV = @ARGV;

    capture_timing(\$script_start_time);

    # If this script fails, and we have successfully made it past reading the config file (so we know how to send
    # messages to Foundation), the $status_message will be sent to Foundation, and show up in the Event Console.
    # Thus there is no point in defining $status_message in the code below until we have made it past that point.
    my $status_message = '';
    $process_outcome = 1;

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
	$process_outcome = 0;
    }

    if ($process_outcome) {
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

	# We don't use a message prefix, because this is intended to be an interactive script and
	# the extra text written to the terminal would just be distracting and useless there.  We
	# don't expect multiple concurrent copies of this script to be writing to the log file, so
	# we don't really have a need to disambiguate where each message comes from in that record.
	GW::Logger->new( $logfile, $run_interactively, $reflect_log_to_stdout, $max_logfile_size, $max_logfiles_to_retain, '' );

	if ( !open_logfile() ) {
	    ## The routine will print an error message if it fails, so we don't do so ourselves.
	    $status_message  = 'cannot open log file';
	    $process_outcome = 0;
	}
    }

    if ($process_outcome) {
	## We precede the startup message with a blank line, simply so the startup message is more visible.
	log_message '';
	log_timed_message "=== Status marker creation script (version $VERSION) starting up (process $$). ===";
	log_timed_message "INFO:  Running with options:  " . join (' ', @SAVED_ARGV);

	# For complete clarity, let's interpret the major program options, as specified on the command line.
	log_timed_message 'NOTICE:  Will'
	  . ( $roll_back_all_changes  ? ' do dry-run event creation' : ' create current events' )
	  . ( $save_all_object_status ? ' for all'                   : ' for' )
	  . ( $save_status_for_hosts  ? ' hosts'                     : '' )
	  . ( $save_status_for_hosts && $save_status_for_services ? ' and' : '' )
	  . ( $save_status_for_services ? ' services' : '' )
	  . ( $save_all_object_status   ? ''          : ' with missing status markers' ) . '.';

	# Since we are parasitizing the log-archiving send config file rather than
	# using one of our own, and the enable_processing flag in that config file
	# has no real business affecting the actions of this one-shot script, this
	# test makes no sense in the current context.  So we just disable it.
	if (0) {
	    if ( !$enable_processing ) {
		print "FATAL:  log-archive sending is not enabled in its config file.\n";
		log_timed_message "FATAL:  Stopping creation of current status markers (process $$)"
		  . " because processing is not enabled in the config file ($config_file).";
		$status_message  = 'processing is disabled in the config file';
		$process_outcome = 0;
	    }
	}
    }

    capture_timing(\$script_init_end_time);

    # Open a connection to the runtime database.
    if ($process_outcome) {
	log_timed_message "NOTICE:  Opening a connection to the runtime database.";
	$process_outcome = open_database_connection();
	$status_message = 'cannot connect to the runtime database' if not $process_outcome;
    }

    # Create new host rows in the runtime database.
    if ( $process_outcome && $save_status_for_hosts ) {
	log_timed_message "NOTICE:  Creating new host status rows in the runtime database.";
	$process_outcome = create_host_status_markers( $save_all_object_status, $roll_back_all_changes );
	$status_message = 'cannot create host status rows' if not $process_outcome;
    }

    capture_timing(\$script_host_processing_end_time);

    # Create new service rows in the runtime database.
    if ( $process_outcome && $save_status_for_services ) {
	log_timed_message "NOTICE:  Creating new service status rows in the runtime database.";
	$process_outcome = create_service_status_markers( $save_all_object_status, $roll_back_all_changes );
	$status_message = 'cannot create service status rows' if not $process_outcome;
    }

    capture_timing(\$script_service_processing_end_time);

    # Close the connection to the runtime database.  This is done unconditionally, since we should close
    # the connection even if errors occurred after it was opened.  The routine can internally handle the
    # case where the connection was never opened in the first place because of prior errors.  However,
    # it gets confusing if we log the occurrence of this call under circumstances when it won't actually
    # do anything, so we do qualify the logging here.
    log_timed_message "NOTICE:  Closing the connection to the runtime database." if $dbh and log_is_open();
    close_database_connection();

    log_action_statistics($status_message) if log_is_open();

    send_outcome_to_foundation( $status_message, $process_outcome );

    close_logfile();

    # Now return the overall processing success or failure as the status of this routine.
    # This will be turned into a corresponding script exit code.

    return $process_outcome ? STOP_STATUS : ERROR_STATUS;
}

sub print_version {
    print "$PROGNAME Version:  $VERSION\n";
    print "Copyright $COPYRIGHT_YEAR GroundWork, Inc. (www.gwos.com).\n";
    print "All rights reserved.\n";
}

sub print_usage {
    print <<EOF;

usage:  $PROGNAME -h
        $PROGNAME -v
        $PROGNAME -d [-c config_file]
        $PROGNAME [-n] {-a|-m} [-H] [-S] [-c config_file] [-i] [-o]

where:  -h:  print this help message
        -v:  print the version number
        -d:  debug config file
        -n:  make no permanent changes; roll back instead of committing them
        -a:  create rows for all hosts and/or services
        -m:  create rows only for hosts and/or services with missing rows
        -H:  create rows for hosts
        -S:  create rows for services
        -c config_file
             specifies an alternate config file; the default config file is:
             $default_config_file
        -i:  run interactively, not as a background process
        -o:  write log messages also to standard output

The usual (interactive) invocation is:

    $PROGNAME -m -H -S -i -o

When running this script to create markers, either -a or -m (but not both)
must be specified, and at least one of -H and -S must be specified.

The -o option is illegal unless -i is also specified.

The -n option is useful for dry-run testing, to avoid permanently
modifying the database so that repeated runs all start out with the
same setup.  It will run the expected queries and row insertions, then
roll them back out.  They will never be seen outside of this script's own
connection to the database.  This facility can help both with potential
code modifications and with performance testing on large databases.

EOF
}

# See http://en.wikipedia.org/wiki/Hostname#Restrictions_on_valid_host_names for the tests we run here.
# FIX LATER:  We should probably go further, and run a name-service lookup here, to validate that $hostname
# will actually be useable later on.
sub is_valid_hostname {
    my $hostname = shift;
    my $label    = '(?:[a-zA-Z0-9](?:[-a-zA-Z0-9]{0,61}[a-zA-Z0-9])?)';
    return ( defined($hostname) and $hostname ne '' and length($hostname) <= 255 and $hostname =~ /^$label(?:\.$label)*$/o );
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
	#
	# We don't accept the config-file setting for this value, because we are parasitically using
	# the config file for one of the log-archive scripts and we don't want log messages from
	# this script mixed in with log messages from the log archiving.  Instead, we just use the
	# default logfile path which is set as a hardcoded value at the beginning of this script.
	# If there were really some need to allow selection of a logfile distinct from our default
	# logfile for this script, we could implement a command-line -l option to provide the
	# appropriate override path.  For a script which is intended to only be run once (ever) at
	# a customer site, that seems pointlessly extravagant.
	#
	# $logfile = $config->get_scalar ('logfile');

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
    ## First, clean up the $default_config_file value in case we print usage.
    ## (This is disabled because of potential working-directory issues with realpath().)
    ## my $real_path = realpath ($default_config_file);
    ## $default_config_file = $real_path if $real_path;

    my %opts;
    if ( not getopts( 'hvc:dionamHS', \%opts ) ) {
	print_usage();
	return 0;
    }

    $show_help             = $opts{h};
    $show_version          = $opts{v};
    $config_file           = ( defined $opts{c} && $opts{c} ne '' ) ? $opts{c} : $default_config_file;
    $debug_config          = $opts{d};
    $run_interactively     = $opts{i};
    $reflect_log_to_stdout = $opts{o};

    $roll_back_all_changes      = $opts{n};
    $save_all_object_status     = $opts{a};
    $save_missing_object_status = $opts{m};
    $save_status_for_hosts      = $opts{H};
    $save_status_for_services   = $opts{S};

    # This test is not a full enforcement of intended exclusivity of the major
    # mode options, but it at least requires that you specify either -d or
    # -a or -m, if neither -h nor -v is specified, and if -a or -m is specified,
    # either -H or -S is also specified.
    if (   !$show_version
	&& !$show_help
	&& !$debug_config
	&& !( ( $save_all_object_status xor $save_missing_object_status ) && ( $save_status_for_hosts || $save_status_for_services ) ) )
    {
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
	## FIX MAJOR:  This next line is commented out because $Bin is not yet defined here.
	## $config_file = "$Bin/../config/$config_file" if $config_file !~ m{^/};
	my $real_path = realpath($config_file);
	if ( !$real_path ) {
	    spill_message
	      "FATAL:  The path to the $PROGNAME config file $config_file either does not exist or is inaccessible to this script running as ",
	      ( scalar getpwuid $> ), '.';
	    return 0;
	}
	$config_file = $real_path;
    }

    if ( !$run_interactively && $reflect_log_to_stdout ) {
	print_usage();
	return 0;
    }

    return 1;
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
		log_timed_message "ERROR:  Cannot call GW::Foundation::APP_ARCHIVE()." if log_is_open();
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
	"created $host_rows_created host rows, $service_rows_created service rows at $row_insertion_speed total rows/sec;"
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
	## For the short form of this log reference to be valid, we need our standard symlink
	## to be in place.  But partly because this script is likely to be run on older GWMEE
	## releases that we know do not contain it, we must test for such a symlink.
	## FIX LATER:  We could further check that the symlink actually points to where we expect it to,
	## even if that file does not exist (though it certainly ought to, by the time we get here).
	( my $log_file_only = $logfile ) =~ s{.*/}{};
	my $log_details =
	  -l "/usr/local/groundwork/logs/$log_file_only" ? " See logs/$log_file_only for details." : " See $logfile for details.";
	log_to_foundation( SEVERITY_WARNING, $status_message eq '' ? "\u$statistics." : "\u$status_message; $statistics.$log_details" );
    }
}

# The only changes we make to the database in this create-current-status-markers.pl script are
# individual row creates, so there is no interesting transaction behavior we need to control
# explicitly.  Therefore, we enable auto-commit on this connection, to keep our application
# code simple.  Note that if a PostgreSQL command fails under auto-commit mode, it will be
# automatically rolled back; the application does not need to take any action to make
# this happen.  (Under PostgreSQL, all changes made so far in the transaction are rolled
# back, any additional commands in the transaction are aborted as soon as the command is
# run, before they have a chance to make any changes, and the COMMIT or END that ends
# the transaction is automatically turned into a ROLLBACK; the application has no choice
# about this.  That behavior is not necessarily the case with other commercial databases,
# so this issue would need to be investigated if we ever wanted to port this code to some
# other database.)  So any failed creates in this script will be turned into no-ops, which
# is fine; a future run of this script ought to successfully perform similar creations,
# if the underlying problem gets resolved.
#
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
    ## We don't specify RaiseError and/or PrintError here because we
    ## perform all error checking and logging directly in this script.
    $dbh = DBI->connect( $dsn, $runtime_dbuser, $runtime_dbpass, { 'AutoCommit' => 1 } );
    if (!$dbh) {
	log_timed_message "ERROR:  Cannot connect to database $runtime_dbname: ", $DBI::errstr;
	return 0;
    }

    # The purpose of this script is to insert new rows into the database, so we enable that capability.
    # (That should already be the ordinary default; this is somewhat redundant.)
    if (not defined $dbh->do("set session characteristics as transaction read write")) {
	log_timed_message "ERROR:  Cannot set session transaction access mode to read/write; no database insertions are possible.";
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

sub create_host_status_markers {
    my $save_all_status   = shift;
    my $roll_back_changes = shift;
    my $outcome           = 1;

    $host_rows_created = 0;

    my $existing_host_constraint =
      $save_all_status ? '' : 'h.hostid not in (select distinct hoststatusid from logmessage where hoststatusid is not null) and';

    my $query = "
	insert into logmessage
	(
	    applicationtypeid,
	    deviceid,
	    hoststatusid,
	    servicestatusid,
	    textmessage,
	    msgcount,
	    firstinsertdate,
	    lastinsertdate,
	    reportdate,
	    monitorstatusid,
	    severityid,
	    applicationseverityid,
	    priorityid,
	    typeruleid,
	    componentid,
	    operationstatusid,
	    isstatechanged,
	    consolidationhash,
	    statelesshash,
	    statetransitionhash
	)
	select distinct on (h.hostid)
	    hs.applicationtypeid,		-- applicationtypeid
	    h.deviceid,				-- deviceid
	    hs.hoststatusid,			-- hoststatusid
	    null,				-- servicestatusid
	    'host status captured',		-- textmessage
	    1,					-- msgcount
	    coalesce(hs.lastchecktime,now()),	-- firstinsertdate
	    coalesce(hs.lastchecktime,now()),	-- lastinsertdate
	    now(),				-- reportdate
	    hs.monitorstatusid,			-- monitorstatusid
	    sev.severityid,			-- severityid
	    sev.severityid,			-- applicationseverityid
	    1,					-- priorityid
	    tr.typeruleid,			-- typeruleid
	    comp.componentid,			-- componentid
	    os.operationstatusid,		-- operationstatusid
	    false,				-- isstatechanged
	    0,					-- consolidationhash
	    0,					-- statelesshash
	    null				-- statetransitionhash
	from
	    host h,
	    hoststatus hs,
	    severity sev,
	    typerule tr,
	    component comp,
	    operationstatus os
	where
	    $existing_host_constraint
	    hs.hoststatusid = h.hostid
	and  sev.name = 'STATISTIC'
	and   tr.name = 'UNDEFINED'
	and comp.name = 'UNDEFINED'
	and   os.name = 'ACCEPTED'
    ";

    if ($roll_back_changes) {
	if ( not defined $dbh->do("begin transaction") ) {
	    log_timed_message "ERROR:  Cannot begin transaction.";
	    log_timed_message "        Database error is: ", $dbh->errstr;
	    $outcome = 0;
	}
    }
    if ($outcome) {
	my $sth = $dbh->prepare($query);
	if ( not $sth ) {
	    log_timed_message "ERROR:  Cannot prepare host query.";
	    log_timed_message "        Database error is: ", $dbh->errstr;
	    $outcome = 0;
	}
	else {
	    my $affected_rows = $sth->execute();
	    if ( not defined $affected_rows ) {
		log_timed_message "ERROR:  Cannot execute host query.";
		log_timed_message "        Database error is: ", $dbh->errstr;
		$outcome = 0;
	    }
	    else {
		$host_rows_created += $affected_rows;
	    }
	    $sth->finish;
	}
    }
    if ($roll_back_changes) {
	if ( not defined $dbh->do("rollback") ) {
	    log_timed_message "ERROR:  Cannot roll back transaction.";
	    log_timed_message "        Database error is: ", $dbh->errstr;
	    $outcome = 0;
	}
    }

    return $outcome;
}

sub create_service_status_markers {
    my $save_all_status   = shift;
    my $roll_back_changes = shift;
    my $outcome           = 1;

    $service_rows_created = 0;

    my $existing_service_constraint =
      $save_all_status
      ? ''
      : 'ss.servicestatusid not in (select distinct servicestatusid from logmessage where servicestatusid is not null) and';

    my $query = "
	insert into logmessage
	(
	    applicationtypeid,
	    deviceid,
	    hoststatusid,
	    servicestatusid,
	    textmessage,
	    msgcount,
	    firstinsertdate,
	    lastinsertdate,
	    reportdate,
	    monitorstatusid,
	    severityid,
	    applicationseverityid,
	    priorityid,
	    typeruleid,
	    componentid,
	    operationstatusid,
	    isstatechanged,
	    consolidationhash,
	    statelesshash,
	    statetransitionhash
	)
	select distinct on (ss.hostid, ss.servicestatusid)
	    ss.applicationtypeid,					-- applicationtypeid
	    h.deviceid,							-- deviceid
	    ss.hostid,							-- hoststatusid
	    ss.servicestatusid,						-- servicestatusid
	    'service status captured',					-- textmessage
	    1,								-- msgcount
	    coalesce(ss.laststatechange,ss.lastchecktime,now()),	-- firstinsertdate
	    coalesce(ss.lastchecktime,now()),				-- lastinsertdate
	    now(),							-- reportdate
	    ss.monitorstatusid,						-- monitorstatusid
	    sev.severityid,						-- severityid
	    sev.severityid,						-- applicationseverityid
	    1,								-- priorityid
	    tr.typeruleid,						-- typeruleid
	    comp.componentid,						-- componentid
	    os.operationstatusid,					-- operationstatusid
	    false,							-- isstatechanged
	    0,								-- consolidationhash
	    0,								-- statelesshash
	    null							-- statetransitionhash
	from
	    servicestatus ss,
	    host h,
	    severity sev,
	    typerule tr,
	    component comp,
	    operationstatus os
	where
	    $existing_service_constraint
	    h.hostid = ss.hostid
	and  sev.name = 'STATISTIC'
	and   tr.name = 'UNDEFINED'
	and comp.name = 'UNDEFINED'
	and   os.name = 'ACCEPTED'
    ";

    if ($roll_back_changes) {
	if ( not defined $dbh->do("begin transaction") ) {
	    log_timed_message "ERROR:  Cannot begin transaction.";
	    log_timed_message "        Database error is: ", $dbh->errstr;
	    $outcome = 0;
	}
    }
    if ($outcome) {
	my $sth = $dbh->prepare($query);
	if ( not $sth ) {
	    log_timed_message "ERROR:  Cannot prepare service query.";
	    log_timed_message "        Database error is: ", $dbh->errstr;
	    $outcome = 0;
	}
	else {
	    my $affected_rows = $sth->execute();
	    if ( not defined $affected_rows ) {
		log_timed_message "ERROR:  Cannot execute service query.";
		log_timed_message "        Database error is: ", $dbh->errstr;
		$outcome = 0;
	    }
	    else {
		$service_rows_created += $affected_rows;
	    }
	    $sth->finish;
	}
    }
    if ($roll_back_changes) {
	if ( not defined $dbh->do("rollback") ) {
	    log_timed_message "ERROR:  Cannot roll back transaction.";
	    log_timed_message "        Database error is: ", $dbh->errstr;
	    $outcome = 0;
	}
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

sub log_action_statistics {
    my $status_message = shift;
    my $suffix         = $status_message eq '' ? '' : " ($status_message)";
    my $process_status = $process_outcome ? 'SUCCEEDED' : 'FAILED';
    my ( $second, $minute, $hour, $month_day, $month, $year, $week_day, $year_day, $is_dst );

    # Sleep for one full second, to move past the $script_end_timestamp computed below by
    # rounding up a high-resolution time via POSIX::ceil().  This will guarantee that any
    # subsequent log_timed_message() output that appears after our "script ended at" message
    # will have a timestamp no earlier than the timestamp specified in that message.
    select undef, undef, undef, 1.0;

    ( $second, $minute, $hour, $month_day, $month, $year, $week_day, $year_day, $is_dst ) = localtime(POSIX::floor($script_start_time));
    my $script_start_timestamp = sprintf "%04d-%02d-%02d %02d:%02d:%02d", $year + 1900, $month + 1, $month_day, $hour, $minute, $second;
    ( $second, $minute, $hour, $month_day, $month, $year, $week_day, $year_day, $is_dst ) = localtime(POSIX::ceil($script_service_processing_end_time));
    my $script_end_timestamp = sprintf "%04d-%02d-%02d %02d:%02d:%02d", $year + 1900, $month + 1, $month_day, $hour, $minute, $second;

    $init_time         = $script_init_end_time - $script_start_time;
    $host_time         = $script_host_processing_end_time - $script_init_end_time;
    $service_time      = $script_service_processing_end_time - $script_host_processing_end_time;
    $total_time        = POSIX::ceil($script_service_processing_end_time - $script_start_time);

    $init_timestamp    = format_hhmmss_timestamp($init_time);
    $host_timestamp    = format_hhmmss_timestamp($host_time);
    $service_timestamp = format_hhmmss_timestamp($service_time);
    $total_timestamp   = format_hhmmss_timestamp($total_time);

    $total_rows_inserted = $host_rows_created + $service_rows_created;
    $insert_data_time    = $host_time + $service_time;

    # All speed measurements are "per second".
    $row_insertion_speed = $insert_data_time > 0 ? sprintf( "%12.3f", $total_rows_inserted / $insert_data_time ) : 'indeterminate';

    log_timed_message "STATS:  Status save statistics:";
    log_message "Status-save script started at:  $script_start_timestamp";
    log_message "Status-save script   ended at:  $script_end_timestamp";
    log_message         "$init_timestamp taken to initialize the status-save script";
    log_message         "$host_timestamp taken to run the host-status phase on the runtime database";
    log_message      "$service_timestamp taken to run the service-status phase on the runtime database";
    log_message        "$total_timestamp taken to run the entire status-save operation";

    log_message sprintf( "%8d rows of host    status were saved into the runtime database%s",
	$host_rows_created,    $roll_back_all_changes ? ' (BUT THEN ROLLED BACK)' : '' );
    log_message sprintf( "%8d rows of service status were saved into the runtime database%s",
	$service_rows_created, $roll_back_all_changes ? ' (BUT THEN ROLLED BACK)' : '' );

    log_message  "$row_insertion_speed rows inserted per second";

    log_timed_message "STATS:  This run of status saving $process_status$suffix.";

    # Reformat certain speed measurements for later use in a status message sent to Foundation.
    $row_insertion_speed = $insert_data_time > 0 ? sprintf( "%.1f", $total_rows_inserted / $insert_data_time ) : 'indeterminate';
}
