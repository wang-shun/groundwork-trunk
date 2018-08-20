#!/usr/local/groundwork/perl/bin/perl -w --

# Script invoked by REST API to Register Agent by Profile.

# Copyright (c) 2012-2018 GroundWork, Inc. (www.gwos.com).  All rights reserved.
# Use of this software is subject to commercial license terms.

# TO DO:
#
# FIX MINOR
# (*) Check all possible failures to assign a host profile, service profile, hostgroup, or monarch
#     group, and generate externally-visible messages to Foundation in appropriate cases.
#
# FIX LATER
# (*) Update the OS field (and log that action) if the host exists, the OS field was missing,
#     and a non-empty OS is provided to this call.
#
# FIX LATER
# (*) In this first version, we perform trivial validation of an IPv6 address (just the proper
#     character set, for now); work with the NetAddr::IP maintainer to provide a proper and
#     complete validation routine for use in a future version of this script.
#
# Version 1.2.1  DN 2015-05-27	Modified sub host_exists() host existence logic updated.
#				Case : agent registers successfully; operator changes address in monarch;
#				agent has a problem later that causes it to try auto registration again;
#				host_exists now thinks host not found since incoming address != monarch address;
#				host not found in monarch -> host_import_api with updates called, which resets the
#				config on the host (alias, address, services, profile etc) to the host profile
#				given to host_import_api (from the agent originally). See more notes in host_exists().
# Version 1.2.2  GH 2016-08-30	Localize $_ in subroutines, per Perl documentation.
# Version 1.2.3  GH 2018-05-24	Add support for the assign_hostgroups_to_existing_hostgroup_hosts option.

use strict;

use IO::Handle;
use Socket;
use TypedConfig;
use dassmonarch;

use GW::Logger;
use GW::Foundation;

# This is where we'll find the customer-network package (e.g., AutoRegistration.pm), if one is configured.
use lib qw( /usr/local/groundwork/foundation/scripts );

# ================================
# Package Parameters
# ================================

my $PROGNAME = "registerAgentByProfile.pl";
my $VERSION  = "1.2.3";

my $default_config_file = "/usr/local/groundwork/config/register_agent.properties";

# ================================
# Command-Line Parameters
# ================================

# In theory, these parameter settings could be overridden by command-line arguments.
# In practice, we don't currently support any such arguments; this script uses only
# a fixed set of arguments.

my $config_file           = $default_config_file;
my $debug_config          = 0;                      # if set, spill out certain data about config-file processing to STDOUT
my $run_interactively     = 0;
my $reflect_log_to_stdout = 0;

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

my $logfile                                       = undef;
my $max_logfile_size                              = undef;                 # log rotate is handled externally, not here
my $max_logfiles_to_retain                        = undef;                 # log rotate is handled externally, not here
my $hostname_qualification                        = undef;
my %hardcoded_hostnames                           = ();
my $default_host_profile                          = undef;
my $default_hostgroup                             = undef;
my $assign_hostgroups_to_existing_hostgroup_hosts = undef;
my $default_monarch_group                         = undef;
my $assign_monarch_groups_to_existing_group_hosts = undef;
my $customer_network_package                      = 'AutoRegistration';    # may be overridden via the config file
my $compare_to_foundation_hosts                   = undef;
my $match_case_insensitive_foundation_hosts       = undef;
my $force_hostname_case                           = undef;
my $force_domainname_case                         = undef;
my $use_hostname_as_key                           = undef;
my $use_mac_as_key                                = undef;
my $rest_api_requestor                            = undef;
my $ws_client_config_file                         = undef;
my $log4perl_config                               = undef;

# Parameters derived from the config-file parameters.

# These values will be replaced once $debug_level is itself replaced by a value from
# the config file.  But we want these values to be operational even before the config
# file is read, in case we need to debug early operation of this script.
my $debug_minimal = ( $debug_level >= 1 );
my $debug_basic   = ( $debug_level >= 2 );
my $debug_maximal = ( $debug_level >= 3 );

my $have_customer_network_package    = undef;
my $customer_network                 = undef;
my $have_soft_recode                 = undef;
my $have_hard_recode                 = undef;
my $have_hostgroup_determination     = undef;
my $have_monarch_group_determination = undef;

# Locally defined parameters, that might someday move to the config file.
# See the AlertSite integration config file for detail.

my $foundation_host           = "localhost";
my $foundation_port           = 4913;
my $monitor_server_hostname   = "localhost";
my $monitor_server_ip_address = "127.0.0.1";
my $socket_send_timeout       = 60;
my $send_buffer_size          = 0;

# ================================
# Working Variables
# ================================

my $agent_type        = undef;
my $host_name         = undef;
my $submitted_address = undef;
my $host_address      = undef;
my $host_mac          = undef;
my $host_os           = undef;
my $host_profile      = undef;
my $service_profile   = undef;

my @errors  = ();
my $err_ref = undef;

my $monarchapi  = undef;
my $outcome     = undef;
my $found_host  = {};
my $is_new_host = undef;

my $hostprofile_id    = undef;
my $serviceprofile_id = undef;

my $final_hostname = '';

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
    }

    if (@ARGV != 7) {
	print "usage:  registerAgentByProfile.pl agent_type host_name host_address host_mac host_os host_profile service_profile\n";
	print "where:  host_profile and service_profile must both be specified,\n";
	print "        but may be given as empty strings.\n";
	return ERROR_STATUS;
    }

    if ( not read_config_file( $config_file, $debug_config ) ) {
	spill_message "FATAL:  $PROGNAME cannot load configuration from $config_file";
	return ERROR_STATUS;
    }

    # The user may have configured this script to ignore the incoming IP
    # address data and use either the hostname or the MAC address instead
    # for the Nagios address field of a host.  See the config file comments
    # for details on why this might be appropriate (mainly in situations
    # where client-side DNS is not yielding reliable data).
    $agent_type        = $ARGV[0];
    $host_name         = $ARGV[1];
    $submitted_address = $ARGV[2];
    $host_address      = $use_hostname_as_key ? $ARGV[1] : $use_mac_as_key ? $ARGV[3] : $ARGV[2];
    $host_mac          = $ARGV[3];
    $host_os           = $ARGV[4];
    $host_profile      = $ARGV[5];
    $service_profile   = $ARGV[6];

    if ($debug_basic) {
	print "Auto-Registration request parameters:\n";
	print "     agent_type = $agent_type\n";
	print "      host_name = $host_name\n";    # not to be confused with "hostname=$final_hostname\n" as the final output
	print "   host_address = $host_address" . ( $host_address ne $submitted_address ? " (submitted as $submitted_address)" : '' ) . "\n";
	print "       host_mac = $host_mac\n";
	print "        host_os = $host_os\n";
	print "   host_profile = $host_profile\n";
	print "service_profile = $service_profile\n";
    }

    # These messages are just to prove that we have successfully redirected STDERR to STDOUT
    # (and that both streams are captured by the calling REST web service and returned to the
    # caller) with no problems of interleaving between the two data streams.
    if ($debug_maximal) {
	print STDOUT "DEBUG:  This message is on STDOUT.\n";
	print STDERR "DEBUG:  This message is on STDERR.\n";
    }

    # Only validate the host address as an IP address if it is really supposed to be such.
    unless ( $use_hostname_as_key || $use_mac_as_key ) {
	if ( not is_valid_ip_address($host_address) ) {
	    print "ERROR:  While attempting auto-registration of host \"$host_name\" ($host_address):\n";
	    print "  the IP address specified ($host_address) is invalid.\n";
	    return ERROR_STATUS;
	}
    }

    # Stop if this is just a debugging run.
    return STOP_STATUS if $debug_config;

    # We use a message prefix because multiple concurrent copies of this script may be writing to the log file,
    # and we need a means to disambiguate where each message comes from.
    GW::Logger->new( $logfile, $run_interactively, $reflect_log_to_stdout, $max_logfile_size, $max_logfiles_to_retain, "($$)\t" );

    if ( !open_logfile() ) {
	## The routine will print an error message if it fails, so we don't do so ourselves.
	return ERROR_STATUS;
    }

    log_timed_message "=== Starting up (process $$). ===";

    if ( !$enable_processing ) {
	print "FATAL:  Server-side auto-registration processing is not enabled in its config file.\n";
	log_timed_message "FATAL:  Stopping auto-registration (process $$) because processing is not enabled in the config file ($config_file).";
	close_logfile();

	# A STOP_STATUS returned from main() is the logical exit code in this situation, from the
	# standpoint of the server script -- it has finished executing cleanly, according to its
	# configured options.  However, a client would see that (turned into a 0 exit code from the
	# script as a whole) as a successful execution, and look for the returned hostname.  So we
	# reluctantly classify this situation as an error, so the client can more easily determine
	# that something bad happened from its own perspective.
	return ERROR_STATUS;
    }

    # Record this run for audit purposes, regardless of how it turns out.
    log_timed_message "NOTICE:  Received auto-registration request with these host attributes:\n",
      "     agent_type = $agent_type\n",
      "      host_name = $host_name\n",
      "   host_address = $host_address" . ( $host_address ne $submitted_address ? " (submitted as $submitted_address)" : '' ) . "\n",
      "       host_mac = $host_mac\n",
      "        host_os = $host_os\n",
      "   host_profile = $host_profile\n",
      "service_profile = $service_profile";

    eval {
	$monarchapi = dassmonarch->new();
    };
    if ($@) {
	chomp $@;
	print "FATAL:  Cannot establish a connection to the Monarch database.\n";
	log_timed_message "FATAL:  Cannot establish a connection to the Monarch database:\n  $@";
	return ERROR_STATUS;
    }

    if ($debug_maximal) {
	$monarchapi->set_debuglevel('verbose');
    }
    elsif ($debug_minimal) {
	$monarchapi->set_debuglevel('error');
    }
    else {
	$monarchapi->set_debuglevel('none');
    }

    if ($host_profile) {
	$hostprofile_id = $monarchapi->get_hostprofileid($host_profile);
	if (not defined $hostprofile_id) {
	    log_dassmonarch();
	    print "ERROR:  While attempting auto-registration of host \"$host_name\" ($host_address):\n";
	    print "  database access error while looking up host profile \"$host_profile\".\n";
	    log_timed_message "ERROR:  While attempting auto-registration of host \"$host_name\" ($host_address):";
	    log_timed_message "  database access error while looking up host profile \"$host_profile\".";
	    return ERROR_STATUS;
	}
	elsif (not $hostprofile_id) {
	    print "ERROR:  While attempting auto-registration of host \"$host_name\" ($host_address):\n";
	    print "  desired host profile \"$host_profile\" does not exist in Monarch.\n";
	    log_timed_message "ERROR:  While attempting auto-registration of host \"$host_name\" ($host_address):";
	    log_timed_message "  desired host profile \"$host_profile\" does not exist in Monarch.";
	    return ERROR_STATUS;
	}
    }

    if ($service_profile) {
	$serviceprofile_id = $monarchapi->get_serviceprofileid($service_profile);
	if (not defined $serviceprofile_id) {
	    log_dassmonarch();
	    print "ERROR:  While attempting auto-registration of host \"$host_name\" ($host_address):\n";
	    print "  database access error while looking up service profile \"$service_profile\".\n";
	    log_timed_message "ERROR:  While attempting auto-registration of host \"$host_name\" ($host_address):";
	    log_timed_message "  database access error while looking up service profile \"$service_profile\".";
	    return ERROR_STATUS;
	}
	elsif (not $serviceprofile_id) {
	    print "ERROR:  While attempting auto-registration of host \"$host_name\" ($host_address):\n";
	    print "  desired service profile \"$service_profile\" does not exist in Monarch.\n";
	    log_timed_message "ERROR:  While attempting auto-registration of host \"$host_name\" ($host_address):";
	    log_timed_message "  desired service profile \"$service_profile\" does not exist in Monarch.";
	    return ERROR_STATUS;
	}
    }

    # We attempt to look up the host attributes in Monarch before adding the
    # host.  If the lookup succeeds, we assume this is the same host again.
    # If the lookup fails, we assume this is a new host.
    #
    # We allow two passes of host lookup in Monarch.  We allow customizable
    # intervention and host-attribute recoding before performing these
    # lookups, to prevent mistakes and to provide more accurate data to be
    # stored in Monarch.  We name the two recoding passes "soft" and "hard".
    # The hard recoding and subsequent lookup pass is only run if the lookup
    # failed after the soft recoding.  The exact split of responsibilities
    # between the soft and hard recoding routines is up to the implementer,
    # and may depend on the complexity of the local network environment.

    $outcome = 1;

    # We run the soft recoding to normalize the host identification info to some degree,
    # as the server sees it, before performing any comparisons.  The soft recoding has a
    # chance to perform simple substitutions, such as recognizing that the client has sent
    # in obviously bad data like "localhost" as the hostname, or "127.0.0.1" as the IP
    # address, and trying to find better replacement data using the other host attributes.
    # We don't want such obviously bad data from a client to match what might actually be
    # an existing host in Monarch.
    if ($have_soft_recode) {
	( $outcome, $err_ref, $host_name, $host_address, $host_mac, $host_os ) =
	  $customer_network->soft_recode_host_attributes( $agent_type, $host_name, $host_address, $host_mac, $host_os );
	push @errors, @$err_ref;
    }

    if ($outcome > 0) {
	## We try to match what's in Monarch.
	$found_host = host_exists ($agent_type, $host_name, $host_address, $host_mac);
	if (%$found_host) {
	    $is_new_host = 0;
	    $final_hostname = $found_host->{name};
	    if (defined($found_host->{os})) {
		if ($found_host->{os} ne $host_os) {
		    print "WARNING:  Found existing host \"$found_host->{name}\" ($found_host->{address}) with OS \"$found_host->{os}\",\n";
		    print "          while registering $host_name ($host_address) with OS \"$host_os\".\n";
		    print "          Will assume a match anyway, without overwriting the existing value.\n";
		    log_timed_message "WARNING:  Found existing host \"$found_host->{name}\" ($found_host->{address}) with OS \"$found_host->{os}\",";
		    log_timed_message "          while registering $host_name ($host_address) with OS \"$host_os\".";
		    log_timed_message "          Will assume a match anyway, without overwriting the existing value.";
		}
	    }
	    else {
		## FIX LATER:  Should we update the existing value in this case?
		print "WARNING:  Found existing host \"$found_host->{name}\" ($found_host->{address}) with undefined OS,\n";
		print "          while registering $host_name ($host_address) with OS \"$host_os\".\n";
		print "          Will assume a match, without overwriting the existing value.\n";
		log_timed_message "WARNING:  Found existing host \"$found_host->{name}\" ($found_host->{address}) with undefined OS,";
		log_timed_message "          while registering $host_name ($host_address) with OS \"$host_os\".";
		log_timed_message "          Will assume a match, without overwriting the existing value.";
	    }
	}
    }

    if (!%$found_host && $outcome >= 0 && $have_hard_recode) {
	# If we could not find the host in Monarch, we give the recoding a second chance,
	# this time to perform more-aggressive attribute analysis and replacement.
	( $outcome, $err_ref, $host_name, $host_address, $host_mac, $host_os ) =
	  $customer_network->hard_recode_host_attributes( $agent_type, $host_name, $host_address, $host_mac, $host_os );
	push @errors, @$err_ref;

	# If the hard recoding failed, then it doesn't consider the host-attribute data to be trustworthy
	# enough to match against what is in Monarch (or for that matter, to register a new host with).
	if ($outcome > 0) {
	    ## Try to match what's in Monarch, once again.
	    $found_host = host_exists ($agent_type, $host_name, $host_address, $host_mac);
	    if (%$found_host) {
		$is_new_host = 0;
		$final_hostname = $found_host->{name};
		if (defined($found_host->{os})) {
		    if ($found_host->{os} ne $host_os) {
			print "WARNING:  Found existing host \"$found_host->{name}\" ($found_host->{address}) with OS \"$found_host->{os}\",\n";
			print "          while registering $host_name ($host_address) with OS \"$host_os\".\n";
			print "          Will assume a match anyway, without overwriting the existing value.\n";
			log_timed_message "WARNING:  Found existing host \"$found_host->{name}\" ($found_host->{address}) with OS \"$found_host->{os}\",";
			log_timed_message "          while registering $host_name ($host_address) with OS \"$host_os\".";
			log_timed_message "          Will assume a match anyway, without overwriting the existing value.";
		    }
		}
		else {
		    ## FIX LATER:  Should we update the existing value in this case?
		    print "WARNING:  Found existing host \"$found_host->{name}\" ($found_host->{address}) with undefined OS,\n";
		    print "          while registering $host_name ($host_address) with OS \"$host_os\".\n";
		    print "          Will assume a match, without overwriting the existing value.\n";
		    log_timed_message "WARNING:  Found existing host \"$found_host->{name}\" ($found_host->{address}) with undefined OS,";
		    log_timed_message "          while registering $host_name ($host_address) with OS \"$host_os\".";
		    log_timed_message "          Will assume a match, without overwriting the existing value.";
		}
	    }
	}
	elsif ($outcome < 0) {
	    print "ERROR:  Auto-registration request is denied on the basis of bad host attributes.\n";
	    log_errors (\@errors);
	    log_timed_message "FATAL:  Host attributes for host \"$host_name\", address \"$host_address\" are not trustworthy; auto-registration request is being ignored.";
	    return ERROR_STATUS;
	}
    }

    if ($compare_to_foundation_hosts) {
	## If no host was found (in Monarch), do a lookup in Foundation to see if this host is
	## already monitored in other ways.  This is specifically intended to resolve the issue
	## at some customers where CloudHub notices the host first, before auto-registration
	## comes along, and inserts the hostname into Foundation exactly as recorded in
	## VMware (e.g., perhaps as all-uppercase, or perhaps as mixed-case) even though
	## GDMA historically responded to auto-registration with lowercase names.  If we
	## knew the hostname in VMware was uppercase, we could simply rely on fixed settings
	## of our force_hostname_case and force_domainname_case options to establish the
	## desired hostnames.  But it's possible that VMware might have hostnames recorded in
	## mixed-case, and there's no way a fixed option setting that applies to all hosts can
	## handle that situation correctly.
	##
	## We look for a case-insensitive match to hostnames in Foundation.  The present code
	## here depends on using the host's short name (essentially, because that is what we
	## expect to see in Foundation, and for now we take no trouble here to accommodate
	## any difference in reality from that assumption).  (To force the use of the short
	## name, the hostname_qualification option in register_agent.properties must be set to
	## "short".)  You should also use "as-is" as the value of the force_hostname_case and
	## force_domainname_case options.  That is done so that subsequent auto-registration
	## attempts from the same machine may then be able to match whatever mixed-case
	## hostname we stuff into Monarch if we get a match in Foundation here, provided that
	## such subsequent attempts manage to submit exactly the same mixed-case hostname in
	## those attempts.
	##
	## If we match a host in Foundation, we replace $host_name with what is in Foundation,
	## and in that sense declare a successful name "recoding" before we add the
	## supposedly-new host to Monarch.
	##
	## If on a subsequent auto-registration attempt the requesting machine fails to submit
	## exactly the same hostname as we discover here, then it will once again fail to match
	## in Monarch, and we will once again attempt to match that hostname in Foundation.  A
	## match in this case means that we will again "add" the host to Monarch, but the later
	## code that does so is equipped to update the existing host and not generate an error
	## simply because the host already exists.
	if ( !%$found_host && $outcome >= 0 ) {
	    ## The pattern is compiled only once, because every new request will be a separate run of
	    ## this script and we don't want to recompile the pattern every time through the matching loop.
	    ## Also, this construction allows us to easily control whether or not the matching is case-insensitive.
	    my $host_pattern;
	    eval {
		$host_pattern = $match_case_insensitive_foundation_hosts ? qr/^$host_name$/oi : qr/^$host_name$/o;
	    };
	    if ($@) {
		chomp $@;
		print "$@\n";
		log_timed_message $@;
	    }
	    ## Select which code branch to run.
	    elsif (1) {
		my $rest_api;
		eval {
		    require GW::RAPID;

		    # Basic security:  disallow code in the logging config data.
		    Log::Log4perl::Config->allow_code(0);

		    Log::Log4perl::Logger::create_custom_level( "NOTICE", "WARN" );
		    Log::Log4perl::Logger::create_custom_level( "STATS",  "NOTICE" );

		    eval {
			## If the value starts with a leading slash, we interpret it as an absolute path to a file that
			## contains the logging configuration data.  Otherwise, we interpret it as the data itself.
			Log::Log4perl::init( $log4perl_config =~ m{^/} ? $log4perl_config : \$log4perl_config );
		    };
		    if ($@) {
			chomp $@;
			die "ERROR:  Could not initialize Log::Log4perl logging:\n$@\n";
		    }

		    # Initialize the REST API object.
		    my %rest_api_options = (
			logger => Log::Log4perl::get_logger("Automated.Agent.Registration.GW.RAPID"),
			access => $ws_client_config_file
		    );
		    $rest_api = GW::RAPID->new( undef, undef, undef, undef, $rest_api_requestor, \%rest_api_options );
		    if ( not defined $rest_api ) {
			## The GW::RAPID constructor doesn't directly return any information to the caller on the reason for
			## a failure.  But it will already have used the logger handle to write such detail into the logfile.
			die "ERROR:  Could not create a GW::RAPID object.\n";
		    }

		    my %outcome;
		    my %results;

		    my @match_hosts = ();
		    push @match_hosts, $host_name if not $match_case_insensitive_foundation_hosts;

		    # FIX MINOR:  All we need back are hostnames.  As an important optimization, we should have
		    # available a query depth of "trivial" that would just return that amount of information and no
		    # other configuration or monitoring state.  Without that, there will be a lot of wasted effort here
		    # that will be repeated continually for all the auto-registration attempts that will come in.
		    if ( not $rest_api->get_hosts( \@match_hosts, { depth => 'simple' }, \%outcome, \%results ) ) {
			## Failed.
			die "ERROR:  Could not look up hosts in Foundation.\n";
		    }

		    my $matched_in_foundation = 0;
		    foreach my $name ( keys %results ) {
			if ( $name =~ /$host_pattern/ ) {
			    $host_name             = $name;
			    $matched_in_foundation = 1;
			    print "NOTICE:  Found matching host \"$host_name\" in Foundation.\n";
			    log_timed_message "NOTICE:  Found matching host \"$host_name\" in Foundation.";
			    last;
			}
		    }

		    if ( not $matched_in_foundation ) {
			print "NOTICE:  No matching host found in Foundation.\n";
			log_timed_message "NOTICE:  No matching host found in Foundation.";
		    }
		};
		if ($@) {
		    chomp $@;
		    print "$@\n";
		    log_timed_message $@;
		}

		# Destroy the transient REST API session, if any.
		$rest_api = undef;
	    }
	    else {
		require DBI;

		my $dbh;
		eval {
		    require CollageQuery;

		    my ( $f_dbname, $f_dbhost, $f_dbuser, $f_dbpass, $f_dbtype ) = CollageQuery::readGroundworkDBConfig('collage');

		    if ( !defined($f_dbname) or !defined($f_dbhost) or !defined($f_dbuser) or !defined($f_dbpass) ) {
			die "Error:  Cannot read Foundation database parameters.\n";
		    }

		    my $dsn   = "DBI:Pg:dbname=$f_dbname;host=$f_dbhost";
		    my $dbh   = DBI->connect( $dsn, $f_dbuser, $f_dbpass, { 'AutoCommit' => 1, RaiseError => 1 } );
		    my $query = "select hostname from host";
		    $query .= " where hostname = " . $dbh->quote($host_name) if not $match_case_insensitive_foundation_hosts;
		    my $sth      = $dbh->prepare($query);
		    my $executed = $sth->execute();

		    my $matched_in_foundation = 0;
		    while ( my $row = $sth->fetchrow_hashref() ) {
			if ( $$row{hostname} =~ /$host_pattern/ ) {
			    $host_name             = $$row{hostname};
			    $matched_in_foundation = 1;
			    print "NOTICE:  Found matching host \"$host_name\" in Foundation.\n";
			    log_timed_message "NOTICE:  Found matching host \"$host_name\" in Foundation.";
			    last;
			}
		    }
		    $sth->finish();

		    if ( not $matched_in_foundation ) {
			print "NOTICE:  No matching host found in Foundation.\n";
			log_timed_message "NOTICE:  No matching host found in Foundation.";
		    }
		};
		if ($@) {
		    chomp $@;
		    print "$@\n";
		    log_timed_message $@;
		}

		eval {
		    $dbh->disconnect() if defined $dbh;
		};
		if ($@) {
		    chomp $@;
		    print "$@\n";
		    log_timed_message $@;
		}
	    }
	}
    }

    if (!%$found_host && $outcome >= 0) {
	## Add the host to Monarch, applying the host profile as we do so.

	# We have no information from the client about what alias to use,
	# so we make a reasonable default value to fill in that field.
	my $host_alias = $host_name;

	( $outcome, $err_ref, $host_profile ) =
	  $customer_network->host_profile_to_assign( $agent_type, $host_name, $host_address, $host_mac, $host_os, $host_profile, $service_profile );
	push @errors, @$err_ref;
	if (!$outcome or !defined($host_profile) or $host_profile eq '') {
	    print "ERROR:  Auto-registration request is denied because no explicit or defaulted host profile name was provided.\n";
	    log_errors (\@errors);
	    log_timed_message "FATAL:  No host profile is available for host \"$host_name\", address \"$host_address\"; auto-registration request is being ignored.";
	    return ERROR_STATUS;
	}
	else {
	    ## We re-validate the host profile, in case it got changed by the host_profile_to_assign() routine.
	    $hostprofile_id = $monarchapi->get_hostprofileid($host_profile);
	    if (not defined $hostprofile_id) {
		log_errors (\@errors);
		log_dassmonarch();
		print "ERROR:  While attempting auto-registration of host \"$host_name\" ($host_address):\n";
		print "  database access error while looking up host profile \"$host_profile\".\n";
		log_timed_message "ERROR:  While attempting auto-registration of host \"$host_name\" ($host_address):";
		log_timed_message "  database access error while looking up host profile \"$host_profile\".";
		return ERROR_STATUS;
	    }
	    elsif (not $hostprofile_id) {
		log_errors (\@errors);
		print "ERROR:  While attempting auto-registration of host \"$host_name\" ($host_address):\n";
		print "  desired host profile \"$host_profile\" does not exist in Monarch.\n";
		log_timed_message "ERROR:  While attempting auto-registration of host \"$host_name\" ($host_address):";
		log_timed_message "  desired host profile \"$host_profile\" does not exist in Monarch.";
		return ERROR_STATUS;
	    }
	    my $update_existing_host = 1;  # flag named here for clarity
	    if ( !$monarchapi->import_host_api( $host_name, $host_alias, $host_address, $host_profile, $update_existing_host ) ) {
		log_errors (\@errors);
		log_dassmonarch();
		print "ERROR:  Attempted addition of host to Monarch has failed.\n";
		log_timed_message "FATAL:  Addition of host \"$host_name\", address \"$host_address\" to Monarch has failed.";
		return ERROR_STATUS;
	    }
	    else {
		## Verify that the new data is accessible in the database before we declare complete victory,
		## and simultaneously fetch host details for later use.
		my %host = $monarchapi->host_exists($host_name);
		if ( %host and $host{name} eq $host_name and $host{address} eq $host_address ) {
		    $is_new_host = 1;
		    $found_host = \%host;
		    $final_hostname = $found_host->{name};
		    log_timed_message "STATS:  Host \"$host_name\", address \"$host_address\" has been added to Monarch.";
		}
		else {
		    log_errors (\@errors);
		    print "ERROR:  Cannot find host just added to Monarch.\n";
		    log_timed_message "FATAL:  Retrieval of host \"$host_name\", address \"$host_address\" from Monarch has failed.";
		    return ERROR_STATUS;
		}
	    }
	}
    }

    if (!%$found_host) {
	my $message = "NOTICE:  Host \"$host_name\", address \"$host_address\" was neither found nor added to the system.";
	log_errors (\@errors);
	print "$message\n";
	log_timed_message $message;
	return ERROR_STATUS;
    }
    else {
	my $applied_host_profile = 0;
	if ( $hostprofile_id and not has_host_profile_assigned( $final_hostname, $hostprofile_id ) ) {
	    ## Apply the host profile if it wasn't already assigned (which it should have been, as well as being
	    ## applied, if the host just got added).  Strictly speaking, the host profile could have been previously
	    ## assigned but not applied at that time, or it might have been altered since it was last applied, and
	    ## we're not covering those cases here for now.  FIX MINOR:  Perhaps we should, in particular because not
	    ## just the associated service profiles, but also any externals attached to the host profile or to any
	    ## services attached to any service profiles attached to this host profile, may have changed since this
	    ## host profile was last applied to this host.

	    # We merge any services from the service profiles in the host profile into any existing services on the host,
	    # so as not to disturb any prior independent setup.  (There is no one right answer for all such cases.)
	    my $replace_existing_services = 0;  # 1 => replace, 0 => merge
	    if ( !$monarchapi->assign_and_apply_hostprofile_to_host( $final_hostname, $host_profile, $replace_existing_services ) ) {
		log_dassmonarch();
		print "ERROR:  Application of host profile \"$host_profile\" to host \"$final_hostname\" has failed.\n";
		log_timed_message "ERROR:  Application of host profile \"$host_profile\" to host \"$final_hostname\" has failed.";
	    }
	    else {
		log_timed_message "STATS:  Host profile \"$host_profile\" has been applied to host \"$final_hostname\".";
		$applied_host_profile = 1;
	    }
	    ## Service profiles referenced by the host profile will have been applied when we just applied the
	    ## host profile, so there is no need to also call assign_and_apply_hostprofile_serviceprofiles().
	    ## Host externals and service externals will have been applied to the host and its host services
	    ## when the corresponding profiles were applied.
	}

	if ($serviceprofile_id) {
	    my $assign_and_apply_service_profile = 0;
	    my $remove_service_profile           = 0;

	    # See our case-analysis at the end for details of the logic here.

	    # For now, we will call certain monarchWrapper functions directly, instead of through another
	    # layer of indirection at the dassmonarch layer.  If we did define such functions within
	    # dassmonarch, we would probably use object names instead of object IDs as parameters, making
	    # these routines that much less efficient.

	    if ( monarchWrapper->host_has_service_profile_via_host( $found_host->{host_id}, $serviceprofile_id ) ) {
		## The service profile is directly assigned to the host.  We apply it now to ensure that the
		## current definition of the service profile is used to create all the desired services, without
		## checking if the direct assignment (and application) just happened because we applied a host
		## profile containing this service profile.
		$assign_and_apply_service_profile = 1;
	    }
	    elsif ( monarchWrapper->host_has_service_profile_via_hostgroup( $found_host->{host_id}, $serviceprofile_id ) ) {
		## The service profile is assigned to a hostgroup containing the host, but not directly to
		## the host (by dint of how we got here).  So we force the current service profile definition
		## to be applied, but then back out the direct assignment of the service profile to the host.
		$assign_and_apply_service_profile = 1;
		$remove_service_profile           = 1;
	    }
	    elsif ( monarchWrapper->host_has_service_profile_via_hostprofile( $found_host->{host_id}, $serviceprofile_id ) ) {
		if ($is_new_host) {
		    ## We have a new host for which the service profile should have just been applied while applying the
		    ## host profile that references it, so this case should never occur, and there's nothing to do here.
		    ## FIX MINOR:  Abort this run of auto-registration, since the fact that we're here is evidence of failure.
		}
		elsif ($applied_host_profile) {
		    ## We have an existing host for which we just applied a host profile that references this service
		    ## profile, so the service profile should have just been assigned and applied, this case should have
		    ## been covered in the first step above (so we should not be here), and there's nothing to do now.
		    ## FIX MINOR:  Abort this run of auto-registration, since the fact that we're here is evidence of failure.
		}
		else {
		    ## We have a previously-existing host that either already had the same host profile assigned, or we had
		    ## no host profile defined from external sources that we should have applied earlier, so we didn't apply
		    ## any host profile and therefore didn't just apply this service profile.  FIX MINOR:  The fact that we
		    ## are in this case suggests that we should re-apply the entire host profile, if we in fact have one, at
		    ## this time, not just this one service profile.  And if we don't have a host profile named from some
		    ## external source, then we should just re-apply the host profile that is currently applied to this host.
		    if ($hostprofile_id) {
			print "WARNING:  Application of host profile \"$host_profile\" to host \"$final_hostname\" might not be complete.\n";
			log_timed_message "WARNING:  Application of host profile \"$host_profile\" to host \"$final_hostname\" might not be complete.";
		    }
		    $assign_and_apply_service_profile = 1;
		}
	    }
	    else {
		## The service profile is not associated with the host in any way.  So we impose a direct assignment,
		## and apply that assignment to get the services in place.
		$assign_and_apply_service_profile = 1;
	    }

	    if ($assign_and_apply_service_profile) {
		## We merge any services from the service profile into any existing services on the host, so as
		## not to disturb any prior independent setup.  (There is no one right answer for all such cases.)
		my $replace_existing_services = 0;    # 1 => replace, 0 => merge
		if ( !$monarchapi->assign_and_apply_serviceprofile_to_host( $final_hostname, $service_profile, $replace_existing_services ) ) {
		    log_dassmonarch();
		    print "ERROR:  Application of service profile \"$service_profile\" to host \"$final_hostname\" has failed.\n";
		    log_timed_message "ERROR:  Application of service profile \"$service_profile\" to host \"$final_hostname\" has failed.";
		}
		else {
		    log_timed_message "STATS:  Service profile \"$service_profile\" has been applied to host \"$final_hostname\".";
		}
	    }
	    if ($remove_service_profile) {
		if ( !$monarchapi->remove_serviceprofile_from_host( $final_hostname, $service_profile ) ) {
		    log_dassmonarch();
		    print "ERROR:  Removal of service profile \"$service_profile\" assignment from host \"$final_hostname\" has failed.\n";
		    log_timed_message "ERROR:  Removal of service profile \"$service_profile\" assignment from host \"$final_hostname\" has failed.";
		}
		else {
		    log_timed_message "STATS:  Direct assignment of service profile \"$service_profile\" has been removed from host \"$final_hostname\".";
		}
	    }
	}

	# Hostgroup assignment should always be done for a newly added host, so the Status Viewer will operate correctly.
	# For an existing host, hostgroup assignment should normally only be done if there is not already some existing
	# hostgroup containing that host, so as to preserve any local changes to the setup after the host got initially
	# registered and not have the host pop back up where it is no longer wanted.
	if ($have_hostgroup_determination) {
	    my $assign_hostgroups = 1;
	    if (!$is_new_host and !$assign_hostgroups_to_existing_hostgroup_hosts) {
		my $host_hostgroups = $monarchapi->get_hostgroups_for_host($final_hostname);
		$assign_hostgroups = 0 if @$host_hostgroups;
	    }
	    if ($assign_hostgroups) {
		my $hostgroups = [];
		( $outcome, $err_ref, $hostgroups ) =
		  $customer_network->hostgroups_to_assign( $agent_type, $final_hostname, $host_address, $host_mac, $host_os, $host_profile,
		    $service_profile );
		push @errors, @$err_ref;
		if (!$outcome) {
		    log_errors (\@errors);
		    print "ERROR:  Auto-registration request has failed while trying to determine proper hostgroup(s).\n";
		    log_timed_message "FATAL:  Cannot determine hostgroup(s) for host \"$final_hostname\", address \"$host_address\"; auto-registration request has failed.";
		    ## Notice that if we added the host above, we're not backing that out here.  Perhaps we should.
		    return ERROR_STATUS;
		}
		else {
		    foreach my $hostgroup (@$hostgroups) {
			if ( !$monarchapi->hostgroup_exists($hostgroup) ) {
			    print "ERROR:  Hostgroup \"$hostgroup\" (for host \"$final_hostname\") does not exist.\n";
			    log_timed_message "ERROR:  Hostgroup \"$hostgroup\" (for host \"$final_hostname\") does not exist.";
			}
			else {
			    ## The assign_hostgroup() call will check internally to see if the host is already a member
			    ## of the hostgroup, so we don't do so before trying to assign the host to the hostgroup.
			    if ( !$monarchapi->assign_hostgroup( $final_hostname, $hostgroup ) ) {
				log_dassmonarch();
				print "ERROR:  Assignment of hostgroup \"$hostgroup\" to host \"$final_hostname\" has failed.\n";
				log_timed_message "ERROR:  Assignment of hostgroup \"$hostgroup\" to host \"$final_hostname\" has failed.";
			    }
			    else {
				log_timed_message "STATS:  Hostgroup \"$hostgroup\" has been assigned to host \"$final_hostname\".";
			    }
			}
		    }
		}
	    }
	}

	# Monarch-group assignment should always be done for a newly added host, so we have some location defined in which to place
	# the generated externals file for this host.  For an existing host, Monarch-group assignment should normally only be done
	# if there is not already some existing Monarch group for that host, so as not to possibly conflict with the existing setup.
	my $host_has_all_monarch_groups_assigned = 1;
	if ($have_monarch_group_determination) {
	    my $assign_monarch_groups = 1;
	    if (!$is_new_host and !$assign_monarch_groups_to_existing_group_hosts) {
		my $orphans = $monarchapi->get_monarch_group_orphans();
		$assign_monarch_groups = 0 if not exists $orphans->{$final_hostname};
	    }
	    if ($assign_monarch_groups) {
		my $monarch_groups = [];
		( $outcome, $err_ref, $monarch_groups ) =
		  $customer_network->monarch_groups_to_assign( $agent_type, $final_hostname, $host_address, $host_mac, $host_os, $host_profile,
		    $service_profile );
		push @errors, @$err_ref;
		if (!$outcome) {
		    log_errors (\@errors);
		    print "ERROR:  Auto-registration request has failed while trying to determine proper Monarch group(s).\n";
		    log_timed_message "FATAL:  Cannot determine Monarch group(s) for host \"$final_hostname\", address \"$host_address\"; auto-registration request has failed.";
		    ## Notice that if we added the host above, we're not backing that out here.  Perhaps we should.
		    return ERROR_STATUS;
		}
		else {
		    my @assigned_hostgroups = $monarchapi->get_hostgroups_for_host($final_hostname);
		    my %host_hostgroups = ();
		    @host_hostgroups{@assigned_hostgroups} = (1) x @assigned_hostgroups;
		    foreach my $monarch_group (@$monarch_groups) {
			if ( !$monarchapi->monarch_group_exists($monarch_group) ) {
			    print "ERROR:  Monarch group \"$monarch_group\" (for host \"$final_hostname\") does not exist.\n";
			    log_timed_message "ERROR:  Monarch group \"$monarch_group\" (for host \"$final_hostname\") does not exist.";
			    $host_has_all_monarch_groups_assigned = 0;
			}
			else {
			    ## We prefer to assign the host to the Monarch group indirectly, as that makes the system easier to manage.
			    ## So if we already have matching hostgroups for the host and the Monarch group, we don't bother to make a
			    ## direct assignment.
			    my $mg_hostgroups = $monarchapi->get_monarch_group_hostgroups($monarch_group);
			    if (not defined $mg_hostgroups) {
				log_errors (\@errors);
				log_dassmonarch();
				print "ERROR:  Auto-registration request has failed while trying to assign Monarch group(s).\n";
				log_timed_message "FATAL:  Cannot assign Monarch group(s) for host \"$final_hostname\", address \"$host_address\"; auto-registration request has failed.";
				## Notice that if we added the host above, we're not backing that out here.  Perhaps we should.
				return ERROR_STATUS;
			    }
			    else {
				my $in_shared_hostgroup = 0;
				foreach my $mg_hostgroup (@$mg_hostgroups) {
				    if ( $host_hostgroups{$mg_hostgroup} ) {
					$in_shared_hostgroup = 1;
					last;
				    }
				}
				if (!$in_shared_hostgroup) {
				    ## The assign_monarch_group_hosts() call will check internally to see if the host is already a member
				    ## of the Monarch group, so we don't do so before trying to assign the host to the Monarch group.
				    if ( !$monarchapi->assign_monarch_group_hosts( $monarch_group, [$final_hostname] ) ) {
					log_dassmonarch();
					print "ERROR:  Assignment of Monarch group \"$monarch_group\" to host \"$final_hostname\" has failed.\n";
					log_timed_message "ERROR:  Assignment of Monarch group \"$monarch_group\" to host \"$final_hostname\" has failed.";
					$host_has_all_monarch_groups_assigned = 0;
				    }
				    else {
					log_timed_message "STATS:  Monarch group \"$monarch_group\" has been assigned to host \"$final_hostname\".";
				    }
				}
			    }
			}
		    }
		}
	    }
	}

	# For now, we don't provide any explicit means here to establish (create and/or apply) certain host externals
	# on the host, nor any service externals on the host services, even though they will be needed if the host
	# is being registered for remote-client monitoring.  Instead, we just assume that any such externals will
	# be managed by having them be already attached to the host profile which was applied to the host, or to
	# the services referenced by service profiles attached to that host profile, or to the services referenced
	# by the service profile which may have been directly applied to the host, by prior action outside of this
	# auto-registration.  Then the application of those profiles above should be a deep copy that applies the
	# associated externals to the host and host services.

	# We build externals for this host right away, because regardless of whether or not the host already
	# existed before this script was called, it wouldn't have been called unless the client could not get
	# hold of its externals file from the server.

	# We need some mechanism to block concurrent building of externals files, from all sources, at least on an
	# individual-file level, to prevent collisions and possible file corruption.  Such protection is built into
	# MonarchExternals.pm, which uses a file lock to prevent more than one actor from handling each file at a
	# given time.  This is all we really need; if some other actor is building the particular externals file that
	# we care about, then the file will be built by that actor, and it doesn't really matter that we didn't do
	# the work ourselves.  The only downside of this granularity is that every attempt to build externals for an
	# individual host will incur all the overhead of doing so, which might potentially involve a lot of analysis
	# for lots of other hosts as well that could perhaps be avoided.  In that sense, it might be advantageous
	# from a system-load perspective to instead queue and consolidate requests to build externals before doing
	# so.  We leave such a possible optimization to future evolution.

	# If we don't create the externals file here, then we don't tell the client what its hostname is,
	# so it will be forced to register again at some future time, which will cause another attempt
	# to build externals for this host.
	if ( !$host_has_all_monarch_groups_assigned ) {
	    my $message = "Externals are not being built for host \"$final_hostname\" because of earlier Monarch group issues.";
	    print "ERROR:  $message\n";
	    log_timed_message "ERROR:  $message";
	    ## We generate a Foundation log message to bring this kind of failure to the attention of the
	    ## system operators, so they know the host probably won't be being monitored as intended.
	    $message = "Host \"$final_hostname\" will not be monitored because of Monarch group issues during auto-registration.";
	    log_to_foundation( SEVERITY_WARNING, $message );
	}
	elsif ( !$monarchapi->buildSomeExternals( [$final_hostname] ) ) {
	    log_dassmonarch();
	    my $message = "Building of externals for host \"$final_hostname\" has failed.";
	    print "ERROR:  $message\n        (See $logfile on the server.)\n";
	    log_timed_message "ERROR:  $message";
	    ## We generate a Foundation log message to bring this kind of failure to the attention of the
	    ## system operators, so they know the host probably won't be being monitored as intended.
	    log_to_foundation( SEVERITY_WARNING, $message );
	}
	else {
	    log_timed_message "STATS:  Externals have been created for host \"$final_hostname\".";

	    # This is how we let the client know the exact hostname under which it is registered within Monarch.
	    # The client should use this to figure out the filename of the externals file that it should download
	    # from the server to configure various GDMA options and what service checks the client should run.
	    # We only send this once we know the externals file has been successfully built for this host.  Without
	    # that, the host will be forced to register again at some future time, which will cause another attempt
	    # to build externals for this host.  So the system will attempt to self-heal in that fashion.
	    print "hostname=$final_hostname\n";
	}
    }

    return STOP_STATUS;
}

sub log_errors {
    my $errors = shift;
    foreach my $err (@$errors) {
	log_message $err;
    }
}

sub log_dassmonarch {
    if ($debug_minimal) {
	my $dm_error = $monarchapi->get_errormessages();
	chomp $dm_error;
	log_timed_message $dm_error if $dm_error;
    }
    if ($debug_maximal) {
	my $dm_debug = $monarchapi->get_debugmessages();
	chomp $dm_debug;
	log_timed_message $dm_debug if $dm_debug;
    }
}

sub log_to_foundation {
    my $severity   = shift;
    my $message    = shift;
    my $foundation = undef;
    local $_;

    eval {
	$foundation = GW::Foundation->new( $foundation_host, $foundation_port, $monitor_server_hostname, $monitor_server_ip_address,
	  $socket_send_timeout, $send_buffer_size, $debug_basic );
    };
    if ($@) {
	chomp $@;
	log_timed_message $@;
    }
    if ( defined $foundation ) {
	my $errors = $foundation->send_log_message( $severity, APP_SYSTEM, $message );
	map { log_timed_message $_ } @$errors if defined $errors;
    }
}

sub allow {
    my $package = shift;
    ## We're careful to use a form of the require that should provide some protection
    ## against Perl-injection attacks through our configuration file, though of course
    ## there is no possible protection against what is in the allowed package itself.
    return 0 if ! defined $package || ! $package;
    eval {require "$package.pm";};
    if ($@) {
	## 'require' died; $package is not available.
	return 0;
    } else {
	## 'require' succeeded; $package was loaded.
	return 1;
    }
}

sub by_address {
    ## This mainly handles IPv4 addresses at the moment.  If we have IPv6, we just compare the
    ## literal strings.  Perhaps some future version of this script will extend this logic.
    ($a =~ /\./ and $b =~ /\./) ? inet_aton($a) cmp inet_aton($b) : $a <=> $b;
}

sub read_config_file {
    my $config_file  = shift;
    my $config_debug = shift;

    # All the config-file processing is wrapped in an eval{}; because TypedConfig
    # throws exceptions when it cannot open the config file or finds bad config data.
    eval {
	my $config = TypedConfig->new( $config_file, $config_debug );

	# Whether to process anything.  Turn this off if you want to disable
	# this process completely, so auto-registration is prohibited.
	$enable_processing = $config->get_boolean('enable_processing');

	$debug_level = $config->get_number('debug_level');

	$debug_minimal = ( $debug_level >= 1 );
	$debug_basic   = ( $debug_level >= 2 );
	$debug_maximal = ( $debug_level >= 3 );

	# Where to log debug messages.
	$logfile = $config->get_scalar ('logfile');

	$hostname_qualification = $config->get_scalar('hostname_qualification');
	if ($hostname_qualification !~ /^(full|short|custom)$/) {
	    die "ERROR:  configured value for hostname_qualification (\"$hostname_qualification\") is not supported\n";
	}

	%hardcoded_hostnames = $config->get_hash('hardcoded_hostnames');

	if (%hardcoded_hostnames) {
	    foreach my $ipaddr ( sort by_address keys %hardcoded_hostnames ) {
		my $name = $hardcoded_hostnames{$ipaddr};
		my $label = '(?:[a-zA-Z0-9](?:[-a-zA-Z0-9]{0,61}[a-zA-Z0-9])?)';
		## FIX LATER:  A future version will use some standard Perl module to validate the address,
		## and support IPv6 addresses as well.
		if ($ipaddr !~ /^(?:\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}|)$/) {
		    die "ERROR:  illegal address \"$ipaddr\" found in <hardcoded_hostnames> section of config file\n";
		    ## If we ever decide to just print that message and continue here instead of dying,
		    ## then we'd better delete the invalid entry.
		    delete $hardcoded_hostnames{$ipaddr};
		}
		elsif (length($name) > 255 or $name !~ /^$label(?:\.$label)*$/o) {
		    die "ERROR:  address \"$ipaddr\" maps to illegal hostname \"$hardcoded_hostnames{$ipaddr}\" found in <hardcoded_hostnames> section of config file\n";
		    ## If we ever decide to just print that message and continue here instead of dying,
		    ## then we'd better delete the invalid entry.
		    delete $hardcoded_hostnames{$ipaddr};
		}
	    }
	}

	$default_host_profile                          = $config->get_scalar('default_host_profile');
	$default_hostgroup                             = $config->get_scalar('default_hostgroup');
	$assign_hostgroups_to_existing_hostgroup_hosts = $config->get_boolean('assign_hostgroups_to_existing_hostgroup_hosts');
	$default_monarch_group                         = $config->get_scalar('default_monarch_group');
	$assign_monarch_groups_to_existing_group_hosts = $config->get_boolean('assign_monarch_groups_to_existing_group_hosts');
	$compare_to_foundation_hosts                   = $config->get_boolean('compare_to_foundation_hosts');
	$match_case_insensitive_foundation_hosts       = $config->get_boolean('match_case_insensitive_foundation_hosts');
	$force_hostname_case                           = $config->get_scalar('force_hostname_case');
	$force_domainname_case                         = $config->get_scalar('force_domainname_case');
	$use_hostname_as_key                           = $config->get_boolean('use_hostname_as_key');
	$use_mac_as_key                                = $config->get_boolean('use_mac_as_key');
	$rest_api_requestor                            = $config->get_scalar('rest_api_requestor');
	$ws_client_config_file                         = $config->get_scalar('ws_client_config_file');
	$log4perl_config                               = $config->get_scalar('log4perl_config');

	if ( $force_hostname_case !~ /^(lower|upper|as-is)$/ ) {
	    die "ERROR:  force_hostname_case must be \"lower\" or \"upper\" or \"as-is\"\n";
	}
	if ( $force_domainname_case !~ /^(lower|upper|as-is)$/ ) {
	    die "ERROR:  force_domainname_case must be \"lower\" or \"upper\" or \"as-is\"\n";
	}

	if ( $use_hostname_as_key && $use_mac_as_key ) {
	    die "ERROR:  use_hostname_as_key and use_mac_as_key cannot both be true\n";
	}

	# Set to the name of an external package (not including the .pm filename extension) to call
	# to analyze and recode host attributes, or to an empty string if no such package should be used.
	$customer_network_package      = $config->get_scalar('customer_network_package');
	$have_customer_network_package = allow $customer_network_package;

	if ($customer_network_package) {
	    if ( !$have_customer_network_package ) {
		## $@ here is from the allow() call just above, not from a local eval{};.
		chomp $@;
		die "ERROR:  configured external package \"$customer_network_package\" cannot be found:  $@\n";
	    }

	    $customer_network =
	    $customer_network_package->new(
		{
		    default_host_profile   => $default_host_profile,
		    default_hostgroup      => $default_hostgroup,
		    default_monarch_group  => $default_monarch_group,
		    hardcoded_hostnames    => \%hardcoded_hostnames,
		    hostname_qualification => $hostname_qualification,
		    force_hostname_case    => $force_hostname_case,
		    force_domainname_case  => $force_domainname_case,
		    use_hostname_as_key    => $use_hostname_as_key,
		    use_mac_as_key         => $use_mac_as_key
		}
	    );
	    $customer_network->debug($debug_level) if $customer_network->can('debug');
	}

	# Whether the package defines soft and hard recoding, and in what forms, depends on the particular
	# desires of the customer.  The hostgroup and Monarch group determination routines are theoretically
	# optional, but in all practical cases, at least simple versions of these routines must be supplied.
	$have_soft_recode                 = $have_customer_network_package && $customer_network->can('soft_recode_host_attributes');
	$have_hard_recode                 = $have_customer_network_package && $customer_network->can('hard_recode_host_attributes');
	$have_hostgroup_determination     = $have_customer_network_package && $customer_network->can('hostgroups_to_assign');
	$have_monarch_group_determination = $have_customer_network_package && $customer_network->can('monarch_groups_to_assign');
    };
    if ($@) {
	chomp $@;
	$@ =~ s/^ERROR:\s+//i;
	print "ERROR:  Cannot read config file $config_file\n  ($@).\n";
	return 0;
    }

    return 1;
}

# Verify that this host exists.  If so, return the details of the
# found host, which might not precisely match the search criteria.
# The agent type possibly determines the reliability of the $host, $ip,
# and $mac fields, and whether to test using those particular values.
sub host_exists {
    my $agent = shift;
    my $host  = shift;
    my $ip    = shift;
    my $mac   = shift;
    my %found = ();

    # First, check by name.
    my %host = $monarchapi->host_exists($host);
    # Version 1.2.1 update.
    # This logic doesn't seem right and we can't think of a case where it makes sense.
    # A hostname is unique in monarch and is case sensitive. If a host exists with different
    # cases in monarch, thats fine. It's the agent's responsibility to configure the case
    # of the hostname. If dassmonarch::host_exists( somename ) returns something, then host
    # somename exists end of story. Multiple hosts can have same ip, but they have to have
    # different names.
    # Case where checking $host{name} and $host{address} fails badly :
    # Agent registers successfully; operator changes address in monarch;
    # agent has a problem later that causes it to try auto registration again;
    # host_exists now thinks host not found since incoming address != monarch address;
    # host not found in monarch -> host_import_api with updates called, which resets the
    # config on the host (alias, address, services, profile etc) to the host profile
    # given to host_import_api (from the agent originally). See more notes in host_exists().
    #if ( %host and $host{name} eq $host and $host{address} eq $ip ) {
    if ( %host ) {
	    %found = %host;
    }
    elsif ( $agent ne 'VEMA' ) {
	## Since that didn't work, check by IP address.  This is why we try to normalize the IP address
	## elsewhere in this script, before possible comparison here.  Inasmuch as we don't have any
	## guarantee at the database level that the address field is unique, this could be problematic,
	## as we might end up fetching some mixture of data from multiple hosts.  The best we can do is
	## to find some matching host, then fetch the full host details from that reference.  Of course,
	## there's always the possibility here that we could find the wrong matching host!
	my $hostnames = $monarchapi->get_hosts_with_address($ip);
	foreach my $hostname (@$hostnames) {
	    %host = $monarchapi->host_exists($hostname);
	    if ( %host and $host{address} eq $ip ) {
		%found = %host;
		last;
	    }
	}
    }
    ## Currently, we don't store the MAC address anywhere in Monarch, so we
    ## cannot compare the request MAC address with anything in the database.

    return \%found;
}

# Verify that this host has this host profile assigned.
sub has_host_profile_assigned {
    my $host  = shift;
    my $hp_id = shift;

    # FIX LATER:  This is really an abuse of the $monarchapi->host_exists() routine,
    # taking advantage of the actual result it returns instead of what it is documented
    # to return.  This ought to be fixed in dassmonarch, too, thereby outlawing this
    # construction.  But then we would need to provide some alternate routine that would
    # provide return data similar to what that routine now provides.
    my %host = $monarchapi->host_exists($host);
    if (%host) {
	return 1 if defined( $host{hostprofile_id} ) and $host{hostprofile_id} == $hp_id;
    }

    return 0;
}

sub is_valid_ip_address {
    my $ipaddr = shift;
    ## It would be nice if we disallowed the network address ("network ID"), and broadcast
    ## and multicast addresses, but then (at least, for some such purposes) we would need
    ## to know subnet sizes.  And any client which is likely to send such an address is
    ## also likely to spoof the subnet mask, so such a test seems pointless.
    ## FIX MINOR:  However, we ought to use some standard Perl module for this validation.
    return 1 if $ipaddr =~ /^\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}$/;
    ## FIX MINOR:  Validate IPv6 addresses using some standard Perl module.
    ## This expression is far too primitive, as it passes some non-IPv6 strings.
    return 1 if $ipaddr =~ /^[0-9a-f]{0,4}(:[0-0a-f]{0,4])+$/i;
    return 0;
}

__END__

========================================================================
How a host profile should be applied to a host.
========================================================================

When we apply a host profile to a host via the UI, we are given lots of options
about which pieces ought to be applied:  parents, hostgroups, escalations,
contact groups, custom object variables, and the profile detail (which essentially
comprises the host template and any overrides to that template that are associated
with the host profile, exclusive of contact groups and custom object variables,
whose application is controlled via separate choices).  The question for us here
is, to what extent are all these component pieces to be applied to a host during
the auto-registration process, for either a new host or for an existing host?

I don't have an explicit answer for that yet.  The present code is taking some
default action that has not been analyzed in detail.  It is presumably applying
all of the associated data objects in an indiscriminant fashion.

We also need to know what to do with service profiles associated with the host
profile, and of course the services associated with those service profiles.
When we apply a host profile to a host via the UI, we apparently also directly
apply the service profiles from the host profile to the host (as well as the
services in the service profiles, which become host services, which is of course
the target action at this level).  While I think it probably makes more sense
to just apply the services in those service profiles to the host, and let the
service profiles themselves continue to be indirectly associated with the host,
only via the host profile, that kind of sophistication will apparently await a
later release, when we do a better job of displaying and manipulating direct and
indirect associations throughout the data model.

========================================================================
Case analysis of how a service profile should be applied to a host.
========================================================================

When we apply an individual service profile (not via a host profile) to a host,
what we care about is making sure all the services assigned to the service profile
end up being assigned to the host as host services.  We would like to do this at as
high a level as possible, while leaving behind as little debris as possible that
might need to be managed in the future.  We also need to take into consideration
the notion that whatever application of the service profile to the host might
have happened in the past, the current definition of the service profile might
now be different, so we need to force application at this time just to be sure
that we really are achieving the desired result.  The cases we might see are:

(*) We look and find the service profile is assigned directly to the host.

    Action:  In this case, we need only apply the service profile to the host,
    merging the services from the service profile with any existing services on
    that host, to ensure that the current definition of the service profile is
    completely applied now even if it may have already been applied in the past.
    No cleanup afterward is needed.

(*) Otherwise, we look and find the service profile is assigned to a hostgroup
    of which the host is a member (and per this sequence of tests, the service
    profile is not directly assigned to the host).

    Action:  Simply having the assignments of service profile to hostgroup, and
    hostgroup to host, does not necessarily mean that the service profile has
    previously been applied to this host.  We can force that now by assigning
    and applying the service profile to the host, but given the linkage through
    the hostgroup, we would rather not leave the low-level direct association
    of service profile to host around afterward, if that association was not
    already in place (as it was not, by dint of how we got to this case).  So we
    temporarily assign the service profile to the host for purposes of applying
    it, merging any services from the service profile into the services already
    on the host.  But since the direct association was not previously in place,
    we then remove the service profile assignment from the host, leaving behind
    the individual host services that got applied to the host.

(*) Otherwise, we look and find the service profile is assigned to the host
    profile which is assigned to the host.

    Action:  Look at sub-cases.

    (+) If this was a new host, then the service profile should have already been
	(assigned and) applied when the host was created, as part of a deep-copy
	operation on the host profile.	So this case should never occur, given
	that we already looked to see if the service profile was directly assigned
	to the host and found that it was not, and there is nothing to do now
	(except perhaps to raise a warning that the deep copy seems to have failed,
	and perhaps abort the entire auto-registration, if we don't want to try
	to repair it now).

    (+) If this was an existing host and the same host profile had not been
	previously assigned to the host, then we obviously have fixed that (by
	virtue of the fact that we now see that this host profile is assigned to
	the host).  And in so doing, when we assigned and applied the host profile
	to the host, all of its service profiles will have been (assigned and)
	applied at the same time.  So we should never reach this case (since we
	should have found the service profile assigned directly to the host in
	a previous test), and there is nothing to do now.

    (+) If this was an existing host and either the same host profile had previously
	been assigned to the host or we had no named host profile to apply to the
	existing host, we have not earlier forced the host profile to be assigned
	and applied to the host during this auto-registration request.	So we might
	assume without checking that the host profile was also previously applied
	(not just assigned) to the host, in which case all of its service profiles
	should have been (assigned and) applied at the same time, and there is
	logically nothing to do now.  However, it is possible that the service
	profile was assigned to the host profile after the host profile was last
	applied to the host, or that the definition of the service profile has
	been augmented with additional services since it was last applied to the
	host, or that externals attached to these host or service profiles have
	been modified in the interim.  In fact, since applying a host profile to
	a host also assigns its service profiles directly to the host, the fact
	that we are in this case proves that the service profile was assigned to
	the host profile after the host profile was last (assigned and) applied to
	this host.  So in this case, we assign and apply the service profile to the
	host, leaving it (with respect to this one service profile) in the state
	it would have been had we applied the entire host profile to this host.
	(FIX MINOR:  The fact that we are in this case clearly indicates that
	there might be other aspects of the host profile as applied to this host
	that might also be out-of-date [in particular, such as host or service
	externals directly or indirectly attached to the host profile], so perhaps
	we should just re-apply the entire host profile at this point instead of
	just applying this one service profile.)  Note that to distinguish this
	sub-case from the previous sub-case, we will have needed to save our earlier
	determination of whether the host profile was previously assigned to the
	host before we possibly forcibly assigned and applied the host profile.

(*) Otherwise, we conclude that the service profile is not associated with the host
    in any way.

    Action:  Assign the service profile directly to the host, and apply it to that
    host.  As above, we merge with instead of replacing existing host services,
    because we want to preserve any other services that are referenced by other
    service profiles assigned to the host profile which is assigned to the host,
    or by other service profiles directly assigned to the host, or that are just
    directly assigned to the host independent of any profiles.

