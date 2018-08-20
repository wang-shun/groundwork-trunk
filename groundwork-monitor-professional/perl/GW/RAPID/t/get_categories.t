#!/usr/local/groundwork/perl/bin/perl -w --

die "$0: Please set PGPASSWORD in the environment before running this test script.\n" if not $ENV{'PGPASSWORD'};
my $PSQL = "/usr/local/groundwork/postgresql/bin/psql";    # change if necessary

# This test script tests the REST API Perl module get_categories() routine

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
$logger->debug("----- START get_categories() tests ----");
my ( %outcome, %results, @results, $query ) = ();

# ----------------------------------------------------------------------------------------------------------------------
# initialize the REST API
my $rest_api = GW::RAPID->new( undef, undef, undef, undef, $requestor, { access => '/usr/local/groundwork/config/ws_client.properties' });

# Exception testing

#is $rest_api->get_categories("arg1"), 0, 'Missing arguments exception';
#is( ( not $rest_api->get_categories( "arg1", "arg2", \%outcome, "arg4", "arg5" ) and $outcome{response_error} =~ /Invalid number of args/ ), 1, 'Too many arguments exception' );
#is( ( not $rest_api->get_categories( undef, {}, \%outcome, {} ) and $outcome{response_error} =~ /Undefined arg/ ), 1, 'Undefined argument exception' );
#is( ( not $rest_api->get_categories( [], undef, \%outcome, {} ) and $outcome{response_error} =~ /Undefined arg/ ), 1, 'Undefined argument exception' );
#is $rest_api->get_categories( [], {}, undef, {} ), 0, 'Undefined argument exception'; is( ( not $rest_api->get_categories( [], {}, \%outcome, undef ) and $outcome{response_error} =~ /Undefined arg/ ), 1, 'Undefined argument exception' ); 
#is( ( not $rest_api->get_categories( {}, {}, \%outcome, {} ) and $outcome{response_error} =~ /Expecting ARRAY reference/ ), 1, 'Incorrect object type reference exception' );
#is( ( not $rest_api->get_categories( [], [], \%outcome, {} ) and $outcome{response_error} =~ /Expecting HASH reference/ ), 1, 'Incorrect object type reference exception' );
#is $rest_api->get_categories( [], {}, [], {} ), 0, 'Incorrect object type reference exception'; is( ( not $rest_api->get_categories( [], {}, \%outcome, [] ) and $outcome{response_error} =~ /Expecting HASH reference/ ), 1, 'Incorrect object type reference exception' ); 
#is( ( not $rest_api->get_categories( "", {}, \%outcome, {} ) and $outcome{response_error} =~ /Expecting ARRAY reference/ ), 1, 'Incorrect object type reference exception' );
#is( ( not $rest_api->get_categories( [], "", \%outcome, {} ) and $outcome{response_error} =~ /Expecting HASH reference/ ), 1, 'Incorrect object type reference exception' );
#is $rest_api->get_categories( [], {}, "", {} ), 0, 'Incorrect object type reference exception';
#is( ( not $rest_api->get_categories( [], {}, \%outcome, "" ) and $outcome{response_error} =~ /Expecting HASH reference/ ), 1, 'Incorrect object type reference exception' );

# ------------------------------------------------------------------------------------
# First create a category to get
# ------------------------------------------------------------------------------------
my $catname_base = "RAPIDtestcategory";
my $catname      = $catname_base . "1";
my $catname_2    = $catname_base . "2";

# delete any test categories first
my $delete_command = "$PSQL -c \"delete from category where name like '${catname_base}_%';\" gwcollagedb;";
print "Removing collage test categories ${catname_base}* with '$delete_command'\n";
if ( system($delete_command ) >> 8 ) { print "Command '$delete_command' failed!! Quitting\n"; exit; }
my @categories = (
    {
	"name"           => $catname,
	"description"    => "description one",
	"entityTypeName" => "SERVICE_GROUP"
    },
    {
	"name"           => $catname_2,
	"description"    => "description two ",
	"entityTypeName" => "SERVICE_GROUP"
    }
);
is $rest_api->upsert_categories( \@categories, {}, \%outcome, \@results ), 1, "Successfully created category $catname";

# is <RAPID function>(params) , <expected return value of function>, <test name>


my @get = ( 
		"$catname/SERVICE_GROUP" ,
		#"$catname_2/SERVICE_GROUP" 
	);

#$rest_api->get_categories( \@get , { query=>'something' }, \%outcome, \%results );
$rest_api->get_categories( [ 'a' ] , {}, \%outcome, \%results );
$logger->error( Dumper \%outcome, \%results );

#$rest_api->get_categories( [], {}, \%outcome, \%results );
#$logger->error( Dumper \%outcome, \%results );
done_testing(); exit;


is $rest_api->get_categories( [$catname], { depth => "simple" },  \%outcome, \%results ), 1, "Category $catname found, depth = simple";


is $rest_api->get_categories( [$catname], { depth => "shallow" }, \%outcome, \%results ), 1, "Category $catname found, depth = shallow";
is $rest_api->get_categories( [$catname], { depth => "deep" },    \%outcome, \%results ), 1, "Category $catname found, depth = deep";
is $rest_api->get_categories( ["___dingleworts_domtest___"], {}, \%outcome, \%results ), 0, "Category ___dingleworts_domtest___ not found";

# Query based
$query = "entityTypeName = 'SERVICE_GROUP'";
#is $rest_api->get_categories( [], { query => $query }, \%outcome, \%results ), 1, "Simple successful query, depth not defined";
#$rest_api->get_categories( [], { query => $query }, \%outcome, \%results );
$rest_api->get_categories( [], { }, \%outcome, \%results );

$logger->error( Dumper \%outcome, \%results );

done_testing(); exit;


is $rest_api->get_categories( [], { depth => "simple",  query => $query }, \%outcome, \%results ), 1, "Simple successful query , depth = simple";
is $rest_api->get_categories( [], { depth => "shallow", query => $query }, \%outcome, \%results ), 1, "Simple successful query, depth = shallow";
is $rest_api->get_categories( [], { depth => "deep",    query => $query }, \%outcome, \%results ), 1, "Simple successful query , with depth = deep";

$query = "entityTypeName = 'CABBAGES'";
is $rest_api->get_categories( [], { query => $query }, \%outcome, \%results ), 0, "Unsuccessful simple query ($query)";

$query = "entityTypeName illegaloperator 'CABBAGES'";
is $rest_api->get_categories( [], { query => $query }, \%outcome, \%results ), 0,
  "Unsuccessful illegal simple query ($query) (this should produce a HTTP status 500 )";

# Deeper positive single and multi host retrieval testing
my $simple_category_cmp = { 'entityTypeName' => re('.*'), 'name' => re('.*'), 'id' => re('.*'), 'description' => re('.*'), };

# Simple successful *category* retrieval test
is $rest_api->get_categories( [$catname], { depth => "simple" }, \%outcome, \%results ), 1, "Category $catname found";
foreach my $c_name ( keys %results ) {
    ## $logger->debug("Comparing resultant '$c_name' category.");
    cmp_deeply( $results{$c_name}, $simple_category_cmp, "Single-category result: Received expected structure of category result '$c_name'" );
}

# Simple Successful *categories* retrieval test
is $rest_api->get_categories( [], { depth => "simple" }, \%outcome, \%results ), 1, "No category - all categories should be returned";
$logger->debug( "\\\%outcome:\n", Dumper \%outcome );
$logger->debug( "\\\%results:\n", Dumper \%results );
foreach my $c_name ( keys %results ) {
    $logger->debug("Comparing resultant '$c_name' category.");
    cmp_deeply( $results{$c_name}, $simple_category_cmp, "Multi-category result: Received expected structure of category result '$c_name'" );
}

# Complex Successful *categories* retrieval test
is $rest_api->get_categories( [ $catname, $catname_2 ], { depth => "simple" }, \%outcome, \%results ), 1, "Several specific categories should be returned";
$logger->debug( "\\\%outcome:\n", Dumper \%outcome );
$logger->debug( "\\\%results:\n", Dumper \%results );
foreach my $c_name ( keys %results ) {
    $logger->debug("Comparing resultant '$c_name' category.");
    cmp_deeply( $results{$c_name}, $simple_category_cmp, "Multi-category result: Received expected structure of category result '$c_name'" );
}

# Verify that logout (that is, destroying any server-side authentication tokens) works as planned.
is $rest_api->DESTROY(), 1, 'Logout';

# Now have Perl itself call the destructor.  This will call $rest_api->DESTROY() a second time.
$rest_api = undef;

# ----------------------------------------------------------------------------------------------------------------------
$logger->debug("----- END get_categories() tests ----");
done_testing();

