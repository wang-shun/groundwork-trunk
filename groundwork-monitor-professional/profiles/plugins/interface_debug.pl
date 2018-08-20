#!/usr/local/groundwork/perl/bin/perl -w

use strict;
use Data::Dumper ();

use lib q(/usr/local/groundwork/nagios/libexec);
use parse_func ();
use snmp_func ();

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
print Data::Dumper::Dumper $interface;
