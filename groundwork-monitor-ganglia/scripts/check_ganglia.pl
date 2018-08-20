#!/usr/local/groundwork/perl/bin/perl -w --
#
# check_ganglia - A Ganglia cluster checker
#
use strict;
use warnings;

use DBI;
use Safe;
use Fcntl;
use IO::Socket;
use XML::LibXML;
use Data::Dumper;
use Time::HiRes;
use Getopt::Long;

# This is where we'll find the custom-metrics package, if one is configured.
# It's also where (in legacy deployments) we would pick up the TypedConfig
# package, so this line must come earlier than the TypedConfig reference.
use lib qw( /usr/local/groundwork/nagios/libexec );

use TypedConfig;

our ($PROGNAME, $socket);

$PROGNAME = "check_ganglia";
my $VERSION = "7.0.0";

#######################################################
#
#   Command Line Execution Options
#
#######################################################

my $print_help       = 0;
my $print_version    = 0;
my $config_file      = "/usr/local/groundwork/config/check_ganglia.conf";
my $debug_config     = 0;
my $suppress_updates = 0;

sub print_usage {
    print "usage:  check_ganglia.pl [-h] [-v] [-c config_file] [-d] [-n]\n";
    print "        -h:  print this help message\n";
    print "        -v:  print the version number\n";
    print "        -c config_file:  specify an alternate config file\n";
    print "             (default is $config_file)\n";
    print "        -d:  dump the config file entries (to debug them)\n";
    print "        -n:  run in read-only mode (don't update any external resources)\n";
}

print "=== Starting up (process $$). ===\n";

Getopt::Long::Configure ("no_ignore_case");
if (! GetOptions (
    'help'         => \$print_help,
    'version'      => \$print_version,
    'config=s'     => \$config_file,
    'debug-config' => \$debug_config,
    'noupdate'     => \$suppress_updates,
    )) {
    print "ERROR:  cannot parse command-line options!\n";
    print_usage;
    # Sleep a short while so we don't spin, chewing up CPU, when we are quickly restarted.
    sleep 1;
    exit 1;
}

if ($print_version) {
    print "$PROGNAME $VERSION\n";
    print "Copyright 2008-2017 GroundWork Open Source, Inc. (\"GroundWork\").  All rights\n";
    print "reserved.  Use is subject to GroundWork commercial license terms.\n";
}

if ($print_help) {
    print_usage;
}

exit if $print_help or $print_version;

# Since the remainder of our script does not process any command-line arguments,
# let's detect an apparently confused command line.
if (scalar @ARGV) {
    print "ERROR:  extra command-line arguments \"@ARGV\" are not understood\n";
    print_usage;
    # Sleep a short while so we don't spin, chewing up CPU, when we are quickly restarted.
    sleep 1;
    exit 1;
}

#######################################################
#
#   Configuration File Handling
#
#######################################################

# FIX MINOR:  All the reading of config info should be done inside an eval{}; statement,
# because it can throw exceptions.

my $config = TypedConfig->secure_new ($config_file, $debug_config);

sub allow {
    my $package = shift;
    # We're careful to use a form of the require that should provide some protection
    # against Perl-injection attacks through our configuration file, though of course
    # there is no possible protection against what is in the allowed package itself.
    return if ! defined $package || ! $package;
    eval {require "$package.pm";};
    if ($@) {
	## 'require' died; $package is not available.
	return;
    }
    else {
	## 'require' succeeded; $package was loaded.
	return 1;
    }
}

#######################################################
#
#   General Program Execution Options
#
#######################################################

# Process Ganglia XML streams?  If not, just sleep forever.
# This option is turned off in the default configuration file simply so the script can be
# safely installed before it is locally configured.  To get the software to run, it must be
# turned on in the configuration file once the rest of the setup is correct for your site.
my $enable_processing = $config->get_boolean ('enable_processing');

# Possibly autoflush the log output on every single write, for debugging mysterious failures.
my $autoflush_log_output = $config->get_boolean ('autoflush_log_output');
if ($autoflush_log_output || !$enable_processing) {
    $| = 1;
}

# Global Debug Level Flag;  No debug = 0, Statistics = 5, Normal debug = 6,
#                           Detail debug = 7 (gmond XML and metric attribute parsing)
# More precisely, specify one of the following numbers:
# NONE    = 0; turn off all debug info
# FATAL   = 1; the application is about to die
# ERROR   = 2; the application has found a serious problem, but will attempt to recover
# WARNING = 3; the application has found an anomaly, but will try to handle it
# NOTICE  = 4; the application wants to inform you of a significant event
# STATS   = 5; the application wants to log statistical data for later analysis
# INFO    = 6; the application wants to log a potentially interesting event
# DEBUG   = 7; the application wants to log detailed debugging data
# FIX MINOR:  allow the config file to specify these states as strings, not just as numbers,
# perhaps by implementing a TypedConfig->get_enum_value() routine so all the hard work is done elsewhere.
my $debug_level = $config->get_number ('debug_level');

# Send state updates to Nagios
my $send_to_nagios = $config->get_boolean ('send_to_nagios');

# Send updates to the database and Nagios even when the state does not change?
# (If not, update in any case on the next iteration after maximum_service_non_update_time.)
my $send_updates_even_when_state_is_persistent = $config->get_boolean ('send_updates_even_when_state_is_persistent');

# Avoid sending updates to the database and Nagios when the state is not changing?
# (Even if so, send them in any case on the next iteration after maximum_service_non_update_time.)
my $suppress_most_updates_for_persistent_non_okay_states = $config->get_boolean ('suppress_most_updates_for_persistent_non_okay_states');

# Absolute pathname of the Nagios command pipe.
my $nagios_cmd_pipe = $config->get_scalar ('nagios_cmd_pipe');

# Set to 1 to send all metric results as a single service.
my $consolidate_metrics = $config->get_boolean ('consolidate_metrics');

# Service name used for consolidated metrics.
my $consolidate_metrics_service_name = $config->get_scalar ('consolidate_metrics_service_name');

# Set to 1 to show more detail in the service output for each metric.
my $consolidate_metrics_service_output_detail = $config->get_boolean ('consolidate_metrics_service_output_detail');

# Set to 1 to send to nagios after each service result, 0 to send all results in a single write.
my $send_after_each_service = $config->get_boolean ('send_after_each_service');

# Set to a pattern that selects the part of a host name to report out.
my $short_hostname_pattern = $config->get_scalar ('short_hostname_pattern');

if ($short_hostname_pattern eq '') {
    # If the user doesn't want any hostname stripping, the config file setting will be
    # an empty string.  For proper usage within the script, we set this pattern to "^#",
    # which cannot match any hostnames since it contains an invalid hostname character.
    $short_hostname_pattern = "^#";
}

# Set to the name of an external package (not including the .pm filename extension) to call
# to filter the data or to process metrics, or to an empty string if no such package should be used.
my $custom_metrics_package = $config->get_scalar ('custom_metrics_package');

my $have_custom_metrics_package = allow $custom_metrics_package;
if ($custom_metrics_package && ! $have_custom_metrics_package) {
    chomp $@;
    print "Configured external package \"$custom_metrics_package\" cannot be found: $@\n";
    # Sleep a short while so we don't spin, chewing up CPU, when we are quickly restarted.
    sleep 10;
    exit 1;
}

my $custom_metrics = $custom_metrics_package->new() if $have_custom_metrics_package;
$custom_metrics_package->debug($debug_level) if $have_custom_metrics_package && $custom_metrics_package->can("debug");

my $initialize_custom_metrics       = $have_custom_metrics_package && $custom_metrics->can("initialize_custom_metrics");
my $process_custom_metrics          = $have_custom_metrics_package && $custom_metrics->can("process_custom_metrics");
my $update_custom_metrics           = $have_custom_metrics_package && $custom_metrics->can("update_custom_metrics");

my $initialize_non_production_hosts = $have_custom_metrics_package && $custom_metrics->can("initialize_non_production_hosts");;
my $find_non_production_hosts       = $have_custom_metrics_package && $custom_metrics->can("find_non_production_hosts");;

# Set to 1 to output a warning if the host has rebooted since the last reporting cycle.
my $output_reboot_warning = $config->get_boolean ('output_reboot_warning');

# Set to the number of seconds to continue to report a reboot warning, if $output_reboot_warning is set.
my $output_reboot_warning_duration = $config->get_number ('output_reboot_warning_duration');

# Set to 1 to output the mem_free_percent calculated metric.
my $output_mem_free_percent = $config->get_boolean ('output_mem_free_percent');

# Set to 1 to output the mem_cached_percent calculated metric.
my $output_mem_cached_percent = $config->get_boolean ('output_mem_cached_percent');

# Set to 1 to output the swap_free_percent calculated metric.
my $output_swap_free_percent = $config->get_boolean ('output_swap_free_percent');

# Set to 1 to output the time since Ganglia received an update for each host.
my $output_time_since_last_update = $config->get_boolean ('output_time_since_last_update');

#######################################################
#
#   Performance Throttling Parameters
#
#######################################################

# Time interval for each ganglia poll, in seconds.
my $cycle_time = $config->get_number ('cycle_time');

# Maximum time interval in seconds between service checks sent to Nagios.  That is, we will always
# send a service check result to Nagios on the next iteration after this time period has elapsed.
my $maximum_service_non_update_time = $config->get_number ('maximum_service_non_update_time');

# Maximum number of state changes that will be sent.
# If greater than this, check_ganglia will wait before sending remaining messages.
my $throttle_state_change_threshold = $config->get_number ('throttle_state_change_threshold');

# When threshold met, time in seconds to wait before sending remaining state change message buffer.
my $throttle_state_change_threshold_wait = $config->get_number ('throttle_state_change_threshold_wait');

# The number of slots to pre-allocate for queueing Nagios service messages
# before they are sent to the command pipe.  This number should be a little
# more than the total number of hosts in your grid.
my $initial_bulk_messages_size = $config->get_number ('initial_bulk_messages_size');

# The number of slots to pre-allocate for queueing metric-instance update rows before
# they are sent to the database.  This number should be a little more than the total
# number of metrics you expect to process in each cycle.  Bear in mind that you will
# typically have configured at least several metrics to be thresholded per host.
my $initial_metric_values_size = $config->get_number ('initial_metric_values_size');

# The maximum number of metric value rows you want to have updated in one database
# statement.  For efficient updates, it should be at least several thousand.
my $max_bulk_update_rows = $config->get_number ('max_bulk_update_rows');

# The maximum time in seconds to wait for any single write to the output command pipe
# to complete.
my $max_command_pipe_wait_time = $config->get_number ('max_command_pipe_wait_time');

# The maximum size in bytes for any single write operation to the output command pipe.
# The value chosen here must be no larger than PIPE_BUF (getconf -a | fgrep PIPE_BUF)
# on your platform, unless you have an absolute guarantee that no other process will
# ever write to the command pipe.
my $max_command_pipe_write_size = $config->get_number ('max_command_pipe_write_size');

# Send a check_ganglia service check result at the end of each polling cycle?
my $send_check_ganglia_service_check = $config->get_boolean ('send_check_ganglia_service_check');

#######################################################
#
#   Ganglia Parameters
#
#######################################################

# In this section, we occasionally call Data::Dumper->Dump() under debug control, to
# dump out the state of certain complex data structures.  That's because we need to
# provide an easy means of finding out how the script reacts to malformed setup in
# what is now an external configuration file.

# default Ganglia  gmond port is 8712
# default Ganglia gmetad port is 8651

# List ganglia GMOND hosts to query
my %ganglia_host_hash = $config->get_hash ('ganglia_hosts');

print Data::Dumper->Dump([\%ganglia_host_hash], [qw(\%ganglia_host_hash)]) if $debug_config;

my %ganglia_hosts = %{$ganglia_host_hash{'host'}};

print Data::Dumper->Dump([\%ganglia_hosts], [qw(\%ganglia_hosts)]) if $debug_config;

my @gangliaHosts = ();
foreach my $g_host (keys %ganglia_hosts) {
    print "ganglia_hosts.host = $g_host\n" if $debug_config;
    my $g_host_port = $ganglia_hosts{$g_host}{'port'};
    if (defined $g_host_port) {
	print "port for $g_host is $g_host_port\n" if $debug_config;
	push @gangliaHosts, [ $g_host, $g_host_port ];
    }
}

if (scalar @gangliaHosts == 0) {
    print "ERROR:  no ganglia_hosts with ports are defined in $config_file\n";
    # We sleep as a courtesy so we don't get into a rapid loop whereby some
    # watchdog process tries to restart us immediately after this failure.
    sleep 60;
    die "Exiting!\n";
}

print Data::Dumper->Dump([\@gangliaHosts], [qw(\@gangliaHosts)]) if $debug_config;

# If there are entries in the gangliaClusters array, only those clusters listed will be monitored.
# If empty, then all clusters will be monitored.
my @gangliaClusters = $config->get_array ('ganglia_cluster');

print Data::Dumper->Dump([\@gangliaClusters], [qw(\@gangliaClusters)]) if $debug_config;

# Ganglia thresholds database connection parameters.
my $ganglia_dbtype = $config->get_scalar ('ganglia_dbtype');
my $ganglia_dbhost = $config->get_scalar ('ganglia_dbhost');
my $ganglia_dbname = $config->get_scalar ('ganglia_dbname');
my $ganglia_dbuser = $config->get_scalar ('ganglia_dbuser');
my $ganglia_dbpass = $config->get_scalar ('ganglia_dbpass');

my $nonProductionHostMapRef = {};

#######################################################
#
#   Foundation Options (used if send_to_foundation=1)
#
#######################################################

# Send host/service status updates to Foundation
my $send_to_foundation = $config->get_boolean ('send_to_foundation');

# This monitoring server name; used as update content (message source).
my $this_server        = $config->get_scalar ('this_server');

# Where to connect to Foundation to send the updates.
my $foundation_host    = $config->get_scalar ('foundation_host');
my $foundation_port    = $config->get_scalar ('foundation_port');	# usual is 4913

#######################################################
#
#   Custom Options
#
#######################################################

$custom_metrics->initialize_non_production_hosts ($short_hostname_pattern, $debug_config) if $initialize_non_production_hosts;

#######################################################
#
#   Execution Global variables
#
#######################################################

# This is an internal variable, not something to be drawn from the configuration file.
my $check_ganglia_service_check_host = `hostname`;

# Variables to be used as quick tests to see if we're interested in particular debug messages.
my $DEBUG_NONE    = $debug_level == 0;	# turn off all debug info
my $DEBUG_FATAL   = $debug_level >= 1;	# the application is about to die
my $DEBUG_ERROR   = $debug_level >= 2;	# the application has found a serious problem, but will attempt to recover
my $DEBUG_WARNING = $debug_level >= 3;	# the application has found an anomaly, but will try to handle it
my $DEBUG_NOTICE  = $debug_level >= 4;	# the application wants to inform you of a significant event
my $DEBUG_STATS   = $debug_level >= 5;	# the application wants to log statistical data for later analysis
my $DEBUG_INFO    = $debug_level >= 6;	# the application wants to log a potentially interesting event
my $DEBUG_DEBUG   = $debug_level >= 7;	# the application wants to log detailed debugging data

my $metric_thresh			= undef;	# Threshold Hashtable Populated From DB
my $metric_state			= undef;	# Current state reference - used to determine if we need to send nagios update
my %perfData				= ();
my @gangliaXMLString			= ();
my @crit_metrics			= ();	# Critical array
my @warn_metrics			= ();	# Warning array
my @ok_metrics				= ();	# OK array
my @duration_metrics			= ();	# Critical but in duration array
my @unknown_metrics			= ();	# Unknown array
my $host_msg				= '';	# Set output message for a host
my $foundation_xml_message		= '';
my $loopcount				= 1;
my $state_has_changed			= 0;
my $state_is_stale			= 0;
my $total_state_changes			= 0;
my $total_stale_state_updates		= 0;
my $throttle_state_change_count		= 0;
my $throttle_state_change_host_flag	= 0;
my $must_update_nagios			= 0;

my $is_postgresql = ( defined($ganglia_dbtype) && $ganglia_dbtype eq 'postgresql' );
my $is_mysql      = !$is_postgresql;

my @bulk_messages			= ();
$#bulk_messages = $initial_bulk_messages_size;	# pre-extend the array, for efficiency
$#bulk_messages = -1;				# truncate the array, since we don't have any Nagios messages yet

my @metric_values			= ();	# made global to reduce memory fragmentation
$#metric_values = $initial_metric_values_size;	# pre-extend the array, for efficiency

my $startTime = Time::HiRes::time();

########################################################
#
#   Program Start
#
########################################################

# Stop if this is just a debugging run.
exit(0) if $debug_config;

if (! $enable_processing) {
    # This might not be the best channel on which to output this warning, but at least we've made some attempt
    # to tell the administrator what's happening, regardless of what context this script is running in.
    print "WARNING:  check_ganglia processing is not enabled in the config file; it will sleep forever.\n";

    # Let's also send a targeted notice that we hope will show up on the monitoring operator console.
    if ($send_check_ganglia_service_check and $send_to_nagios) {
	my $nagios_message = "WARNING:  check_ganglia processing is not enabled in the config file; it will sleep forever.";
	$#bulk_messages = -1;	# truncate the array of Nagios messages, so we only send this one service message right now
	construct_process_service_check_message($check_ganglia_service_check_host, "check_ganglia", 0, $nagios_message);
	sendToNagios();
    }

    # Sleep forever, simply so we don't get continually restarted and waste resources.
    sleep 100000000;
    # We use an exit status of 4 (if the sleep ever expired) to indicate that the script is disabled.
    exit 4;
}

# We re-open the STDERR stream as a duplicate of the output stream, to capture any output
# written to STDERR (from, say, any Perl warnings generated by poor coding).  This also
# is used to ensure that the output from STDERR is properly interleaved when-it-happens
# with the output from STDOUT, to simplify interpreting the log file.
if ( !open( STDERR, '>>&STDOUT' ) ) {
    print "ERROR:  Cannot redirect STDERR to STDOUT: $!\n";
    exit 1;
}
else {
    ## Autoflush the standard output on every single write, to avoid problems
    ## with block i/o and badly interleaved output lines on STDOUT and STDERR.
    ## Note that it is the STDOUT stream that needs to be autoflushed (to avoid
    ## buffering those lines, so they are output immediately just like STDERR
    ## lines), not the STDERR stream, to achieve proper interleaving.
    ##
    ## Note that if we turn on autoflush here, this will unconditionally override
    ## the autoflush_log_output option.  So for the time being, we'll leave that
    ## up to the config file setting instead.
    # STDOUT->autoflush(1);
}

readGangliaSTATE();
$perfData{'ganglia_db_initial_state_read_time'} = sprintf("%0.2F", (Time::HiRes::time() - $startTime) * 1000);	# milliseconds
print_metric_state() if $DEBUG_DEBUG;
while (1) {
    my $loop_start_time = Time::HiRes::time();
    $startTime = Time::HiRes::time();
    &main;
    my $exec_time = Time::HiRes::time() - $loop_start_time;	# used to compute time to wait before next execution
    print "Loop count=$loopcount. Last loop exec time= $exec_time.\n";
    flush_log_output();
    if ($exec_time < $cycle_time) {
	my $wait_time = int($cycle_time - $exec_time);
	$loopcount++;
	print "Waiting $wait_time seconds...\n";
	flush_log_output();
	sleep $wait_time;
    }
}
exit 0;

########################################################
#
#   Program Subroutines
#
########################################################

sub main() {
    readGangliaDB();
    print "Finished reading Ganglia db at ".(Time::HiRes::time() - $startTime)."\n";
    $perfData{'ganglia_db_threshold_read_time'} = sprintf("%0.2F", (Time::HiRes::time() - $startTime) * 1000);	# milliseconds
    readGangliaGMOND();
    print "Finished reading Ganglia GMOND at ".(Time::HiRes::time() - $startTime)."\n";
    $custom_metrics->initialize_custom_metrics() if $initialize_custom_metrics;
    if ($find_non_production_hosts) {
	$nonProductionHostMapRef = $custom_metrics->find_non_production_hosts();
	print "Finished reading Non Production Hosts at ".(Time::HiRes::time() - $startTime)."\n";
    }
    processGangliaXML();
    $custom_metrics->update_custom_metrics() if ($update_custom_metrics && ! $suppress_updates);
    print "Finished parsing Ganglia XML at ".(Time::HiRes::time() - $startTime)."\n";
    if (!$send_after_each_service and $send_to_nagios) {
	sendToNagios();
	print "Finished Sending to Nagios at ".(Time::HiRes::time() - $startTime)."\n";
    }
    if (!$send_after_each_service and $send_to_foundation) {
	sendToFoundation($foundation_host,$foundation_port,$this_server);
	print "Finished Sending to Foundation at ".(Time::HiRes::time() - $startTime)."\n";
    }
    my $updatestartTime = Time::HiRes::time();
    updateGangliaDB() if ! $suppress_updates;
    $perfData{'ganglia_db_update_time'} = sprintf("%0.2F", (Time::HiRes::time() - $updatestartTime) * 1000);	# milliseconds
    calculateStatistics();
    printStatistics() if $DEBUG_STATS;
    if ($send_check_ganglia_service_check and $send_to_nagios) {
	# The performance statistics for sending this one additional message will not be included in any of the reported statistics, but so be it.
	my $nagios_message = "Processed $perfData{'num_nagios_messages'} Nagios updates in $perfData{'total_execution_time'} seconds.";
	my @perf_stats = ();
	push @perf_stats, "nagios_messages=$perfData{'num_nagios_messages'}";
	push @perf_stats, "state_changes=$total_state_changes";
	push @perf_stats, "stale_state_updates=$total_stale_state_updates";
	push @perf_stats, "script_exec_time=$perfData{'total_execution_time'}s";
	push @perf_stats, "cks_piped_per_sec=$perfData{'nagios_insertion_speed_sps'}";
	$#bulk_messages = -1;	# truncate the array of Nagios messages, so we only send this one last service message right now
	construct_process_service_check_message($check_ganglia_service_check_host, "check_ganglia", 0, $nagios_message . "|" . join(" ",@perf_stats));
	sendToNagios();
    }
    %perfData = ();			# Reset all performance counters
    @gangliaXMLString = ();		# Reset ganglia XML read string
    $#bulk_messages = -1;		# truncate the array of Nagios messages
    $foundation_xml_message = '';	# Reset Foundation send buffer
}

sub flush_log_output {
    if (! $autoflush_log_output) {
	$| = 1;
	$| = 0;
    }
}

sub print_metric_thresh {
    print "Metric Thresholds:\n";
    my $CLUSTER_hashref = $metric_thresh->{CLUSTER};
    my $HOST_hashref;
    my $METRIC_hashref;
    if (defined $CLUSTER_hashref) {
	foreach my $cluster (sort keys %$CLUSTER_hashref) {
	    print "Cluster=$cluster\n";
	    $HOST_hashref = $CLUSTER_hashref->{$cluster}->{HOST};
	    if (defined $HOST_hashref) {
		foreach my $host (sort keys %$HOST_hashref) {
		    print "\thost=$host\n";
		    ## FIX MAJOR:  This just looks completely wrong; it doesn't reference the $host,
		    ## so we're just looping over the same stuff for every $host.  Is this code correct?
		    ## One way or another, use proper hashref variables here to speed it up.
		    foreach my $metric (sort keys %{ $CLUSTER_hashref->{$cluster}->{METRIC}}) {
			print "\t\tmetric=$metric\n";
		    }
		}
	    }
	}
    }
}

sub print_metric_state {
    print "Metric State:\n";
    my $CLUSTER_hashref = $metric_state->{CLUSTER};
    my $HOST_hashref;
    my $METRIC_hashref;
    my $metric_hashref;
    if (defined $CLUSTER_hashref) {
	foreach my $cluster (sort keys %$CLUSTER_hashref) {
	    print "Cluster=$cluster\n";
	    $HOST_hashref = $CLUSTER_hashref->{$cluster}->{HOST};
	    if (defined $HOST_hashref) {
		foreach my $host (sort keys %$HOST_hashref) {
		    print "\thost=$host\n";
		    $METRIC_hashref = $HOST_hashref->{$host}->{METRIC};
		    if (defined $METRIC_hashref) {
			foreach my $metric (sort keys %$METRIC_hashref) {
			    print "\t\tmetric=$metric\n";
			    $metric_hashref = $METRIC_hashref->{$metric};
			    if (defined $metric_hashref) {
				foreach my $key (sort keys %$metric_hashref) {
				    print "\t\t\t$key=$metric_hashref->{$key}\n";
				}
			    }
			}
		    }
		}
	    }
	}
    }
}

sub readGangliaDB {
    my $dsn = '';
    if ( defined($ganglia_dbtype) && $ganglia_dbtype eq 'postgresql' ) {
        $dsn = "DBI:Pg:dbname=$ganglia_dbname;host=$ganglia_dbhost";
    }
    else {
        $dsn = "DBI:mysql:database=$ganglia_dbname;host=$ganglia_dbhost";
    }
    my $dbh = DBI->connect( $dsn, $ganglia_dbuser, $ganglia_dbpass, { 'AutoCommit' => 1 } );
    if (!$dbh) {
	print "ERROR:  Cannot connect to database $ganglia_dbname: ", $DBI::errstr, "\n";
	# Sleep a short while so we don't spin, chewing up CPU, when we are quickly restarted.
	sleep 10;
	exit 2;
    }

    # Before we grab current thresholds from the database, clear out any previous notion of what the thresholds were.
    # This allows us to quickly alter our behavior when thresholds are deleted from the database.
    $metric_thresh = undef;

    my ($query, $sth);
    # Set global default metric thresholds.
    $query =
	"select
	    m.Name      as \"MetricName\",
	    mv.Critical as \"Critical\",
	    mv.Warning  as \"Warning\",
	    mv.Duration as \"Duration\"
	from
	    metricvalue as mv,
	    metric as m,
	    location as l,
	    cluster as c,
	    host as h
	where
	    m.MetricID=mv.MetricID and
	    l.LocationID=mv.LocationID and
	    c.ClusterID=mv.ClusterID and
	    h.HostID=mv.HostID and
	    l.Name='Default' and
	    c.Name='Default' and
	    h.Name='Default'
	";
    $sth = $dbh->prepare($query);
    $sth->execute() or die $sth->errstr;
    my $metric_hashref;
    while (my $row = $sth->fetchrow_hashref()) {
	# We're using autovivification here.
	$metric_hashref = \%{ $metric_thresh->{METRIC}->{$row->{MetricName}} };
	$metric_hashref->{WARN}     = $row->{Warning};
	$metric_hashref->{CRIT}     = $row->{Critical};
	$metric_hashref->{DURATION} = $row->{Duration};
    }
    if (defined($sth->err)) {
	print "Database problem while fetching global default metric thresholds; Error: " . $sth->errstr . "; State: " . $sth->state . "\n";
    }
    $sth->finish();
    # Set cluster default metric thresholds.
    $query =
	"select
	    m.Name      as \"MetricName\",
	    c.Name      as \"ClusterName\",
	    mv.Critical as \"Critical\",
	    mv.Warning  as \"Warning\",
	    mv.Duration as \"Duration\"
	from
	    metricvalue as mv,
	    metric as m,
	    location as l,
	    cluster as c,
	    host as h
	where
	    m.MetricID=mv.MetricID and
	    l.LocationID=mv.LocationID and
	    c.ClusterID=mv.ClusterID and
	    h.HostID=mv.HostID and
	    l.Name='Default' and
	    c.Name<>'Default' and
	    h.Name='Default'
	";
    $sth = $dbh->prepare($query);
    $sth->execute() or die $sth->errstr;
    while (my $row = $sth->fetchrow_hashref()) {
	# We're using autovivification here.
	$metric_hashref = \%{ $metric_thresh->{CLUSTER}->{$row->{ClusterName}}->{METRIC}->{$row->{MetricName}} };
	$metric_hashref->{WARN}     = $row->{Warning};
	$metric_hashref->{CRIT}     = $row->{Critical};
	$metric_hashref->{DURATION} = $row->{Duration};
    }
    if (defined($sth->err)) {
	print "Database problem while fetching cluster default metric thresholds; Error: " . $sth->errstr . "; State: " . $sth->state . "\n";
    }
    $sth->finish();
    # Set host metric thresholds.
    $query =
	"select
	    m.Name      as \"MetricName\",
	    c.Name      as \"ClusterName\",
	    h.Name      as \"HostName\",
	    mv.Critical as \"Critical\",
	    mv.Warning  as \"Warning\",
	    mv.Duration as \"Duration\"
	from
	    metricvalue as mv,
	    metric as m,
	    location as l,
	    cluster as c,
	    host as h
	where
	    m.MetricID=mv.MetricID and
	    l.LocationID=mv.LocationID and
	    c.ClusterID=mv.ClusterID and
	    h.HostID=mv.HostID and
	    l.Name='Default' and
	    c.Name<>'Default' and
	    h.Name<>'Default'
	";
    $sth = $dbh->prepare($query);
    $sth->execute() or die $sth->errstr;
    while (my $row = $sth->fetchrow_hashref()) {
	# We're using autovivification here.
	$metric_hashref = \%{ $metric_thresh->{CLUSTER}->{$row->{ClusterName}}->{HOST}->{$row->{HostName}}->{METRIC}->{$row->{MetricName}} };
	$metric_hashref->{WARN}     = $row->{Warning};
	$metric_hashref->{CRIT}     = $row->{Critical};
	$metric_hashref->{DURATION} = $row->{Duration};
    }
    if (defined($sth->err)) {
	print "Database problem while fetching host metric thresholds; Error: " . $sth->errstr . "; State: " . $sth->state . "\n";
    }
    $sth->finish();
    $dbh->disconnect();
}

sub readGangliaSTATE {
    my $dsn = '';
    if ( defined($ganglia_dbtype) && $ganglia_dbtype eq 'postgresql' ) {
        $dsn = "DBI:Pg:dbname=$ganglia_dbname;host=$ganglia_dbhost";
    }
    else {
        $dsn = "DBI:mysql:database=$ganglia_dbname;host=$ganglia_dbhost";
    }
    my $dbh = DBI->connect( $dsn, $ganglia_dbuser, $ganglia_dbpass, { 'AutoCommit' => 1 } );
    if (!$dbh) {
	print "ERROR:  Cannot connect to database $ganglia_dbname: ", $DBI::errstr, "\n";
	# Sleep a short while so we don't spin, chewing up CPU, when we are quickly restarted.
	sleep 10;
	exit 2;
    }
    my ($query, $sth);
    # Set Ganglia state reference objects
    $query =
	"select
	    mi.MetricInstanceID    as \"MetricInstanceID\",
	    m.Name                 as \"MetricName\",
	    c.Name                 as \"ClusterName\",
	    h.Name                 as \"HostName\",
	    h.IPAddress            as \"IPAddress\",
	    mi.LastState           as \"LastState\",
	    mi.LastUpdateTime      as \"LastUpdateTime\",
	    mi.LastStateChangeTime as \"LastStateChangeTime\",
	    mi.LastValue           as \"LastValue\"
	from
	    host as h,
	    hostinstance as hi,
	    metricinstance as mi,
	    metric as m,
	    cluster as c
	where
	    hi.HostID = h.HostID
	and mi.HostInstanceID = hi.HostInstanceID
	and m.MetricID = mi.MetricID
	and c.ClusterID = hi.ClusterID;
	";
    $sth = $dbh->prepare($query);
    $sth->execute() or die $sth->errstr;
    $perfData{'ganglia_db_initial_state_metric_count'} = 0;
    my %hostcounthash = ();
    my %clustercounthash = ();
    my $cluster_hashref;
    my $host_hashref;
    my $metric_hashref;
    while (my $row = $sth->fetchrow_hashref()) {
	## We're using autovivification here, which complicates the points at which we can capture hashrefs.
	$cluster_hashref = \%{ $metric_state->{CLUSTER}->{$row->{ClusterName}} };
	$cluster_hashref->{DEFINED} = 1;
	$host_hashref = \%{ $cluster_hashref->{HOST}->{$row->{HostName}} };
	$host_hashref->{DEFINED}   = 1;
	$host_hashref->{IPADDRESS} = $row->{IPAddress};
	$metric_hashref = \%{ $host_hashref->{METRIC}->{$row->{MetricName}} };
	$metric_hashref->{DEFINED}             = 1;
	$metric_hashref->{METRICINSTANCEID}    = $row->{MetricInstanceID};
	$metric_hashref->{LASTSTATE}           = $row->{LastState};
	$metric_hashref->{LASTSTATECHANGETIME} = $row->{LastStateChangeTime};
	$metric_hashref->{LASTUPDATETIME}      = $row->{LastUpdateTime};
	$metric_hashref->{VALUE}               = $row->{LastValue};

	$perfData{'ganglia_db_initial_state_metric_count'}++;
	$hostcounthash{$row->{HostName}} = 1;
	$clustercounthash{$row->{ClusterName}} = 1;

	# print "Initial State $row->{ClusterName},$row->{HostName}, $row->{MetricName} LastState=$row->{LastState},".
	#     " LastUpdateTime=$row->{LastUpdateTime}, LastStateChangeTime=$row->{LastStateChangeTime}\n" if $DEBUG_INFO;
    }
    if (defined($sth->err)) {
	print "Database problem while fetching Ganglia state data; Error: " . $sth->errstr . "; State: " . $sth->state . "\n";
    }
    $sth->finish();
    # Set cluster default metric thresholds.
    $perfData{'ganglia_db_initial_state_cluster_count'} = scalar(keys %clustercounthash);
    $perfData{'ganglia_db_initial_state_host_count'} = scalar(keys %hostcounthash);
    $dbh->disconnect();
}

# The organization of this code is intended to minimize the number of database accesses,
# as they are considered to be very expensive operations if done on a datum-by-datum basis.
# To understand the nature of the tables we're dealing with, here are basic statistics on
# the relative sizes of the ganglia database tables at a large site.
#
# Table           # Rows
# =============== ======
# cluster              7
# clusterhost          2
# host              5000
# hostinstance      5000
# location             1
# metric               6
# metricinstance   30000
# metricvalue         54

sub updateGangliaDB {
    my $dsn = '';
    if ( defined($ganglia_dbtype) && $ganglia_dbtype eq 'postgresql' ) {
        $dsn = "DBI:Pg:dbname=$ganglia_dbname;host=$ganglia_dbhost";
    }
    else {
        $dsn = "DBI:mysql:database=$ganglia_dbname;host=$ganglia_dbhost";
    }
    my $dbh = DBI->connect( $dsn, $ganglia_dbuser, $ganglia_dbpass, { 'AutoCommit' => 1 } );
    if (!$dbh) {
	print "ERROR:  Cannot connect to database $ganglia_dbname: ", $DBI::errstr, "\n";
	# Sleep a short while so we don't spin, chewing up CPU, when we are quickly restarted.
	sleep 10;
	exit 2;
    }
    my ($query, $sth);
    $#metric_values = -1;	# truncate the array, since we haven't saved anything into it yet
    $perfData{'ganglia_db_metric_update'} = 0;

    print "Updating Ganglia DB with metric state information.\n" if $DEBUG_INFO;
    my $CLUSTER_hashref = $metric_state->{CLUSTER};
    my $HOST_hashref;
    my $host_hashref;
    my $METRIC_hashref;
    my $metric_hashref;
    if (defined $CLUSTER_hashref) {
	foreach my $cluster (sort keys %$CLUSTER_hashref) {
	    print "Checking cluster $cluster.\n" if $DEBUG_DEBUG;
	    # If this cluster is not in the metric_state reference, then add to database.
	    if (!($CLUSTER_hashref->{$cluster}->{DEFINED})) {
		$CLUSTER_hashref->{$cluster}->{DEFINED} = 1;
		## Insert the cluster into the database.
		$query = "select ClusterID as \"ClusterID\" from cluster where Name='$cluster' limit 1";
		$sth = $dbh->prepare($query);
		$sth->execute() or print "ERROR: $query\n", $sth->errstr, "\n";
		my $clusterid = undef;
		while (my $row = $sth->fetchrow_hashref()) {
		    $clusterid = $row->{ClusterID};
		}
		if (defined($sth->err)) {
		    print "Database problem while updating Ganglia state data; Error: " . $sth->errstr . "; State: " . $sth->state . "\n";
		}
		$sth->finish();
		## if cluster already exists, then skip
		if ($clusterid) {
		    print "Trying to insert a cluster '$cluster' that already exists in the database\n" if $DEBUG_INFO;
		}
		else {
		    print "Insert new cluster '$cluster'\n" if $DEBUG_INFO;
		    $query = "insert into cluster values(default,'$cluster','$cluster',0)";
		    $dbh->do($query) or print "ERROR: $query\n", $dbh->errstr, "\n";
		}
	    }
	    $HOST_hashref = $CLUSTER_hashref->{$cluster}->{HOST};
	    if (defined $HOST_hashref) {
		foreach my $host (sort keys %$HOST_hashref) {
		    print "Checking host $host.\n" if $DEBUG_DEBUG;
		    $host_hashref = $HOST_hashref->{$host};
		    # If this host is not in the metric_state reference, then add to database.
		    if (!($host_hashref->{DEFINED})) {
			# Insert the host into the database
			$query = "select HostID as \"HostID\" from host where Name='$host'";
			$sth = $dbh->prepare($query);
			$sth->execute() or print "ERROR: $query\n", $sth->errstr, "\n";
			my $hostid = undef;
			while (my $row = $sth->fetchrow_hashref()) {
			    $hostid = $row->{HostID};
			}
			if (defined($sth->err)) {
			    print "Database problem while updating Ganglia state data; Error: " . $sth->errstr . "; State: " . $sth->state . "\n";
			}
			$sth->finish();
			if ($hostid) {	# if host already exists, then skip
			    print "Trying to insert host '$host' that already exists in the database\n" if $DEBUG_INFO;
			}
			else {
			    print "Insert new host '$host'\n" if $DEBUG_INFO;
			    if ($host_hashref->{IPADDRESS}) {
				$query = "insert into host values".
				    "(default,'$host','$host_hashref->{IPADDRESS}',".
				    "'$host_hashref->{CONFIGSTRING}',0)";
			    }
			    else {
				print "Trying to insert host '$host' without an IP address.\n" if $DEBUG_WARNING;
				next;
			    }
			    $dbh->do($query) or print "ERROR: $query\n", $dbh->errstr, "\n";
			    # $perfData{'ganglia_db_new_host_count'}++;
			}
		    }
		    else {
			# See if the data changed for an existing host
			if ($host_hashref->{IPADDRESS_CHANGED}) {
			    if ($host_hashref->{IPADDRESS}) {
				print "Update host '$host' IP address to $host_hashref->{IPADDRESS}\n" if $DEBUG_INFO;
				$query = "update host set IPAddress='$host_hashref->{IPADDRESS}' where Name='$host'";
				$dbh->do($query) or print "ERROR: $query\n", $dbh->errstr, "\n";
				$host_hashref->{IPADDRESS_CHANGED} = 0;
				# Let's force the IP address embedded within the ganglia.host.Description field to be updated as well.
				$host_hashref->{BOOTTIME_CHANGED} = 1;
			    }
			}
			if ($host_hashref->{BOOTTIME_CHANGED}) {
			    # The ganglia.host.Description field contains a variety of data, any of which might have changed while the machine was down.
			    # Most notably, this includes the boot time itself.
			    if ($host_hashref->{CONFIGSTRING}) {
				print "Update host '$host' Description to $host_hashref->{CONFIGSTRING}\n" if $DEBUG_INFO;
				$query = "update host set Description='$host_hashref->{CONFIGSTRING}' where Name='$host'";
				$dbh->do($query) or print "ERROR: $query\n", $dbh->errstr, "\n";
				$host_hashref->{BOOTTIME_CHANGED} = 0;
			    }
			}
		    }
		    # See if we need to insert a host instance
		    if (!($host_hashref->{DEFINED})) {
			$host_hashref->{DEFINED} = 1;
			## Insert host instance
			$query = "select hi.HostID as \"HostID\" from hostinstance as hi, cluster as c, host as h ".
			    "where h.Name='$host' and c.Name='$cluster' and hi.HostID=h.HostID and hi.ClusterID=c.ClusterID";
			$sth = $dbh->prepare($query);
			$sth->execute() or print "ERROR: $query\n", $sth->errstr, "\n";
			my $hostinstid = undef;
			while (my $row = $sth->fetchrow_hashref()) {
			    $hostinstid = $row->{HostID};
			}
			if (defined($sth->err)) {
			    print "Database problem while updating Ganglia state data; Error: " . $sth->errstr . "; State: " . $sth->state . "\n";
			}
			$sth->finish();
			if ($hostinstid) {	# if host already exists, then skip
			    print "Trying to insert host instance cluster '$cluster', host '$host' that already exists in the database\n" if $DEBUG_INFO;
			}
			else {
			    print "Insert new host instance for cluster $cluster, host '$host'\n" if $DEBUG_INFO;
			    $query = "insert into hostinstance values(default,(select ClusterID from cluster where Name='$cluster'),".
				"(select HostID from host where Name='$host'),(select LocationID from location where Name='Default'))";
			    $dbh->do($query) or print "ERROR: $query\n", $dbh->errstr, "\n";
			}
		    }
		    $METRIC_hashref = $host_hashref->{METRIC};
		    if (defined $METRIC_hashref) {
			foreach my $metric (sort keys %$METRIC_hashref) {
			    print "Checking metric $metric.\n" if $DEBUG_DEBUG;
			    my $metricinstanceid = undef;
			    $metric_hashref = \%{ $METRIC_hashref->{$metric} };
			    ## If this metric is not in the metric_state reference, then add to database.
			    if (!($metric_hashref->{DEFINED})) {
				# Insert the metric into the database
				$query = "select MetricID as \"MetricID\" from metric where Name='$metric'";
				$sth = $dbh->prepare($query);
				$sth->execute() or die $sth->errstr;
				my $metricid = undef;
				while (my $row = $sth->fetchrow_hashref()) {
				    $metricid = $row->{MetricID};
				}
				if ( defined( $sth->err ) ) {
				    print "Database problem while updating Ganglia state data; Error: "
				      . $sth->errstr
				      . "; State: "
				      . $sth->state . "\n";
				}
				$sth->finish();
				## if metric already exists, then skip
				if ($metricid) {
				    print "Trying to insert metric '$metric' that already exists in the database\n" if $DEBUG_INFO;
				}
				else {
				    print "Insert new metric '$metric'\n" if $DEBUG_INFO;
				    $query = "insert into metric values(default,'$metric','$metric','',0,0,0)";
				    $dbh->do($query) or print "ERROR: $query\n", $dbh->errstr, "\n";
				}

				# get this metric instance ID, if it already exists (it might not, in which case we'll create it below)
				$query =
				    "select
					mi.MetricInstanceID as \"MetricInstanceID\"
				    from
					hostinstance as hi,
					metricinstance as mi,
					metric as m,
					cluster as c,
					host as h
				    where
					h.Name='$host' and
					c.Name='$cluster' and
					hi.HostID=h.HostID and
					hi.ClusterID=c.ClusterID and
					m.Name='$metric' and
					mi.HostInstanceID=hi.HostInstanceID and
					mi.MetricID=m.MetricID
				    ";
				$sth = $dbh->prepare($query);
				$sth->execute() or print "ERROR: $query\n", $sth->errstr, "\n";
				while (my $row = $sth->fetchrow_hashref()) {
				    $metricinstanceid = $row->{MetricInstanceID};
				    $metric_hashref->{METRICINSTANCEID} = $row->{MetricInstanceID};
				    $metric_hashref->{DEFINED}          = 1;
				}
				if ( defined( $sth->err ) ) {
				    print "Database problem while updating Ganglia state data; Error: "
				      . $sth->errstr
				      . "; State: "
				      . $sth->state . "\n";
				}
				$sth->finish();
			    }
			    else {
				$metricinstanceid = $metric_hashref->{METRICINSTANCEID};
			    }
			    print "Checking metric instance $cluster, $host, $metric, id=$metricinstanceid.\n" if $DEBUG_INFO;
			    ## See if we need to insert a metric instance.
			    if ($metricinstanceid) {
				if ($metric_hashref->{DBUPDATE_STATES}) {
				    print "Updating metric instance $metric.\n" if $DEBUG_INFO;

				    # Queue the updating of the metric variables.
				    push @metric_values, join (',',
					$metricinstanceid,
					$metric_hashref->{LASTSTATE},
					$metric_hashref->{LASTUPDATETIME},
					$metric_hashref->{LASTSTATECHANGETIME},
					$metric_hashref->{VALUE});

		# Old code that we replaced with the @metric_values bulk-update-queueing mechanism, for performance reasons.
		#		    # Update the metric variables
		#		    $query = "update metricinstance set ".
		#		    	      "LastState='$metric_hashref->{LASTSTATE}',".
		#		    	 "LastUpdateTime='$metric_hashref->{LASTUPDATETIME}',".
		#		        "LastStateChangeTime='$metric_hashref->{LASTSTATECHANGETIME}',".
		#		    	      "LastValue='$metric_hashref->{VALUE}' ".
		#		        " where MetricInstanceID=$metricinstanceid";
		#		    $dbh->do($query) or print "ERROR: $query\n", $dbh->errstr, "\n";

				    $metric_hashref->{DBUPDATE_STATES} = 0;
				    $perfData{'ganglia_db_metric_update'}++;
				}
			    }
			    else {
				## Insert a metric instance.
				print "Insert new metric instance for cluster $cluster, host '$host', metric '$metric'\n" if $DEBUG_INFO;
				$query = "insert into metricinstance values(default,".
				    "(select hi.HostInstanceID from hostinstance as hi, cluster as c, host as h ".
				    "    where h.Name='$host' and c.Name='$cluster' and hi.HostID=h.HostID and hi.ClusterID=c.ClusterID),".
				    "(select MetricID from metric where name='$metric'),".
				    "'$metric',".
				    "'$metric_hashref->{LASTSTATE}',".
				    "'$metric_hashref->{LASTUPDATETIME}',".
				    "'$metric_hashref->{LASTSTATECHANGETIME}',".
				    "'$metric_hashref->{VALUE}')";
				$dbh->do($query) or print "ERROR: $query\n", $dbh->errstr, "\n";
				## Set the metricinstanceid in the in-memory state table, so it is
				## available to make future updates highly efficient.
				## FIX LATER:  We could probably fetch this value faster using the last insert ID.
				$query =
				    "select
					mi.MetricInstanceID as \"MetricInstanceID\"
				    from
					hostinstance as hi,
					metricinstance as mi,
					metric as m,
					cluster as c,
					host as h
				    where
					h.Name='$host' and
					c.Name='$cluster' and
					hi.HostID=h.HostID and
					hi.ClusterID=c.ClusterID and
					m.Name='$metric' and
					mi.HostInstanceID=hi.HostInstanceID and
					mi.MetricID=m.MetricID
				    ";
				$sth = $dbh->prepare($query);
				$sth->execute() or print "ERROR: $query\n", $sth->errstr, "\n";
				while (my $row = $sth->fetchrow_hashref()) {
				    $metric_hashref->{METRICINSTANCEID} = $row->{MetricInstanceID};
				    $metric_hashref->{DEFINED}          = 1;
				}
				if ( defined( $sth->err ) ) {
				    print "Database problem while updating Ganglia state data; Error: "
				      . $sth->errstr
				      . "; State: "
				      . $sth->state . "\n";
				}
				$sth->finish();
			    }
			}
		    }
		}
	    }
	}
    }

    # We probably now have a huge number of metricinstance-table updates queued up.  Do them all in bulk now.
    # These are all pure updates, not inserts, so we need a bulk-update command, not a bulk-upsert command.
    #
    if (scalar(@metric_values)) {
	## The mechanisms for doing optimal bulk updates in PostgreSQL and MySQL use similar syntax for the list of values,
	## so we can implement the differences at the level of the query rather than at the level of the enclosing logic.
	my $saved_list_separator = $";
	my $first;
	my $last;
	for ($first = 0; $first <= $#metric_values; $first = $last + 1) {
	    $last = $first + $max_bulk_update_rows - 1;
	    $last = $#metric_values if $last > $#metric_values;
	    $" = '),(';
	    if ($is_postgresql) {
		## "update ... from" (and thus the use of values() in this context) is a PostgreSQL extension
		## which is perfect for our bulk-insert needs here.
		##
		## Immediately after a PostgreSQL bulk update, or at least once per overall cycle (cycle_time or
		## maximum_service_non_update_time), we need to run a simple "VACUUM metricinstance" command, to
		## remove the now-dead rows so as to prevent the table from filling up with garbage and slowing
		## down subsequent activity on this table.  For that reason, we might want to also enable the
		## cost-based vacuum delay feature.  We might also want some extra configuration parameters
		## in the check_ganglia.conf file to control the vacuuming behavior.  Also note that we might
		## want to disable VACUUM ANALYZE on this one table by the autovacuum daemon, and run it much
		## less frequently, since it might degrade performance to no good effect (since the set of
		## MetricInstanceID and other possible join-column values in this table will likely be stable
		## for very long periods).  Note, though, that VACUUM ANALYZE is claimed to be a fast operation,
		## only using a sampling of the rows in a table, so we will need some empirical evidence here.
		##
		## We (separately) run the autovacuum daemon with default parameters, which will check each table
		## in each database once per minute, vacuum the table if more than 20% of the rows have been
		## deleted or updated, and run the vacuum with a cost delay of 20ms.  That will suffice for our
		## purposes relative to this table, so we don't implement our own explicit "VACUUM metricinstance"
		## command here.  The autovacuum daemon will analyze the table when more than 10% of the rows have
		## been modified, so that will happen all the time with this table, but if it's fast as claimed
		## (Section 23.1.3 of the documentation), we shouldn't care.  If necessary, the use of autovacuum,
		## and these scale factors, can be overridden on a per-table basis (Section 23.1.5 and the CREATE
		## TABLE command).
		##
		## NOTE:  The LastState and LastValue columns in the metricinstance table are text types, not
		## numeric types, so they logically need to be single-quoted in the list of values; but we are
		## not doing that either here or when @metric_values values are constructed.  The only reason
		## we are getting away with that is because the actual values of these two columns turn out to
		## always be numbers, and the database is able to coerce numbers into strings here without any
		## explicit quoting.  (It's not clear why these columns were declared as text instead of numeric
		## types to begin with.)
		$query = "update metricinstance as mi set
		    LastState=v.LastState, LastUpdateTime=v.LastUpdateTime,
		    LastStateChangeTime=v.LastStateChangeTime, LastValue=v.LastValue
		    from (values (@metric_values[$first..$last]) )
		    as v (MetricInstanceID,LastState,LastUpdateTime,LastStateChangeTime,LastValue)
		    where mi.MetricInstanceID=v.MetricInstanceID";
	    }
	    elsif ($is_mysql) {
		## We use the "insert ... on duplicate key update" statement in MySQL for performance reasons,
		## not because we ever do any inserts here.  This SQL construction supports a bulk-update
		## operation which is not available in a pure UPDATE command in MySQL.
		##
		## NOTE:  The LastState and LastValue columns in the metricinstance table are text types, not
		## numeric types, so they logically need to be single-quoted in the list of values; but we are
		## not doing that either here or when @metric_values values are constructed.  The only reason
		## we are getting away with that is because the actual values of these two columns turn out to
		## always be numbers, and the database is able to coerce numbers into strings here without any
		## explicit quoting.  (It's not clear why these columns were declared as text instead of numeric
		## types to begin with.)
		$query = "insert into metricinstance (MetricInstanceID,LastState,LastUpdateTime,LastStateChangeTime,LastValue)
		    values (@metric_values[$first..$last]) on duplicate key update
		    LastState=values(LastState), LastUpdateTime=values(LastUpdateTime),
		    LastStateChangeTime=values(LastStateChangeTime), LastValue=values(LastValue)";
	    }
	    else {
		## This is an intentionally bad command, since we don't know what database it's for.
		$query = "update metricinstance";
	    }
	    $" = $saved_list_separator;
	    # We don't dump out the entire query on error here, because we expect it to generally be huge.
	    $dbh->do($query) or print "ERROR: updating metricinstance table\n", $dbh->errstr, "\n";
	}
    }

    # It's not supposed to be necessary to unconditionally call $sth->finish() here, but without
    # the call we might get an apparently spurious warning message from the following disconnect.
    # Note that the statement handle will not have been defined above if we had no need to add any new
    # objects to the database, so we need to make sure we don't try to dereference an undefined handle.
    # Later note:  $sth->finish() is now called above as needed after each individual query, so there
    # should no longer be a need for a call at this point.
    # $sth->finish() if defined($sth);
    $dbh->disconnect();
}

sub readGangliaGMOND {
    $perfData{'ganglia_xml_sock_open_time'} = 0;
    $perfData{'ganglia_xml_message_size'}   = 0;
    $perfData{'ganglia_xml_bulk_read_time'} = 0;
    # Cycle through each GMOND and read the XML stream
    foreach (@gangliaHosts) {
	my $ganglia_host       = @{$_}[0];
	my $ganglia_gmond_port = @{$_}[1];
	# Open the socket and time the operation
	my $startTime = Time::HiRes::time();
	if ( ! ($socket = IO::Socket::INET->new(Proto    => "tcp",
						PeerAddr => $ganglia_host,
						PeerPort => $ganglia_gmond_port)) ) {
	    print "Can't open socket to host $ganglia_host port $ganglia_gmond_port: $!\n";
	    # Sleep a short while so we don't spin, chewing up CPU, when we are quickly restarted.
	    sleep 10;
	    exit 2;
	}
	my $stopTime = Time::HiRes::time();
	$perfData{'ganglia_xml_sock_open_time'} += ($stopTime - $startTime);
	# Read the XML stream and time the operation
	my $xml_string = '';
	$startTime = Time::HiRes::time();
	while (my $line=<$socket>) { $xml_string .= $line; }
	$stopTime = Time::HiRes::time();
	close $socket;
	print "WARNING: XML data stream from host $ganglia_host port $ganglia_gmond_port is empty!\n" if ($xml_string eq '');
	print "GMOND XML:\n".$xml_string."\n" if $DEBUG_DEBUG;
	push (@gangliaXMLString, $xml_string);
	$perfData{'ganglia_xml_message_size'}   += (length($xml_string) / (1024 * 1024));
	$perfData{'ganglia_xml_bulk_read_time'} += ($stopTime - $startTime);
    }
}

sub processGangliaXML {
    my $startTime = Time::HiRes::time();

    $perfData{'ganglia_cluster_count'}     = 0;
    $perfData{'ganglia_host_count'}        = 0;
    $perfData{'ganglia_metric_count'}      = 0;
    $perfData{'ganglia_metric_processed'}  = 0;
    $perfData{'num_nagios_messages'}       = 0;
    $perfData{'total_nagios_message_size'} = 0;
    $perfData{'num_nagios_pipe_writes'}    = 0;
    $perfData{'total_pipe_write_size'}     = 0;
    $perfData{'maximum_pipe_write_size'}   = 0;
    $perfData{'nagios_command_pipe_write_time'} = 0;

    $state_has_changed = 0;
    $state_is_stale = 0;
    $total_state_changes = 0;
    $total_stale_state_updates = 0;
    $throttle_state_change_count = 0;
    $must_update_nagios = 0;

    my $is_a_production_host = 1;

    # Process the XML retrieved from each GMOND
    foreach (@gangliaXMLString) {
	next if ($_ eq '');

	my $parser = XML::LibXML->new(
	    ext_ent_handler => sub { die "INVALID FORMAT: external entity references are not allowed in XML documents.\n" },
	    no_network      => 1
	);
	my $doc = undef;
	eval {
	    $doc = $parser->parse_string($_);
	};
	if ($@) {
	    my ($package, $file, $line) = caller;
	    # print STDERR $@, " called from $file line $line.";
	    ## FIX LATER:  HTMLifying here, along with embedded markup in print() output, is something of a hack,
	    ## as it presumes a context not in evidence.  But it's necessary in the browser context.
	    # $@ = HTML::Entities::encode($@);
	    # $@ =~ s/\n/<br>/g;
	    if ($@ =~ s/external entity callback died: // || $@ =~ /external entity references are not allowed/) {
		## First undo the effect of the croak() call in XML::LibXML.
		$@ =~ s/ at \S+ line \d+\n//;
		chomp $@;
		print "ERROR:  Bad XML string (parse_xml):\n$@\n";
	    }
	    elsif ($@ =~ /Attempt to load network entity/) {
		chomp $@;
		print "ERROR:  Bad XML string (parse_xml):\nINVALID FORMAT: non-local entity references are not allowed in XML documents.\n$@\n";
	    }
	    else {
		chomp $@;
		print "ERROR:  Bad XML string (parse_xml):\n$@ called from $file line $line.\n";
	    }
	}
	else {
	    my @nodes = ();

	    # Note:  The parsing of the XML stream DOM needs to be instrumented, and timing
	    # measurements taken, to compare possible alternatives for deconstructing the data.
	    # To begin with, should we adopt a configuration-file setting for whether the data
	    # feed from each configured source is a gmond or gmetad daemon, or should we always
	    # just look at the incoming data stream?  And how should we do such looking?
	    #
	    # The hierarchy of gmond  XML data is:  <GANGLIA_XML>        <CLUSTER> <HOST> <METRIC>
	    # The hierarchy of gmetad XML data is:  <GANGLIA_XML> <GRID> <CLUSTER> <HOST> <METRIC>
	    #
	    # The GANGLIA_XML tag, which is present in either type of data stream, looks like
	    # one of these:
	    #
	    # <GANGLIA_XML VERSION="3.0.3" SOURCE="gmond">
	    # <GANGLIA_XML VERSION="3.0.4" SOURCE="gmetad">
	    #
	    # So if we can get at the SOURCE field, we shouldn't have to go looking for a GRID
	    # node; we'll know immediately whether we should expect it.

	    # One thing that's not clear yet is how much searching the ->getElementsByTagName()
	    # and ->findnodes() methods perform in the document, and so how efficient these two
	    # mechanisms are for locating the nodes of interest.  Are calls to these two methods
	    # essentially interchangeable?  It's clear that ->getElementsByTagName() must dig an
	    # extra level into the document to find the GRID elements, so we might presume it
	    # will walk the entire DOM, which could be quite inefficient.  Because of that, we
	    # will instead use the alternate formulation of this code, starting first with the
	    # GANGLIA_XML node(s) and looking only one level down at each iteration.

	    # my $use_gmetad = 0;
	    # if ($use_gmetad) { @nodes = $doc->getElementsByTagName("GRID"); }
	    # else             { @nodes = $doc->findnodes("GANGLIA_XML");     }
	    @nodes = $doc->findnodes("GANGLIA_XML");

	    foreach my $node (@nodes) {
		my @grid_node_array = $node->findnodes("GRID");	# GRID will be in GMETAD but not GMOND.  Need to handle either case
		my @cluster_node_array = ();
		if ($#grid_node_array < 0) {			# If no grid, then querying GMOND
		    @cluster_node_array = $node->findnodes("CLUSTER");
		}
		else {
		    foreach my $grid (@grid_node_array) {		# Include this if looking at GMETAD node
			push @cluster_node_array, $grid->findnodes("CLUSTER");
		    }
		}
		foreach my $cluster (@cluster_node_array) {
		    if (my $cluster_name = $cluster->getAttribute('NAME')) {
			print "Found cluster: $cluster_name\n" if $DEBUG_INFO;
			if (scalar @gangliaClusters) {
			    my $monitorcluster_flag = 0;
			    foreach my $validcluster (@gangliaClusters) {
				if ($cluster_name =~ /^$validcluster$/i) {
				    $monitorcluster_flag = 1;
				    last;
				}
			    }
			    if ($monitorcluster_flag == 0) {
				print "Cluster $cluster_name not in monitored cluster array. Skipping.\n" if $DEBUG_INFO;
				next;	# Go to the next cluster
			    }
			    else {
				print "Cluster $cluster_name in monitored cluster array. Continue processing.\n" if $DEBUG_INFO;
			    }
			}
			$perfData{'ganglia_cluster_count'}++;
			foreach my $cluster_child ($cluster->findnodes("HOST")) {
			    if (my $host_name = $cluster_child->getAttribute('NAME')) {
				if ($find_non_production_hosts) {
				    # Use the short host name for a Ganglia host name, when doing the lookups to find out whether this is a production host.
				    my $short_host = $host_name;
				    if (($short_host !~ /^\d+\.\d+\.\d+\.\d+$/i) and ($short_host =~ /$short_hostname_pattern/io)) { $short_host = $1; }
				    $is_a_production_host = ! exists $nonProductionHostMapRef->{$short_host};
				}
				if ($is_a_production_host) {
				    my $ip_address = $cluster_child->getAttribute('IP');
				    my $host_last_report_time = $cluster_child->getAttribute('REPORTED');
				    $throttle_state_change_host_flag = 0;
				    print "\tFound host: $host_name\n" if $DEBUG_INFO;
				    $perfData{'ganglia_host_count'}++;
				    @crit_metrics     = ();	# Critical array
				    @warn_metrics     = ();	# Warning array
				    @ok_metrics       = ();	# OK array
				    @duration_metrics = ();	# Unknown array
				    $host_msg = '';		# Set output message for this host
				    # Config data text string
				    my $configstring = "Cluster $cluster_name, Host $host_name, IP $ip_address,";
				    print "XXXXX   $configstring\n" if $DEBUG_INFO;
				    my $metrics_ref = undef;
				    foreach my $host_child ($cluster_child->findnodes("METRIC")) {
					if (my $metric_name = $host_child->getAttribute('NAME')) {
					    print "\t\tFound metric $metric_name\n" if $DEBUG_DEBUG;
					    $perfData{'ganglia_metric_count'}++;
					    foreach my $metric_node ($host_child->getAttributes()) {
						next if ($metric_node->nodeName() eq 'NAME');
						print "\t\t\tAdding ".$metric_node->nodeName().", value ".$metric_node->nodeValue()."\n" if $DEBUG_DEBUG;
						# autovivification is used here to extend the reference tree; nodeName is one of the following,
						# not all of which we care about here:  VAL TYPE UNITS TN TMAX DMAX SLOPE SOURCE
						$metrics_ref->{$metric_name}->{$metric_node->nodeName()} = $metric_node->nodeValue();
					    }
					}
				    }

				    # Cycle through the metrics and process the thresholds
				    while (my ($metric_name, $metric_hashref) = each (%$metrics_ref)) {
					my ($value,$units) = (0,0);
					if (defined($metric_hashref->{UNITS})) {
					    $units = $metric_hashref->{UNITS};
					}
					if (defined($metric_hashref->{VAL})) {
					    $value = $metric_hashref->{VAL};
					}
					if ($metric_name =~ /(os_name|os_release|machine_type|cpu_num|cpu_speed|mem_total|swap_total)/) {
					    if ($units) {
						$configstring .= " $metric_name: $value $units,";
					    }
					    else {
						$configstring .= " $metric_name: $value,";
					    }
					    next;
					}
					if ($metric_name eq 'boottime') {
					    my ($sec,$min,$hour,$mday,$mon,$year,$wday,$yday,$isdst) = localtime($value);
					    # I personally dislike putting the time before the date, but that's what we're doing here.
					    my $bootstring = sprintf("%02d:%02d:%02d-%04d/%02d/%02d", $hour, $min, $sec, $year+1900, $mon+1, $mday);
					    # We want this as the last component of $configstring, and in practice that seems to happen,
					    # but I don't see any guarantees of that ordering here in the code.
					    $configstring .= " booted at $bootstring";
					    if ($output_reboot_warning) {
						# Generate a Nagios alert (just a warning, never critical) if the Ganglia-monitored "boottime" metric
						# has changed since the last probe (ignoring such effects during the first cycle when check_ganglia
						# starts up, if the previous boot time is not available).
						#
						# Currently, we don't take any special precautions to impose special throttling (beyond our normal
						# throttling) to handle all the boottime messages that will appear after a site-wide outage.
						process_boottime($cluster_name,$host_name,$value,$bootstring);
					    }
					    next;
					}
					# Continue processing only if there is a default threshold defined for this metric in the database.
					next if (!(is_metric_threshold_defined($cluster_name, $host_name, $metric_name)));

					$perfData{'ganglia_metric_processed'}++;
					process_metric($cluster_name,$host_name,$metric_name,$value,$units);
				    }

				    #
				    #   Process special calculated metrics here
				    #
				    my ($metric_name,$value,$units) = ();

				    if ($output_mem_free_percent) {
					if ($metrics_ref->{'mem_free'} and $metrics_ref->{'mem_total'}) {
					    $metric_name = "mem_free_percent";
					    if ($metrics_ref->{'mem_total'}->{VAL} > 0) {
						$value = ($metrics_ref->{'mem_free'}->{VAL} / $metrics_ref->{'mem_total'}->{VAL}) * 100;
					    }
					    else {
						$value = 0;
					    }
					    $value = sprintf("%0.2F", $value);
					    $units = '%';
					    process_metric($cluster_name,$host_name,$metric_name,$value,$units);
					}
				    }
				    if ($output_mem_cached_percent) {
					if ($metrics_ref->{'mem_cached'} and $metrics_ref->{'mem_total'}) {
					    $metric_name = "mem_cached_percent";
					    if ($metrics_ref->{'mem_total'}->{VAL} > 0) {
						$value = ($metrics_ref->{'mem_cached'}->{VAL} / $metrics_ref->{'mem_total'}->{VAL}) * 100;
					    }
					    else {
						$value = 0;
					    }
					    $value = sprintf("%0.2F", $value);
					    $units = '%';
					    process_metric($cluster_name,$host_name,$metric_name,$value,$units);
					}
				    }
				    if ($output_swap_free_percent) {
					if ($metrics_ref->{'swap_free'} and $metrics_ref->{'swap_total'}) {
					    $metric_name = "swap_free_percent";
					    if ($metrics_ref->{'swap_total'}->{VAL} > 0) {
						$value = ($metrics_ref->{'swap_free'}->{VAL} / $metrics_ref->{'swap_total'}->{VAL}) * 100;
					    }
					    else {
						# If the swap_total is zero, then 100% of it is free (vacuously);
						# this avoids warnings that might arise if we set the value to 0%.
						$value = 100;
					    }
					    $value = sprintf("%0.2F", $value);
					    $units = '%';
					    process_metric($cluster_name,$host_name,$metric_name,$value,$units);
					}
				    }
				    if ($output_time_since_last_update) {
					# Compute the time since Ganglia received an update for this host.
					$metric_name = "time_since_last_update";
					if ($host_last_report_time > 0) {
					    $value = time - $host_last_report_time;
					}
					else {
					    $value = 0;
					}
					$value = sprintf("%0.2F", $value);
					$units = 'secs';
					process_metric($cluster_name,$host_name,$metric_name,$value,$units);
				    }

				    #
				    #   Process special customer-specific metrics here
				    #
				    $custom_metrics->process_custom_metrics ($host_name, $host_last_report_time, $metrics_ref) if $process_custom_metrics;

				    #
				    #   End special calculated metrics
				    #

				    my $host_hashref = \%{ $metric_state->{CLUSTER}->{$cluster_name}->{HOST}->{$host_name} };
				    $host_hashref->{CONFIGSTRING} = $configstring;
				    # See if this host's IP address changed.
				    if (! defined($host_hashref->{IPADDRESS}) || $host_hashref->{IPADDRESS} ne $ip_address) {
					$host_hashref->{IPADDRESS} =  $ip_address;
					$host_hashref->{IPADDRESS_CHANGED} =  1;
				    }
				    else {
					$host_hashref->{IPADDRESS_CHANGED} =  0;
				    }

				    if ($state_is_stale) {
					$state_is_stale = 0;
					# We may have observed that the state was stale for one metric,
					# but had changed for another metric.  Let's not double-count.
					if (! $state_has_changed) {
					    $total_stale_state_updates++;
					}
				    }
				    if ($state_has_changed) {
					$state_has_changed = 0;
					$total_state_changes++;
				    }
				    if ($consolidate_metrics
					    and
					$must_update_nagios
					    and
					$send_to_nagios) {
					$must_update_nagios = 0;
					construct_process_consolidated_service_check_message
					    ($host_name,$host_msg,\@crit_metrics,\@warn_metrics,\@ok_metrics,\@duration_metrics);
					if ($throttle_state_change_host_flag) {
					    $throttle_state_change_count++;
					}
					if ($throttle_state_change_count >= $throttle_state_change_threshold) {
					    sendToNagios();
					    print "State change throttle threshold $throttle_state_change_threshold met. Sent to Nagios at ".
						(Time::HiRes::time() - $startTime)."\n";
					    $#bulk_messages = -1;	# truncate the array of Nagios messages
					    $throttle_state_change_count = 0;
					    # Temporarily hold up all further processing of all metrics for all hosts and all clusters,
					    # to allow the downstream processes time and resources to handle the load.
					    sleep $throttle_state_change_threshold_wait;
					}
				    }

				    # Send to Foundation
				    if ($consolidate_metrics and $send_to_foundation) {
					process_foundation_alarm($host_name,$host_msg,\@crit_metrics,\@warn_metrics,\@ok_metrics,\@duration_metrics);
				    }
				}
			    }
			    else {
				print "Cluster $cluster_name, Host with no name found\n";
			    }
			}
		    }
		    else {
			print "Cluster with no name found.\n";
		    }
		}
	    }
	}
    }
    my $stopTime = Time::HiRes::time();
    $perfData{'ganglia_processing_time'} = sprintf("%0.2F", $stopTime - $startTime);
}

# FIX MINOR:  This only checks if there is a CRITICAL threshold defined, not if there might be only a WARNING threshold defined.
# Is a warning-only check acceptable, and just not being allowed here?
sub is_metric_threshold_defined {
    my $cluster = shift;
    my $host    = shift;
    my $metric  = shift;
    if (defined($metric_thresh->{CLUSTER}->{$cluster}->{HOST}->{$host}->{METRIC}->{$metric}->{CRIT}) or
	defined($metric_thresh->{CLUSTER}->{$cluster}->{METRIC}->{$metric}->{CRIT})                  or
	defined($metric_thresh->{METRIC}->{$metric}->{CRIT})) {
	return 1;
    }
    return 0;
}

# This routine is similar to process_metric() below, but it avoids a lot of the complexity that
# doesn't apply to handling a change in boottime, both for efficiency and to avoid special-casing
# the logic in process_metric() to deal with differences in the handling of this metric.

sub process_boottime {
    my $cluster_name = shift;
    my $host_name    = shift;
    my $metric_name  = 'boottime';
    my $value        = shift;
    my $bootstring   = shift;
    my $sev          = 0;
    my $warnflag     = 0;
    my $okflag       = 0;

    # Force autovivification of the intermediate hashes without modifying the effective boolean value of this flag, even if it
    # was previously undefined, simply so that we can grab downstream hash references to make subsequent accesses efficient.
    my   $host_hashref = \%{ $metric_state->{CLUSTER}->{$cluster_name}->{HOST}->{$host_name} };
    my $metric_hashref = \%{ $host_hashref->{METRIC}->{$metric_name} };
    $metric_hashref->{DEFINED} |= 0;

    my $nagios_message = undef;
    my $prevboottime = $metric_hashref->{VALUE};

    # See if this is the first time we've seen this metric instance
    if (!($metric_hashref->{DEFINED})) {
	print "Instantiating in state reference $cluster_name, $host_name, $metric_name\n" if $DEBUG_INFO;
	$metric_hashref->{LASTSTATE}           = 3;	# Set to unknown
	$metric_hashref->{LASTSTATECHANGETIME} = time;
	$metric_hashref->{LASTUPDATETIME}      = time;
    }
    my $laststate           = $metric_hashref->{LASTSTATE};
    my $laststatechangetime = $metric_hashref->{LASTSTATECHANGETIME};
    my $lastupdatetime      = $metric_hashref->{LASTUPDATETIME};

    # Whether to make the duration test relative to the actual boot time or the last time we updated the state is a judgment call.
    # But to ensure we get the word out even if this script was down for awhile, we base the test on when we noticed the state change,
    # which might be well after the actual reboot.  But that's not quite good enough by itself, because we need to protect against
    # warning just because the last state-change we recorded was when we first noticed this host.
    if ((defined($prevboottime) and $value != $prevboottime) or
	($laststate == 1 and (time - $laststatechangetime) <= $output_reboot_warning_duration)) {
	$warnflag = 1;
    }
    else {
	$okflag = 1;
    }

    # Numeric severities are:  0 = OK, 1 = WARNING, 2 = CRITICAL, 3 = UNKNOWN, 4 = SOFT WARNING, 5 = SOFT CRITICAL
    if ($warnflag) {
	$sev = 1;
	print "Cluster $cluster_name, Host $host_name, last rebooted at $bootstring.\n" if $DEBUG_INFO;
	if ($consolidate_metrics) {
	    if ($consolidate_metrics_service_output_detail) {
		my $rebooted_how_long_ago = time - $value;
		$host_msg .= "<br>WARNING: last rebooted $bootstring, $rebooted_how_long_ago secs ago.";
	    }
	    else {
		$host_msg .= "<br>WARNING: reboot at $bootstring.";
	    }
	    push @warn_metrics, "boottime $bootstring";
	}
	else {
	    $nagios_message .= "WARNING: reboot at $bootstring.";
	}
    }
    elsif ($okflag) {
	$sev = 0;
	print "OK: Cluster $cluster_name, Host $host_name, last rebooted at $bootstring.\n" if $DEBUG_INFO;
	if ($consolidate_metrics) {
	    # Don't report anything for an OK state, mostly so we don't clutter up the consolidated service message.
	    # In the situation of consolidated metrics, we assume the reporting of some other component metric will
	    # suffice to clear a prior warning state of the boottime metric.
	    # if ($consolidate_metrics_service_output_detail) {
		# $host_msg .= "<br>OK: last rebooted $bootstring.";
	    # }
	    # else {
		# # Don't report anything for an OK state.
		# # $host_msg .= '';
	    # }
	    # push @ok_metrics, "OK: boottime $bootstring";
	}
	else {
	    $nagios_message .= "OK: last reboot at $bootstring.";
	}
    }

    # if the state for this metric didn't change, don't bother sending updates for it to the database or to nagios
    if (($laststate == $sev)
	    and
	($sev == 0 or $suppress_most_updates_for_persistent_non_okay_states)
	    and
	(!$send_updates_even_when_state_is_persistent)
	) {
	if ((time - $lastupdatetime) < $maximum_service_non_update_time) {
	    print "Don't update Cluster $cluster_name, Host $host_name, Metric $metric_name. No state change and max non-update time not exceeded.\n" if $DEBUG_INFO;
	    ## The resting value of $metric_hashref->{DBUPDATE_STATES} between passes is already zero (or undefined so far), so we don't
	    ## bother to modify it here.  Similarly, the value of the $must_update_nagios flag is already zero unless we already concluded
	    ## from processing some other metric that we must update Nagios for this host.  So here, we cannot allow the fact that there's
	    ## nothing to do for this one metric override whatever we might have needed to do for other, previously-processed metrics for
	    ## this host during this pass, and we don't set $must_update_nagios to any particular value now.
	    return;
	}
	$state_is_stale = 1;
    }
    if ($laststate != $sev) {
	$throttle_state_change_host_flag = 1;
	$metric_hashref->{LASTSTATE}           = $sev;
	$metric_hashref->{LASTSTATECHANGETIME} = time;
	$state_has_changed = 1;
    }
    $metric_hashref->{VALUE}           = $value;
    $metric_hashref->{LASTUPDATETIME}  = time;
    $metric_hashref->{DBUPDATE_STATES} = 1;
    $must_update_nagios = 1;
    if (defined($prevboottime) and $value != $prevboottime) {
	# Force a database update, to reflect the new boottime and possibly other usually-constant-but-might-now-have-changed metric data.
	$host_hashref->{BOOTTIME_CHANGED} = 1;
    }
    if (!$consolidate_metrics and $send_to_nagios) {
	construct_process_service_check_message($host_name, "ganglia_$metric_name", $sev, $nagios_message);
    }
    return;
}

sub process_metric {
    my $cluster_name = shift;
    my $host_name    = shift;
    my $metric_name  = shift;
    my $value        = shift;
    my $units        = shift;
    my $warn         = undef;
    my $crit         = undef;
    my $sev          = 0;
    my $comparegt    = 1;		# "compare greater than" is default
    my $comparetext  = "greater than";
    my $critflag     = 0;
    my $warnflag     = 0;
    my $okflag       = 0;
    my $duration     = 0;

    # FIX MINOR:  The statements here are ripe for further factoring out of hashref chains.
    # If we do so, we want to watch out for unintended autovivification.

    my $CLUSTER_hashref     = \%{ $metric_thresh->{CLUSTER} };
    my $HOST_hashref        = \%{ $CLUSTER_hashref->{$cluster_name}->{HOST} };
    my $HOST_METRIC_hashref = \%{ $HOST_hashref->{$host_name}->{METRIC} };

    # Find the critical threshold for this cluster,host,metric
    # If host level threshold exists, use it:
    if ( defined( $HOST_METRIC_hashref->{$metric_name}->{CRIT} ) ) {
	$crit =   $HOST_METRIC_hashref->{$metric_name}->{CRIT};
    }
    ## Else if cluster level threshold exists, use it:
    elsif ( defined( $CLUSTER_hashref->{$cluster_name}->{METRIC}->{$metric_name}->{CRIT} ) ) {
	$crit =      $CLUSTER_hashref->{$cluster_name}->{METRIC}->{$metric_name}->{CRIT};
    }
    ## Else if global level threshold exists, use it:
    elsif ( defined( $metric_thresh->{METRIC}->{$metric_name}->{CRIT} ) ) {
	$crit =      $metric_thresh->{METRIC}->{$metric_name}->{CRIT};
    }

    # Do the same for the warning threshold
    # If host level threshold exists, use it:
    if ( defined( $HOST_METRIC_hashref->{$metric_name}->{WARN} ) ) {
	$warn =   $HOST_METRIC_hashref->{$metric_name}->{WARN};
    }
    ## Else if cluster level threshold exists, use it:
    elsif ( defined( $CLUSTER_hashref->{$cluster_name}->{METRIC}->{$metric_name}->{WARN} ) ) {
	$warn =      $CLUSTER_hashref->{$cluster_name}->{METRIC}->{$metric_name}->{WARN};
    }
    ## Else if global level threshold exists, use it:
    elsif ( defined( $metric_thresh->{METRIC}->{$metric_name}->{WARN} ) ) {
	$warn =      $metric_thresh->{METRIC}->{$metric_name}->{WARN};
    }

    # Do the same for the duration threshold
    # If host level threshold exists, use it:
    if ( defined(   $HOST_METRIC_hashref->{$metric_name}->{DURATION} ) ) {
	$duration = $HOST_METRIC_hashref->{$metric_name}->{DURATION};
    }
    ## Else if cluster level threshold exists, use it:
    elsif ( defined( $CLUSTER_hashref->{$cluster_name}->{METRIC}->{$metric_name}->{DURATION} ) ) {
	$duration =  $CLUSTER_hashref->{$cluster_name}->{METRIC}->{$metric_name}->{DURATION};
    }
    ## Else if global level threshold exists, use it:
    elsif ( defined( $metric_thresh->{METRIC}->{$metric_name}->{DURATION} ) ) {
	$duration =  $metric_thresh->{METRIC}->{$metric_name}->{DURATION};
    }

    if (!defined($value)) {
	print "UNKNOWN: Cluster $cluster_name, Host $host_name, Metric $metric_name - No value for this metric.\n"  if $DEBUG_INFO;
	# push @unknown_metrics, "$metric_name (No value)";
    }
    else {
	if (defined($crit) and !defined($warn)) {
	    $warn = $crit;
	}
	if (!defined($warn)) {
	    print "UNKNOWN: Cluster $cluster_name, Host $host_name, Metric $metric_name value $value - No thresholds defined.\n" if $DEBUG_INFO;
	    # push @unknown_metrics, "$metric_name ($value $units)";
	}
	else {
	    if ($value !~ /^[\d\.]+$/) {
		print "UNKNOWN: Cluster $cluster_name, Host $host_name, Metric $metric_name value $value is a non-numeric value.\n"  if $DEBUG_ERROR;
		# $sev = 2;
		# push @unknown_metrics, "$metric_name ($value $units non-numeric)";
	    }
	    else {
		if (defined($crit)) {
		    if ($warn > $crit) {
			$comparegt = 0;		# If the warning is greater than critical, then values should be less than thresholds
			$comparetext = "less than";
		    }
		    if ($comparegt) {
			if ($value >= $crit) {
			    $critflag = 1;
			}
			elsif ($value >= $warn) {
			    $warnflag = 1;
			}
			else {
			    $okflag = 1;
			}
		    }
		    else {
			if ($value <= $crit) {
			    $critflag = 1;
			}
			elsif ($value <= $warn) {
			    $warnflag = 1;
			}
			else {
			    $okflag = 1;
			}
		    }
		}
		else {
		    if ($comparegt) {
			if ($value >= $warn) {
			    $warnflag = 1;
			}
			else {
			    $okflag = 1;
			}
		    }
		    else {
			if ($value <= $warn) {
			    $warnflag = 1;
			}
			else {
			    $okflag = 1;
			}
		    }
		}
	    }
	}
    }

    # Force autovivification of the intermediate hashes without modifying the effective boolean value of this flag, even if it
    # was previously undefined, simply so that we can grab downstream hash references to make subsequent accesses efficient.
    my   $host_hashref = \%{ $metric_state->{CLUSTER}->{$cluster_name}->{HOST}->{$host_name} };
    my $metric_hashref = \%{ $host_hashref->{METRIC}->{$metric_name} };
    $metric_hashref->{DEFINED} |= 0;

    my $nagios_message = undef;
    # See if this is the first time we've seen this metric instance
    if (!($metric_hashref->{DEFINED})) {
	print "Instantiating in state reference $cluster_name, $host_name, $metric_name\n" if $DEBUG_INFO;
	$metric_hashref->{LASTSTATE}           = 3;	# Set to unknown
	$metric_hashref->{LASTSTATECHANGETIME} = time;
	$metric_hashref->{LASTUPDATETIME}      = time;
    }
    my $laststate           = $metric_hashref->{LASTSTATE};
    my $laststatechangetime = $metric_hashref->{LASTSTATECHANGETIME};
    my $lastupdatetime      = $metric_hashref->{LASTUPDATETIME};
    my $current_duration    = time - $laststatechangetime;
    $crit = defined ($crit) ? ("threshold " . format_number($crit)) : "undefined threshold";
    $warn = defined ($warn) ? ("threshold " . format_number($warn)) : "undefined threshold";

    # Numeric severities are:  0 = OK, 1 = WARNING, 2 = CRITICAL, 3 = UNKNOWN, 4 = SOFT WARNING, 5 = SOFT CRITICAL
    if ($critflag) {
	if (($laststate != 2) and ($laststate != 5)) {
	    $current_duration = 0;
	}
	#
	#	Process state transitions
	#
	if ($laststate == 2) {
	    $sev = 2;
	}
	elsif ($laststate == 5) {
	    $sev = ($current_duration >= $duration) ? 2 : 5;
	}
	elsif ($laststate == 0) {
	    ## If the duration threshold is 0, then sev is set to 2.  Else set to 5.
	    $sev = ($current_duration >= $duration) ? 2 : 5;
	}
	elsif ($laststate == 4) {
	    $sev = ($current_duration >= $duration) ? 2 : 5;
	}
	elsif ($laststate == 1) {
	    $sev = ($current_duration >= $duration) ? 2 : 5;
	}
	elsif ($laststate == 3) {
	    $sev = ($current_duration >= $duration) ? 2 : 5;
	}
	else {
	    $sev = 2;	# Shouldn't get here
	}
	#
	# Now process state
	#
	if ($sev == 2) {
	    $duration = format_number($duration);
	    print "Cluster $cluster_name, Host $host_name, Metric $metric_name value $value $comparetext $crit.\n" if $DEBUG_INFO;
	    if ($consolidate_metrics) {
		if ($consolidate_metrics_service_output_detail) {
		    $host_msg .= "<br>CRITICAL: $metric_name value $value $units $comparetext $crit, duration $current_duration secs.";
		}
		else {
		    $host_msg .= "<br>CRITICAL: $metric_name $value $units duration $current_duration secs.";
		}
		push @crit_metrics, "$metric_name $value $units $comparetext $crit";
	    }
	    else {
		$nagios_message .= "CRITICAL: $metric_name $value $units $comparetext $crit.";
	    }
	}
	else {
	    # sev should be 5 here
	    $duration = format_number($duration);
	    if ($consolidate_metrics) {
		if ($consolidate_metrics_service_output_detail) {
		    $host_msg .= "<br>$metric_name value $value $units $comparetext $crit but duration $current_duration hasn't reached threshold of $duration secs yet.";
		}
		else {
		    # Don't report anything yet; we're still in a soft-critical state.
		    # $host_msg .= '';
		}
		push @duration_metrics, "$metric_name value $value $units $comparetext $crit but duration $current_duration hasn't reached threshold of $duration secs yet.";
	    }
	    else {
		$nagios_message .= "$metric_name value $value $units $comparetext $crit but duration $current_duration hasn't reached threshold of $duration secs yet.";
	    }
	}
    }
    elsif ($warnflag) {
	if (($laststate != 1) and ($laststate != 4)) {
	    $current_duration = 0;
	}
	##
	##	Determine state transitions
	##
	if ($laststate == 0) {
	    $sev = ($current_duration >= $duration) ? 1 : 4;
	}
	elsif ($laststate == 1) {
	    $sev = 1;
	}
	elsif ($laststate == 2) {
	    $sev = 1;
	}
	elsif ($laststate == 3) {
	    $sev = ($current_duration >= $duration) ? 1 : 4;
	}
	elsif ($laststate == 4) {
	    $sev = ($current_duration >= $duration) ? 1 : 4;
	}
	elsif ($laststate == 5) {
	    $sev = ($current_duration >= $duration) ? 1 : 4;
	}
	else {
	    $sev = 1;	# Shouldn't get here
	}
	if ($sev == 1) {
	    print "Cluster $cluster_name, Host $host_name, Metric $metric_name value $value $units $comparetext $warn.\n" if $DEBUG_INFO;
	    if ($consolidate_metrics) {
		if ($consolidate_metrics_service_output_detail) {
		    $host_msg .= "<br>WARNING: $metric_name value $value $units $comparetext $warn, duration $current_duration secs.";
		}
		else {
		    $host_msg .= "<br>WARNING: $metric_name $value $units duration $current_duration secs.";
		}
		push @warn_metrics, "$metric_name $value $units $comparetext $warn";
	    }
	    else {
		$nagios_message .= "WARNING: $metric_name $value $units $comparetext $warn.";
	    }
	}
	else {
	    # sev should be 4 here
	    $duration = format_number($duration);
	    if ($consolidate_metrics) {
		if ($consolidate_metrics_service_output_detail) {
		    $host_msg .= "<br>$metric_name value $value $units $comparetext $warn but duration $current_duration hasn't reached threshold of $duration secs yet.";
		}
		else {
		    # Don't report anything yet; we're still in a soft-warning state.
		    # $host_msg .= '';
		}
		push @duration_metrics, "$metric_name value $value $units $comparetext $warn but duration $current_duration hasn't reached threshold of $duration secs yet.";
	    }
	    else {
		$nagios_message .= "$metric_name value $value $units $comparetext $warn but duration $current_duration hasn't reached threshold of $duration secs yet.";
	    }
	}
    }
    elsif ($okflag) {
	$sev = 0;
	print "OK: Cluster $cluster_name, Host $host_name, Metric $metric_name value $value $units within thresholds (WARNING $warn, CRITICAL $crit).\n" if $DEBUG_INFO;
	if ($consolidate_metrics) {
	    if ($consolidate_metrics_service_output_detail) {
		$host_msg .= "<br>OK: $metric_name value $value $units.";
	    }
	    else {
		# Don't report anything for an OK state.
		# $host_msg .= '';
	    }
	    push @ok_metrics, "OK: $metric_name $value $units";
	}
	else {
	    $nagios_message .= "OK: $metric_name value $value $units within thresholds.";
	}
    }

    # if the state for this metric didn't change, don't bother sending updates for it to the database or to nagios
    if (($laststate == $sev)
	    and
	($sev == 0 or $suppress_most_updates_for_persistent_non_okay_states)
	    and
	(!$send_updates_even_when_state_is_persistent)
	) {
	if ((time - $lastupdatetime) < $maximum_service_non_update_time) {
	    print "Don't update Cluster $cluster_name, Host $host_name, Metric $metric_name. No state change and max non-update time not exceeded.\n" if $DEBUG_INFO;
	    # The resting value of $metric_hashref->{DBUPDATE_STATES} between passes is already zero (or undefined so far), so we don't bother to modify it here.
	    # Similarly, the value of the $must_update_nagios flag is already zero unless we already concluded from processing some other metric that we must update
	    # Nagios for this host.  So here, we cannot allow the fact that there's nothing to do for this one metric override whatever we might have needed to do
	    # for other, previously-processed metrics for this host during this pass, and we don't set $must_update_nagios to any particular value now.
	    return;
	}
	$state_is_stale = 1;
    }
    if ($laststate != $sev) {
	$throttle_state_change_host_flag = 1;
	$metric_hashref->{LASTSTATE}           = $sev;
	$metric_hashref->{LASTSTATECHANGETIME} = time;
	$state_has_changed = 1;
    }
    $metric_hashref->{VALUE}           = $value;
    $metric_hashref->{LASTUPDATETIME}  = time;
    $metric_hashref->{DBUPDATE_STATES} = 1;
    $must_update_nagios= 1;
    if (!$consolidate_metrics and $send_to_nagios) {
	construct_process_service_check_message($host_name, "ganglia_$metric_name", $sev, $nagios_message);
    }
    return;
}

# Note:  dealing with the Nagios command pipe is fraught with possible race conditions.
# We deal with one of them here:  namely, that we might open a file descriptor and write
# to it, only to block during that write and have no reader ever come around to read it.
# To get around that, we set an alarm so we can break out of an otherwise infinite wait.
# Our approach to handling the alarm is distinctly unsophisticated:  we simply die.
# That's because catching the signal and trying to continue would mean that we would
# not have sent state updates to Nagios, but we would continue on and write those
# state changes to the "ganglia" database.  So these two representations would get
# out of sync and we might never realize we needed to subsequently send the missing
# state updates to Nagios.  Better that we should die and get automatically restarted,
# so we can take a clean shot at it again.

# Some of the timeout support here is left over from a version where we did not use
# the :unix discipline on the FIFO stream.  We could probably simplify the code now,
# and simply die directly out of the signal handler.

sub quit_instantly {
    my $message = shift;

    # Let's try to get a notation in the log file, to help with forensic work.
    $| = 1;	# force output flushing before we quit
    print "$message\n";

    kill "TERM", $$;
}

sub catch_signal {
    my $signame = shift;

    # Here's the weird thing we have to cope with.  If we just return from this signal handler
    # in a non-eval context, Perl would just restart the system call on which we were hung.  If
    # instead we die here and use an eval context to catch the error and try to finish up by
    # shutting down gracefully with a subsequent "die" or "exit", Perl tries to close the FIFO
    # file descriptor, and if we're using buffered i/o, the close will try to flush the buffer
    # and that write won't complete until the write completes, which means the process will
    # hang again, outside the control of an alarm context.  If we try to send SIGTERM to this
    # process, that will only work if we don't have a signal handler in place for that signal
    # that likewise tries to die or exit.

    quit_instantly "=== Writing to the Nagios command pipe timed out; caught a SIG$signame signal; exiting! ===";
}

sub sendToNagios {
    my $startTime;
    my $stopTime;
    $startTime = Time::HiRes::time();
    if ((! $suppress_updates) && scalar(@bulk_messages)) {
	# We don't want to open an actual file if the expected pipe does not exist.  The workaround is
	# this strange '+<" open mode that allows us write access, but won't create a nonexisting file.
	# The :unix discipline says we should perform unbuffered i/o.  This helps if we ever try to exit
	# gracefully after a timeout, so buffering doesn't kick in again causing the program to re-execute
	# a failed write operation and quite likely hang again.  It should also avoid the extra overhead
	# of copying from our string into an i/o buffer.
	open(FIFO, '+<:unix', $nagios_cmd_pipe) or die "Could not open the Nagios command pipe: $!";
	local $SIG{ALRM} = \&catch_signal;
	# To guarantee atomicity of the pipe writes, we can write no more than PIPE_BUF bytes in a single write operation.
	# This avoids having the pipe reader interleave messages from multiple sources at places other than message boundaries.
	# If this script is configured to write to some intermediate pipe or socket which is known to be private to this one
	# script, the PIPE_BUF limitation can be relaxed and the atomic-write constraint deferred to the downstream process
	# that finally writes to the Nagios command pipe.  Hence we make the max write size used here configurable.
	my $first = 0;
	my $last = $first;
	my $message_size;
	my $buffer_size = 0;
	my $index_past_end = scalar(@bulk_messages);
	for (my $index = 0; $index <= $index_past_end; ++$index) {
	    if ($index < $index_past_end) {
		$message_size = length ($bulk_messages[$index]);
	    }
	    else {
		$message_size = 0;
	    }
	    if ($index < $index_past_end && $buffer_size + $message_size <= $max_command_pipe_write_size) {
	        $buffer_size += $message_size;
	    }
	    else {
		if ($buffer_size > 0) {
		    $perfData{'num_nagios_pipe_writes'}++;
		    $perfData{'total_pipe_write_size'} += $buffer_size;
		    $perfData{'maximum_pipe_write_size'} = 0 if not defined $perfData{'maximum_pipe_write_size'};
		    $perfData{'maximum_pipe_write_size'} = $buffer_size if $buffer_size > $perfData{'maximum_pipe_write_size'};
		    alarm($max_command_pipe_wait_time);
		    eval {
			print FIFO join('', @bulk_messages[$first..$last]) or die "Cannot write to the Nagios command pipe: $!";
		    };
		    alarm(0);
		    quit_instantly "Exiting: $@" if ($@);	# skip closing the FIFO stream, as that may hang and we want to stop executing
		}
		$first = $index;
		$buffer_size = $message_size;
	    }
	    $last = $index;
	}
	close(FIFO);
    }
    $stopTime = Time::HiRes::time();
    $perfData{'nagios_command_pipe_write_time'} += ($stopTime - $startTime);
}

sub calculateStatistics {
    my $xml_read_speed = 0;
    if ($perfData{'ganglia_xml_bulk_read_time'} > 0) {
	$xml_read_speed = $perfData{'ganglia_xml_message_size'} / $perfData{'ganglia_xml_bulk_read_time'};
    }
    $perfData{'ganglia_xml_sock_open_time'} = sprintf("%0.2F", $perfData{'ganglia_xml_sock_open_time'} * 1000);
    $perfData{'ganglia_xml_message_size'}   = sprintf("%0.2F", $perfData{'ganglia_xml_message_size'});
    $perfData{'ganglia_xml_bulk_read_time'} = sprintf("%0.2F", $perfData{'ganglia_xml_bulk_read_time'});
    $perfData{'ganglia_xml_read_speed'}     = sprintf("%0.2F", $xml_read_speed);

    if ($perfData{'num_nagios_messages'} > 0) {
	$perfData{'avg_nagios_message_size'} = ($perfData{'total_nagios_message_size'} / $perfData{'num_nagios_messages'});
    }
    else {
	$perfData{'avg_nagios_message_size'} = 0;
    }
    if ($perfData{'num_nagios_pipe_writes'} > 0) {
	$perfData{'average_pipe_write_size'} = ($perfData{'total_pipe_write_size'} / $perfData{'num_nagios_pipe_writes'});
    }
    else {
	$perfData{'average_pipe_write_size'} = 0;
    }
    $perfData{'total_nagios_message_size'} = sprintf("%0.2F", ($perfData{'total_nagios_message_size'} / (1024 * 1024)));
    $perfData{'avg_nagios_message_size'}   = sprintf("%0.0F", $perfData{'avg_nagios_message_size'});
    $perfData{'total_pipe_write_size'}     = sprintf("%0.2F", ($perfData{'total_pipe_write_size'} / (1024 * 1024)));
    $perfData{'average_pipe_write_size'}   = sprintf("%0.0F", $perfData{'average_pipe_write_size'});

    if ($perfData{'ganglia_host_count'} > 0) {
	$perfData{'ganglia_metrics_observed_per_host'}  = sprintf("%0.1F", ($perfData{'ganglia_metric_count'}     / $perfData{'ganglia_host_count'}));
	$perfData{'ganglia_metrics_processed_per_host'} = sprintf("%0.1F", ($perfData{'ganglia_metric_processed'} / $perfData{'ganglia_host_count'}));
    }
    else {
	$perfData{'ganglia_metrics_observed_per_host'}  = 0;
	$perfData{'ganglia_metrics_processed_per_host'} = 0;
    }
#   $perfData{'total_execution_time'} = sprintf("%0.2F", ($perfData{'ganglia_processing_time'} + $perfData{'nagios_command_pipe_write_time'}));
    $perfData{'total_execution_time'} = sprintf("%0.2F", (Time::HiRes::time() - $startTime));

    if ($perfData{'nagios_command_pipe_write_time'} > 0) {
	$perfData{'nagios_insertion_speed_bps'} = ($perfData{'total_nagios_message_size'} / $perfData{'nagios_command_pipe_write_time'});
    }
    else {
	$perfData{'nagios_insertion_speed_bps'} = 0;
    }

    $perfData{'nagios_insertion_speed_bps'} = sprintf("%0.2F", $perfData{'nagios_insertion_speed_bps'});

    if ($perfData{'nagios_command_pipe_write_time'} > 0) {
	$perfData{'nagios_insertion_speed_sps'} = ($perfData{'num_nagios_messages'} / $perfData{'nagios_command_pipe_write_time'});
    }
    else {
	$perfData{'nagios_insertion_speed_sps'} = 0;
    }

    $perfData{'nagios_command_pipe_write_time'} = sprintf("%0.2F", $perfData{'nagios_command_pipe_write_time'});

    $perfData{'nagios_insertion_speed_sps'} = sprintf("%0.0F",  $perfData{'nagios_insertion_speed_sps'});
    $perfData{'nagios_insertion_speed_spm'} = sprintf("%0.0F", ($perfData{'nagios_insertion_speed_sps'} * 60));
    $perfData{'nagios_insertion_speed_sph'} = sprintf("%0.0F", ($perfData{'nagios_insertion_speed_sps'} * 60 * 60));

    # FIX MINOR:  there is something wrong with the statistics we're putting out.
    #     Total Nagios Message Size:      2.67 MB
    #     Total Nagios Pipe Write Size:   3.49 MB
    # These two numbers should be equal.
}

sub printStatistics {
    print "----------------------------------------------------------------\n";
    print "Statistics\n";
    print "----------------------------------------------------------------\n";
    print "Time = ".time."\n";
    print "[Database]\n";
    if (defined($perfData{'ganglia_db_initial_state_read_time'})) {	# These are only done on startup, not on every loop
	print "    Ganglia Initial State Database Read Time:   " . $perfData{'ganglia_db_initial_state_read_time'}     . " milliseconds\n";
	print "    Ganglia Initial State Cluster Count:        " . $perfData{'ganglia_db_initial_state_cluster_count'} . "\n";
	print "    Ganglia Initial State Host Count:           " . $perfData{'ganglia_db_initial_state_host_count'}    . "\n";
	print "    Ganglia Initial State Metric Count:         " . $perfData{'ganglia_db_initial_state_metric_count'}  . "\n";
    }

    print "    Ganglia Threshold Database Read Time:       " . $perfData{'ganglia_db_threshold_read_time'} . " milliseconds\n";
    print "    Ganglia State Database Update Time:         " . $perfData{'ganglia_db_update_time'} . " milliseconds\n";
    if (defined($perfData{'ganglia_db_metric_update'})) {
	print "    Ganglia State Database Update Metric Count: " . $perfData{'ganglia_db_metric_update'} . "\n";
    }
    print "\n";
    print "[Input]\n";
    print "    Ganglia Socket Open Time: " . $perfData{'ganglia_xml_sock_open_time'} . " milliseconds\n";
    print "    Ganglia XML Message Size: " . $perfData{'ganglia_xml_message_size'}   . " MB\n";
    print "    Ganglia XML Read Time:    " . $perfData{'ganglia_xml_bulk_read_time'} . " seconds\n";
    print "    Ganglia XML Read Speed:   " . $perfData{'ganglia_xml_read_speed'}     . " MB per second\n";
    print "\n";
    print "[Processing]\n";
    print "    Number of Ganglia Clusters:          " . $perfData{'ganglia_cluster_count'}              . "\n";
    print "    Number of Ganglia Hosts:             " . $perfData{'ganglia_host_count'}                 . "\n";
    print "    Number of Ganglia Metrics Observed:  " . $perfData{'ganglia_metric_count'}               . " (" .
							$perfData{'ganglia_metrics_observed_per_host'}  . " metrics per host)\n";
    print "    Number of Ganglia Metrics Processed: " . $perfData{'ganglia_metric_processed'}           . " (" .
							$perfData{'ganglia_metrics_processed_per_host'} . " metrics per host)\n";
    print "\n";
    print "[Output]\n";
    print "    Number of Nagios Messages:      " . $perfData{'num_nagios_messages'}       . "\n";
    print "    Total Nagios Message Size:      " . $perfData{'total_nagios_message_size'} . " MB\n";
    print "    Average Nagios Message Size:    " . $perfData{'avg_nagios_message_size'}   . " bytes\n";
    print "    Number of Nagios Pipe Writes:   " . $perfData{'num_nagios_pipe_writes'}    . "\n";
    print "    Total Nagios Pipe Write Size:   " . $perfData{'total_pipe_write_size'}     . " MB\n";
    print "    Maximum Nagios Pipe Write Size: " . $perfData{'maximum_pipe_write_size'}   . " bytes\n";
    print "    Average Nagios Pipe Write Size: " . $perfData{'average_pipe_write_size'}   . " bytes\n";
    print "\n";
    print "[Execution]\n";
    print "    Ganglia XML Processing Time:             " . $perfData{'ganglia_processing_time'} . " seconds\n";
    print "    Nagios Command Pipe Write Time:          " . $perfData{'nagios_command_pipe_write_time'} . " seconds\n";
    print "    Number of State Changes:                 " . $total_state_changes . "\n";
    print "    Number of Forced Stale-State Updates:    " . $total_stale_state_updates . "\n";

    print "    Total Script Execution Time:             " . $perfData{'total_execution_time'} . " seconds\n";
    print "\n";
    print "    Nagios Insertion Speed [Data]:           " . $perfData{'nagios_insertion_speed_bps'} . " MB per second\n";
    print "    Nagios Insertion Speed [Service Checks]: " . $perfData{'nagios_insertion_speed_sps'} . " service checks per second\n";
    print "    Nagios Insertion Speed [Service Checks]: " . $perfData{'nagios_insertion_speed_spm'} . " service checks per minute\n";
    print "    Nagios Insertion Speed [Service Checks]: " . $perfData{'nagios_insertion_speed_sph'} . " service checks per hour\n";
    print "\n";
    print "[Foundation]\n";
    if (defined($perfData{'num_foundation_messages'})) {
	print "    Number of services updates:  " . $perfData{'num_foundation_messages'} . "\n";
    }
    else {
	print "    Number of services updates:  None\n";
    }
    if (defined($perfData{'num_send_to_foundation_messages'})) {
	print "    Number of bulk sends to Foundation:  " . $perfData{'num_send_to_foundation_messages'} . "\n";
    }
    else {
	print "    Number of Foundation sends:  None\n";
    }

    print "----------------------------------------------------------------\n";
    print "\n\n";
}

sub construct_process_service_check_message {
    my $host      = shift;
    my $service   = shift;
    my $severity  = shift;
    my $message   = shift;

    my $datetime  = time;

    if ($severity == 4) {
	$severity = 0;
    }
    elsif ($severity == 5) {
	$severity = 0;
    }

    if (($host !~ /^\d+\.\d+\.\d+\.\d+$/i) and ($host =~ /$short_hostname_pattern/io)) { $host = $1; }

    $perfData{'num_nagios_messages'}++;
    $perfData{'total_nagios_message_size'} += length($message);

    push @bulk_messages, "[$datetime] PROCESS_SERVICE_CHECK_RESULT;$host;$service;$severity;$message\n";
}

sub format_number {
    # Format number for printing
    my $number = shift;
    $number =~ s/(\.\d*?)0+$/$1/;	# Get rid of trailing 0s
    $number =~ s/\.$//;			# Get rid of trailing decimal point
    return $number;
}

sub construct_process_consolidated_service_check_message {
    my $host           = shift;
    my $msg            = shift;
    my $crit_array     = shift;
    my $warn_array     = shift;
    my $ok_array       = shift;
    my $duration_array = shift;
    my $nagiossev      = 0;
    my $postmsg        = undef;
    my $service        = $consolidate_metrics_service_name;

    my $newmsg = "Alarm Counts:";
    if ($#$crit_array >= 0) {
	$newmsg .= "CRITICAL (".($#$crit_array+1).") ";
#	$newmsg .= "<FONT COLOR=RED>CRITICAL (".($#$crit_array+1)."),</FONT> ";
	$nagiossev = 2;
#	$postmsg .= "<br><FONT COLOR=RED>Critical</FONT>: ";
#	foreach my $metric (sort @$crit_array) {
#	    $postmsg .= "$metric,";
#	}
    }
    if ($#$warn_array >= 0) {
	$newmsg .= "WARNING (".($#$warn_array+1).") ";
#	$newmsg .= "<FONT COLOR=YELLOW>WARNING (".($#$warn_array+1)."),</FONT> ";
	if ($nagiossev < 2) {
	    $nagiossev = 1;
	}
#	$postmsg .= "<br><FONT COLOR=YELLOW>Warning</FONT>: ";
#	foreach my $metric (sort @$warn_array) {
#	    $postmsg .= "$metric,";
#	}
    }
    if (($#$ok_array >= 0) or ($#$duration_array >= 0)) {
	$newmsg .= "OK (".($#$ok_array+1 + $#$duration_array+1).") ";
#	$newmsg .= "<FONT COLOR=GREEN>OK (".($#$ok_array+1 + $#$duration_array+1)."),</FONT> ";
#   Some customers don't want to see OK metrics.  If we ever need to re-enable this,
#   it will have to be done under control of a configuration option, not unconditionally.
#	$postmsg .= "<br><FONT COLOR=GREEN>OK</FONT>: ";
#	foreach my $metric (sort @$ok_array) {
#	    $postmsg .= "$metric,";
#	}
#	foreach my $metric (sort @$duration_array) {
#	    $postmsg .= "$metric,";
#	}
    }

    # $msg = $newmsg . $postmsg;
    $msg = $newmsg . $msg;

    if (($host !~ /^\d+\.\d+\.\d+\.\d+$/i) and ($host =~ /$short_hostname_pattern/io)) { $host = $1; }

    my $datetime = time;
    push @bulk_messages, "[$datetime] PROCESS_SERVICE_CHECK_RESULT;$host;$service;$nagiossev;$msg\n";

    $perfData{'num_nagios_messages'}++;
    $perfData{'total_nagios_message_size'} += length($msg);

    if ($send_after_each_service and $send_to_nagios) {
	sendToNagios();
	print "Sent " . ($#bulk_messages + 1) . " messages to Nagios." if $DEBUG_INFO;
	print join ('', @bulk_messages) if $DEBUG_DEBUG;
	$#bulk_messages = -1;	# truncate the array of Nagios messages
    }
    return;
}

sub sendToFoundation {
    my $remote_host = shift;
    my $remote_port = shift;
    if (! $suppress_updates) {
	if ( $socket = IO::Socket::INET->new(PeerAddr => $remote_host,
					     PeerPort => $remote_port,
					     Proto    => "tcp",
					     Type     => SOCK_STREAM)
	    ) {
	    CommandClose();		# Add the close as the last XML message
	    print $foundation_xml_message."\n\n";
	    print $socket $foundation_xml_message;
	    $perfData{'num_send_to_foundation_messages'}++;
	    close($socket);
	}
	else {
	    print "Couldn't connect to $remote_host:$remote_port : $!\n.";
	    # Sleep a short while so we don't spin, chewing up CPU, when we are quickly restarted.
	    sleep 10;
	    exit 2;
	}
    }
    return;
}

sub process_foundation_alarm {
    my $host          = shift;
    my $msg           = shift;
    my $crit_array    = shift;
    my $warn_array    = shift;
    my $ok_array      = shift;
    my $unknown_array = shift;

    if (!$consolidate_metrics) {
	UpdateHost($host,$msg);
    }
    UpdateServices($host,$msg,$crit_array,$warn_array,$ok_array,$unknown_array);
    $perfData{'num_foundation_messages'}++;

    return;
}

sub UpdateHost {
    my $host = shift;
    my $msg  = shift;

    # see Programming Perl, 3/e, page 598
    $foundation_xml_message = join '', $foundation_xml_message,
	'<HOST_STATUS ',		# Start message tag
		 'MonitorServerName="', $this_server,    '" ',
			      'Host="', $host,           '" ',
		    'Identification="', $host,           '" ',
		     'MonitorStatus="OK" ',
		     'LastCheckTime="', time_text(time), '" ',
		   'LastStateChange="', time_text(time), '" ',
		    'isAcknowledged="0" ',
			    'TimeUp="0" ',
			  'TimeDown="0" ',
		   'TimeUnreachable="0" ',
	      'LastNotificationTime="', time_text(time), '" ',
	 'CurrentNotificationNumber="0" ',
	    'isNotificationsEnabled="0" ',
	    'isEventHandlersEnabled="0" ',
		   'isChecksEnabled="0" ',
	    'isFlapDetectionEnabled="0" ',
		  'isHostIsFlapping="0" ',
		'PercentStateChange="0" ',
	    'ScheduledDowntimeDepth="0" ',
	'isFailurePredictionEnabled="0" ',
	  'isProcessPerformanceData="0" ',
	    'isPassiveChecksEnabled="0" ',
		       'CheckTypeID="0" ',
		  'LastPluginOutput=', $msg,             '" ',
	'/>';				# End message tag
    return;
}

sub UpdateServices {
    my $host          = shift;
    my $msg           = shift;
    my $crit_array    = shift;
    my $warn_array    = shift;
    my $ok_array      = shift;
    my $unknown_array = shift;

    # Create XML stream - Format:
    #   <SERVICE_STATUS MonitorServerName='ServerName' Host='HostName' Identification='DeviceName' >
    #       <SERVICE {list service attributes} />
    #       <SERVICE {list service attributes} />
    #   </SERVICE_STATUS>

    if (($host !~ /^\d+\.\d+\.\d+\.\d+$/i) and ($host =~ /$short_hostname_pattern/io)) { $host = $1; }

    if ($consolidate_metrics) {
	my $sev = 3;
	if ($#$crit_array >= 0) {
	    $sev = 2;
	}
	elsif ($#$warn_array >= 0) {
	    $sev = 1;
	}
	elsif ($#$ok_array >= 0) {
	    $sev = 0;
	}
	elsif ($#$unknown_array >= 0) {
	    $sev = 0;
	}
	$foundation_xml_message .= format_single_service_xml($this_server,$host,$consolidate_metrics_service_name,$sev,$msg);
    }
    else {
	my @xml_parts = ();
	push @xml_parts, join ('',
	    '<SERVICE_STATUS ',				# Start message tag
	    'MonitorServerName="', $this_server, '" ',	# Default Identification - should set to IP address if known
			 'Host="', $host,        '" ',	# Default Identification - should set to IP address if known
	       'Identification="', $host,        '" ',	# No IP address, then set to host name
	    '>');					# End message tag
	if ($#$crit_array >= 0) {
	    foreach my $metric (sort @$crit_array) {
		push @xml_parts, format_service_xml($metric,"CRITICAL");
	    }
	}
	if ($#$warn_array >= 0) {
	    foreach my $metric (sort @$warn_array) {
		push @xml_parts, format_service_xml($metric,"WARNING");
	    }
	}
	if ($#$ok_array >= 0) {
	    foreach my $metric (sort @$ok_array) {
		push @xml_parts, format_service_xml($metric,"OK");
	    }
	}
	if ($#$unknown_array >= 0) {
	    foreach my $metric (sort @$unknown_array) {
		push @xml_parts, format_service_xml($metric,"UNKNOWN");
	    }
	}
	push @xml_parts, '</SERVICE_STATUS>';
	$foundation_xml_message .= join '', @xml_parts;
    }
    return;
}

sub format_single_service_xml {
    my $monitorserver   = shift;
    my $host            = shift;
    my $service         = shift;
    my $sev             = shift;
    my $msg             = shift;
#   my ($service, @tmp) = split /\s/,$metric;

    #   <SERVICE ServiceDescription='Local_Procs' CheckType='ACTIVE' CurrentNotificationNumber='0' ExecutionTime='0' LastCheckTime='2005-07-15 22:47:28'
    #       LastHardState='OK' LastNotificationTime='0' LastPluginOutput='PROCS OK: 88 processes with STATE = RSZDT ' LastStateChange='2005-07-13 12:30:31'
    #       Latency='0' MonitorStatus='OK' NextCheckTime='2005-07-15 22:52:28' PercentStateChange='0.00' RetryNumber='1' ScheduledDowntimeDepth='0'
    #       StateType='HARD' TimeCritical='0' TimeOK='75674' TimeUnknown='0' TimeWarning='0' isAcceptPassiveChecks='1' isChecksEnabled='1'
    #       isEventHandlersEnabled='1' isFailurePredictionEnabled='1' isFlapDetectionEnabled='1' isNotificationsEnabled='1' isObsessOverService='1'
    #       isProblemAcknowledged='0' isProcessPerformanceData='1' isServiceFlapping='0'/>

    # see Programming Perl, 3/e, page 598
    my $xml_message = join '',
	"<SERVICE_STATUS ",	# Start message tag
		 "MonitorServerName='", $monitorserver,  "' ",
			      "host='", $host,           "' ",
		"ServiceDescription='", $service,        "' ",
			 "CheckType='ACTIVE' ",
	 "CurrentNotificationNumber='0' ",
		     "ExecutionTime='0' ",
		     "LastCheckTime='", time_text(time), "' ",
		     "LastHardState='", $sev,            "' ",
	      "LastNotificationTime='0' ",
		  "LastPluginOutput='", $msg,            "' ",
		   "LastStateChange='", time_text(time), "' ",
			   "Latency='0' ",
		     "MonitorStatus='", $sev,            "' ",
		     "NextCheckTime='", time_text(time), "' ",
		"PercentStateChange='0.00' ",
		       "RetryNumber='1' ",
	    "ScheduledDowntimeDepth='0' ",
			 "StateType='HARD' ",
		      "TimeCritical='0' ",
			    "TimeOK='0' ",
		       "TimeUnknown='0' ",
		       "TimeWarning='0' ",
	     "isAcceptPassiveChecks='0' ",
		   "isChecksEnabled='1' ",
	    "isEventHandlersEnabled='0' ",
	"isFailurePredictionEnabled='0' ",
	    "isFlapDetectionEnabled='0' ",
	    "isNotificationsEnabled='0' ",
	       "isObsessOverService='0' ",
	     "isProblemAcknowledged='0' ",
	  "isProcessPerformanceData='0' ",
		 "isServiceFlapping='0' ",
	"/> ";			# End message tag
    return $xml_message;
}

sub format_service_xml {
    my $metric          = shift;
    my $sev             = shift;
    my ($service, @tmp) = split /\s/,$metric;

#   <SERVICE ServiceDescription='Local_Procs' CheckType='ACTIVE' CurrentNotificationNumber='0' ExecutionTime='0' LastCheckTime='2005-07-15 22:47:28'
#       LastHardState='OK' LastNotificationTime='0' LastPluginOutput='PROCS OK: 88 processes with STATE = RSZDT ' LastStateChange='2005-07-13 12:30:31'
#       Latency='0' MonitorStatus='OK' NextCheckTime='2005-07-15 22:52:28' PercentStateChange='0.00' RetryNumber='1' ScheduledDowntimeDepth='0'
#       StateType='HARD' TimeCritical='0' TimeOK='75674' TimeUnknown='0' TimeWarning='0' isAcceptPassiveChecks='1' isChecksEnabled='1'
#       isEventHandlersEnabled='1' isFailurePredictionEnabled='1' isFlapDetectionEnabled='1' isNotificationsEnabled='1' isObsessOverService='1'
#       isProblemAcknowledged='0' isProcessPerformanceData='1' isServiceFlapping='0'/>

    # see Programming Perl, 3/e, page 598
    my $xml_message = join '',
	"<SERVICE ",	# Start message tag
		"ServiceDescription='", $service,        "' ",
			 "CheckType='ACTIVE' ",
	 "CurrentNotificationNumber='0' ",
		     "ExecutionTime='0' ",
		     "LastCheckTime='", time_text(time), "' ",
		     "LastHardState='", $sev,            "' ",
	      "LastNotificationTime='0' ",
		  "LastPluginOutput='", $metric,         "' ",
		   "LastStateChange='", time_text(time), "' ",
			   "Latency='0' ",
		     "MonitorStatus='", $sev,            "' ",
		     "NextCheckTime='", time_text(time), "' ",
		"PercentStateChange='0.00' ",
		       "RetryNumber='1' ",
	    "ScheduledDowntimeDepth='0' ",
			 "StateType='HARD' ",
		      "TimeCritical='0' ",
			    "TimeOK='0' ",
		       "TimeUnknown='0' ",
		       "TimeWarning='0' ",
	     "isAcceptPassiveChecks='0' ",
		   "isChecksEnabled='1' ",
	    "isEventHandlersEnabled='0' ",
	"isFailurePredictionEnabled='0' ",
	    "isFlapDetectionEnabled='0' ",
	    "isNotificationsEnabled='0' ",
	       "isObsessOverService='0' ",
	     "isProblemAcknowledged='0' ",
	  "isProcessPerformanceData='0' ",
		 "isServiceFlapping='0' ",
	"/> ";	# End message tag
    return $xml_message;
}

sub CommandClose {
    # Create XML stream - Format:
    #   <SERVICE-MAINTENANCE command="close" />
    $foundation_xml_message .= '<SERVICE-MAINTENANCE command="close" />';
    return;
}

sub time_text {
    my $timestamp = shift;
    if ($timestamp <= 0) {
	return "0";
    }
    else {
	my ($seconds, $minutes, $hours, $day_of_month, $month, $year,$wday, $yday, $isdst) = localtime($timestamp);
	return sprintf "%02d-%02d-%02d %02d:%02d:%02d",$year+1900,$month+1,$day_of_month,$hours,$minutes,$seconds;
    }
}

__END__
