#!/usr/local/groundwork/perl/bin/perl -w --

# This test script tests the REST API Perl module get_hosts() routine

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
$logger->debug("----- START get_hosts() tests ----");
my ( %outcome, %results, @results, $query ) = ();

# ----------------------------------------------------------------------------------------------------------------------
# initialize the REST API
my $rest_api = GW::RAPID->new( undef, undef, undef, undef, $requestor, { access => '/usr/local/groundwork/config/ws_client.properties' });

# $rest_api->get_hosts( "depth=shallow&query=property.LastPluginOutput like 'OK%'", \%results   );
# $rest_api->get_hosts( 'localhost?depth=simple&first=0&count=5', \%results, "shallow" ) ;
# $rest_api->get_hosts( "localhost?first=10&count=5", \%results, "shallow" ) ;
# $rest_api->get_hosts( "localhost?depth=simple&first=10&count=1", \%results ) ; die to_json(\%results, {  utf8 => 1 , pretty => 1});

# Exception testing

is $rest_api->get_hosts("arg1"), 0, 'Missing arguments exception';
is( ( not $rest_api->get_hosts( "arg1", "arg2", \%outcome, "arg4", "arg5" ) and $outcome{response_error} =~ /Invalid number of args/ ),
    1, 'Too many arguments exception' );

is( ( not $rest_api->get_hosts( undef, {}, \%outcome, {} ) and $outcome{response_error} =~ /Undefined arg/ ),
    1, 'Undefined argument exception' );
is( ( not $rest_api->get_hosts( [], undef, \%outcome, {} ) and $outcome{response_error} =~ /Undefined arg/ ),
    1, 'Undefined argument exception' );
is $rest_api->get_hosts( [], {}, undef, {} ), 0, 'Undefined argument exception';
is( ( not $rest_api->get_hosts( [], {}, \%outcome, undef ) and $outcome{response_error} =~ /Undefined arg/ ),
    1, 'Undefined argument exception' );

is( ( not $rest_api->get_hosts( {}, {}, \%outcome, {} ) and $outcome{response_error} =~ /Expecting ARRAY reference/ ),
    1, 'Incorrect object type reference exception' );
is( ( not $rest_api->get_hosts( [], [], \%outcome, {} ) and $outcome{response_error} =~ /Expecting HASH reference/ ),
    1, 'Incorrect object type reference exception' );
is $rest_api->get_hosts( [], {}, [], {} ), 0, 'Incorrect object type reference exception';
is( ( not $rest_api->get_hosts( [], {}, \%outcome, [] ) and $outcome{response_error} =~ /Expecting HASH reference/ ),
    1, 'Incorrect object type reference exception' );

is( ( not $rest_api->get_hosts( "", {}, \%outcome, {} ) and $outcome{response_error} =~ /Expecting ARRAY reference/ ),
    1, 'Incorrect object type reference exception' );
is( ( not $rest_api->get_hosts( [], "", \%outcome, {} ) and $outcome{response_error} =~ /Expecting HASH reference/ ),
    1, 'Incorrect object type reference exception' );
is $rest_api->get_hosts( [], {}, "", {} ), 0, 'Incorrect object type reference exception';
is( ( not $rest_api->get_hosts( [], {}, \%outcome, "" ) and $outcome{response_error} =~ /Expecting HASH reference/ ),
    1, 'Incorrect object type reference exception' );


is $rest_api->get_hosts( ["localhost"], {}, \%outcome, \%results ), 1, "Host localhost found";
$logger->debug("\\\%outcome:\n", Dumper \%outcome);
$logger->debug("\\\%results:\n", Dumper \%results);

is $rest_api->get_hosts( ["localhost"], { depth => "shallow" }, \%outcome, \%results ), 1, "Host localhost found, depth = shallow";
$logger->debug("\\\%outcome:\n", Dumper \%outcome);
$logger->debug("\\\%results:\n", Dumper \%results);

# is <RAPID function>(params) , <expected return value of function>, <test name>
is $rest_api->get_hosts( ["___dingleworts_domtest___"], {}, \%outcome, \%results ), 0,
  "Host ___dingleworts_domtest___ not found";    # Successfully failed to find a host that doesn't exist

is $rest_api->get_hosts( ["localhost"], { depth => "simple" },  \%outcome, \%results ), 1, "Host localhost found, depth = simple";
is $rest_api->get_hosts( ["localhost"], { depth => "deep" },    \%outcome, \%results ), 1, "Host localhost found, depth = deep";
$logger->debug( "\\\%outcome:\n", Dumper \%outcome );
$logger->debug( "\\\%results:\n", Dumper \%results );
is $rest_api->get_hosts( ["localhost"], {}, \%outcome, \%results ), 1, "Host localhost found, depth not defined";

# Simple Successful *hosts* retrieval test, first 2 only
is $rest_api->get_hosts( [], { depth => 'simple', count => 2, first => 1 }, \%outcome, \%results ), 1,
  "No hostname specified , depth=simple, count=2, first=1 - hosts should be returned";
$logger->debug("\\\%outcome:\n", Dumper \%outcome);
$logger->debug("\\\%results:\n", Dumper \%results);

$query = "property.LastPluginOutput like 'OK%'";    # successful query based hosts retrieval
is $rest_api->get_hosts( [], { query => $query }, \%outcome, \%results ), 1, "Simple successful query ($query)";
$logger->debug("\\\%outcome:\n", Dumper \%outcome);
$logger->debug("\\\%results:\n", Dumper \%results);

$query = "property.LastPluginOutput like 'OK%'";
is $rest_api->get_hosts( [], { depth => 'simple', query => $query }, \%outcome, \%results ), 1,
  "Simple successful query ($query), with depth set to simple";
$logger->debug( "\\\%outcome:\n", Dumper \%outcome );
$logger->debug( "\\\%results:\n", Dumper \%results );

$query = "property.LastPluginOutput like 'CABBAGES'";    # UNSuccessful *hosts* retrieval with a query
is $rest_api->get_hosts( [], { query => $query }, \%outcome, \%results ), 0, "Unsuccessful simple query ($query)";

# Deeper positive single and multi host retrieval testing
my $simple_host_cmp = {
    appType      => re('.*'),
    hostName     => re('.*'),    # don't really care what the values are, just that the fields are there
    acknowledged => re('.*'),
    serviceCount => re('.*'),
    id           => re('.*'),
    description  => re('.*'),
    properties   => re('.*')
};

# Simple successful *host* retrieval test
# It's unlikely that localhost isn't in the configuration.
is $rest_api->get_hosts( ['localhost'], { depth => "simple" }, \%outcome, \%results, ), 1, "Host 'localhost' found";
$logger->debug( "\\\%outcome:\n", Dumper \%outcome );
$logger->debug( "\\\%results:\n", Dumper \%results );
foreach my $h_name ( keys %results ) {
    ## $logger->debug("Comparing resultant '$h_name' host.");
    cmp_deeply( $results{$h_name}, $simple_host_cmp, "Single-host result: Received expected structure of host result '$h_name'" );
}

is $rest_api->get_hosts( ['localhost', 'dakota'], { depth => "simple" }, \%outcome, \%results, ), 1, "Hosts 'localhost' and 'dakota' found";
$logger->debug( "\\\%outcome:\n", Dumper \%outcome );
$logger->debug( "\\\%results:\n", Dumper \%results );
foreach my $h_name ( keys %results ) {
    ## $logger->debug("Comparing resultant '$h_name' host.");
    cmp_deeply( $results{$h_name}, $simple_host_cmp, "Multi-host result: Received expected structure of host result '$h_name'" );
}

# Simple Successful *hosts* retrieval test, first 4 only
is $rest_api->get_hosts( [], { depth => 'simple', count => 4, first => 1 }, \%outcome, \%results ), 1,
  "No hostname specified , depth=simple, count=4, first=1 - hosts should be returned";

foreach my $h_name ( keys %results ) {
    ## $logger->debug("Comparing resultant '$h_name' host.");
    cmp_deeply( $results{$h_name}, $simple_host_cmp, "Multi-host result: Received expected structure of host result '$h_name'" );
}

# Verify that logout (that is, destroying any server-side authentication tokens) works as planned.
is $rest_api->DESTROY(), 1, 'Logout';

# Now have Perl itself call the destructor.  This will call $rest_api->DESTROY() a second time.
$rest_api = undef;

# ----------------------------------------------------------------------------------------------------------------------
$logger->debug("----- END get_hosts() tests ----");
done_testing();

