#!/usr/local/groundwork/perl/bin/perl -w

use strict;
use Net::SNMP ();

use lib q(/usr/local/groundwork/nagios/libexec);
use nagios_func ();
use parse_func ();
use snmp_func ();

use constant PLUGIN => 'check_infoblox';

use constant OID => { 'cputemp'     => '.1.3.6.1.4.1.7779.3.1.1.2.1.1.0', 
                      'platform'    => '.1.3.6.1.4.1.7779.3.1.1.2.1.4.0',
                      'version'     => '.1.3.6.1.4.1.7779.3.1.1.2.1.7.0',
                      'cpuusage'    => '.1.3.6.1.4.1.7779.3.1.1.2.1.8.1.1.0',
                      'memusage'    => '.1.3.6.1.4.1.7779.3.1.1.2.1.8.2.1.0',
                      'svcname'     => '.1.3.6.1.4.1.7779.3.1.1.2.1.9.1.1',
                      'svcstatus'   => '.1.3.6.1.4.1.7779.3.1.1.2.1.9.1.2',
                      'svcdesc'     => '.1.3.6.1.4.1.7779.3.1.1.2.1.9.1.3',
                      'n1svcname'   => '.1.3.6.1.4.1.7779.3.1.1.2.1.10.1.1',
                      'n1svcstatus' => '.1.3.6.1.4.1.7779.3.1.1.2.1.10.1.2',
                      'n1svcdesc'   => '.1.3.6.1.4.1.7779.3.1.1.2.1.10.1.3',
                      'n2svcname'   => '.1.3.6.1.4.1.7779.3.1.1.2.1.11.1.1',
                      'n2svcstatus' => '.1.3.6.1.4.1.7779.3.1.1.2.1.11.1.2',
                      'n2svcdesc'   => '.1.3.6.1.4.1.7779.3.1.1.2.1.11.1.3',
                      'dhcpused'    => '.1.3.6.1.4.1.7779.3.1.1.4.1.1.1.3',
                    };

use constant FUNCTIONS => { 'clock'        => \&clock,
                            'cpu'          => \&cpu,
                            'dhcp'         => \&dhcp,
                            'disk'         => \&disk,
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
# cpu - checks cpu utilization using InfoBlox MIB                              #
################################################################################
sub cpu {
   # set thresholds
   my ($warn, $crit) = grep /^\d+$/ => split /:/ => $args->{l};
   $warn ||= 85;    # default 85%
   $crit ||= 95;    # default 95%

   # retrieve CPU utilization in percentage
   my $cpuusage = $snmp->snmpget( OID->{cpuusage} );
   my $perfdata = "cpu=$cpuusage";

   if ($cpuusage >= $crit) { 
      print "CRITICAL - CPU usage at $cpuusage% (threshold $crit%)|$perfdata";
      exit 2;
   }
   elsif ($cpuusage >= $warn) {
      print "WARNING - CPU usage at $cpuusage% (threshold $warn%)|$perfdata";
      exit 1;
   }
   else {
      print "OK - CPU usage at $cpuusage%|$perfdata";
   }
}


################################################################################
# dhcp - checks each dhcp scope utilization
################################################################################
sub dhcp {
   # instantiate variables
   my @output = ();

   # set thresholds
   my ($warn, $crit) = grep /^\d+$/ => split /:/ => $args->{l};
   $warn ||= 90;    # default 90%
   $crit ||= 95;    # default 95%

   # retrieve all dhcp scopes and percentages used
   my $dhcp = $snmp->snmpbulkwalk( OID->{dhcpused} );

   # parse through results and check against thresholds
   my $dhcpused = OID->{dhcpused};
   foreach my $key (Net::SNMP::oid_lex_sort( keys %$dhcp )) {
      my $value = $dhcp->{ $key };
      my ($suffix) = $key =~ /^$dhcpused\.[0-9]+\.([0-9.]+)$/;
      my $scope = join('', map { chr($_) } split(/\./, $suffix));
      if ($value >= $crit) {
         push @output, "CRITICAL - DHCP scope $scope at $value% utilization " .
                       "(threshold $crit%)";
      }
      elsif ($value >= $warn) {
         push @output, "WARNING - DHCP scope $scope at $value% utilization " .
                       "(threshold $warn%)";
      }
      else {
         push @output, "OK - DHCP scope $scope at $value% utilization";
      }
   }

   # generate output
   if (my @critical = grep /CRITICAL/ => @output) {
      print join("\n", @critical, grep(!/CRITICAL/, @output));
      exit 2;
   }
   elsif (my @warning = grep /WARNING/ => @output) {
      print join("\n", @warning, grep(!/WARNING/, @output));
      exit 1;
   }
   else {
      print "OK - @{[scalar @output]} dhcp scopes healthy\n";
      print join "\n", @output;
   }
}


################################################################################
# disk - checks disk/partition utilization using standard HOST-RESOURCE-MIB    #
################################################################################
sub disk {
   return $snmp->snmp_disk( $args );
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
# memory - retrieve memory utilization using InfoBlox MIB                      #
################################################################################
sub memory {
   # set thresholds
   my ($warn, $crit) = grep /^\d+$/ => split /:/ => $args->{l};
   $warn ||= 80;    # default 80%
   $crit ||= 90;    # default 90%

   # retrieve memory utilization in percentage
   my $memusage = $snmp->snmpget( OID->{memusage} );
   my $perfdata = "mem=$memusage"; 

   # test against threhsolds and generate output
   if ($memusage >= $crit) { 
      print "CRITICAL - MEM usage at $memusage% (threshold $crit%)|$perfdata";
      exit 2;
   }
   elsif ($memusage >= $warn) {
      print "WARNING - MEM usage at $memusage% (threshold $warn%)|$perfdata";
      exit 1;
   }
   else {
      print "OK - MEM usage at $memusage%|$perfdata";
   }
}


################################################################################
# services - check status of all InfoBlox services                             #
################################################################################
sub services {
   # instantiate variables
   my @output = ();
   my $states =   { 1 => 'working',
                    2 => 'warning',
                    3 => 'failed',
                    4 => 'inactive',
                    5 => 'unknown',
                  };
   my $services = { 1  => 'dhcp',
                    2  => 'dns',
                    3  => 'ntp',
                    4  => 'tftp',
                    5  => 'http-file-dist',
                    6  => 'ftp',
                    7  => 'bloxtools-move',
                    8  => 'bloxtools',
                    9  => 'node-status',
                    10 => 'disk-usage',
                    11 => 'enet-lan',
                    12 => 'enet-lan2',
                    13 => 'enet-ha',
                    14 => 'enet-mgmt',
                    15 => 'lcd',
                    16 => 'memory',
                    17 => 'replication',
                    18 => 'db-object',
                    19 => 'raid-summary',
                    20 => 'raid-disk1',
                    21 => 'raid-disk2',
                    22 => 'raid-disk3',
                    23 => 'raid-disk4',
                    24 => 'raid-disk5',
                    25 => 'raid-disk6',
                    26 => 'raid-disk7',
                    27 => 'raid-disk8',
                    28 => 'fan1',
                    29 => 'fan2',
                    30 => 'fan3',
                    31 => 'fan4',
                    32 => 'fan5',
                    33 => 'fan6',
                    34 => 'fan7',
                    35 => 'fan8',
                    36 => 'power-supply1',
                    37 => 'power-supply2',
                    38 => 'ntp-sync',
                    39 => 'cpu1-temp',
                    40 => 'cpu2-temp',
                    41 => 'sys-temp',
                    42 => 'raid-battery',
                    43 => 'cpu-usage',
                    44 => 'ospf',
                    45 => 'bgp',
                    46 => 'mgm-service',
                    47 => 'subgrid-conn',
                    48 => 'network-capacity',
                    49 => 'reporting',
                  };

   # retrieve list of all service names
   my @svcnames = $snmp->snmpbulkwalk( OID->{svcname} );

   # loop through each service to check its status
   foreach my $i (@svcnames) {
      my $svcstatus = sprintf "%s.%d", OID->{svcstatus}, $i;
      my $svcdesc   = sprintf "%s.%d", OID->{svcdesc}, $i;
      my ($status, $desc) = $snmp->snmpget( $svcstatus, $svcdesc );
      my $svcname   = $services->{ $i };
      my $svcstate  = $states->{ $status };
      if ($status == 3) {
         push @output, "CRITICAL - $svcname ($svcstate): $desc";
      }
      elsif ($status == 2) {
         push @output, "WARNING - $svcname ($svcstate): $desc";
      }
      else {
         push @output, "OK - $svcname ($svcstate): $desc";
      }
   }

   # generate output
   if (my @critical = grep /CRITICAL/ => @output) {
      print join("\n", @critical, grep(!/CRITICAL/, @output));
      exit 2;
   }
   elsif (my @warning = grep /WARNING/ => @output) {
      print join("\n", @warning, grep(!/WARNING/, @output));
      exit 1;
   }
   else {
      print "OK - @{[scalar @output]} services healthy\n";
      print join "\n", @output;
   }
}


################################################################################
# uptime - check system uptime using HOST-RESOURCE-MIB                         #
################################################################################
sub uptime {
   return $snmp->snmp_uptime( $args );
}


################################################################################
# version - check system platform and version using InfoBlox MIB               #
################################################################################
sub version {
   my ($platform, $version) = $snmp->snmpget( OID->{platform}, OID->{version} );
   my $upgrade = {#'9.1.1' => 'Bug #7213',
                 };

   if (exists $upgrade->{$version}) {
      print "WARNING - Infoblox version $version should be " .
            "upgraded: $upgrade->{$version}";
      exit 1;
   }
   else {
      print "OK - Infoblox $platform version $version";
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

