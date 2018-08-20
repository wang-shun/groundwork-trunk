#!/usr/local/groundwork/perl/bin/perl -w

use strict;
use lib q(/usr/local/groundwork/nagios/libexec);
use cache_func ();
use nagios_func ();
use parse_func ();
use snmp_func ();

use constant PLUGIN => 'check_juniper_fw';

use constant OID => { 'cpuusage'      => '.1.3.6.1.4.1.3224.16.1.3.0',
                      'freemem'       => '.1.3.6.1.4.1.3224.16.2.2.0',
                      'usedmem'       => '.1.3.6.1.4.1.3224.16.2.1.0',
                      'nsrpclusterid' => '.1.3.6.1.4.1.3224.6.1.1.0',
                      'nsrplocalid'   => '.1.3.6.1.4.1.3224.6.1.2.0',
                      'nsrpgroupid'   => '.1.3.6.1.4.1.3224.6.2.2.1.1',
                      'nsrpunitid'    => '.1.3.6.1.4.1.3224.6.2.2.1.2',
                      'nsrpstatus'    => '.1.3.6.1.4.1.3224.6.2.2.1.3',
                      'sessionused'   => '.1.3.6.1.4.1.3224.16.3.2.0',
                      'sessionmax'    => '.1.3.6.1.4.1.3224.16.3.3.0',
                      'version'       => '.1.3.6.1.2.1.1.1.0',
                      'powerindex'    => '.1.3.6.1.4.1.3224.21.1.1.1',
                      'powerstatus'   => '.1.3.6.1.4.1.3224.21.1.1.2',
                      'powername'     => '.1.3.6.1.4.1.3224.21.1.1.3',
                      'fanindex'      => '.1.3.6.1.4.1.3224.21.2.1.1',
                      'fanstatus'     => '.1.3.6.1.4.1.3224.21.2.1.2',
                      'fanname'       => '.1.3.6.1.4.1.3224.21.2.1.3',
                      'tempindex'     => '.1.3.6.1.4.1.3224.21.4.1.1',
                      'tempstatus'    => '.1.3.6.1.4.1.3224.21.4.1.2',
                      'tempvalue'     => '.1.3.6.1.4.1.3224.21.4.1.3',
                      'tempname'      => '.1.3.6.1.4.1.3224.21.4.1.4',
                    };

use constant FUNCTIONS => { 'cpu'      => \&cpu,
                            'failover' => \&failover,
                            'int_list' => \&interface,
                            'mem'      => \&memory,
                            'sensors'  => \&sensors,
                            'sessions' => \&sessions,
                            'uptime'   => \&uptime,
                            'version'  => \&version,
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

   # retrieve cpu utilization in percentage
   my $cpu = $snmp->snmpget( OID->{cpuusage} );

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
# failover - check for device failover
################################################################################
sub failover {
   # instantiate variables
   my @output = ();
   my $state = { 1 => 'init',
                 2 => 'master',
                 3 => 'primary-backup',
                 4 => 'backup',
                 5 => 'ineligible',
                 6 => 'inoperable',
               };

   # retrieve cluster id and local unit id
   my ($clusterid, $localid) = $snmp->snmpget( OID->{nsrpclusterid}, 
      OID->{nsrplocalid} );

   # cluster id should be > 0 when failover is enabled
   if (!$clusterid) {
      print "OK - Failover is not enabled on this device";
      return;
   }

   # retrieve failover cluster unit ids
   my @unitid = $snmp->snmpbulkwalk( OID->{nsrpunitid} );
   
   # retrieve failover cluster unit status
   my @status = $snmp->snmpbulkwalk( OID->{nsrpstatus} );

   # determine which firewall we are and return the index
   my ($index) = grep { $localid == $unitid[$_] } 0 .. $#unitid or do {
      print "UNKNOWN - Unable to determine unit id";
      exit 3;
   };

   # retrieve cached state
   my $cache  = cache_func->new( $args->{h} );
   my $cached = $cache->get( 'failover' );
   $cache->set( 'failover', $status[$index] );

   if (defined $cached && $cached != $status[$index]) {
      print "CRITICAL - Failover state changed from $state->{ $cached } to " .
            "$state->{ $status[$index] }";
      exit 2;
   }
   else {
      print "OK - Failover state is $state->{ $status[$index] }";
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
      $int->{int_name} =~ tr|/|-|;
      $int->{int_name} =~ s/^\S+ : (\S+)/$1/;
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

   # retrieve free/used memory
   my ($free, $used) = $snmp->snmpget( OID->{freemem}, OID->{usedmem} );

   # calculate used memory percentage
   my $memory = sprintf "%d", 100 * $used / ($free + $used);

   # test against thresholds and generate output
   if ($memory >= $crit) {
      print "CRITICAL - Memory utilization at $memory% (threshold $crit%)" .
            "|percent=$memory";
      exit 2;
   }
   elsif ($memory >= $warn) {
      print "WARNING - Memory utilization at $memory% (threshold $warn%)" .
            "|percent=$memory";
      exit 1;
   }
   else {
      print "OK - Memory utilization at $memory%|percent=$memory";
   }
}


################################################################################
# sensors - check health sensors                                               #
################################################################################
sub sensors {
   # instantiate variables
   my @output = ();
   my $states = [ qw/fail good not-installed/ ];

   # not all netscreen devices support all sensors
   # so lets disable error-checking 
   $snmp->{ec} = 0;

   # check power supplies
   my @powerindex = $snmp->snmpbulkwalk( OID->{powerindex} );
   foreach my $index (@powerindex) {
      my $oid_status = sprintf "%s.%s", OID->{powerstatus}, $index;
      my $oid_name   = sprintf "%s.%s", OID->{powername}, $index;
      my ($status, $name) = $snmp->snmpget( $oid_status, $oid_name );

      # some buggy versions of code don't provide a power "name"
      # so we will make one up based on the index
      $name ||= "Power Supply $index";

      if ($status == 0) {
         push @output, "CRITICAL - $name has failed";
      }
      elsif ($status == 1) {
         push @output, "OK - $name is healthy";
      }
      elsif ($status == 2) {
         push @output, "OK - $name is not installed";
      }
   }      

   # check fans
   my @fanindex = $snmp->snmpbulkwalk( OID->{fanindex} );
   foreach my $index (@fanindex) {
      my $oid_status = sprintf "%s.%s", OID->{fanstatus}, $index;
      my $oid_name   = sprintf "%s.%s", OID->{fanname}, $index;
      my ($status, $name) = $snmp->snmpget( $oid_status, $oid_name );
      if ($status == 0) {
         push @output, "CRITICAL - $name has failed";
      }
      elsif ($status == 1) {
         push @output, "OK - $name is healthy";
      }
   }

   # check temperatures
   my @tempindex = $snmp->snmpbulkwalk( OID->{tempindex} );
   foreach my $index (@tempindex) {
      my $oid_temp = sprintf "%s.%s", OID->{tempvalue}, $index;
      my $oid_name = sprintf "%s.%s", OID->{tempname}, $index;
      my ($temp, $name) = $snmp->snmpget( $oid_temp, $oid_name );
      if ($name =~ /CPU/ && $temp >= 80) {
         push @output, "CRITICAL - $name at ${temp}C (threshold 80C)";
      }
      elsif ($name !~ /CPU/ && $temp >= 60) {
         push @output, "CRITICAL - $name at ${temp}C (threshold 60C)";
      }
      else {
         push @output, "OK - $name at ${temp}C";
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
# sessions - check concurrent sessions                                         #
################################################################################
sub sessions {
   # set warning/critical thresholds
   my ($warn, $crit) = grep /^\d+$/ => split /:/ => $args->{l};
   $warn ||= 80;    # default 80%
   $crit ||= 90;    # default 90%

   # retrieve concurrent sessions and session limit
   my ($current, $limit) = $snmp->snmpget( OID->{sessionused}, 
      OID->{sessionmax} );

   # calculate percentage used
   my $percent = sprintf "%d", 100 * $current / $limit;

   # test against thresholds and generate output
   if ($percent >= $crit) {
      print "CRITICAL - Current sessions at $current of $limit or $percent% " .
            "utilization (threshold $crit%)|current=$current";
      exit 2;
   }
   elsif ($percent >= $warn) {
      print "WARNING - Current sessions at $current of $limit or $percent% " .
            "utilization (threshold $warn%)|current=$current";
      exit 1;
   }
   else {
      print "OK - Current sessions at $current of $limit or $percent% " .
            "utilization|current=$current";
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
   my $sysdescr = $snmp->snmpget( OID->{version} );
   my ($platform, $version) = $sysdescr =~ /^(.*?) version (\S+)/;

   # some really old versions don't provide platform/version in the expected
   # format.  In these cases, set sysdescr to platform and version unknown.
   $platform ||= $sysdescr;
   $version  ||= 'UNKNOWN';

   my $upgrade = { #'9.1.1' => 'Bug #7213',
                 };

   if (exists $upgrade->{$version}) {
      print "WARNING - Juniper $platform code version $version " .
            "should be upgraded: $upgrade->{$version}";
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
