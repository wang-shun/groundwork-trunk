#!/usr/local/groundwork/perl/bin/perl -w --

# This test script tests the REST API Perl module upsert_hostgroups() routine

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
$logger->debug("----- START upsert_hostgroups() tests ----");
my ( @hostgroups, %outcome, @results ) = ();

# ----------------------------------------------------------------------------------------------------------------------
# initialize the REST API
my $rest_api = GW::RAPID->new( undef, undef, undef, undef, $requestor, { access => '/usr/local/groundwork/config/ws_client.properties' });

# Exception testing

is $rest_api->upsert_hostgroups("arg1"), 0, 'Missing arguments exception';
is(
    ( not $rest_api->upsert_hostgroups( "arg1", "arg2", \%outcome, "arg4", "arg5" ) and $outcome{response_error} =~ /Invalid number of args/ ),
    1,
    'Too many arguments exception'
);

is( ( not $rest_api->upsert_hostgroups( undef, {}, \%outcome, [] ) and $outcome{response_error} =~ /Undefined arg/ ),
    1, 'Undefined instructions argument exception' );
is( ( not $rest_api->upsert_hostgroups( [], undef, \%outcome, [] ) and $outcome{response_error} =~ /Undefined arg/ ),
    1, 'Undefined results argument exception' );
is $rest_api->upsert_hostgroups( [], {}, undef, [] ), 0, 'Undefined results argument exception';
is( ( not $rest_api->upsert_hostgroups( [], {}, \%outcome, undef ) and $outcome{response_error} =~ /Undefined arg/ ),
    1, 'Undefined results argument exception' );

is( ( not $rest_api->upsert_hostgroups( {}, {}, \%outcome, [] ) and $outcome{response_error} =~ /Expecting ARRAY reference/ ),
    1, 'Incorrect object type reference exception' );
is( ( not $rest_api->upsert_hostgroups( [], [], \%outcome, [] ) and $outcome{response_error} =~ /Expecting HASH reference/ ),
    1, 'Incorrect object type reference exception' );
is $rest_api->upsert_hostgroups( [], {}, [], [] ), 0, 'Incorrect object type reference exception';
is( ( not $rest_api->upsert_hostgroups( [], {}, \%outcome, {} ) and $outcome{response_error} =~ /Expecting ARRAY reference/ ),
    1, 'Incorrect object type reference exception' );

is( ( not $rest_api->upsert_hostgroups( "", {}, \%outcome, [] ) and $outcome{response_error} =~ /Expecting ARRAY reference/ ),
    1, 'Incorrect object type reference exception' );
is( ( not $rest_api->upsert_hostgroups( [], "", \%outcome, [] ) and $outcome{response_error} =~ /Expecting HASH reference/ ),
    1, 'Incorrect object type reference exception' );
is $rest_api->upsert_hostgroups( [], {}, "", [] ), 0, 'Incorrect object type reference exception';
is( ( not $rest_api->upsert_hostgroups( [], {}, \%outcome, "" ) and $outcome{response_error} =~ /Expecting ARRAY reference/ ),
    1, 'Incorrect object type reference exception' );

# ------------------------------------------------------------------------------------
# Simple positive test case  - creates a hostgroup
# ------------------------------------------------------------------------------------
my $hostgroup_name_base = "__RAPID_test_hostgroup_";
my $hostgroup           = $hostgroup_name_base . time();

# delete any test hostgroups first
my $delete_command = "$PSQL -c \"delete from hostgroup where name like '${hostgroup_name_base}_%';\" gwcollagedb;";
print "Removing collage test hostgroup(s) ${hostgroup_name_base}* with '$delete_command'\n";
if ( system($delete_command ) >> 8 ) { print "Command '$delete_command' failed!! Quitting\n"; exit; }
@hostgroups = (
    {
	"name"        => $hostgroup,
	"description" => "CREATED at " . localtime,
	"alias"       => "Alias for $hostgroup"
    }
);

# create a test hostgroup
is $rest_api->upsert_hostgroups( \@hostgroups, {}, \%outcome, \@results ), 1, "Successfully created hostgroup $hostgroup";

# ------------------------------------------------------------------------------------
# update that freshly created test hostgroup by adding a host to it
# ------------------------------------------------------------------------------------
# Note that the host must already exist, for this to work.  So either we need to know
# that the selected host always exists, or we need to explicitly add it here.
my $second_hostname = 'bsm-host';
@hostgroups = (
    {
	"name"        => $hostgroup,
	"description" => "CREATED at " . localtime,
	"alias"       => "Alias for $hostgroup",
	"hosts"       => [ { hostName => 'localhost' }, { hostName => $second_hostname } ]
    }
);

# FIX MAJOR:  This unit test is currently failing, because $second_hostname has not been
# either previously auto-discovered or created in this script.  We need to keep this
# broken test (though reverse the sense of the test, so we expect a failure), and then add
# a similar test where we do attempt to add muliple already-known hosts to the hostgroup.
is $rest_api->upsert_hostgroups( \@hostgroups, {}, \%outcome, \@results ), 1, "Successfully updated hostgroup $hostgroup by adding localhost";
$logger->debug("\\\%outcome:\n", Dumper \%outcome);
$logger->debug("\\\@results:\n", Dumper \@results);

# ------------------------------------------------------------------------------------
# update that freshly created test hostgroup by adding a host to it
# ------------------------------------------------------------------------------------
@hostgroups = (
    {
	"name"        => $hostgroup,
	"description" => "CREATED at " . localtime,
	"alias"       => "Alias for $hostgroup",
	"hosts"       => [ { hostName => "__RHUBARB__" } ]
    }
);
is $rest_api->upsert_hostgroups( \@hostgroups, {}, \%outcome, \@results ), 0, "Failed to update hostgroup $hostgroup by adding a non-existent host __RHUBARB__";

# ------------------------------------------------------------------------------------
# try to create a hostgroup with malformed instructions - missing required property test
@hostgroups = (
	{
	    ## "name" => $hostgroup, # <<< MISSING
	    "description" => "CREATED at " . localtime,
	    "alias"       => "Alias for $hostgroup"
	}
);

is $rest_api->upsert_hostgroups( \@hostgroups, {}, \%outcome, \@results ), 0, "Failed to create hostgroup due to missing required property 'name'";

# MORE TESTS TBD

# Verify that logout (that is, destroying any server-side authentication tokens) works as planned.
is $rest_api->DESTROY(), 1, 'Logout';

# Now have Perl itself call the destructor.  This will call $rest_api->DESTROY() a second time.
$rest_api = undef;

# ------------------------------------------------------------------------------------
$logger->debug("----- END upsert_hostgroups() tests ----");
done_testing();

