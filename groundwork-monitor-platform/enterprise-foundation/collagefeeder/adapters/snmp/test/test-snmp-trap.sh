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
use IO::Socket;
my $debug =1 ;
my $remote_host = "localhost";
my $remote_port = 4913;
my $socket;
if ( $socket = IO::Socket::INET->new(PeerAddr => $remote_host,
                                                        PeerPort => $remote_port,
                                                        Proto    => "tcp",
                                                   Type     => SOCK_STREAM)
) {
                my $xml_message = "<SNMPTRAP consolidation='SNMPTRAP' MonitorServerName='localhost' Device='172.28.113.201' Host='MyHost2' Severity='Normal' MonitorStatus='UNKNOWN' ReportDate='2005-10-25 04:18:44' LastInsertDate='2005-10-25 04:18:44' ipaddress='192.168.2.203' Event_OID_numeric='.1.3.6.1.4.1.9.0.1' Event_OID_symbolic='enterprises.9.0.1' Event_Name='tcpConnectionClose' Category='Status Events' Variable_Bindings='enterprises.9.2.9.3.1.1.1.1:5 ' TextMessage='A tty trap signifies that a TCP connection, previously established with the sending protocol entity for the purposes of a tty session, has been terminated.' />" ;



                print $xml_message."\n\n" if $debug;
                print $socket $xml_message;
}  else {
        print "Couldn't connect to $remote_host:$remote_port : $@\n";
}
