#!/usr/local/groundwork/perl/bin/perl -w --

# This test script tests the REST API Perl module for the customgroup routines.
# Its more convenient to do it all together rather than separate .t files for get/set/clear etc

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
$logger->debug("----- START get_host_identities() tests ----");
my ( %outcome, %results, @results, $query, $msg, @ids ) = ();

# ----------------------------------------------------------------------------------------------------------------------
# initialize the REST API
my $rest_api = GW::RAPID->new( undef, undef, undef, undef, $requestor, { access => '/usr/local/groundwork/config/ws_client.properties' });

is( ( not $rest_api->get_customgroups( "arg1", "arg2", \%outcome, "arg4", "arg5" ) and $outcome{response_error} =~ /Invalid number of args/ ), 1, 'Too many arguments exception' );
is( ( not $rest_api->get_customgroups( undef, {}, \%outcome, {} ) and $outcome{response_error} =~ /Undefined arg/ ), 1, 'Undefined argument exception' );
is( ( not $rest_api->get_customgroups( [], undef, \%outcome, {} ) and $outcome{response_error} =~ /Undefined arg/ ), 1, 'Undefined argument exception' );
is $rest_api->get_customgroups( [], {}, undef, {} ), 0, 'Undefined argument exception'; is( ( not $rest_api->get_hosts( [], {}, \%outcome, undef ) and $outcome{response_error} =~ /Undefined arg/ ), 1, 'Undefined argument exception' );
is( ( not $rest_api->get_customgroups( {}, {}, \%outcome, {} ) and $outcome{response_error} =~ /Expecting ARRAY reference/ ), 1, 'Incorrect object type reference exception' );
is( ( not $rest_api->get_customgroups( [], [], \%outcome, {} ) and $outcome{response_error} =~ /Expecting HASH reference/ ), 1, 'Incorrect object type reference exception' );
is $rest_api->get_customgroups( [], {}, [], {} ), 0, 'Incorrect object type reference exception';
is( ( not $rest_api->get_customgroups( [], {}, \%outcome, [] ) and $outcome{response_error} =~ /Expecting HASH reference/ ), 1, 'Incorrect object type reference exception' );
is( ( not $rest_api->get_customgroups( "", {}, \%outcome, {} ) and $outcome{response_error} =~ /Expecting ARRAY reference/ ), 1, 'Incorrect object type reference exception' );
is( ( not $rest_api->get_customgroups( [], "", \%outcome, {} ) and $outcome{response_error} =~ /Expecting HASH reference/ ), 1, 'Incorrect object type reference exception' );
is $rest_api->get_customgroups( [], {}, "", {} ), 0, 'Incorrect object type reference exception';
is( ( not $rest_api->get_customgroups( [], {}, \%outcome, "" ) and $outcome{response_error} =~ /Expecting HASH reference/ ), 1, 'Incorrect object type reference exception' );

is $rest_api->upsert_customgroups("arg1"), 0, 'Missing arguments exception';
is( ( not $rest_api->upsert_customgroups( "arg1", "arg2", \%outcome, "arg4", "arg5" ) and $outcome{response_error} =~ /Invalid number of args/ ), 1, 'Too many arguments exception' );
is( ( not $rest_api->upsert_customgroups( undef, {}, \%outcome, [] ) and $outcome{response_error} =~ /Undefined arg/ ), 1, 'Undefined argument exception' );
is( ( not $rest_api->upsert_customgroups( [], undef, \%outcome, [] ) and $outcome{response_error} =~ /Undefined arg/ ), 1, 'Undefined argument exception' );
is $rest_api->upsert_customgroups( [], {}, undef, [] ), 0, 'Undefined argument exception'; 
is( ( not $rest_api->upsert_customgroups( [], {}, \%outcome, undef ) and $outcome{response_error} =~ /Undefined arg/ ), 1, 'Undefined argument exception' );
is( ( not $rest_api->upsert_customgroups( {}, {}, \%outcome, [] ) and $outcome{response_error} =~ /Expecting ARRAY reference/ ), 1, 'Incorrect arguments object type reference exception' );
is( ( not $rest_api->upsert_customgroups( [], [], \%outcome, [] ) and $outcome{response_error} =~ /Expecting HASH reference/ ), 1, 'Incorrect arguments object type reference exception' );
is $rest_api->upsert_customgroups( [], {}, [], [] ), 0, 'Incorrect arguments object type reference exception';
is( ( not $rest_api->upsert_customgroups( [], {}, \%outcome, {} ) and $outcome{response_error} =~ /Expecting ARRAY reference/ ), 1, 'Incorrect arguments object type reference exception' );
is( ( not $rest_api->upsert_customgroups( "", {}, \%outcome, [] ) and $outcome{response_error} =~ /Expecting ARRAY reference/ ), 1, 'Incorrect arguments object type reference exception' );
is( ( not $rest_api->upsert_customgroups( [], "", \%outcome, [] ) and $outcome{response_error} =~ /Expecting HASH reference/ ), 1, 'Incorrect arguments object type reference exception' );
is $rest_api->upsert_customgroups( [], {}, "", [] ), 0, 'Incorrect arguments object type reference exception'; 
is( ( not $rest_api->upsert_customgroups( [], {}, \%outcome, "" ) and $outcome{response_error} =~ /Expecting ARRAY reference/ ), 1, 'Incorrect arguments object type reference exception' );

is $rest_api->delete_customgroups("arg1"), 0, 'Missing arguments exception';
is( ( not $rest_api->delete_customgroups( "arg1", "arg2", \%outcome, "arg4", "arg5" ) and $outcome{response_error} =~ /Invalid number of args/ ), 1, 'Too many arguments exception' );
is( ( not $rest_api->delete_customgroups( undef, {}, \%outcome, [] ) and $outcome{response_error} =~ /Undefined arg/ ), 1, 'Undefined argument exception' );
is( ( not $rest_api->delete_customgroups( [], undef, \%outcome, [] ) and $outcome{response_error} =~ /Undefined arg/ ), 1, 'Undefined argument exception' );
is $rest_api->delete_customgroups( [], {}, undef, [] ), 0, 'Undefined argument exception'; is( ( not $rest_api->delete_customgroups( [], {}, \%outcome, undef ) and $outcome{response_error} =~ /Undefined arg/ ), 1, 'Undefined argument exception' );
is( ( not $rest_api->delete_customgroups( {}, {}, \%outcome, [] ) and $outcome{response_error} =~ /Expecting ARRAY reference/ ), 1, 'Incorrect object type reference exception' );
is( ( not $rest_api->delete_customgroups( [], [], \%outcome, [] ) and $outcome{response_error} =~ /Expecting HASH reference/ ), 1, 'Incorrect object type reference exception' );
is $rest_api->delete_customgroups( [], {}, [], [] ), 0, 'Incorrect object type reference exception';
is( ( not $rest_api->delete_customgroups( [], {}, \%outcome, {} ) and $outcome{response_error} =~ /Expecting ARRAY reference/ ), 1, 'Incorrect object type reference exception' );
is( ( not $rest_api->delete_customgroups( "", {}, \%outcome, [] ) and $outcome{response_error} =~ /Expecting ARRAY reference/ ), 1, 'Incorrect object type reference exception' );
is( ( not $rest_api->delete_customgroups( [], "", \%outcome, [] ) and $outcome{response_error} =~ /Expecting HASH reference/ ), 1, 'Incorrect object type reference exception' );
is $rest_api->delete_customgroups( [], {}, "", [] ), 0, 'Incorrect object type reference exception';
is( ( not $rest_api->delete_customgroups( [], {}, \%outcome, "" ) and $outcome{response_error} =~ /Expecting ARRAY reference/ ), 1, 'Incorrect object type reference exception' );

is $rest_api->add_customgroups_members("arg1"), 0, 'Missing arguments exception';
is( ( not $rest_api->add_customgroups_members( "arg1", "arg2", \%outcome, "arg4", "arg5" ) and $outcome{response_error} =~ /Invalid number of args/ ), 1, 'Too many arguments exception' );
is( ( not $rest_api->add_customgroups_members( undef, {}, \%outcome, [] ) and $outcome{response_error} =~ /Undefined arg/ ), 1, 'Undefined argument exception' );
is( ( not $rest_api->add_customgroups_members( [], undef, \%outcome, [] ) and $outcome{response_error} =~ /Undefined arg/ ), 1, 'Undefined argument exception' );
is $rest_api->add_customgroups_members( [], {}, undef, [] ), 0, 'Undefined argument exception'; 
is( ( not $rest_api->add_customgroups_members( [], {}, \%outcome, undef ) and $outcome{response_error} =~ /Undefined arg/ ), 1, 'Undefined argument exception' );
is( ( not $rest_api->add_customgroups_members( {}, {}, \%outcome, [] ) and $outcome{response_error} =~ /Expecting ARRAY reference/ ), 1, 'Incorrect object type reference exception' );
is( ( not $rest_api->add_customgroups_members( [], [], \%outcome, [] ) and $outcome{response_error} =~ /Expecting HASH reference/ ), 1, 'Incorrect object type reference exception' );
is $rest_api->add_customgroups_members( [], {}, [], [] ), 0, 'Incorrect object type reference exception';
is( ( not $rest_api->add_customgroups_members( [], {}, \%outcome, {} ) and $outcome{response_error} =~ /Expecting ARRAY reference/ ), 1, 'Incorrect object type reference exception' );
is( ( not $rest_api->add_customgroups_members( "", {}, \%outcome, [] ) and $outcome{response_error} =~ /Expecting ARRAY reference/ ), 1, 'Incorrect object type reference exception' );
is( ( not $rest_api->add_customgroups_members( [], "", \%outcome, [] ) and $outcome{response_error} =~ /Expecting HASH reference/ ), 1, 'Incorrect object type reference exception' );
is $rest_api->add_customgroups_members( [], {}, "", [] ), 0, 'Incorrect object type reference exception'; is( ( not $rest_api->update_events( [], {}, \%outcome, "" ) and $outcome{response_error} =~ /Expecting ARRAY reference/ ), 1, 'Incorrect object type reference exception' );

is $rest_api->delete_customgroups_members("arg1"), 0, 'Missing arguments exception';
is( ( not $rest_api->delete_customgroups_members( "arg1", "arg2", \%outcome, "arg4", "arg5" ) and $outcome{response_error} =~ /Invalid number of args/ ), 1, 'Too many arguments exception' );
is( ( not $rest_api->delete_customgroups_members( undef, {}, \%outcome, [] ) and $outcome{response_error} =~ /Undefined arg/ ), 1, 'Undefined argument exception' );
is( ( not $rest_api->delete_customgroups_members( [], undef, \%outcome, [] ) and $outcome{response_error} =~ /Undefined arg/ ), 1, 'Undefined argument exception' );
is $rest_api->delete_customgroups_members( [], {}, undef, [] ), 0, 'Undefined argument exception'; 
is( ( not $rest_api->delete_customgroups_members( [], {}, \%outcome, undef ) and $outcome{response_error} =~ /Undefined arg/ ), 1, 'Undefined argument exception' );
is( ( not $rest_api->delete_customgroups_members( {}, {}, \%outcome, [] ) and $outcome{response_error} =~ /Expecting ARRAY reference/ ), 1, 'Incorrect object type reference exception' );
is( ( not $rest_api->delete_customgroups_members( [], [], \%outcome, [] ) and $outcome{response_error} =~ /Expecting HASH reference/ ), 1, 'Incorrect object type reference exception' );
is $rest_api->delete_customgroups_members( [], {}, [], [] ), 0, 'Incorrect object type reference exception';
is( ( not $rest_api->delete_customgroups_members( [], {}, \%outcome, {} ) and $outcome{response_error} =~ /Expecting ARRAY reference/ ), 1, 'Incorrect object type reference exception' );
is( ( not $rest_api->delete_customgroups_members( "", {}, \%outcome, [] ) and $outcome{response_error} =~ /Expecting ARRAY reference/ ), 1, 'Incorrect object type reference exception' );
is( ( not $rest_api->delete_customgroups_members( [], "", \%outcome, [] ) and $outcome{response_error} =~ /Expecting HASH reference/ ), 1, 'Incorrect object type reference exception' );
is $rest_api->delete_customgroups_members( [], {}, "", [] ), 0, 'Incorrect object type reference exception'; is( ( not $rest_api->update_events( [], {}, \%outcome, "" ) and $outcome{response_error} =~ /Expecting ARRAY reference/ ), 1, 'Incorrect object type reference exception' );


# Basic core functionality tests
# ------------------------------
# TBD could do more testing but the basics are covered for now - improve as needed.

my @upsert = (
       			#{
    			#    "name" => "TEST-CUSTOM-GROUP-0",
    			#    "description" => "TEST-CUSTOM-GROUP-0",
    			#    "appType" => "NAGIOS",
    			#    "agentId" => "TEST-AGENT",
    			#    "hostGroupNames" => [ "TEST-HOST-GROUP-0" ]
  		        #  }, 
		        #  {
    			#    "name" => "TEST-CUSTOM-GROUP-1",
    			#    "agentId" => "TEST-AGENT",
    			#    "serviceGroupNames" => [ "somecat" ]
  	  	        #  }, 
		          {
    			    "name" => "group1",
    			    "appType" => "NAGIOS"
  			  }, 
			  {
    			    "name" => "group3"
  			  } 
);

is $rest_api->upsert_customgroups(  \@upsert, {}, \%outcome, \@results ), 1, "Basic upsert custom groups";
$logger->debug( Dumper \%outcome, \@results );

# Need to create hg first 
assign_to_hostgroup(  "hg", [ "localhost" ] );

my @members =  (
	{
  		"name" => "group1",
  		"hostGroupNames" => [ "hg" ]
	}
);

is $rest_api->add_customgroups_members( \@members, {}, \%outcome, \@results ), 1, "Basic adding of hostgroup members";
$logger->debug( Dumper \%outcome, \@results );

is $rest_api->get_customgroups( [ ], {}, \%outcome, \%results ), 1, "Basic getting of all customgroups";
$logger->debug( Dumper \%outcome, \%results );

is $rest_api->get_customgroups( [ 'group1', 'group2', 'group3' ], {}, \%outcome, \%results ), 1, "Basic a list of customgroups"; # group2 doesn't exist but this should still work
$logger->debug( Dumper \%outcome, \%results );


is $rest_api->delete_customgroups_members( \@members, {}, \%outcome, \@results ), 1, "Basic deletion of hostgroup members";
$logger->debug( Dumper \%outcome, \@results );

#$rest_api->get_customgroups( [ ], {}, \%outcome, \%results ); $logger->debug( Dumper \%outcome, \%results ); done();

$rest_api->get_customgroups_autocomplete( [ "group" ]  , {},  \%outcome, \%results )  ; $logger->debug( Dumper \%outcome, \%results, scalar @{$results{'unknown customgroup'}{'names'}} ); #done(); exit;


is ( ($rest_api->get_customgroups_autocomplete( [ "group" ]  , {},  \%outcome, \%results )   and  scalar @{$results{'unknown customgroup'}{'names'}} == 2 ) , 1, "Basic autocomplete") ;
$logger->debug( Dumper \%outcome, \%results );
#done();

is $rest_api->delete_customgroups( [ "group1", "group3" ], {}, \%outcome, \@results ), 1, "Basic deletion of custom groups";
$logger->debug( Dumper \%outcome, \@results );


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

