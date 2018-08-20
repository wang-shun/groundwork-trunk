#!/usr/local/groundwork/perl/bin/perl -w

use strict;
use lib q(/usr/local/groundwork/nagios/libexec);
use cache_func ();
use nagios_func ();
use parse_func ();
use snmp_func ();

use constant PLUGIN => 'check_paloalto';

use constant OID => { hrdevidx    => '.1.3.6.1.2.1.25.3.2.1.1',
                      hrdevtype   => '.1.3.6.1.2.1.25.3.2.1.2',
                      hrdevname   => '.1.3.6.1.2.1.25.3.2.1.3',
                      hrdevload   => '.1.3.6.1.2.1.25.3.3.1.2',
                      sensortype  => '.1.3.6.1.2.1.99.1.1.1.1',
                      sensorvalue => '.1.3.6.1.2.1.99.1.1.1.4',
                      sensorstate => '.1.3.6.1.2.1.99.1.1.1.5',
                      foself      => '.1.3.6.1.4.1.25461.2.1.2.1.11.0',
                      fomode      => '.1.3.6.1.4.1.25461.2.1.2.1.13.0',
                      platform    => '.1.3.6.1.4.1.25461.2.1.2.2.1.0',
                      sessions    => '.1.3.6.1.4.1.25461.2.1.2.3.3.0',
                      sessionmax  => '.1.3.6.1.4.1.25461.2.1.2.3.2.0',
                      version     => '.1.3.6.1.4.1.25461.2.1.2.1.1.0',
                    };

use constant FUNCTIONS => { cpu      => \&cpu,
                            failover => \&failover,
                            int_list => \&interface,
                            sensors  => \&sensors,
                            sessions => \&sessions,
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
# cpu - checks cpu utilization using HOST-RESOURCE-MIB                         #
################################################################################
sub cpu {
   # instantiate variables
   my @output = my @perfdata = ();

   # set warning/critical thresholds
   my ($warn, $crit) = grep /^\d+$/ => split /:/ => $args->{l};
   $warn ||= 85;    # default 85%
   $crit ||= 95;    # default 95%

   # retrieve list of hrDeviceIndex
   my @index = $snmp->snmpbulkwalk( OID->{hrdevidx} );

   # loop through each index
   foreach my $idx (@index) {
      # generate oid strings for use in this loop
      my $oid_type = sprintf "%s.%s", OID->{hrdevtype}, $idx;
      my $oid_name = sprintf "%s.%s", OID->{hrdevname}, $idx;
      my $oid_load = sprintf "%s.%s", OID->{hrdevload}, $idx;

      # retrieve the type (returns oid format)
      my $type = $snmp->snmpget( $oid_type );
  
      # skip if not a "processor" device type
      $type eq '.1.3.6.1.2.1.25.3.1.3' or next;

      # retrieve the name and the load (cpu% usage)
      my ($name, $load) = $snmp->snmpget( $oid_name, $oid_load );

      # test against thresholds
      if ($load >= $crit) {
         push @output, "CRITICAL - $name at $load% utilization " .
                       "(threshold $crit%)";
      }
      elsif ($load >= $warn) {
         push @output, "WARNING - $name at $load% utilization " .
                       "(threshold $warn%)";
      }
      else {
         push @output, "OK - $name at $load% utilization";
      }

      # normalize the name for perfdata output
      my ($perfname) = $name =~ /^(\S+)/;
      $perfname =~ tr/[A-Z]/[a-z]/;
      $perfname =~ tr/-//d;

      # populate perfdata
      push @perfdata, "$perfname=$load";
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
      print "OK - $ok cpus healthy [@perfdata]|@perfdata\n";
      print join "\n" => @sorted;
   }
}


################################################################################
# failover - check for device failover                                         #
################################################################################
sub failover {
   # retrieve failover state and mode
   my ($self, $mode) = $snmp->snmpget( OID->{foself}, OID->{fomode} );

   if ($mode =~ /disabled/) {
      print "OK - Failover is not enabled";
      return;
   }

   # retrieve cached failover state
   my $cache = cache_func->new( $args->{h} );
   my $cached = $cache->get( 'failover' );

   # check for failover
   if ($cached && $cached ne $self) {
      print "CRITICAL - Failover state changed from $cached to $self";
      exit 2;
   }
   else {
      print "OK - Failover state is $self";
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
# sensors - check hardware sensors                                             #
################################################################################
sub sensors {
   # instantiate variables
   my @output = ();
   my $types   = [ qw/null other unkown vac vdc amps watts hertz celsius 
                     percentrh rpm cmm truthvalue/ ];
   my $states  = [ qw/null ok unavailable nonoperational/ ];
   my $sensors = { rpm => 'fan', celsius => 'temperature' };

   # retrieve sensor data
   my @sensortype  = $snmp->snmpbulkwalk( OID->{sensortype} );
   my @sensorvalue = $snmp->snmpbulkwalk( OID->{sensorvalue} );
   my @sensorstate = $snmp->snmpbulkwalk( OID->{sensorstate} );

   # loop through sensors
   foreach my $i (0 .. $#sensortype) {
      my $sensoridx = $i + 1;
      my $type   = $types->[ $sensortype[$i] ];
      my $value  = $sensorvalue[$i];
      my $state  = $states->[ $sensorstate[$i] ];
      my $sensor = $sensors->{ $type };
      if ($sensorstate[$i] == 1) {
         push @output, "OK - Sensor $sensoridx ($sensor) is $state at " .
                       "$value $type";
      }
      elsif ($sensorstate[$i] == 2) {
         push @output, "UNKNOWN - Sensor $sensoridx ($sensor) is $state at " .
                       "$value $type";
      }
      elsif ($sensorstate[$i] == 3) {
         push @output, "CRITICAL - Sensor $sensoridx ($sensor) is $state at " .
                       "$value $type";
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
# sessions - check session utilization                                         #
################################################################################
sub sessions {
   # set warning/critical thresholds
   my ($warn, $crit) = grep /^\d+$/ => split /:/ => $args->{l};
   $warn ||= 80;    # default 80%
   $crit ||= 90;    # default 90%
 
   # retrieve current (active) and maximum (limit)  
   my ($cur, $max) = $snmp->snmpget( OID->{sessions}, OID->{sessionmax} );
  
   # calculate a percentage based on the above 
   my $percent = sprintf "%d", 100 * $cur / $max;

   # test against thresholds and generate output 
   if ($percent >= $crit) {
      print "CRITICAL - Session utilization at $cur of $max or $percent% " .
            "(threshold $crit%)|percent=$percent";
      exit 2;
   }
   elsif ($percent >= $warn) {
      print "WARNING - Session utilization at $cur of $max or $percent% " .
            "(threshold $warn%)|percent=$percent";
      exit 1;
   }
   else {
      print "OK - Session utilization at $cur of $max or $percent%" .
            "|percent=$percent";
   }
}


################################################################################
# uptime - check system uptime using HOST-RESOURCE-MIB                         #
################################################################################
sub uptime {
   return $snmp->snmp_uptime( $args );
}


################################################################################
# version - check system platform and version using Aventail MIB               #
################################################################################
sub version {
   my ($version, $platform) = $snmp->snmpget( OID->{version}, OID->{platform});
   my $upgrade = { #'9.1.1' => 'Bug #7213',
                 };

   if (exists $upgrade->{$version}) {
      print "WARNING - Palo Alto code version $version should be " .
            "upgraded: $upgrade->{$version}";
      exit 1;
   }
   else {
      print "OK - Palo Alto $platform version $version";
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
