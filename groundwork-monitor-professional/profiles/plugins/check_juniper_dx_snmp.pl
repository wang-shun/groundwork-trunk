#!/usr/local/groundwork/perl/bin/perl -w

use strict;
use lib q(/usr/local/groundwork/nagios/libexec);
use nagios_func ();
use parse_func ();
use snmp_func ();

use constant PLUGIN => 'check_juniper_dx';

use constant OID => { 'platform'    => '.1.3.6.1.4.1.6213.2.2.1.0',
                      'version'     => '.1.3.6.1.4.1.6213.2.2.3.0',
                      'sessions'    => '.1.3.6.1.4.1.6213.2.4.1.1.2.0',
                      'requests'    => '.1.3.6.1.4.1.6213.2.4.1.2.2.0',
                      'connections' => '.1.3.6.1.4.1.6213.2.4.1.4.2.0',
                      'cpupercent'  => '.1.3.6.1.4.1.6213.2.4.1.4.9.0',
                      'mempercent'  => '.1.3.6.1.4.1.6213.2.4.1.4.10.0',
                    };

use constant FUNCTIONS => { 'cpu'          => \&cpu,
                            'int_list'     => \&interface,
                            'mem'          => \&memory,
                            'stats'        => \&statistics,
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

   # retrieve CPU utilization in percentage
   my $cpu = $snmp->snmpget( OID->{cpupercent} );
  
   # test against thresholds and generate output 
   if ($cpu >= $crit) {
      print "CRITICAL - CPU utilization at $cpu% (threshold $crit%)" .
            "|cpu0=$cpu";
      exit 2;
   }
   elsif ($cpu >= $warn) {
      print "WARNING - CPU utilization at $cpu% (threshold $warn%)" .
            "|cpu0=$cpu";
      exit 1;
   }
   else {
      print "OK - CPU utilization at $cpu%|cpu0=$cpu";
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
# memory - check memory utilization                                            #
################################################################################
sub memory {
   # set warning/critical thresholds
   my ($warn, $crit) = grep /^\d+$/ => split /:/ => $args->{l};
   $warn ||= 80;    # default 80%
   $crit ||= 90;    # default 90%

   # retrieve memory utilization in percentage
   my $memory = $snmp->snmpget( OID->{mempercent} );
   
   # test against thresholds and generate output
   if ($memory >= $crit) {
      print "CRITICAL - Memory utilization at $memory% (threshold $crit%)" .
            "percent=$memory";
      exit 2;
   }
   elsif ($memory >= $warn) {
      print "WARNING - Memory utilization at $memory% (threshold $warn%)" .
            "percent=$memory";
      exit 1;
   }
   else {
      print "OK - Memory utilization at $memory%|percent=$memory";
   }
}


################################################################################
# statistics - collect session, request and connection statistics              #
################################################################################
sub statistics {
   # retrieve session, request and connection counts
   my ($sess, $req, $conn) = $snmp->snmpget( OID->{sessions}, OID->{requests},
      OID->{connections} );
   my @perfdata = ( "sessions=$sess", "requests=$req", "connections=$conn" );

   # generate output
   print "OK - Performance statistics collected|@perfdata\n";
   print "Sessions:    $sess\n";
   print "Requests:    $req\n";
   print "Connections: $conn\n";
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
      print "WARNING - Juniper DX code version $version should be " .
            "upgraded: $upgrade->{$version}";
      exit 1;
   }
   else {
      print "OK - Juniper $platform version $version";
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
