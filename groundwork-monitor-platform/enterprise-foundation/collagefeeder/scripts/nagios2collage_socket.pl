#!/usr/local/groundwork/perl/bin/perl -w --

# nagios2collage_socket.pl
# Copyright (c) 2004-2018 GroundWork Open Source, Inc.
# www.groundworkopensource.com
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
# You should have received a copy of the GNU General Public License along
# with this program; if not, write to the Free Software Foundation, Inc.,
# 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA

# To Do:
# (*) Drop the {Host} and {Service} levels of the nested hash trees,
#     as they don't seem to provide any useful discrimination at their
#     respective levels, and just waste space and time.
# (*) FIX MAJOR:  test to see whether we need to UTF-8 encode any part of
#     events sent to Foundation, if that isn't already handled entirely
#     within the GW::RAPID package.
# (*) FIX MAJOR:  (This comment is somewhat old and may be inaccurate now.)
#     The timestamp processing for the Foundation REST API is seriously
#     messed up in this program.  Currently, inside rest_to_internal_time(), we are
#     using substr() to strip off millisecond and timezone-offset information.  The
#     former gives a slightly inaccurate result which may never matter, although we
#     should examine that assumption (since if Foundation saw two results within a
#     second, we might override a later result with an earlier result).  We might
#     perhaps run into such a problem if a given host is owned by CloudHub but
#     has some Nagios-owned services attached, and is therefore being monitored
#     by both CloudHub and Nagios.  But in any case, simply dropping the timezone
#     information is probably dangerous, because it risks major confusion about
#     what is the actual time around Daylight Savings Time transitions.  At those
#     times, the timezone offset may suddenly change by a half hour or an hour, and
#     our interpretation of the data when comparing it with some other timestamp
#     (which may have been converted on the other side of the DST transition) might
#     be suddenly wrong.  Best would be to avoid all types of string or object
#     comparisons when working with timestamps, and reduce everything end-to-end
#     between the database, the REST API, and this script to simple UNIX integer
#     UTC timestamps, where time always runs monotonically and there is never any
#     confusion induced by representation conversions.
# (*) FIX MINOR:  Check the usage of ExecutionTime.  In some places, it is being
#     treated as though it were a fixed timestamp, whose value we want to compare
#     between successive host or service checks.  But in actuality, we are instead
#     seeing its value be a simple integer.  Check the Nagios messages from which
#     we draw this value, to find out what is really the case.  Then visit all
#     places in this script that reference this field's valie, to ensure that they
#     treat this field as intended.
# (*) FIX MINOR:  Support has requested that we think about what the feeder's
#     response should be if detects an out-of-sync condition.  Historically, its
#     behavior has been to die and restart, on the assumption that this would both
#     get it out of the way for awhile and allow any ongoing Commit activity within
#     Foundation to complete, and get the feeder re-synchronized.  In practice,
#     though, we don't have a good model for why out-of-sync conditions arise, and
#     this default restart behavior puts the system out of production for as long as
#     the mismatch persists.  Support has requested some sort of alternative behavior
#     whereby some detail on the out-of-sync condition would be logged for diagnosis
#     and repair, but the feeder would continue operating as best it can with hosts
#     and services that are not in fact unsynchronized.  We would need to analyzes
#     cases here to see to what extent that would be possible, and how to implement
#     such a strategy.

####################################################################
# Perl Setup
####################################################################

use strict;

use Time::Local;
use vars qw($socket $smart_update);
use IO::Socket;
use Time::HiRes;
use DBI;
use CollageQuery;
use POSIX qw(strftime);

# At least DateTime version 0.56 is required, because that is when the
# bug was fixed where set_formatter returned a DateTime::Format::Strptime
# object instead of the DateTime object we expected and need.
use DateTime 1.08;
use DateTime::Format::Strptime;
use DateTime::Format::Builder;

use Data::Dumper;
$Data::Dumper::Indent   = 1;
$Data::Dumper::Sortkeys = 1;

use GDMA::GDMAUtils;
use TypedConfig;

# BE SURE TO UPDATE THIS WITH EVERY CHANGE.
my $VERSION = '7.2.2.0';

####################################################################
# Configuration Parameters
####################################################################

# --------------------------------------------------------------------------------
# Internal config options.  THESE SHOULD BE MOVED OUT TO THE CONFIG FILE.
# --------------------------------------------------------------------------------

# Whether to use simple UTC integers as our internal representation of timestamp values.
# Setting this to 1 is the only correct mode of operation, as it avoids all issues of
# confusion about timestamp ordering.  Setting this to 0 uses a text representation of
# timestamp values, which (if it includes timezone offsets) can easily produce invalid
# timestamp-ordering comparisons (because those comparisons will be done as simple
# lexical string comparisons, which DO NOT reflect true timestamp ordering, even
# ignoring global relativistic effects).
#
# We have this flag set to 0 until we have fully implemented UTC integers as a possible
# internal timestamp representation.  THAT IS NOT DONE YET (see rest_to_internal_time()),
# SO LEAVE THIS AS ZERO.
#
# We might add another flag later on, such as $use_milli_utc, to provide the capability
# to use integer milliseconds as the time units instead of full seconds.  We would then
# need to prioritize the order in which these flags are tested, and document that order.
my $use_utc = 0;

# How frequently (counting in processing cycles) to send an event to Foundation telling
# of an overload situation, if the overload persists across multiple consecutive cycles.
my $overload_event_frequency = 12;

# --------------------------------------------------------------------------------
# General config options.
# --------------------------------------------------------------------------------

my $debug_config = 0;    # if set, spill out certain data about config-file processing to STDOUT

# The extra config files are used when we internally configure to use the Foundation REST API, to use
# certain config parameters that we would rather not duplicate in the status-feeder.properties file.
my $default_config_file   = '/usr/local/groundwork/config/status-feeder.properties';

# 0 => minimal, 1 => summary, 2 => basic, 3 => XML messages, 4 => debug level, 5 => trace level.
my $debug_level = undef;

my $logfile = undef;    # Where the log file is to be written.

my $log_as_utf8 = 0;    # Set to 0 to log Foundation messages as ISO-8859-1, to 1 to log as UTF-8.

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

# Whether to skip initializing the data cache from Foundation during the first processing cycle,
# in favor of just copying data from Nagios into the cache and otherwise skipping the first cycle.
my $skip_initialization = undef;

# Minimum time (in seconds) between extended Nagios/Foundation synchronization checks, or 0 to disable such checks.
my $normal_sync_check_period = undef;

my $smart_update                  = undef;    # If set to 1, then send only state changes and heartbeats.
my $continue_after_blockage       = undef;    # If set to 1, soldier on after seeing that the server is overloaded.

my $send_on_host_data_change      = undef;
my $send_on_host_timing_change    = undef;
my $send_on_service_data_change   = undef;
my $send_on_service_timing_change = undef;
my $send_sync_warning             = undef;    # Send a console message when Nagios and Foundation are out of sync. 0 = no warning, 1 = warning.
my $send_events_for_pending_to_ok = undef;    # Whether to send pending-to-ok transition events, or just skip them.

my $failure_sleep_time = undef;    # Seconds to sleep before restarting after failure, to prevent tight looping.

# Whether Nagios SOFT-state object-check results should be ignored in favor of the last-known
# Nagios HARD-state object-check results when forwarding monitoring states to Foundation.
# All check-result metadata (e.g., the plugin output and the state type) from SOFT-state
# checks will still be forwarded unchanged if this option is enabled; only the check result
# itself (and its last-change timestamp) will be overridden.
my $ignore_soft_states = undef;

# GWMON-12227:  Suppress all reported stale status.  This is a temporary workaround to be enabled
# only for certain specialized setups, until Monarch is able to store, edit, and use information
# about which individual services on a host should and should not be monitored on a child server.
# Such data would be used when generating the configuration files for the child server.
#
# This option is for customers like the EGAIN-54 case where STALE STATUS from a CHILD SERVER
# overpowers PARENT reported passive checks from your GDMA STANDARD configuration which is not able
# to deal with duelling Nagios configuration.  It is a blunt instrument to deal with parent and
# child both thinking they are the source of monitoring data for a certain services.
my $ignore_stale_status = undef;

# GWMON-12251:  suppress_downtime_update was an experimental option, apparently abandoned because some
# GroundWork application needs the ScheduledDowntimeDepth property to perform monitorStatus conversion
# of "CRITICAL" or "DOWN" states to [UN]SCHEDULED CRITICAL/DOWN.  If we ever re-enable support for
# this option in the config file, it should also be applied to sending data to Foundation via the XML
# socket.  That begs the question of whether suppression should occur at the time when packets are
# built rather than just before they are sent.
my $suppress_downtime_update = 0;

# --------------------------------------------------------------------------------
# Options for sending to Foundation
# --------------------------------------------------------------------------------

my $use_rest_api = undef;          # set to 1 to use the REST API instead of the $foundation_port socket API

# Maximum number of events to accumulate before sending them all as a bundle.
my $max_event_bundle_size = undef;

# $syncwait is a multiplier of $cycle_sleep_time to wait on updates while Foundation processes a
# sync.  Typical value is 20.  In theory, you might need to increase this if you see deadlocks after
# commit in the framework.log file.  In practice, though, the need for this should have completely
# disappeared now that we have proper synchronization with pre-flight and commit operations in place.
my $syncwait = undef;

# --------------------------------------------------------------------------------
# Options for sending event data to Foundation via the Foundation REST API.
# --------------------------------------------------------------------------------

# The application name by which the nagios2collage_eventlog.pl process
# will be known to the Foundation REST API.
my $rest_api_requestor = undef;

# Where to find credentials for accessing the Foundation REST API.
my $ws_client_config_file = undef;

# The application-specific override for the default GW::RAPID REST call timeout.
my $rest_api_timeout = undef;

my $rest_bundle_size     = undef;  # Typical number of messages to send in each bundle.  This is NOT the minimum size ...
my $max_rest_bundle_size = undef;  # ... but this is the maximum size.  ??? seems to work reasonably well in testing.

# These flags control whether most calls to the Foundation REST API use asynchronous processing.
# This provides a significant performance improvement, so all of these flags are normally set
# to true in a production setup.  These flags are here primarily so this behavior can be easily
# controlled when the code is under development.
my $use_async_upsert_hosts    = undef;
my $use_async_upsert_services = undef;

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

my $foundation_host = undef;       # Where to send results to Foundation, if $use_rest_api is false.
my $foundation_port = undef;       # Where to send results to Foundation, if $use_rest_api is false.

my $xml_bundle_size     = undef;   # Typical number of messages to send in each bundle.  This is NOT the minimum size ...
my $max_xml_bundle_size = undef;   # ... but this is the maximum size.  150 seems to work reasonably well in testing.

my $sync_timeout_seconds = undef;  # Soft limit on time for which accumulating messages are held before sending.

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

# --------------------------------------------------------------------------------
# Options for sending state data to parent/standby server(s)
# --------------------------------------------------------------------------------

# Whether to send actual last-check timestamps in data forwarded to a parent/standby server.  If false,
# the effective historical setting, send the current time as the last-check time instead, each time
# data is forwarded.  We default this option to true because that is now the recommended setting, and
# we want to allow operation using that default even if this option is not found in the config file.
my $send_actual_check_timestamps = 1;

## Options for sending state data via direct NSCA invocations

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

## Options for sending state data via the GDMA spooler

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
my $debug_summary = undef;
my $debug_basic   = undef;
my $debug_xml     = undef;
my $debug_debug   = undef;
my $debug_trace   = undef;

# If $ignore_soft_states is false, we just use the keys of this hash.
# If $ignore_soft_states is true, we use the values as well.
my %mandatory_host_data_change = (
    MonitorStatus          => 'LastHardState',
    ScheduledDowntimeDepth => 'ScheduledDowntimeDepth',
    LastStateChange        => 'LastHardStateChange'
);
my @default_host_data_change = qw(
  CheckType
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
);
## FIX MAJOR:  Do all of these make sense to compare across successive invocations of a plugin???
## FIX MAJOR:  Do things like PercentStateChange and PerformanceData belong here as timing-related???
my @default_host_timing_change = qw(
  ExecutionTime
  Latency
  LastCheckTime
  NextCheckTime
  PercentStateChange
  PerformanceData
  CurrentAttempt
  LastPluginOutput
);

# If $ignore_soft_states is false, we just use the keys of this hash.
# If $ignore_soft_states is true, we use the values as well.
my %mandatory_service_data_change = (
    MonitorStatus          => 'LastHardState',
    ScheduledDowntimeDepth => 'ScheduledDowntimeDepth',
    LastStateChange        => 'LastHardStateChange'
);
my @default_service_data_change = qw(
  CheckType
  Comments
  CurrentNotificationNumber
  LastHardState
  LastNotificationTime
  isAcceptPassiveChecks
  isChecksEnabled
  isEventHandlersEnabled
  isFlapDetectionEnabled
  isNotificationsEnabled
  isProblemAcknowledged
  MaxAttempts
  StateType
);
## FIX MAJOR:  Do all of these make sense to compare across successive invocations of a plugin???
## FIX MAJOR:  Do things like PercentStateChange and PerformanceData belong here as timing-related???
my @default_service_timing_change = qw(
  LastCheckTime
  NextCheckTime
  Latency
  ExecutionTime
  PercentStateChange
  PerformanceData
  CurrentAttempt
  LastPluginOutput
);

my @non_default_host_data_change      = ();
my @non_default_host_timing_change    = ();
my @non_default_service_data_change   = ();
my @non_default_service_timing_change = ();

my @host_data_change      = ();
my @host_timing_change    = ();
my @service_data_change   = ();
my @service_timing_change = ();

# FIX MAJOR:  Do we need mappings to Foundation names?  Or do we have properties with the
# exact same names, and we need mappings in here from Nagios names to Foundation names?

# CheckType is now standard in @default_host_data_change, but it used to
# be an optional externally-specified field, so it is still allowed here.
#
# isFailurePredictionEnabled has no underlying support in Monarch now if it
# even ever did, so it is now obsolete.
#
my %allowed_host_data_fields = (
    CheckType                  => 1,
    isFailurePredictionEnabled => 1,
    isHostFlapping             => 1,
    isObsessOverHost           => 1,
    isProcessPerformanceData   => 1
);
my @obsolete_host_data_fields = (
    qw(
      isFailurePredictionEnabled
      )
);
my %allowed_host_timing_fields = (
    TimeDown        => 1,
    TimeUnreachable => 1,
    TimeUp          => 1
);
my @obsolete_host_timing_fields = ();

# CheckType is now standard in @default_service_data_change, but it used to
# be an optional externally-specified field, so it is still allowed here.
#
# LastHardState is now standard in @default_service_data_change, but it used
# to be an optional externally-specified field, so it is still allowed here.
#
# isFailurePredictionEnabled has no underlying support in Monarch now if it
# even ever did, so it is now obsolete.
#
my %allowed_service_data_fields = (
    CheckType                  => 1,
    LastHardState              => 1,
    isFailurePredictionEnabled => 1,
    isObsessOverService        => 1,
    isProcessPerformanceData   => 1,
    isServiceFlapping          => 1
);
my @obsolete_service_data_fields = (
    qw(
      isFailurePredictionEnabled
      )
);
my %allowed_service_timing_fields = (
    TimeCritical => 1,
    TimeOK       => 1,
    TimeUnknown  => 1,
    TimeWarning  => 1
);
my @obsolete_service_timing_fields = ();

my $heartbeat_mode           = 0;	# Do not change this setting -- it is controlled by smart_update.
my $last_nsca_heartbeat_time = undef;
my $last_nsca_full_dump_time = undef;
my $last_gdma_heartbeat_time = undef;
my $last_gdma_full_dump_time = undef;

my $heartbeat_high_water_mark    = 100;    # initial size for arrays holding heartbeat states; will be adjusted upward
my $state_change_high_water_mark = 100;    # initial size for arrays holding object state changes; will be adjusted upward

my $next_sync_timeout      = 0;    # used for XML batching
my $message_counter        = 1;
my $last_statusfile_mtime  = 0;
my $global_nagios          = {};
my $collage_status_ref     = {};
my $element_ref            = {};
my @hosts_to_cache         = ();
my %host_services_to_cache = ();
my $remote_hosts           = {};
my $remote_svcs            = {};
## my $device_ref            = {};
## my $host_ref              = {};
## my $service_ref           = {};
my $last_sync_check_time   = 0;
my $cycle_number           = -1;
my $loop_count             = 0;
my $total_wait             = 0;
my $rest_api               = undef;
my @xml_messages           = ();
my @host_status_updates    = ();
my @service_status_updates = ();
my @rest_event_messages    = ();    # Used for REST API event messages.
my @xml_event_messages     = ();    # Used for XML API event messages.
my $n_hostcount            = 0;
my $n_servicecount         = 0;
my $last_n_hostcount       = 0;
my $last_n_servicecount    = 0;
my $enable_feeding         = 1;
my $syncwaitcount          = 0;
my $logtime                = '';
my $sync_at_start          = 0;
my $looping_start_time     = 0;
my $gdma_spool_filename    = undef;
my $gdma_results_to_spool  = [];
my $overloaded_cycles      = 0;

my $async_upsert_hosts    = 'true';
my $async_upsert_services = 'true';

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

my %mapped_services = ();

my $start_message =
    "<GENERICLOG consolidation='SYSTEM' ApplicationType='SYSTEM' MonitorServerName='localhost' Device='127.0.0.1' Severity='OK' MonitorStatus='OK' TextMessage='Foundation-Nagios status check process started.' />";
my $command_close = '<SERVICE-MAINTENANCE command="close" />';
my $restart_xml   = '<RESTART />';
my $no_xml        = '';

# Here's an object for converting string timestamps we receive from the Foundation REST API
# into objects that we can compare, irrespective of any variations in the stated timezones
# of the supplied timestamps.
#
# We presently use a fixed output format, that does not include a colon in the timezone
# offset.  Alas, there does not seem to be any format specifier available that will
# automatically insert the colon between the hh and mm parts of the timezone offset.
#
my $internal_time_format = DateTime::Format::Strptime->new( pattern => '%FT%T.%3N%z' );

# The standard parsers available do not allow mixing basic (minimal punctuation)
# and extended (maximal punctuation) formats for the datetime components and the
# timezone offset component of a string timestamp.  So we build our own parser
# that will handle all combinations we might expect to see.  We even go slightly
# further and allow both period and comma for the fractional seconds separator.
# Both characters are allowed by the ISO-8601 standard; I'd like to see how well
# all of our parsers under test here stand up to the alternatives.
my $rest_time_parser = DateTime::Format::Builder->new();
$rest_time_parser->parser(
    regex       => qr/^(\d\d\d\d)-(\d\d)-(\d\d)T(\d\d):(\d\d):(\d\d)[.,](\d\d\d)([-+]\d\d:?\d\d)$/,
    params      => [qw( year month day hour minute second nanosecond time_zone )],
    postprocess => sub {
	my %args = @_;
	$args{parsed}{nanosecond} *= 1_000_000;
	return 1;
    }
);

# Here's a canonical timestamp which we can use if necessary to represent a time which
# is supposed to be entirely off the timeline, if we need such a representation which is
# still a DateTime object.  This is an ugly workaround, but may sometimes find use.
my $epoch_DateTime = DateTime->from_epoch( epoch => 0 );
$epoch_DateTime->set_formatter($internal_time_format);

# FIX MAJOR:  Verify that this list includes all fields that may be exchanged between
# this script and the Foundation REST API, that might contain timestamp data.
#
# FIX LATER:  If we ever support sending LastHardStateChange values to Foundation, we will
# need to specify LastHardStateChange and/or lastHardStateChange in this hash as well.
#
# The keys in this hash are supposed to be the field names understood by Foundation,
# not those that we use internal to this script (which may have capitalization and
# potentially other differences).
my %rest_time_field = (
    LastCheckTime        => 1,
    LastNotificationTime => 1,
    LastStateChange      => 1,
    NextCheckTime        => 1,
    lastCheckTime        => 1,
    lastStateChange      => 1,
    nextCheckTime        => 1,
    reportDate           => 1
);

# These are the fields that the REST API will return a "true" or "false"
# value for, that we need to recode to "1" or "0" when we read data from
# Foundation for comparison with equivalent Nagios data.
my %boolean_field = (
    isAcceptPassiveChecks  => 1,
    isAcknowledged         => 1,
    isChecksEnabled        => 1,
    isEventHandlersEnabled => 1,
    isFlapDetectionEnabled => 1,
    isNotificationsEnabled => 1,
    isPassiveChecksEnabled => 1,
    isProblemAcknowledged  => 1
);

# These are the Foundation names of fields that the REST API will truncate trailing "0" digits
# on.  The value of the hash is the precision that we need, mostly just for documentation purposes.
# Nagios formats check_execution_time and check_latency with 3 digits of precision, but we
# separately recode those to milliseconds before sending to Foundation, so they end up as integers
# and won't be handled as floating-point numbers here.
my %float_field = (
    PercentStateChange => 2
);

# FIX MINOR:  Map from external names provided by the REST API to internal names used in this script.
    # acknowledged         => 'FIX MINOR',
    # bubbleUpStatus       => 'FIX MINOR',
    # description          => 'FIX MINOR',
    # deviceIdentification => 'FIX MINOR',
    # id                   => 'FIX MINOR',
    # serviceAvailability  => 'FIX MINOR',
    # serviceCount         => 'FIX MINOR',

## LastHardState and LastHardStateChange are not currently recorded for hosts as such
## in Foundation, so they do not appear in these hashes.
my %host_inside_attr = (
    appType       => 'ApplicationType',
    checkType     => 'CheckType',
    hostName      => 'HostName',
    lastCheckTime => 'LastCheckTime',
    monitorStatus => 'MonitorStatus',
    nextCheckTime => 'NextCheckTime',
    stateType     => 'StateType'
);
my %host_rest_attr = (
    ApplicationType => 'appType',
    CheckType       => 'checkType',
    HostName        => 'hostName',
    LastCheckTime   => 'lastCheckTime',
    MonitorStatus   => 'monitorStatus',
    NextCheckTime   => 'nextCheckTime',
    StateType       => 'stateType',
);
my %host_prop_attr = (
    CactiRRDCommand           => 1,
    Comments                  => 1,
    CurrentAttempt            => 1,
    CurrentNotificationNumber => 1,
    ExecutionTime             => 1,
    LastNotificationTime      => 1,
    LastPluginOutput          => 1,
    LastStateChange           => 1,
    Latency                   => 1,
    MaxAttempts               => 1,
    PercentStateChange        => 1,
    PerformanceData           => 1,
    ScheduledDowntimeDepth    => 1,
    TimeDown                  => 1,
    TimeUnreachable           => 1,
    TimeUp                    => 1,
    isAcknowledged            => 1,
    isChecksEnabled           => 1,
    isEventHandlersEnabled    => 1,
    isFlapDetectionEnabled    => 1,
    isHostFlapping            => 1,
    isNotificationsEnabled    => 1,
    isObsessOverHost          => 1,
    isPassiveChecksEnabled    => 1,
    isProcessPerformanceData  => 1,
);

# We're getting strange data as the lastPlugInOutput field for many services, containing
# many "^" characters that look out of place.  That is due to a "constructed" value for
# this attribute, which contains as one piece the separate LastPluginOutput property
# value, with sub-fields separated by "^^^" as a delimiter.  This constructed attribute
# seems to have no utility for this status feeder, so we just ignore it.

# FIX MINOR:  Map from external names provided by the REST API to internal names used in this script.
    # appType          => 'FIX MINOR',
    # description      => 'FIX MINOR',
    # hostName         => 'FIX MINOR',
    # id               => 'FIX MINOR',
    # monitorServer    => 'FIX MINOR',

## LastHardStateChange is not currently recorded for services as such
## in Foundation, so it does not appear in these hashes.
my %serv_inside_attr = (
    checkType        => 'CheckType',
    lastCheckTime    => 'LastCheckTime',
    lastHardState    => 'LastHardState',
    lastStateChange  => 'LastStateChange',
    monitorStatus    => 'MonitorStatus',
    nextCheckTime    => 'NextCheckTime',
    stateType        => 'StateType'
);
my %serv_rest_attr = (
    CheckType        => 'checkType',
    LastCheckTime    => 'lastCheckTime',
    LastHardState    => 'lastHardState',
    LastStateChange  => 'lastStateChange',
    MonitorStatus    => 'monitorStatus',
    NextCheckTime    => 'nextCheckTime',
    StateType        => 'stateType',
);
my %serv_prop_attr = (
    Comments                  => 1,
    CurrentAttempt            => 1,
    CurrentNotificationNumber => 1,
    ExecutionTime             => 1,
    LastNotificationTime      => 1,
    LastPluginOutput          => 1,
    Latency                   => 1,
    MaxAttempts               => 1,
    PercentStateChange        => 1,
    PerformanceData           => 1,
    RRDCommand                => 1,
    RRDLabel                  => 1,
    RRDPath                   => 1,
    RemoteRRDCommand          => 1,
    ScheduledDowntimeDepth    => 1,
    TimeCritical              => 1,
    TimeOK                    => 1,
    TimeUnknown               => 1,
    TimeWarning               => 1,
    isAcceptPassiveChecks     => 1,
    isChecksEnabled           => 1,
    isEventHandlersEnabled    => 1,
    isFlapDetectionEnabled    => 1,
    isNotificationsEnabled    => 1,
    isObsessOverService       => 1,
    isProblemAcknowledged     => 1,
    isProcessPerformanceData  => 1,
    isServiceFlapping         => 1,
);

my $restart_change = undef;
my $no_change      = {};

our $shutdown_requested = 0;
our $reconfig_requested = 0;

use constant ERROR_STATUS    => 0;
use constant STOP_STATUS     => 1;
use constant RESTART_STATUS  => 2;
use constant CONTINUE_STATUS => 3;

####################################################################
# Program
####################################################################

# Here is the entire substance of this script, in a one-liner:
exit ((main() == ERROR_STATUS) ? 1 : 0);

# To be kind to the server and always disconnect our session, we attempt to force a shutdown
# of the REST API before global destruction sets in and makes it impossible to log out.
END {
    terminate_rest_api() if $use_rest_api;
}

####################################################################
# Supporting Subroutines
####################################################################

sub read_config_file {
    my $config_file = shift;
    eval {
	my $config = TypedConfig->new($config_file);

	$debug_level                   = $config->get_number('debug_level');
	$logfile                       = $config->get_scalar('logfile');
	$thisnagios                    = $config->get_scalar('thisnagios');
	$nagios_version                = $config->get_number('nagios_version');
	$statusfile                    = $config->get_scalar('statusfile');
	$cycle_sleep_time              = $config->get_number('cycle_sleep_time');
	$local_full_update_time        = $config->get_number('local_full_update_time');
	$skip_initialization           = $config->get_boolean('skip_initialization');
	$normal_sync_check_period      = $config->get_number('normal_sync_check_period');
	$smart_update                  = $config->get_boolean('smart_update');
	$continue_after_blockage       = $config->get_boolean('continue_after_blockage');
	$send_on_host_data_change      = $config->get_scalar('send_on_host_data_change');
	$send_on_host_timing_change    = $config->get_scalar('send_on_host_timing_change');
	$send_on_service_data_change   = $config->get_scalar('send_on_service_data_change');
	$send_on_service_timing_change = $config->get_scalar('send_on_service_timing_change');
	$send_sync_warning             = $config->get_boolean('send_sync_warning');
	$send_events_for_pending_to_ok = $config->get_boolean('send_events_for_pending_to_ok');
	$failure_sleep_time            = $config->get_number('failure_sleep_time');
	$ignore_soft_states            = $config->get_boolean('ignore_soft_states');
	$ignore_stale_status           = $config->get_boolean('ignore_stale_status');
	## $suppress_downtime_update      = $config->get_boolean('suppress_downtime_update');
	$use_rest_api                  = $config->get_boolean('use_rest_api');
	$max_event_bundle_size         = $config->get_number('max_event_bundle_size');
	$syncwait                      = $config->get_number('syncwait');
	$rest_api_requestor            = $config->get_scalar('rest_api_requestor');
	$ws_client_config_file         = $config->get_scalar('ws_client_config_file');
	$rest_api_timeout              = $config->get_integer('rest_api_timeout');
	$rest_bundle_size              = $config->get_number('rest_bundle_size');
	$max_rest_bundle_size          = $config->get_number('max_rest_bundle_size');
	$use_async_upsert_hosts        = $config->get_boolean('use_async_upsert_hosts');
	$use_async_upsert_services     = $config->get_boolean('use_async_upsert_services');
	$GW_RAPID_log_level            = $config->get_scalar('GW_RAPID_log_level');
	$log4perl_config               = $config->get_scalar('log4perl_config');
	$foundation_host               = $config->get_scalar('foundation_host');
	$foundation_port               = $config->get_number('foundation_port');
	$xml_bundle_size               = $config->get_number('xml_bundle_size');
	$max_xml_bundle_size           = $config->get_number('max_xml_bundle_size');
	$sync_timeout_seconds          = $config->get_number('sync_timeout_seconds');
	$send_buffer_size              = $config->get_number('send_buffer_size');
	$socket_send_timeout           = $config->get_number('socket_send_timeout');
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

	# To simplify providing an updated script to run on older releases, we will accept but don't
	# demand that this option be found in the config file.  That way, we don't need to have the
	# customer migrate their existing config-file settings into a new config file just to support
	# this new option, unless they want to change the standard recommended default value.
	eval {
	    ## If this fails, we stick with the default value set earlier.
	    $send_actual_check_timestamps = $config->get_boolean('send_actual_check_timestamps');
	};
	if ( $@ && $@ !~ / cannot find a config-file value / ) {
	    chomp $@;
	    die "$@\n";
	}
	## To avoid extra work later on, we only keep the internal flag as true if we are actually going to send data.
	$send_actual_check_timestamps &&= ( $send_state_changes_by_nsca || $send_state_changes_by_gdma );

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

	# FIX LATER:  range-validate many of the values we obtained from the config file

	$debug_summary = $debug_level >= 1;
	$debug_basic   = $debug_level >= 2;
	$debug_xml     = $debug_level >= 3;
	$debug_debug   = $debug_level >= 4;
	$debug_trace   = $debug_level >= 5;

	chomp $thisnagios;

	$async_upsert_hosts    = $use_async_upsert_hosts    ? 'true' : 'false';
	$async_upsert_services = $use_async_upsert_services ? 'true' : 'false';

	if ($send_state_changes_by_nsca) {
	    if ( $primary_parent eq '' ) {
		die "primary_parent must be non-empty if send_state_changes_by_nsca is true\n";
	    }
	    if ( $send_to_secondary_NSCA && $secondary_parent eq '' ) {
		die "secondary_parent must be non-empty if send_state_changes_by_nsca and send_to_secondary_NSCA are true\n";
	    }
	    if ( $max_messages_per_send_nsca < 1 ) {
		die "max_messages_per_send_nsca must be positive if send_state_changes_by_nsca is true\n";
	    }
	}
	if ($send_state_changes_by_gdma) {
	    if ( $gdma_install_base eq '' ) {
		die "gdma_install_base must be non-empty if send_state_changes_by_gdma is true\n";
	    }
	    if ( !-d $gdma_install_base ) {
		die "gdma_install_base must be an existing directory if send_state_changes_by_gdma is true\n";
	    }

	    # Set up the spoolfile path based on the platform we are running on.
	    $gdma_spool_filename = GDMAUtils::get_spool_filename($gdma_install_base);
	    if ( $max_unspooled_results_to_save < 0 ) {
		die "max_unspooled_results_to_save cannot be negative\n";
	    }
	}

	if ( $send_on_host_data_change ne '' ) {
	    @non_default_host_data_change = split( ' ', $send_on_host_data_change );
	    foreach my $field (@non_default_host_data_change) {
		if ( not $allowed_host_data_fields{$field} ) {
		    die "send_on_host_data_change contains unknown field \"$field\"\n";
		}
	    }
	}
	if ( $send_on_host_timing_change ne '' ) {
	    @non_default_host_timing_change = split( ' ', $send_on_host_timing_change );
	    foreach my $field (@non_default_host_timing_change) {
		if ( not $allowed_host_timing_fields{$field} ) {
		    die "send_on_host_timing_change contains unknown field \"$field\"\n";
		}
	    }
	}
	if ( $send_on_service_data_change ne '' ) {
	    @non_default_service_data_change = split( ' ', $send_on_service_data_change );
	    foreach my $field (@non_default_service_data_change) {
		if ( not $allowed_service_data_fields{$field} ) {
		    die "send_on_service_data_change contains unknown field \"$field\"\n";
		}
	    }
	}
	if ( $send_on_service_timing_change ne '' ) {
	    @non_default_service_timing_change = split( ' ', $send_on_service_timing_change );
	    foreach my $field (@non_default_service_timing_change) {
		if ( not $allowed_service_timing_fields{$field} ) {
		    die "send_on_service_timing_change contains unknown field \"$field\"\n";
		}
	    }
	}

	# To avoid duplication in how we generally calculate and in what fields we send to the XML Socket API if that
	# is in use, remove elements from the "non_default" arrays that might still be allowed there for convenience,
	# but that now reside primarily in the corresponding "default" arrays due to code evolution over time.
	#
	my %non_default_host_data_change      = ();
	my %non_default_host_timing_change    = ();
	my %non_default_service_data_change   = ();
	my %non_default_service_timing_change = ();
	@non_default_host_data_change     {@non_default_host_data_change}      = (1) x @non_default_host_data_change;
	@non_default_host_timing_change   {@non_default_host_timing_change}    = (1) x @non_default_host_timing_change;
	@non_default_service_data_change  {@non_default_service_data_change}   = (1) x @non_default_service_data_change;
	@non_default_service_timing_change{@non_default_service_timing_change} = (1) x @non_default_service_timing_change;
	delete @non_default_host_data_change     {@default_host_data_change};
	delete @non_default_host_timing_change   {@default_host_timing_change};
	delete @non_default_service_data_change  {@default_service_data_change};
	delete @non_default_service_timing_change{@default_service_timing_change};
	@non_default_host_data_change      = keys %non_default_host_data_change;
	@non_default_host_timing_change    = keys %non_default_host_timing_change;
	@non_default_service_data_change   = keys %non_default_service_data_change;
	@non_default_service_timing_change = keys %non_default_service_timing_change;

	# Check for obsolete fields in the "non_default" arrays.  We could have simply filtered out such fields by a
	# mechanism like that used just above, but that would leave the user confused as to why the obsolete field was
	# not accepted but is not working.  Or we could have checked for these obsolete fields earlier by just no longer
	# including them in the %allowed_..._fields hashes, but for items that used to be valid and are no longer because
	# the feeder itself has changed, it's better to be more informative about why the obsolete field is not recognized.
	#
	foreach my $obsolete_field (@obsolete_host_data_fields) {
	    die "$obsolete_field is no longer supported in send_on_host_data_change\n" if $non_default_host_data_change{$obsolete_field};
	}
	foreach my $obsolete_field (@obsolete_host_timing_fields) {
	    die "$obsolete_field is no longer supported in send_on_host_timing_change\n" if $non_default_host_timing_change{$obsolete_field};
	}
	foreach my $obsolete_field (@obsolete_service_data_fields) {
	    die "$obsolete_field is no longer supported in send_on_service_data_change\n" if $non_default_service_data_change{$obsolete_field};
	}
	foreach my $obsolete_field (@obsolete_service_timing_fields) {
	    die "$obsolete_field is no longer supported in send_on_service_timing_change\n" if $non_default_service_timing_change{$obsolete_field};
	}

	@host_data_change      = ( @default_host_data_change,      @non_default_host_data_change );
	@host_timing_change    = ( @default_host_timing_change,    @non_default_host_timing_change );
	@service_data_change   = ( @default_service_data_change,   @non_default_service_data_change );
	@service_timing_change = ( @default_service_timing_change, @non_default_service_timing_change );
    };
    if ($@) {
	chomp $@;
	$@ =~ s/^ERROR:\s+//i;

	## In most contexts, the caller will sleep as a result of this failure.
	## But just in case, a short nap here won't hurt.
	sleep 10;

	## If we printed to STDOUT, that would be captured in core/services/feeder-nagios-status/log/main/log
	## (though printing to STDERR might not be).  But that is a lonely place that few people will think
	## to look in.  Instead we return the error to the caller, in hopes that it can be output to the
	## standard logfile for this process before the process dies.
	return "ERROR:  Cannot read config file $config_file ($@).";
    }
    return undef;
}

sub freeze_logtime {
    $logtime = '[' . ( scalar localtime ) . '] ';
}

sub log_message {
    print LOG @_, "\n";
}

sub log_timed_message {
    freeze_logtime();
    print LOG $logtime, @_, "\n";
}

sub log_shutdown {
    my $caller_line       = ( caller(0) )[2];
    my $caller_subroutine = ( caller(1) )[3];
    log_timed_message "=== Shutdown request sensed at line $caller_line in $caller_subroutine; terminating (process $$). ===";
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

sub code_coordinates {
    my $package;
    my $filename;
    my $parent_line;
    my $grandparent_line;
    my $great_grandparent_line;
    my $myself;
    my $parent;
    my $grandparent;

    ( $package, $filename, $parent_line,            $myself )      = caller(0);
    ( $package, $filename, $grandparent_line,       $parent )      = caller(1);
    ( $package, $filename, $great_grandparent_line, $grandparent ) = caller(2);
    return "at $parent() line $parent_line, called from $grandparent, line $grandparent_line";
}

sub xml_time_text {
    my $timestamp = shift;
    if ( $timestamp <= 0 ) {
	return '';
    }
    else {
	my ( $seconds, $minutes, $hours, $day_of_month, $month, $year, $wday, $yday, $isdst ) = localtime($timestamp);
	return sprintf '%02d-%02d-%02d %02d:%02d:%02d', $year + 1900, $month + 1, $day_of_month, $hours, $minutes, $seconds;
    }
}

# ================================================================
# A NOTE ABOUT REST AND INTERNAL TIMESTAMP REPRESENTATIONS
# ================================================================
#
# In an earlier incarnation of this script, while we were developing support for the REST API,
# we tried to use a DateTime object as our internal representation of a timestamp.  This would
# allow great generality.  Unfortunately, when it came time to serialize such objects to return
# them to the REST API, the JSON::XS package turned out to be horribly inefficient.  We had to
# enable the convert_blessed option, and then when JSON::XS serialized an instance of DateTime,
# it took an inordinately long time to do so.  While we have no direct proof yet, it seems as
# though for each object so serialized, a new Perl interpreter would be spawned just to convert
# that one object via a call to our DateTime::TO_JSON() routine.  In aggregate across several
# timestamps within each application object and many application objects, this overhead was
# clearly unacceptable.
#
# Consequently, we had to find an alternative internal representation.  It had to have the full
# range of a DateTime, but not be subject to its conversion performance problem.  The two basic
# choices seem to be some sort of integer (either standard Unix epoch time, or a modified version
# that includes millisecond resolution), or a string in some canonical timezone that can never
# have comparison problems with any other similar string in the same timezone.
#
# Using an integer is by far the most efficient means of representing the data.  It avoids all
# the very expensive overhead of conversions on both ends of transfers (in both Perl and Java
# code).  Since we are trying to improve performance here, that would be the best choice overall,
# avoiding a ton of useless work.  The only downside would be the need to understand that for
# these fields, the field-comparison logic in routines like hostStatusChange(), such as:
#     if ( $el_host_field ne $cs_host_field ) { ...}
# would have to be generalized to use integer comparisons for timestamp fields.
#
# That said, given the current uncertain support for such integers when sending and receiving
# timestamp data to and from the REST API, as a short-term measure to get around the terrible
# performance of DateTime conversions when sending data to the REST API, we are therefore going
# to use a string instead of a DateTime object as our internal representation.  This will avoid
# the need to convert on the way out, as we would have to do with an integer representation if
# we could not specify an option for the REST API to accept integer timestamps.
#
# For our string timestamp, we choose this format for the time being:
#     YYYY-MM-DDThh:mm:ss.sss+0000
# THIS IS NOT AN ISO-8601 COMPLIANT FORMAT!  To get one, we need to have the trailing timezone
# information specified as +00:00 instead.  Once we get an updated DateTime package in our build,
# we will so change our internal representation, and test against the REST API to ensure that it
# correctly accepts this format.  This format does have one advantage over a standard Unix epoch
# integer timestamp, namely that it includes millisecond resolution.  Other than that, it offers
# no special advantages (over, say, a Unix epoch timestamp multiplied by 1000, to match the
# resolution sometimes offered in timestamps provided by the REST API).
#
# This format is chosen because it presently needs no later conversion for sending directly to
# the REST API.  We distinguish this internal format from the REST API format because the data
# we may receive from the REST API might be in any arbitrary timezone.
#
# Notice that this format expresses the timestamp always in UTC, never in the local timezone.
# All data-conversion routines must take that into account.  It avoids all issues of any local
# Daylight Savings Time adjustments, and all issues of comparison across timezones.  Its major
# defect is that it's more difficult for the humans in any other timezone to understand whether
# a particular timestamp is "correct" relative to some other representation in their own local
# timezone.

# FIX MAJOR:  (This comment was created during earlier development, and might now be inaccurate.)
# This script is currently calling rest_to_internal_time() on timestamp data
# fetched from Foundation, and unix_to_rest_time() on timestamp date fetched from the Nagios
# status log.  Then timestamps from these two sources are being directly string-compared to see
# whether they are out-of-order or at least different.  THIS MAKES NO SENSE.  The presence of
# the timezone offset completely throws off the validity of a direct string comparison.  What
# we need instead is to normalize both types of timestamp to some standard time representation,
# such as UTC, that has a known fixed timezone (as opposed to a floating timezone), and then
# when sending timestamp data out to Foundation, convert from our internal format to whatever
# format the REST API requires.  Best of all would be if we could both retrieve and send
# data in unadulterated UTC integers with one-second resolution (or perhaps with millisecond
# resolution, if we have to track and compare with status updates from other monitoring
# sources that might record their data with such resolution).  That would avoid all sorts of
# purely-wasteful conversions in both directions between the integral values which are used
# both in the PostgreSQL "timestamp without time zone" fields that we use, and inside Nagios
# and in the Nagios status files.

# See the Foundation REST API doc for supported timestamp formats.
# There is terrible ambiguity in the doc about what format to use for inserted
# timestamps, so we have simply settled on something that seems to work.
#
# UNIX timestamp in, Foundation REST API representation out.
sub unix_to_rest_time {
    my $timestamp = shift;
    if ( $timestamp <= 0 ) {
	## This value turns out to be accepted by the Java date parser, as representing
	## the standard UNIX epoch.  In spite of that, it won't otherwise accept a simple
	## integer timestamp representing some other time.
	return '0';
    }
    else {
	## This is what we might use if we didn't need to append a timezone offset.  Or
	## we could perhaps use a variant of this by calling gmtime($timestamp) instead,
	## and appending a fixed ".000+0000" string for sub-second and timezone components.
	## my ( $sec, $min, $hour, $dom, $mon, $year, $wday, $yday, $dst ) = localtime($timestamp);
	## return sprintf "%04d-%02d-%02dT%02d:%02d:%02d", $year + 1900, $mon + 1, $dom, $hour, $min, $sec;
	##
	## The Foundation REST API is interpreting a missing offset-from-GMT as meaning the
	## timestamp is specified as UTC rather than in the local timezone.  So here we use
	## strftime() to dig out that offset from the system, since we have no other obvious
	## way to obtain it.  I suppose we could have punted and actually converted the
	## timestamp to its component numbers using gmtime() instead, and then we would
	## already know the timezone (UTC) and could append a fixed +0000 or +00:00 string
	## to the result.  But that representation would be different from other forms that
	## are flying around, so it would be considerably confusing for debugging purposes,
	## when humans compare the timestamps.
	##
	## Note that %z here is generating a "-0700" string, not a "-07:00" string.  THIS IS
	## NOT ISO-8601 COMPLIANT.  ISO-8601 specifies an Extended format which includes
	## punctuation in the time portion of the full timestamp, plus an appended timezone
	## offset, but that format requires corresponding punctuation in the offset.  So in
	## fact we have a kind of non-standards-compliant mixed-mode representation in use.
	## When we decide to change that, we will either need some other conversion routine,
	## or we will need to use gmtime() and UTC as noted above.
	return strftime( "%FT%T.000%z", localtime($timestamp) );
    }
}

# We encapsulate the difference between REST and XML APIs in this one routine,
# so every call that needs time conversion doesn't need to make this test itself.
# FIX MAJOR:  We need to test to see whether or not DateTime->from_epoch() takes
# leap seconds into account.  If Nagios is not doing so when it displays time
# values, then it would probably be a mistake to do so here.  See the doc for
# the DateTime::Format::Epoch package for more detail, and hints as to what might
# need to be tested and an alternate way to perform this conversion.  Also see
# the doc for DateTime->leap_seconds().
sub unix_to_internal_time {
    ## From when we were using the REST string directly as the internal representation
    ## (WHICH IS THE WRONG THING TO DO, BECAUSE OF VARIABLE TIMEZONE OFFSETS):
    ## return $use_rest_api ? ( $use_utc ? $_[0] : unix_to_rest_time( $_[0] ) ) : xml_time_text( $_[0] );

    if ($use_rest_api) {
	if ($use_utc) {
	    return $_[0];
	}
	elsif ($_[0] == 0) {
	    ## Return the same value we use for null_internal_time() in this mode of operation.
	    return undef;
	}
	else {
	    my ( $sec, $min, $hour, $dom, $mon, $year, $wday, $yday, $dst ) = gmtime( $_[0] );
	    return sprintf "%04d-%02d-%02dT%02d:%02d:%02d.000+0000", $year + 1900, $mon + 1, $dom, $hour, $min, $sec;

	    # This is what we used to use when we used a DateTime object as our internal
	    # timestamp representation.
	    return DateTime->from_epoch( epoch => $_[0] )->set_formatter($internal_time_format);
	}
    }
    else {
	return xml_time_text( $_[0] );
    }

    # FIX MAJOR:  Probably just drop this, now that the cases above involve more
    # complex logic that is less amenable to consolidation into a single expression.
    #
    # FIX MAJOR:  This compact expression has not been converted away from using
    # a DateTime object to our string representation instead, nor to emit the
    # special undefined value for a null internal time.
    return $use_rest_api
      ? ( $use_utc ? $_[0] : DateTime->from_epoch( epoch => $_[0] )->set_formatter($internal_time_format) )
      : xml_time_text( $_[0] );
}

# We don't actually call this anywhere in this script, because for historical reasons we
# used the xml_time_text() output format directly as our internal format, when sending data
# to the Foundation XML API.  That was done because it matched the "2014-04-03 17:02:35"
# format in which we received data when querying the gwcollagedb database directly via
# routines in our CollageQuery.pm package, which we used in the XML mode of operation.
sub internal_to_xml_time {
    return $_[0];
}

# This routine is no longer in use.  We used it in earlier development when we were using
# DateTime as our internal representation of timestamps, but using JSON::XS to serialize
# such timestamps turned out to be horrendously expensive.  Drop this in a future release.
#
# Provide a JSON serializer for DateTime objects.  This depends on support
# within GW::RAPID for the convert_blessed option when converting to JSON.
sub DateTime::TO_JSON {
    ## This works as a JSON serializer, provided we have
    ## previously set the formatter for this DateTime object.
    return "$_[0]";

    # This works as a JSON serializer, even if we did not
    # previously set the formatter for this DateTime object.
    # (However, in early development testing, it seems to have the timezone offset
    # stuck at zero.  FIX MAJOR:  Track down why that is occurring, and fix it.)
    return $internal_time_format->format_datetime( $_[0] );
}

# Originally:  We haven't yet converted our code to call this routine, because we are
# currently using the REST external representation as our internal representation.
# THAT IS WRONG, because it will lead to incorrect timestamp comparisons.
#
# Later:  We haven't yet converted our code to call this routine, because instead we
# have already set the DateTime formatter on the internal-time representation when we
# converted to internal time within unix_to_internal_time() or rest_to_internal_time().
# That ought to suffice for use whenever we need to stringify an internal time.
#
# More precisely, because we are passing complex nested hashes to GW::RAPID routines,
# it would take extra processing when we collect up modified fields for a host or
# service, to tell whether or not it needs this conversion.  It's much easier to
# simply depend on JSON to make that determination at the time of serialization.
#
# Now:  We haven't yet converted our code to call this routine, because now when we
# are using the REST API, we are using an internal representation that is directly
# compatible with sending such data to the REST API.  So no explicit conversion is
# necessary, and we avoid the overhead of a subroutine call for an identity transform.
#
# FIX MAJOR:  If we ever try to set $use_utc, the strategy of never calling this
# routine will fail, since then there will be no calls to this routine to perform an
# integer-to-string conversion.  So if we use integers for our internal timestamp
# representation and we cannot set some GW::RAPID option to make that package directly
# accept such a form, we will need to move forward and install the currently missing calls.
sub internal_to_rest_time {
    ## From when we were using the REST string as the internal representation
    ## (WHICH IS THE WRONG THING TO DO, BECAUSE OF VARIABLE TIMEZONE OFFSETS):
    ## return $use_utc ? unix_to_rest_time( $_[0] ) : $_[0];

    ## From when we used a DateTime object as our internal representation.
    ## return $use_utc ? unix_to_rest_time( $_[0] ) : $internal_time_format->format_datetime( $_[0] );

    ## Now that we are using a REST-compatible internal representation, but with a
    ## fixed and frozen timezone (always UTC):
    return $use_utc ? unix_to_rest_time( $_[0] ) : $_[0];
}

# The Foundation REST API returns data typically returns timestamps in the form:
#     2014-04-01T10:38:21.254-0700
# For purposes of comparison with Nagios data, we need to cut this down to a form which is
# directly comparable (that is, we need to drop the millisecond and timezone information,
# since we are not using those fields).
sub rest_to_internal_time {
    ## FIX MAJOR:  Setting $use_utc to a false value is just plain wrong (though see
    ## below for why we still do so), as we noted earlier; that gets us the full text
    ## format which includes a super-confusing timezone offset.  If it is ever the
    ## case that two different data sources have different timezone-definition files
    ## installed, that will only make the problem even more confusing.  What we need
    ## instead is a format that dissolves away all such offsets and gives us back a
    ## UTC timestamp without any confusion at all about timezone or Daylight Savings
    ## Time, so we can directly compare that data against the UTC timestamps (based
    ## on simple UNIX epoch integers) seen in Nagios log data.
    ##
    ## FIX MAJOR:  On the other hand, we don't yet have the $use_utc code branch
    ## developed here.  Change the "0" here to a proper time conversion, truncating
    ## milliseconds (or perhaps always rounding any non-zero milliseconds up to the
    ## next second?) and taking the timezone offset into account.  Think carefully
    ## about the milliseconds issue, because it might matter when a host is being
    ## monitored by both Nagios and CloudHub.
    ##
    ## FIX MAJOR:  For the $use_utc case, should we use the POSIX strptime() and mktime()
    ## routines for this conversion?  Or use a nested mktime(getdate()) sequence?  Are
    ## they even available in the POSIX package?  strptime() seems to only handle strings
    ## which it assumes represent local time, without any attached timezone offset, unless
    ## the GNU extensions are used, which support a %z conversion descriptor.  Perhaps
    ## there is some CPAN package which handles this stuff in a known fashion.

    ## From when we were using the REST string as the internal representation
    ## (WHICH IS THE WRONG THING TO DO, BECAUSE OF VARIABLE TIMEZONE OFFSETS):
    ## return $use_utc ? 0 : $_[0];

    if ($use_utc) {
	## FIX MAJOR:  The "0" here needs fixing, as described in the comments above.
	return 0;
    }
    else {
	return undef if $_[0] eq '';
	my $time = $rest_time_parser->parse_datetime( $_[0] );
	if (defined $time) {
	    $time->set_formatter($internal_time_format);

	    # FIX MAJOR:  Test this to verify that this always prints in the UTC timezone.
	    # At the moment, I have no idea how that is being forced, if it is at all,
	    # unless it's being defaulted from some class-level standard default.
	    #
	    # Return the stringified form of the DateTime object.  This serialization
	    # works because we set the formatter for this DateTime object to print the
	    # value in our internal-time format.
	    return "$time";

	    # This is an alternative formulation that should also work, even if we did
	    # not previously set the formatter for this particular DateTime object.
	    # But if you want to use this, test it first with a variety of incoming
	    # timezone values, to make sure they all end up as UTC.
	    return $internal_time_format->format_datetime($time);
	}
	else {
	    ## This branch can be triggered when we retrieve a NULL value from
	    ## Foundation as the value of LastNotificationTime for a host; the
	    ## current REST API will turn that into an empty string.

	    # If we got a known non-timestamp value (for now, an empty string)
	    # from Foundation, we want to treat it internally within this feeder
	    # as an undefined value.  The desired effect is to use that situation
	    # to avoid sending any form of this value back to Foundation as an
	    # actual specific timestamp.  That suppression will take some special
	    # handling by the calling code, not here.
	    return undef if not $_[0];

	    # We could have used this representation instead.
	    # return null_internal_time() if not $_[0];

	    # This should never happen.
	    die "FATAL:  Cannot convert \"$_[0]\" to a valid DateTime object.";
	};
    }

    ## This is never used, because we should have always returned in the logic above.
    ## FIX MAJOR:  The "0" here needs fixing, as described in the comments above.
    return $use_utc ? 0 : $internal_time_format->parse_datetime( $_[0] )->set_formatter($internal_time_format);
}

# Return whatever representation of a NULL value that we need to use for a missing time value,
# depending on whatever mode we are running in.
sub null_internal_time {
    if ($use_rest_api) {
	if ($use_utc) {
	    return 0;
	}
	else {
	    ## The undef value is the simplest form, as it makes comparing supposed DateTime
	    ## objects (or a string representation of timestamps) for NULL status a trivial
	    ## operation. is_null_internal_time() will recognize it because we might generate it
	    ## via other means (e.g., as a possible return value from rest_to_internal_time()).
	    ## Also, if we have such a value in hand when we serialize some structure to be sent
	    ## to Foundation, it should be serialized as an unquoted "null" value (when expressed
	    ## in JSON by the GW::RAPID package) and therefore either not affect any existing
	    ## value in Foundation, or (eventually, better, and probably appropriate; see
	    ## GWMON-11446) it would entirely delete the associated property row from Foundation,
	    ## or otherwise set its value to NULL in the database.
	    ##
	    ## On the other hand, if we should attempt within this Perl code to directly
	    ## compare such an undef value with some DateTime object (no longer in use except
	    ## transiently during timestamp conversions, so this is not a problem), or with
	    ## another such undef value, then if we are not careful and recognize that we might
	    ## see a comparison-to-undef, we might not get the result we expect.  Where such
	    ## comparisons might be possible, the application must take care to accommodate the
	    ## possibilities, possibly recoding the data before the comparison.  That is the
	    ## strategy we take elsewhere in this program, which already has to handle other
	    ## types of undefined values in a similar fashion.
	    return undef;

	    ## Cloning the fixed value (creating an extra copy) seems excessive, since
	    ## we never modify timestamp values in this script.  It's simpler and more
	    ## efficient to just return the sentinel copy.
	    # return $epoch_DateTime->clone();

	    ## This was a possible choice back when we used DateTime as our internal timestamp
	    ## representation.
	    # return $epoch_DateTime;
	}
    }
    else {
	return '0';
    }
}

# We have defined this routine to be more general than it really needs to be for its present use,
# to automatically accommodate potential future changes elsewhere in this script.
sub is_null_internal_time {
    my $time = $_[0];

    if (defined $time) {
	if ($use_rest_api) {
	    if ($use_utc) {
		return ($time == 0);
	    }
	    else {
		## What we used to use here, when we used DateTime as our internal
		## representation when we were using the REST API.
		## return ($time == $epoch_DateTime);

		# What makes sense now that we use a string representation in this mode.
		return ($time eq '' or $time eq '1970-01-01T00:00:00.000+0000');
	    }
	}
	else {
	    return ($time eq '0');
	}
    }
    else {
	return 1;
    }
}

# Recode from what the Foundation REST API returns for certain boolean
# values to what we use internally to store equivalent values from Nagios.
sub internal_boolean {
    my $state = $_[0];
    return $state eq 'true' ? '1' : $state eq 'false' ? '0' : $state;
}

# We send certain floating-point data gleaned from Nagios to Foundation in the same
# format that Nagios printed it:
#
#   percent_state_change=%.2f
#
# But the Foundation REST API will return such data with trailing zeroes suppressed,
# so "12.10" becomes "12.1" and "0.00" becomes "0".  This will confuse our cache
# comparison unless we normalize the Foundation data back to the string form that we
# originally used to send the data.  This trivial formulation won't chop any excess
# digits beyond the standard precision, but we don't expect to see such input data.
sub internal_float {
    my $value = $_[0];
    return $value !~ /\./ ? "$value.00" : $value =~ /\.\d$/ ? "${value}0" : $value;
}

sub main {
    # If a "once" argument was passed on the command line, just run once to synchronize state between Nagios and Foundation.
    $sync_at_start = $ARGV[0] || 0;

    # Printing to STDOUT in this paragraph should be captured by the controlling dumblog process
    # into the core/services/feeder-nagios-status/log/main/log file, so it is not entirely lost
    # to view even though we could not open our normal logfile.
    my $config_error = read_config_file ($default_config_file);
    if ($logfile) {
	if ( !open( LOG, '>>', $logfile ) ) {
	    freeze_logtime();
	    print "${logtime}$config_error\n" if $config_error;
	    print "${logtime}FATAL:  Cannot open the logfile $logfile ($!); aborting!\n";
	    ## FIX MINOR:  follow the perf-data script model to record an error and send a summary log message to Foundation
	    sleep $failure_sleep_time if $failure_sleep_time;
	    return ERROR_STATUS;
	}
	LOG->autoflush(1);
    }
    elsif ($config_error) {
	## We didn't get far enough along to find where to persist the error message in a log file.
	## So let's just do the best we can at this point.
	freeze_logtime();
	print "${logtime}$config_error\n";
	sleep $failure_sleep_time if $failure_sleep_time;
	return ERROR_STATUS;
    }

    log_timed_message "=== Starting up (process $$). ===";
    print   "${logtime}=== Starting up (process $$). ===\n";
    print "This logfile is only useful if the feeder is so badly misconfigured that it\n";
    print "cannot complete the config-file analysis and startup processing.\n";
    print "Principal logging for the Nagios status feeder is being captured in this file:\n";
    print "    $logfile\n";
    print "See that file for all the detail you expected to see here.\n";
    ## Output a simple spacer line for clarity in the log between successive runs of this daemon.
    ## The dumblog process that captures and logs this STDOUT data suppresses any completely-empty
    ## lines it sees, so we must output a single space in the spacer line at a minimum.
    print " \n";
    ## Flush everything written to STDOUT so far, so it's quickly visible in any log file
    ## in which this output stream is captured (subject to independent buffering by the
    ## dumblog process that captures this output and logs it).  Curiously, the output is
    ## delayed a bit if we use "$| = 1;" but not much if we use "STDOUT->autoflush(1);"
    ## instead, even though these two mechanisms should be equivalent.  Regardless, this
    ## will set autoflush permanently on STDOUT, but that's not such a bad thing for a
    ## file descriptor we expect to use rarely.
    STDOUT->autoflush(1);

    if ($config_error) {
	## Finally, we get a chance to capture the error detail where it can be found.
	log_timed_message $config_error;
	sleep $failure_sleep_time if $failure_sleep_time;
	return ERROR_STATUS;
    }

    # Long term, this won't be an issue, because we will permanently switch over to the REST API.
    # But in the near term, when there might be some question about what API is in use for a particular run,
    # it's simplest to just reflect that choice into the logfile.
    log_timed_message "NOTICE:  Running in " . ( $use_rest_api ? 'REST' : 'XML' ) . " API mode.";

    if ($debug_summary) {
	## If requested (only), we re-open the STDERR stream as a duplicate of the
	## logfile stream, to capture any output written to STDERR (from, say, any
	## Perl errors or warnings generated by poor coding or unexpected input data).
	## FIX MINOR:  Why shouldn't we do this unconditionally?
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
    }

    # Set up to handle broken pipe errors.  This has to be done in conjunction with later code that
    # will cleanly process an EPIPE return code from a socket write.
    #
    # Our trivial signal handler turns SIGPIPE signals generated when we write to sockets already
    # closed by the server into EPIPE errors returned from the write operations.  The same would
    # happen if instead we just ignored these signals, but with this mechanism we also automatically
    # impose a short delay (inside the signal handler) when this situation occurs -- there is little
    # reason to keep pounding the server when it has already indicated it cannot accept data just now.
    $SIG{'PIPE'} = \&sig_pipe_handler;

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

    # We catch SIGHUP so we can dynamically reconfigure the daemon.  This capability is experimental,
    # and intended largely to allow changing logging levels without losing any current cached state.
    local $SIG{HUP} = \&handle_reconfigure_signal;

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
	sleep $failure_sleep_time;
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
	    sleep $failure_sleep_time if $failure_sleep_time;
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
    if ($use_rest_api) {
	if (@host_status_updates) {
	    $message_counter = output_bundle_to_rest_api( 'host', \@host_status_updates, $message_counter );
	    @host_status_updates = ();
	}
	if (@service_status_updates) {
	    $message_counter = output_bundle_to_rest_api( 'service', \@service_status_updates, $message_counter );
	    @service_status_updates = ();
	}
    }
    else {
	if (@xml_messages) {
	    ## Note that $message_counter may well be -1 at this point.
	    $message_counter = output_bundle_to_socket( \@xml_messages, $message_counter );
	    @xml_messages = ();
	}
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

# Support for dynamic reconfiguration is experimental.
sub handle_reconfigure_signal {
    my $signame = shift;
    $reconfig_requested = 1;

    # for developer debugging only
    # log_timed_message "ERROR:  Received SIG$signame; will reconfigure!";
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

sub initialize_rest_api {
    my $phase = $_[0];

    require GW::RAPID;

    # Basic security:  disallow code in the logging config data.
    Log::Log4perl::Config->allow_code(0);

    # Here we add custom logging levels to form our full standard complement.  There are six
    # predefined log levels:  FATAL, ERROR, WARN, INFO, DEBUG, and TRACE (in descending priority).
    # We add NOTICE and STATS levels to the default set of logging levels supplied by Log4perl,
    # to form the full useful set:  FATAL, ERROR, WARN, NOTICE, STATS, INFO, DEBUG, and TRACE
    # (excepting NONE, I suppose, though there is some hint in the code that OFF is also supported).
    # This *must* be done before the call to Log::Log4perl::init().
    if ($phase eq 'started') {
	Log::Log4perl::Logger::create_custom_level("NOTICE", "WARN");
	Log::Log4perl::Logger::create_custom_level("STATS", "NOTICE");
    }

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
	logger        => Log::Log4perl::get_logger("Nagios.Status.Feeder.GW.RAPID"),
	access        => $ws_client_config_file,
	interruptible => \$shutdown_requested,
	timeout       => $rest_api_timeout
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

sub connect_to_rest_api {
    my $phase = $_[0];
    if (not initialize_rest_api($phase)) {
	return 0;
    }
    push @rest_event_messages,
      {
	consolidationName => 'SYSTEM',
	appType           => 'SYSTEM',
	monitorServer     => 'localhost',
	host              => $thisnagios,
	device            => '127.0.0.1',
	severity          => 'OK',
	monitorStatus     => 'OK',
	textMessage       => "Foundation-Nagios status check process $phase.",
	reportDate        => unix_to_rest_time(time)
      };
    $message_counter = send_pending_events( $message_counter, 1 );
    return 1;
}

sub initialize_feeder {
    ## Pre-extend the xml_event_messages array for later efficiency, then truncate back to an empty state.
    $#xml_event_messages = $max_event_bundle_size;
    @xml_event_messages  = ();

    if ($use_rest_api) {
	if (not connect_to_rest_api('started')) {
	    log_timed_message "ERROR:  Cannot connect to the Foundation REST API.  Retrying in $failure_sleep_time seconds.";
	    sleep $failure_sleep_time;
	    return RESTART_STATUS;
	}
    }
    else {
	my $failed = 1;
	if ( my $socket =
	    IO::Socket::INET->new( PeerAddr => $foundation_host, PeerPort => $foundation_port, Proto => 'tcp', Type => SOCK_STREAM ) )
	{
	    $socket->autoflush();
	    log_timed_message 'Start message local port: ', $socket->sockport() if $debug_summary;
	    $failed = 0;
	    unless ( $socket->sockopt( SO_SNDTIMEO, pack( 'L!L!', $socket_send_timeout, 0 ) ) ) {
		log_socket_problem('setting send timeout on');
		$failed = 1;
	    }
	    unless ($failed) {
		log_timed_message 'Writing start message to Foundation.' if $debug_summary;
		unless ( $socket->print($start_message) ) {
		    log_socket_problem('writing to');
		    $failed = 1;
		}
		else {
		    LOG->print( $start_message, "\n" ) if $debug_xml;
		}
	    }
	    unless ($failed) {
		log_timed_message 'Writing close message to Foundation.' if $debug_summary;
		unless ( $socket->print($command_close) ) {
		    log_socket_problem('writing to');
		    $failed = 1;
		}
		else {
		    LOG->print( $command_close, "\n" ) if $debug_xml;
		}
	    }
	    unless ( close($socket) ) {
		log_socket_problem('closing');
		$failed = 1;
	    }
	}
	if ($failed) {
	    log_timed_message "Listener services not available. Retrying in $failure_sleep_time seconds.";
	    sleep $failure_sleep_time;
	    return RESTART_STATUS;
	}
    }

    my $init_start_time = Time::HiRes::time();
    log_timed_message 'loading cached addresses ...';
    load_cached_addresses() or return ERROR_STATUS;
    log_timed_message 'loading global nagios parameters ...';
    $global_nagios = get_globals($statusfile);
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
	my $full_dump = assemble_remote_full_dump( $element_ref, $collage_status_ref );
	my $last_full_dump_time = Time::HiRes::time();
	if ($send_state_changes_by_nsca) {
	    send_nsca( $primary_parent, $nsca_port, $nsca_timeout, $send_to_secondary_NSCA, $secondary_parent, $max_messages_per_send_nsca,
		$nsca_batch_delay, $full_dump );
	    $last_nsca_full_dump_time = $last_full_dump_time;
	}
	if ($send_state_changes_by_gdma) {
	    gdma_spool( $gdma_results_to_spool, $full_dump );
	    $last_gdma_full_dump_time = $last_full_dump_time;
	}
    }
    if ($shutdown_requested) {
	log_shutdown();
	return STOP_STATUS;
    }

    if ($debug_summary) {
	my $init_time = sprintf '%0.4F', ( Time::HiRes::time() - $init_start_time );
	log_timed_message "Startup init time=$init_time seconds.";
    }

    if ($debug_trace) {
	log_timed_message "Full dump of Foundation data:\n", Data::Dumper->Dump( [ \%{$collage_status_ref} ], [qw(\%{collage_status_ref})] );
    }

    # We count from 0 for the first cycle, after we increment it during the cycle.
    $cycle_number   = -1;
    $loop_count     = 0;
    $total_wait     = 0;
    $n_hostcount    = 0;
    $n_servicecount = 0;

    $next_sync_timeout  = time + $sync_timeout_seconds;
    $looping_start_time = Time::HiRes::time();

    log_timed_message 'starting main loop ...';

    return CONTINUE_STATUS;
}

sub terminate_feeder {
    terminate_rest_api() if $use_rest_api;
}

sub perform_feeder_cycle_actions {
    my $host_updates_blocked    = 0;
    my $service_updates_blocked = 0;
    my $sent_hosts_count        = 0;
    my $sent_services_count     = 0;

    my $start_time = Time::HiRes::time();
    log_timed_message 'Starting cycle.' if $debug_summary;

    ++$cycle_number;
    $total_wait += $cycle_sleep_time;

    # Dynamic reconfiguration support is experimental, and intended largely
    # to allow changing logging levels while the daemon is running, to better
    # understand failing code in a mid-operational context.  Note that, for
    # instance, we take no effort yet to reopen the $logfile in case that
    # value has changed.
    if ($reconfig_requested) {
	$reconfig_requested = 0;
	my $config_error = read_config_file ($default_config_file);
	if ($config_error) {
	    log_timed_message $config_error;
	    ## Sleeping for $failure_sleep_time will happen in the caller, as well.
	    ## But if we have a bad config file, a little extra delay won't hurt.
	    sleep $failure_sleep_time if $failure_sleep_time;
	    return ERROR_STATUS;
	}
	if ($use_rest_api) {
	    if (not connect_to_rest_api('reconfigured')) {
		log_timed_message "ERROR:  Cannot connect to the Foundation REST API.  Will retry shortly.";
		## Sleeping for $failure_sleep_time will happen in the caller, as well.
		## But if the REST API is not yet running, a little extra delay won't hurt.
		sleep $failure_sleep_time;
		return RESTART_STATUS;
	    }
	}
    }

    # Don't bother with this loop iteration if the input data hasn't changed since last time.
    my $statusfile_mtime = (stat($statusfile))[9];
    if ( !defined $statusfile_mtime ) {
	log_timed_message "WARNING: stat of file $statusfile failed: $!";
	## Sleeping for $failure_sleep_time will happen in the caller, as well.
	## But if the status file is not yet available, that probably means that Nagios is down,
	## so a little extra delay won't hurt.
	sleep $failure_sleep_time;
	return ERROR_STATUS;
    }
    elsif ($statusfile_mtime <= $last_statusfile_mtime) {
	log_message "Skipping cycle -- $statusfile has not changed.";
    }
    else {
	$last_statusfile_mtime = $statusfile_mtime;

	if ( $total_wait >= $local_full_update_time ) {
	    $total_wait = 0;
	    if ($smart_update) {
		## Time to send heartbeat. That is, time to update LastUpdateTime stamps.
		$heartbeat_mode = 1;
		log_message "Heartbeat in progress this cycle ..." if $debug_summary;
	    }
	}

	# Get the status and counts from Nagios.
	$element_ref = get_status( $statusfile, $nagios_version, $element_ref );
	if ($shutdown_requested) {
	    log_shutdown();
	    return STOP_STATUS;
	}
	if ( !defined($element_ref) ) {
	    return RESTART_STATUS;
	}

	# FIX MAJOR:  This simple local cache initialization DOES NOTHING to set in motion any
	# initial data copying to a parent server that also needs to happen.
	if ( $skip_initialization and $loop_count == 0 ) {
	    $skip_initialization = 0;
	    ## Copy all the available Nagios status data to our data cache, then skip the rest of this first cycle.
	    my $el_hosts = $element_ref->{Host};
	    foreach my $hostkey ( keys %{$el_hosts} ) {
		hostStatusUpdate( $element_ref, $collage_status_ref, $hostkey );
		my $el_svcs = $el_hosts->{$hostkey}->{Service};
		if ( defined $el_svcs ) {
		    foreach my $servicekey ( keys %{$el_svcs} ) {
			serviceStatusUpdate( $element_ref, $collage_status_ref, $hostkey, $servicekey );
		    }
		}
	    }
	    return CONTINUE_STATUS;
	}

	# Here we verify that we don't have a serious mismatch between what is in Nagios and what
	# is in Foundation, so we can tell whether we need either to hold off reporting status,
	# or simply to restart this script entirely.  Historically, counting hosts and services was
	# used as a simple proxy to tell whether Nagios and Foundation were synchronized.  But that
	# proxy is really too simple; what happens if you have one object added and another object
	# deleted on one side of this comparison?  The counts would remain balanced even though
	# the shapes would be mismatched.  We need a more-complex but still fast way to check for
	# sufficiently-equivalent shapes, noting that this check will be run on every feeder cycle
	# where the status file is seen to have changed (which is very often; see the "Aggregated
	# status data update interval" in Configuration > Control > Nagios Main Configuration Page 1).
	#
	# Further thinking reveals that we don't really need full equivalance between our notions of
	# what is in Nagios and what is in Foundation.  All we need is a partial order imposed by
	# inclusion on pairs of sets, so that {set A} <= {set B} iff {set A} is a (possibly improper)
	# subset of {set B}.  In particular, this would apply to successive pulls of Nagios status
	# data; we're okay if we see only deletions of hosts and services in the new copy compared
	# with the old copy, and no adds, as long as all of our processing is driven by the subset.
	# The same would apply to comparing Nagios data with Foundation data; we're okay if all the
	# Nagios host and services show up in Foundation, but we don't really care (for purposes of
	# this script) whether all the Foundation data is also present in Nagios.

	# Note:  Unlike in getInitialState(), the calls to CollageQuery within FoundationIsSynced()
	# (called below in this routine) should be purposely set up to not die immediately should
	# a termination signal be received while the queries are running.  That's for two reasons.
	# One, the calls we use here are simple "select count(*)" queries that we don't expect to
	# run terribly long.  (Note:  that reasoning is now wrong on two counts:  unlike MySQL,
	# PostgreSQL's count(*) is not necessarily fast; and our axioms of the universe have changed
	# since that claim was originally made, and the Foundation ApplicationType can no longer be
	# used by itself as a simple filter to identify the hosts of interest, so the queries we need
	# are now more complex than simple count(*) retrievals.)  And two, in this part of the logic,
	# we want to allow the caller an opportunity to clean up and flush any pending data before we
	# exit the process.  (That reasoning still holds.)
	#
	# FIX MINOR:  Given the change of database and the changed nature of the queries we now need
	# to execute here, revisit the treatment of the termination signal in the FoundationIsSynced()
	# routine, and see how we can safely interrupt a long database query and return control to the
	# application code without killing the entire script.  Run an initial test by setting up a
	# long-running transaction from some other connection, that will block the queries we attempt
	# here.  Here's an example, using the monarch database.  In the outside process, run:
	#     begin transaction;
	#     update hosts set notes='barfoo' where name='localhost';
	# which will set up a row lock.  Then in this script, run:
	#     update hosts set notes='abcdef' where name='localhost';
	# which will wait for the first transaction to either commit or roll back.  Then try to interrupt
	# this script, and see what happens.  Alternatively, without involving any external process, we
	# could simply run a "select pg_sleep(1000);" query from within this client process, to pause the
	# server process (and therefore this script) for that many seconds.  Also see if the PostgreSQL
	# driver has the same difficulty in allowing an interruption in the middle of a long query as the
	# MySQL driver does (that is, does the PostgreSQL driver effectively also internally restart an
	# interrupted system call instead of bubbling up that exception?).

	if ($debug_basic) {
	    ## We no longer compute or report corresponding counts from Foundation, since
	    ## with the addition of support for CloudHub-managed hosts in the system, extracting
	    ## that information is now significantly more expensive than it used to be.  If
	    ## we wanted to do so, we would need to complete a full dataset-shape comparison
	    ## within FoundationIsSynced() instead of short-circuiting if the comparison fails.
	    log_message "Nagios Host Count: $n_hostcount";
	    log_message "Nagios Service Count: $n_servicecount";
	}

	# The first cycle will not have counts from a previous cycle, so we initialize them here.
	if ( $cycle_number == 0 ) {
	    $last_n_hostcount    = $n_hostcount;
	    $last_n_servicecount = $n_servicecount;
	}

	my $synced = 1;  # Presumed innocent until proven guilty.
	if ( $normal_sync_check_period > 0 && $last_sync_check_time + $normal_sync_check_period < $start_time ) {
	    ## Now we must compare datasets to see if Nagios and Foundation are really in sync.
	    $synced = FoundationIsSynced($element_ref);
	    if ($shutdown_requested) {
		log_shutdown();
		return STOP_STATUS;
	    }
	    if ( not defined $synced ) {
		## The specific reason for this failure should have already been logged.
		return ERROR_STATUS;
	    }
	    ## We only update the sync check time upon an actual successful check.  If the check was
	    ## unsuccessful, we want to continue checking on every subsequent cycle until we get a
	    ## good check, even though this imposes an extra load on the system during this period, to
	    ## make sure we don't mistakenly feed data downstream when we ought not to be doing so.
	    $last_sync_check_time = $start_time if $synced;
	}
	if ($synced) {
	    ## We're in sync, so theoretically we should go forward with feeding.
	    $enable_feeding = 1;
	    $syncwaitcount  = 0;
	    ## However, let's make one other check, to ensure that not only the external data is synchronized,
	    ## but also that the script's own copy of the data has remained synchronized across cycles.
	    if ( ( $n_hostcount ne $last_n_hostcount ) or ( $n_servicecount ne $last_n_servicecount ) ) {
		## In sync, but changed since the last cycle.  We missed the sync (it happened while we were asleep
		## between cycles, so we had no direct interaction with it), so just re-start.  That will allow us
		## to bring our cached copy of Foundation data back into sync with what is in the database.
		log_timed_message 'In sync now, but changed since the last cycle (missed sync). Restarting without feeding status data this cycle.';
		return RESTART_STATUS;
	    }
	}
	else {
	    ## FIX MAJOR:  Explain how this can happen.
	    ## Hold off on updates for a bit, because Nagios and Foundation are not synced.  With the proper
	    ## synchronization code for this script now in play, this should never happen.  We keep the body
	    ## of this clause around mainly to generate the out-of-sync message in case somehow the unexpected
	    ## happens.  We don't want to be feeding during this time both to prevent the feeder from
	    ## inadvertently causing Foundation to add back some hosts or services that were intentionally
	    ## deleted, and to reduce the load so any Foundation-internal operations which are still finishing
	    ## a sync operation are given that much more system resources to complete their work.  The tradeoff
	    ## is that during this period, we do consume some extra resources when we continue to check to see
	    ## if the sync has finally completed.
	    if ( $syncwaitcount < $syncwait ) {
		## We still need to wait for a sync to complete.
		$enable_feeding = 0;
		$syncwaitcount++;
		my $cycles_left = $syncwait - $syncwaitcount;
		log_timed_message "Out of sync detected! Waiting on updates without feeding status data, for up to $cycles_left more cycles ...";
	    }
	    else {
		## We're still not in sync, but we're tired of waiting so long and don't want to prevent all
		## status updates forever.  So we're going to run a cycle of feeding.  On the next cycle, if
		## we're still out of sync, we'll resume waiting again.
		$enable_feeding = 1;
		$syncwaitcount  = 0;

		## Legacy debug statements, presumed no longer useful now that the nature of the hosts we are
		## tracking has changed.  Left here for now only for emergency use, this code will be removed
		## in a future release.
		if ( 0 && $debug_basic ) {
		    my $deltas = find_deltas( $element_ref, $collage_status_ref );
		    log_message "Hosts and/or services in Nagios and not in Foundation:";
		    log_message( Data::Dumper->Dump( [ \%{ $deltas->{NagiosHost} } ], [qw(Nagios)] ) );
		    log_message "Hosts and/or services in Foundation and not in Nagios:";
		    log_message( Data::Dumper->Dump( [ \%{ $deltas->{FoundationHost} } ], [qw(Foundation)] ) );
		}

		# FIX MINOR:  In earlier versions of this script, we only emitted log and console messages in this situation
		# if we had previously fed some amount of status data.  I'm not sure why the operators wouldn't want to know
		# that they might need to take corrective action even if no data has been previously sent in this run of the
		# script.  So I am disabling the loop-count condition here.  A later release will perhaps drop it entirely.
		if ( 1 or $loop_count != 0 ) {
		    log_timed_message 'Out of sync for too long!! Please try commit again. Feeding status data anyway, for one cycle.';
		    if ($send_sync_warning) {
			if ($use_rest_api) {
			    my %out_of_sync_event = (
				consolidationName => 'SYSTEM',
				appType           => 'SYSTEM',
				monitorServer     => 'localhost',
				device            => '127.0.0.1',
				severity          => 'WARNING',
				monitorStatus     => 'WARNING',
				textMessage       => 'Nagios and Foundation are out of sync.'
				  . ' You may need to commit your Nagios configuration again.'
				  . ' Check the log at /usr/local/groundwork/foundation/container/logs/nagios2collage_socket.log for details.'
				  . " Nagios knows of $n_hostcount hosts, $n_servicecount services.",
				reportDate        => unix_to_rest_time(time)
			    );
			    my %outcome = ();
			    my @results = ();
			    if ( not $rest_api->create_events( [ \%out_of_sync_event ], {}, \%outcome, \@results ) ) {
				log_outcome \%outcome, 'creation of out-of-sync event';
				log_results \@results, 'creation of out-of-sync event';
			    }
			}
			else {
			    unless (
				my $socket = IO::Socket::INET->new(
				    PeerAddr => $foundation_host,
				    PeerPort => $foundation_port,
				    Proto    => 'tcp',
				    Type     => SOCK_STREAM
				)
			      )
			    {
				log_timed_message 'Foundation listener services are not available.';
				## We don't "return RESTART_STATUS;" here or on subsequent socket failures because
				## the message we're about to submit is just advisory, and the opportunity to submit
				## the same message will probably appear again in a later processing cycle.
			    }
			    else {
				$socket->autoflush();
				log_timed_message 'Out-of-sync message local port: ', $socket->sockport() if $debug_summary;
				unless ( $socket->sockopt( SO_SNDTIMEO, pack( 'L!L!', $socket_send_timeout, 0 ) ) ) {
				    log_socket_problem('setting send timeout on');
				}
				else {
				    unless (
					$socket->print(
    "<GENERICLOG consolidation='SYSTEM' ApplicationType='SYSTEM' MonitorServerName='localhost' Device='127.0.0.1' Severity='WARNING' MonitorStatus='WARNING' TextMessage='Nagios and Foundation are out of sync. You may need to commit your Nagios configuration again. Check the log at /usr/local/groundwork/foundation/container/logs/nagios2collage_socket.log for details. Nagios knows of $n_hostcount hosts, $n_servicecount services.' />"
					)
				      )
				    {
					log_timed_message 'Failed writing log message to Foundation.' if $debug_summary;
					log_socket_problem('writing to');
				    }
				}
				unless ( close($socket) ) {
				    log_socket_problem('closing');
				}
			    }
			}
		    }
		}
	    }
	}

	# Now reset the counts for next time.
	$last_n_hostcount    = $n_hostcount;
	$last_n_servicecount = $n_servicecount;

	if ( $element_ref && $enable_feeding ) {
	    if ($heartbeat_mode) {
		$global_nagios = get_globals( $statusfile );
		if ( !defined($global_nagios) ) {
		    return RESTART_STATUS;
		}
	    }

	    my $state_changes = ( $send_state_changes_by_nsca || $send_state_changes_by_gdma ) ? [] : undef;
	    if ($use_rest_api) {
		my $host_updates_ref = find_host_status_changes( $thisnagios, $element_ref, $collage_status_ref, $state_changes );
		return RESTART_STATUS if not defined $host_updates_ref;
		my $serv_updates_ref = find_service_status_changes( $thisnagios, $element_ref, $collage_status_ref, $state_changes );
		return RESTART_STATUS if not defined $serv_updates_ref;
		push( @host_status_updates,    @{$host_updates_ref} );
		push( @service_status_updates, @{$serv_updates_ref} );
	    }
	    else {
		my $host_updates_ref = build_host_xml( $thisnagios, $element_ref, $collage_status_ref, $state_changes );
		return RESTART_STATUS if not defined $host_updates_ref;
		my $serv_updates_ref = build_service_xml( $thisnagios, $element_ref, $collage_status_ref, $state_changes );
		return RESTART_STATUS if not defined $serv_updates_ref;
		push( @xml_messages, @{$host_updates_ref} );
		push( @xml_messages, @{$serv_updates_ref} );
	    }

	    if ( defined($state_changes) && @$state_changes ) {
		if ($send_state_changes_by_nsca) {
		    send_nsca( $primary_parent, $nsca_port, $nsca_timeout, $send_to_secondary_NSCA, $secondary_parent,
			$max_messages_per_send_nsca, $nsca_batch_delay, $state_changes );
		}
		if ($send_state_changes_by_gdma) {
		    gdma_spool($gdma_results_to_spool, $state_changes);
		}
	    }

	    # In the REST API mode of operation, if $continue_after_blockage is set, we will no longer
	    # restart this daemon if Foundation rejects data submission because it is overloaded with
	    # prior submissions.  We will instead note that fact and avoid updating our cache of what
	    # Foundation contains, so a future cycle can attempt to re-send any accumulated deltas.
	    # However, we must notice that this strategy is potentially unreliable.  If some of the
	    # changed data did get to Foundation, and we do not update our cache, then our cache will
	    # misrepresent the data in Foundation.  If on the next cycle the Nagios data goes back
	    # to what our cache contains, then we will no longer send any deltas for such a field to
	    # Foundation, but Foundation will persistently contain the wrong data until and unless it
	    # just so happens to change again in Nagios.  A complementary effect is also possible,
	    # as described below.  So when we adopt this strategy of not just restarting because our
	    # sending failed, we must manage the cache very carefully.

	    # In this area of the code, we check both before and after potentially long-running i/o calls
	    # to see if we have been asked to terminate the process.  That provides the best response time
	    # to a shutdown request, at the cost of possibly throwing away some amount of status updates.
	    my $sent_stuff = 0;
	    if ($use_rest_api) {
		if (   ( @host_status_updates + @service_status_updates >= $rest_bundle_size )
		    || ( @host_status_updates + @service_status_updates > 0 && time >= $next_sync_timeout ) )
		{
		    if ($shutdown_requested) {
			log_shutdown();
			return STOP_STATUS;
		    }

		    # We look at the returned value from these calls to output_bundle_to_rest_api()
		    # to attempt to determine whether or not the data got through.  But in fact,
		    # that determination is unreliable, because the bundle as a whole is chopped up
		    # into segments of at most $max_rest_bundle_size elements, and some may have
		    # been successfully sent while others may not have made it through.  Thus the
		    # following scenario may apply.  Some (but not all) of data gets through, and
		    # then the server declares it is overloaded.  Now what are we to do?
		    #
		    # If we update our cache for all the hosts and services, then for data not sent
		    # to Foundation, if the Nagios data was different on this cycle but remains
		    # stable after that, Foundation will never see it updated to the correct value
		    # because subsequent cycles will not see a delta that needs to be forwarded.
		    #
		    # If we don't update our cache, then for data that did make it through to
		    # Foundation, if the Nagios data was different on this cycle, our cache will
		    # represent old data and be inaccurate.  Now if on the next cycle the Nagios
		    # data reverts back to the value in our cache and remains stable after that,
		    # Foundation will never see it updated to the correct value because subsequent
		    # cycles will never see a delta that needs to be forwarded.
		    #
		    # The only way out of this dilemma is to be more precise about what data did get
		    # through and what data did not get through, by returning info about how many
		    # updates got through, and by determining which hosts and host services were in
		    # each bundle.  We do so here via the $sent_hosts_count and $sent_services_count
		    # variables.  Then if we got blocked, we update our cache for only the data that
		    # got through.  But that requires that we pick apart the data we tried to send
		    # and use it as the basis for updating hosts and services in the cache.

		    $message_counter = output_bundle_to_rest_api( 'host', \@host_status_updates, $message_counter, \$sent_hosts_count );
		    return RESTART_STATUS if $message_counter < 0;
		    if ( $message_counter == 0 ) {
			$host_updates_blocked = 1;
			if ( $use_rest_api and $smart_update and $continue_after_blockage ) {
			    ## Regenerate @hosts_to_cache from @host_status_updates and $sent_hosts_count,
			    ## to contain only the hosts whose data actually got sent.
			    @hosts_to_cache = ();
			    for ( my $i = 0 ; $i < $sent_hosts_count ; ++$i ) {
				push @hosts_to_cache, $host_status_updates[$i]{hostName};
			    }
			}
		    }
		    @host_status_updates = ();

		    if ($shutdown_requested) {
			log_shutdown();
			return STOP_STATUS;
		    }

		    $message_counter =
		      output_bundle_to_rest_api( 'service', \@service_status_updates, $message_counter, \$sent_services_count );
		    return RESTART_STATUS if $message_counter < 0;
		    if ( $message_counter == 0 ) {
			$service_updates_blocked = 1;
			if ( $use_rest_api and $smart_update and $continue_after_blockage ) {
			    ## Regenerate %host_services_to_cache from @service_status_updates and $sent_services_count,
			    ## to contain only the host services whose data actually got sent.
			    %host_services_to_cache = ();
			    for ( my $i = 0 ; $i < $sent_services_count ; ++$i ) {
				push @{ $host_services_to_cache{ $service_status_updates[$i]{hostName} } },
				  $service_status_updates[$i]{description};
			    }
			}
		    }
		    @service_status_updates = ();

		    if ($shutdown_requested) {
			log_shutdown();
			return STOP_STATUS;
		    }

		    $sent_stuff = 1;
		}
	    }
	    else {
		if ( @xml_messages >= $xml_bundle_size || ( @xml_messages > 0 && time >= $next_sync_timeout ) ) {
		    if ($shutdown_requested) {
			log_shutdown();
			return STOP_STATUS;
		    }

		    $message_counter = output_bundle_to_socket( \@xml_messages, $message_counter );
		    return RESTART_STATUS if ( $message_counter < 0 );
		    @xml_messages = ();

		    if ($shutdown_requested) {
			log_shutdown();
			return STOP_STATUS;
		    }

		    $sent_stuff = 1;
		}
	    }

	    if ( $host_updates_blocked or $service_updates_blocked ) {
		log_timed_message "WARNING:  Processing cycle has been aborted due to server-side blockage.";

		if ($overloaded_cycles == 0) {
		    my %cycle_abort_event = (
			consolidationName => 'SYSTEM',
			appType           => 'SYSTEM',
			monitorServer     => 'localhost',
			device            => '127.0.0.1',
			severity          => 'PERFORMANCE',
			monitorStatus     => 'WARNING',
			textMessage       => 'Nagios status feeder cycle has been aborted due to server-side blockage.'
			  . ' Some Foundation data may become stale.',
			reportDate        => unix_to_rest_time(time)
		    );
		    my %outcome = ();
		    my @results = ();
		    if ( not $rest_api->create_events( [ \%cycle_abort_event ], {}, \%outcome, \@results ) ) {
			log_outcome \%outcome, 'creation of cycle-abort event';
			log_results \@results, 'creation of cycle-abort event';
		    }
		}
		if (++$overloaded_cycles >= $overload_event_frequency) {
		    $overloaded_cycles = 0;
		}

		## If $continue_after_blockage is not set, restart this daemon.
		$message_counter = -1 if not $continue_after_blockage;
	    }
	    else {
		$overloaded_cycles = 0;
	    }

	    # To support aborting a cycle due to server-side blockage, we need to update our cache here instead of
	    # running it from inside the find_host_status_changes() and find_service_status_changes() routines as
	    # we used to.  That's because our cache is supposed to reflect what we believe Foundation contains, and
	    # if we know it wasn't updated for certain hosts or host services, our cache must not change for those
	    # hosts and host services.  But we only find out whether it got updated after we try sending the data.
	    if ( $use_rest_api and $smart_update and $continue_after_blockage ) {
		## Update as much of our hosts cache as got sent to Foundation.
		foreach my $hostkey (@hosts_to_cache) {
		    hostStatusUpdate( $element_ref, $collage_status_ref, $hostkey );
		}
		## Update as much of our services cache as got sent to Foundation.
		foreach my $hostkey ( keys %host_services_to_cache ) {
		    foreach my $servicekey ( @{ $host_services_to_cache{$hostkey} } ) {
			serviceStatusUpdate( $element_ref, $collage_status_ref, $hostkey, $servicekey );
		    }
		}
	    }

	    if ($sent_stuff) {
		$next_sync_timeout = time + $sync_timeout_seconds;
		$loop_count++;
		if ($debug_summary) {
		    my $cycle_time = sprintf '%0.3f', Time::HiRes::time() - $start_time;
		    ## A "loop" is defined as all the processing for a single push of data to Foundation.  It may encompass several cycles.
		    ## "Loop time" is the useful processing time for such loops, not including any sleep time between cycles.
		    my $avg_loop_time = sprintf '%0.3f',
		      ( Time::HiRes::time() - $looping_start_time - ( $cycle_number * $cycle_sleep_time ) ) / $loop_count;
		    log_timed_message "Loops Completed = $loop_count.  Last cycle time = $cycle_time seconds. Avg loop time = $avg_loop_time seconds.";
		}
	    }
	}

	# quit after just one run -- legacy, now used only for development testing
	if ( $enable_feeding && $sync_at_start =~ /once/ ) {
	    log_timed_message 'Exiting after one cycle, per command option.';
	    return STOP_STATUS;
	}

	$heartbeat_mode = 0;
    }

    # Send any pending state transitions left in the buffer.
    $message_counter = send_pending_events( $message_counter, 1 ) if not $host_updates_blocked and not $service_updates_blocked;

    my $now = Time::HiRes::time();
    my $send_nsca_full_dump = $send_state_changes_by_nsca && ($nsca_full_dump_interval > 0) &&
	(($now - $last_nsca_full_dump_time) > $nsca_full_dump_interval);
    my $send_gdma_full_dump = $send_state_changes_by_gdma && ($gdma_full_dump_interval > 0) &&
	(($now - $last_gdma_full_dump_time) > $gdma_full_dump_interval);

    if (@$gdma_results_to_spool) {
	gdma_spool($gdma_results_to_spool, []);
    }
    if ( $send_nsca_full_dump || $send_gdma_full_dump ) {
	my $full_dump = assemble_remote_full_dump( $element_ref, $collage_status_ref );
	if ($send_nsca_full_dump) {
	    send_nsca( $primary_parent, $nsca_port, $nsca_timeout, $send_to_secondary_NSCA, $secondary_parent, $max_messages_per_send_nsca,
		$nsca_batch_delay, $full_dump );
	    $last_nsca_full_dump_time = $now;
	}
	if ($send_gdma_full_dump) {
	    gdma_spool( $gdma_results_to_spool, $full_dump );
	    $last_gdma_full_dump_time = $now;
	}
    }

    return ($message_counter < 0) ? RESTART_STATUS : CONTINUE_STATUS;
}

sub load_cached_addresses() {
    ## Get hosts->IPaddress from Monarch
    my ( $dbname, $dbhost, $dbuser, $dbpass, $dbtype ) = CollageQuery::readGroundworkDBConfig('monarch');
    my $dsn = '';
    if ( defined($dbtype) && $dbtype eq 'postgresql' ) {
	$dsn = "DBI:Pg:dbname=$dbname;host=$dbhost";
    }
    else {
	$dsn = "DBI:mysql:database=$dbname;host=$dbhost";
    }
    my $dbh = DBI->connect( $dsn, $dbuser, $dbpass, { 'AutoCommit' => 1 } );
    if ( !$dbh ) {
	log_message "Can't connect to database $dbname. Error: ", $DBI::errstr;
	return 0;
    }
    my $query = 'select name, address from hosts;';
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
    my $foundation;

    if ($skip_initialization) {
	return $collage_status_ref;
    }

    if ($use_rest_api) {
	## FIX MAJOR:  fill in equivalent REST code here, if any is required (doubtful)
    }
    else {
	## In this routine, we set up to die instantly if certain database calls are interrupted by a signal.
	## The $foundation->getHostServices() call in particular can take a considerable amount of time, but
	## some of its internal database-access components (DBD::mysql, certainly; DBD::Pg, possibly) are
	## effectively not interruptible by signals (the EINTR return code from some internal system call is
	## recognized and the interrupted system call is restarted, instead of having some means to check a
	## cancel-is-requested flag and stop the request).  This script is instrumented to effectively return as
	## quickly as signals are recognized by Perl, but that might be far too long for outside applications
	## to wait for the death of this script once it has been signaled to terminate, especially on a very
	## busy system (typically, one where the available disk i/o is saturated).  Fortunately, we know by code
	## inspection that there are no resources that need flushing or cleaning up before we exit here.
	eval {
	    ## local $SIG{INT}  = \&die_upon_exit_signal;
	    ## local $SIG{QUIT} = \&die_upon_exit_signal;
	    ## local $SIG{TERM} = \&die_upon_exit_signal;
	    local $SIG{INT}  = 'DEFAULT';
	    local $SIG{QUIT} = 'DEFAULT';
	    local $SIG{TERM} = 'DEFAULT';
	    $foundation = CollageQuery->new();
	};
	if ($@) {
	    chomp $@;
	    log_message $@;
	    return undef;
	}
    }
    log_timed_message '... getting Nagios status ...';
    $element_ref = get_status( $statusfile, $nagios_version, {} );
    if ($shutdown_requested) {
	return undef;
    }
    if ( !defined($element_ref) ) {
	return undef;
    }
    if ($debug_trace) {
	log_timed_message( Data::Dumper->Dump( [ \%{$element_ref} ], [qw(\%element_ref)] ) );
    }
    log_timed_message '... getting hosts ...';
    ## With the advent of CloudHub, we can no longer call $foundation->getHosts() (for the
    ## non-REST-API branch) and just get the NAGIOS-related hosts, because CloudHub might
    ## own some hosts (as VEMA or CHRHEV application types, for instance) that we need to
    ## deal with here.  So we must inefficiently drag back information on all hosts known
    ## to Foundation, and filter them below to ignore the ones we don't really care about.
    my $fn_hosts = undef;
    if ($use_rest_api) {
	$fn_hosts = get_foundation_hosts(undef);
	if ( not defined $fn_hosts ) {
	    log_timed_message "ERROR:  Cannot find hosts in Foundation.";
	    return undef;
	}
    }
    else {
	eval {
	    ## local $SIG{INT}  = \&die_upon_exit_signal;
	    ## local $SIG{QUIT} = \&die_upon_exit_signal;
	    ## local $SIG{TERM} = \&die_upon_exit_signal;
	    local $SIG{INT}  = 'DEFAULT';
	    local $SIG{QUIT} = 'DEFAULT';
	    local $SIG{TERM} = 'DEFAULT';
	    $fn_hosts = $foundation->getHostsByType(undef);
	};
	if ($@) {
	    chomp $@;
	    log_timed_message "Error in getHostsByType: $@";
	    return undef;
	}
    }
    if ($shutdown_requested) {
	return undef;
    }
    log_timed_message '... getting host services ...';
    my $fn_host_services = undef;
    if ($use_rest_api) {
	$fn_host_services = get_foundation_host_services('NAGIOS');
	if ( not defined $fn_host_services ) {
	    log_timed_message "ERROR:  Cannot find host services in Foundation.";
	    return undef;
	}
    }
    else {
	eval {
	    ## local $SIG{INT}  = \&die_upon_exit_signal;
	    ## local $SIG{QUIT} = \&die_upon_exit_signal;
	    ## local $SIG{TERM} = \&die_upon_exit_signal;
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
    }
    if ($shutdown_requested) {
	return undef;
    }
    log_timed_message '... processing host/service state ...';
    my $el_hosts = \%{ $element_ref->{Host} };
    my $cs_hosts = \%{ $collage_status_ref->{Host} };
    if ( ref($fn_hosts) eq 'HASH' ) {
	foreach my $host ( keys %{$fn_hosts} ) {
	    my $fn_host = $fn_hosts->{$host};
	    my $el_host = $el_hosts->{$host};
	    ## Ignore hosts that are not directly owned by NAGIOS, have no NAGIOS-related services,
	    ## and are not otherwise known to be associated with Nagios.
	    next if $fn_host->{ApplicationType} ne 'NAGIOS' and not exists $fn_host_services->{$host} and not defined $el_host;
	    my $cs_host = \%{ $cs_hosts->{$host} };

	    # Note that just because we qualified a host in Foundation that seems to be associated
	    # with Nagios in some way, we cannot necessarily expect it to also be still in Nagios.
	    # So we need to test every use of $el_host before we dereference it.

	    if ($debug_debug) {
		log_message( Data::Dumper->Dump( [$fn_host], [qw($fn_host)] ) );
		if ( defined $el_host ) {
		    log_message "Nagios last check time for $host: $el_host->{LastCheckTime}";
		    log_message "Nagios next check time for $host: $el_host->{NextCheckTime}";
		}
	    }

	    # Look for hosts that have never been checked -- don't bother sending results if so.
	    if (   defined($el_host)
		&& is_null_internal_time( $el_host->{LastCheckTime} )
		&& is_null_internal_time( $fn_host->{LastCheckTime} ) )
	    {
		$cs_host->{LastCheckTime} = null_internal_time();    # This will show up as no change of state.
	    }
	    else {
		$cs_host->{LastCheckTime} = $fn_host->{LastCheckTime};    # Might be a change, might not.
	    }

	    # FIX MINOR:  Logically, we ought to use the gwcollagedb.hoststatus.lastchecktime value above, and
	    # convert it in some unambiguous manner to a UTC epoch value for use as $cs_host->{last_check}, so the
	    # two values (LastCheckTime and last_check) match up.  That way, the timestamps would be known to reflect
	    # the exact time of the $cs_host->{MonitorStatus} value we set below.  The key word there is "unambiguous",
	    # particularly as it relates to times around Daylight Savings Time transitions in some arbitrary timezone.
	    # In the meantime, until we see otherwise, this construction should provide a reasonable initial value.
	    #
	    $cs_host->{last_check} = defined($el_host) ? $el_host->{last_check} : 0 if $send_actual_check_timestamps;

	    # Do the same for NextCheckTime in case it was never fed (like for passive checks)
	    if (   defined($el_host)
		&& is_null_internal_time( $el_host->{NextCheckTime} )
		&& is_null_internal_time( $fn_host->{NextCheckTime} ) )
	    {
		$cs_host->{NextCheckTime} = null_internal_time();         # This will show up as no change of state.
	    }
	    else {
		$cs_host->{NextCheckTime} = $fn_host->{NextCheckTime};    # Might be a change, might not.
	    }

	    # Do the same for LastNotificationTime
	    if (   defined($el_host)
		&& is_null_internal_time( $el_host->{LastNotificationTime} )
		&& is_null_internal_time( $fn_host->{LastNotificationTime} ) )
	    {
		$cs_host->{LastNotificationTime} = null_internal_time();    # This will show up as no change of state.
	    }
	    else {
		$cs_host->{LastNotificationTime} = $fn_host->{LastNotificationTime};    # Might be a change, might not.
	    }
	    $cs_host->{Comments}                  = $fn_host->{Comments};
	    $cs_host->{CurrentAttempt}            = $fn_host->{CurrentAttempt};
	    $cs_host->{CurrentNotificationNumber} = $fn_host->{CurrentNotificationNumber};
	    $cs_host->{ExecutionTime}             = $fn_host->{ExecutionTime};
	    $cs_host->{Latency}                   = $fn_host->{Latency};
	    $cs_host->{MaxAttempts}               = $fn_host->{MaxAttempts};
	    $cs_host->{MonitorStatus}             = $fn_host->{MonitorStatus} // 'PENDING';
	    $cs_host->{ScheduledDowntimeDepth}    = $fn_host->{ScheduledDowntimeDepth};
	    $cs_host->{StateType}                 = $fn_host->{StateType};
	    $cs_host->{isAcknowledged}            = $fn_host->{isAcknowledged};
	    $cs_host->{isChecksEnabled}           = $fn_host->{isChecksEnabled};
	    $cs_host->{isEventHandlersEnabled}    = $fn_host->{isEventHandlersEnabled};
	    $cs_host->{isFlapDetectionEnabled}    = $fn_host->{isFlapDetectionEnabled};
	    $cs_host->{isNotificationsEnabled}    = $fn_host->{isNotificationsEnabled};
	    $cs_host->{isPassiveChecksEnabled}    = $fn_host->{isPassiveChecksEnabled};
	    $cs_host->{LastPluginOutput}          = $fn_host->{LastPluginOutput};
	    $cs_host->{PercentStateChange}        = $fn_host->{PercentStateChange};
	    $cs_host->{LastStateChange}           = $fn_host->{LastStateChange};
	    $cs_host->{PerformanceData}           = $fn_host->{PerformanceData};
	    $cs_host->{CheckType}                 = $fn_host->{CheckType};

	    # Look for fancy MonitorStatus values and translate to the simple ones Nagios knows.
	    if ( $fn_host->{MonitorStatus} =~ /DOWN/ ) {
		$cs_host->{MonitorStatus} = 'DOWN';
	    }

	    if ( !defined $cs_host->{Comments} ) {
		$cs_host->{Comments} = ' ';
	    }

	    # Also capture any optional fields the user has configured.
	    @$cs_host{@non_default_host_data_change}   = @$fn_host{@non_default_host_data_change};
	    @$cs_host{@non_default_host_timing_change} = @$fn_host{@non_default_host_timing_change};

	    # Following are some possible special adjustments for optional non-default fields.
	    # Before we enable any of these, we would want to find out why anyone would want
	    # to use these optional fields, to see whether these adjustments are appropriate.

	    # The isObsessOverHost flag is perhaps problematic.  The obsess_over_host flag can be set in Nagios
	    # for an individual host, but such settings can be globally overridden by the obsess_over_hosts flag
	    # at the Nagios level.  So we need to override the host setting with the global if it's off ...
#	    if ( defined( $cs_host->{isObsessOverHost} ) and $global_nagios->{obsess_over_hosts} == 0 ) {
#		$cs_host->{isObsessOverHost} = 0;
#	    }

	    # FIX LATER:  We should probably uncomment this, and have similar logic later on.
#	    if ( defined( $cs_svc->{isProcessPerformanceData} ) and $global_nagios->{process_performance_data} == 0 ) {
#		$cs_svc->{isProcessPerformanceData} = 0;
#	    }

	    # FIX LATER:  We should probably uncomment this, and have similar logic later on.
#	    if ( defined( $cs_svc->{isHostFlapping} ) and $global_nagios->{enable_flap_detection} == 0 ) {
#		$cs_svc->{isHostFlapping} = 0;
#	    }

	    if ( ref($fn_host_services) eq 'HASH' ) {
		foreach my $service ( keys %{ $fn_host_services->{$host} } ) {
		    my $fn_svc = $fn_host_services->{$host}->{$service};
		    my $cs_svc  = \%{ $cs_host->{Service}->{$service} };
		    my $el_svc = ( defined($el_host) && defined( $el_host->{Service} ) ) ? $el_host->{Service}->{$service} : undef;
		    if ($debug_debug) {
			log_message( Data::Dumper->Dump( [$fn_svc], [qw($fn_svc)] ) );
		    }

		    # These variables must be vestiges of old code; they're not referenced anywhere.
		    # my $f_state = $fn_svc->{MonitorStatus};
		    # my $n_state = defined($el_svc) ? $el_svc->{MonitorStatus} : undef;

		    # $fn_svc->{LastCheckTime}; This does not exist -- must use the Check Time from the current status log ...
		    # FIX MINOR:  The gwcollagedb.servicestatus.lastchecktime column should contain such data; can't we get it from there?
		    $cs_svc->{LastCheckTime} = defined($el_svc) ? $el_svc->{LastCheckTime} : undef;

		    # FIX MINOR:  As long as we're using $el_svc->{LastCheckTime} for the value of $cs_svc->{LastCheckTime}
		    # instead of drawing it from the gwcollagedb.servicestatus.lastchecktime column, it makes sense to also
		    # use $el_svc->{last_check} as the value of $cs_svc->{last_check} so the two values (LastCheckTime and
		    # last_check) match up.  But logically, we ought to use the gwcollagedb.servicestatus.lastchecktime value and
		    # convert it in some unambiguous manner to a UTC epoch value, so the timestamps would be known to reflect
		    # the exact time of the $cs_svc->{MonitorStatus} value we set below.  The key word there is "unambiguous",
		    # particularly as it relates to times around Daylight Savings Time transitions in some arbitrary timezone.
		    # In the meantime, until we see otherwise, this construction should provide a reasonable initial value.
		    #
		    $cs_svc->{last_check} = defined($el_svc) ? $el_svc->{last_check} : 0 if $send_actual_check_timestamps;

		    # $fn_svc->{LastNotificationTime}; This might not be defined, so if 0 in nagios, don't generate a difference.
		    if (   defined($el_svc)
			&& is_null_internal_time( $el_svc->{LastNotificationTime} )
			&& is_null_internal_time( $fn_svc->{LastNotificationTime} ) )
		    {
			$cs_svc->{LastNotificationTime} = null_internal_time();    # This will show up as no change of state.
		    }
		    else {
			$cs_svc->{LastNotificationTime} = $fn_svc->{LastNotificationTime};    # Might be a change, might not.
		    }

# FIX LATER:  not sure why Comments is historically commented out
#		    $cs_svc->{Comments}                  = $fn_svc->{Comments};
		    $cs_svc->{CurrentAttempt}            = $fn_svc->{CurrentAttempt};
		    $cs_svc->{CurrentNotificationNumber} = $fn_svc->{CurrentNotificationNumber};
		    $cs_svc->{LastHardState}             = $fn_svc->{LastHardState};
		    $cs_svc->{MonitorStatus}             = $fn_svc->{MonitorStatus} // 'PENDING';
		    $cs_svc->{NextCheckTime}             = $fn_svc->{NextCheckTime};
		    $cs_svc->{ScheduledDowntimeDepth}    = $fn_svc->{ScheduledDowntimeDepth};
		    $cs_svc->{isAcceptPassiveChecks}     = $fn_svc->{isAcceptPassiveChecks};
		    $cs_svc->{isChecksEnabled}           = $fn_svc->{isChecksEnabled};
		    $cs_svc->{isEventHandlersEnabled}    = $fn_svc->{isEventHandlersEnabled};
		    $cs_svc->{isFlapDetectionEnabled}    = $fn_svc->{isFlapDetectionEnabled};
		    $cs_svc->{isNotificationsEnabled}    = $fn_svc->{isNotificationsEnabled};
		    $cs_svc->{isProblemAcknowledged}     = $fn_svc->{isProblemAcknowledged};
		    $cs_svc->{MaxAttempts}               = $fn_svc->{MaxAttempts};
		    $cs_svc->{LastPluginOutput}          = $fn_svc->{LastPluginOutput};
		    $cs_svc->{PercentStateChange}        = $fn_svc->{PercentStateChange};
		    $cs_svc->{Latency}                   = $fn_svc->{Latency};
		    $cs_svc->{ExecutionTime}             = $fn_svc->{ExecutionTime};
		    $cs_svc->{LastStateChange}           = $fn_svc->{LastStateChange};
		    $cs_svc->{PerformanceData}           = $fn_svc->{PerformanceData};
		    $cs_svc->{StateType}                 = $fn_svc->{StateType};
		    $cs_svc->{CheckType}                 = $fn_svc->{CheckType};

		    # Look for fancy MonitorStatus values and translate to the simple ones Nagios knows
		    if ( $fn_svc->{MonitorStatus} =~ /CRITICAL/ ) {
			$cs_svc->{MonitorStatus} = 'CRITICAL';
		    }
		    elsif ( $fn_svc->{MonitorStatus} =~ /WARNING/ ) {
			$cs_svc->{MonitorStatus} = 'WARNING';
		    }

		    if ( !defined $cs_svc->{Comments} ) {
			$cs_svc->{Comments} = ' ';
		    }

		    # Also capture any optional fields the user has configured.
		    @$cs_svc{@non_default_service_data_change}   = @$fn_svc{@non_default_service_data_change};
		    @$cs_svc{@non_default_service_timing_change} = @$fn_svc{@non_default_service_timing_change};

		    # Following are some possible special adjustments for optional non-default fields.
		    # Before we enable any of these, we would want to find out why anyone would want
		    # to use these optional fields, to see whether these adjustments are appropriate.

		    # The isObsessOverService flag is perhaps problematic.  The obsess_over_service flag can be set in Nagios
		    # for an individual service, but such settings can be globally overridden by the obsess_over_services flag
		    # at the Nagios level.  So we need to override the service setting with the global if it's off ...
#		    if ( defined( $cs_svc->{isObsessOverService} ) and $global_nagios->{obsess_over_services} == 0 ) {
#			$cs_svc->{isObsessOverService} = 0;
#		    }

		    # FIX LATER:  We should probably uncomment this, and have similar logic later on.
#		    if ( defined( $cs_svc->{isProcessPerformanceData} ) and $global_nagios->{process_performance_data} == 0 ) {
#			$cs_svc->{isProcessPerformanceData} = 0;
#		    }

		    # FIX LATER:  We should probably uncomment this, and have similar logic later on.
#		    if ( defined( $cs_svc->{isServiceFlapping} ) and $global_nagios->{enable_flap_detection} == 0 ) {
#			$cs_svc->{isServiceFlapping} = 0;
#		    }
		}
	    }
	}
    }
    return $collage_status_ref;
}

# We want the REST-API equivalent of:
#   my $fn_hosts = $foundation->getHostsByType(undef);
#   return $fn_hosts;
# with undef returned if the processing fails.
sub get_foundation_hosts {
    my $apptype = shift;
    my %fn_hosts = ();

    # FIX MAJOR:  Distinguish between <host> and <hosts> at the top level of returned results, and
    # handle appropriately.  (Actually, that should be completely handled within the GW::RAPID package.)
    # (Actually, it shouldn't even be an issue in the first place; it's nonsensical for the REST API
    # to possibly return two different types of results.  It should only return <hosts>, period, even
    # if there is only one <host> contained within it.)
    my %outcome = ();
    my %results = ();
    my %get_hosts_opts = ( depth => 'shallow' );
    $get_hosts_opts{query} = "appType='$apptype'" if defined $apptype;
    if ( not $rest_api->get_hosts( [], \%get_hosts_opts, \%outcome, \%results ) ) {
	log_timed_message 'ERROR:  Could not find hosts in Foundation (' . code_coordinates() . ').';
	log_outcome \%outcome, 'fetching of Foundation host data';
	log_results \%results, 'fetching of Foundation host data';
	return undef;
    }

    foreach my $host (keys %results) {
	## This constant checking to see whether a shutdown request has come in is necessitated
	## because of the extreme inefficiency of converting timestamps.  If we were able to use
	## a simple UNIX epoch timestamp instead of a human-readable string for data exchange, we
	## could use the numeric epoch timestamp for our internal representation as well, and this
	## loop would execute so fast even with a large number of hosts that we wouldn't bother to
	## check periodically within the loop to see if a shutdown had been requested.
	if ($shutdown_requested) {
	    log_shutdown();
	    return undef;
	}
	my $rest_host = $results{$host};
	my %fn_host = ();
	foreach my $key (keys %$rest_host) {
	    ## FIX MAJOR:  Check to ensure that all property names returned from the REST API have EXACTLY the same
	    ## spelling and capitalization as was provided in the earlier CollageQuery call, or convert them here.

#	Old code expected field names like:
#
#	$fn_host->{ApplicationType}
#	$fn_host->{Comments}
#	$fn_host->{CurrentAttempt}
#	$fn_host->{CurrentNotificationNumber}
#	$fn_host->{ExecutionTime}
#	$fn_host->{LastCheckTime}
#	$fn_host->{LastNotificationTime}
#	$fn_host->{LastPluginOutput}
#	$fn_host->{LastStateChange}
#	$fn_host->{Latency}
#	$fn_host->{MaxAttempts}
#	$fn_host->{MonitorStatus}
#	$fn_host->{NextCheckTime}
#	$fn_host->{PercentStateChange}
#	$fn_host->{PerformanceData}
#	$fn_host->{ScheduledDowntimeDepth}
#	$fn_host->{StateType}
#	$fn_host->{isAcknowledged}
#	$fn_host->{isChecksEnabled}
#	$fn_host->{isEventHandlersEnabled}
#	$fn_host->{isFlapDetectionEnabled}
#	$fn_host->{isHostFlapping}
#	$fn_host->{isNotificationsEnabled}
#	$fn_host->{isObsessOverHost}
#	$fn_host->{isPassiveChecksEnabled}
#
#	But now I'm getting this instead from the REST API:
#
#	$fn_host->{acknowledged}
#	$fn_host->{appType}                                     <= different spelling (used to be ApplicationType)
#	$fn_host->{bubbleUpStatus}
#	$fn_host->{checkType}
#	$fn_host->{description}
#	$fn_host->{deviceIdentification}
#	$fn_host->{hostName}                                    <= different spelling (used to be HostName)
#	$fn_host->{id}
#	$fn_host->{lastCheckTime}                               <= different spelling (used to be LastCheckTime)
#	$fn_host->{monitorStatus}                               <= different spelling (used to be MonitorStatus)
#	$fn_host->{nextCheckTime}                               <= different spelling (used to be NextCheckTime)
#	$fn_host->{properties}{CactiRRDCommand}
#	$fn_host->{properties}{Comments}
#	$fn_host->{properties}{CurrentAttempt}
#	$fn_host->{properties}{CurrentNotificationNumber}
#	$fn_host->{properties}{ExecutionTime}
#	$fn_host->{properties}{LastNotificationTime}
#	$fn_host->{properties}{LastPluginOutput}
#	$fn_host->{properties}{LastStateChange}
#	$fn_host->{properties}{Latency}
#	$fn_host->{properties}{MaxAttempts}
#	$fn_host->{properties}{PercentStateChange}
#	$fn_host->{properties}{PerformanceData}
#	$fn_host->{properties}{ScheduledDowntimeDepth}
#	$fn_host->{properties}{isAcknowledged}
#	$fn_host->{properties}{isChecksEnabled}
#	$fn_host->{properties}{isEventHandlersEnabled}
#	$fn_host->{properties}{isFlapDetectionEnabled}
#	$fn_host->{properties}{isNotificationsEnabled}
#	$fn_host->{properties}{isPassiveChecksEnabled}
#	$fn_host->{serviceAvailability}
#	$fn_host->{serviceCount}
#	$fn_host->{stateType}                                   <= different spelling (used to be StateType)

	    # Here is some sample data retrieved from the REST API.
	    # Notice that many fields we might want to see here are missing, so far.
	    # Also note the datatype of each provided field.
	    # [Mon Mar 31 19:19:22 2014]     localhost{lastCheckTime} => '2014-03-31T12:50:09.000-0700'
	    # [Mon Mar 31 19:19:22 2014]     localhost{acknowledged} => 'false'
	    # [Mon Mar 31 19:19:22 2014]     localhost{deviceIdentification} => '127.0.0.1'
	    # [Mon Mar 31 19:19:22 2014]     localhost{nextCheckTime} => '2014-03-31T12:50:09.000-0700'
	    # [Mon Mar 31 19:19:22 2014]     localhost{monitorStatus} => 'UP'
	    # [Mon Mar 31 19:19:22 2014]     localhost{description} => 'Host localhost'
	    # [Mon Mar 31 19:19:22 2014]     localhost{stateType} => 'HARD'
	    # [Mon Mar 31 19:19:22 2014]     localhost{serviceAvailability} => '0'
	    # [Mon Mar 31 19:19:22 2014]     localhost{properties} => 'HASH(0x27ab9e0)'
	    # [Mon Mar 31 19:19:22 2014]     localhost{hostName} => 'localhost'
	    # [Mon Mar 31 19:19:22 2014]     localhost{appType} => 'NAGIOS'
	    # [Mon Mar 31 19:19:22 2014]     localhost{bubbleUpStatus} => 'UP'
	    # [Mon Mar 31 19:19:22 2014]     localhost{checkType} => 'ACTIVE'
	    # [Mon Mar 31 19:19:22 2014]     localhost{serviceCount} => '21'
	    # [Mon Mar 31 19:19:22 2014]     localhost{id} => '1'

	    # Ensure that for timestamp-related values, we capture data here in whatever form must
	    # be used to compare with Nagios data.  Also ensure that the same data format will be
	    # accepted when returned to Foundation in status updates.
	    my $inside_attr;
	    if ( $inside_attr = $host_inside_attr{$key} ) {
		$fn_host{$inside_attr} = $rest_time_field{$key} ? rest_to_internal_time( $rest_host->{$key} ) : $rest_host->{$key};
	    }
	    elsif ( $key eq 'properties' ) {
		## Decode whatever "properties" sub-structure got returned from Foundation,
		## into the individual property names and values.
		my $properties = $rest_host->{$key};
		foreach my $subkey ( keys %$properties ) {
		    $fn_host{$subkey} =
			$rest_time_field{$subkey} ? rest_to_internal_time( $properties->{$subkey} )
		      : $boolean_field{$subkey}   ? internal_boolean( $properties->{$subkey} )
		      : $float_field{$subkey}     ? internal_float( $properties->{$subkey} )
		      :                             $properties->{$subkey};
		}
	    }
	    else {
		$fn_host{$key} = $rest_time_field{$key} ? rest_to_internal_time( $rest_host->{$key} ) : $rest_host->{$key};
	    }
	}
	$fn_hosts{$host} = \%fn_host if %fn_host;
    }

    return \%fn_hosts;
}

# We want the REST-API equivalent of:
#   my $fn_host_services = $foundation->getHostServices();
#   return $fn_host_services;
# with undef returned if the processing fails.
sub get_foundation_host_services {
    my $apptype = shift;

    # FIX MAJOR:  Distinguish between <service> and <services> at the top level of returned results, and
    # handle appropriately.  (Actually, that should be completely handled within the GW::RAPID package.)
    # (Actually, it shouldn't even be an issue in the first place; it's nonsensical for the REST API
    # to possibly return two different types of results.  It should only return <services>, period, even
    # if there is only one <service> contained within it.)
    my %outcome = ();
    my %results = ();

    my %get_host_services_opts = ( format => 'host,service' );
    $get_host_services_opts{query} = "appType='$apptype'" if defined $apptype;
    if ( not $rest_api->get_services( [], \%get_host_services_opts, \%outcome, \%results ) ) {
	log_timed_message 'ERROR:  Could not find host services in Foundation (' . code_coordinates() . ').';
	log_outcome \%outcome, 'fetching of Foundation host service data';
	log_results \%results, 'fetching of Foundation host service data';
	return undef;
    }

    my %fn_host_services = ();
    my $attribute;
    my $inside_attr;
    foreach my $host ( keys %results ) {
	## This constant checking to see whether a shutdown request has come in is necessitated
	## because of the extreme inefficiency of converting timestamps.  If we were able to use
	## a simple UNIX epoch timestamp instead of a human-readable string for data exchange, we
	## could use the numeric epoch timestamp for our internal representation as well, and this
	## loop would execute so fast even with a large number of hosts that we wouldn't bother to
	## check periodically within the loop to see if a shutdown had been requested.
	if ($shutdown_requested) {
	    log_shutdown();
	    return undef;
	}
	my $rest_host = $results{$host};
	foreach my $service ( keys %$rest_host ) {
	    my $rest_host_service = $rest_host->{$service};
	    my %attributes        = ();
	    foreach my $key ( keys %$rest_host_service ) {
		$attribute = $rest_host_service->{$key};
		## FIX MAJOR:  Check to ensure that all property names returned from the REST API have EXACTLY the same
		## spelling and capitalization as was provided in the earlier CollageQuery call, or convert them here.
		## log_message "assigning $host $service key is $key; serv_inside_attr{$key} = " . ($serv_inside_attr{$key} || 'undefined');

		# Ensure that for timestamp-related values, we capture data here in whatever form must
		# be used to compare with Nagios data.  Also ensure that the same data format will be
		# accepted when returned to Foundation in status updates.
		if ( $inside_attr = $serv_inside_attr{$key} ) {
		    ## log_message "assigning mapped service attribute $host $service $inside_attr";
		    $attributes{$inside_attr} = $rest_time_field{$key} ? rest_to_internal_time($attribute) : $attribute;
		}
		elsif ( $key eq 'properties' ) {
		    ## Decode whatever "properties" sub-structure got returned from Foundation,
		    ## into the individual property names and values.
		    foreach my $subkey ( keys %$attribute ) {
			## log_message "assigning service property $host $service $subkey";
			$attributes{$subkey} =
			    $rest_time_field{$subkey} ? rest_to_internal_time( $attribute->{$subkey} )
			  : $boolean_field{$subkey}   ? internal_boolean( $attribute->{$subkey} )
			  : $float_field{$subkey}     ? internal_float( $attribute->{$subkey} )
			  :                             $attribute->{$subkey};
		    }
		}
		else {
		    ## log_message "assigning service attribute $host $service $key";
		    $attributes{$key} = $rest_time_field{$key} ? rest_to_internal_time($attribute) : $attribute;
		}
	    }
	    $fn_host_services{$host}{$service} = \%attributes if %attributes;
	}
    }

#	Comments
#	CurrentAttempt
#	CurrentNotificationNumber
#	ExecutionTime
#	LastNotificationTime
#	LastPluginOutput
#	Latency
#	MaxAttempts
#	PercentStateChange
#	PerformanceData
#	RRDCommand
#	RRDLabel
#	RRDPath
#	RemoteRRDCommand
#	ScheduledDowntimeDepth
#	appType
#	checkType
#	description
#	hostName
#	id
#	isAcceptPassiveChecks
#	isChecksEnabled
#	isEventHandlersEnabled
#	isFlapDetectionEnabled
#	isNotificationsEnabled
#	isProblemAcknowledged
#	lastCheckTime
#	lastHardState
#	lastPlugInOutput  (a constructed attribute, not the same as the property-level LastPluginOutput field)
#	lastStateChange
#	monitorServer
#	monitorStatus
#	nextCheckTime
#	stateType

    # For development debugging only.
    if (0) {
	foreach my $host ( sort keys %fn_host_services ) {
	    foreach my $service ( sort keys %{ $fn_host_services{$host} } ) {
		foreach my $attribute ( sort keys %{ $fn_host_services{$host}{$service} } ) {
		    log_message("    fn_host_services{$host}{$service}{$attribute} => '$fn_host_services{$host}{$service}{$attribute}'");
		}
	    }
	}
    }

    return \%fn_host_services;
}

# We want the REST-API equivalent of:
#   my $fn_host_types = $foundation->getHostTypes(undef);
#   return $fn_host_types;
# with undef returned if the processing fails.
sub get_foundation_host_types {
    my $apptype = shift;

    # FIX MAJOR:  Distinguish between <host> and <hosts> at the top level of returned results, and
    # handle appropriately.  (Actually, that should be completely handled within the GW::RAPID package.)
    # (Actually, it shouldn't even be an issue in the first place; it's nonsensical for the REST API
    # to possibly return two different types of results.  It should only return <hosts>, period, even
    # if there is only one <host> contained within it.)
    my %outcome = ();
    my %results = ();
    my %get_hosts_opts = ( depth => 'simple' );
    $get_hosts_opts{query} = "appType='$apptype'" if defined $apptype;
    if ( not $rest_api->get_hosts( [], \%get_hosts_opts, \%outcome, \%results ) ) {
	log_outcome \%outcome, 'fetching of Foundation host application types';
	return undef;
    }
    my %fn_host_types = ();
    if (%results) {
	foreach my $host ( keys %results ) {
	    $fn_host_types{$host} = $results{$host}{appType};
	    if ($debug_trace) {
		log_timed_message "TRACE:  Found host $host in Foundation while looking for app types.";
		foreach my $key ( sort keys %{ $results{$host} } ) {
		    if ( ref $results{$host}{$key} eq 'HASH' ) {
			if ( %{ $results{$host}{$key} } ) {
			    foreach my $subkey ( sort keys %{ $results{$host}{$key} } ) {
				log_timed_message "TRACE:  results{$host}{$key}{$subkey} = $results{$host}{$key}{$subkey}";
			    }
			}
			else {
			    log_timed_message "TRACE:  results{$host}{$key} = {}";
			}
		    }
		    else {
			log_timed_message "TRACE:  results{$host}{$key} = $results{$host}{$key}";
		    }
		}
	    }
	}
    }
    else {
	log_timed_message "WARNING:  Found no hosts in Foundation while looking for app types.";
    }
    return \%fn_host_types;
}

# We want the REST-API equivalent of:
#   my $fn_host_service_types = $foundation->getHostServiceTypes('NAGIOS');
#   return $fn_host_service_types;
# with undef returned if the processing fails.
sub get_foundation_host_service_types {
    my $apptype = shift;

    # FIX MAJOR:  Distinguish between <service> and <services> at the top level of returned results, and
    # handle appropriately.  (Actually, that should be completely handled within the GW::RAPID package.)
    # (Actually, it shouldn't even be an issue in the first place; it's nonsensical for the REST API
    # to possibly return two different types of results.  It should only return <services>, period, even
    # if there is only one <service> contained within it.)
    my %outcome = ();
    my %results = ();
    my %get_host_services_opts = ( format => 'host,service' );
    $get_host_services_opts{query} = "appType='$apptype'" if defined $apptype;
    if ( not $rest_api->get_services( [], \%get_host_services_opts, \%outcome, \%results ) ) {
	log_outcome \%outcome, 'fetching of Foundation host-service application types';
	return undef;
    }
    my %fn_host_service_types = ();
    foreach my $host (keys %results) {
	my $rest_host = $results{$host};
	foreach my $service (keys %$rest_host) {
	    $fn_host_service_types{$host}{$service} = $rest_host->{$service}{appType};
	}
    }
    return \%fn_host_service_types;
}

# Find each host and service in Foundation that has some association with Nagios, and compare to what Nagios knows.
sub FoundationIsSynced {
    my $element_ref = shift;
    local $_;

    # FIX MAJOR:  The statements in the next paragraph are not all true.  Test how DBD::Pg reacts to an
    # interrupt signal while executing a long-running query, and figure out how best to handle a shutdown
    # signal here, given both the code in this routine and the expected behavior in its calling context.
    #
    # In this routine, we set up to die instantly if certain database calls are interrupted by a signal.
    # The $foundation->getHostServiceTypes() call in particular might take a considerable amount of time,
    # but some of its internal database-access components (DBD::mysql, certainly; DBD::Pg, possibly) are
    # effectively not interruptible by signals (the EINTR return code from some internal system call is
    # recognized and the interrupted system call is restarted, instead of having some means to check a
    # cancel-is-requested flag and stop the request).  This script is instrumented to effectively return as
    # quickly as signals are recognized by Perl, but that might be far too long for outside applications
    # to wait for the death of this script once it has been signaled to terminate, especially on a very
    # busy system (typically, one where the available disk i/o is saturated).  Fortunately, we know by code
    # inspection that there are no resources that need flushing or cleaning up before we exit here.

    my $foundation;
    if ($use_rest_api) {
	## FIX MAJOR:  fill in equivalent REST code here, if any is required (doubtful)
    }
    else {
	eval {
	    ## local $SIG{INT}  = \&die_upon_exit_signal;
	    ## local $SIG{QUIT} = \&die_upon_exit_signal;
	    ## local $SIG{TERM} = \&die_upon_exit_signal;
	    local $SIG{INT}  = 'DEFAULT';
	    local $SIG{QUIT} = 'DEFAULT';
	    local $SIG{TERM} = 'DEFAULT';
	    $foundation = CollageQuery->new();
	};
	if ($@) {
	    chomp $@;
	    log_message $@;
	    return undef;
	}
    }
    if ($shutdown_requested) {
	return undef;
    }
    log_timed_message 'Getting host types ...';
    my $fn_host_types = undef;
    if ($use_rest_api) {
	$fn_host_types = get_foundation_host_types(undef);
	if ( not defined $fn_host_types ) {
	    log_timed_message "ERROR:  Cannot find host types in Foundation.";
	    return undef;
	}
    }
    else {
	eval {
	    ## local $SIG{INT}  = \&die_upon_exit_signal;
	    ## local $SIG{QUIT} = \&die_upon_exit_signal;
	    ## local $SIG{TERM} = \&die_upon_exit_signal;
	    local $SIG{INT}  = 'DEFAULT';
	    local $SIG{QUIT} = 'DEFAULT';
	    local $SIG{TERM} = 'DEFAULT';
	    ## With the advent of CloudHub, we cannot call getHostTypes('NAGIOS') and just get the NAGIOS-related hosts,
	    ## because CloudHub might own some hosts (as VEMA or CHRHEV application types, for instance) that we need
	    ## to deal with here.  So we must inefficiently drag back information on all hosts known to Foundation,
	    ## and filter them below to ignore the ones we don't really care about.
	    $fn_host_types = $foundation->getHostTypes(undef);
	};
	if ($@) {
	    chomp $@;
	    log_timed_message "Error in getHostTypes: $@";
	    return undef;
	}
    }
    if ($shutdown_requested) {
	return undef;
    }
    if ( not defined $fn_host_types ) {
	log_timed_message "ERROR:  getHostTypes failed to retrieve data from Foundation.";
	return undef;
    }
    log_timed_message 'Getting host service types ...';
    my $fn_host_service_types = undef;
    if ($use_rest_api) {
	$fn_host_service_types = get_foundation_host_service_types('NAGIOS');
	if ( not defined $fn_host_service_types ) {
	    log_timed_message "ERROR:  Cannot find host service types in Foundation.";
	    return undef;
	}
    }
    else {
	eval {
	    ## local $SIG{INT}  = \&die_upon_exit_signal;
	    ## local $SIG{QUIT} = \&die_upon_exit_signal;
	    ## local $SIG{TERM} = \&die_upon_exit_signal;
	    local $SIG{INT}  = 'DEFAULT';
	    local $SIG{QUIT} = 'DEFAULT';
	    local $SIG{TERM} = 'DEFAULT';
	    $fn_host_service_types = $foundation->getHostServiceTypes('NAGIOS');
	};
	if ($@) {
	    chomp $@;
	    log_timed_message "Error in getHostServiceTypes: $@";
	    return undef;
	}
    }
    if ($shutdown_requested) {
	return undef;
    }
    if ( not defined $fn_host_service_types ) {
	log_timed_message "ERROR:  getHostServiceTypes failed to retrieve data from Foundation.";
	return undef;
    }

    # Compare what's in Nagios with what's in Foundation.  Look for structural shape equivalence
    # (the same hosts, and the same host services, in both places).  If we find any mismatch,
    # short-circuit the comparison and return a result immediately.
    #
    # What we have from Foundation now is:
    #
    #         $fn_host_types->{"Host_A"}                = "application_type";  # app type might not be "NAGIOS"
    # $fn_host_service_types->{"Host_A"}->{"Service_2"} = "application_type";  # app type is always "NAGIOS"

    log_timed_message 'Comparing Nagios and Foundation data ...';
    my $el_hosts = \%{ $element_ref->{Host} };
    my $el_host;
    my $el_svcs;
    my $fn_svcs;
    my $f_hostcount = 0;
    foreach my $host ( keys %$fn_host_types ) {
	$fn_svcs = $fn_host_service_types->{$host};
	## Check hosts that are not known to be associated with Nagios.
	if ( not defined( $el_host = $el_hosts->{$host} ) ) {
	    ## If Nagios doesn't think this is a Nagios host, but Foundation thinks so, we have a mismatch.
	    if ( $fn_host_types->{$host} eq 'NAGIOS' ) {
		log_message "... mismatch found:  host \"$host\" is in Foundation (owned by NAGIOS) but not in Nagios";
		return 0;
	    }
	    elsif ( defined $fn_svcs ) {
		log_message "... mismatch found:  host \"$host\" is in Foundation (not owned by NAGIOS, but with NAGIOS-owned services) but not in Nagios";
		return 0;
	    }
	    ## Ignore hosts that are not directly owned by NAGIOS, that have no NAGIOS-related services,
	    ## and that Nagios itself doesn't know about.
	    next;
	}
	++$f_hostcount;
	if ( defined( $el_svcs = $el_host->{Service} ) ) {
	    if ( not defined $fn_svcs ) {
		log_message "... mismatch found:  host \"$host\" has services in Nagios but not in Foundation.";
		log_message "... $host service count in Nagios:  ", scalar keys %$el_svcs;
		log_message "... $host services in Nagios: ", join(', ', keys %$el_svcs);
		return 0;
	    }
	    ## FIX MAJOR:  The appropriateness of this test is suspect.  Does every service in Foundation that is
	    ## monitored by Nagios have to be owned by the NAGIOS application type in Foundation?  That condition
	    ## certainly does not hold at the host level.  Why would it need to hold at the service level?
	    if ( keys %$fn_svcs != keys %$el_svcs ) {
		log_message "... mismatch found:  host \"$host\" has different services in Nagios and in Foundation (owned by NAGIOS).";
		log_message "... $host service count in Nagios:  ", scalar keys %$el_svcs;
		log_message "... $host services in Nagios: ",     join( ', ', keys %$el_svcs );
		log_message "... $host service count in Foundation:  ", scalar keys %$fn_svcs;
		log_message "... $host services in Foundation: ", join( ', ', keys %$fn_svcs );
		return 0;
	    }
	    ## Since we already checked found identical cardinality of the service sets (but see
	    ## the potential caveat above on the appropriateness of that test), we need only confirm
	    ## inclusion in one direction.  We won't log any NAGIOS services in Foundation that are
	    ## not in Nagios, but we'll catch at least one Nagios service that is not a NAGIOS
	    ## service in Foundation, and that suffices for documenting the out-of-sync condition.
	    for ( keys %$el_svcs ) {
		if ( not exists $fn_svcs->{$_} ) {
		    log_message "... mismatch found:  host \"$host\" service \"$_\" is in Nagios but not in Foundation.";
		    log_message "... $host services in Nagios: ",     join( ', ', keys %$el_svcs );
		    log_message "... $host services in Foundation: ", join( ', ', keys %$fn_svcs );
		    return 0;
		}
	    }
	}
	else {
	    if ( defined $fn_svcs ) {
		log_message "... mismatch found:  host \"$host\" has NAGIOS services in Foundation but not in Nagios.";
		log_message "... $host service count in Foundation:  ", scalar keys %$fn_svcs;
		log_message "... $host services in Foundation: ", join(', ', keys %$fn_svcs);
		return 0;
	    }
	}
    }

    # Since we just ran that scan based on hosts known to Foundation, we possibly
    # missed comparing some hosts known to Nagios but not to Foundation.  This last
    # check covers that case, in toto.  We couldn't compare hash sizes at the host
    # level up front, because what we retrieved from Foundation at that level could
    # have included some non-Nagios hosts that we needed to filter out above.
    #
    # When the counts match, we must have found a Foundation host for every Nagios
    # host, and we have already compared services for each such host.  When the
    # counts do not match, we need to log at least one Nagios host that is not in
    # Foundation, or which was not matched up earlier, for diagnostic purposes.
    if ( $f_hostcount == $n_hostcount ) {
	return 1;
    }
    else {
	log_timed_message "Nagios has $n_hostcount hosts, while Foundation has $f_hostcount hosts.";
	foreach my $host ( keys %{$el_hosts} ) {
	    if ( not defined $fn_host_types->{$host} ) {
		log_message "... mismatch found:  host \"$host\" is in Nagios but not in Foundation";
		return 0;
	    }
	    ## Any host in both Nagios and Foundation will have had its services compared
	    ## in the earlier loop, so there is no point in repeating such tests now.
	}
	log_timed_message "ERROR:  Out of sync, but could not identify a specific difference between Nagios and Foundation host sets.";
	return 0;
    }
}

sub open_socket {
    my $socket = undef;
    my $failed = 1;

    # FIX LATER:  Here and for all the other sockets in this script, we want to implement a
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
	log_timed_message 'Output bundle local port: ', $socket->sockport() if $debug_summary;
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
	    log_timed_message 'Reported socket send buffer size: ', $send_buf;
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

    # FIX LATER:  This socket closing will invoke a write operation on any data still left hanging
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

# We implement this routine, using either REST API async requests (for status updates) or parallel
# virtual threads (perhaps, in some future version), so we don't have to wait a full round-trip time
# for each call before sending data on a subsequent call.  This is necessary to keep several threads
# within Foundation busy performing all the updates.
#
# Note that because of our desired parallelism, there is always the possibility for race conditions
# to arise in processing of results for a particular object.  That effect will be somewhat limited
# by the nature of sending all change-results for hosts and services in each cycle, so the only real
# danger is the downstream code mixing up change-results from different cycles.  If it were found
# to be a significant problem, we could switch to using virtual threads here, and binning the change
# results within this feeder using a fixed binning assignment algorithm, in such a manner that for
# any given host or host service, at most only one virtual thread here would ever be handling the
# bin that that host or host service at any given time.
sub output_bundle_to_rest_api {
    my $obj_type    = shift;
    my $obj_ref     = shift;
    my $series_num  = shift;
    my $sent_ref    = shift;
    my $sent_count  = 0;
    my $blocked     = 0;
    my $failed      = 0;
    my $in_shutdown = $shutdown_requested;

    # GWMON-12251
    # If suppression of ScheduledDowntimeDepth property is enabled, just remove that property from REST API data.
    if ($suppress_downtime_update) {
	foreach my $obj ( @{$obj_ref} ) {
	    delete $obj->{properties}->{ScheduledDowntimeDepth};
	}
    }

    my %outcome = ();
    my @results = ();

    my $routine = undef;
    my $async   = undef;
    if ( $obj_type eq 'host' ) {
	$routine = 'upsert_hosts';
	$async   = $async_upsert_hosts;
    }
    elsif ( $obj_type eq 'service' ) {
	$routine = 'upsert_services';
	$async   = $async_upsert_services;
    }
    else {
	log_timed_message "ERROR:  Unknown object type '$obj_type' passed to output_bundle_to_rest_api().";
	## $failed = 1;
	return -1;
    }

    log_timed_message 'Writing ' . ( scalar @$obj_ref ) . " $obj_type object(s) to Foundation." if $debug_debug;

    # Ensure that the caller can never get confused by any leftover blocked or failed state from previous calls,
    # no matter whether or not we actually send any data in this call.
    $series_num = 1 if $series_num < 1;

    my $next       = 0;
    my $last       = -1;
    my $last_index = $#$obj_ref;
    while ( $next <= $last_index ) {
	$last = $next + $max_rest_bundle_size - 1;
	$last = $last_index if $last > $last_index;
	$series_num++;

	# FIX MINOR:  Either of these formulations works.  The question is, which is
	# more efficient?  If possible, we'd like to reference elements in the array
	# slice directly, without copying.  Also look at Data::Alias to see if that
	# might improve performance.
	#
	#   if (
	#       not $rest_api->$routine(
	#           [ @{$obj_ref}[ $next .. $last ] ],
	#           { async => 'true' },
	#           \%outcome, \@results
	#       )
	#     )
	#   {
	#       ## ...
	#   }
	#
	# FIX MAJOR:  Verify that the construction below yields a reference to the
	# desired array slice, and that there is not some simpler way to create such
	# a reference.
	@results = ();
	if (
	    $rest_api->$routine(
		sub { \@_ }
		  ->( @{$obj_ref}[ $next .. $last ] ), { async => $async }, \%outcome, \@results
	    )
	  )
	{
	    $sent_count = $last + 1;
	}
	else {
	    ## FIX LATER:  Check the @results thoroughly.  Maybe even retry any failed sends, one time.
	    log_timed_message "ERROR:  Could not upsert ${obj_type}s in Foundation (" . code_coordinates() . ').';
	    log_outcome \%outcome, "outputting $obj_type data bundle to REST API";
	    log_results \@results, "outputting $obj_type data bundle to REST API";

	    # HTTP response code 429 is "Too Many Requests", the value the server returns if its queue is full.
	    $blocked = 1 if $outcome{response_code} == 429;
	    $failed = 1;
	    last;
	}

	$next = $last + 1;

	# This constant checking to see whether a shutdown request has come in is necessitated
	# because the server might be introducing artificial delays before responding, if it is
	# overloaded.  If that happens, we want to respond to a shutdown request with some alacrity,
	# not waiting for all remaining updates to be sent to the server.  On the other hand, if
	# we are in here because we wanted to flush pending updates on the way out after previously
	# recognizing a shutdown signal, then we want to continue flushing.  This logic might evolve
	# further in future releases.
	if ( not $in_shutdown and $shutdown_requested ) {
	    log_shutdown();
	    return -1;
	}
    }

    $$sent_ref = $sent_count if $sent_ref;
    return $blocked ? 0 : $failed ? -1 : $series_num;
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
		LOG->print ($elements, "\n") if $debug_xml && !$log_as_utf8;
		utf8::encode($elements);
		log_timed_message "Writing Adapter message (Session $series_num) to Foundation: ", length($elements), ' bytes.' if $debug_summary;
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
		LOG->print ($elements, "\n") if $debug_xml && $log_as_utf8;

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
		LOG->print ($elements, "\n") if $debug_xml && !$log_as_utf8;
		utf8::encode($elements);
		log_timed_message "Writing Adapter message (Session $series_num) to Foundation: ", length($elements), ' bytes.' if $debug_summary;
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
		LOG->print ($elements, "\n") if $debug_xml && $log_as_utf8;
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
		    LOG->print ($message, "\n") if $debug_xml && !$log_as_utf8;
		    utf8::encode($message);
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
		    LOG->print ($message, "\n") if $debug_xml && $log_as_utf8;
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
    my $failed          = 0;

    if ($use_rest_api) {
	## FIX MAJOR:  perhaps implement logging of created-event details, via log_results()?
	if ( scalar(@rest_event_messages) >= $max_bundle_size ) {
	    $failed = 1;
	    if ($shutdown_requested) {
		log_shutdown();
	    }
	    else {
		log_timed_message 'Writing ' . ( scalar @rest_event_messages ) . ' events message(s) to Foundation.' if $debug_debug;
		$failed = 0;
		my %outcome;

		my $next       = 0;
		my $last       = -1;
		my $last_index = $#rest_event_messages;
		while ( $next <= $last_index ) {
		    $last = $next + $max_bundle_size - 1;
		    $last = $last_index if $last > $last_index;
		    $series_num++;

		    # FIX MINOR:  Either of these formulations works.  The question is, which is
		    # more efficient?  If possible, we'd like to reference elements in the array
		    # slice directly, without copying.  Also look at Data::Alias to see if that
		    # might improve performance.
		    #
		    #	if ( not $rest_api->create_events( [ @rest_event_messages[ $next .. $last ] ], {}, \%outcome, \@results ) ) {
		    #	    ## ...
		    #	}
		    #
		    # FIX MAJOR:  Verify that the construction below yields a reference to the
		    # desired array slice, and that there is not some simpler way to create such
		    # a reference.
		    my @results = ();
		    if (
			not $rest_api->create_events(
			    sub { \@_ }
			      ->( @rest_event_messages[ $next .. $last ] ), {}, \%outcome, \@results
			)
		      )
		    {
			## FIX LATER:  Check the @results thoroughly.  Maybe even retry any failed sends, one time.
			## (But first understand what the REST API itself might do in terms of internal retries,
			## and how to distinguish cases where that type of action might already be in play.)
			log_timed_message 'ERROR:  Could not create events in Foundation (' . code_coordinates() . ').';
			log_outcome \%outcome, 'creation of pending-state events';
			log_results \@results, 'creation of pending-state events';

			# If we have trouble sending data to Foundation, that is cause for restarting this daemon,
			# because there could be some unknown problem on our side that only a full restart can correct.
			# That is true not only for sending status data, but also for sending the few events that the
			# status feeder might generate.  Those events will later be critical to accurately generating
			# availability graphs, so we need to ensure that they get through.
			#
			# At one point in the development of this feeder, we saw some event-creation failures happen
			# in the field, that initially did not appear to be the fault of this feeder.  At the time,
			# we could not replicate those problems in-house, but the relevant JIRAs (GWMON-12029 and
			# GWMON-12030) indicate that the problem did in fact originate in the status feeder, because
			# it was trying to use asynchronous REST calls for event creation and asynchronicity is not
			# supported for that type of call.  (Why we could not therefore replicate the problem in-house
			# is something of a mystery, but may have had to do with the particular development versions
			# under test.)  We have since removed all references to such asynchronicity.  We do believe it
			# is important that the events we generate get through, and have no model for why the feeder
			# would have failed other than that, so we are once again re-enabling having the daemon restart
			# upon such failures.
			#
			$failed = 1;
			last;
		    }

		    $next = $last + 1;
		}

		# Here we don't discard messages we could not send.
		# That means they will build up indefinitely until we do.
		# FIX MAJOR:  If some events got created but others not, then we would want to know about that.
		# Is there some way to get status back from the REST API in that level of detail, when we send
		# in a bunch of events all at once to be created?
		if ( !$failed ) {
		    @rest_event_messages = ();
		}
	    }
	}
    }
    else {
	if ( scalar(@xml_event_messages) >= $max_bundle_size ) {
	    my $socket;
	    $failed = 1;
	    for ( my $attempts = 10 ; --$attempts >= 0 ; ) {
		## SendBuf is an as-yet-undocumented patch to IO::Socket::INET.
		my @socket_args = ( PeerAddr => $foundation_host, PeerPort => $foundation_port, Proto => 'tcp', Type => SOCK_STREAM );
		push @socket_args, ( SendBuf => $send_buffer_size ) if ( $send_buffer_size > 0 );
		if ( $socket = IO::Socket::INET->new(@socket_args) ) {
		    $socket->autoflush();
		    log_timed_message 'Pending events local port: ', $socket->sockport() if $debug_summary;
		    $failed = 0;
		    last if $socket->sockopt( SO_SNDTIMEO, pack( 'L!L!', $socket_send_timeout, 0 ) );
		    log_socket_problem('setting send timeout on');
		    $failed = 1;
		    unless ( close($socket) ) {
			log_socket_problem('closing');
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
		my $element_begin =
		  qq(<Adapter Session="$series_num" AdapterType="SystemAdmin">\n<Command ApplicationType='NAGIOS' Action='ADD'>);
		my $element_end = "</Command>\n</Adapter>";
		my $elements = join( "\n", $element_begin, @xml_event_messages, $element_end, $command_close );
		LOG->print( $elements, "\n" ) if $debug_xml && !$log_as_utf8;
		utf8::encode($elements);

		log_timed_message 'Writing events message to Foundation.' if $debug_summary;
		unless ( $socket->print($elements) ) {
		    log_socket_problem('writing to');
		    $failed = 1;
		}
		else {
		    LOG->print( $elements, "\n" ) if $debug_xml && $log_as_utf8;
		}
		unless ( close($socket) ) {
		    log_socket_problem('closing');
		    $failed = 1;
		}
		## Here we don't discard messages we could not send.
		## That means they will build up indefinitely until we do.
		if ( !$failed ) {
		    @xml_event_messages = ();
		}
	    }
	}
    }
    if ($shutdown_requested) {
	log_shutdown();
	$failed = 1;
    }
    return $failed ? -1 : $series_num;
}

# FIX MINOR:  drop v1/v2 support here, as it is no longer being maintained
sub get_status {
    my $statusfile      = shift;
    my $version         = shift;
    my $old_element_ref = shift;
    if ( $version == 3 ) {
	return get_status_v3($statusfile, $old_element_ref);
    }
    if ( $version == 2 ) {
	return get_status_v2($statusfile, $old_element_ref);
    }
    if ( $version == 1 ) {
	return get_status_v1($statusfile, $old_element_ref);
    }
    log_message "$0 error: unknown Nagios version: [$version]";
    sleep $failure_sleep_time;
    return undef;
}

## ----------------------------------------------------------------------
## 120809.rlynch:
## UNIFIED encoding
## encode_text is a somewhat ugly combination of formatting changes (collapsing various forms of
## whitespace into single space characters) and encoding for transmission via the XML socket API.
## More or less for the sake of convenience in the places it is called, it confuses the permanent
## transformation of data to make it perhaps easier to display in some downstream contexts with the
## temporary transformation needed to pass it through an XML channel to Foundation.  We should igure
## out what the real intent is in all of the places it is currently being called, figure out why
## the treatment of breaks is different in various places in the original code and what should or
## should not be done about that, put together one or more routines that do the right things and are
## sensibly named, and rework the code to alleviate the current mass confusion.
## ----------------------------------------------------------------------

sub nolinebreaks {
    my $value = shift;
    if ( defined $value ) {
	## FIX MINOR:  We should test to see if these substitutions would be faster if done in just one scan.
	$value =~ s/\n/ /g;       # convert newline to space
	$value =~ s/\f/ /g;       # and    formfeed
	$value =~ s/<br>/ /ig;    # and html breaks
    }
    return $value;
}

sub text2xml {
    my $value = shift;
    if ( defined $value ) {
	$value =~ s/&/&amp;/g;    # then go and convert naked 'bad chars'
	$value =~ s/"/&quot;/g;
	$value =~ s/'/&apos;/g;
	$value =~ s/</&lt;/g;
	$value =~ s/>/&gt;/g;
    }
    return $value;
}

sub encode_text {
    return text2xml( nolinebreaks(shift) );
}

## ----------------------------------------------------------------------
## 120809.rlynch:
## for UNIFIED encoding of strings, instead of all the inline stuff, which is hard to maintain
## and does NOT need to be inline either for efficiency, or for specific cases with different
## substitutions (I checked).
## GH#1:  I suspect that there is significant overhead in passing the string value in and out of this
## routine (and note that this routine will get called a large number of times).  How much overhead
## this imposes, we cannot say without explicit performance testing.  I do like the notion of
## centralizing this transform, but perhaps it would be better implemented with pass-by-reference.
## GH#2:  Actual testing with a small benchmark program shows there may be a seriously bad and
## program-pervasive counterintuitive performance hit once you call a routine like this with
## pass-by-reference.  So we shouldn't go changing this without understanding the real-world impact.
## ----------------------------------------------------------------------
sub xml2text
{
    my $value = shift;

    $value =~ s/&gt;/>/g;
    $value =~ s/&lt;/</g;
    $value =~ s/&apos;/'/g;
    $value =~ s/&quot;/"/g;
    $value =~ s/&amp;/&/g;

    return $value;
}

# FIX MINOR:  drop v1/v2 support here, as it is no longer being maintained
sub get_status_v1 {
    my $statusfile      = shift;
    my $old_element_ref = shift;
    my ( $timestamp, $msgtype );
    my @field;
    my $element_ref;

    # FIX LATER:  don't just abort on failure; retry 3 times or so
    if ( !open( STATUSFILE, '<:unix:mmap', $statusfile ) ) {
	log_timed_message "Error opening file $statusfile: $!";
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
	    $field[31] = &{ $use_rest_api ? \&nolinebreaks : \&encode_text }( $field[31] );
	    # $field[31] =~ s/\n/ /g;
	    # $field[31] =~ s/\f/ /g;
	    # $field[31] =~ s/<br>/ /ig;
	    # $field[31] =~ s/&/&amp;/g;
	    # $field[31] =~ s/"/&quot;/g;
	    # $field[31] =~ s/'/&apos;/g;
	    # $field[31] =~ s/</&lt;/g;
	    # $field[31] =~ s/>/&gt;/g;

	    # $el_svc->{RetryNumber} = '1'; #$field[4];
	    my $tmp = $field[4];
	    if ( $tmp =~ /(\d+)\/(\d+)/ ) {
		my $RetryNumber = $1;
		my $MaxTry      = $2;
		$el_svc->{RetryNumber} = $RetryNumber;
	    }
	    $el_svc->{MonitorStatus}              = $field[3];
	    $el_svc->{StateType}                  = $field[5];
	    $el_svc->{LastCheckTime}              = unix_to_internal_time( $field[6] );
	    $el_svc->{NextCheckTime}              = unix_to_internal_time( $field[7] );
	    $el_svc->{CheckType}                  = $field[8];
	    $el_svc->{isChecksEnabled}            = $field[9];
	    $el_svc->{isAcceptPassiveChecks}      = $field[10];
	    $el_svc->{isEventHandlersEnabled}     = $field[11];
	    $el_svc->{LastStateChange}            = unix_to_internal_time( $field[12] );
	    $el_svc->{isProblemAcknowledged}      = $field[13];
	    $el_svc->{LastHardState}              = $field[14];
	    $el_svc->{TimeOK}                     = $field[15];
	    $el_svc->{TimeUnknown}                = $field[16];
	    $el_svc->{TimeWarning}                = $field[17];
	    $el_svc->{TimeCritical}               = $field[18];
	    $el_svc->{LastNotificationTime}       = unix_to_internal_time( $field[19] );
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
	    $el_svc->{PerformanceData}            = $field[32];
	}
	elsif ( $msgtype =~ /HOST/ ) {
	    if ( $field[3] == 0 ) { $field[3] = time; }
	    if ( $field[4] == 0 ) { $field[4] = time; }
	    $field[20] = &{ $use_rest_api ? \&nolinebreaks : \&encode_text }( $field[20] );
	    # $field[20] =~ s/\n/ /g;
	    # $field[20] =~ s/\f/ /g;
	    # $field[20] =~ s/<br>/ /ig;
	    # $field[20] =~ s/&/&amp;/g;
	    # $field[20] =~ s/"/&quot;/g;
	    # $field[20] =~ s/'/&apos;/g;
	    # $field[20] =~ s/</&lt;/g;
	    # $field[20] =~ s/>/&gt;/g;
	    $el_host->{MonitorStatus}              = $field[2];
	    $el_host->{LastCheckTime}              = unix_to_internal_time( $field[3] );
	    $el_host->{LastStateChange}            = unix_to_internal_time( $field[4] );
	    $el_host->{isAcknowledged}             = $field[5];
	    $el_host->{TimeUp}                     = $field[6];
	    $el_host->{TimeDown}                   = $field[7];
	    $el_host->{TimeUnreachable}            = $field[8];
	    $el_host->{LastNotificationTime}       = unix_to_internal_time( $field[9] );
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
#                                                    $field[21];   # where is this one?
	    $el_host->{PerformanceData}            = $field[22];
	}
	elsif ( $msgtype =~ /PROGRAM/ ) {
	}
    }
    close STATUSFILE;
    return $element_ref;
}

# FIX MINOR:  drop v1/v2 support here, as it is no longer being maintained
sub get_status_v2 {
    my $statusfile      = shift;
    my $old_element_ref = shift;
    my ( $timestamp, $msgtype );
    my @field;
    my $element_ref;

    # FIX LATER:  don't just abort on failure; retry 3 times or so
    if ( !open( STATUSFILE, '<:unix:mmap', $statusfile ) ) {
	log_timed_message "Error opening file $statusfile: $!";
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
		$el_svc->{MonitorStatus} = 'PENDING';
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
		$attribute{plugin_output} = &{ $use_rest_api ? \&nolinebreaks : \&encode_text }( $attribute{plugin_output} );
		# $attribute{plugin_output} =~ s/\n/ /g;
		# $attribute{plugin_output} =~ s/\f/ /g;
		# $attribute{plugin_output} =~ s/<br>/ /ig;
		# $attribute{plugin_output} =~ s/&/&amp;/g;
		# $attribute{plugin_output} =~ s/"/&quot;/g;
		# $attribute{plugin_output} =~ s/'/&apos;/g;
		# $attribute{plugin_output} =~ s/</&lt;/g;
		# $attribute{plugin_output} =~ s/>/&gt;/g;
	    }

	    if ( $attribute{last_state_change} == 0 ) { $attribute{last_state_change} = time; }
	    ## Collage expects latency in integer. Set to ms
	    $attribute{check_latency} = int( 1000 * $attribute{check_latency} );
	    ## Collage expects execution time in integer. Set to ms
	    $attribute{check_execution_time} = int( 1000 * $attribute{check_execution_time} );

	    $el_svc->{CheckType}                  = $CheckType{ $attribute{check_type} };
	    $el_svc->{CurrentNotificationNumber}  = $attribute{current_notification_number};
	    $el_svc->{ExecutionTime}              = $attribute{check_execution_time};
	    $el_svc->{LastCheckTime}              = unix_to_internal_time( $attribute{last_check} );
	    $el_svc->{LastHardState}              = $ServiceStatus{ $attribute{last_hard_state} };
	    $el_svc->{LastNotificationTime}       = unix_to_internal_time( $attribute{last_notification} );
	    $el_svc->{LastPluginOutput}           = $attribute{plugin_output};
	    $el_svc->{LastStateChange}            = unix_to_internal_time( $attribute{last_state_change} );
	    $el_svc->{Latency}                    = $attribute{check_latency};
	    $el_svc->{NextCheckTime}              = unix_to_internal_time( $attribute{next_check} );
	    $el_svc->{PercentStateChange}         = $attribute{percent_state_change};
	    $el_svc->{PerformanceData}            = $attribute{performance_data};
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
		$attribute{plugin_output} = &{ $use_rest_api ? \&nolinebreaks : \&encode_text }( $attribute{plugin_output} );
		# $attribute{plugin_output} =~ s/\n/ /g;
		# $attribute{plugin_output} =~ s/\f/ /g;
		# $attribute{plugin_output} =~ s/<br>/ /ig;
		# $attribute{plugin_output} =~ s/&/&amp;/g;
		# $attribute{plugin_output} =~ s/"/&quot;/g;
		# $attribute{plugin_output} =~ s/'/&apos;/g;
		# $attribute{plugin_output} =~ s/</&lt;/g;
		# $attribute{plugin_output} =~ s/>/&gt;/g;
	    }

	    if ( ( $attribute{last_check} == 0 ) and ( $attribute{has_been_checked} == 0 ) ) {
		## $attribute{last_check} = time;
		$el_host->{MonitorStatus} = 'PENDING';
	    }
	    else {
		$el_host->{MonitorStatus} = $HostStatus{ $attribute{current_state} };
	    }

	    if ( $attribute{last_state_change} == 0 ) { $attribute{last_state_change} = time; }

	    $el_host->{CheckType}                  = $CheckType{ $attribute{check_type} };
	    $el_host->{CurrentNotificationNumber}  = $attribute{current_notification_number};
	    $el_host->{LastCheckTime}              = unix_to_internal_time( $attribute{last_check} );
	    $el_host->{LastNotificationTime}       = unix_to_internal_time( $attribute{last_notification} );
	    $el_host->{LastPluginOutput}           = $attribute{plugin_output};
	    $el_host->{LastStateChange}            = unix_to_internal_time( $attribute{last_state_change} );
	    $el_host->{PercentStateChange}         = $attribute{percent_state_change};
	    $el_host->{PerformanceData}            = $attribute{performance_data};
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
    my $statusfile      = shift;
    my $old_element_ref = shift;
    local $_;

    my ( $timestamp, $msgtype );
    my @field;
    my $element_ref;
    my $check_old = %$old_element_ref ? 1                              : 0;
    my $old_hosts = $check_old        ? \%{ $old_element_ref->{Host} } : {};
    my $el_hosts  = \%{ $element_ref->{Host} };
    my $now       = time();

    # FIX LATER:  don't just abort on failure; retry 3 times or so
    if ( !open( STATUSFILE, '<:unix:mmap', $statusfile ) ) {
	log_timed_message "Error opening file $statusfile: $!";
	sleep $failure_sleep_time;
	return undef;
    }
    my $state          = '';
    my $hostcomment    = undef;
    my $servicecomment = undef;
    my %attribute      = ();
    $n_hostcount    = 0;
    $n_servicecount = 0;
    my $entrytime;
    while ( my $line = <STATUSFILE> ) {
	if ($shutdown_requested) {
	    return undef;
	}
	chomp $line;
	if ( $line =~ /^\s*\#]/ ) { next; }
	if ( !$state and ( $line =~ /\s*host(?:status)?\s*\{/ ) ) {
	    $state = 'Host';
	    ++$n_hostcount;
	    next;
	}
	elsif ( !$state and ( $line =~ /\s*service(?:status)?\s*\{/ ) ) {
	    $state = 'Service';
	    ++$n_servicecount;
	    next;
	}
	elsif ( ( $state eq 'Service' ) and ( $line =~ /^\s*\}/ ) and $attribute{host_name} and $attribute{service_description} ) {
	    ## We don't bother to check if the host-level hash element exists first, which means this
	    ## check will auto-vivify that level and cause it to exist if it didn't before, confusing
	    ## our checking.  We're saved by the fact that the Nagios status log file dumps out all host
	    ## objects before all service objects, so we will have checked the host level earlier, and
	    ## we wouldn't still be here if such auto-vivification were about to occur.
	    if ($check_old and not exists $old_hosts->{ $attribute{host_name} }->{Service}->{ $attribute{service_description} }) {
		log_timed_message "Host \"$attribute{host_name}\" service \"$attribute{service_description}\" is new; need to restart.";
		return undef;
	    }
	    my $el_svc = \%{ $el_hosts->{ $attribute{host_name} }->{Service}->{ $attribute{service_description} } };

	    # The Nagios internal state-transition clocking which determines how we should set the MonitorStatus, LastHardState,
	    # and LastHardStateChange values here is extremely complex.  So much so, that it's not worth trying to maintain a
	    # detailed account of it in this code, though it is vital for understanding the choices we make here.  Instead, see
	    # the companion README.status-feeder file for a complete analysis.

	    # Check for PENDING service status.
	    if ( $attribute{has_been_checked} == 0 && $attribute{last_check} == 0 ) {
		$el_svc->{MonitorStatus} = 'PENDING';
	    }
	    else {
		$el_svc->{MonitorStatus} = $ServiceStatus{ $attribute{current_state} };
	    }

	    # Check for recent PENDING status.
	    if (   ( $attribute{has_been_checked} == 0 && $attribute{last_check} == 0 )
		|| ( $attribute{state_type} == 0 && $attribute{last_problem_id} == 0 ) )
	    {
		## We've never had an accurate HARD state, so mark it as PENDING.
		$el_svc->{LastHardState}       = 'PENDING';
		$el_svc->{LastHardStateChange} = unix_to_internal_time(0);
	    }
	    else {
		$el_svc->{LastHardState}       = $ServiceStatus{ $attribute{last_hard_state} };
		$el_svc->{LastHardStateChange} = unix_to_internal_time( $attribute{last_hard_state_change} );
	    }

	    if ($attribute{performance_data}) {
		$attribute{performance_data} = &{ $use_rest_api ? \&nolinebreaks : \&encode_text }( $attribute{performance_data} );
	    }

	    if ($attribute{plugin_output}) {
		$attribute{plugin_output} = &{ $use_rest_api ? \&nolinebreaks : \&encode_text }( $attribute{plugin_output} );
		## $attribute{plugin_output} =~ s/\n/ /g;
		## $attribute{plugin_output} =~ s/\f/ /g;
		## $attribute{plugin_output} =~ s/<br>/ /ig;
		## $attribute{plugin_output} =~ s/&/&amp;/g;
		## $attribute{plugin_output} =~ s/"/&quot;/g;
		## $attribute{plugin_output} =~ s/'/&apos;/g;
		## $attribute{plugin_output} =~ s/</&lt;/g;
		## $attribute{plugin_output} =~ s/>/&gt;/g;
	    }

	    if ($attribute{long_plugin_output}) {
		$attribute{long_plugin_output} = &{ $use_rest_api ? \&nolinebreaks : \&encode_text }( $attribute{long_plugin_output} );
		## $attribute{long_plugin_output} =~ s/\n/ /g;
		## $attribute{long_plugin_output} =~ s/\f/ /g;
		## $attribute{long_plugin_output} =~ s/<br>/ /ig;
		## $attribute{long_plugin_output} =~ s/&/&amp;/g;
		## $attribute{long_plugin_output} =~ s/"/&quot;/g;
		## $attribute{long_plugin_output} =~ s/'/&apos;/g;
		## $attribute{long_plugin_output} =~ s/</&lt;/g;
		## $attribute{long_plugin_output} =~ s/>/&gt;/g;
	    }

	    # This is an old correction, that seems to just cause problems by constantly changing this value
	    # in the absence of any real input (a type of sensory-deprivation hallucination).  Now we support
	    # an undefined timestamp value instead, so there is no need for this dynamic recoding.
	    # if ( $attribute{last_state_change} == 0 ) { $attribute{last_state_change} = $now; }

	    ## Collage expects latency in integer. Set to ms
	    $attribute{check_latency} = int( 1000 * $attribute{check_latency} );
	    ## Collage expects execution time in integer. Set to ms
	    $attribute{check_execution_time} = int( 1000 * $attribute{check_execution_time} );

	    # FIX MAJOR:  Should this handling of service plugin output (short and long) be used
	    #  as a model for handling host plugin output?
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
	    $el_svc->{last_check}                 = $attribute{last_check} if $send_actual_check_timestamps;
	    $el_svc->{LastCheckTime}              = unix_to_internal_time( $attribute{last_check} );
	    $el_svc->{LastNotificationTime}       = unix_to_internal_time( $attribute{last_notification} );
	    $el_svc->{LastPluginOutput}           = $plugin_output;
	    $el_svc->{LastStateChange}            = unix_to_internal_time( $attribute{last_state_change} );
	    $el_svc->{Latency}                    = $attribute{check_latency};
	    $el_svc->{MaxAttempts}                = $attribute{max_attempts};
	    $el_svc->{NextCheckTime}              = unix_to_internal_time( $attribute{next_check} );
	    $el_svc->{PercentStateChange}         = $attribute{percent_state_change};
	    $el_svc->{PerformanceData}            = $attribute{performance_data};
	    ## FIX MINOR:  drop support for RetryNumber, as it just duplicates CurrentAttempt and is no longer used later on
	    ## $el_svc->{RetryNumber}                = $attribute{current_attempt};
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
	    if ($check_old and not exists $old_hosts->{ $attribute{host_name} }) {
		log_timed_message "Host \"$attribute{host_name}\" is new; need to restart.";
		return undef;
	    }
	    my $el_host = \%{ $el_hosts->{ $attribute{host_name} } };

	    # The Nagios internal state-transition clocking which determines how we should set the MonitorStatus, LastHardState,
	    # and LastHardStateChange values here is extremely complex.  So much so, that it's not worth trying to maintain a
	    # detailed account of it in this code, though it is vital for understanding the choices we make here.  Instead, see
	    # the companion README.status-feeder file for a complete analysis.

	    # Check for PENDING host status.
	    if ( $attribute{has_been_checked} == 0 && $attribute{last_check} == 0 ) {
		$el_host->{MonitorStatus} = 'PENDING';
	    }
	    else {
		$el_host->{MonitorStatus} = $HostStatus{ $attribute{current_state} };
	    }

	    # Check for recent PENDING status.
	    if (   ( $attribute{has_been_checked} == 0 && $attribute{last_check} == 0 )
		|| ( $attribute{state_type} == 0 && $attribute{last_problem_id} == 0 ) )
	    {
		## We've never had an accurate HARD state, so mark it as PENDING.
		$el_host->{LastHardState}       = 'PENDING';
		$el_host->{LastHardStateChange} = unix_to_internal_time(0);
	    }
	    else {
		$el_host->{LastHardState}       = $HostStatus{ $attribute{last_hard_state} };
		$el_host->{LastHardStateChange} = unix_to_internal_time( $attribute{last_hard_state_change} );
	    }

	    # This is an old correction, that seems to just cause problems by constantly changing this value
	    # in the absence of any real input (a type of sensory-deprivation hallucination).  Now we support
	    # an undefined timestamp value instead, so there is no need for this dynamic recoding.
	    # if ( $attribute{last_state_change} == 0 ) { $attribute{last_state_change} = $now; }

	    # FIX MAJOR:  Should we be handling long plugin output here too, as we do for services,
	    # comparing short and long and saving both if both are provided?  Compare to the code above.
	    if ($attribute{plugin_output}) {
		$attribute{plugin_output} = &{ $use_rest_api ? \&nolinebreaks : \&encode_text }( $attribute{plugin_output} );
		## $attribute{plugin_output} =~ s/\n/ /g;
		## $attribute{plugin_output} =~ s/\f/ /g;
		## $attribute{plugin_output} =~ s/<br>/ /ig;
		## $attribute{plugin_output} =~ s/&/&amp;/g;
		## $attribute{plugin_output} =~ s/"/&quot;/g;
		## $attribute{plugin_output} =~ s/'/&apos;/g;
		## $attribute{plugin_output} =~ s/</&lt;/g;
		## $attribute{plugin_output} =~ s/>/&gt;/g;
	    }

	    ## Collage expects latency in integer. Set to ms
	    $attribute{check_latency} = int( 1000 * $attribute{check_latency} );
	    ## Collage expects execution time in integer. Set to ms
	    $attribute{check_execution_time} = int( 1000 * $attribute{check_execution_time} );

	    $el_host->{CheckType}                  = $CheckType{ $attribute{check_type} };
	    $el_host->{CurrentAttempt}             = $attribute{current_attempt};
	    $el_host->{CurrentNotificationNumber}  = $attribute{current_notification_number};
	    $el_host->{ExecutionTime}              = $attribute{check_execution_time};
	    $el_host->{last_check}                 = $attribute{last_check} if $send_actual_check_timestamps;
	    $el_host->{LastCheckTime}              = unix_to_internal_time( $attribute{last_check} );
	    $el_host->{LastNotificationTime}       = unix_to_internal_time( $attribute{last_notification} );
	    $el_host->{LastPluginOutput}           = $attribute{plugin_output};
	    $el_host->{LastStateChange}            = unix_to_internal_time( $attribute{last_state_change} );
	    $el_host->{Latency}                    = $attribute{check_latency};
	    $el_host->{MaxAttempts}                = $attribute{max_attempts};
	    $el_host->{NextCheckTime}              = unix_to_internal_time( $attribute{next_check} );
	    $el_host->{PercentStateChange}         = $attribute{percent_state_change};
	    $el_host->{PerformanceData}            = $attribute{performance_data};
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
	    $entrytime = sprintf '%02d-%02d-%4d %02d:%02d:%02d', $mon + 1, $mday, $year + 1900, $hour, $min, $sec;
	    $attribute{comment_data} =~ tr/'"//d;
	    ## Customers have asked that the most recent comments be listed first, so we take care of that here.
	    ## This depends on Nagios listing them in chronological order in the status file, since we're not
	    ## explicitly comparing the $entry_time value in this logic.
	    push @{ $el_hosts->{ $attribute{host_name} }->{Comments} },
	      [
		"#!#$attribute{comment_id};::;$entrytime;::;$attribute{author};::;\'$attribute{comment_data}\'",
		$attribute{entry_time} + 0,
		$attribute{comment_id} + 0
	      ];
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
	    $entrytime = sprintf '%02d-%02d-%4d %02d:%02d:%02d', $mon + 1, $mday, $year + 1900, $hour, $min, $sec;
	    $attribute{comment_data} =~ tr/'"//d;
	    ## Customers have asked that the most recent comments be listed first, so we take care of that here.
	    ## This depends on Nagios listing them in chronological order in the status file, since we're not
	    ## explicitly comparing the $entry_time value in this logic.
	    push @{ $el_hosts->{ $attribute{host_name} }->{Service}->{ $attribute{service_description} }->{Comments} },
	      [
		"#!#$attribute{comment_id};::;$entrytime;::;$attribute{author};::;\'$attribute{comment_data}\'",
		$attribute{entry_time} + 0,
		$attribute{comment_id} + 0
	      ];
	    $servicecomment = undef;
	}
	else {
	    next;
	}
    }
    close STATUSFILE;

    # Fix all the comments (once)
    my $com_ref = undef;
    my $comment = undef;
    foreach my $hostkey ( keys(%$el_hosts) ) {
	my $el_host = \%{ $el_hosts->{$hostkey} };
	$com_ref = $el_host->{Comments};
	if ( defined $com_ref ) {
	    ## We sort by entry_time, then by comment_id, outputting in reverse-time order.
	    ## For efficiency, we do so using a variant of the Schwartzian Transform
	    ## (see the "sort" function description in Programming Perl), but with the
	    ## initial map of the map-sort-map sequence already constructed above.
	    $comment = join( '', map { $_->[0] } sort { $b->[1] <=> $a->[1] || $b->[2] <=> $a->[2] } @$com_ref );
	    $comment = &{ $use_rest_api ? \&nolinebreaks : \&encode_text }($comment);
	    $el_host->{Comments} = $comment;
	    log_message "DEBUG:  Host Comments for host $hostkey: $comment" if $debug_debug;
	}
	else {
	    $el_host->{Comments} = ' ';
	}
	my $el_svcs = $el_host->{Service};
	if ( defined $el_svcs ) {
	    foreach my $servicekey ( keys( %{$el_svcs} ) ) {
		my $el_svc = \%{ $el_svcs->{$servicekey} };
		$com_ref = $el_svc->{Comments};
		if ( defined $com_ref ) {
		    ## We sort by entry_time, then by comment_id, outputting in reverse-time order.
		    ## For efficiency, we do so using a variant of the Schwartzian Transform
		    ## (see the "sort" function description in Programming Perl), but with the
		    ## initial map of the map-sort-map sequence already constructed above.
		    $comment = join( '', map { $_->[0] } sort { $b->[1] <=> $a->[1] || $b->[2] <=> $a->[2] } @$com_ref );
		    $comment = &{ $use_rest_api ? \&nolinebreaks : \&encode_text }($comment);
		    $el_svc->{Comments} = $comment;
		    log_message "DEBUG:  Service Comments for host $hostkey, service $servicekey: $comment" if $debug_debug;
		}
		else {
		    $el_svc->{Comments} = ' ';
		}
	    }
	}
    }
    return $element_ref;
}

sub get_globals {
    my $statusfile = shift;
    my ( $timestamp, $msgtype );
    my @field;

    # FIX LATER:  don't just abort on failure; retry 3 times or so
    if ( !open( STATUSFILE, '<:unix:mmap', $statusfile ) ) {
	log_timed_message  "Error opening file $statusfile: $!";
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
		log_message "DEBUG:  Global Attribute found: $1 = $2" if $debug_debug;
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
    my $gwconfigfile = '/usr/local/groundwork/config/db.properties';
    if ( $type !~ /^(collage|insightreports)$/ ) { return 'ERROR: Invalid database type.'; }
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
	    if ( $1 eq 'username' ) {
		$username = $2;
	    }
	    elsif ( $1 eq 'password' ) {
		$password = $2;
	    }
	    elsif ( $1 eq 'database' ) {
		$database = $2;
	    }
	    elsif ( $1 eq 'dbhost' ) {
		$dbhost = $2;
	    }
	}
    }
    close CONFIG;
    return ( $database, $dbhost, $username, $password );
}

sub hostStatusChange {
    my $el_hosts      = shift;
    my $cs_hosts      = shift;
    my $hostkey       = shift;
    my $state_changes = shift;    # arrayref or undef
    my %host_change   = ();
    my $el_host       = $el_hosts->{$hostkey};
    my $cs_host       = $cs_hosts->{$hostkey};
    my $data_change   = 0;
    my $timing_change = 0;
    my $have_cache    = 0;
    my $remote_change = 0;
    my $rm_host       = undef;
    my $rm_field      = undef;
    my $el_host_field;
    my $cs_host_field;

    # FIX MINOR:  Note the following about the first time this routine is called after daemon startup, after
    # new hosts have been added to the system.  At least when using the Foundation socket API, Monarch sets
    # the LastStateChange property on each new host, when it sends the add-host command to Foundation.  This
    # will contradict Nagios's notion of the last_state_change=0 for such hosts.  So our comparison here on
    # the first cycle will necessarily set $data_change=1 and force an update for every such host, even if
    # nothing else has actually yet changed for that host.  In this case, the LastStateChange value itself
    # will be suppressed in the update when using the REST API, but the rest of the REST object for the host
    # will still be sent.  Perhaps we ought to include some extra logic to detect this situation and avoid
    # the extra initial updates if they're not otherwise forced by other data changes.  After the first
    # cycle, of course, our internal cache will be updated with the value from Nagios, and this first-cycle
    # data-mismatch condition will no longer apply.

    my $prefer_hard_data = $ignore_soft_states && $el_host->{StateType} eq 'SOFT';

    # We always need these fields if we send any data (GWMON-7684) ...
    foreach my $field ( keys %mandatory_host_data_change ) {
	$el_host_field       = $el_host->{ $prefer_hard_data ? $mandatory_host_data_change{$field} : $field };
	$cs_host_field       = $cs_host->{$field};
	$el_host_field       = '' if not defined $el_host_field;
	$cs_host_field       = '' if not defined $cs_host_field;
	$host_change{$field} = $el_host_field;
	## but don't miss a change to these ...
	## This handles both ordinary scalar string-value comparsion and DateTime comparison,
	## since DateTime overloads the "ne" string-comparison operator appropriately for use
	## when the other operand is not also a DateTime object.
	if ( $el_host_field ne $cs_host_field ) {
	    $data_change = 1;
	    log_message "Found data change for host $hostkey field "
	      . ( $prefer_hard_data ? $mandatory_host_data_change{$field} : $field )
	      . ": \"$cs_host_field\" => \"$el_host_field\"."
	      if $debug_debug;
	}
    }
    ## Check each condition that might require an update to the database status.
    foreach my $field (@host_data_change) {
	$el_host_field = $el_host->{$field}; $el_host_field = '' if not defined $el_host_field;
	$cs_host_field = $cs_host->{$field}; $cs_host_field = '' if not defined $cs_host_field;
	## This handles both ordinary scalar string-value comparsion and DateTime comparison,
	## since DateTime overloads the "ne" string-comparison operator appropriately for use
	## when the other operand is not also a DateTime object.
	if ( $el_host_field ne $cs_host_field ) {
	    $host_change{$field} = $el_host_field;
	    $data_change = 1;
	    log_message "Found data change for host $hostkey field $field: \"$cs_host_field\" => \"$el_host_field\"." if $debug_debug;
	}
    }
    ## Check each condition that might require an update to the timing change fields
    ## (sync only on heartbeat, or if other data has changed).
    log_message "Checking host $hostkey" if $debug_debug;
    foreach my $field (@host_timing_change) {
	$el_host_field = $el_host->{$field}; $el_host_field = '' if not defined $el_host_field;
	$cs_host_field = $cs_host->{$field}; $cs_host_field = '' if not defined $cs_host_field;
	## This handles both ordinary scalar string-value comparsion and DateTime comparison,
	## since DateTime overloads the "ne" string-comparison operator appropriately for use
	## when the other operand is not also a DateTime object.
	if ( $el_host_field ne $cs_host_field ) {
	    $host_change{$field} = $el_host_field;
	    $timing_change = 1;
	    if ( defined( $rm_host = $remote_hosts->{$hostkey} ) ) {
		$have_cache = 1;
		if ( defined( $rm_field = $rm_host->{$field} ) and $el_host_field ne $rm_field ) {
		    log_message "Remote cache update for host $hostkey field $field: \"$rm_field\" => \"$el_host_field\"." if $debug_debug;
		    $rm_host->{$field} = $el_host_field;
		    $remote_change = 1;
		}
	    }
	    log_message "Found timing change for host $hostkey field $field: \"$cs_host_field\" => \"$el_host_field\"." if $debug_debug;
	}
    }

    if ($data_change) {
	## Check for "Pending Transition", so we can send an event and trigger a state change
	## when we go from PENDING to UP.
	my $el_status = $el_host->{ $prefer_hard_data ? 'LastHardState' : 'MonitorStatus' };
	my $cs_status = $cs_host->{MonitorStatus};
	if ( not defined $el_status ) {
	    log_message "WARNING:  Host $hostkey has no "
	      . ( $prefer_hard_data ? 'LastHardState' : 'MonitorStatus' )
	      . ' in Nagios.'
	      if $debug_summary;
	}
	elsif ( not defined $cs_status ) {
	    log_message "WARNING:  Host $hostkey has no MonitorStatus in Foundation." if $debug_summary;
	}
	elsif ( ( $el_status eq 'UP' ) and ( $cs_status eq 'PENDING' ) ) {
	    my $queueing_status = queue_pending_host_event( $el_host, $hostkey );
	    return $restart_change if $queueing_status != CONTINUE_STATUS;
	}
	log_message "Found data change for host $hostkey" if $debug_basic and not $debug_debug;
	log_message( Data::Dumper->Dump( [ \%{$cs_host} ], [qw(\%{cs_hosts})] ) ) if $debug_trace;
	log_message( Data::Dumper->Dump( [ \%{$el_host} ], [qw(\%{el_hosts})] ) ) if $debug_trace;
	push_host_state_change( $hostkey, $el_host, $state_changes ) if defined $state_changes;
	return \%host_change;
    }
    elsif ($timing_change) {
	## See the corresponding code in serviceStatusChange() for a detailed analysis.
	if ( ($heartbeat_mode or $el_host->{StateType} eq 'SOFT') and not ($have_cache xor $remote_change) ) {
	    log_message "Queuing remote timing-only change for host $hostkey" if $debug_debug;
	    push_host_state_change( $hostkey, $el_host, $state_changes ) if defined $state_changes;
	    ## If we're in local heartbeat mode (i.e., if we return %host_change data from this routine, just below), the remote
	    ## cache will be destroyed before we use it again, so there's no point in stuffing the data there in that case.
	    if (!$heartbeat_mode) {
		@{ $remote_hosts->{$hostkey} }{@host_timing_change} = @$el_host{@host_timing_change};
		if ($debug_debug) {
		    log_message "=== remote_hosts fields:";
		    foreach my $key ( keys %{ $remote_hosts->{$hostkey} } ) {
			log_message "=== $key => \"$remote_hosts->{$hostkey}->{$key}\"";
		    }
		}
	    }
	}
	if ($heartbeat_mode) {
	    log_message "Accepting local heartbeat change for host $hostkey" if $debug_basic;
	    return \%host_change;
	}
	else {
	    log_message "Rejecting local change since it's just a timing update and we are not doing a heartbeat: $hostkey" if $debug_basic;
	    return $no_change;
	}
    }
    return $no_change;
}

sub serviceStatusChange {
    my $el_svcs        = shift;
    my $cs_svcs        = shift;
    my $hostkey        = shift;
    my $servicekey     = shift;
    my $state_changes  = shift;    # arrayref or undef
    my %service_change = ();
    my $el_svc         = $el_svcs->{$servicekey};
    my $cs_svc         = $cs_svcs->{$servicekey};
    my $data_change    = 0;
    my $timing_change  = 0;
    my $have_cache     = 0;
    my $remote_change  = 0;
    my $rm_host        = undef;
    my $rm_svc         = undef;
    my $rm_field       = undef;
    my $el_svc_field;
    my $cs_svc_field;

    # FIX MINOR:  Note the following about the first time this routine is called after daemon startup, after
    # new services have been added to the system.  At least when using the Foundation socket API, Monarch sets
    # the LastStateChange property on each new service, when it sends the add-service command to Foundation.
    # This will contradict Nagios's notion of the last_state_change=0 for such services.  So our comparison
    # here on the first cycle will necessarily set $data_change=1 and force an update for every such service,
    # even if nothing else has actually yet changed for that service.  In this case, the LastStateChange value
    # itself will be suppressed in the update when using the REST API, but the rest of the REST object for the
    # service will still be sent.  Perhaps we ought to include some extra logic to detect this situation and
    # avoid the extra initial updates if they're not otherwise forced by other data changes.  After the first
    # cycle, of course, our internal cache will be updated with the value from Nagios, and this first-cycle
    # data-mismatch condition will no longer apply.

    my $prefer_hard_data = $ignore_soft_states && $el_svc->{StateType} eq 'SOFT';

    # We always need these fields if we send any data (GWMON-7684) ...
    foreach my $field ( keys %mandatory_service_data_change ) {
	$el_svc_field           = $el_svc->{ $prefer_hard_data ? $mandatory_service_data_change{$field} : $field };
	$cs_svc_field           = $cs_svc->{$field};
	$el_svc_field           = '' if not defined $el_svc_field;
	$cs_svc_field           = '' if not defined $cs_svc_field;
	$service_change{$field} = $el_svc_field;
	## but don't miss a change to these ...
	## This handles both ordinary scalar string-value comparsion and DateTime comparison,
	## since DateTime overloads the "ne" string-comparison operator appropriately for use
	## when the other operand is not also a DateTime object.
	if ( $el_svc_field ne $cs_svc_field ) {
	    $data_change = 1;
	    log_message "Found data change for host $hostkey service $servicekey field "
	      . ( $prefer_hard_data ? $mandatory_service_data_change{$field} : $field )
	      . ": \"$cs_svc_field\" => \"$el_svc_field\"."
	      if $debug_debug;
	}
    }
    ## Check each condition that might require an update to the database status.
    foreach my $field (@service_data_change) {
	## We sequence the $el_svc_field definition tests here so the first one fails most often,
	## given that the usual setting of $ignore_soft_states will be true.
	$el_svc_field = $el_svc->{$field}; $el_svc_field = '' if not defined $el_svc_field;
	$cs_svc_field = $cs_svc->{$field}; $cs_svc_field = '' if not defined $cs_svc_field;
	## This handles both ordinary scalar string-value comparsion and DateTime comparison,
	## since DateTime overloads the "ne" string-comparison operator appropriately for use
	## when the other operand is not also a DateTime object.
	if ( $el_svc_field ne $cs_svc_field ) {
	    $service_change{$field} = $el_svc_field;
	    $data_change = 1;
	    log_message "Found data change for host $hostkey service $servicekey field $field: \"$cs_svc_field\" => \"$el_svc_field\"."
	      if $debug_debug;
	}
    }
    ## Check fields that constitute a timing update (sync only on heartbeat, or if other data has changed).
    log_message "Checking host $hostkey service $servicekey" if $debug_debug;
    foreach my $field (@service_timing_change) {
	$el_svc_field = $el_svc->{$field}; $el_svc_field = '' if not defined $el_svc_field;
	$cs_svc_field = $cs_svc->{$field}; $cs_svc_field = '' if not defined $cs_svc_field;
	## This handles both ordinary scalar string-value comparsion and DateTime comparison,
	## since DateTime overloads the "ne" string-comparison operator appropriately for use
	## when the other operand is not also a DateTime object.
	if ( $el_svc_field ne $cs_svc_field ) {
	    $service_change{$field} = $el_svc_field;
	    $timing_change = 1;
	    if ( defined( $rm_host = $remote_svcs->{$hostkey} ) and defined( $rm_svc = $rm_host->{$servicekey} ) ) {
		$have_cache = 1;
		if ( defined( $rm_field = $rm_svc->{$field} ) and $el_svc_field ne $rm_field ) {
		    log_message "Remote cache update for host $hostkey service $servicekey field $field: \"$rm_field\" => \"$el_svc_field\"."
		      if $debug_debug;
		    $rm_svc->{$field} = $el_svc_field;
		    $remote_change = 1;
		}
	    }
	    log_message "Found timing change for host $hostkey service $servicekey field $field: \"$cs_svc_field\" => \"$el_svc_field\"."
	      if $debug_debug;
	}
    }

    if ($data_change) {
	## Check for "Pending Transition", so we can send an event and trigger a state change
	## when we go from Pending to OK.
	my $el_status = $el_svc->{ $prefer_hard_data ? 'LastHardState' : 'MonitorStatus' };
	my $cs_status = $cs_svc->{MonitorStatus};
	if ( not defined $el_status ) {
	    log_message "WARNING:  Host $hostkey service $servicekey has no "
	      . ( $prefer_hard_data ? 'LastHardState' : 'MonitorStatus' )
	      . ' in Nagios.'
	      if $debug_summary;
	}
	elsif ( not defined $cs_status ) {
	    log_message "WARNING:  Host $hostkey service $servicekey has no MonitorStatus in Foundation." if $debug_summary;
	}
	elsif ( ( $el_status eq 'OK' ) and ( $cs_status eq 'PENDING' ) ) {
	    my $queueing_status = queue_pending_svc_event( $el_svc, $hostkey, $servicekey );
	    return $restart_change if $queueing_status != CONTINUE_STATUS;
	}
	log_message "Found data change for host $hostkey service $servicekey" if $debug_basic and not $debug_debug;
	log_message( Data::Dumper->Dump( [ \%{$cs_svc} ], [qw(\%{cs_svcs})] ) ) if $debug_trace;
	log_message( Data::Dumper->Dump( [ \%{$el_svc} ], [qw(\%{el_svcs})] ) ) if $debug_trace;
	push_service_state_change( $hostkey, $servicekey, $el_svc, $state_changes ) if defined $state_changes;
	return \%service_change;
    }
    elsif ($timing_change) {
	## This next if() represents our decision making on whether or not to queue timing changes to Nagios on
	## remote systems, independently of whether they are sent to Foundation on the local system.  We may push
	## service state timing-only changes to remote servers even if we are not in heartbeat mode, so the parent
	## Nagios has a chance to clock its SOFT-to-HARD state machine in synchrony with the child Nagios.  But
	## for that to happen, we need to only send SOFT-state timing changes to remote systems when they actually
	## occur, and to send all such changes, which may differ from when they are sent to the local Foundation.
	## $timing_change==1 means:  I have a locally unsent delta in timing data.  It may or may not have already
	##    been queued to remote systems, and it therefore may or may not represent something that we need to
	##    queue now for forwarding to the remote systems.  For that, we need to look at the remote cache (namely,
	##    the derived $have_cache and $remote_change variables) for this service.
	## $have_cache==0 means:  This is a new timing change, never before queued to the remote systems, so we
	##    have not previously created a remote cache for stuff previously queued to the remote systems that
	##    might differ from what has been sent to the local system, at least since the last time the local
	##    and remote states were synchronized.
	## $have_cache==1 means:  Local changes have been pending for at least one processing cycle, and some of
	##    those changes were queued for sending to remote systems and saved in the remote cache for future
	##    comparison.  So now the base for computing remote changes must be the remote cache instead of what
	##    we see as the base for stuff to be sent to the local system.
	## $remote_change==0 means:  Either no remote cache exists (the $have_cache==0 case), or no further changes
	##    have been received since changes were last queued to remote systems (the $have_cache==1 case).
	## $remote_change==1 means:  There has been a new timing change since the last time we queued data to
	##    remote systems.
	## With those definitions in mind, here is the case-by-case analysis for how to make our queue (forward to
	## remote systems) and save (write to remote cache) decisions.  Bear in mind that a local heartbeat will
	## soon flush the remote cache (thereby synchronizing it with the local state), so in those cases there's
	## no point in saving to the cache.

	# cached? rchg? soft? heart? | queue? save? notes
	# ---------------------------+-----------------------------------------------------------------------------
	# N       N     N     N      | N      N     unimportant new timing change; don't queue to remote systems or cache
	# N       N     N     Y      | Y      N     heartbeat forces timing change to be recognized; queue, but don't bother caching
	# N       N     Y     N      | Y      Y     queue new soft state change to clock remote logic; write to cache
	# N       N     Y     Y      | Y      N     queue new soft state change to clock remote logic; don't cache
	# N       Y     N     N      | -      -     impossible case (cache changed, but no cache exists)
	# N       Y     N     Y      | -      -     impossible case (cache changed, but no cache exists)
	# N       Y     Y     N      | -      -     impossible case (cache changed, but no cache exists)
	# N       Y     Y     Y      | -      -     impossible case (cache changed, but no cache exists)
	# Y       N     N     N      | N      N     don't queue or cache (same data was queued previously, and cache is up-to-date)
	# Y       N     N     Y      | N      N     don't queue or cache (same data was queued previously, so ignore heartbeat now)
	# Y       N     Y     N      | N      N     same soft state was previously queued/cached, so don't queue/cache now
	# Y       N     Y     Y      | N      N     same soft state was previously queued/cached, so don't queue/cache now
	# Y       Y     N     N      | N      N     unimportant new remote timing change; don't queue or update cache
	# Y       Y     N     Y      | Y      N     queue new remote timing change as heartbeat; don't bother with cache
	# Y       Y     Y     N      | Y      Y     queue new soft state change to clock remote logic; write to cache
	# Y       Y     Y     Y      | Y      N     queue new soft state change to clock remote logic; don't bother with cache

	if ( ($heartbeat_mode or $el_svc->{StateType} eq 'SOFT') and not ($have_cache xor $remote_change) ) {
	    log_message "Queuing remote timing-only change for host $hostkey service $servicekey" if $debug_debug;
	    push_service_state_change( $hostkey, $servicekey, $el_svc, $state_changes ) if defined $state_changes;
	    ## If we're in local heartbeat mode (i.e., if we return %service_change data from this routine, just below), the remote
	    ## cache will be destroyed before we use it again, so there's no point in stuffing the data there in that case.
	    if (!$heartbeat_mode) {
		@{ $remote_svcs->{$hostkey}->{$servicekey} }{@service_timing_change} = @$el_svc{@service_timing_change};
		if ($debug_debug) {
		    log_message "=== remote_svcs fields:";
		    foreach my $key ( keys %{ $remote_svcs->{$hostkey}->{$servicekey} } ) {
			log_message "=== $key => \"$remote_svcs->{$hostkey}->{$servicekey}->{$key}\"";
		    }
		}
	    }
	}
	if ($heartbeat_mode)  {
	    log_message "Accepting local heartbeat change for host $hostkey service $servicekey" if $debug_basic;
	    return \%service_change;
	}
	else {
	    log_message "Rejecting local change since it's just a timing update and we are not doing a heartbeat: $hostkey $servicekey" if $debug_basic;
	    return $no_change;
	}
    }
    return $no_change;
}

## FIX MAJOR:  verify all %change key names used here
sub find_host_status_changes {
    my $thisnagios      = shift;
    my $element_ref     = shift;
    my $collage_ref     = shift;
    my $state_changes   = shift;                                                  # arrayref or undef
    my $insertcount     = 0;
    my $skipcount       = 0;
    my @output          = ();
    my $el_hosts        = $element_ref->{Host};
    my $cs_hosts        = $collage_ref->{Host};

    # Create REST status-update packets that can be interpreted later on by the REST API.
    @hosts_to_cache = ();
    foreach my $hostkey ( keys %{$el_hosts} ) {
	if ($shutdown_requested) {
	    log_shutdown();
	    return undef;
	}

	# if no host status change then don't send
	my $host_change = undef;
	if ($smart_update) {
	    $host_change = hostStatusChange( $el_hosts, $cs_hosts, $hostkey, $state_changes );
	    if ( not defined $host_change ) {
		return undef;
	    }
	    if ( !%$host_change ) {
		$skipcount++;
		next;
	    }
	}

	# Changes recorded in this routine are not specifically flagged as being for a host.
	# It is up to the calling routine to handle them that way.
	my %change = (
	    appType => 'NAGIOS',

	    # default identification -- set to IP address if known
	    monitorServer => $thisnagios,

	    # default identification -- set to IP address if known
	    hostName => $hostkey
	);

	# Monarch Sync now sets the IP as Identification. We should use address field from Monarch, whatever that is.
	# It's possible that the address changed, or that we are feeding a result for a host that
	# was not in Monarch when this program started. If the Identification is blank, reload the cache.
	if ( $hostipaddress{$hostkey} ) {
	    ## Set Device to IP
	    ## FIX MAJOR:  This should probably really be the "device" field, as documented, but the
	    ## host REST API is only accepting "deviceIdentification" right now.  Get this reviewed.
	    $change{deviceIdentification} = $hostipaddress{$hostkey};
	}
	else {
	    ## For some reason we don't know the IP. Might be a new host? Anyway, reload and try one time more.
	    load_cached_addresses() or return undef;
	    ## Set Device = IP, or bail out and set Device = hostname.  In the latter case,
	    ## there is something wrong with the local Monarch DB.
	    ## FIX MAJOR:  This should probably really be the "device" field, as documented, but the
	    ## host REST API is only accepting "deviceIdentification" right now.  Get this reviewed.
	    $change{deviceIdentification} = $hostipaddress{$hostkey} || $hostkey;
	}
	if ($smart_update) {
	    ## Logically, we just want:
	    ##     @change{ keys %$host_change } = values %$host_change;
	    ## But in fact, we need to convert the key strings we use internal to this program (until someday
	    ## when we drop all support for the XML API, and use the REST API strings natively here instead),
	    ## and we also need to map certain keys into a subsidiary "properties" hash.
	    my $key;
	    my $value;
	    while ( ( $key, $value ) = each %$host_change ) {
		if ( not $rest_time_field{$key} or not is_null_internal_time($value) ) {
		    if ( $host_prop_attr{$key} ) {
			$change{properties}{$key} = $value;
		    }
		    else {
			$change{ $host_rest_attr{$key} || $key } = $value;
		    }
		}
	    }
	}
	else {
	    my $el_host = $el_hosts->{$hostkey};
	    my $prefer_hard_data = $ignore_soft_states && $el_host->{StateType} eq 'SOFT';
	    foreach my $field ( keys %{$el_host} ) {
		next if $field eq 'Service';    # skip the Service hash key
		## FIX MAJOR:  Do we need to perform any recoding of either the key (e.g., to recognize
		## whether it belongs under a {properties} sub-hash, or needs some sort of capitalization
		## change), or the value (e.g., to attempt to alter quoting, although perhaps using
		## &quot; instead of changing the meaning of the data), or logic to suppress this entire
		## assignment (e.g., if this is a null internal timestamp)?  THIS NEEDS TESTING.
		$change{$field} = $el_host->{ $prefer_hard_data && $mandatory_host_data_change{$field} || $field };
	    }
	}

	push @output, \%change;
	if ($smart_update) {
	    if ($continue_after_blockage) {
		## Defer cache updates until we know whether the data made it to Foundation.
		push @hosts_to_cache, $hostkey;
	    }
	    else {
		## Execute cache updates right away, since there's no good reason to wait.
		hostStatusUpdate( $element_ref, $collage_ref, $hostkey );
	    }
	}
	$insertcount++;
	if ( ( $insertcount % 100 ) == 0 ) {
	    log_message "Queueing hosts for insert, count=$insertcount" if $debug_basic;
	}
    }
    if ($smart_update) {
	log_timed_message "Total Hosts Queued for Insert Count=$insertcount. No status change for $skipcount hosts." if $debug_summary;
    }
    else {
	log_timed_message "Total Hosts Queued for Insert Count=$insertcount." if $debug_summary;
    }
    return \@output;
}

## FIX MAJOR:  verify all %change key names used here
sub find_service_status_changes {
    my $thisnagios    = shift;
    my $element_ref   = shift;
    my $collage_ref   = shift;
    my $state_changes = shift;                  # arrayref or undef
    my $insertcount   = 0;
    my $skipcount     = 0;
    my @output        = ();
    my $el_hosts      = $element_ref->{Host};
    my $cs_hosts      = $collage_ref->{Host};

    # Create REST status-update packets that can be interpreted later on by the REST API.
    %host_services_to_cache = ();
    foreach my $hostkey ( keys %{$el_hosts} ) {
	if ($shutdown_requested) {
	    log_shutdown();
	    return undef;
	}

	my $el_svcs = $el_hosts->{$hostkey}->{Service};
	my $cs_svcs = $cs_hosts->{$hostkey}->{Service};

	if ( defined $el_svcs ) {
	    foreach my $servicekey ( keys %{$el_svcs} ) {
		## if no service status change, then don't send
		my $service_change = undef;
		if ($smart_update) {
		    $service_change = serviceStatusChange( $el_svcs, $cs_svcs, $hostkey, $servicekey, $state_changes );
		    if ( not defined $service_change ) {
			return undef;
		    }
		    if ( !%$service_change ) {
			$skipcount++;
			next;
		    }
		}

		# Changes recorded in this routine are not specifically flagged as being for a host service.
		# It is up to the calling routine to handle them that way.
		my %change = (
		    appType => 'NAGIOS',

		    # default identification -- set to IP address if known
		    monitorServer => $thisnagios,

		    # default identification -- set to IP address if known
		    hostName => $hostkey,

		    # FIX MAJOR:  The REST API used "description" for the service name, in the 2014-04-01 build.
		    # This supposedly changed in the next build to be "service".  Verify whether that happened,
		    # and change this code if necessary.
		    description => $servicekey
		);

		# Monarch Sync now sets the IP as Identification. We should use address field from Monarch, whatever that is.
		# It's possible that the address changed, or that we are feeding a result for a host that
		# was not in Monarch when this program started. If the Identification is blank, reload the cache.
		if ( $hostipaddress{$hostkey} ) {
		    ## Set Device to IP
		    ## FIX MAJOR:  This should probably really be the "device" field, as documented, but the
		    ## service REST API is only accepting "deviceIdentification" right now.  Get this reviewed.
		    $change{deviceIdentification} = $hostipaddress{$hostkey};
		}
		else {
		    ## For some reason we don't know the IP. Might be a new host? Anyway, reload and try one time more.
		    load_cached_addresses() or return undef;
		    ## Set Device = IP, or bail out and set Device = hostname.  In the latter case,
		    ## there is something wrong with the local Monarch DB.
		    ## FIX MAJOR:  This should probably really be the "device" field, as documented, but the
		    ## service REST API is only accepting "deviceIdentification" right now.  Get this reviewed.
		    $change{deviceIdentification} = $hostipaddress{$hostkey} || $hostkey;
		}
		if ($smart_update) {
		    ## Logically, we just want:
		    ##     @change{ keys %$service_change } = values %$service_change;
		    ## But in fact, we need to convert the key strings we use internal to this program (until someday
		    ## when we drop all support for the XML API, and use the REST API strings natively here instead),
		    ## and we also need to map certain keys into a subsidiary "properties" hash.
		    my $key;
		    my $value;
		    while ( ( $key, $value ) = each %$service_change ) {
			if ( not $rest_time_field{$key} or not is_null_internal_time($value) ) {
			    if ( $serv_prop_attr{$key} ) {
				$change{properties}{$key} = $value;
			    }
			    else {
				$change{ $serv_rest_attr{$key} || $key } = $value;
			    }
			}
		    }
		}
		else {
		    my $el_svc = $el_svcs->{$servicekey};
		    my $prefer_hard_data = $ignore_soft_states && $el_svc->{StateType} eq 'SOFT';
		    foreach my $field ( keys %{$el_svc} ) {
			## FIX MAJOR:  Do we need to perform any recoding of either the key (e.g., to recognize
			## whether it belongs under a {properties} sub-hash, or needs some sort of capitalization
			## change), or the value (e.g., to attempt to alter quoting, although perhaps using
			## &quot; instead of changing the meaning of the data), or logic to suppress this entire
			## assignment (e.g., if this is a null internal timestamp)?  THIS NEEDS TESTING.
			$change{$field} = $el_svc->{ $prefer_hard_data && $mandatory_service_data_change{$field} || $field };
		    }
		}

		push @output, \%change;
		if ($smart_update) {
		    if ($continue_after_blockage) {
			## Defer cache updates until we know whether the data made it to Foundation.
			push @{ $host_services_to_cache{$hostkey} }, $servicekey;
		    }
		    else {
			## Execute cache updates right away, since there's no good reason to wait.
			serviceStatusUpdate( $element_ref, $collage_ref, $hostkey, $servicekey );
		    }
		}
		$insertcount++;
		if ( ( $insertcount % 100 ) == 0 ) {
		    log_message "Queueing services for insert, count=$insertcount" if $debug_basic;
		}
	    }
	}
    }
    if ($smart_update) {
	log_timed_message "Total Services Queued for Insert Count=$insertcount. No status change for $skipcount services." if $debug_summary;
    }
    else {
	log_timed_message "Total Services Queued for Insert Count=$insertcount." if $debug_summary;
    }
    return \@output;
}

sub build_host_xml {
    my $thisnagios      = shift;
    my $element_ref     = shift;
    my $collage_ref     = shift;
    my $state_changes   = shift;  # arrayref or undef
    my $insertcount     = 0;
    my $skipcount       = 0;
    my @output          = ();
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
	    ## Set Device = IP, or bail out and set Device = hostname.  In the latter case,
	    ## there is something wrong with the local Monarch DB.
	    push @xml_message, $hostipaddress{$hostkey} ? "Device=\"$hostipaddress{$hostkey}\" " : "Device=\"$hostkey\" ";
	}
	if ($smart_update) {
	    push @xml_message, $host_xml;
	}
	else {
	    my $el_host = $el_hosts->{$hostkey};
	    my $prefer_hard_data = $ignore_soft_states && $el_host->{StateType} eq 'SOFT';
	    foreach my $field ( keys %{$el_host} ) {
		next if $field eq 'Service';    # skip the Service hash key
		## FIX MAJOR:  We should probably escape " characters as &quot; (for purposes of inserting this data
		## into the XML stream) instead of changing the type of quotes.  For that matter, we should probably
		## apply proper XMLification, here or earlier.  Whatever we do in this regard, it needs to be tested.
		( my $tmpinfo = $el_host->{ $prefer_hard_data && $mandatory_host_data_change{$field} || $field } ) =~ s/"/'/g;
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
	    log_message "Queueing hosts for insert, count=$insertcount" if $debug_basic;
	}
    }
    if ($smart_update) {
	log_timed_message "Total Hosts Queued for Insert Count=$insertcount. No status change for $skipcount hosts." if $debug_summary;
    }
    else {
	log_timed_message "Total Hosts Queued for Insert Count=$insertcount." if $debug_summary;
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

	if ( defined $el_svcs ) {
	    foreach my $servicekey ( keys %{$el_svcs} ) {
		## if no service status change, then don't send
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
		    ## Set Device = IP, or bail out and set Device = hostname.  In the latter case,
		    ## there is something wrong with the local Monarch DB.
		    push @xml_message, $hostipaddress{$hostkey} ? "Device=\"$hostipaddress{$hostkey}\" " : "Device=\"$hostkey\" ";
		}
		push @xml_message, "ServiceDescription=\"$servicekey\" ";
		if ($smart_update) {
		    push @xml_message, $service_xml;
		}
		else {
		    my $el_svc = $el_svcs->{$servicekey};
		    my $prefer_hard_data = $ignore_soft_states && $el_svc->{StateType} eq 'SOFT';
		    foreach my $field ( keys %{$el_svc} ) {
			## FIX MAJOR:  We should probably escape " characters as &quot; (for purposes of inserting this data
			## into the XML stream) instead of changing the type of quotes.  For that matter, we should probably
			## apply proper XMLification, here or earlier.  Whatever we do in this regard, it needs to be tested.
			( my $tmpinfo = $el_svc->{ $prefer_hard_data && $mandatory_service_data_change{$field} || $field } ) =~ s/"/'/g;
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
		    log_message "Queueing services for insert, count=$insertcount" if $debug_basic;
		}
	    }
	}
    }
    if ($smart_update) {
	log_timed_message "Total Services Queued for Insert Count=$insertcount. No status change for $skipcount services." if $debug_summary;
    }
    else {
	log_timed_message "Total Services Queued for Insert Count=$insertcount." if $debug_summary;
    }
    return \@output;
}

# Queue up host state changes for sending to a remote Nagios instance.
sub push_host_state_change {
    my $host          = shift;
    my $el_host       = shift;
    my $state_changes = shift;

    my $el_status = $el_host->{MonitorStatus};
    ## FIX MINOR:  Check for DOWN (1 ?) vs. UNKNOWN (2 ?)?  Just guessing out of ignorance here.
    if ( defined($el_status) && $el_status !~ /PENDING/ ) {
	my $el_plugin_out = $el_host->{LastPluginOutput} // '';
	if ( !$ignore_stale_status || $el_plugin_out !~ m/stale/i ) {
	    my $last_check = $send_actual_check_timestamps ? $el_host->{last_check} . "\t" : '';
	    my $check_state = ( $el_status =~ /UP/ ) ? 0 : 1;
	    ## Reverse the XML Substitution needed for Foundation in the status text.
	    ## FIX MAJOR:   Why bother reverting?  Just keep the original string around unmodified,
	    ## though then be sure not to send the extra copy where it's not wanted.
	    ## FIX MAJOR NOW:  Was such XMLification even previously applied, if we're using REST to send to Foundation?
	    my $host_text = xml2text($el_plugin_out);
	    ## $host_text =~ s/&gt;/>/g;
	    ## $host_text =~ s/&lt;/</g;
	    ## $host_text =~ s/&apos;/'/g;
	    ## $host_text =~ s/&quot;/"/g;
	    ## $host_text =~ s/&amp;/&/g;
	    push @$state_changes, "$last_check$host\t$check_state\t$host_text|\n";
	}
    }
}

# Queue up service state changes for sending to a remote Nagios instance.
sub push_service_state_change {
    my $host          = shift;
    my $service       = shift;
    my $el_svc        = shift;
    my $state_changes = shift;

    my $el_status = $el_svc->{MonitorStatus};
    if ( defined($el_status) && $el_status !~ /PENDING/ ) {
	my $el_plugin_out = $el_svc->{LastPluginOutput} // '';
	if ( !$ignore_stale_status || $el_plugin_out !~ m/stale/i ) {
	    my $last_check = $send_actual_check_timestamps ? $el_svc->{last_check} . "\t" : '';
	    my $check_state = ( $el_status =~ /OK/ ) ? 0 : ( $el_status =~ /WARNING/ ) ? 1 : ( $el_status =~ /CRITICAL/ ) ? 2 : 3;
	    ## Reverse the XML Substitution needed for Foundation in the status text.
	    ## FIX MAJOR:   Why bother reverting?  Just keep the original string around unmodified,
	    ## though then be sure not to send the extra copy where it's not wanted.
	    ## FIX MAJOR NOW:  Was such XMLification even previously applied, if we're using REST to send to Foundation?
	    my $service_text = xml2text($el_plugin_out);
	    ## $service_text =~ s/&gt;/>/g;
	    ## $service_text =~ s/&lt;/</g;
	    ## $service_text =~ s/&apos;/'/g;
	    ## $service_text =~ s/&quot;/"/g;
	    ## $service_text =~ s/&amp;/&/g;
	    push @$state_changes, "$last_check$host\t$service\t$check_state\t$service_text|\n";
	}
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
    my $timing_change = 0;
    my $have_cache    = 0;
    my $remote_change = 0;
    my $rm_host       = undef;
    my $rm_field      = undef;
    my $el_host_field;
    my $cs_host_field;

    my $prefer_hard_data = $ignore_soft_states && $el_host->{StateType} eq 'SOFT';

    # We always need these fields if we send any XML (GWMON-7684) ...
    foreach my $field ( keys %mandatory_host_data_change ) {
	$el_host_field = $el_host->{ $prefer_hard_data ? $mandatory_host_data_change{$field} : $field };
	$cs_host_field = $cs_host->{$field};
	$el_host_field = '' if not defined $el_host_field;
	$cs_host_field = '' if not defined $cs_host_field;
	## FIX MAJOR:  We should probably escape " characters as &quot; (for purposes of inserting this data
	## into the XML stream) instead of changing the type of quotes.  For that matter, we should probably
	## apply proper XMLification, here or earlier.  Whatever we do in this regard, it needs to be tested.
	( my $tmpinfo = $el_host_field ) =~ s/"/'/g;
	push @host_xml, "$field=\"$tmpinfo\" ";
	if ( $el_host_field ne $cs_host_field ) {
	    $data_change = 1;
	}
    }
    ## Check each condition that might require an update to the database status.
    foreach my $field (@host_data_change) {
	$el_host_field = $el_host->{$field}; $el_host_field = '' if not defined $el_host_field;
	$cs_host_field = $cs_host->{$field}; $cs_host_field = '' if not defined $cs_host_field;
	if ( $el_host_field ne $cs_host_field ) {
	    ## FIX MAJOR:  We should probably escape " characters as &quot; (for purposes of inserting this data
	    ## into the XML stream) instead of changing the type of quotes.  For that matter, we should probably
	    ## apply proper XMLification, here or earlier.  Whatever we do in this regard, it needs to be tested.
	    (my $tmpinfo = $el_host_field) =~ s/"/'/g;
	    push @host_xml, "$field=\"$tmpinfo\" ";
	    $data_change = 1;
	}
    }
    ## Check each condition that might require an update to the timing change fields
    ## (sync only on heartbeat, or if other data has changed).
    log_message "Checking host $hostkey" if $debug_debug;
    foreach my $field (@host_timing_change) {
	$el_host_field = $el_host->{$field}; $el_host_field = '' if not defined $el_host_field;
	$cs_host_field = $cs_host->{$field}; $cs_host_field = '' if not defined $cs_host_field;
	if ( $el_host_field ne $cs_host_field ) {
	    ## FIX MAJOR:  We should probably escape " characters as &quot; (for purposes of inserting this data
	    ## into the XML stream) instead of changing the type of quotes.  For that matter, we should probably
	    ## apply proper XMLification, here or earlier.  Whatever we do in this regard, it needs to be tested.
	    (my $tmpinfo = $el_host_field) =~ s/"/'/g;
	    push @host_xml, "$field=\"$tmpinfo\" ";
	    $timing_change = 1;
	    if ( defined( $rm_host = $remote_hosts->{$hostkey} ) ) {
		$have_cache = 1;
		if ( defined( $rm_field = $rm_host->{$field} ) and $el_host_field ne $rm_field ) {
		    log_message "Remote cache update for host $hostkey field $field: \"$rm_field\" => \"$el_host_field\"." if $debug_debug;
		    $rm_host->{$field} = $el_host_field;
		    $remote_change = 1;
		}
	    }
	    log_message "Found timing change for host $hostkey field $field: \"$cs_host_field\" => \"$el_host_field\"." if $debug_debug;
	}
    }

    if ($data_change) {
	## Check for "Pending Transition", so we can send an event and trigger a state change
	## when we go from PENDING to UP.
	my $el_status = $el_host->{ $prefer_hard_data ? 'LastHardState' : 'MonitorStatus' };
	my $cs_status = $cs_host->{MonitorStatus};
	if ( not defined $el_status ) {
	    log_message "WARNING:  Host $hostkey has no "
	      . ( $prefer_hard_data ? 'LastHardState' : 'MonitorStatus' )
	      . ' in Nagios.'
	      if $debug_summary;
	}
	elsif ( not defined $cs_status ) {
	    log_message "WARNING:  Host $hostkey has no MonitorStatus in Foundation." if $debug_summary;
	}
	elsif ( ( $el_status eq 'UP' ) and ( $cs_status eq 'PENDING' ) ) {
	    my $queueing_status = queue_pending_host_event( $el_host, $hostkey );
	    return $restart_xml if $queueing_status != CONTINUE_STATUS;
	}
	log_message "Found data change for host $hostkey" if $debug_basic;
	log_message( Data::Dumper->Dump( [ \%{$cs_host} ], [qw(\%{cs_hosts})] ) ) if $debug_trace;
	log_message( Data::Dumper->Dump( [ \%{$el_host} ], [qw(\%{el_hosts})] ) ) if $debug_trace;
	push_host_state_change( $hostkey, $el_host, $state_changes ) if defined $state_changes;
	return join( '', @host_xml );
    }
    elsif ($timing_change) {
	## See the corresponding code in serviceStatusChangeXML() for a detailed analysis.
	if ( ($heartbeat_mode or $el_host->{StateType} eq 'SOFT') and not ($have_cache xor $remote_change) ) {
	    log_message "Queuing remote timing-only change for host $hostkey" if $debug_debug;
	    push_host_state_change( $hostkey, $el_host, $state_changes ) if defined $state_changes;
	    ## If we're in local heartbeat mode (i.e., if we return @host_xml data from this routine, just below), the remote
	    ## cache will be destroyed before we use it again, so there's no point in stuffing the data there in that case.
	    if (!$heartbeat_mode) {
		@{ $remote_hosts->{$hostkey} }{@host_timing_change} = @$el_host{@host_timing_change};
		if ($debug_debug) {
		    log_message "=== remote_hosts fields:";
		    foreach my $key ( keys %{ $remote_hosts->{$hostkey} } ) {
			log_message "=== $key => \"$remote_hosts->{$hostkey}->{$key}\"";
		    }
		}
	    }
	}
	if ($heartbeat_mode) {
	    log_message "Accepting local heartbeat change for host $hostkey" if $debug_basic;
	    return join( '', @host_xml );
	}
	else {
	    log_message "Rejecting local change since it's just a timing update and we are not doing a heartbeat: $hostkey" if $debug_basic;
	    return $no_xml;
	}
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
    my $timing_change = 0;
    my $have_cache    = 0;
    my $remote_change = 0;
    my $rm_host       = undef;
    my $rm_svc        = undef;
    my $rm_field      = undef;
    my $el_svc_field;
    my $cs_svc_field;

    my $prefer_hard_data = $ignore_soft_states && $el_svc->{StateType} eq 'SOFT';

    # We always need these fields if we send any XML (GWMON-7684) ...
    foreach my $field ( keys %mandatory_service_data_change ) {
	$el_svc_field = $el_svc->{ $prefer_hard_data ? $mandatory_service_data_change{$field} : $field };
	$cs_svc_field = $cs_svc->{$field};
	$el_svc_field = '' if not defined $el_svc_field;
	$cs_svc_field = '' if not defined $cs_svc_field;
	## FIX MAJOR:  We should probably escape " characters as &quot; (for purposes of inserting this data
	## into the XML stream) instead of changing the type of quotes.  For that matter, we should probably
	## apply proper XMLification, here or earlier.  Whatever we do in this regard, it needs to be tested.
	(my $tmpinfo = $el_svc_field) =~ s/"/'/g;
	push @service_xml, "$field=\"$tmpinfo\" ";
	# but don't miss a change to these ...
	if ( $el_svc_field ne $cs_svc_field ) {
	    $data_change = 1;
	}
    }
    ## Check each condition that might require an update to the database status.
    foreach my $field (@service_data_change) {
	## We sequence the $el_svc_field definition tests here so the first one fails most often,
	## given that the usual setting of $ignore_soft_states will be true.
	$el_svc_field = $el_svc->{$field}; $el_svc_field = '' if not defined $el_svc_field;
	$cs_svc_field = $cs_svc->{$field}; $cs_svc_field = '' if not defined $cs_svc_field;
	if ( $el_svc_field ne $cs_svc_field ) {
	    ## FIX MAJOR:  We should probably escape " characters as &quot; (for purposes of inserting this data
	    ## into the XML stream) instead of changing the type of quotes.  For that matter, we should probably
	    ## apply proper XMLification, here or earlier.  Whatever we do in this regard, it needs to be tested.
	    (my $tmpinfo = $el_svc_field) =~ s/"/'/g;
	    push @service_xml, "$field=\"$tmpinfo\" ";
	    $data_change = 1;
	}
    }
    ## Check fields that constitute a timing update (sync only on heartbeat, or if other data has changed).
    log_message "Checking host $hostkey service $servicekey" if $debug_debug;
    foreach my $field (@service_timing_change) {
	$el_svc_field = $el_svc->{$field}; $el_svc_field = '' if not defined $el_svc_field;
	$cs_svc_field = $cs_svc->{$field}; $cs_svc_field = '' if not defined $cs_svc_field;
	if ( $el_svc_field ne $cs_svc_field ) {
	    ## FIX MAJOR:  We should probably escape " characters as &quot; (for purposes of inserting this data
	    ## into the XML stream) instead of changing the type of quotes.  For that matter, we should probably
	    ## apply proper XMLification, here or earlier.  Whatever we do in this regard, it needs to be tested.
	    (my $tmpinfo = $el_svc_field) =~ s/"/'/g;
	    push @service_xml, "$field=\"$tmpinfo\" ";
	    $timing_change = 1;
	    if ( defined( $rm_host = $remote_svcs->{$hostkey} ) and defined( $rm_svc = $rm_host->{$servicekey} ) ) {
		$have_cache = 1;
		if ( defined( $rm_field = $rm_svc->{$field} ) and $el_svc_field ne $rm_field ) {
		    log_message "Remote cache update for host $hostkey service $servicekey field $field: \"$rm_field\" => \"$el_svc_field\"."
		      if $debug_debug;
		    $rm_svc->{$field} = $el_svc_field;
		    $remote_change = 1;
		}
	    }
	    log_message "Found timing change for host $hostkey service $servicekey field $field: \"$cs_svc_field\" => \"$el_svc_field\"."
	      if $debug_debug;
	}
    }

    if ($data_change) {
	## Check for "Pending Transition", so we can send an event and trigger a state change
	## when we go from Pending to OK.
	my $el_status = $el_svc->{ $prefer_hard_data ? 'LastHardState' : 'MonitorStatus' };
	my $cs_status = $cs_svc->{MonitorStatus};
	if ( not defined $el_status ) {
	    log_message "WARNING:  Host $hostkey service $servicekey has no "
	      . ( $prefer_hard_data ? 'LastHardState' : 'MonitorStatus' )
	      . ' in Nagios.'
	      if $debug_summary;
	}
	elsif ( not defined $cs_status ) {
	    log_message "WARNING:  Host $hostkey service $servicekey has no MonitorStatus in Foundation." if $debug_summary;
	}
	elsif ( ( $el_status eq 'OK' ) and ( $cs_status eq 'PENDING' ) ) {
	    my $queueing_status = queue_pending_svc_event( $el_svc, $hostkey, $servicekey );
	    return $restart_xml if $queueing_status != CONTINUE_STATUS;
	}
	log_message "Found data change for host $hostkey service $servicekey" if $debug_basic;
	log_message( Data::Dumper->Dump( [ \%{$cs_svc} ], [qw(\%{cs_svcs})] ) ) if $debug_trace;
	log_message( Data::Dumper->Dump( [ \%{$el_svc} ], [qw(\%{el_svcs})] ) ) if $debug_trace;
	push_service_state_change( $hostkey, $servicekey, $el_svc, $state_changes ) if defined $state_changes;
	return join( '', @service_xml );
    }
    elsif ($timing_change) {
	## This next if() represents our decision making on whether or not to queue timing changes to Nagios on
	## remote systems, independently of whether they are sent to Foundation on the local system.  We may push
	## service state timing-only changes to remote servers even if we are not in heartbeat mode, so the parent
	## Nagios has a chance to clock its SOFT-to-HARD state machine in synchrony with the child Nagios.  But
	## for that to happen, we need to only send SOFT-state timing changes to remote systems when they actually
	## occur, and to send all such changes, which may differ from when they are sent to the local Foundation.
	## $timing_change==1 means:  I have a locally unsent delta in timing data.  It may or may not have already
	##    been queued to remote systems, and it therefore may or may not represent something that we need to
	##    queue now for forwarding to the remote systems.  For that, we need to look at the remote cache (namely,
	##    the derived $have_cache and $remote_change variables) for this service.
	## $have_cache==0 means:  This is a new timing change, never before queued to the remote systems, so we
	##    have not previously created a remote cache for stuff previously queued to the remote systems that
	##    might differ from what has been sent to the local system, at least since the last time the local
	##    and remote states were synchronized.
	## $have_cache==1 means:  Local changes have been pending for at least one processing cycle, and some of
	##    those changes were queued for sending to remote systems and saved in the remote cache for future
	##    comparison.  So now the base for computing remote changes must be the remote cache instead of what
	##    we see as the base for stuff to be sent to the local system.
	## $remote_change==0 means:  Either no remote cache exists (the $have_cache==0 case), or no further changes
	##    have been received since changes were last queued to remote systems (the $have_cache==1 case).
	## $remote_change==1 means:  There has been a new timing change since the last time we queued data to
	##    remote systems.
	## With those definitions in mind, here is the case-by-case analysis for how to make our queue (forward to
	## remote systems) and save (write to remote cache) decisions.  Bear in mind that a local heartbeat will
	## soon flush the remote cache (thereby synchronizing it with the local state), so in those cases there's
	## no point in saving to the cache.

	# cached? rchg? soft? heart? | queue? save? notes
	# ---------------------------+-----------------------------------------------------------------------------
	# N       N     N     N      | N      N     unimportant new timing change; don't queue to remote systems or cache
	# N       N     N     Y      | Y      N     heartbeat forces timing change to be recognized; queue, but don't bother caching
	# N       N     Y     N      | Y      Y     queue new soft state change to clock remote logic; write to cache
	# N       N     Y     Y      | Y      N     queue new soft state change to clock remote logic; don't cache
	# N       Y     N     N      | -      -     impossible case (cache changed, but no cache exists)
	# N       Y     N     Y      | -      -     impossible case (cache changed, but no cache exists)
	# N       Y     Y     N      | -      -     impossible case (cache changed, but no cache exists)
	# N       Y     Y     Y      | -      -     impossible case (cache changed, but no cache exists)
	# Y       N     N     N      | N      N     don't queue or cache (same data was queued previously, and cache is up-to-date)
	# Y       N     N     Y      | N      N     don't queue or cache (same data was queued previously, so ignore heartbeat now)
	# Y       N     Y     N      | N      N     same soft state was previously queued/cached, so don't queue/cache now
	# Y       N     Y     Y      | N      N     same soft state was previously queued/cached, so don't queue/cache now
	# Y       Y     N     N      | N      N     unimportant new remote timing change; don't queue or update cache
	# Y       Y     N     Y      | Y      N     queue new remote timing change as heartbeat; don't bother with cache
	# Y       Y     Y     N      | Y      Y     queue new soft state change to clock remote logic; write to cache
	# Y       Y     Y     Y      | Y      N     queue new soft state change to clock remote logic; don't bother with cache

	if ( ($heartbeat_mode or $el_svc->{StateType} eq 'SOFT') and not ($have_cache xor $remote_change) ) {
	    log_message "Queuing remote timing-only change for host $hostkey service $servicekey" if $debug_debug;
	    push_service_state_change( $hostkey, $servicekey, $el_svc, $state_changes ) if defined $state_changes;
	    ## If we're in local heartbeat mode (i.e., if we return @service_xml data from this routine, just below), the remote
	    ## cache will be destroyed before we use it again, so there's no point in stuffing the data there in that case.
	    if (!$heartbeat_mode) {
		@{ $remote_svcs->{$hostkey}->{$servicekey} }{@service_timing_change} = @$el_svc{@service_timing_change};
		if ($debug_debug) {
		    log_message "=== remote_svcs fields:";
		    foreach my $key ( keys %{ $remote_svcs->{$hostkey}->{$servicekey} } ) {
			log_message "=== $key => \"$remote_svcs->{$hostkey}->{$servicekey}->{$key}\"";
		    }
		}
	    }
	}
	if ($heartbeat_mode)  {
	    log_message "Accepting local heartbeat change for host $hostkey service $servicekey" if $debug_basic;
	    return join( '', @service_xml );
	}
	else {
	    log_message "Rejecting local change since it's just a timing update and we are not doing a heartbeat: $hostkey $servicekey" if $debug_basic;
	    return $no_xml;
	}
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
    if ( not $send_events_for_pending_to_ok ) {
	return CONTINUE_STATUS;
    }

    if ($use_rest_api) {
	## FIX MAJOR:  Is this the proper treatment of the plugin output?
	## Test end-to-end, including its appearance in Status Viewer.

	## FIX MAJOR:  Verify that the properties construction here works as intended.
	## At a minimum, ensure that the timestamp is formatted in a manner that is acceptable to the REST API.
	## FIX MAJOR:  This event is missing the consolidationName field.  Does that matter?
	push @rest_event_messages,
	  {
	    appType        => 'NAGIOS',
	    monitorServer  => $thisnagios,
	    host           => $hostkey,
	    device         => $hostipaddress{$hostkey} || $hostkey,
	    severity       => 'OK',
	    monitorStatus  => 'UP',
	    textMessage    => nolinebreaks( $el_host->{LastPluginOutput} ),
	    reportDate     => unix_to_rest_time(time),
	    lastInsertDate => $el_host->{LastCheckTime},
	    properties     => { SubComponent => $hostkey, ErrorType => 'HOST ALERT' }
	  };
    }
    else {
	my @xml_message = ();
	push @xml_message, '<LogMessage ';

	# default identification -- should set to IP address if known
	push @xml_message, "MonitorServerName=\"$thisnagios\" ";
	push @xml_message, "Host=\"$hostkey\" ";
	## if have IP address, use it; else set to host name
	push @xml_message, $hostipaddress{$hostkey} ? "Device=\"$hostipaddress{$hostkey}\" " : "Device=\"$hostkey\" ";
	push @xml_message, 'Severity="OK" ';
	push @xml_message, 'MonitorStatus="UP" ';
	my $tmp = $el_host->{LastPluginOutput};
	$tmp = encode_text($tmp);
	## $tmp =~ s/\n/ /g;
	## $tmp =~ s/<br>/ /ig;
	## $tmp =~ s/&/&amp;/g;
	## $tmp =~ s/"/&quot;/g;
	## $tmp =~ s/'/&apos;/g;
	## $tmp =~ s/</&lt;/g;
	## $tmp =~ s/>/&gt;/g;
	push @xml_message, "TextMessage=\"$tmp\" ";
	$tmp = xml_time_text(time);
	push @xml_message, "ReportDate=\"$tmp\" ";
	push @xml_message, "LastInsertDate=\"$el_host->{LastCheckTime}\" ";
	push @xml_message, "SubComponent=\"$hostkey\" ";
	push @xml_message, 'ErrorType="HOST ALERT" ';
	push @xml_message, '/>';

	my $xml_message = join( '', @xml_message );

	log_message "Pending Transition Host Event:\n$xml_message" if $debug_xml;

	push @xml_event_messages, $xml_message;
    }

    # FIX MAJOR:  should $max_event_bundle_size apply to both REST and XML messages?
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
    if ( not $send_events_for_pending_to_ok ) {
	return CONTINUE_STATUS;
    }

    my $prefer_hard_data = $ignore_soft_states && $el_svc->{StateType} eq 'SOFT';
    my $MonitorStatus = $el_svc->{ $prefer_hard_data ? 'LastHardState' : 'MonitorStatus' };

    if ($use_rest_api) {
	## FIX MAJOR:  Is this the proper treatment of the plugin output?
	## Test end-to-end, including its appearance in Status Viewer.

	## FIX MAJOR:  Verify that the properties construction here works as intended.
	## At a minimum, ensure that the timestamp is formatted in a manner that is acceptable to the REST API.
	## FIX MAJOR:  This event is missing the consolidationName field.  Does that matter?
	my %one_event = (
	    appType        => 'NAGIOS',
	    monitorServer  => $thisnagios,
	    host           => $hostkey,
	    device         => $hostipaddress{$hostkey} || $hostkey,
	    service        => $servicekey,
	    severity       => $MonitorStatus,
	    monitorStatus  => $MonitorStatus,
	    textMessage    => nolinebreaks( $el_svc->{LastPluginOutput} ),
	    reportDate     => unix_to_rest_time(time),
	    lastInsertDate => $el_svc->{LastCheckTime},
	    properties     => { SubComponent => "$hostkey:$servicekey", ErrorType => 'SERVICE ALERT' }
	);

	# Handle possible attribute exceptions.
	foreach my $pattern ( keys %mapped_services ) {
	    if ( $servicekey =~ m{^$pattern$} ) {
		$one_event{appType} = $mapped_services{$pattern}{application_type} if defined $mapped_services{$pattern}{application_type};
		if ( defined $mapped_services{$pattern}{consolidation_criteria} ) {
		    $one_event{consolidationName} = $mapped_services{$pattern}{consolidation_criteria};
		    ## Historically, we don't include an IP address for Nagios-related events.
		    ## But if attribute mapping is in play for this service, it might refer to some
		    ## consolidation criteria that in turn references this field.  So in that case,
		    ## we supply such information if we have it available.
		    $one_event{properties}{ipaddress} = $hostipaddress{$hostkey} if defined $hostipaddress{$hostkey};
		}
		last;
	    }
	}

	push @rest_event_messages, \%one_event;
    }
    else {
	my @xml_message = ();
	push @xml_message, '<LogMessage ';

	# default identification -- should set to IP address if known
	push @xml_message, "MonitorServerName=\"$thisnagios\" ";
	push @xml_message, "Host=\"$hostkey\" ";
	## if have IP address, use it; else set to host name
	push @xml_message, $hostipaddress{$hostkey} ? "Device=\"$hostipaddress{$hostkey}\" " : "Device=\"$hostkey\" ";
	push @xml_message, "ServiceDescription=\"$servicekey\" ";
	push @xml_message, "Severity=\"$MonitorStatus\" ";
	push @xml_message, "MonitorStatus=\"$MonitorStatus\" ";
	my $tmp = $el_svc->{LastPluginOutput};
	$tmp = encode_text($tmp);
	## $tmp =~ s/\n/ /g;
	## $tmp =~ s/<br>/ /ig;
	## $tmp =~ s/&/&amp;/g;
	## $tmp =~ s/"/&quot;/g;
	## $tmp =~ s/'/&apos;/g;
	## $tmp =~ s/</&lt;/g;
	## $tmp =~ s/>/&gt;/g;
	push @xml_message, "TextMessage=\"$tmp\" ";
	$tmp = xml_time_text(time);
	push @xml_message, "ReportDate=\"$tmp\" ";
	push @xml_message, "LastInsertDate=\"$el_svc->{LastCheckTime}\" ";
	push @xml_message, "SubComponent=\"$hostkey:$servicekey\" ";
	push @xml_message, 'ErrorType="SERVICE ALERT" ';
	push @xml_message, '/>';

	my $xml_message = join( '', @xml_message );

	log_message "Pending Transition Service Event:\n$xml_message" if $debug_xml;

	push @xml_event_messages, $xml_message;
    }

    # FIX MAJOR:  should $max_event_bundle_size apply to both REST and XML messages?
    $message_counter = send_pending_events( $message_counter, $max_event_bundle_size );
    return ($message_counter < 0) ? RESTART_STATUS : CONTINUE_STATUS;
}

sub hostStatusUpdate {
    my $element_ref = shift;
    my $collage_ref = shift;
    my $hostkey     = shift;
    my $el_host     = $element_ref->{Host}->{$hostkey};
    my $cs_host     = \%{ $collage_ref->{Host}->{$hostkey} };

    # In addition to implementing SOFT-state suppression here, we use this same flag
    # to maintain $cs_host->{MonitorStatus} as PENDING regardless of the setting of
    # $ignore_soft_states, until we see the first HARD state driven by an actual check
    # result.  That allows us to reliably send the necessary PENDING-to-UP transition
    # event from the status feeder even if some SOFT states intervene in the middle.
    # Not seeing the initial SOFT states on the Foundation side is considered to be a
    # small price to pay for this.
    my $prefer_hard_data = ( $ignore_soft_states && $el_host->{StateType} eq 'SOFT' ) || $el_host->{LastHardState} eq 'PENDING';

    # We want the effect of:
    # $cs_host = $el_host;
    # except that we really would need to copy the hashes, not symlinks to them.
    # FIX LATER:  It might be more efficient to copy using hash slices, similar to what we do for optional fields.

    # We only need to save items with keys we might send to Foundation, and
    # therefore would need to later compare as such with equivalent data from
    # Nagios.  So we don't save LastHardState and LastHardStateChange here,
    # because we don't currently send either of them to Foundation.
    $cs_host->{Comments}                  = $el_host->{Comments};
    $cs_host->{CurrentAttempt}            = $el_host->{CurrentAttempt};
    $cs_host->{CurrentNotificationNumber} = $el_host->{CurrentNotificationNumber};
    $cs_host->{LastNotificationTime}      = $el_host->{LastNotificationTime};
    $cs_host->{ExecutionTime}             = $el_host->{ExecutionTime};
    $cs_host->{last_check}                = $el_host->{last_check} if $send_actual_check_timestamps;
    $cs_host->{LastCheckTime}             = $el_host->{LastCheckTime};
    $cs_host->{Latency}                   = $el_host->{Latency};
    $cs_host->{MaxAttempts}               = $el_host->{MaxAttempts};
    $cs_host->{MonitorStatus}             = $el_host->{ $prefer_hard_data ? 'LastHardState' : 'MonitorStatus' };
    $cs_host->{NextCheckTime}             = $el_host->{NextCheckTime};
    $cs_host->{ScheduledDowntimeDepth}    = $el_host->{ScheduledDowntimeDepth};
    $cs_host->{StateType}                 = $el_host->{StateType};
    $cs_host->{isAcknowledged}            = $el_host->{isAcknowledged};
    $cs_host->{isChecksEnabled}           = $el_host->{isChecksEnabled};
    $cs_host->{isEventHandlersEnabled}    = $el_host->{isEventHandlersEnabled};
    $cs_host->{isFlapDetectionEnabled}    = $el_host->{isFlapDetectionEnabled};
    $cs_host->{isNotificationsEnabled}    = $el_host->{isNotificationsEnabled};
    $cs_host->{isPassiveChecksEnabled}    = $el_host->{isPassiveChecksEnabled};
    $cs_host->{LastPluginOutput}          = $el_host->{LastPluginOutput};
    $cs_host->{PercentStateChange}	  = $el_host->{PercentStateChange};
    $cs_host->{LastStateChange}           = $el_host->{ $prefer_hard_data ? 'LastHardStateChange' : 'LastStateChange' };
    $cs_host->{PerformanceData}           = $el_host->{PerformanceData};
    $cs_host->{CheckType}                 = $el_host->{CheckType};

    # Also capture any optional fields the user has configured.
    @$cs_host{@non_default_host_data_change}   = @$el_host{@non_default_host_data_change};
    @$cs_host{@non_default_host_timing_change} = @$el_host{@non_default_host_timing_change};

    delete $remote_hosts->{$hostkey};
    return;
}

sub serviceStatusUpdate {
    my $element_ref = shift;
    my $collage_ref = shift;
    my $hostkey     = shift;
    my $servicekey  = shift;
    my $el_svc      = $element_ref->{Host}->{$hostkey}->{Service}->{$servicekey};
    my $cs_svc      = \%{ $collage_ref->{Host}->{$hostkey}->{Service}->{$servicekey} };

    # In addition to implementing SOFT-state suppression here, we use this same flag
    # to maintain $cs_svc->{MonitorStatus} as PENDING regardless of the setting of
    # $ignore_soft_states, until we see the first HARD state driven by an actual check
    # result.  That allows us to reliably send the necessary PENDING-to-OK transition
    # event from the status feeder even if some SOFT states intervene in the middle.
    # Not seeing the initial SOFT states on the Foundation side is considered to be a
    # small price to pay for this.
    my $prefer_hard_data = ( $ignore_soft_states && $el_svc->{StateType} eq 'SOFT' ) || $el_svc->{LastHardState} eq 'PENDING';

    # We want the effect of:
    # $cs_svc = $el_svc;
    # except that we really would need to copy the hashes, not symlinks to them.
    # FIX LATER:  It might be more efficient to copy using hash slices, similar to what we do for optional fields.

    # We only need to save items with keys we might send to Foundation, and
    # therefore would need to later compare as such with equivalent data from
    # Nagios.  So we don't save LastHardStateChange here, because we don't
    # currently send it to Foundation.
    $cs_svc->{Comments}                  = $el_svc->{Comments};
    $cs_svc->{CurrentAttempt}            = $el_svc->{CurrentAttempt};
    $cs_svc->{CurrentNotificationNumber} = $el_svc->{CurrentNotificationNumber};
    $cs_svc->{LastHardState}             = $el_svc->{LastHardState};
    $cs_svc->{LastNotificationTime}      = $el_svc->{LastNotificationTime};
    $cs_svc->{last_check}                = $el_svc->{last_check} if $send_actual_check_timestamps;
    $cs_svc->{LastCheckTime}             = $el_svc->{LastCheckTime};
    $cs_svc->{MonitorStatus}             = $el_svc->{ $prefer_hard_data ? 'LastHardState' : 'MonitorStatus' };
    $cs_svc->{NextCheckTime}             = $el_svc->{NextCheckTime};
    $cs_svc->{ScheduledDowntimeDepth}    = $el_svc->{ScheduledDowntimeDepth};
    $cs_svc->{isAcceptPassiveChecks}     = $el_svc->{isAcceptPassiveChecks};
    $cs_svc->{isChecksEnabled}           = $el_svc->{isChecksEnabled};
    $cs_svc->{isEventHandlersEnabled}    = $el_svc->{isEventHandlersEnabled};
    $cs_svc->{isFlapDetectionEnabled}    = $el_svc->{isFlapDetectionEnabled};
    $cs_svc->{isNotificationsEnabled}    = $el_svc->{isNotificationsEnabled};
    $cs_svc->{isProblemAcknowledged}     = $el_svc->{isProblemAcknowledged};
    $cs_svc->{MaxAttempts}               = $el_svc->{MaxAttempts};
    $cs_svc->{LastPluginOutput}          = $el_svc->{LastPluginOutput};
    $cs_svc->{PercentStateChange}        = $el_svc->{PercentStateChange};
    $cs_svc->{Latency}                   = $el_svc->{Latency};
    $cs_svc->{ExecutionTime}             = $el_svc->{ExecutionTime};
    $cs_svc->{LastStateChange}           = $el_svc->{ $prefer_hard_data ? 'LastHardStateChange' : 'LastStateChange' };
    $cs_svc->{PerformanceData}           = $el_svc->{PerformanceData};
    $cs_svc->{StateType}                 = $el_svc->{StateType};
    $cs_svc->{CheckType}                 = $el_svc->{CheckType};

    # Also capture any optional fields the user has configured.
    @$cs_svc{@non_default_service_data_change}   = @$el_svc{@non_default_service_data_change};
    @$cs_svc{@non_default_service_timing_change} = @$el_svc{@non_default_service_timing_change};

    # Clean up the secondary cache reflecting what last got sent to remote hosts
    # that is different from what last got sent to local hosts.
    if ( defined $remote_svcs->{$hostkey} ) {
	delete $remote_svcs->{$hostkey}->{$servicekey};
	delete $remote_svcs->{$hostkey} if not %{ $remote_svcs->{$hostkey} };
    }
    return;
}

sub find_deltas {
    my $element_ref        = shift;
    my $collage_status_ref = shift;
    my $deltas             = {};
    my $el_hosts           = \%{ $element_ref->{Host} };
    my $cs_hosts           = \%{ $collage_status_ref->{Host} };

    foreach my $hostkey ( keys %$cs_hosts ) {
	my $el_host = $el_hosts->{$hostkey};
	if ( !defined $el_host ) {
	    $deltas->{FoundationHost}->{$hostkey} = 1;
	    next;
	}
	my $el_svcs = $el_host->{Service};
	foreach my $servicekey ( keys( %{ $cs_hosts->{$hostkey}->{Service} } ) ) {
	    if ( !defined $el_svcs->{$servicekey} ) {
		$deltas->{FoundationHost}->{$hostkey}->{Service}->{$servicekey} = 1;
	    }
	}
    }
    foreach my $hostkey ( keys %$el_hosts ) {
	my $cs_host = $cs_hosts->{$hostkey};
	if ( !defined $cs_host ) {
	    $deltas->{NagiosHost}->{$hostkey} = 1;
	    next;
	}
	my $cs_svcs = $cs_host->{Service};
	foreach my $servicekey ( keys( %{ $el_hosts->{$hostkey}->{Service} } ) ) {
	    if ( !defined $cs_svcs->{$servicekey} ) {
		$deltas->{NagiosHost}->{$hostkey}->{Service}->{$servicekey} = 1;
	    }
	}
    }
    return $deltas;
}

sub assemble_remote_full_dump {
    my $element_ref        = shift;
    my $collage_status_ref = shift;
    my @states             = ();
    my $cs_status          = undef;
    my $cs_plugin_out      = undef;
    my $check_state        = undef;
    my $last_check         = undef;
    my $el_hosts           = \%{ $element_ref->{Host} };
    my $cs_hosts           = \%{ $collage_status_ref->{Host} };
    my $el_host            = undef;
    my $cs_host            = undef;
    my $el_svcs            = undef;
    my $cs_svcs            = undef;
    my $cs_svc             = undef;
    my $host_text          = undef;
    my $service_text       = undef;

    $#states = $heartbeat_high_water_mark;    # pre-extend the array, for efficiency
    $#states = -1;                            # truncate the array, since we don't have any messages yet

    # We export data in the $collage_status_ref tree, though I'm not sure why that tree of data
    # was originally chosen over the $element_ref tree.  In any case, we drive the looping here
    # based on the hosts and services in the $element_ref tree in order to exclude any excess
    # data in the $collage_status_ref tree that we might have still in memory, left over from a
    # previous data draw from Foundation.  Such excess data might be hanging around if we have
    # had an unrecognized Nagios commit operation that only deleted hosts and/or services and
    # did not have any additions, so we have not yet found it necessary to restart this script.
    foreach my $host ( keys(%$el_hosts) ) {
	$el_host       = $el_hosts->{$host};
	$cs_host       = $cs_hosts->{$host};
	$cs_status     = $cs_host->{MonitorStatus};
	$cs_plugin_out = $cs_host->{LastPluginOutput};
	if ( $cs_status =~ /UP/ ) {
	    $check_state = 0;
	}
	elsif ( $cs_status =~ /PENDING/ ) {
	    next;
	}
	elsif ( $ignore_stale_status && $cs_plugin_out =~ m/stale/i ) {
	    ## Prevent auto upload of stale status host events on nagios restart.
	    log_message "Dropping $cs_host \"$cs_plugin_out\" message from full update.";
	    next;
	}
	else {
	    $check_state = 1;
	}
	$last_check = $send_actual_check_timestamps ? $cs_host->{last_check} . "\t" : '';
	## Reverse the XML Substitution needed for Foundation in the status text.
	## FIX MAJOR:   Why bother reverting?  Just keep the original string around unmodified,
	## though then be sure not to send the extra copy where it's not wanted.
	$host_text = xml2text($cs_plugin_out);
	## $host_text =~ s/&gt;/>/g;
	## $host_text =~ s/&lt;/</g;
	## $host_text =~ s/&apos;/'/g;
	## $host_text =~ s/&quot;/"/g;
	## $host_text =~ s/&amp;/&/g;
	push @states, "$last_check$host\t$check_state\t$host_text|\n";
	$el_svcs = $el_host->{Service};
	if ( defined $el_svcs ) {
	    $cs_svcs = \%{ $cs_host->{Service} };
	    foreach my $service ( keys(%$el_svcs) ) {
		$cs_svc        = $cs_svcs->{$service};
		$cs_status     = $cs_svc->{MonitorStatus};
		$cs_plugin_out = $cs_svc->{LastPluginOutput};
		if ( $cs_status =~ /PENDING/ ) {
		    next;
		}
		elsif ( $ignore_stale_status && $cs_plugin_out =~ m/stale/i ) {
		    ## Prevent auto upload of stale status service events on nagios restart.
		    log_message "Dropping $cs_host $cs_svc \"$cs_plugin_out\" message from full update.";
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
		$last_check = $send_actual_check_timestamps ? $cs_svc->{last_check} . "\t" : '';
		$service_text = xml2text($cs_plugin_out);
		## $service_text =~ s/&gt;/>/g;
		## $service_text =~ s/&lt;/</g;
		## $service_text =~ s/&apos;/'/g;
		## $service_text =~ s/&quot;/"/g;
		## $service_text =~ s/&amp;/&/g;
		push @states, "$last_check$host\t$service\t$check_state\t$service_text|\n";
	    }
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
    my $el_status    = undef;
    my $cs_status    = undef;
    my $check_state  = undef;
    my $el_hosts     = \%{ $element_ref->{Host} };
    my $cs_hosts     = \%{ $collage_ref->{Host} };
    my $el_host      = undef;
    my $cs_host      = undef;
    my $el_svcs      = undef;
    my $cs_svcs      = undef;
    my $el_svc       = undef;
    my $cs_svc       = undef;
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
	    ## FIX MAJOR:   Why bother reverting?  Just keep the original string around unmodified,
	    ## though then be sure not to send the extra copy where it's not wanted.
	    $host_text = xml2text( $el_host->{LastPluginOutput} );
	    ## $host_text =~ s/&gt;/>/g;
	    ## $host_text =~ s/&lt;/</g;
	    ## $host_text =~ s/&apos;/'/g;
	    ## $host_text =~ s/&quot;/"/g;
	    ## $host_text =~ s/&amp;/&/g;
	    push @states, "$host\t$check_state\t$host_text|\n";
	}
	$el_svcs = $el_host->{Service};
	$cs_svcs = \%{ $cs_host->{Service} };
	if ( defined $el_svcs ) {
	    foreach my $service ( keys( %{$el_svcs} ) ) {
		$el_svc    = \%{ $el_svcs->{$service} };
		$cs_svc    = \%{ $cs_svcs->{$service} };
		$el_status = $el_svc->{MonitorStatus};
		$cs_status = $cs_svc->{MonitorStatus};
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
		    $service_text = xml2text( $el_svc->{LastPluginOutput} );
		    ## $service_text =~ s/&gt;/>/g;
		    ## $service_text =~ s/&lt;/</g;
		    ## $service_text =~ s/&apos;/'/g;
		    ## $service_text =~ s/&quot;/"/g;
		    ## $service_text =~ s/&amp;/&/g;
		    push @states, "$host\t$service\t$check_state\t$service_text|\n";
		}
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

    ## If we are including individual timestamps in the messages, send_nsca needs to know that.
    if ($send_actual_check_timestamps) {
	$send_nsca_command .= ' -od';
	$secondary_send_nsca_command .= ' -od' if $send_to_secondary_NSCA;
    }

    log_timed_message 'Sending ' . $total_messages . ' results at ' . $logtime . "." if ($debug_basic);
    if ($debug_debug) {
	log_message "================================================================";
	print LOG @$messages;
	log_message "================================================================";
    }

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
    local $_;

    ## Prepend to each result the overhead info needed by the GDMA spooler, before spooling it.
    my $default_retries = 0;
    my $default_target  = 0;        # "0" implies that the result is to be sent to all the primary targets.
    my $prefix          = "$default_retries\t$default_target\t";
    $prefix .= time() . "\t" if not $send_actual_check_timestamps;    # provide a half-sane timestamp if none is already provided
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
