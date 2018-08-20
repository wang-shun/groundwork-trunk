#!/usr/bin/perl
#
#Copyright 2007 GroundWork Open Source, Inc. ("GroundWork")
#All rights reserved. Use is subject to GroundWork commercial license terms. 
#

package GWInstaller::AL::FileSystem;


sub new{
	 
	my ($invocant,$file_system,$size,$used,$avail,$use_percent,$mount_point) = @_;
	my $class = ref($invocant) || $invocant;
	my $self = {
		file_system=>$file_system,
		size=>$size,
		used=>$used,
		avail=>$avail,
		use_percent=>$use_percent,
		mount_point=>$mount_point
	};
	bless($self,$class);
	return $self;
}

1;