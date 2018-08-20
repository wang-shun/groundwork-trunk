#!/usr/local/groundwork/perl/bin/perl -w

use strict;
use lib q(/usr/local/groundwork/nagios/libexec);
use parse_func ();
use snmp_func ();
use nagios_func ();

use constant PLUGIN => 'check_utm1edge';

use constant OID => { 'platform'    => '.1.3.6.1.4.1.6983.1.1.2.0',
                      'configtotal' => '.1.3.6.1.4.1.6983.1.2.1.1.0',
                      'configfree'  => '.1.3.6.1.4.1.6983.1.2.1.2.0',
                      'firmtotal'   => '.1.3.6.1.4.1.6983.1.2.2.1.0',
                      'firmfree'    => '.1.3.6.1.4.1.6983.1.2.2.2.0',
                      'cftotal'     => '.1.3.6.1.4.1.6983.1.2.3.1.0',
                      'cffree'      => '.1.3.6.1.4.1.6983.1.2.3.2.0',
                      'licname'     => '.1.3.6.1.4.1.6983.1.3.3.0',
                      'usednodes'   => '.1.3.6.1.4.1.6983.1.3.4.0',
                      'firmware'    => '.1.3.6.1.4.1.6983.1.4.1.0',
                      'ramfree'     => '.1.3.6.1.4.1.6983.1.5.1.1.0',
                      'ramtotal'    => '.1.3.6.1.4.1.6983.1.5.1.2.0',
                      'dfafree'     => '.1.3.6.1.4.1.6983.1.5.2.1.0',
                      'dfatotal'    => '.1.3.6.1.4.1.6983.1.5.2.2.0',
                      'usermemfree' => '.1.3.6.1.4.1.6983.1.5.3.0',
                      'kernmemfree' => '.1.3.6.1.4.1.6983.1.5.4.0',
                      'fwmemfree'   => '.1.3.6.1.4.1.6983.1.5.5.0',
                    };

use constant FUNCTIONS => { 'cpu'          => \&cpu,
                            'disk'         => \&disk,
                            'int_list'     => \&interface,
                            'license'      => \&license,
		            'load'         => \&load,
                            'mem'          => \&memory,
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
# cpu - checks cpu utilization using ucDvais MIB; no smp support               #
################################################################################
sub cpu {
   return $snmp->ucd_cpu( $args );
}


################################################################################
# disk - checks disk/partition utilization using UTM-1 Edge MIB                #
################################################################################
sub disk {
   # instantiate variables
   my @output = my @perfdata = ();

   # set thresholds
   my ($warn, $crit) = grep /^\d+$/ => split /:/ => $args->{l};
   $warn ||= 80;
   $crit ||= 90;

   # verify the utm-1 edge mib exists
   firmware();

   # retrieve the config partition OIDs
   my ($configtotal, $configfree) = $snmp->snmpget( OID->{configtotal},
                                                    OID->{configfree} );

   # calculate config partition percentage used
   my $configpercent = do {
      if ($configtotal) {
         sprintf "%d", 100 - $configfree / $configtotal * 100;
      }
      else {
         0;
      }
   };

   # populate perfdata
   push @perfdata, "config=$configpercent";

   # test against thresholds
   if ($configpercent >= $crit) {
      push @output, "CRITICAL - Config storage $configpercent% " .
                    "utilization (threshold $crit%)";
   }
   elsif ($configpercent >= $warn) {
      push @output, "WARNING - Config storage $configpercent% " .
                    "utilization (threshold $warn%)";
   }
   else {
      push @output, "OK - Config storage $configpercent% utilization";
   }

   # retrieve the firmware partition OIDs 
   my ($firmtotal, $firmfree) = $snmp->snmpget( OID->{firmtotal}, 
                                                OID->{firmfree} );

   # calculate firmware partition percentage used
   my $firmpercent = do {
      if ($firmtotal) {
         sprintf "%d", 100 - $firmfree / $firmtotal * 100;
      }
      else {
         0;
      }
   };

   # populate perfdata
   push @perfdata, "firmware=$firmpercent";

   # test against thresholds
   if ($firmpercent >= $crit) {
      push @output, "CRITICAL - Firmware storage $firmpercent% " .
                    "utilization (threshold $crit%)";
   }
   elsif ($firmpercent >= $warn) {
      push @output, "WARNING - Firmware storage $firmpercent% " .
                    "utilization (threshold $warn%)";
   }
   else {
      push @output, "OK - Firmware storage $firmpercent% utilization";
   }
 
   # retrieve the compact flash partition OIDs 
   my ($cftotal, $cffree) = $snmp->snmpget(OID->{cftotal}, OID->{cffree});

   # calculate compact flash partition percentage used
   my $cfpercent = do {
      if ($cftotal) {
         sprintf "%d", 100 - $cffree / $cftotal * 100;
      }
      else {
         0;
      }
   };

   # populate perfdata
   push @perfdata, "compactflash=$cfpercent";

   # test against thresholds
   if ($cfpercent >= $crit) {
      push @output, "CRITICAL - Compact flash storage $cfpercent% " .
                    "utilization (threshold $crit%)";
   }
   elsif ($cfpercent >= $warn) {
      push @output, "WARNING - Compact flash storage $cfpercent% " .
                    "utilization (threshold $warn%)";
   }
   else {
      push @output, "OK - Compact flash storage $cfpercent% utilization";
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
      print "OK - $ok storage checks healthy|@perfdata\n";
      print join "\n" => @sorted;
   }
}

 
################################################################################
# firmware - retrieve UTM-1 Edge firmware if available                         #
# returns the firmware version                                                 #
################################################################################
sub firmware {
   # disable error checking
   $snmp->{ec} = 0;

   $snmp->snmpget( OID->{firmware} ) or 
      $snmp->die3("This device doesn't support the UTM-1 Edge MIB"); 

   # enable error checking
   $snmp->{ec} = 1;
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
      $int->{no_in_errors} = 1;    # disable input error checking
      $int->{no_out_errors} = 1;   # disable output error checking
      $nagios->interface_status_passive( $int );
      next unless ( ($int->{int_in_oct}  && $int->{int_in_oct}  ne 'U') || 
                    ($int->{int_out_oct} && $int->{int_out_oct} ne 'U') );
      push @int_list, "${i}:$int->{int_name}";
      $nagios->interface_stats_passive( $int );
      $nagios->interface_problems_passive( $int );
   }
   print "OK - Interfaces with traffic counters: @int_list";
}


################################################################################
# interface - retrieves interface statistics for each interface                #
################################################################################
sub license {
   # set thresholds
   my ($warn, $crit) = grep /^\d+$/ => split /:/ => $args->{l};
   $warn ||= 14;   # warning 14 days
   $crit ||= 7;    # critical 7 days

   # verify the utm-1 edge mib exists
   firmware();

   # retrieve the license OIDs
   my ($licname, $usednodes) = $snmp->snmpget(OID->{licname}, OID->{usednodes});
   my ($licensedfor) = $licname =~ /(\S+) nodes/;
   my $perfdata = "usednodes=$usednodes";

   # test against thresholds and generate output
   if (!$licensedfor) {
      print "UNKNOWN - Unable to determine node limit|$perfdata";
      exit 3;
   }
   elsif ($licensedfor =~ /unlimited/) {
      print "OK - Nodes used $usednodes of $licensedfor|$perfdata";
   }
   elsif ($usednodes >= $licensedfor * $crit / 100) {
      print "CRITICAL - Nodes used $usednodes of $licensedfor " .
            "(threshold $crit%)|$perfdata";
      exit 2;
   }
   elsif ($usednodes >= $licensedfor * $warn / 100) {
      print "WARNING - Nodes used $usednodes of $licensedfor " .
            "(threshold $warn%)|$perfdata";
      exit 1;
   }
   else {
      print "OK - Nodes used $usednodes of $licensedfor|$perfdata";
   }
}


################################################################################
# load - retrieve linux load average using ucDavis MIB                         #
################################################################################
sub load {
   return $snmp->ucd_load( $args );
}


################################################################################
# memory - retrieve memory utilization using UTM-1 Edge MIB
################################################################################
sub memory {
   # instantiate variables
   my @output = my @perfdata = ();
   my $lowfree = 128;   # low memory threshold in kB

   # set thresholds
   my ($warn, $crit) = grep /^\d+$/ => split /:/ => $args->{l};
   $warn ||= 80;        # warning  80 %
   $crit ||= 90;        # critical 90 %
   
   # verify the utm-1 edge mib exists
   firmware();

   # retrieve physical memory OIDs
   my ($ramfree, $ramtotal) = $snmp->snmpget( OID->{ramfree}, OID->{ramtotal} );

   # calculate physical memory percentage used
   my $rampercent = do {
      if ($ramtotal) {
         sprintf("%d", 100 - $ramfree / $ramtotal * 100);
      }
      else {
         0;
      }
   };

   # populate perfdata
   push @perfdata, "ram=$rampercent";
  
   # test against thresholds 
   if ($rampercent >= $crit) {
      push @output, "CRITICAL - Physical memory $rampercent% utilization " .
                    "(threshold $crit%)";
   }
   elsif ($rampercent >= $warn) {
      push @output, "WARNING - Physical memory $rampercent% utilization " .
                    "(threshold $warn%)";
   }
   else {
      push @output, "OK - Physical memory $rampercent% utilization";
   }

   # retrieve DFA memory OIDs
   my ($dfafree, $dfatotal) = $snmp->snmpget(OID->{dfafree}, OID->{dfatotal});

   # calculate DFA memory percentage used
   my $dfapercent = do {
      if ($dfatotal) {
         sprintf("%d", 100 - $dfafree / $dfatotal * 100);
      }
      else {
         0;
      }
   };

   # populate perfdata
   push @perfdata, "dfa=$dfapercent";
  
   # test against thresholds 
   if ($dfapercent >= $crit) {
      push @output, "CRITICAL - DFA memory $dfapercent% utilization " .
                    "(threshold $crit%)";
   }
   elsif ($dfapercent >= $warn) {
      push @output, "WARNING - DFA memory $dfapercent% utilization " .
                    "(threshold $warn%)";
   }
   else {
      push @output, "OK - DFA memory $dfapercent% utilization";
   }

   # retrieve user, kernel and firewall free memory
   my ($usermemfree, $kernmemfree, $fwmemfree) = $snmp->snmpget(
                                                    OID->{usermemfree},
                                                    OID->{kernmemfree},
                                                    OID->{fwmemfree});

   # test user memory against threshold
   if ($usermemfree <= $lowfree) {
      push @output, "CRITICAL - User memory free $usermemfree kB " .
                    "(threshold $lowfree kB)";
   }
   else {
      push @output, "OK - User memory free $usermemfree kB";
   }

   # test kernel memory against threshold
   if ($kernmemfree <= $lowfree) {
      push @output, "CRITICAL - Kernel memory free $kernmemfree kB " .
                    "(threshold $lowfree kB)";
   }
   else {
      push @output, "OK - Kernel memory free $kernmemfree kB";
   }

   # test firewall memory against threshold
   if ($fwmemfree <= $lowfree) {
      push @output, "CRITICAL - Firewall memory free $fwmemfree kB " .
                    "(threshold $lowfree kB)";
   }
   else {
      push @output, "OK - Firewall memory free $fwmemfree kB";
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
      print "OK - $ok memory checks healthy|@perfdata\n";
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
# version - check system platform and version using UTM-1 Edge MIB             #
################################################################################
sub version {
   # verify the utm-1 edge mib exists
   firmware();

   my ($platform, $version) = $snmp->snmpget( OID->{platform}, 
                                              OID->{firmware} );
   my $upgrade = { #'9.1.1' => 'Bug #7213',
                 };

   if (exists $upgrade->{$version}) {
      print "WARNING - CheckPoint UTM-1 Edge $version should be " .
            "upgraded: $upgrade->{$version}";
      exit 1;
   }
   else {
      print "OK - CheckPoint UTM-1 Edge $platform version $version";
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
