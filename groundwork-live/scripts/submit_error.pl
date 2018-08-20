#!/usr/local/groundwork/perl/bin/perl --
#
# submit results to nagios using send_nsca
# data format example  TAB separated entries one per line in a text file

# <host_name>\t<service_description>\t<return_code>\t<plugin_output>
DC-Vienna_aix-05        snmp_hpux_disk_root     2       CRITICAL FAILING | value = 0;0;0;;

# this job runs from cron on "check_interval" schedule
# data format incoming requires that the Host appear in one line (can have Service too)
# subsequent lines without Host are treated as belonging to that Host
# this is consistent with the discovery process output
# line without Service does not produce a Service sending value
# line with a Service also produces a Service value

use strict;
my $input = $ARGV[0];
my $line = "";
my $send_nsca = "/usr/local/groundwork/common/bin/send_nsca";
my $send_config = "/usr/local/groundwork/common/etc/send_nsca.cfg";
my $gw_host = "127.0.0.1"; 
my $throttle = 30;
my $delay = $throttle;

if (!open (input,$input. "FAIL")) {
        print "ERROR: Can't open script log file:" .$input. "FAIL";
        exit 2;
}
while ($line = <input>) {
       	chomp $line;
       	`echo '$line\n' | $send_nsca -H $gw_host -c $send_config`;
	$throttle--;
	if ( ! $throttle ) {
		$throttle = $delay;
		sleep 1;
	}
}
close input;

