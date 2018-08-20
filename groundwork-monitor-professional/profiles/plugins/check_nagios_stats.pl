#!/usr/local/groundwork/perl/bin/perl -w

use strict;
use IO::Handle ();
use IO::File ();
use IO::Select ();
use IPC::Open3 ();
use Symbol ();

use lib q(/usr/local/groundwork/nagios/libexec);
use parse_func ();

use constant PLUGIN => 'check_nagios_stats';

use constant NAGIOSTATS => '/usr/local/groundwork/nagios/bin/nagiostats';
use constant EXECTIME   => 'AVGACTHSTEXT,AVGACTSVCEXT';
use constant LATENCY    => 'AVGACTHSTLAT,AVGACTSVCLAT';
use constant CMDBUFFERS => 'USEDCMDBUF,HIGHCMDBUF,TOTCMDBUF';

use constant FUNCTIONS => { cmdbuffers => \&command_buffers, 
                            exectime   => \&exectime,
                            latency    => \&latency,
                          };

use constant OPTIONS => { #'l?' => 'levels [warning:critical]',
                          't'  => { 'type of check' => FUNCTIONS },
                        };

my $args = parse_func->new(\@ARGV, OPTIONS);
defined(FUNCTIONS->{ $args->{t} }) ? FUNCTIONS->{ $args->{t} }() : do {
   print "UNKNOWN - Unknown check type: $args->{t}";
   exit 3;
};
exit 0;


################################################################################
# nagiostats - execute nagiostats binary with passed arguments                 #
################################################################################
sub nagiostats {
   my @opts = ( qw(-m -d), @_ );
  
   # instantiate variables
   my $stdout = my $stderr = '';
   my $null = IO::File->new( '/dev/null', 'r' );
   my $out  = Symbol::gensym;
   my $err  = Symbol::gensym;

   # run nagiostats with passed arguments
   my $pid = IPC::Open3::open3( $null, $out, $err, NAGIOSTATS, @opts );
   my $select = IO::Select->new( $out, $err );
   while (my @ready = $select->can_read) {
      foreach my $fh (@ready) {
         if ($fh == $out) {
            $fh->sysread( $stdout, 4096, length($stdout)) or $fh->close;
         }
         elsif ($fh == $err) {
            $fh->sysread( $stderr, 4096, length($stderr)) or $fh->close;
         }
      }
   }
   waitpid( $pid, 0 );
   my $retval = $? >> 8;

   # return results
   if ($retval) {
      $@ = $stderr;
      return undef;
   }
   else {
      return $stdout;
   }

}


################################################################################
# exectime - retrieve host/service check execution time                        #
################################################################################
sub exectime {
   my $exectime = nagiostats( EXECTIME );
   my ($host, $service) = split /\n/ => $exectime;
   $host    /= 1000;
   $service /= 1000;
   my $perfdata = "host=$host service=$service";
   print "OK - Host/Service exectime retrieved [$perfdata]|$perfdata";
}


################################################################################
# latency - retrieve host/service check latency                                #
################################################################################
sub latency {
   my $latency = nagiostats( LATENCY );
   my ($host, $service) = split /\n/ => $latency;
   $host    /= 1000;
   $service /= 1000;
   my $perfdata = "host=$host service=$service";
   print "OK - Host/Service latency retrieved [$perfdata]|$perfdata";
}


################################################################################
# command_buffers - retrieve current/high/limit command buffer usage           #
################################################################################
sub command_buffers {
   my $cmdbuffers = nagiostats( CMDBUFFERS );
   my ($cur, $high, $limit) = split /\n/ => $cmdbuffers;
   my $perfdata = "current=$cur high=$high limit=$limit";
   print "OK - Command buffers retreived [$perfdata]|$perfdata";
}
