#!/usr/local/groundwork/perl/bin/perl -w --

# This test script tests for existence of the importable functions.
# If these change, then the docs need changing amongst other things.

use strict;
use warnings;

use GW::RAPID;
use Test::More;         #qw(no_plan);
use Test::Exception;    #qw(no_plan);
use lib '/usr/local/groundwork/core/foundation/api/perl/lib';
use Log::Log4perl qw(get_logger);

Log::Log4perl::init('GW_RAPID.log4perl.conf');

my $logger = get_logger("GW.RAPID.module");
$logger->debug("----- START initialization tests ----");

# ------------------------------------------------------------

can_ok( 'GW::RAPID', "new" );
can_ok( 'GW::RAPID', "get_hosts" );
can_ok( 'GW::RAPID', "get_hostgroups" );
can_ok( 'GW::RAPID', "get_services" );
can_ok( 'GW::RAPID', "get_events" );
can_ok( 'GW::RAPID', "get_devices" );
can_ok( 'GW::RAPID', "get_categories" );
can_ok( 'GW::RAPID', "upsert_hosts" );
can_ok( 'GW::RAPID', "upsert_hostgroups" );
can_ok( 'GW::RAPID', "upsert_services" );
can_ok( 'GW::RAPID', "upsert_devices" );
can_ok( 'GW::RAPID', "upsert_categories" );
can_ok( 'GW::RAPID', "create_events" );
can_ok( 'GW::RAPID', "update_events" );
can_ok( 'GW::RAPID', "delete_hosts" );
can_ok( 'GW::RAPID', "delete_hostgroups" );
can_ok( 'GW::RAPID', "delete_services" );
can_ok( 'GW::RAPID', "delete_devices" );
can_ok( 'GW::RAPID', "delete_events" );
can_ok( 'GW::RAPID', "delete_categories" );
can_ok( 'GW::RAPID', "update_events" );
can_ok( 'GW::RAPID', "analyze_error" );
can_ok( 'GW::RAPID', "analyze_upsert_response" );
can_ok( 'GW::RAPID', "analyze_delete_response" );
can_ok( 'GW::RAPID', "analyze_put_response" );
can_ok( 'GW::RAPID', "is_valid_dns_hostname" );

# ------------------------------------------------------------
$logger->debug("----- END interface tests ----");
done_testing();

