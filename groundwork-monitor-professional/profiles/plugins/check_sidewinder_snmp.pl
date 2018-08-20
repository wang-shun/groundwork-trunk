#!/usr/local/groundwork/perl/bin/perl -w

use strict;
use lib q(/usr/local/groundwork/nagios/libexec);
use snmp_func;
use parse_func;
use nagios_func;

use constant PLUGIN => 'check_sidewinder';

my %oid = ( 
            'version'     => '.1.3.6.1.2.1.1.1.0'
          );

my %functions = ( 'cpu'          => \&ucd_cpu,
                  'disk'         => \&snmp_disk,
                  'int_list'     => \&interface,
                  'load'         => \&ucd_load,
                  'mem'          => \&ucd_memory,
                  'uptime'       => \&snmp_uptime,
                  'version'      => \&version,
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
                            'required' => 0,
                          },
                   'l' => { 'desc'     => 'warning/critical levels [warn:crit]',
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


sub interface {
   unless (defined $args{h}) {
      print STDERR "\n!!! Missing argument $valid_args{h}{desc}\n";
      usage();
   }

   my $interface = $snmp->snmp_interface();

   my @int_list = ();
   foreach my $i (sort { $a <=> $b } keys %{$interface}) {
      my $int = $$interface{$i};
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
   my ($version) = ($snmp->snmpget( $oid{version} ))[0] =~ /SecureOS \S+ ([0-9\.]+)/;
   print "OK - Found Sidewinder version $version";
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

