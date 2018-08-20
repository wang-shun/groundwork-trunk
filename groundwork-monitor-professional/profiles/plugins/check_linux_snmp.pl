#!/usr/local/groundwork/perl/bin/perl -w

use strict;
use lib q(/usr/local/groundwork/nagios/libexec);
use nagios_func ();
use parse_func ();
use snmp_func ();

use constant PLUGIN => 'check_linux_snmp';

use constant FUNCTIONS => { 'clock'    => \&clock,
                            'cpu'      => \&cpu,
                            'disk'     => \&disk,
                            'int_list' => \&interface,
                            'load'     => \&load,
                            'mem'      => \&memory,
                            'proc'     => \&processes,
                            'uptime'   => \&uptime,
                          };

use constant OPTIONS => { 'c'  => 'Community string',
                          'h'  => 'Hostname',
                          'i'  => 'IP address',
                          'l?' => 'Levels [warning:critical]',
                          'p?' => 'Process name',
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
# cpu - checks cpu utilization using HOST-RESOURCE-MIB                         #
################################################################################
sub cpu {
   # test whether HOST-RESOURCE-MIB will work
   $snmp->{ec} = 0;   # disable snmp error-checking
   my $procs = () = $snmp->snmpbulkwalk( '.1.3.6.1.2.1.25.3.3.1.2' );
   $snmp->{ec} = 1;   # enable snmp error-checking

   if ($procs) {
      return $snmp->snmp_cpu( $args );
   }
   else {
      return $snmp->ucd_cpu( $args );
   }
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
      $int->{int_type} == 6 or next;
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
# load - retrieve system load averages using ucDavis MIB                       #
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
# processes - check for system processes using HOST-RESOURCE-MIB               #
################################################################################
sub processes {
   return $snmp->snmp_proc( $args );
}


################################################################################
# uptime - check system uptime using HOST-RESOURCE-MIB                         #
################################################################################
sub uptime {
   return $snmp->snmp_uptime( $args );
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
