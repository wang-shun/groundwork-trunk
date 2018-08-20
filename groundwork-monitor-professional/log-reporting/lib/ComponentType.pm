#!/usr/bin/perl
#
#Copyright 2007 GroundWork Open Source, Inc. ("GroundWork")
#All rights reserved. Use is subject to GroundWork commercial license terms. 
#

package ComponentType;

sub new{
	my ($invocant,$id,$name) = @_;
	my $class = ref($invocant) || $invocant;
	my $self = {
		id=>$id,
		name=> $name	
	};
	bless($self,$class);
	return $self;	
	
}



sub addType{
	#columbia change table name
	my $componentType = $_[0];
	my $query = qq{
				insert into ComponentType(componentTypeName)
				values('$componentType')
				};
				 
   if(($componentType ne '') && ($componentType !~ /^\s+$/)){
	$sth = DBLib::executeQuery($query);
	$sth->finish();

   }				 			
}
sub deleteType{
	#columbia change table name
	my $componentTypeName = $_[0];
	my $query = qq{
				delete from ComponentType 
				where componentTypeName = '$componentTypeName';
				};
				 
	$sth = DBLib::executeQuery($query);
	$sth->finish();
}




sub getID{
	$self = shift;
	return $self->{id};	
}

sub queryID{
    $name = shift;
    $query = qq{
      SELECT componentTypeID
      FROM ComponentType
      WHERE componentTypeName = '$name';
    };
    $sth = DBLib::executeQuery($query);
    $sth->bind_col(1,\$typeID);
    while ($sth->fetch()){
    }
    $sth->finish();
    return $typeID;   
}
sub getName{
	$self = shift;
	return $self->{name};
}

sub getTypeList{
	my @ComponentTypeList;
	my $typeName;
	my $query = qq{ select componentTypeName 
					from ComponentType 
					ORDER BY componentTypeName
					};
	$sth = DBLib::executeQuery($query);
	$sth->bind_col( 1, \$typeName )    || reportError("Couldn't bind column");
	 	
	while($sth->fetch()){
		push(@ComponentTypeList,$typeName);
		  # print "typeName: $typeName";	
	}
	$sth->finish();
	return @ComponentTypeList;
}

1;

