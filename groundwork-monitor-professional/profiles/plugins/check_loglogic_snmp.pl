#!/usr/local/groundwork/perl/bin/perl -w

use strict;
use lib q(/usr/local/groundwork/nagios/libexec);
use cache_func ();
use nagios_func ();
use parse_func ();
use snmp_func ();

use constant PLUGIN => 'check_loglogic';

use constant OID => { product   => '.1.3.6.1.4.1.18552.1.2.1.11.0',
                      model     => '.1.3.6.1.4.1.18552.1.2.1.12.0',
                      msgdrop   => '.1.3.6.1.4.1.18552.1.2.1.15.0',
                      syslogsrc => '.1.3.6.1.4.1.18552.1.2.1.19.0',
                      syslogmsg => '.1.3.6.1.4.1.18552.1.2.1.25.0',
                      leamsg    => '.1.3.6.1.4.1.18552.1.2.1.26.0',
                      msgproc   => '.1.3.6.1.4.1.18552.1.2.1.27.0',
                      msgpersec => '.1.3.6.1.4.1.18552.1.2.1.51.0',
                      fans      => '.1.3.6.1.4.1.18552.1.2.1.61.0',
                    };

use constant FUNCTIONS => { cpu      => \&cpu,
                            disk     => \&disk,
                            int_list => \&interface,
                            load     => \&load,
                            mem      => \&memory,
                            messages => \&messages,
                            uptime   => \&uptime,
                            version  => \&version,
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
# cpu - check cpu utilization from ucDavis MIB                                 #
################################################################################
sub cpu {
   return $snmp->ucd_cpu( $args );
}


################################################################################
# disk - check disk utilization from HOST-RESOURCE-MIB                         #
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
      $int->{int_name} =~ s/^(\S+).*/$1/;
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
# load - check system load averages from ucDavis MIB                           #
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
# messages - check message rates                                               #
################################################################################
sub messages {
   # instantiate variables
   my @output = ();
   my $overutil = 0;
   my $sources = 16000;
   my $models = { LX510  => 500,
                  LX820  => 1500,
                  LX1010 => 1500,
                  LX1020 => 5000,
                  LX2010 => 4000,
                  LX4020 => 10000,
                  ST1020 => 75000,
                  ST2010 => 75000,
                  ST2020 => 160000,
                  ST3010 => 75000,
                  ST4020 => 150000,
                };

   # check for message limits (max sources and msg/sec)
   my ($model, $syslogsrc, $msgpersec) = $snmp->snmpget( OID->{model}, 
      OID->{syslogsrc}, OID->{msgpersec} );

   if ($models->{$model}) {
      if ($syslogsrc > $sources) {
         push @output, "CRITICAL - Messages sources $syslogsrc exceeds " .
                       "$sources limit";
         $overutil = 1;
      }
      else {
         push @output, "OK - Message sources at $syslogsrc";
      }

      if ($msgpersec > $models->{$model}) {
         push @output, "WARNING - Message rate $msgpersec/sec exceeds " .
                       "maximum sustained limit of $models->{$model}/sec";
         $overutil = 1;
      }
      else {
         push @output, "OK - Message rate at $msgpersec/sec";
      }
   }
   else {
      push @output, "UNKNOWN - Unknown LogLogic model $model";
   }

   # capture received/dropped msg counters for graphing 
   my ($msgdrop, $msgproc) = $snmp->snmpget( OID->{msgdrop}, OID->{msgproc} );
   my @perfdata = ( "dropped=$msgdrop", "processed=$msgproc" );

   # retrieve cached drop counter
   my $cache = cache_func->new( $args->{h} );
   my $cached = $cache->get( 'msgdrop' );
   $cache->set( 'msgdrop', $msgdrop );

   if ($overutil && $cached && $msgdrop > $cached) {
      push @output, "CRITICAL - Device overutilization is resulting in " .
                    "dropped messages";
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
      print "OK - $ok message checks healthy|@perfdata";
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
# version - retrieve system model (version not available via SNMP)
################################################################################
sub version {
   my $model = $snmp->snmpget( OID->{model} );
   print "OK - LogLogic $model";
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
