#!/usr/local/groundwork/perl/bin/perl -w

use strict;
use lib q(/usr/local/groundwork/nagios/libexec);
use cache_func ();
use nagios_func ();
use parse_func ();
use snmp_func ();

use constant PLUGIN => 'check_f5_firepass';

use constant OID => { clusterconf     => '.1.3.6.1.4.1.3375.1.10.250.1.0',
                      clusternodes    => '.1.3.6.1.4.1.3375.1.10.250.2.0',
                      failoverconf    => '.1.3.6.1.4.1.3375.1.10.200.1.0',
                      failoverstatus  => '.1.3.6.1.4.1.3375.1.10.200.2.0',
                      failoversync    => '.1.3.6.1.4.1.3375.1.10.200.3.0',
                      hrdevicetypes   => '.1.3.6.1.2.1.25.3.2.1.2',
                      usersessions    => '.1.3.6.1.4.1.3375.1.10.100.3.0',
                    };

use constant FUNCTIONS => { clock        => \&clock,
                            cpu          => \&cpu,
                            disk         => \&disk,
                            failover     => \&failover,
                            int_list     => \&interface,
		            load         => \&load,
                            mem          => \&memory,
                            users        => \&users,
                            uptime       => \&uptime,
                            version      => \&version,
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
# cpu - checks cpu utilization using ucDvais MIB; no smp support               #
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
# failover - check for device failover
################################################################################
sub failover {
   # instantiate variables
   my @output = ();
   my $states = { 0 => 'not-defined', 1 => 'primary', 2 => 'standby' };
   my $syncs  = { 0 => 'not-defined', 1 => 'in-sync', 2 => 'out-of-sync' };

   # retrieve failover OIDs
   # sometimes they return no such instance on first attempt
   # so we will use snmpget_or to check same oid twice
   my $conf   = $snmp->snmpget_or( OID->{failoverconf}, OID->{failoverconf} );
   my $status = $snmp->snmpget_or( OID->{failoverstatus}, OID->{failoverstatus} );
   my $sync   = $snmp->snmpget_or( OID->{failoversync}, OID->{failoversync} );

   # exit check if not a failover member
   if ($conf == 0) {
      print "OK - Standalone device";
      return;
   }

   # retrieve cached status
   my $cache = cache_func->new( $args->{h} );
   my $cached = $cache->get( 'status' );
   $cache->set( 'status', $status );

   # check for failover state change
   if (defined $cached && $status != $cached) {
      push @output, "CRITICAL - Failover state changed from " .
                    "$states->{$cached} to $states->{$status}";
   }
   else {
      push @output, "OK - Failover state is $states->{$status}";
   }

   # check for failover sync
   if ($sync == 1) {
      push @output, "OK - Failover peer configuration is $syncs->{$sync}";
   }
   else {
      push @output, "WARNING - Failover peer configuration is $syncs->{$sync}";
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
# load - retrieve linux load average using ucDavis MIB                         #
################################################################################
sub load {
   return $snmp->ucd_load( $args );
}


################################################################################
# memory - check memory utilization from HOST-RESOURCE-MIB                     #
################################################################################
sub memory {
   return $snmp->snmp_memory( $args );
}


################################################################################
# sessions - check concurrent sessions
################################################################################
sub users {
   # instantiate variables
   # define maximum users based on model
   my $maxusers = { 1200 => 100,
                    4100 => 2000,
                    4300 => 2000,
                  };

   # set warning/critical thresholds
   my ($warn, $crit) = grep /^\d+$/ => split /:/ => $args->{l};
   $warn ||= 80;    # default 80%
   $crit ||= 90;    # default 90%

   # retrieve the model (series)
   my $model = get_model_by_processor();

   # retrieve the number of concurrent users
   my $users    = $snmp->snmpget( OID->{usersessions} );
   my $perfdata = "concurrent=$users";

   # calculate user thresholds
   my $warn_users = sprintf "%d", $maxusers->{ $model } * $warn / 100;
   my $crit_users = sprintf "%d", $maxusers->{ $model } * $crit / 100;

   # test against thresholds and generate output
   if ($users >= $crit_users) {
      print "CRITICAL - Concurrent users at $users (threshold $crit_users)" .
            "|$perfdata";
      exit 2;
   }
   elsif ($users >= $warn_users) {
      print "WARNING - Concurrent users at $users (threshold $warn_users)" .
            "|$perfdata";
      exit 1;
   }
   else {
      print "OK - Concurrent users at $users|$perfdata";
   }
}


################################################################################
# uptime - check system uptime using HOST-RESOURCE-MIB                         #
################################################################################
sub uptime {
   return $snmp->snmp_uptime( $args );
}


################################################################################
# version - retrieve system model (version not available via SNMP)
################################################################################
sub version {
   my $model = get_model_by_processor();
   print "OK - F5 FirePass $model";
}


################################################################################
# get_model_by_processor - determine model of device based on processor count  #
# firepass devices dont have a OID to show what model or code version          #
# so we will count processors and reverse map to a device model (series)       #
################################################################################
sub get_model_by_processor {
   my $model = { qw/ 1 1200 2 4100 4 4300 / };
   my @hrdevicetypes = $snmp->snmpbulkwalk( OID->{hrdevicetypes} );
   my $count = grep { $_ eq '.1.3.6.1.2.1.25.3.1.3' } @hrdevicetypes;
   return $model->{ $count };
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
