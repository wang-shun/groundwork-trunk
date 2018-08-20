#!/usr/local/groundwork/perl/bin/perl -w --

# This test script tests the REST API Perl module delete_services() routine

die "$0: Please set PGPASSWORD in the environment before running this test script.\n" if not $ENV{'PGPASSWORD'};
my $PSQL = "/usr/local/groundwork/postgresql/bin/psql";    # change if necessary

use strict;
use warnings;

use GW::RAPID;
use Test::More;
use Test::Deep;
use Test::Exception;
use Data::Dumper; $Data::Dumper::Indent   = 1; $Data::Dumper::Sortkeys = 1;
use File::Basename; my $requestor = "RAPID-" . basename($0);

use lib '/usr/local/groundwork/core/foundation/api/perl/lib';
use Log::Log4perl qw(get_logger);

Log::Log4perl::init('GW_RAPID.log4perl.conf');

my $logger = get_logger("GW.RAPID.module");
$logger->debug("----- START delete_services() tests ----");
my ( @services, %outcome, %results, @results, %instructions, %hosts, @hosts, $hostname ) = ();

# ----------------------------------------------------------------------------------------------------------------------
# initialize the REST API
my $rest_api = GW::RAPID->new( undef, undef, undef, undef, $requestor, { access => '/usr/local/groundwork/config/ws_client.properties' });
$hostname = "abc";

# Exception testing

is $rest_api->delete_services("arg1"), 0, 'Missing arguments exception';
is( ( not $rest_api->delete_services( "arg1", "arg2", \%outcome, "arg4", "arg5" ) and $outcome{response_error} =~ /Invalid number of args/ ),
    1, 'Too many arguments exception' );

is( ( not $rest_api->delete_services( undef, {}, \%outcome, [] ) and $outcome{response_error} =~ /Undefined arg/ ),
    1, 'Undefined argument exception' );
is( ( not $rest_api->delete_services( [], undef, \%outcome, [] ) and $outcome{response_error} =~ /Undefined arg/ ),
    1, 'Undefined argument exception' );
is $rest_api->delete_services( [], {}, undef, [] ), 0, 'Undefined argument exception';
is( ( not $rest_api->delete_services( [], {}, \%outcome, undef ) and $outcome{response_error} =~ /Undefined arg/ ),
    1, 'Undefined argument exception' );

is( ( not $rest_api->delete_services( {}, {}, \%outcome, [] ) and $outcome{response_error} =~ /Expecting ARRAY reference/ ),
    1, 'Incorrect object type reference exception' );
is( ( not $rest_api->delete_services( [], [], \%outcome, [] ) and $outcome{response_error} =~ /Expecting HASH reference/ ),
    1, 'Incorrect object type reference exception' );
is $rest_api->delete_services( [], {}, [], [] ), 0, 'Incorrect object type reference exception';
is( ( not $rest_api->delete_services( [], {}, \%outcome, {} ) and $outcome{response_error} =~ /Expecting ARRAY reference/ ),
    1, 'Incorrect object type reference exception' );

is( ( not $rest_api->delete_services( "", {}, \%outcome, [] ) and $outcome{response_error} =~ /Expecting ARRAY reference/ ),
    1, 'Incorrect object type reference exception' );
is( ( not $rest_api->delete_services( [], "", \%outcome, [] ) and $outcome{response_error} =~ /Expecting HASH reference/ ),
    1, 'Incorrect object type reference exception' );
is $rest_api->delete_services( [], {}, "", [] ), 0, 'Incorrect object type reference exception';
is( ( not $rest_api->delete_services( [], {}, \%outcome, "" ) and $outcome{response_error} =~ /Expecting ARRAY reference/ ),
    1, 'Incorrect object type reference exception' );

# ------------------------------------------------------------------------------------
# Simple positive test case  - creates some services and then deletes them
# ------------------------------------------------------------------------------------

# Assumes device id for localhost is 127.0.0.1
my $servicename_base = "__RAPID_test_service__";
my $servicename      = $servicename_base . time();

# delete any test hosts first - won't need this if test works, but in case of test failing ...
my $delete_command =
"$PSQL -c \"delete from servicestatus where servicedescription like '${servicename_base}%' and hostid = ( select hostid from host where hostname = 'localhost');\" gwcollagedb;";
print "Removing collage test services ${servicename_base}* from localhost with '$delete_command'\n";
if ( system($delete_command ) >> 8 ) { print "Command '$delete_command' failed!! Quitting\n"; exit; }
@services = (
    {
	'deviceIdentification' => '127.0.0.1',                   # localhost
	'hostName'             => 'localhost',
	'description'          => $servicename,
	'appType'              => 'NAGIOS',
	'stateType'            => 'HARD',
	'monitorServer'        => 'localhost',
	'checkType'            => 'ACTIVE',
	'lastHardState'        => 'PENDING',
	'monitorStatus'        => 'OK',
	'lastCheckTime'        => '2013-05-22T09:36:47-07:00',
	'nextCheckTime'        => '2013-05-22T09:46:47-07:00',
	'lastStateChange'      => '2013-05-22T09:36:47-07:00',
	'properties'           => {
	    'Latency'          => '950',
	    'ExecutionTime'    => '7',
	    'MaxAttempts'      => '3',
	    'LastPluginOutput' => 'ORIGINAL output from test service'
	}
    },
    {
	'deviceIdentification' => '127.0.0.1',                   # localhost
	'hostName'             => 'localhost',
	'description'          => $servicename . "_two",
	'appType'              => 'NAGIOS',
	'stateType'            => 'HARD',
	'monitorServer'        => 'localhost',
	'checkType'            => 'ACTIVE',
	'lastHardState'        => 'PENDING',
	'monitorStatus'        => 'OK',
	'lastCheckTime'        => '2013-05-22T09:36:47-07:00',
	'nextCheckTime'        => '2013-05-22T09:46:47-07:00',
	'lastStateChange'      => '2013-05-22T09:36:47-07:00',
	'properties'           => {
	    'Latency'          => '950',
	    'ExecutionTime'    => '7',
	    'MaxAttempts'      => '3',
	    'LastPluginOutput' => 'ORIGINAL output from test service'
	}
    }
);

# create test services attached to localhost
is $rest_api->upsert_services( \@services, {}, \%outcome, \@results ), 1, "Successfully created service $servicename on host localhost";

# get the servicenames and hostnames of the test services
is $rest_api->get_services( [], { query => "description like '$servicename%'" }, \%outcome, \%results ), 1,
  "Successfully retrieved test services - identification: " . join( ", ", map { $results{$_}{description} } keys %results );
my @servicenames = ();
my @hostnames    = ();
foreach my $serviceid ( keys %results ) {
    push @servicenames, $results{$serviceid}{description};
    push @hostnames,    $results{$serviceid}{hostName};
}
$logger->debug( "\\\%outcome:\n", Dumper \%outcome );
$logger->debug( "\\\%results:\n", Dumper \%results );
$logger->debug("servicenames: @servicenames");
$logger->debug("hostnames: @hostnames");

# delete the services
is $rest_api->delete_services( \@servicenames, { hostname => \@hostnames }, \%outcome, \@results ), 1,
  "Successfully deleted these services from localhost: @servicenames";
$logger->debug( "\\\%outcome:\n", Dumper \%outcome );
$logger->debug( "\\\%results:\n", Dumper \@results );

# delete the now non-existent services
is $rest_api->delete_services( \@servicenames, { hostname => \@hostnames }, \%outcome, \@results ), 0,
  "Failure deleting already-removed non existent services in list: @servicenames";

# FIX MAJOR:  Is it possible to use service IDs instead of servicename/hostname pairs, to delete services?

# Verify that logout (that is, destroying any server-side authentication tokens) works as planned.
is $rest_api->DESTROY(), 1, 'Logout';

# Now have Perl itself call the destructor.  This will call $rest_api->DESTROY() a second time.
$rest_api = undef;

# ------------------------------------------------------------------------------------
$logger->debug("----- END delete_services() tests ----");
done_testing();

