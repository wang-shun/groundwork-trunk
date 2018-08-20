#!/usr/local/groundwork/bin/perl --
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

my $helpstring = "
add description here

		Options: 
			-u <user>	Authorized user ID to access Nagios reports page.
			-L <OUTPUT LOG>		Log file containing status messages from this program
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

my $Logfile = "" ;	# Log file for this program. 
my $rrd_dir = "/usr/local/groundwork/collage/feeder/rrd" ;	# Directory containing RRDs 
my $nagioscgi_dir="/usr/local/groundwork/apache2/cgi-bin";	# Directory containing Nagios CGI programs
my $sample_interval = 1 * 60 * 60;			# how often this program will be called - in seconds
#my $sample_interval = 1 * 60 * 5;			# Test with a 5 minute interval
my $Rrdtool = "/usr/local/groundwork/bin/rrdtool";				# Location of rrdtool executable
my $remote_host = "localhost";
my $remote_port = 4913;


my $debug = 1;	# Set to 1 for debug mode.
my $testmode = 0;   # Set to 1 to disable socket communications for testing
my $logfile; 
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
if ($opt{L}) { 
	$logfile = $opt{L};
} else {
	$logfile = "/usr/local/groundwork/collage/feeder/nagios2collage_availability.log";
}

open (LOG,">$logfile");
print LOG "Program started at ".time_text(time)."\n";
print LOG "Debug: $debug\n";
print LOG "Output log: $logfile\n";

# Nagios Status grid URL is http://192.168.4.88/nagios/cgi-bin/status.cgi?hostgroup=all&style=grid
my $statusgrid_cgi=$nagioscgi_dir."/status.cgi";
my $availreport_cgi=$nagioscgi_dir."/avail.cgi";
my $cmd  = "export REQUEST_METHOD=GET;export QUERY_STRING=\"hostgroup=all&style=grid\";export REMOTE_USER=$remote_user;$statusgrid_cgi";	
@lines = `$cmd`;
$hg_ref=undef;
$current_hg=undef;
foreach $line (@lines) {
	if ($line =~ /a href=["']status.cgi\?hostgroup=(.*?)\&/i) {
		print LOG "Host Group=$1\n"  if ($debug); 
		if ($1 !~ /^all$/i) {
			$current_hg=unencode($1);
			$hg_ref->{$current_hg}->{NAME}=$current_hg;
		}  
	}
	if ($line =~ /a href=["']status.cgi\?host=(.*?)["']/i) {
		print LOG "Host=$1\n" if ($debug);
		$host=unencode($1);
		if ($current_hg) {
			$hg_ref->{$current_hg}->{HOST}->{$host}->{NAME}=unencode($host);	
		}
	}
	if ($line =~ /href=["']extinfo.cgi\?type=\d+\&host=(.*?)\&service=(.*?)["']/i) {
		print LOG "Host=$1, Service=$2\n" if ($debug); 
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
		print LOG "Host Group=$hg\n";
		foreach $host (sort keys %{$hg_ref->{$current_hg}->{HOST}} ) {
			print LOG "\tHost=$host\n";
			foreach $service (sort keys %{$hg_ref->{$current_hg}->{HOST}->{$host}->{SERVICE}} ) {
				print LOG "\t\tService=$service\n";
			}
		}
	}
}


# Get host avail by looking at the host availability report
#http://192.168.19.128/nagios/cgi-bin/avail.cgi?t1=1097161620&t2=1097766420&show_log_entries=&host=all&assumeinitialstates=yes&assumestateretention=yes&initialassumedstate=3&backtrack=4&timeperiod=yesterday
#$startutc = 1097161620 ;
#$endutc = 1097766420 ;
$endutc = time;		
$startutc = $endutc - $sample_interval;	# start at last sample interval
print LOG "Processing date $date. start_utc=$startutc, end_utc=$endutc\n" if $debug;
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
print LOG "##############################################################\n" if $debug;
print LOG "Host Availability report: $availreport_cgi\n$url\n" if $debug;
my $cmd  = "export REQUEST_METHOD=GET;export QUERY_STRING=\"$url\";export REMOTE_USER=$remote_user;$availreport_cgi";
@lines = `$cmd`;
print LOG @lines if $debug;
my $host_ref = undef;
$keys_found = 0;
for ($i=1; $i<=$#lines ;$i++) {
	chomp $lines[$i];
	if (!$keys_found) {
		if ($lines[$i] !~ /^HOST_NAME/) { next }
		print  LOG "keys=".$lines[$i]."\n"  if ($debug);
		@keys = split /\s*,\s*/ , $lines[$i];
		$keys_found = 1;
		next;
	}
	print LOG "values=".$lines[$i]."\n"  if ($debug);
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
print LOG "##############################################################\n" if $debug;
print LOG "Service Availability report: $availreport_cgi\n$url\n" if $debug;

my $cmd  = "export REQUEST_METHOD=GET;export QUERY_STRING=\"$url\";export REMOTE_USER=$remote_user;$availreport_cgi";
@lines = `$cmd`;
print LOG @lines if $debug;

my $service_ref = undef;
$keys_found = 0;
for ($i=1; $i<=$#lines ;$i++) {
	chomp $lines[$i];
	if (!$keys_found) {
		if ($lines[$i] !~ /^HOST_NAME/) { next }
		print  LOG "keys=".$lines[$i]."\n"  if ($debug);
		@keys = split /\s*,\s*/ , $lines[$i];
		$keys_found = 1;
		next;
	}
	print LOG "values=".$lines[$i]."\n"  if ($debug);
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
		print LOG "host=$host\n";
		foreach $param (sort keys %{$host_ref->{HOST}->{$host}->{PARAMETER}}) {
			print LOG " $host, Host parameter: $param=".$host_ref->{HOST}->{$host}->{PARAMETER}->{$param}."\n";
		}
		foreach $service (sort keys %{$service_ref->{HOST}->{$host}->{SERVICE}}) {
			foreach $param (sort keys %{$service_ref->{HOST}->{$host}->{SERVICE}->{$service}->{PARAMETER}}) {
				print LOG "$host, $service Service parameter: $param=".$service_ref->{HOST}->{$host}->{SERVICE}->{$service}->{PARAMETER}->{$param}."\n";
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

if (!$testmode) {
	$socket = IO::Socket::INET->new(PeerAddr => $remote_host,
                                PeerPort => $remote_port,
                                Proto    => "tcp",
                               Type     => SOCK_STREAM)
    or print "Couldn't connect to $remote_host:$remote_port : $@\n";
}

# Send Hostgroup members to Collage
#$hg_ref->{$current_hg}->{HOST}->{$host}
foreach $hg  (sort keys %$hg_ref) {
	$hostlist = "";
	foreach $host (sort keys %{$hg_ref->{$hg}->{HOST}}) {
		$hostlist .= $host.",";
	}
	$hostlist =~ s/,$//;	# take off last comma
	my $xmlstring = formatxml_hostgroup_add($hg,$hostlist);
	print $socket $xmlstring if (!$testmode) ;
	print LOG $xmlstring."\n\n" if $debug;
}



#	Insert host and service data into the database
foreach $host (sort keys %{$host_ref->{HOST}}) {
	my $xmlstring = formatxml_host_avail($host,$startutc,$endutc,\%{$host_ref->{HOST}->{$host}->{PARAMETER}}); 
	print $socket $xmlstring if (!$testmode) ;
	print LOG $xmlstring."\n\n" if $debug;
	$processcount++;
	foreach $service (sort keys %{$service_ref->{HOST}->{$host}->{SERVICE}}) {
		my $xmlstring =  formatxml_service_avail($host,$service,$startutc,$endutc,\%{$service_ref->{HOST}->{$host}->{SERVICE}->{$service}->{PARAMETER}});
		print $socket $xmlstring if (!$testmode) ;
		print LOG $xmlstring."\n\n" if $debug;
		$processcount++;
#		sleep 1;
	}
}
# now divide to compute avg
foreach $hg (sort keys %$hg_ref) {
	foreach $param (sort keys %{$host_ref->{"HOST GROUP"}->{$hg}->{"HOST PARAMETER"}->{SUM}}) {
		$host_ref->{"HOST GROUP"}->{$hg}->{"HOST PARAMETER"}->{AVERAGE}->{$param} = 
			$host_ref->{"HOST GROUP"}->{$hg}->{"HOST PARAMETER"}->{SUM}->{$param} / 
			$host_ref->{"HOST GROUP"}->{$hg}->{"HOST PARAMETER"}->{COUNT}->{$param};
		# Round off Time values (in seconds) to integer values
		if ($param =~ /^(TIME_|TOTAL_TIME_)/) {
			$host_ref->{"HOST GROUP"}->{$hg}->{"HOST PARAMETER"}->{AVERAGE}->{$param} = 
					int(0.5 + $host_ref->{"HOST GROUP"}->{$hg}->{"HOST PARAMETER"}->{AVERAGE}->{$param});
		}

	}
	my $xmlstring = formatxml_hostgroup_host_avail($hg,$startutc,$endutc,\%{$host_ref->{"HOST GROUP"}->{$hg}->{"HOST PARAMETER"}->{AVERAGE}} );
	print $socket $xmlstring if (!$testmode) ;
	print LOG $xmlstring."\n\n" if $debug;
	foreach $param (sort keys %{$service_ref->{"HOST GROUP"}->{$hg}->{"SERVICE PARAMETER"}->{SUM}}) {
		$service_ref->{"HOST GROUP"}->{$hg}->{"SERVICE PARAMETER"}->{AVERAGE}->{$param} = 
			$service_ref->{"HOST GROUP"}->{$hg}->{"SERVICE PARAMETER"}->{SUM}->{$param} / 
			$service_ref->{"HOST GROUP"}->{$hg}->{"SERVICE PARAMETER"}->{COUNT}->{$param};	
		# Round off Time values (in seconds) to integer values
		if ($param =~ /^(TIME_|TOTAL_TIME_)/) {
			$service_ref->{"HOST GROUP"}->{$hg}->{"SERVICE PARAMETER"}->{AVERAGE}->{$param} = 
					int(0.5 + $service_ref->{"HOST GROUP"}->{$hg}->{"SERVICE PARAMETER"}->{AVERAGE}->{$param});
		}
	}
	my $xmlstring = formatxml_hostgroup_service_avail($hg,$startutc,$endutc,\%{$service_ref->{"HOST GROUP"}->{$hg}->{"SERVICE PARAMETER"}->{AVERAGE}} 	);
	print $socket $xmlstring if (!$testmode) ;
	print LOG $xmlstring."\n\n" if $debug;
#	sleep 1;

}
CommandClose($socket) if (!$testmode) ;
close($socket) if (!$testmode) ;

# Do RRD update 
foreach $hg (sort keys %$hg_ref) {
	foreach $param (sort keys %{$service_ref->{"HOST GROUP"}->{$hg}->{"SERVICE PARAMETER"}->{AVERAGE}}) {
		if ($param eq "PERCENT_TIME_CRITICAL_UNSCHEDULED") {
			updaterrd($sample_interval,$Rrdtool,$rrd_dir,$hg,$service_ref->{"HOST GROUP"}->{$hg}->{"SERVICE PARAMETER"}->{AVERAGE}->{$param});
		}
	}
}
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

sub updaterrd {
	my $sample_interval = shift;
	my $Rrdtool =shift;
	my $rrd_dir = shift;
	my $hg = shift;
	my $percent = shift;
	$rrdname = "$rrd_dir/$hg.rrd";
	# ADD: check to see if RRD exists. If not then create
	if (!stat($rrdname)) {
		print LOG  "Creating $rrdname\n";
		$create_rrd_cmd =
                        "$Rrdtool create $rrdname ".
                        "--step $sample_interval --start n-1yr ".
                        "DS:percent:GAUGE:36000:U:U ".
                        "RRA:MIN:0.5:1:14400 ".
                        "RRA:MIN:0.5:5:20160 ".
                        "RRA:MIN:0.5:15:28800 ".
                        "RRA:MIN:0.5:60:43200 ".
						"RRA:AVERAGE:0.5:1:14400 ".
                        "RRA:AVERAGE:0.5:5:20160 ".
                        "RRA:AVERAGE:0.5:15:28800 ".
                        "RRA:AVERAGE:0.5:60:43200 ".
                        "RRA:MAX:0.5:1:14400 ".
                        "RRA:MAX:0.5:5:20160 ".
                        "RRA:MAX:0.5:15:28800 ".
                        "RRA:MAX:0.5:60:43200 ";
		my @lines = qx($create_rrd_cmd);
		print LOG "RRD create command: $create_rrd_cmd\n"  if $debug ;
		print LOG @lines  if $debug;
		$cmd = "chown apache.apache $rrdname";
		@lines = qx($cmd);
		print LOG "Command: $cmd\n" if $debug;
		print LOG @lines if $debug;
		$cmd = "chmod g+w $rrdname";
		@lines = qx($cmd);
		print LOG "Command: $cmd\n" if $debug;
		print LOG @lines if $debug;
	}
	# update RRD
	my $checktime = time;
	print LOG "Values: percent used=$percent\n" if $debug;
	my $rrdcommand = "$Rrdtool update $rrdname $checktime:$percent 2>&1";
	print LOG qq($rrdcommand) if $debug;
	my @lines = qx($rrdcommand);
	print LOG "\nReturn: " . "@lines" . "\n" if $debug;
}

__END__



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

