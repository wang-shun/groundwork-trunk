#!/usr/local/groundwork/perl/bin/perl -w --

# Copyright (c) 2010 GroundWork Open Source, Inc. ("GroundWork").  All rights
# reserved.  Use is subject to GroundWork commercial license terms.
#
# cloud_config.pl
#
# Script that should be run regularly via cron as user nagios to update GroundWork
# Monitor 6.3 Enterprise or later with Eucalyptus cloud instances (which we will call
# "regions", to match the corresponding EC2 abstraction) and availability zones.
#
# This script relies on the EC2 API tools being accessible, and on files containing
# Eucalyptus environment variables to be prepared before running.  (The data in the
# environment variables must be either switched between accessing different regions,
# or provided instead on the EC2 API command lines.)
#
# Change log:
#
# 2010-03-16	v0.1	Initial version.
# 2010-03-17	v0.2	Tracking of inactive hosts.
# 2010-03-24	v0.3	Source format cleanup and minor editing.
# 2010-03-24	v0.4	Moved local config data into an external config file.
# 2010-04-07	v0.5	Added support for multiple regions.
# 2010-04-21	v0.6	Extended virtual host alias content to include region.
#			Also fixed hostgroup deletion to instead just cull members.
# 2010-07-02	v1.0	Added support for a synchronized commit operation.
#			Added proper logging.
#			Added proper timeouts for EC2 commands.
#			Added orphaned-host deletion.
# 2010-11-08	v2.0	Added full support for orphaned_hosts_disposition and
#			orphaned_host_retention_period options.
#			Improved efficiency of database accesses.
#			Added support for printing orphaned host data.
#			Tweaked to support EC2 as well, not just Eucalyptus.

# To do:
# (*) Find a way to parallelize execution of the EC2 API tools commands,
#     to dramatically reduce the wall-clock time spent running them.
#     (This will be particulary noticeable in $list_orphans mode.)
# (*) Improve the error checking in this script; for example, check the results
#     returned by various dassmonarch calls.
# (*) Send errors to Foundation, such as issues with objects which are named the
#     same but appear in different clouds, so they become very visible rather than
#     being buried in some log file (if they aren't simply dropped on the floor).

use strict;

my $PROGNAME              = 'cloud_config.pl';
my $VERSION               = '2.0';
my $EC2_API_TOOLS_RELEASE = '{EC2_API_TOOLS_RELEASE}';  # substituted during the build process
my $config_file           = '/usr/local/groundwork/cloud/config/cloud_connector.conf';
my $access_dir            = '/usr/local/groundwork/cloud/credentials';
my $default_ec2_home      = "/usr/local/groundwork/cloud/ec2-api-tools-$EC2_API_TOOLS_RELEASE";
my $script_lock_file      = '/usr/local/groundwork/cloud/var/cloud_config.lock';
my $test_mode             = 0;
my $list_orphans          = 0;
my $max_command_wait_time = 30;  # seconds

my $foundation_msg_count = 0;
my $socket_send_timeout  = 30;   # seconds; to address GWMON-7407; set to 0 to disable
my $remote_host          = 'localhost';
my $remote_port          = 4913;

# Global Debug Level Flag.  Someday, this should be drawn from a config file instead.
# $debug_level = $config->{'debug-level'};
my $debug_level = 6;

# This flag can be used as a debugging aid to dump the configuration as seen inside this script.
# FIX LATER:  In a future release, perhaps allow this flag to be set via a command-line option.
my $debug_config = 0;

my $got_script_lock = 0;
my $script_lock;

my $enable_cloud_processing                   = undef;
my $default_host_profile                      = undef;
my $ec2_availability_zone_host_profile        = undef;
my $eucalyptus_availability_zone_host_profile = undef;
my $orphaned_hosts_disposition                = undef;
my $inactive_hosts_hostgroup                  = undef;
my $orphaned_host_retention_period            = undef;

use lib '/usr/local/groundwork/cloud/perl/lib';

use Getopt::Long;
use Cwd 'realpath';
use File::Basename;
use POSIX qw(strftime :signal_h);
use IO::Socket;
use Time::Local;
use TypedConfig;
use Clouds::Logger;
use CollageQuery;
use MonarchLocks;
use dassmonarch;
use monarchWrapper;

use vars qw($opt_a $opt_c $opt_d $opt_h $opt_i $opt_o $opt_t $opt_v $opt_z);

# The -i flag is partly for future use, when we convert this from running under cron
# into a self-sustaining daemon that wakes up every so often.  It also serves to let
# logging know when it should and should not redirect STDERR to the log file.

sub print_usage {
    print "usage:  $PROGNAME [-d|--debug_level <#>] [-v|--version] [-t|--test] [-h|--help]\n";
    print "            [-c|--config config_file] [-a|--access credentials_base_directory]\n";
    print "            [-i|--interactive] [-o|--output_logs] [-z|--zombies]\n";
    print "where:  <#> is 0 or higher.  Set <#> to 1 or higher for debug messages.\n";
    print "        Test mode is a dry run.  No configuration changes will be made\n";
    print "        in this mode.\n";
    print "        -d debug_level:  from 1 (fatal) to 7 (debug); default 6 (info)\n";
    print "        -c config_file:  specify an alternate config file\n";
    print "             (default is $config_file)\n";
    print "        -a credentials_base_directory:  specify an alternate base directory\n";
    print "             under which region-specific credentials directories will be found\n";
    print "             (default is $access_dir)\n";
    print "        -i:  run interactively, not as a daemon\n";
    print "        -o:  output log messages to standard output, not just to the log file\n";
    print "        -z:  list data on zombies (orphans) instead of committing changes\n";
}

my $status = GetOptions(
    'c=s' => \$opt_c, 'config=s'      => \$opt_c,
    'a=s' => \$opt_a, 'access=s'      => \$opt_a,
    'd=s' => \$opt_d, 'debug_level=s' => \$opt_d,
    'v'   => \$opt_v, 'version'       => \$opt_v,
    't'   => \$opt_t, 'test'          => \$opt_t,
    'i'   => \$opt_i, 'interactive'   => \$opt_i,
    'o'   => \$opt_o, 'output_logs'   => \$opt_o,
    'z'   => \$opt_z, 'zombies'       => \$opt_z,
    'h'   => \$opt_h, 'help'          => \$opt_h
);

if (not $status) {
    print "FATAL:  Cannot understand command-line arguments.\n";
    exit 1;
}

if ($opt_v) {
    print "Version:  $PROGNAME $VERSION\n";
    exit 0;
}

if ($opt_h) {
    print_usage();
    exit 0;
}

$config_file  = $opt_c if $opt_c;
$access_dir   = $opt_a if $opt_a;
$debug_level  = $opt_d if $opt_d;  # only conditional so we can still force the default on during development testing
$test_mode    = $opt_t if $opt_t;  # only conditional so we can still force the default on during development testing
$list_orphans = $opt_z if $opt_z;

our $DEBUG_NONE    = undef;
our $DEBUG_FATAL   = undef;
our $DEBUG_ERROR   = undef;
our $DEBUG_WARNING = undef;
our $DEBUG_NOTICE  = undef;
our $DEBUG_STATS   = undef;
our $DEBUG_INFO    = undef;
our $DEBUG_DEBUG   = undef;

# Variables to be used as quick tests to see if we're interested in particular debug messages.
$DEBUG_NONE    = $debug_level == 0;  # turn off all debug info
$DEBUG_FATAL   = $debug_level >= 1;  # the application is about to die
$DEBUG_ERROR   = $debug_level >= 2;  # the application has found a serious problem, but will attempt to recover
$DEBUG_WARNING = $debug_level >= 3;  # the application has found an anomaly, but will try to handle it
$DEBUG_NOTICE  = $debug_level >= 4;  # the application wants to inform you of a significant event
$DEBUG_STATS   = $debug_level >= 5;  # the application wants to log statistical data for later analysis
$DEBUG_INFO    = $debug_level >= 6;  # the application wants to log a potentially interesting event
$DEBUG_DEBUG   = $debug_level >= 7;  # the application wants to log detailed debugging data

my $logfile                = undef;
my $run_interactively      = 0;
my $reflect_log_to_stdout  = 0;
my $max_logfile_size       = undef;
my $max_logfiles_to_retain = undef;

$run_interactively     = $opt_i;
$reflect_log_to_stdout = $opt_o;

# FIX LATER:  These values ought to be drawn from a Cloud Connector config file, and in a future release, that may be so.
# $logfile                = $config->{'cloud-connector-log-file'};
# $max_logfile_size       = $config->{'max-logfile-size'};
# $max_logfiles_to_retain = $config->{'max-logfiles-to-retain'};
$logfile                = '/usr/local/groundwork/cloud/logs/cloud_connector.log';
$max_logfile_size       = 10;
$max_logfiles_to_retain = 5;

# Convert to rough megabytes.  We use the conventional disk-drive manufacturer's definition of
# a megabyte (10^^6, a round million) rather than the binary definition (2^^20 = 1024 * 1024).
$max_logfile_size *= 1_000_000;

# Convert to a number, just in case we got some bad input.
$max_logfiles_to_retain += 0;

Clouds::Logger->new ($logfile, $run_interactively, $reflect_log_to_stdout, $max_logfile_size, $max_logfiles_to_retain);
open_logfile();
log_timed_message "=== Starting up (process $$). ===";

my $real_config_file = realpath $config_file;
my $real_access_dir  = realpath $access_dir;

if (not defined $real_config_file) {
    log_timed_message "FATAL:  The specified config file \"$config_file\" does not exist." if $DEBUG_FATAL;
    exit 1;
}

if (not defined $real_access_dir) {
    log_timed_message "FATAL:  The specified credentials base directory \"$access_dir\" does not exist." if $DEBUG_FATAL;
    exit 1;
}

$config_file = $real_config_file;
$access_dir  = $real_access_dir;

if ($test_mode) {
    log_timed_message "DEBUG:  test mode is set.  No configuration changes will be made in this mode even if debug output says otherwise." if $DEBUG_DEBUG;
}

$ENV{EC2_HOME} = $default_ec2_home if not defined $ENV{EC2_HOME};
my $EC2_HOME = $ENV{EC2_HOME};
if (not -d $EC2_HOME) {
    log_timed_message "FATAL:  $PROGNAME error:  EC2_HOME \"$EC2_HOME\" is not a directory." if $DEBUG_FATAL;
    exit 1;
}
log_timed_message "DEBUG:  export EC2_HOME=$EC2_HOME" if $DEBUG_DEBUG;

#################################################################
# Setup
#################################################################

use sigtrap qw(die untrapped normal-signals QUIT stack-trace error-signals);

# Don't bother with the script lock if we are only here to list orphans.
# (More generally, later on we make sure that in the $list_orphans mode,
# execution doesn't make any database changes.)
if (not $list_orphans) {
    # Prevent more than one copy of this script from running concurrently.
    my $errors = Locks->open_and_lock( \*script_lock, $script_lock_file, $Locks::EXCLUSIVE, $Locks::NON_BLOCKING );
    if (@$errors) {
	my @errors = ();
	my @blocking_errors = ();
	if (defined fileno \*script_lock) {
	    my ($pid_errors, $pid_blocks, $pids) = Locks->get_blocking_pids( \*script_lock, $script_lock_file, $Locks::EXCLUSIVE );
	    if (@$pid_blocks) {
		push @blocking_errors, 'Another cloud configuration operation is already in progress.';
		push @blocking_errors, 'Underlying detail:' if @$pid_blocks || @$pid_errors;
		push @blocking_errors, @$pid_blocks;
	    }
	    push @blocking_errors, @$pid_errors;
	    Locks->close_and_unlock( \*script_lock );
	}
	push @errors, @blocking_errors;
	push @errors, @$errors if !@blocking_errors;  # excessive detail
	for (@errors) {
	    log_message($_);
	}
	exit 1;
    }
}

# If we get interrupted by a signal right here, the unlink won't be executed
# because the flag won't be set yet.  We will live with that race condition.
$got_script_lock = 1;
END {
    if (not $list_orphans) {
	Locks->unlink_and_close( \*script_lock, $script_lock_file ) if $got_script_lock;
    }
    log_shutdown() if log_is_open();
}

my %clouds = ();
eval {
    my $config = TypedConfig->new( $config_file, $debug_config );
    $enable_cloud_processing                   = $config->get_scalar('enable_cloud_processing');
    $default_host_profile                      = $config->get_scalar('default_host_profile');
    $ec2_availability_zone_host_profile        = $config->get_scalar('ec2_availability_zone_host_profile');
    $eucalyptus_availability_zone_host_profile = $config->get_scalar('eucalyptus_availability_zone_host_profile');
    $orphaned_hosts_disposition                = $config->get_scalar('orphaned_hosts_disposition');
    $inactive_hosts_hostgroup                  = $config->get_scalar('inactive_hosts_hostgroup');
    $orphaned_host_retention_period            = $config->get_number('orphaned_host_retention_period');
    %clouds                                    = $config->get_hash('clouds');
};
if ($@) {
    chomp $@;
    log_timed_message $@;
    log_timed_message "FATAL:  The Cloud Connector cannot function without a useable config file." if $DEBUG_FATAL;
    exit 1;
}

print Data::Dumper->Dump([\%clouds], [qw(\%clouds)]) if $debug_config;
my %regions = $clouds{'region'} ? %{ $clouds{'region'} } : ();
print Data::Dumper->Dump([\%regions], [qw(\%regions)]) if $debug_config;

foreach my $region (keys %regions) {
    print "region = $region\n" if $debug_config;
    my $region_type    = $regions{$region}{type};
    my $region_host    = $regions{$region}{host};
    my $region_enabled = $regions{$region}{enabled};
    # If all of these conditions are not met, the region will be silently ignored.
    if (defined $region_type && defined $region_host && defined $region_enabled) {
	if ($debug_config) {
	    print "type for $region is $region_type\n";
	    print "host for $region is $region_host\n";
	    print "region $region is ", ($region_enabled ? 'enabled' : 'disabled'), "\n";
	}
	if (not $region_enabled) {
	    log_timed_message "INFO:  Region \"$region\" is not enabled." if $DEBUG_INFO;
	    delete $regions{$region};
	}
    }
    else {
	log_timed_message "INFO:  Region \"$region\" is not enabled." if $DEBUG_INFO;
	delete $regions{$region};
    }
}

if (scalar keys %regions == 0) {
    log_timed_message "FATAL:  no regions with type, host, and enabled fields are defined in $config_file" if $DEBUG_FATAL;
    exit 1;
}

if (not $enable_cloud_processing) {
    log_timed_message "NOTICE:  The Cloud Connector is currently disabled." if $DEBUG_NOTICE;
    exit 0;
}

exit 0 if $debug_config;

my $monarchapi = dassmonarch->new();

$monarchapi->set_debuglevel(
    $DEBUG_DEBUG   ? 'verbose' :
    $DEBUG_INFO    ? 'info'    :
    $DEBUG_WARNING ? 'warning' :
    $DEBUG_ERROR   ? 'error'   :
		     'none'
);

#################################################################
# Read data from all the accessible clouds
#################################################################

if (not opendir(DIRECTORY, $access_dir)) {
    log_timed_message "FATAL:  Cannot read $access_dir ($!)" if $DEBUG_FATAL;
    exit 1;
}
my %directories = map { $_ => 1 } grep { -d } map { "$access_dir/$_" } readdir DIRECTORY;
closedir DIRECTORY;

if (not keys %directories) {
    log_timed_message "FATAL:  No region credentials subdirectories are available." if $DEBUG_FATAL;
    exit 1;
}

foreach my $directory (keys %directories) {
    my $dir = basename $directory;
    if (not exists $regions{$dir}) {
	# We silently ignore a directory which exists but is not configured.
	delete $directories{$directory};
    }
}

my $have_orphan_region = 0;
foreach my $region (keys %regions) {
    if (not exists $directories{"$access_dir/$region"}) {
	# This is more serious.  We have a configured region but no credentials for it.  This is grounds for dismissal.
	log_timed_message "FATAL:  No credentials are available for the configured region \"$region\"." if $DEBUG_FATAL;
	$have_orphan_region = 1;
    }
}
if ($have_orphan_region) {
    exit 1;
}

my %az       = ();
my %in       = ();
my %profiles = ();

my %host_profile_ids = monarchWrapper->get_table_objects('profiles_host');

foreach my $directory (keys %directories) {
    my $region = basename $directory;

    # Inhale the new region's configuration.
    my $region_config;
    my $EC2_URL         = undef;
    my $EC2_PRIVATE_KEY = undef;
    my $EC2_CERT        = undef;
    eval {
	$region_config = TypedConfig->new ("$directory/access.conf", $debug_config);
	$EC2_URL         = $region_config->get_scalar ('EC2_URL');
	$EC2_PRIVATE_KEY = $region_config->get_scalar ('EC2_PRIVATE_KEY');
	$EC2_CERT        = $region_config->get_scalar ('EC2_CERT');
    };
    if ($@) {
	chomp $@;
	log_timed_message "FATAL:  region \"$region\" is not properly configured." if $DEBUG_FATAL;
	log_timed_message $@;
	next;
    }

    # Perhaps in the future, we might just skip this region instead.  But we may as well
    # be obnoxious about the failure so it can get noticed and corrected right away.
    # (Actually, the TypedConfig package will probably already complain for us.)
    if (not defined $EC2_URL) {
	log_timed_message "FATAL:  EC2_URL is not defined for region \"$region\"" if $DEBUG_FATAL;
	exit 1;
    }
    if (not defined $EC2_PRIVATE_KEY) {
	log_timed_message "FATAL:  EC2_PRIVATE_KEY is not defined for region \"$region\"" if $DEBUG_FATAL;
	exit 1;
    }
    if (not defined $EC2_CERT) {
	log_timed_message "FATAL:  EC2_CERT is not defined for region \"$region\"" if $DEBUG_FATAL;
	exit 1;
    }

    # Notes on the standard options:
    #
    # (1) The Amazon documentation is not terribly clear on how the --region option defaults.
    #     In fact, if you don't give it, it will extract the part of the EC2_URL that it needs
    #     from either the -U option or the EC2_URL environment variable.  But it doesn't take
    #     the full EC2_URL value straight up; it will extract the part that it needs.  Further,
    #     if you specify the --region option such as --region 172.28.115.66 then it will emit
    #     the following error:
    #         Unknown host: 'https://ec2.172.28.115.66.amazonaws.com'
    #     whereas if you just set the -U option and let --region default, that seems to work
    #     without problem.
    #
    # (2) We use the --show-empty-fields option to protect ourselves against variant formatting
    #     for different lines in the output of the commands.
    #
    my @standard_options = ('-U', $EC2_URL, '-K', $EC2_PRIVATE_KEY, '-C', $EC2_CERT, '--show-empty-fields');

    # Get list of availability zones

    # syntax:  ec2-describe-availability-zones [zone_name...]
    # Errors that are written by the command to STDERR will be captured by our
    # logging mechanism and stuffed into the log file.
    my @azout_command = ($EC2_HOME . '/bin/ec2-describe-availability-zones', @standard_options);
    log_timed_message 'DEBUG:  about to run ec2-describe-availability-zones' if $DEBUG_DEBUG;
    # Note:  If this region is inaccessible, this command will time out and exit internally.
    my $azout = run_timed_command($region, 'ec2-describe-availability-zones', @azout_command);

    foreach my $azline (@$azout) {
	# For a Eucalyptus region:
	#     AVAILABILITYZONE        Kluster 172.28.115.66   (nil)   (nil)
	# For an EC2 region:
	#     AVAILABILITYZONE        us-east-1a      available       us-east-1       (nil)
	#     AVAILABILITYZONE        us-east-1b      available       us-east-1       (nil)
	#     AVAILABILITYZONE        us-east-1c      available       us-east-1       (nil)
	#     AVAILABILITYZONE        us-east-1d      available       us-east-1       (nil)
	if ( $azline =~ /^AVAILABILITYZONE/ ) {
	    my @azparams = split( /\t/, $azline );
	    if (exists $az{$azparams[1]}) {
		log_timed_message
		  "WARNING:  zone \"$azparams[1]\" exists in both region \"$az{$azparams[1]}->{REGION}\" and region \"$region\"; the latter will be ignored."
		  if $DEBUG_WARNING;
	    }
	    else {
		# FIX LATER:  check for "(nil)" in various fields?

		# Note:  These fields may change slightly between EC2 API tools releases.
		# The later fields shown here may not accurately reflect what is being
		# returned by the version of the EC2 API tools release we are using.

		# [0] AVAILABILITYZONE	# AVAILABILITYZONE identifier
		# [1] Kluster		# availability zone name
		# [2] 172.28.115.66	# EC2 returns the state of the zone here ("available"), but Eucalyptus returns an IP address here
		# [3] (nil)		# EC2 returns the Region name here, but Eucalyptus returns (nil)
		# [4] (nil)		# Messages

		# For EC2 zones, $azparams[2] is not an address; for that type of zone, we need to
		# take the region name ($azparams[3], e.g., "us-east-1"), and append a fixed domain name
		# (".ec2.amazonaws.com") to form a fully qualified hostname (in this case, for the region
		# host, not something you might identify as the zone host).  This host will be used to hang
		# service checks for all of the associated zone(s) onto.  We don't perform a DNS lookup to
		# find an actual final address, because the region host may resolve to different IP addresses
		# over time, and we don't want to cause a failure if that happens.  (On the other hand, this
		# method does tie us to DNS working, so a DNS failure could cause apparent host/service check
		# failures.  In the context of accessing an external cloud, that seems reasonable.)
		#
		# For EC2 in particular, we might end up defining multiple availability zones in the same
		# region using the same address for the availability zone host.  This is not believed to
		# cause any difficulties in either Monarch or Foundation.

		$az{$azparams[1]}->{REGION}  = $region;
		$az{$azparams[1]}->{ADDRESS} = $EC2_URL =~ /\.amazonaws\.com$/ ? "$azparams[3].ec2.amazonaws.com" : $azparams[2];
		$az{$azparams[1]}->{PROFILE} = $EC2_URL =~ /\.amazonaws\.com$/ ? $ec2_availability_zone_host_profile : $eucalyptus_availability_zone_host_profile;
	    }
	}
    }

    if ($DEBUG_DEBUG) {
	log_timed_message "DEBUG:  @azout_command\n@$azout";
	# Note:  This listing includes zones from all previously scanned regions,
	# not just the one we just scanned in this iteration.
	foreach my $key ( keys %az ) {
	    log_timed_message "Availability zone = $key";
	    log_timed_message "\tREGION  = " . $az{$key}->{REGION};
	    log_timed_message "\tADDRESS = " . $az{$key}->{ADDRESS};
	    log_timed_message "\tPROFILE = " . $az{$key}->{PROFILE};
	}
    }

    # Get list of images

    # The result of this code is not being used later on, so there is no point in executing it.
    if (0) {
	# syntax:  ec2-describe-images [ami_id ...] [-a] [-o owner ...] [-x user_id]
	# FIX THIS:  We will need to supply appropriate arguments to ec2-describe-images;
	# it's not clear what the default of no specified user_id is supposed to mean;
	# we should probably ask Amazon to clarify that.
	# Errors that are written by the command to STDERR will be captured by our
	# logging mechanism and stuffed into the log file.
	my @imout_command = ($EC2_HOME . '/bin/ec2-describe-images', @standard_options);
	log_timed_message 'DEBUG:  about to run ec2-describe-images' if $DEBUG_DEBUG;
	my $imout = run_timed_command($region, 'ec2-describe-images', @imout_command);

	my %im = ();
	foreach my $imline (@$imout) {
	    # IMAGE   emi-E20410A8    centos53/centos.5-3.x86.img.manifest.xml        admin   available       public  (nil)   x86_64  machine eki-A34013D6   eri-E33D14C7     (nil)   instance-store
	    # IMAGE   eri-E33D14C7    centos53initrd/initrd.img-2.6.24-19-xen.manifest.xml    admin   available       public  (nil)   x86_64  ramdisk (nil)  (nil)    (nil)   instance-store
	    # IMAGE   eki-A34013D6    centos53kernel/vmlinuz-2.6.24-19-xen.manifest.xml       admin   available       public  (nil)   x86_64  kernel  (nil)  (nil)    (nil)   instance-store
	    if ( $imline =~ /^IMAGE/ ) {
		my @imparams = split( /\t/, $imline );
		if (exists $im{$imparams[1]}) {
		    log_timed_message "WARNING:  image \"$imparams[1]\" exists in both region \"$im{$imparams[1]}->{REGION}\" and region \"$region\"; the latter will be ignored." if $DEBUG_WARNING;
		}
		else {
		    # FIX THIS:  check for "(nil)" in various fields?

		    # Note:  These fields may change slightly between EC2 API tools releases.
		    # The later fields shown here may not accurately reflect what is being
		    # returned by the version of the EC2 API tools release we are using.

		    # [ 0] IMAGE		# IMAGE identifier
		    # [ 1] emi-E20410A8		# image ID
		    # [ 2] centos53/centos.5-3.x86.img.manifest.xml	# manifest location
		    # [ 3] admin		# owner:  ID of the AWS location that registered the image (or "amazon")
		    # [ 4] available		# image state (available, pending, failed)
		    # [ 5] public		# accessibility:  image visibility (public or private)
		    # [ 6] (nil)		# product codes, if any, that are attached to the instance
		    # [ 7] x86_64		# image architecture (i386 or x86_64)
		    # [ 8] machine		# image type (machine, kernel, or ramdisk)
		    # [ 9] eki-A34013D6		# ID of the kernel associated with the image (machine images only)
		    # [10] eri-E33D14C7		# ID of the ramdisk associated with the image (machine images only)
		    # [11] instance-store	# type of root device (ebs or instance-store)
		    # [12] ???			# virtualization type (paravirtual or hvm)
		    # [13] ???			# BLOCKDEVICEMAPPING identifier for AMIs that use one or more Amazon EBS volumes
		    # [14] ???			# any tags assigned to the image

		    $im{$imparams[1]}->{REGION}   = $region;
		    $im{$imparams[1]}->{MANIFEST} = $imparams[2];
		}
	    }
	}

	if ($DEBUG_DEBUG) {
	    log_timed_message "DEBUG:  @imout_command\n@$imout";
	    # Note:  This listing includes images from all previously scanned regions,
	    # not just the one we just scanned in this iteration.
	    foreach my $key ( keys %im ) {
		log_timed_message "Image = $key";
		log_timed_message "\tREGION   = " . $im{$key}->{REGION};
		log_timed_message "\tMANIFEST = " . $im{$key}->{MANIFEST};
	    }
	}
    }

    # Get list of instances

    # syntax:  ec2-describe-instances [instance_id ...]
    # Errors that are written by the command to STDERR will be captured by our
    # logging mechanism and stuffed into the log file.
    my @inout_command = ($EC2_HOME . '/bin/ec2-describe-instances', @standard_options);
    log_timed_message 'DEBUG:  about to run ec2-describe-instances' if $DEBUG_DEBUG;
    my $inout = run_timed_command($region, 'ec2-describe-instances', @inout_command);

    foreach my $inline (@$inout) {
	# RESERVATION     r-40E80856      admin   default
	# INSTANCE        i-36DA06EC      emi-F8A2191B    172.28.115.247  172.28.115.247  running (nil)   0       (nil)   m1.small        2010-04-05T23:56:31+0000        EucalyptusCluster       eki-87A21373    eri-C6491458    (nil)   monitoring-false        (nil)   (nil)   (nil)   (nil)   (nil)  (nil)    (nil)
	if ( $inline =~ /^INSTANCE/ ) {
	    my @inparams = split( /\t/, $inline );
	    my $instance = $inparams[1];
	    if (exists $in{$instance}) {
		log_timed_message "WARNING:  instance \"$inparams[1]\" exists in both region \"$in{$instance}->{REGION}\" and region \"$region\"; the latter will be ignored." if $DEBUG_WARNING;
	    }
	    else {
		# FIX LATER:  check for "(nil)" in various fields?

		# Note:  These fields may change slightly between EC2 API tools releases.
		# The later fields shown here may not accurately reflect what is being
		# returned by the version of the EC2 API tools release we are using.

		# [ 0] INSTANCE			# output type identifier
		# [ 1] i-36DA06EC		# instance ID
		# [ 2] emi-F8A2191B		# owner:  AMI ID of the image on which the instance is based
		# [ 3] 172.28.115.247		# public DNS name (only present for running instances)
		# [ 4] 172.28.115.247		# private DNS name (only present for running instances)
		# [ 5] running			# instance state (pending, running, shutting-down, terminated, stopping, stopped)
		# [ 6] (nil)			# key name, if a key was associated with the instance at launch
		# [ 7] 0			# AMI launch index
		# [ 8] (nil)			# product codes attached to the instance
		# [ 9] m1.small			# instance type
		# [10] 2010-04-05T23:56:31+0000	# instance launch time
		# [11] EucalyptusCluster	# availability zone
		# [12] eki-87A21373		# kernel ID
		# [13] eri-C6491458		# ramdisk ID
		# [14] (nil)			# monitoring state  [some confusion here with next column]
		# [15] monitoring-false		# public IP address [some confusion here with previous column]
		# [16] (nil)			# private IP address
		# [17] (nil)			# subnet ID (if the instance is running in a VPC)
		# [18] (nil)			# VPC ID (if the instance is running in a VPC)
		# [19] (nil)			# type of root device (ebs or instance-store)
		# [20] (nil)			# placement group the cluster compute instance is in
		# [21] (nil)			# virtualization type (paravirtual or hvm)
		# [22...] (nil)			# any tags assigned to the instance
		# [23...] (nil)			# BLOCKDEVICE identifier for each Amazon EBS volume the instance is using,
		#				# along with the device name, the volume ID, and the timestamp

		my $data = \%{ $in{$instance} };
		$data->{REGION}  = $region;
		$data->{AMI}     = $inparams[2];
		$data->{PUBDNS}  = $inparams[3];
		$data->{PRIVDNS} = $inparams[4];
		$data->{STATE}   = $inparams[5];
		$data->{TYPE}    = $inparams[9];
		$data->{AZ}      = $inparams[11];
		my $preferred_profile = "cloud-machine-$inparams[2]";
		$profiles{$preferred_profile} = 1;
		if (exists $host_profile_ids{$preferred_profile}) {
		    $data->{PROFILE} = $preferred_profile;
		}
		else {
		    log_timed_message "DEBUG:  host profile = \"", $preferred_profile , "\" does not exist.  Using default." if $DEBUG_DEBUG;
		    $data->{PROFILE} = $default_host_profile;
		}
	    }
	}
    }

    if ($DEBUG_DEBUG) {
	log_timed_message "DEBUG:  @inout_command\n@$inout";
	# Note:  This listing includes instances from all previously scanned regions,
	# not just the one we just scanned in this iteration.
	foreach my $instance ( keys %in ) {
	    log_timed_message "Instance = $instance";
	    log_timed_message "\tREGION  = " . $in{$instance}->{REGION};
	    log_timed_message "\tAMI     = " . $in{$instance}->{AMI};
	    log_timed_message "\tPUBDNS  = " . $in{$instance}->{PUBDNS};
	    log_timed_message "\tPRIVDNS = " . $in{$instance}->{PRIVDNS};
	    log_timed_message "\tSTATE   = " . $in{$instance}->{STATE};
	    log_timed_message "\tTYPE    = " . $in{$instance}->{TYPE};
	    log_timed_message "\tAZ      = " . $in{$instance}->{AZ};
	    log_timed_message "\tPROFILE = " . $in{$instance}->{PROFILE};
	}
    }
}

#################################################################
# Read data from Monarch and make updates as necessary
#################################################################

log_timed_message "DEBUG:  reading and updating Monarch ..." if $DEBUG_DEBUG;

my %host_id = monarchWrapper->get_hosts();
my %hostgroups = monarchWrapper->get_table_objects('hostgroups');
my @inactivehosts = ();

if (not $list_orphans) {
    # create inactive hostgroup if necessary
    if ($orphaned_hosts_disposition eq 'move') {
	if ($inactive_hosts_hostgroup eq '') {
	    log_timed_message "FATAL:  The configured inactive_hosts_hostgroup is blank." if $DEBUG_FATAL;
	    exit 1;
	}
	if (exists $hostgroups{$inactive_hosts_hostgroup}) {
	    log_timed_message "DEBUG:  inactive hostgroup \"$inactive_hosts_hostgroup\" already exists." if $DEBUG_DEBUG;

	    # get members of inactive hostgroup for adding back if they are NOT returned in the list of instances
	    @inactivehosts = $monarchapi->get_hosts_in_hostgroup($inactive_hosts_hostgroup);
	    if ( !$test_mode ) {
		my $result = $monarchapi->delete_hostgroup_members($inactive_hosts_hostgroup);
	    }
	}
	else {
	    log_timed_message "DEBUG:  inactive hostgroup \"$inactive_hosts_hostgroup\" does not exist." if $DEBUG_DEBUG;
	    if ( !$test_mode ) {
		if ( $monarchapi->create_hostgroup( $inactive_hosts_hostgroup, $inactive_hosts_hostgroup ) ) {
		    $hostgroups{$inactive_hosts_hostgroup} = -1;
		}
	    }
	}
    }

    # Add nonexistent instances back to inactive hostgroup if they were already defined as hosts.

    if (@inactivehosts) {
	log_timed_message "DEBUG:  processing inactive hosts from Monarch." if $DEBUG_DEBUG;
    }
    else {
	log_timed_message "DEBUG:  no inactive hosts in Monarch." if $DEBUG_DEBUG;
    }

    foreach my $instance (@inactivehosts) {
	if ( !$test_mode && !$in{$instance} ) {
	    my $result = $monarchapi->assign_hostgroup( $instance, $inactive_hosts_hostgroup );
	}
    }

    # Go through the list of availability zones and see if they exist as
    # either host groups or virtual hosts.  If not, then create them.

    log_timed_message "DEBUG:  checking for availability zones in Monarch." if $DEBUG_DEBUG;

    foreach my $azitem ( keys %az ) {

	# create host group if necessary
	if (exists $hostgroups{$azitem}) {
	    log_timed_message "DEBUG:  availability zone \"$azitem\" already exists as a host group." if $DEBUG_DEBUG;
	    if ( !$test_mode ) {
		my $result = $monarchapi->delete_hostgroup_members($azitem);
	    }
	}
	else {
	    log_timed_message "DEBUG:  availability zone \"$azitem\" does not exist as a host group." if $DEBUG_DEBUG;
	    if ( !$test_mode ) {
		if ( $monarchapi->create_hostgroup( $azitem, $azitem ) ) {
		    $hostgroups{$azitem} = -1;
		}
	    }
	}

	# create virtual host - call import_host_api with update flag set
	if (exists $host_id{$azitem}) {
	    log_timed_message "DEBUG:  availability zone \"$azitem\" already exists as a host." if $DEBUG_DEBUG;
	}
	else {
	    log_timed_message "DEBUG:  availability zone \"$azitem\" does not exist as a host." if $DEBUG_DEBUG;
	}
	if ( !$test_mode ) {
	    # We update the availability-zone pseudohost even if it already exists,
	    # mainly (I suppose) to ensure that all the settings are still correct.
	    # We use the host alias to store both region and availability zone, for
	    # the use of downstream processing.
	    if ( not $monarchapi->import_host_api( $azitem, "$az{$azitem}->{REGION}/$azitem",
		$az{$azitem}->{ADDRESS}, $az{$azitem}->{PROFILE}, 1 ) ) {
		log_timed_message "ERROR:  Cannot create host \"$azitem\".";
	    }
	}

	# assign virtual host to host group
	my $result = $monarchapi->assign_hostgroup( $azitem, $azitem );
    }

    # Go through the list of preferred host profiles for instances, and verify they exist.
    # If they do, get the host profile description and use it as the name of a host group.
    # We need to do this before looping through defined instances because we want to flush
    # out the host group by deleting and recreating it.
    #
    # KNOWN ISSUE:  We are getting the list of preferred host profiles from instance
    # image data that comes from the EC2 API.  If an instance doesn't appear in that
    # list or it changes its image, we may end up leaving stale host groups in Monarch.

    log_timed_message "DEBUG:  processing host profiles to check for host groups." if $DEBUG_DEBUG;

    foreach my $profile (keys %profiles) {
	my %where = ( 'name' => $profile );
	my %results = monarchWrapper->fetch_one_where( 'profiles_host', \%where );

	# There may be no such host profile (e.g., "cloud-machine-emi-E20410A8"),
	# so we need to check explicitly for that condition to avoid warnings.
	if (%results) {
	    my $profiledesc = $results{'description'};
	    if (defined($profiledesc) && $profiledesc ne '') {
		log_timed_message "DEBUG:  processing host profile = $profile." if $DEBUG_DEBUG;
		if (exists $hostgroups{$profiledesc}) {
		    if ( !$test_mode ) {
			my $result = $monarchapi->delete_hostgroup_members($profiledesc);
		    }
		}
		else {
		    if ( !$test_mode ) {
			if ( $monarchapi->create_hostgroup( $profiledesc, $profiledesc ) ) {
			    $hostgroups{$profiledesc} = -1;
			}
		    }
		}
	    }
	}
    }
}

# Prepare certain database info we will rely on in other routines.
my ($detach_time, $display_detach_time) = get_deactivation_times();

my %hosts_in_hostgroup = ();
foreach my $hostgroup (keys %hostgroups) {
    %{ $hosts_in_hostgroup{$hostgroup} } = map {$_ => 1} $monarchapi->get_hosts_in_hostgroup($hostgroup);
}
my %hostgroups_for_host = ();
foreach my $hostgroup (keys %hosts_in_hostgroup) {
    foreach my $host (keys %{ $hosts_in_hostgroup{$hostgroup} }) {
	push @{ $hostgroups_for_host{$host} }, $hostgroup;
    }
}

# Cloud instances for EC2 have hostnames of the form /^i-[0-9a-f]{8}$/, such as:
#
#     i-54f14a39
#     i-90b10afd
#
# while cloud instances for Eucalyptus have hostnames of the form /^i-[0-9A-F]{8}$/, such as:
#
#     i-3C73080E
#     i-4BDE08B3
#
# All hostnames of these forms will be recognized as originating in some cloud, and treated as such.
my @cloud_instances = grep { /^i-[0-9A-Fa-f]{8}$/ } keys %host_id;
log_timed_message "DEBUG:  cloud instances:  ", join(' ', @cloud_instances) if $DEBUG_DEBUG;

if ($list_orphans) {
    my $host_data = monarchWrapper->fetch_fields('hosts', 'name', 'address', 'hostprofile_id');
    my %host_profile_names = monarchWrapper->get_table_objects('profiles_host', 1);

    # We prefix the useful data with a tag that we can use to filter the output from this script,
    # since other messages might also appear both before and after these messages.
    log_message "orphan: Region Image Instance Address Hostgroups Profile Status Time";
    foreach my $instance (@cloud_instances) {
	if (exists $display_detach_time->{$instance}) {
	    my $region     = $in{$instance}->{REGION};
	    my $image      = $in{$instance}->{AMI};
	    my $address    = $in{$instance}->{PUBDNS};
	    my $hostgroups = exists($hostgroups_for_host{$instance}) ? join(', ', @{ $hostgroups_for_host{$instance} }) : undef;
	    my $profile    = $in{$instance}->{PROFILE};
	    my $status     = $in{$instance}->{STATE};
	    my $time       = $display_detach_time->{$instance};
	    $address    = $host_data->{$instance}{address} if ( !defined($address) || $address eq '(nil)' );
	    $profile    = $host_profile_names{ $host_data->{$instance}{hostprofile_id} } if not defined $profile;
	    $region     = '' if not defined $region;
	    $image      = '' if not defined $image;
	    $address    = '' if not defined $address;
	    $hostgroups = '' if not defined $hostgroups;
	    $profile    = '' if not defined $profile;
	    $status     = '' if not defined $status;
	    $time       = '' if not defined $time;  # should never happen
	    log_message "orphan#$region#$image#$instance#$address#$hostgroups#$profile#$status#$time#";
	}
    }
    exit 0;
}

# Go through the list of instances and see if they exist.  If not, create them
# and assign them to the relevant host groups.  Also set the Nagios parent to
# the availability zone virtual host.

# If we find a hostname that looks like cloud instance, but no cloud claims it
# as its own, then we should treat the host as having been abandoned by some
# server bounce or other malfunction, and deactivate monitoring of that host.
# Note that this means we must process hostnames that do NOT appear in any
# output from probing clouds, in addition to processing cloud-probe data.
my @orphaned_cloud_hosts = grep { not exists $in{$_} } @cloud_instances;
log_timed_message "DEBUG:  orphaned cloud hosts:  ", join(' ', @orphaned_cloud_hosts) if $DEBUG_DEBUG;

# FIX MAJOR:  There is some danger of hostname collisions with regard to hosts
# from different clouds.  This situation is as yet unresolved in our code.  The
# possibility of collision is somewhat reduced by EC2 using lowercase alphabetics
# for hex characters while Eucalyptus uses uppercase, but of course that simply
# improves the statistics while making no absolute guarantees between separate
# EC2 regions or separate Eucalyptus regions, and without affecting the chances
# of collision for all-numeric hostname suffixes.

my @active_hosts      = ();
my @deleted_hosts     = ();
my @deactivated_hosts = ();
foreach my $instance ( @orphaned_cloud_hosts, keys %in ) {
    log_timed_message "DEBUG:  processing instance = $instance." if $DEBUG_DEBUG;
    if (not exists $host_id{$instance}) {
	log_timed_message "DEBUG:  instance = \"$instance\" does not exist as a host." if $DEBUG_DEBUG;
	# A "running" host with an address of '(nil)' (EC2) or '0.0.0.0' (Eucalyptus) is still coming up, and
	# is not yet ready for monitoring (since we don't know how to probe it until it has a valid IP address).
	if ( !exists($in{$instance}) ||
	    $in{$instance}->{STATE}  !~ /running/ ||
	    $in{$instance}->{PUBDNS} eq '(nil)'   ||
	    $in{$instance}->{PUBDNS} eq '0.0.0.0' ) {
	    log_timed_message "DEBUG:  instance = $instance is NOT running." if $DEBUG_DEBUG;
	    next;
	}
    }
    if ( exists $in{$instance} && $in{$instance}->{STATE} =~ /running/ ) {
	log_timed_message "DEBUG:  instance = $instance is running." if $DEBUG_DEBUG;
	if ( !$test_mode ) {
	    my $result = $monarchapi->import_host_api( $instance, $instance, $in{$instance}->{PUBDNS}, $in{$instance}->{PROFILE}, 1 );
	    $result = $monarchapi->assign_hostgroup( $instance, $in{$instance}->{AZ} );

	    # Add in here the assignment to the relevant host profile host group.
	    # Will need to look up for the existence of a host group again based on profile name.
	    my %where = ( 'name' => $in{$instance}->{PROFILE} );
	    my %results = monarchWrapper->fetch_one_where( 'profiles_host', \%where );
	    if (%results) {
		my $profiledesc = $results{'description'};
		if (exists $hostgroups{$profiledesc}) {
		    my $result = $monarchapi->assign_hostgroup( $instance, $profiledesc );
		}
	    }
	    my @parents;
	    push @parents, $in{$instance}->{AZ};
	    $result = $monarchapi->set_parents( $instance, \@parents );
	    push @active_hosts, $instance;
	}
    }
    else {
	log_timed_message "DEBUG:  instance = $instance is NOT running." if $DEBUG_DEBUG;
	if ( !$test_mode ) {
	    if ($orphaned_hosts_disposition eq 'delete') {
		push @deleted_hosts, $instance;
	    }
	    elsif ($orphaned_hosts_disposition eq 'move') {
		push @deactivated_hosts, $instance;
		# delete the instance from its former hostgroup(s)
		if (exists $hostgroups_for_host{$instance}) {
		    foreach my $hostgroup (@{ $hostgroups_for_host{$instance} }) {
			unless ($hostgroup eq $inactive_hosts_hostgroup) {
			    my $result = $monarchapi->dismiss_hostgroup( $instance, $hostgroup );
			    delete $hosts_in_hostgroup{$hostgroup}{$instance};
			}
		    }
		    delete $hostgroups_for_host{$instance};
		}
		if (not exists $hosts_in_hostgroup{$inactive_hosts_hostgroup}{$instance}) {
		    log_timed_message "DEBUG:  adding $instance to the \"$inactive_hosts_hostgroup\" hostgroup" if $DEBUG_DEBUG;
		    if ( $monarchapi->assign_hostgroup( $instance, $inactive_hosts_hostgroup ) ) {
			$hosts_in_hostgroup{$inactive_hosts_hostgroup}{$instance} = 1;
		    }
		}
	    }
	    elsif ($orphaned_hosts_disposition eq 'keep') {
		push @deactivated_hosts, $instance;
		# Other than that, there is nothing to do here; the instance stays in the hostgroup
		# it belonged to while it was still alive.
	    }
	}
    }
}

# We defer this database activity until now so we can take advantage
# of available optimization for doing these operations in bulk.

delete_instances(\@deleted_hosts);
reactivate_instances(\@active_hosts);
deactivate_instances(\@deactivated_hosts);
delete_old_deactivated_instances(\@deactivated_hosts);

# Perform a commit operation.

if ( !$test_mode ) {
    log_timed_message "NOTICE:  performing commit (this will not be interruptible)." if $DEBUG_NOTICE;

    # FIX MINOR:  The way we set the filter parameter here is somewhat clumsy and not
    # reliably indicative of running in an HTML context.  It suffices for distinguishing
    # between ordinary daemon or cron context and the way we run this script from the UI,
    # but could be incorrect for other interactive situations.
    my $result = $monarchapi->filteredGenerateAndCommit( ($run_interactively && $reflect_log_to_stdout) ? 'html' : '' );
}

log_rotation();

exit 0;

#################################################################
# Supporting subroutines
#################################################################

sub log_rotation {
    if (!rotate_logfile()) {
	my $message  = "FATAL:  Problem with rotating the logfile; Cloud Connector (process $$) will stop." if $DEBUG_FATAL;
	log_timed_message $message;
	exit 1;
    }
}

sub catch_signal {
    my $signame = shift;

    die "Caught a SIG$signame signal after $max_command_wait_time seconds of wait time!\n";
}

# FIX LATER:  Figure out some way to invert this routine so that multiple sub-processes
# can be started in parallel (to overlap whatever computation and i/o is possible), and
# their results read serially (to simplify the logic).  Perhaps there should be a start
# routine to begin the subprocess and return a handle to it, a read routine to capture
# output from the handle (with timeout), and a cleanup routine to destroy the handle
# along with any other resources.

# We need to time out EC2 commands because they will run for about 12m42s before finally
# timing out on their own, say if they are unable to connnect to the target host.
sub run_timed_command {
    my $region     = shift;
    my $program    = shift;
    my @command    = @_;
    my @output     = ();
    my $time_left  = alarm(0);
    my $start_time = time();
    my $child_pid  = undef;

    local $SIG{ALRM} = \&catch_signal;

    # We block SIGCHLD for the duration of this routine, using sigprocmask, to guarantee
    # that we get a consistent view of the child process until we can call segpgid() on
    # the child process (the process ID of the child process won't be able to be reassigned
    # during this period, since the zombie process won't be reaped during this period).
    # Note that the implementation here presumes we are running a single-threaded process,
    # so sigprocmask() can be called.  Otherwise, only pthread_sigmask() would necessarily
    # be valid, but then calling it for a single thread probably would not be sufficient
    # to provide the protection we're looking for.
    my $oldblockset = POSIX::SigSet->new;
    my $newblockset = POSIX::SigSet->new(SIGCHLD);
    sigprocmask(SIG_BLOCK, $newblockset, $oldblockset) or die "FATAL:  Could not block SIGCHLD ($!),";

    eval {
	alarm($max_command_wait_time);
	## We might die here either explicitly or because of a timeout and the signal
	## handler action.  If we get the alarm signal and die because of it, we need
	## not worry about resetting the alarm before exiting the eval, because it has
	## already expired.
	eval {
	    $child_pid = open COMMAND, '-|';
	    if (not defined $child_pid) {
		log_timed_message "FATAL:  for region \"$region\", cannot fork() for $program ($!)" if $DEBUG_FATAL;
		exit 1;
	    }

	    if ($child_pid) {
		# This setpgid() may fail with EACCES, but that's okay, as it's probably because
		# the child did the same thing first.  See APITUE2/e, page 270 for the rationale.
		#
		# Two things are confusing about POSIX::setpgid().  First, upon success, it
		# will return the string '0 but true' as its result, so this call behaves more
		# like a standard Perl routine (returns a true value upon success) rather than
		# as a C call (returns a 0 [i.e., false] upon success, and -1 [i.e., true] upon
		# failure).  Of course, if you capture the '0 but true' return value and try to
		# use it in a numeric context, it will behave as though it were 0, which can be
		# rather confusing and contradictory to what you would naively expect considering
		# that the value is true.)  Second, as with any system call, POSIX::setpgid()
		# does not clear $! before running, so you cannot depend on the value of $! if
		# you do not first check for an error return from the call and you have not
		# explicitly cleared $! yourself before the call.
		#
		# A POSIX::EACCES failure is okay (the child did the dirty work first).
		if (! POSIX::setpgid($child_pid, 0) && $! != POSIX::EACCES) {
		    log_timed_message "WARNING:  for region \"$region\", setpgid() in parent of $program process $child_pid failed ($!)"
		      if $DEBUG_WARNING;
		}
		@output = <COMMAND>;
		chomp @output;
	    }
	    else {
		# In child process.  Run the command here.
		if (not POSIX::setpgid(0, 0)) {
		    log_timed_message "WARNING:  for region \"$region\", setpgid() in child $program process $$ failed ($!)"
		      if $DEBUG_WARNING;
		}
		# Redirect error messages so we can see them and comment on them.
		open STDERR, '>>&', 'STDOUT';
		exec { $command[0] } @command
		  or log_timed_message "FATAL:  for region \"$region\", child process cannot exec($program): $!";
		exit 1;
	    }
	};
	alarm(0);
	if ($@) {
	    chomp $@;
	    log_timed_message "FATAL:  while running $program for region \"$region\": ", $@ if $DEBUG_FATAL;
	    # Kill all descendants.
	    my $count = kill 'TERM', 0 - $child_pid;
	    exit 1;
	}
    };
    if ($@) {
	chomp $@;
	log_timed_message "FATAL:  while running $program for region \"$region\": ", $@ if $DEBUG_FATAL;
	# Kill all descendants.
	my $count = kill 'TERM', 0 - $child_pid;
	exit 1;
    }
    if ($child_pid) {
	if (not close(COMMAND)) {
	    if ($!) {
		log_timed_message "FATAL:  for region \"$region\", problem creating a pipe to execute $program ($!)" if $DEBUG_FATAL;
		exit 1;
	    }
	    else {
		# Problem was with the executed program, not the pipe.  Details logged below.
	    }
	}
    }
    elsif (defined fileno COMMAND) {
	close(COMMAND);
    }
    my $child_error = $?;
    if ($child_error || $DEBUG_DEBUG) {
	# Log all output.  If we had a child error, it probably contains some STDERR messages we captured
	# instead of logging them directly.  If we are simply debugging, this provides visibility even if
	# we did not have a child error, for output that would normally have been logged except that we
	# captured the STDERR output instead so we could check it for particular error message content.
	log_timed_message "DEBUG:  Output for $program on region \"$region\":";
	log_message $_ foreach (@output);
	if ($output[0] eq 'Client.InvalidSecurity: Request has expired') {
	    log_timed_message "DEBUG:  Perhaps your monitoring server time is not synchronized with Internet time.";
	}
    }
    if ($child_error) {
	if ($DEBUG_FATAL) {
	    log_timed_message "FATAL:  for region \"$region\", problem executing $program (child wait status $child_error)";
	}
	if ($DEBUG_DEBUG) {
	    log_message "Complete failed command is:";
	    log_message "", join(' ', @command);
	}
	exit 1;
    }

    # Restore the old signal mask so we can reap zombies once again.
    sigprocmask(SIG_SETMASK, $oldblockset) or die "FATAL:  Could not restore SIGCHLD signal ($!),";

    my $end_time = time();
    if ($time_left) {
	my $time_til_alarm = $time_left - ( $end_time - $start_time );
	alarm( $time_til_alarm > 0 ? $time_til_alarm : 1 );
    }

    return \@output;
}

sub delete_instances {
    my $instances_to_delete = shift;
    foreach my $instancename (@$instances_to_delete) {
	# Delete this instance from Monarch (only).  A subsequent
	# Commit will reflect the changes in Foundation as well.
	# I hate the inefficiency of the internals of this routine,
	# but this action won't happen very often, so addressing that
	# will have to wait until we revise the entire Monarch API.
	if ( !$monarchapi->delete_host($instancename) ) {
	    log_timed_message "ERROR:  instance $instancename could not be deleted!";
	}
    }
}

# Convert the strict format "YYYY-MM-DD hh:mm:ss" (expressed in the local timezone) to UNIX time.
sub epoch_time {
    my $string_time = shift;
    my $unix_time = undef;
    if ($string_time =~ /^(\d{4})-(\d{2})-(\d{2}) (\d{2}):(\d{2}):(\d{2})$/) {
	my ($year, $month, $day, $hour, $minute, $second) = ($1, $2, $3, $4, $5, $6);
	$year -= 1900;
	$month -= 1;
	$unix_time = timelocal($second, $minute, $hour, $day, $month, $year);
    }
    return $unix_time;
}

# Find hosts with a DeactivationTime already defined, and their respective DeactivationTime values.
# Ideally, we would do this by probing Foundation instead of GWCollageDB,
# but I don't (yet) know of any web service that would return this data.
sub get_deactivation_times {
    my %deact_times         = ();
    my %display_deact_times = ();
    my $foundation = CollageQuery->new();
    my $sqlstmt =
	"select h.HostName, hsp.ValueDate from GWCollageDB.Host h, GWCollageDB.HostStatusProperty hsp, GWCollageDB.PropertyType pt " .
	"where pt.Name = 'DeactivationTime' and hsp.PropertyTypeID = pt.PropertyTypeID and h.HostID = HostStatusID";
    # Yes, this use of the CollageQuery database handle is cheating, by sidestepping the expected conventions.
    # In a future release, we may migrate this routine into CollageQuery itself, to avoid that problem.
    my $sth = $foundation->{dbh}->prepare($sqlstmt);
    $sth->execute;
    while ( my @values = $sth->fetchrow_array() ) {
	# DeactivationTime is returned as a string of the format "2010-10-27 17:48:29",
	# which we recode here to standard UNIX time for easy timestamp comparison.
	#
	# If a DeactivationTime is cleared, in GW6.3 it will remain in the table as a NULL
	# rather than disappearing entirely.  We ignore such NULL values (which show up as
	# undef values here, when retrieved from the database), to make such hosts look as
	# though they never had a DeactivationTime assigned to begin with.
	$deact_times{ $values[0] } = epoch_time( $display_deact_times{ $values[0] } = $values[1] ) if defined $values[1];
    }
    $sth->finish;
    return \%deact_times, \%display_deact_times;
}

# To be able to depend on DeactivationTime values, we must ensure that we never run
# into a situation where a given instance name was used and then discarded, creating
# a DeactivationTime, and then re-used.  The latter action would cause it to be seen
# as both inactive and active.  To address this, we clear possibly-nonexistent
# DeactivationTime values for all (newly) active hosts.

sub reactivate_instances {
    my $instances_to_activate = shift;
    if (@$instances_to_activate) {
	my @activations = ();
	foreach my $activated_host (@$instances_to_activate) {
	    # We don't want to create a NULL DeactivationTime time if we can avoid it, so we
	    # first need to find out if the instance already has a DeactivationTime assigned.
	    if (exists $detach_time->{$activated_host}) {
		# An empty string for the DeactivationTime would leave the date in the
		# database unchanged, which is not what we need.
		# A single space clears an existing date field to NULL, while (in GW6.3,
		# at least) leaving the row still existing rather than deleting it.
		push @activations, "<Host Host='$activated_host' DeactivationTime=' ' />";
	    }
	}

	#   To set a value for a specific Host, send the following XML to port 4913:
	#   <Adapter Session='1' AdapterType='SystemAdmin'>
	#        <Command Action='MODIFY' ApplicationType='NAGIOS'>
	#            <Host Host='localhost' DeactivationTime=' ' />
	#        </Command>
	#   </Adapter>

	if (@activations) {
	    ## FIX LATER:  Do this for array slices of perhaps 100 or 200 hosts at a time.
	    write_command_xml( 'MODIFY', join("\n", @activations) );
	}
    }
}

sub deactivate_instances {
    my $instances_to_deactivate = shift;
    if (@$instances_to_deactivate) {
	my @deactivations = ();
	# DeactivationTime='2010-04-16 11:30:20'
	my $deactivation_time = strftime("%F %T", localtime);
	my $now = time();
	foreach my $deactivated_host (@$instances_to_deactivate) {
	    # We don't want to update a previous DeactivationTime with the current time, so we
	    # first need to find out if the instance already has a DeactivationTime assigned.
	    if (not exists $detach_time->{$deactivated_host}) {
		push @deactivations, "<Host Host='$deactivated_host' DeactivationTime='$deactivation_time' />";
		# Recording the deactivation time is somewhat optional, as it is unlikely that
		# this host would be a candidate for orphaned-host deletion in the same cycle.
		$detach_time->{$deactivated_host} = $now;
	    }
	}

	#   To set a value for a specific Host, send the following XML to port 4913:
	#   <Adapter Session='1' AdapterType='SystemAdmin'>
	#        <Command Action='MODIFY' ApplicationType='NAGIOS'>
	#            <Host Host='localhost' DeactivationTime='2010-04-16 11:30:20' />
	#        </Command>
	#   </Adapter>

	if (@deactivations) {
	    ## FIX LATER:  Do this for array slices of perhaps 100 or 200 hosts at a time.
	    write_command_xml( 'MODIFY', join("\n", @deactivations) );
	}
    }
}

sub delete_old_deactivated_instances {
    my $orphaned_hosts = shift;

    # Delete all instances where the DeactivationTime property is sufficiently old
    # (older than the configured $orphaned_host_retention_period).

    my $now = time();
    my @old_orphaned_hosts = ();
    foreach my $host (@$orphaned_hosts) {
	if ( exists( $detach_time->{$host} ) && ($now - $detach_time->{$host}) > $orphaned_host_retention_period ) {
	    # The subsequent Commit operation will remove the host from Foundation.
	    push @old_orphaned_hosts, $host;
	}
    }
    delete_instances(\@old_orphaned_hosts);
}

# FIX MINOR:  What to do if we cannot send the data?  Perhaps don't move or keep,
# so we catch the same hosts on the next invocation?
sub write_command_xml {
    my $action     = shift;
    my $xml_string = shift;
    if ( $xml_string eq '' ) {    # Nothing to do ...
	return;
    }
    my $socket = undef;

    # Open connection to Foundation (or not).
    my $max_connect_attempts = 3;
    for ( my $i = 0 ; $i <= $max_connect_attempts ; $i++ ) {
	if ( $i == $max_connect_attempts ) {
	    log_timed_message "ERROR:  Could not connect to $remote_host:$remote_port : $@ ($!)" if $DEBUG_ERROR;
	    return;               # no listener socket available, so skip feeding this round
	}
	else {
	    $socket = IO::Socket::INET->new( PeerAddr => $remote_host, PeerPort => $remote_port, Proto => 'tcp', Type => SOCK_STREAM );
	    if ($socket) {
		log_message 'DEBUG:  Opened socket to Foundation.' if $DEBUG_DEBUG;
		$socket->autoflush();
		last if $socket->sockopt(SO_SNDTIMEO, pack('L!L!', $socket_send_timeout, 0));
		log_timed_message 'ERROR:  Could not set send timeout on socket to Foundation.' if $DEBUG_ERROR;
		close($socket);
	    }
	    sleep 1;
	}
    }
    $foundation_msg_count++;
    my $xml_out =
qq(<Adapter Session="$foundation_msg_count" AdapterType="SystemAdmin">
<Command Action='$action' ApplicationType='NAGIOS'>
$xml_string
</Command>
</Adapter>
);
    print $socket $xml_out;
    log_message   $xml_out if $DEBUG_DEBUG;
    print $socket '<SERVICE-MAINTENANCE command="close" />';
    log_message   '<SERVICE-MAINTENANCE command="close" />' if $DEBUG_DEBUG;
    close($socket);
    return;
}

# ================================================================

# FIX MINOR:
# The routines in the rest of this code are modified versions of, or additions to, those
# in the GW6.3 release.  The improvements here should be folded into the standard release.

package dassmonarch;

# filteredGenerateAndCommit() is a variant of the dassmonarch generateAndCommit() routine
# which provides the extra optional capability of filtering (HTMLizing) the output results.

# FIX MINOR:  add the filter parameter to the dassmonarch generateAndCommit() routine

## @method boolean generateAndCommit (void)
# Perform a pre-flight check and do a commit.
# @param filter if true, HTMLicize commit output for improved browser presentation
# @return success true if commit succeeds, false otherwise.
sub filteredGenerateAndCommit {
    my $self   = shift;
    my $filter = shift;

    $self->debug( 'verbose', 'Attempting commit.' );
    my ($errors, $results, $timings) = monarchWrapper->synchronized_commit(
	$self->{'user_acct'},
	$self->{'nagios_ver'},
	$self->{'nagios_etc'},
	$self->{'nagios_bin'},
	$self->{'monarch_home'},
	$filter
    );
    my @errors  = @{$errors};
    my @results = @{$results};

    $self->debug( 'info', "Commit results:\n" . join( "\n", @results ) );
    if (@errors) {
	$self->debug( 'error', "Errors during pre-flight or commit, exiting:\n" . join( "\n", @errors ) );
	return 0;
    }

    return grep ( $self->{'commit_ok_string'}, @results );
}

# FIX MINOR:  this routine should be migrated into the standard dassmonarch package

## @method boolean dismiss_hostgroup (string hostname, string hostgroup)
# Dismiss a host from a hostgroup.
# @param hostname The host to be dismissed
# @param hostgroup Hostgroup name the host shall be removed from
# @return success true if operation is successful, else false
sub dismiss_hostgroup {
    my $self      = shift;
    my $hostname  = $_[0];
    my $hostgroup = $_[1];
    $self->debug( 'verbose', "Dismiss host $hostname from hostgroup $hostgroup" );
    my $hostID = $self->get_hostid($hostname);

    if ($hostID) {
	$self->debug( 'verbose', "Host $hostname found with ID: $hostID" );
	my %group = monarchWrapper->fetch_one( 'hostgroups', 'name', $hostgroup );
	if (%group) {
	    $self->debug( 'verbose', "Hostgroup $hostgroup found with ID: $group{'hostgroup_id'}" );
	    ## Dismiss assignment
	    my %where = ( 'hostgroup_id' => $group{'hostgroup_id'}, 'host_id' => $hostID );
	    my $result = monarchWrapper->delete_one_where( 'hostgroup_host', \%where );
	    if ( $result =~ /Error/ ) {
		$self->debug( 'error', "delete failed: $result" );
		return 0;
	    }
	    else {
		$self->debug( 'verbose', "delete result: $result" );
	    }
	}
	else {
	    $self->debug( 'error', "Hostgroup $hostgroup not found" );
	    return 0;
	}
    }
    else {
	$self->debug( 'error', "Host $hostname not found" );
	return 0;
    }
    return 1;
}

package monarchWrapper;

# FIX MINOR:  this routine should be migrated into the standard monarchWrapper package

sub fetch_fields {
    shift;
    return StorProc->fetch_fields(@_);
}

package main;
