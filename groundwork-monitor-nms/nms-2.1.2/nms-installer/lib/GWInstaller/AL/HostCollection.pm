#!/usr/bin/perl
#
#Copyright 2008 GroundWork Open Source, Inc. ("GroundWork")
#All rights reserved. Use is subject to GroundWork commercial license terms. 
#

package GWInstaller::AL::HostCollection;


sub get_host{
	($self,$hostname) = @_;
	
	%host_array = $self->host_array;
	return $host_array{$hostname};
	
}


sub has_next{
	$self = shift;
	$has_next = 0;
	my @host_array = $self->host_array;
	$len = @host_array;
	if($len){
		$has_next = 1;
	}
	return $has_next
}


sub next{
	$self = shift;
	my @host_array = $self->host_array;
	$host = pop(@host_array);
	$self->host_array = \@host_array;
	return $host;
}
