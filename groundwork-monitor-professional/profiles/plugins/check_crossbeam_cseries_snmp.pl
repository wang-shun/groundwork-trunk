#!/usr/local/groundwork/perl/bin/perl -w

use strict;
use lib q(/usr/local/groundwork/nagios/libexec);
use snmp_func;
use parse_func;
use nagios_func;
use fw_func;

use constant PLUGIN => 'check_crossbeam_cseries';

my %oid = ( 'version'     => '.1.3.6.1.2.1.1.1.0',
            'cpuidle'     => '.1.3.6.1.4.1.2021.11.11',
            'fw1filter'   => '.1.3.6.1.4.1.2620.1.1.2.0',
            'fw1accepts'  => '.1.3.6.1.4.1.2620.1.1.4.0',
            'fw1rejects'  => '.1.3.6.1.4.1.2620.1.1.5.0',
            'fw1drops'    => '.1.3.6.1.4.1.2620.1.1.6.0',
            'fw1logs'     => '.1.3.6.1.4.1.2620.1.1.7.0',
            'fw1conns'    => '.1.3.6.1.4.1.2620.1.1.25.3.0',
            'vrrpd'       => '.1.3.6.1.4.1.4242.101',
          );

my %functions = ( 'cpu'          => \&cpu_usage,
                  'disk'         => \&snmp_disk,
                  'fw1-policy'   => \&fw1_policy,
                  'fw1-drops'    => \&fw1_drops,
                  'fw1-state'    => \&fw1_state,
                  'int_list'     => \&interface,
                  'load'         => \&ucd_load,
                  'mem'          => \&snmp_memory,
                  #'version'      => \&version,
                  'uptime'       => \&snmp_uptime,
                  'vrrp'         => \&vrrp,
                );

my %valid_args = ( 'i' => { 'desc'     => 'IP to snmp query',
                            'required' => 1,
                          },
                   'c' => { 'desc'     => 'community string',
                            'required' => 1,
                          },
                   't' => { 'desc'     => 'type of check',
                            'required' => 1,
                            'sub'      => \%functions,
                          },
                   'v' => { 'desc'     => 'snmp version [1 or 2c]',
                            'required' => 1,
                          },
                   'h' => { 'desc' => 'hostname of device',
                            'required' => 0,
                          },
                   'l' => { 'desc' => 'warning/critical levels [warn:crit]',
                            'required' => 0,
                          },
                 );

our %args = parse_args(@ARGV);
foreach my $arg (keys %valid_args) {
   if ($valid_args{$arg}{required} == 1 && ! defined $args{$arg}) {
      print STDERR "\n!!! Missing argument $valid_args{$arg}{desc}\n";
      usage();
   }
   if ($arg eq 't' && ! defined $functions{$args{t}}) {
      print STDERR "\n!!! Invalid type provided: $args{t}\n";
      usage();
   }
}

my $snmp = snmp_func->new( host => $args{i}, version => $args{v}, community => $args{c} );

$functions{$args{t}}() if defined $functions{$args{t}}; 
exit 0;


sub cpu_usage {
   my ($WARN, $CRIT) = parse_levels('cpu');

   my (@cpuidle) = $snmp->snmpwalk( $oid{cpuidle} );

   my $cpuusage = 0;
   $cpuusage += $_ foreach (@cpuidle);
   $cpuusage /= scalar @cpuidle;
   $cpuusage = 100 - $cpuusage;
   my $PERFDATA = "cpu_usage=$cpuusage";

   if ($cpuusage >= $CRIT) {
      print "CRITICAL - CPU usage at $cpuusage% (threshold $CRIT%)|$PERFDATA";
      exit 2;
   } 
   elsif ($cpuusage >= $WARN) {
      print "WARNING - CPU usage at $cpuusage% (threshold $WARN%)|$PERFDATA";
      exit 1;
   }
   else {
      print "OK - CPU usage at $cpuusage%|$PERFDATA";
   }
}


sub vrrp {
   snmp_max_msg_size(5000);
   my ($MASTER, $BACKUP) = (0, 0);
   my @OUTPUT = ();
   my (@vrrp) = $snmp->snmpwalk( $oid{vrrpd} );

   my $REGEX = qr/\d+\s+(\S+)\s+\d+\s+(\w+)\s+\w+\s+\d+\.\d+\.\d+\.\d+\s+\w+\s+(\d+)\s+(\d+)\s+(\d+)/;

   foreach my $line (@vrrp) {
      if ($line =~ /$REGEX/) {
         my $interface = $1;
         my $state = $2;
         my $configprio = $3;
         my $activeprio = $4;
         my $masterprio = $5;
         $MASTER++ if $state eq 'master';
         $BACKUP++ if $state eq 'backup';
        
         if ($configprio != $activeprio) {
            push @OUTPUT, "CRITICAL - VRRP is degraded on interface " .
                          "$interface [state=$state; c=$configprio; " .
                          "a=$activeprio]";
         }
	 else {
	    push @OUTPUT, "OK - VRRP is okay on interface $interface " .
	                  "[state=$state; c=$configprio; a=$activeprio]";
	 }
      }
   }

   my $PERFDATA = "master=$MASTER backup=$BACKUP";

   if (my $CRITICAL = scalar grep /CRITICAL/, @OUTPUT) {
      print "CRITICAL - $CRITICAL interfaces with degraded VRRP|$PERFDATA\n";
      print join "\n", @OUTPUT;
   }
   elsif ($MASTER == 0 && $BACKUP == 0) {
      print "OK - VRRP is not configured on this firewall|$PERFDATA";
   }
   else {
      print "OK - VRRP is functioning properly [master=$MASTER; " .
            "backup=$BACKUP]|$PERFDATA\n";
      print join "\n", @OUTPUT;
   }
}


sub fw1_policy {
   my ($policy) = $snmp->snmpget( $oid{fw1filter} );
   
   if ($policy eq '-') {
      print "CRITICAL - No FW-1 policy loaded ($policy)";
      exit 2;
   }
   elsif ($policy eq 'defaultfilter') {
      print "CRITICAL - FW-1 using default filter ($policy)";
      exit 2;
   }
   elsif ($policy eq 'InitialPolicy') {
      print "CRITICAL - FW-1 using initial policy ($policy)";
      exit 2;
   }
   else {
      print "OK - FW-1 using custom policy ($policy)";
   }
}


sub fw1_drops {
   my ($absaccepts, 
       $absdrops, 
       $absrejects, 
       $abslogs) = $snmp->snmpget( $oid{fw1accepts}, $oid{fw1drops}, $oid{fw1rejects}, $oid{fw1logs} );

   my @OUTPUT = checkpoint_fw1_drops($absaccepts, 
                                     $absdrops, 
                                     $absrejects, 
                                     $abslogs
				    );
   my $PERFDATA = shift @OUTPUT;

   if ( scalar (my @CRITICAL = grep /CRITICAL/, @OUTPUT) ) {
      print "@{[shift @CRITICAL]}|$PERFDATA\n";
      print join "\n", @CRITICAL, grep(!/CRITICAL/, @OUTPUT);
      exit 2;
   }
   elsif ( scalar (my @WARNING = grep /WARNING/, @OUTPUT ) ) {
      print "@{[shift @WARNING]}|$PERFDATA\n";
      print join "\n", @WARNING, grep(!/WARNING/, @OUTPUT);
      exit 1;
   }
   else {
      print "OK - @{[scalar @OUTPUT]} checks healthy|$PERFDATA\n";
      print join "\n", @OUTPUT;
   }
}


sub fw1_state {
   my ($WARN, $CRIT) = parse_levels();

   my ($conns) = $snmp->snmpget( $oid{fw1conns} );
   my $PERFDATA = "conns=$conns;";

   if ($conns >= $CRIT) {
      print "CRITICAL - Concurrent connections at $conns (threshold $CRIT)" .
            "|$PERFDATA";
      exit 2;
   } 
   elsif ($conns >= $WARN) {
      print "WARNING - Concurrent connections at $conns (threshold $WARN)" .
            "|$PERFDATA";
      exit 1;
   } 
   else {
      print "OK - Concurrent connections at $conns|$PERFDATA";
   }
}


sub interface {
   unless (defined $args{h}) {
      print STDERR "\n!!! Missing argument $valid_args{h}{desc}\n";
      usage();
   }

   my $interface = $snmp->snmp_interface();

   my @int_list = ();
   foreach my $i (sort { $a <=> $b } keys %{$interface}) {
      my $int = $$interface{$i};
      ($$int{int_name}) = $$int{int_name} =~ /^(\S+)/;
      next if $$int{int_name} eq 'lo';
      nagios_interface_status_passive($int);
      next unless ( ($$int{int_in_oct} && $$int{int_in_oct} ne 'U') || 
                    ($$int{int_out_oct} && $$int{int_out_oct} ne 'U') );
      push @int_list, "${i}:$$int{int_name}";
      nagios_interface_stats_passive($int);
      nagios_interface_problems_passive($int);
   }
   print "OK - Interfaces with traffic counters: @int_list";
}


sub version {
   my ($version) = $snmp->snmpget( $oid{version} );
   ($version) = $version =~ /IPSO \S+ (\S+) rel/;
   my %upgrade = ( #'9.1.1' => 'Bug #7213',
                 );

   if (exists $upgrade{$version}) {
      print "WARNING - Cisco FW code version $version should be upgraded: " .
            "$upgrade{$version}";
      exit 1;
   }
   else {
      print "OK - Nokia IPSO code version $version";
   }
}


sub usage {
   print STDERR '
   The following options are available:

';
   foreach my $key (keys %valid_args) {
      print STDERR "      -$key  --  $valid_args{$key}{desc}\n";
      if (exists $valid_args{$key}{sub}) {
         foreach my $subkey (sort keys %{$valid_args{$key}{sub}}) {
            print STDERR "              - $subkey\n";
         }
      }
   }
   
   print STDERR "\n";
   exit 255;
}


