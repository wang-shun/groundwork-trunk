#!/usr/local/groundwork/perl/bin/perl -w

use strict;
use lib q(/usr/local/groundwork/nagios/libexec);
use cache_func ();
use nagios_func ();
use parse_func ();
use snmp_func ();

use constant PLUGIN => 'check_cisco_fw';

use constant OID => { 'blocks'      => '.1.3.6.1.4.1.9.9.147.1.2.2.1.1.4',
                      'cpuusage'    => '.1.3.6.1.4.1.9.9.109.1.1.1.1.5.1',
                      'connections' => '.1.3.6.1.4.1.9.9.147.1.2.2.2.1.5.40.6',
                      'fo_pri_info' => '.1.3.6.1.4.1.9.9.147.1.2.1.1.1.2.6',
                      'fo_sec_info' => '.1.3.6.1.4.1.9.9.147.1.2.1.1.1.2.7',
                      'fo_pri_stat' => '.1.3.6.1.4.1.9.9.147.1.2.1.1.1.3.6',
                      'fo_sec_stat' => '.1.3.6.1.4.1.9.9.147.1.2.1.1.1.3.7',
                      'freemem'     => '.1.3.6.1.4.1.9.9.48.1.1.1.6.1',
	              'platform'    => '.1.3.6.1.2.1.47.1.1.1.1.13.1',
                      'usedmem'     => '.1.3.6.1.4.1.9.9.48.1.1.1.5.1',
                      'version'     => '.1.3.6.1.2.1.1.1.0',
          
                      'rasmaxsessions' => '.1.3.6.1.4.1.9.9.392.1.1.1.0',
                      'rasmaxusers'    => '.1.3.6.1.4.1.9.9.392.1.1.2.0',
                      'rasmaxgroups'   => '.1.3.6.1.4.1.9.9.392.1.1.3.0',
                      'rascursessions' => '.1.3.6.1.4.1.9.9.392.1.3.1.0',
                      'rascurusers'    => '.1.3.6.1.4.1.9.9.392.1.3.3.0',
                      'rascurgroups'   => '.1.3.6.1.4.1.9.9.392.1.3.4.0',
                    };

use constant FUNCTIONS => { 'blocks'       => \&blocks,
                            'cpu'          => \&cpu,
                            'conns'        => \&connections,
                            'failover'     => \&failover,
                            'int_list'     => \&interface,
                            'mem'          => \&memory,
                            'uptime'       => \&uptime,
                            'version'      => \&version,
                            'vpnras'       => \&vpn_ras,
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
# blocks - collect blocks utilization in percentage                            #
################################################################################
sub blocks {
   # instantiate variables
   my @output = my @perfdata = ();
   my $blocksize = [ qw/0 4 80 256 1550 2048 2560 4096 8192 16384 65536/ ];

   # set warning/critical thresholds
   my ($warn, $crit) = grep /^\d+$/ => split /:/ => $args->{l};
   $warn ||= 80;    # default 80%
   $crit ||= 90;    # default 90%

   # retrieve all blocks values
   my $blocks = $snmp->snmpbulkwalk( OID->{blocks} );

   # loop through all blocksizes to see if exists
   my $blocksoid = OID->{blocks};
   foreach my $bs (@$blocksize) {
      if ( grep(/^$blocksoid\.$bs\.[358]$/, keys %$blocks) == 3 ) {
         my $max  = $blocks->{ "$blocksoid.$bs.3" };
         my $low  = $blocks->{ "$blocksoid.$bs.5" };
         my $free = $blocks->{ "$blocksoid.$bs.8" };
         my $used = sprintf( "%d", ($max - $free) / $max * 100 );
         if ($used >= $crit) {
            push @output, "CRITICAL - $bs byte blocks at $used% " .
                          "(threshold $crit%)";
         }
         elsif ($used >= $warn) {
            push @output, "WARNING - $bs byte blocks at $used% " .
                          "(threshold $warn%)";
         }
         else {
            push @output, "OK - $bs byte blocks at $used%";
         }
         push @perfdata, "$bs=$used"; 
      }
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
      print "OK - $ok blocks healthy|@perfdata\n";
      print join "\n" => @sorted;
   }
}


################################################################################
# cpu - check cpu utilization in percentage                                    #
################################################################################
sub cpu {
   # set warning/critical thresholds
   my ($warn, $crit) = grep /^\d+$/ => split /:/ => $args->{l};
   $warn ||= 85;    # default 85%
   $crit ||= 95;    # default 95%

   # retrieve cpu utilization percentage
   my $cpu = $snmp->snmpget( OID->{cpuusage} );

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
# connections - retrieve concurrent connection count                           #
################################################################################
sub connections {
   # set warning/critical thresholds
   my ($warn, $crit) = grep /^\d+$/ => split /:/ => $args->{l};
   $warn ||= 80;    # default 80%
   $crit ||= 90;    # default 90%

   # retrieve concurrent connection count
   my $conns = $snmp->snmpget( OID->{connections} );

   # test against thresholds
   if ( my $connlimit = get_platform_conn_limit() ) {
      my $usage = sprintf "%d", $conns / $connlimit * 100;
      if ($usage >= $crit) {
         print "CRITICAL - Concurrent connections at $conns of $connlimit " .
               "or $usage% utilized (threshold $crit%)|current=$conns";
         exit 2;
      }
      elsif ($usage >= $warn) {
         print "WARNING - Concurrent connections at $conns of $connlimit " .
               "or $usage% utilized (threshold $warn%)|current=$conns";
         exit 1;
      }
      else {
         print "OK - Concurrent connections at $conns of $connlimit or " .
               "$usage% utilized|current=$conns";
      }
   }
   else {
      print "OK - Concurrent connections at $conns|current=$conns";
   }
}


################################################################################
# failover - check for device failover state changes                           #
################################################################################
sub failover {
   # instantiate variables
   my $states = [ qw/not_configured other up down error overTemp busy noMedia 
                     backup active standby/ ];

   # retrieve failover states for primary and standby
   my ($pri, $sec) = $snmp->snmpget( OID->{fo_pri_stat}, OID->{fo_sec_stat} );

   # retrieve/set failover states
   my $cache = cache_func->new( $args->{h} );
   my $cached_pri = $cache->get( 'primary' );
   my $cached_sec = $cache->get( 'secondary' );
   $cache->set( 'primary', $pri );
   $cache->set( 'secondary', $sec );
 
   # test for state changes 
   my $pri_changed = defined $cached_pri ? $pri <=> $cached_pri : 0;
   my $sec_changed = defined $cached_sec ? $sec <=> $cached_sec : 0;

   # generate output
   if ($pri_changed && $sec_changed) {
      print "CRITICAL - Primary [$states->[$cached_pri] -\> $states->[$pri]] " .
            "Secondary [$states->[$cached_sec] -\> $states->[$sec]]";
      exit 2;
   }
   elsif ($pri_changed) {
      print "CRITICAL - Primary [$states->[$cached_pri] -\> $states->[$pri]] " .
            "Secondary [$states->[$sec]]";
      exit 2;
   }
   elsif ($sec_changed) {
      print "CRITICAL - Primary [$states->[$pri]] Secondary " .
            "[$states->[$cached_sec] -\> $states->[$sec]]";
      exit 2;
   }
   else {
      print "OK - Primary [$states->[$pri]] Secondary [$states->[$sec]]";
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
      $int->{int_name} =~ s/Cisco PIX Security Appliance '(\S+)' interface/$1/;
      $int->{int_name} =~ s/PIX Firewall '(\S+)' interface/$1/;
      $int->{int_name} =~ s/FWSM Firewall '(\S+)' interface/$1/;
      $int->{int_name} =~ s/Adaptive Security Appliance '(\S+)' interface/$1/;
      $int->{int_name} =~ s/Firewall Services Module '(\S+)' interface/$1/;
      $int->{int_name} =~ tr|/|-|;
      $int->{int_name} =~ tr/&//d;
      $int->{no_in_drops} = 1;
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
# memory - check memory utilization in percentage                              #
################################################################################
sub memory {
   # set warning/critical thresholds
   my ($warn, $crit) = grep /^\d+$/ => split /:/ => $args->{l};
   $warn ||= 80;    # default 80%
   $crit ||= 90;    # default 90%

   # retrieve free and used memory values
   my ($free, $used) = $snmp->snmpget( OID->{freemem}, OID->{usedmem} );

   # calculate percentage used
   my $total = $free + $used;
   my $mem = sprintf "%d", $used / $total * 100;

   # test thresholds and generate output 
   if ($mem >= $crit) {
      print "CRITICAL - Memory usage at $mem% (threshold $crit%)|percent=$mem";
      exit 2;
   }
   elsif ($mem >= $warn) {
      print "WARNING - Memory usage at $mem% (threshold $warn%)|percent=$mem";
      exit 1;
   }
   else {
      print "OK - Memory usage at $mem%|percent=$mem";
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
   # retrieve code version
   my $version = $snmp->snmpget( OID->{version} );
   ($version) = $version =~ /Version (\S+)$/;

   # retrieve platform if it exists
   $snmp->{ec} = 0;
   my $platform = $snmp->snmpget( OID->{platform} ) || 'UNKNOWN';

   # define upgrade array
   my $upgrade = { #'9.1.1' => 'Bug #7213',
                 };

   # generate output
   if (exists $upgrade->{$version}) {
      print "WARNING - Cisco $platform version $version should be " .
            "upgraded: $upgrade->{$version}";
      exit 1;
   }
   else {
      print "OK - Cisco $platform version $version";
   }
}


################################################################################
# vpn_ras - check RAS sessions/groups/users                                    #
################################################################################
sub vpn_ras {
   # instantiate variables
   my @output = my @perfdata = ();
   my @order = qw(sessions users groups);

   # set warning/critical thresholds
   my ($warn, $crit) = grep /^\d+$/ => split /:/ => $args->{l};
   $warn ||= 80;    # default 80%
   $crit ||= 90;    # default 90%

   # retrieve ras maximums
   my @max = $snmp->snmpget( OID->{rasmaxsessions}, OID->{rasmaxusers},
                             OID->{rasmaxgroups} );

   # retrieve ras current values
   my @ras = $snmp->snmpget( OID->{rascursessions}, OID->{rascurusers}, 
                             OID->{rascurgroups} );

   # compare sessions / users / groups
   foreach my $i (0 .. $#order) {
      my $name = $order[$i];
      push @perfdata, "$name=$ras[$i]";
      if ($max[$i] && defined $ras[$i]) {
         my $t_crit = sprintf "%d", $max[$i] * $crit / 100;
         my $t_warn = sprintf "%d", $max[$i] * $warn / 100;
         my $t_per  = sprintf "%d", $ras[$i] / $max[$i] * 100;
         if ($t_per >= $t_crit) {
            push @output, "CRITICAL - Current $name at $t_per% " .
                          "utilization [$ras[$i]/$max[$i]] (threshold $crit%)";
         }
         elsif ($t_per >= $t_warn) {
            push @output, "WARNING - Current $name at $t_per% " .
                          "utilization [$ras[$i]/$max[$i]] (threshold $warn%)";
         }
         else {
            push @output, "OK - Current $name at $t_per% utilization " .
                          "[$ras[$i]/$max[$i]]";
         }
      }
      else {
         push @output, "OK - Current $name at $ras[$i]";
      } 
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
      print "OK - $ok blocks healthy|@perfdata\n|";
      print join "\n" => @sorted;
   }
}


################################################################################
# get_platform_conn_limit - retrieve maximum concurrent connection limits      #
################################################################################
sub get_platform_conn_limit {
   my $connlimit = { 'PIX-501'      => 7500,
                     'PIX-506E'     => 25000,
                     'PIX-515E'     => 130000,
		     'PIX-525'      => 280000,
		     'PIX-535'      => 500000,
		     'WS-SVC-FWM-1' => 1000000,
		     'ASA5505'      => 10000,
		     'ASA5510-K8'   => 50000,
		     'ASA5510'      => 130000,
		     'ASA5520'      => 280000,
		     'ASA5540'      => 400000,
		     'ASA5550'      => 650000,
		     'ASA5580-20'   => 1000000,
		     'ASA5580-40'   => 2000000,
                   };

   $snmp->{ec} = 0;
   my $platform = $snmp->snmpget( OID->{platform} ) or return undef;
   $connlimit->{ $platform };
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
