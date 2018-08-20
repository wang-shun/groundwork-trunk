#!/usr/local/groundwork/perl/bin/perl -w

package linux_func;

use strict;
use POSIX ();
use Exporter;

use constant TMPCHARS => ( 48..57, 65..90, 97..122 );
use constant TMPDIR   => '/tmp/';

our @ISA = qw(Exporter);
our @EXPORT = qw();


sub tempfile {
   my $suffix = shift;
   my $random = TMPDIR;
   for (1 .. 8) {
      $random .=  chr( (TMPCHARS)[ int(rand(scalar(TMPCHARS))) ] );
   }
   return $suffix ? $random . $suffix : $random;
}


sub change_process_name {
   my $name = shift;
   my $SYS_prctl = 172;   # x86=172   x86_64=157
   my $SYS_PR_SET_NAME = 15;   # set
   syscall($SYS_prctl, $SYS_PR_SET_NAME, $name, 0, 0, 0);
   $0 = $name;
}


sub daemonize {
   my $user = shift;
   defined(my $pid = fork) or die "Can't fork: $!";
   exit if $pid;   # parent
   POSIX::setsid or die "Can't setsid: $!";
   if ( defined $user && $< == 0 ) {
      scalar POSIX::getpwnam($user) or die "Setuid user not found: $!";
      POSIX::setgid( (getpwnam($user))[3] ) or die "Can't setgid: $!";
      POSIX::setuid( (getpwnam($user))[2] ) or die "Can't setuid: $!";
   }
   open STDIN, '< /dev/null' or die "Can't redirect STDIN: $!";
   open STDOUT, '> /dev/null' or die "Can't redirect STDOUT: $!";
   open STDERR, '> /dev/null' or die "Can't redirect STDERR: $!";
}


return 1;
