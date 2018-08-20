#!/usr/local/groundwork/perl/bin/perl -w

use strict;
use lib q(/usr/local/groundwork/nagios/libexec);
use cache_func ();
use nagios_func ();
use parse_func ();
use snmp_func ();

use constant PLUGIN => 'check_juniper_srx';

use constant OID => { 'sysdescr'       => '.1.3.6.1.2.1.1.1.0',
                      'jnxboxdescr'    => '.1.3.6.1.4.1.2636.3.1.2.0',
                      'jnxconindex'    => '.1.3.6.1.4.1.2636.3.1.6.1.1',
                      'jnxcondescr'    => '.1.3.6.1.4.1.2636.3.1.6.1.6',
                      'jnxopdescr'     => '.1.3.6.1.4.1.2636.3.1.13.1.5',
                      'jnxopstate'     => '.1.3.6.1.4.1.2636.3.1.13.1.6',
                      'jnxoptemp'      => '.1.3.6.1.4.1.2636.3.1.13.1.7',
                      'jnxopcpu'       => '.1.3.6.1.4.1.2636.3.1.13.1.8',
                      'jnxopmem'       => '.1.3.6.1.4.1.2636.3.1.13.1.11',
                      'jnxspucpu'      => '.1.3.6.1.4.1.2636.3.39.1.12.1.1.1.4',
                      'jnxspumem'      => '.1.3.6.1.4.1.2636.3.39.1.12.1.1.1.5',
                      'jnxspucurflows' => '.1.3.6.1.4.1.2636.3.39.1.12.1.1.1.6',
                      'jnxspumaxflows' => '.1.3.6.1.4.1.2636.3.39.1.12.1.1.1.7',
                      'jnxspucurcp'    => '.1.3.6.1.4.1.2636.3.39.1.12.1.1.1.8',
                      'jnxspumaxcp'    => '.1.3.6.1.4.1.2636.3.39.1.12.1.1.1.9',
                      'jnxspunode'     => '.1.3.6.1.4.1.2636.3.39.1.12.1.1.1.11',
                    };

use constant FUNCTIONS => { 'clock'        => \&clock,
                            'cpu'          => \&cpu,
                            'disk'         => \&disk,
                            'int_list'     => \&interface,
                            'mem'          => \&memory,
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
# clock - compare local/remote clocks using HOST-RESOURCE-MIB                  #
################################################################################
sub clock {
   return $snmp->snmp_datetime( $args );
}


################################################################################
# cpu - check cpu utilization of control/data planes                           #
################################################################################
sub cpu {
   # instantiate variables
   my @output = my @perfdata = ();
   my $counter = {};

   # set warning/critical thresholds
   my ($warn, $crit) = grep /^\d+$/ => split /:/ => $args->{l};
   $warn ||= 85;    # default 85%
   $crit ||= 95;    # default 95%

   # map container id to description
   my $containers = { 
      map { $_ => $snmp->snmpget( OID->{jnxcondescr} . ".$_" ) }
         $snmp->snmpbulkwalk( OID->{jnxconindex} ) 
   };

   # retrieve routing engine (control-plane) cpu utilization
   foreach my $container (sort { $a <=> $b } keys %$containers) {
      $containers->{$container} =~ /Routing Engine/ or next;
      my @descr = $snmp->snmpbulkwalk( OID->{jnxopdescr} . ".$container" );
      my @cpu   = $snmp->snmpbulkwalk( OID->{jnxopcpu} . ".$container" );
      foreach my $i (0 .. $#cpu) {
         $descr[$i] =~ /Routing Engine/ or next;
         my ($node, $reid) = $descr[$i] =~ /^(node\d+) Routing Engine (\d+)$/;
         if (defined $node && defined $reid) {
            push @perfdata, "${node}_re_$reid=$cpu[$i]";
         }
         else {
            push @perfdata, "re=$cpu[$i]";
         }
         if ($cpu[$i] >= $crit) {
            push @output, "CRITICAL - $descr[$i] cpu utilization at " .
                          "$cpu[$i]% (threshold $crit%)";
         }
         elsif ($cpu[$i] >= $warn) {
            push @output, "WARNING - $descr[$i] cpu utilization at $cpu[$i]% " .
                          "(threshold $warn%)";
         }
         else {
            push @output, "OK - $descr[$i] cpu utilization at $cpu[$i]%";
         }
      }
   }

   # retrieve spu (data-plane) cpu utilization
   my @spucpu  = $snmp->snmpbulkwalk( OID->{jnxspucpu} );
   my @spunode = $snmp->snmpbulkwalk( OID->{jnxspunode} );

   # test spu cpu utilization against thresholds
   foreach my $i (0 .. $#spucpu) {
      my $cpu  = $spucpu[$i];
      my $node = $spunode[$i];
      my $spu  = $counter->{ $node }++;
      push @perfdata, "${node}_spu_${spu}=$cpu";
      if ($cpu >= $crit) {
         push @output, "CRITICAL - $node spu $spu cpu utilization at $cpu% " .
                       "(threshold $crit%)";
      }
      elsif ($cpu >= $warn) {
         push @output, "WARNING - $node spu $spu cpu utilization at $cpu% " .
                       "(threshold $warn%)";
      }
      else {
         push @output, "OK - $node spu $spu cpu utilization at $cpu%";
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
# disk - checks disk/partition utilization using standard HOST-RESOURCE-MIB    #
################################################################################
sub disk {
   return $snmp->snmp_disk( $args, qw(/dev/md0 /dev/md2) );
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
      $int->{int_type} == 6 or $int->{int_type} == 135 or next;
      $int->{int_name} =~ /\.32767$/ and next;
      $int->{int_name} =~ tr|/|-|;
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
# memory - check memory utilization on control/data planes                     #
################################################################################
sub memory {
   # instantiate variables
   my @output = my @perfdata = ();
   my $counter = {};

   # set warning/critical thresholds
   my ($warn, $crit) = grep /^\d+$/ => split /:/ => $args->{l};
   $warn ||= 80;    # default 80%
   $crit ||= 90;    # default 90%

   # map container id to description
   my $containers = { 
      map { $_ => $snmp->snmpget( OID->{jnxcondescr} . ".$_" ) }
         $snmp->snmpbulkwalk( OID->{jnxconindex} ) 
   };

   # retrieve routing engine (control-plane) memory buffers
   foreach my $container (sort { $a <=> $b } keys %$containers) {
      $containers->{$container} =~ /Routing Engine/ or next;
      my @descr = $snmp->snmpbulkwalk( OID->{jnxopdescr} . ".$container" );
      my @mem   = $snmp->snmpbulkwalk( OID->{jnxopmem} . ".$container" );
      foreach my $i (0 .. $#mem) {
         $descr[$i] =~ /Routing Engine/ or next;
         my ($node, $reid) = $descr[$i] =~ /^(node\d+) Routing Engine (\d+)$/;
         if (defined $node && defined $reid) {
            push @perfdata, "${node}_re_$reid=$mem[$i]";
         }
         else {
            push @perfdata, "re=$mem[$i]";
         }
         if ($mem[$i] >= $crit) {
            push @output, "CRITICAL - $descr[$i] memory utilization at " .
                          "$mem[$i]% (threshold $crit%)";
         }
         elsif ($mem[$i] >= $warn) {
            push @output, "WARNING - $descr[$i] memory utilization at " .
                          "$mem[$i]% (threshold $warn%)";
         }
         else {
            push @output, "OK - $descr[$i] memory utilization at $mem[$i]%";
         }
      }
   }

   # retrieve spu (data-plane) memory utilization
   my @spumem  = $snmp->snmpbulkwalk( OID->{jnxspumem} );
   my @spunode = $snmp->snmpbulkwalk( OID->{jnxspunode} );

   # test spu memory utilization against thresholds
   foreach my $i (0 .. $#spumem) {
      my $mem  = $spumem[$i];
      my $node = $spunode[$i];
      my $spu  = $counter->{ $node }++;
      push @perfdata, "${node}_spu_${spu}=$mem";
      if ($mem >= $crit) {
         push @output, "CRITICAL - $node spu $spu memory utilization at " .
                       "$mem% (threshold $crit%)";
      }
      elsif ($spumem[$i] >= $warn) {
         push @output, "WARNING - $node spu $spu memory utilization at " .
                       "$mem% (threshold $warn%)";
      }
      else {
         push @output, "OK - $node spu $spu memory utilization at $mem%";
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
      print "OK - Memory healthy [@perfdata]|@perfdata\n";
      print join "\n" => @sorted;
   }
}


################################################################################
# sensors - check srx component/service/sensor health                          #
################################################################################
sub sensors {
   # instantiate variables
   my @output = ();
   my $states = { 1 => 'unknown',
                  2 => 'running',
                  3 => 'ready',
                  4 => 'reset',
                  5 => 'running-full-speed',
                  6 => 'down',
                  7 => 'standby',
                };

   # map container id to description
   my $containers = { 
      map { $_ => $snmp->snmpget( OID->{jnxcondescr} . ".$_" ) }
         $snmp->snmpbulkwalk( OID->{jnxconindex} ) 
   };

   # compontent states (i.e. running)
   foreach my $container (sort { $a <=> $b } keys %$containers) {
      my @descr = $snmp->snmpbulkwalk( OID->{jnxopdescr} . ".$container" );
      my @state = $snmp->snmpbulkwalk( OID->{jnxopstate} . ".$container" );
      foreach my $i (0 .. $#state) {
         if ($state[$i] =~ /^[235]$/) {
            push @output, "OK - $descr[$i] is $states->{ $state[$i] }";
         } 
         else {
            push @output, "CRITICAL - $descr[$i] is $states->{ $state[$i] }";
         }
      }
   }

   # retrieve temperatures
   foreach my $container (sort { $a <=> $b } keys %$containers) {
      $containers->{$container} =~ /^FPC/ or
      $containers->{$container} =~ /^CB/  or next;
      my @descr = $snmp->snmpbulkwalk( OID->{jnxopdescr} . ".$container" );
      my @temp  = $snmp->snmpbulkwalk( OID->{jnxoptemp} . ".$container" );
      foreach my $i (0 .. $#temp) {
         if ($temp[$i] >= 85) {
            push @output, "CRITICAL - $descr[$i] at $temp[$i]C (threshold 85C)";
         }
         else {
            push @output, "OK - $descr[$i] at $temp[$i]C";
         }
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
# sessions - check spu session utilization                                     #
################################################################################
sub sessions {
   # instantiate variables
   my @output = my @perfdata = ();
   my $counter = {};

   # set warning/critical thresholds
   my ($warn, $crit) = grep /^\d+$/ => split /:/ => $args->{l};
   $warn ||= 80;    # default 80%
   $crit ||= 90;    # default 90%

   # retrieve listing of spu nodes
   my @spunode  = $snmp->snmpbulkwalk( OID->{jnxspunode} );

   # retrieve current and maximum (limit) flow sessions
   my @curflows = $snmp->snmpbulkwalk( OID->{jnxspucurflows} );
   my @maxflows = $snmp->snmpbulkwalk( OID->{jnxspumaxflows} );

   # retrieve current and maximum (limit) cp sessions
   # only supported on enterprise-class devices
   my @curcp    = $snmp->snmpbulkwalk( OID->{jnxspucurcp} );
   my @maxcp    = $snmp->snmpbulkwalk( OID->{jnxspumaxcp} );

   # loop through each node and test sessions against thresholds
   foreach my $i (0 .. $#spunode) {
      my $spu  = $counter->{ $spunode[$i] }++;
      # test flow sessions (all platforms)
      my $flows = sprintf "%d", 100 * $curflows[$i] / $maxflows[$i];
      push @perfdata, "$spunode[$i]_flows_$spu=$flows";
      if ($flows >= $crit) {
         push @output, "CRITICAL - $spunode[$i] spu $spu flow sessions at " .
                       "$curflows[$i] of $maxflows[$i] or $flows% " .
                       "utilization (threshold $crit%)";
      }
      elsif ($flows >= $warn) {
         push @output, "WARNING - $spunode[$i] spu $spu flow sessions at " .
                       "$curflows[$i] of $maxflows[$i] or $flows% " .
                       "utilization (threshold $warn%)";
      }
      else {
         push @output, "OK - $spunode[$i] spu $spu flow sessions at " .
                       "$curflows[$i] of $maxflows[$i] or $flows% utilization";
      }

      # skip cp sessions if there isn't any data to act on
      $curcp[$i] and $maxcp[$i] or next;

      # test cp sessions (datacenter platforms)
      my $cps = sprintf "%d", 100 * $curcp[$i] / $maxcp[$i];
      push @perfdata, "$spunode[$i]_cp_$spu=$cps";
      if ($cps >= $crit) {
         push @output, "CRITICAL - $spunode[$i] spu $spu cp sessions at " .
                       "$curcp[$i] of $maxcp[$i] or $cps% utilization " .
                       "(threshold $crit%)";
      }
      elsif ($cps >= $warn) {
         push @output, "WARNING - $spunode[$i] spu $spu cp sessions at " .
                       "$curcp[$i] of $maxcp[$i] or $cps% utilization " .
                       "(threshold $warn%)";
      }
      else {
         push @output, "OK - $spunode[$i] spu $spu cp sessions at $curcp[$i] " .
                       "of $maxcp[$i] or $cps% utilization";
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
      print "OK - $ok sessions healthy [@perfdata]|@perfdata\n";
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
   my $sysdescr = $snmp->snmpget( OID->{sysdescr} );
   my ($platform, $version) = $sysdescr =~
      /^Juniper Networks, Inc. (\w+) internet router, kernel JUNOS (\S+)/;

   my $upgrade = { #'9.1.1' => 'Bug #7213',
                 };

   if (exists $upgrade->{$version}) {
      print "WARNING - JunOS code version $version should be upgraded: " .
            "$upgrade->{$version}";
      exit 1;
   }
   else {
      print "OK - JunOS $platform version $version";
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
