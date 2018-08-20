#!/usr/local/groundwork/perl/bin/perl -w --

# This test script tests the REST API Perl module upsert_services() routine

die "$0: Please set PGPASSWORD in the environment before running this test script.\n" if not $ENV{'PGPASSWORD'};
my $PSQL = "/usr/local/groundwork/postgresql/bin/psql";    # change if necessary

use strict;
use warnings;

use GW::RAPID;
use Test::More;
use Test::Deep;
use Test::Exception;
use Data::Dumper;
use File::Basename; my $requestor = "RAPID-" . basename($0);

use lib '/usr/local/groundwork/core/foundation/api/perl/lib';
use Log::Log4perl qw(get_logger);

Log::Log4perl::init('GW_RAPID.log4perl.conf');

my $logger = get_logger("GW.RAPID.module");
$logger->debug("----- START upsert_services() tests ----");
my ( @services, %outcome, @results ) = ();

# ----------------------------------------------------------------------------------------------------------------------
# initialize the REST API
my $rest_api = GW::RAPID->new( undef, undef, undef, undef, $requestor, { access => '/usr/local/groundwork/config/ws_client.properties' });

# Exception testing

is $rest_api->upsert_services("arg1"), 0, 'Missing arguments exception';
is( ( not $rest_api->upsert_services( "arg1", "arg2", \%outcome, "arg4", "arg5" ) and $outcome{response_error} =~ /Invalid number of args/ ),
    1, 'Too many arguments exception' );

is( ( not $rest_api->upsert_services( undef, {}, \%outcome, [] ) and $outcome{response_error} =~ /Undefined arg/ ),
    1, 'Undefined argument exception' );
is( ( not $rest_api->upsert_services( [], undef, \%outcome, [] ) and $outcome{response_error} =~ /Undefined arg/ ),
    1, 'Undefined argument exception' );
is $rest_api->upsert_services( [], {}, undef, [] ), 0, 'Undefined argument exception';
is( ( not $rest_api->upsert_services( [], {}, \%outcome, undef ) and $outcome{response_error} =~ /Undefined arg/ ),
    1, 'Undefined argument exception' );

is( ( not $rest_api->upsert_services( {}, {}, \%outcome, [] ) and $outcome{response_error} =~ /Expecting ARRAY reference/ ),
    1, 'Incorrect object type reference exception' );
is( ( not $rest_api->upsert_services( [], [], \%outcome, [] ) and $outcome{response_error} =~ /Expecting HASH reference/ ),
    1, 'Incorrect object type reference exception' );
is $rest_api->upsert_services( [], {}, [], [] ), 0, 'Incorrect object type reference exception';
is( ( not $rest_api->upsert_services( [], {}, \%outcome, {} ) and $outcome{response_error} =~ /Expecting ARRAY reference/ ),
    1, 'Incorrect object type reference exception' );

is( ( not $rest_api->upsert_services( "", {}, \%outcome, [] ) and $outcome{response_error} =~ /Expecting ARRAY reference/ ),
    1, 'Incorrect object type reference exception' );
is( ( not $rest_api->upsert_services( [], "", \%outcome, [] ) and $outcome{response_error} =~ /Expecting HASH reference/ ),
    1, 'Incorrect object type reference exception' );
is $rest_api->upsert_services( [], {}, "", [] ), 0, 'Incorrect object type reference exception';
is( ( not $rest_api->upsert_services( [], {}, \%outcome, "" ) and $outcome{response_error} =~ /Expecting ARRAY reference/ ),
    1, 'Incorrect object type reference exception' );

# ------------------------------------------------------------------------------------
# Simple positive test case  - creates a service on a host
# ------------------------------------------------------------------------------------
# Assumes device id for localhost is 127.0.0.1
my $servicename_base = "__RAPID_test_service__";
my $servicename      = $servicename_base . time();

# delete any test hosts services first
my $delete_command =
"$PSQL -c \"delete from servicestatus where servicedescription like '${servicename_base}%' and hostid = ( select hostid from host where hostname = 'localhost');\" gwcollagedb;";
print "Removing collage test services ${servicename_base}* from localhost with '$delete_command'\n";
if ( system($delete_command ) >> 8 ) { print "Command '$delete_command' failed!! Quitting\n"; exit; }
@services = (
    {
	'lastCheckTime'        => '2013-05-22T09:36:47-07:00',
	'deviceIdentification' => '127.0.0.1',                   # localhost
	'nextCheckTime'        => '2013-05-22T09:46:47-07:00',
	'lastHardState'        => 'PENDING',
	'monitorStatus'        => 'OK',
	'description'          => $servicename,
	'properties' =>
	  { 'Latency' => '950', 'ExecutionTime' => '7', 'MaxAttempts' => '3', 'LastPluginOutput' => 'ORIGINAL output from test service' },
	'stateType'       => 'HARD',
	'hostName'        => 'localhost',
	'appType'         => 'NAGIOS',
	'monitorServer'   => 'localhost',
	'checkType'       => 'ACTIVE',
	'lastStateChange' => '2013-05-22T09:36:47-07:00'
    }
);

# create a test service attached to localhost
is $rest_api->upsert_services( \@services, {}, \%outcome, \@results ), 1, "Successfully created service $servicename on host localhost";

# ------------------------------------------------------------------------------------
# update that freshly created test service on localhost
# ------------------------------------------------------------------------------------
@services = (
    {
	'lastCheckTime'        => '2013-05-22T09:36:47-07:00',
	'deviceIdentification' => '127.0.0.1',                   # localhost
	'nextCheckTime'        => '2013-05-22T09:46:47-07:00',
	'lastHardState'        => 'PENDING',
	'monitorStatus'        => 'OK',
	'description'          => $servicename,
	'properties'           => {
	    'Latency'          => '950',
	    'ExecutionTime'    => '7',
	    'MaxAttempts'      => '3',
	    'LastPluginOutput' => 'UPDATED output from test service'    # << UPDATE
	},
	'stateType'       => 'HARD',
	'hostName'        => 'localhost',
	'appType'         => 'NAGIOS',
	'monitorServer'   => 'localhost',
	'checkType'       => 'ACTIVE',
	'lastStateChange' => '2013-05-22T09:36:47-07:00'
    }
);
is $rest_api->upsert_services( \@services, {}, \%outcome, \@results ), 1, "Successfully updated service description for service $servicename on host localhost";

# ------------------------------------------------------------------------------------
# try to create a service using malformed instructions - missing required property test
@services = (
    {
	'lastCheckTime'        => '2013-05-22T09:36:47-07:00',
	'deviceIdentification' => '127.0.0.1',                   # localhost
	'nextCheckTime'        => '2013-05-22T09:46:47-07:00',
	'lastHardState'        => 'PENDING',
	'monitorStatus'        => 'OK',
	## 'description' => $servicename, # <<< OMISSION
	'properties' => {
	    'Latency'          => '950',
	    'ExecutionTime'    => '7',
	    'MaxAttempts'      => '3',
	    'LastPluginOutput' => 'SHOULD NOT SEE THIS output from test service'
	},
	'stateType'       => 'HARD',
	'hostName'        => 'localhost',
	'appType'         => 'NAGIOS',
	'monitorServer'   => 'localhost',
	'checkType'       => 'ACTIVE',
	'lastStateChange' => '2013-05-22T09:36:47-07:00'
    }
);

is $rest_api->upsert_services( \@services, {}, \%outcome, \@results ), 0, "Failed to create service due to missing required property 'description'";

# Verify that logout (that is, destroying any server-side authentication tokens) works as planned.
is $rest_api->DESTROY(), 1, 'Logout';

# Now have Perl itself call the destructor.  This will call $rest_api->DESTROY() a second time.
$rest_api = undef;

# ------------------------------------------------------------------------------------
$logger->debug("----- END upsert_services() tests ----");
done_testing();

