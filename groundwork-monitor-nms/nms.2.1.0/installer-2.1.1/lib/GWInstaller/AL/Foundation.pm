#!/usr/bin/perl
#
#Copyright 2008 GroundWork Open Source, Inc. ("GroundWork")
#All rights reserved. Use is subject to GroundWork commercial license terms. 
#

package GWInstaller::AL::Foundation;
@ISA = qw(GWInstaller::AL::Software);

use Socket;

# 
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
	
	#set default port
	$self->set_port(4913);
	
}

sub save_values{
	my $self = shift;
	#UI Collection
	my $UICollection = $self->{UICollection};
	
	#port
	my $dbObj = $UICollection->next();
	$self->set_port($dbObj->get());
 
		
 }

# Check whether Foundation is functioning properly
# Arguments passed: None
# Returns: None

sub is_functional{
    shift; # Remove class name argument array

    ## Check if port is listening

    my $timeout = 1;
    my $hostName = 'localhost';
    my $portNumber = 4913;
    my $protocol = getprotobyname('tcp');
    my $iaddr = inet_aton($hostName);
    my $paddr = sockaddr_in($portNumber, $iaddr);

    # Create a socket
    socket(SOCKET, PF_INET, SOCK_STREAM, $protocol) || die "socket: $!";

    eval{
        local $SIG{ALRM} = sub { die "timeout" };
        alarm($timeout);
        connect(SOCKET, $paddr) || error();   # Connect using socket
        alarm(0);
    };

    if ($@) {
        close SOCKET || die "close: $!";
        $foundationOn = 0;
    }
    else {
        close SOCKET || die "close: $!";
        $foundationOn = 1;
    }

    ## Check if process is running

    my $scriptName = "start-foundation.sh";
    # Execute a command to check if process is running
    my $cmd = "ps -ef | grep " . $scriptName ."| grep -v grep";
    my @lines = `$cmd`;

    if (@lines && ($foundationOn == 1))
    {
        return 1;
    }else {
        return 0;
    }

}
1;
