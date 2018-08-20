#!/usr/bin/perl --
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
#use strict;
use Time::Local;
use IO::Socket;

my $debug =1 ;

my $eventfile = '/var/log/nagios/nagios.log';
my $seekfile = '/var/log/nagios/nagios_seek.tmp';

my $thisnagios = "localhost";
my $remote_host = $thisnagios;
my $remote_port = 4913;
my $socket;
chomp $thisnagios;

while (1) {					# Do forever 
	my $LoopCount = 0;
	my $SkipCount = 0;
	open LOG_FILE, $eventfile || die "Unable to open log file $eventfile: $!";
	# Try to open log seek file.  If open fails, we seek from beginning of
	# file by default.
	if (open(SEEK_FILE, $seekfile)) {
		chomp(@seek_pos = <SEEK_FILE>);
		close(SEEK_FILE);
		#  If file is empty, no need to seek...
		if ($seek_pos[0] != 0) {
			# Compare seek position to actual file size.  If file size is smaller
			# then we just start from beginning i.e. file was rotated, etc.
			($dev,$ino,$mode,$nlink,$uid,$gid,$rdev,$size,$atime,$mtime,$ctime,$blksize,$blocks) = stat(LOG_FILE);
			if ($seek_pos[0] <= $size) {
				seek(LOG_FILE, $seek_pos[0], 0);
			}
		}
	}
	$socket = IO::Socket::INET->new(PeerAddr => $remote_host,
                                PeerPort => $remote_port,
                                Proto    => "tcp",
                               Type     => SOCK_STREAM)
    or die "Couldn't connect to $remote_host:$remote_port : $@\n";

	while (my $line=<LOG_FILE>) {
		chomp $line;
#  Sample Events below
		if ($line =~ /^\s*\#]/) { next; }			
		@field = split /;/,$line;
		if ($field[0] =~ /\[(\d+)\]\s([\w\s]+):\s(\w+)/) {
			$timestamp = $1;
			$msgtype = $2;
			$host = $3;
		} else {		# Parse other formats here if necessary
			$SkipCount++;
			next; 
		}
		my $xml_message = "<NAGIOS_LOG ";	# Start message tag
		$xml_message .= "MonitorServerName=\"$thisnagios\" ";			# Default Identification - should set to IP address if known
		if ($msgtype =~ /HOST ALERT/) {
#		[1110304792] HOST ALERT: peter;UP;HARD;1;PING OK - Packet loss = 0%, RTA = 0.88 ms
			$xml_message .= "Host=\"$host\" ";					# Default Identification - should set to IP address if known
			if ($field[1] = "DOWN" )
			{
				$xml_message .= "Severity=\"CRITICAL\" ";	
				$xml_message .= "HostStatus=\"CRITICAL\" ";	
			}
			$tmp = $field[4];
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
		} elsif ($msgtype =~ /SERVICE ALERT/) {
#		[1110304792] SERVICE ALERT: peter;icmp_ping;OK;HARD;1;PING OK - Packet loss = 0%, RTA = 1.05 ms
			$xml_message .= "Host=\"$host\" ";					# Default Identification - should set to IP address if known
			$xml_message .= "ServiceDescription=\"$field[1]\" ";			# Invalid field??
			$xml_message .= "Severity=\"$field[2]\" ";	
			$xml_message .= "ServiceStatus=\"$field[2]\" ";	
			$tmp = $field[5];
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
		} elsif ($msgtype =~ /HOST NOTIFICATION/) {
#		[1110304792] HOST NOTIFICATION: nagios;peter;UP;host-notify-by-epager;PING OK - Packet loss = 0%, RTA = 0.88 ms
			$xml_message .= "Host=\"$field[1]\" ";	
			$xml_message .= "LoggerName=\"$host\" ";	
			if ($field[2] = "DOWN" )
			{
				$xml_message .= "Severity=\"CRITICAL\" ";	
				$xml_message .= "HostStatus=\"CRITICAL\" ";	
			}
			$xml_message .= "ApplicationName=\"$field[3]\" ";	
			$tmp = $field[4];
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
			$xml_message .= "SubComponent=\"$field[1]\" ";
			$xml_message .= "ErrorType=\"HOST NOTIFICATION\" ";
		} elsif ($msgtype =~ /SERVICE NOTIFICATION/) {
#		[1110304792] SERVICE NOTIFICATION: nagios;peter;check_http;CRITICAL;notify-by-epager;A HREF=http://192.168.2.146:80/ target=_blankConnection refused
			$xml_message .= "Host=\"$field[1]\" ";	
			$xml_message .= "LoggerName=\"$host\" ";	
			$xml_message .= "ServiceDescription=\"$field[2]\" ";		# Invalid field??	
			$xml_message .= "Severity=\"$field[3]\" ";	
			$xml_message .= "HostStatus=\"$field[3]\" ";	
			$xml_message .= "ApplicationName=\"$field[4]\" ";	
			$tmp = $field[5];
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
			$xml_message .= "SubComponent=\"$field[1]:$field[2]\" ";
			$xml_message .= "ErrorType=\"SERVICE NOTIFICATION\" ";
		} else {
			$SkipCount++;
			next;
		}
		$LoopCount++;
		$xml_message .= "/>";			# End message tag
		print $xml_message."\n\n" if $debug;
		print $socket $xml_message;
		die "im outa here"
	}	# All events read
	CommandClose($socket);
	close($socket);
	# Overwrite log seek file and print the byte position we have seeked to.
    open(SEEK_FILE, "> $seekfile") || die "Unable to open seek count file $seekfile: $!";
    print SEEK_FILE tell(LOG_FILE);
    # Close seek file.
    close(SEEK_FILE);
	# Close the log file.	
	close(LOG_FILE);
	print "Processed $LoopCount records. Skipped $SkipCount.\n";
	sleep 15;
}
exit;




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


sub FormatTime {
	my $intimestring=shift;
	my $outtimestring;
	if ($intimestring =~ /(\d{2})\/(\d{2})\/(\d{4}) (\d{2}:\d{2}:\d{2})/) {
		$outtimestring = "$3-$1-$2 $4";
	}
	return $outtimestring;
}


__END__

