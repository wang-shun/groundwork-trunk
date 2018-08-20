#!/usr/local/groundwork/perl/bin/perl -w --

# This test script tests the REST API Perl module create_performance_data() routine

use warnings;
use strict;

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
$logger->debug("----- START create_performance_data() tests ----");
my ( %outcome, %results, @results ) = ();

# ----------------------------------------------------------------------------------------------------------------------
# initialize the REST API
my $rest_api = GW::RAPID->new( undef, undef, undef, undef, $requestor, { access => '/usr/local/groundwork/config/ws_client.properties' });

# Exception testing
is $rest_api->create_performance_data(  "arg1" ), 0, 'Missing arguments exception';
is( ( not $rest_api->create_performance_data( "arg1", "arg2", \%outcome, "arg4", "arg5" ) and $outcome{response_error} =~ /Invalid number of args/ ),
    1, 'Too many arguments exception' );

is( ( not $rest_api->create_performance_data( undef, {}, \%outcome, [] ) and $outcome{response_error} =~ /Undefined arg/ ),
    1, 'Undefined argument exception' );
is( ( not $rest_api->create_performance_data( [], undef, \%outcome, [] ) and $outcome{response_error} =~ /Undefined arg/ ),
    1, 'Undefined argument exception' );
is $rest_api->create_performance_data( [], {}, undef, [] ), 0, 'Undefined argument exception';
is( ( not $rest_api->create_performance_data( [], {}, \%outcome, undef ) and $outcome{response_error} =~ /Undefined arg/ ),
    1, 'Undefined argument exception' );

is( ( not $rest_api->create_performance_data( {}, {}, \%outcome, [] ) and $outcome{response_error} =~ /Expecting ARRAY reference/ ),
    1, 'Incorrect object type reference exception' );
is( ( not $rest_api->create_performance_data( [], [], \%outcome, [] ) and $outcome{response_error} =~ /Expecting HASH reference/ ),
    1, 'Incorrect arguments object type reference exception' );
is $rest_api->create_performance_data( [], {}, [], [] ), 0, 'Incorrect arguments object type reference exception';
is( ( not $rest_api->create_performance_data( [], {}, \%outcome, {} ) and $outcome{response_error} =~ /Expecting ARRAY reference/ ),
    1, 'Incorrect arguments object type reference exception' );

is( ( not $rest_api->create_performance_data( "", {}, \%outcome, [] ) and $outcome{response_error} =~ /Expecting ARRAY reference/ ),
    1, 'Incorrect argument object type reference exception' );
is( ( not $rest_api->create_performance_data( [], "", \%outcome, [] ) and $outcome{response_error} =~ /Expecting HASH reference/ ),
    1, 'Incorrect argument object type reference exception' );
is $rest_api->create_performance_data( [], {}, "", [] ), 0, 'Incorrect argument object type reference exception';
is( ( not $rest_api->create_performance_data( [], {}, \%outcome, "" ) and $outcome{response_error} =~ /Expecting ARRAY reference/ ),
    1, 'Incorrect argument object type reference exception' );

# ------------------------------------------------------------------------------------
# Simple positive test case  - creates some performance data records
# ------------------------------------------------------------------------------------
my $now = time();

my @perfdata = (
    {
	appType     => 'NAGIOS',
#	criteria    => 'unknown',
	serverName  => 'localhost',
	serviceName => 'local_load',
	serverTime  => $now,
	label       => 'local_load_load1',
	value       => 0.110,
	warning     => 5.0,
	critical    => 10.0
    },
    {
	appType     => 'NAGIOS',
#	criteria    => 'unknown',
	serverName  => 'localhost',
	serviceName => 'local_load',
	serverTime  => $now,
	label       => 'local_load_load5',
	value       => 0.060,
	warning     => 4.0,
	critical    => 8.0
    },
    {
	appType     => 'NAGIOS',
#	criteria    => 'unknown',
	serverName  => 'localhost',
	serviceName => 'local_load',
	serverTime  => $now,
	label       => 'local_load_load15',
	value       => 0.030,
	warning     => 3.0,
	critical    => 6.0
    },
    {
	appType     => 'NAGIOS',
#	criteria    => 'unknown',
	serverName  => 'localhost',
	serviceName => 'local_cpu_java',
	serverTime  => $now,
	label       => 'local_cpu_java_%CPU',
	value       => 4.9,
	warning     => 40,
	critical    => 50
    }
);

# create a test performance data record
is $rest_api->create_performance_data( \@perfdata, {}, \%outcome, \@results ), 1, "Successfully created test performance data on host localhost";
$logger->debug("\\\%outcome:\n", Dumper \%outcome);
$logger->debug("\\\@results:\n", Dumper \@results);

if (0) {
    # ------------------------------------------------------------------------------------
    # try to create performance data with malformed instructions - missing required property test
    @perfdata = (
	{
	    'consolidationName' => 'NAGIOSEVENT',
	    'monitorStatus' => 'UP',
	    'service'       => 'local_load',
	    'properties'    => { 'Latency' => '125.0', 'Comments' => 'Additional comments' },
	    'host'          => 'localhost',
	    'appType'       => 'NAGIOS',
	    'monitorServer' => 'localhost',
	    'severity'      => 'SERIOUS',
	    'reportDate'    => '2013-06-02T10:55:32.943'
	}
    );

    is $rest_api->create_performance_data( \@perfdata, {},  \%outcome, \@results ), 0, "Failed to create performance data due to missing required property 'device'";
    $logger->debug("\\\%outcome:\n", Dumper \%outcome);
    $logger->debug("\\\@results:\n", Dumper \@results);
}

# MORE TESTS TBD

# Verify that logout (that is, destroying any server-side authentication tokens) works as planned.
is $rest_api->DESTROY(), 1, 'Logout';

# Now have Perl itself call the destructor.  This will call $rest_api->DESTROY() a second time.
$rest_api = undef;

# ------------------------------------------------------------------------------------
$logger->debug("----- END create_performance_data() tests ----");
done_testing();

