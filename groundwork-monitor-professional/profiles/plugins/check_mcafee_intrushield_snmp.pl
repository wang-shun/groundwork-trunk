#!/usr/local/groundwork/perl/bin/perl -w

use strict;
use lib q(/usr/local/groundwork/nagios/libexec);
use cache_func ();
use nagios_func ();
use parse_func ();
use snmp_func ();

use constant PLUGIN => 'check_mcafee_intrushield';

use constant OID => { alertchan  => '.1.3.6.1.4.1.8962.2.1.2.1.1.11.0',
                      diskleft   => '.1.3.6.1.4.1.8962.2.1.2.1.1.10.0',
                      failmode   => '.1.3.6.1.4.1.8962.2.1.2.1.3.3.0',
                      failstatus => '.1.3.6.1.4.1.8962.2.1.2.1.3.1.0',
                      fanstatus  => '.1.3.6.1.4.1.8962.2.1.2.1.7.2.0',
                      freeflows  => '.1.3.6.1.4.1.8962.2.1.3.1.1.1.1.9.2',
                      health	 => '.1.3.6.1.4.1.8962.2.1.2.1.1.13.0',
                      ifdescr    => '.1.3.6.1.4.1.8962.2.1.2.1.11.1.1.1.2',
                      loadavg    => '.1.3.6.1.4.1.8962.2.1.3.1.1.5.1.1.2',
                      maxflows   => '.1.3.6.1.4.1.8962.2.1.3.1.1.1.1.1.2',
                      pktlogchan => '.1.3.6.1.4.1.8962.2.1.2.1.1.12.0',
                      platform   => '.1.3.6.1.4.1.8962.2.1.2.1.1.4.0',
                      psu1status => '.1.3.6.1.4.1.8962.2.1.2.1.7.3.0',
                      psu2status => '.1.3.6.1.4.1.8962.2.1.2.1.7.4.0',
                      resdrops   => '.1.3.6.1.4.1.8962.2.1.3.1.2.2.1.22.2',
                      tcpflows   => '.1.3.6.1.4.1.8962.2.1.3.1.1.1.1.2.2',
                      tempstatus => '.1.3.6.1.4.1.8962.2.1.2.1.7.1.0',
                      udpflows   => '.1.3.6.1.4.1.8962.2.1.3.1.1.1.1.3.2',
                      version    => '.1.3.6.1.4.1.8962.2.1.2.1.8.1.1.5.1',
                    };

use constant FUNCTIONS => { cpu      => \&cpu,
                            disk     => \&disk,
                            drops    => \&drops,
                            ems_conn => \&ems_connectivity,
                            failover => \&failover,
                            flows    => \&flows,
                            sensors  => \&sensors,
                            uptime   => \&uptime,
                            version  => \&version,
                          };

use constant OPTIONS => { 'a'  => 'SNMPv3 authentication passphrase',
                          'h'  => 'Hostname',
                          'i'  => 'IP address',
                          'l?' => 'Levels [warning:critical]',
                          'p'  => 'SNMPv3 privacy passphrase',
                          't'  => { 'Type of check' => FUNCTIONS },
                          'u'  => 'SNMPv3 username',
                          'v'  => 'SNMP version [3]',
                        };

my $args = parse_func->new(\@ARGV, OPTIONS);
my $snmp = snmp_func->new( host         => $args->{i},
                           version      => $args->{v},
                           user         => $args->{u},
                           authpassword => $args->{a},
                           privpassword => $args->{p},
                           callback     => \&callback_check_snmp,
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

   # retrieve cpu utilization in percentage
   my $cpu = $snmp->snmpget( OID->{loadavg} );

   # test against thresholds and generate output
   if ($cpu >= $crit) {
      print "CRITICAL - CPU utilization at $cpu% (threshold $crit%)" .
            "|percent=$cpu";
      exit 2;
   }
   elsif ($cpu >= $warn) {
      print "WARNING - CPU utilization at $cpu% (threshold $warn%)" .
            "|percent=$cpu";
      exit 1;
   }
   else {
      print "OK - CPU utilization at $cpu%|percent=$cpu";
   }
}


################################################################################
# disk - check disk utilization in kB free                                     #
################################################################################
sub disk {
   # set warning/critical thresholds
   my ($warn, $crit) = grep /^\d+$/ => split /:/ => $args->{l};
   $warn ||= 100000;    # default 100 MB
   $crit ||= 50000;     # default 50 MB

   # retrieve disk space left in kB
   my $diskleft = $snmp->snmpget( OID->{diskleft} );

   my $perfdata = "kb_free=$diskleft";
   # test against thresholds and generate output
   if ($diskleft <= $crit) {
      print "CRITICAL - Low disk space remaining $diskleft kB " .
            "(threshold $crit kB)|kb_free=$diskleft";
      exit 2;
   }
   elsif ($diskleft <= $warn) {
      print "WARNING - Low disk space remaining $diskleft kB " .
            "(threshold $warn kB)|kb_free=$diskleft";
      exit 1;
   }
   else {
      print "OK - Disk space at $diskleft kB free|kb_free=$diskleft";
   }
}


################################################################################
# drops - check for interface packet drops                                     #
################################################################################
sub drops {
   # instantiate variables
   my @output = my @perfdata = ();

   # retrieve interface names
   my @ifdescr = $snmp->snmpbulkwalk( OID->{ifdescr} );

   # retrieve interface drops
   my @drops = $snmp->snmpbulkwalk( OID->{resdrops} );

   # get/set cached drops
   my $cache = cache_func->new( $args->{h} );
   my $cached = $cache->get( 'drops' );
   $cache->set( 'drops', \@drops );

   # loop through each interface
   foreach my $i (0 .. $#drops) {
      # skip the interface if it has no name
      defined $ifdescr[$i] or next;

      # check for incrementing packet drops
      if ($cached->[$i] && $drops[$i] > $cached->[$i]) {
         push @output, "CRITICAL - Interface $ifdescr[$i] drops " .
                       "incrementing ($drops[$i])";
      }
      else {
         push @output, "OK - Interface $ifdescr[$i] drop sholding ($drops[$i])";
      }

      # populat perfdata
      push @perfdata, "$ifdescr[$i]=$drops[$i]";
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
      print "OK - $ok interfaces healthy|@perfdata\n";
      print join "\n" => @sorted;
   }
}


################################################################################
# ems_connectivity - check communication to ems manager                        #
################################################################################
sub ems_connectivity {
   # instantiate variables
   my @output = ();
   my $alertstatus = [ qw/down up errorInGetTimeFromManager 
                          errorGeneratingCertificates errorPersistingCertificates
                          errorConnectingToManager errorInUntrustedConnectionSetup
                          errorInInstall errorPersistingManagerPublicCertificate 
                          errorInMutualTrustMatch errorInSnmpKeyExchange 
                          errorInInitialProtocolMessageExchange 
                          sensorInstallInProgress openingAlertChannelInProgress 
                          errorInLinkHenceReopening errorInChannelReopening 
                          closingChannelInProgress errorClosingChannel 
                          sendAlertWarning keepAliveWarning errorDeletingCerts 
                          errorCreatingSnmpUser errorChangingSnmpUserKeys/,
                     ];
   my $pktlogstatus = [ qw/down up errorInGetTimeFromManager 
                           errorGeneratingCertificates errorPersistingCertificates
                           errorConnectingToManager 
                           errorInUntrustedConnectionSetup errorInInstall 
                           errorPersistingManagerPublicCertificate 
                           errorInMutualTrustMatch errorInSnmpKeyExchange 
                           errorInInitialProtocolMessageExchange 
                           packetLogInstallInProgress openingPacketLogInProgress 
                           errorInLinkHenceReopening errorInChannelReopening 
                           closingChannelInProgress errorClosingChannel 
                           sendLogWarning keepAliveWarning/,
                      ];

   # retrieve status of alert and packet log channels
   my ($alertchan, $pktlogchan) = $snmp->snmpget( OID->{alertchan}, 
                                                  OID->{pktlogchan} );

   # check alert channel
   if ($alertchan == 1) {
      push @output, "OK - Sensor to EMS alert channel is up";
   }
   else {
      push @output, "CRITICAL - Sensor to EMS alert channel is " .
                    $alertstatus->[$alertchan];
   }
 
   # check packet log channel 
   if ($pktlogchan == 1) {
      push @output, "OK - Sensor to EMS packet log channel is up";
   }
   else {
      push @output, "CRITICAL - Sensor to EMS packet log channel is " .
                    $pktlogstatus->[$pktlogchan];
   }

   # generate output  
   my @sorted = sort nagios_func::nagsort @output;
   if (grep /CRITICAL/ => @sorted) { 
      print shift(@sorted), "\n";
      print join "\n" => @sorted;
      exit 2;
   }
   elsif (grep /WARNING/ => @sorted) {
      print shift(@sorted), "\n";
      print join "\n" => @sorted;
      exit 1;
   }
   else {
      my $ok = @sorted;
      print "OK - $ok channels healthy\n";
      print join "\n" => @sorted;
   }
}


################################################################################
# failover - check failover state                                              #
################################################################################
sub failover {
   # instantiate variables
   my $states = [ qw/standalone primary standby/ ];

   # retrieve current failover mode
   my $failmode = $snmp->snmpget( OID->{failmode} );

   # get/set cached failover state
   my $cache = cache_func->new( $args->{h} );
   my $cached = $cache->get( 'failmode' );
   $cache->set( 'failmode', $failmode );

   # check for failover and generate output
   if (defined $cached && $failmode != $cached) {
      print "CRITICAL - Failover state change from $states->[ $cached ] " .
            "to $states->[ $failmode ]";
   }
   else {
      print "OK - Failover state is $states->[ $failmode ]";
   }
}


################################################################################
# flows - check flow sessions                                                  #
################################################################################
sub flows {
   # set warning/critical thresholds
   my ($warn, $crit) = grep /^\d+$/ => split /:/ => $args->{l};
   $warn ||= 80;    # default 80%
   $crit ||= 90;    # default 90%

   # increase snmpget timeout to 10 seconds
   $snmp->{snmpget_timeout} = 10;

   # retrieve flows
   my ($max, $free, $tcp, $udp) = $snmp->snmpget( OID->{maxflows},
      OID->{freeflows}, OID->{tcpflows}, OID->{udpflows} );

   # populate perfdata
   my $perfdata = "tcp=$tcp udp=$udp";

   # calculate used flows and percentage
   my $used    = sprintf "%d", $max - $free;
   my $percent = sprintf "%d", $used / $max * 100;

   # test against thresholds and generate output
   if ($percent >= $crit) {
      print "CRITICAL - Flows at $used of $max or $percent% utilization " .
            "(threshold $crit%)|$perfdata";
      exit 2;
   }
   elsif ($percent >= $warn) {
      print "WARNING - Flows at $used of $max or $percent% utilization " .
            "(threshol $warn%)|$perfdata";
      exit 1;
   }
   else {
      print "OK - Flows at $used of $max or $percent% utilization|$perfdata";
   }
}


################################################################################
# sensors - check application and hardware sensors                             #
################################################################################
sub sensors {
   # instantiate variables
   my @output = ();

   # retrieve software health status
   my $health = $snmp->snmpget( OID->{health} );

   # check software health status
   if ($health == 0) {
      push @output, "CRITICAL - Sensor software unhealthy";
   }
   elsif ($health == 1) {
      push @output, "OK - Sensor software healthy";
   }
   elsif ($health == 2) {
      push @output, "CRITICAL - Sensor software has no signatures applied";
   }

   # retrieve hardware temperature status
   my $tempstatus = $snmp->snmpget( OID->{tempstatus} );

   # check hardware temperature status
   if ($tempstatus == 0) {
      push @output, "OK - Sensor temperature normal";
   }
   elsif ($tempstatus == 1) {
      push @output, "CRITICAL - Sensor temperature abnormal";
   }

   # retrieve hardware fan status
   my $fanstatus = $snmp->snmpget( OID->{fanstatus} );

   # check hardware fan status
   if ($fanstatus == 0) {
      push @output, "OK - Sensor fans normal";
   }
   elsif ($fanstatus == 1) {
      push @output, "CRITICAL - Sensor fans abnormal";
   }

   # retrieve psu status
   my @psustatus = $snmp->snmpget( OID->{psu1status}, OID->{psu2status} );

   # loop through each psu
   for my $i (0 .. $#psustatus) {
      my $psuid = $i + 1;
      if ($psustatus[$i] == 0) { 
         push @output, "OK - Power Supply #$psuid not present";
      }
      elsif ($psustatus[$i] == 1) {
         push @output, "OK - Power Supply #$psuid operational";
      }
      elsif ($psustatus[$i] == 2) {
         push @output, "CRITICAL - Power Supply #$psuid bad";
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
# version - retrieve host platform and version                                 #
################################################################################
sub version {
   my ($platform, $version) = $snmp->snmpget( OID->{platform}, OID->{version} );
   my $upgrade = { #'9.1.1' => 'Bug #7213',
                 };
   if (exists $upgrade->{$version}) {
      print "WARNING - McAfee $platform version $version should be " .
            "upgraded: $upgrade->{$version}";
      exit 1;
   }
   else {
      print "OK - McAfee $platform version $version";
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
