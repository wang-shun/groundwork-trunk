#!/usr/local/groundwork/perl/bin/perl -w

use strict;
use lib q(/usr/local/groundwork/nagios/libexec);
use cache_func;
use nagios_func;
use parse_func;
use snmp_func;

use constant PLUGIN => 'check_netscaler';

my %oid = ( 'clientconns' => '.1.3.6.1.4.1.5951.1.2.1.2.0',
            'cpuusage'    => '.1.3.6.1.4.1.5951.4.1.1.41.1.0',
            'failover'    => '.1.3.6.1.4.1.5951.1.2.2.6.0',
            'fostate'     => '.1.3.6.1.4.1.5951.4.1.1.6.0',
            'memusage'    => '.1.3.6.1.4.1.5951.4.1.1.41.2.0',
            'platform'    => '.1.3.6.1.4.1.5951.4.1.1.11.0',
            'serverconns' => '.1.3.6.1.4.1.5951.1.2.1.4.0',
            'version'     => '.1.3.6.1.2.1.1.1.0',
          );

my %functions = ( 'conns'        => \&connections,
                  'cpu'          => \&cpu_usage,
                  'failover'     => \&failover,
                  'int_list'     => \&interface,
                  'mem'          => \&memory_usage,
                  'uptime'       => \&snmp_uptime,
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
                   'h' => { 'desc'     => 'hostname of device',
                            'required' => 1,
                          },
                   'l' => { 'desc'     => 'warning/critical levels [warn:crit]',
                            'required' => 0,
                          },
                   'p' => { 'desc'     => 'process name',
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


sub connections {
   my ($clientconns, $serverconns) = $snmp->snmpget( $oid{clientconns}, $oid{serverconns} );
   my $PERFDATA = "cli_conns=$clientconns srv_conns=$serverconns";
   print "OK - Client/Server connection statistics collected|$PERFDATA\n";
   print "Client Connections: $clientconns\nServer Connections: $serverconns";
}


sub cpu_usage {
   my ($WARN, $CRIT) = parse_levels('cpu');
   my ($cpuusage) = $snmp->snmpget( $oid{cpuusage} );
   my $PERFDATA = "cpu_usage=$cpuusage";
   if ($cpuusage >= $CRIT) {
      print "CRITICAL - CPU utilization at $cpuusage% (threshold $CRIT%)" .
            "|$PERFDATA";
   }
   elsif ($cpuusage >= $WARN) {
      print "WARNING - CPU utilization at $cpuusage% (threshold $WARN%)" .
            "|$PERFDATA";
   }
   else {
      print "OK - CPU utilization at $cpuusage%|$PERFDATA";
   }
}


sub failover {
   my @states = qw/standalone primary secondary/;

   # returns 1 (enabled) or 2 (disabled)
   my ($failover) = $snmp->snmpget( $oid{failover} );   # 1=enabled 2=disabled
   unless ($failover == 1) {
      print "OK - Failover not enabled on this device.";
      return;
   }

   my ($fostate) = $snmp->snmpget( $oid{fostate} );
   my $cached_fostate = cache_get('fostate');
   cache_set('fostate', $fostate);

   # validate failover argument; valid is primary or secondary
   if (defined $cached_fostate && $fostate != $cached_fostate) {
      print "CRITICAL - Failover state changed from " .
            "$states[$cached_fostate] to $states[$fostate]";
      exit 2;
   }
   else {
      print "OK - Failover state is $states[$fostate]";
   }
   return;
} 


sub memory_usage {
   my ($WARN, $CRIT) = parse_levels('mem');
   my ($memusage) = $snmp->snmpget( $oid{memusage} );
   my $PERFDATA = "mem_usage=$memusage";
   if ($memusage >= $CRIT) {
      print "CRITCAL - Memory usage at $memusage% (threshold $CRIT%)|$PERFDATA";
      exit 2;
   }
   elsif ($memusage > $WARN) {
      print "WARNING - Memory usage at $memusage% (threshold $WARN%)|$PERFDATA";
      exit 1;
   }
   else {
      print "OK - Memory usage at $memusage%|$PERFDATA";
   }
}


sub interface {
   my $interface = $snmp->snmp_interface();
   my @int_list = ();
   foreach my $i (sort { $a <=> $b } keys %{$interface}) {
      my $int = $$interface{$i};
      $$int{int_name} =~ tr|/|-|;
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
   my ($version) = ($snmp->snmpget( $oid{version} ))[0] =~ /NetScaler NS(\S+):/o;
   my ($platform) = ($snmp->snmpget( $oid{platform} ))[0] =~ /^(\d+)/o;
   print "OK - Found NetScaler $platform version $version";
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

