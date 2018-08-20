#!/usr/local/groundwork/perl/bin/perl -w

use strict;
use Net::SNMP ();

use lib q(/usr/local/groundwork/nagios/libexec);
use cache_func ();
use nagios_func ();
use parse_func ();
use snmp_func ();

use constant PLUGIN => 'check_cisco_css';

use constant ARROWPOINT => '.1.3.6.1.4.1.2467';
use constant CISCO      => '.1.3.6.1.4.1.9.9.368';

use constant OID => { 'platform'        => '1.34.2.0',
                      'version'         => '1.34.9.0',
                      'mod_slot'        => '1.34.16.1.2',
                      'mod_type'        => '1.34.16.1.3',
                      'mod_status'      => '1.34.16.1.6',
                      'submod_slot'     => '1.34.17.1.2',
                      'submod_type'     => '1.34.17.1.3',
                      'submod_status'   => '1.34.17.1.5',
                      'submod_freemem'  => '1.34.17.1.10',
                      'submod_totalmem' => '1.34.17.1.12',
                      'submod_cpuavg'   => '1.34.17.1.14',
                      'psu_text'        => '1.34.24.0',
                      'mod_text'        => '1.34.25.0',
                    };

use constant FUNCTIONS => { 'cpu'      => \&cpu,
                            'health'   => \&health,
                            'int_list' => \&interface,
                            'mem'      => \&memory,
                            'uptime'   => \&uptime,
                            'version'  => \&version,
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
# cpu - checks cpu utilization of each submodule                               #
################################################################################
sub cpu {
   # instantiate variables
   my @output = my @perfdata = ();
   my $submod_types = [ qw/scm-submodule sfm-submodule scfm-submodule
                           t1-submodule hssi-submodule epif-submodule
                           v35-submodule xpif-submodule sfm2-submodule
                           scfm2-submodule genic-2port-submodule
                           genic-1port-submodule hdfem-submodule
                           unknown-submodule mallomar-submodule
                           spritz-submodule fc-submodule fortune-submodule
                           g2iom-submodule session-submodule nilla-submodule
                           ssl2-submodule/ ];

   # set thresholds
   my ($warn, $crit) = grep /^\d+$/ => split /:/ => $args->{l};
   $warn ||= 85;    # default 85%
   $crit ||= 95;    # default 95%

   # retrieve module and submodule information
   my $slots = get_mod_submod();
 
   # capture submodule cpu utilization  
   foreach my $slot (sort keys %$slots) {
      foreach my $subslot (sort keys %{ $slots->{$slot}->{submodules} }) {
         my $submod = $slots->{$slot}->{submodules}->{$subslot};
         if ($submod->{cpuinstant} >= $crit) {
            push @output, "CRITICAL - $submod_types->[ $submod->{type} ] " .
                          "in slot $slot/$subslot cpu usage at " .
                          "$submod->{cpuinstant}% (threshold $crit%)";
         }
         elsif ($submod->{cpuinstant} >= $warn) {
            push @output, "WARNING - $submod_types->[ $submod->{type} ] " .
                          "in slot $slot/$subslot cpu usage at " .
                          "$submod->{cpuinstant}% (threshold $warn%)";
         }
         else {
            push @output, "OK - $submod_types->[ $submod->{type} ] in slot " .
                          "$slot/$subslot cpu usage at $submod->{cpuinstant}%";
         }
         push @perfdata, "$submod_types->[ $submod->{type} ]_$slot-$subslot=" .
                         "$submod->{cpuinstant}";
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
      print "OK - $ok cpu checks healthy|@perfdata\n";
      print join "\n" => @sorted;
   }
}


################################################################################
# health - checks health status of each module and submodule                   #
################################################################################
sub health {
   # instantiate variables
   my @output = ();
   my $mod_types = [ qw/scm-1g sfm scfm fem-t1 dual-hssi fem fenic genic gem 
                        hdfem unknown iom scm fc ssl/ ];
   my $submod_types = [ qw/scm-submodule sfm-submodule scfm-submodule
                           t1-submodule hssi-submodule epif-submodule
                           v35-submodule xpif-submodule sfm2-submodule
                           scfm2-submodule genic-2port-submodule
                           genic-1port-submodule hdfem-submodule
                           unknown-submodule mallomar-submodule
                           spritz-submodule fc-submodule fortune-submodule
                           g2iom-submodule session-submodule nilla-submodule
                           ssl2-submodule/ ];
   my $mod_status = [ qw/powered-off powered-on primary backup bad unknown/ ];
   my $submod_status = [ qw/offline-ok offline-bad online bad going-online
                            going-offline inserted post post-ok post-fail
                            post-bad-comm flash-upgrade flash-upgrade-cmplt
                            any unknown-state/ ];

   # retrieve module and submodule information
   my $slots = get_mod_submod();

   # get/set cached module and submodule information
   my $cache = cache_func->new( $args->{h} );
   my $cached_slots = $cache->get( 'slots' );
   $cache->set( 'slots', $slots );

   # check for state changes in module/submodule status
   foreach my $slot (sort keys %$slots) {
      my $n_module = $slots->{$slot}; 
      my $c_module = $cached_slots->{$slot} or next;
      if ($c_module->{status} != $n_module->{status}) {
         push @output, "CRITICAL - $mod_types->[ $n_module->{type} ] module " .
                       "in slot $slot changed state from " .
                       "$mod_status->[ $c_module->{status} ] to " .
                       "$mod_status->[ $n_module->{status} ]";
      }
      else {
         push @output, "OK - $mod_types->[ $n_module->{type} ] module in " .
                       "slot $slot is $mod_status->[ $n_module->{status} ]";
      }
                       
      foreach my $subslot (sort keys %{ $slots->{$slot}->{submodules} }) {
         my $n_submodule = $slots->{$slot}->{submodules}->{$subslot};
         my $c_submodule = $cached_slots->{$slot}->{submodules}->{$subslot} 
            or next;
         if ($c_submodule->{status} != $n_submodule->{status}) {
            push @output, "CRITICAL - $submod_types->[ $n_submodule->{type} ] ".
                          "in slot $slot/$subslot changed state " .
                          "from $submod_status->[ $c_submodule->{status} ] " .
                          "to $submod_status->[ $n_submodule->{status} ]";
         }
         else {
            push @output, "OK - $submod_types->[ $n_submodule->{type} ] " .
                          "in slot $slot/$subslot is " .
                          "$submod_status->[ $n_submodule->{status} ]";
         }
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
      my $ok = scalar @sorted;
      print "OK - $ok sensors healthy\n";
      print join "\n" => @sorted;
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
      $int->{int_name} =~ s/^ //;
      $int->{int_name} =~ tr|/|-|;
      $args->{l} and $args->{l} =~ /no_in_drops/ and $int->{no_in_drops} = 1;
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
# memory - retrieve linux memory utilization using ucDavis MIB                 #
################################################################################
sub memory {
# instantiate variables
   my @output = my @perfdata = ();
   my $submod_types = [ qw/scm-submodule sfm-submodule scfm-submodule
                        t1-submodule hssi-submodule epif-submodule
                        v35-submodule xpif-submodule sfm2-submodule
                        scfm2-submodule genic-2port-submodule
                        genic-1port-submodule hdfem-submodule
                        unknown-submodule mallomar-submodule
                        spritz-submodule fc-submodule fortune-submodule
                        g2iom-submodule session-submodule nilla-submodule
                        ssl2-submodule/ ];

   # set thresholds
   my ($warn, $crit) = grep /^\d+$/ => split /:/ => $args->{l};
   $warn ||= 80;    # default 80%
   $crit ||= 90;    # default 90%

   # retrieve module and submodule information
   my $slots = get_mod_submod();

   # capture submodule memory utilization
   foreach my $slot (sort keys %$slots) {
      foreach my $subslot (sort keys %{ $slots->{$slot}->{submodules} }) {
         my $submod = $slots->{$slot}->{submodules}->{$subslot};
         my $free = $submod->{heapfree};
         my $total = $submod->{installedmemory} or next;
         my $used = sprintf "%d", 100 * ($total-$free) / $total;
         if ($used >= $crit) {
            push @output, "CRITICAL - $submod_types->[ $submod->{type} ] " .
                          "submodule memory utilization at $used% " .
                          "(threshold $crit%)";
         }
         elsif ($used >= $warn) {
            push @output, "WARNING - $submod_types->[ $submod->{type} ] " .
                          "submodule memory utilization at $used% " .
                          "(threshold $warn%)";
         }
         else {
            push @output, "OK - $submod_types->[ $submod->{type} ] submodule " .
                          "memory utilization at $used%";
         }
         push @perfdata, "$submod_types->[ $submod->{type} ]_$slot-$subslot=" .
                         "$used";
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
      print "OK - $ok memory checks healthy|@perfdata\n|";
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
# version - check system platform and version
################################################################################
sub version {
   # instantiate variables
   my $platforms = [ qw/ws100 ws800 ws150 ws50 unknown css11503 css11506 
                        css11501/ ];

   # retrieve oid prefix
   my $oid_prefix = get_oid_prefix();

   # build temporary oids
   my $oid_platform = "$oid_prefix." . OID->{platform};
   my $oid_version  = "$oid_prefix." . OID->{version};

   # retrieve oids
   my ($platform, $version) = $snmp->snmpget( $oid_platform, $oid_version );

   # define upgrade array
   my $upgrade = { #'9.1.1' => 'Bug #7213',
                 };

   # generate output
   if (exists $upgrade->{$version}) {
      print "WARNING - Cisco $platforms->[$platform] code version $version " .
            "should be upgraded: $upgrade->{$version}";
      exit 1;
   }
   else {
      print "OK - Cisco $platforms->[$platform] code version $version";
   }
}


################################################################################
# get_oid_prefix - checks for ARROWPOINT or CISCO snmp oid prefix              #
################################################################################
sub get_oid_prefix {
   # disable snmp error checking
   $snmp->{ec} = 0;

   # define OIDS to check 
   my $arrowpoint = "@{[ARROWPOINT]}.@{[ OID->{platform} ]}";
   my $cisco = "@{[CISCO]}.@{[ OID->{platform} ]}";

   # determine which OID responds
   my $found = do {
      if ($snmp->snmpget( $arrowpoint )) {
         ARROWPOINT;
      }
      elsif ($snmp->snmpget( $cisco )) {
         CISCO;
      }
   };

   # enable snmp error checking
   $snmp->{ec} = 1;

   # return prefix OID found
   return $found;
}


################################################################################
# get_mod_submod - retrieves modules and submodules in stores in a hash        #
################################################################################
sub get_mod_submod {
   # instantiate variables 
   my $slot = {};
   my $oid_prefix = get_oid_prefix();
   my $mod_prefix = "$oid_prefix.1.34.16.1";
   my $submod_prefix = "$oid_prefix.1.34.17.1";
   my $mod_oids     = [qw/undef undef slot type name serial status subcount
                          hwmajor hwminor subtype/];
   my $submod_oids  = [qw/undef slot subslot type name status sscardtype
                          sscardstatus portname portnum heapfree
                          heapchaindepth installedmemory cpuinstant
                          cpuaverage curspweight cursppower cpubusy1min
                          cpubusy5min/];

   # retrieve modules
   my $modules    = $snmp->snmpbulkwalk( $mod_prefix );

   # loop through all modules
   foreach my $oid (Net::SNMP::oid_lex_sort( keys %$modules )) {
      if ($oid =~ /^$mod_prefix\.([0-9]+)\.([0-9]+)$/) {
         $slot->{$2}->{ $mod_oids->[$1] } = $modules->{ $oid };
      }
   }   

   # retrieve submodules
   my $submodules = $snmp->snmpbulkwalk( $submod_prefix );

   # loop through all submodule
   foreach my $oid (Net::SNMP::oid_lex_sort( keys %$submodules )) {
      if ($oid =~ /^$submod_prefix\.([0-9]+)\.([0-9]+)\.([0-9]+)$/) {
         $slot->{$2}->{submodules}->{$3}->{ $submod_oids->[ $1 ] } = 
            $submodules->{ $oid };
      }
   }

   # return hash to calling function
   return $slot;
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
