#!/usr/local/groundwork/perl/bin/perl -w

use strict;
use lib q(/usr/local/groundwork/nagios/libexec);
use nagios_func ();
use parse_func ();
use snmp_func ();

use constant PLUGIN => 'check_hpblade';

use constant OID => { 'platform'  => '.1.3.6.1.4.1.232.22.2.3.1.1.1.3.1',
                      'version'   => '.1.3.6.1.4.1.232.22.2.3.1.1.1.8.1',

                      'enchealth' => '.1.3.6.1.4.1.232.22.2.3.1.1.1.16.1',
                      'hasblades' => '.1.3.6.1.4.1.232.22.2.3.1.1.1.17.1',
                      'haspsu'    => '.1.3.6.1.4.1.232.22.2.3.1.1.1.18.1',
                      'hasnet'    => '.1.3.6.1.4.1.232.22.2.3.1.1.1.19.1',
                      'hastemp'   => '.1.3.6.1.4.1.232.22.2.3.1.1.1.20.1',
                      'hasfans'   => '.1.3.6.1.4.1.232.22.2.3.1.1.1.21.1',
                      'hasfuses'  => '.1.3.6.1.4.1.232.22.2.3.1.1.1.22.1',
                      'hasman'    => '.1.3.6.1.4.1.232.22.2.3.1.1.1.33.1',

                      'fanindex'  => '.1.3.6.1.4.1.232.22.2.3.1.3.1.3',
                      'fanloc'    => '.1.3.6.1.4.1.232.22.2.3.1.3.1.5',
                      'fancond'   => '.1.3.6.1.4.1.232.22.2.3.1.3.1.11',
              
                      'manindex'  => '.1.3.6.1.4.1.232.22.2.3.1.6.1.3', 
                      'manloc'    => '.1.3.6.1.4.1.232.22.2.3.1.6.1.5',
                      'mancond'   => '.1.3.6.1.4.1.232.22.2.3.1.6.1.12',

                      'encpower'  => '.1.3.6.1.4.1.232.22.2.3.3.1.1.9.1',
 
                      'psuindex'  => '.1.3.6.1.4.1.232.22.2.5.1.1.1.3',
                      'psuloc'    => '.1.3.6.1.4.1.232.22.2.5.1.1.1.11',
                      'psucond'   => '.1.3.6.1.4.1.232.22.2.5.1.1.1.17',
                    };

use constant FUNCTIONS => { 'health'       => \&health,
                            'int_list'     => \&interface,
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
# health - enclosure health/sensor checking                                    #
################################################################################
sub health {
   # instantiate variables
   my @output = ();
   my $status = { 1 => 'other',
                  2 => 'ok',
                  3 => 'degraded',
                  4 => 'failed',
                };

   # check enclosure overall health
   my $enchealth = $snmp->snmpget( OID->{enchealth} );
   if ($enchealth > 2) {
      # according to the MIB a "failed" state will never occur because the
      # enclosure will automatically shut itself down
      push @output, "CRITICAL - Enclosure condition is $status->{ $enchealth }";
   }
   elsif ($enchealth == 2) {
      push @output, "OK - Enclosure condition is $status->{ $enchealth }";
   }
   elsif ($enchealth == 1) {
      push @output, "WARNING - Enclosure condition is $status->{ $enchealth }";
   }

   # check enclosure overall power health
   my $encpower = $snmp->snmpget( OID->{encpower} );
   if ($encpower > 2) {
      push @output, "CRITICAL - Enclosure power is $status->{ $encpower }";
   }
   elsif ($encpower == 2) {
      push @output, "OK - Enclosure power is $status->{ $encpower }";
   }
   elsif ($encpower == 1) {
      push @output, "WARNING - Enclosure condition is $status->{ $encpower }";
   }

   # check enclosure fans
   my $hasfans = $snmp->snmpget( OID->{hasfans} );
   if ($hasfans == 2) {
      my @fanindex = $snmp->snmpbulkwalk( OID->{fanindex} );
      foreach my $index (@fanindex) {
         my $oid_fanloc   = sprintf "%s.%s", OID->{fanloc}, $index;
         my $oid_fancond  = sprintf "%s.%s", OID->{fancond}, $index;
         my ($fanloc, $fancond) = $snmp->snmpget( $oid_fanloc, $oid_fancond );
         if ($fancond > 2) {
            push @output, "CRITICAL - Fan $fanloc is $status->{ $fancond }";
         }
         elsif ($fancond == 2) {
            push @output, "OK - Fan $fanloc is $status->{ $fancond }";
         }
         elsif ($fancond == 1) {
            push @output, "WARNING - Fan $fanloc is $status->{ $fancond }";
         }
      }
   }
   else {
      push @output, "OK - Enclosure reports there are no fans installed";
   }

   # check manager blades
   my $hasman = $snmp->snmpget( OID->{hasman} );
   if ($hasman == 2) {
      my @manindex = $snmp->snmpbulkwalk( OID->{manindex} );
      $snmp->{ snmpget_timeout } = 5;
      foreach my $index (@manindex) {
         my $oid_manloc  = sprintf "%s.%s", OID->{manloc}, $index;
         my $oid_mancond = sprintf "%s.%s", OID->{mancond}, $index;
         my ($manloc, $mancond) = $snmp->snmpget( $oid_manloc, $oid_mancond );
         if ($mancond > 2) {
            push @output, "CRITICAL - Manager blade $manloc is " .
                          "$status->{ $mancond }";
         }
         elsif ($mancond == 2) {
            push @output, "OK - Manager blade $manloc is $status->{ $mancond }";
         }
         elsif ($mancond == 1) {
            push @output, "WARNING - Manager blade $manloc is " .
                          "$status->{ $mancond }";
         }
      }
      $snmp->{ snmpget_timeout } = 2;
   }
   else {
      push @output, "OK - Enclosure reports there are no manager blades " .
                    "installed";
   }

   # check power supplies
   my $haspsu = $snmp->snmpget( OID->{haspsu} );
   if ($haspsu == 2) {
      my @psuindex = $snmp->snmpbulkwalk( OID->{psuindex} );
      foreach my $index (@psuindex) {
         my $oid_psuloc  = sprintf "%s.%s", OID->{psuloc}, $index;
         my $oid_psucond = sprintf "%s.%s", OID->{psucond}, $index;
         my ($psuloc, $psucond) = $snmp->snmpget( $oid_psuloc, $oid_psucond );
         if ($psucond > 2) {
            push @output, "CRITICAL - Power supply $psuloc is " .
                          "$status->{ $psucond }";
         }
         elsif ($psucond == 2) {
            push @output, "OK - Power supply $psuloc is $status->{ $psucond }";
         }
         elsif ($psucond == 1) {
            push @output, "WARNING - Power supply $psuloc is " .
                          "$status->{ $psucond }";
         }
      }
   }
   else {
      push @output, "OK - Enclosure reports there are no power supplies " .
                    "installed";
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
      print "OK - $ok enclosure checks healthy\n";
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
# uptime - check system uptime using HOST-RESOURCE-MIB                         #
################################################################################
sub uptime {
   return $snmp->snmp_uptime( $args );
}


################################################################################
# version - collect platform and version information                           #
################################################################################
sub version {
   my ($platform, $version) = $snmp->snmpget( OID->{platform}, OID->{version} );
   my $upgrade = { #'9.1.1' => 'Bug #7213',
                 };

   if (exists $upgrade->{$version}) {
      print "WARNING - HP BladeSystem version $version should be " .
            "upgraded: $upgrade->{$version}";
      exit 1;
   }
   else {
      print "OK - HP $platform version $version";
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
