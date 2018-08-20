#!/usr/local/groundwork/perl/bin/perl -w --

# This test script tests the REST API Perl module delete_devices() routine

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
$logger->debug("----- START delete_devices() tests ----");
my ( @devices, %outcome, %results, @results, %hosts, @hosts ) = ();

# ----------------------------------------------------------------------------------------------------------------------
# initialize the REST API
my $rest_api = GW::RAPID->new( undef, undef, undef, undef, $requestor, { access => '/usr/local/groundwork/config/ws_client.properties' });

# Exception testing

is $rest_api->delete_devices("arg1"), 0, 'Missing arguments exception';
is( ( not $rest_api->delete_devices( "arg1", "arg2", \%outcome, "arg4", "arg5" ) and $outcome{response_error} =~ /Invalid number of args/ ),
    1, 'Too many arguments exception' );

is( ( not $rest_api->delete_devices( undef, {}, \%outcome, [] ) and $outcome{response_error} =~ /Undefined arg/ ),
    1, 'Undefined instructions argument exception' );
is( ( not $rest_api->delete_devices( [], undef, \%outcome, [] ) and $outcome{response_error} =~ /Undefined arg/ ),
    1, 'Undefined instructions argument exception' );
is $rest_api->delete_devices( [], {}, undef, [] ), 0, 'Undefined instructions argument exception';
is( ( not $rest_api->delete_devices( [], {}, \%outcome, undef ) and $outcome{response_error} =~ /Undefined arg/ ),
    1, 'Undefined instructions argument exception' );

is( ( not $rest_api->delete_devices( {}, {}, \%outcome, [] ) and $outcome{response_error} =~ /Expecting ARRAY reference/ ),
    1, 'Incorrect object type reference exception' );
is( ( not $rest_api->delete_devices( [], [], \%outcome, [] ) and $outcome{response_error} =~ /Expecting HASH reference/ ),
    1, 'Incorrect object type reference exception' );
is $rest_api->delete_devices( [], {}, [], [] ), 0, 'Incorrect object type reference exception';
is( ( not $rest_api->delete_devices( [], {}, \%outcome, {} ) and $outcome{response_error} =~ /Expecting ARRAY reference/ ),
    1, 'Incorrect object type reference exception' );

is( ( not $rest_api->delete_devices( "", {}, \%outcome, [] ) and $outcome{response_error} =~ /Expecting ARRAY reference/ ),
    1, 'Incorrect object type reference exception' );
is( ( not $rest_api->delete_devices( [], "", \%outcome, [] ) and $outcome{response_error} =~ /Expecting HASH reference/ ),
    1, 'Incorrect object type reference exception' );
is $rest_api->delete_devices( [], {}, "", [] ), 0, 'Incorrect object type reference exception';
is( ( not $rest_api->delete_devices( [], {}, \%outcome, "" ) and $outcome{response_error} =~ /Expecting ARRAY reference/ ),
    1, 'Incorrect object type reference exception' );

# ------------------------------------------------------------------------------------
# Simple positive test case  - creates some devices and then deletes them
# ------------------------------------------------------------------------------------

my $devicename_base = "__RAPID_test_device_";
my $devicename      = $devicename_base . time();
my $hostname_base   = "__RAPID_test_host_";
my $hostname        = $hostname_base . time();

# delete any test devices first - shouldn't be any if this test works, but just in case test fails
my $delete_command = "$PSQL -c \"delete from device where displayname like '${devicename_base}_%';\" gwcollagedb;";
print "Removing collage test devices ${devicename_base}* with '$delete_command'\n";
if ( system($delete_command ) >> 8 ) { print "Command '$delete_command' failed!! Quitting\n"; exit; }
@devices = (
    {
	'monitorServers' => [ { 'monitorServerName' => 'localhost', 'ip' => '127.0.0.1' } ],
	'identification' => '111.111.222.222',
	'displayName'    => $devicename,
	'description'    => "RAPID test device"
    },
    {
	'monitorServers' => [ { 'monitorServerName' => 'localhost', 'ip' => '127.0.0.1' } ],
	'identification' => '333.333.444.444',
	'displayName'    => "$devicename two",
	'description'    => "RAPID test device two"
    }
);

# create the test devices
is $rest_api->upsert_devices( \@devices, {}, \%outcome, \@results ), 1, "Successfully created devices $devicename and $devicename two";

# get the id's of the test devices
is $rest_api->get_devices( [], { query => "displayname like '__RAPID_test_device_%'" }, \%outcome, \%results ), 1,
  "Successfully retrieved test devices - identification: " . join(', ', sort keys %results );
$logger->debug("\\\%outcome:\n", Dumper \%outcome);
$logger->debug("\\\%results:\n", Dumper \%results);
my @deviceids = ( keys %results );

# delete the devices
is $rest_api->delete_devices( \@deviceids, {}, \%outcome, \@results ), 1, "Successfully deleted devices in list: @deviceids";
$logger->debug("\\\%outcome:\n", Dumper \%outcome);
$logger->debug("\\\@results:\n", Dumper \@results);

# delete the now non-existent devices ids again
is $rest_api->delete_devices( \@deviceids, {}, \%outcome, \@results ), 0,
  "Failure deleting already-removed non existent devices in list: @deviceids";

# Verify that logout (that is, destroying any server-side authentication tokens) works as planned.
is $rest_api->DESTROY(), 1, 'Logout';

# Now have Perl itself call the destructor.  This will call $rest_api->DESTROY() a second time.
$rest_api = undef;

# ------------------------------------------------------------------------------------
$logger->debug("----- END delete_devices() tests ----");
done_testing();

