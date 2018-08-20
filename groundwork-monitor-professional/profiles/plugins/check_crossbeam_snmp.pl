#!/usr/local/groundwork/perl/bin/perl -w

use strict;
use lib q(/usr/local/groundwork/nagios/libexec);
use snmp_func;
use parse_func;

my %oid = ( 'vapgroupid'  => '.1.3.6.1.4.1.6848.2.4.1.1.1',
            'vapcount'    => '.1.3.6.1.4.1.6848.2.4.1.1.6',
            'vapname'     => '.1.3.6.1.4.1.6848.2.4.2.1.3',
            'vapslot'     => '.1.3.6.1.4.1.6848.2.4.2.1.6',
            'cpuutil1'    => '.1.3.6.1.4.1.6848.2.3.1.1.6',
            'cpuutil5'    => '.1.3.6.1.4.1.6848.2.3.1.1.7',
            'cpuutil15'   => '.1.3.6.1.4.1.6848.2.3.1.1.8',
            'cpuload1'    => '.1.3.6.1.4.1.6848.2.3.1.1.3',
            'cpuload5'    => '.1.3.6.1.4.1.6848.2.3.1.1.4',
            'cpuload15'   => '.1.3.6.1.4.1.6848.2.3.1.1.5',
            'memtotal'    => '.1.3.6.1.4.1.6848.2.3.2.1.1',
            'memused'     => '.1.3.6.1.4.1.6848.2.3.2.1.2',
            'status'      => '.1.3.6.1.4.1.6848.2.1.6.1.1',
            'cputemp'     => '.1.3.6.1.4.1.6848.2.1.6.1.2',
            'intemp'      => '.1.3.6.1.4.1.6848.2.1.6.1.4',
            'intempalrm'  => '.1.3.6.1.4.1.6848.2.1.6.1.5',
            'extemp'      => '.1.3.6.1.4.1.6848.2.1.6.1.6',
            'extempalrm'  => '.1.3.6.1.4.1.6848.2.1.6.1.7',
            'activeled'   => '.1.3.6.1.4.1.6848.2.1.6.1.14',
            'standbyled'  => '.1.3.6.1.4.1.6848.2.1.6.1.15',
            'failedled'   => '.1.3.6.1.4.1.6848.2.1.6.1.16',
            'chassistemp' => '.1.3.6.1.4.1.6848.2.1.2.3',
            'upfantray'   => '.1.3.6.1.4.1.6848.2.1.2.4',
            'lofantray'   => '.1.3.6.1.4.1.6848.2.1.2.5',
            'systemalarm' => '.1.3.6.1.4.1.6848.2.1.2.6',
            'upfanstatus' => '.1.3.6.1.4.1.6848.2.1.3.1.3.2',
            'lofanstatus' => '.1.3.6.1.4.1.6848.2.1.3.1.3.1',
          );

my %functions = ( 'apm_cpu'    => { 'type'  => \&apm,
                                    'check' => \&blade_cpu_usage,
                                  },
                  'apm_mem'    => { 'type'  => \&apm,
                                    'check' => \&blade_mem_usage, # deprecated
                                  },
                  'apm_load'   => { 'type'  => \&apm,
                                    'check' => \&blade_load_avg,
                                  },
                  'apm_health' => { 'type'  => \&apm,
                                    'check' => \&blade_health,
                                  },
                  'cpm_cpu'    => { 'type'  => \&cpm,
                                    'check' => \&blade_cpu_usage,
                                  },
                  'cpm_mem'    => { 'type'  => \&cpm,
                                    'check' => \&blade_mem_usage, # deprecated
                                  },
                  'cpm_load'   => { 'type'  => \&cpm,
                                    'check' => \&blade_load_avg,
                                  },
                  'cpm_health' => { 'type'  => \&cpm,
                                    'check' => \&blade_health,
                                  },
                  'npm_cpu'    => { 'type'  => \&npm,
                                    'check' => \&blade_cpu_usage,
                                  },
                  'npm_mem'    => { 'type'  => \&npm,
                                    'check' => \&blade_mem_usage, # deprecated
                                  },
                  'npm_load'   => { 'type'  => \&npm,
                                    'check' => \&blade_load_avg,
                                  },
                  'npm_health' => { 'type'  => \&npm,
                                    'check' => \&blade_health,
                                  },
                  'chassis'    => { 'type'  => \&cpm,
                                    'check' => \&chassis_health,
                                  },
                );

my %valid_args = ( 'i' => { 'desc'     => 'ip address of cpm to snmp query',
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
                   'n' => { 'desc'     => 'name of blade (i.e. Core_FW-1)',
                            'required' => 1,
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

$functions{$args{t}}{type}() if defined $functions{$args{t}}{type}; 
exit 0;


sub apm {
   my (@getapms) = $snmp->snmpwalk( $oid{vapname} );
   unless ("@getapms" =~ /$args{n}/) {
      print "CRITICAL - Unable to find blade with the name $args{n}, " .
            "was it removed?\n";
      exit 2;
   }

   my $vapgroups = $snmp->snmpwalk( $oid{vapgroupid} );
   VAPGROUPS: foreach (my $i=1; $i<=$vapgroups; $i++) {
      my ($vapcount) = $snmp->snmpget( "$oid{vapcount}.$i" );
      for (my $j=1; $j<=$vapcount; $j++) {
         my ($vapname) = $snmp->snmpget( "$oid{vapname}.$i.$j" );
         my ($vapslot) = $snmp->snmpget( "$oid{vapslot}.$i.$j" );
         if ($vapname =~ /$args{n}/ && defined $functions{$args{t}}{check}) {
            $functions{$args{t}}{check}($vapslot);
            last VAPGROUPS;
         }
      }
   }
   return;
}


sub cpm {
   $functions{$args{t}}{check}($args{n}) if defined $functions{$args{t}}{check};
   return;
}


sub npm {
   $functions{$args{t}}{check}($args{n}) if defined $functions{$args{t}}{check};
   return;
}


sub blade_cpu_usage {
   my $slotnum = shift;

   my ($WARN, $CRIT) = parse_levels('cpu');

   #my ($cpuutil1) = $snmp->snmpget( "$oid{cpuutil1}.$slotnum" );
   my ($cpuutil5) = $snmp->snmpget( "$oid{cpuutil5}.$slotnum" );
   #my ($cpuutil15) = $snmp->snmpget( "$oid{cpuutil15}.$slotnum" );

   my $PERFDATA = "cpu_usage=$cpuutil5";

   if ($cpuutil5 >= $CRIT) {
      print "CRITICAL - CPU usage at $cpuutil5% (threshold $CRIT%)|$PERFDATA";
      exit 2;
   } 
   elsif ($cpuutil5 >= $WARN) {
      print "WARNING - CPU usage at $cpuutil5% (threshold $WARN%)|$PERFDATA";
      exit 1;
   }
   else {
      print "OK - CPU usage at $cpuutil5%|$PERFDATA";
   }

   return;
}


sub blade_mem_usage {
   my $slotnum = shift;

   my ($WARN, $CRIT) = parse_levels('mem');

   my ($memtotal) = $snmp->snmpget( "$oid{memtotal}.$slotnum" );
   my ($memused) = $snmp->snmpget( "$oid{memused}.$slotnum" );

   my $memusedpercent = sprintf("%d", 100*$memused/$memtotal);

   my $PERFDATA = "mem_usage=$memusedpercent";

   if ($memusedpercent >= $CRIT) {
      print "CRITICAL - Memory usage at $memusedpercent% (threshold $CRIT%)" .
            "|$PERFDATA";
      exit 2;
   } 
   elsif ($memusedpercent >= $WARN) {
      print "WARNING - Memory usage at $memusedpercent% (threshold $WARN%)" .
            "|$PERFDATA";
      exit 1;
   }
   else {
      print "OK - Memory usage at $memusedpercent%|$PERFDATA";
   }

   return;
}


sub blade_load_avg {
   my $slotnum = shift;

   my ($MAXLOAD1MIN, $MAXLOAD5MIN, $MAXLOAD15MIN) = parse_levels('load');

   my ($cpuload1) = $snmp->snmpget( "$oid{cpuload1}.$slotnum" );
   my ($cpuload5) = $snmp->snmpget( "$oid{cpuload5}.$slotnum" );
   my ($cpuload15) = $snmp->snmpget( "$oid{cpuload15}.$slotnum" );

   my $loadavg1  = sprintf("%.2f",$cpuload1/100);
   my $loadavg5  = sprintf("%.2f",$cpuload5/100);
   my $loadavg15 = sprintf("%.2f",$cpuload15/100);

   my $LOADAVG = "$loadavg1 $loadavg5 $loadavg15";
   my $PERFDATA = "load1min=$cpuload1 load5min=$cpuload5 load15min=$cpuload15";

   if ($loadavg1 >= $MAXLOAD1MIN) {
      print "WARNING - 1 MIN load ($loadavg1) is abnormally high " .
            "(threshold $MAXLOAD1MIN)|$PERFDATA";
      exit 1;
   } elsif ($loadavg5 >= $MAXLOAD5MIN) {
      print "WARNING - 5 MIN load ($loadavg15) is abnormally high " .
            "(threshold $MAXLOAD5MIN)|$PERFDATA";
      exit 1;
   } elsif ($loadavg15 >= $MAXLOAD15MIN) {
      print "WARNING - 15 MIN load ($loadavg15) is abnormally high " .
            "(threshold $MAXLOAD15MIN)|$PERFDATA";
      exit 1;
   } else {
      print "OK - Load average $LOADAVG|$PERFDATA";
   }

   return;
}


sub blade_health {
   my $slotnum = shift;
   my (@OUTPUT) = ();

   my ($CPUTEMPWARN, $CPUTEMPCRIT) = parse_levels('cputemp');

   my %statusval = ( 1 => 'unavailable',
                     2 => 'down',
                     3 => 'initializing',
                     4 => 'up',
                     5 => 'standby',
                     6 => 'bootwait',
                     7 => 'offline',
                     8 => 'maintenance',
                   );

   my ($status) = $snmp->snmpget( "$oid{status}.$slotnum" );
   if ($status != 4 && $args{n} != 13) {
      push @OUTPUT, "CRITICAL - Status reporting as $statusval{$status}";
   } 
   else {
      push @OUTPUT, "OK - Status reporting as $statusval{$status}";
   }
   
   my ($cputemp) = $snmp->snmpget( "$oid{cputemp}.$slotnum" );
   if ($cputemp >= $CPUTEMPCRIT) {
      push @OUTPUT, "CRITICAL - CPU temperature at ${cputemp}C " .
                    "(threshold ${CPUTEMPCRIT}C)";
   } 
   elsif ($cputemp >= $CPUTEMPWARN) {
      push @OUTPUT, "WARNING - CPU temperature at ${cputemp}C " .
                    "(threshold ${CPUTEMPWARN}C)";
   } 
   else {
      push @OUTPUT, "OK - CPU temperature ${cputemp}C";
   }

   my ($intemp, $intempalrm) = $snmp->snmpget( "$oid{intemp}.$slotnum", "$oid{intempalrm}.$slotnum" );
   if ($intempalrm > 0) {
      push @OUTPUT, "CRITICAL - Intake temperature alarm at ${intemp}C";
   } 
   else {
      push @OUTPUT, "OK - Intake temperature ${intemp}C";
   }

   my ($extemp, $extempalrm) = $snmp->snmpget( "$oid{extemp}.$slotnum", "$oid{extempalrm}.$slotnum" );
   if ($extempalrm > 0) {
      push @OUTPUT, "CRITICAL - Exhaust temperature alarm at ${extemp}C";
   }
   else {
      push @OUTPUT, "OK - Exhaust temperature ${extemp}C";
   }

   my ($activeled) = $snmp->snmpget( "$oid{activeled}.$slotnum" );
   if ($activeled > 1) {
      my ($standbyled, $failedled) = $snmp->snmpget( "$oid{standbyled}.$slotnum", "$oid{failedled}.$slotnum" );
      if ($standbyled == 1 && $args{n} != 13) {
         push @OUTPUT, "WARNING - Standby LED illuminated";
      } 
      elsif ($failedled == 1) {
         push @OUTPUT, "CRITICAL - Failed LED illuminated";
      }
      else {
         push @OUTPUT, "OK - No alarm LED illuminated";
      }
   }

   my $PERFDATA = "cputemp=$cputemp intemp=$intemp extemp=$extemp";

   if (my $CRITICAL = scalar grep /CRITICAL/, @OUTPUT) {
      print "CRITICAL - $CRITICAL blade checks returned critical status" .
            "|$PERFDATA\n";
      print join "\n", @OUTPUT;
      exit 2;
   } 
   elsif (my $WARNING = scalar grep /WARNING/, @OUTPUT) {
      print "WARNING - $WARNING blade checks returned warning status" .
            "|$PERFDATA\n";
      print join "\n", @OUTPUT;
      exit 1;
   } 
   else {
      my $OK = scalar @OUTPUT;
      print "OK - $OK blade checks okay|$PERFDATA\n";
      print join "\n", @OUTPUT;
   }

   return;
}


sub chassis_health {
   my $slotnum = shift; # not used here
   my @OUTPUT = ();

   my %statusval = ( 1 => 'up',
                     2 => 'down',
                     3 => 'not-present',
                   );

   my %alarmval = ( 1 => 'none',
                    2 => 'minor',
                    3 => 'major',
                    4 => 'critical',
                  );

   my ($systemalarm) = $snmp->snmpget( "$oid{systemalarm}.0" );
   if ($systemalarm > 2) { 
      push @OUTPUT, "CRITICAL - System alarm found state = " .
                    $alarmval{$systemalarm};
   } 
   elsif ($systemalarm > 1) {
      push @OUTPUT, "WARNING - System alarm found state = " .
                    $alarmval{$systemalarm};
   } 
   else {
      push @OUTPUT, "OK - No system alarms found";
   }

   my ($TEMPWARN, $TEMPCRIT) = parse_levels('systemp');

   my ($chassistemp) = $snmp->snmpget( "$oid{chassistemp}.0" );
   if ($chassistemp >= $TEMPCRIT) {
      push @OUTPUT, "CRITICAL - Chassis temperature at ${chassistemp}C " .
                    "(threshold ${TEMPCRIT}C)";
   } 
   elsif ($chassistemp >= $TEMPWARN) {
      push @OUTPUT, "WARNING - Chassis temperature at ${chassistemp}C " .
                    "(threshold ${TEMPWARN}C)";
   } 
   else {
      push @OUTPUT, "OK - Chassis temperature at ${chassistemp}C";
   }
   
   my ($upperfantray) = $snmp->snmpget( "$oid{upfantray}.0" );
   if ($upperfantray == 1) {
      my (@upperfans) = $snmp->snmpwalk( $oid{upfanstatus} );
      for (my $i=0; $i<scalar @upperfans; $i++) {
         push @OUTPUT, "CRITICAL - Upper fan #$i is $statusval{$upperfans[$i]}"             if ($upperfans[$i] > 1);
      }
   }

   my ($lowerfantray) = $snmp->snmpget( "$oid{lofantray}.0" );
   if ($lowerfantray == 1) {
      my (@lowerfans) = $snmp->snmpwalk( $oid{lofanstatus} );
      for (my $i=1; $i<scalar @lowerfans; $i++) {
         push @OUTPUT, "CRITICAL - Lower fan #$i is $statusval{$lowerfans[$i]}"
            if ($lowerfans[$i] > 1);
      }
   }
   
   my $PERFDATA = "chassistemp=$chassistemp";

   if (my $CRITICAL = scalar grep /CRITICAL/, @OUTPUT) {
      print "CRITICAL - $CRITICAL chassis checks returned critical status" .
            "|$PERFDATA\n";
      print join "\n", @OUTPUT;
      exit 2;
   } elsif (my $WARNING = scalar grep /WARNING/, @OUTPUT) {
      print "WARNING - $WARNING chassis checks returned warning status" .
            "|$PERFDATA\n";
      print join "\n", @OUTPUT;
      exit 1;
   } else {
      my $OK = scalar @OUTPUT;
      print "OK - $OK chassis checks okay|$PERFDATA\n";
      print join "\n", @OUTPUT;
   }

   return;
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

