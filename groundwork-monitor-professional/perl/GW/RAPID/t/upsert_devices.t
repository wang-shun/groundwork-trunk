#!/usr/local/groundwork/perl/bin/perl -w --

# This test script tests the REST API Perl module upsert_devices() routine

die "$0: Please set PGPASSWORD in the environment before running this test script.\n" if not $ENV{'PGPASSWORD'};
my $PSQL = "/usr/local/groundwork/postgresql/bin/psql";    # change if necessary

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
$logger->debug("----- START upsert_devices() tests ----");
my ( @devices, %outcome, @results ) = ();

# ----------------------------------------------------------------------------------------------------------------------
# initialize the REST API
my $rest_api = GW::RAPID->new( undef, undef, undef, undef, $requestor, { access => '/usr/local/groundwork/config/ws_client.properties' });

# Exception testing
is $rest_api->upsert_devices("arg1"), 0, 'Missing arguments exception';
is( ( not $rest_api->upsert_devices( "arg1", "arg2", \%outcome, "arg4", "arg5" ) and $outcome{response_error} =~ /Invalid number of args/ ),
    1, 'Too many arguments exception' );

is( ( not $rest_api->upsert_devices( undef, {}, \%outcome, [] ) and $outcome{response_error} =~ /Undefined arg/ ),
    1, 'Undefined argument exception' );
is( ( not $rest_api->upsert_devices( [], undef, \%outcome, [] ) and $outcome{response_error} =~ /Undefined arg/ ),
    1, 'Undefined argument exception' );
is $rest_api->upsert_devices( [], {}, undef, [] ), 0, 'Undefined argument exception';
is( ( not $rest_api->upsert_devices( [], {}, \%outcome, undef ) and $outcome{response_error} =~ /Undefined arg/ ),
    1, 'Undefined argument exception' );

is( ( not $rest_api->upsert_devices( {}, {}, \%outcome, [] ) and $outcome{response_error} =~ /Expecting ARRAY reference/ ),
    1, 'Incorrect object type reference exception' );
is( ( not $rest_api->upsert_devices( [], [], \%outcome, [] ) and $outcome{response_error} =~ /Expecting HASH reference/ ),
    1, 'Incorrect object type reference exception' );
is $rest_api->upsert_devices( [], {}, [], [] ), 0, 'Incorrect object type reference exception';
is( ( not $rest_api->upsert_devices( [], {}, \%outcome, {} ) and $outcome{response_error} =~ /Expecting ARRAY reference/ ),
    1, 'Incorrect object type reference exception' );

is( ( not $rest_api->upsert_devices( "", {}, \%outcome, [] ) and $outcome{response_error} =~ /Expecting ARRAY reference/ ),
    1, 'Incorrect object type reference exception' );
is( ( not $rest_api->upsert_devices( [], "", \%outcome, [] ) and $outcome{response_error} =~ /Expecting HASH reference/ ),
    1, 'Incorrect object type reference exception' );
is $rest_api->upsert_devices( [], {}, "", [] ), 0, 'Incorrect object type reference exception';
is( ( not $rest_api->upsert_devices( [], {}, \%outcome, "" ) and $outcome{response_error} =~ /Expecting ARRAY reference/ ),
    1, 'Incorrect object type reference exception' );

# ------------------------------------------------------------------------------------
# Simple positive test case  - creates a device
# ------------------------------------------------------------------------------------
my $devicename_base = "__RAPID_test_device_";
my $devicename      = $devicename_base . time();
my $hostname_base   = "__RAPID_test_host_";
my $hostname        = $hostname_base . time();

# delete any test hosts first
#my $delete_command = "$PSQL -c \"delete from host where hostname like '${hostname_base}_%';\" gwcollagedb;" ;
#print "Removing collage test hosts ${hostname_base}* with '$delete_command'\n";
#if ( system( $delete_command ) >> 8  )  { print "Command '$delete_command' failed!! Quitting\n" ; exit ; }
#%instructions = ( hosts => [ { "hostName" => $hostname, "description" => "hn1", "monitorStatus" => "UP",
#                                  "appType" => "NAGIOS", "deviceIdentification"  => "10.20.30.40", "monitorServer" => "localhost", "deviceDisplayName" => $hostname },
#                              { "hostName" => $hostname . "_two", "description" => "hn2", "monitorStatus" => "UP",
#                                  "appType" => "NAGIOS", "deviceIdentification"  => "50.60.70.80", "monitorServer" => "localhost", "deviceDisplayName" => $hostname . "_two" } ] );
#
## create two test hosts
#is upsert_hosts( \%instructions, {}, \%outcome, \@results ), 1, "Successfully created two hosts: $hostname and ${hostname}_two";

# delete any test device first
my $delete_command = "$PSQL -c \"delete from device where displayname like '${devicename_base}_%';\" gwcollagedb;";
print "Removing collage test devices ${devicename_base}* with '$delete_command'\n";
if ( system($delete_command ) >> 8 ) { print "Command '$delete_command' failed!! Quitting\n"; exit; }
@devices = (
    {
	'monitorServers' => [ { 'monitorServerName' => 'localhost', 'ip' => '127.0.0.1' } ],
	##, 'hosts' => [ { $hostname => 'hn1' }, { $hostname. "_two" => 'hn2' } ],
	'identification' => '111.111.222.222',
	'displayName'    => $devicename,
	'description'    => "RAPID test device"
    }
);

# create the test device
is $rest_api->upsert_devices( \@devices, {}, \%outcome, \@results ), 1, "Successfully created device $devicename";

# ------------------------------------------------------------------------------------
# update that freshly created test device
# ------------------------------------------------------------------------------------
@devices = (
    {
	'monitorServers' => [ { 'monitorServerName' => 'localhost', 'ip' => '127.0.0.1' } ],
	'identification' => '111.111.222.222',
	'displayName'    => $devicename,
	'description'    => "UPDATED description for RAPID test device"    # <<< UPDATE
    }
);

is $rest_api->upsert_devices( \@devices, {}, \%outcome, \@results ), 1, "Successfully updated device $devicename";

# ------------------------------------------------------------------------------------
# try to create a host with malformed instructions - missing required property test
@devices = (
    {
	'monitorServers' => [ { 'monitorServerName' => 'localhost', 'ip' => '127.0.0.1' } ],
	## 'identification' => '111.111.222.222', ### OMISSION
	'displayName' => $devicename,
	'description' => "some description"
    }
);

is $rest_api->upsert_devices( \@devices, {}, \%outcome, \@results ), 0, "Failed to create device due to missing required property 'identification'";

# Verify that logout (that is, destroying any server-side authentication tokens) works as planned.
is $rest_api->DESTROY(), 1, 'Logout';

# Now have Perl itself call the destructor.  This will call $rest_api->DESTROY() a second time.
$rest_api = undef;

# ------------------------------------------------------------------------------------
$logger->debug("----- END upsert_devices() tests ----");
done_testing();

