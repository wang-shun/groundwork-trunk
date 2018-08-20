#!/usr/local/groundwork/perl/bin/perl -w

use strict;
use lib q(/usr/local/groundwork/nagios/libexec);
use cache_func ();
use nagios_func ();
use parse_func ();
use snmp_func ();
use ssl_func ();

use constant PLUGIN => 'check_f5';

use constant OID => { 'v4version'       => '.1.3.6.1.4.1.3375.1.1.1.1.2.0',
                      'v4productcode'   => '.1.3.6.1.4.1.3375.1.1.1.1.5.0',
                      'v4maintmode'     => '.1.3.6.1.4.1.3375.1.1.1.1.11.0',
                      'v4master'        => '.1.3.6.1.4.1.3375.1.1.1.1.12.0',
                      'v4fastflow'      => '.1.3.6.1.4.1.3375.1.1.1.1.16.0',
                      'v4uptime'        => '.1.3.6.1.4.1.3375.1.1.1.2.1.0',
                      'v4conns'         => '.1.3.6.1.4.1.3375.1.1.1.2.12.0',
                      'v4poolmemtotal'  => '.1.3.6.1.4.1.3375.1.1.1.2.14.0',
                      'v4poolmemused'   => '.1.3.6.1.4.1.3375.1.1.1.2.15.0',
                      'v4ooconns'       => '.1.3.6.1.4.1.3375.1.1.1.2.26.0',
                      'v4cpuindex'      => '.1.3.6.1.4.1.3375.1.7.2.2.1.1',
                      'v4cputemp'       => '.1.3.6.1.4.1.3375.1.7.2.2.1.2',
                      'v4cpufan'        => '.1.3.6.1.4.1.3375.1.7.2.2.1.3',
                      'v4chassisfan'    => '.1.3.6.1.4.1.3375.1.7.3.2.1.2',
                      'v4psulocation'   => '.1.3.6.1.4.1.3375.1.7.3.4.1.1',
                      'v4psustatus'     => '.1.3.6.1.4.1.3375.1.7.3.4.1.2',
                      'v4swapmemtotal'  => '.1.3.6.1.4.1.2021.4.3.0',
                      'v4realmemtotal'  => '.1.3.6.1.4.1.2021.4.5.0',
                      'v4vmemfree'      => '.1.3.6.1.4.1.2021.4.11.0',
                      'v4diskpath'      => '.1.3.6.1.4.1.2021.9.1.2',
                      'v4diskdev'       => '.1.3.6.1.4.1.2021.9.1.3',
                      'v4diskusage'     => '.1.3.6.1.4.1.2021.9.1.9',
                      'v4load1min'      => '.1.3.6.1.4.1.2021.10.1.3.1',
                      'v4load5min'      => '.1.3.6.1.4.1.2021.10.1.3.2',
                      'v4load15min'     => '.1.3.6.1.4.1.2021.10.1.3.3',
                        
                      'ucdcpuidle'      => '.1.3.6.1.4.1.2021.11.11.0',
          
                      'v9version'       => '.1.3.6.1.4.1.3375.2.1.4.2.0',
                      'v9syncstate'     => '.1.3.6.1.4.1.3375.2.1.1.1.1.6.0',
                      'v9failunitmask'  => '.1.3.6.1.4.1.3375.2.1.1.1.1.19.0',
                      'v9maintmode'     => '.1.3.6.1.4.1.3375.2.1.1.1.1.21.0',
                      'v9pvaaccel'      => '.1.3.6.1.4.1.3375.2.1.1.1.1.27.0',
                      'v9cliconns'      => '.1.3.6.1.4.1.3375.2.1.1.2.1.7.0',
                      'v9srvconns'      => '.1.3.6.1.4.1.3375.2.1.1.2.1.14.0',
                      'v9maintmodedeny' => '.1.3.6.1.4.1.3375.2.1.1.2.1.32.0',
                      'v9virtconndeny'  => '.1.3.6.1.4.1.3375.2.1.1.2.1.33.0',
                      'v9licensedeny'   => '.1.3.6.1.4.1.3375.2.1.1.2.1.36.0',
                      'v9ooconns'       => '.1.3.6.1.4.1.3375.2.1.1.2.1.37.0',
                      'v9droppedpkt'    => '.1.3.6.1.4.1.3375.2.1.1.2.1.46.0',
                      'v9inpkterr'      => '.1.3.6.1.4.1.3375.2.1.1.2.1.47.0',
                      'v9outpkterr'     => '.1.3.6.1.4.1.3375.2.1.1.2.1.48.0',
                      'v9httprequest'   => '.1.3.6.1.4.1.3375.2.1.1.2.1.56.0',
                      'v9cachehits'     => '.1.3.6.1.4.1.3375.2.1.1.2.4.46.0',
                      'v9cachemisses'   => '.1.3.6.1.4.1.3375.2.1.1.2.4.47.0',
                      'v9cpuindex'      => '.1.3.6.1.4.1.3375.2.1.3.1.2.1.1',
                      'v9cputemp'       => '.1.3.6.1.4.1.3375.2.1.3.1.2.1.2',
                      'v9cpufan'        => '.1.3.6.1.4.1.3375.2.1.3.1.2.1.3',
                      'v9sysfanindex'   => '.1.3.6.1.4.1.3375.2.1.3.2.1.2.1.1',
                      'v9sysfanstatus'  => '.1.3.6.1.4.1.3375.2.1.3.2.1.2.1.2',
                      'v9sysfanspeed'   => '.1.3.6.1.4.1.3375.2.1.3.2.1.2.1.3',
                      'v9psuindex'      => '.1.3.6.1.4.1.3375.2.1.3.2.2.2.1.1',
                      'v9psustatus'     => '.1.3.6.1.4.1.3375.2.1.3.2.2.2.1.2',
                      'v9systempindex'  => '.1.3.6.1.4.1.3375.2.1.3.2.3.2.1.1',
                      'v9systemp'       => '.1.3.6.1.4.1.3375.2.1.3.2.3.2.1.2',
                      'v9sysmemtotal'   => '.1.3.6.1.4.1.3375.2.1.7.1.1.0',
                      'v9sysmemused'    => '.1.3.6.1.4.1.3375.2.1.7.1.2.0',
                      'v9tmtotalcycles' => '.1.3.6.1.4.1.3375.2.1.1.2.1.41.0',
                      'v9tmidlecycles'  => '.1.3.6.1.4.1.3375.2.1.1.2.1.42.0',
                      'v9tmsleepcycles' => '.1.3.6.1.4.1.3375.2.1.1.2.1.43.0',
                      'v9tmmemtotal'    => '.1.3.6.1.4.1.3375.2.1.1.2.1.44.0',
                      'v9tmmemused'     => '.1.3.6.1.4.1.3375.2.1.1.2.1.45.0',
                      'v9cpu2index'     => '.1.3.6.1.4.1.3375.2.1.7.2.2.1.1',
                      'v9cpu2user'      => '.1.3.6.1.4.1.3375.2.1.7.2.2.1.3',
                      'v9cpu2nice'      => '.1.3.6.1.4.1.3375.2.1.7.2.2.1.4',
                      'v9cpu2system'    => '.1.3.6.1.4.1.3375.2.1.7.2.2.1.5',
                      'v9cpu2idle'      => '.1.3.6.1.4.1.3375.2.1.7.2.2.1.6',
                      'v9cpu2irq'       => '.1.3.6.1.4.1.3375.2.1.7.2.2.1.7',
                      'v9cpu2softirq'   => '.1.3.6.1.4.1.3375.2.1.7.2.2.1.8',
                      'v9cpu2iowait'    => '.1.3.6.1.4.1.3375.2.1.7.2.2.1.9',
                      'v9diskpartname'  => '.1.3.6.1.4.1.3375.2.1.7.3.2.1.1',
                      'v9diskparttotal' => '.1.3.6.1.4.1.3375.2.1.7.3.2.1.3',
                      'v9diskpartfree'  => '.1.3.6.1.4.1.3375.2.1.7.3.2.1.4',
                      'v9uptime'        => '.1.3.6.1.4.1.3375.2.1.6.6.0',
                        
                      'v9multihost'     => '.1.3.6.1.4.1.3375.2.1.7.4.2.1.1.1',
                      'v9multicpucount' => '.1.3.6.1.4.1.3375.2.1.7.5.1.0',
                      'v9smpcpuusage5m' => '.1.3.6.1.4.1.3375.2.1.7.5.2.1.35.1',
          
                      'ipaddress'       => '.1.3.6.1.2.1.4.20.1.1',


                      'v9gtmipip'         => '.1.3.6.1.4.1.3375.2.3.4.1.2.1.2',
                      'v9gtmipservername' => '.1.3.6.1.4.1.3375.2.3.4.1.2.1.4',
                      'v9gtmservername'   => '.1.3.6.1.4.1.3375.2.3.9.1.2.1.1',
                      'v9gtmservertype'   => '.1.3.6.1.4.1.3375.2.3.9.1.2.1.3',
                      'v9gtmserverstate'  => '.1.3.6.1.4.1.3375.2.3.9.3.2.1.2',
                      'v9gtmserverenable' => '.1.3.6.1.4.1.3375.2.3.9.3.2.1.3',
                      'v9gtmserverreason' => '.1.3.6.1.4.1.3375.2.3.9.3.2.1.5',
          
                      'v9gtmpeers'      => '.1.3.6.1.4.1.3375.2.3.9.1.1.0',
	              'v9gtmpeername'   => '.1.3.6.1.4.1.3375.2.3.9.1.2.1.1',
	              'v9gtmpeertype'   => '.1.3.6.1.4.1.3375.2.3.9.1.2.1.3',
	              'v9gtmpeerenable' => '.1.3.6.1.4.1.3375.2.3.9.1.2.1.4',
	              'v9gtmpeerstate'  => '.1.3.6.1.4.1.3375.2.3.9.3.2.1.2',
         
                      'v9ltmvsname'     => '.1.3.6.1.4.1.3375.2.2.10.13.2.1.1',
                      'v9ltmvsstate'    => '.1.3.6.1.4.1.3375.2.2.10.13.2.1.2',
                      'v9ltmvsenabled'  => '.1.3.6.1.4.1.3375.2.2.10.13.2.1.3',
                      'v9ltmvsreason'   => '.1.3.6.1.4.1.3375.2.2.10.13.2.1.5',

                      'v9vsstate'       => '.1.3.6.1.4.1.3375.2.2.10.13.2.1.2',
                      'v9vsreason'      => '.1.3.6.1.4.1.3375.2.2.10.13.2.1.5',
                      'v9vstype'        => '.1.3.6.1.4.1.3375.2.2.10.1.2.1.15',
                      'v9vsdefpool'     => '.1.3.6.1.4.1.3375.2.2.10.1.2.1.19',
          
                      'v9oldvsstate'    => '.1.3.6.1.4.1.3375.2.2.10.1.2.1.22',
                      'v9oldvsreason'   => '.1.3.6.1.4.1.3375.2.2.10.1.2.1.25',
          
                      'v9poolenabled'   => '.1.3.6.1.4.1.3375.2.2.5.5.2.1.3',
                      'v9poolstate'     => '.1.3.6.1.4.1.3375.2.2.5.5.2.1.2',
                      'v9poolreason'    => '.1.3.6.1.4.1.3375.2.2.5.5.2.1.5',
                      'v9poolmemaddr'   => '.1.3.6.1.4.1.3375.2.2.5.3.2.1.3',
                      'v9poolmemport'   => '.1.3.6.1.4.1.3375.2.2.5.3.2.1.4',
                      'v9poolmemstate'  => '.1.3.6.1.4.1.3375.2.2.5.6.2.1.5',
                      'v9poolmemenabled'=> '.1.3.6.1.4.1.3375.2.2.5.6.2.1.6',
                      'v9poolmemreason' => '.1.3.6.1.4.1.3375.2.2.5.6.2.1.8',
          
                      'v9oldpoolenabled'    => '.1.3.6.1.4.1.3375.2.2.5.1.2.1.19',
                      'v9oldpoolstate'      => '.1.3.6.1.4.1.3375.2.2.5.1.2.1.18',
                      'v9oldpoolreason'     => '.1.3.6.1.4.1.3375.2.2.5.1.2.1.21',
                      'v9oldpoolmemaddr'    => '.1.3.6.1.4.1.3375.2.2.5.3.2.1.3',
                      'v9oldpoolmemport'    => '.1.3.6.1.4.1.3375.2.2.5.3.2.1.4',
                      'v9oldpoolmemenabled' => '.1.3.6.1.4.1.3375.2.2.5.3.2.1.16',
                      'v9oldpoolmemstate'   => '.1.3.6.1.4.1.3375.2.2.5.3.2.1.15',
                      'v9oldpoolmemreason'  => '.1.3.6.1.4.1.3375.2.2.5.3.2.1.18',
          
                      'v9nodeenabled'   => '.1.3.6.1.4.1.3375.2.2.4.3.2.1.4',
                      'v9nodestate'     => '.1.3.6.1.4.1.3375.2.2.4.3.2.1.3',
                      'v9nodereason'    => '.1.3.6.1.4.1.3375.2.2.4.3.2.1.6',
          
                      'v9oldnodeenabled' => '.1.3.6.1.4.1.3375.2.2.4.1.2.1.14',
                      'v9oldnodestate'   => '.1.3.6.1.4.1.3375.2.2.4.1.2.1.13',
                      'v9oldnodereason'  => '.1.3.6.1.4.1.3375.2.2.4.1.2.1.16',
          
                      'ucdswaptotal' => '.1.3.6.1.4.1.2021.4.3.0',
                      'ucdswapavail' => '.1.3.6.1.4.1.2021.4.4.0',
                      'ucdmemtotal'  => '.1.3.6.1.4.1.2021.4.5.0',
                      'ucdmemavail'  => '.1.3.6.1.4.1.2021.4.6.0',
                      'ucdmembuffer' => '.1.3.6.1.4.1.2021.4.14.0',
                      'ucdmemcached' => '.1.3.6.1.4.1.2021.4.15.0',
          
                      'v9cursslusers' => '.1.3.6.1.4.1.3375.2.1.1.2.9.2.0',
                    };

use constant FUNCTIONS => { 'conns'         => \&connections,
                            'cpu'           => \&cpu,
                            'disk'          => \&disk,
                            'failover'      => \&failover,
                            'gtmpeers'      => \&gtm_peers,
                            'int_list'      => \&interface,
                            'mem'           => \&memory,
                            'sensors'       => \&sensors,
                            'sslusers'      => \&sslusers,
                            'uptime'        => \&uptime,
                            'version'       => \&version,
                            'virtualserver' => \&virtual_server,
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
                           retries   => 2,
                           callback  => \&callback_check_snmp,
                         );
my $f5   = f5->new( $args, $snmp );
defined(FUNCTIONS->{ $args->{t} }) ? FUNCTIONS->{ $args->{t} }() :
   $snmp->die3("Unknown check type: $args->{t}");
exit 0;


################################################################################
# connections - retrieve connections and check for out-of-connections          #
################################################################################
sub connections {
   $f5->connections;
}


################################################################################
# cpu - check cpu utilization                                                  #
################################################################################
sub cpu {
   $f5->cpu;
}


################################################################################
# disk - check disk utilization                                                #
################################################################################
sub disk {
   $f5->disk;
}


################################################################################
# failover - check for device failover                                         #
################################################################################
sub failover {
   $f5->failover;
}


################################################################################
# gtm_peers - check gtm and peers                                              #
################################################################################
sub gtm_peers {
   $f5->gtm_peers;
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
      grep { $int->{int_type} == $_ } (6, 135) or next;
      $int->{int_name} =~ tr/~/-/;
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
# memory - check memory utilization                                            #
################################################################################
sub memory {
   $f5->memory;
}


################################################################################
# sensors - check hardware sensors                                             #
################################################################################
sub sensors {
   $f5->sensors;
}


################################################################################
# sslusers - count number of concurrent ssl users                              #
################################################################################
sub sslusers {
   $f5->sslusers;
}


################################################################################
# uptime - retrieve system uptime                                              #
################################################################################
sub uptime {
   $f5->uptime;
}


################################################################################
# version - check software version                                             #
################################################################################
sub version {
   $f5->version;
}


################################################################################
# virtual_server - check virtual server status                                 #
################################################################################
sub virtual_server {
   $f5->virtual_server;
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




package f5;


sub new {
   my $class = shift;
   my $args  = shift;
   my $snmp  = shift;
   my $self  = { args => $args, snmp => $snmp };

   # retrieve version from f5
   my $version = $snmp->snmpget_or( main::OID->{v4version}, 
                                    main::OID->{v9version} );

   # parse version string on legacy BIG-IP devices
   $version =~ s/BIG-IP Version (\S+).*/$1/; 

   # define versions
   ($self->{majorversion}) = $version =~ /^([0-9]+)/;
   ($self->{minorversion}) = $version =~ /^([0-9]+\.[0-9]+)/;
   $self->{fullversion} = $version;

   # check whether we are a gtm
   $self->{gtm} = do {
      $snmp->{ec} = 0;
      my $gtm = $snmp->snmpget( main::OID->{v9gtmpeers} );
      $snmp->{ec} = 1;
      $gtm ? 1 : 0;
   };

   # bless object
   bless($self, $class);

   # return object
   return $self;
}


sub connections {
   # instantiate variables
   my $self = shift;
   my $args = $self->{args};
   my $snmp = $self->{snmp};

   # retrieve cached out-of-connections count
   my $cache  = cache_func->new( $args->{h} );
   my $cached = $cache->get( 'ooconns' );

   # version 4
   if ( $self->{majorversion} == 4 ) {    # version 4
      # retreive connections and out-of-connections counters
      my ($conns, $ooconns) = $snmp->snmpget( main::OID->{v4conns}, 
                                              main::OID->{v4ooconns} );

      # write to the cache
      $cache->set( 'ooconns', $ooconns );

      # generate output
      if (defined $cached && $ooconns > $cached) {
         print "CRITICAL - Out-of-connections incrementing " .
               "[$cached -> $ooconns]|current=$conns";
         exit 2;
      }
      else {
         print "OK - Connections at $conns|current=$conns";
      }
   }

   # all other versions
   else { 
      # retrieve client, server and out-of-connections counters
      my ($client, $server, $ooconns) = $snmp->snmpget( main::OID->{v9cliconns},
                                                        main::OID->{v9srvconns},
                                                        main::OID->{v9ooconns},
                                                      );

      # write to the cache
      $cache->set( 'ooconns', $ooconns );
 
      # generate output
      if (defined $cached && $ooconns > $cached) {
         print "CRITICAL - Out-of-connections incrementing " .
               "[$cached -> $ooconns]|client=$client server=$server";
         exit 2;
      }
      else {
         print "OK - client=$client server=$server" .
               "|client=$client server=$server";
      }
   }
}
 

sub cpu {
   # instantiate variables
   my $self = shift;
   my $args = $self->{args};
   my $snmp = $self->{snmp};

   # set warning/critical thresholds
   my ($warn, $crit) = grep /^\d+$/ => split /:/ => $args->{l};
   $warn ||= 85;    # default 85%
   $crit ||= 95;    # default 95%

   # version 4 uses the ucDavis MIB
   if ( $self->{majorversion} == 4 ) {
      $snmp->ucd_cpu( $args );
   }

   # for all other versions we are only interested in tmm cpu
   else {
      # disable error checking as sleep may not be implemented on all versions
      $snmp->{ec} = 0;
  
      # retrieve first round of cpu statistics
      my ($total1, $idle1, $sleep1) = $snmp->snmpget( 
         main::OID->{v9tmtotalcycles},
         main::OID->{v9tmidlecycles},
         main::OID->{v9tmsleepcycles},
      );

      # sleep for 2 seconds to allow stats to update
      sleep 2;

      # retrieve second round of cpu statistics
      my ($total2, $idle2, $sleep2) = $snmp->snmpget( 
         main::OID->{v9tmtotalcycles},
         main::OID->{v9tmidlecycles},
         main::OID->{v9tmsleepcycles},
      );

      # test outputs to verify valid data is returned
      $total1 and $idle1 and $total2 and $idle2 or do {
         print "CRITICAL - Failed to retrieve tmm cpu statistics";
         exit 2;
      };

      # set sleep to zero if not > 0
      $sleep1 ||= 0;
      $sleep2 ||= 0;


      # calculate ticks
      my $total = $total2 - $total1;
      my $idle  = $idle2 - $idle1;
      my $sleep = $sleep2 - $sleep1;

      # calculate cpu usage in percentage
      my $cpu = sprintf "%d", ($total - ($idle + $sleep)) / $total * 100;

      # test against thresholds and generate output
      if ($cpu >= $crit) {
         print "CRITICAL - TMM cpu utilization at $cpu% (threshold $crit%)" .
               "|tmm=$cpu";
         exit 2;
      }
      elsif ($cpu >= $warn) {
         print "WARNING - TMM cpu utilization at $cpu% (threshold $warn%)" .
               "|tmm=$cpu";
         exit 1;
      }
      else {
         print "OK - TMM cpu utilization at $cpu%|tmm=$cpu";
      }
   } 
} 


sub disk {
   # instantiate variables
   my $self = shift;
   my $args = $self->{args};
   my $snmp = $self->{snmp};

   # set warning/critical thresholds
   my ($warn, $crit) = grep /^\d+$/ => split /:/ => $args->{l};
   $warn ||= 80;    # default 80%
   $crit ||= 90;    # default 90%

   # versions 4.0 <=> 9.1 will use ucDavis MIB
   if ( $self->{minorversion} >= 4 && $self->{minorversion} <= 9.1 ) {
      $snmp->ucd_disk( $args );
   }
  
   # all other versions
   else {
      # instantiate local variables
      my @output = my @perfdata = ();

      # retrieve partition information
      my @partname    = $snmp->snmpbulkwalk( main::OID->{v9diskpartname} );
      my @blockstotal = $snmp->snmpbulkwalk( main::OID->{v9diskparttotal} );
      my @blocksfree  = $snmp->snmpbulkwalk( main::OID->{v9diskpartfree} );
  
      foreach my $i (0.. $#partname) {
         # temporary variables for clarity
         my $name  = $partname[$i];
         my $total = $blockstotal[$i];
         my $free  = $blocksfree[$i];

         # calculate percentage used
         my $percent = sprintf "%d", ($total - $free) / $total * 100;

         # test against thresholds and generate output
         if ($percent >= $crit) {
            push @output, "CRITICAL - Partition '$name' at $percent% " .
                          "utilization (threshold $crit%)";
         }
         elsif ($percent >= $warn) {
            push @output, "WARNING - Partition '$name' at $percent% " .
                          "utilization (threshold $warn%)";
         }
         else {
            push @output, "OK - Partition '$name' at $percent% utilization";
         } 

         # populate perfdata
         $name =~ tr|^/||d;
         $name =~ tr|/|_|;
         $name ||= 'root';
         push @perfdata, "$name=$percent";
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
         print "OK - $ok partitions healthy|@perfdata\n";
         print join "\n" => @sorted;
      }
   }
}


sub failover {
   # instantiate variables
   my $self = shift;
   my $args = $self->{args};
   my $snmp = $self->{snmp};

   # get cached state
   my $cache  = cache_func->new( $args->{h} );
   my $cached = $cache->get( 'failover' );

   # version 4
   if ( $self->{majorversion} == 4 ) {
      # instantiate local variables
      my $states = { 1 => 'master',   # true
                     2 => 'slave',    # false
                     3 => 'unsupported',
                   };

      # retrieve whether unit is in master state
      my $master = $snmp->snmpget( main::OID->{v4master} );

      # write retrieved state out to cache
      $cache->set( 'failover', $master );

      # check for state change and generate output
      if (defined $cached && $cached != $master) {
         print "CRITICAL - Failover state changed from $states->{ $cached } " .
               "to $states->{ $master }";
         exit 2;
      }
      else {
         print "OK - Failover state is $states->{ $master }";
      }
   }

   # all other versions
   else {
      # instantiate local variables
      my $states = { 0 => 'active-passive standby unit',
                     1 => 'active-active unit 1',
                     2 => 'active-active unit 2',
                     3 => 'active-passive active unit',
                   };

      # retrieve the current unit failover mask
      my $mask = $snmp->snmpget( main::OID->{v9failunitmask} );
    
      # write retrieved mask out to cache
      $cache->set( 'failover', $mask );

      # check for state change and generate output
      if (defined $cached && $cached != $mask) {
         print "CRITICAL - Failover state changed from $states->{ $cached } " .
               "to $states->{ $mask }";
         exit 2;
      }
      else {
         print "OK - Failover state is $states->{ $mask }";
      } 
   }
}


sub gtm_peers {
   # instantiate variables
   my $self = shift;
   my $args = $self->{args};
   my $snmp = $self->{snmp};
   my @output = my @logical = my @physical = ();

   # set warning/critical thresholds
   my ($warn, $crit) = grep /^\d+$/ => split /:/ => $args->{l};
   $warn ||= 30;    # default 30 days
   $crit ||= 14;    # default 14 days

   # check whether we are a gtm before proceeding
   $self->{gtm} or do {
      print "OK - F5 is not a GTM\n";
      return;
   };

   # retrieve list of all logical gtm peers by name
   my @gtmservernames = $snmp->snmpbulkwalk( main::OID->{v9gtmservername} );

   # loop through each peer to verify its status according to gtm
   foreach my $peer (@gtmservernames) {
      # build oids
      my $hex = $snmp->string_to_hex( $peer );
      my $oid_type   = sprintf "%s.%s", main::OID->{v9gtmservertype}, $hex;
      my $oid_state  = sprintf "%s.%s", main::OID->{v9gtmserverstate}, $hex;
      my $oid_enable = sprintf "%s.%s", main::OID->{v9gtmserverenable}, $hex;
      my $oid_reason = sprintf "%s.%s", main::OID->{v9gtmserverreason}, $hex;

      # retrieve oids
      my ($type, $state, $enable, $reason) = $snmp->snmpget( $oid_type,
         $oid_state, $oid_enable, $oid_reason );

      # normalize reason
      $reason =~ s/^Server $peer:[ ]?//;

      # skip non-f5 peers
      $type == 0 or $type == 1 or next;

      # skip disabled peers
      $enable or next;

      # check state of peer
      if ( $state == 0 ) {
         push @output, "CRITICAL - GTM peer '$peer' error reason '$reason'";
      }
      elsif ( $state == 1 ) {
         push @output, "OK - GTM peer '$peer' available";
      }
      elsif ( $state == 2 ) {
         push @output, "WARNING - GTM peer '$peer' temporarily unavailable " .
                       "reason '$reason'";
      }
      elsif ( $state == 3 ) {
         push @output, "CRITICAL - GTM peer '$peer' not available reason " .
                       "'$reason'";
      }
      elsif ( $state == 4 ) {
         push @output, "UNKNOWN - GTM peer '$peer' unknown reason '$reason'";
      }
      elsif ( $state == 5 ) {
         push @output, "CRITICAL - GTM peer '$peer' unlicensed reason " .
                       "'$reason'";
      }

      # add peer to array to test certificate
      push @logical, $peer;
   }

   # retrieve all physical peer ip addresses and names
   my @gtmipips         = $snmp->snmpbulkwalk( main::OID->{v9gtmipip} );
   my @gtmipservernames = $snmp->snmpbulkwalk( main::OID->{v9gtmipservername} );

   # add gtm entry to physical array
   my $iphex = pack "C4", split /[.]/ => $args->{i};
   push @physical, [ $args->{h} => $iphex ];

   # loop through each logical peer and add matching physical peers to array
   foreach my $peer (@logical) {
      foreach my $i (grep { $peer eq $gtmipservernames[$_] } 0 .. $#gtmipservernames) {
         push @physical, [ $gtmipservernames[$i] => $gtmipips[$i] ];
      }
   }

   # loop through gtm itself and each physical peer to check certificate status
   foreach my $peer (@physical) {
      # instantiate local variables
      my $name = $peer->[0];
      my $ip   = join '.' => $snmp->hex_to_string( $peer->[1] );

      # build ssl connection
      my $ssl = ssl_func->new;

      # retrieve ssl certificate
      $ssl->get_cert( "$ip:443" ) or do {
         push @output, "UNKNOWN - GTM peer '$name' $@";
         next;
      };
 
      # parse begin/end dates from certificate
      my @dates = $ssl->get_cert_dates or do {
         push @output, "UNKNOWN - GTM peer '$name' $@";
         next;
      };

      # test whether cert starts in the future
      if ( $dates[0] > time ) {
         push @output, "CRITICAL - GTM peer '$name' SSL certificate " .
                       "start date is in the future: " . 
                       scalar localtime $dates[0];
         next;
      }
   
      # test whether cert is expired or expiring in the near future
      if ( $dates[1] < time ) {
         push @output, "CRITICAL - GTM peer '$name' SSL certificate " .
                       "expired at " .  scalar localtime $dates[1];
      }
      elsif ( $dates[1] < (time + 86400 * $crit) ) {
         push @output, "CRITICAL - GTM peer '$name' SSL certificate " .
                       "expires at " . scalar(localtime($dates[1])) . 
                       " (threshold $crit days)";
      }
      elsif ( $dates[1] < (time + 86400 * $warn) ) {
         push @output, "WARNING - GTM peer '$name' SSL certificate " .
                       "expires at " . scalar(localtime($dates[1])) . 
                       " (threshold $warn days)";
      }
      else {
         push @output, "OK - GTM peer '$name' SSL certificate expires at " .
                       scalar localtime $dates[1];
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
      print "OK - $ok gtm peer checks healthy\n";
      print join "\n" => @sorted;
   }
}


sub memory {
   # instantiate variables
   my $self = shift;
   my $args = $self->{args};
   my $snmp = $self->{snmp};

   # set warning/critical thresholds
   my ($warn, $crit) = grep /^\d+$/ => split /:/ => $args->{l};
   $warn ||= 80;    # default 80%
   $crit ||= 90;    # default 90%

   # version 4
   if ( $self->{majorversion} == 4) {
      # retrieve memory oids
      my ($total, $used) = $snmp->snmpget( main::OID->{v4poolmemtotal}, 
                                           main::OID->{v4poolmemused} );

      # calculate used memory percentage
      my $mem = sprintf "%d", $used / $total * 100;

      # test against thresholds and generate output
      if ($mem >= $crit) {
         print "CRITICAL - TMM memory utilization at $mem% (threshold $crit%)" .
               "|tmm=$mem";
         exit 2;
      }
      elsif ($mem >= $warn) {
         print "WARNING - TMM memory utilization at $mem% (threshold $warn%)" .
               "|tmm=$mem";
         exit 1;
      }
      else {
         print "OK - TMM memory utilization at $mem%|tmm=$mem";
      }
   }

   # all other versions
   else {
      # retrieve memory oids
      my ($total, $used) = $snmp->snmpget( main::OID->{v9tmmemtotal}, 
                                           main::OID->{v9tmmemused} ); 
      
      # calculate used memory percentage
      my $mem = sprintf "%d", $used / $total * 100;
      
      # test against thresholds and generate output
      if ($mem >= $crit) {
         print "CRITICAL - TMM memory utilization at $mem% (threshold $crit%)" .
               "|tmm=$mem";
         exit 2;
      }
      elsif ($mem >= $warn) {
         print "WARNING - TMM memory utilization at $mem% (threshold $warn%)" .
               "|tmm=$mem";
         exit 1;
      }
      else {
         print "OK - TMM memory utilization at $mem%|tmm=$mem";
      }
   }
}


sub sensors {
   # instantiate variables
   my $self = shift;
   my $args = $self->{args};
   my $snmp = $self->{snmp};
   my @output = ();
   my $threshold = { cputemp => [ 70, 75 ],          # celsius
                     cpufan  => [ 3000, 2000 ],      # rpm
                     chassisfan => [ 2000, 1500 ],   # rpm
                     systemtemp => [ 70, 75 ],       # celsius
                   };
 

   # version 4 
   if ( $self->{majorversion} == 4 ) { 
      # instantiate local variables
      my $location = { 1 => 'top', 2 => 'bottom' };
      my $status   = { 1 => 'up',  2 => 'down' };

      # retrieve f5 product code
      my $productcode = $snmp->snmpget( main::OID->{v4productcode} );
     
      # check for f5 software features
      my ($maintmode, $fastflow) = $snmp->snmpget( main::OID->{v4maintmode},
                                                   main::OID->{v4fastflow} );

      # test for maintenance mode
      if ($maintmode == 1) {
         push @output, "CRITICAL - Maintenance mode enabled";
      }
      else {
         push @output, "OK - Maintenance mode disabled";
      }
    
      # test for fast flow
      # not supported on threedns devices
      if ($fastflow == 0 && $productcode != 4) {
         push @output, "CRITICAL - Fast flow disabled";
      }
      else {
         push @output, "OK - Fast flow enabled";
      }

      # check cpu temperatures
      my @cputemps = $snmp->snmpbulkwalk( main::OID->{v4cputemp} );
      foreach my $i (0 .. $#cputemps) {
         # temporary variables for clarity
         my $warn = $threshold->{cputemp}->[0];
         my $crit = $threshold->{cputemp}->[1];
         my $temp = $cputemps[$i];

         # test against thresholds and generate output
         if ($temp >= $crit) {
            push @output, "CRITICAL - CPU $i temperature at ${temp}C " .
                          "(threshold ${crit}C)";
         }
         elsif ($temp >= $warn) {
            push @output, "WARNING - CPU $i temperature at ${temp}C " .
                          "(threshold ${warn}C)";
         }
         else {
            push @output, "OK - CPU $i temperature at ${temp}C";
         }
      } 

      # check cpu fans
      my @cpufans = $snmp->snmpbulkwalk( main::OID->{v4cpufan} );
      foreach my $i (0 .. $#cpufans) {
         # temporary variables for clarity
         my $warn = $threshold->{cpufan}->[0];
         my $crit = $threshold->{cpufan}->[1];
         my $fan  = $cpufans[$i];

         # test against thresholds and generate output
         if ($fan <= $crit) {
            push @output, "CRITICAL - CPU $i fan at $fan rpm " .
                          "(threshold $crit rpm)";
         }
         elsif ($fan <= $warn) {
            push @output, "WARNING - CPU $i fan at $fan rpm " .
                          "(threshold $warn rpm)";
         }
         else {
            push @output, "OK - CPU $i fan at $fan rpm";
         }
      }
      
      # check chassis fans
      my @chassisfans = $snmp->snmpbulkwalk( main::OID->{v4chassisfan} );
      foreach my $i (0 .. $#chassisfans) {
         # temporary variables for clarity
         my $warn = $threshold->{chassisfan}->[0];
         my $crit = $threshold->{chassisfan}->[1];
         my $fan  = $chassisfans[$i];

         # test against thresholds and generate output
         if ($fan <= $crit) {
            push @output, "CRITICAL - Chassis $i fan at $fan rpm " .
                          "(threshold $crit rpm)";
         }
         elsif ($fan <= $warn) {
            push @output, "WARNING - Chassis $i fan at $fan rpm " .
                          "(threshold $warn rpm)";
         }
         else {
            push @output, "OK - Chassis $i fan at $fan rpm";
         }
      }

      # check power supplies
      my @psustatus = $snmp->snmpbulkwalk( main::OID->{v4psustatus} );
      foreach my $i (0 .. $#psustatus) {
         # temporary variables for clarity
         my $index = $i + 1;
         my $psu  = $psustatus[$i];

         # retrieve psu location in chassis
         my $loc  = $snmp->snmpget( main::OID->{v4psulocation} . ".$index" );
         
         # generate output
         if ($psu == 2) {
            push @output, "CRITICAL - Power supply ($loc) is $status->{ $psu }";
         }
         else {
            push @output, "OK - Power supply ($loc) is $status->{ $psu }";
         } 
      }
   }

   # all other versions
   else {
      # instantiate local variables
      my $status = [ qw/bad good not-present/ ];

      # check for f5 software features
      my ($maintmode, $pvaaccel) = $snmp->snmpget( main::OID->{v9maintmode},
                                                   main::OID->{v9pvaaccel} );
      
      # test for maintenance mode
      if ($maintmode == 1) {
         push @output, "CRITICAL - Maintenance mode enabled";
      }
      else {
         push @output, "OK - Maintenance mode disabled";
      }

      # test pva acceleration
      #if ($pvaaccel == 0) {
      #   push @output, "CRITICAL - Packet Velocity ASIC acceleration is " .
      #                 " disabled";
      #}
      #elsif ($pvaaccel == 1) {
      #   push @output, "WARNING - Packet Velocity ASIC acceleration is " .
      #                 "partially running";
      #}
      #else {
         push @output, "OK - Packet Velocity ASIC acceleration is enabled";
      #}

      # check cpu temperatures
      my @cputemps = $snmp->snmpbulkwalk( main::OID->{v9cputemp} );
      foreach my $i (0 .. $#cputemps) {
         # temporary variables for clarity
         my $warn = $threshold->{cputemp}->[0];
         my $crit = $threshold->{cputemp}->[1];
         my $temp = $cputemps[$i];

         # test against thresholds and generate output
         if ($temp >= $crit) {
            push @output, "CRITICAL - CPU $i temperature at ${temp}C " .
                          "(threshold ${crit}C)";
         }
         elsif ($temp >= $warn) {
            push @output, "WARNING - CPU $i temperature at ${temp}C " .
                          "(threshold ${warn}C)";
         }
         else {
            push @output, "OK - CPU $i temperature at ${temp}C";
         }
      } 

      # check cpu fans
      my @cpufans = $snmp->snmpbulkwalk( main::OID->{v9cpufan} );
      foreach my $i (0 .. $#cpufans) {
         # temporary variables for clarity
         my $warn = $threshold->{cpufan}->[0];
         my $crit = $threshold->{cpufan}->[1];
         my $fan  = $cpufans[$i];

         # test against thresholds and generate output
         if ($fan <= $crit) {
            push @output, "CRITICAL - CPU $i fan at $fan rpm " .
                          "(threshold $crit rpm)";
         }
         elsif ($fan <= $warn) {
            push @output, "WARNING - CPU $i fan at $fan rpm " .
                          "(threshold $warn rpm)";
         }
         else {
            push @output, "OK - CPU $i fan at $fan rpm";
         }
      }
     
      # check system fans
      my @sysfans = $snmp->snmpbulkwalk( main::OID->{v9sysfanstatus} );
      foreach my $i (0 .. $#sysfans) {
         my $fan = $sysfans[$i];
         if ($fan == 0) {
            push @output, "CRITICAL - System fan $i is $status->[ $fan ]";
         }
         else {
            push @output, "OK - System fan $i is $status->[ $fan ]";
         }
      }

      # check power supplies
      my @psustatus = $snmp->snmpbulkwalk( main::OID->{v9psustatus} );
      foreach my $i (0 .. $#psustatus) {
         my $psu = $psustatus[$i];
         if ($psu == 0) {
            push @output, "CRITICAL - Power supply $i is $status->[ $psu ]";
         }
         else {
            push @output, "OK - Power supply $i is $status->[ $psu ]";
         }
      }

      # check system temperatures
      my @systemps = $snmp->snmpbulkwalk( main::OID->{v9systemp} );
      foreach my $i (0 .. $#systemps) {
         # temporary variables for clarity
         my $warn = $threshold->{systemtemp}->[0];
         my $crit = $threshold->{systemtemp}->[1];
         my $temp = $systemps[$i];

         # test against thresholds and generate output
         if ($temp >= $crit) {
            push @output, "CRITICAL - System temperature $i at ${temp}C " .
                          "(threshold ${crit}C)";
         }
         elsif ($temp >= $warn) {
            push @output, "WARNING - System temperature $i at ${temp}C " .
                          "(threshold ${warn}C)";
         }
         else {
            push @output, "OK - System temperature $i at ${temp}C";
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


sub sslusers {
   # instantiate variables
   my $self = shift;
   my $args = $self->{args};
   my $snmp = $self->{snmp};

   # set warning/critical thresholds
   my ($warn, $crit) = grep /^\d+$/ => split /:/ => $args->{l};
   $warn ||= 80;    # default 80%
   $crit ||= 90;    # default 90%

   # retrieve concurrent ssl users
   my $sslusers = $snmp->snmpget( main::OID->{v9cursslusers} );

   # test against thresolds and generate output
   if ($sslusers >= $crit) {
      print "CRITICAL - Concurrent SSL users at $sslusers (threshold $crit)" .
            "|current=$sslusers";
      exit 2;
   }
   elsif ($sslusers >= $warn) {
      print "WARNING - Concurrent SSL users at $sslusers (threshold $warn)" .
            "|current=$sslusers";
      exit 1;
   }
   else {
      print "OK - Concurrent SSL users at $sslusers";
   }
} 


sub uptime {
   # instantiate variables
   my $self = shift;
   my $args = $self->{args};
   my $snmp = $self->{snmp};

   # version 4
   if ( $self->{majorversion} == 4 ) {
      $snmp->snmp_uptime( $args, main::OID->{v4uptime}, 100 );
   }
  
   # all other versions
   else {
      $snmp->snmp_uptime( $args, main::OID->{v9uptime} );
   } 
}


sub version {
   # instantiate variables
   my $self = shift;
   my $version = $self->{fullversion};

   # define upgrade array
   my $upgrade = { #'9.1.1' => 'Bug #7213',
                 };

   # generate output
   if (exists $upgrade->{$version}) {
      print "WARNING - F5 code version $version should be upgraded: " .
            "$upgrade->{$version}";
      exit 1;
   }
   else {
      print "OK - F5 version $self->{fullversion}";
   }
}


sub virtual_server {
   # instantiate variables
   my $self   = shift;
   my $args   = $self->{args};
   my $snmp   = $self->{snmp};
   my $states = [ qw/none enabled disabled disabled-by-parent/ ];

   # build oids
   my $hexname     = $snmp->string_to_hex( $args->{l} );
   my $oid_name    = sprintf "%s.%s", main::OID->{v9ltmvsname}, $hexname;
   my $oid_state   = sprintf "%s.%s", main::OID->{v9ltmvsstate}, $hexname;
   my $oid_enabled = sprintf "%s.%s", main::OID->{v9ltmvsenabled}, $hexname;
   my $oid_reason  = sprintf "%s.%s", main::OID->{v9ltmvsreason}, $hexname;

   # temporarily disable snmp error-checking
   $snmp->{ec} = 0;

   # test whether we can find the virtual server by name
   my $vsname = $snmp->snmpget( $oid_name) or do {
      $snmp->die3( "Unable to find virtual server '$args->{l}'" );
   };

   # re-enable snmp error checking
   $snmp->{ec} = 1;

   # retrieve virtual server information
   my ($state, $enabled, $reason) = $snmp->snmpget( $oid_state, $oid_enabled, 
                                                    $oid_reason );

   # test health of virtual server
   if ( $enabled == 1) {
      if ( $state == 0 ) {
         # no color
         print "CRITICAL - Virtual server error reason '$reason'";
         exit 2;
      }
      elsif ( $state == 1 ) {
         # green
         print "OK - Virtual server available";
      }
      elsif ( $state == 2 ) {
         # yellow
         print "WARNING - Virtual server temporarily unavailable reason " .
               "'$reason'";
         exit 1;
      }
      elsif ( $state == 3 ) {
         # red
         print "CRITICAL - Virtual server not available reason '$reason'";
         exit 2;
      }
      elsif ( $state == 4 ) { 
         # blue
         print "UNKNOWN - Virtual server unknown reason '$reason'";
         exit 3;
      }
      elsif ( $state == 5 ) {
         # gray
         print "CRITICAL - Virtual server unlicensed reason '$reason'";
         exit 2;
      }
   }
   else {
      print "CRITICAL - Virtual server state is $states->[ $enabled ] reason " .
            "'$reason'";
      exit 2;
   }
}
