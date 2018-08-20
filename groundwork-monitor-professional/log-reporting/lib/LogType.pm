#!/usr/bin/perl
#
#Copyright 2007 GroundWork Open Source, Inc. ("GroundWork")
#All rights reserved. Use is subject to GroundWork commercial license terms. 
#

package LogType;

sub new{
	my ($invocant,$dirName,$id) = @_;
	my $class = ref($invocant) || $invocant;
	my $self = {
		typeName=> $typeName,
		id=>$id
	};
	bless($self,$class);
	DBLib::initDB();
	return $self;
}

sub getID{
	$self = shift;
	return $self->{id};
}

sub getName{
	$self = shift;
	return $self->{typeName}	
}