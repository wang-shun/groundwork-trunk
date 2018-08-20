#!/usr/bin/perl --
#
#	Copyright (C) 2008 GroundWork Open Source, Inc. ("GroundWork")
#	All rights reserved. Use is subject to GroundWork commercial license terms.
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