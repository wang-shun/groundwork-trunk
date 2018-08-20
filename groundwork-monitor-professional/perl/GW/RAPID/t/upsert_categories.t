#!/usr/local/groundwork/perl/bin/perl -w --

# This test script tests the REST API Perl module upsert_categories() routine

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
$logger->debug("----- START upsert_categories() tests ----");
my ( @categories, %outcome, @results ) = ();

# ----------------------------------------------------------------------------------------------------------------------
# initialize the REST API
my $rest_api = GW::RAPID->new( undef, undef, undef, undef, $requestor, { access => '/usr/local/groundwork/config/ws_client.properties' });

# Exception testing

is $rest_api->upsert_categories("arg1"), 0, 'Missing arguments exception';
is(
    ( not $rest_api->upsert_categories( "arg1", "arg2", \%outcome, "arg4", "arg5" ) and $outcome{response_error} =~ /Invalid number of args/ ),
    1,
    'Too many arguments exception'
);

is( ( not $rest_api->upsert_categories( undef, {}, \%outcome, [] ) and $outcome{response_error} =~ /Undefined arg/ ),
    1, 'Undefined argument exception' );
is( ( not $rest_api->upsert_categories( [], undef, \%outcome, [] ) and $outcome{response_error} =~ /Undefined arg/ ),
    1, 'Undefined argument exception' );
is $rest_api->upsert_categories( [], {}, undef, [] ), 0, 'Undefined argument exception';
is( ( not $rest_api->upsert_categories( [], {}, \%outcome, undef ) and $outcome{response_error} =~ /Undefined arg/ ),
    1, 'Undefined argument exception' );

is( ( not $rest_api->upsert_categories( {}, {}, \%outcome, [] ) and $outcome{response_error} =~ /Expecting ARRAY reference/ ),
    1, 'Incorrect object type reference exception' );
is( ( not $rest_api->upsert_categories( [], [], \%outcome, [] ) and $outcome{response_error} =~ /Expecting HASH reference/ ),
    1, 'Incorrect object type reference exception' );
is $rest_api->upsert_categories( [], {}, [], [] ), 0, 'Incorrect object type reference exception';
is( ( not $rest_api->upsert_categories( [], {}, \%outcome, {} ) and $outcome{response_error} =~ /Expecting ARRAY reference/ ),
    1, 'Incorrect object type reference exception' );

is( ( not $rest_api->upsert_categories( "", {}, \%outcome, [] ) and $outcome{response_error} =~ /Expecting ARRAY reference/ ),
    1, 'Incorrect object type reference exception' );
is( ( not $rest_api->upsert_categories( [], "", \%outcome, [] ) and $outcome{response_error} =~ /Expecting HASH reference/ ),
    1, 'Incorrect object type reference exception' );
is $rest_api->upsert_categories( [], {}, "", [] ), 0, 'Incorrect object type reference exception';
is( ( not $rest_api->upsert_categories( [], {}, \%outcome, "" ) and $outcome{response_error} =~ /Expecting ARRAY reference/ ),
    1, 'Incorrect object type reference exception' );

# ------------------------------------------------------------------------------------
# Simple positive test case  - creates a category
# ------------------------------------------------------------------------------------
my $catname_base = "RAPIDtestcategory";
my $catname      = $catname_base . time();

# delete any test categories first
my $delete_command = "$PSQL -c \"delete from category where name like '${catname_base}_%';\" gwcollagedb;";
print "Removing collage test categories ${catname_base}* with '$delete_command'\n";
if ( system($delete_command ) >> 8 ) { print "Command '$delete_command' failed!! Quitting\n"; exit; }
@categories = (
    {
	"name"           => $catname,
	"description"    => "$catname description",
	"entityTypeName" => "SERVICE_GROUP"
    }
);

# create a test category
is $rest_api->upsert_categories( \@categories, {}, \%outcome, \@results ), 1, "Successfully created category $catname";
my %results; $rest_api->get_categories( [], { depth => "simple" },  \%outcome, \%results );
$logger->error( Dumper \%outcome, \%results );
done_testing();
exit;

# ------------------------------------------------------------------------------------
# update that freshly created test category
# ------------------------------------------------------------------------------------
@categories = (
    {
	"name"           => $catname,
	"description"    => "UPDATED $catname description",    # <<< UPDATE
	"entityTypeName" => "SERVICE_GROUP"
    }
);

is $rest_api->upsert_categories( \@categories, {}, \%outcome, \@results ), 1, "Successfully updated category $catname";
$logger->debug("\\\%outcome:\n", Dumper \%outcome);
$logger->debug("\\\@results:\n", Dumper \@results);

# ------------------------------------------------------------------------------------
# try to create a category with malformed instructions - missing required property test
# ------------------------------------------------------------------------------------
@categories = (
    {
	## "name" => $catname, # <<< INTENTIONAL OMISSION
	"description"    => "SHOULD NEVER SEE THIS $catname description",
	"entityTypeName" => "SERVICE_GROUP"
    }
);

is $rest_api->upsert_categories( \@categories, {}, \%outcome, \@results ), 0,
  "Failed to create category due to missing required property 'name'";

# MORE TESTS TBD

# Verify that logout (that is, destroying any server-side authentication tokens) works as planned.
is $rest_api->DESTROY(), 1, 'Logout';

# Now have Perl itself call the destructor.  This will call $rest_api->DESTROY() a second time.
$rest_api = undef;

# ------------------------------------------------------------------------------------
$logger->debug("----- END upsert_categories() tests ----");
done_testing();

