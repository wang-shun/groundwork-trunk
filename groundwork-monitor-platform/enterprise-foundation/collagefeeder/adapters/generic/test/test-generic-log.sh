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
                                my $xml_message = "<GENERICLOG ApplicationType='LOG4J' MonitorServerName='localhost' Host='dashboard.itgroundwork.com' Device='dashboard.itgroundwork.com' Severity='WARNING' MonitorStatus='WARNING' ErrorType='LogRotation' SubComponent='Log4J Integrator' TextMessage='16:15:54,593 [WARN ] com.groundwork.collage.impl.LogMessageDAOImpl - Consolidation criteria matches with more than one record. Make sure the criteria is better defined. If the consolidation criteria was turned on after identical messages were inserted you have to run consolidate existing messages. Contact support for more information about database maintenance.' />" ;



                print $xml_message."\n\n" if $debug;
                print $socket $xml_message;
                my $xml_message = "<SERVICE-MAINTENANCE command=\"close\" />";
                print $xml_message."\n\n" if $debug;
                print $socket $xml_message;
                
}  else {
        print "Couldn't connect to $remote_host:$remote_port : $@\n";
}
