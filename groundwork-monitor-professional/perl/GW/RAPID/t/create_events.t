#!/usr/local/groundwork/perl/bin/perl -w --

# This test script tests the REST API Perl module create_events() routine

die "$0: Please set PGPASSWORD in the environment before running this test script.\n" if not $ENV{'PGPASSWORD'};
my $PSQL="/usr/local/groundwork/postgresql/bin/psql"; # change if necessary

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
$logger->debug("----- START create_events() tests ----");
my ( %outcome, %results, @results ) = ();

# ----------------------------------------------------------------------------------------------------------------------
# initialize the REST API
my $rest_api = GW::RAPID->new( undef, undef, undef, undef, $requestor, { access => '/usr/local/groundwork/config/ws_client.properties' });

# Exception testing
is $rest_api->create_events(  "arg1" ), 0, 'Missing arguments exception';
is( ( not $rest_api->create_events( "arg1", "arg2", \%outcome, "arg4", "arg5" ) and $outcome{response_error} =~ /Invalid number of args/ ),
    1, 'Too many arguments exception' );

is( ( not $rest_api->create_events( undef, {}, \%outcome, [] ) and $outcome{response_error} =~ /Undefined arg/ ),
    1, 'Undefined argument exception' );
is( ( not $rest_api->create_events( [], undef, \%outcome, [] ) and $outcome{response_error} =~ /Undefined arg/ ),
    1, 'Undefined argument exception' );
is $rest_api->create_events( [], {}, undef, [] ), 0, 'Undefined argument exception';
is( ( not $rest_api->create_events( [], {}, \%outcome, undef ) and $outcome{response_error} =~ /Undefined arg/ ),
    1, 'Undefined argument exception' );

is( ( not $rest_api->create_events( {}, {}, \%outcome, [] ) and $outcome{response_error} =~ /Expecting ARRAY reference/ ),
    1, 'Incorrect object type reference exception' );
is( ( not $rest_api->create_events( [], [], \%outcome, [] ) and $outcome{response_error} =~ /Expecting HASH reference/ ),
    1, 'Incorrect arguments object type reference exception' );
is $rest_api->create_events( [], {}, [], [] ), 0, 'Incorrect arguments object type reference exception';
is( ( not $rest_api->create_events( [], {}, \%outcome, {} ) and $outcome{response_error} =~ /Expecting ARRAY reference/ ),
    1, 'Incorrect arguments object type reference exception' );

is( ( not $rest_api->create_events( "", {}, \%outcome, [] ) and $outcome{response_error} =~ /Expecting ARRAY reference/ ),
    1, 'Incorrect argument object type reference exception' );
is( ( not $rest_api->create_events( [], "", \%outcome, [] ) and $outcome{response_error} =~ /Expecting HASH reference/ ),
    1, 'Incorrect argument object type reference exception' );
is $rest_api->create_events( [], {}, "", [] ), 0, 'Incorrect argument object type reference exception';
is( ( not $rest_api->create_events( [], {}, \%outcome, "" ) and $outcome{response_error} =~ /Expecting ARRAY reference/ ),
    1, 'Incorrect argument object type reference exception' );

# ------------------------------------------------------------------------------------
# Simple positive test case  - creates an event
# ------------------------------------------------------------------------------------
my $eventmessage_base = "__RAPID_test__ test message";
my $eventmessage = $eventmessage_base . time();

# delete any test events first
my $delete_command = "$PSQL -c \"delete from logmessage where textmessage like '${eventmessage_base}_%';\" gwcollagedb;";
print "Removing collage events (log messages) with textmessage like ${eventmessage_base}* with '$delete_command'\n";
if ( system( $delete_command ) >> 8  )  { print "Command '$delete_command' failed!! Quitting\n"; exit; }
my @events = (
    {
	'consolidationName' => 'NAGIOSEVENT',
	'device'            => '127.0.0.1',
	'monitorStatus'     => 'UP',
	'service'           => 'local_load',
	'properties'        => { 'Latency' => '125.0', 'Comments' => 'Additional comments' },
	'host'              => 'localhost',
	'appType'           => 'NAGIOS',
	'textMessage'       => $eventmessage,
	'monitorServer'     => 'localhost',
	'severity'          => 'SERIOUS',
	'reportDate'        => '2013-06-02T10:55:32.943'
    }
);

# create a test event
is $rest_api->create_events( \@events, {}, \%outcome, \@results ), 1, "Successfully created test event with message '$eventmessage' on host localhost";
$logger->debug("\\\%outcome:\n", Dumper \%outcome);
$logger->debug("\\\@results:\n", Dumper \@results);

# get the id of the test event
is $rest_api->get_events( [], { query => "textMessage like '$eventmessage%' and host='localhost')" }, \%outcome, \%results ), 1,
  "Successful retrieved event (id: " . join(', ', keys %results) . ")";
$logger->debug("\\\%outcome:\n", Dumper \%outcome);
$logger->debug("\\\%results:\n", Dumper \%results);

@events = (
    {
	'consolidationName' => 'NAGIOSEVENT',
	'device'            => '127.0.0.1',
	'monitorStatus'     => 'UP',
	'service'           => 'local_load',
	'properties'        => {
	    'Latency'  => '125.0',
	    'Comments' => 'UPDATED Additional comments'    # << update
	},
	'host'          => 'localhost',
	'appType'       => 'NAGIOS',
	'textMessage'   => $eventmessage,
	'monitorServer' => 'localhost',
	'severity'      => 'OK',                           # << update
	'reportDate'    => '2013-06-02T10:58:32.943'
    }
);

# NOTE NOTE NOTE
# There is a bug in the REST API (actually in the underlying Foundation API that the REST API calls) today that
# prevents events of application type NAGIOS or SYSTEM from being updated.

# ------------------------------------------------------------------------------------
# try to create an event with malformed instructions - missing required property test
@events = (
    {
	'consolidationName' => 'NAGIOSEVENT',
	## 'device' => '127.0.0.1', #<<< OMISSION
	'monitorStatus' => 'UP',
	'service'       => 'local_load',
	'properties'    => { 'Latency' => '125.0', 'Comments' => 'Additional comments' },
	'host'          => 'localhost',
	'appType'       => 'NAGIOS',
	'textMessage'   => $eventmessage,
	'monitorServer' => 'localhost',
	'severity'      => 'SERIOUS',
	'reportDate'    => '2013-06-02T10:55:32.943'
    }
);

is $rest_api->create_events( \@events, {},  \%outcome, \@results ), 0, "Failed to create event due to missing required property 'device'";
$logger->debug("\\\%outcome:\n", Dumper \%outcome);
$logger->debug("\\\@results:\n", Dumper \@results);

# MORE TESTS TBD

# Verify that logout (that is, destroying any server-side authentication tokens) works as planned.
is $rest_api->DESTROY(), 1, 'Logout';

# Now have Perl itself call the destructor.  This will call $rest_api->DESTROY() a second time.
$rest_api = undef;

# ------------------------------------------------------------------------------------
$logger->debug("----- END create_events() tests ----");
done_testing();

