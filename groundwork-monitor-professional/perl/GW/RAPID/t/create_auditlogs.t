#!/usr/local/groundwork/perl/bin/perl -w --

# This test script tests the REST API Perl module create_auditlogs() routine

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
$logger->debug("----- START create_auditlogs() tests ----");
my ( %outcome, %results, @results ) = ();

# ----------------------------------------------------------------------------------------------------------------------
# initialize the REST API
my $rest_api = GW::RAPID->new( undef, undef, undef, undef, $requestor, { access => '/usr/local/groundwork/config/ws_client.properties' });

# Exception testing
is $rest_api->create_auditlogs(  "arg1" ), 0, 'Missing arguments exception';
is( ( not $rest_api->create_auditlogs( "arg1", "arg2", \%outcome, "arg4", "arg5" ) and $outcome{response_error} =~ /Invalid number of args/ ),
    1, 'Too many arguments exception' );

is( ( not $rest_api->create_auditlogs( undef, {}, \%outcome, [] ) and $outcome{response_error} =~ /Undefined arg/ ),
    1, 'Undefined argument exception' );
is( ( not $rest_api->create_auditlogs( [], undef, \%outcome, [] ) and $outcome{response_error} =~ /Undefined arg/ ),
    1, 'Undefined argument exception' );
is $rest_api->create_auditlogs( [], {}, undef, [] ), 0, 'Undefined argument exception';
is( ( not $rest_api->create_auditlogs( [], {}, \%outcome, undef ) and $outcome{response_error} =~ /Undefined arg/ ),
    1, 'Undefined argument exception' );

is( ( not $rest_api->create_auditlogs( {}, {}, \%outcome, [] ) and $outcome{response_error} =~ /Expecting ARRAY reference/ ),
    1, 'Incorrect object type reference exception' );
is( ( not $rest_api->create_auditlogs( [], [], \%outcome, [] ) and $outcome{response_error} =~ /Expecting HASH reference/ ),
    1, 'Incorrect arguments object type reference exception' );
is $rest_api->create_auditlogs( [], {}, [], [] ), 0, 'Incorrect arguments object type reference exception';
is( ( not $rest_api->create_auditlogs( [], {}, \%outcome, {} ) and $outcome{response_error} =~ /Expecting ARRAY reference/ ),
    1, 'Incorrect arguments object type reference exception' );

is( ( not $rest_api->create_auditlogs( "", {}, \%outcome, [] ) and $outcome{response_error} =~ /Expecting ARRAY reference/ ),
    1, 'Incorrect argument object type reference exception' );
is( ( not $rest_api->create_auditlogs( [], "", \%outcome, [] ) and $outcome{response_error} =~ /Expecting HASH reference/ ),
    1, 'Incorrect argument object type reference exception' );
is $rest_api->create_auditlogs( [], {}, "", [] ), 0, 'Incorrect argument object type reference exception';
is( ( not $rest_api->create_auditlogs( [], {}, \%outcome, "" ) and $outcome{response_error} =~ /Expecting ARRAY reference/ ),
    1, 'Incorrect argument object type reference exception' );

# ------------------------------------------------------------------------------------
# Simple positive test case  - creates some auditlog records
# ------------------------------------------------------------------------------------
my $now = time();

# The known supported enumeration values for the acion are:  'ADD', 'MODIFY', 'DELETE', 'SYNC', 'ENABLE', and 'DISABLE';
my @auditlogs = (
    {
	subsystem   => 'Monarch',
	hostName    => 'localhost',
	action      => 'SYNC',
	description => 'Commit operation was invoked.',
	username    => 'admin'
    },
    {
	subsystem          => 'Monarch',
	hostName           => 'localhost',
	serviceDescription => 'icmp_ping',
	action             => 'ADD',
	description        => 'Added service icmp_ping to host localhost.',
	username           => 'admin'
    },
);

# create a test auditlog record
is $rest_api->create_auditlogs( \@auditlogs, { async => 'false' }, \%outcome, \@results ), 1, "Successfully created test auditlog data on host localhost";
$logger->debug("\\\%outcome:\n", Dumper \%outcome);
$logger->debug("\\\@results:\n", Dumper \@results);

if (0) {
    # ------------------------------------------------------------------------------------
    # try to create auditlog data with malformed instructions - missing required property hostName
    @auditlogs = (
	{
	    subsystem          => 'Monarch',
	    ## hostName           => 'localhost',
	    serviceDescription => 'foobar',
	    action             => 'ADD',
	    description        => 'Added service foobar to host localhost.',
	    username           => 'admin'
	},
    );

    is $rest_api->create_auditlogs( \@auditlogs, {},  \%outcome, \@results ), 0, "Failed to create auditlog data due to missing required property 'device'";
    $logger->debug("\\\%outcome:\n", Dumper \%outcome);
    $logger->debug("\\\@results:\n", Dumper \@results);
}

# MORE TESTS TBD

# Verify that logout (that is, destroying any server-side authentication tokens) works as planned.
is $rest_api->DESTROY(), 1, 'Logout';

# Now have Perl itself call the destructor.  This will call $rest_api->DESTROY() a second time.
$rest_api = undef;

# ------------------------------------------------------------------------------------
$logger->debug("----- END create_auditlogs() tests ----");
done_testing();

