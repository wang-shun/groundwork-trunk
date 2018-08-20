#!/usr/local/groundwork/perl/bin/perl -w

use strict;
use lib q(/usr/local/groundwork/nagios/libexec);
use nagios_func ();
use parse_func ();
use snmp_func ();

use constant PLUGIN => 'check_aventail';

use constant OID => { 'version'       => '.1.3.6.1.4.1.4331.1.1.0',
                      'platform'      => '.1.3.6.1.4.1.4331.1.2.0',
	              'currentusers'  => '.1.3.6.1.4.1.4331.2.1.1.0',
	              'userlicenses'  => '.1.3.6.1.4.1.4331.2.1.3.0',
	              'currentconns'  => '.1.3.6.1.4.1.4331.2.2.1.0',
	              'serviceid'     => '.1.3.6.1.4.1.4331.3.1.1.1',
	              'servicedesc'   => '.1.3.6.1.4.1.4331.3.1.1.2',
	              'servicestate'  => '.1.3.6.1.4.1.4331.3.1.1.3',
	              'logindenials'  => '.1.3.6.1.4.1.4331.4.1.0',
                      'ngserverstate' => '.1.3.6.1.4.1.4331.5.1.0',
                    };

use constant FUNCTIONS => { 'clock'        => \&clock,
                            'conns'        => \&connections,
                            'cpu'          => \&cpu,
                            'disk'         => \&disk,
                            'int_list'     => \&interface,
		            'load'         => \&load,
                            'loginfail'    => \&login_failures,
                            'mem'          => \&memory,
		            'services'     => \&services,
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
# connections - retrieve concurrent connection counter                         #
# there are no documented limits on the Aventail website                       #
################################################################################
sub connections {
   # retrieve conccurrent connection count
   my $count = $snmp->snmpget( OID->{currentconns} );

   # generate output
   print "OK - Concurrent connections at $count|count=$count";
}


################################################################################
# cpu - checks cpu utilization using ucDvais MIB; no smp support               #
################################################################################
sub cpu {
   return $snmp->ucd_cpu( $args );
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
# login_failures - retrieve login failures counter                             #
################################################################################
sub login_failures {
   # retrieve login failures counter
   my $counter = $snmp->snmpget( OID->{logindenials} );

   # generate output
   print "OK - Captured $counter login failures|count=$counter";
}


################################################################################
# memory - retrieve linux memory utilization using ucDavis MIB                 #
################################################################################
sub memory {
   return $snmp->ucd_memory( $args );
}


################################################################################
# users - retrieve conncurrent users and test against licensed amount          #
################################################################################
sub users {
   # set thresholds
   my ($warn, $crit) = grep /^\d+$/ => split /:/ => $args->{l};
   $warn ||= 75;    # default 75%
   $crit ||= 90;    # default 90%

   # retrieve current user count and license limit
   my ($current, $licensed) = $snmp->snmpget( OID->{currentusers}, 
                                              OID->{userlicenses} );
  
   # test against thresholds and generate output 
   if ( $licensed == 0 ) {
      print "CRITICAL - No user licenses found; expired license?" .
            "|count=$current";
      exit 2;
   }
   elsif ( $current >= $licensed * $crit / 100 ) {
      print "CRITICAL - Concurrent users at $current of $licensed " .
            "(threshold $crit%)|count=$current";
      exit 2;
   }
   elsif ( $current >= $licensed * $warn / 100 ) {
      print "WARNING - Concurrent users at $current of $licensed " .
            "(threshold $warn%)|count=$current";
      exit 1; 
   }
   else {
      print "OK - Concurrent users at $current of $licensed|count=$current";
   }
}


################################################################################
# services - check aventail service health using Aventail MIB                  #
################################################################################
sub services {
   # instantiate variables
   my @output = ();
   my $states = { 1 => 'active',
                  2 => 'down',
		  6 => 'crashed',
		};

   # check service status
   my @serviceid = $snmp->snmpbulkwalk( OID->{serviceid} );
   foreach my $index (@serviceid) {
      my $sd = OID->{servicedesc} . ".$index";
      my $ss = OID->{servicestate} . ".$index";
      my ($servicedesc, $servicestate) = $snmp->snmpget( $sd, $ss );
      next if $servicedesc =~ /OBSOLETE/;
      if ($servicestate > 1) {
	 push @output, "CRITICAL - Service $servicedesc is " .
	               "$states->{$servicestate}";
      }
      else {
         push @output, "OK - Service $servicedesc is " .
	               "$states->{$servicestate}";
      }
   }

   # check ng server status
   chomp(my $ngserver = $snmp->snmpget( OID->{ngserverstate} ));
   if ($ngserver =~ /ACTIVE/) {
      push @output, "OK - NG Server state is $ngserver";
   }
   else {
      push @output, "CRITICAL - NG Server state is $ngserver";
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
      print "OK - $ok services healthy\n";
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
# version - check system platform and version using Aventail MIB               #
################################################################################
sub version {
   # retrieve version and platform
   my ($version, $platform) = $snmp->snmpget( OID->{version}, OID->{platform});

   # define upgrade array
   my $upgrade = { #'9.1.1' => 'Bug #7213',
                 };

   # generate output
   if (exists $upgrade->{$version}) {
      print "WARNING - SonicWALL Aventail code version $version should be " .
            "upgraded: $upgrade->{$version}";
      exit 1;
   }
   else {
      print "OK - SonicWALL Aventail $platform code version $version";
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
