#!/usr/local/groundwork/perl/bin/perl -w --

# nagios2collage_socket.pl
# Copyright (c) 2004-2010 GroundWork Open Source, Inc.
# info@groundworkopensource.com
#
# This program is free software; you can redistribute it and/or modify
# it under the terms of version 2 of the GNU General Public License
# as published by the Free Software Foundation.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program; if not, write to the Free Software
# Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA

use strict;
use Time::Local;
use vars qw($socket $smart_update);
use IO::Socket;
use Time::HiRes;
use CollageQuery;
use Data::Dumper;
use GDMA::GDMAUtils;
use TypedConfig;

####################################################################
# Configuration Parameters
####################################################################

my $default_config_file = '/usr/local/groundwork/config/status-feeder.properties';

# 0 => minimal, 1 => summary, 2 => basic, 3 => XML messages, 4 => debug level, 5 => ridiculous level.
my $debug_level = undef;

my $logfile = undef;    # Where the log file is to be written.

my $thisnagios       = undef;    # Identifier for this instance of Nagios; should generally be `hostname -s`.
my $nagios_version   = undef;    # Major version only (e.g., 3).
my $statusfile       = undef;    # Absolute pathname of the Nagios status file.
my $cycle_sleep_time = undef;    # Wait time in seconds between checks of the Nagios status.log file.

# Time between full updates to the local Foundation, in seconds.  This is the longest you want to wait for updates
# to the LastCheckTime in Foundation.  Set this to a longer time on busy systems.  Suggested 90 second minimum,
# 300 second maximum.  The longer the time, the larger the bundles of updates.  Setting this too long could result
# in a "bumpy" performance curve, as the system processes large bundles.  Old advice:  If you set this near the
# maximum, you might also want to also increase the max_xml_bundle_size below.
my $local_full_update_time = undef;

my $smart_update      = undef;	# If set to 1, then send only state changes and heartbeats.
my $send_sync_warning = undef;	# Send a console message when Nagios and Foundation are out of sync. 0 = no warning, 1 = warning.
my $send_events_for_pending_to_ok = undef;    # Whether to send pending-to-ok transition events, or just skip them.

my $failure_sleep_time   = undef;    # Seconds to sleep before restarting after failure, to prevent tight looping.

my $foundation_host = undef;    # Where to send results to Foundation.
my $foundation_port = undef;    # Where to send results to Foundation.

my $xml_bundle_size      = undef;    # Typical number of messages to send in each bundle.  This is NOT the minimum size ...
my $max_xml_bundle_size  = undef;    # ... but this is the maximum size.  150 seems to work reasonably well in testing.
my $sync_timeout_seconds = undef;    # Soft limit on time for which accumulating messages are held before sending.

# This is the actual SO_SNDBUF value, as set by setsockopt().  This is therefore the actual size of
# the data buffer available for writing, irrespective of additional kernel bookkeeping overhead.
# This will have no effect without the companion as-yet-undocumented patch to IO::Socket::INET.
# Set this to 0 to use the system default socket send buffer size.  A typical value to set here is
# 262144.  (Note that the value specified here is likely to be limited to something like 131071 by
# the sysctl net.core.wmem_max parameter.)
my $send_buffer_size = undef;

# Socket timeout (in seconds), to address GWMON-7407.  Typical value is 60.  Set to 0 to disable.
#
# This timeout is here only for use in emergencies, when Foundation has completely frozen up and is no
# longer reading (will never read) a socket we have open.  We don't want to set this value so low that
# it will interfere with normal communication, even given the fact that Foundation may wait a rather
# long time between sips from this straw as it processes a large bundle of messages that we sent it, or
# is otherwise busy and just cannot get back around to reading the socket in a reasonably short time.
my $socket_send_timeout = undef;

# Maximum number of events to accumulate before sending them all as a bundle.
my $max_event_bundle_size = undef;

# $syncwait is a multiplier of $cycle_sleep_time to wait on updates while Foundation processes a
# sync.  Typical value is 20.  In theory, you might need to increase this if you see deadlocks after
# commit in the framework.log file.  In practice, though, the need for this should have completely
# disappeared now that we have proper synchronization with pre-flight and commit operations in place.
my $syncwait = undef;

# Options for sending state data to parent/standby server(s)

my $send_state_changes_by_nsca = undef;    # Whether to send state changes and heartbeats via direct NSCA (requires primary_parent).

# Valid hostname or IP address, if $send_state_changes_by_nsca is true.
my $primary_parent = undef;

my $send_to_secondary_NSCA = undef;        # 0 => do not send to secondary, 1 => send, in which case you must define secondary_parent.

# Valid hostname or IP address, if $send_state_changes_by_nsca and $send_to_secondary_NSCA are true.
my $secondary_parent = undef;

# Seconds between NSCA heartbeats (approximate; will be at least this, possibly this + $remote_full_update_time).
my $nsca_heartbeat_interval = undef;
my $nsca_full_dump_interval = undef;       # Seconds between NSCA full dumps (approximate).  Set to zero to disable, if desired.

my $nsca_port                  = undef;    # Port the parent (and secondary parent) is listening on (normally 5667).
my $max_messages_per_send_nsca = undef;    # Limit to the size of batched NSCA sends, to avoid overloads (typical value 100).
my $nsca_batch_delay           = undef;    # Sleep this many seconds between sending batches of $max_messages_per_send_nsca results
my $nsca_timeout               = undef;    # Give up on sending a heartbeat if we get no answer from parent after this long.

# Options for sending state data via the GDMA spooler:

my $send_state_changes_by_gdma = undef;    # Whether to send state changes and heartbeats via the GDMA spooler.

my $gdma_heartbeat_interval = undef;       # Seconds between GDMA heartbeats (approximate).
my $gdma_full_dump_interval = undef;       # Seconds between GDMA full dumps (approximate).  Set to zero to disable, if desired.

# Absolute path to the base of the GDMA software installation (typically, "/usr/local/groundwork/gdma").
# This will be used to locate the spool file the status feeder will write into.
my $gdma_install_base = undef;

my $max_unspooled_results_to_save = undef;    # How many unspooled GDMA results to save for another attempt to spool them.

####################################################################
# Working Variables
####################################################################

# Derived flags, for easy testing.
my $debug_summary    = undef;
my $debug_basic      = undef;
my $debug_xml        = undef;
my $debug_debug      = undef;
my $debug_ridiculous = undef;

my $heartbeat_mode           = 0;	# Do not change this setting -- it is controlled by smart_update.
my $last_nsca_heartbeat_time = undef;
my $last_nsca_full_dump_time = undef;
my $last_gdma_heartbeat_time = undef;
my $last_gdma_full_dump_time = undef;

my $heartbeat_high_water_mark    = 100;    # initial size for arrays holding heartbeat states; will be adjusted upward
my $state_change_high_water_mark = 100;    # initial size for arrays holding object state changes; will be adjusted upward

my $next_sync_timeout     = 0;			# used for XML batching
my $message_counter       = 1;
my $last_statusfile_mtime = 0;
my $element_ref           = {};
my $global_nagios	  = {};
my $collage_status_ref    = {};
my $device_ref            = {};
my $host_ref              = {};
my $service_ref           = {};
my $loop_count            = 0;
my $total_wait            = 0;
my @xml_messages          = ();
my @event_messages        = ();
my $n_hostcount           = 0;
my $n_servicecount        = 0;
my $last_n_hostcount      = 0;
my $last_n_servicecount   = 0;
my $f_hostcount           = 0;
my $f_servicecount        = 0;
my $last_f_hostcount      = 0;
my $last_f_servicecount   = 0;
my $enable_feeding        = 1;
my $syncwaitcount         = 0;
my $logtime               = '';
my $sync_at_start         = 0;
my $looping_start_time    = 0;
my $gdma_spool_filename   = undef;
my $gdma_results_to_spool = [];

# These mappings must reflect the corresponding Nagios internal enumerations,
# so we can correctly interpret data from the status file.

# from nagios.h:  HOST_UP, HOST_DOWN, HOST_UNREACHABLE
my %HostStatus = ( 0 => 'UP', 1 => 'DOWN', 2 => 'UNREACHABLE' );

# from nagios.h:  STATE_OK, STATE_WARNING, STATE_CRITICAL, STATE_UNKNOWN
my %ServiceStatus = ( 0 => 'OK', 1 => 'WARNING', 2 => 'CRITICAL', 3 => 'UNKNOWN' );

# from common.h:  HOST_CHECK_ACTIVE and SERVICE_CHECK_ACTIVE, HOST_CHECK_PASSIVE and SERVICE_CHECK_PASSIVE
my %CheckType = ( 0 => 'ACTIVE', 1 => 'PASSIVE' );

# from common.h:  SOFT_STATE, HARD_STATE
my %StateType = ( 0 => 'SOFT', 1 => 'HARD' );

my %hostipaddress = ();

my $start_message =
    "<GENERICLOG consolidation='SYSTEM' ApplicationType='SYSTEM' MonitorServerName='localhost' Device='127.0.0.1' Severity='OK' MonitorStatus='OK' TextMessage='Foundation-Nagios status check process started.' />";
my $command_close = '<SERVICE-MAINTENANCE command="close" />';
my $restart_xml   = '<RESTART />';
my $no_xml        = '';

our $shutdown_requested = 0;

use constant ERROR_STATUS    => 0;
use constant STOP_STATUS     => 1;
use constant RESTART_STATUS  => 2;
use constant CONTINUE_STATUS => 3;

####################################################################
# Program
####################################################################

# Here is the entire substance of this script, in a one-liner:
exit (main() == ERROR_STATUS) ? 1 : 0;

sub read_config_file {
    my $config_file = shift;
    eval {
	my $config = TypedConfig->new ($config_file);

	$debug_level                   = $config->get_number('debug_level');
	$logfile                       = $config->get_scalar('logfile');
	$thisnagios                    = $config->get_scalar('thisnagios');
	$nagios_version                = $config->get_number('nagios_version');
	$statusfile                    = $config->get_scalar('statusfile');
	$cycle_sleep_time              = $config->get_number('cycle_sleep_time');
	$local_full_update_time        = $config->get_number('local_full_update_time');
	$smart_update                  = $config->get_boolean('smart_update');
	$send_sync_warning             = $config->get_boolean('send_sync_warning');
	$send_events_for_pending_to_ok = $config->get_boolean('send_events_for_pending_to_ok');
	$failure_sleep_time            = $config->get_number('failure_sleep_time');
	$foundation_host               = $config->get_scalar('foundation_host');
	$foundation_port               = $config->get_number('foundation_port');
	$xml_bundle_size               = $config->get_number('xml_bundle_size');
	$max_xml_bundle_size           = $config->get_number('max_xml_bundle_size');
	$sync_timeout_seconds          = $config->get_number('sync_timeout_seconds');
	$send_buffer_size              = $config->get_number('send_buffer_size');
	$socket_send_timeout           = $config->get_number('socket_send_timeout');
	$max_event_bundle_size         = $config->get_number('max_event_bundle_size');
	$syncwait                      = $config->get_number('syncwait');
	$send_state_changes_by_nsca    = $config->get_boolean('send_state_changes_by_nsca');
	$primary_parent                = $config->get_scalar('primary_parent');
	$send_to_secondary_NSCA        = $config->get_boolean('send_to_secondary_NSCA');
	$secondary_parent              = $config->get_scalar('secondary_parent');
	$nsca_heartbeat_interval       = $config->get_number('nsca_heartbeat_interval');
	$nsca_full_dump_interval       = $config->get_number('nsca_full_dump_interval');
	$nsca_port                     = $config->get_number('nsca_port');
	$max_messages_per_send_nsca    = $config->get_number('max_messages_per_send_nsca');
	$nsca_batch_delay              = $config->get_number('nsca_batch_delay');
	$nsca_timeout                  = $config->get_number('nsca_timeout');
	$send_state_changes_by_gdma    = $config->get_boolean('send_state_changes_by_gdma');
	$gdma_heartbeat_interval       = $config->get_number('gdma_heartbeat_interval');
	$gdma_full_dump_interval       = $config->get_number('gdma_full_dump_interval');
	$gdma_install_base             = $config->get_scalar('gdma_install_base');
	$max_unspooled_results_to_save = $config->get_number('max_unspooled_results_to_save');

	# FIX LATER:  range-validate many of the values we obtained from the config file

	if ($send_state_changes_by_nsca) {
	    if ($primary_parent eq '') {
		die "primary_parent must be non-empty if send_state_changes_by_nsca is true\n";
	    }
	    if ($send_to_secondary_NSCA && $secondary_parent eq '') {
		die "secondary_parent must be non-empty if send_state_changes_by_nsca and send_to_secondary_NSCA are true\n";
	    }
	    if ($max_messages_per_send_nsca < 1) {
		die "max_messages_per_send_nsca must be positive if send_state_changes_by_nsca is true\n";
	    }
	}
	if ($send_state_changes_by_gdma) {
	    if ($gdma_install_base eq '') {
		die "gdma_install_base must be non-empty if send_state_changes_by_gdma is true\n";
	    }
	    if (!-d $gdma_install_base) {
		die "gdma_install_base must be an existing directory if send_state_changes_by_gdma is true\n";
	    }
	    # Set up the spoolfile path based on the platform we are running on.
	    $gdma_spool_filename = GDMAUtils::get_spool_filename($gdma_install_base);
	    if ($max_unspooled_results_to_save < 0) {
		die "max_unspooled_results_to_save cannot be negative\n";
	    }
	}

	$debug_summary    = $debug_level >= 1;
	$debug_basic      = $debug_level >= 2;
	$debug_xml        = $debug_level >= 3;
	$debug_debug      = $debug_level >= 4;
	$debug_ridiculous = $debug_level >= 5;
    };
    if ($@) {
	chomp $@;
	$@ =~ s/^ERROR:\s+//i;
	die "Error:  Cannot read config file $config_file ($@)\n";
    }
}

sub freeze_logtime {
    $logtime = '[' . ( scalar localtime ) . '] ';
}

sub time_text {
    my $timestamp = shift;
    if ( $timestamp <= 0 ) {
	return '0';
    }
    else {
	my ( $seconds, $minutes, $hours, $day_of_month, $month, $year, $wday, $yday, $isdst ) = localtime($timestamp);
	return sprintf "%02d-%02d-%02d %02d:%02d:%02d", $year + 1900, $month + 1, $day_of_month, $hours, $minutes, $seconds;
    }
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

sub main {
    # If a "once" argument was passed on the command line, just run once to synchronize state between Nagios and Foundation.
    $sync_at_start = $ARGV[0] || 0;

    read_config_file ($default_config_file);

    if ( !open( LOG, '>>', $logfile ) ) {
	print "Can't open logfile $logfile ($!)\n";
    }
    LOG->autoflush(1);

    log_timed_message "=== Starting up (process $$). ===";

    # Set up to handle broken pipe errors.  This has to be done in conjunction with later code that
    # will cleanly process an EPIPE return code from a socket write.
    #
    # Our trivial signal handler turns SIGPIPE signals generated when we write to sockets already
    # closed by the server into EPIPE errors returned from the write operations.  The same would
    # happen if instead we just ignored these signals, but with this mechanism we also automatically
    # impose a short delay (inside the signal handler) when this situation occurs -- there is little
    # reason to keep pounding the server when it has already indicated it cannot accept data just now.
    $SIG{"PIPE"} = \&sig_pipe_handler;

    chomp $thisnagios;

    my $daemon_status = synchronized_daemon();

    close LOG;

    return $daemon_status;
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
	    return STOP_STATUS;
	}

	if ( !Locks->wait_for_file_to_disappear( $Locks::in_progress_file, \&log_timed_message, \$shutdown_requested ) ) {
	    flush_pending_output();
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
		flush_pending_output();
		log_shutdown();
		return STOP_STATUS;
	    }
	}

	( $dev, $ino, $mode, $nlink, $uid, $gid, $rdev, $size, $atime, $mtime, @rest ) = stat( \*commit_lock );
	if ( $mtime != $initial_mtime ) {
	    Locks->close_and_unlock( \*commit_lock );
	    flush_pending_output();
	    log_timed_message("=== A commit has occurred; will exit to start over and re-initialize (process $$). ===");
	    return RESTART_STATUS;
	}

	my $cycle_status = perform_feeder_cycle_actions();

	Locks->close_and_unlock( \*commit_lock );

	if ($cycle_status != CONTINUE_STATUS) {
	    flush_pending_output();
	    log_timed_message("=== Cycle status is not to continue; will exit (process $$). ===");
	    return $cycle_status;
	}

	if ($shutdown_requested) {
	    flush_pending_output();
	    log_shutdown();
	    return STOP_STATUS;
	}

	# Sleep until the next cycle boundary.
	sleep $cycle_sleep_time;
    }
}

sub flush_pending_output {
    if (@xml_messages) {
	## Note that $message_counter may well be -1 at this point.
	$message_counter = output_bundle_to_socket( \@xml_messages, $message_counter );
	@xml_messages = ();
    }
    ## Note that $message_counter may well be -1 at this point.
    $message_counter = send_pending_events( $message_counter, 1 );
}

# This signal handler is for ordinary use, during code that can be expected to check the
# $shutdown_requested flag fairly often.
sub handle_exit_signal {
    my $signame = shift;
    $shutdown_requested = 1;

    # for developer debugging only
    # log_timed_message "ERROR:  Received SIG$signame; aborting!";
}

# This signal handler is to be potentially installed as an alternate signal handler only around
# code that might run for a long time without checking the $shutdown_requested flag.  DBI calls
# often fall into this category; the C code within the DBI library might simply resume its action
# after seeing an EINTR, and not return to Perl so we can recognize the interrupt.  (DBD::mysql
# does not implement the $sth->cancel() operation, so that is not an option; see the DBI
# documentation about this.)  If you do use this, whatever cleanup activities you would
# ordinarily run before final process exit won't be run, so keep that in mind in the design of
# the overall script algorithm.
#
# Unfortunately, actual testing under heavy disk load shows that even running this short signal
# handler that exits from within its own context is not good enough to kill the script quickly
# upon receipt of a termination signal.  So instead we just revert to the usual system default
# behavior for such signals, allowing them to terminate the process directly.
sub die_upon_exit_signal {
    my $signame = shift;
    log_timed_message "NOTICE:  Received SIG$signame; exiting!";
    log_shutdown();
    exit (1);
}

sub sig_pipe_handler {
    sleep 2;
}

sub initialize_feeder {
    ## Pre-extend the event_messages array for later efficiency, then truncate back to an empty state.
    $#event_messages = $max_event_bundle_size;
    @event_messages = ();

    my $failed = 1;
    if ( my $socket = IO::Socket::INET->new( PeerAddr => $foundation_host, PeerPort => $foundation_port, Proto => 'tcp', Type => SOCK_STREAM ) ) {
	$socket->autoflush();
	log_timed_message "Start message local port: ", $socket->sockport() if $debug_summary;
	$failed = 0;
	unless ( $socket->sockopt(SO_SNDTIMEO, pack('L!L!', $socket_send_timeout, 0)) ) {
	    log_socket_problem ('setting send timeout on');
	    $failed = 1;
	}
	unless ($failed) {
	    log_timed_message 'Writing start message to Foundation.' if $debug_summary;
	    unless ( $socket->print ($start_message) ) {
		log_socket_problem ('writing to');
		$failed = 1;
	    }
	    else {
		LOG->print ($start_message, "\n") if $debug_xml;
	    }
	}
	unless ($failed) {
	    log_timed_message 'Writing close message to Foundation.' if $debug_summary;
	    unless ( $socket->print ($command_close) ) {
		log_socket_problem ('writing to');
		$failed = 1;
	    }
	    else {
		LOG->print ($command_close, "\n") if $debug_xml;
	    }
	}
	unless ( close($socket) ) {
	    log_socket_problem ('closing');
	    $failed = 1;
	}
    }
    if ($failed) {
	log_timed_message "Listener services not available. Retrying in $failure_sleep_time seconds.";
	sleep $failure_sleep_time;
	return RESTART_STATUS;
    }

    my $init_start_time = Time::HiRes::time();
    log_timed_message 'loading cached addresses ...';
    load_cached_addresses() or return ERROR_STATUS;
    log_timed_message 'loading global nagios parameters ...';
    $global_nagios = get_globals( $statusfile );
    if ( !defined($global_nagios) ) {
	return RESTART_STATUS;
    }
    log_timed_message 'loading initial state ...';
    my $ref = getInitialState($collage_status_ref);
    if ($shutdown_requested) {
	log_shutdown();
	return STOP_STATUS;
    }
    if ( !defined($ref) ) {
	return RESTART_STATUS;
    }
    
    # Startup message to parent - send sync
    if ( $send_state_changes_by_nsca || $send_state_changes_by_gdma ) {
	my $full_dump = assemble_remote_full_dump($collage_status_ref);
	my $last_full_dump_time = Time::HiRes::time();
	if ($send_state_changes_by_nsca) {
	    send_nsca( $primary_parent, $nsca_port, $nsca_timeout, $send_to_secondary_NSCA, $secondary_parent,
		$max_messages_per_send_nsca, $nsca_batch_delay, $full_dump );
	    $last_nsca_full_dump_time = $last_full_dump_time;
	}
	if ($send_state_changes_by_gdma) {
	    gdma_spool($gdma_results_to_spool, $full_dump);
	    $last_gdma_full_dump_time = $last_full_dump_time;
	}
    }
    if ($shutdown_requested) {
	log_shutdown();
	return STOP_STATUS;
    }

    if ($debug_summary) {
	my $init_time = sprintf "%0.4F", ( Time::HiRes::time() - $init_start_time );
	freeze_logtime();
	print               "Startup init time=$init_time seconds.\n";
	print LOG "${logtime}Startup init time=$init_time seconds.\n";
    }

    if ( $debug_ridiculous ) {
	freeze_logtime();
	print LOG $logtime, Data::Dumper->Dump( [ \%{$collage_status_ref} ], [qw(\%{collage_status_ref})] );
    }

    $total_wait     = 0;
    $n_hostcount    = 0;
    $n_servicecount = 0;

    $next_sync_timeout = time + $sync_timeout_seconds;
    $looping_start_time = Time::HiRes::time();

    log_timed_message 'starting main loop ...';

    return CONTINUE_STATUS;
}

sub perform_feeder_cycle_actions {
    my $start_time = Time::HiRes::time();
    if ( $debug_summary ) {
	log_timed_message 'Starting cycle.';
    }

    $total_wait += $cycle_sleep_time;

    # Don't bother with this loop iteration if the input data hasn't changed since last time.
    my $statusfile_mtime = (stat($statusfile))[9];
    if ( !defined $statusfile_mtime ) {
	freeze_logtime();
	print               "Warning: stat of file $statusfile failed: $!\n";
	print LOG "${logtime}Warning: stat of file $statusfile failed: $!\n";
	sleep $failure_sleep_time;
	return ERROR_STATUS;
    }
    elsif ($statusfile_mtime <= $last_statusfile_mtime) {
	print LOG "Skipping cycle -- $statusfile has not changed.\n";
    }
    else {
	$last_statusfile_mtime = $statusfile_mtime;

	if ( $total_wait >= $local_full_update_time ) {
	    $total_wait = 0;
	    if ($smart_update) {
		## Time to send heartbeat. That is, time to update LastUpdateTime stamps.
		$heartbeat_mode = 1;
		print LOG "Heartbeat in progress this cycle ...\n" if $debug_summary;
	    }
	}

	# Check count of hosts and services in Nagios and Foundation.
	# Note:  Unlike in getInitialState(), the calls to CollageQuery here are purposely not
	# set up to die immediately should a termination signal be received while the queries are
	# running.  That's for two reasons.  One, the calls we use here are simple "select count(*)"
	# queries that we don't expect to run terribly long.  And two, in this part of the logic,
	# we want to allow the caller an opportunity to clean up and flush any pending data before
	# we exit the process.
	my $foundation;
	eval {
	    $foundation = CollageQuery->new();
	};
	if ($@) {
	    chomp $@;
	    print LOG $@, "\n";
	    return ERROR_STATUS;
	}
	$f_hostcount = $foundation->getHostCount('NAGIOS');
	print LOG "Foundation Host Count: $f_hostcount\n" if $debug_basic;
	$f_servicecount = $foundation->getServiceCount('NAGIOS');
	print LOG "Foundation Service Count: $f_servicecount\n" if $debug_basic;

	# Get the status and counts from Nagios
	$element_ref = get_status( $statusfile, $nagios_version );
	if ($shutdown_requested) {
	    log_shutdown();
	    return STOP_STATUS;
	}
	if ( !defined($element_ref) ) {
	    return RESTART_STATUS;
	}

	print LOG "Nagios Host Count: $n_hostcount\n"       if $debug_basic;
	print LOG "Nagios Service Count: $n_servicecount\n" if $debug_basic;
	if ( $loop_count == 0 ) {    # first loop will not have last counts
	    $last_f_hostcount    = $f_hostcount;
	    $last_f_servicecount = $f_servicecount;
	    $last_n_hostcount    = $n_hostcount;
	    $last_n_servicecount = $n_servicecount;
	}

	# Now we can compare counts and see if Nagios and Foundation are in sync
	if ( ( $f_hostcount ne $n_hostcount ) or ( $f_servicecount ne $n_servicecount ) ) {

	    # Hold off on updates for a bit, because Nagios and Foundation are not synced.  With the proper
	    # synchronization code for this script now in play, this should never happen.  We keep this code
	    # around mainly to generate the out-of-sync message in case somehow the unexpected happens.
	    if ( $syncwaitcount >= $syncwait ) {

		# Tell the log about the differences that caused the sync errors
		if ( $debug_summary ) {
		    my $deltas = find_deltas( $element_ref, $collage_status_ref );
		    if ( $f_hostcount ne $n_hostcount ) {
			print LOG "Found $f_hostcount hosts in Foundation and $n_hostcount hosts in Nagios.\n";
		    }
		    if ( $f_servicecount ne $n_servicecount ) {
			print LOG "Found $f_servicecount services in Foundation and $n_servicecount services in Nagios.\n";
		    }
		    if ( $debug_basic ) {
			print LOG "Hosts and/or services in Foundation and not in Nagios:\n";
			print LOG Data::Dumper->Dump( [ \%{ $deltas->{FoundationHost} } ], [qw(Foundation)] );
			print LOG "Hosts and/or services in Nagios and not in Foundation:\n";
			print LOG Data::Dumper->Dump( [ \%{ $deltas->{NagiosHost} } ], [qw(Nagios)] );
		    }
		}
		$enable_feeding = 1;
		$syncwaitcount  = 0;
		if ( $loop_count != 0 ) {
		    log_timed_message 'Out of sync for too long!! Please try commit again. Resuming feeding.';
		    if ($send_sync_warning) {
			unless ( my $socket = IO::Socket::INET->new(
			  PeerAddr => $foundation_host, PeerPort => $foundation_port, Proto => 'tcp', Type => SOCK_STREAM
			  ) ) {
			    log_timed_message 'Listener services not available.';
			    # We don't "return RESTART_STATUS;" here or on subsequent socket failures because
			    # the message we're about to submit is just advisory, and the opportunity to submit
			    # the same message will probably appear again in a later processing cycle.
			}
			else {
			    $socket->autoflush();
			    log_timed_message "Out-of-sync message local port: ", $socket->sockport() if $debug_summary;
			    unless ( $socket->sockopt(SO_SNDTIMEO, pack('L!L!', $socket_send_timeout, 0)) ) {
				log_socket_problem ('setting send timeout on');
			    }
			    else {
				unless ( $socket->print (
				  "<GENERICLOG consolidation='SYSTEM' ApplicationType='SYSTEM' MonitorServerName='localhost' Device='127.0.0.1' Severity='WARNING' MonitorStatus='WARNING' TextMessage='Foundation and Nagios are out of sync. You may need to commit your Nagios configuration again. Check the log at /usr/local/groundwork/foundation/container/logs/nagios2collage_socket.log for details. Nagios hosts: $n_hostcount, Foundation hosts: $f_hostcount, Nagios services: $n_servicecount, Foundation services: $n_servicecount.' />"
				  ) ) {
				    log_timed_message 'Writing log message to Foundation.' if $debug_summary;
				    log_socket_problem ('writing to');
				}
			    }
			    unless ( close($socket) ) {
				log_socket_problem ('closing');
			    }

			    # Re-query Foundation
			    $f_hostcount    = $foundation->getHostCount('NAGIOS');
			    $f_servicecount = $foundation->getServiceCount('NAGIOS');
			}
		    }
		}
	    }
	    else {
		$enable_feeding = 0;
		$syncwaitcount++;
		my $cycles_left = $syncwait - $syncwaitcount;
		log_timed_message "Out of sync detected! Waiting on updates for up to $cycles_left more cycles ...";
	    }
	}
	else {
	    if (   ( $last_f_hostcount    ne $f_hostcount )
		or ( $last_f_servicecount ne $f_servicecount )
		or ( $last_n_hostcount    ne $n_hostcount )
		or ( $last_n_servicecount ne $n_servicecount ) )
	    {
		## Case 1: Changed, but in sync.  We missed the sync, so just re-start.
		log_timed_message 'Changed, but in sync (missed sync). Restarting.';
		return RESTART_STATUS;
	    }
	}

	# Now reset the counts for next time
	$last_f_hostcount    = $f_hostcount;
	$last_f_servicecount = $f_servicecount;
	$last_n_hostcount    = $n_hostcount;
	$last_n_servicecount = $n_servicecount;
	$n_hostcount         = 0;
	$n_servicecount      = 0;

	if ( $element_ref && $enable_feeding ) {
	    my $host_updates_ref;
	    my $serv_updates_ref;
	    if ($heartbeat_mode) {
		$global_nagios = get_globals( $statusfile );
		if ( !defined($global_nagios) ) {
		    return RESTART_STATUS;
		}
	    }

	    my $state_changes = ( $send_state_changes_by_nsca || $send_state_changes_by_gdma ) ? [] : undef;
	    $host_updates_ref = build_host_xml( $thisnagios, $element_ref, $collage_status_ref, $state_changes );
	    return RESTART_STATUS if not defined $host_updates_ref;
	    $serv_updates_ref = build_service_xml( $thisnagios, $element_ref, $collage_status_ref, $state_changes );
	    return RESTART_STATUS if not defined $serv_updates_ref;
	    push( @xml_messages, @{$host_updates_ref} );
	    push( @xml_messages, @{$serv_updates_ref} );

	    if ( defined($state_changes) && @$state_changes ) {
		if ($send_state_changes_by_nsca) {
		    send_nsca( $primary_parent, $nsca_port, $nsca_timeout, $send_to_secondary_NSCA, $secondary_parent,
			$max_messages_per_send_nsca, $nsca_batch_delay, $state_changes );
		}
		if ($send_state_changes_by_gdma) {
		    gdma_spool($gdma_results_to_spool, $state_changes);
		}
	    }

	    if ( @xml_messages >= $xml_bundle_size || ( @xml_messages > 0 && time >= $next_sync_timeout ) ) {
		$message_counter = output_bundle_to_socket( \@xml_messages, $message_counter );
		return RESTART_STATUS if ($message_counter < 0);
		@xml_messages      = ();
		$next_sync_timeout = time + $sync_timeout_seconds;

		$loop_count++;
		if ($debug_summary) {
		    my $loop_time = sprintf "%0.4F", Time::HiRes::time() - $start_time;
		    my $avg_loop_time = sprintf "%0.4F",
		      ( Time::HiRes::time() - $looping_start_time - ( ( $loop_count - 1 ) * $cycle_sleep_time ) ) / $loop_count;
		    freeze_logtime();
		    print               "Loops Completed = $loop_count. Last loop time=$loop_time seconds. Avg loop time=$avg_loop_time seconds.\n";
		    print LOG "${logtime}Loops Completed = $loop_count. Last loop time=$loop_time seconds. Avg loop time=$avg_loop_time seconds.\n";
		}
	    }
	}

	# quit after just one run -- legacy, now used only for development testing
	if ( $enable_feeding && $sync_at_start =~ /once/ ) {
	    log_timed_message 'Exiting after one cycle, per command option.';
	    return STOP_STATUS;
	}

	# re-enable feeding ...
	$enable_feeding = 1;

	$heartbeat_mode = 0;
    }
    # Send any pending state transitions left in the buffer
    $message_counter = send_pending_events( $message_counter, 1 );

    my $now = Time::HiRes::time();
    my $send_nsca_full_dump = $send_state_changes_by_nsca && ($nsca_full_dump_interval > 0) &&
	(($now - $last_nsca_full_dump_time) > $nsca_full_dump_interval);
    my $send_gdma_full_dump = $send_state_changes_by_gdma && ($gdma_full_dump_interval > 0) &&
	(($now - $last_gdma_full_dump_time) > $gdma_full_dump_interval);

    if (@$gdma_results_to_spool) {
	gdma_spool($gdma_results_to_spool, []);
    }
    if ( $send_nsca_full_dump || $send_gdma_full_dump ) {
	my $full_dump = assemble_remote_full_dump($collage_status_ref);
	if ($send_nsca_full_dump) {
	    send_nsca( $primary_parent, $nsca_port, $nsca_timeout, $send_to_secondary_NSCA, $secondary_parent,
		$max_messages_per_send_nsca, $nsca_batch_delay, $full_dump );
	    $last_nsca_full_dump_time = $now;
	}
	if ($send_gdma_full_dump) {
	    gdma_spool($gdma_results_to_spool, $full_dump);
	    $last_gdma_full_dump_time = $now;
	}
    }

    return ($message_counter < 0) ? RESTART_STATUS : CONTINUE_STATUS;
}

sub load_cached_addresses() {

    # Get hosts->IPaddress from Monarch
    my ( $Database_Name, $Database_Host, $Database_User, $Database_Password ) = CollageQuery::readGroundworkDBConfig("monarch");
    my $dbh = DBI->connect( "DBI:mysql:$Database_Name:$Database_Host", $Database_User, $Database_Password );
    if ( !$dbh ) {
	log_message "Can't connect to database $Database_Name. Error: ", $DBI::errstr;
	return 0;
    }
    my $query = "select name, address from hosts;";
    my $sth   = $dbh->prepare($query);
    if ( !$sth->execute() ) {
	log_message $sth->errstr;
	$sth->finish();
	$dbh->disconnect();
	return 0;
    }
    my @serviceprofile_ids = ();
    while ( my $row = $sth->fetchrow_hashref() ) {
	$hostipaddress{ $$row{name} } = $$row{address};
    }
    $sth->finish();
    $dbh->disconnect();

    return 1;
}

sub getInitialState {
    ## Check each host and service status in Foundation, and populate collage_status_ref
    ## with current state.  Do this at startup to avoid huge initial message loads.
    my $collage_status_ref = shift;

    # In this routine, we set up to die instantly if certain database calls are interrupted by a signal.
    # The $foundation->getHostServices() call in particular can take a considerable amount of time, but
    # some of its internal database-access components (DBD::mysql) are effectively not interruptible by
    # signals (the EINTR return code from some internal system call is recognized and the interrupted
    # system call is restarted, instead of having some means to check a cancel-is-requested flag and
    # stop the request).  This script is instrumented to effectively return as quickly as signals are
    # recognized by Perl, but that might be far too long for outside applications to wait for the death
    # of this script once it has been signaled to terminate, especially on a very busy system (typically,
    # one where the available disk i/o is saturated).  Fortunately, we know by code inspection that there
    # are no resources that need flushing or cleaning up before we exit here.

    my $foundation;
    eval {
	# local $SIG{INT}  = \&die_upon_exit_signal;
	# local $SIG{QUIT} = \&die_upon_exit_signal;
	# local $SIG{TERM} = \&die_upon_exit_signal;
	local $SIG{INT}  = 'DEFAULT';
	local $SIG{QUIT} = 'DEFAULT';
	local $SIG{TERM} = 'DEFAULT';
	$foundation = CollageQuery->new();
    };
    if ($@) {
	chomp $@;
	print LOG $@, "\n";
	return undef;
    }
    log_timed_message '... getting Nagios status ...';
    my $element_ref = get_status( $statusfile, $nagios_version );
    if ($shutdown_requested) {
	return undef;
    }
    if ( !defined($element_ref) ) {
	return undef;
    }
    if ( $debug_ridiculous ) {
	freeze_logtime();
	print LOG $logtime, Data::Dumper->Dump( [ \%{$element_ref} ], [qw(\%element_ref)] );
    }
    log_timed_message '... getting hosts ...';
    my $fn_hosts = undef;
    eval {
	# local $SIG{INT}  = \&die_upon_exit_signal;
	# local $SIG{QUIT} = \&die_upon_exit_signal;
	# local $SIG{TERM} = \&die_upon_exit_signal;
	local $SIG{INT}  = 'DEFAULT';
	local $SIG{QUIT} = 'DEFAULT';
	local $SIG{TERM} = 'DEFAULT';
	$fn_hosts = $foundation->getHosts();
    };
    if ($@) {
	chomp $@;
	log_timed_message "Error in getHosts: $@";
	return undef;
    }
    if ($shutdown_requested) {
	return undef;
    }
    log_timed_message '... getting host services ...';
    my $fn_host_services = undef;
    eval {
	# local $SIG{INT}  = \&die_upon_exit_signal;
	# local $SIG{QUIT} = \&die_upon_exit_signal;
	# local $SIG{TERM} = \&die_upon_exit_signal;
	local $SIG{INT}  = 'DEFAULT';
	local $SIG{QUIT} = 'DEFAULT';
	local $SIG{TERM} = 'DEFAULT';
	$fn_host_services = $foundation->getHostServices();
    };
    if ($@) {
	chomp $@;
	log_timed_message "Error in getHostServices: $@";
	return undef;
    }
    if ($shutdown_requested) {
	return undef;
    }
    log_timed_message '... processing host/service state ...';
    if ( ref($fn_hosts) eq 'HASH' ) {
	foreach my $host ( keys %{$fn_hosts} ) {
	    my $fn_host = $fn_hosts->{$host};
	    my $cs_host = \%{ $collage_status_ref->{Host}->{$host} };
	    my $el_host = $element_ref->{Host}->{$host};
	    if ( $debug_debug ) {
		print LOG Data::Dumper->Dump( [ $fn_host ], [qw($fn_host)] );
		print LOG "Nagios last check time: $el_host->{LastCheckTime}\n";
		print LOG "Nagios next check time: $el_host->{NextCheckTime}\n";
	    }

	    # Look for hosts that have never been checked -- don't bother sending results if so.
	    if ( $el_host->{LastCheckTime} eq '0' && ( !defined $fn_host->{LastCheckTime} ) ) {
		$cs_host->{LastCheckTime} = '0';    # This will show up as no change of state
	    }
	    else {
		$cs_host->{LastCheckTime} = $fn_host->{LastCheckTime};    # Might be a change, might not
	    }
	    # Do the same for NexCheckTime in case it was never fed (like for passive checks)
	    if ( $el_host->{NextCheckTime} eq '0' && ( !defined $fn_host->{NextCheckTime} ) ) {
		$cs_host->{NextCheckTime} = '0';    # This will show up as no change of state
	    }
	    else {
		$cs_host->{NextCheckTime} = $fn_host->{NextCheckTime};    # Might be a change, might not
	    }
	    # Do the same for LastNotificationTime
	    if ( $el_host->{LastNotificationTime} eq '0' && ( !defined $fn_host->{LastNotificationTime} ) ) {
		$cs_host->{LastNotificationTime} = '0';    # This will show up as no change of state
	    }
	    else {
		$cs_host->{LastNotificationTime} = $fn_host->{LastNotificationTime};    # Might be a change, might not
	    }
	    $cs_host->{Comments}                  = $fn_host->{Comments};
	    $cs_host->{CurrentAttempt}            = $fn_host->{CurrentAttempt};
	    $cs_host->{CurrentNotificationNumber} = $fn_host->{CurrentNotificationNumber};
	    $cs_host->{ExecutionTime}             = $fn_host->{ExecutionTime};
	    $cs_host->{Latency}                   = $fn_host->{Latency};
	    $cs_host->{MaxAttempts}               = $fn_host->{MaxAttempts};
	    $cs_host->{MonitorStatus}             = $fn_host->{MonitorStatus};
	    $cs_host->{NextCheckTime}             = $fn_host->{NextCheckTime};
	    $cs_host->{ScheduledDowntimeDepth}    = $fn_host->{ScheduledDowntimeDepth};
	    $cs_host->{StateType}                 = $fn_host->{StateType};
	    $cs_host->{isAcknowledged}            = $fn_host->{isAcknowledged};
	    $cs_host->{isChecksEnabled}           = $fn_host->{isChecksEnabled};
	    $cs_host->{isEventHandlersEnabled}    = $fn_host->{isEventHandlersEnabled};
	    $cs_host->{isFlapDetectionEnabled}    = $fn_host->{isFlapDetectionEnabled};
#	    $cs_host->{isHostFlapping}            = $fn_host->{isHostFlapping};
	    $cs_host->{isNotificationsEnabled}    = $fn_host->{isNotificationsEnabled};
#	    $cs_host->{isObsessOverHost}          = $fn_host->{isObsessOverHost};
	    $cs_host->{isPassiveChecksEnabled}    = $fn_host->{isPassiveChecksEnabled};
	    $cs_host->{LastPluginOutput}          = $fn_host->{LastPluginOutput};
	    $cs_host->{PercentStateChange}        = $fn_host->{PercentStateChange};
	    $cs_host->{LastStateChange}           = $fn_host->{LastStateChange};
	    # Look for fancy MonitorStatus values and translate to the simple ones Nagios knows
	    if ( $fn_host->{MonitorStatus} =~ /DOWN/ ) {
		$cs_host->{MonitorStatus} = 'DOWN';
	    }
	    # FIX FUTURE: We ignore isObsessOverHost for now, as it is not needed in Foundation (yet).
	    # Similarly, we ignore isHostFlapping.
	    # isObsessOverHost           (property)
	    # The isObsessOverHost flag is perhaps problematic.  The obsess_over_host flag can be set in Nagios
	    # for an individual host, but such settings can be globally overridden by the obsess_over_hosts flag
	    # at the Nagios level.  So we need to override the host setting with the global if it's off ...
#	    if ( $global_nagios->{obsess_over_hosts} == 0 ) {
#		$cs_host->{isObsessOverHost} = 0;
#	    }
	    # Separately, this property is not set in Foundation: GWMON-7678 filed to address this.
	    # Take out the following assignment when that issue is resolved:
#	    if ( !defined $cs_host->{isObsessOverHost} ) {
#		$cs_host->{isObsessOverHost} = $el_host->{isObsessOverHost};
#	    }
	    ####

	    if ( !defined $cs_host->{Comments} ) {
		$cs_host->{Comments} = ' ';
	    }
	    if ( ref($fn_host_services) eq 'HASH' ) {
		foreach my $service ( keys %{ $fn_host_services->{$host} } ) {
		    my $fn_svc = $fn_host_services->{$host}->{$service};
		    my $cs_svc  = \%{ $cs_host->{Service}->{$service} };
		    my $el_svc  = $el_host->{Service}->{$service};
		    if ( $debug_debug ) {
			print LOG Data::Dumper->Dump( [ $fn_svc ], [qw($fn_svc)] );
		    }
		    my $f_state = $fn_svc->{MonitorStatus};
		    my $n_state = $el_svc->{MonitorStatus};

		    # $fn_svc->{LastCheckTime}; This does not exist -- must use the Check Time from the current status log ...
		    $cs_svc->{LastCheckTime} = $el_svc->{LastCheckTime};

		    # $fn_svc->{LastNotificationTime}; This might not be defined, so if 0 in nagios, don't generate a difference.
		    if ( $el_svc->{LastNotificationTime} eq '0' && ( !defined $fn_svc->{LastNotificationTime} ) ) {
			$cs_svc->{LastNotificationTime} = '0';    # This will show up as no change of state
		    } else {
			$cs_svc->{LastNotificationTime} = $fn_svc->{LastNotificationTime};    # Might be a change, might not
		    }
		    if ( !defined $cs_svc->{Comments} ) {
			$cs_svc->{Comments} = ' ';
		    }
		    $cs_svc->{MonitorStatus} 	         = $fn_svc->{MonitorStatus};
		    $cs_svc->{CurrentAttempt}            = $fn_svc->{CurrentAttempt};
		    $cs_svc->{CurrentNotificationNumber} = $fn_svc->{CurrentNotificationNumber};
		    $cs_svc->{MaxAttempts}               = $fn_svc->{MaxAttempts};
		    $cs_svc->{NextCheckTime}             = $fn_svc->{NextCheckTime};
		    $cs_svc->{ScheduledDowntimeDepth}    = $fn_svc->{ScheduledDowntimeDepth};
		    $cs_svc->{isAcceptPassiveChecks}     = $fn_svc->{isAcceptPassiveChecks};
		    $cs_svc->{isChecksEnabled}           = $fn_svc->{isChecksEnabled};
		    $cs_svc->{isEventHandlersEnabled}    = $fn_svc->{isEventHandlersEnabled};
		    $cs_svc->{isFlapDetectionEnabled}    = $fn_svc->{isFlapDetectionEnabled};
		    $cs_svc->{isNotificationsEnabled}    = $fn_svc->{isNotificationsEnabled};
#		    $cs_svc->{isObsessOverService}       = $fn_svc->{isObsessOverService};
		    $cs_svc->{isProblemAcknowledged}     = $fn_svc->{isProblemAcknowledged};
#		    $cs_svc->{isServiceFlapping}         = $fn_svc->{isServiceFlapping};
		    $cs_svc->{LastPluginOutput}	         = $fn_svc->{LastPluginOutput};
		    $cs_svc->{PercentStateChange}        = $fn_svc->{PercentStateChange};
		    $cs_svc->{Latency}		         = $fn_svc->{Latency};
		    $cs_svc->{ExecutionTime}             = $fn_svc->{ExecutionTime};
		    $cs_svc->{LastStateChange}           = $fn_svc->{LastStateChange};
		    $cs_svc->{StateType}		 = $fn_svc->{StateType};
		    # Look for fancy MonitorStatus values and translate to the simple ones Nagios knows
		    if ( $fn_svc->{MonitorStatus} =~ /CRITICAL/ ) {
			$cs_svc->{MonitorStatus} = 'CRITICAL';
		    } elsif ( $fn_svc->{MonitorStatus} =~ /WARNING/ ) {
			$cs_svc->{MonitorStatus} = 'WARNING';
		    }
		}
	    }
	}
    }
    return $collage_status_ref;
}

sub open_socket {
    my $socket = undef;
    my $failed = 1;

    # FIX FUTURE:  Here and for all the other sockets in this script, we want to implement a
    # connect timeout, possibly by using the new() Timeout parameter.  But the documentation
    # is terribly ambiguous about the actual effect of that setting, so careful testing is
    # required to verify that it would have the desired effect.
    #
    # SendBuf is an as-yet-undocumented patch to IO::Socket::INET.
    my @socket_args = ( PeerAddr => $foundation_host, PeerPort => $foundation_port, Proto => 'tcp', Type => SOCK_STREAM );
    push @socket_args, ( SendBuf => $send_buffer_size ) if ($send_buffer_size > 0);
    unless ( $socket = IO::Socket::INET->new( @socket_args ) ) {
	log_timed_message "Couldn't connect to $foundation_host:$foundation_port : $!";
    }
    else {
	$socket->autoflush();
	log_timed_message "Output bundle local port: ", $socket->sockport() if $debug_summary;
	$failed = 0;

	# Here we set a send timeout.  The right value is subject to discussion, given that it may depend
	# on the current load of the receiver process.  Compare this send timout with the receiver timeout,
	# which is set as thread.timeout.idle in /usr/local/groundwork/config/foundation.properties .
	unless ( $socket->sockopt(SO_SNDTIMEO, pack('L!L!', $socket_send_timeout, 0)) ) {
	    log_socket_problem ('setting send timeout on');
	    $failed = 1;
	}
	if ($debug_summary) {
	    my $send_buf = $socket->sockopt(SO_SNDBUF);
	    unless ( $send_buf >= 0 ) {
		log_socket_problem ('getting send buffer size on');
		$failed = 1;
	    }
	    log_timed_message "Reported socket send buffer size: ", $send_buf;
	}
	if ($failed) {
	    unless ( close($socket) ) {
		log_socket_problem ('closing');
	    }
	    $socket = undef;
	}
    }

    return $socket;
}

# Close the socket, whether it was working or faulty.
sub close_socket {
    my $socket = shift;
    my $failed = shift;

    unless ($failed) {
	log_timed_message 'Writing close message to Foundation.' if $debug_summary;
	unless ( $socket->print ($command_close) ) {
	    log_socket_problem ('writing to');
	    $failed = 1;
	}
	else {
	    LOG->print ($command_close, "\n\n") if $debug_xml;
	}
    }

    # FIX FUTURE:  This socket closing will invoke a write operation on any data still left hanging
    # within Perl's own buffering of the data we wrote above.  Generally, each of the writes above
    # would have written all the data in the buffer before the write returned to this code.  But some
    # data can be left in the Perl buffers if the socket write times out.  And now this close() will
    # attempt to write that data, to a socket which is probably bungled, without the last previous
    # write having successfully completed (but with the write pointer inexplicably updated in spite
    # of the error) -- clearly a bad idea from the point of view of the downstream reader, who will
    # now be faced with a corrupted data stream if this additional writing actually succeeds in
    # transferring any data.  So to minimize problems, we ought to figure out how to clear the Perl
    # buffer before attempting the close() operation, if not all of the data got sent above.  But we
    # currently don't see any IO::Handle method that will carry out this $socket->clear() operation.
    # The upshot is that any additional writes invoked here may also block and be subject to whatever
    # SO_SNDTIMEO timeout we set above on the socket.  (I suppose we could set that timeout here to
    # just 1 microsecond, as the closest approximation to what we want, given the tools available.
    # That won't actually prevent the extra write(s) from occurring, though.)
    unless ( !$failed || $socket->sockopt(SO_SNDTIMEO, pack('L!L!', 0, 1)) ) {
	log_socket_problem ('setting send timeout on');
    }

    # An error reported here might be due to an error writing whatever remains in the Perl i/o
    # buffering.  If that is true, then we should treat it just like a failure to write just
    # above, and revert back to the beginning of this adapter packet and re-send the entire thing.
    unless ( close($socket) ) {
	log_socket_problem ('closing');
	$failed = 1;
    }

    return !$failed;
}

sub output_bundle_to_socket {
    my $msg_ref    = shift;
    my $series_num = shift;
    my $socket;
    my $failed = 1;

    $socket = open_socket();
    if ($socket) {
	$failed = 0;

	my $use_careful_sockets   = 1;
	my $use_efficient_sockets = 0;
	if ($use_careful_sockets) {
	    ## Efficient operation as below, except that we limit the total amount of data sent
	    ## per connection, closing it and opening a new connection if we exceed that limit.
	    ## Also, this code is able to accommodate a transient sending failure by retrying
	    ## the failed operation.
	    my $next          = 0;
	    my $last          = -1;
	    my $last_index    = $#$msg_ref;
	    my $element_begin = undef;
	    my $element_end   = "</Command>\n</Adapter>";
	    my $elements;
	    my $bytes_per_connection     = 0;
	    my $max_bytes_per_connection = 253952;  # 256K - 8K, for initial testing
	    my $send_retries     = 0;
	    my $max_send_retries = 3;
	    while ( $next <= $last_index ) {
		$last = $next + $max_xml_bundle_size - 1;
		$last = $last_index if $last > $last_index;
		my $curr;
		for ($curr = $next; $curr <= $last; ++$curr) {
		    $bytes_per_connection += length( $msg_ref->[$curr] );
		    last if ($bytes_per_connection > $max_bytes_per_connection);
		}
		--$curr;
		$last = ($curr < $next) ? $next : $curr;
		$series_num++;
		$element_begin =
		  qq(<Adapter Session="$series_num" AdapterType="SystemAdmin">\n<Command ApplicationType='NAGIOS' Action='MODIFY'>\n);
		$elements = join( '', $element_begin, @{$msg_ref}[ $next .. $last ], $element_end );
		log_timed_message "Writing Adapter message (Session $series_num) to Foundation: ", length($elements), " bytes." if $debug_summary;
		unless ( $socket->print ($elements) ) {
		    log_socket_problem ('writing to');
		    if (++$send_retries > $max_send_retries) {
			log_timed_message 'Too many retries on socket writing -- will exit.';
		    }
		    else {
			# Ignore errors on closing, as we already know the socket is faulty.
			close_socket($socket, 1);
			$socket = open_socket();
			if ($socket) {
			    $bytes_per_connection = 0;
			    redo;
			}
		    }
		    $failed = 1;
		    last;
		}
		if ($shutdown_requested) {
		    log_shutdown();
		    close_socket($socket, 0);
		    $socket = undef;
		    $failed = 1;
		    last;
		}
		LOG->print ($elements, "\n") if $debug_xml;

		if ($bytes_per_connection > $max_bytes_per_connection && $last < $last_index) {
		    # We've sent enough already on this particular connection, and there is
		    # still more data to send.  Use a new connection for the remaining data.
		    my $clean_close = close_socket($socket, 0);
		    $socket = open_socket();
		    if (!$socket) {
			$failed = 1;
			last;
		    }
		    $send_retries = 0;
		    $bytes_per_connection = 0;
		    redo if !$clean_close;
		}
		$next = $last + 1;
	    }
	}
	elsif ($use_efficient_sockets) {
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
		  qq(<Adapter Session="$series_num" AdapterType="SystemAdmin">\n<Command ApplicationType='NAGIOS' Action='MODIFY'>\n);
		$elements = join( '', $element_begin, @{$msg_ref}[ $next .. $last ], $element_end );
		log_timed_message "Writing Adapter message (Session $series_num) to Foundation: ", length($elements), " bytes." if $debug_summary;
		unless ( $socket->print ($elements) ) {
		    log_socket_problem ('writing to');
		    $failed = 1;
		    last;
		}
		if ($shutdown_requested) {
		    log_shutdown();
		    $failed = 1;
		    last;
		}
		LOG->print ($elements, "\n") if $debug_xml;
		$next = $last + 1;
	    }
	}
	else {
	    ## Legacy operation, now deprecated.
	    my $element_begin = undef;
	    my $element_end   = "</Command>\n</Adapter>";
	    while (@{$msg_ref}) {
		$series_num++;
		$element_begin =
		  qq(<Adapter Session="$series_num" AdapterType="SystemAdmin">\n<Command ApplicationType='NAGIOS' Action='MODIFY'>\n);
		log_timed_message 'Writing Adapter begin message to Foundation.' if $debug_summary;
		unless ( $socket->print ($element_begin) ) {
		    log_socket_problem ('writing to');
		    $failed = 1;
		    last;
		}
		if ($shutdown_requested) {
		    log_shutdown();
		    $failed = 1;
		    last;
		}
		LOG->print ($element_begin, "\n") if $debug_xml;
		my $num_messages_output = 0;
		while ( @{$msg_ref} && $num_messages_output < $max_xml_bundle_size ) {
		    $num_messages_output++;
		    my $message = shift( @{$msg_ref} );
		    log_timed_message 'Writing Adapter body message to Foundation.' if $debug_summary;
		    unless ( $socket->print ($message) ) {
			log_socket_problem ('writing to');
			$failed = 1;
			last;
		    }
		    if ($shutdown_requested) {
			log_shutdown();
			$failed = 1;
			last;
		    }
		    LOG->print ($message, "\n") if $debug_xml;
		}
		last if $failed;
		log_timed_message 'Writing Adapter end message to Foundation.' if $debug_summary;
		unless ( $socket->print ($element_end) ) {
		    log_socket_problem ('writing to');
		    $failed = 1;
		    last;
		}
		if ($shutdown_requested) {
		    log_shutdown();
		    $failed = 1;
		    last;
		}
		LOG->print ($element_end, "\n") if $debug_xml;
	    }
	}

	if ($socket) {
	    $failed |= !close_socket($socket, $failed);
	}
    }

    return $failed ? -1 : $series_num;
}

sub send_pending_events {
    my $series_num      = shift;
    my $max_bundle_size = shift;
    my $failed = 0;

    if ( scalar(@event_messages) >= $max_bundle_size ) {
	my $socket;
	$failed = 1;
	for (my $attempts = 10; --$attempts >= 0; ) {
	    # SendBuf is an as-yet-undocumented patch to IO::Socket::INET.
	    my @socket_args = ( PeerAddr => $foundation_host, PeerPort => $foundation_port, Proto => 'tcp', Type => SOCK_STREAM );
	    push @socket_args, ( SendBuf => $send_buffer_size ) if ($send_buffer_size > 0);
	    if ( $socket = IO::Socket::INET->new( @socket_args ) ) {
		$socket->autoflush();
		log_timed_message "Pending events local port: ", $socket->sockport() if $debug_summary;
		$failed = 0;
		last if $socket->sockopt(SO_SNDTIMEO, pack('L!L!', $socket_send_timeout, 0));
		log_socket_problem ('setting send timeout on');
		$failed = 1;
		unless ( close($socket) ) {
		    log_socket_problem ('closing');
		}
	    }
	    log_timed_message 'Cannot open a socket to the Foundation listener. Retrying in 2 seconds.';
	    sleep 2;
	    if ($shutdown_requested) {
		log_shutdown();
		last;
	    }
	}
	if ($failed) {
	    log_timed_message "Listener services not available. Restarting in $failure_sleep_time seconds.";
	    sleep $failure_sleep_time;
	}
	else {
	    ## Assemble XML for sending to Foundation.
	    $series_num++;
	    my $element_begin = qq(<Adapter Session="$series_num" AdapterType="SystemAdmin">\n<Command ApplicationType='NAGIOS' Action='ADD'>);
	    my $element_end   = "</Command>\n</Adapter>";
	    my $elements      = join( "\n", $element_begin, @event_messages, $element_end, $command_close );

	    log_timed_message 'Writing events message to Foundation.' if $debug_summary;
	    unless ( $socket->print ($elements) ) {
		log_socket_problem ('writing to');
		$failed = 1;
	    }
	    else {
		LOG->print ($elements, "\n") if $debug_xml;
	    }
	    unless ( close($socket) ) {
		log_socket_problem ('closing');
		$failed = 1;
	    }
	    ## Here we don't discard messages we could not send.
	    ## That means they will build up indefinitely until we do.
	    if ( !$failed ) {
		@event_messages = ();
	    }
	}
    }
    if ($shutdown_requested) {
	log_shutdown();
	$failed = 1;
    }
    return $failed ? -1 : $series_num;
}

sub get_status {
    my $statusfile = shift;
    my $version    = shift;
    if ( $version == 3 ) {
	return get_status_v3($statusfile);
    }
    if ( $version == 2 ) {
	return get_status_v2($statusfile);
    }
    if ( $version == 1 ) {
	return get_status_v1($statusfile);
    }
    print LOG "$0 error: unknown Nagios version: [$version]\n";
    sleep $failure_sleep_time;
    return undef;
}

sub get_status_v1 {
    my $statusfile = shift;
    my ( $timestamp, $msgtype );
    my @field;
    my $element_ref;

    # FIX FUTURE:  don't just abort on failure; retry 3 times or so
    if ( !open( STATUSFILE, '<:unix:mmap', $statusfile ) ) {
	freeze_logtime();
	print               "Error opening file $statusfile: $!\n";
	print LOG "${logtime}Error opening file $statusfile: $!\n";
	sleep $failure_sleep_time;
	return undef;
    }
    while ( my $line = <STATUSFILE> ) {

# [1100304091] HOST;Application_1;UP;1100304086;1100280796;0;7462261;6887;36466;1100280796;0;1;1;1;1;0;0.00;0;1;1;PING OK - Packet loss = 0%, RTA = 25.22 ms
	if ( $line =~ /^\s*\#]/ ) { next; }
	@field = split /;/, $line;
	if ( $field[0] =~ /\[(\d+)\] (.*)/ ) {
	    $timestamp = $1;
	    $msgtype   = $2;
	}
	else {
	    next;
	}

	# Use Collage database field names as service keys
	my $el_host = \%{ $element_ref->{Host}->{ $field[1] } };
	if ( $msgtype =~ /SERVICE/ ) {
	    my $el_svc = \%{ $el_host->{Service}->{ $field[2] } };

	    if ( $field[6] == 0 )  { $field[6]  = time; }
	    if ( $field[12] == 0 ) { $field[12] = time; }
	    $field[31] =~ s/\n/ /g;
	    $field[31] =~ s/\f/ /g;
	    $field[31] =~ s/<br>/ /ig;
	    $field[31] =~ s/&/&amp;/g;
	    $field[31] =~ s/"/&quot;/g;
	    $field[31] =~ s/'/&apos;/g;
	    $field[31] =~ s/</&lt;/g;
	    $field[31] =~ s/>/&gt;/g;

	    # $el_svc->{RetryNumber} = '1'; #$field[4];
	    my $tmp = $field[4];
	    if ( $tmp =~ /(\d+)\/(\d+)/ ) {
		my $RetryNumber = $1;
		my $MaxTry      = $2;
		$el_svc->{RetryNumber} = $RetryNumber;
	    }
	    $el_svc->{MonitorStatus}              = $field[3];
	    $el_svc->{StateType}                  = $field[5];
	    $el_svc->{LastCheckTime}              = time_text( $field[6] );
	    $el_svc->{NextCheckTime}              = time_text( $field[7] );
	    $el_svc->{CheckType}                  = $field[8];
	    $el_svc->{isChecksEnabled}            = $field[9];
	    $el_svc->{isAcceptPassiveChecks}      = $field[10];
	    $el_svc->{isEventHandlersEnabled}     = $field[11];
	    $el_svc->{LastStateChange}            = time_text( $field[12] );
	    $el_svc->{isProblemAcknowledged}      = $field[13];
	    $el_svc->{LastHardState}              = $field[14];
	    $el_svc->{TimeOK}                     = $field[15];
	    $el_svc->{TimeUnknown}                = $field[16];
	    $el_svc->{TimeWarning}                = $field[17];
	    $el_svc->{TimeCritical}               = $field[18];
	    $el_svc->{LastNotificationTime}       = time_text( $field[19] );
	    $el_svc->{CurrentNotificationNumber}  = $field[20];
	    $el_svc->{isNotificationsEnabled}     = $field[21];
	    $el_svc->{Latency}                    = $field[22];
	    $el_svc->{ExecutionTime}              = $field[23];
	    $el_svc->{isFlapDetectionEnabled}     = $field[24];
	    $el_svc->{isServiceFlapping}          = $field[25];
	    $el_svc->{PercentStateChange}         = $field[26];
	    $el_svc->{ScheduledDowntimeDepth}     = $field[27];
	    $el_svc->{isFailurePredictionEnabled} = $field[28];
	    $el_svc->{isProcessPerformanceData}   = $field[29];
	    $el_svc->{isObsessOverService}        = $field[30];
	    $el_svc->{LastPluginOutput}           = $field[31];
	}
	elsif ( $msgtype =~ /HOST/ ) {
	    if ( $field[3] == 0 ) { $field[3] = time; }
	    if ( $field[4] == 0 ) { $field[4] = time; }
	    $field[20] =~ s/\n/ /g;
	    $field[20] =~ s/\f/ /g;
	    $field[20] =~ s/<br>/ /ig;
	    $field[20] =~ s/&/&amp;/g;
	    $field[20] =~ s/"/&quot;/g;
	    $field[20] =~ s/'/&apos;/g;
	    $field[20] =~ s/</&lt;/g;
	    $field[20] =~ s/>/&gt;/g;
	    $el_host->{MonitorStatus}              = $field[2];
	    $el_host->{LastCheckTime}              = time_text( $field[3] );
	    $el_host->{LastStateChange}            = time_text( $field[4] );
	    $el_host->{isAcknowledged}             = $field[5];
	    $el_host->{TimeUp}                     = $field[6];
	    $el_host->{TimeDown}                   = $field[7];
	    $el_host->{TimeUnreachable}            = $field[8];
	    $el_host->{LastNotificationTime}       = time_text( $field[9] );
	    $el_host->{CurrentNotificationNumber}  = $field[10];
	    $el_host->{isNotificationsEnabled}     = $field[11];
	    $el_host->{isEventHandlersEnabled}     = $field[12];
	    $el_host->{isChecksEnabled}            = $field[13];
	    $el_host->{isFlapDetectionEnabled}     = $field[14];
	    $el_host->{isHostIsFlapping}           = $field[15];
	    $el_host->{PercentStateChange}         = $field[16];
	    $el_host->{ScheduledDowntimeDepth}     = $field[17];
	    $el_host->{isFailurePredictionEnabled} = $field[18];
	    $el_host->{isProcessPerformanceData}   = $field[19];
	    $el_host->{LastPluginOutput}           = $field[20];
	}
	elsif ( $msgtype =~ /PROGRAM/ ) {
	}
    }
    close STATUSFILE;
    return $element_ref;
}

sub get_status_v2 {
    my $statusfile = shift;
    my ( $timestamp, $msgtype );
    my @field;
    my $element_ref;

    # FIX FUTURE:  don't just abort on failure; retry 3 times or so
    if ( !open( STATUSFILE, '<:unix:mmap', $statusfile ) ) {
	freeze_logtime();
	print               "Error opening file $statusfile: $!\n";
	print LOG "${logtime}Error opening file $statusfile: $!\n";
	sleep $failure_sleep_time;
	return undef;
    }
    my $state     = '';
    my %attribute = ();
    while ( my $line = <STATUSFILE> ) {
	chomp $line;
	if ( $line =~ /^\s*\#]/ ) { next; }
	if ( !$state and ( $line =~ /\s*host \{/ ) ) {
	    $state = 'Host';
	    next;
	}
	elsif ( !$state and ( $line =~ /\s*service \{/ ) ) {
	    $state = 'Service';
	    next;
	}
	elsif ( ( $state eq 'Service' ) and ( $line =~ /^\s*\}/ ) and $attribute{host_name} and $attribute{service_description} ) {
	    my $el_svc = \%{ $element_ref->{Host}->{ $attribute{host_name} }->{Service}->{ $attribute{service_description} } };
	    if ( ( $attribute{last_check} == 0 ) and ( $attribute{has_been_checked} == 0 ) ) {
		## $attribute{last_check} = time;
		$el_svc->{MonitorStatus} = "PENDING";
	    }
	    else {
		$el_svc->{MonitorStatus} = $ServiceStatus{ $attribute{current_state} };
	    }

	    # Set element hash
	    # Map Nagios V2 status parameters to Nagios V1 definitions in Collage
	    $el_svc->{StateType}   = $StateType{ $attribute{state_type} };
	    $el_svc->{RetryNumber} = $attribute{current_attempt};

	    ## if ($attribute{last_check} == 0) { $attribute{last_check} = time;	}

	    if ($attribute{plugin_output}) {
		$attribute{plugin_output} =~ s/\n/ /g;
		$attribute{plugin_output} =~ s/\f/ /g;
		$attribute{plugin_output} =~ s/<br>/ /ig;
		$attribute{plugin_output} =~ s/&/&amp;/g;
		$attribute{plugin_output} =~ s/"/&quot;/g;
		$attribute{plugin_output} =~ s/'/&apos;/g;
		$attribute{plugin_output} =~ s/</&lt;/g;
		$attribute{plugin_output} =~ s/>/&gt;/g;
	    }

	    if ( $attribute{last_state_change} == 0 ) { $attribute{last_state_change} = time; }
	    ## Collage expects latency in integer. Set to ms
	    $attribute{check_latency} = int( 1000 * $attribute{check_latency} );
	    ## Collage expects execution time in integer. Set to ms
	    $attribute{check_execution_time} = int( 1000 * $attribute{check_execution_time} );

	    $el_svc->{CheckType}                  = $CheckType{ $attribute{check_type} };
	    $el_svc->{CurrentNotificationNumber}  = $attribute{current_notification_number};
	    $el_svc->{ExecutionTime}              = $attribute{check_execution_time};
	    $el_svc->{LastCheckTime}              = time_text( $attribute{last_check} );
	    $el_svc->{LastHardState}              = $ServiceStatus{ $attribute{last_hard_state} };
	    $el_svc->{LastNotificationTime}       = time_text( $attribute{last_notification} );
	    $el_svc->{LastPluginOutput}           = $attribute{plugin_output};
	    $el_svc->{LastStateChange}            = time_text( $attribute{last_state_change} );
	    $el_svc->{Latency}                    = $attribute{check_latency};
	    $el_svc->{NextCheckTime}              = time_text( $attribute{next_check} );
	    $el_svc->{PercentStateChange}         = $attribute{percent_state_change};
	    $el_svc->{ScheduledDowntimeDepth}     = $attribute{scheduled_downtime_depth};
	    $el_svc->{TimeCritical}               = $attribute{last_time_critical};
	    $el_svc->{TimeOK}                     = $attribute{last_time_ok};
	    $el_svc->{TimeUnknown}                = $attribute{last_time_unknown};
	    $el_svc->{TimeWarning}                = $attribute{last_time_warning};
	    $el_svc->{isAcceptPassiveChecks}      = $attribute{passive_checks_enabled};
	    $el_svc->{isChecksEnabled}            = $attribute{active_checks_enabled};
	    $el_svc->{isEventHandlersEnabled}     = $attribute{event_handler_enabled};
	    $el_svc->{isFailurePredictionEnabled} = $attribute{failure_prediction_enabled};
	    $el_svc->{isFlapDetectionEnabled}     = $attribute{flap_detection_enabled};
	    $el_svc->{isNotificationsEnabled}     = $attribute{notifications_enabled};
	    $el_svc->{isObsessOverService}        = $attribute{obsess_over_service};
	    $el_svc->{isProblemAcknowledged}      = $attribute{problem_has_been_acknowledged};
	    $el_svc->{isProcessPerformanceData}   = $attribute{process_performance_data};
	    $el_svc->{isServiceFlapping}          = $attribute{is_flapping};

	    # reset variables for next object
	    $state     = '';
	    %attribute = ();
	    next;
	}
	elsif ( ( $state eq 'Host' ) and ( $line =~ /\s*\}/ ) and $attribute{host_name} ) {
	    my $el_host = \%{ $element_ref->{Host}->{ $attribute{host_name} } };

	    if ($attribute{plugin_output}) {
		$attribute{plugin_output} =~ s/\n/ /g;
		$attribute{plugin_output} =~ s/\f/ /g;
		$attribute{plugin_output} =~ s/<br>/ /ig;
		$attribute{plugin_output} =~ s/&/&amp;/g;
		$attribute{plugin_output} =~ s/"/&quot;/g;
		$attribute{plugin_output} =~ s/'/&apos;/g;
		$attribute{plugin_output} =~ s/</&lt;/g;
		$attribute{plugin_output} =~ s/>/&gt;/g;
	    }

	    if ( ( $attribute{last_check} == 0 ) and ( $attribute{has_been_checked} == 0 ) ) {
		## $attribute{last_check} = time;
		$el_host->{MonitorStatus} = "PENDING";
	    }
	    else {
		$el_host->{MonitorStatus} = $HostStatus{ $attribute{current_state} };
	    }

	    if ( $attribute{last_state_change} == 0 ) { $attribute{last_state_change} = time; }

	    $el_host->{CheckType}                  = $CheckType{ $attribute{check_type} };
	    $el_host->{CurrentNotificationNumber}  = $attribute{current_notification_number};
	    $el_host->{LastCheckTime}              = time_text( $attribute{last_check} );
	    $el_host->{LastNotificationTime}       = time_text( $attribute{last_notification} );
	    $el_host->{LastPluginOutput}           = $attribute{plugin_output};
	    $el_host->{LastStateChange}            = time_text( $attribute{last_state_change} );
	    $el_host->{PercentStateChange}         = $attribute{percent_state_change};
	    $el_host->{ScheduledDowntimeDepth}     = $attribute{scheduled_downtime_depth};
	    $el_host->{TimeDown}                   = $attribute{last_time_down};
	    $el_host->{TimeUnreachable}            = $attribute{last_time_unreachable};
	    $el_host->{TimeUp}                     = $attribute{last_time_up};
	    $el_host->{isAcknowledged}             = $attribute{problem_has_been_acknowledged};
	    $el_host->{isChecksEnabled}            = $attribute{active_checks_enabled};
	    $el_host->{isEventHandlersEnabled}     = $attribute{event_handler_enabled};
	    $el_host->{isFailurePredictionEnabled} = $attribute{failure_prediction_enabled};
	    $el_host->{isFlapDetectionEnabled}     = $attribute{flap_detection_enabled};
	    $el_host->{isHostFlapping}             = $attribute{is_flapping};
	    $el_host->{isNotificationsEnabled}     = $attribute{notifications_enabled};
	    $el_host->{isPassiveChecksEnabled}     = $attribute{passive_checks_enabled};
	    $el_host->{isProcessPerformanceData}   = $attribute{process_performance_data};

	    # reset variables for next object
	    $state     = '';
	    %attribute = ();
	    next;
	}
	if ( $state and ( $line =~ /\s*(\S+?)=(.*)/ ) ) {
	    if ( $2 ne '' ) {
		$attribute{$1} = $2;
	    }
	}
	else { next; }
    }
    close STATUSFILE;
    return $element_ref;
}

sub get_status_v3 {
    my $statusfile = shift;
    my ( $timestamp, $msgtype );
    my @field;
    my $element_ref;

    # FIX FUTURE:  don't just abort on failure; retry 3 times or so
    if ( !open( STATUSFILE, '<:unix:mmap', $statusfile ) ) {
	freeze_logtime();
	print               "Error opening file $statusfile: $!\n";
	print LOG "${logtime}Error opening file $statusfile: $!\n";
	sleep $failure_sleep_time;
	return undef;
    }
    my $state          = '';
    my $hostcomment    = undef;
    my $servicecomment = undef;
    my %attribute      = ();
    while ( my $line = <STATUSFILE> ) {
	if ($shutdown_requested) {
	    return undef;
	}
	chomp $line;
	if ( $line =~ /^\s*\#]/ ) { next; }
	if ( !$state and ( $line =~ /\s*host(?:status)?\s*\{/ ) ) {
	    $state = 'Host';
	    $n_hostcount++;
	    next;
	}
	elsif ( !$state and ( $line =~ /\s*service(?:status)?\s*\{/ ) ) {
	    $state = 'Service';
	    $n_servicecount++;
	    next;
	}
	elsif ( ( $state eq 'Service' ) and ( $line =~ /^\s*\}/ ) and $attribute{host_name} and $attribute{service_description} ) {
	    my $el_svc = \%{ $element_ref->{Host}->{ $attribute{host_name} }->{Service}->{ $attribute{service_description} } };

	    # Check for pending service status
	    if ( ( $attribute{last_check} == 0 ) and ( $attribute{has_been_checked} == 0 ) ) {
		$el_svc->{MonitorStatus} = "PENDING";
	    }
	    else {
		$el_svc->{MonitorStatus} = $ServiceStatus{ $attribute{current_state} };
	    }

	    if ($attribute{plugin_output}) {
		$attribute{plugin_output} =~ s/\n/ /g;
		$attribute{plugin_output} =~ s/\f/ /g;
		$attribute{plugin_output} =~ s/<br>/ /ig;
		$attribute{plugin_output} =~ s/&/&amp;/g;
		$attribute{plugin_output} =~ s/"/&quot;/g;
		$attribute{plugin_output} =~ s/'/&apos;/g;
		$attribute{plugin_output} =~ s/</&lt;/g;
		$attribute{plugin_output} =~ s/>/&gt;/g;
	    }

	    if ($attribute{long_plugin_output}) {
		$attribute{long_plugin_output} =~ s/\n/ /g;
		$attribute{long_plugin_output} =~ s/\f/ /g;
		$attribute{long_plugin_output} =~ s/<br>/ /ig;
		$attribute{long_plugin_output} =~ s/&/&amp;/g;
		$attribute{long_plugin_output} =~ s/"/&quot;/g;
		$attribute{long_plugin_output} =~ s/'/&apos;/g;
		$attribute{long_plugin_output} =~ s/</&lt;/g;
		$attribute{long_plugin_output} =~ s/>/&gt;/g;
	    }

	    if ( $attribute{last_state_change} == 0 ) { $attribute{last_state_change} = time; }
	    ## Collage expects latency in integer. Set to ms
	    $attribute{check_latency} = int( 1000 * $attribute{check_latency} );
	    ## Collage expects execution time in integer. Set to ms
	    $attribute{check_execution_time} = int( 1000 * $attribute{check_execution_time} );

	    my $short_output = $attribute{plugin_output};
	    my $long_output  = $attribute{long_plugin_output};
	    my $plugin_output =
		(defined($short_output) && defined($long_output)) ? "$short_output $long_output" :
		defined($short_output) ? $short_output : $long_output;

	    # Set element hash
	    # Map Nagios V2 status parameters to Nagios V1 definitions in Collage
	    $el_svc->{CheckType}                  = $CheckType{ $attribute{check_type} };
	    $el_svc->{CurrentAttempt}             = $attribute{current_attempt};
	    $el_svc->{CurrentNotificationNumber}  = $attribute{current_notification_number};
	    $el_svc->{ExecutionTime}              = $attribute{check_execution_time};
	    $el_svc->{LastCheckTime}              = time_text( $attribute{last_check} );
	    $el_svc->{LastHardState}              = $ServiceStatus{ $attribute{last_hard_state} };
	    $el_svc->{LastNotificationTime}       = time_text( $attribute{last_notification} );
	    $el_svc->{LastPluginOutput}           = $plugin_output;
	    $el_svc->{LastStateChange}            = time_text( $attribute{last_state_change} );
	    $el_svc->{Latency}                    = $attribute{check_latency};
	    $el_svc->{MaxAttempts}                = $attribute{max_attempts};
	    $el_svc->{NextCheckTime}              = time_text( $attribute{next_check} );
	    $el_svc->{PercentStateChange}         = $attribute{percent_state_change};
	    $el_svc->{RetryNumber}                = $attribute{current_attempt};
	    $el_svc->{ScheduledDowntimeDepth}     = $attribute{scheduled_downtime_depth};
	    $el_svc->{StateType}                  = $StateType{ $attribute{state_type} };
	    $el_svc->{TimeCritical}               = $attribute{last_time_critical};
	    $el_svc->{TimeOK}                     = $attribute{last_time_ok};
	    $el_svc->{TimeUnknown}                = $attribute{last_time_unknown};
	    $el_svc->{TimeWarning}                = $attribute{last_time_warning};
	    $el_svc->{isAcceptPassiveChecks}      = $attribute{passive_checks_enabled};
	    $el_svc->{isChecksEnabled}            = $attribute{active_checks_enabled};
	    $el_svc->{isEventHandlersEnabled}     = $attribute{event_handler_enabled};
	    $el_svc->{isFailurePredictionEnabled} = $attribute{failure_prediction_enabled};
	    $el_svc->{isFlapDetectionEnabled}     = $attribute{flap_detection_enabled};
	    $el_svc->{isNotificationsEnabled}     = $attribute{notifications_enabled};
	    $el_svc->{isObsessOverService}        = $attribute{obsess_over_service};
	    $el_svc->{isProblemAcknowledged}      = $attribute{problem_has_been_acknowledged};
	    $el_svc->{isProcessPerformanceData}   = $attribute{process_performance_data};
	    $el_svc->{isServiceFlapping}          = $attribute{is_flapping};
	    ## Use global values to overide where needed
	    ## Obsession
	    if ( $global_nagios->{obsess_over_services} == 0 ) {
		 $el_svc->{isObsessOverService} = 0;
	    }
	    ## Notifications
	    if ( $global_nagios->{enable_notifications} == 0 ) {
		 $el_svc->{isNotificationsEnabled} = 0;
	    }
	    ## Active Checks
	    if ( $global_nagios->{active_service_checks_enabled} == 0 ) {
		 $el_svc->{isChecksEnabled} = 0;
	    }
	    ## Passive Checks
	    if ( $global_nagios->{passive_service_checks_enabled} == 0 ) {
		 $el_svc->{isAcceptPassiveChecks} = 0;
	    }
	    ## Flap Detection
	    if ( $global_nagios->{enable_flap_detection} == 0 ) {
		 $el_svc->{isFlapDetectionEnabled} = 0;
	    }
	    ## Event Handlers
	    if ( $global_nagios->{enable_event_handlers} == 0 ) {
		 $el_svc->{isEventHandlersEnabled} = 0;
	    }
	    ## reset variables for next object
	    $state     = '';
	    %attribute = ();
	    next;
	}
	elsif ( ( $state eq 'Host' ) and ( $line =~ /\s*\}/ ) and $attribute{host_name} ) {
	    my $el_host = \%{ $element_ref->{Host}->{ $attribute{host_name} } };

	    if ($attribute{plugin_output}) {
		$attribute{plugin_output} =~ s/\n/ /g;
		$attribute{plugin_output} =~ s/\f/ /g;
		$attribute{plugin_output} =~ s/<br>/ /ig;
		$attribute{plugin_output} =~ s/&/&amp;/g;
		$attribute{plugin_output} =~ s/"/&quot;/g;
		$attribute{plugin_output} =~ s/'/&apos;/g;
		$attribute{plugin_output} =~ s/</&lt;/g;
		$attribute{plugin_output} =~ s/>/&gt;/g;
	    }

	    if ( ( $attribute{last_check} == 0 ) and ( $attribute{has_been_checked} == 0 ) ) {
		## $attribute{last_check} = time;
		$el_host->{MonitorStatus} = "PENDING";
	    }
	    else {
		$el_host->{MonitorStatus} = $HostStatus{ $attribute{current_state} };
	    }

	    if ( $attribute{last_state_change} == 0 ) { $attribute{last_state_change} = time; }
	    ## Collage expects latency in integer. Set to ms
	    $attribute{check_latency} = int( 1000 * $attribute{check_latency} );
	    ## Collage expects execution time in integer. Set to ms
	    $attribute{check_execution_time} = int( 1000 * $attribute{check_execution_time} );

	    $el_host->{CheckType}                  = $CheckType{ $attribute{check_type} };
	    $el_host->{CurrentAttempt}             = $attribute{current_attempt};
	    $el_host->{CurrentNotificationNumber}  = $attribute{current_notification_number};
	    $el_host->{ExecutionTime}              = $attribute{check_execution_time};
	    $el_host->{LastCheckTime}              = time_text( $attribute{last_check} );
	    $el_host->{LastNotificationTime}       = time_text( $attribute{last_notification} );
	    $el_host->{LastPluginOutput}           = $attribute{plugin_output};
	    $el_host->{LastStateChange}            = time_text( $attribute{last_state_change} );
	    $el_host->{Latency}                    = $attribute{check_latency};
	    $el_host->{MaxAttempts}                = $attribute{max_attempts};
	    $el_host->{NextCheckTime}              = time_text( $attribute{next_check} );
	    $el_host->{PercentStateChange}         = $attribute{percent_state_change};
	    $el_host->{ScheduledDowntimeDepth}     = $attribute{scheduled_downtime_depth};
	    $el_host->{StateType}                  = $StateType{ $attribute{state_type} };
	    $el_host->{TimeDown}                   = $attribute{last_time_down};
	    $el_host->{TimeUnreachable}            = $attribute{last_time_unreachable};
	    $el_host->{TimeUp}                     = $attribute{last_time_up};
	    $el_host->{isAcknowledged}             = $attribute{problem_has_been_acknowledged};
	    $el_host->{isChecksEnabled}            = $attribute{active_checks_enabled};
	    $el_host->{isEventHandlersEnabled}     = $attribute{event_handler_enabled};
	    $el_host->{isFailurePredictionEnabled} = $attribute{failure_prediction_enabled};
	    $el_host->{isFlapDetectionEnabled}     = $attribute{flap_detection_enabled};
	    $el_host->{isHostFlapping}             = $attribute{is_flapping};
	    $el_host->{isNotificationsEnabled}     = $attribute{notifications_enabled};
	    $el_host->{isObsessOverHost}           = $attribute{obsess_over_host};
	    $el_host->{isPassiveChecksEnabled}     = $attribute{passive_checks_enabled};
	    $el_host->{isProcessPerformanceData}   = $attribute{process_performance_data};
	    ## Use global values where needed
	    ## Obsession
	    if ( $global_nagios->{obsess_over_hosts} == 0 ) {
		$el_host->{isObsessOverHost} = 0;
	    }
	    ## Notifications
	    if ( $global_nagios->{enable_notifications} == 0 ) {
		 $el_host->{isNotificationsEnabled} = 0;
	    }
	    ## Active Checks
	    if ( $global_nagios->{active_host_checks_enabled} == 0 ) {
		 $el_host->{isChecksEnabled} = 0;
	    }
	    ## Passive Checks
	    if ( $global_nagios->{passive_host_checks_enabled} == 0 ) {
		 $el_host->{isPassiveChecksEnabled} = 0;
	    }
	    ## Flap Detection
	    if ( $global_nagios->{enable_flap_detection} == 0 ) {
		 $el_host->{isFlapDetectionEnabled} = 0;
	    }
	    ## Event Handlers
	    if ( $global_nagios->{enable_event_handlers} == 0 ) {
		 $el_host->{isEventHandlersEnabled} = 0;
	    }
	    # reset variables for next object
	    $state     = '';
	    %attribute = ();
	    next;
	}
	if ( $state and ( $line =~ /\s*(\S+?)=(.*)/ ) ) {
	    if ( $2 ne '' ) {
		$attribute{$1} = $2;
	    }
	}
	if ( $line =~ /\s*hostcomment\s*\{/ ) {
	    $hostcomment = 1;
	    next;
	}
	elsif ( $line =~ /\s*servicecomment\s*\{/ ) {
	    $servicecomment = 1;
	    next;
	}
	elsif ( $hostcomment and ( $line =~ /\s*(\S+?)=(.*)/ ) ) {
	    if ( $2 ne '' ) {
		$attribute{$1} = $2;
	    }
	}
	elsif ( $hostcomment and ( $line =~ /\s*\}/ ) and $attribute{host_name} ) {
	    ## Assign host comment attributes
	    my ( $sec, $min, $hour, $mday, $mon, $year, $wday, $yday, $isdst ) = localtime( $attribute{entry_time} );
	    my $entrytime = sprintf "%02d-%02d-%4d %02d:%02d:%02d", $mon + 1, $mday, $year + 1900, $hour, $min, $sec;
	    $attribute{comment_data} =~ s/'//g;
	    $attribute{comment_data} =~ s/"//g;
	    $element_ref->{Host}->{ $attribute{host_name} }->{Comments} .=
	      "#!#$attribute{comment_id};::;$entrytime;::;$attribute{author};::;\'$attribute{comment_data}\'";
	    $hostcomment = undef;
	}
	elsif ( $servicecomment and ( $line =~ /\s*(\S+?)=(.*)/ ) ) {
	    if ( $2 ne '' ) {
		$attribute{$1} = $2;
	    }
	}
	elsif ( $servicecomment and ( $line =~ /\s*\}/ ) and $attribute{host_name} ) {
	    ## Assign service comment attributes
	    my ( $sec, $min, $hour, $mday, $mon, $year, $wday, $yday, $isdst ) = localtime( $attribute{entry_time} );
	    my $entrytime = sprintf "%02d-%02d-%4d %02d:%02d:%02d", $mon + 1, $mday, $year + 1900, $hour, $min, $sec;
	    $attribute{comment_data} =~ s/'//g;
	    $attribute{comment_data} =~ s/"//g;
	    $element_ref->{Host}->{ $attribute{host_name} }->{Service}->{ $attribute{service_description} }->{Comments} .=
	      "#!#$attribute{comment_id};::;$entrytime;::;$attribute{author};::;\'$attribute{comment_data}\'";
	    $servicecomment = undef;
	}
	else { next; }
    }
    close STATUSFILE;

    # Fix all the comments (once)
    my $comment = undef;
    foreach my $hostkey ( keys( %{ $element_ref->{Host} } ) ) {
	my $el_host = \%{ $element_ref->{Host}->{$hostkey} };
	$comment = $el_host->{Comments};
	if ( defined $comment ) {
	    $comment =~ s/\n/ /g;
	    $comment =~ s/\f/ /g;
	    $comment =~ s/<br>/ /ig;
	    $comment =~ s/&/&amp;/g;
	    $comment =~ s/"/&quot;/g;
	    $comment =~ s/'/&apos;/g;
	    $comment =~ s/</&lt;/g;
	    $comment =~ s/>/&gt;/g;
	    $el_host->{Comments} = $comment;
	    print LOG "*** Host Comments for host $hostkey: $comment\n" if $debug_debug;
	}
	else {
	    $el_host->{Comments} = ' ';
	}
	foreach my $servicekey ( keys( %{ $el_host->{Service} } ) ) {
	    my $el_svc = \%{ $el_host->{Service}->{$servicekey} };
	    $comment = $el_svc->{Comments};
	    if ( defined $comment ) {
		$comment =~ s/\n/ /g;
		$comment =~ s/\f/ /g;
		$comment =~ s/<br>/ /ig;
		$comment =~ s/&/&amp;/g;
		$comment =~ s/"/&quot;/g;
		$comment =~ s/'/&apos;/g;
		$comment =~ s/</&lt;/g;
		$comment =~ s/>/&gt;/g;
		$el_svc->{Comments} = $comment;
		print LOG "*** Service Comments for host $hostkey, service $servicekey: $comment\n" if $debug_debug;
	    }
	    else {
		$el_svc->{Comments} = ' ';
	    }
	}
    }
    return $element_ref;
}

sub get_globals {
    my $statusfile = shift;
    my ( $timestamp, $msgtype );
    my @field;

    # FIX FUTURE:  don't just abort on failure; retry 3 times or so
    if ( !open( STATUSFILE, '<:unix:mmap', $statusfile ) ) {
	freeze_logtime();
	print               "Error opening file $statusfile: $!\n";
	print LOG "${logtime}Error opening file $statusfile: $!\n";
	sleep $failure_sleep_time;
	return undef;
    }
    my $state     = '';
    my $attribute = {};
    while ( my $line = <STATUSFILE> ) {
	chomp $line;
	if ( $line =~ /^\s*\#]/ ) { next; }
	if ( !$state and ( $line =~ /\s*program(?:status)?\s*\{/ ) ) {
	    $state = 'Global';
	    next;
	}
	## Reading the globals in ...
	if ( $state and ( $line =~ /\s*(\S+?)=(.*)/ ) ) {
	    if ( $2 ne '' ) {
		$attribute->{$1} = $2;
		print LOG "Global Attribute found: $1 = $2\n" if $debug_debug;
	    }
	}
	if ( $state and $line =~ /\s*\}/ ) {
	    # we are done reading globals
	    last;
	}
    }
    close STATUSFILE;
    return $attribute;
}

# This routine is no longer called from anywhere.
sub readNagiosfeedersConfig {
    my $type         = shift;
    my $database     = undef;
    my $dbhost       = undef;
    my $username     = undef;
    my $password     = undef;
    my $gwconfigfile = "/usr/local/groundwork/config/db.properties";
    if ( $type !~ /^(collage|insightreports)$/ ) { return "ERROR: Invalid database type."; }
    if ( !open( CONFIG, "$gwconfigfile" ) ) {
	return "ERROR: Unable to find configuration file $gwconfigfile";
    }
    ## collage.username=collage
    ## collage.password=gwrk
    ## collage.database=GWCollageDB
    ## collage.dbhost = localhost
    while ( my $line = <CONFIG> ) {
	chomp $line;
	if ( $line =~ /\s*$type\.(\S+)\s*=\s*(\S*)\s*/ ) {
	    if ( $1 eq "username" ) {
		$username = $2;
	    }
	    elsif ( $1 eq "password" ) {
		$password = $2;
	    }
	    elsif ( $1 eq "database" ) {
		$database = $2;
	    }
	    elsif ( $1 eq "dbhost" ) {
		$dbhost = $2;
	    }
	}
    }
    close CONFIG;
    return ( $database, $dbhost, $username, $password );
}

sub build_host_xml {
    my $thisnagios      = shift;
    my $element_ref     = shift;
    my $collage_ref     = shift;
    my $state_changes   = shift;  # arrayref or undef
    my $insertcount     = 0;
    my $skipcount       = 0;
    my @output          = ();
    my %HostStatusCodes = ( "2" => "UP", "4" => "DOWN", "8" => "UNREACHABLE" );
    my $el_hosts        = $element_ref->{Host};
    my $cs_hosts        = $collage_ref->{Host};

    # Create XML stream -- Format:
    # <{SERVICE_STATUS | HOST_STATUS | LOG_MESSAGE} database field=value | database field=value | ... />
    # <HOST_STATUS  database field=value | database field=value | ... />

    foreach my $hostkey ( keys %{ $el_hosts } ) {

	if ($shutdown_requested) {
	    log_shutdown();
	    return undef;
	}

	# if no host status change then don't send
	my $host_xml = '';
	if ($smart_update) {
	    $host_xml = hostStatusChangeXML( $el_hosts, $cs_hosts, $hostkey, $state_changes );
	    if ( !$host_xml ) {
		$skipcount++;
		next;
	    }
	    if ( $host_xml eq $restart_xml ) {
		return undef;
	    }
	}

	my @xml_message = ();
	push @xml_message, '<Host ';

	# default identification -- set to IP address if known
	push @xml_message, "MonitorServerName=\"$thisnagios\" ";

	# default identification -- set to IP address if known
	push @xml_message, "Host=\"$hostkey\" ";

	# Monarch Sync now sets the IP as Identification. We should use address field from Monarch, whatever that is.
	# It's possible that the address changed, or that we are feeding a result for a host that
	# was not in Monarch when this program started. If the Identification is blank, reload the cache.
	if ( $hostipaddress{$hostkey} ) {
	    ## Set Device to IP
	    push @xml_message, "Device=\"$hostipaddress{$hostkey}\" ";
	}
	else {
	    ## For some reason we don't know the IP. Might be a new host? Anyway, reload and try one time more.
	    load_cached_addresses() or return undef;
	    if ( $hostipaddress{$hostkey} ) {
		## Set Device to IP
		push @xml_message, "Device=\"$hostipaddress{$hostkey}\" ";
	    }
	    else {
		## Bail out and set Device = hostname. There is something wrong with the local Monarch DB.
		push @xml_message, "Device=\"$hostkey\" ";
	    }
	}
	if ($smart_update) {
	    push @xml_message, $host_xml;
	}
	else {
	    my $el_host = $el_hosts->{$hostkey};
	    foreach my $field ( keys %{ $el_host } ) {
		if ( $field eq 'Service' ) { next }    # skip the Service hash key
		my $tmpinfo = $el_host->{$field};
		$tmpinfo =~ s/"/'/g;
		push @xml_message, "$field=\"$tmpinfo\" ";
	    }
	}
	push @xml_message, "/>\n";

	push( @output, join( '', @xml_message ) );
	if ($smart_update) {
	    hostStatusUpdate( $element_ref, $collage_ref, $hostkey );
	}
	$insertcount++;
	if ( ( $insertcount % 100 ) == 0 ) {
	    print     "Queueing hosts for insert, count=$insertcount\n" if $debug_summary;
	    print LOG "Queueing hosts for insert, count=$insertcount\n" if $debug_summary;
	}
    }
    freeze_logtime();
    if ($smart_update) {
	print     "${logtime}Total Hosts Queued for Insert Count=$insertcount. No status change for $skipcount hosts.\n" if $debug_summary;
	print LOG "${logtime}Total Hosts Queued for Insert Count=$insertcount. No status change for $skipcount hosts.\n" if $debug_summary;
    }
    else {
	print     "${logtime}Total Hosts Queued for Insert Count=$insertcount.\n" if $debug_summary;
	print LOG "${logtime}Total Hosts Queued for Insert Count=$insertcount.\n" if $debug_summary;
    }
    return \@output;
}

sub build_service_xml {
    my $thisnagios    = shift;
    my $element_ref   = shift;
    my $collage_ref   = shift;
    my $state_changes = shift;  # arrayref or undef
    my $insertcount   = 0;
    my $skipcount     = 0;
    my @output        = ();
    my $el_hosts      = $element_ref->{Host};
    my $cs_hosts      = $collage_ref->{Host};

    # Create XML stream -- Format:
    # <{SERVICE_STATUS | HOST_STATUS | LOG_MESSAGE} database field=value | database field=value | ... />
    # <SERVICE_STATUS  database field=value | database field=value | ... />

    foreach my $hostkey ( keys %{$el_hosts} ) {

	if ($shutdown_requested) {
	    log_shutdown();
	    return undef;
	}

	my $el_svcs = $el_hosts->{$hostkey}->{Service};
	my $cs_svcs = $cs_hosts->{$hostkey}->{Service};

	foreach my $servicekey ( keys %{$el_svcs} ) {

	    # if no service status change, then don't send
	    my $service_xml = '';
	    if ($smart_update) {
		$service_xml = serviceStatusChangeXML( $el_svcs, $cs_svcs, $hostkey, $servicekey, $state_changes );
		if ( !$service_xml ) {
		    $skipcount++;
		    next;
		}
		if ( $service_xml eq $restart_xml ) {
		    return undef;
		}
	    }

	    my @xml_message = ();
	    push @xml_message, '<Service ';    # Start message tag

	    # default identification -- set to IP address if known
	    push @xml_message, "MonitorServerName=\"$thisnagios\" ";

	    # default identification -- set to IP address if known
	    push @xml_message, "Host=\"$hostkey\" ";

	    # Monarch Sync now sets the IP as Identification. We should use address field from Monarch, whatever that is.
	    # It's possible that the address changed, or that we are feeding a result for a host that
	    # was not in Monarch when this program started. If the Identification is blank, reload the cache.
	    if ( $hostipaddress{$hostkey} ) {
		## Set Device to IP
		push @xml_message, "Device=\"$hostipaddress{$hostkey}\" ";
	    }
	    else {
		## For some reason we don't know the IP. Might be a new host? Anyway, reload and try one time more.
		load_cached_addresses() or return undef;
		if ( $hostipaddress{$hostkey} ) {
		    ## Set Device to IP
		    push @xml_message, "Device=\"$hostipaddress{$hostkey}\" ";
		}
		else {
		    ## Bail out and set Device = hostname. There is something wrong with the local Monarch DB.
		    push @xml_message, "Device=\"$hostkey\" ";
		}
	    }
	    push @xml_message, "ServiceDescription=\"$servicekey\" ";
	    if ($smart_update) {
		push @xml_message, $service_xml;
	    }
	    else {
		my $el_svc = $el_svcs->{$servicekey};
		foreach my $field ( keys %{$el_svc} ) {
		    my $tmpinfo = $el_svc->{$field};
		    $tmpinfo =~ s/"/'/g;
		    push @xml_message, "$field=\"$tmpinfo\" ";
		}
	    }
	    push @xml_message, "/>\n";

	    push( @output, join( '', @xml_message ) );
	    if ($smart_update) {
		serviceStatusUpdate( $element_ref, $collage_ref, $hostkey, $servicekey );
	    }
	    $insertcount++;
	    if ( ( $insertcount % 100 ) == 0 ) {
		print     "Queueing services for insert, count=$insertcount\n" if $debug_summary;
		print LOG "Queueing services for insert, count=$insertcount\n" if $debug_summary;
	    }
	}
    }
    freeze_logtime();
    if ($smart_update) {
	print     "${logtime}Total Services Queued for Insert Count=$insertcount. No status change for $skipcount services.\n" if $debug_summary;
	print LOG "${logtime}Total Services Queued for Insert Count=$insertcount. No status change for $skipcount services.\n" if $debug_summary;
    }
    else {
	print     "${logtime}Total Services Queued for Insert Count=$insertcount.\n" if $debug_summary;
	print LOG "${logtime}Total Services Queued for Insert Count=$insertcount.\n" if $debug_summary;
    }
    return \@output;
}

sub push_host_state_change {
    my $host          = shift;
    my $el_host       = shift;
    my $state_changes = shift;

    my $el_status = $el_host->{MonitorStatus};
    if ( defined($el_status) && $el_status !~ /PENDING/ ) {
	my $check_state = ( $el_status =~ /UP/ ) ? 0 : 1;
	## Reverse the XML Substitution needed for Foundation in the status text.
	my $host_text = $el_host->{LastPluginOutput};
	$host_text =~ s/&amp;/&/g;
	$host_text =~ s/&quot;/"/g;
	$host_text =~ s/&apos;/'/g;
	$host_text =~ s/&lt;/</g;
	$host_text =~ s/&gt;/>/g;
	push @$state_changes, "$host\t$check_state\t$host_text|\n";
    }
}

sub push_service_state_change {
    my $host          = shift;
    my $service       = shift;
    my $el_svc        = shift;
    my $state_changes = shift;

    my $el_status = $el_svc->{MonitorStatus};
    if ( defined($el_status) && $el_status !~ /PENDING/ ) {
	my $check_state = ( $el_status =~ /OK/ ) ? 0 : ( $el_status =~ /WARNING/ ) ? 1 : ( $el_status =~ /CRITICAL/ ) ? 2 : 3;
	my $service_text = $el_svc->{LastPluginOutput};
	$service_text =~ s/&amp;/&/g;
	$service_text =~ s/&quot;/"/g;
	$service_text =~ s/&apos;/'/g;
	$service_text =~ s/&lt;/</g;
	$service_text =~ s/&gt;/>/g;
	push @$state_changes, "$host\t$service\t$check_state\t$service_text|\n";
    }
}

# Note:  There is some potential system-level performance optimization we could do, by being slightly
# more discriminating about what we send to a remote server for a data-only change, for both host and
# service changes.  In some cases, we might perhaps be sending state data too often to the remote
# server Nagios, by effectively trying to mirror all the state changes we send to the local Foundation.
# Delving into that, to see if we should be more selective, awaits some future release.

sub hostStatusChangeXML {
    my $el_hosts      = shift;
    my $cs_hosts      = shift;
    my $hostkey       = shift;
    my $state_changes = shift;    # arrayref or undef
    my @host_xml      = ();
    my $el_host       = $el_hosts->{$hostkey};
    my $cs_host       = $cs_hosts->{$hostkey};
    my $data_change   = 0;
    my $el_host_field;
    my $cs_host_field;

    # We always need these fields if we send any XML ...
    foreach my $field qw(
	MonitorStatus
	ScheduledDowntimeDepth
	LastStateChange
    ) {
	$el_host_field = $el_host->{$field}; $el_host_field = '' if not defined $el_host_field;
	$cs_host_field = $cs_host->{$field}; $cs_host_field = '' if not defined $cs_host_field;
	my $tmpinfo = $el_host_field;
	$tmpinfo =~ s/"/'/g;
	push @host_xml, "$field=\"$tmpinfo\" ";
	if ( $el_host_field ne $cs_host_field ) {
	    $data_change = 1;
	}
    }
    # Check each condition that might require an update to the database status
#       isHostFlapping
#       isObsessOverHost
    foreach my $field qw(
	Comments
	CurrentNotificationNumber
	LastNotificationTime
	MaxAttempts
	StateType
	isAcknowledged
	isChecksEnabled
	isEventHandlersEnabled
	isFlapDetectionEnabled
	isNotificationsEnabled
	isPassiveChecksEnabled
    ) {
	$el_host_field = $el_host->{$field}; $el_host_field = '' if not defined $el_host_field;
	$cs_host_field = $cs_host->{$field}; $cs_host_field = '' if not defined $cs_host_field;
	if ( $el_host_field ne $cs_host_field ) {
	    my $tmpinfo = $el_host_field;
	    $tmpinfo =~ s/"/'/g;
	    push @host_xml, "$field=\"$tmpinfo\" ";
	    $data_change = 1;
	}
    }
    my $timing_change = 0;
    # Check each condition that might require an update to the timing change fields (sync only on heartbeat)
    foreach my $field qw(
	ExecutionTime
	Latency
	LastCheckTime
	NextCheckTime
	PercentStateChange
	CurrentAttempt
	LastPluginOutput
    ) {
	$el_host_field = $el_host->{$field}; $el_host_field = '' if not defined $el_host_field;
	$cs_host_field = $cs_host->{$field}; $cs_host_field = '' if not defined $cs_host_field;
	if ( $el_host_field ne $cs_host_field ) {
	    my $tmpinfo = $el_host_field;
	    $tmpinfo =~ s/"/'/g;
	    push @host_xml, "$field=\"$tmpinfo\" ";
	    $timing_change = 1;
	}
    }

    if ( ( $timing_change == 1 ) && ( $data_change == 0 ) ) {
	if ($heartbeat_mode || $el_host->{StateType} eq 'SOFT') {
	    # We may push host state changes to remote servers even if we're not in heartbeat mode,
	    # so the parent Nagios has a chance to clock its SOFT-to-HARD state machine.
	    push_host_state_change( $hostkey, $el_host, $state_changes ) if defined $state_changes;
	}
	if ($heartbeat_mode) {
	    print LOG "Accepting heartbeat change for host: $hostkey\n" if $debug_basic;
	    return join( '', @host_xml );
	}
	else {
	    print LOG "Rejecting change since it's just a timing update and we are not doing a heartbeat: $hostkey\n" if $debug_basic;
	    return $no_xml;
	}
    }
    if ( $data_change == 1 ) {
	## Check for "Pending Transition", so we can send an event and trigger a state change
	## when we go from PENDING to UP
	if ( ( $el_host->{MonitorStatus} eq "UP" ) and ( $cs_host->{MonitorStatus} ) eq "PENDING" ) {
	    my $queueing_status = queue_pending_host_event( $el_host, $hostkey );
	    return $restart_xml if $queueing_status != CONTINUE_STATUS;
	}

	print LOG Data::Dumper->Dump([\%{$cs_host}], [qw(\%{cs_hosts})]) if $debug_ridiculous;
	print LOG Data::Dumper->Dump([\%{$el_host}], [qw(\%{el_hosts})]) if $debug_ridiculous;
	print LOG "State changed for $hostkey -- should tell Foundation now\n" if $debug_basic;
	push_host_state_change( $hostkey, $el_host, $state_changes ) if defined $state_changes;
	return join( '', @host_xml );
    }
    return $no_xml;
}

sub serviceStatusChangeXML {
    my $el_svcs       = shift;
    my $cs_svcs       = shift;
    my $hostkey       = shift;
    my $servicekey    = shift;
    my $state_changes = shift;    # arrayref or undef
    my @service_xml   = ();
    my $el_svc        = $el_svcs->{$servicekey};
    my $cs_svc        = $cs_svcs->{$servicekey};
    my $data_change   = 0;
    my $el_svc_field;
    my $cs_svc_field;

    # We always need these fields if we send anything ...
    foreach my $field qw(
	MonitorStatus
	ScheduledDowntimeDepth
	LastStateChange
    ) {
	$el_svc_field = $el_svc->{$field}; $el_svc_field = '' if not defined $el_svc_field;
	$cs_svc_field = $cs_svc->{$field}; $cs_svc_field = '' if not defined $cs_svc_field;
	my $tmpinfo = $el_svc_field;
	$tmpinfo =~ s/"/'/g;
	push @service_xml, "$field=\"$tmpinfo\" ";
	# but don't miss a change to these ...
	if ( $el_svc_field ne $cs_svc_field ) {
	    $data_change = 1;
	}
    }
    # Check each condition that might require an update to the database status
#       isServiceFlapping
#       isObsessOverService
    foreach my $field qw(
	Comments
	CurrentNotificationNumber
	LastNotificationTime
	isAcceptPassiveChecks
	isChecksEnabled
	isEventHandlersEnabled
	isFlapDetectionEnabled
	isNotificationsEnabled
	isProblemAcknowledged
	MaxAttempts
	StateType
    ) {
	$el_svc_field = $el_svc->{$field}; $el_svc_field = '' if not defined $el_svc_field;
	$cs_svc_field = $cs_svc->{$field}; $cs_svc_field = '' if not defined $cs_svc_field;
	if ( $el_svc_field ne $cs_svc_field ) {
	    my $tmpinfo = $el_svc_field;
	    $tmpinfo =~ s/"/'/g;
	    push @service_xml, "$field=\"$tmpinfo\" ";
	    $data_change = 1;
	}
    }
    my $timing_change = 0;
    # Check fields that constitute a timing update (only sync on heartbeat)
    foreach my $field qw(
	LastCheckTime
	NextCheckTime
	Latency
	ExecutionTime
	PercentStateChange
	CurrentAttempt
	LastPluginOutput
    ) {
	$el_svc_field = $el_svc->{$field}; $el_svc_field = '' if not defined $el_svc_field;
	$cs_svc_field = $cs_svc->{$field}; $cs_svc_field = '' if not defined $cs_svc_field;
	if ( $el_svc_field ne $cs_svc_field ) {
	    my $tmpinfo = $el_svc_field;
	    $tmpinfo =~ s/"/'/g;
	    push @service_xml, "$field=\"$tmpinfo\" ";
	    $timing_change = 1;
	}
    }
    if ( ($timing_change == 1) && ($data_change == 0) ) {
	if ($heartbeat_mode || $el_svc->{StateType} eq 'SOFT') {
	    # We may push service state changes to remote servers even if we're not in heartbeat mode,
	    # so the parent Nagios has a chance to clock its SOFT-to-HARD state machine.
	    push_service_state_change( $hostkey, $servicekey, $el_svc, $state_changes ) if defined $state_changes;
	}
	if ($heartbeat_mode)  {
	    print LOG "Accepting heartbeat change for host: $hostkey and service $servicekey\n" if $debug_basic;
	    return join( '', @service_xml );
	} else {
	    print LOG "Rejecting change since it's just a timing update and we are not doing a heartbeat: $servicekey\n" if $debug_basic;
	    return $no_xml;
	}
    }
    if ($data_change == 1) {
	## Check for "Pending Transition", so we can send an event and trigger a state change
	## when we go from Pending to OK
	if ( ( $el_svc->{MonitorStatus} eq "OK" ) and ( $cs_svc->{MonitorStatus} ) eq "PENDING" ) {
	    my $queueing_status = queue_pending_svc_event( $el_svc, $hostkey, $servicekey );
	    return $restart_xml if $queueing_status != CONTINUE_STATUS;
	}
	if ( $debug_debug ) {
	    print LOG "Found changed $servicekey\n";
	    print LOG Data::Dumper->Dump( [ \%{$cs_svc} ], [qw(\%{cs_svcs})] );
	    print LOG Data::Dumper->Dump( [ \%{$el_svc} ], [qw(\%{el_svcs})] );
	}
	push_service_state_change( $hostkey, $servicekey, $el_svc, $state_changes ) if defined $state_changes;
	return join( '', @service_xml );
    }
    return $no_xml;
}

sub queue_pending_host_event {
    ## This subroutine sends an event in the rare case where the host has transitioned from PENDING to UP.
    ## Nagios does not recognize this as an event, but we want it in Foundation so we are detecting and
    ## sending it here. After initial script startup, when a lot of these might be found, there is not much
    ## point in bundling these, as they will trickle in based on the scheduler, and should only occur after
    ## hosts are added.
    my $el_host = shift;
    my $hostkey = shift;

    # Bail if events are off.
    if (not $send_events_for_pending_to_ok) {
	return CONTINUE_STATUS;
    }
    my @xml_message = ();
    push @xml_message, "<LogMessage ";

    # default identification -- should set to IP address if known
    push @xml_message, "MonitorServerName=\"$thisnagios\" ";
    push @xml_message, "Host=\"$hostkey\" ";
    if ( $hostipaddress{$hostkey} ) {
	## have IP address; use it
	push @xml_message, "Device=\"$hostipaddress{$hostkey}\" ";
    }
    else {
	## no IP address; set to host name
	push @xml_message, "Device=\"$hostkey\" ";
    }
    push @xml_message, 'Severity="OK" ';
    push @xml_message, 'MonitorStatus="UP" ';
    my $tmp = $el_host->{LastPluginOutput};
    $tmp =~ s/\n/ /g;
    $tmp =~ s/<br>/ /ig;
    $tmp =~ s/&/&amp;/g;
    $tmp =~ s/"/&quot;/g;
    $tmp =~ s/'/&apos;/g;
    $tmp =~ s/</&lt;/g;
    $tmp =~ s/>/&gt;/g;
    push @xml_message, "TextMessage=\"$tmp\" ";
    $tmp = time_text(time);
    push @xml_message, "ReportDate=\"$tmp\" ";
    push @xml_message, "SubComponent=\"$hostkey\" ";
    push @xml_message, "LastInsertDate=\"$el_host->{LastCheckTime}\" ";
    push @xml_message, 'ErrorType="HOST ALERT" ';
    push @xml_message, '/>';

    my $xml_message = join( '', @xml_message );

    print LOG "Pending Transition Host Event:\n$xml_message\n" if $debug_xml;

    push @event_messages, $xml_message;
    $message_counter = send_pending_events( $message_counter, $max_event_bundle_size );

    return ($message_counter < 0) ? RESTART_STATUS : CONTINUE_STATUS;
}

sub queue_pending_svc_event {
    ## This subroutine sends an event in the rare case where the service has transitioned from PENDING to OK.
    ## Nagios does not recognize this as an event, but we want it in Foundation so we are detecting and
    ## sending it here. After initial script startup, when a lot of these might be found, there is not much
    ## point in bundling these, as they will trickle in based on the scheduler, and should only occur after
    ## services are added.
    my $el_svc      = shift;
    my $hostkey     = shift;
    my $servicekey  = shift;

    # Bail if events are off.
    if (not $send_events_for_pending_to_ok) {
	return CONTINUE_STATUS;
    }
    my @xml_message = ();
    push @xml_message, "<LogMessage ";

    # default identification -- should set to IP address if known
    push @xml_message, "MonitorServerName=\"$thisnagios\" ";
    push @xml_message, "Host=\"$hostkey\" ";
    if ( $hostipaddress{$hostkey} ) {
	## have IP address; use it
	push @xml_message, "Device=\"$hostipaddress{$hostkey}\" ";
    }
    else {
	## no IP address; set to host name
	push @xml_message, "Device=\"$hostkey\" ";
    }
    push @xml_message, "ServiceDescription=\"$servicekey\" ";
    push @xml_message, "Severity=\"$el_svc->{MonitorStatus}\" ";
    push @xml_message, "MonitorStatus=\"$el_svc->{MonitorStatus}\" ";
    my $tmp = $el_svc->{LastPluginOutput};
    $tmp =~ s/\n/ /g;
    $tmp =~ s/<br>/ /ig;
    $tmp =~ s/&/&amp;/g;
    $tmp =~ s/"/&quot;/g;
    $tmp =~ s/'/&apos;/g;
    $tmp =~ s/</&lt;/g;
    $tmp =~ s/>/&gt;/g;
    push @xml_message, "TextMessage=\"$tmp\" ";
    $tmp = time_text(time);
    push @xml_message, "ReportDate=\"$tmp\" ";
    push @xml_message, "LastInsertDate=\"$el_svc->{LastCheckTime}\" ";
    push @xml_message, "SubComponent=\"$hostkey:$servicekey\" ";
    push @xml_message, 'ErrorType="SERVICE ALERT" ';
    push @xml_message, '/>';

    my $xml_message = join( '', @xml_message );

    print LOG "Pending Transition Service Event:\n$xml_message\n" if $debug_xml;

    push @event_messages, $xml_message;
    $message_counter = send_pending_events( $message_counter, $max_event_bundle_size );

    return ($message_counter < 0) ? RESTART_STATUS : CONTINUE_STATUS;
}

sub hostStatusUpdate {
    my $element_ref = shift;
    my $collage_ref = shift;
    my $hostkey     = shift;
    my $el_host     = $element_ref->{Host}->{$hostkey};
    my $cs_host     = \%{ $collage_ref->{Host}->{$hostkey} };
    #$cs_host = $el_host;
    $cs_host->{Comments}                  = $el_host->{Comments};
    $cs_host->{CurrentAttempt}            = $el_host->{CurrentAttempt};
    $cs_host->{CurrentNotificationNumber} = $el_host->{CurrentNotificationNumber};
    $cs_host->{LastNotificationTime}      = $el_host->{LastNotificationTime};
    $cs_host->{ExecutionTime}             = $el_host->{ExecutionTime};
    $cs_host->{LastCheckTime}             = $el_host->{LastCheckTime};
    $cs_host->{Latency}                   = $el_host->{Latency};
    $cs_host->{MaxAttempts}               = $el_host->{MaxAttempts};
    $cs_host->{MonitorStatus}             = $el_host->{MonitorStatus};
    $cs_host->{NextCheckTime}             = $el_host->{NextCheckTime};
    $cs_host->{ScheduledDowntimeDepth}    = $el_host->{ScheduledDowntimeDepth};
    $cs_host->{StateType}                 = $el_host->{StateType};
    $cs_host->{isAcknowledged}            = $el_host->{isAcknowledged};
    $cs_host->{isChecksEnabled}           = $el_host->{isChecksEnabled};
    $cs_host->{isEventHandlersEnabled}    = $el_host->{isEventHandlersEnabled};
    $cs_host->{isFlapDetectionEnabled}    = $el_host->{isFlapDetectionEnabled};
#    $cs_host->{isHostFlapping}            = $el_host->{isHostFlapping};
    $cs_host->{isNotificationsEnabled}    = $el_host->{isNotificationsEnabled};
#    $cs_host->{isObsessOverHost}          = $el_host->{isObsessOverHost};
    $cs_host->{isPassiveChecksEnabled}    = $el_host->{isPassiveChecksEnabled};
    $cs_host->{LastPluginOutput}          = $el_host->{LastPluginOutput};
    $cs_host->{PercentStateChange}	  = $el_host->{PercentStateChange};
    $cs_host->{LastStateChange}          = $el_host->{LastStateChange};
    return;
}

sub serviceStatusUpdate {
    my $element_ref = shift;
    my $collage_ref = shift;
    my $hostkey     = shift;
    my $servicekey  = shift;
    my $el_svc      = $element_ref->{Host}->{$hostkey}->{Service}->{$servicekey};
    my $cs_svc      = \%{ $collage_ref->{Host}->{$hostkey}->{Service}->{$servicekey} };
#     $cs_svc = $el_svc;
    $cs_svc->{Comments}                  = $el_svc->{Comments};
    $cs_svc->{CurrentAttempt}            = $el_svc->{CurrentAttempt};
    $cs_svc->{CurrentNotificationNumber} = $el_svc->{CurrentNotificationNumber};
    $cs_svc->{LastNotificationTime}      = $el_svc->{LastNotificationTime};
    $cs_svc->{LastCheckTime}             = $el_svc->{LastCheckTime};
    $cs_svc->{MonitorStatus}             = $el_svc->{MonitorStatus};
    $cs_svc->{NextCheckTime}             = $el_svc->{NextCheckTime};
    $cs_svc->{ScheduledDowntimeDepth}    = $el_svc->{ScheduledDowntimeDepth};
    $cs_svc->{isAcceptPassiveChecks}     = $el_svc->{isAcceptPassiveChecks};
    $cs_svc->{isChecksEnabled}           = $el_svc->{isChecksEnabled};
    $cs_svc->{isEventHandlersEnabled}    = $el_svc->{isEventHandlersEnabled};
    $cs_svc->{isFlapDetectionEnabled}    = $el_svc->{isFlapDetectionEnabled};
    $cs_svc->{isNotificationsEnabled}    = $el_svc->{isNotificationsEnabled};
#    $cs_svc->{isObsessOverService}       = $el_svc->{isObsessOverService};
    $cs_svc->{isProblemAcknowledged}     = $el_svc->{isProblemAcknowledged};
#    $cs_svc->{isServiceFlapping}         = $el_svc->{isServiceFlapping};
    $cs_svc->{MaxAttempts}               = $el_svc->{MaxAttempts};
    $cs_svc->{PercentStateChange}        = $el_svc->{PercentStateChange};
    $cs_svc->{LastPluginOutput}          = $el_svc->{LastPluginOutput};
    $cs_svc->{Latency}                   = $el_svc->{Latency};
    $cs_svc->{ExecutionTime}             = $el_svc->{ExecutionTime};
    $cs_svc->{LastStateChange}           = $el_svc->{LastStateChange};
    $cs_svc->{StateType}          	 = $el_svc->{StateType};
    return;
}

sub find_deltas {
    my $element_ref        = shift;
    my $collage_status_ref = shift;
    my $deltas             = {};

    foreach my $hostkey ( keys( %{ $collage_status_ref->{Host} } ) ) {
	my $el_host = $element_ref->{Host}->{$hostkey};
	if ( !defined $el_host ) {
	    $deltas->{FoundationHost}->{$hostkey} = 1;
	    next;
	}
	foreach my $servicekey ( keys( %{ $collage_status_ref->{Host}->{$hostkey}->{Service} } ) ) {
	    my $el_svc = $el_host->{Service}->{$servicekey};
	    if ( !defined $el_svc ) {
		$deltas->{FoundationHost}->{$hostkey}->{Service}->{$servicekey} = 1;
	    }
	}
    }
    foreach my $hostkey ( keys( %{ $element_ref->{Host} } ) ) {
	my $cs_host = $collage_status_ref->{Host}->{$hostkey};
	if ( !defined $cs_host ) {
	    $deltas->{NagiosHost}->{$hostkey} = 1;
	    next;
	}
	foreach my $servicekey ( keys( %{ $element_ref->{Host}->{$hostkey}->{Service} } ) ) {
	    if ( !defined $cs_host->{Service}->{$servicekey} ) {
		$deltas->{NagiosHost}->{$hostkey}->{Service}->{$servicekey} = 1;
	    }
	}
    }
    return $deltas;
}

sub assemble_remote_full_dump {
    my $collage_status_ref = shift;
    my @states             = ();
    my $cs_host            = undef;
    my $cs_serv            = undef;
    my $cs_status          = undef;
    my $check_state        = undef;
    my $cs_hosts           = $collage_status_ref->{Host};
    my $cs_services        = undef;
    my $host_text          = undef;
    my $service_text       = undef;

    $#states = $heartbeat_high_water_mark;    # pre-extend the array, for efficiency
    $#states = -1;                            # truncate the array, since we don't have any messages yet

    foreach my $host ( keys( %{$cs_hosts} ) ) {
	$cs_host   = $cs_hosts->{$host};
	$cs_status = $cs_host->{MonitorStatus};
	if ( $cs_status =~ /UP/ ) {
	    $check_state = 0;
	}
	elsif ( $cs_status =~ /PENDING/ ) {
	    next;
	}
	else {
	    $check_state = 1;
	}
	## Reverse the XML Substitution needed for Foundation in the status text.
	$host_text = $cs_host->{LastPluginOutput};
	$host_text =~ s/&amp;/&/g;
	$host_text =~ s/&quot;/"/g;
	$host_text =~ s/&apos;/'/g;
	$host_text =~ s/&lt;/</g;
	$host_text =~ s/&gt;/>/g;
	push @states, "$host\t$check_state\t$host_text|\n";
	$cs_services = $cs_host->{Service};
	foreach my $service ( keys( %{$cs_services} ) ) {
	    $cs_serv   = $cs_services->{$service};
	    $cs_status = $cs_serv->{MonitorStatus};
	    if ( $cs_status =~ /PENDING/ ) {
		next;
	    }
	    elsif ( $cs_status =~ /OK/ ) {
		$check_state = 0;
	    }
	    elsif ( $cs_status =~ /WARNING/ ) {
		$check_state = 1;
	    }
	    elsif ( $cs_status =~ /CRITICAL/ ) {
		$check_state = 2;
	    }
	    else {
		$check_state = 3;
	    }
	    $service_text = $cs_serv->{LastPluginOutput};
	    $service_text =~ s/&amp;/&/g;
	    $service_text =~ s/&quot;/"/g;
	    $service_text =~ s/&apos;/'/g;
	    $service_text =~ s/&lt;/</g;
	    $service_text =~ s/&gt;/>/g;
	    push @states, "$host\t$service\t$check_state\t$service_text|\n";
	}
    }

    # Prepare for the next iteration.
    my $count = @states;
    $heartbeat_high_water_mark = $count if $heartbeat_high_water_mark < $count;

    return \@states;
}

# This routine is obsolete now.  It is kept here only temporarily,
# for historical interest, and may be removed in a future release.
sub assemble_remote_state_changes {
    my ( $element_ref, $collage_ref ) = @_;
    my @states       = ();
    my $el_host      = undef;
    my $cs_host      = undef;
    my $el_serv      = undef;
    my $cs_serv      = undef;
    my $el_status    = undef;
    my $cs_status    = undef;
    my $check_state  = undef;
    my $el_hosts     = \%{ $element_ref->{Host} };
    my $cs_hosts     = \%{ $collage_ref->{Host} };
    my $el_services  = undef;
    my $cs_services  = undef;
    my $host_text    = undef;
    my $service_text = undef;

    $#states = $state_change_high_water_mark;    # pre-extend the array, for efficiency
    $#states = -1;                               # truncate the array, since we don't have any messages yet

    foreach my $host ( keys( %{$el_hosts} ) ) {
	$el_host   = \%{ $el_hosts->{$host} };
	$cs_host   = \%{ $cs_hosts->{$host} };
	$el_status = $el_host->{MonitorStatus};
	$cs_status = $cs_host->{MonitorStatus};
	if ( $el_status ne $cs_status ) {
	    if ( $el_status =~ /UP/ ) {
		$check_state = 0;
	    }
	    elsif ( $el_status =~ /PENDING/ ) {
		next;
	    }
	    else {
		$check_state = 1;
	    }
	    ## Reverse the XML Substitution needed for Foundation in the status text.
	    $host_text = $el_host->{LastPluginOutput};
	    $host_text =~ s/&amp;/&/g;
	    $host_text =~ s/&quot;/"/g;
	    $host_text =~ s/&apos;/'/g;
	    $host_text =~ s/&lt;/</g;
	    $host_text =~ s/&gt;/>/g;
	    push @states, "$host\t$check_state\t$host_text|\n";
	}
	$el_services = \%{ $el_host->{Service} };
	$cs_services = \%{ $cs_host->{Service} };
	foreach my $service ( keys( %{$el_services} ) ) {
	    $el_serv   = \%{ $el_services->{$service} };
	    $cs_serv   = \%{ $cs_services->{$service} };
	    $el_status = $el_serv->{MonitorStatus};
	    $cs_status = $cs_serv->{MonitorStatus};
	    if ( $el_status ne $cs_status ) {
		if ( $el_status =~ /PENDING/ ) {
		    next;
		}
		elsif ( $el_status =~ /OK/ ) {
		    $check_state = 0;
		}
		elsif ( $el_status =~ /WARNING/ ) {
		    $check_state = 1;
		}
		elsif ( $el_status =~ /CRITICAL/ ) {
		    $check_state = 2;
		}
		else {
		    $check_state = 3;
		}
		$service_text = $el_serv->{LastPluginOutput};
		$service_text =~ s/&amp;/&/g;
		$service_text =~ s/&quot;/"/g;
		$service_text =~ s/&apos;/'/g;
		$service_text =~ s/&lt;/</g;
		$service_text =~ s/&gt;/>/g;
		push @states, "$host\t$service\t$check_state\t$service_text|\n";
	    }
	}
    }

    # Prepare for the next iteration.
    my $count = @states;
    $state_change_high_water_mark = $count if $state_change_high_water_mark < $count;

    return \@states;
}

# sub for sending state data via NSCA. 
# Note that we don't try to collect details of which messages actually made it
# through to the destination.  In that sense, this is a somewhat unreliable channel.
sub send_nsca {
    my $nsca_host              = shift;
    my $nsca_port              = shift;
    my $nsca_timeout           = shift;
    my $send_to_secondary_NSCA = shift;
    my $secondary_nsca_host    = shift;
    my $max_messages_per_send  = shift;
    my $nsca_batch_delay       = shift;
    my $messages               = shift;
    my $message_set            = undef;
    my $failed                 = 0;
    my $first                  = 0;
    my $last                   = 0;
    my $total_messages         = @$messages;
    my $last_index             = $total_messages - 1;
    my $send_nsca_command =
"/usr/local/groundwork/common/bin/send_nsca -H $nsca_host -p $nsca_port -to $nsca_timeout -c /usr/local/groundwork/common/etc/send_nsca.cfg";
    my $secondary_send_nsca_command =
"/usr/local/groundwork/common/bin/send_nsca -H $secondary_nsca_host -p $nsca_port -to $nsca_timeout -c /usr/local/groundwork/common/etc/send_nsca.cfg"
      if $send_to_secondary_NSCA;

    print LOG $logtime . ' Sending ' . $total_messages . ' results at ' . $logtime . ".\n" if ($debug_basic);

    for ( $first = 0 ; $first <= $last_index ; $first = $last + 1 ) {
	$last = $first + $max_messages_per_send - 1;
	$last = $last_index if $last > $last_index;
	# We use an array slice here to avoid a lot of expensive and pointless copying into a second array.
	# We concatenate all the messages in the slice to avoid a lot of individual system calls within the
	# print statement, as print will make a separate call for each list element provided.
	$message_set = join( '', @$messages[ $first .. $last ] );
	open NSCA, '|-', "$send_nsca_command >> $logfile";
	print NSCA $message_set;
	$failed |= !close NSCA;
	if ($send_to_secondary_NSCA) {
	    open NSCA, '|-', "$secondary_send_nsca_command >> $logfile";
	    print NSCA $message_set;
	    $failed |= !close NSCA;
	}
	sleep $nsca_batch_delay if $last < $last_index;
    }
    return !$failed;
}

sub gdma_spool {
    my $gdma_results = shift;  # arrayref to possibly-empty list of previously-failed-to-spool messages
    my $commands     = shift;  # arrayref to list of new messages to spool

    ## Prepend to each result the overhead info needed by the GDMA spooler, before spooling it.
    my $default_retries = 0;
    my $default_target  = 0;        # "0" implies that the result is to be sent to all the primary targets.
    my $now             = time();
    my $prefix = join( '', $default_retries, "\t", $default_target, "\t", $now, "\t" );
    push @$gdma_results, map { $prefix . $_ } @$commands;

    # Flush the data out to the spool file immediately.
    # We make this a non-blocking call, as we don't want to block for too long.
    # If the spooling doesn't work, the prepared results will be left in place
    # (in @$gdma_results) and can/should be passed back here on the next call.
    my $blocking = 0;
    my $spooled_result_count;
    my $errstr;
    if ( GDMAUtils::spool_results( $gdma_spool_filename, $gdma_results, $blocking, \$spooled_result_count, \$errstr ) ) {
	@$gdma_results = ();
    }
    else {
	## Spooling failed, but the results to spool are still there in the @$gdma_results array.
	## Hopefully, they will be spooled at a later time.
	log_timed_message "ERROR:  GDMA spooling:  $errstr";
	## Safety valve:  prevent an infinite growth of accumulating as-yet-unspooled results.
	my $results_to_discard = @$gdma_results - $max_unspooled_results_to_save;
	if ($results_to_discard > 0) {
	    log_timed_message "NOTICE:  GDMA spooling:  discarding $results_to_discard results";
	    splice @$gdma_results, 0, $results_to_discard;
	}
    }
}

__END__

NAGIOS V1 STATUS.LOG FILE
All Host Lines:

[Time of last update] HOST;
Host Name (string);
Status (OK/DOWN/UNREACHABLE);
Last Check Time (long time);
Last State Change (long time);
Acknowledged (0/1);
Time Up (long time);
Time Down (long time);
Time Unreachable (long time);
Last Notification Time (long time);
Current Notification Number (#);
Notifications Enabled (0/1);
Event Handlers Enabled (0/1);
Checks Enabled (0/1);
Flap Detection Enabled (0/1);
Host is Flapping (0/1);
Percent State Change (###.##);
Scheduled downtime depth (#);
Failure Prediction Enabled (0/1);
Process Performance Data(0/1);
Plugin Output (string)

Service Lines:

[Time of last update] SERVICE;
Host Name (string);
Service Description (string);
Status (OK/WARNING/CRITICAL/UNKNOWN);
Retry number (#/#);
State Type (SOFT/HARD);
Last check time (long time);
Next check time (long time);
Check type (ACTIVE/PASSIVE);
Checks enabled (0/1);
Accept Passive Checks (0/1);
Event Handlers Enabled (0/1);
Last state change (long time);
Problem acknowledged (0/1);
Last Hard State (OK/WARNING/CRITICAL/UNKNOWN);
Time OK (long time);
Time Unknown (long time);
Time Warning (long time);
Time Critical (long time);
Last Notification Time (long time);
Current Notification Number (#);
Notifications Enabled (0/1);
Latency (#);
Execution Time (#);
Flap Detection Enabled (0/1);
Service is Flapping (0/1);
Percent State Change (###.##);
Scheduled Downtime Depth (#);
Failure Prediction Enabled (0/1);
Process Performance Date (0/1);
Obsess Over Service (0/1);
Plugin Output (string)

Program line (second line of the status log):

[Current Time] PROGRAM;
Program Start Time (long time);
Nagios PID (#);
Daemon Mode (0/1);
Last Command Check (long time);
Last Log Rotation (long time);
Notifications Enabled (0/1);
Execute Service Checks (0/1);
Accept Passive Service Checks (0/1);
Enable Event Handlers (0/1);
Obsess Over Services (0/1);
Enable Flap Detection (0/1);
Enable Failure Prediction (0/1);
Process Performance Data (0/1)


NAGIOS V2 STATUS.DAT FILE
info {
	created=1122681331
	version=2.0b3
	}

program {
	modified_host_attributes=0
	modified_service_attributes=0
	nagios_pid=48776
	daemon_mode=1
	program_start=1122681286
	last_command_check=0
	last_log_rotation=0
	enable_notifications=1
	active_service_checks_enabled=1
	passive_service_checks_enabled=1
	active_host_checks_enabled=1
	passive_host_checks_enabled=1
	enable_event_handlers=1
	obsess_over_services=0
	obsess_over_hosts=0
	check_service_freshness=0
	check_host_freshness=0
	enable_flap_detection=0
	enable_failure_prediction=1
	process_performance_data=0
	global_host_event_handler=
	global_service_event_handler=
	}

host {
	host_name=localhost
	modified_attributes=0
	check_command=check-host-alive
	event_handler=
	has_been_checked=1
	should_be_scheduled=0
	check_execution_time=0.061
	check_latency=0.000
	current_state=0
	last_hard_state=0
	check_type=0
	plugin_output=PING OK - Packet loss = 0%, RTA = 0.04 ms
	performance_data=
	last_check=1122681125
	next_check=0
	current_attempt=1
	max_attempts=10
	state_type=1
	last_state_change=1122681115
	last_hard_state_change=1122681115
	last_time_up=1122681125
	last_time_down=0
	last_time_unreachable=0
	last_notification=0
	next_notification=0
	no_more_notifications=0
	current_notification_number=0
	notifications_enabled=1
	problem_has_been_acknowledged=0
	acknowledgement_type=0
	active_checks_enabled=1
	passive_checks_enabled=1
	event_handler_enabled=1
	flap_detection_enabled=1
	failure_prediction_enabled=1
	process_performance_data=1
	obsess_over_host=1
	last_update=1122681331
	is_flapping=0
	percent_state_change=0.00
	scheduled_downtime_depth=0
	}

service {
	host_name=localhost
	service_description=Current Load
	modified_attributes=0
	check_command=check_local_load!5.0,4.0,3.0!10.0,6.0,4.0
	event_handler=
	has_been_checked=1
	should_be_scheduled=1
	check_execution_time=0.008
	check_latency=0.539
	current_state=0
	last_hard_state=0
	current_attempt=1
	max_attempts=4
	state_type=1
	last_state_change=1122681115
	last_hard_state_change=1122681115
	last_time_ok=1122681286
	last_time_warning=0
	last_time_unknown=0
	last_time_critical=0
	plugin_output=OK - load average: 0.12, 0.15, 0.21
	performance_data=load1=0.123535;5.000000;10.000000;0.000000 load5=0.154785;4.000000;6.000000;0.000000 load15=0.214844;3.000000;4.000000;0.000000
	last_check=1122681286
	next_check=1122681586
	check_type=0
	current_notification_number=0
	last_notification=0
	next_notification=0
	no_more_notifications=0
	notifications_enabled=1
	active_checks_enabled=1
	passive_checks_enabled=1
	event_handler_enabled=1
	problem_has_been_acknowledged=0
	acknowledgement_type=0
	flap_detection_enabled=1
	failure_prediction_enabled=1
	process_performance_data=1
	obsess_over_service=1
	last_update=1122681331
	is_flapping=0
	percent_state_change=0.00
	scheduled_downtime_depth=0
	}
