#!/usr/local/groundwork/perl/bin/perl -w --

# nagios2collage_eventlog.pl

# Copyright (c) 2007-2018 GroundWork Open Source, Inc. (GroundWork).
# All rights reserved.  Use is subject to GroundWork commercial license terms.

# TO DO:
# (*) Finish the implementation of calling the Foundation REST API.
# (*) Review the entire script to cover areas we have not yet flagged as needing changes
#     to support the Foundation REST API.
# (*) Implement some number of retries if the REST API returns certain types of failures.
# (*) Process all new events (when we reach the new bundle size limit, or timeout, or
#     reach EOF on the input file), then any updates queued at that time.
# (*) FIX MAJOR:  Figure out how the new REST API (that is, the GW::RAPID package and any
#     routines it calls) responds to a SIGTERM.  Does it allow itself to be interrupted so
#     our $shutdown_requested flag can be set by our signal handler?  If it is interrupted,
#     does it resume operation, or fail immediately with some sort of error return?  Adapt
#     this feeder to however it behaves.

use strict;

use Time::Local;
use IO::Socket;
use DBI;
use CollageQuery;
use TypedConfig;
use Log::Log4perl;    # For logging from the GW::RAPID package.

use Data::Dumper;
$Data::Dumper::Indent   = 1;
$Data::Dumper::Sortkeys = 1;

# BE SURE TO UPDATE THIS WITH EVERY CHANGE.
my $VERSION = '7.2.2.0';

# --------------------------------------------------------------------------------
# General config options.
# --------------------------------------------------------------------------------

my $debug_config = 0;    # if set, spill out certain data about config-file processing to STDOUT

# A fixed value for this script.  Other config option values are drawn from this file.
my $default_config_file = '/usr/local/groundwork/config/event-feeder.conf';

# 0 => minimal, 1 => summary, 2 => basic, 3 => print output data, 4 => print input lines.
my $debug_level = undef;

# The absolute pathname to the Nagios event log file.
my $eventfile = undef;

# The absolute pathname to a seek file which the nagios2collage_eventlog.pl
# process uses to keep track of its place within the Nagios event log file.
my $seekfile = undef;

# The absolute pathname to where the nagios2collage_eventlog.pl writes
# its own loggging data.
my $logfile = undef;

# Rough measure of max time to hoard incoming messages before passing them on.
my $sync_timeout_seconds = undef;

# Wait time in seconds between checks of the Nagios nagios.log file.
my $cycle_sleep_time     = undef;

# Seconds to sleep before restarting after failure, to prevent tight looping.
my $failure_sleep_time   = undef;    # seconds

# The name of the server which is running this daemon.  For a parent server,
# this is typically just set to "localhost".
my $thisnagios = undef;

# Set to 1 to use the REST API instead of the $remote_port socket API.
my $use_rest_api = undef;

# --------------------------------------------------------------------------------
# Options for controlling special-case treatment of events for certain services.
# --------------------------------------------------------------------------------

my %attribute_mappings = ();

my $send_host_notification_events    = undef;
my $send_service_notification_events = undef;

# --------------------------------------------------------------------------------
# Options for sending event data to Foundation via the Foundation REST API.
# --------------------------------------------------------------------------------

# The application name by which the nagios2collage_eventlog.pl process
# will be known to the Foundation REST API.
my $rest_api_requestor = undef;

# Where to find credentials for accessing the Foundation REST API.
my $ws_client_config_file = undef;

my $rest_event_bundle_size     = undef;    # This is NOT the minimum size ...
my $max_rest_event_bundle_size = undef;    # but this is the maximum size.

# Bundling REST messages sent to Foundation might reduce downstream race conditions in the
# current version of Foundation.  If that is so, we would be willing to wait a small period of
# time for additional messages to come in, before sending on any received messages.  Thus we
# might possibly want to retain some messages across (relatively short) feeder cycles, and thus
# we need to flush any such retained messages whenever we handle a shutdown request.
#
# Must all queued results be sent in the same cycle, or can some be held over from
# one processing cycle to the next?  We now generally set this to true, because our
# $rest_event_bundle_size setting is fairly large and we don't want to be holding on
# to messages for any significant length of time waiting for a bundle to fill up.
my $flush_rest_bundle_each_cycle = undef;

# There are six predefined log levels within the Log4perl package:  FATAL, ERROR, WARN, INFO,
# DEBUG, and TRACE (in descending priority).  We define two custom levels at the application
# level to form the full useful set:  FATAL, ERROR, WARN, NOTICE, STATS, INFO, DEBUG, and TRACE.
# To see an individual message appear, your configured logging level here has to at least match
# the priority of that logging message in the code.
my $GW_RAPID_log_level = undef;

# Application-level logging configuration, for that portion of the logging
# which is currently handled by the Log4perl package.
my $log4perl_config = undef;

# --------------------------------------------------------------------------------
# Options for sending event data to Foundation via the legacy XML socket API.
# --------------------------------------------------------------------------------

# Set to 0 to log Foundation messages as ISO-8859-1, to 1 to log as UTF-8.
my $log_as_utf8 = undef;

# Where to access the legacy Foundation XML socket API.
my $remote_host = undef;
my $remote_port = undef;

# The $max_xml_bundle_size has complex downstream effects.  Before changing this value,
# please consult with GroundWork Support or GroundWork Professional Services.

my $xml_bundle_size     = undef;    # This is NOT the minimum size ...
my $max_xml_bundle_size = undef;    # but this is the maximum size.

# This is the actual SO_SNDBUF value, as set by setsockopt().  This is therefore the actual size of
# the data buffer available for writing, irrespective of additional kernel bookkeeping overhead.
# This will have no effect without the companion as-yet-undocumented patch to IO::Socket::INET.
# Set this to 0 to use the system default socket send buffer size.  (Note that the value specified
# here is likely to be limited to something like 131071 by the sysctl net.core.wmem_max parameter.)
my $send_buffer_size = undef;

# This timeout is here only for use in emergencies, when Foundation has completely frozen up and is no
# longer reading (will never read) a socket we have open.  We don't want to set this value so low that
# it will interfere with normal communication, even given the fact that Foundation may wait a rather
# long time between sips from this straw as it processes a large bundle of messages that we sent it, or
# is otherwise busy and just cannot get back around to reading the socket in a reasonably short time.
my $socket_send_timeout = undef;    # seconds; to address GWMON-7407; set to 0 to disable

# Bundling XML messages sent to Foundation helps reduce downstream race conditions in the current
# version of Foundation.  Hence we are willing to wait a small period of time for additional
# messages to come in, before sending on any received messages.  Thus we might possibly want to
# retain some messages across (relatively short) feeder cycles, and thus we need to flush any such
# retained messages whenever we handle a shutdown request.
#
# Must all queued results be sent in the same cycle, or can some be held over from one processing
# cycle to the next?  We now set this to true, because our $xml_bundle_size setting is fairly large
# and we don't want to be holding on to messages for any significant length of time waiting for a
# bundle to fill up.
my $flush_xml_bundle_each_cycle = undef;

# --------------------------------------------------------------------------------
# Static configuration.
# --------------------------------------------------------------------------------

# These two messages are used for the legacy XML socket API.
my $start_message =
    "<GENERICLOG consolidation='SYSTEM' ApplicationType='SYSTEM' MonitorServerName='localhost' Device='127.0.0.1' Severity='OK' MonitorStatus='OK' TextMessage='Foundation-Nagios log check process started.' />";
my $command_close = '<SERVICE-MAINTENANCE command="close" />';

use constant ERROR_STATUS    => 0;
use constant STOP_STATUS     => 1;
use constant RESTART_STATUS  => 2;
use constant CONTINUE_STATUS => 3;

# --------------------------------------------------------------------------------
# Working variables.
# --------------------------------------------------------------------------------

# Derived flags, for easy testing.
my $debug_summary = undef;
my $debug_basic   = undef;
my $debug_output  = undef;
my $debug_input   = undef;

my $next_sync_timeout = 0;    # Time past which a bundle should finally be sent.

# XML batching for performance in large installations, for use with the Foundation socket API.
my @xml_messages = ();

# Local handle for the REST API object, if configured above for use.
my $rest_api = undef;

# Event batching for performance in large installations, for use with the Foundation REST API.
my @create_event_list = ();
my @ack_event_list    = ();
my @unack_event_list  = ();

my $message_counter = 0;  # Should rename this: it does not include ACKNOWLEDGE messages.

# This host -> IPaddress mapping will only be updated when this script restarts.
# We ensure it gets updated when we pause for a Commit operation, by restarting.
my %hostipaddress = ();

my %mapped_services = ();

my $eventfile_device = -1;
my $eventfile_inode  = -1;
my @seek_pos = ( 0, $eventfile_device, $eventfile_inode );  # seek position, ...

my $logtime = '';

our $shutdown_requested = 0;

# --------------------------------------------------------------------------------
# Program
# --------------------------------------------------------------------------------

# Here is the entire substance of this script, in a one-liner:
exit ((main() == ERROR_STATUS) ? 1 : 0);

# To be kind to the server and always disconnect our session, we attempt to force a shutdown
# of the REST API before global destruction sets in and makes it impossible to log out.
END {
    terminate_rest_api() if $use_rest_api;
}

# --------------------------------------------------------------------------------
# Supporting Subroutines
# --------------------------------------------------------------------------------

sub read_config_file {
    my $config_file = shift;
    local $_;

    eval {
	my $config = TypedConfig->new($config_file);

	$debug_level                      = $config->get_number('debug_level');
	$eventfile                        = $config->get_scalar('eventfile');
	$seekfile                         = $config->get_scalar('seekfile');
	$logfile                          = $config->get_scalar('logfile');
	$sync_timeout_seconds             = $config->get_number('sync_timeout_seconds');
	$cycle_sleep_time                 = $config->get_number('cycle_sleep_time');
	$failure_sleep_time               = $config->get_number('failure_sleep_time');
	$thisnagios                       = $config->get_scalar('thisnagios');
	$use_rest_api                     = $config->get_boolean('use_rest_api');
	$send_host_notification_events    = $config->get_boolean('send_host_notification_events');
	$send_service_notification_events = $config->get_boolean('send_service_notification_events');
	$rest_api_requestor               = $config->get_scalar('rest_api_requestor');
	$ws_client_config_file            = $config->get_scalar('ws_client_config_file');
	$rest_event_bundle_size           = $config->get_number('rest_event_bundle_size');
	$max_rest_event_bundle_size       = $config->get_number('max_rest_event_bundle_size');
	$flush_rest_bundle_each_cycle     = $config->get_boolean('flush_rest_bundle_each_cycle');
	$GW_RAPID_log_level               = $config->get_scalar('GW_RAPID_log_level');
	$log4perl_config                  = $config->get_scalar('log4perl_config');
	$log_as_utf8                      = $config->get_boolean('log_as_utf8');
	$remote_host                      = $config->get_scalar('remote_host');
	$remote_port                      = $config->get_number('remote_port');
	$xml_bundle_size                  = $config->get_number('xml_bundle_size');
	$max_xml_bundle_size              = $config->get_number('max_xml_bundle_size');
	$send_buffer_size                 = $config->get_number('send_buffer_size');
	$socket_send_timeout              = $config->get_number('socket_send_timeout');
	$flush_xml_bundle_each_cycle      = $config->get_boolean('flush_xml_bundle_each_cycle');

	my %attribute_mappings_hash = $config->get_hash('attribute_mappings');
	print Data::Dumper->Dump( [ \%attribute_mappings_hash ], [qw(\%attribute_mappings_hash)] ) if $debug_config;

	%mapped_services = defined( $attribute_mappings_hash{'service'} ) ? %{ $attribute_mappings_hash{'service'} } : ();
	## Remove any entries all of whose attribute definitions are commented out,
	# so we don't waste time on them later on.
	foreach my $pattern ( keys %mapped_services ) {
	    if ( $pattern =~ /^\s+|\s+$/ ) {
		die "<service> name pattern \"$pattern\" contains leading or trailing whitespace\n";
	    }
	    eval { qr{$pattern} };
	    if ($@) {
		chomp $@;
		die "<service> name pattern \"$pattern\" is invalid:  $@\n";
	    }
	    if ( %{ $mapped_services{$pattern} } ) {
		## Verify that all attributes are valid.
		foreach my $attribute ( keys %{ $mapped_services{$pattern} } ) {
		    ## Verify that we have only one of the expected attribute names.
		    if ( not grep $attribute eq $_, 'application_type', 'consolidation_criteria' ) {
			die "found invalid attribute name \"$attribute\" for <service $pattern>\n";
		    }
		    ## Verify that we have at most one of each attribute name.
		    if ( ref( $mapped_services{$pattern}{$attribute} ) eq 'ARRAY' ) {
			die "found multiple \"$attribute\" attributes for <service $pattern>\n";
		    }
		    ## We expect only all-uppercase application-type and consolidation-criteria values,
		    ## to match our conventions for these names in Foundation.  We may as well verify
		    ## correctness here and now instead of wondering later on why we get a failure.
		    ## This won't detect anything like an embedded # character,
		    if ( $mapped_services{$pattern}{$attribute} !~ /^[A-Z]+$/ ) {
			die "found invalid value for the \"$attribute\" attribute for <service $pattern>\n";
		    }
		}
	    }
	    else {
		delete $mapped_services{$pattern};
	    }
	}
	print Data::Dumper->Dump( [ \%mapped_services ], [qw(\%mapped_services)] ) if $debug_config;

	# FIX LATER:  range-validate more of the values we obtained from the config file

	$debug_summary = $debug_level >= 1;
	$debug_basic   = $debug_level >= 2;
	$debug_output  = $debug_level >= 3;
	$debug_input   = $debug_level >= 4;

	if ($rest_event_bundle_size <= 0) {
	    die "rest_event_bundle_size must be at least 1\n";
	}
	if ($max_rest_event_bundle_size <= 0) {
	    die "max_rest_event_bundle_size must be at least 1\n";
	}

	# Security constraint.  There doesn't seem to be a way to outlaw this through the
	# Log::Log4perl package itself, so we must do so at the application level.
	if ($log4perl_config =~ /^(ldap|https?|ftp|wais|gopher|file):/i) {
	    die "Reading Log::Log4perl configuration from a URL is not supported.\n";
	}

	chomp $thisnagios;
    };
    if ($@) {
	chomp $@;
	$@ =~ s/^ERROR:\s+//i;
	sleep 10;
	die "Error:  Cannot read config file $config_file ($@).\n";
    }
}


sub freeze_logtime {
    $logtime = '[' . ( scalar localtime ) . '] ';
}

sub time_text {
    my $timestamp = shift;
    if ( $timestamp <= 0 ) {
	return "none";
    }
    else {
	my ( $sec, $min, $hour, $dom, $mon, $year, $wday, $yday, $dst ) = localtime($timestamp);
	return sprintf "%04d-%02d-%02d %02d:%02d:%02d", $year + 1900, $mon + 1, $dom, $hour, $min, $sec;
    }
}

# See the Foundation REST API doc for supported timestamp formats.
# There is terrible ambiguity in the doc about what format to use for inserted
# timestamps, so we have simply settled on something that seems to work.
sub rest_time_text {
    my $timestamp = shift;
    if ( $timestamp <= 0 ) {
	return "none";
    }
    else {
	## This formulation, being the UNIX epoch time in milliseconds, is a supported
	## format for sending timestamp data to the Foundation REST API.  It works now in
	## testing, and it is perhaps the most efficient encoding.  The only reason we're
	## not using it is because we seem to like a more-readable format, even though
	## that is less efficient and it will be very rare for anybody to read this data.
	## return "${timestamp}000";

	## Because of the difference in timezone offset between GMT and the local timezone,
	## this fixed-as-GMT formulation is easy for humans to misinterpret if it is ever
	## printed in diagnostic log messages, but it is always technically correct.
	my ( $sec, $min, $hour, $dom, $mon, $year, $wday, $yday, $dst ) = gmtime($timestamp);
	return sprintf "%04d-%02d-%02dT%02d:%02d:%02d+0000", $year + 1900, $mon + 1, $dom, $hour, $min, $sec;

	## If we don't like that formulation either, and want to force the timestamp to be
	## represented in the local timezone, our best bet would be to go with a DateTime
	## object and a JSON serializer for it that would express the value in the local
	## timezone.  The conversion from UNIX epoch time to DateTime and then back to
	## string form is a lot of overhead, though, for questionable benefit, so we
	## haven't developed or tested such code yet.
    }
}

sub FormatTime {
    my $intimestring = shift;
    my $outtimestring;
    if ( $intimestring =~ /(\d{2})\/(\d{2})\/(\d{4}) (\d{2}:\d{2}:\d{2})/ ) {
	$outtimestring = "$3-$1-$2 $4";
    }
    return $outtimestring;
}

sub log_message {
    print LOG @_, "\n";
}

sub log_timed_message {
    freeze_logtime();
    print LOG $logtime, @_, "\n";
}

sub log_shutdown {
    log_timed_message "=== Shutdown requested; terminating (process $$). ===";
}

sub log_socket_problem {
    my $type = $_[0];
    log_timed_message "Trouble $type socket: $!";
}

sub log_outcome {
    my $outcome = $_[0];
    my $context = $_[1];

    if ($debug_summary) {
	if (%$outcome) {
	    log_timed_message "ERROR:  Outcome of $context:";
	    foreach my $key ( sort keys %$outcome ) {
		log_timed_message "    $key => $outcome->{$key}";
	    }
	}
	else {
	    log_timed_message "ERROR:  No outcome data returned for failed $context.";
	}
    }
}

sub log_results {
    my $results = $_[0];
    my $context = $_[1];

    if ($debug_summary) {
	if ( ref $results eq 'HASH' ) {
	    if (%$results) {
		log_timed_message "ERROR:  Results of $context:";
		foreach my $key ( sort keys %$results ) {
		    if ( ref $results->{$key} eq 'HASH' ) {
			foreach my $subkey ( sort keys %{ $results->{$key} } ) {
			    if ( ref $results->{$key}{$subkey} eq 'HASH' ) {
				foreach my $subsubkey ( sort keys %{ $results->{$key}{$subkey} } ) {
				    if ( ref $results->{$key}{$subkey}{$subsubkey} eq 'HASH' ) {
					foreach my $subsubsubkey ( sort keys %{ $results->{$key}{$subkey}{$subsubkey} } ) {
					    log_message("    ${key}{$subkey}{$subsubkey}{$subsubsubkey} => '$results->{$key}{$subkey}{$subsubkey}{$subsubsubkey}'");
					}
				    }
				    else {
					log_message("    ${key}{$subkey}{$subsubkey} => '$results->{$key}{$subkey}{$subsubkey}'");
				    }
				}
			    }
			    else {
				log_message("    ${key}{$subkey} => '$results->{$key}{$subkey}'");
			    }
			}
		    }
		    else {
			log_message("    $key => '$results->{$key}'");
		    }
		}
	    }
	    else {
		log_timed_message "ERROR:  No results data returned for failed $context.";
	    }
	}
	elsif ( ref $results eq 'ARRAY' ) {
	    if (@$results) {
		log_timed_message "ERROR:  Results of $context:";
		my $i = 0;
		foreach my $result (@$results) {
		    if ( ref $result eq 'HASH' ) {
			foreach my $key ( keys %$result ) {
			    log_timed_message("    result[$i]{$key} => '$result->{$key}'");
			}
		    }
		    else {
			log_timed_message "    result[$i]:  $result";
		    }
		    ++$i;
		}
	    }
	    else {
		log_timed_message "ERROR:  No results data returned for failed $context.";
	    }
	}
	else {
	    log_timed_message 'ERROR:  Internal programming error when displaying results (' . code_coordinates() . ').';
	}
    }
}

sub main {
    read_config_file ($default_config_file);

    if ( !open( LOG, '>>', $logfile ) ) {
	print "Can't open logfile $logfile ($!)\n";
	## FIX MINOR:  follow the perf-data script model to record an error and send a summary log message to Foundation
	sleep $failure_sleep_time;
	return ERROR_STATUS;
    }
    LOG->autoflush(1);

    log_timed_message "=== Starting up (process $$). ===";

    # Long term, this won't be an issue, because we will permanently switch over to the REST API.
    # But in the near term, when there might be some question about what API is in use for a particular run,
    # it's simplest to just reflect that choice into the logfile.
    log_timed_message "NOTICE:  Running in " . ( $use_rest_api ? 'REST' : 'XML' ) . " API mode.";

    if ( !open( STDERR, '>>&LOG' ) ) {
	log_timed_message "ERROR:  Can't redirect STDERR to '$logfile': $!";
	sleep $failure_sleep_time;
	return ERROR_STATUS;
    }
    else {
	## Autoflush the error output on every single write, to avoid problems
	## with block i/o and badly interleaved output lines on LOG and STDERR.
	STDERR->autoflush(1);
    }

    # Set up to handle broken pipe errors.  This has to be done in conjunction with later code that
    # will cleanly process an EPIPE return code from a socket write.
    #
    # Our trivial signal handler turns SIGPIPE signals generated when we write to sockets already
    # closed by the server into EPIPE errors returned from the write operations.  The same would
    # happen if instead we just ignored these signals, but with this mechanism we also automatically
    # impose a short delay (inside the signal handler) when this situation occurs -- there is little
    # reason to keep pounding the server when it has already indicated it cannot accept data just now.
    $SIG{"PIPE"} = \&sig_pipe_handler;

    my $daemon_status = synchronized_daemon();

    close LOG;

    return $daemon_status;
}

sub synchronized_daemon {
    local $_;

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
	terminate_feeder();
	return $init_status;
    }

    while (1) {
	if ($shutdown_requested) {
	    flush_pending_output();
	    log_shutdown();
	    terminate_feeder();
	    return STOP_STATUS;
	}

	if ( !Locks->wait_for_file_to_disappear( $Locks::in_progress_file, \&log_timed_message, \$shutdown_requested ) ) {
	    flush_pending_output();
	    log_shutdown();
	    terminate_feeder();
	    return STOP_STATUS;
	}

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
		terminate_feeder();
		return STOP_STATUS;
	    }
	}

	( $dev, $ino, $mode, $nlink, $uid, $gid, $rdev, $size, $atime, $mtime, @rest ) = stat( \*commit_lock );
	if ( $mtime != $initial_mtime ) {
	    Locks->close_and_unlock( \*commit_lock );
	    flush_pending_output();
	    log_timed_message("=== A commit has occurred; will exit to start over and re-initialize (process $$). ===");
	    terminate_feeder();
	    return RESTART_STATUS;
	}

	my $cycle_status = perform_feeder_cycle_actions();

	Locks->close_and_unlock( \*commit_lock );

	if ($cycle_status != CONTINUE_STATUS) {
	    flush_pending_output();
	    log_timed_message("=== Cycle status is not to continue; will exit (process $$). ===");
	    terminate_feeder();
	    return $cycle_status;
	}

	if ($shutdown_requested) {
	    flush_pending_output();
	    log_shutdown();
	    terminate_feeder();
	    return STOP_STATUS;
	}

	# Sleep until the next cycle boundary.
	sleep $cycle_sleep_time;
    }
}

sub flush_pending_output {
    my $sent_output  = 0;
    my $SentCount    = 0;
    my $DroppedCount = 0;

    if ($use_rest_api) {
	if (@create_event_list) {
	    ## Note that $message_counter may well be -1 at this point.
	    $message_counter = output_events_to_rest_api( 'create', \@create_event_list, $message_counter, \$SentCount, \$DroppedCount );
	    @create_event_list = ();
	    $sent_output ||= $SentCount && !$DroppedCount;
	}
	if (@ack_event_list) {
	    $SentCount    = 0;
	    $DroppedCount = 0;
	    $message_counter = output_events_to_rest_api( 'ack', \@ack_event_list, $message_counter, \$SentCount, \$DroppedCount );
	    @ack_event_list = ();
	    $sent_output ||= $SentCount && !$DroppedCount;
	}
	if (@unack_event_list) {
	    $SentCount    = 0;
	    $DroppedCount = 0;
	    $message_counter = output_events_to_rest_api( 'unack', \@unack_event_list, $message_counter, \$SentCount, \$DroppedCount );
	    @unack_event_list = ();
	    $sent_output ||= $SentCount && !$DroppedCount;
	}
    }
    else {
	if (@xml_messages) {
	    ## Note that $message_counter may well be -1 at this point.
	    $message_counter = output_bundle_to_socket( \@xml_messages, $message_counter );
	    @xml_messages    = ();
	    $sent_output     = 1;
	}
    }

    if ( $sent_output and $message_counter < 0 ) {
	## The seek file was previously updated to account for the messages we just tried and failed to output,
	## presuming that sending them would eventually be successful, even though they were not sent yet.  Since the
	## attempt to flush the queued messages has failed, we need to roll back to the last known good seek position,
	## so we will re-read those messages upon startup (if the event file has not been rolled in the interim).
	my $update_status = update_seek_file( $seek_pos[0], 0 );
    }
}

sub handle_exit_signal {
    my $signame = shift;
    $shutdown_requested = 1;

    # for developer debugging only
    # log_timed_message "ERROR:  Received SIG$signame; aborting!";
}

sub sig_pipe_handler {
    sleep 2;
}

sub initialize_rest_api {
    require GW::RAPID;

    # Basic security:  disallow code in the logging config data.
    Log::Log4perl::Config->allow_code(0);

    # Here we add custom logging levels to form our full standard complement.  There are six
    # predefined log levels:  FATAL, ERROR, WARN, INFO, DEBUG, and TRACE (in descending priority).
    # We add NOTICE and STATS levels to the default set of logging levels supplied by Log4perl,
    # to form the full useful set:  FATAL, ERROR, WARN, NOTICE, STATS, INFO, DEBUG, and TRACE
    # (excepting NONE, I suppose, though there is some hint in the code that OFF is also supported).
    # This *must* be done before the call to Log::Log4perl::init().
    Log::Log4perl::Logger::create_custom_level("NOTICE", "WARN");
    Log::Log4perl::Logger::create_custom_level("STATS", "NOTICE");

    # If we wanted to support logging either through a syslog appender (I'm not sure how this would
    # be done; presumably via something other than Log::Dispatch::Syslog, since that is still
    # Log::Dispatch) or through Log::Dispatch, the following code extensions would come in handy.
    # (Frankly, I'm not really sure that Log4perl even supports syslog logging other than through
    # Log::Log4perl::JavaMap::SyslogAppender, which just wraps Log::Dispatch::Syslog.)
    #
    # use Sys::Syslog qw(:macros);
    # use Log::Dispatch;
    # my $log_null = Log::Dispatch->new( outputs => [ [ 'Null', min_level => 'debug' ] ] );
    # Log::Log4perl::Logger::create_custom_level("NOTICE", "WARN", LOG_NOTICE, $log_null->_level_as_number('notice'));
    # Log::Log4perl::Logger::create_custom_level("STATS", "NOTICE", LOG_INFO, $log_null->_level_as_number('info'));

    # This logging setup is an application-global initialization for the Log::Log4perl package, so
    # it only makes sense to initialize it at the application level, not in some lower-level package.
    #
    # It's not documented, but apparently Log::Log4perl::init() always returns 1, even if
    # it is handed a garbage configuration as a literal string.  That makes it hard to tell
    # if you really have it configured correctly.  On the other hand, if it's handed the
    # path to a missing config file, it throws an exception (also undocumented).
    eval {
	## If the value starts with a leading slash, we interpret it as an absolute path to a file that
	## contains the logging configuration data.  Otherwise, we interpret it as the data itself.
	Log::Log4perl::init( $log4perl_config =~ m{^/} ? $log4perl_config : \$log4perl_config );
    };
    if ($@) {
	chomp $@;
	log_timed_message "ERROR:  Could not initialize Log::Log4perl logging:\n$@";
	return 0;
    }

    # Initialize the REST API object.
    my %rest_api_options = (
	logger => Log::Log4perl::get_logger("Nagios.Event.Feeder.GW.RAPID"),
	access => $ws_client_config_file
    );
    $rest_api = GW::RAPID->new( undef, undef, undef, undef, $rest_api_requestor, \%rest_api_options );
    if ( not defined $rest_api ) {
	## The GW::RAPID constructor doesn't directly return any information to the caller on the reason for
	## a failure.  But it will already have used the logger handle to write such detail into the logfile.
	log_timed_message "ERROR:  Could not create a GW::RAPID object.";
	return 0;
    }

    return 1;
}

sub terminate_rest_api {
    ## Release our handle to the REST API (if we used it), to force the REST API to call its destructor.
    ## This will attempt to log out before Perl's global destruction pass wipes out resources needed for
    ## logout to work properly.
    $rest_api = undef;
}

sub initialize_feeder {
    my $socket;
    my $failed = 1;

    if ($use_rest_api) {
	if (initialize_rest_api()) {
	    $failed = 0;
	}
	if ($failed) {
	    log_timed_message "ERROR:  Cannot connect to the Foundation REST API. Retrying in $failure_sleep_time seconds.";
	    sleep $failure_sleep_time;
	    return RESTART_STATUS;
	}
    }
    else {
	if ( $socket = IO::Socket::INET->new( PeerAddr => $remote_host, PeerPort => $remote_port, Proto => 'tcp', Type => SOCK_STREAM ) ) {
	    $socket->autoflush();
	    log_timed_message "Start message local port: ", $socket->sockport() if $debug_summary;
	    $failed = 0;
	    unless ( $socket->sockopt(SO_SNDTIMEO, pack('L!L!', $socket_send_timeout, 0)) ) {
		log_socket_problem ('setting send timeout on');
		$failed = 1;
	    }
	    unless ($failed) {
		unless ( $socket->print ($start_message) ) {
		    log_socket_problem ('writing to');
		    $failed = 1;
		}
		else {
		    LOG->print ($start_message, "\n") if $debug_output;
		}
	    }
	    unless ($failed) {
		unless ( $socket->print ($command_close) ) {
		    log_socket_problem ('writing to');
		    $failed = 1;
		}
		else {
		    LOG->print ($command_close, "\n") if $debug_output;
		}
	    }
	    unless ( close($socket) ) {
		log_socket_problem ('closing');
		$failed = 1;
	    }
	}
	if ($failed) {
	    log_timed_message "Listener socket not available. Retrying in $failure_sleep_time seconds.";
	    sleep $failure_sleep_time;
	    return RESTART_STATUS;
	}
	log_timed_message 'Listener socket successfully opened.';
    }

    # Note GWMON-9076: the "monarch" database is the wrong place to look for current configuration data,
    # because it represents a possible future configuration, not the presently running configuration.
    # We are doing so anyway, for the time being.  The main problem comes if this process is bounced
    # for some reason long after a Commit, by which time Monarch might be significantly out of sync
    # with the running configuration, having incorporated a variety of changes which are not yet in
    # production.
    #
    # Get host->IPaddress mapping from Monarch.
    # This script will automatically restart itself when a Commit occurs, thereby
    # re-synchronizing the script's notion of what is currently being monitored.

    my ( $dbname, $dbhost, $dbuser, $dbpass, $dbtype ) = CollageQuery::readGroundworkDBConfig('monarch');
    my $dsn = '';
    if ( defined($dbtype) && $dbtype eq 'postgresql' ) {
	$dsn = "DBI:Pg:dbname=$dbname;host=$dbhost";
    }
    else {
	$dsn = "DBI:mysql:database=$dbname;host=$dbhost";
    }

    my $dbh = DBI->connect( $dsn, $dbuser, $dbpass, { 'AutoCommit' => 1 } );
    if (!$dbh) {
	log_timed_message "Can't connect to database $dbname.\nError: ", $DBI::errstr;
	sleep $failure_sleep_time;
	return ERROR_STATUS;
    }

    my $query = "select name, address from hosts;";
    my $sth   = $dbh->prepare($query);
    if ( !$sth->execute() ) {
	log_timed_message $sth->errstr;
	$sth->finish();
	$dbh->disconnect();
	sleep $failure_sleep_time;
	return ERROR_STATUS;
    }
    while ( my $row = $sth->fetchrow_hashref() ) {
	$hostipaddress{ $$row{name} } = $$row{address};
    }
    $sth->finish();
    $dbh->disconnect();

    return CONTINUE_STATUS;
}

sub terminate_feeder {
    terminate_rest_api() if $use_rest_api;
}

sub perform_feeder_cycle_actions {
    my $SeenCount    = 0;
    my $SkippedCount = 0;
    my $SentCount    = 0;
    my $DroppedCount = 0;
    my $MessageCount = 0;
    my @field;
    my $timestamp;
    my $msgtype;
    my $host;

    if ( !open( LOG_FILE, '<', $eventfile ) ) {
	log_timed_message "Unable to open log file $eventfile: $!";
	sleep $failure_sleep_time;
	return RESTART_STATUS;
    }

    my ( $dev, $ino, $mode, $nlink, $uid, $gid, $rdev, $size, @rest ) = stat(LOG_FILE);
    $eventfile_device = $dev;
    $eventfile_inode  = $ino;
    if ( !defined($eventfile_inode) ) {
	log_timed_message "Unable to stat log file $eventfile: $!";
	close(LOG_FILE);
	sleep $failure_sleep_time;
	return RESTART_STATUS;
    }

    # Try to open the log seek file.  If this open fails, we will default to reading from the beginning
    # of the event file.  We use the (possibly missing) device and inode numbers as well to detect a
    # different event file now in use.  Note that there is an open hole here -- if the event file was
    # rolled since the last time we read it, there may be some unprocessed messages at the end of the
    # old event file that will now never be read and processed.  That is one reason to keep the cycle
    # time in this feeder relatively short, to minimize the number of such missed events.

    # FIX LATER:  If we decide that it is important to try to process such messages, then when we detect
    # that the event file has changed, we would need to scan the /usr/local/groundwork/nagios/var/archives/
    # directory, looking for the same inode number (presuming that Nagios moves the file rather than copying
    # it), then process those trailing messages, then finally switch to reading the new file from the beginning.

    my @last_seek_pos = @seek_pos;

    if ( open( SEEK_FILE, '<', $seekfile ) ) {
	chomp( @seek_pos = <SEEK_FILE> );
	close(SEEK_FILE);

	# Provide default values if the file is empty or doesn't (yet) include the device/inode numbers.
	# (device == -1 && inode == -1) means we assume the same $eventfile file is still in play.
	$seek_pos[0] =  0 if ( !defined($seek_pos[0]) || $seek_pos[0] !~ /\d/ );  # default seek position
	$seek_pos[1] = -1 if ( !defined($seek_pos[1]) || $seek_pos[1] !~ /\d/ );  # default device number
	$seek_pos[2] = -1 if ( !defined($seek_pos[2]) || $seek_pos[2] !~ /\d/ );  # default inode number

	#  If the seek file is empty, there is no need to seek ...
	if ( $seek_pos[0] != 0 ) {
	    ## Compare seek position to actual file size.  If the file size is smaller
	    ## then we just start from the beginning; i.e., the file was rotated, etc.,
	    ## even if it somehow still has the same device and inode numbers.
	    if  (
		($seek_pos[1] == -1 || $seek_pos[1] == $eventfile_device) &&
		($seek_pos[2] == -1 || $seek_pos[2] == $eventfile_inode ) &&
		$seek_pos[0] <= $size
		) {
		seek( LOG_FILE, $seek_pos[0], 0 );
		if ( @xml_messages && $last_seek_pos[1] == $eventfile_device && $last_seek_pos[2] == $eventfile_inode ) {
		    # We retained some messages from the last cycle, so if we fail to send them,
		    # the seek position will need to be rolled back to where they were found.
		    # FIX LATER:  This gets more complicated if the event file was rolled
		    # between the last cycle and this one.  In that case, we really ought to
		    # set the last known good seek position to the old file, which we are not
		    # doing here.  This is another part of the open hole that may result in
		    # some messages at the end of the old event file never being processed.
		    $seek_pos[0] = $last_seek_pos[0];
		}
	    }
	}
    }
    else {
	$seek_pos[0] = 0;
    }
    $seek_pos[1] = $eventfile_device;
    $seek_pos[2] = $eventfile_inode;

    # Operational flag to see if we should update the seek position to reflect the farthest point we have read in
    # the event file.  We only do so if there has been no data to send, or if we were able to send all events found
    # to that point, or if we will pretend we did while in fact we retained some results for the next cycle.
    my $update_seek       = 1;
    my $flush_immediately = 0;

    while ( my $line = <LOG_FILE> ) {
	next if ( $line =~ /^\s*#]/ );
	chomp $line;
	$SeenCount++;

	# Sample Events below
	@field = split /;/, $line;
	if ( $field[0] =~ /\[(\d+)\]\s([\w\s]+):\s(.*)/ ) {
	    $timestamp = $1;
	    $msgtype   = $2;
	    $host      = $3;
	}
	else {    # Parse other formats here if necessary
	    ## log_message 'Skipping line (does not start with timestamp/msgtype/host): ', $line if $debug_input;
	    $SkippedCount++;
	    ## [1252888617] Caught SIGTERM, shutting down...
	    if ( $line =~ /\[(\d+)\] Caught SIG\w+, shutting down/ ) {
		my $nagios_shutdown_time = $1;

		# For all we know, Nagios may have been bounced multiple times while this entire script was down,
		# so if there is no current pause file ($Locks::in_progress_file) in place, we could just sail
		# past this point and continue reading input lines.  On the other hand, perhaps an entire Commit
		# took place while we were in the $cycle_sleep_time, and the pause file is already gone.  In that
		# case, we want to re-synchronize the %hostipaddress mapping if we have been running a long time.
		# So we just wait for an opportune time to quit, then do so.  Thus we will bounce this script every
		# time we see such a message in the $eventfile, which may be more often than strictly necessary,
		# but we're willing to incur that cost in the name of reliability.

		if ($use_rest_api) {
		    if (@create_event_list) {
			$next_sync_timeout = 0;
			if ( not send_queued_events( 'create', \@create_event_list, \$SentCount, \$DroppedCount ) ) {
			    $update_seek = 0;
			}
		    }
		    if (@ack_event_list) {
			$next_sync_timeout = 0;
			if ( not send_queued_events( 'ack', \@ack_event_list, \$SentCount, \$DroppedCount ) ) {
			    $update_seek = 0;
			}
		    }
		    if (@unack_event_list) {
			$next_sync_timeout = 0;
			if ( not send_queued_events( 'unack', \@unack_event_list, \$SentCount, \$DroppedCount ) ) {
			    $update_seek = 0;
			}
		    }
		}
		else {
		    if (@xml_messages) {
			$MessageCount      = @xml_messages;
			$next_sync_timeout = 0;
			if ( send_queued_messages( \@xml_messages ) ) {
			    $SentCount += $MessageCount;
			}
			else {
			    $DroppedCount += $MessageCount;
			    $update_seek = 0;
			}
		    }
		}
		my $update_status = update_seek_file( tell(LOG_FILE), $update_seek );
		close(LOG_FILE);
		log_timed_message "Saw $SeenCount records; skipped $SkippedCount, sent $SentCount, dropped $DroppedCount."
		  if $debug_summary;
		log_timed_message 'Restarting in response to Nagios shutdown at ',
		  scalar localtime $nagios_shutdown_time, '.';
		return ($update_status == ERROR_STATUS) ? ERROR_STATUS : RESTART_STATUS;
	    }
	    next;
	}

	# for building up a single message, in parts
	my @xml_message = ();
	my %one_event   = ();
	my $time_now    = time;

	if (not $use_rest_api) {
	    push @xml_message, "<LogMessage consolidation='NAGIOSEVENT' ";

	    # default identification -- should set to IP address if known
	    push @xml_message, "MonitorServerName=\"$thisnagios\" ";
	}

	# Note:  Values assigned below to ReportDate and LastInsertDate may look like
	# they are swapped from what they ought to be, and that is probably true.
	# This is to accommodate some ugly remapping of such values downstream.

	if ( ( $msgtype =~ /HOST ALERT/ ) and ( $field[2] eq "HARD" ) ) {
	    ## [1110304792] HOST ALERT: peter;UP;HARD;1;PING OK - Packet loss = 0%, RTA = 0.88 ms
	    my $msg = $field[4] // '';
	    $msg =~ s/\n/ /g;
	    $msg =~ s/<br>/ /ig;

	    if ($use_rest_api) {
		my $monitorStatus = $field[1] =~ /^ACKNOWLEDGEMENT/ ? $field[1] : $field[1] =~ /\((.+)\)$/ ? $1 : $field[1];
		$one_event{consolidationName}        = 'NAGIOSEVENT';
		$one_event{monitorServer}            = $thisnagios;
		$one_event{host}                     = $host;
		$one_event{device}                   = $hostipaddress{$host} || $host;
		$one_event{severity}                 = $monitorStatus eq 'DOWN' ? 'CRITICAL' : $monitorStatus eq 'UP' ? 'OK' : $monitorStatus;
		$one_event{monitorStatus}            = $monitorStatus;
		$one_event{textMessage}              = $msg;
		$one_event{reportDate}               = rest_time_text($time_now);
		$one_event{lastInsertDate}           = rest_time_text($timestamp);
		$one_event{properties}{SubComponent} = $host;
		$one_event{properties}{ErrorType}    = 'HOST ALERT';
		queue_action( 'create', \%one_event, \$SentCount, \$DroppedCount );
	    }
	    else {
		$msg =~ s/&/&amp;/g;
		$msg =~ s/"/&quot;/g;
		$msg =~ s/'/&apos;/g;
		$msg =~ s/</&lt;/g;
		$msg =~ s/>/&gt;/g;
		push @xml_message, "Host=\"$host\" ";
		## default identification -- should set to IP address if known
		if ( $hostipaddress{$host} ) {
		    ## set value of Device to IP address
		    push @xml_message, "Device=\"" . $hostipaddress{$host} . "\" ";
		}
		else {
		    ## IP address not available; set value of Device to host name
		    push @xml_message, "Device=\"$host\" ";
		}
		if ( $field[1] eq 'DOWN' ) {
		    push @xml_message, "Severity=\"CRITICAL\" ";
		}
		elsif ( $field[1] eq 'UP' ) {
		    push @xml_message, "Severity=\"OK\" ";
		}
		else {
		    push @xml_message, "Severity=\"$field[1]\" ";
		}
		push @xml_message, "MonitorStatus=\"$field[1]\" ";
		push @xml_message, "TextMessage=\"$msg\" ";
		my $now  = time_text($time_now);
		my $then = time_text($timestamp);
		push @xml_message, "ReportDate=\"$now\" ";
		push @xml_message, "LastInsertDate=\"$then\" ";
		push @xml_message, "SubComponent=\"$host\" ";
		push @xml_message, "ErrorType=\"HOST ALERT\" ";
		push @xml_message, "/>\n";
	    }
	}
	elsif ( ( $msgtype =~ /SERVICE ALERT/ ) and ( $field[3] eq "HARD" ) ) {
	    ## [1110304792] SERVICE ALERT: peter;icmp_ping;OK;HARD;1;PING OK - Packet loss = 0%, RTA = 1.05 ms
	    my $msg = $field[5] // '';
	    $msg =~ s/\n/ /g;
	    $msg =~ s/<br>/ /ig;

	    if ($use_rest_api) {
		my $service = $field[1];
		my $monitorStatus = $field[2] =~ /^ACKNOWLEDGEMENT/ ? $field[2] : $field[2] =~ /\((.+)\)$/ ? $1 : $field[2];
		$one_event{consolidationName}        = 'NAGIOSEVENT';
		$one_event{monitorServer}            = $thisnagios;
		$one_event{host}                     = $host;
		$one_event{device}                   = $hostipaddress{$host} || $host;
		$one_event{service}                  = $service;                        # Invalid field??
		$one_event{severity}                 = $monitorStatus;
		$one_event{monitorStatus}            = $monitorStatus;
		$one_event{textMessage}              = $msg;
		$one_event{reportDate}               = rest_time_text($time_now);
		$one_event{lastInsertDate}           = rest_time_text($timestamp);
		$one_event{properties}{SubComponent} = "$host:$service";
		$one_event{properties}{ErrorType}    = 'SERVICE ALERT';

		# Handle possible attribute exceptions.
		foreach my $pattern ( keys %mapped_services ) {
		    if ( $service =~ m{^$pattern$} ) {
			$one_event{appType} = $mapped_services{$pattern}{application_type}
			  if defined $mapped_services{$pattern}{application_type};
			if ( defined $mapped_services{$pattern}{consolidation_criteria} ) {
			    $one_event{consolidationName} = $mapped_services{$pattern}{consolidation_criteria};
			    ## Historically, we don't include an IP address for Nagios-related events.
			    ## But if attribute mapping is in play for this service, it might refer to some
			    ## consolidation criteria that in turn references this field.  So in that case,
			    ## we supply such information if we have it available.
			    $one_event{properties}{ipaddress} = $hostipaddress{$host} if defined $hostipaddress{$host};
			}
			last;
		    }
		}

		queue_action( 'create', \%one_event, \$SentCount, \$DroppedCount );
	    }
	    else {
		$msg =~ s/&/&amp;/g;
		$msg =~ s/"/&quot;/g;
		$msg =~ s/'/&apos;/g;
		$msg =~ s/</&lt;/g;
		$msg =~ s/>/&gt;/g;
		push @xml_message, "Host=\"$host\" ";
		## default identification -- should set to IP address if known
		if ( $hostipaddress{$host} ) {
		    ## set value of Device to IP address
		    push @xml_message, "Device=\"" . $hostipaddress{$host} . "\" ";
		}
		else {
		    ## no IP address; set to host name
		    push @xml_message, "Device=\"$host\" ";
		}
		push @xml_message, "ServiceDescription=\"$field[1]\" ";    # Invalid field??
		push @xml_message, "Severity=\"$field[2]\" ";
		push @xml_message, "MonitorStatus=\"$field[2]\" ";
		push @xml_message, "TextMessage=\"$msg\" ";
		my $now  = time_text($time_now);
		my $then = time_text($timestamp);
		push @xml_message, "ReportDate=\"$now\" ";
		push @xml_message, "LastInsertDate=\"$then\" ";
		push @xml_message, "SubComponent=\"$host:$field[1]\" ";
		push @xml_message, "ErrorType=\"SERVICE ALERT\" ";
		push @xml_message, "/>\n";
	    }
	}
	elsif ( $msgtype =~ /HOST NOTIFICATION/ && $send_host_notification_events ) {
	    ## [1110304792] HOST NOTIFICATION: nagios;peter;UP;host-notify-by-epager;PING OK - Packet loss = 0%, RTA = 0.88 ms
	    my $msg = $field[4] // '';
	    $msg =~ s/\n/ /g;
	    $msg =~ s/<br>/ /ig;

	    if ($use_rest_api) {
		my $monitorStatus = $field[2] =~ /^ACKNOWLEDGEMENT/ ? $field[2] : $field[2] =~ /\((.+)\)$/ ? $1 : $field[2];
		$one_event{consolidationName}           = 'NAGIOSEVENT';
		$one_event{monitorServer}               = $thisnagios;
		$one_event{host}                        = $field[1];
		$one_event{device}                      = $hostipaddress{ $field[1] } || $field[1];
		$one_event{properties}{LoggerName}      = $host;
		$one_event{severity}                    = $monitorStatus eq 'DOWN' ? 'CRITICAL' : $monitorStatus eq 'UP' ? 'OK' : $monitorStatus;
		$one_event{monitorStatus}               = $monitorStatus;
		$one_event{properties}{ApplicationName} = $field[3];
		$one_event{textMessage}                 = $msg;
		$one_event{reportDate}                  = rest_time_text($time_now);
		$one_event{lastInsertDate}              = rest_time_text($timestamp);
		$one_event{properties}{SubComponent}    = $field[1];
		$one_event{properties}{ErrorType}       = 'HOST NOTIFICATION';
		queue_action( 'create', \%one_event, \$SentCount, \$DroppedCount );
	    }
	    else {
		$msg =~ s/&/&amp;/g;
		$msg =~ s/"/&quot;/g;
		$msg =~ s/'/&apos;/g;
		$msg =~ s/</&lt;/g;
		$msg =~ s/>/&gt;/g;
		push @xml_message, "Host=\"$field[1]\" ";
		if ( $hostipaddress{ $field[1] } ) {
		    ## set value of Device to IP address
		    push @xml_message, "Device=\"" . $hostipaddress{ $field[1] } . "\" ";
		}
		else {
		    ## no IP address; set to host name
		    push @xml_message, "Device=\"$field[1]\" ";
		}
		push @xml_message, "LoggerName=\"$host\" ";
		if ( $field[2] eq 'DOWN' ) {
		    push @xml_message, "Severity=\"CRITICAL\" ";
		}
		elsif ( $field[2] eq 'UP' ) {
		    push @xml_message, "Severity=\"OK\" ";
		}
		else {
		    push @xml_message, "Severity=\"$field[2]\" ";
		}
		push @xml_message, "MonitorStatus=\"$field[2]\" ";
		push @xml_message, "ApplicationName=\"$field[3]\" ";
		push @xml_message, "TextMessage=\"$msg\" ";
		my $now  = time_text($time_now);
		my $then = time_text($timestamp);
		push @xml_message, "ReportDate=\"$now\" ";
		push @xml_message, "LastInsertDate=\"$then\" ";
		push @xml_message, "SubComponent=\"$field[1]\" ";
		push @xml_message, "ErrorType=\"HOST NOTIFICATION\" ";
		push @xml_message, "/>\n";
	    }
	}
	elsif ( $msgtype =~ /SERVICE NOTIFICATION/ && $send_service_notification_events ) {
	    ## [1110304792] SERVICE NOTIFICATION: nagios;peter;check_http;CRITICAL;notify-by-epager;A HREF=http://192.168.2.146:80/ target=_blankConnection refused
	    my $msg = $field[5] // '';
	    $msg =~ s/\n/ /g;
	    $msg =~ s/<br>/ /ig;

	    if ($use_rest_api) {
		my $service = $field[2];
		my $monitorStatus = $field[3] =~ /^ACKNOWLEDGEMENT/ ? $field[3] : $field[3] =~ /\((.+)\)$/ ? $1 : $field[3];
		$one_event{consolidationName}           = 'NAGIOSEVENT';
		$one_event{monitorServer}               = $thisnagios;
		$one_event{host}                        = $field[1];
		$one_event{device}                      = $hostipaddress{ $field[1] } || $field[1];
		$one_event{properties}{LoggerName}      = $host;
		$one_event{service}                     = $service;                                  # invalid field?
		$one_event{severity}                    = $monitorStatus;
		$one_event{monitorStatus}               = $monitorStatus;
		$one_event{properties}{ApplicationName} = $field[4];
		$one_event{textMessage}                 = $msg;
		$one_event{reportDate}                  = rest_time_text($time_now);
		$one_event{lastInsertDate}              = rest_time_text($timestamp);
		$one_event{properties}{SubComponent}    = "$field[1]:$service";
		$one_event{properties}{ErrorType}       = 'SERVICE NOTIFICATION';

		# Handle possible attribute exceptions.
		foreach my $pattern ( keys %mapped_services ) {
		    if ( $service =~ m{^$pattern$} ) {
			$one_event{appType} = $mapped_services{$pattern}{application_type}
			  if defined $mapped_services{$pattern}{application_type};
			if ( defined $mapped_services{$pattern}{consolidation_criteria} ) {
			    $one_event{consolidationName} = $mapped_services{$pattern}{consolidation_criteria};
			    ## Historically, we don't include an IP address for Nagios-related events.
			    ## But if attribute mapping is in play for this service, it might refer to some
			    ## consolidation criteria that in turn references this field.  So in that case,
			    ## we supply such information if we have it available.
			    $one_event{properties}{ipaddress} = $hostipaddress{$host} if defined $hostipaddress{$host};
			}
			last;
		    }
		}

		queue_action( 'create', \%one_event, \$SentCount, \$DroppedCount );
	    }
	    else {
		$msg =~ s/&/&amp;/g;
		$msg =~ s/"/&quot;/g;
		$msg =~ s/'/&apos;/g;
		$msg =~ s/</&lt;/g;
		$msg =~ s/>/&gt;/g;
		push @xml_message, "Host=\"$field[1]\" ";
		if ( $hostipaddress{ $field[1] } ) {
		    ## set value of Device to IP address
		    push @xml_message, "Device=\"" . $hostipaddress{ $field[1] } . "\" ";
		}
		else {
		    ## no IP address; set to host name
		    push @xml_message, "Device=\"$field[1]\" ";
		}
		push @xml_message, "LoggerName=\"$host\" ";
		push @xml_message, "ServiceDescription=\"$field[2]\" ";    # invalid field?
		push @xml_message, "Severity=\"$field[3]\" ";
		push @xml_message, "MonitorStatus=\"$field[3]\" ";
		push @xml_message, "ApplicationName=\"$field[4]\" ";
		push @xml_message, "TextMessage=\"$msg\" ";
		my $now  = time_text($time_now);
		my $then = time_text($timestamp);
		push @xml_message, "ReportDate=\"$now\" ";
		push @xml_message, "LastInsertDate=\"$then\" ";
		push @xml_message, "SubComponent=\"$field[1]:$field[2]\" ";
		push @xml_message, "ErrorType=\"SERVICE NOTIFICATION\" ";
		push @xml_message, "/>\n";
	    }
	}
	elsif ( ( $msgtype =~ /EXTERNAL COMMAND/ ) and ( $host =~ /ACKNOWLEDGE_HOST_PROBLEM/ ) ) {
	    ## Host field not host for this msg type
	    ## [1153242440] EXTERNAL COMMAND: ACKNOWLEDGE_HOST_PROBLEM;localhost;1;1;1;joe;This is a test
	    ## <NAGIOS_LOG ApplicationType="NAGIOS" Host="MyHost" ServiceDescription="CPU_CHECK"
	    ##     AcknowledgedBy="you_or_me" AcknowledgeComment="Here we go why" TypeRule"ACKNOWLEDGE" />
	    ## ServiceDescription,  AcknowledgeComment are optional
	    ## If TypeRule is UNACKNOWLEDGE AcknowledgedBy and AcknowledgeComment will be cleared.
	    my $msg = $field[6] // '';
	    $msg =~ s/\n/ /g;
	    $msg =~ s/<br>/ /ig;

	    if ($use_rest_api) {
		$one_event{appType}            = 'NAGIOS';
		$one_event{host}               = $field[1];
		$one_event{acknowledgedBy}     = $field[5];
		$one_event{acknowledgeComment} = $msg;
		queue_action( 'ack', \%one_event, \$SentCount, \$DroppedCount );
	    }
	    else {
		$msg =~ s/&/&amp;/g;
		$msg =~ s/"/&quot;/g;
		$msg =~ s/'/&apos;/g;
		$msg =~ s/</&lt;/g;
		$msg =~ s/>/&gt;/g;
		$msgtype = "ACKNOWLEDGE_HOST_PROBLEM";
		my @ack_message = ();
		push @ack_message, "<NAGIOS_LOG ApplicationType='NAGIOS' ";
		push @ack_message, "Host=\"$field[1]\" ";
		push @ack_message, "AcknowledgedBy=\"$field[5]\" ";
		push @ack_message, "AcknowledgeComment=\"$msg\" ";
		push @ack_message, "TypeRule=\"ACKNOWLEDGE\" ";
		push @ack_message, "/>\n";
		@xml_message = ();
		push @xml_message, join( '', @ack_message );
		$flush_immediately = 1;
	    }
	}
	elsif ( ( $msgtype =~ /EXTERNAL COMMAND/ ) and ( $host =~ /ACKNOWLEDGE_SVC_PROBLEM/ ) ) {
	    ## Host field not host for this msg type
	    ## [1153242140] EXTERNAL COMMAND: ACKNOWLEDGE_SVC_PROBLEM;localhost;Current Users;1;1;1;joe;This is a test acknowledge.
	    ## <NAGIOS_LOG ApplicationType="NAGIOS" Host="MyHost" ServiceDescription="CPU_CHECK"
	    ##     AcknowledgedBy="you_or_me" AcknowledgeComment="Here we go why" TypeRule"ACKNOWLEDGE" />
	    ## ServiceDescription,  AcknowledgeComment are optional
	    ## If TypeRule is UNACKNOWLEDGE AcknowledgedBy and AcknowledgeComment will be cleared.
	    my $msg = $field[7] // '';
	    $msg =~ s/\n/ /g;
	    $msg =~ s/<br>/ /ig;

	    if ($use_rest_api) {
		my $service = $field[2];
		$one_event{appType}            = 'NAGIOS';
		$one_event{host}               = $field[1];
		$one_event{service}            = $service;
		$one_event{acknowledgedBy}     = $field[6];
		$one_event{acknowledgeComment} = $msg;

		# Handle possible attribute exceptions.
		foreach my $pattern ( keys %mapped_services ) {
		    if ( $service =~ m{^$pattern$} ) {
			$one_event{appType} = $mapped_services{$pattern}{application_type}
			  if defined $mapped_services{$pattern}{application_type};
			## Historically, we don't supply a consolidation criteria for these types of events,
			## probably because they are singular in nature.  So we take no action as well in
			## that regard, in the case of an attribute-mapping exception.
			last;
		    }
		}

		queue_action( 'ack', \%one_event, \$SentCount, \$DroppedCount );
	    }
	    else {
		$msg =~ s/&/&amp;/g;
		$msg =~ s/"/&quot;/g;
		$msg =~ s/'/&apos;/g;
		$msg =~ s/</&lt;/g;
		$msg =~ s/>/&gt;/g;
		$msgtype = "ACKNOWLEDGE_SVC_PROBLEM";
		my @ack_message = ();
		push @ack_message, "<NAGIOS_LOG ApplicationType='NAGIOS' ";
		push @ack_message, "Host=\"$field[1]\" ";
		push @ack_message, "ServiceDescription=\"$field[2]\" ";
		push @ack_message, "AcknowledgedBy=\"$field[6]\" ";
		push @ack_message, "AcknowledgeComment=\"$msg\" ";
		push @ack_message, "TypeRule=\"ACKNOWLEDGE\" ";
		push @ack_message, "/>\n";
		@xml_message = ();
		push @xml_message, join( '', @ack_message );
		$flush_immediately = 1;
	    }
	}
	elsif ( ( $msgtype =~ /EXTERNAL COMMAND/ ) and ( $host =~ /REMOVE_HOST_ACKNOWLEDGEMENT/ ) ) {
	    ## Host field not host for this msg type
	    ## [1153257740] EXTERNAL COMMAND: REMOVE_HOST_ACKNOWLEDGEMENT;localhost
	    ## <NAGIOS_LOG ApplicationType="NAGIOS" Host="MyHost" ServiceDescription="CPU_CHECK"
	    ##     AcknowledgedBy="you_or_me" AcknowledgeComment="Here we go why" TypeRule"ACKNOWLEDGE" />
	    ## ServiceDescription,  AcknowledgeComment are optional
	    ## If TypeRule is UNACKNOWLEDGE AcknowledgedBy and AcknowledgeComment will be cleared.
	    if ($use_rest_api) {
		$one_event{appType} = 'NAGIOS';
		$one_event{host}    = $field[1];
		queue_action( 'unack', \%one_event, \$SentCount, \$DroppedCount );
	    }
	    else {
		$msgtype = "REMOVE_HOST_ACKNOWLEDGEMENT";
		my @ack_message = ();
		push @ack_message, "<NAGIOS_LOG ApplicationType='NAGIOS' ";
		push @ack_message, "Host=\"$field[1]\" ";
		push @ack_message, "TypeRule=\"UNACKNOWLEDGE\" ";
		push @ack_message, "/>\n";
		@xml_message = ();
		push @xml_message, join( '', @ack_message );
		$flush_immediately = 1;
	    }
	}
	elsif ( ( $msgtype =~ /EXTERNAL COMMAND/ ) and ( $host =~ /REMOVE_SVC_ACKNOWLEDGEMENT/ ) ) {
	    ## Host field not host for this msg type
	    ## [1153258340] EXTERNAL COMMAND: REMOVE_SVC_ACKNOWLEDGEMENT;localhost;Current Load
	    ## <NAGIOS_LOG ApplicationType="NAGIOS" Host="MyHost" ServiceDescription="CPU_CHECK"
	    ##     AcknowledgedBy="you_or_me" AcknowledgeComment="Here we go why" TypeRule"ACKNOWLEDGE" />
	    ## ServiceDescription,  AcknowledgeComment are optional
	    ## If TypeRule is UNACKNOWLEDGE AcknowledgedBy and AcknowledgeComment will be cleared.
	    if ($use_rest_api) {
		my $service = $field[2];
		$one_event{appType} = 'NAGIOS';
		$one_event{host}    = $field[1];
		$one_event{service} = $service;

		# Handle possible attribute exceptions.
		foreach my $pattern ( keys %mapped_services ) {
		    if ( $service =~ m{^$pattern$} ) {
			$one_event{appType} = $mapped_services{$pattern}{application_type}
			  if defined $mapped_services{$pattern}{application_type};
			## Historically, we don't supply a consolidation criteria for these types of events,
			## probably because they are singular in nature.  So we take no action as well in
			## that regard, in the case of an attribute-mapping exception.
			last;
		    }
		}

		queue_action( 'unack', \%one_event, \$SentCount, \$DroppedCount );
	    }
	    else {
		$msgtype = "REMOVE_SVC_ACKNOWLEDGEMENT";
		my @ack_message = ();
		push @ack_message, "<NAGIOS_LOG ApplicationType='NAGIOS' ";
		push @ack_message, "Host=\"$field[1]\" ";
		push @ack_message, "ServiceDescription=\"$field[2]\" ";
		push @ack_message, "TypeRule=\"UNACKNOWLEDGE\" ";
		push @ack_message, "/>\n";
		@xml_message = ();
		push @xml_message, join( '', @ack_message );
		$flush_immediately = 1;
	    }
	}
	else {
	    ## log_message 'Skipping line (not a message of concern): ', $line if $debug_input;
	    $SkippedCount++;
	    next;
	}
	log_message 'Line: ', $line if $debug_input;

	if ($use_rest_api) {
	    if ( @create_event_list == 0 && @ack_event_list == 0 && @unack_event_list == 0 ) {
		$next_sync_timeout = $time_now + $sync_timeout_seconds;
	    }
	    if ( @create_event_list && ( @create_event_list >= $rest_event_bundle_size || $time_now >= $next_sync_timeout ) ) {
		$message_counter = output_events_to_rest_api( 'create', \@create_event_list, $message_counter, \$SentCount, \$DroppedCount );
		@create_event_list = ();
		if ( $message_counter >= 0 ) {
		    ## Save the current known good seek position for use in case we set $update_seek to
		    ## zero on a later iteration within this processing cycle, and then need to abort the
		    ## processing cycle and set up for the next cycle to re-process unsent messages.  This
		    ## will also be used if we retain some results across cycles, then receive a shutdown
		    ## request between cycles and attempt to flush them to Foundation, and that flushing
		    ## fails.  In that case, we will need to roll back the seek file, since it will have
		    ## already been updated to a state presuming the messages would be successfully sent.
		    $seek_pos[0] = tell(LOG_FILE);
		}
		else {
		    $update_seek = 0;
		    last;
		}
	    }
	    if ( @ack_event_list && ( @ack_event_list >= $rest_event_bundle_size || $time_now >= $next_sync_timeout ) ) {
		$message_counter = output_events_to_rest_api( 'ack', \@ack_event_list, $message_counter, \$SentCount, \$DroppedCount );
		@ack_event_list = ();
		if ( $message_counter >= 0 ) {
		    ## Save the current known good seek position for use in case we set $update_seek to
		    ## zero on a later iteration within this processing cycle, and then need to abort the
		    ## processing cycle and set up for the next cycle to re-process unsent messages.  This
		    ## will also be used if we retain some results across cycles, then receive a shutdown
		    ## request between cycles and attempt to flush them to Foundation, and that flushing
		    ## fails.  In that case, we will need to roll back the seek file, since it will have
		    ## already been updated to a state presuming the messages would be successfully sent.
		    $seek_pos[0] = tell(LOG_FILE);
		}
		else {
		    $update_seek = 0;
		    last;
		}
	    }
	    if ( @unack_event_list && ( @unack_event_list >= $rest_event_bundle_size || $time_now >= $next_sync_timeout ) ) {
		$message_counter = output_events_to_rest_api( 'unack', \@unack_event_list, $message_counter, \$SentCount, \$DroppedCount );
		@unack_event_list = ();
		if ( $message_counter >= 0 ) {
		    ## Save the current known good seek position for use in case we set $update_seek to
		    ## zero on a later iteration within this processing cycle, and then need to abort the
		    ## processing cycle and set up for the next cycle to re-process unsent messages.  This
		    ## will also be used if we retain some results across cycles, then receive a shutdown
		    ## request between cycles and attempt to flush them to Foundation, and that flushing
		    ## fails.  In that case, we will need to roll back the seek file, since it will have
		    ## already been updated to a state presuming the messages would be successfully sent.
		    $seek_pos[0] = tell(LOG_FILE);
		}
		else {
		    $update_seek = 0;
		    last;
		}
	    }
	}
	else {
	    if ( @xml_messages == 0 ) {
		$next_sync_timeout = $time_now + $sync_timeout_seconds;
	    }
	    push @xml_messages, join( '', @xml_message );
	    if ( $flush_immediately || @xml_messages >= $xml_bundle_size || $time_now >= $next_sync_timeout ) {
		$MessageCount    = @xml_messages;
		$message_counter = output_bundle_to_socket( \@xml_messages, $message_counter );
		@xml_messages    = ();
		if ( $message_counter >= 0 ) {
		    $flush_immediately = 0;
		    ## Save the current known good seek position for use in case we set $update_seek to
		    ## zero on a later iteration within this processing cycle, and then need to abort the
		    ## processing cycle and set up for the next cycle to re-process unsent messages.  This
		    ## will also be used if we retain some results across cycles, then receive a shutdown
		    ## request between cycles and attempt to flush them to Foundation, and that flushing
		    ## fails.  In that case, we will need to roll back the seek file, since it will have
		    ## already been updated to a state presuming the messages would be successfully sent.
		    $seek_pos[0] = tell(LOG_FILE);
		    $SentCount += $MessageCount;
		}
		else {
		    $DroppedCount += $MessageCount;
		    $update_seek = 0;
		    last;
		}
	    }
	}

	if ($shutdown_requested) {
	    $next_sync_timeout = 0;
	    last;
	}
    }

    if ($use_rest_api) {
	if (@create_event_list) {
	    $next_sync_timeout = 0 if $flush_rest_bundle_each_cycle;
	    if ( not send_queued_events( 'create', \@create_event_list, \$SentCount, \$DroppedCount ) ) {
		$update_seek = 0;
	    }
	}
	## We force any queued acks and unacks to be flushed immediately during this cycle,
	## instead of possibly holding them over to the next cycle.
	if (@ack_event_list) {
	    $next_sync_timeout = 0;
	    if ( not send_queued_events( 'ack', \@ack_event_list, \$SentCount, \$DroppedCount ) ) {
		$update_seek = 0;
	    }
	}
	if (@unack_event_list) {
	    $next_sync_timeout = 0;
	    if ( not send_queued_events( 'unack', \@unack_event_list, \$SentCount, \$DroppedCount ) ) {
		$update_seek = 0;
	    }
	}
    }
    else {
	if (@xml_messages) {
	    $MessageCount = @xml_messages;
	    $next_sync_timeout = 0 if $flush_xml_bundle_each_cycle;
	    if ( send_queued_messages( \@xml_messages ) ) {
		## Maybe we retained some messages instead of sending them,
		## which still counts as a "successful" send.
		if ( @xml_messages == 0 ) {
		    $SentCount += $MessageCount;
		}
	    }
	    else {
		$DroppedCount += $MessageCount;
		$update_seek = 0;
	    }
	}
    }

    # If we are retaining any results until the next cycle, the seek file will be updated
    # here presuming that those results will be successfully sent.  That presumption will
    # later need to be reversed if sending those results ultimately fails.
    my $update_status = update_seek_file( tell(LOG_FILE), $update_seek );

    # Close the log file, to allow for Nagios rotating it when it bounces.
    # But of course this means we might actually be opening a different file in the next
    # iteration of the loop, so our seek-position handling has to allow for that.
    close(LOG_FILE);

    log_timed_message "Saw $SeenCount records; skipped $SkippedCount, sent $SentCount, dropped $DroppedCount." if $debug_summary;
    return ($update_status == ERROR_STATUS) ? ERROR_STATUS : CONTINUE_STATUS;
}

sub update_seek_file {
    my $file_pos   = shift;
    my $update_pos = shift;

    # All events presently in the file are now read, or we otherwise cannot proceed.
    # Overwrite the log seek file and print the byte position we have seeked to,
    # unless we were not able to write to the socket for some reason.
    if ( !open( SEEK_FILE, '>', $seekfile ) ) {
	log_timed_message "Unable to open seek position file $seekfile: $!";
	sleep $failure_sleep_time;
	return ERROR_STATUS;
    }
    if ($update_pos) {
	## We wrote to Foundation, or want to pretend we did, so update the seek position.
	log_timed_message "Writing to seek position file $seekfile -- $file_pos" if $debug_output;
	print SEEK_FILE "$file_pos\n$eventfile_device\n$eventfile_inode\n";
    }
    else {
	## We could not write to Foundation.  Save the last known-good seek position.
	log_timed_message "Write to Foundation failed.  Keeping last good seek position -- $seek_pos[0]";
	print SEEK_FILE "$seek_pos[0]\n$eventfile_device\n$eventfile_inode\n";
    }
    close(SEEK_FILE);
    return CONTINUE_STATUS;
}

# The REST API uses different URLs to process different event actions (create, ack, unack).  And
# the GW::RAPID package keeps these same distinctions because the objects you pass are different in
# each case.  So we need to queue these different actions in separate queues, to keep track of which
# actions get sent to which API call.
#
# That leaves us in a potential quandry, depending on how we manage those queues.  If we accumulate
# and flush the queues independently, there is a reasonable chance that the outgoing data may be
# sent out-of-order with respect to the data file in the input stream.  And that might possibly
# result in acking or unacking the wrong set of events.  For instance, if we get an incoming
# create-ack sequence and this gets translated to an outgoing ack-create sequence, the ack might not
# act on the newly created event at all!
#
# On the other hand, we don't want to send every event action as a separate call, since we want to
# be efficient and amortize call overhead over many actions as much as possible.  So here's what we
# do instead.  We keep track of the type of actions that have been queued so far.  If we get in some
# new action and it is of the same type as the last one, we simply append to that queue (and flush
# if the queue gets too big).  If we get in some new action and it is of a type different from the
# last one, then we flush the queue of prior queued actions, then queue the new action (on its own
# action-type-specific queue).

# FIX MAJOR:  There is little error checking in the chain of routines starting from here,
# other than recording sent and dropped events.  Should we bubble up some status to tell
# whether the seek pointer should be updated?
sub queue_action {
    my $action        = shift;    # 'create'|'ack'|'unack'
    my $payload       = shift;    # details, details
    my $sent_count    = shift;
    my $dropped_count = shift;

    # We order these by most common case first.
    if ( $action eq 'create' ) {
	flush_acks( $sent_count, $dropped_count ) if @ack_event_list;
	flush_unacks( $sent_count, $dropped_count ) if @unack_event_list;
	push @create_event_list, $payload;
    }
    elsif ( $action eq 'ack' ) {
	flush_creates( $sent_count, $dropped_count ) if @create_event_list;
	flush_unacks( $sent_count, $dropped_count ) if @unack_event_list;
	push @ack_event_list, $payload;
    }
    elsif ( $action eq 'unack' ) {
	flush_creates( $sent_count, $dropped_count ) if @create_event_list;
	flush_acks( $sent_count, $dropped_count ) if @ack_event_list;
	push @unack_event_list, $payload;
    }
}

sub flush_creates {
    my $sent_count    = shift;
    my $dropped_count = shift;

    ## Note that $message_counter may well be -1 at this point.
    $message_counter = output_events_to_rest_api( 'create', \@create_event_list, $message_counter, $sent_count, $dropped_count );
    @create_event_list = ();
}

sub flush_acks {
    my $sent_count    = shift;
    my $dropped_count = shift;

    $message_counter = output_events_to_rest_api( 'ack', \@ack_event_list, $message_counter, $sent_count, $dropped_count );
    @ack_event_list = ();
}

sub flush_unacks {
    my $sent_count    = shift;
    my $dropped_count = shift;

    $message_counter = output_events_to_rest_api( 'unack', \@unack_event_list, $message_counter, $sent_count, $dropped_count );
    @unack_event_list = ();
}

sub send_queued_messages {
    my $msg_ref = shift;
    my $failed  = 0;

    if ( @$msg_ref > 0 && time >= $next_sync_timeout ) {
	$message_counter = output_bundle_to_socket( $msg_ref, $message_counter );
	$failed = ($message_counter < 0);
	@$msg_ref = ();
    }

    return !$failed;
}

sub send_queued_events {
    my $events_type   = shift;
    my $msg_ref       = shift;
    my $sent_count    = shift;
    my $dropped_count = shift;
    my $failed        = 0;

    if ( @$msg_ref > 0 && time >= $next_sync_timeout ) {
	$message_counter = output_events_to_rest_api( $events_type, $msg_ref, $message_counter, $sent_count, $dropped_count );
	$failed = ($message_counter < 0);
	@$msg_ref = ();
    }

    return !$failed;
}

sub output_events_to_rest_api {
    my $events_type   = shift;
    my $events_ref    = shift;
    my $series_num    = shift;
    my $sent_count    = shift;
    my $dropped_count = shift;
    my $failed        = 0;
    my %outcome       = ();
    my @results       = ();

    my $routine    = $events_type . '_events';
    my $next       = 0;
    my $last       = -1;
    my $last_index = $#$events_ref;
    while ( $next <= $last_index ) {
	$last = $next + $max_rest_event_bundle_size - 1;
	$last = $last_index if $last > $last_index;
	$series_num++;

	# FIX MINOR:  Verify that the construction below yields a reference to the
	# desired array slice, and that there is not some simpler way to create such
	# a reference.

	## FIX MINOR:  Either of these formulations works.  The question is, which is more efficient?
	## If possible, we'd like to reference elements in the array slice directly, without copying.
	## Also look at Data::Alias to see if that might improve performance.
	# if ( not $rest_api->$routine( [ @{$events_ref}[ $next .. $last ] ], {}, \%outcome, \@results) ) {
	#     ...
	# }
	##
	## FIX LATER:  We probably want to use the options to set an explicit timeout on this call.
	if ( $rest_api->$routine( sub{\@_}->( @{$events_ref}[ $next .. $last ] ), {}, \%outcome, \@results) ) {
	    $$sent_count += $outcome{successful} + $outcome{warning};
	}
	else {
	    ## FIX MINOR:  Check the @results thoroughly.  Maybe even retry any failed sends, one time.
	    ## Note that multiple @results elements might have the same 'entity' key.  That will happen
	    ## whenever Foundation consolidates multiple events into the same event.  So any retries
	    ## would need to be based on the position within @$events_ref corresponding to a failed
	    ## position in @results, not on a returned $results[$n]{entity} value.

	    # Some events may have been successfully actioned, while others may have failed.
	    # (Warnings may have driven us down this code path even if all succeeded.)
	    log_outcome \%outcome, "$routine() call" if $debug_output;
	    log_results \@results, "$routine() call" if $debug_output;
	    if ( defined $outcome{count} ) {
		$$sent_count += $outcome{successful} + $outcome{warning};
		$$dropped_count += $outcome{failed};
		if ( $outcome{failed} ) {
		    my $i = $next;
		    foreach my $result (@results) {
			log_timed_message( ( $result->{status} eq 'failure' ? 'ERROR' : 'WARNING' )
			    . ":  $result->{status} in $routine() for host "
			    . $events_ref->[$i]{host}
			    . ( $events_ref->[$i]{service} ? " service $events_ref->[$i]{service}" : '' ) . ': '
			    . ( $result->{message} || '(reason unknown)' ) )
			  if $result->{status} ne 'success';
			++$i;
		    }
		}
	    }
	    else {
		$$dropped_count += $last - $next + 1;
	    }
	    if ( $outcome{failed} or ( $outcome{response_code} || 200 ) != 200 ) {
		log_timed_message( "ERROR:  Could not $events_type events in Foundation"
		      . ( $outcome{response_code} ? " $outcome{response_status} (" . $outcome{response_error} . ').' : '.' ) );
	    }

	    # Logically, we would want almost any failure to cause the intended action to be retried,
	    # which will happen if we set $failed=1 here:
	    #
	    #     if ( $outcome{failed} or ( $outcome{response_code} || 200 ) != 200 ) { $failed = 1; last; }
	    #
	    # But we don't want customer systems to be infinitely stuck without recourse if
	    # something unexpected happens.  So we limit forcing retries to the situation where we
	    # could not contact the server, or there is some fundamental internal server error.
	    # That is clearly a situation that unconditionally demands retries, and it is expected
	    # such a situation might arise during server startup/shutdown periods.  That should
	    # generate a 500-series error, so that is what we test for here.
	    if ( defined( $outcome{response_code} ) and $outcome{response_code} % 100 == 5 ) {
		$failed = 1;
		last;
	    }
	}
	$next = $last + 1;

	# Theoretically, we ought to update $seek_pos[0] now to account for the messages just
	# successfully sent, in case we have a failure on a subsequent iteration of this loop.
	# But currently we are not tracking the seek position on a per-message basis, so we
	# (as yet) have no way to capture that seek position here.
    }
    return $failed ? -1 : $series_num;
}

sub output_bundle_to_socket {
    my $msg_ref    = shift;
    my $series_num = shift;
    my $socket;
    my $failed = 1;

    # FIX FUTURE:  Here and for all the other sockets in this script, we want to implement a
    # connect timeout, possibly by using the new() Timeout parameter.  But the documentation
    # is terribly ambiguous about the actual effect of that setting, so careful testing is
    # required to verify that it would have the desired effect.
    #
    # SendBuf is an as-yet-undocumented patch to IO::Socket::INET.
    my @socket_args = ( PeerAddr => $remote_host, PeerPort => $remote_port, Proto => 'tcp', Type => SOCK_STREAM );
    push @socket_args, ( SendBuf => $send_buffer_size ) if ($send_buffer_size > 0);
    unless ( $socket = IO::Socket::INET->new( @socket_args ) ) {
	log_timed_message "Couldn't connect to $remote_host:$remote_port : $!";
    }
    else {
	$socket->autoflush();
	log_timed_message "Bundle message local port: ", $socket->sockport() if $debug_summary;
	$failed = 0;

	# Here we set a send timeout.  The right value is subject to discussion, given that it may depend
	# on the current load of the receiver process.  Compare this send timout with the receiver timeout,
	# which is set as thread.timeout.idle in /usr/local/groundwork/config/foundation.properties .
	unless ( $socket->sockopt(SO_SNDTIMEO, pack('L!L!', $socket_send_timeout, 0)) ) {
	    log_socket_problem ('setting send timeout on');
	    $failed = 1;
	}

	unless ($failed) {
	    if (1) {
		## Efficient operation, except that the underlying PerlIO buffering layer will
		## break up our individual write actions here into actual max-4096-byte write()
		## calls, thereby preventing the efficiency gains we aim for here.  We have
		## found no way to set the Perl buffering and write() sizes to a larger value.
		my $next          = 0;
		my $last          = -1;
		my $last_index    = $#$msg_ref;
		my $element_begin = undef;
		my $element_end   = "</Command>\n</Adapter>";
		my $elements;
		while ( $next <= $last_index ) {
		    $last = $next + $max_xml_bundle_size - 1;
		    $last = $last_index if $last > $last_index;
		    $series_num++;
		    $element_begin =
		      qq(<Adapter Session="$series_num" AdapterType="SystemAdmin">\n<Command ApplicationType='NAGIOS' Action='ADD'>\n);
		    $elements = join( '', $element_begin, @{$msg_ref}[ $next .. $last ], $element_end );
		    LOG->print ($elements, "\n") if $debug_output && !$log_as_utf8;
		    utf8::encode($elements);
		    log_timed_message "Writing Adapter message (Session $series_num) to Foundation: ", length($elements), " bytes." if $debug_basic;
		    unless ( $socket->print ($elements) ) {
			log_socket_problem ('writing to');
			$failed = 1;
			last;
		    }
		    LOG->print ($elements, "\n") if $debug_output && $log_as_utf8;
		    $next = $last + 1;
		    # Theoretically, we ought to update $seek_pos[0] now to account for the messages just
		    # successfully sent, in case we have a failure on a subsequent iteration of this loop.
		    # But currently we are not tracking the seek position on a per-message basis, so we
		    # (as yet) have no way to capture that seek position here.
		}
	    }
	    else {
		## Legacy operation, now deprecated.
		my $element_begin = undef;
		my $element_end   = "</Command>\n</Adapter>";
		while (@{$msg_ref}) {
		    $series_num++;
		    $element_begin =
		      qq(<Adapter Session="$series_num" AdapterType="SystemAdmin">\n<Command ApplicationType='NAGIOS' Action='ADD'>\n);
		    log_timed_message 'Writing Adapter begin message to Foundation.' if $debug_basic;
		    unless ( $socket->print ($element_begin) ) {
			log_socket_problem ('writing to');
			$failed = 1;
			last;
		    }
		    LOG->print ($element_begin, "\n") if $debug_output;
		    my $num_msgs_output = 0;
		    while ( @{$msg_ref} && $num_msgs_output < $max_xml_bundle_size ) {
			$num_msgs_output++;
			my $message = shift( @{$msg_ref} );
			LOG->print ($message, "\n") if $debug_output && !$log_as_utf8;
			utf8::encode($message);
			log_timed_message 'Writing Adapter body message to Foundation.' if $debug_basic;
			unless ( $socket->print ($message) ) {
			    log_socket_problem ('writing to');
			    $failed = 1;
			    last;
			}
			LOG->print ($message, "\n") if $debug_output && $log_as_utf8;
		    }
		    unless ($failed) {
			log_timed_message 'Writing Adapter end message to Foundation.' if $debug_basic;
			unless ( $socket->print ($element_end) ) {
			    log_socket_problem ('writing to');
			    $failed = 1;
			    last;
			}
			LOG->print ($element_end, "\n") if $debug_output;
		    }
		}
	    }
	    unless ($failed) {
		unless ( $socket->print ($command_close) ) {
		    log_socket_problem ('writing to');
		    $failed = 1;
		}
		else {
		    LOG->print ($command_close, "\n\n") if $debug_output;
		}
	    }
	}

	unless ( close($socket) ) {
	    log_socket_problem ('closing');
	    $failed = 1;
	}
    }
    return $failed ? -1 : $series_num;
}

# This routine is no longer called from anywhere.
sub output_message_to_socket {
    my $message = shift;
    my $socket;
    my $failed = 1;
    unless ( $socket = IO::Socket::INET->new( PeerAddr => $remote_host, PeerPort => $remote_port, Proto => 'tcp', Type => SOCK_STREAM ) ) {
	log_timed_message "Couldn't connect to $remote_host:$remote_port : $!";
    }
    else {
	$socket->autoflush();
	log_timed_message "Single message local port: ", $socket->sockport() if $debug_summary;
	$failed = 0;
	unless ( $socket->sockopt(SO_SNDTIMEO, pack('L!L!', $socket_send_timeout, 0)) ) {
	    log_socket_problem ('setting send timeout on');
	    $failed = 1;
	}
	unless ($failed) {
	    LOG->print ($message, "\n") if $debug_output && !$log_as_utf8;
	    utf8::encode($message);
	    unless ( $socket->print ($message) ) {
		log_socket_problem ('writing to');
		$failed = 1;
	    }
	    else {
		LOG->print ($message, "\n") if $debug_output && $log_as_utf8;
	    }
	}
	unless ($failed) {
	    unless ( $socket->print ($command_close) ) {
		log_socket_problem ('writing to');
		$failed = 1;
	    }
	    else {
		LOG->print ($command_close, "\n\n") if $debug_output;
	    }
	}
	unless ( close($socket) ) {
	    log_socket_problem ('closing');
	    $failed = 1;
	}
    }
    return !$failed;
}
