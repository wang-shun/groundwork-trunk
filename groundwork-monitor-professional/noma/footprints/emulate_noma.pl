#!/usr/local/groundwork/perl/bin/perl -w
use strict;

die "Usage : $0 ( -h | -s) <state> \n" if ( not @ARGV and scalar @ARGV != 2 ) ;
my $state = $ARGV[1];

my @service_args = (
          'from@somewhere', # from
          'nagios@localhost', # to
          's', # type
          time, # time
          #'OK', # state
          $state, # state
          'PROBLEM', # tye
          'localhost', # hostname
          'HOSTALIAS', # host alias
          'HOSTADDRESS', # address
          'some id', # indicent id
          'NOTIFICATIONAUTHORALIAS',
          'NOTIFICATIONCOMMENT',
          'SERVICEOUTPUT',
          'tcp_nsca' # service name
);
my @host_args = (
          'from@somewhere', # from
          'nagios@localhost', # to
          'h', # type
          time, # time
          #'DOWN', # state
           $state,
          'PROBLEM', # tye
          'localhost', # hostname
          'HOSTALIAS', # host alias
          'HOSTADDRESS', # address
          'some id', # indicent id
          'NOTIFICATIONAUTHORALIAS',
          'NOTIFICATIONCOMMENT',
          'HOSTOUTPUT',
);

my $cmd = ""; my @args;
if ( $ARGV[0] eq '-s'  ) {
    @args =  map {qq|"$_"|} @service_args; # wrap all args in quotes
}
elsif ( $ARGV[0] eq '-h'  ) {
    @args =  ( map {qq|"$_"|} @host_args ) ; 
}
else {
    die "Usage : $0 ( -h | -s) <state> \n" ;
}

system ( "./tix.pl @args");

