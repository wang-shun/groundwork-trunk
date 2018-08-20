#!/usr/local/groundwork/perl/bin/perl -w --

# Copyright 2010 GroundWork Open Source, Inc. ("GroundWork").  All rights
# reserved.  Use is subject to GroundWork commercial license terms.
#
# check_ec2_availability_zone.pl
#
# Nagios plugin for determining if an availability zone within a particular region
# is present and available.
#
# Change log:
#
# 2010-11-07	v0.1	Initial version.

use strict;

use lib q(/usr/local/groundwork/nagios/libexec);

use utils qw(%ERRORS);

my $PROGNAME = "check_ec2_availability_zone.pl";
my $VERSION  = "0.1";

use Getopt::Long;
use vars qw($opt_h $opt_r $opt_v $opt_z);

my $status = GetOptions(
    'r=s' => \$opt_r, 'region=s' => \$opt_r,
    'z=s' => \$opt_z, 'zone=s'   => \$opt_z,
    'v'   => \$opt_v, 'version'  => \$opt_v,
    'h'   => \$opt_h, 'help'     => \$opt_h
);

if ($opt_v) {
    print "Version:  $PROGNAME $VERSION\n";
    exit $ERRORS{'OK'};
}

if ( $opt_h || !$opt_r || !$opt_z ) {
    print "usage:  $PROGNAME -r|--region=<r> -z|--zone=<z> [-v|--version] [-h|--help]\n";
    print "where:  <r> is the name of an EC2 region (e.g., us-east-1)\n";
    print "        <z> is the name of an EC2 availability zone (e.g., us-east-1a)\n";
    exit ($opt_h ? $ERRORS{'OK'} : $ERRORS{'CRITICAL'});
}

########################################################################
# Read availability zone data from the specified region of the clouds. #
########################################################################

my $region = $opt_r;
my $zone   = $opt_z;

# This trimming supports our convention for setting the alias of a cloud virtual host,
# which is defined to be "region/zone", and may be passed here as the region parameter.
$region =~ s{/.*}{};

# FIX MINOR:  ec2-describe-availability-zones prints error message on STDERR, which we are currently ignoring.
# FIX MINOR:  Look at the ec2-describe-availability-zones exit code as well.

my $azout_command = "ec2-describe-availability-zones '$zone'";
my @azout         = `/bin/bash -c "source /usr/local/groundwork/cloud/scripts/setenv-cloud.bash $region ; $azout_command"`;
my $state         = $ERRORS{'UNKNOWN'};
my $output        = '';

# The $azout data we will parse should look something like this:
#	AVAILABILITYZONE        us-east-1a      available       us-east-1

foreach my $azline (@azout) {
    chomp $azline;
    my @azparams = split( /\t/, $azline );
    if ( $azparams[1] eq $zone ) {
	$output = "Region \"$azparams[3]\" availability zone \"$azparams[1]\" is $azparams[2].";
	$state = ( $azparams[2] eq 'available' ) ? $ERRORS{'OK'} : $ERRORS{'CRITICAL'};
	last;
    }
}

if ( !$output ) {
    print "Region \"$region\" availability zone \"$zone\" not found.\n";
    exit $ERRORS{'CRITICAL'};
}
else {
    print "$output\n";
    exit $state;
}

exit $ERRORS{'UNKNOWN'};
