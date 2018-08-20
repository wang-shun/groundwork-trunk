#!/usr/local/groundwork/perl/bin/perl -w

use strict;
use lib q(/usr/local/groundwork/nagios/libexec);
use cache_func ();
use fw_func ();
use nagios_func ();
use parse_func ();
use snmp_func ();

use constant PLUGIN => 'check_splat';

use constant OID => { 'hrswrunname' => '.1.3.6.1.2.1.25.4.2.1.2',
                      'fw1filter'   => '.1.3.6.1.4.1.2620.1.1.2.0',
                      'fw1accepts'  => '.1.3.6.1.4.1.2620.1.1.4.0',
                      'fw1rejects'  => '.1.3.6.1.4.1.2620.1.1.5.0',
                      'fw1drops'    => '.1.3.6.1.4.1.2620.1.1.6.0',
                      'fw1logs'     => '.1.3.6.1.4.1.2620.1.1.7.0',
                      'fw1conns'    => '.1.3.6.1.4.1.2620.1.1.25.3.0',
                      'hainstalled' => '.1.3.6.1.4.1.2620.1.5.2.0',
                      'hastarted'   => '.1.3.6.1.4.1.2620.1.5.5.0',
                      'hastate'     => '.1.3.6.1.4.1.2620.1.5.6.0',
                      'platform'    => '.1.3.6.1.4.1.2620.1.6.5.1.0',
                      'version'     => '.1.3.6.1.4.1.2620.1.6.5.7.0',
                      'smpindex'    => '.1.3.6.1.4.1.2620.1.6.7.5.1.1',
                      'smpuser'     => '.1.3.6.1.4.1.2620.1.6.7.5.1.2',
                      'smpsystem'   => '.1.3.6.1.4.1.2620.1.6.7.5.1.3',
                      'smpidle'     => '.1.3.6.1.4.1.2620.1.6.7.5.1.4',
                      'smpusage'    => '.1.3.6.1.4.1.2620.1.6.7.5.1.5',
                    };

use constant FUNCTIONS => { 'clock'        => \&clock,
                            'cpu'          => \&cpu,
                            'disk'         => \&disk,
                            'failover'     => \&failover,
                            'fw1-policy'   => \&fw1_policy,
                            'fw1-drops'    => \&fw1_drops,
                            'fw1-state'    => \&fw1_state,
                            'int_list'     => \&interface,
                            'load'         => \&load,
                            'mem'          => \&memory,
                            'services'     => \&services,
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
# clock - compare local/remote clocks using HOST-RESOURCE-MIB                  #
################################################################################
sub clock {
   return $snmp->snmp_datetime( $args );
}


################################################################################
# cpu - checks smp cpu utilization using CheckPoint MIB                        #
# fallback to ucDavis MIB if CheckPoint MIB fails (up)                         #
################################################################################
sub cpu {
   # instantiate variables
   my @output = my @perfdata = ();
   my @order = qw/user system idle/;

   # set warning/critical thresholds
   my ($warn, $crit) = grep /^\d+$/ => split /:/ => $args->{l};
   $warn ||= 85;    # default 85%
   $crit ||= 95;    # default 95%

   # disable error checking
   $snmp->{ec} = 0;

   # retrieve a list of cpu indexes (error-checking disabled)
   my @cpuindex = $snmp->snmpbulkwalk( OID->{smpindex} );

   # enable error checking
   $snmp->{ec} = 1;

   # default to ucDavis cpu check if CheckPoint MIB doesn't work
   if ( grep { ! defined } @cpuindex ) {
      return $snmp->ucd_cpu( $args );
   }

   # loop through each cpu to retrieve utilization
   foreach my $cpuid (@cpuindex) {
      my @oids = map { sprintf "%s.%s.0", OID->{"smp$_"}, $cpuid } @order;
      my $cpu = {};
      @{ $cpu }{ @order } = $snmp->snmpget( @oids );
      my $usage = sprintf "%d", $cpu->{user} + $cpu->{system};
      my @data = map { "$_=$cpu->{$_}" } @order;
      if ($usage >= $crit) {
         push @output, "CRITICAL - CPU $cpuid at $usage% utilization " .
                       "(threshold $crit%) [@data]";
      }
      elsif ($usage >= $warn) {
         push @output, "WARNING - CPU $cpuid at $usage% utilization " .
                       "(threshold $warn%) [@data]";
      }
      else {
         push @output, "OK - CPU $cpuid at $usage% utilization [@data]";
      }
      push @perfdata, "cpu$cpuid=$usage";
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
# disk - checks disk/partition utilization using standard HOST-RESOURCE-MIB    #
################################################################################
sub disk {
   return $snmp->snmp_disk( $args );
}


################################################################################
# failover - checks failover state against the last stored state               #
################################################################################
sub failover {
   # set snmpget retries to 2
   $snmp->{retries} = 2;

   # disable snmp error checking
   # some standalone firewalls seem to act as if oid doesn't exist (R60?)
   $snmp->{ec} = 0;

   # retrieve current HA state
   my $hastate = $snmp->snmpget( OID->{hastate} );

   # check results to see if anything returned
   if (!defined $hastate) {
      print "OK - CheckPoint High Availability not running";
      exit 0;
   }

   # retrieve saved HA state and save current HA state for next check
   my $cache = cache_func->new( $args->{h} );
   my $cached = $cache->get( 'failover' );
   $cache->set( 'failover', $hastate );

   # test state and generate output
   if (defined $cached && $cached ne $hastate) {
      print "CRITICAL - Failover state changed from $cached to $hastate";
      exit 2;
   }
   else {
      print "OK - Failover state is $hastate";
   }
}


################################################################################
# fw1_policy - checks for valid policy loaded using CheckPoint MIB             #
################################################################################
sub fw1_policy {
   # retrieve policy name 
   my $policy = $snmp->snmpget( OID->{fw1filter} );

   # test policy and generate output
   if ($policy eq '') {
      # no policy (blank)
      print "CRITICAL - No FW-1 policy loaded ($policy)";
      exit 2;
   }
   elsif ($policy eq 'defaultfilter') {
      # default policy loaded
      print "CRITICAL - FW-1 using default filter ($policy)";
      exit 2;
   }
   elsif ($policy eq 'InitialPolicy') {
      # initial policy loaded
      print "CRITICAL - FW-1 using initial policy ($policy)";
      exit 2;
   }
   else {
      # custom policy
      print "OK - FW-1 using custom policy ($policy)";
   }
}


################################################################################
# fw1_drops - retrieves accepts/drops/logs/rejects using CheckPoint MIB        #
################################################################################
sub fw1_drops {
   # retrieve snmp oids
   my ($absaccepts, $absdrops, $absrejects, $abslogs) = $snmp->snmpget( 
      OID->{fw1accepts}, OID->{fw1drops}, OID->{fw1rejects}, OID->{fw1logs});

   # check for defined value or set to 'U'nknown
   my $accepts = defined $absaccepts ? sprintf("%u", $absaccepts) : 'U';
   my $drops   = defined $absdrops   ? sprintf("%u", $absdrops)   : 'U';
   my $rejects = defined $absrejects ? sprintf("%u", $absrejects) : 'U';
   my $logs    = defined $abslogs    ? sprintf("%u", $abslogs)    : 'U';

   # populate perfdata
   my @perfdata = ( "accepts=$accepts", "drops=$drops", "rejects=$rejects",
                    "logs=$logs" );

   # generate output
   print "OK - [@perfdata]|@perfdata";
}


################################################################################
# fw1_state - retrieves accepts/drops/logs/rejects using CheckPoint MIB        #
################################################################################
sub fw1_state {
   # set thresholds
   my ($warn, $crit) = grep /^\d+$/ => split /:/ => $args->{l};
   $warn ||= 20000;    # default 20000 of 25000
   $crit ||= 23000;    # default 23000 of 25000

   # retrieve concurrent connection count
   my $conns = $snmp->snmpget( OID->{fw1conns} );

   # test against thresholds and generate output
   if ($conns >= $crit) {
      print "CRITICAL - Concurrent connections at $conns (threshold $crit)" .
            "|conns=$conns";
      exit 2;
   } 
   elsif ($conns >= $warn) {
      print "WARNING - Concurrent connections at $conns (threshold $warn)" .
            "|conns=$conns";
      exit 1;
   } 
   else {
      print "OK - Concurrent connections at $conns|conns=$conns";
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
# load - retrieve linux load average using ucDavis MIB                         #
################################################################################
sub load {
   return $snmp->ucd_load( $args );
}


################################################################################
# memory - retrieve linux memory utilization using ucDavis MIB                 #
################################################################################
sub memory {
   return $snmp->ucd_memory( $args );
}


################################################################################
# services - check splat services running using HOST-RESOURCE-MIB              #
################################################################################
sub services {
   # instantiate some arrays to hold running/notrunning services
   my (@run, @notrun) = ();

   # list of processes to look for
   my @procs = qw(cpd fwd? cpsnmpagentx);

   # retrieve list of all running processes
   my @hrswrunname = $snmp->snmpbulkwalk( OID->{hrswrunname} );

   # loop throuch each process and add to run/notrun arrays
   foreach my $proc (@procs) {
      (my $trim = $proc) =~ tr/\?//d;
      if (grep /^$proc$/ => @hrswrunname) {
         push @run => $trim;
      }
      else {
         push @notrun => $trim;
      }
   }
     
   # generate output 
   if (my $nr = @notrun) {
      print "CRITICAL - $nr services not running [@notrun]";
      exit 2;
   }
   else {
      print "OK - All services running [@run]";
   }
}


################################################################################
# uptime - check system uptime using HOST-RESOURCE-MIB                         #
################################################################################
sub uptime {
   return $snmp->snmp_uptime( $args );
}


################################################################################
# version - check system platform and version using CheckPoint MIB             #
################################################################################
sub version {
   # retrieve platform and version
   my ($platform, $version) = $snmp->snmpget( OID->{platform}, OID->{version} );

   # instantiate upgrade hash
   my $upgrade = { #'9.1.1' => 'Bug #7213',
                 };

   # generate output
   if (exists $upgrade->{$version}) {
      print "WARNING - SPLAT version $version should be upgraded: " .
            "$upgrade->{$version}";
      exit 1;
   }
   else {
      print "OK - $platform version $version";
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
