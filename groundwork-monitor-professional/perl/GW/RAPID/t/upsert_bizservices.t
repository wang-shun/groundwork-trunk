#!/usr/local/groundwork/perl/bin/perl -w --

# This test script tests upsert_bizservices() with the REST API Perl module for biz/hosts.

use strict;

# FIX MAJOR:  This is one form of workaround for the underscore-to-dash translation issue.
# It should be fixed instead by renaming the APP_NAME and GWOS_API_TOKEN header names.
use HTTP::Headers; $HTTP::Headers::TRANSLATE_UNDERSCORE = 0;
use GW::RAPID;
use Test::More;
use Test::Deep;
use Test::Exception;
use JSON;
use Data::Dumper; $Data::Dumper::Indent   = 1; $Data::Dumper::Sortkeys = 1;
use File::Basename; my $requestor = "RAPID-" . basename($0);
use Log::Log4perl qw(get_logger);
Log::Log4perl::init('GW_RAPID.log4perl.conf');
my $logger = get_logger("GW.RAPID.module");
$logger->debug("----- START upsert_bizservices() tests ----");
my ( %outcome, %results, @results, $query, $msg, @ids ) = ();

# ----------------------------------------------------------------------------------------------------------------------
# initialize the REST API
my $rest_api = GW::RAPID->new( undef, undef, undef, undef, $requestor, { access => '/usr/local/groundwork/config/ws_client.properties' });

is $rest_api->upsert_bizservices("arg1"), 0, 'Missing arguments exception';
is( ( not $rest_api->upsert_bizservices( "arg1", "arg2", \%outcome, "arg4", "arg5" ) and $outcome{response_error} =~ /Invalid number of args/ ), 1, 'Too many arguments exception' );
is( ( not $rest_api->upsert_bizservices( undef, {}, \%outcome, [] ) and $outcome{response_error} =~ /Undefined arg/ ), 1, 'Undefined argument exception' );
is( ( not $rest_api->upsert_bizservices( [], undef, \%outcome, [] ) and $outcome{response_error} =~ /Undefined arg/ ), 1, 'Undefined argument exception' );
is $rest_api->upsert_bizservices( [], {}, undef, [] ), 0, 'Undefined argument exception'; 
is( ( not $rest_api->upsert_bizservices( [], {}, \%outcome, undef ) and $outcome{response_error} =~ /Undefined arg/ ), 1, 'Undefined argument exception' );
is( ( not $rest_api->upsert_bizservices( {}, {}, \%outcome, [] ) and $outcome{response_error} =~ /Expecting ARRAY reference/ ), 1, 'Incorrect arguments object type reference exception' );
is( ( not $rest_api->upsert_bizservices( [], [], \%outcome, [] ) and $outcome{response_error} =~ /Expecting HASH reference/ ), 1, 'Incorrect arguments object type reference exception' );
is $rest_api->upsert_bizservices( [], {}, [], [] ), 0, 'Incorrect arguments object type reference exception';
is( ( not $rest_api->upsert_bizservices( [], {}, \%outcome, {} ) and $outcome{response_error} =~ /Expecting ARRAY reference/ ), 1, 'Incorrect arguments object type reference exception' );
is( ( not $rest_api->upsert_bizservices( "", {}, \%outcome, [] ) and $outcome{response_error} =~ /Expecting ARRAY reference/ ), 1, 'Incorrect arguments object type reference exception' );
is( ( not $rest_api->upsert_bizservices( [], "", \%outcome, [] ) and $outcome{response_error} =~ /Expecting HASH reference/ ), 1, 'Incorrect arguments object type reference exception' );
is $rest_api->upsert_bizservices( [], {}, "", [] ), 0, 'Incorrect arguments object type reference exception'; 
is( ( not $rest_api->upsert_bizservices( [], {}, \%outcome, "" ) and $outcome{response_error} =~ /Expecting ARRAY reference/ ), 1, 'Incorrect arguments object type reference exception' );


# Basic core functionality tests
# ------------------------------

# NOTES 1/15/16
# When this biz/services api is fully documented, this test will be formalized. For not it's just enough to
# get the new feeder cluster metrics functionality working.

my $host = 'anewhost3';
my $hg = "anewhg3";
my @upsert = (
	{
		"host" => $host,
		"hostGroup" => $hg,
		"device" => $host,
		"service" => "svc1",
		#"serviceGroup" => "cpu", # optional
		"appType" => "NAGIOS",
		"agentId" => "a4ca80e0-e855-43ee-86d8-f4e499f0a824",
		"status" => "OK",
		"message" => "all ok",
		#"allowInserts" => 1,
		#"mergeHosts" => 0,
		#"metricType" => "vm",
		#"checkIntervalMinutes" => 2
	}, {
		"host" => $host,
		"hostGroup" => $hg,
		"device" => $host,
		"service" => "svc2",
		#"serviceGroup" => "cpu", # optional
		"appType" => "NAGIOS",
		"agentId" => "a4ca80e0-e855-43ee-86d8-f4e499f0a824",
		"status" => "OK",
		"message" => "ok - new",
		#"allowInserts" => 1,
		#"mergeHosts" => 0,
		#"metricType" => "vm",
		#"checkIntervalMinutes" => 2
	}
);

is $rest_api->upsert_bizservices( \@upsert, {}, \%outcome, \@results ), 1, "Basic upsert test of adding new host with new services to new hostgroup";
$logger->error( Dumper \%outcome, \@results );

# ========
done();
# ========

# ----------------------------------------------------------------------------------------------------------------------
sub delete_test_hosts
{
	my ( $hosts_ref ) = @_;
 	if ( not $rest_api->delete_hosts( $hosts_ref, {}, \%outcome, \@results ) ) {
		$logger->error("Failed to delete hosts : " . Dumper \%outcome, \@results );
		return 0;
	}
	return 1;
}
# ----------------------------------------------------------------------------------------------------------------------
sub create_test_hosts
{
	# creates some test hosts
	my ( $hosts_ref ) = @_;
	my ( @hosts ) ;
	foreach  my $host  ( @{$hosts_ref} ) {
	    push @hosts,
		{
			"hostName"             => $host,
			"description"          => "CREATED at " . localtime,
			"monitorStatus"        => "UP",
			"appType"              => "NAGIOS",
			"deviceIdentification" => $host,
			"monitorServer"        => "localhost",
			"deviceDisplayName"    => $host,
			"properties"           => { "Latency" => "125", "UpdatedBy" => "admin", "Comments" => "Blacklists testing host" }
		};
	    }
    
	    if ( not $rest_api->upsert_hosts(  \@hosts, {}, \%outcome, \@results )  ) {
	       $logger->error("Failed to build test hosts : " . Dumper \%outcome, \@results);
	       return 0;
	    }

    return 1;

}

# ----------------------------------------------------------------------------------------------------------------------
sub clear
{
	system("clear");
	print "=" x 80 . "\n";
}

# ----------------------------------------------------------------------------------------------------------------------
sub show_and_die 
{
	my ( $dontdie ) = @_;
	#clear(); 
	print "=" x 80 . "\n";
	$rest_api->get_hostidentities( [ ] , { }, \%outcome, \%results ); 
	print "GET ALL : " . Dumper \%outcome, \%results; 
	if ( not defined $dontdie )  {
		done_testing();
		$rest_api = undef;
 		exit;
	}
}

# ----------------------------------------------------------------------------------------------------------------------
sub array_contains
{
	my ( $array_ref, $search_item ) = @_;
	( grep {$_ eq $search_item} @{$array_ref} ) ? return 1: return 0;
}

# ----------------------------------------------------------------------------------------------------------------------
sub assign_to_hostgroup
{
	my ( $hostgroup_name, $hosts_array_ref ) = @_;

	# create the hostgroup and put the hosts in it
	my @hostgroups = (
    		{
			"name"        => $hostgroup_name,
			"description" => "CREATED at " . localtime,
			"alias"       => "Alias for $hostgroup_name",
			"hosts"       => $hosts_array_ref
    		}
	);

	if ( not $rest_api->upsert_hostgroups( \@hostgroups, {}, \%outcome, \@results ) ) {
	       $logger->error("Failed to build test hostgroup and assign hosts : " . Dumper \%outcome, \@results);
		return 0;
        }

	return 1;
}

sub attach_services_to_a_host
{
	# tries to attach service_1 and service_2 to $host
	my ( $host ) = @_;
	my @services = (
    	   {
		'lastCheckTime'        => '2013-05-22T09:36:47-07:00',
		'deviceIdentification' => $host,
		'nextCheckTime'        => '2013-05-22T09:46:47-07:00',
		'lastHardState'        => 'PENDING',
		'monitorStatus'        => 'OK',
		'description'          => "service_1",
		'properties' => { 'Latency' => '950', 'ExecutionTime' => '7', 'MaxAttempts' => '3', 'LastPluginOutput' => 'ORIGINAL output from test service' },
		'stateType'       => 'HARD',
		'hostName'        => $host,
		'appType'         => 'NAGIOS',
		'monitorServer'   => 'localhost',
		'checkType'       => 'ACTIVE',
		'lastStateChange' => '2013-05-22T09:36:47-07:00'
    	   },
    	   {
		'lastCheckTime'        => '2013-05-22T09:36:47-07:00',
		'deviceIdentification' => $host,
		'nextCheckTime'        => '2013-05-22T09:46:47-07:00',
		'lastHardState'        => 'PENDING',
		'monitorStatus'        => 'OK',
		'description'          => "service_2",
		'properties' => { 'Latency' => '950', 'ExecutionTime' => '7', 'MaxAttempts' => '3', 'LastPluginOutput' => 'ORIGINAL output from test service' },
		'stateType'       => 'HARD',
		'hostName'        => $host,
		'appType'         => 'NAGIOS',
		'monitorServer'   => 'localhost',
		'checkType'       => 'ACTIVE',
		'lastStateChange' => '2013-05-22T09:36:47-07:00'
    	   },
	);
	
	if ( not  $rest_api->upsert_services( \@services, {}, \%outcome, \@results ) ) { 
		$logger->error("Failed to attach service to host : " . Dumper \%outcome, \@results );
		return 0;
	}

	return 1;
}


# ----------------------------------------------------------------------------------------------------------------------
sub done
{
	$logger->debug("----- END $0 tests ----"); 
	done_testing(); 
	$rest_api = undef; exit;
}

