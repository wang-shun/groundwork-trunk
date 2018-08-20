#!/usr/local/groundwork/perl/bin/perl -w

package cisco_fw_func;

use strict;

# define prompts
use constant USER   => qr/[a-zA-Z0-9-\057]+> $/;
use constant ENABLE => qr/[a-zA-Z0-9-\057]+# $/;
use constant SYSTEM => qr/[a-zA-Z0-9-]*# $/;
use constant MORE   => qr/<--- More --->$/;
use constant PASS   => qr/[Pp]assword: $/;


################################################################################
# new - instantiate new cisco_fw_func object                                   #
#       expects ssh_func object to be passed in                                #
#       returns cisco_fw_func object or undef on failure                       #
################################################################################
sub new {
   my $class = shift;
   my $ssh   = shift;

   # bless this to be an object
   bless(my $self = {}, $class);

   # open a ssh shell (channel 0)
   $self->{shell} = $ssh->shell;
  
   # wait for first user-mode prompt
   my ($preprompt, $prompt) = $self->waitfor( USER, 5 );
   if (!$prompt) {
      $@ = 'Timed out waiting for user-mode prompt after login';
      return undef;
   }

   # successfully entered user mode

   # store prompt and mode
   $self->{user} = $prompt;
   $self->{mode} = q(user);

   # retrieve output from "show version"
   $self->{show_ver} = $self->cmd( 'show version' ) or do {
      $@ = "Failed to retrieve show version output: $@";
      return undef;
   };

   # test for multi-context mode and store for later use
   if ($self->{show_ver} =~ /<context>/) {
      # we are multicontext
      $self->{multicontext} = 1;

      # are we the admin context?
      if ($self->{show_ver} =~ /Serial Number: \S+/) {
         # yes
         $self->{admincontext} = 1;
      }
      else {
         # no
         $self->{admincontext} = 0;
      }

      # retrieve context name from prompt
      ($self->{context}) = $self->{user} =~ m|/(\S+)> $|;
   }
   else {
      # single context
      $self->{multicontext} = 0;
   }

   # return object to caller
   return $self;
}


################################################################################
# enable - change into cisco firewall enable mode from user mode               #
#          expects a enable password to be passed in                           #
#          returns 1 on success and undef on failure                           #
################################################################################
sub enable {
   my $self     = shift;
   my $password = shift;

   # can only switch to enable mode from user mode
   $self->{mode} eq 'user' or do {
      $@ = "Cannot change to enable mode from $self->{mode} mode";
      return undef;
   };

   # send "enable" command
   $self->send( "enable\n" );

   # wait for enable password prompt
   if ($self->waitfor( PASS, 1 )) {
      # send password
      $self->send( "$password\n" );
   }
   else {
     $@ = 'Timed out waiting for enable password prompt';
     return undef;
   }

   # wait for user-mode or enable-mode prompt
   my ($preprompt, $prompt) = $self->waitfor( $self->{user}, ENABLE, 1 );

   if (!$prompt) {
      $@ = 'Timed out waiting for prompt after sending enable-mode password';
      return undef;
   }
   elsif ($prompt =~ $self->{user}) {
      # user prompt; enable mode failed
      $preprompt =~ tr/\r//d;
      my $error = [ split /\n/ => $preprompt ]->[-1];
      $@ = "Enable mode access failed: $error"; 
      return undef;
   }
   else {
      # enable mode successful
      $self->{enable} = $prompt;
      $self->{mode}   = q(enable);
   }

   # success
   return 1;
} 


################################################################################
# changeto_system - change into system context mode from enable mode           #
#                   expects nothing                                            #
#                   returns 1 on success and undef on failure                  #
################################################################################
sub changeto_system {
   my $self = shift;

   # there is only a system context when we are in multicontext mode
   if ($self->{multicontext} == 0) {
      $@ = 'Cannot enter system context on a single-context device';
      return undef;
   }

   # can only switch to system context from enable mode
   $self->{mode} eq 'enable' or do {
      $@ = "Cannot change to system context from $self->{mode} mode";
      return undef;
   };

   # can only switch to system context if we are coming from admin context
   $self->{admincontext} == 1 or do {
      $@ = "Cannot change to system context from $self->{context} context";
      return undef;
   };

   # send "changeto system" command
   $self->send( "changeto system\n" );

   # wait for system-mode prompt
   my ($preprompt, $prompt) = $self->waitfor( $self->{enable}, SYSTEM, 1 );

   if (!$prompt) {
      $@ = 'Timed out waiting for system-context prompt';
      return undef;
   }
   elsif ($prompt =~ $self->{enable}) {
      # enable prompt; system context failed
      $preprompt =~ tr/\r//d;
      my $error = [ split /\n/ => $preprompt ]->[-1];
      $@ = "System context access failed: $error";
      return undef;
   }
   else {
      # system context successful
      $self->{system} = $prompt;
      $self->{mode}   = q(system);
   }

   # success
   return 1;
}


################################################################################
# cmd - execute command in current mode                                        #
#       expects a command and optional timeout (seconds)                       #
#       returns output of command or undef on error                            #
################################################################################
sub cmd {
   my $self = shift;
   my $command = shift;
   my $timeout = shift || 5;
   my $data = ();

   # retrieve current mode prompt
   my $prompt = $self->{ $self->{mode} };

   # send command
   $self->send( "$command\n" );

   # loop through output of command
   for (;;) {
      my ($prematch, $match) = $self->waitfor( $prompt, MORE, $timeout );
      if (!$match) {
         $@ = "Timed out waiting for output from command: $command";
         return undef;
      }
      elsif ($match =~ MORE) {
         # found "more" prompt; send space
         $data .= $prematch;
         $self->send( ' ' );
      }
      elsif ($match =~ $prompt) {
         # found mode prompt; leave loop
         $data .= $prematch;
         last;
      }
   } 

   # remove carriage-returns
   $data =~ tr/\r//d;

   # remove "more" prompts
   $data =~ s/^[ ]{14}//msg;

   # capture any syntax errors
   if ($data =~ /^(?:Type help|ERROR:)/msg) {
      $@ = "Syntax error detected in command: $command";
      return undef;
   }

   # return output from command
   return $data;
}


################################################################################
# waitfor - wait for output from shell                                         #
#           expects one or more regex "prompts" to match                       #
#           optional last argument is timeout in seconds                       #
#           returns prematch and match                                         #
################################################################################
sub waitfor {
   my $self    = shift;
   my $timeout = pop @_;
   my @regex   = @_;
   my $data = '';
   my $prompt = ();

   # dereference ssh shell
   my $shell = $self->{shell};

   # build new IO::Select object
   my $select = IO::Select->new( $shell );

   # wait for data from shell with timeouts
   eval {
      local $SIG{ALRM} = sub { die 'TimedouT' };
      alarm $timeout;
      while ( $select->can_read ) {
         sysread( $shell, $data, 4096, length($data) ) or last;
         ($prompt) = grep { $data =~ /$_/ } @regex and last;
         alarm 1;
      }
      alarm 0;
   }; 

   # trap eval errors
   if ($@) {
      $@ =~ /TimedouT/ ?
      $@ = 'Timed out during waitfor' :
      $@ = "Eval error: $@";
      return undef;
   }

   # return success if caller is expecting a scalar
   !wantarray and return 1;

   # separate output into prematch (output) and match (prompt)
   my ($prematch, $match) = $data =~ /^(.*?)($prompt)/msg;

   # return prematch and match
   return $prematch, $match;
}


################################################################################
# send - send something to the shell                                           #
#        expects a string                                                      #
#        returns nothing                                                       #
################################################################################
sub send {
   my $self = shift;
   my $cmd  = shift;
   my $shell = $self->{shell};

   # send command to remote
   syswrite( $shell, $cmd );

   # extract echoed command
   read( $shell, my $null, length($cmd) );
}
    

################################################################################
# exit - attempt to send exit command to remote device                         #
#        expects nothing                                                       #
#        returns nothing                                                       #
################################################################################
sub exit {
   my $self = shift;
   $self->send( "exit\n" );
}


1; 
