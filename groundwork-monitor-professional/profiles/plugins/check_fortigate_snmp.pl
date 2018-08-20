#!/usr/local/groundwork/perl/bin/perl -w

use strict;
use lib q(/usr/local/groundwork/nagios/libexec);
use cache_func ();
use nagios_func ();
use parse_func ();
use snmp_func ();

use constant PLUGIN => 'check_fortigate';

use constant OID => { platform  => '.1.3.6.1.2.1.1.1.0',
                      version   => '.1.3.6.1.4.1.12356.101.4.1.1.0',
                      syscpu    => '.1.3.6.1.4.1.12356.101.4.1.3.0',
                      sysmem    => '.1.3.6.1.4.1.12356.101.4.1.3.0',
                      diskusage => '.1.3.6.1.4.1.12356.101.4.1.6.0',
                      disktotal => '.1.3.6.1.4.1.12356.101.4.1.7.0',
                      sessions  => '.1.3.6.1.4.1.12356.101.4.1.8.0',
                      syslowmem => '.1.3.6.1.4.1.12356.101.4.1.9.0',
                      hamode    => '.1.3.6.1.4.1.12356.101.13.1.1.0',
                      hacpu     => '.1.3.6.1.4.1.12356.101.13.2.1.1.3',
                      hamem     => '.1.3.6.1.4.1.12356.101.13.2.1.1.4',
                      hanet     => '.1.3.6.1.4.1.12356.101.13.2.1.1.5',
                      hases     => '.1.3.6.1.4.1.12356.101.13.2.1.1.6',
                    };

use constant FUNCTIONS => { 'cpu'      => \&cpu,
                            'disk'     => \&disk,
                            'failover' => \&failover,
                            'int_list' => \&interface,
                            'mem'      => \&memory,
                            'sessions' => \&sessions,
                            'uptime'   => \&uptime,
                            'version'  => \&version,
                          };

use constant OPTIONS => { 'c'  => 'community string',
                          'h'  => 'hostname',
                          'i'  => 'ip address',
                          'l?' => 'levels [warning:critical]',
                          't'  => { 'check type' => FUNCTIONS },
                          'v'  => 'snmp version [1 or 2c]',
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
# cpu - checks cpu utilization                                                 #
################################################################################
sub cpu {
   # set thresholds
   my ($warn, $crit) = grep /^\d+$/ => split /:/ => $args->{l};
   $warn ||= 85;    # default 85%
   $crit ||= 95;    # default 95%

   # retrieve cpu utilization OID (in percent)
   my $cpu = $snmp->snmpget( OID->{syscpu} );

   # test against thresholds and generate output
   my $perfdata = "percent=$cpu";
   if ($cpu >= $crit) {
      print "CRITICAL - CPU usage at $cpu% (threshold $crit%)|$perfdata";
      exit 2;
   }
   elsif ($cpu >= $warn) {
      print "WARNING - CPU usage at $cpu% (threshold $warn%)|$perfdata";
      exit 1;
   }
   else {
      print "OK - CPU usage at $cpu%|$perfdata";
   }
}


################################################################################
# disk - checks disk utilization                                               #
################################################################################
sub disk {
   # set thresholds
   my ($warn, $crit) = grep /^\d+$/ => split /:/ => $args->{l};
   $warn ||= 80;    # default 80%
   $crit ||= 90;    # default 90%

   # snmpget returns megabytes
   my ($used, $total) = $snmp->snmpget( OID->{diskusage}, OID->{disktotal} );

   # sometimes there are no disks
   if (!$total) {
      print "OK - No disks found";
      exit 0;
   }

   # calculate percentage
   my $disk = sprintf("%d", 100 * $used / $total);
   my $perfdata = "percent=$disk";

   # test against thresholds and generate output
   if ($disk >= $crit) {
      print "CRITICAL - Disk utilization at $disk% (threshold $crit%)" .
            "|$perfdata";
      exit 2;
   }
   elsif ($disk >= $warn) {
      print "WARNING - Disk utilization at $disk% (threshold $warn%)|$perfdata";
      exit 1;
   }
   else {
      print "OK - Disk utilization at $disk%|$perfdata";
   }
}


################################################################################
# failover - check firewall for failovers                                      #
################################################################################
sub failover {
   # instantiate variables
   my $sum = 0;
   my $modes = { 1 => 'standalone', 2 => 'active-active', 
                 3 => 'active-passive' };
   my $states = { 0 => 'active', 1 => 'standby' };

   # retrieve HA mode (standalone, active-active or active-passive)
   my $hamode = $snmp->snmpget( OID->{hamode} );
   if ($hamode < 3) {
      print "OK - Failover mode is $modes->{$hamode}";
      return;
   }
  
   # retrieve member statistics  
   my @hacpu = $snmp->snmpbulkwalk( OID->{hacpu} );
   my @hamem = $snmp->snmpbulkwalk( OID->{hamem} );
   my @hanet = $snmp->snmpbulkwalk( OID->{hanet} );
   my @hases = $snmp->snmpbulkwalk( OID->{hases} );

   # compare member statistics to determine who is more active
   $sum += $hacpu[0] <=> $hacpu[1];
   $sum += $hamem[0] <=> $hamem[1];
   $sum += $hanet[0] <=> $hanet[1];
   $sum += $hases[0] <=> $hases[1];

   # retrieve cached failover state
   my $cache = cache_func->new( $args->{h} );
   my $cached = $cache->get( 'failstate' );

   # 
   if ($sum > 0) {
      # this is the active unit
      $cache->set( 'failstate', 0 );
      if ($cached == 0 || not defined $cached) {
         print "OK - Failover state is active";
      }
      else {
         print "CRITICAL - Failover changed state from $states->{$cached} to " .
               "active";
         exit 2;
      }
   }
   elsif ($sum < 0) {
      # this is the standby unit
      $cache->set( 'failstate', 1 );
      if ($cached == 1 || not defined $cached) {
         print "OK - Failover state is standby";
      }
      else {
         print "CRITICAL - Failvoer state changed from $states->{$cached} to " .
               "standby";
         exit 2;
      }
   }
   else {
      # unable to determine which unit this is
      print "UNKNOWN - Unable to determine whether device is active or standby";
      exit 3;
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
      grep { $int->{int_type} == $_ } (6, 135) or next;
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
# memory - checks memory utilization                                           #
################################################################################
sub memory {
   # instantiate variables
   my @output = my @perfdata = ();

   # set thresholds
   my ($warn, $crit) = grep /^\d+$/ => split /:/ => $args->{l};
   $warn ||= 80;    # default 80%
   $crit ||= 90;    # default 90%

   # retrieve memory and lowmem (percent)
   my ($mem, $lowmem) = $snmp->snmpget( OID->{sysmem}, OID->{syslowmem} );
   push @perfdata, "mem=$mem";
   push @perfdata, "lowmem=$lowmem";

   # test memory against thresholds
   if ($mem >= $crit) {
      push @output, "CRITICAL - System memory utilization at $mem% " .
                    "(threshold $crit%)";
   }
   elsif ($mem >= $warn) {
      push @output, "WARNING - System memory utilization at $mem% " .
                    "(threshold $warn%)";
   }
   else {
      push @output, "OK - System memory utilization at $mem%";
   }

   # test lowmem against thresholds
   if ($lowmem >= $crit) {
      push @output, "CRITICAL - Low memory utilization at $lowmem% " .
                    "(threshold $crit%)";
   }
   elsif ($lowmem >= $warn) {
      push @output, "WARNING - Low memory utilization at $lowmem% " .
                    "(threshold $warn%)";
   }
   else {
      push @output, "OK - Low memory utilization at $lowmem%";
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
      print shift(@sorted), "|@perfdata\n";
      print join "\n" => @sorted;
   }
}


################################################################################
# sessions - retrieves concurrent sessions and checks against upper limit      #
################################################################################
sub sessions {
   # define concurrent session maximums per platform
   my $platforms = { 'FortiGate-5020'      => 4000000,
                     'FortiGate-5060'      => 66000000,
                     'FortiGate-5140'      => 132000000,
                     'FortiGate-5140B'     => 132000000,
                     'FortiGate-3950B'     => 20000000,
                     'FortiGate-1240B'     => 5000000,
                     'FortiGate-600C'      => 3000000,
                     'FortiGate-200B'      => 500000,
                     'FortiGate-80C'       => 1200000,
                     'FortiGate-40C'       => 40000,
                     'FortiGate-VM00'      => 400000,
                     'FortiGate-VM01'      => 600000,
                     'FortiGate-VM02'      => 800000,
                     'FortiGate-VM04'      => 1100000,
                     'FortiGate-VM08'      => 1200000,
                     'FortiGate-3140B'     => 10000000,
                     'FortiGate-1000C'     => 7000000,
                     'FortiGate-310B'      => 600000,
                     'FortiGate-311B'      => 600000,
                     'FortiGate-110C'      => 400000,
                     'FortiGate-60C'       => 80000,
                     'FortiGate-30B'       => 5000,
                     'FortiGate-5001A-SW'  => 2000000,
                     'FortiGate-5001A-DW'  => 2000000,
                     'FortiGate-5001B'     => 11000000,
                     'FortiGate-3040B'     => 10000000,
                     'FortiGate-620B'      => 1000000,
                     'FortiGate-300C'      => 1000000,
                     'FortiGate-Voice-80C' => 400000,
                     'FortiGate-50B'       => 25000,
                     'FortiGate-20C'       => 10000,
                   };

   # set thresholds
   my ($warn, $crit) = grep /^\d+$/ => split /:/ => $args->{l};
   $warn ||= 75;    # default 75%
   $crit ||= 90;    # default 90%

   # retrieve the platform and current sessions
   my ($platform, $sessions) = $snmp->snmpget( OID->{platform}, 
                                               OID->{sessions} );

   # truncate the platform 
   ($platform) = $platform =~ /(FortiGate-\S+)/i;

   # not a known platform
   unless (exists $platforms->{$platform}) {
      print "UNKNOWN - Unknown FortiGate model $platform";
      exit 3;
   }

   # calculate session thresholds
   my $critlevel = sprintf("%d", $platforms->{$platform} * $crit / 100);
   my $warnlevel = sprintf("%d", $platforms->{$platform} * $warn / 100);
   my $perfdata  = "sessions=$sessions";

   # test against thresholds and generate output
   if ($sessions >= $critlevel) {
      print "CRITICAL - Sessions at $sessions (threshold $critlevel)|$perfdata";
      exit 2;
   }
   elsif ($sessions >= $warnlevel) {
      print "WARNING - Sessions at $sessions (threshold $warnlevel)|$perfdata";
      exit 1;
   }
   else {
      print "OK - Sessions at $sessions|$perfdata";
   } 
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
   my ($platform, $version) = $snmp->snmpget( OID->{platform}, OID->{version} );
   my $upgrade = { #'9.1.1' => 'Bug #7213',
                 };

   if (exists $upgrade->{$version}) {
      print "WARNING - FortiGate code version $version should be upgraded: " .
            "$upgrade->{$version}";
      exit 1;
   }

   print "OK - $platform version $version";
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

