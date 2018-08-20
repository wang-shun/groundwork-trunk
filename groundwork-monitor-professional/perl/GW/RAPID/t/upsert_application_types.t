#!/usr/local/groundwork/perl/bin/perl -w --

# This test script tests the REST API Perl module upsert_application_types() routine

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
$logger->debug("----- START upsert_application_types() tests ----");
my ( @application_types, %outcome, @results ) = ();

# ----------------------------------------------------------------------------------------------------------------------
# initialize the REST API
my $rest_api = GW::RAPID->new( undef, undef, undef, undef, $requestor, { access => '/usr/local/groundwork/config/ws_client.properties' });

# Exception testing
is $rest_api->upsert_application_types("arg1"), 0, 'Missing arguments exception';
is( ( not $rest_api->upsert_application_types( "arg1", "arg2", \%outcome, "arg4", "arg5" ) and $outcome{response_error} =~ /Invalid number of args/ ),
    1, 'Too many arguments exception' );

is( ( not $rest_api->upsert_application_types( undef, {}, \%outcome, [] ) and $outcome{response_error} =~ /Undefined arg/ ),
    1, 'Undefined argument exception' );
is( ( not $rest_api->upsert_application_types( [], undef, \%outcome, [] ) and $outcome{response_error} =~ /Undefined arg/ ),
    1, 'Undefined argument exception' );
is $rest_api->upsert_application_types( [], {}, undef, [] ), 0, 'Undefined argument exception';
is( ( not $rest_api->upsert_application_types( [], {}, \%outcome, undef ) and $outcome{response_error} =~ /Undefined arg/ ),
    1, 'Undefined argument exception' );

is( ( not $rest_api->upsert_application_types( {}, {}, \%outcome, [] ) and $outcome{response_error} =~ /Expecting ARRAY reference/ ),
    1, 'Incorrect object type reference exception' );
is( ( not $rest_api->upsert_application_types( [], [], \%outcome, [] ) and $outcome{response_error} =~ /Expecting HASH reference/ ),
    1, 'Incorrect object type reference exception' );
is $rest_api->upsert_application_types( [], {}, [], [] ), 0, 'Incorrect object type reference exception';
is( ( not $rest_api->upsert_application_types( [], {}, \%outcome, {} ) and $outcome{response_error} =~ /Expecting ARRAY reference/ ),
    1, 'Incorrect object type reference exception' );

is( ( not $rest_api->upsert_application_types( "", {}, \%outcome, [] ) and $outcome{response_error} =~ /Expecting ARRAY reference/ ),
    1, 'Incorrect object type reference exception' );
is( ( not $rest_api->upsert_application_types( [], "", \%outcome, [] ) and $outcome{response_error} =~ /Expecting HASH reference/ ),
    1, 'Incorrect object type reference exception' );
is $rest_api->upsert_application_types( [], {}, "", [] ), 0, 'Incorrect object type reference exception';
is( ( not $rest_api->upsert_application_types( [], {}, \%outcome, "" ) and $outcome{response_error} =~ /Expecting ARRAY reference/ ),
    1, 'Incorrect object type reference exception' );

# ------------------------------------------------------------------------------------
# Simple positive test case  - creates an application type
# ------------------------------------------------------------------------------------
my $app_type_name_base = "__RAPID_test_app_type_";
my $app_type_name      = $app_type_name_base . time();

# delete any test application type first
my $delete_command = "$PSQL -c \"delete from applicationtype where name like '${app_type_name_base}_%';\" gwcollagedb;";
print "Removing collage test application_types ${app_type_name_base}* with '$delete_command'\n";
if ( system($delete_command ) >> 8 ) { print "Command '$delete_command' failed!! Quitting\n"; exit; }
@application_types = (
    {
	'name'                    => $app_type_name,
	'description'             => "RAPID test application type",
	'stateTransitionCriteria' => 'Device;Host',
    }
);

# create the test application type
is $rest_api->upsert_application_types( \@application_types, {}, \%outcome, \@results ), 1,
  "Successfully created application type $app_type_name";

# ------------------------------------------------------------------------------------
# update that freshly created test application type
# ------------------------------------------------------------------------------------
@application_types = (
    {
	'name'                    => $app_type_name,
	'description'             => "UPDATED description for RAPID test application type",    # <<< UPDATE
	'stateTransitionCriteria' => 'Device;Host',
    }
);

is $rest_api->upsert_application_types( \@application_types, {}, \%outcome, \@results ), 1,
  "Successfully updated application type $app_type_name";

# ------------------------------------------------------------------------------------
# try to update an application type with malformed instructions -
# missing required property test
# ------------------------------------------------------------------------------------
@application_types = (
    {
	'name'                    => $app_type_name,
	'description'             => "updated description with missing transition",
	# 'stateTransitionCriteria' => 'Device;Host',        ### OMISSION
    }
);
is $rest_api->upsert_application_types( \@application_types, {}, \%outcome, \@results ), 1,
  "Failed to update application type due to missing required property 'stateTransitionCriteria'";
$logger->debug( "\\\%outcome:\n", Dumper \%outcome );
$logger->debug( "\\\@results:\n", Dumper \@results );


# ------------------------------------------------------------------------------------
# try to create an application type with malformed instructions -
# missing required property test
# ------------------------------------------------------------------------------------
@application_types = (
    {
	'name'                    => $app_type_name_base . "missing",
	'description'             => "some description",
	# 'stateTransitionCriteria' => 'Device;Host',        ### OMISSION
    }
);

# As of this writing, this call is returning a failure indication as expected, but the accompanying
# failure message is:
# 'Batch update returned unexpected row count from update [0]; actual row count: 0; expected: 1; nested exception is org.hibernate.StaleStateException: Batch update returned unexpected row count from update [0]; actual row count: 0; expected: 1'
# which is not at all reflective of the fact that the stateTransitionCriteria field is missing.

is $rest_api->upsert_application_types( \@application_types, {}, \%outcome, \@results ), 0,
  "Failed to create application type due to missing required property 'stateTransitionCriteria'";
$logger->debug( "\\\%outcome:\n", Dumper \%outcome );
$logger->debug( "\\\@results:\n", Dumper \@results );

# Verify that logout (that is, destroying any server-side authentication tokens) works as planned.
is $rest_api->DESTROY(), 1, 'Logout';

# Now have Perl itself call the destructor.  This will call $rest_api->DESTROY() a second time.
$rest_api = undef;

# ------------------------------------------------------------------------------------
$logger->debug("----- END upsert_application_types() tests ----");
done_testing();

