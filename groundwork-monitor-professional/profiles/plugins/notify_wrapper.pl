#!/usr/local/groundwork/perl/bin/perl -w
# nagios: -epn

use strict;

use constant NOTIFY        => 'notify.pl';
use constant PLUGINPATH    => '/usr/local/groundwork/nagios/libexec';
#use constant VZBNAG_CLIENT => "@{[PLUGINPATH]}/vzbnag_client.pl";
use constant VZBNAG_CLIENT => "@{[PLUGINPATH]}/vzbnag_client";

my ($agentip) = "@ARGV" =~ /--HOSTCHECKCOMMAND \S+!((?:\d{1,3}\.){3}\d{1,3})/;
die "Unable to parse AGENTIP." unless defined $agentip;

{
   open my $fh, ">> /tmp/notify_wrapper.log";
   print $fh "@ARGV\n";
   close $fh;
}

{
   local @ARGV = ( VZBNAG_CLIENT, $agentip, NOTIFY, @main::ARGV);
   system(@ARGV);
   #exec { $ARGV[0] } @ARGV;
   #do &VZBNAG_CLIENT;
}
