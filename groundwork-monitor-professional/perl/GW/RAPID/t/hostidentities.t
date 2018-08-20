#!/usr/local/groundwork/perl/bin/perl -w --

# This test script tests the REST API Perl module get/upsert/delete_hostidentities() routines
# Its more convenient to do it all together rather than separate .t files for get/delete/upsert.

use strict;

# FIX MAJOR:  This is one form of workaround for the underscore-to-dash translation issue.
# It should be fixed instead by renaming the APP_NAME and GWOS_API_TOKEN header names.
use HTTP::Headers; $HTTP::Headers::TRANSLATE_UNDERSCORE = 0;
use GW::RAPID;
use Test::More;
use Test::Deep;
use Test::Exception;
use JSON;
use Data::Dumper; $Data::Dumper::Indent   = 1; $Data::Dumper::Sortkeys = 1;
use File::Basename; my $requestor = "RAPID-" . basename($0);
use Log::Log4perl qw(get_logger);
Log::Log4perl::init('GW_RAPID.log4perl.conf');
my $logger = get_logger("GW.RAPID.module");
$logger->debug("----- START get_host_identities() tests ----");
my ( %outcome, %results, @results, $query, $msg, @ids ) = ();

# ----------------------------------------------------------------------------------------------------------------------
# initialize the REST API
my $rest_api = GW::RAPID->new( undef, undef, undef, undef, $requestor, { access => '/usr/local/groundwork/config/ws_client.properties' });

# Exception testing
# the usual expection testing for get/upsert/delete methods
is( ( not $rest_api->get_hostidentities( "arg1", "arg2", \%outcome, "arg4", "arg5" ) and $outcome{response_error} =~ /Invalid number of args/ ), 1, 'Too many arguments exception' );
is( ( not $rest_api->get_hostidentities( undef, {}, \%outcome, {} ) and $outcome{response_error} =~ /Undefined arg/ ), 1, 'Undefined argument exception' );
is( ( not $rest_api->get_hostidentities( [], undef, \%outcome, {} ) and $outcome{response_error} =~ /Undefined arg/ ), 1, 'Undefined argument exception' );
is $rest_api->get_hostidentities( [], {}, undef, {} ), 0, 'Undefined argument exception'; is( ( not $rest_api->get_hosts( [], {}, \%outcome, undef ) and $outcome{response_error} =~ /Undefined arg/ ), 1, 'Undefined argument exception' );
is( ( not $rest_api->get_hostidentities( {}, {}, \%outcome, {} ) and $outcome{response_error} =~ /Expecting ARRAY reference/ ), 1, 'Incorrect object type reference exception' );
is( ( not $rest_api->get_hostidentities( [], [], \%outcome, {} ) and $outcome{response_error} =~ /Expecting HASH reference/ ), 1, 'Incorrect object type reference exception' );
is $rest_api->get_hostidentities( [], {}, [], {} ), 0, 'Incorrect object type reference exception';
is( ( not $rest_api->get_hostidentities( [], {}, \%outcome, [] ) and $outcome{response_error} =~ /Expecting HASH reference/ ), 1, 'Incorrect object type reference exception' );
is( ( not $rest_api->get_hostidentities( "", {}, \%outcome, {} ) and $outcome{response_error} =~ /Expecting ARRAY reference/ ), 1, 'Incorrect object type reference exception' );
is( ( not $rest_api->get_hostidentities( [], "", \%outcome, {} ) and $outcome{response_error} =~ /Expecting HASH reference/ ), 1, 'Incorrect object type reference exception' );
is $rest_api->get_hostidentities( [], {}, "", {} ), 0, 'Incorrect object type reference exception';
is( ( not $rest_api->get_hostidentities( [], {}, \%outcome, "" ) and $outcome{response_error} =~ /Expecting HASH reference/ ), 1, 'Incorrect object type reference exception' );


is $rest_api->upsert_hostidentities("arg1"), 0, 'Missing arguments exception';
is( ( not $rest_api->upsert_hostidentities( "arg1", "arg2", \%outcome, "arg4", "arg5" ) and $outcome{response_error} =~ /Invalid number of args/ ), 1, 'Too many arguments exception' );
is( ( not $rest_api->upsert_hostidentities( undef, {}, \%outcome, [] ) and $outcome{response_error} =~ /Undefined arg/ ), 1, 'Undefined argument exception' );
is( ( not $rest_api->upsert_hostidentities( [], undef, \%outcome, [] ) and $outcome{response_error} =~ /Undefined arg/ ), 1, 'Undefined argument exception' );
is $rest_api->upsert_hostidentities( [], {}, undef, [] ), 0, 'Undefined argument exception'; 
is( ( not $rest_api->upsert_hostidentities( [], {}, \%outcome, undef ) and $outcome{response_error} =~ /Undefined arg/ ), 1, 'Undefined argument exception' );
is( ( not $rest_api->upsert_hostidentities( {}, {}, \%outcome, [] ) and $outcome{response_error} =~ /Expecting ARRAY reference/ ), 1, 'Incorrect arguments object type reference exception' );
is( ( not $rest_api->upsert_hostidentities( [], [], \%outcome, [] ) and $outcome{response_error} =~ /Expecting HASH reference/ ), 1, 'Incorrect arguments object type reference exception' );
is $rest_api->upsert_hostidentities( [], {}, [], [] ), 0, 'Incorrect arguments object type reference exception';
is( ( not $rest_api->upsert_hostidentities( [], {}, \%outcome, {} ) and $outcome{response_error} =~ /Expecting ARRAY reference/ ), 1, 'Incorrect arguments object type reference exception' );
is( ( not $rest_api->upsert_hostidentities( "", {}, \%outcome, [] ) and $outcome{response_error} =~ /Expecting ARRAY reference/ ), 1, 'Incorrect arguments object type reference exception' );
is( ( not $rest_api->upsert_hostidentities( [], "", \%outcome, [] ) and $outcome{response_error} =~ /Expecting HASH reference/ ), 1, 'Incorrect arguments object type reference exception' );
is $rest_api->upsert_hostidentities( [], {}, "", [] ), 0, 'Incorrect arguments object type reference exception'; 
is( ( not $rest_api->upsert_hostidentities( [], {}, \%outcome, "" ) and $outcome{response_error} =~ /Expecting ARRAY reference/ ), 1, 'Incorrect arguments object type reference exception' );


is $rest_api->delete_hostidentities("arg1"), 0, 'Missing arguments exception';
is( ( not $rest_api->delete_hostidentities( "arg1", "arg2", \%outcome, "arg4", "arg5" ) and $outcome{response_error} =~ /Invalid number of args/ ), 1, 'Too many arguments exception' );
is( ( not $rest_api->delete_hostidentities( undef, {}, \%outcome, [] ) and $outcome{response_error} =~ /Undefined arg/ ), 1, 'Undefined argument exception' );
is( ( not $rest_api->delete_hostidentities( [], undef, \%outcome, [] ) and $outcome{response_error} =~ /Undefined arg/ ), 1, 'Undefined argument exception' );
is $rest_api->delete_hostidentities( [], {}, undef, [] ), 0, 'Undefined argument exception'; is( ( not $rest_api->delete_hostidentities( [], {}, \%outcome, undef ) and $outcome{response_error} =~ /Undefined arg/ ), 1, 'Undefined argument exception' );
is( ( not $rest_api->delete_hostidentities( {}, {}, \%outcome, [] ) and $outcome{response_error} =~ /Expecting ARRAY reference/ ), 1, 'Incorrect object type reference exception' );
is( ( not $rest_api->delete_hostidentities( [], [], \%outcome, [] ) and $outcome{response_error} =~ /Expecting HASH reference/ ), 1, 'Incorrect object type reference exception' );
is $rest_api->delete_hostidentities( [], {}, [], [] ), 0, 'Incorrect object type reference exception';
is( ( not $rest_api->delete_hostidentities( [], {}, \%outcome, {} ) and $outcome{response_error} =~ /Expecting ARRAY reference/ ), 1, 'Incorrect object type reference exception' );
is( ( not $rest_api->delete_hostidentities( "", {}, \%outcome, [] ) and $outcome{response_error} =~ /Expecting ARRAY reference/ ), 1, 'Incorrect object type reference exception' );
is( ( not $rest_api->delete_hostidentities( [], "", \%outcome, [] ) and $outcome{response_error} =~ /Expecting HASH reference/ ), 1, 'Incorrect object type reference exception' );
is $rest_api->delete_hostidentities( [], {}, "", [] ), 0, 'Incorrect object type reference exception';
is( ( not $rest_api->delete_hostidentities( [], {}, \%outcome, "" ) and $outcome{response_error} =~ /Expecting ARRAY reference/ ), 1, 'Incorrect object type reference exception' );


is $rest_api->clear_hostidentities("arg1"), 0, 'Missing arguments exception';
is ((not $rest_api->clear_hostidentities( "arg1", "arg2", \%outcome, "arg4", "arg5" ) and $outcome{response_error} =~ /Invalid number of args/), 1, 'Too many arguments exception');
is ((not $rest_api->clear_hostidentities( undef, {}, \%outcome, [] ) and $outcome{response_error} =~ /Undefined arg/), 1, 'Undefined argument exception');
is ((not $rest_api->clear_hostidentities( [], undef, \%outcome, [] ) and $outcome{response_error} =~ /Undefined arg/), 1, 'Undefined argument exception');
is $rest_api->clear_hostidentities( [], {}, undef, [] ), 0, 'Undefined argument exception';
is ((not $rest_api->clear_hostidentities( [], {}, \%outcome, undef ) and $outcome{response_error} =~ /Undefined arg/), 1, 'Undefined argument exception');
is ((not $rest_api->clear_hostidentities( {}, {}, \%outcome, [] ) and $outcome{response_error} =~ /Expecting ARRAY reference/), 1, 'Incorrect object type reference exception');
is ((not $rest_api->clear_hostidentities( [], [], \%outcome, [] ) and $outcome{response_error} =~ /Expecting HASH reference/), 1, 'Incorrect object type reference exception');
is $rest_api->clear_hostidentities( [], {}, [], [] ), 0, 'Incorrect object type reference exception';
is ((not $rest_api->clear_hostidentities( [], {}, \%outcome, {} ) and $outcome{response_error} =~ /Expecting ARRAY reference/), 1, 'Incorrect object type reference exception');
is ((not $rest_api->clear_hostidentities( "", {}, \%outcome, [] ) and $outcome{response_error} =~ /Expecting ARRAY reference/), 1, 'Incorrect object type reference exception');
is ((not $rest_api->clear_hostidentities( [], "", \%outcome, [] ) and $outcome{response_error} =~ /Expecting HASH reference/), 1, 'Incorrect object type reference exception');
is $rest_api->clear_hostidentities( [], {}, "", [] ), 0, 'Incorrect object type reference exception';
is ((not $rest_api->clear_hostidentities( [], {}, \%outcome, "" ) and $outcome{response_error} =~ /Expecting ARRAY reference/), 1, 'Incorrect object type reference exception');


# From the docs:
# Add or update host identities by post. Any number of host identities can be posted to the server in a single asynchronous, (the default), or synchronous request. 
# Host identity ids will be defaulted by the server, but they can be specified as part of the post body request. 
# Requests are considered updates if the host identity referenced by id or primary host name exists. 
# Updates that specify a hostName are assumed to rename the host identity and associated host. 
# If the update specifies hostNames, these are added as new alternate host names. 
# The delete/clear request documented below can be used to remove all hostNames.

# Basic core functionality tests
# ------------------------------
# create some identities
# get some of them
# delete some of them
# clear some of them

# delete any existing identities first
if ( not delete_all_identities() ) { 
	$logger->error("Failed to delete all identities - quitting");
	exit;
}

# create some test hosts
my $unlikely_host = '__abc_domtesting__RAPID__abc_zzz___' ; # it's really unlikely this host will ever exist - could write a function to return non existent host but this is fine for now
create_test_hosts( [ "ahost1" , "ahost2" , "ahost3", 'ahost4'] );

# basic create test that should work
my @create_new_identities = ( 
    {
  	"hostName"  => "ahost1",
    	"hostNames" => ["ahost1-alias1", "ahost1-alias2"]
    },
    {
  	"hostName"  => "ahost2",
    	"hostNames" => ["ahost2-alias1", "ahost2-alias2"]
    },
    {
  	"hostName"  => "ahost3",
    	"hostNames" => ["ahost3-alias1", "ahost3-alias2"]
    },
);
is ( ( $rest_api->upsert_hostidentities( \@create_new_identities, { }, \%outcome, \@results ) 
     and $outcome{successful} == scalar @create_new_identities ),   # ie the successful prop val in the return json = the same number as the hosts to create aliases for
     1, "Basic upsert of host identities for " . scalar @create_new_identities . " hosts") ;
$logger->debug( "BASIC CREATE: " . Dumper \%outcome, \@results) ;

# Basic get test that should work
is ( ( $rest_api->get_hostidentities( [ ] , { }, \%outcome, \%results ) 
     and exists $results{'unknown hostIdentities'}{'hostIdentities'}  # need to check got some basic structure back first
     and scalar @{$results{'unknown hostIdentities'}{'hostIdentities'}} == scalar @create_new_identities ), # check number of host alias blocks returned = number just created
     1, "Basic get of all identities");
$logger->debug( "BASIC GET ALL : " . Dumper \%outcome, \%results);

# Basic deletion of aliases from two hosts, by hostName should fail - seems like hostIdentityId is required
# Get the hostIdentityId's for the hosts for which aliases will be deleted
my @delete_these = ( 'ahost1', 'ahost2' );
$rest_api->get_hostidentities( \@delete_these , { }, \%outcome, \%results ); 
@ids = map { $_->{hostIdentityId} } @{$results{'unknown hostIdentities'}{'hostIdentities'} }; 
# Do the delete using the list of id's
is ( ( $rest_api->delete_hostidentities( \@ids , { }, \%outcome, \@results ) 
     and exists $results{'unknown hostIdentities'}{'hostIdentities'}  # need to check got some basic structure back first
     and scalar @{$results{'unknown hostIdentities'}{'hostIdentities'}} == scalar @delete_these ), # check number of host alias blocks returned = number of hosts selected
     1, "Basic failed delete of identities for hosts");
$logger->debug( "DELETE 2 : " . Dumper \%outcome, \%results);

# Clear one of them rather than delete it
my @clear_hosts = ( 'ahost3' );
$rest_api->get_hostidentities( \@clear_hosts , { }, \%outcome, \%results ); 
#print "GET for ahost3: " . Dumper \%outcome, \%results;  exit;
#is ( ( $rest_api->delete_hostidentities( [ $results{'unknown hostIdentities'}{'hostIdentities'}[0]{'hostIdentityId'} ] , { "clear" => "true" } , \%outcome, \@results )
is ( ( $rest_api->clear_hostidentities( [ $results{'unknown hostIdentities'}{'hostIdentities'}[0]{'hostIdentityId'} ] , { } , \%outcome, \@results )
       and $outcome{successful} == scalar @clear_hosts ),
     1, "Basic clearing of identities of one host");
$logger->debug( "CLEARING : " . Dumper \%outcome, \@results);

# Upsert-specific testing
# ==============================
# DOC : Add or update host identities by post. Any number of host identities can be posted to the server in a single asynchronous, (the default), 
#       or synchronous request. Host identity ids will be defaulted by the server, but they can be specified as part of the post body request. 
#       Requests are considered updates if the host identity referenced by id or primary host name exists. 
#       Updates that specify a hostName are assumed to rename the host identity and associated host. 
#       If the update specifies hostNames, these are added as new alternate host names. 

# - create new one : alias for existing host - defaulting the hostidentityid
delete_all_identities(); # Clean up before next batch of tests
@create_new_identities = ( 
    {
  	"hostName"  => "ahost1",
    	"hostNames" => ["ahost1-alias3", "ahost1-alias4"]
    }
);
is ( ( $rest_api->upsert_hostidentities( \@create_new_identities, { }, \%outcome, \@results ) 
     and $outcome{successful} == scalar @create_new_identities ),   # ie the successful prop val in the return json = the same number as the hosts to create aliases for
     1, "Upsert specific test - creating alias for existing host - defaulting the hostIdentityId") ;
$logger->debug( "UPSERT CREATE 1 : " . Dumper \%outcome, \@results) ;

# - create new one : alias for existing host - specifying the hostidentityid
delete_all_identities(); # Clean up before next batch of tests
@create_new_identities = ( 
    {
	"hostIdentityId" => "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa",
  	"hostName"  => "ahost1",
    	"hostNames" => ["ahost1-alias3", "ahost1-alias4"]
    }
);
is ( ( $rest_api->upsert_hostidentities( \@create_new_identities, { }, \%outcome, \@results ) 
     and $outcome{successful} == scalar @create_new_identities ),   # ie the successful prop val in the return json = the same number as the hosts to create aliases for
     1, "Upsert specific test - creating alias for existing host - specifying a valid hostIdentityId") ;
$logger->debug( "UPSERT CREATE 2 : " . Dumper \%outcome, \@results) ;

# - create new one : alias for existing host - specifying the hostidentityid using an invalid uuid 
delete_all_identities(); # Clean up before next batch of tests
@create_new_identities = ( 
    {
	"hostIdentityId" => "an_invalid_id",
  	"hostName"  => "ahost1",
    	"hostNames" => ["ahost1-alias3", "ahost1-alias4"]
    }
);
is ( $rest_api->upsert_hostidentities( \@create_new_identities, { }, \%outcome, \@results ) , 
     0, "Upsert specific test - creating alias for existing host - specifying an invalid hostIdentityId") ;
$logger->debug( "UPSERT CREATE 3 : " . Dumper \%outcome, \@results) ;

# - create new one : alias to alias (ie neither is a host that exists)
delete_all_identities(); # Clean up before next batch of tests
@create_new_identities = ( 
    {
  	"hostName"  => $unlikely_host,
    	"hostNames" => [ "alias1", "alias2" ]
    }
);
is ( ( $rest_api->upsert_hostidentities( \@create_new_identities, { }, \%outcome, \@results ) 
     and $outcome{successful} == scalar @create_new_identities ),   # ie the successful prop val in the return json = the same number as the hosts to create aliases for
     1, "Upsert specific test - creating alias for non-existent host") ;
$logger->debug( "UPSERT CREATE 4 : " . Dumper \%outcome, \@results) ;

# - update one - basic
delete_all_identities(); # Clean up before next batch of tests
@create_new_identities = ( 
    {
  	"hostName"  => "ahost1",
    	"hostNames" => [ "alias1", "alias2" ]
    }
);
is ( ( $rest_api->upsert_hostidentities( \@create_new_identities, { }, \%outcome, \@results ) 
     and $outcome{successful} == scalar @create_new_identities ),   # ie the successful prop val in the return json = the same number as the hosts to create aliases for
     1, "Upsert specific test - updating - step 1 - creating alias ") ;
$logger->debug( "UPDATE 1 - part 1 - create new alias: " . Dumper \%outcome, \@results) ;


# - now update the created alias - using the hostname - add another alias
my @update_identities = ( 
    {
  	"hostName"  => "ahost1", 
    	"hostNames" => [ "a4" ]
    }
);

is ( $rest_api->upsert_hostidentities( \@update_identities, { }, \%outcome, \@results ) , 1, "Upsert specific test - updating part 2 - adding another alias to existing identity by hostname");
$logger->debug( "UPDATE 1 - part 2 - adding an alias to existing identity by hostname : " . Dumper \%outcome, \@results) ;

# - now update the created alias - using the id
is ( $rest_api->get_hostidentities( [ 'ahost1' ] , { }, \%outcome, \%results ), 1 , "UPDATE 1 - part 3 - getting the hostIdentityId");
$logger->debug( "UPDATE 1 - part 3 - getting the hostIdentityId: " . Dumper \%outcome, \%results) ;
my $id = $results{'unknown hostIdentities'}{'hostIdentities'}[0]{hostIdentityId};
@update_identities = ( 
    {
	"hostIdentityId" => $id,
  	"hostName"  => "ahost1", 
    	"hostNames" => [ "a5" ]
    }
);
$msg = "Upsert specific test - updating - part 3 - updating existing host alias set by adding another alias using hostidentityid";
is ( $rest_api->upsert_hostidentities( \@update_identities, { }, \%outcome, \@results ) , 1, $msg);
$logger->debug( "$msg : " . Dumper \%outcome, \@results) ;

# - now update the identity by changing it's hostname
my $newname = "${unlikely_host}_" . time();
@update_identities = ( 
    {
	"hostIdentityId" => $id,
  	"hostName"  =>  $newname,
    	"hostNames" => [ "yet_another_alias" ]
    }
);
$msg = "Upsert specific test - updating - part 4 - changing existing identity hostname";
#is ( ( $rest_api->upsert_hostidentities( \@update_identities, { }, \%outcome, \@results ) and ( $results{'unknown hostIdentities'}{'hostIdentities'}[0]{hostName} eq 'some_new_host_name'    ) ), 1, $msg);
is ( $rest_api->upsert_hostidentities( \@update_identities, { }, \%outcome, \@results ) , 1, $msg);
$logger->debug( "$msg : " . Dumper \%outcome, \@results) ;


# Get-specific testing
# ==============================
# - all non existent
delete_all_identities(); 
$msg = "No host identities found as expected";
is ( ( not $rest_api->get_hostidentities( [  ], { }, \%outcome, \%results) and $outcome{response_code} == 404 ) , 1, $msg);
$logger->debug( "$msg : " . Dumper \%outcome, \@results) ;

# - mix of non existent and existent by hostname
# -- first create an identity
@create_new_identities = ( 
    { "hostName"  => "ahost1", "hostNames" => [ "alias1", "alias2" ] },
    { "hostName"  => "ahost3", "hostNames" => [ "alias31","alias32" ] },
);
$msg = "GET-specfic testing : creating a new identity";
is ( ( $rest_api->upsert_hostidentities( \@create_new_identities, { }, \%outcome, \@results ) and $outcome{successful} == scalar @create_new_identities ),   1, $msg);
$logger->debug( "$msg : " . Dumper \%outcome, \@results) ;
# -- now try to get it and some other non existent one - for exist/non exist mix, no warnings are returned from API, but success for the ones found
$msg = "GET-specfic testing : getting identities by hostname, for 1) existence host 2) non existent host";
my @hosts = ( 'ahost1', 'non_existent_host_alias' );
is ( ( $rest_api->get_hostidentities( \@hosts , { }, \%outcome, \%results ) and ( scalar @{$results{'unknown hostIdentities'}{'hostIdentities'}} == 1 ) )  , 1 , $msg);
$logger->debug( "$msg : " . Dumper \%outcome, \%results) ;

# - get by mix of hostname and id - currently not possible in this version of RAPID
$id = $results{'unknown hostIdentities'}{'hostIdentities'}[0]{hostIdentityId};
@ids = ( "$id", "ahost3" );
$msg = "GET-specfic testing : getting identities by hostIdentityId and hostName mix - this test should fail because RAPID doesn't support searching by things other than hostName in its default mode";
is ( ( $rest_api->get_hostidentities( \@ids , { }, \%outcome, \%results ) and ( scalar @{$results{'unknown hostIdentities'}{'hostIdentities'}} == scalar @ids  ) )  , 1 , $msg); # leaving as expecting 1 for now to highlight this
$logger->debug( "$msg : " . Dumper \%outcome, \%results) ;

# - single by id - have to do this by query
$msg = "GET-specific testing : getting identity by hostIdentityId using query";
is ( $rest_api->get_hostidentities( [] , { query => "hostIdentityId = '$id'" }, \%outcome, \%results ) , 1, $msg);
$logger->debug( "$msg : " . Dumper \%outcome, \%results) ;

# - single by hostname
$msg = "GET-specific testing : getting identity by hostName";
is ( $rest_api->get_hostidentities( [ 'ahost1' ] , { }, \%outcome, \%results ) , 1, $msg);
$logger->debug( "$msg : " . Dumper \%outcome, \%results) ;

# - multiple hostnames
$msg = "GET-specific testing : getting multiple identities by hostNames";
is ( $rest_api->get_hostidentities( [ 'ahost1', 'ahost3' ] , { }, \%outcome, \%results ) , 1, $msg);
$logger->debug( "$msg : " . Dumper \%outcome, \%results) ;

# - multiple by hostname plus count and first options 
$msg = "GET-specific testing : getting multiple identities by hostNames, with count and first options";
is ( $rest_api->get_hostidentities( [ 'ahost1', 'ahost3' ] , { count=>1, first=>1}, \%outcome, \%results ) , 1, $msg);
$logger->debug( "$msg : " . Dumper \%outcome, \%results) ;

# - all - already covered elsewhere

# - query based - with ok query
$msg = "GET-specific testing : query look-up";
is ( $rest_api->get_hostidentities( [ ] , { query => "hostNames.id LIKE 'alias%1' ORDER BY hostName" }, \%outcome, \%results ) , 1, $msg);
$logger->debug( "$msg : " . Dumper \%outcome, \%results) ;

# - query based - with duff query
$msg = "GET-specific testing : duff query look-up";
is ( $rest_api->get_hostidentities( [ ] , { query => "xyz.id LIKE 'alias%1'" }, \%outcome, \%results ) , 0, $msg);
$logger->debug( "$msg : " . Dumper \%outcome, \%results) ;


# Don't delete the host aliases remaining at this point - should be for hosts ahost1 and ahost3 ...
# ... Delete-specific testing...
# ==============================
# - all non existent - not possible - need to pass in details of existing ones to delete so the closest to this test is trying to delete something that doesn't exist
$msg = "DELETE: non existing identity, by id";
is ( ( not $rest_api->delete_hostidentities( [ "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa" ], { }, \%outcome, \@results) and $results[0]{message} eq 'HostIdentity not found, cannot delete' ) , 1, $msg);
$logger->debug( "$msg : " . Dumper \%outcome, \@results) ;
# Now do a get-all - there should be two entries in the results, for hosts ahost1 and ahost3 - ie checking that the delete above didn't wreak havoc
my @identities = ();
get_all_identities_by_id_or_hostname( \@identities , 'hostIdentityId' );
is ( (scalar @identities == 2) , 1, "Found expected number of host identities after failed delete"); 

# - mix of non existent and existent by id - delete_hostidentities will return 0 in this case it seems
$id = shift @identities;
$msg = "DELETE: mix of existing and non existing identities, by id";
is ( ( not $rest_api->delete_hostidentities( [ $id, "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa" ], { }, \%outcome, \@results) and $outcome{successful} == 1 and $outcome{warning} == 1 ) , 1 , $msg);
$logger->debug( "$msg : " . Dumper \%outcome, \@results) ;

# - mix of non existent and existent by hostname - na - cannot delete by hostname contrary to the docs - this test reduced for now to just trying to delete one by hostname
# - also relevant here : single by hostname 
# - also relevant here : multiple by hostnames 
$msg = "DELETE: by hostName - currently this fails which is contrary to the docs which say you can delete by hostname or id";
is ( $rest_api->delete_hostidentities( [ 'ahost3' ], { }, \%outcome, \@results) , 1, $msg ) ;
$logger->debug( "$msg : " . Dumper \%outcome, \@results) ;

# - single by id  - at this point should have just one host left
$msg = "DELETE: delete one by id";
is ( $rest_api->delete_hostidentities( [ $identities[0] ], { }, \%outcome, \@results) , 1, $msg ) ;
$logger->debug( "$msg : " . Dumper \%outcome, \@results) ;

# - mutliple by ids - already covered by delete_all_identities()


# - clear some identities
# first create some ...
@identities = (); # empty this out first !
$msg = "CLEARING test - create and then clear";
@create_new_identities = (
   { "hostName"  => "ahost1", "hostNames" => [ "alias1_1", "alias1_2" ] },
   { "hostName"  => "ahost2", "hostNames" => [ "alias2_1", "alias2_2" ] },
);
is ( ( $rest_api->upsert_hostidentities( \@create_new_identities, { }, \%outcome, \@results ) and $outcome{successful} == scalar @create_new_identities ), 1, $msg);
$logger->debug( "DELETE CLEAR upsert : " . Dumper \%outcome, \@results) ;
@clear_hosts = ( 'ahost1', 'ahost2' );
is ( $rest_api->get_hostidentities( \@clear_hosts , { }, \%outcome, \%results ), 1, "$msg (get all hostidentities by id first)");
get_all_identities_by_id_or_hostname( \@identities , 'hostIdentityId' );
#is ( ( $rest_api->delete_hostidentities( \@identities , { "clear" => "true" } , \%outcome, \@results ) and $outcome{successful} == scalar @clear_hosts ), 1, $msg);
is ( ( $rest_api->clear_hostidentities( \@identities , { } , \%outcome, \@results ) and $outcome{successful} == scalar @clear_hosts ), 1, $msg);
# test that the aliases are actually cleared
$logger->debug( "$msg : " . Dumper \%outcome, \@results); 
$rest_api->get_hostidentities( \@clear_hosts , { }, \%outcome, \%results ); 
foreach my $alias (  @{ $results{'unknown hostIdentities'}{'hostIdentities'} }  ) { 
	is ( (scalar @{$alias->{hostNames}} == 1) , 1, "Cleared - looks ok - just the identity = hostname remaining");
} 

# Auto complete testing...
# =============================
$msg = "AUTOCOMPLETE test - create and then autocomplete - found";
@create_new_identities = (
   { "hostName"  => "ahost1", "hostNames" => [ "alias1_1", "alias1_2" ] },
   { "hostName"  => "ahost2", "hostNames" => [ "alias2_1", "alias2_2" ] },
   { "hostName"  => "Bhost3", "hostNames" => [ "b_a1", "ba_2" ] },
);
is ( ( $rest_api->upsert_hostidentities( \@create_new_identities, { }, \%outcome, \@results ) and $outcome{successful} == scalar @create_new_identities ), 1, $msg);
$logger->debug( "AUTOCOMPLETE CLEAR upsert : " . Dumper \%outcome, \@results) ;
is ( ( $rest_api->get_hostidentities_autocomplete( [ "B" ]  , {},  \%outcome, \%results ) and  (scalar @{$results{'unknown hostIdentities'}{'hostIdentities'}[0]{'suggestions'}} == 3 )) , 1 , $msg);
$logger->debug( "$msg : " . Dumper \%outcome, \%results); 

$msg = "AUTOCOMPLETE test - create and then autocomplete - not found";
is ( ( not $rest_api->get_hostidentities_autocomplete( [ "dingledorks" ] , {},  \%outcome, \%results ) and $outcome{response_code} == 404 ) , 1 , $msg);
$logger->debug( "$msg : " . Dumper \%outcome, \%results); 

$msg = "AUTOCOMPLETE test - autocompletion on multiple prefixes should fail";
is ( $rest_api->get_hostidentities_autocomplete( [ "a", "b" ] , {},  \%outcome, \%results ) , 0 , $msg); $logger->debug( "$msg : " . Dumper \%outcome, \%results); 

$msg = "AUTOCOMPLETE test - autocompletion on no prefixes should fail";
is ( $rest_api->get_hostidentities_autocomplete( [ ] , {},  \%outcome, \%results ) , 0 , $msg); $logger->debug( "$msg : " . Dumper \%outcome, \%results); 

# =============================================================================================
$logger->debug("----- END hostidentities tests ----"); done_testing(); $rest_api = undef; exit;
# =============================================================================================



# ----------------------------------------------------------------------------------------------------------------------
sub delete_all_identities
{
	print "Deleting all host identities...\n";
	my @hostidentityids;
	if ( not get_all_identities_by_id_or_hostname( \@hostidentityids , 'hostIdentityId' ) ) {
		$logger->error("Failed to get all identities by hostIdentityId - quitting");
		exit;
	}
		
	$rest_api->delete_hostidentities( \@hostidentityids, { } , \%outcome, \@results );
	#print "UPSERT: " . Dumper \%outcome, \@results; #exit;
	# after deleting all, there should be none to get
	@hostidentityids = ();
	get_all_identities_by_id_or_hostname( \@hostidentityids , 'hostIdentityId' );
	if ( scalar @hostidentityids ) { 
		$logger->error("After deleting all, there appear to still be some!");
		return 0;
	}
	return 1;
	
}

# ----------------------------------------------------------------------------------------------------------------------
sub get_all_identities_by_id_or_hostname
# returns an array of keys for all host identities
# a key would be one of hostIdentityId or hostName - ie these two things can be used as keys for other ops like delete
{
	my ( $res_ref , $by ) = @_;
	if ( $by ne 'hostIdentityId' and $by ne 'hostName' ) { 
		$logger->error("get_all_identities_by_id_or_hostname() : expected key to be either 'hostIdentityId' or 'hostName'");
		return 0;
	}
	print "Getting all host identities by $by ...\n";
	$rest_api->get_hostidentities( [ ] , { }, \%outcome, \%results );
	if ( defined $results{'unknown hostIdentities'}{'hostIdentities'} ) { 
		foreach my $alias ( @{ $results{'unknown hostIdentities'}{'hostIdentities'} }  ) {
			push @{$res_ref} , $alias->{$by};
		}
	}
	return 1;
}

# ----------------------------------------------------------------------------------------------------------------------
sub create_test_hosts
{
	# creates some test hosts
	my ( $hosts_ref ) = @_;
	my ( @hosts ) ;
	foreach  my $host  ( @{$hosts_ref} ) {
	    push @hosts,
		{
			"hostName"             => $host,
			"description"          => "CREATED at " . localtime,
			"monitorStatus"        => "UP",
			"appType"              => "NAGIOS",
			"deviceIdentification" => $host,
			"monitorServer"        => "localhost",
			"deviceDisplayName"    => $host,
			"properties"           => { "Latency" => "125", "UpdatedBy" => "admin", "Comments" => "Blacklists testing host" }
		};
	    }
    
	    if ( not $rest_api->upsert_hosts(  \@hosts, {}, \%outcome, \@results )  ) {
	    $logger->error("Failed to build test hosts : " . Dumper \%outcome, \@results);
	    return 0;
	}

    return 1;

}

# ----------------------------------------------------------------------------------------------------------------------
sub clear
{
	system("clear");
	print "=" x 80 . "\n";
}

# ----------------------------------------------------------------------------------------------------------------------
sub show_and_die 
{
	my ( $dontdie ) = @_;
	#clear(); 
	print "=" x 80 . "\n";
	$rest_api->get_hostidentities( [ ] , { }, \%outcome, \%results ); 
	print "GET ALL : " . Dumper \%outcome, \%results; 
	if ( not defined $dontdie )  {
		done_testing();
		$rest_api = undef;
 		exit;
	}
}

# ----------------------------------------------------------------------------------------------------------------------
sub array_contains
{
	my ( $array_ref, $search_item ) = @_;
	( grep {$_ eq $search_item} @{$array_ref} ) ? return 1: return 0;
}

# ----------------------------------------------------------------------------------------------------------------------
sub done
{
	$logger->debug("----- END $0 tests ----"); 
	done_testing(); 
	$rest_api = undef; exit;
}


