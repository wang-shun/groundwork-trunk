#!/usr/local/groundwork/perl/bin/perl -w --

# This test script tests the REST API Perl module get_events() routine

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
$logger->debug("----- START get_events() tests ----");

my ( %outcome, %results, @results, $query ) = ();

# ----------------------------------------------------------------------------------------------------------------------
# initialize the REST API
my $rest_api = GW::RAPID->new( undef, undef, undef, undef, $requestor, { access => '/usr/local/groundwork/config/ws_client.properties' });

my ( $startid, $secondid ) = ( 1,2 );    # on fresh system

#my ($startid, $secondid) = (122,123);

# Exception testing

is $rest_api->get_events("arg1"), 0, 'Missing arguments exception';
is(
    (
	not $rest_api->get_events( "arg1", "arg2", \%outcome, "arg4", "arg5" )
	  and $outcome{response_error} =~ /Invalid number of args/
    ),
    1,
    'Too many arguments exception'
);

is( ( not $rest_api->get_events( undef, {}, \%outcome, {} ) and $outcome{response_error} =~ /Undefined arg/ ),
    1, 'Undefined argument exception' );
is( ( not $rest_api->get_events( [], undef, \%outcome, {} ) and $outcome{response_error} =~ /Undefined arg/ ),
    1, 'Undefined argument exception' );
is $rest_api->get_events( [], {}, undef, {} ), 0, 'Undefined argument exception';
is( ( not $rest_api->get_events( [], {}, \%outcome, undef ) and $outcome{response_error} =~ /Undefined arg/ ),
    1, 'Undefined argument exception' );

is( ( not $rest_api->get_events( {}, {}, \%outcome, {} ) and $outcome{response_error} =~ /Expecting ARRAY reference/ ),
    1, 'Incorrect object type reference exception' );
is( ( not $rest_api->get_events( [], [], \%outcome, {} ) and $outcome{response_error} =~ /Expecting HASH reference/ ),
    1, 'Incorrect object type reference exception' );
is $rest_api->get_events( [1], {}, [], {} ), 0, 'Incorrect object type reference exception';
is( ( not $rest_api->get_events( [1], {}, \%outcome, [] ) and $outcome{response_error} =~ /Expecting HASH reference/ ),
    1, 'Incorrect object type reference exception' );

is( ( not $rest_api->get_events( "", {}, \%outcome, {} ) and $outcome{response_error} =~ /Expecting ARRAY reference/ ),
    1, 'Incorrect object type reference exception' );
is( ( not $rest_api->get_events( [], "", \%outcome, {} ) and $outcome{response_error} =~ /Expecting HASH reference/ ),
    1, 'Incorrect object type reference exception' );
is $rest_api->get_events( [1], {}, "", {} ), 0, 'Incorrect object type reference exception';
is( ( not $rest_api->get_events( [1], {}, \%outcome, "" ) and $outcome{response_error} =~ /Expecting HASH reference/ ),
    1, 'Incorrect object type reference exception' );

# is <RAPID function>(params) , <expected return value of function>, <test name>

is(
    (
	not $rest_api->get_events( [], {}, \%outcome, \%results )
	  and $outcome{response_error} =~ /You are not allowed to retrieve all events in one call/
    ),
    1,
    'Get all events exception'
);

is $rest_api->get_events( [$startid], {}, \%outcome, \%results ), 1, "Get event id=$startid";    # assumes clean install with an event id of $startid present
is $rest_api->get_events( ["___dingleworts_domtest___"], {}, \%outcome, \%results ), 0,
  "Event ___dingleworts_domtest___ not found";    # Successfully failed to find an event that doesn't exist
is $rest_api->get_events( [999999999], {}, \%outcome, \%results ), 0,
  "One singe event 999999999 not found";          # Successfully failed to find an event that doesn't exist
is $rest_api->get_events( [ 999999999, $startid ], {}, \%outcome, \%results ), 1,
  "At least one of the events 999999999,$startid were found";    # Successfully failed to find an event that doesn't exist

# Dominic:  During testing I discovered the strangest thing.  If I ran this test script directly from t/,
# it would pass without failing the 'non deep check' test below.  However, running prove on it would always
# fail the test.  Checking against the curl command, that also failed -- wtf -- why did the test ever pass?
# The reason the curl command failed is because the query in the test yielded no results.  To work around
# this, I had to first create an event to ensure the get would work.
my @events = (
    {
	'consolidationName' => 'NAGIOSEVENT',
	'device'            => '127.0.0.1',
	'monitorStatus'     => 'UP',
	'service'           => 'local_load',
	'properties'        => { 'Latency' => '125.0', 'Comments' => 'Additional comments' },
	'host'              => 'localhost',
	'appType'           => 'NAGIOS',
	'textMessage'       => "RAPID test message",
	'monitorServer'     => 'localhost',
	'severity'          => 'SERIOUS',
	'reportDate'        => '2013-06-02T10:55:32.943'
    }
);

# create a test event
is $rest_api->create_events( \@events, {}, \%outcome, \@results ), 1,
  "Successfully created test event with message 'RAPID test message' on host localhost";

is $rest_api->get_events( [], { query => "(appType='CABBAGE' and service='local_load' and host='localhost')" }, \%outcome, \%results ), 0,
  "UNSuccessful query (non deep check, appType=CABBAGE)";
is $rest_api->get_events( [], { query => "(appType='CABBAGE' FOOBAR service='local_load' and host='localhost')" }, \%outcome, \%results ), 0,
  "UNSuccessful malformed query";

# If your system has been running any reasonable length of time, these constraints may not be nearly enough to
# prevent a flood of data coming back, and to prevent the server from spending a lot of time on this request.
is $rest_api->get_events( [], { query => "(appType='NAGIOS' and service='local_load' and host='localhost')" }, \%outcome, \%results ), 1,
  "Successful query (non deep check)";    # <<< doesn't work unless seed with event - see above

$logger->debug("\\\%outcome:\n", Dumper \%outcome);
$logger->debug("\\\%results:\n", Dumper \%results);

# This is basically a very bad idea, trying to grab ALL events, order them in reverse, then just pluck out
# the first one of them (effectively, just the last event).  The overhead in doing this is ENORMOUS.
is $rest_api->get_events( [], { count => 1, query => "order by id desc" }, \%outcome, \%results ), 1,
  "Successful query to get last log message";    # dumper if want to check :)

# Deeper positive single and multi host retrieval testing
my %event_cmp = (
    priority            => re('.*'),
    operationStatus     => re('.*'),
    lastInsertDate      => re('.*'),
    monitorStatus       => re('.*'),
    appType             => re('.*'),
    id                  => re('.*'),
    severity            => re('.*'),
    typeRule            => re('.*'),
    firstInsertDate     => re('.*'),
    applicationSeverity => re('.*'),
    device              => re('.*'),
    properties          => re('.*'),
    textMessage         => re('.*'),
    monitorServer       => re('.*'),
    component           => re('.*'),
    msgCount            => re('.*'),
    reportDate          => re('.*'),
);

# Event or events - they all get shoved into an events container
is $rest_api->get_events( [$startid], {}, \%outcome, \%results ), 1, "Get event id=$startid";
foreach my $got_v ( @{ $results{events} } ) {
    cmp_deeply( $got_v, \%event_cmp, "Event id=$startid result: Expected structure of event id $got_v->{id}" );
}

is $rest_api->get_events( [ $startid, $secondid ], {}, \%outcome, \%results ), 1, "Get events $startid and $secondid";
foreach my $got_v ( @{ $results{events} } ) {
    cmp_deeply( $got_v, \%event_cmp, "Event result: Expected structure of event id $got_v->{id}" );
}

#is $rest_api->get_events( "", \%results ), 1, "Get all events"; # this test might be overkill espy if there are a lot of events in the system!
# Skipping as structure can vary - overkill anyhow
#foreach my $got_v ( @{$results{events}} )
#{
#    cmp_deeply($got_v, \%event_cmp, "Event result: Expected structure of event id $got_v->{id}");
#}

# Verify that logout (that is, destroying any server-side authentication tokens) works as planned.
is $rest_api->DESTROY(), 1, 'Logout';

# Now have Perl itself call the destructor.  This will call $rest_api->DESTROY() a second time.
$rest_api = undef;

# ----------------------------------------------------------------------------------------------------------------------
$logger->debug("----- END get_events() tests ----");
done_testing();

