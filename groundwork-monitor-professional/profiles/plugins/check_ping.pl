#!/usr/local/groundwork/perl/bin/perl -w

use strict;
use IO::File ();
use IO::Handle ();
use IO::Select ();
use IPC::Open3 ();
use Symbol ();

use lib q(/usr/local/groundwork/nagios/libexec);
use parse_func ();

use constant PING    => q(/bin/ping);

use constant OPTIONS => { 'h'  => 'hostname or ip address',
                          'c'  => 'packet count',
                          't'  => 'ping timeout (seconds)',
                        };

my $args = parse_func->new(\@ARGV, OPTIONS);

# instantiate variables
my $alive = 0;
my $in  = IO::File->new( '/dev/null', 'r' );
my $out = Symbol::gensym;
my $err = IO::File->new( '/dev/null', 'w' );

# build list of arguments to execute ping
my @cmds = ( PING, '-A', '-c', $args->{c}, '-n', '-w', $args->{t}, $args->{h});

# execute ping command
my $pid = IPC::Open3::open3($in, $out, $err, @cmds) or do {
   printf "CRITICAL - Unable to execute %s", PING;
   exit 2;
};

# wait for stdout
while ( IO::Select->new( $out )->can_read ) {
   if ( defined( my $line = <$out> ) ) {
      chomp $line;
      $line =~ /time=[0-9.]+ ms$/ and ++$alive and $out->close;
   }
   else {
      $out->close;
   }
}

# generate output
if ($alive) {
   printf "OK - %s is alive\n", $args->{h};
   exit 0;
}
else {
   printf "CRITICAL - %s is down\n", $args->{h};
   exit 2;
}
