#!/usr/local/groundwork/perl/bin/perl -w --

# This test script tests the REST API Perl module delete_hosts() routine

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
$logger->debug("----- START delete_hosts() tests ----");
my ( @hosts, @hostnames, %outcome, %results, @results ) = ();

# ----------------------------------------------------------------------------------------------------------------------
# initialize the REST API
my $rest_api = GW::RAPID->new( undef, undef, undef, undef, $requestor, { access => '/usr/local/groundwork/config/ws_client.properties' });

# Exception testing

is $rest_api->delete_hosts("arg1"), 0, 'Missing arguments exception';
is( ( not $rest_api->delete_hosts( "arg1", "arg2", \%outcome, "arg4", "arg5" ) and $outcome{response_error} =~ /Invalid number of args/ ), 1, 'Too many arguments exception' );
is( ( not $rest_api->delete_hosts( undef, {}, \%outcome, [] ) and $outcome{response_error} =~ /Undefined arg/ ), 1, 'Undefined argument exception' );
is( ( not $rest_api->delete_hosts( [], undef, \%outcome, [] ) and $outcome{response_error} =~ /Undefined arg/ ), 1, 'Undefined argument exception' );
is $rest_api->delete_hosts( [], {}, undef, [] ), 0, 'Undefined argument exception'; is( ( not $rest_api->delete_hosts( [], {}, \%outcome, undef ) and $outcome{response_error} =~ /Undefined arg/ ), 1, 'Undefined argument exception' );
is( ( not $rest_api->delete_hosts( {}, {}, \%outcome, [] ) and $outcome{response_error} =~ /Expecting ARRAY reference/ ), 1, 'Incorrect object type reference exception' );
is( ( not $rest_api->delete_hosts( [], [], \%outcome, [] ) and $outcome{response_error} =~ /Expecting HASH reference/ ), 1, 'Incorrect object type reference exception' );
is $rest_api->delete_hosts( [], {}, [], [] ), 0, 'Incorrect object type reference exception';
is( ( not $rest_api->delete_hosts( [], {}, \%outcome, {} ) and $outcome{response_error} =~ /Expecting ARRAY reference/ ), 1, 'Incorrect object type reference exception' );
is( ( not $rest_api->delete_hosts( "", {}, \%outcome, [] ) and $outcome{response_error} =~ /Expecting ARRAY reference/ ), 1, 'Incorrect object type reference exception' );
is( ( not $rest_api->delete_hosts( [], "", \%outcome, [] ) and $outcome{response_error} =~ /Expecting HASH reference/ ), 1, 'Incorrect object type reference exception' );
is $rest_api->delete_hosts( [], {}, "", [] ), 0, 'Incorrect object type reference exception';
is( ( not $rest_api->delete_hosts( [], {}, \%outcome, "" ) and $outcome{response_error} =~ /Expecting ARRAY reference/ ), 1, 'Incorrect object type reference exception' );

# ------------------------------------------------------------------------------------
# Simple positive test case  - creates a host and then deletes it
# ------------------------------------------------------------------------------------
my $hostname_base = "__RAPID_test_host_";
my $hostname      = $hostname_base . time();

# delete any test hosts first
my $delete_command = "$PSQL -c \"delete from host where hostname like '${hostname_base}_%';\" gwcollagedb;";
print "Removing collage test hosts ${hostname_base}* with '$delete_command'\n";
if ( system($delete_command ) >> 8 ) { print "Command '$delete_command' failed!! Quitting\n"; exit; }
@hosts = (
    {
	"hostName"             => $hostname,
	"description"          => "CREATED at " . localtime,
	"monitorStatus"        => "UP",
	"appType"              => "NAGIOS",
	"deviceIdentification" => "1.2.3.4",
	"monitorServer"        => "localhost",
	"deviceDisplayName"    => $hostname,
	"properties"           => { "Latency" => "125", "UpdatedBy" => "admin", "Comments" => "Testing" }
    }
);

# create a test host
is $rest_api->upsert_hosts( \@hosts, {}, \%outcome, \@results ), 1, "Successfully created host $hostname";
$logger->debug("\\\%outcome:\n", Dumper \%outcome);
$logger->debug("\\\@results:\n", Dumper \@results);

# ------------------------------------------------------------------------------------
# delete that freshly created test host
# ------------------------------------------------------------------------------------
@hostnames = ($hostname);
is $rest_api->delete_hosts( \@hostnames, {}, \%outcome, \@results ), 1, "Successfully deleted hosts in list: @hostnames";
$logger->debug("\\\%outcome:\n", Dumper \%outcome);
$logger->debug("\\\@results:\n", Dumper \@results);

# ------------------------------------------------------------------------------------
# delete the now non-existent host again
# ------------------------------------------------------------------------------------
#is $rest_api->delete_hosts( \@hostnames, {}, \%outcome, \@results ), 1, "Successfully deleted hosts in list: @hostnames";
is $rest_api->delete_hosts( \@hostnames, {}, \%outcome, \@results ), 0, "Failure deleting a host that is not found/doesn't exist";

# delete any test hosts first
$delete_command = "$PSQL -c \"delete from host where hostname like '${hostname_base}_%';\" gwcollagedb;";
print "Removing collage test hosts ${hostname_base}* with '$delete_command'\n";
if ( system($delete_command ) >> 8 ) { print "Command '$delete_command' failed!! Quitting\n"; exit; }
@hosts = (
    {
	"hostName"             => $hostname,
	"description"          => "CREATED at " . localtime,
	"monitorStatus"        => "UP",
	"appType"              => "NAGIOS",
	"deviceIdentification" => "1.2.3.4",
	"monitorServer"        => "localhost",
	"deviceDisplayName"    => $hostname,
	"properties"           => { "Latency" => "125", "UpdatedBy" => "admin", "Comments" => "Testing" }
    },
    {
	"hostName"             => "${hostname}_number2",
	"description"          => "CREATED at " . localtime,
	"monitorStatus"        => "UP",
	"appType"              => "NAGIOS",
	"deviceIdentification" => "1.2.3.4",
	"monitorServer"        => "localhost",
	"deviceDisplayName"    => "${hostname}_number2",
	"properties"           => { "Latency" => "125", "UpdatedBy" => "admin", "Comments" => "Testing" }
    }

);

# create two test hosts
is $rest_api->upsert_hosts( \@hosts, {}, \%outcome, \@results ), 1, "Successfully created hosts $hostname and ${hostname}_number2";

# ------------------------------------------------------------------------------------
# delete both hosts
# ------------------------------------------------------------------------------------
@hostnames = ( $hostname, "${hostname}_number2" );
is $rest_api->delete_hosts( \@hostnames, {}, \%outcome, \@results ), 1, "Successfully deleted hosts in list: @hostnames";
## FIX MAJOR:  Verify the returned data in @results.
$logger->debug("\\\%outcome:\n", Dumper \%outcome);
$logger->debug("\\\@results:\n", Dumper \@results);

# ------------------------------------------------------------------------------------
# try to delete both hosts but force an error
# ------------------------------------------------------------------------------------
# create two test hosts again
is $rest_api->upsert_hosts( \@hosts, {}, \%outcome, \@results ), 1, "Successfully created hosts $hostname and ${hostname}_number2";
@hostnames = ( $hostname, "${hostname}_numberTWO" );    # <<< second hostname is deliberately wrong
is $rest_api->delete_hosts( \@hostnames, {}, \%outcome, \@results ), 0, "UNSuccessfully deleted ALL hosts in list: @hostnames";

# MORE TESTS TBD

# Verify that logout (that is, destroying any server-side authentication tokens) works as planned.
is $rest_api->DESTROY(), 1, 'Logout';

# Now have Perl itself call the destructor.  This will call $rest_api->DESTROY() a second time.
$rest_api = undef;

# ------------------------------------------------------------------------------------
$logger->debug("----- END delete_hosts() tests ----");
done_testing();

