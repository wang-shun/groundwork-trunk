#!@PERL_DIR@ --
#
#	Copyright 2005-2011 GroundWork Open Source, Inc. ("GroundWork")
#	All rights reserved. Use is subject to GroundWork commercial license terms.
#

my $helpstring = "
FIX MINOR:  add description here

	Options:
		-c <Nagios config file>	Nagios config file directory and name, usually /usr/local/nagios/etc/nagios.cfg
		-L <OUTPUT LOG>		Log file containing status messages from this program
		-d		Debug mode. Will log additional messages to the log file
		-h		Displays help message.

	GroundWork Monitor - The ultimate data integration framework.
	Copyright (C) 2008, 2011 GroundWork Open Source
	www.groundworkopensource.com

	This program is free software; you can redistribute it and/or modify
	it under the terms of version 2 of the GNU General Public License
	as published by the Free Software Foundation.

	This program is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
	GNU General Public License for more details.

	You should have received a copy of the GNU General Public License
	along with this program; if not, write to the Free Software
	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA
";

use strict;
use Time::Local;
use Getopt::Std;
use IO::Socket;
use DBI;
use CollageQuery;

my $Logfile = "" ;	# Log file for this program.
my $remote_host = "localhost";
my $remote_port = 4913;
my $thisnagios = "localhost";

my $debug = 1;	# Set to 1 for debug mode.
my $testmode = 0;   # Set to 1 to disable socket communications for testing
my $usemonarch = 1;   # Set to 1 to get synch Collage database with monarch. If 0, will synch Collage with nagios config files
my $orphan_hostgroupname="__Hosts not in any host group";

my $logfile;
my $hostgroupConfig;
my $socket;
my $hostlist;

my %opt = ();	# Program options hash
getopts("dhL:c:",\%opt);
if ($opt{h} or $opt{help}) {
	print $helpstring;
	exit;
}
if ($opt{d}) { $debug = 1;}
if ($opt{L}) {
	$logfile = $opt{L};
} else {
	$logfile = "@FEEDER_LOG@/nagios2collage_hostgroupcheck.log";
}
if ($opt{c}) {
	$hostgroupConfig = $opt{c};
} else {
	$hostgroupConfig = "/usr/local/groundwork/nagios/etc/nagios.cfg";
}

open (LOG,">$logfile");
print LOG "Program started at ".time_text(time)."\n";
print LOG "Debug: $debug\n";
print LOG "Output log: $logfile\n";
if (!$usemonarch) {
	print LOG "Nagios config file: $hostgroupConfig\n";
} else {
	print LOG "Synching with Monarch database.\n";
}

my ($hg_ref,$host_ref) = ();
if ($usemonarch) {
	($hg_ref,$host_ref) = read_hostgroup_monarch();					# Read hostgroup config from monarch
} else {
	($hg_ref,$host_ref) = read_hostgroup_nagios($hostgroupConfig);	# Read hostgroup config from nagios config files
}
#
#	Now get all hosts to make sure we don't have any hosts that are not in hostgroups
#
my $t;
my $collage_hosts_ref = ();
my $collage_hostgroups_ref = ();
if ($t=CollageQuery->new()) {
	$collage_hosts_ref = $t->getHosts();
	$collage_hostgroups_ref = $t->getHostGroups();
	if ($collage_hosts_ref) {
		foreach my $host (sort keys %{$collage_hosts_ref}) {
			if (my $services_ref = $t->getServicesForHost($host)) {
				foreach my $service (sort keys %{$services_ref}) {
					$collage_hosts_ref->{$host}->{"SERVICES"}->{$service}->{"EXISTS"} = 1;
				}
			}
		}
	} else {
		print LOG "No hosts found in Collage database\n";
	}
} else {
	print LOG "Unable to connect to Collage database.\n";
}
if ($debug) {
	foreach my $hg (sort keys %$hg_ref) {
		print LOG "Host Group=$hg\n";
		foreach my $host (sort keys %{$hg_ref->{$hg}->{HOSTS}} ) {
			print LOG "\tHost=$host\n";
		}
	}
	print LOG "Collage hosts:\n";
	if ($collage_hosts_ref) {
		my $count = 0;
		my $servicecount = 0;
		foreach my $host (sort keys %{$collage_hosts_ref}) {
			print LOG "\tHost=$host\n";
			$count++;
			foreach my $service (sort keys %{$collage_hosts_ref->{$host}->{"SERVICES"}}) {
				print LOG "\t\tService=$service\n";
				$servicecount++;
			}
		}
		print LOG "$count hosts, $servicecount services found in Collage database\n";
	} else {
		print LOG "No hosts found in Collage database\n";
	}
	print LOG "Nagios hosts:\n";
	if ($host_ref) {
		my $count = 0;
		my $servicecount = 0;
		foreach my $host (sort keys %{$host_ref}) {
			print LOG "\tHost=$host, Address=".$host_ref->{$host}->{"ADDRESS"}."\n";
			$count++;
			foreach my $service (sort keys %{$host_ref->{$host}->{"SERVICES"}}) {
				print LOG "\t\tService=$service\n";
				$servicecount++;
			}
		}
		print LOG "$count hosts, $servicecount services found in Nagios config files\n";
	} else {
		print LOG "No hosts found in Nagios config files\n";
	}
}
if (!$testmode) {
	my $maxretries = 10;
	my $retrycount = 1;
	while (!($socket = IO::Socket::INET->new(PeerAddr => $remote_host, PeerPort => $remote_port, Proto => "tcp", Type => SOCK_STREAM) ) )
	{
		print "Attempt $retrycount. Couldn't connect to $remote_host:$remote_port : $@\n";
		print LOG "Attempt $retrycount. Couldn't connect to $remote_host:$remote_port : $@\n";
		$retrycount++;
		if ($retrycount > $maxretries) {
			print "Max tries exceeded. Exiting\n";
			print LOG "Max tries exceeded. Exiting\n";
			exit;
		}
		sleep 10;
	}
}
print "Connected to collage.\n";
print LOG "Connected to collage.\n";
print $socket "<GENERICLOG consolidation='SYSTEM' ApplicationType='SYSTEM' MonitorServerName='localhost' Device='$remote_host' Severity='OK' MonitorStatus='OK' TextMessage='Foundation-Nagios synch process completed.' />";
# Send Hostgroup members to Collage
if ($host_ref) {
	foreach my $host  (sort keys %$host_ref) {
		if (!($collage_hosts_ref->{$host})) {		# Check if the host is in collage. If not, the add
			my $xmlstring = formatxml_host_add($thisnagios,$host,$host_ref->{$host}->{"ADDRESS"});
			print $socket $xmlstring if (!$testmode) ;
			print LOG $xmlstring."\n\n" if $debug;
		}
	}
}
if ($hg_ref) {
	foreach my $hg  (sort keys %$hg_ref) {
		$hostlist = "";
		foreach my $host (sort keys %{$hg_ref->{$hg}->{HOSTS}}) {
			$hostlist .= $host.",";
			if ($host_ref->{$host}) {
				$host_ref->{$host}->{"IN_HOSTGROUP"} = 1;
			}


		}
		$hostlist =~ s/,$//;	# take off last comma
		my $xmlstring = formatxml_hostgroup_add($hg,$hostlist);
		print $socket $xmlstring if (!$testmode) ;
		print LOG $xmlstring."\n\n" if $debug;
	}
}
if ($collage_hostgroups_ref) {
# Delete Hostgroups that no are no longer in Nagios
	foreach my $collage_hg  (sort keys %$collage_hostgroups_ref) {
		if ($hg_ref->{$collage_hg}) {
			$collage_hostgroups_ref->{$collage_hg}->{"IN_NAGIOS"} = 1;
		}
	}
	foreach my $collage_hg  (sort keys %$collage_hostgroups_ref) {
		if ((!$collage_hostgroups_ref->{$collage_hg}->{"IN_NAGIOS"}) and ($collage_hg ne $orphan_hostgroupname)) {
			my $xmlstring = formatxml_hostgroup_delete($collage_hg);
			print $socket $xmlstring if (!$testmode) ;
			print LOG $xmlstring."\n\n" if $debug;
			delete $collage_hostgroups_ref->{$collage_hg};
		}
	}
}

# Delete Hosts that no are no longer in Nagios
if ($collage_hosts_ref) {
	foreach my $collage_host  (sort keys %$collage_hosts_ref) {
		if ($host_ref->{$collage_host}) {
			$collage_hosts_ref->{$collage_host}->{"IN_NAGIOS"} = 1;
			# Delete Services that no are no longer in Nagios
			foreach my $collage_service (sort keys %{$collage_hosts_ref->{$collage_host}->{"SERVICES"}}) {
				if ($host_ref->{$collage_host}->{"SERVICES"}->{$collage_service}) {
					$collage_hosts_ref->{$collage_host}->{"SERVICES"}->{$collage_service}->{"IN_NAGIOS"} = 1;
				}
			}
		}
	}
	foreach my $collage_host  (sort keys %$collage_hosts_ref) {
		if (!$collage_hosts_ref->{$collage_host}->{"IN_NAGIOS"}) {
			my $xmlstring = formatxml_host_delete($collage_host);
			print $socket $xmlstring if (!$testmode) ;
			print LOG $xmlstring."\n\n" if $debug;
			my $xmlstring = formatxml_device_delete($collage_host);
			print $socket $xmlstring if (!$testmode) ;
			print LOG $xmlstring."\n\n" if $debug;
			delete $collage_hosts_ref->{$collage_host};
		}

		if ($usemonarch) {	# If synching with monarch, delete unused service. Don't do with Nagios because we haven't parsed for services.
			foreach my $collage_service (sort keys %{$collage_hosts_ref->{$collage_host}->{"SERVICES"}}) {
				if (!$collage_hosts_ref->{$collage_host}->{"SERVICES"}->{$collage_service}->{"IN_NAGIOS"}) {
					my $xmlstring = formatxml_service_delete($collage_host,$collage_service);
					print $socket $xmlstring if (!$testmode) ;
					print LOG $xmlstring."\n\n" if $debug;
					delete $collage_hosts_ref->{$collage_host}->{"SERVICES"}->{$collage_service};
				}
			}
		}
	}
}
#
#	Create orphans hostgroup
$hostlist = "";
#if ($collage_hosts_ref) {
#	foreach my $host (sort keys %{$collage_hosts_ref}) {
#		if (!($collage_hosts_ref->{$host}->{"IN_HOSTGROUP"})) {
#			$hostlist .= $host.",";
#		}
#	}
#}

if ($host_ref) {
	foreach my $host (sort keys %{$host_ref}) {
#		if (!($collage_hosts_ref->{$host}->{"IN_HOSTGROUP"})) {
#			$hostlist .= $host.",";
#		}
		if (!($host_ref->{$host}->{"IN_HOSTGROUP"})) {
			$hostlist .= $host.",";
		}
	}
}

if ($hostlist ne "") {
	$hostlist =~ s/,$//;	# take off last comma
	my $xmlstring = formatxml_hostgroup_add($orphan_hostgroupname,$hostlist);
	print $socket $xmlstring if (!$testmode) ;
	print LOG $xmlstring."\n\n" if $debug;
} else {
	my $xmlstring = formatxml_hostgroup_delete($orphan_hostgroupname);
	print $socket $xmlstring if (!$testmode) ;
	print LOG $xmlstring."\n\n" if $debug;
}
CommandClose($socket) if (!$testmode) ;
close($socket) if (!$testmode) ;

exit;

sub unencode {
	my $coded_string = shift;
	$coded_string =~ s/%([0-9a-f]{2})/pack("c",hex($1))/gie;
	return $coded_string;
}


sub formatxml_hostgroup_add {
#<SYSTEM_CONFIG action="add" destination="HostGroup" destinationName="demo-servers" collection="Host" collectionNames="torino,geneva,asti" />
	my $hostgroup=shift;
	my $hostlist=shift;
	my $xml_message = "<SYSTEM_CONFIG action=\"add\" destination=\"HostGroup\" ";	# Start message tag
	$xml_message .= "destinationName=\"$hostgroup\" ";
	$xml_message .= "collection=\"Host\" ";
	$xml_message .= "collectionNames=\"$hostlist\" ";
	$xml_message .= "/>";
    return $xml_message;
}

sub formatxml_hostgroup_delete {
#<ADMIN SessionID="12345" Action="remove" Type="HostGroup" Name="monitor-server" />
	my $hostgroup=shift;
	my $xml_message = "<ADMIN SessionID=\"12345\" Action=\"remove\" Type=\"HostGroup\" ";	# Start message tag
	$xml_message .= "Name=\"$hostgroup\" ";
	$xml_message .= "/>";
    return $xml_message;
}
sub formatxml_host_add {
#<HOST_STATUS MonitorServerName="localhost" Host="localhost" Device=="127.0.0.1" CheckTypeID="0" CurrentNotificationNumber="0" LastCheckTime="0000-00-00 00:00:00" LastNotificationTime="0" LastPluginOutput="" LastStateChange="0000-00-00 00:00:00" MonitorStatus="UP" PercentStateChange="0.00" ScheduledDowntimeDepth="0" TimeDown="0" TimeUnreachable="0" TimeUp="0" isAcknowledged="0" isChecksEnabled="0" isEventHandlersEnabled="0" isFailurePredictionEnabled="0" isFlapDetectionEnabled="0" isHostIsFlapping="0" isNotificationsEnabled="0" isPassiveChecksEnabled="0" isProcessPerformanceData="0" />
	my $monitorserver=shift;
	my $host=shift;
	my $ipaddress=shift;
	my $currenttime=time_text(time);
	my $xml_message = "<HOST_STATUS ".
						"MonitorServerName=\"$monitorserver\" ".
						"Host=\"$host\" ".
						"Device=\"$ipaddress\" ".
						"CheckTypeID=\"0\" ".
						"CurrentNotificationNumber=\"0\" ".
						"LastCheckTime=\"$currenttime\" ".
						"LastNotificationTime=\"0\" ".
						"LastPluginOutput=\"\" ".
						"LastStateChange=\"$currenttime\" ".
						"MonitorStatus=\"PENDING\" ".
						"PercentStateChange=\"0.00\" ".
						"ScheduledDowntimeDepth=\"0\" ".
						"TimeDown=\"0\" ".
						"TimeUnreachable=\"0\" ".
						"TimeUp=\"0\" ".
						"isAcknowledged=\"0\" ".
						"isChecksEnabled=\"0\" ".
						"isEventHandlersEnabled=\"0\" ".
						"isFailurePredictionEnabled=\"0\" ".
						"isFlapDetectionEnabled=\"0\" ".
						"isHostIsFlapping=\"0\" ".
						"isNotificationsEnabled=\"0\" ".
						"isPassiveChecksEnabled=\"0\" ".
						"isProcessPerformanceData=\"0\" ".
						"/>";	# Start message tag
    return $xml_message;
}
sub formatxml_host_delete {
#<ADMIN SessionID="12345" Action="remove" Type="Host" HostName="localhost" />
	my $host=shift;
	my $xml_message = "<ADMIN SessionID=\"12345\" Action=\"remove\" Type=\"Host\" ";	# Start message tag
	$xml_message .= "Name=\"$host\" ";
	$xml_message .= "/>";
    return $xml_message;
}
sub formatxml_device_delete {
#<ADMIN SessionID="12345" Action="remove" Type="Device" Device="test-device-snmp" />
	my $device=shift;
	my $xml_message = "<ADMIN SessionID=\"12345\" Action=\"remove\" Type=\"Device\" ";	# Start message tag
	$xml_message .= "Device=\"$device\" ";
	$xml_message .= "/>";
    return $xml_message;
}

sub formatxml_service_delete {
#<ADMIN SessionID="12345" Action="remove" Type="ServiceStatus" Host="localhost" ServiceDescription="PING" />
	my $host=shift;
	my $service=shift;
	my $xml_message = "<ADMIN SessionID=\"12345\" Action=\"remove\" Type=\"ServiceStatus\" ";	# Start message tag
	$xml_message .= "Host=\"$host\" ";
	$xml_message .= "ServiceDescription=\"$service\" ";
	$xml_message .= "/>";
    return $xml_message;
}




sub time_text {
		my $timestamp = shift;
		if ($timestamp <= 0) {
			return "0";
		} else {
			my ($seconds, $minutes, $hours, $day_of_month, $month, $year,$wday, $yday, $isdst) = localtime($timestamp);
			return sprintf "%04d-%02d-%02d %02d:%02d:%02d",$year+1900,$month+1,$day_of_month,$hours,$minutes,$seconds;
		}
}


sub CommandClose {
	my $socket = shift;
# Create XML stream - Format:
#	<SERVICE-MAINTENANCE     command="close" />
	my $xml_message = "<SERVICE-MAINTENANCE command=\"close\" />";
	print LOG $xml_message."\n\n" if $debug;
	print $socket $xml_message;
	return;
}


sub read_hostgroup_monarch {
	my $hostgroup_ref = undef;
	my $host_ref = undef;
	my $hg;
	my @members = ();
	my ($dbname,$dbhost,$dbuser,$dbpass,$dbtype) = CollageQuery::readGroundworkDBConfig('monarch');
	my $dsn = '';
	if ( defined($dbtype) && $dbtype eq 'postgresql' ) {
	    $dsn = "DBI:Pg:dbname=$dbname;host=$dbhost";
	}
	else {
	    $dsn = "DBI:mysql:database=$dbname;host=$dbhost";
	}
	my $dbh = DBI->connect($dsn, $dbuser, $dbpass, { 'AutoCommit' => 1 })
		or die "Can't connect to database $dbname. Error: ".$DBI::errstr;
	my $query = "select h.name as hostname, h.address as address, sn.name as servicename from hosts as h, services as s, service_names as sn where h.host_id=s.host_id and s.servicename_id=sn.servicename_id; ";
	my $sth = $dbh->prepare($query);
	$sth->execute() or die  $@;
	my @serviceprofile_ids = ();
	while (my $row=$sth->fetchrow_hashref()) {
		$host_ref->{$$row{hostname}}->{"SERVICES"}->{$$row{servicename}}->{"EXISTS"}=1;
		$host_ref->{$$row{hostname}}->{"ADDRESS"} = $$row{address};
	}
	$sth->finish();
	my $query = "select hg.name as hostgroupname, h.name as hostname from hostgroups as hg, hosts as h, hostgroup_host as hgh where hg.hostgroup_id=hgh.hostgroup_id and h.host_id=hgh.host_id; ";
	my $sth = $dbh->prepare($query);
	$sth->execute() or die  $@;
	my @serviceprofile_ids = ();
	while (my $row=$sth->fetchrow_hashref()) {
		$host_ref->{$$row{hostname}}->{"HOST GROUPS"}->{$$row{hostgroupname}}=1;
		$hostgroup_ref->{$$row{hostgroupname}}->{"HOSTS"}->{$$row{hostname}}=1;
	}
	$sth->finish();
	$dbh->disconnect();
	return ($hostgroup_ref,$host_ref);
}

sub read_hostgroup_nagios {
	my $nagiosconfigfile = shift;
	my $hostgroup_ref = undef;
	my $host_ref = undef;
	my $configfile = undef;
	my $hg;
	my @members = ();
	open(NAGIOSCFG, $nagiosconfigfile) or die "Can't open file $configfile: $!";
	my %configfiles=();
	while (my $line=<NAGIOSCFG>) {
		chomp $line;
		if ($line =~ /cfg_file=(\S+)/) {
			$configfiles{$1}=$1;
		}
	}
	close NAGIOSCFG;
	foreach my $file (keys %configfiles) {
		# do something with "$dirname/$file"
		if (!open(CONFIG,"$file") ) {
				print "Can't open host group configuration file $file\n";
				print LOG "Can't open host group configuration file $file\n";
				next;
		}
		print LOG "Opening file $file\n";
		my $define_hg_open = 0;
		my $define_host_open = 0;
		my $host = undef;
		my $address = undef;
		while (my $line = <CONFIG>) {
			#print LOG "Processing line: $line";
			chomp $line;
			# Process Hostgroup statements
			if (($line =~ /define\s+hostgroup\s*\{/i) and ($define_hg_open == 0)) {
					$define_hg_open = 1;
			} elsif (($line =~ /define\s+hostgroup\s*\{/i) and ($define_hg_open == 1)) {
					print "Invalid host group configuration file $file\n";
					return "ERROR";
			}
			if (($line =~ /hostgroup_name\s+(.*\S)\s*$/i) and ($define_hg_open == 1)) {
				$hg = $1;
			}
			if (($line =~ /members\s+(.*)/i) and ($define_hg_open == 1)) {
	#			@members = split /[\s\,]+/,$1;
				@members = splitmembers($1);
			}
			if (($line =~ /\}/) and ($define_hg_open == 1)) {
				$define_hg_open = 0;
				foreach my $host (@members) {
					$hostgroup_ref->{$hg}->{"HOSTS"}->{$host}=1;
					$host_ref->{$host}->{"HOST GROUPS"}->{$hg}=1;
				}
			}
			# process host statements
			if (($line =~ /define\s+host\s*\{/i) and ($define_host_open == 0)) {
					$define_host_open = 1;
					$host = undef;
					$address = undef;
			} elsif (($line =~ /define\s+host\s*\{/i) and ($define_host_open == 1)) {
					print LOG "Invalid host configuration file $file\n";
					#return "ERROR";
			}
			if (($line =~ /host_name\s+(.*\S)\s*$/i) and ($define_host_open == 1)) {
				$host = $1;
				$host_ref->{$host}->{"HOST"}=1;
				if ($address) {
					$host_ref->{$host}->{"ADDRESS"}=$address;
				}
			}
			if (($line =~ /address\s+(.*\S)\s*/i) and ($define_host_open == 1)) {
				$address = $1;
				if ($host) {
					$host_ref->{$host}->{"ADDRESS"}=$address;
				}
			}
			if (($line =~ /\}/) and ($define_host_open == 1)) {
				$define_host_open = 0;
			}
		}
		close CONFIG;
	}

	return ($hostgroup_ref,$host_ref);
}
sub splitmembers {
	my @members = ();
	my $tmpstring = shift;
	while ($tmpstring =~ /\s*(\S+?)[\s,]+(.*)?/) {
		push @members, $1;
		print LOG "Adding $1 to members\n";
		$tmpstring = $2;
	}
	if ($tmpstring =~ /(\S+)\s*?/ ) {
		push @members, $1;
		print LOG "Adding $1 to members\n";
	}
	return @members;
}

__END__

# Service Parameters
#	Service parameter: PERCENT_KNOWN_TIME_CRITICAL=0.000%
#	Service parameter: PERCENT_KNOWN_TIME_CRITICAL_SCHEDULED=0.000%
#	Service parameter: PERCENT_KNOWN_TIME_CRITICAL_UNSCHEDULED=0.000%
#	Service parameter: PERCENT_KNOWN_TIME_OK=100.000%
#	Service parameter: PERCENT_KNOWN_TIME_OK_SCHEDULED=0.000%
#	Service parameter: PERCENT_KNOWN_TIME_OK_UNSCHEDULED=100.000%
#	Service parameter: PERCENT_KNOWN_TIME_UNKNOWN=0.000%
#	Service parameter: PERCENT_KNOWN_TIME_UNKNOWN_SCHEDULED=0.000%
#	Service parameter: PERCENT_KNOWN_TIME_UNKNOWN_UNSCHEDULED=0.000%
#	Service parameter: PERCENT_KNOWN_TIME_WARNING=0.000%
#	Service parameter: PERCENT_KNOWN_TIME_WARNING_SCHEDULED=0.000%
#	Service parameter: PERCENT_KNOWN_TIME_WARNING_UNSCHEDULED=0.000%
#	Service parameter: PERCENT_TIME_CRITICAL_SCHEDULED=0.000%
#	Service parameter: PERCENT_TIME_CRITICAL_UNSCHEDULED=0.000%
#	Service parameter: PERCENT_TIME_OK_SCHEDULED=0.000%
#	Service parameter: PERCENT_TIME_OK_UNSCHEDULED=48.922%
#	Service parameter: PERCENT_TIME_UNDETERMINED_NOT_RUNNING=51.078%
#	Service parameter: PERCENT_TIME_UNDETERMINED_NO_DATA=0.000%
#	Service parameter: PERCENT_TIME_UNKNOWN_SCHEDULED=0.000%
#	Service parameter: PERCENT_TIME_UNKNOWN_UNSCHEDULED=0.000%
#	Service parameter: PERCENT_TIME_WARNING_SCHEDULED=0.000%
#	Service parameter: PERCENT_TIME_WARNING_UNSCHEDULED=0.000%
#	Service parameter: PERCENT_TOTAL_TIME_CRITICAL=0.000%
#	Service parameter: PERCENT_TOTAL_TIME_OK=48.922%
#	Service parameter: PERCENT_TOTAL_TIME_UNDETERMINED=51.078%
#	Service parameter: PERCENT_TOTAL_TIME_UNKNOWN=0.000%
#	Service parameter: PERCENT_TOTAL_TIME_WARNING=0.000%
#	Service parameter: TIME_CRITICAL_SCHEDULED=0
#	Service parameter: TIME_CRITICAL_UNSCHEDULED=0
#	Service parameter: TIME_OK_SCHEDULED=0
#	Service parameter: TIME_OK_UNSCHEDULED=42269
#	Service parameter: TIME_UNDETERMINED_NOT_RUNNING=44131
#	Service parameter: TIME_UNDETERMINED_NO_DATA=0
#	Service parameter: TIME_UNKNOWN_SCHEDULED=0
#	Service parameter: TIME_UNKNOWN_UNSCHEDULED=0
#	Service parameter: TIME_WARNING_SCHEDULED=0
#	Service parameter: TIME_WARNING_UNSCHEDULED=0
#	Service parameter: TOTAL_TIME_CRITICAL=0
#	Service parameter: TOTAL_TIME_OK=42269
#	Service parameter: TOTAL_TIME_UNDETERMINED=44131
#	Service parameter: TOTAL_TIME_UNKNOWN=0
#	Service parameter: TOTAL_TIME_WARNING=0

# Host Parameters
#	Host parameter: PERCENT_KNOWN_TIME_DOWN=0.000%
#	Host parameter: PERCENT_KNOWN_TIME_DOWN_SCHEDULED=0.000%
#	Host parameter: PERCENT_KNOWN_TIME_DOWN_UNSCHEDULED=0.000%
#	Host parameter: PERCENT_KNOWN_TIME_UNREACHABLE=0.000%
#	Host parameter: PERCENT_KNOWN_TIME_UNREACHABLE_SCHEDULED=0.000%
#	Host parameter: PERCENT_KNOWN_TIME_UNREACHABLE_UNSCHEDULED=0.000%
#	Host parameter: PERCENT_KNOWN_TIME_UP=100.000%
#	Host parameter: PERCENT_KNOWN_TIME_UP_SCHEDULED=0.000%
#	Host parameter: PERCENT_KNOWN_TIME_UP_UNSCHEDULED=100.000%
#	Host parameter: PERCENT_TIME_DOWN_SCHEDULED=0.000%
#	Host parameter: PERCENT_TIME_DOWN_UNSCHEDULED=0.000%
#	Host parameter: PERCENT_TIME_UNDETERMINED_NOT_RUNNING=51.078%
#	Host parameter: PERCENT_TIME_UNDETERMINED_NO_DATA=0.000%
#	Host parameter: PERCENT_TIME_UNREACHABLE_SCHEDULED=0.000%
#	Host parameter: PERCENT_TIME_UNREACHABLE_UNSCHEDULED=0.000%
#	Host parameter: PERCENT_TIME_UP_SCHEDULED=0.000%
#	Host parameter: PERCENT_TIME_UP_UNSCHEDULED=48.922%
#	Host parameter: PERCENT_TOTAL_TIME_DOWN=0.000%
#	Host parameter: PERCENT_TOTAL_TIME_UNDETERMINED=51.078%
#	Host parameter: PERCENT_TOTAL_TIME_UNREACHABLE=0.000%
#	Host parameter: PERCENT_TOTAL_TIME_UP=48.922%
#	Host parameter: TIME_DOWN_SCHEDULED=0
#	Host parameter: TIME_DOWN_UNSCHEDULED=0
#	Host parameter: TIME_UNDETERMINED_NOT_RUNNING=44131
#	Host parameter: TIME_UNDETERMINED_NO_DATA=0
#	Host parameter: TIME_UNREACHABLE_SCHEDULED=0
#	Host parameter: TIME_UNREACHABLE_UNSCHEDULED=0
#	Host parameter: TIME_UP_SCHEDULED=0
#	Host parameter: TIME_UP_UNSCHEDULED=42269
#	Host parameter: TOTAL_TIME_DOWN=0
#	Host parameter: TOTAL_TIME_UNDETERMINED=44131
#	Host parameter: TOTAL_TIME_UNREACHABLE=0
#	Host parameter: TOTAL_TIME_UP=42269

