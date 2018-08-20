#!/usr/local/groundwork/perl/bin/perl -w --

# This test script tests the REST API Perl module upsert_hosts() routine

die "$0: Please set PGPASSWORD in the environment before running this test script.\n" if not $ENV{'PGPASSWORD'};
my $PSQL="/usr/local/groundwork/postgresql/bin/psql"; # change if necessary

use warnings;
use strict;

# FIX MAJOR:  This is one form of workaround for the underscore-to-dash translation issue.
# We have solved that problem instead by renaming the APP_NAME and GWOS_API_TOKEN header
# names, which were used in an early version of the Foundation REST API support for tokens,
# but this is just here to demonstrate that we are not depending on the HTTP::Headers
# package to make such translations.
use HTTP::Headers;
$HTTP::Headers::TRANSLATE_UNDERSCORE = 0;

use GW::RAPID;
use Test::More;
use Test::Deep;
use Test::Exception;
use Data::Dumper; $Data::Dumper::Indent   = 1; $Data::Dumper::Sortkeys = 1;
use File::Basename; my $requestor = "RAPID-" . basename($0);

# use lib '/usr/local/groundwork/core/foundation/api/perl/lib';
use Log::Log4perl qw(get_logger);

Log::Log4perl::init('GW_RAPID.log4perl.conf');

my $logger = get_logger("GW.RAPID.module");
$logger->debug("----- START upsert_hosts() tests ----");
my %outcome = ();
my @results = ();

# ----------------------------------------------------------------------------------------------------------------------

# initialize the REST API 
my $rest_api = GW::RAPID->new( undef, undef, undef, undef, $requestor, { access => '/usr/local/groundwork/config/ws_client.properties' });

# Exception testing
# Usage: $rest_api->upsert_hosts( \@hosts, \%opts, \%outcome, \@results );

#is $rest_api->upsert_hosts("arg1"), 0, 'Missing arguments exception';
#is( ( not $rest_api->upsert_hosts( "arg1", "arg2", \%outcome, "arg4", "arg5" ) and $outcome{response_error} =~ /Invalid number of args/ ), 1, 'Too many arguments exception' );
#is( ( not $rest_api->upsert_hosts( undef, {}, \%outcome, [] ) and $outcome{response_error} =~ /Undefined arg/ ), 1, 'Undefined argument exception' );
#is( ( not $rest_api->upsert_hosts( [], undef, \%outcome, [] ) and $outcome{response_error} =~ /Undefined arg/ ), 1, 'Undefined argument exception' );
#is $rest_api->upsert_hosts( [], {}, undef, [] ), 0, 'Undefined argument exception'; is( ( not $rest_api->upsert_hosts( [], {}, \%outcome, undef ) and $outcome{response_error} =~ /Undefined arg/ ), 1, 'Undefined argument exception' );
#is( ( not $rest_api->upsert_hosts( {}, {}, \%outcome, [] ) and $outcome{response_error} =~ /Expecting ARRAY reference/ ), 1, 'Incorrect arguments object type reference exception' );
#is( ( not $rest_api->upsert_hosts( [], [], \%outcome, [] ) and $outcome{response_error} =~ /Expecting HASH reference/ ), 1, 'Incorrect arguments object type reference exception' );
#is $rest_api->upsert_hosts( [], {}, [], [] ), 0, 'Incorrect arguments object type reference exception';
#is( ( not $rest_api->upsert_hosts( [], {}, \%outcome, {} ) and $outcome{response_error} =~ /Expecting ARRAY reference/ ), 1, 'Incorrect arguments object type reference exception' );
#is( ( not $rest_api->upsert_hosts( "", {}, \%outcome, [] ) and $outcome{response_error} =~ /Expecting ARRAY reference/ ), 1, 'Incorrect arguments object type reference exception' );
#is( ( not $rest_api->upsert_hosts( [], "", \%outcome, [] ) and $outcome{response_error} =~ /Expecting HASH reference/ ), 1, 'Incorrect arguments object type reference exception' );
#is $rest_api->upsert_hosts( [], {}, "", [] ), 0, 'Incorrect arguments object type reference exception'; is( ( not $rest_api->upsert_hosts( [], {}, \%outcome, "" ) and $outcome{response_error} =~ /Expecting ARRAY reference/ ), 1, 'Incorrect arguments object type reference exception' );

# ------------------------------------------------------------------------------------
# Simple positive test case  - creates a host
# ------------------------------------------------------------------------------------
my $hostname_base = "__RAPID_test_host_";
my $now = time();
my $hostname = $hostname_base . $now;
my $hostname_2 = $hostname . "_2";

# delete any test hosts first
my $delete_command = "$PSQL -c \"delete from host where hostname like '${hostname_base}_%';\" gwcollagedb;";
print "Removing collage test hosts ${hostname_base}* with '$delete_command'\n";
if ( system($delete_command ) >> 8 ) { print "Command '$delete_command' failed!! Quitting\n"; exit; }
my @hosts = (
    {
	"hostName"             => $hostname,
	"description"          => "CREATED at " . localtime,
	"monitorStatus"        => "UP",
	"appType"              => "NAGIOS",
	"deviceIdentification" => "1.2.3.4",
	"monitorServer"        => "localhost",
	"deviceDisplayName"    => $hostname,
	"properties"           => { "Latency" => "125", "UpdatedBy" => "admin", "Comments" => "Testing host #1" }
    },
    {
	"hostName"             => $hostname_2,
	"description"          => "CREATED at " . localtime,
	"monitorStatus"        => "DOWN",
	"appType"              => "NAGIOS",
	"deviceIdentification" => "5,6,7,8",
	"monitorServer"        => "localhost",
	"deviceDisplayName"    => $hostname_2,
	"properties"           => { "Latency" => "150", "UpdatedBy" => "admin", "Comments" => "Testing host #2" }
    }
);

# create a test host
# Usage: $rest_api->upsert_hosts( \@hosts, \%opts, \%outcome, \@results );
is $rest_api->upsert_hosts(  \@hosts, {}, \%outcome, \@results ), 1, "Successfully created hosts $hostname and $hostname_2";
$logger->debug("\\\%outcome:\n", Dumper \%outcome);
$logger->debug("\\\@results:\n", Dumper \@results);
# perform deep structural test of results here ?

# ------------------------------------------------------------------------------------
# update that freshly created test host 
# ------------------------------------------------------------------------------------
@hosts = (
    {
	"hostName"             => $hostname,
	"description"          => "UPDATED at " . localtime,                                                # <<<< UPDATE
	"monitorStatus"        => "UP",
	"appType"              => "NAGIOS",
	"deviceIdentification" => "1.2.3.4",
	"monitorServer"        => "localhost",
	"deviceDisplayName"    => $hostname,
	"properties"           => { "Latency" => "125", "UpdatedBy" => "admin", "Comments" => "Testing" }
    }
);

is $rest_api->upsert_hosts(  \@hosts, {}, \%outcome, \@results ), 1, "Successfully updated host $hostname";
$logger->debug("\\\%outcome:\n", Dumper \%outcome);
$logger->debug("\\\@results:\n", Dumper \@results);
# perform deep structural test of results here ?

# ------------------------------------------------------------------------------------
# try to create a host with malformed instructions - one-host missing required property test
@hosts = (
    {
	## "hostName" => $hostname, # <<< INTENTIONAL OMISSION
	"description"          => "SHOULD NOT SEE THIS IN DATABASE: Created at " . localtime,
	"monitorStatus"        => "UP",
	"appType"              => "NAGIOS",
	"deviceIdentification" => "1.2.3.4",
	"monitorServer"        => "localhost",
	"deviceDisplayName"    => $hostname,
	"properties"           => { "Latency" => "125", "UpdatedBy" => "admin", "Comments" => "Testing" }
    }
);

is $rest_api->upsert_hosts(  \@hosts, {}, \%outcome, \@results ), 0, "Failed to create one single host due to missing required property 'hostName'";
$logger->debug("\\\%outcome:\n", Dumper \%outcome);
$logger->debug("\\\@results:\n", Dumper \@results);

# ------------------------------------------------------------------------------------
# try to create a host with malformed instructions - two-host missing required property test
my $hostname_3 = $hostname . "_3";
@hosts = (
    {
	## "hostName" => $hostname, # <<< INTENTIONAL OMISSION
	"description"          => "SHOULD NOT SEE THIS IN DATABASE: Created at " . localtime,
	"monitorStatus"        => "UP",
	"appType"              => "NAGIOS",
	"deviceIdentification" => "1.2.3.4",
	"monitorServer"        => "localhost",
	"deviceDisplayName"    => $hostname,
	"properties"           => { "Latency" => "125", "UpdatedBy" => "admin", "Comments" => "Testing" }
    },
    {
	"hostName" => $hostname_3,
	"description"          => "SHOULD NOT SEE THIS IN DATABASE: Created at " . localtime,
	"monitorStatus"        => "UP",
	"appType"              => "NAGIOS",
	## "deviceIdentification" => "1.2.3.4", # <<< INTENTIONAL OMISSION
	"monitorServer"        => "localhost",
	"deviceDisplayName"    => $hostname_3,
	"properties"           => { "Latency" => "125", "UpdatedBy" => "admin", "Comments" => "Testing" }
    }
);

is $rest_api->upsert_hosts(  \@hosts, {}, \%outcome, \@results ), 0, "Failed to create hosts, due to missing required property 'hostName' or 'deviceIdentification'";
$logger->debug("\\\%outcome:\n", Dumper \%outcome);
$logger->debug("\\\@results:\n", Dumper \@results);

# ------------------------------------------------------------------------------------

# MORE TESTS TBD

# ------------------------------------------------------------------------------------

# Verify that logout (that is, destroying any server-side authentication tokens) works as planned.
is $rest_api->DESTROY(), 1, 'Logout';

# Now have Perl itself call the destructor.  This will call $rest_api->DESTROY() a second time.
$rest_api = undef;

$logger->debug("----- END upsert_hosts() tests ----");
done_testing();

