#
#Copyright 2007 GroundWork Open Source, Inc. ("GroundWork")
#All rights reserved. Use is subject to GroundWork commercial license terms. 
#

package LogFilenameFilter;
use DBLib;
sub new {
  my ($invocant,
      $typeName,
      $regex) = @_;
      
	my $class = ref($invocant) || $invocant;
	$arrayRef = \@filterList;
	my $self = {
			regex=>$regex,
			typeName=>$typeName
	};
 	bless($self,$class);    
	return $self;
				
}

1;

 