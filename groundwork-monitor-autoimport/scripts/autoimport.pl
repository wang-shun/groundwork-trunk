#!/usr/local/groundwork/perl/bin/perl -w --

#	autoimport.pl
#
#	Copyright 2007-2012 GroundWork Open Source, Inc. ("GroundWork")
#	All rights reserved. Use is subject to GroundWork commercial license terms.
#
#	Unless required by applicable law or agreed to in writing, software
#	distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
#	WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
#	License for the specific language governing permissions and limitations under
#	the License.

# TO DO:
# (*) make sure all "die" output makes it into the LOG file, if possible
# (*) test using a "cacti" database, not just the "ganglia" database we've used so far

use strict;

use DBI;
use Time::Local;
use Time::HiRes;
use Getopt::Long;

use lib "/usr/local/groundwork/core/monarch/lib";
use MonarchStorProc;

use TypedConfig;

our $PROGNAME;

$PROGNAME = "autoimport";
my $VERSION = "3.0.0";

#######################################################
#
#   Command Line Execution Options
#
#######################################################

my $print_help = 0;
my $print_version = 0;
my $config_file = "/usr/local/groundwork/config/autoimport.conf";
my $debug_config = 0;

sub print_usage {
    print "usage:  autoimport.pl [-h] [-v] [-c config_file] [-d]\n";
    print "        -h:  print this help message\n";
    print "        -v:  print the version number\n";
    print "        -c config_file:  specify an alternate config file\n";
    print "             (default is $config_file)\n";
    print "        -d:  dump the config file entries (to debug them)\n";
}

Getopt::Long::Configure ("no_ignore_case");
if (! GetOptions (
    'help'         => \$print_help,
    'version'      => \$print_version,
    'config=s'     => \$config_file,
    'debug-config' => \$debug_config,
    )) {
    print "ERROR:  cannot parse command-line options!\n";
    print_usage;
    exit 1;
}

if ($print_version) {
    print "$PROGNAME $VERSION\n";
    print "Copyright 2007-2012 GroundWork Open Source, Inc. (\"GroundWork\").  All rights\n";
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
	# 'require' died; $package is not available.
	return;
    }
    else {
	# 'require' succeeded; $package was loaded.
	return 1;
    }
}

#######################################################
#
#   General Program Execution Options
#
#######################################################

my $debug_level = $config->get_number ('debug_level');

my $logfile = $config->get_scalar ('logfile');

my $commit_changes = $config->get_boolean ('commit_changes');

# When defining a monarch host, use the host name instead of the IP address?
my $define_monarch_host_using_dns = $config->get_boolean ('define_monarch_host_using_dns');

my $process_wg_hostgroups = $config->get_boolean ('process_wg_hostgroups');

my $use_hostgroup_program = $config->get_boolean ('use_hostgroup_program');

my $hostgroup_program = $config->get_scalar ('hostgroup_program');

# Set to the name of an external package (not including the .pm filename extension) to call to
# construct hostgroup names from hostnames, or to an empty string if no such package should be used.
my $custom_hostgroup_package = $config->get_scalar ('custom_hostgroup_package');

my $custom_hostgroup_package_options = $config->get_scalar ('custom_hostgroup_package_options');

my $have_custom_hostgroup_package = (! $use_hostgroup_program) && allow $custom_hostgroup_package;
if ((! $use_hostgroup_program) && $custom_hostgroup_package && (! $have_custom_hostgroup_package)) {
    print "Configured external package \"$custom_hostgroup_package\" cannot be found: $@\n";
    exit 1;
}

my $custom_hostgroups = $custom_hostgroup_package->new() if $have_custom_hostgroup_package;
$custom_hostgroup_package->debug($debug_level) if $have_custom_hostgroup_package && $custom_hostgroup_package->can("debug");

my $initialize_hostgroup_options = $have_custom_hostgroup_package && $custom_hostgroup_package->can("initialize_hostgroup_options");
my $hostgroup_name               = $have_custom_hostgroup_package && $custom_hostgroup_package->can("hostgroup_name");

if ($have_custom_hostgroup_package && ! $hostgroup_name) {
    print "Configured external package \"$custom_hostgroup_package\" contains no hostgroup_name() function; aborting!\n";
    exit 1;
}

my $nagioslogfile = $config->get_scalar ('nagioslogfile');

# Ganglia service name to search for in Nagios log file.  Hosts with this service will be matched.
my $ganglia_svc_name = $config->get_scalar ('ganglia_svc_name');

# Cacti service name to search for in Nagios log file.  Hosts with this service will be matched.
my $cacti_svc_name = $config->get_scalar ('cacti_svc_name');

# Set to 1 to assign deleted hosts to the deleted hostgroup.
my $assign_deleted_hosts_to_hostgroup = $config->get_boolean ('assign_deleted_hosts_to_hostgroup');

my $deleted_hostgroup = $config->get_scalar ('deleted_hostgroup');

# Set to 1 to assign new hosts to the new hostgroup.
my $assign_new_hosts_to_hostgroup = $config->get_boolean ('assign_new_hosts_to_hostgroup');

my $new_ganglia_hosts_hostgroup = $config->get_scalar ('new_ganglia_hosts_hostgroup');
my $new_cacti_hosts_hostgroup   = $config->get_scalar ('new_cacti_hosts_hostgroup');

my $assign_host_profiles_by_ganglia_clusters = $config->get_boolean ('assign_host_profiles_by_ganglia_clusters');

my $default_host_profile_ganglia       = $config->get_scalar ('default_host_profile_ganglia');
my $default_service_profile_ganglia    = $config->get_scalar ('default_service_profile_ganglia');
my $default_service_profile_ganglia_id = $config->get_scalar ('default_service_profile_ganglia_id');
my $default_host_profile_cacti         = $config->get_scalar ('default_host_profile_cacti');
my $default_service_profile_cacti      = $config->get_scalar ('default_service_profile_cacti');
my $default_service_profile_cacti_id   = $config->get_scalar ('default_service_profile_cacti_id');

# Set to 1 to process ganglia hosts.
my $process_ganglia_hosts = $config->get_boolean ('process_ganglia_hosts');

my $get_ganglia_host_from_nagios_log = $config->get_boolean ('get_ganglia_host_from_nagios_log');

my $get_ganglia_host_from_ganglia_db = $config->get_boolean ('get_ganglia_host_from_ganglia_db');

my $ganglia_dbtype = $config->get_scalar ('ganglia_dbtype');
my $ganglia_dbname = $config->get_scalar ('ganglia_dbname');
my $ganglia_dbhost = $config->get_scalar ('ganglia_dbhost');
my $ganglia_dbuser = $config->get_scalar ('ganglia_dbuser');
my $ganglia_dbpass = $config->get_scalar ('ganglia_dbpass');

# Set to 1 to process cacti hosts.
my $process_cacti_hosts = $config->get_boolean ('process_cacti_hosts');

my $cacti_dbtype = $config->get_scalar ('cacti_dbtype');
my $cacti_dbname = $config->get_scalar ('cacti_dbname');
my $cacti_dbhost = $config->get_scalar ('cacti_dbhost');
my $cacti_dbuser = $config->get_scalar ('cacti_dbuser');
my $cacti_dbpass = $config->get_scalar ('cacti_dbpass');

my $monarch_user_acct = $config->get_scalar ('monarch_user_acct');

#######################################################
#
#   Custom Options
#
#######################################################

$custom_hostgroups->initialize_hostgroup_options ($custom_hostgroup_package_options, $debug_config) if $initialize_hostgroup_options;

#######################################################
#
#   Execution Global variables
#
#######################################################

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

my $nagios_preflight_check_time		= 0;
my $nagios_preflight_check_start_time	= 0;
my $monarch_commit_time			= 0;
my $monarch_commit_start_time		= 0;

my $hostgroup_name_time		= 0;
my $modification_time		= 0;
my $initial_analysis_time	= 0;
my $middle_analysis_time	= 0;
my $final_analysis_time		= 0;
my $preflight_time		= 0;
my $backup_time			= 0;
my $commit_time			= 0;
my $total_script_time		= 0;

my $middle_analysis_start_time	= 0;
my $final_analysis_start_time	= 0;
my $preflight_start_time	= 0;
my $backup_start_time		= 0;
my $commit_start_time		= 0;
my $script_start_time		= Time::HiRes::time();

my $inserthostcount             = 0;
my $deletehostcount             = 0;
my $serviceprofilesappliedcount = 0;

my $ganglia_host_count		= 0;
my $cacti_host_count		= 0;
my $monarch_initial_host_count	= 0;
my $monarch_final_host_count	= -1;

# The use of certain distinguished values changed when we moved from MySQL to PostgreSQL,
# so we need to understand what type of database we're dealing with.
my $monarch_version;
my $monarch_vstring;
my $is_postgresql;

END {
    if (! defined ($script_start_time)) {
	$script_start_time = Time::HiRes::time();
    }
    if (! defined ($modification_time) || $modification_time == 0) {
	$modification_time = Time::HiRes::time() - $script_start_time;
    }
    if (! defined ($initial_analysis_time) || $initial_analysis_time == 0) {
	$initial_analysis_time = Time::HiRes::time() - $script_start_time;
    }
    if (! defined ($middle_analysis_start_time)) {
	$middle_analysis_time = 0;
    } elsif ($middle_analysis_start_time) {
	$middle_analysis_time = Time::HiRes::time() - $middle_analysis_start_time;
    }
    if (! defined ($final_analysis_start_time)) {
	$final_analysis_time = 0;
    } elsif ($final_analysis_start_time) {
	$final_analysis_time = Time::HiRes::time() - $final_analysis_start_time;
    }
    if (! defined ($nagios_preflight_check_start_time)) {
	$nagios_preflight_check_time = 0;
    } elsif ($nagios_preflight_check_start_time) {
	$nagios_preflight_check_time = Time::HiRes::time() - $nagios_preflight_check_start_time;
    }
    if (! defined ($preflight_start_time)) {
	$preflight_time = 0;
    } elsif ($preflight_start_time) {
	$preflight_time = Time::HiRes::time() - $preflight_start_time;
    }
    if (! defined ($backup_start_time)) {
	$backup_time = 0;
    } elsif ($backup_start_time) {
	$backup_time = Time::HiRes::time() - $backup_start_time;
    }
    if (! defined ($monarch_commit_start_time)) {
	$monarch_commit_time = 0;
    } elsif ($monarch_commit_start_time) {
	$monarch_commit_time = Time::HiRes::time() - $monarch_commit_start_time;
    }
    if (! defined ($commit_start_time)) {
	$commit_time = 0;
    } elsif ($commit_start_time) {
	$commit_time = Time::HiRes::time() - $commit_start_time;
    }
    my %final_hosts = ();
    eval {
	%final_hosts = defined(&StorProc::get_hosts) ? StorProc->get_hosts() : ();
    };
    if ($@) {
	chomp $@;
	print LOG "Error:  cannot read the Monarch database to find the final host count:\n$@\n";
    }
    else {
	$monarch_final_host_count = scalar keys %final_hosts;
    }
    if (! defined ($total_script_time) || $total_script_time == 0) {
	$total_script_time = Time::HiRes::time() - $script_start_time;
    }
    print_statistics() if $DEBUG_STATS;
}

my $default_host_profile_ganglia_id = undef;
my $default_host_profile_cacti_id   = undef;
my ($nagios_ver, $nagios_bin, $nagios_etc, $monarch_home, $backup_dir, $is_portal, $upload_dir) = ();
my ($sec,$min,$hour,$mday,$mon,$year,$wday,$yday,$isdst) = localtime(time);
$year += 1900;
my $month = qw(January February March April May June July August September October November December)[$mon];
my $timestring = sprintf "%02d:%02d:%02d",$hour,$min,$sec;
my $thisday = qw(Sunday Monday Tuesday Wednesday Thursday Friday Saturday)[$wday];

my $monarch_hostgroups = undef;
my $monarch_host_ref = undef;
my $ganglia_host_ref = undef;
my $cacti_host_ref = undef;
my %hostprofile = ();
my %hostprofile_name_by_id = ();
my %ganglia_cluster_by_host_profile_id = ();

# Let's not assume this table exists until we actually verify that we can access it.
my $contactgroup_assign_table_exists = 0;

########################################################
#
#   Program Start
#
########################################################

# Stop if this is just a debugging run.
exit if $debug_config;

# If we receive certain common signals, exit cleanly, so our print_statistics() routine
# will get called to spill out timing statistics for whatever phases have been executed
# so far.  Yes, this can be considered a bit dangerous because of async-unsafe invocation
# of the underlying C library routines (we might get a core dump), but since we're going
# down, we're willing to risk that to get at least a little summary information about
# what happened so far.  If you don't want to risk that, comment out the assignment of
# $SIG{} values just below.
sub handle_exit_signal {
    my $signame = shift;
    if (defined (fileno LOG)) {
	print LOG "\n";
	print LOG "@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@\n";
	print LOG "Received a SIG$signame signal; aborting!\n";
	print LOG "@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@\n";
	print LOG "\n";
    }
    die "Received a SIG$signame signal; aborting!\n";
}

$SIG{HUP}  = \&handle_exit_signal;
$SIG{INT}  = \&handle_exit_signal;
$SIG{TERM} = \&handle_exit_signal;

sub error {
    my $msg = shift;
    my $die = shift;
    print LOG $msg;
    if ($die) {
	print $msg;
	print LOG "Exiting!\n";
	exit 1;
    }
    return;
}

# FIX LATER:  Add in statistics that show how long each phase of the auto-commit operation took:
# backup, analysis, monarch updating, pre-flight test, commit.  Then run the script on a large
# database, and break down some of the larger periods into smaller chunks to isolate exactly what
# operations are consuming the most time.
sub print_statistics {
    if (defined (fileno LOG)) {
	print LOG "\n";
	print LOG "================================================================================================\n";
	print LOG "AUTO-IMPORT STATISTICS\n";
	print LOG "------------------------------------------------------------------------------------------------\n";
	print LOG "\n";
	print LOG "[Overall Timing]\n";
	print LOG "            Analysis/Modification Time: " . sprintf("%9.3f", $modification_time) . " seconds\n";
	print LOG "           Nagios Pre-flight Test Time: " . sprintf("%9.3f", $preflight_time)    . " seconds\n";
	print LOG "          Monarch Database Backup Time: " . sprintf("%9.3f", $backup_time)       . " seconds\n";
	print LOG "    Monarch Commit/Nagios Restart Time: " . sprintf("%9.3f", $commit_time)       . " seconds\n";
	print LOG "    =====================================================\n";
	print LOG "           Total Script Execution Time: " . sprintf("%9.3f", $total_script_time) . " seconds\n";

	# FIX LATER:  Let's add other timing component categories that might look promising for optimization.
	# These include oft-repeated database operations, to begin with.  But the vast majority of the overall
	# time is in the pre-flight test, so that's where optimization effort is most critical.
	print LOG "\n";
	print LOG "[Subsidiary Timing Components] (some of these measurement intervals overlap)\n";
	print LOG "        Initial Database Analysis Time: " . sprintf("%9.3f", $initial_analysis_time) . " seconds (" .
	    sprintf("%5.1f", ($initial_analysis_time / $modification_time) * 100.0) . "% of analysis/modification time)\n";
	print LOG "         Middle Database Analysis Time: " . sprintf("%9.3f", $middle_analysis_time)  . " seconds (" .
	    sprintf("%5.1f", ($middle_analysis_time / $modification_time) * 100.0) . "% of analysis/modification time)\n";
	print LOG "          Final Database Analysis Time: " . sprintf("%9.3f", $final_analysis_time)   . " seconds (" .
	    sprintf("%5.1f", ($final_analysis_time / $modification_time) * 100.0) . "% of analysis/modification time)\n";
	print LOG "     Hostgroup Name Determination Time: " . sprintf("%9.3f", $hostgroup_name_time)   . " seconds (" .
	    sprintf("%5.1f", ($hostgroup_name_time / $modification_time) * 100.0) . "% of analysis/modification time)\n";

	print LOG "          Nagios Pre-flight Check Time: " . sprintf("%9.3f", $nagios_preflight_check_time) . " seconds";
	if ($preflight_time) {
	    print LOG " (" . sprintf("%5.1f", ($nagios_preflight_check_time / $preflight_time) * 100.0) . "% of pre-flight time)\n";
	} else {
	    print LOG "\n";
	}
	print LOG "                   Monarch Commit Time: " . sprintf("%9.3f", $monarch_commit_time) . " seconds";
	if ($commit_time) {
	    print LOG " (" . sprintf("%5.1f", ($monarch_commit_time / $commit_time) * 100.0) . "% of commit time)\n";
	} else {
	    print LOG "\n";
	}

	my $final_host_count = $monarch_final_host_count >= 0 ? sprintf("%8d", $monarch_final_host_count) : ' unknown';
	print LOG "\n";
	print LOG "[Host Counts]\n";
	print LOG '    Hosts      found in "ganglia" database: ' . sprintf("%8d",         $ganglia_host_count) . "\n";
	print LOG '    Hosts      found in   "cacti" database: ' . sprintf("%8d",           $cacti_host_count) . "\n";
	print LOG "    ================================================\n";
	print LOG '    Hosts  initially in "monarch" database: ' . sprintf("%8d", $monarch_initial_host_count) . "\n";
	print LOG '    Hosts inserted into "monarch" database: ' . sprintf("%8d",            $inserthostcount) . "\n";
	print LOG '    Hosts  deleted from "monarch" database: ' . sprintf("%8d",            $deletehostcount) .
	    " (moved to \"$deleted_hostgroup\" hostgroup)\n";
	print LOG "    ================================================\n";
	print LOG '    Hosts    finally in "monarch" database: ' . $final_host_count . "\n";

	# FIX LATER:  Let's add other counts, such as the number of times certain operations take place,
	# such as particular database lookups, host additions, host deletions, the number of hosts in and
	# not in the $deleted_hostgroup at script startup and termination, etc.
	# print LOG "\n";
	# print LOG "[Subsidiary Host Counts]\n";

	print LOG "\n";
	print LOG "================================================================================================\n";
	print LOG "\n";
    }
}

sub ganglia_cluster_name {
    my $host = $_[0];
    return (defined ($ganglia_host_ref->{NAME}->{$host}->{CLUSTER}) ? $ganglia_host_ref->{NAME}->{$host}->{CLUSTER} : "Unknown Ganglia Cluster");
}

sub ganglia_cluster_host_profile_id {
    my $host = $_[0];
    my $cluster = ganglia_cluster_name($host);
    return $hostprofile{$cluster} if (defined ($hostprofile{$cluster}));
    print LOG "\tWARNING:  Could not find host profile \"$cluster\" in Monarch; defaulting host profile for host \"$host\" to \"$default_host_profile_ganglia\".\n" if $DEBUG_WARNING;
    return $default_host_profile_ganglia_id;
}

#
# This subroutine was copied from monarch_ez.cgi, then significantly revised.
# It's a bit hacky in that it takes a single hostname followed by an array of host IDs.
# In practice, it only ever gets called with a single-entry array of host IDs.
#
sub apply_host_profile($$$) {
    my $host = $_[0];
    my $hosts_ref = $_[1];
    my $hostprofileid = $_[2];
    my @errors = ();
    my @hosts = @{$hosts_ref};
    # my $apply_services = $query->param('apply_services');
    my %profile = StorProc->fetch_one('profiles_host','hostprofile_id',$hostprofileid);
    my %where = ('hostprofile_id' => $hostprofileid);
    my @profiles = StorProc->fetch_list_where('profile_host_profile_service','serviceprofile_id',\%where);

    foreach my $hid (@hosts) {
	if ($process_wg_hostgroups) {
	    my $hostgroup_name_start_time = Time::HiRes::time();
	    my $hg_name = $use_hostgroup_program ? (`$hostgroup_program $host`) : $custom_hostgroup_package->hostgroup_name($host);
	    $hostgroup_name_time += Time::HiRes::time() - $hostgroup_name_start_time;
	    chomp $hg_name;
	    if ($hg_name !~ /invalid/) {
		if (defined ($monarch_hostgroups->{NAME}->{$hg_name}->{ID})) {
		    print LOG "\tApplying hostgroup \"$hg_name\" to host $host.\n" if $DEBUG_INFO;
		    my @vals = ($monarch_hostgroups->{NAME}->{$hg_name}->{ID},$hid);
		    my $result = StorProc->insert_obj('hostgroup_host',\@vals);
		    if ($result =~ /^Error/) { push @errors, $result }
		}
		else {
		    print LOG "Missing hostgroup name \"$hg_name\"; not applied to host $host.\n" if $DEBUG_WARNING;
		}
	    }
	    else {
		print LOG "Invalid host name $host. Not applied to any hostgroup.\n" if $DEBUG_ERROR;
	    }
	}
	else {
	    my %w = ('hostprofile_id' => $profile{'hostprofile_id'});
	    my @hostgroups = StorProc->fetch_list_where('profile_hostgroup','hostgroup_id',\%w);
	    foreach my $hgid (@hostgroups) {
		my @vals = ($hgid,$hid);
		my $result = StorProc->insert_obj('hostgroup_host',\@vals);
		if ($result =~ /^Error/) { push @errors, $result }
	    }
	}
	if ($contactgroup_assign_table_exists) {
	    my %w = ('type' => 'host_profiles','object' => $profile{'hostprofile_id'});
	    my @contactgroups = StorProc->fetch_list_where('contactgroup_assign','contactgroup_id',\%w);
	    foreach my $cgid (@contactgroups) {
		my @vals = ($cgid,'hosts',$hid);
		my $result = StorProc->insert_obj('contactgroup_assign',\@vals);
		if ($result =~ /^Error/) { push @errors, $result }
	    }
	}
	else {
	    my %w = ('hostprofile_id' => $profile{'hostprofile_id'});
	    my @contactgroups = StorProc->fetch_list_where('contactgroup_host_profile','contactgroup_id',\%w);
	    foreach my $cgid (@contactgroups) {
		my @vals = ($cgid,$hid);
		my $result = StorProc->insert_obj('contactgroup_host',\@vals);
		if ($result =~ /^Error/) { push @errors, $result }
	    }
	}
	my $result = StorProc->delete_all('serviceprofile_host','host_id',$hid);
	if ($result =~ /^Error/) { push @errors, $result }
	foreach my $spid (@profiles) {
	    if (!$spid) { next; }
	    my %w = ('host_id' => $hid,'serviceprofile_id' => $spid);
	    my %p = StorProc->fetch_one_where('serviceprofile_host',\%w);
	    unless ($p{'host_id'}) {
		my @vals = ($spid,$hid);
		my $result = StorProc->insert_obj('serviceprofile_host',\@vals);
		if ($result =~ /^Error/) { push @errors, $result }
	    }
	}
    }

    my %vals = ('host_escalation_id' => $profile{'host_escalation_id'});
    my $result = StorProc->update_obj('hosts','hostprofile_id',$profile{'hostprofile_id'},\%vals);
    if ($result =~ /^Error/) { push @errors, $result }

    %vals = ('service_escalation_id' => $profile{'service_escalation_id'});
    $result = StorProc->update_obj('hosts','hostprofile_id',$profile{'hostprofile_id'},\%vals);
    if ($result =~ /^Error/) { push @errors, $result }

    %vals = ('hosttemplate_id' => $profile{'host_template_id'});
    $result = StorProc->update_obj('hosts','hostprofile_id',$profile{'hostprofile_id'},\%vals);
    if ($result =~ /^Error/) { push @errors, $result }
    my @errs = StorProc->host_profile_apply($profile{'hostprofile_id'},\@hosts);
    if (@errs) { push (@errors,@errs) }

    %vals = ('hostextinfo_id' => $profile{'host_extinfo_id'});
    $result = StorProc->update_obj('hosts','hostprofile_id',$profile{'hostprofile_id'},\%vals);
    if ($result =~ /^Error/) { push @errors, $result }
    my ($cnt, $err) = StorProc->service_profile_apply(\@profiles,'replace',\@hosts);
    if ($err) { push (@errors, @{$err}) }

    my %w = ('hostprofile_id' => $profile{'hostprofile_id'});
    my @host_ids = StorProc->fetch_list_where('hosts','host_id',\%w);
    my @externals = StorProc->fetch_list_where('external_host_profile','external_id',\%w);

    my $modified = $is_postgresql ? \'0+0' : '0+0';
    foreach my $hid (@host_ids) {
	my $result = StorProc->delete_all('external_host','host_id',$hid);
	if ($result =~ /^Error/) { push @errors, $result }
	foreach my $ext (@externals) {
	    my %e = StorProc->fetch_one('externals','external_id',$ext);
	    my @vals = ($ext,$hid,$e{'display'},$modified);
	    $result = StorProc->insert_obj('external_host',\@vals);
	    if ($result =~ /^Error/) { push @errors, $result }
	}
    }
    return \@errors;
}

sub apply_service_profile($$) {
    my $hosts_ref = $_[0];
    my $serviceprofiles_ref = $_[1];
    my @errors = ();
    my @hosts = @{$hosts_ref};
    my @profiles = @{$serviceprofiles_ref};
    # FIX LATER:  The nested queries here are not designed for efficiency.
    # Better would be to pull back the entire content of the serviceprofile_host
    # table in one operation, then just probe the local copy on each iteration.
    foreach my $hid (@hosts) {
	foreach my $spid (@profiles) {
	    if (!$spid) { next; }
	    my %w = ('host_id' => $hid,'serviceprofile_id' => $spid);
	    my %p = StorProc->fetch_one_where('serviceprofile_host',\%w);
	    unless ($p{'host_id'}) {
		my @vals = ($spid,$hid);
		my $result = StorProc->insert_obj('serviceprofile_host',\@vals);
		if ($result =~ /^Error/) { push @errors, $result }
	    }
	}
    }
    # my ($cnt, $err) = StorProc->service_profile_apply(\@profiles,'replace',\@hosts);
    my ($cnt, $err) = StorProc->service_profile_apply(\@profiles,'merge',\@hosts);
    if ($err) { push (@errors, @{$err}) }
    return \@errors;
}

sub get_configs() {
    my %where = ('type' => 'config');
    my %objects = StorProc->fetch_list_hash_array('setup',\%where);
    $is_portal    = $objects{'is_portal'}[2];
    $nagios_ver   = $objects{'nagios_version'}[2];
    $nagios_bin   = $objects{'nagios_bin'}[2];
    $nagios_etc   = $objects{'nagios_etc'}[2];
    $monarch_home = $objects{'monarch_home'}[2];
    # $monarch_ver = $objects{'monarch_version'}[2];
    $backup_dir   = $objects{'backup_dir'}[2];
    $upload_dir   = $objects{'upload_dir'}[2];
}

sub pre_flight_test() {
    my @errors  = ();
    my @results = ();

    my %nagios_cfg = StorProc->fetch_one( 'setup', 'name', 'log_file' );
    my %nagios_cgi = StorProc->fetch_one( 'setup', 'name', 'default_user_name' );
    if ( $nagios_cfg{'value'} && $nagios_cgi{'type'} ) {
	$nagios_preflight_check_start_time = Time::HiRes::time();
	my ($errors, $results, $files) =
	    StorProc->synchronized_preflight( $monarch_user_acct, $nagios_ver, $nagios_etc, $nagios_bin, $monarch_home, '', '' );
	push @errors,  "Pre-flight Errors:",  @$errors  if @$errors;
	push @results, "Pre-flight Results:", @$results;
	$nagios_preflight_check_time       = Time::HiRes::time() - $nagios_preflight_check_start_time;
	$nagios_preflight_check_start_time = 0;
    }
    my $ok_flag = 0;
    foreach my $line (@errors) {
	print LOG $line . "\n";
    }
    foreach my $line (@results) {
	print LOG $line . "\n";
	if ( $line =~ /Success/i ) {
	    $ok_flag = 1;
	}
    }
    if ($ok_flag) {
	print LOG "\nPreflight check OK.\n";
	return 1;
    }
    else {
	print LOG "\nPreflight check failed.\n";
	return 0;
    }
}

sub commit($) {
    my $action  = shift;
    my $status  = 1;  # presume success until we find otherwise
    my @errors  = ();
    my @results = ();
    my @timings = ();

    if ($action eq 'backup') {
	print LOG "Starting commit \"$action\" process.\n";
	my ($backup, $errors) = StorProc->backup($nagios_etc,$backup_dir);
	if (@$errors) {
	    push @errors, "Problem(s) backing up files and/or database to $backup:", @$errors;
	    $status = 0;
	}
	else {
	    push @results, "Files backed up to $backup.\n";
	}
    }
    elsif ($action eq 'commit') {
	print LOG "Starting commit \"$action\" process.\n";
	$monarch_commit_start_time = Time::HiRes::time();
	my ($errors, $results, $timings) =
	    StorProc->synchronized_commit( $monarch_user_acct, $nagios_ver, $nagios_etc, $nagios_bin, $monarch_home, '' );
	push @errors,  "Commit Errors:",  @$errors  if @$errors;
	push @results, "Commit Results:", @$results if @$results;
	push @timings, "Commit Timing:",  @$timings if @$timings;
	$status = 0 if @$errors;
	$monarch_commit_time = Time::HiRes::time() - $monarch_commit_start_time;
	$monarch_commit_start_time = 0;
    }
    if ($DEBUG_ERROR && @errors) {
	foreach my $line (@errors) {
	    print LOG $line."\n";
	}
    }
    if ($DEBUG_NOTICE && @results) {
	foreach my $line (@results) {
	    print LOG $line."\n";
	}
	print LOG "Completed commit \"$action\" process.\n";
    }
    if ($DEBUG_STATS && @timings) {
	foreach my $line (@timings) {
	    print LOG $line."\n";
	}
    }
    return $status;
}

sub get_ganglia_hosts {
    my $dsn = '';
    if ( defined($ganglia_dbtype) && $ganglia_dbtype eq 'postgresql' ) {
        $dsn = "DBI:Pg:dbname=$ganglia_dbname;host=$ganglia_dbhost";
    }
    else {  
        $dsn = "DBI:mysql:database=$ganglia_dbname;host=$ganglia_dbhost";
    }
    my $dbh = DBI->connect( $dsn, $ganglia_dbuser, $ganglia_dbpass, { 'AutoCommit' => 1 } );
    if (!$dbh) {
	print LOG "ERROR:  Cannot connect to database $ganglia_dbname: ", $DBI::errstr;
	exit 2;
    }
    my ($query,$sth);
    my $host_ref = undef;

    $query = "SELECT Name as \"Name\", IPAddress as \"IPAddress\" from host";
    $sth = $dbh->prepare($query);
    $sth->execute() or die $@;
    while (my $row=$sth->fetchrow_hashref()) {
	my $tmphost = $row->{Name};
	# Strip domain if not IP address.
	if (($tmphost !~ /^\d+\.\d+\.\d+\.\d+$/i) and ($tmphost =~ /(\S+?)\./i)) {
	    $tmphost = $1;
	}
	# The test for the IPAddress field will discard rows for hosts like "Default", which is just a placeholder.
	if ($row->{IPAddress} and $row->{Name}) {
	    if ($define_monarch_host_using_dns) {
		# Set the "IP address" to be the FQHN.
		$host_ref->{NAME}->{$tmphost}->{IP} = $row->{Name};
	    }
	    else {
		# Capture the known IP address.
		$host_ref->{NAME}->{$tmphost}->{IP} = $row->{IPAddress};
	    }
	    $host_ref->{NAME}->{$tmphost}->{IN_GANGLIA} = 1;
	}
	if ($monarch_host_ref->{NAME}->{$tmphost}) {
	    $host_ref->{NAME}->{$tmphost}->{IN_MONARCH} = 1;
	}
    }
    $sth->finish();

    $query = "SELECT distinct h.Name as hostname, c.Name as clustername from host h, cluster c, hostinstance hi where hi.HostID = h.HostID and c.ClusterID = hi.ClusterID";
    $sth = $dbh->prepare($query);
    $sth->execute() or die $@;
    while (my $row=$sth->fetchrow_hashref()) {
	my $tmphost = $row->{hostname};
	# Strip domain if not IP address.
	if (($tmphost !~ /^\d+\.\d+\.\d+\.\d+$/i) and ($tmphost =~ /(\S+?)\./i)) {
	    $tmphost = $1;
	}
	# For each host, we only save the name of the last cluster found which contains that host.
	$host_ref->{NAME}->{$tmphost}->{CLUSTER} = $row->{clustername};
    }
    $sth->finish();

    $query = "SELECT Name as \"Name\" from cluster";
    $sth = $dbh->prepare($query);
    $sth->execute() or die $@;
    while (my $row=$sth->fetchrow_hashref()) {
	$ganglia_cluster_by_host_profile_id{$hostprofile{$row->{Name}}} = $row->{Name};
    }
    $sth->finish();

    $dbh->disconnect();
    return $host_ref;
}

sub get_cacti_hosts {
    my $dsn = '';
    if ( defined($cacti_dbtype) && $cacti_dbtype eq 'postgresql' ) {
        $dsn = "DBI:Pg:dbname=$cacti_dbname;host=$cacti_dbhost";
    }
    else {  
        $dsn = "DBI:mysql:database=$cacti_dbname;host=$cacti_dbhost";
    }
    my $dbh = DBI->connect( $dsn, $cacti_dbuser, $cacti_dbpass, { 'AutoCommit' => 1 } );
    if (!$dbh) {
	print LOG "ERROR:  Cannot connect to database $cacti_dbname: ", $DBI::errstr;
	exit 2;
    }
    my ($query,$sth);
    my $host_ref = undef;

    # Set global metric defaults.
    # Lift this SQL from the cacti thold plugin.
    $query = "SELECT hostname, description from host";
    $sth = $dbh->prepare($query);
    $sth->execute() or die $@;
    while (my $row=$sth->fetchrow_hashref()) {
	# The usage of the host.hostname and host.description fields may be a little confusing,
	# if you try to interpret those field names literally.
	#
	# We expect the host.description field to contain the hostname known to GroundWork Monitor,
	# so we can match it up there properly and get the proper associations made for when we
	# want to hook up the Cacti service for this host.  The point is, the description field is
	# not an arbitrary text characterization of the node; its content must be tightly controlled.
	#
	# We expect the host.hostname field to contain either a FQHN or an IP address; these values
	# might not match the unqualified name by which the node is known within GroundWork Monitor.
	#
	# These conventions for the use of host.description and host.hostname must be understood
	# when you add nodes to Cacti.
	my $hostname = $row->{description};
	# Strip any domain name, to get just the unqualified hostname.
	# We assume the description did not contain an IP address.
	if ($hostname =~ /(\S+?)\./i) {
	    $hostname = $1;
	}
	# Capture the IP address, assuming description=>hostname, hostname=>ip address as above.
	$host_ref->{NAME}->{$hostname}->{IP} = $row->{hostname};
    }
    $sth->finish();

    $dbh->disconnect();
    return $host_ref;
}

open (LOG, ">>", $logfile) or die "Cannot open log file $logfile\n";
print LOG "##########################################################################################\n";
print LOG "Starting data load on $thisday, $month $mday, $year at $timestring.\n";

# We re-open the STDERR stream as a duplicate of the LOG stream, to capture any output
# written to STDERR (from, say, any Perl warnings generated by poor coding).  This also
# is used to ensure that the output from STDERR is properly interleaved when-it-happens
# with the output from LOG, to simplify interpreting the log file.
if ( !open( STDERR, '>>&LOG' ) ) {
    print "ERROR:  Cannot redirect STDERR to LOG: $!\n";
    exit 1;
}
else {
    ## Autoflush the standard output on every single write, to avoid problems
    ## with block i/o and badly interleaved output lines on LOG and STDERR. 
    ## Note that it is the LOG stream that needs to be autoflushed (to avoid
    ## buffering those lines, so they are output immediately just like STDERR
    ## lines), not the STDERR stream, to achieve proper interleaving.
    LOG->autoflush(1);
}

# Connect to Monarch
StorProc->dbconnect() or error("FATAL:  Cannot connect to Monarch database\n", 1);

my %results = ();
eval {  
    %results = StorProc->fetch_one( 'setup', 'name', 'monarch_version' );
};
if ($@) {
    chomp $@;
    error("FATAL:  Database access error while fetching Monarch version: $@\n", 1);
}

# Later logic that depends on the Monarch version would fail if it is not available.
error("FATAL:  Monarch version is not available.\n", 1) if not $results{value};

$monarch_version = $results{value};
$monarch_vstring = pack( 'U*', split( /\./, $monarch_version ) );
$is_postgresql = ( $monarch_vstring ge v4.0 );

# Find out whether the Monarch database we're running against contains a
# specific table of interest, so we can portably adapt the behavior of
# this script to different versions of the database.
#
# The empty catch_warn() routine suppresses an extra error message that would
# otherwise be printed in the output of this script in spite of the fact that
# we have encapsulated the possibly-failing count() invocation in an eval{}.
# The extra error message arises because StorProc allows the DBI connection
# to the Monarch database to default the PrintError setting to "on", which
# causes duplicate copies of error messages to be printed.
sub catch_warn { }
$contactgroup_assign_table_exists = (eval { $SIG{__WARN__}=\&catch_warn; StorProc->count('contactgroup_assign'); } || -1) >= 0;

# Create List of known hosts
my %hosts = StorProc->get_hosts();
$monarch_initial_host_count = scalar keys %hosts;

# FIX MINOR:  The assigning of values to $default_service_profile_ganglia_id
# and $default_service_profile_cacti_id here is kind of dumb.  Why not just
# look up the $default_service_profile_ganglia and $default_service_profile_cacti
# profiles directly in the profiles_service table, once at the beginning?  If we
# don't do that, then these two variables only get assigned (if they were defined
# as empty strings in the config file, which is the standard setup) if the default
# service profiles are already assigned to at least one host in Monarch.  Simply
# put, that makes no sense.

print LOG "Monarch Hosts:\n" if $DEBUG_INFO;
foreach my $host (sort keys %hosts) {
    # $monarch_host_ref->{HOSTID}->{$hosts{$host}}->{NAME} = $host;
    $monarch_host_ref->{NAME}->{$host}->{ID} = $hosts{$host};
    my %wherehash = ('host_id' => $monarch_host_ref->{NAME}->{$host}->{ID});
    my %values = StorProc->fetch_one_where('hosts',\%wherehash);
    $monarch_host_ref->{NAME}->{$host}->{IP} = $values{address};
    print LOG "\tHost=$host, IP=$monarch_host_ref->{NAME}->{$host}->{IP}:" if $DEBUG_INFO;
    %wherehash = ('host_id' => $hosts{$host});
    my @serviceprofiles = StorProc->fetch_list_where('serviceprofile_host','serviceprofile_id',\%wherehash,'serviceprofile_id');
    foreach my $profileid (@serviceprofiles) {
	my %serviceprofilehash = StorProc->fetch_one('profiles_service','serviceprofile_id',$profileid);
	print LOG " Service Profile=\"$serviceprofilehash{name}\"" if $DEBUG_INFO;
	if ($serviceprofilehash{name} eq $default_service_profile_ganglia) {
	    $default_service_profile_ganglia_id = $profileid;
	    $monarch_host_ref->{NAME}->{$host}->{ASSIGNED_GANGLIA_SERVICE_PROFILE} = 1;
	}
	elsif ($serviceprofilehash{name} eq $default_service_profile_cacti) {
	    $default_service_profile_cacti_id = $profileid;
	    $monarch_host_ref->{NAME}->{$host}->{ASSIGNED_CACTI_SERVICE_PROFILE} = 1;
	}
    }
    print LOG "\n" if $DEBUG_INFO;
}

# Find default profile to assign to new hosts.
my %profiles = StorProc->get_profiles();
my $hostprofilefound_ganglia = 0;
my $hostprofilefound_cacti = 0;
print LOG "Monarch Profiles:\n";
foreach my $hostprof (sort keys %profiles) {
    print LOG "\tHost Profile $hostprof, Description=$profiles{$hostprof}{'description'}\n";
    if ($hostprof eq $default_host_profile_ganglia) {
	$hostprofilefound_ganglia = 1;
    }
    if ($hostprof eq $default_host_profile_cacti) {
	$hostprofilefound_cacti = 1;
    }
}

%hostprofile_name_by_id = StorProc->get_table_objects('profiles_host',1);

%hostprofile = StorProc->get_table_objects('profiles_host');
if ($process_ganglia_hosts) {
    if ($hostprofilefound_ganglia) {
	$default_host_profile_ganglia_id = $hostprofile{$default_host_profile_ganglia};
	print LOG "Found default Ganglia host profile \"$default_host_profile_ganglia\" with host profile id=$default_host_profile_ganglia_id\n";
    }
    else {
	print LOG "Error. Default Ganglia host profile \"$default_host_profile_ganglia\" not found in Monarch database. Exiting.\n";
	exit 2;
    }
}
if ($process_cacti_hosts) {
    if ($hostprofilefound_cacti) {
	$default_host_profile_cacti_id = $hostprofile{$default_host_profile_cacti};
	print LOG "Found default Cacti host profile \"$default_host_profile_cacti\" with host profile id=$default_host_profile_cacti_id\n";
    }
    else {
	print LOG "Error. Default Cacti host profile \"$default_host_profile_cacti\" not found in Monarch database. Exiting.\n";
	exit 2;
    }
}

if ($process_ganglia_hosts) {
    if ($get_ganglia_host_from_nagios_log) {
	# Read Nagios log and see if any external commands submitted to unknown hosts.
	open (NAGIOS, "$nagioslogfile") or error("FATAL:  Cannot open Nagios log file $nagioslogfile\n", 1);
	while (my $line=<NAGIOS>) {
	    # print LOG "Line: $line\n";
	    # Sample check_ganglia message:
	    # [1151039011] EXTERNAL COMMAND: PROCESS_SERVICE_CHECK_RESULT;gmaus19;ganglia;2;Cluster unspecified, Host gmaus19.Cadence.COM, IP 172.28.113.200,  cpu_speed:440 MHz, swap_total:2783256 KB, os_name:SunOS , cpu_num:1 CPUs, mem_total:1001600 KB, Booted at 11:41:45-2006/03/15 machine_type:sun4u , os_release:5.9 ,<br>Alarm Counts:<FONT COLOR=RED>CRITICAL (5),</FONT> <FONT COLOR=GREEN>OK (17),</FONT> <br><FONT COLOR=RED>Critical</FONT>: cpu_aidle (0.0 %),cpu_idle (0.2 %),disk_free (0.000 GB),disk_total (0.000 GB),mem_free (702712 KB),<br><FONT COLOR=GREEN>OK</FONT>: bytes_in (0.00 bytes/sec),bytes_out (0.00 bytes/sec),cpu_nice (0.0 %),cpu_user (0.4 %),cpu_wio (0.0 %),load_fifteen (2.32 ),load_five (2.51 ),load_one (2.78 ),mem_buffers (0 KB),mem_cached (0 KB),mem_shared (0 KB),part_max_used (0.0 ),pkts_in (0.00 packets/sec),pkts_out (0.00 packets/sec),proc_run (1 ),proc_total (57 ),swap_free (2655840 KB),
	    if ($line =~ /\[(\d+)\] EXTERNAL COMMAND: PROCESS_SERVICE_CHECK_RESULT;(.*?);$ganglia_svc_name;\d;Cluster (.*?), Host (.*?), IP (.*?),/o) {
		my $timestamp = $1;
		my $host      = $2;
		my $ip        = $5;
		## FIX LATER:  what is the purpose of the LASTUPDATE assignment here?
		$ganglia_host_ref->{NAME}->{$host}->{LASTUPDATE} = $timestamp;
		$ganglia_host_ref->{NAME}->{$host}->{IP} = $ip;
		$ganglia_host_ref->{NAME}->{$host}->{IN_GANGLIA} = 1;	# This host is in Ganglia.
		if ($monarch_host_ref->{NAME}->{$host}) {
		    # FIX LATER:  what is the purpose of the LASTUPDATE assignment here?
		    $monarch_host_ref->{NAME}->{$host}->{LASTUPDATE} = $timestamp;
		    $ganglia_host_ref->{NAME}->{$host}->{IN_MONARCH} = 1;
		}
	    }
	}
	close NAGIOS;
    }
    elsif ($get_ganglia_host_from_ganglia_db) {
	$ganglia_host_ref = get_ganglia_hosts();
	$ganglia_host_count = scalar keys %{$ganglia_host_ref->{NAME}};
    }
    else {
	%{$ganglia_host_ref->{NAME}} = ();
    }
}

if ($process_cacti_hosts) {
    $cacti_host_ref = get_cacti_hosts();
    $cacti_host_count = scalar keys %{$cacti_host_ref->{NAME}};
}
else {
    %{$cacti_host_ref->{NAME}} = ();
}

if ($process_ganglia_hosts && $DEBUG_INFO) {
    print LOG "Ganglia Hosts:\n";
    foreach my $host (sort keys %{$ganglia_host_ref->{NAME}}) {
	print LOG "\tHost=$host\n" if ($ganglia_host_ref->{NAME}->{$host}->{IN_GANGLIA});
    }
}

if ($process_cacti_hosts) {
    print LOG "Cacti Hosts:\n" if $DEBUG_INFO;
    my $heading_printed = $DEBUG_INFO;
    foreach my $host (sort keys %{$cacti_host_ref->{NAME}}) {
	print LOG "\tHost=$host. IP address=".$cacti_host_ref->{NAME}->{$host}->{IP}."\n" if $DEBUG_INFO;
	$ganglia_host_ref->{NAME}->{$host}->{IN_CACTI} = 1;	# This host is in Cacti.
	if ($monarch_host_ref->{NAME}->{$host}) {
	    $ganglia_host_ref->{NAME}->{$host}->{IN_MONARCH} = 1;
	}
	if ($ganglia_host_ref->{NAME}->{$host}->{IP}) {
	    if ($ganglia_host_ref->{NAME}->{$host}->{IP} ne $cacti_host_ref->{NAME}->{$host}->{IP}) {
		if (! $heading_printed) {
		    print LOG "Cacti Hosts:\n";
		    $heading_printed = 1;
		}
		print LOG "\tCacti host $host IP address ".$cacti_host_ref->{NAME}->{$host}->{IP}." does not match Nagios IP address ".
		  $ganglia_host_ref->{NAME}->{$host}->{IP}.". Keeping Nagios IP address.\n" if $DEBUG_WARNING;
	    }
	}
	else {
	    $ganglia_host_ref->{NAME}->{$host}->{IP} = $cacti_host_ref->{NAME}->{$host}->{IP};
	}
    }
}

foreach my $host (sort keys %{$monarch_host_ref->{NAME}}) {
    if ($process_ganglia_hosts and !$ganglia_host_ref->{NAME}->{$host}->{IN_GANGLIA}) {
	print LOG "Monarch host $host is not referenced in Ganglia.\n" if $DEBUG_NOTICE;
    }
    if ($process_cacti_hosts and !$ganglia_host_ref->{NAME}->{$host}->{IN_CACTI}) {
	print LOG "Monarch host $host is not referenced in Cacti.\n" if $DEBUG_NOTICE;
    }
}

# Grab all the Monarch hostgroups in bulk, so we have an efficient means to map
# from a hostgroup name to a hostgroup id when we call apply_host_profile().
print LOG "Getting all existing Monarch hostgroups.\n" if $DEBUG_NOTICE;
my %where_hash = ();
my %hostgroups_table = StorProc->fetch_list_hash_array('hostgroups',\%where_hash);
foreach my $hg_id (sort keys %hostgroups_table) {
    $monarch_hostgroups->{NAME}->{$hostgroups_table{$hg_id}[1]}->{ID} = $hg_id;	# Instantiate each monarch hostgroup name and ID
    print LOG "\tHost group \"$hostgroups_table{$hg_id}[1]\" is in Monarch.\n" if $DEBUG_INFO;
}

$initial_analysis_time = Time::HiRes::time() - $script_start_time;

$middle_analysis_start_time = Time::HiRes::time();

# Insert new hosts.
foreach my $host (sort keys %{$ganglia_host_ref->{NAME}}) {
    if ($process_ganglia_hosts) {
	if ($ganglia_host_ref->{NAME}->{$host}->{IN_GANGLIA} and !$ganglia_host_ref->{NAME}->{$host}->{IN_MONARCH}) {
	    print LOG "Ganglia host $host is not referenced in Monarch DB.\n" if $DEBUG_NOTICE;
	    if ($ganglia_host_ref->{NAME}->{$host}->{IN_GANGLIA}) {
		my $host_profile_id = $assign_host_profiles_by_ganglia_clusters ? ganglia_cluster_host_profile_id($host) : $default_host_profile_ganglia_id;
		print LOG "\tAdding host $host to Monarch.  Ganglia host=$host, ip=$ganglia_host_ref->{NAME}->{$host}->{IP}, profileid=$host_profile_id.\n" if $DEBUG_NOTICE;
		# @values fields:
		# host_id, name, alias, address, os, hosttemplate_id, hostextinfo_id, hostprofile_id, host_escalation_id, service_escalation_id, status, comment, notes
		my $default_id = $is_postgresql ? \undef : '';
		my @values = ($default_id,$host,$host,$ganglia_host_ref->{NAME}->{$host}->{IP},'','','',$host_profile_id,'','','','','');
		my $id = StorProc->insert_obj_id('hosts',\@values,'host_id');
		my @errors = ();
		if ($id =~ /error/i) {
		    push @errors, $id;
		}
		else {
		    $monarch_host_ref->{NAME}->{$host}->{ID} = $id;
		    my @hosts = ($id);
		    my $errors = apply_host_profile($host,\@hosts,$host_profile_id);
		    if (@$errors) {
			push @errors, @$errors;
		    }
		    else {
			$inserthostcount++;
		    }
		}
		if (@errors) {
		    print LOG "\tErrors:\n";
		    foreach my $error (@errors) {
			print LOG "\t\t", $error;
		    }
		}
	    }
	}
	elsif ($ganglia_host_ref->{NAME}->{$host}->{IN_MONARCH}) {
	    # If this is in Monarch, check to see if the IP address changed.
	    if ($ganglia_host_ref->{NAME}->{$host}->{IP} ne $monarch_host_ref->{NAME}->{$host}->{IP}) {
		# Update Monarch IP address with the nagios/ganglia IP address
		my %values = ('address' => $ganglia_host_ref->{NAME}->{$host}->{IP});
		my $result = StorProc->update_obj('hosts','name',$host,\%values);
		if ($result =~ /error/i) {
		    print LOG "Errors: $result\n";
		}
	    }
	    # Add apply ganglia service profile to each ganglia host found but not currently assigned.
	    # FIX MINOR:  Why isn't this done on first add of a new host?
	    if (!$monarch_host_ref->{NAME}->{$host}->{ASSIGNED_GANGLIA_SERVICE_PROFILE} and $ganglia_host_ref->{NAME}->{$host}->{IN_GANGLIA}) {
		if ($default_service_profile_ganglia_id) {
		    my @applyservices = ($default_service_profile_ganglia_id);
		    my @applyhosts = ($monarch_host_ref->{NAME}->{$host}->{ID});
		    my $errors = apply_service_profile(\@applyhosts,\@applyservices);
		    if (@$errors) {
			print LOG "\tErrors:\n";
			foreach my $error (@$errors) {
			    print LOG "\t\t", $error;
			}
		    }
		    else {
			print LOG "Applied ganglia service profile \"$default_service_profile_ganglia\" to $host.\n" if $DEBUG_NOTICE;
			$serviceprofilesappliedcount++;
		    }
		    # FIX LATER:  what is the purpose of the LASTUPDATE assignment here?
		    $monarch_host_ref->{NAME}->{$host}->{LASTUPDATE} = 1;
		}
		else {
		    print LOG
			"Error: The default Ganglia service profile ID \"default_service_profile_ganglia_id\"\n",
			"    is not defined, and thus the Ganglia service profile \"$default_service_profile_ganglia\"\n",
			"    cannot be assigned to \"$host\"!  (The easiest way to fix this is to\n",
			"    manually assign and apply this service profile to at least one host, using\n",
			"    Monarch, rather than defining a value in the autoimport.conf file.)\n"
			if $DEBUG_ERROR;
		}
	    }
	}
    }
    if ($process_cacti_hosts) {
	if ($ganglia_host_ref->{NAME}->{$host}->{IN_CACTI} and !$ganglia_host_ref->{NAME}->{$host}->{IN_MONARCH}) {
	    print LOG "Cacti host $host is not referenced in Monarch DB.\n" if $DEBUG_NOTICE;
	    print LOG "\tAdding host $host to Monarch.  Cacti host=$host, ip=$ganglia_host_ref->{NAME}->{$host}->{IP}, profileid=$default_host_profile_cacti_id.\n" if $DEBUG_NOTICE;
	    my $default_host_profile_id = $default_host_profile_cacti_id;
	    my $default_id = $is_postgresql ? \undef : '';
	    my @values = ($default_id,$host,$host,$ganglia_host_ref->{NAME}->{$host}->{IP},'','','',$default_host_profile_id,'','','','','');
	    my $id = StorProc->insert_obj_id('hosts',\@values,'host_id');
	    my @errors = ();
	    if ($id =~ /error/i) {
		push @errors, $id;
	    }
	    else {
		$monarch_host_ref->{NAME}->{$host}->{ID} = $id;
		my @hosts = ($id);
		my $errors = apply_host_profile($host,\@hosts,$default_host_profile_id);
		if (@errors) {
		    push @errors, @$errors;
		}
		else {
		    $inserthostcount++;
		}
	    }
	    if (@errors) {
		print LOG "\tErrors:\n";
		foreach my $error (@errors) {
		    print LOG "\t\t", $error;
		}
	    }
	}
	else {
	    # This host is already in Monarch.
	    # Add and apply cacti service profile to each cacti host found but not currently assigned.
	    # FIX MINOR:  Why isn't this done on first add of a new host?
	    if (!$monarch_host_ref->{NAME}->{$host}->{ASSIGNED_CACTI_SERVICE_PROFILE} and $ganglia_host_ref->{NAME}->{$host}->{IN_CACTI}) {
		if ($default_service_profile_cacti_id) {
		    my @applyservices = ($default_service_profile_cacti_id);
		    my @applyhosts = ($monarch_host_ref->{NAME}->{$host}->{ID});
		    my $errors = apply_service_profile(\@applyhosts,\@applyservices);
		    if (@$errors) {
			print LOG "\tErrors:\n";
			foreach my $error (@$errors) {
			    print LOG "\t\t", $error;
			}
		    }
		    else {
			print LOG "Applied cacti service profile \"$default_service_profile_cacti\" to $host.\n" if $DEBUG_NOTICE;
			$serviceprofilesappliedcount++;
		    }
		    # FIX LATER:  what is the purpose of the LASTUPDATE assignment here?
		    $monarch_host_ref->{NAME}->{$host}->{LASTUPDATE} = 1;
		}
		else {
		    print LOG
			"Error: The default Cacti service profile ID \"default_service_profile_cacti_id\"\n",
			"    is not defined, and thus the Cacti service profile \"$default_service_profile_cacti\"\n",
			"    cannot be assigned to $host!  (The easiest way to fix this is to\n",
			"    manually assign and apply this service profile to at least one host, using\n",
			"    Monarch, rather than defining a value in the autoimport.conf file.)\n"
			if $DEBUG_ERROR;
		}
	    }
	}
    }
}

# Assign new hosts to the proper hostgroups

my $wg_hostgroups_changed = 0;
if ($process_wg_hostgroups) {
    print LOG "Assigning New Hosts to Hostname-Derived Hostgroups.\n";
    #
    # Assign new host to the proper hostgroups
    #
    #	Now get Monarch hostgroup list
    #
    print LOG "Getting Monarch hostgroups.\n" if $DEBUG_NOTICE;
    my $monarch_hg_ref = undef;
    my %wherehash = ();
    my %hgs = StorProc->fetch_list_hash_array('hostgroups',\%wherehash);
    foreach my $hg_id (sort keys %hgs) {
	$monarch_hg_ref->{NAME}->{$hgs{$hg_id}[1]}->{ID} = $hg_id;	# Instantiate each monarch hostgroup name and ID
	print LOG "\tHost group \"$hgs{$hg_id}[1]\" is in Monarch.\n" if $DEBUG_INFO;
	# Get Monarch members
	my %wherehash = ('hostgroup_id' => $hg_id);
	my @hg_members_id = StorProc->fetch_list_where('hostgroup_host','host_id',\%wherehash);
	foreach my $member_id (sort @hg_members_id) {
	    $monarch_hg_ref->{NAME}->{$hgs{$hg_id}[1]}->{HOSTS_ID}->{$member_id} = 1;
	    my %wherehash = ('host_id' => $member_id);
	    my %hostitem = StorProc->fetch_one_where('hosts',\%wherehash);	# Get host name using host id
	    $monarch_hg_ref->{NAME}->{$hgs{$hg_id}[1]}->{HOSTS}->{$hostitem{name}} = 1;
	}
    }
    #
    #	Set hostgroup for each Monarch host which is also a Ganglia or Cacti host
    #
    print LOG "Getting hostgroups derived from the hostname for each host.\n" if $DEBUG_NOTICE;
    my $wg_hg_ref = undef;
    foreach my $host (sort keys %{$monarch_host_ref->{NAME}}) {
	if ($ganglia_host_ref->{NAME}->{$host}->{IN_GANGLIA} or $ganglia_host_ref->{NAME}->{$host}->{IN_CACTI}) {
	    my $hostgroup_name_start_time = Time::HiRes::time();
	    my $hg_name = $use_hostgroup_program ? (`$hostgroup_program $host`) : $custom_hostgroup_package->hostgroup_name($host);
	    $hostgroup_name_time += Time::HiRes::time() - $hostgroup_name_start_time;
	    chomp $hg_name;
	    if ($hg_name !~ /invalid/) {
		$wg_hg_ref->{NAME}->{$hg_name}->{HOSTS}->{$host} = 1;
		print LOG "\tHost group for host $host is \"$hg_name\".\n" if $DEBUG_INFO;
	    }
	    else {
		print LOG "\tInvalid host name $host. Not assigned to any hostgroup.\n" if $DEBUG_ERROR;
	    }
	}
    }

    #
    #	If Hostgroup unchanged, then keep
    #	If Hostgroup changed, then delete
    #	Add new and changed hostgroups
    #
    foreach my $wg_hg (sort keys %{$wg_hg_ref->{NAME}}) {
	# Check to see what derived hostgroups are different.  If so, delete the hostgroup, then mark for add.
	# See if this exists in monarch.
	if ($monarch_hg_ref->{NAME}->{$wg_hg}) {
	    print LOG "Comparing hostgroup \"$wg_hg\" members in Monarch and (Ganglia or Cacti).\n" if $DEBUG_NOTICE;
	    # Compute the difference between monarch member list and derived member list for this host wg_hg.
	    my %count = ();
	    my $updateflag = 0;
	    if ($DEBUG_DEBUG) {
		foreach my $e (keys %{$monarch_hg_ref->{NAME}->{$wg_hg}->{HOSTS}}) {
		    print LOG "\tHost $e is in Monarch hostgroup \"$wg_hg\".\n";
		}
		foreach my $e (keys %{$wg_hg_ref->{NAME}->{$wg_hg}->{HOSTS}}) {
		    print LOG "\tHost $e is in (Ganglia or Cacti) hostgroup \"$wg_hg\".\n";
		}
	    }
	    foreach my $e (keys %{$monarch_hg_ref->{NAME}->{$wg_hg}->{HOSTS}}, keys %{$wg_hg_ref->{NAME}->{$wg_hg}->{HOSTS}}) { $count{$e}++ }
	    foreach my $e (keys %count) {
		# See if this host is not in both lists, i.e., not equal to 2.
		if ($count{$e} != 2) {
		    print LOG "\tHost $e is not in both Monarch and (Ganglia or Cacti).\n" if $DEBUG_INFO;
		    $updateflag = 1;	# Set flag if not
		}
	    }
	    if ($updateflag) {
		$wg_hostgroups_changed = 1;
		print LOG "Host group \"$wg_hg\" derived from hostnames is different from Monarch.  Deleting from Monarch.\n" if $DEBUG_NOTICE;
		# Delete hostgroup-host collection for this hostgroup.
		my $result = StorProc->delete_all('hostgroup_host','hostgroup_id',$monarch_hg_ref->{NAME}->{$wg_hg}->{ID});
		if ($result =~ /error/i) {
		    print LOG "\tErrors: $result\n";
		}
		# Delete hostgroup.
		$result = StorProc->delete_all('hostgroups','hostgroup_id',$monarch_hg_ref->{NAME}->{$wg_hg}->{ID});
		if ($result =~ /error/i) {
		    print LOG "\tErrors: $result\n";
		}
		# Mark the hostgroup for insertion/add.
		delete($monarch_hg_ref->{NAME}->{$wg_hg});
	    }
	}
	# Now check for hosts to insert.
	# See if this derived hostgroup is in both lists.  If not in monarch, add to the monarch db.
	unless ($monarch_hg_ref->{NAME}->{$wg_hg}) {
	    print LOG "Adding derived hostgroup \"$wg_hg\" to Monarch.\n" if $DEBUG_NOTICE;
	    # "hostgroups" table is id, name, alias, hostprofile_id, host_escalation_id, service_escalation_id, status, comment.
	    if (!$wg_hg_ref->{NAME}->{$wg_hg}->{DESCRIPTION}) { $wg_hg_ref->{NAME}->{$wg_hg}->{DESCRIPTION} = $wg_hg; 	}
	    my $default_id = $is_postgresql ? \undef : '';
	    my @values = ($default_id,$wg_hg,$wg_hg_ref->{NAME}->{$wg_hg}->{DESCRIPTION},'','','','','','');
	    my $result = StorProc->insert_obj('hostgroups',\@values);
	    if ($result =~ /error/i) {
		print LOG "\tErrors: $result\n";
		next;
	    }
	    # FIX LATER:  if the insert succeeded, should we modify $monarch_hg_ref to reflect that fact that this entry now exists?

	    my %wherehash = ('name' => $wg_hg);
	    my %hostgroupitem = StorProc->fetch_one_where('hostgroups',\%wherehash);	# Get host name using host id

	    # Now add the members to this hg.
	    foreach my $hg_member (sort keys %{$wg_hg_ref->{NAME}->{$wg_hg}->{HOSTS}}) {
		# Make sure there is a host id for this. If not, don't add to list.
		if (!$monarch_host_ref->{NAME}->{$hg_member}->{ID}) {
		    print LOG "\tHost \"$hg_member\" is not a Monarch host!  Not adding to hostgroup \"$wg_hg\".\n" if $DEBUG_WARNING;
		    next;
		}
		print LOG "\tAdding host \"$hg_member\" to \"$wg_hg\" hostgroup.\n" if $DEBUG_INFO;
		# "hostgroup_host" table is hostgroup_id, host_id.
		my @values = ($hostgroupitem{"hostgroup_id"},$monarch_host_ref->{NAME}->{$hg_member}->{ID});
		my $result = StorProc->insert_obj('hostgroup_host',\@values);
		if ($result =~ /error/i) {
		    print LOG "\tErrors: $result\n";
		    next;
		}
	    }
	    $wg_hostgroups_changed = 1;
	}
    }
}

$middle_analysis_time = Time::HiRes::time() - $middle_analysis_start_time;
$middle_analysis_start_time = 0;

$final_analysis_start_time = Time::HiRes::time();

# Delete unwanted hosts, by just assigning to the $deleted_hostgroup hostgroup.
# Note that we don't actually drop any of its existing hostgroup association(s).

if ($assign_deleted_hosts_to_hostgroup) {
    print LOG "Assigning Deleted Hosts to Deleted Hostgroup.\n";
    my %deletedhg = StorProc->fetch_one('hostgroups','name',$deleted_hostgroup);
    if ($deletedhg{'hostgroup_id'}) {
	# Delete all existing entries in the deleted hostgroup.
	my $result = StorProc->delete_all('hostgroup_host','hostgroup_id',$deletedhg{'hostgroup_id'});

	foreach my $host (sort keys %{$monarch_host_ref->{NAME}}) {
	    print LOG "\tHost $host: process_ganglia_hosts=$process_ganglia_hosts, IN_GANGLIA=".
		(defined($ganglia_host_ref->{NAME}->{$host}->{IN_GANGLIA}) ? $ganglia_host_ref->{NAME}->{$host}->{IN_GANGLIA} : "0")."; ".
		"process_cacti_hosts=$process_cacti_hosts, IN_CACTI=".
		(defined($ganglia_host_ref->{NAME}->{$host}->{IN_CACTI}) ? $ganglia_host_ref->{NAME}->{$host}->{IN_CACTI} : "0")."\n"
		if $DEBUG_INFO;
	    if (
		($process_ganglia_hosts  and !$ganglia_host_ref->{NAME}->{$host}->{IN_GANGLIA}
		and $process_cacti_hosts and !$ganglia_host_ref->{NAME}->{$host}->{IN_CACTI})
		    or
		($process_ganglia_hosts  and !$ganglia_host_ref->{NAME}->{$host}->{IN_GANGLIA}
		and !$process_cacti_hosts)
		    or
		(!$process_ganglia_hosts
		and $process_cacti_hosts and !$ganglia_host_ref->{NAME}->{$host}->{IN_CACTI})
		) {
		my %w = ('name' => $host);
		my %h = StorProc->fetch_one_where('hosts',\%w);
		if (($h{'hostprofile_id'} != $default_host_profile_ganglia_id) and
		    ($h{'hostprofile_id'} != $default_host_profile_cacti_id)   and
		    (! defined ($ganglia_cluster_by_host_profile_id{$h{'hostprofile_id'}}))) {
		    # If host profile isn't from ganglia or cacti, then don't mark for delete.
		    print LOG "\tSkipping assignment of host $host to hostgroup \"$deleted_hostgroup\".\n".
			"\t    Its host profile (".
			    (defined ($hostprofile_name_by_id{$h{'hostprofile_id'}}) ? ('"'.$hostprofile_name_by_id{$h{'hostprofile_id'}}.'"') : "[unknown name]").
			    ", id=".$h{'hostprofile_id'}.") is not set to the default Ganglia or Cacti host profile,\n".
			"\t    and does not match any of the known Ganglia clusters.\n" if $DEBUG_WARNING;
		    next;
		}
		print LOG "\tMonarch host $host, host profile \"".$h{'hostprofile_id'}."\" is not referenced in ".
		    (($process_ganglia_hosts && $process_cacti_hosts) ? "Ganglia or Cacti" : $process_ganglia_hosts ? "Ganglia" : "Cacti")
		    .".\n\t    Assigning host $host to hostgroup $deleted_hostgroup in Monarch.\n" if $DEBUG_WARNING;
		my @vals = ($deletedhg{'hostgroup_id'},$monarch_host_ref->{NAME}->{$host}->{ID});
		my $result = StorProc->insert_obj('hostgroup_host',\@vals);
		if ($result =~ /error/i) {
		    print LOG "\tErrors: $result\n";
		}
		$deletehostcount++;
	    }
	}
    }
    else {
	print LOG "\tERROR: Deleted Hostgroup \"$deleted_hostgroup\" not found.\nRemoved hosts not assigned to any hostgroup.\n";
    }
}

#
# Assign new Ganglia hosts to the new Ganglia hostgroup (new_ganglia_hosts_hostgroup from the config file).
# Assign new Cacti hosts to the new Cacti hostgroup (new_cacti_hosts_hostgroup from the config file).
#
if ($assign_new_hosts_to_hostgroup) {
    print LOG "Assigning New Hosts to Fixed Hostgroups.\n";
    my $newgangliacount = 0;
    my %newgangliahg = StorProc->fetch_one('hostgroups','name',$new_ganglia_hosts_hostgroup);
    if ($newgangliahg{'hostgroup_id'} and $process_ganglia_hosts) {
	# Delete all existing entries in the hostgroup.
	my $result = StorProc->delete_all('hostgroup_host','hostgroup_id',$newgangliahg{'hostgroup_id'});

	foreach my $host (sort keys %{$ganglia_host_ref->{NAME}}) {
	    if ($ganglia_host_ref->{NAME}->{$host}->{IN_GANGLIA}) {
		if (!$monarch_host_ref->{NAME}->{$host}->{ID}) {
		    my %w = ('name' => $host);
		    my %h = StorProc->fetch_one_where('hosts',\%w);
		    print LOG "\tNew Ganglia host $host, assigning to new Ganglia hostgroup \"$new_ganglia_hosts_hostgroup\".\n" if $DEBUG_NOTICE;
		    my @vals = ($newgangliahg{'hostgroup_id'},$h{host_id});
		    my $result = StorProc->insert_obj('hostgroup_host',\@vals);
		    if ($result =~ /error/i) {
			print LOG "\t\tErrors: $result\n";
		    }
		    $newgangliacount++;
		}
		else {
		    # Already in monarch.  Check to see if it is in a hostgroup.
		    my %w = ('host_id' => $monarch_host_ref->{NAME}->{$host}->{ID});
		    my %h = StorProc->fetch_one_where('hostgroup_host',\%w);
		    if (!$h{'hostgroup_id'}) {
			print LOG "\tExisting Ganglia host $host is not in any hostgroup, assigning to new Ganglia hostgroup \"$new_ganglia_hosts_hostgroup\".\n" if $DEBUG_WARNING;
			my @vals = ($newgangliahg{'hostgroup_id'},$monarch_host_ref->{NAME}->{$host}->{ID});
			my $result = StorProc->insert_obj('hostgroup_host',\@vals);
			if ($result =~ /error/i) {
			    print LOG "\t\tErrors: $result\n";
			}
			$newgangliacount++;
		    }
		    else {
			print LOG "\tGanglia host $host in hostgroup id $h{'hostgroup_id'}\n" if $DEBUG_NOTICE;
		    }
		}
	    }
	}
    }
    else {
	print LOG "\tERROR: New Ganglia Hostgroup \"$new_ganglia_hosts_hostgroup\" not found.\n\tNew Ganglia hosts not assigned to any hostgroup.\n";
    }
    print LOG "Hosts in New Ganglia Hostgroup \"$new_ganglia_hosts_hostgroup\" = $newgangliacount\n";
    #
    # Assign new Cacti hosts to the new Cacti hostgroup
    #
    my $newcacticount = 0;
    my %newcactihg = StorProc->fetch_one('hostgroups','name',$new_cacti_hosts_hostgroup);
    if ($newcactihg{'hostgroup_id'} and $process_cacti_hosts) {
	# Delete all existing entries in the hostgroup.
	my $result = StorProc->delete_all('hostgroup_host','hostgroup_id',$newcactihg{'hostgroup_id'});

	foreach my $host (sort keys %{$ganglia_host_ref->{NAME}}) {
	    if ($ganglia_host_ref->{NAME}->{$host}->{IN_CACTI}) {
		if (!$monarch_host_ref->{NAME}->{$host}->{ID}) {
		    my %w = ('name' => $host);
		    my %h = StorProc->fetch_one_where('hosts',\%w);
		    print LOG "\tCacti host $host, assigning to new Cacti hostgroup \"$new_cacti_hosts_hostgroup\".\n" if $DEBUG_NOTICE;
		    my @vals = ($newcactihg{'hostgroup_id'},$h{host_id});
		    my $result = StorProc->insert_obj('hostgroup_host',\@vals);
		    if ($result =~ /error/i) {
			print LOG "\t\tErrors: $result\n";
		    }
		    $newcacticount++;
		}
		else {
		    # Already in monarch.  Check to see if it is in a hostgroup.
		    my %w = ('host_id' => $monarch_host_ref->{NAME}->{$host}->{ID});
		    my %h = StorProc->fetch_one_where('hostgroup_host',\%w);
		    if (!$h{'hostgroup_id'}) {
			print LOG "\tExisting Cacti host $host is not in any hostgroup, assigning to new Cacti hostgroup \"$new_cacti_hosts_hostgroup\".\n" if $DEBUG_WARNING;
			my @vals = ($newcactihg{'hostgroup_id'},$monarch_host_ref->{NAME}->{$host}->{ID});
			my $result = StorProc->insert_obj('hostgroup_host',\@vals);
			if ($result =~ /error/i) {
			    print LOG "\t\tErrors: $result\n";
			}
			$newcacticount++;
		    }
		    else {
			print LOG "\tCacti host $host in hostgroup id $h{'hostgroup_id'}\n" if $DEBUG_NOTICE;
		    }
		}
	    }
	}
    }
    else {
	print LOG "\tERROR: New Cacti Hostgroup \"$new_cacti_hosts_hostgroup\" not found.\n\tNew Cacti hosts not assigned to any hostgroup.\n";
    }
    print LOG "\tHosts in New Cacti Hostgroup \"$new_cacti_hosts_hostgroup\" = $newcacticount.\n";
}

$final_analysis_time = Time::HiRes::time() - $final_analysis_start_time;
$final_analysis_start_time = 0;

$modification_time = Time::HiRes::time() - $script_start_time;

my $outcome = 1;

# Implement changes (propagate to Nagios) if new hosts found/deleted.
if ($inserthostcount or $deletehostcount or $wg_hostgroups_changed or $serviceprofilesappliedcount) {
    print LOG "\nImplementing changes.  New hosts=$inserthostcount.  Deleted hosts=$deletehostcount.  Service profiles applied=$serviceprofilesappliedcount.\n";
    get_configs();
    if ($commit_changes) {
	## Commit the new configuration to Nagios.  This internally includes a pre-flight,
	## so we won't accidentally commit a bad configuration.
	$backup_start_time = Time::HiRes::time();
	if (commit("backup")) {
	    $backup_time = Time::HiRes::time() - $backup_start_time;
	    $backup_start_time = 0;
	    $commit_start_time = Time::HiRes::time();
	    $outcome = commit("commit");
	    $commit_time = Time::HiRes::time() - $commit_start_time;
	    $commit_start_time = 0;

	    # In the GW6.4 release, the internals of the commit operation above inappropriately assume
	    # that no database connection is already active, so the Audit->foundation_sync() routine
	    # takes its own action to connect and disconnect, thus destroying any connection which was
	    # already open before the commit.  We recover from that here so we may continue to access the
	    # database for after-the-fact statistical-summary purposes.  We do so in a manner that is
	    # robust against some future version of the base product that will properly leave an already
	    # open connection open after the commit operation (dbdisconnect() is safely idempotent in
	    # the present release).  For now, we tolerate any inefficiency that may result from this
	    # construction, and will address it in a future release once the base product is improved.
	    StorProc->dbdisconnect();
	    StorProc->dbconnect();
	}
	else {
	    $outcome = 0;
	}
    }
    else {
	## Don't commit the new configuration; just run a pre-flight to see if the configuration is okay.
	$preflight_start_time = Time::HiRes::time();
	if (pre_flight_test()) {
	    print LOG "\nAutomatic changes passed preflight test.\n";
	}
	else {
	    die "\nERROR: Automatic changes failed preflight test.  Manually correct errors before retrying.\n";
	}
	$preflight_time = Time::HiRes::time() - $preflight_start_time;
	$preflight_start_time = 0;
    }
}
else {
    print LOG "No new or deleted hosts found.  No change to Nagios configuration required.\n";
}

$total_script_time = Time::HiRes::time() - $script_start_time;
print LOG ($outcome ? "Finished" : "Failure"), " after " . sprintf("%0.3f", $total_script_time) . " seconds total run time.\n";
print "Autoconfiguration ".($outcome ? "completed successfully" : "failed (see the log file)").".  New hosts=$inserthostcount.  Deleted hosts=$deletehostcount.\n";
exit ($outcome ? 0 : 1);
