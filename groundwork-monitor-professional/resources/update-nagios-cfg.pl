#!/usr/local/groundwork/bin/perl
# Updated nagios.cfg for event broker

my $cfg_file = "/usr/local/groundwork/nagios/etc/nagios.cfg";

open(FILE, $cfg_file);
@CFG_FILE = <FILE>;
close(FILE);

foreach my $line (@CFG_FILE) {
  chomp $line;
  if ($line =~ /event_broker_options/) {
    print "event_broker_options=-1\n";
  }
  elsif ($line =~ /broker_module/) {
    print "broker_module=/usr/local/groundwork/nagios/modules/libbronx.so\n";
  }
  else {
    print "$line\n";
  }
}
