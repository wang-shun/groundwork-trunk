#!/usr/local/groundwork/perl/bin/perl -w --

# Test program for Collage package
# use lib "/usr/local/groundwork/perl-api";
use CollageQuery;

# print Collage->getHostGroups()."\n";

# Alternative use
# use Collage qw(getHostGroups);
# print getHostGroups()."\n";

my $t;
if ( $t = CollageQuery->new() ) {
    print "New CollageQuery object.\n";
}
else {
    die "Error: connect to CollageQuery failed!\n";
}

#
#	CollageHostGroupQuery methods
#
print "\nSample getServicesForHostGroup method\n";
$getparam = "demo-system";
print "Getting services for host $getparam\n";
my $ref = $t->getServicesForHostGroup($getparam);
foreach my $host ( sort keys %{$ref} ) {
    print "Host=$host\n";
    foreach my $service ( sort keys %{ $ref->{$host} } ) {
	print "\tService=$service\n";
	foreach my $attribute ( sort keys %{ $ref->{$host}->{$service} } ) {
	    print "\t\t$attribute=" . $ref->{$host}->{$service}->{$attribute} . "\n";
	}
    }
}

print "\nSample getHostsForHostGroup method\n";
$getparam = "demo-system";
print "Getting services for host $getparam\n";
$ref = $t->getHostsForHostGroup($getparam);
foreach my $host ( sort keys %{$ref} ) {
    print "Host=$host\n";
    foreach my $attribute ( sort keys %{ $ref->{$host} } ) {
	print "\t\t$attribute=" . $ref->{$host}->{$attribute} . "\n";
    }
}

print "\nSample getHostGroups method\n";
$ref = $t->getHostGroups();
foreach my $key ( sort keys %{$ref} ) {
    print "HostGroup=$key\n";
    foreach my $attribute ( sort keys %{ $ref->{$key} } ) {
	print "\t$attribute=" . $ref->{$key}->{$attribute} . "\n";
    }
}

print "\nSample getHostGroup method\n";
$getparam = "demo-system";
print "Getting services for host $getparam\n";
# Note that this formulation of the call does not account for the possibility
# of an error condition, wherein $getparam is not the name of a hostgroup.
my %hash = $t->getHostGroup($getparam);
foreach my $key ( sort keys %hash ) {
    print "\t$key=$hash{$key}\n";
}

#
# CollageHostQuery class methods
#
print "\nSample getServicesForHost method\n";
$getparam = "nagios";
print "Getting services for host $getparam\n";
$ref = $t->getServicesForHost($getparam);
foreach my $service ( sort keys %{$ref} ) {
    print "\tService=$service\n";
    foreach my $attribute ( sort keys %{ $ref->{$service} } ) {
	print "\t\t$attribute=" . $ref->{$service}->{$attribute} . "\n";
    }
}

print "\nSample getHosts method\n";
print "Getting all hosts\n";
$ref = $t->getHosts();
foreach my $host ( sort keys %{$ref} ) {
    print "Host=$host\n";
    foreach my $attribute ( sort keys %{ $ref->{$host} } ) {
	print "\t\t$attribute=" . $ref->{$host}->{$attribute} . "\n";
    }
}

print "\nSample getHostStatusForHost method\n";
$getparam = "nagios";
print "Getting Host Status for host $getparam\n";
# Note that this formulation of the call does not account for the possibility
# of an error condition, wherein $getparam is not the name of a host.
%hash = $t->getHostStatusForHost($getparam);
foreach my $key ( sort keys %hash ) {
    print "$key=$hash{$key}\n";
}

print "\nSample getDeviceForHost method\n";
$getparam = "nagios";
print "Getting Devices for host $getparam\n";
# Note that this formulation of the call does not account for the possibility
# of an error condition, wherein $getparam is not the name of a host.
%hash = $t->getDeviceForHost($getparam);
foreach my $key ( sort keys %hash ) {
    print "\t$key=$hash{$key}\n";
}

#
# CollageServiceQuery class methods
#
print "\nSample getService method\n";
$gethost    = "nagios";
$getservice = "local_disk";
print "Getting Service data for host $gethost, service $getservice.\n";
# Note that this formulation of the call does not account for the possibility
# of an error condition, wherein $gethost and $getservice in combination do
# not refer to an actual host service.
%hash = $t->getService( $gethost, $getservice );
foreach my $key ( sort keys %hash ) {
    print "\t$key=$hash{$key}\n";
}

print "\nSample getServices method\n";
print "Getting services for all hosts\n";
$ref = $t->getServices();
foreach my $host ( keys %{$ref} ) {
    print "Host=$host\n";
    foreach my $service ( keys %{ $ref->{$host} } ) {
	print "\tService=$service\n";
	foreach my $attribute ( keys %{ $ref->{$host}->{$service} } ) {
	    print "\t\t$attribute=" . $ref->{$host}->{$service}->{$attribute} . "\n";
	}
    }
}

#
# CollageMonitorQuery class methods
#
#	getMonitorServers() - return a reference to a hash of monitorserver-attributes
#	getHostsForMonitorServer(String MonitorServer) - return a reference to a hash of hosts for a designated monitorserver
#	getHostGroupsForMonitorServer(String MonitorServer) - return a reference to a hash of host groups-attributes

print "\nSample getMonitorServers method\n";
print "Getting all monitor servers \n";
$ref = $t->getMonitorServers();
foreach my $monitor ( keys %{$ref} ) {
    print "Monitor=$monitor\n";
    foreach my $attribute ( keys %{ $ref->{$monitor} } ) {
	print "\t\t$attribute=" . $ref->{$monitor}->{$attribute} . "\n";
    }
}

print "\nSample getHostsForMonitorServer method\n";
$getparam = "zinal";
print "Getting hosts for monitor server $getparam\n";
$ref = $t->getHostsForMonitorServer($getparam);
foreach my $host ( keys %{$ref} ) {
    print "host=$host\n";
    foreach my $attribute ( keys %{ $ref->{$host} } ) {
	print "\t\t$attribute=" . $ref->{$host}->{$attribute} . "\n";
    }
}

print "\nSample getHostGroupsForMonitorServer method\n";
$getparam = "zinal";
print "Getting hostgroups for monitor server $getparam\n";
$ref = $t->getHostGroupsForMonitorServer($getparam);
foreach my $getHostGroupsForMonitorServer ( keys %{$ref} ) {
    print "hostgroups=$hostgroups\n";
    foreach my $attribute ( keys %{ $ref->{$hostgroups} } ) {
	print "\t\t$attribute=" . $ref->{$hostgroups}->{$attribute} . "\n";
    }
}

#
# CollageEventQuery class methods
#
print "\nSample getEventsForDevice method\n";
$getparam1 = "192.168.1.100";
$getparam2 = "LastInsertDate";
$getparam3 = "2005-05-01 00:00:00";
$getparam4 = "2005-05-12 00:00:00";
print "Getting events for device $getparam1, $getparam2 from $getparam3 to $getparam4.\n";
$ref = $t->getEventsForDevice( $getparam1, $getparam2, $getparam3, $getparam4 );

foreach my $event ( keys %{$ref} ) {
    print "\tEvent=$event\n";
    foreach my $attribute ( keys %{ $ref->{$event} } ) {
	print "\t\t$attribute=" . $ref->{$event}->{$attribute} . "\n";
    }
}

print "\nSample getEventsForService method\n";
$getparam2 = "LastInsertDate";
$getparam3 = "2005-05-01 00:00:00";
$getparam4 = "2005-05-12 00:00:00";
$getparam5 = "nagios";
$getparam6 = "localhost";
print "Getting events for host $getparam5, service $getparam6, $getparam2 from $getparam3 to $getparam4.\n";
$ref = $t->getEventsForService( $getparam5, $getparam6, $getparam2, $getparam3, $getparam4 );
foreach my $event ( keys %{$ref} ) {
    print "\tEvent=$event\n";
    foreach my $attribute ( keys %{ $ref->{$event} } ) {
	print "\t\t$attribute=" . $ref->{$event}->{$attribute} . "\n";
    }
}

print "\nSample getEventsForHost method\n";
$getparam2 = "LastInsertDate";
$getparam3 = "2005-05-01 00:00:00";
$getparam4 = "2005-05-12 00:00:00";
$getparam5 = "nagios";
print "Getting events for host $getparam5, $getparam2 from $getparam3 to $getparam4.\n";
$ref = $t->getEventsForHost( $getparam5, $getparam2, $getparam3, $getparam4 );
foreach my $event ( keys %{$ref} ) {
    print "\tEvent=$event\n";
    foreach my $attribute ( keys %{ $ref->{$event} } ) {
	print "\t\t$attribute=" . $ref->{$event}->{$attribute} . "\n";
    }
}

$t->destroy();
