#!/usr/local/groundwork/perl/bin/perl -w --

# This test script tests the REST API Perl module get_devices() routine

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
$logger->debug("----- START get_devices() tests ----");
my ( %outcome, %results, @results, $query ) = ();

# ----------------------------------------------------------------------------------------------------------------------
# initialize the REST API
my $rest_api = GW::RAPID->new( undef, undef, undef, undef, $requestor, { access => '/usr/local/groundwork/config/ws_client.properties' });

# Exception testing

is $rest_api->get_devices("arg1"), 0, 'Missing arguments exception';
is( ( not $rest_api->get_devices( "arg1", "arg2", \%outcome, "arg4", "arg5" ) and $outcome{response_error} =~ /Invalid number of args/ ),
    1, 'Too many arguments exception' );

is( ( not $rest_api->get_devices( undef, {}, \%outcome, {} ) and $outcome{response_error} =~ /Undefined arg/ ),
    1, 'Undefined argument exception' );
is( ( not $rest_api->get_devices( [], undef, \%outcome, {} ) and $outcome{response_error} =~ /Undefined arg/ ),
    1, 'Undefined argument exception' );
is $rest_api->get_devices( [], {}, undef, {} ), 0, 'Undefined argument exception';
is( ( not $rest_api->get_devices( [], {}, \%outcome, undef ) and $outcome{response_error} =~ /Undefined arg/ ),
    1, 'Undefined argument exception' );

is( ( not $rest_api->get_devices( {}, {}, \%outcome, {} ) and $outcome{response_error} =~ /Expecting ARRAY reference/ ),
    1, 'Incorrect object type reference exception' );
is( ( not $rest_api->get_devices( [], [], \%outcome, {} ) and $outcome{response_error} =~ /Expecting HASH reference/ ),
    1, 'Incorrect object type reference exception' );
is $rest_api->get_devices( [], {}, [], {} ), 0, 'Incorrect object type reference exception';
is( ( not $rest_api->get_devices( [], {}, \%outcome, [] ) and $outcome{response_error} =~ /Expecting HASH reference/ ),
    1, 'Incorrect object type reference exception' );

is( ( not $rest_api->get_devices( "", {}, \%outcome, {} ) and $outcome{response_error} =~ /Expecting ARRAY reference/ ),
    1, 'Incorrect object type reference exception' );
is( ( not $rest_api->get_devices( [], "", \%outcome, {} ) and $outcome{response_error} =~ /Expecting HASH reference/ ),
    1, 'Incorrect object type reference exception' );
is $rest_api->get_devices( [], {}, "", {} ), 0, 'Incorrect object type reference exception';
is( ( not $rest_api->get_devices( [], {}, \%outcome, "" ) and $outcome{response_error} =~ /Expecting HASH reference/ ),
    1, 'Incorrect object type reference exception' );

# is <RAPID function>(params) , <expected return value of function>, <test name>
is $rest_api->get_devices( ["127.0.0.1"], {}, \%outcome, \%results ), 1, "Device 127.0.0.1 (for localhost) found, depth not defined";
is $rest_api->get_devices( ["127.0.0.1"], { depth => "simple" },  \%outcome, \%results ), 1, "Device 127.0.0.1 (for localhost) found, depth = simple";
is $rest_api->get_devices( ["127.0.0.1"], { depth => "shallow" }, \%outcome, \%results ), 1, "Device 127.0.0.1 (for localhost) found, depth = shallow";
is $rest_api->get_devices( ["127.0.0.1"], { depth => "deep" },    \%outcome, \%results ), 1, "Device 127.0.0.1 (for localhost) found, depth = deep";
is $rest_api->get_devices( ["___dingleworts_domtest___"], {}, \%outcome, \%results ), 0, "Device ___dingleworts_domtest___ not found";

# Query based
$query = "description like '%localhost%'";
is $rest_api->get_devices( [], { query => "description = 'Device localhost'" }, \%outcome, \%results ), 1, "Simple successful query , depth not defined";
is $rest_api->get_devices( [], { depth => "deep", query => "description like '%localhost%'" }, \%outcome, \%results ), 1, "Simple successful query , depth = deep";

$query = "description = 'CABBAGES FOR LUNCH' ";
#is $rest_api->get_devices( [], { query => $query }, \%outcome, \%results ), 0, "Unsuccessful simple query ($query)";
is $rest_api->get_devices( [], { query => "description = 'CABBAGES FOR LUNCH' "}, \%outcome, \%results ), 0, "Unsuccessful simple query ($query)";


$query = "cabbages illegaloperator 'CABBAGES FOR LUNCH' ";
is $rest_api->get_devices( [], { query => $query }, \%outcome, \%results ), 0, "Unsuccessful illegal simple query ($query) (this should produce a HTTP status 500 )";

# Deeper positive single and multi host retrieval testing
my $simple_device_cmp = { id => re('.*'), description => re('.*'), identification => re('.*'), displayName => re('.*'), };

# Simple successful *device* retrieval test
# unlikely that localhost isn't in configuration
is $rest_api->get_devices( ["127.0.0.1"], { depth => "simple" }, \%outcome, \%results ), 1, "Device '127.0.0.1' found";
$logger->debug( "\\\%outcome:\n", Dumper \%outcome );
$logger->debug( "\\\%results:\n", Dumper \%results );
foreach my $d_identification ( keys %results ) {
    ## $logger->debug("Comparing resultant '$d_identification' device.");
    cmp_deeply( $results{$d_identification}, $simple_device_cmp, "Single-device result: Received expected structure of device result '$d_identification'" );
}

# FIX MAJOR:  prepare this so we know we do have both devices available, including a non-NULL "description" field.
is $rest_api->get_devices( ["127.0.0.1", "111.111.222.222"], { depth => "simple" }, \%outcome, \%results ), 1, "Device(s) found";
$logger->debug( "\\\%outcome:\n", Dumper \%outcome );
$logger->debug( "\\\%results:\n", Dumper \%results );
foreach my $d_identification ( keys %results ) {
    ## $logger->debug("Comparing resultant '$d_identification' device.");
    cmp_deeply( $results{$d_identification}, $simple_device_cmp, "Multi-device result: Received expected structure of device result '$d_identification'" );
}

# Simple Successful *devices* retrieval test
# Skipping this for now as the result fields set can vary - eg some can omit description for example
#is $rest_api->get_devices( [], {depth=>'simple'}, \%outcome, \%results ), 1, "No device id - all devices should be returned";
#foreach my $got_v ( @{$results{devices}} )
#{
#    cmp_deeply($got_v, $simple_device_cmp, "Multi-device result: Expected structure of device result $got_v->{identification}") ;
#}

# Verify that logout (that is, destroying any server-side authentication tokens) works as planned.
is $rest_api->DESTROY(), 1, 'Logout';

# Now have Perl itself call the destructor.  This will call $rest_api->DESTROY() a second time.
$rest_api = undef;

# ----------------------------------------------------------------------------------------------------------------------
$logger->debug("----- END get_devices() tests ----");
done_testing();

