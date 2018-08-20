#!/usr/local/groundwork/perl/bin/perl -w --

# This test script tests the REST API Perl module clear_hostgroups() routine

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
$logger->debug("----- START clear_hostgroups() tests ----");
my ( @hostgroups, @hostgroupnames, %outcome, %results, @results ) = ();

# ----------------------------------------------------------------------------------------------------------------------
# initialize the REST API
my $rest_api = GW::RAPID->new( undef, undef, undef, undef, $requestor, { access => '/usr/local/groundwork/config/ws_client.properties' });
# Exception testing

is $rest_api->clear_hostgroups("arg1"), 0, 'Missing arguments exception';
is ((not $rest_api->clear_hostgroups( "arg1", "arg2", \%outcome, "arg4", "arg5" ) and $outcome{response_error} =~ /Invalid number of args/), 1, 'Too many arguments exception');
is ((not $rest_api->clear_hostgroups( undef, {}, \%outcome, [] ) and $outcome{response_error} =~ /Undefined arg/), 1, 'Undefined argument exception');
is ((not $rest_api->clear_hostgroups( [], undef, \%outcome, [] ) and $outcome{response_error} =~ /Undefined arg/), 1, 'Undefined argument exception');
is $rest_api->clear_hostgroups( [], {}, undef, [] ), 0, 'Undefined argument exception';
is ((not $rest_api->clear_hostgroups( [], {}, \%outcome, undef ) and $outcome{response_error} =~ /Undefined arg/), 1, 'Undefined argument exception');
is ((not $rest_api->clear_hostgroups( {}, {}, \%outcome, [] ) and $outcome{response_error} =~ /Expecting ARRAY reference/), 1, 'Incorrect object type reference exception');
is ((not $rest_api->clear_hostgroups( [], [], \%outcome, [] ) and $outcome{response_error} =~ /Expecting HASH reference/), 1, 'Incorrect object type reference exception');
is $rest_api->clear_hostgroups( [], {}, [], [] ), 0, 'Incorrect object type reference exception';
is ((not $rest_api->clear_hostgroups( [], {}, \%outcome, {} ) and $outcome{response_error} =~ /Expecting ARRAY reference/), 1, 'Incorrect object type reference exception');
is ((not $rest_api->clear_hostgroups( "", {}, \%outcome, [] ) and $outcome{response_error} =~ /Expecting ARRAY reference/), 1, 'Incorrect object type reference exception');
is ((not $rest_api->clear_hostgroups( [], "", \%outcome, [] ) and $outcome{response_error} =~ /Expecting HASH reference/), 1, 'Incorrect object type reference exception');
is $rest_api->clear_hostgroups( [], {}, "", [] ), 0, 'Incorrect object type reference exception';
is ((not $rest_api->clear_hostgroups( [], {}, \%outcome, "" ) and $outcome{response_error} =~ /Expecting ARRAY reference/), 1, 'Incorrect object type reference exception');

# ------------------------------------------------------------------------------------------
# Simple positive test case  - creates a hostgroup, with host localhost in it, then clear it
# ------------------------------------------------------------------------------------------
my $hostgroupname_base = "__RAPID_test_hostgroup_";
my $hostgroup          = $hostgroupname_base . time();

# delete any test hostgroups first
my $delete_command = "$PSQL -c \"delete from hostgroup where name like '${hostgroupname_base}_%';\" gwcollagedb;";
print "Removing collage test hostgroups ${hostgroupname_base}* with '$delete_command'\n";
if ( system($delete_command ) >> 8 ) { print "Command '$delete_command' failed!! Quitting\n"; exit; }
@hostgroups = (
    {
	"name"        => $hostgroup,
	"description" => "CREATED at " . localtime,
	"alias"       => "Alias for $hostgroup",
	"hosts"       => [ { "hostName" => "localhost" } ]
    },
    {
	"name"        => "$hostgroup" . "_two",
	"description" => "CREATED at " . localtime,
	"alias"       => "Alias for $hostgroup" . "_two",
	"hosts"       => [ { "hostName" => "localhost" } ]
    }
);

# create a test hostgroup
is $rest_api->upsert_hostgroups( \@hostgroups, {}, \%outcome, \@results ), 1,
  "Successfully created hostgroups $hostgroup and ${hostgroup}_two with localhost as a member";

# ------------------------------------------------------------------------------------
# clear/empty that freshly created test hostgroup
# ------------------------------------------------------------------------------------
@hostgroupnames = ( $hostgroup, $hostgroup . "_two" );
is $rest_api->clear_hostgroups( \@hostgroupnames, {}, \%outcome, \@results ), 1, "Successfully emptied hostgroups @hostgroupnames";
# NOTE - status viewer didn't seem to update to reflect the empty hostgroups yet the data was correctly updated.
# Is this one of the issues that will be fixed once AOP stuff in the Foundation REST API is addressed?

# ------------------------------------------------------------------------------------
# clear/empty that freshly created test hostgroup AGAIN (to make sure that the
# clearing of hostgroups didn't completely delete them instead, this call should
# succeed because the hostgroups should still exist, not fail because they were
# unintentionally deleted just above)
# ------------------------------------------------------------------------------------
is $rest_api->clear_hostgroups( \@hostgroupnames, {}, \%outcome, \@results ), 1, "Successfully emptied AGAIN hostgroups @hostgroupnames";
$logger->debug("\\\%outcome:\n", Dumper \%outcome);
$logger->debug("\\\@results:\n", Dumper \@results);

# ------------------------------------------------------------------------------------
# clear/empty hostgroups that partially don't exist
# ------------------------------------------------------------------------------------
@hostgroupnames = ( $hostgroup, $hostgroup . "_I_SAY_PARSTA_YOU_SAY_PASTA" );
is $rest_api->clear_hostgroups( \@hostgroupnames, {}, \%outcome, \@results ), 0, "Failed to empty non existent hostgroup";

# Verify that logout (that is, destroying any server-side authentication tokens) works as planned.
is $rest_api->DESTROY(), 1, 'Logout';

# Now have Perl itself call the destructor.  This will call $rest_api->DESTROY() a second time.
$rest_api = undef;

# ------------------------------------------------------------------------------------
$logger->debug("----- END clear_hostgroups() tests ----");
done_testing();

