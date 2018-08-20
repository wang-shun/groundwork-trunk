#!/usr/local/groundwork/perl/bin/perl -w

use strict;
use lib q(/usr/local/groundwork/nagios/libexec);
use cache_func ();
use nagios_func ();
use parse_func ();
use snmp_func ();

use constant PLUGIN => 'check_juniper_ive';

use constant OID => { 'logutil'      => '.1.3.6.1.4.1.12532.1.0',
                      'webusers'     => '.1.3.6.1.4.1.12532.2.0',
                      'mailusers'    => '.1.3.6.1.4.1.12532.3.0',
                      'platform'     => '.1.3.6.1.4.1.12532.6.0',
                      'version'      => '.1.3.6.1.4.1.12532.7.0',
                      'cpuutil'      => '.1.3.6.1.4.1.12532.10.0',
                      'memutil'      => '.1.3.6.1.4.1.12532.11.0',
                      'iveusers'     => '.1.3.6.1.4.1.12532.12.0',
                      'clusterusers' => '.1.3.6.1.4.1.12532.13.0',
                      'swaputil'     => '.1.3.6.1.4.1.12532.24.0',
                      'diskutil'     => '.1.3.6.1.4.1.12532.25.0',
                    };

use constant FUNCTIONS => { 'cpu'          => \&cpu,
                            'disk'         => \&disk,
                            'int_list'     => \&interface,
                            'mem'          => \&memory,
                            'uptime'       => \&uptime,
                            'users'        => \&users,
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
# cpu - check cpu utilization                                                  #
################################################################################
sub cpu {
   # set warning/critical thresholds
   my ($warn, $crit) = grep /^\d+$/ => split /:/ => $args->{l};
   $warn ||= 85;    # default 85%
   $crit ||= 95;    # default 95%

   # retrieve cpu utilization in percentage
   my $cpu = $snmp->snmpget( OID->{cpuutil} );
   
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
# disk - check disk utilization                                                #
################################################################################
sub disk {
   # set warning/critical thresholds
   my ($warn, $crit) = grep /^\d+$/ => split /:/ => $args->{l};
   $warn ||= 80;    # default 80%
   $crit ||= 90;    # default 90%

   # retrieve disk utilization in percentage
   my $disk = $snmp->snmpget( OID->{diskutil} );

   # test against thresholds and generate output
   if ($disk >= $crit) {
      print "CRITICAL - Disk utilization at $disk% (threshold $crit%)" .
            "|percent=$disk";
      exit 2;
   }
   elsif ($disk >= $warn) {
      print "WARNING - Disk utilization at $disk% (threshold $warn%)" .
            "|percent=$disk";
      exit 1;
   }
   else {
      print "OK - Disk utilization at $disk%|percent=$disk";
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
   # instantiate variables
   my @output = my @perfdata = ();
   
   # set warning/critical thresholds
   my ($warn, $crit) = grep /^\d+$/ => split /:/ => $args->{l};
   $warn ||= 25;    # default 25%
   $crit ||= 50;    # default 50%

   # retrieve version and memory/swap oids
   my ($version, $ram, $swap) = $snmp->snmpget( OID->{version},
      OID->{memutil}, OID->{swaputil} );
   push @perfdata, "ram=$ram";
   push @perfdata, "swap=$swap";

   # physical memory (ram) checking
   # version 6.3 has a memory leak; 35% warn and 40% crit for ram
   # otherwise its linux and we don't care how much ram it uses
   if ($version =~ /^6\.3/ && $ram >= 40) {
      push @output, "CRITICAL - RAM utilization at $ram% (threshold 40%)";
   }
   elsif ($version =~ /^6\.3/ && $ram >= 35) {
      push @output, "WARNING - RAM utilization at $ram% (threshold 35%)";
   }
   else {
      push @output, "OK - RAM utilization at $ram%";
   }

   # swap memory checking
   if ($swap >= $crit) {
      push @output, "CRITICAL - SWAP utilization at $swap% (threshold $crit%)";
   }
   elsif ($swap >= $warn) {
      push @output, "WARNING - SWAP utilization at $swap% (threshold $warn%)";
   }
   else {
      push @output, "OK - SWAP utilization at $swap%";
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
      print "OK - $ok memory healthy [@perfdata]|@perfdata\n";
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
# users - check number of concurrent vpn users                                 #
################################################################################
sub users {
   # instantiate variables
   my $users = { 'SA-700'  => 25,
                 'SA-750'  => 25,
                 'SA-2000' => 100,
                 'SA-2500' => 100,
                 'SA-3010' => 50,
                 'SA-3020' => 100,
                 'SA-4000' => 1000,
                 'SA-4500' => 1000,
                 'SA-6000' => 5000,
                 'SA-6500' => 10000,
               };

   # set warning/critical thresholds
   my ($warn, $crit) = grep /^\d+$/ => split /:/ => $args->{l};
   $warn ||= 75;    # default 75%
   $crit ||= 90;    # default 90%

   # retrieve platform and user oids
   my ($platform, $web, $mail, $ive, $cluster) = $snmp->snmpget( 
      OID->{platform}, OID->{webusers}, OID->{mailusers}, OID->{iveusers},
      OID->{clusterusers} );
   my @perfdata = ( "web=$web", "mail=$mail", "ive=$ive", "cluster=$cluster" );

   # locate the proper platform and retrieve maximum supported users
   my ($match) = grep { $platform =~ /$_/ } keys %$users;
   if (not defined $match) {
      print "UNKNOWN - Unable to match '$platform' to a known platform" .
            "|@perfdata";
      exit 3;
   }

   # calculate percentage of allowed ive users
   my $percent = sprintf "%d", 100 * $ive / $users->{$match};

   # test against thresholds and generate output
   if ($percent >= $crit) {
      print "CRITICAL - IVE current users at $ive of $users->{$match} or " .
            "$percent% utilization (threshold $crit%)|@perfdata";
      exit 2;
   }
   elsif ($percent >= $warn) {
      print "WARNING - IVE current users at $ive of $users->{$match} or " .
            "$percent% utilization (threshold $warn%)|@perfdata";
      exit 1;
   }
   else {
      print "OK - IVE current users at $ive of $users->{$match} or " .
            "$percent% utilization|@perfdata";
   }
}


################################################################################
# version - collect platform and version information                           #
################################################################################
sub version {
   my ($platform, $version) = $snmp->snmpget( OID->{platform}, OID->{version} );

  # my ($version) = (snmpget $oid{version})[0] =~ /^(\S+)/;
  # (my $platform = (snmpget $oid{platform})[0]) =~ tr/ \n//d;
   my $upgrade = { #'9.1.1' => 'Bug #7213',
                 };

   if (exists $upgrade->{$version}) {
      print "WARNING - Juniper code version $version should be upgraded: " .
            "$upgrade->{$version}";
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
