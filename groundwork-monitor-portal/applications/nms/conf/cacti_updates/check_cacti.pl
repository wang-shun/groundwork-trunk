#!/usr/local/groundwork/perl/bin/perl -w --

#	check_cacti.pl
#
#	Copyright 2011-2013 GroundWork Open Source, Inc. ("GroundWork")
#	All rights reserved. Use is subject to GroundWork commercial license terms.
#
#	Unless required by applicable law or agreed to in writing, software
#	distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
#	WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
#	License for the specific language governing permissions and limitations under
#	the License.

# To Do:
# (*) Re-work the alarm involved in writing to the Nagios command pipe to accommodate
#     the overarching script-execution max_cycle_time timeout, rather than effectively
#     disabling that global timeout.

use strict;
use DBI;
use Getopt::Long;
use Data::Dumper;
use Time::HiRes;
use Time::localtime;

use lib "/usr/local/groundwork/nagios/libexec";
use utils qw(%ERRORS);

use TypedConfig;

my $PROGNAME = "check_cacti";
my $VERSION = "3.0.1";

#######################################################
#
#   Command Line Execution Options
#
#######################################################

my $print_help    = 0;
my $print_version = 0;
my $config_file   = "/usr/local/groundwork/common/etc/check_cacti.conf";
my $debug_config  = 0;
my $plugin_mode                         = 0;    # In plugin mode, run just once and stop with Nagios plugin style output.
my $command_line_elapsed_time_threshold = 0;    # threshold to be passed as an argument

sub print_usage {
    print "usage:  check_cacti.pl [-h] [-v] [-c config_file] [-d] [-p] [-t seconds]\n";
    print "        -h:  print this help message\n";
    print "        -v:  print the version number\n";
    print "        -c config_file:  specify an alternate config file\n";
    print "             (default is $config_file)\n";
    print "        -d:  dump the config file entries (to debug them)\n";
    print "        -p:  run as a plugin, not as a cron job\n";
    print "        -t seconds:  specify the maximum cycle execution time\n";
    print "             beyond which a critical service state is declared,\n";
    print "             overriding the corresponding value in the config file\n";
}

Getopt::Long::Configure ("no_ignore_case");
if (! GetOptions (
    'help'         => \$print_help,
    'version'      => \$print_version,
    'config=s'     => \$config_file,
    'debug-config' => \$debug_config,
    'plugin'       => \$plugin_mode,
    'threshold=s'  => \$command_line_elapsed_time_threshold
    )) {
    print "ERROR:  cannot parse command-line options!\n";
    print_usage;
    exit $ERRORS{'CRITICAL'};
}

if ($print_version) {
    print "$PROGNAME $VERSION\n";
    print "Copyright 2011-2013 GroundWork Open Source, Inc. (\"GroundWork\").  All rights\n";
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
    exit $ERRORS{'CRITICAL'};
}

#######################################################
#
#   Configuration File Handling
#
#######################################################

my $config = TypedConfig->secure_new ($config_file, $debug_config);

#######################################################
#
#   General Program Execution Options
#
#######################################################

# Check Cacti thresholds?  If not, just sleep forever.
# This option is turned off in the default configuration file simply so the script can be
# safely installed before it is locally configured.  To get the software to run, it must be
# turned on in the configuration file once the rest of the setup is correct for your site.
my $enable_processing = $config->get_boolean ('enable_processing');

# Global Debug Level (controls the output of messages in the logfile)
my $debug_level = $config->get_number ('debug_level');

# Send the passive service check results to Nagios?
my $send_to_nagios = $config->get_boolean ('send_to_nagios');

# Service name used in the passive service check result sent to Nagios.
my $cacti_service_name = $config->get_scalar ('cacti_service_name');

# Whether to qualify threshold alerts by checking the threshold fail count
# against the threshold fail trigger.
my $check_thold_fail_count = $config->get_boolean ('check_thold_fail_count');

# Whether to qualify baseline alerts by checking the baseline fail count
# against the baseline fail trigger.
my $check_bl_fail_count = $config->get_boolean ('check_bl_fail_count');

# The hostname for which the check_cacti service result is to be reported;
# that is, the host on which this copy of the script runs.
my $check_cacti_host = $config->get_scalar ('check_cacti_host');

# What subset of hosts to check Cacti thresholds for.  This must be either
# a single Cacti group name, to check only the hosts associated with that
# group (generally associated with a particular child server), or an empty
# string, to check all hosts in Cacti.
# FIX MINOR:  We don't actually have support for this (yet).
my $check_cacti_group = $config->get_scalar ('check_cacti_group');

# How often to repeat the entire set of checks (seconds between starts of
# successive passes), if the script is run as a persistent daemon.  Zero
# means don't repeat; instead, exit after one pass, as is appropriate for
# execution as a cron job.
my $cycle_time = $config->get_number ('cycle_time');

# Use NSCA to send results to a (generally remote) Nagios command pipe?
#  no = write directly to a local Nagios command pipe
# yes = use send_nsca to write to a (generally remote) Nagios command pipe
my $use_nsca = $config->get_boolean ('use_nsca');

# Absolute pathname of the Nagios command pipe.
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
# the check_cacti service to be in a CRITICAL state.  This value may be
# overridden on the command line.  Zero means we don't apply a threshold.
# This value is used strictly to determine the final service result, and
# does not cause the script to expire any earlier than it otherwise would.
my $default_elapsed_time_threshold = $config->get_number ('default_elapsed_time_threshold');

# When to quit, if a given pass of the script hasn't completed all its work by
# the time this many seconds have passed.  A value of zero disables the timeout,
# allowing the script to run forever.
my $max_cycle_time = $config->get_number ('max_cycle_time');

# Set to a pattern that selects the part of a host name to report out.
my $short_hostname_pattern = $config->get_scalar ('short_hostname_pattern');

if ($short_hostname_pattern eq '') {
    # If the user doesn't want any hostname stripping, the config file setting will be
    # an empty string.  For proper usage within the script, we set this pattern to "^#",
    # which cannot match any hostnames since it contains an invalid hostname character.
    $short_hostname_pattern = "^#";
}

# How to access the Cacti database.
my $dbhost = $config->get_scalar ('cacti_db_host');
my $dbname = $config->get_scalar ('cacti_db_name');
my $dbuser = $config->get_scalar ('cacti_db_user');
my $dbpass = $config->get_scalar ('cacti_db_pass');
my $dbtype = undef;
eval {
    $dbtype = $config->get_scalar ('cacti_db_type');
};
if ($@) {
    if ($@ =~ /cannot find/) {
	$dbtype = 'mysql';
    }
    else {
	die $@;
    }
}
my $dbport = undef;
eval {
    $dbport = $config->get_number ('cacti_db_port');
};
if ($@) {
    if ($@ =~ /cannot find/) {
	$dbport = 3306;
    }
    else {
	die $@;
    }
}

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

# Whether to also send a copy of the Cacti threshold data to a secondary server.
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

my $elapsed_time_threshold = 0;         # threshold used for comparison
my           $send_nsca_command = "/usr/local/groundwork/common/bin/send_nsca -H $nsca_host -p $nsca_port -to $nsca_timeout -c /usr/local/groundwork/common/etc/send_nsca.cfg";
my $secondary_send_nsca_command = "/usr/local/groundwork/common/bin/send_nsca -H $secondary_nsca_host -p $secondary_nsca_port -to $secondary_nsca_timeout -c /usr/local/groundwork/common/etc/send_nsca.cfg";
my $failed = 0;		# Will be used as exit status; 0 means successful.

# Variables to be used as quick tests to see if we're interested in particular debug messages.
# FIX MINOR:  allow the config file to specify these states as strings, not just as numbers,
# perhaps by implementing a TypedConfig->get_enum_value() routine so all the hard work is done elsewhere.
my $DEBUG_NONE    = $debug_level == 0;	# turn off all debug info
my $DEBUG_FATAL   = $debug_level >= 1;	# the application is about to die
my $DEBUG_ERROR   = $debug_level >= 2;	# the application has found a serious problem, but will attempt to recover
my $DEBUG_WARNING = $debug_level >= 3;	# the application has found an anomaly, but will try to handle it
my $DEBUG_NOTICE  = $debug_level >= 4;	# the application wants to inform you of a significant event
my $DEBUG_STATS   = $debug_level >= 5;	# the application wants to log statistical data for later analysis
my $DEBUG_INFO    = $debug_level >= 6;	# the application wants to log a potentially interesting event
my $DEBUG_DEBUG   = $debug_level >= 7;	# the application wants to log detailed debugging data

my @aggregated_commands = ();
$#aggregated_commands = $max_hosts_per_send_nsca;	# pre-extend the array, for efficiency
$#aggregated_commands = -1;				# truncate the array, since we don't have any messages yet

#######################################################
#
#   Configuration Processing
#
#######################################################

if ($command_line_elapsed_time_threshold > 0) {
    $elapsed_time_threshold = $command_line_elapsed_time_threshold + 0; # force numeric
}
else {
    $elapsed_time_threshold = $default_elapsed_time_threshold;
}

if ($check_cacti_group ne '') {
    # Try to validate the string given, to prevent an SQL injection attack,
    # even if we use placeholders and value binding below for the same purpose.
    if ($check_cacti_group =~ m:[`~!\$%^&*|'"<>?,()=/\\;\r\n\t\0]:) {
	print "ERROR:  The check_cacti_group setting in the configuration file contains illegal characters.\n";
	sleep (5) if (!$plugin_mode);	# Don't have gwservices restart us immediately, in a tight loop.
	exit $ERRORS{'CRITICAL'};
    }
}

########################################################
#
#   Subroutines
#
########################################################

sub catch_signal {
    my $signame = shift;

    $| = 1;     # force output flushing before we quit
    die "=== Writing to the Nagios command pipe timed out; caught a SIG$signame signal; exiting! ===\n";
}

# The format of a message sent directly to the command pipe is one of:
# [$unixtime] PROCESS_HOST_CHECK_RESULT;$host_name;$status_code;$plugin_output\n
# [$unixtime] PROCESS_SERVICE_CHECK_RESULT;$host_name;$service_description;$return_code;$plugin_output\n
# which is different from the format we pass to send_nsca.

sub send_to_nagios {
    if (scalar(@aggregated_commands)) {
	open(FIFO, "+<:unix", $nagios_cmd_pipe) or die "Could not open the Nagios command pipe: $!";
	local $SIG{ALRM} = \&catch_signal;
	my $first = 0;
	my $last = $first;
	my $message_size;
	my $buffer_size = 0;
	my $index_past_end = scalar(@aggregated_commands);
	for (my $index = 0; $index <= $index_past_end; ++$index) {
	    if ($index < $index_past_end) {
		$message_size = length ($aggregated_commands[$index]);
	    }
	    else {
		$message_size = 0;
	    }
	    if ($index < $index_past_end && $buffer_size + $message_size <= $max_command_pipe_write_size) {
		$buffer_size += $message_size;
	    }
	    else {
		if ($buffer_size > 0) {
		    alarm($max_command_pipe_wait_time);
		    eval {
			print FIFO join('', @aggregated_commands[$first..$last]) or die "Cannot write to the Nagios command pipe: $!";
		    };
		    alarm(0);
		    die "Exiting: $@" if ($@);
		}
		$first = $index;
		$buffer_size = $message_size;
	    }
	    $last = $index;
	}
	close(FIFO);
    }
}

sub output_results {
    if ($use_nsca) {
	# Now send the aggregated commands to send_nsca.

	open NSCA, "|-", $send_nsca_command;
	print NSCA join ('', @aggregated_commands);
	$failed |= ! close NSCA;

	if ($send_to_secondary_NSCA) {
	    open NSCA, "|-", $secondary_send_nsca_command;
	    print NSCA join ('', @aggregated_commands);
	    $failed |= ! close NSCA;
	}
    }
    else {
	send_to_nagios();
    }
}

sub time_text {
    my $unixtime = shift;
    if ($unixtime <= 0) {
	return "0";
    }
    else {
	my $tm = localtime($unixtime);
	return sprintf "%04d-%02d-%02d %02d:%02d:%02d",$tm->year+1900,$tm->mon+1,$tm->mday,$tm->hour,$tm->min,$tm->sec;
    }
}

########################################################
#
#   Program Start
#
########################################################

# Stop if this is just a debugging run.
exit if $debug_config;

if (! $enable_processing) {
    if ($plugin_mode) {
	print "WARNING:  check_cacti processing is not enabled in the config file; it will not return any results.\n";
    }
    else {
	print "WARNING:  check_cacti processing is not enabled in the config file; it will sleep forever.\n";
	# Sleep forever, simply so we don't get continually restarted and waste resources.
	sleep 100000000 if ($cycle_time != 0);
    }
    # We use an exit status of 4 to indicate that the script is disabled.
    exit $ERRORS{'DEPENDENT'};
}

########################################################
#
#   Main Program
#
########################################################

# In case of problems, let's not hang Nagios.
$SIG{'ALRM'} = sub {
    if ($plugin_mode) {
	print "SERVICE STATUS: CRITICAL: Plugin timed out after $max_cycle_time seconds\n";
    }
    else {
	my $service_result = $ERRORS{'CRITICAL'};
	if ($use_nsca) {
	    push @aggregated_commands,
		"$check_cacti_host\t$PROGNAME\t$service_result".
		"\tSERVICE STATUS: CRITICAL: Plugin timed out after $max_cycle_time seconds\n";
	}
	else {
	    my $unixtime = time;
	    push @aggregated_commands,
		"[$unixtime] PROCESS_SERVICE_CHECK_RESULT;$check_cacti_host;$PROGNAME;$service_result".
		";SERVICE STATUS: CRITICAL: Plugin timed out after $max_cycle_time seconds\n";
	}
	output_results;
	$#aggregated_commands = -1;     # truncate the array of messages
	sleep (5);	# Don't have gwservices restart us immediately, in a tight loop.
    }
    exit $ERRORS{'UNKNOWN'};
};

for (;;) {
    my $starttime = Time::HiRes::time();
    my $service_result = $ERRORS{'OK'};

    # FIX MINOR:  this alarm will be disrupted and be ineffective if we write directly to the command pipe
    alarm($max_cycle_time) if ($max_cycle_time > 0);

    #
    #   Connect to Cacti DB
    #
    my $dsn = '';
    if ( defined($dbtype) && $dbtype eq 'postgresql' ) {
	$dsn = "DBI:Pg:dbname=$dbname;host=$dbhost;port=$dbport";
    }
    else {
	$dsn = "DBI:mysql:database=$dbname;host=$dbhost;port=$dbport";
    }
    my $dbh = DBI->connect($dsn, $dbuser, $dbpass, { 'AutoCommit' => 1 });
    if (!$dbh) {
	print "Can't connect to database $dbname. Error: ".$DBI::errstr;
	sleep (5) if (!$plugin_mode);	# Don't have gwservices restart us immediately, in a tight loop.
	exit $ERRORS{'CRITICAL'};
    }

    my ($query,$sth);

    # We just read in the entire table, so we can thereafter do very rapid in-memory lookups.
    # This is considered preferable to reading one row of the table for each threshold in alert status,
    # even if it means that we will read the entire table when no thresholds are in alert, because the
    # overhead of possibly multiple small queries is believed to swamp the overhead of one large query.

    my %service_name = ();
    # This commented-out query is for debugging.  The standard one is for efficiency, containing only what we need.
    # $query = "select id,local_data_id,name,name_cache from data_template_data";
    $query = "select local_data_id,name_cache from data_template_data";
    $sth = $dbh->prepare($query);
    $sth->execute();
    while (my $row=$sth->fetchrow_hashref()) {
	$service_name{$row->{local_data_id}} = $row->{name_cache};
    }
    if (defined($sth->err)) {
	$service_result ||= $ERRORS{'CRITICAL'};
	print "Database problem while fetching Cacti template data: Error=" . $sth->errstr . "; State=" . $sth->state . "\n";
    }
    $sth->finish();

    my %alert_data = ();
    my $thold_alarm;
    my    $bl_alarm;
    ## Set global metric defaults
    ## This SQL is lifted from the Cacti Thold plugin.
    $query = "SELECT thold_data.*, host.description, host.status FROM thold_data left join host on thold_data.host_id=host.id where " .
	"thold_enabled='on' or bl_enabled='on' ORDER BY thold_alert DESC, bl_alert DESC, host.description, rra_id ASC";
    $sth = $dbh->prepare($query);
    $sth->execute() or die $@;
    while (my $row=$sth->fetchrow_hashref()) {
	## autovivify the host_hashref, and keep track of all metric thresholds/baselines monitored for this host
	$alert_data{ $row->{description} }{TOTAL_METRICS}++;
	$alert_data{ $row->{description} }{status} = $row->{status};
	my $host_hashref = $alert_data{ $row->{description} };
	## Check if this metric is in alert, either by threshold comparison or for deviance from baseline.
	$thold_alarm = ($row->{thold_enabled} eq 'on') && $row->{thold_alert} && ((! $check_thold_fail_count) || ($row->{thold_fail_count} >= $row->{thold_fail_trigger}));
	   $bl_alarm = ($row->   {bl_enabled} eq 'on') && $row->   {bl_alert} && ((!    $check_bl_fail_count) || ($row->   {bl_fail_count} >= $row->   {bl_fail_trigger}));
	if ($thold_alarm || $bl_alarm) {
	    ## this incrementing will autovivify as needed, so we don't risk later dereferencing an undef
	    $host_hashref->{TOTAL_ALARMS}++;    # Keep track of all threshold and baseline alarms for this host
	    $host_hashref->{THOLD_ALARMS} += $thold_alarm ? 1 : 0;    # Keep track of all threshold alarms for this host
	    $host_hashref->{BL_ALARMS}    += $bl_alarm    ? 1 : 0;    # Keep track of all  baseline alarms for this host
	    ## This first ID element autovivifies the id_hashref.
	    $host_hashref->{ID}->{$row->{id}}->{LASTREAD} = $row->{lastread};
	    my $id_hashref = $host_hashref->{ID}->{$row->{id}};
	    $id_hashref->{THOLD_HI}    = $row->{thold_hi}  if $thold_alarm;
	    $id_hashref->{THOLD_LOW}   = $row->{thold_low} if $thold_alarm;
	    $id_hashref->{BL_PCT_UP}   = $row->{bl_pct_up}   if  $bl_alarm;
	    $id_hashref->{BL_PCT_DOWN} = $row->{bl_pct_down} if  $bl_alarm;
	    $id_hashref->{SERVICE}     = $service_name{ $row->{rra_id} } if defined $service_name{ $row->{rra_id} };
	}
	else {
	    ## autovivify the alarm counts so we don't risk dereferencing an undef later on, without damaging any existing value
	    $host_hashref->{TOTAL_ALARMS} += 0;
	    $host_hashref->{THOLD_ALARMS} += 0;
	    $host_hashref->{   BL_ALARMS} += 0;
	}
    }
    if (defined($sth->err)) {
	$service_result ||= $ERRORS{'CRITICAL'};
	print "Database problem while fetching Cacti threshold data: Error=" . $sth->errstr . "; State=" . $sth->state . "\n";
    }
    $sth->finish();
    $dbh->disconnect();

    my $unixtime = time;
    $#aggregated_commands = -1;	# truncate the array of messages

    my  $total_hosts = 0;
    my   $host_count = 0;
    my $metric_count = 0;
    my  $alarm_count = 0;
    my $timestamp = time_text(time);
    foreach my $host ( sort keys %alert_data ) {
	my $alarmcount = 0;
	my $sev = 0;
	my $host_hashref  = $alert_data{$host};
	my $total_metrics = $host_hashref->{TOTAL_METRICS};
	my $total_alarms  = $host_hashref->{TOTAL_ALARMS};
	$metric_count += $total_metrics;
	$alarm_count  += $total_alarms;

	my $host_status = sprintf "%0d", $host_hashref->{status};
	my $serviceoutput = '';
	## if host is Up in cacti, return threshold status.
	if ($host_status == 3 || $host_status == 2) {
	    $serviceoutput = "$total_alarms ".
		($total_alarms  == 1 ? 'alarm'  : 'alarms' )." (".$host_hashref->{THOLD_ALARMS}." threshold, ".$host_hashref->{BL_ALARMS}." baseline) found for $total_metrics ".
		($total_metrics == 1 ? 'metric' : 'metrics')." monitored on Cacti host $host, polled at $timestamp.";

	## Now add all alarms to the output string.  We may want to limit this to only one.
	foreach my $id (sort keys %{$host_hashref->{ID}}) {
	    $sev = 2; # An alarm exists, so set the severity to critical.
	    my $id_hashref = $host_hashref->{ID}->{$id};
	    $serviceoutput .= '<br>'.(defined($id_hashref->{SERVICE}) ? $id_hashref->{SERVICE} : "UNKNOWN SERVICE")." last_value=".$id_hashref->{LASTREAD}." is out of bounds;";
	    $serviceoutput .= " upper_threshold=".$id_hashref->{THOLD_HI} if defined($id_hashref->{THOLD_HI});
	    $serviceoutput .= " lower_threshold=".$id_hashref->{THOLD_LOW} if defined($id_hashref->{THOLD_LOW});
	    $serviceoutput .= " baseline_%_up=" .$id_hashref->{BL_PCT_UP} if defined($id_hashref->{BL_PCT_UP});
	    $serviceoutput .= " baseline_%_down=".$id_hashref->{BL_PCT_DOWN} if defined($id_hashref->{BL_PCT_DOWN});
	}
	}
	else {
	    ## if host is not Up in cacti return some other status.
	    $sev = 2;
	    $serviceoutput .= " Target host $host is either Down or Inaccessible.";
	}

	# You wouldn't want this message for a lot of hosts being monitored,
	# if this script is being run as a plugin where all of this output
	# would appear as the plugin's result.  But you might want to spill
	# the beans here if you are running this manually, just to see what
	# is happening during a troubleshooting scenario.
	print "HOST $host: $serviceoutput\n" if $DEBUG_DEBUG;

	if (($host !~ /^\d+\.\d+\.\d+\.\d+$/i) and ($host =~ /$short_hostname_pattern/io)) { $host = $1; }
	if ($use_nsca) {
	    push @aggregated_commands, "$host\t$cacti_service_name\t$sev\t$serviceoutput\n";
	}
	else {
	    push @aggregated_commands, "[$unixtime] PROCESS_SERVICE_CHECK_RESULT;$host;$cacti_service_name;$sev;$serviceoutput\n";
	}
	$total_hosts++;
	$host_count++;
	if ($host_count >= $max_hosts_per_send_nsca) {
	    # Flush aggregated commands.
	    if ($#aggregated_commands >= 0) {
		output_results if $send_to_nagios;
		$#aggregated_commands = -1;     # truncate the array of messages
	    }
	    $host_count = 0;
	}
    }

    if ($#aggregated_commands >= 0) {
	output_results if $send_to_nagios;
	$#aggregated_commands = -1;     # truncate the array of messages
    }

    my $elapsed_time = sprintf("%0.3f", (Time::HiRes::time() - $starttime));

    # Compute the passive service check result for the "check_cacti" service.
    $service_result ||= (($elapsed_time > $elapsed_time_threshold) && ($elapsed_time_threshold > 0)) ? $ERRORS{'CRITICAL'} : $ERRORS{'OK'};
    if ($plugin_mode) {
	$failed |= $service_result;
	## Exit gracefully, and output what Nagios expects.
	## Output our own status so Nagios can alarm.
	## Include timing if requested for QA and scaling.
	print "SERVICE STATUS: ".($service_result == $ERRORS{'OK'} ? 'OK: ' : 'CRITICAL: ').
	    "$alarm_count " .($alarm_count  == 1 ? 'alarm'  : 'alarms' )." found for ".
	    "$metric_count ".($metric_count == 1 ? 'metric' : 'metrics')." processed on ".
	    "$total_hosts " .($total_hosts  == 1 ? 'host'   : 'hosts'  ).
	    ($DEBUG_STATS ? (" in $elapsed_time ".($elapsed_time eq '1.000' ? 'second' : 'seconds')) : '').
	    "|alarms=$alarm_count;;;0; metrics=$metric_count;;;0; hosts=$total_hosts;;;0; ExecutionTime=${elapsed_time}s;;".
	    ($elapsed_time_threshold > 0 ? $elapsed_time_threshold : "").";0;\n";
	last;
    }
    else {
	## Note that sending this last service check result separately is a relatively expensive operation
	## because of the extra forking it invokes (if we're configured to use send_nsca), as opposed to
	## sending it as the last service check result in the last bunch of results.  But then our timing
	## information probably wouldn't include the full set of sends for all the hosts.  Oh, well.

	# FIX LATER:  should these messages include the phrase 'SERVICE STATUS:' as above?
	if ($use_nsca) {
	    push @aggregated_commands,
		"$check_cacti_host\t$PROGNAME\t$service_result".
		"\tProcessed thresholds for $total_hosts hosts in $elapsed_time ".($elapsed_time eq '1.000' ? 'second' : 'seconds').
		"|alarms=$alarm_count;;;0; metrics=$metric_count;;;0; hosts=$total_hosts;;;0; ExecutionTime=${elapsed_time}s;;".
		($elapsed_time_threshold > 0 ? $elapsed_time_threshold : '').";0;\n";
	}
	else {
	    push @aggregated_commands,
		"[$unixtime] PROCESS_SERVICE_CHECK_RESULT;$check_cacti_host;$PROGNAME;$service_result".
		";Processed thresholds for $total_hosts hosts in $elapsed_time ".($elapsed_time eq '1.000' ? 'second' : 'seconds').
		"|alarms=$alarm_count;;;0; metrics=$metric_count;;;0; hosts=$total_hosts;;;0; ExecutionTime=${elapsed_time}s;;".
		($elapsed_time_threshold > 0 ? $elapsed_time_threshold : '').";0;\n";
	}
	output_results;
	$#aggregated_commands = -1;     # truncate the array of messages

	last if ($cycle_time == 0);

	# Start the checks every cycle_time, unless they took longer than that for the present cycle.
	my $sleeptime = $cycle_time - $elapsed_time;
	if ($sleeptime > 0) {
	    sleep ($sleeptime);
	}
    }
}

exit $failed;

__END__
