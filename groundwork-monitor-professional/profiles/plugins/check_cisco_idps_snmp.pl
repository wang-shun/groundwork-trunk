#!/usr/local/groundwork/perl/bin/perl -w

use strict;
use lib q(/usr/local/groundwork/nagios/libexec);
use cache_func ();
use nagios_func ();
use parse_func ();
use snmp_func ();

use constant PLUGIN => 'check_cisco_idps';

use constant OID => { 'alarmsgen'    => '.1.3.6.1.4.1.9.9.383.1.4.3.0',
                      'cpuusage'     => '.1.3.6.1.4.1.9.9.109.1.1.1.1.8',
                      'denialrate'   => '.1.3.6.1.4.1.9.9.383.1.4.2.0',
                      'memcritical'  => '.1.3.6.1.4.1.9.9.383.1.4.14.0',
                      'memused'      => '.1.3.6.1.4.1.9.9.221.1.1.1.1.7.1.1',
                      'memfree'      => '.1.3.6.1.4.1.9.9.221.1.1.1.1.8.1.1',
                      'packetloss'   => '.1.3.6.1.4.1.9.9.383.1.4.1.0',
                      'platform'     => '.1.3.6.1.2.1.47.1.1.1.1.13.1',
                      'sensoractive' => '.1.3.6.1.4.1.9.9.383.1.4.15.0',
                      'version'      => '.1.3.6.1.2.1.47.1.1.1.1.10.1',
                    };

use constant FUNCTIONS => { 'alerts'       => \&alerts,
                            'cpu'          => \&cpu,
                            'failover'     => \&failover,
                            'health'       => \&health,
                            'int_list'     => \&interface,
                            'mem'          => \&memory,
                            'performance'  => \&performance,
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
# alerts - collect number of alerts generated                                  #
################################################################################
sub alerts {
   my $alarms = $snmp->snmpget( OID->{alarmsgen} );
   print "OK - $alarms alarms generated|alarms=$alarms"
}


################################################################################
# cpu - check cpu utilization in percentage                                    #
################################################################################
sub cpu {
   # instantiate variables
   my @output = my @perfdata = ();

   # set warning/critical thresholds
   my ($warn, $crit) = grep /^\d+$/ => split /:/ => $args->{l};
   $warn ||= 85;    # default 85%
   $crit ||= 95;    # default 95%

   # retrieve all cpus utilization in percentage
   my @cpuusage = $snmp->snmpwalk( OID->{cpuusage} );
   foreach my $i (0 .. $#cpuusage) {
      my $cpu = $cpuusage[$i];
      push @perfdata, "core$i=$cpu";
      if ($cpu >= $crit) {
         push @output, "CRITICAL - CPU $i utilization at $cpu% " .
                       "(threshold $crit%)";
      }
      elsif ($cpu >= $warn) {
         push @output, "WARNING - CPU $i utilization at $cpu% " .
                       "(threshold $warn%)";
      }
      else {
         push @output, "OK - CPU $i utilization at $cpu%";
      }
   }

   # generate output  
   my @sorted = sort nagios_func::nagsort @output;
   if (grep /CRITICAL/ => @sorted) { 
      print shift(@sorted), "|@perfdata\n";
      print join "\n" => @sorted;
      exit 2;
   }
   elsif (grep /WARNING/ => @sorted) {
      print shift(@sorted), "|@perfdata\n";
      print join "\n" => @sorted;
      exit 1;
   }
   else {
      my $ok = @sorted;
      print "OK - $ok CPUs healthy [@perfdata]|@perfdata\n";
      print join "\n" => @sorted;
   }
}


################################################################################
# failover - check for failover state changes                                  #
################################################################################
sub failover {
   # instantiate variables
   my $state = { 1 => 'active', 2 => 'standby' };

   # retrieve current failover state
   my $sensoractive = $snmp->snmpget( OID->{sensoractive} );

   # get/set cached failover state
   my $cache = cache_func->new( $args->{h} );
   my $cached = $cache->get( 'sensoractive' );
   $cache->set( 'sensoractive', $sensoractive );

   # check for failover and generate output
   if ($cached && $cached ne $sensoractive) {
      print "CRITICAL - Failover state changed from $state->{$cached} to " .
            "$state->{ $sensoractive }";
      exit 2;
   }
   else {
      print "OK - Failover state is $state->{$sensoractive}";
   }
}


################################################################################
# health - check sensor overall health                                         #
################################################################################
sub health {
   # retrieve sensor health indicator
   my $memcritical = $snmp->snmpget( OID->{memcritical} );

   # generate output
   if ($memcritical > 3) {
      print "CRITICAL - Sensor health is severely degraded";
      exit 2;
   }
   elsif ($memcritical >0 && $memcritical < 4) {
      print "WARNING - Sensor health may be degraded";
      exit 1;
   }
   else {
      print "OK - Sensor health is okay";
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
# memory - check memory utilization                                            #
################################################################################
sub memory {
   # set warning/critical thresholds
   my ($warn, $crit) = grep /^\d+$/ => split /:/ => $args->{l};
   $warn ||= 80;    # default 80%
   $crit ||= 90;    # default 90%

   # retrieve and calculate memory utilization
   my ($used, $free) = $snmp->snmpget( OID->{memused}, OID->{memfree} );
   my $mem = sprintf "%d", 100 * $used / ($used + $free);

   # check against thresholds and generate output
   if ($mem >= $crit) {
      print "CRITICAL - Memory utilization at $mem% (threshold $crit%)" .
            "|percent=$mem";
      exit 2;
   }
   elsif ($mem >= $warn) {
      print "WARNING - Memory utilization at $mem% (threshold $warn%)" .
            "|percent=$mem";
      exit 1;
   }
   else {
      print "OK - Memory utilization at $mem%|percent=$mem";
   }
}


################################################################################
# performance - check packet loss and idp denies                               #
################################################################################
sub performance {
   # retrieve packet loss and denial rates
   my ($pl, $pd) = $snmp->snmpget( OID->{packetloss}, OID->{denialrate} );
   my $perfdata = "packetloss=$pl denials=$pd";

   # generate output
   if ($pl > 0) {
      print "WARNING - Sensor packet loss at $pl%|$perfdata";
      exit 1;
   }
   else {
      print "OK - Sensor packet loss and denial rate collected|$perfdata";
   }   
}


################################################################################
# uptime - check system uptime using HOST-RESOURCE-MIB                         #
################################################################################
sub uptime {
   return $snmp->snmp_uptime( $args );
}


################################################################################
# version - collect platform and version information                           #
################################################################################
sub version {
   # retrieve platform and version
   my ($platform, $version) = $snmp->snmpget( OID->{platform}, OID->{version} );

   # define upgrade array
   my $upgrade = { #'9.1.1' => 'Bug #7213',
                 };

   # generate output
   if (exists $upgrade->{$version}) {
      print "WARNING - Cisco $platform code version $version should be " .
            "upgraded: $upgrade->{$version}";
      exit 1;
   }
   else {
      print "OK - Cisco $platform version $version";
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
