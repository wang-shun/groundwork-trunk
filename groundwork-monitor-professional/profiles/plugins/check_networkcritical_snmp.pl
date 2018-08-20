#!/usr/local/groundwork/perl/bin/perl -w

use strict;
use lib q(/usr/local/groundwork/nagios/libexec);
use cache_func ();
use nagios_func ();
use parse_func ();
use snmp_func ();

use constant PLUGIN => 'check_networkcritical';

use constant OID => { 'sysdescr'      => '.1.3.6.1.2.1.1.1.0',
   
                      '1Gpsucount'    => '.1.3.6.1.4.1.31645.2.2.1.1.4.0',
                      '1Gpsuloc'      => '.1.3.6.1.4.1.31645.2.2.1.2',

                      '1Gslotstree'   => '.1.3.6.1.4.1.31645.2.2.1.2.5.1',
                      '1Gslotsindex'  => '.1.3.6.1.4.1.31645.2.2.1.2.5.1.1',
                      '1Gslotsloc'    => '.1.3.6.1.4.1.31645.2.2.1.2.5.1.2',
                      '1Gslotsinuse'  => '.1.3.6.1.4.1.31645.2.2.1.2.5.1.3',
                      '1Gslotstoohot' => '.1.3.6.1.4.1.31645.2.2.1.2.5.1.4',
                      '1Gslotstemp'   => '.1.3.6.1.4.1.31645.2.2.1.2.5.1.5',

                      '1Gpstree'      => '.1.3.6.1.4.1.31645.2.2.3.2.1.1',
                      '1Gpsindex'     => '.1.3.6.1.4.1.31645.2.2.3.2.1.1.1',
                      '1Gpsloc'       => '.1.3.6.1.4.1.31645.2.2.3.2.1.1.2',
                      '1Gpstype'      => '.1.3.6.1.4.1.31645.2.2.3.2.1.1.4',
                      '1Gpslink'      => '.1.3.6.1.4.1.31645.2.2.3.2.1.1.5',

                      '10GmonA'       => '.1.3.6.1.4.1.31645.2.1.8.0',
                      '10GmonB'       => '.1.3.6.1.4.1.31645.2.1.9.0',
                      '10GnetA'       => '.1.3.6.1.4.1.31645.2.1.10.0',
                      '10GnetB'       => '.1.3.6.1.4.1.31645.2.1.11.0',
                      '10Gactive'     => '.1.3.6.1.4.1.31645.2.5.2.0',
                      '10Gpassive'    => '.1.3.6.1.4.1.31645.2.5.3.0',
                    };

use constant FUNCTIONS => { linkstate => \&linkstate,
                            sensors   => \&sensors,
                            uptime    => \&uptime,
                            version   => \&version,
                          };

use constant OPTIONS => { 'c'  => 'Community string',
                          'h'  => 'Hostname',
                          'i'  => 'IP address',
                          'l?' => 'Levels [warning:critical]',
                          't'  => { 'Type of check' => FUNCTIONS },
                          'v'  => 'SNMP version [1]',
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
# linkstate - check link state of all interfaces                               #
################################################################################
sub linkstate {
   # instantiate variables
   my $hash = {};
   my @output = ();

   # retrieve cached interface status
   my $cache = cache_func->new( $args->{h} );
   my $cached = $cache->get( 'linkstate' );

   # determine what kind of device we are working with
   my $sysdescr = $snmp->snmpget( OID->{sysdescr} );

   # 1G tap chassis
   if ($sysdescr =~ /SmartNA SNAM[ ]+1U Filtering/) {
      # define interface link state
      my $states = { qw( 1 up 2 down ) };

      # snmpwalk entire interface tree and store in hash
      my $pstree = $snmp->snmpwalk( OID->{'1Gpstree'} );

      # grep out interface indexes
      my $psindexoid = OID->{'1Gpsindex'};
      my @indexes = map { /^$psindexoid\.\d+$/ ? $pstree->{$_} : () } keys %$pstree;

      # loop through each interface (by index) in ascending order
      foreach my $index (sort { $a <=> $b } @indexes) {
         my $loc  = $pstree->{ OID->{'1Gpsloc'} . ".$index" };
         my $type = $pstree->{ OID->{'1Gpstype'} . ".$index" };
         my $link = $pstree->{ OID->{'1Gpslink'} . ".$index" };
         my $previous = $cached->{ $index };
         $type < 4 and next;
         if (defined $previous && $previous != $link) {
            push @output, "CRITICAL - Interface $loc changed from " .
                          "$states->{ $previous } to $states->{ $link }";
         }
         else {
            push @output, "OK - Interface $loc is $states->{ $link }";
         }
         $hash->{ $index } = $link;
      }
   }
      
   # 10G tap
   if ($sysdescr =~ /SNA10GV/) {
      # define interface link state
      my $states = { qw( 1 down 2 up ) };

      # loop through each interface
      foreach my $int ( qw/10GmonA 10GmonB 10GnetA 10GnetB/ ) {
         my $previous = $cached->{ $int };
         my $current  = $snmp->snmpget( OID->{ $int } );
         if (defined $previous && $previous != $current) {
            push @output, "CRTIICAL - Interface $int changed from " .
                          "$states->{ $previous } to $states->{ $current }";
         }
         else {
            push @output, "OK - Interface $int is $states->{ $current }";
         }
         $hash->{ $int } = $current;
      }
   }

   # write current interface status out to cache
   $cache->set( 'linkstate', $hash );

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
      my $ok = @sorted;
      print "OK - $ok interfaces healthy\n";
      print join "\n" => @sorted;
   }
}


################################################################################
# sensors - check hardware sensors
################################################################################
sub sensors {
   # instantiate variables
   my @output = ();

   # determine what kind of device we are working with
   # only 1G chassis support retrieval of sensors
   my $sysdescr = $snmp->snmpget( OID->{sysdescr} );
   if ($sysdescr =~ /SNA10GV/) {
      # 10G doesn't support any sensors via SNMP
      # fan checks avaiable via ssh
      print "OK - Chassis sensors not supported on SNA10GV";
      return;
   }

   # power supply health
   my $psucount = $snmp->snmpget( OID->{'1Gpsucount'} );
   for my $i (1.. $psucount) { 
      my $psu = $snmp->snmpget( OID->{'1Gpsuloc'} . ".$i.0" );
      if ($psu == 1) {
         push @output, "OK - PSU $i is enabled";
      }
      else {
         push @output, "CRITICAL - PSU $i is disabled";
      }
   }

   # snmpwalk entire slots tree and store in hash
   my $slots = $snmp->snmpwalk( OID->{'1Gslotstree'} );
   
   # grep out slot indexes
   my $slotindexoid = OID->{'1Gslotsindex'};
   my @indexes = map { /^$slotindexoid\.\d+$/ ? $slots->{$_} : () } keys %$slots;

   # loop through each interface (by index) in ascending order
   foreach my $index (sort { $a <=> $b } @indexes) {
      my $loc    = $slots->{ OID->{'1Gslotsloc'} . ".$index" };
      my $inuse  = $slots->{ OID->{'1Gslotsinuse'} . ".$index" };
      my $toohot = $slots->{ OID->{'1Gslotstoohot'} . ".$index" };
      my $temp   = $slots->{ OID->{'1Gslotstemp'} . ".$index" };
      $inuse == 2 and next;   # slot not in use
      if ($toohot == 1) {
         push @output, "CRITICAL - $loc too hot ${temp}C";
      }
      else {
         push @output, "OK - $loc at ${temp}C";
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
      my $ok = @sorted;
      print "OK - $ok sensors healthy\n";
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
# version - retrieve platform                                                  #
################################################################################
sub version {
   my $sysdescr = $snmp->snmpget( OID->{sysdescr} );
   print "OK - $sysdescr";
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
