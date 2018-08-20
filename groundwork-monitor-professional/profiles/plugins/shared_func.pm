#!/usr/local/groundwork/perl/bin/perl -w

package shared_func;

use strict;

use lib q(/usr/local/groundwork/nagios/libexec);
use cache_func ();


sub shared_uptime {
   my ($args, $uptime) = @_;
   my $perfdata = "days=$uptime";

   # parse the warning:critical levels or default if not specified
   my ($warn, $crit) = grep /^\d+$/ => split /:/ => $args->{l};
   # don't set any default thresholds
   #$warn ||= 999;    # default to 999 days
   #$crit ||= 1999;   # default to 1999 days

   # retrieve (last) uptime check value / set
   my $cache  = cache_func->new( $args->{h} );
   my $cached = $cache->get( 'uptime' );
   $cache->set( 'uptime', $uptime );

   if ($crit and $uptime >= $crit) {
      print "CRITICAL - Device uptime $uptime days (threshold $crit)|$perfdata";
      exit 2;
   } 
   elsif ($warn and $uptime >= $warn) {
      print "WARNING - Device uptime $uptime days (threshold $warn)|$perfdata";
      exit 1;
   } 
   elsif ($uptime < 1) {
      if (defined $cached && ($cached > $uptime && $cached != 497)) {
         print "WARNING - Device reboot detected ($cached to $uptime days)" .
               "|$perfdata";
         exit 1;
      }
      else {
         print "OK - Device uptime $uptime days|$perfdata";
      }
   } 
   else {
      print "OK - Device uptime $uptime days|$perfdata";
   }
}


return 1;
