#!/usr/local/groundwork/perl/bin/perl -w

use strict;
use lib q(/usr/local/groundwork/nagios/libexec);
use nagios_func ();
use parse_func ();
use snmp_func ();

use constant PLUGIN => 'check_datapower';

use constant OID => { 'sysdescr'         => '.1.3.6.1.2.1.1.1.0',
                      'cpuusage'         => '.1.3.6.1.4.1.14685.3.1.14.2.0',
                      'cryptoengtype'    => '.1.3.6.1.4.1.14685.3.1.34.1.0',
                      'cryptoengstate'   => '.1.3.6.1.4.1.14685.3.1.34.2.0',
                      'diskfreeenc'      => '.1.3.6.1.4.1.14685.3.1.29.1.0',
                      'disktotalenc'     => '.1.3.6.1.4.1.14685.3.1.29.2.0',
                      'diskfreeunenc'    => '.1.3.6.1.4.1.14685.3.1.29.3.0',
                      'disktotalunenc'   => '.1.3.6.1.4.1.14685.3.1.29.4.0',
                      'diskfreetmp'      => '.1.3.6.1.4.1.14685.3.1.29.5.0',
                      'disktotaltmp'     => '.1.3.6.1.4.1.14685.3.1.29.6.0',
                      'diskfreeint'      => '.1.3.6.1.4.1.14685.3.1.29.7.0',
                      'disktotalint'     => '.1.3.6.1.4.1.14685.3.1.29.8.0',
                      'load'             => '.1.3.6.1.4.1.14685.3.1.52.2.0',
                      'memusage'         => '.1.3.6.1.4.1.14685.3.1.5.1.0',
                      'sensorcaseopen'   => '.1.3.6.1.4.1.14685.3.1.55.9.0',
                      'sensorfanname'    => '.1.3.6.1.4.1.14685.3.1.97.1.1',
                      'sensorfanspeed'   => '.1.3.6.1.4.1.14685.3.1.97.1.2',
                      'sensorfanmin'     => '.1.3.6.1.4.1.14685.3.1.97.1.3',
                      'sensorfanstate'   => '.1.3.6.1.4.1.14685.3.1.97.1.4',
                      'sensorothername'  => '.1.3.6.1.4.1.14685.3.1.142.1.1',
                      'sensorothervalue' => '.1.3.6.1.4.1.14685.3.1.142.1.2',
                      'sensorotherstate' => '.1.3.6.1.4.1.14685.3.1.142.1.3',
                      'sensorpsu'        => '.1.3.6.1.4.1.14685.3.1.55.13.0',
                      'sensortempname'   => '.1.3.6.1.4.1.14685.3.1.141.1.1',
                      'sensortempvalue'  => '.1.3.6.1.4.1.14685.3.1.141.1.2',
                      'sensortempwarn'   => '.1.3.6.1.4.1.14685.3.1.141.1.3',
                      'sensortempcrit'   => '.1.3.6.1.4.1.14685.3.1.141.1.4',
                      'sensortempstate'  => '.1.3.6.1.4.1.14685.3.1.141.1.5',
                      'sensorvoltname'   => '.1.3.6.1.4.1.14685.3.1.140.1.1',
                      'sensorvoltvalue'  => '.1.3.6.1.4.1.14685.3.1.140.1.2',
                      'sensorvoltlow'    => '.1.3.6.1.4.1.14685.3.1.140.1.3',
                      'sensorvolthigh'   => '.1.3.6.1.4.1.14685.3.1.140.1.4',
                      'sensorvoltstate'  => '.1.3.6.1.4.1.14685.3.1.140.1.5',
                      'version'          => '.1.3.6.1.4.1.14685.3.1.112.2.0',
                    };

use constant FUNCTIONS => { 'cpu'          => \&cpu,
                            'disk'         => \&disk,
                            'int_list'     => \&interface,
                            'load'         => \&load,
                            'mem'          => \&memory,
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
# cpu - check cpu utilization in percentage                                    #
################################################################################
sub cpu {
   # instantiate variables
   my @output = my $perfdata = ();

   # set warning/critical thresholds
   my ($warn, $crit) = grep /^\d+$/ => split /:/ => $args->{l};
   $warn ||= 85;    # default 85%
   $crit ||= 95;    # default 95%

   # retrieve cpu utilization in percentage
   my $cpu = $snmp->snmpget( OID->{cpuusage} );

   # test against thresholds and generate output
   if ($cpu >= $crit) {
      print "CRITICAL - CPU utilization at $cpu% (threshold $crit%)" .
            "|cpu_usage=$cpu";
      exit 2;
   }
   elsif ($cpu >= $warn) {
      print "WARNING - CPU utilization at $cpu% (threshold $warn%)" .
            "|cpu_usage=$cpu";
      exit 1;
   }
   else {
      print "OK - CPU utilization at $cpu%|cpu_usage=$cpu";
   }
}


################################################################################
# disk - disk partition utilization                                            #
################################################################################
sub disk {
   # instantiate variables
   my @output = my @perfdata = ();
   my $partitions = { enc   => 'Encrypted',
                      int   => 'Internal',
                      tmp   => 'Temporary',
                      unenc => 'Unencrypted',
                    };

   # set warning/critical thresholds
   my ($warn, $crit) = grep /^\d+$/ => split /:/ => $args->{l};
   $warn ||= 80;    # default 80%
   $crit ||= 90;    # default 90%

   # disable error checking
   $snmp->{ec} = 0;
   
   # loop through all partitions
   foreach my $part (sort keys %$partitions) {
      my $free_oid  = OID->{ "diskfree$part" };
      my $total_oid = OID->{ "disktotal$part" };
      my ($free, $total) = $snmp->snmpget( $free_oid, $total_oid );
      if (defined $free && defined $total) {
         my $percent = sprintf "%d", ($total - $free) / $total * 100;
         if ($percent >= $crit) {
            push @output, "CRITICAL - $partitions->{$part} partition at " .
                          "$percent% utilization (threshold $crit%)";
         }
         elsif ($percent >= $warn) {
            push @output, "WARNING - $partitions->{$part} partition at " .
                          "$percent% utilization (threshold $warn%)";
         }
         else {
            push @output, "OK - $partitions->{$part} partition at $percent% " .
                          "utilization";
         }
         push @perfdata, "$part=$percent";
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
      print "OK - $ok partitions healthy [@perfdata]|@perfdata\n";
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
      $int->{int_name} eq 'lo' and next;
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
# load - retrieve system overall load percentage                               #
################################################################################
sub load {
   # set warning/critical thresholds
   my ($warn, $crit) = grep /^\d+$/ => split /:/ => $args->{l};
   $warn ||= 80;    # default 80%
   $crit ||= 90;    # default 90%

   # retrieve system load in percentage
   my $load = $snmp->snmpget( OID->{load} );
   
   # test against thresholds and generate output
   if ($load >= $crit) {
      print "CRITICAL - System load at $load% (threshold $crit%)|load=$load";
      exit 2;
   }
   elsif ($load >= $warn) {
      print "WARNING - System load at $load% (threshold $warn%)|load=$load";
      exit 1;
   }
   else {
      print "OK - System load at $load%|load=$load";
   }
}


################################################################################
# memory - retrieve system memory utilization                                  #
################################################################################
sub memory {
   # set warning/critical thresholds
   my ($warn, $crit) = grep /^\d+$/ => split /:/ => $args->{l};
   $warn ||= 80;    # default 80%
   $crit ||= 90;    # default 90%

   # retrieve system memory utilization in percentage
   my $memusage = $snmp->snmpget( OID->{memusage} );
   my $perfdata = "mem_usage=$memusage";
  
   # test against thresholds and generate output 
   if ($memusage >= $crit) {
      print "CRITICAL - Memory utilization at $memusage% (threshold $crit%)" .
            "|$perfdata";
      exit 2;
   }
   elsif ($memusage >= $warn) {
      print "WARNING - Memory utilization at $memusage% (threshold $warn%)" .
            "|$perfdata";
      exit 1;
   }
   else {
      print "OK - Memory utilization at $memusage%|$perfdata";
   }
}


################################################################################
# sensors - check chassis sensors                                              #
################################################################################
sub sensors {
   # instantiate variables
   my @output = ();
   my $states = { 1  => 'low-non-recoverable',
                  2  => 'low-critical',
                  3  => 'low-non-critical',
                  4  => 'ok',
                  5  => 'high-non-critical',
                  6  => 'high-critical',
                  7  => 'high-non-recoverale',
                  8  => 'failed',
                  9  => 'unreadable',
                  10 => 'invalid',
                };

   # check chassis intrusion sensor (case opened)
   my $caseopen = $snmp->snmpget( OID->{sensorcaseopen} );
   if ($caseopen == 1) {
      push @output, "WARNING - Chassis case is open";
   }
   elsif ($caseopen == 2) {
      push @output, "OK - Chassis case is closed";
   }

   # check power supply health
   my $sensorpsu = $snmp->snmpget( OID->{sensorpsu} );
   if ($sensorpsu == 1) {
      push @output, "OK - All power supplies are healthy";
   }
   elsif ($sensorpsu == 2) {
      push @output, "CRITICAL - Power supply 1 failure";
   }
   elsif ($sensorpsu == 3) {
      push @output, "CRITICAL - Power supply 2 failure";
   }

   # check fan health
   my $fannames = [ qw/null cpu-1 cpu-2 chassis-1 chassis-2 chassis-3 chassis-4 
                     chassis-5 chassis-6 chassis-7 chassis-8 fan-tray-1-fan-1
                     fan-tray-1-fan-2 fan-tray-1-fan-3 fan-tray-1-fan-4
                     fan-tray-2-fan-1 fan-tray-2-fan-2 fan-tray-2-fan-3
                     fan-tray-2-fan-4 hard-disk-tray-fan-1 
                     hard-disk-tray-fan-2/ ];
   my @fannames = $snmp->snmpbulkwalk( OID->{sensorfanname} );
   foreach my $index (@fannames) {
      my $oid_state = OID->{sensorfanstate} . ".$index";
      my $oid_rpm   = OID->{sensorfanspeed} . ".$index";
      my $oid_min   = OID->{sensorfanmin}   . ".$index";
      my ($state, $rpm, $min) = $snmp->snmpget( $oid_state, $oid_rpm, 
         $oid_min );
      if ($state == 4) {
         push @output, "OK - Fan $fannames->[$index] in $states->{$state} " .
                       "state at $rpm rpm";
      }
      else {
         push @output, "CRITICAL - Fan $fannames->[$index] in " .
                       "$states->{$state} state at $rpm rpm [min=$min]";
      }
   }

   
   # check voltage rails health
   my @voltnames = $snmp->snmpbulkwalk( OID->{sensorvoltname} );
   foreach my $voltname (@voltnames) {
      (my $ord = $voltname) =~ s/(.)/"." . ord($1)/eg;
      my $oid_state = OID->{sensorvoltstate} . $ord;
      my $oid_volts = OID->{sensorvoltvalue} . $ord;
      my $oid_low   = OID->{sensorvoltlow}   . $ord;
      my $oid_high  = OID->{sensorvolthigh}  . $ord;
      my ($state, $volts, $lower, $upper) = $snmp->snmpget( $oid_state, 
         $oid_volts, $oid_low, $oid_high );
      if ($state == 4) {
         push @output, "OK - $voltname in $states->{$state} at ${volts}v";
      }
      else {
         push @output, "CRITICAL - $voltname in $states->{$state} at " .
                       "${volts}v [lower=${lower}v upper=${upper}v]";
      }
   }
    
   # check temperatures
   my @tempnames = $snmp->snmpbulkwalk( OID->{sensortempname} );
   foreach my $tempname (@tempnames) {
      (my $ord = $tempname) =~ s/(.)/"." . ord($1)/eg;
      my $oid_state = OID->{sensortempstate} . $ord;
      my $oid_value = OID->{sensortempvalue} . $ord;
      my $oid_warn  = OID->{sensortempwarn}  . $ord;
      my $oid_crit  = OID->{sensortempcrit}  . $ord;
      my ($state, $value, $warn, $crit) = $snmp->snmpget( $oid_state, 
         $oid_value, $oid_warn, $oid_crit );
      if ($state == 4) {
         push @output, "OK - $tempname in $states->{$state} state at " .
                       "${value}C";
      }
      else {
         push @output, "CRITICAL - $tempname in $states->{$state} state " .
                       "at ${value}C [warn=${warn}C crit=${crit}C]";
      }
   }

   # check "other" miscellaneous sensors
   my @othernames = $snmp->snmpbulkwalk( OID->{sensorothername} );
   foreach my $othername (@othernames) {
      (my $ord = $othername) =~ s/(.)/"." . ord($1)/eg;
      my $oid_state = OID->{sensorotherstate} . $ord;
      my $oid_value = OID->{sensorothervalue} . $ord;
      my $z = { 1 => 'true', 2 => 'false' };
      my ($state, $value) = $snmp->snmpget( $oid_state, $oid_value );
      if ($state == 4) {
         push @output, "OK - $othername: $z->{$value}";
      }
      else {
         push @output, "CRITICAL - $othername: $z->{$value}";
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
# uptime - check system uptime using HOST-RESOURCE-MIB                         #
################################################################################
sub uptime {
   return $snmp->snmp_uptime( $args );
}


################################################################################
# version - collect platform and version information                           #
################################################################################
sub version {
   my ($sysdescr, $version) = $snmp->snmpget( OID->{sysdescr}, OID->{version} );

   my $upgrade = { #'9.1.1' => 'Bug #7213',
                 };
   
   if (exists $upgrade->{$version}) {
      print "WARNING - IBM DataPower code version $version should be " .
            "upgraded: $upgrade->{$version}";
      exit 1;
   }
   else {
      print "OK - IBM $sysdescr version $version";
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
