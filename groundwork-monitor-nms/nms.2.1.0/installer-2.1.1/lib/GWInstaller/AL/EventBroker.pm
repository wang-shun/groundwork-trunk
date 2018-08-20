#!/usr/bin/perl
#
#Copyright 2008 GroundWork Open Source, Inc. ("GroundWork")
#All rights reserved. Use is subject to GroundWork commercial license terms. 
#

package GWInstaller::AL::EventBroker;
@ISA = qw(GWInstaller::AL::Software);

use Socket;

sub new{
   my ($invocant) = @_;
   my $class = ref($invocant) || $invocant;
   my $self =  {};

   bless($self,$class);
   $self->init();
   return $self;
}

sub init{
	$self = shift;
	$self->set_port(5667);
}
# Check whether Event Broker is functioning properly
# Arguments passed: Hostname
# Returns: 1 on success and 0 on failure

sub is_functional{
    shift;  # Ignore class name
    my $timeout = 1;
    my ($hostName) = @_;
    my $portNumber = 5667;
    my $protocol = getprotobyname('tcp');
    my $iaddr = inet_aton($hostName);
    my $paddr = sockaddr_in($portNumber, $iaddr);

    # Create a socket
    socket(SOCKET, PF_INET, SOCK_STREAM, $protocol) || die "socket: $!";

    eval {
        local $SIG{ALRM} = sub { die "timeout" };
        alarm($timeout);
        connect(SOCKET, $paddr) || error();  # Test connection
        alarm(0);
    };
    close SOCKET || die "close: $!";

    if ($@) {
        return 0;
    }
    else {
        return 1;
    }
}
1;
