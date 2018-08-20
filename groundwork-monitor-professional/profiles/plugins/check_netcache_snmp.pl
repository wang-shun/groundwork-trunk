#!/usr/local/groundwork/perl/bin/perl -w

use strict;
use lib q(/usr/local/groundwork/nagios/libexec);
use nagios_func ();
use parse_func ();
use snmp_func ();

use constant PLUGIN => 'check_netcache';

use constant OID => { 'clientconns'   => '.1.3.6.1.4.1.789.1.8.3.6.5.0',
                      'cpuusage'      => '.1.3.6.1.4.1.789.1.2.1.3.0',
                      'diskfailed'    => '.1.3.6.1.4.1.789.1.6.4.7.0',
	              'diskfailedmsg' => '.1.3.6.1.4.1.789.1.6.4.10.0',
                      'diskindex'     => '.1.3.6.1.4.1.789.1.5.4.1.1',
	              'diskname'      => '.1.3.6.1.4.1.789.1.5.4.1.2',
                      'disktotalkb'   => '.1.3.6.1.4.1.789.1.5.4.1.3',
	              'diskused'      => '.1.3.6.1.4.1.789.1.5.4.1.6',
	              'enabled'       => '.1.3.6.1.4.1.789.1.8.1.1.0',
                      'envovertemp'   => '.1.3.6.1.4.1.789.1.2.4.1.0',
	              'failedfans'    => '.1.3.6.1.4.1.789.1.2.4.2.0',
	              'failedfanmsg'  => '.1.3.6.1.4.1.789.1.2.4.3.0',
	              'failedpsus'    => '.1.3.6.1.4.1.789.1.2.4.4.0',
	              'failedpsumsg'  => '.1.3.6.1.4.1.789.1.2.4.5.0',
	              'httpbwsavings' => '.1.3.6.1.4.1.789.1.8.3.6.6.0',
	              'httphitrate'   => '.1.3.6.1.4.1.789.1.8.3.6.7.0',
	              'httpreqrate'   => '.1.3.6.1.4.1.789.1.8.3.6.28.0',
	              'licensed'      => '.1.3.6.1.4.1.789.1.8.1.2.0',
	              'memtotal'      => '.1.3.6.1.4.1.789.1.8.3.13.1.1.0',
	              'memfree'       => '.1.3.6.1.4.1.789.1.8.3.13.1.2.0',
	              'nvrambattery'  => '.1.3.6.1.4.1.789.1.2.5.1.0',
                      'platform'      => '.1.3.6.1.4.1.789.1.1.5.0',
	              'raidindex'     => '.1.3.6.1.4.1.789.1.6.2.1.1.1.1',
	              'raidname'      => '.1.3.6.1.4.1.789.1.6.2.1.2.1.1',
	              'raidstate'     => '.1.3.6.1.4.1.789.1.6.2.1.3.1.1',
	              'serverconns'   => '.1.3.6.1.4.1.789.1.8.3.6.4.0',
                      'syshealth'     => '.1.3.6.1.4.1.789.1.2.2.4.0',
	              'syshealth2'    => '.1.3.6.1.4.1.789.1.2.2.25.0',
                      'version'       => '.1.3.6.1.2.1.1.1.0',
                    };

use constant FUNCTIONS => { 'conns'        => \&connections,
                            'cpu'          => \&cpu,
                            'disk'         => \&disk,
                            'int_list'     => \&interface,
                            'license'      => \&license,
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
# connections - retrieve client/server connections                             #
################################################################################
sub connections {
   # retrieve client/server connections
   my ($client, $server) = $snmp->snmpget( OID->{clientconns}, 
                                           OID->{serverconns} );
   
   # set perfdata variable
   my $perfdata = "client=$client server=$server";

   # generate output
   print "OK - Retrieved client/server connections[$perfdata]|$perfdata";
}


################################################################################
# cpu - check cpu utilization in percentage                                    #
################################################################################
sub cpu {
   # set warning/critical thresholds
   my ($warn, $crit) = grep /^\d+$/ => split /:/ => $args->{l};
   $warn ||= 85;    # default 85%
   $crit ||= 95;    # default 95%

   # retrieve cpu utilization in percentage
   my $cpu = $snmp->snmpget( OID->{cpuusage} );

   # detect wrapped counter
   if ($cpu < 0 || $cpu > 100) {
      print "UNKNOWN - CPU utilization cannot be determined due to wrapped " .
            "32-bit counter.   Please reboot at least every 497 days to " .
            "restore valid CPU utilization reporting.";
      exit 3;
   }

   # test against thresholds and generate output
   if ($cpu >= $crit) {
      print "CRITICAL - CPU usage at $cpu% (threshold $crit%)|percent=$cpu";
      exit 2;
   } 
   elsif ($cpu >= $warn) {
      print "WARNING - CPU usage at $cpu% (threshold $warn%)|percent=$cpu";
      exit 1;
   }
   else {
      print "OK - CPU usage at $cpu%|percent=$cpu";
   }
}


################################################################################
# disk - check disk utilization and raid health                                #
################################################################################
sub disk {
   # instantiate variables
   my @output = my @perfdata = ();
   my $raid = [ qw/NULL active reconstructionInProgress
                   parityReconstructionInProgress
                   parityVerificationInProgress
                   failed addingSpare spare prefailed/,
              ];

   # set warning/critical thresholds
   my ($warn, $crit) = grep /^\d+$/ => split /:/ => $args->{l};
   $warn ||= 80;    # default 80%
   $crit ||= 90;    # default 90%

   # check disk utilization
   my @diskindex = $snmp->snmpwalk( OID->{diskindex} );
   foreach my $index (@diskindex) {
      my $oid_name  = sprintf "%s.%s", OID->{diskname}, $index;
      my $oid_total = sprintf "%s.%s", OID->{disktotalkb}, $index;
      my $oid_used  = sprintf "%s.%s", OID->{diskused}, $index;
      my ($name, $total, $used) = $snmp->snmpget( $oid_name, $oid_total, 
                                                  $oid_used );
      $total or next;   # skip invalid partitions (0 bytes total)
      if ($used >= $crit) {
         push @output, "CRITICAL - Partition '$name' utilization at $used% " .
                       "(threshold $crit%)";
      }
      elsif ($used >= $warn) {
         push @output, "WARNING - Partition '$name' utilization at $used% " .
                       "(threshold $warn%)";
      }
      else {
         push @output, "OK - Partition '$name' utilization at $used%";
      }

      # format name for perfdata
      my ($perfname) = $name =~ m|/vol/(\w+)/|;
      push @perfdata, "$perfname=$used";
   } 
   
   # check raid status
   my @raidindex = $snmp->snmpwalk( OID->{raidindex} ); 
   foreach my $index (@raidindex) {
      my $oid_name  = sprintf "%s.%s", OID->{raidname}, $index;
      my $oid_state = sprintf "%s.%s", OID->{raidstate}, $index;
      my ($name, $state) = $snmp->snmpget( $oid_name, $oid_state );
      if ($state == 1) {
         push @output, "OK - RAID disk '$name' is $raid->[ $state ]";
      }
      else {
         push @output, "CRITICAL - RAID disk '$name' is $raid->[ $state ]";
      }
   }

   # check for failed disks
   my ($failed, $msg) = $snmp->snmpget( OID->{diskfailed}, 
                                        OID->{diskfailedmsg} );
   if ($failed > 0) {
      push @output, "CRITICAL - $failed disks have failed: $msg";
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
      my $ok = scalar @sorted;
      print "OK - $ok blocks healthy|@perfdata\n";
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
# license - check license status                                               #
################################################################################
sub license {
   # instantiate variables
   my @output = ();

   # retrieve enabled/licensed status
   # 1 == false
   # 2 == true
   my ($enabled, $licensed) = $snmp->snmpget( OID->{enabled}, OID->{licensed} );

   # check netcache status
   if ($enabled == 2) {
      push @output, "OK - NetCache is enabled";
   }
   elsif ($enabled == 1) {
      push @output, "CRITICAL - NetCache is disabled";
   }

   # check netcache license
   if ($licensed == 2) {
      push @output, "OK - NetCache is licensed";
   }
   elsif ($licensed == 1) {
      push @output, "CRITICAL - NetCache not licensed";
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
      my $ok = scalar @sorted;
      print "OK - $ok checks healthy\n";
      print join "\n" => @sorted;
   }
}


################################################################################
# memory - check memory utilization in percentage                              #
################################################################################
sub memory {
   # set warning/critical thresholds
   my ($warn, $crit) = grep /^\d+$/ => split /:/ => $args->{l};
   $warn ||= 80;    # default 80%
   $crit ||= 90;    # default 90%

   # retrieve memory statistics
   my ($total, $free) = $snmp->snmpget( OID->{memtotal}, OID->{memfree} );

   # calculate percentage in use
   my $used = sprintf "%d", ($total - $free) / $total * 100;

   if ($used >= $crit) {
      print "CRITICAL - Memory utilization at $used% (threshold $crit%)" .
            "|percent=$used";
      exit 2;
   }
   elsif ($used >= $warn) {
      print "WARNING - Memory utilization at $used% (threshold $warn%)" .
            "|percent=$used";
      exit 1;
   }
   else {
      print "OK - Memory utilization at $used%|percent=$used";
   }
}


################################################################################
# sensors - check chassis sensors                                              #
################################################################################
sub sensors {
   # instantiate variables
   my @output = ();
   my $status = [ qw/null other unknown ok nonCritical critical nonRecoverable/ ];
   my $nvram  = [ qw/null ok partiallyDischarged fullyDischarged notPresent
                     nearEndOfLife atEndOfLife unknown/ ];

   my ($syshealth, $syshealth2) = $snmp->snmpget( OID->{syshealth}, 
                                                  OID->{syshealth2} );

   # check overall system health
   if ($syshealth == 3) {
      push @output, "OK - System status is $status->[ $syshealth ]: " .
                    $syshealth2;
   }
   elsif ($syshealth == 4) {
      push @output, "WARNING - System status is $status->[ $syshealth ]: " .
                    $syshealth2;
   }
   else {
      push @output, "CRITICAL - System status is $status->[ $syshealth ]: " .
                    $syshealth2;
   }

   # check environmental temperature
   my $envovertemp = $snmp->snmpget( OID->{envovertemp} );
   if ($envovertemp == 2) {
      push @output, "CRITICAL - System temperature alarm";
   }
   else {
      push @output, "OK - System temperature healthy";
   }

   # check fans
   my ($failedfans, $failedfanmsg) = $snmp->snmpget( OID->{failedfans},
                                                     OID->{failedfanmsg} );
   if ($failedfans) {
      push @output, "CRITICAL - $failedfans fans have failed: $failedfanmsg";
   }
   else {
      push @output, "OK - Fans healthy";
   }

   # check power supplies
   my ($failedpsus, $failedpsumsg) = $snmp->snmpget( OID->{failedpsus},
                                                     OID->{failedpsumsg} );
   if ($failedpsus) {
      push @output, "CRITICAL - $failedpsus power supplies have failed: " .
                    $failedpsumsg;
   }
   else {
      push @output, "OK - Power supplies healthy";
   }

   # check nvram battery
   my $nvrambattery = $snmp->snmpget( OID->{nvrambattery} );
   if ($nvrambattery == 1) {
      push @output, "OK - NVRAM battery is $nvram->[ $nvrambattery ]";
   }
   else {
      push @output, "CRITICAL - NVRAM battery is $nvram->[ $nvrambattery ]";
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
      my $ok = scalar @sorted;
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
# version - retrieve host platform and version                                 #
################################################################################
sub version {
   my ($platform, $version) = $snmp->snmpget( OID->{platform}, OID->{version} );
   ($version) = $version =~ /Release ([0-9.]+)/;

   my $upgrade = { #'9.1.1' => 'Bug #7213',
                 };

   if (exists $upgrade->{$version}) {
      print "WARNING - NetCache $platform version $version should be " .
            "upgraded: $upgrade->{$version}";
      exit 1;
   }
   else {
      print "OK - NetCache $platform version $version";
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
