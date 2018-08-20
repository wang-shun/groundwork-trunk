#!/usr/local/groundwork/perl/bin/perl -w --

# This test script tests the REST API Perl module get_application_types() routine

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
$logger->debug("----- START get_application_types() tests ----");
my ( %outcome, %results, @results, $query ) = ();

# ----------------------------------------------------------------------------------------------------------------------
# initialize the REST API
my $rest_api = GW::RAPID->new( undef, undef, undef, undef, $requestor, { access => '/usr/local/groundwork/config/ws_client.properties' });

# $rest_api->get_application_types( "depth=shallow&query=property.LastPluginOutput like 'OK%'", \%results   );
# $rest_api->get_application_types( 'localhost?depth=simple&first=0&count=5', \%results, "shallow" ) ;
# $rest_api->get_application_types( "localhost?first=10&count=5", \%results, "shallow" ) ;
# $rest_api->get_application_types( "localhost?depth=simple&first=10&count=1", \%results ) ; die to_json(\%results, {  utf8 => 1 , pretty => 1});

# Exception testing

is $rest_api->get_application_types("arg1"), 0, 'Missing arguments exception';
is( ( not $rest_api->get_application_types( "arg1", "arg2", \%outcome, "arg4", "arg5" ) and $outcome{response_error} =~ /Invalid number of args/ ),
    1, 'Too many arguments exception' );

is( ( not $rest_api->get_application_types( undef, {}, \%outcome, {} ) and $outcome{response_error} =~ /Undefined arg/ ),
    1, 'Undefined argument exception' );
is( ( not $rest_api->get_application_types( [], undef, \%outcome, {} ) and $outcome{response_error} =~ /Undefined arg/ ),
    1, 'Undefined argument exception' );
is $rest_api->get_application_types( [], {}, undef, {} ), 0, 'Undefined argument exception';
is( ( not $rest_api->get_application_types( [], {}, \%outcome, undef ) and $outcome{response_error} =~ /Undefined arg/ ),
    1, 'Undefined argument exception' );

is( ( not $rest_api->get_application_types( {}, {}, \%outcome, {} ) and $outcome{response_error} =~ /Expecting ARRAY reference/ ),
    1, 'Incorrect object type reference exception' );
is( ( not $rest_api->get_application_types( [], [], \%outcome, {} ) and $outcome{response_error} =~ /Expecting HASH reference/ ),
    1, 'Incorrect object type reference exception' );
is $rest_api->get_application_types( [], {}, [], {} ), 0, 'Incorrect object type reference exception';
is( ( not $rest_api->get_application_types( [], {}, \%outcome, [] ) and $outcome{response_error} =~ /Expecting HASH reference/ ),
    1, 'Incorrect object type reference exception' );

is( ( not $rest_api->get_application_types( "", {}, \%outcome, {} ) and $outcome{response_error} =~ /Expecting ARRAY reference/ ),
    1, 'Incorrect object type reference exception' );
is( ( not $rest_api->get_application_types( [], "", \%outcome, {} ) and $outcome{response_error} =~ /Expecting HASH reference/ ),
    1, 'Incorrect object type reference exception' );
is $rest_api->get_application_types( [], {}, "", {} ), 0, 'Incorrect object type reference exception';
is( ( not $rest_api->get_application_types( [], {}, \%outcome, "" ) and $outcome{response_error} =~ /Expecting HASH reference/ ),
    1, 'Incorrect object type reference exception' );


is $rest_api->get_application_types( ["NAGIOS"], {}, \%outcome, \%results ), 1, "Application type NAGIOS found";
$logger->debug("\\\%outcome:\n", Dumper \%outcome);
$logger->debug("\\\%results:\n", Dumper \%results);

is $rest_api->get_application_types( ["NAGIOS"], { depth => "shallow" }, \%outcome, \%results ), 1, "Application type NAGIOS found, depth = shallow";
$logger->debug("\\\%outcome:\n", Dumper \%outcome);
$logger->debug("\\\%results:\n", Dumper \%results);

# is <RAPID function>(params) , <expected return value of function>, <test name>
is $rest_api->get_application_types( ["___dingleworts_domtest___"], {}, \%outcome, \%results ), 0,
  "Application type ___dingleworts_domtest___ not found";    # Successfully failed to find an application type that doesn't exist
$logger->debug("\\\%outcome:\n", Dumper \%outcome);
$logger->debug("\\\%results:\n", Dumper \%results);

is $rest_api->get_application_types( ["NAGIOS"], { depth => "simple" },  \%outcome, \%results ), 1, "Application type NAGIOS found, depth = simple";
is $rest_api->get_application_types( ["NAGIOS"], { depth => "deep" },    \%outcome, \%results ), 1, "Application type NAGIOS found, depth = deep";
is $rest_api->get_application_types( ["NAGIOS"], {}, \%outcome, \%results ), 1, "Application type NAGIOS found, depth not defined";

# Simple Successful *application types* retrieval test, first 2 only
is $rest_api->get_application_types( [], { depth => 'simple', count => 2, first => 1 }, \%outcome, \%results ), 1,
  "No application type specified , depth=simple, count=2, first=1 - application types should be returned";
$logger->debug("\\\%outcome:\n", Dumper \%outcome);
$logger->debug("\\\%results:\n", Dumper \%results);

$query = "description like '%application%'";    # successful query based application types retrieval
is $rest_api->get_application_types( [], { query => $query }, \%outcome, \%results ), 1, "Simple successful query ($query)";
$logger->debug("\\\%outcome:\n", Dumper \%outcome);
$logger->debug("\\\%results:\n", Dumper \%results);

$query = "description like '%application%'";
is $rest_api->get_application_types( [], { depth => 'simple', query => $query }, \%outcome, \%results ), 1,
  "Simple successful query ($query), with depth set to simple";
$logger->debug( "\\\%outcome:\n", Dumper \%outcome );
$logger->debug( "\\\%results:\n", Dumper \%results );

$query = "description like 'CABBAGES'";    # UNSuccessful *application types* retrieval with a query
is $rest_api->get_application_types( [], { query => $query }, \%outcome, \%results ), 0, "Unsuccessful simple query ($query)";

# Deeper positive single and multi application type retrieval testing.
# We don't really care what the values are, just that the fields are there.
my $simple_application_type_cmp = {
    displayName             => re('.*'),
    description             => re('.*'),
    id                      => re('.*'),
    name                    => re('.*'),
    stateTransitionCriteria => re('.*'),
    properties              => re('.*')
};

# Simple successful *application type* retrieval test
# It's unlikely that SYSTEM isn't in the configuration.
is $rest_api->get_application_types( ['SYSTEM'], { depth => "simple" }, \%outcome, \%results, ), 1, "Application type 'SYSTEM' found";
$logger->debug( "\\\%outcome:\n", Dumper \%outcome );
$logger->debug( "\\\%results:\n", Dumper \%results );
foreach my $app_type_name ( keys %results ) {
    ## $logger->debug("Comparing resultant '$app_type_name' application type.");
    cmp_deeply( $results{$app_type_name}, $simple_application_type_cmp, "Single-application-type result: Received expected structure of application type result '$app_type_name'" );
}

is $rest_api->get_application_types( ['CHRHEV', 'ARCHIVE'], { depth => "simple" }, \%outcome, \%results, ), 1, "Application types 'CHRHEV' and 'ARCHIVE' found";
$logger->debug( "\\\%outcome:\n", Dumper \%outcome );
$logger->debug( "\\\%results:\n", Dumper \%results );
foreach my $app_type_name ( keys %results ) {
    ## $logger->debug("Comparing resultant '$app_type_name' application type.");
    cmp_deeply( $results{$app_type_name}, $simple_application_type_cmp, "Multi-application-type result: Received expected structure of application type result '$app_type_name'" );
}

# Simple Successful *application types* retrieval test, first 4 only
is $rest_api->get_application_types( [], { depth => 'simple', count => 4, first => 1 }, \%outcome, \%results ), 1,
  "No application type name specified , depth=simple, count=4, first=1 - application types should be returned";

foreach my $app_type_name ( keys %results ) {
    ## $logger->debug("Comparing resultant '$app_type_name' application type.");
    cmp_deeply( $results{$app_type_name}, $simple_application_type_cmp, "Multi-application0type result: Received expected structure of application type result '$app_type_name'" );
}

# Verify that logout (that is, destroying any server-side authentication tokens) works as planned.
is $rest_api->DESTROY(), 1, 'Logout';

# Now have Perl itself call the destructor.  This will call $rest_api->DESTROY() a second time.
$rest_api = undef;

# ----------------------------------------------------------------------------------------------------------------------
$logger->debug("----- END get_application_types() tests ----");
done_testing();

