#!/usr/local/groundwork/perl/bin/perl -w --

# This test script tests the REST API Perl module get_hosts() routine after an internal timeout failure.

use warnings;
use strict;

# FIX MAJOR:  This is one form of workaround for the underscore-to-dash translation issue.
# It should be fixed instead by renaming the APP_NAME and GWOS_API_TOKEN header names.
use HTTP::Headers;
$HTTP::Headers::TRANSLATE_UNDERSCORE = 0;

use GW::RAPID;
use Test::More;
use Test::Deep;
use Test::Exception;
use JSON;
use Data::Dumper; $Data::Dumper::Indent   = 1; $Data::Dumper::Sortkeys = 1;
use File::Basename; my $requestor = "RAPID-" . basename($0);

use lib '/usr/local/groundwork/core/foundation/api/perl/lib';
use Log::Log4perl qw(get_logger);

Log::Log4perl::init('GW_RAPID.log4perl.conf');

my $logger = get_logger("GW.RAPID.module");
$logger->debug("----- START get_hosts() tests ----");
my ( %outcome, %results, @results, $query ) = ();

# ----------------------------------------------------------------------------------------------------------------------
# initialize the REST API
my $rest_api = GW::RAPID->new( undef, undef, undef, undef, $requestor, { access => '/usr/local/groundwork/config/ws_client.properties' });

is $rest_api->get_hosts( ["localhost"], {}, \%outcome, \%results ), 1, "Host localhost found";
$logger->debug("\\\%outcome:\n", Dumper \%outcome);
$logger->debug("\\\%results:\n", Dumper \%results);

# FIX LATER:  This sleep presumes that the server has been configured with a session timeout of perhaps 2 seconds.
# We want to sleep longer than that, to force the following call to fail, and then internally
# re-login and retry the failing query before returning a final result to the caller.
my $sleep_seconds = 65;
$logger->debug("NOTICE:  SLEEPING FOR $sleep_seconds SECONDS TO ALLOW THE API TO TIME OUT");
sleep $sleep_seconds;

is $rest_api->get_hosts( ["localhost"], { depth => "shallow" }, \%outcome, \%results ), 1, "Host localhost found, depth = shallow";
$logger->debug("\\\%outcome:\n", Dumper \%outcome);
$logger->debug("\\\%results:\n", Dumper \%results);

# Verify that logout (that is, destroying any server-side authentication tokens) works as planned.
is $rest_api->DESTROY(), 1, 'Logout';

# Now have Perl itself call the destructor.  This will call $rest_api->DESTROY() a second time.
$rest_api = undef;

# ----------------------------------------------------------------------------------------------------------------------
$logger->debug("----- END get_hosts() tests ----");
done_testing();

