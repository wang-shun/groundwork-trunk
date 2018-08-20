#!/usr/local/groundwork/perl/bin/perl -w --

# This test script tests the REST API Perl module check_license() routine

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
$logger->debug("----- START check_license() tests ----");
my ( %outcome, %results ) = ();

# ----------------------------------------------------------------------------------------------------------------------
# initialize the REST API
my $rest_api = GW::RAPID->new( undef, undef, undef, undef, $requestor, { access => '/usr/local/groundwork/config/ws_client.properties' });

# Exception testing
is $rest_api->check_license(  "arg1" ), 0, 'Missing arguments exception';
is( ( not $rest_api->check_license( "arg1", "arg2", \%outcome, "arg4", "arg5" ) and $outcome{response_error} =~ /Invalid number of args/ ),
    1, 'Too many arguments exception' );

is( ( not $rest_api->check_license( undef, {}, \%outcome, {} ) and $outcome{response_error} =~ /Undefined arg/ ),
    1, 'Undefined argument exception' );
is( ( not $rest_api->check_license( [], undef, \%outcome, {} ) and $outcome{response_error} =~ /Undefined arg/ ),
    1, 'Undefined argument exception' );
is $rest_api->check_license( [], {}, undef, {} ), 0, 'Undefined argument exception';
is( ( not $rest_api->check_license( [], {}, \%outcome, undef ) and $outcome{response_error} =~ /Undefined arg/ ),
    1, 'Undefined argument exception' );

is( ( not $rest_api->check_license( {}, {}, \%outcome, {} ) and $outcome{response_error} =~ /Expecting ARRAY reference/ ),
    1, 'Incorrect object type reference exception' );
is( ( not $rest_api->check_license( [], [], \%outcome, {} ) and $outcome{response_error} =~ /Expecting HASH reference/ ),
    1, 'Incorrect arguments object type reference exception' );
is $rest_api->check_license( [], {}, [], {} ), 0, 'Incorrect arguments object type reference exception';
is( ( not $rest_api->check_license( [], {}, \%outcome, [] ) and $outcome{response_error} =~ /Expecting HASH reference/ ),
    1, 'Incorrect arguments object type reference exception' );

is( ( not $rest_api->check_license( "", {}, \%outcome, {} ) and $outcome{response_error} =~ /Expecting ARRAY reference/ ),
    1, 'Incorrect argument object type reference exception' );
is( ( not $rest_api->check_license( [], "", \%outcome, {} ) and $outcome{response_error} =~ /Expecting HASH reference/ ),
    1, 'Incorrect argument object type reference exception' );
is $rest_api->check_license( [], {}, "", {} ), 0, 'Incorrect argument object type reference exception';
is( ( not $rest_api->check_license( [], {}, \%outcome, "" ) and $outcome{response_error} =~ /Expecting HASH reference/ ),
    1, 'Incorrect argument object type reference exception' );

# ------------------------------------------------------------------------------------
# Checks various allocations against the current license, which is assumed to be a
# 50-device license vs. a small fresh-install configuration of 3 current devices.
# ------------------------------------------------------------------------------------

my @deviceidentifications = ();

is $rest_api->check_license( \@deviceidentifications, { allocate => 0 }, \%outcome, \%results ), 1, "Successfully checked current license state";
$logger->debug("\\\%outcome:\n", Dumper \%outcome);
$logger->debug("\\\%results:\n", Dumper \%results);

is $rest_api->check_license( \@deviceidentifications, { }, \%outcome, \%results ), 1, "Passed default-count (0) device allocation check";
$logger->debug("\\\%outcome:\n", Dumper \%outcome);
$logger->debug("\\\%results:\n", Dumper \%results);

is $rest_api->check_license( \@deviceidentifications, { allocate => 1 }, \%outcome, \%results ), 1, "Passed one-device allocation check";
$logger->debug("\\\%outcome:\n", Dumper \%outcome);
$logger->debug("\\\%results:\n", Dumper \%results);

is $rest_api->check_license( \@deviceidentifications, { allocate => 30 }, \%outcome, \%results ), 1, "Passed multiple-device allocation check";
$logger->debug("\\\%outcome:\n", Dumper \%outcome);
$logger->debug("\\\%results:\n", Dumper \%results);

is $rest_api->check_license( \@deviceidentifications, { allocate => 45 }, \%outcome, \%results ), 1, "Passed bulk-device allocation check";
$logger->debug("\\\%outcome:\n", Dumper \%outcome);
$logger->debug("\\\%results:\n", Dumper \%results);

is $rest_api->check_license( \@deviceidentifications, { allocate => 49 }, \%outcome, \%results ), 0, "Failed large device allocation check";
$logger->debug("\\\%outcome:\n", Dumper \%outcome);
$logger->debug("\\\%results:\n", Dumper \%results);

is $rest_api->check_license( \@deviceidentifications, { allocate => 30_000 }, \%outcome, \%results ), 0, "Failed huge device allocation check";
$logger->debug("\\\%outcome:\n", Dumper \%outcome);
$logger->debug("\\\%results:\n", Dumper \%results);

is $rest_api->check_license( \@deviceidentifications, { allocate => -100 }, \%outcome, \%results ), 1, "Passed device deletion check";
$logger->debug("\\\%outcome:\n", Dumper \%outcome);
$logger->debug("\\\%results:\n", Dumper \%results);

# MORE TESTS TBD

# Verify that logout (that is, destroying any server-side authentication tokens) works as planned.
is $rest_api->DESTROY(), 1, 'Logout';

# Now have Perl itself call the destructor.  This will call $rest_api->DESTROY() a second time.
$rest_api = undef;

# ------------------------------------------------------------------------------------
$logger->debug("----- END check_license() tests ----");
done_testing();

