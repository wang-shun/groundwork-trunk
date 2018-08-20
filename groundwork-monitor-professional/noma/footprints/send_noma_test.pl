#!/usr/local/groundwork/perl/bin/perl

use strict;

my $cmd = '/usr/local/groundwork/noma/notifier/alert_via_noma.pl -c h -s "DOWN" -H "burbank_ns1" -G "gw67-CACTI" -o "HOST OUTPUT" -n "CUSTOM" -a "burbank_ns1" -i "burbank_ns1" -t "2013-06-27 10:10:50" -A "Groundwork CACTI Connector" -C "Notification from Groundwork CACTI Connector"';
#my $cmd = '/usr/local/groundwork/noma/notifier/alert_via_noma.pl -c s -s "CRITICAL" -H "CSSSSL" -S "cacti" -o "Cacti dead" -n "PROBLEM" -a "CSSSSLalias" -i "CSSSSL.symprod.com" -t "2013-11-16 09:55:50" -C "Test Notification" -R "somecontact"';


# HOST 
#$cmd = '/usr/local/groundwork/noma/notifier/alert_via_noma.pl -c h -s "DOWN" -H "HOSTNAME"  -G "HOSTGROUPNAMES" -n "CUSTOM" -i "HOSTADDRESS" -o "HOSTOUTPUT" -t "TIMET" -u "HOSTNOTIFICATIONID" -A "NOTIFICATIONAUTHORALIAS" -C "NOTIFICATIONCOMMENT" -R "NOTIFICATIONRECIPIENTS"';
$cmd = '/usr/local/groundwork/noma/notifier/alert_via_noma.pl -c h -s "DOWN" -H "HOSTNAME"  -G "HOSTGROUPNAMES" -n "CUSTOM" -i "HOSTADDRESS" -o "HOSTOUTPUT" -t "TIMET" -u "HOSTNOTIFICATIONID" -A "NOTIFICATIONAUTHORALIAS" -C "NOTIFICATIONCOMMENT" -R "NOTIFICATIONRECIPIENTS"';

# SERVICE
#$cmd = '/usr/local/groundwork/noma/notifier/alert_via_noma.pl -c s -s "OK" -H "HOSTNAME" -G "HOSTGROUPNAMES" -E "SERVICEGROUPNAMES" -S "SERVICEDESC" -o "SERVICEOUTPUT" -n "PROBLEM" -a "HOSTALIAS" -i "HOSTADDRESS" -t "123" -u "SERVICENOTIFICATIONID" -A "NOTIFICATIONAUTHORALIAS" -C "NOTIFICATIONCOMMENT" -R "NOTIFICATIONRECIPIENTS"';

print "$cmd\n";
my $s = system( $cmd ) >> 8;
print "Status : $s\n";
exit;
