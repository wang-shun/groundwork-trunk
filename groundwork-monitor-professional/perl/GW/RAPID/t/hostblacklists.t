#!/usr/local/groundwork/perl/bin/perl -w --

# This test script tests the REST API Perl module get/upsert/delete_hostblacklists() routines
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
$logger->debug("----- START get_host_blacklists() tests ----");
my ( %outcome, %results, @results, $query ) = ();

# ----------------------------------------------------------------------------------------------------------------------
# initialize the REST API
my $rest_api = GW::RAPID->new( undef, undef, undef, undef, $requestor, { access => '/usr/local/groundwork/config/ws_client.properties' });

# $rest_api->get_hosts( "depth=shallow&query=property.LastPluginOutput like 'OK%'", \%results   );
# $rest_api->get_hosts( 'localhost?depth=simple&first=0&count=5', \%results, "shallow" ) ;
# $rest_api->get_hosts( "localhost?first=10&count=5", \%results, "shallow" ) ;
# $rest_api->get_hosts( "localhost?depth=simple&first=10&count=1", \%results ) ; die to_json(\%results, {  utf8 => 1 , pretty => 1});

# Exception testing
# the usual expection testing for get/upsert/delete methods
is( ( not $rest_api->get_hostblacklists( "arg1", "arg2", \%outcome, "arg4", "arg5" ) and $outcome{response_error} =~ /Invalid number of args/ ), 1, 'Too many arguments exception' );
is( ( not $rest_api->get_hostblacklists( undef, {}, \%outcome, {} ) and $outcome{response_error} =~ /Undefined arg/ ), 1, 'Undefined argument exception' );
is( ( not $rest_api->get_hostblacklists( [], undef, \%outcome, {} ) and $outcome{response_error} =~ /Undefined arg/ ), 1, 'Undefined argument exception' );
is $rest_api->get_hostblacklists( [], {}, undef, {} ), 0, 'Undefined argument exception'; is( ( not $rest_api->get_hosts( [], {}, \%outcome, undef ) and $outcome{response_error} =~ /Undefined arg/ ), 1, 'Undefined argument exception' );
is( ( not $rest_api->get_hostblacklists( {}, {}, \%outcome, {} ) and $outcome{response_error} =~ /Expecting ARRAY reference/ ), 1, 'Incorrect object type reference exception' );
is( ( not $rest_api->get_hostblacklists( [], [], \%outcome, {} ) and $outcome{response_error} =~ /Expecting HASH reference/ ), 1, 'Incorrect object type reference exception' );
is $rest_api->get_hostblacklists( [], {}, [], {} ), 0, 'Incorrect object type reference exception';
is( ( not $rest_api->get_hostblacklists( [], {}, \%outcome, [] ) and $outcome{response_error} =~ /Expecting HASH reference/ ), 1, 'Incorrect object type reference exception' );
is( ( not $rest_api->get_hostblacklists( "", {}, \%outcome, {} ) and $outcome{response_error} =~ /Expecting ARRAY reference/ ), 1, 'Incorrect object type reference exception' );
is( ( not $rest_api->get_hostblacklists( [], "", \%outcome, {} ) and $outcome{response_error} =~ /Expecting HASH reference/ ), 1, 'Incorrect object type reference exception' );
is $rest_api->get_hostblacklists( [], {}, "", {} ), 0, 'Incorrect object type reference exception';
is( ( not $rest_api->get_hostblacklists( [], {}, \%outcome, "" ) and $outcome{response_error} =~ /Expecting HASH reference/ ), 1, 'Incorrect object type reference exception' );


is $rest_api->upsert_hostblacklists("arg1"), 0, 'Missing arguments exception';
is( ( not $rest_api->upsert_hostblacklists( "arg1", "arg2", \%outcome, "arg4", "arg5" ) and $outcome{response_error} =~ /Invalid number of args/ ), 1, 'Too many arguments exception' );
is( ( not $rest_api->upsert_hostblacklists( undef, {}, \%outcome, [] ) and $outcome{response_error} =~ /Undefined arg/ ), 1, 'Undefined argument exception' );
is( ( not $rest_api->upsert_hostblacklists( [], undef, \%outcome, [] ) and $outcome{response_error} =~ /Undefined arg/ ), 1, 'Undefined argument exception' );
is $rest_api->upsert_hostblacklists( [], {}, undef, [] ), 0, 'Undefined argument exception'; 
is( ( not $rest_api->upsert_hostblacklists( [], {}, \%outcome, undef ) and $outcome{response_error} =~ /Undefined arg/ ), 1, 'Undefined argument exception' );
is( ( not $rest_api->upsert_hostblacklists( {}, {}, \%outcome, [] ) and $outcome{response_error} =~ /Expecting ARRAY reference/ ), 1, 'Incorrect arguments object type reference exception' );
is( ( not $rest_api->upsert_hostblacklists( [], [], \%outcome, [] ) and $outcome{response_error} =~ /Expecting HASH reference/ ), 1, 'Incorrect arguments object type reference exception' );
is $rest_api->upsert_hostblacklists( [], {}, [], [] ), 0, 'Incorrect arguments object type reference exception';
is( ( not $rest_api->upsert_hostblacklists( [], {}, \%outcome, {} ) and $outcome{response_error} =~ /Expecting ARRAY reference/ ), 1, 'Incorrect arguments object type reference exception' );
is( ( not $rest_api->upsert_hostblacklists( "", {}, \%outcome, [] ) and $outcome{response_error} =~ /Expecting ARRAY reference/ ), 1, 'Incorrect arguments object type reference exception' );
is( ( not $rest_api->upsert_hostblacklists( [], "", \%outcome, [] ) and $outcome{response_error} =~ /Expecting HASH reference/ ), 1, 'Incorrect arguments object type reference exception' );
is $rest_api->upsert_hostblacklists( [], {}, "", [] ), 0, 'Incorrect arguments object type reference exception'; 
is( ( not $rest_api->upsert_hostblacklists( [], {}, \%outcome, "" ) and $outcome{response_error} =~ /Expecting ARRAY reference/ ), 1, 'Incorrect arguments object type reference exception' );


is $rest_api->delete_hostblacklists("arg1"), 0, 'Missing arguments exception';
is( ( not $rest_api->delete_hostblacklists( "arg1", "arg2", \%outcome, "arg4", "arg5" ) and $outcome{response_error} =~ /Invalid number of args/ ), 1, 'Too many arguments exception' );
is( ( not $rest_api->delete_hostblacklists( undef, {}, \%outcome, [] ) and $outcome{response_error} =~ /Undefined arg/ ), 1, 'Undefined argument exception' );
is( ( not $rest_api->delete_hostblacklists( [], undef, \%outcome, [] ) and $outcome{response_error} =~ /Undefined arg/ ), 1, 'Undefined argument exception' );
is $rest_api->delete_hostblacklists( [], {}, undef, [] ), 0, 'Undefined argument exception'; 
is( ( not $rest_api->delete_hostblacklists( [], {}, \%outcome, undef ) and $outcome{response_error} =~ /Undefined arg/ ), 1, 'Undefined argument exception' );
is( ( not $rest_api->delete_hostblacklists( {}, {}, \%outcome, [] ) and $outcome{response_error} =~ /Expecting ARRAY reference/ ), 1, 'Incorrect object type reference exception' );
is( ( not $rest_api->delete_hostblacklists( [], [], \%outcome, [] ) and $outcome{response_error} =~ /Expecting HASH reference/ ), 1, 'Incorrect object type reference exception' );
is $rest_api->delete_hostblacklists( [], {}, [], [] ), 0, 'Incorrect object type reference exception';
is( ( not $rest_api->delete_hostblacklists( [], {}, \%outcome, {} ) and $outcome{response_error} =~ /Expecting ARRAY reference/ ), 1, 'Incorrect object type reference exception' );
is( ( not $rest_api->delete_hostblacklists( "", {}, \%outcome, [] ) and $outcome{response_error} =~ /Expecting ARRAY reference/ ), 1, 'Incorrect object type reference exception' );
is( ( not $rest_api->delete_hostblacklists( [], "", \%outcome, [] ) and $outcome{response_error} =~ /Expecting HASH reference/ ), 1, 'Incorrect object type reference exception' );
is $rest_api->delete_hostblacklists( [], {}, "", [] ), 0, 'Incorrect object type reference exception';
is( ( not $rest_api->delete_hostblacklists( [], {}, \%outcome, "" ) and $outcome{response_error} =~ /Expecting ARRAY reference/ ), 1, 'Incorrect object type reference exception' );


# here's the script roughly...
# (don't think this is really required so won't do this for now) create some test hosts and put them in some hostgroup 
# get blacklists for all hosts - should 404
# get blacklist for one of the hosts just created - should 404
# get blacklists for some bogus host - should error or none
# get blacklists for some query - should work
# create new blacklist for some of the hosts - should work
# create them again - should fail (not specifying the id's)
# create some for non existent host - should work
# get blacklists for all hosts - should return some
# get blacklists for some of the hosts - should return results
# delete blacklists for bogus hosts - should fail
# delete blacklists for some of the hosts - should work
# delete non existence bl - should be ok but warning should be set
# do an update with an incorrect id - should fail
# etc

# delete all blacklists first
delete_all_bls();

# create some test hosts
my $unlikely_host = '__x__dorkmunster__y__z_shazem_333211'; # it's really unlikely this host will ever exist - could write a function to return non existent host but this is fine for now
create_test_hosts( [ "ablt1" , "ablt2" , "ablt3", 'ablt4'] );

# get blacklists for all hosts - should 404
is ( ( not $rest_api->get_hostblacklists( [  ], { }, \%outcome, \%results) and $outcome{response_code} == 404 ) , 1, "No blacklists found for all hosts as expected") ;
print "GET all blacklists : " . Dumper \%outcome, \%results; # this will be printed out if not running in prove mode

# get blacklist for one of the hosts just created - should 404
is ( ( not $rest_api->get_hostblacklists( [ 'ablt1' ], { }, \%outcome, \%results) and $outcome{response_code} == 404 ) , 1, "No blacklists found (ablt1 host) as expected") ;
print "GET ablt1 blacklists : " . Dumper \%outcome, \%results;

# get blacklists for some bogus host - should 404
is ( ( not $rest_api->get_hostblacklists( [ $unlikely_host ], { }, \%outcome, \%results) and $outcome{response_code} == 404 ) , 1, "No blacklists found ($unlikely_host host) as expected") ;
print "GET $unlikely_host blacklists : " . Dumper \%outcome, \%results;

my @create_new_blacklists = ( 
	{ 'hostName' => 'ablt1' },
	{ 'hostName' => 'ablt2' },
	{ 'hostName' => 'ablt3' }
);

my @non_host_bls = ( 
	{ 'hostName' => $unlikely_host }
);

# create new blacklist for some of the hosts - should work
is ( $rest_api->upsert_hostblacklists( \@create_new_blacklists, {}, \%outcome, \@results ), 1, "Blacklists created successfully for hosts 'ablt1' and 'ablt2'");
print "UPSERT: " . Dumper \%outcome, \@results;


# create them again - should fail (not specifying the id's implies this is an update but that requires correct id's to be supplied so failure)
is ( $rest_api->upsert_hostblacklists( \@create_new_blacklists, {}, \%outcome, \@results ), 0, "Blacklists created again unsuccessfully for hosts 'ablt1' and 'ablt2'");
print "UPSERT: " . Dumper \%outcome, \@results;

# create some for non existent host - should work
is ( $rest_api->upsert_hostblacklists( \@non_host_bls, {}, \%outcome, \@results ) , 1, "Blacklists created successfully for non existent host");
print "UPSERT: " . Dumper \%outcome, \@results;

# run a query based get and make sure worked as expected 
$query = "hostName LIKE '%dork%' ORDER BY hostName"; # search for the dorky non existent host's bl
is $rest_api->get_hostblacklists( [], { query => $query }, \%outcome, \%results ), 1, "Blacklists successful query ($query)";
print "QUERY GET '$query' blacklists : " . Dumper \%outcome, \%results;
# make sure only 1 result tho
if ( scalar @{$results{'unknown hostBlacklists'}{'hostBlacklists'}} != 1 ) { 
	$logger->error("Hmm something went wrong -expected to find just 1 blacklists but didn't." . Dumper \%outcome, \%results);
	exit;
}

# get the blacklists for all hosts and check them
is ( $rest_api->get_hostblacklists( [  ], { }, \%outcome, \%results) , 1, "Blacklists found for all hosts") ;
print "GET all blacklists : " . Dumper \%outcome, \%results; # this will be printed out if not running in prove mode

my $simple_bl_cmp = {
    hostBlacklistId  => re('.*'),
    hostName     => re('.*'),    
};

foreach my $bl ( @{$results{'unknown hostBlacklists'}{'hostBlacklists'}} ) {
     #$logger->error(" ----- \n" . Dumper $bl);
     cmp_deeply( $bl, $simple_bl_cmp, "Single-blacklist result from ALL : Received expected structure of blacklist result (hostName = $bl->{hostName}, hostBlacklistId = $bl->{hostBlacklistId} )" );
}

# get the blacklists for two hosts and check them too
is ( $rest_api->get_hostblacklists( [ 'ablt1', 'ablt2' ], { }, \%outcome, \%results) , 1, "Blacklists found for hosts ablt1 and ablt2") ;

if ( scalar @{$results{'unknown hostBlacklists'}{'hostBlacklists'}} != 2 ) { 
	$logger->error("Hmm something went wrong -expected to find 2 blacklists but didn't." . Dumper \%outcome, \%results);
	exit;
}

foreach my $bl ( @{$results{'unknown hostBlacklists'}{'hostBlacklists'}} ) {
     #$logger->error(" ----- \n" . Dumper $bl);
     cmp_deeply( $bl, $simple_bl_cmp, "Single-blacklist results host subset : Received expected structure of blacklist result (hostName = $bl->{hostName}, hostBlacklistId = $bl->{hostBlacklistId} )" );
}

# delete the blacklists
my $blacklists = $results{'unknown hostBlacklists'}{'hostBlacklists'};  
is ( $rest_api->delete_hostblacklists( $blacklists, {}, \%outcome, \@results ), 1, "Delete blacklists for ablt1 and ablt2");
print "DELETE : " . Dumper \%outcome, \@results;

# delete the blacklists AGAIN
is ( $rest_api->delete_hostblacklists( $blacklists, {}, \%outcome, \@results ), 1, "Delete blacklists for ablt1 and ablt2");
print "DELETE : " . Dumper \%outcome, \@results;

# at this point, there should be two remaining blacklist, for ablt3 and the wierd named non existent host, so get them and delete them
is ( $rest_api->get_hostblacklists( [  ], { }, \%outcome, \%results) , 1, "Blacklists found for all hosts") ;
print "GET all blacklists : " . Dumper \%outcome, \%results; 
$blacklists = $results{'unknown hostBlacklists'}{'hostBlacklists'};  
is ( $rest_api->delete_hostblacklists( $blacklists, {}, \%outcome, \@results ), 1, "Delete remaining blacklists");
print "DELETE : " . Dumper \%outcome, \@results;

# update with incorrect id should throw error : create a blacklist, get it, change it's id and upsert it again
my @more_blacklists = ( 
	{ 'hostName' => 'ablt4' } ,
);
is ( $rest_api->upsert_hostblacklists( \@more_blacklists, {}, \%outcome, \@results ), 1, "Blacklist created successfully for host 'ablt4'");
print "UPSERT: " . Dumper \%outcome, \@results;
is ( $rest_api->get_hostblacklists( [ 'ablt4' ], { }, \%outcome, \%results) , 1, "Blacklists found for host ablt4") ;
print "UPSERT: " . Dumper \%outcome, \@results;

# check that there's only 1 blacklist just in case
my $s = scalar @results;
if ( $s != 1 ) { 
	$logger->error( "Hmm something has gone wrong - expected at this point to be only 1 blacklist" . Dumper @results);
	exit;
}
$blacklists = $results{'unknown hostBlacklists'}{'hostBlacklists'}[0];  
$blacklists->{hostBlacklistId} = -123; # set id to bogus value
#print "==== adjusted " . Dumper $blacklists;

is ( $rest_api->upsert_hostblacklists( [ $blacklists ]  , {}, \%outcome, \@results) ,  0, "Blacklist failed to successfully update for host 'ablt4' with bogus id");
print "UPSERT FAILURE: " . Dumper \%outcome, \@results;

# deletion of non existent bl for NON EXISTENT host fails...
is ( ( not $rest_api->delete_hostblacklists( [ $unlikely_host ] , {}, \%outcome, \@results ) and $outcome{warning} == 1 ), 1, "Delete with warning on non existence blacklist for non existent host $unlikely_host");
print "DELETE : " . Dumper \%outcome, \@results;

# ... and deletion of non existent bl for EXISTENT host should fail too
is ( ( not $rest_api->delete_hostblacklists( [ 'ablt1' ] , {}, \%outcome, \@results ) and $outcome{warning} == 1 ), 1, "Delete with warning on non existence blacklist for existent host ablt1");
print "DELETE : " . Dumper \%outcome, \@results;

# delete giving a missing hostBlacklistId should work too
my @baddel = ( { 'hostName' => 'ablt4' } ); # ie no hostBlacklistId
is ( $rest_api->delete_hostblacklists( \@baddel, {}, \%outcome, \@results ), 1, "Delete valid blacklist, but deletion by not supplying id");
print "DELETE : " . Dumper \%outcome, \@results;
show_all_bls();

$logger->debug("----- END hostblacklists tests ----");
done_testing();
$rest_api = undef;

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

sub delete_all_bls
{
    $rest_api->get_hostblacklists( [  ], { }, \%outcome, \%results) ;
    print Dumper \%outcome, \%results;
    my $bls = $results{'unknown hostBlacklists'}{'hostBlacklists'};
    if ( $bls ) {
    $rest_api->delete_hostblacklists( $bls, {}, \%outcome, \@results );
        print "DELETE : " . Dumper \%outcome, \%results;
    }
}

sub show_all_bls
{
    $rest_api->get_hostblacklists( [  ], { }, \%outcome, \%results) ;
    print "\n\nALL BLACKLISTS : \n" , Dumper \%outcome, \%results ;
}

sub clear
{
	system("clear");
	print "=" x 80 . "\n";
}

sub done
{
	$logger->debug("----- END $0 tests ----"); 
	done_testing(); 
	$rest_api = undef; exit;
}

