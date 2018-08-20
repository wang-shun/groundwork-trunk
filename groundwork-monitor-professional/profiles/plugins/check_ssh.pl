#!/usr/local/groundwork/perl/bin/perl -w

use strict;
use IO::Socket::INET ();
use IO::Select ();
use POSIX 'EAGAIN';
use Socket ();

use lib q(/usr/local/groundwork/nagios/libexec);
use parse_func ();

use constant PLUGIN  => 'check_ssh';

use constant OPTIONS => { 'h'  => 'host [name|ip]',
                          'p?' => 'ssh port [default=22]',
                          't?' => 'timeout in seconds [default=5]',
                        };

my $args = parse_func->new(\@ARGV, OPTIONS);

# set default port if necessary
$args->{p} and $args->{p} eq 'SSHPORT' and $args->{p} = 22;

# create a socket
my $socket = IO::Socket::INET->new( Proto    => 'tcp',
                                    PeerHost => $args->{h},
                                    PeerPort => $args->{p} ||= 22,
                                  ) or do {
   print "CRITICAL - Unable to connect to $args->{h}:$args->{p}";
   exit 2;
};

# set socket receive timeout to TIMEOUT seconds
setsockopt($socket, Socket::SOL_SOCKET, Socket::SO_RCVTIMEO, 
   pack("LL", $args->{t} ||= 5, 0));

# attempt to read in ssh version line from socket
my $sshver = <$socket>;

# check for timeout error
if ($! == EAGAIN) {
   print "CRITICAL - Timed out waiting for SSH to respond";
   exit 2;
}

# generate output
chomp $sshver;
print "OK - $sshver";
exit 0;
