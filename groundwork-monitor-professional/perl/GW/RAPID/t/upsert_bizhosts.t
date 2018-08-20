#!/usr/local/groundwork/perl/bin/perl -w --

# This test script tests upsert_bizhosts() with the REST API Perl module for biz/hosts.

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
$logger->debug("----- START upsert_bizhosts() tests ----");
my ( %outcome, %results, @results, $query, $msg, @ids ) = ();

# ----------------------------------------------------------------------------------------------------------------------
# initialize the REST API
my $rest_api = GW::RAPID->new( undef, undef, undef, undef, $requestor, { access => '/usr/local/groundwork/config/ws_client.properties' });

is $rest_api->upsert_bizhosts("arg1"), 0, 'Missing arguments exception';
is( ( not $rest_api->upsert_bizhosts( "arg1", "arg2", \%outcome, "arg4", "arg5" ) and $outcome{response_error} =~ /Invalid number of args/ ), 1, 'Too many arguments exception' );
is( ( not $rest_api->upsert_bizhosts( undef, {}, \%outcome, [] ) and $outcome{response_error} =~ /Undefined arg/ ), 1, 'Undefined argument exception' );
is( ( not $rest_api->upsert_bizhosts( [], undef, \%outcome, [] ) and $outcome{response_error} =~ /Undefined arg/ ), 1, 'Undefined argument exception' );
is $rest_api->upsert_bizhosts( [], {}, undef, [] ), 0, 'Undefined argument exception'; 
is( ( not $rest_api->upsert_bizhosts( [], {}, \%outcome, undef ) and $outcome{response_error} =~ /Undefined arg/ ), 1, 'Undefined argument exception' );
is( ( not $rest_api->upsert_bizhosts( {}, {}, \%outcome, [] ) and $outcome{response_error} =~ /Expecting ARRAY reference/ ), 1, 'Incorrect arguments object type reference exception' );
is( ( not $rest_api->upsert_bizhosts( [], [], \%outcome, [] ) and $outcome{response_error} =~ /Expecting HASH reference/ ), 1, 'Incorrect arguments object type reference exception' );
is $rest_api->upsert_bizhosts( [], {}, [], [] ), 0, 'Incorrect arguments object type reference exception';
is( ( not $rest_api->upsert_bizhosts( [], {}, \%outcome, {} ) and $outcome{response_error} =~ /Expecting ARRAY reference/ ), 1, 'Incorrect arguments object type reference exception' );
is( ( not $rest_api->upsert_bizhosts( "", {}, \%outcome, [] ) and $outcome{response_error} =~ /Expecting ARRAY reference/ ), 1, 'Incorrect arguments object type reference exception' );
is( ( not $rest_api->upsert_bizhosts( [], "", \%outcome, [] ) and $outcome{response_error} =~ /Expecting HASH reference/ ), 1, 'Incorrect arguments object type reference exception' );
is $rest_api->upsert_bizhosts( [], {}, "", [] ), 0, 'Incorrect arguments object type reference exception'; 
is( ( not $rest_api->upsert_bizhosts( [], {}, \%outcome, "" ) and $outcome{response_error} =~ /Expecting ARRAY reference/ ), 1, 'Incorrect arguments object type reference exception' );


# Basic core functionality tests
# ------------------------------

# NOTES 11/24/15
# This test payload to biz/hosts requires that the host already exists. 
# No effort is made today to check that expected state-change events and notifications are generated.
# When this biz/hosts api is fully documented, this test will be formalized. For not it's just enough to
# get the new feeder cluster metrics functionality working.
# GW Docs are required first to properly test this API. Its not yet know which props are required etc for example.

my $host = 'localhost';
my @upsert = (
    {
        "properties" => { },
        "host" => $host,
        #"status" => "UNSCHEDULED DOWN",
        "status" => "UP",
        "message" => "OK",
        "device" => "127.0.0.1",
        "appType" => "NAGIOS",
        "agentId" => "",
        "checkIntervalMinutes" => 5,
        "allowInserts" => 0,
        "mergeHosts" => 0,
        "services" => [ 
            # This bit updates the local_cpu_httpd service to be a warning
		    {
                    "agentId" => "",
      		        "allowInserts" => 0,
                    "appType" => "NAGIOS",
                    "device" => "127.0.0.1",
                    "host" => $host,
      		        "mergeHosts" => 0,
                    "message" => "RAPID test 1 " . localtime  ,
     		        "properties" => { "isMonitored" => 1, "isGraphed" => 0 }, 
                    "service" => "local_cpu_httpd",
                    "status" => "WARNING",
            }, 
            # this bit should add a service called new_service, 
            # and it'll be in a pending state unless setStatusOnCreate = true / 1
		    {
                    "host" => $host,
                    "device" => "127.0.0.1",
                    "service" => "new_service",
                    "appType" => "NAGIOS",
                    "agentId" => "",
                    "status" => "OK",
                    "message" => "RAPID test 2 " . localtime ,
     		        "properties" => { "isMonitored" => 1, "isGraphed" => 0 },
      		        "allowInserts" => 0,
      		        "mergeHosts" => 0,
                    "setStatusOnCreate" => 1
            }, 
		    {
                    "host" => $host,
                    "device" => "127.0.0.1",
                    "service" => "another_new_service",
                    "appType" => "NAGIOS",
                    "agentId" => "",
                    "status" => "OK",
                    "message" => "RAPID test 2 " . localtime ,
     		        "properties" => { "isMonitored" => 1, "isGraphed" => 0 },
      		        "allowInserts" => 0,
      		        "mergeHosts" => 0,
                    "setStatusOnCreate" => 0
            }, 
	    ]
    },
#   {
#       "properties" => { },
#       "host" => $host,
#       "status" => "UP",
#       "message" => "OK",
#       "device" => "127.0.0.1",
#       "appType" => "NAGIOS",
#       "agentId" => "",
#       #"checkIntervalMinutes" => 5,
#       "allowInserts" => 0,
#       "mergeHosts" => 0,
#       "services" => [ 
#	    {
#                   "host" => $host,
#                   "device" => "127.0.0.1",
#                   "service" => "local_load",
#                   "appType" => "NAGIOS",
#                   "agentId" => "",
#                   "status" => "WARNING",
#                   "message" => "RAPID test 1 " . localtime  ,
#    		        "properties" => { "isMonitored" => 1, "isGraphed" => 0 }, 
#     		        "allowInserts" => 0,
#     		        "mergeHosts" => 0,
#           } 
#    ]
#   }
);


# case from actual feeder json but not in use in these tests - just here for ref.
my @upsert2 =  (
 {
            'agentId' => '9f73f3f8-9911-11e5-86e2-df06e170b3c9',
            'allowInserts' => 0,
            'appType' => 'CACTI',
            'checkIntervalMinutes' => 5,
            'device' => 'gw702',
            'host' => 'gw702',
            'mergeHosts' => 0,
            'message' => 'OK',
            'properties' => {},
            'services' => [
                            {
                              'agentId' => '9f73f3f8-9911-11e5-86e2-df06e170b3c9',
                              'allowInserts' => 0,
                              'appType' => 'CACTI',
                              'device' => 'gw702',
                              'host' => 'gw702',
                              'mergeHosts' => 0,
                              'message' => 'health_hostname = gw702 msg = Cycle 1 : Elapsed processing time was 1.93 seconds',
                              'properties' => {
                                                'isGraphed' => 0,
                                                'isMonitored' => 1
                                              },
                              'service' => 'cycle_elapsed_time',
                              'status' => 'OK'
                            },
                            {
                              'agentId' => '9f73f3f8-9911-11e5-86e2-df06e170b3c9',
                              'allowInserts' => 0,
                              'appType' => 'CACTI',
                              'device' => 'gw702',
                              'host' => 'gw702',
                              'mergeHosts' => 0,
                              'message' => 'health_hostname = gw702 msg = Cycle 1 : 0 raw data rows, 0 built thresholds, 0 built rows processed.',
                              'properties' => {
                                                'isGraphed' => 0,
                                                'isMonitored' => 1
                                              },
                              'service' => 'cycle_processed_built_thresholds',
                              'status' => 'OK'
                            }
                          ],
            'status' => 'UP'
          },
          {
            'agentId' => '9f73f3f8-9911-11e5-86e2-df06e170b3c9',
            'allowInserts' => 0,
            'appType' => 'CACTI',
            'checkIntervalMinutes' => 5,
            'device' => 'cfv2',
            'host' => 'cfv2',
            'mergeHosts' => 0,
            'message' => 'OK',
            'properties' => {},
            'services' => [
                            {
                              'agentId' => '9f73f3f8-9911-11e5-86e2-df06e170b3c9',
                              'allowInserts' => 0,
                              'appType' => 'CACTI',
                              'device' => 'cfv2',
                              'host' => 'cfv2',
                              'mergeHosts' => 0,
                              'message' => 'health_hostname = cfv2 msg = Cycle 1 : Elapsed processing time was 1.57 seconds',
                              'properties' => {
                                                'isGraphed' => 0,
                                                'isMonitored' => 1
                                              },
                              'service' => 'cycle_elapsed_time',
                              'status' => 'OK'
                            },
                            {
                              'agentId' => '9f73f3f8-9911-11e5-86e2-df06e170b3c9',
                              'allowInserts' => 0,
                              'appType' => 'CACTI',
                              'device' => 'cfv2',
                              'host' => 'cfv2',
                              'mergeHosts' => 0,
                              'message' => 'health_hostname = cfv2 msg = Cycle 1 : 0 raw data rows, 0 built thresholds, 0 built rows processed.',
                              'properties' => {
                                                'isGraphed' => 0,
                                                'isMonitored' => 1
                                              },
                              'service' => 'cycle_processed_built_thresholds',
                              'status' => 'OK'
                            }
                          ],
            'status' => 'UP'
          }
);

# Negative test case - should fail if host another doesn't exist
my @upsert3 = ( 
{
            'agentId' => '9f73f3f8-9911-11e5-86e2-df06e170b3c9',
            'allowInserts' => 0,
            'appType' => 'CACTI',
            'checkIntervalMinutes' => 5,
            'device' => 'another',
            'host' => 'another',
            'mergeHosts' => 0,
            'message' => 'OK',
            'properties' => {},
            'services' => [
                            {
                              'agentId' => '9f73f3f8-9911-11e5-86e2-df06e170b3c9',
                              'allowInserts' => 0,
                              'appType' => 'CACTI',
                              'checkIntervalMinutes' => 5,
                              'criticalLevel' => -1,
                              'device' => 'another',
                              'host' => 'another',
                              'mergeHosts' => 0,
                              'message' => 'Couldn\'t create feeder object for endpoint \'another\' - updating its retry cache and ending processing attempt. ',
                              'metricType' => 'hypervisor',
                              'properties' => {
                                                'isGraphed' => 0,
                                                'isMonitored' => 1
                                              },
                              'service' => 'cacti_feeder_health',
                              'serviceValue' => 0,
                              'status' => 'UNSCHEDULED CRITICAL',
                              'warningLevel' => -1
                            }
                          ],
            'status' => 'UP'
          },
);

# positive test case  - expects localhost to exist
is $rest_api->upsert_bizhosts( \@upsert, {}, \%outcome, \@results ), 1, "Basic upsert test of two services on localhost";
$logger->debug( Dumper \%outcome, \@results );

# simple negative test case - host doesn't exist and should fail
$host = "doesntexist____" . time();
$upsert[0]->{host} = $host;
$upsert[0]->{services}[0]->{host} = $host;
$upsert[0]->{services}[1]->{host} = $host;
is $rest_api->upsert_bizhosts( \@upsert, {}, \%outcome, \@results ), 0, "Basic upsert negative test of two services on non existent host";
$logger->debug( Dumper \%outcome, \@results );

# More tests need adding once docs are in place for how to use this API

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

