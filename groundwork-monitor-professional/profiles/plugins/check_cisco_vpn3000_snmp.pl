#!/usr/local/groundwork/perl/bin/perl -w

use strict;
use lib q(/usr/local/groundwork/nagios/libexec);
use nagios_func ();
use parse_func ();
use snmp_func ();

use constant PLUGIN => 'check_cisco_vpn3000';

use constant OID => { 'activeusers'   => '.1.3.6.1.4.1.3076.2.1.2.17.1.9.0',
                      'cpuvoltvalue'  => '.1.3.6.1.4.1.3076.2.1.2.22.1.1.0',
                      'cpuvoltalarm'  => '.1.3.6.1.4.1.3076.2.1.2.22.1.2.0',
                      'ps13vvalue'    => '.1.3.6.1.4.1.3076.2.1.2.22.1.5.0',
                      'ps13valarm'    => '.1.3.6.1.4.1.3076.2.1.2.22.1.6.0',
                      'ps15vvalue'    => '.1.3.6.1.4.1.3076.2.1.2.22.1.9.0',
                      'ps15valarm'    => '.1.3.6.1.4.1.3076.2.1.2.22.1.10.0',
                      'ps23vvalue'    => '.1.3.6.1.4.1.3076.2.1.2.22.1.13.0',
                      'ps23valarm'    => '.1.3.6.1.4.1.3076.2.1.2.22.1.14.0',
                      'ps25vvalue'    => '.1.3.6.1.4.1.3076.2.1.2.22.1.17.0',
                      'ps25valarm'    => '.1.3.6.1.4.1.3076.2.1.2.22.1.18.0',
                      'mobo3vvalue'   => '.1.3.6.1.4.1.3076.2.1.2.22.1.21.0',
                      'mobo3valarm'   => '.1.3.6.1.4.1.3076.2.1.2.22.1.22.0',
                      'mobo5vvalue'   => '.1.3.6.1.4.1.3076.2.1.2.22.1.25.0',
                      'mobo5valarm'   => '.1.3.6.1.4.1.3076.2.1.2.22.1.26.0',
                      'cputempvalue'  => '.1.3.6.1.4.1.3076.2.1.2.22.1.29.0',
                      'cputempalarm'  => '.1.3.6.1.4.1.3076.2.1.2.22.1.30.0',
                      'cagetempvalue' => '.1.3.6.1.4.1.3076.2.1.2.22.1.33.0',
                      'cagetempalarm' => '.1.3.6.1.4.1.3076.2.1.2.22.1.34.0',
                      'fan1rpmvalue'  => '.1.3.6.1.4.1.3076.2.1.2.22.1.37.0',
                      'fan1rpmalarm'  => '.1.3.6.1.4.1.3076.2.1.2.22.1.38.0',
                      'fan2rpmvalue'  => '.1.3.6.1.4.1.3076.2.1.2.22.1.41.0',
                      'fan2rpmalarm'  => '.1.3.6.1.4.1.3076.2.1.2.22.1.42.0',
                      'fan3rpmvalue'  => '.1.3.6.1.4.1.3076.2.1.2.22.1.45.0',
                      'fan3rpmalarm'  => '.1.3.6.1.4.1.3076.2.1.2.22.1.46.0',
                      'ps1type'       => '.1.3.6.1.4.1.3076.2.1.2.22.1.49.0',
                      'ps2type'       => '.1.3.6.1.4.1.3076.2.1.2.22.1.50.0',
                      'slot1state'    => '.1.3.6.1.4.1.3076.2.1.2.22.1.51.0',
                      'slot2state'    => '.1.3.6.1.4.1.3076.2.1.2.22.1.52.0',
                      'slot3state'    => '.1.3.6.1.4.1.3076.2.1.2.22.1.53.0',
                      'slot4state'    => '.1.3.6.1.4.1.3076.2.1.2.22.1.54.0',
                      'slot1oper'     => '.1.3.6.1.4.1.3076.2.1.2.22.1.55.0',
                      'slot2oper'     => '.1.3.6.1.4.1.3076.2.1.2.22.1.56.0',
                      'slot3oper'     => '.1.3.6.1.4.1.3076.2.1.2.22.1.57.0',
                      'slot4oper'     => '.1.3.6.1.4.1.3076.2.1.2.22.1.58.0',
                      'cpuusage'      => '.1.3.6.1.4.1.3076.2.1.2.25.1.2.0',
                      'sessiongauge'  => '.1.3.6.1.4.1.3076.2.1.2.25.1.3.0',
                      'devicetype'    => '.1.3.6.1.4.1.3076.2.1.2.36.1.2.0',
                      'deviceversion' => '.1.3.6.1.4.1.3076.2.1.2.1.1.4.0',
                    };

use constant FUNCTIONS => { 'cpu'          => \&cpu,
                            'int_list'     => \&interface,
                            'sensors'      => \&sensors,
                            'sessions'     => \&sessions,
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
# cpu - check cpu utilization in percentage                                    #
################################################################################
sub cpu {
   # set warning/critical thresholds
   my ($warn, $crit) = grep /^\d+$/ => split /:/ => $args->{l};
   $warn ||= 85;    # default 85%
   $crit ||= 95;    # default 95%

   # retrieve cpu utilization in percent
   my $cpu = $snmp->snmpget( OID->{cpuusage} );

   # test against thresholds and generate output
   if ($cpu >= $crit) {
      print "CRITICAL - CPU utilization at $cpu% (threshold $crit%)" .
            "|percent=$cpu";
      exit 2;
   }
   elsif ($cpu >= $warn) {
      print "WARNING - CPU utilization at $cpu% (threshold $warn%)" .
            "|percent=$cpu";
      exit 1;
   }
   else {
      print "OK - CPU utilization at $cpu%|percent=$cpu";
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
   my $dup = {};
   foreach my $i (sort { $a <=> $b } keys %$interface) {
      my $int = $interface->{$i};
      $int->{int_name} =~ tr/ /-/;
      $int->{no_in_drops} = 1;
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
# sensors - check hardware sensors                                             #
################################################################################
sub sensors {
   # instantiate variables
   my @output = ();

   # cpu voltage
   my ($cpuvoltvalue, $cpuvoltalarm) = $snmp->snmpget( OID->{cpuvoltvalue}, 
                                                       OID->{cpuvoltalarm} );
   my $cpuvolts = sprintf "%.2fv", $cpuvoltvalue / 100;
   if ($cpuvoltalarm == 1) {
      push @output, "CRITICAL - CPU voltage alarm $cpuvolts";
   }
   else {
      push @output, "OK - CPU voltage at $cpuvolts";
   }

   # power supply voltages
   foreach my $psu (1 .. 2) {
      foreach my $voltage ('3v', '5v') {
         my $value_oid = OID->{ "ps${psu}${voltage}value" };
         my $alarm_oid = OID->{ "ps${psu}${voltage}alarm" };
         my ($value, $alarm) = $snmp->snmpget( $value_oid, $alarm_oid );
         my $volts = sprintf "%.2fv", $value / 100;
         if ($alarm == 1) {
            push @output, "CRITICAL - Power supply $psu voltage $voltage " .
                          "alarm $volts";
         }
         else {
            push @output, "OK - Power supply $psu voltage $voltage at $volts";
         }
      }
   }
      
   # motherboard voltages
   foreach my $voltage ('3v', '5v') {
      my $value_oid = OID->{ "mobo${voltage}value" };
      my $alarm_oid = OID->{ "mobo${voltage}alarm" };
      my ($value, $alarm) = $snmp->snmpget( $value_oid, $alarm_oid );
      my $volts = sprintf "%.2fv", $value / 100;
      if ($alarm == 1) {
         push @output, "CRITICAL - Motherboard voltage $voltage alarm $volts";
      }
      else {
         push @output, "OK - Motherboard voltage $voltage at $volts";
      }
   }

   # cpu temperature
   my ($cputempvalue, $cputempalarm) = $snmp->snmpget( OID->{cputempvalue},
                                                       OID->{cputempalarm} );
   if ($cputempalarm == 1) {
      push @output, "CRITICAL - CPU temperature alarm ${cputempvalue}C";
   }
   else {
      push @output, "OK - CPU temperature at ${cputempvalue}C";
   }

   # chassis (cage) temperature
   my ($cagetempvalue, $cagetempalarm) = $snmp->snmpget( OID->{cagetempvalue},
                                                         OID->{cagetempalarm} );
   if ($cagetempalarm == 1) {
      push @output, "CRITICAL - Chassis temperature alarm ${cagetempvalue}C";
   }
   else {
      push @output, "OK - Chassis temperature at ${cagetempvalue}C";
   }

   # fans
   for my $fan (1 .. 3) {
      my $value_oid = OID->{ "fan${fan}rpmvalue" };
      my $alarm_oid = OID->{ "fan${fan}rpmalarm" };
      my ($value, $alarm) = $snmp->snmpget( $value_oid, $alarm_oid );
      if ($alarm == 1) {
         push @output, "CRITICAL - Fan $fan alarm $value rpm";
      }
      else {
         push @output, "OK - Fan $fan at $value rpm";
      }
   }

   # power supplies
   foreach my $psu (1 .. 2) {
      my $type_oid = OID->{ "ps${psu}type" };
      my $type = $snmp->snmpget( $type_oid );
      if ($type == 1) {
         push @output, "OK - Power supply $psu not found";
      }
      else {
         push @output, "OK - Power supply $psu found";
      }
   }

   # card slots
   foreach my $slot (1 .. 4) {
      my $type_oid = OID->{ "slot${slot}state" };
      my $oper_oid = OID->{ "slot${slot}oper" };
      my ($type, $oper) = $snmp->snmpget( $type_oid, $oper_oid );
      if ($type > 1) {
         if ($oper == 2) {
            push @output, "CRITICAL - Card in slot $slot is not operational";
         }
         else {
            push @output, "OK - Card in slot $slot is operational";
         }
      }
      else {
         push @output, "OK - No card in slot $slot";
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
      my $ok = @sorted;
      print "OK - $ok sensors healthy\n";
      print join "\n" => @sorted;
   }
}


################################################################################
# sessions - capture number of sessions in use                                 #
################################################################################
sub sessions {
   # set warning/critical thresholds
   my ($warn, $crit) = grep /^\d+$/ => split /:/ => $args->{l};
   $warn ||= 85;    # default 85%
   $crit ||= 95;    # default 95%

   # retrieve number of current sessions and percentage of maximum
   my ($activeusers, $sessiongauge) = $snmp->snmpget( OID->{activeusers},
                                                      OID->{sessiongauge} );

   # check against thresholds and generate output
   if ($sessiongauge >= $crit) {
      print "CRITICAL - Current sessions at $activeusers ($sessiongauge%) " .
            "(threshold $crit%)|users=$activeusers";
      exit 2;
   }
   elsif ($sessiongauge >= $warn) {
      print "WARNING - Current sessions at $activeusers ($sessiongauge%) " .
            "(threshold $warn%)|users=$activeusers";
      exit 1;
   }
   else {
      print "OK - Current sessions at $activeusers ($sessiongauge%)" .
            "|users=$activeusers";
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
   # instantiate variables
   my $types = { qw/1 unknown 3 vpn3005 4 vpn3015 5 vpn3030 6 vpn3060 7 vpn3080
                    8 vpn3002/ };

   # define upgrade array
   my $upgrade = { #'9.1.1' => 'Bug #7213',
                 };
  
   # retrieve device type and version 
   my ($type, $version) = $snmp->snmpget( OID->{devicetype}, 
                                          OID->{deviceversion} );
   my $platform = $types->{$type} || 'UNKNOWN';

   # generate output
   if (exists $upgrade->{$version}) {
      print "WARNING - Cisco $platform version $version should be " .
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
