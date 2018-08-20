#!/usr/local/groundwork/perl/bin/perl -w

use strict;
use lib q(/usr/local/groundwork/nagios/libexec);
use cisco_fw_func ();
use host_func ();
use nagios_func ();
use parse_func ();
use ssh_func ();

use constant PLUGIN => 'check_cisco_fw';

use constant FUNCTIONS => { 'env' => \&environment };

use constant OPTIONS => { 'h'  => 'hostname',
                          't'  => { 'type of check' => FUNCTIONS },
                        };

my $args = parse_func->new(\@ARGV, OPTIONS);
my $host = host_func->new( $args->{h} ) or do {
   print "UNKNOWN - Host '$args->{h}' not found";
   exit 3;
};
my $cred = [ split /:/ => $host->decrypt( 'backup_pass' ) ];
my $ssh  = ssh_func->new( hostname => $args->{h},
                          username => $host->get( 'backup_user' ),
                          password => $cred->[0],
                        ) or do {
   print "ERROR - $@";
   exit 3;
};
my $fw   = cisco_fw_func->new( $ssh ) or do {
   print "ERROR - $@";
   exit 3;
};
defined(FUNCTIONS->{ $args->{t} }) ? FUNCTIONS->{ $args->{t} }() :
   $ssh->die3("Unknown check type: $args->{t}");
exit 0;


sub environment {
   # instantiate variables
   my @output = ();

   # test for ASA5580 platform
   $fw->{show_ver} =~ /ASA5580/ or do {
      print "OK - Environmental checks only supported on ASA5580 platform\n";
      return;
   };

   # test whether we are admin context
   if ($fw->{multicontext} && !$fw->{admincontext}) {
      print "OK - Environmental checks only supported on admin context\n";
      return;
   }

   # enter enable mode
   $fw->enable( $cred->[1] || $cred->[0] ) or do {
      print "UNKNOWN - $@";
      exit 3;
   };

   # enter system context if multicontext device
   if ( $fw->{multicontext} ) {
      $fw->changeto_system or do {
         print "UNKNOWN - $@";
         exit 3;
      };
   }

   # check fans
   my $fans = $fw->cmd( 'show environment fans' ) or do {
      print "UNKNOWN - $@";
      exit 3;
   };
   while ($fans =~ /^Cooling Fan (\d+): (\S+)$/msg) {
      my ($fan, $status) = ($1, $2);
      if ($status =~ /OK/) {
         push @output, "OK - Cooling fan $fan status is $status";
      }
      else {
         push @output, "CRITICAL - Cooling fan $fan status is $status";
      }
   }

   # check power supplies
   my $psus = $fw->cmd( 'show environment power-supplies' ) or do {
      print "UNKNOWN - $@";
      exit 3;
   };
   while ($psus =~ /^Power Supply (\d+): (\S+)$/msg) {
      my ($psu, $status) = ($1, $2);
      if ($status =~ /OK/) {
         push @output, "OK - Power supply $psu status is $status";
      }
      else {
         push @output, "CRITICAL - Power supply $psu status is $status";
      }
   }

   # check chassis temperature
   my $chassis = $fw->cmd( 'show environment temperature chassis' ) or do {
      print "UNKNOWN - $@";
      exit 3;
   };
   while ($chassis =~ /Ambient (\d+): (\d+) C - (\S+)$/msg) {
      my ($sensor, $temp, $status) = ($1, $2, $3);
      if ($status =~ /OK/) {
         push @output, "OK - Chassis temperature sensor $sensor ($status) at ${temp}C";
      }
      else {
         push @output, "CRITICAL - Chassis temperature sensor $sensor ($status) at ${temp}C";
      }
   }

   # check cpu temperature
   my $cpus = $fw->cmd( 'show environment temperature cpu' ) or do {
      print "UNKNOWN - $@";
      exit 3;
   };
   while ($cpus =~ /Processor (\d+): (\d+) C - (\S+)$/msg) {
      my ($cpu, $temp, $status) = ($1, $2, $3);
      if ($status =~ /OK/) {
         push @output, "OK - CPU $cpu temperature ($status) at ${temp}C";
      }
      else {
         push @output, "CRITICAL - CPU $cpu temperature ($status) at ${temp}C";
      }
   }

   # logoff
   $fw->exit;

   # generate output
   my @sorted = sort nagios_func::nagsort @output;
   if (grep /CRITICAL/ => @sorted) { 
      print shift(@sorted), "\n";
      print join "\n" => @sorted;
      exit 2;
   }
   elsif (grep /WARNING/ => @sorted) {
      print shift(@sorted), "\n";
      print join "\n" => @sorted;
      exit 1;
   }
   else {
      my $ok = scalar @sorted;
      print "OK - $ok environmental sensors healthy\n";
      print join "\n" => @sorted;
   }
}
