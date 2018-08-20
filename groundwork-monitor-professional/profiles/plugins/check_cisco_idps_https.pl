#!/usr/local/groundwork/perl/bin/perl -w

use strict;
use lib q(/usr/local/groundwork/nagios/libexec);
use curl_func ();
use host_func ();
use nagios_func ();
use parse_func ();
use xml_func ();

use constant PLUGIN  => 'check_cisco_idps';

use constant FUNCTIONS => { 'engine'  => \&engine,
                            'license' => \&license,
                          };

use constant OPTIONS => { 'h'  => 'hostname',
                          'l?' => 'levels [warn:crit]',
                          't'  => { 'type of check' => FUNCTIONS },
                        };

my $args = parse_func->new(\@ARGV, OPTIONS);
my $host = host_func->new( $args->{h} ) or do { 
   print "UNKNOWN - Host '$args->{h}' not found";
   exit 3;
};
my $curl = curl_func->new( '--basic'      => undef,
                           '--data'       => '@-',
                           '--header'     => 'Content-Type:text/xml',
                           '--insecure'   => undef,
                           '--ipv4'       => undef,
                           '--max-time'   => 30,
                           '--silent'     => undef,
                           '--show-error' => undef,
                         );
if ( defined(FUNCTIONS->{ $args->{t} }) ) {
   FUNCTIONS->{ $args->{t} }();
}
else {
   print "UNKNOWN - Unknown check type: $args->{t}";
   exit 3;
}
exit 0;

    
################################################################################
# engine - check engine operational status                                     #
################################################################################
sub engine {
   # instantiate variables
   my @output = ();

   # retrieve username and password
   my $user = $host->get( 'backup_user' );
   my $pass = $host->decrypt( 'backup_pass' );
   $curl->{ '--user' } = "$user:$pass";

   # build url and xml request
   my $url = "https://$args->{h}/cgi-bin/transaction-server?command=getVersion";
   my $xml = qq(<?xml version="1.0" encoding="UTF-8" standalone="yes"?>) .
             qq(<request><getHostStatistics></getHostStatistics></request>\n);

   # send request and retrieve output
   my $content = $curl->curl( $url, $xml ) or do { 
      print $@;
      exit 2;
   }; 

   # parse xml output
   my $hash = xml_func::recursive_xml( $content );

   # check for errors in xml output
   if ( exists $hash->{error} ) {
      my ($error, $msg) = each %{ $hash->{error} };
      $error =~ s/^errorMessage-//;
      print "UNKNOWN - Error '$error' returned '$msg'";
      exit 3;
   }

   # loop through each application (engine) to check the status
   my $applications = $hash->{response}->{respGetVersion}->{'partition-application'}->{applications}->{application};
   foreach my $application (@$applications) {
      if ( $application->{ executionState } eq 'running' ) {
         push @output, "OK - $application->{ name } engine in $application->{ executionState } state";
      }
      else {
         push @output, "CRITICAL - $application->{ name } engine in $application->{ executionState} state";
      }
   }

   # generate output
   my @sorted = sort nagios_func::nagsort @output;
   if ( grep /CRITICAL/ => @sorted ) {
      print join "\n" => @sorted;
      exit 2;
   }
   elsif ( grep /WARNING/ => @sorted ) {
      print join "\n" => @sorted;
      exit 1;
   }
   else {
      my $ok = @sorted;
      print "OK - $ok engine checks healthy\n";
      print join "\n" => @output;
   }
}


################################################################################
# engine - check engine operational status                                     #
################################################################################
sub license {
   # instantiate variables
   my @output = ();

   # retrieve username and password
   my $user = $host->get( 'backup_user' );
   my $pass = $host->decrypt( 'backup_pass' );
   $curl->{ '--user' } = "$user:$pass";

   # set warning/critical thresholds
   my ($warn, $crit) = grep /^\d+$/ => split /:/ => $args->{l};
   $warn ||= 30;    # default 30 days
   $crit ||= 7;     # default 7 days

   # build url and xml request
   my $url = "https://$args->{h}/cgi-bin/transaction-server?command=getVersion";
   my $xml = qq(<?xml version="1.0" encoding="UTF-8" standalone="yes"?>) .
             qq(<request><getHostStatistics></getHostStatistics></request>\n);

   # send request and retrieve output
   my $content = $curl->curl( $url, $xml ) or do {
      print $@;
      exit 2;
   };

   # parse xml output
   my $hash = xml_func::recursive_xml( $content );
  
   # check for errors in xml output 
   if ( exists $hash->{error} ) {
      my ($error, $msg) = each %{ $hash->{error} };
      $error =~ s/^errorMessage-//;
      print "UNKNOWN - Error '$error' returned '$msg'";
      exit 3;
   }

   # retrieve platform and check for eol equipment
   my $platform = $hash->{response}->{respGetVersion}->{platform};
   my $eol = $platform =~ /^IDS-/;

   # parse license expiration
   my $license = $hash->{response}->{respGetVersion}->{licenseKey};
   my $expiration = substr $license->{expirationTime}, 0, 10;
   my $string     = scalar localtime $expiration;
   my $status     = $license->{status};
   my $expires    = sprintf "%d" => ($expiration - time) / 86400;

   # test against thresholds and generate output
   if ($expires < 0) {
      my $state = $eol ? 'OK' : 'CRITICAL';
      print "$state - License expired $string";
      $eol ? exit 0 : exit 2;
   }
   elsif ($expires <= $crit) {
      my $state = $eol ? 'OK' : 'CRITICAL';
      print "$state - License expires in $expires days (threshold $crit days)";
      $eol ? exit 0 : exit 2;
   }
   elsif ($expires <= $warn) {
      my $state = $eol ? 'OK' : 'WARNING';
      print "$state - License expires in $expires days (threshold $warn days)";
      $eol ? exit 0 : exit 1;
   }
   else {
      print "OK - License expires in $expires days";
   }
}
