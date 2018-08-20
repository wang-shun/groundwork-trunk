#!/usr/local/groundwork/perl/bin/perl -w --

# Copyright 2010 GroundWork Open Source, Inc. ("GroundWork").  All rights
# reserved.  Use is subject to GroundWork commercial license terms.
#
# check_eucalyptus_availability_zone.pl
#
# Nagios plugin for determining if an availability zone within a particular region
# is present, and if so if there are any problems with capacity (number of available
# [unused] instances of each VM type).
#
# Change log:
#
# 2010-03-16	v0.1	Initial version.
# 2010-03-24	v0.2	Source formatting cleanup; minor editing and bug fixing.
# 2010-04-01	v0.3	Fixed path problem in calling ec2-describe-availability-zones.
# 2010-04-06	v0.4	Added support for multiple regions.
# 2010-04-21	v0.5	Region option may now be specified as "region/zone",
#			from which the region (only) will be extracted.
# 2010-11-05	v0.6	Adjusted comments to reflect the lack of EC2 support.

use strict;

use lib q(/usr/local/groundwork/nagios/libexec);

use utils qw(%ERRORS);

my $PROGNAME = "check_eucalyptus_availability_zone.pl";
my $VERSION  = "0.6";

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
    print "where:  <r> is the name of a region (for Eucalyptus, a CLC host)\n";
    print "        <z> is the name of an availability zone\n";
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

# Note that "verbose" is required here to get the detailed output we need from Eucalyptus, but it is
# not a documented option in the current release of the EC2 API tools.  Eucalyptus treats it as an
# option producing special output, where EC2 just treats it as an invalid availability zone name.

my $azout_command = "ec2-describe-availability-zones '$zone' verbose";
my @azout         = `/bin/bash -c "source /usr/local/groundwork/cloud/scripts/setenv-cloud.bash $region ; $azout_command"`;
my $azcurr        = '';
my $state         = $ERRORS{'OK'};
my $output        = '';
my $perfoutput    = '';

# The $azout data we will parse should look something like this:
#	AVAILABILITYZONE        EucalyptusCluster       10.0.12.34
#	AVAILABILITYZONE        |- vm types     free / max   cpu   ram  disk
#	AVAILABILITYZONE        |- m1.small     0006 / 0032   1    128     2
#	AVAILABILITYZONE        |- c1.medium    0006 / 0032   1    256     5
#	AVAILABILITYZONE        |- m1.large     0003 / 0016   2    512    10
#	AVAILABILITYZONE        |- m1.xlarge    0003 / 0011   2   1024    20
#	AVAILABILITYZONE        |- c1.xlarge    0001 / 0005   4   2048    20

foreach my $azline (@azout) {
    chomp $azline;
    my @azparams = split( /\t/, $azline );
    if ( $azparams[1] !~ /\|\-/ ) {
	$azcurr = $azparams[1];
	next;
    }
    if ( $azcurr =~ /^$zone$/ ) {
	if ( $azparams[1] =~ /vm types/ ) { next; }
	$azparams[1] =~ s/\|\- //;
	my ( $free, $max ) = $azparams[2] =~ m{0*(\d+) / 0*(\d+)};
	$perfoutput .= " $azparams[1]=$free;0;;0;$max";
	if ( !$free ) {
	    $state = $ERRORS{'WARNING'};
	}
	$output .= " $azparams[1] ($free free)";
    }
    else { next; }
}

if ( !$output ) {
    print "Region \"$region\" availability zone \"$zone\" not found.\n";
    exit $ERRORS{'CRITICAL'};
}
else {
    print "VM Types:$output |$perfoutput\n";
    exit $state;
}

exit $ERRORS{'UNKNOWN'};
