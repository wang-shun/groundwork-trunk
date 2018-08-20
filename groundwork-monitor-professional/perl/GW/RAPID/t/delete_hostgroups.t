#!/usr/local/groundwork/perl/bin/perl -w --

# This test script tests the REST API Perl module delete_hostgroups() routine

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
$logger->debug("----- START delete_hostgroups() tests ----");
my ( @hostgroupnames, @hostgroups, %outcome, %results, @results ) = ();

# ----------------------------------------------------------------------------------------------------------------------
# initialize the REST API
my $rest_api = GW::RAPID->new( undef, undef, undef, undef, $requestor, { access => '/usr/local/groundwork/config/ws_client.properties' });

# Exception testing

is $rest_api->delete_hostgroups("arg1"), 0, 'Missing arguments exception';
is(
    ( not $rest_api->delete_hostgroups( "arg1", "arg2", \%outcome, "arg4", "arg5" ) and $outcome{response_error} =~ /Invalid number of args/ ),
    1,
    'Too many arguments exception'
);

is( ( not $rest_api->delete_hostgroups( undef, {}, \%outcome, [] ) and $outcome{response_error} =~ /Undefined arg/ ),
    1, 'Undefined instructions argument exception' );
is( ( not $rest_api->delete_hostgroups( [], undef, \%outcome, [] ) and $outcome{response_error} =~ /Undefined arg/ ),
    1, 'Undefined instructions argument exception' );
is $rest_api->delete_hostgroups( [], {}, undef, [] ), 0, 'Undefined instructions argument exception';
is( ( not $rest_api->delete_hostgroups( [], {}, \%outcome, undef ) and $outcome{response_error} =~ /Undefined arg/ ),
    1, 'Undefined results argument exception' );

is( ( not $rest_api->delete_hostgroups( {}, {}, \%outcome, [] ) and $outcome{response_error} =~ /Expecting ARRAY reference/ ),
    1, 'Incorrect object type reference exception' );
is( ( not $rest_api->delete_hostgroups( [], [], \%outcome, [] ) and $outcome{response_error} =~ /Expecting HASH reference/ ),
    1, 'Incorrect object type reference exception' );
is $rest_api->delete_hostgroups( [], {}, [], [] ), 0, 'Incorrect object type reference exception';
is( ( not $rest_api->delete_hostgroups( [], {}, \%outcome, {} ) and $outcome{response_error} =~ /Expecting ARRAY reference/ ),
    1, 'Incorrect object type reference exception' );

is( ( not $rest_api->delete_hostgroups( "", {}, \%outcome, [] ) and $outcome{response_error} =~ /Expecting ARRAY reference/ ),
    1, 'Incorrect object type reference exception' );
is( ( not $rest_api->delete_hostgroups( [], "", \%outcome, [] ) and $outcome{response_error} =~ /Expecting HASH reference/ ),
    1, 'Incorrect object type reference exception' );
is $rest_api->delete_hostgroups( [], {}, "", [] ), 0, 'Incorrect object type reference exception';
is( ( not $rest_api->delete_hostgroups( [], {}, \%outcome, "" ) and $outcome{response_error} =~ /Expecting ARRAY reference/ ),
    1, 'Incorrect object type reference exception' );

# ------------------------------------------------------------------------------------
# Simple positive test case  - creates a hostgroup and then deletes it
# ------------------------------------------------------------------------------------
my $hostgroupname_base = "__RAPID_test_hostgroup_";
my $hostgroup          = $hostgroupname_base . time();

# delete any test hostgroups first
my $delete_command = "$PSQL -c \"delete from hostgroup where name like '${hostgroupname_base}_%';\" gwcollagedb;";
print "Removing collage test hostgroups ${hostgroupname_base}* with '$delete_command'\n";
if ( system($delete_command ) >> 8 ) { print "Command '$delete_command' failed!! Quitting\n"; exit; }
@hostgroups = ( { "name" => $hostgroup, "description" => "CREATED at " . localtime, "alias" => "Alias for $hostgroup" } );

# create a test hostgroup
is $rest_api->upsert_hostgroups( \@hostgroups, {}, \%outcome, \@results ), 1, "Successfully created hostgroup $hostgroup";

# ------------------------------------------------------------------------------------
# delete that freshly created test hostgroup
# ------------------------------------------------------------------------------------
@hostgroupnames = ($hostgroup);
is $rest_api->delete_hostgroups( \@hostgroupnames, {}, \%outcome, \@results ), 1, "Successfully deleted hostgroups in list: @hostgroups";

# ------------------------------------------------------------------------------------
# delete the now non-existent hostgroup again
# ------------------------------------------------------------------------------------
is $rest_api->delete_hostgroups( \@hostgroupnames, {}, \%outcome, \@results ), 0,
  "Failure deleting a hostgroup that is not found/doesn't exist";

# delete any test hostgroups first
$delete_command = "$PSQL -c \"delete from hostgroup where name like '${hostgroupname_base}_%';\" gwcollagedb;";
print "Removing collage test hosts ${hostgroupname_base}* with '$delete_command'\n";
if ( system($delete_command ) >> 8 ) { print "Command '$delete_command' failed!! Quitting\n"; exit; }
@hostgroups = (
    { "name" => $hostgroup,          "description" => "CREATED at " . localtime, "alias" => "Alias for $hostgroup" },
    { "name" => $hostgroup . "_two", "description" => "CREATED at " . localtime, "alias" => "Alias for $hostgroup" . "_two" }
);

# create two test hostgroups
is $rest_api->upsert_hostgroups( \@hostgroups, {}, \%outcome, \@results ), 1, "Successfully created hostgroups $hostgroup and ${hostgroup}_two";

# ------------------------------------------------------------------------------------
# delete both hostgroups
# ------------------------------------------------------------------------------------
@hostgroupnames = ( $hostgroup, "${hostgroup}_two" );
is $rest_api->delete_hostgroups( \@hostgroupnames, {}, \%outcome, \@results ), 1, "Successfully deleted hostgroups in list: @hostgroups";
$logger->debug("\\\%outcome:\n", Dumper \%outcome);
$logger->debug("\\\@results:\n", Dumper \@results);

# ------------------------------------------------------------------------------------
# try to delete both hostgroups but force an error
# ------------------------------------------------------------------------------------
# create two test hostgroups again
is $rest_api->upsert_hostgroups( \@hostgroups, {}, \%outcome, \@results ), 1, "Successfully created hostgroups $hostgroup and ${hostgroup}_two";
@hostgroupnames = ( $hostgroup, "${hostgroup}_numberTWO" );    # <<< second hostname deliberately wrong
is $rest_api->delete_hostgroups( \@hostgroupnames, {}, \%outcome, \@results ), 0, "UNSuccessfully deleted ALL hostgroups in list: @hostgroups";
$logger->debug("\\\%outcome:\n", Dumper \%outcome);
$logger->debug("\\\@results:\n", Dumper \@results);

# MORE TESTS TBD

# Verify that logout (that is, destroying any server-side authentication tokens) works as planned.
is $rest_api->DESTROY(), 1, 'Logout';

# Now have Perl itself call the destructor.  This will call $rest_api->DESTROY() a second time.
$rest_api = undef;

# ------------------------------------------------------------------------------------
$logger->debug("----- END delete_hostgroups() tests ----");
done_testing();

