#!/usr/local/groundwork/perl/bin/perl -w --

# This test script tests the REST API Perl module get_services() routine

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
$logger->debug("----- START get_services() tests ----");
my ( %outcome, %results, @results, $query ) = ();

# ----------------------------------------------------------------------------------------------------------------------
# initialize the REST API
my $rest_api = GW::RAPID->new( undef, undef, undef, undef, $requestor, { access => '/usr/local/groundwork/config/ws_client.properties' });

# Exception testing

is $rest_api->get_services("arg1"), 0, 'Missing arguments exception';
is( ( not $rest_api->get_services( "arg1", "arg2", \%outcome, "arg4", "arg5" ) and $outcome{response_error} =~ /Invalid number of args/ ),
    1, 'Too many arguments exception' );

is( ( not $rest_api->get_services( undef, {}, \%outcome, {} ) and $outcome{response_error} =~ /Undefined arg/ ),
    1, 'Undefined argument exception' );
is( ( not $rest_api->get_services( [], undef, \%outcome, {} ) and $outcome{response_error} =~ /Undefined arg/ ),
    1, 'Undefined argument exception' );
is $rest_api->get_services( [], {}, undef, {} ), 0, 'Undefined argument exception';
is( ( not $rest_api->get_services( [], {}, \%outcome, undef ) and $outcome{response_error} =~ /Undefined arg/ ),
    1, 'Undefined argument exception' );

is( ( not $rest_api->get_services( {}, {}, \%outcome, {} ) and $outcome{response_error} =~ /Expecting ARRAY reference/ ),
    1, 'Incorrect object type reference exception' );
is( ( not $rest_api->get_services( [], [], \%outcome, {} ) and $outcome{response_error} =~ /Expecting HASH reference/ ),
    1, 'Incorrect object type reference exception' );
is $rest_api->get_services( [], {}, [], {} ), 0, 'Incorrect object type reference exception';
is( ( not $rest_api->get_services( [], {}, \%outcome, [] ) and $outcome{response_error} =~ /Expecting HASH reference/ ),
    1, 'Incorrect object type reference exception' );

is( ( not $rest_api->get_services( "", {}, \%outcome, {} ) and $outcome{response_error} =~ /Expecting ARRAY reference/ ),
    1, 'Incorrect object type reference exception' );
is( ( not $rest_api->get_services( [], "", \%outcome, {} ) and $outcome{response_error} =~ /Expecting HASH reference/ ),
    1, 'Incorrect object type reference exception' );
is $rest_api->get_services( [], {}, "", {} ), 0, 'Incorrect object type reference exception';
is( ( not $rest_api->get_services( [], {}, \%outcome, "" ) and $outcome{response_error} =~ /Expecting HASH reference/ ),
    1, 'Incorrect object type reference exception' );

# is <RAPID function>(params) , <expected return value of function>, <test name>
is $rest_api->get_services( [], {}, \%outcome, \%results ), 1, "All services retrieved";
is $rest_api->get_services( ['local_load'], { hostname => 'localhost' }, \%outcome, \%results ), 1,
  "Service local_load for hostname=localhost found";
is $rest_api->get_services( ['local_load'], { hostname => '_N_A_H_' }, \%outcome, \%results ), 0,
  "Service local_load for hostname=_N_A_H_ not found";

# FIX MAJOR:  This is not working as planned; it currently returns all services for all hosts, even
# though we explicitly tried to apply a filter that would return only services for a particular host.
is $rest_api->get_services( [], { hostname => 'localhost' }, \%outcome, \%results ), 1, "Services for hostname=localhost found";
$logger->debug("\\\%outcome:\n", Dumper \%outcome);
$logger->debug("\\\%results:\n", Dumper \%results);
## FIX MAJOR:  clean this up
if (%results) {
    $logger->debug( "got " . (scalar keys %results) . " services returned at the client level" );
    foreach my $key ( keys %results ) {
	$logger->debug("result key = $key");
    }
}
else {
    $logger->debug("results for all services on localhost is empty");
}

is $rest_api->get_services( [], { query => "description='local_cpu_java'" }, \%outcome, \%results ), 1,
  "Services on all hosts for service local_cpu_java found";

my $hostname_base = "__RAPID_test_host_";
my $now = time(); 
my $hostname = $hostname_base . $now; 
my $hostname_2 = $hostname . "_2"; 

# delete any test hosts first
my $delete_command = "$PSQL -c \"delete from host where hostname like '${hostname_base}_%';\" gwcollagedb;";
print "Removing collage test hosts ${hostname_base}* with '$delete_command'\n";
if ( system($delete_command ) >> 8 ) { print "Command '$delete_command' failed!! Quitting\n"; exit; } 
my @hosts = (
    {
        "hostName"             => $hostname,
        "description"          => "CREATED at " . localtime,
        "monitorStatus"        => "UP",
        "appType"              => "NAGIOS",
        "deviceIdentification" => "1.2.3.4",
        "monitorServer"        => "localhost",
        "deviceDisplayName"    => $hostname,
        "properties"           => { "Latency" => "125", "UpdatedBy" => "admin", "Comments" => "Testing host #1" }
    },
    {
        "hostName"             => $hostname_2,
        "description"          => "CREATED at " . localtime,
        "monitorStatus"        => "DOWN", 
        "appType"              => "NAGIOS",
        "deviceIdentification" => "5,6,7,8",
        "monitorServer"        => "localhost",
        "deviceDisplayName"    => $hostname_2,
        "properties"           => { "Latency" => "150", "UpdatedBy" => "admin", "Comments" => "Testing host #2" }
    }
);

# create test hosts to which we can then attach host services so we can test getting those services
is $rest_api->upsert_hosts(  \@hosts, {}, \%outcome, \@results ), 1, "Successfully created hosts $hostname and $hostname_2";

my $servicename_base = "__RAPID_test_service__";
my $servicename      = $servicename_base . time(); 

# delete any test host services first
$delete_command =
"$PSQL -c \"delete from servicestatus where servicedescription like '${servicename_base}%' and hostid in ( select hostid from host where hostname = '$hostname' or hostname = '$hostname_2' );\" gwcollagedb;";
print "Removing collage test services ${servicename_base}* from $hostname and $hostname_2 with '$delete_command'\n";
if ( system($delete_command ) >> 8 ) { print "Command '$delete_command' failed!! Quitting\n"; exit; } 
my @services = (
    {
        'lastCheckTime'        => '2013-05-22T09:36:47-07:00',
        'deviceIdentification' => '1.2.3.4',
        'nextCheckTime'        => '2013-05-22T09:46:47-07:00',
        'lastHardState'        => 'PENDING',
        'monitorStatus'        => 'OK',
        'description'          => $servicename,
        'properties' =>
          { 'Latency' => '950', 'ExecutionTime' => '7', 'MaxAttempts' => '3', 'LastPluginOutput' => 'ORIGINAL output from test service' },
        'stateType'       => 'HARD', 
        'hostName'        => $hostname,
        'appType'         => 'NAGIOS',
        'monitorServer'   => 'localhost',
        'checkType'       => 'ACTIVE',
        'lastStateChange' => '2013-05-22T09:36:47-07:00'
    },
    {
        'lastCheckTime'        => '2013-05-22T09:36:47-07:00',
        'deviceIdentification' => '5.6.7.8',
        'nextCheckTime'        => '2013-05-22T09:46:47-07:00',
        'lastHardState'        => 'PENDING',
        'monitorStatus'        => 'OK',
        'description'          => $servicename,
        'properties' =>
          { 'Latency' => '950', 'ExecutionTime' => '7', 'MaxAttempts' => '3', 'LastPluginOutput' => 'ORIGINAL output from test service' },
        'stateType'       => 'HARD', 
        'hostName'        => $hostname_2,
        'appType'         => 'NAGIOS',
        'monitorServer'   => 'localhost',
        'checkType'       => 'ACTIVE',
        'lastStateChange' => '2013-05-22T09:36:47-07:00'
    },
);

# create test services attached to our previously created test hosts
is $rest_api->upsert_services( \@services, {}, \%outcome, \@results ), 1, "Successfully created service $servicename on hosts $hostname and $hostname_2";

# This test is here to check returning data for services already instantiated on multiple hosts.
is $rest_api->get_services( [], { query => "description='$servicename'" }, \%outcome, \%results ), 1,
  "Services on all hosts for service $servicename found";
$logger->debug("\\\%outcome:\n", Dumper \%outcome);
$logger->debug("\\\%results:\n", Dumper \%results);

# This is a legal but non-recommended form of query.
is $rest_api->get_services( [], { query => "hostName='$hostname'" }, \%outcome, \%results ), 1,
  "Services on host $hostname for all services found";

is $rest_api->get_services( [], { hostname => $hostname_2 }, \%outcome, \%results ), 1,
  "Services on host $hostname_2 for all services found";

# This is a legal but non-recommended form of query.
is $rest_api->get_services( [$servicename], { query => "hostName='$hostname'" }, \%outcome, \%results ), 1,
  "Services on host $hostname for service $servicename found";

is $rest_api->get_services( [$servicename], { hostname => $hostname_2 }, \%outcome, \%results ), 1,
  "Services on host $hostname_2 for service $servicename found";

is $rest_api->get_services( [$servicename], {}, \%outcome, \%results ), 1,
  "Services on host $hostname and $hostname_2 for service $servicename found";

# This is a legal but non-recommended form of query.
is $rest_api->get_services( [$servicename], { query => "hostName in ('$hostname', '$hostname_2')" }, \%outcome, \%results ), 1,
  "Services on hosts $hostname and $hostname_2 for service $servicename found";

is $rest_api->get_services( [$servicename], { hostname => [ $hostname, $hostname_2 ] }, \%outcome, \%results ), 1,
  "Services on hosts $hostname and $hostname_2 for service $servicename found";

is $rest_api->get_services( [], { hostname => [ 'localhost', $hostname, $hostname_2 ] }, \%outcome, \%results ), 1,
  "Services on hosts localhost, $hostname, and $hostname_2 found by id";
if (%results) {
    $logger->debug( "got " . ( scalar keys %results ) . " services returned by id at the client level" );
    foreach my $key ( keys %results ) {
	$logger->debug("found result{$key}");
    }
}
else {
    $logger->debug("results for all services on three machines is empty");
}

is $rest_api->get_services( [], { hostname => [ 'localhost', $hostname, $hostname_2 ], format => 'host,service' }, \%outcome, \%results ), 1,
  "Services on hosts localhost, $hostname, and $hostname_2 found by host,service";
if (%results) {
    $logger->debug( "got " . ( scalar keys %results ) . " hostnames returned at the client level" );
    foreach my $key ( keys %results ) {
	foreach my $subkey ( keys %{ $results{$key} } ) {
	    $logger->debug("found result{$key}{$subkey}");
	}
    }
}
else {
    $logger->debug("results for all services on three machines is empty");
}

is $rest_api->get_services( [], { hostname => [ 'localhost', $hostname, $hostname_2 ], format => 'service,host' }, \%outcome, \%results ), 1,
  "Services on hosts localhost, $hostname, and $hostname_2 found by service,host";
if (%results) {
    $logger->debug( "got " . ( scalar keys %results ) . " servicenames returned at the client level" );
    foreach my $key ( keys %results ) {
	foreach my $subkey ( keys %{ $results{$key} } ) {
	    $logger->debug("found result{$key}{$subkey}");
	}
    }
}
else {
    $logger->debug("results for all services on three machines is empty");
}

is $rest_api->get_services( ['___DDD___'], { hostname => "localhost" }, \%outcome, \%results ), 0,
  "Service ___DDD___ for hostName=localhost not found";
is $rest_api->get_services( ['local_load'], { hostname => "localhost" }, \%outcome, \%results ), 1,
  "Service local_load for hostName=localhost found";

$query = "monitorStatus = 'OK'";
is $rest_api->get_services( [], { query => $query }, \%outcome, \%results ), 1, "Simple successful query-based service/host ($query)";

# Deeper positive single and multi host retrieval testing
my $service_cmp = {
    properties       => re('.*'),
    id               => re('.*'),
    appType          => re('.*'),
    description      => re('.*'),
    monitorStatus    => re('.*'),
    lastCheckTime    => re('.*'),
    nextCheckTime    => re('.*'),
    lastStateChange  => re('.*'),
    hostName         => re('.*'),
    stateType        => re('.*'),
    checkType        => re('.*'),
    lastHardState    => re('.*'),
    monitorServer    => re('.*'),
    lastPlugInOutput => re('.*'),
    deviceIdentification => re('.*'),
};

# Simple successful *host/service* retrieval test
is $rest_api->get_services( ["local_load"], { hostname => "localhost" }, \%outcome, \%results ), 1, "Service local_load for hostName=localhost found";
foreach my $key (keys %results) {
    cmp_deeply( $results{$key}, $service_cmp, "local_load/localhost result: Got expected structure for '$results{$key}{description}/$results{$key}{hostName}'" );
}

# Simple successful *hosts/services* retrieval test
# skipping - structure can vary
#is $rest_api->get_services( [], {}, \%outcome, \%results  ), 1, "No service or hostname specified - all hosts services should be returned";
#foreach my $got_v ( @{$results{services}} )
#{
#    cmp_deeply($got_v, $service_cmp, "Multi-service/host result: Got expected structure of service/host result $got_v->{description} / $got_v->{hostName}") ;
#}

# Verify that logout (that is, destroying any server-side authentication tokens) works as planned.
is $rest_api->DESTROY(), 1, 'Logout';

# Now have Perl itself call the destructor.  This will call $rest_api->DESTROY() a second time.
$rest_api = undef;

# ----------------------------------------------------------------------------------------------------------------------
$logger->debug("----- END get_services() tests ----");
done_testing();

