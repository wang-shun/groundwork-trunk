#!/usr/local/groundwork/perl/bin/perl -w --

# =============================================================
# This plugin uses df to gather filesystem statistics and check
# the percent used of inodes.  It currently supports Linux,
# Solaris, and AIX systems.
#
# Note: the percentages passed in MUST NOT have % after them.
#
# No warranty is either implied, nor expressed herein.
#
# Overhauled by GroundWork, December 2014.
# =============================================================

use strict;
use warnings;

sub print_usage {
    print "usage:  check_inodes.pl filesystem warnpercent critpercent\n";
    print "where:  warnpercent <= critpercent\n";
}

# Make sure we got called with the right number of arguments.
if ( @ARGV != 3 ) {
    print_usage();
    exit 2;
}

my $filesystem  = $ARGV[0];
my $warnpercent = $ARGV[1];
my $critpercent = $ARGV[2];

# Validate the threshold arguments.
if ( $warnpercent !~ /^\d+$/ ) {
    print_usage();
    exit 2;
}
if ( $critpercent !~ /^\d+$/ ) {
    print_usage();
    exit 2;
}
if ($warnpercent > $critpercent) {
    print_usage();
    exit 2;
}

# Find out what kind of syntax to expect.
my $systype = `uname`;
chomp($systype);

my $df_command = $systype eq 'SunOS' ? 'df -o i' : 'df -i';

# This gets the data from the df command.
my @inputlines    = qx($df_command '$filesystem' 2>&1);
my $status        = $?;
my $raw_inputline = join( '', grep { !/Filesystem/ } @inputlines );

if ($status) {
    chomp $raw_inputline;
    $raw_inputline =~ s/\n/; /g;
    print "CRITICAL: $filesystem filesystem inode use cannot be found: $raw_inputline\n";
    exit 2;
}

# Replace all strings of spaces with a single ":"; that way, we can use split() to find fields.
( my $inputline = $raw_inputline ) =~ tr/ /:/s;

# Different OSes give back different sets of columns from the $df_command.
# This way, we can use this same plugin on multiple types of hosts.
# If none of these work, this code can be extended to cover new systems.
my ( $fs, $blks, $free, $percentused, $inodes, $iused, $ifree, $ipercent, $mntpt );
if ( $systype eq 'Linux' ) {
    ## Filesystem            Inodes   IUsed   IFree IUse% Mounted on
    ## /dev/mapper/dakota-root
    ##                      9355264 2239632 7115632   24% /
    ( $fs, $inodes, $iused, $ifree, $ipercent, $mntpt ) = split( /:/, $inputline );
}
elsif ( $systype eq 'SunOS' ) {
    ## Filesystem             iused   ifree  %iused  Mounted on
    ## /dev/dsk/c0t0d0s0     203775 1391681    13%   /
    ( $fs, $iused, $ifree, $ipercent, $mntpt ) = split( /:/, $inputline );
}
elsif ( $systype eq 'AIX' ) {
    ## Filesystem    512-blocks      Free %Used    Iused %Iused Mounted on
    ## /dev/hd4         2097152    905008   57%     8736     8% /
    ( $fs, $blks, $free, $percentused, $iused, $ipercent, $mntpt ) = split( /:/, $inputline );
}
else {
    print "CRITICAL: System type '$systype' is not (yet) supported by the check_inodes.pl plugin.\n";
    exit 2;
}

$ipercent =~ tr/%//ds;

# Sometimes the df command exits with 0 status even when it failed.
# So a failure might not be detected until now.
if ( $ipercent !~ /^\d+$/ ) {
    chomp $raw_inputline;
    $raw_inputline =~ s/\n/; /g;
    print "CRITICAL: $filesystem filesystem inode use cannot be found: $raw_inputline\n";
    exit 2;
}

# 'label'=value[UOM];[warn];[crit];[min];[max]
my $perfdata = "inode_pct=$ipercent%;$warnpercent;$critpercent;0;100";

# First we check the critical threshold, since that is, by definition and convention,
# going to be at least as large as the warning threshold.
if ( $ipercent > $critpercent ) {
    print "CRITICAL: $filesystem filesystem inode use ($ipercent%) exceeds critical threshold of $critpercent%|$perfdata\n";
    exit 2;
}

# Next we check the warning threshold.
if ( $ipercent > $warnpercent ) {
    print "WARNING: $filesystem filesystem inode use ($ipercent%) exceeds warning threshold of $warnpercent%|$perfdata\n";
    exit 1;
}

# Thanks to the magic of procedural programming,
# we figure if we got here, everything MUST be fine.
print "OK: $filesystem filesystem inode use ($ipercent) is within limits|$perfdata\n";
exit 0;

