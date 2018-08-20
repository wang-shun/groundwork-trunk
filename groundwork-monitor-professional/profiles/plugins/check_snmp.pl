#!/usr/local/groundwork/perl/bin/perl -w

use strict;
use lib q(/usr/local/groundwork/nagios/libexec);
use parse_func ();
use snmp_func ();

use constant PLUGIN => 'check_snmp';

use constant OID => '.1.3.6.1.2.1.1.1.0';   # sysDesc

use constant OPTIONS => { 'a?'  => 'AUTH passphrase  [SNMPv3]',
                          'c?'  => 'Community string [SNMPv1/2c]',
                          'i'  => 'IP address',
                          'p?'  => 'PRIV passphrase  [SNMPv3]',
                          'u?'  => 'Username         [SNMPv3]',
                          'v'  => 'SNMP Version',
                        };

my $args = parse_func->new(\@ARGV, OPTIONS);

# clean up arguments based on snmp version
if ( $args->{v} eq '1' || $args->{v} eq '2c' ) {
   $args->{a} = undef;
   $args->{p} = undef;
   $args->{u} = undef;
}
elsif ( $args->{v} eq '3' ) {
   $args->{c} = undef;
}

# build snmp object
my $snmp = snmp_func->new( authpassword => $args->{a},
                           community    => $args->{c},
                           host         => $args->{i},
                           privpassword => $args->{p},
                           user         => $args->{u},
                           version      => $args->{v},
                         );

# retrieve sysdescr oid
my $sysdesc = $snmp->snmpget( OID );

# generate output
print "OK - SNMP poll was successful";
exit 0;
