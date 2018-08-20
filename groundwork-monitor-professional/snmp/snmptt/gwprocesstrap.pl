#!/usr/local/groundwork/perl/bin/perl --

# gwprocesstrap.pl

# Copyright 2007-2014 GroundWork Open Source, Inc. ("GroundWork")
# All rights reserved. This program is free software; you can redistribute it
# and/or modify it under the terms of the GNU General Public License version 2
# as published by the Free Software Foundation.
#
# This program is distributed in the hope that it will be useful, but WITHOUT
# ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
# FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License along with
# this program; if not, write to the Free Software Foundation, Inc., 51 Franklin
# Street, Fifth Floor, Boston, MA 02110-1301, USA.

# snmptt.conf exec command format:
# EXEC /usr/local/groundwork/nagios/eventhandlers/gwprocesstrap.pl "$x" "$X" "$aA" "$A" "$o" "$O" "$s" "$N" "$c" "$+*" "trap description text"

use strict;
use IO::Socket;
## use File::stat;
use DBI;
use CollageQuery;
use HTML::Entities;

my $send_to_nagios  = 1;
my $thisnagios      = 'localhost';
my $logfile         = '/usr/local/groundwork/common/var/log/snmp/gwprocesstrap.log';
my $nagios_cmd_pipe = '/usr/local/groundwork/nagios/var/spool/nagios.cmd';
my $service_last    = 'snmptraps_last';
my $echo_cmd        = '/bin/echo';

# This timeout is here only for use in emergencies, when Foundation has completely frozen up and is no
# longer reading (will never read) a socket we have open.  We don't want to set this value so low that
# it will interfere with normal communication, even given the fact that Foundation may wait a rather
# long time between sips from this straw as it processes a large bundle of messages that we sent it, or
# is otherwise busy and just cannot get back around to reading the socket in a reasonably short time.
my $socket_send_timeout = 60;

my $timestamp = "$ARGV[0] $ARGV[1]";
my $ipaddr    = $ARGV[2];
my $host      = $ARGV[3];
my $oid       = $ARGV[4];
my $oid_sym   = $ARGV[5];
my $service   = 'snmp_traps';
my $sev       = $ARGV[6];              # NORMAL|OK => 0, MINOR|WARNING => 1, MAJOR|CRITICAL => 2, UNKNOWN => 3
my $eventname = $ARGV[7];
my $category  = $ARGV[8];
my $varbinds  = $ARGV[9];
my $msg       = $ARGV[10];

# Set defaults
if ( $ipaddr eq '' ) {
    if ( open LOG, '>>', $logfile ) {
	print LOG "ERROR:  Invalid host and IP address. Args are:\n";
	foreach my $arg (@ARGV) {
	    print LOG "$arg\n";
	}
	close LOG;
    }
    exit;
}

my %ipaddresshost = ();

# Get hosts->IPaddress from Monarch
my ( $dbname, $dbhost, $dbuser, $dbpass, $dbtype ) = CollageQuery::readGroundworkDBConfig('monarch');
my $dsn = '';
if ( defined($dbtype) && $dbtype eq 'postgresql' ) {
    $dsn = "DBI:Pg:dbname=$dbname;host=$dbhost";
}
else {
    $dsn = "DBI:mysql:database=$dbname;host=$dbhost";
}
my $dbh = DBI->connect( $dsn, $dbuser, $dbpass, { 'AutoCommit' => 1 } );
if ($dbh) {
    my $query = "select name,address from hosts where address=\"$ipaddr\"; ";
    my $sth   = $dbh->prepare($query);
    $sth->execute() or die $@;
    while ( my $row = $sth->fetchrow_hashref() ) {
	$ipaddresshost{ $$row{address} } = $$row{name};
    }
    $dbh->disconnect();
}
else {
    if ( open LOG, '>>', $logfile ) {
	print LOG "ERROR:  Can't connect to database $dbname. Error:" . $DBI::errstr;
	foreach my $arg (@ARGV) {
	    print LOG "$arg\n";
	}
	close LOG;
    }
}
if ( $ipaddresshost{$ipaddr} ) {
    $host = $ipaddresshost{$ipaddr};    # set the host to the Monarch hostname for this IP address
}    # else the host is set to the resolved host name
if ( !$sev ) {
    $sev = 'UNKNOWN';
}
my $nagiossev     = 3;            # set default value as unknown
my $monitorstatus = 'UNKNOWN';    # set default value as unknown
if ( $sev =~ /(NORMAL|OK|INFORMATIONAL)/i ) {
    $nagiossev     = 0;
    $monitorstatus = 'OK';
}
elsif ( $sev =~ /(MINOR|WARNING)/i ) {
    $nagiossev     = 1;
    $monitorstatus = 'WARNING';
}
elsif ( $sev =~ /(MAJOR|CRITICAL)/i ) {
    $nagiossev     = 2;
    $monitorstatus = 'CRITICAL';
}

# submit to snmp_trap service for the host
if ($send_to_nagios) {
    ## FIX MINOR:  This use of "echo" to write to the pipe is absurdly expensive,
    ## and doesn't necessarily guarantee the required atomic write to the pipe.
    ## We ought to convert this to call our GW::Nagios package instead.
    if ( stat($nagios_cmd_pipe) ) {
	my $datetime = time;
	my $cmdline  = "[$datetime] PROCESS_SERVICE_CHECK_RESULT;$host;$service_last;$nagiossev;$msg";
	my @lines    = `$echo_cmd "$cmdline" >> $nagios_cmd_pipe`;
    }
    else {
	if ( open LOG, '>>', $logfile ) {
	    print LOG "ERROR:  Can't stat nagios command pipe $nagios_cmd_pipe\n";
	    close LOG;
	}
    }
}

my $remote_host = 'localhost';
my $remote_port = 4913;

# Log in flat file first before Foundation test
if ( my $socket = IO::Socket::INET->new( PeerAddr => $remote_host, PeerPort => $remote_port, Proto => 'tcp', Type => SOCK_STREAM ) ) {
    unless ( $socket->sockopt( SO_SNDTIMEO, pack( 'L!L!', $socket_send_timeout, 0 ) ) ) {
	if ( open LOG, '>>', $logfile ) {
	    print LOG "WARNING:  Couldn't set send timeout on socket ($!).\n";
	    close LOG;
	}
    }

    # massage the message text both for sanitization and for XMLification
    my $tmp = $msg;
    $tmp =~ s/\n/ /g;
    $tmp =~ s/<br>/ /ig;
    $tmp =~ s/&/&amp;/g;
    $tmp =~ s/["']/&quot;/g;    # FIX MINOR:  Bad idea to collapse different types of quotes, should use ' => &apos; instead.
    $tmp =~ s/</&lt;/g;
    $tmp =~ s/>/&gt;/g;

    my $xml_message = "<SNMPTRAP consolidation='SNMPTRAP' ";    # Start message tag.  Consolidation is ON
    ## my $xml_message = "<SNMPTRAP ";	# Start message tag.  Consolidation is OFF
    $xml_message .= "MonitorServerName=\"$thisnagios\" ";                     # Default Identification - should set to IP address if known
    $xml_message .= "Device=\"$ipaddr\" ";                                    # set to IP address
    $xml_message .= "Host=\"$host\" " if $host;                               # Set to Monarch host
    $xml_message .= "Severity=\"$sev\" ";
    $xml_message .= "MonitorStatus=\"$monitorstatus\" ";
    $xml_message .= "ReportDate=\"" . time_text(time) . "\" ";                # set ReportDate to current local time
    $xml_message .= "LastInsertDate=\"" . convert_time($timestamp) . "\" ";
    $xml_message .= "ipaddress=\"$ipaddr\" ";
    $xml_message .= "Event_OID_numeric=\"$oid\" ";
    $xml_message .= "Event_OID_symbolic=\"$oid_sym\" ";
    $xml_message .= "Event_Name=\"$eventname\" ";
    ## GWMON-1200 Fields are not encoded, and can include valid XML chars.
    ## FIX MINOR:  Using HTML::Entities to perform XML encoding is the WRONG approach,
    ## because XML IS NOT HTML.
    $xml_message .= "Category=\"" . encode_entities($category) . "\" ";
    $xml_message .= "Variable_Bindings=\"" . encode_entities($varbinds) . "\" ";
    $xml_message .= "TextMessage=\"$tmp\" ";
    $xml_message .= "/>";                                                          # End message tag
    if ( open LOG, '>>', $logfile ) {
	print LOG $xml_message . "\n";
	close LOG;
    }
    ## The Foundation socket API is expecting Unicode, in UTF-8 format.  So that's how
    ## we have to provide any of the ISO-8859-1 (Latin-1) high-order characters we may
    ## have acquired as input.
    if ( not binmode( $socket, ':utf8' ) ) {
	if ( open LOG, '>>', $logfile ) {
	    print LOG "ERROR:  Cannot set UTF-8 encoding on socket.\n";
	    close LOG;
	}
    }
    print $socket $xml_message;
    CommandClose($socket);
    close($socket);
}
else {
    if ( open LOG, '>>', $logfile ) {
	print LOG "ERROR:  Couldn't connect to $remote_host:$remote_port ($!).\n";
	close LOG;
    }
}

sub convert_time {
    my $timetext = shift;    # Time in format Fri Oct 28 2005 11:18:06
    my %mons     = (
	Jan => '01',
	Feb => '02',
	Mar => '03',
	Apr => '04',
	May => '05',
	Jun => '06',
	Jul => '07',
	Aug => '08',
	Sep => '09',
	Oct => '10',
	Nov => '11',
	Dec => '12'
    );
    if ( $timetext =~ /(\w+)\s+(\w+)\s+(\d+)\s+(\d+)\s+(\d\d:\d\d:\d\d)/ ) {
	my $mon;
	if ( $mons{$2} ) {
	    $mon = $mons{$2};
	}
	else {
	    ## return time_text(time);
	    return "INVALID DATE MONTH: $2";
	}
	my $day  = sprintf '%02d', $3;
	my $year = sprintf '%04d', $4;
	my $time = $5;
	return "$year-$mon-$day $time";
    }
    else {
	## return time_text(time);
	return "INVALID DATE STRING: $timetext ";
    }
}

sub time_text {
    my $timestamp = shift;
    if ( $timestamp <= 0 ) {
	return 'none';
    }
    else {
	my ( $seconds, $minutes, $hours, $day_of_month, $month, $year, $wday, $yday, $isdst ) = localtime($timestamp);
	return sprintf '%04d-%02d-%02d %02d:%02d:%02d', $year + 1900, $month + 1, $day_of_month, $hours, $minutes, $seconds;
    }
}

sub CommandClose {
    my $socket = shift;

    # Create XML stream -- Format:
    #	<SERVICE-MAINTENANCE     command="close" />
    my $xml_message = "<SERVICE-MAINTENANCE command=\"close\" />";

    # print $xml_message."\n\n" if $debug;
    print $socket $xml_message;
    return;
}
