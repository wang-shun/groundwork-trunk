#!/usr/local/groundwork/perl/bin/perl -w

use strict;
use lib q(/usr/local/groundwork/nagios/libexec);
use nagios_func ();
use parse_func ();
use snmp_func ();

use constant PLUGIN => 'check_bluecoat';

use constant OID => { 'version'      => '.1.3.6.1.2.1.1.1.0',
                      'sensors'      => '.1.3.6.1.4.1.3417.2.1.1.1.1.1',
                      'sensorunits'  => '.1.3.6.1.4.1.3417.2.1.1.1.1.1.3',
                      'sensorscale'  => '.1.3.6.1.4.1.3417.2.1.1.1.1.1.4',
                      'sensorvalue'  => '.1.3.6.1.4.1.3417.2.1.1.1.1.1.5',
                      'sensorcode'   => '.1.3.6.1.4.1.3417.2.1.1.1.1.1.6',
                      'sensorstatus' => '.1.3.6.1.4.1.3417.2.1.1.1.1.1.7',
                      'sensorname'   => '.1.3.6.1.4.1.3417.2.1.1.1.1.1.9',
                      'usagename'    => '.1.3.6.1.4.1.3417.2.4.1.1.1.3',
                      'usagepercent' => '.1.3.6.1.4.1.3417.2.4.1.1.1.4',
                      'usagestatus'  => '.1.3.6.1.4.1.3417.2.4.1.1.1.6',
                      'diskstatus'   => '.1.3.6.1.4.1.3417.2.2.1.1.1.1.3',
                      'mempressure5' => '.1.3.6.1.4.1.3417.2.8.2.3.0',
                      'mempressure6' => '.1.3.6.1.4.1.3417.2.11.2.3.4.0',

                      'clirequests'    => '.1.3.6.1.4.1.3417.2.11.3.1.1.1.0',
                      'cliconnsactive' => '.1.3.6.1.4.1.3417.2.11.3.1.3.2.0',
                      'cliconnsidle'   => '.1.3.6.1.4.1.3417.2.11.3.1.3.3.0',
                      'srvrequests'    => '.1.3.6.1.4.1.3417.2.11.3.1.2.1.0',
                      'srvconnsactive' => '.1.3.6.1.4.1.3417.2.11.3.1.3.5.0',
                      'srvconnsidle'   => '.1.3.6.1.4.1.3417.2.11.3.1.3.6.0',

                     'avFilesScanned'         => '.1.3.6.1.4.1.3417.2.10.1.1.0',
                     'avVirusesDetected'      => '.1.3.6.1.4.1.3417.2.10.1.2.0',
                     'avLicenseDaysRemaining' => '.1.3.6.1.4.1.3417.2.10.1.7.0',
                     'avSlowICAPConnections' => '.1.3.6.1.4.1.3417.2.10.1.10.0',
          
                    };

use constant FUNCTIONS => { 'av_license'   => \&av_license,
                            'av_filescan'  => \&av_filescan,
                            'client'       => \&client,
                            'cpu'          => \&cpu,
                            'disk'         => \&disk,
                            'int_list'     => \&interface,
                            'mem'          => \&memory,
                            'sensors'      => \&sensors,
                            'server'       => \&server,
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
# av_license - Proxy AV license check                                          #
################################################################################
sub av_license {
   # set thresholds
   my ($warn, $crit) = grep /^\d+$/ => split /:/ => $args->{l};
   $warn ||= 30;    # default 30 days
   $crit ||= 7;     # default 7 days

   # retrieve number of days left on AV license
   my $daysleft = $snmp->snmpget( OID->{avLicenseDaysRemaining} );
  
   # test against thresholds and generate output 
   if ($daysleft <= $crit) {
      print "CRITICAL - Blue Coat AV License expires in $daysleft days";
      exit 2;
   }
   elsif ($daysleft <= $warn) {
      print "WARNING - Blue Coat AV License expires in $daysleft days";
      exit 1;
   }
   else {
      print "OK - Blue Coat AV License expires in $daysleft days";
   }
}


################################################################################
# av_filescan - Proxy AV files scanned                                         #
################################################################################
sub av_filescan {
   # retrieve count of files scanned and infected
   my ($scanned, $infected) = $snmp->snmpget( OID->{avFilesScanned},
                                              OID->{avVirusesDetected} );

   # populate perfdata
   my $perfdata = "scanned=$scanned infected=$infected";

   # generate output
   print "OK - Collected AV file scanning statistics|$perfdata\n";
   print "Files scanned:  $scanned\n";
   print "Files infected: $infected\n";   
}


################################################################################
# client - gathers client requests/connections statistics from BC PROXY MIB    #
# requests is COUNTER style that constantly increments                         #
# connections are GAUGE style that is a count when polled                      #
################################################################################
sub client {
   # versions 3.x and 4.x don't support http statistics via snmp
   if (version(1) =~ /^([34])/) {
      print "OK - Proxy http statistics not supported in v$1.x";
      return;
   }
 
   # retrieve client requests and connections 
   my ($requests, $activeconns, $idleconns) = $snmp->snmpget(
      OID->{clirequests}, OID->{cliconnsactive}, OID->{cliconnsidle} );

   # populate perfdata
   my @perfdata = ( "requests=$requests", "active_conns=$activeconns",
                    "idle_conns=$idleconns" );

   # generate output
   print "OK - Client statistics gathered|@perfdata\n";
   print "Client Requests: $requests\n";
   print "Client Active Connections: $activeconns\n";
   print "Client Idle connections: $idleconns\n";
}


################################################################################
# cpu - retrieve cpu utilization
################################################################################
sub cpu {
   # set thresholds
   my ($warn, $crit) = grep /^\d+$/ => split /:/ => $args->{l};
   $warn ||= 85;    # default 85%
   $crit ||= 95;    # default 95%

   # version 6.1.4.1 has a cpu bug, we will lower thresholds for this version
   if (version(1) eq '6.1.4.1') {
      $warn = 65;   # 65%
      $crit = 90;   # 90%
   }
   
   # retrieve all usage oid names
   my @usagename = $snmp->snmpbulkwalk( OID->{usagename} );

   # parse out the cpuindex based on the name
   my ($cpuindex) = grep { $usagename[$_] =~ /CPU/ } 0 .. $#usagename or do {
      print "UNKNOWN - Unable to retrieve CPU utilization OID";
      exit 3;
   };

   # retrieve cpu utilization in percent
   my $cpu = $snmp->snmpget( OID->{usagepercent} . '.' . ($cpuindex+1) );

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
# disk - retrieve disk utilization and health                                  #
################################################################################
sub disk {
   # instantiate variables
   my @output = ();

   # set thresholds
   my ($warn, $crit) = grep /^\d+$/ => split /:/ => $args->{l};
   $warn ||= 80;    # default 80%
   $crit ||= 90;    # default 90%

   # load lookup tables
   my $t_sensor = load_lookup_tables('sensor');
   my $t_disk = load_lookup_tables('disk');

   # retrieve health status of each disk in the system
   my @diskstatus = $snmp->snmpbulkwalk( OID->{diskstatus} );
   foreach my $i (0 ..$#diskstatus) {
      my $disk = $i + 1;
      my $status = $diskstatus[$i];
      if ($status =~ /^[67]$/) {
         # drive bay empty; don't alarm; lots of empty's in production
         push @output, "OK - Disk #$disk $t_disk->{$status}";
      }
      elsif ($status > 5) {
         push @output, "CRITICAL - Disk #$disk $t_disk->{$status}";
      }
      elsif ($status > 2) {
         push @output, "WARNING - Disk #$disk $t_disk->{$status}";
      }
      else {
         push @output, "OK - Disk #$disk $t_disk->{$status}";
      }
   }

   # retrieve all usage names
   my @usagename = $snmp->snmpbulkwalk( OID->{usagename} );

   # parse out the disk index based on the name
   my ($diskindex) = grep { $usagename[$_] eq 'Disk' } 0 .. $#usagename or do {
      print "UNKNOWN - Unable to retrieve DISK utilization OID";
      exit 3;
   };

   # retrieve disk utilization in percent
   my $disk = $snmp->snmpget( OID->{usagepercent} . '.' . ($diskindex+1) );

   # populate perfdata
   my $perfdata = "percent=$disk";

   # test disk utilization against configured thresholds
   if ($disk >= $crit) {
      push @output, "CRITICAL - Disk usage at $disk% (threshold $crit%)";
   }
   elsif ($disk >= $warn) {
      push @output, "WARNING - Disk usage at $disk% (threshold $crit%)";
   }
   else {
      push @output, "OK - Disk usage at $disk%";
   }

   # generate output
   my @sorted = sort nagios_func::nagsort @output;
   if (grep /CRITICAL/ => @sorted) {
      print shift(@sorted), "|$perfdata\n";
      print join "\n" => @sorted;
      exit 2;
   }
   elsif (grep /WARNING/ => @sorted) {
      print shift(@sorted), "|$perfdata\n";
      print join "\n" => @sorted;
      exit 1;
   }
   else {
      my $ok = scalar @sorted;
      print "OK - $ok disk checks healthy|$perfdata\n";
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
   if ( %$interface ) {
      # loop through each interface and submit passive results
      my @int_list = ();
      foreach my $i (sort { $a <=> $b } keys %$interface) {
         my $int = $interface->{$i};
         $int->{int_type} == 6 or next;
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
   else {
      print "WARNING - No interfaces detected";
      exit 1
   }
}


################################################################################
# memory - check memory pressure                                               #
################################################################################
sub memory {
   # instantiate variables
   my $memory = ();
   
   # version 3.x doesn't provide a memory oid
   if (version(1) =~ /^3/ && version(2) =~ /SG/) {
      print "OK - Memory utilization not provided on v3.x";
      return;
   }
  
   # set thresholds
   my ($warn, $crit) = grep /^\d+$/ => split /:/ => $args->{l};
   $warn ||= 80;    # default 80%
   $crit ||= 90;    # default 90%

   # retrieve all usage names 
   my @usagename = $snmp->snmpbulkwalk( OID->{usagename} );

   # parse out memory index based on name
   my ($memindex) = grep { $usagename[$_] =~ /Memory pressure/ } 
      0 .. $#usagename;

   # retrieve memory index
   if ($memindex) {
      $memory = $snmp->snmpget( OID->{usagepercent} . '.' . ($memindex+1) );
   }
   else {
      $memory = $snmp->snmpget_or( OID->{mempressure6}, OID->{mempressure5} );
   }
  
   # test against thresholds and generate output 
   if ($memory >= $crit) {
      print "CRITICAL - Memory pressure at $memory% (threshold $crit%)" .
            "|percent=$memory";
      exit 2;
   }
   elsif ($memory >= $warn) {
      print "WARNING - Memory pressure at $memory% (threshold $warn%)" .
            "|percent=$memory";
      exit 1;
   }
   else {
      print "OK - Memory pressure at $memory%|percent=$memory";
   }
}


################################################################################
# sensors - check environmental sensors                                        #
################################################################################
sub sensors {
   # instantiate variables
   my @output = ();
   my $sensor = load_lookup_tables('sensor');

   # retrieve hash of sensors table
   my $sensors = $snmp->snmpbulkwalk( OID->{sensors} );

   # count number of sensors
   my $count = grep { $_ =~ OID->{sensorstatus} } keys %$sensors;

   # loop through each sensor
   foreach my $i (1 .. $count) {
      my $units  = $sensors->{ OID->{sensorunits}  . ".$i" } or next;
      my $scale  = $sensors->{ OID->{sensorscale}  . ".$i" } or next;
      my $value  = $sensors->{ OID->{sensorvalue}  . ".$i" } or next;
      my $code   = $sensors->{ OID->{sensorcode}   . ".$i" } or next;
      my $status = $sensors->{ OID->{sensorstatus} . ".$i" } or next;
      my $name   = $sensors->{ OID->{sensorname}   . ".$i" } or next;

      # correct value by scale
      $value *= 10 ** $scale;

      # Not Installed, skip
      if ($status == 4) {
         next;
      }

      # check sensor status 
      if ($status > 1) {
         push @output, "CRITICAL - Sensor #$i ($name) " .
                       "$sensor->{status}->{$status}";
      }
      elsif ($code > 1) {
         my $state = $sensor->{code}->{$code} =~ /warning/ ? 
                     'WARNING' : 'CRITICAL';
         push @output, "$state - $sensor->{code}->{$code} sensor #$i " .
                       "($name) = $value $sensor->{units}->{$units}";
      }
      else {
         push @output, "OK - Sensor #$i ($name) = $value " .
                       "$sensor->{units}->{$units}";
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
      my $ok = scalar @sorted;
      print "OK - $ok sensors healthy\n";
      print join "\n" => @sorted;
   }
}


################################################################################
# server - gathers server requests/connections statistics from BC PROXY MIB    #
# requests is COUNTER style that constantly increments                         #
# connections are GAUGE style that is a count when polled                      #
################################################################################
sub server {
   # versions 3.x and 4.x don't support http statistics via snmp
   if (version(1) =~ /^([34])/) {
      print "OK - Proxy http statistics not supported in v$1.x";
      return;
   }

   # retrieve requests and connections
   my ($requests, $activeconns, $idleconns) = $snmp->snmpget(
      OID->{srvrequests}, OID->{srvconnsactive}, OID->{srvconnsidle} );

   # populate perfdata
   my @perfdata = ( "requests=$requests", "active_conns=$activeconns",
                    "idle_conns=$idleconns" );

   # generate output
   print "OK - Server statistics gathered|@perfdata\n";
   print "Server requests: $requests\n";
   print "Server active connections: $activeconns\n";
   print "Server idle connections: $idleconns\n";
}


################################################################################
# uptime - retrieve system uptime                                              #
################################################################################
sub uptime {
   return $snmp->snmp_uptime( $args );
}


################################################################################
# version - retreive system platform and code version                          #
################################################################################
sub version {
   # capture flag whether we should return the value or exit
   my $return = shift;

   # retrieve system description
   my $sysdescr = $snmp->snmpget( OID->{version} );

   # parse platform and version out of system description
   my ($platform, $version) = $sysdescr =~ 
      /Blue Coat (\S+) .*? Version: (?:\S+ )?([0-9\.]+)/;
  
   # define upgrade array 
   my $upgrade = { #'9.1.1' => 'Bug #7213',
                 };

   # return version or generate output
   if ($return && $return == 1) {
      return $version;
   }
   elsif ($return && $return == 2) {
      return $platform;
   }
   elsif (exists $upgrade->{$version}) {
      print "WARNING - Bluecoat code version $version should be upgraded: " .
            "$upgrade->{$version}";
      exit 1;
   }
   else {
      print "OK - Bluecoat $platform code version $version";
   }
}


################################################################################
# load_lookup_tables - lookup tables to be loaded on-demand                    #
################################################################################
sub load_lookup_tables {
   my $type = shift;
   my $hash = {
      sensor =>  { units =>  { 1 => 'other',
                               2 => 'truthvalue',
                               3 => 'specialEnum',
                               4 => 'volts',
                               5 => 'celsius',
                               6 => 'rpm',
                             },
                   code  =>  { 1  => 'ok',
                               2  => 'unknown',
                               3  => 'not-installed',
                               4  => 'voltage-low-warning',
                               5  => 'voltage-low-critical',
                               6  => 'no-power',
                               7  => 'voltage-high-warning', 
                               8  => 'voltage-high-critical', 
                               9  => 'voltage-high-severe', 
                               10 => 'temperature-high-warning',
                               11 => 'temperature-high-critical',
                               12 => 'temperature-high-severe',
                               13 => 'fan-slow-warning',
                               14 => 'fan-slow-critical',
                               15 => 'fan-stopped',
                             },
                   status => { 1 => 'ok',
                               2 => 'unavailable',
                               3 => 'nonoperational',
                               4 => 'notinstalled',
                             },
                 },
      license => { util   => { 0 => 'ok',
                               1 => 'low-warning',
                               2 => 'warning',
                               3 => 'high-warning',
                               4 => 'low-critical',
                               5 => 'critical',
                               6 => 'high-critical',
                             },
                   exp    => { 0 => 'not-licensed',
                               1 => 'valid',
                               2 => 'expired',
                             },
                 },
      disk =>    { 1 => 'present',
                   2 => 'initializing',
                   3 => 'inserted',
                   4 => 'offline',
                   5 => 'removed',
                   6 => 'not-present',
                   7 => 'empty',
                   8 => 'bad',
                   9 => 'unknown',
                 },
      attack =>  { 1 => 'no-attack',
                   2 => 'under-attack',
                 },
   };
   return $hash->{$type};
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
