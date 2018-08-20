#!/usr/local/groundwork/perl/bin/perl -w

use strict;
use lib q(/usr/local/groundwork/nagios/libexec);
use cache_func ();
use nagios_func ();
use parse_func ();
use snmp_func ();

use constant PLUGIN => 'check_cisco_ace';

use constant OID => { 'cpu5min'     => '.1.3.6.1.4.1.9.9.109.1.1.1.1.8.1',
                      'connsyn'     => '.1.3.6.1.4.1.9.9.161.1.1.1.1.7.1',
                      'conntcp'     => '.1.3.6.1.4.1.9.9.161.1.1.1.1.9.1',
                      'hwmodel'     => '.1.3.6.1.2.1.47.1.1.1.1.13.1',
                      'hwver'       => '.1.3.6.1.2.1.47.1.1.1.1.8.1',
                      'peerstatus'  => '.1.3.6.1.4.1.9.9.650.1.1.2.1.1.1.1',
                      'redunstate'  => '.1.3.6.1.4.1.9.9.650.1.1.4.1.2.1',
                      'swver'       => '.1.3.6.1.2.1.47.1.1.1.1.10.1',
                    };

use constant FUNCTIONS => { 'conns'        => \&connections,
                            'cpu'          => \&cpu,
                            'failover'     => \&failover,
                            'int_list'     => \&interface,
                            'sensors'      => \&sensors,
                            'uptime'       => \&uptime,
                            'version'      => \&version,
                          };

use constant OPTIONS => { 'c'  => 'Community string',
                          'h'  => 'Hostname',
                          'i'  => 'IP address',
                          'l?' => 'Levels [warning:critical]',
                          't'  => { 'Type of check' => FUNCTIONS },
                          'v'  => 'SNMP version [1 or 2c]',
                        };

my $args = parse_func->new(\@ARGV, OPTIONS);
my $snmp = snmp_func->new( host      => $args->{i},
                           version   => $args->{v},
                           community => $args->{c},
                           callback  => \&callback_check_snmp,
                         );
defined(FUNCTIONS->{ $args->{t} }) ? FUNCTIONS->{ $args->{t} }() :
   $snmp->die3("Unknown check type: $args->{t}");
exit 0;


################################################################################
# connections - collect number of half-open (SYN) and full TCP connections     #
################################################################################
sub connections {
   # disable snmp error checking
   $snmp->{ec} = 0;
  
   # retrieve count of half-open (syn) and full (tcp) connections
   my ($syn, $tcp) = $snmp->snmpget( OID->{connsyn}, OID->{conntcp} );

   # generate output
   if ($syn && $tcp) { 
      print "OK - Connection statistics collected|syn=$syn tcp=$tcp\n";
      print "SYN: $syn\n";
      print "TCP: $tcp\n";
   }
   else {
      print 'OK - Connection statistics not supported';
   }
}
   

################################################################################
# cpu - check cpu utilization
################################################################################
sub cpu {
   # set warning/critical thresholds
   my ($warn, $crit) = grep /^\d+$/ => split /:/ => $args->{l};
   $warn ||= 85;    # default 85%
   $crit ||= 95;    # default 95%

   # retrieve 5 minute cpu usage in percentage
   my $cpu = $snmp->snmpget( OID->{cpu5min} );

   # test against thresholds and generate output
   if ($cpu >= $crit) {
      print "CRITICAL - CPU usage at $cpu% (threshold $crit%)|percent=$cpu";
      exit 2;
   }
   elsif ($cpu >= $warn) {
      print "WARNING - CPU usage at $cpu% (threshold $warn%)|percent=$cpu";
      exit 1;
   }
   else {
      print "OK - CPU usage at $cpu%|percent=$cpu";
   }
}


################################################################################
# failover - check for device failover
################################################################################
sub failover {
   # instantiate variables
   my @output = ();
   my $peer = { 1  => 'initializing',
                2  => 'local IP missing',
                3  => 'peer IP missing',
                4  => 'starting heartbeat',
                5  => 'connecting to peer',
                6  => 'software version check',
                7  => 'license check',
                8  => 'operational',
                9  => 'peer HA interface down',
                10 => 'peer device down',
                11 => 'error with peer',
              };
   my $redun = { 1  => 'other',
                 2  => 'redundancy disabled',
                 3  => 'initializing',
                 4  => 'negotiation',
                 5  => 'active',
                 6  => 'cold standby',
                 7  => 'config standby',
                 8  => 'bulk standby',
                 9  => 'hot standby',
                 10 => 'warm standby',
               };

   # check peer status
   #my $peerstatus = $snmp->snmpget( OID->{peerstatus} );
   #if ($peerstatus == 8) {
   #   push @output, "OK - Peer HA status is $peer->{ $peerstatus }";
   #}
   #else {
   #   push @output, "CRITICAL - Peer HA status is $peer->{ $peerstatus }";
   #}

   # retrieve reudundancy state
   my @redunstate = $snmp->snmpwalk( OID->{redunstate} );

   # retrieve/set cached redundancy state
   my $cache = cache_func->new( $args->{h} );
   my $redundancy = $cache->get( 'redundancy' );
   $cache->set( 'redundancy', \@redunstate );

   # test each redundancy group for state changes
   foreach my $i (0 .. $#redunstate) {
      if (defined $redundancy->[$i] && $redundancy->[$i] != $redunstate[$i]) {
         push @output, "CRITICAL - Redundancy group #@{[$i+1]} changed state " .
                       "from $redun->{ $redundancy->[$i] } to " .
                       "$redun->{ $redunstate[$i] }";
      }
      else {
         push @output, "OK - Redundancy group #@{[$i+1]} in state " .
                       "$redun->{ $redunstate[$i] }";
      }
   }

   # generate output
   my @sorted = sort nagios_func::nagsort @output;
   if (grep /CRITICAL/ => @sorted) {
      print join "\n" => @sorted;
      exit 2;
   }
   elsif (grep /WARNING/ => @sorted) {
      print join "\n" => @sorted;
      exit 1;
   }
   else {
      my $ok = scalar @sorted;
      print "OK - $ok failover checks healthy\n";
      print join "\n" => @sorted;
   }
}


################################################################################
# interface - retrieves interface statistics for each interface                #
################################################################################
sub interface {
   # instantiate nagios object for submitting passive check results
   my $nagios = nagios_func->new( $args );

   # retrieve statistics for all interfaces
   my $interface = $snmp->snmp_interface;

   # loop through each interface and submit passive results
   my @int_list = ();
   foreach my $i (sort { $a <=> $b } keys %$interface) {
      my $int = $interface->{$i};
      grep { $int->{int_type} ==  $_ } (6, 136) or next;
      #$int->{int_name} =~ s/^ //;
      $int->{int_name} =~ tr|/|-|;
      $nagios->interface_status_passive( $int );
      next unless ( ($int->{int_in_oct} && $int->{int_in_oct} ne 'U') || 
                    ($int->{int_out_oct} && $int->{int_out_oct} ne 'U') );
      push @int_list, "${i}:$int->{int_name}";
      $nagios->interface_stats_passive( $int );
      $nagios->interface_problems_passive( $int );
   }
   print "OK - Interfaces with traffic counters: @int_list";
}



################################################################################
# sensors - check hardware sensors
################################################################################
sub sensors {
   # stub for sensor checking using ENTITY-MIB / ENTITY-SENSOR-MIB
}


################################################################################
# uptime - check system uptime using HOST-RESOURCE-MIB                         #
################################################################################
sub uptime {
   return $snmp->snmp_uptime( $args );
}


################################################################################
# version - check system platform and version                                  #
################################################################################
sub version {
   # version checking only supported on admin context

   # disable error checking
   $snmp->{ec} = 0;

   # retrieve model and hardware/software versions   
   my ($hwver, $swver, $model) = $snmp->snmpget( OID->{hwver}, OID->{swver},
                                                 OID->{hwmodel} );

   # generate output
   if ($hwver && $swver && $model) {
      print "OK - Found Cisco ACE $model ($hwver) software version $swver";
   }
   else {
      print "OK - Version checking only supported on admin context";
   }
}


################################################################################
# callback_check_snmp - callback routine for snmp failures                     #
# this is used to send a passive check_snmp alarm back to nagios to prevent    #
# notifications on 1-attempt and volatile services when snmp has stopped       #
# working.                                                                     #
################################################################################
sub callback_check_snmp {
   # we expect a nagios status code and message to be passed
   my ($code, $msg) = @_;

   # instantiate a new nagios_func object
   my $nagios = nagios_func->new( $args );

   # send a passive alarm to set check_snmp into an alarm state
   $nagios->passive_svc_submit({
      service => 'check_snmp',
      status  => $code,
      output  => $msg,
   });

   # since this is a snmp problem, lets exit this plugin as unknown
   $snmp->die3($msg);
}
