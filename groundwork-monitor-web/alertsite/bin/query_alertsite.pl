#!/usr/local/groundwork/perl/bin/perl -w --

# Daemon script to periodically probe AlertSite, and inject customer
# website status and performance data into GroundWork Monitor.

# Copyright (c) 2011 GroundWork Open Source, Inc.  All rights reserved.
# Use of this software is subject to commercial license terms.

# ================================================================
# Documentation.
# ================================================================

# Note:  This script uses SSL to contact AlertSite.  To that end, we need
# a copy of the IO::Socket::SSL package that includes full support for a
# non-blocking socket.  The version of this package in GWMEE6.1 (that is,
# version 0.97) was not up to the task.  GWMEE6.4 includes version 1.33
# of this package, which includes the necessary support.

# To do:
# FIX MINOR:
# * This script makes changes to the Monarch configuration under a shared
#   write lock on the COMMIT_LOCKFILE file.  That will prevent collisions
#   with synchronized Monarch commit operations.  But note that it won't
#   prevent all collisions with pre-flight operations, because of a subtle
#   race condition between the call to Locks->wait_for_file_to_disappear()
#   and the call to Locks->open_and_lock().  See comments in code below.
# FIX MINOR:
# * Consider re-examining the formal design of the commit-synchronization
#   protocol implemented here, and seeing if in addition to regularly
#   checking $shutdown_requested to see if we have been signaled, we
#   should also check to see if the COMMIT_IN_PROGRESS file exists, and
#   quiesce our actions (but not necessarily shut down completely) if so.
# FIX LATER:
# * This script is not subject to the wrong-current-configuration-database
#   issue noted in GWMON-9076, because its direct handling of Monarch data
#   represents the intent to modify the future configuration of Nagios,
#   not to depend on the current configuration.  However, when we resolve
#   that JIRA by renaming the future-configuration database, this script
#   will need to adapt accordingly.

# ================================================================
# Perl setup.
# ================================================================

use strict;

use Cwd 'realpath';
use Getopt::Std;

use Data::Dumper;
$Data::Dumper::Indent   = 1;
$Data::Dumper::Sortkeys = 1;

use lib '/usr/local/groundwork/core/monarch/lib';
use MonarchStorProc;
use dassmonarch;

# This is where we'll pick up any Perl packages not in the standard Perl
# distribution, to make this a self-contained add-on package.
use FindBin qw($Bin);
use lib "$Bin/../perl/lib";

use TypedConfig;

# GW:: packages are deemed to be either in-development and therefore potentially
# containing unstable APIs, or application-specific.  Once we stabilize their
# respective APIs and formally release them as part of the base GWMEE product,
# they will be changed to corresponding GroundWork:: packages.
use GW::Daemon;
use GW::Foundation;
use GW::Logger;
use GW::Nagios qw(
    nagios_plugin_numeric_host_status
    nagios_plugin_numeric_service_severity
);
use GW::AlertSite;

my $PROGNAME = "query_alertsite.pl";

# Be sure to update this as changes are made to this script!
my $VERSION = '0.0.2';

# ================================================================
# Command-line execution options and working variables.
# ================================================================

my $default_config_file = "$Bin/../config/query_alertsite.conf";
my $config_file         = undef;

my $debug_config = 0;
my $show_help    = 0;
my $show_version = 0;

my $probe_for_status  = 0;
my $probe_for_metrics = 0;

# $run_interactively is defaulted to true for proper END block processing
# during the initial phase of the process, before command-line argument
# parsing will override this default one way or the other.
my $run_interactively     = 1;
my $do_one_resource       = 0;
my $do_one_cycle          = 0;
my $reflect_log_to_stdout = 0;

# ================================================================
# Global configuration variables, to be read from the config file.
# ================================================================

my $enable_processing = undef;

# FIX LATER:  The $DEBUG_XXX variables are made public ("our" istead of "my")
# so that other packages (e.g., GW::Logger) can see their values.  We ought to
# have a better means to globally publicize the current debug level of the whole
# application, perhaps via some extension to the GW::Logger package itself.
my $debug_level   = undef;
our $DEBUG_NONE    = undef;
our $DEBUG_FATAL   = undef;
our $DEBUG_ERROR   = undef;
our $DEBUG_WARNING = undef;
our $DEBUG_NOTICE  = undef;
our $DEBUG_STATS   = undef;
our $DEBUG_INFO    = undef;
our $DEBUG_DEBUG   = undef;

my $status_logfile         = undef;
my $metrics_logfile        = undef;
my $logfile                = undef;
my $max_logfile_size       = undef;
my $max_logfiles_to_retain = undef;

my $status_cycle_time           = undef;
my $metrics_cycle_time          = undef;
my $cycle_time                  = undef;
my $minimum_wait_between_cycles = undef;
my $expected_metrics_latency    = undef;

my $alertsite_resource_host_profile     = undef;
my $alertsite_location_service_template = undef;

my $alertsite_monitoring_host = undef;
my $alertsite_status_service  = undef;
my $alertsite_metrics_service = undef;

my $alertsite_db_host = undef;
my $alertsite_db_name = undef;
my $alertsite_db_user = undef;
my $alertsite_db_pass = undef;

my $alertsite_server   = undef;
my $alertsite_username = undef;
my $alertsite_password = undef;

my $network_server_timeout = undef;

my $master_timezone = undef;

my %hostmap    = ();
my %servicemap = ();

my $generate_nagios_checks_from_metrics  = undef;
my $nagios_check_result_rollup_algorithm = undef;
my $max_nagios_checks_to_queue           = undef;

my $rrd_base_directory = undef;
my $rrd_graph_command  = undef;

# ----------------------------------------------------------------
# Options for sending messages to Foundation.
# ----------------------------------------------------------------

my $foundation_host = undef;
my $foundation_port = undef;

my $monitor_server_hostname   = undef;
my $monitor_server_ip_address = undef;

my $socket_send_timeout = undef;
my $send_buffer_size    = undef;

my $max_command_xml_bundle_size = undef;

# ----------------------------------------------------------------
# Options for sending messages to Nagios.
# ----------------------------------------------------------------

my $send_to_nagios              = undef;
my $use_nsca                    = undef;
my $nagios_command_pipe         = undef;
my $max_command_pipe_write_size = undef;
my $max_command_pipe_wait_time  = undef;

my $max_messages_per_send_nsca = undef;
my $delay_between_sends        = undef;
my $nsca_host                  = undef;
my $nsca_port                  = undef;
my $nsca_timeout               = undef;
my $send_to_secondary_NSCA     = undef;
my $secondary_nsca_host        = undef;
my $secondary_nsca_port        = undef;
my $secondary_nsca_timeout     = undef;

# ================================================================
# Configuration variables that perhaps ought to be migrated to
# the config file.
# ================================================================

# Seconds to sleep before dying, to prevent a daemon watchdog that immediately
# senses the death of this process from instantly restarting this application
# in a tight loop (in case the reason for dying is persistent).  We only invoke
# this delay if we are running in a context where there might actually be such
# a watchdog in play.  That does not include running as a daemon (in which
# case the initial process will fork and let a child process carry on), nor if
# this process is still connected to an interactive terminal session.  In such
# contexts, there should be no watchdog to worry about.
#
# To make this logic work correctly during the period before we have determined
# whether we are running interactively, the initial value for $run_interactively
# is set to assume we will do so.  That might impose an extra process-stop delay
# under some additional circumstances where it won't matter.
my $end_of_life_delay = 10;

# Rather than pepper the code with a call to sleep() everywhere we want
# to exit, we can cover all our bases in a reliable way with just this
# one END block.
END {
    if ( $run_interactively and not -t STDIN ) {
	## These seemingly contradictory conditions might indicate
	## that we are under control of some daemon watchdog.
	sleep $end_of_life_delay;
    }
}

# ================================================================
# Global working variables.
# ================================================================

my $logtime = '';

use constant ERROR_STATUS    => 0;
use constant STOP_STATUS     => 1;
use constant RESTART_STATUS  => 2;
use constant CONTINUE_STATUS => 3;

# By declaring this flag as "our", it can be accessed by various library
# functions as $main::shutdown_requested, and thus the application and the
# libraries can share a common mechanism for detecting shutdown requests.
our $shutdown_requested = 0;

my $alertsite  = undef;
my $foundation = undef;

my $host_id_by_name        = undef;
my $servicename_id_by_name = undef;
my %hosts_services         = ();
my $host_at_address        = undef;

my $last_unique_address = undef;

my %sent_graph_command = ();

# ================================================================
# Program.
# ================================================================

exit ((main() == ERROR_STATUS) ? 1 : 0);

# ================================================================
# Supporting subroutines.
# ================================================================

sub main {
    ## Change our working directory to a safe place that we know the nagios user should be able to
    ## access, because the realpath() call inside parse_command_line() performs internal chdir()
    ## operations and eventually tries to get back to where it started.  If that starting place is
    ## in fact inaccessible to the nagios user, the realpath() call will fail.  This chdir() can be
    ## considered to be one simplistic part of what make_daemon() does, and it will be overridden
    ## by make_daemon() if we are not running interactively.  The directory we choose here is the
    ## same one that make_daemon() uses, namely "/", for the same reason, namely to prevent any
    ## "filesystem busy" problems during unmounts.
    if (not chdir '/') {
	spill_message "FATAL:  $PROGNAME cannot change directory to \"/\".";
	return ERROR_STATUS;
    }

    my $command_line_status = parse_command_line();
    if (!$command_line_status) {
	spill_message "FATAL:  $PROGNAME either cannot understand its command-line parameters or cannot find its config file";
	return ERROR_STATUS;
    }

    if ($show_version) {
	print_version();
    }

    if ($show_help) {
	print_usage();
    }

    if ($show_version || $show_help) {
	return STOP_STATUS;
    }

    # Daemonize, if we don't have a command-line argument saying not to.
    if (!$run_interactively) {
	make_daemon();
    }

    # Read the configuration file.
    if (!read_config_file()) {
	return ERROR_STATUS;
    }

    # Stop if this is just a debugging run.
    return STOP_STATUS if $debug_config;

    GW::Logger->new( $logfile, $run_interactively, $reflect_log_to_stdout, $max_logfile_size, $max_logfiles_to_retain );

    if (!open_logfile()) {
	## The routine will print an error message if it fails, so we don't do so ourselves.
	return ERROR_STATUS;
    }

    log_timed_message "=== Starting up (process $$). ===";

    if (!$enable_processing) {
	log_timed_message "FATAL:  Stopping execution (process $$) because processing is not enabled in the config file.";
	close_logfile();
	return STOP_STATUS;
    }

    # Set up to handle broken pipe errors.  This has to be done in conjunction with later code that
    # will cleanly process an EPIPE return code from a socket write.
    #
    # Our trivial signal handler turns SIGPIPE signals generated when we write to sockets already
    # closed by the server into EPIPE errors returned from the write operations.  The same would
    # happen if instead we just ignored these signals, but with this mechanism we also automatically
    # impose a short delay (inside the signal handler) when this situation occurs -- there is little
    # reason to keep pounding the server when it has already indicated it cannot accept data just now.
    #
    # FIX MINOR:  Check our later code to see how we would handle an EPIPE return code from a socket
    # write, before we enable this.
    # $SIG{"PIPE"} = \&sig_pipe_handler;

    my $daemon_status = synchronized_daemon();

    close_logfile();

    return $shutdown_requested ? STOP_STATUS : $daemon_status;
}

sub synchronized_daemon {
    my $commit_lock;
    my $errors;

    # We catch SIGTERM, SIGINT, and SIGQUIT so we can stop when Nagios stops, or when we are asked nicely.
    local $SIG{INT}  = \&handle_exit_signal;
    local $SIG{QUIT} = \&handle_exit_signal;
    local $SIG{TERM} = \&handle_exit_signal;

    use MonarchLocks;

    if ( !Locks->wait_for_file_to_disappear( $Locks::in_progress_file, \&log_timed_message, \$shutdown_requested ) ) {
	log_shutdown();
	return STOP_STATUS;
    }

    # FIX MINOR:  There is a race condition here between grabbing a shared lock on $Locks::commit_lock_file
    # and having the $Locks::in_progress_file file not be present, as we just checked above.  In fact, if we
    # ever need to sleep and loop here, the window for that race condition opens considerably wider.  If the
    # file appears before we grab the lock, a pre-flight operation can be executed while we are performing
    # initialization operations in this script.
    while (1) {
	$errors = Locks->open_and_lock( \*commit_lock, $Locks::commit_lock_file, $Locks::SHARED, $Locks::NON_BLOCKING );
	last if !@$errors;
	for (@$errors) {
	    log_message($_);
	}
	sleep 30;
	if ($shutdown_requested) {
	    log_shutdown();
	    return STOP_STATUS;
	}
    }

    my ( $dev, $ino, $mode, $nlink, $uid, $gid, $rdev, $size, $atime, $mtime, @rest ) = stat( \*commit_lock );
    my $initial_mtime = $mtime;

    my $init_status = initialize_feeder();

    Locks->close_and_unlock( \*commit_lock );

    if ($init_status != CONTINUE_STATUS) {
	log_timed_message("=== Initialization failed; will exit (process $$). ===");
	return $init_status;
    }

    while (1) {
	if ($shutdown_requested) {
	    flush_pending_output();
	    log_shutdown();
	    finalize_feeder();
	    return STOP_STATUS;
	}

	if ( !Locks->wait_for_file_to_disappear( $Locks::in_progress_file, \&log_timed_message, \$shutdown_requested ) ) {
	    flush_pending_output();
	    log_shutdown();
	    finalize_feeder();
	    return STOP_STATUS;
	}

	# FIX MINOR:  There is a race condition here between grabbing a shared lock on $Locks::commit_lock_file
	# and having the $Locks::in_progress_file file not be present, as we just checked above.  In fact, if we
	# ever need to sleep and loop here, the window for that race condition opens considerably wider.  If the
	# file appears before we grab the lock, a pre-flight operation can be executed while we are performing
	# cycle operations in this script.
	while (1) {
	    $errors = Locks->open_and_lock( \*commit_lock, $Locks::commit_lock_file, $Locks::SHARED, $Locks::NON_BLOCKING );
	    last if !@$errors;
	    for (@$errors) {
		log_message($_);
	    }
	    sleep 30;
	    if ($shutdown_requested) {
		flush_pending_output();
		log_shutdown();
		finalize_feeder();
		return STOP_STATUS;
	    }
	}

	( $dev, $ino, $mode, $nlink, $uid, $gid, $rdev, $size, $atime, $mtime, @rest ) = stat( \*commit_lock );
	if ( $mtime != $initial_mtime ) {
	    Locks->close_and_unlock( \*commit_lock );
	    flush_pending_output();
	    log_timed_message("=== A commit has occurred; will exit to start over and re-initialize (process $$). ===");
	    finalize_feeder();
	    return RESTART_STATUS;
	}

	my $cycle_start_time = time();
	my $cycle_status = perform_feeder_cycle_actions();

	Locks->close_and_unlock( \*commit_lock );

	if ($cycle_status != CONTINUE_STATUS) {
	    flush_pending_output();
	    log_timed_message("=== Cycle status is not to continue; will exit (process $$). ===");
	    finalize_feeder();
	    return $cycle_status;
	}

	if ($shutdown_requested) {
	    flush_pending_output();
	    log_shutdown();
	    finalize_feeder();
	    return STOP_STATUS;
	}

	log_timed_message("--- taking a siesta ---") if $DEBUG_NOTICE;

	# We try to run cycles so the next cycle starts at a fixed time period relative to
	# the start of the last cycle, instead of relative to the end of the last cycle.
	# If that isn't possible, we impose a minimum down time until the next cycle.
	my $cycle_run_time = time() - $cycle_start_time;
	if ($cycle_run_time + $minimum_wait_between_cycles < $cycle_time) {
	    my $wait_time = int($cycle_time - $cycle_run_time);
	    sleep $wait_time;
	}
	else {
	    sleep $minimum_wait_between_cycles;
	}

	# Rotate our own log file when it grows too large, so we don't need to depend
	# on any external agent to do so and to synchronize with our own operations.
	if (!rotate_logfile()) {
	    log_timed_message("=== Problem with rotating the logfile; will exit (process $$). ===");
	    finalize_feeder();
	    return ERROR_STATUS;
	}
    }
}

sub flush_pending_output {
    ## This daemon has no pending output kept across processing cycles.

    ## This application does not queue any output that might need to be flushed
    ## if we are about to go down unexpectedly.  However, we leave this function
    ## around, along with the calls to it elsewhere in the code, as a placeholder
    ## to serve as a reminder that such a capability might be needed, if this code
    ## is ever used as an example skeleton for some similar daemon.
}

sub print_usage {
    print "usage:  $PROGNAME [-h] [-v] [-c config_file] [-d] {-a|-m} [-i] [-r|-s] [-o]\n";
    print "where:  -h:  print this help message\n";
    print "        -v:  print the version number\n";
    print "        -c config_file:  specify an alternate config file\n";
    print "             (default is $default_config_file)\n";
    print "        -d:  dump the config file entries (to debug them)\n";
    print "        -a:  probe for alarm (status) data instead of metric data\n";
    print "        -m:  probe for metric data instead of alarm (status) data\n";
    print "        -i:  run interactively, not as a persistent daemon\n";
    print "        -r:  process just a single valid resource, then stop\n";
    print "        -s:  run just a single cycle, then stop\n";
    print "        -o:  write log messages also to standard output\n";
    print "The operating mode (either -a or -m) must be specified.\n";
    print "The -o option is illegal unless -i is also specified.\n";
}

sub print_version {
    print "$PROGNAME Version:  $VERSION\n";
    print "Copyright 2011 GroundWork Open Source, Inc. (\"GroundWork\").\n";
    print "All rights reserved.\n";
}

sub parse_command_line {
    # First, clean up the $default_config_file value in case we print usage.
    my $real_path = realpath ($default_config_file);
    $default_config_file = $real_path if $real_path;

    my %opts;
    if (not getopts('hvc:damirso', \%opts)) {
	print_usage();
	return 0;
    }

    $show_help             = $opts{h};
    $show_version          = $opts{v};
    $config_file           = ( defined $opts{c} && $opts{c} ne '' ) ? $opts{c} : $default_config_file;
    $debug_config          = $opts{d};
    $probe_for_status      = $opts{a};   # status data == alarm data
    $probe_for_metrics     = $opts{m};
    $run_interactively     = $opts{i};
    $do_one_resource       = $opts{r};
    $do_one_cycle          = $opts{s};
    $reflect_log_to_stdout = $opts{o};

    # Adjust the config file specification to be an absolute pathname,
    # partly so we could be a bit more cavalier when we specified it, and
    # partly because a relative pathname would be misleading considering
    # that our working directory will generally be different from where
    # we started, even if we don't run this script as a daemon.
    $config_file = "$Bin/../config/$config_file" if $config_file !~ m{^/};
    $real_path = realpath($config_file);
    if (!$real_path) {
	spill_message "FATAL:  The path to the $PROGNAME config file $config_file either does not exist or is inaccessible to this script running as ", (scalar getpwuid $>), '.';
	return 0;
    }
    $config_file = $real_path;

    if ($probe_for_status && $probe_for_metrics) {
	print "FATAL:  The -a and -m options are mutually exclusive.\n";
	print_usage();
	return 0;
    }

    if (!$probe_for_status && !$probe_for_metrics) {
	print "FATAL:  Either -a or -m must be specified.\n";
	print_usage();
	return 0;
    }

    if ($do_one_resource && $do_one_cycle) {
	print "FATAL:  The -r and -s options are mutually exclusive.\n";
	print_usage();
	return 0;
    }

    if (!$run_interactively && $reflect_log_to_stdout) {
	print_usage();
	return 0;
    }

    return 1;
}

sub read_config_file {
    eval {
	my $config = TypedConfig->secure_new( $config_file, $debug_config );

	# Whether to process anything.  Turn this off if you want to disable
	# this process completely in case it gets run some time when you're
	# not expecting it to.
	$enable_processing = $config->get_boolean('enable_processing');

	# Global Debug Level Flag.
	$debug_level = $config->get_number('debug_level');

	# Variables to be used as quick tests to see if we're interested in
	# particular debug messages.
	$DEBUG_NONE    = $debug_level == 0;    # turn off all debug info
	$DEBUG_FATAL   = $debug_level >= 1;    # the application is about to die
	$DEBUG_ERROR   = $debug_level >= 2;    # the application has found a serious problem, but will attempt to recover
	$DEBUG_WARNING = $debug_level >= 3;    # the application has found an anomaly, but will try to handle it
	$DEBUG_NOTICE  = $debug_level >= 4;    # the application wants to inform you of a significant event
	$DEBUG_STATS   = $debug_level >= 5;    # the application wants to log statistical data for later analysis
	$DEBUG_INFO    = $debug_level >= 6;    # the application wants to log a potentially interesting event
	$DEBUG_DEBUG   = $debug_level >= 7;    # the application wants to log detailed debugging data

	# Where to log ordinary operational messages for status data processing,
	# especially for debugging.  A relative pathname specified here will be
	# interpreted relative to the directory in which the query_alertsite.pl
	# script lives.
	$status_logfile = $config->get_scalar('status_logfile');

	# Where to log ordinary operational messages for metrics data processing,
	# especially for debugging.  A relative pathname specified here will be
	# interpreted relative to the directory in which the query_alertsite.pl
	# script lives.
	$metrics_logfile = $config->get_scalar('metrics_logfile');

	# Where to log ordinary operational messages.
	$logfile = $probe_for_status ? $status_logfile : $metrics_logfile;

	# We need to absolutize a relative path to the $logfile right away
	# before we attempt to use $logfile to open the file, so we do that
	# work here instead of within initialize_feeder().
	$logfile = "$Bin/$logfile" if $logfile !~ m{^/};

	# How large (in MBytes) the logfile is allowed to get before it is
	# automatically rotated at the end of a processing cycle.
	$max_logfile_size = $config->get_number('max_logfile_size');
	$max_logfile_size *= 1024 * 1024;    # convert from MB to bytes

	# How many total logfiles will be retained when the logfile is rotated.
	$max_logfiles_to_retain = $config->get_number('max_logfiles_to_retain');

	# How often to probe the AlertSite server for new status data, in seconds.
	$status_cycle_time = $config->get_number('status_cycle_time');

	# How often to probe the AlertSite server for new metrics data, in seconds.
	$metrics_cycle_time = $config->get_number('metrics_cycle_time');

	# How often this invocation should probe the AlertSite server for new data,
	# in seconds.
	$cycle_time = $probe_for_status ? $status_cycle_time : $metrics_cycle_time;

	# A minimum period to wait between successive cycles, in seconds.
	$minimum_wait_between_cycles = $config->get_number('minimum_wait_between_cycles');

	if ($minimum_wait_between_cycles <= 1) {
	    die 'ERROR:  minimum_wait_between_cycles must be greater than 1';
	}

	# How long we allow AlertSite to complete metrics gathering, in seconds.
	$expected_metrics_latency = $config->get_number('expected_metrics_latency');

	if ($expected_metrics_latency <= 1) {
	    die 'ERROR:  expected_metrics_latency must be greater than 1';
	}

	# The host profile which will be applied to all customer resources being
	# monitored by AlertSite, when such resources are created as hosts within
	# Monarch.
	$alertsite_resource_host_profile = $config->get_scalar('alertsite_resource_host_profile');

	# The service template which will be applied to all locations from which
	# AlertSite monitors customer resources, when such locations are created
	# as services within Monarch.
	$alertsite_location_service_template = $config->get_scalar('alertsite_location_service_template');

	# The hostname and servicenames for this daemon script, so trouble in fetching
	# and processing data from AlertSite can be reported to GroundWork Monitor.
	$alertsite_monitoring_host = $config->get_scalar('alertsite_monitoring_host');
	$alertsite_status_service  = $config->get_scalar('alertsite_status_service');
	$alertsite_metrics_service = $config->get_scalar('alertsite_metrics_service');

	# "alertsite" database access credentials.
	# FIX LATER:  Perhaps we should grab these from db.properties instead.
	$alertsite_db_host = $config->get_scalar('alertsite_db_host');
	$alertsite_db_name = $config->get_scalar('alertsite_db_name');
	$alertsite_db_user = $config->get_scalar('alertsite_db_user');
	$alertsite_db_pass = $config->get_scalar('alertsite_db_pass');

	# AlertSite server web-access credentials.
	$alertsite_server   = $config->get_scalar('alertsite_server');
	$alertsite_username = $config->get_scalar('alertsite_username');
	$alertsite_password = $config->get_scalar('alertsite_password');

	# Max time (seconds) to wait for network server activity.
	$network_server_timeout = $config->get_number('network_server_timeout');

	# The timezone in which all timestamps from AlertSite are expressed.
	# We need to convert from the AlertSite timezone to either GMT or the
	# timezone of the GroundWork server in various circumstances, and also
	# convert in the opposite direction when sending timestamps to AlertSite.
	$master_timezone = $config->get_scalar('master_timezone');

	# ---------------------------------------------------------------- #

	# Non-default customer-resource to host-name mapping.
	my %host_map_hash = $config->get_hash ('host_map');

	print Data::Dumper->Dump([\%host_map_hash], [qw(\%host_map_hash)]) if $debug_config;

	my %host_names = %{$host_map_hash{'resource'}};

	print Data::Dumper->Dump([\%host_names], [qw(\%host_names)]) if $debug_config;

	foreach my $resource (keys %host_names) {
	    print "host_names.host = $resource\n" if $debug_config;
	    my $resource_ref = $host_names{$resource};
	    if (ref $resource_ref eq 'HASH') {
		my $resource_host = $host_names{$resource}{'host'};
		if (defined $resource_host) {
		    print "host for $resource is $resource_host\n" if $debug_config;
		    $hostmap{$resource} = $resource_host;
		}
	    }
	}

	print Data::Dumper->Dump([\%hostmap], [qw(\%hostmap)]) if $debug_config;

	# ---------------------------------------------------------------- #

	# Non-default AlertSite-location to service-name mapping.
	my %service_map_hash = $config->get_hash ('service_map');

	print Data::Dumper->Dump([\%service_map_hash], [qw(\%service_map_hash)]) if $debug_config;

	my %service_names = %{$service_map_hash{'location'}};

	print Data::Dumper->Dump([\%service_names], [qw(\%service_names)]) if $debug_config;

	foreach my $location (keys %service_names) {
	    print "service_names.service = $location\n" if $debug_config;
	    my $location_ref = $service_names{$location};
	    if (ref $location_ref eq 'HASH') {
		my $location_service = $service_names{$location}{'service'};
		if (defined $location_service) {
		    print "service for $location is $location_service\n" if $debug_config;
		    $servicemap{$location} = $location_service;
		}
	    }
	}

	print Data::Dumper->Dump([\%servicemap], [qw(\%servicemap)]) if $debug_config;

	# ---------------------------------------------------------------- #

	# Whether to send Nagios host and service checks based on AlertSite device metric data (true)
	# or based on AlertSite device status data (false).
	$generate_nagios_checks_from_metrics = $config->get_boolean('generate_nagios_checks_from_metrics');

	# How to combine Nagios service-check results into Nagios host-check results.
	$nagios_check_result_rollup_algorithm = $config->get_scalar('nagios_check_result_rollup_algorithm');

	if ($nagios_check_result_rollup_algorithm ne 'worst-case' and
	    $nagios_check_result_rollup_algorithm ne 'most-recent' and
	    $nagios_check_result_rollup_algorithm ne 'none') {
	    die 'ERROR:  nagios_check_result_rollup_algorithm must be either "worst-case", "most-recent", or "none"';
	}

	# The maximum number of Nagios host and service check messages to queue before
	# sending them to Nagios.
	$max_nagios_checks_to_queue = $config->get_number('max_nagios_checks_to_queue');

	# The directory in which RRD files created and updated by this script shall live.
	$rrd_base_directory = $config->get_scalar('rrd_base_directory');

	# The command used to create RRD graphs for this application's collected metrics.
	$rrd_graph_command = $config->get_scalar('rrd_graph_command');

	# We normalize the graph command here to make it easier to specify in the config file,
	# while meeting the restrictions needed for processing by Foundation (no newlines,
	# which will appear in our config-file version when we use a HERE document to contain
	# the command, or tabs, which would probably have appeared if we didn't use a HERE
	# document and instead used tabs before backslashes at the ends of the lines).
	$rrd_graph_command =~ s/\n\s*/ /g;
	$rrd_graph_command =~ s/\t+\s*/ /g;

	# ----------------------------------------------------------------
	# Options for sending messages to Foundation.
	# ----------------------------------------------------------------

	# Where to contact Foundation.
	$foundation_host = $config->get_scalar('foundation_host');
	$foundation_port = $config->get_number('foundation_port');

	# The name of the monitoring server on which this AlertSite Integration is running.
	$monitor_server_hostname = $config->get_scalar('monitor_server_hostname');

	# The IP address of the monitoring server on which this AlertSite Integration is running.
	$monitor_server_ip_address = $config->get_scalar('monitor_server_ip_address');

	# FIX LATER:  We ought to further validate $monitor_server_ip_address, either by implementing
	# $config->get_ip_address() in TypedConfig, or by IPv4/IPv6 address pattern matching here.

	# Socket timeout (in seconds), to address GWMON-7407.  Typical value is 60.  Set to 0 to disable.
	$socket_send_timeout = $config->get_number('socket_send_timeout');

	# This is the actual SO_SNDBUF value, as set by setsockopt().
	#
	# This value is not currently used by the GW::Foundation package, but might be
	# in the future for use in other applications.  So we support it here now.
	$send_buffer_size = $config->get_number('send_buffer_size');

	# A limit on the number of command XML items sent to Foundation in a single packet.
	$max_command_xml_bundle_size = $config->get_number('max_command_xml_bundle_size');

	# ----------------------------------------------------------------
	# Options for sending messages to Nagios.
	# ----------------------------------------------------------------

	# Send the passive service check results to Nagios?
	$send_to_nagios = $config->get_boolean('send_to_nagios');

	# Use NSCA to send results to a (generally remote) Nagios command pipe?
	$use_nsca = $config->get_boolean('use_nsca');

	# Absolute pathname of the Nagios command pipe.
	$nagios_command_pipe = $config->get_scalar('nagios_command_pipe');

	# The maximum size in bytes for any single write operation to the Nagios
	# command pipe.
	$max_command_pipe_write_size = $config->get_number('max_command_pipe_write_size');

	# The maximum time in seconds to wait for any single write to the Nagios
	# command pipe to complete.
	$max_command_pipe_wait_time = $config->get_number('max_command_pipe_wait_time');

	#
	#   NSCA Options
	#

	# The maximum number of messages that will be passed to one call of send_nsca.
	$max_messages_per_send_nsca = $config->get_number('max_messages_per_send_nsca');

	# The number of seconds to delay between successive calls to send_nsca.
	$delay_between_sends = $config->get_number('delay_between_sends');

	# Host of target (generally remote) NSCA.
	$nsca_host = $config->get_scalar('nsca_host');

	# NSCA port to send_nsca results to (usually port 5667).
	$nsca_port = $config->get_number('nsca_port');

	# The number of seconds before send_nsca times out.
	$nsca_timeout = $config->get_number('nsca_timeout');

	# Whether to also send a copy of the Cacti threshold data to a secondary server.
	$send_to_secondary_NSCA = $config->get_boolean('send_to_secondary_NSCA');

	# Host of secondary target NSCA.
	$secondary_nsca_host = $config->get_scalar('secondary_nsca_host');

	# Secondary-host NSCA port to send_nsca results to (usually port 5667).
	$secondary_nsca_port = $config->get_number('secondary_nsca_port');

	# The number of seconds before secondary-host send_nsca times out.
	$secondary_nsca_timeout = $config->get_number('secondary_nsca_timeout');
    };
    if ($@) {
	chomp $@;
	# We cannot log error messages here, because we haven't opened the log file yet.
	print $@, "\n";
	print "FATAL:  Cannot process config file \"$config_file\".\n";
	return 0;
    }

    return 1;
}

sub initialize_feeder {
    my $debug = $DEBUG_DEBUG;

    eval {
	$alertsite = GW::AlertSite->new(
	    $alertsite_db_host,        $alertsite_db_name, $alertsite_db_user,                    $alertsite_db_pass,
	    $expected_metrics_latency, $alertsite_server,  $network_server_timeout,               $master_timezone,
	    \%hostmap,                 \%servicemap,       $nagios_check_result_rollup_algorithm, $rrd_base_directory,
	    $debug
	);
    };
    ## We don't issue a log message at this level regarding a failure to create the
    ## GW::AlertSite object, because the package will have already logged details.
    return ERROR_STATUS if $@;

    if ( not $alertsite->login( $alertsite_username, $alertsite_password ) ) {
	## We don't issue a log message at this level regarding a failure to log in,
	## because the login() routine should have already provided adequate detail.
	$alertsite = undef;
	return ERROR_STATUS;
    }

    eval {
	$foundation = GW::Foundation->new (
	    $foundation_host,         $foundation_port,
	    $monitor_server_hostname, $monitor_server_ip_address,
	    $socket_send_timeout,     $send_buffer_size,
	    $debug
	);
    };
    if ($@) {
	chomp $@;
	log_timed_message $@;
    }
    if (not defined $foundation) {
	finalize_feeder();
	return RESTART_STATUS;
    }

    return CONTINUE_STATUS;
}

sub finalize_feeder {
    ## Let's be kind to AlertSite, and kill our session on their site.
    if (defined $alertsite) {
	$alertsite->logout();
	$alertsite = undef;
    }
}

sub perform_feeder_cycle_actions {
    log_timed_message "--- running a cycle ---" if $DEBUG_NOTICE;

    my @nagios_messages    = ();
    my $got_valid_resource = 0;
    my $fatal_condition    = 0;

    my $got_monarch_data = undef;
    if ($probe_for_status) {
	## We construct host, service, and host+service lists here, so we can compare them
	## against the virtual host/service pairs we derive from the AlertSite status data.  We
	## do this on every status-monitoring cycle (that is, every cycle in the mode that we
	## allow to modify Monarch) because our list from a previous cycle might be stale, due
	## to possible changes made through other channels.  If we find some host+service now
	## being monitored by AlertSite but not in Monarch, we want to automatically add that
	## host (if not already present), service (if not already present), and host+service
	## into Monarch, with appropriate host and service profiles/templates applied.
	$got_monarch_data = read_monarch_database();
    }

    my $start_time = time();
    my $device_list = find_device_list();
    my $run_time = time() - $start_time;
    log_timed_message "STATS:  find_device_list() took $run_time seconds" if $DEBUG_STATS;
    $fatal_condition = 1 if not defined $device_list;

    my $device_statuses = undef;
    if (not $fatal_condition) {
	$start_time = time();
	$device_statuses = find_device_statuses();
	$run_time = time() - $start_time;
	log_timed_message "STATS:  find_device_statuses() took $run_time seconds" if $DEBUG_STATS;
	$fatal_condition = 1 if not defined $device_statuses;
    }

    if (not $fatal_condition) {
	## Compare the device status info we just retrieved against the Monarch setup.  Add hosts,
	## services, and host services as needed to the "monarch" database, for any new host+service
	## combinations we find that are now being actively monitored by AlertSite.
	##
	## FIX LATER:  Note that, due to race conditions in the current design and implementation
	## of our commit-synchronization protocol, we might be adding objects to Monarch here while
	## a pre-flight is in progress.  This could result in pre-flight errors because we could
	## be changing the database while Monarch is trying to write a clean and consistent set of
	## Nagios files, and there can be race conditions between writing and reading the database
	## that could result in unresolved object references on the reading side.
	update_monarch_database($device_statuses) if $got_monarch_data;

	if (not $generate_nagios_checks_from_metrics) {
	    ## Under this configuration option, we generate Nagios service check messages
	    ## right away, before the device status becomes stale, because AlertSite doesn't
	    ## make available an efficient mechanism for pulling back all the metric data
	    ## for all devices in one shot.  Compared to metric-derived service check
	    ## messages, the status-derived service check messages currently have a more
	    ## descriptive human-readable state message, but a less-nuanced calculation of
	    ## the state itself.
	    my $host_status_UP = nagios_plugin_numeric_host_status('UP');
	    foreach my $obj_device ( keys %$device_statuses ) {
		my $device_status = $device_statuses->{$obj_device};
		## Construct Nagios service checks from the available device status data.
		my $CustomerResource         = $device_status->{CustomerResource};
		my $rollup_host_status       = $host_status_UP;
		my $RollupLastStatus         = 'No problem seen.';
		my $RollupLastStatusUTCEpoch = 0;
		foreach my $obj_location ( keys %{ $device_status->{Probes} } ) {
		    my $status_location    = $device_status->{Probes}{$obj_location};
		    my $Location           = $status_location->{Location};
		    my $LastStatusCode     = $status_location->{LastStatusCode};
		    my $LastStatus         = $status_location->{LastStatus};
		    my $LastStatusUTCEpoch = $status_location->{LastStatusUTCEpoch};
		    ## If there is no $LastStatusUTCEpoch, then no actual monitoring has occurred,
		    ## so we just skip this host/service combination.
		    if ( defined $LastStatusUTCEpoch ) {
			my ($host_status, $service_severity) = $alertsite->nagios_interpretation_of_alertsite_statuses( $LastStatusCode, undef, undef );
			## If $LastStatusUTCEpoch is stale, we still report this last-current status to Nagios,
			## and let it figure out that the service status ought now to be UNKNOWN using its own
			## logic, even though we may well have reported this exact same timestamp/result before.
			push @nagios_messages,
			  $alertsite->service_check_result( $LastStatusUTCEpoch, $CustomerResource, $Location, $service_severity, $LastStatus );

			# Here we process just the most-recent result from each location that is still monitoring the resource.
			# The timestamps of those results might vary quite a bit, though.
			#
			# A question arises as to how to transform the service state(s) into the host state.  In one sense, we
			# want to take the worst-case service state and reflect that as the host state.  But the reasoning behind
			# that logic generally assumes that all the services represent a sampling at the same time, which is often
			# not the case with AlertSite monitoring.  If a later service probe senses the host is okay now, shouldn't
			# that override an earlier bad state?  The answer is generally no, because that will mask trouble from some
			# monitoring locations.
			#
			# It is possible that a good result from one location might be more recent than a bad result from another
			# location, and in some sense might therefore be more reflective of the true current status of the resource.
			# However, we usually want to make sure that trouble accessing the resource from any location is reflected
			# in the reported resource status.  So the recommended rollup algorithm is "worst-case".

			if ($nagios_check_result_rollup_algorithm eq 'worst-case') {
			    ## We ignore all location probe timestamps, and just take the worst of the most-recent probes from all the active locations.
			    ## We only use the most-recent result when we find results of the worst-case probes are tied.
			    if ($rollup_host_status < $host_status) {
				$rollup_host_status = $host_status;
				## Capture the timestamp and message, too.
				$RollupLastStatusUTCEpoch = $LastStatusUTCEpoch;
				$RollupLastStatus         = $LastStatus;
			    }
			    elsif ($rollup_host_status == $host_status and $RollupLastStatusUTCEpoch < $LastStatusUTCEpoch) {
				$RollupLastStatusUTCEpoch = $LastStatusUTCEpoch;
				$RollupLastStatus         = $LastStatus;
			    }
			}
			elsif ($nagios_check_result_rollup_algorithm eq 'most-recent') {
			    ## Instead of looking for the worst-case result, we look for the most-recent result, and ignore any possibly worse results.
			    ## Using this rollup algorithm is not really recommended, as it can mask trouble accessing the resource from some locations.
			    ## We only use the worst-case result when we find timestamps of the most-recent probes are tied.
			    if ($RollupLastStatusUTCEpoch < $LastStatusUTCEpoch) {
				$RollupLastStatusUTCEpoch = $LastStatusUTCEpoch;
				$RollupLastStatus         = $LastStatus;
				$rollup_host_status       = $host_status;
			    }
			    elsif ($RollupLastStatusUTCEpoch == $LastStatusUTCEpoch and $rollup_host_status < $host_status) {
				$rollup_host_status = $host_status;
				$RollupLastStatus   = $LastStatus;
			    }
			}
		    }
		}
		if ($RollupLastStatusUTCEpoch) {
		    push @nagios_messages,
		      $alertsite->host_check_result( $RollupLastStatusUTCEpoch, $CustomerResource, $rollup_host_status, $RollupLastStatus );
		}
	    }
	    send_nagios_messages(\@nagios_messages);
	    @nagios_messages = ();
	}

	if ($probe_for_status) {
	    ## Update RRD graph definitions in Foundation.
	    my @service_xml          = ();
	    my %queued_graph_command = ();
	    foreach my $obj_device ( keys %$device_statuses ) {
		if ($shutdown_requested) {
		    $fatal_condition = 1;
		    last;
		}

		my $device_status    = $device_statuses->{$obj_device};
		my $CustomerResource = $device_status->{CustomerResource};
		foreach my $obj_location ( keys %{ $device_status->{Probes} } ) {
		    my $status_location = $device_status->{Probes}{$obj_location};
		    my $Location        = $status_location->{Location};
		    my $rrd_file        = "${CustomerResource}_${Location}.rrd";
		    ## We only deal with this graph command if we have not already sent a copy to Foundation since
		    ## this script was started.  If the graph command definition in the configuration file is ever
		    ## updated, you need only bounce this script to cause a full set of updated graph commands to
		    ## be sent to Foundation.  This design provides simple and efficient robustness against errors.
		    if ( not $sent_graph_command{$rrd_file} ) {
			my $config_label  = "AlertSite monitoring metrics";
			my $rrd_path      = "$rrd_base_directory/$rrd_file";
			my $graph_command = $rrd_graph_command;
			$graph_command =~ s/\$HOST\$/$CustomerResource/g;
			$graph_command =~ s/\$SERVICE\$/$Location/g;
			$graph_command =~ s/rrd_source/$rrd_path/g;
			push @service_xml, $foundation->local_service_xml( $CustomerResource, $Location, $config_label, $rrd_path, $graph_command );
			$queued_graph_command{$rrd_file} = 1;
			if ( @service_xml >= $max_command_xml_bundle_size ) {
			    my $service_xml = join( '', @service_xml );
			    if ( $foundation->write_command_xml( 'MODIFY', APP_NAGIOS, $service_xml ) ) {
				my @rrd_files = keys %queued_graph_command;
				@sent_graph_command{@rrd_files} = (1) x @rrd_files;
			    }
			    elsif ($DEBUG_ERROR) {
				log_timed_message
				  "ERROR:  Cannot write RRD graph data to Foundation on host $foundation_host; will skip for this cycle.";
			    }
			    @service_xml          = ();
			    %queued_graph_command = ();
			}
		    }
		}
	    }
	    if ($shutdown_requested) {
		$fatal_condition = 1;
	    }
	    elsif (@service_xml) {
		my $service_xml = join( '', @service_xml );
		if ( $foundation->write_command_xml( 'MODIFY', APP_NAGIOS, $service_xml ) ) {
		    my @rrd_files = keys %queued_graph_command;
		    @sent_graph_command{@rrd_files} = (1) x @rrd_files;
		}
		elsif ($DEBUG_ERROR) {
		    log_timed_message "ERROR:  Cannot write RRD graph data to Foundation on host $foundation_host; will skip for this cycle.";
		}
	    }
	}

	if ( $probe_for_metrics and provision_last_access_times() ) {
	    foreach my $obj_device ( keys %$device_statuses ) {
		if ($shutdown_requested) {
		    $fatal_condition = 1;
		    last;
		}

		my $device_status = $device_statuses->{$obj_device};
		if ($DEBUG_INFO) {
		    log_message "INFO:  found device $obj_device ($device_status->{CustomerResource})";
		    foreach my $obj_location ( keys %{ $device_status->{Probes} } ) {
			my $status_location = $device_status->{Probes}{$obj_location};
			log_message "INFO:      being monitored from location $obj_location ($status_location->{Location})";
		    }
		}
		my $obj_locations = [ keys %{ $device_status->{Probes} } ];

		# Note that find_device_metrics() is run internally with termination-signal catching disabled,
		# so we might not come back from this call.  That allows a long-running operation to be cut
		# short, by external request, in a safe place (i.e., not while we're in the middle of making
		# sensitive changes to external data repositories).  We'll make those changes after we come
		# back here, when we are once again under signal-termination catching control.
		#
		# Note that if we do get killed by an external signal while we're in this routine, we won't
		# generate a service-check result for this daemon (further below) during this cycle.
		$start_time = time();
		my ($devices, $metrics, $messages) = find_device_metrics( $obj_device, $obj_locations );
		$run_time = time() - $start_time;
		log_timed_message "STATS:  find_device_metrics($obj_device, [ @$obj_locations ]) took $run_time seconds" if $DEBUG_STATS;

		if ($DEBUG_INFO) {
		    foreach my $host_service ( keys %$metrics ) {
			log_message "INFO:      found metrics for host_service $host_service";
		    }
		}
		# FIX LATER:  In theory, we shouldn't update the timestamps in the database until we
		# have saved the metric data in the RRD files.  But if we got interrupted partway
		# through the RRD updates, or in between the RRD updates and the database updates, or
		# partway through the database updates, we would be left in a situation that would
		# cause some of those same RRD updates to be applied again in a future cycle -- and
		# that would cause RRD update errors, leading to probable loss of even more data (given
		# our strategy of bulk updates to a given RRD file).  So for the time being, we update
		# the database before the RRD files.  This could result in some missing data in the RRD
		# files, if this updating gets interrupted partway through.  Once we have code in place
		# for RRD updates to sense a duplicate-datapoint failure during a bulk update and turn
		# it into a succession of single-datapoint updates (with update failures ignored), we
		# can reverse the order of updates in these next two calls.
		save_updated_timestamps($devices);
		save_device_metrics($obj_device, $metrics);

		if ($generate_nagios_checks_from_metrics) {
		    ## The Nagios messages have been constructed while the metric data was being
		    ## fetched, so they are possibly slightly more up-to-date than what we got
		    ## from the device status listing.  But as of this writing, though the state
		    ## determination might be more nuanced, the human-readable message part of
		    ## the metric-derived service checks is less descriptive, which is why we
		    ## allow a configurable choice as to where the service checks come from.
		    ##
		    ## Probing for AlertSite metric data is currently rather slow, as it can only
		    ## be done device-by-device, and each probe takes a fair number of seconds to
		    ## respond.  $max_nagios_checks_to_queue is used to control sending blocks
		    ## of host and service check messages because if we save them up and send
		    ## them later, that will mean extra latency, which could be rather severe if
		    ## all the messages wait until the last device is probed for metric data.
		    ## Quite possibly, we are better off by limiting the amount of time before
		    ## each message is sent.  On the other hand, the overhead of sending Nagios
		    ## messages may depend on the transport we use.  Hence we allow this limit to
		    ## be configured.
		    push @nagios_messages, @$messages;
		    if (@nagios_messages >= $max_nagios_checks_to_queue) {
			send_nagios_messages(\@nagios_messages);
			@nagios_messages = ();
		    }
		}

		$got_valid_resource = 1;

		# In development mode, where we just want to test a single device at a time,
		# we want to abort the looping at this point, rather than continuing to process
		# additional devices, since otherwise the overall cycle period will be very long.
		last if $do_one_resource;

		last if $fatal_condition;
	    }
	}
    }

    # If we were requested to shut down earlier in this cycle, while our signal handling was
    # still in play so we got to here, we take the time to report the result of this cycle
    # even though that will take slightly more time to complete the cycle.  That's because
    # we want to accurately report the status of this daemon service under all conditions.
    # Note that if we were requested to shut down, the sending of the Nagios messages itself
    # can impose some extra delay in doing so, well beyond the expectation of urgency in the
    # shutdown request.  We will live with that possibility.

    my ($alertsite_warning, $alertsite_error) = $alertsite->daemon_status();
    $alertsite->clear_daemon_status();
    my $service_severity =
	( $alertsite_error || ( $fatal_condition && !$shutdown_requested ) ) ? nagios_plugin_numeric_service_severity('CRITICAL')
      : ( $alertsite_warning || $fatal_condition )                           ? nagios_plugin_numeric_service_severity('WARNING')
      :                                                                        nagios_plugin_numeric_service_severity('OK');
    my $message =
	$alertsite_error                             ? "Last error:  $alertsite_error"
      : $alertsite_warning                           ? "Last warning:  $alertsite_warning"
      : ( $fatal_condition && !$shutdown_requested ) ? 'Internal error -- see log file.'
      : $fatal_condition                             ? 'Interrupted by external agent.'
      :                                                'Operating normally.';
    push @nagios_messages,
      $alertsite->service_check_result( undef, $alertsite_monitoring_host,
	$probe_for_status ? $alertsite_status_service : $alertsite_metrics_service,
	$service_severity, $message );

    send_nagios_messages(\@nagios_messages);

    # quit after just one valid resource -- used mainly for development testing
    if ( $do_one_resource && $got_valid_resource ) {
	log_timed_message 'NOTICE:  Exiting after one valid resource, per command option.' if $DEBUG_NOTICE;
    }

    # quit after just one cycle -- used mainly for development testing
    if ($do_one_cycle) {
	log_timed_message 'NOTICE:  Exiting after one cycle, per command option.' if $DEBUG_NOTICE;
    }

    return ( ( $do_one_resource && $got_valid_resource ) || $do_one_cycle || $fatal_condition ) ? STOP_STATUS : CONTINUE_STATUS;
}

# FIX LATER:  Perhaps install local signal handlers in this routine to revert to DEFAULT behavior
# (signal kills process) for SIGINT, SIGQUIT, and SIGTERM.  That's because some database operations
# might not be effectively terminated by a signal if a signal handler is in place (DBD::mysql
# just recognizes EINTR after an interrupt occurs, and restarts whatever system call was executing).
sub read_monarch_database {
    ## We connect to the monarch database on every cycle, to avoid any issues with the MySQL daemon
    ## potentially being bounced while this long-running daemon stays up.  A connect here will
    ## implicitly destroy an existing connection, so there is no connection leakage involved.
    eval {
	my $auth = StorProc->dbconnect();
    };
    if ($@) {
	chomp $@;
	log_timed_message "ERROR:  Cannot connect to the \"monarch\" database:\n$@" if $DEBUG_ERROR;
	log_timed_message "NOTICE:  \"monarch\" database updates will be skipped in this cycle." if $DEBUG_NOTICE;
	return 0;
    }

    eval {
	# StorProc->fetch_map() is still marked as being an unstable interface, so
	# we could use these other routines for these purposes, if we so choose.
	# Note that these routines return hashes instead of hashrefs.
	#
	#	## This yields:  $host_id_by_name{$name} = $host_id;
	#	# %host_id_by_name = StorProc->get_table_objects('hosts', 0);
	#
	#	## This yields:  $servicename_id_by_name{$name} = $servicename_id;
	#	# %servicename_id_by_name = StorProc->get_table_objects('service_names', 0);
	#
	# StorProc->fetch_map() returns a hashref instead of a hash, so it is much more
	# efficient (returning a hash effectively returns a very large list, namely the
	# list-serialized list of all the key=>value pairs in the hash).

	# This yields:  $host_id_by_name->{$name} = $host_id;
	$host_id_by_name = StorProc->fetch_map( 'hosts', 'name', 'host_id' );

	# This yields:  $servicename_id_by_name->{$name} = $servicename_id;
	$servicename_id_by_name = StorProc->fetch_map( 'service_names', 'name', 'servicename_id' );

	# We ignore service instances here, as they will not be relevant to our processing.
	#
	# This yields:  $hosts_services{ $host_id }{ $servicename_id } = $service_id;
	%hosts_services = StorProc->get_hostid_servicenameid_serviceid();

	# FIX LATER:  Possibly, we should deal with inactive hosts within Monarch, by recording the
	# notion that they are intended to be inactive, and using that information when we go to
	# send service checks for such hosts.  But in practice, for now we ignore such settings in
	# favor of using the AlertSite-internal settings that control whether monitoring is enabled.
	#
	# FIX LATER:  Check the parameter:  false:  inactive w.r.t. Foundation; true:  inactive w.r.t. Nagios
	# %host_inactive = get_inactive_hosts(1);
	# $host_inactive{ $host_id } = 1;

	# We could use StorProc->get_hosts_vitals() for this purpose, if we so choose.
	#
	# This yields:  $host_at_address->{$address} = $name;
	$host_at_address = StorProc->fetch_map( 'hosts', 'address', 'name' );
    };
    if ($@) {
	chomp $@;
	log_timed_message "ERROR:  \"monarch\" database access failure:\n$@" if $DEBUG_ERROR;
	log_timed_message "NOTICE:  \"monarch\" database updates will be skipped in this cycle." if $DEBUG_NOTICE;
	return 0;
    }

    $last_unique_address = 0x7f010000;
    foreach my $address (keys %$host_at_address) {
	## We only deal with IPv4 addresses here, inasmuch as that's all we're going to generate.
	if ($address =~ /^(\d{1,3})\.(\d{1,3})\.(\d{1,3})\.(\d{1,3})$/) {
	    my $numeric_address = ($1 << 24) + ($2 << 16) + ($3 << 8) + $4;
	    ## See unique_address() for why we ignore anything outside of these limits.
	    next if $numeric_address < 0x7f010000;
	    next if $numeric_address > 0x7f01ffff;
	    if ($last_unique_address < $numeric_address) {
		$last_unique_address = $numeric_address;
	    }
	}
    }

    return 1;
}

# Generate a unique address which can be assigned to a host.
# We choose to use addresses in the range [127.1.0.0 .. 127.1.255.255],
# which all point to localhost, as a means of avoiding any addresses which
# are likely to be assigned to hosts unrelated to AlertSite monitoring.
sub unique_address {
    my $addr = pack('N', ++$last_unique_address);
    return sprintf("%vd", $addr);
}

# FIX LATER:  Perhaps install local signal handlers in this routine to revert to DEFAULT behavior
# (signal kills process) for SIGINT, SIGQUIT, and SIGTERM.  That's because some database operations
# might not be effectively terminated by a signal if a signal handler is in place (DBD::mysql
# just recognizes EINTR after an interrupt occurs, and restarts whatever system call was executing).
sub update_monarch_database {
    my $device_statuses                   = shift;
    my $hosts_added_to_monarch            = 0;
    my $generic_services_added_to_monarch = 0;
    my $host_services_added_to_monarch    = 0;
    my $successful                        = 1;
    eval {
	my $monarchapi = undef;
	foreach my $ObjDevice (keys %$device_statuses) {
	    my $device_status    = $device_statuses->{$ObjDevice};
	    my $host             = $device_status->{CustomerResource};
	    my $status_locations = $device_status->{Probes};
	    my $monitor_host     = $device_status->{Monitor};
	    ## Ignore resources that are not currently being monitored by AlertSite.
	    next if !defined($monitor_host) or $monitor_host ne 'y';
	    my $host_id = $host_id_by_name->{$host};
	    foreach my $ObjLocation (keys %$status_locations) {
		my $status_location = $status_locations->{$ObjLocation};
		my $service         = $status_location->{Location};
		my $monitor_service = $status_location->{Monitor};
		## Ignore {resource, location} pairs that are not currently being monitored by AlertSite.
		next if !defined($monitor_service) or $monitor_service ne 'y';
		my $servicename_id = $servicename_id_by_name->{$service};

		# if (resource is not a host in monarch) ...
		if (not defined $host_id) {
		    ## Attempt to add the host to monarch, and apply an entire host profile (hostgroup, etc.).
		    if (not defined $monarchapi) {
			$monarchapi = dassmonarch->new();
			$monarchapi->set_debuglevel('none');
		    }

		    log_timed_message "NOTICE:  Creating host $host in Monarch." if $DEBUG_NOTICE;

		    ## We could potentially use the unaltered, URL-unescaped TxnName field (from the
		    ## device list) or URL-unescaped Descrip field (from the device status) as the alias;
		    ## either would typically include spaces.  But for now, we settle for simplicity.
		    my $alias = $host;
		    ## We cannot use the FullIP field from the AlertSite device information for the host
		    ## address, because each of our constructed hosts must have a unique IP address within
		    ## GroundWork Monitor, and it is possible for multiple customer resources (here mapped
		    ## to separate hosts) at the same IP address to be monitored by AlertSite.  So we must
		    ## manufacture a unique address accessible to Nagios which should not conflict with
		    ## any IP address already in use by any other configured host.
		    if (not $monarchapi->import_host_api( $host, $alias, unique_address(), $alertsite_resource_host_profile, 1 )) {
			log_timed_message "ERROR:  Cannot create host $host:\n", $monarchapi->get_errormessages() if $DEBUG_ERROR;
			## Currently, our only means of clearing the errors in the dassmonarch instance
			## is to destroy the entire instance.
			$monarchapi = undef;
			next;
		    }

		    ## If we needed to assign a (configurable) hostgroup outside of what the host profile
		    ## refers to, this is how we would do so.
		    # $result = $monarchapi->assign_hostgroup( $host, $hostgroup );

		    # Unfortunately, we don't get the $host_id back when we add the host, so we need to go fish for it.
		    my @host_ids = StorProc->fetch_unique('hosts', 'host_id', 'name', $host);
		    if (@host_ids != 1) {
			log_timed_message "ERROR:  Cannot find the host_id of newly added host \"$host\"." if $DEBUG_ERROR;
			next;
		    }
		    $host_id = $host_ids[0];

		    # Stop processing if we didn't add the host properly.
		    next if not defined $host_id;

		    # Add the host to our cache of monarch hosts.
		    $host_id_by_name->{$host} = $host_id;

		    ++$hosts_added_to_monarch;
		}
		# if (location is not a service in monarch) ...
		if (not defined $servicename_id) {
		    ## Attempt to add the service to monarch, and apply an entire service template (service check, etc.).
		    if (not defined $monarchapi) {
			$monarchapi = dassmonarch->new();
			$monarchapi->set_debuglevel('none');
		    }

		    log_timed_message "NOTICE:  Creating generic service $service in Monarch." if $DEBUG_NOTICE;

		    ## We could potentially use the unaltered, URL-unescaped Location field (from
		    ## the device status) as the description; it would typically include spaces and
		    ## various punctuation characters.  But for now, we settle for simplicity.
		    my $description = '';
		    my %service_template = StorProc->fetch_one( 'service_templates', 'name', $alertsite_location_service_template );
		    my @values = ( '', $service, $description, $service_template{'servicetemplate_id'}, '', '', '', '', '' );
		    my $id = StorProc->insert_obj_id( 'service_names', \@values, 'servicename_id' );
		    if ( $id =~ /^Error/ ) {
			log_timed_message $id;
		    }
		    else {
			# GWMON-9934:  The $id returned by StorProc->insert_obj_id() in GWMEE 6.4 and earlier
			# is subject to a race condition that could yield an incorrect value under conditions
			# of concurrent insertions.  If we didn't have that problem, we would just:
			#     $servicename_id = $id;
			# But until that gets solved, we probe explicitly for the just-inserted servicename_id.
			my @servicename_ids = StorProc->fetch_unique('service_names', 'servicename_id', 'name', $service);
			if (@servicename_ids != 1) {
			    log_timed_message "ERROR:  Cannot find the servicename_id of newly added generic service \"$service\"." if $DEBUG_ERROR;
			    next;
			}
			$servicename_id = $servicename_ids[0];
		    }

		    # Stop processing if we didn't add the generic service properly.
		    next if not defined $servicename_id;

		    # Add the service to our cache of monarch services.
		    $servicename_id_by_name->{$service} = $servicename_id;

		    ++$generic_services_added_to_monarch;
		}
		# if ({resource,location} is not a host+service in monarch) ...
		if (!defined( $hosts_services{$host_id} ) or not $hosts_services{$host_id}{$servicename_id}) {
		    ## Attempt to add the host+service to monarch, and apply all the ancillary data also normally expected
		    ## (service overrides, contact groups, service dependencies, service externals).
		    if (not defined $monarchapi) {
			$monarchapi = dassmonarch->new();
			$monarchapi->set_debuglevel('none');
		    }

		    log_timed_message "NOTICE:  Creating host $host service $service in Monarch." if $DEBUG_NOTICE;

		    my $service_id = undef;
		    my %generic_service = StorProc->fetch_one('service_names', 'servicename_id', $servicename_id);
		    my @values = (
			'', $host_id, $servicename_id,
			$generic_service{'template'},
			$generic_service{'extinfo'},
			$generic_service{'escalation'},
			'1',
			$generic_service{'check_command'},
			$generic_service{'command_line'},
			'', ''
		    );
		    my $id = StorProc->insert_obj_id( 'services', \@values, 'service_id' );
		    if ( $id =~ /^Error/ ) {
			log_timed_message $id;
		    }
		    else {
			# GWMON-9934:  The $id returned by StorProc->insert_obj_id() in GWMEE 6.4 and earlier
			# is subject to a race condition that could yield an incorrect value under conditions
			# of concurrent insertions.  If we didn't have that problem, we would just:
			#     $service_id = $id;
			# But until that gets solved, we probe explicitly for the just-inserted service_id.
			my %where       = ( 'host_id' => $host_id, 'servicename_id' => $servicename_id );
			my @service_ids = StorProc->fetch_list_where('services', 'service_id', \%where);
			if (@service_ids != 1) {
			    log_timed_message "ERROR:  Cannot find the service_id of newly added host \"$host\" service \"$service\"." if $DEBUG_ERROR;
			    next;
			}
			$service_id = $service_ids[0];

			my @errs = StorProc->apply_service_overrides( $service_id, $servicename_id );
			log_timed_message $_ for @errs;

			%where = ( 'servicename_id' => $servicename_id );
			my %service_name_dependency = ();
			my %hash_array = StorProc->fetch_list_hash_array( 'servicename_dependency', \%where );
			foreach my $id ( keys %hash_array ) {
			    $service_name_dependency{$id}{'depend_on_host_id'} = $hash_array{$id}[2];
			    $service_name_dependency{$id}{'template'}          = $hash_array{$id}[3];
			}
			foreach my $dependency_id ( keys %service_name_dependency ) {
			    my $depend_on_host_id = $service_name_dependency{$dependency_id}{'depend_on_host_id'} || $host_id;
			    @values = (
				'', $service_id, $host_id, $depend_on_host_id,
				$service_name_dependency{$dependency_id}{'template'}, ''
			    );
			    my $result = StorProc->insert_obj( 'service_dependency', \@values );
			    log_timed_message $result if $result =~ /^Error/;
			}

			%where = ( 'servicename_id' => $servicename_id );
			my @external_ids = StorProc->fetch_list_where( 'external_service_names', 'external_id', \%where );
			foreach my $external_id (@external_ids) {
			    my %external = StorProc->fetch_one( 'externals', 'external_id', $external_id );
			    @values = ( $external_id, $host_id, $service_id, $external{'display'}, '0+0' );
			    my $result = StorProc->insert_obj( 'external_service', \@values );
			    log_timed_message $result if $result =~ /^Error/;
			}
		    }

		    # Stop processing if we didn't add the host service properly.
		    next if not defined $service_id;

		    # Add the host+service to our cache of monarch host+service pairs.
		    $hosts_services{$host_id}{$servicename_id} = $service_id;

		    ++$host_services_added_to_monarch;
		}
	    }
	}
    };
    if ($@) {
	chomp $@;
	log_timed_message "ERROR:  \"monarch\" database access failure:\n$@" if $DEBUG_ERROR;
	log_timed_message "NOTICE:  any remaining \"monarch\" database updates will be skipped in this cycle." if $DEBUG_NOTICE;
	$successful = 0;
    }
    if ( $hosts_added_to_monarch or $generic_services_added_to_monarch or $host_services_added_to_monarch ) {
	## Send a message to the operators indicating that a Commit will be needed to bring newly
	## configured monitored resources into view.  The message will appear in the Event Console.
	## Send just this one message per cycle, no matter how many objects were added in that cycle.
	my $message      = '';
	my @phrases      = ();
	my $object_count = $hosts_added_to_monarch + $generic_services_added_to_monarch + $host_services_added_to_monarch;
	if ($hosts_added_to_monarch) {
	    push @phrases, $hosts_added_to_monarch . ( $hosts_added_to_monarch == 1 ? ' host' : ' hosts' );
	}
	if ($generic_services_added_to_monarch) {
	    push @phrases,
	      $generic_services_added_to_monarch . ( $generic_services_added_to_monarch == 1 ? ' generic service' : ' generic services' );
	}
	if ($host_services_added_to_monarch) {
	    push @phrases, $host_services_added_to_monarch . ( $host_services_added_to_monarch == 1 ? ' host service' : ' host services' );
	}
	if ( @phrases == 1 ) {
	    $message = $phrases[0];
	}
	elsif ( @phrases == 2 ) {
	    $message = $phrases[0] . ' and ' . $phrases[1];
	}
	else {
	    $message = $phrases[0] . ', ' . $phrases[1] . ', and ' . $phrases[2];
	}
	$message .=
	    ( $object_count == 1 ? ' has' : ' have' )
	  . ' been added to the configuration for AlertSite-monitored resources.  A Commit is needed to bring'
	  . ( $object_count == 1 ? ' it' : ' them' )
	  . ' into view.';
	my $errors = $foundation->send_log_message( SEVERITY_WARNING, APP_SYSTEM, $message );
	log_timed_message @$errors if $errors;
    }
    return $successful;
}

sub provision_last_access_times {
    ## It's okay if we're already connected; the dbconnect() routine will disconnect and
    ## reconnect if that's the case.  Generally, we do want to do this once per cycle,
    ## just to make sure we have a connection in case the database server got bounced
    ## since the last cycle.  But we don't want to do this more often than that.
    if ( not $alertsite->dbconnect() ) {
	## We don't issue a log message at this level regarding the failure to connect,
	## because the dbconnect() routine should have already provided adequate detail.
	return 0;
    }
    return 1;
}

sub find_device_list {
    ## Establish the list of devices that may be monitored in this cycle.
    ## There are only two reasons to do this:
    ## (*) To make available the derived StepSize field, so that any newly
    ##     created RRD files are correctly constructed.
    ## (*) To make available the Monitor field, which tells whether AlertSite
    ##     monitoring has been disabled at the customer resource level (as
    ##     opposed to the level of the individual location [city] from which
    ##     AlertSite may probe the customer resource).  This is used to tell
    ##     whether we need to ensure that the customer resource is listed as
    ##     a host in the "monarch" database, or whether we can just skip it.
    ##     (A listing of device status provides similar Monitor flags at the
    ##     level of {resource, location} pairs, but not at the resource level.)
    ## The list is not otherwise used, as fetching device status yields all the
    ## other information we need that is provided when trying to list devices.
    $alertsite->set_device_list( $alertsite->get_device_list() );
}

sub find_device_statuses {
    return $alertsite->set_device_status( $alertsite->get_device_status() );
}

sub find_device_metrics {
    ## Because it can take a rather long time to fetch AlertSite probe metrics
    ## for a single device (anywhere from 10 to 55 seconds seen in testing),
    ## we allow this routine (and the entire script) to be unceremoniously
    ## interrupted by external agent.  That isolates the interruptable period
    ## to operations that do not modify external data repositories.  Once we
    ## return from this routine, we will be back under control of our signal
    ## handling routines, and can then safely update such repositories without
    ## fear of being interrupted in the middle of such an operation.
    local $SIG{INT}  = 'DEFAULT';
    local $SIG{QUIT} = 'DEFAULT';
    local $SIG{TERM} = 'DEFAULT';

    return $alertsite->get_device_metrics(@_);
}

sub save_updated_timestamps {
    my $devices = shift;
    foreach my $obj_device (@$devices) {
	$alertsite->update_last_access_time($obj_device);
    }
}

sub save_device_metrics {
    $alertsite->create_and_update_rrd_files(@_);
}

sub send_nagios_messages {
    my $messages = shift;  # array ref

    if (@$messages) {
	if ( not $send_to_nagios ) {
	    log_timed_message "NOTICE:  $PROGNAME is configured to not send messages to Nagios." if $DEBUG_NOTICE;
	}
	else {
	    ## We want to reliably send the messages to Nagios.  In the current release, we
	    ## just write to the Nagios command pipe (though very carefully, taking into account
	    ## the usually-unrecognized trickiness of writing to a pipe).  In a future release, for
	    ## simplicity, we will allow writing via the Bronx socket, as an option.  Regardless of
	    ## the chosen transport, at the moment we attempt this writing, Nagios might be down
	    ## (say, because it's in the middle of a Commit operation), so we might be tempted
	    ## to queue the messages using the GDMA spooler instead.  But currently, the GDMA
	    ## spooler can only be used to send host and service check results, not commands.

	    ## The later implementation will allow writes to the Bronx socket mostly so we can
	    ## potentially move this integration onto a child server if that turns out to be useful
	    ## at some customer site.  But when we add this capability, we will make the selection
	    ## of pipe vs. socket a configuration option, thus retaining flexibility as to how the
	    ## data transfer will occur.  Writing directly to the Nagios command pipe avoids the
	    ## overhead of forking a send_nsca process and the complex NSCA protocol needed for the
	    ## Bronx socket.

	    if ($use_nsca) {
		## FIX LATER:  Allow optionally writing all the messages efficiently to the Bronx socket.
		## I suppose that means we should run send_nsca to handle the connection protocol.
		## But all the heavy lifting should be done inside a separate GW::Bronx module.
		## FIX LATER:  Make sure the messages we constructed above are the right format for
		## writing to the Bronx socket.
		log_timed_message 'ERROR:  use_nsca is not yet supported in this application.' if $DEBUG_ERROR;
	    }
	    else {
		my $nagios = GW::Nagios->new( $nagios_command_pipe, $max_command_pipe_write_size, $max_command_pipe_wait_time );
		if ( not defined $nagios ) {
		    if ($DEBUG_ERROR) {
			## There is no sense in dying once we have logged this error, because this is
			## a daemon that must carry on and continue to operate during the next cycle.
			my $count = scalar @$messages;
			log_timed_message 'ERROR:  Creating a GW::Nagios object has failed; thus';
			log_timed_message "        $count messages will not be sent to Nagios.";
		    }
		}
		else {
		    my $errors = $nagios->send_messages_to_nagios($messages);
		    log_timed_message $_ for @$errors;
		}
	    }
	}
    }
}

# This signal handler is for ordinary use, during code that can be expected to check the
# $shutdown_requested flag fairly often.
sub handle_exit_signal {
    my $signame = shift;
    $shutdown_requested = 1;

    # for developer debugging only
    log_timed_message "NOTICE:  Received SIG$signame; aborting!" if $DEBUG_NOTICE;
}

sub sig_pipe_handler {
    sleep 2;
}

# For initial debugging only.
sub printstack {
    my $i = 0;
    while (my ($package, $filename, $line, $subroutine) = caller($i++)) {
	print STDERR "${package}, ${filename} line $line (${subroutine})\n";
    }
}
