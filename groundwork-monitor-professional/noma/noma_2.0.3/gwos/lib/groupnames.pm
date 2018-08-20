# groupnames.pm
#
# Retrieve hostgroup and servicegroup data from Foundation.
#
# Copyright 2017 GroundWork Open Source, Inc. (GroundWork).  All rights reserved.

use strict;
use warnings;

use CollageQuery;

sub getHostGroupAndServiceGroupNames {
    my ( $host, $service ) = @_;
    my $hostgroups    = undef;
    my $servicegroups = undef;

    eval {
	local $SIG{INT}  = 'DEFAULT';
	local $SIG{QUIT} = 'DEFAULT';
	local $SIG{TERM} = 'DEFAULT';

	my $foundation = CollageQuery->new();
	$hostgroups = $foundation->getHostGroupsForHost($host);
	$servicegroups = $foundation->getServiceGroupsForService( $host, $service ) if defined $service;
	$foundation->destroy();
    };
    if ($@) {
	debug( 'ERROR:  cannot retrieve hostgroup/servicegroup info from Foundation', 1 );
	return undef, undef;
    }

    return $hostgroups, $servicegroups;
}

1;
