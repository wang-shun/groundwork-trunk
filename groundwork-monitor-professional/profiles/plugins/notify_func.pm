#!/usr/local/groundwork/perl/bin/perl -w

package notify_func;

use strict;
use Net::SNMP ();
use Net::SMTP ();
use POSIX ();
use Sys::Hostname ();

use lib q(/usr/local/groundwork/nagios/libexec);
use cache_func ();


sub smarts_snmptrap {
   my $args = shift;

   # define Net::SNMP variables
   my $integer     = Net::SNMP::INTEGER;
   my $ipaddress   = Net::SNMP::IPADDRESS;
   my $octetstring = Net::SNMP::OCTET_STRING;

   # define trap enterprise oids
   my $stateful_open  = [ qw/.1.3.6.1.4.1.25516.424 1/ ];
   my $stateful_close = [ qw/.1.3.6.1.4.1.25516.424 2/ ];
   my $stateless      = [ qw/.1.3.6.1.4.1.25516.424 3/ ];

   # define oids for use within traps
   my $oid = { eventname => '.1.3.6.1.4.1.25516.424.1001',
               entityid  => '.1.3.6.1.4.1.25516.424.1002',
               ipaddress => '.1.3.6.1.4.1.25516.424.1003',
               hostname  => '.1.3.6.1.4.1.25516.424.1004',
               service   => '.1.3.6.1.4.1.25516.424.1005',
               severity  => '.1.3.6.1.4.1.25516.424.1006',
               message   => '.1.3.6.1.4.1.25516.424.1007',
             };

   # define smarts trap receivers
   my $smarts = {  '1' => [ qw/pdcd01-ic2 ndcd01-icb2/ ],
                   '2' => [ qw/pdcd02-ic2 ndcd02-icb2/ ],
                   '3' => [ qw/pdcd03-ic2 ndcd02-icb2/ ],
                   '4' => [ qw/pdcd04-ic2 ndcd04-icb2/ ],
                   '5' => [ qw/pdcd05-ic2 ndcd05-icb2/ ],
                   '6' => [ qw/pdcd06-ic2 ndcd06-icb2/ ],
                   '7' => [ qw/pdcd07-ic3 ndcd07-icb3/ ],
                   '8' => [ qw/pdcd08-oi1 ndcd08-oib1/ ],
                   '9' => [ qw/pdcd09-ic3 ndcd09-icb3/ ],
                  '10' => [ qw/pdcd10-ic3 ndcd10-icb3/ ],
                  '11' => [ qw/pdcd11-sam1 ndcd11-samb1/ ],
                  '12' => [ qw/pdcd12-ic3 ndcd12-icb3/ ],
                  '13' => [ ],
                  '14' => [ qw/pdcd14-ic2 ndcd14-icb2/ ],
                  '15' => [ qw/pdcd15-ic5 ndcd15-icb5/ ],
                  '16' => [ ],
                  '17' => [ ],
                  '18' => [ qw/pdcd18-ic5 ndcd18-icb5/ ],
                  '19' => [ ],
                  '20' => [ qw/pdcd20-oi1 ndcd20-oib1/ ],
                  '21' => [ ],
                  '22' => [ qw/pdcd22-ic2 ndcd22-icb2/ ],
                  '23' => [ qw/pdcd23-ic3 ndcd23-icb3/ ],
                  '24' => [ qw/pdcd24-ic2 ndcd24-icb2/ ],
                  '25' => [ qw/pdcd25-ic3 ndcd25-icb3/ ],
                  '26' => [ qw/pdcd26-ic2 ndcd26-icb2/ ],
                  '27' => [ ],
                  '28' => [ qw/pdcd28-ic2 ndcd28-icb2/ ],
                  '29' => [ ],
                  '30' => [ qw/pdcd30-ic2 ndcd30-icb2/ ],
                  '31' => [ qw/pdcd31-ic2 ndcd31-icb2/ ],
                  '32' => [ qw/pdcd32-ic2 ndcd32-icb2/ ],
                  '33' => [ qw/pdcd33-ic3 ndcd33-icb3/ ],
                  '34' => [ ],
                  '35' => [ qw/pdcd35-ic3 ndcd35-icb3/ ],
                  '36' => [ ],
                  '37' => [ ],
                  '38' => [ ],
                  '39' => [ ],
                  '40' => [ ],
                  '41' => [ ],
               };

   # parse numeric domain from the server hostname
   my $domain = sprintf("%d", lc(Sys::Hostname::hostname) =~ /d([0-9]+)-/) or return;

   # smarts handles host alerts itself no need to send traps
   defined $args->{HOSTSTATEID} and return;

   # define service alert variables
   my $eventname = sprintf "MSS_%s", $args->{SERVICEDESC};
   my $service   = $args->{SERVICEDESC};
   my $state     = $args->{SERVICESTATEID};

   foreach my $traphost (@{ $smarts->{ $domain } }) {
      $traphost .= '.mso.mci.com';
      my $snmp = Net::SNMP->session( -hostname  => $traphost,
                                     -port      => 162,
                                     -community => 'public',
                                     -version   => '1',
                                   ) or return;

      if ($args->{STATEFUL} == 0) {
         $snmp->trap(-enterprise   => $stateless->[0],
                     -specifictrap => $stateless->[1],
                     -agentaddr    => $args->{HOSTADDRESS},
                     -varbindlist  => [
                          $oid->{eventname}, $octetstring, $eventname,
                          $oid->{entityid},  $octetstring, $args->{HOSTALIAS},
                          $oid->{ipaddress}, $ipaddress,   $args->{HOSTADDRESS},
                          $oid->{hostname},  $octetstring, $args->{HOSTNAME},
                          $oid->{service},   $octetstring, $service,
                          $oid->{severity},  $integer,     $args->{SEVERITY},
                          $oid->{message},   $octetstring, $args->{OUTPUT},
                                      ],
                    );
      }
      elsif ($args->{STATEFUL} && $state =~ /[12]/) {
         $snmp->trap(-enterprise   => $stateful_open->[0],
                     -specifictrap => $stateful_open->[1],
                     -agentaddr    => $args->{HOSTADDRESS},
                     -varbindlist  => [
                          $oid->{eventname}, $octetstring, $eventname,
                          $oid->{entityid},  $octetstring, $args->{HOSTALIAS},
                          $oid->{ipaddress}, $ipaddress,   $args->{HOSTADDRESS},
                          $oid->{hostname},  $octetstring, $args->{HOSTNAME},
                          $oid->{service},   $octetstring, $service,
                          $oid->{severity},  $integer,     $args->{SEVERITY},
                          $oid->{message},   $octetstring, $service,
                                      ],
                    );
      }
      elsif ($args->{STATEFUL} && $state == 0) {
         $snmp->trap(-enterprise   => $stateful_close->[0],
                     -specifictrap => $stateful_close->[1],
                     -agentaddr    => $args->{HOSTADDRESS},
                     -varbindlist  => [
                          $oid->{eventname}, $octetstring, $eventname,
                          $oid->{entityid},  $octetstring, $args->{HOSTALIAS},
                          $oid->{ipaddress}, $ipaddress,   $args->{HOSTADDRESS},
                          $oid->{hostname},  $octetstring, $args->{HOSTNAME},
                          $oid->{service},   $octetstring, $service,
                          $oid->{severity},  $integer,     $args->{SEVERITY},
                          $oid->{message},   $octetstring, $service,
                                      ],
                    );
      }
   }
}


sub smtp_sendmail {
   my $args = shift;
   my $destaddr = shift;
   
   my $maildate = POSIX::strftime("%a, %d %b %Y %H:%M:%S %z", localtime);
   
   my ($service, $state, $subject) = (); 
   if ($state = $args->{HOSTSTATE}) {   # host alarm
      ($service) = $args->{HOSTCHECKCOMMAND} =~ /(\S+)!/;
      $subject = "Host $state alert for $args->{HOSTGROUPNAME}: " .
                 $args->{HOSTNAME};
      if ( $args->{HOSTPROBLEMID} ) {
         my $cache  = cache_func->new( $args->{HOSTNAME} );
         my $cached = $cache->get( $service );
         defined $cached and $cached == $args->{HOSTPROBLEMID} and return;
         $cache->set( $service, $args->{HOSTPROBLEMID} );
      }
   }
   elsif ($state = $args->{SERVICESTATE}) {   # service alarm
      $service = $args->{SERVICEDESC};
      $subject = "Service $state alert for $args->{HOSTGROUPNAME}: " .
                 "$args->{HOSTNAME}/$args->{SERVICEDESC}";
      if ( $args->{SERVICEPROBLEMID} ) {
         my $cache  = cache_func->new( $args->{HOSTNAME} );
         my $cached = $cache->get( $service );
         defined $cached and $cached == $args->{SERVICEPROBLEMID} and return;
         $cache->set( $service, $args->{SERVICEPROBLEMID} );
      }
   }
   
   (my $longoutput = $args->{LONGOUTPUT} || '') =~ s/\\n/\n/g;

   (my $body = qq/***** Converged SOC Monitoring *****

                  Time: $maildate

                  Plugin: $service

                  Notification Type: $args->{NOTIFICATIONTYPE}
                  Customer: $args->{HOSTGROUPALIAS}
                  Hostname: $args->{HOSTNAME}
                  Hostalias: $args->{HOSTALIAS}
                  Address: $args->{HOSTADDRESS}
                  State: $state

                  Alert
                  =====
                  $args->{OUTPUT}
                  $longoutput
   /) =~ s/^[ \t]+//msg;
   
   my $fromaddr = 'cary-soc-alerts@verizon.com';
   my $fromname = 'Verizon Business Converged Security Operations Center ' .
                  '<cary-soc-alerts@verizon.com>';

   my $smtp = Net::SMTP->new('127.0.0.1');
   $smtp->mail($fromaddr);
   $smtp->to(@$destaddr);
   $smtp->data();
   $smtp->datasend("From: $fromname\n");
   $smtp->datasend("To: Alarm Recipients " .
                   "<alarm.recipients\@verizon.com>\n");
   $smtp->datasend("Date: $maildate\n");
   $smtp->datasend("Reply-To: <carysoc\@verizon.com>\n");
   $smtp->datasend("Subject: $subject\n\n");
   $smtp->datasend($body);
   $smtp->datasend("\n");
   $smtp->dataend();
   $smtp->quit;
}


return 1;
