#!/usr/local/groundwork/perl/bin/perl -w

use strict;
use lib q(/usr/local/groundwork/nagios/libexec);
use parse_func ();
use ssl_func ();

use constant PLUGIN => 'check_ssl';

use constant FUNCTIONS => { 'dates' => \&certificate_dates };

use constant OPTIONS => { 'h'  => 'host [name|ip]',
                          'l?' => 'levels [warning:critical]',
                          'p'  => 'port',
                          't'  => { 'type of check' => FUNCTIONS },
                        };

my $args = parse_func->new(\@ARGV, OPTIONS);
my $ssl  = ssl_func->new;
defined(FUNCTIONS->{ $args->{t} }) ? FUNCTIONS->{ $args->{t} }() : do {
   print "Unknown check type: $args->{t}\n";
   exit 3;
};
exit 0;


sub certificate_dates {
   # set warning/critical thresholds
   my ($warn, $crit) = grep /^\d+$/ => split /:/ => $args->{l};
   $warn ||= 30;    # default 30 days
   $crit ||= 7;     # default 7 days 

   # build -connect argument for openssl
   my $connect = sprintf "%s:%d", $args->{h}, $args->{p};

   # attept to retrieve and decode cert
   # any errors will return undef and set $@
   $ssl->get_cert( $connect ) or do {
      print "ERROR - $@";
      exit 3;
   };

   # retrieve start/end dates
   my @dates = $ssl->get_cert_dates or do {
      print "ERROR - $@";
      exit 3;
   };

   # retrieve cert common-name / hostname
   my $cn = $ssl->get_cert_common_name or do {
      print "ERROR - $@";
      exit 3;
   };

   # test whether cert starts in the future
   if ($dates[0] > time) {
      print "CRITICAL - SSL certificate '$cn' start date is in the future: ",
            scalar localtime $dates[0];
      exit 2;
   }

   # test whether cert is expired or expiring in the near future
   if ($dates[1] < time) {
      print "CRITICAL - SSL certificate '$cn' expired at ",
            scalar localtime $dates[1];
      exit 2;
   }
   elsif ($dates[1] < (time + 86400 * $crit)) {
      print "CRITICAL - SSL certificate '$cn' expires at ",
            scalar localtime $dates[1], " (threshold $crit days)";
      exit 2;
   }
   elsif ($dates[1] < (time + 86400 * $warn)) {
      print "WARNING - SSL certificate '$cn' expires at ",
            scalar localtime $dates[1], " (threshold $warn days)";
      exit 1;
   }
   else {
      print "OK - SSL certificate '$cn' expires at ",
            scalar localtime $dates[1];
   }
}                          
