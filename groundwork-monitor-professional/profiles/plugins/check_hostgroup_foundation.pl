#!/usr/local/groundwork/perl/bin/perl -w --
#
#	check_hostgroup_foundation.pl	v1.1.0	10/24/2012
#
#	Copyright 2006-2012 GroundWork Open Source, Inc. ("GroundWork")
#	All rights reserved.  Use is subject to GroundWork commercial license terms.
#
#
# Changelog:
#
# 2007-04-29 Added perfomance data for availability graphing:
# value = 0 for OK, state, .5 for warning state, 1 for Critical
# - Thomas Stocking
#
# 2007-08-22 Changed the performace data for availability graphing to be 0 for critical, 50 for warning, and 100 for OK.
# This matches the FOM graphs we are generating for other multi-measure containers.
# - Thomas Stocking
#
# 2010-09-23 Adjusted the parsing of unscheduled status, to be compatible with 6.x results.
# Added option -i to include scheduled downtime in non-ok status stats.  By default ehse will now be excluded.
# Kudos to Peter Jestico for the suggestion.
# - Thomas Stocking
#
# 2012-10-24 Modified to generally clean up the script, as part of accomodating VEMA hosts.

use strict;
use Getopt::Std;
use CollageQuery;

my $helpstring = "
This script will query the Foundation database for the status of all hosts or services in
the designated host group.  If the host/services that are down/critical exceeds the specified
thresholds, a warning or critical alert is returned.

Options:
-g <hostgroup_name>   Host group containing hosts or services.
-H                    Query for hosts.
-S                    Query for Services.
-w <number>           Number of host down or services critical for warning.
		      Can be specified as a percentage, i.e., 50% (generally
		      recommended over absolute thresholds, when the hostgroup
		      membership may change over time)
		      If not specified, warning alert will not be returned.
-c <number>           Number of host down or services critical for critical.
		      Can be specified as a percentage, i.e., 75% (generally
		      recommended over absolute thresholds, when the hostgroup
		      membership may change over time)
		      If not specified, critical alert will not be returned.
-n <name>,<name>,...  Optional, comma-separated list of service names to query.
		      This is only valid with the -S switch,
		      If not specified, all services will be queried.
-F 		      Toggles Figure of Merit mode. In this mode, all non-ok services or non-up
		      host status measures will be used to determine state.
		      Performance output will shift to the following:
		      Performance counter changes to FOM=n, where n is between 0 and 100.
			(0 if all OK/UP >= n <= 100 if all Non-OK/Down/Unreachable)
		      Percent counter changes to pct_not_up (for hosts) and pct_not_ok (for services)
-i		      Include scheduled downtime in the calculation of the non-ok states.
		      Default is to ignore scheduled downtime (treat as OK/UP).
-d                    Debug mode. Will output additional messages.
-h or -help           Displays help message.

For example, to alert on host group LINUX, warning when 3 hosts are down and
critical when 5 hosts are down, execute the command

check_hostgroup_foundation.pl -g LINUX -H -w 3 -c 5

For example, to alert on host group LINUX, warning when 25% services are critical and
critical when 50% services are critical, on services PING and check_disk, execute the command

check_hostgroup_foundation.pl -g LINUX -S -w 25% -c 50% -n PING,check_disk

Copyright 2006-2012 GroundWork Open Source, Inc. (\"GroundWork\")
All rights reserved. Use is subject to GroundWork commercial license terms.
http://www.gwos.com/
";

my %opt = ();
getopts( 'hHSFidg:w:c:n:', \%opt );
if ( $opt{h} or $opt{help} ) {
    print $helpstring;
    exit 3;
}

# Test for required options
if ( !( $opt{g} and ( $opt{H} or $opt{S} ) ) ) {
    print "ERROR! Required parameter Host group (-g) and Host (-H) or Service (-S) not specified.\n";
    exit 3;
}

#
#	Read options
#
my ( $debug, $hostgroup, $warning, $critical, $querytype, $fom, $includescheduled ) = ();
my @services = ();
$debug     = 1       if $opt{d};              # Change setting to 2 for more detailed debug
$hostgroup = $opt{g} if $opt{g};
$warning   = $opt{w} if defined( $opt{w} );
$critical  = $opt{c} if defined( $opt{c} );
$includescheduled = defined( $opt{i} ) ? 1 : 0;
$querytype = $opt{H} ? 'H' : $opt{S} ? 'S' : '';

if ( $opt{n} ) {
    @services = split /,/, $opt{n};
}
if ( $opt{F} ) {
    $fom = 1;
}
if ($debug) {
    print "Host Group = $hostgroup\n";
    print "Warning Threshold = $warning\n"                       if defined $warning;
    print "Critical Threshold = $critical\n"                     if defined $critical;
    print "Including hosts and services in scheduled downtime\n" if $includescheduled;
    if ( $querytype eq 'H' ) {
	print "Query for Hosts Status.\n";
    }
    elsif ( $querytype eq 'S' ) {
	print "Query for Service Status.\n";
	if (@services) {
	    print "Looking for services:\n";
	    foreach my $service ( sort @services ) {
		print "\t$service\n";
	    }
	}
	else {
	    print "Looking for all services.\n";
	}
    }
    else {
	print "Query type not specified.\n";
    }
}

my $t;
if ( $t = CollageQuery->new() ) {
    print "New CollageQuery object.\n" if $debug;
}
else {
    print "Error: connect to CollageQuery failed!\n";
    exit 3;
}

my $hosts_down_count    = 0;
my $hosts_count         = 0;
my $services_crit_count = 0;
my $services_count      = 0;
my $status              = 0;
my $output              = undef;
my $perf                = undef;
if ( $querytype eq 'H' ) {
    my $ref = $t->getHostsForHostGroup($hostgroup);
    if ( !$ref ) {
	print "UNKNOWN: Please check that hostgroup $hostgroup exists\n";
	exit 3;
    }
    if ($debug) {    # Show all host attributes if in debug
	print "\nSample getHostsForHostGroup method\n";
	print "Getting host attributes for hostgroup $hostgroup\n";
	if ($ref) {
	    foreach my $host ( sort keys %{$ref} ) {
		print "Host=$host\n";
		foreach my $attribute ( sort keys %{ $ref->{$host} } ) {
		    print "\t\t$attribute=" . $ref->{$host}->{$attribute} . "\n";
		}
		my %hash = $t->getHostStatusForHost($host);
		foreach my $key ( sort keys %hash ) {
		    print "\t\t$key=$hash{$key}\n";
		}
	    }
	}
	else {
	    print "No results found.\n";
	    exit 3;
	}
    }
    foreach my $host ( sort keys %{$ref} ) {
	my %hash = $t->getHostStatusForHost($host);
	$hosts_count++;
	if (   ( $includescheduled and $hash{MonitorStatus} eq 'SCHEDULED DOWN' )
	    or ( $hash{MonitorStatus} eq 'UNSCHEDULED DOWN' )
	    or ( $fom && $hash{MonitorStatus} ne 'UP' ) )
	{
	    $hosts_down_count++;
	}
	print "Host $host - $hash{MonitorStatus}\n" if $debug;
    }
    my $down_percent = 0;
    if ( $hosts_count > 0 ) {
	$down_percent = 100 * ( $hosts_down_count / $hosts_count );
    }
    if ( defined($critical)
	and ( ( $critical =~ /([\d\.]+)%$/ and $down_percent >= $1 ) or ( $hosts_down_count >= $critical and $critical !~ /%$/ ) ) )
    {
	$status = 2;
	$output = 'CRITICAL ';
	$perf   = $fom ? "FOM=$down_percent;;;;" : "status=0;;;;";
    }
    elsif ( defined($warning)
	and ( ( $warning =~ /([\d\.]+)%$/ and $down_percent >= $1 ) or ( $hosts_down_count >= $warning and $warning !~ /%$/ ) ) )
    {
	$status = 1;
	$output = 'WARNING ';
	$perf   = $fom ? "FOM=$down_percent;;;;" : "status=50;;;;";
    }
    else {
	$status = 0;
	$output = 'OK ';
	$perf   = $fom ? "FOM=$down_percent;;;;" : "status=100;;;;";
    }
    $down_percent = sprintf '%.02f', $down_percent;
    if ($fom) {
	$perf   .= " pct_not_up=$down_percent";
	$output .= "Total hosts=$hosts_count, Hosts not UP=$hosts_down_count, % not up=$down_percent% | $perf";
    }
    else {
	$perf   .= " pct_down=$down_percent";
	$output .= "Total hosts=$hosts_count, Hosts down=$hosts_down_count, % Down=$down_percent% | $perf";
    }
}

if ( $querytype eq 'S' ) {
    my $ref = $t->getServicesForHostGroup($hostgroup);
    if ( !$ref ) {
	print "UNKNOWN: Please check that hostgroup $hostgroup exists\n";
	exit 3;
    }
    if ( $debug > 1 ) {
	print "\nSample getServicesForHostGroup method\n";
	print "Getting services for hostgroup $hostgroup\n";
	if ($ref) {
	    foreach my $host ( sort keys %{$ref} ) {
		print "Host=$host\n";
		foreach my $service ( sort keys %{ $ref->{$host} } ) {
		    print "\tService=$service\n";
		    foreach my $attribute ( sort keys %{ $ref->{$host}->{$service} } ) {
			print "\t\t$attribute=" . $ref->{$host}->{$service}->{$attribute} . "\n";
		    }
		}
	    }
	}
	else {
	    print "No results found.\n";
	    exit 3;
	}
    }
    my %checkserviceshash = ();
    ## Check if in services array.
    foreach my $key (@services) {
	$checkserviceshash{$key} = 1;
    }
    foreach my $host ( sort keys %{$ref} ) {
	$hosts_count++;
	foreach my $service ( sort keys %{ $ref->{$host} } ) {
	    ## Check if in services array; if not, then skip.
	    next if @services and not $checkserviceshash{$service};
	    $services_count++;
	    print "Host=$host, Service=$service, Status=" . $ref->{$host}->{$service}->{MonitorStatus} . "\n" if $debug;
	    if ( $includescheduled and $ref->{$host}->{$service}->{MonitorStatus} eq 'SCHEDULED CRITICAL' ) {
		$services_crit_count++;
	    }
	    if ( $ref->{$host}->{$service}->{MonitorStatus} eq 'UNSCHEDULED CRITICAL' ) {
		$services_crit_count++;
	    }
	    elsif ( $fom && $ref->{$host}->{$service}->{MonitorStatus} ne 'OK' ) {
		$hosts_down_count++;
	    }
	}
    }
    my $crit_percent = 0;
    if ( $services_count > 0 ) {
	$crit_percent = 100 * ( $services_crit_count / $services_count );
    }
    if (   ( ( $critical =~ /([\d\.]+)%$/ ) and ( $crit_percent >= $1 ) )
	or ( defined($critical) and ( $services_crit_count >= $critical ) and ( $critical !~ /%$/ ) ) )
    {
	$status = 2;
	$output = 'CRITICAL ';
	if ($fom) {
	    my $FOM = ( 100 - $crit_percent );
	    $perf = "FOM=$FOM;;;;";
	}
	else {
	    $perf = "status=0;;;;";
	}
    }
    elsif (( ( $warning =~ /([\d\.]+)%$/ ) and ( $crit_percent >= $1 ) )
	or ( defined($warning) and ( $services_crit_count >= $warning ) and ( $warning !~ /%$/ ) ) )
    {
	$status = 1;
	$output = 'WARNING ';
	$perf   = $fom ? "FOM=$crit_percent;;;;" : "status=50;;;;";
    }
    else {
	$status = 0;
	$output = 'OK ';
	$perf   = $fom ? "FOM=$crit_percent;;;;" : "status=100;;;;";
    }
    $crit_percent = sprintf "%.02f", $crit_percent;
    if ($fom) {
	$perf .= " pct_not_ok=$crit_percent;;;;";
	$output .=
	  "Total hosts=$hosts_count, total services=$services_count, services NOT OK=$services_crit_count, % NOT OK=$crit_percent% |$perf";
    }
    else {
	$perf .= " pct_crit=$crit_percent;;;;";
	$output .=
	  "Total hosts=$hosts_count, total services=$services_count, services critical=$services_crit_count, % Critical=$crit_percent% |$perf";
    }
}
print $output. "\n";
exit $status;

