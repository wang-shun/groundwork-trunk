#!/usr/local/groundwork/perl/bin/perl -w

use strict;
use lib q(/usr/local/groundwork/nagios/libexec);
use cache_func ();
use curl_func;
use host_func ();
use nagios_func ();
use parse_func ();
use xml_func ();

use constant PLUGIN => 'check_bluecoat_https';

use constant FUNCTIONS => { 'auth_health'      => \&auth_health,
		            'xml_healthchecks' => \&xml_healthchecks,
                          };

use constant OPTIONS => { 'h'  => 'hostname',
                          'l?' => 'levels [warning:critical]',
                          't'  => { 'check type' => FUNCTIONS },
			  'p?'  => 'https port (default 8082)',
                        };

my $args = parse_func->new(\@ARGV, OPTIONS);
my $host = host_func->new( $args->{h} ) or do {
   print "UNKNOWN - Host '$args->{h}' not found";
   exit 3;
};
my $user = $host->get('backup_user');
my $pass = [ split /:/ => $host->decrypt( 'backup_pass' ) ]->[0];
my $curl = curl_func->new( '--basic'      => undef,
                           '--header'     => 'Content-Type:text/html',
                           '--insecure'   => undef,
                           '--ipv4'       => undef,
                           '--max-time'   => 30,
                           '--silent'     => undef,
                           '--show-error' => undef,
			   '--user'	  => "$user:$pass",
                         );
if ( defined(FUNCTIONS->{ $args->{t} }) ) {
   FUNCTIONS->{ $args->{t} }();
}
else {
   print "UNKNOWN - Unknown check type: $args->{t}";
   exit 3;
}
exit 0;


sub xml_healthchecks{
   # instantiate variables
   my $hash = {};
   my @output = my @perfdata = ();

   # set port to default if not otherwise specified
   $args->{p} ||= 8082;

   # set warning/critical thresholds
   my ($warn, $crit) = grep /^\d+$/ => split /:/ => $args->{l};
   $warn ||= 400;    # default 400
   $crit ||= 500;    # default 500

   # retrieve xml statistics from the blue coat proxy
   my $url = "https://$args->{h}:$args->{p}/health_check/statistics_xml";
   my $xmlhealth = $curl->curl( $url ) or do {
      print "UNKNOWN - $@";
      exit 3;
   };
   $xmlhealth =~ tr/\r\n//d;

   # check for authentication failure
   if ($xmlhealth =~ /authentication required/i) {
      print "UNKNOWN - Unable to authenticate to proxy";
      exit 3;
   }

   # parse results into hash 
   my $xmlhash = xml_func::recursive_xml( $xmlhealth );

   # check whether xml was parsed properly
   if (ref $xmlhash) {
      # loop through each healthcheck looking for status other than OK
      my $healthchecks = $xmlhash->{ HealthCheckStatus }->{ HealthCheck };
      foreach my $hc (@$healthchecks) {
         # skip disabled healthchecks
         $hc->{Enable} eq 'Enabled' or next;

         # skip radius auth healthchecks
         $hc->{Type} eq 'auth' and $hc->{Name} =~ /rad/ and next;

         # skip user-defined healthchecks
         $hc->{Type} eq 'user' and next;

         # check health of each authentication mechanism
         if ($hc->{Status} eq 'Critical') {
            push @output, "CRITICAL - Healthcheck $hc->{Name} status is " .
                          "$hc->{Status} reason $hc->{Health}";
            push @perfdata, "$hc->{Name}=U";
         }
         elsif ($hc->{Status} eq 'Warning' and $hc->{Health} ne 'Unknown') {
            if ($hc->{Type} eq 'auth' and $hc->{Health} eq 'OK on alt server') {
               push @output, "OK - Healthcheck $hc->{Name} response time is " .
                             "$hc->{Transition}->{Avg} ms";
            }
            else {
               push @output, "WARNING - Healthcheck $hc->{Name} status is " .
                             "$hc->{Status} reason $hc->{Health}";
            }
            push @perfdata, "$hc->{Name}=$hc->{Transition}->{Avg}";
         }
         elsif ($hc->{Status} eq 'OK') {
            if ($hc->{Type} eq 'drtr') {
               push @output, "OK - Healthcheck $hc->{Name} response time is " .
                             "$hc->{Transition}->{Avg} ms";
            }
            elsif ($hc->{Transition}->{Avg} >= $crit) {
               push @output, "CRITICAL - Healthcheck $hc->{Name} response " .
                             "time is $hc->{Transition}->{Avg} ms " . 
                             "(threshold $crit ms)";
            }
            elsif ($hc->{Transition}->{Avg} >= $warn) {
               push @output, "WARNING - Healthcheck $hc->{Name} response " .
                             "time is $hc->{Transition}->{Avg} ms " .
                             "(threshold $warn ms)";
            }
            else {
               push @output, "OK - Healthcheck $hc->{Name} response time is " .
                             "$hc->{Transition}->{Avg} ms";
            }
            push @perfdata, "$hc->{Name}=$hc->{Transition}->{Avg}";
         }
         else {
            push @output, "UNKNOWN - Healthcheck $hc->{Name} status is " .
                          "$hc->{Status} reason $hc->{Health}";
            push @perfdata, "$hc->{Name}=U";
         }
      }
   }
   else {
      # xml url failed, lets try standard html fallback

      # retrieve html-based url
      my $html = $curl->curl( substr($url, 0, -4) );

      # remove newlines and html breaks
      $html =~ tr/\r//d;
      $html =~ s/<BR>//g;

      # loop through each healthcheck
      while ($html =~ /^<B>(\S+)<\/B>\n(.*?)^$/msg) {
         # instantiate local variables
         my ($name, $data) = ($1, $2);
         my $hash = {};

         # parse key => value pairs
         while ($data =~ /^  (.*?):[ ]+(.*?)$/msg) {
            $hash->{ $1 } = $2;
         }

         # parse numeric value out of response time
         my ($rt) = $hash->{'Response time'} =~ /^(\d+)/;
         push @perfdata, "$name=$rt";

         # check healthcheck health and response times
         if ($hash->{ State } =~ /Functioning properly/) {
            if ($rt >= $crit) {
               push @output, "CRITICAL - Healthcheck $name response time " .
                             "is $rt ms (threshold $crit ms)";
            }
            elsif ($rt >= $warn) {
               push @output, "WARNING - Healthcheck $name response time " .
                             "is $rt ms (threshold $warn ms)";
            }
            else {
               push @output, "OK - Healthcheck $name response time is $rt ms";
            }
         }
         else {
            push @output, "CRITICAL - Healthcheck $name is $hash->{State}";
         }
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
      print "OK - $ok healthchecks healthy|@perfdata\n";
      print join "\n" => @sorted;
   }
}


sub auth_health {
   # retrieve url
   my $url = "https://$args->{h}:$args->{p}/UA/Lookup/by-all";
   my $authinfo = $curl->curl($url) or do {
      print "UNKNOWN - $@";
      exit 3;
   };

   # parse values from url output
   (my ($successtransparent) = $authinfo =~
      /Success transparent \<\/TD\>\<TD\>([\d\,]+)\<\/TD\>\<\/TR\>/) =~ tr/,//d;
   (my ($successexplicit) = $authinfo =~
      /Success explicit \<\/TD\>\<TD\>([\d\,]+)\<\/TD\>\<\/TR\>/) =~ tr/,//d;
   (my ($deniedtransparent) = $authinfo =~
      /Denied transparent \<\/TD\>\<TD\>([\d\,]+)\<\/TD\>\<\/TR\>/) =~ tr/,//d;
   (my ($deniedexplicit) = $authinfo =~
      /Denied explicit \<\/TD\>\<TD\>([\d\,]+)\<\/TD\>\<\/TR\>/) =~ tr/,//d;
  
   # retrieve cached values 
   my $cache = cache_func->new( $args->{h} );
   my $cached_successtransparent = $cache->get('successtransparent');
   my $cached_successexplicit = $cache->get('successexplicit');
   my $cached_deniedtransparent = $cache->get('deniedtransparent');
   my $cached_deniedexplicit = $cache->get('deniedexplicit');

   # set new cached values
   $cache->set('successtransparent', $successtransparent);
   $cache->set('successexplicit', $successexplicit);
   $cache->set('deniedtransparent', $deniedtransparent);
   $cache->set('deniedexplicit', $deniedexplicit);

   # calculate delta between new and cached value
   my $deltasuccesstransparent = $successtransparent - $cached_successtransparent;
   my $deltasuccessexplicit = $successexplicit - $cached_successexplicit;
   my $deltadeniedtransparent = $deniedtransparent - $cached_deniedtransparent;
   my $deltadeniedexplicit = $deniedexplicit - $cached_deniedexplicit;

   # generate output
   print "Delta:\n$deltasuccesstransparent\n$deltasuccessexplicit\n$deltadeniedtransparent\n$deltadeniedexplicit\n";
}
