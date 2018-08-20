#!/usr/local/groundwork/perl/bin/perl -w

package ssh_func;

use strict;
use IO::File ();
use IO::Select ();
use IPC::Open3 ();
use Symbol ();

# IO::Pty not included with Groundwork, using local perl version
use lib '/usr/lib/perl5/vendor_perl/5.8.5/i386-linux-thread-multi';
use lib '/usr/lib/perl5/vendor_perl/5.8.8/i386-linux-thread-multi';
use IO::Pty ();

# catch signals (ctrl-c and kill) and shutdown gracefully
# this is needed for proper cleanup; see sub DESTROY
$SIG{INT} = $SIG{TERM} = sub { exit 255 };

use constant CONNECT_TIMEOUT => 5;
use constant LOGIN_TIMEOUT   => 20;


sub new {
   my $class = shift;
   my $self = { @_ };
   $self->{ssh} = [];
   $self->{hostname} or $class->die2('hostname not defined');
   $self->{username} or $class->die2('username not defined');
   $self->{password} || $self->{sshkeys} or $class->die2('no password or ssh keys defined');
   $self->{password} || $self->{sshkeys} && ref($self->{sshkeys}) eq 'ARRAY' or $class->die2('ssh keys not array pointer');
   $self->{port} ||= 22;
   $self->{port} =~ /^[0-9]+$/ or $self->{port} = 22;
   $self->{port} >= 1 && $self->{port} <= 65535 or $class->die2('ssh port invalid');
   $self->{version} ||= 2;
   $self->{version} && $self->{version} >= 1 && $self->{version} <= 2 or $class->die2('ssh version invalid');

   my $rand = get_rand(8);
   $self->{controlfile} = "/dev/shm/ssh_func_$rand.control";
   $self->{configfile} = "/dev/shm/ssh_func_$rand.config";

   $self->{config} = { AddressFamily           => 'inet',
                       CheckHostIP             => 'no',
                       ConnectTimeout          => CONNECT_TIMEOUT,
                       ControlPath             => $self->{controlfile},
                       GlobalKnownHostsFile    => '/dev/null',
                       NumberOfPasswordPrompts => 1,
                       Port                    => $self->{port},
                       Protocol                => $self->{version},
                       StrictHostKeyChecking   => 'no',
                       UserKnownHostsFile      => '/dev/null',
                     };

   if ( $self->{sshkeys} ) {
      push @{ $self->{config}->{IdentityFile} }, $_ foreach @{$self->{sshkeys}};
      $self->{config}->{BatchMode} = 'yes',
      $self->{config}->{ChallengeResponseAuthentication} = 'no',
      $self->{config}->{PasswordAuthentication} = 'no';
      $self->{config}->{PreferredAuthentications} = 'publickey';
      $self->{config}->{PubkeyAuthentication} = 'yes';
   }
   else {
      $self->{config}->{BatchMode} = 'no';
      $self->{config}->{ChallengeResponseAuthentication} = 'yes';
      $self->{config}->{PasswordAuthentication} = 'yes';
      $self->{config}->{PreferredAuthentications} = 'password,keyboard-interactive';
      $self->{config}->{PubkeyAuthentication} = 'no';
   }

   bless $self, $class;

   $self->write_config;

   eval {
#      local $SIG{CHLD} = sub { die 1 };
      my $sshargs = "-F $self->{configfile} -M -N -l $self->{username} $self->{hostname}";
      my ($ssh, $err, $pid) = $class->fork_exec("/usr/bin/ssh $sshargs");
      push @{$self->{ssh}}, [ $ssh, $err, $pid ];

      local $SIG{ALRM} = sub { die 2 };
      alarm LOGIN_TIMEOUT;
      my $buffer = '';

      if ($self->{password}) {
         while (my $bytes = sysread( $ssh, $buffer, 1460, length($buffer) ) ) {
            if (! defined $bytes) {
               die 3;
            }
            elsif ($bytes == 0) {
               die 4;
            }
            elsif ($buffer =~ /[Uu]sername:[ ]?$/) {
               syswrite( $ssh, "$self->{username}\n" );
               $buffer = '';
               alarm 5;
            }
            elsif ($buffer =~ /login:[ ]?$/) {
               syswrite( $ssh, "$self->{username}\n" );
               $buffer = '';
               alarm 5;
            }
            elsif ($buffer =~ /[Pp]assword:[ ]?$/) {
               syswrite( $ssh, "$self->{password}\n" );
               $buffer = '';
               alarm 6;
               last;
            }
         }
         sysread( $ssh, my $null, 2 ) or die 1;
      }
      if ( $self->{version} == 2 ) {
          for (my $i=1; $i<=50; $i++) {
             waitpid( $pid, 1 ) and die 1;
             -e $self->{controlfile} and last;
             select(undef,undef,undef,0.1);
             $i == 50 and die 1;
          }
      }
      elsif ( $self->{version} == 1 ) {
         IO::Select->new( $ssh )->can_read(5) or die 1;
      }
      alarm 0;
   };

   if ($@) {
      if ($@ =~ /^1/) {
         my $msg = '';
         my $err = $self->{ssh}->[0]->[1];
         IO::Select->new($err)->can_read(0) and sysread( $err, $msg, 4096 );
         chomp( $@ = "ssh exited: $msg" );
         return undef;
      }
      elsif ($@ =~ /^2/) {
         $@ = 'login timeout';
         return undef;
      }
      elsif ($@ =~ /^3/) {
         $@ = 'login error during read';
         return undef;
      }
      elsif ($@ =~ /^4/) {
         $@ = 'login eof encountered';
         return undef;
      }
      elsif ($@ =~ /^5/) {
         $@ = 'timeout waiting for shell';
         return undef;
      }
      else {
         $@ = "eval error $@";
         return undef;
      }
   }

   return $self;
}


sub shell {
   my $self = shift;
   my $ssh = my $err = my $pid = ();
   if ($self->{version} == 1) {
      ($ssh, $err, $pid) = @{$self->{ssh}->[0]};
   }
   elsif ($self->{version} == 2) {
      my $sshargs = "-F $self->{configfile} -l $self->{username} $self->{hostname}";
      ($ssh, $err, $pid) = $self->fork_exec("/usr/bin/ssh $sshargs");
      push @{$self->{ssh}}, [ $ssh, $err, $pid ];
   }
   return wantarray ? ($ssh, $err, $pid) : $ssh;
}


sub cmd {
   my ($self, $cmd, $stdin) = @_;
   my $stdout = my $stderr = my $retval = '';

   if ( my $r = $self->check_ctl ) {
      $stderr = $@;
      $retval = $r;
   }
   else {
      $self->{config}->{BatchMode} = 'yes';
      $self->write_config;

      my @forcepty = $self->{forcepty} ? ('-tt') : ();

      my $in  = Symbol::gensym;
      my $out = Symbol::gensym;
      my $err = Symbol::gensym;
      my $pid = IPC::Open3::open3($in, $out, $err, '/usr/bin/ssh', @forcepty,
                                                   '-F', $self->{configfile}, 
                                                   '-l', $self->{username}, 
                                                   $self->{hostname}, $cmd);
      defined $stdin and $in->print( $stdin ) and $in->close;
      my $select = IO::Select->new( $out, $err );
      while (my @ready = $select->can_read) {
         foreach my $fh (@ready) {
            if ($fh == $out) {
               $fh->sysread( $stdout, 4096, length($stdout) ) or $fh->close;
            }
            elsif ($fh == $err) {
               $fh->sysread( $stderr, 4096, length($stderr) ) or $fh->close;
            }
         }
      }            

      waitpid( $pid, 0 );
      $retval = $? >> 8;
   }

   if ( wantarray) {
      return $stdout, $stderr, $retval;
   }
   elsif ( $retval ) {
      length($stdout) and $@ = $stdout;
      length($stderr) and $@ = $stderr;
      return undef;
   }
   else {
      return $stdout;
   }
}


# currently only supporting scp get
sub scp {
   my ($self, $file) = @_;
   my $results = $self->cmd( "scp -f $file", "\000\000\000" ) or do {
      $@ = "scp: $@";
      return undef;
   };
   my ($status, $data) = split( /\n/, $results, 2 );
   return $data;
}


sub get_rand {
   my $len = shift;
   my @char = ( 48..57, 65..90, 97..122 );
   return join '', map { chr( $char[ int(rand(scalar(@char))) ] ) } 1 .. $len;
}


sub fork_exec {
   my ($self, $cmd) = @_;
   my $pty = IO::Pty->new or die "Can't make Pty: $!";
   pipe(my $parent, my $child);
   if (!defined(my $pid = fork)) {
     die "Can't fork: $!";
   }
   elsif ($pid) {
      close $child;
      $pty->close_slave;
      return $pty, $parent, $pid;
   }
   else {
      POSIX::setsid();
      close $parent;
      my $tty = $pty->slave;
      $pty->make_slave_controlling_terminal();
      close $pty;
      STDIN->fdopen($tty, "<")  or die "STDIN: $!";
      STDOUT->fdopen($tty, ">") or die "STDOUT: $!";
      STDERR->fdopen($child, ">") or die "STDERR: $!";
      close $tty;
      $|=1;
      exec $cmd;
      die "Couldn't exec: $!";
   }
}


sub write_config {
   my $self = shift;
   my $fh = IO::File->new( $self->{configfile}, 'w' );
   foreach my $key (sort keys %{ $self->{config} }) {
      my $value = $self->{config}->{$key};
      if (ref $value eq 'ARRAY') {
         $fh->print( "$key=$_\n" ) foreach @$value;
      }
      else {
         $fh->print( "$key=$value\n" ); 
      }
   }
   $fh->close;
}


sub check_ctl {
   my $self = shift;
   my $stderr = '';
   my $in  = Symbol::gensym;
   my $out = Symbol::gensym;
   my $err = Symbol::gensym;
   my $pid = IPC::Open3::open3($in, $out, $err, '/usr/bin/ssh', '-F',
                                                $self->{configfile}, 
                                                '-O', 'check', '-l', 
                                                $self->{username},
                                                $self->{hostname});
   while (my ($fh) = IO::Select->new( $err )->can_read) {
      $fh->sysread( $stderr, 4096, length($stderr)) or $fh->close;
   }
   waitpid($pid, 0);
   my $retval = $? >> 8;
   $stderr =~ tr/\r//d;
   $retval and chomp($@ = $stderr);
   return $retval;
}
         

sub die2 {
   my ($self, $msg) = @_;
   print "ssh_func: $msg\n";
   exit 2;
}      


sub die3 {
   my ($self, $msg) = @_;
   print "ssh_func: $msg\n";
   exit 3;
}


sub DESTROY {
   my $self = shift;
   foreach my $arr (reverse @{$self->{ssh}}) {
      my ($ssh, $err, $pid) = @$arr;
      defined $ssh and $ssh->close;
      defined $err and $err->close;
      kill 15, $pid;
      local $?;
      waitpid( $pid, 0 );
   }
   unlink $self->{configfile};
   unlink $self->{controlfile};
}


1;
