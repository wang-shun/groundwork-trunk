#!/usr/local/groundwork/perl/bin/perl -w

use strict;
use lib q(/usr/local/groundwork/nagios/libexec);
use nagios_func ();
use parse_func ();
use snmp_func ();

use constant PLUGIN => 'check_nortel_vpn';

use constant OID => { 'iftype'        => '.1.3.6.1.2.1.2.2.1.3',
                      'sysdescr'      => '.1.3.6.1.2.1.1.1.0',
                      'hrmemorysize'  => '.1.3.6.1.2.1.25.2.2.0',
                      'hrdeviceindex' => '.1.3.6.1.2.1.25.3.2.1.1',
                      'hrdevicetype'  => '.1.3.6.1.2.1.25.3.2.1.2',
                      'hrdevicedesc'  => '.1.3.6.1.2.1.25.3.2.1.3',
                    };

use constant FUNCTIONS => { 'clock'        => \&clock,
                            'cpu'          => \&cpu,
                            'disk'         => \&disk,
                            'int_list'     => \&interface,
                            'mem'          => \&memory,
                            'uptime'       => \&uptime,
                            'users'        => \&users,
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
# cpu - checks cpu utilization using HOST-RESOURCE-MIB                         #
################################################################################
sub cpu {
   return $snmp->snmp_cpu( $args );
}

  
################################################################################
# disk - checks disk/partition utilization using HOST-RESOURCE-MIB             #
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
      $int->{int_name} =~ tr/\c@//d;
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
# memory - retrieve linux memory utilization using HOST-RESOURCE-MIB           #
################################################################################
sub memory {
   return $snmp->snmp_memory( $args );
}


################################################################################
# uptime - check system uptime using HOST-RESOURCE-MIB                         #
################################################################################
sub uptime {
   return $snmp->snmp_uptime( $args );
}


################################################################################
# users - check number of concurrent users against limits                      #
################################################################################
sub users {
   # set thresholds
   my ($warn, $crit) = grep /^\d+$/ => split /:/ => $args->{l};
   $warn ||= 80;    # default 80%
   $crit ||= 90;    # default 90%

   # calculate the number of maximum users supported by this platform
   my $maxusers = calculate_user_limit()->{users};

   # retrieve count of current users (iftype 131 == tunnel)
   my $users = () = grep { $_ == 131 } $snmp->snmpbulkwalk( OID->{iftype} );

   # calculate percentage of users to maximum
   my $percent = sprintf "%d", $users / $maxusers * 100;   

   # test against thresholds and generate output
   if ($percent >= $crit) {
      print "CRITICAL - Concurrent users at $users of $maxusers or $percent% " .
            "utilization (threshold $crit%)|current=$users";
      exit 2;
   }
   elsif ($percent >= $warn) {
      print "WARNING - Concurrent users at $users of $maxusers or $percent% " .
            "utilization (threshold $warn%)|current=$users";
      exit 1;
   }
   else {
      print "OK - Concurrent users at $users of $maxusers or $percent% " .
            "utilization|current=$users";
   }
}


################################################################################
# version - check version                                                      #
################################################################################
sub version {
   # retrieve platform
   my $platform = calculate_user_limit()->{platform};
  
   # retrieve version
   my $version = $snmp->snmpget( OID->{sysdescr} );

   # parse out the version information from returned string
   ($version) = $version =~ /(?:NVR|CES) (\S+)/;

   # check for upgrades
   my $upgrade = { #'9.1.1' => 'Bug #7213',
                 };

   if (exists $upgrade->{$version}) {
      print "WARNING - Nortel VPN version $version should be " .
            "upgraded: $upgrade->{$version}";
      exit 1;
   }
   else {
      print "OK - Nortel VPN $platform version $version";
   }
}


################################################################################
# calculate_user_limit - calculate maximum users based on hardware specs       #
################################################################################
sub calculate_user_limit {
   # instantiate variables
   my $processor = my $processors = ();
   my $hardware = [ { minmem     => 128,
                      maxmem     => 256,
                      platform   => 1700,
                      processor  => qr/850/,
                      processors => 1,
                      users      => 500,
                    },
                    { minmem     => 256,
                      maxmem     => 512,
                      platform   => 2700,
                      processor  => qr/1\.[23][36]/,
                      processors => 1,
                      users      => 2000,
                    },
                    { minmem     => 512,
                      maxmem     => 1536,
                      platform   => 4600,
                      processor  => qr/800/,
                      processors => 2,
                      users      => 5000,
                    },
                    { minmem     => 512,
                      maxmem     => 1536,
                      platform   => 5000,
                      processor  => qr/2\.2/,
                      processors => 2,
                      users      => 5000,
                    },
                  ];

   # retrieve memory kB and convert into MB
   my $memory_kb = $snmp->snmpget( OID->{hrmemorysize} );
   my $memory_mb = sprintf "%d", $memory_kb / 1024;

   # retrieve host-resource-mib device indexes
   my @devices = $snmp->snmpbulkwalk( OID->{hrdeviceindex} );
   
   # loop through each host-resource-mib device
   foreach my $index (@devices) {
      my $oid_type = sprintf "%s.%s", OID->{hrdevicetype}, $index;
      my $oid_desc = sprintf "%s.%s", OID->{hrdevicedesc}, $index;
      my ($type, $desc) = $snmp->snmpget( $oid_type, $oid_desc );
      $type eq '.1.3.6.1.2.1.25.3.1.3' or next;   # processor
      $processors++;
      $processor = $desc;
   }

   # attempt to determine which hardware platform we are 
   foreach my $hash (@$hardware) {
      my $minmem = sprintf "%d", $hash->{minmem} * .98;    # 2% deviation
      my $maxmem = sprintf "%d", $hash->{maxmem} * 1.02;   # 2% deviation
      $memory_mb >= $minmem and 
      $memory_mb <= $maxmem and 
      $processors == $hash->{processors} and 
      $processor =~ $hash->{processor} and 
      return $hash;
   }
     
   # no matching platform found 
   print "UNKNOWN - Unable to match a Nortel Contivity hardware model\n";
   print "memory     = $memory_mb MB\n";
   print "processors = $processors\n";
   print "processor  = $processor";
   exit 3;
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
