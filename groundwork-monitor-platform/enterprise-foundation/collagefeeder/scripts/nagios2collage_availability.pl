#!/usr/bin/perl
#
#	GroundWork Monitor - The ultimate data integration framework.
#	Copyright (C) 2004-2006 GroundWork Open Source Solutions
#	info@itgroundwork.com
#
#	This program is free software; you can redistribute it and/or modify
#	it under the terms of version 2 of the GNU General Public License
#	as published by the Free Software Foundation.
#
#	This program is distributed in the hope that it will be useful,
#	but WITHOUT ANY WARRANTY; without even the implied warranty of
#	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
#	GNU General Public License for more details.
#
#	You should have received a copy of the GNU General Public License
#	along with this program; if not, write to the Free Software
#	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA
#
#	This script will load the SQL dashboard database to	enable the Groundwork Insight Reports.
#   A http request will be issued to generate Nagios availability reports.  This script will 
#   read the response and load the results in the database.
#
#		Options: 
#			-n <nagios server> Nagios server's IP address.
#			-r <realm>	Nagios security realm. This is on the userid/password dialog box when accessing a secure Nagios page.
#			-u <user>	Authorized user ID to access Nagios reports page.
#			-p <password>	Authorized pasword to access Nagios reports page.
#			-L <OUTPUT LOG>		Log file containing status messages from this program
#			-s <YYYYMMDD>	Start day (Default to yesterday, 00 hours).
#			-e <YYYYMMDD>	End day (Default to yesterday, 24 hours)
#			-d		Debug mode. Will log additional messages to the log file
#			-h		Displays help message.
#

my $helpstring = "
This script will load the SQL dashboard database to	enable the Groundwork Insight Reports.
A http request will be issued to generate Nagios availability reports.  This script will 
read the response and load the results in the database.

		Options: 
			-n <nagios server> Nagios server's IP address.
			-r <realm>	Nagios security realm. This is on the userid/password dialog box when accessing a secure Nagios page.
			-u <user>	Authorized user ID to access Nagios reports page.
			-p <password>	Authorized password to access Nagios reports page.
			-L <OUTPUT LOG>		Log file containing status messages from this program
			-s <YYYYMMDD>	Start day (Default to yesterday, 00 hours).
			-e <YYYYMMDD>	End day (Default to yesterday, 24 hours)
			-d		Debug mode. Will log additional messages to the log file
			-h		Displays help message.


	GroundWork Monitor - The ultimate data integration framework.
	Copyright (C) 2008 GroundWork Open Source Solutions
	info@itgroundwork.com

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

#use strict;
use DBI; 
use Time::Local;
use Getopt::Std;
use IO::Socket;
#use HTML::TreeBuilder 3;  # make sure our version isn't ancient

my $remote_host = "localhost";
my $remote_port = 4913;
my $socket;

my $host;	# database host
my $Logfile;	# Log file for this program. Use for debug
my $debug = 0;	# Set to 1 for debug mode.
my $nagios_ipaddr; 
my %opt = ();	# Program options hash
getopts("dhL:n:r:u:p:s:e:",\%opt);
if ($opt{h} or $opt{help}) {
	print $helpstring;
	exit;
}
if ($opt{d}) { $debug = 1;} 
if ($opt{u}) { 
	$remote_user = $opt{u};
} else {
	$remote_user = "nagiosadmin";
}
if ($opt{s}) { 
	if ($opt{s} =~ /(\d{4})(\d{2})(\d{2})/) {
		$startdate = $opt{s};
	} else {
		die "Invalid start date $opt{s}\n";
	}
} else { 
	($sec,$min,$hour,$mday,$mon,$year,$wday,$yday,$isdst) = localtime(time - (24*60*60));	# Compute yesterdays date stamp
	$startdate = sprintf "%04d%02d%02d", $year+1900 , $mon+1, $mday;
}
if ($opt{e}) { 
	if ($opt{e} =~ /(\d{4})(\d{2})(\d{2})/) {
		$enddate = $opt{e};
	} else {
		die "Invalid end date $opt{e}\n";
	}
	if ($enddate < $startdate) {
		die "Invalid dates; end date $enddate earlier than start date $startdate.\n";
	}
} else { 
	$enddate = $startdate;
}
print "Debug: $debug\n";
print "Output log: $Logfile\n";
print "Report days: start=$startdate, end=$enddate\n";

# Nagios Status grid URL is http://192.168.4.88/nagios/cgi-bin/status.cgi?hostgroup=all&style=grid
my $statusgrid_cgi="/usr/lib/nagios/cgi/status.cgi";
my $availreport_cgi="/usr/lib/nagios/cgi/avail.cgi";

my $cmd  = "export REQUEST_METHOD=GET;export QUERY_STRING=\"hostgroup=all&style=grid\";export REMOTE_USER=$remote_user;$statusgrid_cgi";	
@lines = `$cmd`;

$hg_ref=undef;
$current_hg=undef;
foreach $line (@lines) {
	if ($line =~ /a href=["']status.cgi\?hostgroup=(.*?)\&/i) {
		print "Host Group=$1\n"  if ($debug); 
		if ($1 !~ /all/i) {
			$current_hg=unencode($1);
			$hg_ref->{$current_hg}->{NAME}=$current_hg;
		}  
	}
	if ($line =~ /a href=["']status.cgi\?host=(.*?)["']/i) {
		print "Host=$1\n" if ($debug);
		$host=unencode($1);
		if ($current_hg) {
			$hg_ref->{$current_hg}->{HOST}->{$host}->{NAME}=unencode($host);	
		}
	}
	if ($line =~ /href=["']extinfo.cgi\?type=\d+\&host=(.*?)\&service=(.*?)["']/i) {
		print "Host=$1, Service=$2\n" if ($debug); 
		$host=unencode($1);
		$service=unencode($2);
		if ($current_hg) {
			$hg_ref->{$current_hg}->{HOST}->{$host}->{NAME}=$host;	
			$hg_ref->{$current_hg}->{HOST}->{$host}->{SERVICE}->{$service}->{NAME}=$service;	
		}
	}
}
if ($debug) {
	foreach $hg (sort keys %$hg_ref) {
		print "Host Group=$hg\n";
		foreach $host (sort keys %{$hg_ref->{$current_hg}->{HOST}} ) {
			print "\tHost=$host\n";
			foreach $service (sort keys %{$hg_ref->{$current_hg}->{HOST}->{$host}->{SERVICE}} ) {
				print "\t\tService=$service\n";
			}
		}
	}
}


# Get host avail by looking at the host availability report
#http://192.168.19.128/nagios/cgi-bin/avail.cgi?t1=1097161620&t2=1097766420&show_log_entries=&host=all&assumeinitialstates=yes&assumestateretention=yes&initialassumedstate=3&backtrack=4&timeperiod=yesterday
#$startutc = 1097161620 ;
#$endutc = 1097766420 ;
my @getdates = ();
$startdate =~ /(\d{4})(\d{2})(\d{2})/;
$tmputc = timelocal("00", "00", "00",$3, $2-1, $1-1900); 	  #	$TIME = timelocal($sec, $min, $hours, $mday, $mon, $year);
$enddate =~ /(\d{4})(\d{2})(\d{2})/;
$end_interval_utc = timelocal("59", "59", "23", $3, $2-1, $1-1900); 
while ($tmputc < $end_interval_utc) {
	($sec,$min,$hour,$mday,$mon,$year,$wday,$yday,$isdst) = localtime($tmputc);
	$timestring= sprintf "%04d%02d%02d",$year+1900,$mon+1,$mday;
	push @getdates, $timestring;
	print "Pushing $timestring\n";
	$tmputc += 24*60*60 ;  #	Calculate next day's start time 
}

$endutc = time;		
$startutc = $endutc - (1 * 60 * 60);	# start one hour ago
print "Processing date $date. start_utc=$startutc, end_utc=$endutc\n" if $debug;
$url= 	'show_log_entries='.
		'&host=all'.
		"&t1=$startutc".
		"&t2=$endutc". 		
		'&assumeinitialstates=yes'.
		'&assumestateretention=yes'.
		'&initialassumedstate=3'.
		'&backtrack=4'.
		'&timeperiod=custom'.
		'&csvoutput='
	;
print "##############################################################\n" if $debug;
print "Host Availability report: $availreport_cgi\n$url\n" if $debug;
my $cmd  = "export REQUEST_METHOD=GET;export QUERY_STRING=\"$url\";export REMOTE_USER=$remote_user;$availreport_cgi";
@lines = `$cmd`;
print @lines if $debug;

	#                        Host parameter: PERCENT_KNOWN_TIME_DOWN=0.000%
	#                        Host parameter: PERCENT_KNOWN_TIME_DOWN_SCHEDULED=0.000%
	#                        Host parameter: PERCENT_KNOWN_TIME_DOWN_UNSCHEDULED=0.000%
	#                        Host parameter: PERCENT_KNOWN_TIME_UNREACHABLE=0.000%
	#                        Host parameter: PERCENT_KNOWN_TIME_UNREACHABLE_SCHEDULED=0.000%
	#                        Host parameter: PERCENT_KNOWN_TIME_UNREACHABLE_UNSCHEDULED=0.000%
	#                        Host parameter: PERCENT_KNOWN_TIME_UP=100.000%
	#                        Host parameter: PERCENT_KNOWN_TIME_UP_SCHEDULED=0.000%
	#                        Host parameter: PERCENT_KNOWN_TIME_UP_UNSCHEDULED=100.000%
	#                        Host parameter: PERCENT_TIME_DOWN_SCHEDULED=0.000%
	#                        Host parameter: PERCENT_TIME_DOWN_UNSCHEDULED=0.000%
	#                        Host parameter: PERCENT_TIME_UNDETERMINED_NOT_RUNNING=51.078%
	#                        Host parameter: PERCENT_TIME_UNDETERMINED_NO_DATA=0.000%
	#                        Host parameter: PERCENT_TIME_UNREACHABLE_SCHEDULED=0.000%
	#                        Host parameter: PERCENT_TIME_UNREACHABLE_UNSCHEDULED=0.000%
	#                        Host parameter: PERCENT_TIME_UP_SCHEDULED=0.000%
	#                        Host parameter: PERCENT_TIME_UP_UNSCHEDULED=48.922%
	#                        Host parameter: PERCENT_TOTAL_TIME_DOWN=0.000%
	#                        Host parameter: PERCENT_TOTAL_TIME_UNDETERMINED=51.078%
	#                        Host parameter: PERCENT_TOTAL_TIME_UNREACHABLE=0.000%
	#                        Host parameter: PERCENT_TOTAL_TIME_UP=48.922%
	#                        Host parameter: TIME_DOWN_SCHEDULED=0
	#                        Host parameter: TIME_DOWN_UNSCHEDULED=0
	#                        Host parameter: TIME_UNDETERMINED_NOT_RUNNING=44131
	#                        Host parameter: TIME_UNDETERMINED_NO_DATA=0
	#                        Host parameter: TIME_UNREACHABLE_SCHEDULED=0
	#                        Host parameter: TIME_UNREACHABLE_UNSCHEDULED=0
	#                        Host parameter: TIME_UP_SCHEDULED=0
	#                        Host parameter: TIME_UP_UNSCHEDULED=42269
	#                        Host parameter: TOTAL_TIME_DOWN=0
	#                        Host parameter: TOTAL_TIME_UNDETERMINED=44131
	#                        Host parameter: TOTAL_TIME_UNREACHABLE=0
	#                        Host parameter: TOTAL_TIME_UP=42269

my $host_ref = undef;
$keys_found = 0;
for ($i=1; $i<=$#lines ;$i++) {
	chomp $lines[$i];
	if (!$keys_found) {
		if ($lines[$i] !~ /^HOST_NAME/) { next }
		print  "keys=".$lines[$i]."\n"  if ($debug);
		@keys = split /\s*,\s*/ , $lines[$i];
		$keys_found = 1;
		next;
	}
	print "values=".$lines[$i]."\n"  if ($debug);
	@values = split /\s*,\s*/ , $lines[$i];
	$values[0]=~s/"//g;		#	strip "s from host name field
	for ($j=1; $j<=$#keys ;$j++) {
		$values[$j]=~s/%//g;		#	strip %s from percent values
		$host_ref->{HOST}->{$values[0]}->{PARAMETER}->{$keys[$j]}=$values[$j];
	}
}

# Get service avail by looking at the services availability report
#http://192.168.19.128/nagios/cgi-bin/avail.cgi?show_log_entries=&host=localhost&service=all&timeperiod=yesterday&smon=10&sday=1&syear=2004&shour=0&smin=0&ssec=0&emon=10&eday=14&eyear=2004&ehour=24&emin=0&esec=0&assumeinitialstates=yes&assumestateretention=yes&initialassumedstate=6&backtrack=4&csvoutput=
$url = 'show_log_entries='.  
		'&host=all'.
		'&service=all'.
		"&t1= $startutc".
		"&t2=$endutc". 		
		'&assumeinitialstates=yes'.
		'&assumestateretention=yes'.
		'&initialassumedstate=6'.
		'&backtrack=4'.
		'&timeperiod=custom'.
		'&csvoutput='
	;
print "##############################################################\n" if $debug;
print "Service Availability report: $availreport_cgi\n$url\n" if $debug;
	#	
	# Service Parameters
	#						Service parameter: PERCENT_KNOWN_TIME_CRITICAL=0.000%
	#						Service parameter: PERCENT_KNOWN_TIME_CRITICAL_SCHEDULED=0.000%
	#                       Service parameter: PERCENT_KNOWN_TIME_CRITICAL_UNSCHEDULED=0.000%
	#                       Service parameter: PERCENT_KNOWN_TIME_OK=100.000%
	#                       Service parameter: PERCENT_KNOWN_TIME_OK_SCHEDULED=0.000%
	#                       Service parameter: PERCENT_KNOWN_TIME_OK_UNSCHEDULED=100.000%
	#                       Service parameter: PERCENT_KNOWN_TIME_UNKNOWN=0.000%
	#                       Service parameter: PERCENT_KNOWN_TIME_UNKNOWN_SCHEDULED=0.000%
	#                       Service parameter: PERCENT_KNOWN_TIME_UNKNOWN_UNSCHEDULED=0.000%
	#                       Service parameter: PERCENT_KNOWN_TIME_WARNING=0.000%
	#                       Service parameter: PERCENT_KNOWN_TIME_WARNING_SCHEDULED=0.000%
	#                       Service parameter: PERCENT_KNOWN_TIME_WARNING_UNSCHEDULED=0.000%
	#                       Service parameter: PERCENT_TIME_CRITICAL_SCHEDULED=0.000%
	#                       Service parameter: PERCENT_TIME_CRITICAL_UNSCHEDULED=0.000%
	#                       Service parameter: PERCENT_TIME_OK_SCHEDULED=0.000%
	#                       Service parameter: PERCENT_TIME_OK_UNSCHEDULED=48.922%
	#                       Service parameter: PERCENT_TIME_UNDETERMINED_NOT_RUNNING=51.078%
	#                       Service parameter: PERCENT_TIME_UNDETERMINED_NO_DATA=0.000%
	#                       Service parameter: PERCENT_TIME_UNKNOWN_SCHEDULED=0.000%
	#                       Service parameter: PERCENT_TIME_UNKNOWN_UNSCHEDULED=0.000%
	#                       Service parameter: PERCENT_TIME_WARNING_SCHEDULED=0.000%
	#                       Service parameter: PERCENT_TIME_WARNING_UNSCHEDULED=0.000%
	#                       Service parameter: PERCENT_TOTAL_TIME_CRITICAL=0.000%
	#                       Service parameter: PERCENT_TOTAL_TIME_OK=48.922%
	#                       Service parameter: PERCENT_TOTAL_TIME_UNDETERMINED=51.078%
	#                       Service parameter: PERCENT_TOTAL_TIME_UNKNOWN=0.000%
	#                       Service parameter: PERCENT_TOTAL_TIME_WARNING=0.000%
	#                       Service parameter: TIME_CRITICAL_SCHEDULED=0
	#                       Service parameter: TIME_CRITICAL_UNSCHEDULED=0
	#                       Service parameter: TIME_OK_SCHEDULED=0
	#                       Service parameter: TIME_OK_UNSCHEDULED=42269
	#                       Service parameter: TIME_UNDETERMINED_NOT_RUNNING=44131
	#                       Service parameter: TIME_UNDETERMINED_NO_DATA=0
	#                       Service parameter: TIME_UNKNOWN_SCHEDULED=0
	#                       Service parameter: TIME_UNKNOWN_UNSCHEDULED=0
	#                       Service parameter: TIME_WARNING_SCHEDULED=0
	#                       Service parameter: TIME_WARNING_UNSCHEDULED=0
	#                       Service parameter: TOTAL_TIME_CRITICAL=0
	#                       Service parameter: TOTAL_TIME_OK=42269
	#                       Service parameter: TOTAL_TIME_UNDETERMINED=44131
	#                       Service parameter: TOTAL_TIME_UNKNOWN=0
	#                       Service parameter: TOTAL_TIME_WARNING=0
my $cmd  = "export REQUEST_METHOD=GET;export QUERY_STRING=\"$url\";export REMOTE_USER=$remote_user;$availreport_cgi";
@lines = `$cmd`;
print @lines if $debug;

my $service_ref = undef;
$keys_found = 0;
for ($i=1; $i<=$#lines ;$i++) {
	chomp $lines[$i];
	if (!$keys_found) {
		if ($lines[$i] !~ /^HOST_NAME/) { next }
		print  "keys=".$lines[$i]."\n"  if ($debug);
		@keys = split /\s*,\s*/ , $lines[$i];
		$keys_found = 1;
		next;
	}
	print "values=".$lines[$i]."\n"  if ($debug);
	@values = split /\s*,\s*/ , $lines[$i];
	$values[0]=~s/"//g;		#	strip "s from host name field
	$values[1]=~s/"//g;		#	strip "s from service name
	for ($j=1; $j<=$#keys ;$j++) {
		$values[$j]=~s/%//g;		#	strip %s from percent values
		$service_ref->{HOST}->{$values[0]}->{SERVICE}->{$values[1]}->{PARAMETER}->{$keys[$j]}=$values[$j];
	}
}

if ($debug) {
	foreach $host (sort keys %{$host_ref->{HOST}}) {
		print "host=$host\n";
		foreach $param (sort keys %{$host_ref->{HOST}->{$host}->{PARAMETER}}) {
			print " $host, Host parameter: $param=".$host_ref->{HOST}->{$host}->{PARAMETER}->{$param}."\n";
		}
		foreach $service (sort keys %{$service_ref->{HOST}->{$host}->{SERVICE}}) {
			foreach $param (sort keys %{$service_ref->{HOST}->{$host}->{SERVICE}->{$service}->{PARAMETER}}) {
				print "$host, $service Service parameter: $param=".$service_ref->{HOST}->{$host}->{SERVICE}->{$service}->{PARAMETER}->{$param}."\n";
			}
		}
	}
}

#	Compute host group availability into the database
#		first sum all values
foreach $hg (sort keys %$hg_ref) {
	foreach $host (sort keys %{$hg_ref->{$hg}->{HOST}} ) {
		foreach $param (sort keys %{$host_ref->{HOST}->{$host}->{PARAMETER}}) {
			$host_ref->{"HOST GROUP"}->{$hg}->{"HOST PARAMETER"}->{SUM}->{$param} += $host_ref->{HOST}->{$host}->{PARAMETER}->{$param};	# Sum all parameter values to compute avg
			$host_ref->{"HOST GROUP"}->{$hg}->{"HOST PARAMETER"}->{COUNT}->{$param}++;
		}
		foreach $service (sort keys %{$hg_ref->{$hg}->{HOST}->{$host}->{SERVICE}} ) {
			foreach $param (sort keys %{$service_ref->{HOST}->{$host}->{SERVICE}->{$service}->{PARAMETER}}) {
				$service_ref->{"HOST GROUP"}->{$hg}->{"SERVICE PARAMETER"}->{SUM}->{$param} += $service_ref->{HOST}->{$host}->{SERVICE}->{$service}->{PARAMETER}->{$param};	# Sum all parameter values to compute avg
				$service_ref->{"HOST GROUP"}->{$hg}->{"SERVICE PARAMETER"}->{COUNT}->{$param}++; 
			}
		}
	}
}




$debug = 1;
	$socket = IO::Socket::INET->new(PeerAddr => $remote_host,
                                PeerPort => $remote_port,
                                Proto    => "tcp",
                               Type     => SOCK_STREAM)
    or die "Couldn't connect to $remote_host:$remote_port : $@\n";

#	Insert host and service data into the database
foreach $host (sort keys %{$host_ref->{HOST}}) {
	#print $socket formatxml_host_avail($host,$startutc,$endutc,\%{$host_ref->{HOST}->{$host}->{PARAMETER}});
	#print formatxml_host_avail($host,$startutc,$endutc,\%{$host_ref->{HOST}->{$host}->{PARAMETER}})."\n\n" if $debug;
	$processcount++;
	foreach $service (sort keys %{$service_ref->{HOST}->{$host}->{SERVICE}}) {
		print $socket formatxml_service_avail($host,$service,$startutc,$endutc,\%{$service_ref->{HOST}->{$host}->{SERVICE}->{$service}->{PARAMETER}});
		print formatxml_service_avail($host,$service,$startutc,$endutc,\%{$service_ref->{HOST}->{$host}->{SERVICE}->{$service}->{PARAMETER}})."\n\n" if ($debug);
		$processcount++;
	}
}
# now divide to compute avg
foreach $hg (sort keys %$hg_ref) {
	foreach $param (sort keys %{$host_ref->{"HOST GROUP"}->{$hg}->{"HOST PARAMETER"}->{SUM}}) {
		$host_ref->{"HOST GROUP"}->{$hg}->{"HOST PARAMETER"}->{AVERAGE}->{$param} = 
			$host_ref->{"HOST GROUP"}->{$hg}->{"HOST PARAMETER"}->{SUM}->{$param} / 
			$host_ref->{"HOST GROUP"}->{$hg}->{"HOST PARAMETER"}->{COUNT}->{$param};
	}
	print $socket formatxml_hostgroup_host_avail($hg,$startutc,$endutc,\%{$host_ref->{"HOST GROUP"}->{$hg}->{"HOST PARAMETER"}->{AVERAGE}} );
	print formatxml_hostgroup_host_avail($hg,$startutc,$endutc,\%{$host_ref->{"HOST GROUP"}->{$hg}->{"HOST PARAMETER"}->{AVERAGE}} )."\n\n" if $debug;
	foreach $param (sort keys %{$service_ref->{"HOST GROUP"}->{$hg}->{"SERVICE PARAMETER"}->{SUM}}) {
		$service_ref->{"HOST GROUP"}->{$hg}->{"SERVICE PARAMETER"}->{AVERAGE}->{$param} = 
			$service_ref->{"HOST GROUP"}->{$hg}->{"SERVICE PARAMETER"}->{SUM}->{$param} / 
			$service_ref->{"HOST GROUP"}->{$hg}->{"SERVICE PARAMETER"}->{COUNT}->{$param};	
	}
	print $socket formatxml_hostgroup_service_avail($hg,$startutc,$endutc,\%{$service_ref->{"HOST GROUP"}->{$hg}->{"SERVICE PARAMETER"}->{AVERAGE}} 	);
	print formatxml_hostgroup_service_avail($hg,$startutc,$endutc,\%{$service_ref->{"HOST GROUP"}->{$hg}->{"SERVICE PARAMETER"}->{AVERAGE}} 	)."\n\n" if $debug;
}

CommandClose($socket);
close($socket);
exit;

sub unencode {
	my $coded_string = shift;
	$coded_string =~ s/%([0-9a-f]{2})/pack("c",hex($1))/gie;
	return $coded_string;
}

sub formatxml_host_avail {
	my $host=shift;
	my $starttime=time_text(shift);
	my $endtime=time_text(shift);
	my $parameter_ref=shift;
	my $xml_message = "<HOST_AVAILABILITY ";	# Start message tag
#	$xml_message .= "MonitorServerName=\"$thisnagios\" ";			# Need this??
	$xml_message .= "Host=\"$host\" ";					
	$xml_message .= "StartTime=\"$starttime\" ";	
	$xml_message .= "EndTime=\"$endtime\" ";	
	foreach my $param (keys %{$parameter_ref}) {
		$xml_message .= " $param=\"".$parameter_ref->{$param}."\" ";
	}
	$xml_message .= "/>";	
    return $xml_message;
}	

sub formatxml_hostgroup_host_avail {
	my $hostgroup=shift;
	my $starttime=time_text(shift);
	my $endtime=time_text(shift);
	my $parameter_ref=shift;
	my $xml_message = "<HOSTGROUP_HOST_AVAILABILITY ";	# Start message tag
#	$xml_message .= "MonitorServerName=\"$thisnagios\" ";			# Need this??
	$xml_message .= "Hostgroup=\"$hostgroup\" ";					
	$xml_message .= "StartTime=\"$starttime\" ";	
	$xml_message .= "EndTime=\"$endtime\" ";	
	foreach my $param (keys %{$parameter_ref}) {
		$xml_message .= " $param=\"".$parameter_ref->{$param}."\" ";
	}
	$xml_message .= "/>";	
    return $xml_message;
}	

sub formatxml_hostgroup_service_avail {
	my $hostgroup=shift;
	my $starttime=time_text(shift);
	my $endtime=time_text(shift);
	my $parameter_ref=shift;
	my $xml_message = "<HOSTGROUP_SERVICE_AVAILABILITY ";	# Start message tag
#	$xml_message .= "MonitorServerName=\"$thisnagios\" ";			# Need this??
	$xml_message .= "Hostgroup=\"$hostgroup\" ";					
	$xml_message .= "StartTime=\"$starttime\" ";	
	$xml_message .= "EndTime=\"$endtime\" ";	
	foreach my $param (keys %{$parameter_ref}) {
		$xml_message .= " $param=\"".$parameter_ref->{$param}."\" ";
	}
	$xml_message .= "/>";	
    return $xml_message;
}	


sub formatxml_service_avail {
	my $host=shift;
	my $service=shift;
	my $starttime=time_text(shift);
	my $endtime=time_text(shift);
	my $parameter_ref=shift;
	my $xml_message = "<SERVICE_AVAILABILITY ";	# Start message tag
#	$xml_message .= "MonitorServerName=\"$thisnagios\" ";			# Need this??
	$xml_message .= "Host=\"$host\" ";					
	$xml_message .= "ServiceDescription=\"$service\" ";					
	$xml_message .= "StartTime=\"$starttime\" ";	
	$xml_message .= "EndTime=\"$endtime\" ";	
	foreach my $param (keys %{$parameter_ref}) {
		$xml_message .= " $param=\"".$parameter_ref->{$param}."\" ";
	}
	$xml_message .= "/>";	
    return $xml_message;
}	

sub time_text {
		my $timestamp = shift;
		if ($timestamp <= 0) {
			return "none";
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
	print $socket $xml_message;
	return;
}



__END__





