#!/usr/local/groundwork/perl/bin/perl -w --
#
# Copyright 2007-2016 GroundWork Open Source, Inc.(GroundWork)  San Francisco CA
# All rights reserved. This program is free software; you can redistribute it and/or
# modify it under the terms of the GNU General Public License version 2 as published
# by the Free Software Foundation.
#
# This program is distributed in the hope that it will be useful, but WITHOUT ANY
# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
# PARTICULAR PURPOSE.  See the GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License along with this
# program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street,
# Fifth Floor, Boston, MA 02110-1301, USA.

use strict;

use Time::HiRes;
use IO::Socket;
use LWP::UserAgent;
use HTTP::Cookies;
use XML::Parser;
use Fcntl;

use TypedConfig;

use Data::Dumper;
$Data::Dumper::Indent   = 1;
$Data::Dumper::Sortkeys = 1;

my $version = "";
$version = "1.2.1";    # 2012-12-21.rlynch - functionality added to trigger RTMM cache
$version = "1.2.2";    # 2013-09-05.gherteg - use GWMEE 7.0.0 login/out URLs; change user; log out at end
$version = "1.2.3";    # 2013-09-12.gherteg - wait until the portal is really ready before contacting SV
$version = "1.2.4";    # 2013-09-24.gherteg - implement manual deployments of portal applications
$version = "1.2.5";    # 2013-12-16.gherteg - add option to wait for externally-triggered deployments
$version = "1.2.6";    # 2014-03-16.rruttimann - modified code to avoid exceptions because of modified config-file format
$version = "1.2.7";    # 2014-04-18.gherteg - generalize the deployment base directory to support modified dual jboss startup processing
$version = "1.2.8";    # 2016-01-08.dnicholas - tightened up config xml parse/read error handling GWMON-12456
$version = "1.2.9";    # 2016-09-15.gherteg - ported code to pass compilation checks under Perl 5.24.0
$version = "1.2.10";   # 2016-09-21.gherteg - fix unescaped brace in regex, now deprecated in Perl 5.24.0

# FIX MINOR:  The parsing of command-line arguments needs improvement.
my $debug        = 0;
my $debug_config = 0;    # if set, spill out certain data about config-file processing to STDOUT
$debug_config++, shift @ARGV while ( @ARGV && $ARGV[0] eq "-d" );
my $verbose      = 0;    # increment ...
$verbose++, shift @ARGV while ( @ARGV && $ARGV[0] eq "-v" );
my $remove_markers = 0;    # increment ...
$remove_markers++, shift @ARGV while ( @ARGV && $ARGV[0] eq "-r" );
my $query_for_deployments = 0;    # increment ...
$query_for_deployments++, shift @ARGV while ( @ARGV && $ARGV[0] eq "-q" );
my $wait_for_deployments = 0;    # increment ...
$wait_for_deployments++, shift @ARGV while ( @ARGV && $ARGV[0] eq "-w" );

my $fqhost = "";
my $host = $ARGV[0];
my $port = $ARGV[1];
my $file = $ARGV[2];  # a config-file override allowed as argument #3
my %pair = ();        # which will have configuration information.

my $socket;
my $user_agent;

my $portal_start_wait_time = 0;
my $listener_wait_time     = 0;
my $total_deployments_time = 0;

# ----------------------------------------------------------------------
# Locally-set configuration parameters.  In a future version of this
# script, many of these values may be migrated to the $config_file.
# ----------------------------------------------------------------------

my $config_file = "/usr/local/groundwork/foundation/container/jpp/standalone/configuration/check-listener.conf";
my $deployment_base_dir_pattern = "/usr/local/groundwork/foundation/container/(?:jpp|jpp2)/standalone/deployments";

my @marker_file_extensions_to_remove = qw(
  .dodeploy
  .skipdeploy
  .isdeploying
  .deployed
  .failed
  .isundeploying
  .undeployed
  .pending
);

# GWMON-7407
my $socket_send_timeout        = 60;   # seconds

my $get_socket_retries         = 20;   # count
my $sleep_on_no_socket         = 15;   # seconds
my $loop_redirect_max          = 10;   # count
my $sleep_on_state_wait        = 5;    # seconds
my $server_state_loop_max      = 60;   # count
my $status_viewer_properties   = '/usr/local/groundwork/config/status-viewer.properties';
my $foundation_properties_file = $file || '/usr/local/groundwork/config/foundation.properties';
my $configuration_properties   = '/usr/local/groundwork/config/configuration.properties';
my $josso_agent_config         = '/usr/local/groundwork/config/josso-agent-config.xml';
my $SETENV_SCRIPT              = '/usr/local/groundwork/scripts/setenv.sh';
my $JPP_HOME                   = '/usr/local/groundwork/foundation/container/jpp';

use constant DEPLOY_IN_SEQUENCE => 0;
use constant DEPLOY_IN_PARALLEL => 1;

# ----------------------------------------------------------------------
# Externally-set configuration parameters.
# ----------------------------------------------------------------------

my $initial_deployment_max_cycles       = undef;
my $initial_deployment_cycle_sleep_time = undef;

my $secondary_deployment_max_cycles       = undef;
my $secondary_deployment_cycle_sleep_time = undef;

my $tertiary_deployment_max_cycles       = undef;
my $tertiary_deployment_cycle_sleep_time = undef;

my @initial_deployments   = ();
my @secondary_deployments = ();
my @tertiary_deployments  = ();

# ----------------------------------------------------------------------
# Program.
# ----------------------------------------------------------------------
main();

# ----------------------------------------------------------------------
# Supporting subroutines.
# ----------------------------------------------------------------------

sub main
{
    # Make sure that all output is flushed as soon as possible to the terminal,
    # not waiting for the next newline to be output.
    STDOUT->autoflush(1);

    # print "\n";       # make sure we're against the leftwall

    my $xc = ""; # Exit Code
    my $em = ""; # Error Message

    # ----------------------------------------------------------------------
    # there are NO allowed error-returns, because this code is optional
    # ----------------------------------------------------------------------
    exit(0) if ( $xc = &read_configuration() );

    $em .= "check-listener.pl {HOST} {PORT}\n" if ( ( !defined $host ) || ( !defined $port ) );

    print STDERR $em if $em;
    exit(1) if $em;

    # ----------------------------------------------------------------------
    # exit(0) can be used (but just in development testing) to render the
    # underlying errors or failures 'soft'; production use demands exit(1)
    # for any failure.
    # FIX MINOR:  Modify all these routines so they return true on success,
    # false on failure, per standard Perl usage; then change these test.
    # ----------------------------------------------------------------------
    exit(1) if not read_config_file( $config_file, $debug_config );

    # Stop if this is just a debugging run.
    exit(0) if $debug_config;

    # Removing all the marker files has to be done in a separate run, before svscan
    # (that is, supervise) is started, which in turn starts the portal.
    if ($remove_markers) {
	exit(1) if not remove_all_marker_files( @initial_deployments, @secondary_deployments, @tertiary_deployments );
    }
    else {
	exit(1) if ( $xc = wait_until_portal_is_running($query_for_deployments) );
	exit(1)
	  if not perform_deployments(
	    $query_for_deployments, $wait_for_deployments, \@initial_deployments, DEPLOY_IN_SEQUENCE,
	    $initial_deployment_max_cycles,
	    $initial_deployment_cycle_sleep_time
	  );
	exit(1) if ( $xc = connect_to_foundation() );
	exit(1)
	  if not perform_deployments(
	    $query_for_deployments, $wait_for_deployments, \@secondary_deployments, DEPLOY_IN_SEQUENCE,
	    $secondary_deployment_max_cycles,
	    $secondary_deployment_cycle_sleep_time
	  );
	exit(1)
	  if not perform_deployments(
	    $query_for_deployments, $wait_for_deployments, \@tertiary_deployments, DEPLOY_IN_PARALLEL,
	    $tertiary_deployment_max_cycles,
	    $tertiary_deployment_cycle_sleep_time
	  );
    }

    # FIX MINOR:  Old, deprecated code.  Will be removed in the next release,
    # along with supporting config variables above.  Retained here only
    # temporarily, for emergency purposes.
    # exit(1) if ( $xc = connect_to_authentication_host() );
    # exit(1) if ( $xc = connect_to_rtmm() );

    exit(0);
}

END {
    if ( not $query_for_deployments ) {
	my $gwservices_start_wait_time = $portal_start_wait_time + $listener_wait_time + $total_deployments_time;
	print sprintf( "    Total portal startup time:         %7.3f seconds.\n", $portal_start_wait_time )     if $portal_start_wait_time;
	print sprintf( "    Total listener wait time:          %7.3f seconds.\n", $listener_wait_time )         if $listener_wait_time;
	print sprintf( "    Total war-file deployment time:    %7.3f seconds.\n", $total_deployments_time )     if $total_deployments_time;
	print sprintf( "    Total gwservices start-wait time:  %7.3f seconds.\n", $gwservices_start_wait_time ) if $gwservices_start_wait_time;
    }
}

sub read_config_file
{
    my $portal_config_file = shift;
    my $config_debug       = shift;

    eval {
	my $config = TypedConfig->new($portal_config_file, $config_debug);

	$initial_deployment_max_cycles       = $config->get_number('initial_deployment_max_cycles');
	$initial_deployment_cycle_sleep_time = $config->get_number('initial_deployment_cycle_sleep_time');

	$secondary_deployment_max_cycles       = $config->get_number('secondary_deployment_max_cycles');
	$secondary_deployment_cycle_sleep_time = $config->get_number('secondary_deployment_cycle_sleep_time');

	$tertiary_deployment_max_cycles       = $config->get_number('tertiary_deployment_max_cycles');
	$tertiary_deployment_cycle_sleep_time = $config->get_number('tertiary_deployment_cycle_sleep_time');

	@initial_deployments = $config->get_array('initial_deployment');
	print Data::Dumper->Dump( [ \@initial_deployments ], [qw(\@initial_deployments)] ) if $config_debug;

	foreach my $war_path (@initial_deployments) {
	    if ( $war_path !~ m{^$deployment_base_dir_pattern/[^/]+$} ) {
		die "FATAL:  Configured initial war-file $war_path is not in a required directory.\n";
	    }
	}

	@secondary_deployments = $config->get_array('secondary_deployment');
	print Data::Dumper->Dump( [ \@secondary_deployments ], [qw(\@secondary_deployments)] ) if $config_debug;

	foreach my $war_path (@secondary_deployments) {
	    if ( $war_path !~ m{^$deployment_base_dir_pattern/[^/]+$} ) {
		die "FATAL:  Configured secondary war-file $war_path is not in a required directory.\n";
	    }
	}

	@tertiary_deployments = $config->get_array('tertiary_deployment');
	print Data::Dumper->Dump( [ \@tertiary_deployments ], [qw(\@tertiary_deployments)] ) if $config_debug;

	foreach my $war_path (@tertiary_deployments) {
	    if ( $war_path !~ m{^$deployment_base_dir_pattern/[^/]+$} ) {
		die "FATAL:  Configured tertiary war-file $war_path is not in a required directory.\n";
	    }
	}
    };
    if ($@) {
	chomp $@;
	$@ =~ s/^ERROR:\s+//i;
	print "    ERROR:  Cannot read config file $portal_config_file ($@).\n";
	return 0;
    }

    return 1;
}

sub remove_all_marker_files
{
    my @deployments = @_;
    my $marker_file = undef;
    foreach my $war_path (@deployments) {
	if ( $war_path !~ m{^$deployment_base_dir_pattern/[^/]+$} ) {
	    print "    FATAL:  $war_path is not in a required directory.\n";
	    return 0;
	}
	foreach my $extension (@marker_file_extensions_to_remove) {
	    $marker_file = $war_path . $extension;
	    if ( -e $marker_file ) {
		if ( unlink $marker_file ) {
		    print "    NOTICE:  Marker file $marker_file was deleted.\n" if $verbose;
		}
		else {
		    ## We make a final check, and don't object if the marker
		    ## file was instead removed by some other agency between our
		    ## test for its existence and our attempt to remove it.
		    print "    FATAL:  Marker file $marker_file could not be deleted ($!).\n" if -e $marker_file;
		    return 0;
		}
	    }
	}
    }
    return 1;
}

sub deployment_succeeded
{
    my $only_query_status     = shift;
    my $only_wait_until_alive = shift;
    my $war_path              = shift;
    my $max_wait_cycles       = shift;
    my $wait_cycle_sleep_time = shift;

    ( my $war_file = $war_path ) =~ s{.*/}{};

    if ( not $only_query_status ) {
	print $only_wait_until_alive ? "\tWaiting for $war_file deployment:  " : "\tDeploying $war_file:  ";
    }
    for (my $i = 0; $i < ($only_query_status ? 1 : $max_wait_cycles); ++$i) {
	print $i % 10 || '.' if not $only_query_status;
	if ( -e "$war_path.deployed" ) {
	    if (not $only_query_status) {
		if ($verbose) {
		    print "\n\t$war_file has been deployed.\n";
		}
		else {
		    print "\n";
		}
	    }
	    return 1;
	}
	elsif ( -e "$war_path.failed" ) {
	    print "\n" if not $only_query_status;
	    print "\tERROR:  $war_file has failed to deploy.\n";
	    return 0;
	}

	# Sleep briefly before looking again.
	select undef, undef, undef, $wait_cycle_sleep_time if not $only_query_status;
    }

    print $only_query_status ? "\t$war_file is not running.\n" : "\n\tFATAL:  Deployment of $war_file timed out.\n";
    return 0;
}

sub touch_marker_file
{
    my $marker_file = shift;

    # The use of O_EXCL here is not strictly necessary.  It simply reinforces the notion
    # that we were supposed to have removed all marker files before we got here.
    if ( not sysopen( MARKERFILE, $marker_file, O_WRONLY | O_NOFOLLOW | O_EXCL | O_CREAT | O_APPEND, 0600 ) ) {
	print "\tERROR:  Could not create marker file $marker_file ($!).\n";
	return 0;
    }

    close MARKERFILE;
    return 1;
}

sub perform_deployments
{
    my $only_query_status     = shift;
    my $only_wait_until_alive = shift;
    my $deployments           = shift;
    my $sequencing            = shift;
    my $max_wait_cycles       = shift;
    my $wait_cycle_sleep_time = shift;

    my $deployments_start_time = Time::HiRes::time();

    eval {
	print "    Checking application deployments ...\n" if $only_query_status and @$deployments;
	## An error message will have already been printed upon failure,
	## so all we need to do here is to pass the buck by dying.
	if ( $sequencing == DEPLOY_IN_SEQUENCE ) {
	    if (not $only_query_status) {
		print $only_wait_until_alive
		  ? "    Waiting for sequential application deployments:\n"
		  : "    Performing application deployments in sequence:\n";
	    }
	    foreach my $war_path (@$deployments) {
		die if not ($only_query_status or $only_wait_until_alive) and not touch_marker_file("$war_path.dodeploy");
		die if not deployment_succeeded( $only_query_status, $only_wait_until_alive, $war_path, $max_wait_cycles, $wait_cycle_sleep_time );
	    }
	}
	elsif ( $sequencing == DEPLOY_IN_PARALLEL ) {
	    if (not $only_query_status) {
		print $only_wait_until_alive
		  ? "    Waiting for parallel application deployments:\n"
		  : "    Performing application deployments in parallel:\n";
	    }
	    if ( not ($only_query_status or $only_wait_until_alive) ) {
		foreach my $war_path (@$deployments) {
		    die if not touch_marker_file("$war_path.dodeploy");
		}
	    }
	    foreach my $war_path (@$deployments) {
		die if not deployment_succeeded( $only_query_status, $only_wait_until_alive, $war_path, $max_wait_cycles, $wait_cycle_sleep_time );
	    }
	}
	else {
	    print "    FATAL:  Unknown sequencing specified for deployments.\n";
	    die;
	}
    };
    my $failed = $@;
    $total_deployments_time += Time::HiRes::time() - $deployments_start_time;
    return $failed ? 0 : 1;
}

sub parse_line
{
    chomp;

    s/\\#/!_!/g; # allow for inline comments
    s/#.*//;     # and inline data that has an escaped #
    s/!_!/#/g;

    return unless /^\s*([^=[:space:]]+)\s*=\s*(.+)\s*/;

    my $left  = $1;
    my $right = $2;

    $left  =~ s/^\s+//;
    $left  =~ s/\s+$//;

    $right =~ s/^\s+//;
    $right =~ s/\s+$//;

    # Keep "{" pairs balanced here with this comment.
    while ($right =~ /\$\{([^}]+)\}/) {
	my $key = $1;
	if (defined $pair{$key}) {
	    $right =~ s/\$\{$key\}/$pair{$key}/;
	}
	else {
	    print "WARNING:  $key is referenced in the definition of $left but not defined.\n" if $verbose;
	    last;
	}
    }

    $pair{ $left } = $right;    # now we got our factors
}

sub extern_entity {
    print "    Configuration failure:  external entities are not allowed.\n";
    return undef;
}

sub extern_entity_end {
    return undef;
}

# $value = MyXML::Tree2Value( $tree, "s:beans -> jb42:agent -> gatewayLoginUrl" );
# ----------------------------------------------------------------------
# Tree2Value( tree, path )
# ----------------------------------------------------------------------
# Tree2Value takes { $tree = $xml->parsefile( $josso_agent_config ); }
# from XML::Parser module output, and allows it to be traversed to
# retrieve values WITH EASE.  To look for the value associated with
#
# <s:beans>
#   <jb42:agent>
#     <gatewayLoginUrl>
#       alabama.groundworkopensource.groundwork.com
#     </gatewayLoginUrl>
#   </jb42:agent>
# </s:bean>
#
# (which is the "alabama..." URI)
# One needs only call this function with this:
#
# $value = Tree2Value( $tree, "s:beans -> jb42:agent -> GATEWAYlOGINURL" );
#
# NOTE that I've capitalized the last element to show that it is case
# insensitive. (XML tags are supposed to be case insensitive)
# ----------------------------------------------------------------------
# Copyright (C) 2016 by GroundworkOpenSource, Inc.  SF CA   Bob Lynch
# ----------------------------------------------------------------------
sub MyXML::Tree2Value
{
    my $t = shift;
    my $format = shift;

    my @parts = split /\s*[-=]>\s*/, $format;

    # printf "Parts = |%s|\n", join "|", @parts;

    my @things = @{$t};
    my $parti = 0;
    my $thingi = 0;

    for( $thingi = 0; $thingi < @things; $thingi++ )
    {
	my $thing = lc $things[ $thingi ];
	my $part  = lc $parts[ $parti ];

	# print "$thing, $part\n";
	if( $thing eq $part  )
	{
	    $parti++;
	    @things = @{$things[ $thingi + 1 ]};
	    $thingi = 0;
	    next;
	}
	# print "$thing // $parti\n";
	if( $thing eq "0" && $parti == scalar @parts )
	{
	    return $things[ $thingi + 1 ]; # case preserved
	}
    }
    return undef;   # if not found at all ...
}

sub read_configuration
{
    # ----------------------------------------------------------------------
    # In theory, the config-file parsing used here should be replaced
    # with the configuration-file parsing routines popularly authored
    # and lovingly maintained by Mr. Glenn Herteg.  However, the
    # configuration.properties file uses a special form of reference to
    # previously defined option values ("${option.name}"), which I'm not
    # sure that our standard TypedConfig.pm module will handle.  The braces
    # might well confuse it, and some of the substitution values are not
    # even defined in that config file, but are defined instead in various
    # .xml files elsewhere in the system that we don't even parse here.
    # Those missing values don't matter for the options we happen to care
    # about, but might throw a general parser into a fit.
    # ----------------------------------------------------------------------

    # ----------------------------------------------------------------------
    # FIRST, read in the status-viewer properties
    # ----------------------------------------------------------------------

    print "Reading configuration file ('$status_viewer_properties')\n" if $verbose;

    if( ! open PROP, '<', $status_viewer_properties )
    {
	print "... no status viewer properties config file.\n";
	return 1;
    }

    &parse_line() while( <PROP> );

    close PROP;

    # ----------------------------------------------------------------------
    # Now ... the foundation properties (for overrides to status viewer)
    # ----------------------------------------------------------------------

    print "Reading configuration file ('$foundation_properties_file')\n" if $verbose;

    if( ! open PROP, '<', $foundation_properties_file )
    {
	print "... no foundation properties config file (override on cmd-line?).\n";
	return 1;
    }

    &parse_line() while( <PROP> );

    close PROP;

    # ----------------------------------------------------------------------
    # Now ... the configuration properties (for login info)
    # ----------------------------------------------------------------------

    print "Reading configuration file ('$configuration_properties')\n" if $verbose;

    if( ! open PROP, '<', $configuration_properties )
    {
	print "... no configuration properties config file.\n";
	return 1;
    }

    &parse_line() while( <PROP> );

    close PROP;

    # ----------------------------------------------------------------------
    # NOW ... let's see if specific global parameters are to be put into effect
    # ----------------------------------------------------------------------

    my $x;  # to hold, or not to hold the gotten value
    $port                = $x if defined ( $x = $pair{ port } );
    $host                = $x if defined ( $x = $pair{ host } );
    $socket_send_timeout = $x if defined ( $x = $pair{ socket_send_timeout } );
    $get_socket_retries  = $x if defined ( $x = $pair{ get_socket_retries } );
    $sleep_on_no_socket  = $x if defined ( $x = $pair{ sleep_on_no_socket } );

    # THIS SECTION is commented out because it is being replaced with a reader of
    # the file /usr/local/groundwork/config/resources/josso-agent-config.xml
    #
    # # ----------------------------------------------------------------------
    # # PROTECTION against an errant library version messsage.  Harmless but dumb.
    # # ----------------------------------------------------------------------
    #     open( OLDERR, ">&STDERR" );
    #     open( STDERR, ">/dev/null" );
    #     $fqhost = `host $host`;
    #     close STDERR;
    #     open( STDERR, ">&OLDERR" );
    #     close OLDERR;
    # # ----------------------------------------------------------------------
    #
    #     $fqhost = "" unless $fqhost;
    #     $fqhost = "" if $fqhost =~ /(localhost|127.0.0.1)/i;
    #
    #     $fqhost = $fqhost || `hostname`;
    #     $fqhost = $fqhost || `uname -n`;
    #
    #     chomp $fqhost; # just in case
    #     $fqhost =~ s/\s+has\s+.*//;

    my ( $xml, $tree ) ;
    $xml = new XML::Parser( Style => "Tree", Handlers => { ExternEnt => \&extern_entity, ExternEntFin => \&extern_entity_end } );
    eval { 
    	$tree = $xml->parsefile( $josso_agent_config );
    };
    if ($@) {
	$@ =~ s/\n//g;
	print "    ERROR:  An error occurred reading / parsing config file $josso_agent_config ($@).\n";
	return 1;
    }
    # FIX MAJOR:  This code assumes certain old structures which were in place in an earlier release (7.0.0/7.0.1).
    # These structures have changed with the 7.0.2 release, so this code would now throw errors.  Commenting out this
    # logic has no functional effect because the code that uses $fqhost is obsolete, commented out, and will be removed
    # in the next major release.
    # $fqhost = MyXML::Tree2Value( $tree, "s:beans -> jb42:agent -> gatewayLoginUrl" );
    # $fqhost =~ s/^https?:\/\///i;
    # $fqhost =~ s/\/.*//;

    print "Fully Qualified Hostname: '$fqhost'\n" if $verbose;
    return 0;
}

sub wait_until_portal_is_running
{
    my $only_query_status     = shift;

    # ----------------------------------------------------------------------
    # NEXT, let's wait until all Foundation services are really running.
    # ----------------------------------------------------------------------

    print $only_query_status
      ? "    Checking for portal to be completely up and running:\n"
      : "    Waiting for portal to be completely up and running:\n";

    my $portal_startup_start_time = Time::HiRes::time();

    for (my $server_state_loop = 0; $server_state_loop < ($only_query_status ? 1 : $server_state_loop_max); ++$server_state_loop) {
	sleep $sleep_on_state_wait;
	my @cli_lines = qx(. $SETENV_SCRIPT; $JPP_HOME/bin/jboss-cli.sh --commands='connect,:read-attribute(name=server-state)' 2>&1);
	if ($? != 0) {
	    if (@cli_lines) {
		my $errors = join('', @cli_lines);
		if ($errors =~ /The controller is not available/) {
		    print "\tPortal controller is not available ...\n";
		    next;
		}
		chomp $errors;
		print "\tERROR:  Cannot run the jboss-cli.sh script ($errors).\n";
		return 1;
	    }
	}
	my @results = grep /"result" => "\w+"/, @cli_lines;
	if (not @results) {
	    print "\tPortal is inaccessible ...\n";
	    next;
	}
	$results[0] =~ /"result" => "(\w+)"/;
	my $status = $1;
	if ($status eq 'running') {
	    $portal_start_wait_time = Time::HiRes::time() - $portal_startup_start_time;
	    print "\tPortal is " . ( $only_query_status ? "" : "now " ) . "running.\n";
	    return 0;
	}
	elsif ($status eq 'starting') {
	    print "\tPortal is still starting ...\n";
	}
	else {
	    print "\tPortal is perhaps still down ...\n";
	}
    }

    $portal_start_wait_time = Time::HiRes::time() - $portal_startup_start_time;
    print $only_query_status
      ? "\tPortal is not running.\n"
      : "\tWaiting for portal to be running has timed out.\n";
    return 1;
}

sub connect_to_foundation
{
    print $verbose
	? "    Connecting to Foundation (Host:$host Port:$port):\n"
	: "    Connecting to Foundation:\n" ;

    my $listener_wait_start_time = Time::HiRes::time();

    for(my $i = 0; $i < $get_socket_retries; ++$i)
    {
	$socket = IO::Socket::INET->new
       (
	    PeerAddr  => $host,
	    PeerPort  => $port,
	    Proto     => 'tcp',
	    Type      => SOCK_STREAM
	);

	if($socket)
	{
	    $socket->autoflush();
	    my $failed = 0;

	    if( ! $socket->sockopt(SO_SNDTIMEO, pack('L!L!', $socket_send_timeout, 0)))
	    {
		print "\tERROR:  failed to set send timeout on socket\n";
		$failed = 1;
	    }
	    else
	    {
		print "\tListener is ready to accept data feeds ...\n";
		if( ! $socket->print( '<SERVICE-MAINTENANCE command="close" />' ))
		{
		    print "\tERROR:  failed writing to socket\n";
		    $failed = 1;
		}
	    }

	    if( ! close( $socket ))
	    {
		print "\tERROR:  failed closing socket\n";
		$failed = 1;
	    }

	    if( $failed )# from many causes
	    {
		print "\tListener services are not yet available ...\n";
	    }
	    else
	    {
		$listener_wait_time = Time::HiRes::time() - $listener_wait_start_time;
		print "\tListener services are available.\n";
		return 0; # return on SUCCESS
	    }
	}
	else
	{
	    print "\tListener is not ready to accept data feeds ...\n";
	}
	sleep $sleep_on_no_socket;
    }

    $listener_wait_time = Time::HiRes::time() - $listener_wait_start_time;
    print "\tListener is not accepting calls.  System may be still initializing.\n";
    return 1;  # FAIL code
}

sub paired
{
    my $key = shift;
    return "$key=" . $pair{ $key };
}

sub connect_to_authentication_host
{
    # ----------------------------------------------------------------------
    # NEXT, lets test to see if we have ALL the properties we need to do job
    # ----------------------------------------------------------------------
    # The foundation.properties file should have these lines (customized for the site):
    # portal.proxy.user=username                             # Either the "proxy user" alone
    # portal.proxy.password=userpass                         # or ... can be overridden with
    # josso_username=myusername                              # the "josso" usernames, as shown
    # josso_password=myuserpass                              # here.
    # josso_cmd=login                                        # [default], not required
    # josso_url_accesspoint=/josso/signon/login.do           # [default], not required
    # rtmm_url_accesspoint=/portal/classic/status            # [default], not required
    # ----------------------------------------------------------------------

    my $em = "";
    my $x;

    $pair{ "josso_username" } ||= $pair{ "portal.proxy.user" };
    $pair{ "josso_password" } ||= $pair{ "portal.proxy.password" };

    $em .= "    ... Need '$x' in config\n" unless defined $pair{ $x = "josso_username" };
    $em .= "    ... Need '$x' in config\n" unless defined $pair{ $x = "josso_password" };

    $pair{ "josso_cmd" } ||= "login";

    # configuration.properties file, but with the protocol and hostname stripped
    $pair{ "josso_url_accesspoint" } ||= $pair{ "gatein.sso.server.url" };
    $pair{ "josso_url_accesspoint" } ||= "/josso/signon/login.do";

    if ( not defined $pair{ "josso_url_accesspoint" } )
    {
	print "    Cannot connect to authentication host: access point is not defined.\n";
	return 1;
    }
    $pair{ "josso_url_accesspoint" } =~ s{^https?://[^/]+}{}i;

    $pair{ "josso_url_logout" } ||= "/portal/classic/?portal:componentId=UIPortal&portal:action=Logout";

    if( not defined $pair{ "secure.access.enabled" } )
    {
	print "    Cannot authenticate: secure.access.enabled is not defined.\n";
	return 1;
    }

    if ($verbose && $debug) {
	## For development debugging only.
	foreach my $key (sort keys %pair) {
	    print "$key => " . (defined($pair{$key}) ? $pair{$key} : '(is undefined)') . "\n";
	}
    }

    my $josso_url = sprintf "%s//%s%s",
	$pair{ "secure.access.enabled" } eq "true" ? "https:" : "http:",
	$fqhost,
	$pair{ josso_url_accesspoint };

    if( $em )
    {
	print "    Authentication failed: '$em'\n";
	return 1;
    }

    # ----------------------------------------------------------------------
    # NOW, let's see if we can make the connection
    # ----------------------------------------------------------------------

    print "    Trying auto-login to portal:\n";
    $user_agent = LWP::UserAgent->new;

    $user_agent->agent( "check-listener/$version" );
    # ----------------------------------------------------------------------
    # COOKIES code examples typically send 'em to "jars" which are files ...
    # but it turns out that they're also happy "just in the object" (memory)
    # ... and THEY ARE CRITICAL for authentication to propagate to the
    # desired triggering URL.
    # ----------------------------------------------------------------------
    # $user_agent->cookie_jar( HTTP::Cookies->new( file => "lwpcookies.txt", autosave => 1 ));
    $user_agent->cookie_jar( HTTP::Cookies->new( ));

    my $request = HTTP::Request->new( POST => $josso_url );
    $request->content_type('application/x-www-form-urlencoded');
    $request->authorization_basic( $pair{ josso_username }, $pair{ josso_password } );
    $request->content(
	join "&",
	&paired( "josso_cmd" ),
	&paired( "josso_username" ),
	&paired( "josso_password" ),
    );

    # Pass request to the user agent and get a response back
    my $result = $user_agent->request( $request );

    # Check the outcome of the response
    if( $result->is_success )
    {
	# print $result->content;  # for DEBUGGING
	print "\tAuto-login succeeded.\n";
    }
    else
    {
	## FOR DEBUGGING (sort of)
	print "    JOSSO - Connection failed: \"" . $josso_url . "\"\n";
	print "            status line:       \"" . $result->status_line . "\"\n";
	return 1;
    }
    return 0;
}

sub connect_to_rtmm
{
    my $initialized = 0;
    my $target_url;
    my $request;
    my $result;
    my $em = "";

    $pair{ "rtmm_url_accesspoint" } ||= "/portal/classic/status";

    # ----------------------------------------------------------------------
    # NOW, let's see if we can make the connection
    # ----------------------------------------------------------------------

    print "    Initializing Status Viewer Cache:\n";

    if( not defined $pair{ "secure.access.enabled" } )
    {
	print "    Cannot connect to rtmm: secure.access.enabled is not defined.\n";
    }
    else
    {
	$target_url = sprintf "%s//%s%s",
	    ( $pair{ "secure.access.enabled" } eq "true" ) ? "https:" : "http:",
	    $fqhost,
	    $pair{ rtmm_url_accesspoint };

	print "    ... LOCATION DIRECT '$target_url'\n" if $verbose;

	# "5" is specified somewhere online as "reasonable # of redirects",
	# but we actually need more to run out the full list of redirects
	for( my $i = 0; $i < $loop_redirect_max; $i++ )
	{
	    $request = HTTP::Request->new( POST => $target_url );
	    $request->content_type( 'application/x-www-form-urlencoded' );
	    # $request->authorization_basic( $pair{ josso_username }, $pair{ josso_password } );

	    if ($verbose && $debug) {
		my $cookie_jar = $user_agent->cookie_jar();
		$cookie_jar->scan( sub {
		    my $version   = shift;
		    my $key       = shift;
		    my $val       = shift;
		    my $path      = shift;
		    my $domain    = shift;
		    my $port      = shift;
		    my $path_spec = shift;
		    my $secure    = shift;
		    my $expires   = shift;
		    my $discard   = shift;
		    my $hash      = shift;
		    $version   = "(is not defined)" if not defined $version;
		    $key       = "(is not defined)" if not defined $key;
		    $val       = "(is not defined)" if not defined $val;
		    $path      = "(is not defined)" if not defined $path;
		    $domain    = "(is not defined)" if not defined $domain;
		    $port      = "(is not defined)" if not defined $port;
		    $path_spec = "(is not defined)" if not defined $path_spec;
		    $secure    = "(is not defined)" if not defined $secure;
		    $expires   = "(is not defined)" if not defined $expires;
		    $discard   = "(is not defined)" if not defined $discard;
		    $hash      = "(is not defined)" if not defined $hash;
		    print "=== Cookie:\n";
		    print "      version = $version\n";
		    print "          key = $key\n";
		    print "          val = $val\n";
		    print "         path = $path\n";
		    print "       domain = $domain\n";
		    print "         port = $port\n";
		    print "    path_spec = $path_spec\n";
		    print "       secure = $secure\n";
		    print "      expires = $expires\n";
		    print "      discard = $discard\n";
		    if (ref $hash eq 'HASH') {
			foreach my $key (sort keys %$hash) {
			print "         hash{$key} = $hash->{$key}\n";
			}
		    }
		    else {
			print "         hash = $hash\n";
		    }
		} );
	    }

	    $result = $user_agent->request( $request );

	    if( $result->is_success )
	    {
		# print $result->content;  # for DEBUGGING
		print "\tMonitor Status cache is initialized.\n";
		$initialized = 1;
		last;
	    }
	    elsif( 302 == $result->code && $result->as_string =~ /Location:\s+([^\r\n]+)/si )
	    {
		$target_url = $1;
		print "\t... LOCATION REDIRECT '$target_url'\n" if $verbose;
		next;
	    }
	    else
	    {
		if( $verbose )
		{
		    print "\tMonitor Status Cache connection failed: \"" . $result->status_line . "\"\n";
		    print "\t    code        = '", $result->code, "'\n";
		    print "\t    is_info     = '", $result->is_info, "'\n";
		    print "\t    is_redirect = '", $result->is_redirect, "'\n";
		    print "\t    is_error    = '", $result->is_error, "'\n";
		    print "\t    message     = '", $result->message, "'\n";
		}
		last;
	    }
	}
    }

    # Time to log out now, regardless of whether we triggered the cache update.
    $target_url = sprintf "%s//%s%s",
	( $pair{ "secure.access.enabled" } eq "true" ) ? "https:" : "http:",
	$fqhost,
	$pair{ "josso_url_logout" };

    print "    ... LOGOUT LOCATION DIRECT '$target_url'\n" if $verbose;

    for( my $i = 0; $i < $loop_redirect_max; $i++ )
    {
	$request = HTTP::Request->new( POST => $target_url );
	$request->content_type( 'application/x-www-form-urlencoded' );
	$result = $user_agent->request( $request );
	if( $result->is_success )
	{
	    ## We should get back our UI login page, which by itself isn't terribly interesting.
	    ## print $result->content;  # for DEBUGGING
	    print "    Logged out.\n";
	    last;
	}
	elsif( 302 == $result->code && $result->as_string =~ /Location:\s+([^\r\n]+)/si )
	{
	    $target_url = $1;
	    print "    ... LOGOUT LOCATION REDIRECT '$target_url'\n" if $verbose;
	    next;
	}
	else
	{
	    if( $verbose )
	    {
		print "    Logout connection failed: \"" . $result->status_line . "\"\n";
		print "        code        = '", $result->code, "'\n";
		print "        is_info     = '", $result->is_info, "'\n";
		print "        is_redirect = '", $result->is_redirect, "'\n";
		print "        is_error    = '", $result->is_error, "'\n";
		print "        message     = '", $result->message, "'\n";
	    }
	    last;
	}
    }

    if( not $initialized )
    {
	print "    Monitor Status Cache direct connection was not made.\n";
	print "    It will be initialized on first Monitor Status click-through.\n";
	return 1;
    }

    # If we dropped out of the logout loop without logging out, too bad.  There's not
    # much we can do about it, and there's no real reason to visibly complain, either.

    # ----------------
    # must have worked
    # ----------------
    return 0;
}
