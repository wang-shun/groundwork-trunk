#!/usr/local/groundwork/perl/bin/perl -w --

# This test script tests the REST API Perl module delete_application_types() routine

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
$logger->debug("----- START delete_application_types() tests ----");
my ( @application_types, @app_type_names, %outcome, %results, @results ) = ();

# ----------------------------------------------------------------------------------------------------------------------
# initialize the REST API
my $rest_api = GW::RAPID->new( undef, undef, undef, undef, $requestor, { access => '/usr/local/groundwork/config/ws_client.properties' });

# Exception testing

is $rest_api->delete_application_types("arg1"), 0, 'Missing arguments exception';
is( ( not $rest_api->delete_application_types( "arg1", "arg2", \%outcome, "arg4", "arg5" ) and $outcome{response_error} =~ /Invalid number of args/ ),
    1, 'Too many arguments exception' );

is( ( not $rest_api->delete_application_types( undef, {}, \%outcome, [] ) and $outcome{response_error} =~ /Undefined arg/ ),
    1, 'Undefined argument exception' );
is( ( not $rest_api->delete_application_types( [], undef, \%outcome, [] ) and $outcome{response_error} =~ /Undefined arg/ ),
    1, 'Undefined argument exception' );
is $rest_api->delete_application_types( [], {}, undef, [] ), 0, 'Undefined argument exception';
is( ( not $rest_api->delete_application_types( [], {}, \%outcome, undef ) and $outcome{response_error} =~ /Undefined arg/ ),
    1, 'Undefined argument exception' );

is( ( not $rest_api->delete_application_types( {}, {}, \%outcome, [] ) and $outcome{response_error} =~ /Expecting ARRAY reference/ ),
    1, 'Incorrect object type reference exception' );
is( ( not $rest_api->delete_application_types( [], [], \%outcome, [] ) and $outcome{response_error} =~ /Expecting HASH reference/ ),
    1, 'Incorrect object type reference exception' );
is $rest_api->delete_application_types( [], {}, [], [] ), 0, 'Incorrect object type reference exception';
is( ( not $rest_api->delete_application_types( [], {}, \%outcome, {} ) and $outcome{response_error} =~ /Expecting ARRAY reference/ ),
    1, 'Incorrect object type reference exception' );

is( ( not $rest_api->delete_application_types( "", {}, \%outcome, [] ) and $outcome{response_error} =~ /Expecting ARRAY reference/ ),
    1, 'Incorrect object type reference exception' );
is( ( not $rest_api->delete_application_types( [], "", \%outcome, [] ) and $outcome{response_error} =~ /Expecting HASH reference/ ),
    1, 'Incorrect object type reference exception' );
is $rest_api->delete_application_types( [], {}, "", [] ), 0, 'Incorrect object type reference exception';
is( ( not $rest_api->delete_application_types( [], {}, \%outcome, "" ) and $outcome{response_error} =~ /Expecting ARRAY reference/ ),
    1, 'Incorrect object type reference exception' );

# ------------------------------------------------------------------------------------
# Simple positive test case  - creates an application type and then deletes it
# ------------------------------------------------------------------------------------
my $app_type_name_base = "__RAPID_test_application_type_";
my $app_type_name      = $app_type_name_base . time();

# delete any test application types first
my $delete_command = "$PSQL -c \"delete from applicationtype where name like '${app_type_name_base}_%';\" gwcollagedb;";
print "Removing collage test application types ${app_type_name_base}* with '$delete_command'\n";
if ( system($delete_command ) >> 8 ) { print "Command '$delete_command' failed!! Quitting\n"; exit; }
@application_types = (
    {
	"name"                    => $app_type_name,
	"description"             => "CREATED at " . localtime,
	"stateTransitionCriteria" => "Device;Host;ServiceDescription",
	# FIX MAJOR:  include some "entityProperties" or "properties" here, as appropriate, for test purposes
	# "properties"              => { "Latency" => "125", "UpdatedBy" => "admin", "Comments" => "Testing" }
    }
);

# create a test application type
is $rest_api->upsert_application_types( \@application_types, {}, \%outcome, \@results ), 1, "Successfully created application type $app_type_name";
$logger->debug("\\\%outcome:\n", Dumper \%outcome);
$logger->debug("\\\@results:\n", Dumper \@results);

# ------------------------------------------------------------------------------------
# delete that freshly created test application type
# ------------------------------------------------------------------------------------
@app_type_names = ($app_type_name);
is $rest_api->delete_application_types( \@app_type_names, {}, \%outcome, \@results ), 1, "Successfully deleted application types in list: @app_type_names";
$logger->debug("\\\%outcome:\n", Dumper \%outcome);
$logger->debug("\\\@results:\n", Dumper \@results);

# ------------------------------------------------------------------------------------
# delete the now non-existent application type again
# ------------------------------------------------------------------------------------
# FIX MAJOR:  This attempt to delete an application type we have already deleted should result
# in a warning status for the particular application type we are trying to delete, since it
# should not be found during the search phase of the deletion, but it will be gone by the time
# the entire call returns to this application code.  That should translate to a failure of the
# $rest_api->delete_application_types() call, so the application code will know to go looking in
# @results for details.  Alas, in the present implementation of the REST API as of this writing,
# it is returning a success status for this deletion, which translates to a successful return
# from this call.  We ought to get that fixed within the REST API.
is $rest_api->delete_application_types( \@app_type_names, {}, \%outcome, \@results ), 0, "Failure deleting an application type that is not found/doesn't exist";
$logger->debug("\\\%outcome:\n", Dumper \%outcome);
$logger->debug("\\\@results:\n", Dumper \@results);

# delete any test application types first
$delete_command = "$PSQL -c \"delete from applicationtype where name like '${app_type_name_base}_%';\" gwcollagedb;";
print "Removing collage test application types ${app_type_name_base}* with '$delete_command'\n";
if ( system($delete_command ) >> 8 ) { print "Command '$delete_command' failed!! Quitting\n"; exit; }
@application_types = (
    {
	"name"                    => $app_type_name,
	"description"             => "CREATED at " . localtime,
	"stateTransitionCriteria" => "Device;Event_OID_numeric",
	# FIX MAJOR:  include some "entityProperties" or "properties" here, as appropriate, for test purposes
	# "properties"              => { "Latency" => "125", "UpdatedBy" => "admin", "Comments" => "Testing" }
    },
    {
	"name"                    => "${app_type_name}_number2",
	"description"             => "CREATED at " . localtime,
	"stateTransitionCriteria" => "Device;Host;ServiceDescription",
	# FIX MAJOR:  include some "entityProperties" or "properties" here, as appropriate, for test purposes
	# "properties"              => { "Latency" => "125", "UpdatedBy" => "admin", "Comments" => "Testing" }
    }

);

# create two test application types
is $rest_api->upsert_application_types( \@application_types, {}, \%outcome, \@results ), 1, "Successfully created application types $app_type_name and ${app_type_name}_number2";

# ------------------------------------------------------------------------------------
# delete both application types
# ------------------------------------------------------------------------------------
@app_type_names = ( $app_type_name, "${app_type_name}_number2" );
is $rest_api->delete_application_types( \@app_type_names, {}, \%outcome, \@results ), 1, "Successfully deleted application types in list: @app_type_names";
## FIX MAJOR:  Verify the returned data in @results.
$logger->debug("\\\%outcome:\n", Dumper \%outcome);
$logger->debug("\\\@results:\n", Dumper \@results);

# ------------------------------------------------------------------------------------
# try to delete both application types but force an error
# ------------------------------------------------------------------------------------
# create two test application types again
is $rest_api->upsert_application_types( \@application_types, {}, \%outcome, \@results ), 1, "Successfully created application types $app_type_name and ${app_type_name}_number2";

# FIX MAJOR:  As above, this attempt to delete an application type that does not exist should
# result in a warning status for the missing application type we are trying to delete, since it
# should not be found during the search phase of the deletion, but it will be gone by the time
# the entire call returns to this application code.  That should translate to a failure of the
# $rest_api->delete_application_types() call, so the application code will know to go looking in
# @results for details.  Alas, in the present implementation of the REST API as of this writing,
# it is returning a success status for this deletion, which translates to a successful return
# from this call.  We ought to get that fixed within the REST API.
@app_type_names = ( $app_type_name, "${app_type_name}_numberTWO" );    # <<< second application type name is deliberately wrong
is $rest_api->delete_application_types( \@app_type_names, {}, \%outcome, \@results ), 0, "UNSuccessfully deleted ALL application types in list: @app_type_names";
$logger->debug("\\\%outcome:\n", Dumper \%outcome);
$logger->debug("\\\@results:\n", Dumper \@results);

# MORE TESTS TBD

# Verify that logout (that is, destroying any server-side authentication tokens) works as planned.
is $rest_api->DESTROY(), 1, 'Logout';

# Now have Perl itself call the destructor.  This will call $rest_api->DESTROY() a second time.
$rest_api = undef;

# ------------------------------------------------------------------------------------
$logger->debug("----- END delete_application_types() tests ----");
done_testing();

