#!/usr/local/groundwork/bin/perl --
#
#	nagios2master.pl	Version 0.1
#	
#	Copyright 2007 GroundWork Open Source Solutions, Inc. (GroundWork)  
#	All rights reserved. Use is subject to GroundWork commercial license terms.
#
#
use strict;
use Time::Local;
use Time::HiRes;
use IO::Socket;
use CollageQuery;
use SOAP::Lite;
use XML::LibXML;

#
#	Program parameters
#
my $socket;
my $debug = 2 ;		# 1 = summary output; 2= output XML message; 3 = print input lines
my $testmode = 0;	# If testmode set to 1, XML messages not sent to master
my $use_hostgroup_prefix = 1;	# Use host group naming conventions to synchronize master-child host groups and hosts
my $hostgroup_prefix = "cm-tor-";	# if $use_hostgroup_prefix is 1, use this naming convention. Only synch with hostgroups using this prefix and their hosts
my $thisnagios = "brooklyn";	# Query to get the hostname of this host and set to this variable
my $master_host = "172.28.113.226";
#my $master_host = "172.28.112.144";


my $eventfile = '/usr/local/groundwork/nagios/var/nagios.log';
my $statusfile = '/usr/local/groundwork/nagios/var/status.log';
my $seekfile = '/usr/local/groundwork/nagios/var/nagios2master_seek.tmp';
my $logfile = '/usr/local/groundwork/foundation/container/logs/nagios2master_eventlog.log';
#my $logfile = '/usr/local/groundwork/nagios/var/nagios2master_eventlog.log';
my $remote_host = $master_host;
my $remote_port = 4913;
my $maxhostsendcount  = 250;
my $max_bulk_host_add = 50;

#
#	Program variables
#
my %hostipaddress = ();
my $socket;
my $master_hostgroups_ref = undef;
my $master_hosts_ref = undef;
my $nagios_hosts_ref = undef;
my $nagios_hostgroups_ref = undef;

$SIG {"PIPE"} = \&sig_pipe_handler;		# Try to handle broken pipe error
chomp $thisnagios;

#
#	Open Log File
#
if (!open(LOG,">$logfile")) {
		print "Can't open logfile $logfile.\n";
	};
LOG->autoflush(1);
print LOG "nagios2master.pl program started at ".time_text(time)."\n";

#
#	Test socket connection to Master server
#
if ( $socket = IO::Socket::INET->new(PeerAddr => $remote_host,
							PeerPort => $remote_port,
							Proto    => "tcp",
						   Type     => SOCK_STREAM)
) {
#	Send System message to console	
	$socket->autoflush();
	print $socket "<GENERICLOG consolidation='SYSTEM' ApplicationType='SYSTEM' MonitorServerName='localhost' Device='$remote_host' Severity='OK' MonitorStatus='OK' TextMessage='Master Foundation communication process started.' />";
} else {
	print LOG "Listener services not available. Retrying in 10 seconds.\n";
	sleep 10;
	exit(0);
}

#############################################
#
#	Synchronize Configuration
#
#############################################
my $startTime = Time::HiRes::time();
#
#	Get Master Server Hostgroups, Hosts and Services
#
print LOG "Getting master hostgroups\n";
$master_hostgroups_ref = getMasterHostGroups();
#print LOG "Getting master hosts and services\n";
#$master_hosts_ref = getMasterHosts();

print LOG "Getting master services\n";
$master_hosts_ref = getMasterServicesForHostAll();

# If set, determine whether to delete hg and hosts 
if ($use_hostgroup_prefix) {
	# Mark those hostgroups and hosts not belonging to this hostgroup prefix
	foreach my $hostgroup (sort keys %{$master_hostgroups_ref}) {
		if ($hostgroup !~ /^$hostgroup_prefix/) {
			$master_hostgroups_ref->{$hostgroup}->{DELETE} = 1;
			foreach my $host (sort keys %{$master_hostgroups_ref->{$hostgroup}->{HOSTS}}) {
				$master_hosts_ref->{$host}->{DELETE} = 1;
			}
		}
	}
	# Make sure that a host marked for deletion isn't also in a hostgroup we are going to keep
	foreach my $hostgroup (sort keys %{$master_hostgroups_ref}) {
		if ($hostgroup =~ /^$hostgroup_prefix/) {
			foreach my $host (sort keys %{$master_hostgroups_ref->{$hostgroup}->{HOSTS}}) {
				$master_hosts_ref->{$host}->{DELETE} = 0;
			}
		}
	}
	# Delete marked hostgroups and hosts 
	foreach my $hostgroup (sort keys %{$master_hostgroups_ref}) {
		if ($master_hostgroups_ref->{$hostgroup}->{DELETE}) {
			delete($master_hostgroups_ref->{$hostgroup});
		}
	}
	foreach my $host (sort keys %{$master_hosts_ref}) {
		if ($master_hosts_ref->{$host}->{DELETE}) {
			delete($master_hosts_ref->{$host});
		}
	}
}

if ($debug) {
	print LOG "Master Foundation Web Services Response - Host Groups and Hosts:\n";
	foreach my $hostgroup (sort keys %{$master_hostgroups_ref}) {
		print LOG "Master Foundation Host Group $hostgroup\n";
		foreach my $host (sort keys %{$master_hostgroups_ref->{$hostgroup}->{HOSTS}}) {
			print LOG "\tMaster Foundation Host $host\n";
			foreach my $service (sort keys %{$master_hosts_ref->{$host}->{SERVICES}}) {
				print LOG "\t\tMaster Foundation Service $service\n";
			}
		}
	}
}

#
#	Get this Nagios system's Hostgroups, Hosts and Services
#
($nagios_hostgroups_ref,$nagios_hosts_ref) = readChildHostGroupConfig();	# Get child Nagios hostgroup and host references from Monarch
if ($debug) {
	print LOG "Local Host Groups and Hosts:\n";
	foreach my $hostgroup (sort keys %{$nagios_hostgroups_ref}) {
		print LOG "Local Host Group $hostgroup\n";
		foreach my $host (sort keys %{$nagios_hostgroups_ref->{$hostgroup}->{HOSTS}}) {
			print LOG "\tLocal Host $host\n";
			foreach my $service (sort keys %{$nagios_hosts_ref->{$host}->{SERVICES}}) {
				print LOG "\t\tLocal Service $service\n";
			}
		}
	}
}

#
#	Compute Hostgroup and Host changes
#		Find hosts in child that are not in master then add
if ($nagios_hosts_ref) {
	# Delete hosts whose IP address has changed
	if ($master_hosts_ref) {
		foreach my $host (sort keys %{$nagios_hosts_ref}) {
			if ($master_hosts_ref->{$host}->{EXISTS} and 
				($master_hosts_ref->{$host}->{ADDRESS} ne $nagios_hosts_ref->{$host}->{ADDRESS} )
				) {
				print LOG "Deleting Master host $host - changed IP address. OLD:".$master_hosts_ref->{$host}->{ADDRESS}
							."  NEW:".$nagios_hosts_ref->{$host}->{ADDRESS}."\n";
				my $xmlstring = formatxml_host_delete($host);
				print $socket $xmlstring if (!$testmode) ;
				print LOG $xmlstring."\n\n" if ($debug>1);
				my $xmlstring = formatxml_device_delete($host);
				print $socket $xmlstring if (!$testmode) ;
				print LOG $xmlstring."\n\n" if ($debug>1);
				foreach my $service (sort keys %{$master_hosts_ref->{$host}->{"SERVICES"}}) {
					my $xmlstring = formatxml_service_delete($host,$service);
					print $socket $xmlstring if (!$testmode) ;
					print LOG $xmlstring."\n\n" if ($debug>1);
				}
				delete $master_hosts_ref->{$host};
			}
		}
	}
	my @bulkHostArray = ();
	# Cycle through and put (host,ip) tuples into the 2 dimensional bulkHostsArray.
	foreach my $host (sort keys %{$nagios_hosts_ref}) {
		if ($master_hosts_ref) {
			# Check if the host is in master. If not, then add
			if (!($master_hosts_ref->{$host}->{EXISTS})) {
				$nagios_hosts_ref->{$host}->{IN_MASTER} = 1;
				bulkHostAdd(\@bulkHostArray, $max_bulk_host_add, $host, $nagios_hosts_ref->{$host}->{"ADDRESS"});
				print LOG "Child Host added to insert for Master Collage: $host \n" if $debug;
			}
			else {
				$nagios_hosts_ref->{$host}->{IN_MASTER} = 1;
				print LOG "Child Host not inserted (Host Already In Master Collage): $host \n" if ($debug > 1);
			}
		}
		else {
			$nagios_hosts_ref->{$host}->{IN_MASTER} = 1;
			bulkHostAdd(\@bulkHostArray, $max_bulk_host_add, $host, $nagios_hosts_ref->{$host}->{"ADDRESS"});
			print LOG "Child Host added to insert for Master Collage: $host \n" if $debug;
		}
	}
	# Cycle through the 2-d bulk host containers and print out the xml
	foreach (@bulkHostArray) {
		my $xmlstring = formatxml_bulk_host_add(\@{$_});
		print $socket $xmlstring if (!$testmode) ;
		print LOG $xmlstring."\n\n" if ($debug>1);
	}
}
#	Find hostgroups in child that are not in master then add
if ($nagios_hostgroups_ref) {
	foreach my $hg  (sort keys %$nagios_hostgroups_ref) {
		my $add_hg_to_master = 0;
		my $delete_hg_from_master = 0;
		if ($master_hostgroups_ref) {
			if (!($master_hostgroups_ref->{$hg}->{EXISTS})) {
				$add_hg_to_master = 1;
			} else {		# Compare hosts in the hostgoup. Update if not identical
				my @hosts_differences = ();
				foreach my $host (keys %{$master_hostgroups_ref->{$hg}->{HOSTS}}) {
				    push(@hosts_differences, $host) unless exists $nagios_hostgroups_ref->{$hg}->{HOSTS}->{$host};
				}
				foreach my $host (keys %{$nagios_hostgroups_ref->{$hg}->{HOSTS}}) {
				    push(@hosts_differences, $host) unless exists $master_hostgroups_ref->{$hg}->{HOSTS}->{$host};
				}
				if ($#hosts_differences >= 0) {
					$delete_hg_from_master = 1;
					$add_hg_to_master = 1;
				}
			}
		} else {	# If master has no hostgroups then add
			$add_hg_to_master = 1;
		}		
		if ($delete_hg_from_master) {
		# First delete hostgroup, in case the members changed
			my $xmlstring = formatxml_hostgroup_delete($hg);
			print $socket $xmlstring if (!$testmode) ;
			print LOG $xmlstring."\n\n" if ($debug>1);
		}
		if ($add_hg_to_master) {			
			my @hostsArray = sort keys %{$nagios_hostgroups_ref->{$hg}->{HOSTS}};
			# Walk through the hostsArray in blocks of maxhostsendcount
			for (my $i=0; $i<=$#hostsArray; $i = $i + $maxhostsendcount) {
				my $hostlist = "";
				my $host     = "";
				# Construct list of hosts in this hostgroup in blocks of maxsendcount
				for (my $j=$i; (($j<=$#hostsArray) && ($j < ($i+$maxhostsendcount))); $j++) {
					$host      = $hostsArray[$j];
					$hostlist .= $host.",";
					if ($nagios_hosts_ref->{$host}) {
						$nagios_hosts_ref->{$host}->{"IN_HOSTGROUP"} = 1;
					}
				}
				$hostlist  =~ s/,$//;    # take off last comma
				my $xmlstring = formatxml_hostgroup_add($hg,$hostlist);		# then add hostgroup with current members
				print $socket $xmlstring if (!$testmode) ;
				print LOG $xmlstring."\n\n" if ($debug>1);
			}
		}
	}
}
if ($master_hostgroups_ref) {
# Delete Hostgroups that no are no longer in Nagios
	foreach my $hg  (sort keys %{$master_hostgroups_ref}) {
		if (!$nagios_hostgroups_ref->{$hg}->{EXISTS}) {
			my $xmlstring = formatxml_hostgroup_delete($hg);
			print $socket $xmlstring if (!$testmode) ;
			print LOG $xmlstring."\n\n" if ($debug>1);
			delete $master_hostgroups_ref->{$hg};
		}
	}
}

# Delete Hosts that no are no longer in Nagios
if ($master_hosts_ref) {
	foreach my $host  (sort keys %{$master_hosts_ref}) {
		if ($nagios_hosts_ref->{$host}->{EXISTS}) {
			$master_hosts_ref->{$host}->{"IN_NAGIOS"} = 1;
			# Delete Services that no are no longer in Nagios
			foreach my $service (sort keys %{$master_hosts_ref->{$host}->{"SERVICES"}}) {
				if ($nagios_hosts_ref->{$host}->{"SERVICES"}->{$service}) {
					$master_hosts_ref->{$host}->{"SERVICES"}->{$service}->{"IN_NAGIOS"} = 1;
				}
			}
		}
	}
	foreach my $host  (sort keys %$master_hosts_ref) {
		if (!$master_hosts_ref->{$host}->{"IN_NAGIOS"}) {
			my $xmlstring = formatxml_host_delete($host);
			print $socket $xmlstring if (!$testmode) ;
			print LOG $xmlstring."\n\n" if ($debug>1);
			my $xmlstring = formatxml_device_delete($host);
			print $socket $xmlstring if (!$testmode) ;
			print LOG $xmlstring."\n\n" if ($debug>1);
			delete $master_hosts_ref->{$host};
		}
		foreach my $service (sort keys %{$master_hosts_ref->{$host}->{"SERVICES"}}) {
			if (!$master_hosts_ref->{$host}->{"SERVICES"}->{$service}->{"IN_NAGIOS"}) {
				my $xmlstring = formatxml_service_delete($host,$service);
				print $socket $xmlstring if (!$testmode) ;
				print LOG $xmlstring."\n\n" if ($debug>1);
				delete $master_hosts_ref->{$host}->{"SERVICES"}->{$service};
			}
		}
	}
}

# Add Nagios Hosts that don't belong to any hostgroup
if ($nagios_hosts_ref) {
	my @bulkHostArray = ();
	my @hostsArray = ();

	my $hg = "$thisnagios\_Orphan_Hosts";
	my $xmlstring = formatxml_hostgroup_delete($hg);
	print $socket $xmlstring if (!$testmode) ;
	print LOG $xmlstring."\n\n" if ($debug>1);

	foreach my $host  (sort keys %{$nagios_hosts_ref}) {
		if (!$nagios_hosts_ref->{$host}->{IN_MASTER}) {
			bulkHostAdd(\@bulkHostArray, $max_bulk_host_add, $host, $nagios_hosts_ref->{$host}->{"ADDRESS"});
			print LOG "Child Host not in Host Group added to insert for Master Collage: $host \n" if $debug;
			push @hostsArray,$host;
		}
	}
	foreach (@bulkHostArray) {
		my $xmlstring = formatxml_bulk_host_add(\@{$_});
		print $socket $xmlstring if (!$testmode) ;
		print LOG $xmlstring."\n\n" if ($debug>1);
	}
	# Send to orphans hostgroup
	for (my $i=0; $i<=$#hostsArray; $i = $i + $maxhostsendcount) {
		my $hostlist = "";
		my $host     = "";
		# Construct list of hosts in this hostgroup in blocks of maxsendcount
		for (my $j=$i; (($j<=$#hostsArray) && ($j < ($i+$maxhostsendcount))); $j++) {
			$host      = $hostsArray[$j];
			$hostlist .= $host.",";
			if ($nagios_hosts_ref->{$host}) {
				$nagios_hosts_ref->{$host}->{"IN_HOSTGROUP"} = 1;
			}
		}
		$hostlist  =~ s/,$//;    # take off last comma
		my $xmlstring = formatxml_hostgroup_add($hg,$hostlist);		# then add hostgroup with current members
		print $socket $xmlstring if (!$testmode) ;
		print LOG $xmlstring."\n\n" if ($debug>1);
	}
}

# Log Config Synch Time 
my $SynchConfigTime   =  Time::HiRes::time() - $startTime;
print LOG sprintf("Synchronize Configuration Time: %0.2F seconds\n",$SynchConfigTime);

#exit;


#############################################
#
#	Synchronize Status
#
#############################################
$startTime = Time::HiRes::time();

# Get hosts->IPaddress from Monarch 
my ($Database_Name,$Database_Host,$Database_User,$Database_Password) = CollageQuery::readGroundworkDBConfig("monarch");
if (my $dbh = DBI->connect("DBI:mysql:$Database_Name:$Database_Host", $Database_User, $Database_Password)) {
	my $query = "select name,address from hosts; ";
	my $sth = $dbh->prepare($query);
	$sth->execute() or die  $@;
	my @serviceprofile_ids = ();
	while (my $row=$sth->fetchrow_hashref()) {
		$hostipaddress{$$row{name}} = $$row{address};
	} 
	$sth->finish();
	$dbh->disconnect();
} else { 
	print LOG "Can't connect to database $Database_Name. Error:".$DBI::errstr;
}

my $element_ref = get_status_v2($statusfile) ; 
if ($element_ref) {
	UpdateHosts($socket,$thisnagios,\%{$element_ref});
	UpdateServices_bulk($socket,$thisnagios,\%{$element_ref});
	my $SynchStatusTime=sprintf "%0.4F",Time::HiRes::time() - $startTime;
	print LOG sprintf("Synchronize Status Time: %0.2F seconds\n",$SynchStatusTime);
}

CommandClose($socket);
close($socket);

#exit;

#############################################
#
#	Start processing Nagios Events 
#
#############################################

while (1) {					# Do forever 
	my $LoopCount = 0;
	my $SkipCount = 0;
	
	if ( !open LOG_FILE, $eventfile ) {
	print LOG  "Unable to open log file $eventfile: $!";
	exit;
	}
	
	# Try to open log seek file.  If open fails, we seek from beginning of
	# file by default.
	if (open(SEEK_FILE, $seekfile)) {
		chomp(my @seek_pos = <SEEK_FILE>);
		close(SEEK_FILE);
		#  If file is empty, no need to seek...
		if ($seek_pos[0] != 0) {
			# Compare seek position to actual file size.  If file size is smaller
			# then we just start from beginning i.e. file was rotated, etc.
			my ($dev,$ino,$mode,$nlink,$uid,$gid,$rdev,$size,$atime,$mtime,$ctime,$blksize,$blocks) = stat(LOG_FILE);
			if ($seek_pos[0] <= $size) {
				seek(LOG_FILE, $seek_pos[0], 0);
			}
		}
	}
	if (!($socket = IO::Socket::INET->new(PeerAddr => $remote_host,
                                PeerPort => $remote_port,
                                Proto    => "tcp",
                               Type     => SOCK_STREAM))) {
		print LOG "Couldn't connect to $remote_host:$remote_port : $@\n";
		exit
	}
	$socket->autoflush();
	print LOG "Connected to $remote_host:$remote_port : $@\n" if $debug;
	while (my $line=<LOG_FILE>) {
		my ($timestamp,$msgtype,$host) = ();
		chomp $line;
#  Sample Events below
		if ($line =~ /^\s*\#]/) { next; }			
		my @field = split /;/,$line;
		if ($field[0] =~ /\[(\d+)\]\s([\w\s]+):\s(.*)/) {
			$timestamp = $1;
			$msgtype = $2;
			$host = $3;
		} else {		# Parse other formats here if necessary
			$SkipCount++;
			next; 
		}
		my $xml_message = "<TURBO_NOC_EVENT consolidation='NAGIOSEVENT' ";	# Start message tag
		$xml_message .= "MonitorServerName=\"$thisnagios\" ";			# Default Identification - should set to IP address if known
		if ($msgtype =~ /HOST ALERT/) {
#		if (($msgtype =~ /HOST ALERT/) and ($field[2] eq "HARD")) {
#		[1110304792] HOST ALERT: peter;UP;HARD;1;PING OK - Packet loss = 0%, RTA = 0.88 ms
			$xml_message .= "Host=\"$host\" ";					# Default Identification - should set to IP address if known
			if ($hostipaddress{$host}) {
				$xml_message .= "Device=\"".$hostipaddress{$host}."\" ";			# No IP address, then set to host name
			} else {
				$xml_message .= "Device=\"$host\" ";			# No IP address, then set to host name
			}
			if ($field[1] eq "DOWN" )
			{
				$xml_message .= "Severity=\"CRITICAL\" ";	
			} elsif ($field[1] eq "UP" ) {
				$xml_message .= "Severity=\"OK\" ";	
			} else {
				$xml_message .= "Severity=\"$field[1]\" ";	
			}
			$xml_message .= "MonitorStatus=\"$field[1]\" ";	
			my $tmp = $field[4];
			$tmp =~ s/\n/ /g;
            $tmp =~ s/<br>/ /ig;
            $tmp =~ s/["']/&quot;/g;
            $tmp =~ s/</&lt;/g;
            $tmp =~ s/>/&gt;/g;
			$xml_message .= "TextMessage=\"$tmp\" ";	
			$tmp = time_text(time);
			$xml_message .= "ReportDate=\"$tmp\" ";	
			$tmp = time_text($timestamp);
			$xml_message .= "LastInsertDate=\"$tmp\" ";	
			$xml_message .= "SubComponent=\"$host\" ";
			$xml_message .= "ErrorType=\"HOST ALERT\" ";
#		} elsif (($msgtype =~ /SERVICE ALERT/) and ($field[3] eq "HARD")) {
		} elsif ($msgtype =~ /SERVICE ALERT/) {
#		[1110304792] SERVICE ALERT: peter;icmp_ping;OK;HARD;1;PING OK - Packet loss = 0%, RTA = 1.05 ms
			$xml_message .= "Host=\"$host\" ";					# Default Identification - should set to IP address if known
			if ($hostipaddress{$host}) {
				$xml_message .= "Device=\"".$hostipaddress{$host}."\" ";			# No IP address, then set to host name
			} else {
				$xml_message .= "Device=\"$host\" ";			# No IP address, then set to host name
			}
			$xml_message .= "ServiceDescription=\"$field[1]\" ";			# Invalid field??
			$xml_message .= "Severity=\"$field[2]\" ";	
			$xml_message .= "MonitorStatus=\"$field[2]\" ";	
			my $tmp = $field[5];
			$tmp =~ s/\n/ /g;
            $tmp =~ s/<br>/ /ig;
            $tmp =~ s/["']/&quot;/g;
            $tmp =~ s/</&lt;/g;
            $tmp =~ s/>/&gt;/g;
			$xml_message .= "TextMessage=\"$tmp\" ";	
			$tmp = time_text(time);
			$xml_message .= "ReportDate=\"$tmp\" ";	
			$tmp = time_text($timestamp);
			$xml_message .= "LastInsertDate=\"$tmp\" ";	
			$xml_message .= "SubComponent=\"$host:$field[1]\" ";
			$xml_message .= "ErrorType=\"SERVICE ALERT\" ";
		} elsif (($msgtype =~ /EXTERNAL COMMAND/) and ($host =~ /ACKNOWLEDGE_HOST_PROBLEM/)) {		# Host field not host for this msg type
#		[1153242440] EXTERNAL COMMAND: ACKNOWLEDGE_HOST_PROBLEM;localhost;1;1;1;joe;This is a test
#		<NAGIOS_LOG ApplicationType="NAGIOS" Host="MyHost" ServiceDescription="CPU_CHECK" AcknowledgedBy="you_or_me" AcknowledgeComment="Here we go why" TypeRule"ACKNOWLEDGE" />
#		ServiceDescription,  AcknowledgeComment are optional
#		If TypeRule is UNACKNOWLEDGE AcknowledgedBy and AcknowledgeComment will be cleared.
			$msgtype = "ACKNOWLEDGE_HOST_PROBLEM";
			$xml_message = "<NAGIOS_LOG ApplicationType='NAGIOS' ";	# Start message tag
			$xml_message .= "Host=\"$field[1]\" ";	
			$xml_message .= "AcknowledgedBy=\"$field[5]\" ";	
			my $tmp = $field[6];
			$tmp =~ s/\n/ /g;
            $tmp =~ s/<br>/ /ig;
            $tmp =~ s/["']/&quot;/g;
            $tmp =~ s/</&lt;/g;
            $tmp =~ s/>/&gt;/g;
			$xml_message .= "AcknowledgeComment=\"$tmp\" ";	
			$xml_message .= " TypeRule=\"ACKNOWLEDGE\" ";	
		} elsif (($msgtype =~ /EXTERNAL COMMAND/) and ($host =~ /ACKNOWLEDGE_SVC_PROBLEM/)) {		# Host field not host for this msg type
#		[1153242140] EXTERNAL COMMAND: ACKNOWLEDGE_SVC_PROBLEM;localhost;Current Users;1;1;1;joe;This is a test acknowledge.
#		<NAGIOS_LOG ApplicationType="NAGIOS" Host="MyHost" ServiceDescription="CPU_CHECK" AcknowledgedBy="you_or_me" AcknowledgeComment="Here we go why" TypeRule"ACKNOWLEDGE" />
#		ServiceDescription,  AcknowledgeComment are optional
#		If TypeRule is UNACKNOWLEDGE AcknowledgedBy and AcknowledgeComment will be cleared.
			$msgtype = "ACKNOWLEDGE_SVC_PROBLEM";
			$xml_message = "<NAGIOS_LOG ApplicationType='NAGIOS' ";	# Start message tag
			$xml_message .= "Host=\"$field[1]\" ";	
			$xml_message .= "ServiceDescription=\"$field[2]\" ";	
			$xml_message .= "AcknowledgedBy=\"$field[6]\" ";	
			my $tmp = $field[7];
			$tmp =~ s/\n/ /g;
            $tmp =~ s/<br>/ /ig;
            $tmp =~ s/["']/&quot;/g;
            $tmp =~ s/</&lt;/g;
            $tmp =~ s/>/&gt;/g;
			$xml_message .= "AcknowledgeComment=\"$tmp\" ";	
			$xml_message .= " TypeRule=\"ACKNOWLEDGE\" ";	
		} elsif (($msgtype =~ /EXTERNAL COMMAND/) and ($host =~ /REMOVE_HOST_ACKNOWLEDGEMENT/)) {		# Host field not host for this msg type	
#		[1153257740] EXTERNAL COMMAND: REMOVE_HOST_ACKNOWLEDGEMENT;localhost
#		<NAGIOS_LOG ApplicationType="NAGIOS" Host="MyHost" ServiceDescription="CPU_CHECK" AcknowledgedBy="you_or_me" AcknowledgeComment="Here we go why" TypeRule"ACKNOWLEDGE" />
#		ServiceDescription,  AcknowledgeComment are optional
#		If TypeRule is UNACKNOWLEDGE AcknowledgedBy and AcknowledgeComment will be cleared.
			$msgtype = "REMOVE_HOST_ACKNOWLEDGEMENT";
			$xml_message = "<NAGIOS_LOG ApplicationType='NAGIOS' ";	# Start message tag
			$xml_message .= "Host=\"$field[1]\" ";	
			$xml_message .= " TypeRule=\"UNACKNOWLEDGE\" ";	
		} elsif (($msgtype =~ /EXTERNAL COMMAND/) and ($host =~ /REMOVE_SVC_ACKNOWLEDGEMENT/)) {		# Host field not host for this msg type		
#		[1153258340] EXTERNAL COMMAND: REMOVE_SVC_ACKNOWLEDGEMENT;localhost;Current Load
#		<NAGIOS_LOG ApplicationType="NAGIOS" Host="MyHost" ServiceDescription="CPU_CHECK" AcknowledgedBy="you_or_me" AcknowledgeComment="Here we go why" TypeRule"ACKNOWLEDGE" />
#		ServiceDescription,  AcknowledgeComment are optional
#		If TypeRule is UNACKNOWLEDGE AcknowledgedBy and AcknowledgeComment will be cleared.
			$msgtype = "REMOVE_SVC_ACKNOWLEDGEMENT";
			$xml_message = "<NAGIOS_LOG ApplicationType='NAGIOS' ";	# Start message tag
			$xml_message .= "Host=\"$field[1]\" ";	
			$xml_message .= "ServiceDescription=\"$field[2]\" ";	
			$xml_message .= " TypeRule=\"UNACKNOWLEDGE\" ";	
		} else {
			#print LOG "Skipping line: $line\n" if ($debug>2) ; 
			$SkipCount++;
			next;
		}
		$LoopCount++;
		$xml_message .= "/>";			# End message tag
		#print $xml_message."\n\n" if ($debug>1) ;
		print LOG "Line: $line \n" if ($debug>2) ;
		print LOG $xml_message."\n\n" if ($debug>1) ;
		print $socket $xml_message if (!$testmode);
	}	# All events read
#	CommandClose($socket) if ($LoopCount > 0) ;
	CommandClose($socket) ;
	close($socket);
	# Overwrite log seek file and print the byte position we have seeked to.
    if (!open(SEEK_FILE, "> $seekfile")) {
		print LOG "Unable to open seek count file $seekfile: $!";
		exit;
	} else {
		print LOG "Writing to seek file $seekfile - ".tell(LOG_FILE)."\n" if ($debug>1);
	}
    print SEEK_FILE tell(LOG_FILE);
    # Close seek file.
    close(SEEK_FILE);
	# Close the log file.	
	close(LOG_FILE);
	print LOG "Processed $LoopCount records. Skipped $SkipCount.\n" if ($debug>1);
	sleep 15;
}
exit;

sub time_text {
		my $timestamp = shift;
		if ($timestamp <= 0) {
			return "0001-01-01 00:00:00";
		} else {
			my ($seconds, $minutes, $hours, $day_of_month, $month, $year,$wday, $yday, $isdst) = localtime($timestamp);
			return sprintf "%02d-%02d-%02d %02d:%02d:%02d",$year+1900,$month+1,$day_of_month,$hours,$minutes,$seconds;
		}
}

sub CommandClose {
	my $socket = shift;
# Create XML stream - Format:
#	<SERVICE-MAINTENANCE     command="close" /> 
	my $xml_message = "<SERVICE-MAINTENANCE command=\"close\" />";	
#	print $xml_message."\n\n" if $debug;
	print LOG $xml_message."\n" if ($debug > 1);
	print $socket $xml_message if (!$testmode);
	return;
}

sub FormatTime {
	my $intimestring=shift;
	my $outtimestring;
	if ($intimestring =~ /(\d{2})\/(\d{2})\/(\d{4}) (\d{2}:\d{2}:\d{2})/) {
		$outtimestring = "$3-$1-$2 $4";
	}
	return $outtimestring;
}

sub sig_pipe_handler  {
		sleep 2;
}


sub getMasterHostGroups {
	my $master_hostgroups_ref = undef;
	my $soap = SOAP::Lite->service("http://$master_host:8080/foundation-webapp/services/wshostgroup?wsdl");
#	my $soap2 = SOAP::Lite->service("http://$master_host:8080/foundation-webapp/services/wsservice?wsdl");
	$soap->outputxml(1);
#	$soap2->outputxml(1);
	my $result;
	$result = $soap->getHostGroupsByString('ALL', '', '', "true", 0, 0, '', '');
	print LOG "getHostGroupsByString RESULT: \n$result\n\n" if ($debug > 1);
	my $parser = XML::LibXML->new();
	my $doc = $parser->parse_string($result);
	my $root = $doc->getDocumentElement;
	my @nodes = $root->getElementsByTagName('HostGroup');
	foreach my $node (@nodes) {
#		print "Node $node\n";
		my $master_hostgroupname = undef;
		foreach my $hostgroupname ($node->findvalue("Name")) {
#			print "Host Group Name: $hostgroupname \n";
			$master_hostgroupname = $hostgroupname;
		}
		$master_hostgroups_ref->{$master_hostgroupname}->{EXISTS} = 1;
		foreach my $host ($node->getElementsByTagName('Hosts')) {
			my $master_hostname = undef;
			foreach my $hostname ($host->findvalue("Name")) {
#				print "\tHost Name: $hostname \n";
				$master_hostname = $hostname;
			}
			$master_hostgroups_ref->{$master_hostgroupname}->{HOSTS}->{$master_hostname}->{EXISTS} = 1;
			#my $master_device = undef;
			#foreach my $device ($host->getElementsByTagName('Device')) {
			#	foreach my $identification ($device->findvalue("Identification")) {
			#		print "\t\tIdentification: $identification \n";
			#		$master_device = $identification;
			#	}
			#}
#			getServicesForHost($master_hostname,$soap2);
#			$master_hostgroups_ref->{$master_hostgroupname}->{HOSTS}->{$master_hostname}->{ADDRESS} = $master_device;
#			$master_hosts_ref->{$master_hostname}->{ADDRESS} = $master_device;
		}
	}
	return $master_hostgroups_ref;
}



sub getMasterHosts {
	my $master_hosts_ref = undef;
	my $soap = SOAP::Lite->service('http://172.28.112.144:8080/foundation-webapp/services/wshost?wsdl');
	my $soap2 = SOAP::Lite->service("http://$master_host:8080/foundation-webapp/services/wsservice?wsdl");
	$soap->outputxml(1);
	$soap2->outputxml(1);
	my $parser = XML::LibXML->new();
	my $result = $soap->getHostsByString('ALL', '', '', 0, 0, '', '');
	print LOG $result."\n"if ($debug >1);
	my $doc = $parser->parse_string($result);
	my $root = $doc->getDocumentElement;
	foreach my $host ($root->getElementsByTagName('Host')) {
		my $master_hostname = undef;
		foreach my $hostname ($host->findvalue("Name")) {
			$master_hostname = $hostname;
		}
		my $master_device = undef;
		foreach my $device ($host->getElementsByTagName('Device')) {
			foreach my $identification ($device->findvalue("Identification")) {
				$master_device = $identification;
			}
		}
		$master_hosts_ref->{$master_hostname}->{EXISTS} = 1;
		$master_hosts_ref->{$master_hostname}->{ADDRESS} = $master_device;
		$master_hosts_ref = getServicesForHost($master_hostname,$master_hosts_ref,$soap2);
	}
	return $master_hosts_ref;	
}



sub getServicesForHost {
	my $hostname = shift;
	my $master_hosts_ref = shift;
	my $soap = shift;
	my $parser = XML::LibXML->new();
	my $result = $soap->getServicesByString('HOSTNAME', $hostname, '', 0, 0, '', '');
	print LOG "getServicesForHost $hostname RESULT: \n$result\n\n" if ($debug > 1);
	my $doc = $parser->parse_string($result);
	my $root = $doc->getDocumentElement;
	my @nodes = $root->getElementsByTagName('ServiceStatus');
	foreach my $node (@nodes) {
		foreach my $description ($node->findvalue("Description")) {
			$master_hosts_ref->{$hostname}->{SERVICES}->{$description}->{EXISTS} = 1;
		}
	}
	return $master_hosts_ref;	
}

sub getServicesForHost_SAVE {
	my $soap = SOAP::Lite->service("http://$master_host:8080/foundation-webapp/services/wsservice?wsdl");
	$soap->outputxml(1);
	my $hostname = shift;
	my $parser = XML::LibXML->new();
	my $result = $soap->getServicesByString('HOSTNAME', $hostname, '', 0, 0, '', '');
	print LOG "getServicesForHost $hostname RESULT: \n$result\n\n" if ($debug > 1);
	my $doc = $parser->parse_string($result);
	my $root = $doc->getDocumentElement;
	my @nodes = $root->getElementsByTagName('ServiceStatus');
	foreach my $node (@nodes) {
		foreach my $description ($node->findvalue("Description")) {
			#print "\t\tService Description: $description \n";
			$master_hosts_ref->{$hostname}->{SERVICES}->{$description}->{EXISTS} = 1;
		}
	}
	return;	
}




sub getMasterHostGroups_SAVE {
	my $master_hg_ref = undef;
	my $soap = SOAP::Lite->service("http://$master_host:8080/foundation-webapp/services/wshostgroup?wsdl");
	$soap->outputxml(1);
	my $result;
	$result = $soap->getHostGroupsByString('ALL', '', '', "true", 0, 0, '', '');
	print LOG "getHostGroupsByString RESULT: \n$result\n\n" if ($debug > 1);
	my $parser = XML::LibXML->new();
	my $doc = $parser->parse_string($result);
	my $root = $doc->getDocumentElement;
	my @nodes = $root->getElementsByTagName('HostGroup');
	foreach my $node (@nodes) {
		#print "Node $node\n";
		my $master_hostgroupname = undef;
		foreach my $hostgroupname ($node->findvalue("Name")) {
			#print "Host Group Name: $hostgroupname \n";
			$master_hostgroupname = $hostgroupname;
		}
		$master_hg_ref->{$master_hostgroupname}->{EXISTS} = 1;
		foreach my $host ($node->getElementsByTagName('Hosts')) {
			my $master_hostname = undef;
			foreach my $hostname ($host->findvalue("Name")) {
				#print "\tHost Name: $hostname \n";
				$master_hostname = $hostname;
			}
			my $master_device = undef;
			foreach my $device ($host->getElementsByTagName('Device')) {
				foreach my $identification ($device->findvalue("Identification")) {
					#print "\t\tIdentification: $identification \n";
					$master_device = $identification;
				}
			}
			$master_hg_ref->{$master_hostgroupname}->{HOSTS}->{$master_hostname}->{ADDRESS} = $master_device;
		}
	}
	return $master_hg_ref;
}


sub getMasterServicesForHostAll {
	my $master_hosts_ref = undef;
	my $soap = SOAP::Lite->service("http://$master_host:8080/foundation-webapp/services/wsservice?wsdl");
	$soap->outputxml(1);
	my $parser = XML::LibXML->new();
	my $result = $soap->getServicesByString('ALL', '', '', 0, 0, '', '');
	print LOG "getServicesByString RESULT: \n$result\n\n" if ($debug > 1);
	my $doc = $parser->parse_string($result);
	my $root = $doc->getDocumentElement;
	my @nodes = $root->getElementsByTagName('ServiceStatus');
	foreach my $node (@nodes) {
		#print "Node $node\n";
		my $master_service = undef;
		my $master_hostname = undef;
		my $master_device = undef;
		foreach my $description ($node->findvalue("Description")) {
			#print "Service Description: $description \n";
			$master_service = $description;
		}
		foreach my $host ($node->getElementsByTagName('HOST')) {
			foreach my $hostname ($host->findvalue("Name")) {
				#print "\tHost Name: $hostname \n";
				$master_hostname = $hostname;
			}
			foreach my $device ($host->getElementsByTagName('Device')) {
				foreach my $identification ($device->findvalue("Identification")) {
					#print "\t\tIdentification: $identification \n";
					$master_device = $identification;
				}
			}
		}
		$master_hosts_ref->{$master_hostname}->{EXISTS} = 1;
		$master_hosts_ref->{$master_hostname}->{ADDRESS} = $master_device;
		$master_hosts_ref->{$master_hostname}->{SERVICES}->{$master_service}->{EXISTS} = 1;
	}
	return $master_hosts_ref;	
}



sub readChildHostGroupConfig {
	my $hostgroup_ref = undef;
	my $host_ref = undef;
	my $hg;
	my @members = ();
	my ($Database_Name,$Database_Host,$Database_User,$Database_Password) = CollageQuery::readGroundworkDBConfig("monarch");
	my $dbh = DBI->connect("DBI:mysql:$Database_Name:$Database_Host", $Database_User, $Database_Password) 
		or die "Can't connect to database $Database_Name. Error:".$DBI::errstr;
	my $query = "select h.name as hostname, h.address as address, sn.name as servicename from hosts as h, services as s, service_names as sn where h.host_id=s.host_id and s.servicename_id=sn.servicename_id; ";
	my $sth = $dbh->prepare($query);
	$sth->execute() or die  $@;
	my @serviceprofile_ids = ();
	while (my $row=$sth->fetchrow_hashref()) {
		$host_ref->{$$row{hostname}}->{"EXISTS"}=1;
		$host_ref->{$$row{hostname}}->{"SERVICES"}->{$$row{servicename}}->{"EXISTS"}=1;
		$host_ref->{$$row{hostname}}->{"ADDRESS"} = $$row{address};
	} 
	$sth->finish;
	my $query = "select hg.name as hostgroupname, h.name as hostname from hostgroups as hg, hosts as h, hostgroup_host as hgh where hg.hostgroup_id=hgh.hostgroup_id and h.host_id=hgh.host_id; ";
	my $sth = $dbh->prepare($query);
	$sth->execute() or die  $@;
	my @serviceprofile_ids = ();
	while (my $row=$sth->fetchrow_hashref()) {
		$host_ref->{$$row{hostname}}->{"HOST GROUPS"}->{$$row{hostgroupname}}->{"EXISTS"}=1;
		$hostgroup_ref->{$$row{hostgroupname}}->{"EXISTS"}=1;
		$hostgroup_ref->{$$row{hostgroupname}}->{"HOSTS"}->{$$row{hostname}}->{"EXISTS"}=1;
	}
	$sth->finish;
	$dbh->disconnect();
	return ($hostgroup_ref,$host_ref);
}




sub bulkHostAdd {
	my $bulkHostArray       = shift;
	my $max_bulk_host_count = shift;
	my $host                = shift;
	my $host_ip             = shift;

	# Create an array of arrays.
	# The outer array holds containers which contain arrays.
	# The inner array holds tuples of (host, ip) information with a maximum max_bulk_host_count tuples.

	my @emptyArray = ();

	if ($#{$bulkHostArray} == -1) { push(@{$bulkHostArray}, \@emptyArray); }

	my $bulkHostList = ${$bulkHostArray}[$#{$bulkHostArray}];
	my @hostProps    = ($host, $host_ip);

	if ($#{$bulkHostList} <= $max_bulk_host_count ) { 
		push (@{$bulkHostList},  \@hostProps);  
	} else  { 
		push (@{$bulkHostList},  \@hostProps);
		push (@{$bulkHostArray}, \@emptyArray); 
	}
}

sub formatxml_bulk_host_add {
	#
	# XML Format Example: 
	#
	#<SYSTEM_CONFIG >
	#  <HOST Host='localhost' Description='localhost' Device='127.0.0.1' DeviceDisplay='' />
	#  <HOST Host='Test1'     Description='testBox'   Device='127.0.0.2' DeviceDisplay='' />
	#  <HOST Host='Test2'     Description='Test Box'  Device='127.0.0.3' DeviceDisplay='' />
	#</SYSTEM_CONFIG>
	#
	# Notes:
	#   HostName and Identification are required
	#   Description and DeviceDisplay are optional
	#

	my $hostList = shift;

	my $xml_message = "<SYSTEM_CONFIG >" ."\n";

	foreach (@{$hostList}) {
		my $hostname       = ${$_}[0];
		my $identification = ${$_}[1];

		$xml_message .= "<HOST ";
		$xml_message .= "Host='"       . $hostname       . "' ";
		$xml_message .= "Device='" . $identification . "' ";
		$xml_message .= "/>" .  "\n";
	}

	$xml_message .= "</SYSTEM_CONFIG>";

	return $xml_message;
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

sub formatxml_host_delete {
#<ADMIN SessionID="12345" Action="remove" Type="Host" Host="localhost" />
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





sub get_status_v2 {
	my $statusfile = shift;
	my ($timestamp,$msgtype);
	my @field;
	my $element_ref;
	my %HostStatus = (
					0 => "UP",
					1 => "DOWN",
					2 => "UNREACHABLE"
			);
	my %ServiceStatus = (
					0 => "OK",
					1 => "WARNING",
					2 => "CRITICAL",
					3 => "UNKNOWN"
			);

	my %CheckType = (
					0 => "ACTIVE",
					1 => "PASSIVE"
			);

	my %StateType = (
					0 => "SOFT",
					1 => "HARD"
			);
	if (!open(STATUSFILE,$statusfile)) { 
		print "Error opening file $statusfile\n";
		print LOG "Error opening file $statusfile\n";
		return 0;
	}
	my $state=undef;
	my %attribute=();
	while (my $line=<STATUSFILE>) {
		chomp $line;
		if ($line =~ /^\s*\#]/) { next; }			
		if (!$state and ($line =~ /\s*host \{/)) {
			$state = "Host";
			next;
		} elsif (!$state and ($line =~ /\s*service \{/)) {
			$state = "Service";
			next;
		} elsif (($state eq "Service") and ($line =~ /\s*\}/) and $attribute{host_name} and $attribute{service_description}) {
			# Set element hash
			# Map Nagios V2 status parameters to Nagios V1 definitions in Collage
			$element_ref->{Host}->{$attribute{host_name}}->{Service}->{$attribute{service_description}}->{MonitorStatus}=$ServiceStatus{$attribute{current_state}};
			$element_ref->{Host}->{$attribute{host_name}}->{Service}->{$attribute{service_description}}->{StateType}=$StateType{$attribute{state_type}};	
			$element_ref->{Host}->{$attribute{host_name}}->{Service}->{$attribute{service_description}}->{RetryNumber}=$attribute{current_attempt}; 
			if ($attribute{last_check} == 0) { $attribute{last_check} = time;	}
			$element_ref->{Host}->{$attribute{host_name}}->{Service}->{$attribute{service_description}}->{LastCheckTime}=time_text($attribute{last_check});
			$element_ref->{Host}->{$attribute{host_name}}->{Service}->{$attribute{service_description}}->{NextCheckTime}=time_text($attribute{next_check});
			$element_ref->{Host}->{$attribute{host_name}}->{Service}->{$attribute{service_description}}->{CheckType}=$CheckType{$attribute{check_type}};
			$element_ref->{Host}->{$attribute{host_name}}->{Service}->{$attribute{service_description}}->{isChecksEnabled}=$attribute{active_checks_enabled};
			$element_ref->{Host}->{$attribute{host_name}}->{Service}->{$attribute{service_description}}->{isAcceptPassiveChecks}=$attribute{passive_checks_enabled};
			$element_ref->{Host}->{$attribute{host_name}}->{Service}->{$attribute{service_description}}->{isEventHandlersEnabled}=$attribute{event_handler_enabled};
			if ($attribute{last_state_change} == 0) { $attribute{last_state_change} = time;	}
			$element_ref->{Host}->{$attribute{host_name}}->{Service}->{$attribute{service_description}}->{LastStateChange}=time_text($attribute{last_state_change});  
			$element_ref->{Host}->{$attribute{host_name}}->{Service}->{$attribute{service_description}}->{isProblemAcknowledged}=$attribute{problem_has_been_acknowledged};
			$element_ref->{Host}->{$attribute{host_name}}->{Service}->{$attribute{service_description}}->{LastHardState}=$ServiceStatus{$attribute{last_hard_state}};
			$element_ref->{Host}->{$attribute{host_name}}->{Service}->{$attribute{service_description}}->{TimeOK}=$attribute{last_time_ok};
			$element_ref->{Host}->{$attribute{host_name}}->{Service}->{$attribute{service_description}}->{TimeUnknown}=$attribute{last_time_unknown};
			$element_ref->{Host}->{$attribute{host_name}}->{Service}->{$attribute{service_description}}->{TimeWarning}=$attribute{last_time_warning};
			$element_ref->{Host}->{$attribute{host_name}}->{Service}->{$attribute{service_description}}->{TimeCritical}=$attribute{last_time_critical};
			$element_ref->{Host}->{$attribute{host_name}}->{Service}->{$attribute{service_description}}->{LastNotificationTime}=time_text($attribute{last_notification});
			$element_ref->{Host}->{$attribute{host_name}}->{Service}->{$attribute{service_description}}->{CurrentNotificationNumber}=$attribute{current_notification_number};
			$element_ref->{Host}->{$attribute{host_name}}->{Service}->{$attribute{service_description}}->{isNotificationsEnabled}=$attribute{notifications_enabled};
			$attribute{check_latency} = int(1000*$attribute{check_latency});	# Collage expects latency in integer. Set to ms
			$element_ref->{Host}->{$attribute{host_name}}->{Service}->{$attribute{service_description}}->{Latency}=$attribute{check_latency};
			$attribute{check_execution_time} = int(1000*$attribute{check_execution_time});	# Collage expects execution time in integer. Set to ms
			$element_ref->{Host}->{$attribute{host_name}}->{Service}->{$attribute{service_description}}->{ExecutionTime}=$attribute{check_execution_time};
			$element_ref->{Host}->{$attribute{host_name}}->{Service}->{$attribute{service_description}}->{isFlapDetectionEnabled}=$attribute{flap_detection_enabled};
			$element_ref->{Host}->{$attribute{host_name}}->{Service}->{$attribute{service_description}}->{isServiceFlapping}=$attribute{is_flapping};
			$element_ref->{Host}->{$attribute{host_name}}->{Service}->{$attribute{service_description}}->{PercentStateChange}=$attribute{percent_state_change};
			#$element_ref->{Host}->{$attribute{host_name}}->{Service}->{$attribute{service_description}}->{ScheduledDowntimeDepth}=$attribute{scheduled_downtime_depth};
			$element_ref->{Host}->{$attribute{host_name}}->{Service}->{$attribute{service_description}}->{ScheduledDowntimeDepth}=0;
			$element_ref->{Host}->{$attribute{host_name}}->{Service}->{$attribute{service_description}}->{isFailurePredictionEnabled}=$attribute{failure_prediction_enabled};
			$element_ref->{Host}->{$attribute{host_name}}->{Service}->{$attribute{service_description}}->{isProcessPerformanceData}=$attribute{process_performance_data};
			$element_ref->{Host}->{$attribute{host_name}}->{Service}->{$attribute{service_description}}->{isObsessOverService}=$attribute{obsess_over_service};
			$attribute{plugin_output}=~ s/\n/ /g;
			$attribute{plugin_output}=~ s/<br>/ /ig;
			$attribute{plugin_output}=~ s/["']/&quot;/g;
			$attribute{plugin_output}=~ s/</&lt;/g;
			$attribute{plugin_output}=~ s/>/&gt;/g;
			$element_ref->{Host}->{$attribute{host_name}}->{Service}->{$attribute{service_description}}->{LastPluginOutput}=$attribute{plugin_output};
			# reset variables for next object
			$state = undef;
			%attribute=();
			next;
		} elsif (($state eq "Host") and ($line =~ /\s*\}/) and $attribute{host_name}) {
			$element_ref->{Host}->{$attribute{host_name}}->{MonitorStatus}=$HostStatus{$attribute{current_state}};
			if ($attribute{last_check} == 0) { $attribute{last_check} = time;	}
			$element_ref->{Host}->{$attribute{host_name}}->{LastCheckTime} = time_text($attribute{last_check});
			if ($attribute{last_state_change} == 0) { $attribute{last_state_change} = time;	}
			$element_ref->{Host}->{$attribute{host_name}}->{LastStateChange} = time_text($attribute{last_state_change});
			$element_ref->{Host}->{$attribute{host_name}}->{isAcknowledged} = $attribute{problem_has_been_acknowledged};
			$element_ref->{Host}->{$attribute{host_name}}->{TimeUp} = $attribute{last_time_up};
			$element_ref->{Host}->{$attribute{host_name}}->{TimeDown} = $attribute{last_time_down};
			$element_ref->{Host}->{$attribute{host_name}}->{TimeUnreachable} = $attribute{last_time_unreachable};
			$element_ref->{Host}->{$attribute{host_name}}->{LastNotificationTime} = time_text($attribute{last_notification});
			$element_ref->{Host}->{$attribute{host_name}}->{CurrentNotificationNumber} = $attribute{current_notification_number};
			$element_ref->{Host}->{$attribute{host_name}}->{isNotificationsEnabled} = $attribute{notifications_enabled};
			$element_ref->{Host}->{$attribute{host_name}}->{isEventHandlersEnabled} = $attribute{event_handler_enabled};
			$element_ref->{Host}->{$attribute{host_name}}->{isChecksEnabled} = $attribute{active_checks_enabled};
			$element_ref->{Host}->{$attribute{host_name}}->{isFlapDetectionEnabled} = $attribute{flap_detection_enabled};
			$element_ref->{Host}->{$attribute{host_name}}->{isHostIsFlapping} = $attribute{is_flapping};
			$element_ref->{Host}->{$attribute{host_name}}->{PercentStateChange} = $attribute{percent_state_change};
			#$element_ref->{Host}->{$attribute{host_name}}->{ScheduledDowntimeDepth} = $attribute{scheduled_downtime_depth};
			$element_ref->{Host}->{$attribute{host_name}}->{ScheduledDowntimeDepth} = 0;
			$element_ref->{Host}->{$attribute{host_name}}->{isFailurePredictionEnabled} = $attribute{failure_prediction_enabled};
			$element_ref->{Host}->{$attribute{host_name}}->{isProcessPerformanceData} = $attribute{process_performance_data};
			$element_ref->{Host}->{$attribute{host_name}}->{isPassiveChecksEnabled} = $attribute{passive_checks_enabled};
			$element_ref->{Host}->{$attribute{host_name}}->{CheckTypeID} = $attribute{check_type};
			$attribute{plugin_output}=~ s/\n/ /g;
			$attribute{plugin_output}=~ s/<br>/ /ig;
			$attribute{plugin_output}=~ s/["']/&quot;/g;
			$attribute{plugin_output}=~ s/</&lt;/g;
			$attribute{plugin_output}=~ s/>/&gt;/g;
			$element_ref->{Host}->{$attribute{host_name}}->{LastPluginOutput} = $attribute{plugin_output};
			# reset variables for next object
			$state = undef;
			%attribute=();
			next;
		}
		if ($state and ($line =~ /\s*(\S+?)=(.*)/)) {
			if ($2 ne "") {
				$attribute{$1} = $2;
			}
		} else { next; }
	}
	close STATUSFILE;
	return \%{$element_ref};
}


sub UpdateHosts {
	my $socket = shift;
	my $thisnagios = shift;
	my $element_ref = shift;
	my $insertcount = 0;
	my $skipcount = 0;
	my $nagios_url;	# TMP!!
	my	%HostStatusCodes= ("2"=>"UP","4"=>"DOWN","8"=>"UNREACHABLE");
# Create XML stream - Format:
#		<{SERVICE_STATUS | HOST_STATUS | LOG_MESSAGE} database field=value | database field=value |...} />
#		<HOST_STATUS  database field=value | database field=value |...} />
	foreach my $hostkey (sort keys %{$element_ref->{Host}}) {
		my $xml_message = "<HOST_STATUS ";	# Start message tag
		$xml_message .= "MonitorServerName=\"$thisnagios\" ";	# Default Identification - should set to IP address if known
		$xml_message .= "Host=\"$hostkey\" ";					# Default Identification - should set to IP address if known
		if ($hostipaddress{$hostkey}) {
			$xml_message .= "Device=\"".$hostipaddress{$hostkey}."\" ";			# No IP address, then set to host name
		} else {
			$xml_message .= "Device=\"$hostkey\" ";			# No IP address, then set to host name
		}
		foreach my $field (sort keys %{$element_ref->{Host}->{$hostkey}}) {
			if ($field eq "Service" ) { next }						# Skip the service hash key
			my $tmpinfo = $element_ref->{Host}->{$hostkey}->{$field};
			$tmpinfo =~ s/"/'/g;
			$xml_message .= "$field=\"$tmpinfo\" "; 
		}
		$xml_message .= "/>";			# End message tag
		print LOG $xml_message."\n\n" if ($debug > 1);
		print $socket $xml_message if (!$testmode) ;
		$insertcount++;
	}
	print LOG "Total Hosts Insert Count=$insertcount. \n" if $debug;
	return;
}


sub UpdateServices_bulk {
	my $socket = shift;
	my $thisnagios = shift;
	my $element_ref = shift;
	my $insertcount = 0;
	my $skipcount = 0;
	my $bulkcount = 0;
	my $bulk_send_threshold = 3;			# If bulk_send is set to 1, Minimum number of services for a host before bulk send option is used.

# Create XML stream - Format:
#<SERVICE_STATUS MonitorServerName='ServerName' Host='HostName' Device='DeviceName' >
#   <SERVICE {ListService attributes} />
#   <SERVICE {List Service attributes} />
#</SERVICE_STATUS> 
#
#	Sample
#<SERVICE_STATUS MonitorServerName='localhost' Host='localhost' Device='localhost' >
#  <SERVICE ServiceDescription='Local_Users' CheckType='ACTIVE' CurrentNotificationNumber='0' ExecutionTime='0' LastCheckTime='2005-07-15 22:47:57' LastHardState='OK' LastNotificationTime='0' LastPluginOutput='USERS OK - 1 users currently logged in ' LastStateChange='2005-07-13 12:17:48' Latency='0' MonitorStatus='OK' NextCheckTime='2005-07-15 22:52:57' PercentStateChange='0.00' RetryNumber='1' ScheduledDowntimeDepth='0' StateType='HARD' TimeCritical='0' TimeOK='76402' TimeUnknown='0' TimeWarning='0' isAcceptPassiveChecks='1' isChecksEnabled='1' isEventHandlersEnabled='1' isFailurePredictionEnabled='1' isFlapDetectionEnabled='1' isNotificationsEnabled='1' isObsessOverService='1' isProblemAcknowledged='0' isProcessPerformanceData='1' isServiceFlapping='0' />
#	<SERVICE ServiceDescription='Local_Procs' CheckType='ACTIVE' CurrentNotificationNumber='0' ExecutionTime='0' LastCheckTime='2005-07-15 22:47:28' LastHardState='OK' LastNotificationTime='0' LastPluginOutput='PROCS OK: 88 processes with STATE = RSZDT ' LastStateChange='2005-07-13 12:30:31' Latency='0' MonitorStatus='OK' NextCheckTime='2005-07-15 22:52:28' PercentStateChange='0.00' RetryNumber='1' ScheduledDowntimeDepth='0' StateType='HARD' TimeCritical='0' TimeOK='75674' TimeUnknown='0' TimeWarning='0' isAcceptPassiveChecks='1' isChecksEnabled='1' isEventHandlersEnabled='1' isFailurePredictionEnabled='1' isFlapDetectionEnabled='1' isNotificationsEnabled='1' isObsessOverService='1' isProblemAcknowledged='0' isProcessPerformanceData='1' isServiceFlapping='0'/>
#</SERVICE_STATUS> 
	foreach my $hostkey (sort keys %{$element_ref->{Host}}) {
		my @services_list = sort keys %{$element_ref->{Host}->{$hostkey}->{Service}};
		if ($#services_list < 0) {		# Check if there are any services for this host. 
			next;								# If none then skip
		} elsif ($#services_list >= $bulk_send_threshold) {			# If > threshold, then execute bulk send
			my $xml_message = "<SERVICE_STATUS ";	# Start message tag
			$xml_message .= "MonitorServerName=\"$thisnagios\" ";			# Default Identification - should set to IP address if known
			$xml_message .= "Host=\"$hostkey\" ";			# Default Identification - should set to IP address if known
			if ($hostipaddress{$hostkey}) {
				$xml_message .= "Device=\"".$hostipaddress{$hostkey}."\" ";			# No IP address, then set to host name
			} else {
				$xml_message .= "Device=\"$hostkey\" ";			# No IP address, then set to host name
			}
			$xml_message .= ">";			# End message tag
			print LOG $xml_message."\n\n" if ($debug>1) ;
			$bulkcount++;
			print $socket $xml_message if (!$testmode) ;
			foreach my $servicekey (@services_list) {
				$xml_message = "<SERVICE ";	# Start message tag
				$xml_message .= "ServiceDescription=\"$servicekey\" ";
				foreach my $field (sort keys %{$element_ref->{Host}->{$hostkey}->{Service}->{$servicekey}}) {
					my $tmpinfo = $element_ref->{Host}->{$hostkey}->{Service}->{$servicekey}->{$field};
					$tmpinfo =~ s/"/'/g;
					$xml_message .= "$field=\"$tmpinfo\" "; 
				}
				$xml_message .= "/>";			# End message tag
				print LOG $xml_message."\n\n" if ($debug>1) ;
				print $socket $xml_message if (!$testmode) ;
				$insertcount++;
			}
			my $xml_message = "</SERVICE_STATUS> ";	# End message tag
			print LOG $xml_message."\n\n" if ($debug>1) ;
			print $socket $xml_message if (!$testmode) ;
		} else {												# Less than threshold so don't execute bulk send. Send individual service updates
			foreach my $servicekey (@services_list) {
				my $xml_message = "<SERVICE_STATUS ";	# Start message tag
				$xml_message .= "MonitorServerName=\"$thisnagios\" ";			# Default Identification - should set to IP address if known
				$xml_message .= "Host=\"$hostkey\" ";			# Default Identification - should set to IP address if known
				if ($hostipaddress{$hostkey}) {
					$xml_message .= "Device=\"".$hostipaddress{$hostkey}."\" ";			# No IP address, then set to host name
				} else {
					$xml_message .= "Device=\"$hostkey\" ";			# No IP address, then set to host name
				}
				$xml_message .= "ServiceDescription=\"$servicekey\" ";
				foreach my $field (sort keys %{$element_ref->{Host}->{$hostkey}->{Service}->{$servicekey}}) {
					my $tmpinfo = $element_ref->{Host}->{$hostkey}->{Service}->{$servicekey}->{$field};
					$tmpinfo =~ s/"/'/g;
					$xml_message .= "$field=\"$tmpinfo\" "; 
				}
				$xml_message .= "/>";			# End message tag
				print LOG $xml_message."\n\n" if ($debug>1);
				print $socket $xml_message if (!$testmode) ;
				$insertcount++;
			}
		}
	}
	print LOG "Total Services Insert Count=$insertcount.  Total bulk updates=$bulkcount. \n" if $debug ;
	return;
}



__END__

