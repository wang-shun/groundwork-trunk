#!/usr/local/groundwork/perl/bin/perl -w

use strict;
use lib q(/usr/local/groundwork/nagios/libexec);
use curl_func ();
use host_func ();
use nagios_func ();
use parse_func ();
use ssl_func ();

use constant PLUGIN => 'check_hp_ilo';

use constant FUNCTIONS => { 'sensors' => \&sensors };

use constant OPTIONS => { 'h' => 'hostname',
                          't' => { 'type of check' => FUNCTIONS },
                        };

my $args = parse_func->new(\@ARGV, OPTIONS);
my $host = host_func->new( $args->{h} ) or do {
   print "UNKNOWN - Host '$args->{h}' not found";
   exit 3;
};
defined(FUNCTIONS->{ $args->{t} }) ? FUNCTIONS->{ $args->{t} }() : do {
   print "UNKNOWN - Unknown check type: $args->{t}";
   exit 3;
};
exit 0;


################################################################################
# sensors - check hardware sensors                                             #
################################################################################
sub sensors {
   # instantiate variables
   my @output = ();
   my $in = Symbol::gensym;
   my $out = Symbol::gensym;
   my $err = Symbol::gensym;

   # build xml request
   my $user = $host->get( 'backup_user' );
   my $pass = $host->decrypt( 'backup_pass' );
   my $xml = join '' => (
      '<?xml version="1.0"?>',
      '<RIBCL VERSION="2.0">',
      "<LOGIN USER_LOGIN=\"$user\" PASSWORD=\"$pass\">",
      '<RIB_INFO MODE="read">',
      '<GET_FW_VERSION/>',
      '</RIB_INFO>',
      '<SERVER_INFO MODE="read">',
      '<GET_EMBEDDED_HEALTH/>',
      '</SERVER_INFO>',
      '</LOGIN>',
      '</RIBCL>',
      "\n",
   );

   # test for version 3
   my $curl = curl_func->new( '--data'       => '@-',
                              '--header'     => 'Content-Type:text/xml',
                              '--insecure'   => undef,
                              '--ipv4'       => undef,
                              '--max-time'   => 30,
                              '--silent'     => undef,
                              '--show-error' => undef,
                            );
   my $content = $curl->curl( "https://$args->{h}/ribcl" ) or do {
      print "UNKNOWN - $@";
      exit 3;
   };

   my $health = do {
      if ($content =~ /RIBCL/) {
         # this is a v3 iLO
         $curl->curl( "https://$args->{h}/ribcl", $xml );
      }
      else {
         # this is a v1/v2 iLO
         my $ssl = ssl_func->new( s_client => { '-crlf'  => undef,
                                                '-quiet' => undef,
                                              },
                                );
         $ssl->s_client( "$args->{h}:443", $xml );
      }
   } or do {
      print "UNKNOWN - $@";
      exit 3;
   };

   # retrieve firmware version
   my ($version) = $health =~ /^[ ]+MANAGEMENT_PROCESSOR[ ]+= "(.*?)"$/ms;

   if ($version eq 'iLO') {
      print "OK - iLO v1 doesn't support sensors";
      return;
   }

   # check fans
   my ($fans) = $health =~ /^<FANS>\n(.*?)<\/FANS>$/ms;
   $fans ||= '';
   while ($fans =~ /^[ ]+<FAN>\n(.*?)[ ]+<\/FAN>$/msg) {
      my $fan = $1;
      my ($label)  = $fan =~ /<LABEL VALUE = "(.*?)"\/>/;
      my ($zone)   = $fan =~ /<ZONE VALUE = "(.*?)"\/>/;
      my ($status) = $fan =~ /<STATUS VALUE = "(.*?)"\/>/;
      my ($speed, $units) = $fan =~ /<SPEED VALUE = "(.*?)" UNIT="(.*?)"\/>/;
      if ($status =~ /ok/i) {
         push @output, "OK - $label ($zone) is $status at $speed $units";
      }
      else {
         push @output, "CRITICAL - $label ($zone) is $status at $speed $units";
      } 
   }

   # check temperatures
   my ($temps) = $health =~ /^<TEMPERATURE>\n(.*?)<\/TEMPERATURE>$/ms;
   $temps ||= '';
   while ($temps =~ /^[ ]+<TEMP>\n(.*?)[ ]+<\/TEMP>$/msg) {
      my $temp = $1;
      my ($label)   = $temp =~ /<LABEL VALUE = "(.*?)"\/>/;
      my ($loc)     = $temp =~ /<LOCATION VALUE = "(.*?)"\/>/;
      my ($status)  = $temp =~ /<STATUS VALUE = "(.*?)"\/>/;
      my ($curr, $currunits) = $temp =~ /<CURRENTREADING VALUE = "(.*?)" UNIT="(.*?)"\/>/;
      my ($warn, $warnunits) = $temp =~ /<CAUTION VALUE = "(.*?)" UNIT="(.*?)"\/>/;
      my ($crit, $critunits) = $temp =~ /<CRITICAL VALUE = "(.*?)" UNIT="(.*?)"\/>/;
      if ($status =~ /ok|n\/a|not installed/i) {
         push @output, "OK - $label ($loc) is $status at $curr $currunits";
      }
      elsif ($curr >= $crit) {
         push @output, "CRITICAL - $label ($loc) is $status at $curr $currunits " .
                       "(threshold $crit $critunits)";
      }   
      elsif ($curr >= $warn) {
         push @output, "WARNING - $label ($loc) is $status at $curr $currunits " .
                       "(threshold $warn $warnunits)";
      }   
   }

   # check vrms
   my ($vrms) = $health =~ /^<VRM>\n(.*?)<\/VRM>$/ms;
   $vrms ||= '';
   while ($vrms =~ /^[ ]+<MODULE>\n(.*?)[ ]+<\/MODULE>$/msg) {
      my $vrm = $1;
      my ($label)  = $vrm =~ /<LABEL VALUE = "(.*?)"\/>/;
      my ($status) = $vrm =~ /<STATUS VALUE = "(.*?)"\/>/;
      if ($status =~ /ok/i) {
         push @output, "OK - $label is $status";
      }
      else {
         push @output, "CRITICAL - $label is $status";
      }
   }

   # check power supplies
   my ($psus) = $health =~ /^<POWER_SUPPLIES>\n(.*?)<\/POWER_SUPPLIES>$/ms;
   $psus ||= '';
   while ($psus =~ /^[ ]+<SUPPLY>\n(.*?)[ ]+<\/SUPPLY>$/msg) {
      my $psu = $1;
      my ($label)  = $psu =~ /<LABEL VALUE = "(.*?)"\/>/;
      my ($status) = $psu =~ /<STATUS VALUE = "(.*?)"\/>/;
      if ($status =~ /ok/i) {
         push @output, "OK - $label is $status";
      }
      else {
         push @output, "CRITICAL - $label is $status";
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
