#!/usr/local/groundwork/perl/bin/perl --
#
# Copyright 2007 GroundWork Open Source, Inc. (<93>GroundWork<94>) # All rights reserved. This program is free software; you can redistribute it and/or # modify it under the terms of the GNU General Public License version 2 as published # by the Free Software Foundation.
#
# This program is distributed in the hope that it will be useful, but WITHOUT ANY # WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A # PARTICULAR PURPOSE.  See the GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License along with this # program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, # Fifth Floor, Boston, MA 02110-1301, USA.
#
use strict;
use Getopt::Long;
my %ERRORS=('OK'=>0,'WARNING'=>1,'CRITICAL'=>2,'UNKNOWN'=>3,'DEPENDENT'=>4);

my $debug = 0;
my $warn=300; # 300 secs default threshold for warning
my $crit=900; # 900 secs default threshold for critical

my $statprogram = "/usr/local/groundwork/nagios/bin/nagiostats";

my $clps = GetOptions(
        "w|warning=i"   => \$warn,
        "c|critical=i"  => \$crit,
);

sub nagexit
{
        my $errlevel = shift;
        my $string = shift;

        print "$errlevel: $string\n";
        exit $ERRORS{$errlevel};
}

if (! -x $statprogram) {nagexit ('UNKNOWN',"$statprogram not executable");}

my ($min,$max,$avg);
my @lines = `$statprogram`;
foreach my $line (@lines) {
        chomp $line;
        if ($line =~ /Active Service Latency:\s+([\d\.]+)\s\/\s([\d\.]+)\s\/\s([\d\.]+)/) {
                $min=$1;
                $max=$2;
                $avg=$3;
        }
}

if (!$avg) { nagexit ('UNKNOWN',"Nothing found"); }

my $outputstring = "Nagios latency: Min=$min, Max=$max, Avg=$avg | Min=$min;;;; Max=$max;;;; Avg=$avg;$warn;$crit;;";

if ($avg > $crit) { nagexit('CRITICAL',$outputstring);}
if ($avg > $warn) { nagexit('WARNING',$outputstring);}

# else everything fine
nagexit('OK',$outputstring);



