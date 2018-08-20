#!/usr/local/groundwork/perl/bin/perl -w --

# This test script tests the REST API Perl module create_noma_service_notifications() routine

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
$logger->debug("----- START create_noma_service_notifications() tests ----");
my ( %outcome, %results, @results ) = ();

# ----------------------------------------------------------------------------------------------------------------------
# initialize the REST API
my $rest_api = GW::RAPID->new( undef, undef, undef, undef, $requestor, { access => '/usr/local/groundwork/config/ws_client.properties' });

# Exception testing
is $rest_api->create_noma_service_notifications(  "arg1" ), 0, 'Missing arguments exception';
is( ( not $rest_api->create_noma_service_notifications( "arg1", "arg2", \%outcome, "arg4", "arg5" ) and $outcome{response_error} =~ /Invalid number of args/ ),
    1, 'Too many arguments exception' );

is( ( not $rest_api->create_noma_service_notifications( undef, {}, \%outcome, [] ) and $outcome{response_error} =~ /Undefined arg/ ),
    1, 'Undefined argument exception' );
is( ( not $rest_api->create_noma_service_notifications( [], undef, \%outcome, [] ) and $outcome{response_error} =~ /Undefined arg/ ),
    1, 'Undefined argument exception' );
is $rest_api->create_noma_service_notifications( [], {}, undef, [] ), 0, 'Undefined argument exception';
is( ( not $rest_api->create_noma_service_notifications( [], {}, \%outcome, undef ) and $outcome{response_error} =~ /Undefined arg/ ),
    1, 'Undefined argument exception' );

is( ( not $rest_api->create_noma_service_notifications( {}, {}, \%outcome, [] ) and $outcome{response_error} =~ /Expecting ARRAY reference/ ),
    1, 'Incorrect object type reference exception' );
is( ( not $rest_api->create_noma_service_notifications( [], [], \%outcome, [] ) and $outcome{response_error} =~ /Expecting HASH reference/ ),
    1, 'Incorrect arguments object type reference exception' );
is $rest_api->create_noma_service_notifications( [], {}, [], [] ), 0, 'Incorrect arguments object type reference exception';
is( ( not $rest_api->create_noma_service_notifications( [], {}, \%outcome, {} ) and $outcome{response_error} =~ /Expecting ARRAY reference/ ),
    1, 'Incorrect arguments object type reference exception' );

is( ( not $rest_api->create_noma_service_notifications( "", {}, \%outcome, [] ) and $outcome{response_error} =~ /Expecting ARRAY reference/ ),
    1, 'Incorrect argument object type reference exception' );
is( ( not $rest_api->create_noma_service_notifications( [], "", \%outcome, [] ) and $outcome{response_error} =~ /Expecting HASH reference/ ),
    1, 'Incorrect argument object type reference exception' );
is $rest_api->create_noma_service_notifications( [], {}, "", [] ), 0, 'Incorrect argument object type reference exception';
is( ( not $rest_api->create_noma_service_notifications( [], {}, \%outcome, "" ) and $outcome{response_error} =~ /Expecting ARRAY reference/ ),
    1, 'Incorrect argument object type reference exception' );

# ------------------------------------------------------------------------------------
# Setup.
# ------------------------------------------------------------------------------------
my $host    = 'localhost';
my $service = 'local_cpu_java';

# ------------------------------------------------------------------------------------
# Simple positive test case  - creates a service notification
# ------------------------------------------------------------------------------------
my $output_message_base = "__RAPID_test__ test message";
my $output_message = $output_message_base . time();

# delete any test service notifications first
my $delete_command = "$PSQL -c \"delete from logmessage where textmessage like '${output_message_base}_%';\" gwcollagedb;";
print "Removing collage service notifications (log messages) with textmessage like ${output_message_base}* with '$delete_command'\n";
if ( system( $delete_command ) >> 8  )  { print "Command '$delete_command' failed!! Quitting\n"; exit; }

my @service_notifications = (
    {
	'hostName'            => $host,
	'hostAddress'         => '127.0.0.1',
	'serviceDescription'  => $service,
	'serviceState'        => 'OK',
	'notificationType'    => 'PROBLEM',
	'serviceOutput'       => $output_message,
	'hostGroupNames'      => 'Linux Servers',
	'serviceGroupNames'   => 'My Service Group',
	'notificationComment' => 'more rants and ramblings',
	'checkDateTime'       => '2013-06-02T10:55:32.943'
    }
);

# create a test service notification
is $rest_api->create_noma_service_notifications( \@service_notifications, {}, \%outcome, \@results ), 1,
  "Successfully created test service notification with message '$output_message' on host $host service $service";
$logger->debug("\\\%outcome:\n", Dumper \%outcome);
$logger->debug("\\\@results:\n", Dumper \@results);

# ------------------------------------------------------------------------------------
# FIX MAJOR:  There doesn't seem to be any way to retrieve a hostNotificationId value (or should
# that be serviceNotificationId instead?), if one is assigned on the server when we don't supply
# one when a service notification is created.  Also, there seems to be no way to "update" a
# notification, if such a thing even makes sense.
if (0) {
    ## get the id of the test service notification
    is $rest_api->get_events( [], { query => "textMessage like '$output_message%' and host='$host')" }, \%outcome, \%results ), 1,
      "Successful retrieved event (id: " . join( ', ', keys %results ) . ")";
    $logger->debug( "\\\%outcome:\n", Dumper \%outcome );
    $logger->debug( "\\\%results:\n", Dumper \%results );

    @service_notifications = (
	{
	    'hostName'            => $host,
	    'hostAddress'         => '127.0.0.1',
	    'serviceDescription'  => $service,
	    'serviceState'        => 'CRITICAL',                   # << update
	    'notificationType'    => 'PROBLEM',
	    'serviceOutput'       => $output_message,
	    'hostGroupNames'      => 'Linux Servers',
	    'serviceGroupNames'   => 'My Service Group',
	    'notificationComment' => 'more rants and ramblings',
	    'checkDateTime'       => '2014-04-16T15:26:07.112'     # << update
	}
    );
}

# ------------------------------------------------------------------------------------
# try to create a service notification with malformed instructions - missing required property test
@service_notifications = (
    {
	'hostName'            => $host,
	'hostAddress'         => '127.0.0.1',
	'serviceDescription'  => $service,
	'serviceState'        => 'CRITICAL',
	## 'notificationType'    => 'PROBLEM',	     # << OMISSION
	## 'serviceOutput'       => $output_message, # << OMISSION
	'hostGroupNames'      => 'Linux Servers',
	'serviceGroupNames'   => 'My Service Group',
	'notificationComment' => 'more rants and ramblings',
	'checkDateTime'       => '2014-04-16T15:26:07.112'
    }
);

is $rest_api->create_noma_service_notifications( \@service_notifications, {}, \%outcome, \@results ), 0,
  "Failed to create service notification due to missing required attribute 'notificationType' or 'serviceOutput'";
$logger->debug("\\\%outcome:\n", Dumper \%outcome);
$logger->debug("\\\@results:\n", Dumper \@results);

# MORE TESTS TBD

# Verify that logout (that is, destroying any server-side authentication tokens) works as planned.
is $rest_api->DESTROY(), 1, 'Logout';

# Now have Perl itself call the destructor.  This will call $rest_api->DESTROY() a second time.
$rest_api = undef;

# ------------------------------------------------------------------------------------
$logger->debug("----- END create_noma_service_notifications() tests ----");
done_testing();

