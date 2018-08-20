#!/usr/local/groundwork/perl/bin/perl -w --

# This test script tests the REST API Perl module initialize_rest_api() routine

use strict;
use warnings;

use GW::RAPID;
use Test::More;
use Test::Exception;

use lib '/usr/local/groundwork/core/foundation/api/perl/lib';
use Log::Log4perl qw(get_logger);
use File::Basename; my $requestor = "RAPID-" . basename($0);

Log::Log4perl::init('GW_RAPID.log4perl.conf');

my $logger = get_logger("GW.RAPID.module");
$logger->debug("----- TESTS: START initialize_rest_api() ----");
my $rest_api;

# -----------------------------------------------------------------------------------------------------
is $rest_api = GW::RAPID->new( "arg1", "arg2", "arg3" ), undef, 'Wrong number of arguments';
is $rest_api = GW::RAPID->new( "arg1", "arg2", "arg3", "arg4", undef ), undef, 'Undefined argument(s)';

is $rest_api = GW::RAPID->new( "http", "localhost", "", "wsuser", "requestor" ), undef, 'Empty REST API username';
is $rest_api = GW::RAPID->new( "http", "localhost", "wsuser", "", "requestor" ), undef, 'Empty REST API password';

is $rest_api = GW::RAPID->new( "not http", "localhost", "wsuser", "wsuser", "requestor" ), undef, 'Invalid REST API protocol';
is $rest_api = GW::RAPID->new( "not http", "localhost", undef,    "wsuser", "requestor" ), undef, 'Undefined argument';

# What we used to use, before the new /api/auth/login API became possible and before we changed new() to not throw an exception:
# throws_ok { < RAPID function(params) > }  <expected stringified exception regex>, <test name>
# throws_ok { $rest_api = GW::RAPID->new( "http", "localhost", "__not_wuser__", "***", "requestor" ) } qr/Authentication of REST API credentials failed/, 'Authentication failure';

# What we now use, now that we have suppressed having the constructor throw certain types of exceptions:
is $rest_api = GW::RAPID->new( "http", "localhost", "__not_wuser__", "***", "requestor" ), undef, 'Authentication failure';

# is <RAPID function>(params) , <expected return value of function>, <test name>
like $rest_api = GW::RAPID->new( undef, undef, undef, undef, $requestor,
    { access => '/usr/local/groundwork/config/ws_client.properties' } ),
     qr/^GW::RAPID=HASH\(0x[0-9a-fA-F]+\)$/, 'Initialized REST API object';

$logger->debug("REST token is:  " . ($rest_api->{token} || "unknown"));

# Verify that logout (that is, destroying any server-side authentication tokens) works as planned.
is $rest_api->DESTROY(), 1, 'Logout';

# Now have Perl itself call the destructor.  This will call $rest_api->DESTROY() a second time.
$rest_api = undef;

# -----------------------------------------------------------------------------------------------------
$logger->debug("----- TESTS: END new() ----");
done_testing();
