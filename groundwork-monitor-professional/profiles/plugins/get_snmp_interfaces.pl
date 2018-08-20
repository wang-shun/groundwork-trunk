#!/usr/local/groundwork/perl/bin/perl -w

use strict;
use lib q(/usr/local/groundwork/nagios/libexec);
use parse_func ();
use snmp_func ();

use constant IFTYPE => [ qw/6 53 117 135 136/ ];
use constant STATE  => [ qw/UNDEF UP DOWN TESTING UNKNOWN DORMANT NOT-PRESET
                             LOWER-LAYER-DOWN/ ];

use constant OPTIONS => { 'c' => 'Community string',
                          'i' => 'IP address',
                          'v' => 'SNMP version [1 or 2c]',
                        };

my $args = parse_func->new(\@ARGV, OPTIONS);
my $snmp = snmp_func->new( host      => $args->{i},
                           version   => $args->{v},
                           community => $args->{c},
                         );

my $interface = $snmp->snmp_interface;

foreach my $ifname (keys %$interface) {
   my $if = $interface->{ $ifname };

   grep { $if->{int_type} == $_ } @{&IFTYPE} or next;

   $if->{int_name} =~ tr/\x00//d;
   $if->{int_name} =~ s/NetScreen-\d+ : (\S+)$/$1/;

   my $traffic = do {
      if ($if->{int_in_oct} !~ /^[0U]$/) {
         'YES';
      }
      elsif ($if->{int_out_oct} !~ /^[0U]$/) {
         'YES';
      }
      else {
         'NO';
      }
   };

   my $admin = STATE->[ $if->{int_admin_status} ];
   my $oper  = STATE->[ $if->{int_oper_status} ];
      
   print "$if->{int_name} => " .
         "{ admin=$admin; operational=$oper; traffic=$traffic }\n";
}
  
exit 0;
