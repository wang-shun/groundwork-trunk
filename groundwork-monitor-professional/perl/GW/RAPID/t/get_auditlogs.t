#!/usr/local/groundwork/perl/bin/perl -w --

# This test script tests the REST API Perl module get_auditlogs() routine

use warnings;
use strict;

# FIX MAJOR:  This is one form of workaround for the underscore-to-dash translation issue.
# It should be fixed instead by renaming the APP_NAME and GWOS_API_TOKEN header names.
use HTTP::Headers;
$HTTP::Headers::TRANSLATE_UNDERSCORE = 0;

use GW::RAPID;
use Test::More;
use Test::Deep;
use Test::Exception;
use JSON;
use Data::Dumper; $Data::Dumper::Indent   = 1; $Data::Dumper::Sortkeys = 1;
use File::Basename; my $requestor = "RAPID-" . basename($0);

use lib '/usr/local/groundwork/core/foundation/api/perl/lib';
use Log::Log4perl qw(get_logger);

Log::Log4perl::init('GW_RAPID.log4perl.conf');

my $logger = get_logger("GW.RAPID.module");
$logger->debug("----- START get_auditlogs() tests ----");
my ( %outcome, %results, @results, $query ) = ();

# ----------------------------------------------------------------------------------------------------------------------
# initialize the REST API
my $rest_api = GW::RAPID->new( undef, undef, undef, undef, $requestor, { access => '/usr/local/groundwork/config/ws_client.properties' });

# Exception testing

is $rest_api->get_auditlogs("arg1"), 0, 'Missing arguments exception';
is( ( not $rest_api->get_auditlogs( "arg1", "arg2", \%outcome, "arg4", "arg5" ) and $outcome{response_error} =~ /Invalid number of args/ ),
    1, 'Too many arguments exception' );

is( ( not $rest_api->get_auditlogs( undef, {}, \%outcome, {} ) and $outcome{response_error} =~ /Undefined arg/ ),
    1, 'Undefined argument exception' );
is( ( not $rest_api->get_auditlogs( [], undef, \%outcome, {} ) and $outcome{response_error} =~ /Undefined arg/ ),
    1, 'Undefined argument exception' );
is $rest_api->get_auditlogs( [], {}, undef, {} ), 0, 'Undefined argument exception';
is( ( not $rest_api->get_auditlogs( [], {}, \%outcome, undef ) and $outcome{response_error} =~ /Undefined arg/ ),
    1, 'Undefined argument exception' );

is( ( not $rest_api->get_auditlogs( {}, {}, \%outcome, {} ) and $outcome{response_error} =~ /Expecting ARRAY reference/ ),
    1, 'Incorrect object type reference exception' );
is( ( not $rest_api->get_auditlogs( [], [], \%outcome, {} ) and $outcome{response_error} =~ /Expecting HASH reference/ ),
    1, 'Incorrect object type reference exception' );
is $rest_api->get_auditlogs( [], {}, [], {} ), 0, 'Incorrect object type reference exception';
is( ( not $rest_api->get_auditlogs( [], {}, \%outcome, [] ) and $outcome{response_error} =~ /Expecting HASH reference/ ),
    1, 'Incorrect object type reference exception' );

is( ( not $rest_api->get_auditlogs( "", {}, \%outcome, {} ) and $outcome{response_error} =~ /Expecting ARRAY reference/ ),
    1, 'Incorrect object type reference exception' );
is( ( not $rest_api->get_auditlogs( [], "", \%outcome, {} ) and $outcome{response_error} =~ /Expecting HASH reference/ ),
    1, 'Incorrect object type reference exception' );
is $rest_api->get_auditlogs( [], {}, "", {} ), 0, 'Incorrect object type reference exception';
is( ( not $rest_api->get_auditlogs( [], {}, \%outcome, "" ) and $outcome{response_error} =~ /Expecting HASH reference/ ),
    1, 'Incorrect object type reference exception' );


is $rest_api->get_auditlogs( [], {}, \%outcome, \%results ), 1, "Unlimited query, probably dangerously large";
$logger->debug("\\\%outcome:\n", Dumper \%outcome);
$logger->debug("\\\%results:\n", Dumper \%results);

is $rest_api->get_auditlogs( [], { count => 2, first => 1 }, \%outcome, \%results ), 1,
  "No application type specified , depth=simple, count=2, first=1 - application types should be returned";
$logger->debug("\\\%outcome:\n", Dumper \%outcome);
$logger->debug("\\\%results:\n", Dumper \%results);

$query = "description like '%Commit%'";    # successful query based auditlog entry retrieval
is $rest_api->get_auditlogs( [], { query => $query }, \%outcome, \%results ), 1, "Simple successful query ($query)";
$logger->debug("\\\%outcome:\n", Dumper \%outcome);
$logger->debug("\\\%results:\n", Dumper \%results);

$query = "description like 'CABBAGES'";    # UNSuccessful *application types* retrieval with a query
is $rest_api->get_auditlogs( [], { query => $query }, \%outcome, \%results ), 0, "Unsuccessful simple query ($query)";

$query = "auditLogId = 1";    # Successful auditLogId retrieval with a query
is $rest_api->get_auditlogs( [], { query => $query }, \%outcome, \%results ), 1, "Successful simple query ($query)";

# Deeper positive single and multi application type retrieval testing.
# We don't really care what the values are, just that the fields are there.

# See http://www.perlmonks.org/?node_id=1039467 for initial hints about this mechanism of supporting optional hash keys.
# (Our construction seems a bit cleaner for matching a single object.)
my %required = (
    auditLogId         => re('.*'),
    subsystem          => re('.*'),
    hostName           => re('.*'),
    description        => re('.*'),
    action             => re('.*'),
    username           => re('.*'),
    timestamp          => re('.*')
);
my %optional = (
    serviceDescription => re('.*')
);
my $validator = all( superhashof( \%required ), subhashof( { %required, %optional } ) );

# Simple successful retrieval test
is $rest_api->get_auditlogs( [2], {}, \%outcome, \%results, ), 1, "auditLogId 2 found";
$logger->debug( "\\\%outcome:\n", Dumper \%outcome );
$logger->debug( "\\\%results:\n", Dumper \%results );
foreach my $app_type_name ( keys %results ) {
    ## $logger->debug("Comparing resultant '$app_type_name' application type.");
    cmp_deeply( $results{$app_type_name},
	$validator, "Single-application-type result: Received expected structure of application type result '$app_type_name'" );
}

# Simple Successful *application types* retrieval test, first 4 only
is $rest_api->get_auditlogs( [], { depth => 'simple', count => 4, first => 1 }, \%outcome, \%results ), 1,
  "No application type name specified , depth=simple, count=4, first=1 - application types should be returned";

foreach my $app_type_name ( keys %results ) {
    ## $logger->debug("Comparing resultant '$app_type_name' application type.");
    cmp_deeply( $results{$app_type_name},
	$validator, "Multi-application-type result: Received expected structure of application type result '$app_type_name'" );
}

# Verify that logout (that is, destroying any server-side authentication tokens) works as planned.
is $rest_api->DESTROY(), 1, 'Logout';

# Now have Perl itself call the destructor.  This will call $rest_api->DESTROY() a second time.
$rest_api = undef;

# ----------------------------------------------------------------------------------------------------------------------
$logger->debug("----- END get_auditlogs() tests ----");
done_testing();

