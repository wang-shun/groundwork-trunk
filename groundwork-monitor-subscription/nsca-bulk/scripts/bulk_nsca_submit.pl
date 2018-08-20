#!/usr/local/groundwork/perl/bin/perl -w --
#
# bulk_nsca_submit.pl
#
# Submits Nagios host and service check data from a file via send_nsca.
#
# Copyright 2009, 2011 GroundWork Open Source, Inc. ("GroundWork")
# All rights reserved.  This program is free software; you may redistribute it and/or
# modify it under the terms of the GNU General Public License version 2 as published
# by the Free Software Foundation.
#
# This program is distributed in the hope that it will be useful, but WITHOUT ANY
# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
# PARTICULAR PURPOSE.  See the GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License along with this
# program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street,
# Fifth Floor, Boston, MA 02110-1301, USA.

# Revision History:
# 07-Nov-2007 1.0	Dave Blunt	first draft
# 29-Nov-2007 1.3	Glenn Herteg	cleaned up, made efficient, and tested
# 09-Dec-2007 1.3.1	Glenn Herteg	changed version number to reflect RPM packaging
# 09-Feb-2009 2.0.0	Glenn Herteg	porting to the GroundWork Monitor 5.3 environment
# 27-Feb-2011 2.0.1	Glenn Herteg	moved the log file; cleaned up the package RPM
my $VERSION = "2.0.1";

use strict;
use Time::Local;
use Time::HiRes;

# Options you might need to adjust (but probably okay as-is).
my $debug = 1;
my $msgs_per_send = 200;
my $check_data_file = "/usr/local/groundwork/nagios/eventhandlers/check_data.log";
my $debuglog = "/usr/local/groundwork/nagios/var/log/bulk_nsca_submit.log";

# Internal variables.
my $start_time = Time::HiRes::time();
my @aggregated_commands = ();
$#aggregated_commands = $msgs_per_send;	# pre-extend the array, for efficiency
$#aggregated_commands = -1;		# truncate the array, since we don't have any messages yet

my $msg_count = 0;
my $msg_count_total = 0;

sub usage {
    print "Required arguments not given!\n\n";
    print "Bulk NSCA Submit script for Nagios, Version $VERSION\n";
    print "Copyright (c) 2009, 2011 Groundwork Open Source Solutions, All Rights Reserved \n\n";
    print "Usage:  bulk_nsca_submit.pl <host> <port> <timeout>\n";
    exit 3;
}

if (scalar @ARGV != 3) { &usage; }

my $nsca_host=$ARGV[0];
my $nsca_port=$ARGV[1];
my $nsca_timeout=$ARGV[2];

my $send_nsca_command="/usr/local/groundwork/common/bin/send_nsca -H $nsca_host -p $nsca_port -to $nsca_timeout -c /usr/local/groundwork/common/etc/send_nsca.cfg";

sub send_nsca {
    print FP "$send_nsca_command\n". join ('', @aggregated_commands) if ($debug > 1);
    open NSCA, "|-", $send_nsca_command;
    print NSCA join ('', @aggregated_commands);
    close NSCA;

    $#aggregated_commands = -1;	# truncate the array of messages
    $msg_count=0;
}

open(FP, ">>", $debuglog) if $debug;
open (DATA, "<", $check_data_file) or die "Can't open check data file $check_data_file.\n";
while (my $line = <DATA>) {
    push @aggregated_commands, $line;
    $msg_count++;
    $msg_count_total++;
    send_nsca() if ($msg_count >= $msgs_per_send);
}
send_nsca() if ($msg_count > 0);

close DATA;
if ($debug) {
    my $execution_time = sprintf("%0.3F", (Time::HiRes::time() - $start_time));
    print FP "============================================================================\n " if ($debug > 1);
    my $tmpstring = `date`;
    chomp $tmpstring;
    $tmpstring .= " Total_Messages=$msg_count_total";
    $tmpstring .= " Execution_Time=$execution_time seconds";
    print FP "$tmpstring\n";
    print FP "============================================================================\n " if ($debug > 1);
    close FP;
}
exit 0;
