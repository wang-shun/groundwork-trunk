#!/usr/local/groundwork/perl/bin/perl -w

package curl_func;

use strict;
use IO::Handle ();
use IO::Select ();
use IPC::Open3 ();
use Symbol ();

use constant CURL    => '/usr/bin/curl';


sub new {
   my $class = shift;
   my $self = { @_ };
   bless( $self, $class );
   return $self;
}
   

sub curl {
   my ($self, $url, $stdin) = @_;

   # instantiate variables
   my $stdout = my $stderr = '';
   my $in  = Symbol::gensym;
   my $out = Symbol::gensym;
   my $err = Symbol::gensym;

   # execute curl binary
   my $pid = IPC::Open3::open3( $in, $out, $err, CURL, grep { defined } %$self, 
                                $url ) or do {
      $@ = "Couldn't exec @{[ CURL ]}";
      return ();
   };

   # send stdin to curl
   defined $stdin and $in->syswrite( $stdin );
   $in->close;

   # capture output from curl 
   my $select = IO::Select->new( $out, $err );
   while (my @ready = $select->can_read) {
      foreach my $fh (@ready) {
         if ($fh == $out) {
            $fh->sysread($stdout, 4096, length($stdout)) or $fh->close;
         }
         elsif ($fh == $err) {
            $fh->sysread($stderr, 4096, length($stderr)) or $fh->close;
         }
      }
   }

   # wait for curl to finish
   waitpid( $pid, 0 );

   # retrieve exit code from curl
   my $retval = $? >> 8;

   # return output from curl or error
   if ($retval) {
      chomp( $@ = "curl_func: $stderr" );
      return undef;
   }
   else {
      return $stdout;
   }
} 


return 1;
