#!/usr/local/groundwork/perl/bin/perl -w

use strict;
use lib q(/usr/local/groundwork/nagios/libexec);
use host_func ();
use parse_func ();
use ssh_func ();

use constant PLUGIN => 'check_splat';

use constant FUNCTIONS => { 'nat' => \&nat };

use constant OPTIONS => { 'h'  => 'Hostname',
                          'l?' => 'Levels [warning:critical]',
                          't'  => { 'type of check' => FUNCTIONS },
                        };

my $args = parse_func->new(\@ARGV, OPTIONS);
my $host = host_func->new( $args->{h} ) or do {
   print "UNKNOWN - Host '$args->{h}' not found";
   exit 3;
};
my $ssh = ssh_func->new( hostname => $args->{h},
                         username => $host->get( 'backup_user' ),
                         password => $host->decrypt( 'backup_pass' ),
                         port     => $host->{access_port},
                       ) or do {
   print "ERROR - $@";
   exit 3;
};
defined(FUNCTIONS->{ $args->{t} }) ? FUNCTIONS->{ $args->{t} }() :
   $ssh->die3("Unknown check type: $args->{t}");
exit 0;


################################################################################
# cpu - check fw-1 nat table utilization                                       #
################################################################################
sub nat {
   # workaround for bombay (capone)
   $host->{name} =~ /bombay/ and $ssh->{forcepty} = 1;
  
   # set warning/critical thresholds
   my ($warn, $crit) = grep /^\d+$/ => split /:/ => $args->{l};
   $warn ||= 80;    # default 80%
   $crit ||= 90;    # default 90%

   # test for fwsuid
   my ($a, $b, $c) = $ssh->cmd( '[ -f ~/fwsuid ]' );
   my $wrapper = $c == 0 ? '~/fwsuid' : '';
  
   # retrieve nat connection limit 
   my $fwx_maxconns = $ssh->cmd( 
     "source /etc/profile; $wrapper fw ctl get int fwx_max_conns 2>/dev/null" );

   # retrieve nat current connections
   my $fwx_alloc = $ssh->cmd( 
      "source /etc/profile; $wrapper fw tab -t fwx_alloc -s 2>/dev/null" );

   # only proceed if both were successful
   if ($fwx_maxconns && defined $fwx_alloc) {
      # parse out numeric values from output
      my ($limit) = $fwx_maxconns =~ /^fwx_max_conns = (\d+)$/;
      my ($conns) = $fwx_alloc =~ /^localhost\s+fwx_alloc\s+\d+\s+(\d+)/msg;
  
      # define perfdata
      my $perfdata = "conns=$conns";

      # calculate a percentage of nat connectinos utilized
      my $used = sprintf "%d", 100 * $conns / $limit;

      # test against thresholds and generate output
      if ($used >= $crit) {
         print "CRITICAL - NAT table at $used% utilization [$conns/$limit] " .
               "(threshold $crit%)|$perfdata";
         exit 2;
      }
      elsif ($used >= $warn) {
         print "WARNING - NAT table at $used% utilization [$conns/$limit] ".
               "(threshold $warn%)|$perfdata";
         exit 1;
      }
      else {
         print "OK - NAT table at $used% utilization [$conns/$limit]|$perfdata";
      } 
   }
   else {
      print "UNKNOWN - Unable to retrieve NAT entries";
      exit 3;
   }
}
