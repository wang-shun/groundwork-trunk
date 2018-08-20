#!/usr/local/groundwork/perl/bin/perl -w --

# This test script tests the REST API Perl module update_events() routine

die "$0: Please set PGPASSWORD in the environment before running this test script.\n" if not $ENV{'PGPASSWORD'};
my $PSQL = "/usr/local/groundwork/postgresql/bin/psql";    # change if necessary

use strict;
use warnings;

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
$logger->debug("----- START update_events() tests -----");

my ( @events, %outcome, @results, %results, %params ) = ();

# ----------------------------------------------------------------------------------------------------------------------
# initialize the REST API
my $rest_api = GW::RAPID->new( undef, undef, undef, undef, $requestor, { access => '/usr/local/groundwork/config/ws_client.properties' });

# Exception testing
# expected args:  $eventids, $options, $outcome, $results

is $rest_api->update_events("arg1"), 0, 'Missing arguments exception';
is( ( not $rest_api->update_events( "arg1", "arg2", \%outcome, "arg4", "arg5" ) and $outcome{response_error} =~ /Invalid number of args/ ), 1, 'Too many arguments exception' );
is( ( not $rest_api->update_events( undef, {}, \%outcome, [] ) and $outcome{response_error} =~ /Undefined arg/ ), 1, 'Undefined argument exception' );
is( ( not $rest_api->update_events( [], undef, \%outcome, [] ) and $outcome{response_error} =~ /Undefined arg/ ), 1, 'Undefined argument exception' );
is $rest_api->update_events( [], {}, undef, [] ), 0, 'Undefined argument exception'; 
is( ( not $rest_api->update_events( [], {}, \%outcome, undef ) and $outcome{response_error} =~ /Undefined arg/ ), 1, 'Undefined argument exception' );
is( ( not $rest_api->update_events( {}, {}, \%outcome, [] ) and $outcome{response_error} =~ /Expecting ARRAY reference/ ), 1, 'Incorrect object type reference exception' );
is( ( not $rest_api->update_events( [], [], \%outcome, [] ) and $outcome{response_error} =~ /Expecting HASH reference/ ), 1, 'Incorrect object type reference exception' );
is $rest_api->update_events( [], {}, [], [] ), 0, 'Incorrect object type reference exception';
is( ( not $rest_api->update_events( [], {}, \%outcome, {} ) and $outcome{response_error} =~ /Expecting ARRAY reference/ ), 1, 'Incorrect object type reference exception' );
is( ( not $rest_api->update_events( "", {}, \%outcome, [] ) and $outcome{response_error} =~ /Expecting ARRAY reference/ ), 1, 'Incorrect object type reference exception' );
is( ( not $rest_api->update_events( [], "", \%outcome, [] ) and $outcome{response_error} =~ /Expecting HASH reference/ ), 1, 'Incorrect object type reference exception' );
is $rest_api->update_events( [], {}, "", [] ), 0, 'Incorrect object type reference exception'; is( ( not $rest_api->update_events( [], {}, \%outcome, "" ) and $outcome{response_error} =~ /Expecting ARRAY reference/ ), 1, 'Incorrect object type reference exception' );

# ------------------------------------------------------------------------------------
# Simple positive test case  - creates some events and then updates them
# ------------------------------------------------------------------------------------

my $eventmessage_base = "__RAPID_test__ test message";
my $eventmessage      = $eventmessage_base . time();

# delete any test events first
my $delete_command = "$PSQL -c \"delete from logmessage where textmessage like '${eventmessage_base}_%';\" gwcollagedb;";
print "Removing collage events (log messages) with textmessage like ${eventmessage_base}* with '$delete_command'\n";
if ( system($delete_command ) >> 8 ) { print "Command '$delete_command' failed!! Quitting\n"; exit; }

@events = (
    {
	## 'consolidationName' => 'NAGIOSEVENT', # switch off consolidation in order to make two separate log events
	'device'        => '127.0.0.1',
	'monitorStatus' => 'UP',
	'service'       => 'local_load',
	'host'          => 'localhost',
	'appType'       => 'NAGIOS',
	'textMessage'   => "$eventmessage local_load service message",
	'monitorServer' => 'localhost',
	'severity'      => 'SERIOUS',
	'properties'    => { 'UpdatedBy' => 'dom', 'Comments' => 'Additional comments' },
	'reportDate'    => '2013-06-02T10:55:32.943'
    },
    {
	## 'consolidationName' => 'NAGIOSEVENT', # switch off consolidation in order to make two separate log events
	'device'        => '127.0.0.1',
	'monitorStatus' => 'UP',
	'service'       => 'local_memory',                                                  # <<< another service
	'host'          => 'localhost',
	'appType'       => 'NAGIOS',
	'textMessage'   => "$eventmessage local_memory service message",
	'monitorServer' => 'localhost',
	'severity'      => 'SERIOUS',
	'properties'    => { 'UpdatedBy' => 'dom', 'Comments' => 'Additional comments' },
	'reportDate'    => '2013-06-02T11:55:32.943'
    }
);

# create test events
is $rest_api->create_events( \@events, {}, \%outcome, \@results ), 1,
  "Successfully created test events with message '$eventmessage' and '$eventmessage x two' on host localhost";
$logger->debug("\\\%outcome:\n", Dumper \%outcome);
$logger->debug("\\\@results:\n", Dumper \@results);

# get the ids of the test events to delete
is $rest_api->get_events( [], { query => "textMessage like '$eventmessage%' and host='localhost')" }, \%outcome, \%results ), 1,
  "Successful retrieved events (ids: ".join(', ', sort keys %results).")";
$logger->debug("\\\%outcome:\n", Dumper \%outcome);
$logger->debug("\\\%results:\n", Dumper \%results);
my @eventids = sort keys %results;

# NOTE NOTE NOTE
# There is a bug in the REST API (actually in the underlying Foundation API that the REST API calls) today that
# prevents events of application type NAGIOS or SYSTEM from being updated.
%params = (
    "opStatus"  => "CLOSED",                       # this change definitely makes it into event console
    "updatedBy" => "admin",                        # no idea where this value is visible in events - asked David
    "comments"  => "RAPID test harness comment"    # no idea where this value is visible in events - asked David
);
my $params;
foreach ( keys %params ) { $params .= "$_ => $params{$_} " }
is $rest_api->update_events( \@eventids, \%params, \%outcome, \@results ), 1, "Succesfully updated test events with: $params";

# now try to update an non existent event
@eventids = ( 999999999999999999, -1 );
is $rest_api->update_events( \@eventids, \%params, \%outcome, \@results ), 0, "Failed to update non-existent test event(s) @eventids";

# Verify that logout (that is, destroying any server-side authentication tokens) works as planned.
is $rest_api->DESTROY(), 1, 'Logout';

# Now have Perl itself call the destructor.  This will call $rest_api->DESTROY() a second time.
$rest_api = undef;

# ------------------------------------------------------------------------------------
$logger->debug("----- END update_events() tests ----");
done_testing();

