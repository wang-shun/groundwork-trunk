#!/usr/local/groundwork/perl/bin/perl -w

use strict;
use Net::SNMP ();

use lib q(/usr/local/groundwork/nagios/libexec);
use cache_func ();
use fw_func ();
use nagios_func ();
use parse_func ();
use snmp_func ();

use constant PLUGIN => 'check_nokia_ipso';

use constant OID => { 'version'     => '.1.3.6.1.2.1.1.1.0',
                      'vrrpstate'   => '.1.3.6.1.2.1.68.1.3.1.3',
                      'chassistemp' => '.1.3.6.1.4.1.94.1.21.1.1.5.0',
                      'cardindex'   => '.1.3.6.1.4.1.94.1.21.1.1.6.1.1',
                      'cardstatus'  => '.1.3.6.1.4.1.94.1.21.1.1.6.1.2',
                      'fanindex'    => '.1.3.6.1.4.1.94.1.21.1.2.1.1.1',
                      'fanstatus'   => '.1.3.6.1.4.1.94.1.21.1.2.1.1.2',
                      'psuindex'    => '.1.3.6.1.4.1.94.1.21.1.3.1.1.1',
                      'psuovertemp' => '.1.3.6.1.4.1.94.1.21.1.3.1.1.2',
                      'psustatus'   => '.1.3.6.1.4.1.94.1.21.1.3.1.1.3',
                      'cpuusage'    => '.1.3.6.1.4.1.94.1.21.1.7.1',
                      'fw1license'  => '.1.3.6.1.4.1.94.1.21.1.10.12.1.5',
                      'fw1filter'   => '.1.3.6.1.4.1.2620.1.1.2.0',
                      'fw1accepts'  => '.1.3.6.1.4.1.2620.1.1.4.0',
                      'fw1rejects'  => '.1.3.6.1.4.1.2620.1.1.5.0',
                      'fw1drops'    => '.1.3.6.1.4.1.2620.1.1.6.0',
                      'fw1logs'     => '.1.3.6.1.4.1.2620.1.1.7.0',
                      'fw1conns'    => '.1.3.6.1.4.1.2620.1.1.25.3.0',
                      'hrswrunname' => '.1.3.6.1.2.1.25.4.2.1.2',
                      'ifname'      => '.1.3.6.1.2.1.31.1.1.1.1',
                    };

use constant FUNCTIONS => { 'clock'        => \&clock,
                            'cpu'          => \&cpu,
                            'disk'         => \&disk,
                            'fw1-drops'    => \&fw1_drops,
                            'fw1-policy'   => \&fw1_policy,
                            'fw1-state'    => \&fw1_state,
                            'int_list'     => \&interface,
                            'mem'          => \&memory,
                            'sensors'      => \&sensors,
                            'services'     => \&services,
                            'uptime'       => \&uptime,
                            'version'      => \&version,
                            'vrrp'         => \&vrrp,
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
   return $snmp->snmp_datetime( $args, 1 );
}


################################################################################
# cpu - checks cpu utilization using Nokia MIB                                 #
################################################################################
sub cpu {
   # instantiate variables
   my @output = my @perfdata = ();

   # set warning/critical thresholds
   my ($warn, $crit) = grep /^\d+$/ => split /:/ => $args->{l};
   $warn ||= 85;    # default 85%
   $crit ||= 95;    # default 95%

   # retrieve smp cpu utilization 
   my @cpus = $snmp->snmpbulkwalk( OID->{cpuusage} );

   # loop through the results
   foreach my $i (0.. $#cpus) {
      # sk40025 fix for cpu usage > 100%
      $cpus[$i] > 100 and $cpus[$i] = 100;

      # compare cpu utilization against thresholds
      if ($cpus[$i] >= $crit) {
         push @output, "CRITICAL - CPU $i at $cpus[$i]% utilization" .
                       "(threshold $crit%)";
      }
      elsif ($cpus[$i] >= $warn) {
         push @output, "WARNING - CPU $i at $cpus[$i]% utilization" .
                       "(threshold $warn%)";
      }
      else {
         push @output, "OK - CPU $i at $cpus[$i]% utilization";
      }

      # populate perfdata
      push @perfdata, "cpu$i=$cpus[$i]";
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
# disk - checks disk/partition utilization using HOST-RESOURCE-MIB             #
################################################################################
sub disk {
   return $snmp->snmp_disk( $args );
}


################################################################################
# fw1_drops - retrieves accepts/drops/logs/rejects using CheckPoint MIB        #
################################################################################
sub fw1_drops {
   # define custom callback for snmp failures
   $snmp->{callback} = \&callback_check_fw1;

   my ($absaccepts, $absdrops, $absrejects, $abslogs) = $snmp->snmpget( 
       OID->{fw1accepts}, OID->{fw1drops}, OID->{fw1rejects}, OID->{fw1logs});

   my $accepts = defined $absaccepts ? sprintf("%u", $absaccepts) : 'U';
   my $drops   = defined $absdrops   ? sprintf("%u", $absdrops)   : 'U';
   my $rejects = defined $absrejects ? sprintf("%u", $absrejects) : 'U';
   my $logs    = defined $abslogs    ? sprintf("%u", $abslogs)    : 'U';

   my @perfdata = ( "accepts=$accepts", "drops=$drops", "rejects=$rejects",
                    "logs=$logs" );

   print "OK - @perfdata|@perfdata";
}


################################################################################
# fw1_policy - checks for valid policy loaded using CheckPoint MIB             #
################################################################################
sub fw1_policy {
   # define custom callback for snmp failures
   $snmp->{callback} = \&callback_check_fw1;

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
# fw1_state - retrieves accepts/drops/logs/rejects using CheckPoint MIB        #
################################################################################
sub fw1_state {
   # set thresholds
   # no default warning/critical levels
   # must be explicitly set when calling the plugin
   # we have no way of identifying the maximum state table size via snmp
   # if no thresholds provided, will default to OK state
   my ($warn, $crit) = grep /^\d+$/ => split /:/ => $args->{l};
   
   # define custom callback for snmp failures
   $snmp->{callback} = \&callback_check_fw1;

   # retrieve concurrent connection count
   my $conns = $snmp->snmpget( OID->{fw1conns} );

   # test against thresholds and generate output
   if ($crit && $conns >= $crit) {
      print "CRITICAL - Concurrent connections at $conns (threshold $crit)" .
            "|conns=$conns";
      exit 2;
   } 
   elsif ($warn && $conns >= $warn) {
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
      $int->{int_name} =~ s/^(\S+).*/$1/;
      $int->{int_name} =~ tr|/|-|;
      $int->{int_type} == 6 or $int->{int_type} == 136 or next;
      $args->{l} and $args->{l} =~ /no_in_errors/ and $int->{no_in_errors} = 1;
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
# memory - retrieve linux memory utilization using HOST-RESOURCE-MIB           #
################################################################################
sub memory {
   return $snmp->snmp_memory( $args );
}


################################################################################
# sensors - checks chassis temp, card health, fan health and psu health        #
################################################################################
sub sensors {
   # instantiate variables
   my @output = ();

   # parse filters
   my @filters = split /:/ => $args->{l};

   # disable snmp error checking as some sensor OIDs may not exist
   # depending on hardware / software platform and version
   $snmp->{ec} = 0;

   # retrieve chassis temperature
   my $chassistemp = $snmp->snmpget( OID->{chassistemp} );
   if (! defined $chassistemp) {
      push @output, "UNKNOWN - Chassis temperature OID not found";
   }
   elsif ($chassistemp == 2) {
      push @output, "CRITICAL - Chassis temperature is above the " .
                    "Nokia-supplied threshold";
   }
   else {
      push @output, "OK - Chassis temperature is within Nokia-supplied " .
                    "limits";
   }

   # retrieve status of card slots (interface cards, etc)
   my @cardstatus = $snmp->snmpbulkwalk( OID->{cardstatus} );
   foreach my $i (0 .. $#cardstatus) {
      my $card = $i + 1;
      if (! defined $cardstatus[$i]) {
         push @output, "UNKNOWN - Card #$card status OID not found";
      }
      elsif (grep /^no-card-$card$/ => @filters) {
         push @output, "OK - Card #$card status is ignored",
      }
      elsif ($cardstatus[$i] == 2) {
         push @output, "CRITICAL - Card #$card status is offline";
      }
      else {
         push @output, "OK - Card #$card status is online";
      }
   }

   # retrieve status of system fans
   my @fanstatus = $snmp->snmpbulkwalk( OID->{fanstatus} );
   foreach my $i (0 .. $#fanstatus) {
      my $fan = $i + 1;
      if (! defined $fanstatus[$i]) {
         push @output, "UNKNOWN - Fan #$fan status OID not found";
      }
      elsif ($fanstatus[$i] == 3) {
         push @output, "UNKNOWN - Fan #$fan status is not available";
      }
      elsif ($fanstatus[$i] == 2) {
         push @output, "CRITICAL - Fan #$fan status is not running";
      }
      else {
         push @output, "OK - Fan #$fan status is running";
      }
   }

   # retrieve power supply over temperature alarms
   my @psuovertemp = $snmp->snmpbulkwalk( OID->{psuovertemp} );
   foreach my $i (0 .. $#psuovertemp) {
      my $psu = $i + 1;
      if (! defined $psuovertemp[$i]) {
         push @output, "UNKNOWN - PSU #$psu overtemp OID not found";
      }
      elsif ($psuovertemp[$i] == 3) {
         push @output, "UNKNOWN - PSU #$psu overtemp is not available";
      }
      elsif ($psuovertemp[$i] == 2) {
         push @output, "CRITICAL - PSU #$psu is above the Nokia-supplied " .
                       "threshold";
      }
      else {
         push @output, "OK - PSU #$psu is within the Nokia-supplied threshold";
      }
   }

   # retreive status of power supplies
   my @psustatus = $snmp->snmpbulkwalk( OID->{psustatus} );
   foreach my $i (0 .. $#psustatus) {
      my $psu = $i + 1;
      if (! defined $psustatus[$i]) {
         push @output, "UNKNOWN - PSU #$psu status OID not found";
      }
      elsif ($psustatus[$i] == 3) {
         push @output, "UNKNOWN - PSU #$psu status is not available";
      }
      elsif ($psustatus[$i] == 2) {
         push @output, "CRITICAL - PSU #$psu stautus is not running";
      }
      else {
         push @output, "OK - PSU #$psu status is running";
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
# services - check splat services running using HOST-RESOURCE-MIB              #
################################################################################
sub services {
   # instantiate some arrays to hold running/notrunning services
   my @run = my @notrun = ();

   # list of processes to look for
   my @procs = qw(cpd fwd? cpsnmpd);

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
# version - check system version using SNMPv2 MIB                              #
################################################################################
sub version {
   # retrieve version
   my $version = $snmp->snmpget( OID->{version} );

   # parse results
   ($version) = $version =~ m/IPSO \S+ (\S+) rel/;

   # alarm upgrade hash
   my $upgrade = { #'9.1.1' => 'Bug #7213',
                 };

   # generate output
   if (exists $upgrade->{$version}) {
      print "WARNING - Nokia IPSO $version should be upgraded: " .
            "$upgrade->{$version}";
      exit 1;
   }
   else {
      print "OK - Nokia IPSO $version";
   }
}


################################################################################
# vrrp - check vrrp status and detect failovers                                #
################################################################################
sub vrrp {
   # instantiate variables
   my @output = ();
   my $vrrpoid = OID->{vrrpstate};   # copy to variable to use in regex
   my $vrrpval = { qw/1 initialize 2 backup 3 master/ };   

   # disable snmp error checking
   $snmp->{ec} = 0;

   # retreive current vrrp state for all interfaces
   my $vrrpstate = $snmp->snmpbulkwalk( OID->{vrrpstate} );

   # test that we actually received some output since ec is off
   if (! scalar keys %$vrrpstate) {
      print "OK - VRRP doesn't appear to be enabled";
      return;
   }

   # retrieve vrrp states from the last successful check
   # save current vrrp states for the next check
   my $cache = cache_func->new( $args->{h} );
   my $cached_vrrpstate = $cache->get('vrrp');
   $cache->set( 'vrrp', $vrrpstate );

   # loop through each interface to see if the state has changed
   foreach my $oid (Net::SNMP::oid_lex_sort( keys %$vrrpstate )) {
      my ($if_id) = $oid =~ /^$vrrpoid(?:[0-9.]+)?\.([0-9]+)\.[0-9]+$/;
      my $interface = $snmp->snmpget( OID->{ifname} . ".$if_id" );
      my $cached  = $cached_vrrpstate->{$oid};
      my $current = $vrrpstate->{$oid};
      if (defined $cached && $cached != $current) {
         push @output, "CRITICAL - Interface $interface vrrp state changed " .
                       "from $vrrpval->{ $cached } to $vrrpval->{ $current }";
      }
      else {
         push @output, "OK - Interface $interface vrrp state is " .
                       "$vrrpval->{ $current }";
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
      print "OK - $ok vrrp healthy\n";
      print join "\n" => @sorted;
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


################################################################################
# callback_check_fw1 - callback routine for snmp failures                      #
# in the case that the fw1-specific checks fail, we will just exit out with    #
# unknown status.                                                              #
################################################################################
sub callback_check_fw1 {
   # we expect a nagios status code and message to be passed
   my ($code, $msg) = @_;

   $snmp->die3($msg);
}
