#!/usr/local/groundwork/perl/bin/perl -w --
#
# FPING FEEDER - PERFORMANCE ENHANCEMENT FOR NAGIOS(R)
# Copyright (c) 2009-2017 GroundWork Open Source (www.groundworkopensource.com).

# TO DO:
# (*) Put all the config-file processing inside an eval{};, and sleep for 60 seconds
#     if it fails when operating in daemon mode (not in plugin mode).
# (*) Why stop at only a secondary NSCA target?  Perhaps generalize to allow sending
#     to an arbitrary number of targets, by the use of arrays of option settings,
#     much as we have done in certain other scripts.
# (*) Other FIX issues noted in this script.

# Note:  The "rta" performance metric is supposed to be the "round-trip average" time (milliseconds)
# for the ping packets, but in practice might not be an average of multiple trips.

use strict;
use Time::Local;
use IO::Socket;
use Data::Dumper;
use Time::HiRes;
use DBI;
use CollageQuery;
use IPC::Open2;
use Getopt::Long;

# This is where we'll pick up the TypedConfig package, so this line must
# come earlier than the TypedConfig reference.
use lib qw( /usr/local/groundwork/nagios/libexec );

use TypedConfig;

my $PROGNAME = "fping_process";
my $VERSION = "5.1.0";

# Use standard credentials for Monarch DB manipulation.

my $dbname = undef;
my $dbhost = undef;
my $dbuser = undef;
my $dbpass = undef;
my $dbtype = undef;
my $properties_file = '/usr/local/groundwork/config/db.properties';

if ( !open( FILE, '<', $properties_file ) ) {
    print "Cannot open $properties_file ($!)\n";
    exit 1;
}

while ( my $line = <FILE> ) {
    if ( $line =~ /^\s*global\.db\.type\s*=\s*(\S+)/  ) { $dbtype = $1 }
    if ( $line =~ /^\s*monarch\.dbhost\s*=\s*(\S+)/   ) { $dbhost = $1 }
    if ( $line =~ /^\s*monarch\.database\s*=\s*(\S+)/ ) { $dbname = $1 }
    if ( $line =~ /^\s*monarch\.username\s*=\s*(\S+)/ ) { $dbuser = $1 }
    if ( $line =~ /^\s*monarch\.password\s*=\s*(\S+)/ ) { $dbpass = $1 }
}

close(FILE);

#######################################################
#
#   Command Line Execution Options
#
#######################################################

my $print_help    = 0;
my $print_version = 0;
my $config_file   = "/usr/local/groundwork/config/fping_process.conf";
my $debug_config  = 0;
my $plugin_mode   = 0;    # In plugin mode, run just once and stop with Nagios plugin style output.
my $command_line_elapsed_time_threshold = 0;    # threshold to be passed as an argument

sub print_usage {
    print "usage:  fping_process.pl [-h] [-v] [-c config_file] [-d] [-p] [-t seconds]\n";
    print "        -h:  print this help message\n";
    print "        -v:  print the version number\n";
    print "        -c config_file:  specify an alternate config file\n";
    print "             (default is $config_file)\n";
    print "        -d:  dump the config file entries (to debug them)\n";
    print "        -p:  run as a plugin, not as a persistent daemon\n";
    print "        -t seconds:  specify the maximum cycle execution time\n";
    print "             beyond which a critical service state is declared,\n";
    print "             overriding the corresponding value in the config file\n";
}

Getopt::Long::Configure ("no_ignore_case");
if (
    !GetOptions(
	'help'         => \$print_help,
	'version'      => \$print_version,
	'config=s'     => \$config_file,
	'debug-config' => \$debug_config,
	'plugin'       => \$plugin_mode,
	'threshold=s'  => \$command_line_elapsed_time_threshold
    )
  )
{
    print "ERROR:  cannot parse command-line options!\n";
    print_usage;
    exit 1;
}

if ($print_version) {
    print "$PROGNAME $VERSION\n";
    print "Copyright 2009-2017 GroundWork Open Source, Inc. (\"GroundWork\").  All\n";
    print "rights reserved.  Use is subject to GroundWork commercial license terms.\n";
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
    exit 1;
}

#######################################################
#
#   Configuration File Handling
#
#######################################################

my $config = undef;
eval {
    $config = TypedConfig->secure_new ($config_file, $debug_config);
};
if ($@) {
    # Wait a bit so the process is not restarted immediately by a controlling daemon,
    # only to immediately fail again.
    sleep 10 if not $plugin_mode;
    die $@;
}

#######################################################
#
#   General Program Execution Options
#
#######################################################

# Spawn and process fpings?  If not, just sleep forever.
# This option is turned off in the default configuration file simply so the script can be
# safely installed before it is locally configured.  To get the software to run, it must be
# turned on in the configuration file once the rest of the setup is correct for your site.
my $enable_processing = $config->get_boolean ('enable_processing');

# Auto-flush the logging output.
# 0 = normal processing, for efficiency
# 1 = autoflush the log output on every single write, for debugging mysterious failures
my $autoflush_log_output = $config->get_boolean ('autoflush_log_output');

# Where to log debug messages.
my $logfile = $config->get_scalar ('logfile');

# Global Debug Mode Flag;  No debug = 0, Normal debug=1, Detail debug=2
my $debug_level = $config->get_number ('debug_level');

# Debug list detail:  control how much host/service info to list in the logfile;
# can generate very large log files; really only useful during initial setup.
# 0 => no detail; 1 => print IP addresses; 2 => also print hosts;
# 3 => also print host parents; 4 => also print services
# Only effective if debug_level > 1; otherwise, all this output is suppressed.
my $debug_list_detail = $config->get_number ('debug_list_detail');

# Where to find the list of hosts to ping -- in the database.
# The dbtype override in particular was historically not in the config file,
# so we allow it to be missing.
my $dbtype_override = undef;
eval {
    $dbtype_override = $config->get_scalar ('Database_Type');
};
if ($@ && $@ !~ /cannot find a config-file value/) {
    die $@;
}
my $dbname_override = $config->get_scalar ('Database_Name');
my $dbhost_override = $config->get_scalar ('Database_Host');
my $dbuser_override = $config->get_scalar ('Database_User');
my $dbpass_override = $config->get_scalar ('Database_Pass');

# Optionally overide the db.properties values
if ($dbtype_override) {
    print "Overriding database type with type $dbtype_override.\n" if $debug_config;
    $dbtype = $dbtype_override;
}
if ($dbname_override) {
    print "Overriding database name with database $dbname_override.\n" if $debug_config;
    $dbname = $dbname_override;
}
if ($dbhost_override) {
    print "Overriding database host with host $dbhost_override.\n" if $debug_config;
    $dbhost = $dbhost_override;
}
if ($dbuser_override) {
    print "Overriding database user with user $dbuser_override.\n" if $debug_config;
    $dbuser = $dbuser_override;
}
if ($dbpass_override) {
    print "Overriding database password with password from config file.\n" if $debug_config;
    $dbpass = $dbpass_override;
}

# The hostname for which the fping_process service result is to be reported;
# that is, the host on which this copy of the script runs.
my $fping_host = $config->get_scalar ('fping_host');

# What subset of hosts to ping.  This must be either a single Monarch group name,
# to fping only the hosts associated with that group (generally associated with a
# particular child server), or an empty string, to fping all hosts in Monarch.
my $fping_group = $config->get_scalar ('fping_group');

# You may use the fping_exclude_groups option to specify a comma-separated list
# of Monarch Groups to exclude from processing.  (Such exclusions will only
# take effect if the fping_group option is set to an empty string.)  Hosts
# belonging indirectly (via hostgroups) or directly to any of the Monarch Groups
# named in this list will be ignored.  This might be used on a parent server,
# for instance, to ignore all hosts that will be pinged instead from child
# servers.  Note:  Only commas are used to separate the Monarch Group names; do
# not introduce extra space characters as separators.
my $fping_exclude_groups = $config->get_scalar ('fping_exclude_groups');

# Exclude Monarch Groups from ping?  This extra filtering will only be evaluated
# if $fping_group is NOT set.
#
# If exclude groups are configured, we create an additional portion of a
# WHERE clause here, including an initial AND conjunction, to be appended to
# existing WHERE clauses.  For that to work correctly, the entire set of prior
# conditions in the WHERE clause to which this is appended must be parenthesized
# as a group, to ensure that the extra filtering we specify here does not
# restrict only the last condition in the existing WHERE clause due to operator
# precedence rules.

my $and_host_not_in_excluded_groups = '';
if ( $fping_exclude_groups and not $fping_group ) {
    my @groups_to_exclude = split( ',', $fping_exclude_groups );
    print Dumper @groups_to_exclude if $debug_config;
    if (@groups_to_exclude) {
	my $excluded_groups_list = "'" . join( "','", @groups_to_exclude ) . "'";

	if (0) {
	    ## This is an early-development query; I haven't traced whether it is
	    ## generally correct.  It contains no processing for Monarch sub-groups.
	    $and_host_not_in_excluded_groups = "
		and h.host_id not in (
		    select host_id from hostgroup_host
		    where hostgroup_id in (
			select hostgroup_id
			from monarch_group_hostgroup
			where group_id in (
			    select group_id from monarch_groups
			    where name in ($excluded_groups_list)
			)
		    ),
		    select host_id from monarch_group_host
		    where group_id in (
			select group_id from monarch_groups
			where name in ($excluded_groups_list)
		    )
		)
	    ";
	}
	elsif (0) {
	    ## This is an un-optimized query.  It is believed to work in general,
	    ## though it pays no attention to Monarch sub-groups.  For that, we
	    ## would need to join to the monarch_group_child table as well, with
	    ## more conditions.  Note that there can be a whole tree of sub-groups,
	    ## so that can get quite complicated.
	    ## FIX MINOR:  Decide whether Monarch sub-groups are important here,
	    ## and process them as well if so.
	    $and_host_not_in_excluded_groups = "
		and h.host_id not in (
		    select host_id from hostgroup_host
		    where hostgroup_id in (
			select hostgroup_id
			from monarch_group_hostgroup
			where group_id in (
			    select group_id from monarch_groups
			    where name in ($excluded_groups_list)
			)
		    )
		)
		and h.host_id not in (
		    select host_id from monarch_group_host
		    where group_id in (
			select group_id from monarch_groups
			where name in ($excluded_groups_list)
		    )
		)
	    ";
	}
	else {
	    ## This is our attempt at an optimized query.  Note that it currently
	    ## pays no attention to Monarch sub-groups, which perhaps it ought to.
	    ## For that, we would need to join to the monarch_group_child table
	    ## as well, with more conditions.  Note that there can be a whole tree
	    ## of sub-groups, so that can get quite complicated.
	    ## FIX MINOR:  Decide whether Monarch sub-groups are important here,
	    ## and process them as well if so.
	    $and_host_not_in_excluded_groups = "
		and h.host_id not in (
			select distinct host_id
			from
			    hostgroup_host
			    inner join monarch_group_hostgroup using (hostgroup_id)
			    left join monarch_groups using (group_id)
			where
			    monarch_groups.name in ($excluded_groups_list)
		    UNION DISTINCT
			select distinct host_id
			from
			    monarch_group_host
			    left join monarch_groups using (group_id)
			where
			    monarch_groups.name in ($excluded_groups_list)
		)
	    ";
	}
    }
}

# How often to repeat the entire set of pings (seconds between starts of successive passes).
my $cycle_time = $config->get_number ('cycle_time');

# The maximum number of hosts to fping at one time (i.e., in one call to fping).
# Such calls to fping will repeat until all hosts are pinged.
my $max_hosts_per_fping = $config->get_number ('max_hosts_per_fping');

# How many seconds to wait between successive calls to fping.
# A non-zero value is recommended here to prevent huge spawning storms.
my $pause_time = $config->get_number ('pause_time');

# Send host checks?
my $send_host_check = $config->get_boolean ('send_host_check');
# Send service checks?
my $send_service_check = $config->get_boolean ('send_service_check');

# List of services to submit passive results for.
# Specify a comma-separated list of service names (with no embedded spaces).
# The standard value is just "icmp_ping".  Setting this to an empty string
# will default it to "Host_alive,icmp_ping".
my $services_list = $config->get_scalar ('services_list');

if (! $services_list) {
    $services_list = "Host_alive,icmp_ping";	# Default comma-separated list of service names which will be sent passive results.
}

my %services_list_hash = ();
foreach my $service (split /,/, $services_list) {
    $services_list_hash{$service} = 1;
}

$services_list =~ s/,/','/g;			# Make comma separated list into a quoted, comma separated list.
$services_list = "'".$services_list."'";	# Add starting and ending single quotes.

# === deal with negated service results ===

# In this section, we occasionally call Data::Dumper->Dump() under debug control, to
# dump out the state of certain complex data structures.  That's because we need to
# provide an easy means of finding out how the script reacts to malformed setup in
# what is now an external configuration file.

# List of services whose sensed results are to be negated before being sent out.
my %negated_service_hash = $config->get_hash ('negated_services');

print Data::Dumper->Dump([\%negated_service_hash], [qw(\%negated_service_hash)]) if $debug_config;

my %negated_services = %{$negated_service_hash{'service'}};

print Data::Dumper->Dump([\%negated_services], [qw(\%negated_services)]) if $debug_config;

my %NegatedServices = ();
foreach my $service (keys %negated_services) {
    if (exists $services_list_hash{$service}) {
	print "$service service results will be negated.\n" if $debug_config;
	$NegatedServices{$service}{'in-play'} = 1;
	my $service_okay_text     = $negated_services{$service}{'okay'};
	my $service_warning_text  = $negated_services{$service}{'warning'};
	my $service_critical_text = $negated_services{$service}{'critical'};
	my $service_unknown_text  = $negated_services{$service}{'unknown'};
	if (defined $service_okay_text) {
	    print "text for $service in okay state is '$service_okay_text'\n" if $debug_config;
	    $NegatedServices{$service}{0} = $service_okay_text;
	}
	if (defined $service_warning_text) {
	    print "text for $service in warning state is '$service_warning_text'\n" if $debug_config;
	    $NegatedServices{$service}{1} = $service_warning_text;
	}
	if (defined $service_critical_text) {
	    print "text for $service in critical state is '$service_critical_text'\n" if $debug_config;
	    $NegatedServices{$service}{2} = $service_critical_text;
	}
	if (defined $service_unknown_text) {
	    print "text for $service in unknown state is '$service_unknown_text'\n" if $debug_config;
	    $NegatedServices{$service}{3} = $service_unknown_text;
	}
    }
    else {
	print "negated_services service $service is not in services_list and so will be ignored.\n" if $debug_config;
    }
}

if (scalar keys %NegatedServices == 0) {
    print "No negated_services are defined in $config_file\n" if $debug_config;
}

print Data::Dumper->Dump([\%NegatedServices], [qw(\%NegatedServices)]) if $debug_config;

# =========================================

# Use NSCA to send results to a (generally remote) Nagios command pipe?
#  no = write directly to a local Nagios command pipe
# yes = use send_nsca to write to a (generally remote) Nagios command pipe
my $use_nsca = $config->get_boolean ('use_nsca');

# Absolute pathname of the local Nagios command pipe.
my $nagios_cmd_pipe = $config->get_scalar ('nagios_cmd_pipe');

# The maximum time in seconds to wait for any single write to the Nagios
# command pipe to complete.
my $max_command_pipe_wait_time = $config->get_number ('max_command_pipe_wait_time');

# The maximum size in bytes for any single write operation to the Nagios
# command pipe.  The value chosen here must be no larger than PIPE_BUF
# (getconf -a | fgrep PIPE_BUF) on your platform, unless you have an absolute
# guarantee that no other process will ever write to the command pipe.
my $max_command_pipe_write_size = $config->get_number ('max_command_pipe_write_size');

# Default elapsed time threshold for one cycle time, beyond which we declare
# the fping_process service to be in a CRITICAL state.  This value may be
# overridden on the command line.  Zero means we don't apply a threshold.
my $default_elapsed_time_threshold = $config->get_number ('default_elapsed_time_threshold');

# The number of seconds to delay between successive calls to send data, whether
# via the local Nagios command pipe or via send_nsca.  This is used to spread
# out sending of results over some small period of time, to reduce the chances
# of the receiver being overloaded with a sudden inrush of results, and thereby
# also to reduce the chances that this script will not be able to successfully
# send the results.
my $delay_between_sends = $config->get_number ('delay_between_sends');

#######################################################
#
#   NSCA Options
#
#######################################################

# The maximum number of hosts for which check results will be passed to
# one call of send_nsca.
my $max_hosts_per_send_nsca = $config->get_number ('max_hosts_per_send_nsca');

# Host of target (generally remote) NSCA.
my $nsca_host = $config->get_scalar ('nsca_host');

# NSCA port to send_nsca results to (usually port 5667).
my $nsca_port = $config->get_number ('nsca_port');

# The number of seconds before send_nsca times out.
my $nsca_timeout = $config->get_number ('nsca_timeout');

# Whether to also send a copy of the fping data to a secondary server.
my $send_to_secondary_NSCA = $config->get_boolean ('send_to_secondary_NSCA');

# Host of secondary target NSCA.
my $secondary_nsca_host = $config->get_scalar ('secondary_nsca_host');

# Secondary-host NSCA port to send_nsca results to (usually port 5667).
my $secondary_nsca_port = $config->get_number ('secondary_nsca_port');

# The number of seconds before secondary-host send_nsca times out.
my $secondary_nsca_timeout = $config->get_number ('secondary_nsca_timeout');

#######################################################
#
#   Execution Global variables
#
#######################################################

my $terminate       = 0;
my $switch_logfiles = 0;
my $logtime         = '';

my $elapsed_time_threshold = 0;    # threshold used for comparison
my $query       = undef;
my $parent      = "none";
my $parent_down = undef;
my $host        = undef;
my $service     = undef;
my $time1       = undef;
my $time2       = undef;
my $unixtime    = 0;
my $executed    = undef;
my           $send_nsca_command = "/usr/local/groundwork/common/bin/send_nsca -H $nsca_host -p $nsca_port -to $nsca_timeout -c /usr/local/groundwork/common/etc/send_nsca.cfg";
my $secondary_send_nsca_command = "/usr/local/groundwork/common/bin/send_nsca -H $secondary_nsca_host -p $secondary_nsca_port -to $secondary_nsca_timeout -c /usr/local/groundwork/common/etc/send_nsca.cfg";
my $result_sends = 0;
my $failed = 0;		# Will be used as exit status; 0 means successful.

my @monthnames = qw(January February March April May June July August September October November December);
my @daynames = qw(Sunday Monday Tuesday Wednesday Thursday Friday Saturday);

my @aggregated_commands = ();
$#aggregated_commands = $max_hosts_per_send_nsca;	# pre-extend the array, for efficiency
$#aggregated_commands = -1;				# truncate the array, since we don't have any messages yet

#######################################################
#
#   Configuration Processing
#
#######################################################

if ($command_line_elapsed_time_threshold > 0) {
    $elapsed_time_threshold = $command_line_elapsed_time_threshold + 0;	# force numeric
}
else {
    $elapsed_time_threshold = $default_elapsed_time_threshold;
}

if ($fping_group ne '') {
    # Try to validate the string given, to prevent an SQL injection attack,
    # even if we use placeholders and value binding below for the same purpose.
    if ($fping_group =~ m:[`~!\$%^&*|'"<>?,()=/\\;\r\n\t\0]:) { # here is a ` character so vim color highlighting stops on this line
	print "ERROR:  The fping_group setting in the configuration file contains illegal characters.\n";
	sleep 5;	# Don't have gwservices restart us immediately, in a tight loop.
	exit 1;
    }
}

#######################################################
#
#   Subroutines
#
#######################################################

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
    log_timed_message "=== Shutdown requested; terminating (process $$). ===";
}

# The normal signal handler for process termination.  We just record the fact that
# the signal came in, and process it at the next safe and convenient opportunity.
sub record_terminate_signal {
    $terminate = 1;
}

# For use only in long-running code sequences that need to be interrupted so they
# complete quickly if a signal comes in.  Run such code within an eval{}; that will
# catch the die() here.  Establish this signal handler as a local $SIG{TERM} value
# within the eval{}; statement:
#     local $SIG{TERM} = \&die_on_terminate_signal;
# and then be sure to test $terminate and/or $@ after the eval{}; to see if the code
# got interrupted, so you don't misinterpret whatever inconsistent state it may have
# left behind.
sub die_on_terminate_signal {
    my $signame = shift;
    $terminate = 1;
    die "Caught a SIG$signame signal.\n";
}

sub handle_switch_signal {
    $switch_logfiles = 1;
}

sub catch_signal {
    my $signame = shift;

    $| = 1;	# force output flushing before we quit
    die "=== Writing to the Nagios command pipe timed out; caught a SIG$signame signal; exiting! ===\n";
}

# The format of a message sent directly to the command pipe is one of:
# [$unixtime] PROCESS_HOST_CHECK_RESULT;$host_name;$status_code;$plugin_output\n
# [$unixtime] PROCESS_SERVICE_CHECK_RESULT;$host_name;$service_description;$return_code;$plugin_output\n
# which is different from the format we pass to send_nsca.

sub send_to_nagios {
    if ( scalar @aggregated_commands ) {
	log_timed_message 'Sending '
	  . ( scalar @aggregated_commands )
	  . ( @aggregated_commands == 1 ? ' command' : ' commands' )
	  . ' to the Nagios command pipe.';
	open( FIFO, '+<:unix', $nagios_cmd_pipe ) or die "Could not open the Nagios command pipe: $!";
	local $SIG{ALRM} = \&catch_signal;
	my $first = 0;
	my $last  = $first;
	my $message_size;
	my $buffer_size    = 0;
	my $index_past_end = scalar(@aggregated_commands);

	for ( my $index = 0 ; $index <= $index_past_end ; ++$index ) {
	    if ( $index < $index_past_end ) {
		$message_size = length( $aggregated_commands[$index] );
	    }
	    else {
		$message_size = 0;
	    }
	    if ( $index < $index_past_end && $buffer_size + $message_size <= $max_command_pipe_write_size ) {
		$buffer_size += $message_size;
	    }
	    else {
		if ( $buffer_size > 0 ) {
		    alarm($max_command_pipe_wait_time);
		    eval {
			local $SIG{TERM} = \&die_on_terminate_signal;
			if ( not print FIFO join( '', @aggregated_commands[ $first .. $last ] ) ) {
			    die "Cannot write to the Nagios command pipe: $!";
			}
		    };
		    alarm(0);
		    die "Exiting:  $@" if $@;
		}
		$first       = $index;
		$buffer_size = $message_size;
	    }
	    $last = $index;
	}
	close(FIFO);
    }
}

sub output_results {
    ++$result_sends;
    log_timed_message 'Sending ' . $#aggregated_commands . ' results.' if $debug_level;
    if ($use_nsca) {
	## Now send the aggregated commands to send_nsca.

	# Note:  We want to capture error messages from send_nsca so they end up in our logfile,
	# to make debugging problems in the field a lot easier.  send_nsca uses stdout rather
	# than stderr for all its error messages, so we only need to capture the output stream,
	# not also the error stream, to get its error messages reflected in our own logfile.
	# Also note that using the shell to redirect the output stream to the same logfile that
	# this script is using avoids all the complexity of trying to read the stdout stream here
	# using IPC::Open2, with its potential for deadlocks and other conniptions, and writing
	# that same output ourselves to the logfile.  The main thing is to ensure that everybody
	# writing to the logfile opens it in append mode, so we don't suffer overwrites and lost
	# data.  We don't expect any problems with interleaved writes from parent and child,
	# because we explicitly flush the logfile before connecting the child process(es) to it,
	# and we don't write to the logfile ourselves while the child is alive.
	LOG->flush;

	# Make sure we don't die with a "Broken pipe" error, which could arise if the command we run
	# were to exit before we were able to complete our printing to the pipe.  If we do receive a
	# SIGPIPE, it will be reflected in the close() status, so we will still know about the failure.
	local $SIG{PIPE} = 'IGNORE';

	# The eval{}; statements and special signal handler are used here to ensure that we regain
	# control immediately upon receipt of a termination signal, rather than waiting until the
	# i/o completes.  This is in service of having the script respond quickly to a termination
	# request.  Elsewhere in the script, we can afford to wait because we're CPU bound and we
	# believe it won't be long until we check for the termination flag.  But here, we could
	# wait a significant amount of time without any notice that a signal was pending, if we
	# didn't interrupt the operation explicitly.
	if (! $terminate) {
	    eval {
		local $SIG{TERM} = \&die_on_terminate_signal;
		die 'Terminated.' if $terminate;  # handle race condition
		log_timed_message 'Sending '
		  . ( scalar @aggregated_commands )
		  . ( @aggregated_commands == 1 ? ' packet' : ' packets' )
		  . ' to the NSCA port.';
		open NSCA, '|-', "$send_nsca_command >>$logfile";
		print NSCA join ('', @aggregated_commands);
		$failed |= ! close NSCA;
	    };
	    if ($@) {
		## In case we aborted before the end, let's try not to leak resources.
		$failed |= ! close NSCA;
	    }
	}
	if (! $terminate && $send_to_secondary_NSCA) {
	    eval {
		local $SIG{TERM} = \&die_on_terminate_signal;
		die 'Terminated.' if $terminate;  # handle race condition
		log_timed_message 'Sending '
		  . ( scalar @aggregated_commands )
		  . ( @aggregated_commands == 1 ? ' packet' : ' packets' )
		  . ' to the secondary NSCA port.';
		open NSCA, '|-', "$secondary_send_nsca_command >>$logfile";
		print NSCA join ('', @aggregated_commands);
		$failed |= ! close NSCA;
	    };
	    if ($@) {
		## In case we aborted before the end, let's try not to leak resources.
		$failed |= ! close NSCA;
	    }
	}
    }
    else {
	send_to_nagios();
    }
}

#######################################################
#
#   Program Start
#
#######################################################

# Stop if this is just a debugging run.
exit if $debug_config;

if (!open(LOG, '>>', $logfile)) {
    print "ERROR:  Can't open logfile:  $logfile\n";
    sleep 10;	# Don't have gwservices restart us immediately, in a tight loop.
    exit 1;
}

# Possibly autoflush the log output on every single write, for debugging mysterious failures.
LOG->autoflush($autoflush_log_output || !$enable_processing);

log_timed_message "=== Starting up (process $$). ===";

if (! $enable_processing) {
    if ($plugin_mode) {
	log_timed_message 'WARNING:  fping_process processing is not enabled in the config file; it will not return any results.';
    }
    else {
	log_timed_message 'WARNING:  fping_process processing is not enabled in the config file; it will sleep forever.';
	## Sleep forever, simply so we don't get continually restarted and waste resources.
	sleep 100000000;
    }
    ## We use an exit status of 4 to indicate that the script is disabled.
    exit 4;
}

log_timed_message "services_list = $services_list" if $debug_level > 1;

#######################################################
#
#   Main loop
#
#######################################################

$SIG{TERM} = \&record_terminate_signal;
$SIG{HUP}  = \&handle_switch_signal;

# run forever, unless running as a plugin or there is an issue or we are asked to shut down
while (! $terminate) {

    if ($switch_logfiles) {
	$switch_logfiles = 0;
	close(LOG);
	if (!open(LOG, '>>', $logfile)) {
	    print "ERROR:  Can't open logfile:  $logfile\n";
	    exit 1;
	}
	## Possibly autoflush the log output on every single write, for debugging mysterious failures.
	LOG->autoflush($autoflush_log_output);
    }

    if ($debug_level) {
	my ( $sec, $min, $hour, $mday, $mon, $year, $wday, $yday, $isdst ) = localtime(time);
	$year += 1900;
	my $month = $monthnames[$mon];
	my $thisday = $daynames[$wday];
	my $timestring = sprintf "%02d:%02d:%02d", $hour, $min, $sec;
	log_message "--- NEW LOOP on $thisday, $month $mday, $year at $timestring ---";
    }

    my $starttime = Time::HiRes::time();
    ## Get hosts->IPaddress from Monarch
    my $host_ref    = undef;
    my $ip_ref      = undef;
    my $query       = undef;
    my $sth         = undef;
    my $sth2        = undef;    # for service_instance query
    my $parent      = "none";
    my $parent_down = undef;
    my $host        = undef;
    my $service     = undef;
    $result_sends = 0;          # reset send count for this cycle
    $failed       = 0;          # reset failed status for this cycle

    # my ( $dbname, $dbhost, $dbuser, $dbpass, $dbtype ) = CollageQuery::readGroundworkDBConfig('monarch');

    $time1 = time;

    # Elsewhere in this script, we have used the die_on_terminate_signal() signal handler in all
    # places that we know of that might contain restartable system calls (mostly, i/o of one sort
    # or another) that might take a long time to finally complete.  Generally, we have done so
    # when it is obvious that the call might need such treatment, or when testing revealed that a
    # potentially long-running system call would be restarted if we did not do so.  However, it's
    # possible that database calls such as $dbh->prepare($query); or $sth->execute(); might have
    # the same issue, say if the database is not responding, is extremely busy for a long time, or
    # the query is accessing resources that are locked by some other client.  So we need to protect
    # ourselves in a similar way for all database accesses, hoping both that this construction will
    # guarantee that our signal handler will be called (so we can capture receipt of the signal) and
    # that any pending, in-progress database operations will be aborted by this setup.
    #
    # To test any construction here vis-a-vis wrapping database calls, we run a mysql (or psql)
    # client in one terminal, have it lock some table which is used by these queries, and make some
    # modification on the locked table (to effectively invalidate any caching of previous queries).
    #
    #     mysql> set autocommit=0;
    #     mysql> lock tables hosts write;
    #     mysql> update hosts set name='foobar' where name='localhost';
    #
    # We then run this script interactively from a second terminal.  It should hang when it tries
    # to access that table.  Then from a third terminal, we find the script and send it a SIGTERM
    # signal, and verify that it does properly break out of the wait.
    #
    # Actual testing with the die_on_terminate_signal() signal handler in this context shows that,
    # while the signal handler would be called, somehow its die() won't take effect here on the
    # client if, for instance, the query is being held up by a table lock on the server.  This
    # script would continue to wait until the lock is released, rather than springing out of the
    # eval{}; statement we have here.  We suspect that the database-access library is effectively
    # wrapping the query execution call within its own equivalent of an eval{};, and then restarting
    # the query after the interrupt, hence defeating our design.  Ideally, the DBI->connect() call
    # would make available some kind of attribute to control this behavior, but we don't see it in
    # either the DBI module or the DBD::mysql module.
    #
    # Because of that library interference, we seem to have little choice here but to reset the
    # signal handling behavior in this section back to the default of dying immediately upon receipt
    # of SIGTERM.  That bypasses whatever eval{}; is in place within the database access code.
    eval {
	local $SIG{TERM} = 'DEFAULT';
	die 'Terminated.' if $terminate;  # handle race condition

	my $dsn = '';
	if ( defined($dbtype) && $dbtype eq 'postgresql' ) {
	    $dsn = "DBI:Pg:dbname=$dbname;host=$dbhost";
	}
	else {
	    $dsn = "DBI:mysql:database=$dbname;host=$dbhost";
	}
	my $dbh = DBI->connect($dsn, $dbuser, $dbpass, { 'AutoCommit' => 1 })
	    or die "Can't connect to database $dbname. Error: ".$DBI::errstr;

	if ($send_host_check) {
	    $parent = "none";

	    ## First get all the root hosts.
	    if ($fping_group eq '') {
		## ping all hosts in the monarch database
		$query = "
		    select name, address
		    from hosts as h
		    where (
			host_id not in (
			    select host_id from host_parent
			)
		    )
		    $and_host_not_in_excluded_groups;
		";
	    }
	    else {
		## ping only a particular subset of the hosts in the monarch database
		$query = "
			select h.name, h.address
			from hosts h, hostgroup_host hgh, monarch_group_hostgroup mghg, monarch_groups mg
			where
			    h.host_id not in (
				select host_id from host_parent
			    )
			and hgh.host_id = h.host_id
			and mghg.hostgroup_id = hgh.hostgroup_id
			and mg.group_id = mghg.group_id
			and mg.name = ?
		    UNION
			select h.name, h.address
			from hosts h, monarch_group_host mgh, monarch_groups mg
			where
			    h.host_id not in (
				select host_id from host_parent
			    )
			and mgh.host_id = h.host_id
			and mg.group_id = mgh.group_id
			and mg.name = ?;
		";
	    }
	    $sth = $dbh->prepare($query);
	    if ($fping_group eq '') {
		$executed = $sth->execute();
	    }
	    else {
		$executed = $sth->execute($fping_group, $fping_group);
	    }
	    if (not $executed) {
		log_timed_message 'ERROR:  Bad database query:';
		log_message $query;
		log_message $sth->errstr;
		exit 1;
	    }
	    while (my $row = $sth->fetchrow_hashref()) {
		$$row{address} =~ s/^\s+//;
		$$row{address} =~ s/\s+$//;
		$ip_ref->{IPADDRESS}->{$$row{address}}->{HOSTNAME}->{$$row{name}}->{PARENT}->{$parent}->{EXISTS} = 1;
		$host_ref->{HOSTNAME}->{$$row{name}}->{IPADDRESS} = $$row{address};
	    }
	    $sth->finish();

	    ## Next, get all the hosts that have parents, and their parents, so that we can walk the dependency tree.
	    if ($fping_group eq '') {
		$query = "
		    select h.name, h.address, hpa.name as \"parent\"
		    from hosts h, host_parent hp, hosts hpa
		    where (
			    hp.host_id = h.host_id
			AND hpa.host_id = hp.parent_id
		    )
		    $and_host_not_in_excluded_groups;
		";
	    }
	    else {
		$query = "
			select h.name, h.address, hpa.name as \"parent\"
			from hosts hpa, hosts h, host_parent hp, hostgroup_host hgh, monarch_group_hostgroup mghg, monarch_groups mg
			where
			    hp.parent_id = hpa.host_id
			and h.host_id = hp.host_id
			and hgh.host_id = h.host_id
			and mghg.hostgroup_id = hgh.hostgroup_id
			and mg.group_id = mghg.group_id
			and mg.name = ?
		    UNION
			select h.name, h.address, hpa.name as \"parent\"
			from hosts hpa, hosts h, host_parent hp, monarch_group_host mgh, monarch_groups mg
			where
			    hp.parent_id = hpa.host_id
			and h.host_id = hp.host_id
			and mgh.host_id = h.host_id
			and mg.group_id = mgh.group_id
			and mg.name = ?;
		";
	    }
	    $sth = $dbh->prepare($query);
	    if ($fping_group eq '') {
		$executed = $sth->execute();
	    }
	    else {
		$executed = $sth->execute($fping_group, $fping_group);
	    }
	    if (not $executed) {
		log_timed_message 'ERROR:  Bad database query:';
		log_message $query;
		log_message $sth->errstr;
		exit 1;
	    }
	    while (my $row = $sth->fetchrow_hashref()) {
		$ip_ref->{IPADDRESS}->{$$row{address}}->{HOSTNAME}->{$$row{name}}->{PARENT}->{$$row{parent}}->{EXISTS} = 1;
		$host_ref->{HOSTNAME}->{$$row{name}}->{IPADDRESS} = $$row{address};
	    }
	    $sth->finish();
	}

	if ($send_service_check) {
	    $query = "select servicename_id, name from service_names where name in ($services_list);";
	    $sth = $dbh->prepare($query);
	    $executed = $sth->execute();
	    if (not $executed) {
		log_timed_message 'ERROR:  Bad database query:';
		log_message $query;
		log_message $sth->errstr;
		exit 1;
	    }
	    my %servicename = ();
	    while (my $row = $sth->fetchrow_hashref()) {
		$servicename{$$row{servicename_id}} = $$row{name};
	    }
	    $sth->finish();
	    foreach my $servicename_id (sort keys %servicename) {
		if ($fping_group eq '') {
		    ## FIX LATER:  These days, I would try a LEFT JOIN to the services table instead of using a sub-select.
		    $query = "
			select name, address, host_id
			from hosts as h
			where (
			    host_id in (
				select host_id from services
				where servicename_id=$servicename_id
			    )
			)
			$and_host_not_in_excluded_groups;
		    ";
		}
		else {
		    $query = "
			    select h.name, h.address, h.host_id
			    from hosts h, hostgroup_host hgh, monarch_group_hostgroup mghg, monarch_groups mg
			    where
				h.host_id in (
				    select host_id from services
				    where servicename_id = $servicename_id
				)
			    and hgh.host_id = h.host_id
			    and mghg.hostgroup_id = hgh.hostgroup_id
			    and mg.group_id = mghg.group_id
			    and mg.name = ?
			UNION
			    select h.name, h.address, h.host_id
			    from hosts h, monarch_group_host mgh, monarch_groups mg
			    where
				h.host_id in (
				    select host_id from services
				    where servicename_id = $servicename_id
				)
			    and mgh.host_id = h.host_id
			    and mg.group_id = mgh.group_id
			    and mg.name = ?;
		    ";
		}
		$sth = $dbh->prepare($query);
		if ($fping_group eq '') {
		    $executed = $sth->execute();
		}
		else {
		    $executed = $sth->execute($fping_group, $fping_group);
		}
		if (not $executed) {
		    log_timed_message 'ERROR:  Bad database query:';
		    log_message $query;
		    log_message $sth->errstr;
		    exit 1;
		}
		while (my $row = $sth->fetchrow_hashref()) {
		    ## We need to take each service and see if it has service_instance
		    ## entries.  If it does, then create ip_ref entries and host_ref entries.
		    ## If it does not, then create just a host_ref entry since the IP address
		    ## to be pinged is the same as already stored for the host.
		    $query = "
			select s.name as \"service_name\", s.arguments from service_instance s, services
			where s.status = 1 AND s.service_id=services.service_id AND services.servicename_id=$servicename_id AND services.host_id=$$row{host_id};
		    ";
		    $sth2 = $dbh->prepare($query);
		    $executed = $sth2->execute();
		    if (not $executed) {
			log_timed_message 'ERROR:  Bad database query:';
			log_message $query;
			log_message $sth->errstr;
			exit 1;
		    }

		    my $service_instance = 0;
		    while (my $row2 = $sth2->fetchrow_hashref()) {
			$service_instance = 1;
			my $service_name = $servicename{$servicename_id} . $$row2{service_name};
			$ip_ref->{IPADDRESS}->{$$row2{arguments}}->{HOSTNAME}->{$$row{name}}->{SERVICENAME}->{$service_name} = 1;
		    }
		    $sth2->finish();

		    if (!$service_instance) {
			$ip_ref->{IPADDRESS}->{$$row{address}}->{HOSTNAME}->{$$row{name}}->{SERVICENAME}->{$servicename{$servicename_id}} = 1;
		    }
		}
		$sth->finish();
	    }
	}

	$dbh->disconnect();
    };  # end of eval{}; block enclosing database operations
    last if $terminate;
    if ($@) {
	chomp $@;
	log_timed_message $@;
	last;
    }

    $time2 = time;
    log_timed_message "Monarch query time = " . ($time2 - $time1) . "s" if $debug_level;

    # You may fine-tune this debug output by setting debug_level and debug_list_detail in the config file.
    if ( $debug_level > 1 && $debug_list_detail >= 1 ) {
	foreach my $ip ( sort keys %{ $ip_ref->{IPADDRESS} } ) {
	    log_message "IP=$ip";
	    if ( $debug_list_detail >= 2 ) {
		foreach $host ( sort keys %{ $ip_ref->{IPADDRESS}->{$ip}->{HOSTNAME} } ) {
		    log_message "\tHOST=$host";
		    if ( $debug_list_detail >= 3 ) {
			foreach $parent ( sort keys %{ $ip_ref->{IPADDRESS}->{$ip}->{HOSTNAME}->{$host}->{PARENT} } ) {
			    log_message "\t\tparent=$parent";
			}
		    }
		    if ( $debug_list_detail >= 4 ) {
			foreach $service ( sort keys %{ $ip_ref->{IPADDRESS}->{$ip}->{HOSTNAME}->{$host}->{SERVICENAME} } ) {
			    log_message "\t\tSERVICE=$service";
			}
		    }
		}
	    }
	}
    }

    my $host_count   = 0;
    my @one_hostlist = ();
    $#one_hostlist = $max_hosts_per_fping;    # pre-extend the array, for efficiency
    $#one_hostlist = -1;                      # truncate the array, since we don't have any hosts yet
    my @all_hostlists = ();
    $#all_hostlists = int ((scalar keys %{$ip_ref->{IPADDRESS}}) / $max_hosts_per_fping) + 1;	# pre-extend the array, for efficiency
    $#all_hostlists = -1;		# truncate the array, since we don't have any hostlists yet
    foreach my $key (sort keys %{$ip_ref->{IPADDRESS}}) {
	push @one_hostlist, "$key\n";
	$host_count++;
	if ($host_count >= $max_hosts_per_fping) {
	    push @all_hostlists, join ('', @one_hostlist);
	    $host_count = 0;
	    $#one_hostlist = -1;	# truncate the array
	}
    }
    if ($#one_hostlist >= 0) {
	push @all_hostlists, join ('', @one_hostlist);
    }

    # Fping everthing
    $time1 = time;

    my @achild_in  = ();
    my @achild_out = ();
    my @apid       = ();
    my $acount     = 0;
    my @lines      = ();

    # FIX LATER:  perhaps use IPC::Open3 instead, to handle the standard error stream as well;
    # would that then avoid the need to spawn an intermediate shell?
    # FIX LATER:  we need to trap SIGPIPE to handle exec failures
    # FIX LATER:  perhaps we can/should close in/out filehandles explicitly in this code

    eval {
	local $SIG{TERM} = \&die_on_terminate_signal;
	die 'Terminated.' if $terminate;    # handle race condition
	## Protect against a child that might die before we finish writing to it.
	local $SIG{PIPE} = 'IGNORE';
	## By default, fping will read the list of target systems from its standard input stream.
	my $fping_command = '/usr/local/groundwork/common/sbin/fping -e 2>/dev/null';
	foreach my $hostlist (@all_hostlists) {
	    $apid[$acount] = open2( $achild_out[$acount], $achild_in[$acount], $fping_command );
	    my $fh = $achild_in[$acount];
	    print $fh $hostlist;
	    $achild_in[$acount] = undef;
	    $acount++;
	    if ( $pause_time > 0 ) {
		## Sleep for possibly fractional seconds.
		## This pause will be cut short should a termination signal be received.
		select undef, undef, undef, $pause_time;
	    }
	}
    };
    last if $terminate;
    if ($@) {
	chomp $@;
	log_timed_message $@;
	last;
    }

    $acount = 0;
    # What we expect back for each host is one of these lines:
    #     dev0020.devel.london.europe.xyz.com is unreachable
    #     dev0455.devel.london.europe.xyz.com is alive (17.9 ms)
    # What we won't get back are messages like:
    #     ICMP Host Unreachable from 111.111.111.222 for ICMP Echo sent to dev9999.devel.london.europe.xyz.com (111.111.111.111)
    #     dev1234.devel.london.europe.xyz.com address not found
    # because these appear on the standard error stream, and we're dumping that overboard.
    # And that may mean we get no output at all for a given host.
    #
    # The point behind that is that we might see more than 50 characters in the response for a single host,
    # and we need to allow space for that much data.
    my $maxlength = 32768;
    if ($maxlength < $max_hosts_per_fping * 80) {
	$maxlength = $max_hosts_per_fping * 80;
    }

    # Here we might have long-running operations, trying to read output from child processes
    # and wait for them to exit.  To avoid an interminable wait before a termination signal
    # is finally recognized and this script exits, we need to allow such operations to be
    # aborted if we receive a termination signal.  The system calls behind the read() and
    # waitpid() routines will be restarted if we don't die() out of the signal handler, so
    # we set up to do just that.
    eval {
	local $SIG{TERM} = \&die_on_terminate_signal;
	die 'Terminated.' if $terminate;    # handle race condition
	foreach my $achild (@achild_out) {
	    my $achild_output;
	    read $achild, $achild_output, $maxlength;
	    waitpid( $apid[$acount], 0 );
	    my @achild_array = split( /\n/, $achild_output );
	    push @lines, @achild_array;
	    $acount++;
	}
    };
    last if $terminate;
    if ($@) {
	chomp $@;
	log_timed_message $@;
	last;
    }

    for my $line (@lines) {
	## Find the non-responders
	log_message "Result: $line" if $debug_level > 1;
	my ( $ip, $is, $state, $perf ) = split /\s+/, $line;
	if ( !$perf ) {
	    ## This default value is set to match the DEFAULT_TIMEOUT value compiled into fping.
	    ## We set this to avoid having undefined performance data to report below; the
	    ## alternative would be to specify this as "U".
	    $perf = "500";
	}
	$perf =~ s/\(//g;
	$perf =~ s/\)//g;
	$perf =~ s/ms//g;
	log_message "perfstring: $perf" if $debug_level > 1;

	# We now have all the results.  Pump them into the array.
	$ip_ref->{IPADDRESS}->{$ip}->{STATUS} = $state;    # might be undefined
	$ip_ref->{IPADDRESS}->{$ip}->{PERF}   = $perf;
    }

    $time2 = time;
    log_timed_message "fping time was " . ($time2 - $time1) . "s" if $debug_level;

    if ($debug_level > 1) {
	foreach my $ip (sort keys %{$ip_ref->{IPADDRESS}}) {
	    log_message "inserted into array: IP=$ip, State=".
		(defined($ip_ref->{IPADDRESS}->{$ip}->{STATUS}) ? $ip_ref->{IPADDRESS}->{$ip}->{STATUS} : "[unknown]").", Perf=".
		(defined($ip_ref->{IPADDRESS}->{$ip}->{PERF}  ) ? $ip_ref->{IPADDRESS}->{$ip}->{PERF}   : "[unknown]");
	}
    }

    # Now walk the tree and submit the results.
    # Stage submission to Nagios by max_hosts_per_send_nsca to avoid an overload condition.
    $host_count           = 0;
    $time1                = time;
    $unixtime             = "$time1";
    $#aggregated_commands = -1;         # truncate the array of messages
    my $totalhosts   = 0;
    my $servicecount = 0;

    foreach my $ip (sort keys %{$ip_ref->{IPADDRESS}}) {
	my $state = $ip_ref->{IPADDRESS}->{$ip}->{STATUS};
	my $perf  = $ip_ref->{IPADDRESS}->{$ip}->{PERF};
	log_message "IP = $ip, state = " . ( defined($state) ? $state : "[unknown]" ) if $debug_level > 1;
	foreach my $host ( sort keys %{ $ip_ref->{IPADDRESS}->{$ip}->{HOSTNAME} } ) {
	    if ( $send_host_check && $host_ref->{HOSTNAME}->{$host}->{IPADDRESS} eq $ip ) {
		my $check_state = undef;
		my $host_state  = undef;

		if ( !( defined($state) && $state =~ /alive/ ) ) {
		    ## Check the parent host(s) - if any are up and the host is down, it's really down
		    my $parent_state = 0;
		    my $parent_down  = '';

		    foreach my $ref_parent ( sort keys %{ $ip_ref->{IPADDRESS}->{$ip}->{HOSTNAME}->{$host}->{PARENT} } ) {
			if ( !( $ref_parent =~ /none/ ) ) {
			    my $parent_ip = $host_ref->{HOSTNAME}->{$ref_parent}->{IPADDRESS};
			    ## If there is a parent, pull the parent state based on hostname from the hash
			    $parent_state = $ip_ref->{IPADDRESS}->{$parent_ip}->{STATUS};
			    log_message "parent state: " . ( defined($parent_state) ? $parent_state : "[unknown]" ) if $debug_level > 1;
			    if ( defined($parent_state) && $parent_state =~ /alive/ ) {
				$parent_state = 1;
			    }
			    else {
				$parent_down .= "$ref_parent ";
				log_message "parent down = $parent_down" if $debug_level > 1;
				$parent_state = 0;
			    }
			}
			else {
			    $parent_state = 1;
			}
		    }
		    if ($parent_state) {
			$check_state = '1';
			$host_state  = 'DOWN';
		    }
		    else {
			$check_state = '2';
			$host_state  = 'UNREACHABLE';
		    }
		}
		else {
		    $check_state = '0';
		    $host_state  = $state;
		}

		log_message "sending host check for ip $ip, host $host" if $debug_level > 1;
		$totalhosts++;

		$host =~ s/^\s+//;
		$host =~ s/\s+$//;
		if ($use_nsca) {
		    push @aggregated_commands, "$host\t$check_state\t$ip $host_state|rta=${perf}ms;;;0;\n";
		}
		else {
		    push @aggregated_commands, "[$unixtime] PROCESS_HOST_CHECK_RESULT;$host;$check_state;$ip $host_state|rta=${perf}ms;;;0;\n";
		}
	    }
	    if ($send_service_check) {
		my $check_state = undef;
		if ( !( defined($state) && $state =~ /alive/ ) ) {
		    $check_state = '2';
		}
		else {
		    $check_state = '0';
		}
		my $state_text = $state;
		foreach $service ( sort keys %{ $ip_ref->{IPADDRESS}->{$ip}->{HOSTNAME}->{$host}->{SERVICENAME} } ) {
		    log_message "sending service check for ip $ip, host $host, service $service" if $debug_level > 1;
		    $servicecount++;
		    if ( defined $NegatedServices{$service}{'in-play'} ) {
			## Perform the state result negation.
			if ( $check_state == 0 ) {
			    $check_state = 2;
			}
			elsif ( $check_state == 2 ) {
			    $check_state = 0;
			}
			if ( defined $NegatedServices{$service}{$check_state} ) {
			    $state_text = $NegatedServices{$service}{$check_state};
			}
		    }
		    if ($use_nsca) {
			push @aggregated_commands, "$host\t$service\t$check_state\t$ip $state_text|rta=${perf}ms;;;0;\n";
		    }
		    else {
			push @aggregated_commands,
			  "[$unixtime] PROCESS_SERVICE_CHECK_RESULT;$host;$service;$check_state;$ip $state_text|rta=${perf}ms;;;0;\n";
		    }
		}
	    }
	    $host_count++;
	    if ( $host_count >= $max_hosts_per_send_nsca ) {
		## Flush aggregated commands.
		if ( $#aggregated_commands >= 0 ) {
		    output_results();
		    $#aggregated_commands = -1;    # truncate the array of messages
		}

		if ( $delay_between_sends > 0 && !$terminate ) {
		    log_message "Staging host check results -- sleeping for $delay_between_sends seconds" if $debug_level > 1;
		    ## This sleep will be cut short should a termination signal be received.
		    sleep $delay_between_sends;
		}
		$host_count = 0;
		last if $terminate;
	    }
	}    # foreach host
	last if $terminate;
    } # foreach ip
    last if $terminate;

    if ($#aggregated_commands >= 0) {
	output_results();
	$#aggregated_commands = -1;	# truncate the array of messages
    }
    last if $terminate;

    $time2 = time;
    my $submit_time = $time2 - $time1;
    log_message "Nagios submit time was " . $submit_time . "s" if $debug_level;

    my $elapsed_time = sprintf("%0.3f", (Time::HiRes::time() - $starttime));
    log_message "Processed $totalhosts hosts and $servicecount services in $elapsed_time seconds" if $debug_level;

    # Compute the passive service check result for the "fping_process" service.
    my $service_result = (($elapsed_time > $elapsed_time_threshold) && ($elapsed_time_threshold > 0)) ? 2 : 0;
    if ($plugin_mode) {
	## Exit gracefully, and output what Nagios expects
	$failed |= $service_result;
	print "Processed $totalhosts pings in $elapsed_time seconds|hosts=$totalhosts;;;0; pingtime=${elapsed_time}s;;"
	  . ( $elapsed_time_threshold > 0 ? $elapsed_time_threshold : "" )
	  . ";0; sends=$result_sends;;;0; submit_time=${submit_time}s;;;0;\n";
	last;
    }
    else {
	# Note that sending this last service check result separately is a relatively expensive operation
	# because of the extra forking it invokes (if we're configured to use send_nsca), as opposed to
	# sending it as the last service check result in the last bunch of results.  But then our timing
	# information probably wouldn't include the full set of sends for all the hosts.  Oh, well.

	if ($use_nsca) {
	    push @aggregated_commands,
		"$fping_host\t$PROGNAME\t$service_result\tProcessed $totalhosts pings in $elapsed_time seconds using $result_sends NSCA sends"
	      . " in $submit_time seconds|hosts=$totalhosts;;;0; pingtime=${elapsed_time}s;;"
	      . ( $elapsed_time_threshold > 0 ? $elapsed_time_threshold : "" )
	      . ";0; sends=$result_sends;;;0; submit_time=${submit_time}s;;;0;\n";
	}
	else {
	    push @aggregated_commands,
		"[$time2] PROCESS_SERVICE_CHECK_RESULT;$fping_host;$PROGNAME;$service_result;Processed $totalhosts pings in $elapsed_time seconds"
	      . " using $result_sends sends to Nagios in $submit_time seconds|hosts=$totalhosts;;;0; pingtime=${elapsed_time}s;;"
	      . ( $elapsed_time_threshold > 0 ? $elapsed_time_threshold : "" )
	      . ";0; sends=$result_sends;;;0; submit_time=${submit_time}s;;;0;\n";
	}
	output_results();
	$#aggregated_commands = -1;	# truncate the array of messages

	# Start the pings every cycle_time, unless they took longer than that for the present cycle.
	my $sleeptime = $cycle_time - $elapsed_time;
	if ( $sleeptime > 0 && !$terminate ) {
	    ## This sleep will be cut short should a termination signal be received.
	    sleep($sleeptime);
	}
    }
}

log_shutdown();
close(LOG);
exit $failed;

__END__

