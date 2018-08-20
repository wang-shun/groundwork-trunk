#!/usr/local/groundwork/perl/bin/perl -w

use strict;
use lib q(/usr/local/groundwork/nagios/libexec);
use nagios_func ();
use parse_func ();
use snmp_func ();

use constant PLUGIN => 'check_bluecoat_director';

# Blue Coat Director (SGME) 5.x custom MIB provides only traps
use constant OID => { 'sysdescr' => '.1.3.6.1.2.1.1.1.0' };

use constant FUNCTIONS => { 'clock'    => \&clock,
                            'cpu'      => \&cpu,
                            'disk'     => \&disk,
                            'int_list' => \&interface,
                            'load'     => \&load,
                            'mem'      => \&memory,
                            'uptime'   => \&uptime,
                            'version'  => \&version,
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
# cpu - checks cpu utilization using ucDavis MIB                               #
# SG-ME supports both snmp_cpu and ucd_cpu however the only model at this time #
# is the Director 510 which is uni-processor so no reason to use SMP-capable   #
# snmp_cpu function                                                            #
################################################################################
sub cpu {
   return $snmp->ucd_cpu( $args );
}


################################################################################
# disk - checks disk/partition utilization using standard HOST-RESOURCE-MIB    #
################################################################################
sub disk {
   return $snmp->snmp_disk( $args );
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
      $int->{int_type} == 24 and next;   # skip softwareLoopback(24)
      $int->{int_name} =~ tr/\000//d;    # remove null characters 
      $int->{int_name} =~ tr/ /-/;       # convert spaces to hyphens
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
# load - retrieve linux load average using ucDavis MIB                         #
################################################################################
sub load {
   return $snmp->ucd_load( $args );
}


################################################################################
# memory - retrieve linux memory utilization using ucDavis MIB                 #
################################################################################
sub memory {
   return $snmp->ucd_memory( $args );
}


################################################################################
# uptime - check system uptime using HOST-RESOURCE-MIB                         #
################################################################################
sub uptime {
   return $snmp->snmp_uptime( $args );
}


################################################################################
# version - check director version                                             #
################################################################################
sub version {
   # retrieve snmpv2-mib sysdescr oid
   my $sysdescr = $snmp->snmpget( OID->{sysdescr} );

   # parse version from returned sysdescr
   my ($version) = $sysdescr =~ /^SG-ME \S+ ([0-9.]+)/;

   # alarm upgrade hash
   my $upgrade = { #'9.1.1' => 'Bug #7213',
                 };

   # generate output
   if (exists $upgrade->{$version}) {
      print "WARNING - Blue Coat Director $version should be upgraded: " .
            "$upgrade->{$version}";
      exit 1;
   }
   else {
      print "OK - Blue Coat Director $version";
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
