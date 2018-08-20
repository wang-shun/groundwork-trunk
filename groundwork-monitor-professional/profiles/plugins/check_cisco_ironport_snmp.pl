#!/usr/local/groundwork/perl/bin/perl -w

use strict;
use lib q(/usr/local/groundwork/nagios/libexec);
use nagios_func ();
use parse_func ();
use snmp_func ();

use constant PLUGIN => 'check_cisco_ironport';

use constant OID => { 'sysdescr'         => '.1.3.6.1.2.1.1.1.0',
                      'memusage'         => '.1.3.6.1.4.1.15497.1.1.1.1.0',
                      'cpuusage'         => '.1.3.6.1.4.1.15497.1.1.1.2.0',
                      'psustatus'        => '.1.3.6.1.4.1.15497.1.1.1.8.1.2',
                      'psuname'          => '.1.3.6.1.4.1.15497.1.1.1.8.1.4',
                      'tempcelsius'      => '.1.3.6.1.4.1.15497.1.1.1.9.1.2',
                      'tempname'         => '.1.3.6.1.4.1.15497.1.1.1.9.1.3',
                      'fanrpm'           => '.1.3.6.1.4.1.15497.1.1.1.10.1.2',
                      'fanname'          => '.1.3.6.1.4.1.15497.1.1.1.10.1.3',
                      'keyname'          => '.1.3.6.1.4.1.15497.1.1.1.12.1.2',
                      'keyisperm'        => '.1.3.6.1.4.1.15497.1.1.1.12.1.3',
                      'keyexpires'       => '.1.3.6.1.4.1.15497.1.1.1.12.1.4',
                      'raidstatus'       => '.1.3.6.1.4.1.15497.1.1.1.18.1.2',
                      'raidname'         => '.1.3.6.1.4.1.15497.1.1.1.18.1.3',
                      'mail_diskio'      => '.1.3.6.1.4.1.15497.1.1.1.3.0',
                      'mail_resconserve' => '.1.3.6.1.4.1.15497.1.1.1.6.0',
                      'mail_xferthreads' => '.1.3.6.1.4.1.15497.1.1.1.20.0',
                    };

use constant FUNCTIONS => { 'cpu'          => \&cpu,
                            'int_list'     => \&interface,
                            'license'      => \&license,
                            'mail'         => \&mail,
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
# cpu - check cpu utilization in percentage                                    #
################################################################################
sub cpu {
   # set warning/critical thresholds
   my ($warn, $crit) = grep /^\d+$/ => split /:/ => $args->{l};
   $warn ||= 85;    # default 85%
   $crit ||= 95;    # default 95%

   # retrieve cpu utilization in percent
   my $cpu = $snmp->snmpget( OID->{cpuusage} );

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
      $int->{int_name} =~ tr/ /-/;
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
# mail - check IronPort mail appliance metrics                                 #
################################################################################
sub mail {
   # instantiate variables
   my @output = my @perfdata = ();
   
   # set warning/critical thresholds
   my ($warn, $crit) = grep /^\d+$/ => split /:/ => $args->{l};
   $warn ||= 80;    # default 80%
   $crit ||= 90;    # default 90%

   # we should just exit in OK state if this isn't a mail appliance
   get_series_by_model() =~ /^[CX]$/ or do {
      print "OK - mail plugin not supported on this platform";
      return;
   };

   # check disk io utilization
   my $diskio = $snmp->snmpget( OID->{mail_diskio} );
   push @perfdata, "diskio=$diskio";
   if ($diskio >= $crit) {
      push @output, "CRITICAL - Disk IO utilization at $diskio% " .
                    "(threshold 80%)";
   }
   elsif ($diskio >= $warn) {
      push @output, "WARNING - Disk IO utilization at $diskio% (threshold 50%)";
   }
   else {
      push @output, "OK - Disk IO utilization at $diskio%";
   }
  
   # check whether we are in resource conservation mode
   my $conserve = $snmp->snmpget( OID->{mail_resconserve} );
   if ($conserve == 1) {
      push @output, "OK - Resource conservation not in effect";
   }
   elsif ($conserve == 2) {
      push @output, "WARNING - Resource conservation due to memory shortage";
   }
   elsif ($conserve == 3) {
      push @output, "WARNING - Resource conservation due to queue space " .
                    "shortage";
   }
   elsif ($conserve == 4) {
      push @output, "CRITICAL - Resource conservation due to queue space full";
   }

   # capture number of mta transfer threads for graphing
   my $xferthreads = $snmp->snmpget( OID->{mail_xferthreads} );
   push @output, "OK - Mail transfer threads currently at $xferthreads";
   push @perfdata, "xfer_threads=$xferthreads";

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
      print "OK - $ok mail checks healthy|@perfdata\n";
      print join "\n" => @sorted;
   }
}


################################################################################
# license - check license keys for validity and expiration                     #
################################################################################
sub license {
   # instantiate variables
   my @output = ();
   
   # set warning/critical thresholds
   my ($warn, $crit) = grep /^\d+$/ => split /:/ => $args->{l};
   defined $warn and $warn == 0 or $warn ||= 60;   # default 60 days
   defined $crit and $crit == 0 or $crit ||= 30;   # default 30 days
   
   # check license key validity
   my @keyname    = $snmp->snmpbulkwalk( OID->{keyname} );
   my @keyisperm  = $snmp->snmpbulkwalk( OID->{keyisperm} );
   my @keyexpires = $snmp->snmpbulkwalk( OID->{keyexpires} );
   foreach my $i (0 .. $#keyisperm) {
      my $keyexpdate = scalar localtime( time + $keyexpires[$i] ); 
      if ($keyisperm[$i] == 1) {
         push @output, "OK - $keyname[$i] license key is perpetual";
      }
      elsif ($keyexpires[$i] <= 0) {   # already expired
         push @output, "OK - $keyname[$i] is expired";
      }
      elsif ($keyexpires[$i] < 86400*$crit) {   # 7 days
         push @output, "CRITICAL - $keyname[$i] expires on $keyexpdate";
      }
      elsif ($keyexpires[$i] <= 86400*$warn) {   # 30 days
         push @output, "WARNING - $keyname[$i] expires on $keyexpdate";
      }
      else {
         push @output, "OK - $keyname[$i] expires on $keyexpdate"; 
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
      print "OK - $ok licenses healthy\n";
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

   # retrieve memory utilization in percent
   my $mem = $snmp->snmpget( OID->{memusage} );
  
   # test against thresholds and generate output 
   if ($mem >= $crit) {
      print "CRITICAL - Memory utilization at $mem% (threshold $crit%)" .
            "|percent=$mem";
      exit 2;
   }
   elsif ($mem >= $warn) {
      print "WARNING - Memory utilization at $mem% (threshold $warn%)" .
            "|percent=$mem";
      exit 1;
   }
   else {
      print "OK - Memory utilization at $mem%|percent=$mem";
   }
}



################################################################################
# sensors - check system sensors for health
################################################################################
sub sensors {
   # instantiate variables
   my @output = ();
   my $psu    = { 1 => 'not-installed',
                  2 => 'healthy',
                  3 => 'no-ac',
                  4 => 'faulty',
                };
   my $raid   = { 1 => 'drive-healthy',
                  2 => 'drive-failure',
                  3 => 'drive-rebuild',
                };

   # parse levels 
   my %levels = do { map { $_ => 1 } split /:/, $args->{l} };

   # check power supply health
   my @psustatus = $snmp->snmpbulkwalk( OID->{psustatus} );
   my @psuname   = $snmp->snmpbulkwalk( OID->{psuname} );
   foreach my $i (0 .. $#psustatus) {
      my $state = $psustatus[$i] > 2 ? 'CRITICAL' : 'OK';
      exists $levels{ 'NOPS' . ($i+1) } and $state = 'OK';
      push @output, "$state - Power Supply $psuname[$i] is " .
                    $psu->{ $psustatus[$i] };
   }

   # check temperature sensors
   my @tempcelsius = $snmp->snmpbulkwalk( OID->{tempcelsius} );
   my @tempname    = $snmp->snmpbulkwalk( OID->{tempname} );
   foreach my $i (0 .. $#tempcelsius) {
      my $state = $tempcelsius[$i] >= 65 ? 'CRITICAL' : 'OK';
      push @output, "$state - $tempname[$i] temperature is $tempcelsius[$i]C";
   }

   # check fan health
   my @fanrpm  = $snmp->snmpbulkwalk( OID->{fanrpm} );
   my @fanname = $snmp->snmpbulkwalk( OID->{fanname} );
   foreach my $i (0 .. $#fanrpm) {
      my $state = $fanrpm[$i] == 0 ? 'CRITICAL' : 'OK';
      push @output, "$state - $fanname[$i] at $fanrpm[$i] rpm";
   }

   # check drive / raid status
   # snmpbulkwalk fails on some devices, so use snmpwalk instead
   my @raidstatus = $snmp->snmpwalk( OID->{raidstatus} );
   my @raidname   = $snmp->snmpwalk( OID->{raidname} );
   foreach my $i (0 .. $#raidstatus) {
      if ($raidstatus[$i] == 2) {
         push @output, "CRITICAL - $raidname[$i] has failed";
      }
      elsif ($raidstatus[$i] == 3) {
         push @output, "WARNING - $raidname[$i] is rebuilding";
      }
      elsif ($raidstatus[$i] == 1) {
         push @output, "OK - $raidname[$i] is healthy";
      }
      else {
         push @output, "UNKNOWN - $raidname[$i] in unknown state";
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
# version - collect platform and version information                           #
################################################################################
sub version {
   # retrieve system description
   my $sysdescr = $snmp->snmpget( OID->{sysdescr} );

   # parse platform and version from sysdescr
   my ($platform, $version) = $sysdescr =~ 
      /IronPort Model (\S+), AsyncOS Version: (\S+),/;

   # define upgrade array
   my $upgrade = { #'9.1.1' => 'Bug #7213',
                 };

   # generate output
   if (exists $upgrade->{$version}) {
      print "WARNING - Cisco IronPort $platform version $version should be " .
            "upgraded: $upgrade->{$version}";
      exit 1;
   }
   else {
      print "OK - Cisco IronPort $platform version $version";
   }
}


################################################################################
# get_purpose_by_model - returns the series of device from the model number    #
#                        X == mail                                             #
#                        C == mail                                             #
#                        S == web security                                     #
#                        M == management                                       #
################################################################################
sub get_series_by_model {
   my $sysdescr = $snmp->snmpget( OID->{sysdescr} );
   my ($series) = $sysdescr =~ /IronPort Model ([CXSM])/;
   return $series;
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
