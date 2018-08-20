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
                my $xml_message = "<SYSLOG MonitorServerName='localhost' Host='exchange.itgroundwork.com' Device='exchange.itgroundwork.com' Severity='Normal' MonitorStatus='Normal' ReportDate='2005-10-25 04:18:44' LastInsertDate='2005-10-25 04:18:44' ipaddress='192.168.2.203' ErrorType='LogRotation' SubComponent='FileServer' TextMessage='Syslog text message' />" ;
                
                
                
                print $xml_message."\n\n" if $debug;
                print $socket $xml_message;
}  else {
        print "Couldn't connect to $remote_host:$remote_port : $@\n";
}