#!/usr/local/groundwork/perl/bin/perl -w

use strict;
use lib q(/usr/local/groundwork/nagios/libexec);
use host_func ();
use nagios_func ();
use parse_func ();
use ssh_func ();

use constant PLUGIN => 'check_loglogic_ssh';

use constant FUNCTIONS => { 'raid' => \&raid };

use constant OPTIONS => { 'h'  => 'hostname',
                          'p?' => 'ssh port [default=22]',
                          't'  => { 'type of check' => FUNCTIONS },
                        };

my $args = parse_func->new(\@ARGV, OPTIONS);
my $host = host_func->new( $args->{h} ) or do {
   print "UNKNOWN - Host '$args->{h}' not found";
   exit 3;
};
my $ssh  = ssh_func->new( hostname => $args->{h},
                          port     => $args->{p},
                          username => $host->get( 'backup_user' ),
                          password => (split /:/ => $host->decrypt( 'backup_pass' ))[0],
                        ) or do {
   print "ERROR - $@";
   exit 3;
};
defined(FUNCTIONS->{ $args->{t} }) ? FUNCTIONS->{ $args->{t} }() :
   $ssh->die3("Unknown check type: $args->{t}");
exit 0;


sub raid {
   # instantiate variables
   my $twcli = q(/usr/sbin/tw_cli);
   my $status = {};
   my @output = ();

   # retrieve generic "show" output
   my $show = $ssh->cmd( "$twcli show" );

   # count controllers
   my @controllers = $show =~ /^(c[0-9]+)/msg;

   # loop through each controller
   foreach my $controller (@controllers) {
       # instantiate local variables
       $status->{ $controller } = {};

       # retrieve controller "show"
       my $conshow = $ssh->cmd( "$twcli /$controller show" );

       # loop through show output and parse out units, ports and bbu
       foreach my $line (split /\n/ => $conshow) {
          my @fields = split /[ ]+/ => $line or next;
          if ($fields[0] =~ /^u([0-9]+)$/) {   # unit
             $status->{ $controller }->{ "Unit $fields[0]" } = $fields[2];
          }
          elsif ($fields[0] =~ /^p([0-9]+)$/) {
             $status->{ $controller }->{ "Port $fields[0]" } = $fields[1];
          }
          elsif ($fields[0] =~ /^bbu$/) {
             $status->{ $controller }->{ "Battery backup" } = $fields[3];
          }
       }
   }

   # loop through all objects and mark non-OK states
   foreach my $controller (sort keys %$status) {
      foreach my $item (sort keys %{ $status->{ $controller } }) {
         if ( $status->{ $controller }->{ $item } =~ /OK|VERIFY/ ) {
            push @output, "OK - $item is $status->{ $controller }->{ $item }";
         }
         else {
            push @output, "CRITICAL - $item is $status->{ $controller }->{ $item }";
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
      print "OK - $ok raid checks healthy\n";
      print join "\n" => @sorted;
   }
}
